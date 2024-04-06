package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.features.module.modules.client.DebugManage
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.entity.player.EntityPlayer
import net.ccbluex.liquidbounce.utils.entity.DarkNyaPotionUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.init.Enchantments

@ModuleInfo(name = "HighDamageDetector", description = "标记高风险(从力量效果,武器伤害)判断 并在NameTags中显示(橙色)", category = ModuleCategory.PLAYER)
object HighDamageDetector : Module() {

    private val itemEnchantValue = IntegerValue("ItemEnchant",10,1,256)
    private val strengthEffectLevelValue = IntegerValue("StrengthEffectLevel",4,1,255)

    val displayInNameTagsValue = BoolValue("DisplayInNameTags", true)

    private val debugValue = BoolValue("Debug", true)

    var warnPlayers: MutableList<EntityPlayer> = mutableListOf()

    override fun onDisable() {
        warnPlayers.clear()
    }
    @EventTarget
    fun onWorld(event: WorldEvent) {
        warnPlayers.clear()
        if (debugValue.get()) DebugManage.info("高风险玩家标记列表已清空")
    }
    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.PRE) {
            for (player in mc.world.playerEntities) {
                if (player !is EntityPlayer) return
                if (mc.player.ticksExisted % 2 == 0) return
                if (warnPlayers.contains(player)) return

                var addReason = ""

                if (player.heldItemMainhand == null) return // Warn了不要管 不然你会收获一堆Exception


                val enchantLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, player.heldItemMainhand)
                if (enchantLevel >= itemEnchantValue.get()) addReason = "持有高锋利附魔武器(附魔等级:${enchantLevel})"

                val level = DarkNyaPotionUtils.getStrengthEffectLevel(player)
                if (level >= strengthEffectLevelValue.get()) addReason = "拥有高等级力量效果(等级:${level})"

                if (addReason != "") {
                    warnPlayers.add(player)
                    if (debugValue.get()) DebugManage.info("玩家 ${player.name} 因 $addReason 被标记")
                }
            }
        }
    }
}
