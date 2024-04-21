package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos

@ModuleInfo(name = "InBlock", description = "InBlock! Bypass GrimAC!", category = ModuleCategory.MOVEMENT)
class InBlock : Module() {
    private val xValue = IntegerValue("X",1,1,5)
    private val zValue = IntegerValue("Z",1,1,5)
    private val yUpValue = IntegerValue("YUp",1,1,3)
    private val yDownValue = IntegerValue("YDown",0,0,3)
    private val checkAirValue = BoolValue("CheckAir",true)
    private val modeValue = ListValue("Mode", arrayOf("Client", "GrimAC"), "GrimAC")
    private val cancelSprintValue = BoolValue("CancelSprint",true)
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return
        val world = mc.world ?: return

        val breaks : MutableList<BlockPos> = mutableListOf()

        for(x in -xValue.get()..xValue.get()) {
            for(y in yDownValue.get()..yUpValue.get()) {
                for(z in -zValue.get()..zValue.get()) {
                    val pos = BlockPos(player.posX.toInt() + x, player.posY.toInt() + y, player.posZ.toInt() + z)
                    breaks.add(pos)
                }
            }
        }
        // 删除空气方块 (减少发包 实际上开不开都不会被GrimAc检测)
        if (checkAirValue.get()) {
            breaks.removeIf { pos -> world.isAirBlock(pos) }
        }
        // 是否需要激活
        if (breaks.size == 0) return

        if (cancelSprintValue.get()) player.isSprinting = false

        // 从客户端层删除方块
        for (pos in breaks) {
            when (modeValue.get().toLowerCase()) {
                "client" -> mc.world.setBlockToAir(pos)
                "grimac" -> {
                    mc.connection!!.sendPacket(
                        CPacketPlayerDigging(
                            CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                            pos,
                            EnumFacing.DOWN
                        )
                    )
                    mc.world.setBlockToAir(pos)
                }
            }
        }
    }
}
