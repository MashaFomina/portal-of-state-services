# drop all tables
DROP TABLE IF EXISTS representatives;
DROP TABLE IF EXISTS tickets;
DROP TABLE IF EXISTS doctors;
DROP TABLE IF EXISTS edu_requests;
DROP TABLE IF EXISTS feedbacks;
DROP TABLE IF EXISTS medical_institutions;
DROP TABLE IF EXISTS educational_institutions_seats;
DROP TABLE IF EXISTS educational_institutions;
DROP TABLE IF EXISTS institutions;
DROP TABLE IF EXISTS childs;
DROP TABLE IF EXISTS citizens;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS users;

# create tables
CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  login VARCHAR(100) UNIQUE NOT NULL,
  password CHAR(40),   # sha1 hash
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL,
  is_admin TINYINT(1) DEFAULT 0
);

CREATE TABLE notifications (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user INT NOT NULL,
  notification VARCHAR(1000) NOT NULL,
  created datetime NOT NULL,
  FOREIGN KEY (user) REFERENCES users(id)
);

CREATE TABLE citizens (
  user INT NOT NULL PRIMARY KEY,
  policy VARCHAR(16) NOT NULL,
  passport VARCHAR(10) NOT NULL,
  birth_date datetime NOT NULL,
  FOREIGN KEY (user) REFERENCES users(id)
);

CREATE TABLE childs (
  id INT AUTO_INCREMENT PRIMARY KEY,
  parent INT NOT NULL,
  fullName VARCHAR(100) NOT NULL,
  birth_certificate VARCHAR(10) NOT NULL,
  birth_date datetime NOT NULL,
  FOREIGN KEY (parent) REFERENCES citizens(user)
);

CREATE TABLE institutions (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(300) NOT NULL,
  city VARCHAR(100) NOT NULL,
  district VARCHAR(100) NOT NULL,
  telephone VARCHAR(16) NOT NULL,
  fax VARCHAR(60) NOT NULL,
  address VARCHAR(200) NOT NULL
);

CREATE TABLE medical_institutions (
  institution_id INT NOT NULL PRIMARY KEY,
  FOREIGN KEY (institution_id) REFERENCES institutions(id)
);

CREATE TABLE educational_institutions (
  institution_id INT NOT NULL PRIMARY KEY,
  FOREIGN KEY (institution_id) REFERENCES institutions(id)
);

CREATE TABLE educational_institutions_seats (
  institution_id INT NOT NULL,
  class_number INT NOT NULL,
  seats INT NOT NULL,
  busy_seats INT NOT NULL,
  PRIMARY KEY (institution_id, class_number),
  FOREIGN KEY (institution_id) REFERENCES educational_institutions(institution_id)
);

CREATE TABLE feedbacks (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user INT NOT NULL,
  feedback_text VARCHAR(1000) NOT NULL,
  created datetime NOT NULL,
  institution_id INT NOT NULL,
  to_user INT DEFAULT NULL,
  FOREIGN KEY (user) REFERENCES users(id),
  FOREIGN KEY (to_user) REFERENCES users(id),
  FOREIGN KEY (institution_id) REFERENCES institutions(id)
);

CREATE TABLE edu_requests (
  id INT AUTO_INCREMENT PRIMARY KEY,
  status ENUM("OPENED", "ACCEPTED_BY_INSTITUTION", "ACCEPTED_BY_PARENT", "REFUSED", "CHILD_IS_ENROLLED") NOT NULL,
  child INT NOT NULL,
  parent INT NOT NULL,
  institution_id INT NOT NULL,
  creation_date datetime NOT NULL,
  appointment datetime DEFAULT NULL,
  class_number int NOT NULL,
  FOREIGN KEY (child) REFERENCES citizens(user),
  FOREIGN KEY (parent) REFERENCES childs(id),
  FOREIGN KEY (institution_id) REFERENCES educational_institutions(institution_id)
);

CREATE TABLE doctors (
  user INT NOT NULL PRIMARY KEY,
  position VARCHAR(100) NOT NULL,
  summary VARCHAR(1000) NOT NULL,
  institution_id INT NOT NULL,
  approved TINYINT(1) DEFAULT 0,
  FOREIGN KEY (user) REFERENCES users(id),
  FOREIGN KEY (institution_id) REFERENCES medical_institutions(institution_id)
);

CREATE TABLE tickets (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user INT DEFAULT NULL,
  child INT DEFAULT NULL,
  doctor INT NOT NULL,
  ticket_date datetime NOT NULL,
  visited TINYINT(1) DEFAULT 0,
  summary VARCHAR(1000) DEFAULT NULL,
  FOREIGN KEY (user) REFERENCES citizens(user),
  FOREIGN KEY (child) REFERENCES childs(id),
  FOREIGN KEY (doctor) REFERENCES doctors(user)
);

