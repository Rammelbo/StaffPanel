package de.staffpanel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

public class StaffPanelScreen extends Screen {
    private EditBox search, reason, duration;
    private String selectedPlayer;
    private List<String> playerNames = new ArrayList<>();
    private int panelX, panelY, panelW, panelH, headerH, footerY;
    private int listX, listY, listW, listBottom, rightX, rightW;
    private int actionY, actionH, actionGap, actionW;

    public StaffPanelScreen() { super(Component.literal("Staff Panel")); }

    @Override protected void init() {
        int margin = Math.max(12, Math.min(36, this.width / 30));
        panelW = Math.min(1360, this.width - margin * 2);
        panelH = Math.min(950, this.height - margin * 2);
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;
        headerH = Math.max(70, Math.min(112, panelH / 7));
        footerY = panelY + panelH - 58;

        int pad = Math.max(16, panelW / 70);
        int gap = Math.max(14, panelW / 60);
        listX = panelX + pad;
        listY = panelY + headerH + 18;
        listW = (panelW - pad * 2 - gap) * 43 / 100;
        listBottom = footerY - 16;
        rightX = listX + listW + gap;
        rightW = panelX + panelW - pad - rightX;

        int searchY = listY + 58;
        search = new EditBox(font, listX + 12, searchY, listW - 24, 36, Component.literal("Spieler suchen"));
        search.setHint(Component.literal("⌕  Spieler suchen ..."));
        addRenderableWidget(search);

        int formX = rightX + 18;
        int formW = rightW - 36;
        reason = new EditBox(font, formX, listY + 184, formW, 34, Component.literal("Grund"));
        reason.setHint(Component.literal("Grund der Maßnahme"));
        addRenderableWidget(reason);
        duration = new EditBox(font, formX, listY + 258, formW, 34, Component.literal("Dauer"));
        duration.setHint(Component.literal("z. B. 1h"));
        addRenderableWidget(duration);

        refreshPlayers();
    }

