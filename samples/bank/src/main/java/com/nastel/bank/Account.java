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
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 */
public class Account extends HttpServlet
{
	private static final long serialVersionUID = 8568527668394717232L;

	/**
	 * The doGet method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request
	 *          the request send by the client to the server
	 * @param response
	 *          the response send by the server to the client
	 * @throws ServletException
	 *           if an error occurred
	 * @throws IOException
	 *           if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		UITemplate.writeHeader(out);

		UITemplate.writeBanner(out, 1);

		UITemplate.bodyStart(out, "Account Overview");
		out.println("<table class=info_body width=100% cellpadding=0 cellspacing=0 border=0>");
		out.println("  <tr class=info_body>");
		out.println("    <td class=label style=\"border-bottom: solid 1px white\" align=right width=50%>Account: </td>");
		out.println("    <td class=value align=left> " + DbUtils.DEMO + "</td>");
		out.println("  </tr>");
		out.println("  <tr class=info_body>");
		out.println("    <td class=label align=right width=50%>Balance: </td>");
		out.println("    <td class=value align=left> $" + DbUtils.getBalance(DbUtils.DEMO) + "</td>");
		out.println("  </tr>");
		out.println("</table>");
		UITemplate.bodyEnd(out);

		UITemplate.infoBox(out, "This utility retrieves the users balance and displays it in the browser.");

		UITemplate.writeFooter(out);

		out.flush();
		out.close();
	}
}
