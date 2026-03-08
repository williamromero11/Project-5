package Project3;

/*
Name: <your name goes here>
Course: CNT 4714 Spring 2026
Assignment title: Project 3 – A Specialized Accountant Application
Date: March 15, 2026
Class: <name of class goes here>
*/

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class AccountantApp extends JFrame {

    private final JTextField usernameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);
    private final JLabel connectionStatus = new JLabel("NO CONNECTION ESTABLISHED");
    private final JTextArea sqlArea = new JTextArea(6, 60);

    private JTable resultTable = new JTable();
    private final JScrollPane resultScroll = new JScrollPane(resultTable);

    private Connection conn;

    public AccountantApp() {
        super("SPECIALIZED ACCOUNTANT APPLICATION - CNT 4714 - PROJECT 3");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(950, 680);
        setLocationRelativeTo(null);

        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(8,8));
        root.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        JPanel connPanel = new JPanel(new GridBagLayout());
        connPanel.setBorder(new TitledBorder("Connection Details"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,4,4,4);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx=0; gc.gridy=0;
        connPanel.add(new JLabel("DB URL Properties: operationslog.properties"), gc);
        gc.gridy=1;
        connPanel.add(new JLabel("User Properties: theaccountant.properties"), gc);

        gc.gridy=2; gc.gridx=0; connPanel.add(new JLabel("Username"), gc);
        gc.gridx=1; connPanel.add(usernameField, gc);

        gc.gridy=3; gc.gridx=0; connPanel.add(new JLabel("Password"), gc);
        gc.gridx=1; connPanel.add(passwordField, gc);

        JButton connectBtn = new JButton("Connect to Database");
        JButton disconnectBtn = new JButton("Disconnect From Database");

        gc.gridx=2; gc.gridy=2; gc.fill=GridBagConstraints.HORIZONTAL;
        connPanel.add(connectBtn, gc);
        gc.gridy=3;
        connPanel.add(disconnectBtn, gc);
        gc.fill=GridBagConstraints.NONE;

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(new TitledBorder("Connection Status"));
        connectionStatus.setOpaque(true);
        connectionStatus.setBackground(new Color(200,60,60));
        connectionStatus.setForeground(Color.WHITE);
        connectionStatus.setHorizontalAlignment(SwingConstants.CENTER);
        connectionStatus.setFont(connectionStatus.getFont().deriveFont(Font.BOLD, 14f));
        statusPanel.add(connectionStatus, BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout(8,8));
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

    private Properties loadProps(String filename) throws Exception {
        Properties p = new Properties();
        try (FileInputStream fis = new FileInputStream("config/" + filename)) {
            p.load(fis);
        }
        return p;
    }

    private void setStatus(boolean connected, String text) {
        connectionStatus.setText(text);
        if (connected) connectionStatus.setBackground(new Color(40,140,60));
        else connectionStatus.setBackground(new Color(200,60,60));
    }

    private void connect() {
        try {
            Properties dbProps = loadProps("operationslog.properties");
            Properties userProps = loadProps("theaccountant.properties");

            String expectedUser = userProps.getProperty("username", "");
            String expectedPass = userProps.getProperty("password", "");

            String enteredUser = usernameField.getText().trim();
            String enteredPass = new String(passwordField.getPassword());

            if (!enteredUser.equals(expectedUser) || !enteredPass.equals(expectedPass)) {
                setStatus(false, "NO CONNECTION - Credentials Mismatch");
                JOptionPane.showMessageDialog(this,
                        "Credentials do not match theaccountant.properties.\nNo connection established.",
                        "Credentials Mismatch", JOptionPane.ERROR_MESSAGE);
                return;
            }

            disconnect();
            conn = DBUtil.connect(dbProps, enteredUser, enteredPass);
            setStatus(true, dbProps.getProperty("url"));
        } catch (Exception ex) {
            setStatus(false, "NO CONNECTION ESTABLISHED");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void disconnect() {
        if (conn != null) {
            try { conn.close(); } catch (Exception ignored) {}
        }
        conn = null;
        setStatus(false, "NO CONNECTION ESTABLISHED");
    }

    private void setEmptyResults() {
        resultTable = new JTable();
        resultScroll.setViewportView(resultTable);
    }

    private void executeSql() {
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Connect to operationslog first.", "No Connection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = DBUtil.normalizeSingleStatement(sqlArea.getText());
        if (sql.isBlank()) return;

        // accountant is only allowed to query; keep it strict
        if (!DBUtil.isSelect(sql)) {
            JOptionPane.showMessageDialog(this, "Accountant app is SELECT-only.", "Not Allowed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ResultSetTableModel model = new ResultSetTableModel(rs);
            resultTable = new JTable(model);
            resultScroll.setViewportView(resultTable);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AccountantApp::new);
    }
}