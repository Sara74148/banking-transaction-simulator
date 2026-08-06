import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class ViewAccDetails extends JFrame {

    // Components
    private JTextField accountNumberField;
    private JButton searchButton;
    private JButton viewAllButton;
    private JButton backButton;
    private JTable accountTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    // Colors
    private final Color HEADER_COLOR = new Color(139, 21, 56); // Maroon/Burgundy
    private final Color BACKGROUND_COLOR = new Color(197, 181, 229); // Light Purple
    private final Color BUTTON_COLOR = new Color(255, 255, 0); // Yellow
    private final Color BUTTON_HOVER_COLOR = new Color(255, 255, 102); // Light Yellow

    // File to read accounts
    private final String ACCOUNTS_FILE = "accounts.txt";

    public ViewAccDetails() {
        // Set up the frame
        setTitle("RS Bank - Account Details");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen
        setLayout(new BorderLayout());

        // Set background color
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Create and add components
        createHeader();
        createContent();

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
        JLabel headerLabel = new JLabel("Account Details");
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

        // Search panel at the top
        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(HEADER_COLOR, 3),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        searchPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JLabel searchLabel = new JLabel("Enter Account Number:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 18));

        accountNumberField = new JTextField(20);
        accountNumberField.setFont(new Font("Arial", Font.PLAIN, 16));
        accountNumberField.setPreferredSize(new Dimension(250, 40));

        searchButton = createStyledButton("Search", new Dimension(150, 40));
        searchButton.addActionListener(e -> searchAccount());

        // Add Enter key listener to text field
        accountNumberField.addActionListener(e -> searchAccount());

        searchPanel.add(searchLabel);
        searchPanel.add(accountNumberField);
        searchPanel.add(searchButton);

        // Table panel in the center
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(BACKGROUND_COLOR);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Create table
        String[] columnNames = {"Account Number", "Full Name", "Age", "CNIC", "Account Type", "Balance"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        accountTable = new JTable(tableModel);
        accountTable.setFont(new Font("Arial", Font.PLAIN, 14));
        accountTable.setRowHeight(35);
        accountTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        accountTable.getTableHeader().setBackground(HEADER_COLOR);
        accountTable.getTableHeader().setForeground(Color.WHITE);
        accountTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set column widths
        accountTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        accountTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        accountTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        accountTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        accountTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        accountTable.getColumnModel().getColumn(5).setPreferredWidth(120);

        // Custom cell renderer for VIP accounts
        accountTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    String accountType = (String) table.getValueAt(row, 4);
                    if (accountType.equalsIgnoreCase("VIP")) {
                        c.setBackground(new Color(255, 250, 205)); // Light yellow for VIP
                    } else {
                        c.setBackground(new Color(240, 248, 255)); // Light blue for Standard
                    }
                }

                // Bold and green for balance column
                if (column == 5) {
                    setFont(new Font("Arial", Font.BOLD, 14));
                    setForeground(new Color(0, 128, 0));
                } else {
                    setFont(new Font("Arial", Font.PLAIN, 14));
                    setForeground(Color.BLACK);
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(accountTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(HEADER_COLOR, 2));

        // Status label
        statusLabel = new JLabel("Enter an account number to search or view all accounts");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        tablePanel.add(statusLabel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Buttons panel at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        viewAllButton = createStyledButton("View All Accounts", new Dimension(200, 50));
        backButton = createStyledButton("Back to Main", new Dimension(200, 50));

        viewAllButton.addActionListener(e -> viewAllAccounts());
        backButton.addActionListener(e -> goBackToMain());

        buttonPanel.add(viewAllButton);
        buttonPanel.add(backButton);

        // Add all panels to content panel
        contentPanel.add(searchPanel, BorderLayout.NORTH);
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JButton createStyledButton(String text, Dimension size) {
        JButton button = new JButton(text);

        // Button styling
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.BLACK);
        button.setPreferredSize(size);
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

    private void searchAccount() {
        String accountNumber = accountNumberField.getText().trim();

        if (accountNumber.isEmpty()) {
            showError("Please enter an account number.");
            return;
        }

        // Clear table
        tableModel.setRowCount(0);

        // Search for account
        boolean found = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(ACCOUNTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6 && parts[0].equals(accountNumber)) {
                    // Found the account
                    String accNumber = parts[0];
                    String fullName = parts[1];
                    String age = parts[2];
                    String cnic = parts[3];
                    String accountType = parts[4];
                    String balance = String.format("%.2f", Double.parseDouble(parts[5]));

                    tableModel.addRow(new Object[]{accNumber, fullName, age, cnic, accountType, balance});
                    found = true;
                    statusLabel.setText("Account found: " + fullName + " (" + accountType + ")");
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            showError("No accounts file found. Please create an account first.");
            return;
        } catch (IOException e) {
            showError("Error reading accounts file.");
            e.printStackTrace();
            return;
        }

        if (!found) {
            showError("Account number not found.");
            statusLabel.setText("No account found with number: " + accountNumber);
        }

        // Clear search field
        accountNumberField.setText("");
    }

    private void viewAllAccounts() {
        // Clear table
        tableModel.setRowCount(0);

        int accountCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(ACCOUNTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6) {
                    String accNumber = parts[0];
                    String fullName = parts[1];
                    String age = parts[2];
                    String cnic = parts[3];
                    String accountType = parts[4];
                    String balance = String.format("%.2f", Double.parseDouble(parts[5]));

                    tableModel.addRow(new Object[]{accNumber, fullName, age, cnic, accountType, balance});
                    accountCount++;
                }
            }
        } catch (FileNotFoundException e) {
            showError("No accounts file found. Please create an account first.");
            return;
        } catch (IOException e) {
            showError("Error reading accounts file.");
            e.printStackTrace();
            return;
        }

        if (accountCount == 0) {
            statusLabel.setText("No accounts found in the system.");
            showInfo("No accounts exist yet. Please create an account first.");
        } else {
            statusLabel.setText(String.format("Total Accounts: %d", accountCount));
        }

        // Clear search field
        accountNumberField.setText("");
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ViewAccDetails();
        });
    }
}