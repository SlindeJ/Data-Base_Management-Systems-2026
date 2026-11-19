import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class BookApp extends JPanel {
    private final Connection db;
    final JTextField search = new JTextField("Harry Potter", 20);
    final JTextField reader = new JTextField("1", 8);
    final JTextField book = new JTextField("2", 7);
    final JTextField score = new JTextField("4", 3);
    final JTable results = new JTable();
    final JLabel status = new JLabel("Choose an action.");
    final JButton find = new JButton("Search");
    final JButton rate = new JButton("Save rating");
    final JButton add = new JButton("Add to list");
    final JButton remove = new JButton("Remove from list");
    final JButton view = new JButton("View list");

    BookApp(Connection db) {
        super(new BorderLayout(0, 8));
        this.db = db;
        setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));
        JPanel controls = new JPanel(new GridLayout(3, 1));
        controls.add(row(new JLabel("Title contains"), search, find));
        controls.add(row(new JLabel("Reader ID"), reader, new JLabel("Book ID"), book,
                         new JLabel("Rating (1-5)"), score));
        controls.add(row(rate, add, view, remove));
        add(controls, BorderLayout.NORTH);
        results.setFillsViewportHeight(true);
        results.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(results);
        scroll.setColumnHeaderView(results.getTableHeader());
        add(scroll, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        for (JButton button : new JButton[]{find, rate, add, view, remove}) {
            button.addActionListener(e -> handle(button));
        }
    }

    private JPanel row(Component... items) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 2));
        for (Component item : items) panel.add(item);
        return panel;
    }

    private int number(JTextField field) {
        return Integer.parseInt(field.getText().trim());
    }

    private void handle(JButton action) {
        try {
            if (action == find) {
                call("{call sp_search_books(?)}", search.getText().trim());
            } else if (action == view) {
                call("{call sp_get_reading_list(?)}", number(reader));
            } else if (action == rate) {
                call("{call sp_save_rating(?,?,?)}", number(reader), number(book), number(score));
            } else if (action == add) {
                call("{call sp_add_to_reading_list(?,?,?)}", number(reader), number(book),
                     java.sql.Date.valueOf(LocalDate.now()));
            } else {
                call("{call sp_remove_from_reading_list(?,?)}", number(reader), number(book));
            }
            status.setText(action.getText() + "  |  Rows returned: " + results.getRowCount());
        } catch (NumberFormatException ex) {
            clearResults("Enter whole numbers for the IDs and rating.");
        } catch (SQLException ex) {
            clearResults("Database message: " + ex.getMessage());
        }
    }

    private void clearResults(String message) {
        results.setModel(new DefaultTableModel());
        status.setText(message);
    }

    private void call(String sql, Object... values) throws SQLException {
        try (CallableStatement stmt = db.prepareCall(sql)) {
            for (int i = 0; i < values.length; i++) stmt.setObject(i + 1, values[i]);
            boolean hasRows = stmt.execute();
            while (!hasRows && stmt.getUpdateCount() != -1) hasRows = stmt.getMoreResults();
            if (!hasRows) throw new SQLException("No result set returned.");
            try (ResultSet rs = stmt.getResultSet()) {
                ResultSetMetaData meta = rs.getMetaData();
                DefaultTableModel model = new DefaultTableModel() {
                    public boolean isCellEditable(int row, int col) { return false; }
                };
                for (int i = 1; i <= meta.getColumnCount(); i++) model.addColumn(meta.getColumnLabel(i));
                while (rs.next()) {
                    Object[] line = new Object[meta.getColumnCount()];
                    for (int i = 0; i < line.length; i++) line[i] = rs.getObject(i + 1);
                    model.addRow(line);
                }
                results.setModel(model);
                for (int i = 0; i < model.getColumnCount(); i++) {
                    int width = model.getColumnName(i).equals("title") ? 520 : 130;
                    results.getColumnModel().getColumn(i).setPreferredWidth(width);
                }
            }
        }
    }
// for Jacob use jdbc:mariadb://127.0.0.1:3307/cs366-2267_slindejs08
    public static void main(String[] args) {
        String url = JOptionPane.showInputDialog("JDBC URL (use your working Lab 2 host and port)",
                "jdbc:mariadb://192.168.100.174:3306/cs366-2267_slindejs08");
        if (url == null) return;
        String user = JOptionPane.showInputDialog("MariaDB username", "slindejs08");
        if (user == null) return;
        JPasswordField password = new JPasswordField();
        if (JOptionPane.showConfirmDialog(null, password, "MariaDB password",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            Connection db = DriverManager.getConnection(url, user, new String(password.getPassword()));
            SwingUtilities.invokeLater(() -> {
                JFrame window = new JFrame("Book Recommendation System");
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                window.setContentPane(new BookApp(db));
                window.setSize(860, 430);
                window.setLocationRelativeTo(null);
                window.setVisible(true);
            });
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Connection failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
