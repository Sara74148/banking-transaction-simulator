import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;
import javax.swing.border.*;

public class CreateAccountForm extends JFrame {

    // Components
    private JTextField fullNameField;
    private JTextField ageField;
    private JTextField cnicField;
    private JComboBox<String> accountTypeCombo;
    private JButton createButton;
    private JButton backButton;

    // Colors
    private final Color HEADER_COLOR = new Color(139, 21, 56); // Maroon/Burgundy
    private final Color BACKGROUND_COLOR = new Color(197, 181, 229); // Light Purple
    private final Color BUTTON_COLOR = new Color(255, 255, 0); // Yellow
    private final Color BUTTON_HOVER_COLOR = new Color(255, 255, 102); // Light Yellow

    // File to store accounts
    private final String ACCOUNTS_FILE = "accounts.txt";

    public CreateAccountForm() {
        // Set up the frame
        setTitle("RS Bank - Create Account");
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
        JLabel headerLabel = new JLabel("Create New Account");
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

        // Full Name
        formGbc.gridx = 0;
        formGbc.gridy = 0;
        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(labelFont);
        formPanel.add(nameLabel, formGbc);

        formGbc.gridx = 1;
        formGbc.weightx = 1.0;
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        fullNameField = new JTextField(25);
        fullNameField.setFont(fieldFont);
        fullNameField.setPreferredSize(new Dimension(300, 35));
        formPanel.add(fullNameField, formGbc);

        // Age
        formGbc.gridx = 0;
        formGbc.gridy = 1;
        formGbc.weightx = 0;
        formGbc.fill = GridBagConstraints.NONE;
        JLabel ageLabel = new JLabel("Age:");
        ageLabel.setFont(labelFont);
        formPanel.add(ageLabel, formGbc);

        formGbc.gridx = 1;
        formGbc.weightx = 1.0;
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        ageField = new JTextField(25);
        ageField.setFont(fieldFont);
        ageField.setPreferredSize(new Dimension(300, 35));
        formPanel.add(ageField, formGbc);

        // CNIC
        formGbc.gridx = 0;
        formGbc.gridy = 2;
        formGbc.weightx = 0;
        formGbc.fill = GridBagConstraints.NONE;
        JLabel cnicLabel = new JLabel("CNIC (13 digits):");
        cnicLabel.setFont(labelFont);
        formPanel.add(cnicLabel, formGbc);

        formGbc.gridx = 1;
        formGbc.weightx = 1.0;
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        cnicField = new JTextField(25);
        cnicField.setFont(fieldFont);
        cnicField.setPreferredSize(new Dimension(300, 35));
        formPanel.add(cnicField, formGbc);

        // Account Type
        formGbc.gridx = 0;
        formGbc.gridy = 3;
        formGbc.weightx = 0;
        formGbc.fill = GridBagConstraints.NONE;
        JLabel typeLabel = new JLabel("Account Type:");
        typeLabel.setFont(labelFont);
        formPanel.add(typeLabel, formGbc);

        formGbc.gridx = 1;
        formGbc.weightx = 1.0;
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        String[] accountTypes = {"Standard", "VIP"};
        accountTypeCombo = new JComboBox<>(accountTypes);
        accountTypeCombo.setFont(fieldFont);
        accountTypeCombo.setPreferredSize(new Dimension(300, 35));
        accountTypeCombo.setBackground(Color.WHITE);
        formPanel.add(accountTypeCombo, formGbc);

        // Buttons Panel
        formGbc.gridx = 0;
        formGbc.gridy = 4;
        formGbc.gridwidth = 2;
        formGbc.anchor = GridBagConstraints.CENTER;
        formGbc.insets = new Insets(30, 10, 10, 10);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);

        createButton = createStyledButton("Create Account");
        backButton = createStyledButton("Back to Main");

        createButton.addActionListener(e -> handleCreateAccount());
        backButton.addActionListener(e -> goBackToMain());

        buttonPanel.add(createButton);
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

    private void handleCreateAccount() {
        // Get input values
        String fullName = fullNameField.getText().trim();
        String ageText = ageField.getText().trim();
        String cnic = cnicField.getText().trim();
        String accountType = (String) accountTypeCombo.getSelectedItem();

        // Validate inputs
        if (fullName.isEmpty()) {
            showError("Please enter full name.");
            return;
        }

        if (ageText.isEmpty()) {
            showError("Please enter age.");
            return;
        }

        // Validate age
        int age;
        try {
            age = Integer.parseInt(ageText);
            if (age < 18) {
                showError("Age must be 18 or above.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid age.");
            return;
        }

        // Validate CNIC
        if (cnic.isEmpty()) {
            showError("Please enter CNIC.");
            return;
        }

        if (!cnic.matches("\\d{13}")) {
            showError("CNIC must be exactly 13 digits.");
            return;
        }

        // Generate account number
        String accountNumber = generateAccountNumber();

        // Save to file
        if (saveAccountToFile(accountNumber, fullName, age, cnic, accountType)) {
            // Log to history
            ViewTransactionsHistoryForm.logHistory(
                    "Account Created",
                    accountNumber,
                    "-",
                    "-",
                    "0.00",
                    accountType,
                    "Completed",
                    String.format("New %s account created for %s", accountType, fullName)
            );

            // Show success dialog with account number
            showAccountCreated(accountNumber, fullName);

            // Clear form
            clearForm();
        } else {
            showError("Error saving account. Please try again.");
        }
    }

    private String generateAccountNumber() {
        Random random = new Random();
        StringBuilder accountNumber = new StringBuilder();

        // Generate 11 digit account number
        for (int i = 0; i < 11; i++) {
            accountNumber.append(random.nextInt(10));
        }

        return accountNumber.toString();
    }

    private boolean saveAccountToFile(String accountNumber, String fullName, int age, String cnic, String accountType) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ACCOUNTS_FILE, true))) {
            // Format: AccountNumber|FullName|Age|CNIC|AccountType|Balance
            String accountData = String.format("%s|%s|%d|%s|%s|0.00%n",
                    accountNumber, fullName, age, cnic, accountType);
            writer.write(accountData);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showAccountCreated(String accountNumber, String fullName) {
        JDialog dialog = new JDialog(this, "Account Created Successfully", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(this);

        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Success icon and message
        JLabel successLabel = new JLabel("✓ Account Created Successfully!");
        successLabel.setFont(new Font("Arial", Font.BOLD, 24));
        successLabel.setForeground(new Color(0, 150, 0));
        successLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel("Account Holder: " + fullName);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel accountLabel = new JLabel("Account Number: " + accountNumber);
        accountLabel.setFont(new Font("Arial", Font.BOLD, 20));
        accountLabel.setForeground(HEADER_COLOR);
        accountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel noteLabel = new JLabel("Please save this account number for future transactions.");
        noteLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        noteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(successLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(nameLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(accountLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
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

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    private void clearForm() {
        fullNameField.setText("");
        ageField.setText("");
        cnicField.setText("");
        accountTypeCombo.setSelectedIndex(0);
    }

    private void goBackToMain() {
        this.dispose();
        new RSBankMainPage();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CreateAccountForm();
        });
    }
}