    private void refreshPlayers() {
        playerNames.clear();
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) playerNames.add(info.getProfile().name());
        }
        playerNames.sort(Comparator.comparing(n -> n.toLowerCase(Locale.ROOT)));
    }

    private List<String> filteredPlayers() {
        String q = search == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT);
        return playerNames.stream().filter(n -> n.toLowerCase(Locale.ROOT).contains(q)).toList();
    }

    private void sendCommand(String command) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || selectedPlayer == null || selectedPlayer.isBlank()) return;
        String why = reason.getValue().trim(), time = duration.getValue().trim(), full;
        switch (command) {
            case "warn" -> full = "warn " + selectedPlayer + (why.isEmpty() ? "" : " " + why);
            case "kick" -> full = "kick " + selectedPlayer + (why.isEmpty() ? "" : " " + why);
            case "ban" -> full = "ban " + selectedPlayer + (why.isEmpty() ? "" : " " + why);
            case "pardon" -> full = "pardon " + selectedPlayer;
            case "timeout" -> full = "timeout " + selectedPlayer + (time.isEmpty() ? "" : " " + time) + (why.isEmpty() ? "" : " " + why);
            default -> { return; }
        }
        mc.player.connection.sendCommand(full);
        mc.gui.setScreen(null);
    }

    @Override public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            int rowY = listY + 104;
            int rowH = 68;
            List<String> shown = filteredPlayers();
            for (int i = 0; i < shown.size(); i++) {
                int y = rowY + i * (rowH + 8);
                if (y + rowH > listBottom - 12) break;
                if (mx >= listX + 8 && mx <= listX + listW - 8 && my >= y && my <= y + rowH) {
                    selectedPlayer = shown.get(i);
                    return true;
                }
            }
            int x = rightX + 18, w = rightW - 36, gap = 12, half = (w - gap) / 2;
            int y = actionY;
            if (my >= y && my <= y + actionH) {
                if (mx >= x && mx <= x + half) { sendCommand("warn"); return true; }
                if (mx >= x + half + gap && mx <= x + w) { sendCommand("kick"); return true; }
            }
            y += actionH + actionGap;
            if (my >= y && my <= y + actionH) {
                if (mx >= x && mx <= x + half) { sendCommand("ban"); return true; }
                if (mx >= x + half + gap && mx <= x + w) { sendCommand("pardon"); return true; }
            }
            y += actionH + actionGap;
            if (my >= y && my <= y + actionH && mx >= x && mx <= x + half) { sendCommand("timeout"); return true; }
            if (mx >= panelX + panelW - 164 && my >= footerY && my <= panelY + panelH) { onClose(); return true; }
            if (mx >= listX + listW - 52 && my >= listY + 8 && my <= listY + 48) { refreshPlayers(); return true; }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        int pad = Math.max(16, panelW / 70);
        int gap = Math.max(14, panelW / 60);
        int rightEnd = panelX + panelW - pad;
        int contentBottom = footerY - 16;
        int cardPad = 18;
        actionY = listY + 318;
        actionH = Math.max(58, Math.min(76, panelH / 10));
        actionGap = 12;
        int formW = rightW - 36;
        int x = rightX + cardPad;
        int half = (rightW - cardPad * 2 - 12) / 2;

        g.fill(0, 0, width, height, 0xA600050D);
        g.fill(panelX + 3, panelY + 4, panelX + panelW + 3, panelY + panelH + 4, 0xAA000000);
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF071326);
        g.fill(panelX + 2, panelY + 2, panelX + panelW - 2, panelY + panelH - 2, 0xFF09172B);
        g.fill(panelX + 2, panelY + 2, panelX + panelW - 2, panelY + headerH, 0xFF0E1C32);
        g.fill(panelX, panelY + headerH - 3, panelX + panelW, panelY + headerH, 0xFF087DFF);
        g.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + 2, 0xFF426DA8);
        g.fill(panelX + 1, panelY + panelH - 2, panelX + panelW - 1, panelY + panelH - 1, 0xFF294B78);

        int cardTop = listY;
        g.fill(listX, cardTop, listX + listW, contentBottom, 0xFF0A1425);
        g.fill(listX + 1, cardTop + 1, listX + listW - 1, contentBottom - 1, 0xFF0A1629);
        g.fill(rightX, cardTop, rightEnd, listY + 156, 0xFF0A172B);
        g.fill(rightX + 1, cardTop + 1, rightEnd - 1, listY + 155, 0xFF0A182D);
        g.fill(rightX, listY + 168, rightEnd, listY + 168 + (actionH * 3 + actionGap * 2 + 48), 0xFF0A172B);
        g.fill(rightX + 1, listY + 169, rightEnd - 1, listY + 168 + (actionH * 3 + actionGap * 2 + 47), 0xFF0A182D);
        g.fill(rightX, listY + 168 + actionH * 3 + actionGap * 2 + 60, rightEnd, contentBottom, 0xFF0A172B);
        super.extractRenderState(g, mouseX, mouseY, delta);

        g.text(font, Component.literal("⬡  STAFF PANEL"), panelX + 26, panelY + 22, 0xFFF1F7FF, true);
        g.text(font, Component.literal("Verwalte Spieler und führe Moderationsaktionen durch"), panelX + 28, panelY + 47, 0xFF9ABDEB, false);
        g.fill(panelX + panelW - 186, panelY + 16, panelX + panelW - 18, panelY + headerH - 14, 0xFF081326);
        g.text(font, Component.literal("● Online"), panelX + panelW - 166, panelY + 24, 0xFF31E99A, true);
        g.text(font, Component.literal("F6 zum Öffnen/Schließen"), panelX + panelW - 166, panelY + 45, 0xFFB8D0F0, false);

        g.text(font, Component.literal("♟  Online-Spieler (" + playerNames.size() + ")"), listX + 16, listY + 16, 0xFF75C7FF, true);
        g.fill(listX + listW - 52, listY + 8, listX + listW - 12, listY + 46, 0xFF122844);
        g.text(font, Component.literal("↻"), listX + listW - 42, listY + 17, 0xFF80C9FF, true);

        List<String> shown = filteredPlayers();
        int rowY = listY + 104, rowH = 68;
        for (int i = 0; i < shown.size(); i++) {
            int y = rowY + i * (rowH + 8);
            if (y + rowH > listBottom - 12) break;
            boolean selected = shown.get(i).equals(selectedPlayer);
            g.fill(listX + 8, y, listX + listW - 8, y + rowH, selected ? 0xFF10345D : 0xFF0B1B30);
            g.fill(listX + 8, y, listX + 11, y + rowH, selected ? 0xFF12A8FF : 0xFF172A43);
            g.text(font, Component.literal("●  " + shown.get(i)), listX + 24, y + 15, selected ? 0xFFFFFFFF : 0xFFE1ECFC, true);
            g.text(font, Component.literal("Online  •  Spieler auswählen"), listX + 42, y + 39, 0xFF5CAFFF, false);
            g.text(font, Component.literal("›"), listX + listW - 30, y + 21, 0xFF78BFFF, true);
        }

        String chosen = selectedPlayer == null ? "Kein Spieler ausgewählt" : selectedPlayer;
        g.text(font, Component.literal("▣  " + chosen), rightX + 20, listY + 18, selectedPlayer == null ? 0xFFFFC66D : 0xFFF2F7FF, true);
        g.text(font, Component.literal("● Online"), rightX + rightW - 88, listY + 18, 0xFF32E99A, false);
        g.text(font, Component.literal("Online seit: —"), rightX + 20, listY + 49, 0xFF8BB9EF, false);
        g.text(font, Component.literal("UUID: —"), rightX + 20, listY + 72, 0xFF7896C0, false);
        g.text(font, Component.literal("IP:   —"), rightX + 20, listY + 94, 0xFF7896C0, false);

        g.text(font, Component.literal("ϟ  Aktionen"), rightX + 20, listY + 184, 0xFF78CFFF, true);
        drawAction(g, x, actionY, half, actionH, 0xFF6C2537, 0xFFFF5368, "⚠  Warnen", "Spieler eine Warnung senden");
        drawAction(g, x + half + 12, actionY, half, actionH, 0xFF49321C, 0xFFFFB02E, "➜  Kicken", "Spieler vom Server kicken");
        drawAction(g, x, actionY + actionH + actionGap, half, actionH, 0xFF612333, 0xFFFF5368, "⊘  Bannen", "Spieler dauerhaft bannen");
        drawAction(g, x + half + 12, actionY + actionH + actionGap, half, actionH, 0xFF103F3B, 0xFF00E5A0, "✓  Entbannen", "Spieler vom Bann entfernen");
        drawAction(g, x, actionY + (actionH + actionGap) * 2, half, actionH, 0xFF352054, 0xFFB14DFF, "◷  Timeout", "Spieler temporär stumm schalten");

        g.text(font, Component.literal("Zusätzliche Informationen"), rightX + 20, listY + 168 + actionH * 3 + actionGap * 2 + 76, 0xFF9BC7FF, true);
        g.text(font, Component.literal("Letzter Login: —"), rightX + 20, listY + 168 + actionH * 3 + actionGap * 2 + 100, 0xFF8AAAD5, false);
        g.text(font, Component.literal("Spielzeit: —     Rang: —"), rightX + 20, listY + 168 + actionH * 3 + actionGap * 2 + 122, 0xFF8AAAD5, false);

        g.fill(panelX + 1, footerY, panelX + panelW - 1, panelY + panelH - 1, 0xFF071225);
        g.fill(panelX + 1, footerY, panelX + panelW - 1, footerY + 1, 0xFF1A3557);
        g.text(font, Component.literal("♟  Spieler: " + playerNames.size()), panelX + 20, footerY + 20, 0xFF8DBDFF, false);
        g.text(font, Component.literal("▣  Server-Befehle und Rechte werden vom Server bestimmt."), panelX + 154, footerY + 20, 0xFF7898C4, false);
        g.text(font, Component.literal("⚙ Einstellungen"), panelX + panelW - 142, footerY + 20, 0xFF9FC7FF, false);
    }

    private void drawAction(GuiGraphicsExtractor g, int x, int y, int w, int h, int bg, int accent, String title, String subtitle) {
        g.fill(x, y, x + w, y + h, accent);
        g.fill(x + 2, y + 2, x + w - 2, y + h - 2, bg);
        g.text(font, Component.literal(title), x + 16, y + 13, accent, true);
        g.text(font, Component.literal(subtitle), x + 16, y + 34, 0xFFD0D9EA, false);
    }

    @Override public boolean isPauseScreen() { return false; }
}
