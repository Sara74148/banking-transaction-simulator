import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class BankingTransactionSystem {

    // File paths
    private static final String ACCOUNTS_FILE = "accounts.txt";
    private static final String PENDING_TRANSACTIONS_FILE = "pending_transactions.txt";
    private static final String HISTORY_FILE = "transaction_history.txt";

    // Data structures
    private Map<String, Account> accounts;
    private CustomPriorityQueue<Transaction> pendingTransactions;
    private CustomLinkedList<HistoryRecord> transactionHistory;

    // Singleton instance
    private static BankingTransactionSystem instance;

    private BankingTransactionSystem() {
        accounts = new HashMap<>();
        pendingTransactions = new CustomPriorityQueue<>(new TransactionComparator());
        transactionHistory = new CustomLinkedList<>();
        loadAllData();
    }


    public static BankingTransactionSystem getInstance() {
        if (instance == null) {
            instance = new BankingTransactionSystem();
        }
        return instance;
    }

    // ==================== ACCOUNT MANAGEMENT ====================


    public String createAccount(String fullName, int age, String cnic, String accountType) throws Exception {
        // Validate inputs
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new Exception("Full name is required");
        }
        if (age < 18) {
            throw new Exception("Age must be 18 or above");
        }
        if (cnic == null || !cnic.matches("\\d{13}")) {
            throw new Exception("CNIC must be exactly 13 digits");
        }

        // Generate account number
        String accountNumber = generateAccountNumber();

        // Create account object
        Account account = new Account(accountNumber, fullName, age, cnic, accountType, 0.0);
        accounts.put(accountNumber, account);

        // Save to file
        saveAccountsToFile();

        // Log to history
        logHistory("Account Created", accountNumber, "-", "-", "0.00", accountType,
                "Completed", String.format("New %s account created for %s", accountType, fullName));

        return accountNumber;
    }

    public Account getAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }
    public boolean accountExists(String accountNumber) {
        return accounts.containsKey(accountNumber);
    }
    public Map<String, Account> getAllAccounts() {
        return new HashMap<>(accounts);
    }
    private String generateAccountNumber() {
        Random random = new Random();
        StringBuilder accountNumber = new StringBuilder();

        for (int i = 0; i < 11; i++) {
            accountNumber.append(random.nextInt(10));
        }

        return accountNumber.toString();
    }
    public void queueWithdraw(String accountNumber, double amount) throws Exception {
        // Validate
        if (!accountExists(accountNumber)) {
            throw new Exception("Account does not exist");
        }
        if (amount <= 0) {
            throw new Exception("Amount must be greater than 0");
        }

        Account account = getAccount(accountNumber);
        if (account.getBalance() < amount) {
            throw new Exception(String.format("Insufficient balance. Available: %.2f", account.getBalance()));
        }

        // Create transaction
        Transaction transaction = new Transaction(
                getNextTransactionId(),
                "Withdraw",
                accountNumber,
                "-",
                amount,
                account.getAccountType()
        );

        // Add to queue
        pendingTransactions.add(transaction);

        // Save to file
        savePendingTransactionsToFile();

        // Log to history
        logHistory("Transaction Queued", accountNumber, accountNumber, "-",
                String.format("%.2f", amount), account.getAccountType(), "Queued",
                String.format("Withdrawal queued from account %s", accountNumber));
    }


    public void queueDeposit(String accountNumber, double amount) throws Exception {
        // Validate
        if (!accountExists(accountNumber)) {
            throw new Exception("Account does not exist");
        }
        if (amount <= 0) {
            throw new Exception("Amount must be greater than 0");
        }

        Account account = getAccount(accountNumber);

        // Create transaction
        Transaction transaction = new Transaction(
                getNextTransactionId(),
                "Deposit",
                "-",
                accountNumber,
                amount,
                account.getAccountType()
        );

        // Add to queue
        pendingTransactions.add(transaction);

        // Save to file
        savePendingTransactionsToFile();

        // Log to history
        logHistory("Transaction Queued", accountNumber, "-", accountNumber,
                String.format("%.2f", amount), account.getAccountType(), "Queued",
                String.format("Deposit queued to account %s", accountNumber));
    }


    public void queueTransfer(String fromAccount, String toAccount, double amount) throws Exception {
        // Validate
        if (!accountExists(fromAccount)) {
            throw new Exception("Source account does not exist");
        }
        if (!accountExists(toAccount)) {
            throw new Exception("Destination account does not exist");
        }
        if (fromAccount.equals(toAccount)) {
            throw new Exception("Cannot transfer to the same account");
        }
        if (amount <= 0) {
            throw new Exception("Amount must be greater than 0");
        }

        Account sourceAccount = getAccount(fromAccount);
        if (sourceAccount.getBalance() < amount) {
            throw new Exception(String.format("Insufficient balance. Available: %.2f", sourceAccount.getBalance()));
        }
        // Create transaction
        Transaction transaction = new Transaction(
                getNextTransactionId(),
                "Transfer",
                fromAccount,
                toAccount,
                amount,
                sourceAccount.getAccountType()
        );

        // Add to queue
        pendingTransactions.add(transaction);

        // Save to file
        savePendingTransactionsToFile();

        // Log to history
        logHistory("Transaction Queued", fromAccount, fromAccount, toAccount,
                String.format("%.2f", amount), sourceAccount.getAccountType(), "Queued",
                String.format("Transfer queued from %s to %s", fromAccount, toAccount));
    }


    public Transaction processNextTransaction() throws Exception {
        if (pendingTransactions.isEmpty()) {
            throw new Exception("No pending transactions");
        }

        Transaction transaction = pendingTransactions.poll();
        executeTransaction(transaction);

        // Save updated data
        saveAccountsToFile();
        savePendingTransactionsToFile();

        // Log to history
        String accountNumber = transaction.getFromAccount().equals("-") ?
                transaction.getToAccount() : transaction.getFromAccount();
        logHistory(transaction.getType() + " Processed", accountNumber,
                transaction.getFromAccount(), transaction.getToAccount(),
                String.format("%.2f", transaction.getAmount()),
                transaction.getAccountType(), "Completed",
                String.format("%s transaction processed successfully", transaction.getType()));

        return transaction;
    }

    public int processAllTransactions() {
        int successCount = 0;

        while (!pendingTransactions.isEmpty()) {
            try {
                processNextTransaction();
                successCount++;
            } catch (Exception e) {
                // Skip failed transactions
            }
        }

        return successCount;
    }


    private void executeTransaction(Transaction transaction) throws Exception {
        String type = transaction.getType().toLowerCase();

        switch (type) {
            case "withdraw":
                Account withdrawAccount = accounts.get(transaction.getFromAccount());
                if (withdrawAccount.getBalance() < transaction.getAmount()) {
                    throw new Exception("Insufficient balance");
                }
                withdrawAccount.setBalance(withdrawAccount.getBalance() - transaction.getAmount());
                break;

            case "deposit":
                Account depositAccount = accounts.get(transaction.getToAccount());
                depositAccount.setBalance(depositAccount.getBalance() + transaction.getAmount());
                break;

            case "transfer":
                Account fromAcc = accounts.get(transaction.getFromAccount());
                Account toAcc = accounts.get(transaction.getToAccount());

                if (fromAcc.getBalance() < transaction.getAmount()) {
                    throw new Exception("Insufficient balance");
                }

                fromAcc.setBalance(fromAcc.getBalance() - transaction.getAmount());
                toAcc.setBalance(toAcc.getBalance() + transaction.getAmount());
                break;

            default:
                throw new Exception("Unknown transaction type");
        }
    }

    public void undoTransaction(Transaction transaction) throws Exception {
        String type = transaction.getType().toLowerCase();

        switch (type) {
            case "withdraw":
                // Reverse: Add money back
                Account withdrawAccount = accounts.get(transaction.getFromAccount());
                withdrawAccount.setBalance(withdrawAccount.getBalance() + transaction.getAmount());
                break;

            case "deposit":
                // Reverse: Remove money
                Account depositAccount = accounts.get(transaction.getToAccount());
                if (depositAccount.getBalance() < transaction.getAmount()) {
                    throw new Exception("Cannot undo. Insufficient balance in account");
                }
                depositAccount.setBalance(depositAccount.getBalance() - transaction.getAmount());
                break;

            case "transfer":
                // Reverse: Transfer back
                Account fromAcc = accounts.get(transaction.getFromAccount());
                Account toAcc = accounts.get(transaction.getToAccount());

                if (toAcc.getBalance() < transaction.getAmount()) {
                    throw new Exception("Cannot undo. Insufficient balance in destination account");
                }

                toAcc.setBalance(toAcc.getBalance() - transaction.getAmount());
                fromAcc.setBalance(fromAcc.getBalance() + transaction.getAmount());
                break;

            default:
                throw new Exception("Unknown transaction type");
        }

        // Re-add to queue
        pendingTransactions.add(transaction);

        // Save updated data
        saveAccountsToFile();
        savePendingTransactionsToFile();

        // Log to history
        String accountNumber = transaction.getFromAccount().equals("-") ?
                transaction.getToAccount() : transaction.getFromAccount();
        logHistory("Transaction Undone", accountNumber,
                transaction.getFromAccount(), transaction.getToAccount(),
                String.format("%.2f", transaction.getAmount()),
                transaction.getAccountType(), "Reversed",
                String.format("%s transaction reversed successfully", transaction.getType()));
    }


    public CustomPriorityQueue<Transaction> getPendingTransactions() {
        return new CustomPriorityQueue<>(pendingTransactions);
    }


    public int getPendingTransactionCount() {
        return pendingTransactions.size();
    }

    private int getNextTransactionId() {
        return pendingTransactions.size() + 1;
    }


    public void logHistory(String activityType, String accountNumber, String fromAccount,
                           String toAccount, String amount, String accountType,
                           String status, String details) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = sdf.format(new Date());

        HistoryRecord record = new HistoryRecord(timestamp, activityType, accountNumber,
                fromAccount, toAccount, amount,
                accountType, status, details);

        transactionHistory.add(record);
        saveHistoryToFile(record);
    }

    public CustomLinkedList<HistoryRecord> getTransactionHistory() {
        return new CustomLinkedList<>(transactionHistory);
    }


    public int getHistoryCount() {
        return transactionHistory.size();
    }

    private void loadAllData() {
        loadAccountsFromFile();
        loadPendingTransactionsFromFile();
        loadHistoryFromFile();
    }


    private void loadAccountsFromFile() {
        accounts.clear();

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

                    Account account = new Account(accountNumber, fullName, age, cnic, accountType, balance);
                    accounts.put(accountNumber, account);
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveAccountsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ACCOUNTS_FILE))) {
            for (Account account : accounts.values()) {
                String accountData = String.format("%s|%s|%d|%s|%s|%.2f%n",
                        account.getAccountNumber(), account.getFullName(), account.getAge(),
                        account.getCnic(), account.getAccountType(), account.getBalance());
                writer.write(accountData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadPendingTransactionsFromFile() {
        pendingTransactions.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(PENDING_TRANSACTIONS_FILE))) {
            String line;
            int id = 1;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    String type = parts[0];
                    String fromAccount = parts[1];
                    String toAccount = parts[2];
                    double amount = Double.parseDouble(parts[3]);
                    String accountType = parts[4];

                    Transaction transaction = new Transaction(id++, type, fromAccount,
                            toAccount, amount, accountType);
                    pendingTransactions.offer(transaction);
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void savePendingTransactionsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PENDING_TRANSACTIONS_FILE))) {
            // Convert priority queue to list to iterate without removing
            CustomLinkedList<Transaction> temp = pendingTransactions.toList();
            for (int i = 0; i < temp.size(); i++) {
                Transaction transaction = temp.get(i);
                String transactionData = String.format("%s|%s|%s|%.2f|%s%n",
                        transaction.getType(), transaction.getFromAccount(),
                        transaction.getToAccount(), transaction.getAmount(),
                        transaction.getAccountType());
                writer.write(transactionData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadHistoryFromFile() {
        transactionHistory.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(HISTORY_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 9) {
                    HistoryRecord record = new HistoryRecord(
                            parts[0], parts[1], parts[2], parts[3],
                            parts[4], parts[5], parts[6], parts[7], parts[8]
                    );
                    transactionHistory.add(record);
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void saveHistoryToFile(HistoryRecord record) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE, true))) {
            String historyEntry = String.format("%s|%s|%s|%s|%s|%s|%s|%s|%s%n",
                    record.getTimestamp(), record.getActivityType(), record.getAccountNumber(),
                    record.getFromAccount(), record.getToAccount(), record.getAmount(),
                    record.getAccountType(), record.getStatus(), record.getDetails());
            writer.write(historyEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --------------------data structures jo ham nay add kiye hain----
    public static class CustomLinkedList<T> {
        private Node<T> head;
        private Node<T> tail;
        private int size;
        private Node<T> current; // Built-in iterator pointer

        public T get(int i) {
                    return null;
        }

        private static class Node<T> {
            T data;
            Node<T> next;
            Node<T> prev;

            Node(T data) {
                this.data = data;
                this.next = null;
                this.prev = null;
            }
        }

        public CustomLinkedList() {
            this.head = null;
            this.tail = null;
            this.size = 0;
            this.current = null;
        }

        // Copy constructor
        public CustomLinkedList(CustomLinkedList<T> other) {
            this.head = null;
            this.tail = null;
            this.size = 0;
            this.current = null;

            Node<T> current = other.head;
            while (current != null) {
                this.add(current.data);
                current = current.next;
            }
        }

//add element
        public void add(T data) {
            Node<T> newNode = new Node<>(data);

            if (head == null) {
                head = tail = newNode;
            } else {
                tail.next = newNode;
                newNode.prev = tail;
                tail = newNode;
            }
            size++;
        }

        public void add(int index, T data) {
            if (index < 0 || index > size) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
            }

            if (index == size) {
                add(data);
                return;
            }

            Node<T> newNode = new Node<>(data);

            if (index == 0) {
                newNode.next = head;
                if (head != null) {
                    head.prev = newNode;
                }
                head = newNode;
                if (tail == null) {
                    tail = newNode;
                }
            } else {
                Node<T> current = head;
                for (int i = 0; i < index; i++) {
                    current = current.next;
                }

                newNode.next = current;
                newNode.prev = current.prev;

                if (current.prev != null) {
                    current.prev.next = newNode;
                }
                current.prev = newNode;
            }
            size++;
        }
        //size
        public int size() {
            return size;
        }


    //empty check
        public boolean isEmpty() {
            return size == 0;
        }

        public void clear() {
            head = null;
            tail = null;
            size = 0;
            current = null;
        }


//       // public void reset() {
//            current = head;
//        }

        public boolean hasNext() {
            return current != null;
        }

        public T next() {
            if (current == null) {
                throw new RuntimeException("No more elements in iteration");
            }
            T data = current.data;
            current = current.next;
            return data;
        }
        public T current() {
            if (current == null) {
                throw new RuntimeException("Iterator not initialized or no more elements");
            }
            return current.data;
        }
        public T previous() {
            if (current == null || current.prev == null) {
                throw new RuntimeException("No previous element");
            }
            current = current.prev;
            return current.data;
        }

        public void reset() {
            current = tail;
        }
        public boolean hasPrevious() {
            return current != null && current.prev != null;
        }
    }


    public static class CustomPriorityQueue<T> {
        private CustomLinkedList<T> list;
        private Comparator<T> comparator;

        public CustomPriorityQueue(Comparator<T> comparator) {
            this.list = new CustomLinkedList<>();
            this.comparator = comparator;
        }

        // Copy constructor
        public CustomPriorityQueue(CustomPriorityQueue<T> other) {
            this.list = new CustomLinkedList<>();
            this.comparator = other.comparator;

            // Copy all elements in order
            for (int i = 0; i < other.list.size(); i++) {
                this.list.add(other.list.get(i));
            }
        }


        public void add(T element) {
            // If list is empty, just add
            if (list.isEmpty()) {
                list.add(element);
                return;
            }

            // Find the correct position to insert based on priority
            int position = 0;
            for (int i = 0; i < list.size(); i++) {
                if (comparator.compare(element, list.get(i)) < 0) {
                    // Element has higher priority, insert here
                    position = i;
                    break;
                }
                position = i + 1;
            }

            // Insert at the found position
            if (position >= list.size()) {
                list.add(element);
            } else {
                list.add(position, element);
            }
        }


        public void offer(T element) {
            add(element);
        }


        public T poll() {
            if (isEmpty()) {
                return null;
            }

            T element = list.get(0);

            return element;
        }


        public T peek() {
            if (isEmpty()) {
                return null;
            }
            return list.get(0);
        }


        public boolean isEmpty() {
            return list.isEmpty();
        }


        public int size() {
            return list.size();
        }

        public void clear() {
            list.clear();
        }


        public CustomLinkedList<T> toList() {
            return new CustomLinkedList<>(list);
        }
    }



    public static class Account {
        private String accountNumber;
        private String fullName;
        private int age;
        private String cnic;
        private String accountType;
        private double balance;

        public Account(String accountNumber, String fullName, int age, String cnic,
                       String accountType, double balance) {
            this.accountNumber = accountNumber;
            this.fullName = fullName;
            this.age = age;
            this.cnic = cnic;
            this.accountType = accountType;
            this.balance = balance;
        }

        // Getters
        public String getAccountNumber() { return accountNumber; }
        public String getFullName() { return fullName; }
        public int getAge() { return age; }
        public String getCnic() { return cnic; }
        public String getAccountType() { return accountType; }
        public double getBalance() { return balance; }

        // Setter for balance
        public void setBalance(double balance) { this.balance = balance; }
    }


    public static class Transaction {
        private int id;
        private String type;
        private String fromAccount;
        private String toAccount;
        private double amount;
        private String accountType;

        public Transaction(int id, String type, String fromAccount, String toAccount,
                           double amount, String accountType) {
            this.id = id;
            this.type = type;
            this.fromAccount = fromAccount;
            this.toAccount = toAccount;
            this.amount = amount;
            this.accountType = accountType;
        }

        // Getters
        public int getId() { return id; }
        public String getType() { return type; }
        public String getFromAccount() { return fromAccount; }
        public String getToAccount() { return toAccount; }
        public double getAmount() { return amount; }
        public String getAccountType() { return accountType; }
    }



    public static class HistoryRecord {
        private String timestamp;
        private String activityType;
        private String accountNumber;
        private String fromAccount;
        private String toAccount;
        private String amount;
        private String accountType;
        private String status;
        private String details;

        public HistoryRecord(String timestamp, String activityType, String accountNumber,
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

        // Getters
        public String getTimestamp() { return timestamp; }
        public String getActivityType() { return activityType; }
        public String getAccountNumber() { return accountNumber; }
        public String getFromAccount() { return fromAccount; }
        public String getToAccount() { return toAccount; }
        public String getAmount() { return amount; }
        public String getAccountType() { return accountType; }
        public String getStatus() { return status; }
        public String getDetails() { return details; }
    }


    private static class TransactionComparator implements Comparator<Transaction> {
        @Override
        public int compare(Transaction t1, Transaction t2) {
            // VIP accounts have higher priority (return negative for higher priority)
            if (t1.getAccountType().equalsIgnoreCase("VIP") &&
                    !t2.getAccountType().equalsIgnoreCase("VIP")) {
                return -1; // t1 has higher priority
            } else if (!t1.getAccountType().equalsIgnoreCase("VIP") &&
                    t2.getAccountType().equalsIgnoreCase("VIP")) {
                return 1; // t2 has higher priority
            } else {
                // Same priority level, maintain FIFO order by ID
                return Integer.compare(t1.getId(), t2.getId());
            }
        }
    }
}