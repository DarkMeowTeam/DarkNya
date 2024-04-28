package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.DarkNya
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.item.ItemUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Enchantments
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.CPacketHeldItemChange


@ModuleInfo(name = "ArmorBreaker", category = ModuleCategory.COMBAT, description = "BreakArmor")
class ArmorBreaker : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Grim","Vanilla"), "Grim")
    private val onlyPlayerValue = BoolValue("OnlyPlayer", true)
    private val debug = BoolValue("Debug", true)
    private val switch = BoolValue("Switch", true)
    private var docriArmorBreaker = false
    private var target: EntityLivingBase? = null

    // private val onlyattackblockingplayer = BoolValue("OnlyAttackBlockingPlayer", true)


    @EventTarget
    fun onUpdate(update: UpdateEvent) {
        val aura = DarkNya.moduleManager[KillAura::class.java] as KillAura
        val thePlayer = mc.player ?: return
        //  if (aura.target==null && aura.state && mc2.player.ticksExisted%19==0)
        //     mc.netHandler.addToSendQueue(classProvider.createCPacketHeldItemChange(mc.thePlayer!!.inventory.currentItem))

        if (!aura.state) return
        target = aura.target
        /*val weaponSlots = (0 .. 8)
            .mapNotNull { slot -> thePlayer.inventory.getStackInSlot(slot)?.let { Pair(slot, it) } }
            .filter { (slot, stack) -> !classProvider.isItemAxe(stack.item) }
            .sortedBy { (slot, stack) ->
                val attribute = stack.getAttributeModifier("generic.attackDamage").first()
                val baseDamage = attribute.amount
                val enchantmentDamage = ItemUtils.getEnchantment(stack, classProvider.getEnchantmentEnum(EnchantmentType.SHARPNESS)) * 1.25
                baseDamage + enchantmentDamage
            }
神秘原因排序不报错但是不执行只能使用lowiq切换物品
         */
        docriArmorBreaker = ((!onlyPlayerValue.get() || target!! is EntityPlayer) && target!!.isHandActive && !mc.gameSettings.keyBindUseItem.pressed && !aura.autoBlockValue.get().equals("Safe", true))
        if (switch.get()){
            val (weaponSlot, _) = (0..8)
            .map { Pair(it, thePlayer.inventory.getStackInSlot(it)) }
            .filter { it.second != null && it.second?.item is ItemSword }
                .maxBy {
                    it.second!!.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).get(SharedMonsterAttributes.ATTACK_DAMAGE.name)
                        .first().amount + 1.25 * ItemUtils.getEnchantment(
                        it.second,
                        Enchantments.SHARPNESS
                    )
                } ?: return
            if (/*aura.swingcounts<21 && */target!! is EntityPlayer && target!!.isHandActive) {
                if (target!!.hurtTime <= 1) {
                    mc.connection?.sendPacket(CPacketHeldItemChange(8))
                    if (debug.get())
                        ClientUtils.displayChatMessage("§aBreak Armor Succeed")
                }
                if (target!!.hurtTime == 9) {
                    val (weaponSlot2, _) = (0..8)
                        .map { Pair(it, thePlayer.inventory.getStackInSlot(it)) }
                        .filter { it.second != null && it.second?.item is ItemSword  }
                        .minBy {
                            it.second!!.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).get(SharedMonsterAttributes.ATTACK_DAMAGE.name)
                                .first().amount + 1.25 * ItemUtils.getEnchantment(
                                it.second,
                                Enchantments.SHARPNESS
                            )
                        } ?: return
                    mc.connection?.sendPacket(CPacketHeldItemChange(weaponSlot2))
                    if (debug.get())
                        ClientUtils.displayChatMessage("§aStart Breaking Armor")
                }
                if (target!!.hurtTime in 2..8) {
                    mc.connection?.sendPacket(CPacketHeldItemChange(weaponSlot))
                    if (debug.get())
                        ClientUtils.displayChatMessage("§aBreak Armor Succeed")
                }
            }else{
                if (aura.target!=null) {
                    thePlayer.inventory.currentItem = weaponSlot
                    mc.connection?.sendPacket(CPacketHeldItemChange(weaponSlot))
                    mc.playerController.updateController()
                }
            }
    }else{
            val (weaponSlot, _) = (0..8)
                .map { Pair(it, thePlayer.inventory.getStackInSlot(it)) }
                .filter { it.second != null &&  it.second?.item is ItemSword }
                .maxBy {
                    it.second!!.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).get(SharedMonsterAttributes.ATTACK_DAMAGE.name)
                        .first().amount + 1.25 * ItemUtils.getEnchantment(
                        it.second,
                        Enchantments.SHARPNESS
                    )
                } ?: return
            if (aura.target!=null) {
                thePlayer.inventory.currentItem = weaponSlot
                mc.playerController.updateController()
            }
        }
    }
    override val tag: String
        get() = modeValue.get()
}