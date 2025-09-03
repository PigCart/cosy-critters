package pigcart.cosycritters.config;

import com.google.gson.TypeAdapter;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import pigcart.cosycritters.CosyCritters;

import pigcart.cosycritters.config.ConfigManager.Group;
import pigcart.cosycritters.config.ConfigManager.Dropdown;
import pigcart.cosycritters.config.ConfigManager.NoGUI;
import pigcart.cosycritters.config.ConfigManager.OnChange;
import pigcart.cosycritters.config.ConfigManager.Label;
import pigcart.cosycritters.config.ConfigManager.Percentage;
import pigcart.cosycritters.config.ConfigManager.EditAsString;
import pigcart.cosycritters.config.ConfigManager.OverrideName;
import pigcart.cosycritters.config.ConfigManager.BooleanFormat;


import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class ConfigScreens {

    private static Screen screenToOpen;
    private static String lastScreenInitialized = "";

    public static void regenerateScreen(YACLScreen thisScreen, Supplier<Screen> generator) {
        final String key = ((TranslatableContents) thisScreen.getTitle().getContents()).getKey();
        if (!lastScreenInitialized.equals(key)) {
            lastScreenInitialized = key;
            if (generator != null) Minecraft.getInstance().setScreen(generator.get());
        }
    }

    public static Screen generateScreen(String titleKey, Collection<OptionGroup> groups, Collection<Option<?>> options, Supplier<Screen> generator, Screen parent) {
        final ConfigCategory.Builder categoryBuilder = ConfigCategory.createBuilder();
        if (groups != null && !groups.isEmpty()) categoryBuilder.groups(groups);
        if (options != null && !options.isEmpty()) categoryBuilder.options(options);
        return YetAnotherConfigLib.createBuilder()
                .title(getComponent(titleKey))
                .category(categoryBuilder.name(getComponent(titleKey)).build())
                .save(ConfigManager::saveConfig)
                .screenInit((thisScreen) -> regenerateScreen(thisScreen, generator))
                .build()
                .generateScreen(parent);
    }

    public static Screen generateMainConfigScreen(Screen prevScreen) {
        return generateScreen(
                "title",
                collectGroups(ConfigManager.defaultConfig, ConfigManager.config),
                collectOptions(ConfigManager.defaultConfig, ConfigManager.config),
                ()-> generateMainConfigScreen(prevScreen), prevScreen);
    }

    private static ButtonOption getScreenButtonOption(Component name, String text, Supplier<Screen> screenSupplier) {
        return ButtonOption.createBuilder()
                .name(name)
                .text(Component.literal(text))
                .action((yaclScreen, buttonOption) -> {
                    Minecraft.getInstance().setScreen(screenSupplier.get());
                }).build();
    }
    private static ButtonOption getScreenButtonOption(Component name, String text, Runnable runnable) {
        return ButtonOption.createBuilder()
                .name(name)
                .text(Component.literal(text))
                .action((yaclScreen, buttonOption) -> {
                    runnable.run();
                    Minecraft.getInstance().setScreen(screenToOpen);
                }).build();
    }

    static <T> ButtonOption getListButtonOption(Object instance, Field field, ListOption<T> listOption) {
        String listText = "";
        try {
            listText = cropText(field.get(instance).toString());
        } catch (IllegalAccessException e) {
            CosyCritters.LOGGER.error(e.getMessage());
        }
        return getScreenButtonOption(
                getComponent( instance.getClass().getSimpleName() + "." + field.getName()),
                listText,
                () -> screenToOpen = generateScreen("editList", List.of(listOption), null, null, Minecraft.getInstance().screen));
    }

    @SuppressWarnings("unchecked")
    static List<OptionGroup> collectGroups(Object defaultInstance, Object instance) {
        List<OptionGroup> groups = new ArrayList<>();
        Field[] fields = instance.getClass().getFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Group.class)) {
                if (field.isAnnotationPresent(Dropdown.class)) {
                    final Dropdown annotation = field.getAnnotation(Dropdown.class);
                    try {
                        List<String> strings = ((Supplier<List<String>>) annotation.supplier().getConstructors()[0].newInstance()).get();
                        groups.add(getStringDropdownListOption(defaultInstance, instance, field, strings));

                    } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
                        throw new RuntimeException(e);
                    }
                } else if (field.getType().equals(List.class)) {
                    groups.add(getListOption(defaultInstance, instance, field));
                } else {
                    field.setAccessible(true);
                    try {
                        groups.add(OptionGroup.createBuilder()
                                .name(getComponent(field.getType().getSimpleName()))
                                .description(OptionDescription.of(getComponentWithFallback(field.getName() + ".description")))
                                .options(collectOptions(field.get(defaultInstance), field.get(instance)))
                                .build());
                    } catch (IllegalAccessException e) {
                        CosyCritters.LOGGER.error(e.toString());
                    }
                }
            }
        }
        return groups;
    }

    @SuppressWarnings("unchecked")
    static List<Option<?>> collectOptions(Object defaultInstance, Object instance) {
        List<Option<?>> options = new ArrayList<>();
        Field[] fields = instance.getClass().getFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(NoGUI.class) || field.isAnnotationPresent(Group.class)) continue;
            if (field.isAnnotationPresent(Label.class)) {
                options.add(LabelOption.create(getComponent(instance.getClass().getSimpleName() + "." + field.getDeclaredAnnotation(Label.class).key())));
            }
            field.setAccessible(true);
            final Class<?> type = field.getType();
            if (type.equals(boolean.class)) {
                options.add(getBoolOption(defaultInstance, instance, field).build());
            } else if (type.equals(float.class)) {
                if (field.isAnnotationPresent(Percentage.class)) {
                    options.add(getPercentOption(defaultInstance, instance, field));
                } else {
                    options.add(getFloatOption(defaultInstance, instance, field));
                }
            } else if (type.equals(int.class)) {
                options.add(getIntOption(defaultInstance, instance, field));
            } else if (field.isAnnotationPresent(Dropdown.class)) {
                final Dropdown annotation = field.getAnnotation(Dropdown.class);
                try {
                    List<String> strings = ((Supplier<List<String>>) annotation.supplier().getConstructors()[0].newInstance()).get();
                    if (type.equals(List.class)) {
                        options.add(getStringDropdownListOption(defaultInstance, instance, field, strings));
                    } else {
                        options.add(getStringDropdownOption(defaultInstance, instance, field, strings));
                    }
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else if (type.equals(String.class) || field.isAnnotationPresent(EditAsString.class)) {
                options.add(getStringOption(defaultInstance, instance, field));
            } else if (type.isEnum()) {
                options.add(getEnumOption(defaultInstance, instance, field, type));
            } else if (type.equals(URI.class)) {
                options.add(getLinkButtonOption(instance, field));
            } else if (type.equals(Color.class)) {
                options.add(getColorOption(defaultInstance, instance, field));
            } else if (type.equals(List.class)) {
                ListOption<?> listOption = getListOption(defaultInstance, instance, field);
                if (listOption != null) options.add(getListButtonOption(instance, field, listOption));
            } else if (type.getFields().length != 0) {
                options.add(getObjectOption(defaultInstance, instance, field));
            } else {
                CosyCritters.LOGGER.error("Unable to create option for field {}", field.getName());
            }
        }
        options.add(LabelOption.create(CommonComponents.EMPTY));
        return options;
    }
    private static ListOption<?> getListOption(Object defaultInstance, Object instance, Field field) {
        final Class<?> listType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        if (listType.equals(String.class)) {
            return getStringListOption(defaultInstance, instance, field);
        } else if (listType.isEnum()) {
            return getEnumListOption(defaultInstance, instance, field, listType);
        } else {
            CosyCritters.LOGGER.error("Unable to create list for field {}", field.getName());
            return null;
        }
    }
    private static ButtonOption getObjectOption(Object defaultInstance, Object instance, Field field) {
        try {
            final Object newDefaultInstance = field.get(defaultInstance);
            final Object newInstance = field.get(instance);
            final Component name = getComponent(field.getName());
            String text = "";
            // exclude classes that haven't overriden toString
            if (!newInstance.toString().startsWith(newInstance.getClass().getName())) {
                text = cropText(newInstance.toString());
            }
            return getScreenButtonOption(name, text, ()-> generateScreen(
                    CosyCritters.MOD_ID + "." + field.getName(),
                    collectGroups(newDefaultInstance, newInstance),
                    collectOptions(newDefaultInstance, newInstance),
                    null,
                    Minecraft.getInstance().screen));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static ButtonOption getLinkButtonOption(Object instance, Field field) {
        String groupName = instance.getClass().getSimpleName();
        if (instance.getClass().isAnnotationPresent(OverrideName.class)) {
            groupName = instance.getClass().getAnnotation(OverrideName.class).newName();
        }
        final String fieldName = field.getName();
        try {
            return ButtonOption.createBuilder()
                    .name(getComponent(groupName + "." + fieldName))
                    .description(OptionDescription.of(Component.literal(field.get(instance).toString())))
                    .text(Component.literal(""))
                    .action(((yaclScreen, buttonOption) -> {
                        Minecraft minecraft = Minecraft.getInstance();
                        try {
                            minecraft.setScreen(new ConfirmLinkScreen((result) -> {
                                    try {
                                        if (result) Util.getPlatform().openUri((URI) field.get(instance));
                                    } catch (IllegalAccessException ignored) {}
                                minecraft.setScreen(yaclScreen);
                            }, field.get(instance).toString(), true
                            ));
                        } catch (IllegalAccessException ignored) {}
                    })).build();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    private static Option<Color> getColorOption(Object defaultInstance, Object instance, Field field) {
        return ConfigScreens.<Color>getOptionBuilder(defaultInstance, instance, field)
                .controller(ColorControllerBuilder::create)
                .build();
    }
    @SuppressWarnings("unchecked")
    private static <T extends Enum<T>> Option<T> getEnumOption(Object defaultInstance, Object instance, Field field, Class<?> eClass) {
        return ConfigScreens.<T>getOptionBuilder(defaultInstance, instance, field)
                .controller(opt -> EnumControllerBuilder.create(opt).enumClass((Class<T>) eClass))
                .build();
    }
    @SuppressWarnings("unchecked")
    private static <T extends Enum<T>> ListOption<T> getEnumListOption(Object defaultInstance, Object instance, Field field, Class<?> eClass) {
        return ConfigScreens.<T>getListOptionBuilder(defaultInstance, instance, field)
                .controller(opt -> EnumControllerBuilder.create(opt).enumClass((Class<T>) eClass))
                .build();
    }
    private static ListOption<String> getStringListOption(Object defaultInstance, Object instance, Field field) {
        return ConfigScreens.<String>getListOptionBuilder(defaultInstance, instance, field)
                .controller(StringControllerBuilder::create)
                .initial("")
                .build();
    }
    private static Option.Builder<Boolean> getBoolOption(Object defaultInstance, Object instance, Field field) {
        return ConfigScreens.<Boolean>getOptionBuilder(defaultInstance, instance, field)
                .controller(opt -> {
                    if (field.isAnnotationPresent(BooleanFormat.class)) {
                        return BooleanControllerBuilder.create(opt).formatValue(val -> getComponent(
                                val ? field.getAnnotation(BooleanFormat.class).t()
                                : field.getAnnotation(BooleanFormat.class).f()
                        ));
                    }
                    return BooleanControllerBuilder.create(opt).coloured(true);
                });
    }
    private static Option<Float> getFloatOption(Object defaultInstance, Object instance, Field field) {
        return ConfigScreens.<Float>getOptionBuilder(defaultInstance, instance, field)
                .controller(opt -> FloatFieldControllerBuilder.create(opt)
                        .formatValue(val -> Component.literal(val.toString())))
                .build();
    }
    private static Option<Float> getPercentOption(Object defaultInstance, Object instance, Field field) {
        return ConfigScreens.<Float>getOptionBuilder(defaultInstance, instance, field)
                .controller(opt -> FloatSliderControllerBuilder.create(opt)
                        .range(0f, 1f)
                        .step(0.01f)
                        .formatValue(val -> Component.literal(NumberFormat.getPercentInstance().format(val))))
                .build();
    }
    private static Option<Integer> getIntOption(Object defaultInstance, Object instance, Field field) {
        return ConfigScreens.<Integer>getOptionBuilder(defaultInstance, instance, field)
                .controller(IntegerFieldControllerBuilder::create)
                .build();
    }
    private static Option<String> getStringOption(Object defaultInstance, Object instance, Field field) {
        return ConfigScreens.<String>getOptionBuilder(defaultInstance, instance, field)
                .controller(StringControllerBuilder::create)
                .build();
    }
    private static Option<String> getStringDropdownOption(Object defaultInstance, Object instance, Field field, List<String> strings) {
        BuiltInRegistries.PARTICLE_TYPE.keySet();
        return ConfigScreens.<String>getOptionBuilder(defaultInstance, instance, field)
                .controller(opt -> DropdownStringControllerBuilder.create(opt)
                        .allowAnyValue(false)
                        .allowEmptyValue(false)
                        .values(strings)
                )
                .build();
    }
    private static ListOption<String> getStringDropdownListOption(Object defaultInstance, Object instance, Field field, List<String> strings) {
        BuiltInRegistries.PARTICLE_TYPE.keySet();
        return ConfigScreens.<String>getListOptionBuilder(defaultInstance, instance, field)
                .controller(opt -> DropdownStringControllerBuilder.create(opt)
                        .allowAnyValue(false)
                        .allowEmptyValue(true)
                        .values(strings)
                )
                .initial("")
                .build();
    }

    private static <T> Option.Builder<T> getOptionBuilder(Object defaultInstance, Object instance, Field field) {
        String groupName = instance.getClass().getSimpleName();
        if (instance.getClass().isAnnotationPresent(OverrideName.class)) {
            groupName = instance.getClass().getAnnotation(OverrideName.class).newName();
        }
        final String fieldName = field.getName();
        final Binding<T> binding = getBinding(defaultInstance, instance, field);
        Option.Builder<T> optionBuilder = Option.<T>createBuilder()
                .name(getComponent(groupName + "." + fieldName))
                .description(OptionDescription.of(getComponentWithFallback(groupName + "." + fieldName + ".description")));
        // Prevents changes from being discarded when browsing between screens.
        optionBuilder.stateManager(StateManager.createInstant(binding));
        return optionBuilder;
    }
    @SuppressWarnings("unchecked")
    private static <T> ListOption.Builder<T> getListOptionBuilder(Object defaultGroup, Object group, Field field) {
        final String groupName = group.getClass().getSimpleName();
        final String fieldName = field.getName();
        return ListOption.<T>createBuilder()
                .name(getComponent(groupName + "." + fieldName))
                .description(OptionDescription.of(getComponentWithFallback(groupName + "." + fieldName + ".description")))
                //.minimumNumberOfEntries(1)
                .binding(getBinding(defaultGroup, group, field))
                .initial(() -> {
                    try {
                        Field defaultField = defaultGroup.getClass().getField(field.getName());
                        defaultField.setAccessible(true);
                        return (T) ((List<?>)defaultField.get(defaultGroup)).get(0);
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
    @SuppressWarnings("unchecked")
    private static <T> Binding<T> getBinding(Object defaultGroup, Object group, Field field) {
        T defaultValue;
        try {
            Field defaultField = defaultGroup.getClass().getField(field.getName());
            defaultField.setAccessible(true);
            defaultValue = (T) defaultField.get(defaultGroup);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return Binding.generic(defaultValue, () -> {
            // gets the value from the field and displays it in the controller
            try {
                Object value = field.get(group);
                if (field.isAnnotationPresent(EditAsString.class)) {
                    if (field.getType().equals(List.class)) {
                        List<String> list = new ArrayList<>();
                        for (Object object : (List<Object>) value) {
                            list.add(((TypeAdapter<Object>) ConfigManager.GSON.getAdapter(object.getClass()))
                                    .toJson(object).replace("\"", ""));
                        }
                        return (T) list;
                    }
                    return (T) ((TypeAdapter<Object>) ConfigManager.GSON.getAdapter(field.getType()))
                            .toJson(value).replace("\"", "");
                }
                return (T) value;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }, newVal -> {
            // sets the value of the field from the contents of the controller
            try {
                Object oldVal = field.get(group);
                String oleVal = oldVal == null ? "" : oldVal.toString();
                if (!newVal.toString().equals(oleVal)) {
                    if (field.isAnnotationPresent(EditAsString.class)) {
                        if (field.getType().equals(List.class)) {
                            final Class<?> listType = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                            List<Object> list = new ArrayList<>();
                            for (String string : (List<String>) newVal) {
                                list.add(((TypeAdapter<Object>) ConfigManager.GSON.getAdapter(listType)).fromJson("\"" + string + "\""));
                            }
                            field.set(group, list);
                        } else {
                            field.set(group, ((TypeAdapter<Object>) ConfigManager.GSON.getAdapter(field.getType())).fromJson("\"" + newVal + "\""));
                        }
                    } else {
                        field.set(group, newVal);
                    }
                    final OnChange annotation = field.getDeclaredAnnotation(OnChange.class);
                    if (annotation != null) {
                        ((Runnable) annotation.runnable().getConstructors()[0].newInstance()).run();
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException | IOException e) {
                //throw new RuntimeException(e);
                CosyCritters.LOGGER.error(e.getMessage());
            }
        });
    }

    protected static Component getComponent(String translationKey) {
        return Component.translatable(CosyCritters.MOD_ID + "." + translationKey);
    }
    private static Component getComponentWithFallback(String translationKey) {
        return Component.translatableWithFallback("cosycritters." + translationKey, "");
    }
    private static String cropText(String text) {
        if (text.length() > 42) text = text.substring(0, 42) + "...";
        return text;
    }
}