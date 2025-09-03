//? if forge {
/*package pigcart.cosycritters.loaders.forge;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import pigcart.cosycritters.CosyCritters;
import pigcart.cosycritters.config.ConfigScreens;
import pigcart.cosycritters.particle.*;

@Mod(CosyCritters.MOD_ID)
public class ForgeEntrypoint {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, CosyCritters.MOD_ID);

    public static final RegistryObject<SimpleParticleType> BIRD    = registerParticle("bird");
    public static final RegistryObject<SimpleParticleType> HAT_MAN = registerParticle("hat_man");
    public static final RegistryObject<SimpleParticleType> MOTH    = registerParticle("moth");
    public static final RegistryObject<SimpleParticleType> SPIDER  = registerParticle("spider");

    private static RegistryObject<SimpleParticleType> registerParticle(String name) {
        return PARTICLE_TYPES.register(name, () -> new SimpleParticleType(true));
    }

    public static void onTick(TickEvent.ClientTickEvent event) {
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

    @SuppressWarnings("removal")
    public ForgeEntrypoint() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.addListener(ForgeEntrypoint::onTick);
        MinecraftForge.EVENT_BUS.addListener(ForgeEntrypoint::onRegisterCommands);
        PARTICLE_TYPES.register(eventBus);
        eventBus.addListener(ForgeEntrypoint::onRegisterParticleProviders);
        CosyCritters.onInitializeClient();
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (client, parent) -> ConfigScreens.generateMainConfigScreen(parent)
                )
        );
    }
}
*///?}