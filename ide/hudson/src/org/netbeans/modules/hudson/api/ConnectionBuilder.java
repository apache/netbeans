/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.hudson.api;

import java.awt.EventQueue;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.modules.hudson.spi.ConnectionAuthenticator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.hudson.api.Bundle.*;
import org.openide.filesystems.FileUtil;
import org.openide.util.NetworkSettings;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Creates an HTTP connection to Hudson.
 * Handles redirects and authentication.
 */
public final class ConnectionBuilder {

    private static final Logger LOG = Logger.getLogger(ConnectionBuilder.class.getName());
    private static final RequestProcessor TIMER = new RequestProcessor(ConnectionBuilder.class.getName() + ".TIMER"); // NOI18N
    /** Do not prompt for authentication for the same server more than once in a given session. */
    private static final Set</*URL*/String> authenticationRejected = new HashSet<String>();

    /**
     * Session cookies set by home.
     * {@link java.net.CookieManager} in JDK 6 would be a bit easier.
     */
    private static final Map</*URL*/String,String[]> COOKIES = new HashMap<String,String[]>();

    private static final Map</*URL*/String,/*[field,crumb]*/String[]> crumbs = Collections.synchronizedMap(new HashMap<String,String[]>()); // #193008

    private URL home;
    private URL url;
    private final Map<String,String> requestHeaders = new LinkedHashMap<String,String>();
    private byte[] postData;
    private int timeout;
    private boolean auth = true;
    private boolean followRedirects = true;

    /**
     * Prepare a connection.
     * You must also specify a location, and if possible an associated instance or job.
     */
    public ConnectionBuilder() {}

    /**
     * Specify the location to connect to.
     * @param url location to open
     * @return this builder
     */
    public ConnectionBuilder url(URL url) {
        this.url = url;
        return this;
    }

    /**
     * Specify the location to connect to.
     * @param url location to open
     * @return this builder
     */
    public ConnectionBuilder url(String url) throws MalformedURLException {
        return url(new URL(url));
    }

    /**
     * Specify the home URL.
     * Useful for login authentication.
     * @param url the base URL of the Hudson instance
     * @return this builder
     */
    public ConnectionBuilder homeURL(URL url) {
        this.home = url;
        return this;
    }

    /**
     * Specify the home URL. Useful for login authentication.
     *
     * @param url the base URL of the Hudson instance
     * @return this builder
     */
    public ConnectionBuilder homeURL(String url) throws MalformedURLException {
        this.home = new URL(url);
        return this;
    }

    /**
     * Specify the Hudson instance as per {@link #homeURL}.
     * @param instance a Hudson instance
     * @return this builder
     */
    public ConnectionBuilder instance(HudsonInstance instance) {
        try {
            this.home = new URL(instance.getUrl());
        } catch (MalformedURLException x) {
            LOG.warning(x.toString());
        }
        return this;
    }

    /**
     * Specify the job, and hence the Hudson instance as per {@link #homeURL}.
     * @param job an arbitrary job in an instance
     * @return this builder
     */
    public ConnectionBuilder job(HudsonJob job) {
        HudsonInstance instance = job.getInstance();
        if (instance != null) {
            instance(instance);
        }
        return this;
    }

    /**
     * Define an HTTP request header.
     * @param key header key
     * @param value header value
     * @return this builder
     */
    public ConnectionBuilder header(String key, String value) {
        requestHeaders.put(key, value);
        return this;
    }

    /**
     * Post data to the connection.
     * @param data bytes to post
     * @return this builder
     */
    @SuppressWarnings("EI_EXPOSE_REP2")
    public ConnectionBuilder postData(byte[] data) {
        postData = data;
        return this;
    }

    /**
     * Sets a timeout on the response.
     * If the connection has not opened within that time,
     * {@link InterruptedIOException} will be thrown from {@link #connection}.
     * @param milliseconds time to wait
     * @return this builder
     */
    public ConnectionBuilder timeout(int milliseconds) {
        timeout = milliseconds;
        return this;
    }

    /**
     * Configures whether to prompt for authentication.
     * @param true to prompt for authentication (the default), false to immediately report 403s as errors
     * @return this builder
     */
    public ConnectionBuilder authentication(boolean a) {
        auth = a;
        return this;
    }

    /**
     * Configures whether to follow redirects.
     * Useful to pass false in case you do not care about the result page.
     * @param true to follow HTTP 301/302 redirects (the default), false to return the connection without error
     * @return this builder
     */
    public ConnectionBuilder followRedirects(boolean fr) {
        followRedirects = fr;
        return this;
    }

