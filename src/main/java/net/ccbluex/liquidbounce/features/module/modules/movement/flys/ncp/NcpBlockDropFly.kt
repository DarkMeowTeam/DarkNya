package net.ccbluex.liquidbounce.features.module.modules.movement.flys.ncp

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.server.SPacketPlayerPosLook

class NcpBlockDropFly : FlyMode("NcpBlockDrop") {
    private val hSpeedValue = FloatValue("${valuePrefix}HorizontalSpeed", 1f, 0.1f, 5f)
    private val vSpeedValue = FloatValue("${valuePrefix}VerticalSpeed", 1f, 0.1f, 5f)
    private var startx = 0.0
    private var starty = 0.0
    private var startz = 0.0
    private var startyaw = 0f
    private var startpitch = 0f
    
    override fun onEnable() {
        val player = mc.player ?: return

        startx = player.posX
        starty = player.posY
        startz = player.posZ
        startyaw = player.rotationYaw
        startpitch = player.rotationPitch
    }
        

    override fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return
        
        MovementUtils.resetMotion(true)
        if (mc.gameSettings.keyBindJump.isKeyDown) player.motionY = vSpeedValue.get().toDouble()
        if (mc.gameSettings.keyBindSneak.isKeyDown) player.motionY -= vSpeedValue.get().toDouble()
        MovementUtils.strafe(hSpeedValue.get())
        
        repeat(2) {
            PacketUtils.sendPacketNoEvent(
                CPacketPlayer.PositionRotation(
                    startx,
                    starty,
                    startz,
                    startyaw,
                    startpitch,
                    true
                )
            )
        }
        repeat(2) {
            PacketUtils.sendPacketNoEvent(
                CPacketPlayer.PositionRotation(
                    player.posX,
                    player.posY,
                    player.posZ,
                    startyaw,
                    startpitch,
                    false
                )
            )
        }
    }
    
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is CPacketPlayer) {
            event.cancelEvent()
        }
        if (packet is SPacketPlayerPosLook) {
            startx = packet.x
            starty = packet.y
            startz = packet.z
            startyaw = packet.getYaw()
            startpitch = packet.getPitch()
            event.cancelEvent()
        }
    }
}
