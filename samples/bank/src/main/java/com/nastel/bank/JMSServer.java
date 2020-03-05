package com.nastel.bank;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JMSServer extends HttpServlet implements Runnable {
	private static final long serialVersionUID = 1L;

	String queueName = null;
	QueueConnectionFactory queueConnectionFactory = null;
	QueueConnection queueConnection = null;
	QueueSession queueSession = null;
	QueueSender queueSender = null;
	QueueReceiver queueReceiver = null;
	Queue importQueue = null;
	Queue exportQueue = null;
	Thread srvThr = null;

	/**
	 * Constructor of the object.
	 */
	public JMSServer() {
		super();
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 *
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
		out.println("  <BODY>");
		out.print("    This is ");
		out.print(this.getClass());
		out.println(", using the GET method");
		out.println("  </BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException
	 *             if an error occure
	 */
	@Override
	public void init() throws ServletException {
		//
		//
		//
		System.out.println("Starting SampleBank JMS Server.");

		//
		// Create a WLS JNDI InitialContext object if none exists yet.
		//
		Context ctx = null;
		try {
			Hashtable<String, String> ht = new Hashtable<String, String>();
			ht.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
			ht.put(Context.PROVIDER_URL, "t3://localhost:7001");
			ctx = new InitialContext(ht);
		} catch (NamingException e) {
		} // If this is WAS, this will fail. Fall thru

		//
		// Get WAS JNDI, if we do not have one yet.
		//
		if (ctx == null) {
			try {
				Hashtable<String, String> ht = new Hashtable<String, String>();
				ht.put(Context.INITIAL_CONTEXT_FACTORY, "com.ibm.websphere.naming.WsnInitialContextFactory");
				ht.put(Context.PROVIDER_URL, "iiop://localhost:2809");
				ctx = new InitialContext(ht);
			} catch (NamingException e) {
			} // If this is WAS, this will fail. Fall thru
		}

		/*
		 * Look up connection factory and queue. If either does not exist, exit.
		 */
		try {
			queueConnectionFactory = (QueueConnectionFactory) ctx.lookup("BankConnectionFactory");
			importQueue = (Queue) ctx.lookup("bankQImport");
			exportQueue = (Queue) ctx.lookup("bankQExport");
		} catch (NamingException e) {
			System.out.println("JNDI API lookup failed: " + e.toString());
		}

		//
		// Start the JMS server thread.
		//
		srvThr = new Thread(this);
		srvThr.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@SuppressWarnings("rawtypes")
	public void run() {
		System.out.println("In JMSServer.run()");

		/*
		 * Create connection. Create session from connection; false means session is not transacted. Create receiver,
		 * then start message delivery. Receive all text messages from queue until a non-text message is received
		 * indicating end of message stream. Close connection.
		 */
		try {
			queueConnection = queueConnectionFactory.createQueueConnection();
			queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			queueSender = queueSession.createSender(exportQueue);
			queueReceiver = queueSession.createReceiver(importQueue);
			queueConnection.start();

			while (true) {
				// System.out.println( "JMSServer : Waiting for message..." );
				Message m = queueReceiver.receive(30000);
				if (m != null) {
					System.out.println("Got message : " + m.getJMSMessageID());
					for (Enumeration e = m.getPropertyNames(); e.hasMoreElements();) {
						String name = (String) e.nextElement();

						Object o = m.getObjectProperty(name);

						System.out.println("  " + name + " : " + o.toString());
					}

					if (m.getJMSMessageID().equals("sendmeimport")) {
						//
						// Send random xact
						//
						System.out.println("sending import data");
						Message message = queueSession.createMessage();
						message.setJMSMessageID("jms_export");
						message.setStringProperty("xact_boo", "zoo");
						queueSender.send(message);
					}
				}

			}
		} catch (JMSException e) {
			System.out.println("Exception occurred: " + e.toString());
		} finally {
			if (queueConnection != null) {
				try {
					queueConnection.close();
				} catch (JMSException e) {
				}
			}
		}
	}
}
