package spark.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpOptions;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpTrace;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

public class SparkTestUtil {

    private int port;

    private CloseableHttpClient httpClient;
    private List<Integer> followRedirectCodes;

    public SparkTestUtil(int port) {
        this.port = port;
        this.httpClient = httpClientBuilder().build();
    }

    private HttpClientBuilder httpClientBuilder() {
        SSLConnectionSocketFactory sslConnectionSocketFactory =
                new SSLConnectionSocketFactory(getSslFactory(), (paramString, paramSSLSession) -> true);
        Registry<ConnectionSocketFactory> socketRegistry = RegistryBuilder
                .<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", sslConnectionSocketFactory)
                .build();
        BasicHttpClientConnectionManager connManager = new BasicHttpClientConnectionManager(socketRegistry);
        return HttpClientBuilder.create().setConnectionManager(connManager);
    }

    // HttpClient 5.x's RedirectStrategy can no longer change the method used for a followed
    // redirect (that's now hardcoded inside RedirectExec: only POST is downgraded to GET on
    // 301/302, every other method - including PUT/DELETE - is preserved as-is). To replicate
    // the old 4.x behavior these tests rely on (a 301/302 redirect is followed as GET, except
    // HEAD which stays HEAD, regardless of the original method), disable HttpClient's built-in
    // redirect following entirely and do it ourselves in doMethod().
    public void setFollowRedirectStrategy(Integer... codes) {
        this.followRedirectCodes = Arrays.asList(codes);
        RequestConfig requestConfig = RequestConfig.custom().setRedirectsEnabled(false).build();
        this.httpClient = httpClientBuilder().setDefaultRequestConfig(requestConfig).build();
    }

    public UrlResponse get(String path) throws Exception {
        return doMethod("GET", path, null);
    }


    public UrlResponse doMethodSecure(String requestMethod, String path, String body)
            throws Exception {
        return doMethod(requestMethod, path, body, true, "text/html");
    }

    public UrlResponse doMethod(String requestMethod, String path, String body) throws Exception {
        return doMethod(requestMethod, path, body, false, "text/html");
    }

    public UrlResponse doMethodSecure(String requestMethod, String path, String body, String acceptType)
            throws Exception {
        return doMethod(requestMethod, path, body, true, acceptType);
    }

    public UrlResponse doMethod(String requestMethod, String path, String body, String acceptType) throws Exception {
        return doMethod(requestMethod, path, body, false, acceptType);
    }

    private UrlResponse doMethod(String requestMethod, String path, String body, boolean secureConnection,
                                 String acceptType) throws Exception {
        return doMethod(requestMethod, path, body, secureConnection, acceptType, null);
    }

