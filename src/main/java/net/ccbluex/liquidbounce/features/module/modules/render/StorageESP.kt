package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.world.ChestAura.clickedBlocks
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.item.EntityMinecartChest
import net.minecraft.tileentity.*
import java.awt.Color

@ModuleInfo(name = "StorageESP", description = "Allows you to see chests, dispensers, etc. through walls.", category = ModuleCategory.RENDER)
class StorageESP : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Box", "OtherBox", "2D"), "Box")
    private val chestValue = BoolValue("Chest", true)
    private val enderChestValue = BoolValue("EnderChest", true)
    private val furnaceValue = BoolValue("Furnace", true)
    private val dispenserValue = BoolValue("Dispenser", true)
    private val hopperValue = BoolValue("Hopper", true)
    private val shulkerBoxValue = BoolValue("ShulkerBox", true)
    private val noteBoxValue = BoolValue("NoteBox", true)
    private val bedValue = BoolValue("Bed", true)

    private fun getColor(tileEntity: TileEntity):Color?{
        return when {
            chestValue.get() && tileEntity is TileEntityChest && !clickedBlocks.contains(tileEntity.pos) -> Color.YELLOW
            enderChestValue.get() && tileEntity is TileEntityEnderChest && !clickedBlocks.contains(tileEntity.pos) -> Color.MAGENTA
            furnaceValue.get() && tileEntity is TileEntityFurnace -> Color.BLACK
            dispenserValue.get() && tileEntity is TileEntityDispenser -> Color.BLACK
            hopperValue.get() && tileEntity is TileEntityHopper -> Color.GRAY
            shulkerBoxValue.get() && tileEntity is TileEntityShulkerBox -> Color.PINK
            noteBoxValue.get() && tileEntity is TileEntityNote -> Color.orange
            bedValue.get() && tileEntity is TileEntityBed -> Color.red
            else -> null
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        try {
            val mode = modeValue.get()

            val gamma = mc.gameSettings.gammaSetting

            mc.gameSettings.gammaSetting = 100000.0f

            for (tileEntity in mc.world!!.loadedTileEntityList) {
                val color: Color = getColor(tileEntity)?: continue

                if (!(tileEntity is TileEntityChest || tileEntity is TileEntityEnderChest)) {
                    RenderUtils.drawBlockBox(tileEntity.pos, color, !mode.equals("otherbox", ignoreCase = true))
                    continue
                }
                when (mode.toLowerCase()) {
                    "otherbox", "box" -> RenderUtils.drawBlockBox(tileEntity.pos, color, !mode.equals("otherbox", ignoreCase = true))
                    "2d" -> RenderUtils.draw2D(tileEntity.pos, color.rgb, Color.BLACK.rgb)
                }
            }
            for (entity in mc.world!!.loadedEntityList) {
                if (entity is EntityMinecartChest) {
                    when (mode.toLowerCase()) {
                        "otherbox", "box" -> RenderUtils.drawEntityBox(entity, Color(0, 66, 255), !mode.equals("otherbox", ignoreCase = true))
                        "2d" -> RenderUtils.draw2D(entity.position, Color(0, 66, 255).rgb, Color.BLACK.rgb)
                    }
                }
            }
            RenderUtils.glColor(Color(255, 255, 255, 255))
            mc.gameSettings.gammaSetting = gamma
        } catch (ignored: Exception) {
        }
    }
}