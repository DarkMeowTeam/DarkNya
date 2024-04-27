package net.ccbluex.liquidbounce.features.module.modules.movement.flys.ncp

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.EnumHand

class OldNcpFly : FlyMode("OldNcp") {
    private var startY = 0.0

    override fun onEnable() {
        val player = mc.player ?: return
        val connection = mc.connection ?: return

        if (!player.onGround) return

        startY = player.posY

        for (i in 0..3) {
            connection.sendPacket(CPacketPlayer.Position(player.posX, player.posY + 1.01, player.posZ, false))
            connection.sendPacket(CPacketPlayer.Position(player.posX, player.posY, player.posZ, false))
        }

        player.jump()
        player.swingArm(EnumHand.MAIN_HAND)
    }
        

    override fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return

        if (startY > player.posY)
            player.motionY = -0.000000000000000000000000000000001
        if (mc.gameSettings.keyBindSneak.isKeyDown)
            player.motionY = -0.2
        if (mc.gameSettings.keyBindJump.isKeyDown && player.posY < startY - 0.1)
            player.motionY = 0.2
        MovementUtils.strafe()
    }
}
