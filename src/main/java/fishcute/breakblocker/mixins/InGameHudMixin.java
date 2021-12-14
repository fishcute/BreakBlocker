package fishcute.breakblocker.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import fishcute.breakblocker.BreakBlocker;
import fishcute.breakblocker.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "renderCrosshair", at = @At("HEAD"))
    public void renderCrosshairHead(MatrixStack matrices, CallbackInfo info) {
        if (BreakBlocker.CONFIG.getIndicatorType().equals(Config.Indicator.CROSSHAIR)&& BreakBlocker.disabledBlock())
            RenderSystem.setShaderColor(
                    BreakBlocker.CONFIG.getR(),
                    BreakBlocker.CONFIG.getG(),
                    BreakBlocker.CONFIG.getB(),
                    BreakBlocker.CONFIG.getA());
    }
    @Inject(method = "renderCrosshair", at = @At("RETURN"))
    public void renderCrosshairReturn(MatrixStack matrices, CallbackInfo info) {
        renderBlocked();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }
    private static void renderBlocked() {
        if (!MinecraftClient.getInstance().options.hudHidden&&
                MinecraftClient.getInstance().options.getPerspective().isFirstPerson()&&
                BreakBlocker.CONFIG.getIndicatorType().equals(Config.Indicator.ICON)&&
                BreakBlocker.disabledBlock()) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableTexture();
            int x = (MinecraftClient.getInstance().getWindow().getScaledWidth() / 2) + BreakBlocker.CONFIG.getIndicatorX();
            int y = (MinecraftClient.getInstance().getWindow().getScaledHeight() / 2) + BreakBlocker.CONFIG.getIndicatorY();
            int size = BreakBlocker.CONFIG.getIndicatorSize();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            RenderSystem.setShaderTexture(0, Config.TEXTURE);
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            bufferBuilder.vertex(0.0D + x, size + y, -90.0D).texture(0.0F, 1.0F).next();
            bufferBuilder.vertex(size + x, size + y, -90.0D).texture(1.0F, 1.0F).next();
            bufferBuilder.vertex(size + x, 0.0D + y, -90.0D).texture(1.0F, 0.0F).next();
            bufferBuilder.vertex(0.0D + x, 0.0D + y, -90.0D).texture(0.0F, 0.0F).next();
            tessellator.draw();
            RenderSystem.disableTexture();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        }
    }
}
