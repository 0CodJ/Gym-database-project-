# AI Documentation

## AI Model Used
**ChatGPT**

## Summary
We used ChatGPT to help optimize our program, verify additions to our project, determine whether certain functions were necessary, and improve the appearance and usability of the UI.

---

## Prompts and Responses

### 1. “Would using switch statements be more efficient for the menu UI of my GymDatabase project?”
**What we adopted:**
- ChatGPT confirmed that using `switch` statements would be more user-friendly.
- We updated `Main.java` to use `switch` statements so the database could be accessed cleanly through the terminal.

---

### 2. “I need assistance with getting the Java file to read the app.properties file. Help us accomplish this please.”
**What we adopted:**
- ChatGPT identified the missing imports:  
  `java.io.FileInputStream`, `java.io.IOException`, `java.util.Properties`.
- Provided code to correctly read the database URL, username, and password from `app.properties`.

---

### 3. “Can you help us make the output of table viewing look much fancier for our view methods?”
**What we adopted:**
- Added helper methods: `printTableRow` and `printTableSeparator`.
- Added calculations for dynamic column widths.
- Converted rows to formatted strings for terminal display.
- Used an `ArrayList` to manage rows before printing.

---

### 4. “Can you verify why using the method of default data for PlanTypeInfo and Plan causes errors when adding other data?”
**What we adopted:**
- ChatGPT explained that pre-inserting default data through Java caused dependency issues with tables requiring `PlanTypeInfo` and `Plan` entries.
- We removed the Java data-insertion method and instead inserted initial data manually through MySQL Workbench.

**What we changed:**
- We rejected altering the `add PlanType` Java methods to avoid confusing the test cases or introducing further errors.

---

### 5. “Why will our `cancel_Membership_After_Refund` trigger not fire? Resolve this please.”
**What we adopted:**
- Attempted formatting changes to the SQL file so the trigger would be processed correctly.

**What we changed:**
- Decided to move the trigger into its own SQL file, which resolved the issue, although the underlying cause remains unknown.

---

### 6. “Can you help us make it so the view methods can be used during insertion parts without printing the menu text?”
**What we adopted:**
- Moved menu description text into `Main.java` inside each switch case.
- Ensured the description prints first, followed by the view method execution.
- ChatGPT provided correct syntax to use `DatabaseViews.java` methods inside `DatabaseInsertions.java`.

---

### 7. “Can you prepare sample data for our new database?”
**What we adopted:**
- Added sample data for:
  - `PlanTypeInfo`, `Plan`, `GymMember`, `StaffMember`
  - `Desk`, `Trainer`, `Manager`
  - `Guest`, `GuestVisit`
  - `Membership`, `Payment`, `CheckIn`
  - `TrainerTrainsMember`

**What we changed:**
- ChatGPT suggested six plan types, but we chose to use only three for simplicity.

---

### 8. “What is one way to prevent users from entering invalid inputs during menu selection?”
**What we adopted:**
- Added a helper method to ensure only integer inputs are accepted before entering a `switch` statement.

---

