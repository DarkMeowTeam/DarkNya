package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.entity.EntityLivingBase

@ModuleInfo(name = "EntitySpeed", description = "", category = ModuleCategory.MOVEMENT)
class EntitySpeed : Module() {
    private val onlyAir = BoolValue("OnlyAir",false)
    private val okstrafe = BoolValue("Strafe",false)
    private val keepSprint = BoolValue("KeepSprint",false)
    private val speedUp = BoolValue("SpeedUp",false)
    private val speed = IntegerValue("Speed", 0, 0, 10)
    private val distance = FloatValue("Range", 0f, 0f, 1f)
    private var speeded = false
    private var pre = false
    var sprint = false

    override fun onEnable() {
        speeded = false
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val strafe = DarkNya.moduleManager.getModule(Strafe::class.java) as Strafe
        for (entity in mc.world.loadedEntityList) {
            if (entity is EntityLivingBase && entity.entityId != mc.player.entityId && mc.player.getDistanceToEntityBox(
                    entity
                ) <= distance.get() && ( !onlyAir.get() || !mc.player.onGround)
            ) {
                if(speedUp.get()) {
                    mc.player.motionX *= (1 + (speed.get() * 0.01))
                    mc.player.motionZ *= (1 + (speed.get() * 0.01))
                }
                if(keepSprint.get()){
                    sprint = true
                }
                if(okstrafe.get()){
                    strafe.state = true
                }
                return
            }
            sprint = false
            strafe.state = false
        }
    }


}