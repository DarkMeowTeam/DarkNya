package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.ColorManage
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.features.module.modules.player.HighDamageDetector
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.entity.DarkNyaPotionUtils
import net.ccbluex.liquidbounce.utils.extensions.getPing
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.quickDrawRect
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.FontValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.potion.PotionUtils
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.roundToInt

@ModuleInfo(name = "NameTags", description = "Changes the scale of the nametags so you can always read them.", category = ModuleCategory.RENDER)
class NameTags : Module() {
    private val selfValue = BoolValue("Self", false)

    private val healthValue = BoolValue("Health", true)
    private val pingValue = BoolValue("Ping", true)
    private val distanceValue = BoolValue("Distance", false)
    private val clearNamesValue = BoolValue("ClearNames", false)

    private val fontValue = FontValue("Font", Fonts.minecraftFont)

    private val rectValue = ListValue("Rect", arrayOf("None","Up","Down"),"Up")

    private val scaleValue = FloatValue("Scale", 1F, 1F, 4F)

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        for (entity in mc.world!!.loadedEntityList) {
            if (!EntityUtils.isSelected(entity, false))
                continue

            if (entity !is EntityLivingBase) continue

            renderNameTag(entity,
                if (clearNamesValue.get())
                    ColorUtils.stripColor(entity.displayName?.unformattedText) ?: continue
                else
                    (entity.displayName ?: continue).unformattedText
            )
        }
        if (selfValue.get()) renderNameTag(mc.player,mc.player.name)
    }

    private fun renderNameTag(entity: EntityLivingBase, tag: String) {
        val thePlayer = mc.player ?: return

        val fontRenderer = fontValue.get()

        // Modify tag
        val bot = AntiBot.isBot(entity)
        val nameColor = if (bot) "§3" else if (entity.isInvisible) "§6" else if (entity.isSneaking) "§4" else "§7"
        val ping = if (entity is EntityPlayer) entity.getPing() else 0

        val distanceText = if (distanceValue.get()) "§7${thePlayer.getDistance(entity).roundToInt()}m " else ""
        val pingText = if (pingValue.get() && entity is EntityPlayer) (if (ping > 200) "§c" else if (ping > 100) "§e" else "§a") + ping + "ms §7" else ""
        val healthText = if (healthValue.get()) "§7§c " + entity.health.toInt() + " " else ""
        val botText = if (bot) " §c§lBot" else ""

        val text = "$distanceText$pingText$nameColor$tag$healthText$botText"

        // Push
        glPushMatrix()

        // Translate to player position
        val timer = mc.timer
        val renderManager = mc.renderManager


        glTranslated( // Translate to player position with render pos and interpolate it
            entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks - renderManager.renderPosX,
            entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks - renderManager.renderPosY + entity.eyeHeight.toDouble() + 0.55,
            entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks - renderManager.renderPosZ
        )

        glRotatef(-mc.renderManager.playerViewY, 0F, 1F, 0F)
        glRotatef(mc.renderManager.playerViewX, 1F, 0F, 0F)


        // Scale
        var distance = thePlayer.getDistance(entity) * 0.25f

        if (distance < 1F)
            distance = 1F

        val scale = distance / 100f * scaleValue.get()

        glScalef(-scale, -scale, scale)

        AWTFontRenderer.assumeNonVolatile = true

        // Draw NameTag
        val width = fontRenderer.getStringWidth(text) * 0.5f

        // Disable lightning and depth test
        RenderUtils.disableGlCap(GL_LIGHTING, GL_DEPTH_TEST)

        // Enable blend
        RenderUtils.enableGlCap(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)


        var health = entity.health / entity.maxHealth
        if (health > 1F) health = 1F // 生命附加之类的BUFF血量大于100%

        val healthDrawX = -width - 2F
        val healthDrawXEnd = healthDrawX + (health * ( width * 2 + 5F))

        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)

        quickDrawRect(-width - 2F, -2F, width + 3F, fontRenderer.FONT_HEIGHT + 3F, Integer.MIN_VALUE)

        // 默认NameTags颜色
        var color = ColorManage.getColorByTime()
        try {
            if (entity is EntityPlayer) {
                // 生命恢复黄色NameTags颜色
                if (DarkNyaPotionUtils.getRegenerationEffectDuration(entity) != 0) color = Color(240, 240, 40).rgb
                // 力量效果红色NameTags颜色
                if (DarkNyaPotionUtils.getStrengthEffectDuration(entity) != 0) color = Color(240, 40, 40).rgb
                // 高风险用户橙色NameTags颜色
                if (HighDamageDetector.displayInNameTagsValue.get() && HighDamageDetector.warnPlayers.contains(entity)) color = Color(200, 100, 0).rgb
            }
        } catch (_:Exception) { }
        when (rectValue.get().toLowerCase()) {
            "up" -> quickDrawRect(
                healthDrawX,
                -3F,
                healthDrawXEnd,
                -4F,
                color
            )
            "down" -> quickDrawRect(
                healthDrawX,
                fontRenderer.FONT_HEIGHT + 2F,
                healthDrawXEnd,
                fontRenderer.FONT_HEIGHT + 3F,
                color
            )
        }

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)


        fontRenderer.drawString(text, 1F + -width, if (fontRenderer == Fonts.minecraftFont) 1F else 1.5F,
            0xFFFFFF, true)

        AWTFontRenderer.assumeNonVolatile = false

        RenderUtils.resetCaps()

        // Pop
        glPopMatrix()
    }
}