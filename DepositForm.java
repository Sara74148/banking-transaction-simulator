import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.border.*;

public class DepositForm extends JFrame {

    // Components
    private JTextField accountNumberField;
    private JTextField amountField;
    private JButton depositButton;
    private JButton backButton;

    // Colors
    private final Color HEADER_COLOR = new Color(139, 21, 56); // Maroon/Burgundy
    private final Color BACKGROUND_COLOR = new Color(197, 181, 229); // Light Purple
    private final Color BUTTON_COLOR = new Color(255, 255, 0); // Yellow
    private final Color BUTTON_HOVER_COLOR = new Color(255, 255, 102); // Light Yellow

    // File to store accounts
    private final String ACCOUNTS_FILE = "accounts.txt";

    public DepositForm() {
        // Set up the frame
        setTitle("RS Bank - Deposit");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen
        setLayout(new BorderLayout());

        // Set background color
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Create and add components
        createHeader();
        createForm();

        // Make visible
        setVisible(true);
    }

    private void createHeader() {
        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(HEADER_COLOR);
        headerPanel.setPreferredSize(new Dimension(getWidth(), 80));
        headerPanel.setLayout(new GridBagLayout());

        // Header label
        JLabel headerLabel = new JLabel("Deposit Funds");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 36));
        headerLabel.setForeground(Color.WHITE);

        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void createForm() {
        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create form panel with border
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(HEADER_COLOR, 3),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.insets = new Insets(10, 10, 10, 10);
        formGbc.anchor = GridBagConstraints.WEST;

        // Font for labels and fields
        Font labelFont = new Font("Arial", Font.BOLD, 18);
        Font fieldFont = new Font("Arial", Font.PLAIN, 16);

        // Account Number
        formGbc.gridx = 0;
        formGbc.gridy = 0;
        JLabel accountLabel = new JLabel("Account Number:");
        accountLabel.setFont(labelFont);
        formPanel.add(accountLabel, formGbc);

        formGbc.gridx = 1;
        formGbc.weightx = 1.0;
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        accountNumberField = new JTextField(25);
        accountNumberField.setFont(fieldFont);
        accountNumberField.setPreferredSize(new Dimension(300, 35));
        formPanel.add(accountNumberField, formGbc);

        // Amount
        formGbc.gridx = 0;
        formGbc.gridy = 1;
        formGbc.weightx = 0;
        formGbc.fill = GridBagConstraints.NONE;
        JLabel amountLabel = new JLabel("Amount to Deposit:");
        amountLabel.setFont(labelFont);
        formPanel.add(amountLabel, formGbc);

        formGbc.gridx = 1;
        formGbc.weightx = 1.0;
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        amountField = new JTextField(25);
        amountField.setFont(fieldFont);
        amountField.setPreferredSize(new Dimension(300, 35));
        formPanel.add(amountField, formGbc);

        // Buttons Panel
        formGbc.gridx = 0;
        formGbc.gridy = 2;
        formGbc.gridwidth = 2;
        formGbc.anchor = GridBagConstraints.CENTER;
        formGbc.insets = new Insets(30, 10, 10, 10);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);

        depositButton = createStyledButton("Deposit");
        backButton = createStyledButton("Back to Main");

        depositButton.addActionListener(e -> handleDeposit());
        backButton.addActionListener(e -> goBackToMain());

        buttonPanel.add(depositButton);
        buttonPanel.add(backButton);
        formPanel.add(buttonPanel, formGbc);

        // Add form panel to content panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(formPanel, gbc);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);

        // Button styling
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.BLACK);
        button.setPreferredSize(new Dimension(200, 50));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BUTTON_HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(BUTTON_COLOR);
            }
        });

        return button;
    }

    private void handleDeposit() {
        // Get input values
        String accountNumber = accountNumberField.getText().trim();
        String amountText = amountField.getText().trim();

        // Validate inputs
        if (accountNumber.isEmpty()) {
            showError("Please enter account number.");
            return;
        }

        if (amountText.isEmpty()) {
            showError("Please enter the amount to deposit.");
            return;
        }

        // Validate amount
        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                showError("Amount must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid amount.");
            return;
        }

        // Load accounts
        Map<String, AccountInfo> accounts = loadAccounts();

        // Validate account exists
        if (!accounts.containsKey(accountNumber)) {
            showError("Account number does not exist.");
            return;
        }

        // Get account info
        AccountInfo accountInfo = accounts.get(accountNumber);

        // Get account type for priority
        String accountType = accountInfo.accountType;

        // Add transaction to pending queue
        if (addTransactionToPendingQueue("Deposit", "-", accountNumber, amount, accountType)) {
            // Log to history
            ViewTransactionsHistoryForm.logHistory(
                    "Transaction Queued",
                    accountNumber,
                    "-",
                    accountNumber,
                    String.format("%.2f", amount),
                    accountType,
                    "Queued",
                    String.format("Deposit queued to account %s", accountNumber)
            );

            showTransactionQueued("Deposit", accountNumber, amount, accountType);
            clearForm();
        } else {
            showError("Error queuing transaction. Please try again.");
        }
    }

    private boolean addTransactionToPendingQueue(String type, String fromAccount, String toAccount,
                                                 double amount, String accountType) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("pending_transactions.txt", true))) {
            String transactionData = String.format("%s|%s|%s|%.2f|%s%n",
                    type, fromAccount, toAccount, amount, accountType);
            writer.write(transactionData);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showTransactionQueued(String type, String accountNumber, double amount, String accountType) {
        JDialog dialog = new JDialog(this, "Transaction Queued", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(this);

        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Success message
        JLabel successLabel = new JLabel("✓ Transaction Added to Queue!");
        successLabel.setFont(new Font("Arial", Font.BOLD, 24));
        successLabel.setForeground(new Color(0, 150, 0));
        successLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel typeLabel = new JLabel("Transaction Type: " + type);
        typeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel accountLabel = new JLabel("Account: " + accountNumber);
        accountLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        accountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel amountLabel = new JLabel(String.format("Amount: %.2f", amount));
        amountLabel.setFont(new Font("Arial", Font.BOLD, 18));
        amountLabel.setForeground(HEADER_COLOR);
        amountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel accountTypeLabel = new JLabel("Priority: " + accountType);
        accountTypeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        accountTypeLabel.setForeground(accountType.equalsIgnoreCase("VIP") ? new Color(255, 140, 0) : new Color(100, 100, 100));
        accountTypeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel noteLabel = new JLabel("Transaction will be processed based on priority.");
        noteLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        noteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(successLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(typeLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(accountLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(amountLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(accountTypeLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(noteLabel);

        // OK button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        JButton okButton = createStyledButton("OK");
        okButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(okButton);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private Map<String, AccountInfo> loadAccounts() {
        Map<String, AccountInfo> accounts = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(ACCOUNTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6) {
                    String accountNumber = parts[0];
                    String fullName = parts[1];
                    int age = Integer.parseInt(parts[2]);
                    String cnic = parts[3];
                    String accountType = parts[4];
                    double balance = Double.parseDouble(parts[5]);

                    accounts.put(accountNumber, new AccountInfo(accountNumber, fullName, age, cnic, accountType, balance));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return accounts;
    }

    private boolean updateAccountsFile(Map<String, AccountInfo> accounts) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ACCOUNTS_FILE))) {
            for (AccountInfo account : accounts.values()) {
                String accountData = String.format("%s|%s|%d|%s|%s|%.2f%n",
                        account.accountNumber, account.fullName, account.age,
                        account.cnic, account.accountType, account.balance);
                writer.write(accountData);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void clearForm() {
        accountNumberField.setText("");
        amountField.setText("");
    }

    private void goBackToMain() {
        this.dispose();
        new RSBankMainPage();
    }

    // Helper class to store account information
    private class AccountInfo {
        String accountNumber;
        String fullName;
        int age;
        String cnic;
        String accountType;
        double balance;

        AccountInfo(String accountNumber, String fullName, int age, String cnic, String accountType, double balance) {
            this.accountNumber = accountNumber;
            this.fullName = fullName;
            this.age = age;
            this.cnic = cnic;
            this.accountType = accountType;
            this.balance = balance;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DepositForm();
        });
    }
}