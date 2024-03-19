package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.utils.ClientUtils

@ModuleInfo(name = "DebugManage", description = "控制全局模块输出debugger", category = ModuleCategory.CLIENT, array = false)
class DebugManage : Module() {
    var prefixValue = BoolValue("Prefix",true)
    var optValue = ListValue("Opt",arrayOf("All","Less"),"All")

    companion object {
        @JvmStatic
        fun info(info: String) {
            if (
                DarkNya.moduleManager[DebugManage::class.java].state  &&
                (DarkNya.moduleManager[DebugManage::class.java] as DebugManage).optValue.get().toLowerCase() != "less"
            ) {
                ClientUtils.displayChatMessage("${getPrefix()}§7$info")
            }
        }
        @JvmStatic
        fun warn(info: String) {
            if (DarkNya.moduleManager[DebugManage::class.java].state) {
                ClientUtils.displayChatMessage("${getPrefix()}§c$info")
            }
        }
        private fun getPrefix(): String {
            return if ((DarkNya.moduleManager[DebugManage::class.java] as DebugManage).prefixValue.get())
                "§d${DarkNya.CLIENT_NAME} §8>> "
                else ""
        }
    }
}