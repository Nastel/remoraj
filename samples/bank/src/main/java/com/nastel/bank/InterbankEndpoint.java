package com.nastel.bank;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nastel.bank.data.Transaction;

public class InterbankEndpoint extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Transaction transaction = InterbankTransaction.getRandomTx();
		ObjectOutputStream oos = new ObjectOutputStream(resp.getOutputStream());
		oos.writeObject(transaction);
		oos.flush();

	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ObjectInputStream oos = new ObjectInputStream(req.getInputStream());
		try {
			Transaction tx = (Transaction) oos.readObject();
			DbUtils.addTransaction(tx.uId, tx.tType, tx.checkNo, tx.amount, tx.balanceEnd);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}
}
