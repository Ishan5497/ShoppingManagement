import java.sql.*;
import java.util.Scanner;

public class Shopping {
    private static final String URL =
            System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/shopping");
    private static final String USER =
            System.getenv().getOrDefault("DB_USER", "root");
    // Set DB_PASSWORD in your environment. Never commit database passwords.
    private static final String PASS = System.getenv("DB_PASSWORD");

    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        if (PASS == null || PASS.isBlank()) {
            System.out.println("Database error: DB_PASSWORD environment variable is not set.");
            return;
        }

        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            System.out.println("Connected to shopping database.");

            while (true) {
                System.out.println("\\n===== SHOPPING APPLICATION =====");
                System.out.println("1. View Products");
                System.out.println("2. Search Product");
                System.out.println("3. Purchase Product");
                System.out.println("4. View Orders");
                System.out.println("5. Exit");
                System.out.print("Enter choice: ");

                int choice;
                try {
                    choice = Integer.parseInt(sc.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a number from 1 to 5.");
                    continue;
                }

                try {
                    switch (choice) {
                        case 1 -> viewProducts(con);
                        case 2 -> searchProduct(con);
                        case 3 -> purchaseProduct(con);
                        case 4 -> viewOrders(con);
                        case 5 -> {
                            System.out.println("Thank you for shopping!");
                            return;
                        }
                        default -> System.out.println("Invalid choice.");
                    }
                } catch (SQLException e) {
                    System.out.println("Database operation failed: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            System.out.println("Check MySQL, database setup, and DB_* environment variables.");
        }
    }

    static void viewProducts(Connection con) throws SQLException {
        String sql = "SELECT product_id, product_name, price, stock FROM products ORDER BY product_id";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.println("\\nID\\tProduct\\t\\tPrice (Rs.)\\tStock");
            boolean found = false;

            while (rs.next()) {
                found = true;
                System.out.printf("%d\\t%s\\t\\t%.2f\\t\\t%d%n",
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getDouble("price"),
                        rs.getInt("stock"));
            }
            if (!found) {
                System.out.println("No products available.");
            }
        }
    }

    static void searchProduct(Connection con) throws SQLException {
        System.out.print("Enter product ID: ");
        int id;
        try {
            id = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid product ID.");
            return;
        }

        String sql = "SELECT product_id, product_name, price, stock FROM products WHERE product_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Product: " + rs.getString("product_name"));
                    System.out.printf("Price: Rs. %.2f%n", rs.getDouble("price"));
                    System.out.println("Stock: " + rs.getInt("stock"));
                } else {
                    System.out.println("Product not found.");
                }
            }
        }
    }

    static void purchaseProduct(Connection con) throws SQLException {
        System.out.print("Enter product ID: ");
        int id;
        System.out.print("Enter quantity: ");
        int qty;

        try {
            id = Integer.parseInt(sc.nextLine().trim());
            qty = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Product ID and quantity must be integers.");
            return;
        }

        if (qty <= 0) {
            System.out.println("Quantity must be positive.");
            return;
        }

        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);

        try {
            String sql = "SELECT product_name, price, stock FROM products WHERE product_id = ? FOR UPDATE";
            String name;
            double price;
            int stock;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Product not found.");
                        con.rollback();
                        return;
                    }
                    name = rs.getString("product_name");
                    price = rs.getDouble("price");
                    stock = rs.getInt("stock");
                }
            }

            if (qty > stock) {
                System.out.println("Insufficient stock. Available stock: " + stock);
                con.rollback();
                return;
            }

            double total = price * qty;

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE products SET stock = stock - ? WHERE product_id = ?")) {
                ps.setInt(1, qty);
                ps.setInt(2, id);
                ps.executeUpdate();
            }

            String insert = "INSERT INTO orders (product_id, quantity, total_amount) VALUES (?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, id);
                ps.setInt(2, qty);
                ps.setDouble(3, total);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        System.out.println("Order ID: " + keys.getInt(1));
                    }
                }
            }

            con.commit();
            System.out.println("\\nPurchase successful!");
            System.out.println("Product: " + name);
            System.out.println("Quantity: " + qty);
            System.out.printf("Total bill: Rs. %.2f%n", total);
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }

    static void viewOrders(Connection con) throws SQLException {
        String sql = """
                SELECT o.order_id, p.product_name, o.quantity,
                       o.total_amount, o.order_date
                FROM orders o
                JOIN products p ON o.product_id = p.product_id
                ORDER BY o.order_id
                """;

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.println("\\nOrder ID\\tProduct\\t\tQuantity\\tTotal (Rs.)\\tOrder Date");
            boolean found = false;

            while (rs.next()) {
                found = true;
                System.out.printf("%d\\t\\t%s\\t\\t%d\\t\\t%.2f\\t\\t%s%n",
                        rs.getInt("order_id"),
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("total_amount"),
                        rs.getTimestamp("order_date"));
            }
            if (!found) {
                System.out.println("No orders found.");
            }
        }
    }
}