    /**
     * Actually try to open the connection.
     * May need to retry to handle redirects and/or authentication.
     * @return an open and valid connection, ready for {@link URLConnection#getInputStream},
     *         {@link URLConnection#getHeaderField(String)}, etc.
     * @throws FileNotFoundException in case of {@link HttpURLConnection#HTTP_NOT_FOUND}
     * @throws HttpRetryException in case of other non-{@link HttpURLConnection#HTTP_OK} status codes
     *                             (not including redirects even if {@link #followRedirects} is false,
     *                             but including {@link HttpURLConnection#HTTP_FORBIDDEN} if {@link #authentication} is false)
     * @throws IOException for various other reasons
     */
    public URLConnection connection() throws IOException {
        if (url == null) {
            throw new IllegalArgumentException("You must call the url method!"); // NOI18N
        }
        if (url.getProtocol().matches("https?") && EventQueue.isDispatchThread()) {
            throw new IOException("#184196: refusing to open " + url + " from EQ");
        }
        if (timeout == 0) {
            return doConnection();
        } else {
            final Thread curr = Thread.currentThread();
            RequestProcessor.Task task = TIMER.post(new Runnable() {
                public @Override void run() {
                    curr.interrupt();
                }
            }, timeout);
            try {
                return doConnection();
            } finally {
                task.cancel();
            }
        }
    }

    @Messages({"# {0} - URL", "ConnectionBuilder.log_in=Must log in to access {0}"})
    private URLConnection doConnection() throws IOException {
        URLConnection conn = url.openConnection();
        RETRY: while (true) {
            if (conn instanceof HttpURLConnection) {
                ((HttpURLConnection) conn).setInstanceFollowRedirects(false);
            }
            if (conn instanceof HttpsURLConnection) {
                // #161324: permit self-signed SSL certificates.
                try {
                    SSLContext sc = SSLContext./* XXX JDK 6: getDefault() */getInstance("SSL"); // NOI18N
                    sc.init(null, new TrustManager[] {
                        new X509TrustManager() {
                            public @Override void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                            public @Override void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                            public @Override X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        }
                    }, new SecureRandom());
                    ((HttpsURLConnection) conn).setSSLSocketFactory(sc.getSocketFactory());
                    ((HttpsURLConnection) conn).setHostnameVerifier(new HostnameVerifier() {
                        public @Override boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });
                } catch (Exception x) {
                    LOG.log(Level.FINE, "could not disable SSL verification", x);
                }
            }
            URL curr = conn.getURL();
            LOG.log(Level.FINER, "Trying to open {0}", curr);
            if (home != null) {
                for (ConnectionAuthenticator authenticator : Lookup.getDefault().lookupAll(ConnectionAuthenticator.class)) {
                    authenticator.prepareRequest(conn, home);
                }
                if (COOKIES.containsKey(home.toString())) {
                    for (String cookie : COOKIES.get(home.toString())) {
                        String cookieBare = cookie.replaceFirst(";.*", ""); // NOI18N
                        LOG.log(Level.FINER, "Setting cookie {0} for {1}", new Object[] {cookieBare, conn.getURL()});
                        conn.setRequestProperty("Cookie", cookieBare); // NOI18N
                    }
                }
                String[] fieldCrumb = crumbs.get(home.toString());
                if (fieldCrumb != null) {
                    conn.setRequestProperty(fieldCrumb[0], fieldCrumb[1]);
                }
            }
            if (postData != null) {
                conn.setDoOutput(true);
            }
            for (Map.Entry<String,String> header : requestHeaders.entrySet()) {
                conn.setRequestProperty(header.getKey(), header.getValue());
            }
            try {
                conn.connect();
            } catch (IOException x) {
                throw x;
            } catch (Exception x) {
                // JRE #6797318, etc.; various bugs in JRE networking code; see e.g. #163555
                throw new IOException("Connecting to " + curr + ": " + x, x);
            }
            if (postData != null) {
                OutputStream os = conn.getOutputStream();
                try {
                    os.write(postData);
                } finally {
                    os.close();
                }
            }
            if (!(conn instanceof HttpURLConnection)) {
                break;
            }
            if (home != null) {
                List<String> cookies = getHeaderFields(conn).get("Set-Cookie"); // NOI18N
                if (cookies != null) {
                    LOG.log(Level.FINE, "Cookies set for domain {0}: {1}", new Object[] {home, cookies});
                    COOKIES.put(home.toString(), cookies.toArray(new String[0]));
                }
            }
            int responseCode = ((HttpURLConnection) conn).getResponseCode();
            LOG.log(Level.FINER, "  => {0}", responseCode);
            switch (responseCode) {
            // Workaround for JDK bug #6810084; HttpURLConnection.setInstanceFollowRedirects does not work.
            case HttpURLConnection.HTTP_MOVED_PERM:
            case HttpURLConnection.HTTP_MOVED_TEMP:
                if (!followRedirects) {
                    break RETRY;
                }
                URL redirect = new URL(conn.getHeaderField("Location")); // NOI18N
                conn = redirect.openConnection();
                continue RETRY;
            case HttpURLConnection.HTTP_FORBIDDEN:
                if (auth && home != null) {
                    synchronized (authenticationRejected) {
                        if (!authenticationRejected.contains(home.toString())) {
                            for (ConnectionAuthenticator authenticator : Lookup.getDefault().lookupAll(ConnectionAuthenticator.class)) {
                                URLConnection retry = authenticator.forbidden(conn, home);
                                if (retry != null) {
                                    LOG.log(Level.FINER, "Retrying after auth from {0}", authenticator);
                                    conn = retry;
                                    try { // check for CSRF before continuing
                                        InputStream is = new ConnectionBuilder().url(new URL(home, "crumbIssuer/api/xml?xpath=concat(//crumbRequestField,'=',//crumb)")).homeURL(home).connection().getInputStream();
                                        try {
                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                            FileUtil.copy(is, baos);
                                            String crumb = baos.toString("UTF-8");
                                            String[] crumbA = crumb.split("=", 2);
                                            if (crumbA.length == 2 && crumbA[0].indexOf('\n') == -1) {
                                                LOG.log(Level.FINER, "Received crumb: {0}", crumb);
                                                crumbs.put(home.toString(), crumbA);
                                            } else {
                                                LOG.log(Level.WARNING, "Bad crumb response: {0}", crumb);
                                            }
                                        } finally {
                                            is.close();
                                        }
                                    } catch (FileNotFoundException x) {
                                        LOG.finer("not using crumbs");
                                    }
                                    continue RETRY;
                                }
                            }
                            authenticationRejected.add(home.toString());
                        }
                    }
                }
                IOException x = new HttpRetryException("403 on " + url, responseCode); // NOI18N
                Exceptions.attachLocalizedMessage(x, ConnectionBuilder_log_in(url));
                throw x;
            case HttpURLConnection.HTTP_NOT_FOUND:
                throw new FileNotFoundException(curr.toString());
            case HttpURLConnection.HTTP_OK:
                break RETRY;
            default:
                // XXX are there other legitimate response codes?
                String resMsg = ((HttpURLConnection) conn).getResponseMessage();
                String errMsg = "Server rejected connection to " //NOI18N
                        + curr + " with code " + responseCode //NOI18N
                        + (resMsg != null ? " and message " + resMsg : ""); //NOI18N
                throw new HttpRetryException(errMsg, responseCode);
            }
        }
        return conn;
    }

