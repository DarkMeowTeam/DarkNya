package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.ListValue
import org.lwjgl.input.Keyboard

/**
 *
 * By CatX_feitu
 *
 */
@ModuleInfo(name = "ScaffoldModeChanger", description = "Quick change Scaffold SameY",category = ModuleCategory.WORLD)
class ScaffoldModeChanger : Module() {

    private val modeValue = ListValue("Mode", arrayOf("toggle","key"), "key")
    private val onScaffoldHelper = BoolValue("OnScaffoldHelper", false)

    override fun onEnable() {
        if (modeValue.get() == "toggle") {
            if (!onScaffoldHelper.get()) (DarkNya.moduleManager[Scaffold::class.java] as Scaffold).sameYValue.set(!(DarkNya.moduleManager[Scaffold::class.java] as Scaffold).sameYValue.get())
        }
    }

    override fun onDisable() {
        onEnable()
    }
    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if (!DarkNya.moduleManager[Scaffold::class.java].state) return

        if (onScaffoldHelper.get()) return
        if (modeValue.get() == "key") (DarkNya.moduleManager[Scaffold::class.java] as Scaffold).sameYValue.set(!Keyboard.isKeyDown(Keyboard.KEY_SPACE))
    }
}
