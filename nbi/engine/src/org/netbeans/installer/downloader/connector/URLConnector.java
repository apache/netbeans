/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.installer.downloader.connector;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.installer.downloader.DownloadManager;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.helper.Pair;
import org.netbeans.installer.utils.xml.DomUtil;
import org.netbeans.installer.utils.xml.visitors.DomVisitor;
import org.netbeans.installer.utils.xml.visitors.RecursiveDomVisitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Danila_Dugurov
 */

public class URLConnector {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static URLConnector instance; //Singleton

    public static synchronized URLConnector getConnector() {
        if (instance != null) {
            return instance;
        }
        
        return instance = new URLConnector(new File(
                DownloadManager.instance.getWd(), 
                SETTINGS_FILE_NAME));
    }

    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    final MyProxySelector proxySelector = new MyProxySelector();
    
    int readTimeout = MINUTE / 3;
    int connectTimeout = MINUTE / 3;
    boolean doInput = true;
    boolean doOutput = false;
    boolean useCaches = false;

    boolean useProxy = false;

    private File settingFile;

    private void addSystemProxies() {
        addProxyFrom(HTTP_PROXY_HOST_PROPERTY, 
                HTTPS_PROXY_PORT_PROPERTY, 
                MyProxyType.HTTP);
        addProxyFrom(FTP_PROXY_HOST_PROPERTY, 
                FTP_PROXY_PORT_PROPERTY, 
                MyProxyType.FTP);
        addProxyFrom(SOCKS_PROXY_HOST_PROPERTY, 
                SOCKS_PROXY_PORT_PROPERTY, 
                MyProxyType.SOCKS);
    }

    private void addDeploymentProxies() {
        addProxyFrom(DEPLOYMENT_PROXY_HTTP_HOST, 
                DEPLOYMENT_PROXY_HTTP_PORT, 
                MyProxyType.HTTP);
        addProxyFrom(DEPLOYMENT_PROXY_FTP_HOST, 
                DEPLOYMENT_PROXY_FTP_PORT, 
                MyProxyType.FTP);
        addProxyFrom(DEPLOYMENT_PROXY_SOCKS_HOST, 
                DEPLOYMENT_PROXY_SOCKS_PORT, 
                MyProxyType.SOCKS);
        
        if (DIRECT_CONNECTION_VALUE.equalsIgnoreCase(
                System.getProperty(DIRECT_CONNECTION_PROPERTY))) {
            useProxy = false;
        }
    }

    private void configureByPassList() {
        addByPassHostsFrom(DEPLOYMENT_PROXY_BYPASS_LIST);
    }

    private void addProxyFrom(
            final String hostProp, 
            final String portProp, 
            final MyProxyType type) {
        final String host = System.getProperty(hostProp);
        final String stringPort = System.getProperty(portProp);
        final int port = stringPort != null ? Integer.parseInt(stringPort) : -1;
        
        if (host != null && port != -1) {
            final Proxy socksProxy = new Proxy(
                    type.getType(), 
                    new InetSocketAddress(host, port));
            proxySelector.add(new MyProxy(socksProxy, type));
            useProxy = true;
        }
    }

    private void addByPassHostsFrom(final String byPassProp) {
        final String byPassList = System.getProperty(byPassProp);
        if (byPassList == null) {
            return;
        }
        for (String host : byPassList.split(PROXY_BYPASS_LIST_SEPARATOR)) {
            host = host.trim();
            if (!StringUtils.EMPTY_STRING.equals(host)) {
                proxySelector.addByPassHost(host);
            }
        }
    }

    public URLConnector(final File settingFile) {
        addSystemProxies();
        addDeploymentProxies();
        configureByPassList();
        this.settingFile = settingFile;
        
        if (settingFile.exists()) {
            load();
            LogManager.log(
                    "loaded configuration from file: " + settingFile); // NOI18N
        } else {
            LogManager.log("" + settingFile + // NOI18N
                    " not exist, default configuration was set"); // NOI18N
        }
    }

