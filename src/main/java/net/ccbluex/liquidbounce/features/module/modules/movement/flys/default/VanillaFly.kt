package net.ccbluex.liquidbounce.features.module.modules.movement.flys.default

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.CPacketKeepAlive
import net.minecraft.network.play.client.CPacketPlayer

class VanillaFly : FlyMode("Vanilla") {
    private val smoothValue = BoolValue("${valuePrefix}Smooth", false)
    private val speedValue = FloatValue("${valuePrefix}Speed", 2f, 0f, 5f).displayable {
        !smartSpeedValue.get()
    }
    private val vSpeedValue = FloatValue("${valuePrefix}Vertical", 2f, 0f, 5f)
    private val kickBypassValue = BoolValue("${valuePrefix}KickBypass", false)
    private val kickBypassModeValue = ListValue("${valuePrefix}KickBypassMode", arrayOf("Motion", "Packet"), "Packet").displayable { kickBypassValue.get() }
    private val kickBypassMotionSpeedValue = FloatValue("${valuePrefix}KickBypass-MotionSpeed", 0.0626F, 0.05F, 0.1F).displayable { kickBypassModeValue.get() == "Motion" && kickBypassValue.get() }
    private val keepAliveValue = BoolValue("${valuePrefix}KeepAlive", false)
    private val noClipValue = BoolValue("${valuePrefix}NoClip", false)
    private val spoofValue = BoolValue("${valuePrefix}SpoofGround", false)

    private val smartSpeedValue = BoolValue("${valuePrefix}SmartSpeed", false)
    private val smartSpeedModeValue = ListValue("${valuePrefix}SmartSpeedMode", arrayOf("NoEntityNear"), "NoEntityNear").displayable{
        smartSpeedValue.get()
    }

    private val smartSpeedEntityDistanceValue = IntegerValue("${valuePrefix}EntityDistance", 255,0,255).displayable {
        smartSpeedValue.get()
    }.displayable {
        smartSpeedModeValue.get().toLowerCase() == "noentitynear"
    }
    private val smartSpeedEntityOnlyTargetValue = BoolValue("${valuePrefix}EntityOnlyTarget", false).displayable {
        smartSpeedValue.get()
    }.displayable {
        smartSpeedModeValue.get().toLowerCase() == "noentitynear"
    }

    private val smartSpeedNormalSpeedValue = FloatValue("SpeedNormal", 2f, 0f, 10f)
    private val smartSpeedActiveSpeedValue = FloatValue("SpeedActive", 10f, 0f, 10f)


    private var packets = 0
    private var kickBypassMotion = 0f

    private var calcTicket = 0

    override fun onEnable() {
        packets = 0
        kickBypassMotion = 0f
    }

    override fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return

        if (smartSpeedValue.get()) {
            calcTicket++ // 每10ticket计算一次 减少资源占用
            if (calcTicket >= 10) {
                calcTicket = 0
                speedValue.set(
                    if (smartSpeed()) {
                        smartSpeedActiveSpeedValue.get()
                    } else {
                        smartSpeedNormalSpeedValue.get()
                    }
                )
            }
        }

        if (keepAliveValue.get()) mc.connection?.sendPacket(CPacketKeepAlive())
        if (noClipValue.get()) mc.player.noClip = true


        if(kickBypassValue.get()) {
            if(kickBypassModeValue.get() === "Motion") {
                kickBypassMotion = kickBypassMotionSpeedValue.get()

                if (player.ticksExisted % 2 == 0) {
                    kickBypassMotion = -kickBypassMotion
                }

                if(!mc.gameSettings.keyBindJump.pressed && !mc.gameSettings.keyBindSneak.pressed) {
                    player.motionY = kickBypassMotion.toDouble()
                }
            }
        }

        if(smoothValue.get()) {
            player.capabilities.isFlying = true
            player.capabilities.flySpeed = speedValue.get() * 0.05f
        } else {
            player.capabilities.isFlying = false
            MovementUtils.resetMotion(true)
            if (mc.gameSettings.keyBindJump.isKeyDown) player.motionY += vSpeedValue.get()

            if (mc.gameSettings.keyBindSneak.isKeyDown) player.motionY -= vSpeedValue.get()

            MovementUtils.strafe(speedValue.get())
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is CPacketPlayer) {
            if(spoofValue.get()) packet.onGround = true
            if (packets++ >= 40 && kickBypassValue.get()) {
                packets = 0
                if(kickBypassModeValue.get() === "Packet") {
                    MovementUtils.handleVanillaKickBypass()
                }
            }
        }
    }

    private fun smartSpeed() : Boolean {
        var active = false
        when (smartSpeedModeValue.get().toLowerCase())
        {
            "noentitynear" -> {
                mc.world.loadedEntityList.forEach { entity ->
                    if (entity == mc.player) return@forEach
                    if (smartSpeedEntityOnlyTargetValue.get() && !EntityUtils.isSelected(entity,false)) return@forEach

                    if (entity.getDistance(mc.player) <= smartSpeedEntityDistanceValue.get()) active = true
                }
            }
        }
        return active
    }
}
