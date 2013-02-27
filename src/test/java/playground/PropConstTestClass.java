package playground;

public class PropConstTestClass {
	private int i;

	public PropConstTestClass(int i) {
		this.i = fak(i);
	}

	private int fak(int i) {
		if (i == 0)
			return 1;
		return i * fak(i - 1);
	}
}

// import java.io.IOException;
// import java.io.Serializable;
// import java.net.HttpURLConnection;
// import java.net.URI;
// import java.net.URLConnection;
// import java.util.Iterator;
// import java.util.List;
//
// import
// sun.org.mozilla.javascript.commonjs.module.provider.UrlConnectionExpiryCalculator;
//
// // Referenced classes of package
// sun.org.mozilla.javascript.commonjs.module.provider:
// // UrlConnectionExpiryCalculator, UrlModuleSourceProvider
//
// public class PropConstTestClass implements Serializable {
//
// private static final long serialVersionUID = 1L;
// private final URI uri;
// private final long lastModified;
// private final String entityTags;
// private long expiry;
//
// boolean updateValidator(URLConnection urlConnection, long request_time,
// UrlConnectionExpiryCalculator urlConnectionExpiryCalculator)
// throws IOException {
// boolean isResourceChanged = isResourceChanged(urlConnection);
// if (!isResourceChanged) {
// expiry = calculateExpiry(urlConnection, request_time,
// urlConnectionExpiryCalculator);
// }
// return isResourceChanged;
// }
//
// private boolean isResourceChanged(URLConnection urlConnection)
// throws IOException {
// if (urlConnection instanceof HttpURLConnection) {
// return ((HttpURLConnection) urlConnection).getResponseCode() == 304;
// } else {
// return lastModified == urlConnection.getLastModified();
// }
// }
//
// private long calculateExpiry(URLConnection urlConnection,
// long request_time,
// UrlConnectionExpiryCalculator urlConnectionExpiryCalculator) {
// if ("no-cache".equals(urlConnection.getHeaderField("Pragma"))) {
// return 0L;
// }
// String cacheControl = urlConnection.getHeaderField("Cache-Control");
// if (cacheControl != null) {
// if (cacheControl.indexOf("no-cache") != -1) {
// return 0L;
// }
// int max_age = getMaxAge(cacheControl);
// if (-1 != max_age) {
// long response_time = System.currentTimeMillis();
// long apparent_age = Math.max(0L,
// response_time - urlConnection.getDate());
// long corrected_received_age = Math
// .max(apparent_age, (long) urlConnection
// .getHeaderFieldInt("Age", 0) * 1000L);
// long response_delay = response_time - request_time;
// long corrected_initial_age = corrected_received_age
// + response_delay;
// long creation_time = response_time - corrected_initial_age;
// return (long) max_age * 1000L + creation_time;
// }
// }
// long explicitExpiry = urlConnection.getHeaderFieldDate("Expires", -1L);
// if (explicitExpiry != -1L) {
// return explicitExpiry;
// } else {
// return urlConnectionExpiryCalculator != null ? urlConnectionExpiryCalculator
// .calculateExpiry(urlConnection) : 0L;
// }
// }
//
// private int getMaxAge(String cacheControl) {
// String strAge;
// int maxAgeIndex = cacheControl.indexOf("max-age");
// if (maxAgeIndex == -1) {
// return -1;
// }
// int eq = cacheControl.indexOf('=', maxAgeIndex + 7);
// if (eq == -1) {
// return -1;
// }
// int comma = cacheControl.indexOf(',', eq + 1);
// if (comma == -1) {
// strAge = cacheControl.substring(eq + 1);
// } else {
// strAge = cacheControl.substring(eq + 1, comma);
// }
// return Integer.parseInt(strAge);
//
// }
//
// private String getEntityTags(URLConnection urlConnection) {
// List etags = (List) urlConnection.getHeaderFields().get("ETag");
// if (etags == null || etags.isEmpty()) {
// return null;
// }
// StringBuilder b = new StringBuilder();
// Iterator it = etags.iterator();
// b.append((String) it.next());
// for (; it.hasNext(); b.append(", ").append((String) it.next())) {
// }
// return b.toString();
// }
//
// boolean appliesTo(URI uri) {
// return this.uri.equals(uri);
// }
//
// void applyConditionals(URLConnection urlConnection) {
// if (lastModified != 0L) {
// urlConnection.setIfModifiedSince(lastModified);
// }
// if (entityTags != null && entityTags.length() > 0) {
// urlConnection.addRequestProperty("If-None-Match", entityTags);
// }
// }
//
// boolean entityNeedsRevalidation() {
// return System.currentTimeMillis() > expiry;
// }
//
// public PropConstTestClass(URI uri, URLConnection urlConnection,
// long request_time,
// UrlConnectionExpiryCalculator urlConnectionExpiryCalculator) {
// this.uri = uri;
// lastModified = urlConnection.getLastModified();
// entityTags = getEntityTags(urlConnection);
// expiry = calculateExpiry(urlConnection, request_time,
// urlConnectionExpiryCalculator);
// }
// }
