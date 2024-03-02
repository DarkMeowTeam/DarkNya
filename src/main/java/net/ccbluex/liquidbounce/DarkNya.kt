package net.ccbluex.liquidbounce

import net.ccbluex.liquidbounce.event.ClientShutdownEvent
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.special.AntiForge
import net.ccbluex.liquidbounce.features.special.BungeeCordSpoof
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.script.ScriptManager
import net.ccbluex.liquidbounce.script.remapper.Remapper.loadSrg
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.client.hud.HUD.Companion.createDefault
import net.ccbluex.liquidbounce.ui.cnfont.FontLoaders
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.minecraft.util.ResourceLocation
import op.wawa.manager.CombatManager
import op.wawa.sound.Sound
import op.wawa.utils.sound.TipSoundManager

object DarkNya {

    // Client information
    const val CLIENT_NAME = "DarkNya"
    const val CLIENT_VERSION = "B1"
    const val CLIENT_CREATOR = "CatX_feitu"
    const val CLIENT_CLOUD = "https://cloud.liquidbounce.net/LiquidBounce"

    var isStarting = false

    // Managers
    lateinit var moduleManager: ModuleManager
    lateinit var commandManager: CommandManager
    lateinit var eventManager: EventManager
    lateinit var fileManager: FileManager
    lateinit var scriptManager: ScriptManager
    lateinit var combatManager: CombatManager
    lateinit var tipSoundManager: TipSoundManager

    // HUD & ClickGUI
    lateinit var hud: HUD

    lateinit var clickGui: ClickGui

    // Menu Background
    var background: ResourceLocation? = null


    var mainMenuPrep = false
    var darkMode = false

    /**
     * Execute if client will be started
     */
    fun startClient() {
        isStarting = true

        ClientUtils.getLogger().info("Loading $CLIENT_NAME $CLIENT_VERSION")
        ClientUtils.getLogger().info("Initializing...")
        val startTime = System.currentTimeMillis()

        // Initialize managers
        fileManager = FileManager()
        eventManager = EventManager()

        combatManager = CombatManager()
        // Register listeners
        eventManager.registerListener(combatManager)
        eventManager.registerListener(RotationUtils())
        eventManager.registerListener(AntiForge())
        eventManager.registerListener(BungeeCordSpoof())
        eventManager.registerListener(InventoryUtils())

        tipSoundManager = TipSoundManager()

        commandManager = CommandManager()

        // Load client fonts
        Fonts.loadFonts()
        FontLoaders.initFonts()
        ClientUtils.getLogger().info("$CLIENT_NAME >> Fonts Loaded.")

        moduleManager = ModuleManager()

        // Setup modules
        moduleManager.registerModules()
        ClientUtils.getLogger().info("$CLIENT_NAME >> Modules Loaded.")

        try {
            loadSrg()
            scriptManager = ScriptManager()
            scriptManager.loadScripts()
            scriptManager.enableScripts()

            ClientUtils.getLogger().info("$CLIENT_NAME >> Scripts Loaded.")
        } catch (throwable: Throwable) {
            ClientUtils.getLogger().error("Failed to load scripts.", throwable)
        }

        // Register commands
        commandManager.registerCommands()
        ClientUtils.getLogger().info("$CLIENT_NAME >> Commands Loaded.")

        // Load configs
        fileManager.loadConfigs(fileManager.modulesConfig, fileManager.valuesConfig, fileManager.accountsConfig,
            fileManager.friendsConfig, fileManager.xrayConfig, fileManager.shortcutsConfig)

        // ClickGUI
        clickGui = ClickGui()
        fileManager.loadConfig(fileManager.clickGuiConfig)

        fileManager.loadConfigs(
            fileManager.modulesConfig,
            fileManager.valuesConfig
        )

        // Set HUD
        hud = createDefault()
        fileManager.loadConfig(fileManager.hudConfig)

        // Load generators
        GuiAltManager.loadGenerators()

        // Set is starting status
        isStarting = false
        // Log success
        ClientUtils.getLogger().info("$CLIENT_NAME $CLIENT_VERSION loaded in ${(System.currentTimeMillis() - startTime)}ms!")
        // Sound
        Sound.INSTANCE.Spec()
    }

    /**
     * Execute if client will be stopped
     */
    fun stopClient() {
        // Call client shutdown
        eventManager.callEvent(ClientShutdownEvent())

        // Save all available configs
        fileManager.saveAllConfigs()
    }

}