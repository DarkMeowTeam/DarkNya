package net.ccbluex.liquidbounce.features.module.modules.movement.flys.aac

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer
import net.ccbluex.liquidbounce.value.*
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.network.play.client.CPacketPlayer
import org.lwjgl.input.Keyboard



class AACFly : FlyMode("AAC") {

    private var flys = ListValue("AACFly-Mode", arrayOf("AAC1.9.10", "AAC3.0.5", "AAC3.3.12"), "AAC1.9.10")

    // Val
    private val speedAAC1910Value = FloatValue("AAC1.9.10-Speed", 0.3f, 0.2f, 1.7f).displayable { flys.equals("AAC1.9.10") }
    private val fastAAC305Value = BoolValue("AAC3.0.5-Fast", true).displayable { flys.equals("AAC.3.0.5") }
    private val AAC3312motionValue = FloatValue("AAC3.3.12-Motion", 10f, 0.1f, 10f).displayable { flys.equals("AAC.3.3.12") }

    // Var
    private var aacJump = 0.0
    private var delay = 0

    override fun onEnable() {
        aacJump = -3.8

    }

    override fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return
        val connection = mc.connection ?: return
        val timer = mc.timer as IMixinTimer

        when (flys.get()) {
            "AAC1.9.10" -> {
                if (mc.gameSettings.keyBindJump.isKeyDown) aacJump += 0.2

                if (mc.gameSettings.keyBindSneak.isKeyDown) aacJump -= 0.2

                if (fly.launchY + aacJump > player.posY) {
                    connection.sendPacket(CPacketPlayer(true))
                    player.motionY = 0.8
                    MovementUtils.strafe(speedAAC1910Value.get())
                }

                MovementUtils.strafe()
            }
            "AAC3.0.5" -> {
                if (delay == 2) {
                    player.motionY = 0.1
                } else if (delay > 2) {
                    delay = 0
                }

                if (fastAAC305Value.get()) {
                    if (player.movementInput.moveStrafe.toDouble() == 0.0) player.jumpMovementFactor =
                        0.08f else player.jumpMovementFactor = 0f
                }

                delay++
            }
           "AAC3.3.12" -> {
               if (player.posY < -70) player.motionY = AAC3312motionValue.get().toDouble()

               timer.timerSpeed = 1F

               if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                   timer.timerSpeed = 0.2F
                   mc.rightClickDelayTimer = 0
               }
           }
        }
    }

}