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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.MyTestCase;
import org.netbeans.installer.downloader.connector.MyProxy;
import org.netbeans.installer.downloader.connector.MyProxySelector;
import org.netbeans.installer.downloader.connector.MyProxyType;

/**
 *
 * @author Danila_Dugurov
 */
public class ProxySelectorTest extends MyTestCase {
  
  URI httpURI;
  URI ftpURI;
  URI svnURI;
  public void setUp() throws Exception {
    super.setUp();
    httpURI  = new URI("http://www.fumm.off/");
    ftpURI = new URI("ftp://www.fumm.off/");
    svnURI = new URI("svn://www.fumm.off/");
  }
  
  public void testSimpleProxySelect() throws URISyntaxException {
    
    MyProxySelector selector = new MyProxySelector();
    
    
    final MyProxy proxy1 = new MyProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8080)));
    final MyProxy proxy2 = new MyProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("localhost", 8080)));
    final MyProxy proxy3 = new MyProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("localhost", 8081)), MyProxyType.FTP);
    selector.add(proxy2);
    selector.add(proxy1);
    selector.add(proxy3);
    assertEquals(1, selector.select(httpURI).size());
    assertEquals(1, selector.select(svnURI).size());
    assertEquals(1, selector.select(ftpURI).size());
    assertEquals(proxy1.getProxy(), selector.select(httpURI).get(0));
    assertEquals(proxy3.getProxy(), selector.select(ftpURI).get(0));
    assertEquals(proxy2.getProxy(), selector.select(svnURI).get(0));
  }
  
  public void testAddAndSelect() {
    MyProxySelector selector = new MyProxySelector();
    assertNull(selector.getForType(MyProxyType.HTTP));
    assertNull(selector.getForType(MyProxyType.SOCKS));
    assertNull(selector.getForType(MyProxyType.FTP));
    assertEquals(Proxy.NO_PROXY, selector.select(httpURI).get(0));
    assertEquals(Proxy.NO_PROXY, selector.select(ftpURI).get(0));
    assertEquals(Proxy.NO_PROXY, selector.select(svnURI).get(0));
    final MyProxy http = new MyProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8080)), MyProxyType.HTTP);
    final MyProxy ftp = new MyProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("hehe.kill.yourself", 8080)), MyProxyType.FTP);
    final MyProxy socks = new MyProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("localhost", 8080)), MyProxyType.SOCKS);
    selector.add(http);
    selector.add(ftp);
    selector.add(socks);
    assertNotNull(selector.getForType(MyProxyType.HTTP));
    assertNotNull(selector.getForType(MyProxyType.SOCKS));
    assertNotNull(selector.getForType(MyProxyType.FTP));
    assertEquals(http.getProxy(), selector.select(httpURI).get(0));
    assertEquals(ftp.getProxy(), selector.select(ftpURI).get(0));
    assertEquals(socks.getProxy(), selector.select(svnURI).get(0));
  }
  
  public void testRemoveReplace() {
    MyProxySelector selector = new MyProxySelector();
    final MyProxy http = new MyProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8080)));
    final MyProxy ftp = new MyProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("hehe.kill.yourself", 8080)), MyProxyType.FTP);
    final MyProxy socks = new MyProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("localhost", 8080)));
    final MyProxy socks2 = new MyProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("localhost", 8081)));
    final MyProxy noProxy = new MyProxy(Proxy.NO_PROXY);
    selector.add(http);
    selector.add(socks);
    selector.add(noProxy);
    assertEquals(socks.getProxy(), selector.select(svnURI).get(0));
    selector.add(socks2);
    assertEquals(socks2.getProxy(), selector.select(svnURI).get(0));
    selector.remove(MyProxyType.FTP);
    assertNotNull(selector.getForType(MyProxyType.HTTP));
    assertNotNull(selector.getForType(MyProxyType.SOCKS));
    assertNotNull(selector.getForType(MyProxyType.DIRECT));
    assertNull(selector.getForType(MyProxyType.FTP));
    selector.remove(MyProxyType.HTTP);
    selector.remove(MyProxyType.SOCKS);
    assertNull(selector.getForType(MyProxyType.HTTP));
    assertNull(selector.getForType(MyProxyType.SOCKS));
    assertNotNull(selector.getForType(MyProxyType.DIRECT));
    assertNull(selector.getForType(MyProxyType.FTP));
  }
  
  public void testByPassAddGet() {
    MyProxySelector selector = new MyProxySelector();
    assertTrue(selector.getByPass().length == 0);
    final Set<String> expected = new HashSet<String>();
    expected.add("sun.com");
    selector.addByPassHost("sun.com");
    assertTrue(selector.getByPass().length == 1);
    assertEquals("sun.com", selector.getByPass()[0]);
    expected.add("www.my.ru");
    expected.add("w3c.go.go");
    expected.add("12.34.65.2");
    selector.addByPassHost("www.my.ru");
    selector.addByPassHost("w3c.go.go");
    selector.addByPassHost("12.34.65.2");
    final Set<String> list = new HashSet<String>();
    for(String str : selector.getByPass()) {
      list.add(str);
    }
    assertEquals(expected, list);
  }
  
  public void testClearAndAddNewByPass() {
    MyProxySelector selector = new MyProxySelector();
    selector.addByPassHost("sun.com");
    assertTrue(selector.getByPass().length > 0);
    selector.clearByPassList();
    assertTrue(selector.getByPass().length == 0);
    selector.addByPassHost("mysun.com");
    assertEquals("mysun.com", selector.getByPass()[0]);
  }
  
  public void testSelectWithByPass() {
    MyProxySelector selector = new MyProxySelector();
    final MyProxy http = new MyProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8080)), MyProxyType.HTTP);
    selector.add(http);
    assertEquals(http.getProxy(), selector.select(httpURI).get(0));
    selector.addByPassHost(httpURI.getHost());
    assertEquals(Proxy.NO_PROXY, selector.select(httpURI).get(0));
  }
}
