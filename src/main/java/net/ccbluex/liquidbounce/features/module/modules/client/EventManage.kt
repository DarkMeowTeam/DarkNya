package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.event.Event
import net.ccbluex.liquidbounce.event.EventHook
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.value.BoolValue

@ModuleInfo(name = "EventManage", description = "事件管理", category = ModuleCategory.CLIENT, array = false)
class EventManage : Module() {
    companion object {
        private var logExceptionInConsoleValue = BoolValue("LogExceptionInConsole",true)
        private var logExceptionInChatValue = BoolValue("LogExceptionInChat",true)
        private var logLagInConsoleValue = BoolValue("LogLagInConsole",true)
        private var logLagInChatValue = BoolValue("LogLagInChat",true)
        @JvmStatic
        fun onException(throwable: Throwable) {
            if (!DarkNya.moduleManager[EventManage::class.java].state) return

            if (logExceptionInConsoleValue.get()) throwable.printStackTrace()
            if (logExceptionInChatValue.get()) DebugManage.warn(
                "在处理事件时发生异常:${throwable.javaClass.simpleName}${if (throwable.message.isNullOrEmpty()) { "" } else { " ${throwable.message}" }}")
        }
        @JvmStatic
        fun onLag(used: Long, event: Event, invokableEventTarget: EventHook) {
            if (!DarkNya.moduleManager[EventManage::class.java].state) return

            if (logLagInConsoleValue.get()) {
                ClientUtils.getLogger().info(
                    "在处理事件时耗时异常\n" +
                    "Used:${used}ms\n" +
                    "Event:${event.javaClass.simpleName}\n" +
                    "Invokable:\n" +
                    "  ClassName:${invokableEventTarget.eventClass.javaClass.name}\n" +
                    "  Status:${invokableEventTarget.eventClass.handleEvents()}"
                )
            }
            if (logLagInChatValue.get()) DebugManage.warn(
                "在处理事件是耗时异常:${event.javaClass.simpleName} ${invokableEventTarget.eventClass.javaClass.simpleName} (${used}ms)"
            )
        }
    }
}