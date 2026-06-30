import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StartMenuClone extends Frame implements Runnable {

    // =====================================================================
    //  KONSTANTA – Dimensi & Warna & Font
    // =====================================================================

    // Dimensi layar
    static final int SW     = 1024, SH = 660;
    static final int TB_H   = 42;           // tinggi taskbar
    static final int MNU_W  = 420;          // lebar total menu
    static final int MNU_H  = 460;          // tinggi total menu
    static final int L_W    = 255;          // kolom kiri
    static final int R_W    = MNU_W - L_W;  // kolom kanan
    static final int ITEM_H = 26;
    static final int SRCH_H = 28;
    static final int HEAD_H = 70;

    // Warna tema
    static final Color C_TB     = new Color(22,   30,  50);
    static final Color C_TB2    = new Color(10,   18,  36);
    static final Color C_START1 = new Color(50,  155,  90);
    static final Color C_START2 = new Color(20,   95,  50);
    static final Color C_MENU_L  = new Color(248, 250, 255, 252);
    static final Color C_MENU_L2 = new Color(218, 232, 248, 252);
    static final Color C_MENU_R  = new Color(210, 225, 245, 252);
    static final Color C_MENU_R2 = new Color(185, 208, 238, 252);
    static final Color C_HOV_L  = new Color(170, 210, 255, 200);
    static final Color C_HOV_R  = new Color(155, 200, 250, 200);
    static final Color C_SEP    = new Color(150, 175, 205);
    static final Color C_TXT_H  = new Color(25,   50, 120);
    static final Color C_TXT    = new Color(18,   18,  18);
    static final Color C_GRAY   = new Color(110, 115, 130);
    static final Color C_SHUT1  = new Color(55,  155,  75);
    static final Color C_SHUT2  = new Color(25,   95,  40);

    // Font
    static final Font F_TITLE  = new Font("Georgia",      Font.BOLD,  14);
    static final Font F_ITEM   = new Font("Trebuchet MS",  Font.PLAIN, 12);
    static final Font F_ITEM_B = new Font("Trebuchet MS",  Font.BOLD,  12);
    static final Font F_SMALL  = new Font("Trebuchet MS",  Font.PLAIN, 11);
    static final Font F_MONO   = new Font("Courier New",   Font.PLAIN, 12);
    static final Font F_CLOCK  = new Font("Georgia",       Font.BOLD,  13);
    static final Font F_USER   = new Font("Georgia",       Font.BOLD,  13);
    static final Font F_HINT   = new Font("Trebuchet MS",  Font.ITALIC,11);

    // =====================================================================
    //  STATE
    // =====================================================================
    Canvas canvas;
    volatile boolean running = true;

    // Menu animasi
    boolean menuOpen  = false;
    boolean animating = false;
    boolean opening   = false;
    double  animProg  = 0.0;
    double  menuPanelY = SH;

    // Navigasi menu
    boolean inAccessories = false;
    int     scrollOff     = 0;
    String  searchText    = "";
    boolean searchFocused = false;
    boolean caretVis      = true;
    long    caretMs       = 0;

    // Hover state
    int     hovLeft = -1, hovRight = -1;
    boolean hovStart = false, hovShutMain = false, hovShutArrow = false, hovBack = false;

    // Power dropdown
    boolean shutDrop    = false;
    int     hovShutOpt  = -1;

    final String[] SHUT_OPTS = {"Switch user","Log off","Lock","Sleep","Restart","Shut down"};

    // Power state
    boolean sleeping     = false;
    boolean shuttingDown = false;
    long    shutStartMs  = 0;
    boolean restarting   = false;

    // Toast notifikasi
    String toastText  = null;
    long   toastUntil = 0;

    // Fake windows
    List<FakeWindow> windows = new ArrayList<>();
    FakeWindow dragging;
    int dragDX, dragDY;

    // Data aplikasi
    List<AppItem> mainList  = new ArrayList<>();
    List<AppItem> accList   = new ArrayList<>();
    List<AppItem> rightList = new ArrayList<>();

    long lastFrame;

    // Wallpaper: bintang & awan
    int[] starX, starY, starBright;
    int[] cloudX = {120, 310, 580, 780, 920};
    int[] cloudY = {60, 140, 90, 55, 130};

    // Ikon desktop
    String[] deskIcons  = {"Recycle Bin","My Computer","Network","Documents","Browser"};
    Color[]  deskColors = {new Color(90,180,90), new Color(100,140,200),
            new Color(200,140,50), new Color(80,150,220), new Color(50,160,200)};

    // Helper
    private Renderer renderer;
    private InputHandler inputHandler;

    // Posisi mouse terakhir & status drag (dibaca InputHandler.processScroll)
    int lastMouseX = -1, lastMouseY = -1;
    private boolean mouseButtonDown = false;

    // =====================================================================
    //  KONSTRUKTOR
    // =====================================================================
    public StartMenuClone() {
        setTitle("Start Menu Clone – Final Project Grafika Komputer");
        setSize(SW, SH);
        setResizable(false);
        setLayout(null);

        canvas = new Canvas();
        canvas.setBounds(0, 0, SW, SH);
        canvas.setFocusable(true);
        add(canvas);

        buildData();
        buildStars();

        renderer     = new Renderer(this);
        inputHandler = new InputHandler(this);

        setVisible(true);
        canvas.createBufferStrategy(2);
        canvas.requestFocus();

        // Daftarkan SATU listener global ke Toolkit (bukan addMouseListener/
        // addKeyListener/addWindowListener per komponen). Listener ini hanya
        // menjadi jembatan: ia membaca koordinat/kode tombol dari event mentah
        // lalu meneruskannya sebagai nilai primitif (int/char/boolean) ke
        // InputHandler, yang sama sekali tidak bergantung pada java.awt.event.
        java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(
                this::dispatchRawEvent,
                java.awt.AWTEvent.MOUSE_EVENT_MASK
                        | java.awt.AWTEvent.MOUSE_MOTION_EVENT_MASK
                        | java.awt.AWTEvent.MOUSE_WHEEL_EVENT_MASK
                        | java.awt.AWTEvent.KEY_EVENT_MASK
                        | java.awt.AWTEvent.WINDOW_EVENT_MASK
        );

        lastFrame = System.currentTimeMillis();
        new Thread(this, "render-loop").start();
    }

    private void dispatchRawEvent(java.awt.AWTEvent ev) {
        if (ev instanceof java.awt.event.MouseWheelEvent) {
            java.awt.event.MouseWheelEvent we = (java.awt.event.MouseWheelEvent) ev;
            inputHandler.processScroll(we.getWheelRotation());
            return;
        }
        if (ev instanceof java.awt.event.MouseEvent) {
            java.awt.event.MouseEvent me = (java.awt.event.MouseEvent) ev;
            int id = me.getID();
            int mx = me.getX(), my = me.getY();
            lastMouseX = mx; lastMouseY = my;
            if (id == java.awt.event.MouseEvent.MOUSE_PRESSED) {
                mouseButtonDown = true;
                inputHandler.processClick(mx, my);
            } else if (id == java.awt.event.MouseEvent.MOUSE_RELEASED) {
                mouseButtonDown = false;
                inputHandler.processRelease();
            } else if (id == java.awt.event.MouseEvent.MOUSE_MOVED
                    || id == java.awt.event.MouseEvent.MOUSE_DRAGGED) {
                inputHandler.processMouseMove(mx, my, mouseButtonDown);
            }
            return;
        }
        if (ev instanceof java.awt.event.KeyEvent) {
            java.awt.event.KeyEvent ke = (java.awt.event.KeyEvent) ev;
            if (ke.getID() == java.awt.event.KeyEvent.KEY_TYPED) {
                inputHandler.processCharTyped(ke.getKeyChar());
            } else if (ke.getID() == java.awt.event.KeyEvent.KEY_PRESSED) {
                inputHandler.processSpecialKey(ke.getKeyCode());
            }
            return;
        }
        if (ev instanceof java.awt.event.WindowEvent) {
            java.awt.event.WindowEvent we = (java.awt.event.WindowEvent) ev;
            if (we.getID() == java.awt.event.WindowEvent.WINDOW_CLOSING) {
                running = false; dispose(); System.exit(0);
            }
        }
    }

    // =====================================================================
    //  DATA
    // =====================================================================
    void buildData() {
        // Kolom kiri: program utama
        mainList.add(new AppItem("Internet Explorer",   "browser",     "IE",  new Color(30,120,210)));
        mainList.add(new AppItem("Windows Media Player","mediaplayer", "",    new Color(20,140,160)));
        mainList.add(new AppItem("Windows DVD Maker",   "generic",     "D",   new Color(210,130,30)));
        mainList.add(new AppItem("Microsoft Store",  "generic",     "MS",   new Color(110,110,110)));
        mainList.add(new AppItem("Calendar",      "generic",     "C",  new Color(60,120,170)));
        mainList.add(new AppItem("Windows Update",      "generic",     "UPD", new Color(190,55,55)));
        mainList.add(new AppItem("WPS Office",          "generic",     "WPS",   new Color(120,75,165)));
        mainList.add(new AppItem("All Program",         "folder",      "",    new Color(225,190,70)));

        // Kolom kiri: accessories
        accList.add(new AppItem("Calculator",           "calc",    "",   new Color(80,80,90)));
        accList.add(new AppItem("Command Prompt",       "cmd",     "",   new Color(12,12,12)));
        accList.add(new AppItem("Notepad",              "notepad", "",   new Color(240,240,240)));
        accList.add(new AppItem("Paint",                "paint",   "",   new Color(220,220,220)));
        accList.add(new AppItem("Snipping Tool",        "snip",    "",   new Color(200,80,40)));
        accList.add(new AppItem("Sound Recorder",       "generic", "SR", new Color(80,80,195)));
        accList.add(new AppItem("Sticky Notes",         "sticky",  "",   new Color(240,215,80)));
        accList.add(new AppItem("Word",              "wordpad", "",   new Color(30,130,220)));
        accList.add(new AppItem("Run",                  "run",     "",   new Color(130,130,130)));

        // Kolom kanan
        rightList.add(new AppItem("Documents",          "folder_r",    "",    new Color(55,125,195)));
        rightList.add(new AppItem("Pictures",           "folder_r",    "",    new Color(50,160,85)));
        rightList.add(new AppItem("Music",              "folder_r",    "",    new Color(160,55,160)));
        rightList.add(new AppItem("Videos",             "folder_r",    "",    new Color(195,55,55)));
        rightList.add(new AppItem("Downloads",          "folder_r",    "",    new Color(185,130,35)));
        rightList.add(new AppItem("Games",              "game",        "",    new Color(185,55,55),  true));
        rightList.add(new AppItem("Computer",           "computer",    "",    new Color(140,145,155)));
        rightList.add(new AppItem("Control Panel",      "controlpanel","",    new Color(100,105,115),true));
        rightList.add(new AppItem("Devices & Printers", "printer",     "",    new Color(45,145,145)));
        rightList.add(new AppItem("Default Programs",   "generic",     "DP",  new Color(75,135,205)));
        rightList.add(new AppItem("Help & Support",     "help",        "",    new Color(38,115,195)));
        rightList.add(new AppItem("Run...",             "run",         "",    new Color(85,85,85)));
    }

    void buildStars() {
        starX = new int[120]; starY = new int[120]; starBright = new int[120];
        for (int i = 0; i < 120; i++) {
            starX[i]      = (int)(Math.random() * SW);
            starY[i]      = (int)(Math.random() * (SH - TB_H));
            starBright[i] = 80 + (int)(Math.random() * 175);
        }
    }

    // =====================================================================
    //  GAME LOOP
    // =====================================================================
    @Override
    public void run() {
        while (running) {
            long now = System.currentTimeMillis();
            long dt  = now - lastFrame; lastFrame = now;
            update(dt);
            renderer.render();
            try { Thread.sleep(14); } catch (InterruptedException ignored) {}
        }
    }

    void update(long dt) {
        // Animasi buka/tutup menu
        if (animating) {
            double sp = dt / 200.0;
            if (opening) { animProg += sp; if (animProg >= 1) { animProg = 1; animating = false; } }
            else         { animProg -= sp; if (animProg <= 0) { animProg = 0; animating = false; menuOpen = false; } }
        }
        menuPanelY = (SH - TB_H) - MNU_H * animProg;

        // Kursor berkedip
        caretMs += dt;
        if (caretMs > 520) { caretMs = 0; caretVis = !caretVis; }

        // Power actions
        if (shuttingDown && System.currentTimeMillis() - shutStartMs > 1600) System.exit(0);
        if (restarting   && System.currentTimeMillis() - shutStartMs > 1000) {
            running = false; dispose();
            System.out.println("Restarting...");
            new StartMenuClone();
        }

        // Toast timeout
        if (toastText != null && System.currentTimeMillis() > toastUntil) toastText = null;

        // Twinkle bintang
        for (int i = 0; i < starBright.length; i++) {
            starBright[i] += (int)((Math.random() - 0.5) * 18);
            starBright[i]  = Math.max(40, Math.min(255, starBright[i]));
        }
    }

    /** Mengembalikan true jika menu sepenuhnya terbuka dan siap menerima input. */
    boolean menuReady() {
        return menuOpen && !animating && animProg >= 0.999;
    }

    // =====================================================================
    //  AKSI
    // =====================================================================
    void toggleMenu() {
        shutDrop = false;
        if (!menuOpen) {
            menuOpen = true; opening = true; animating = true;
            inAccessories = false; searchText = ""; searchFocused = false; scrollOff = 0;
        } else {
            opening = false; animating = true;
        }
    }

    void closeMenu() {
        if (menuOpen) { opening = false; animating = true; }
        shutDrop = false;
    }

    void launchApp(AppItem a) {
        // Jika sudah terbuka, bawa ke depan
        for (FakeWindow fw : windows) {
            if (fw.title.equals(a.name)) {
                windows.remove(fw); windows.add(fw);
                closeMenu(); return;
            }
        }
        FakeWindow fw   = new FakeWindow();
        fw.title        = a.name;
        fw.iconColor    = a.color;
        fw.iconType     = a.iconType;
        int n           = windows.size();
        fw.x = 100 + (n % 6) * 32;
        fw.y = 50  + (n % 5) * 28;
        fw.w = getWindowWidth(a.iconType);
        fw.h = getWindowHeight(a.iconType);
        windows.add(fw);
        closeMenu();
    }

    int getWindowWidth(String type) {
        switch (type) {
            case "calc":   return 220;
            case "sticky": return 200;
            case "run":    return 310;
            case "cmd":    return 380;
            default:       return 340;
        }
    }

    int getWindowHeight(String type) {
        switch (type) {
            case "calc":        return 230;
            case "sticky":      return 180;
            case "run":         return 170;
            case "browser":     return 250;
            case "mediaplayer": return 230;
            default:            return 240;
        }
    }

    void showToast(String txt) {
        toastText  = txt;
        toastUntil = System.currentTimeMillis() + 1600;
    }

    void doPowerAction(String opt) {
        shutDrop = false;
        switch (opt) {
            case "Shut down": shuttingDown = true; shutStartMs = System.currentTimeMillis(); break;
            case "Restart":   restarting   = true; shutStartMs = System.currentTimeMillis(); break;
            case "Sleep":     sleeping = true; menuOpen = false; animProg = 0; break;
            default:          showToast("Simulasi: " + opt);
        }
    }

    List<AppItem> buildSearchList(boolean searching) {
        if (!searching) return inAccessories ? accList : mainList;
        List<AppItem> res = new ArrayList<>();
        for (AppItem a : mainList)  if (!a.isFolder() && a.matches(searchText)) res.add(a);
        for (AppItem a : accList)   if (a.matches(searchText))                  res.add(a);
        for (AppItem a : rightList) if (a.matches(searchText))                  res.add(a);
        return res;
    }
}