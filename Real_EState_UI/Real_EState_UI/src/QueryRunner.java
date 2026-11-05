import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QueryRunner {

    private static Object[] run(String sql, Object... params) {
        List<Object[]> rows = new ArrayList<>();
        String[] columns = new String[0];
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int col = meta.getColumnCount();
            columns = new String[col];
            for (int i = 0; i < col; i++) columns[i] = meta.getColumnLabel(i + 1);
            while (rs.next()) {
                Object[] row = new Object[col];
                for (int i = 0; i < col; i++) row[i] = rs.getObject(i + 1);
                rows.add(row);
            }
        } catch (SQLException e) { System.err.println("Query error: " + e.getMessage()); }
        return new Object[]{ columns, rows };
    }

    // ── Agent login ───────────────────────────────────────────────────────────
    public static Object[] loginAgent(String agentId, String phone) {
        return run(
            "SELECT agent_id, name, status FROM agent " +
            "WHERE agent_id = ? AND phone = ? AND status = 'present'",
            Integer.parseInt(agentId), phone
        );
    }

    // ── Office: Sales report (all time) ──────────────────────────────────────
    public static Object[] salesReport() {
        return run(
            "SELECT a.name 'Agent', p.address_street 'Street', p.address_city 'City', " +
            "p.property_type 'Type', p.bedroom 'BHK', " +
            "t.amount 'Amount (Rs)', t.date_listed 'Listed', t.date_closed 'Closed', " +
            "c.name 'Client', t.property_id 'Prop ID' " +
            "FROM transaction t " +
            "JOIN agent a    ON t.agent_id    = a.agent_id " +
            "JOIN property p ON t.property_id = p.property_id " +
            "JOIN client c   ON t.client_id   = c.client_id " +
            "ORDER BY t.date_closed DESC"
        );
    }

    // ── Office: Sales report filtered by date range ───────────────────────────
    public static Object[] salesReportByDateRange(String fromDate, String toDate) {
        return run(
            "SELECT a.name 'Agent', p.address_street 'Street', p.address_city 'City', " +
            "p.property_type 'Type', p.bedroom 'BHK', " +
            "t.amount 'Amount (Rs)', t.date_listed 'Listed', t.date_closed 'Closed', " +
            "c.name 'Client', t.property_id 'Prop ID' " +
            "FROM transaction t " +
            "JOIN agent a    ON t.agent_id    = a.agent_id " +
            "JOIN property p ON t.property_id = p.property_id " +
            "JOIN client c   ON t.client_id   = c.client_id " +
            "WHERE t.date_closed BETWEEN ? AND ? " +
            "ORDER BY t.date_closed DESC",
            fromDate, toDate
        );
    }

    // ── Office: Rental report ─────────────────────────────────────────────────
    public static Object[] rentalReport() {
        return run(
            "SELECT a.name 'Agent', p.address_street 'Street', p.address_city 'City', " +
            "p.bedroom 'BHK', p.property_type 'Type', " +
            "p.monthly_rent 'Rent/mo (Rs)', p.status 'Status', " +
            "o.name 'Owner', p.property_id 'Prop ID' " +
            "FROM property p " +
            "JOIN agent a ON p.agent_id = a.agent_id " +
            "JOIN owner o ON p.owner_id = o.owner_id " +
            "WHERE p.status = 'rented' AND p.monthly_rent IS NOT NULL " +
            "ORDER BY p.monthly_rent DESC"
        );
    }

    // ── Office: Summary stats ─────────────────────────────────────────────────
    public static Object[] dashboardStats() {
        return run(
            "SELECT " +
            "(SELECT COUNT(*) FROM agent WHERE status = 'present') 'Active Agents', " +
            "(SELECT COUNT(*) FROM property WHERE status = 'available') 'Available', " +
            "(SELECT COUNT(*) FROM property WHERE status = 'rented') 'Rented', " +
            "(SELECT COUNT(*) FROM property WHERE status = 'sold') 'Sold', " +
            "(SELECT COUNT(*) FROM transaction) 'Total Transactions', " +
            "(SELECT COALESCE(SUM(amount),0) FROM transaction) 'Total Revenue (Rs)'"
        );
    }

    // ── Office: Properties overview ───────────────────────────────────────────
    public static Object[] propertiesOverview() {
        return run(
            "SELECT p.property_id 'ID', p.address_street 'Street', " +
            "p.address_city 'City', p.bedroom 'BHK', " +
            "p.property_type 'Type', p.status 'Status', " +
            "p.monthly_rent 'Rent', p.sales_price 'Sale Price', " +
            "o.name 'Owner', a.name 'Agent', p.property_id 'Prop ID' " +
            "FROM property p " +
            "JOIN owner o ON p.owner_id = o.owner_id " +
            "JOIN agent a ON p.agent_id = a.agent_id " +
            "WHERE p.status != 'deleted' ORDER BY p.status, p.property_id"
        );
    }

    // ── Office: Agent workload ────────────────────────────────────────────────
    public static Object[] agentWorkload() {
        return run(
            "SELECT a.agent_id 'ID', a.name 'Agent', a.status 'Status', " +
            "COUNT(CASE WHEN p.status = 'available' THEN 1 END) 'Available', " +
            "COUNT(CASE WHEN p.status = 'rented'    THEN 1 END) 'Rented', " +
            "COUNT(CASE WHEN p.status = 'sold'      THEN 1 END) 'Sold', " +
            "COUNT(CASE WHEN p.status NOT IN ('deleted','sold') THEN 1 END) 'Active Load' " +
            "FROM agent a " +
            "LEFT JOIN property p ON a.agent_id = p.agent_id " +
            "GROUP BY a.agent_id, a.name, a.status " +
            "ORDER BY COUNT(CASE WHEN p.status NOT IN ('deleted','sold') THEN 1 END) DESC"
        );
    }

    // ── Property detail by ID ─────────────────────────────────────────────────
    public static Object[] propertyDetail(int propertyId) {
        return run(
            "SELECT p.property_id 'ID', p.address_street 'Street', " +
            "p.address_city 'City', p.address_state 'State', " +
            "p.address_postal_code 'Pincode', p.size_sqft 'Size (sqft)', " +
            "p.construction_date 'Built', p.bedroom 'BHK', " +
            "p.property_type 'Type', p.status 'Status', " +
            "p.monthly_rent 'Rent', p.sales_price 'Sale Price', " +
            "o.name 'Owner', o.phone 'Owner Phone', " +
            "a.name 'Agent', a.phone 'Agent Phone' " +
            "FROM property p " +
            "JOIN owner o ON p.owner_id = o.owner_id " +
            "JOIN agent a ON p.agent_id = a.agent_id " +
            "WHERE p.property_id = ?",
            propertyId
        );
    }

    // ── Agent: Properties assigned to this agent ──────────────────────────────
    public static Object[] agentProperties(int agentId) {
        return run(
            "SELECT p.property_id 'ID', p.address_street 'Street', " +
            "p.address_city 'City', p.bedroom 'BHK', " +
            "p.property_type 'Type', p.status 'Status', " +
            "p.monthly_rent 'Rent', p.sales_price 'Sale Price', " +
            "o.name 'Owner', o.owner_id 'Owner ID' " +
            "FROM property p JOIN owner o ON p.owner_id = o.owner_id " +
            "WHERE p.agent_id = ? AND p.status NOT IN ('sold','deleted') " +
            "ORDER BY p.property_id",
            agentId
        );
    }

    // ── Agent: Close a deal ───────────────────────────────────────────────────
    public static String closeDeal(int agentId, int ownerId, int propertyId,
            String clientName, String clientPhone, String clientEmail,
            double amount, String dateListed, String dateClosed, String newStatus) {
        Connection c = DBConnection.getConnection();
        try {
            c.setAutoCommit(false);

            PreparedStatement insertClient = c.prepareStatement(
                "INSERT INTO client (name, phone, email) VALUES (?,?,?)",
                Statement.RETURN_GENERATED_KEYS
            );
            insertClient.setString(1, clientName);
            insertClient.setString(2, clientPhone);
            insertClient.setString(3, clientEmail);
            insertClient.executeUpdate();
            ResultSet keys = insertClient.getGeneratedKeys();
            if (!keys.next()) { c.rollback(); c.setAutoCommit(true); return "Failed to insert client."; }
            int clientId = keys.getInt(1);
            insertClient.close();

            PreparedStatement insertTxn = c.prepareStatement(
                "INSERT INTO transaction (agent_id, client_id, owner_id, property_id, " +
                "amount, date_listed, date_closed) VALUES (?,?,?,?,?,?,?)"
            );
            insertTxn.setInt(1, agentId);
            insertTxn.setInt(2, clientId);
            insertTxn.setInt(3, ownerId);
            insertTxn.setInt(4, propertyId);
            insertTxn.setDouble(5, amount);
            insertTxn.setString(6, dateListed);
            insertTxn.setString(7, dateClosed);
            insertTxn.executeUpdate();
            insertTxn.close();

            PreparedStatement updateProp = c.prepareStatement(
                "UPDATE property SET status = ? WHERE property_id = ?"
            );
            updateProp.setString(1, newStatus);
            updateProp.setInt(2, propertyId);
            updateProp.executeUpdate();
            updateProp.close();

            c.commit();
            c.setAutoCommit(true);
            return "success";
        } catch (SQLException e) {
            try { c.rollback(); c.setAutoCommit(true); } catch (SQLException ignored) {}
            return e.getMessage();
        }
    }
}