package net.ccbluex.liquidbounce.injection.forge.mixins.client;

import net.ccbluex.liquidbounce.DarkNya;
import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.features.module.modules.combat.AutoClicker;
import net.ccbluex.liquidbounce.features.module.modules.exploit.AbortBreaking;
import net.ccbluex.liquidbounce.features.module.modules.exploit.MultiActions;
import net.ccbluex.liquidbounce.features.module.modules.world.FastPlace;
import net.ccbluex.liquidbounce.ui.client.GuiMainMenu;

import net.ccbluex.liquidbounce.utils.CPSCounter;
import net.ccbluex.liquidbounce.utils.render.IconUtils;
import net.ccbluex.liquidbounce.utils.render.MiniMapRegister;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

@Mixin(Minecraft.class)
@SideOnly(Side.CLIENT)
public abstract class MixinMinecraft {

    @Shadow
    public GuiScreen currentScreen;

    @Shadow
    public boolean skipRenderWorld;
    @Shadow
    public RayTraceResult objectMouseOver;
    @Shadow
    public WorldClient world;
    @Shadow
    public EntityPlayerSP player;
    @Shadow
    public ParticleManager effectRenderer;
    @Shadow
    public PlayerControllerMP playerController;
    @Shadow
    public int displayWidth;
    @Shadow
    public int displayHeight;
    @Shadow
    public int rightClickDelayTimer;
    @Shadow
    public GameSettings gameSettings;
    @Shadow
    private int leftClickCounter;
    private long lastFrame = getTime();

    @Inject(method = "run", at = @At("HEAD"))
    private void init(CallbackInfo callbackInfo) {
        if (displayWidth < 1067)
            displayWidth = 1067;

        if (displayHeight < 622)
            displayHeight = 622;
    }

    /*
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;checkGLError(Ljava/lang/String;)V", ordinal = 1, shift = At.Shift.AFTER))
    public void step1(CallbackInfo ci) {
        //GuiSplashProgress.INSTANCE.setProgress(1, "textures");
    }*/
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;checkGLError(Ljava/lang/String;)V", ordinal = 2, shift = At.Shift.AFTER))
    private void startGame(CallbackInfo callbackInfo) {
        DarkNya.INSTANCE.startClient();
    }

