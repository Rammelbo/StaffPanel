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
        int panelWidth = Math.min(680, this.width - 24);
        int left = (this.width - panelWidth) / 2;
        int rightColumn = left + panelWidth / 2 + 8;
        int rightWidth = left + panelWidth - 8 - rightColumn;

        listTop = 78;
        listBottom = this.height - 48;

        reason = new EditBox(this.font, rightColumn, 105, rightWidth, 20, Component.literal("Grund"));
        reason.setHint(Component.literal("Grund der Maßnahme"));
        addRenderableWidget(reason);

        duration = new EditBox(this.font, rightColumn, 151, rightWidth, 20, Component.literal("Dauer"));
        duration.setHint(Component.literal("z. B. 1h"));
        addRenderableWidget(duration);

        addRenderableWidget(Button.builder(Component.literal("Warnen"), b -> sendCommand("/warn"))
            .bounds(rightColumn, 190, rightWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Kicken"), b -> sendCommand("/kick"))
            .bounds(rightColumn, 215, rightWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Bannen"), b -> sendCommand("/ban"))
            .bounds(rightColumn, 240, rightWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Entbannen"), b -> sendCommand("/pardon"))
            .bounds(rightColumn, 265, rightWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Timeout"), b -> sendCommand("/timeout"))
            .bounds(rightColumn, 290, rightWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Schließen"), b -> onClose())
            .bounds(rightColumn, this.height - 35, rightWidth, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Liste aktualisieren"), b -> rebuildWidgets())
            .bounds(left, 51, panelWidth / 2 - 4, 20).build());

        List<String> names = new ArrayList<>();
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
                names.add(info.getProfile().name());
            }
        }
        names.sort(Comparator.comparing(name -> name.toLowerCase(java.util.Locale.ROOT)));

        int rowY = listTop;
        int rowHeight = 22;
        int maxRows = Math.max(0, (listBottom - listTop) / rowHeight);
        for (int i = 0; i < Math.min(names.size(), maxRows); i++) {
            String name = names.get(i);
            Button button = Button.builder(
                Component.literal((name.equals(selectedPlayer) ? "▶ " : "") + name),
                b -> {
                    selectedPlayer = name;
                    rebuildWidgets();
                })
                .bounds(left, rowY + i * rowHeight, panelWidth / 2 - 4, 20)
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
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int panelWidth = Math.min(440, this.width - 24);
        int left = (this.width - panelWidth) / 2;
        int rightColumn = left + panelWidth / 2 + 8;

        graphics.fill(left - 8, 18, left + panelWidth + 8, this.height - 12, 0xE8141924);
        graphics.fill(left - 8, 18, left + panelWidth + 8, 44, 0xFF263449);
        graphics.text(this.font, Component.literal("STAFF PANEL"), left + 4, 27, 0xFFFFFFFF, true);
        graphics.text(this.font, Component.literal("ONLINE-SPIELER"), left, 65, 0xFF8FCBFF, true);
        graphics.text(this.font, Component.literal("AKTIONEN"), rightColumn, 65, 0xFF8FCBFF, true);

        String chosen = selectedPlayer == null ? "Kein Spieler ausgewählt" : "Ausgewählt: " + selectedPlayer;
        graphics.text(this.font, Component.literal(chosen), rightColumn, 83,
            selectedPlayer == null ? 0xFFFFC66D : 0xFF8BE0A5, false);
        graphics.text(this.font, Component.literal("Grund"), rightColumn, 94, 0xFFD5DCE8, false);
        graphics.text(this.font, Component.literal("Dauer"), rightColumn, 140, 0xFFD5DCE8, false);

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
