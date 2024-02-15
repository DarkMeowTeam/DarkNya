package net.ccbluex.liquidbounce.ui.client


import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.client.mainmenu.MainMenuButton
import net.ccbluex.liquidbounce.ui.client.mainmenu.MainMenuSmallButton
import net.ccbluex.liquidbounce.ui.cnfont.FontLoaders
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import java.awt.Color

class GuiMainMenu : GuiScreen() {

    private val backgroundResource = ResourceLocation("wawa/mainmenu.png")

    private val icon = ResourceLocation("darknya/big.png")

    // Background
    private var currentX = 0f
    private var currentY = 0f

    override fun initGui() {
        val defaultHeight = height / 2f - 20 / 2f - 57

        buttonList.add(MainMenuButton(100, (width/2) + 3,
            (height/2) + 3, 107, 34, "hp", "AltManager"))
/*        buttonList.add(GuiButton(102, 20,
            (defaultHeight + 72).toInt(), 100, 20, "Background"))*/

        buttonList.add(
            MainMenuButton(1, (width/2) - (107+3),
            (height/2) + 3, 107, 34, "sp", "SinglePlayer")
        )
        buttonList.add(MainMenuButton(2, (width/2) - (220/2),
            (height/2) - (50+3), 220, 50, "mp","MulitPlayer"))

        buttonList.add(MainMenuSmallButton(0, 3,
            height - (20+3), 20, 20, "settings"))
        buttonList.add(MainMenuSmallButton(4, 3 + 20 + 3,
            height - (20+3), 20, 20, "exit")
        )
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {

        val res = ScaledResolution(mc)
        val width = res.scaledWidth
        val height = res.scaledHeight

        // Background
        val xDiff: Float = ((mouseX - height / 2).toFloat() - this.currentX) / res.scaleFactor.toFloat()
        val yDiff: Float = ((mouseY - width / 2).toFloat() - this.currentY) / res.scaleFactor.toFloat()
        this.currentX += xDiff * 0.2f
        this.currentY += yDiff * 0.2f

        GlStateManager.translate(this.currentX / 30.0f, this.currentY / 15.0f, 0.0f)
        RenderUtils.drawImage(backgroundResource, -30, -30, width + 60, height + 60)
        GlStateManager.translate(-this.currentX / 30.0f, -this.currentY / 15.0f, 0.0f)

        RenderUtils.drawRoundRect(1.5F, height - 24F,
            50F + FontLoaders.F24.getStringWidth("PridePlus") + FontLoaders.F14.getStringWidth("NextGen1.0"),
            height - 1.5F, 5.6F, Color(0,0,0,100).rgb)
        FontLoaders.F24.drawString("PridePlus", 47F, height.toFloat() - 14F - (Fonts.bold45.height/2), Color.WHITE.rgb)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            1 -> mc.displayGuiScreen(GuiWorldSelection(this))
            2 -> mc.displayGuiScreen(GuiMultiplayer(this))
            4 -> mc.shutdown()
            100 -> mc.displayGuiScreen((GuiAltManager(this)))
            102 -> mc.displayGuiScreen((GuiBackground(this)))
            103 -> mc.displayGuiScreen((GuiModsMenu(this)))
        }
    }
}