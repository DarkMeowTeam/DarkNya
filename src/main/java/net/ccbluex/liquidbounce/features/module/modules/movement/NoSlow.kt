package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.createOpenInventoryPacket
import net.ccbluex.liquidbounce.utils.createUseItemPacket
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.item.*
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.SPacketWindowItems
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper.floor
import op.wawa.utils.PacketUtils
import op.wawa.utils.PacketUtils.cancelC08
import java.util.*


@ModuleInfo(name = "NoSlow", description = "Cancels slowness effects caused by SoulSand and using items.",
    category = ModuleCategory.MOVEMENT)
class NoSlow : Module() {

    private val modeValue = ListValue("PacketMode", arrayOf("None",
        "Vanilla",
        "GrimAC",
        "NoPacket",
        "FakeBlock",
        "AAC",
        "AAC5",
        "Matrix",
        "Vulcan",
        "Custom"), "GrimAC")
    private val grimFoodValue = ListValue("GrimFoodMode", arrayOf("Inv", "HuaYuTing"), "GrimAC").displayable { modeValue.get() == "GrimAC" }
    private val blockForwardMultiplier = FloatValue("BlockForwardMultiplier", 1.0F, 0.2F, 1.0F)
    private val blockStrafeMultiplier = FloatValue("BlockStrafeMultiplier", 1.0F, 0.2F, 1.0F)
    private val consumeForwardMultiplier = FloatValue("ConsumeForwardMultiplier", 1.0F, 0.2F, 1.0F)
    private val consumeStrafeMultiplier = FloatValue("ConsumeStrafeMultiplier", 1.0F, 0.2F, 1.0F)
    private val bowForwardMultiplier = FloatValue("BowForwardMultiplier", 1.0F, 0.2F, 1.0F)
    private val bowStrafeMultiplier = FloatValue("BowStrafeMultiplier", 1.0F, 0.2F, 1.0F)
    private val customOnGround = BoolValue("CustomOnGround", false)
    private val customDelayValue = IntegerValue("CustomDelay",60,10,200)

    // Soulsand
    val soulsandValue = BoolValue("Soulsand", true)

    val timer = MSTimer()
    private val Timer = MSTimer()
    private var pendingFlagApplyPacket = false
    private val msTimer = MSTimer()
    private var sendBuf = false
    private var packetBuf = LinkedList<Packet<INetHandlerPlayServer>>()
    private var nextTemp = false
    private var waitC03 = false
    private var lastBlockingStat = false

    //GrimAC
    private var isOpenContainer = false
    private val grimTimer = MSTimer()

    private val killAura = DarkNya.moduleManager[KillAura::class.java] as KillAura


    private fun isBlock(): Boolean {
        return mc.player.isActiveItemStackBlocking || killAura.blockingStatus
    }

    private fun onPre(event : MotionEvent): Boolean {
        return event.eventState == EventState.PRE
    }

    private fun onPost(event : MotionEvent): Boolean {
        return event.eventState == EventState.POST
    }

    private val isBlocking: Boolean
        get() = (mc.player!!.isHandActive || (DarkNya.moduleManager[KillAura::class.java] as KillAura).blockingStatus) && mc.player!!.getHeldItem(EnumHand.MAIN_HAND).item is ItemSword

    override fun onDisable() {
        Timer.reset()
        msTimer.reset()
        grimTimer.reset()
        pendingFlagApplyPacket = false
        sendBuf = false
        packetBuf.clear()
        nextTemp = false
        waitC03 = false
        isOpenContainer = false
    }

    private fun sendPacket(Event : MotionEvent,SendC07 : Boolean, SendC08 : Boolean,Delay : Boolean,DelayValue : Long,onGround : Boolean,Hypixel : Boolean = false) {
        val aura = DarkNya.moduleManager[KillAura::class.java] as KillAura
        val digging = CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos(-1,-1,-1),EnumFacing.DOWN)
        val blockPlace = CPacketPlayerTryUseItem(EnumHand.MAIN_HAND)
        val blockMent = CPacketPlayerTryUseItemOnBlock(BlockPos(-1, -1, -1), EnumFacing.values()[255], EnumHand.MAIN_HAND, 0f, 0f, 0f)
        if(onGround && !mc.player!!.onGround) {
            return
        }

