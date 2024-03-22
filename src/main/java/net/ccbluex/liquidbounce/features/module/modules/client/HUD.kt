package net.ccbluex.liquidbounce.features.module.modules.client

import com.mojang.realmsclient.gui.ChatFormatting
import me.utils.render.VisualUtils
import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.*
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.cnfont.FontDrawer
import net.ccbluex.liquidbounce.ui.cnfont.FontLoaders
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.render.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.init.MobEffects
import net.minecraft.util.ResourceLocation
import op.wawa.utils.animation.AnimationUtil
import java.awt.Color
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

@ModuleInfo(name = "HUD", description = "Toggles visibility of the HUD.", category = ModuleCategory.CLIENT, array = false)
class HUD : Module() {
    companion object {
        @JvmField
        val hotbarModeSection = ListValue("HotbarMode", arrayOf("PridePlus", "Pride", "LiquidBounce", "Off"), "Pride")
    }

    val inventoryParticle = BoolValue("InventoryParticle", false)
    private val blurValue = BoolValue("Blur", false)
    val fontChatValue = BoolValue("FontChat", false)

    val shadowValue = ListValue("TextShadowMode", arrayOf("Good", "Long", "D1ck"), "Good")


    val radius = FloatValue("Radius",15F,0F,100F)
    @JvmField
    val domainValue = TextValue("Scoreboard-Domain", DarkNya.CLIENT_NAME)
    val rainbowStart = FloatValue("RainbowStart", 0.41f, 0f, 1f)
    val rainbowStop = FloatValue("RainbowStop", 0.58f, 0f, 1f)

    private var hotBarX = 0F



    @EventTarget
    fun onRender2D(event: Render2DEvent?) {
        if (mc.currentScreen is GuiHudDesigner)
            return

        DarkNya.hud.render(false)
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        DarkNya.hud.update()
    }

    @EventTarget
    fun onKey(event: KeyEvent) {
        DarkNya.hud.handleKey('a', event.key)
    }

    @EventTarget(ignoreCondition = true)
    fun onScreen(event: ScreenEvent) {
        if (mc.world == null || mc.player == null) return
        if (state && blurValue.get() && !mc.entityRenderer.isShaderActive() && event.guiScreen != null &&
                !(event.guiScreen is GuiChat || event.guiScreen is GuiHudDesigner)) mc.entityRenderer.loadShader(
            ResourceLocation("darknya/blur.json")
        ) else if (mc.entityRenderer.shaderGroup != null &&
                mc.entityRenderer.shaderGroup.shaderGroupName.contains("darknya/blur.json")) mc.entityRenderer.stopUseShader()
    }
    fun getAnimPos(pos: Float): Float {
        hotBarX = if (state && hotbarModeSection.get() == "Pride") AnimationUtil.animate(
            pos,
            hotBarX,
            0.02F * RenderUtils.deltaTime.toFloat()
        )
        else pos

        return hotBarX
    }

    init {
        state = true
    }
}