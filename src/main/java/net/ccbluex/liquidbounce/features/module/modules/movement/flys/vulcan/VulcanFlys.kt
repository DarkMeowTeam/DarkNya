package net.ccbluex.liquidbounce.features.module.modules.movement.flys.vulcan

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.TransferUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.Block.FULL_BLOCK_AABB
import net.minecraft.block.BlockCarpet
import net.minecraft.block.BlockLadder
import net.minecraft.block.BlockSnow
import net.minecraft.block.BlockVine
import net.minecraft.client.settings.GameSettings
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import net.minecraft.block.material.Material
import net.minecraft.network.play.client.CPacketConfirmTransaction
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos

class VulcanFlys : FlyMode("Vulcan") {

    private var flys = ListValue("Vulcan-Mode", arrayOf("High", "Clip", "Damage", "Fast", "Ghost", "GhostNew"), "GhostNew")

    private val vulcanhighheight = IntegerValue("VulcanHigh-ClipHeight", 10, 50, 100).displayable { flys.equals("High") }
    private val vulcanclipcanClipValue = BoolValue("VulcanClip-CanClip", true).displayable { flys.equals("Clip") }

    // Vulcan Damage
    private val bypassMode = ListValue("VulcanDmg-BypassMode", arrayOf("Damage", "SelfDamage", "InstantDamage", "Flag"), "InstantDamage").displayable { flys.equals("Damage") }
    private val flyMode = ListValue("VulcanDmg-FlyMode", arrayOf("Timer", "CancelMove", "Clip"), "CancelMove").displayable { flys.equals("Damage") }
    private val flyHSpeedValue = FloatValue("VulcanDmg-Horizontal", 1.0f, 0.5f, 2.5f).displayable { flyMode.equals("CancelMove") && flys.equals("Damage") }
    private val flyVSpeedValue = FloatValue("VulcanDmg-Vertical", 0.42f, 0.42f, 2.5f).displayable{ flyMode.equals("CancelMove") && flys.equals("Damage") }
    private val flyDistanceValue = FloatValue("VulcanDmg-Distance", 10.0f, 6.0f, 10.0f).displayable { flys.equals("Damage") }
    private val autoDisableValue = BoolValue("VulcanDmg-AutoDisable", true).displayable { flys.equals("Damage") }
    private val flyTimerValue = FloatValue("VulcanDmg-Timer", 0.05f, 0.05f, 0.25f).displayable{ flyMode.equals("Timer") }

    // Fast
    private val speedValue = FloatValue("VulcanFast-Speed", 1f, 0.1f, 6f).displayable { flys.equals("Fast") }
    private val verticalValue = BoolValue("VulcanFast-Vertical", false).displayable { flys.equals("Fast") }

    // Ghost
    private val timerValue = FloatValue("VulcanGhost-Timer", 2f, 1f, 3f).displayable { flys.equals("Ghost") }


    // Variables
    private var ticks = 0
    private var sent = false
    private var waitFlag = false
    private var canGlide = false
    private var isStarted = false
    private var isDamaged = false
    private var dmgJumpCount = 0
    private var flyTicks = 0
    private var lastSentX = 0.0
    private var lastSentY = 0.0
    private var lastSentZ = 0.0
    private var lastTickX = 0.0
    private var lastTickY = 0.0
    private var lastTickZ = 0.0
    private var isSuccess = false
    private var vticks = 0
    private var doCancel = false
    private var stage = FlyStage.FLYING
    private var startX = 0.0
    private var startZ = 0.0
    private var startY = 0.0
    private var modifyTicks = 0
    private var flags = 0
    private var groundX = 0.0
    private var groundY = 0.0
    private var groundZ = 0.0

