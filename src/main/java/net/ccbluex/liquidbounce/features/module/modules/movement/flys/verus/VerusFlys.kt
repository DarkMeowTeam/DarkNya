package net.ccbluex.liquidbounce.features.module.modules.movement.flys.verus

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.BlockAir
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.AxisAlignedBB

class VerusFlys : FlyMode("Verus") {

    private var flys = ListValue("${valuePrefix}Mode", arrayOf("Jump", "Basic"), "Jump")

    // Jump
    private val boostValue = BoolValue("${valuePrefix}Boost", false).displayable { flys.equals("Jump") }
    private val speedValue = FloatValue("${valuePrefix}Speed", 2f, 0f, 3f).displayable { boostValue.get() && flys.equals("Jump") }
    private val boostLength = IntegerValue("${valuePrefix}BoostTime", 500, 300, 1000).displayable { boostValue.get() && flys.equals("Jump") }
    private val moveBeforeDamage = BoolValue("${valuePrefix}MoveBeforeDamage", true).displayable { boostValue.get() && flys.equals("Jump") }
    private val airStrafeValue = BoolValue("${valuePrefix}AirStrafe", true).displayable { flys.equals("Jump") }

    // Variables
    private var times = 0
    private var timer = MSTimer()
    private var ticks = 0
    private var justEnabled = true



    override fun onEnable() {
        times = 0
        timer.reset()
        ticks = 0
        justEnabled = true
    }

    override fun onUpdate(event: UpdateEvent) {
        val player = mc.player ?: return
        when (flys.get()) {
            "Jump" -> {
                if (boostValue.get()) {
                    mc.gameSettings.keyBindJump.pressed = false
                    if (times < 5 && !moveBeforeDamage.get()) {
                        MovementUtils.strafe(0f)
                    }
                    if (player.onGround && times < 5) {
                        times++
                        timer.reset()
                        if (times <5) {
                            player.jump()
                            MovementUtils.strafe(0.48F)
                        }
                    }

                    if (times >= 5) {
                        if (!timer.hasTimePassed(boostLength.get().toLong())) {
                            MovementUtils.strafe(speedValue.get())
                        } else {
                            times = 0
                        }
                    }
                } else {
                    mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                    if (player.onGround && MovementUtils.isMoving) {
                        mc.gameSettings.keyBindJump.pressed = false
                        player.jump()
                        MovementUtils.strafe(0.48F)
                    } else if(airStrafeValue.get()) {
                        MovementUtils.strafe()
                    }
                }
            }
            "Basic" -> {
                if (player.motionY < 0.4)  player.motionY = 0.0
                player.onGround = true
            }
        }

    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        when (flys.get()) {
            "Jump" -> {
                if(boostValue.get()) {
                    if (packet is CPacketPlayer) {
                        packet.onGround = (times >= 5 && !timer.hasTimePassed(boostLength.get().toLong()))
                    }
                }
            }
        }
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= fly.launchY) {
            event.boundingBox = AxisAlignedBB(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
        }
    }
}