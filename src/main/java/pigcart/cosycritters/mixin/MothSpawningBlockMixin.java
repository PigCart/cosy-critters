package pigcart.cosycritters.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import pigcart.cosycritters.Cosycritters;

import java.lang.reflect.Array;

@Mixin(BaseTorchBlock.class)
public class BaseTorchBlockMixin extends BlockMixin {



    @Override
    public void spawnCritters(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Cosycritters.trySpawnMoth(level, pos);
    }
}
