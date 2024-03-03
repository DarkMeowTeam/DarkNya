package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUI
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color

class GuiWelcome : GuiScreen() {
    override fun initGui() {
        buttonList.add(GuiButton(1, this.width / 2 - 100, height - 40, "Ok"))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        val font = Fonts.font35

        font.drawCenteredString("Thank you for downloading and installing our client!", width / 2F, height / 8F + 70, 0xffffff, true)
        font.drawCenteredString("Here is some information you might find useful if you are using ${DarkNya.CLIENT_NAME} for the first time.", width / 2F, height / 8F + 70 + font.FONT_HEIGHT, 0xffffff, true)

        font.drawCenteredString("§lClickGUI:", width / 2F, height / 8F + 80 + font.FONT_HEIGHT * 3, 0xffffff, true)
        font.drawCenteredString("Press ${Keyboard.getKeyName(DarkNya.moduleManager[ClickGUI::class.java].keyBind)} to open up the ClickGUI", width / 2F, height / 8 + 80F + font.FONT_HEIGHT * 4, 0xffffff, true)
        // Unnecessary non-null assertion (!!) on a non-null receiver of type Module -> don't trust
        font.drawCenteredString("Right-click modules with a '+' next to them to edit their settings.", width / 2F, height / 8F + 80 + font.FONT_HEIGHT * 5, 0xffffff, true)
        font.drawCenteredString("Hover a module to see it's description.", width / 2F, height / 8F + 80 + font.FONT_HEIGHT * 6, 0xffffff, true)

        font.drawCenteredString("§lImportant Commands:", width / 2F, height / 8F + 80 + font.FONT_HEIGHT * 8, 0xffffff, true)
        font.drawCenteredString(".bind <module> <key> / .bind <module> none", width / 2F, height / 8F + 80 + font.FONT_HEIGHT * 9, 0xffffff, true)
        font.drawCenteredString(".autosettings load <name> / .autosettings list", width / 2F, height / 8F + 80 + font.FONT_HEIGHT * 10, 0xffffff, true)

        font.drawCenteredString("§cUsing our client may be considered cheating for some servers.", width / 2F, height / 8F + 80 + font.FONT_HEIGHT * 12, 0xff0000, true)
        font.drawCenteredString("§cYou are responsible for any consequences caused.", width / 2F, height / 8F + 80 + font.FONT_HEIGHT * 13, 0xff0000, true)

        font.drawCenteredString("§cIf you not accept this.You can't using ${DarkNya.CLIENT_NAME}.", width / 2F, height / 8F + 80 + font.FONT_HEIGHT * 15, 0xff0000, true)

        super.drawScreen(mouseX, mouseY, partialTicks)

        // Title
        GL11.glScalef(2F, 2F, 2F)
        Fonts.font40.drawCenteredString("Welcome!", width / 2 / 2F, height / 8F / 2 + 20, Color(0, 140, 255).rgb, true)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (Keyboard.KEY_ESCAPE == keyCode)
            return

        super.keyTyped(typedChar, keyCode)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            1 -> mc.displayGuiScreen(GuiMainMenu())
        }
    }
}