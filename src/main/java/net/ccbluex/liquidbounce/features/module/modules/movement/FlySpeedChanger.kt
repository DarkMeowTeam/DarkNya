package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.value.*

@ModuleInfo(name = "FlySpeedChanger", description = "Change speed in vanilla speed.", category = ModuleCategory.MOVEMENT)
class FlySpeedChanger : Module() {
    private val modeValue = ListValue("Mode", arrayOf("NoEntityNear"), "NoEntityNear")

    private val entityDistanceValue = IntegerValue("Distance", 255,0,255).displayable {
        modeValue.get().toLowerCase() == "noentitynear"
    }
    private val entityOnlyTargetValue = BoolValue("OnlyTarget", false).displayable {
        modeValue.get().toLowerCase() == "noentitynear"
    }

    private val normalSpeedValue = FloatValue("SpeedNormal", 2f, 0f, 10f)
    private val activeSpeedValue = FloatValue("SpeedActive", 10f, 0f, 10f)

    private val calcCoolDownValue = IntegerValue("CalcCoolDown", 20, 0, 200) // 每多少ticket(1s=20tuicket) 计算一次

    private var ticket = 0

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        ticket++
        if (ticket < calcCoolDownValue.get()) return
        ticket = 0

        var active = false
        when (modeValue.get().toLowerCase())
        {
            "noentitynear" -> {
                mc.world.loadedEntityList.forEach { entity ->
                    if (entity == mc.player) return@forEach
                    if (entityOnlyTargetValue.get() && !EntityUtils.isSelected(entity,false)) return@forEach

                    if (entity.getDistance(mc.player) <= entityDistanceValue.get()) active = true
                }
            }
        }
        toggleFlySpeed(active)
    }


    private fun toggleFlySpeed(active : Boolean) {
        (DarkNya.moduleManager[Fly::class.java] as Fly).vanillaSpeedValue.set(
            if (active) {
                activeSpeedValue.get()
            } else {
                normalSpeedValue.get()
            }
        )
    }
}
