/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.connector;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import org.MyTestCase;
import org.netbeans.installer.downloader.connector.MyProxy;
import org.netbeans.installer.downloader.connector.URLConnector;
import org.server.TestDataGenerator;
import org.server.WithServerTestCase;

/**
 *
 * @author Danila_Dugurov
 */
public class ConnectorTest_ extends WithServerTestCase {
  
  public static URL smallest;
  public static URL small;
  public static URL noResource;
  
  static {
    try {
      smallest = new URL("http://localhost:8080/" + TestDataGenerator.testFiles[0]);
      small = new URL("http://127.0.0.1:8080/" + TestDataGenerator.testFiles[1]);
      noResource = new URL("http://localhost:8080/kadabra.data");
    } catch (MalformedURLException ex) {
      ex.printStackTrace();
    }
  }
  
  public void testDirect() {
    URLConnector connector = new URLConnector(MyTestCase.testWD);
    URLConnection connection = null;
    try {
      connection = connector.establishConnection(smallest);
      assertEquals(TestDataGenerator.testFileSizes[0], connection.getContentLength());
      connection.getInputStream().close();
      connection = connector.establishConnection(small);
      assertEquals(TestDataGenerator.testFileSizes[1], connection.getContentLength());
      connection.getInputStream().close();
    } catch (IOException ex) {
      ex.printStackTrace();
      fail();
    }
    try {
      connection = connector.establishConnection(noResource);
      connection.getInputStream().close();
      fail();
    } catch (FileNotFoundException ex) {
    } catch (IOException ex) {
      ex.printStackTrace();
      fail();
    } finally {
      if (connection != null) {
        try {
          final InputStream in = connection.getInputStream();
          if (in != null) in.close();
        } catch (IOException ignored) {//skip
        }
      }
    }
  }
  
  public void testWithProxy() {
    URLConnector connector = new URLConnector(MyTestCase.testWD);
    URLConnection connection = null;
    try {
      connection = connector.establishConnection(smallest);
      connection.getInputStream().close();
    } catch (IOException ex) {
      ex.printStackTrace();
      fail();
    }
    connector.addProxy(new MyProxy(new Proxy(Proxy.Type.HTTP,new InetSocketAddress("www.fake.com", 1234))));
    connector.setUseProxy(true);
    try {
      connection = connector.establishConnection(smallest);
      connection.getInputStream().close();
      fail();//what's the matter?It's seems to me that sometimes for localhost java URLConnection just ignor proxy as argument.
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
  }
  
  public void testWithProxyWithByPassList() {
    URLConnector connector = new URLConnector(MyTestCase.testWD);
    connector.addProxy(new MyProxy(new Proxy(Proxy.Type.HTTP,new InetSocketAddress("www.fake.com", 1234))));
    connector.setUseProxy(true);
    connector.addByPassHost("127.0.0.1");
    URLConnection connection = null;
    try {
      connection = connector.establishConnection(smallest);
      connection.getInputStream().close();
      fail();
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
    try {
      connection = connector.establishConnection(small);
      connection.getInputStream().close();
    } catch (IOException ex) {
      ex.printStackTrace();
      fail();
    }
  }
}
