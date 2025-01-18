package pigcart.cosycritters.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import pigcart.cosycritters.Cosycritters;

@Mixin(CrossCollisionBlock.class)
public abstract class CrossCollisionBlockMixin extends Block implements SimpleWaterloggedBlock {

    public CrossCollisionBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        Cosycritters.trySpawnBird(state, level, pos);
    }
}