    override fun onEnable() {
        val player = mc.player ?: return
        val connection = mc.connection ?: return
        val timer = mc.timer as IMixinTimer
        
        sent = false


        when (flys.get()) {
            "GhostNew" -> {
                ClientUtils.displayChatMessage("Ensure that you sneak on landing.")
                ClientUtils.displayChatMessage("After landing, go backward (Air) and go forward to landing location, then sneak again.")
                ClientUtils.displayChatMessage("And then you can turn off fly.")
            }
            "Clip" -> {
                if(player.onGround && vulcanclipcanClipValue.get()) {
                    clip(0f, -0.1f)
                    waitFlag = true
                    canGlide = false
                    ticks = 0
                    timer.timerSpeed = 0.1f
                } else {
                    waitFlag = false
                    canGlide = true
                }
            }
            "Damage" -> {
                flyTicks = 0
                waitFlag = false
                isStarted = false
                isDamaged = false
                dmgJumpCount = 0
                timer.timerSpeed = 1.0f
                if (bypassMode.equals("InstantDamage")) {
                    dmgJumpCount = 11451
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY,
                            player.posZ,
                            true
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY - 0.0784,
                            player.posZ,
                            false
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY,
                            player.posZ,
                            true
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY + 0.41999998688697815,
                            player.posZ,
                            false
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY + 0.7531999805212,
                            player.posZ,
                            false
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY + 1.0,
                            player.posZ,
                            true
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY + 1.4199999868869781,
                            player.posZ,
                            false
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY + 1.7531999805212,
                            player.posZ,
                            false
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY + 2.0,
                            player.posZ,
                            true
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY + 2.419999986886978,
                            player.posZ,
                            false
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY + 2.7531999805212,
                            player.posZ,
                            false
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY + 3.00133597911214,
                            player.posZ,
                            false
                        )
                    )
                    player.setPosition(player.posX, player.posY + 3.00133597911214, player.posZ)
                    waitFlag = true
                } else if (bypassMode.equals("Flag")) {
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY,
                            player.posZ,
                            true
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY - 2,
                            player.posZ,
                            true
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY,
                            player.posZ,
                            true
                        )
                    )
                } else {
                    runSelfDamageCore()
                }
            }
            "Fast" -> {
                vticks = 0
                doCancel = false
                if(verticalValue.get()) {
                    ClientUtils.displayChatMessage("§8[§c§lVulcan-Fast-Flight§8] §cVertical Flying sometimes flag!")
                }
                if(player.posY % 1 != 0.0) {
                    fly.state = false
                    ClientUtils.displayChatMessage("§8[§c§lVulcan-Fast-Flight§8] §cPlease stand on a solid block to fly!")
                    isSuccess = true
                    return
                }
                stage = FlyStage.FLYING
                isSuccess = false
                ClientUtils.displayChatMessage("§8[§c§lVulcan-Fast-Flight§8] §aPlease press Sneak before you land on ground!")
                ClientUtils.displayChatMessage("§8[§c§lVulcan-Fast-Flight§8] §7Tips: DO NOT Use killaura when you're flying!")
                startX = player.posX
                startY = player.posY
                startZ = player.posZ
            }
            "Ghost" -> {
                ticks = 0
                modifyTicks = 0
                flags = 0
                player.setPosition(player.posX, (player.posY * 2).roundToInt().toDouble() / 2, player.posZ)
                stage = FlyStage.WAITING
                ClientUtils.displayChatMessage("§8[§c§lVulcan-Ghost-Flight§8] §aPlease press Sneak before you land on ground!")
                ClientUtils.displayChatMessage("§8[§c§lVulcan-Ghost-Flight§8] §aYou can go Up/Down by pressing Jump/Sneak")
            }
        }
    }

    override fun onDisable() {
        val player = mc.player ?: return
        val timer = mc.timer as IMixinTimer
        
        timer.timerSpeed = 1.0f

        if (flys.equals("Fast")) {
            if (!isSuccess) {
                player.setPosition(startX, startY, startZ)
                ClientUtils.displayChatMessage("§8[§c§lVulcan-Fast-Flight§8] §cFly attempt Failed...")
                ClientUtils.displayChatMessage("§8[§c§lVulcan-Fast-Flight§8] §cIf it keeps happen, Don't use it again in CURRENT gameplay")
            }
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return
        val connection = mc.connection ?: return
        val world = mc.world ?: return
        val timer = mc.timer as IMixinTimer
        
        when (flys.get()) {
            "Damage" -> {
                if (flyTicks > 7 && autoDisableValue.get()) {
                    fly.state = false
                    fly.onDisable()
                    return
                }

                if (!bypassMode.equals("InstantDamage") && runSelfDamageCore()) {
                    return
                }
                if (bypassMode.equals("InstantDamage") && dmgJumpCount == 11451) {
                    if (!isStarted) {
                        return
                    } else {
                        isStarted = false
                        waitFlag = false
                        connection.sendPacket(
                            CPacketPlayer.Position(
                                player.posX,
                                player.posY,
                                player.posZ,
                                true
                            )
                        )
                        dmgJumpCount = 999
                    }
                }
                player.jumpMovementFactor = 0.00f
                if (!isStarted && !waitFlag) {
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY - 0.0784,
                            player.posZ,
                            false
                        )
                    )
                    waitFlag = true
                }
                if (isStarted) {
                    when (flyMode.get().toLowerCase()) {
                        "cancelmove" -> {
                            timer.timerSpeed = 1.0f
                            MovementUtils.resetMotion(false)
                            if (!mc.gameSettings.keyBindSneak.isKeyDown) {
                                MovementUtils.resetMotion(true)
                                if (mc.gameSettings.keyBindJump.isKeyDown) {
                                    player.motionY = flyVSpeedValue.get().toDouble()
                                }
                            }

                            MovementUtils.strafe(flyHSpeedValue.get())
                        }
                        "timer" -> {
                            flyTicks++
                            timer.timerSpeed = flyTimerValue.get()
                            MovementUtils.resetMotion(true)
                            if (flyTicks > 4) {
                                MovementUtils.strafe(flyDistanceValue.get() - 0.005f)
                            } else {
                                MovementUtils.strafe(flyDistanceValue.get() - 0.205f + flyTicks.toFloat() * 0.05f)
                            }
                        }
                        "clip" -> {
                            MovementUtils.resetMotion(true)
                            if (player.ticksExisted % 10 == 0) {
                                flyTicks++
                                val yaw = Math.toRadians(player.rotationYaw.toDouble())
                                player.setPosition(player.posX + (-sin(yaw) * flyDistanceValue.get()), player.posY + 0.42, player.posZ + (cos(yaw) * flyDistanceValue.get()))
                                PacketUtils.sendPacketNoEvent(
                                    CPacketPlayer.Position(
                                        player.posX,
                                        player.posY,
                                        player.posZ,
                                        false
                                    )
                                )
                            }
                        }
                    }
                }
            }
            "Fast" -> {
                when (stage) {
                    FlyStage.FLYING -> {
                        isSuccess = false

                        MovementUtils.resetMotion(false)

                        MovementUtils.strafe(speedValue.get())
                        doCancel = true

                        if(mc.gameSettings.keyBindSneak.pressed) {
                            MovementUtils.strafe(0.45f)
                            if(verticalValue.get()) {
                                player.motionY = 0.0 - speedValue.get().toDouble()
                            }
                        }
                        if(verticalValue.get()) {
                            if(mc.gameSettings.keyBindJump.pressed) {
                                player.motionY = speedValue.get().toDouble()
                            } else if(!mc.gameSettings.keyBindSneak.pressed) {
                                player.motionY = 0.0
                            }
                        }
                        if(mc.gameSettings.keyBindSneak.pressed && player.ticksExisted % 2 == 1) {
                            val fixedY = player.posY - (player.posY % 1)

                            if(BlockUtils.isFullBlock(BlockPos(player.posX, fixedY - 1, player.posZ))) {
                                stage = FlyStage.WAIT_APPLY
                                MovementUtils.resetMotion(true)
                                player.jumpMovementFactor = 0.00f
                                doCancel = false
                                player.onGround = false
                                var fixedX = player.posX - (player.posX % 1)
                                var fixedZ = player.posZ - (player.posZ % 1)
                                if(fixedX>0) {
                                    fixedX += 0.5
                                }else{
                                    fixedX -= 0.5
                                }
                                if(fixedZ>0) {
                                    fixedZ += 0.5
                                }else{
                                    fixedZ -= 0.5
                                }
                                player.setPosition(fixedX, fixedY, fixedZ)
                                connection.sendPacket(CPacketPlayer.Position(player.posX, fixedY , player.posZ, true))
                                doCancel = true
                                ClientUtils.displayChatMessage("§8[§c§lVulcan-Flight§8] §aWaiting for landing...")
                            } else {
                                ClientUtils.displayChatMessage("§8[§c§lVulcan-Flight§8] §cYou can only land on a solid block!")
                            }
                        }
                    }
                    FlyStage.WAIT_APPLY -> {
                        vticks++
                        doCancel = false
                        if(vticks == 60) {
                            ClientUtils.displayChatMessage("§8[§c§lVulcan-Flight§8] §cSeems took a long time! Please turn off the Flight manually")
                        }
                        timer.timerSpeed = 1f
                        MovementUtils.resetMotion(true)
                        player.jumpMovementFactor = 0.00f
                        val fixedY = player.posY - (player.posY % 1)
                        if(world.getCollisionBoxes(player, player.entityBoundingBox.offset(0.0, -10.0, 0.0)).isEmpty() && world.getCollisionBoxes(player, player.entityBoundingBox.offset(0.0, -12.0, 0.0)).isEmpty()) {
                            connection.sendPacket(CPacketPlayer.Position(player.posX, fixedY - 10, player.posZ, true))
                        }else {
                            connection.sendPacket(CPacketPlayer.Position(player.posX, fixedY - 1024, player.posZ, true))
                        }
                        doCancel = true
                    }
                }
            }
            "Ghost" -> {
                ticks++
                modifyTicks++
                mc.gameSettings.keyBindJump.pressed = false
                mc.gameSettings.keyBindSneak.pressed = false
                when(stage) {
                    FlyStage.FLYING, FlyStage.WAITING -> {
                        if(stage == FlyStage.FLYING) {
                            timer.timerSpeed = timerValue.get()
                        }else{
                            timer.timerSpeed = 1.0f
                        }
                        if(ticks == 2 && GameSettings.isKeyDown(mc.gameSettings.keyBindJump) && modifyTicks>=6 && world.getCollisionBoxes(player, player.entityBoundingBox.offset(0.0, 0.5, 0.0)).isEmpty()) {
                            player.setPosition(player.posX, player.posY+0.5, player.posZ)
                            modifyTicks = 0
                        }
                        if(!MovementUtils.isMoving && ticks == 1 && (GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) || GameSettings.isKeyDown(mc.gameSettings.keyBindJump)) && modifyTicks>=5) {
                            val playerYaw = player.rotationYaw * Math.PI / 180
                            player.setPosition(player.posX + 0.05 * -sin(playerYaw)
                                , player.posY
                                , player.posZ + 0.05 * cos(playerYaw)
                            )
                        }
                        if(ticks == 2 && GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) && modifyTicks>=6 && world.getCollisionBoxes(player, player.entityBoundingBox.offset(0.0, -0.5, 0.0)).isEmpty()) {
                            player.setPosition(player.posX, player.posY-0.5, player.posZ)
                            modifyTicks = 0
                        }else if(ticks == 2 && GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) && world.getCollisionBoxes(player, player.entityBoundingBox.offset(0.0, -0.5, 0.0))
                                .isNotEmpty()) {
                            PacketUtils.sendPacketNoEvent(CPacketPlayer.Position(player.posX+0.05,player.posY,player.posZ,true))
                            PacketUtils.sendPacketNoEvent(CPacketPlayer.Position(player.posX,player.posY,player.posZ,true))
                            PacketUtils.sendPacketNoEvent(CPacketPlayer.Position(player.posX,player.posY+0.42,player.posZ,true))
                            PacketUtils.sendPacketNoEvent(CPacketPlayer.Position(player.posX,player.posY+0.7532,player.posZ,true))
                            PacketUtils.sendPacketNoEvent(CPacketPlayer.Position(player.posX,player.posY+1.0,player.posZ,true))
                            player.setPosition(player.posX, player.posY+1.0, player.posZ)
                            stage = FlyStage.WAIT_APPLY
                            modifyTicks = 0
                            groundY = player.posY - 1.0
                            groundX = player.posX
                            groundZ = player.posZ
                            ClientUtils.displayChatMessage("§8[§c§lVulcan-Ghost-Flight§8] §aWaiting to land...")
                        }
                        player.onGround = true
                        player.motionY = 0.0
                    }
                    FlyStage.WAIT_APPLY -> {
                        timer.timerSpeed = 1.0f
                        MovementUtils.resetMotion(true)
                        player.jumpMovementFactor = 0.0f
                        if (modifyTicks >= 10) {
                            val playerYaw = player.rotationYaw * Math.PI / 180
                            if (modifyTicks % 2 != 0) {
                                player.setPosition(player.posX + 0.1 * -sin(playerYaw)
                                    , player.posY
                                    , player.posZ + 0.1 * cos(playerYaw)
                                )
                            }else{
                                player.setPosition(player.posX - 0.1 * -sin(playerYaw)
                                    , player.posY
                                    , player.posZ - 0.1 * cos(playerYaw)
                                )
                                if (modifyTicks >= 16 && ticks == 2) {
                                    modifyTicks = 16
                                    player.setPosition(player.posX
                                        , player.posY + 0.5
                                        , player.posZ)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onMotion(event: MotionEvent) {
        val player = mc.player ?: return
        val connection = mc.connection ?: return
        val timer = mc.timer as IMixinTimer
        
        when (flys.get()) {
            "High" -> {
                if (event.eventState == EventState.PRE && !player.onGround) {
                    timer.timerSpeed = 1f
                    player.motionY = -if(ticks % 2 == 0) {
                        0.16
                    } else {
                        0.10
                    }
                    if(ticks == 0) {
                        player.motionY = -0.07
                    }
                    ticks++
                } else if (player.onGround && !sent) {
                    sent = true
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY,
                            player.posZ,
                            true
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY - 0.0784,
                            player.posZ,
                            false
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY,
                            player.posZ,
                            true
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY + 0.41999998688697815,
                            player.posZ,
                            false
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY + 0.7531999805212,
                            player.posZ,
                            false
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY + 1.0,
                            player.posZ,
                            true
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY + 1.4199999868869781,
                            player.posZ,
                            false
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY + 1.7531999805212,
                            player.posZ,
                            false
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY + 2.0,
                            player.posZ,
                            true
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY + 2.419999986886978,
                            player.posZ,
                            false
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY + 2.7531999805212,
                            player.posZ,
                            false
                        )
                    )
                    connection.sendPacket(
                        CPacketPlayer.Position(
                            player.posX,
                            player.posY + 3.00133597911214,
                            player.posZ,
                            false
                        )
                    )
                    player.setPosition(player.posX, player.posY + 3.00133597911214, player.posZ)
                }

                if (sent && player.hurtTime == 9) {
                    player.posY += vulcanhighheight.get()
                }
            }
            "Clip" -> {
                if (event.eventState == EventState.PRE && canGlide) {
                    timer.timerSpeed = 1f
                    player.motionY = -if(ticks % 2 == 0) {
                        0.17
                    } else {
                        0.10
                    }
                    if(ticks == 0) {
                        player.motionY = -0.07
                    }
                    ticks++
                }
            }
        }
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (!flys.equals("GhostNew"))
        if (!mc.gameSettings.keyBindJump.isKeyDown && mc.gameSettings.keyBindSneak.isKeyDown) return
        if (!event.block.defaultState.material.blocksMovement() &&
            event.block !is BlockCarpet &&
            event.block !is BlockVine &&
            event.block !is BlockSnow &&
            event.block !is BlockLadder) {
            event.boundingBox = AxisAlignedBB(
                -2.0,
                -1.0,
                -2.0,
                2.0,
                1.0,
                2.0
            ).offset(
                event.x.toDouble(),
                event.y.toDouble(),
                event.z.toDouble()
            )
        }
    }

    override fun onPacket(event: PacketEvent) {
        val player = mc.player ?: return
        val connection = mc.connection ?: return
        val timer = mc.timer as IMixinTimer
        
        val packet = event.packet
        
        when (flys.get()) {
            "GhostNew" -> {
                if (packet is SPacketPlayerPosLook) {
                    event.cancelEvent()
                }
            }
            "Clip" -> {
                if(packet is SPacketPlayerPosLook && waitFlag) {
                    waitFlag = false
                    player.setPosition(packet.x, packet.y, packet.z)
                    connection.sendPacket(
                        CPacketPlayer.PositionRotation(
                            player.posX,
                            player.posY,
                            player.posZ,
                            player.rotationYaw,
                            player.rotationPitch,
                            false
                        )
                    )
                    event.cancelEvent()
                    player.jump()
                    clip(0.127318f, 0f)
                    clip(3.425559f, 3.7f)
                    clip(3.14285f, 3.54f)
                    clip(2.88522f, 3.4f)
                    canGlide = true
                }
            }
            "Damage" -> {
                if (packet is CPacketPlayer && waitFlag) {
                    event.cancelEvent()
                }
                if (packet is CPacketPlayer && (dmgJumpCount < 4 && ( bypassMode.equals("SelfDamage") || bypassMode.equals("InstantDamage") ) )) {
                    packet.onGround = false
                }
                if (isStarted && flyMode.equals("cancelmove")) {
                    if(packet is CPacketPlayer && (packet is CPacketPlayer.Position || packet is CPacketPlayer.PositionRotation)) {
                        val deltaX = packet.x - lastSentX
                        val deltaY = packet.y - lastSentY
                        val deltaZ = packet.z - lastSentZ

                        if (sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) > flyDistanceValue.get()) {
                            flyTicks++
                            PacketUtils.sendPacketNoEvent(
                                CPacketPlayer.Position(
                                    lastTickX,
                                    lastTickY,
                                    lastTickZ,
                                    false
                                )
                            )
                            lastSentX = lastTickX
                            lastSentY = lastTickY
                            lastSentZ = lastTickZ
                        }
                        lastTickX = packet.x
                        lastTickY = packet.y
                        lastTickZ = packet.z
                        event.cancelEvent()
                    }else if(packet is CPacketPlayer) {
                        event.cancelEvent()
                    }
                }

                if (packet is CPacketPlayer && flyMode.equals("clip") && isStarted) {
                    event.cancelEvent()
                }

                if (packet is SPacketPlayerPosLook) {
                    isStarted = true
                    waitFlag = false
                }

                if (packet is SPacketPlayerPosLook && waitFlag && !flyMode.equals("cancelmove")) {
                    if (bypassMode.equals("InstantDamage")) PacketUtils.sendPacketNoEvent(
                        CPacketPlayer.PositionRotation(
                            packet.x,
                            packet.y,
                            packet.z,
                            packet.yaw,
                            packet.pitch,
                            false
                        )
                    )
                    timer.timerSpeed = 1.0f
                    flyTicks = 0

                } else if (packet is SPacketPlayerPosLook && flyMode.equals("cancelmove")) {
                    lastSentX = packet.x
                    lastSentY = packet.y
                    lastSentZ = packet.z

                    if (!bypassMode.equals("InstantDamage")) event.cancelEvent()

                    TransferUtils.noMotionSet = true
                    PacketUtils.sendPacketNoEvent(
                        CPacketPlayer.PositionRotation(
                            packet.x,
                            packet.y,
                            packet.z,
                            packet.yaw,
                            packet.pitch,
                            false
                        )
                    )
                }

                if (packet is CPacketConfirmTransaction) { //Make sure it works with Vulcan Velocity
                    val transUID = (packet.uid).toInt()
                    if (transUID >= -31767 && transUID <= -30769) {
                        event.cancelEvent()
                        PacketUtils.sendPacketNoEvent(packet)
                    }
                }
            }
            "Fast" -> {
                when (val packet = event.packet) {
                    is CPacketPlayer -> {
                        if(doCancel) {
                            event.cancelEvent()
                            doCancel = false
                        }
                        packet.onGround = true
                    }
                    is SPacketPlayerPosLook -> {
                        if (stage == FlyStage.WAIT_APPLY) {
                            if(sqrt((packet.x-player.posX)*(packet.x-player.posX)
                                        +(packet.y-player.posY)*(packet.y-player.posY)
                                        +(packet.z-player.posZ)*(packet.z-player.posZ)) < 1.4) {
                                isSuccess = true
                                fly.state = false
                                return
                            }
                        }
                        event.cancelEvent()
                    }
                    is CPacketEntityAction -> {
                        event.cancelEvent()
                    }
                }
            }
            "Ghost" -> {
                when (val packet = event.packet) {
                    is CPacketPlayer -> {
                        if(ticks > 2) {
                            ticks = 0
                            packet.y += 0.5
                        }
                        packet.onGround = true
                    }
                    is SPacketPlayerPosLook -> {
                        if (stage == FlyStage.WAITING) {
                            flags++
                            if (flags >= 2) {
                                flags = 0
                                stage = FlyStage.FLYING
                            }
                        }
                        if (stage == FlyStage.WAIT_APPLY) {
                            if(sqrt((packet.x - groundX) * (packet.x - groundX)
                                        + (packet.z - groundZ) * (packet.z - groundZ)) < 1.4 && packet.y >= (groundY - 0.5)) {
                                fly.state = false
                                return
                            }
                        }
                        event.cancelEvent()
                    }
                    is CPacketEntityAction -> {
                        event.cancelEvent()
                    }
                }
            }
        }
    }



    private fun clip(dist: Float, y: Float) {
        val player = mc.player ?: return
        val connection = mc.connection ?: return
        
        val yaw = Math.toRadians(player.rotationYaw.toDouble())
        val x = -sin(yaw) * dist
        val z = cos(yaw) * dist
        player.setPosition(player.posX + x, player.posY + y, player.posZ + z)
        connection.sendPacket(
            CPacketPlayer.Position(
                player.posX,
                player.posY,
                player.posZ,
                false
            )
        )
    }

    fun runSelfDamageCore(): Boolean {
        val player = mc.player ?: return false
        val connection = mc.connection ?: return false
        val timer = mc.timer as IMixinTimer
        
        timer.timerSpeed = 1.0f
        if (bypassMode.equals("Damage") || bypassMode.equals("Flag")) {
            if (!bypassMode.equals("Flag")) {
                if (player.hurtTime > 0 || isDamaged) {
                    isDamaged = true
                    dmgJumpCount = 999
                    return false
                }else {
                    return true
                }
            }
            isDamaged = true
            dmgJumpCount = 999
            return false
        }
        if (isDamaged) {
            dmgJumpCount = 999
            return false
        }
        player.jumpMovementFactor = 0.00f
        if (player.onGround) {
            if (dmgJumpCount >= 4) {
                connection.sendPacket(
                    CPacketPlayer.Position(
                        player.posX,
                        player.posY,
                        player.posZ,
                        true
                    )
                )
                isDamaged = true
                dmgJumpCount = 999
                return false
            }
            dmgJumpCount++
            MovementUtils.resetMotion(true)
            player.jump()
        }
        MovementUtils.resetMotion(false)
        return true
    }

    enum class FlyStage {
        WAITING,
        FLYING,
        WAIT_APPLY
    }

}