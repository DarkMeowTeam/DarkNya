
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.ListValue

import net.minecraft.util.ResourceLocation

@ModuleInfo(name = "Cape", description = "LiquidBounce+ capes.", category = ModuleCategory.CLIENT)
class Cape : Module() {

    val styleValue = ListValue("Style", arrayOf("Dark", "Astolfo", "Sunny", "Target", "Wyy", "PowerX", "Azrael", "Flux", "LiquidBounce", "Light", "Novoline", "Special1", "Special2"), "Dark")

    fun getCapeLocation(value: String): ResourceLocation {
        return try {
            CapeStyle.valueOf(value.toUpperCase()).location
        } catch (e: IllegalArgumentException) {
            CapeStyle.DARK.location
        }
    }

    enum class CapeStyle(val location: ResourceLocation) {
        DARK(ResourceLocation("darknya/capes/dark.png")),
        ASTOLFO(ResourceLocation("darknya/capes/astolfo.png")),
        LIGHT(ResourceLocation("darknya/capes/light.png")),
        SUNNY(ResourceLocation("darknya/capes/Sunny.png")),
        WYY(ResourceLocation("darknya/capes/Wyy.png")),
        POWERX(ResourceLocation("darknya/capes/PowerX.png")),
        AZRAEL(ResourceLocation("darknya/capes/azrael.png")),
        TARGET(ResourceLocation("darknya/capes/Target.png")),
        FLUX(ResourceLocation("darknya/capes/Flux.png")),
        LIQUIDBOUNCE(ResourceLocation("darknya/capes/LiquidBounce.png")),
        NOVOLINE(ResourceLocation("darknya/capes/Novoline.png")),
        SPECIAL1(ResourceLocation("darknya/capes/special1.png")),
        SPECIAL2(ResourceLocation("darknya/capes/special2.png"))
    }

    override val tag: String
        get() = styleValue.get()

}