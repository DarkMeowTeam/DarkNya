package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.features.module.modules.client.ColorManage
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.FontRenderer
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.max


@ElementInfo(name = "Notifications")
class Notifications(x: Double = 0.0, y: Double = 0.0, scale: Float = 1F,side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)) : Element(x, y, scale, side) {


    private val backGroundAlphaValue = IntegerValue("BackGroundAlpha", 170, 0, 255)
    private val titleShadow = BoolValue("TitleShadow", false)
    private val motionBlur = BoolValue("Motionblur", false)
    private val contentShadow = BoolValue("ContentShadow", true)
    private val whiteText = BoolValue("WhiteTextColor", true)
    private val modeColored = BoolValue("CustomModeColored", true)

    /**
     * Example notification for CustomHUD designer
     */
    private val exampleNotification = Notification("Notification", "This is an example notification.", NotifyType.INFO)

    /**
     * Draw element
     */
    override fun drawElement(): Border? {
        // bypass java.util.ConcurrentModificationException
        DarkNya.hud.notifications.map { it }.forEachIndexed { index, notify ->
            GL11.glPushMatrix()

            if (notify.drawNotification(index, Fonts.font35, backGroundAlphaValue.get(), 0F, this.renderX.toFloat(), this.renderY.toFloat(), scale,contentShadow.get(),titleShadow.get(),motionBlur.get(),whiteText.get(),modeColored.get())) {
                DarkNya.hud.notifications.remove(notify)
            }

            GL11.glPopMatrix()
        }

        if (mc.currentScreen is GuiHudDesigner) {
            if (!DarkNya.hud.notifications.contains(exampleNotification)) {
                DarkNya.hud.addNotification(exampleNotification)
            }

            exampleNotification.fadeState = FadeState.STAY
            exampleNotification.displayTime = System.currentTimeMillis()

            return Border(-exampleNotification.width.toFloat(), -exampleNotification.height.toFloat(), 0F, 0F)
        }

        return null
    }
}


class Notification(
    val title: String,
    val content: String,
    val type: NotifyType,
    val time: Int = 1500,
    private val animeTime: Int = 500
) {
    var width = 100
    val height = 30

    var x = 0F
    var textLengthtitle = 0
    var textLengthcontent = 0
    var textLength = 0f
    init {
        textLengthtitle = Fonts.font35.getStringWidth(title)
        textLengthcontent = Fonts.font35.getStringWidth(content)
        textLength = textLengthcontent.toFloat() + textLengthtitle.toFloat()
    }

    var fadeState = FadeState.IN
    private var nowY = -height
    var displayTime = System.currentTimeMillis()
    private var animeXTime = System.currentTimeMillis()
    private var animeYTime = System.currentTimeMillis()

    /**
     * Draw notification
     */
    fun drawNotification(
        index: Int, font: FontRenderer, alpha: Int, blurRadius: Float, x: Float, y: Float, scale: Float,
        contentShadow: Boolean,
        titleShadow: Boolean,
        motionBlur: Boolean,
        whiteText: Boolean,
        modeColored: Boolean

    ): Boolean {
        this.width = 100.coerceAtLeast(
            font.getStringWidth(content)
                .coerceAtLeast(font.getStringWidth(title)) + 15
        )
        val realY = -(index + 1) * (height + 2)
        val nowTime = System.currentTimeMillis()
        var transY = nowY.toDouble()

        var textColor = Color(255, 255, 255).rgb

        if (whiteText) {
            textColor = Color(255, 255, 255).rgb
        } else {
            textColor = Color(10, 10, 10).rgb
        }

        //Y-Axis Animation
        if (nowY != realY) {
            var pct = (nowTime - animeYTime) / animeTime.toDouble()
            if (pct > 1) {
                nowY = realY
                pct = 1.0
            } else {
                pct = EaseUtils.easeOutQuart(pct)
            }
            GL11.glTranslated(0.0, (realY - nowY) * pct, 0.0)
        } else {
            animeYTime = nowTime

        }
        GL11.glTranslated(1.0, nowY.toDouble(), 0.0)

        // X-Axis Animation
        var pct = (nowTime - animeXTime) / animeTime.toDouble()
        when (fadeState) {
            FadeState.IN -> {
                if (pct > 1) {
                    fadeState = FadeState.STAY
                    animeXTime = nowTime
                    pct = 1.0
                }
                pct = EaseUtils.easeOutQuart(pct)
                transY += (realY - nowY) * pct
            }

            FadeState.STAY -> {
                pct = 1.0
                if ((nowTime - animeXTime) > time) {
                    fadeState = FadeState.OUT
                    animeXTime = nowTime
                }
            }

            FadeState.OUT -> {
                if (pct > 1) {
                    fadeState = FadeState.END
                    animeXTime = nowTime
                    pct = 2.0
                }
                pct = 1 - EaseUtils.easeInQuart(pct)
            }

            FadeState.END -> {
                return true
            }
        }

        GL11.glTranslated(width - (width * pct), 0.0, 0.0)
        GL11.glTranslatef(-width.toFloat(), 0F, 0F)


        val colors = Color(type.renderColor.red, type.renderColor.green, type.renderColor.blue, alpha / 3)
        RenderUtils.drawRect(2, 0, 4, 27 - 5, colors.rgb)
        RenderUtils.drawRect(3F, 0F, width.toFloat() + 5f, 27f - 5f, Color(0, 0, 0, 150))
        // RenderUtils.drawGradientSidewaysH(3.0, 0.0, 20.0, 27f - 5.0, colors.rgb, Color(0, 0, 0, 0).rgb)
        RenderUtils.drawRect(
            2f,
            27f - 6f,
            max(width - width * ((nowTime - displayTime) / (animeTime * 2F + time)) + 5f, 0F),
            27f - 5f,
            ColorManage.getColorByTime()
        )
        RenderUtils.drawShadowWithCustomAlpha(3F, 0F, width.toFloat(), 27f - 5f, 255f)
        Fonts.font35.drawString(title, 6F, 3F, textColor, titleShadow)
        font.drawString(content, 6F, 12F, textColor, contentShadow)
        return false
    }
}

//NotifyType Color
enum class NotifyType(var renderColor: Color) {
    SUCCESS(Color(0x60E092)),
    ERROR(Color(0xFF2F2F)),
    WARNING(Color(0xF5FD00)),
    INFO(Color(0x6490A7));
}

enum class FadeState { IN, STAY, OUT, END }


