package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue

@ModuleInfo(name = "EventManage", description = "事件管理", category = ModuleCategory.CLIENT, array = false)
class EventManage : Module() {
    companion object {
        private var logExceptionInConsoleValue = BoolValue("LogExceptionInConsole",true)
        private var logExceptionInChatValue = BoolValue("LogExceptionInChat",true)
        @JvmStatic
        fun onException(throwable: Throwable) {
            if (!DarkNya.moduleManager[EventManage::class.java].state) return

            if (logExceptionInConsoleValue.get()) throwable.printStackTrace()
            if (logExceptionInChatValue.get()) DebugManage.warn("在处理事件时发生异常:${throwable.javaClass.simpleName}${if (throwable.message.isNullOrEmpty()) { "" } else { " ${throwable.message}" }}")
        }
    }
}