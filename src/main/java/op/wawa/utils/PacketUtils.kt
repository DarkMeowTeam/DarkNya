package op.wawa.utils

import me.utils.PacketUtils
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraft.block.Block
import net.minecraft.item.*
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumHand

object PacketUtils : MinecraftInstance() {
    @JvmStatic
    fun sendPacket(packet: Packet<INetHandlerPlayServer>) {
        //packets.add(packet)
        //mc.netHandler.addToSendQueue(packet as IPacket)
        mc.connection!!.sendPacket(packet)//我草
    }
    @JvmStatic
    fun sendPacketNoEvent(packet: Packet<INetHandlerPlayServer>) {
        if (mc.connection == null) return

        PacketUtils.isPacketSend = true
        mc.connection!!.sendPacket(packet)
        PacketUtils.isPacketSend = false
    }
    @JvmStatic
    fun sendTryUseItem() {
        mc.connection!!.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
        mc.connection!!.sendPacket(CPacketPlayerTryUseItem(EnumHand.OFF_HAND))
    }
    @JvmStatic
    fun cancelC08(event: PacketEvent, packet: Packet<*>) {
        if (packet is CPacketPlayerTryUseItemOnBlock
            && (mc.player!!.heldItemMainhand.item is ItemSword || mc.player!!.heldItemMainhand.item is ItemFood ||
                    mc.player!!.heldItemMainhand.item is ItemPotion || mc.player.heldItemMainhand.item is ItemAppleGold ||
                    mc.player.heldItemMainhand.item is ItemBucketMilk)) {
            if (!listOf(54, 146, 61, 62).contains(Block.getIdFromBlock(mc.world!!.getBlockState(mc.objectMouseOver.blockPos).block)) && mc.objectMouseOver != null) {
                if (packet.facingX != 0F || packet.facingY != 0F || packet.facingZ != 0F || packet.pos.x != -1 || packet.pos.y != -1 || packet.pos.z != -1) event.cancelEvent()
            }
        }
    }
}