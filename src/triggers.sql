-- Trigger Example 
-- Auto-cancel membership when payment is refunded
-- Note: DROP statements must execute before DELIMITER change

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

