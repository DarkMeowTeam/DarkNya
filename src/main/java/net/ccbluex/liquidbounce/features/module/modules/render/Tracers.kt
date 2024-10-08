package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*

@ModuleInfo(name = "Tracers", description = "Draws a line to targets around you.", category = ModuleCategory.RENDER)
class Tracers : Module() {
    private val colorMode = ListValue("Color", arrayOf("Custom", "DistanceColor"), "Theme")

    private val playerHeightValue = BoolValue("PlayerHeight", true)

    private val thicknessValue = FloatValue("Thickness", 2F, 1F, 5F)
    private val colorAlphaValue = IntegerValue("Alpha", 150, 1, 255)

    private val colorRedValue = IntegerValue("R", 0, 0, 255)
    private val colorGreenValue = IntegerValue("G", 160, 0, 255)
    private val colorBlueValue = IntegerValue("B", 255, 0, 255)


    private val directLineValue = BoolValue("Directline", false)
    private val fovModeValue = ListValue("FOV-Mode", arrayOf("All", "Back", "Front"), "All")
    private val fovValue = FloatValue("FOV", 180F, 0F, 180F).displayable { !fovModeValue.get().equals("all", true) }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(thicknessValue.get())
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(false)

        for (entity in if (fovModeValue.get().equals("all", true)) mc.world.loadedEntityList else mc.world.loadedEntityList.filter { if (fovModeValue.get().equals("back", true)) RotationUtils.getRotationBackDifference(it) <= fovValue.get() else RotationUtils.getRotationDifference(it) <= fovValue.get() }) {
            if (entity != null && entity != mc.player && EntityUtils.isSelected(entity, false)) {
                var dist = (mc.player.getDistance(entity) * 2).toInt()

                if (dist > 255) dist = 255

                val colorMode = colorMode.get().toLowerCase(Locale.getDefault())
                val color = when {
                    EntityUtils.isFriend(entity) -> Color(0, 255, 0, 200)
                    colorMode == "custom" -> Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), 150)
                    colorMode == "distancecolor" -> Color(255 - dist, dist, 0, 150)
                    else -> Color(255, 255, 255, 150)
                }

                drawTraces(entity, color, !directLineValue.get())
            }
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(true)
        GL11.glDisable(GL11.GL_BLEND)
        GlStateManager.resetColor()
    }

    fun drawTraces(entity: Entity, color: Color, drawHeight: Boolean) {
        val x = (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks
                - mc.renderManager.renderPosX)
        val y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks
                - mc.renderManager.renderPosY)
        val z = (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks
                - mc.renderManager.renderPosZ)

        val eyeVector = Vec3d(0.0, 0.0, 1.0)
            .rotatePitch((-Math.toRadians(mc.player.rotationPitch.toDouble())).toFloat())
            .rotateYaw((-Math.toRadians(mc.player.rotationYaw.toDouble())).toFloat())

        RenderUtils.glColor(color, colorAlphaValue.get())

        GL11.glBegin(GL11.GL_LINE_STRIP)
        GL11.glLineWidth(thicknessValue.get())
        GL11.glVertex3d(eyeVector.x,
            if(playerHeightValue.get()) { mc.player.getEyeHeight().toDouble() } else { 0.0 } + eyeVector.y,
            eyeVector.z)
        if(drawHeight) {
            GL11.glVertex3d(x, y + entity.height, z)
        } else {
            GL11.glVertex3d(x, y, z)
        }
        GL11.glEnd()
    }
}
