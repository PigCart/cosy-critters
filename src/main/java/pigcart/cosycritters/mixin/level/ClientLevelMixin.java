package pigcart.cosycritters.mixin.level;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.cosycritters.CosyCritters;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Inject(method = "doAnimateTick", at = @At("TAIL"))
    public void hookDoAnimateTick(
            int posX, int posY, int posZ, //player position
            int range, // called twice with ranges 16 & 32
            RandomSource random,
            Block block, // 'marker particle target' like barrier blocks i guess?
            BlockPos.MutableBlockPos blockPos, // set in method body
            CallbackInfo ci,
            @Local BlockState state // set in method body
    ) {
        CosyCritters.doAnimateTick(blockPos, state);
    }

}
