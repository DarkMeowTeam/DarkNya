package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.cnfont.FontLoaders
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import net.ccbluex.liquidbounce.utils.misc.TipsUtils
import net.ccbluex.liquidbounce.utils.render.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.client.GuiModList
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.io.FileOutputStream
import java.nio.file.Files

class GuiMainMenu : GuiScreen() {
    private var slideX : Float = 0F
    var fade : Float = 0F

    private var sliderX : Float = 0F
    private var sliderDarkX : Float = 0F

    private var lastAnimTick: Long = 0L
    private var alrUpdate = false

    private var lastXPos = 0F

    private var extendedModMode = false
    private var extendedBackgroundMode = false

    companion object {
        var useParallax = true
    }
    private var currentX = 0f
    private var currentY = 0f

    override fun initGui() {
        slideX = 0F
        fade = 0F
        sliderX = 0F
        sliderDarkX = 0F
        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!alrUpdate) {
            lastAnimTick = System.currentTimeMillis()
            alrUpdate = true
        }
        val creditInfo = ""
        drawBackground(0)

        val h = height
        val w = width
        val res = ScaledResolution(MinecraftInstance.mc)
        val xDiff: Float = ((mouseX - h / 2).toFloat() - this.currentX) / res.scaleFactor.toFloat()
        val yDiff: Float = ((mouseY - w / 2).toFloat() - this.currentY) / res.scaleFactor.toFloat()
        this.currentX += xDiff * 0.3f
        this.currentY += yDiff * 0.3f
        GlStateManager.translate(this.currentX / 30.0f, this.currentY / 15.0f, 0.0f)
        RenderUtils.drawImage(ResourceLocation("darknya/wallpaper.png"), -30, -30, res.scaledWidth + 60, res.scaledHeight + 60)
        GlStateManager.translate(-this.currentX / 30.0f, -this.currentY / 15.0f, 0.0f)

        GL11.glPushMatrix()
        renderSwitchButton()
        renderDarkModeButton()
        FontLoaders.F16.drawStringWithShadow(" ${DarkNya.CLIENT_NAME} ${DarkNya.CLIENT_VERSION} ${DarkNya.CLIENT_SUFFIX} | By ${DarkNya.CLIENT_CREATOR}",
            2.0, (h - 12F).toDouble(), -1)
        FontLoaders.F16.drawStringWithShadow(creditInfo,
            (w - 3F - FontLoaders.F16.getStringWidth(creditInfo)).toDouble(),
            (h - 12F).toDouble(), -1)
        if (useParallax) moveMouseEffect(mouseX, mouseY, 10F)
        GlStateManager.disableAlpha()
        //RenderUtils.drawImage2(bigLogo, (w / 2F - 50F).toInt(), (h / 2F - 90F).toInt(), 100, 100)
        GlStateManager.enableAlpha()
        renderBar(mouseX, mouseY, partialTicks)
        GL11.glPopMatrix()
        super.drawScreen(mouseX, mouseY, partialTicks)

