-- Added DROP statement in case we need to reset all tables 
DROP TABLE IF EXISTS TrainerTrainsMember, CheckIn, Payment, Membership, GuestVisit, Guest, Manager, Trainer, Desk, StaffMember, Plan, PlanTypeInfo, GymMember;

-- Order of creation, PlanTypeInfo & Plan, GymMember, StaffMember & Subentities, Guest & GuestVisit, Membership, Payment, CheckIn, TrainerTrainsMember
-- This is done so we can make the foreign key connections properly 

-- ____________________________________________________________
-- PlanTypeInfo & Plan Table (these are connected)
-- Note: Table renamed from PlanType to PlanTypeInfo to avoid ambiguity with planType attribute
CREATE TABLE PlanTypeInfo (
  planType ENUM('Monthly', 'Monthly Premium', 'Annual') PRIMARY KEY,
  price DECIMAL(10,2) NOT NULL,
  CHECK (price > 0)
);

CREATE TABLE Plan (
  planID INT AUTO_INCREMENT PRIMARY KEY,
  planType ENUM('Monthly','Monthly Premium', 'Annual') NOT NULL,
  FOREIGN KEY (planType) REFERENCES PlanTypeInfo(planType)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
);

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

CREATE TABLE Manager (
  staffID INT PRIMARY KEY,
  department VARCHAR(100) NOT NULL,
  officeLocation VARCHAR(100) NOT NULL UNIQUE,
  experience INT,
  FOREIGN KEY (staffID) REFERENCES StaffMember(staffID)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

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

CREATE TABLE GuestVisit (
  visitID INT AUTO_INCREMENT PRIMARY KEY,
  guestID INT NOT NULL,
  visitDate DATE NOT NULL,
  FOREIGN KEY (guestID) REFERENCES Guest(guestID)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

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

