package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.utils.ClientUtils

@ModuleInfo(name = "DebugManage", description = "控制全局模块输出debugger", category = ModuleCategory.CLIENT, array = false)
class DebugManage : Module() {
    val infoValue = BoolValue("Info", true)
    val warnValue = BoolValue("Warn", true)

    companion object {
        @JvmStatic
        fun info(info: String) {
            if (DarkNya.moduleManager[DebugManage::class.java].state && (DarkNya.moduleManager[DebugManage::class.java] as DebugManage).infoValue.get()) {
                ClientUtils.displayChatMessage("§d${DarkNya.CLIENT_NAME} §8>> §7$info")
            }
        }
        @JvmStatic
        fun warn(info: String) {
            if (DarkNya.moduleManager[DebugManage::class.java].state && (DarkNya.moduleManager[DebugManage::class.java] as DebugManage).warnValue.get()) {
                ClientUtils.displayChatMessage("§d${DarkNya.CLIENT_NAME} §c>> §7$info")
            }
        }
    }
}