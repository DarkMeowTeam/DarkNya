/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.minecraft.block.BlockLiquid
import net.minecraft.block.BlockWeb
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumFacing


@ModuleInfo(name = "GrimNoWeb", description = "bypass Grimac", category = ModuleCategory.MOVEMENT)
class GrimNoWeb : Module() {
//private val range = FloatValue("range",0f,0f,10f)
//private var blockpos2 = BlockPos(0,0,0)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {

        val searchBlocks = BlockUtils.searchBlocks(4)

        for (block in searchBlocks){
            val blockpos = block.key
            val blocks = block.value
            if(blocks is BlockWeb){
                mc.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,blockpos, EnumFacing.DOWN))
                mc.player.isInWeb = false


            }
            if(blocks is BlockLiquid){
                mc.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,blockpos, EnumFacing.DOWN))
                mc.player.inWater = false
            }
        }

        if (mc.player.isOnLadder && mc.gameSettings.keyBindJump.isKeyDown) {
            if (mc.player.motionY >= 0.0) {
                mc.player.motionY = 0.1786
            }
        }
    }
    @EventTarget
    fun onRender3D (event:Render3DEvent){
       // if(blockpos2 != BlockPos(0,0,0))
      //  RenderUtils.drawBlockBox(blockpos2.wrap(),Color(255,0,0),true)
    }
}