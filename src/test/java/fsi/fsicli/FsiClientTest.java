package fsi.fsicli;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

public class FsiClientTest {
	private static URL service_url;

	static {
		try {
			service_url = new URL("http://fsitest.mse.jhu.edu/fsi/");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	private static String user = "admin";
	private static String pass = "xxxx";

	@Test
	@Ignore
	public void testLogin() throws IOException {
		FsiClient client = new FsiClient(service_url);
		
		String ses = client.login(user, pass);
		
		System.err.println(ses);
		
		List<ImageInfo> result = client.list("aor/PrincetonU101");
		
		assertEquals(260, result.size());
		
		// client.upload("api/test.tif", "/home/msp/test.tif");
		
		client.createDirectory("api/pasture");
		
	}
	
	
}
