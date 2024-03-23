package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.client.settings.GameSettings

class LegitSpeedTimer : SpeedMode("LegitTimer") {
    private var wasOnGround = false
    
    override fun onUpdate() {
        (mc.timer as IMixinTimer).timerSpeed = 1.004f
        if (MovementUtils.isMoving) {
            if (mc.player.onGround) {
                mc.gameSettings.keyBindJump.pressed = true
                wasOnGround = true
            } else {
                if (wasOnGround) {
                    mc.gameSettings.keyBindJump.pressed = false
                    wasOnGround = false
                } else {
                    mc.gameSettings.keyBindJump.pressed = true
                }
            }
        }
    }
    override fun onMove(event: MoveEvent) {}
    override fun onMotion(event: MotionEvent) {}
    override fun onDisable() {
        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        (mc.timer as IMixinTimer).timerSpeed = 1f
    }

}
