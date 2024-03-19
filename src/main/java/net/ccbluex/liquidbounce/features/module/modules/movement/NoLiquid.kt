package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.DebugManage
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.minecraft.block.BlockLiquid
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumFacing

@ModuleInfo(name = "NoLiquid", description = "Remove water&lava", category = ModuleCategory.MOVEMENT)
class NoLiquid : Module() {
    private val waterValue = BoolValue("Water", true)
    private val lavaValue = BoolValue("Lava", true)
    private val modeValue = ListValue("Mode", arrayOf("Client", "GrimAC"), "Client")
    private val rangeValue = IntegerValue("DisLiquidRange", 5, 0, 6)
    private val grimLessPacketValue = BoolValue("LessPacket", true).displayable { modeValue.get().toLowerCase() == "grimac" }
    private val debugValue = BoolValue("Debug", true)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {

        val searchBlocks = BlockUtils.searchBlocks(rangeValue.get())

        for (block in searchBlocks){
            val blockPos = block.key

            if (
                    (waterValue.get() && (block.value is BlockLiquid)) ||
                    (lavaValue.get() && block.value == Blocks.LAVA)
                )
            {
                if (debugValue.get()) DebugManage.info("NoLiquid -> ${blockPos.x} ${blockPos.y} ${blockPos.z}")

                when (modeValue.get().toLowerCase()) {
                    /* 别忘了在LiquidBounce_at.cfg这个文件里面加上这句
                        public net.minecraft.entity.Entity field_70171_ac #inWater
                    */
                    "client" -> {
                        mc.world.setBlockToAir(blockPos)
                        mc.player.inWater = false
                    }
                    "grimac" -> {
                        if (grimLessPacketValue.get()) {
                            // 玩家下方方块忽略
                            if (blockPos.y < mc.player.posY - 1) return
                        }
                        mc.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,blockPos, EnumFacing.DOWN))

                        mc.world.setBlockToAir(blockPos)
                        mc.player.inWater = false
                    }
                }
            }
        }
    }


    override val tag: String
        get() = modeValue.get()
}
