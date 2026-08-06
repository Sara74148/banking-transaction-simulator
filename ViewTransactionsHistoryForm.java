import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.LinkedList;
import javax.swing.border.*;

public class ViewTransactionsHistoryForm extends JFrame {

    // Components
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JButton backButton;
    private JButton refreshButton;
    private JLabel statusLabel;

    // Colors
    private final Color HEADER_COLOR = new Color(139, 21, 56); // Maroon/Burgundy
    private final Color BACKGROUND_COLOR = new Color(197, 181, 229); // Light Purple
    private final Color BUTTON_COLOR = new Color(255, 255, 0); // Yellow
    private final Color BUTTON_HOVER_COLOR = new Color(255, 255, 102); // Light Yellow

    // File to store history
    private final String HISTORY_FILE = "transaction_history.txt";

    // LinkedList to store transaction history
    private LinkedList<HistoryRecord> transactionHistory;

    public ViewTransactionsHistoryForm() {
        // Set up the frame
        setTitle("RS Bank - Transaction History");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen
        setLayout(new BorderLayout());

        // Set background color
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Initialize linked list
        transactionHistory = new LinkedList<>();

        // Create and add components
        createHeader();
        createContent();

        // Load history
        loadHistory();

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
        JLabel headerLabel = new JLabel("Transaction History");
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
        String[] columnNames = {"Date & Time", "Activity Type", "Account Number", "From Account",
                "To Account", "Amount", "Account Type", "Status", "Details"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setFont(new Font("Arial", Font.PLAIN, 13));
        historyTable.setRowHeight(35);
        historyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        historyTable.getTableHeader().setBackground(HEADER_COLOR);
        historyTable.getTableHeader().setForeground(Color.WHITE);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Set column widths
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(150); // Date & Time
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(130); // Activity Type
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Account Number
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(120); // From Account
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(120); // To Account
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Amount
        historyTable.getColumnModel().getColumn(6).setPreferredWidth(110); // Account Type
        historyTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Status
        historyTable.getColumnModel().getColumn(8).setPreferredWidth(200); // Details

        // Custom cell renderer for colored rows based on activity type
        historyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    String activityType = (String) table.getValueAt(row, 1);
                    switch (activityType) {
                        case "Account Created":
                            c.setBackground(new Color(200, 255, 200)); // Light green
                            break;
                        case "Deposit":
                        case "Deposit Processed":
                            c.setBackground(new Color(144, 238, 144)); // Green
                            break;
                        case "Withdraw":
                        case "Withdraw Processed":
                            c.setBackground(new Color(255, 200, 200)); // Light red
                            break;
                        case "Transfer":
                        case "Transfer Processed":
                            c.setBackground(new Color(200, 220, 255)); // Light blue
                            break;
                        case "Transaction Queued":
                            c.setBackground(new Color(255, 255, 200)); // Light yellow
                            break;
                        case "Transaction Undone":
                            c.setBackground(new Color(255, 220, 180)); // Light orange
                            break;
                        default:
                            c.setBackground(Color.WHITE);
                            break;
                    }
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(HEADER_COLOR, 2));

        // Status label
        statusLabel = new JLabel("Total Records: 0");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        refreshButton = createStyledButton("Refresh");
        backButton = createStyledButton("Back to Main");

        refreshButton.addActionListener(e -> refreshHistory());
        backButton.addActionListener(e -> goBackToMain());

        buttonPanel.add(refreshButton);
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

    private void loadHistory() {
        // Clear existing data
        tableModel.setRowCount(0);
        transactionHistory.clear();

        // Read history from file
        try (BufferedReader reader = new BufferedReader(new FileReader(HISTORY_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 9) {
                    String timestamp = parts[0];
                    String activityType = parts[1];
                    String accountNumber = parts[2];
                    String fromAccount = parts[3];
                    String toAccount = parts[4];
                    String amount = parts[5];
                    String accountType = parts[6];
                    String status = parts[7];
                    String details = parts[8];

                    // Create history record and add to linked list
                    HistoryRecord record = new HistoryRecord(timestamp, activityType, accountNumber,
                            fromAccount, toAccount, amount,
                            accountType, status, details);
                    transactionHistory.add(record);

                    // Add to table
                    tableModel.addRow(new Object[]{
                            timestamp,
                            activityType,
                            accountNumber,
                            fromAccount,
                            toAccount,
                            amount,
                            accountType,
                            status,
                            details
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

    private void refreshHistory() {
        loadHistory();
        showInfo("History refreshed successfully!");
    }

    private void updateStatus() {
        statusLabel.setText(String.format("Total Records: %d | Showing all transaction history",
                transactionHistory.size()));
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void goBackToMain() {
        this.dispose();
        new RSBankMainPage();
    }

    // Helper class to store history records
    private class HistoryRecord {
        String timestamp;
        String activityType;
        String accountNumber;
        String fromAccount;
        String toAccount;
        String amount;
        String accountType;
        String status;
        String details;

        HistoryRecord(String timestamp, String activityType, String accountNumber,
                      String fromAccount, String toAccount, String amount,
                      String accountType, String status, String details) {
            this.timestamp = timestamp;
            this.activityType = activityType;
            this.accountNumber = accountNumber;
            this.fromAccount = fromAccount;
            this.toAccount = toAccount;
            this.amount = amount;
            this.accountType = accountType;
            this.status = status;
            this.details = details;
        }
    }

    // Static method to log history (called from other forms)
    public static void logHistory(String activityType, String accountNumber, String fromAccount,
                                  String toAccount, String amount, String accountType,
                                  String status, String details) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("transaction_history.txt", true))) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = sdf.format(new Date());

            String historyEntry = String.format("%s|%s|%s|%s|%s|%s|%s|%s|%s%n",
                    timestamp, activityType, accountNumber, fromAccount, toAccount,
                    amount, accountType, status, details);

            writer.write(historyEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ViewTransactionsHistoryForm();
        });
    }
}