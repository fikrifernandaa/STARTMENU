import java.util.List;

public class InputHandler {

    private final StartMenuClone app;

    public InputHandler(StartMenuClone app) {
        this.app = app;
    }

    // =====================================================================
    //  MOUSE — dipanggil dari StartMenuClone berdasarkan event mentah
    // =====================================================================

    /** Dipanggil saat posisi mouse berubah (gerak biasa atau drag). */
    public void processMouseMove(int mx, int my, boolean dragging) {
        if (dragging && app.dragging != null) {
            app.dragging.x = mx - app.dragDX;
            app.dragging.y = my - app.dragDY;
        } else {
            updateHover(mx, my);
        }
    }

    /** Dipanggil saat roda scroll digerakkan. rotation: -1 (atas) / +1 (bawah). */
    public void processScroll(int rotation) {
        if (!app.menuReady()) return;
        int py = (int) app.menuPanelY;
        int mx = app.lastMouseX, my = app.lastMouseY;
        if (mx >= 0 && mx <= StartMenuClone.L_W && my >= py && my <= py + StartMenuClone.MNU_H) {
            app.scrollOff += rotation * StartMenuClone.ITEM_H;
            if (app.scrollOff < 0) app.scrollOff = 0;
        }
    }

    /** Dipanggil saat tombol mouse kiri ditekan (klik). */
    public void processClick(int mx, int my) {
        // ── Sleep: klik mana saja = bangun ───────────────────────────
        if (app.sleeping) { app.sleeping = false; return; }
        if (app.shuttingDown || app.restarting) return;

        // ── Drag / close fake windows ─────────────────────────────────
        for (int i = app.windows.size() - 1; i >= 0; i--) {
            FakeWindow fw = app.windows.get(i);
            if (inside(mx, my, fw.closeX, fw.closeY, fw.closeW, fw.closeH)) {
                app.windows.remove(i); return;
            }
            if (mx >= fw.x && mx <= fw.x + fw.w && my >= fw.y && my <= fw.y + 28) {
                app.windows.remove(i); app.windows.add(fw);
                app.dragging = fw; app.dragDX = mx - fw.x; app.dragDY = my - fw.y; return;
            }
            if (inside(mx, my, fw.x, fw.y, fw.w, fw.h)) {
                app.windows.remove(i); app.windows.add(fw); return;
            }
        }

        // ── Dropdown shutdown ─────────────────────────────────────────
        if (app.shutDrop) {
            int dw = 155, ih = 23, dh = app.SHUT_OPTS.length * ih + 8;
            int dx = StartMenuClone.MNU_W - dw - 6,
                    dy = StartMenuClone.MNU_H - StartMenuClone.SRCH_H - 4 - dh - 4;
            int py = (int) app.menuPanelY;
            if (mx >= dx && mx <= dx + dw && my >= py + dy && my <= py + dy + dh) {
                int idx = (my - py - dy - 4) / ih;
                if (idx >= 0 && idx < app.SHUT_OPTS.length) app.doPowerAction(app.SHUT_OPTS[idx]);
                return;
            } else {
                app.shutDrop = false;
            }
        }

        // ── Tombol Start ──────────────────────────────────────────────
        if (inside(mx, my, 0, StartMenuClone.SH - StartMenuClone.TB_H,
                72, StartMenuClone.TB_H)) {
            app.toggleMenu(); return;
        }

        if (!app.menuReady()) return;
        int py = (int) app.menuPanelY;
        int rx = mx, ry = my - py;

        // Klik di luar menu = tutup menu
        if (rx < 0 || rx > StartMenuClone.MNU_W || ry < 0 || ry > StartMenuClone.MNU_H) {
            app.closeMenu(); return;
        }

        // ── Search box ────────────────────────────────────────────────
        int sx = 6, sy = StartMenuClone.MNU_H - StartMenuClone.SRCH_H - 4,
                sw = StartMenuClone.L_W - 14;
        if (inside(rx, ry, sx, sy, sw, StartMenuClone.SRCH_H)) {
            app.searchFocused = true; return;
        }
        app.searchFocused = false;

        // ── Shutdown button ───────────────────────────────────────────
        int bx = StartMenuClone.L_W + 6,
                by = StartMenuClone.MNU_H - StartMenuClone.SRCH_H - 4,
                bwt = StartMenuClone.R_W - 12,
                bh  = StartMenuClone.SRCH_H,
                mw  = bwt - 22;
        if (inside(rx, ry, bx, by, mw, bh))           { app.doPowerAction("Shut down"); return; }
        if (inside(rx, ry, bx + mw + 2, by, bwt - mw - 2, bh)) { app.shutDrop = !app.shutDrop; return; }

        // ── Back button ───────────────────────────────────────────────
        boolean searching = app.searchText.length() > 0;
        boolean showBack  = app.inAccessories && !searching;
        if (showBack) {
            int aBot = StartMenuClone.MNU_H - StartMenuClone.SRCH_H - 8 - 28;
            if (inside(rx, ry, 3, aBot + 2, StartMenuClone.L_W - 10, 26)) {
                app.inAccessories = false; app.scrollOff = 0; return;
            }
        }

        // ── Daftar kiri ───────────────────────────────────────────────
        List<AppItem> list = app.buildSearchList(searching);
        int backH = showBack ? 28 : 0;
        int aTop  = 4, aBot = StartMenuClone.MNU_H - StartMenuClone.SRCH_H - 8 - backH;
        if (rx >= 0 && rx <= StartMenuClone.L_W && ry >= aTop && ry <= aBot) {
            int idx = (ry - aTop + app.scrollOff) / StartMenuClone.ITEM_H;
            if (idx >= 0 && idx < list.size()) {
                AppItem a = list.get(idx);
                if (a.isFolder()) { app.inAccessories = true; app.scrollOff = 0; app.searchText = ""; }
                else              { app.launchApp(a); }
            }
            return;
        }

        // ── Daftar kanan ──────────────────────────────────────────────
        int top = StartMenuClone.HEAD_H;
        if (rx >= StartMenuClone.L_W && rx <= StartMenuClone.MNU_W && ry >= top) {
            int idx = (ry - top) / StartMenuClone.ITEM_H;
            if (idx >= 0 && idx < app.rightList.size()) app.launchApp(app.rightList.get(idx));
        }
    }

