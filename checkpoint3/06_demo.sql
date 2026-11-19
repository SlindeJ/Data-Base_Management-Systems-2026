-- Run in a session with autocommit on and no pending work.
-- Generates an unused reader ID from the current table for this demonstration.
START TRANSACTION;
SELECT COALESCE(MAX(user_id), 0) + 1 INTO @demo_reader FROM `user`;
INSERT INTO `user` (user_id, user_name) VALUES (@demo_reader, 'Test Reader');
SELECT * FROM `user` WHERE user_id = @demo_reader;

CALL sp_search_books('Sorcerer''s Stone');
CALL sp_save_rating(@demo_reader, 1, 4);
CALL sp_save_rating(@demo_reader, 1, 5);
CALL sp_add_to_reading_list(@demo_reader, 2, CURDATE());
CALL sp_get_reading_list(@demo_reader);
CALL sp_remove_from_reading_list(@demo_reader, 2);
CALL sp_get_reading_list(@demo_reader);
ROLLBACK;
SELECT COUNT(*) AS demo_readers_after_rollback FROM `user` WHERE user_id = @demo_reader;
