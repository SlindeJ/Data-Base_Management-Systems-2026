-- Start in a fresh session. Record server duration for each SELECT.
SET profiling = 1;
SET profiling_history_size = 100;
-- Q1: first run warms the cache; use the next three durations.
SELECT b.book_id, b.title, p.publisher_name
FROM book b LEFT JOIN publisher p ON p.publisher_id = b.publisher_id
WHERE b.title LIKE '%Harry Potter%'
ORDER BY b.title, b.book_id LIMIT 20;
SELECT b.book_id, b.title, p.publisher_name
FROM book b LEFT JOIN publisher p ON p.publisher_id = b.publisher_id
WHERE b.title LIKE '%Harry Potter%'
ORDER BY b.title, b.book_id LIMIT 20;
SELECT b.book_id, b.title, p.publisher_name
FROM book b LEFT JOIN publisher p ON p.publisher_id = b.publisher_id
WHERE b.title LIKE '%Harry Potter%'
ORDER BY b.title, b.book_id LIMIT 20;
SELECT b.book_id, b.title, p.publisher_name
FROM book b LEFT JOIN publisher p ON p.publisher_id = b.publisher_id
WHERE b.title LIKE '%Harry Potter%'
ORDER BY b.title, b.book_id LIMIT 20;
EXPLAIN SELECT b.book_id, b.title, p.publisher_name
FROM book b LEFT JOIN publisher p ON p.publisher_id = b.publisher_id
WHERE b.title LIKE '%Harry Potter%'
ORDER BY b.title, b.book_id LIMIT 20;

-- Q3: first run warms the cache; use the next three durations.
SELECT b.book_id, b.title, bt.tag_count
FROM tag t JOIN book_tag bt ON bt.tag_id = t.tag_id
JOIN book b ON b.book_id = bt.book_id
WHERE t.tag_name = 'fantasy'
ORDER BY bt.tag_count DESC, b.book_id LIMIT 20;
SELECT b.book_id, b.title, bt.tag_count
FROM tag t JOIN book_tag bt ON bt.tag_id = t.tag_id
JOIN book b ON b.book_id = bt.book_id
WHERE t.tag_name = 'fantasy'
ORDER BY bt.tag_count DESC, b.book_id LIMIT 20;
SELECT b.book_id, b.title, bt.tag_count
FROM tag t JOIN book_tag bt ON bt.tag_id = t.tag_id
JOIN book b ON b.book_id = bt.book_id
WHERE t.tag_name = 'fantasy'
ORDER BY bt.tag_count DESC, b.book_id LIMIT 20;
SELECT b.book_id, b.title, bt.tag_count
FROM tag t JOIN book_tag bt ON bt.tag_id = t.tag_id
JOIN book b ON b.book_id = bt.book_id
WHERE t.tag_name = 'fantasy'
ORDER BY bt.tag_count DESC, b.book_id LIMIT 20;
EXPLAIN SELECT b.book_id, b.title, bt.tag_count
FROM tag t JOIN book_tag bt ON bt.tag_id = t.tag_id
JOIN book b ON b.book_id = bt.book_id
WHERE t.tag_name = 'fantasy'
ORDER BY bt.tag_count DESC, b.book_id LIMIT 20;

-- Q4: first run warms the cache; use the next three durations.
SELECT b.book_id, b.title, ROUND(AVG(r.rating), 2) AS average_rating,
       COUNT(*) AS rating_count
FROM book b JOIN rating r ON r.book_id = b.book_id
GROUP BY b.book_id, b.title
HAVING COUNT(*) >= 50
ORDER BY average_rating DESC, rating_count DESC, b.book_id LIMIT 20;
SELECT b.book_id, b.title, ROUND(AVG(r.rating), 2) AS average_rating,
       COUNT(*) AS rating_count
FROM book b JOIN rating r ON r.book_id = b.book_id
GROUP BY b.book_id, b.title
HAVING COUNT(*) >= 50
ORDER BY average_rating DESC, rating_count DESC, b.book_id LIMIT 20;
SELECT b.book_id, b.title, ROUND(AVG(r.rating), 2) AS average_rating,
       COUNT(*) AS rating_count
FROM book b JOIN rating r ON r.book_id = b.book_id
GROUP BY b.book_id, b.title
HAVING COUNT(*) >= 50
ORDER BY average_rating DESC, rating_count DESC, b.book_id LIMIT 20;
SELECT b.book_id, b.title, ROUND(AVG(r.rating), 2) AS average_rating,
       COUNT(*) AS rating_count
FROM book b JOIN rating r ON r.book_id = b.book_id
GROUP BY b.book_id, b.title
HAVING COUNT(*) >= 50
ORDER BY average_rating DESC, rating_count DESC, b.book_id LIMIT 20;
EXPLAIN SELECT b.book_id, b.title, ROUND(AVG(r.rating), 2) AS average_rating,
       COUNT(*) AS rating_count
FROM book b JOIN rating r ON r.book_id = b.book_id
GROUP BY b.book_id, b.title
HAVING COUNT(*) >= 50
ORDER BY average_rating DESC, rating_count DESC, b.book_id LIMIT 20;

-- Q5: first run warms the cache; use the next three durations.
SELECT b.book_id, b.title, rl.date_added
FROM reading_list rl JOIN book b ON b.book_id = rl.book_id
WHERE rl.user_id = 1
ORDER BY b.title, b.book_id LIMIT 20;
SELECT b.book_id, b.title, rl.date_added
FROM reading_list rl JOIN book b ON b.book_id = rl.book_id
WHERE rl.user_id = 1
ORDER BY b.title, b.book_id LIMIT 20;
SELECT b.book_id, b.title, rl.date_added
FROM reading_list rl JOIN book b ON b.book_id = rl.book_id
WHERE rl.user_id = 1
ORDER BY b.title, b.book_id LIMIT 20;
SELECT b.book_id, b.title, rl.date_added
FROM reading_list rl JOIN book b ON b.book_id = rl.book_id
WHERE rl.user_id = 1
ORDER BY b.title, b.book_id LIMIT 20;
EXPLAIN SELECT b.book_id, b.title, rl.date_added
FROM reading_list rl JOIN book b ON b.book_id = rl.book_id
WHERE rl.user_id = 1
ORDER BY b.title, b.book_id LIMIT 20;

-- Q10: first run warms the cache; use the next three durations.
SELECT b.book_id, b.title
FROM book b LEFT JOIN rating r
  ON r.book_id = b.book_id AND r.user_id = 1
WHERE r.user_id IS NULL
ORDER BY b.book_id LIMIT 20;
SELECT b.book_id, b.title
FROM book b LEFT JOIN rating r
  ON r.book_id = b.book_id AND r.user_id = 1
WHERE r.user_id IS NULL
ORDER BY b.book_id LIMIT 20;
SELECT b.book_id, b.title
FROM book b LEFT JOIN rating r
  ON r.book_id = b.book_id AND r.user_id = 1
WHERE r.user_id IS NULL
ORDER BY b.book_id LIMIT 20;
SELECT b.book_id, b.title
FROM book b LEFT JOIN rating r
  ON r.book_id = b.book_id AND r.user_id = 1
WHERE r.user_id IS NULL
ORDER BY b.book_id LIMIT 20;
EXPLAIN SELECT b.book_id, b.title
FROM book b LEFT JOIN rating r
  ON r.book_id = b.book_id AND r.user_id = 1
WHERE r.user_id IS NULL
ORDER BY b.book_id LIMIT 20;
SHOW PROFILES;
SET profiling = 0;
