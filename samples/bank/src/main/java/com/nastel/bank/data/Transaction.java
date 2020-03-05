
package com.nastel.bank.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Transaction implements Serializable {
	public int tId = 0;
	public int uId = 0;
	public Timestamp tDate = null;
	public int tType = 0;
	public int checkNo = 0;
	public float amount = 0;
	public float balanceEnd = 0;

	public static final int XACT_ATM = 1;
	public static final int XACT_CHECK = 2;
	public static final int XACT_DEPOSIT = 3;
	public static final int XACT_BILLPAY = 4;

	/**
	 * 
	 */
	public Transaction(ResultSet rs) {
		try {
			tId = rs.getInt("tId");
			uId = rs.getInt("user_id");
			tDate = rs.getTimestamp("tDate");
			tType = rs.getInt("tType");
			checkNo = rs.getInt("checkNo");
			amount = rs.getFloat("amount");
			balanceEnd = rs.getFloat("balanceEnd");
		} catch (SQLException se) {
			System.out.println(se);
		}
	}

}
