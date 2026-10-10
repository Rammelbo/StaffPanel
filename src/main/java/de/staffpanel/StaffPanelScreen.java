package de.staffpanel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

public class StaffPanelScreen extends Screen {
    private EditBox search;
    private EditBox reason;
    private EditBox duration;
    private String selectedPlayer;
    private String pendingAction;
    private String statusMessage = "";
    private final List<String> playerNames = new ArrayList<>();

    private int panelX, panelY, panelW, panelH, headerH, footerY;
    private int listX, listY, listW, listBottom, rightX, rightW, contentBottom;
    private int searchY, rowY, rowH;
    private int profileBottom, actionsTop, actionsBottom, infoTop;
    private int actionH, actionGap, actionColW, actionX, actionW;
    private boolean showSettings;

    public StaffPanelScreen() { super(Component.literal("Staff Panel")); }

    @Override protected void init() {
        layout();
        search = new EditBox(font, listX + 12, searchY, listW - 24, 26, Component.literal("Spieler suchen"));
        search.setHint(Component.literal("Spieler suchen ..."));
        addRenderableWidget(search);
        if (pendingAction != null) {
            reason = new EditBox(font, panelX + panelW / 2 - 160, panelY + panelH / 2 - 10, 320, 24, Component.literal("Grund"));
            reason.setHint(Component.literal("Grund der Maßnahme"));
            addRenderableWidget(reason);
            duration = new EditBox(font, panelX + panelW / 2 - 160, panelY + panelH / 2 + 30, 320, 24, Component.literal("Dauer"));
            duration.setHint(Component.literal("z. B. 1h"));
            if (!pendingAction.equals("timeout")) duration.visible = false;
            else addRenderableWidget(duration);
        }
        refreshPlayers();
    }

    private void layout() {
        int margin = Math.max(8, Math.min(28, width / 45));
        panelW = Math.max(320, Math.min(1360, width - margin * 2));
        panelH = Math.max(240, Math.min(950, height - margin * 2));
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        headerH = Math.max(58, Math.min(86, panelH / 7));
        footerY = panelY + panelH - 42;
        int pad = Math.max(12, Math.min(22, panelW / 65));
        int gap = Math.max(10, Math.min(22, panelW / 65));
        listX = panelX + pad;
        listY = panelY + headerH + 12;
        contentBottom = footerY - 12;
        listBottom = contentBottom;
        listW = (panelW - pad * 2 - gap) * 42 / 100;
        rightX = listX + listW + gap;
        rightW = panelX + panelW - pad - rightX;
        searchY = listY + 38;
        rowY = searchY + 36;
        rowH = Math.max(36, Math.min(52, (contentBottom - rowY - 10) / 7));
        profileBottom = listY + Math.max(82, Math.min(104, (contentBottom - listY) / 5));
        actionsTop = profileBottom + 10;
        actionsBottom = contentBottom - Math.max(86, Math.min(108, (contentBottom - listY) / 5)) - 10;
        infoTop = actionsBottom + 10;
        actionX = rightX + 12;
        actionW = rightW - 24;
        actionGap = 10;
        actionColW = (actionW - actionGap) / 2;
        actionH = Math.max(48, Math.min(66, (actionsBottom - actionsTop - 42) / 3));
    }

