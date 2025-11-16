package pigcart.cosycritters.config.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class InputWidget extends EditBox {

    Pattern filteredChars;
    /// matches characters that aren't digits
    public static final Pattern NON_INTEGER = Pattern.compile("[^0-9]");
    /// matches characters that aren't digits or points
    public static final Pattern NON_FLOAT = Pattern.compile("[^0-9.]");
    /// matches characters that aren't valid in an identifier
    public static final Pattern NON_PATH = Pattern.compile("[^a-z0-9/._-]");

    public InputWidget(int x, int y, int width, int height, Supplier<String> getter, Consumer<String> setter) {
        super(Minecraft.getInstance().font, x, y, width, height, Component.empty());
        this.setMaxLength(8);
        this.setValue(getter.get());
        this.setResponder((value) -> {
            try {
                setter.accept(value);
                this.setTextColor(EditBox.DEFAULT_TEXT_COLOR);
            } catch (NumberFormatException ignored) {
                // keep old value if input invalid
                this.setTextColor(0xFFFF5555); // equivalent of ChatFormatting.RED. modern mc needs alpha specified
            }
        });
    }

    /// Sets the filter used by [EditBox]
    /// @param pattern Regex describing characters to be omitted from input
    public void setFilter(Pattern pattern) {
        filteredChars = pattern;
        if (filteredChars != null) {
            this.setFilter((string) -> !filteredChars.matcher(string).find());
        } else {
            this.setFilter(Objects::nonNull);
        }
    }
}
