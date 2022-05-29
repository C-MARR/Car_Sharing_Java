package carsharing;

import java.sql.*;
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
            eraseTables();
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
                            "0. Exit");
            String input = scanner.nextLine();
            if ("1".equals(input)) {
                System.out.println();
                adminMenu();
            } else if ("0".equals(input)) {
                EXIT = true;
            }
        }
    }

    private void adminMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println(
                    "1. Company list\n" +
                            "2. Create a company\n" +
                            "0. Back");
            String input = scanner.nextLine();
            System.out.println();
            if ("1".equals(input)) {
                getCompanyList();
            } else if ("2".equals(input)) {
                addCompany();
            } else if ("0".equals(input)) {
                break;
            }
        }

    }

    private void companyMenu(int companyID) {
        Scanner scanner = new Scanner(System.in);
        try {
            String sql = String.format(
                    "SELECT NAME " +
                            "FROM COMPANY " +
                            "WHERE ID = %d;", companyID);
            ResultSet resultSet = SMT.executeQuery(sql);
            resultSet.next();
            String companyName = resultSet.getString("NAME");
            System.out.printf("'%s' company\n", companyName);
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
                    getCarList(companyID);
                } else if ("2".equals(input)) {
                    addCar(companyID);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void getCompanyList() {
        try {
            Scanner scanner = new Scanner(System.in);
            String sql = "SELECT * FROM COMPANY";
            ResultSet resultSet = SMT.executeQuery(sql);
            StringBuilder companyOptions = new StringBuilder();
            int companyCount = 0;
            if (!resultSet.next()) {
                System.out.println("The company list is empty!");
            } else {
                do {
                    int id = resultSet.getInt("ID");
                    companyCount++;
                    String name = resultSet.getString("NAME");
                    companyOptions.append(String.format("%d. %s\n", id, name));
                } while (resultSet.next());
                companyOptions.append("0. Back");
                while (true) {
                    System.out.println(companyOptions);
                    String input = scanner.nextLine();
                    System.out.println();
                    if (!input.matches("\\d")
                            || Integer.parseInt(input) < 0
                            || Integer.parseInt(input) > companyCount) {
                        System.out.println("Invalid Entry");
                    } else {
                        int option = Integer.parseInt(input);
                        if (option == 0) {
                            break;
                        } else {
                            companyMenu(option);
                        }
                    }
                }

            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void getCarList(int companyID) {
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

    private void addCompany() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the company name:");
        String companyName = scanner.nextLine();
        executeStatement(String.format(
                "INSERT INTO COMPANY(NAME) " +
                "VALUES ('%s')", companyName));
        System.out.println("The company was created!\n");
    }

    private void addCar(int companyID) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the car name:");
        String companyName = scanner.nextLine();
        executeStatement(String.format(
                "INSERT INTO CAR(NAME, COMPANY_ID) " +
                "VALUES ('%s', '%s')", companyName, companyID));
        System.out.println("The car was created!\n");
    }


    private void createTables() {
        String sql =  "CREATE TABLE IF NOT EXISTS COMPANY(" +
                "ID INT PRIMARY KEY AUTO_INCREMENT, " +
                "NAME VARCHAR UNIQUE NOT NULL);";
        executeStatement(sql);
        sql =  "CREATE TABLE IF NOT EXISTS CAR(" +
                "ID INT PRIMARY KEY AUTO_INCREMENT, " +
                "NAME VARCHAR UNIQUE NOT NULL, " +
                "COMPANY_ID INT NOT NULL, " +
                "CONSTRAINT COMPANY_ID_FK FOREIGN KEY (COMPANY_ID) " +
                "REFERENCES COMPANY(ID)" +
                ");";
        executeStatement(sql);
    }

    private void eraseTables() {
        String sql =  "DROP TABLE IF EXISTS CAR;";
        executeStatement(sql);
        sql =  "DROP TABLE IF EXISTS COMPANY;";
        executeStatement(sql);
    }


}
