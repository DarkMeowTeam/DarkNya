package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue

@ModuleInfo(name = "NoClip", description = "Allows you to freely move through walls (A sandblock has to fall on your head).", category = ModuleCategory.MOVEMENT)
class NoClip : Module() {
    private val resetFallDistanceValue = BoolValue("ResetFallDistance",true)
    private val speedValue = FloatValue("Speed", 0.1f, 0f, 5f)

    override fun onDisable() {
        mc.player?.noClip = false
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return

        player.noClip = true
        if (resetFallDistanceValue.get()) player.fallDistance = 0f
        player.onGround = false

        player.capabilities.isFlying = false
        player.motionX = 0.0
        player.motionY = 0.0
        player.motionZ = 0.0

        val speed = speedValue.get()

        player.jumpMovementFactor = speed

        if (mc.gameSettings.keyBindJump.isKeyDown)
            player.motionY += speed.toDouble()

        if (mc.gameSettings.keyBindSneak.isKeyDown)
            player.motionY -= speed.toDouble()
    }
}
