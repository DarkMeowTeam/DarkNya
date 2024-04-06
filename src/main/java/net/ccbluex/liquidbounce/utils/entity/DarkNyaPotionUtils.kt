package net.ccbluex.liquidbounce.utils.entity

import net.minecraft.entity.EntityLivingBase
import net.minecraft.potion.Potion

object DarkNyaPotionUtils {
    @JvmStatic
    fun getStrengthEffectDuration(entity: EntityLivingBase): Int {
        return getCustomEffectDuration(entity, "strength")
    }
    @JvmStatic
    fun getRegenerationEffectDuration(entity: EntityLivingBase): Int {
        return getCustomEffectDuration(entity, "regeneration")
    }
    @JvmStatic
    fun getCustomEffectDuration(entity: EntityLivingBase, potion: String): Int {
        return Potion.getPotionFromResourceLocation(potion)?.let {
            getCustomEffectDuration(entity, it)
        } ?: 0
    }
    @JvmStatic
    fun getStrengthEffectLevel(entity: EntityLivingBase): Int {
        return getCustomEffectLevel(entity, "strength")
    }
    @JvmStatic
    fun getCustomEffectDuration(entity: EntityLivingBase, potion: Potion): Int {
        return potion.let { strength ->
            entity.activePotionEffects
                .firstOrNull { effect -> effect.potion === strength }
                ?.duration ?: 0
        }
    }
    @JvmStatic
    fun getCustomEffectLevel(entity: EntityLivingBase, potion: String): Int {
        return Potion.getPotionFromResourceLocation(potion)?.let {
            getCustomEffectLevel(entity, it)
        } ?: 0
    }
    @JvmStatic
    fun getCustomEffectLevel(entity: EntityLivingBase, potion: Potion): Int {
        return potion.let { strength ->
            entity.activePotionEffects
                .firstOrNull { effect -> effect.potion === strength }
                ?.amplifier?.plus(1) ?: 0
        }
    }
}