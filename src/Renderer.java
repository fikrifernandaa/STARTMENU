import java.awt.*;
import java.awt.image.BufferStrategy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Bertanggung jawab atas seluruh logika penggambaran (rendering) layar.
 * Dipanggil dari game-loop di StartMenuClone.
 */
public class Renderer {

    private final StartMenuClone app;

    public Renderer(StartMenuClone app) {
        this.app = app;
    }

    // =====================================================================
    //  ENTRY POINT RENDER
    // =====================================================================
    public void render() {
        BufferStrategy bs = app.canvas.getBufferStrategy();
        if (bs == null) return;

        Graphics2D g = (Graphics2D) bs.getDrawGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (app.sleeping) {
            drawSleepScreen(g);
        } else {
            drawWallpaper(g);
            drawDesktopIcons(g);
            for (FakeWindow w : app.windows) drawFakeWindow(g, w);
            drawTaskbar(g);
            if (app.animProg > 0.001) drawMenu(g);
            if (app.toastText != null) drawToast(g);
            if (app.shuttingDown || app.restarting) drawShutdownOverlay(g);
        }

        g.dispose();
        bs.show();
    }

    // =====================================================================
    //  WALLPAPER
    // =====================================================================
    void drawWallpaper(Graphics2D g) {
        GradientPaint sky = new GradientPaint(0, 0, new Color(4, 14, 38),
                0, StartMenuClone.SH - StartMenuClone.TB_H, new Color(30, 72, 140));
        g.setPaint(sky);
        g.fillRect(0, 0, StartMenuClone.SW, StartMenuClone.SH - StartMenuClone.TB_H);

        // Bintang berkedip
        for (int i = 0; i < app.starX.length; i++) {
            int b = app.starBright[i];
            g.setColor(new Color(b, b, Math.min(255, b + 30), b));
            int sz = (b > 200) ? 2 : 1;
            g.fillOval(app.starX[i] - sz / 2, app.starY[i] - sz / 2, sz + 1, sz + 1);
        }

        // Awan bercahaya
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
        for (int i = 0; i < app.cloudX.length; i++) {
            int cx = app.cloudX[i], cy = app.cloudY[i];
            Color cc = (i % 2 == 0) ? new Color(80, 120, 220) : new Color(140, 80, 200);
            g.setColor(cc);
            g.fillOval(cx - 55, cy - 20, 110, 40);
            g.fillOval(cx - 35, cy - 32, 70,  35);
            g.fillOval(cx + 10, cy - 18, 60,  32);
        }
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        // Cahaya bulan
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.28f));
        for (int r = 5; r >= 0; r--) {
            int alpha = 40 + r * 30;
            g.setColor(new Color(200, 220, 255, Math.min(255, alpha)));
            g.fillOval(StartMenuClone.SW - 100 - (r * 18), 18 - (r * 8), 70 + r * 18, 70 + r * 18);
        }
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g.setColor(new Color(240, 248, 255));
        g.fillOval(StartMenuClone.SW - 88, 22, 62, 62);
        g.setColor(new Color(210, 225, 248));
        g.fillOval(StartMenuClone.SW - 76, 30, 15, 12);
        g.fillOval(StartMenuClone.SW - 55, 50, 10,  9);
        g.fillOval(StartMenuClone.SW - 78, 55,  8,  7);

        // Pegunungan siluet
        g.setColor(new Color(8, 18, 42));
        int[] mx = {0,80,160,240,310,380,440,520,590,680,760,840,920, StartMenuClone.SW};
        int[] my = {StartMenuClone.SH-StartMenuClone.TB_H,
                StartMenuClone.SH-StartMenuClone.TB_H-80,  StartMenuClone.SH-StartMenuClone.TB_H-140,
                StartMenuClone.SH-StartMenuClone.TB_H-90,  StartMenuClone.SH-StartMenuClone.TB_H-170,
                StartMenuClone.SH-StartMenuClone.TB_H-100, StartMenuClone.SH-StartMenuClone.TB_H-155,
                StartMenuClone.SH-StartMenuClone.TB_H-85,  StartMenuClone.SH-StartMenuClone.TB_H-145,
                StartMenuClone.SH-StartMenuClone.TB_H-70,  StartMenuClone.SH-StartMenuClone.TB_H-130,
                StartMenuClone.SH-StartMenuClone.TB_H-95,  StartMenuClone.SH-StartMenuClone.TB_H-160,
                StartMenuClone.SH-StartMenuClone.TB_H};
        g.fillPolygon(mx, my, mx.length);

        // Kota cakrawala
        g.setColor(new Color(12, 25, 55));
        int[] bx = {40,70,100,130,160,190,220,260,290,330,360,400,440,500,540,580,630,680,730,780,830,880,930,970};
        int[] bh = {22,35,18, 40, 28, 15, 45, 30, 20, 42, 25, 18, 38, 22, 35, 15, 28, 40, 18, 32, 25, 42, 20, 35};
        for (int i = 0; i < bx.length; i++) {
            g.fillRect(bx[i], StartMenuClone.SH - StartMenuClone.TB_H - bh[i], 14 + (i % 3) * 4, bh[i]);
            if (i % 2 == 0) {
                g.setColor(new Color(255, 240, 150, 160));
                for (int row = 0; row < bh[i] / 8; row++)
                    for (int col = 0; col < 2; col++)
                        g.fillRect(bx[i] + 2 + col * 6,
                                StartMenuClone.SH - StartMenuClone.TB_H - bh[i] + 4 + row * 8, 3, 3);
                g.setColor(new Color(12, 25, 55));
            }
        }
}

    // ── Ikon Desktop ────────────────────────────────────────────────────
    void drawDesktopIcons(Graphics2D g) {
        for (int i = 0; i < app.deskIcons.length; i++) {
            int x = StartMenuClone.SW - 68, y = 30 + i * 80;
            g.setColor(new Color(0, 0, 0, 60));
            g.fillRoundRect(x + 3, y + 3, 46, 46, 8, 8);
            g.setColor(app.deskColors[i]);
            g.fillRoundRect(x, y, 46, 46, 8, 8);
            drawDesktopIconDetail(g, app.deskIcons[i], x, y, app.deskColors[i]);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Trebuchet MS", Font.PLAIN, 10));
            int tw = g.getFontMetrics().stringWidth(app.deskIcons[i]);
            g.setColor(new Color(0, 0, 0, 140));
            g.drawString(app.deskIcons[i], x + 23 - tw / 2 + 1, y + 57 + 1);
            g.setColor(Color.WHITE);
            g.drawString(app.deskIcons[i], x + 23 - tw / 2, y + 57);
        }
    }

    void drawDesktopIconDetail(Graphics2D g, String name, int x, int y, Color c) {
        switch (name) {
            case "Recycle Bin":
                g.setColor(new Color(255, 255, 255, 200));
                g.drawRoundRect(x + 10, y + 8, 26, 28, 4, 4);
                g.setColor(new Color(255, 255, 255, 160));
                g.drawLine(x + 7, y + 8, x + 39, y + 8);
                g.drawLine(x + 17, y + 5, x + 29, y + 5);
                for (int i = 0; i < 3; i++) g.drawLine(x + 14 + i * 6, y + 12, x + 14 + i * 6, y + 32);
                break;
            case "My Computer":
                g.setColor(new Color(200, 220, 255, 220));
                g.fillRoundRect(x + 8, y + 10, 30, 20, 3, 3);
                g.setColor(new Color(60, 140, 220));
                g.fillRect(x + 10, y + 12, 26, 15);
                g.setColor(new Color(200, 220, 255, 220));
                g.fillRect(x + 19, y + 30, 8, 6);
                g.fillRect(x + 13, y + 36, 20, 3);
                break;
            case "Network":
                g.setColor(new Color(255, 255, 255, 200));
                g.fillOval(x + 8, y + 8, 30, 30);
                g.setColor(c.darker());
                g.drawOval(x + 8, y + 8, 30, 30);
                g.drawLine(x + 23, y + 8, x + 23, y + 38);
                g.drawLine(x + 8, y + 23, x + 38, y + 23);
                g.drawOval(x + 13, y + 13, 20, 20);
                break;
            case "Documents":
                g.setColor(new Color(255, 255, 255, 220));
                g.fillRect(x + 10, y + 7, 22, 30);
                g.setColor(c.darker());
                g.drawRect(x + 10, y + 7, 22, 30);
                g.setColor(c);
                for (int i = 0; i < 4; i++) g.drawLine(x + 13, y + 13 + i * 6, x + 29, y + 13 + i * 6);
                break;
            default:
                g.setColor(new Color(255, 255, 255, 180));
                g.drawOval(x + 8, y + 8, 30, 30);
                g.drawLine(x + 23, y + 8, x + 23, y + 38);
                g.drawLine(x + 8, y + 23, x + 38, y + 23);
                g.drawOval(x + 13, y + 13, 20, 20);
        }
    }

    // =====================================================================
    //  TASKBAR
    // =====================================================================
    void drawTaskbar(Graphics2D g) {
        int y = StartMenuClone.SH - StartMenuClone.TB_H;
        GradientPaint gp = new GradientPaint(0, y, StartMenuClone.C_TB,
                0, StartMenuClone.SH, StartMenuClone.C_TB2);
        g.setPaint(gp);
        g.fillRect(0, y, StartMenuClone.SW, StartMenuClone.TB_H);
        g.setColor(new Color(80, 120, 200, 100));
        g.drawLine(0, y, StartMenuClone.SW, y);

        drawStartBtn(g, y);

        // Quick launch
        String[] ql = {"e", "📁", "🎵"};
        Color[]  qc = {new Color(35, 125, 215), new Color(235, 185, 35), new Color(185, 55, 55)};
        for (int i = 0; i < ql.length; i++) {
            int bx = 78 + i * 36, by = y + 6;
            g.setColor(new Color(255, 255, 255, 22));
            g.fillRoundRect(bx, by, 28, 28, 5, 5);
            g.setColor(qc[i]);
            g.fillRoundRect(bx + 2, by + 2, 24, 24, 4, 4);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.drawString(ql[i], bx + 7, by + 19);
        }

        // Jam & tanggal
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        g.setColor(Color.WHITE);
        g.setFont(StartMenuClone.F_CLOCK);
        g.drawString(time, StartMenuClone.SW - 70, y + 18);
        g.setFont(StartMenuClone.F_SMALL);
        g.setColor(new Color(200, 215, 235));
        g.drawString(date, StartMenuClone.SW - 70, y + 32);
        g.setColor(new Color(255, 255, 255, 55));
        g.drawLine(StartMenuClone.SW - 85, y + 5, StartMenuClone.SW - 85, y + StartMenuClone.TB_H - 5);
    }

    void drawStartBtn(Graphics2D g, int ty) {
        int w = 72, h = StartMenuClone.TB_H;
        boolean on = app.hovStart || app.menuOpen;
        GradientPaint gp = on
                ? new GradientPaint(0, ty, new Color(80, 175, 105), 0, ty + h, new Color(35, 115, 55))
                : new GradientPaint(0, ty, StartMenuClone.C_START1,  0, ty + h, StartMenuClone.C_START2);
        g.setPaint(gp);
        g.fillRect(0, ty, w, h);

        int ox = 8, oy = ty + 5, ow = h - 10;
        g.setColor(new Color(255, 255, 255, 45));
        g.fillOval(ox, oy, ow, ow);

        int cx = ox + ow / 2, cy = oy + ow / 2, s = 6, gap = 2;
        Color[] wc = {new Color(235, 75, 65), new Color(75, 175, 75),
                new Color(65, 125, 215), new Color(235, 185, 45)};
        g.setColor(wc[0]); g.fillRect(cx - s - gap, cy - s - gap, s, s);
        g.setColor(wc[1]); g.fillRect(cx + gap,      cy - s - gap, s, s);
        g.setColor(wc[2]); g.fillRect(cx - s - gap,  cy + gap,     s, s);
        g.setColor(wc[3]); g.fillRect(cx + gap,       cy + gap,     s, s);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Trebuchet MS", Font.BOLD, 13));
        g.drawString("Start", 36, ty + 25);
    }

    // =====================================================================
    //  START MENU
    // =====================================================================
    void drawMenu(Graphics2D g) {
        int py = (int) Math.round(app.menuPanelY);
        Graphics2D gc = (Graphics2D) g.create();
        gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        gc.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        gc.setClip(0, 0, StartMenuClone.MNU_W, StartMenuClone.SH - StartMenuClone.TB_H);
        gc.translate(0, py);

        GradientPaint lgp = new GradientPaint(0, 0, StartMenuClone.C_MENU_L,
                0, StartMenuClone.MNU_H, StartMenuClone.C_MENU_L2);
        gc.setPaint(lgp);
        gc.fillRect(0, 0, StartMenuClone.L_W, StartMenuClone.MNU_H);

        GradientPaint rgp = new GradientPaint(StartMenuClone.L_W, 0, StartMenuClone.C_MENU_R,
                StartMenuClone.L_W, StartMenuClone.MNU_H, StartMenuClone.C_MENU_R2);
        gc.setPaint(rgp);
        gc.fillRect(StartMenuClone.L_W, 0, StartMenuClone.R_W, StartMenuClone.MNU_H);

        gc.setColor(StartMenuClone.C_SEP);
        gc.drawRect(0, 0, StartMenuClone.MNU_W - 1, StartMenuClone.MNU_H - 1);
        gc.drawLine(StartMenuClone.L_W, 4, StartMenuClone.L_W,
                StartMenuClone.MNU_H - StartMenuClone.SRCH_H - 8);

        drawLeftCol(gc);
        drawRightCol(gc);
        drawSearchBar(gc);
        drawShutBtn(gc);
        if (app.shutDrop) drawShutDrop(gc);

        gc.dispose();
    }

    // ── Kolom Kiri ───────────────────────────────────────────────────────
    void drawLeftCol(Graphics2D g) {
        boolean searching = app.searchText.length() > 0;
        List<AppItem> list = app.buildSearchList(searching);

        boolean showBack = app.inAccessories && !searching;
        int backH = showBack ? 28 : 0;
        int aTop  = 4;
        int aBot  = StartMenuClone.MNU_H - StartMenuClone.SRCH_H - 8 - backH;
        int aH    = aBot - aTop;
        int maxSc = Math.max(0, list.size() * StartMenuClone.ITEM_H - aH);
        app.scrollOff = Math.max(0, Math.min(app.scrollOff, maxSc));

        Graphics2D gc = (Graphics2D) g.create();
        gc.setClip(0, aTop, StartMenuClone.L_W, aH);
        gc.translate(0, aTop - app.scrollOff);

        for (int i = 0; i < list.size(); i++) {
            AppItem a = list.get(i);
            int iy = i * StartMenuClone.ITEM_H;
            boolean hov = (i == app.hovLeft);

            if (hov) {
                GradientPaint hp = new GradientPaint(0, iy, StartMenuClone.C_HOV_L,
                        0, iy + StartMenuClone.ITEM_H, new Color(130, 185, 245, 200));
                gc.setPaint(hp);
                gc.fillRoundRect(3, iy + 1, StartMenuClone.L_W - 10, StartMenuClone.ITEM_H - 2, 6, 6);
                gc.setColor(new Color(90, 150, 210, 180));
                gc.drawRoundRect(3, iy + 1, StartMenuClone.L_W - 10, StartMenuClone.ITEM_H - 2, 6, 6);
            }

            drawIconSmall(gc, a, 6, iy + 3, 20);
            gc.setColor(a.isFolder() ? StartMenuClone.C_TXT_H : StartMenuClone.C_TXT);
            gc.setFont(a.isFolder() ? StartMenuClone.F_ITEM_B : StartMenuClone.F_ITEM);
            gc.drawString(a.name, 32, iy + 17);

            if (a.isFolder()) {
                gc.setColor(StartMenuClone.C_GRAY);
                gc.setFont(StartMenuClone.F_SMALL);
                gc.drawString("▶", StartMenuClone.L_W - 18, iy + 17);
            }
        }
        gc.dispose();

        // Back button
        if (showBack) {
            int by = aBot + 2;
            if (app.hovBack) {
                g.setColor(StartMenuClone.C_HOV_L);
                g.fillRoundRect(3, by, StartMenuClone.L_W - 10, backH - 2, 5, 5);
            }
            g.setColor(StartMenuClone.C_TXT_H);
            g.setFont(StartMenuClone.F_ITEM_B);
            g.drawString("◀  Back", 10, by + 18);
        }

        // Scrollbar
        if (maxSc > 0) {
            int bx = StartMenuClone.L_W - 8;
            g.setColor(new Color(200, 215, 235, 180));
            g.fillRoundRect(bx, aTop, 5, aH, 3, 3);
            int th = Math.max(18, aH * aH / (list.size() * StartMenuClone.ITEM_H));
            int ty = aTop + (int) ((double) app.scrollOff / maxSc * (aH - th));
            g.setColor(new Color(120, 155, 195));
            g.fillRoundRect(bx, ty, 5, th, 3, 3);
        }

        if (searching && list.isEmpty()) {
            g.setColor(StartMenuClone.C_GRAY);
            g.setFont(StartMenuClone.F_HINT);
            g.drawString("Tidak ada hasil untuk \"" + app.searchText + "\"", 10, 60);
        }
    }

    // ── Kolom Kanan ──────────────────────────────────────────────────────
    void drawRightCol(Graphics2D g) {
        int rx = StartMenuClone.L_W + 8;

        // Avatar profil
        int ax = StartMenuClone.MNU_W - 60, ay = 8, as = 48;
        g.setColor(new Color(255, 255, 255, 220));
        g.fillRoundRect(ax - 3, ay - 3, as + 6, as + 6, 6, 6);
        g.setColor(new Color(100, 140, 180));
        g.drawRoundRect(ax - 3, ay - 3, as + 6, as + 6, 6, 6);
        GradientPaint av = new GradientPaint(ax, ay, new Color(60, 130, 220),
                ax + as, ay + as, new Color(130, 50, 200));
        g.setPaint(av);
        g.fillRect(ax, ay, as, as);
        g.setColor(new Color(255, 255, 255, 180));
        g.fillOval(ax + 14, ay + 6, 20, 20);
        g.fillRoundRect(ax + 8, ay + 26, 32, 22, 10, 10);

        g.setColor(StartMenuClone.C_TXT_H);
        g.setFont(StartMenuClone.F_USER);
        g.drawString("Fikri", StartMenuClone.L_W + 8, ay + 32);
        g.setFont(StartMenuClone.F_SMALL);
        g.setColor(StartMenuClone.C_GRAY);
        g.drawString("Administrator", StartMenuClone.L_W + 8, ay + 46);

        g.setColor(new Color(160, 185, 215, 160));
        g.drawLine(StartMenuClone.L_W + 6, StartMenuClone.HEAD_H - 4,
                StartMenuClone.MNU_W - 6, StartMenuClone.HEAD_H - 4);

        int top = StartMenuClone.HEAD_H;
        for (int i = 0; i < app.rightList.size(); i++) {
            AppItem a = app.rightList.get(i);
            int iy   = top + i * StartMenuClone.ITEM_H;
            boolean hov = (i == app.hovRight);
            if (hov) {
                g.setColor(StartMenuClone.C_HOV_R);
                g.fillRoundRect(StartMenuClone.L_W + 4, iy + 1,
                        StartMenuClone.R_W - 10, StartMenuClone.ITEM_H - 2, 5, 5);
            }
            drawIconSmall(g, a, StartMenuClone.L_W + 6, iy + 3, 18);
            g.setColor(StartMenuClone.C_TXT_H);
            g.setFont(StartMenuClone.F_ITEM);
            g.drawString(a.name, rx + 22, iy + 16);
            if (a.hasArrow) {
                g.setColor(StartMenuClone.C_GRAY);
                g.setFont(StartMenuClone.F_SMALL);
                g.drawString("▶", StartMenuClone.MNU_W - 16, iy + 16);
            }
        }
    }

    // ── Search Bar ───────────────────────────────────────────────────────
    void drawSearchBar(Graphics2D g) {
        int sx = 6, sy = StartMenuClone.MNU_H - StartMenuClone.SRCH_H - 4,
                sw = StartMenuClone.L_W - 14;
        g.setColor(Color.WHITE);
        g.fillRoundRect(sx, sy, sw, StartMenuClone.SRCH_H, 5, 5);
        g.setColor(app.searchFocused ? new Color(70, 135, 215) : new Color(160, 175, 195));
        g.setStroke(new BasicStroke(app.searchFocused ? 2 : 1));
        g.drawRoundRect(sx, sy, sw, StartMenuClone.SRCH_H, 5, 5);
        g.setStroke(new BasicStroke(1));

        int lx = sx + 8, ly = sy + 7;
        g.setColor(StartMenuClone.C_GRAY);
        g.drawOval(lx, ly, 11, 11);
        g.setColor(app.searchFocused ? new Color(70, 135, 215) : StartMenuClone.C_GRAY);
        g.drawLine(lx + 9, ly + 9, lx + 14, ly + 14);

        String shown = (app.searchText.isEmpty() && !app.searchFocused)
                ? "Search programs and files" : app.searchText;
        g.setColor(app.searchText.isEmpty() ? new Color(150, 155, 170) : StartMenuClone.C_TXT);
        g.setFont(app.searchText.isEmpty() ? StartMenuClone.F_HINT : StartMenuClone.F_ITEM);
        g.drawString(shown, sx + 26, sy + 18);

        if (app.searchFocused && app.caretVis) {
            int tw = g.getFontMetrics(StartMenuClone.F_ITEM).stringWidth(app.searchText);
            g.setColor(StartMenuClone.C_TXT);
            g.drawLine(sx + 26 + tw + 1, sy + 5, sx + 26 + tw + 1, sy + StartMenuClone.SRCH_H - 5);
        }
    }

    // ── Tombol Shutdown ──────────────────────────────────────────────────
    void drawShutBtn(Graphics2D g) {
        int bx = StartMenuClone.L_W + 6, by = StartMenuClone.MNU_H - StartMenuClone.SRCH_H - 4,
                bw = StartMenuClone.R_W - 12, bh = StartMenuClone.SRCH_H, mw = bw - 22;

        GradientPaint gp1 = new GradientPaint(bx, by,
                app.hovShutMain ? new Color(90, 185, 110) : StartMenuClone.C_SHUT1,
                bx, by + bh,
                app.hovShutMain ? new Color(45, 125,  60) : StartMenuClone.C_SHUT2);
        g.setPaint(gp1);
        g.fillRoundRect(bx, by, mw, bh, 5, 5);
        g.setColor(Color.WHITE);
        g.setFont(StartMenuClone.F_ITEM_B);
        g.drawString("Shut down", bx + 10, by + 18);

        GradientPaint gp2 = new GradientPaint(bx + mw + 2, by,
                app.hovShutArrow ? new Color(90, 185, 110) : StartMenuClone.C_SHUT1,
                bx + mw + 2, by + bh,
                app.hovShutArrow ? new Color(45, 125,  60) : StartMenuClone.C_SHUT2);
        g.setPaint(gp2);
        g.fillRoundRect(bx + mw + 2, by, bw - mw - 2, bh, 5, 5);
        g.setColor(Color.WHITE);
        g.setFont(StartMenuClone.F_ITEM);
        g.drawString("▲", bx + mw + 8, by + 18);
    }

    void drawShutDrop(Graphics2D g) {
        int dw = 155, ih = 23;
        int dh = app.SHUT_OPTS.length * ih + 8;
        int dx = StartMenuClone.MNU_W - dw - 6,
                dy = StartMenuClone.MNU_H - StartMenuClone.SRCH_H - 4 - dh - 4;
        g.setColor(new Color(248, 250, 255));
        g.fillRoundRect(dx, dy, dw, dh, 6, 6);
        g.setColor(new Color(140, 160, 185));
        g.drawRoundRect(dx, dy, dw, dh, 6, 6);
        for (int i = 0; i < app.SHUT_OPTS.length; i++) {
            int iy = dy + 4 + i * ih;
            if (i == app.hovShutOpt) {
                g.setColor(StartMenuClone.C_HOV_L);
                g.fillRect(dx + 2, iy, dw - 4, ih);
            }
            g.setColor(StartMenuClone.C_TXT);
            g.setFont(StartMenuClone.F_ITEM);
            g.drawString(app.SHUT_OPTS[i], dx + 10, iy + 16);
        }
    }

    // ── Toast ─────────────────────────────────────────────────────────────
    void drawToast(Graphics2D g) {
        g.setFont(StartMenuClone.F_ITEM);
        int tw = g.getFontMetrics().stringWidth(app.toastText) + 28;
        int tx = (StartMenuClone.SW - tw) / 2,
                ty = StartMenuClone.SH - StartMenuClone.TB_H - 58;
        g.setColor(new Color(22, 22, 30, 220));
        g.fillRoundRect(tx, ty, tw, 34, 8, 8);
        g.setColor(new Color(100, 160, 255));
        g.drawRoundRect(tx, ty, tw, 34, 8, 8);
        g.setColor(Color.WHITE);
        g.drawString(app.toastText, tx + 14, ty + 22);
    }

    // ── Sleep Screen ─────────────────────────────────────────────────────
    void drawSleepScreen(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, StartMenuClone.SW, StartMenuClone.SH);
        long t     = System.currentTimeMillis();
        int  alpha = (int) (128 + 127 * Math.sin(t / 800.0));
        g.setColor(new Color(40, 80, 180, Math.max(0, Math.min(255, alpha))));
        g.fillOval(StartMenuClone.SW / 2 - 6, StartMenuClone.SH - 30, 12, 12);
    }

    // ── Shutdown Overlay ─────────────────────────────────────────────────
    void drawShutdownOverlay(Graphics2D g) {
        long el    = System.currentTimeMillis() - app.shutStartMs;
        int  alpha = (int) Math.min(255, el / 1500.0 * 255);
        g.setColor(new Color(0, 0, 0, alpha));
        g.fillRect(0, 0, StartMenuClone.SW, StartMenuClone.SH);
        g.setColor(new Color(255, 255, 255, Math.min(255, alpha + 60)));
        g.setFont(new Font("Georgia", Font.PLAIN, 22));
        String msg = app.restarting ? "Restarting..." : "Shutting down...";
        int    tw  = g.getFontMetrics().stringWidth(msg);
        g.drawString(msg, StartMenuClone.SW / 2 - tw / 2, StartMenuClone.SH / 2);
    }

    // =====================================================================
    //  FAKE WINDOWS
    // =====================================================================
    void drawFakeWindow(Graphics2D g, FakeWindow w) {
        g.setColor(new Color(0, 0, 0, 65));
        g.fillRoundRect(w.x + 5, w.y + 5, w.w, w.h, 8, 8);

        g.setColor(new Color(240, 244, 250));
        g.fillRoundRect(w.x, w.y, w.w, w.h, 6, 6);
        g.setColor(new Color(110, 135, 165));
        g.drawRoundRect(w.x, w.y, w.w, w.h, 6, 6);

        GradientPaint tgp = new GradientPaint(w.x, w.y, new Color(110, 160, 220),
                w.x, w.y + 28, new Color(65, 115, 180));
        g.setPaint(tgp);
        g.fillRoundRect(w.x, w.y, w.w, 28, 6, 6);
        g.fillRect(w.x, w.y + 14, w.w, 14);

        drawIconSmall(g, new AppItem(w.title, w.iconType, "", w.iconColor), w.x + 6, w.y + 6, 16);
        g.setColor(Color.WHITE);
        g.setFont(StartMenuClone.F_ITEM_B);
        g.drawString(w.title, w.x + 26, w.y + 19);

        drawWinBtn(g, w.x + w.w - 58, w.y + 6, new Color(90, 160,  90), "─");
        drawWinBtn(g, w.x + w.w - 38, w.y + 6, new Color(90,  90, 180), "□");
        drawWinBtn(g, w.x + w.w - 18, w.y + 6, new Color(205, 65,  55), "✕");

        w.closeX = w.x + w.w - 18;
        w.closeY = w.y + 6;
        w.closeW = 16;
        w.closeH = 16;

        Graphics2D body = (Graphics2D) g.create();
        body.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        body.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        body.setClip(w.x + 2, w.y + 29, w.w - 4, w.h - 31);
        body.translate(w.x, w.y + 29);
        int cw = w.w - 4, ch = w.h - 31;

        switch (w.iconType) {
            case "calc":         drawCalcContent(body, cw, ch); break;
            case "notepad":      drawNotepadContent(body, cw, ch); break;
            case "wordpad":      drawWordpadContent(body, cw, ch, w); break;
            case "paint":        drawPaintContent(body, cw, ch, w); break;
            case "cmd":          drawCmdContent(body, cw, ch, w); break;
            case "browser":      drawBrowserContent(body, cw, ch, w); break;
            case "mediaplayer":  drawMediaContent(body, cw, ch, w); break;
            case "snip":         drawSnipContent(body, cw, ch, w); break;
            case "sticky":       drawStickyContent(body, cw, ch, w); break;
            case "run":          drawRunContent(body, cw, ch, w); break;
            case "computer":     drawComputerContent(body, cw, ch, w); break;
            case "controlpanel": drawControlPanelContent(body, cw, ch, w); break;
            case "printer":      drawPrinterContent(body, cw, ch, w); break;
            case "help":         drawHelpContent(body, cw, ch, w); break;
            case "game":         drawGameContent(body, cw, ch, w); break;
            case "folder_r":     drawFolderContent(body, cw, ch, w); break;
            default:             drawGenericContent(body, cw, ch, w); break;
        }
        body.dispose();
    }

    void drawWinBtn(Graphics2D g, int x, int y, Color c, String sym) {
        g.setColor(c);
        g.fillRoundRect(x, y, 16, 16, 3, 3);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.drawString(sym, x + 3, y + 12);
    }

    // ── Konten Calculator ────────────────────────────────────────────────
    void drawCalcContent(Graphics2D g, int cw, int ch) {
        g.setColor(new Color(230, 232, 235));
        g.fillRect(0, 0, cw, ch);
        g.setColor(new Color(245, 250, 255));
        g.fillRect(6, 6, cw - 12, 32);
        g.setColor(new Color(140, 155, 175));
        g.drawRect(6, 6, cw - 12, 32);
        g.setColor(StartMenuClone.C_TXT);
        g.setFont(new Font("Courier New", Font.BOLD, 18));
        g.drawString("0", cw - 22, 30);

        String[][] keys = {{"MC","MR","MS","M+"}, {"7","8","9","/"}, {"4","5","6","*"},
                {"1","2","3","-"}, {"0",".","+/-","+"}};
        Color[] kc = {new Color(210, 215, 225), new Color(225, 230, 240)};
        for (int r = 0; r < keys.length; r++) for (int c = 0; c < 4; c++) {
            int bx = 6 + c * (cw - 12) / 4 + 2, by = 46 + r * 26;
            int bw = (cw - 12) / 4 - 4, bh = 22;
            g.setColor(kc[(r + c) % 2]);
            g.fillRoundRect(bx, by, bw, bh, 4, 4);
            g.setColor(new Color(160, 168, 185));
            g.drawRoundRect(bx, by, bw, bh, 4, 4);
            g.setColor(StartMenuClone.C_TXT);
            g.setFont(new Font("Trebuchet MS", Font.PLAIN, 11));
            int tw = g.getFontMetrics().stringWidth(keys[r][c]);
            g.drawString(keys[r][c], bx + bw / 2 - tw / 2, by + 15);
        }
        g.setColor(new Color(55, 120, 210));
        g.fillRoundRect(cw - 48, ch - 26, 42, 22, 4, 4);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Trebuchet MS", Font.BOLD, 13));
        g.drawString("=", cw - 29, ch - 10);
    }

    // ── Konten Notepad ───────────────────────────────────────────────────
    void drawNotepadContent(Graphics2D g, int cw, int ch) {
        g.setColor(new Color(240, 240, 244));
        g.fillRect(0, 0, cw, 18);
        g.setColor(StartMenuClone.C_SEP);
        g.drawLine(0, 18, cw, 18);
        String[] menus = {"File","Edit","Format","View","Help"};
        int mx = 6;
        for (String m : menus) {
            g.setColor(StartMenuClone.C_TXT);
            g.setFont(StartMenuClone.F_SMALL);
            g.drawString(m, mx, 13);
            mx += g.getFontMetrics().stringWidth(m) + 14;
        }
        g.setColor(Color.WHITE);
        g.fillRect(0, 19, cw, ch - 19);
        g.setColor(new Color(200, 220, 240));
        for (int i = 0; i < 20; i++) g.drawLine(0, 35 + i * 14, cw, 35 + i * 14);
        g.setColor(StartMenuClone.C_TXT);
        g.setFont(StartMenuClone.F_MONO);
        g.drawString("Ketik sesuatu di sini...", 6, 30);
        if (app.caretVis) { g.setColor(StartMenuClone.C_TXT); g.drawLine(6, 32, 6, 20); }
    }

    // ── Konten WordPad ───────────────────────────────────────────────────
    void drawWordpadContent(Graphics2D g, int cw, int ch, FakeWindow w) {
        g.setColor(new Color(235, 238, 245));
        g.fillRect(0, 0, cw, 26);
        g.setColor(StartMenuClone.C_SEP);
        g.drawLine(0, 26, cw, 26);
        String[] tbBtns = {"B","I","U","≡","≡","≡"};
        for (int i = 0; i < tbBtns.length; i++) {
            g.setColor(new Color(215, 222, 232));
            g.fillRoundRect(4 + i * 26, 3, 22, 20, 3, 3);
            g.setColor(new Color(55, 55, 55));
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            g.drawString(tbBtns[i], 9 + i * 26, 16);
        }
        g.setColor(new Color(180, 185, 195));
        g.fillRect(0, 27, cw, ch - 27);
        g.setColor(Color.WHITE);
        g.fillRect(14, 32, cw - 28, ch - 40);
        g.setColor(StartMenuClone.C_TXT);
        g.setFont(new Font("Georgia", Font.PLAIN, 12));
        g.drawString("Dokumen Baru", 20, 50);
        g.setFont(new Font("Trebuchet MS", Font.PLAIN, 11));
        g.setColor(StartMenuClone.C_GRAY);
        g.drawString("Mulai mengetik...", 20, 66);
    }

    // ── Konten Paint ─────────────────────────────────────────────────────
    void drawPaintContent(Graphics2D g, int cw, int ch, FakeWindow w) {
        g.setColor(new Color(238, 240, 245));
        g.fillRect(0, 0, cw, 24);
        Color[] palette = {Color.BLACK, Color.WHITE, Color.RED, Color.ORANGE, Color.YELLOW,
                Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, new Color(140, 60, 20)};
        for (int i = 0; i < palette.length; i++) {
            g.setColor(palette[i]);
            g.fillRect(4 + i * 20, 4, 16, 16);
            g.setColor(new Color(100, 100, 100));
            g.drawRect(4 + i * 20, 4, 16, 16);
        }
        g.setColor(Color.WHITE);
        g.fillRect(0, 25, cw, ch - 25);
        g.setColor(new Color(220, 100, 60)); g.fillOval(18, 40, 55, 55);
        g.setColor(new Color(60, 140, 220)); g.fillRect(85, 50, 50, 40);
        g.setColor(new Color(60, 190, 80));
        int[] px = {155,175,195}, py = {100,50,100};
        g.fillPolygon(px, py, 3);
        g.setColor(new Color(200, 160, 60)); g.fillArc(30, 110, 60, 50, 0, 180);
        g.setColor(new Color(180, 50, 180)); g.fillRoundRect(110, 105, 55, 35, 8, 8);
        g.setColor(new Color(255, 190, 40, 160)); g.fillOval(50, 85, 30, 20);
        g.setColor(new Color(40, 120, 255, 140)); g.fillOval(130, 75, 25, 25);
    }

    // ── Konten Command Prompt ────────────────────────────────────────────
    void drawCmdContent(Graphics2D g, int cw, int ch, FakeWindow w) {
        g.setColor(new Color(12, 12, 12));
        g.fillRect(0, 0, cw, ch);
        g.setColor(new Color(60, 220, 60));
        g.setFont(StartMenuClone.F_MONO);
        String[] lines = {"Microsoft Windows [Version 7.0]",
                "(c) 2009 Microsoft Corporation.", "",
                "C:\\Users\\Fikri>" + (app.caretVis ? "_" : "")};
        for (int i = 0; i < lines.length; i++) g.drawString(lines[i], 6, 18 + i * 18);
    }

    // ── Konten Browser ───────────────────────────────────────────────────
    void drawBrowserContent(Graphics2D g, int cw, int ch, FakeWindow w) {
        g.setColor(new Color(238, 240, 245));
        g.fillRect(0, 0, cw, 28);
        g.setColor(StartMenuClone.C_SEP);
        g.drawLine(0, 28, cw, 28);
        g.setColor(Color.WHITE);
        g.fillRoundRect(50, 4, cw - 90, 20, 3, 3);
        g.setColor(new Color(150, 170, 195));
        g.drawRoundRect(50, 4, cw - 90, 20, 3, 3);
        g.setColor(StartMenuClone.C_GRAY);
        g.setFont(StartMenuClone.F_SMALL);
        g.drawString("www.google.com", 56, 18);
        g.setColor(new Color(200, 210, 225));
        g.fillRoundRect(4, 5, 18, 18, 4, 4);
        g.fillRoundRect(24, 5, 18, 18, 4, 4);
        g.setColor(StartMenuClone.C_TXT);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.drawString("◀", 7, 17); g.drawString("▶", 27, 17);
        g.setColor(Color.WHITE);
        g.fillRect(0, 29, cw, ch - 29);
        String ggl = "Google";
        int gx = cw / 2 - 38, gy = 60;
        g.setFont(new Font("Georgia", Font.BOLD, 28));
        Color[] gColors = {new Color(66, 133, 244), new Color(234, 67, 53),
                new Color(251, 188, 5), new Color(52, 168, 83),
                new Color(234, 67, 53), new Color(66, 133, 244)};
        for (int i = 0; i < ggl.length(); i++) {
            g.setColor(gColors[i % gColors.length]);
            g.drawString(String.valueOf(ggl.charAt(i)), gx + i * 22, gy);
        }
        g.setColor(Color.WHITE);
        g.fillRoundRect(cw / 2 - 80, gy + 12, 160, 22, 11, 11);
        g.setColor(new Color(200, 210, 225));
        g.drawRoundRect(cw / 2 - 80, gy + 12, 160, 22, 11, 11);
        g.setColor(new Color(200, 200, 200));
        g.setFont(StartMenuClone.F_HINT);
        g.drawString("Cari dengan Google", cw / 2 - 60, gy + 27);
    }

    // ── Konten Media Player ──────────────────────────────────────────────
    void drawMediaContent(Graphics2D g, int cw, int ch, FakeWindow w) {
        g.setColor(new Color(15, 15, 20));
        g.fillRect(0, 0, cw, ch);
        GradientPaint ap = new GradientPaint(cw / 2 - 40, 10, new Color(40, 80, 160),
                cw / 2 + 40, 80, new Color(120, 30, 120));
        g.setPaint(ap);
        g.fillRoundRect(cw / 2 - 40, 10, 80, 80, 8, 8);
        g.setColor(new Color(20, 20, 20)); g.fillOval(cw / 2 - 15, 35, 30, 30);
        g.setColor(new Color(80, 80, 80)); g.fillOval(cw / 2 - 6, 44, 12, 12);
        g.setColor(new Color(200, 220, 255, 200));
        g.setFont(new Font("Trebuchet MS", Font.BOLD, 11));
        g.drawString("Now Playing", cw / 2 - 30, 105);
        g.setColor(new Color(150, 160, 185));
        g.setFont(StartMenuClone.F_SMALL);
        g.drawString("Unknown Artist – Track 1", cw / 2 - 52, 119);
        int bx = 10, by = ch - 45, bw = cw - 20, bh = 4;
        g.setColor(new Color(45, 48, 60)); g.fillRoundRect(bx, by, bw, bh, 2, 2);
        g.setColor(new Color(30, 160, 215)); g.fillRoundRect(bx, by, (int)(bw * 0.35), bh, 2, 2);
        String[] ctrls = {"⏮","⏪","⏯","⏩","⏭"};
        for (int i = 0; i < ctrls.length; i++) {
            int cx = cw / 2 - 52 + i * 26;
            g.setColor(i == 2 ? new Color(30, 160, 215) : new Color(150, 165, 185));
            g.setFont(new Font("SansSerif", Font.PLAIN, i == 2 ? 18 : 14));
            g.drawString(ctrls[i], cx, ch - 16);
        }
        g.setColor(new Color(80, 85, 100));
        g.setFont(StartMenuClone.F_SMALL);
        g.drawString("1:24", bx, by - 4);
        g.drawString("3:58", bx + bw - 22, by - 4);
    }

    // ── Snipping Tool ────────────────────────────────────────────────────
    void drawSnipContent(Graphics2D g, int cw, int ch, FakeWindow w) {
        g.setColor(new Color(240, 242, 245)); g.fillRect(0, 0, cw, ch);
        g.setColor(new Color(200, 210, 225)); g.fillRect(0, 0, cw, 28);
        g.setColor(StartMenuClone.C_TXT);
        g.setFont(StartMenuClone.F_ITEM_B);
        g.drawString("✂  Snipping Tool", 8, 18);
        g.setColor(StartMenuClone.C_SEP); g.drawLine(0, 28, cw, 28);
        g.setColor(StartMenuClone.C_GRAY);
        g.setFont(StartMenuClone.F_HINT);
        g.drawString("Klik 'New' untuk mengambil tangkapan layar", 10, 60);
        g.setColor(new Color(55, 125, 210));
        g.fillRoundRect(10, 74, 60, 22, 5, 5);
        g.setColor(Color.WHITE); g.setFont(StartMenuClone.F_ITEM_B);
        g.drawString("New", 18, 89);
        g.setColor(new Color(210, 215, 225)); g.fillRect(10, 105, cw - 20, ch - 115);
        g.setColor(StartMenuClone.C_GRAY); g.setFont(StartMenuClone.F_HINT);
        int tw = g.getFontMetrics().stringWidth("Area pratinjau");
        g.drawString("Area pratinjau", (cw - tw) / 2, (ch - 115) / 2 + 120);
    }

    // ── Sticky Notes ─────────────────────────────────────────────────────
    void drawStickyContent(Graphics2D g, int cw, int ch, FakeWindow w) {
        g.setColor(new Color(255, 235, 80)); g.fillRect(0, 0, cw, ch);
        g.setColor(new Color(230, 210, 50)); g.fillRect(0, 0, cw, 20);
        g.setColor(new Color(180, 160, 20));
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.drawString("+ Sticky Notes", 4, 14);
        g.setColor(new Color(100, 90, 10));
        g.setFont(new Font("Trebuchet MS", Font.PLAIN, 12));
        g.drawString("Catatan: Jangan lupa", 6, 40);
        g.drawString("mengerjakan tugas!", 6, 58);
        if (app.caretVis) { g.setColor(new Color(80, 70, 5)); g.drawLine(6, 80, 6, 68); }
    }

    // ── Run Dialog ───────────────────────────────────────────────────────
    void drawRunContent(Graphics2D g, int cw, int ch, FakeWindow w) {
        g.setColor(new Color(242, 244, 248)); g.fillRect(0, 0, cw, ch);
        g.setColor(new Color(80, 140, 220)); g.fillRoundRect(10, 10, 36, 36, 6, 6);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString("▶", 18, 34);
        g.setColor(StartMenuClone.C_TXT); g.setFont(StartMenuClone.F_ITEM);
        g.drawString("Ketik nama program, folder, atau", 54, 24);
        g.drawString("dokumen yang ingin dibuka:", 54, 40);
        g.setColor(Color.WHITE); g.fillRect(10, 60, cw - 20, 22);
        g.setColor(new Color(150, 170, 195)); g.drawRect(10, 60, cw - 20, 22);
        g.setColor(StartMenuClone.C_TXT); g.setFont(StartMenuClone.F_MONO);
        g.drawString("notepad" + (app.caretVis ? "|" : ""), 14, 76);
        g.setColor(new Color(210, 215, 228)); g.fillRoundRect(cw - 130, ch - 30, 55, 22, 4, 4);
        g.setColor(new Color(90, 155, 210));  g.fillRoundRect(cw - 70,  ch - 30, 55, 22, 4, 4);
        g.setColor(StartMenuClone.C_TXT); g.setFont(StartMenuClone.F_ITEM);
        g.drawString("Cancel", cw - 125, ch - 14);
        g.setColor(Color.WHITE);
        g.drawString("   OK", cw - 65, ch - 14);
    }

    // ── My Computer ──────────────────────────────────────────────────────
    void drawComputerContent(Graphics2D g, int cw, int ch, FakeWindow w) {
        g.setColor(new Color(240, 243, 248)); g.fillRect(0, 0, cw, ch);
        g.setColor(new Color(210, 220, 238)); g.fillRect(0, 0, 80, ch);
        g.setColor(StartMenuClone.C_SEP); g.drawLine(80, 0, 80, ch);
        String[] nav = {"Favorites","Desktop","Downloads","Libraries","Computer","Network"};
        for (int i = 0; i < nav.length; i++) {
            if (i == 4) { g.setColor(new Color(180, 210, 245)); g.fillRect(0, 8 + i * 22, 80, 20); }
            g.setColor(StartMenuClone.C_TXT); g.setFont(StartMenuClone.F_SMALL);
            g.drawString(nav[i], 4, 22 + i * 22);
        }
        String[] drives  = {"Local Disk (C:)","Local Disk (D:)","DVD Drive (E:)"};
        Color[]  dclr    = {new Color(60,130,200),new Color(60,180,80),new Color(200,80,50)};
        for (int i = 0; i < drives.length; i++) {
            int dx = 90 + i * 80, dy = 20;
            g.setColor(dclr[i]); g.fillRoundRect(dx, dy, 52, 42, 5, 5);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            g.drawString(i < 2 ? "💾" : "💿", dx + 14, dy + 28);
            g.setColor(new Color(200, 215, 235)); g.fillRect(dx, dy + 44, 52, 6);
            g.setColor(i == 0 ? new Color(210,70,50) : new Color(60,150,60));
            g.fillRect(dx, dy + 44, (int)(52 * (i == 0 ? 0.82 : 0.35)), 6);
            g.setColor(StartMenuClone.C_TXT);
            g.setFont(new Font("Trebuchet MS", Font.PLAIN, 9));
            g.drawString(drives[i], dx - 4, dy + 60);
        }
    }

    // ── Control Panel ─────────────────────────────────────────────────────
    void drawControlPanelContent(Graphics2D g, int cw, int ch, FakeWindow w) {
        g.setColor(new Color(240, 243, 248)); g.fillRect(0, 0, cw, ch);
        String[] items = {"System","Display","Network","Sound","Users","Security","Updates","Themes"};
        Color[]  ic    = {new Color(55,120,210),new Color(200,140,40),new Color(60,165,80),
                new Color(140,60,200),new Color(50,150,220),new Color(200,60,60),
                new Color(60,180,60),new Color(180,80,160)};
        int cols = 4;
        for (int i = 0; i < items.length; i++) {
            int col = i % cols, row = i / cols;
            int ix = 8 + col * 70, iy = 10 + row * 64;
            g.setColor(ic[i]); g.fillRoundRect(ix + 10, iy, 36, 36, 6, 6);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            g.drawString(items[i].substring(0, 1), ix + 22, iy + 23);
            g.setColor(StartMenuClone.C_TXT);
            g.setFont(new Font("Trebuchet MS", Font.PLAIN, 9));
            int tw = g.getFontMetrics().stringWidth(items[i]);
            g.drawString(items[i], ix + 28 - tw / 2, iy + 50);
        }
    }

    // ── Devices & Printers ────────────────────────────────────────────────
    void drawPrinterContent(Graphics2D g, int cw, int ch, FakeWindow w) {
        g.setColor(new Color(240, 243, 248)); g.fillRect(0, 0, cw, ch);
        String[] devs = {"Printer HP","Scanner","Keyboard","Mouse","Monitor","USB Hub"};
        Color[]  dc   = {new Color(55,110,190),new Color(60,170,80),new Color(180,80,50),
                new Color(140,60,200),new Color(40,140,210),new Color(190,140,40)};
        for (int i = 0; i < devs.length; i++) {
            int dx = 10 + (i % 3) * 90, dy = 10 + (i / 3) * 70;
            g.setColor(dc[i]); g.fillRoundRect(dx + 12, dy, 50, 44, 5, 5);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            g.drawString(i==0?"🖨":i==1?"📷":i==2?"⌨":i==3?"🖱":i==4?"🖥":"🔌", dx + 16, dy + 33);
            g.setColor(StartMenuClone.C_TXT); g.setFont(StartMenuClone.F_SMALL);
            int tw = g.getFontMetrics().stringWidth(devs[i]);
            g.drawString(devs[i], dx + 37 - tw / 2, dy + 58);
        }
    }

    // ── Help & Support ────────────────────────────────────────────────────
    void drawHelpContent(Graphics2D g, int cw, int ch, FakeWindow w) {
        g.setColor(Color.WHITE); g.fillRect(0, 0, cw, ch);
        g.setColor(new Color(235, 240, 248)); g.fillRect(0, 0, cw, 28);
        g.setColor(Color.WHITE); g.fillRoundRect(6, 4, cw - 40, 20, 3, 3);
        g.setColor(StartMenuClone.C_SEP); g.drawRoundRect(6, 4, cw - 40, 20, 3, 3);
        g.setColor(StartMenuClone.C_GRAY); g.setFont(StartMenuClone.F_HINT);
        g.drawString("Cari bantuan...", 10, 17);
        g.setColor(StartMenuClone.C_TXT_H);
        g.setFont(new Font("Georgia", Font.BOLD, 13));
        g.drawString("Windows Help and Support", 10, 50);
        String[] topics = {"• Memulai dengan Windows 7","• Pengaturan Jaringan",
                "• Backup dan Restore","• Pemecahan Masalah","• Tentang Windows 7"};
        for (int i = 0; i < topics.length; i++) {
            g.setColor(new Color(30, 100, 200));
            g.setFont(StartMenuClone.F_ITEM);
            g.drawString(topics[i], 10, 72 + i * 20);
        }
    }

    // ── Games ─────────────────────────────────────────────────────────────
    void drawGameContent(Graphics2D g, int cw, int ch, FakeWindow w) {
        g.setColor(new Color(20, 22, 38)); g.fillRect(0, 0, cw, ch);
        String[] games = {"Solitaire","Minesweeper","Chess","Mahjong","Pinball"};
        Color[]  gc2   = {new Color(40,160,80),new Color(200,60,60),new Color(220,200,100),
                new Color(180,80,180),new Color(60,120,200)};
        for (int i = 0; i < games.length; i++) {
            int gx = 10 + (i % 3) * 90, gy = 10 + (i / 3) * 75;
            GradientPaint gp = new GradientPaint(gx, gy, gc2[i].brighter(), gx, gy + 48, gc2[i].darker());
            g.setPaint(gp); g.fillRoundRect(gx, gy, 78, 48, 8, 8);
            g.setColor(new Color(255, 255, 255, 180));
            g.setFont(new Font("Georgia", Font.BOLD, 10));
            int tw = g.getFontMetrics().stringWidth(games[i]);
            g.drawString(games[i], gx + 39 - tw / 2, gy + 30);
            g.setColor(new Color(255, 255, 255, 100));
            g.setFont(StartMenuClone.F_SMALL);
            g.drawString(games[i], gx + 5, gy + 62);
        }
    }

    // ── Folder Contents ───────────────────────────────────────────────────
    void drawFolderContent(Graphics2D g, int cw, int ch, FakeWindow w) {
        g.setColor(new Color(240, 243, 248)); g.fillRect(0, 0, cw, ch);
        g.setColor(new Color(220, 228, 240)); g.fillRect(0, 0, cw, 24);
        g.setColor(StartMenuClone.C_SEP); g.drawLine(0, 24, cw, 24);
        g.setColor(StartMenuClone.C_TXT); g.setFont(StartMenuClone.F_SMALL);
        g.drawString("◀  ▶  ↑  " + w.title, 6, 16);
        String[] files;
        switch (w.title) {
            case "Documents": files = new String[]{"Laporan.docx","Skripsi.pdf","Catatan.txt","Presentasi.pptx"}; break;
            case "Pictures":  files = new String[]{"Liburan.jpg","Foto.png","Wallpaper.jpg","Screenshot.png"}; break;
            case "Music":     files = new String[]{"Song1.mp3","Playlist.m3u","Album.flac","Podcast.mp3"}; break;
            case "Videos":    files = new String[]{"Video1.mp4","Rekaman.avi","Tutorial.mkv","Film.mp4"}; break;
            case "Downloads": files = new String[]{"Setup.exe","Archive.zip","eBook.pdf","Update.msi"}; break;
            default:          files = new String[]{"File1.txt","File2.txt","File3.txt"};
        }
        for (int i = 0; i < files.length; i++) {
            if (i % 2 == 0) { g.setColor(new Color(230, 238, 250)); g.fillRect(0, 28 + i * 22, cw, 22); }
            g.setColor(w.iconColor); g.fillRect(8, 32 + i * 22, 12, 14);
            g.setColor(StartMenuClone.C_TXT); g.setFont(StartMenuClone.F_ITEM);
            g.drawString(files[i], 26, 44 + i * 22);
        }
    }

    // ── Generic ───────────────────────────────────────────────────────────
    void drawGenericContent(Graphics2D g, int cw, int ch, FakeWindow w) {
        g.setColor(Color.WHITE); g.fillRect(0, 0, cw, ch);
        g.setColor(w.iconColor); g.fillRoundRect(cw / 2 - 28, ch / 2 - 42, 56, 56, 10, 10);
        drawIconSmall(g, new AppItem(w.title, w.iconType, "", w.iconColor), cw / 2 - 22, ch / 2 - 38, 44);
        g.setColor(StartMenuClone.C_TXT);
        g.setFont(new Font("Trebuchet MS", Font.PLAIN, 12));
        int tw = g.getFontMetrics().stringWidth(w.title + " (simulasi)");
        g.drawString(w.title + " (simulasi)", cw / 2 - tw / 2, ch / 2 + 28);
        g.setColor(StartMenuClone.C_GRAY); g.setFont(StartMenuClone.F_HINT);
        tw = g.getFontMetrics().stringWidth("Klik X untuk menutup");
        g.drawString("Klik X untuk menutup", cw / 2 - tw / 2, ch / 2 + 46);
    }

    // =====================================================================
    //  IKON KECIL – digambar manual
    // =====================================================================
    void drawIconSmall(Graphics2D g, AppItem a, int x, int y, int sz) {
        switch (a.iconType) {
            case "folder":
            case "folder_r":
                g.setColor(a.color); g.fillRoundRect(x, y + 3, sz, sz - 5, 3, 3);
                g.setColor(a.color.brighter()); g.fillRect(x, y + 1, sz / 2, 5);
                g.setColor(a.color.darker()); g.drawRoundRect(x, y + 3, sz, sz - 5, 3, 3);
                break;
            case "calc":
                g.setColor(new Color(70, 75, 85)); g.fillRoundRect(x, y, sz, sz, 3, 3);
                g.setColor(new Color(200, 225, 210)); g.fillRect(x + 2, y + 2, sz - 4, sz / 3);
                g.setColor(new Color(180, 200, 180));
                for (int r = 0; r < 3; r++) for (int c = 0; c < 3; c++)
                    g.fillRect(x + 2 + c * (sz - 4) / 3, y + sz / 3 + 3 + r * ((sz * 2 / 3 - 5) / 3), sz / 4, sz / 5);
                break;
            case "cmd":
                g.setColor(new Color(10, 10, 10)); g.fillRoundRect(x, y, sz, sz, 3, 3);
                g.setColor(new Color(50, 215, 50));
                g.setFont(new Font("Monospaced", Font.BOLD, sz > 16 ? 9 : 7));
                g.drawString(">_", x + 2, y + sz - 4);
                break;
            case "notepad":
                g.setColor(Color.WHITE); g.fillRect(x + 1, y, sz - 2, sz);
                g.setColor(new Color(130, 130, 130)); g.drawRect(x + 1, y, sz - 2, sz);
                g.setColor(new Color(80, 140, 210));
                for (int i = 0; i < 3; i++) g.drawLine(x + 3, y + 4 + i * 5, x + sz - 3, y + 4 + i * 5);
                break;
            case "wordpad":
                g.setColor(new Color(30, 120, 215)); g.fillRoundRect(x, y, sz, sz, 3, 3);
                g.setColor(Color.WHITE); g.fillRect(x + 3, y + 3, sz - 6, sz - 6);
                g.setColor(new Color(80, 80, 90));
                for (int i = 0; i < 3; i++) g.drawLine(x + 4, y + 6 + i * 4, x + sz - 4, y + 6 + i * 4);
                break;
            case "paint":
                g.setColor(new Color(215, 215, 215)); g.fillOval(x, y + 2, sz, sz - 4);
                g.setColor(Color.RED);    g.fillOval(x + 2,        y + 4, 5, 5);
                g.setColor(Color.BLUE);   g.fillOval(x + sz - 7,   y + 4, 5, 5);
                g.setColor(Color.GREEN);  g.fillOval(x + sz / 2 - 3, y + sz - 8, 5, 5);
                g.setColor(Color.YELLOW); g.fillOval(x + sz / 2 - 3, y + 4, 5, 5);
                break;
            case "browser":
                g.setColor(new Color(30, 120, 215)); g.fillOval(x, y, sz, sz);
                g.setColor(Color.WHITE); g.drawOval(x + 2, y + 2, sz - 4, sz - 4);
                g.drawLine(x + sz / 2, y + 2, x + sz / 2, y + sz - 2);
                g.drawLine(x + 2, y + sz / 2, x + sz - 2, y + sz / 2);
                break;
            case "mediaplayer":
                g.setColor(new Color(20, 135, 155)); g.fillRoundRect(x, y, sz, sz, 5, 5);
                g.setColor(Color.WHITE);
                int[] px = {x+4, x+sz-4, x+4}, py = {y+3, y+sz/2, y+sz-3};
                g.fillPolygon(px, py, 3);
                break;
            case "snip":
                g.setColor(new Color(195, 75, 35)); g.fillRoundRect(x, y, sz, sz, 3, 3);
                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, sz > 16 ? 11 : 8));
                g.drawString("✂", x + 2, y + sz - 3);
                break;
            case "sticky":
                g.setColor(new Color(245, 215, 70)); g.fillRect(x, y, sz, sz);
                g.setColor(new Color(215, 185, 30)); g.fillRect(x, y, sz, sz / 4);
                break;
            case "run":
                g.setColor(new Color(120, 125, 135)); g.fillRoundRect(x, y, sz, sz, 3, 3);
                g.setColor(Color.WHITE);
                int[] px2 = {x+4, x+sz-3, x+4}, py2 = {y+3, y+sz/2, y+sz-3};
                g.fillPolygon(px2, py2, 3);
                break;
            case "computer":
                g.setColor(new Color(200, 205, 215)); g.fillRoundRect(x, y + 2, sz, sz - 6, 2, 2);
                g.setColor(new Color(70, 140, 200)); g.fillRect(x + 2, y + 4, sz - 4, sz - 12);
                g.setColor(new Color(140, 145, 155));
                g.fillRect(x + sz / 2 - 3, y + sz - 4, 6, 3);
                g.fillRect(x + 2, y + sz - 2, sz - 4, 2);
                break;
            case "controlpanel":
                g.setColor(new Color(100, 108, 120)); g.fillRoundRect(x, y, sz, sz, 3, 3);
                g.setColor(new Color(200, 210, 225)); g.drawOval(x + 3, y + 3, sz - 6, sz - 6);
                g.setColor(new Color(220, 225, 235)); g.fillOval(x + sz / 2 - 3, y + sz / 2 - 3, 6, 6);
                break;
            case "printer":
                g.setColor(new Color(40, 140, 145)); g.fillRoundRect(x, y, sz, sz, 3, 3);
                g.setColor(Color.WHITE); g.fillRect(x + 3, y + sz / 3, sz - 6, sz / 3);
                g.setColor(new Color(200, 230, 230)); g.fillRect(x + 4, y + sz / 3 + 1, sz - 8, 4);
                break;
            case "help":
                g.setColor(new Color(35, 110, 195)); g.fillOval(x, y, sz, sz);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Georgia", Font.BOLD, sz > 16 ? 13 : 9));
                g.drawString("?", x + sz / 2 - 4, y + sz - 3);
                break;
            case "game":
                g.setColor(new Color(180, 50, 50)); g.fillRoundRect(x, y, sz, sz, 4, 4);
                g.setColor(Color.WHITE);
                g.drawOval(x + 2, y + sz / 2 - 3, 6, 6);
                g.drawLine(x + sz / 2, y + 3, x + sz / 2, y + sz - 3);
                g.drawLine(x + sz - 8, y + sz / 2 - 3, x + sz - 2, y + sz / 2 + 3);
                break;
            default:
                g.setColor(a.color); g.fillRoundRect(x, y, sz, sz, 4, 4);
                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, a.label.length() > 1 ? 7 : 9));
                int tw2 = g.getFontMetrics().stringWidth(a.label);
                g.drawString(a.label, x + (sz - tw2) / 2, y + sz - 4);
        }
    }
}