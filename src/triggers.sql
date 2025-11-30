-- Trigger Example 
-- Check membership before payment update

DELIMITER //

CREATE TRIGGER activate_Membership_After_Payment
AFTER UPDATE ON Payment
FOR EACH ROW
BEGIN
  -- Check if payment was changed to Success
  IF NEW.status = 'Success' THEN
    UPDATE Membership
    SET status = 'Active'
    WHERE memberID = NEW.memberID;
  END IF;
END;
//

DELIMITER ;

-- Trigger Example continued 