    private void load() {
        try {
            Document settings = DomUtil.parseXmlFile(settingFile);
            final DomVisitor visitor = new RecursiveDomVisitor() {
                @Override
                public void visit(Element elemet) {
                    if (READ_TIMEOUT_TAG.equals(elemet.getNodeName())) {
                        readTimeout = 
                                Integer.parseInt(elemet.getTextContent().trim());
                    } else if (CONNECT_TIMEOUT_TAG.equals(elemet.getNodeName())) {
                        connectTimeout = 
                                Integer.parseInt(elemet.getTextContent().trim());
                    } else if (USE_PROXY_TAG.equals(elemet.getNodeName())) {
                        useProxy = 
                                Boolean.valueOf(elemet.getTextContent().trim());
                    } else if (PROXY_TAG.equals(elemet.getNodeName())) {
                        final MyProxy proxy = new MyProxy();
                        proxy.readXML(elemet);
                        proxySelector.add(proxy);
                    } else {
                        super.visit(elemet);
                    }
                }
            };
            
            visitor.visit(settings);
        } catch (IOException e) {
            ErrorManager.notifyDebug("I/O error during connector " + // NOI18N
                    "setting loading. Default configuration was set.", e); // NOI18N
        } catch (ParseException e) {
            ErrorManager.notifyDebug("Failed to load settings: " + // NOI18N
                    "corrupted xml. Default configuration set.", e); // NOI18N
        }
    }

    public synchronized void dump() {
        try {
            final Document document = DomUtil.parseXmlFile(DEFAULT_SETTINGS_TEXT);
            final Element root = document.getDocumentElement();
            
            DomUtil.addElement(
                    root, 
                    READ_TIMEOUT_TAG, 
                    String.valueOf(readTimeout));
            DomUtil.addElement(
                    root, 
                    CONNECT_TIMEOUT_TAG, 
                    String.valueOf(connectTimeout));
            DomUtil.addElement(
                    root, 
                    USE_PROXY_TAG, 
                    String.valueOf(useProxy));
            DomUtil.addChild(
                    root, 
                    proxySelector);
            
            DomUtil.writeXmlFile(document, settingFile);
        } catch (IOException e) {
            ErrorManager.notifyDebug("I/O error. Can't " + // NOI18N
                    "dump configuration", e);
        } catch (ParseException e) {
            ErrorManager.notifyDebug("fatal error can't parse " + // NOI18N
                    "<connectSettings/>", e); // NOI18N
        }
    }

    public void addProxy(final MyProxy proxy) {
        proxySelector.add(proxy);
        dump();
    }

    public void removeProxy(final MyProxyType type) {
        proxySelector.remove(type);
        dump();
    }

    public void addByPassHost(final String host) {
        proxySelector.addByPassHost(host);
    }

    public void clearByPassList() {
        proxySelector.clearByPassList();
    }

    public String[] getByPassHosts() {
        return proxySelector.getByPass();
    }

    public void setReadTimeout(final int readTimeout) {
        if (readTimeout < 0) {
            throw new IllegalArgumentException();
        }
        this.readTimeout = readTimeout;
        dump();
    }

    public void setConnectTimeout(final int connectTimeout) {
        if (connectTimeout < 0) {
            throw new IllegalArgumentException();
        }
        this.connectTimeout = connectTimeout;
        dump();
    }

