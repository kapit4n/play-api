# --- !Downs
drop table IF EXISTS comments;
drop table IF EXISTS news;

## --- !Ups
create table news (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(100) not null,
  body TEXT,
  likes INT(6)
);

create table comments (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  newsId INT(6) UNSIGNED,
  body VARCHAR(100),
  likes INT(6),
  FOREIGN KEY (newsId)
    REFERENCES news (id)
);
