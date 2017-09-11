/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
        
        if (!proxy.type().equals(type.getType())) {
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
