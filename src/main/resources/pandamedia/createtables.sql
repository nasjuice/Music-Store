DROP TABLE IF EXISTS front_page_settings;
DROP TABLE IF EXISTS invoice_album;
DROP TABLE IF EXISTS invoice_track;
DROP TABLE IF EXISTS invoice;
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS track;
DROP TABLE IF EXISTS album;
DROP TABLE IF EXISTS shop_user;
DROP TABLE IF EXISTS survey;
DROP TABLE IF EXISTS genre;
DROP TABLE IF EXISTS artist;
DROP TABLE IF EXISTS songwriter;
DROP TABLE IF EXISTS recording_label;
DROP TABLE IF EXISTS province;
DROP TABLE IF EXISTS cover_art;
DROP TABLE IF EXISTS newsfeed;
DROP TABLE IF EXISTS advertisement;

-- Genre
CREATE TABLE genre
(
	id INT AUTO_INCREMENT PRIMARY KEY,
	name VARCHAR(255) NOT NULL UNIQUE
);

-- Artist
CREATE TABLE artist
(
	id INT AUTO_INCREMENT PRIMARY KEY,
	name VARCHAR(255) NOT NULL UNIQUE
);

-- Songwriter
CREATE TABLE songwriter
(
	id INT AUTO_INCREMENT PRIMARY KEY,
	name VARCHAR(255) NOT NULL UNIQUE
);

-- Recording Label
CREATE TABLE recording_label
(
	id INT AUTO_INCREMENT PRIMARY KEY,
	name VARCHAR(255) NOT NULL UNIQUE
);

-- Cover Art
CREATE TABLE cover_art
(
	id INT AUTO_INCREMENT PRIMARY KEY,
	image_path VARCHAR(255) NOT NULL UNIQUE
);

-- Survey
CREATE TABLE survey
(
	id INT AUTO_INCREMENT PRIMARY KEY,
	question VARCHAR(255) NOT NULL,
	answer_a VARCHAR(255) NOT NULL,
	answer_b VARCHAR(255) NOT NULL,
	answer_c VARCHAR(255) NOT NULL,
	answer_d VARCHAR(255) NOT NULL,
	votes_a INT NOT NULL DEFAULT 0,
	votes_b INT NOT NULL DEFAULT 0,
	votes_c INT NOT NULL DEFAULT 0,
	votes_d INT NOT NULL DEFAULT 0
);

-- Newsfeed
CREATE TABLE newsfeed
(
	id INT AUTO_INCREMENT PRIMARY KEY,
	url VARCHAR(255) NOT NULL UNIQUE
);

-- Advertisement
CREATE TABLE advertisement
(
	id INT AUTO_INCREMENT PRIMARY KEY,
	ad_path VARCHAR(255) NOT NULL UNIQUE
);

-- Province
CREATE TABLE province
(
	id INT AUTO_INCREMENT PRIMARY KEY,
	name VARCHAR(255) NOT NULL UNIQUE,
	pst_rate DOUBLE NOT NULL DEFAULT 0.0,
	gst_rate DOUBLE NOT NULL DEFAULT 0.0,
	hst_rate DOUBLE NOT NULL DEFAULT 0.0
);

-- Album
CREATE TABLE album
(
	id INT AUTO_INCREMENT PRIMARY KEY,
	title VARCHAR(255) NOT NULL,
	release_date DATE NOT NULL,
	artist_id INT NOT NULL,
	genre_id INT NOT NULL,
	recording_label_id INT NOT NULL,
	num_tracks INT NOT NULL DEFAULT 0,
	cover_art_id INT NOT NULL,
	date_entered DATE NOT NULL,
	cost_price DOUBLE NOT NULL,
	list_price DOUBLE NOT NULL,
	sale_price DOUBLE NOT NULL DEFAULT 0.0,
	removal_status TINYINT NOT NULL DEFAULT 0,
	removal_date DATE,
	FOREIGN KEY (artist_id) REFERENCES artist(id),
	FOREIGN KEY (genre_id) REFERENCES genre(id),
	FOREIGN KEY (recording_label_id) REFERENCES recording_label(id),
	FOREIGN KEY (cover_art_id) REFERENCES cover_art(id),
	CHECK (cost_price < list_price),
	CHECK (sale_price < list_price)
);

