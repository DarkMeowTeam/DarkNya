package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.DebugManage
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.server.SPacketEntityStatus
import org.lwjgl.input.Keyboard
import java.awt.Color

@ModuleInfo(name = "Fly", category = ModuleCategory.MOVEMENT, keyBind = Keyboard.KEY_F)
object Fly : Module() {

    private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.flys", FlyMode::class.java)
        .takeIf { it.isNotEmpty() }
        ?.map { it.newInstance() as FlyMode }
        ?.sortedBy { it.modeName }
        ?: listOf()

    private val mode: FlyMode
        get() = modes.find { it.modeName == modeValue.get() } ?: modes[0] // 不能throw 不然会出现神秘报错

    private val modeValue: ListValue = object : ListValue("Mode", modes.map { it.modeName }.toTypedArray(), "Vanilla") {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    private val motionResetValue = BoolValue("MotionReset", false)


    // Visuals
    private val markValue = ListValue("Mark", arrayOf("Up", "Down", "Off"), "Up")
    private val fakeDamageValue = BoolValue("FakeDamage", false)
    private val viewBobbingValue = BoolValue("ViewBobbing", false)
    private val viewBobbingYawValue = FloatValue("ViewBobbingYaw", 0.1f, 0f, 0.5f).displayable { viewBobbingValue.get() }

    var launchX = 0.0
    var launchY = 0.0
    var launchZ = 0.0
    var launchYaw = 0f
    var launchPitch = 0f

    var antiDesync = false

    var needReset = true

    override fun onEnable() {
        val player = mc.player ?: return

        antiDesync = false
        needReset = true
        if (player.onGround && fakeDamageValue.get()) {
            val event = PacketEvent(SPacketEntityStatus(mc.player, 2.toByte()))
            DarkNya.eventManager.callEvent(event)
            if (!event.isCancelled) {
                player.handleStatusUpdate(2.toByte())
            }
        }

        launchX = player.posX
        launchY = player.posY
        launchZ = player.posZ
        launchYaw = player.rotationYaw
        launchPitch = player.rotationPitch

        if (mode == null) {
            DebugManage.warn(modes.size.toString())
            return
        }

        mode.onEnable()
    }

    override fun onDisable() {
        val player = mc.player ?: return
        val timer = mc.timer as IMixinTimer

        antiDesync = false
        player.capabilities.isFlying = false
        player.capabilities.flySpeed = 0.05f
        player.noClip = false

        timer.timerSpeed = 1F
        player.speedInAir = 0.02F

        if (motionResetValue.get() && needReset) MovementUtils.resetMotion(true)

        mode.onDisable()
    }

    @EventTarget
    fun onRender3d(event: Render3DEvent) {
        val player = mc.player ?: return

        if (markValue.equals("Off")) {
            return
        }

        RenderUtils.drawPlatform(
            if (markValue.equals("Up")) launchY + 2.0 else launchY,
            if (player.entityBoundingBox.maxY < launchY + 2.0) Color(0, 255, 0, 90) else Color(255, 0, 0, 90),
            1.0)
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mode.onUpdate(event)
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val player = mc.player ?: return

        if(viewBobbingValue.get()) {
            player.cameraYaw = viewBobbingYawValue.get()
            player.prevCameraYaw = viewBobbingYawValue.get()
        }
        mode.onMotion(event)
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        mode.onPacket(event)
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        mode.onWorld(event)
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        mode.onMove(event)
    }

    @EventTarget
    fun onBlockBB(event: BlockBBEvent) {
        mode.onBlockBB(event)
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        mode.onJump(event)
    }

    @EventTarget
    fun onStep(event: StepEvent) {
        mode.onStep(event)
    }

    override val tag: String
        get() = modeValue.get()

    /**
     * 读取mode中的value并和本体中的value合并
     * 所有的value必须在这个之前初始化
     */
    override val values = super.values.toMutableList().also {
        modes.map {
            mode -> mode.values.forEach { value ->
                it.add(value.displayable {  mode.modeName == modeValue.get() })
            }
        }
    }
}
