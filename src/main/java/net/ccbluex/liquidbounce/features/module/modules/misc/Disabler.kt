package net.ccbluex.liquidbounce.features.module.modules.misc


import me.utils.PacketUtils
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.SPacketPlayerPosLook
import java.util.*
import kotlin.math.sqrt


@ModuleInfo(name = "Disabler", description = "Great", category = ModuleCategory.MISC)
class Disabler : Module() {

    private val modeValue = ListValue("Mode", arrayOf("LessLag","Basic","FakeLag"), "LessLag")
    private val lagDelayValue = IntegerValue("LagDelay", 0, 0, 2000)
    private val lagDurationValue = IntegerValue("LagDuration", 200, 100, 1000)
    private val fakeLagBlockValue = BoolValue("FakeLagBlock", true)
    private val fakeLagPosValue = BoolValue("FakeLagPosition", true)
    private val fakeLagAttackValue = BoolValue("FakeLagAttack", true)
    private val fakeLagSpoofValue = BoolValue("FakeLagC03Spoof", false)
    private val debugerValue = BoolValue("Debug",false)


    private val keepAlives = arrayListOf<CPacketKeepAlive>()
    private val transactions = arrayListOf<CPacketConfirmTransaction>()
    private var isSent = false
    private val fakeLagDelay = MSTimer()
    private val fakeLagDuration = MSTimer()
    private val packetBuffer = LinkedList<Packet<INetHandlerPlayServer>>()
    override fun onDisable() {
        when (modeValue.get().toLowerCase()) {
            "fakelag" -> {
                for (packet in packetBuffer) {
                    PacketUtils.sendPacketNoEvent(packet)
                }
                packetBuffer.clear()
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        when (modeValue.get().toLowerCase()) {
            "fakelag" -> {
                if (!fakeLagDelay.hasTimePassed(lagDelayValue.get().toLong())) fakeLagDuration.reset()
                // Send
                if (fakeLagDuration.hasTimePassed(lagDurationValue.get().toLong())) {
                    fakeLagDelay.reset()
                    fakeLagDuration.reset()
                    for (packet in packetBuffer) {
                        PacketUtils.sendPacketNoEvent(packet)
                    }
                    isSent = true
                    packetBuffer.clear()
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        when (modeValue.get().toLowerCase()) {
            "lesslag" -> {
                if (packet is CPacketKeepAlive){
                    if(debugerValue.get()) {
                        ClientUtils.displayChatMessage("Reduce Ur Lag")
                    }
                }
                if (packet is SPacketPlayerPosLook) {
                    val x = packet.x - mc.player?.posX!!
                    val y = packet.y - mc.player?.posY!!
                    val z = packet.z - mc.player?.posZ!!
                    val diff = sqrt(x * x + y * y + z * z)
                    if (diff <= 8) {
                        event.cancelEvent()
                        PacketUtils.sendPacketNoEvent(
                            CPacketPlayer.PositionRotation(
                                packet.x,
                                packet.y,
                                packet.z,
                                packet.getYaw(),
                                packet.getPitch(),
                                true
                            )
                        )
                        ClientUtils.displayChatMessage("Flag Reduced")
                    } else {
                        ClientUtils.displayChatMessage("Too Far Away")
                    }
                }
            }
            "fakelag" -> {
                if (fakeLagDelay.hasTimePassed(lagDelayValue.get().toLong())) {
                    if (isSent && fakeLagSpoofValue.get()) {
                        PacketUtils.sendPacketNoEvent(CPacketPlayer(true))
                        if (lagDurationValue.get() >= 300) PacketUtils.sendPacketNoEvent(CPacketPlayer(true))
                        isSent = false
                    }
                    if (packet is CPacketKeepAlive || packet is CPacketConfirmTransaction) {
                        event.cancelEvent()
                        packetBuffer.add(packet as Packet<INetHandlerPlayServer>)
                    }
                    if (fakeLagAttackValue.get() && (packet is CPacketUseEntity || packet is CPacketAnimation)) {
                        event.cancelEvent()
                        packetBuffer.add(packet as Packet<INetHandlerPlayServer>)
                        if (packet is CPacketAnimation) return
                    }
                    if (fakeLagBlockValue.get() && (packet is CPacketPlayerDigging || packet is CPacketPlayerTryUseItemOnBlock || packet is CPacketAnimation)) {
                        event.cancelEvent()
                        packetBuffer.add(packet as Packet<INetHandlerPlayServer>)
                    }
                    if (fakeLagPosValue.get() && (packet is CPacketPlayer || packet is CPacketPlayer.Position || packet is CPacketPlayer.Rotation || packet is CPacketPlayer.PositionRotation || packet is CPacketEntityAction)) {
                        event.cancelEvent()
                        packetBuffer.add(packet as Packet<INetHandlerPlayServer>)
                    }
                }
            }
            "basic" -> {
            }
        }
    }

    override val tag: String
        get() = this.modeValue.get()
}

