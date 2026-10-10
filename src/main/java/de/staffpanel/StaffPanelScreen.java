package de.staffpanel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class StaffPanelScreen extends Screen {
    private EditBox playerName;
    private EditBox reason;
    private EditBox duration;

    public StaffPanelScreen() {
        super(Component.literal("StaffPanel - Moderation"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;

        playerName = new EditBox(this.font, cx - 105, 62, 210, 20, Component.literal("Spielername"));
        playerName.setHint(Component.literal("Minecraft-Name"));
        addRenderableWidget(playerName);

        reason = new EditBox(this.font, cx - 105, 102, 210, 20, Component.literal("Grund"));
        reason.setHint(Component.literal("Grund der Maßnahme"));
        addRenderableWidget(reason);

        duration = new EditBox(this.font, cx - 105, 142, 210, 20, Component.literal("Dauer"));
        duration.setHint(Component.literal("z. B. 1h"));
        addRenderableWidget(duration);

        addRenderableWidget(Button.builder(Component.literal("Warnen"), b -> sendCommand("/warn"))
            .bounds(cx - 105, 180, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Kicken"), b -> sendCommand("/kick"))
            .bounds(cx + 5, 180, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Bannen"), b -> sendCommand("/ban"))
            .bounds(cx - 105, 207, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Entbannen"), b -> sendCommand("/pardon"))
            .bounds(cx + 5, 207, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Timeout"), b -> sendCommand("/timeout"))
            .bounds(cx - 50, 234, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Schließen"), b -> onClose())
            .bounds(cx - 50, 265, 100, 20).build());
    }

    private void sendCommand(String command) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        String target = playerName.getValue().trim();
        if (target.isEmpty()) {
            mc.gui.toastManager().addToast(new net.minecraft.client.gui.components.toasts.SystemToast(
                net.minecraft.client.gui.components.toasts.SystemToast.SystemToastId.NARRATOR_TOGGLE,
                Component.literal("StaffPanel"),
                Component.literal("Bitte Spielernamen eingeben.")
            ));
            return;
        }

        String why = reason.getValue().trim();
        String time = duration.getValue().trim();
        String full;

        switch (command) {
            case "/warn" -> full = "/warn " + target + (why.isEmpty() ? "" : " " + why);
            case "/kick" -> full = "/kick " + target + (why.isEmpty() ? "" : " " + why);
            case "/ban" -> full = "/ban " + target + (why.isEmpty() ? "" : " " + why);
            case "/pardon" -> full = "/pardon " + target;
            case "/timeout" -> full = "/timeout " + target
                    + (time.isEmpty() ? "" : " " + time)
                    + (why.isEmpty() ? "" : " " + why);
            default -> { return; }
        }

        // Server command syntax and permissions are controlled by the server.
        mc.player.connection.sendCommand(full.substring(1));
        mc.gui.setScreen(null);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.text(this.font, this.title, this.width / 2 - this.font.width(this.title) / 2, 35, 0xFFFFFFFF, true);
        graphics.text(this.font, "Spielername", this.width / 2 - 105, 50, 0xFFCFCFCF, false);
        graphics.text(this.font, "Begruendung", this.width / 2 - 105, 90, 0xFFCFCFCF, false);
        graphics.text(this.font, "Dauer (optional)", this.width / 2 - 105, 130, 0xFFCFCFCF, false);
        graphics.text(this.font, "Befehle und Syntax haengen vom Server ab.", this.width / 2 - 115, this.height - 18, 0xFFAAAAAA, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
