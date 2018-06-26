/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.weblogic9;

import java.io.IOException;
import java.net.InetAddress;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import org.netbeans.modules.weblogic.common.spi.WebLogicTrustHandler;
import org.openide.util.Lookup;

/**
 * Utility class.
 *
 * @author  Petr Jiricka, Pavel Buzek, Petr Hejl
 *
 */
public final class URLWait {

    private static final Logger LOGGER = Logger.getLogger(URLWait.class.getName());

    private static final HostnameVerifier EMPTY_VERIFIER = new HostnameVerifier() {

        @Override
        public boolean verify(String string, SSLSession ssls) {
            return true;
        }
    };

    private URLWait() {
        super();
    }

    /** Will wait until the URL is accessible and returns a valid resource
     * (response code other then 4xx or 5xx) or the timeout is reached.
     *
     * @return true if non error response was obtained
     */
    public static boolean waitForUrlReady(WLDeploymentManager dm, ExecutorService service, URL url, int timeout) {
        String host = url.getHost();
        try {
            InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            return false;
        }
        return waitForUrlConnection(dm, service, url, timeout, 100);
    }

    private static boolean waitForUrlConnection(WLDeploymentManager dm, ExecutorService service,
            URL url, int timeout, int retryTime) {

        Connect connect = new Connect(dm, url, retryTime);
        Future<Boolean> task = service.submit(connect);

        try {
            return task.get(timeout, TimeUnit.MILLISECONDS).booleanValue();
        } catch (ExecutionException ex) {
            LOGGER.log(Level.FINE, null, ex);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.FINE, null, ex);
            Thread.currentThread().interrupt();
        } catch (TimeoutException ex) {
            task.cancel(true);
        }
        return false;
    }

    private static class Connect implements Callable<Boolean> {

        private final WLDeploymentManager dm;

        private final URL url;

        private final int retryTime;

        private final String host;

        public Connect(WLDeploymentManager dm, URL url, int retryTime) {
            this.dm = dm;
            this.url = url;
            this.retryTime = retryTime;
            host = url.getHost();
        }

        public Boolean call() throws Exception {
            try {
                InetAddress.getByName(host);
            } catch (UnknownHostException e) {
                return Boolean.FALSE;
            }

            boolean interrupted = false;

            HttpURLConnection con = null;
            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }

                try {
                    if (dm != null && dm.isProxyMisconfigured()) {
                        con = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
                    } else {
                        con = (HttpURLConnection) url.openConnection();
                        if (con instanceof HttpsURLConnection) {
                            WebLogicTrustHandler handler = Lookup.getDefault().lookup(WebLogicTrustHandler.class);
                            if (handler != null) {
                                SSLContext context = SSLContext.getInstance("TLS"); // NOI18N
                                context.init(null, new TrustManager[] {handler.getTrustManager(dm.getCommonConfiguration())},
                                        new SecureRandom());
                                ((HttpsURLConnection) con).setSSLSocketFactory(
                                        (SSLSocketFactory) context.getSocketFactory());
                                ((HttpsURLConnection) con).setHostnameVerifier(EMPTY_VERIFIER);
                            }
                        }
                    }
                    int code = con.getResponseCode();
                    if (code == HttpURLConnection.HTTP_BAD_GATEWAY) {
                        if (dm != null) {
                            dm.setProxyMisconfigured(true);
                        }
                    } else {
                        boolean error = (code == -1)
                                // with no index page we will get 403 - FORBIDDEN
                                // so we handle it as "something is running there"
                                // as opposed to 404
                                || (code > 399 && code < 600 && code != 403);
                        if (!error) {
                            return Boolean.TRUE;
                        }
                    }
                } catch (IOException ioe) {
                    // try without proxy in next loop
                    if (dm != null) {
                        if (ioe.getMessage() != null && ioe.getMessage().contains(Integer.toString(HttpURLConnection.HTTP_BAD_GATEWAY))) {
                            dm.setProxyMisconfigured(true);
                        } else {
                            dm.setProxyMisconfigured(false);
                        }
                    }
                    LOGGER.log(Level.FINE, null, ioe);
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                }
                try {
                    Thread.sleep(retryTime);
                } catch (InterruptedException ie) {
                    interrupted = true;
                    break;
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
            return Boolean.FALSE;
        }
    }
}
