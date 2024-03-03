package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.*
import net.minecraft.network.play.server.SPacketTimeUpdate
import net.minecraft.network.play.server.SPacketChangeGameState

@ModuleInfo(name = "Ambience", description = "修改本地客户端时间和天气", category = ModuleCategory.RENDER)
object Ambience : Module() {

    private val timeModeValue = ListValue("TimeMode", arrayOf("None", "Normal", "Custom"), "Custom")

    private val customWorldTimeValue = IntegerValue("CustomTime", 19, 0, 24).displayable { timeModeValue.equals("Custom") }
    private val changeWorldTimeSpeedValue = IntegerValue("ChangeWorldTimeSpeed", 150, 10, 500).displayable { timeModeValue.equals("Normal") }

    private val weatherModeValue = ListValue("WeatherMode", arrayOf("None", "Sun", "Rain", "Thunder"), "None")
    private val weatherStrengthValue = FloatValue("WeatherStrength", 1f, 0f, 1f).displayable { !weatherModeValue.equals("None") }

    var i = 0L

    override fun onDisable() {
        i = 0
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        when (timeModeValue.get().toLowerCase()) {
            "normal" -> {
                if (i < 24000) {
                    i += changeWorldTimeSpeedValue.get()
                } else {
                    i = 0
                }
                mc.world.worldTime = i
            }
            "custom" -> {
                mc.world.worldTime = customWorldTimeValue.get().toLong() * 1000
            }
        }

        when (weatherModeValue.get().toLowerCase()) {
            "sun" -> {
                mc.world.setRainStrength(0f)
                mc.world.setThunderStrength(0f)
            }
            "rain" -> {
                mc.world.setRainStrength(weatherStrengthValue.get())
                mc.world.setThunderStrength(0f)
            }
            "thunder" -> {
                mc.world.setRainStrength(weatherStrengthValue.get())
                mc.world.setThunderStrength(weatherStrengthValue.get())
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (!timeModeValue.equals("none") && packet is SPacketTimeUpdate) {
            event.cancelEvent()
        }

        if (!weatherModeValue.equals("none") && packet is SPacketChangeGameState) {
            if (packet.gameState in 7..8) { // change weather packet
                event.cancelEvent()
            }
        }
    }
}