-- Track
CREATE TABLE track
(
	id INT AUTO_INCREMENT PRIMARY KEY,
	title VARCHAR(255) NOT NULL,
	album_id INT NOT NULL,
	artist_id INT NOT NULL,
	songwriter_id INT NOT NULL,
	genre_id INT NOT NULL,
	release_date DATE NOT NULL,
	play_length VARCHAR(10) NOT NULL,
	album_track_number INT NOT NULL,
	cover_art_id INT NOT NULL,
	date_entered DATE NOT NULL,
	part_of_album TINYINT NOT NULL DEFAULT 0,
	cost_price DOUBLE NOT NULL,
	list_price DOUBLE NOT NULL,
	sale_price DOUBLE NOT NULL DEFAULT 0.0,
	removal_status TINYINT NOT NULL DEFAULT 0,
	removal_date DATE,
	FOREIGN KEY (album_id) REFERENCES album(id),
	FOREIGN KEY (artist_id) REFERENCES artist(id),
	FOREIGN KEY (songwriter_id) REFERENCES songwriter(id),
	FOREIGN KEY (genre_id) REFERENCES genre(id),
	FOREIGN KEY (cover_art_id) REFERENCES cover_art(id),
	CHECK (play_length > 0.0),
	CHECK (album_track_number > 0),
	CHECK (cost_price < list_price),
	CHECK (sale_price < list_price)
	
);

-- Shop User (Managers and Clients)
CREATE TABLE shop_user
(
	id INT AUTO_INCREMENT PRIMARY KEY,
	title VARCHAR(10) NOT NULL,
	last_name VARCHAR(255) NOT NULL,
	first_name VARCHAR(255) NOT NULL,
	company_name VARCHAR(255),
	street_address VARCHAR(255) NOT NULL,
	street_address_2 VARCHAR(255),
	city VARCHAR(255) NOT NULL,
	province_id INT NOT NULL,
	country VARCHAR(255) NOT NULL,
	postal_code VARCHAR(10) NOT NULL,
	home_phone VARCHAR(15) NOT NULL,
	cell_phone VARCHAR(15),
	email VARCHAR(255) NOT NULL UNIQUE,
	hashed_pw BINARY(64) NOT NULL,
	salt VARCHAR(255) NOT NULL,
	last_genre_searched INT,
	is_manager TINYINT NOT NULL DEFAULT 0,
	FOREIGN KEY (province_id) REFERENCES province(id),
	FOREIGN KEY (last_genre_searched) REFERENCES genre(id)
);

-- Review
CREATE TABLE review
(
	id INT AUTO_INCREMENT PRIMARY KEY,
	track_id INT NOT NULL,
	user_id INT NOT NULL,
	date_entered DATE NOT NULL,
	rating INT(1) NOT NULL,
	review_content VARCHAR(2000) NOT NULL,
	approval_status TINYINT NOT NULL DEFAULT 0,
	FOREIGN KEY (track_id) REFERENCES track(id),
	FOREIGN KEY (user_id) REFERENCES shop_user(id),
	CHECK (rating >= 0 AND rating <= 5)
);

-- Invoice
CREATE TABLE invoice
(
	id INT AUTO_INCREMENT PRIMARY KEY,
	user_id INT NOT NULL,
	sale_date DATE NOT NULL,
	total_net_value DOUBLE NOT NULL,
	pst_tax DOUBLE NOT NULL DEFAULT 0.0,
	gst_tax DOUBLE NOT NULL DEFAULT 0.0,
	hst_tax DOUBLE NOT NULL DEFAULT 0.0,
	total_gross_value DOUBLE NOT NULL,
	removal_status TINYINT NOT NULL DEFAULT 0,
	removal_date DATE,
	FOREIGN KEY (user_id) REFERENCES shop_user(id)
);

-- Invoice Track
CREATE TABLE invoice_track
(
	invoice_id INT NOT NULL,
	track_id INT NOT NULL,
	final_price DOUBLE NOT NULL,
	removal_status TINYINT NOT NULL DEFAULT 0,
	removal_date DATE,
	PRIMARY KEY (invoice_id, track_id),
	FOREIGN KEY (invoice_id) REFERENCES invoice(id),
	FOREIGN KEY (track_id) REFERENCES track(id)
);

-- Invoice Album
CREATE TABLE invoice_album
(
	invoice_id INT NOT NULL,
	album_id INT NOT NULL,
	final_price DOUBLE NOT NULL,
	removal_status TINYINT NOT NULL DEFAULT 0,
	removal_date DATE,
	PRIMARY KEY (invoice_id, album_id),
	FOREIGN KEY (invoice_id) REFERENCES invoice(id),
	FOREIGN KEY (album_id) REFERENCES album(id)
);

-- Front Page Settings
CREATE TABLE front_page_settings
(
	id INT AUTO_INCREMENT PRIMARY KEY,
	survey_id INT NOT NULL,
	newsfeed_id INT NOT NULL,
	ad_a_id INT NOT NULL,
	FOREIGN KEY (survey_id) REFERENCES survey(id),
	FOREIGN KEY (newsfeed_id) REFERENCES newsfeed(id),
	FOREIGN KEY (ad_a_id) REFERENCES advertisement(id)
);