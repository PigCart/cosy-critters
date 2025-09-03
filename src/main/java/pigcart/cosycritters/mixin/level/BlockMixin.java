package pigcart.cosycritters.mixin.level;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {

    @Unique
    public void spawnCritters(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // override this in child classes
        // overriding animateTick directly will conflict with other mods
    }

    @Inject(method = "animateTick", at = @At("HEAD"))
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        this.spawnCritters(state, level, pos, random);
    }
}
