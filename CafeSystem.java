import javax.swing.SwingUtilities;

public class CafeSystem {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MenuWindow();
        });
    }
}
