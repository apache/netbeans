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

package org.connector;

import java.net.InetSocketAddress;
import java.net.Proxy;
import org.MyTestCase;
import org.netbeans.installer.downloader.connector.MyProxy;
import org.netbeans.installer.downloader.connector.MyProxyType;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.xml.DomUtil;
import org.netbeans.installer.utils.xml.visitors.DomVisitor;
import org.netbeans.installer.utils.xml.visitors.RecursiveDomVisitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Danila_Dugurov
 */
public class ProxyTest extends MyTestCase {
    
    public void testProxyCreation() {
        final Proxy real = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 4321));
        MyProxy proxy = new MyProxy(real);
        assertEquals(real, proxy.getProxy());
        
        proxy = new MyProxy();
        assertEquals(Proxy.NO_PROXY, proxy.getProxy());
    }
    
    public void testProxySerializing() {
        final MyProxy proxy = new MyProxy(
            new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 4321)));
        final MyProxy desirealized = new MyProxy();
        try {
            Document doc = DomUtil.parseXmlFile("<proxy-test/>");
            DomUtil.addChild(doc.getDocumentElement(), proxy);
            
            DomVisitor visitor = new RecursiveDomVisitor() {
                public void visit(Element element) {
                    if ("proxy".equals(element.getNodeName())) {
                        desirealized.readXML(element);
                    } else super.visit(element);
                }
            };
            visitor.visit(doc);
        } catch(ParseException ex) {}
        assertEquals(proxy, desirealized);
    }
    
    public void testProxyEquals() {
        MyProxy proxy1 = new MyProxy();
        MyProxy proxy2 = new MyProxy();
        assertTrue(proxy1.equals(proxy2));
        assertEquals(proxy1.hashCode(), proxy2.hashCode());
        assertFalse(proxy1.equals(null));
        
        proxy1 = new MyProxy();
        proxy2 = new MyProxy(Proxy.NO_PROXY);
        assertTrue(proxy1.equals(proxy2));
        
        proxy1 = new MyProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 4321)));
        proxy2 = new MyProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 4321)));
        assertTrue(proxy1.equals(proxy2));
        assertEquals(proxy1.hashCode(), proxy2.hashCode());
        
        proxy1 = new MyProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 4321)));
        proxy2 = new MyProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 4321)));
        assertFalse(proxy1.equals(proxy2));
        assertNotSame(proxy1.hashCode(), proxy2.hashCode());
        
        proxy1 = new MyProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 4321)));
        proxy2 = new MyProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 4322)));
        assertFalse(proxy1.equals(proxy2));
        assertNotSame(proxy1.hashCode(), proxy2.hashCode());
        
        //comments: long duration due to host resolving in constructor InetSocketAddress!
        proxy1 = new MyProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("125.0.0.1", 4321)));
        proxy2 = new MyProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 4321)));
        assertFalse(proxy1.equals(proxy2));
        assertNotSame(proxy1.hashCode(), proxy2.hashCode());
        proxy1 = new MyProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("www.fake.ru", 4321)));
        proxy2 = new MyProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 4321)));
        assertFalse(proxy1.equals(proxy2));
        assertNotSame(proxy1.hashCode(), proxy2.hashCode());
    }
    
    public void testIncompatible() {
        try {
            MyProxy proxy1 = new MyProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 4321)), MyProxyType.SOCKS);
            fail();
        } catch(IllegalArgumentException ex) {
        }
         try {
            MyProxy proxy1 = new MyProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 4321)), MyProxyType.HTTP);
            fail();
        } catch(IllegalArgumentException ex) {
        }
         try {
            MyProxy proxy1 = new MyProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 4321)), MyProxyType.DIRECT);
            fail();
        } catch(IllegalArgumentException ex) {
        }
         try {
            MyProxy proxy1 = new MyProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 4321)), MyProxyType.FTP);
            fail();
        } catch(IllegalArgumentException ex) {
        }
    }
}
