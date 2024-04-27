package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import io.netty.buffer.Unpooled;
import net.ccbluex.liquidbounce.DarkNya;
import net.ccbluex.liquidbounce.event.EntityMovementEvent;
import net.ccbluex.liquidbounce.features.module.modules.client.SilentDisconnect;
import net.ccbluex.liquidbounce.features.module.modules.misc.NoRotateSet;
import net.ccbluex.liquidbounce.features.special.AntiForge;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.TransferUtils;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketResourcePackStatus;
import net.minecraft.network.play.server.SPacketEntity;
import net.minecraft.network.play.server.SPacketJoinGame;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketResourcePackSend;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.WorldSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {

    @Shadow
    public int currentServerMaxPlayers;
    @Shadow
    @Final
    private NetworkManager netManager;
    @Shadow
    private Minecraft gameController;
    @Shadow
    private WorldClient clientWorldController;

    @Shadow private boolean doneLoadingTerrain;

    @Inject(method = "handleResourcePack", at = @At("HEAD"), cancellable = true)
    private void handleResourcePack(final SPacketResourcePackSend p_handleResourcePack_1_, final CallbackInfo callbackInfo) {
        final String url = p_handleResourcePack_1_.getURL();
        final String hash = p_handleResourcePack_1_.getHash();

        try {
            final String scheme = new URI(url).getScheme();
            final boolean isLevelProtocol = "level".equals(scheme);

            if (!"http".equals(scheme) && !"https".equals(scheme) && !isLevelProtocol)
                throw new URISyntaxException(url, "Wrong protocol");

            if (isLevelProtocol && (url.contains("..") || !url.endsWith("/resources.zip")))
                throw new URISyntaxException(url, "Invalid levelstorage resourcepack path");
        } catch (final URISyntaxException e) {
            ClientUtils.getLogger().error("Failed to handle resource pack", e);
            netManager.sendPacket(new CPacketResourcePackStatus(CPacketResourcePackStatus.Action.FAILED_DOWNLOAD));
            callbackInfo.cancel();
        }
    }
    @Inject(method={"onDisconnect"}, at={@At(value="HEAD")}, cancellable=true)
    private void onDisconnect(ITextComponent reason, CallbackInfo callbackInfo) {
        if (this.gameController.world == null || this.gameController.player == null) return;
        if (DarkNya.moduleManager.getModule(SilentDisconnect.class).getState()) {
            SilentDisconnect.onDisconnect(reason);
            callbackInfo.cancel();
        }
    }
    @Inject(method = "handleJoinGame", at = @At("HEAD"), cancellable = true)
    private void handleJoinGameWithAntiForge(SPacketJoinGame packetIn, final CallbackInfo callbackInfo) {
        if (!AntiForge.enabled || !AntiForge.blockFML || Minecraft.getMinecraft().isIntegratedServerRunning())
            return;

        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, gameController);
        this.gameController.playerController = new PlayerControllerMP(gameController, (NetHandlerPlayClient) (Object) this);
        this.clientWorldController = new WorldClient((NetHandlerPlayClient) (Object) this, new WorldSettings(0L, packetIn.getGameType(), false, packetIn.isHardcoreMode(), packetIn.getWorldType()), packetIn.getDimension(), packetIn.getDifficulty(), this.gameController.mcProfiler);
        this.gameController.gameSettings.difficulty = packetIn.getDifficulty();
        this.gameController.loadWorld(this.clientWorldController);
        this.gameController.player.dimension = packetIn.getDimension();
        this.gameController.displayGuiScreen(new GuiDownloadTerrain());
        this.gameController.player.setEntityId(packetIn.getPlayerId());
        this.currentServerMaxPlayers = packetIn.getMaxPlayers();
        this.gameController.player.setReducedDebug(packetIn.isReducedDebugInfo());
        this.gameController.playerController.setGameType(packetIn.getGameType());
        this.gameController.gameSettings.sendSettingsToServer();
        this.netManager.sendPacket(new CPacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(ClientBrandRetriever.getClientModName())));
        callbackInfo.cancel();
    }

    @Inject(method = "handleEntityMovement", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;onGround:Z"))
    private void handleEntityMovementEvent(SPacketEntity packetIn, final CallbackInfo callbackInfo) {
        final Entity entity = packetIn.getEntity(this.clientWorldController);

        if (entity != null)
            DarkNya.eventManager.callEvent(new EntityMovementEvent(entity));
    }

    /**
     * @author CatX_feitu
     * @reason NoRotateSet / S08 Silent Confirm
    */
    @Overwrite
    public void handlePlayerPosLook(SPacketPlayerPosLook packetIn) {
        final NoRotateSet noRotateSet = (NoRotateSet) DarkNya.moduleManager.getModule(NoRotateSet.class);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, this.gameController);
        EntityPlayer entityplayer = this.gameController.player;
        double d0 = packetIn.getX();
        double d1 = packetIn.getY();
        double d2 = packetIn.getZ();
        float f = packetIn.getYaw();
        float f1 = packetIn.getPitch();

        if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.X)) {
            d0 += entityplayer.posX;
        } else {
            if (!TransferUtils.INSTANCE.getNoMotionSet()) {
                entityplayer.motionX = 0.0D;
            }
        }

        if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.Y)) {
            d1 += entityplayer.posY;
        } else {
            if (!TransferUtils.INSTANCE.getNoMotionSet()) {
                entityplayer.motionY = 0.0D;
            }
        }

        if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.Z)) {
            d2 += entityplayer.posZ;
        } else {
            if (!TransferUtils.INSTANCE.getNoMotionSet()) {
                entityplayer.motionZ = 0.0D;
            }
        }

        TransferUtils.INSTANCE.setNoMotionSet(false);

        if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.X_ROT)) {
            f1 += entityplayer.rotationPitch;
        }

        if (packetIn.getFlags().contains(SPacketPlayerPosLook.EnumFlags.Y_ROT)) {
            f += entityplayer.rotationYaw;
        }

        float overwriteYaw = f;
        float overwritePitch = f1;

        boolean flag = false;

        if (TransferUtils.INSTANCE.getSilentConfirm()) {
            this.netManager.sendPacket(new CPacketConfirmTeleport(packetIn.getTeleportId()));
            this.netManager.sendPacket(new CPacketPlayer.PositionRotation(d0, d1, d2, f, f1, false));
            TransferUtils.INSTANCE.setSilentConfirm(false);
        } else {
            if (Objects.requireNonNull(noRotateSet).getState()) {
                if (!noRotateSet.getNoLoadingValue().get() || this.doneLoadingTerrain) {
                    flag = true;
                    if (!noRotateSet.getOverwriteTeleportValue().get()) {
                        overwriteYaw = entityplayer.rotationYaw;
                        overwritePitch = entityplayer.rotationPitch;
                    }
                }
            }
            if (flag) {
                if (noRotateSet.getRotateValue().get()) {
                    entityplayer.setPositionAndRotation(d0, d1, d2, entityplayer.rotationYaw, entityplayer.rotationPitch);
                } else {
                    entityplayer.setPosition(d0, d1, d2);
                }
            } else {
                entityplayer.setPositionAndRotation(d0, d1, d2, f, f1);
            }
            this.netManager.sendPacket(new CPacketConfirmTeleport(packetIn.getTeleportId()));
            this.netManager.sendPacket(new CPacketPlayer.PositionRotation(entityplayer.posX, entityplayer.getEntityBoundingBox().minY, entityplayer.posZ, overwriteYaw, overwritePitch, false));
        }

        if (!this.doneLoadingTerrain) {
            this.gameController.player.prevPosX = this.gameController.player.posX;
            this.gameController.player.prevPosY = this.gameController.player.posY;
            this.gameController.player.prevPosZ = this.gameController.player.posZ;
            this.doneLoadingTerrain = true;
            this.gameController.displayGuiScreen(null);
        }
    }
}
