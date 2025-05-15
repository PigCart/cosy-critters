package pigcart.cosycritters.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseTorchBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import pigcart.cosycritters.CosyCritters;

@Mixin({BaseTorchBlock.class, LanternBlock.class})
public class MothSpawningBlockMixin extends BlockMixin {

    @Override
    public void spawnCritters(BlockState state, Level level, BlockPos pos, RandomSource random) {
        CosyCritters.trySpawnMoth(level, pos);
    }
}