CREATE TABLE representatives (
  user INT NOT NULL PRIMARY KEY,
  institution_id INT NOT NULL,
  approved TINYINT(1) DEFAULT 0,
  FOREIGN KEY (user) REFERENCES users(id),
  FOREIGN KEY (institution_id) REFERENCES institutions(id)
);

# inser data for tests
INSERT INTO users(login, full_name, email, password, is_admin) VALUES ("admin", "admin", "admin@mail.com", SHA1("admin"), 1);

INSERT INTO users(login, full_name, email, password) VALUES ("citizen", "citizen", "citizen@mail.com", SHA1("pass"));
INSERT INTO citizens (user, policy, passport, birth_date) VALUES ((SELECT id FROM users WHERE login = "citizen"), "1234567891234566", "4050123450", "1995-01-04");
INSERT INTO childs (parent, fullName, birth_certificate, birth_date) VALUES ((SELECT user FROM citizens WHERE policy = "1234567891234566"), "F I O", "IJ1229394844", "2015-01-14");

INSERT INTO users(login, full_name, email, password) VALUES ("citizen1", "citizen1", "citizen1@mail.com", SHA1("pass"));
INSERT INTO citizens (user, policy, passport, birth_date) VALUES ((SELECT id FROM users WHERE login = "citizen1"), "1234567891234567", "4050123450", "1995-01-04");

INSERT INTO institutions (title, city, district, telephone, fax, address)  VALUES ("school № 1", "Saint-Petersburg", "Kirovskyi", "88127777777", "88127777777", "pr. Veteranov h. 69");
INSERT INTO institutions (title, city, district, telephone, fax, address)  VALUES ("school № 2", "Saint-Petersburg", "Kirovskyi", "88127777779", "88127777779", "pr. Veteranov h. 79");
INSERT INTO educational_institutions (institution_id) VALUES ((SELECT id FROM institutions WHERE title = "school № 1"));
INSERT INTO educational_institutions (institution_id) VALUES ((SELECT id FROM institutions WHERE title = "school № 2"));
INSERT INTO educational_institutions_seats (institution_id, class_number, seats, busy_seats) VALUES ((SELECT id FROM institutions WHERE title = "school № 1"), 1, 50, 10);
INSERT INTO educational_institutions_seats (institution_id, class_number, seats, busy_seats) VALUES ((SELECT id FROM institutions WHERE title = "school № 2"), 1, 20, 5);

INSERT INTO users(login, full_name, email, password) VALUES ("edur", "edur", "edur@mail.com", SHA1("pass"));
INSERT INTO representatives (user, institution_id, approved) VALUES ((SELECT id FROM users WHERE login = "edur"), (SELECT id FROM institutions WHERE title = "school № 1"), 1);

INSERT INTO institutions (title, city, district, telephone, fax, address)  VALUES ("hospital № 1", "Saint-Petersburg", "Kirovskyi", "88127777777", "88127777777", "pr. Veteranov h. 69");
INSERT INTO institutions (title, city, district, telephone, fax, address)  VALUES ("hospital № 2", "Saint-Petersburg", "Kirovskyi", "88127777778", "88127777778", "pr. Veteranov h. 69");
INSERT INTO medical_institutions (institution_id) VALUES ((SELECT id FROM institutions WHERE title = "hospital № 1"));
INSERT INTO medical_institutions (institution_id) VALUES ((SELECT id FROM institutions WHERE title = "hospital № 2"));

INSERT INTO users(login, full_name, email, password) VALUES ("medr", "medr", "medr@mail.com", SHA1("pass"));
INSERT INTO representatives (user, institution_id, approved) VALUES ((SELECT id FROM users WHERE login = "medr"), (SELECT id FROM institutions WHERE title = "hospital № 1"), 1);
INSERT INTO users(login, full_name, email, password) VALUES ("medr1", "medr1", "medr1@mail.com", SHA1("pass"));
INSERT INTO representatives (user, institution_id, approved) VALUES ((SELECT id FROM users WHERE login = "medr1"), (SELECT id FROM institutions WHERE title = "hospital № 2"), 1);

INSERT INTO users(login, full_name, email, password) VALUES ("doctor", "doctor", "doctor@mail.com", SHA1("pass"));  
INSERT INTO users(login, full_name, email, password) VALUES ("doctor1", "doctor1", "doctor1@mail.com", SHA1("pass"));           
INSERT INTO  doctors (user, position, summary, institution_id, approved) VALUES ((SELECT id FROM users WHERE login = "doctor"), "therapist", "good doctor", (SELECT id FROM institutions WHERE title = "hospital № 1"), 1);
INSERT INTO  doctors (user, position, summary, institution_id, approved) VALUES ((SELECT id FROM users WHERE login = "doctor1"), "therapist", "good doctor", (SELECT id FROM institutions WHERE title = "hospital № 2"), 1);   