    public void setUseProxy(final boolean useProxy) {
        this.useProxy = useProxy;
        
        dump();
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public boolean getUseProxy() {
        return useProxy;
    }

    public Proxy getProxy(final MyProxyType type) {
        final MyProxy proxy = proxySelector.getForType(type);
        
        return (proxy != null) ? proxy.getProxy() : null;
    }

    public URLConnection establishConnection(
            final URL url) throws IOException {
        return establishConnection(url, new ArrayList<Pair<String, String>>(0));
    }

    public URLConnection establishConnection(
            final URL url, 
            final List<Pair<String, String>> headerFields) throws IOException {
        Proxy proxy = null;
        URI uri = null;
        try {
            proxy = useProxy ? proxySelector.select(uri = url.toURI()).get(0) : Proxy.NO_PROXY;
            final URLConnection connection = getConnectionThroughProxy(url, proxy);
            configure(connection, headerFields);
            connection.connect();
            return connection;
        } catch (URISyntaxException e) {
            ErrorManager.notifyDebug(url + " does not comply " + // NOI18N
                    "with RFC 2396 and cannot be converted to URI", e); // NOI18N
            
            return url.openConnection(Proxy.NO_PROXY);
        } catch (IOException e) {
            proxySelector.connectFailed(uri, proxy.address(), e);
            
            throw (IOException) new IOException(ResourceUtils.getString(
                    URLConnector.class, 
                    ERROR_FAILED_PROXY_KEY, 
                    proxy, 
                    url)).initCause(e);
        }
    }

    private URLConnection getConnectionThroughProxy(
            final URL url, 
            final Proxy proxy) throws IOException {
        try {
            return url.openConnection(proxy);
        } catch (SecurityException e) {
            ErrorManager.notifyDebug("No permission to connect to " + // NOI18N
                    "proxy: " + proxy, e); // NOI18N
        } catch (IllegalArgumentException e) {
            ErrorManager.notifyDebug("Proxy: " + proxy + "has wrong " + // NOI18N
                    "type.", e); // NOI18N
        } catch (UnsupportedOperationException e) {
            ErrorManager.notifyDebug(url.getProtocol() + " handler does " + // NOI18N
                    "not support openConnection through proxy.", e); // NOI18N
        }
        
        throw new IOException(ResourceUtils.getString(
                URLConnector.class, 
                ERROR_FAILED_PROXY_KEY, 
                proxy, 
                url));
    }

    private void configure(
            final URLConnection connection, 
            final List<Pair<String, String>> headerFields) {
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
        connection.setDoInput(doInput);
        connection.setDoOutput(doOutput);
        connection.setUseCaches(useCaches);
        for (Pair<String, String> pair : headerFields) {
            connection.setRequestProperty(pair.getFirst(), pair.getSecond());
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final int SECOND = 1000;
    
    public static final int MINUTE = 60 * SECOND;
    
    public static final String SETTINGS_FILE_NAME = 
            "settings.xml"; // NOI18N
    
    public static final String HTTP_PROXY_HOST_PROPERTY = 
            "http.proxyHost"; // NOI18N
    
    public static final String HTTP_PROXY_PORT_PROPERTY = 
            "http.proxyPort"; // NOI18N
    
    public static final String FTP_PROXY_HOST_PROPERTY = 
            "ftp.proxyHost"; // NOI18N
    
    public static final String FTP_PROXY_PORT_PROPERTY = 
            "ftp.proxyPort"; // NOI18N
    
    public static final String HTTPS_PROXY_HOST_PROPERTY = 
            "https.proxyHost"; // NOI18N
    
    public static final String HTTPS_PROXY_PORT_PROPERTY = 
            "https.proxyPort"; // NOI18N
    
    public static final String SOCKS_PROXY_HOST_PROPERTY = 
            "socksProxyHost"; // NOI18N
    
    public static final String SOCKS_PROXY_PORT_PROPERTY = 
            "socksProxyPort"; // NOI18N
    
    public static final String DEPLOYMENT_PROXY_HTTP_HOST = 
            "deployment.proxy.http.host"; // NOI18N
    
    public static final String DEPLOYMENT_PROXY_HTTP_PORT = 
            "deployment.proxy.http.port"; // NOI18N
    
    public static final String DEPLOYMENT_PROXY_FTP_HOST = 
            "deployment.proxy.ftp.host"; // NOI18N
    
    public static final String DEPLOYMENT_PROXY_FTP_PORT = 
            "deployment.proxy.ftp.port"; // NOI18N
    
    public static final String DEPLOYMENT_PROXY_SOCKS_HOST = 
            "deployment.proxy.socks.host"; // NOI18N
    
    public static final String DEPLOYMENT_PROXY_SOCKS_PORT = 
            "deployment.proxy.socks.port"; // NOI18N
    
    public static final String DEPLOYMENT_PROXY_BYPASS_LIST = 
            "deployment.proxy.bypass.list"; // NOI18N
    
    public static final String DIRECT_CONNECTION_PROPERTY = 
            "javaplugin.proxy.config.type"; // NOI18N
    
    public static final String DIRECT_CONNECTION_VALUE = 
            "direct"; // NOI18N
    
    public static final String PROXY_BYPASS_LIST_SEPARATOR = 
            ",|;"; // NOI18N
    
    public static final String PROXY_TAG = 
            "proxy"; // NOI18N
    
    public static final String USE_PROXY_TAG = 
            "useProxy"; // NOI18N
    
    public static final String CONNECT_TIMEOUT_TAG = 
            "connectTimeout"; // NOI18N
    
    public static final String READ_TIMEOUT_TAG = 
            "readTimeout"; // NOI18N
    
    public static final String DEFAULT_SETTINGS_TEXT = 
            "<connectSettings/>"; // NOI18N
    
    public static final String ERROR_FAILED_PROXY_KEY = 
            "UC.error.failed.proxy"; // NOI18N
}
