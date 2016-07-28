package fsi.fsicli;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * In memory cookie jar that doesn't implement any policy and just returns
 * cookies that were set for a URL.
 */
public class SimpleCookieJar implements CookieJar {
	private final Map<HttpUrl, List<Cookie>> jar;
	private List<Cookie> default_cookies;

	public SimpleCookieJar() {
		jar = new HashMap<>();
		default_cookies = Collections.emptyList();
	}

	@Override
	public List<Cookie> loadForRequest(HttpUrl url) {
		System.err.println("cookies for " + url);

		return jar.getOrDefault(url, default_cookies);
	}

	@Override
	public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
		jar.put(url, cookies);
	}

	public void setDefaultCookies(List<Cookie> cookies) {
		default_cookies = cookies;
	}
}
