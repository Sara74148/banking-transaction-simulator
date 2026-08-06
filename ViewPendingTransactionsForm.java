import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.border.*;

public class ViewPendingTransactionsForm extends JFrame {

    // Components
    private JTable transactionsTable;
    private DefaultTableModel tableModel;
    private JButton processOneButton;
    private JButton processAllButton;
    private JButton backButton;
    private JLabel statusLabel;

    // Colors
    private final Color HEADER_COLOR = new Color(139, 21, 56); // Maroon/Burgundy
    private final Color BACKGROUND_COLOR = new Color(197, 181, 229); // Light Purple
    private final Color BUTTON_COLOR = new Color(255, 255, 0); // Yellow
    private final Color BUTTON_HOVER_COLOR = new Color(255, 255, 102); // Light Yellow
    private final Color PENDING_COLOR = new Color(173, 216, 230); // Light Blue
    private final Color PROCESSED_COLOR = new Color(144, 238, 144); // Light Green

    // Files
    private final String ACCOUNTS_FILE = "accounts.txt";
    private final String TRANSACTIONS_FILE = "pending_transactions.txt";

    // Priority Queue for transactions
    private PriorityQueue<Transaction> transactionQueue;
    private List<Integer> processedRows;

    public ViewPendingTransactionsForm() {
        // Set up the frame
        setTitle("RS Bank - Pending Transactions");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen
        setLayout(new BorderLayout());

        // Set background color
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Initialize data structures
        transactionQueue = new PriorityQueue<>(new TransactionComparator());
        processedRows = new ArrayList<>();

        // Create and add components
        createHeader();
        createContent();

        // Load transactions
        loadTransactions();

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
        JLabel headerLabel = new JLabel("Pending Transactions");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 36));
        headerLabel.setForeground(Color.WHITE);

        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void createContent() {
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create table
        String[] columnNames = {"Transaction ID", "Type", "From Account", "To Account", "Amount", "Account Type", "Status", "Action"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only the Action column is editable (for button)
            }

            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 7) return JButton.class;
                return Object.class;
            }
        };

        transactionsTable = new JTable(tableModel);
        transactionsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        transactionsTable.setRowHeight(40);
        transactionsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        transactionsTable.getTableHeader().setBackground(HEADER_COLOR);
        transactionsTable.getTableHeader().setForeground(Color.WHITE);
        transactionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set column widths
        transactionsTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        transactionsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        transactionsTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        transactionsTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        transactionsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        transactionsTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        transactionsTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        transactionsTable.getColumnModel().getColumn(7).setPreferredWidth(100);

        // Custom cell renderer for row colors
        transactionsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected && column != 7) {
                    if (processedRows.contains(row)) {
                        c.setBackground(PROCESSED_COLOR);
                    } else {
                        c.setBackground(PENDING_COLOR);
                    }
                }

                return c;
            }
        });

        // Add button renderer and editor for Undo column
        transactionsTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        transactionsTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(HEADER_COLOR, 2));

        // Status label
        statusLabel = new JLabel("Total Pending Transactions: 0");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        processOneButton = createStyledButton("Process Next Transaction");
        processAllButton = createStyledButton("Process All Transactions");
        backButton = createStyledButton("Back to Main");

        processOneButton.addActionListener(e -> processNextTransaction());
        processAllButton.addActionListener(e -> processAllTransactions());
        backButton.addActionListener(e -> goBackToMain());

        buttonPanel.add(processOneButton);
        buttonPanel.add(processAllButton);
        buttonPanel.add(backButton);

        // Add components
        contentPanel.add(statusLabel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);

        // Button styling
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.BLACK);
        button.setPreferredSize(new Dimension(220, 50));
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

    private void loadTransactions() {
        // Clear existing data
        tableModel.setRowCount(0);
        transactionQueue.clear();
        processedRows.clear();

        // Read transactions from file
        try (BufferedReader reader = new BufferedReader(new FileReader(TRANSACTIONS_FILE))) {
            String line;
            int transactionId = 1;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    String type = parts[0];
                    String fromAccount = parts[1];
                    String toAccount = parts[2];
                    double amount = Double.parseDouble(parts[3]);
                    String accountType = parts[4];

                    // Create transaction object
                    Transaction transaction = new Transaction(transactionId++, type, fromAccount,
                            toAccount, amount, accountType);
                    transactionQueue.offer(transaction);

                    // Add to table with Undo button
                    tableModel.addRow(new Object[]{
                            transaction.id,
                            transaction.type,
                            transaction.fromAccount,
                            transaction.toAccount,
                            String.format("%.2f", transaction.amount),
                            transaction.accountType,
                            "Pending",
                            "Undo"
                    });
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet, that's okay
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateStatus();
    }

    private void processNextTransaction() {
        if (transactionQueue.isEmpty()) {
            showInfo("No pending transactions to process.");
            return;
        }

        // Get next transaction from priority queue
        Transaction transaction = transactionQueue.poll();

        // Find the row in table
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int rowId = (int) tableModel.getValueAt(i, 0);
            if (rowId == transaction.id && !processedRows.contains(i)) {
                // Process the transaction
                boolean success = executeTransaction(transaction);

                if (success) {
                    // Mark as processed
                    processedRows.add(i);
                    tableModel.setValueAt("Processed", i, 6);
                    transactionsTable.repaint();

                    // Log to history
                    ViewTransactionsHistoryForm.logHistory(
                            transaction.type + " Processed",
                            transaction.fromAccount.equals("-") ? transaction.toAccount : transaction.fromAccount,
                            transaction.fromAccount,
                            transaction.toAccount,
                            String.format("%.2f", transaction.amount),
                            transaction.accountType,
                            "Completed",
                            String.format("%s transaction processed successfully", transaction.type)
                    );

                    showSuccess(String.format("Transaction #%d processed successfully!\nType: %s\nAmount: %.2f",
                            transaction.id, transaction.type, transaction.amount));
                } else {
                    showError("Failed to process transaction. Please check account details.");
                    // Put it back in queue
                    transactionQueue.offer(transaction);
                }
                break;
            }
        }

        updateStatus();

        // Update transactions file
        saveRemainingTransactions();
    }

    private void processAllTransactions() {
        if (transactionQueue.isEmpty()) {
            showInfo("No pending transactions to process.");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        // Process all transactions in priority order
        while (!transactionQueue.isEmpty()) {
            Transaction transaction = transactionQueue.poll();

            // Find the row in table
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                int rowId = (int) tableModel.getValueAt(i, 0);
                if (rowId == transaction.id && !processedRows.contains(i)) {
                    // Process the transaction
                    boolean success = executeTransaction(transaction);

                    if (success) {
                        // Mark as processed
                        processedRows.add(i);
                        tableModel.setValueAt("Processed", i, 6);
                        successCount++;

                        // Log to history
                        ViewTransactionsHistoryForm.logHistory(
                                transaction.type + " Processed",
                                transaction.fromAccount.equals("-") ? transaction.toAccount : transaction.fromAccount,
                                transaction.fromAccount,
                                transaction.toAccount,
                                String.format("%.2f", transaction.amount),
                                transaction.accountType,
                                "Completed",
                                String.format("%s transaction processed successfully", transaction.type)
                        );

                        // Add delay for visual effect
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        transactionsTable.repaint();
                    } else {
                        failCount++;
                    }
                    break;
                }
            }
        }

        updateStatus();

        // Show summary
        showSuccess(String.format("Processing Complete!\nSuccessful: %d\nFailed: %d",
                successCount, failCount));

        // Update transactions file
        saveRemainingTransactions();
    }

    private boolean executeTransaction(Transaction transaction) {
        Map<String, AccountInfo> accounts = loadAccounts();

        switch (transaction.type.toLowerCase()) {
            case "withdraw":
                if (accounts.containsKey(transaction.fromAccount)) {
                    AccountInfo account = accounts.get(transaction.fromAccount);
                    if (account.balance >= transaction.amount) {
                        account.balance -= transaction.amount;
                        return updateAccountsFile(accounts);
                    }
                }
                break;

            case "deposit":
                if (accounts.containsKey(transaction.toAccount)) {
                    AccountInfo account = accounts.get(transaction.toAccount);
                    account.balance += transaction.amount;
                    return updateAccountsFile(accounts);
                }
                break;

            case "transfer":
                if (accounts.containsKey(transaction.fromAccount) &&
                        accounts.containsKey(transaction.toAccount)) {
                    AccountInfo fromAccount = accounts.get(transaction.fromAccount);
                    AccountInfo toAccount = accounts.get(transaction.toAccount);
                    if (fromAccount.balance >= transaction.amount) {
                        fromAccount.balance -= transaction.amount;
                        toAccount.balance += transaction.amount;
                        return updateAccountsFile(accounts);
                    }
                }
                break;
        }

        return false;
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

                    accounts.put(accountNumber, new AccountInfo(accountNumber, fullName, age,
                            cnic, accountType, balance));
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

    private void saveRemainingTransactions() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TRANSACTIONS_FILE))) {
            // Save only unprocessed transactions
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (!processedRows.contains(i)) {
                    String type = (String) tableModel.getValueAt(i, 1);
                    String fromAccount = (String) tableModel.getValueAt(i, 2);
                    String toAccount = (String) tableModel.getValueAt(i, 3);
                    String amount = (String) tableModel.getValueAt(i, 4);
                    String accountType = (String) tableModel.getValueAt(i, 5);

                    String transactionData = String.format("%s|%s|%s|%s|%s%n",
                            type, fromAccount, toAccount, amount, accountType);
                    writer.write(transactionData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateStatus() {
        int pending = transactionQueue.size();
        int processed = processedRows.size();
        statusLabel.setText(String.format("Total Transactions: %d | Pending: %d | Processed: %d",
                tableModel.getRowCount(), pending, processed));
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void goBackToMain() {
        this.dispose();
        new RSBankMainPage();
    }

    // Undo transaction method
    private void undoTransaction(int row) {
        // Check if transaction is processed
        if (!processedRows.contains(row)) {
            showInfo("Cannot undo. Transaction has not been processed yet.");
            return;
        }

        // Get transaction details
        String type = (String) tableModel.getValueAt(row, 1);
        String fromAccount = (String) tableModel.getValueAt(row, 2);
        String toAccount = (String) tableModel.getValueAt(row, 3);
        double amount = Double.parseDouble((String) tableModel.getValueAt(row, 4));

        // Reverse the transaction
        Map<String, AccountInfo> accounts = loadAccounts();
        boolean success = false;

        switch (type.toLowerCase()) {
            case "withdraw":
                // Reverse: Add money back to account
                if (accounts.containsKey(fromAccount)) {
                    AccountInfo account = accounts.get(fromAccount);
                    account.balance += amount;
                    success = updateAccountsFile(accounts);
                }
                break;

            case "deposit":
                // Reverse: Remove money from account
                if (accounts.containsKey(toAccount)) {
                    AccountInfo account = accounts.get(toAccount);
                    if (account.balance >= amount) {
                        account.balance -= amount;
                        success = updateAccountsFile(accounts);
                    } else {
                        showError("Cannot undo. Insufficient balance in account.");
                        return;
                    }
                }
                break;

            case "transfer":
                // Reverse: Transfer money back
                if (accounts.containsKey(fromAccount) && accounts.containsKey(toAccount)) {
                    AccountInfo fromAcc = accounts.get(fromAccount);
                    AccountInfo toAcc = accounts.get(toAccount);
                    if (toAcc.balance >= amount) {
                        toAcc.balance -= amount;
                        fromAcc.balance += amount;
                        success = updateAccountsFile(accounts);
                    } else {
                        showError("Cannot undo. Insufficient balance in destination account.");
                        return;
                    }
                }
                break;
        }

        if (success) {
            // Mark as pending again
            processedRows.remove(Integer.valueOf(row));
            tableModel.setValueAt("Pending", row, 6);
            transactionsTable.repaint();

            // Add back to queue
            int id = (int) tableModel.getValueAt(row, 0);
            String accountType = (String) tableModel.getValueAt(row, 5);
            Transaction transaction = new Transaction(id, type, fromAccount, toAccount, amount, accountType);
            transactionQueue.offer(transaction);

            // Log to history
            ViewTransactionsHistoryForm.logHistory(
                    "Transaction Undone",
                    fromAccount.equals("-") ? toAccount : fromAccount,
                    fromAccount,
                    toAccount,
                    String.format("%.2f", amount),
                    accountType,
                    "Reversed",
                    String.format("%s transaction reversed successfully", type)
            );

            updateStatus();
            showSuccess(String.format("Transaction #%d successfully undone!\nType: %s\nAmount: %.2f",
                    id, type, amount));
        } else {
            showError("Failed to undo transaction. Please try again.");
        }
    }

    // Button Renderer class
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Undo" : value.toString());
            setFont(new Font("Arial", Font.BOLD, 12));
            setBackground(new Color(255, 100, 100));
            setForeground(Color.WHITE);
            return this;
        }
    }

    // Button Editor class
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setBackground(new Color(255, 100, 100));
            button.setForeground(Color.WHITE);

            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "Undo" : value.toString();
            button.setText(label);
            isPushed = true;
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                undoTransaction(currentRow);
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }

    // Transaction class
    private class Transaction {
        int id;
        String type;
        String fromAccount;
        String toAccount;
        double amount;
        String accountType;

        Transaction(int id, String type, String fromAccount, String toAccount,
                    double amount, String accountType) {
            this.id = id;
            this.type = type;
            this.fromAccount = fromAccount;
            this.toAccount = toAccount;
            this.amount = amount;
            this.accountType = accountType;
        }
    }

    // Comparator for priority queue - VIP transactions first
    private class TransactionComparator implements Comparator<Transaction> {
        @Override
        public int compare(Transaction t1, Transaction t2) {
            // VIP accounts have higher priority
            if (t1.accountType.equalsIgnoreCase("VIP") && !t2.accountType.equalsIgnoreCase("VIP")) {
                return -1;
            } else if (!t1.accountType.equalsIgnoreCase("VIP") && t2.accountType.equalsIgnoreCase("VIP")) {
                return 1;
            } else {
                // Same priority, maintain order by ID
                return Integer.compare(t1.id, t2.id);
            }
        }
    }

    // Helper class to store account information
    private class AccountInfo {
        String accountNumber;
        String fullName;
        int age;
        String cnic;
        String accountType;
        double balance;

        AccountInfo(String accountNumber, String fullName, int age, String cnic,
                    String accountType, double balance) {
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
            new ViewPendingTransactionsForm();
        });
    }
}