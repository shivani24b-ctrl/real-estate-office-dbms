import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AgentLogin extends JFrame {

    private final JTextField  idField    = new JTextField();
    private final JTextField  phoneField = new JTextField();
    private final JLabel      errorLabel = new JLabel(" ");

    public AgentLogin() {
        setTitle("Agent Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 360);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel();
        header.setBackground(Theme.HEADER_BG);
        header.setBorder(Theme.emptyBorder(20, 24));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Agent Portal");
        title.setFont(Theme.TITLE);
        title.setForeground(Theme.HEADER_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub = new JLabel("Sign in with your Agent ID and phone");
        sub.setFont(Theme.SMALL);
        sub.setForeground(new Color(0xBFD7F5));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(sub);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG);
        form.setBorder(Theme.emptyBorder(20, 32));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.weightx = 1;

        styleField(idField);
        styleField(phoneField);
        idField.setToolTipText("Your numeric Agent ID");
        phoneField.setToolTipText("Registered phone number");

        gbc.gridy = 0; form.add(fieldLabel("Agent ID"), gbc);
        gbc.gridy = 1; form.add(idField, gbc);
        gbc.gridy = 2; form.add(Box.createVerticalStrut(6), gbc);
        gbc.gridy = 3; form.add(fieldLabel("Phone Number"), gbc);
        gbc.gridy = 4; form.add(phoneField, gbc);
        gbc.gridy = 5; form.add(Box.createVerticalStrut(4), gbc);

        errorLabel.setFont(Theme.SMALL);
        errorLabel.setForeground(Theme.DANGER);
        gbc.gridy = 6; form.add(errorLabel, gbc);

        JButton loginBtn = new JButton("Sign In");
        loginBtn.setFont(Theme.LABEL);
        loginBtn.setBackground(Theme.SUCCESS);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBorderPainted(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setPreferredSize(new Dimension(0, 38));
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.addActionListener(e -> attemptLogin());
        phoneField.addActionListener(e -> attemptLogin());

        gbc.gridy = 7; form.add(loginBtn, gbc);
        gbc.gridy = 8; form.add(Box.createVerticalStrut(8), gbc);

        JButton backBtn = new JButton("Back to Role Selection");
        backBtn.setFont(Theme.SMALL);
        backBtn.setForeground(Theme.TEXT_MID);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> { new RoleSelector().setVisible(true); dispose(); });
        gbc.gridy = 9; form.add(backBtn, gbc);

        add(header, BorderLayout.NORTH);
        add(form,   BorderLayout.CENTER);
    }

    @SuppressWarnings("unchecked")
    private void attemptLogin() {
        String id    = idField.getText().trim();
        String phone = phoneField.getText().trim();

        if (id.isEmpty() || phone.isEmpty()) {
            errorLabel.setText("Please enter both Agent ID and phone.");
            return;
        }
        try {
            Integer.parseInt(id);
        } catch (NumberFormatException e) {
            errorLabel.setText("Agent ID must be a number.");
            return;
        }

        Object[] result = QueryRunner.loginAgent(id, phone);
        List<Object[]> rows = (List<Object[]>) result[1];

        if (rows.isEmpty()) {
            errorLabel.setText("Invalid credentials or agent is not active.");
            return;
        }

        Object[] agentRow = rows.get(0);
        int agentId     = ((Number) agentRow[0]).intValue();
        String agentName = agentRow[1].toString();

        new AgentPortal(agentId, agentName).setVisible(true);
        dispose();
    }

    private void styleField(JTextField f) {
        f.setFont(Theme.BODY);
        f.setBackground(Theme.SURFACE);
        f.setForeground(Theme.TEXT);
        f.setPreferredSize(new Dimension(0, 36));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.LABEL);
        l.setForeground(Theme.TEXT_MID);
        return l;
    }
}
