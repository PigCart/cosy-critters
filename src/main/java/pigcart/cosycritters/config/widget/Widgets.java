package pigcart.cosycritters.config.widget;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;
import java.util.function.Consumer;
import java.util.function.Function;

public class Widgets {

    public static LabelWidget getLabel(Component message) {
        return new LabelWidget(20, message);
    }

    public static LabelWidget getOptionLabel(Component message) {
        return new LabelWidget(20, message).alignRight();
    }

    public static AbstractWidget getBool(String name, boolean initialValue, Consumer<Boolean> onValueChange) {
        return new CycleButton.Builder<Boolean>(
                (value)-> value ? CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN) : CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED)
                //? if >=1.21.11 {
                /*,()-> initialValue)
                *///?} else {
        ).withInitialValue(initialValue)
                //?}
                .withValues(true, false)
                .create(40, 100, 200, 20,
                        Component.translatable(name),
                        (widget, value) -> onValueChange.accept(value));
    }

    public static AbstractWidget getFloat(String name, float initialValue, Consumer<Float> onValueChange) {
        final DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(6);
        final InputWidget inputWidget = new InputWidget(10, 10, 200, 20,
                () -> df.format(initialValue),
                (string) -> onValueChange.accept(Float.valueOf(string)));
        inputWidget.setFilter(InputWidget.NON_FLOAT);
        inputWidget.setMessage(Component.translatable(name));
        return inputWidget;
    }

    public static AbstractWidget getInt(String name, int initialValue, Consumer<Integer> onValueChange) {
        final InputWidget inputWidget = new InputWidget(10, 10, 200, 20,
                () -> String.valueOf(initialValue),
                (string) -> onValueChange.accept(Integer.valueOf(string)));
        inputWidget.setFilter(InputWidget.NON_INTEGER);
        inputWidget.setMessage(Component.translatable(name));
        return inputWidget;
    }

    public static AbstractWidget getButton(String name, Button.OnPress onPress) {
        return Button.builder(Component.translatable(name), onPress).bounds(40, 40, 120, 20).build();
    }

    public static AbstractWidget getSlider(String name, double initialValue, Consumer<Double> onValueChange, Function<Double, String> valueFormatter) {
        return new AbstractSliderButton(0, 0, 0, 0, Component.translatable(name), initialValue) {
            protected void updateMessage() {
                this.setMessage(Component.translatable(name, valueFormatter.apply(this.value)));
            }

            protected void applyValue() {
                onValueChange.accept(this.value);
            }
        };
    }
}
