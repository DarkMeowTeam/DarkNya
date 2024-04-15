package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.ColorManage
import net.ccbluex.liquidbounce.features.module.modules.client.DebugManage
import net.ccbluex.liquidbounce.features.module.modules.player.InvManager
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.item.ItemUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.block.BlockContainer
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.inventory.ClickType
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketClickWindow
import net.minecraft.network.play.client.CPacketCloseWindow
import net.minecraft.network.play.client.CPacketConfirmTransaction
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.server.SPacketCloseWindow
import net.minecraft.network.play.server.SPacketOpenWindow
import net.minecraft.network.play.server.SPacketWindowItems
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import org.lwjgl.opengl.GL11
import java.awt.Color

@ModuleInfo(name = "ChestStealer2", description = "Automatically steals all items from a chest.", category = ModuleCategory.WORLD)
class ChestStealer : Module() {

    /**
     * OPTIONS
     */
    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 0, 0, 400) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minDelayValue.get()
            if (i > newValue)
                set(i)

            nextDelay = TimeUtils.randomDelay(minDelayValue.get(), get())
        }
    }

    private val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 0, 0, 400) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxDelayValue.get()

            if (i < newValue)
                set(i)

            nextDelay = TimeUtils.randomDelay(get(), maxDelayValue.get())
        }
    }

    private val invManagerUsefulCheckValue = BoolValue("InvManagerUsefulCheck", false)

    private val silentValue = BoolValue("Silent", false) // 静默开箱 不会有箱子GUI界面
    private val chestContainerTitleValue = ListValue("CheckContainerTitle", arrayOf("None","OnlyDefault","OnlyTitled"),"OnlyDefault")
    private val checkFailedCloseValue = BoolValue("CheckFailedClose", false) // 检查失败 是否关闭箱子


    private val autoCloseValue = BoolValue("AutoClose", false) // 自动关闭箱子 静默开箱模式不开启可能引发奇奇怪怪的反应

    private val viewerValue = BoolValue("Viewer", true)
    private val viewerFontValue = FontValue("ViewerFont", Fonts.font35)
    private val viewerScaleValue = FloatValue("ViewerScale", 1F, 1F, 4F)

    private val debugValue = BoolValue("Debug", false)

    /**
     * VALUES
     */

    private val delayTimer = MSTimer()
    private var nextDelay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())


    var chestOpenWindowId = 0
    var chestOpenSlotCount = 0
    var chestOpenItems : List<ItemStack> = listOf()
    var chestBlockPos : BlockPos? = null

    override fun onEnable() {
        chestOpenWindowId = 0
    }

    override fun onDisable() {
        chestOpenWindowId = 0
    }

    @EventTarget
    private fun onPacket(event: PacketEvent) {
        val packet = event.packet
        // 容器打开
        if (packet is SPacketOpenWindow) {
            var check = false

            if (debugValue.get()) DebugManage.info("ChestOpenPacket -> Title:${packet.windowTitle.unformattedComponentText} WindowId:${packet.windowId} SlotCount:${packet.slotCount}")

            when (chestContainerTitleValue.get().toLowerCase()) {
                "onlydefault" -> if (!isDefaultTitle(packet.windowTitle)) check = true
                "onlytitled" -> if (isDefaultTitle(packet.windowTitle)) check = true
            }
            if (checkInventoryFull()) check = true

            if (check) {
                if (checkFailedCloseValue.get()) {
                    mc?.connection?.sendPacket(CPacketCloseWindow(packet.windowId))
                    event.cancelEvent()
                }
                return
            }

            chestOpenWindowId = packet.windowId
            chestOpenSlotCount = packet.slotCount

            if (silentValue.get()) event.cancelEvent()
        }
        // 容器内物品更新
        if (packet is SPacketWindowItems) {
            if (packet.windowId == 0 || packet.windowId != chestOpenWindowId) return // 玩家自身背包或者ID不匹配跳过

            chestOpenItems = packet.itemStacks

            if (silentValue.get()) event.cancelEvent()
        }
        // 容器关闭
        if (packet is SPacketCloseWindow) chestOpenWindowId = 0


        if (packet is CPacketPlayerTryUseItemOnBlock) {
            if (BlockUtils.getBlock(packet.pos) is BlockContainer) {
                chestBlockPos = packet.pos
                if (debugValue.get()) DebugManage.info("ChestOpenPos -> ${packet.pos.x} ${packet.pos.y} ${packet.pos.z}")
            }
        }
    }
    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        if (chestOpenWindowId != 0 && chestBlockPos != null && viewerValue.get()) renderChestItemViewer(chestBlockPos!!, chestOpenItemsNoInv())
    }
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (chestOpenWindowId != 0) {
            var haveItem = false
            chestOpenItems.forEachIndexed { i , it ->
                if (!(nextDelay == 0L || delayTimer.hasTimePassed(nextDelay))) return
                // 检测是否是玩家背包格子
                if (!isChestSlot(i)) return@forEachIndexed
                // 检测是否为空
                if (it.isEmpty) return@forEachIndexed
                // 检测背包是否已满
                if (checkInventoryFull()) return@forEachIndexed
                // 检查是否需要拿
                if (!checkUseful(it)) return@forEachIndexed

                moveItem(i)

                haveItem = true

                // 重置间隔
                calcRandomDelay()
            }
            if (!haveItem) closeChest()
        }
    }

    private fun calcRandomDelay() {
        delayTimer.reset()
        nextDelay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
    }
    private fun isDefaultTitle(text: ITextComponent) : Boolean {
        val titles = arrayOf("container.chest","container.furnace","container.dispenser","container.dropper","container.hopper")
        return titles.contains(text.unformattedComponentText)
    }
    private fun isChestSlot(slot: Int) : Boolean {
        return if (chestOpenWindowId == 0) { false } else { slot < chestOpenSlotCount }
    }
    private fun checkChestOpenItemSlotId(slot: Int) : Boolean {
        return slot >= 0 && slot < chestOpenItems.size
    }
    private fun checkInventoryFull(): Boolean {
        // 检查所有主背包槽位是否都不为空 (都有物品)
        return mc.player?.inventory?.mainInventory?.all { !ItemUtils.isStackEmpty(it) } ?: false
    }
    private val invManager : InvManager = DarkNya.moduleManager[InvManager::class.java] as InvManager
    private fun checkUseful(stack: ItemStack): Boolean {
        // InvManager检查
        if (invManagerUsefulCheckValue.get()) if (!invManager.state || invManager.isUseful(stack,-1)) return false

        return true
    }
    private fun closeChest() : Boolean {
        if (debugValue.get()) DebugManage.info("ChestClosed")
        if (chestOpenWindowId == 0) return false

        if (autoCloseValue.get()) { // Fake close
            chestOpenWindowId = 0
            return true
        }

        mc?.connection?.sendPacket(CPacketCloseWindow(chestOpenWindowId))
        chestOpenWindowId = 0

        if (!silentValue.get() && mc.currentScreen is GuiContainer) mc.player.closeScreen()

        return true
    }
    private fun moveItem(slot: Int) : Boolean {
        // 是否未打开箱子
        if (chestOpenWindowId == 0 ) return false
        // 是否为无效slot
        if (!checkChestOpenItemSlotId(slot)) return false

        mc?.connection?.sendPacket(
            CPacketClickWindow(
                chestOpenWindowId,
                slot,
                0,
                ClickType.QUICK_MOVE,
                chestOpenItems[slot],
                1.toShort()
            )
        )
        mc.connection!!.sendPacket(
            CPacketConfirmTransaction(
                chestOpenWindowId,
                1.toShort(),
                true
            )
        )

        return true
    }
    private fun chestOpenItemsNoInv() : List<ItemStack> {
        val newList : MutableList<ItemStack> = mutableListOf()

        chestOpenItems.forEachIndexed { i , it ->
            if (isChestSlot(i)) newList.add(it)
        }

        return newList
    }
    /**
     * 绘制箱子上方悬浮物品预览
     * @param blockPos 箱子坐标
     * @param items 箱子内物品
     */
    private fun renderChestItemViewer(blockPos: BlockPos, items : List<ItemStack>) {
        // 暂不支持漏斗 | 传递有误
        if (items.size < 9) return

        // Push
        GL11.glPushMatrix()

        val renderManager = mc.renderManager

        // 设置渲染位置
        GL11.glTranslated(
            blockPos.x + 0.5 - renderManager.renderPosX,
            blockPos.y + 1.5 - renderManager.renderPosY,
            blockPos.z + 0.5 - renderManager.renderPosZ
        )
        // 面朝玩家
        GL11.glRotatef(-mc.renderManager.playerViewY, 0F, 1F, 0F)
        GL11.glRotatef(mc.renderManager.playerViewX, 1F, 0F, 0F)

        // 计算缩放
        val scale : Float = viewerScaleValue.get() * 0.01f
        GL11.glScalef(-scale, -scale, scale)

        GL11.glDisable(GL11.GL_TEXTURE_2D)
        RenderUtils.disableGlCap(GL11.GL_LIGHTING, GL11.GL_DEPTH_TEST)

        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        // 固定值 | 一行高度
        val heightRect = 22F
        // 计算mc内这么多格子对应的箱子高度
        val height : Int = (items.size / 9)

        // 最顶部线条 | rect
        RenderUtils.drawRect(-87F, 0F, 87F, 1F,  ColorManage.getColorByTime())
        // 物品列表展示背景 | background
        // 一行高度 = 22F   items 9成员 = 一行
        RenderUtils.drawRect(-87F, 1F, 87F, 1F + height * heightRect, Color(0, 0, 0, 160))
        // 绘制箱子内物品
        RenderHelper.enableGUIStandardItemLighting()
        val font = viewerFontValue.get()
        for (i in 1..height) {
            val startSlot = (i - 1) * 9
            renderInv(items,startSlot, startSlot + 8, -81, (6 + heightRect * (i - 1)).toInt(), font)
        }
        RenderHelper.disableStandardItemLighting()

        GL11.glDisable(GL11.GL_BLEND)

        GL11.glEnable(GL11.GL_TEXTURE_2D)

        // Pop
        GL11.glPopMatrix()
    }
    /**
     * 绘制背包
     * @param endSlot slot+9
     */
    private fun renderInv(slots: List<ItemStack>, slot: Int, endSlot: Int, x: Int, y: Int, font: FontRenderer) {
        var xOffset = x
        for (i in slot..endSlot) {
            xOffset += 18
            val stack = slots[i]

            if (stack.isEmpty) continue

            // 不会修
            // mc.renderItem.renderItemAndEffectIntoGUI(stack, xOffset - 18, y)
            mc.renderItem.renderItemOverlays(font, stack, xOffset - 18, y)
        }
    }
}