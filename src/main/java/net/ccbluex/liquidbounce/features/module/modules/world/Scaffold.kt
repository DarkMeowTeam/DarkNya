

package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.MathUtils.wrapAngleTo180_float
import net.ccbluex.liquidbounce.utils.block.BlockUtils.canBeClicked
import net.ccbluex.liquidbounce.utils.block.BlockUtils.isReplaceable
import net.ccbluex.liquidbounce.utils.block.PlaceInfo
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.block.BlockBush
import net.minecraft.client.settings.GameSettings
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.network.play.server.SPacketDisconnect
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.*

@ModuleInfo(
    name = "Scaffold",
    description = "telly",
    category = ModuleCategory.WORLD
)
class Scaffold : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Normal"), "Normal")
    var aparkour = BoolValue("AutoJump", true)
    private var sameYStatus = false



    // Delay
    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 0, 0, 1) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val minDelay = minDelayValue.get()
            if (minDelay > newValue) {
                set(minDelay)
            }
        }
    }

    private val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 0, 0, 1) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val maxDelay = maxDelayValue.get()
            if (maxDelay < newValue) {
                set(maxDelay)
            }
        }
    }
    private val falldowndelay = IntegerValue("FallDownDelay", 0, 0, 1)
    // Placeable delay
    private val placeDelay = BoolValue("PlaceDelay", true)

    // AutoBlock
    private val autoBlockValue = ListValue("AutoBlock", arrayOf("Pick", "Switch", "Switchfix"), "Pick")



    // Basic stuff
    private val sprintModeValue = ListValue("SprintMode", arrayOf("Telly"), "Telly")
    private val swingValue = BoolValue("Swing", true)
    private val searchValue = BoolValue("Search", true)
    private val downValue = BoolValue("Down", false)
    private val placeModeValue = ListValue("PlaceTiming", arrayOf("Pre"), "Pre")
    private val placeConditionValue = ListValue("PlaceCondition", arrayOf("DelayAir"), "DelayAir")
    private val RotConditionValue = ListValue("RotCondition", arrayOf("DelayAir"), "DelayAir")
    private var f = false
    private var n = false
    private var canPlace = false
    private var canRot = false
    private var airtime = 0
    private val airticks = IntegerValue("PlaceAirTime",1,0,5)
    private val Rotairticks = IntegerValue("RotAirTime",2,0,5)

    // Eagle
    private val eagleValue = ListValue("Eagle", arrayOf("Normal", "Silent", "Off"), "off")
    private val blocksToEagleValue = IntegerValue("BlocksToEagle", 0, 0, 10)
    private val edgeDistanceValue = FloatValue("EagleEdgeDistance", 0f, 0f, 0.5f)

    // Expand
    private val omniDirectionalExpand = BoolValue("OmniDirectionalExpand", false)
    private val expandLengthValue = IntegerValue("ExpandLength", 1, 1, 6)

    // Rotation Options
    private val strafeMode = ListValue("Strafe", arrayOf("Off", "AAC"), "AAC")
    private val rotationsValue = BoolValue("Rotations", true)
    private val silentRotationValue = BoolValue("SilentRotation", true)
    private val keepRotationValue = BoolValue("KeepRotation", true)
    private val keepLengthValue = IntegerValue("KeepRotationLength", 20, 20, 20)


    // XZ/Y range
    private val searchMode = ListValue("XYZSearch", arrayOf("Auto", "AutoCenter"), "AutoCenter")
    private val xzRangeValue = FloatValue("xzRange", 0.8f, 0f, 1f)
    private var yRangeValue = FloatValue("yRange", 0.8f, 0f, 1f)
    private val minDistValue = FloatValue("MinDist", 0.0f, 0.0f, 0.2f)

    // Search Accuracy
    private val searchAccuracyValue: IntegerValue = object : IntegerValue("SearchAccuracy", 4, 1, 16) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            if (maximum < newValue) {
                set(maximum)
            } else if (minimum > newValue) {
                set(minimum)
            }
        }
    }

    // Turn Speed
    private val maxTurnSpeedValue: FloatValue = object : FloatValue("MaxTurnSpeed", 180f, 1f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minTurnSpeedValue.get()
            if (v > newValue) set(v)
            if (maximum < newValue) {
                set(maximum)
            } else if (minimum > newValue) {
                set(minimum)
            }
        }
    }
    private val minTurnSpeedValue: FloatValue = object : FloatValue("MinTurnSpeed", 180f, 1f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxTurnSpeedValue.get()
            if (v < newValue) set(v)
            if (maximum < newValue) {
                set(maximum)
            } else if (minimum > newValue) {
                set(minimum)
            }
        }
    }

    // Zitter
    private val zitterMode = ListValue("Zitter", arrayOf("Off", "Teleport", "Smooth"), "Off")
    private val zitterSpeed = FloatValue("ZitterSpeed", 0.13f, 0.1f, 0.3f)
    private val zitterStrength = FloatValue("ZitterStrength", 0.05f, 0f, 0.2f)

    // Game
    private val timerValue = FloatValue("Timer", 1f, 0.1f, 10f)
    private val timeronground = BoolValue("Timeronground", false)
    private val speedModifierValue = FloatValue("SpeedModifier", 1f, 0f, 2f)
    private val slowValue = BoolValue("Slow", false)
    private val slowSpeed = FloatValue("SlowSpeed", 0.6f, 0.2f, 0.8f)

    // Safety
    private val sameYValue = ListValue("SameY", arrayOf("Off","On","PressJumpKeyOn","PressJumpKeyOff"),"Off")

    private val safeWalkValue = BoolValue("SafeWalk", false)
    private val airSafeValue = BoolValue("AirSafe", false)
    private val FallFastplace = BoolValue("Fallfastplace", false)
    private val Fastplace = BoolValue("fastplace", false)
    // Visuals
    private val markValue = BoolValue("Mark", true)

    // Target block
    private var targetPlace: PlaceInfo? = null

    // Rotation lock
    private var lockRotation: Rotation? = null
    private var lockRotationTimer = TickTimer()

    // Launch position
    private var launchY = 0
    private var facesBlock = false

    // AutoBlock
    private var slot = 0

    // Zitter Direction
    private var zitterDirection = false

    // Delay
    private val delayTimer = MSTimer()
    private val zitterTimer = MSTimer()
    private var delay = 0L
    private var ticks = 0

    // Eagle
    private var placedBlocksWithoutEagle = 0
    private var eagleSneaking = false

    // Downwards
    private var shouldGoDown = false
    private var keydown = 0


    // Enabling module
    override fun onEnable() {
        val player = mc.player ?: return

        updateSameY()

        canPlace = false
        canRot = false
        f = false
        airtime = 0
        launchY = player.posY.roundToInt()
        slot = player.inventory.currentItem
        facesBlock = false
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        ticks = 1
        if (mc.player!!.onGround) airtime++
    }



    // Events
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        updateSameY()
        val player = mc.player ?: return
        mc.gameSettings.keyBindBack.pressed = false
        if (aparkour.get()){
            if (MovementUtils.isMoving && player.onGround && !player.isSneaking && !mc.gameSettings.keyBindSneak.isKeyDown && !mc.gameSettings.keyBindJump.isKeyDown && mc.gameSettings.keyBindForward.isKeyDown) {
                mc.gameSettings.keyBindJump.pressed = true
            }else{
                mc.gameSettings.keyBindJump.pressed = mc.gameSettings.keyBindJump.isKeyDown
            }
        }
        if(!player.onGround){
            airtime++
        }else{
            if(placeConditionValue.get().equals("falldown", ignoreCase = true)|| (placeConditionValue.get().equals("delayair", ignoreCase = true))){
                delay = 0L
                delayTimer.reset()
                eagleSneaking = false
                shouldGoDown = false
                canPlace = false
                canRot = false
                f = false
                launchY = player.posY.roundToInt()
                slot = player.inventory.currentItem
                facesBlock = false
            }
            airtime = 0

        }
        f = airtime > airticks.get()
        n = airtime > Rotairticks.get()
        if ((timeronground.get() && mc.player!!.onGround) || !timeronground.get()) (mc.timer as IMixinTimer).timerSpeed = timerValue.get()
        if(mc.player!!.onGround){
            ticks = 0
        }

        canPlace = (((placeConditionValue.get().equals("falldown", ignoreCase = true))&&mc.player!!.fallDistance > 0)||(placeConditionValue.get().equals("always", ignoreCase = true)) || (placeConditionValue.get().equals("delayair", ignoreCase = true)&&!mc.player!!.onGround&&f) )
        canRot =(((RotConditionValue.get().equals("falldown", ignoreCase = true))&&mc.player!!.fallDistance > 0)||(RotConditionValue.get().equals("always", ignoreCase = true)) || (RotConditionValue.get().equals("delayair", ignoreCase = true)&&!mc.player!!.onGround&&n) )
        if ((sprintModeValue.get().equals("Telly", ignoreCase = true) && ticks ==1) || sprintModeValue.get().equals("off", ignoreCase = true)
        ) {
            mc.player!!.isSprinting = false
        }else if (sprintModeValue.get().equals("Telly", ignoreCase = true)){
            mc.player!!.isSprinting = true
        }

        shouldGoDown =
            downValue.get() && !sameYStatus && GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) && blocksAmount > 1
        if (shouldGoDown) {
            mc.gameSettings.keyBindSneak.pressed = false
        }
        if (slowValue.get()) {
            player.motionX = player.motionX * slowSpeed.get()
            player.motionZ = player.motionZ * slowSpeed.get()
        }
        // Eagle
        // Eagle
        if (!eagleValue.get().equals("Off", true) && !shouldGoDown) {
            var dif = 0.5
            val blockPos = BlockPos(player.posX, player.posY - 1.0, player.posZ)
            if (edgeDistanceValue.get() > 0) {
                for (facingType in EnumFacing.values()) {
                    if (facingType == EnumFacing.UP || facingType == EnumFacing.DOWN) {
                        continue
                    }
                    val side = facingType
                    val neighbor = blockPos.offset(side)
                    if (isReplaceable(neighbor)) {
                        val calcDif = (if (facingType == EnumFacing.NORTH || facingType == EnumFacing.SOUTH) {
                            abs((neighbor.z + 0.5) - player.posZ)
                        } else {
                            abs((neighbor.x + 0.5) - player.posX)
                        }) - 0.5

                        if (calcDif < dif) {
                            dif = calcDif
                        }
                    }
                }
            }
            if (placedBlocksWithoutEagle >= blocksToEagleValue.get()) {
                val shouldEagle =
                    isReplaceable(blockPos) || (edgeDistanceValue.get() > 0 && dif < edgeDistanceValue.get())
                if (eagleValue.get().equals("Silent", true)) {
                    if (eagleSneaking != shouldEagle) {
                        mc.connection?.sendPacket(
                            CPacketEntityAction(
                                player, if (shouldEagle) {
                                    CPacketEntityAction.Action.START_SNEAKING
                                } else {
                                    CPacketEntityAction.Action.STOP_SNEAKING
                                }
                            )
                        )
                    }
                    eagleSneaking = shouldEagle
                } else {
                    mc.gameSettings.keyBindSneak.pressed = shouldEagle
                }
                placedBlocksWithoutEagle = 0
            } else {
                placedBlocksWithoutEagle++
            }
        }
        if (player.onGround) {
            when (modeValue.get().toLowerCase()) {
                "rewinside" -> {
                    MovementUtils.strafe(0.2F)
                    player.motionY = 0.0
                }
            }
            when (zitterMode.get().toLowerCase()) {
                "off" -> {
                    return
                }
                "smooth" -> {
                    if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) {
                        mc.gameSettings.keyBindRight.pressed = false
                    }
                    if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) {
                        mc.gameSettings.keyBindLeft.pressed = false
                    }
                    if (zitterTimer.hasTimePassed(100)) {
                        zitterDirection = !zitterDirection
                        zitterTimer.reset()
                    }
                    if (zitterDirection) {
                        mc.gameSettings.keyBindRight.pressed = true
                        mc.gameSettings.keyBindLeft.pressed = false
                    } else {
                        mc.gameSettings.keyBindRight.pressed = false
                        mc.gameSettings.keyBindLeft.pressed = true
                    }
                }
                "teleport" -> {
                    MovementUtils.strafe(zitterSpeed.get())
                    val yaw = Math.toRadians(player.rotationYaw + if (zitterDirection) 90.0 else -90.0)
                    player.motionX = player.motionX - sin(yaw) * zitterStrength.get()
                    player.motionZ = player.motionZ + cos(yaw) * zitterStrength.get()
                    zitterDirection = !zitterDirection
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if(event.packet is SPacketDisconnect){
            state = false
        }
        if (mc.player == null) {
            return
        }

        val packet = event.packet
        if (packet is CPacketHeldItemChange) {
            slot = packet.slotId
        }

    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (strafeMode.get().equals("Off", true)) {
            return
        }
        if(!canRot){ return }
        update()
        val rotation = lockRotation ?: return

        if (rotationsValue.get() && (keepRotationValue.get() || !lockRotationTimer.hasTimePassed(keepLengthValue.get()))) {
            if (targetPlace == null) {
                rotation.yaw = wrapAngleTo180_float((rotation.yaw / 45f).roundToInt() * 45f)
            }
            setRotation(rotation)
            lockRotationTimer.update()

            rotation.applyStrafeToPlayer(event)
            event.cancelEvent()
            return
        }

        val targetRotation = RotationUtils.targetRotation ?: return
        targetRotation.applyStrafeToPlayer(event)
        event.cancelEvent()
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val eventState = event.eventState
        if(!canRot){ return }
        // Lock Rotation
        if (rotationsValue.get() && (keepRotationValue.get() || !lockRotationTimer.hasTimePassed(keepLengthValue.get())) && lockRotation != null && strafeMode.get()
                .equals("Off", true)
        ) {
            setRotation(lockRotation!!)
            if (eventState == EventState.POST) {
                lockRotationTimer.update()
            }
        }

        // Face block
        if ((facesBlock || !rotationsValue.get()) && placeModeValue.get().equals(eventState.stateName, true)) {
            if(!canPlace){return}
            place()
        }
        if((mc.player!!.fallDistance > 0 && FallFastplace.get()) || (canPlace&&Fastplace.get())){
            place()
        }
        // Update and search for a new block
        if (eventState == EventState.PRE && strafeMode.get().equals("Off", true)) {
            update()
        }

        // Reset placeable delay
        if (targetPlace == null && placeDelay.get()) {
            delayTimer.reset()
        }
    }

    fun update() {
        if(!canRot){ return }
        val player = mc.player ?: return

        val holdingItem = player.heldItemMainhand != null && player.heldItemMainhand!!.item is ItemBlock
        if (if (!autoBlockValue.get()
                    .equals("off", true)
            ) InventoryUtils.findAutoBlockBlock() == -1 && !holdingItem else !holdingItem
        ) {
            return
        }

        findBlock(modeValue.get().equals("expand", true))
    }

    private fun setRotation(rotation: Rotation) {

        if(!canRot){ return }
        val player = mc.player ?: return

        if (silentRotationValue.get()) {
            RotationUtils.setTargetRotation(rotation, 0)
        } else {
            player.rotationYaw = rotation.yaw
            player.rotationPitch = rotation.pitch
        }
    }

    // Search for new target block
    private fun findBlock(expand: Boolean) {

        val player = mc.player ?: return
        if(!canRot){ return }
        val blockPosition = if (shouldGoDown) {
            (if (player.posY == player.posY.roundToInt() + 0.5) {
                BlockPos(player.posX, player.posY - 0.6, player.posZ)
            } else {
                BlockPos(player.posX, player.posY - 0.6, player.posZ).down()
            })
        } else (if (sameYStatus && launchY <= player.posY) {
            BlockPos(player.posX, launchY - 1.0, player.posZ)
        } else (if (player.posY == player.posY.roundToInt() + 0.5) {
            BlockPos(player)
        } else {
            BlockPos(player.posX, player.posY, player.posZ).down()
        }))
        if (!expand && (!isReplaceable(blockPosition) || search(blockPosition, !shouldGoDown))) {
            return
        }

        if (expand) {
            val yaw = Math.toRadians(player.rotationYaw.toDouble() + 180)
            val x = if (omniDirectionalExpand.get()) -sin(yaw).roundToInt() else player.horizontalFacing.directionVec.x
            val z = if (omniDirectionalExpand.get()) cos(yaw).roundToInt() else player.horizontalFacing.directionVec.z
            for (i in 0 until expandLengthValue.get()) {
                if (search(blockPosition.add(x * i, 0, z * i), false)) {
                    return
                }
            }
        } else if (searchValue.get()) {
            for (x in -1..1) {
                for (z in -1..1) {
                    if (search(blockPosition.add(x, 0, z), !shouldGoDown)) {
                        return
                    }
                }
            }
        }
    }

    fun place() {
        val player = mc.player ?: return
        val world = mc.world ?: return
        if(!canPlace){ return }
        if (targetPlace == null) {
            if (placeDelay.get()) {
                delayTimer.reset()
            }
            return
        }

        if (!delayTimer.hasTimePassed(delay) || sameYStatus && launchY - 1 != targetPlace!!.vec3.y.toInt()) {
            return
        }

        var itemStack = player.heldItemMainhand
        if (itemStack == null || itemStack.item !is ItemBlock|| (itemStack.item!! as ItemBlock).block is BlockBush || player.heldItemMainhand!!.stackSize <= 0) {
            val blockSlot = InventoryUtils.findAutoBlockBlock()

            if (blockSlot == -1) {
                return
            }

            when (autoBlockValue.get().toLowerCase()) {
                "off" -> {
                    return
                }
                "pick" -> {
                    if (blockSlot - 36 != slot) {
                        mc.player!!.inventory.currentItem = blockSlot - 36
                        mc.playerController.updateController()
                    }
                }
                "spoof" -> {
                    if (blockSlot - 36 != slot) {
                        mc.connection?.sendPacket(CPacketHeldItemChange(blockSlot - 36))
                    }
                }
                "switchfix" -> {
                    if (blockSlot - 36 != slot) {
                        mc.connection?.sendPacket(CPacketHeldItemChange(blockSlot - 36))
                    }
                }
                "switch" -> {
                    if (blockSlot - 36 != slot) {
                        mc.player!!.inventory.currentItem = blockSlot - 36
                    }
                }
            }
            itemStack = player.inventoryContainer.getSlot(blockSlot).stack
        }

        if (mc.playerController.processRightClickBlock(
                player, world, targetPlace!!.blockPos, targetPlace!!.enumFacing, targetPlace!!.vec3, EnumHand.MAIN_HAND
            ) == EnumActionResult.SUCCESS
        ) {
            delayTimer.reset()
            delay = if (!placeDelay.get()){
                0
            }else{
                if(mc.player!!.fallDistance > 0){
                    falldowndelay.get().toLong()
                }else {
                    TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
                }
            }


            if (player.onGround) {
                val modifier = speedModifierValue.get()
                player.motionX = player.motionX * modifier
                player.motionZ = player.motionZ * modifier
            }

            if (swingValue.get()) {
                player.swingArm(EnumHand.MAIN_HAND)

            } else {
                mc.connection?.sendPacket(CPacketAnimation())
            }
        }

        targetPlace = null
    }

    // Disabling module
    override fun onDisable() {
        val player = mc.player ?: return

        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            mc.gameSettings.keyBindSneak.pressed = false
            if (eagleSneaking) {
                mc.connection?.sendPacket(
                    CPacketEntityAction(
                        player, CPacketEntityAction.Action.STOP_SNEAKING
                    )
                )
            }
        }
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) mc.gameSettings.keyBindRight.pressed = false
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) mc.gameSettings.keyBindLeft.pressed = false
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindJump)) mc.gameSettings.keyBindJump.pressed = false

        lockRotation = null
        facesBlock = false
        (mc.timer as IMixinTimer).timerSpeed = 1f
        shouldGoDown = false

        if (slot != player.inventory.currentItem) {
            mc.connection?.sendPacket(CPacketHeldItemChange(player.inventory.currentItem))
        }
    }

    // Entity movement event
    @EventTarget
    fun onMove(event: MoveEvent) {
        val player = mc.player ?: return

        if (!safeWalkValue.get() || shouldGoDown) {
            return
        }
        if (airSafeValue.get() || player.onGround) {
            event.isSafeWalk = true
        }
    }

    // Visuals
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val player = mc.player ?: return
        if (!markValue.get()) {
            return
        }
        for (i in 0 until if (modeValue.get().equals("Expand", true)) expandLengthValue.get() + 1 else 2) {
            val yaw = Math.toRadians(player.rotationYaw.toDouble())
            val x = if (omniDirectionalExpand.get()) -sin(yaw).roundToInt() else player.horizontalFacing.directionVec.x
            val z = if (omniDirectionalExpand.get()) cos(yaw).roundToInt() else player.horizontalFacing.directionVec.z
            val blockPos = BlockPos(
                player.posX + x * i,
                if (sameYStatus && launchY <= player.posY) launchY - 1.0 else player.posY - (if (player.posY == player.posY + 0.5) 0.0 else 1.0) - if (shouldGoDown) 1.0 else 0.0,
                player.posZ + z * i
            )
            val placeInfo = PlaceInfo.get(blockPos)
            if (isReplaceable(blockPos) && placeInfo != null) {
                RenderUtils.drawBlockBox(blockPos, Color(68, 117, 255, 100), false)
                break
            }
        }
    }

    /**
     * Search for placeable block
     *
     * @param blockPosition pos
     * @param raycast visible
     * @return
     */

    private fun search(blockPosition: BlockPos, raycast: Boolean): Boolean {

        facesBlock = false
        val player = mc.player ?: return false
        val world = mc.world ?: return false

        if (!isReplaceable(blockPosition)) {
            return false
        }

        // Search Ranges
        val xzRV = xzRangeValue.get().toDouble()
        val xzSSV = calcStepSize(xzRV.toFloat())
        val yRV = yRangeValue.get().toDouble()
        val ySSV = calcStepSize(yRV.toFloat())
        val eyesPos = Vec3d(player.posX, player.entityBoundingBox.minY + player.eyeHeight, player.posZ)
        var placeRotation: PlaceRotation? = null
        for (facingType in EnumFacing.values()) {
            val side = facingType
            val neighbor = blockPosition.offset(side)
            if (!canBeClicked(neighbor)) {
                continue
            }
            val dirVec = net.minecraft.util.math.Vec3d(side.directionVec)
            val auto = searchMode.get().equals("Auto", true)
            val center = searchMode.get().equals("AutoCenter", true)
            var xSearch = if (auto) 0.1 else 0.5 - xzRV / 2
            while (xSearch <= if (auto) 0.9 else 0.5 + xzRV / 2) {
                var ySearch = if (auto) 0.1 else 0.5 - yRV / 2
                while (ySearch <= if (auto) 0.9 else 0.5 + yRV / 2) {
                    var zSearch = if (auto) 0.1 else 0.5 - xzRV / 2
                    while (zSearch <= if (auto) 0.9 else 0.5 + xzRV / 2) {
                        val posVec = Vec3d(blockPosition).addVector(
                            if (center) 0.5 else xSearch, if (center) 0.5 else ySearch, if (center) 0.5 else zSearch
                        )
                        val distanceSqPosVec = eyesPos.squareDistanceTo(posVec)
                        val hitVec = posVec.add(
                            net.minecraft.util.math.Vec3d(
                                dirVec.x * 0.5,
                                dirVec.y * 0.5,
                                dirVec.z * 0.5
                            )
                        )
                        if (raycast && (eyesPos.distanceTo(hitVec) > 4.25 || distanceSqPosVec > eyesPos.squareDistanceTo(
                                posVec.add(dirVec)
                            ) || world.rayTraceBlocks(
                                eyesPos,
                                hitVec,
                                false,
                                true,
                                false
                            ) != null)
                        ) {
                            zSearch += if (auto) 0.1 else xzSSV
                            continue
                        }

                        // Face block
                        val diffX = hitVec.x - eyesPos.x
                        val diffY = hitVec.y - eyesPos.y
                        val diffZ = hitVec.z - eyesPos.z
                        val diffXZ = sqrt(diffX * diffX + diffZ * diffZ)
                        if (facingType != EnumFacing.UP && facingType != EnumFacing.DOWN) {
                            val diff = abs(if (facingType == EnumFacing.NORTH || facingType == EnumFacing.SOUTH) diffZ else diffX)
                            if (diff < minDistValue.get()) {
                                zSearch += if (auto) 0.1 else xzSSV
                                continue
                            }
                        }
                        var rotation = Rotation(
                            wrapAngleTo180_float(Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f),
                            wrapAngleTo180_float(-Math.toDegrees(atan2(diffY, diffXZ)).toFloat())
                        )
                        val rotationVector = RotationUtils.getVectorForRotation(rotation)
                        val vector = eyesPos.addVector(
                            rotationVector.x * 4.25, rotationVector.y * 4.25, rotationVector.z * 4.25
                        )


                        val obj = world.rayTraceBlocks(
                            eyesPos,
                            vector,
                            false,
                            false,
                            true
                        ) ?: continue

                        if (obj.typeOfHit != RayTraceResult.Type.BLOCK || obj.blockPos != neighbor) {
                            zSearch += if (auto) 0.1 else xzSSV
                            continue
                        }
                        if (placeRotation == null || RotationUtils.getRotationDifference(rotation) < RotationUtils.getRotationDifference(
                                placeRotation.rotation
                            )
                        ) {
                            placeRotation = PlaceRotation(PlaceInfo(neighbor, side.opposite, hitVec), rotation)
                        }

                        zSearch += if (auto) 0.1 else xzSSV
                    }
                    ySearch += if (auto) 0.1 else ySSV
                }
                xSearch += if (auto) 0.1 else xzSSV
            }
        }
        if (placeRotation == null) {
            return false
        }
        if (rotationsValue.get() && canRot) {

            if (minTurnSpeedValue.get() < 180) {
                val limitedRotation = RotationUtils.limitAngleChange(
                    RotationUtils.serverRotation,
                    placeRotation.rotation,
                    (Math.random() * (maxTurnSpeedValue.get() - minTurnSpeedValue.get()) + minTurnSpeedValue.get()).toFloat()
                )

                if ((10 * wrapAngleTo180_float(limitedRotation.yaw)).roundToInt() == (10 * wrapAngleTo180_float(
                        placeRotation.rotation.yaw
                    )).roundToInt() && (10 * wrapAngleTo180_float(limitedRotation.pitch)).roundToInt() == (10 * wrapAngleTo180_float(
                        placeRotation.rotation.pitch
                    )).roundToInt()
                ) {
                    setRotation(placeRotation.rotation)
                    lockRotation = placeRotation.rotation
                    facesBlock = true
                } else {
                    setRotation(limitedRotation)
                    lockRotation = limitedRotation
                    facesBlock = false
                }
            } else {
                setRotation(placeRotation.rotation)
                lockRotation = placeRotation.rotation
                facesBlock = true
            }
            lockRotationTimer.reset()
        }
        targetPlace = placeRotation.placeInfo
        return true
    }

    private fun calcStepSize(range: Float): Double {
        var accuracy = searchAccuracyValue.get().toDouble()
        accuracy += accuracy % 2 // If it is set to uneven it changes it to even. Fixes a bug
        return if (range / accuracy < 0.01) 0.01 else (range / accuracy)
    }
    private fun updateSameY() {
        when (sameYValue.get().toLowerCase()) {
            "on" -> sameYStatus = true
            "off" -> sameYStatus = false
            "pressjumpkeyon" -> sameYStatus = Keyboard.isKeyDown(mc.gameSettings.keyBindJump.keyCode)
            "pressjumpkeyoff" -> sameYStatus = !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.keyCode)
        }
    }


    // Return hotbar amount
    val blocksAmount: Int
        get() {
            var amount = 0
            for (i in 36..44) {
                val itemStack = mc.player.inventoryContainer.getSlot(i).stack
                if (itemStack != null && itemStack.item is ItemBlock) {
                    val block = (itemStack.item as ItemBlock).block
                    val heldItem = mc.player.heldItemMainhand
                    if (heldItem != null &&
                        heldItem == itemStack ||
                        !InventoryUtils.BLOCK_BLACKLIST.contains(block) &&
                        block !is BlockBush

                    ) amount += itemStack.stackSize
                }
            }
            return amount
        }
    }