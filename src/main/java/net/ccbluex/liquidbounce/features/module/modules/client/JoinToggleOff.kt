package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.exploit.Kick
import net.ccbluex.liquidbounce.features.module.modules.exploit.PingSpoof
import net.ccbluex.liquidbounce.features.module.modules.exploit.ServerCrasher
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.player.InvManager
import net.ccbluex.liquidbounce.features.module.modules.world.ChestAura
import net.ccbluex.liquidbounce.features.module.modules.world.ChestStealer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import org.lwjgl.input.Keyboard

@ModuleInfo(name = "JoinToggleOff", description = "进入服务器自动关闭某些功能 避免卡住", category = ModuleCategory.CLIENT)
class JoinToggleOff : Module() {
    private val killauraValue = BoolValue("Killaura", false)
    private val invManagerValue = BoolValue("InvManager", false)
    private val chestStealerValue = BoolValue("ChestStealer", false)
    private val chestAuraValue = BoolValue("ChestAura", false)
    private val flyValue = BoolValue("Fly", false)
    private val speedValue = BoolValue("Speed", false)
    private val pingSpoofValue = BoolValue("PingSpoof", false)
    private val serverCrasherValue = BoolValue("ServerCrasher", false)
    private val hudValue = ListValue("Hud",arrayOf("None","Hidden","Display"),"None")
    private val bindKickNoneValue = BoolValue("BindKickNone", false)

    @EventTarget
    fun onWorld(worldEvent: WorldEvent) {
        if (killauraValue.get()) DarkNya.moduleManager[KillAura::class.java].state = false
        if (invManagerValue.get()) DarkNya.moduleManager[InvManager::class.java].state = false
        if (chestStealerValue.get()) DarkNya.moduleManager[ChestStealer::class.java].state = false
        if (chestAuraValue.get()) DarkNya.moduleManager[ChestAura::class.java].state = false
        if (flyValue.get()) DarkNya.moduleManager[Fly::class.java].state = false
        if (speedValue.get()) DarkNya.moduleManager[Speed::class.java].state = false
        if (pingSpoofValue.get()) DarkNya.moduleManager[PingSpoof::class.java].state = false
        if (serverCrasherValue.get()) DarkNya.moduleManager[ServerCrasher::class.java].state = false
        if (hudValue.get() != "None") DarkNya.moduleManager[HUD::class.java].state = hudValue.get() == "Display"
        if (bindKickNoneValue.get() && DarkNya.moduleManager[Kick::class.java].keyBind != Keyboard.CHAR_NONE) DarkNya.moduleManager[Kick::class.java].keyBind = Keyboard.CHAR_NONE
    }
}