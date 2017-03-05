# --- !Downs
drop table IF EXISTS news;

## --- !Ups
create table news (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(100) not null,
  body VARCHAR(100),
  likes INT(6)
);
