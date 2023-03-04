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

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.FileHandler;
import org.MyTestCase;
import org.netbeans.installer.downloader.DownloadManager;
import org.netbeans.installer.downloader.connector.MyProxy;
import org.netbeans.installer.downloader.connector.MyProxyType;
import org.netbeans.installer.downloader.connector.URLConnector;
import org.util.*;

/**
 *
 * @author Danila_Dugurov
 */
public class ConnectionConfiguratorTest extends MyTestCase {
  static {
    System.setProperty("http.proxyHost", "www.potstava.danilahttp");
    System.setProperty("http.proxyPort", "1234");
    System.setProperty("socksProxyHost", "www.potstava.danilasocks");
    System.setProperty("socksProxyPort", "1234");
    System.setProperty("ftp.proxyHost", "www.potstava.danilaftp");
    System.setProperty("ftp.proxyPort", "1234");
    System.setProperty("deployment.proxy.http.host", "www.miracle.com");
    System.setProperty("deployment.proxy.http.port", "6060");
    System.setProperty("deployment.proxy.bypass.list", "sun.com,www.my.ru ,   , w3c.go.go,, 12.34.65.2  ");
  }
  Proxy httpPro;
  Proxy ftpPro;
  Proxy socksPro;
  Proxy deploymentProxy;
  
  public void setUp() throws Exception {
    super.setUp();
    httpPro  = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("www.potstava.danilahttp", 1234));
    ftpPro = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("www.potstava.danilaftp", 1234));
    socksPro = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("www.potstava.danilasocks", 1234));
    deploymentProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("www.miracle.com", 6060));
  }
  
  public void testSystemProxyCatched() {
    URLConnector connector = new URLConnector(MyTestCase.testWD);
    assertEquals(deploymentProxy, connector.getProxy(MyProxyType.HTTP));
    assertEquals(ftpPro, connector.getProxy(MyProxyType.FTP));
    assertEquals(socksPro, connector.getProxy(MyProxyType.SOCKS));
  }
  public void testByPassCatchedAndMyAdd() {
    URLConnector connector = new URLConnector(MyTestCase.testWD);
    final Set<String> expected = new HashSet<String>();
    expected.add("sun.com");
    expected.add("www.my.ru");
    expected.add("w3c.go.go");
    expected.add("12.34.65.2");
    final Set<String> list = new HashSet<String>();
    for(String str : connector.getByPassHosts()) {
      list.add(str);
    }
    assertEquals(expected, list);
    connector.addByPassHost("myNew.one");
    list.clear();
    for(String str : connector.getByPassHosts()) {
      list.add(str);
    }
    expected.add("myNew.one");
    assertEquals(expected, list);
  }
  
  public void testClearAndAddNewByPass() {
    URLConnector connector = new URLConnector(MyTestCase.testWD);
    assertTrue(connector.getByPassHosts().length > 0);
    connector.clearByPassList();
    assertTrue(connector.getByPassHosts().length == 0);
    connector.addByPassHost("test.one");
    assertEquals("test.one", connector.getByPassHosts()[0]);
    connector.addByPassHost("test.two");
    assertTrue(connector.getByPassHosts().length == 2);
  }
  
  public void testMyOwnProxyAdd() {
    URLConnector connector = new URLConnector(MyTestCase.testWD);
    MyProxy http = new MyProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("www.mpotstava.danilahttp", 1234)));
    MyProxy ftp = new MyProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("www.mpotstava.danilaftp", 1234)), MyProxyType.FTP);
    MyProxy socks = new MyProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("www.mpotstava.danilasocks", 1234)));
    connector.addProxy(http);
    connector.addProxy(ftp);
    connector.addProxy(socks);
    assertEquals(http.getProxy(), connector.getProxy(MyProxyType.HTTP));
    assertEquals(ftp.getProxy(), connector.getProxy(MyProxyType.FTP));
    assertEquals(socks.getProxy(), connector.getProxy(MyProxyType.SOCKS));
  }
  
  public void testConnectorPropertiesSetGet() {
    URLConnector connector = new URLConnector(MyTestCase.testWD);
    connector.setConnectTimeout(1000);
    assertEquals(1000, connector.getConnectTimeout());
    connector.setReadTimeout(2000);
    assertEquals(2000, connector.getReadTimeout());
    connector.setUseProxy(false);
    assertEquals(false, connector.getUseProxy());
  }
}
