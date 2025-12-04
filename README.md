# README.md - CS157A Gym Database Project

## Overview
This project is a Java console application that uses JDBC to interact with a MySQL database for a gym management system.  
Key Features:
- Uses **PreparedStatement** only for all SQL
- Implements **SELECT**, **INSERT**, **UPDATE**, **DELETE**
- Demonstrates a multi-statement **transaction workflow** that uses both **COMMIT** and **ROLLBACK**
- Provides meaningful input validation and error messages
- Includes a **SQL VIEW** and a **Stored Procedure**
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
│  └─ App.class  
│  └─ DatabaseDeletions.class  
│  └─ DatabaseViews.class  
│  └─ functions.sql  
│  └─ tables.sql  
│  └─ triggers.sql  
├─ lib/  
│  └─ mysql-connector-j-9.5.0.jar  
├─ src/  
│  ├─ App.java  
│  ├─ DatabaseDeletions.java  
│  ├─ DatabaseInsertions.java  
│  ├─ DatabaseUpdates.java  
│  ├─ Main.java  
│  ├─ create_and_populate.sql  
│  ├─ DatabaseViews.java  
│  ├─ functions.sql  
│  ├─ tables.sql  
│  └─ triggers.sql  
├─ .gitignore  
└─ app.properties  

---

## Database Setup

1. Install **MySQL 8.x** on your machine if not already installed.
2. Create a database for the gym:
```sql
CREATE DATABASE gym;
Use gym;
```
3. Execute `tables.sql` to create all tables:
```sql
SOURCE src/tables.sql;
```
This script creates all necessary tables including:
- PlanTypeInfo & Plan
- Gym Member
- StaffMember (Desk, Trainer, Manager sub-entities)
- Guest & GuestVisit
- Membership
- Payment
- CheckIn
- TrainerTrainsMember
**Sample Data** inserted in tables.sql:
```sql
-- GymMember
-- GymMember
INSERT INTO GymMember (firstName, lastName, birthday, phoneNumber, email, dateJoined) VALUES
('John', 'Smith', '1990-05-15', '408-555-0101', 'john.smith@email.com', '2024-01-15');

-- PlanTypeInfo
INSERT INTO PlanTypeInfo (planType, price) VALUES
('Monthly', 49.99);
```
5. Execute `functions.sql` to create views, stored procedures, and functions and Execute `triggers.sql` to create triggers:
```sql
SOURCE src/functions.sql;
SOURCE src/triggers.sql;
```
- `ActiveMembersView`: Show all active members with membership details
- `GetMemberPaymentHistory`: Stored procedure for retrieving payment history
- `CalculateTotalRevenue`: Stored function to calculate total revenue
- Trigger `activate_Membership_After_Payment`: Automatically sets membership status to `Active` after a payment is marked as `Success`

---

## Running the Java Program

1. Compile all Java files in the `src/` folder:
```bash
javac -d bin -cp ".;path/to/mysql-connector-java-8.0.30.jar" src/*.java
```
2. Run the main program:
```bash
java -cp ".;bin;path/to/mysql-connector-java-8.0.30.jar" App
```
3. Follow the on-screen menu to perform operations.

---

## Java CLI Menu
The main Java program provides a command-line interface for interacting with the database:
```sql
=== Gym Database Management System Menu ===
1. View Gym Members
2. View All Memberships
3. View Staff Members
4. View Plans
5. View Payments
6. View Check-Ins
7. View Active Members (DB View)
8. View Trainer Trains Member
9. View Guest Members
10. View Guest Visits
11. View Member Payment History (Stored Procedure)
12. Calculate Total Revenue (Stored Function)
13. Add new Gym Member
14. Purchase Membership (Transaction Demo)
15. Insert Staff Member
16. Add New Plan
17. Setup Default Plans (Quick Setup)
18. Update Gym Member
19. Update Membership Status
20. Update Staff Salary
21. Update Payment Status
22. Delete Gym Member
23. Transfer Membership Plan (COMMIT/ROLLBACK Demo)
0. Exit
```

---

## Sample Commands & Usage

1. Insertions (`DatabaseInsertions.java`)
- Test Case 1: Add a Gym Member
```yaml
Input:
First Name: John
Last Name: Doe
Birthday: 1990-05-12
Phone: 555-1234
Email: john.doe@example.com

Expencted Ouput:
Member John Doe added with Member ID: 1
```
- Test Case 2: Add a Guest
```yaml
Input:
Member ID: 1
First Name: Jane
Last Name: Doe
Relationship: Sister
Birthday: 1992-04-10

Expected Output:
Guest Jane Doe added with Guest ID: 1
```
- Test Case 3: Add a Payment
```yaml
Input:
Member ID: 1
Staff ID: 2
Amount: 50.00
Payment Type: CARD
Status: Success

Expected Output:
Payment added with Payment ID: 1
Membership status updated to Active
```
2. Updates (DatabaseUpdates.java)
- Test Case 4: Update Member Contact Info
```yaml
Input:
Member ID: 1
New Phone: 555-6789
New Email: jone.doe2@example.com

Expected Output:
Member ID 1 updated successfully
```
- Test Case 5: Update Payment Status
```yaml
Input:
Payment ID: 1
New Status: Success

Expected Output:
Payment ID 1 status updated to Success
Membership status for Member ID 1 is Active
```
- Test Case 6: Update Membership Plan
```yaml
Input:
Membership ID: 1
New Plan Type: Annual

Expected Output:
Membership ID 1 plan updated to Annual
```
3. Deletions (Database Deletions.java)
- Test Case 7: Delete Guest
```yaml
Input:
Guest ID: 1
Confirm: Y

Expected Output:
Guest ID 1 deleted successfully
```
- Test Case 8: Delete Staff Member
```yaml
Input:
Staff ID: 3
Confirm: Y

Expected Output:
Staff Member ID 3 deleted successfully
```
- Test Case 9: Delete Membership
```yaml
Input:
Membership ID: 1
Confirm: Y

Expected Output:
Membership ID 1 deleted successfully
```
4. Views (DatabaseViews.java)
- Test Case 10: View Active Members
```sql
Output:
Member ID | Member Name | Email              | Phone     | Membership ID | Start Date | End Date   | Plan Type | Price
-------------------------------------------------------------------------------------------------------------
1         | John Doe    | john.doe@example.com | 555-5678 | 1             | 2025-01-01 | 2026-01-01 | Annual    | $500.00
```
- Test Case 11: View Guest Members
```sql
Output:
Guest ID | First Name | Last Name | Birthday   | Relationship | Member ID | Member Name
-----------------------------------------------------------------------------
1        | Jane       | Doe       | 1992-04-10 | Sister      | 1         | John Doe
```
- Test Case 12: View Guest Visits
```sql
Output:
Visit ID | Guest ID | Guest Name | Visit Date | Member ID | Member Name
---------------------------------------------------------------------
1        | 1        | Jane Doe   | 2025-11-01 | 1         | John Doe
```
- Test Case 13: View Member Payment History
```sql
Input:
Member ID: 1

Output:
Payment ID | Amount | Payment Type | Date of Payment | Status  | Processed By
---------------------------------------------------------------------------
1          | $50.00 | CARD         | 2025-11-01      | Success | Alice Smith

```
- Test Case 14: View Total Revenue
```mathematica
Input:
Choice: 1 (All Success payments)

Output:
Total revenue (Success payments): $500.00
```
