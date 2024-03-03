package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.Criticals
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.exploit.PUBGDisabler
import net.ccbluex.liquidbounce.features.module.modules.exploit.Phase
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly
import net.ccbluex.liquidbounce.features.module.modules.movement.NoClip
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.player.AntiAim
import net.ccbluex.liquidbounce.features.module.modules.player.InvManager
import net.ccbluex.liquidbounce.features.module.modules.world.ChestAura
import net.ccbluex.liquidbounce.features.module.modules.world.ChestStealer
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.minecraft.network.play.client.CPacketEntityAction

import net.minecraft.network.play.server.SPacketTitle

@ModuleInfo(name = "PUBGHelper", description = "花雨庭代号吃鸡助手 适用于lowiq的自动跳伞和自动开无敌 | by CatX_feitu", category = ModuleCategory.MISC)
class PUBGHelper : Module() {
    private val autoParachuteValue = BoolValue("AutoParachute", false)
    private val autoDisableValue = BoolValue("AutoDisable", false)
    private val autoDisableTicketValue = IntegerValue("AutoDisableTicket", 40,0,200)
    private val lowIQFix = BoolValue("LowIQFix", false)
    private val autoToggleCriticals = BoolValue("AutoToggleCriticals", false)
    private val autoToggleAntiAim = BoolValue("AutoToggleAntiAim", false)
    private val autoToggleFly = BoolValue("AutoToggleFly", false)

    private var parachuteTicket = -1
    @EventTarget
    fun onPacket(event:PacketEvent) {
        val packet = event.packet
        if (packet is SPacketTitle) {
            if (packet.message.unformattedText == "§a§l按Shift跳伞" && autoParachuteValue.get()) {
                ClientUtils.displayChatMessage("§d${DarkNya.CLIENT_NAME} §8>> §a自动跳伞")
                mc.connection!!.sendPacket(CPacketEntityAction(mc.player!!, CPacketEntityAction.Action.START_SNEAKING))
                mc.connection!!.sendPacket(CPacketEntityAction(mc.player!!, CPacketEntityAction.Action.STOP_SNEAKING))
            }
            if (packet.message.unformattedText == "§a跳伞成功" && autoDisableValue.get()) {
                ClientUtils.displayChatMessage("§d${DarkNya.CLIENT_NAME} §8>> §7" + packet.message)
                parachuteTicket = 0
            }
        }
    }
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (parachuteTicket >= 0) {
            parachuteTicket++
            if (parachuteTicket >= autoDisableTicketValue.get()) {
                DarkNya.moduleManager[PUBGDisabler::class.java].state = true

                if (autoToggleAntiAim.get()) DarkNya.moduleManager[AntiAim::class.java].state = true
                if (autoToggleCriticals.get()) DarkNya.moduleManager[Criticals::class.java].state = true
                if (autoToggleFly.get()) DarkNya.moduleManager[Fly::class.java].state = true

                ClientUtils.displayChatMessage("§d${DarkNya.CLIENT_NAME} §8>> §a游戏开始~助君飘得愉快~")

                parachuteTicket = -1
            }
        }
    }
    @EventTarget
    fun onWorld(worldEvent: WorldEvent) {
        // 切换世界 可以当成进游戏
        if (autoDisableValue.get()) DarkNya.moduleManager[PUBGDisabler::class.java].state = false //自动关闭Disable
        if (lowIQFix.get()) {
            DarkNya.moduleManager[KillAura::class.java].state = false
            DarkNya.moduleManager[Fly::class.java].state = false
            DarkNya.moduleManager[Speed::class.java].state = false
            DarkNya.moduleManager[Criticals::class.java].state = false
            DarkNya.moduleManager[AntiAim::class.java].state = false
            DarkNya.moduleManager[ChestAura::class.java].state = false
            DarkNya.moduleManager[ChestStealer::class.java].state = false
            DarkNya.moduleManager[InvManager::class.java].state = false
            DarkNya.moduleManager[Phase::class.java].state = false
            DarkNya.moduleManager[NoClip::class.java].state = false
        }
    }
}