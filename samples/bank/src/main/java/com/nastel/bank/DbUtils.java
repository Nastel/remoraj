/*
 * Copyright (c) 2005 NasTel Technologies, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of NasTel
 * Technologies, Inc. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with NasTel
 * Technologies.
 *
 * NASTEL MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. NASTEL SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
 *
 */
package com.nastel.bank;

import java.sql.*;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.nastel.bank.data.Transaction;

/**
 *
 */
public class DbUtils {
	public static final String DEMO = "demo";

	private static final String DATA_SOURCE_NAME = "bank_db";

	private static DataSource ds;

	/** @return the data source. */
	public static Connection getConnection() throws NamingException, SQLException {
		if (ds == null) {
			ds = findDataSource();
		}
		return ds.getConnection();
	}

	private static DataSource findDataSource() throws NamingException {
		if (context == null) {
			context = new InitialContext();
		}

		Object o = lookup(context, DATA_SOURCE_NAME);
		if (o instanceof DataSource) {
			return (DataSource) o;
		}

		o = lookup(context, "java:/" + DATA_SOURCE_NAME);
		if (o instanceof DataSource) {
			return (DataSource) o;
		}

		o = lookup(context, "java:comp/env/" + DATA_SOURCE_NAME);
		if (o instanceof DataSource) {
			return (DataSource) o;
		}

		return null;
	}

	private static Context context;

	private static Object lookup(Context context, String name) {
		try {
			return context.lookup(name);
		} catch (NamingException ex) {
			return null;
		}
	}

	private static void close(Connection connection) {
		try {
			connection.close();
		} catch (Exception ex) {
		}
	}

	private static void close(Statement statement) {
		try {
			statement.close();
		} catch (Exception ex) {
		}
	}

	public static ResultSet executeQuery(String query) throws Exception {
		Connection connection = getConnection();
		Statement statement = connection.createStatement();
		return statement.executeQuery(query);
	}

	public static void closeConnection(ResultSet result) {
		try {
			Statement statement = result.getStatement();
			Connection connection = statement.getConnection();
			close(statement);
			close(connection);
		} catch (Exception ex) {
		}
	}

	public static int executeUpdate(String update) throws Exception {
		Connection connection = null;
		Statement statement = null;
		try {
			connection = getConnection();
			statement = connection.createStatement();
			return statement.executeUpdate(update);
		} finally {
			close(statement);
			close(connection);
		}
	}

	/**
	 * @return Returns the integrity of the database.
	 */
	public static boolean checkDB() {
		ResultSet rs = null;
		try {
			rs = executeQuery("SELECT COUNT(*) FROM BankUsers");
			return rs != null && rs.next();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeConnection(rs);
		}

		return false;
	}

	public static int getUserId(String email) {
		if (email == null) {
			email = DEMO;
		}

		ResultSet rs = null;
		try {
			rs = executeQuery("SELECT user_id FROM BankUsers WHERE email='" + email + "'");
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} finally {
			closeConnection(rs);
		}

		return -1;
	}

	public static float getBalance(String email) {
		if (email == null) {
			email = DEMO;
		}

		ResultSet rs = null;
		try {
			rs = executeQuery("SELECT balance FROM BankUsers WHERE email='" + email + "'");
			if (rs.next()) {
				return rs.getFloat(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} finally {
			closeConnection(rs);
		}

		return -1;
	}

	public static ArrayList<Transaction> getTransactions(int uId) {
		ArrayList<Transaction> xacts = new ArrayList<>();
		ResultSet rs = null;
		try {
			rs = executeQuery("SELECT * FROM Transactions WHERE user_id=" + uId);
			while (rs.next()) {
				xacts.add(new Transaction(rs));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} finally {
			closeConnection(rs);
		}

		return xacts;
	}

	public static boolean addTransaction(int uId, int tType, int checkNo, float amount, float bEnd) {
		Connection connection = null;
		try {
			connection = getConnection();

			int id = 0;

			Statement s = connection.createStatement();
			try {
				ResultSet result = s.executeQuery("SELECT MAX(tId) FROM Transactions");
				if (result.next()) {
					id = result.getInt(1);
				}
				id += 10;
			} finally {
				close(s);
			}

			PreparedStatement statement = null;
			try {
				statement = connection.prepareStatement(
						"INSERT INTO Transactions " + "(tId, user_id, tDate, tType, checkNo, amount, balanceEnd) "
								+ "VALUES (?, ?, ?, ?, ?, ?, ?)");
				statement.setInt(1, id);
				statement.setInt(2, uId);
				statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
				statement.setInt(4, tType);
				statement.setInt(5, checkNo);
				statement.setFloat(6, amount);
				statement.setFloat(7, bEnd);
				statement.executeUpdate();
			} finally {
				close(statement);
			}

			// Now update the balance for this user.
			try {
				statement = connection.prepareStatement("update BankUsers set balance = ? where user_id = ?");
				statement.setFloat(1, bEnd);
				statement.setInt(2, uId);
				statement.executeUpdate();
				return true;
			} finally {
				close(statement);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		} finally {
			close(connection);
		}
	}

	public static Merchant getMerchant(int mId) {
		ResultSet rs = null;
		try {
			rs = executeQuery("SELECT * FROM Merchants WHERE mId=" + mId);
			if (rs.next()) {
				return new Merchant(rs);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} finally {
			closeConnection(rs);
		}

		return null;
	}

	public static ArrayList<Merchant> getMerchants(int uId) {
		ArrayList<Merchant> merchants = new ArrayList<>();
		ResultSet rs = null;
		try {
			rs = executeQuery("SELECT * FROM Merchants WHERE user_id=" + uId);
			while (rs.next()) {
				merchants.add(new Merchant(rs));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} finally {
			closeConnection(rs);
		}

		return merchants;
	}

	public static boolean addMerchant(int uId, String mName, String acctNo) {
		Connection connection = null;
		Statement s = null;
		PreparedStatement statement = null;
		try {
			connection = getConnection();
			s = connection.createStatement();

			int id = 0;
			ResultSet result = s.executeQuery("SELECT MAX(mId) FROM Merchants");
			if (result.next()) {
				id = result.getInt(1);
			}
			id += 10;

			statement = connection.prepareStatement(
					"INSERT INTO Merchants " + "(mId, user_id, mName, accountNo) " + "VALUES (?, ?, ?, ?)");
			statement.setInt(1, id);
			statement.setInt(2, uId);
			statement.setString(3, mName);
			statement.setString(4, acctNo);
			statement.executeUpdate();
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		} finally {
			close(s);
			close(statement);
			close(connection);
		}
	}
}
