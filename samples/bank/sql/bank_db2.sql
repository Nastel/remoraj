CREATE TABLE BankUsers (
  user_id   INTEGER NOT NULL,
  email     VARCHAR(64),
  fname     VARCHAR(64),
  lname     VARCHAR(64),
  balance   DOUBLE,
  PRIMARY KEY(user_id)
);

INSERT INTO BankUsers (user_id, email, fname, lname, balance) VALUES (1, 'demo', 'Demo', 'User', 300.00);


CREATE TABLE Merchants (
  mId       INTEGER NOT NULL,
  user_id   INTEGER NOT NULL,
  mName     VARCHAR(255),
  accountNo VARCHAR(64),
  PRIMARY KEY(mId),
  UNIQUE(mName),
  UNIQUE(accountNo)
);

CREATE TABLE Transactions (
  tId           INTEGER NOT NULL,
  user_id       INTEGER NOT NULL,
  tDate         TIMESTAMP,
  tType         INTEGER,
  checkNo       INTEGER,
  amount        DOUBLE,
  balanceEnd    DOUBLE,
  PRIMARY KEY(tId)
);
