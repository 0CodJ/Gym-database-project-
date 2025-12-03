import java.sql.*;
import java.util.Scanner;

public class DatabaseDeletions {
    //Method to delete a gym member from the GymMember table
    public static void deleteGymMember(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\n=== Delete Gym Member ===");
        System.out.println();
        System.out.println("Delete a gym member from the GymMember table.");
        
        // First, display the gym member table
        DatabaseViews.viewGymMembers(conn);
        System.out.println();
        
        // Get the member ID from user
        System.out.print("Enter the member ID to delete: ");
        if (!scanner.hasNextInt()) {
            scanner.nextLine(); 
            System.out.println("Error: Invalid input. Member ID must be a number.");
            return;
        }
        int memberID = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        // Check if member exists before attempting deletion
        String checkSql = "SELECT memberID FROM GymMember WHERE memberID = ?";
        try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, memberID);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Error: Member with ID " + memberID + " does not exist.");
                    return;
                }
            }
        }
        
        // Delete the gym member with a prepared statement 
        String deleteSql = "DELETE FROM GymMember WHERE memberID = ?";
        try (PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
            deletePs.setInt(1, memberID);
            int rowsAffected = deletePs.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Successfully deleted gym member with ID " + memberID + ".");
            } else {
                System.out.println("Error: Failed to delete gym member with ID " + memberID + ".");
            }
        }
    }
}