    /**
     * Call {@link URLConnection#getHeaderFields()}, supppress authentication
     * dialog if user interaction is forbidden.
     */
    private Map<String, List<String>> getHeaderFields(
            final URLConnection conn) throws IOException {
        return callSilentlyIfNeeded(conn,
                new Callable<Map<String, List<String>>>() {
                    @Override
                    public Map<String, List<String>> call() throws Exception {
                        return conn.getHeaderFields();
                    }
                });
    }

    /**
     * Call a callable. If user interaction is forbidden by the connection,
     * disable authentication dialog.
     */
    private <R> R callSilentlyIfNeeded(URLConnection conn, Callable<R> call)
            throws IOException {

        if (conn.getAllowUserInteraction()) {
            try {
                return call.call();
            } catch (Exception ex) {
                throw new IOException(ex);
            }
        } else {
            try {
                return NetworkSettings.suppressAuthenticationDialog(call);
            } catch (Exception ex) {
                throw new IOException(ex);
            }
        }
    }

    /**
     * Like {@link #connection} but coerced to an HTTP connection.
     * @throws IOException see {@link #connection} for subtype description
     */
    public HttpURLConnection httpConnection() throws IOException {
        URLConnection c = connection();
        if (c instanceof HttpURLConnection) {
            return (HttpURLConnection) c;
        } else {
            throw new IOException("Not an HTTP connection: " + c); // NOI18N
        }
    }

    /**
     * Parses content as XML.
     * @throws IOException see {@link #connection} for subtype description; also thrown in case of malformed XML
     */
    public Document parseXML() throws IOException {
        URLConnection c = connection();
        InputSource source = new InputSource(url.toString());
        source.setByteStream(c.getInputStream());
        try {
            return XMLUtil.parse(source, false, false, XMLUtil.defaultErrorHandler(), null);
        } catch (SAXException x) {
            throw new IOException(x);
        }
    }

    /**
     * Clears list of servers for which we are not checking for authentication any more.
     * Normally when login fails that server is blacklisted so that you are not pestered to log in over and over.
     * But sometimes you want to explicitly try again, and should not need to restart the IDE.
     *
     * @since hudson/1.30
     */
    public static void clearRejectedAuthentication() {
        authenticationRejected.clear();
    }

}
