package Project3;

/*
Name: <your name goes here>
Course: CNT 4714 Spring 2026
Assignment title: Project 3 – A Two-tier Client-Server Application
Date: March 15, 2026
Class: <name of class goes here>
*/

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.*;

public class SQLClientApp extends JFrame {

    private final JComboBox<String> dbPropsCombo = new JComboBox<>();
    private final JComboBox<String> userPropsCombo = new JComboBox<>();
    private final JTextField usernameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);

    private final JLabel connectionStatus = new JLabel("NO CONNECTION ESTABLISHED");
    private final JTextArea sqlArea = new JTextArea(6, 60);

    private JTable resultTable = new JTable();
    private final JScrollPane resultScroll = new JScrollPane(resultTable);

    private Connection userConn;
    private String currentLoginUser; // e.g. root@localhost
    private LogUtil logUtil;

    public SQLClientApp() {
        super("SQL CLIENT APPLICATION - CNT 4714 - PROJECT 3");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(950, 700);
        setLocationRelativeTo(null);

        try {
            logUtil = new LogUtil("config/app.properties");
        } catch (Exception e) {
            logUtil = null;
            JOptionPane.showMessageDialog(this,
                "Warning: Logging disabled (could not load config/app.properties)\n" + e.getMessage(),
                "Logging Disabled", JOptionPane.WARNING_MESSAGE);
        }

        buildUI();
        loadDropdowns();
        setVisible(true);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel connPanel = new JPanel(new GridBagLayout());
        connPanel.setBorder(new TitledBorder("Connection Details"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,4,4,4);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx=0; gc.gridy=0; connPanel.add(new JLabel("DB URL Properties"), gc);
        gc.gridx=1; connPanel.add(dbPropsCombo, gc);

        gc.gridx=0; gc.gridy=1; connPanel.add(new JLabel("User Properties"), gc);
        gc.gridx=1; connPanel.add(userPropsCombo, gc);

        gc.gridx=0; gc.gridy=2; connPanel.add(new JLabel("Username"), gc);
        gc.gridx=1; connPanel.add(usernameField, gc);

        gc.gridx=0; gc.gridy=3; connPanel.add(new JLabel("Password"), gc);
        gc.gridx=1; connPanel.add(passwordField, gc);

        JButton connectBtn = new JButton("Connect to Database");
        JButton disconnectBtn = new JButton("Disconnect From Database");

        gc.gridx=2; gc.gridy=0; gc.gridheight=2; gc.fill=GridBagConstraints.HORIZONTAL;
        connPanel.add(connectBtn, gc);
        gc.gridy=2; connPanel.add(disconnectBtn, gc);
        gc.gridheight=1; gc.fill=GridBagConstraints.NONE;

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(new TitledBorder("Connection Status"));
        connectionStatus.setOpaque(true);
        connectionStatus.setBackground(new Color(200, 60, 60));
        connectionStatus.setForeground(Color.WHITE);
        connectionStatus.setHorizontalAlignment(SwingConstants.CENTER);
        connectionStatus.setFont(connectionStatus.getFont().deriveFont(Font.BOLD, 14f));
        statusPanel.add(connectionStatus, BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(connPanel, BorderLayout.CENTER);
        top.add(statusPanel, BorderLayout.EAST);

        JPanel sqlPanel = new JPanel(new BorderLayout(6,6));
        sqlPanel.setBorder(new TitledBorder("SQL Command Input Window"));
        sqlArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        sqlPanel.add(new JScrollPane(sqlArea), BorderLayout.CENTER);

        JButton execBtn = new JButton("Execute SQL Command");
        JButton clearSqlBtn = new JButton("Clear SQL Command");
        JPanel sqlBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        sqlBtns.add(execBtn);
        sqlBtns.add(clearSqlBtn);
        sqlPanel.add(sqlBtns, BorderLayout.SOUTH);

        JPanel resultsPanel = new JPanel(new BorderLayout(6,6));
        resultsPanel.setBorder(new TitledBorder("SQL Execution Result Window"));
        resultsPanel.add(resultScroll, BorderLayout.CENTER);

        JButton clearResultsBtn = new JButton("Clear Result Window");
        JButton closeBtn = new JButton("Close Application");
        JPanel bottomBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        bottomBtns.add(clearResultsBtn);
        bottomBtns.add(closeBtn);
        resultsPanel.add(bottomBtns, BorderLayout.SOUTH);

        root.add(top, BorderLayout.NORTH);
        root.add(sqlPanel, BorderLayout.CENTER);
        root.add(resultsPanel, BorderLayout.SOUTH);
        setContentPane(root);

        connectBtn.addActionListener(e -> connect());
        disconnectBtn.addActionListener(e -> disconnect());
        execBtn.addActionListener(e -> executeSql());
        clearSqlBtn.addActionListener(e -> sqlArea.setText(""));
        clearResultsBtn.addActionListener(e -> setEmptyResults());
        closeBtn.addActionListener(e -> { disconnect(); dispose(); System.exit(0); });
    }

    private void loadDropdowns() {
        File cfg = new File("config");
        File[] files = cfg.listFiles((d, name) -> name.endsWith(".properties"));
        if (files == null) return;

        java.util.List<String> dbFiles = new ArrayList<>();
        java.util.List<String> userFiles = new ArrayList<>();

        for (File f : files) {
            if (f.getName().equalsIgnoreCase("app.properties")) continue;
            try {
                Properties p = new Properties();
                try (FileInputStream fis = new FileInputStream(f)) { p.load(fis); }
                if (p.getProperty("url") != null) dbFiles.add(f.getName());
                if (p.getProperty("username") != null) userFiles.add(f.getName());
            } catch (Exception ignored) {}
        }

        Collections.sort(dbFiles);
        Collections.sort(userFiles);

        dbPropsCombo.removeAllItems();
        for (String s : dbFiles) dbPropsCombo.addItem(s);

        userPropsCombo.removeAllItems();
        for (String s : userFiles) userPropsCombo.addItem(s);

        dbPropsCombo.setSelectedItem("project3.properties");
        userPropsCombo.setSelectedItem("root.properties");
    }

    private Properties loadProps(String filename) throws Exception {
        Properties p = new Properties();
        try (FileInputStream fis = new FileInputStream(new File("config", filename))) {
            p.load(fis);
        }
        return p;
    }

    private void setStatus(boolean connected, String text) {
        connectionStatus.setText(text);
        if (connected) {
            connectionStatus.setBackground(new Color(40, 140, 60));
        } else {
            connectionStatus.setBackground(new Color(200, 60, 60));
        }
    }

    private void connect() {
        try {
            String dbFile = (String) dbPropsCombo.getSelectedItem();
            String userFile = (String) userPropsCombo.getSelectedItem();
            if (dbFile == null || userFile == null) return;

            Properties dbProps = loadProps(dbFile);
            Properties userProps = loadProps(userFile);

            String expectedUser = userProps.getProperty("username", "");
            String expectedPass = userProps.getProperty("password", "");

            String enteredUser = usernameField.getText().trim();
            String enteredPass = new String(passwordField.getPassword());

            if (!enteredUser.equals(expectedUser) || !enteredPass.equals(expectedPass)) {
                setStatus(false, "NO CONNECTION - Credentials Mismatch");
                JOptionPane.showMessageDialog(this,
                        "Either the username or password does not match the selected properties file.\nNo connection established.",
                        "Credentials Mismatch", JOptionPane.ERROR_MESSAGE);
                return;
            }

            disconnect(); // close old connection if any
            userConn = DBUtil.connect(dbProps, enteredUser, enteredPass);
            currentLoginUser = enteredUser + "@localhost";

            setStatus(true, dbProps.getProperty("url"));
        } catch (Exception ex) {
            setStatus(false, "NO CONNECTION ESTABLISHED");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void disconnect() {
        if (userConn != null) {
            try { userConn.close(); } catch (Exception ignored) {}
        }
        userConn = null;
        currentLoginUser = null;
        setStatus(false, "NO CONNECTION ESTABLISHED");
    }

    private void setEmptyResults() {
        resultTable = new JTable();
        resultScroll.setViewportView(resultTable);
    }

    private void executeSql() {
        if (userConn == null) {
            JOptionPane.showMessageDialog(this, "Connect to a database first.", "No Connection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = DBUtil.normalizeSingleStatement(sqlArea.getText());
        if (sql.isBlank()) return;

        boolean isRoot = usernameField.getText().trim().equalsIgnoreCase("root");
        boolean usePrepared = !isRoot; // per assignment: client commands must use PreparedStatement

        try {
            boolean hasResultSet;
            ResultSet rs = null;
            int updateCount = -1;

            if (usePrepared) {
                try (PreparedStatement ps = userConn.prepareStatement(sql)) {
                    hasResultSet = ps.execute();

                    if (hasResultSet) {
                        rs = ps.getResultSet();
                        ResultSetTableModel model = new ResultSetTableModel(rs);
                        resultTable = new JTable(model);
                        resultScroll.setViewportView(resultTable);
                    } else {
                        updateCount = ps.getUpdateCount();
                        JOptionPane.showMessageDialog(this,
                                "Statement executed successfully. Rows affected: " + updateCount,
                                "Successful Update", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } else {
                try (Statement st = userConn.createStatement()) {
                    hasResultSet = st.execute(sql);

                    if (hasResultSet) {
                        rs = st.getResultSet();
                        ResultSetTableModel model = new ResultSetTableModel(rs);
                        resultTable = new JTable(model);
                        resultScroll.setViewportView(resultTable);
                    } else {
                        updateCount = st.getUpdateCount();
                        JOptionPane.showMessageDialog(this,
                                "Statement executed successfully. Rows affected: " + updateCount,
                                "Successful Update", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }

            // Logging: successful ops only; do NOT log theaccountant
            boolean isAccountant = (currentLoginUser != null) && currentLoginUser.toLowerCase().startsWith("theaccountant");
            if (logUtil != null && !isAccountant) {
                boolean wasQuery = hasResultSet;   // SHOW/DESCRIBE/SELECT all count as queries
                logUtil.logSuccess(currentLoginUser, wasQuery);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SQLClientApp::new);
    }
}