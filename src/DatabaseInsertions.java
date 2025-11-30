import java.sql.*;
import java.util.Scanner;

public class DatabaseInsertions {

    // Helper method to get string input from user
    private static String getStringInput(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    // Helper method to get date input from user (format: YYYY-MM-DD)
    private static String getDateInput(Scanner scanner, String prompt) {
        System.out.print(prompt + " (YYYY-MM-DD): ");
        return scanner.nextLine().trim();
    }

    public static void insertGymMember(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\n=== Insert New Gym Member ===");
        System.out.println();
        
        // Get required fields
        String firstName = getStringInput(scanner, "Enter first name: ");
        if (firstName.isEmpty()) {
            System.out.println("Error: First name is required.");
            return;
        }
        
        String lastName = getStringInput(scanner, "Enter last name: ");
        if (lastName.isEmpty()) {
            System.out.println("Error: Last name is required.");
            return;
        }
        
        String birthday = getDateInput(scanner, "Enter birthday");
        if (birthday.isEmpty()) {
            System.out.println("Error: Birthday is required.");
            return;
        }
        
        // Validate date format
        if (!birthday.matches("\\d{4}-\\d{2}-\\d{2}")) {
            System.out.println("Error: Invalid date format. Please use YYYY-MM-DD format.");
            return;
        }
        
        // Get optional fields
        System.out.print("Enter phone number (optional, press Enter to skip): ");
        String phoneNumber = scanner.nextLine().trim();
        if (phoneNumber.isEmpty()) {
            phoneNumber = null;
        }
        
        System.out.print("Enter email (optional, press Enter to skip): ");
        String email = scanner.nextLine().trim();
        if (email.isEmpty()) {
            email = null;
        }
        
        // Prepare SQL statement
        String sql = "INSERT INTO GymMember (firstName, lastName, birthday, phoneNumber, email) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setDate(3, java.sql.Date.valueOf(birthday));
            
            if (phoneNumber != null) {
                ps.setString(4, phoneNumber);
            } else {
                ps.setNull(4, Types.VARCHAR);
            }
            
            if (email != null) {
                ps.setString(5, email);
            } else {
                ps.setNull(5, Types.VARCHAR);
            }
            
            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("\nSuccess! Gym member '" + firstName + " " + lastName + "' has been added to the database.");
            } else {
                System.out.println("\nError: Failed to insert gym member.");
            }
        } catch (SQLException e) {
            // Check for unique constraint violations
            if (e.getErrorCode() == 1062) { // MySQL duplicate entry error code
                String errorMessage = e.getMessage();
                if (errorMessage.contains("phoneNumber")) {
                    System.out.println("\nError: A member with this phone number already exists.");
                } else if (errorMessage.contains("email")) {
                    System.out.println("\nError: A member with this email already exists.");
                } else {
                    System.out.println("\nError: " + e.getMessage());
                }
            } else {
                System.out.println("\nError: " + e.getMessage());
            }
            throw e; // Re-throw to be handled by the calling method
        }
    }
}

