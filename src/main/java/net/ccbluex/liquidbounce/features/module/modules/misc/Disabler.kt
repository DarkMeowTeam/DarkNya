package net.ccbluex.liquidbounce.features.module.modules.misc


import me.utils.PacketUtils
import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.player.InvManager
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.SPacketPlayerPosLook
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.sqrt


@ModuleInfo(name = "Disabler", description = "Great", category = ModuleCategory.MISC)
class Disabler : Module() {

    // LessLag
    private val lessLagSelectedValue = BoolValue("LessLag", false)

    // FakeLag
    private val fakeLagSelectedValue = BoolValue("FakeLag", false)
    private val fakeLagBlockValue = BoolValue("FakeLagBlock", true).displayable { fakeLagSelectedValue.get() }
    private val fakeLagPosValue = BoolValue("FakeLagPosition", true).displayable { fakeLagSelectedValue.get() }
    private val fakeLagAttackValue = BoolValue("FakeLagAttack", true).displayable { fakeLagSelectedValue.get() }
    private val fakeLagSpoofValue = BoolValue("FakeLagC03Spoof", false).displayable { fakeLagSelectedValue.get() }
    private val fakeLagDelayValue = IntegerValue("FakeLagDelay", 0, 0, 2000)
    private val fakeLagDurationValue = IntegerValue("FakeLagDuration", 200, 100, 1000)

    // GrimAC
    private val grimNoBadPacket = BoolValue("Grim-NoBadPacket-C09-C0B", true)
    private val chestStellarValue = BoolValue("ChestStealer", false)
    private val c08 = BoolValue("FastPlace", false)
    private val c0b = BoolValue("C0B", false)

    // Spartan
    private val Spartan = BoolValue("Spartan", false)

    private var isSent = false
    private val fakeLagDelay = MSTimer()
    private val fakeLagDuration = MSTimer()
    private val packetBuffer = LinkedList<Packet<INetHandlerPlayServer>>()

    //Grim-NoBadPacket-C09
    private var c09Sent = false
    private var lastC09 = 0

    //Grim-NoBadPacket-C0B
    private var lastAction = ""
    private var canSprint = false
    private var pre = false
    private val packets = LinkedBlockingQueue<Packet<*>>()

    override fun onDisable() {
        if (fakeLagSelectedValue.get()) {
            for (packet in packetBuffer) {
                PacketUtils.sendPacketNoEvent(packet)
            }
            packetBuffer.clear()
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        // FakeLag
        if (fakeLagSelectedValue.get()) {
            if (!fakeLagDelay.hasTimePassed(fakeLagDelayValue.get().toLong())) fakeLagDuration.reset()
            // Send
            if (fakeLagDuration.hasTimePassed(fakeLagDurationValue.get().toLong())) {
                fakeLagDelay.reset()
                fakeLagDuration.reset()
                for (packet in packetBuffer) {
                    PacketUtils.sendPacketNoEvent(packet)
                }
                isSent = true
                packetBuffer.clear()
            }
        }
        // Spartan
        if (Spartan.get()) {
            mc!!.gameSettings.keyBindJump.pressed = false
            mc.player!!.motionY *= 0.2
            mc.player!!.onGround = true
            (mc.timer as IMixinTimer).timerSpeed = 0.2f
            DarkNya.commandManager.executeCommands(".hclip 8")
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (lessLagSelectedValue.get()) {
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
                }
            }
        }
        if (fakeLagSelectedValue.get()) {
            if (fakeLagDelay.hasTimePassed(fakeLagDelayValue.get().toLong())) {
                if (isSent && fakeLagSpoofValue.get()) {
                    PacketUtils.sendPacketNoEvent(CPacketPlayer(true))
                    if (fakeLagDurationValue.get() >= 300) PacketUtils.sendPacketNoEvent(CPacketPlayer(true))
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
        if (packet is CPacketClickWindow && chestStellarValue.get() && DarkNya.moduleManager[InvManager::class.java].state) {
            if (!pre) {
                event.cancelEvent()
                packets.add(packet)
            }
        }
        if (packet is CPacketEntityAction && c0b.get()) {
            if (!pre) {
                event.cancelEvent()
                packets.add(packet)
            }
        }
        if (packet is CPacketPlayerTryUseItemOnBlock && c08.get()) {
            if (!pre) {
                event.cancelEvent()
                packets.add(packet)
            }
            //Grim-NoBadPacket
            if (grimNoBadPacket.get()) {
                if (packet is CPacketHeldItemChange && !c09Sent) {
                    c09Sent = true
                    lastC09 = packet.slotId
                }
                if (c09Sent) {
                    if (packet is CPacketHeldItemChange) {
                        if (packet.slotId == lastC09) {
                            event.cancelEvent()
                        } else {
                            lastC09 = packet.slotId
                        }
                    }
                }
                canSprint = !(!MovementUtils.isMoving
                        || (mc.player!!.movementInput.moveForward < 0.8f
                        || mc.player!!.isInLava || mc.player!!.isInWater
                        || mc.player!!.isInWeb
                        || mc.player!!.isOnLadder))

                if (packet is CPacketEntityAction) {
                    if (packet.action.name == lastAction) {
                        event.cancelEvent()
                    } else {
                        if (!canSprint && packet.action == CPacketEntityAction.Action.START_SPRINTING) {
                            event.cancelEvent()
                        } else {
                            lastAction = packet.action.name
                        }
                    }
                }
            }
        }
    }
}