    public UrlResponse doMethod(String requestMethod, String path, String body, boolean secureConnection,
                                String acceptType, Map<String, String> reqHeaders) throws IOException, ParseException {
        HttpUriRequest httpRequest = getHttpRequest(requestMethod, path, body, secureConnection, acceptType, reqHeaders);

        UrlResponse urlResponse;
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpRequest)) {
            urlResponse = toUrlResponse(httpResponse);
        }

        if (followRedirectCodes != null && followRedirectCodes.contains(urlResponse.status)) {
            String location = urlResponse.headers.get("Location");
            if (location != null) {
                URI redirectUri;
                try {
                    redirectUri = httpRequest.getUri().resolve(location);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                HttpUriRequest redirectRequest = "HEAD".equalsIgnoreCase(requestMethod)
                        ? new HttpHead(redirectUri)
                        : new HttpGet(redirectUri);
                try (CloseableHttpResponse redirectResponse = httpClient.execute(redirectRequest)) {
                    urlResponse = toUrlResponse(redirectResponse);
                }
            }
        }

        return urlResponse;
    }

    private UrlResponse toUrlResponse(CloseableHttpResponse httpResponse) throws IOException, ParseException {
        UrlResponse urlResponse = new UrlResponse();
        urlResponse.status = httpResponse.getCode();
        HttpEntity entity = httpResponse.getEntity();
        urlResponse.body = entity != null ? EntityUtils.toString(entity) : "";
        Map<String, String> headers = new HashMap<>();
        for (Header header : httpResponse.getHeaders()) {
            headers.put(header.getName(), header.getValue());
        }
        urlResponse.headers = headers;
        return urlResponse;
    }

    private HttpUriRequest getHttpRequest(String requestMethod, String path, String body, boolean secureConnection,
                                          String acceptType, Map<String, String> reqHeaders) {
        String protocol = secureConnection ? "https" : "http";
        String uri = protocol + "://localhost:" + port + path;

        if (requestMethod.equals("GET")) {
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setHeader("Accept", acceptType);
            addHeaders(reqHeaders, httpGet);
            return httpGet;
        }

        if (requestMethod.equals("POST")) {
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setHeader("Accept", acceptType);
            addHeaders(reqHeaders, httpPost);
            httpPost.setEntity(new StringEntity(body));
            return httpPost;
        }

        if (requestMethod.equals("PATCH")) {
            HttpPatch httpPatch = new HttpPatch(uri);
            httpPatch.setHeader("Accept", acceptType);
            addHeaders(reqHeaders, httpPatch);
            httpPatch.setEntity(new StringEntity(body));
            return httpPatch;
        }

        if (requestMethod.equals("DELETE")) {
            HttpDelete httpDelete = new HttpDelete(uri);
            addHeaders(reqHeaders, httpDelete);
            httpDelete.setHeader("Accept", acceptType);
            return httpDelete;
        }

        if (requestMethod.equals("PUT")) {
            HttpPut httpPut = new HttpPut(uri);
            httpPut.setHeader("Accept", acceptType);
            addHeaders(reqHeaders, httpPut);
            httpPut.setEntity(new StringEntity(body));
            return httpPut;
        }

        if (requestMethod.equals("HEAD")) {
            HttpHead httpHead = new HttpHead(uri);
            addHeaders(reqHeaders, httpHead);
            return httpHead;
        }

        if (requestMethod.equals("TRACE")) {
            HttpTrace httpTrace = new HttpTrace(uri);
            addHeaders(reqHeaders, httpTrace);
            return httpTrace;
        }

        if (requestMethod.equals("OPTIONS")) {
            HttpOptions httpOptions = new HttpOptions(uri);
            addHeaders(reqHeaders, httpOptions);
            return httpOptions;
        }

        if (requestMethod.equals("LOCK")) {
            HttpLock httpLock = new HttpLock(uri);
            addHeaders(reqHeaders, httpLock);
            return httpLock;
        }

        throw new IllegalArgumentException("Unknown method " + requestMethod);
    }

    private void addHeaders(Map<String, String> reqHeaders, HttpRequest req) {
        if (reqHeaders != null) {
            for (Map.Entry<String, String> header : reqHeaders.entrySet()) {
                req.addHeader(header.getKey(), header.getValue());
            }
        }
    }

    public int getPort() {
        return port;
    }

    /**
     * Convenience method to use own truststore on SSL Sockets. Will default to
     * the self signed keystore provided in resources, but will respect
     * <p/>
     * -Djavax.net.ssl.keyStore=serverKeys
     * -Djavax.net.ssl.keyStorePassword=password
     * -Djavax.net.ssl.trustStore=serverTrust
     * -Djavax.net.ssl.trustStorePassword=password SSLApplication
     * <p/>
     * So these can be used to specify other key/trust stores if required.
     *
     * @return an SSL Socket Factory using either provided keystore OR the
     * keystore specified in JVM params
     */
    private SSLSocketFactory getSslFactory() {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream fis = new FileInputStream(getTrustStoreLocation());
            keyStore.load(fis, getTrustStorePassword().toCharArray());
            fis.close();

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, tmf.getTrustManagers(), null);
            return ctx.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Return JVM param set keystore or default if not set.
     *
     * @return Keystore location as string
     */
    public static String getKeyStoreLocation() {
        String keyStoreLoc = System.getProperty("javax.net.ssl.keyStore");
        return keyStoreLoc == null ? "./src/test/resources/keystore.jks" : keyStoreLoc;
    }

    /**
     * Return JVM param set keystore password or default if not set.
     *
     * @return Keystore password as string
     */
    public static String getKeystorePassword() {
        String password = System.getProperty("javax.net.ssl.keyStorePassword");
        return password == null ? "password" : password;
    }

    /**
     * Return JVM param set truststore location, or keystore location if not
     * set. if keystore not set either, returns default
     *
     * @return truststore location as string
     */
    public static String getTrustStoreLocation() {
        String trustStoreLoc = System.getProperty("javax.net.ssl.trustStore");
        return trustStoreLoc == null ? getKeyStoreLocation() : trustStoreLoc;
    }

    /**
     * Return JVM param set truststore password or keystore password if not set.
     * If still not set, will return default password
     *
     * @return truststore password as string
     */
    public static String getTrustStorePassword() {
        String password = System.getProperty("javax.net.ssl.trustStorePassword");
        return password == null ? getKeystorePassword() : password;
    }

    public static class UrlResponse {

        public Map<String, String> headers;
        public String body;
        public int status;
    }

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
        }
    }

    static class HttpLock extends HttpUriRequestBase {
        public final static String METHOD_NAME = "LOCK";

        public HttpLock(final String uri) {
            super(METHOD_NAME, URI.create(uri));
        }
    }

}
