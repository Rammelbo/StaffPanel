package de.staffpanel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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
    private int panelLeft, panelTop, panelWidth, panelHeight;
    private int contentTop, leftX, rightX, leftWidth, rightWidth;

    public StaffPanelScreen() {
        super(Component.literal("Staff Panel"));
    }

    @Override
    protected void init() {
        playerButtons.clear();

        panelWidth = Math.min(720, this.width - 24);
        panelHeight = Math.min(420, this.height - 16);
        panelLeft = (this.width - panelWidth) / 2;
        panelTop = (this.height - panelHeight) / 2;
        contentTop = panelTop + 76;

        int gap = 12;
        leftX = panelLeft + 12;
        leftWidth = Math.min(286, (panelWidth - 36) / 2);
        rightX = leftX + leftWidth + gap;
        rightWidth = panelLeft + panelWidth - 12 - rightX;

        int inputX = rightX + 10;
        int inputWidth = rightWidth - 20;
        reason = new EditBox(this.font, inputX, contentTop + 82, inputWidth, 20, Component.literal("Grund"));
        reason.setHint(Component.literal("Grund der Maßnahme"));
        addRenderableWidget(reason);

        duration = new EditBox(this.font, inputX, contentTop + 126, inputWidth, 20, Component.literal("Dauer"));
        duration.setHint(Component.literal("z. B. 1h"));
        addRenderableWidget(duration);

        int actionTop = contentTop + 160;
        int actionGap = 6;
        int actionWidth = (inputWidth - actionGap) / 2;
        addRenderableWidget(Button.builder(Component.literal("Warnen"), b -> sendCommand("/warn"))
            .bounds(inputX, actionTop, actionWidth, 24).build());
        addRenderableWidget(Button.builder(Component.literal("Kicken"), b -> sendCommand("/kick"))
            .bounds(inputX + actionWidth + actionGap, actionTop, actionWidth, 24).build());
        addRenderableWidget(Button.builder(Component.literal("Bannen"), b -> sendCommand("/ban"))
            .bounds(inputX, actionTop + 30, actionWidth, 24).build());
        addRenderableWidget(Button.builder(Component.literal("Entbannen"), b -> sendCommand("/pardon"))
            .bounds(inputX + actionWidth + actionGap, actionTop + 30, actionWidth, 24).build());
        addRenderableWidget(Button.builder(Component.literal("Timeout"), b -> sendCommand("/timeout"))
            .bounds(inputX, actionTop + 60, inputWidth, 24).build());

        addRenderableWidget(Button.builder(Component.literal("Schließen"), b -> onClose())
            .bounds(panelLeft + panelWidth - 112, panelTop + panelHeight - 30, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("↻"), b -> rebuildWidgets())
            .bounds(leftX + leftWidth - 26, contentTop + 2, 22, 20).build());

        List<String> names = new ArrayList<>();
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
                names.add(info.getProfile().name());
            }
        }
        names.sort(Comparator.comparing(name -> name.toLowerCase(Locale.ROOT)));

        int listY = contentTop + 30;
        int rowHeight = 29;
        int maxRows = Math.max(0, (panelTop + panelHeight - 42 - listY) / rowHeight);
        for (int i = 0; i < Math.min(names.size(), maxRows); i++) {
            String name = names.get(i);
            Button button = Button.builder(
                Component.literal((name.equals(selectedPlayer) ? "●  " : "   ") + name),
                b -> {
                    String reasonValue = reason.getValue();
                    String durationValue = duration.getValue();
                    selectedPlayer = name;
                    rebuildWidgets();
                    reason.setValue(reasonValue);
                    duration.setValue(durationValue);
                })
                .bounds(leftX + 4, listY + i * rowHeight, leftWidth - 8, 25)
                .build();
            playerButtons.add(button);
            addRenderableWidget(button);
        }
    }

    private void sendCommand(String command) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || selectedPlayer == null || selectedPlayer.isBlank()) return;

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
        int pw = Math.min(720, this.width - 24);
        int ph = Math.min(420, this.height - 16);
        int pl = (this.width - pw) / 2;
        int pt = (this.height - ph) / 2;
        int ct = pt + 76;
        int lx = pl + 12;
        int lw = Math.min(286, (pw - 36) / 2);
        int rx = lx + lw + 12;

        graphics.fill(0, 0, this.width, this.height, 0x99030A16);
        graphics.fill(pl, pt, pl + pw, pt + ph, 0xFF081326);
        graphics.fill(pl + 1, pt + 1, pl + pw - 1, pt + ph - 1, 0xFF0A1529);
        graphics.fill(pl + 1, pt + 1, pl + pw - 1, pt + 55, 0xFF102746);
        graphics.fill(pl + 1, pt + 53, pl + pw - 1, pt + 56, 0xFF168BFF);
        graphics.fill(lx, ct, lx + lw, pt + ph - 38, 0xFF0D1B30);
        graphics.fill(rx, ct, pl + pw - 12, pt + ph - 38, 0xFF0D1B30);
        graphics.fill(pl, pt, pl + pw, pt + 1, 0xFF397CC5);
        graphics.fill(pl, pt + ph - 1, pl + pw, pt + ph, 0xFF23466F);
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.text(this.font, Component.literal("⬡  STAFF PANEL"), pl + 14, pt + 13, 0xFFF1F7FF, true);
        graphics.text(this.font, Component.literal("Spieler verwalten und Moderation durchführen"), pl + 14, pt + 32, 0xFF9ABDEB, false);
        graphics.text(this.font, Component.literal("ONLINE"), pl + pw - 70, pt + 15, 0xFF35E89A, true);
        graphics.text(this.font, Component.literal("F6 zum Öffnen/Schließen"), pl + pw - 140, pt + 32, 0xFFB8D0F0, false);
        graphics.text(this.font, Component.literal("Online-Spieler"), lx + 8, ct + 7, 0xFF70C8FF, true);
        graphics.text(this.font, Component.literal("MODERATIONSAKTIONEN"), rx + 10, ct + 7, 0xFF70C8FF, true);

        String chosen = selectedPlayer == null ? "Kein Spieler ausgewählt" : "Ausgewählt: " + selectedPlayer;
        graphics.text(this.font, Component.literal(chosen), rx + 10, ct + 32,
            selectedPlayer == null ? 0xFFFFC66D : 0xFF3CE8A0, false);
        graphics.text(this.font, Component.literal("Grund der Maßnahme"), rx + 10, ct + 61, 0xFFD7E7FF, false);
        graphics.text(this.font, Component.literal("Dauer (für Timeout)"), rx + 10, ct + 105, 0xFFD7E7FF, false);

        Minecraft mc = Minecraft.getInstance();
        int online = mc.getConnection() == null ? 0 : mc.getConnection().getOnlinePlayers().size();
        graphics.text(this.font, Component.literal("Spieler: " + online), pl + 12, pt + ph - 24, 0xFF9DB8DB, false);
        graphics.text(this.font, Component.literal("Server-Befehle und Rechte werden vom Server bestimmt."),
            pl + 105, pt + ph - 24, 0xFF7895BB, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
