package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.TextEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.misc.StringUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.translateAlternateColorCodes
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.TextValue

@ModuleInfo(name = "NameProtect", description = "Changes playernames clientside.", category = ModuleCategory.MISC)
class NameProtect : Module() {
    @JvmField
    val allPlayersValue = BoolValue("AllPlayers", false)

    @JvmField
    val skinProtectValue = BoolValue("SkinProtect", true)
    private val fakeNameValue = TextValue("FakeName", "&cMe")

    @EventTarget(ignoreCondition = true)
    fun onText(event: TextEvent) {
        val player = mc.player

        if (player == null || event.text!!.contains("§8[§9§l" + DarkNya.CLIENT_NAME + "§8] §3"))
            return

        for (friend in DarkNya.fileManager.friendsConfig.friends)
            event.text = StringUtils.replace(event.text, friend.playerName, translateAlternateColorCodes(friend.alias) + "§f")

        if (!state)
            return
        event.text = StringUtils.replace(event.text, player.name, translateAlternateColorCodes(fakeNameValue.get()) + "§f")

        if (allPlayersValue.get()) {
            for (playerInfo in mc.connection!!.playerInfoMap)
                event.text = StringUtils.replace(event.text, playerInfo.gameProfile.name, "Protected User")
        }
    }
}