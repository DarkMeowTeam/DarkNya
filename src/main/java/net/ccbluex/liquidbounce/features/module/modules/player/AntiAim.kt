package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.client.CPacketUseEntity
import org.lwjgl.input.Mouse

@ModuleInfo(name = "AntiAim", description = "Like COUNTER-STRIKE Cheats.", category = ModuleCategory.PLAYER)
class AntiAim : Module() {
    private val targetyaw = ListValue("TargetYaw", arrayOf("Local", "Target(未完成)", "OnMove", "Static"), "Local")
    private val pitchMode = ListValue("PitchMode", arrayOf("Change", "Random", "Static", "None"), "Change")
    private val changepitch = FloatValue("ChangePitch", 90.0f, 0.0f, 90.0f)
    private val randompitch = FloatValue("RandomPitch", 90.0f, 45.0f, 90.0f)
    private val staticpitch = FloatValue("StaticPitch", 0.0f, -90.0f, 90.0f)
    private val jitterpitch = FloatValue("JitterPitch", 10.0f, 0.0f, 90.0f)
    private val yawMode = ListValue("YawMove", arrayOf("Spin", "Jitter", "Random", "Static", "None"), "Spin")
    private val spinspeed = FloatValue("SpinSpeed", 40.0f, 0.0f, 180.0f)
    private val jitteryaw = FloatValue("JitterYaw", 90.0f, 0.0f, 180.0f)
    private val randomfov = FloatValue("RandomFov", 180.0f, 0.0f, 180.0f)
    private val staticyaw = FloatValue("StaticYaw", 0.0f, -180.0f, 180.0f)
    private val strafeValue = BoolValue("RotationStrafe", false)
    private val noMoveValue = BoolValue("NoMove", true)
    private val allowSneakValue = BoolValue("AllowSneak", false)
    private val clickBypassValue = BoolValue("CancelClick", true)
    private val noFireValue = BoolValue("NoFire", true)
    private val lavaValue = BoolValue("NoLava", true)
    private val waterValue = BoolValue("NoWater", true)
    private val webValue = BoolValue("NoWeb", true)
    private val climbValue = BoolValue("NoClimb", true)
    private val rotation = ListValue("RotMode", arrayOf("Client", "Packet", "Legit", "Fake"), "Client")

    private var pyaw = 0f
    private var yaw = 0f
    private var pitch = 0f
    /*private var lastPlayer: Entity? = null
    val player = mc.thePlayer
    private val otherPlayer = lastPlayer

    val deltaX = otherPlayer!!.posX - player.posX
    val deltaZ = otherPlayer!!.posZ - player.posZ
    val yawToEntity = Math.toDegrees(atan2(deltaZ, deltaX)).toFloat()
    val playerYaw = player.rotationYaw*/

    override fun onEnable() {
        if (targetyaw.get() == "static") pyaw = mc.player!!.rotationYaw
    }

    override fun onDisable() {
    }
    //AntiAim by 口服液(1912367443)
    //Like COUNTER-STRIKE Cheats XD
    //口服液御用陀螺

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return
        //安全模块
        if (clickBypassValue.get() && Mouse.isButtonDown(0) || clickBypassValue.get() && Mouse.isButtonDown(1) || clickBypassValue.get() && Mouse.isButtonDown(2) || clickBypassValue.get() && Mouse.isButtonDown(3
            ) || clickBypassValue.get() && Mouse.isButtonDown(4) || clickBypassValue.get() && Mouse.isButtonDown(5) || clickBypassValue.get() && Mouse.isButtonDown(6) || player.isInLava && lavaValue.get() || player.isInWater && waterValue.get() || player.isInWeb && webValue.get() ||
            player.isOnLadder && climbValue.get() || player.isBurning && noFireValue.get()
        ) {
            return
        }
        if (MovementUtils.isMoving && noMoveValue.get()) {
            if (allowSneakValue.get() && !player.isSneaking) {
                return
            } else {
                if (!allowSneakValue.get()) {
                    return
                }
            }
        }
        when (targetyaw.get().toLowerCase()) {
            "local" -> pyaw = player.rotationYaw
            "target(未完成)" -> {
                /*if (otherPlayer == null) {
                    pyaw = player.rotationYaw
                }
                var relativeYaw = yawToEntity - playerYaw
                while (relativeYaw <= -180.0f) relativeYaw += 360.0f
                while (relativeYaw > 180.0f) relativeYaw -= 360.0f

                pyaw = relativeYaw*/
            }
            "onmove" -> if (MovementUtils.isMoving) pyaw = player.rotationYaw
        }
        when (yawMode.get().toLowerCase()) {
            "spin" -> {
                yaw += spinspeed.get()
                if (yaw > 180.0f) {
                    yaw = -180.0f
                } else if (yaw < -180.0f) {
                    yaw = 180.0f
                }//陀螺
            }

            "jitter" -> yaw =
                pyaw + if (player.ticksExisted % 2 == 0) jitteryaw.get() else -jitteryaw.get() //颤抖
            "random" -> yaw = RandomUtils.nextFloat(-randomfov.get(), randomfov.get()).toFloat() //颤抖
            "static" -> yaw = pyaw + staticyaw.get() + 180f //静态
            "none" -> yaw = pyaw //啥也木有
        }
        when (pitchMode.get().toLowerCase()) {
            "random" -> pitch = RandomUtils.nextFloat(-randompitch.get(), randompitch.get())//随机
            "change" -> {
                pitch =
                    if (player.ticksExisted % 2 == 0) changepitch.get() else -changepitch.get()//直升机
            }

            "static" -> {
                pitch = staticpitch.get() + RandomUtils.nextFloat(
                    -jitterpitch.get(),
                    jitterpitch.get()
                )//自定义
            }

            "none" -> {
                pitch = player.rotationPitch//啥也木有
            }
        }
        when (rotation.get().toLowerCase()) {
            "client" -> RotationUtils.setTargetRotation(Rotation(yaw, pitch))
            "packet" -> {
                RotationUtils.serverRotation.yaw = yaw
                RotationUtils.serverRotation.pitch = pitch
            }
            "legit" -> {
                player.rotationYaw = yaw
                player.rotationPitch = pitch
            }
        }
    }
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val player = mc.player ?: return
        if (rotation.get() == "fake") {
            player.rotationYawHead = yaw
            player.renderYawOffset = yaw
        }
    }
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (clickBypassValue.get() && (packet is CPacketUseEntity || packet is CPacketPlayerTryUseItemOnBlock)) {
            return
        }
    }

    override val tag: String
        get() = "Yaw - " + yawMode.get() + " Pitch - " + pitchMode.get()
}

