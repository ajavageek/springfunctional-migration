CREATE TABLE IF NOT EXISTS PERSON
(
    ID         BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    FIRST_NAME VARCHAR(50) NOT NULL,
    LAST_NAME  VARCHAR(50) NOT NULL,
    BIRTHDATE  DATE
);