        if (!DarkNya.mainMenuPrep) {
            val animProgress = ((System.currentTimeMillis() - lastAnimTick).toFloat() / 1500F).coerceIn(0F, 1F)
            RenderUtils.drawRect(0F, 0F, w.toFloat(), h.toFloat(), Color(0F, 0F, 0F, 1F - animProgress))
            if (animProgress >= 1F)
                DarkNya.mainMenuPrep = true
        }
        if (DarkNya.darkMode) RenderUtils.drawRect(0F, 0F, w.toFloat(), h.toFloat(), Color(0F, 0F, 0F, 0.3F))


    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!DarkNya.mainMenuPrep || mouseButton != 0) return

        if (isMouseHover(2F, height - 26F, 28F, height - 16F, mouseX, mouseY))
            useParallax = !useParallax

        if (isMouseHover(2F, height - 38F, 28F, height - 28F, mouseX, mouseY))
            DarkNya.darkMode = !DarkNya.darkMode

        val staticX = width / 2F - 120F
        val staticY = height / 2F + 20F
        var index: Int = 0
        for (icon in if (extendedModMode) ExtendedImageButton.values() else ImageButton.values()) {
            if (isMouseHover(staticX + 40F * index, staticY, staticX + 40F * (index + 1), staticY + 20F, mouseX, mouseY))
                when (index) {
                    0 -> if (extendedBackgroundMode) extendedBackgroundMode = false else if (extendedModMode) extendedModMode = false else mc.displayGuiScreen(GuiWorldSelection(this))
                    1 -> if (extendedBackgroundMode) GuiBackground.enabled = !GuiBackground.enabled else if (extendedModMode) mc.displayGuiScreen(GuiModList(this)) else mc.displayGuiScreen(GuiMultiplayer(this))
                    2 -> if (extendedBackgroundMode) GuiBackground.particles = !GuiBackground.particles else if (extendedModMode) mc.displayGuiScreen(GuiScripts(this)) else mc.displayGuiScreen(GuiAltManager(this))
                    3 -> if (extendedBackgroundMode) {
                        val file = MiscUtils.openFileChooser() ?: return
                        if (file.isDirectory) return

                        try {
                            Files.copy(file.toPath(), FileOutputStream(DarkNya.fileManager.backgroundFile))
                            DarkNya.fileManager.loadBackground()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            MiscUtils.showErrorPopup("Error", "Exception class: " + e.javaClass.name + "\nMessage: " + e.message)
                            DarkNya.background = null
                            DarkNya.fileManager.backgroundFile.delete()
                        }
                    } else if (extendedModMode) {


                    } else mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
                    4 -> if (extendedBackgroundMode) {
                        DarkNya.background = null
                        DarkNya.fileManager.backgroundFile.delete()
                    } else if (extendedModMode) extendedBackgroundMode = true else extendedModMode = true
                    5 -> mc.shutdown()
                }

            index++
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    fun moveMouseEffect(mouseX: Int, mouseY: Int, strength: Float) {
        val mX = mouseX - width / 2
        val mY = mouseY - height / 2
        val xDelta = mX.toFloat() / (width / 2).toFloat()
        val yDelta = mY.toFloat() / (height / 2).toFloat()

        GL11.glTranslatef(xDelta * strength, yDelta * strength, 0F)
    }

    fun renderSwitchButton() {
        sliderX = (sliderX + (if (useParallax) 2F else -2F)).coerceIn(0F, 12F)
        FontLoaders.F16.drawString("Parallax", 28.0F, (height - 25F).toDouble().toFloat(), -1,true)
        RenderUtils.drawRoundedRect(4F, height - 24F, 22F, height - 18F, 3, if (useParallax) Color(0, 111, 255, 255).rgb else (if (DarkNya.darkMode) Color(70, 70, 70, 255) else Color(140, 140, 140, 255)).rgb)
        RenderUtils.drawRoundedRect(2F + sliderX, height - 26F, 12F + sliderX, height - 16F, 5, Color.white.rgb)
    }

    fun renderDarkModeButton() {
        sliderDarkX = (sliderDarkX + (if (DarkNya.darkMode) 2F else -2F)).coerceIn(0F, 12F)
        GlStateManager.disableAlpha()

        FontLoaders.F16.drawString("Dark Mode", 28F, height - 38F, -1,true)

        GlStateManager.enableAlpha()
        RenderUtils.drawRoundedRect(4F, height - 36F, 22F, height - 30F, 3, (if (DarkNya.darkMode) Color(70, 70, 70, 255) else Color(140, 140, 140, 255)).rgb)
        RenderUtils.drawRoundedRect(2F + sliderDarkX, height - 38F, 12F + sliderDarkX, height - 28F, 5, Color.white.rgb)
    }

    fun renderBar(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val staticX = width / 2F - 120F
        val staticY = height / 2F + 20F

        RenderUtils.drawRoundedRect(staticX, staticY, staticX + 240F, staticY + 20F, 10, (if (DarkNya.darkMode) Color(0, 0, 0, 100) else Color(255, 255, 255, 100)).rgb)

        var index: Int = 0
        var shouldAnimate = false
        var displayString: String? = null
        var moveX = 0F
        if (extendedModMode) {
            if (extendedBackgroundMode)
                for (icon in ExtendedBackgroundButton.values()) {
                    if (isMouseHover(staticX + 40F * index, staticY, staticX + 40F * (index + 1), staticY + 20F, mouseX, mouseY)) {
                        shouldAnimate = true
                        displayString = if (icon == ExtendedBackgroundButton.Enabled)
                            "Custom background: ${if (GuiBackground.enabled) "§aON" else "§cOFF"}"
                        else if (icon == ExtendedBackgroundButton.Particles)
                            "${icon.buttonName}: ${if (GuiBackground.particles) "§aON" else "§cOFF"}"
                        else
                            icon.buttonName
                        moveX = staticX + 40F * index
                    }
                    index++
                }
            else
                for (icon in ExtendedImageButton.values()) {
                    if (isMouseHover(staticX + 40F * index, staticY, staticX + 40F * (index + 1), staticY + 20F, mouseX, mouseY)) {
                        shouldAnimate = true
                        displayString = icon.buttonName
                        moveX = staticX + 40F * index
                    }
                    index++
                }
        } else
            for (icon in ImageButton.values()) {
                if (isMouseHover(staticX + 40F * index, staticY, staticX + 40F * (index + 1), staticY + 20F, mouseX, mouseY)) {
                    shouldAnimate = true
                    displayString = icon.buttonName
                    moveX = staticX + 40F * index
                }
                index++
            }

        if (displayString != null)
            FontLoaders.F16.drawCenteredString(
                displayString,
                (width / 2F).toDouble(), (staticY + 30F).toDouble(), -1)
        else
            FontLoaders.F16.drawCenteredString(TipsUtils.getTips(), (width / 2F).toDouble(),
                (staticY + 30F).toDouble(), -1)

        if (shouldAnimate) {
            if (fade == 0F)
                slideX = moveX
            else
                slideX = AnimationUtils.animate(moveX, slideX, 0.5F * (1F - partialTicks))

            lastXPos = moveX

            fade += 10F
            if (fade >= 100F) fade = 100F
        } else {
            fade -= 10F
            if (fade <= 0F) fade = 0F

            slideX = AnimationUtils.animate(lastXPos, slideX, 0.5F * (1F - partialTicks))
        }

        if (fade != 0F)
            RenderUtils.drawRoundedRect(slideX, staticY, slideX + 40F, staticY + 20F, 10, (if (DarkNya.darkMode) Color(0F, 0F, 0F, fade / 100F * 0.6F) else Color(1F, 1F, 1F, fade / 100F * 0.6F)).rgb)

        index = 0
        GlStateManager.disableAlpha()
        if (extendedModMode) {
            if (extendedBackgroundMode)
                for (i in ExtendedBackgroundButton.values()) {
                    if (DarkNya.darkMode)
                        RenderUtils.drawImage(i.texture, (staticX + 40F * index + 11F).toInt(), (staticY + 1F).toInt(), 18, 18)
                    else
                        RenderUtils.drawImage(i.texture, staticX + 40F * index + 11F, staticY + 1F, 18, 18, 0F, 0F, 0F, 1F)
                    index++
                }
            else
                for (i in ExtendedImageButton.values()) {
                    if (DarkNya.darkMode)
                        RenderUtils.drawImage(i.texture, (staticX + 40F * index + 11F).toInt(), (staticY + 1F).toInt(), 18, 18)
                    else
                        RenderUtils.drawImage(i.texture, staticX + 40F * index + 11F, staticY + 1F, 18, 18, 0F, 0F, 0F, 1F)
                    index++
                }
        } else
            for (i in ImageButton.values()) {
                if (DarkNya.darkMode)
                    RenderUtils.drawImage(i.texture, (staticX + 40F * index + 11F).toInt(), (staticY + 1F).toInt(), 18, 18)
                else
                    RenderUtils.drawImage(i.texture, staticX + 40F * index + 11F, staticY + 1F, 18, 18, 0F, 0F, 0F, 1F)
                index++
            }
        GlStateManager.enableAlpha()
    }

    fun isMouseHover(x: Float, y: Float, x2: Float, y2: Float, mouseX: Int, mouseY: Int): Boolean = mouseX >= x && mouseX < x2 && mouseY >= y && mouseY < y2

    enum class ImageButton(val buttonName: String, val texture: ResourceLocation) {
        Single("Singleplayer", ResourceLocation("darknya/menu/singleplayer.png")),
        Multi("Multiplayer", ResourceLocation("darknya/menu/multiplayer.png")),
        Alts("Alts", ResourceLocation("darknya/menu/alt.png")),
        Settings("Settings", ResourceLocation("darknya/menu/settings.png")),
        Mods("Mods/Customize", ResourceLocation("darknya/menu/mods.png")),
        Exit("Exit", ResourceLocation("darknya/menu/exit.png"))
    }

    enum class ExtendedImageButton(val buttonName: String, val texture: ResourceLocation) {
        Back("Back", ResourceLocation("darknya/menu/back.png")),
        Mods("Mods", ResourceLocation("darknya/menu/mods.png")),
        Scripts("Scripts", ResourceLocation("darknya/menu/docs.png")),
        DiscordRPC("Null", ResourceLocation("darknya/menu/discord.png")),
        Background("Background", ResourceLocation("darknya/menu/wallpaper.png")),
        Exit("Exit", ResourceLocation("darknya/menu/exit.png"))
    }

    enum class ExtendedBackgroundButton(val buttonName: String, val texture: ResourceLocation) {
        Back("Back", ResourceLocation("darknya/menu/back.png")),
        Enabled("Enabled", ResourceLocation("darknya/menu/wallpaper.png")),
        Particles("Particles", ResourceLocation("darknya/menu/brush.png")),
        Change("Change wallpaper", ResourceLocation("darknya/menu/import.png")),
        Reset("Reset wallpaper", ResourceLocation("darknya/menu/reload.png")),
        Exit("Exit", ResourceLocation("darknya/menu/exit.png"))
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {}
}