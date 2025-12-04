# README.md - CS157A Gym Database Project

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
- **Java JDK 17+** (OpenJDK or Oracle JDK)
- **MySQL Server 8.x**
- **MySQL Connector/J 9.5.0**
- Optional: **MySQL Workbench 8.x** (To run SQL and view tables)
- Optional: **VSCode Studio 1.106.0** (Used for development)

---

## Database connection configuration
Located in `app.properties`:
```properties
db.url=jdbc:mysql://127.0.0.1:3306/gym
db.username=root
db.password=cs157a
db.driver=com.mysql.cj.jdbc.Driver
```

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

1. Install **MySQL 8.x** on your machine if not already installed.
2. Create a database for the gym:
```sql
CREATE DATABASE gym;
USE gym;
```
3. Execute `create_and_populate.sql` to create all tables, views, stored procedures, functions, and sample data:
```sql
SOURCE src/create_and_populate.sql;
```
This script creates all necessary database objects including:
- **Tables**: PlanTypeInfo & Plan, GymMember, StaffMember (Desk, Trainer, Manager sub-entities), Guest & GuestVisit, Membership, Payment, CheckIn, TrainerTrainsMember
- **Views**: `ActiveMembersView` - Shows all active members with membership details
- **Stored Procedures**: `GetMemberPaymentHistory` - Retrieves payment history for a specific member
- **Stored Functions**: `CalculateTotalRevenue` - Calculates total revenue from payments (optionally filtered by status)
- **Triggers**: `cancel_Membership_After_Refund` - Automatically sets membership status to `Cancelled` when a payment is marked as `Refunded`

**Note**: If the trigger doesn't work when running the entire file, you can run `triggers.sql` separately:
```sql
SOURCE src/triggers.sql;
```

---

## Running the Java Program

1. Compile all Java files in the `src/` folder:
```bash
javac -d bin -cp ".;lib/mysql-connector-j-9.5.0.jar" src/*.java
```
2. Run the main program:
```bash
java -cp ".;bin;lib/mysql-connector-j-9.5.0.jar" Main
```
3. Follow the on-screen menu to perform operations.

**Note**: Adjust the path to `mysql-connector-j-9.5.0.jar` based on your actual file location.

---

## Java CLI Menu
The main Java program provides a command-line interface for interacting with the database:

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

0. Exit

---

## Sample Commands & Usage

1. Insertions (`DatabaseInsertions.java`)
- Test Case 1: Add a Gym Member
```yaml
Input:
First Name: John
Last Name: Doe
Birthday: 1990-05-12
Phone: 5551234567 (10 digits)
Email: john.doe@example.com

Expected Output:
✓ Success! Gym member 'John Doe' has been added to the database.
```

- Test Case 2: Purchase Membership (Transaction Demo)
```yaml
Input:
Member ID: 1
Plan ID: 1 (from displayed plans)
Start Date: 2025-01-01 (or press Enter for today)
Desk Staff ID: 1 (from displayed desk staff)
Payment Type: 2 (CARD)

Expected Output:
✓ Success! Membership purchased and activated.
  Member: John Doe (ID: 1)
  Plan: Monthly ($49.99)
  Start Date: 2025-01-01
  End Date: 2025-02-01
  Payment: CARD - Success
```

- Test Case 3: Insert Staff Member
```yaml
Input:
First Name: Alice
Last Name: Smith
Phone: 5559876543 (optional)
Email: alice.smith@gym.com (optional)
Hire Date: 2024-01-15
Salary: 50000.00
Role: 1 (Desk Staff)
Schedule: Mon-Fri 9AM-5PM
Desk Location: Front Desk C
Responsibility: Member services

Expected Output:
✓ Success! Staff member 'Alice Smith' has been added to the database.
  Staff ID: 6
  Role: Desk Staff
```
2. Updates (`DatabaseUpdates.java`)
- Test Case 4: Update Gym Member
```yaml
Input:
Member ID: 1
Choice: 3 (Phone Number)
New Phone: 5556789012

Expected Output:
✓ Success! Gym member updated.
```

- Test Case 5: Update Payment Status
```yaml
Input:
Payment ID: 1
Choice: 1 (Success)

Expected Output:
✓ Success! Payment status updated from 'Pending' to 'Success'.
```

- Test Case 6: Transfer Membership Plan (COMMIT/ROLLBACK Demo)
```yaml
Input:
Membership ID: 1
New Plan ID: 3 (Annual)
Desk Staff ID: 1
Payment Type: 2 (CARD)

Expected Output:
--- Transaction Starting ---
Step 1: Updating membership plan...
   Membership updated successfully.
Step 2: Creating payment record...
   Payment record created successfully.

--- COMMIT executed ---
✓ Success! Membership transferred to new plan.
  New Plan: Annual ($499.99)
  New Start Date: 2025-01-15
  New End Date: 2026-01-15
  Payment: CARD - Success
```
3. Deletions (`DatabaseDeletions.java`)
- Test Case 7: Delete Gym Member
```yaml
Input:
Member ID: 1

Expected Output:
Successfully deleted gym member with ID 1.
```

**Note**: The current implementation only includes deletion of Gym Members. Deletion of other entities (Guest, Staff Member, Membership) would cascade based on foreign key constraints defined in the database schema.

4. Views (`DatabaseViews.java`)
- Test Case 10: View Active Members (using SQL VIEW)
```sql
Output (formatted table):
| Member ID | Member Name | Email              | Phone Number | Membership ID | Start Date | End Date   | Plan Type | Price    |
|-----------|-------------|--------------------|-------------|---------------|------------|------------|-----------|----------|
| 1         | John Smith  | john.smith@email.com | 408-555-0101 | 1            | 2024-01-15 | 2024-02-15 | Monthly Premium | $79.99 |
```

- Test Case 11: View Guest Members
```sql
Output (formatted table):
| Guest ID | First Name | Last Name | Birthday   | Relationship to Member | Member ID | Member Name |
|----------|------------|-----------|------------|------------------------|-----------|-------------|
| 1        | Jane       | Smith     | 1991-06-20 | Spouse                 | 1         | John Smith  |
```

- Test Case 12: View Guest Visits
```sql
Output (formatted table):
| Visit ID | Guest ID | Guest Name | Visit Date | Member ID | Member Name |
|----------|----------|------------|------------|-----------|-------------|
| 1        | 1        | Jane Smith | 2024-11-15 | 1         | John Smith  |
```

- Test Case 13: View Member Payment History (Stored Procedure)
```sql
Input:
Member ID: 1

Output (formatted table):
| Payment ID | Amount  | Payment Type | Date of Payment | Status  | Processed By    |
|------------|---------|--------------|-----------------|---------|------------------|
| 1          | $79.99  | CARD         | 2024-01-15      | Success | Amanda Davis     |
```

- Test Case 14: View Total Revenue (Stored Function)
```yaml
Input:
Choice: 1 (All Success payments)

Output:
=== Total Revenue ===
Total revenue (Success payments): $749.95
```

**Note**: All view outputs are displayed as formatted tables with proper column alignment. The actual output may vary based on the data in your database.
