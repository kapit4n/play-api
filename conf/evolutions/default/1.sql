# --- !Downs
drop table IF EXISTS comments;
drop table IF EXISTS news;
drop table IF EXISTS sources;

## --- !Ups
create table news (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(250) not null,
  body TEXT,
  imgUrl VARCHAR(250),
  source VARCHAR(250),
  likes INT(6),
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

create table sources (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(250) not null,
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

create table comments (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  newsId INT(6) UNSIGNED,
  body text,
  likes INT(6),
  FOREIGN KEY (newsId) REFERENCES news (id)
);