    private void refreshPlayers() {
        playerNames.clear();
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) playerNames.add(info.getProfile().name());
        }
        playerNames.sort(Comparator.comparing(n -> n.toLowerCase(Locale.ROOT)));
        if (selectedPlayer != null && !playerNames.contains(selectedPlayer)) selectedPlayer = null;
    }

    private List<String> filteredPlayers() {
        String q = search == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT);
        return playerNames.stream().filter(n -> n.toLowerCase(Locale.ROOT).contains(q)).toList();
    }

    private void performAction(String action) {
        if (selectedPlayer == null || selectedPlayer.isBlank()) {
            statusMessage = "Bitte zuerst einen Spieler auswählen.";
            return;
        }
        if (action.equals("pardon")) {
            sendCommand("pardon");
            return;
        }
        pendingAction = action;
        reason = null;
        duration = null;
        rebuildWidgets();
    }

    private void confirmAction() {
        if (selectedPlayer == null || pendingAction == null) return;
        String why = reason == null ? "" : reason.getValue().trim();
        String time = duration == null ? "" : duration.getValue().trim();
        String command = switch (pendingAction) {
            case "warn" -> "warn " + selectedPlayer + (why.isEmpty() ? "" : " " + why);
            case "kick" -> "kick " + selectedPlayer + (why.isEmpty() ? "" : " " + why);
            case "ban" -> "ban " + selectedPlayer + (why.isEmpty() ? "" : " " + why);
            case "timeout" -> "timeout " + selectedPlayer + (time.isEmpty() ? "" : " " + time) + (why.isEmpty() ? "" : " " + why);
            default -> null;
        };
        if (command != null) sendCommand(command);
        pendingAction = null;
        rebuildWidgets();
    }

    private void sendCommand(String command) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.player.connection.sendCommand(command);
        mc.setScreen(null);
    }

    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x(), my = event.y();
        if (event.button() == 0) {
            if (pendingAction != null) {
                int cx = panelX + panelW / 2, cy = panelY + panelH / 2;
                if (mx >= cx - 160 && mx <= cx + 160 && my >= cy + 64 && my <= cy + 98) {
                    confirmAction();
                    return true;
                }
                if (mx >= cx - 160 && mx <= cx + 160 && my >= cy + 108 && my <= cy + 142) {
                    pendingAction = null;
                    rebuildWidgets();
                    return true;
                }
                return super.mouseClicked(event, doubleClick);
            }
            if (mx >= listX + listW - 46 && mx <= listX + listW - 8 && my >= listY + 5 && my <= listY + 35) {
                refreshPlayers();
                statusMessage = "Spielerliste aktualisiert.";
                return true;
            }
            List<String> shown = filteredPlayers();
            int visibleRows = Math.max(0, (listBottom - rowY - 8) / rowH);
            for (int i = 0; i < Math.min(shown.size(), visibleRows); i++) {
                int y = rowY + i * (rowH + 6);
                if (mx >= listX + 8 && mx <= listX + listW - 8 && my >= y && my <= y + rowH) {
                    selectedPlayer = shown.get(i);
                    statusMessage = "";
                    return true;
                }
            }
            int y1 = actionsTop + 30;
            int y2 = y1 + actionH + 8;
            int y3 = y2 + actionH + 8;
            if (my >= y1 && my <= y1 + actionH) {
                if (mx >= actionX && mx <= actionX + actionColW) performAction("warn");
                else if (mx >= actionX + actionColW + actionGap && mx <= actionX + actionW) performAction("kick");
                return true;
            }
            if (my >= y2 && my <= y2 + actionH) {
                if (mx >= actionX && mx <= actionX + actionColW) performAction("ban");
                else if (mx >= actionX + actionColW + actionGap && mx <= actionX + actionW) performAction("pardon");
                return true;
            }
            if (my >= y3 && my <= y3 + actionH && mx >= actionX && mx <= actionX + actionColW) {
                performAction("timeout");
                return true;
            }
            if (mx >= panelX + panelW - 148 && my >= footerY && my <= panelY + panelH) {
                showSettings = !showSettings;
                statusMessage = showSettings ? "F6 öffnet und schließt das Panel." : "";
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        layout();
        int rightEnd = panelX + panelW - Math.max(12, Math.min(22, panelW / 65));
        int cardBottom = contentBottom;
        g.fill(0, 0, width, height, 0xB900050D);
        g.fill(panelX + 4, panelY + 5, panelX + panelW + 4, panelY + panelH + 5, 0xB8000000);
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF071326);
        g.fill(panelX + 2, panelY + 2, panelX + panelW - 2, panelY + panelH - 2, 0xFF09172B);
        g.fill(panelX + 2, panelY + 2, panelX + panelW - 2, panelY + headerH, 0xFF0E1C32);
        g.fill(panelX, panelY + headerH - 3, panelX + panelW, panelY + headerH, 0xFF0788FF);
        g.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + 2, 0xFF426DA8);

        g.fill(listX, listY, listX + listW, cardBottom, 0xFF0B172A);
        g.fill(listX + 1, listY + 1, listX + listW - 1, cardBottom - 1, 0xFF0A182D);
        g.fill(rightX, listY, rightEnd, profileBottom, 0xFF0B172A);
        g.fill(rightX + 1, listY + 1, rightEnd - 1, profileBottom - 1, 0xFF0A182D);
        g.fill(rightX, actionsTop, rightEnd, actionsBottom, 0xFF0B172A);
        g.fill(rightX + 1, actionsTop + 1, rightEnd - 1, actionsBottom - 1, 0xFF0A182D);
        g.fill(rightX, infoTop, rightEnd, cardBottom, 0xFF0B172A);
        g.fill(rightX + 1, infoTop + 1, rightEnd - 1, cardBottom - 1, 0xFF0A182D);
        super.extractRenderState(g, mouseX, mouseY, delta);

        g.text(font, Component.literal("⬡  STAFF PANEL"), panelX + 24, panelY + 17, 0xFFF1F7FF, true);
        g.text(font, Component.literal("Verwalte Spieler und führe Moderationsaktionen durch"), panelX + 26, panelY + 39, 0xFF9ABDEB, false);
        int badgeX = panelX + panelW - 174;
        g.fill(badgeX, panelY + 10, panelX + panelW - 12, panelY + headerH - 10, 0xFF081326);
        g.text(font, Component.literal("● Online"), badgeX + 16, panelY + 18, 0xFF31E99A, true);
        g.text(font, Component.literal("F6 zum Öffnen/Schließen"), badgeX + 16, panelY + 38, 0xFFB8D0F0, false);

        g.text(font, Component.literal("♟  Online-Spieler (" + playerNames.size() + ")"), listX + 14, listY + 12, 0xFF75C7FF, true);
        g.fill(listX + listW - 42, listY + 5, listX + listW - 8, listY + 33, 0xFF122844);
        g.text(font, Component.literal("↻"), listX + listW - 33, listY + 10, 0xFF80C9FF, true);
        List<String> shown = filteredPlayers();
        int visibleRows = Math.max(0, (listBottom - rowY - 8) / (rowH + 6));
        for (int i = 0; i < Math.min(shown.size(), visibleRows); i++) {
            int y = rowY + i * (rowH + 6);
            boolean selected = shown.get(i).equals(selectedPlayer);
            g.fill(listX + 8, y, listX + listW - 8, y + rowH, selected ? 0xFF10345D : 0xFF0D1D32);
            if (selected) {
                g.fill(listX + 8, y, listX + 11, y + rowH, 0xFF12A8FF);
                g.fill(listX + 11, y, listX + listW - 8, y + 2, 0xFF168FFF);
            } else g.fill(listX + 8, y, listX + 10, y + rowH, 0xFF172A43);
            int textY = y + Math.max(4, rowH / 2 - 5);
            g.text(font, Component.literal("●  " + shown.get(i)), listX + 22, textY, selected ? 0xFFFFFFFF : 0xFFE1ECFC, true);
            g.text(font, Component.literal("›"), listX + listW - 28, textY, 0xFF78BFFF, true);
        }
        if (shown.isEmpty()) g.text(font, Component.literal("Keine passenden Spieler"), listX + 18, rowY + 10, 0xFF8299B8, false);

        String chosen = selectedPlayer == null ? "Kein Spieler ausgewählt" : selectedPlayer;
        g.text(font, Component.literal("▣  " + chosen), rightX + 18, listY + 18, selectedPlayer == null ? 0xFFFFC66D : 0xFFF2F7FF, true);
        g.text(font, Component.literal("● Online"), rightX + rightW - 86, listY + 18, 0xFF32E99A, false);
        g.text(font, Component.literal("Online seit: —"), rightX + 18, listY + 42, 0xFF8BB9EF, false);
        g.text(font, Component.literal("UUID: " + selectedUuid()), rightX + 18, listY + 61, 0xFF7896C0, false);
        g.text(font, Component.literal("IP:  —"), rightX + 18, listY + 80, 0xFF7896C0, false);

        g.text(font, Component.literal("ϟ  Aktionen"), rightX + 18, actionsTop + 10, 0xFF78CFFF, true);
        int ax = actionX, ay = actionsTop + 30;
        drawAction(g, ax, ay, actionColW, actionH, 0xFF321A2A, 0xFFFF5368, "⚠  Warnen", "Warnung senden", mouseX, mouseY);
        drawAction(g, ax + actionColW + actionGap, ay, actionColW, actionH, 0xFF35291B, 0xFFFFB02E, "➜  Kicken", "Vom Server kicken", mouseX, mouseY);
        ay += actionH + 8;
        drawAction(g, ax, ay, actionColW, actionH, 0xFF351A28, 0xFFFF5368, "⊘  Bannen", "Dauerhaft bannen", mouseX, mouseY);
        drawAction(g, ax + actionColW + actionGap, ay, actionColW, actionH, 0xFF10372F, 0xFF00E5A0, "✓  Entbannen", "Ban entfernen", mouseX, mouseY);
        ay += actionH + 8;
        drawAction(g, ax, ay, actionColW, actionH, 0xFF28183F, 0xFFB14DFF, "◷  Timeout", "Temporär stummschalten", mouseX, mouseY);

        g.text(font, Component.literal("▤  Zusätzliche Informationen"), rightX + 18, infoTop + 12, 0xFF9BC7FF, true);
        g.text(font, Component.literal("Letzter Login: —"), rightX + 18, infoTop + 32, 0xFF8AAAD5, false);
        g.text(font, Component.literal("Spielzeit: —      Rang: " + (selectedPlayer == null ? "—" : "Spieler")), rightX + 18, infoTop + 50, 0xFF8AAAD5, false);

        g.fill(panelX + 1, footerY, panelX + panelW - 1, panelY + panelH - 1, 0xFF071225);
        g.fill(panelX + 1, footerY, panelX + panelW - 1, footerY + 1, 0xFF1A3557);
        g.text(font, Component.literal("♟  Spieler: " + playerNames.size()), panelX + 18, footerY + 15, 0xFF8DBDFF, false);
        g.text(font, Component.literal("▣  Server-Befehle und Rechte werden vom Server bestimmt."), panelX + 120, footerY + 15, 0xFF7898C4, false);
        g.text(font, Component.literal("⚙ Einstellungen"), panelX + panelW - 126, footerY + 15, 0xFF9FC7FF, false);
        if (!statusMessage.isEmpty() && pendingAction == null)
            g.text(font, Component.literal(statusMessage), panelX + panelW / 2 - font.width(statusMessage) / 2, footerY - 12, 0xFFFFC66D, false);

        if (pendingAction != null) drawConfirmation(g);
        if (showSettings && pendingAction == null) {
            g.fill(panelX + panelW / 2 - 140, panelY + panelH / 2 - 44, panelX + panelW / 2 + 140, panelY + panelH / 2 + 44, 0xFF102440);
            g.text(font, Component.literal("Einstellungen"), panelX + panelW / 2 - 48, panelY + panelH / 2 - 30, 0xFF8CCBFF, true);
            g.text(font, Component.literal("F6 öffnet und schließt das Panel."), panelX + panelW / 2 - 118, panelY + panelH / 2 - 8, 0xFFD0D9EA, false);
            g.text(font, Component.literal("Klicken zum Schließen"), panelX + panelW / 2 - 74, panelY + panelH / 2 + 18, 0xFF80C9FF, false);
        }
    }

    private String selectedUuid() {
        if (selectedPlayer == null || Minecraft.getInstance().getConnection() == null) return "—";
        for (PlayerInfo info : Minecraft.getInstance().getConnection().getOnlinePlayers()) {
            if (info.getProfile().name().equals(selectedPlayer)) return info.getProfile().id().toString();
        }
        return "—";
    }

    private void drawAction(GuiGraphicsExtractor g, int x, int y, int w, int h, int bg, int accent, String title, String subtitle, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        g.fill(x, y, x + w, y + h, accent);
        g.fill(x + 2, y + 2, x + w - 2, y + h - 2, hover ? brighten(bg) : bg);
        g.fill(x + 10, y + 9, x + 42, y + h - 9, 0x332FFFFFF);
        g.text(font, Component.literal(title), x + 50, y + 10, accent, true);
        if (h >= 56) g.text(font, Component.literal(subtitle), x + 50, y + h - 20, 0xFFD0D9EA, false);
    }

    private int brighten(int color) {
        int r = Math.min(255, ((color >> 16) & 255) + 10);
        int g = Math.min(255, ((color >> 8) & 255) + 10);
        int b = Math.min(255, (color & 255) + 10);
        return (color & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    private void drawConfirmation(GuiGraphicsExtractor g) {
        int cx = panelX + panelW / 2, cy = panelY + panelH / 2;
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0x99000000);
        g.fill(cx - 190, cy - 100, cx + 190, cy + 150, 0xFF10213A);
        g.fill(cx - 190, cy - 100, cx + 190, cy - 97, 0xFF159BFF);
        g.text(font, Component.literal("Bestätigen: " + pendingAction.toUpperCase(Locale.ROOT)), cx - 164, cy - 78, 0xFFF0F6FF, true);
        g.text(font, Component.literal("Spieler: " + selectedPlayer), cx - 164, cy - 52, 0xFF9FC7FF, false);
        g.text(font, Component.literal("Grund der Maßnahme"), cx - 160, cy - 30, 0xFFD5E4FA, false);
        if (pendingAction.equals("timeout")) g.text(font, Component.literal("Dauer"), cx - 160, cy + 10, 0xFFD5E4FA, false);
        g.fill(cx - 160, cy + 64, cx + 160, cy + 98, 0xFF0E704E);
        g.text(font, Component.literal("Bestätigen"), cx - 42, cy + 76, 0xFFFFFFFF, true);
        g.fill(cx - 160, cy + 108, cx + 160, cy + 142, 0xFF273A55);
        g.text(font, Component.literal("Abbrechen"), cx - 38, cy + 120, 0xFFE3ECFA, false);
    }

    @Override public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (pendingAction != null && event.key() == 256) {
            pendingAction = null;
            rebuildWidgets();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override public boolean isPauseScreen() { return false; }
}
