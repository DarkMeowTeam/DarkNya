package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.exploit.Phase
import net.ccbluex.liquidbounce.value.FloatValue

@ModuleInfo(name = "Step", description = "Allows you to step up blocks.", category = ModuleCategory.MOVEMENT)
class Step : Module() {

    /**
     * OPTIONS
     */
    private val heightValue = FloatValue("Height", 1F, 0.6F, 10F)

    /**
     * VALUES
     */

    private var isStep = false
    private var stepX = 0.0
    private var stepY = 0.0
    private var stepZ = 0.0

    override fun onDisable() {
        val player = mc.player ?: return

        // Change step height back to default (0.5 is default)
        player.stepHeight = 0.5F
    }

    @EventTarget
    fun onStep(event: StepEvent) {
        val player = mc.player ?: return

        // Phase should disable step
        if (DarkNya.moduleManager[Phase::class.java]!!.state) {
            event.stepHeight = 0F
            return
        }

        // Set step height
        val height = heightValue.get()
        player.stepHeight = height
        event.stepHeight = height

        // Detect possible step
        if (event.stepHeight > 0.5F) {
            isStep = true
            stepX = player.posX
            stepY = player.posY
            stepZ = player.posZ
        }
    }

    @EventTarget(ignoreCondition = true)
    fun onStepConfirm(event: StepConfirmEvent) {
        val player = mc.player

        if (player == null || !isStep) // Check if step
            return

        isStep = false
        stepX = 0.0
        stepY = 0.0
        stepZ = 0.0
    }
}