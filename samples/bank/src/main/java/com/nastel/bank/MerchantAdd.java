package com.nastel.bank;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MerchantAdd extends HttpServlet 
{
	private static final long serialVersionUID = 5950524882154047041L;

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

		UITemplate.bodyStart(out, "Add Merchant");
		out.println("<form method=post action='MerchantAdd'>");
		out.println("<table class=info_body width=100% cellspacing=0 cellpadding=1 border=0>");
		out.println("  <tr class=info_body>");
		out.println("    <td class=label style=\"border-bottom: solid 1px white\" align=right width=40%>Merchant:</td>");
		out.println("    <td class=value align=left><input name=mName size=30></td>");
		out.println("  </tr>");
		out.println("  <tr class=info_body>");
		out.println("    <td class=label align=right width=40%>Account #:</td>");
		out.println("    <td class=value align=left><input name=acctno size=30></td>");
		out.println("  </tr>");
		out.println("  <tr class=info_body>");
		out.println("    <td class=info_body align=center colspan=2><input type=submit name=submit value='Create'></td>");
		out.println("  </tr>");
		out.println("</table>");
		out.println("</form>");
		UITemplate.bodyEnd(out);

		UITemplate.infoBox(out, "The Merchant Add function allows the banking user to create a new payable merchant within the system.  The underlying code will perform a JDBC insert.");

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
		int uId = DbUtils.getUserId(DbUtils.DEMO);

		String mName  = request.getParameter("mName");
		String acctNo = request.getParameter("acctno");

		if (mName != null && acctNo != null && DbUtils.addMerchant(uId, mName, acctNo))
			response.sendRedirect("BillPay");
	}
}
