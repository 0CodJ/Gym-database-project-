//This java file consists of the methods for the update cases of the gym database project 

import java.sql.*;
import java.util.Scanner;

public class DatabaseUpdates {

    // Helper method to get string input from user
    private static String getStringInput(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    // Helper method to get integer input from user
    private static int getIntInput(Scanner scanner, String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextInt()) {
            scanner.next(); // consume the invalid input
            scanner.nextLine(); // consume newline
            return -1; // return invalid value
        }
        int value = scanner.nextInt();
        scanner.nextLine(); // consume newline
        return value;
    }

    // Helper method to check if a string contains any digits
    private static boolean containsNumbers(String str) {
        return str.matches(".*\\d.*");
    }

    // Helper method to validate email format
    private static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return true; // Optional field
        }
        return email.matches("^[^@]+@[^@]+\\.[^@]+$");
    }

    // Update Gym Member information
    public static void updateGymMember(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\n=== Update Gym Member ===");
        System.out.println();
        
        // First, display the gym member table
        DatabaseViews.viewGymMembers(conn);
        System.out.println();
        
        // Get the member ID to update
        int memberID = getIntInput(scanner, "Enter the member ID to update: ");
        if (memberID <= 0) {
            System.out.println("Error: Invalid member ID.");
            return;
        }
        
        // Check if member exists
        String checkSql = "SELECT * FROM GymMember WHERE memberID = ?";
        String currentFirstName = "", currentLastName = "", currentPhone = "", currentEmail = "";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, memberID);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Error: Member with ID " + memberID + " does not exist.");
                    return;
                }
                currentFirstName = rs.getString("firstName");
                currentLastName = rs.getString("lastName");
                currentPhone = rs.getString("phoneNumber");
                currentEmail = rs.getString("email");
            }
        }
        
        System.out.println("\nCurrent member information:");
        System.out.println("  First Name: " + currentFirstName);
        System.out.println("  Last Name: " + currentLastName);
        System.out.println("  Phone: " + (currentPhone != null ? currentPhone : "(none)"));
        System.out.println("  Email: " + (currentEmail != null ? currentEmail : "(none)"));
        
        System.out.println("\nWhat would you like to update?");
        System.out.println("1. First Name");
        System.out.println("2. Last Name");
        System.out.println("3. Phone Number");
        System.out.println("4. Email");
        System.out.println("0. Cancel");
        
        int choice = getIntInput(scanner, "Enter your choice: ");
        
        String updateSql = "";
        String newValue = "";
        
        switch (choice) {
            case 1:
                newValue = getStringInput(scanner, "Enter new first name: ");
                if (newValue.isEmpty()) {
                    System.out.println("Error: First name cannot be empty.");
                    return;
                }
                if (containsNumbers(newValue)) {
                    System.out.println("Error: First name cannot contain numbers.");
                    return;
                }
                updateSql = "UPDATE GymMember SET firstName = ? WHERE memberID = ?";
                break;
            case 2:
                newValue = getStringInput(scanner, "Enter new last name: ");
                if (newValue.isEmpty()) {
                    System.out.println("Error: Last name cannot be empty.");
                    return;
                }
                if (containsNumbers(newValue)) {
                    System.out.println("Error: Last name cannot contain numbers.");
                    return;
                }
                updateSql = "UPDATE GymMember SET lastName = ? WHERE memberID = ?";
                break;
            case 3:
                System.out.print("Enter new phone number (10 digits, or press Enter to clear): ");
                newValue = scanner.nextLine().trim();
                if (newValue.isEmpty()) {
                    // Clear phone number
                    updateSql = "UPDATE GymMember SET phoneNumber = NULL WHERE memberID = ?";
                } else {
                    newValue = newValue.replaceAll("[^\\d]", "");
                    if (newValue.length() != 10) {
                        System.out.println("Error: Phone number must be exactly 10 digits.");
                        return;
                    }
                    updateSql = "UPDATE GymMember SET phoneNumber = ? WHERE memberID = ?";
                }
                break;
            case 4:
                System.out.print("Enter new email (or press Enter to clear): ");
                newValue = scanner.nextLine().trim();
                if (newValue.isEmpty()) {
                    // Clear email
                    updateSql = "UPDATE GymMember SET email = NULL WHERE memberID = ?";
                } else {
                    if (!isValidEmail(newValue)) {
                        System.out.println("Error: Invalid email format.");
                        return;
                    }
                    updateSql = "UPDATE GymMember SET email = ? WHERE memberID = ?";
                }
                break;
            case 0:
                System.out.println("Update cancelled.");
                return;
            default:
                System.out.println("Invalid choice.");
                return;
        }
        
        // Execute the update with prepared statement
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            if (choice == 3 && newValue.isEmpty()) {
                // Clear phone - only set memberID
                ps.setInt(1, memberID);
            } else if (choice == 4 && newValue.isEmpty()) {
                // Clear email - only set memberID
                ps.setInt(1, memberID);
            } else {
                ps.setString(1, newValue);
                ps.setInt(2, memberID);
            }
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("\n✓ Success! Gym member updated.");
            } else {
                System.out.println("\nError: Failed to update gym member.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry
                String errorMessage = e.getMessage();
                if (errorMessage.contains("phoneNumber")) {
                    System.out.println("\n✗ Error: This phone number is already in use.");
                } else if (errorMessage.contains("email")) {
                    System.out.println("\n✗ Error: This email is already in use.");
                } else {
                    System.out.println("\n✗ Error: Duplicate entry. " + e.getMessage());
                }
            } else {
                throw e;
            }
        }
    }

    // Update Membership status
    public static void updateMembershipStatus(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\n=== Update Membership Status ===");
        System.out.println();
        
        // First, display the memberships
        DatabaseViews.viewAllMemberships(conn);
        System.out.println();
        
        // Get member ID
        int memberID = getIntInput(scanner, "Enter member ID to update membership: ");
        if (memberID <= 0) {
            System.out.println("Error: Invalid member ID.");
            return;
        }
        
        // Get current membership info
        String checkSql = "SELECT membershipID, status FROM Membership WHERE memberID = ? ORDER BY membershipID DESC LIMIT 1";
        int membershipID = 0;
        String currentStatus = "";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, memberID);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Error: No membership found for member ID " + memberID);
                    return;
                }
                membershipID = rs.getInt("membershipID");
                currentStatus = rs.getString("status");
            }
        }
        
        System.out.println("\nCurrent status: " + currentStatus);
        System.out.println("\nSelect new status:");
        System.out.println("1. Active");
        System.out.println("2. Paused");
        System.out.println("3. Cancelled");
        System.out.println("0. Cancel update");
        
        int choice = getIntInput(scanner, "Enter your choice: ");
        
        String newStatus = "";
        switch (choice) {
            case 1:
                newStatus = "Active";
                break;
            case 2:
                newStatus = "Paused";
                break;
            case 3:
                newStatus = "Cancelled";
                break;
            case 0:
                System.out.println("Update cancelled.");
                return;
            default:
                System.out.println("Invalid choice.");
                return;
        }
        
        if (newStatus.equals(currentStatus)) {
            System.out.println("Status is already '" + currentStatus + "'. No change needed.");
            return;
        }
        
        // Update membership status with prepared statement
        String updateSql = "UPDATE Membership SET status = ? WHERE membershipID = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, membershipID);
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("\n✓ Success! Membership status updated from '" + currentStatus + "' to '" + newStatus + "'.");
            } else {
                System.out.println("\nError: Failed to update membership status.");
            }
        }
    }

    // Update Staff Member salary
    public static void updateStaffSalary(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\n=== Update Staff Member Salary ===");
        System.out.println();
        
        // First, display all staff members
        DatabaseViews.viewAllStaffMembers(conn);
        System.out.println();
        
        // Get staff ID
        int staffID = getIntInput(scanner, "Enter staff ID to update salary: ");
        if (staffID <= 0) {
            System.out.println("Error: Invalid staff ID.");
            return;
        }
        
        // Check if staff exists and get current salary
        String checkSql = "SELECT firstName, lastName, salary FROM StaffMember WHERE staffID = ?";
        String staffName = "";
        double currentSalary = 0;
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, staffID);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Error: Staff member with ID " + staffID + " does not exist.");
                    return;
                }
                staffName = rs.getString("firstName") + " " + rs.getString("lastName");
                currentSalary = rs.getDouble("salary");
            }
        }
        
        System.out.println("\nStaff Member: " + staffName);
        System.out.printf("Current Salary: $%.2f%n", currentSalary);
        
        System.out.print("Enter new salary (must be >= 0): ");
        String salaryStr = scanner.nextLine().trim();
        if (salaryStr.isEmpty()) {
            System.out.println("Error: Salary is required.");
            return;
        }
        
        double newSalary;
        try {
            newSalary = Double.parseDouble(salaryStr);
            if (newSalary < 0) {
                System.out.println("Error: Salary must be >= 0.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid salary format.");
            return;
        }
        
        // Update salary with prepared statement
        String updateSql = "UPDATE StaffMember SET salary = ? WHERE staffID = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setDouble(1, newSalary);
            ps.setInt(2, staffID);
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.printf("\n✓ Success! Salary updated from $%.2f to $%.2f for %s.%n", 
                                  currentSalary, newSalary, staffName);
            } else {
                System.out.println("\nError: Failed to update salary.");
            }
        }
    }

    // Update Payment status (demonstrates transactional workflow with rollback)
    public static void updatePaymentStatus(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\n=== Update Payment Status ===");
        System.out.println();
        
        // First, display all payments
        DatabaseViews.viewPayments(conn);
        System.out.println();
        
        // Get payment ID
        int paymentID = getIntInput(scanner, "Enter payment ID to update: ");
        if (paymentID <= 0) {
            System.out.println("Error: Invalid payment ID.");
            return;
        }
        
        // Check if payment exists and get current status
        String checkSql = "SELECT status, memberID, amount FROM Payment WHERE paymentID = ?";
        String currentStatus = "";
        int memberID = 0;
        double amount = 0;
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, paymentID);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Error: Payment with ID " + paymentID + " does not exist.");
                    return;
                }
                currentStatus = rs.getString("status");
                memberID = rs.getInt("memberID");
                amount = rs.getDouble("amount");
            }
        }
        
        System.out.println("\nCurrent status: " + currentStatus);
        System.out.println("\nSelect new status:");
        System.out.println("1. Success");
        System.out.println("2. Pending");
        System.out.println("3. Failed");
        System.out.println("4. Refunded");
        System.out.println("0. Cancel update");
        
        int choice = getIntInput(scanner, "Enter your choice: ");
        
        String newStatus = "";
        switch (choice) {
            case 1:
                newStatus = "Success";
                break;
            case 2:
                newStatus = "Pending";
                break;
            case 3:
                newStatus = "Failed";
                break;
            case 4:
                newStatus = "Refunded";
                break;
            case 0:
                System.out.println("Update cancelled.");
                return;
            default:
                System.out.println("Invalid choice.");
                return;
        }
        
        if (newStatus.equals(currentStatus)) {
            System.out.println("Status is already '" + currentStatus + "'. No change needed.");
            return;
        }
        
        // Update payment status with prepared statement
        String updateSql = "UPDATE Payment SET status = ? WHERE paymentID = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, paymentID);
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("\n✓ Success! Payment status updated from '" + currentStatus + "' to '" + newStatus + "'.");
            } else {
                System.out.println("\nError: Failed to update payment status.");
            }
        }
    }

    // Transactional Workflow: Transfer membership to another plan with payment
    // This demonstrates COMMIT and ROLLBACK
    public static void transferMembershipPlan(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\n=== Transfer Membership to Different Plan ===");
        System.out.println("This operation updates the membership plan and creates a new payment record.");
        System.out.println("It uses a TRANSACTION to ensure both operations succeed or both fail (ROLLBACK).");
        System.out.println();
        
        // Disable auto-commit for transaction
        boolean originalAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        
        try {
            // Display members with active memberships
            System.out.println("Members with Active Memberships:");
            String activeMembersSql = "SELECT m.membershipID, m.memberID, gm.firstName, gm.lastName, " +
                                     "p.planID, pt.planType, pt.price " +
                                     "FROM Membership m " +
                                     "JOIN GymMember gm ON m.memberID = gm.memberID " +
                                     "JOIN Plan p ON m.planID = p.planID " +
                                     "JOIN PlanTypeInfo pt ON p.planType = pt.planType " +
                                     "WHERE m.status = 'Active' " +
                                     "ORDER BY m.memberID";
            
            try (PreparedStatement ps = conn.prepareStatement(activeMembersSql);
                 ResultSet rs = ps.executeQuery()) {
                
                java.util.List<String[]> rows = new java.util.ArrayList<>();
                while (rs.next()) {
                    rows.add(new String[]{
                        String.valueOf(rs.getInt("membershipID")),
                        String.valueOf(rs.getInt("memberID")),
                        rs.getString("firstName") + " " + rs.getString("lastName"),
                        String.valueOf(rs.getInt("planID")),
                        rs.getString("planType"),
                        String.format("$%.2f", rs.getDouble("price"))
                    });
                }
                
                if (rows.isEmpty()) {
                    System.out.println("No active memberships found.");
                    conn.setAutoCommit(originalAutoCommit);
                    return;
                }
                
                int[] colWidths = {0, 0, 0, 0, 0, 0};
                String[] headers = {"Membership ID", "Member ID", "Member Name", "Plan ID", "Plan Type", "Price"};
                
                for (int i = 0; i < headers.length; i++) {
                    colWidths[i] = Math.max(colWidths[i], headers[i].length());
                }
                for (String[] row : rows) {
                    for (int i = 0; i < row.length; i++) {
                        colWidths[i] = Math.max(colWidths[i], row[i].length());
                    }
                }
                
                DatabaseViews.printTableRow(headers, colWidths);
                DatabaseViews.printTableSeparator(colWidths);
                for (String[] row : rows) {
                    DatabaseViews.printTableRow(row, colWidths);
                }
            }
            
            System.out.println();
            
            // Get membership ID to transfer
            int membershipID = getIntInput(scanner, "Enter membership ID to transfer: ");
            if (membershipID <= 0) {
                System.out.println("Error: Invalid membership ID.");
                conn.rollback();
                System.out.println("ROLLBACK executed - no changes made.");
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            
            // Verify membership exists and is active
            String checkMembershipSql = "SELECT memberID, planID FROM Membership WHERE membershipID = ? AND status = 'Active'";
            int memberID = 0;
            int currentPlanID = 0;
            try (PreparedStatement ps = conn.prepareStatement(checkMembershipSql)) {
                ps.setInt(1, membershipID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Error: Active membership with ID " + membershipID + " not found.");
                        conn.rollback();
                        System.out.println("ROLLBACK executed - no changes made.");
                        conn.setAutoCommit(originalAutoCommit);
                        return;
                    }
                    memberID = rs.getInt("memberID");
                    currentPlanID = rs.getInt("planID");
                }
            }
            
            // Display available plans
            System.out.println("\nAvailable Plans:");
            String plansSql = "SELECT p.planID, pt.planType, pt.price FROM Plan p " +
                             "JOIN PlanTypeInfo pt ON p.planType = pt.planType ORDER BY p.planID";
            try (PreparedStatement ps = conn.prepareStatement(plansSql);
                 ResultSet rs = ps.executeQuery()) {
                
                java.util.List<String[]> rows = new java.util.ArrayList<>();
                while (rs.next()) {
                    rows.add(new String[]{
                        String.valueOf(rs.getInt("planID")),
                        rs.getString("planType"),
                        String.format("$%.2f", rs.getDouble("price"))
                    });
                }
                
                int[] colWidths = {0, 0, 0};
                String[] headers = {"Plan ID", "Plan Type", "Price"};
                
                for (int i = 0; i < headers.length; i++) {
                    colWidths[i] = Math.max(colWidths[i], headers[i].length());
                }
                for (String[] row : rows) {
                    for (int i = 0; i < row.length; i++) {
                        colWidths[i] = Math.max(colWidths[i], row[i].length());
                    }
                }
                
                DatabaseViews.printTableRow(headers, colWidths);
                DatabaseViews.printTableSeparator(colWidths);
                for (String[] row : rows) {
                    DatabaseViews.printTableRow(row, colWidths);
                }
            }
            
            System.out.println();
            
            // Get new plan ID
            int newPlanID = getIntInput(scanner, "Enter new plan ID: ");
            if (newPlanID <= 0) {
                System.out.println("Error: Invalid plan ID.");
                conn.rollback();
                System.out.println("ROLLBACK executed - no changes made.");
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            
            if (newPlanID == currentPlanID) {
                System.out.println("Error: Member is already on this plan.");
                conn.rollback();
                System.out.println("ROLLBACK executed - no changes made.");
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            
            // Get new plan price
            String getPriceSql = "SELECT pt.price, pt.planType FROM Plan p " +
                                "JOIN PlanTypeInfo pt ON p.planType = pt.planType WHERE p.planID = ?";
            double newPrice = 0;
            String newPlanType = "";
            try (PreparedStatement ps = conn.prepareStatement(getPriceSql)) {
                ps.setInt(1, newPlanID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Error: Plan ID " + newPlanID + " does not exist.");
                        conn.rollback();
                        System.out.println("ROLLBACK executed - no changes made.");
                        conn.setAutoCommit(originalAutoCommit);
                        return;
                    }
                    newPrice = rs.getDouble("price");
                    newPlanType = rs.getString("planType");
                }
            }
            
            // Get desk staff to process
            System.out.println("\nAvailable Desk Staff to process transfer:");
            String deskStaffSql = "SELECT d.staffID, sm.firstName, sm.lastName FROM Desk d " +
                                 "JOIN StaffMember sm ON d.staffID = sm.staffID ORDER BY d.staffID";
            try (PreparedStatement ps = conn.prepareStatement(deskStaffSql);
                 ResultSet rs = ps.executeQuery()) {
                
                java.util.List<String[]> rows = new java.util.ArrayList<>();
                while (rs.next()) {
                    rows.add(new String[]{
                        String.valueOf(rs.getInt("staffID")),
                        rs.getString("firstName") + " " + rs.getString("lastName")
                    });
                }
                
                if (rows.isEmpty()) {
                    System.out.println("Error: No desk staff available to process the transfer.");
                    conn.rollback();
                    System.out.println("ROLLBACK executed - no changes made.");
                    conn.setAutoCommit(originalAutoCommit);
                    return;
                }
                
                int[] colWidths = {0, 0};
                String[] headers = {"Staff ID", "Name"};
                
                for (int i = 0; i < headers.length; i++) {
                    colWidths[i] = Math.max(colWidths[i], headers[i].length());
                }
                for (String[] row : rows) {
                    for (int i = 0; i < row.length; i++) {
                        colWidths[i] = Math.max(colWidths[i], row[i].length());
                    }
                }
                
                DatabaseViews.printTableRow(headers, colWidths);
                DatabaseViews.printTableSeparator(colWidths);
                for (String[] row : rows) {
                    DatabaseViews.printTableRow(row, colWidths);
                }
            }
            
            System.out.println();
            int staffID = getIntInput(scanner, "Enter desk staff ID to process: ");
            if (staffID <= 0) {
                System.out.println("Error: Invalid staff ID.");
                conn.rollback();
                System.out.println("ROLLBACK executed - no changes made.");
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            
            // Verify staff is desk staff
            String checkStaffSql = "SELECT staffID FROM Desk WHERE staffID = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkStaffSql)) {
                ps.setInt(1, staffID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Error: Staff ID " + staffID + " is not a desk staff member.");
                        conn.rollback();
                        System.out.println("ROLLBACK executed - no changes made.");
                        conn.setAutoCommit(originalAutoCommit);
                        return;
                    }
                }
            }
            
            // Get payment type
            System.out.println("\nPayment Types:");
            System.out.println("1. CASH");
            System.out.println("2. CARD");
            System.out.println("3. ONLINE");
            int paymentChoice = getIntInput(scanner, "Select payment type (1-3): ");
            
            String paymentType = "";
            switch (paymentChoice) {
                case 1:
                    paymentType = "CASH";
                    break;
                case 2:
                    paymentType = "CARD";
                    break;
                case 3:
                    paymentType = "ONLINE";
                    break;
                default:
                    System.out.println("Error: Invalid payment type selection.");
                    conn.rollback();
                    System.out.println("ROLLBACK executed - no changes made.");
                    conn.setAutoCommit(originalAutoCommit);
                    return;
            }
            
            // Calculate new dates
            java.sql.Date startDate = new java.sql.Date(System.currentTimeMillis());
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(startDate);
            if (newPlanType.equals("Monthly") || newPlanType.equals("Monthly Premium")) {
                cal.add(java.util.Calendar.MONTH, 1);
            } else if (newPlanType.equals("Annual")) {
                cal.add(java.util.Calendar.YEAR, 1);
            }
            java.sql.Date endDate = new java.sql.Date(cal.getTimeInMillis());
            
            System.out.println("\n--- Transaction Starting ---");
            System.out.println("Step 1: Updating membership plan...");
            
            // STEP 1: Update membership plan and dates
            String updateMembershipSql = "UPDATE Membership SET planID = ?, startDate = ?, endDate = ? WHERE membershipID = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateMembershipSql)) {
                ps.setInt(1, newPlanID);
                ps.setDate(2, startDate);
                ps.setDate(3, endDate);
                ps.setInt(4, membershipID);
                
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Failed to update membership.");
                }
                System.out.println("   Membership updated successfully.");
            }
            
            System.out.println("Step 2: Creating payment record...");
            
            // STEP 2: Create payment record
            String insertPaymentSql = "INSERT INTO Payment (staffID, memberID, amount, paymentType, status) " +
                                     "VALUES (?, ?, ?, ?, 'Success')";
            try (PreparedStatement ps = conn.prepareStatement(insertPaymentSql)) {
                ps.setInt(1, staffID);
                ps.setInt(2, memberID);
                ps.setDouble(3, newPrice);
                ps.setString(4, paymentType);
                
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Failed to create payment record.");
                }
                System.out.println("   Payment record created successfully.");
            }
            
            // COMMIT the transaction
            conn.commit();
            System.out.println("\n--- COMMIT executed ---");
            System.out.println("\n✓ Success! Membership transferred to new plan.");
            System.out.println("  New Plan: " + newPlanType + " ($" + String.format("%.2f", newPrice) + ")");
            System.out.println("  New Start Date: " + startDate);
            System.out.println("  New End Date: " + endDate);
            System.out.println("  Payment: " + paymentType + " - Success");
            
        } catch (SQLException e) {
            // ROLLBACK on any error
            conn.rollback();
            System.out.println("\n--- ROLLBACK executed ---");
            System.out.println("\n✗ Error: Transaction failed. All changes have been rolled back.");
            System.out.println("  Reason: " + e.getMessage());
            throw e;
        } finally {
            // Restore original auto-commit setting
            conn.setAutoCommit(originalAutoCommit);
        }
    }
}
