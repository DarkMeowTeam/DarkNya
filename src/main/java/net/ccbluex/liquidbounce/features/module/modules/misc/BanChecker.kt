package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.features.value.TextValue
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.minecraft.network.play.server.SPacketChat
import java.util.regex.Pattern

@ModuleInfo(name = "BanChecker", description = "Test ban player(s) and autoL", category = ModuleCategory.MISC)
class BanChecker : Module(){
    private val banMessage = TextValue("BanMessage","玩家(.*?)在本局游戏中行为异常")
    private val autoL = BoolValue("AutoL",false)

    var ban = 0
    @EventTarget
    fun onPacket(event : PacketEvent){
        val packet = event.packet
        if(packet is SPacketChat){
            val matcher = Pattern.compile(banMessage.get()).matcher(packet.chatComponent.unformattedText)
            if(matcher.find()){
                ban ++
                val banned = matcher.group(1)
                if (banned == "") return
                if (autoL.get()) mc.player!!.sendChatMessage("@ $banned  主播你怎么ban了啊 这都ban了" + ban + "个人了 ")

                DarkNya.hud.addNotification(Notification("BanChecker","$banned was banned. (banned:$ban)",NotifyType.INFO, animeTime = 1000))
            }
        }
    }
    override val tag: String
        get() = ban.toString()
}