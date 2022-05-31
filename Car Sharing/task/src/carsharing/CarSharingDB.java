package carsharing;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class CarSharingDB {

    private static boolean EXIT = false;
    private final String DB_URL;
    private Statement SMT;

    public CarSharingDB(String dbFile) {
        String dbPath = "./src/carsharing/db/" + dbFile;
        this.DB_URL = "jdbc:h2:" + dbPath;
        initDatabase();
    }

    private void initDatabase() {
        try (
                Connection connection = DriverManager.getConnection(DB_URL);
                Statement statement = connection.createStatement()
        ){
            this.SMT = statement;
            String jdbcDriver = "org.h2.Driver";
            Class.forName(jdbcDriver);
            connection.setAutoCommit(true);
            //eraseTables();
            createTables();
            menu();
        } catch(SQLException | ClassNotFoundException se) {
            se.printStackTrace();
        }
    }

    private void executeStatement(String command) {
        try {
            SMT.executeUpdate(command);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void menu() {
        Scanner scanner = new Scanner(System.in);
        while (!EXIT) {
            System.out.println(
                    "1. Log in as a manager\n" +
                            "2. Log in as a customer\n" +
                            "3. Create a customer\n" +
                            "0. Exit");
            String input = scanner.nextLine();
            if ("0".equals(input)) {
                EXIT = true;
            } else {
                if ("1".equals(input)) {
                    adminMenu();
                } else if ("2".equals(input)) {
                    selectCustomer();
                } else if ("3".equals(input)) {
                    addCustomer();
                }
                System.out.println();
            }
        }
    }

    private void adminMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println(
                    "\n1. Company list\n" +
                            "2. Create a company\n" +
                            "0. Back");
            String input = scanner.nextLine();
            System.out.println();
            if ("1".equals(input)) {
                int selectedCompany = selectCompany();
                if (selectedCompany == 0) {
                    continue;
                } else {
                    if (selectedCompany != -1) {
                        companyMenu(selectedCompany);
                    }
                }
            } else if ("2".equals(input)) {
                addCompany();
            } else if ("0".equals(input)) {
                break;
            }
        }

    }

    private void selectCustomer() {
        try {
            Scanner scanner = new Scanner(System.in);
            String sql = "SELECT * FROM CUSTOMER";
            ResultSet resultSet = SMT.executeQuery(sql);
            int customerCount = 0;
            System.out.println();
            if (!resultSet.next()) {
                System.out.println("The customer list is empty!");
            } else {
                do {
                    int id = resultSet.getInt("ID");
                    customerCount++;
                    String name = resultSet.getString("NAME");
                    System.out.printf("%d. %s\n", id, name);
                } while (resultSet.next());
                System.out.println("0. Back");
                String input;
                while (true) {
                    input = scanner.nextLine();
                    if ("0".equals(input)) {
                        return;
                    } else if (input.matches("\\d")
                            && Integer.parseInt(input) > 0
                            && Integer.parseInt(input) <= customerCount) {
                        break;
                    } else {
                        System.out.println("Invalid Input");
                    }
                }
                int option = Integer.parseInt(input);
                System.out.println();
                customerMenu(option);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void customerMenu(int customerID) {
        String noRentedCar = "You didn't rent a car!\n";
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println(
                    "1. Rent a car\n" +
                            "2. Return a rented car\n" +
                            "3. My rented car\n" +
                            "0. Back");
            String input = scanner.nextLine();
            System.out.println();
            if ("1".equals(input)) {
                if (!customerHasRentedCar(customerID)) {
                    int companyID = selectCompany();
                    if (companyID != -1) {
                        rentAvailableCar(companyID, customerID);
                    }
                } else {
                    System.out.println("You've already rented a car!\n");
                }
            } else if ("2".equals(input)) {
                if (customerHasRentedCar(customerID)) {
                    customerReturnCar(customerID);
                } else {
                    System.out.println(noRentedCar);
                }
            } else if ("3".equals(input)) {
                if (customerHasRentedCar(customerID)) {
                    customerShowRentedCar(customerID);
                } else {
                    System.out.println(noRentedCar);
                }
            } else if ("0".equals(input)) {
                break;
            }
        }
    }

    private boolean customerHasRentedCar(int customerID) {
        try {
            String customerRentedCarCommand = String.format(
                    "SELECT * FROM CUSTOMER WHERE ID = %d", customerID);
            ResultSet rentalCarResultSet = SMT.executeQuery(customerRentedCarCommand);
            rentalCarResultSet.next();
            return rentalCarResultSet.getInt("RENTED_CAR_ID") != 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void customerShowRentedCar(int customerID) {
        try {
            String getRentalCar = String.format(
                    "SELECT * FROM CAR " +
                            "WHERE ID = (" +
                            "SELECT RENTED_CAR_ID FROM CUSTOMER WHERE ID = %d)", customerID);
            ResultSet rentalCarResultSet = SMT.executeQuery(getRentalCar);

            if (!rentalCarResultSet.next()) {
                System.out.println("You didn't rent a car!");
            } else {
                String carName = rentalCarResultSet.getString("NAME");
                String getRentalCarCompany = String.format(
                        "SELECT NAME FROM COMPANY " +
                                "WHERE ID = %s", rentalCarResultSet.getInt("COMPANY_ID"));
                ResultSet rentalCarCompanyResultSet = SMT.executeQuery(getRentalCarCompany);
                rentalCarCompanyResultSet.next();
                String companyName = rentalCarCompanyResultSet.getString("NAME");
                System.out.printf("Your rented car:\n" +
                        "%s\n" +
                        "Company:\n" +
                        "%s\n\n", carName, companyName);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void customerReturnCar(int customerID) {
        executeStatement(String.format("UPDATE CUSTOMER " +
                "SET RENTED_CAR_ID = NULL " +
                "WHERE ID = %d", customerID));
        System.out.println("You've returned a rented car!\n");
    }

    private void companyMenu(int companyID) {
        if (companyID == 0) {
            return;
        }
        Scanner scanner = new Scanner(System.in);
        try {
            String sql = String.format(
                    "SELECT NAME " +
                            "FROM COMPANY " +
                            "WHERE ID = %d;", companyID);
            ResultSet resultSet = SMT.executeQuery(sql);
            resultSet.next();
            String companyName = resultSet.getString("NAME");
            System.out.printf("\n'%s' company\n", companyName);
            while (true) {
                System.out.println(
                        "1. Car list\n" +
                                "2. Create a car\n" +
                                "0. Back");
                String input = scanner.nextLine();
                System.out.println();
                if ("0".equals(input)) {
                    adminMenu();
                    break;
                } else if ("1".equals(input)) {
                    getFullCarList(companyID);
                } else if ("2".equals(input)) {
                    addCar(companyID);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int selectCompany() {
        try {
            Scanner scanner = new Scanner(System.in);
            String companyListCommand = "SELECT * FROM COMPANY";
            ResultSet resultSet = SMT.executeQuery(companyListCommand);
            int companyCount = 0;
            if (!resultSet.next()) {
                System.out.println("The company list is empty!\n");
                return -1;
            } else {
                do {
                    companyCount++;
                    String name = resultSet.getString("NAME");
                    System.out.printf("%d. %s\n", companyCount, name);
                } while (resultSet.next());
                System.out.println("0. Back");
                String input;
                while (true) {
                    input = scanner.nextLine();
                    if ("0".equals(input)) {
                        return 0;
                    } else if (input.matches("\\d")
                            && Integer.parseInt(input) > 0
                            && Integer.parseInt(input) <= companyCount) {
                        break;
                    } else {
                        System.out.println("Invalid Entry");
                    }
                }
                int option = Integer.parseInt(input);
                String selectedCompanyIDCommand = String.format(
                        "SELECT ID FROM (" +
                        "SELECT " +
                        "ROW_NUMBER() OVER (ORDER BY ID) AS ROW_NUM, " +
                        "ID " +
                        "FROM COMPANY)  " +
                        "WHERE ROW_NUM = %d", option);
                resultSet = SMT.executeQuery(selectedCompanyIDCommand);
                resultSet.next();
                return resultSet.getInt("ID");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void getFullCarList(int companyID) {
        try {
            int carCount = 1;
            String sql = String.format(
                    "SELECT * " +
                    "FROM CAR " +
                    "WHERE COMPANY_ID = %d;", companyID);
            ResultSet resultSet = SMT.executeQuery(sql);
            if (!resultSet.next()) {
                System.out.println("The car list is empty!");
            } else {
                System.out.println("Car list:");
                do {
                    String name = resultSet.getString("NAME");
                    System.out.printf("%d. %s\n", carCount++, name);
                } while (resultSet.next());
            }
            System.out.println();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void rentAvailableCar(int companyID, int customerID) {
        try {
            int carCount = 1;
            String companyNameCommand = String.format("SELECT NAME " +
                    "FROM COMPANY " +
                    "WHERE ID = %d ", companyID);
            ResultSet companyNameResult = SMT.executeQuery(companyNameCommand);
            companyNameResult.next();
            String companyName = companyNameResult.getString("NAME");
            String availableCarListCommand = String.format("SELECT * " +
                    "FROM CAR " +
                    "WHERE COMPANY_ID = %d " +
                    "EXCEPT " +
                    "SELECT * " +
                    "FROM CAR " +
                    "WHERE ID IN (SELECT RENTED_CAR_ID FROM CUSTOMER);", companyID);
            ResultSet availableCarResultSet = SMT.executeQuery(availableCarListCommand);
            if (!availableCarResultSet.next()) {
                System.out.printf("No available cars in the '%s' company\n", companyName);
            } else {
                ArrayList<String> cars = new ArrayList<>();
                System.out.println("\nChoose a car:");
                do {
                    String name = availableCarResultSet.getString("NAME");
                    cars.add(name);
                    System.out.printf("%d. %s\n", carCount++, name);
                } while (availableCarResultSet.next());
                while (true) {
                    Scanner scanner = new Scanner(System.in);
                    String input = scanner.nextLine();
                    if ("0".equals(input)) {
                        break;
                    }
                    if (!input.matches("\\d")
                            || Integer.parseInt(input) < 0
                            || Integer.parseInt(input) > carCount) {
                        System.out.println("Invalid Input");
                    } else {
                        String rentedCarName = cars.get(Integer.parseInt(input) - 1);
                        System.out.printf("\nYou rented '%s'\n", rentedCarName);
                        SMT.executeUpdate(String.format("UPDATE CUSTOMER " +
                                "SET RENTED_CAR_ID = (SELECT ID FROM CAR WHERE NAME = '%s') " +
                                "WHERE ID = %d", rentedCarName, customerID));
                        break;
                    }
                }
            }
            System.out.println();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void addCompany() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the company name:");
        String companyName = scanner.nextLine();
        executeStatement(String.format(
                "INSERT INTO COMPANY(NAME) " +
                "VALUES ('%s')", companyName));
        System.out.println("The company was created!\n");
    }

    private void addCustomer() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the customer name:");
        String customerName = scanner.nextLine();
        executeStatement(String.format(
                "INSERT INTO CUSTOMER(NAME) " +
                        "VALUES ('%s')", customerName));
        System.out.println("The customer was added!\n");
    }

    private void addCar(int companyID) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the car name:");
        String carName = scanner.nextLine();
        executeStatement(String.format(
                "INSERT INTO CAR(NAME, COMPANY_ID) " +
                        "VALUES ('%s', '%s')", carName, companyID));
        System.out.println("The car was created!\n");
    }


    private void createTables() {
        String companyTable =
                "CREATE TABLE IF NOT EXISTS COMPANY(" +
                "ID INT PRIMARY KEY AUTO_INCREMENT, " +
                "NAME VARCHAR UNIQUE NOT NULL);";
        executeStatement(companyTable);
        String carTable =
                "CREATE TABLE IF NOT EXISTS CAR(" +
                "ID INT PRIMARY KEY AUTO_INCREMENT, " +
                "NAME VARCHAR UNIQUE NOT NULL, " +
                "COMPANY_ID INT NOT NULL, " +
                "FOREIGN KEY (COMPANY_ID) " +
                "REFERENCES COMPANY(ID)" +
                ");";
        executeStatement(carTable);
        String customerTable =
                "CREATE TABLE IF NOT EXISTS CUSTOMER(" +
                "ID INT PRIMARY KEY AUTO_INCREMENT, " +
                "NAME VARCHAR UNIQUE NOT NULL, " +
                "RENTED_CAR_ID INT DEFAULT NULL, " +
                "FOREIGN KEY (RENTED_CAR_ID) " +
                "REFERENCES CAR(ID)" +
                ");";
        executeStatement(customerTable);
    }

    private void eraseTables() {
        String dropCustomerTable = "DROP TABLE IF EXISTS CUSTOMER;";
        executeStatement(dropCustomerTable);
        String dropCarTable = "DROP TABLE IF EXISTS CAR;";
        executeStatement(dropCarTable);
        String dropCompanyTable = "DROP TABLE IF EXISTS COMPANY;";
        executeStatement(dropCompanyTable);
    }


}
