import java.awt.Color;

// =============================================================================
//  DataModel.java
//  Berisi dua kelas data yang digunakan di seluruh aplikasi:
//    • AppItem   – merepresentasikan satu entri di daftar Start Menu
//    • FakeWindow – merepresentasikan jendela aplikasi palsu di desktop
//
//  Jobdesk : mengelola struktur data / model objek aplikasi
// =============================================================================

/**
 * Model data untuk satu item aplikasi di Start Menu.
 *
 * Field:
 *   name     – label yang ditampilkan di menu
 *   iconType – kunci tipe ikon (digunakan Renderer untuk memilih gambar)
 *   label    – teks singkat cadangan jika tidak ada ikon khusus
 *   color    – warna utama ikon
 *   hasArrow – apakah item ini menampilkan tanda panah (▶) di kolom kanan
 */
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

// -----------------------------------------------------------------------------

/**
 * Merepresentasikan sebuah jendela aplikasi palsu yang tampil di desktop.
 *
 * Field:
 *   title     – judul jendela (sama dengan AppItem.name yang diluncurkan)
 *   iconType  – dipakai Renderer untuk memilih konten yang digambar di body
 *   iconColor – warna ikon / aksen jendela
 *   x, y      – posisi pojok kiri-atas jendela di layar
 *   w, h      – lebar dan tinggi jendela
 *   closeX/Y/W/H – koordinat & ukuran tombol ✕ (untuk hit-test klik)
 *   blink     – dipakai konten tertentu (CMD, Notepad) untuk efek kursor
 */
class FakeWindow {
    String  title     = "";
    String  iconType  = "generic";
    Color   iconColor = Color.GRAY;

    int x, y, w, h;
    int closeX, closeY, closeW = 16, closeH = 16;

    boolean blink = true;
}