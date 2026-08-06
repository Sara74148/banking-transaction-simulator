import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class RSBankMainPage extends JFrame {

    // Buttons
    private JButton createAccountBtn;
    private JButton viewPendingBtn;
    private JButton transferBtn;
    private JButton withdrawBtn;
    private JButton depositBtn;
    private JButton viewHistoryBtn;
    private JButton viewAccountDetailsBtn;

    // Colors
    private final Color HEADER_COLOR = new Color(139, 21, 56); // Maroon/Burgundy
    private final Color BACKGROUND_COLOR = new Color(197, 181, 229); // Light Purple
    private final Color BUTTON_COLOR = new Color(255, 255, 0); // Yellow
    private final Color BUTTON_HOVER_COLOR = new Color(255, 255, 102); // Light Yellow

    public RSBankMainPage() {
        // Set up the frame
        setTitle("RS Bank - Main Page");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen
        setLayout(new BorderLayout());

        // Set background color
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Create and add components
        createHeader();
        createButtons();

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
        JLabel headerLabel = new JLabel("Welcome to RS Bank");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 36));
        headerLabel.setForeground(Color.WHITE);

        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void createButtons() {
        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15); // Padding between buttons

        // Create buttons with styling
        createAccountBtn = createStyledButton("Create Account");
        viewPendingBtn = createStyledButton("View Pending Transactions");
        transferBtn = createStyledButton("Transfer");
        withdrawBtn = createStyledButton("Withdraw");
        depositBtn = createStyledButton("Deposit");
        viewHistoryBtn = createStyledButton("View Transactions History");
        viewAccountDetailsBtn = createStyledButton("View Account Details");

        // Add action listeners (placeholder methods)
        createAccountBtn.addActionListener(e -> openCreateAccount());
        viewPendingBtn.addActionListener(e -> openViewPending());
        transferBtn.addActionListener(e -> openTransfer());
        withdrawBtn.addActionListener(e -> openWithdraw());
        depositBtn.addActionListener(e -> openDeposit());
        viewHistoryBtn.addActionListener(e -> openViewHistory());
        viewAccountDetailsBtn.addActionListener(e -> openViewAccountDetails());

        // Row 1 - First three buttons
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(createAccountBtn, gbc);

        gbc.gridx = 1;
        contentPanel.add(viewPendingBtn, gbc);

        gbc.gridx = 2;
        contentPanel.add(transferBtn, gbc);

        // Row 2 - Next three buttons
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(withdrawBtn, gbc);

        gbc.gridx = 1;
        contentPanel.add(depositBtn, gbc);

        gbc.gridx = 2;
        contentPanel.add(viewHistoryBtn, gbc);

        // Row 3 - View Account Details button (centered)
        gbc.gridx = 1;
        gbc.gridy = 2;
        contentPanel.add(viewAccountDetailsBtn, gbc);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);

        // Button styling
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.BLACK);
        button.setPreferredSize(new Dimension(300, 70));
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

    // Placeholder methods for button actions
    // These will be connected to actual forms later
    private void openCreateAccount() {
        System.out.println("Create Account button clicked");
        this.dispose(); // Close main page
        new CreateAccountForm(); // Open create account form
    }

    private void openViewPending() {
        System.out.println("View Pending Transactions button clicked");
        this.dispose(); // Close main page
        new ViewPendingTransactionsForm(); // Open view pending transactions form
    }

    private void openTransfer() {
        System.out.println("Transfer button clicked");
        this.dispose(); // Close main page
        new TransferForm(); // Open transfer form
    }

    private void openWithdraw() {
        System.out.println("Withdraw button clicked");
        this.dispose(); // Close main page
        new WithdrawForm(); // Open withdraw form
    }

    private void openDeposit() {
        System.out.println("Deposit button clicked");
        this.dispose(); // Close main page
        new DepositForm(); // Open deposit form
    }

    private void openViewHistory() {
        System.out.println("View Transactions History button clicked");
        this.dispose(); // Close main page
        new ViewTransactionsHistoryForm(); // Open view history form
    }

    private void openViewAccountDetails() {
        System.out.println("View Account Details button clicked");
        this.dispose(); // Close main page
        new ViewAccDetails(); // Open view account details form
    }

    public static void main(String[] args) {
        // Use SwingUtilities to ensure thread safety
        SwingUtilities.invokeLater(() -> {
            new RSBankMainPage();
        });
    }
}