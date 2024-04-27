package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue

@ModuleInfo(name = "NoRotateSet", description = "Prevents the server from rotating your head.", category = ModuleCategory.MISC)
object NoRotateSet : Module() {

    val noLoadingValue = BoolValue("NoLoading", true)
    val overwriteTeleportValue = BoolValue("SilentConfirm", true)
    val rotateValue = BoolValue("SilentConfirmSetRotation", true).displayable { overwriteTeleportValue.get() }
}