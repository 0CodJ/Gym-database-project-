# CS157A Gym Database Project

## Overview
This project is a Java console application that uses JDBC to interact with a MySQL database for a gym management system.  
Key Features:
- Uses **PreparedStatement** only for all SQL
- Implements **SELECT**, **INSERT**, **UPDATE**, **DELETE**
- Demonstrates a multi-statement **transaction workflow** that uses both **COMMIT** and **ROLLBACK**
- Provides meaningful input validation and error messages
- Includes a **SQL VIEW**, **Stored Procedure**, and **Stored Function**
- Includes database **Triggers** for automated business logic
- Externalized DB credentials in `app.properties`

---

## Requirements (software & versions)

### Required Software:
- [**Java JDK 17+**](https://www.oracle.com/java/technologies/downloads/) (OpenJDK or Oracle JDK)
  - Verify installation: `java -version` and `javac -version`
- [**MySQL Server 8.0+**](https://dev.mysql.com/downloads/mysql/8.0.html)
  - **Version Used**: MySQL 8.0.x Community Server
  - Verify installation: `mysql --version`
- [**MySQL Connector/J 9.5.0**](https://dev.mysql.com/downloads/connector/j/)
  - **Version Used**: mysql-connector-j-9.5.0.jar
  - **JDBC Driver**: `com.mysql.cj.jdbc.Driver`
  - Download and place in the `lib/` directory

### Recommended Software:
- [**MySQL Workbench 8+**](https://dev.mysql.com/downloads/workbench/) 
  - **Highly Recommended**: While technically optional, MySQL Workbench is the primary tool used in this project's setup instructions
  - All database setup screenshots and examples use MySQL Workbench
  - Provides GUI for database management, SQL execution, and schema visualization
  - Alternative: You can use MySQL command line or other database management tools, but instructions assume MySQL Workbench
- IDE of your choice: 
  - [**VSCode**](https://code.visualstudio.com/) with Java Extension Pack
  - [**IntelliJ IDEA**](https://www.jetbrains.com/idea/) (Community or Ultimate)
  - [**Eclipse**](https://www.eclipse.org/downloads/) with Java Development Tools

---

## Database connection configuration
Located in `app.properties`:
```properties
db.url=jdbc:mysql://127.0.0.1:3306/gym
db.username=root
db.password=cs157a 
db.driver=com.mysql.cj.jdbc.Driver
```

Note: url, username, and password may vary from user to user depending on how MySQl is configured.

---

## Files in this Repository

Gym-database-project-/

├─ .vscode/  
│  └─ settings.json  
├─ bin/  
│  └─ (compiled .class files)  
├─ lib/  
│  └─ mysql-connector-j-9.5.0.jar  
├─ src/  
│  ├─ Main.java  
│  ├─ DatabaseDeletions.java  
│  ├─ DatabaseInsertions.java  
│  ├─ DatabaseUpdates.java  
│  ├─ DatabaseViews.java  
│  ├─ create_and_populate.sql  
│  └─ triggers.sql  
├─ .gitignore  
└─ app.properties  

---

## Database Setup

### Step 1: Install and Configure MySQL

1. **Install MySQL Server 8.0+** on your machine if not already installed
   - Download from [MySQL Downloads](https://dev.mysql.com/downloads/mysql/)
   - Follow installation wizard and set a root password
   - Ensure MySQL service is running

2. **Install MySQL Workbench** (Required for following these instructions)
   - Download from [MySQL Workbench Downloads](https://dev.mysql.com/downloads/workbench/)
   - **Note**: While you can use MySQL command line or other tools, all setup instructions and screenshots in this README use MySQL Workbench
   - This provides a GUI for database management, making it easier to execute SQL scripts and visualize the database structure

### Step 2: Create the Database

1. **Open MySQL Workbench** and connect to your MySQL server using your root credentials

2. **Create the gym database** by either:
   - **Option A**: Using the GUI - Click the "Create new schema" icon (lightning bolt with a plus)
   - **Option B**: Using SQL commands:
   ```sql
   CREATE DATABASE gym;
   USE gym;
   ```

![Create Database](https://github.com/0CodJ/Gym-database-project-/blob/latestVersion/Screenshot%202025-12-03%20213504.png?raw=true)

### Step 3: Execute Database Schema Script

1. **Method 1: Using SOURCE command** (if running from MySQL command line):
   ```sql
   SOURCE src/create_and_populate.sql;
   ```

2. **Method 2: Using MySQL Workbench** (Recommended):
   - Open `src/create_and_populate.sql` in MySQL Workbench
   - Select all the SQL code (Ctrl+A / Cmd+A)
   - Execute the script (Click the execute button or press Ctrl+Shift+Enter)
   
![Execute SQL Script](https://github.com/0CodJ/Gym-database-project-/blob/latestVersion/Screenshot%202025-12-03%20213936.png?raw=true)

3. **Verify database creation**:
   - Check that all tables appear in the Schema Navigator
   - Run: `SHOW TABLES;` to see all created tables
   - Expected tables: `PlanTypeInfo`, `Plan`, `GymMember`, `StaffMember`, `Desk`, `Trainer`, `Manager`, `Guest`, `GuestVisit`, `Membership`, `Payment`, `CheckIn`, `TrainerTrainsMember`

This script creates all necessary database objects including:
- **Tables**: PlanTypeInfo & Plan, GymMember, StaffMember (Desk, Trainer, Manager sub-entities), Guest & GuestVisit, Membership, Payment, CheckIn, TrainerTrainsMember
- **Views**: `ActiveMembersView` - Shows all active members with membership details
- **Stored Procedures**: `GetMemberPaymentHistory` - Retrieves payment history for a specific member
- **Stored Functions**: `CalculateTotalRevenue` - Calculates total revenue from payments (optionally filtered by status)
- **Triggers**: `cancel_Membership_After_Refund` - Automatically sets membership status to `Cancelled` when a payment is marked as `Refunded`

**Note**: If the trigger doesn't work when running the entire file, you can run `triggers.sql` separately in another file at MySQL Workbench:
```sql
SOURCE src/triggers.sql;
```

---

## Java Application Setup and Execution

### Step 1: Download MySQL Connector/J

1. Download **MySQL Connector/J 9.5.0** from [MySQL Connector/J Downloads](https://dev.mysql.com/downloads/connector/j/)
2. Extract the JAR file (`mysql-connector-j-9.5.0.jar`)
3. Place it in the `lib/` directory of the project

### Step 2: Configure IDE (If using VSCode)

1. Open the project in VSCode
2. Navigate to **Java Projects** (found in the bottom left sidebar)
3. Scroll down to **Referenced Libraries**
4. Click the **+** (plus) icon
5. Browse and select `mysql-connector-j-9.5.0.jar` from the `lib/` folder

![Add MySQL Connector in VSCode](https://github.com/0CodJ/Gym-database-project-/blob/latestVersion/Screenshot%202025-12-03%20214323.png?raw=true)

### Step 3: Configure Database Connection

1. Open `app.properties` in the project root
2. Update the following properties to match your MySQL configuration:
   ```properties
   db.url=jdbc:mysql://127.0.0.1:3306/gym
   db.username=root
   db.password=your_mysql_password
   db.driver=com.mysql.cj.jdbc.Driver
   ```
   - **url**: Change `3306` if your MySQL uses a different port
   - **username**: Change if you're not using root
   - **password**: Set to your MySQL root password

### Step 4: Compile the Java Application

**Using Command Line (Windows):**
```bash
javac -d bin -cp ".;lib/mysql-connector-j-9.5.0.jar" src/*.java
```

**Using Command Line (Linux/Mac):**
```bash
javac -d bin -cp ".:lib/mysql-connector-j-9.5.0.jar" src/*.java
```

**Using IDE:**
- **VSCode**: Right-click on `Main.java` → "Run Java"
- **IntelliJ**: Right-click on `Main.java` → "Run 'Main.main()'"
- **Eclipse**: Right-click on `Main.java` → Run As → Java Application

### Step 5: Run the Application

**Using Command Line (Windows):**
```bash
java -cp ".;bin;lib/mysql-connector-j-9.5.0.jar" Main
```

**Using Command Line (Linux/Mac):**
```bash
java -cp ".:bin:lib/mysql-connector-j-9.5.0.jar" Main
```

**Using IDE:**
- Run the application from your IDE (same as compilation step)

### Step 6: Verify Connection

1. When the program starts, you should see:
   ```
   Database connection successful!
   Welcome to the Gym Database Management System!
   ```
2. If you see a connection error, verify:
   - MySQL server is running
   - Database `gym` exists
   - Credentials in `app.properties` are correct
   - MySQL Connector/J is in the classpath

3. Follow the on-screen menu to interact with the database

---

## How the Application Was Built (Step-by-Step)

### Phase 1: Database Design and Schema Creation

1. **Designed Entity-Relationship Model**
   - Identified entities: GymMember, StaffMember, Plan, Membership, Payment, Guest, etc.
   - Defined relationships and foreign key constraints
   - Planned sub-entities for StaffMember (Desk, Trainer, Manager)

2. **Created Database Schema** (`create_and_populate.sql`)
   - Created tables in dependency order:
     - `PlanTypeInfo` and `Plan` (base tables)
     - `GymMember` (independent entity)
     - `StaffMember` and sub-entities (`Desk`, `Trainer`, `Manager`)
     - `Guest` and `GuestVisit` (dependent on GymMember)
     - `Membership` (links GymMember and Plan)
     - `Payment` (links StaffMember, GymMember, and Membership)
     - `CheckIn` (links Membership and StaffMember)
     - `TrainerTrainsMember` (many-to-many relationship)

3. **Implemented Database Features**
   - Created SQL VIEW: `ActiveMembersView` for active member queries
   - Created Stored Procedure: `GetMemberPaymentHistory` for payment history retrieval
   - Created Stored Function: `CalculateTotalRevenue` for revenue calculations
   - Created Trigger: `cancel_Membership_After_Refund` for automated business logic

4. **Populated Sample Data**
   - Inserted test data for all tables to enable testing

### Phase 2: Java Application Development

1. **Project Structure Setup**
   - Created package structure with separate classes for different operations
   - Organized code into: `DatabaseViews.java`, `DatabaseInsertions.java`, `DatabaseUpdates.java`, `DatabaseDeletions.java`
   - Created `Main.java` as the entry point with menu-driven interface

2. **Database Connection Layer**
   - Implemented connection using JDBC with externalized configuration (`app.properties`)
   - Used `PreparedStatement` exclusively for all SQL operations (security best practice)
   - Implemented proper connection management and error handling

3. **View Operations** (`DatabaseViews.java`)
   - Implemented methods to display all database tables
   - Created formatted table output with proper column alignment
   - Integrated SQL VIEW and Stored Procedure calls
   - Added submenu for Staff Members (All, Desk, Trainer, Manager)

4. **Insert Operations** (`DatabaseInsertions.java`)
   - Implemented `insertGymMember()` with age validation (minimum 16 years)
   - Implemented `purchaseMembership()` with transaction support (COMMIT/ROLLBACK)
   - Implemented `insertStaffMember()` with role-based sub-entity creation
   - Added comprehensive input validation and error handling

5. **Update Operations** (`DatabaseUpdates.java`)
   - Implemented `updateGymMember()` for member information updates
   - Implemented `updateMembershipStatus()` for membership management
   - Implemented `updateStaffSalary()` for salary modifications
   - Implemented `updatePaymentStatus()` for payment tracking
   - Implemented `transferMembershipPlan()` as transaction demo (COMMIT/ROLLBACK)

6. **Delete Operations** (`DatabaseDeletions.java`)
   - Implemented `deleteGymMember()` with existence validation
   - Leveraged database foreign key constraints for cascading deletes

7. **Transaction Management**
   - Implemented transaction workflow in `purchaseMembership()`:
     - Creates membership record
     - Creates payment record
     - Uses COMMIT on success, ROLLBACK on failure
   - Implemented transaction workflow in `transferMembershipPlan()`:
     - Updates membership plan
     - Creates new payment record
     - Demonstrates explicit COMMIT/ROLLBACK messaging

8. **Input Validation and Error Handling**
   - Email format validation
   - Phone number format validation (10 digits)
   - Date format validation (YYYY-MM-DD)
   - Age validation for gym members (minimum 16)
   - Schedule format validation for staff
   - Duplicate entry detection and user-friendly error messages
   - SQL exception handling with meaningful messages

### Phase 3: Testing and Refinement

1. **Database Testing**
   - Verified all tables created correctly
   - Tested foreign key constraints
   - Verified triggers fire correctly
   - Tested VIEW, Stored Procedure, and Stored Function

2. **Application Testing**
   - Tested all CRUD operations
   - Verified transaction rollback on errors
   - Tested input validation
   - Verified error messages are user-friendly

3. **Documentation**
   - Created comprehensive README.md
   - Documented all features and usage
   - Added setup instructions with screenshots

### Technology Stack Summary

- **Backend**: Java 17+ with JDBC
- **Database**: MySQL 8.0+ Community Server
- **JDBC Driver**: MySQL Connector/J 9.5.0
- **Database Tools**: MySQL Workbench 8+
- **Development**: VSCode / IntelliJ IDEA

---

## Java Command Line Interface 
The main Java program provides a command-line interface for interacting with the database. The user can interact with this interface by typing a number for a specific menu option. 

**View/Selection Options:**

    1. View Gym Members
    2. View All Memberships
    3. View Staff Members (submenu: All Staff, Desk Staff, Trainers, Managers)
    4. View Plans
    5. View Payments
    6. View Check-Ins
    7. View Active Members (Uses a view created in the database)
    8. View Trainer Trains Member
    9. View Guest Members
    10. View Guest Visits
    11. View Member Payment History (Stored Procedure)
    12. Calculate Total Revenue (Stored Function)

**Insertion Options:**

    13. Add new Gym Member
    14. Purchase Membership (Transaction Demo)
    15. Insert Staff Member

**Update Options:**

    16. Update Gym Member
    17. Update Membership Status
    18. Update Staff Salary
    19. Update Payment Status

**Deletion Options:**

    20. Delete Gym Member

**Transaction Demo:**

    21. Transfer Membership Plan (COMMIT/ROLLBACK Demo)

**End Program:**

    0. Exit

---

## Project Architecture

### Class Structure

- **`Main.java`**: Entry point of the application
  - Handles database connection initialization
  - Displays main menu and routes user input to appropriate methods
  - Manages application lifecycle

- **`DatabaseViews.java`**: All SELECT operations
  - Methods for viewing all database tables
  - Integration with SQL VIEW and Stored Procedures
  - Formatted table output utilities

- **`DatabaseInsertions.java`**: All INSERT operations
  - Gym member insertion with validation
  - Membership purchase with transaction support
  - Staff member insertion with role assignment

- **`DatabaseUpdates.java`**: All UPDATE operations
  - Member information updates
  - Membership status updates
  - Staff salary updates
  - Payment status updates
  - Membership plan transfer (transaction demo)

- **`DatabaseDeletions.java`**: All DELETE operations
  - Gym member deletion with validation

### Database Architecture

- **Tables**: 12 main tables with proper normalization
- **Views**: 1 view (`ActiveMembersView`) for active member queries
- **Stored Procedures**: 1 procedure (`GetMemberPaymentHistory`) for payment history
- **Stored Functions**: 1 function (`CalculateTotalRevenue`) for revenue calculation
- **Triggers**: 1 trigger (`cancel_Membership_After_Refund`) for automated business logic

### Security Features

- **PreparedStatement**: All SQL operations use parameterized queries to prevent SQL injection
- **Input Validation**: Comprehensive validation for all user inputs
- **Error Handling**: Graceful error handling with user-friendly messages
- **Transaction Management**: Proper COMMIT/ROLLBACK for data integrity

---

## Troubleshooting

### Common Issues

1. **Connection Error**
   - Verify MySQL server is running: `mysql --version`
   - Check `app.properties` credentials
   - Ensure database `gym` exists
   - Verify MySQL port (default: 3306)

2. **ClassNotFoundException: com.mysql.cj.jdbc.Driver**
   - Ensure `mysql-connector-j-9.5.0.jar` is in `lib/` directory
   - Verify JAR is in classpath during compilation and execution
   - In IDE, ensure JAR is added to Referenced Libraries

3. **SQL Syntax Errors**
   - Ensure MySQL 8.0+ is installed (some features require 8.0+)
   - Verify `create_and_populate.sql` executed completely
   - Check MySQL error logs for specific issues

4. **Compilation Errors**
   - Verify Java JDK 17+ is installed: `javac -version`
   - Check all source files are in `src/` directory
   - Ensure proper package structure

5. **Transaction Rollback Issues**
   - Check foreign key constraints are satisfied
   - Verify all required fields are provided
   - Check for unique constraint violations (phone, email)

---

## Additional Resources

- [MySQL 8.0 Documentation](https://dev.mysql.com/doc/refman/8.0/en/)
- [MySQL Connector/J Documentation](https://dev.mysql.com/doc/connector-j/8.0/en/)
- [Java JDBC Tutorial](https://docs.oracle.com/javase/tutorial/jdbc/)
- [PreparedStatement Best Practices](https://docs.oracle.com/javase/tutorial/jdbc/basics/prepared.html)

---
