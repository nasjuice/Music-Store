DROP DATABASE IF EXISTS g4w17;
CREATE DATABASE g4w17;

GRANT ALL ON g4w17.* TO g4w17@"%" IDENTIFIED BY "dG6D3EEk";
GRANT ALL ON g4w17.* TO g4w17@"localhost" IDENTIFIED BY "dG6D3EEk";

USE g4w17;

SOURCE C:/pandamedia/createtables.sql;
SOURCE C:/pandamedia/insertdata.sql;