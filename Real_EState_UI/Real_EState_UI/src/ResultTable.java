import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.util.List;

public class ResultTable extends JPanel {

    private final DefaultTableModel model;
    private final JTable table;
    private final JLabel statusLabel;
    private boolean showDetailsButton = false;
    private TableRowSorter<DefaultTableModel> sorter;

    public ResultTable() {
        setLayout(new BorderLayout());
        setBackground(Theme.SURFACE);

        model = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) {
                return showDetailsButton && c == getColumnCount() - 1;
            }
        };

        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        styleTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.SURFACE);

        statusLabel = new JLabel("  Ready");
        statusLabel.setFont(Theme.SMALL);
        statusLabel.setForeground(Theme.TEXT_LIGHT);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        statusLabel.setBackground(Theme.BG);
        statusLabel.setOpaque(true);

        add(scroll, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    public void loadData(Object[] result, String label) {
        loadData(result, label, false);
    }

    @SuppressWarnings("unchecked")
    public void loadData(Object[] result, String label, boolean withDetails) {
        String[] columns = (String[]) result[0];
        List<Object[]> rows = (List<Object[]>) result[1];
        this.showDetailsButton = withDetails;

        model.setRowCount(0);
        model.setColumnCount(0);

        for (String col : columns) model.addColumn(col);
        if (withDetails) model.addColumn("Details");

        for (Object[] row : rows) {
            if (withDetails) {
                Object[] extended = new Object[row.length + 1];
                System.arraycopy(row, 0, extended, 0, row.length);
                extended[row.length] = "View";
                model.addRow(extended);
            } else {
                model.addRow(row);
            }
        }

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        for (int i = 0; i < table.getColumnCount(); i++) {
            int width = 60;
            for (int r = 0; r < table.getRowCount(); r++) {
                TableCellRenderer renderer = table.getCellRenderer(r, i);
                Component comp = table.prepareRenderer(renderer, r, i);
                width = Math.max(comp.getPreferredSize().width + 20, width);
            }
            table.getColumnModel().getColumn(i).setPreferredWidth(Math.min(width, 200));
        }

        if (withDetails && table.getColumnCount() >= 2) {
            int propIdCol  = table.getColumnCount() - 2;
            int detailsCol = table.getColumnCount() - 1;

            table.getColumnModel().getColumn(propIdCol).setMinWidth(0);
            table.getColumnModel().getColumn(propIdCol).setMaxWidth(0);
            table.getColumnModel().getColumn(propIdCol).setWidth(0);

            table.getColumnModel().getColumn(detailsCol).setPreferredWidth(80);
            table.getColumnModel().getColumn(detailsCol).setMinWidth(80);
            table.getColumnModel().getColumn(detailsCol).setMaxWidth(80);

            table.getColumnModel().getColumn(detailsCol).setCellRenderer(new ButtonRenderer());
            table.getColumnModel().getColumn(detailsCol).setCellEditor(
                new ButtonEditor(table, propIdCol));
        }

        int count = rows.size();
        statusLabel.setText("  " + label + "  -  " + count + " row" + (count != 1 ? "s" : ""));
        statusLabel.setForeground(count > 0 ? Theme.SUCCESS : Theme.DANGER);
    }

    public void filterByColumn(String text, int columnIndex) {
        if (text.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text.trim(), columnIndex));
        }
    }

    // ── Export visible rows to CSV ────────────────────────────────────────────
    public void exportToCSV(String suggestedName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(suggestedName + ".csv"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            // Headers — skip hidden Prop ID col and Details col
            StringBuilder header = new StringBuilder();
            for (int i = 0; i < table.getColumnCount(); i++) {
                String colName = table.getColumnName(i);
                if (colName.equals("Prop ID") || colName.equals("Details")) continue;
                if (header.length() > 0) header.append(",");
                header.append("\"").append(colName).append("\"");
            }
            pw.println(header);

            // Visible rows only (respects current filter)
            for (int r = 0; r < table.getRowCount(); r++) {
                StringBuilder row = new StringBuilder();
                for (int c = 0; c < table.getColumnCount(); c++) {
                    String colName = table.getColumnName(c);
                    if (colName.equals("Prop ID") || colName.equals("Details")) continue;
                    Object val = table.getValueAt(r, c);
                    if (row.length() > 0) row.append(",");
                    row.append("\"").append(val == null ? "" : val.toString()).append("\"");
                }
                pw.println(row);
            }

            JOptionPane.showMessageDialog(this,
                "Exported " + table.getRowCount() + " rows to " + file.getName(),
                "Export successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setStatus(String msg, boolean ok) {
        statusLabel.setText("  " + msg);
        statusLabel.setForeground(ok ? Theme.SUCCESS : Theme.DANGER);
    }

    private void styleTable() {
        table.setFont(Theme.BODY);
        table.setRowHeight(30);
        table.setGridColor(Theme.BORDER);
        table.setBackground(Theme.SURFACE);
        table.setForeground(Theme.TEXT);
        table.setSelectionBackground(Theme.PRIMARY_DIM);
        table.setSelectionForeground(Theme.TEXT);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setFont(Theme.LABEL);
        header.setBackground(Theme.HEADER_BG);
        header.setForeground(Theme.HEADER_TEXT);
        header.setBorder(BorderFactory.createEmptyBorder());
        header.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) header.getDefaultRenderer())
            .setHorizontalAlignment(SwingConstants.LEFT);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                setBackground(sel ? Theme.PRIMARY_DIM :
                    row % 2 == 0 ? Theme.SURFACE : Theme.BG);
                setForeground(Theme.TEXT);
                if (val != null) {
                    String s = val.toString().toLowerCase();
                    if      (s.equals("available")) setForeground(Theme.SUCCESS);
                    else if (s.equals("rented"))    setForeground(Theme.AMBER);
                    else if (s.equals("sold"))      setForeground(Theme.DANGER);
                    else if (s.equals("left") || s.equals("deleted"))
                                                    setForeground(Theme.TEXT_LIGHT);
                }
                return this;
            }
        });
    }

    static class ButtonRenderer extends JButton implements TableCellRenderer {
        ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("SansSerif", Font.BOLD, 11));
            setForeground(Theme.PRIMARY);
            setBackground(Theme.PRIMARY_DIM);
            setBorderPainted(false);
            setFocusPainted(false);
            setPreferredSize(new Dimension(80, 26));
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText("View");
            return this;
        }
    }

    static class ButtonEditor extends DefaultCellEditor {
        private final JTable table;
        private final int propIdCol;
        private final JButton button;

        ButtonEditor(JTable table, int propIdCol) {
            super(new JCheckBox());
            this.table     = table;
            this.propIdCol = propIdCol;

            button = new JButton("View");
            button.setFont(new Font("SansSerif", Font.BOLD, 11));
            button.setForeground(Theme.PRIMARY);
            button.setBackground(Theme.PRIMARY_DIM);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setOpaque(true);

            button.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) return;
                int modelRow = table.convertRowIndexToModel(row);
                Object propIdObj = table.getModel().getValueAt(modelRow, propIdCol);
                if (propIdObj == null) return;
                int propertyId = ((Number) propIdObj).intValue();
                showPropertyDetail(propertyId);
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected, int row, int column) {
            return button;
        }

        @Override public Object getCellEditorValue() { return "View"; }

        @SuppressWarnings("unchecked")
        private void showPropertyDetail(int propertyId) {
            Object[] result = QueryRunner.propertyDetail(propertyId);
            String[] columns = (String[]) result[0];
            List<Object[]> rows = (List<Object[]>) result[1];

            if (rows.isEmpty()) {
                JOptionPane.showMessageDialog(table,
                    "Property not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Object[] row = rows.get(0);
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(Theme.BG);
            panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 8, 4, 8);
            gbc.anchor = GridBagConstraints.WEST;

            for (int i = 0; i < columns.length; i++) {
                gbc.gridx = 0; gbc.gridy = i;
                JLabel key = new JLabel(columns[i] + ":");
                key.setFont(new Font("SansSerif", Font.BOLD, 12));
                key.setForeground(Theme.TEXT_MID);
                panel.add(key, gbc);

                gbc.gridx = 1;
                String val = row[i] == null ? "-" : row[i].toString();
                JLabel value = new JLabel(val);
                value.setFont(new Font("SansSerif", Font.PLAIN, 12));
                value.setForeground(Theme.TEXT);
                panel.add(value, gbc);
            }

            JScrollPane scroll = new JScrollPane(panel);
            scroll.setPreferredSize(new Dimension(420, 380));
            scroll.setBorder(BorderFactory.createEmptyBorder());

            JOptionPane.showMessageDialog(table, scroll,
                "Property Details - ID " + propertyId,
                JOptionPane.PLAIN_MESSAGE);
        }
    }
}