import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.border.*;

public class TransferForm extends JFrame {

    // Components
    private JTextField fromAccountField;
    private JTextField toAccountField;
    private JTextField amountField;
    private JButton transferButton;
    private JButton backButton;

    // Colors
    private final Color HEADER_COLOR = new Color(139, 21, 56); // Maroon/Burgundy
    private final Color BACKGROUND_COLOR = new Color(197, 181, 229); // Light Purple
    private final Color BUTTON_COLOR = new Color(255, 255, 0); // Yellow
    private final Color BUTTON_HOVER_COLOR = new Color(255, 255, 102); // Light Yellow

    // File to store accounts
    private final String ACCOUNTS_FILE = "accounts.txt";

    public TransferForm() {
        // Set up the frame
        setTitle("RS Bank - Transfer");
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
        JLabel headerLabel = new JLabel("Transfer Funds");
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

        // Transfer From Account
        formGbc.gridx = 0;
        formGbc.gridy = 0;
        JLabel fromLabel = new JLabel("Transfer From (Account Number):");
        fromLabel.setFont(labelFont);
        formPanel.add(fromLabel, formGbc);

        formGbc.gridx = 1;
        formGbc.weightx = 1.0;
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        fromAccountField = new JTextField(25);
        fromAccountField.setFont(fieldFont);
        fromAccountField.setPreferredSize(new Dimension(300, 35));
        formPanel.add(fromAccountField, formGbc);

        // Transfer To Account
        formGbc.gridx = 0;
        formGbc.gridy = 1;
        formGbc.weightx = 0;
        formGbc.fill = GridBagConstraints.NONE;
        JLabel toLabel = new JLabel("Transfer To (Account Number):");
        toLabel.setFont(labelFont);
        formPanel.add(toLabel, formGbc);

        formGbc.gridx = 1;
        formGbc.weightx = 1.0;
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        toAccountField = new JTextField(25);
        toAccountField.setFont(fieldFont);
        toAccountField.setPreferredSize(new Dimension(300, 35));
        formPanel.add(toAccountField, formGbc);

        // Amount
        formGbc.gridx = 0;
        formGbc.gridy = 2;
        formGbc.weightx = 0;
        formGbc.fill = GridBagConstraints.NONE;
        JLabel amountLabel = new JLabel("Amount:");
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
        formGbc.gridy = 3;
        formGbc.gridwidth = 2;
        formGbc.anchor = GridBagConstraints.CENTER;
        formGbc.insets = new Insets(30, 10, 10, 10);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);

        transferButton = createStyledButton("Transfer");
        backButton = createStyledButton("Back to Main");

        transferButton.addActionListener(e -> handleTransfer());
        backButton.addActionListener(e -> goBackToMain());

        buttonPanel.add(transferButton);
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

    private void handleTransfer() {
        // Get input values
        String fromAccount = fromAccountField.getText().trim();
        String toAccount = toAccountField.getText().trim();
        String amountText = amountField.getText().trim();

        // Validate inputs
        if (fromAccount.isEmpty()) {
            showError("Please enter the account number to transfer from.");
            return;
        }

        if (toAccount.isEmpty()) {
            showError("Please enter the account number to transfer to.");
            return;
        }

        if (amountText.isEmpty()) {
            showError("Please enter the amount to transfer.");
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

        // Check if accounts are the same
        if (fromAccount.equals(toAccount)) {
            showError("Cannot transfer to the same account.");
            return;
        }

        // Validate accounts exist
        Map<String, AccountInfo> accounts = loadAccounts();

        if (!accounts.containsKey(fromAccount)) {
            showError("Source account number does not exist.");
            return;
        }

        if (!accounts.containsKey(toAccount)) {
            showError("Destination account number does not exist.");
            return;
        }

        // Check if source account has sufficient balance
        AccountInfo fromAccountInfo = accounts.get(fromAccount);
        if (fromAccountInfo.balance < amount) {
            showError(String.format("Insufficient balance. Available balance: %.2f", fromAccountInfo.balance));
            return;
        }

        // Get account type for priority (use source account type)
        String accountType = fromAccountInfo.accountType;

        // Add transaction to pending queue
        if (addTransactionToPendingQueue("Transfer", fromAccount, toAccount, amount, accountType)) {
            // Log to history
            ViewTransactionsHistoryForm.logHistory(
                    "Transaction Queued",
                    fromAccount,
                    fromAccount,
                    toAccount,
                    String.format("%.2f", amount),
                    accountType,
                    "Queued",
                    String.format("Transfer queued from %s to %s", fromAccount, toAccount)
            );

            showTransactionQueued("Transfer", fromAccount, toAccount, amount, accountType);
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

    private void showTransactionQueued(String type, String fromAccount, String toAccount,
                                       double amount, String accountType) {
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

        JLabel fromLabel = new JLabel("From Account: " + fromAccount);
        fromLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        fromLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel toLabel = new JLabel("To Account: " + toAccount);
        toLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        toLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

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
        contentPanel.add(fromLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(toLabel);
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
        fromAccountField.setText("");
        toAccountField.setText("");
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
            new TransferForm();
        });
    }
}