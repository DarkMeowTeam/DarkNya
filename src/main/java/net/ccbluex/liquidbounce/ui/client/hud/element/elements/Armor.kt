package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.minecraft.block.material.Material
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import me.utils.render.BlurBuffer
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import me.utils.render.ShadowUtils
import net.ccbluex.liquidbounce.utils.render.RoundedUtil
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.text.DecimalFormat


@ElementInfo(name = "Armor")
class Armor(x: Double = -8.0, y: Double = 57.0, scale: Float = 1F,
            side: Side = Side(Side.Horizontal.MIDDLE, Side.Vertical.DOWN)) : Element(x, y, scale, side) {

    private val bV = BoolValue("Blur", true)
    private val BlurStrength = FloatValue("BlurStrength", 5f,0f,20f)
    val shadowValueopen = BoolValue("shadow", true)
    private val shadowValue = FloatValue("shadow-Value", 10F, 0f, 20f)
    private val shadowColorMode = ListValue("Shadow-Color", arrayOf("Background", "Custom"), "Background")
    private val radiusValue = FloatValue("Radius", 4.25f, 0f, 10f)
    private val FShadow = BoolValue("Font-Shadow", true)
    private val alphaValue = IntegerValue("Alpha", 120, 0, 255)
    private val modeValue = ListValue("Ar-Mode", arrayOf("Mk"), "Mk")

    fun shader() {
        if (modeValue.get().equals("mk", true)) {
            RenderUtils.drawRoundedRect2(0F, 0F, 50F, 76F,5f, -1 )
        }
    }

    override fun drawElement(): Border {
        //  if (mc.playerController.isNotCreative) {
        GL11.glPushMatrix()

        val renderItem = mc.renderItem
        val isInsideWater = mc.player!!.isInsideOfMaterial(Material.WATER)

        var x = 1
        var y = if (isInsideWater) -10 else 0

        val mode = modeValue.get()

        //Rect
        RenderUtils.drawRoundedRect2(0F, 0F, 50F, 76F,radiusValue.get(), Color(11,11,12,this.alphaValue.get()).rgb )
        RenderUtils.drawRoundedRect2(x+22f,y+4f,x + 21f,y + 16.5f,0f,Color(100,100,101,this.alphaValue.get()+20).rgb)
        RenderUtils.drawRoundedRect2(x+22f,y+23.5f,x + 21f,y + 35.5f,0f,Color(100,100,101,this.alphaValue.get()+20).rgb)
        RenderUtils.drawRoundedRect2(x+22f,y+41.5f,x + 21f,y + 53.5f,0f,Color(100,100,101,this.alphaValue.get()+20).rgb)
        RenderUtils.drawRoundedRect2(x+22f,y+60.5f,x + 21f,y + 72.5f,0f,Color(100,100,101,this.alphaValue.get()+20).rgb)
        GL11.glTranslated(-renderX, -renderY, 0.0)
        GL11.glScalef(1F, 1F, 1F)
        GL11.glPushMatrix()
        //shadow
        if (shadowValueopen.get()) {
            ShadowUtils.shadow(shadowValue.get(), {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                GL11.glScalef(scale, scale, scale)

                RenderUtils.originalRoundedRect(
                    0F, 0F, 50F, 76F, radiusValue.get(),
                    if (shadowColorMode.get().equals("background", true))
                        Color(32, 30, 30).rgb
                    else
                        Color(0, 0, 0).rgb
                )
                GL11.glPopMatrix()
            }, {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                GL11.glScalef(scale, scale, scale)
                GlStateManager.enableBlend()
                GlStateManager.disableTexture2D()
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
                RenderUtils.fastRoundedRect(0F, 0F, 50F, 76F, radiusValue.get())
                GlStateManager.enableTexture2D()
                GlStateManager.disableBlend()
                GL11.glPopMatrix()
            }
            )
        }

        GL11.glPopMatrix()
        GL11.glScalef(scale, scale, scale)
        GL11.glTranslated(renderX, renderY, 0.0)
        if (bV.get()) {
            GL11.glTranslated(-renderX, -renderY, 0.0)
            GL11.glPushMatrix()
            BlurBuffer.CustomBlurRoundArea(renderX.toFloat(), renderY.toFloat()  , 50F, 76F,radiusValue.get(),BlurStrength.get())
            GL11.glPopMatrix()
            GL11.glTranslated(renderX, renderY, 0.0)
        }
        Gui.drawRect(0,0,0,0,-1)
        for (index in 3 downTo 0) {
            val stack = mc.player!!.inventory.armorInventory[index] ?: continue

            renderItem.renderItemAndEffectIntoGUI(stack, x + 3, y + 3)
            val itemDamage = stack.maxDamage - stack.itemDamage
            GlStateManager.pushMatrix()
            GlStateManager.scale(0.5F, 0.5F, 0.5F)
            Gui.drawRect(0,0,0,0,-1)
            GlStateManager.popMatrix()
            var ms = Math.round(itemDamage * 1f / stack.maxDamage * 100f).toFloat()
            var s = StringBuilder().append(DecimalFormat().format(java.lang.Float.valueOf(ms))).append("%")
                .toString()
            Fonts.fontSFUI35.drawString(s, (x + 26).toFloat(), (y + 6.7).toFloat(), -1, FShadow.get())
            //Rect Shadow
            RoundedUtil.drawRound(x+25f,y + 13.5f,(itemDamage * 1f / stack.maxDamage * 20f),1.0f,2.5f,Color(255,255,255))
            RoundedUtil.drawRound(x+25f,y + 13.8f,(itemDamage * 1f / stack.maxDamage * 20f),1.1f,2.5f,Color(255,255,255,210))
            RoundedUtil.drawRound(x+25.3f,y + 13.5f,(itemDamage * 1f / stack.maxDamage * 20f),1.1f,2.5f,Color(255,255,255,210))
            if (mode.equals("Mk", true))
                y += 18
        }

        GlStateManager.enableAlpha()
        GlStateManager.disableBlend()
        GlStateManager.disableLighting()
        GlStateManager.disableCull()
        GL11.glPopMatrix()
        // }

        return if (modeValue.get().equals("Mk", true))
            Border(0F, 0F, 50F, 76F)
        else
            Border(0F, 0F, 50F, 76F)
    }
}