package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer


@ModuleInfo(name = "ReallyHurt", category =ModuleCategory.MISC, description = "空刀检测 | by CatX_feitu")
class ReallyHurt : Module() {

    private val attackTestTime = IntegerValue("AttackTestTime", 2, 1, 40)
    private val onlyPlayer = BoolValue("OnlyPlayer", false)

    private val killauraChange = BoolValue("KillauraChange", false)
    private val killauraChangeKeepTicket = IntegerValue("KillauraChangeKeepTicket", 120,20,1200)
    private val killauraChangeRangeGround = FloatValue("KillauraChangeRangeGround", 2.95f, 1f, 8f)
    private val killauraChangeRangeAir = FloatValue("KillauraChangeRangeAir", 2.9f, 1f, 8f)


    private val debug = BoolValue("Debug", true)
    private val debugPrintFully = BoolValue("DebugPrintFully", false)

    var failedHit = 0 // 空刀次数
    var blinked = false
    var killauraChangeRangeTickets = -1;

    private var hitEntities = mutableMapOf<Int, Int>()  // 存储被攻击的实体    EntityID , 攻击时间

    @EventTarget
    fun onAttack(event: AttackEvent) {
        // Blink状态取消记录判断
        if (DarkNya.moduleManager[Blink::class.java].state) return
        val targetEntity = event.targetEntity
        // onlyPlayer筛选
        if (onlyPlayer.get() && targetEntity !is EntityPlayer) return
        // 当攻击实体时，将其添加到map中，并记录当前游戏刻
        if (targetEntity is EntityLivingBase) {
            if (debug.get() && debugPrintFully.get()) ClientUtils.displayChatMessage("§d${DarkNya.CLIENT_NAME} §8>> §b于时间 ${mc.world.totalWorldTime.toInt()} 尝试攻击 ${targetEntity.entityId}")
            if (!hitEntities.containsKey(targetEntity.entityId)) hitEntities[targetEntity.entityId] = mc.world.totalWorldTime.toInt()
        }
    }

    @EventTarget
    fun onUpdateEvent(event: UpdateEvent) {
        // Blink状态取消记录判断
        if (DarkNya.moduleManager[Blink::class.java].state) {
            blinked = true
            return
        }
        // 之前执行过Blink需要清空
        if (blinked) {
            blinked = false
            if (debug.get() && debugPrintFully.get()) ClientUtils.displayChatMessage("§d${DarkNya.CLIENT_NAME} §8>> §cBlink关闭 清空实体列表 §7(Count:${hitEntities.count()})")
            hitEntities = mutableMapOf()
        }

        // 检查所有记录的实体
        val worldTime = mc.world.totalWorldTime.toInt()

        hitEntities.forEach { (entityID, time) ->
            val entity = mc!!.world!!.getEntityByID(entityID) as? EntityLivingBase
            // entityID 不存在删除并返回
            if (entity == null) {
                hitEntities.remove(entityID)
                return
            }
            // 如果实体已死亡或者超时，从map中移除
            // 不移除的话内存会不断升高
            if (entity.isDead || entity.health == 0f || worldTime - time > attackTestTime.get() + 20) {
                if (debug.get() && debugPrintFully.get()) ClientUtils.displayChatMessage("§d${DarkNya.CLIENT_NAME} §8>> §c删除实体 §7" +
                        "(ID:${entity.entityId},isPlayer:${entity is EntityPlayer},Health:${entity.health},HurtTime:${entity.hurtTime},HurtResistantTime:${entity.hurtResistantTime},isDead:${entity.isDead})")
                hitEntities.remove(entityID)
            }
            // 如果实体在适当时间内没有受伤，假定为空刀
            if (!entity.isDead && worldTime - time > attackTestTime.get() && entity.hurtTime == 0 && entity.hurtResistantTime == 0) {
                onHurtFailed(entity)
                hitEntities.remove(entityID)
            }

        }


        if (killauraChange.get() && killauraChangeRangeTickets >= 0) {
            if (debug.get() && killauraChangeRangeTickets == 0) ClientUtils.displayChatMessage("§d${DarkNya.CLIENT_NAME} §8>> §b已切换killaura到安全距离" )
            (DarkNya.moduleManager[KillAura::class.java] as KillAura).pauseSelfRangeChange = true
            (DarkNya.moduleManager[KillAura::class.java] as KillAura).range = if (mc.player!!.onGround) killauraChangeRangeGround.get() else killauraChangeRangeAir.get()
            killauraChangeRangeTickets ++
            if (killauraChangeRangeTickets > killauraChangeKeepTicket.get()) {
                (DarkNya.moduleManager[KillAura::class.java] as KillAura).pauseSelfRangeChange = false
                killauraChangeRangeTickets = -1
                if (debug.get() && killauraChangeRangeTickets == 0) ClientUtils.displayChatMessage("§d${DarkNya.CLIENT_NAME} §8>> §b单位时间内无空刀 已恢复距离" )
            }
        }
    }

    override fun onDisable() {
        if (debug.get() && debugPrintFully.get()) ClientUtils.displayChatMessage("§d${DarkNya.CLIENT_NAME} §8>> §c模块关闭 清空实体列表 §7(Count:${hitEntities.count()})")
        hitEntities = mutableMapOf()
        if (killauraChangeRangeTickets != -1) (DarkNya.moduleManager[KillAura::class.java] as KillAura).pauseSelfRangeChange = false
        killauraChangeRangeTickets = -1
    }

    private fun onHurtFailed(entity : EntityLivingBase) {
        failedHit ++
        killauraChangeRangeTickets = if (killauraChangeRangeTickets == -1) { 0 } else { 1 }
        if (debug.get()) ClientUtils.displayChatMessage("§d${DarkNya.CLIENT_NAME} §8>> §c空刀" +
                (if (debugPrintFully.get()) " §7(ID:${entity.entityId},isPlayer:${entity is EntityPlayer},Health:${entity.health},HurtTime:${entity.hurtTime},HurtResistantTime:${entity.hurtResistantTime},isDead:${entity.isDead})" else "")
        )
    }

    override val tag: String
        get() = "$failedHit${if (killauraChangeRangeTickets != -1) " SafeTime:${killauraChangeRangeTickets}" else ""}"
}