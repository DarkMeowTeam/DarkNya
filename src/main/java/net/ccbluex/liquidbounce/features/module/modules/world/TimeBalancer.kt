package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.*


@ModuleInfo(name = "TimeBalancer", description = "NoMove. NoCheck. NoProblem.", category = ModuleCategory.WORLD)
class TimeBalancer : Module() {

    private val speedValue = FloatValue("Speed", 2F, 0.1F, 10F)
    private val onlyOnGroundValue = BoolValue("OnlyOnGround", false)
    private var currentTick = 0

    override fun onEnable() {
        (mc.timer as IMixinTimer).timerSpeed = 1F
        currentTick = 0
    }

    override fun onDisable() {
        if (mc.player == null)
            return
        (mc.timer as IMixinTimer).timerSpeed = 1F
        currentTick = 0
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        when (event.packet) {
            is CPacketKeepAlive,
            is CPacketEntityAction,
            is CPacketPlayerDigging,
            is CPacketPlayerAbilities,
            is CPacketClientStatus,
            is SPacketAnimation,
            is SPacketDisplayObjective,
            is SPacketEntity,
            is SPacketConfirmTransaction,
            is SPacketPlayerAbilities,
            is SPacketTimeUpdate -> event.cancelEvent()
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mc.player.isSprinting = false
        if (onlyOnGroundValue.get() && !mc.player!!.onGround) return
        if (currentTick < 0) currentTick = 0
        if (currentTick > 1180) {
            currentTick = 0
            return
        }
        if (!MovementUtils.isMoving) {
            (mc.timer as IMixinTimer).timerSpeed = 1f
            currentTick++
            // 存储可加速tick
        }
        if (MovementUtils.isMoving && currentTick > 0) {
            // 移动时每游戏刻减少 1t 并设置加速
            currentTick -= speedValue.get().toInt() / 2 - 20
            (mc.timer as IMixinTimer).timerSpeed = speedValue.get()
        } else (mc.timer as IMixinTimer).timerSpeed = 1f
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        // 如果世界客户端为空，则禁用模块
        if (event.worldClient != null)
            state = false
    }
    override val tag: String
        get() = "AccelerationTime: $currentTick" + "ticks"
}