package net.ccbluex.liquidbounce.features.module.modules.combat.antikbs.vanilla

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.antikbs.AntiKBMode
import net.minecraft.network.play.server.SPacketEntityVelocity

class CancelVelocity : AntiKBMode("Cancel") {
    override fun onPacket(event: PacketEvent) {
        if(event.packet is SPacketEntityVelocity) event.cancelEvent()
    }
}
