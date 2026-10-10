package de.staffpanel;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.platform.InputConstants;

public class StaffPanelClient implements ClientModInitializer {
    private static KeyMapping openPanel;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
            ResourceLocation.fromNamespaceAndPath("staffpanel", "main")
        );
        openPanel = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.staffpanel.open",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_F6,
            category
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openPanel.consumeClick()) {
                if (client.player != null) {
                    client.setScreen(new StaffPanelScreen());
                }
            }
        });
    }
}
