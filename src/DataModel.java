import java.awt.Color;

class AppItem {
    String  name, iconType, label;
    Color   color;
    boolean hasArrow;

    AppItem(String n, String it, String lb, Color c) {
        this(n, it, lb, c, false);
    }

    AppItem(String n, String it, String lb, Color c, boolean ha) {
        name     = n;
        iconType = it;
        label    = lb;
        color    = c;
        hasArrow = ha;
    }

    /** Mengembalikan true jika item ini adalah folder (Accessories). */
    boolean isFolder() {
        return iconType.equals("folder");
    }

    /** Mengembalikan true jika nama item mengandung query pencarian (case-insensitive). */
    boolean matches(String q) {
        return name.toLowerCase().contains(q.toLowerCase());
    }
}

class FakeWindow {
    String  title     = "";
    String  iconType  = "generic";
    Color   iconColor = Color.GRAY;

    int x, y, w, h;
    int closeX, closeY, closeW = 16, closeH = 16;

    boolean blink = true;
}