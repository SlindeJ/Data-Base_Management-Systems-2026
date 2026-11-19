SET NAMES utf8mb4;

CREATE TABLE author (
  author_id INT PRIMARY KEY,
  author_name VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE publisher (
  publisher_id INT PRIMARY KEY,
  publisher_name VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE tag (
  tag_id INT PRIMARY KEY,
  tag_name VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user` (
  user_id INT PRIMARY KEY,
  user_name VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE book (
  book_id INT PRIMARY KEY,
  title VARCHAR(500) NOT NULL CHECK (CHAR_LENGTH(TRIM(title)) > 0),
  publication_year SMALLINT,
  language_code VARCHAR(20),
  publisher_id INT,
  FOREIGN KEY (publisher_id) REFERENCES publisher(publisher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE book_author (
  book_id INT,
  author_id INT,
  PRIMARY KEY (book_id, author_id),
  FOREIGN KEY (book_id) REFERENCES book(book_id),
  FOREIGN KEY (author_id) REFERENCES author(author_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE rating (
  user_id INT,
  book_id INT,
  rating TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
  PRIMARY KEY (user_id, book_id),
  FOREIGN KEY (user_id) REFERENCES `user`(user_id),
  FOREIGN KEY (book_id) REFERENCES book(book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE book_tag (
  book_id INT,
  tag_id INT,
  tag_count INT NOT NULL CHECK (tag_count > 0),
  PRIMARY KEY (book_id, tag_id),
  FOREIGN KEY (book_id) REFERENCES book(book_id),
  FOREIGN KEY (tag_id) REFERENCES tag(tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reading_list (
  user_id INT,
  book_id INT,
  date_added DATE,
  PRIMARY KEY (user_id, book_id),
  FOREIGN KEY (user_id) REFERENCES `user`(user_id),
  FOREIGN KEY (book_id) REFERENCES book(book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
