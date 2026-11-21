import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.net.URI;
import java.sql.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class BookApp extends JPanel {
    private static final String DEFAULT_URL = "jdbc:mariadb://192.168.100.174:3306/cs366-2267_slindejs08";
    private static final String PREF_NODE = "edu/uww/cs366/BookApp";
    private static final String URL_KEY = "lastJdbcUrl";
    private static final String MIN_RATING = "Minimum average rating";
    private static final String TITLE_ORDER = " ORDER BY b.title, b.book_id LIMIT 20";
    private static final String BOOKS = " FROM book b LEFT JOIN publisher p ON p.publisher_id = b.publisher_id ";
    private static final String BOOK_COLUMNS = "SELECT b.book_id, b.title, p.publisher_name, ";

    // A book can have several matching authors or tags. Read its ratings independently.
    private static final String BOOK_STATS = BOOK_COLUMNS
            + "(SELECT ROUND(AVG(r.rating), 2) FROM rating r WHERE r.book_id = b.book_id) AS average_rating, "
            + "(SELECT COUNT(*) FROM rating r WHERE r.book_id = b.book_id) AS rating_count" + BOOKS;
    private static final String BY_AUTHOR = "EXISTS (SELECT 1 FROM book_author ba "
            + "JOIN author a ON a.author_id = ba.author_id "
            + "WHERE ba.book_id = b.book_id AND a.author_name LIKE ?)";
    private static final String BY_GENRE = "EXISTS (SELECT 1 FROM book_tag bt "
            + "JOIN tag t ON t.tag_id = bt.tag_id "
            + "WHERE bt.book_id = b.book_id AND t.tag_name LIKE ?)";
    private static final String BY_RATING = BOOK_COLUMNS
            + "ROUND(s.mean_rating, 2) AS average_rating, s.rating_count" + BOOKS
            + "JOIN (SELECT book_id, AVG(rating) AS mean_rating, COUNT(*) AS rating_count "
            + "FROM rating GROUP BY book_id HAVING AVG(rating) >= ? "
            + "ORDER BY mean_rating DESC, rating_count DESC, book_id LIMIT 20) s ON s.book_id = b.book_id "
            + "ORDER BY s.mean_rating DESC, s.rating_count DESC, b.book_id";

    private final Connection db;
    final JComboBox<String> searchBy = new JComboBox<>(new String[]{"Title", "Author", "Genre (tag)", MIN_RATING});
    final JTextField search = new JTextField("Harry Potter", 20);
    final JTextField reader = new JTextField("1", 8);
    final JTextField book = new JTextField("2", 7);
    final JTextField score = new JTextField("4", 3);
    final JTextField averageBook = new JTextField("2", 7);
    final JTable results = new JTable();
    final JLabel status = new JLabel("Choose an action.");
    final JButton find = new JButton("Search");
    final JButton rate = new JButton("Save rating");
    final JButton add = new JButton("Add to list");
    final JButton remove = new JButton("Remove from list");
    final JButton view = new JButton("View list");
    final JButton average = new JButton("Show average");
    private final JButton[] actions = {find, rate, add, view, remove, average};

    BookApp(Connection db) {
        super(new BorderLayout(0, 8));
        this.db = db;
        setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        JLabel searchLabel = new JLabel("contains");
        controls.add(row(new JLabel("Search by"), searchBy, searchLabel, search, find));
        searchBy.addActionListener(e -> {
            searchLabel.setText(MIN_RATING.equals(searchBy.getSelectedItem()) ? "(1–5)" : "contains");
            search.setText("");
            search.requestFocusInWindow();
        });
        search.addActionListener(e -> find.doClick());
        controls.add(row(new JLabel("Reader ID"), reader, new JLabel("Book ID"), book,
                         new JLabel("Rating (1-5)"), score));
        controls.add(row(rate, add, view, remove));
        JPanel summary = row(new JLabel("Book ID"), averageBook, average);
        summary.setBorder(BorderFactory.createTitledBorder("Average rating for one book"));
        averageBook.addActionListener(e -> average.doClick());
        controls.add(summary);
        add(controls, BorderLayout.NORTH);
        results.setFillsViewportHeight(true);
        results.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(results);
        scroll.setColumnHeaderView(results.getTableHeader());
        add(scroll, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        for (JButton button : actions) {
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
                searchBooks();
            } else if (action == average) {
                runQuery(average, BOOK_STATS + "WHERE b.book_id = ?", number(averageBook));
            } else if (action == view) {
                runQuery(view, "CALL sp_get_reading_list(?)", number(reader));
            } else if (action == rate) {
                runQuery(rate, "CALL sp_save_rating(?,?,?)", number(reader), number(book), number(score));
            } else if (action == add) {
                runQuery(add, "CALL sp_add_to_reading_list(?,?,?)", number(reader), number(book),
                         java.sql.Date.valueOf(LocalDate.now()));
            } else if (action == remove) {
                runQuery(remove, "CALL sp_remove_from_reading_list(?,?)", number(reader), number(book));
            }
        } catch (NumberFormatException ex) {
            clearResults("Enter whole numbers for the IDs and reader rating.");
        }
    }

    private void searchBooks() {
        String term = search.getText().trim();
        String mode = (String) searchBy.getSelectedItem();
        if (MIN_RATING.equals(mode)) {
            try {
                BigDecimal minimum = new BigDecimal(term);
                if (minimum.compareTo(BigDecimal.ONE) >= 0 && minimum.compareTo(BigDecimal.valueOf(5)) <= 0) {
                    runQuery(find, BY_RATING, minimum);
                    return;
                }
            } catch (NumberFormatException ignored) {
                // Use the same hint for non-numeric and out-of-range entries.
            }
            clearResults("Enter a minimum average rating from 1 to 5, such as 4.5.");
            return;
        }
        String predicate = "b.title LIKE ?";
        if ("Author".equals(mode)) predicate = BY_AUTHOR;
        if ("Genre (tag)".equals(mode)) predicate = BY_GENRE;
        runQuery(find, BOOK_STATS + "WHERE " + predicate + TITLE_ORDER, "%" + term + "%");
    }

    private void clearResults(String message) {
        results.setModel(new DefaultTableModel());
        status.setText(message);
    }

    private void runQuery(JButton action, String sql, Object... values) {
        // Keep Swing responsive and allow only one operation at a time on this connection.
        for (JButton button : actions) button.setEnabled(false);
        status.setText(action.getText() + "…");
        new SwingWorker<DefaultTableModel, Void>() {
            protected DefaultTableModel doInBackground() throws SQLException {
                return query(sql, values);
            }

            protected void done() {
                try {
                    DefaultTableModel model = get();
                    results.setModel(model);
                    for (int i = 0; i < model.getColumnCount(); i++) {
                        int width = model.getColumnName(i).equals("title") ? 520 : 130;
                        results.getColumnModel().getColumn(i).setPreferredWidth(width);
                    }
                    status.setText(action == average && model.getRowCount() == 0 ? "Book ID not found."
                            : action.getText() + "  |  Rows returned: " + model.getRowCount());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    clearResults("Query interrupted.");
                } catch (ExecutionException ex) {
                    clearResults("Database message: " + ex.getCause().getMessage());
                } finally {
                    for (JButton button : actions) button.setEnabled(true);
                }
            }
        }.execute();
    }

    private DefaultTableModel query(String sql, Object... values) throws SQLException {
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
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
                    for (int i = 0; i < line.length; i++) {
                        Object value = rs.getObject(i + 1);
                        line[i] = value == null && model.getColumnName(i).equals("average_rating") ? "Unrated" : value;
                    }
                    model.addRow(line);
                }
                return model;
            }
        }
    }

    // Persist a plain endpoint and recognized non-secret options only. Other URL forms
    // can still connect for this session. Never write the original URL as a fallback.
    static String urlForPreferences(String url) {
        if (!url.startsWith("jdbc:")) return null;
        try {
            URI uri = URI.create(url.substring(5));
            if (!("mariadb".equals(uri.getScheme()) || "mysql".equals(uri.getScheme()))
                    || uri.getHost() == null || uri.getRawFragment() != null
                    || !uri.getRawPath().matches("/?[a-zA-Z0-9_$.-]*")) return null;
            String endpoint = "jdbc:" + uri.getScheme() + "://" + uri.getHost()
                    + (uri.getPort() < 0 ? "" : ":" + uri.getPort()) + uri.getRawPath();
            StringJoiner options = new StringJoiner("&");
            if (uri.getRawQuery() != null) {
                for (String option : uri.getRawQuery().split("&")) {
                    String name = option.split("=", 2)[0];
                    if (name.matches("(?i)user(name)?|password|passwd|pwd")) continue;
                    if (!option.matches("(?i)(connectTimeout|socketTimeout)=[0-9]+"
                            + "|(useSsl|requireSsl|verifyServerCertificate|allowPublicKeyRetrieval|tcpKeepAlive)=(true|false)"
                            + "|sslMode=(disable[d]?|trust|verify-ca|verify-full|required|preferred|verify_ca|verify_identity)")) {
                        return null;
                    }
                    options.add(option);
                }
            }
            return options.length() == 0 ? endpoint : endpoint + "?" + options;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static String lastUrl() {
        try {
            return Preferences.userRoot().node(PREF_NODE).get(URL_KEY, DEFAULT_URL);
        } catch (SecurityException ex) {
            return DEFAULT_URL;
        }
    }

    private static String rememberUrl(String url) {
        String safe = urlForPreferences(url);
        if (safe == null || safe.length() > Preferences.MAX_VALUE_LENGTH) {
            return "Connected. This URL format is used for this session only.";
        }
        try {
            Preferences prefs = Preferences.userRoot().node(PREF_NODE);
            prefs.put(URL_KEY, safe);
            prefs.flush();
            return "Choose an action.";
        } catch (BackingStoreException | SecurityException ex) {
            return "Connected. Could not save the URL preference.";
        }
    }

    public static void main(String[] args) {
        String url = JOptionPane.showInputDialog("JDBC URL (use your working Lab 2 host and port)", lastUrl());
        if (url == null) return;
        url = url.trim();
        String user = JOptionPane.showInputDialog("MariaDB username", "slindejs08");
        if (user == null) return;
        JPasswordField password = new JPasswordField();
        if (JOptionPane.showConfirmDialog(null, password, "MariaDB password",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        char[] entered = password.getPassword();
        password.setText("");
        try {
            Connection db = DriverManager.getConnection(url, user, new String(entered));
            String message = rememberUrl(url);
            SwingUtilities.invokeLater(() -> {
                JFrame window = new JFrame("Book Recommendation System");
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                BookApp app = new BookApp(db);
                app.status.setText(message);
                window.setContentPane(app);
                window.setSize(940, 540);
                window.setLocationRelativeTo(null);
                window.setVisible(true);
            });
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Connection failed", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(entered, '\0');
        }
    }
}
