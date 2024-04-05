package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.DebugManage
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.network.play.server.SPacketChat

import net.minecraft.network.play.server.SPacketTitle

@ModuleInfo(name = "TitleManage", description = "输出服务器发来的title信息", category = ModuleCategory.MISC)
class TitleManage : Module() {
    private val printPacketInfoEventValue = BoolValue("PrintPacketInfo", false)
    private val cancelEventValue = BoolValue("CancelEvent", false)

    @EventTarget
    fun onPacket(event:PacketEvent) {
        val packet = event.packet
        if (packet is SPacketTitle) {
            if (printPacketInfoEventValue.get()) DebugManage.info("${packet.type}  ${packet.message}")
            if (cancelEventValue.get()) event.cancelEvent()

        }
        if (packet is SPacketChat) {
            if (packet.type == net.minecraft.util.text.ChatType.GAME_INFO) {
                // actionbar 是通过 SPacketChat 传递的不是 SPacketTitle
                if (printPacketInfoEventValue.get()) DebugManage.info("${SPacketTitle.Type.ACTIONBAR}  ${packet.chatComponent}")
                if (cancelEventValue.get()) event.cancelEvent()
            }
        }
    }
}