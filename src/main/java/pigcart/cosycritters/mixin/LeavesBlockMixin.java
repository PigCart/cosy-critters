package pigcart.cosycritters.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.cosycritters.Cosycritters;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin {

    @Inject(method = "animateTick", at = @At("HEAD"))
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        Cosycritters.spawnBird(state, level, pos);
    }
}
