package ATM;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ATM {
    private JFrame frame;
    private JPanel screenPanel;
    private JLabel screenMessage;
    private JPanel keypadPanel;
    private Connection conn;
    private int currentAccountNumber = -1;

    public ATM() {
        initializeDBConnection();
        initializeLoginGUI();
    }

    private void initializeDBConnection() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm_system", "root", "Gautam@2004");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Connection Failed!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeLoginGUI() {
        frame = new JFrame("ATM Interface");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Bank name at the top
        JLabel bankNameLabel = new JLabel("Reserve Bank Of India", JLabel.CENTER);
        bankNameLabel.setFont(new Font("Verdana", Font.BOLD, 24));  // Set font size and bold
        bankNameLabel.setForeground(new Color(0, 122, 204));        // Blue color for bank name
        frame.add(bankNameLabel, BorderLayout.NORTH);  // Add to the top of the frame

        // Center screen for message
        screenPanel = new JPanel();
        screenPanel.setBackground(new Color(60, 63, 65));
        screenMessage = new JLabel("Welcome. Please Enter Your PIN.", JLabel.CENTER);
        screenMessage.setFont(new Font("Verdana", Font.BOLD, 18));
        screenMessage.setForeground(Color.WHITE);
        screenPanel.add(screenMessage);

        // Keypad layout
        keypadPanel = new JPanel(new GridLayout(4, 3, 10, 10));
        keypadPanel.setBackground(new Color(50, 50, 50));

        // Number buttons
        for (int i = 1; i <= 9; i++) {
            JButton numButton = new JButton(String.valueOf(i));
            numButton.setFont(new Font("Arial", Font.BOLD, 38));
            numButton.setBackground(new Color(0, 122, 204));
            numButton.setForeground(Color.WHITE);
            numButton.addActionListener(this::numberButtonPressed);
            keypadPanel.add(numButton);
        }

        // Clear, 0, Enter buttons
        JButton clearButton = new JButton("Clear");
        clearButton.setBackground(Color.YELLOW);
        clearButton.setForeground(Color.BLACK);
        clearButton.addActionListener(e -> screenMessage.setText("Please Enter Your PIN"));

        JButton zeroButton = new JButton("0");
        zeroButton.setBackground(new Color(0, 132, 204));
        zeroButton.setForeground(Color.WHITE);
        zeroButton.addActionListener(this::numberButtonPressed);

        JButton enterButton = new JButton("Enter");
        enterButton.setBackground(Color.GREEN);
        enterButton.setForeground(Color.BLACK);
        enterButton.addActionListener(e -> checkPIN());

        keypadPanel.add(clearButton);
        keypadPanel.add(zeroButton);
        keypadPanel.add(enterButton);

        frame.add(screenPanel, BorderLayout.CENTER);
        frame.add(keypadPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private void numberButtonPressed(ActionEvent e) {
        String buttonText = ((JButton) e.getSource()).getText();
        screenMessage.setText(screenMessage.getText().equals("Welcome. Please Enter Your PIN.") ? buttonText : screenMessage.getText() + buttonText);
    }

    private void checkPIN() {
        try {
            String pinInput = screenMessage.getText();
            String query = "SELECT * FROM users WHERE pin = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, pinInput);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                currentAccountNumber = rs.getInt("account_number");
                JOptionPane.showMessageDialog(null, "PIN Accepted. Access Granted.");
                frame.dispose();
                showMainOptions();
            } else {
                JOptionPane.showMessageDialog(null, "Invalid PIN. Try Again.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
        }
    }

    // Main options screen after successful login
    private void showMainOptions() {
        frame = new JFrame("ATM Options");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(5, 1, 10, 10));

        JButton withdrawButton = new JButton("Withdrawal");
        JButton balanceButton = new JButton("Balance Enquiry");
        JButton depositButton = new JButton("Deposit");
        JButton pinChangeButton = new JButton("Change/Generate PIN");

        withdrawButton.setFont(new Font("Arial", Font.BOLD, 20));
        balanceButton.setFont(new Font("Arial", Font.BOLD, 20));
        depositButton.setFont(new Font("Arial", Font.BOLD, 20));
        pinChangeButton.setFont(new Font("Arial", Font.BOLD, 20));

        frame.add(withdrawButton);
        frame.add(balanceButton);
        frame.add(depositButton);
        frame.add(pinChangeButton);

        withdrawButton.addActionListener(e -> withdrawAmount());
        balanceButton.addActionListener(e -> checkBalance());
        depositButton.addActionListener(e -> depositAmount());
        pinChangeButton.addActionListener(e -> changePIN());

        frame.setVisible(true);
    }

    // Option 1: Withdrawal
    private void withdrawAmount() {
        String amountStr = JOptionPane.showInputDialog("Enter the amount to withdraw:");
        int amount = Integer.parseInt(amountStr);

        try {
            String query = "SELECT balance FROM users WHERE account_number = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, currentAccountNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int currentBalance = rs.getInt("balance");

                if (currentBalance >= amount) {
                    currentBalance -= amount;

                    String updateQuery = "UPDATE users SET balance = ? WHERE account_number = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                    updateStmt.setInt(1, currentBalance);
                    updateStmt.setInt(2, currentAccountNumber);
                    updateStmt.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Withdrawal successful. Remaining balance: " + currentBalance);
                } else {
                    JOptionPane.showMessageDialog(null, "Insufficient balance.");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
        }
    }

    // Option 2: Balance Enquiry
    private void checkBalance() {
        try {
            String query = "SELECT balance FROM users WHERE account_number = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, currentAccountNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int balance = rs.getInt("balance");
                JOptionPane.showMessageDialog(null, "Your current balance is: " + balance);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
        }
    }

    // Option 3: Deposit
    private void depositAmount() {
        String amountStr = JOptionPane.showInputDialog("Enter the amount to deposit:");
        int amount = Integer.parseInt(amountStr);

        try {
            String query = "SELECT balance FROM users WHERE account_number = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, currentAccountNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int currentBalance = rs.getInt("balance");
                currentBalance += amount;

                String updateQuery = "UPDATE users SET balance = ? WHERE account_number = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setInt(1, currentBalance);
                updateStmt.setInt(2, currentAccountNumber);
                updateStmt.executeUpdate();

                JOptionPane.showMessageDialog(null, "Deposit successful. New balance: " + currentBalance);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
        }
    }

    // Option 4: Change/Generate PIN
    private void changePIN() {
        String newPin = JOptionPane.showInputDialog("Enter new 4-digit PIN:");

        if (newPin.length() != 4 || !newPin.matches("\\d+")) {
            JOptionPane.showMessageDialog(null, "Invalid PIN format. Must be 4 digits.");
            return;
        }

        try {
            String updateQuery = "UPDATE users SET pin = ? WHERE account_number = ?";
            PreparedStatement stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, newPin);
            stmt.setInt(2, currentAccountNumber);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(null, "PIN changed successfully.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new ATM();
    }
}
