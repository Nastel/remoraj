package com.nastel.bank;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.nastel.bank.data.Transaction;

public class BillPay extends HttpServlet
{
	private static final long serialVersionUID = -2357872110057345367L;

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		UITemplate.writeHeader(out);

		int uId = DbUtils.getUserId(DbUtils.DEMO);

		UITemplate.writeBanner(out,3);

		UITemplate.bodyStart(out, "Bill Pay");
		out.println("<form method=post action='BillPay'>");
		out.println("<table class=info_body width=100% cellspacing=0 cellpadding=0 border=0>");
		out.println("  <tr class=info_body>");
		out.println("    <td class=label style=\"border-bottom: solid 1px white\" width=50% align=right>Merchant:</td>");
		out.println("    <td class=value align=center width=40%>");
		out.println("      <select name=merchant style=\"width:95%\">");
		ArrayList<Merchant> mlist = DbUtils.getMerchants(uId);
		for(int i=0; i<mlist.size(); i++)
		{
			Merchant m = (Merchant)mlist.get(i);
			out.println("        <option name=merchant value=" + m.mId + ">" + m.mName + "</option>");
		}
		out.println("      </select>");
		out.println("    </td>");
		out.println("    <td width=10% class=value align=left><a href='MerchantAdd'>Add...</a></td>");
		out.println("  </tr>");
		out.println("  <tr class=info_body>");
		out.println("    <td class=label width=50% align=right>Amount:</td>");
		out.println("    <td class=value align=left colspan=2><input name=amount size=8></td>");
		out.println("  </tr>");
		out.println("  <tr class=info_body>");
		out.println("    <td class=info_body align=center colspan=3><input type=submit name=submit value='Pay'></td>");
		out.println("  </tr>");
		out.println("</table>");
		out.println("</form>");
		UITemplate.bodyEnd(out);

		UITemplate.infoBox(out, "This function simulates a standard Bill Pay mechanism.  The code behind the functionality performs a JDBC call to add the Bill Pay transaction to the database.");

		UITemplate.writeFooter(out);

		out.flush();
		out.close();
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		HttpSession session = request.getSession();
		//
		// For ngTracker v2.5, we do not need these any longer (we use filters).
		//
		//session.setAttribute("TrackerBlock", this.getClass().getName());
		//session.setAttribute("TrackerHost",  request.getRemoteAddr());
		//session.setAttribute("TrackerUser",  request.getRemoteUser());

		response.setContentType("text/html");

		int   mId    = Integer.parseInt(request.getParameter("merchant"));
		float amount = Float.parseFloat(request.getParameter("amount"));

		//
		// For debug purpose, we need to specify the merchant name and amount.
		//
		Merchant m = DbUtils.getMerchant(mId);
		session.setAttribute("TrackerData", "-- Paid $" + amount + " to '" + m.mName + "' --");

		int uId          = DbUtils.getUserId(DbUtils.DEMO);
		float balance    = DbUtils.getBalance(DbUtils.DEMO);
		float balanceEnd = balance - amount;

		if (!DbUtils.addTransaction(uId, Transaction.XACT_BILLPAY, 0, amount, balanceEnd))
			System.out.println("--failed--");

		//session.removeAttribute("TrackerBlock");
		response.sendRedirect("Account");
	}
}
