package fishcute.breakblocker.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import fishcute.breakblocker.BreakBlocker;
import fishcute.breakblocker.Config;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow
    private static void drawShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {}
    @Inject(method = "drawBlockOutline", cancellable = true, at = @At("HEAD"))
    public void drawBlockOutlineBefore(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double d, double e, double f, BlockPos blockPos, BlockState blockState, CallbackInfo info) {
        if (BreakBlocker.CONFIG.getIndicatorType().equals(Config.Indicator.OUTLINE)&&BreakBlocker.blockedList.contains(BreakBlocker.getId(blockState.getBlock()))) {
            info.cancel();
            drawShapeOutline(matrices, vertexConsumer, blockState.getOutlineShape(MinecraftClient.getInstance().world, blockPos, ShapeContext.of(entity)), (double) blockPos.getX() - d, (double) blockPos.getY() - e, (double) blockPos.getZ() - f,
                    BreakBlocker.CONFIG.getR(),
                    BreakBlocker.CONFIG.getG(),
                    BreakBlocker.CONFIG.getB(),
                    BreakBlocker.CONFIG.getA());
        }
    }
}
