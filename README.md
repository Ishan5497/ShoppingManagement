# Basic Shopping Application

A Java console application that uses JDBC and MySQL to manage products and customer orders.

## Features
- View all products and their prices/stock.
- Search for a product by ID.
- Purchase a product by ID and quantity.
- Check stock before purchase.
- Update stock and insert the order in one database transaction.
- View previous orders.

## Requirements
- JDK 17 or later
- MySQL Server
- IntelliJ IDEA
- Maven (dependency is configured in `pom.xml`)

## Database setup
1. Open MySQL Workbench or the MySQL command-line client.
2. Run `database/schema.sql`.
3. The script creates the `shopping` database, `products` and `orders` tables, and sample products.

## Configure credentials
Set these environment variables in your run configuration:
- `DB_PASSWORD` (required): your MySQL password
- `DB_USER` (optional, defaults to `root`)
- `DB_URL` (optional, defaults to `jdbc:mysql://localhost:3306/shopping`)

Do not put database passwords in source code or commit them to GitHub.

### IntelliJ
Open **Run → Edit Configurations...**, select `Shopping`, and add environment variables, for example:
`DB_PASSWORD=your_actual_password;DB_USER=root`

Use your own password locally.

## Run
1. Open this folder in IntelliJ IDEA.
2. Let IntelliJ import the Maven project and download dependencies.
3. Ensure MySQL is running.
4. Run `Shopping.main()`.
5. Use the menu to view, search, purchase, and review orders.
