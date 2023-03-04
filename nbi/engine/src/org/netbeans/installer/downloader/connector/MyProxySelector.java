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

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.xml.DomExternalizable;
import org.netbeans.installer.utils.xml.DomUtil;
import org.netbeans.installer.utils.xml.visitors.DomVisitor;
import org.netbeans.installer.utils.xml.visitors.RecursiveDomVisitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Danila_Dugurov
 */
public class MyProxySelector extends ProxySelector implements DomExternalizable {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private final Map<MyProxyType, MyProxy> proxies = 
            new HashMap<MyProxyType, MyProxy>();

    private transient Set<String> byPassSet = new HashSet<String>();

    public void add(MyProxy proxy) {
        proxies.put(proxy.type, proxy);
    }

    public void remove(MyProxyType type) {
        proxies.remove(type);
    }

    public MyProxy getForType(MyProxyType type) {
        return proxies.get(type);
    }

    public void addByPassHost(String host) {
        byPassSet.add(host);
    }

    public void clearByPassList() {
        byPassSet.clear();
    }

    public String[] getByPass() {
        return byPassSet.toArray(new String[0]);
    }

    public List<Proxy> select(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException(ResourceUtils.getString(
                    MyProxySelector.class, 
                    ERROR_URI_CANNOT_BE_NULL_KEY));
        }
        Proxy proxy = Proxy.NO_PROXY;
        if (byPassSet.contains(uri.getHost())) {
            return Collections.singletonList(proxy);
        }
        if (HTTP_SCHEME.equalsIgnoreCase(uri.getScheme()) || 
                HTTPS_SCHEME.equalsIgnoreCase(uri.getScheme())) {
            if (proxies.containsKey(MyProxyType.HTTP)) {
                proxy = proxies.get(MyProxyType.HTTP).getProxy();
            }
        } else if (FTP_SCHEME.equalsIgnoreCase(uri.getScheme())) {
            if (proxies.containsKey(MyProxyType.FTP)) {
                proxy = proxies.get(MyProxyType.FTP).getProxy();
            }
        } else {
            if (proxies.containsKey(MyProxyType.SOCKS)) {
                proxy = proxies.get(MyProxyType.SOCKS).getProxy();
            }
        }
        return Collections.singletonList(proxy);
    }

    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        //TODO: now very silly selector no any rerang!
    }

    public void readXML(Element element) {
        final DomVisitor visitor = new RecursiveDomVisitor() {
            @Override
            public void visit(Element element) {
                if (MyProxy.PROXY_TAG.equals(element.getNodeName())) {
                    final MyProxy proxy = new MyProxy();
                    proxy.readXML(element);
                    add(proxy);
                } else {
                    super.visit(element);
                }
            }
        };
        visitor.visit(element);
    }

    public Element writeXML(Document document) {
        final Element root = document.createElement(MyProxy.SELECTOR_PROXIES_TAG);
        for (MyProxy proxy : proxies.values()) {
            DomUtil.addChild(root, proxy);
        }
        return root;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String ERROR_URI_CANNOT_BE_NULL_KEY = 
            "MPS.error.uri.cannot.be.null"; // NOI18N
    
    public static final String HTTP_SCHEME = 
            "http"; // NOI18N
    
    public static final String HTTPS_SCHEME = 
            "https"; // NOI18N
    
    public static final String FTP_SCHEME = 
            "ftp"; // NOI18N
}
