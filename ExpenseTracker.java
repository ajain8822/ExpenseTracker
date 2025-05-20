import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

class Transaction {
    private String type;
    private String category;
    private double amount;
    private LocalDate date;

    public Transaction(String type, String category, double amount, LocalDate date) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    public String getType() { return type; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
}

public class ExpenseTracker {
    private static List<Transaction> transactions = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final List<String> incomeCategories = Arrays.asList("Salary", "Business");
    private static final List<String> expenseCategories = Arrays.asList("Food", "Rent", "Travel");

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n1. Add Transaction");
            System.out.println("2. Load from File");
            System.out.println("3. View Monthly Summary");
            System.out.println("4. Exit");
            System.out.print("Choose: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1": addTransaction(); break;
                case "2": loadFromFile(); break;
                case "3": showMonthlySummary(); break;
                case "4": System.out.println("Exiting..."); return;
                default: System.out.println("Invalid option.");
            }
        }
    }

    private static void addTransaction() {
        System.out.print("Enter type (INCOME/EXPENSE): ");
        String type = scanner.nextLine().trim().toUpperCase();

        if (!type.equals("INCOME") && !type.equals("EXPENSE")) {
            System.out.println("Invalid type.");
            return;
        }

        String category = null;
        if (type.equals("INCOME")) {
            category = chooseCategory(incomeCategories, "INCOME");
        } else {
            category = chooseCategory(expenseCategories, "EXPENSE");
        }

        double amount = 0;
        while (true) {
            System.out.print("Enter amount: ");
            String amtStr = scanner.nextLine().trim();
            try {
                amount = Double.parseDouble(amtStr);
                if (amount <= 0) {
                    System.out.println("Amount must be greater than zero.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please enter a valid number.");
            }
        }

        LocalDate date = null;
        while (true) {
            System.out.print("Enter date (yyyy-MM-dd): ");
            String dateStr = scanner.nextLine().trim();
            if (dateStr.isEmpty()) {
                System.out.println("Date cannot be empty.");
                continue;
            }
            try {
                date = LocalDate.parse(dateStr, formatter);
                break;
            } catch (Exception e) {
                System.out.println("Invalid date format. Please use yyyy-MM-dd.");
            }
        }

        transactions.add(new Transaction(type, category, amount, date));
        System.out.println("Transaction added successfully!");
    }

    private static String chooseCategory(List<String> validCategories, String typeLabel) {
        while (true) {
            System.out.println("Choose category for " + typeLabel + ":");
            for (int i = 0; i < validCategories.size(); i++) {
                System.out.println((i + 1) + ". " + validCategories.get(i));
            }
            System.out.print("Select option (1-" + validCategories.size() + "): ");
            String input = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= validCategories.size()) {
                    return validCategories.get(choice - 1);
                } else {
                    System.out.println("Invalid option. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    private static void loadFromFile() {
        System.out.print("Enter file path: ");
        String path = scanner.nextLine().trim();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 4) continue;

                String type = parts[0].trim().toUpperCase();
                String category = parts[1].trim();
                double amount;
                LocalDate date;

                try {
                    amount = Double.parseDouble(parts[2].trim());
                    date = LocalDate.parse(parts[3].trim(), formatter);
                } catch (Exception e) {
                    System.out.println("Skipping invalid entry: " + line);
                    continue;
                }

                transactions.add(new Transaction(type, category, amount, date));
            }
            System.out.println("File loaded successfully.");
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    private static void showMonthlySummary() {
        Map<String, Double> incomeMap = new HashMap<>();
        Map<String, Double> expenseMap = new HashMap<>();

        for (Transaction t : transactions) {
            String key = t.getDate().getYear() + "-" + String.format("%02d", t.getDate().getMonthValue());

            if ("INCOME".equals(t.getType())) {
                incomeMap.put(key, incomeMap.getOrDefault(key, 0.0) + t.getAmount());
            } else {
                expenseMap.put(key, expenseMap.getOrDefault(key, 0.0) + t.getAmount());
            }
        }

        Set<String> allMonths = new TreeSet<>();
        allMonths.addAll(incomeMap.keySet());
        allMonths.addAll(expenseMap.keySet());

        if (allMonths.isEmpty()) {
            System.out.println("No transactions to summarize.");
            return;
        }

        for (String month : allMonths) {
            double income = incomeMap.getOrDefault(month, 0.0);
            double expense = expenseMap.getOrDefault(month, 0.0);
            System.out.println("\nSummary for " + month);
            System.out.printf("  Total Income : %.2f\n", income);
            System.out.printf("  Total Expense: %.2f\n", expense);
            System.out.printf("  Savings      : %.2f\n", income - expense);
        }
    }
}
