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