    /** Dipanggil saat tombol mouse dilepas. */
    public void processRelease() {
        app.dragging = null;
    }

    // =====================================================================
    //  HOVER
    // =====================================================================
    void updateHover(int mx, int my) {
        app.hovStart = inside(mx, my, 0, StartMenuClone.SH - StartMenuClone.TB_H,
                72, StartMenuClone.TB_H);
        app.hovLeft  = -1; app.hovRight  = -1; app.hovBack  = false;
        app.hovShutMain = false; app.hovShutArrow = false; app.hovShutOpt = -1;
        if (!app.menuReady()) return;

        int py = (int) app.menuPanelY, rx = mx, ry = my - py;

        if (app.shutDrop) {
            int dw = 155, ih = 23, dh = app.SHUT_OPTS.length * ih + 8;
            int dx = StartMenuClone.MNU_W - dw - 6,
                    dy = StartMenuClone.MNU_H - StartMenuClone.SRCH_H - 4 - dh - 4;
            if (rx >= dx && rx <= dx + dw && ry >= dy && ry <= dy + dh)
                app.hovShutOpt = (ry - dy - 4) / ih;
        }

        int bx  = StartMenuClone.L_W + 6,
                by  = StartMenuClone.MNU_H - StartMenuClone.SRCH_H - 4,
                bwt = StartMenuClone.R_W - 12,
                bh  = StartMenuClone.SRCH_H,
                mw  = bwt - 22;
        app.hovShutMain  = inside(rx, ry, bx, by, mw, bh);
        app.hovShutArrow = inside(rx, ry, bx + mw + 2, by, bwt - mw - 2, bh);

        boolean searching = app.searchText.length() > 0;
        boolean showBack  = app.inAccessories && !searching;
        if (showBack) {
            int ab = StartMenuClone.MNU_H - StartMenuClone.SRCH_H - 8 - 28;
            app.hovBack = inside(rx, ry, 3, ab + 2, StartMenuClone.L_W - 10, 26);
        }

        List<AppItem> list = app.buildSearchList(searching);
        int backH = showBack ? 28 : 0;
        int aTop  = 4, aBot = StartMenuClone.MNU_H - StartMenuClone.SRCH_H - 8 - backH;
        if (rx >= 0 && rx <= StartMenuClone.L_W && ry >= aTop && ry <= aBot) {
            int idx = (ry - aTop + app.scrollOff) / StartMenuClone.ITEM_H;
            if (idx >= 0 && idx < list.size()) app.hovLeft = idx;
        }

        int top = StartMenuClone.HEAD_H;
        if (rx >= StartMenuClone.L_W && rx <= StartMenuClone.MNU_W && ry >= top) {
            int idx = (ry - top) / StartMenuClone.ITEM_H;
            if (idx >= 0 && idx < app.rightList.size()) app.hovRight = idx;
        }
    }

    // =====================================================================
    //  KEYBOARD — dipanggil dengan kode tombol & karakter mentah (int/char)
    // =====================================================================

    /** Dipanggil saat sebuah karakter dapat dicetak diketik (huruf, angka, simbol). */
    public void processCharTyped(char c) {
        if (!app.searchFocused) return;
        if (c >= 32 && c < 127 && app.searchText.length() < 42) {
            app.searchText += c;
            app.scrollOff  = 0;
        }
    }

    public static final int KEY_ESCAPE    = 27;  // sama dengan VK_ESCAPE
    public static final int KEY_BACKSPACE = 8;   // sama dengan VK_BACK_SPACE

    /** Dipanggil saat tombol non-cetak ditekan (Escape, Backspace, dst). */
    public void processSpecialKey(int keyCode) {
        if (keyCode == KEY_ESCAPE) {
            if (app.searchFocused) { app.searchText = ""; app.searchFocused = false; }
            else app.closeMenu();
        }
        if (!app.searchFocused) return;
        if (keyCode == KEY_BACKSPACE && app.searchText.length() > 0)
            app.searchText = app.searchText.substring(0, app.searchText.length() - 1);
    }

    // ── Helper ──────────────────────────────────────────────────────────
    boolean inside(int px, int py, int x, int y, int w, int h) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }
}