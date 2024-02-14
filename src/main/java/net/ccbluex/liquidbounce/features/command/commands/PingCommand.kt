package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command

class PingCommand : Command("ping") {

    override fun execute(args: Array<String>) {
        chat("§3Your ping is §a${mc.connection!!.getPlayerInfo(mc.player!!.uniqueID)!!.responseTime}ms§3.")
    }

}