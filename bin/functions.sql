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



