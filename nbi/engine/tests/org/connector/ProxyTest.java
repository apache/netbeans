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
