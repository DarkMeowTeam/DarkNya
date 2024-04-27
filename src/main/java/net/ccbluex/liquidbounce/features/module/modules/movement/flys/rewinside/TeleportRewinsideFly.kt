package net.ccbluex.liquidbounce.features.module.modules.movement.flys.rewinside

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.Vec3d
import kotlin.math.cos
import kotlin.math.sin

class TeleportRewinsideFly : FlyMode("TeleportRewinside") {
    override fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return
        val connection = mc.connection ?: return

        val vectorStart = Vec3d(player.posX, player.posY, player.posZ)
        val yaw = -player.rotationYaw
        val pitch = -player.rotationPitch
        val length = 9.9
        val vectorEnd = Vec3d(sin(Math.toRadians(yaw.toDouble())) * cos(Math.toRadians(pitch.toDouble())) * length + vectorStart.x, sin(Math.toRadians(pitch.toDouble())) * length + vectorStart.y, cos(Math.toRadians(yaw.toDouble())) * cos(Math.toRadians(pitch.toDouble())) * length + vectorStart.z)
        connection.sendPacket(CPacketPlayer.Position(
            vectorEnd.x, player.posY + 2, vectorEnd.z, true)
        )
        connection.sendPacket(CPacketPlayer.Position(
            vectorStart.x, player.posY + 2, vectorStart.z, true)
        )
        player.motionY = 0.0
    }
}