        if(SendC07 && onPre(Event)) {
            if(Delay && Timer.hasTimePassed(DelayValue)) {
                mc.connection!!.sendPacket(digging)
            } else if(!Delay) {
                mc.connection!!.sendPacket(digging)
            }
        }
        if(SendC08 && onPost(Event)) {
            if(Delay && Timer.hasTimePassed(DelayValue) && !Hypixel) {
                mc.connection!!.sendPacket(blockPlace)
                Timer.reset()
            } else if(!Delay && !Hypixel) {
                mc.connection!!.sendPacket(blockPlace)
            } else if(Hypixel) {
                mc.connection!!.sendPacket(blockMent)
            }
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val player = mc.player ?: return

        if (!MovementUtils.isMoving) {
            return
        }

        when(modeValue.get().toLowerCase()) {
            "custom" -> {
                sendPacket(event,true,true,true,customDelayValue.get().toLong(),customOnGround.get())
            }
            "vanilla" -> {
                mc.player!!.motionX = mc.player!!.motionX
                mc.player!!.motionY = mc.player!!.motionY
                mc.player!!.motionZ = mc.player!!.motionZ
            }
            "grimac"->{
                if (!player.isActiveItemStackBlocking && !killAura.blockingStatus)
                    return

                if (onPre(event)){
                    mc.connection!!.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
                } else {
                    mc.connection!!.sendPacket(CPacketConfirmTransaction())
                    PacketUtils.sendTryUseItem()
                }
            }
            "aac" -> {
                if (mc.player!!.ticksExisted % 3 == 0) sendPacket(event, true, false, false, 0, false)
                else sendPacket(event, false, true, false, 0, false)

            }
            "aac5" -> {
                if (mc.player!!.isHandActive || mc.player!!.isActiveItemStackBlocking || isBlock()) {
                    mc.connection!!.sendPacket(createUseItemPacket(mc.player!!.inventory.getCurrentItem(), EnumHand.MAIN_HAND))
                    mc.connection!!.sendPacket(createUseItemPacket(mc.player!!.inventory.getCurrentItem(), EnumHand.OFF_HAND))
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        cancelC08(event, packet)
        if(modeValue.equals("Matrix") || modeValue.equals("Vulcan")&& nextTemp) {
            if((packet is CPacketPlayerDigging || packet is CPacketPlayerTryUseItemOnBlock) && isBlocking) {
                event.cancelEvent()
            }
            event.cancelEvent()
        }else if (packet is CPacketPlayer || packet is CPacketAnimation || packet is CPacketEntityAction || packet is CPacketUseEntity || packet is CPacketPlayerDigging || packet is CPacketPlayerTryUseItemOnBlock) {
            if (modeValue.equals("Vulcan") && waitC03 && packet is CPacketPlayer) {
                waitC03 = false
                return
            }
            packetBuf.add(packet as Packet<INetHandlerPlayServer>)
        }
        if (modeValue.equals("FakeBlock")){
            if (isBlocking && packet is CPacketPlayerTryUseItemOnBlock){
                event.cancelEvent()
            }
        }
        if (event.packet is SPacketWindowItems) {
            if (mc.player!!.isHandActive) {
                event.cancelEvent()
            }
        }
    }
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if(modeValue.equals("GrimAC")){
/*          BiliBili BV1sN411E747
            wxcer3 Lv3
            其实就是吃一下瞬间打开背包
            20分钟前 回复*/
            if (mc.currentScreen is GuiContainer && this.isOpenContainer)
                mc.connection!!.sendPacket(CPacketCloseWindow())

            if (mc.player.isHandActive) {
                val item = mc.player.inventory.getCurrentItem().item
                if ((item is ItemFood || item is ItemPotion || item is ItemBucketMilk) && !this.isOpenContainer && grimTimer.hasTimePassed(20)) {
                    //完了发现精髓了send "/report"
                    when (grimFoodValue.get().toLowerCase()){
                        "huayuting" -> mc.player.sendChatMessage("/report")
                        "inv" -> mc.connection!!.sendPacket(createOpenInventoryPacket())
                    }
                    grimTimer.reset()
                    this.isOpenContainer = true
                } else this.isOpenContainer = false
            }
        }

        if((modeValue.equals("Matrix") || modeValue.equals("Vulcan")) && (lastBlockingStat || isBlocking)) {
            if(msTimer.hasTimePassed(230) && nextTemp) {
                nextTemp = false
                CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos(-1, -1, -1), EnumFacing.DOWN)
                if(packetBuf.isNotEmpty()) {
                    var canAttack = false
                    for(packet in packetBuf) {
                        if(packet is CPacketPlayer) {
                            canAttack = true
                        }
                        if(!((packet is CPacketUseEntity || packet is CPacketAnimation) && !canAttack)) {
                            PacketUtils.sendPacketNoEvent(packet)
                        }
                    }
                    packetBuf.clear()
                }
            }
            if(!nextTemp) {
                lastBlockingStat = isBlocking
                if (!isBlocking) {
                    return
                }
                CPacketPlayerTryUseItemOnBlock(BlockPos(-1, -1, -1), EnumFacing.values()[255], EnumHand.MAIN_HAND, 0f, 0f, 0f)
                nextTemp = true
                waitC03 = modeValue.equals("Vulcan")
                msTimer.reset()
            }
        }
    }

    @EventTarget
    fun onSlowDown(event: SlowDownEvent) {
        val heldItem = mc.player!!.getHeldItem(EnumHand.MAIN_HAND)?.item

        event.forward = getMultiplier(heldItem, true)
        event.strafe = getMultiplier(heldItem, false)
    }

    private fun getMultiplier(item: Item?, isForward: Boolean): Float {
        return when {
            (item is ItemFood) || (item is ItemPotion) || (item is ItemBucketMilk) -> {
                if (isForward) this.consumeForwardMultiplier.get() else this.consumeStrafeMultiplier.get()
            }
            (item is ItemSword) -> {
                if (isForward) this.blockForwardMultiplier.get() else this.blockStrafeMultiplier.get()
            }
            (item is ItemBow) -> {
                if (isForward) this.bowForwardMultiplier.get() else this.bowStrafeMultiplier.get()
            }
            else -> 0.2F
        }
    }
    fun getHytBlockpos(): BlockPos {
        val random = java.util.Random()
        val dx = floor(random.nextDouble() / 1000 + 2820)
        val jy = floor(random.nextDouble() / 100 * 0.20000000298023224)
        val kz = floor(random.nextDouble() / 1000 + 2820)
        return BlockPos(dx, -jy % 255, kz)
    }

    override val tag: String?
        get() = modeValue.get()

}
