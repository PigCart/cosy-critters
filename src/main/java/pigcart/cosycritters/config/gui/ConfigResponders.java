package pigcart.cosycritters.config.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import pigcart.cosycritters.mixin.access.ParticleEngineAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ConfigResponders {

    public static abstract class RegistrySupplier implements Supplier<List<String>> {
        public static <T> Stream<TagKey<T>> getTagIds(Registry<T> registry) {
            //? if >=1.21.4 {
            /*return registry.listTagIds();
            *///?} else {
            return registry.getTagNames();
             //?}
        }

        public static List<String> getRegistryEntries(Registry<?> registry) {
            List<String> list = new ArrayList<>();
            registry.keySet().forEach((id)-> list.add(id.toString()));
            getTagIds(registry).forEach((tag)-> list.add("#" + tag.location()));
            return list;
        }
    }

    public static class SupplyBlocks extends RegistrySupplier {
        public List<String> get() {
            if (Minecraft.getInstance().level == null) return List.of("[!] §e§l" + Component.translatable("particlerain.suggest").getString());
            return getRegistryEntries(BuiltInRegistries.BLOCK);
        }
    }

    public static class SupplyBiomes extends RegistrySupplier {
        public List<String> get() {
            if (Minecraft.getInstance().level == null) return List.of("[!] §e§l" + Component.translatable("particlerain.suggest").getString());
            return getRegistryEntries(getRegistry(Registries.BIOME));
        }
        public static <T> Registry<T> getRegistry(ResourceKey<Registry<T>> key) {
            //? if >=1.21.4 {
            /*return Minecraft.getInstance().level.registryAccess().lookupOrThrow(key);
            *///?} else {
            return Minecraft.getInstance().level.registryAccess().registryOrThrow(key);
             //?}
        }
    }

    public static class ResetParticles implements Runnable {
        @Override
        public void run() {
            ((ParticleEngineAccessor) Minecraft.getInstance().particleEngine).callClearParticles();
        }
    }

    public static class DistanceInBlocks implements Function<Object, Component> {
        public Component apply(Object stringValue) {
            return Component.literal(stringValue + " ").append(Component.translatable("cosycritters.distanceInblocks"));
        }
    }

    public static class TimeInTicks implements Function<Object, Component> {
        public Component apply(Object stringValue) {
            try {
                int i = Integer.parseInt((String) stringValue);
                return Component.literal(stringValue + " ")
                        .append(Component.translatable("cosycritters.timeInTicks", i / 20F));
            } catch (NumberFormatException e) {
                return Component.literal((String) stringValue);
            }
        }
    }
}
