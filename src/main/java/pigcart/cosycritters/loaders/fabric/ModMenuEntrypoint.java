//? if fabric {
package pigcart.cosycritters.loaders.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import pigcart.cosycritters.config.ConfigScreens;

public class ModMenuEntrypoint implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreens::generateMainConfigScreen;
    }

}
//?}