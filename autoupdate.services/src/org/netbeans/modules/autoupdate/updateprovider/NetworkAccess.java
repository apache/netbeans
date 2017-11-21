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
package org.netbeans.modules.autoupdate.updateprovider;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jirka Rechtacek
 */
public class NetworkAccess {
    private static final Logger err = Logger.getLogger(NetworkAccess.class.getName());

    private static final RequestProcessor NETWORK_ACCESS = new RequestProcessor("autoupdate-network-access", 10, false);

    private static final int MAX_REDIRECTS = 10;

    private NetworkAccess () {}
    
    public static Task createNetworkAcessTask (URL url, int timeout, NetworkListener networkAcesssListener) {
        return new Task (url, timeout, networkAcesssListener);
    }
    
    public static class Task implements Cancellable {
        private URL url;
        private int timeout;
        private NetworkListener listener;
        private final ExecutorService es = Executors.newSingleThreadExecutor ();
        private Future<InputStream> connect = null;
        private RequestProcessor.Task rpTask = null;
        
        private Task (URL url, int timeout, NetworkListener listener) {
            if (url == null) {
                throw new IllegalArgumentException ("URL cannot be null.");
            }
            if (listener == null) {
                throw new IllegalArgumentException ("NetworkListener cannot be null.");
            }
            this.url = url;
            this.timeout = timeout;
            this.listener = listener;
            postTask ();
        }
        
        private void postTask () {
            final SizedConnection connectTask = createCallableNetwork (url, timeout);
            rpTask = NETWORK_ACCESS.post (new Runnable () {
                @Override
                public void run () {
                    connect = es.submit (connectTask);
                    InputStream is;
                    try {
                        is = connect.get (timeout, TimeUnit.MILLISECONDS);
                        if (connect.isDone ()) {
                            listener.streamOpened (is, connectTask.getContentLength() );
                        } else if (connect.isCancelled ()) {
                            listener.accessCanceled ();
                        } else {
                            listener.accessTimeOut ();
                        }
                    } catch(InterruptedException ix) {
                        listener.notifyException (ix);
                    } catch (ExecutionException ex) {
                        Throwable t = ex.getCause();
                        if(t!=null && t instanceof Exception) {
                            listener.notifyException ((Exception) t);
                        } else {
                            listener.notifyException (ex);
                        }
                    } catch (CancellationException ex) {
                        listener.accessCanceled ();
                    } catch(TimeoutException tx) {
                        IOException io = new IOException(NbBundle.getMessage(NetworkAccess.class, "NetworkAccess_Timeout", url));
                        io.initCause(tx);
                        listener.notifyException (io);
                    }
                }
            });
        }
        
        public void waitFinished () {
            assert rpTask != null : "RequestProcessor.Task must be initialized.";
            rpTask.waitFinished ();
        }
        public boolean isFinished () {
            assert rpTask != null : "RequestProcessor.Task must be initialized.";
            return rpTask.isFinished ();
        }

        
        private SizedConnection createCallableNetwork (final URL url, final int timeout) {
            return new SizedConnection () {
                private int contentLength = -1;

                @Override
                public int getContentLength() {
                    return contentLength;
                }

                @Override
                public InputStream call () throws Exception {
                    URLConnection conn = url.openConnection ();
                    conn.setConnectTimeout (timeout);
                    conn.setReadTimeout(timeout);
                    if(conn instanceof HttpsURLConnection){
                        NetworkAccess.initSSL((HttpsURLConnection) conn);
                    }

                    // handle redirection here
                    int redirCount = 0;
                    URLConnection redir = conn;
                    do {
                       conn = redir;
                       redir = checkRedirect(conn, timeout);
                       redirCount++;
                    } while (conn != redir && redirCount <= MAX_REDIRECTS);

                    if (conn != redir) {
                        throw new IOException("Too many redirects for " + url);
                    }

                    InputStream is = conn.getInputStream ();
                    contentLength = conn.getContentLength();
                    if (err.isLoggable(Level.FINE)) {
                        Map <String, List <String>> map = conn.getHeaderFields();
                        StringBuilder sb = new StringBuilder("Connection opened for:\n");
                        sb.append("    Url: ").append(conn.getURL()).append("\n");
                        for(String field : map.keySet()) {
                           sb.append("    ").append(field==null ? "Status" : field).append(": ").append(map.get(field)).append("\n");
                        }
                        sb.append("\n");
                        err.log(Level.FINE, sb.toString());
                    }
                    return new BufferedInputStream (is);
                }
            };
        }
        
        @Override
        public boolean cancel () {
            return connect.cancel (true);
        }
        
    }
    private interface SizedConnection extends Callable<InputStream> {
        public int getContentLength();
    }
    public interface NetworkListener {
        public void streamOpened (InputStream stream, int contentLength);
        public void accessCanceled ();
        public void accessTimeOut ();
        public void notifyException (Exception x);
    }
    
    public static void initSSL(HttpURLConnection httpCon) throws IOException {
        if (httpCon instanceof HttpsURLConnection) {
            HttpsURLConnection https = (HttpsURLConnection) httpCon;

            try {
                TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }};
                SSLContext sslContext = SSLContext.getInstance("SSL"); // NOI18N
                sslContext.init(null, trustAllCerts, new SecureRandom());
                https.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
                https.setSSLSocketFactory(sslContext.getSocketFactory());
            } catch (KeyManagementException ex) {
                throw new IOException(ex);
            } catch (NoSuchAlgorithmException ex) {
                throw new IOException(ex);
            }
        }
    }

    private static URLConnection checkRedirect(URLConnection conn, int timeout) throws IOException {
        if (conn instanceof HttpURLConnection) {
            conn.connect();
            int code = ((HttpURLConnection) conn).getResponseCode();
            if (code == HttpURLConnection.HTTP_MOVED_TEMP
                    || code == HttpURLConnection.HTTP_MOVED_PERM) {
                // in case of redirection, try to obtain new URL
                String redirUrl = conn.getHeaderField("Location"); //NOI18N
                if (null != redirUrl && !redirUrl.isEmpty()) {
                    //create connection to redirected url and substitute original conn
                    URL redirectedUrl = new URL(redirUrl);
                    URLConnection connRedir = redirectedUrl.openConnection();
                    // XXX is this neede
                    connRedir.setRequestProperty("User-Agent", "NetBeans"); // NOI18N
                    connRedir.setConnectTimeout(timeout);
                    connRedir.setReadTimeout(timeout);
                    if (connRedir instanceof HttpsURLConnection) {
                        NetworkAccess.initSSL((HttpsURLConnection) connRedir);
                    }
                    return connRedir;
                }
            }
        }
        return conn;
    }
}
