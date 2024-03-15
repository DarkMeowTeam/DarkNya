package net.ccbluex.liquidbounce.injection.forge.mixins.splash;

import net.ccbluex.liquidbounce.ui.cnfont.FontDrawer;
import net.ccbluex.liquidbounce.ui.cnfont.FontLoaders;
import net.ccbluex.liquidbounce.utils.render.AnimatedValue;
import net.ccbluex.liquidbounce.utils.render.EaseUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraftforge.fml.client.SplashProgress;
import net.minecraftforge.fml.common.ProgressManager;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

@Mixin(targets="net.minecraftforge.fml.client.SplashProgress$2", remap=false)
public abstract class MixinSplashProgressRunnable {

    @Shadow(remap = false)
    protected abstract void setGL();

    @Shadow(remap = false)
    protected abstract void clearGL();

    @Inject(method="run()V", at=@At(value="HEAD"), remap=false, cancellable=true)
    private void run(CallbackInfo callbackInfo) {
        callbackInfo.cancel();
        this.setGL();
        GL11.glClearColor(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(3553);

        int tex;

        try {
            tex = RenderUtils.loadGlTexture(ImageIO.read(Objects.requireNonNull(this.getClass().getResourceAsStream("/assets/minecraft/darknya/splash.png"))));
        } catch (IOException ioexception) {
            tex = 0;
        }

        GL11.glDisable(3553);
        AnimatedValue animatedValue = new AnimatedValue();

        animatedValue.setType(EaseUtils.EnumEasingType.CIRC);
        animatedValue.setDuration(600L);

        for (; !SplashProgress.done; Display.sync(60)) {
            GL11.glClear(16384);
            int width = Display.getWidth();
            int height = Display.getHeight();

            GL11.glViewport(0, 0, width, height);
            GL11.glMatrixMode(5889);
            GL11.glLoadIdentity();
            GL11.glOrtho(0.0D, width, height, 0.0D, -1.0D, 1.0D);
            GL11.glMatrixMode(5888);
            GL11.glLoadIdentity();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(3553);
            GL11.glBindTexture(3553, tex);
            GL11.glBegin(7);
            GL11.glTexCoord2f(0.0F, 0.0F);
            GL11.glVertex2f(0.0F, 0.0F);
            GL11.glTexCoord2f(1.0F, 0.0F);
            GL11.glVertex2f((float) width, 0.0F);
            GL11.glTexCoord2f(1.0F, 1.0F);
            GL11.glVertex2f((float) width, (float) height);
            GL11.glTexCoord2f(0.0F, 1.0F);
            GL11.glVertex2f(0.0F, (float) height);
            GL11.glEnd();
            GL11.glDisable(3553);

            drawProgress((int) getProgress());

            FontLoaders.getFont("misans.ttf", 55, true).drawCenteredString("DarkNya", (double) width / 2, (double) height / 2 - 70, Color.WHITE.getRGB(), true);

            SplashProgress.mutex.acquireUninterruptibly();
            Display.update();
            SplashProgress.mutex.release();
            if (SplashProgress.pause) {
                this.clearGL();
                this.setGL();
            }
        }

        GL11.glDeleteTextures(tex);
        this.clearGL();
    }

    private static int getProgress() {
        float progress = 0;
        Iterator<ProgressManager.ProgressBar> it = ProgressManager.barIterator();
        if (it.hasNext()) {
            ProgressManager.ProgressBar bar = it.next();
            progress = bar.getStep() / (float) bar.getSteps();
        }

        return (int) (progress * 100);
    }
    private static void drawProgress(int progress) {
        int width = Display.getWidth();
        int height = Display.getHeight();

        int renderProgress = (int) (((float) width / 100) * progress);

        RenderUtils.drawRect(
                0,
                height - 35,
                width,
                height,
                new Color(0, 0, 0, 50).getRGB()
        );
        String leftString = "   ";
        String rightString = progress + "%";

        FontDrawer font = FontLoaders.getFont("misans.ttf", 48, true);
        // 进度条左上侧文字
        font.drawString(leftString,
                2,
                height - 32,
                Color.WHITE.getRGB(), true);
        // 进度条右上侧文字 1 ~ 100%
        font.drawString(rightString,
                        width - font.getStringWidth(rightString) - 2,
                        height - 32,
                        Color.WHITE.getRGB(), true);
        // 进度条绘制
        RenderUtils.drawRect(
                0,
                height - 8,
                renderProgress,
                height,
                new Color(255, 255, 255).getRGB()
        );
    }
}