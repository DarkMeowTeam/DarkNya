package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.DebugManage
import net.minecraft.network.play.server.SPacketSpawnGlobalEntity

@ModuleInfo(name = "LightningDetect", description = "雷声检测", category = ModuleCategory.EXPLOIT)
object LightningDetect : Module() {
    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is SPacketSpawnGlobalEntity) {
            val packet = event.packet
            if (packet.type != 1) return
            DebugManage.warn("雷声(${packet.x.toInt()},${packet.y.toInt()},${packet.z.toInt()})")
        }
    }
}
