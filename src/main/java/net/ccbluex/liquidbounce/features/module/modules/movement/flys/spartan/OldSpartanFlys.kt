package net.ccbluex.liquidbounce.features.module.modules.movement.flys.spartan

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.ccbluex.liquidbounce.value.*
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.EnumHand

class OldSpartanFlys : FlyMode("OldSpartan") {

    private var flys = ListValue("Spartan-Mode", arrayOf("Normal", "Normal2", "Fast"), "Normal")

    private val speedValue = FloatValue("SpartanFast-Speed", 2f, 0f, 5f).displayable {flys.equals("Fast")}


    // Variables
    private val timer = TickTimer()

    override fun onEnable() {
        val player = mc.player ?: return
        val connection = mc.connection ?: return

        if (flys.equals("Fast")) {
            repeat(65) {
                connection.sendPacket(
                    CPacketPlayer.Position(
                        player.posX,
                        player.posY + 0.049,
                        player.posZ,
                        false
                    )
                )
                connection.sendPacket(
                    CPacketPlayer.Position(
                        player.posX,
                        player.posY,
                        player.posZ,
                        false
                    )
                )
            }
            connection.sendPacket(
                CPacketPlayer.Position(
                    player.posX,
                    player.posY + 0.1,
                    player.posZ,
                    true
                )
            )

            player.motionX *= 0.1
            player.motionZ *= 0.1
            player.swingArm(EnumHand.MAIN_HAND)
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return
        val connection = mc.connection ?: return

        when (flys.get()) {
            "Normal" -> {
                fly.antiDesync = true
                player.motionY = 0.0
                timer.update()
                if (timer.hasTimePassed(12)) {
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY + 8,
                            player.posZ,
                            true
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY - 8,
                            player.posZ,
                            true
                        )
                    )
                    timer.reset()
                }
            }
            "Normal2" -> {
                fly.antiDesync = true
                MovementUtils.strafe(0.264f)

                if (player.ticksExisted % 8 == 0) {
                    connection.sendPacket(CPacketPlayer.Position(player.posX, player.posY + 10, player.posZ, true))
                }
            }
            "Fast" -> {
                fly.antiDesync = true
                MovementUtils.resetMotion(true)
                if (mc.gameSettings.keyBindJump.isKeyDown) {
                    player.motionY += speedValue.get() * 0.5
                }
                if (mc.gameSettings.keyBindSneak.isKeyDown) {
                    player.motionY -= speedValue.get() * 0.5
                }

                MovementUtils.strafe(speedValue.get())
            }
        }
    }
}