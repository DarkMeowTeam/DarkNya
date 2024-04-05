package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.features.module.modules.client.ColorManage
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.value.*
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.hurtPercent
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.text.DecimalFormat

@ElementInfo(name = "DarkNyaTarget")
class DarkNyaTarget : Element(-46.0,-40.0,1F,Side(Side.Horizontal.MIDDLE,Side.Vertical.MIDDLE)) {
    private val switchModeValue = ListValue("SwitchMode", arrayOf("Slide","Zoom","None"), "Slide")
    private val animSpeedValue = IntegerValue("AnimSpeed",10,5,20)
    private val switchAnimSpeedValue = IntegerValue("SwitchAnimSpeed",20,5,40)
    private val fontValue = FontValue("Font", Fonts.font40)

    private var prevTarget: EntityLivingBase?=null
    private var lastHealth=20F
    private var lastChangeHealth=20F
    private var changeTime=System.currentTimeMillis()
    private var displayPercent=0f
    private var lastUpdate = System.currentTimeMillis()
    private val decimalFormat = DecimalFormat("0.0")

    private fun getHealth(entity: EntityLivingBase?):Float{
        return if(entity==null || entity.isDead){ 0f }else{ entity.health }
    }

    override fun drawElement(): Border? {
        var target=(DarkNya.moduleManager[KillAura::class.java] as KillAura).target
        val time=System.currentTimeMillis()
        val pct = (time - lastUpdate) / (switchAnimSpeedValue.get()*50f)
        lastUpdate=System.currentTimeMillis()

        if (mc.currentScreen is GuiHudDesigner) {
            target = mc.player
        }
        if (target != null) {
            prevTarget = target
        }
        prevTarget ?: return getTBorder()

        if (target!=null) {
            if (displayPercent < 1) {
                displayPercent += pct
            }
            if (displayPercent > 1) {
                displayPercent = 1f
            }
        } else {
            if (displayPercent > 0) {
                displayPercent -= pct
            }
            if (displayPercent < 0) {
                displayPercent = 0f
                prevTarget=null
                return getTBorder()
            }
        }

        if(getHealth(prevTarget)!=lastHealth){
            lastChangeHealth=lastHealth
            lastHealth=getHealth(prevTarget)
            changeTime=time
        }
        val nowAnimHP=if((time-(animSpeedValue.get()*50))<changeTime){
            getHealth(prevTarget)+(lastChangeHealth-getHealth(prevTarget))*(1-((time-changeTime)/(animSpeedValue.get()*50F)))
        }else{
            getHealth(prevTarget)
        }

        when(switchModeValue.get().toLowerCase()){
            "zoom" -> {
                val border=getTBorder() ?: return null
                GL11.glScalef(displayPercent,displayPercent,displayPercent)
                GL11.glTranslatef(((border.x2 * 0.5f * (1-displayPercent))/displayPercent), ((border.y2 * 0.5f * (1-displayPercent))/displayPercent).toFloat(), 0f)
            }
            "slide" -> {
                val percent= EaseUtils.easeInQuint(1.0-displayPercent)
                val xAxis= ScaledResolution(mc).scaledWidth-renderX
                GL11.glTranslated(xAxis*percent,0.0,0.0)
            }
        }

        drawRise(prevTarget!!,nowAnimHP)

        return getTBorder()
    }


    private fun drawRise(target: EntityLivingBase, easingHealth: Float){
        val font=fontValue.get()

        // 最外层
        RenderUtils.drawRect(0f,0f,150f,50f,Color(0,0,0,160).rgb)
        // 绘制头像
        if (target is EntityPlayer) { // 不是玩家类型绘制头部会出错

            val hurtPercent = target.hurtPercent
            val scale = if (hurtPercent == 0f) { 1f }
            else if(hurtPercent<0.5f){
                1-(0.2f*hurtPercent*2)
            }else{
                0.8f+(0.2f*(hurtPercent-0.5f)*2)
            }
            val size=30

            GL11.glPushMatrix()
            GL11.glTranslatef(5f, 5f, 0f)
            // 受伤的缩放效果
            GL11.glScalef(scale, scale, scale)
            GL11.glTranslatef(((size * 0.5f * (1-scale))/scale), ((size * 0.5f * (1-scale))/scale), 0f)
            // 受伤的红色效果
            GL11.glColor4f(1f, 1-hurtPercent, 1-hurtPercent, 1f)
            // 绘制头部图片
            try {
                RenderUtils.quickDrawHead(
                    mc.connection!!.getPlayerInfo(target.uniqueID).locationSkin,
                    0,
                    0,
                    size,
                    size
                )
            } catch (_:Exception) {}

            GL11.glPopMatrix()
        }
        // 文字
        font.drawString(target.name, 40, 11,Color(240, 240, 240, 255).rgb)
        font.drawString("${decimalFormat.format(mc.player!!.getDistanceToEntityBox(target))} Block", 40, 11+font.FONT_HEIGHT,Color(200, 200, 200, 255).rgb)

        // 渐变血量条
        GL11.glEnable(3042)
        GL11.glDisable(3553)
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(2848)
        GL11.glShadeModel(7425)
        fun renderSideway(x: Int,x1: Int){
            RenderUtils.quickDrawGradientSideways(x.toDouble(),39.0, x1.toDouble(),45.0,ColorManage.getStaticColorBegin(),ColorManage.getStaticColorBegin())
        }
        val stopPos=(5+((135-font.getStringWidth(decimalFormat.format(target.maxHealth)))*(easingHealth/target.maxHealth))).toInt()
        for(i in 5..stopPos step 5){
            renderSideway(i, (i + 5).coerceAtMost(stopPos))
        }
        GL11.glEnable(3553)
        GL11.glDisable(3042)
        GL11.glDisable(2848)
        GL11.glShadeModel(7424)
        GL11.glColor4f(1f, 1f, 1f, 1f)

        font.drawString(decimalFormat.format(easingHealth),stopPos+5,43-font.FONT_HEIGHT/2,Color.WHITE.rgb)
    }

    private fun getTBorder():Border {
        return Border(0F, 0F, 150F, 55F)
    }
}