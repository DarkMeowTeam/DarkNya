package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import com.google.common.collect.Iterables
import com.google.common.collect.Lists
import com.mojang.realmsclient.gui.ChatFormatting
import me.utils.render.ShadowUtils
import net.ccbluex.liquidbounce.features.module.modules.client.ColorManage
import net.ccbluex.liquidbounce.value.*
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.cnfont.FontLoaders
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.scoreboard.ScoreObjective
import net.minecraft.scoreboard.ScorePlayerTeam
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * CustomHUD scoreboard
 *
 * Allows to move and customize minecraft scoreboard
 */
@ElementInfo(name = "Scoreboard")
class ScoreboardElement(
    x: Double = 5.0,
    y: Double = 0.0,
    scale: Float = 1F,
    side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.MIDDLE)
) : Element(x, y, scale, side) {


    private val shadowShaderValue = BoolValue("Shadow", false)
    private val shadowStrength = FloatValue("Shadow-Strength", 0F, 0F, 30F)

    private val radius = IntegerValue("Radius",15,0,10)
    //private val blur = BoolValue("Blur", true)
    //private val blurStrength = FloatValue("Blur-Strength", 2F, 0F, 50F)
    private val rectValue = BoolValue("Rect", false)

    private val noPointValue = BoolValue("NoPoints", false)
    private val fontValue = FontValue("Font", Fonts.minecraftFont)

    /**
     * Draw element
     */
    override fun drawElement(): Border? {
        val fontRenderer = fontValue.get()
        val textColor = textColor().rgb
        val backColor = backgroundColor().rgb

        val worldScoreboard = mc.world!!.scoreboard
        var currObjective: ScoreObjective? = null
        val playerTeam = worldScoreboard.getPlayersTeam(mc.player!!.name)


        if (playerTeam != null) {
            val colorIndex = playerTeam.color.colorIndex

            if (colorIndex >= 0)
                currObjective = worldScoreboard.getObjectiveInDisplaySlot(3 + colorIndex)
        }

        val objective = currObjective ?: worldScoreboard.getObjectiveInDisplaySlot(1) ?: return null

        val scoreboard = objective.scoreboard
        var scoreCollection = scoreboard.getSortedScores(objective)
        val scores = Lists.newArrayList(Iterables.filter(scoreCollection) { input ->
            input?.playerName != null && !input.playerName.startsWith("#")
        })

        scoreCollection = if (scores.size > 15) {
            Lists.newArrayList(Iterables.skip(scores, scoreCollection.size - 15))
        } else {
            scores
        }

        var maxWidth = fontRenderer.getStringWidth(objective.displayName)

        for (score in scoreCollection) {
            val scorePlayerTeam = scoreboard.getPlayersTeam(score.playerName)
            val width = "${
                ScorePlayerTeam.formatPlayerName(
                    scorePlayerTeam,
                    score.playerName
                )
            }: ${ChatFormatting.RED}${score.scorePoints}"
            maxWidth = maxWidth.coerceAtLeast(fontRenderer.getStringWidth(width))
        }

        val maxHeight = scoreCollection.size * fontRenderer.FONT_HEIGHT
        val l1 = -maxWidth - 3 -  0
        if (shadowShaderValue.get()) {
            GL11.glTranslated(-renderX, -renderY, 0.0)
            GL11.glScalef(1F, 1F, 1F)
            GL11.glPushMatrix()
            ShadowUtils.shadow(shadowStrength.get(), {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                GL11.glScalef(scale, scale, scale)
                RenderUtils.drawRoundedRect(l1 - 7f, -5f, 9f, (maxHeight + fontRenderer.FONT_HEIGHT + 5).toFloat(),
                    radius.get(),backColor)
                GL11.glPopMatrix()
            }, {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                GL11.glScalef(scale, scale, scale)
                GlStateManager.enableBlend()
                GlStateManager.disableTexture2D()
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
                RenderUtils.drawRoundedRect(l1 - 7f, -5f, 9f, (maxHeight + fontRenderer.FONT_HEIGHT + 5).toFloat(),
                    radius.get(),backColor)
                GlStateManager.enableTexture2D()
                GlStateManager.disableBlend()
                GL11.glPopMatrix()
            })
            GL11.glPopMatrix()
            GL11.glScalef(scale, scale, scale)
            GL11.glTranslated(renderX, renderY, 0.0)
        }

        RenderUtils.drawRoundedRect(l1 - 7f, -5f, 9f, (maxHeight + fontRenderer.FONT_HEIGHT + 5).toFloat(),
            radius.get(),backColor)

        //Blur
/*        if (blur.get()) {
            GL11.glTranslated(-renderX, -renderY, 0.0)
            BlurBuffer.CustomBlurRoundArea(
                renderX.toFloat() + l1 - 7f,
                renderY.toFloat() - 5f,
                -l1 + 16f,
                maxHeight + fontRenderer.fontHeight + 10f,
                radius.get().toFloat(),
                blurStrength.get()
            )
            GL11.glTranslated(renderX, renderY, 0.0)
        }*/
        scoreCollection.forEachIndexed { index, score ->
            val team = scoreboard.getPlayersTeam(score.playerName)

            var name = ScorePlayerTeam.formatPlayerName(team, score.playerName)
            val scorePoints = "${ChatFormatting.RED}${score.scorePoints}"

            val width = 5 - 0
            val height = maxHeight - index * fontRenderer.FONT_HEIGHT

            GlStateManager.resetColor()

            var listColor = textColor
            FontLoaders.F16.drawStringWithShadow(name, l1.toDouble(), height.toDouble(), listColor)
            if (!noPointValue.get()) {
                FontLoaders.F16.drawStringWithShadow(
                    scorePoints,
                    (width - fontRenderer.getStringWidth(scorePoints)).toFloat(),
                    height.toFloat(),
                    textColor
                )
            }

            if (index == scoreCollection.size - 1) {
                val displayName = objective.displayName

                GlStateManager.resetColor()
                FontLoaders.F16.drawStringWithShadow(
                    displayName,
                    (l1 + maxWidth / 2 - fontRenderer.getStringWidth(displayName) / 2).toFloat(),
                    (height - fontRenderer.FONT_HEIGHT).toFloat(),
                    textColor)
            }


            if (rectValue.get()) {
                RenderUtils.drawGradientSideways((-maxWidth-10f).toDouble(), -5f.toDouble(), 9f.toDouble(), -4.0,
                    ColorManage.getColorByTime(),
                    ColorManage.getColorByTime()
                )
            }
        }

        return Border(-maxWidth.toFloat() - 10f - 0, -5F, 9F, maxHeight.toFloat() + fontRenderer.FONT_HEIGHT + 5)
    }

    private fun backgroundColor() = Color(0,0,0,160)
    private fun textColor() = Color(240,240,240)
}