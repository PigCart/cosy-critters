//? if neoforge {
/*package pigcart.cosycritters.loaders.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import pigcart.cosycritters.CosyCritters;
import pigcart.cosycritters.config.ConfigManager;
import pigcart.cosycritters.particle.*;

@Mod(CosyCritters.MOD_ID)
public class NeoforgeEntrypoint {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, CosyCritters.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BIRD    = registerParticle("bird");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HAT_MAN = registerParticle("hat_man");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MOTH    = registerParticle("moth");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SPIDER  = registerParticle("spider");

    private static DeferredHolder<ParticleType<?>, SimpleParticleType> registerParticle(String name) {
        return PARTICLE_TYPES.register(name, () -> new SimpleParticleType(true));
    }

    public static void onTick(ClientTickEvent.Post event) {
        CosyCritters.onTick(Minecraft.getInstance());
    }

    public static void onRegisterCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(CosyCritters.getCommands());
    }

    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(BIRD   .get(),   BirdParticle.Provider::new);
        event.registerSpriteSet(HAT_MAN.get(), HatManParticle.Provider::new);
        event.registerSpriteSet(MOTH   .get(),   MothParticle.Provider::new);
        event.registerSpriteSet(SPIDER .get(), SpiderParticle.Provider::new);
        CosyCritters.BIRD    = BIRD   .get();
        CosyCritters.HAT_MAN = HAT_MAN.get();
        CosyCritters.MOTH    = MOTH   .get();
        CosyCritters.SPIDER  = SPIDER .get();
    }

    public NeoforgeEntrypoint(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(NeoforgeEntrypoint::onTick);
        NeoForge.EVENT_BUS.addListener(NeoforgeEntrypoint::onRegisterCommands);
        PARTICLE_TYPES.register(eventBus);
        eventBus.addListener(NeoforgeEntrypoint::onRegisterParticleProviders);
        CosyCritters.onInitializeClient();
        ModLoadingContext.get().registerExtensionPoint(
                IConfigScreenFactory.class,
                () -> (modContainer, lastScreen) -> ConfigManager.screenPlease(lastScreen)
        );
    }
}
*///?}