package com.nastel.bank.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueSession;
import javax.jms.TextMessage;

import com.nastel.bank.DbUtils;

public class DbTransactionsRepository extends ArrayList<Transaction> implements JmsMessageRepository {

	private static final long serialVersionUID = 1L;
	private static final int MAX_SIZE = Integer.getInteger("tworks.samples.bank.jms.max.msg.size", 4096);
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	protected int userId;
	protected Iterator<Transaction> iter;

	public DbTransactionsRepository(String userId, int numTrans) {

		super();
		this.userId = DbUtils.getUserId(userId);
		addAll(DbUtils.getTransactions(this.userId));
		iter = iterator();
	}

	public boolean hasNext() {
		return iter.hasNext();
	}

	public Message next(QueueSession session) throws JMSException {

		Transaction t = (Transaction) iter.next();
		TextMessage message = session.createTextMessage();

		message.setJMSMessageID("user_" + userId + "_num_" + indexOf(t));
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		message.setStringProperty("date", dateFormat.format(t.tDate));
		switch (t.tType) {
		case Transaction.XACT_ATM:
			message.setStringProperty("type", "ATM Withdrawal");
			break;
		case Transaction.XACT_BILLPAY:
			message.setStringProperty("type", "Online Bill Payment");
			break;
		case Transaction.XACT_CHECK:
			message.setStringProperty("type", "Check #" + t.checkNo);
			break;
		case Transaction.XACT_DEPOSIT:
			message.setStringProperty("type", "Deposit");
			break;
		}
		message.setDoubleProperty("amount", t.amount);
		message.setDoubleProperty("balance", t.balanceEnd);
		StringBuilder buffer = new StringBuilder(MAX_SIZE);
		for (int i = 0; i < MAX_SIZE; i++) {
			buffer.append(i);
			if (buffer.length() >= MAX_SIZE)
				break;
		}
		message.setText(buffer.toString());
		return message;
	}
}
