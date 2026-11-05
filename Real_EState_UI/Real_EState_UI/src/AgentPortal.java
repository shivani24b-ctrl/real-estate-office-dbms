import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AgentPortal extends JFrame {

    private final int    agentId;
    private final String agentName;
    private final ResultTable tablePanel = new ResultTable();

    public AgentPortal(int agentId, String agentName) {
        this.agentId   = agentId;
        this.agentName = agentName;

        setTitle("Agent Portal - " + agentName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 640);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMain(),   BorderLayout.CENTER);

        loadProperties();
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.HEADER_BG);
        p.setBorder(Theme.emptyBorder(14, 20));

        JLabel left = new JLabel("Agent Portal");
        left.setFont(Theme.HEADING);
        left.setForeground(Theme.HEADER_TEXT);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setBackground(Theme.HEADER_BG);

        JLabel agentLbl = new JLabel(agentName + "  |  ID " + agentId);
        agentLbl.setFont(Theme.SMALL);
        agentLbl.setForeground(new Color(0xBFD7F5));

        JButton refresh = new JButton("Refresh");
        refresh.setFont(Theme.SMALL);
        refresh.setForeground(new Color(0xBFD7F5));
        refresh.setBackground(Theme.HEADER_BG);
        refresh.setBorderPainted(false);
        refresh.setContentAreaFilled(false);
        refresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refresh.addActionListener(e -> loadProperties());

        JButton signOut = new JButton("Sign Out");
        signOut.setFont(Theme.SMALL);
        signOut.setForeground(new Color(0xFCA5A5));
        signOut.setBackground(Theme.HEADER_BG);
        signOut.setBorderPainted(false);
        signOut.setContentAreaFilled(false);
        signOut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signOut.addActionListener(e -> { new RoleSelector().setVisible(true); dispose(); });

        right.add(agentLbl);
        right.add(refresh);
        right.add(signOut);
        p.add(left,  BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JPanel buildMain() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(Theme.BG);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Theme.BG);
        topBar.setBorder(Theme.emptyBorder(14, 20));

        JLabel titleLbl = new JLabel("My Properties");
        titleLbl.setFont(Theme.HEADING);
        titleLbl.setForeground(Theme.TEXT);

        JButton dealBtn = new JButton("Close a Deal") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()  ? Theme.PRIMARY.darker()  :
                            getModel().isRollover() ? Theme.PRIMARY.brighter() : Theme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        dealBtn.setFont(Theme.LABEL);
        dealBtn.setContentAreaFilled(false);
        dealBtn.setBorderPainted(false);
        dealBtn.setFocusPainted(false);
        dealBtn.setOpaque(false);
        dealBtn.setPreferredSize(new Dimension(140, 36));
        dealBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dealBtn.addActionListener(e -> openDealDialog());

        topBar.add(titleLbl, BorderLayout.WEST);
        topBar.add(dealBtn,  BorderLayout.EAST);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Theme.SURFACE);
        tableWrapper.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Theme.BORDER));
        tableWrapper.add(tablePanel, BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Theme.BG);
        content.setBorder(Theme.emptyBorder(0, 16));
        content.add(tableWrapper, BorderLayout.CENTER);

        p.add(topBar,  BorderLayout.NORTH);
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    private void loadProperties() {
        SwingWorker<Object[], Void> w = new SwingWorker<>() {
            @Override protected Object[] doInBackground() { return QueryRunner.agentProperties(agentId); }
            @Override protected void done() {
                try { tablePanel.loadData(get(), "My Properties"); }
                catch (Exception e) { tablePanel.setStatus("Error: " + e.getMessage(), false); }
            }
        };
        w.execute();
    }

    @SuppressWarnings("unchecked")
    private void openDealDialog() {
        Object[] propResult = QueryRunner.agentProperties(agentId);
        List<Object[]> props = (List<Object[]>) propResult[1];

        if (props.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "You have no active properties to close a deal on.",
                "No Properties", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] propOptions = props.stream()
            .map(r -> r[0] + " - " + r[1] + " (" + r[2] + ")")
            .toArray(String[]::new);

        // ── Form ──────────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG);
        form.setBorder(Theme.emptyBorder(8, 4));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.insets  = new Insets(4, 4, 4, 4);
        gbc.weightx = 1;

        JComboBox<String> propBox   = new JComboBox<>(propOptions);
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"rented", "sold"});
        JTextField amountField      = new JTextField();
        JTextField listedField      = new JTextField("2025-01-01");
        JTextField closedField      = new JTextField("2025-04-19");

        // Owner auto-fetched label
        JLabel ownerLabel = new JLabel();
        ownerLabel.setFont(Theme.SMALL);
        ownerLabel.setForeground(Theme.TEXT_MID);

        propBox.addActionListener(e -> {
            int idx = propBox.getSelectedIndex();
            if (idx >= 0) {
                Object[] row = props.get(idx);
                ownerLabel.setText("Owner: " + row[8] + "  (ID " + row[9] + ")");
            }
        });
        // Set initial owner label
        Object[] firstRow = props.get(0);
        ownerLabel.setText("Owner: " + firstRow[8] + "  (ID " + firstRow[9] + ")");

        // New client fields
        JTextField clientNameField  = new JTextField();
        JTextField clientPhoneField = new JTextField();
        JTextField clientEmailField = new JTextField();

        int y = 0;
        gbc.gridy = y++; form.add(formLabel("Property"), gbc);
        gbc.gridy = y++; form.add(propBox, gbc);
        gbc.gridy = y++; form.add(ownerLabel, gbc);

        gbc.gridy = y++; form.add(separator(), gbc);

        gbc.gridy = y++; form.add(formLabel("Deal Type"), gbc);
        gbc.gridy = y++; form.add(statusBox, gbc);
        gbc.gridy = y++; form.add(formLabel("Amount (Rs)"), gbc);
        gbc.gridy = y++; form.add(amountField, gbc);
        gbc.gridy = y++; form.add(formLabel("Date Listed (YYYY-MM-DD)"), gbc);
        gbc.gridy = y++; form.add(listedField, gbc);
        gbc.gridy = y++; form.add(formLabel("Date Closed (YYYY-MM-DD)"), gbc);
        gbc.gridy = y++; form.add(closedField, gbc);

        gbc.gridy = y++; form.add(separator(), gbc);

        gbc.gridy = y++; form.add(sectionHeader("New Client Details"), gbc);
        gbc.gridy = y++; form.add(formLabel("Client Name"), gbc);
        gbc.gridy = y++; form.add(clientNameField, gbc);
        gbc.gridy = y++; form.add(formLabel("Client Phone"), gbc);
        gbc.gridy = y++; form.add(clientPhoneField, gbc);
        gbc.gridy = y++; form.add(formLabel("Client Email"), gbc);
        gbc.gridy = y++; form.add(clientEmailField, gbc);

        int result = JOptionPane.showConfirmDialog(this, form,
            "Close a Deal", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        // ── Validate ──────────────────────────────────────────────────────────
        String clientName  = clientNameField.getText().trim();
        String clientPhone = clientPhoneField.getText().trim();
        String clientEmail = clientEmailField.getText().trim();

        if (clientName.isEmpty() || clientPhone.isEmpty() || clientEmail.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Client name, phone and email are all required.",
                "Missing Fields", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Amount must be a valid number.", "Input Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ── Submit ────────────────────────────────────────────────────────────
        int propIdx    = propBox.getSelectedIndex();
        Object[] pRow  = props.get(propIdx);
        int propertyId = ((Number) pRow[0]).intValue();
        int ownerId    = ((Number) pRow[9]).intValue();
        String listed  = listedField.getText().trim();
        String closed  = closedField.getText().trim();
        String status  = (String) statusBox.getSelectedItem();

        String outcome = QueryRunner.closeDeal(
            agentId, ownerId, propertyId,
            clientName, clientPhone, clientEmail,
            amount, listed, closed, status
        );

        if (outcome.equals("success")) {
            JOptionPane.showMessageDialog(this,
                "Deal closed. Client added. Property marked as " + status + ".",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            loadProperties();
        } else {
            JOptionPane.showMessageDialog(this,
                "Error: " + outcome, "Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.LABEL);
        l.setForeground(Theme.TEXT_MID);
        return l;
    }

    private JLabel sectionHeader(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(Theme.PRIMARY);
        return l;
    }

    private JSeparator separator() {
        JSeparator s = new JSeparator();
        s.setForeground(Theme.BORDER);
        return s;
    }
}