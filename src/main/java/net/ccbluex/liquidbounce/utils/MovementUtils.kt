package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.injection.implementations.IMixinTimer
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.AxisAlignedBB
import java.math.BigDecimal
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object MovementUtils : MinecraftInstance() {
    var bps = 0.0
    private  var lastX:Double = 0.0
    private  var lastY:Double = 0.0
    private  var lastZ:Double = 0.0
    val speed: Float
        get() = sqrt(mc.player!!.motionX * mc.player!!.motionX + mc.player!!.motionZ * mc.player!!.motionZ).toFloat()

    @JvmStatic
    val isMoving: Boolean
        get() = mc.player != null && (mc.player!!.movementInput.moveForward != 0f || mc.player!!.movementInput.moveStrafe != 0f)

    fun resetMotion(y: Boolean) {
        val player = mc.player ?: return

        player.motionX = 0.0
        player.motionZ = 0.0
        if(y) mc.player.motionY = 0.0
    }
    fun hasMotion(): Boolean {
        return mc.player!!.motionX != 0.0 && mc.player!!.motionZ != 0.0 && mc.player!!.motionY != 0.0
    }
    val movingYaw: Float
        get() = (direction * 180f / Math.PI).toFloat()

    @JvmStatic
    fun doTargetStrafe(curTarget: EntityLivingBase, direction_: Float, radius: Float, moveEvent: MoveEvent, mathRadius: Int = 0) {
        if(!isMoving) return

        var forward_ = 0.0
        var strafe_ = 0.0
        val speed_ = sqrt(moveEvent.x * moveEvent.x + moveEvent.z * moveEvent.z)

        if(speed_ <= 0.0001)
            return

        var _direction = 0.0
        if(direction_ > 0.001) {
            _direction = 1.0
        }else if(direction_ < -0.001) {
            _direction = -1.0
        }
        var curDistance = (0.01).toFloat()
        if (mathRadius == 1) {
            curDistance = mc.player.getDistanceToEntityBox(curTarget).toFloat()
        }else if (mathRadius == 0) {
            curDistance = sqrt((mc.player.posX - curTarget.posX) * (mc.player.posX - curTarget.posX) + (mc.player.posZ - curTarget.posZ) * (mc.player.posZ - curTarget.posZ)).toFloat()
        }
        if(curDistance < radius - speed_) {
            forward_ = -1.0
        }else if(curDistance > radius + speed_) {
            forward_ = 1.0
        }else {
            forward_ = (curDistance - radius) / speed_
        }
        if(curDistance < radius + speed_*2 && curDistance > radius - speed_*2) {
            strafe_ = 1.0
        }
        strafe_ *= _direction
        var strafeYaw = RotationUtils.getRotationsEntity(curTarget).yaw.toDouble()
        val covert_ = sqrt(forward_ * forward_ + strafe_ * strafe_)

        forward_ /= covert_
        strafe_ /= covert_
        var turnAngle = Math.toDegrees(asin(strafe_))
        if(turnAngle > 0) {
            if(forward_ < 0)
                turnAngle = 180F - turnAngle
        }else {
            if(forward_ < 0)
                turnAngle = -180F - turnAngle
        }
        strafeYaw = Math.toRadians((strafeYaw + turnAngle))
        moveEvent.x = -sin(strafeYaw) * speed_
        moveEvent.z = cos(strafeYaw) * speed_
        mc.player.motionX = moveEvent.x
        mc.player.motionZ = moveEvent.z
    }

    @JvmStatic
    @JvmOverloads
    fun strafe(speed: Float = this.speed) {
        if (!isMoving) return
        val yaw = direction
        val thePlayer = mc.player!!
        thePlayer.motionX = -sin(yaw) * speed
        thePlayer.motionZ = cos(yaw) * speed
    }

    @JvmStatic
    fun forward(length: Double) {
        val thePlayer = mc.player!!
        val yaw = Math.toRadians(thePlayer.rotationYaw.toDouble())
        thePlayer.setPosition(thePlayer.posX + -sin(yaw) * length, thePlayer.posY, thePlayer.posZ + cos(yaw) * length)
    }
    fun updateBlocksPerSecond() {
        if (mc.player == null || mc.player!!.ticksExisted < 1) {
            bps = 0.0
        }
        val distance = mc.player!!.getDistance(lastX, lastY, lastZ)
        lastX = mc.player!!.posX
        lastY = mc.player!!.posY
        lastZ = mc.player!!.posZ
        bps = distance * (20 * (mc.timer as IMixinTimer).timerSpeed)
    }
    fun getBlockSpeed(entityIn: EntityLivingBase): Double {
        return BigDecimal.valueOf(
            Math.sqrt(
                Math.pow(
                    entityIn.posX - entityIn.prevPosX,
                    2.0
                ) + Math.pow(entityIn.posZ - entityIn.prevPosZ, 2.0)
            ) * 20
        ).setScale(1, BigDecimal.ROUND_HALF_UP).toDouble()
    }
    @JvmStatic
    val direction: Double
        get() {
            val thePlayer = mc.player!!
            var rotationYaw = thePlayer.rotationYaw
            if (thePlayer.moveForward < 0f) rotationYaw += 180f
            var forward = 1f
            if (thePlayer.moveForward < 0f) forward = -0.5f else if (thePlayer.moveForward > 0f) forward = 0.5f
            if (thePlayer.moveStrafing > 0f) rotationYaw -= 90f * forward
            if (thePlayer.moveStrafing < 0f) rotationYaw += 90f * forward
            return Math.toRadians(rotationYaw.toDouble())
        }
    fun getRawDirection(): Float {
        return getRawDirectionRotation(mc.player.rotationYaw, mc.player.moveStrafing, mc.player.moveForward)
    }
    fun getRawDirectionRotation(yaw: Float, pStrafe: Float, pForward: Float): Float {
        var rotationYaw = yaw
        if (pForward < 0f) rotationYaw += 180f
        var forward = 1f
        if (pForward < 0f) forward = -0.5f else if (pForward > 0f) forward = 0.5f
        if (pStrafe > 0f) rotationYaw -= 90f * forward
        if (pStrafe < 0f) rotationYaw += 90f * forward
        return rotationYaw
    }
    @JvmStatic
    fun getScaffoldRotation(yaw: Float, strafe: Float): Float {
        var rotationYaw = yaw
        rotationYaw += 180f
        val forward = -0.5f
        if (strafe < 0f) rotationYaw -= 90f * forward
        if (strafe > 0f) rotationYaw += 90f * forward
        return rotationYaw
    }


    fun handleVanillaKickBypass() {
        val player = mc.player ?: return
        val connection = mc.connection ?: return
        
        val ground = calculateGround()
        run {
            var posY = mc.player.posY
            while (posY > ground) {
                mc.connection?.sendPacket(CPacketPlayer.Position(mc.player.posX, posY, player.posZ, true))
                if (posY - 8.0 < ground) break // Prevent next step
                posY -= 8.0
            }
        }

        connection.sendPacket(CPacketPlayer.Position(player.posX, ground, player.posZ, true))
        var posY = ground
        while (posY < player.posY) {
            connection.sendPacket(CPacketPlayer.Position(player.posX, posY, player.posZ, true))
            if (posY + 8.0 > player.posY) break // Prevent next step
            posY += 8.0
        }
        connection.sendPacket(
            CPacketPlayer.Position(
                player.posX,
                player.posY,
                player.posZ,
                true
            )
        )
    }
    private fun calculateGround(): Double {
        val player = mc.player ?: return 0.0
        val world = mc.world ?: return 0.0
        
        val playerBoundingBox = player.entityBoundingBox
        var blockHeight = 1.0
        var ground = player.posY
        while (ground > 0.0) {
            val customBox = AxisAlignedBB(
                playerBoundingBox.maxX,
                ground + blockHeight,
                playerBoundingBox.maxZ,
                playerBoundingBox.minX,
                ground,
                playerBoundingBox.minZ
            )
            if (world.checkBlockCollision(customBox)) {
                if (blockHeight <= 0.05) return ground + blockHeight
                ground += blockHeight
                blockHeight = 0.05
            }
            ground -= blockHeight
        }
        return 0.0
    }
}