-- Q1 | Level 2 | Search by title
SELECT b.book_id, b.title, p.publisher_name
FROM book b LEFT JOIN publisher p ON p.publisher_id = b.publisher_id
WHERE b.title LIKE '%Harry Potter%'
ORDER BY b.title, b.book_id LIMIT 20;

-- Q2 | Level 2 | Browse an author
SELECT b.book_id, b.title
FROM author a JOIN book_author ba ON ba.author_id = a.author_id
JOIN book b ON b.book_id = ba.book_id
WHERE a.author_name = 'J.K. Rowling'
ORDER BY b.title, b.book_id LIMIT 20;

-- Q3 | Level 2 | Browse a tag
SELECT b.book_id, b.title, bt.tag_count
FROM tag t JOIN book_tag bt ON bt.tag_id = t.tag_id
JOIN book b ON b.book_id = bt.book_id
WHERE t.tag_name = 'fantasy'
ORDER BY bt.tag_count DESC, b.book_id LIMIT 20;

-- Q4 | Level 2 | Find highly rated books
SELECT b.book_id, b.title, ROUND(AVG(r.rating), 2) AS average_rating,
       COUNT(*) AS rating_count
FROM book b JOIN rating r ON r.book_id = b.book_id
GROUP BY b.book_id, b.title
HAVING COUNT(*) >= 50
ORDER BY average_rating DESC, rating_count DESC, b.book_id LIMIT 20;

-- Q5 | Level 2 | Open a reading list
SELECT b.book_id, b.title, rl.date_added
FROM reading_list rl JOIN book b ON b.book_id = rl.book_id
WHERE rl.user_id = 1
ORDER BY b.title, b.book_id LIMIT 20;

-- Q6 | Level 2 | Review past ratings
SELECT u.user_name, b.title, r.rating
FROM `user` u JOIN rating r ON r.user_id = u.user_id
JOIN book b ON b.book_id = r.book_id
WHERE u.user_id = 1
ORDER BY r.rating DESC, b.title, b.book_id LIMIT 20;

-- Q7 | Level 2 | Count books by publisher
SELECT p.publisher_name, COUNT(b.book_id) AS book_count
FROM publisher p LEFT JOIN book b ON b.publisher_id = p.publisher_id
GROUP BY p.publisher_id, p.publisher_name
ORDER BY book_count DESC, p.publisher_id;

-- Q8 | Level 2 | View author credits
SELECT b.title, a.author_name
FROM book b JOIN book_author ba ON ba.book_id = b.book_id
JOIN author a ON a.author_id = ba.author_id
WHERE b.book_id = 2
ORDER BY a.author_name, a.author_id;

-- Q9 | Level 2 | View a book’s tags
SELECT b.title, t.tag_name, bt.tag_count
FROM book b JOIN book_tag bt ON bt.book_id = b.book_id
JOIN tag t ON t.tag_id = bt.tag_id
WHERE b.book_id = 1
ORDER BY bt.tag_count DESC, t.tag_id LIMIT 20;

-- Q10 | Level 2 | Find books a reader has not rated
SELECT b.book_id, b.title
FROM book b LEFT JOIN rating r
  ON r.book_id = b.book_id AND r.user_id = 1
WHERE r.user_id IS NULL
ORDER BY b.book_id LIMIT 20;
