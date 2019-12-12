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

import java.io.PrintWriter;

/**
 *
 */
public class UITemplate
{
	static public void writeHeader( PrintWriter out)
	{
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<html>");
		out.println("<head>");
		out.println("  <title>Nastel Sample Bank (J2EE)</title>");
		out.println("  <link rel='shortcut icon' type='image/x-icon' href='images/nastel.ico'>");
		out.println("  <link rel='stylesheet' type='text/css' href='style.css'>");
		out.println("</head>");
		out.println("<body leftmargin=20 topmargin=20 marginwidth=20 marginheight=20>");
	}

	static public void writeBanner( PrintWriter out, int item)
	{
		out.println("<table width=80% align=center cellpadding=0 cellspacing=0 border=0>");
		out.println("  <thead>");
		out.println("    <tr><td colspan=99 align=left><img src='images/logo.gif'></td></tr>");
		out.println("    <tr><td colspan=99 align=left><img height=20 src='images/spacer.gif'></td></tr>");
		out.println("    <tr class=menu>");
		out.println("      <td class='menu menu_cell'><a href='Account' class=menu>Account Overview</a></td>");
		out.println("      <td class='menu menu_cell'><a href='ViewHistory' class=menu>View History</a></td>");
		out.println("      <td class='menu menu_cell'><a href='BillPay' class=menu>Pay Bills Online</a></td>");
		out.println("      <td class='menu menu_cell'><a href='RandomXact' class=menu>Sample Transactions</a></td>");
		out.println("      <td class='menu menu_cell'><a href='ImportExport' class=menu>Import/Export</a></td>");
        out.println("      <td class='menu menu_cell'><a href='Interbank' class=menu>Interbank</a></td>");
		out.println("      <td class='menu menu_cell'><a href='benchmark' class=menu>Benchmark</a></td>");
		out.println("      <td class='menu'><a href='logout.jsp' class=menu>Logout</a></td>");
		out.println("    </tr>");
		out.println("  </thead>");
		out.println("  <tfoot>");
		out.println("    <tr class=title>");
		out.println("      <td colspan=6 align=center><a href='http://www.nastel.com' class=title>Nastel</a>'s Sample Bank application showcases the TransactionWorks monitoring system.</td>");
		out.println("      <td align=right>v" + Constants.BANK_VERSION + "</td>");
		out.println("    </tr>");
		out.println("  </tfoot>");
		out.println("  <tr><th colspan=99>");
		out.println("    <table width=100% cellpadding=0 cellspacing=20>");
		out.println("      <tr>");
	}

	static public void bodyStart( PrintWriter out, String title)
	{
		out.println("        <th width=50% valign=center>");
		out.println("          <table class=thin_border width=100% cellpadding=1 cellspacing=0>");
		out.println("            <tr class=title><td align=center>" + title + "</td></tr>");
		out.println("            <tr>");
		out.println("              <th>");
	}

	static public void bodyEnd( PrintWriter out)
	{
		out.println("              </th>");
		out.println("            </tr>");
		out.println("          </table>");
		out.println("        </th>");
	}

	static public void infoBox( PrintWriter out, String info)
	{
		out.println("        <th width=50% valign=center>");
		out.println("          <table class=thin_border width=100% cellpadding=0 cellspacing=0>");
		out.println("            <tr class=title><td align=center>Information</td></tr>");
		out.println("            <tr>");
		out.println("              <th>");
		out.println("                <table class=info_body width=100% cellpadding=0 cellspacing=0 border=0>");
		out.println("                  <tr><td class=wrap align=center>" + info + "</td></tr>");
		out.println("                </table>");
		out.println("              </th>");
		out.println("            </tr>");
		out.println("          </table>");
		out.println("        </th>");
	}


	static public void writeFooter( PrintWriter out)
	{
		out.println("      </tr>");
		out.println("    </table>");
		out.println("  </th></tr>");
		out.println("</table>");
		out.println("</body>");
		out.println("</html>");
	}
}
