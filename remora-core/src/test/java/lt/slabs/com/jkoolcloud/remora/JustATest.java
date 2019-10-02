package lt.slabs.com.jkoolcloud.remora;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.HttpURLConnection;
import java.net.URL;

public class JustATest {
	public void instrumentedMethod(String uri, String arg) throws IOException {

			HttpURLConnection urlConnection = (HttpURLConnection) new URL(uri).openConnection();
			throw new IOException("TEST CASE");

		//System.out.println(urlConnection.getResponseCode());
	}


}
