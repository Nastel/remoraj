
package com.nastel.bank;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Merchant {
	int mId = 0;
	int uId = 0;
	String mName = null;
	String accountNo = null;

	/**
	 * 
	 */
	public Merchant(ResultSet rs) {
		try {
			mId = rs.getInt("mId");
			uId = rs.getInt("user_id");
			mName = rs.getString("mName");
			accountNo = rs.getString("accountNo");
		} catch (SQLException se) {
			System.out.println(se);
		}
	}
}
