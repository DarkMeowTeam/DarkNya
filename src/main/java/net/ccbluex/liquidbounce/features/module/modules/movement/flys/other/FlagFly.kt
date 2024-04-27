package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.minecraft.network.play.client.CPacketPlayer

class FlagFly : FlyMode("Flag") {
    override fun onEnable() {
        if (mc.isSingleplayer) fly.state = false
    }
    override fun onUpdate(event: UpdateEvent) {
        if(mc.isSingleplayer) return

        val player = mc.player ?: return
        val connection = mc.connection ?: return

        connection.sendPacket(CPacketPlayer.PositionRotation(player.posX + player.motionX * 999, player.posY + (if (mc.gameSettings.keyBindJump.isKeyDown) 1.5624 else 0.00000001) - if (mc.gameSettings.keyBindSneak.isKeyDown) 0.0624 else 0.00000002, player.posZ + player.motionZ * 999, player.rotationYaw, player.rotationPitch, true))
        connection.sendPacket(CPacketPlayer.PositionRotation(player.posX + player.motionX * 999, player.posY - 6969, player.posZ + player.motionZ * 999, player.rotationYaw, player.rotationPitch, true))
        player.setPosition(player.posX + player.motionX * 11, player.posY, player.posZ + player.motionZ * 11)
        player.motionY = 0.0
    }
}