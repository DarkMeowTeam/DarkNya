package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.network.play.client.CPacketChatMessage

@ModuleInfo(name = "ChatControl", description = "聊天控制", category = ModuleCategory.CLIENT)
class ChatControl : Module() {
    private val forceUnicodeChatValue = BoolValue("ForceUnicodeChat",false)
    private val forceUnicodeChatCommandValue = BoolValue("ForceUnicodeChatCommand",false).displayable { forceUnicodeChatValue.get() }

    private val prefixValue = TextValue("Prefix", "")
    private val suffixValue = TextValue("Suffix", "")

    private val checkInvalidMessageValue = BoolValue("CheckInvalidMessage", false)

    @EventTarget
    fun onPacket(event:PacketEvent) {
        val packet = event.packet
        if (packet is CPacketChatMessage) {
            var message = packet.message ?: return

            if (forceUnicodeChatValue.get() && (!forceUnicodeChatCommandValue.get() || !message.startsWith("/"))) message = unicodeText(message)

            message = prefixValue.get() + message + suffixValue.get()

            if (checkInvalidMessageValue.get()) {
                if (message.contains("§")) event.cancelEvent()
            }

            packet.message = message

        }
    }
    private fun unicodeText(input: String): String {
        val stringBuilder = StringBuilder()

        for (c in input.toCharArray())
            if (c.toInt() in 33..128)
                stringBuilder.append(Character.toChars(c.toInt() + 65248)) else stringBuilder.append(c)

        return stringBuilder.toString()
    }

}