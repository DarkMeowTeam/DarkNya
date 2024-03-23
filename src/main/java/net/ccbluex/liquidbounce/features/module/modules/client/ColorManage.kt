package net.ccbluex.liquidbounce.features.module.modules.client

import me.utils.render.VisualUtils
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.*
import java.awt.Color
import kotlin.math.abs

@ModuleInfo(name = "Color", description = "Toggles visibility of the HUD.", category = ModuleCategory.CLIENT, array = false)
class ColorManage : Module() {
      companion object {
        val redBeginValue = IntegerValue("RedBegin",160,0,255)
        val greenBeginValue = IntegerValue("GreenBegin",0,0,255)
        val blueBeginValue = IntegerValue("BlueBegin",255,0,255)

        val redEndValue = IntegerValue("RedEnd",100,0,255)
        val greenEndValue = IntegerValue("GreenEnd",40,0,255)
        val blueEndValue = IntegerValue("BlueEnd",255,0,255)

        val fadeOffSpeedValue = IntegerValue("FadeOffSpeed",1000,1,1000)

          @JvmStatic
          fun getColor(index: Double): Int {
              return VisualUtils.getGradientOffset(
                  Color(redBeginValue.get(), greenBeginValue.get(), blueBeginValue.get()),
                  Color(redEndValue.get(), greenEndValue.get(), blueEndValue.get()),
                  index
              ).rgb
          }
          @JvmStatic
          fun getColorByTime(): Int {
              return getColor(
                  abs(
                      System.currentTimeMillis() / fadeOffSpeedValue.get().toDouble()
                  ) / 10F
              )
          }
          @JvmStatic
          fun getStaticColorBegin(): Int {
              return Color(
                  redBeginValue.get(),
                  greenBeginValue.get(),
                  blueBeginValue.get(),
                  255
              ).rgb
          }
          @JvmStatic
          fun getStaticColorEnd(): Int {
              return Color(
                  redEndValue.get(),
                  greenEndValue.get(),
                  blueEndValue.get(),
                  255
              ).rgb
          }
    }
}