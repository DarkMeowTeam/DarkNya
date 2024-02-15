package net.ccbluex.liquidbounce.injection.forge.mixins.client;


import net.ccbluex.liquidbounce.DarkNya;
import net.ccbluex.liquidbounce.features.module.modules.movement.InventoryMove;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(KeyBinding.class)
public abstract class MixinKeyBinding {

    @Shadow
    public boolean pressed;

    /**
     * @author CCBlueX
     * @reason DarkNya Inject
     */
    @Overwrite
    public boolean isKeyDown() {
        InventoryMove inventoryMove = (InventoryMove) DarkNya.moduleManager.get(InventoryMove.class);
        boolean InvMove = inventoryMove.getState() ? this.pressed : this.pressed && this.getKeyConflictContext().isActive() && this.getKeyModifier().isActive(this.getKeyConflictContext());
        return InvMove;
    }

    @Shadow
    public abstract IKeyConflictContext getKeyConflictContext();

    @Shadow
    public abstract KeyModifier getKeyModifier();

}
