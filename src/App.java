import java.sql.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

public class App {

    private static Connection conn;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Load database connection properties
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("app.properties"));
        } catch (IOException e) {
            System.out.println("Error loading app.properties file: " + e.getMessage());
            System.out.println("Make sure app.properties exists in the project root directory.");
            return;
        }

        String url = props.getProperty("db.url");
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");

        // Test database connection
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Database connection successful!");
            System.out.println();
        } 
        catch (ClassNotFoundException e) {
            System.out.println("Error: MySQL JDBC Driver not found.");
            return;
        } 
        catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
            return;
        }

        // Main menu loop
        boolean running = true;
        System.out.println("Welcome to the Gym Database Management System!");
        
        while (running) {
            printMainMenu();

            int choice = getIntInput("Enter your choice: ");
            
            
            try {
                switch (choice) {
                    case 1:
                        DatabaseViews.viewGymMembers(conn);
                        break;
                    case 2:
                        DatabaseViews.viewAllMemberships(conn);
                        break;
                    case 3:
                        viewStaffMembers();
                        break;
                    case 4:
                        DatabaseViews.viewPlans(conn);
                        break;
                    case 5:
                        DatabaseViews.viewPayments(conn);
                        break;
                    case 6:
                        DatabaseViews.viewCheckIns(conn);
                        break;
                    case 7:
                        DatabaseViews.viewActiveMembers(conn);
                        break;
                    case 0:
                        running = false;
                        System.out.println("Exiting...");
                        break;
                    default:
                        System.out.println("Invalid input.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
            
            if (running) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }

        // Close connection
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
            scanner.close();
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    private static void printMainMenu() {
        System.out.println("=====================================");
        System.out.println(" Gym Database Management System Menu");
        System.out.println("=====================================");
        System.out.println("Note: How the terminal is zoomed can affect the output of the table columns, so please zoom out to see the full output.");
        System.out.println("Enter the number of the option you want to select:");
        System.out.println("\n-------------------------------------");
        System.out.println("View Options:");
        System.out.println("1. View Gym Members");
        System.out.println("2. View All Memberships");
        System.out.println("3. View Staff Members");
        System.out.println("4. View Plans");
        System.out.println("5. View Payments");
        System.out.println("6. View Check-Ins");
        System.out.println("7. View Active Members");
        System.out.println("-------------------------------------");
        System.out.println("\n0. Exit");
        System.out.println("=====================================");
    }

    private static int getIntInput(String prompt) { 
        System.out.print(prompt);
        if (!scanner.hasNextInt()) {
            scanner.next(); // consume the invalid input
            scanner.nextLine(); // consume newline
            return -1; // return invalid value to trigger default case
        }
        int value = scanner.nextInt();
        scanner.nextLine(); // consume newline
        return value;
    }

    private static void viewStaffMembers() throws SQLException {
        System.out.println("\n=== Staff Members Menu ===");
        System.out.println("1. View All Staff Members");
        System.out.println("2. View Desk Staff Only");
        System.out.println("3. View Trainers Only");
        System.out.println("4. View Managers Only");
        System.out.println("0. Back to Main Menu");
        
        int choice = getIntInput("Enter your choice: ");
        
        switch (choice) {
            case 1:
                DatabaseViews.viewAllStaffMembers(conn);
                break;
            case 2:
                DatabaseViews.viewDeskStaff(conn);
                break;
            case 3:
                DatabaseViews.viewTrainers(conn);
                break;
            case 4:
                DatabaseViews.viewManagers(conn);
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid input.");
        }
    }

}
