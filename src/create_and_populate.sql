-- Added DROP statement in case we need to reset all tables 
DROP TABLE IF EXISTS TrainerTrainsMember, CheckIn, Payment, Membership, GuestVisit, Guest, Manager, Trainer, Desk, StaffMember, Plan, PlanTypeInfo, GymMember;

-- Order of creation, PlanTypeInfo & Plan, GymMember, StaffMember & Subentities, Guest & GuestVisit, Membership, Payment, CheckIn, TrainerTrainsMember
-- This is done so we can make the foreign key connections properly 
-- If trigger does not work when running entire file, run the trigger section separately (lines 327-345) in a separate file in MySQL or use triggers.sql and paste it into a separate file in MySQL and run that file instead.

-- ____________________________________________________________
-- PlanTypeInfo & Plan Table (these are connected)
-- Note: Table renamed from PlanType to PlanTypeInfo to avoid ambiguity with planType attribute
CREATE TABLE PlanTypeInfo (
  planType ENUM('Monthly', 'Monthly Premium', 'Annual') PRIMARY KEY,
  price DECIMAL(10,2) NOT NULL,
  CHECK (price > 0)
);

-- Insert test data for PlanTypeInfo
INSERT INTO PlanTypeInfo (planType, price) VALUES
('Monthly', 49.99),
('Monthly Premium', 79.99),
('Annual', 499.99);

CREATE TABLE Plan (
  planID INT AUTO_INCREMENT PRIMARY KEY,
  planType ENUM('Monthly','Monthly Premium', 'Annual') NOT NULL,
  FOREIGN KEY (planType) REFERENCES PlanTypeInfo(planType)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
);

-- Insert test data for Plan
INSERT INTO Plan (planType) VALUES
('Monthly'),
('Monthly Premium'),
('Annual');

-- ____________________________________________________________
-- GymMember Table 
CREATE TABLE GymMember (
  memberID INT AUTO_INCREMENT PRIMARY KEY,
  firstName VARCHAR(50) NOT NULL,
  lastName  VARCHAR(50) NOT NULL,
  birthday  DATE NOT NULL,
  phoneNumber VARCHAR(15) UNIQUE,
  email VARCHAR(100) UNIQUE,
  dateJoined DATE NOT NULL DEFAULT (CURRENT_DATE)
);

-- Insert test data for GymMember
INSERT INTO GymMember (firstName, lastName, birthday, phoneNumber, email, dateJoined) VALUES
('John', 'Smith', '1990-05-15', '408-555-0101', 'john.smith@email.com', '2024-01-15'),
('Sarah', 'Johnson', '1988-08-22', '408-555-0102', 'sarah.j@email.com', '2024-02-20'),
('Michael', 'Williams', '1995-03-10', '408-555-0103', 'mike.will@email.com', '2024-03-05'),
('Emily', 'Brown', '1992-11-30', '408-555-0104', 'emily.brown@email.com', '2024-01-10'),
('David', 'Martinez', '1985-07-18', '408-555-0105', 'david.m@email.com', '2024-04-12');

-- ____________________________________________________________
-- StaffMember Table and its subentities 
CREATE TABLE StaffMember (
  staffID INT AUTO_INCREMENT PRIMARY KEY,
  firstName VARCHAR(50) NOT NULL,
  lastName  VARCHAR(50) NOT NULL,
  phoneNumber VARCHAR(15) UNIQUE,
  email VARCHAR(100) UNIQUE,
  hireDate DATE,
  salary DECIMAL(10,2)
);

-- Insert test data for StaffMember
INSERT INTO StaffMember (firstName, lastName, phoneNumber, email, hireDate, salary) VALUES
('Amanda', 'Davis', '408-555-0201', 'amanda.d@gym.com', '2023-01-15', 45000.00),
('Robert', 'Wilson', '408-555-0202', 'robert.w@gym.com', '2023-03-20', 55000.00),
('Jessica', 'Garcia', '408-555-0203', 'jessica.g@gym.com', '2023-02-10', 48000.00),
('Carlos', 'Rodriguez', '408-555-0204', 'carlos.r@gym.com', '2022-06-15', 65000.00),
('Lisa', 'Anderson', '408-555-0205', 'lisa.a@gym.com', '2022-01-10', 75000.00);

