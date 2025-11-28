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
        } catch (ClassNotFoundException e) {
            System.out.println("Error: MySQL JDBC Driver not found.");
            System.out.println("Make sure mysql-connector-j is on the classpath.");
            return;
        } catch (SQLException e) {
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
                        viewGymMembers();
                        break;
                    case 2:
                        viewAllMemberships();
                        break;
                    case 3:
                        viewStaffMembers();
                        break;
                    case 4:
                        viewPlans();
                        break;
                    case 5:
                        viewPayments();
                        break;
                    case 6:
                        viewCheckIns();
                        break;
                    case 7:
                        viewActiveMembers();
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
        System.out.println("\nEnter the number of the option you want to select:");
        System.out.println("1. View Gym Members");
        System.out.println("2. View All Memberships");
        System.out.println("3. View Staff Members");
        System.out.println("4. View Plans");
        System.out.println("5. View Payments");
        System.out.println("6. View Check-Ins");
        System.out.println("7. View Active Members");
        System.out.println("0. Exit");
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
    

    //Used to print the table rows in a organized way 
    private static void printTableRow(String[] values, int[] widths) {
        System.out.print("|");
        for (int i = 0; i < values.length; i++) {
            System.out.printf(" %-" + widths[i] + "s |", values[i]);
        }
        System.out.println();
    }
    

    //Used to make the columns in the table much more organized 
    private static void printTableSeparator(int[] widths) {
        System.out.print("+");
        for (int width : widths) {
            for (int i = 0; i < width + 2; i++) {
                System.out.print("-");
            }
            System.out.print("+");
        }
        System.out.println();
    }


    //case 1 function to view the gym members and their memberships
    private static void viewGymMembers() throws SQLException {
        String sql = "SELECT gm.memberID, gm.firstName, gm.lastName, " +
                     "       gm.birthday, gm.phoneNumber, gm.email, gm.dateJoined " +
                     "FROM GymMember gm " +
                     "ORDER BY gm.memberID;";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\n=== Gym Members ===");
            System.out.println();
            System.out.println("This table shows all the gym members and their details, including the member ID, first name, last name, birthday, phone number, email, and date joined.");

            // Collect all data first to determine column widths
            java.util.List<String[]> rows = new java.util.ArrayList<>();
            boolean hasMembers = false;
            
            while (rs.next()) {
                hasMembers = true;
                int memberID = rs.getInt("memberID");
                String firstName = rs.getString("firstName") != null ? rs.getString("firstName") : "";
                String lastName = rs.getString("lastName") != null ? rs.getString("lastName") : "";
                
                String birthday;
                java.sql.Date birthdayDate = rs.getDate("birthday");
                if (rs.wasNull() || birthdayDate == null) {
                    birthday = "(null)";
                } else {
                    birthday = birthdayDate.toString();
                }
                
                String phoneNumber = rs.getString("phoneNumber");
                if (phoneNumber == null) {
                    phoneNumber = "(null)";
                }
                
                String email = rs.getString("email");
                if (email == null) {
                    email = "(null)";
                }
                
                String dateJoined;
                java.sql.Date dateJoinedDate = rs.getDate("dateJoined");
                if (rs.wasNull() || dateJoinedDate == null) {
                    dateJoined = "(null)";
                } else {
                    dateJoined = dateJoinedDate.toString();
                }
                
                rows.add(new String[]{
                    String.valueOf(memberID),
                    firstName,
                    lastName,
                    birthday,
                    phoneNumber,
                    email,
                    dateJoined
                });
            }
            
            if (!hasMembers) {
                System.out.println("No gym members in the database");
                return;
            }
            
            // Calculate column widths
            int[] colWidths = {10, 15, 15, 12, 15, 25, 12}; //temporary column widths
            String[] headers = {"Member ID", "First Name", "Last Name", "Birthday", "Phone Number", "Email", "Date Joined"};
            
            // Adjust widths based on actual data
            for (int i = 0; i < headers.length; i++) {
                colWidths[i] = Math.max(colWidths[i], headers[i].length());
            }
            for (String[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    colWidths[i] = Math.max(colWidths[i], row[i].length());
                }
            }
            
            //Display output 
            // Print table header
            printTableRow(headers, colWidths);
            printTableSeparator(colWidths);
            
            // Print data rows
            for (String[] row : rows) {
                printTableRow(row, colWidths);
            }
        }
    }
    
   

    private static void viewAllMemberships() throws SQLException {
        String sql = "SELECT ms.memberID, gm.firstName, gm.lastName, ms.status, " +
                     "       pt.planType, pt.price, ms.startDate, ms.endDate " +
                     "FROM Membership ms " +
                     "LEFT JOIN GymMember gm ON ms.memberID = gm.memberID " +
                     "LEFT JOIN Plan p ON ms.planID = p.planID " +
                     "LEFT JOIN PlanType pt ON p.planType = pt.planType " +
                     "ORDER BY ms.memberID;";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n=== All Memberships ===");
            System.out.println();
            System.out.println("This table shows all the memberships and their details, including the member ID, first name, last name, status, plan type, price, start date, and end date.");
            System.out.println();

            // Collect all data first to determine column widths
            java.util.List<String[]> rows = new java.util.ArrayList<>();
            boolean hasMemberships = false;
            
            while (rs.next()) {
                hasMemberships = true;
                int memberID = rs.getInt("memberID");
                
                String firstName = rs.getString("firstName");
                if (firstName == null) {
                    firstName = "(null)";
                }
                
                String lastName = rs.getString("lastName");
                if (lastName == null) {
                    lastName = "(null)";
                }
                
                String status = rs.getString("status");
                if (status == null) {
                    status = "(null)";
                }
                
                String planType = rs.getString("planType");
                if (planType == null) {
                    planType = "(none)";
                }
                
                String priceStr;
                Double price = rs.getDouble("price");
                if (rs.wasNull() || price == null) {
                    priceStr = "-";
                } else {
                    priceStr = String.format("$%.2f", price);
                }
                
                String startDate;
                java.sql.Date startDateObj = rs.getDate("startDate");
                if (rs.wasNull() || startDateObj == null) {
                    startDate = "(null)";
                } else {
                    startDate = startDateObj.toString();
                }
                
                String endDate;
                java.sql.Date endDateObj = rs.getDate("endDate");
                if (rs.wasNull() || endDateObj == null) {
                    endDate = "(null)";
                } else {
                    endDate = endDateObj.toString();
                }
                
                rows.add(new String[]{
                    String.valueOf(memberID),
                    firstName,
                    lastName,
                    status,
                    planType,
                    priceStr,
                    startDate,
                    endDate
                });
            }
            
            if (!hasMemberships) {
                System.out.println("No memberships in the database");
                return;
            }
            
            // Calculate column widths
            int[] colWidths = {10, 15, 15, 15, 15, 12, 12, 12}; //temporary column widths
            String[] headers = {"Member ID", "First Name", "Last Name", "Status", "Plan Type", "Price", "Start Date", "End Date"};
            
            // Adjust widths based on actual data
            for (int i = 0; i < headers.length; i++) {
                colWidths[i] = Math.max(colWidths[i], headers[i].length());
            }
            for (String[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    colWidths[i] = Math.max(colWidths[i], row[i].length());
                }
            }
            
            // Print table header
            printTableRow(headers, colWidths);
            printTableSeparator(colWidths);
            
            // Print data rows
            for (String[] row : rows) {
                printTableRow(row, colWidths);
            }
        }
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
                viewAllStaffMembers();
                break;
            case 2:
                viewDeskStaff();
                break;
            case 3:
                viewTrainers();
                break;
            case 4:
                viewManagers();
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid input.");
        }
    }

    private static void viewAllStaffMembers() throws SQLException {
        String sql = "SELECT sm.staffID, sm.firstName, sm.lastName, sm.phoneNumber, " +
                     "       sm.email, sm.hireDate, sm.salary, " +
                     "       CASE " +
                     "         WHEN d.staffID IS NOT NULL THEN 'Desk' " +
                     "         WHEN t.staffID IS NOT NULL THEN 'Trainer' " +
                     "         WHEN m.staffID IS NOT NULL THEN 'Manager' " +
                     "         ELSE 'None' " +
                     "       END AS role " +
                     "FROM StaffMember sm " +
                     "LEFT JOIN Desk d ON sm.staffID = d.staffID " +
                     "LEFT JOIN Trainer t ON sm.staffID = t.staffID " +
                     "LEFT JOIN Manager m ON sm.staffID = m.staffID " +
                     "ORDER BY sm.staffID;";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n=== All Staff Members ===");
            System.out.println();
            System.out.println("This table shows all the staff members and their details, including the staff ID, first name, last name, phone number, email, hire date, salary, and role.");
            System.out.println();
            java.util.List<String[]> rows = new java.util.ArrayList<>();
            boolean hasStaff = false;
            
            while (rs.next()) {
                hasStaff = true;
                int staffID = rs.getInt("staffID");
                String firstName = rs.getString("firstName") != null ? rs.getString("firstName") : "";
                String lastName = rs.getString("lastName") != null ? rs.getString("lastName") : "";
                String phoneNumber = rs.getString("phoneNumber");
                if (phoneNumber == null) {
                    phoneNumber = "(null)";
                }
                String email = rs.getString("email");
                if (email == null) {
                    email = "(null)";
                }
                String hireDate;
                java.sql.Date hireDateObj = rs.getDate("hireDate");
                if (rs.wasNull() || hireDateObj == null) {
                    hireDate = "(null)";
                } else {
                    hireDate = hireDateObj.toString();
                }
                String salaryStr;
                Double salary = rs.getDouble("salary");
                if (rs.wasNull() || salary == null) {
                    salaryStr = "-";
                } else {
                    salaryStr = String.format("$%.2f", salary);
                }
                String role = rs.getString("role");
                if (role == null) {
                    role = "None";
                }
                
                rows.add(new String[]{
                    String.valueOf(staffID),
                    firstName,
                    lastName,
                    phoneNumber,
                    email,
                    hireDate,
                    salaryStr,
                    role
                });
            }
            
            if (!hasStaff) {
                System.out.println("No staff members in the database");
                return;
            }
            
            int[] colWidths = {10, 15, 15, 15, 25, 12, 12, 10};
            String[] headers = {"Staff ID", "First Name", "Last Name", "Phone Number", "Email", "Hire Date", "Salary", "Role"};
            
            for (int i = 0; i < headers.length; i++) {
                colWidths[i] = Math.max(colWidths[i], headers[i].length());
            }
            for (String[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    colWidths[i] = Math.max(colWidths[i], row[i].length());
                }
            }
            
            printTableRow(headers, colWidths);
            printTableSeparator(colWidths);
            
            for (String[] row : rows) {
                printTableRow(row, colWidths);
            }
        }
    }

    private static void viewDeskStaff() throws SQLException {
        String sql = "SELECT sm.staffID, sm.firstName, sm.lastName, sm.phoneNumber, " +
                     "       sm.email, sm.hireDate, sm.salary, " +
                     "       d.schedule, d.deskLocation, d.responsibility " +
                     "FROM StaffMember sm " +
                     "INNER JOIN Desk d ON sm.staffID = d.staffID " +
                     "ORDER BY sm.staffID;";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n=== Desk Staff ===");
            System.out.println();
            System.out.println("This table shows all the desk staff and their details, including the staff ID, first name, last name, phone number, email, hire date, salary, schedule, desk location, and responsibility.");
            System.out.println();
            java.util.List<String[]> rows = new java.util.ArrayList<>();
            boolean hasStaff = false;
            
            while (rs.next()) {
                hasStaff = true;
                int staffID = rs.getInt("staffID");
                String firstName = rs.getString("firstName") != null ? rs.getString("firstName") : "";
                String lastName = rs.getString("lastName") != null ? rs.getString("lastName") : "";
                String phoneNumber = rs.getString("phoneNumber");
                if (phoneNumber == null) {
                    phoneNumber = "(null)";
                }
                String email = rs.getString("email");
                if (email == null) {
                    email = "(null)";
                }
                String hireDate;
                java.sql.Date hireDateObj = rs.getDate("hireDate");
                if (rs.wasNull() || hireDateObj == null) {
                    hireDate = "(null)";
                } else {
                    hireDate = hireDateObj.toString();
                }
                String salaryStr;
                Double salary = rs.getDouble("salary");
                if (rs.wasNull() || salary == null) {
                    salaryStr = "-";
                } else {
                    salaryStr = String.format("$%.2f", salary);
                }
                String schedule = rs.getString("schedule");
                if (schedule == null) {
                    schedule = "(null)";
                }
                String deskLocation = rs.getString("deskLocation");
                if (deskLocation == null) {
                    deskLocation = "(null)";
                }
                String responsibility = rs.getString("responsibility");
                if (responsibility == null) {
                    responsibility = "(null)";
                }
                
                rows.add(new String[]{
                    String.valueOf(staffID),
                    firstName,
                    lastName,
                    phoneNumber,
                    email,
                    hireDate,
                    salaryStr,
                    schedule,
                    deskLocation,
                    responsibility
                });
            }
            
            if (!hasStaff) {
                System.out.println("No desk staff in the database");
                return;
            }
            
            int[] colWidths = {10, 15, 15, 15, 25, 12, 12, 20, 15, 20};
            String[] headers = {"Staff ID", "First Name", "Last Name", "Phone Number", "Email", "Hire Date", "Salary", "Schedule", "Desk Location", "Responsibility"};
            
            for (int i = 0; i < headers.length; i++) {
                colWidths[i] = Math.max(colWidths[i], headers[i].length());
            }
            for (String[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    colWidths[i] = Math.max(colWidths[i], row[i].length());
                }
            }
            
            printTableRow(headers, colWidths);
            printTableSeparator(colWidths);
            
            for (String[] row : rows) {
                printTableRow(row, colWidths);
            }
        }
    }

    private static void viewTrainers() throws SQLException {
        String sql = "SELECT sm.staffID, sm.firstName, sm.lastName, sm.phoneNumber, " +
                     "       sm.email, sm.hireDate, sm.salary, " +
                     "       t.specialty, t.schedule, t.certificationLevel, t.experience " +
                     "FROM StaffMember sm " +
                     "INNER JOIN Trainer t ON sm.staffID = t.staffID " +
                     "ORDER BY sm.staffID;";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n=== Trainers ===");
            System.out.println();
            System.out.println("This table shows all the trainers and their details, including the staff ID, first name, last name, phone number, email, hire date, salary, specialty, schedule, certification level, and experience.");
            System.out.println();

            java.util.List<String[]> rows = new java.util.ArrayList<>();
            boolean hasStaff = false;
            
            while (rs.next()) {
                hasStaff = true;
                int staffID = rs.getInt("staffID");
                String firstName = rs.getString("firstName") != null ? rs.getString("firstName") : "";
                String lastName = rs.getString("lastName") != null ? rs.getString("lastName") : "";
                String phoneNumber = rs.getString("phoneNumber");
                if (phoneNumber == null) {
                    phoneNumber = "(null)";
                }
                String email = rs.getString("email");
                if (email == null) {
                    email = "(null)";
                }
                String hireDate;
                java.sql.Date hireDateObj = rs.getDate("hireDate");
                if (rs.wasNull() || hireDateObj == null) {
                    hireDate = "(null)";
                } else {
                    hireDate = hireDateObj.toString();
                }
                String salaryStr;
                Double salary = rs.getDouble("salary");
                if (rs.wasNull() || salary == null) {
                    salaryStr = "-";
                } else {
                    salaryStr = String.format("$%.2f", salary);
                }
                String specialty = rs.getString("specialty");
                if (specialty == null) {
                    specialty = "(null)";
                }
                String schedule = rs.getString("schedule");
                if (schedule == null) {
                    schedule = "(null)";
                }
                String certificationLevel = rs.getString("certificationLevel");
                if (certificationLevel == null) {
                    certificationLevel = "(null)";
                }
                int experience = rs.getInt("experience");
                if (rs.wasNull()) {
                    experience = 0;
                }
                
                rows.add(new String[]{
                    String.valueOf(staffID),
                    firstName,
                    lastName,
                    phoneNumber,
                    email,
                    hireDate,
                    salaryStr,
                    specialty,
                    schedule,
                    certificationLevel,
                    String.valueOf(experience)
                });
            }
            
            if (!hasStaff) {
                System.out.println("No trainers in the database");
                return;
            }
            
            int[] colWidths = {10, 15, 15, 15, 25, 12, 12, 20, 20, 20, 10};
            String[] headers = {"Staff ID", "First Name", "Last Name", "Phone Number", "Email", "Hire Date", "Salary", "Specialty", "Schedule", "Certification Level", "Experience"};
            
            for (int i = 0; i < headers.length; i++) {
                colWidths[i] = Math.max(colWidths[i], headers[i].length());
            }
            for (String[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    colWidths[i] = Math.max(colWidths[i], row[i].length());
                }
            }
            
            printTableRow(headers, colWidths);
            printTableSeparator(colWidths);
            
            for (String[] row : rows) {
                printTableRow(row, colWidths);
            }
        }
    }

    private static void viewManagers() throws SQLException {
        String sql = "SELECT sm.staffID, sm.firstName, sm.lastName, sm.phoneNumber, " +
                     "       sm.email, sm.hireDate, sm.salary, " +
                     "       m.department, m.officeLocation, m.experience " +
                     "FROM StaffMember sm " +
                     "INNER JOIN Manager m ON sm.staffID = m.staffID " +
                     "ORDER BY sm.staffID;";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n=== Managers ===");
            System.out.println();
            System.out.println("This table shows all the managers and their details, including the staff ID, first name, last name, phone number, email, hire date, salary, department, office location, and experience.");
            System.out.println();

            java.util.List<String[]> rows = new java.util.ArrayList<>();
            boolean hasStaff = false;
            
            while (rs.next()) {
                hasStaff = true;
                int staffID = rs.getInt("staffID");
                String firstName = rs.getString("firstName") != null ? rs.getString("firstName") : "";
                String lastName = rs.getString("lastName") != null ? rs.getString("lastName") : "";
                String phoneNumber = rs.getString("phoneNumber");
                if (phoneNumber == null) {
                    phoneNumber = "(null)";
                }
                String email = rs.getString("email");
                if (email == null) {
                    email = "(null)";
                }
                String hireDate;
                java.sql.Date hireDateObj = rs.getDate("hireDate");
                if (rs.wasNull() || hireDateObj == null) {
                    hireDate = "(null)";
                } else {
                    hireDate = hireDateObj.toString();
                }
                String salaryStr;
                Double salary = rs.getDouble("salary");
                if (rs.wasNull() || salary == null) {
                    salaryStr = "-";
                } else {
                    salaryStr = String.format("$%.2f", salary);
                }
                String department = rs.getString("department");
                if (department == null) {
                    department = "(null)";
                }
                String officeLocation = rs.getString("officeLocation");
                if (officeLocation == null) {
                    officeLocation = "(null)";
                }
                String experienceStr;
                Integer experience = rs.getInt("experience");
                if (rs.wasNull() || experience == null) {
                    experienceStr = "(null)";
                } else {
                    experienceStr = String.valueOf(experience);
                }
                
                rows.add(new String[]{
                    String.valueOf(staffID),
                    firstName,
                    lastName,
                    phoneNumber,
                    email,
                    hireDate,
                    salaryStr,
                    department,
                    officeLocation,
                    experienceStr
                });
            }
            
            if (!hasStaff) {
                System.out.println("No managers in the database");
                return;
            }
            
            int[] colWidths = {10, 15, 15, 15, 25, 12, 12, 20, 20, 10};
            String[] headers = {"Staff ID", "First Name", "Last Name", "Phone Number", "Email", "Hire Date", "Salary", "Department", "Office Location", "Experience"};
            
            for (int i = 0; i < headers.length; i++) {
                colWidths[i] = Math.max(colWidths[i], headers[i].length());
            }
            for (String[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    colWidths[i] = Math.max(colWidths[i], row[i].length());
                }
            }
            
            printTableRow(headers, colWidths);
            printTableSeparator(colWidths);
            
            for (String[] row : rows) {
                printTableRow(row, colWidths);
            }
        }
    }

    private static void viewPlans() throws SQLException {
        String sql = "SELECT p.planID, pt.planType, pt.price " +
                     "FROM Plan p " +
                     "JOIN PlanType pt ON p.planType = pt.planType " +
                     "ORDER BY p.planID;";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\n=== Plans ===");
            System.out.println();
            System.out.println("This table shows all the plans and their details, including the plan ID, plan type, and price.");
            System.out.println();

            // Collect all data first to determine column widths
            java.util.List<String[]> rows = new java.util.ArrayList<>();
            boolean hasPlans = false;
            
            while (rs.next()) {
                hasPlans = true;
                int planID = rs.getInt("planID");
                
                String planType = rs.getString("planType");
                if (planType == null) {
                    planType = "(null)";
                }
                
                String priceStr;
                Double price = rs.getDouble("price");
                if (rs.wasNull() || price == null) {
                    priceStr = "-";
                } else {
                    priceStr = String.format("$%.2f", price);
                }
                
                rows.add(new String[]{
                    String.valueOf(planID),
                    planType,
                    priceStr
                });
            }
            
            if (!hasPlans) {
                System.out.println("No plans in the database");
                return;
            }
            
            // Calculate column widths
            int[] colWidths = {10, 20, 12}; //temporary column widths
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
            
            //Display output 
            // Print table header
            printTableRow(headers, colWidths);
            printTableSeparator(colWidths);
            
            // Print data rows
            for (String[] row : rows) {
                printTableRow(row, colWidths);
            }
        }
    }

    private static void viewPayments() throws SQLException {
        String sql = "SELECT p.paymentID, p.staffID, p.memberID, p.amount, " +
                     "       p.paymentType, p.dateOfPayment, p.status " +
                     "FROM Payment p " +
                     "ORDER BY p.paymentID;";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\n=== Payments ===");
            System.out.println();
            System.out.println("This table shows payments, the ID of the staff member that processed the payment, the ID of the member that made the payment, " //split the long string into 2 lines
            + "the amount of the payment, the type of payment, the date of the payment, and the status of the payment.");
            System.out.println();

            // Collect all data first to determine column widths
            java.util.List<String[]> rows = new java.util.ArrayList<>();
            boolean hasPayments = false;
            
            while (rs.next()) {
                hasPayments = true;
                int paymentID = rs.getInt("paymentID");
                int staffID = rs.getInt("staffID");
                int memberID = rs.getInt("memberID");
                
                String amountStr;
                Double amount = rs.getDouble("amount");
                if (rs.wasNull() || amount == null) {
                    amountStr = "-";
                } else {
                    amountStr = String.format("$%.2f", amount);
                }
                
                String paymentType = rs.getString("paymentType");
                if (paymentType == null) {
                    paymentType = "(null)";
                }
                
                String dateOfPayment;
                java.sql.Date dateOfPaymentObj = rs.getDate("dateOfPayment");
                if (rs.wasNull() || dateOfPaymentObj == null) {
                    dateOfPayment = "(null)";
                } else {
                    dateOfPayment = dateOfPaymentObj.toString();
                }
                
                String status = rs.getString("status");
                if (status == null) {
                    status = "(null)";
                }
                
                rows.add(new String[]{
                    String.valueOf(paymentID),
                    String.valueOf(staffID),
                    String.valueOf(memberID),
                    amountStr,
                    paymentType,
                    dateOfPayment,
                    status
                });
            }
            
            if (!hasPayments) {
                System.out.println("No payments in the database");
                return;
            }
            
            // Calculate column widths
            int[] colWidths = {12, 10, 12, 12, 15, 15, 12}; //temporary column widths
            String[] headers = {"Payment ID", "Staff ID", "Member ID", "Amount", "Payment Type", "Date of Payment", "Status"};
            
            // Adjust widths based on actual data
            for (int i = 0; i < headers.length; i++) {
                colWidths[i] = Math.max(colWidths[i], headers[i].length());
            }
            for (String[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    colWidths[i] = Math.max(colWidths[i], row[i].length());
                }
            }
            
            //Display output 
            // Print table header
            printTableRow(headers, colWidths);
            printTableSeparator(colWidths);
            
            // Print data rows
            for (String[] row : rows) {
                printTableRow(row, colWidths);
            }
        }
    }

    private static void viewCheckIns() throws SQLException {
        String sql = "SELECT c.checkInID, c.membershipID, c.staffID, c.ts, c.location " +
                     "FROM CheckIn c " +
                     "ORDER BY c.checkInID;";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\n=== Check-Ins ===");
            System.out.println();
            System.out.println("This table shows all the check-ins and their details, including the check-in ID, membership ID, ID of staff who checked in the member, timestamp, and location.");
            System.out.println();

            // Collect all data first to determine column widths
            java.util.List<String[]> rows = new java.util.ArrayList<>();
            boolean hasCheckIns = false;
            
            while (rs.next()) {
                hasCheckIns = true;
                int checkInID = rs.getInt("checkInID");
                int membershipID = rs.getInt("membershipID");
                int staffID = rs.getInt("staffID");
                
                String timestamp;
                java.sql.Timestamp ts = rs.getTimestamp("ts");
                if (rs.wasNull() || ts == null) {
                    timestamp = "(null)";
                } else {
                    timestamp = ts.toString();
                }
                
                String location = rs.getString("location");
                if (location == null) {
                    location = "(null)";
                }
                
                rows.add(new String[]{
                    String.valueOf(checkInID),
                    String.valueOf(membershipID),
                    String.valueOf(staffID),
                    timestamp,
                    location
                });
            }
            
            if (!hasCheckIns) {
                System.out.println("No check-ins in the database");
                return;
            }
            
            // Calculate column widths
            int[] colWidths = {12, 15, 10, 20, 15}; //temporary column widths
            String[] headers = {"Check-In ID", "Membership ID", "Staff ID", "Timestamp", "Location"};
            
            // Adjust widths based on actual data
            for (int i = 0; i < headers.length; i++) {
                colWidths[i] = Math.max(colWidths[i], headers[i].length());
            }
            for (String[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    colWidths[i] = Math.max(colWidths[i], row[i].length());
                }
            }
            
            //Display output 
            // Print table header
            printTableRow(headers, colWidths);
            printTableSeparator(colWidths);
            
            // Print data rows
            for (String[] row : rows) {
                printTableRow(row, colWidths);
            }
        }
    }

    private static void viewActiveMembers() throws SQLException {
        String sql = "SELECT DISTINCT gm.memberID, gm.firstName, gm.lastName, " +
                     "       gm.birthday, gm.phoneNumber, gm.email, gm.dateJoined " +
                     "FROM GymMember gm " +
                     "INNER JOIN Membership ms ON gm.memberID = ms.memberID " +
                     "WHERE ms.status = 'Active' " +
                     "ORDER BY gm.memberID;";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\n=== Active Members ===");
            System.out.println();
            System.out.println("This table shows all gym members who have an active membership, including the member ID, first name, last name, birthday, phone number, email, and date joined.");
            System.out.println();

            // Collect all data first to determine column widths
            java.util.List<String[]> rows = new java.util.ArrayList<>();
            boolean hasMembers = false;
            
            while (rs.next()) {
                hasMembers = true;
                int memberID = rs.getInt("memberID");
                String firstName = rs.getString("firstName") != null ? rs.getString("firstName") : "";
                String lastName = rs.getString("lastName") != null ? rs.getString("lastName") : "";
                
                String birthday;
                java.sql.Date birthdayDate = rs.getDate("birthday");
                if (rs.wasNull() || birthdayDate == null) {
                    birthday = "(null)";
                } else {
                    birthday = birthdayDate.toString();
                }
                
                String phoneNumber = rs.getString("phoneNumber");
                if (phoneNumber == null) {
                    phoneNumber = "(null)";
                }
                
                String email = rs.getString("email");
                if (email == null) {
                    email = "(null)";
                }
                
                String dateJoined;
                java.sql.Date dateJoinedDate = rs.getDate("dateJoined");
                if (rs.wasNull() || dateJoinedDate == null) {
                    dateJoined = "(null)";
                } else {
                    dateJoined = dateJoinedDate.toString();
                }
                
                rows.add(new String[]{
                    String.valueOf(memberID),
                    firstName,
                    lastName,
                    birthday,
                    phoneNumber,
                    email,
                    dateJoined
                });
            }
            
            if (!hasMembers) {
                System.out.println("No active members in the database");
                return;
            }
            
            // Calculate column widths
            int[] colWidths = {10, 15, 15, 12, 15, 25, 12}; //temporary column widths
            String[] headers = {"Member ID", "First Name", "Last Name", "Birthday", "Phone Number", "Email", "Date Joined"};
            
            // Adjust widths based on actual data
            for (int i = 0; i < headers.length; i++) {
                colWidths[i] = Math.max(colWidths[i], headers[i].length());
            }
            for (String[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    colWidths[i] = Math.max(colWidths[i], row[i].length());
                }
            }
            
            //Display output 
            // Print table header
            printTableRow(headers, colWidths);
            printTableSeparator(colWidths);
            
            // Print data rows
            for (String[] row : rows) {
                printTableRow(row, colWidths);
            }
        }
    }

}
