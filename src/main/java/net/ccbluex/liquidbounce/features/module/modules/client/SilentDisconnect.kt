package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.minecraft.network.play.server.SPacketDisconnect

@ModuleInfo(name = "SilentDisconnect", description = "静默断连", category = ModuleCategory.CLIENT)
class SilentDisconnect : Module() {
    private val debugValue = BoolValue("Debug", true)

    /*
    核心代码在 MixinNetHandlerPlayClient 里

    @Inject(method={"onDisconnect"}, at={@At(value="HEAD")}, cancellable=true)
    private void onDisconnect(ITextComponent reason, CallbackInfo callbackInfo) {
        if (DarkNya.moduleManager.getModule(SilentDisconnect.class).getState()) {
            if (((BoolValue) Objects.requireNonNull(DarkNya.moduleManager.getModule(SilentDisconnect.class).getValue("Debug"))).get()) DebugManage.info(reason.toString());
            callbackInfo.cancel();
        }
    }

     */
}