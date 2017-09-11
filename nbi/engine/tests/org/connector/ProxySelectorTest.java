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
