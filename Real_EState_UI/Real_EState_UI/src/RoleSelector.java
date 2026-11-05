import javax.swing.*;
import java.awt.*;

public class RoleSelector extends JFrame {

    public RoleSelector() {
        setTitle("Real Estate Office");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(440, 380);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel();
        header.setBackground(Theme.HEADER_BG);
        header.setBorder(Theme.emptyBorder(24, 24));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Real Estate Office");
        title.setFont(Theme.TITLE);
        title.setForeground(Theme.HEADER_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub = new JLabel("Select your role to continue");
        sub.setFont(Theme.BODY);
        sub.setForeground(new Color(0xBFD7F5));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(sub);

        // Buttons
        JPanel body = new JPanel(new GridLayout(2, 1, 0, 14));
        body.setBackground(Theme.BG);
        body.setBorder(Theme.emptyBorder(28, 36));

        JButton officeBtn = makeRoleButton("Office Dashboard",
            "View sales and rental reports", Theme.PRIMARY, Theme.PRIMARY_DIM);
        JButton agentBtn  = makeRoleButton("Agent Portal",
            "Log in to manage your properties", Theme.SUCCESS, Theme.SUCCESS_DIM);

        officeBtn.addActionListener(e -> {
            new OfficeDashboard().setVisible(true);
            dispose();
        });
        agentBtn.addActionListener(e -> {
            new AgentLogin().setVisible(true);
            dispose();
        });

        body.add(officeBtn);
        body.add(agentBtn);

        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    private JButton makeRoleButton(String title, String subtitle, Color accent, Color bg) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() :
                            getModel().isRollover() ? bg : Theme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.setFont(Theme.HEADING);
                g2.setColor(accent);
                g2.drawString(title, 18, 32);
                g2.setFont(Theme.SMALL);
                g2.setColor(Theme.TEXT_MID);
                g2.drawString(subtitle, 18, 52);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(0, 70));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}


