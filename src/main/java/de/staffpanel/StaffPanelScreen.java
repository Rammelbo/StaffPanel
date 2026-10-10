package de.staffpanel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

public class StaffPanelScreen extends Screen {
    private EditBox reason;
    private EditBox duration;
    private String selectedPlayer;
    private final List<Button> playerButtons = new ArrayList<>();
    private int listTop;
    private int listBottom;

    public StaffPanelScreen() {
        super(Component.literal("StaffPanel"));
    }

    @Override
    protected void init() {
        playerButtons.clear();
        int panelWidth = Math.min(980, this.width - 32);
        int left = (this.width - panelWidth) / 2;
        int rightColumn = left + 330;
        int rightWidth = left + panelWidth - 24 - rightColumn;

        listTop = 78;
        listBottom = this.height - 48;

        reason = new EditBox(this.font, rightColumn + 8, 137, rightWidth, 24, Component.literal("Grund"));
        reason.setHint(Component.literal("Grund der Maßnahme"));
        addRenderableWidget(reason);

        duration = new EditBox(this.font, rightColumn + 8, 185, rightWidth, 24, Component.literal("Dauer"));
        duration.setHint(Component.literal("z. B. 1h"));
        addRenderableWidget(duration);

        addRenderableWidget(Button.builder(Component.literal("Warnen"), b -> sendCommand("/warn"))
            .bounds(rightColumn + 8, 224, rightWidth, 26).build());
        addRenderableWidget(Button.builder(Component.literal("Kicken"), b -> sendCommand("/kick"))
            .bounds(rightColumn + 8, 256, rightWidth, 26).build());
        addRenderableWidget(Button.builder(Component.literal("Bannen"), b -> sendCommand("/ban"))
            .bounds(rightColumn + 8, 288, rightWidth, 26).build());
        addRenderableWidget(Button.builder(Component.literal("Entbannen"), b -> sendCommand("/pardon"))
            .bounds(rightColumn + 8, 320, rightWidth, 26).build());
        addRenderableWidget(Button.builder(Component.literal("Timeout"), b -> sendCommand("/timeout"))
            .bounds(rightColumn + 8, 352, rightWidth, 26).build());
        addRenderableWidget(Button.builder(Component.literal("Schließen"), b -> onClose())
            .bounds(rightColumn, this.height - 35, rightWidth, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Liste aktualisieren"), b -> rebuildWidgets())
            .bounds(left + 8, 51, 300, 24).build());

        List<String> names = new ArrayList<>();
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
                names.add(info.getProfile().name());
            }
        }
        names.sort(Comparator.comparing(name -> name.toLowerCase(java.util.Locale.ROOT)));

        int rowY = listTop;
        int rowHeight = 30;
        int maxRows = Math.max(0, (listBottom - listTop) / rowHeight);
        for (int i = 0; i < Math.min(names.size(), maxRows); i++) {
            String name = names.get(i);
            Button button = Button.builder(
                Component.literal((name.equals(selectedPlayer) ? "▶ " : "") + name),
                b -> {
                    selectedPlayer = name;
                    rebuildWidgets();
                })
                .bounds(left + 8, rowY + i * rowHeight, 300, 26)
                .build();
            playerButtons.add(button);
            addRenderableWidget(button);
        }
    }

    private void sendCommand(String command) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || selectedPlayer == null || selectedPlayer.isBlank()) {
            return;
        }

        String why = reason.getValue().trim();
        String time = duration.getValue().trim();
        String full;
        switch (command) {
            case "/warn" -> full = "/warn " + selectedPlayer + (why.isEmpty() ? "" : " " + why);
            case "/kick" -> full = "/kick " + selectedPlayer + (why.isEmpty() ? "" : " " + why);
            case "/ban" -> full = "/ban " + selectedPlayer + (why.isEmpty() ? "" : " " + why);
            case "/pardon" -> full = "/pardon " + selectedPlayer;
            case "/timeout" -> full = "/timeout " + selectedPlayer
                    + (time.isEmpty() ? "" : " " + time)
                    + (why.isEmpty() ? "" : " " + why);
            default -> { return; }
        }

        mc.player.connection.sendCommand(full.substring(1));
        mc.gui.setScreen(null);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int panelWidth = Math.min(980, this.width - 32);
        int left = (this.width - panelWidth) / 2;
        int rightColumn = left + 330;

        graphics.fill(0, 0, this.width, this.height, 0x70000000);
        graphics.fill(left - 10, 18, left + panelWidth + 10, this.height - 12, 0xFF081224);
        graphics.fill(left - 10, 18, left + panelWidth + 10, 64, 0xFF142844);
        graphics.fill(left - 10, 62, left + panelWidth + 10, 65, 0xFF168BFF);
        graphics.fill(left, 72, left + 316, this.height - 46, 0xFF101E33);
        graphics.fill(left + 326, 72, left + panelWidth, this.height - 46, 0xB0101E33);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.text(this.font, Component.literal("◆  STAFF PANEL"), left + 8, 29, 0xFFFFFFFF, true);
        graphics.text(this.font, Component.literal("ONLINE-SPIELER"), left + 8, 82, 0xFF8FCBFF, true);
        graphics.text(this.font, Component.literal("MODERATIONSAKTIONEN"), rightColumn + 8, 82, 0xFF8FCBFF, true);

        String chosen = selectedPlayer == null ? "Kein Spieler ausgewählt" : "Ausgewählt: " + selectedPlayer;
        graphics.text(this.font, Component.literal(chosen), rightColumn + 8, 105,
            selectedPlayer == null ? 0xFFFFC66D : 0xFF8BE0A5, false);
        graphics.text(this.font, Component.literal("Grund der Maßnahme"), rightColumn + 8, 128, 0xFFD5DCE8, false);
        graphics.text(this.font, Component.literal("Dauer (für Timeout)"), rightColumn + 8, 176, 0xFFD5DCE8, false);

        Minecraft mc = Minecraft.getInstance();
        int online = mc.getConnection() == null ? 0 : mc.getConnection().getOnlinePlayers().size();
        graphics.text(this.font, Component.literal("Spieler: " + online), left, this.height - 32, 0xFFADB8C8, false);
        if (online == 0) {
            graphics.text(this.font, Component.literal("Keine Spielerliste verfügbar."), left, listTop + 4, 0xFFADB8C8, false);
        }
        graphics.text(this.font, Component.literal("Server-Befehle und Rechte werden vom Server bestimmt."),
            left, this.height - 20, 0xFFADB8C8, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
