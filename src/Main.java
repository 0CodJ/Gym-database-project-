//This java file consists of the main class for the gym database project 

import java.sql.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

public class Main {

    //Declare connection and scanner objects 
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

        //Get database connection properties from app.properties file
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
                switch (choice) { //switch statements which are used for the option menu in the UI 
                    case 1:
                        System.out.println("\n=== Gym Members ===");
                        System.out.println();
                        System.out.println("This table shows all the gym members and their details, including the member ID, first name, last name, birthday, phone number, email, and date joined.");
                        DatabaseViews.viewGymMembers(conn);
                        break;
                    case 2:
                        System.out.println("\n=== All Memberships ===");
                        System.out.println();
                        System.out.println("This table shows all the memberships and their details, including the member ID, first name, last name, status, plan type, price, start date, and end date.");
                        DatabaseViews.viewAllMemberships(conn);
                        break;
                    case 3:
                        DatabaseViews.viewStaffMembers(conn, scanner);
                        break;
                    case 4:
                        System.out.println("\n=== Plans ===");
                        System.out.println();
                        System.out.println("This table shows all the plans and their details, including the plan ID, plan type, and price.");
                        DatabaseViews.viewPlans(conn);
                        break;
                    case 5:
                        System.out.println("\n=== Payments ===");
                        System.out.println();
                        System.out.println("This table shows payments, the ID of the staff member that processed the payment, the ID of the member that made the payment, the amount of the payment, the type of payment, the date of the payment, and the status of the payment.");
                        DatabaseViews.viewPayments(conn);
                        break;
                    case 6:
                        System.out.println("\n=== Check-Ins ===");
                        System.out.println();
                        System.out.println("This table shows all the check-ins and their details, including the check-in ID, membership ID, ID of staff who checked in the member, timestamp, and location.");
                        DatabaseViews.viewCheckIns(conn);
                        break;
                    case 7:
                        System.out.println("\n=== Active Members (using VIEW) ===");
                        System.out.println();
                        System.out.println("This table shows all active members with their membership details using the ActiveMembersView, including member ID, member name, email, phone number, membership ID, start date, end date, plan type, and price.");
                        DatabaseViews.viewActiveMembersView(conn);
                        break;
                    case 8:
                        System.out.println("\n=== Trainer Trains Member ===");
                        System.out.println();
                        System.out.println("This table shows the relationships between trainers and members, including the trainer ID, trainer name, member ID, and member name.");
                        DatabaseViews.viewTrainerTrainsMember(conn);
                        break;
                    case 9:
                        System.out.println("\n=== Guest Members ===");
                        System.out.println();
                        System.out.println("This table shows all guest members and their details, including the guest ID, first name, last name, birthday, relationship to member, member ID, and the member's name.");
                        DatabaseViews.viewGuestMembers(conn);
                        break;
                    case 10:
                        System.out.println("\n=== Guest Visits ===");
                        System.out.println();
                        System.out.println("This table shows all guest visits, including the visit ID, guest ID, guest name, visit date, member ID, and the member's name.");
                        DatabaseViews.viewGuestVisits(conn);
                        break;
                    case 11:
                        DatabaseViews.viewMemberPaymentHistory(conn, scanner);
                        break;
                    case 12:
                        DatabaseViews.viewTotalRevenue(conn, scanner);
                        break;
                    case 13:
                        DatabaseInsertions.insertGymMember(conn, scanner);
                        break;
                    case 14:
                        DatabaseInsertions.purchaseMembership(conn, scanner);
                        break;
                    case 15:
                        DatabaseInsertions.insertStaffMember(conn, scanner);
                        break;
                    case 16: 
                        DatabaseUpdates.updateGymMember(conn, scanner);
                        break;
                    case 17:
                        DatabaseUpdates.updateMembershipStatus(conn, scanner);
                        break;
                    case 18:
                        DatabaseUpdates.updateStaffSalary(conn, scanner);
                        break;
                    case 19:
                        DatabaseUpdates.updatePaymentStatus(conn, scanner);
                        break;
                    case 20:
                        DatabaseDeletions.deleteGymMember(conn, scanner);
                        break;
                    case 21:
                        DatabaseUpdates.transferMembershipPlan(conn, scanner);
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

    //Method that shows the main mennu and options for the user to select from
    private static void printMainMenu() {
        System.out.println("=====================================");
        System.out.println(" Gym Database Management System Menu");
        System.out.println("=====================================");
        System.out.println("Note: How the terminal is zoomed can affect the output of the table columns, so please zoom out to see the full output.");
        System.out.println("Enter the number of the option you want to select:");
        System.out.println("\n-------------------------------------");
        System.out.println("View/Selection Options:");
        System.out.println("1. View Gym Members");
        System.out.println("2. View All Memberships");
        System.out.println("3. View Staff Members");
        System.out.println("4. View Plans");
        System.out.println("5. View Payments");
        System.out.println("6. View Check-Ins");
        System.out.println("7. View Active Members (Uses a view created in the database)");
        System.out.println("8. View Trainer Trains Member");
        System.out.println("9. View Guest Members");
        System.out.println("10. View Guest Visits");
        System.out.println("11. View Member Payment History (Stored Procedure)");
        System.out.println("12. Calculate Total Revenue (Stored Function)");
        System.out.println("-------------------------------------");
        System.out.println("Insertion Options:");
        System.out.println("13. Add new Gym Member");
        System.out.println("14. Purchase Membership (Transaction Demo)");
        System.out.println("15. Insert Staff Member");
        System.out.println("-------------------------------------");
        System.out.println("Update Options:");
        System.out.println("16. Update Gym Member");
        System.out.println("17. Update Membership Status");
        System.out.println("18. Update Staff Salary");
        System.out.println("19. Update Payment Status");
        System.out.println("-------------------------------------");
        System.out.println("Deletion Options:");
        System.out.println("20. Delete Gym Member");
        System.out.println("-------------------------------------");
        System.out.println("Transaction Demo:");
        System.out.println("21. Transfer Membership Plan (COMMIT/ROLLBACK Demo)");
        System.out.println("\n0. Exit");
        System.out.println("=====================================");
    }

    //Method to get integer input from user (also ensures that invalid inputs are handled properly)
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
}
