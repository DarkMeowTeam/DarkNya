package net.ccbluex.liquidbounce.features.module.modules.movement.flys.ncp

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.EnumHand

class NcpFly : FlyMode("Ncp") {
    private val ncpMotionValue = FloatValue("${valuePrefix}Motion", 0f, 0f, 1f)

    override fun onEnable() {
        val player = mc.player ?: return
        val connection = mc.connection ?: return

        if (!player.onGround) return

        for (i in 0..64) {
            connection.sendPacket(CPacketPlayer.Position(player.posX, player.posY + 0.049, player.posZ, false))
            connection.sendPacket(CPacketPlayer.Position(player.posX, player.posY, player.posZ, false))
        }

        connection.sendPacket(CPacketPlayer.Position(player.posX, player.posY + 0.1, player.posZ, true))

        player.motionX *= 0.1
        player.motionZ *= 0.1
        player.swingArm(EnumHand.MAIN_HAND)
    }
        

    override fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return

        player.motionY = (-ncpMotionValue.get()).toDouble()
        if (mc.gameSettings.keyBindSneak.isKeyDown) player.motionY = -0.5
        MovementUtils.strafe()
    }
    
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is CPacketPlayer) packet.onGround = true
    }
}
