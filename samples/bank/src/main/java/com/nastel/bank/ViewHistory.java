package com.nastel.bank;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nastel.bank.data.Transaction;

public class ViewHistory extends HttpServlet
{
	private static final long serialVersionUID = 1907031950444242951L;

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
		UITemplate.writeBanner(out, 2);
		int uId = DbUtils.getUserId(DbUtils.DEMO);

		UITemplate.bodyStart(out, "View History");
		out.println("   <div class=scroll>");
		out.println("   <table class=grid_body width=100% cellpadding=0 cellspacing=0 border=1>");
		out.println("     <tr class=title>");
		out.println("       <td align=center>Date</td>");
		out.println("       <td align=center>Type</td>");
		out.println("       <td align=center>Amount</td>");
		out.println("       <td align=center>Balance</td>");
		out.println("     </tr>");

		DecimalFormat df = new DecimalFormat("######0.00");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		ArrayList<Transaction> xacts = DbUtils.getTransactions(uId);
		for(int i=0; i<xacts.size(); i++)
		{
			Transaction t = (Transaction)xacts.get(i);
			if (i % 2 == 0)
				out.println(" <tr class=grid_body bgcolor=#CCCCCC>");
			else
				out.println(" <tr class=grid_body bgcolor=white>");
			out.println("     <td align=left>"  + sdf.format(t.tDate) + "</td>");
			out.println("     <td align=left>");
			switch(t.tType)
			{
				case Transaction.XACT_ATM:
					out.println("ATM Withdrawal");
					break;
				case Transaction.XACT_BILLPAY:
					out.println("Online Bill Payment");
					break;
				case Transaction.XACT_CHECK:
					out.println("Check #" + t.checkNo);
					break;
				case Transaction.XACT_DEPOSIT:
					out.println("Deposit");
					break;
			}
			out.println("     </td>");
			out.println("     <td align=right>$" + df.format(t.amount)     + "</td>");
			out.println("     <td align=right>$" + df.format(t.balanceEnd) + "</td>");
			out.println("   </tr>");
		}
		out.println("   </table>");
		out.println("   </div>");
		UITemplate.bodyEnd(out);

		UITemplate.infoBox(out, "The View History function allows users to see every banking transaction.  Depending on the number of transactions, this operation may take a long time, which should be reflected in the underlying code.");

		UITemplate.writeFooter(out);

		out.flush();
		out.close();
	}
}
