/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.odcs.cnd.http;

import com.tasktop.c2c.server.common.service.web.ApacheHttpRestClientDelegate;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.swing.Action.NAME;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.NetworkSettings;

/**
 *
 */
public class HttpClientAdapter {

    private static final Logger LOG = Logger.getLogger(HttpClientAdapter.class.getName());

    private final HttpClient httpClient;
    private final ApacheHttpRestClientDelegate apacheDelegate;

    private final String serverUrl;

    private HttpClientAdapter(String serverUrl, HttpClient httpClient, ApacheHttpRestClientDelegate apacheDelegate) {
        this.serverUrl = serverUrl;
        this.httpClient = httpClient;
        this.apacheDelegate = apacheDelegate;

    }

    public <T> T getForObject(String url, Class<T> responseType) {
        return getForObject(url, responseType, "HTTP request");
    }

    public <T> T postForObject(String url, Class<T> responseType, Object request) {
        return postForObject(url, responseType, request, "HTTP request");
    }

    public <T> T getForObject(String url, Class<T> responseType, String requestName) {
        CndUtils.assertNonUiThread();

        ProgressHandle handle = ProgressHandle.createHandle(requestName); // NOI18N
        try {
            handle.start();
            return apacheDelegate.getForObject(url, responseType);
        } finally {
            handle.finish();
        }
    }

    public <T> T postForObject(String url, Class<T> responseType, Object request, String requestName) {
        CndUtils.assertNonUiThread();

        ProgressHandle handle = ProgressHandle.createHandle(requestName); // NOI18N
        try {
            handle.start();
            return apacheDelegate.getForObject(url, responseType, request);
        } finally {
            handle.finish();
        }
    }

    public String getServerUrl() {
        return serverUrl;
    }

//    public boolean postSimple(String url, String requestKey, String requestValue, String responseKey) {
//        String request = String.format("{\"%s\":\"%s\"}", requestKey, requestValue);
//        Object obj = postForObject(url, Object.class, request);
//        
//        
//        
//        return false;
//    }
    public static HttpClientAdapter create(String serverUrl, PasswordAuthentication pa) {
        URI uri = URI.create(serverUrl);

        Proxy proxy = null;
        String proxyHost = NetworkSettings.getProxyHost(uri);
        if (proxyHost != null && proxyHost.length() > 0) {
            List<Proxy> proxies = ProxySelector.getDefault().select(uri);
            for (Proxy p : proxies) {
                if (p.type() != Proxy.Type.DIRECT) {
                    SocketAddress addr = p.address();
                    if (addr instanceof InetSocketAddress) {
                        InetSocketAddress inet = (InetSocketAddress) addr;
                        if (proxyHost.equals(inet.getHostString())) {
                            proxy = p;
                            break;
                        }
                    }
                }
            }
        }

        ApacheHttpRestClientDelegate apacheDelegate;

        if (proxy != null) {
            String proxyPort = NetworkSettings.getProxyPort(uri);
            assert proxyPort != null;

            LOG.log(Level.FINEST, "Setting proxy: [{0}:{1},{2}]", new Object[]{proxyHost, proxyPort, uri}); //NOI18N
            String proxyUser = NetworkSettings.getAuthenticationUsername(uri);
            if (proxyUser != null) {
                char[] pwd = NetworkSettings.getAuthenticationPassword(uri);
                String proxyPassword = pwd == null ? "" : new String(pwd); //NOI18N
                apacheDelegate = new ApacheHttpRestClientDelegate(pa.getUserName(), new String(pa.getPassword()), proxy, proxyUser, proxyPassword);
            } else {
                apacheDelegate = new ApacheHttpRestClientDelegate(pa.getUserName(), new String(pa.getPassword()), proxy);
            }
        } else {
            apacheDelegate = new ApacheHttpRestClientDelegate(pa.getUserName(), new String(pa.getPassword()));
        }

        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
//        HostConfiguration hostConfiguration = new HostConfiguration();
//        hostConfiguration.setHost(base.toExternalForm());
//        httpClient.setHostConfiguration(hostConfiguration);

        return new HttpClientAdapter(serverUrl, httpClient, apacheDelegate);
    }
}
