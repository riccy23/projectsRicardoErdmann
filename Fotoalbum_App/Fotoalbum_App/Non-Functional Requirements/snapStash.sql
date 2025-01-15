/* 
   The users table stores information about the users, including a user_id, username, password, a role which can be admin or not
*/
create table users(
  user_id int auto_increment not null,
  username varchar(20) not null,
  primary key (user_id),
  password varchar(64) not null,
  isAdmin boolean default false
);
/* 
   The photos table contains information about the photos, including a photo_id, URL, title, and a creation date
*/
create table photos(
  photo_id int auto_increment,
  primary key (photo_id),
  url varchar(256) not null,
  title varchar(25) not null,
  creation_date date default current_date
);
/* 
   The albums table stores information about the albums, including a album_id and the album's title.
*/
create table albums(
    album_id int auto_increment,
    primary key (album_id),
     title varchar(25) not null
);

/* 
   The keywords table stores unique keywords that can be assigned to photos and albums for easier searching and categorization.
*/
create table keywords(
    keyword varchar(15),
    primary key (keyword)
);

/* 
   The photos_keywords table is a linking table that assigns photos to keywords, allowing photos to have multiple keywords.
*/
create table photos_keywords(
    photo_id int,
    keyword varchar(15),
    primary key (photo_id, keyword),
    foreign key (photo_id) references photos(photo_id) on delete cascade,
    foreign key (keyword) references keywords(keyword) on delete cascade
);

/* 
   The photos_albums table is a linking table that assigns photos to albums, allowing a photo to be part of an album.
*/
create table photos_albums(
  photo_id int,
  album_id int,
  primary key (photo_id, album_id),
  foreign key (photo_id) references photos(photo_id) on delete cascade,
  foreign key (album_id) references albums(album_id) on delete cascade
);

/* 
   The albums_keywords table is a linking table that assigns keywords to albums, allowing albums to have multiple keywords.
*/
create table albums_keywords(
    album_id int,
    keyword varchar(15),
    primary key (album_id, keyword),
    foreign key (album_id) references albums(album_id) on delete cascade,
    foreign key (keyword) references keywords(keyword) on delete cascade
);


/* 
   The albums_users table is a linking table that assigns albums to users, indicating which users owns which album.
*/
create table albums_users(
    user_id int,
    album_id int,
    primary key (user_id, album_id),
    foreign key (user_id) references users(user_id) on delete cascade,
    foreign key (album_id) references albums(album_id) on delete cascade
);


/* 
   Inserts an admin user with username "admin" and a hashed password (root), with isAdmin set to true.
 */

 INSERT INTO users (username, password, isAdmin) VALUES ("admin", "$2b$10$uiY0uAMBLZt7xdHpUFxGDe2g8rn.8fdNDN/rGDb4X6mZQvenjg2oq", true);


