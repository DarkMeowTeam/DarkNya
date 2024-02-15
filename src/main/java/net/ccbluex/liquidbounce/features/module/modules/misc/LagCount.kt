package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.minecraft.network.play.server.SPacketChat
import net.minecraft.network.play.server.SPacketPlayerPosLook

@ModuleInfo(name = "LagCount", category =ModuleCategory.MISC, description = "LagCount")
class LagCount : Module() {
    private val server = ListValue("Server",arrayOf("Hyt"),"Hyt")

    private val autoHub = BoolValue("AutoHub", true)
    private val autoHubFlagValue = IntegerValue("AutoHubFlagValue", 10, 1, 100)
    private val debug = BoolValue("Debug", true)
    private var a = 0
    private var b = 0
    private var c = 0

    override fun onEnable() {
        a = 0
        b = 0
        c = 0
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        // Lag ++
        if ((packet is SPacketPlayerPosLook) && b >= 20 && !DarkNya.moduleManager[Blink::class.java].state && c <= 8){
            a += 1
            if (debug.get()) ClientUtils.displayChatMessage("§d${DarkNya.CLIENT_NAME} §8>> §cFlag:$a")
        }

        // Lag 清除处理
        val chatText = if(packet is SPacketChat) packet.chatComponent.unformattedText else return
        if (server.get() == "Hyt") {
            if (!chatText.contains(":") && (
                        chatText.startsWith("起床战争") ||
                                chatText.startsWith("[起床战争") ||
                                chatText.startsWith("花雨庭")
                        )
            ) {
                if (
                    (
                            chatText.contains("游戏开始") ||
                                    chatText.contains("恭喜") ||
                                    chatText.contains("加入了游戏")
                            ) && !(a == 0 && b == 0) // 优化:Flag = 0 总不能反复清除吧
                ) {
                    b = 0
                    a = 0
                    if (debug.get()) ClientUtils.displayChatMessage("§d${DarkNya.CLIENT_NAME} §8>> §aFlag 重置")
                }
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        b += 1
        if (!mc.player!!.onGround) c += 1
        if (mc.player!!.onGround) c = 0

        if (autoHub.get() && a == autoHubFlagValue.get()){
            mc.player!!.sendChatMessage("/hub")
            a = 0
            if (debug.get()) ClientUtils.displayChatMessage("§d${DarkNya.CLIENT_NAME} §8>> §eFlag 到达上限")
        }

        if (mc.player!!.health <= 0f || mc.player!!.isDead ) {
            b = -100
        }
    }
}