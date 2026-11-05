import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        try { DBConnection.getConnection(); }
        catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null,
                "Cannot connect to database.\n" + e.getMessage(),
                "Connection Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        SwingUtilities.invokeLater(() -> new RoleSelector().setVisible(true));
    }
}
