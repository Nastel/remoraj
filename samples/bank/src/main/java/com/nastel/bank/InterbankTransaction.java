package com.nastel.bank;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.SerializableEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.nastel.bank.data.Transaction;

public class InterbankTransaction extends HttpServlet {

	static void deserializeAndAddtoDB(InputStream entity) throws IOException {
		ObjectInputStream oos = new ObjectInputStream(entity);
		try {
			Transaction tx = (Transaction) oos.readObject();
			DbUtils.addTransaction(tx.uId, tx.tType, tx.checkNo, tx.amount, tx.balanceEnd);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	static Transaction getRandomTx() {
		int uId = DbUtils.getUserId(DbUtils.DEMO);
		ArrayList<Transaction> xacts = DbUtils.getTransactions(uId);
		int size = xacts.size();
		int transactionNo = new Random().nextInt(size);
		Transaction transaction = xacts.get(transactionNo);
		return transaction;

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		UITemplate.writeHeader(out);

		UITemplate.writeBanner(out, 3);

		UITemplate.bodyStart(out, "Interbank transaction");
		out.println("<form method=post action='Interbank'>");
		out.println("<table class=info_body width=100% cellspacing=0 cellpadding=0 border=0>");
		out.println("  <tr class=info_body>");
		out.println(
				"    <td class=label style=\"border-bottom: solid 1px white\" width=50% align=right>Remote Bank:</td>");
		out.println("    <td class=value align=center width=40%>");
		out.println("      <input size=30 name=\"remote\">");
		out.println("    </td>");
		out.println("  </tr>");
		out.println("  <tr class=info_body>");
		out.println(
				"    <td class=label style=\"border-bottom: solid 1px white\" width=50% align=right>Implementation:</td>");
		out.println("    <td class=value align=center width=40%>");
		out.println("      <select name=implementation style=\"width:95%\">");

		out.println("        <option name=\"Apache\" value=\"Apache\">Apache Http client</option>");
		out.println(
				"        <option name=\"HttpUrlConnection\" value=\"HttpUrlConnection\">HttpUrlConnection</option>");

		out.println("      </select>");
		out.println("    </td>");
		out.println("  </tr>");
		out.println("  <tr class=info_body>");
		out.println("    <td class=info_body align=center colspan=3></td>");
		out.println("    <td class=info_body align=center colspan=3>" + "<input type=submit name=submit value='Send'>"
				+ "<input type=submit name=submit value='Receive'></td>");
		out.println("  </tr>");
		out.println("</table>");
		out.println("</form>");
		UITemplate.bodyEnd(out);

		UITemplate.infoBox(out,
				"This function simulates a standard Bill Pay mechanism.  The code behind the functionality performs a JDBC call to add the Bill Pay transaction to the database.");

		UITemplate.writeFooter(out);

		out.flush();
		out.close();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();

		response.setContentType("text/html");

		String submit = request.getParameter("submit");
		String implementation = request.getParameter("implementation");

		String remoteServer = request.getParameter("remote");

		switch (submit.toUpperCase() + "_" + implementation.toUpperCase()) {
		case "SEND_APACHE":
			sendMoneyApache(remoteServer);
			break;
		case "RECEIVE_APACHE":
			receiveMoney(remoteServer);
			break;
		case "SEND_HTTPURLCONNECTION":
			sendMoneyHttpUrlConnection(remoteServer);
		case "RECEIVE_HTTPURLCONNECTION":
			receiveMoneyHttpUrlConnection(remoteServer);
		default:
			break;

		}
		// session.removeAttribute("TrackerBlock");
		response.sendRedirect("Interbank");
	}

	private void receiveMoney(String remoteServer) throws IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet request = new HttpGet(remoteServer);
		request.addHeader("User-Agent", "Bank");
		try (CloseableHttpResponse response = httpClient.execute(request)) {

			// Get HttpResponse Status
			System.out.println(response.getStatusLine().toString());

			HttpEntity entity = response.getEntity();
			Header headers = entity.getContentType();
			System.out.println(headers);

			if (entity != null) {
				// return it as a String
				deserializeAndAddtoDB(entity.getContent());

			}

		}
		httpClient.close();
	}

	private void sendMoneyApache(String remoteServer) throws IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();

		Transaction transaction = getRandomTx();

		SerializableEntity serializableEntity = new SerializableEntity(transaction, true);

		HttpPut request = new HttpPut(remoteServer);
		request.setEntity(serializableEntity);
		httpClient.execute(request);
		httpClient.close();
	}

	private void sendMoneyHttpUrlConnection(String remoteServer) throws IOException {
		HttpURLConnection httpClient = (HttpURLConnection) new URL(remoteServer).openConnection();
		httpClient.setRequestMethod("POST");

		Transaction transaction = getRandomTx();

		httpClient.setDoOutput(true);
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(httpClient.getOutputStream());
		objectOutputStream.writeObject(transaction);
		objectOutputStream.flush();

	}

	private void receiveMoneyHttpUrlConnection(String remoteServer) throws IOException {

		HttpURLConnection httpClient = (HttpURLConnection) new URL(remoteServer).openConnection();

		// optional default is GET
		httpClient.setRequestMethod("GET");
		httpClient.setRequestProperty("User-Agent", "Bank");
		httpClient.getResponseCode();

		deserializeAndAddtoDB(httpClient.getInputStream());

	}
}
