package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraft.util.math.BlockPos

@ModuleInfo(name = "Eagle", description = "Makes you eagle (aka. FastBridge).", category = ModuleCategory.PLAYER)
class Eagle : Module() {
    private val onlyHeldValue = BoolValue("OnlyHeldBlock", false)

    private val scaffold = DarkNya.moduleManager[Scaffold::class.java]
    private var lastHeldBlock = false

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return

        if (scaffold.state) return // 当 Scaffold 开启时不处理
        val needSneak = mc.world!!.getBlockState(BlockPos(player.posX, player.posY - 1.0, player.posZ)).block == Blocks.AIR

        @Suppress("UNNECESSARY_NOT_NULL_ASSERTION") // 这个是针对idea屏蔽警告的注释 如果无法解析可以删除
        val heldBlock = player.heldItemMainhand!!.item is ItemBlock

        if (onlyHeldValue.get() && heldBlock) { // 只有在手持方块时触发
            if (!lastHeldBlock) onDisable() // 之前没有手持方块 那就先取消潜行

            lastHeldBlock = heldBlock // 设置之前是否手持方块状态
            return
        }

        mc.gameSettings.keyBindSneak.pressed = needSneak
    }

    override fun onDisable() {
        if (mc.player == null)
            return

        if (!mc.gameSettings.keyBindSneak.isKeyDown) mc.gameSettings.keyBindSneak.pressed = false
    }
}
