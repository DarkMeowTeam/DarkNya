package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.client.resources.I18n
import net.minecraft.util.text.ITextComponent

@ModuleInfo(name = "SilentDisconnect", description = "静默断连", category = ModuleCategory.CLIENT)
class SilentDisconnect : Module() {
    private val debugValue = BoolValue("Debug", true)

    /*
    核心代码在 MixinNetHandlerPlayClient 里
     */
    companion object {
        @JvmStatic
        fun onDisconnect(reason: ITextComponent) {
            if ((DarkNya.moduleManager[SilentDisconnect::class.java] as SilentDisconnect).debugValue.get()) DebugManage.info(
                I18n.format("disconnect.lost") + ": "+ reason.formattedText
            )
        }
    }
}