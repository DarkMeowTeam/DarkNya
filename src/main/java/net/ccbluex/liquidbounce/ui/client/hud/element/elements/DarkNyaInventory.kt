package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.features.module.modules.client.ColorManage
import net.ccbluex.liquidbounce.value.*
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import java.awt.Color

@ElementInfo(name = "DarkNyaInventory")
class DarkNyaInventory(x: Double = -1.0, y: Double = 121.0, scale: Float = 1F) : Element(x, y, scale) {

    private val titleValue = BoolValue("Title",true)
    private val shadowValue = BoolValue("Shadow", true)
    private val fontValue = FontValue("Font", Fonts.font35)

    override fun drawElement(): Border {
        val backgroundColor = Color(0, 0, 0, 160)
        val font = fontValue.get()
        var startY = 1F


        // 顶部线条
        RenderUtils.drawRect(0F, 0F, 174F, 1F,  ColorManage.getColorByTime())
        if (titleValue.get()) {
            startY = 14F
            RenderUtils.drawRect(0F, 1F, 174F, startY, Color(0, 0, 0 , 180).rgb)
            Fonts.font40.drawStringWithShadow(
                "Inventory", 5F,3F, ColorManage.getColorByTime()
            )
        }

        // draw rect
        RenderUtils.drawRect(0F, startY, 174F, 66F + startY, backgroundColor)
        if (shadowValue.get()) {
            RenderUtils.drawShadowWithCustomAlpha(0F, 0F, 174F, 66F + startY, 255f)
        }

        // render item
        RenderHelper.enableGUIStandardItemLighting()
        renderInv(9, 17, 6, 6 + startY.toInt(), font)
        renderInv(18, 26, 6, 24 + startY.toInt(), font)
        renderInv(27, 35, 6, 42 + startY.toInt(), font)
        RenderHelper.disableStandardItemLighting()
        GlStateManager.enableAlpha()
        GlStateManager.disableBlend()
        GlStateManager.disableLighting()

        return Border(0F, 0F, 174F, 66F + startY)
    }

    /**
     * render single line of inventory
     * @param endSlot slot+9
     */
    private fun renderInv(slot: Int, endSlot: Int, x: Int, y: Int, font: FontRenderer) {
        var xOffset = x
        for (i in slot..endSlot) {
            xOffset += 18
            val stack = mc.player.inventoryContainer.getSlot(i).stack ?: continue

            mc.renderItem.renderItemAndEffectIntoGUI(stack, xOffset - 18, y)
            mc.renderItem.renderItemOverlays(font, stack, xOffset - 18, y)
        }
    }

}