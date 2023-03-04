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

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
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
public class MyProxy implements DomExternalizable {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static final Map<Type, MyProxyType> type2Type = 
            new HashMap<Type, MyProxyType>();
    
    static {
        type2Type.put(Type.DIRECT, MyProxyType.DIRECT);
        type2Type.put(Type.HTTP, MyProxyType.HTTP);
        type2Type.put(Type.SOCKS, MyProxyType.SOCKS);
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    int port = -1;
    String host = StringUtils.EMPTY_STRING;
    MyProxyType type = MyProxyType.DIRECT;
    
    public MyProxy() {
    }
    
    public MyProxy(Proxy proxy) {
        type = type2Type.get(proxy.type());
        
        final InetSocketAddress address = (InetSocketAddress) proxy.address();
        if (address != null) {
            host = address.getHostName();
            port = address.getPort();
        }
    }
    
    public MyProxy(Proxy proxy, MyProxyType type) throws IllegalArgumentException {
        this(proxy);
        
        if (proxy.type() != type.getType()) {
            throw new IllegalArgumentException(ResourceUtils.getString(
                    MyProxy.class, 
                    ERROR_TYPES_CONFLICT_KEY, 
                    proxy.type(), 
                    type.getType()));
        }
        this.type = type;
    }
    
    public Proxy getProxy() {
        return type == MyProxyType.DIRECT ? 
            Proxy.NO_PROXY : 
            new Proxy(type.getType(), new InetSocketAddress(host, port));
    }
    
    public void readXML(Element element) {
        final DomVisitor visitor = new RecursiveDomVisitor() {
            @Override
            public void visit(Element element) {
                if (PROXY_TYPE_TAG.equals(element.getNodeName())) {
                    type = MyProxyType.valueOf(
                            element.getTextContent().trim().toUpperCase());
                } else if (PROXY_HOST_TAG.equals(element.getNodeName())) {
                    host = element.getTextContent().trim();
                } else if (PROXY_PORT_TAG.equals(element.getNodeName())) {
                    port = Integer.parseInt(element.getTextContent().trim());
                } else {
                    super.visit(element);
                }
            }
        };
        
        visitor.visit(element);
    }
    
    public Element writeXML(Document document) {
        final Element root = document.createElement(PROXY_TAG);
        DomUtil.addElement(root, PROXY_TYPE_TAG, type.toString());
        DomUtil.addElement(root, PROXY_HOST_TAG, host);
        DomUtil.addElement(root, PROXY_PORT_TAG, String.valueOf(port));
        return root;
    }
    
    @Override
    public boolean equals(Object proxy) {
        if (this == proxy) {
            return true;
        }
        
        if (proxy == null) {
            return false;
        }
        
        if (proxy instanceof MyProxy) {
            final MyProxy prox = (MyProxy) proxy;
            if (port == prox.port && type == prox.type && host.equals(prox.host)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result;
        result = (host != null ? host.hashCode() : 0);
        result = 29 * result + (type != null ? type.hashCode() : 0);
        result = 29 * result + port;
        return result;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String ERROR_TYPES_CONFLICT_KEY = 
            "MP.error.types.conflict"; // NOI18N
    
    public static final String PROXY_TAG = 
            "proxy"; // NOI18N
    
    public static final String PROXY_TYPE_TAG = 
            "proxy-type"; // NOI18N
    
    public static final String PROXY_HOST_TAG = 
            "proxy-host"; // NOI18N
    
    public static final String PROXY_PORT_TAG = 
            "proxy-port"; // NOI18N
    
    public static final String SELECTOR_PROXIES_TAG = 
            "selector-proxies"; // NOI18N
}
