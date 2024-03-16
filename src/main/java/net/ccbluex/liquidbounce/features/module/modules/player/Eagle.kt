package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.world.ChestAura
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold
import net.ccbluex.liquidbounce.features.module.modules.world.ScaffoldHelper
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraft.util.math.BlockPos

@ModuleInfo(name = "Eagle", description = "Makes you eagle (aka. FastBridge).", category = ModuleCategory.PLAYER)
class Eagle : Module() {
    private val onlyHeldValue = BoolValue("OnlyHeldBlock", false)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return

        if (DarkNya.moduleManager[Scaffold::class.java].state) return
        if (DarkNya.moduleManager[ScaffoldHelper::class.java].state) return

        if (onlyHeldValue.get() && player.heldItemMainhand!!.item !is ItemBlock) return



        mc.gameSettings.keyBindSneak.pressed = mc.world!!.getBlockState(BlockPos(player.posX, player.posY - 1.0, player.posZ)).block == Blocks.AIR
    }

    override fun onDisable() {
        if (mc.player == null)
            return

        if (!mc.gameSettings.keyBindSneak.isKeyDown)
            mc.gameSettings.keyBindSneak.pressed = false
    }
}
