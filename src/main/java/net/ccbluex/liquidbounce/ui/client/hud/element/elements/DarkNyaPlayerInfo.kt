
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.features.module.modules.client.ColorManage
import net.ccbluex.liquidbounce.features.module.modules.misc.SelfBanChecker
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.InfosUtils.Recorder
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.hurtPercent
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*

@ElementInfo(name = "DarkNyaPlayerInfo")
class DarkNyaPlayerInfo(
    x: Double = 3.39,
    y: Double = 24.48,
    scale: Float = 1F,
    side: Side = Side.default()
) : Element(x, y, scale, side) {

    private val titleValue = BoolValue("Title",true)
    private val shadowValue = BoolValue("Shadow", true)


    val timer = MSTimer()

    //old pride plus
    private val DATE_FORMAT = SimpleDateFormat("HH:mm:ss")

    override fun drawElement(): Border {
        val player = mc.player ?: return Border(0F, 0F, 150F, 32F)

        var startY = 1F

        // 顶部线条
        RenderUtils.drawRect(0F,0F,150F,1F, ColorManage.getColorByTime())

        // 标题栏
        if (titleValue.get()) {
            startY = 14F
            RenderUtils.drawRect(0F, 1F, 150F, startY, Color(0, 0, 0 , 180).rgb)
            Fonts.font40.drawStringWithShadow(
                "Player Info", 5F,3F, ColorManage.getColorByTime()
            )
        }

        // 填充栏
        RenderUtils.drawRect(0F, startY, 150F, startY + 32F, Color(0, 0, 0, 160).rgb)

        if (shadowValue.get()) {
            RenderUtils.drawShadowWithCustomAlpha(0F, 0F, 150F, startY + 32F, 200F)
        }

        // 玩家头像贴图
        val hurtPercent = player.hurtPercent

        val size = 24
        GL11.glPushMatrix()

        GL11.glColor4f(1f, 1-hurtPercent, 1-hurtPercent, 1f) // 受伤的红色效果

        RenderUtils.quickDrawHead(
            mc.connection!!.getPlayerInfo(player.uniqueID).locationSkin,
            8, startY.toInt() + 3, size, size
        )
        GL11.glPopMatrix()

        Fonts.font35.drawStringWithShadow(mc.player.name,
            16F + size,
            startY + 6F, Color(240, 240, 240, 255).rgb)
        Fonts.font35.drawStringWithShadow("${DATE_FORMAT.format(Date(System.currentTimeMillis() - Recorder.startTime - 8000L * 3600L))} - ${SelfBanChecker.banCount} Bans",
            16F + size,
            startY + 19F, Color(200, 200, 200, 255).rgb)


        return Border(0F, 0F, 150F, startY + 32F)
    }
}
