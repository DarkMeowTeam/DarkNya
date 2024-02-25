package net.ccbluex.liquidbounce.features.command.commands

import catx.feitu.hwid.HwidUtil
import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.features.command.Command

class GetHwidCommand : Command("gethwid") {

    override fun execute(args: Array<String>) {
        chat("§d${DarkNya.CLIENT_NAME} §8>> §b您的设备码是 §a${HwidUtil.getHWID()}")
    }
}