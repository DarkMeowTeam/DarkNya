package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import org.apache.commons.io.IOUtils
import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.zip.ZipFile

class ScriptManagerCommand : Command("scriptmanager", "scripts") {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size > 1) {
            when {
                args[1].equals("import", true) -> {
                    try {
                        val file = MiscUtils.openFileChooser() ?: return
                        val fileName = file.name

                        if (fileName.endsWith(".js")) {
                            DarkNya.scriptManager.importScript(file)

                            DarkNya.clickGui = ClickGui()
                            DarkNya.fileManager.loadConfig(DarkNya.fileManager.clickGuiConfig)

                            chat("Successfully imported script.")
                            return
                        } else if (fileName.endsWith(".zip")) {
                            val zipFile = ZipFile(file)
                            val entries = zipFile.entries()
                            val scriptFiles = ArrayList<File>()

                            while (entries.hasMoreElements()) {
                                val entry = entries.nextElement()
                                val entryName = entry.name
                                val entryFile = File(DarkNya.scriptManager.scriptsFolder, entryName)

                                if (entry.isDirectory) {
                                    entryFile.mkdir()
                                    continue
                                }

                                val fileStream = zipFile.getInputStream(entry)
                                val fileOutputStream = FileOutputStream(entryFile)

                                IOUtils.copy(fileStream, fileOutputStream)
                                fileOutputStream.close()
                                fileStream.close()

                                if (!entryName.contains("/"))
                                    scriptFiles.add(entryFile)
                            }

                            scriptFiles.forEach { scriptFile -> DarkNya.scriptManager.loadScript(scriptFile) }

                            DarkNya.clickGui = ClickGui()
                            DarkNya.fileManager.loadConfig(DarkNya.fileManager.clickGuiConfig)
                            DarkNya.fileManager.loadConfig(DarkNya.fileManager.hudConfig)

                            chat("Successfully imported script.")
                            return
                        }

                        chat("The file extension has to be .js or .zip")
                    } catch (t: Throwable) {
                        ClientUtils.getLogger().error("Something went wrong while importing a script.", t)
                        chat("${t.javaClass.name}: ${t.message}")
                    }
                }

                args[1].equals("delete", true) -> {
                    try {
                        if (args.size <= 2) {
                            chatSyntax("scriptmanager delete <index>")
                            return
                        }

                        val scriptIndex = args[2].toInt()
                        val scripts = DarkNya.scriptManager.scripts

                        if (scriptIndex >= scripts.size) {
                            chat("Index $scriptIndex is too high.")
                            return
                        }

                        val script = scripts[scriptIndex]

                        DarkNya.scriptManager.deleteScript(script)

                        DarkNya.clickGui = ClickGui()
                        DarkNya.fileManager.loadConfig(DarkNya.fileManager.clickGuiConfig)
                        DarkNya.fileManager.loadConfig(DarkNya.fileManager.hudConfig)
                        chat("Successfully deleted script.")
                    } catch (numberFormat: NumberFormatException) {
                        chatSyntaxError()
                    } catch (t: Throwable) {
                        ClientUtils.getLogger().error("Something went wrong while deleting a script.", t)
                        chat("${t.javaClass.name}: ${t.message}")
                    }
                }

                args[1].equals("reload", true) -> {
                    try {
                        DarkNya.commandManager = CommandManager()
                        DarkNya.commandManager.registerCommands()
                        DarkNya.isStarting = true
                        DarkNya.scriptManager.disableScripts()
                        DarkNya.scriptManager.unloadScripts()
                        for(module in DarkNya.moduleManager.modules)
                            DarkNya.moduleManager.generateCommand(module)
                        DarkNya.scriptManager.loadScripts()
                        DarkNya.scriptManager.enableScripts()
                        DarkNya.fileManager.loadConfig(DarkNya.fileManager.modulesConfig)
                        DarkNya.isStarting = false
                        DarkNya.fileManager.loadConfig(DarkNya.fileManager.valuesConfig)
                        DarkNya.clickGui = ClickGui()
                        DarkNya.fileManager.loadConfig(DarkNya.fileManager.clickGuiConfig)
                        chat("Successfully reloaded all scripts.")
                    } catch (t: Throwable) {
                        ClientUtils.getLogger().error("Something went wrong while reloading all scripts.", t)
                        chat("${t.javaClass.name}: ${t.message}")
                    }
                }

                args[1].equals("folder", true) -> {
                    try {
                        Desktop.getDesktop().open(DarkNya.scriptManager.scriptsFolder)
                        chat("Successfully opened scripts folder.")
                    } catch (t: Throwable) {
                        ClientUtils.getLogger().error("Something went wrong while trying to open your scripts folder.", t)
                        chat("${t.javaClass.name}: ${t.message}")
                    }
                }
            }

            return
        }

        val scriptManager = DarkNya.scriptManager

        if (scriptManager.scripts.isNotEmpty()) {
            chat("§c§lScripts")
            scriptManager.scripts.forEachIndexed { index, script -> chat("$index: §a§l${script.scriptName} §a§lv${script.scriptVersion} §3by §a§l${script.scriptAuthors.joinToString(", ")}") }
        }

        chatSyntax("scriptmanager <import/delete/reload/folder>")
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> listOf("delete", "import", "folder", "reload")
                .filter { it.startsWith(args[0], true) }
            else -> emptyList()
        }
    }
}
