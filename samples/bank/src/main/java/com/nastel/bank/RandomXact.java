package com.nastel.bank;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.nastel.bank.data.Transaction;

/**
 * @author pjl
 *
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class RandomXact extends HttpServlet {
	private static final long serialVersionUID = -4568140171056039651L;

	Random randomizer = null;
	NumberFormat nf = null;

	int checkNo = 0;

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

		UITemplate.writeHeader(out);
		UITemplate.writeBanner(out, 4);

		UITemplate.bodyStart(out, "Random Transaction Generator");
		out.println("<form method=post action='RandomXact'>");
		out.println("<table class=info_body width=100% cellpadding=0 cellspacing=0 border=0>");
		out.println("  <tr class=info_body>");
		out.println("    <td class=label align=right width=40%>Number of Transactions:</td>");
		out.println("    <td class=value align=left><input type=text size=3 name=xactnum></td>");
		out.println("    <td class=value align=left width=40%><input type=submit name=submit value=Run></td>");
		out.println("  </tr>");
		out.println("</table>");
		out.println("</form>");
		UITemplate.bodyEnd(out);

		UITemplate.infoBox(out,
				"The Random Transaction Generator will insert a number of different banking transactions, including ATM, Checks and deposits.  The generator will add deposits when the balance is approaching 0.  The user controls how many transactions are added.");

		UITemplate.writeFooter(out);

		out.flush();
		out.close();
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
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
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();

		//
		// For ngTracker v2.5, we do not need these any longer (we use filters).
		//
		// session.setAttribute("TrackerBlock", this.getClass().getName());
		// session.setAttribute("TrackerHost", request.getRemoteAddr());
		// session.setAttribute("TrackerUser", request.getRemoteUser());

		int xactnum = 0;
		try {
			xactnum = Integer.parseInt(request.getParameter("xactnum"));
		} catch (NumberFormatException nfe) {
		}
		;

		session.setAttribute("TrackerData", "Created " + xactnum + " new transactions");

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		UITemplate.writeHeader(out);
		UITemplate.writeBanner(out, 4);

		out.println("<table class=info_body width=100% cellpadding=0 cellspacing=1 border=0>");

		int uId = DbUtils.getUserId(DbUtils.DEMO);

		float balance = DbUtils.getBalance(DbUtils.DEMO);

		out.println("  <tr><td>Running " + xactnum + " transactions:</td></tr>");

		long start = System.currentTimeMillis();
		for (int i = 0; i < xactnum; i++) {
			if (balance < 300) {
				balance = xactDeposit(out, uId, balance);
				continue;
			}

			int type = randomizer.nextInt(3);
			switch (type + 1) {
			case Transaction.XACT_ATM:
				balance = xactATM(out, uId, balance);
				break;
			case Transaction.XACT_CHECK:
				balance = xactCheck(out, uId, balance);
				break;
			case Transaction.XACT_DEPOSIT:
				balance = xactDeposit(out, uId, balance);
				break;
			default:
				out.println("  <tr><td>--bank error--</td></tr>");
				break;
			}
		}
		out.println("  <tr><td><img height=30 src=images/spacer.gif></td></tr>");
		out.println("  <tr><td>Elapsed time: " + (System.currentTimeMillis() - start) + "ms</td></tr>");
		out.println("</table>");

		UITemplate.writeFooter(out);

		out.flush();
		out.close();
	}

	public int generateRandomAmount() {
		return randomizer.nextInt(140);
	}

	public float xactATM(PrintWriter out, int uId, float balanceBegin) {
		int randAmount = generateRandomAmount();
		float balanceEnd = balanceBegin - randAmount;

		out.println("<tr><td>ATM : $" + randAmount + " ($" + balanceEnd + ")" + "</td></tr>");

		if (!DbUtils.addTransaction(uId, Transaction.XACT_ATM, 0, randAmount, balanceEnd)) {
			out.println("<tr><td><FONT color=red>Failure: ATM transaction.</FONT></td></tr>");
			return balanceBegin;
		}
		return balanceEnd;
	}

	public float xactCheck(PrintWriter out, int uId, float balanceBegin) {
		int randAmount = generateRandomAmount();
		float balanceEnd = balanceBegin - randAmount;

		out.println("<tr><td>Check : $" + randAmount + " ($" + balanceEnd + ")" + "</td></tr>");

		if (!DbUtils.addTransaction(uId, Transaction.XACT_CHECK, (++checkNo), randAmount, balanceEnd)) {
			out.println("<tr><td><FONT color=red>Failure: Check transaction.</FONT></td></tr>");
			return balanceBegin;
		}
		return balanceEnd;
	}

	public float xactDeposit(PrintWriter out, int uId, float balanceBegin) {
		int randAmount = generateRandomAmount();
		float balanceEnd = balanceBegin + randAmount;

		out.println("<tr><td>Deposit : $" + randAmount + " ($" + balanceEnd + ")" + "</tr></td>");

		if (!DbUtils.addTransaction(uId, Transaction.XACT_DEPOSIT, 0, randAmount, balanceEnd)) {
			out.println("<tr><td><FONT color=red>Failure: Deposit transaction.</FONT></td></tr>");
			return balanceBegin;
		}
		return balanceEnd;
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException
	 *             if an error occurs
	 */
	@Override
	public void init() throws ServletException {
		// Put your code here
		randomizer = new Random();

		nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
	}
}
