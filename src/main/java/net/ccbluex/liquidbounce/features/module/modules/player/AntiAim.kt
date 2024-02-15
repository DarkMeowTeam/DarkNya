package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.ListValue

@ModuleInfo(name = "AntiAim",description = "狂笑的蛇将写散文", category = ModuleCategory.PLAYER)
class AntiAim : Module() {
    private val yawModeValue = ListValue("YawMove", arrayOf("Jitter","normal","dodge","tilt","step","blink", "Spin", "Back", "BackJitter"), "Spin")
    private val pitchModeValue = ListValue("PitchMode", arrayOf("Down", "Up", "Jitter", "AnotherJitter"), "Down")
    private val rotateValue = BoolValue("SilentRotate", true)
    // 头部和身体的旋转角度、偏移量、倾斜角度、踏步偏移量、眨眼偏移量
    private var headYaw = 0f
    private var bodyYaw = 0f
    private var headOffset = 0f
    private var bodyOffset = 0f
    private var headTilt = 0f
    private var bodyTilt = 0f
    private var headStep = 0f
    private var bodyStep = 0f
    private var headBlink = 0f
    private var bodyBlink = 0f

    // 上次更新头部和身体状态的时间戳、间隔时间
    private var lastHeadUpdate = 0L
    private var lastBodyUpdate = 0L
    private var headTimer = 200L
    private var bodyTimer = 400L
    private var yaw = 0f
    private var pitch = 0f
    private fun updateHeadYaw() {
        if (System.currentTimeMillis() - lastHeadUpdate >= headTimer) {
            // 如果时间到了，改变 yaw 值
            headYaw += 10f
            if (headYaw > 180f) {
                headYaw = -180f
            } else if (headYaw < -180f) {
                headYaw = 180f
            }
            lastHeadUpdate = System.currentTimeMillis() // 更新时间戳
        }
    }

    private fun updateBodyYaw() {
        if (System.currentTimeMillis() - lastBodyUpdate >= bodyTimer) {
            // 如果时间到了，改变 yaw 值
            bodyYaw += 20f
            if (bodyYaw > 180f) {
                bodyYaw = -180f
            } else if (bodyYaw < -180f) {
                bodyYaw = 180f
            }
            lastBodyUpdate = System.currentTimeMillis() // 更新时间戳
        }
    }

    private fun updateHeadOffset() {
        if (System.currentTimeMillis() - lastHeadUpdate >= headTimer) {
            // 如果时间到了，改变偏移量
            headOffset = if (headOffset == 0f) 10f else 0f
            lastHeadUpdate = System.currentTimeMillis() // 更新时间戳
        }
    }

    private fun updateBodyOffset() {
        if (System.currentTimeMillis() - lastBodyUpdate >= bodyTimer) {
            // 如果时间到了，改变偏移量
            bodyOffset = if (bodyOffset == 0f) 20f else 0f
            lastBodyUpdate = System.currentTimeMillis() // 更新时间戳
        }
    }

    private fun updateHeadTilt() {
        if (System.currentTimeMillis() - lastHeadUpdate >= headTimer) {
            // 如果时间到了，改变倾斜角度
            headTilt = if (headTilt == 0f) 10f else -10f
            lastHeadUpdate = System.currentTimeMillis() // 更新时间戳
        }
    }

    private fun updateBodyTilt() {
        if (System.currentTimeMillis() - lastBodyUpdate >= bodyTimer) {
            // 如果时间到了，改变倾斜角度
            bodyTilt = if (bodyTilt == 0f) 5f else -5f
            lastBodyUpdate = System.currentTimeMillis() // 更新时间戳
        }
    }

    private fun updateHeadStep() {
        if (System.currentTimeMillis() - lastHeadUpdate >= headTimer) {
            // 如果时间到了，改变偏移量
            headStep = if (headStep == 0f) 10f else -10f
            lastHeadUpdate = System.currentTimeMillis() // 更新时间戳
        }
    }

