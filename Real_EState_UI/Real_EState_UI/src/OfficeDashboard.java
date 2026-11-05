import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;

public class OfficeDashboard extends JFrame {

    private final ResultTable tablePanel  = new ResultTable();
    private final JLabel      titleLabel  = new JLabel("Sales Report");
    private final JTextField  filterField = new JTextField();
    private final JTextField  fromField   = new JTextField("2010-01-01");
    private final JTextField  toField     = new JTextField("2026-12-31");
    private final JPanel      datePanel   = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private String currentReport = "sales";

    public OfficeDashboard() {
        setTitle("Real Estate Office - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 700);
        setLocationRelativeTo(null);
        setBackground(Theme.BG);
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildMain(),    BorderLayout.CENTER);

        loadSalesReport();
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.HEADER_BG);
        p.setBorder(Theme.emptyBorder(14, 20));

        JLabel title = new JLabel("Real Estate Office");
        title.setFont(Theme.HEADING);
        title.setForeground(Theme.HEADER_TEXT);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setBackground(Theme.HEADER_BG);

        JLabel connDot = new JLabel("\u25CF Connected");
        connDot.setFont(Theme.SMALL);
        connDot.setForeground(new Color(0x86EFAC));

        JButton backBtn = new JButton("Change Role");
        backBtn.setFont(Theme.SMALL);
        backBtn.setForeground(new Color(0xBFD7F5));
        backBtn.setBackground(Theme.HEADER_BG);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> { new RoleSelector().setVisible(true); dispose(); });

        right.add(connDot);
        right.add(backBtn);
        p.add(title, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Theme.SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER),
            BorderFactory.createEmptyBorder(16, 0, 16, 0)
        ));
        p.setPreferredSize(new Dimension(200, 0));

        p.add(sectionLabel("REPORTS"));
        p.add(navButton("Sales Report",    this::loadSalesReport));
        p.add(navButton("Rental Report",   this::loadRentalReport));

        p.add(sectionLabel("DATA"));
        p.add(navButton("Properties",      this::loadPropertiesOverview));
        p.add(navButton("Agent Workload",  this::loadAgentWorkload));

        p.add(Box.createVerticalGlue());
        p.add(sectionLabel("INFO"));
        p.add(navButton("Summary Stats",   this::loadStats));
        return p;
    }

    // ── Main content ──────────────────────────────────────────────────────────
    private JPanel buildMain() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG);

        // Title bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Theme.BG);
        topBar.setBorder(Theme.emptyBorder(14, 20));
        titleLabel.setFont(Theme.HEADING);
        titleLabel.setForeground(Theme.TEXT);

        // Export button
        JButton exportBtn = new JButton("Export CSV") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()  ? Theme.SUCCESS.darker()  :
                            getModel().isRollover() ? Theme.SUCCESS.brighter() : Theme.SUCCESS);
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
        exportBtn.setFont(Theme.LABEL);
        exportBtn.setContentAreaFilled(false);
        exportBtn.setBorderPainted(false);
        exportBtn.setFocusPainted(false);
        exportBtn.setOpaque(false);
        exportBtn.setPreferredSize(new Dimension(110, 32));
        exportBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exportBtn.addActionListener(e -> tablePanel.exportToCSV(currentReport));

        topBar.add(titleLabel, BorderLayout.WEST);
        topBar.add(exportBtn,  BorderLayout.EAST);

        // Filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBar.setBackground(Theme.BG);
        filterBar.setBorder(Theme.emptyBorder(4, 20));

        JLabel filterLbl = new JLabel("Filter by agent:");
        filterLbl.setFont(Theme.LABEL);
        filterLbl.setForeground(Theme.TEXT_MID);

        filterField.setPreferredSize(new Dimension(200, 30));
        filterField.setFont(Theme.BODY);
        filterField.setBackground(Theme.SURFACE);
        filterField.setForeground(Theme.TEXT);
        filterField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
            void applyFilter() { tablePanel.filterByColumn(filterField.getText(), 0); }
        });

        JButton clearBtn = new JButton("Clear");
        clearBtn.setFont(Theme.SMALL);
        clearBtn.setForeground(Theme.TEXT_MID);
        clearBtn.setBorderPainted(false);
        clearBtn.setContentAreaFilled(false);
        clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> { filterField.setText(""); tablePanel.filterByColumn("", 0); });

        filterBar.add(filterLbl);
        filterBar.add(filterField);
        filterBar.add(clearBtn);

        // Date range bar (only shown for sales report)
        datePanel.setBackground(Theme.BG);
        datePanel.setBorder(Theme.emptyBorder(0, 20));

        JLabel fromLbl = new JLabel("From:");
        fromLbl.setFont(Theme.LABEL);
        fromLbl.setForeground(Theme.TEXT_MID);

        fromField.setPreferredSize(new Dimension(110, 30));
        fromField.setFont(Theme.BODY);
        fromField.setBackground(Theme.SURFACE);
        fromField.setForeground(Theme.TEXT);
        fromField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        JLabel toLbl = new JLabel("  To:");
        toLbl.setFont(Theme.LABEL);
        toLbl.setForeground(Theme.TEXT_MID);

        toField.setPreferredSize(new Dimension(110, 30));
        toField.setFont(Theme.BODY);
        toField.setBackground(Theme.SURFACE);
        toField.setForeground(Theme.TEXT);
        toField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        JButton applyDateBtn = new JButton("Apply");
        applyDateBtn.setFont(Theme.SMALL);
        applyDateBtn.setForeground(Theme.PRIMARY);
        applyDateBtn.setBorderPainted(false);
        applyDateBtn.setContentAreaFilled(false);
        applyDateBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        applyDateBtn.addActionListener(e -> applyDateFilter());

        JButton resetDateBtn = new JButton("Reset");
        resetDateBtn.setFont(Theme.SMALL);
        resetDateBtn.setForeground(Theme.TEXT_MID);
        resetDateBtn.setBorderPainted(false);
        resetDateBtn.setContentAreaFilled(false);
        resetDateBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resetDateBtn.addActionListener(e -> {
            fromField.setText("2010-01-01");
            toField.setText("2026-12-31");
            loadSalesReport();
        });

        datePanel.add(fromLbl);
        datePanel.add(fromField);
        datePanel.add(toLbl);
        datePanel.add(toField);
        datePanel.add(Box.createHorizontalStrut(8));
        datePanel.add(applyDateBtn);
        datePanel.add(resetDateBtn);

        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.setBackground(Theme.BG);
        north.add(topBar);
        north.add(filterBar);
        north.add(datePanel);

        // Table
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Theme.SURFACE);
        tableWrapper.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Theme.BORDER));
        tableWrapper.add(tablePanel, BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Theme.BG);
        content.setBorder(Theme.emptyBorder(0, 16));
        content.add(tableWrapper, BorderLayout.CENTER);

        p.add(north,   BorderLayout.NORTH);
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    // ── Data loaders ──────────────────────────────────────────────────────────
    private void loadSalesReport() {
        currentReport = "sales_report";
        filterField.setText("");
        datePanel.setVisible(true);
        titleLabel.setText("Sales Report - click View for property details");
        SwingWorker<Object[], Void> w = new SwingWorker<>() {
            @Override protected Object[] doInBackground() { return QueryRunner.salesReport(); }
            @Override protected void done() {
                try { tablePanel.loadData(get(), "Sales Report", true); }
                catch (Exception e) { tablePanel.setStatus("Error: " + e.getMessage(), false); }
            }
        };
        w.execute();
    }

    private void applyDateFilter() {
        String from = fromField.getText().trim();
        String to   = toField.getText().trim();
        if (from.isEmpty() || to.isEmpty()) return;
        titleLabel.setText("Sales Report (" + from + " to " + to + ")");
        SwingWorker<Object[], Void> w = new SwingWorker<>() {
            @Override protected Object[] doInBackground() {
                return QueryRunner.salesReportByDateRange(from, to);
            }
            @Override protected void done() {
                try { tablePanel.loadData(get(), "Sales Report", true); }
                catch (Exception e) { tablePanel.setStatus("Error: " + e.getMessage(), false); }
            }
        };
        w.execute();
    }

    private void loadRentalReport() {
        currentReport = "rental_report";
        filterField.setText("");
        datePanel.setVisible(false);
        titleLabel.setText("Rental Report - click View for property details");
        SwingWorker<Object[], Void> w = new SwingWorker<>() {
            @Override protected Object[] doInBackground() { return QueryRunner.rentalReport(); }
            @Override protected void done() {
                try { tablePanel.loadData(get(), "Rental Report", true); }
                catch (Exception e) { tablePanel.setStatus("Error: " + e.getMessage(), false); }
            }
        };
        w.execute();
    }

    private void loadPropertiesOverview() {
        currentReport = "properties";
        filterField.setText("");
        datePanel.setVisible(false);
        titleLabel.setText("Properties Overview - click View for details");
        SwingWorker<Object[], Void> w = new SwingWorker<>() {
            @Override protected Object[] doInBackground() { return QueryRunner.propertiesOverview(); }
            @Override protected void done() {
                try { tablePanel.loadData(get(), "Properties", true); }
                catch (Exception e) { tablePanel.setStatus("Error: " + e.getMessage(), false); }
            }
        };
        w.execute();
    }

    private void loadAgentWorkload() {
        currentReport = "agent_workload";
        filterField.setText("");
        datePanel.setVisible(false);
        titleLabel.setText("Agent Workload - active properties per agent");
        SwingWorker<Object[], Void> w = new SwingWorker<>() {
            @Override protected Object[] doInBackground() { return QueryRunner.agentWorkload(); }
            @Override protected void done() {
                try { tablePanel.loadData(get(), "Agent Workload", false); }
                catch (Exception e) { tablePanel.setStatus("Error: " + e.getMessage(), false); }
            }
        };
        w.execute();
    }

    private void loadStats() {
        currentReport = "summary_stats";
        filterField.setText("");
        datePanel.setVisible(false);
        titleLabel.setText("Summary Stats");
        Object[] result = QueryRunner.dashboardStats();
        tablePanel.loadData(result, "Dashboard Stats");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel("  " + text);
        l.setFont(new Font("SansSerif", Font.BOLD, 9));
        l.setForeground(Theme.TEXT_LIGHT);
        l.setBorder(BorderFactory.createEmptyBorder(12, 0, 4, 0));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return l;
    }

    private JButton navButton(String label, Runnable action) {
        JButton btn = new JButton(label);
        btn.setFont(Theme.BODY);
        btn.setForeground(Theme.TEXT);
        btn.setBackground(Theme.SURFACE);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(Theme.PRIMARY_DIM);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(Theme.SURFACE);
            }
        });
        return btn;
    }
}