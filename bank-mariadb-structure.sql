-- MariaDB structure script for bank service application
SET CHARACTER SET utf8;
DROP DATABASE IF EXISTS bank;
CREATE DATABASE bank CHARACTER SET utf8;
USE bank;

CREATE TABLE Account (
	identity BIGINT AUTO_INCREMENT,
	name VARCHAR(63) NULL,
	balance BIGINT NOT NULL,
	interestRate DOUBLE NOT NULL,
	lastModification BIGINT NOT NULL,
	PRIMARY KEY (identity)
);

COMMIT;