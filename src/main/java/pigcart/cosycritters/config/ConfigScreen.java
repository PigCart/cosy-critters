package pigcart.cosycritters.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import pigcart.cosycritters.CosyCritters;
import pigcart.cosycritters.config.Annotations.*;
import pigcart.cosycritters.config.widget.Widgets;

//? if >=1.21.1 {
/*import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.layouts.LinearLayout;
*///?} else {
import net.minecraft.client.gui.screens.OptionsSubScreen;
//?}

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import java.util.function.Function;

/// Based on [net.minecraft.client.gui.screens.VideoSettingsScreen]
public class ConfigScreen extends OptionsSubScreen {

    Object config;

    public ConfigScreen(Screen lastScreen, Object config, Component title) {
        super(lastScreen, Minecraft.getInstance().options, title);
        this.config = config;
    }

    //? if <1.21.1 {
    OptionsList list;

    protected void init() {
        // add contents
        this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
        addOptions();
        this.addWidget(this.list);
        // add footer
        this.addRenderableWidget(resetButton());
        this.addRenderableWidget(doneButton());
    }
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.basicListRender(guiGraphics, this.list, mouseX, mouseY, partialTick);
    }
    //?} else {
    /*protected void addFooter() {
        LinearLayout footerLayout = new LinearLayout(0, 0, LinearLayout.Orientation.HORIZONTAL).spacing(8);
        footerLayout.addChild(resetButton());
        footerLayout.addChild(doneButton());
        this.layout.addToFooter(footerLayout);
    }
    *///?}

    private Button resetButton() {
        return Button.builder(
                Component.translatable("controls.reset"),
                (button) -> {
                    CosyCritters.LOGGER.info("Reset config");
                    ConfigManager.config = new ConfigData();
                    minecraft.setScreen(ConfigManager.screenPlease(lastScreen));
                }
        ).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build();
    }
    private Button doneButton() {
        return Button.builder(
                CommonComponents.GUI_DONE,
                (button) -> this.onClose()
        ).bounds(this.width / 2 - 155 + 160, this.height - 28, 150, 20).build();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
        ConfigManager.save();
    }

    @SuppressWarnings("unchecked")
    protected void addOptions() {
        Field[] fields = config.getClass().getFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(NoGUI.class)) continue;
            if (field.isAnnotationPresent(OnlyVisibleIf.class)) {
                final OnlyVisibleIf annotation = field.getAnnotation(OnlyVisibleIf.class);
                try {
                    final Function<Object, Boolean> function = (Function<Object, Boolean>) annotation.value().getConstructors()[0].newInstance();
                    boolean optionIsVisible = function.apply(config);
                    if (!optionIsVisible) continue;
                } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
            }
            if (field.isAnnotationPresent(Label.class)) {
                addOption(Widgets.getLabel(Component.translatable(field.getDeclaredAnnotation(Label.class).key())));
            }
            String name = CosyCritters.MOD_ID + "." + field.getName();
            field.setAccessible(true);
            final Class<?> type = field.getType();
            Consumer onValueChange = (value) -> {
                try {
                    field.set(config, value);
                    if (field.isAnnotationPresent(OnChange.class)) {
                        final OnChange onChange = field.getDeclaredAnnotation(OnChange.class);
                        if (onChange != null) {
                            ((Runnable) onChange.value().getConstructors()[0].newInstance()).run();
                        }
                    }
                } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
            };
            try {
                if (type.equals(boolean.class)) {
                    addOption(Widgets.getBool(name, (Boolean) field.get(config), onValueChange));
                } else if (type.equals(float.class)) {
                    AbstractWidget label = Widgets.getOptionLabel(Component.translatable(name).append(":"));
                    AbstractWidget editBox = Widgets.getFloat("", (Float) field.get(config), onValueChange);
                    addOption(label, editBox);
                } else if (type.equals(int.class)) {
                    AbstractWidget label = Widgets.getOptionLabel(Component.translatable(name).append(":"));
                    AbstractWidget editBox = Widgets.getInt(name, (Integer) field.get(config), onValueChange);
                    addOption(label, editBox);
                } else if (type.getFields().length > 0) {
                    addOption(Widgets.getButton(name, (bttn)-> {
                                try {
                                    Minecraft.getInstance().setScreen(new ConfigScreen(
                                            this,
                                            field.get(config),
                                            Component.translatable(name)
                                    ));
                                } catch (IllegalAccessException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    ));
                } else {
                    CosyCritters.LOGGER.error("Unable to create option for field {}", field.getName());
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void addOption(AbstractWidget widget) {
        DummyOptionInstance option = new DummyOptionInstance(widget);
        this.list.addBig(option);
    }
    public void addOption(AbstractWidget leftWidget, AbstractWidget rightWidget) {
        DummyOptionInstance leftOption = new DummyOptionInstance(leftWidget);
        DummyOptionInstance rightOption = new DummyOptionInstance(rightWidget);
        this.list.addSmall(leftOption, rightOption);
    }

    /// in 1.20 [OptionsList] does not allow adding [AbstractWidget], only [OptionInstance].
    public class DummyOptionInstance extends OptionInstance {

        public AbstractWidget widget;

        public DummyOptionInstance(AbstractWidget widget) {
            super(null, null, null, null, null, null, null);
            this.widget = widget;
        }

        @Override
        public AbstractWidget createButton(Options ignored, int x, int y, int width, Consumer ignored2) {
            widget.setX(x);
            widget.setY(y);
            widget.setWidth(width);
            return widget;
        }

    }
}
