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

    // Helper method to check if a string contains any digits
    private static boolean containsNumbers(String str) {
        return str.matches(".*\\d.*");
    }

    // Helper method to calculate age from birthday
    private static int calculateAge(java.sql.Date birthday) {
        java.util.Calendar birthCal = java.util.Calendar.getInstance();
        birthCal.setTime(birthday);
        
        java.util.Calendar nowCal = java.util.Calendar.getInstance();
        
        int age = nowCal.get(java.util.Calendar.YEAR) - birthCal.get(java.util.Calendar.YEAR);
        
        // Check if birthday hasn't occurred this year yet
        if (nowCal.get(java.util.Calendar.DAY_OF_YEAR) < birthCal.get(java.util.Calendar.DAY_OF_YEAR)) {
            age--;
        }
        
        return age;
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
        if (containsNumbers(firstName)) {
            System.out.println("Error: First name cannot contain numbers.");
            return;
        }
        
        String lastName = getStringInput(scanner, "Enter last name: ");
        if (lastName.isEmpty()) {
            System.out.println("Error: Last name is required.");
            return;
        }
        if (containsNumbers(lastName)) {
            System.out.println("Error: Last name cannot contain numbers.");
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
        
        // Parse and validate the date, then check age
        java.sql.Date birthdayDate;
        try {
            birthdayDate = java.sql.Date.valueOf(birthday);
            
            // Check if date is in the future
            java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
            if (birthdayDate.after(today)) {
                System.out.println("Error: Birthday cannot be in the future.");
                return;
            }
            
            // Calculate age and reject if under 16
            int age = calculateAge(birthdayDate);
            if (age < 16) {
                System.out.println("\n✗ Error: This member is under 16 years old (" + age + " years).");
                System.out.println("   Members must be at least 16 years old to be added to the database.");
                return;
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: Invalid date. Please use YYYY-MM-DD format.");
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
        
        // Prepare SQL statement - dateJoined will use DB default (CURRENT_DATE)
        String sql = "INSERT INTO GymMember (firstName, lastName, birthday, phoneNumber, email) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setDate(3, birthdayDate);
            
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
                System.out.println("\n✓ Success! Gym member '" + firstName + " " + lastName + "' has been added to the database.");
            } else {
                System.out.println("\nError: Failed to insert gym member.");
            }
        } catch (SQLException e) {
            // Check for unique constraint violations
            if (e.getErrorCode() == 1062) { // MySQL duplicate entry error code. Found via google 
                String errorMessage = e.getMessage();
                if (errorMessage.contains("phoneNumber") || errorMessage.contains("phone")) {
                    System.out.println("\n✗ Error: A member with this phone number already exists.");
                    System.out.println("   Phone numbers must be unique.");
                } else if (errorMessage.contains("email")) {
                    System.out.println("\n✗ Error: A member with this email already exists.");
                    System.out.println("   Email addresses must be unique.");
                } else {
                    System.out.println("\n✗ Error: Duplicate entry detected. " + e.getMessage());
                }
            } else {
                System.out.println("\n✗ Error: Database error occurred. " + e.getMessage());
            }
            throw e; // Re-throw to be handled by the calling method
        }
    }
}