    private fun updateBodyStep() {
        if (System.currentTimeMillis() - lastBodyUpdate >= bodyTimer) {
            // 如果时间到了，改变偏移量
            bodyStep = if (bodyStep == 0f) 5f else -5f
            lastBodyUpdate = System.currentTimeMillis() // 更新时间戳
        }
    }

    private fun updateHeadBlink() {
        if (System.currentTimeMillis() - lastHeadUpdate >= headTimer) {
            // 如果时间到了，改变偏移量
            headBlink = if (headBlink == 0f) 25f else -25f
            lastHeadUpdate = System.currentTimeMillis() // 更新时间戳
        }
    }

    private fun updateBodyBlink() {
        if (System.currentTimeMillis() - lastBodyUpdate >= bodyTimer) {
            // 如果时间到了，改变偏移量
            bodyBlink = if (bodyBlink == 0f) 15f else -15f
            lastBodyUpdate = System.currentTimeMillis() // 更新时间戳
        }
    }
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        when (yawModeValue.get().toLowerCase()) {
            "spin" -> {
                yaw += 20.0f
                if (yaw > 180.0f) {
                    yaw = -180.0f
                } else if (yaw < -180.0f) {
                    yaw = 180.0f
                }
            }
            "jitter" -> {
                yaw = mc.player!!.rotationYaw + if (mc.player!!.ticksExisted % 2 == 0) 90F else -90F
            }
            "back" -> {
                yaw = mc.player!!.rotationYaw + 180f
            }
            "backjitter" -> {
                yaw = mc.player!!.rotationYaw + 180f + RandomUtils.nextDouble(-3.0, 3.0).toFloat()
            }
        }
        when (yawModeValue.get().toLowerCase()) {
            "normal" -> {
                // 正常模式下不改变 yaw 和 pitch 值
            }
            "rotate" -> {
                updateHeadYaw()
                updateBodyYaw()
                // 在原有的 yaw 值上加上头部 yaw 值和身体 yaw 值，实现旋转效果
                yaw = mc.player!!.rotationYaw + headYaw + bodyYaw
            }
            "dodge" -> {
                updateHeadOffset()
                updateBodyOffset()
                // 在原有的 yaw 值上加上头部偏移量和身体偏移量，实现闪避效果
                yaw = mc.player!!.rotationYaw + headOffset + bodyOffset
            }
            "tilt" -> {
                updateHeadTilt()
                updateBodyTilt()
                // 在原有的 pitch 值上加上头部倾斜角度和身体倾斜角度，实现倾斜效果
                pitch = mc.player!!.rotationPitch + headTilt + bodyTilt
            }
            "step" -> {
                updateHeadStep()
                updateBodyStep()
                // 在原有的 yaw 值上加上头部偏移量和身体偏移量，实现踏步效果
                yaw = mc.player!!.rotationYaw + headStep + bodyStep
            }
            "blink" -> {
                updateHeadBlink()
                updateBodyBlink()
                // 在原有的 pitch 值上加上头部偏移量和身体偏移量，实现眨眼效果
                pitch = mc.player!!.rotationPitch + headBlink + bodyBlink
            }
            else -> {
                // 如果输入的模式名不是以上五种，则默认为正常模式。
            }
        }
        when (pitchModeValue.get().toLowerCase()) {
            "up" -> {
                pitch = -90.0f
            }
            "down" -> {
                pitch = 90.0f
            }
            "anotherjitter" -> {
                pitch = 60f + RandomUtils.nextDouble(-3.0, 3.0).toFloat()
            }
            "jitter" -> {
                pitch += 30.0f
                if (pitch > 90.0f) {
                    pitch = -90.0f
                } else if (pitch < -90.0f) {
                    pitch = 90.0f
                }
            }
        }

        if (rotateValue.get()) {
            RotationUtils.setTargetRotation(Rotation(yaw, pitch))
        } else {
            mc.player!!.rotationYaw = yaw
            mc.player!!.rotationPitch = pitch
        }
    }
}