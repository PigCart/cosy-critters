//? if fabric {
package pigcart.cosycritters.loaders.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import pigcart.cosycritters.CosyCritters;
import pigcart.cosycritters.Util;
import pigcart.cosycritters.particle.*;

public class FabricEntrypoint implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CosyCritters.BIRD    = registerParticle("bird");
        CosyCritters.HAT_MAN = registerParticle("hat_man");
        CosyCritters.MOTH    = registerParticle("moth");
        CosyCritters.SPIDER  = registerParticle("spider");

        CosyCritters.onInitializeClient();

        ParticleFactoryRegistry.getInstance().register(CosyCritters.BIRD   ,   BirdParticle.Provider::new);
        ParticleFactoryRegistry.getInstance().register(CosyCritters.HAT_MAN, HatManParticle.Provider::new);
        ParticleFactoryRegistry.getInstance().register(CosyCritters.MOTH   ,   MothParticle.Provider::new);
        ParticleFactoryRegistry.getInstance().register(CosyCritters.SPIDER , SpiderParticle.Provider::new);


        ClientTickEvents.END_CLIENT_TICK.register(CosyCritters::onTick);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(CosyCritters.getCommands());
        });
    }
    private SimpleParticleType registerParticle(String name) {
        return Registry.register(
                BuiltInRegistries.PARTICLE_TYPE,
                Util.getId(name),
                FabricParticleTypes.simple(true)
        );
    }
}
//?}