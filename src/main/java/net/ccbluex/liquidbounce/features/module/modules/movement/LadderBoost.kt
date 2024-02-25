package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.ListValue

@ModuleInfo(name = "LadderBoost", description = "Boosts you up when touching a ladder.", category = ModuleCategory.MOVEMENT)
class LadderBoost : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Vanilla", "GrimAC"), "GrimAC")

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        val player = mc.player ?: return

        when (modeValue.get().toLowerCase()) {
            "vanilla" -> {
                if (player.onGround) {
                    if (player.isOnLadder) {
                        player.motionY = 1.5
                        jumped = true
                    } else jumped = false
                } else if (!player.isOnLadder && jumped) player.motionY += 0.059
            }
            "grimac" -> {
                if (mc.player.isOnLadder && mc.gameSettings.keyBindJump.isKeyDown) {
                    if (mc.player.motionY >= 0.0) {
                        mc.player.motionY = 0.1786
                    }
                }
            }
        }
    }

    companion object {
        @JvmField
        var jumped = false
    }
}