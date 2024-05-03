package ceccs.game.panes.landing;

import ceccs.Client;
import ceccs.game.utils.Utilities;
import javafx.scene.control.CheckBox;

public class SettingsCheckBox extends CheckBox {

    public SettingsCheckBox(String text, String config, String defaultValue) {
        super(text);

        super.setFont(Utilities.veraMono);

        super.setSelected(
                Boolean.parseBoolean(
                        Client.configs.getProperty(config, defaultValue)
                )
        );

        super.selectedProperty().addListener((_, _, newValue) ->
                Client.configs.setProperty(config, newValue.toString())
        );
    }

    public SettingsCheckBox(String text, String config) {
        this(text, config, "false");
    }

}
