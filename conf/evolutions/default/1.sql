# --- !Ups

CREATE TABLE users (userid INTEGER PRIMARY KEY, token CHAR(32));

CREATE SEQUENCE link_ids;
CREATE TABLE links (id INTEGER PRIMARY KEY DEFAULT NEXTVAL('link_ids'), url VARCHAR(2000), code VARCHAR(100));

CREATE SEQUENCE click_ids;
CREATE TABLE clicks (id INTEGER PRIMARY KEY DEFAULT NEXTVAL('click_ids'), referer VARCHAR(2000), remote_ip VARCHAR(15), link_id INTEGER);

# --- !Downs

DROP TABLE users;

DROP SEQUENCE link_ids;
DROP TABLE links;

DROP SEQUENCE click_ids;
DROP TABLE clicks;
