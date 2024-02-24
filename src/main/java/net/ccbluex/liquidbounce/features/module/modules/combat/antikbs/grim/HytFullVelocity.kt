package net.ccbluex.liquidbounce.features.module.modules.combat.antikbs.grim

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.antikbs.AntiKBMode
import net.minecraft.network.play.client.CPacketConfirmTransaction
import net.minecraft.network.play.server.SPacketEntityVelocity
/*
* 花雨庭全反
* 真的能全反
* 但是只能用于娱乐用途
*
* @author CatX_feitu
 */
class HytFullVelocity : AntiKBMode("HytFull") {
    override fun onPacket(event: PacketEvent) {
        if (event.packet is SPacketEntityVelocity) event.cancelEvent()
        if (event.packet is CPacketConfirmTransaction) event.cancelEvent()

    }
}
