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

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.jms.*;
import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import com.nastel.bank.data.DbTransactionsRepository;
import com.nastel.bank.data.FileRepository;
import com.nastel.bank.data.JmsMessageRepository;

public class ImportExport extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(ImportExport.class);

	public static final String MSG_FROM_APP = "dbTransactions";
	public static final String MSG_FROM_FILE = "file";

	private static final long waitInterval = 20000; // milliseconds

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setAttribute("servletName", "servletToJsp");

		try {
			getServletConfig().getServletContext().getRequestDispatcher("/importExport.html").forward(request,
					response);
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HashMap<String, String> params = initParameters(request);
		String msgSource = params.get("msgSource");
		int numTrans = 0;
		String fileData = null;

		boolean useBackend = "on".equals(params.get("backend"));

		try {
			numTrans = Integer.parseInt(params.get("xactnum"));
		} catch (NumberFormatException e) {
			logger.error("!! invalid number of messages", e);
			response.sendRedirect("ImportExport");
			return;
		}

		if (msgSource == null) {
			logger.error("!! msgSource is null");
			response.sendRedirect("ImportExport");
			return;
		}

		if (msgSource.equals("file")) {
			fileData = params.get("file");
		}

		log(msgSource, numTrans, fileData, useBackend);

		performJmsOps(msgSource, numTrans, fileData, useBackend);

		response.sendRedirect("Account");
	}

	private void performJmsOps(String msgSource, int numTrans, String fileData, boolean useBackend)
			throws ServletException {
		// lookup JNDI resources
		QueueConnectionFactory queueConnFactory = null;
		QueueConnection queueConn = null;
		QueueSession queueSession = null;
		Queue requestQueue = null;
		Queue replyQueue = null;

		logger.info("looking up JNDI resources...");

		// TODO
		/*
		 * If JNDI lookup failed to find BankConnectionFactory, RequestQueue or ReplyQueue, display an appropriate error
		 * message. Right now, only a non-informative NullPointerException is shown.
		 */

		try {
			queueConnFactory = (QueueConnectionFactory) lookup("BankConnectionFactory");
		} catch (Exception e) {
			String error = "Failed to lookup 'BankConnectionFactory'";
			logger.error(error, e);
			throw new ServletException(error, e);
		}

		try {
			requestQueue = (Queue) lookup("BankRequestQueue");
		} catch (Exception e) {
			String error = "Failed to lookup 'BankRequestQueue'";
			logger.error(error, e);
			throw new ServletException(error);
		}

		try {
			replyQueue = (Queue) lookup("BankReplyQueue");
		} catch (Exception e) {
			String error = "Failed to lookup 'BankReplyQueue'";
			logger.error(error, e);

			throw new ServletException(error);
		}

		// --- Initialize JMS resources ---
		try {
			queueConn = queueConnFactory.createQueueConnection();
			queueConn.start();

			// set ACKNOWLEDGE_MODE to -1 since it is ignored when the first
			// parameter - 'transacted' - is 'true'.
			queueSession = queueConn.createQueueSession(true, -1);

			logger.info("\tsessions transacted?: " + queueSession.getTransacted());

			JmsMessageRepository msgRepository = null;

			// --- do JMS work ---
			if (msgSource.equals(MSG_FROM_APP)) {
				msgRepository = new DbTransactionsRepository(DbUtils.DEMO, numTrans);
				doJmsWork(queueSession, requestQueue, replyQueue, msgRepository, numTrans, useBackend);
			} else if (msgSource.equals(MSG_FROM_FILE)) {
				msgRepository = new FileRepository(fileData, numTrans);
				doJmsWork(queueSession, requestQueue, replyQueue, msgRepository, numTrans, useBackend);
			} else {
				logger.error("!! Incorrect msgSource=" + msgSource);
			}
		} catch (Exception e) {
			logger.error("Exception occurred: " + e, e);
			e.printStackTrace();
		} finally {
			if (queueSession != null) {
				try {
					queueSession.close();
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
			if (queueConn != null) {
				try {
					queueConn.close();
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
			logger.info("< = = = =  END JMS Operations  = = = = >");
		}
	}

	private void log(String msgSource, int numTrans, String fileData, boolean useBackend) {
		logger.info("< = = = =  Performing JMS operations = = = = >");
		logger.info("settings");
		logger.info("\t" + "useBackend?: " + useBackend);
		logger.info("\t" + "generate messages from: "
				+ (msgSource.equals(MSG_FROM_APP) ? "Bank app's DB records" : "file"));
		logger.info("\t" + "# messages to import/export: " + numTrans);
		logger.trace("\t" + "msg=" + fileData);
		logger.info("");
	}

	@SuppressWarnings("unchecked")
	protected HashMap<String, String> initParameters(HttpServletRequest request) throws IOException {
		HashMap<String, String> params = new HashMap<>();

		// Create a factory for disk-based file items
		FileItemFactory factory = new DiskFileItemFactory();

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		// Parse the request
		List<FileItem> items = null;
		try {
			items = upload.parseRequest(request);
		} catch (FileUploadException e) {
			e.printStackTrace();
		}

		// Process the uploaded items
		for (Iterator<FileItem> iter = items.iterator(); iter.hasNext();) {
			FileItem item = iter.next();

			if (item.isFormField()) {
				if (item.isFormField()) {
					String name = item.getFieldName();
					String value = item.getString();
					params.put(name, value);
				}
			} else {
				InputStream uploadedStream = item.getInputStream();
				byte[] dataBytes = item.get();
				params.put("file", new String(dataBytes));
				uploadedStream.close();
			}
		}

		return params;
	}

	protected void doJmsWork(QueueSession session, Queue requestQueue, Queue replyQueue,
			JmsMessageRepository msgRepository, int numTrans, boolean useBackend) throws JMSException {
		logger.info("\t# messages available: " + msgRepository.size());

		int i = 0;
		boolean useListener = false; // TODO: Added control/property to set this
		Message rspMsg = null;
		Message message = null;

		QueueSender requestSender = session.createSender(requestQueue);
		MsgListener listener = new MsgListener(Thread.currentThread(), numTrans);

		while (i++ < numTrans && msgRepository.hasNext()) {
			try {
				// -------------------------------
				// PREPARE message
				// -------------------------------
				if (rspMsg == null) {
					message = msgRepository.next(session);
				} else {
					message = rspMsg;
				}

				message.setJMSReplyTo(replyQueue);

				logger.info("");
				logger.info("< < < <  begin transaction  > > > >");

				// -------------------------------
				// SEND message
				// -------------------------------

				String correlId = UUID.randomUUID().toString();
				logger.trace("   corrId=" + correlId);
				message.setJMSCorrelationID(correlId);
				logger.info("\t<- sending to " + requestSender.getQueue().getQueueName());
				requestSender.send(message);
				logger.trace("\tsendMsg=" + message);
				if (session.getTransacted()) {
					session.commit();
				}

				String selector = "JMSCorrelationID = '" + correlId + "'";

				// -------------------------------
				// use back-end ?
				// -------------------------------

				if (!useBackend) {
					doReceiveAndSend(session, requestQueue, replyQueue, selector);
				}

				// -------------------------------
				// RECEIVE message
				// -------------------------------

				QueueReceiver replyReceiver = session.createReceiver(replyQueue, selector);

				logger.info("\t() waiting on " + replyReceiver.getQueue().getQueueName());
				logger.info("\t      for " + selector);

				if (useListener) {
					replyReceiver.setMessageListener(listener);
					try {
						// wait for listener to receive message
						Thread.sleep(waitInterval);

						// if wait time expires, message not received
						logger.info("!! Reply has NOT been received");
					} catch (InterruptedException e) {
						// sleep is interrupted when message received
					}
				} else {
					rspMsg = replyReceiver.receive(waitInterval);
					if (rspMsg == null) {
						logger.info("!! Reply has NOT been received");
					} else {
						listener.onMessage(rspMsg);
					}
				}

				if (session.getTransacted()) {
					session.commit();
				}

				replyReceiver.close();

				logger.info("# # # #  end transaction  # # # #");
			} catch (JMSException e) {
				logger.error("Failed to create/send/receive JMS message", e);
			}
		}
		requestSender.close();
	}

	@SuppressWarnings("unchecked")
	protected void doReceiveAndSend(QueueSession session, Queue requestQueue, Queue replyQueue, String selector)
			throws JMSException {
		QueueReceiver receiver = session.createReceiver(requestQueue, selector);
		// QueueReceiver receiver = session.createReceiver(requestQueue);
		logger.info("\t () waiting on " + receiver.getQueue().getQueueName());
		logger.info("\t       for " + selector);

		Message requestMsg = receiver.receive(waitInterval);
		receiver.close();

		if (session.getTransacted()) {
			session.commit();
		}

		if (requestMsg == null) {
			logger.info("!! Reply has NOT been received");
			return;
		}

		logger.info("\t-> received reply");
		logger.trace("\t" + "replMsg=" + requestMsg);

		Message replyMsg = session.createMessage();
		replyMsg.setJMSReplyTo(requestMsg.getJMSReplyTo());
		replyMsg.setJMSCorrelationID(requestMsg.getJMSCorrelationID());
		for (Enumeration<String> e = requestMsg.getPropertyNames(); e.hasMoreElements();) {
			String name = e.nextElement();
			Object o = requestMsg.getObjectProperty(name);
			try {
				replyMsg.setStringProperty(name, o.toString());
			} catch (JMSException ex) {
				// ignore exception if a property can't be set
			}
		}

		QueueSender replySender = session.createSender(replyQueue);
		logger.info("\t<- sending to " + replySender.getQueue().getQueueName());
		replySender.send(replyMsg);
		logger.trace("\tsendMsg=" + replyMsg);
		if (session.getTransacted()) {
			session.commit();
		}
		replySender.close();
	}

	private Object lookup(String name) throws NamingException {
		Context context = null;
		try {
			context = new InitialContext();
		} catch (NamingException e) {
			logger.error("Could not create JNDI API " + "context: " + e.toString());
		}

		if (context == null) {
			context = new InitialContext();
		}

		Object o;
		try {
			o = context.lookup(name);
			return o;
		} catch (NamingException e) {
		}

		try {
			o = context.lookup("java:/" + name);
			return o;
		} catch (NamingException e) {
		}

		o = context.lookup("java:comp/env/" + name);

		return o;
	}
}
