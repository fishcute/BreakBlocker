package fishcute.breakblocker.mixins;

import fishcute.breakblocker.BreakBlocker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    protected int attackCooldown;
    @Inject(method = "handleBlockBreaking", cancellable = true, at = @At("HEAD"))
    public void handleBlockBreaking(boolean bl, CallbackInfo info) {
        //This can probably be redone
        info.cancel();
        if (!BreakBlocker.disabledBlock()) {
            if (!bl) {
                attackCooldown = 0;
            }

            if (attackCooldown <= 0 &&
                    !((MinecraftClient) (Object) this).player.isUsingItem()) {
                if (bl && ((MinecraftClient) (Object) this).crosshairTarget != null && ((MinecraftClient) (Object) this).crosshairTarget.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
                    BlockHitResult blockHitResult = (BlockHitResult) ((MinecraftClient) (Object) this).crosshairTarget;
                    BlockPos blockPos = blockHitResult.getBlockPos();
                    if (!((MinecraftClient) (Object) this).world.getBlockState(blockPos).isAir()) {
                        Direction direction = blockHitResult.getSide();
                        if (((MinecraftClient) (Object) this).interactionManager.updateBlockBreakingProgress(blockPos, direction)) {
                            ((MinecraftClient) (Object) this).particleManager.addBlockBreakingParticles(blockPos, direction);
                            ((MinecraftClient) (Object) this).player.swingHand(Hand.MAIN_HAND);
                        }
                    }
                } else {
                    ((MinecraftClient) (Object) this).interactionManager.cancelBlockBreaking();
                }
            }
        }
        else {
            attackCooldown = BreakBlocker.CONFIG.getCooldown();
            if (bl && ((MinecraftClient) (Object) this).crosshairTarget != null && ((MinecraftClient) (Object) this).crosshairTarget.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
                BreakBlocker.playMiningAnimation((BlockHitResult) ((MinecraftClient) (Object) this).crosshairTarget);
            }
        }
    }
}
