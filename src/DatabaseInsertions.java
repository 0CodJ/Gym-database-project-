//This java file consists of the methods for the insertion cases of the gym database project 

import java.sql.*;
import java.util.Scanner;

public class DatabaseInsertions {

    //Declare scanner object to be used in all the helper methods
    private static Scanner scanner = new Scanner(System.in);
    
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

    // Helper method to validate email format
    private static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return true; // Optional field
        }
        //Validates string but making sure it contains @ and at least one dot after @
        return email.matches("^[^@]+@[^@]+\\.[^@]+$");
    }

    // Helper method to validate schedule format
    // Format: "Mon- 9AM-5PM" for single day or "Mon-Fri 9AM-5PM" for multiple days
    private static boolean isValidScheduleFormat(String schedule) {
        if (schedule == null || schedule.isEmpty()) {
            return false; // Schedule is required
        }
        // Day abbreviations: Mon, Tue, Wed, Thu, Fri, Sat, Sun
        String day = "(Mon|Tue|Wed|Thu|Fri|Sat|Sun)";
        // Time format: 9AM-5PM (1-2 digits, AM/PM, dash, 1-2 digits, AM/PM)
        String time = "\\d{1,2}(AM|PM)-\\d{1,2}(AM|PM)";
        // Pattern 1: Single day with dash: "Mon- 9AM-5PM" (dash, optional space, time)
        String singleDayPattern = "^" + day + "-\\s*" + time + "$";
        // Pattern 2: Multiple days: "Mon-Fri 9AM-5PM" (day-dash-day, space, time)
        String multipleDayPattern = "^" + day + "-" + day + "\\s+" + time + "$";
        // Match either pattern
        return schedule.matches(singleDayPattern) || schedule.matches(multipleDayPattern);
    }

    // Helper method to calculate age from birthday to verify age of gym member is >= 16 
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


    //Case 13 of the main menu. This method contains all the lgoc for inserting a new gym member into the database 
    public static void insertGymMember(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\n=== Insert New Gym Member ===");
        System.out.println();
        
        // Get required fields/attributes for the gym member 
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
        
        // Validate date format for consistency 
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
            int age = calculateAge(birthdayDate); //use calculateAge helper method 
            if (age < 16) {
                System.out.println("\n✗ Error: This member is under 16 years old (" + age + " years).");
                System.out.println("   Members must be at least 16 years old to be added to the database.");
                return;
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: Invalid date. Please use YYYY-MM-DD format.");
            return;
        }
        
        //Phone number attribute is optional 
        System.out.print("Enter phone number (optional, 10 digits only, press Enter to skip): ");
        String phoneNumber = scanner.nextLine().trim();
        if (phoneNumber.isEmpty()) {
            phoneNumber = null;
        } else {
            // Remove any dashes, spaces, or other non-digit characters for consistency of database 
            phoneNumber = phoneNumber.replaceAll("[^\\d]", "");
            if (phoneNumber.isEmpty()) {
                System.out.println("Error: Phone number must contain 10 digits.");
                return;
            }
            // Validate it contains exactly 10 digits
            if (phoneNumber.length() != 10) {
                System.out.println("Error: Phone number must be exactly 10 digits.");
                return;
            }
            // Validate it contains only digits (after removing non-digits)
            if (!phoneNumber.matches("\\d{10}")) {
                System.out.println("Error: Phone number must contain only digits.");
                return;
            }
        }
        
        //Email is optional 
        System.out.print("Enter email (optional, press Enter to skip): ");
        String email = scanner.nextLine().trim();
        if (email.isEmpty()) {
            email = null;
        } else {
            if (!isValidEmail(email)) { //uses isValidEmail helper method for proper email format 
                System.out.println("Error: Invalid email format. Email must contain @ symbol and a domain (e.g., .com).");
                return;
            }
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

    //Helper method to get integer input from user while also checking if the input is valid 
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

    // Helper method to calculate end date based on plan type
    private static java.sql.Date calculateEndDate(java.sql.Date startDate, String planType) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(startDate);
        
        if (planType.equals("Monthly") || planType.equals("Monthly Premium")) {
            cal.add(java.util.Calendar.MONTH, 1);
        } else if (planType.equals("Annual")) {
            cal.add(java.util.Calendar.YEAR, 1);
        }
        
        return new java.sql.Date(cal.getTimeInMillis());
    }

    // Helper method to check if member has an active membership
    private static boolean hasActiveMembership(Connection conn, int memberID) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Membership WHERE memberID = ? AND status = 'Active'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Helper method to display available plans
    private static void displayPlans(Connection conn) throws SQLException {
        String sql = "SELECT p.planID, pt.planType, pt.price " +
                     "FROM Plan p " +
                     "JOIN PlanTypeInfo pt ON p.planType = pt.planType " +
                     "ORDER BY p.planID";
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\nAvailable Plans:");
            
            // Collect all data first to determine column widths
            java.util.List<String[]> rows = new java.util.ArrayList<>();
            boolean hasPlans = false;
            
            while (rs.next()) {
                hasPlans = true;
                int planID = rs.getInt("planID");
                String planType = rs.getString("planType");
                double price = rs.getDouble("price");
                rows.add(new String[]{
                    String.valueOf(planID),
                    planType,
                    "$" + String.format("%.2f", price)
                });
            }
            
            if (!hasPlans) {
                System.out.println("No plans available in the database.");
                return;
            }
            
            // Calculate column widths
            int[] colWidths = {0, 0, 0}; // temporary column widths
            String[] headers = {"Plan ID", "Plan Type", "Price"};
            
            // Adjust widths based on actual data
            for (int i = 0; i < headers.length; i++) {
                colWidths[i] = Math.max(colWidths[i], headers[i].length());
            }
            for (String[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    colWidths[i] = Math.max(colWidths[i], row[i].length());
                }
            }
            
            // Display output
            DatabaseViews.printTableRow(headers, colWidths); //Uses printTableRow method from DatabaseViews.java file 
            DatabaseViews.printTableSeparator(colWidths); //Uses printTableSeparator method from DatabaseViews.java file 
            
            // Print data rows
            for (String[] row : rows) {
                DatabaseViews.printTableRow(row, colWidths);
            }
        }
    }

    // Helper method to display only desk staff members to process a gym member's membership purchase 
    private static void displayDeskStaff(Connection conn) throws SQLException {
        String sql = "SELECT d.staffID, sm.firstName, sm.lastName " +
                     "FROM Desk d " +
                     "INNER JOIN StaffMember sm ON d.staffID = sm.staffID " +
                     "ORDER BY d.staffID";
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\nAvailable Desk Staff:");
            
            // Collect all data first to determine column widths
            java.util.List<String[]> rows = new java.util.ArrayList<>();
            boolean hasDeskStaff = false;
            
            while (rs.next()) { //loop to get all the desk staff members from database 
                hasDeskStaff = true;
                int staffID = rs.getInt("staffID");
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                rows.add(new String[]{
                    String.valueOf(staffID),
                    firstName + " " + lastName
                });
            }
            
            if (!hasDeskStaff) {
                System.out.println("No desk staff available in the database.");
                return;
            }
            
            // Calculate column widths for helper methods
            int[] colWidths = {0, 0}; // temporary column widths
            String[] headers = {"Staff ID", "Name"};
            
            // Adjust widths based on actual data
            for (int i = 0; i < headers.length; i++) {
                colWidths[i] = Math.max(colWidths[i], headers[i].length());
            }
            for (String[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    colWidths[i] = Math.max(colWidths[i], row[i].length());
                }
            }
            
            // Display output
            DatabaseViews.printTableRow(headers, colWidths); //Uses printTableRow method from DatabaseViews.java file 
            DatabaseViews.printTableSeparator(colWidths); //Uses printTableSeparator method from DatabaseViews.java file 
            
            // Print data rows
            for (String[] row : rows) {
                DatabaseViews.printTableRow(row, colWidths);
            }
        }
    }

    // Helper method to verify member age is >= 16
    private static boolean verifyMemberAge(Connection conn, int memberID) throws SQLException {
        String sql = "SELECT birthday FROM GymMember WHERE memberID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    java.sql.Date birthday = rs.getDate("birthday");
                    if (birthday != null) {
                        int age = calculateAge(birthday);
                        return age >= 16;
                    }
                }
            }
        }
        return false;
    }

    //Case 14 of the main menu. This method has all the logic for inserting a new membership for a new gym member 
    public static void purchaseMembership(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\n=== Purchase Membership ===");
        System.out.println();
        
        // Disable auto-commit for transaction
        boolean originalAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        
        try {
            //Get member ID
            int memberID = getIntInput(scanner, "Enter member ID: ");
            if (memberID <= 0) {
                System.out.println("Error: Invalid member ID.");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            
            // Verify member exists in GymMember table
            String checkMemberSql = "SELECT memberID, firstName, lastName FROM GymMember WHERE memberID = ?";
            String memberName = "";
            try (PreparedStatement ps = conn.prepareStatement(checkMemberSql)) {
                ps.setInt(1, memberID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Error: Member ID " + memberID + " does not exist.");
                        conn.rollback();
                        conn.setAutoCommit(originalAutoCommit);
                        return;
                    }
                    memberName = rs.getString("firstName") + " " + rs.getString("lastName");
                }
            }
            
            //Verify member age >= 16 else rollback and show error message 
            if (!verifyMemberAge(conn, memberID)) {
                System.out.println("Error: Member must be at least 16 years old to purchase a membership.");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            
            //Check for existing active membership
            if (hasActiveMembership(conn, memberID)) {
                System.out.println("Error: Member already has an Active membership.");
                System.out.println("   A member can only have one Active membership at a time.");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            
            //Display and select plan
            displayPlans(conn);
            int planID = getIntInput(scanner, "\nEnter plan ID: ");
            if (planID <= 0) {
                System.out.println("Error: Invalid plan ID.");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            
            // Get plan details (planType and price)
            String planType = null;
            double planPrice = 0.0;
            String getPlanSql = "SELECT pt.planType, pt.price " +
                               "FROM Plan p " +
                               "JOIN PlanTypeInfo pt ON p.planType = pt.planType " +
                               "WHERE p.planID = ?";
            try (PreparedStatement ps = conn.prepareStatement(getPlanSql)) {
                ps.setInt(1, planID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Error: Plan ID " + planID + " does not exist.");
                        conn.rollback();
                        conn.setAutoCommit(originalAutoCommit);
                        return;
                    }
                    planType = rs.getString("planType");
                    planPrice = rs.getDouble("price");
                }
            }
            
            //Get start date (default to today if no input is provided)
            System.out.print("Enter start date (YYYY-MM-DD, press Enter for today): ");
            String startDateStr = scanner.nextLine().trim();
            java.sql.Date startDate;
            if (startDateStr.isEmpty()) {
                startDate = new java.sql.Date(System.currentTimeMillis());
            } else {
                if (!startDateStr.matches("\\d{4}-\\d{2}-\\d{2}")) { //validates date format 
                    System.out.println("Error: Invalid date format. Please use YYYY-MM-DD format.");
                    conn.rollback();
                    conn.setAutoCommit(originalAutoCommit);
                    return;
                }
                try {
                    startDate = java.sql.Date.valueOf(startDateStr);
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: Invalid date.");
                    conn.rollback();
                    conn.setAutoCommit(originalAutoCommit);
                    return;
                }
            }
            
            // Calculate end date based on plan type
            java.sql.Date endDate = calculateEndDate(startDate, planType);
            
            //Display and select desk staff
            displayDeskStaff(conn);
            int staffID = getIntInput(scanner, "\nEnter desk staff ID (who is processing the payment): ");
            if (staffID <= 0) {
                System.out.println("Error: Invalid staff ID.");
                conn.rollback();
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
                        conn.setAutoCommit(originalAutoCommit);
                        return;
                    }
                }
            }
            
            //Get payment type
            System.out.println("\nPayment Types:");
            System.out.println("1. CASH");
            System.out.println("2. CARD");
            System.out.println("3. ONLINE");
            int paymentChoice = getIntInput(scanner, "Select payment type (1-3): ");
            
            String paymentType = null;
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
                    conn.setAutoCommit(originalAutoCommit);
                    return;
            }
            
            //Create membership with status 'Active' in Membership table and thorws exception if theres an error
            String insertMembershipSql = "INSERT INTO Membership (memberID, planID, startDate, endDate, status) " +
                                        "VALUES (?, ?, ?, ?, 'Active')";
            try (PreparedStatement ps = conn.prepareStatement(insertMembershipSql)) {
                ps.setInt(1, memberID);
                ps.setInt(2, planID);
                ps.setDate(3, startDate);
                ps.setDate(4, endDate);
                
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Failed to create membership.");
                }
            }
            
            //Create payment with status 'Success' and throws exception if theres an error
            String insertPaymentSql = "INSERT INTO Payment (staffID, memberID, amount, paymentType, status) " +
                                     "VALUES (?, ?, ?, ?, 'Success')";
            try (PreparedStatement ps = conn.prepareStatement(insertPaymentSql)) {
                ps.setInt(1, staffID);
                ps.setInt(2, memberID);
                ps.setDouble(3, planPrice);
                ps.setString(4, paymentType);
                
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Failed to create payment.");
                }
            }
            
            //Commit transaction
            conn.commit();
            System.out.println("\n Success! Membership purchased and activated.");
            System.out.println("  Member: " + memberName + " (ID: " + memberID + ")");
            System.out.println("  Plan: " + planType + " ($" + String.format("%.2f", planPrice) + ")");
            System.out.println("  Start Date: " + startDate);
            System.out.println("  End Date: " + endDate);
            System.out.println("  Payment: " + paymentType + " - Success");
            
        } catch (SQLException e) {
            // Rollback on any error
            conn.rollback();
            System.out.println("\n✗ Error: Transaction failed. No changes were made.");
            System.out.println("  " + e.getMessage());
            throw e;
        } finally {
            // Restore original auto-commit setting
            conn.setAutoCommit(originalAutoCommit);
        }
    }


    //Case 15 of the main menu. This method has all the logic for inserting a new staff member into the databse 
    public static void insertStaffMember(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\n=== Insert New Staff Member ===");
        System.out.println();
        
        // Disable auto-commit for transaction
        boolean originalAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        
        try {
            //Get shared staff attributes
            String firstName = getStringInput(scanner, "Enter first name: ");
            if (firstName.isEmpty()) {
                System.out.println("Error: First name is required.");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            if (containsNumbers(firstName)) {
                System.out.println("Error: First name cannot contain numbers.");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            
            String lastName = getStringInput(scanner, "Enter last name: ");
            if (lastName.isEmpty()) {
                System.out.println("Error: Last name is required.");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            if (containsNumbers(lastName)) {
                System.out.println("Error: Last name cannot contain numbers.");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            
            System.out.print("Enter phone number (optional, 10 digits only, press Enter to skip): ");
            String phoneNumber = scanner.nextLine().trim();
            if (phoneNumber.isEmpty()) {
                phoneNumber = null;
            } else { //Checks for cases where the phone number is not inputted properly 
                // Remove any dashes, spaces, or other non-digit characters
                phoneNumber = phoneNumber.replaceAll("[^\\d]", "");
                if (phoneNumber.isEmpty()) {
                    System.out.println("Error: Phone number must contain 10 digits.");
                    conn.rollback();
                    conn.setAutoCommit(originalAutoCommit);
                    return;
                }
                // Validate it contains exactly 10 digits
                if (phoneNumber.length() != 10) {
                    System.out.println("Error: Phone number must be exactly 10 digits.");
                    conn.rollback();
                    conn.setAutoCommit(originalAutoCommit);
                    return;
                }
                // Validate it contains only digits (after removing non-digits)
                if (!phoneNumber.matches("\\d{10}")) {
                    System.out.println("Error: Phone number must contain only digits.");
                    conn.rollback();
                    conn.setAutoCommit(originalAutoCommit);
                    return;
                }
            }
            
            System.out.print("Enter email (optional, press Enter to skip): ");
            String email = scanner.nextLine().trim();
            if (email.isEmpty()) {
                email = null;
            }
            
            String hireDateStr = getDateInput(scanner, "Enter hire date (required)");
            if (hireDateStr.isEmpty()) { //Case where the hire date is empty 
                System.out.println("Error: Hire date is required.");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            if (!hireDateStr.matches("\\d{4}-\\d{2}-\\d{2}")) { //case where the format of the hire date is wrong 
                System.out.println("Error: Invalid date format. Please use YYYY-MM-DD format.");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            java.sql.Date hireDate;
            try {
                hireDate = java.sql.Date.valueOf(hireDateStr);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: Invalid date.");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            
            System.out.print("Enter salary (required, decimal >= 0): ");
            String salaryStr = scanner.nextLine().trim();
            if (salaryStr.isEmpty()) { //if salary is empty, rollback and show error message 
                System.out.println("Error: Salary is required.");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            Double salary;
            try {
                salary = Double.parseDouble(salaryStr);
                if (salary < 0) { //if salary is a negative number, rollback and show error message
                    System.out.println("Error: Salary must be >= 0.");
                    conn.rollback();
                    conn.setAutoCommit(originalAutoCommit);
                    return;
                }
            } catch (NumberFormatException e) { //if the salary is not a number, rollback and show error message
                System.out.println("Error: Invalid salary format.");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            
            //Make prepared statemetns to insert the new staff member into the staff member table as well 
            String insertStaffSql = "INSERT INTO StaffMember (firstName, lastName, phoneNumber, email, hireDate, salary) " +
                                   "VALUES (?, ?, ?, ?, ?, ?)";
            int staffID = 0;
            try (PreparedStatement ps = conn.prepareStatement(insertStaffSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, firstName);
                ps.setString(2, lastName);
                
                if (phoneNumber != null) {
                    ps.setString(3, phoneNumber);
                } else {
                    ps.setNull(3, Types.VARCHAR);
                }
                
                if (email != null) {
                    ps.setString(4, email);
                } else {
                    ps.setNull(4, Types.VARCHAR);
                }
                
                ps.setDate(5, hireDate);
                ps.setDouble(6, salary);
                
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Failed to create staff member.");
                }
                
                // Get the generated staffID
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        staffID = generatedKeys.getInt(1);
                    }
                }
            }
            
            // Ask for staff role/sub-entity of staff table 
            System.out.println("\nSelect staff role:");
            System.out.println("1. Desk Staff");
            System.out.println("2. Trainer");
            System.out.println("3. Manager");
            int roleChoice = getIntInput(scanner, "Enter your choice (1-3): ");
            
            if (roleChoice < 1 || roleChoice > 3) { //ensure user types the right numbers 
                System.out.println("Error: Invalid role selection. Must be 1, 2, or 3.");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            
            if (roleChoice == 1) { //if the new staff member is taking a desk staff role 
                // Insert into Desk
                System.out.print("Enter schedule (required, format: Mon- 9AM-5PM or Mon-Fri 9AM-5PM): ");
                String schedule = scanner.nextLine().trim();
                if (schedule.isEmpty()) { //Case where the scuedle attribute is empty 
                    System.out.println("Error: Schedule is required.");
                    conn.rollback();
                    conn.setAutoCommit(originalAutoCommit);
                    return;
                }
                if (!isValidScheduleFormat(schedule)) { //case where the schedule format is invalid 
                    System.out.println("Error: Invalid schedule format. Use 'Mon- 9AM-5PM' for single day or 'Mon-Fri 9AM-5PM' for multiple days.");
                    conn.rollback();
                    conn.setAutoCommit(originalAutoCommit);
                    return;
                }
                
                String deskLocation = getStringInput(scanner, "Enter desk location: ");
                if (deskLocation.isEmpty()) {
                    System.out.println("Error: Desk location is required.");
                    conn.rollback();
                    conn.setAutoCommit(originalAutoCommit);
                    return;
                }
                
                String responsibility = getStringInput(scanner, "Enter responsibility: ");
                if (responsibility.isEmpty()) {
                    System.out.println("Error: Responsibility is required.");
                    conn.rollback();
                    conn.setAutoCommit(originalAutoCommit);
                    return;
                }
                
                //Make prepared statemetns to insert the new staff member into the desk table 
                String insertDeskSql = "INSERT INTO Desk (staffID, schedule, deskLocation, responsibility) " +
                                      "VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertDeskSql)) {
                    ps.setInt(1, staffID);
                    ps.setString(2, schedule);
                    ps.setString(3, deskLocation);
                    ps.setString(4, responsibility);
                    
                    int rowsAffected = ps.executeUpdate();
                    if (rowsAffected == 0) {
                        throw new SQLException("Failed to create desk staff record.");
                    }
                }
                
            } else if (roleChoice == 2) { //If the new staff member is taking a trainer role 
                // Insert into Trainer table with respective attributes 
                System.out.print("Enter specialty (optional, press Enter to skip): ");
                String specialty = scanner.nextLine().trim();
                if (specialty.isEmpty()) {
                    specialty = null;
                }
                
                System.out.print("Enter schedule (required, format: Mon- 9AM-5PM or Mon-Fri 9AM-5PM): ");
                String schedule = scanner.nextLine().trim();
                if (schedule.isEmpty()) {
                    System.out.println("Error: Schedule is required.");
                    conn.rollback();
                    conn.setAutoCommit(originalAutoCommit);
                    return;
                }
                if (!isValidScheduleFormat(schedule)) {
                    System.out.println("Error: Invalid schedule format. Use 'Mon- 9AM-5PM' for single day or 'Mon-Fri 9AM-5PM' for multiple days.");
                    conn.rollback();
                    conn.setAutoCommit(originalAutoCommit);
                    return;
                }
                
                String certificationLevel = getStringInput(scanner, "Enter certification level (ex: Beginner, Intermediate, Advanced): ");
                if (certificationLevel.isEmpty()) {
                    System.out.println("Error: Certification level is required.");
                    conn.rollback();
                    conn.setAutoCommit(originalAutoCommit);
                    return;
                }
                
                int experience = getIntInput(scanner, "Enter experience (years): ");
                if (experience < 0) {
                    System.out.println("Error: Experience must be >= 0.");
                    conn.rollback();
                    conn.setAutoCommit(originalAutoCommit);
                    return;
                }
                

                //Make prepared statemetns to insert the new staff member into the trainer table 
                String insertTrainerSql = "INSERT INTO Trainer (staffID, specialty, schedule, certificationLevel, experience) " +
                                         "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertTrainerSql)) {
                    ps.setInt(1, staffID);
                    if (specialty != null) {
                        ps.setString(2, specialty);
                    } else {
                        ps.setNull(2, Types.VARCHAR);
                    }
                    ps.setString(3, schedule);
                    ps.setString(4, certificationLevel);
                    ps.setInt(5, experience);
                    
                    int rowsAffected = ps.executeUpdate();
                    if (rowsAffected == 0) {
                        throw new SQLException("Failed to create trainer record.");
                    }
                }
                
            } else if (roleChoice == 3) { //if the new staff member is taking a manager role
                // Insert into Manager
                String department = getStringInput(scanner, "Enter department: ");
                if (department.isEmpty()) {
                    System.out.println("Error: Department is required.");
                    conn.rollback();
                    conn.setAutoCommit(originalAutoCommit);
                    return;
                }
                
                String officeLocation = getStringInput(scanner, "Enter office location: ");
                if (officeLocation.isEmpty()) {
                    System.out.println("Error: Office location is required.");
                    conn.rollback();
                    conn.setAutoCommit(originalAutoCommit);
                    return;
                }
                
                System.out.print("Enter experience (optional, press Enter to skip): ");
                String experienceStr = scanner.nextLine().trim();
                Integer experience = null;
                if (!experienceStr.isEmpty()) {
                    try {
                        experience = Integer.parseInt(experienceStr);
                        if (experience < 0) {
                            System.out.println("Error: Experience must be >= 0.");
                            conn.rollback();
                            conn.setAutoCommit(originalAutoCommit);
                            return;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Invalid experience format.");
                        conn.rollback();
                        conn.setAutoCommit(originalAutoCommit);
                        return;
                    }
                }
                
                //Make prepared statemetns to insert the new staff member into the manager table 
                String insertManagerSql = "INSERT INTO Manager (staffID, department, officeLocation, experience) " +
                                         "VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertManagerSql)) {
                    ps.setInt(1, staffID);
                    ps.setString(2, department);
                    ps.setString(3, officeLocation);
                    if (experience != null) {
                        ps.setInt(4, experience);
                    } else {
                        ps.setNull(4, Types.INTEGER);
                    }
                    
                    int rowsAffected = ps.executeUpdate();
                    if (rowsAffected == 0) {
                        throw new SQLException("Failed to create manager record.");
                    }
                }
            }
            
            // Step 4: Commit transaction
            conn.commit();
            System.out.println("\n✓ Success! Staff member '" + firstName + " " + lastName + "' has been added to the database.");
            System.out.println("  Staff ID: " + staffID);
            if (roleChoice == 1) {
                System.out.println("  Role: Desk Staff");
            } else if (roleChoice == 2) {
                System.out.println("  Role: Trainer");
            } else if (roleChoice == 3) {
                System.out.println("  Role: Manager");
            }
            
        } catch (SQLException e) {
            // Rollback on any error
            conn.rollback();
            
            // Check for unique constraint violations
            if (e.getErrorCode() == 1062) { // MySQL duplicate entry error code
                String errorMessage = e.getMessage();
                if (errorMessage.contains("phoneNumber") || errorMessage.contains("phone")) {
                    System.out.println("\n✗ Error: A staff member with this phone number already exists.");
                    System.out.println("   Phone numbers must be unique.");
                } else if (errorMessage.contains("email")) {
                    System.out.println("\n✗ Error: A staff member with this email already exists.");
                    System.out.println("   Email addresses must be unique.");
                } else if (errorMessage.contains("officeLocation")) {
                    System.out.println("\n✗ Error: A manager with this office location already exists.");
                    System.out.println("   Office locations must be unique.");
                } else {
                    System.out.println("\n✗ Error: Duplicate entry detected. " + e.getMessage());
                }
            } else {
                System.out.println("\n✗ Error: Transaction failed. No changes were made.");
                System.out.println("  " + e.getMessage());
            }
            throw e;
        } finally {
            // Restore original auto-commit setting
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    // Insert a new Plan (adds to Plan table using existing PlanTypeInfo)
    public static void insertPlan(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\n=== Insert New Plan ===");
        System.out.println();
        
        // First, show existing plan types
        System.out.println("Available Plan Types (from PlanTypeInfo):");
        String showTypesSql = "SELECT planType, price FROM PlanTypeInfo ORDER BY price";
        try (PreparedStatement ps = conn.prepareStatement(showTypesSql);
             ResultSet rs = ps.executeQuery()) {
            
            java.util.List<String[]> rows = new java.util.ArrayList<>();
            boolean hasTypes = false;
            
            while (rs.next()) {
                hasTypes = true;
                rows.add(new String[]{
                    rs.getString("planType"),
                    String.format("$%.2f", rs.getDouble("price"))
                });
            }
            
            if (!hasTypes) {
                System.out.println("No plan types exist in the database.");
                System.out.println("Would you like to create a plan type first? (y/n): ");
                String choice = scanner.nextLine().trim().toLowerCase();
                if (choice.equals("y") || choice.equals("yes")) {
                    insertPlanType(conn, scanner);
                    // Re-run this method after creating plan type
                    insertPlan(conn, scanner);
                }
                return;
            }
            
            int[] colWidths = {0, 0};
            String[] headers = {"Plan Type", "Price"};
            
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
        System.out.println("Select plan type to create a new plan:");
        System.out.println("1. Monthly");
        System.out.println("2. Monthly Premium");
        System.out.println("3. Annual");
        System.out.println("0. Cancel");
        
        int choice = getIntInput(scanner, "Enter your choice: ");
        
        String planType = "";
        switch (choice) {
            case 1:
                planType = "Monthly";
                break;
            case 2:
                planType = "Monthly Premium";
                break;
            case 3:
                planType = "Annual";
                break;
            case 0:
                System.out.println("Cancelled.");
                return;
            default:
                System.out.println("Invalid choice.");
                return;
        }
        
        // Insert new plan with the selected plan type
        String insertSql = "INSERT INTO Plan (planType) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, planType);
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int planID = generatedKeys.getInt(1);
                        System.out.println("\n✓ Success! New plan created.");
                        System.out.println("  Plan ID: " + planID);
                        System.out.println("  Plan Type: " + planType);
                    }
                }
            } else {
                System.out.println("\nError: Failed to create plan.");
            }
        }
    }

    // Insert a new PlanType (for setting up the database initially)
    public static void insertPlanType(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\n=== Insert New Plan Type ===");
        System.out.println();
        System.out.println("Note: Plan types are predefined as ENUM values in the database.");
        System.out.println("Available plan types: Monthly, Monthly Premium, Annual");
        System.out.println();
        
        // Show existing plan types
        System.out.println("Current Plan Types in database:");
        String showTypesSql = "SELECT planType, price FROM PlanTypeInfo ORDER BY price";
        try (PreparedStatement ps = conn.prepareStatement(showTypesSql);
             ResultSet rs = ps.executeQuery()) {
            
            boolean hasTypes = false;
            while (rs.next()) {
                hasTypes = true;
                System.out.println("  - " + rs.getString("planType") + ": $" + String.format("%.2f", rs.getDouble("price")));
            }
            
            if (!hasTypes) {
                System.out.println("  (none)");
            }
        }
        
        System.out.println();
        System.out.println("Select plan type to add:");
        System.out.println("1. Monthly");
        System.out.println("2. Monthly Premium");
        System.out.println("3. Annual");
        System.out.println("0. Cancel");
        
        int choice = getIntInput(scanner, "Enter your choice: ");
        
        String planType = "";
        switch (choice) {
            case 1:
                planType = "Monthly";
                break;
            case 2:
                planType = "Monthly Premium";
                break;
            case 3:
                planType = "Annual";
                break;
            case 0:
                System.out.println("Cancelled.");
                return;
            default:
                System.out.println("Invalid choice.");
                return;
        }
        
        // Get price for the plan type
        System.out.print("Enter price for " + planType + " plan (e.g., 29.99): $");
        String priceStr = scanner.nextLine().trim();
        if (priceStr.isEmpty()) {
            System.out.println("Error: Price is required.");
            return;
        }
        
        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                System.out.println("Error: Price must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid price format.");
            return;
        }
        
        // Insert new plan type
        String insertSql = "INSERT INTO PlanTypeInfo (planType, price) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, planType);
            ps.setDouble(2, price);
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("\n✓ Success! Plan type created.");
                System.out.println("  Plan Type: " + planType);
                System.out.println("  Price: $" + String.format("%.2f", price));
            } else {
                System.out.println("\nError: Failed to create plan type.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry
                System.out.println("\n✗ Error: This plan type already exists.");
            } else {
                throw e;
            }
        }
    }

    // Setup all default plans (convenience method for initial database setup)
    public static void setupDefaultPlans(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\n=== Setup Default Plans ===");
        System.out.println();
        System.out.println("This will create the default plan types and plans for the gym.");
        System.out.println("Default plan types:");
        System.out.println("  - Monthly: $29.99");
        System.out.println("  - Monthly Premium: $49.99");
        System.out.println("  - Annual: $299.99");
        System.out.println();
        System.out.print("Do you want to proceed? (y/n): ");
        
        String choice = scanner.nextLine().trim().toLowerCase();
        if (!choice.equals("y") && !choice.equals("yes")) {
            System.out.println("Setup cancelled.");
            return;
        }
        
        // Disable auto-commit for transaction
        boolean originalAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        
        try {
            // Insert plan types (ignore if already exist)
            String insertTypeSql = "INSERT IGNORE INTO PlanTypeInfo (planType, price) VALUES (?, ?)";
            
            try (PreparedStatement ps = conn.prepareStatement(insertTypeSql)) {
                // Monthly
                ps.setString(1, "Monthly");
                ps.setDouble(2, 29.99);
                ps.executeUpdate();
                
                // Monthly Premium
                ps.setString(1, "Monthly Premium");
                ps.setDouble(2, 49.99);
                ps.executeUpdate();
                
                // Annual
                ps.setString(1, "Annual");
                ps.setDouble(2, 299.99);
                ps.executeUpdate();
            }
            
            // Insert plans for each type
            String insertPlanSql = "INSERT INTO Plan (planType) VALUES (?)";
            try (PreparedStatement ps = conn.prepareStatement(insertPlanSql)) {
                ps.setString(1, "Monthly");
                ps.executeUpdate();
                
                ps.setString(1, "Monthly Premium");
                ps.executeUpdate();
                
                ps.setString(1, "Annual");
                ps.executeUpdate();
            }
            
            conn.commit();
            System.out.println("\n✓ Success! Default plans have been set up.");
            System.out.println("  Created 3 plan types and 3 plans.");
            
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("\n✗ Error: Setup failed. " + e.getMessage());
            throw e;
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }
}

