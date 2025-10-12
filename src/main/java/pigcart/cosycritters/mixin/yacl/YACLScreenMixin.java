package pigcart.cosycritters.mixin.yacl;

import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.cosycritters.CosyCritters;

@Mixin(YACLScreen.class)
public abstract class YACLScreenMixin {

    @Shadow(remap = false) @Final public YetAnotherConfigLib config;

    // workaround for https://github.com/isXander/YetAnotherConfigLib/issues/187
    @Inject( //yacl's mappings are borked
            //? if forge {
            /*method = "m_7379_", at = @At("HEAD"), remap = false)
            *///?} else {
            method = "onClose", at = @At("HEAD"))
            //?}
    public void runSaveFunction(CallbackInfo ci) {
        if (this.config.title().getContents().getClass().equals(TranslatableContents.class)) {
            if (((TranslatableContents) this.config.title().getContents()).getKey().startsWith(CosyCritters.MOD_ID)) {
                this.config.saveFunction().run();
            }
        }
    }
}
