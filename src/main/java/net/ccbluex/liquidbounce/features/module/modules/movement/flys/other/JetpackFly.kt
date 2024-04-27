package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.util.EnumParticleTypes

class JetpackFly : FlyMode("Jetpack") {
    private val particleValue = BoolValue("${valuePrefix}Particle", true)
    override fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return

        if (mc.gameSettings.keyBindJump.isKeyDown) {
            if(particleValue.get()) mc.effectRenderer.spawnEffectParticle(EnumParticleTypes.FLAME.particleID, player.posX, player.posY + 0.2, player.posZ, -player.motionX, -0.5, -player.motionZ)

            player.motionY += 0.15
            player.motionX *= 1.1
            player.motionZ *= 1.1
        }
    }
}