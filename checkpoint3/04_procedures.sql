DELIMITER $$

CREATE PROCEDURE sp_search_books(
    IN p_text VARCHAR(255))
SQL SECURITY INVOKER
BEGIN
  SELECT b.book_id, b.title, p.publisher_name
  FROM book b LEFT JOIN publisher p ON p.publisher_id = b.publisher_id
  WHERE b.title LIKE CONCAT('%', p_text, '%')
  ORDER BY b.title, b.book_id LIMIT 20;
END$$

CREATE PROCEDURE sp_save_rating(
    IN p_user INT, IN p_book INT, IN p_rating INT)
SQL SECURITY INVOKER
BEGIN
  IF p_rating IS NULL OR p_rating NOT BETWEEN 1 AND 5 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Rating must be 1 to 5';
  END IF;
  INSERT INTO rating (user_id, book_id, rating)
  VALUES (p_user, p_book, p_rating)
  ON DUPLICATE KEY UPDATE rating = p_rating;
  SELECT user_id, book_id, rating FROM rating
  WHERE user_id = p_user AND book_id = p_book;
END$$

CREATE PROCEDURE sp_add_to_reading_list(
    IN p_user INT, IN p_book INT, IN p_date DATE)
SQL SECURITY INVOKER
BEGIN
  IF p_date IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Supply the date added';
  END IF;
  INSERT INTO reading_list (user_id, book_id, date_added)
  VALUES (p_user, p_book, p_date);
  SELECT user_id, book_id, date_added FROM reading_list
  WHERE user_id = p_user AND book_id = p_book;
END$$

CREATE PROCEDURE sp_remove_from_reading_list(
    IN p_user INT, IN p_book INT)
SQL SECURITY INVOKER
BEGIN
  DELETE FROM reading_list
  WHERE user_id = p_user AND book_id = p_book;
  SELECT ROW_COUNT() AS removed_entries;
END$$

CREATE PROCEDURE sp_get_reading_list(
    IN p_user INT)
SQL SECURITY INVOKER
BEGIN
  SELECT b.book_id, b.title, rl.date_added
  FROM reading_list rl JOIN book b ON b.book_id = rl.book_id
  WHERE rl.user_id = p_user
  ORDER BY b.title, b.book_id;
END$$

DELIMITER ;
