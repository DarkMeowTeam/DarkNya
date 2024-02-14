package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.font.Fonts

class ReloadCommand : Command("reload", "configreload") {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        chat("Reloading...")
        chat("§c§lReloading commands...")
        DarkNya.commandManager = CommandManager()
        DarkNya.commandManager.registerCommands()
        DarkNya.isStarting = true
        DarkNya.scriptManager.disableScripts()
        DarkNya.scriptManager.unloadScripts()
        for(module in DarkNya.moduleManager.modules)
            DarkNya.moduleManager.generateCommand(module)
        chat("§c§lReloading scripts...")
        DarkNya.scriptManager.reloadScripts()
        chat("§c§lReloading fonts...")
        Fonts.loadFonts()
        chat("§c§lReloading modules...")
        DarkNya.fileManager.loadConfig(DarkNya.fileManager.modulesConfig)
        DarkNya.isStarting = false
        chat("§c§lReloading values...")
        DarkNya.fileManager.loadConfig(DarkNya.fileManager.valuesConfig)
        chat("§c§lReloading accounts...")
        DarkNya.fileManager.loadConfig(DarkNya.fileManager.accountsConfig)
        chat("§c§lReloading friends...")
        DarkNya.fileManager.loadConfig(DarkNya.fileManager.friendsConfig)
        chat("§c§lReloading xray...")
        DarkNya.fileManager.loadConfig(DarkNya.fileManager.xrayConfig)
        chat("§c§lReloading HUD...")
        DarkNya.fileManager.loadConfig(DarkNya.fileManager.hudConfig)
        chat("§c§lReloading ClickGUI...")
        DarkNya.clickGui = ClickGui()
        DarkNya.fileManager.loadConfig(DarkNya.fileManager.clickGuiConfig)
        DarkNya.isStarting = false
        chat("Reloaded.")
    }
}
