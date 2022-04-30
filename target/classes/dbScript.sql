CREATE TABLE cus
(
    nic     VARCHAR(10) PRIMARY KEY,
    name    VARCHAR(100) NOT NULL,
    contact VARCHAR(15)  NOT NULL

);

CREATE TABLE item
(
    isbn    VARCHAR(25) PRIMARY KEY,
    name    VARCHAR(100) NOT NULL,
    company  VARCHAR(100) NOT NULL,
    preview LONGBLOB

);


CREATE TABLE odr
(
    id   INT PRIMARY KEY AUTO_INCREMENT,
    nic  VARCHAR(100) NOT NULL,
    isbn VARCHAR(25)  NOT NULL,
    date DATE         NOT NULL,
    CONSTRAINT fk_cus FOREIGN KEY (nic) REFERENCES cus (nic),
    CONSTRAINT fk_item FOREIGN KEY (isbn) REFERENCES item (isbn)

);