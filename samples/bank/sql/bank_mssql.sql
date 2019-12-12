CREATE TABLE BankUsers (
  user_id   INT NOT NULL,
  email     VARCHAR(64),
  fname     VARCHAR(64),
  lname     VARCHAR(64),
  balance   FLOAT,
  PRIMARY KEY(user_id)
);

INSERT INTO BankUsers (user_id, email, fname, lname, balance) VALUES (1, 'demo', 'Demo', 'User', 300.00);

CREATE TABLE Merchants (
  mId       INT NOT NULL,
  user_id   INT NOT NULL,
  mName     VARCHAR(255),
  accountNo VARCHAR(64),
  PRIMARY KEY(mId),
  UNIQUE(mName),
  UNIQUE(accountNo)
);

CREATE TABLE Transactions (
  tId           INT NOT NULL,
  user_id       INT NOT NULL,
  tDate         DATETIME,
  tType         INT,
  checkNo       INT,
  amount        FLOAT,
  balanceEnd    FLOAT,
  PRIMARY KEY(tId)
);
