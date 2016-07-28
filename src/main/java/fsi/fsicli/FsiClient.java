package fsi.fsicli;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import okhttp3.Cookie;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Playing around with what a client for FSI might look like. Methods are only
 * partially implemented. User must either login or set a session token returned
 * from a previous login attempt. Sessions expire and can be refreshed.
 * 
 * There are two apis, one under /fsi/service and one under /fsi/server. The
 * service API is fairly conventional crud called their Open API. The server API
 * is more aimed at user interfaces and supports various image operations.
 * 
 * The included documentation is poor. Most of this was figured out by examining
 * what the user interface does. Get scaled image:
 * 
 * http://fsitest.mse.jhu.edu/fsi/server?renderer=jpeg&type=image&source=aor/
 * PrincetonK6233/PrincetonK6233.001r.tif&width=400
 * 
 * Login: http://fsitest.mse.jhu.edu/fsi/service/login
 **/

public class FsiClient {
	private static String SESSION_COOKIE_NAME = "JSESSIONID";

	private final URL service_url;
	private final URL server_url;

	private final OkHttpClient client;
	private final SimpleCookieJar cookie_jar;
	private final FsiResponseParser parser;

	/**
	 * @param base_url
	 *            URL to webapp like http://localhost/fsi/
	 * @throws IOException
	 */
	public FsiClient(URL base_url) {
		if (!base_url.getPath().endsWith("/")) {
			throw new IllegalArgumentException("Base url must end with /");
		}

		this.service_url = join(base_url, "service");
		this.server_url = join(base_url, "server");

		this.parser = new FsiResponseParser();
		this.cookie_jar = new SimpleCookieJar();
		this.client = new OkHttpClient.Builder().cookieJar(cookie_jar).build();
	}

	private URL join(URL base, String path) {
		try {
			return new URL(base.toString() + path);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get salt. It also sets session cookie.
	 * 
	 * @return Salt to use for login request.
	 * @throws IOException
	 */
	private String make_salt_request() throws IOException {
		URL url = join(service_url, "/login");

		Request req = new Request.Builder().url(url).build();
		Response resp = client.newCall(req).execute();

		if (!resp.isSuccessful()) {
			throw new IOException("Unexpected code " + resp);
		}

		try (InputStream is = resp.body().byteStream()) {
			SaltResponse salt = parser.parseSaltResponse(is);

			if (!salt.isSuccess()) {
				throw new IOException("Salt request failed: " + url);
			}

			return salt.getSalt();
		}
	}

	private String get_session_id(URL url) throws IOException {
		Optional<Cookie> result = client.cookieJar().loadForRequest(HttpUrl.get(url)).stream()
				.filter(c -> c.name().equals(SESSION_COOKIE_NAME)).findAny();

		if (!result.isPresent()) {
			throw new IOException("No session cookie found");
		}

		return result.get().value();
	}

	// Sets JSESSIONID cookie
	private String make_login_request(String user, String pass, String salt) throws IOException {
		URL url = join(service_url, "/login");

		String login_hash = hash_password(salt, pass);

		RequestBody body = new FormBody.Builder().add("username", user).add("password", login_hash).build();
		Request req = new Request.Builder().url(url).post(body).build();
		Response resp = client.newCall(req).execute();

		if (!resp.isSuccessful()) {
			throw new IOException("Unexpected code " + resp);
		}

		try (InputStream is = resp.body().byteStream()) {
			LoginResponse login = parser.parseLoginResponse(is);

			if (!login.isSuccess()) {
				throw new IOException("Login request failed: " + login);
			}
		}

		System.err.println("Login success!");

		return get_session_id(url);
	}

	// Logging in requires three steps: Requesting a salt, generating a hash
	// value using the password and the salt,
	// sending the hashvalue and the username to the server.
	// The session id set by the salt response is maintained by the cookie jar
	// and needed by the login request.

	public String login(String user, String pass) throws IOException {
		System.err.println("User: " + user);
		System.err.println("Password: " + pass);

		String salt = make_salt_request();

		System.err.println("Salt: " + salt);

		String ses = make_login_request(user, pass, salt);

		System.out.println("Session:" + ses);

		setSession(ses);

		return ses;
	}

	public void setSession(String ses) {
		cookie_jar.setDefaultCookies(Collections.singletonList(create_session_cookie(ses)));
	}

	public void refreshSession() {
		throw new UnsupportedOperationException();
	}

	public String hash_password(String salt, String pass) {
		String pass_hash = Util.SHA256(pass);
		String login_hash = Util.SHA256(salt + pass_hash);

		System.err.println("Pass hash: " + pass_hash);
		System.err.println("Login hash: " + login_hash);

		return login_hash;
	}

	public void logout() {
		throw new UnsupportedOperationException();
	}

	private Cookie create_session_cookie(String ses) {
		return new Cookie.Builder().name(SESSION_COOKIE_NAME).value(ses).domain(service_url.getHost()).path("/")
				.build();
	}

	// List files on the server
	// http://fsitest.mse.jhu.edu/fsi/server?source=aor/PrincetonU101&type=list
	public List<ImageInfo> list(String dir) throws IOException {
		HttpUrl url = HttpUrl.get(server_url).newBuilder().addQueryParameter("type", "list")
				.addQueryParameter("source", dir).build();

		Request req = new Request.Builder().url(url).build();
		Response resp = client.newCall(req).execute();

		if (!resp.isSuccessful()) {
			throw new IOException("Unexpected code " + resp);
		}

		try (InputStream is = resp.body().byteStream()) {
			return parser.parseListResponse(is);
		}
	}

	// Create directory on server
	public void createDirectory(String server_path) throws IOException {
		if (!server_path.startsWith("/")) {
			server_path = "/" + server_path;
		}

		URL url = join(service_url, "/directory" + server_path);

		RequestBody body = RequestBody.create(null, new byte[0]);
		Request req = new Request.Builder().url(url).put(body).build();

		Response resp = client.newCall(req).execute();

		if (!resp.isSuccessful()) {
			throw new IOException("Unexpected code " + resp);
		}

		System.err.println(resp.body().string());
	}

	// Upload an image
	public void upload(String server_path, String local_path) throws IOException {
		if (!server_path.startsWith("/")) {
			server_path = "/" + server_path;
		}

		URL url = join(service_url, "/file" + server_path);

		RequestBody body = RequestBody.create(null, new File(local_path));
		Request req = new Request.Builder().url(url).put(body).build();

		Response resp = client.newCall(req).execute();

		if (!resp.isSuccessful()) {
			throw new IOException("Unexpected code " + resp);
		}

		System.err.println(resp.body().string());
	}

	// Image operation
	public void image() {
		throw new UnsupportedOperationException();
	}

	// Get image metadata
	public void info() {
		throw new UnsupportedOperationException();
	}

}
