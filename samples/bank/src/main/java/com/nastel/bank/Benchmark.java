package com.nastel.bank;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Benchmark extends HttpServlet
{
	private static final long serialVersionUID = 5663081501803867388L;

	class MonitoredThread extends Thread 
	{
		int call_count, count;
		long monitored_time;
		boolean monitored = false;

		public MonitoredThread(String name, int ct, int call_ct, boolean flag) 
		{
			super(name);
			monitored = flag;
			call_count = call_ct;
			count = ct;
		}

		public long getElapsed() { return monitored_time; }
		public void run()
		{
			boolean nanoTimeSupported = false;

			try
			{
				System.class.getMethod("nanoTime", (Class[])null);
				nanoTimeSupported = true;
			}
			catch (NoSuchMethodException ex) { }

			long start = nanoTimeSupported ? System.nanoTime() : System.currentTimeMillis();

			for (int i=0; i < call_count; i++) {
				if (monitored) monitored_benchmark(count);
				else unmonitored_benchmark(count);
			}

			monitored_time = (nanoTimeSupported ? System.nanoTime() : System.currentTimeMillis()) - start;
			if (nanoTimeSupported)
				monitored_time = monitored_time / 1000000;			
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		UITemplate.writeHeader(out);
		UITemplate.writeBanner(out, 6);

		UITemplate.bodyStart(out, "Java Method Monitoring Benchmark");
		out.println("<form method=post action='benchmark'>");
		out.println("  <table class=info_body width=100% cellpadding=0 cellspacing=0 border=0>");
		out.println("    <tr>");
		out.println("      <td width=50% align=right class=label style=\"border-bottom: solid 1px white\">Number of repetitions:</td>");
		out.println("      <td width=20% align=left class=value><input type=text size=8 name=count value=100000></td>");
		out.println("    </tr>");
		out.println("    <tr>");
		out.println("      <td width=50% align=right class=label style=\"border-bottom: solid 1px white\">Calls per repetition:</td>");
		out.println("      <td width=20% align=left class=value><input type=text size=8 name=call_count value=1000></td>");
		out.println("    </tr>");
		out.println("    <tr>");
		out.println("      <td width=50% align=right class=label>Multi-threaded: </td>");
		out.println("      <td width=20% align=left class=value><input type=checkbox name=multi value=Multi></td>");
		out.println("      <td width=35% align=center class=value><input type=submit value=Run></td>");
		out.println("    </tr>");
		out.println("  </table>");
		out.println("</form>");
		UITemplate.bodyEnd(out);

		UITemplate.infoBox(out, "Java method monitoring benchmark");

		UITemplate.writeFooter(out);

		out.flush();
		out.close();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		int count = 0, call_count = 0;
		String multi = null;
		try
		{
			count = Integer.parseInt(request.getParameter("count"));
			call_count = Integer.parseInt(request.getParameter("call_count"));
			multi = request.getParameter("multi");
		} 
		catch ( NumberFormatException nfe ) {};

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		UITemplate.writeHeader(out);
		UITemplate.writeBanner(out, 6);

		MonitoredThread monTh = new MonitoredThread("Bank-Benchmark-Test-1", count, call_count, true);
		MonitoredThread umonTh = new MonitoredThread("Bank-Benchmark-Test-2", count, call_count, false);

		try 
		{
			if (multi != null)
			{
				monTh.start();
				umonTh.start();
				monTh.join();
				umonTh.join();
			}
			else
			{
				monTh.run();
				umonTh.run();      	
			}
		}
		catch (Exception e) 
		{
			UITemplate.bodyStart(out, "Java Method Monitoring Benchmark Result");
			out.println("Error while running benchmark: " + e);
			e.printStackTrace(out);       
			UITemplate.bodyEnd(out);
			UITemplate.writeFooter(out);
			out.flush();
			out.close();
			return;
		}

		long unmonitored_time = umonTh.getElapsed();
		long monitored_time = monTh.getElapsed();

		long diff = (monitored_time - unmonitored_time);
		UITemplate.bodyStart(out, "Java Method Monitoring Benchmark Result");
		out.println("<table class=info_body width=100% cellpadding=0 cellspacing=1 border=0>");
		out.println("  <tr>");
		out.println("    <td width=50% align=right class=label>Number of repetitions:</td>");
		out.println("    <td width=50% align=left>" + count + "</td>");
		out.println("  </tr>");
		out.println("  <tr>");
		out.println("    <td width=50% align=right class=label>Number of calls per repetition:</td>");
		out.println("    <td width=50% align=left>" + call_count + "</td>");
		out.println("  </tr>");
		out.println("  <tr>");
		out.println("    <td width=50% align=right class=label>Unmonitored elapsed time:</td>");
		out.println("    <td width=50% align=left>" + unmonitored_time + " ms, Elapsed time per call: " + (float)unmonitored_time/(float)call_count + " ms</td>");
		out.println("  </tr>");
		out.println("  <tr>");
		out.println("    <td width=50% align=right class=label>Monitored elapsed time:</td>");
		out.println("    <td width=50% align=left>" + monitored_time + " ms, Elapsed time per call: " + (float)monitored_time/(float)call_count + " ms</td>");
		out.println("  </tr>");
		out.println("  <tr>");
		out.println("    <td width=50% align=right class=label>Difference:</td>");
		out.println("    <td width=50% align=left>" + diff + " ms, Cost per call: " + (float)diff/(float)call_count + " ms</td>");
		out.println("  </tr>");
		out.println("  <tr>");
		out.println("    <td width=50% align=right class=label>Overhead:</td>");
		out.println("    <td width=50% align=left>" + (monitored_time > 0?((float)diff/(float)monitored_time)*100.0: 0.0) + "%</td>");
		out.println("  </tr>");
		out.println("</table>");
		UITemplate.bodyEnd(out);

		UITemplate.writeFooter(out);

		out.flush();
		out.close();
	}

	private void monitored_benchmark(int count)
	{
		for (int i = 0; i < count; i++)
			System.currentTimeMillis();
	}

	private void unmonitored_benchmark(int count)
	{
		for (int i = 0; i < count; i++)
			System.currentTimeMillis();
	}
}
