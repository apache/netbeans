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
