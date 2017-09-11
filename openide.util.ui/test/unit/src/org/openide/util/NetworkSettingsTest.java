/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.openide.util;

import java.io.IOException;
import java.net.Authenticator;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import junit.framework.TestCase;
import org.netbeans.junit.MockServices;
import org.openide.util.NetworkSettings.ProxyCredentialsProvider;

/**
 *
 * @author Jiri Rechtacek, Ondrej Vrabec
 */
public class NetworkSettingsTest extends TestCase {

    private static ProxySelector defaultPS;

    public NetworkSettingsTest(String name) {
        super(name);
    }

    @Override
    public void setUp() {
        if (defaultPS == null) {
            defaultPS = ProxySelector.getDefault();
        }
        ProxySelector ps = new ProxySelector() {

            @Override
            public List<Proxy> select(URI uri) {
                if (uri == null) {
                    return Collections.singletonList(Proxy.NO_PROXY);
                }
                if (uri.toString().equals("http://localhost")) {
                    return Collections.singletonList(Proxy.NO_PROXY);
                } else if (uri.toString().startsWith("http://inner")) {
                    return Collections.singletonList(Proxy.NO_PROXY);
                } else {
                    return Collections.singletonList(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("corpcache.cache", 1234)));
                }
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        ProxySelector.setDefault(ps);
        MockServices.setServices(MyProxyCredentialsProvider.class);
    }

    @Override
    public void tearDown() {
        ProxySelector.setDefault(defaultPS);
    }

    public void testGetProxyForLocalhost() throws URISyntaxException {
        URI u = new URI("http://localhost");
        assertNull("NetworkSettings.getProxyHost() returns null for " + u, NetworkSettings.getProxyHost(u));
        assertNull("NetworkSettings.getProxyPort() returns null for " + u, NetworkSettings.getProxyPort(u));
    }

    public void testGetProxyForRemote() throws URISyntaxException {
        URI u = new URI("http://remote.org");
        assertEquals("Check NetworkSettings.getProxyHost() for " + u, "corpcache.cache", NetworkSettings.getProxyHost(u));
        assertEquals("Check NetworkSettings.getProxyPort() for " + u, "1234", NetworkSettings.getProxyPort(u));
    }

    public void testGetProxyForIntra() throws URISyntaxException {
        URI u = new URI("http://inner.private.web");
        assertNull("NetworkSettings.getProxyHost() returns null for " + u, NetworkSettings.getProxyHost(u));
        assertNull("NetworkSettings.getProxyPort() returns null for " + u, NetworkSettings.getProxyPort(u));
    }

    public void testIsAuthenticationDialogNotSuppressed() throws Exception {
        final boolean[] suppressed = new boolean[1];
        Authenticator.setDefault(new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                suppressed[0] = NetworkSettings.isAuthenticationDialogSuppressed();
                return super.getPasswordAuthentication();
            }
        });

        Authenticator.requestPasswordAuthentication("wher.ev.er", Inet4Address.getByName("1.2.3.4"), 1234, "http", null, "http");
        assertFalse(suppressed[0]);
    }

    public void testIsAuthenticationDialogSuppressed() throws Exception {
        final boolean[] suppressed = new boolean[1];
        Authenticator.setDefault(new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                suppressed[0] = NetworkSettings.isAuthenticationDialogSuppressed();
                return super.getPasswordAuthentication();
            }
        });

        Callable<Void> callable = new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Authenticator.requestPasswordAuthentication("wher.ev.er", Inet4Address.getByName("1.2.3.4"), 1234, "http", null, "http");
                return null;
            }
        };
        NetworkSettings.suppressAuthenticationDialog(callable);
        assertTrue(suppressed[0]);
    }

    @SuppressWarnings("SleepWhileInLoop")
    public void testIsAuthenticationDialogSuppressedExclusive() throws InterruptedException, UnknownHostException {
        final boolean[] suppressed = new boolean[1];
        Authenticator.setDefault(new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                suppressed[0] = NetworkSettings.isAuthenticationDialogSuppressed();
                return super.getPasswordAuthentication();
            }
        });

        final CountDownLatch doneSignal1 = new CountDownLatch(1);
        final CountDownLatch doneSignal2 = new CountDownLatch(1);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Callable<Void> callable = new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {
                        doneSignal1.countDown();
                        doneSignal2.await();
                        Authenticator.requestPasswordAuthentication("wher.ev.er", Inet4Address.getByName("1.2.3.4"), 1234, "http", null, "http");
                        return null;
                    }
                };
                try {
                    NetworkSettings.suppressAuthenticationDialog(callable);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        t.start();
        doneSignal1.await();
        Authenticator.requestPasswordAuthentication("wher.ev.er", Inet4Address.getByName("1.2.3.4"), 1234, "http", null, "http");
        assertFalse(suppressed[0]);
        doneSignal2.countDown();
        t.join();
        assertTrue(suppressed[0]);
    }
    
    public void testNoProxyCredentialsProviderFound() throws URISyntaxException {
        MockServices.setServices();
        URI localURI = new URI("http://localhost");
        assertNull("NetworkSettings.getProxyHost() returns null for " + localURI, NetworkSettings.getProxyHost(localURI));
        assertNull("NetworkSettings.getProxyPort() returns null for " + localURI, NetworkSettings.getProxyPort(localURI));
        URI remoteURI = new URI("http://remove.org");
        assertNull("NetworkSettings.getProxyHost() returns null for " + remoteURI, NetworkSettings.getProxyHost(localURI));
        assertNull("NetworkSettings.getProxyHost() returns null for " + remoteURI, NetworkSettings.getProxyHost(localURI));
        URI intraURI = new URI("http://inner.private.web");
        assertNull("NetworkSettings.getProxyHost() returns null for " + intraURI, NetworkSettings.getProxyHost(intraURI));
        assertNull("NetworkSettings.getProxyPort() returns null for " + intraURI, NetworkSettings.getProxyPort(intraURI));
    }
    
    public static class MyProxyCredentialsProvider extends ProxyCredentialsProvider {

        @Override
        protected String getProxyUserName(URI u) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected char[] getProxyPassword(URI u) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected boolean isProxyAuthentication(URI u) {
            return false;
        }

        @Override
        protected String getProxyHost(URI u) {
            InetSocketAddress sa = analyzeProxy(u);
            return sa == null ? null : sa.getHostName();
        }

        @Override
        protected String getProxyPort(URI u) {
            InetSocketAddress sa = analyzeProxy(u);
            return sa == null ? null : Integer.toString(sa.getPort());
        }
    }

    private static InetSocketAddress analyzeProxy(URI uri) {
        Parameters.notNull("uri", uri);
        List<Proxy> proxies = ProxySelector.getDefault().select(uri);
        assert proxies != null : "ProxySelector cannot return null for " + uri;
        assert !proxies.isEmpty() : "ProxySelector cannot return empty list for " + uri;
        Proxy p = proxies.get(0);
        if (Proxy.Type.DIRECT == p.type()) {
            // return null for DIRECT proxy
            return null;
        } else {
            if (p.address() instanceof InetSocketAddress) {
                // check is
                //assert ! ((InetSocketAddress) p.address()).isUnresolved() : p.address() + " must be resolved address.";
                return (InetSocketAddress) p.address();
            } else {
                return null;
            }
        }
    }
}