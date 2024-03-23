package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.network.play.server.SPacketChat
import java.util.regex.Pattern

/**
 * Skid or Made By CatX_feitu
 * @date 2024/2/15 15:14
 * @author WaWa
 */
@ModuleInfo(name = "SelfBanChecker", category =ModuleCategory.MISC, description = "HytSelf ban checker")
class SelfBanChecker : Module() {
    private val server = ListValue("Server",arrayOf("Hyt"),"Hyt")
    private val actionSendMessage = BoolValue("SendMessage",false)
    private val actionSendMessageContent = TextValue("SendMessageContent","")
    private val actionSendQuittingDisconnectingPacket = BoolValue("sendQuittingDisconnectingPacket",false)
    private val actionDisableKillauraBlink = BoolValue("DisableKillauraBlink",true)
    companion object {
        @JvmStatic
        var banCount = 0
    }

    override fun onEnable() {
        banCount = 0
    }


    @EventTarget
    fun onPacket(event : PacketEvent){
        val packet = event.packet
        if(packet is SPacketChat){
            if (server.get() == "Hyt") {
                val matcher =
                    Pattern.compile("玩家${mc.player?.name}在本局游戏中行为异常").matcher(packet.chatComponent.unformattedText)
                if (matcher.find()) {
                    onBan()
                }
            }
        }
    }

    private fun onBan() {
        if (actionSendMessage.get() && actionSendMessageContent.get() != "") mc.player?.sendChatMessage(actionSendMessageContent.get())
        if (actionSendQuittingDisconnectingPacket.get()) mc.world?.sendQuittingDisconnectingPacket()
        if (actionDisableKillauraBlink.get()) (DarkNya.moduleManager[Blink::class.java]).state = false ; (DarkNya.moduleManager[KillAura::class.java]).state = false
        banCount ++
    }

    override val tag: String
        get() = banCount.toString()
}