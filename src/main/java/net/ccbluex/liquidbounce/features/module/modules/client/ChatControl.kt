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
    private val replaceCharsValue = ListValue("ReplaceChars", arrayOf("Normal","Width"),"Normal")

    private val prefixValue = TextValue("Prefix", "")
    private val suffixValue = TextValue("Suffix", "")

    private val checkInvalidMessageValue = BoolValue("CheckInvalidMessage", false)

    @EventTarget
    fun onPacket(event:PacketEvent) {
        val packet = event.packet
        if (packet is CPacketChatMessage) {
            packet.message = styleText(packet.message)
            packet.message = prefixValue.get() + packet.message + suffixValue.get()

            if (checkInvalidMessageValue.get()) {
                if (packet.message.contains("§")) event.cancelEvent()
            }

        }
    }
    private val normalChars = "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z a b c d e f g h i j k l m n o p q r s t u v w x y z 1 2 3 4 5 6 7 8 9 0".split(" ")
    private val styledCharsWidth = "Ａ Ｂ Ｃ Ｄ Ｅ Ｆ Ｇ Ｈ Ｉ Ｊ Ｋ Ｌ Ｍ Ｎ Ｏ Ｐ Ｑ Ｒ Ｓ Ｔ Ｕ Ｖ Ｗ Ｘ Ｙ Ｚ ａ ｂ ｃ ｄ ｅ ｆ ｇ ｈ ｉ ｊ ｋ ｌ ｍ ｎ ｏ ｐ ｑ ｒ ｓ ｔ ｕ ｖ ｗ ｘ ｙ ｚ １ ２ ３ ４ ５ ６ ７ ８ ９ ０"
    private fun styleText(input: String): String {
        var styledCars = listOf("")
        when (replaceCharsValue.get().toLowerCase())
        {
            "normal" -> styledCars = listOf("")
            "width" -> styledCars = styledCharsWidth.split(" ")
        }

        val output = StringBuilder()
        for (char in input) {
            val index = normalChars.indexOf(char.toString())
            if (index != -1) {
                output.append(styledCars[index])
            } else {
                output.append(char)
            }
        }
        return output.toString()
    }

}