-- StaffMember Subentities 
CREATE TABLE Desk (
  staffID INT PRIMARY KEY,
  schedule VARCHAR(100),
  deskLocation VARCHAR(100) NOT NULL,
  responsibility VARCHAR(100) NOT NULL,
  FOREIGN KEY (staffID) REFERENCES StaffMember(staffID)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

-- Insert test data for Desk
INSERT INTO Desk (staffID, schedule, deskLocation, responsibility) VALUES
(1, 'Mon-Fri 9AM-5PM', 'Front Desk A', 'Member check-in and payments'),
(3, 'Mon-Fri 1PM-9PM', 'Front Desk B', 'New member registration');

CREATE TABLE Trainer (
  staffID INT PRIMARY KEY,
  specialty VARCHAR(100),
  schedule  VARCHAR(100),
  certificationLevel VARCHAR(50) NOT NULL,
  experience INT NOT NULL,
  FOREIGN KEY (staffID) REFERENCES StaffMember(staffID)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

-- Insert test data for Trainer
INSERT INTO Trainer (staffID, specialty, schedule, certificationLevel, experience) VALUES
(2, 'Strength Training', 'Mon-Fri 6AM-2PM', 'Advanced', 5),
(4, 'Cardio and HIIT', 'Tue-Sat 10AM-6PM', 'Expert', 8);

CREATE TABLE Manager (
  staffID INT PRIMARY KEY,
  department VARCHAR(100) NOT NULL,
  officeLocation VARCHAR(100) NOT NULL UNIQUE,
  experience INT,
  FOREIGN KEY (staffID) REFERENCES StaffMember(staffID)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

-- Insert test data for Manager
INSERT INTO Manager (staffID, department, officeLocation, experience) VALUES
(5, 'Operations', 'Office 101', 10);

-- ____________________________________________________________
-- Guest and GuestVisit Table (these are connected) 
CREATE TABLE Guest (
  guestID INT AUTO_INCREMENT PRIMARY KEY,
  memberID INT NOT NULL,
  firstName VARCHAR(50) NOT NULL,
  lastName  VARCHAR(50) NOT NULL,
  relationshipToMember VARCHAR(50) NOT NULL,
  birthday DATE NOT NULL,
  FOREIGN KEY (memberID) REFERENCES GymMember(memberID)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

-- Insert test data for Guest
INSERT INTO Guest (memberID, firstName, lastName, relationshipToMember, birthday) VALUES
(1, 'Jane', 'Smith', 'Spouse', '1991-06-20'),
(2, 'Tom', 'Johnson', 'Friend', '1989-12-15'),
(3, 'Lisa', 'Williams', 'Sister', '1997-04-25');

CREATE TABLE GuestVisit (
  visitID INT AUTO_INCREMENT PRIMARY KEY,
  guestID INT NOT NULL,
  visitDate DATE NOT NULL,
  FOREIGN KEY (guestID) REFERENCES Guest(guestID)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

-- Insert test data for GuestVisit
INSERT INTO GuestVisit (guestID, visitDate) VALUES
(1, '2024-11-15'),
(1, '2024-11-20'),
(2, '2024-11-18'),
(3, '2024-11-22');

-- ____________________________________________________________
-- Membership Table
CREATE TABLE Membership (
  membershipID INT AUTO_INCREMENT PRIMARY KEY,
  memberID INT NOT NULL,
  planID   INT NOT NULL,
  startDate DATE NOT NULL,
  endDate   DATE NOT NULL,
  status ENUM('Active','Paused', 'Cancelled') NOT NULL DEFAULT 'Paused',
  FOREIGN KEY (memberID) REFERENCES GymMember(memberID)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  FOREIGN KEY (planID) REFERENCES Plan(planID)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CHECK (startDate < endDate)
);

-- Insert test data for Membership
INSERT INTO Membership (memberID, planID, startDate, endDate, status) VALUES
(1, 2, '2024-01-15', '2024-02-15', 'Active'),
(2, 1, '2024-02-20', '2024-03-20', 'Active'),
(3, 3, '2024-03-05', '2025-03-05', 'Active'),
(4, 2, '2024-01-10', '2024-02-10', 'Paused'),
(5, 1, '2024-04-12', '2024-05-12', 'Active');

-- ____________________________________________________________
-- Payment Table
CREATE TABLE Payment (
  paymentID INT AUTO_INCREMENT PRIMARY KEY,
  staffID INT NOT NULL,
  memberID INT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  paymentType ENUM('CASH','CARD','ONLINE') NOT NULL,
  dateOfPayment DATE NOT NULL DEFAULT (CURRENT_DATE),
  status ENUM('Success','Pending','Failed','Refunded') NOT NULL DEFAULT 'Pending',
  FOREIGN KEY (staffID) REFERENCES Desk(staffID)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  FOREIGN KEY (memberID) REFERENCES GymMember(memberID)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CHECK (amount > 0)
);

-- Insert test data for Payment
INSERT INTO Payment (staffID, memberID, amount, paymentType, dateOfPayment, status) VALUES
(1, 1, 79.99, 'CARD', '2024-01-15', 'Success'),
(1, 2, 49.99, 'CASH', '2024-02-20', 'Success'),
(3, 3, 499.99, 'ONLINE', '2024-03-05', 'Success'),
(1, 4, 79.99, 'CARD', '2024-01-10', 'Success'),
(3, 5, 49.99, 'CARD', '2024-04-12', 'Success'),
(1, 1, 79.99, 'CARD', '2024-11-15', 'Pending');

-- ____________________________________________________________
-- CheckIn Table
CREATE TABLE CheckIn (
  checkInID INT AUTO_INCREMENT PRIMARY KEY,
  membershipID INT NOT NULL,
  staffID INT NOT NULL,
  ts DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- had to change timestamp attribute name to ts so sql doesn't get confused 
  location VARCHAR(50),
  
  FOREIGN KEY (membershipID) REFERENCES Membership(membershipID)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  FOREIGN KEY (staffID) REFERENCES Desk(staffID)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
);

-- Insert test data for CheckIn
INSERT INTO CheckIn (membershipID, staffID, ts, location) VALUES
(1, 1, '2024-11-20 08:30:00', 'Main Entrance'),
(2, 1, '2024-11-20 09:15:00', 'Main Entrance'),
(3, 3, '2024-11-20 10:00:00', 'Main Entrance'),
(5, 1, '2024-11-21 07:45:00', 'Main Entrance'),
(1, 3, '2024-11-21 18:30:00', 'Main Entrance'),
(2, 1, '2024-11-22 08:00:00', 'Main Entrance');

-- ____________________________________________________________
-- TrainerTrainsMember Table 
CREATE TABLE TrainerTrainsMember (
  trainerID INT NOT NULL,
  memberID  INT NOT NULL,
  PRIMARY KEY (trainerID, memberID),
  FOREIGN KEY (trainerID) REFERENCES Trainer(staffID)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  FOREIGN KEY (memberID) REFERENCES GymMember(memberID)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

-- Insert test data for TrainerTrainsMember
INSERT INTO TrainerTrainsMember (trainerID, memberID) VALUES
(2, 1),
(2, 3),
(4, 2),
(4, 5),
(2, 4);

-- Create a VIEW for active members with their membership details
CREATE OR REPLACE VIEW ActiveMembersView AS
SELECT 
    g.memberID,
    CONCAT(g.firstName, ' ', g.lastName) AS memberName,
    g.email,
    g.phoneNumber,
    m.membershipID,
    m.startDate,
    m.endDate,
    pt.planType,
    pt.price
FROM GymMember g
JOIN Membership m ON g.memberID = m.memberID
JOIN Plan p ON m.planID = p.planID
JOIN PlanTypeInfo pt ON p.planType = pt.planType
WHERE m.status = 'Active';

-- Create a stored procedure to get member payment history
DELIMITER //

CREATE PROCEDURE GetMemberPaymentHistory(IN p_memberID INT)
BEGIN
    SELECT 
        paymentID,
        amount,
        paymentType,
        dateOfPayment,
        status,
        CONCAT(s.firstName, ' ', s.lastName) AS processedBy
    FROM Payment p
    JOIN StaffMember s ON p.staffID = s.staffID
    WHERE p.memberID = p_memberID
    ORDER BY dateOfPayment DESC;
END //

DELIMITER ;

-- Create a stored function to calculate total revenue from payments
DELIMITER //

CREATE FUNCTION CalculateTotalRevenue(p_status VARCHAR(20))
RETURNS DECIMAL(10,2)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE total DECIMAL(10,2);
    
    IF p_status IS NULL OR p_status = '' THEN
        SELECT COALESCE(SUM(amount), 0) INTO total
        FROM Payment
        WHERE status = 'Success';
    ELSE
        SELECT COALESCE(SUM(amount), 0) INTO total
        FROM Payment
        WHERE status = p_status;
    END IF;
    
    RETURN total;
END //

DELIMITER ;

-- Trigger Example 
-- Auto-cancel membership when payment is refunded
-- IMPORTANT: If trigger doesn't work when running entire file, 
-- run the trigger section separately (lines 327-345) or use triggers.sql

DROP TRIGGER IF EXISTS activate_Membership_After_Payment;
DROP TRIGGER IF EXISTS cancel_Membership_After_Refund;

DELIMITER //

CREATE TRIGGER cancel_Membership_After_Refund
AFTER UPDATE ON Payment
FOR EACH ROW
BEGIN
  -- Check if payment was changed to Refunded
  IF NEW.status = 'Refunded' AND OLD.status != 'Refunded' THEN
    UPDATE Membership
    SET status = 'Cancelled'
    WHERE memberID = NEW.memberID
      AND status IN ('Active', 'Paused');
  END IF;
END //

DELIMITER ;

