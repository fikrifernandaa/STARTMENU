import java.awt.EventQueue;

/**
 * Entry point aplikasi Tiruan Start Menu Windows 7.
 *
 * Cara kompilasi dan menjalankan:
 *   javac *.java
 *   java Main
 */
public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(StartMenuClone::new);
    }
}