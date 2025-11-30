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

    // Helper method to validate email format
    private static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return true; // Optional field
        }
        // Basic email validation: must contain @ and at least one dot after @
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
        System.out.print("Enter phone number (optional, 10 digits only, press Enter to skip): ");
        String phoneNumber = scanner.nextLine().trim();
        if (phoneNumber.isEmpty()) {
            phoneNumber = null;
        } else {
            // Remove any dashes, spaces, or other non-digit characters
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
        
        System.out.print("Enter email (optional, press Enter to skip): ");
        String email = scanner.nextLine().trim();
        if (email.isEmpty()) {
            email = null;
        } else {
            if (!isValidEmail(email)) {
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
                     "JOIN PlanType pt ON p.planType = pt.planType " +
                     "ORDER BY p.planID";
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\nAvailable Plans:");
            System.out.println("+----------+------------------+----------+");
            System.out.println("| Plan ID  | Plan Type        | Price    |");
            System.out.println("+----------+------------------+----------+");
            
            while (rs.next()) {
                int planID = rs.getInt("planID");
                String planType = rs.getString("planType");
                double price = rs.getDouble("price");
                System.out.printf("| %-8d | %-16s | $%-7.2f |%n", planID, planType, price);
            }
            System.out.println("+----------+------------------+----------+");
        }
    }

    // Helper method to display desk staff
    private static void displayDeskStaff(Connection conn) throws SQLException {
        String sql = "SELECT d.staffID, sm.firstName, sm.lastName " +
                     "FROM Desk d " +
                     "INNER JOIN StaffMember sm ON d.staffID = sm.staffID " +
                     "ORDER BY d.staffID";
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\nAvailable Desk Staff:");
            System.out.println("+----------+------------------+");
            System.out.println("| Staff ID | Name             |");
            System.out.println("+----------+------------------+");
            
            while (rs.next()) {
                int staffID = rs.getInt("staffID");
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                System.out.printf("| %-8d | %-16s |%n", staffID, firstName + " " + lastName);
            }
            System.out.println("+----------+------------------+");
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

    public static void purchaseMembership(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\n=== Purchase Membership ===");
        System.out.println();
        
        // Disable auto-commit for transaction
        boolean originalAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        
        try {
            // Step 1: Get member ID
            int memberID = getIntInput(scanner, "Enter member ID: ");
            if (memberID <= 0) {
                System.out.println("Error: Invalid member ID.");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            
            // Verify member exists
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
            
            // Step 2: Verify member age >= 16
            if (!verifyMemberAge(conn, memberID)) {
                System.out.println("Error: Member must be at least 16 years old to purchase a membership.");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            
            // Step 3: Check for existing active membership
            if (hasActiveMembership(conn, memberID)) {
                System.out.println("Error: Member already has an Active membership.");
                System.out.println("   A member can only have one Active membership at a time.");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            
            // Step 4: Display and select plan
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
                               "JOIN PlanType pt ON p.planType = pt.planType " +
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
            
            // Step 5: Get start date (default to today)
            System.out.print("Enter start date (YYYY-MM-DD, press Enter for today): ");
            String startDateStr = scanner.nextLine().trim();
            java.sql.Date startDate;
            if (startDateStr.isEmpty()) {
                startDate = new java.sql.Date(System.currentTimeMillis());
            } else {
                if (!startDateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
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
            
            // Step 6: Display and select desk staff
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
            
            // Step 7: Get payment type
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
            
            // Step 8: Create membership with status 'Active'
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
            
            // Step 9: Create payment with status 'Success'
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
            
            // Step 10: Commit transaction
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

    public static void insertStaffMember(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\n=== Insert New Staff Member ===");
        System.out.println();
        
        // Disable auto-commit for transaction
        boolean originalAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        
        try {
            // Step 1: Get shared staff information
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
            } else {
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
            if (hireDateStr.isEmpty()) {
                System.out.println("Error: Hire date is required.");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            if (!hireDateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
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
            if (salaryStr.isEmpty()) {
                System.out.println("Error: Salary is required.");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            Double salary;
            try {
                salary = Double.parseDouble(salaryStr);
                if (salary < 0) {
                    System.out.println("Error: Salary must be >= 0.");
                    conn.rollback();
                    conn.setAutoCommit(originalAutoCommit);
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid salary format.");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            
            // Step 2: Insert into StaffMember
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
            
            // Step 3: Ask for role/sub-entity
            System.out.println("\nSelect staff role:");
            System.out.println("1. Desk Staff");
            System.out.println("2. Trainer");
            System.out.println("3. Manager");
            int roleChoice = getIntInput(scanner, "Enter your choice (1-3): ");
            
            if (roleChoice < 1 || roleChoice > 3) {
                System.out.println("Error: Invalid role selection. Must be 1, 2, or 3.");
                conn.rollback();
                conn.setAutoCommit(originalAutoCommit);
                return;
            }
            
            if (roleChoice == 1) {
                // Insert into Desk
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
                
            } else if (roleChoice == 2) {
                // Insert into Trainer
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
                
            } else if (roleChoice == 3) {
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
}

