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
