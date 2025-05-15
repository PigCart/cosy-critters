package pigcart.cosycritters.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import pigcart.cosycritters.CosyCritters;

@Mixin({LeavesBlock.class, WallBlock.class, CrossCollisionBlock.class})
public class BirdSpawningBlockMixin extends BlockMixin {

    @Override
    public void spawnCritters(BlockState state, Level level, BlockPos pos, RandomSource random) {
        CosyCritters.trySpawnBird(state, level, pos);
    }
}
