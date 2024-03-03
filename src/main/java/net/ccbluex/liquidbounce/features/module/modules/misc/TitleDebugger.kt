package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.utils.ClientUtils

import net.minecraft.network.play.server.SPacketTitle

@ModuleInfo(name = "TitleDebugger", description = "输出服务器发来的title信息", category = ModuleCategory.MISC)
class TitleDebugger : Module() {
    private val cancelEventValue = BoolValue("CancelEvent", false)

    @EventTarget
    fun onPacket(event:PacketEvent) {
        val packet = event.packet
        if (packet is SPacketTitle) {
            ClientUtils.displayChatMessage("§d${DarkNya.CLIENT_NAME} §8>> §7" + packet.message)
            if (cancelEventValue.get()) event.cancelEvent()
        }

    }

}