    @Inject(method = "createDisplay", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/Display;setTitle(Ljava/lang/String;)V", shift = At.Shift.AFTER))
    private void createDisplay(CallbackInfo callbackInfo) {
        Display.setTitle(DarkNya.CLIENT_NAME + " " + DarkNya.CLIENT_VERSION + " " + DarkNya.CLIENT_SUFFIX);
    }

    @Inject(method = "displayGuiScreen", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;", shift = At.Shift.AFTER))
    private void displayGuiScreen(CallbackInfo callbackInfo) {
        if (currentScreen instanceof net.minecraft.client.gui.GuiMainMenu || (currentScreen != null && currentScreen.getClass().getName().startsWith("net.labymod") && currentScreen.getClass().getSimpleName().equals("ModGuiMainMenu"))) {
            currentScreen = new GuiMainMenu();

            ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
            currentScreen.setWorldAndResolution(Minecraft.getMinecraft(), scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
            skipRenderWorld = false;
        }

        DarkNya.eventManager.callEvent(new ScreenEvent(currentScreen == null ? null : currentScreen));
    }

    @Inject(method = "runGameLoop", at = @At("HEAD"))
    private void runGameLoop(final CallbackInfo callbackInfo) {
        final long currentTime = getTime();
        final int deltaTime = (int) (currentTime - lastFrame);
        lastFrame = currentTime;

        RenderUtils.deltaTime = deltaTime;
    }

    public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    @Inject(method = "runTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;joinPlayerCounter:I", shift = At.Shift.BEFORE))
    private void onTick(final CallbackInfo callbackInfo) {
        DarkNya.eventManager.callEvent(new TickEvent());
    }

    @Inject(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;dispatchKeypresses()V", shift = At.Shift.AFTER))
    private void onKey(CallbackInfo callbackInfo) {
        if (Keyboard.getEventKeyState() && currentScreen == null)
            DarkNya.eventManager.callEvent(new KeyEvent(Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey()));
    }

    @Inject(method = "sendClickBlockToController", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/RayTraceResult;getBlockPos()Lnet/minecraft/util/math/BlockPos;"))
    private void onClickBlock(CallbackInfo callbackInfo) {
        IBlockState blockState = world.getBlockState(objectMouseOver.getBlockPos());

        if (this.leftClickCounter == 0 && blockState.getMaterial() != Material.AIR) {
            DarkNya.eventManager.callEvent(new ClickBlockEvent(objectMouseOver.getBlockPos(), this.objectMouseOver.sideHit));
        }
    }

    @Inject(method = "setWindowIcon", at = @At("HEAD"), cancellable = true)
    private void setWindowIcon(CallbackInfo callbackInfo) {
        if (Util.getOSType() != Util.EnumOS.OSX) {
            final ByteBuffer[] liquidBounceFavicon = IconUtils.getFavicon();
            if (liquidBounceFavicon != null) {
                Display.setIcon(liquidBounceFavicon);
                callbackInfo.cancel();
            }
        }
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    private void shutdown(CallbackInfo callbackInfo) {
        DarkNya.INSTANCE.stopClient();
    }

    @Inject(method = "clickMouse", at = @At("HEAD"))
    private void clickMouse(CallbackInfo callbackInfo) {
        CPSCounter.registerClick(CPSCounter.MouseButton.LEFT);

        if (DarkNya.moduleManager.getModule(AutoClicker.class).getState())
            leftClickCounter = 0;
    }

    @Inject(method = "middleClickMouse", at = @At("HEAD"))
    private void middleClickMouse(CallbackInfo ci) {
        CPSCounter.registerClick(CPSCounter.MouseButton.MIDDLE);
    }

    @Inject(method = "rightClickMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;rightClickDelayTimer:I", shift = At.Shift.AFTER))
    private void rightClickMouse(final CallbackInfo callbackInfo) {
        CPSCounter.registerClick(CPSCounter.MouseButton.RIGHT);

        final FastPlace fastPlace = (FastPlace) DarkNya.moduleManager.getModule(FastPlace.class);

        if (fastPlace.getState())
            rightClickDelayTimer = fastPlace.getSpeedValue().get();
    }

    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At("HEAD"))
    private void loadWorld(WorldClient p_loadWorld_1_, String p_loadWorld_2_, final CallbackInfo callbackInfo) {
        if (world != null) {
            MiniMapRegister.INSTANCE.unloadAllChunks();
        }

        DarkNya.eventManager.callEvent(new WorldEvent(p_loadWorld_1_));
    }

    /**
     * @author CCBlueX
     * @reason DarkNya Inject
     */
    @Overwrite
    private void sendClickBlockToController(boolean leftClick) {
        if (!leftClick)
            this.leftClickCounter = 0;

        if (this.leftClickCounter <= 0 && (!this.player.isHandActive() || DarkNya.moduleManager.getModule(MultiActions.class).getState())) {
            if (leftClick && this.objectMouseOver != null && this.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
                BlockPos blockPos = this.objectMouseOver.getBlockPos();

                if (this.leftClickCounter == 0)
                    DarkNya.eventManager.callEvent(new ClickBlockEvent(blockPos, this.objectMouseOver.sideHit));


                IBlockState bs = this.world.getBlockState(blockPos);

                if (bs.getMaterial() != Material.AIR && this.playerController.onPlayerDamageBlock(blockPos, this.objectMouseOver.sideHit)) {
                    this.effectRenderer.addBlockHitEffects(blockPos, this.objectMouseOver.sideHit);
                    this.player.swingArm(EnumHand.MAIN_HAND);
                }
            } else if (!DarkNya.moduleManager.getModule(AbortBreaking.class).getState()) {
                this.playerController.resetBlockRemoving();
            }
        }
    }

    /**
     * patcher
     */
    @ModifyConstant(method = "getLimitFramerate", constant = @Constant(intValue = 30))
    private int removeLimitFramerate(int constant) {
        return 60;
    }
}