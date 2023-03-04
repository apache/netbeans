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
package org.netbeans.modules.web.clientproject.cordova;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SelectionKey;
import org.junit.*;
import static org.junit.Assert.*;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cordova.platforms.spi.Device;
import org.netbeans.modules.cordova.platforms.spi.MobilePlatform;
import org.netbeans.modules.cordova.platforms.api.PlatformManager;
import org.netbeans.modules.cordova.platforms.spi.SDK;
import org.netbeans.modules.netserver.api.ProtocolDraft;
import org.netbeans.modules.netserver.websocket.WebSocketClientImpl;
import org.netbeans.modules.netserver.api.WebSocketReadHandler;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author beci
 */
public class AndroidPlatformTest extends NbTestCase {
    
    
    public AndroidPlatformTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(IFL.class);
        
    }

    public static final class IFL extends InstalledFileLocator {

        public IFL() {
        }

        @Override
        public File locate(String relativePath, String codeNameBase, boolean localized) {
            if (relativePath.equals("ant/nblib/bridge.jar")) {
                String path = System.getProperty("test.bridge.jar");
                assertNotNull("must set test.bridge.jar", path);
                return new File(path);
            } else if (relativePath.equals("ant")) {
                String path = System.getProperty("test.ant.home");
                assertNotNull("must set test.ant.home", path);
                return new File(path);
            } else if (relativePath.startsWith("ant/")) {
                String path = System.getProperty("test.ant.home");
                assertNotNull("must set test.ant.home", path);
                return new File(path, relativePath.substring(4).replace('/', File.separatorChar));
            } else {
                return null;
            }
        }
    }

    /**
     * Test of createProject method, of class AndroidPlatform.
     */
    @Test
    public void testGetAvds() throws Exception {
        MobilePlatform instance = org.netbeans.modules.cordova.platforms.api.PlatformManager.getPlatform(PlatformManager.ANDROID_TYPE);
        instance.setSdkLocation("/Users/beci/android-sdk-macosx");
        for (Device avd: instance.getVirtualDevices()) {
            System.out.println(avd);
        }
    }
    
    @Test
    public void testGetDevices() throws Exception {
        MobilePlatform instance = org.netbeans.modules.cordova.platforms.api.PlatformManager.getPlatform(PlatformManager.ANDROID_TYPE);
        instance.setSdkLocation("/Users/beci/android-sdk-macosx");
        for (Device avd: instance.getVirtualDevices()) {
            System.out.println(avd);
        }
    }
    
    @Test
    public void testGetTargets() throws Exception {
        MobilePlatform instance = org.netbeans.modules.cordova.platforms.api.PlatformManager.getPlatform(PlatformManager.ANDROID_TYPE);
        instance.setSdkLocation("/Users/beci/android-sdk-macosx");
        for (SDK target: instance.getSDKs()) {
            System.out.println(target);
        }
    }
    
    @Test
    public void testEnableDebugging() throws Exception {
        File f = File.createTempFile("tmp", "tmp");
        FileWriter fileWriter = new FileWriter(f);
        BufferedWriter buf = new BufferedWriter(fileWriter);
        buf.append("attach " + getPid());
        buf.newLine();
        buf.append("p (void *)[WebView _enableRemoteInspector]");
        buf.newLine();
        buf.append("detach");
        buf.newLine();
        buf.append("quit");
        buf.newLine();
        buf.close();
        System.out.println(f.getPath());
        final Process p = Runtime.getRuntime().exec("gdb --command=" + f.getPath());
    }
static class SyncPipe implements Runnable
{
public SyncPipe(InputStream istrm, OutputStream ostrm) {
      istrm_ = istrm;
      ostrm_ = ostrm;
  }
  public void run() {
      try
      {
          final byte[] buffer = new byte[1024];
          for (int length = 0; (length = istrm_.read(buffer)) != -1; )
          {
              ostrm_.write(buffer, 0, length);
          }

      }
      catch (Exception e)
      {
          e.printStackTrace();
      }
  }
  private static OutputStream ostrm_;
  private static InputStream istrm_;
}    
    
    private int getPid() throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("ps x").getInputStream()));

        String line;

        while (r.ready() && ((line = r.readLine()) != null)) {
            System.out.println(line);
            if (line.contains("iPhone Simulator") && line.contains("ClientSide")) {
                final String trim = line.substring(0, 6).trim();
                System.out.println(trim);
                return Integer.parseInt(trim);
            }
        }
        return -1;
    }
    
    
    
        /**
     * Test of createProject method, of class AndroidPlatform.
     */
    @Test
    public void testListSdks() throws Exception {
        MobilePlatform instance = org.netbeans.modules.cordova.platforms.api.PlatformManager.getPlatform(PlatformManager.IOS_TYPE);
        if (instance != null) {
            for (SDK sdks : instance.getSDKs()) {
                System.out.println(sdks);
            }
        }
    }
    
        /**
     * Test of createProject method, of class AndroidPlatform.
     */
    @Test
    public void testWaitEmulatorReady() throws Exception {
        MobilePlatform instance = org.netbeans.modules.cordova.platforms.api.PlatformManager.getPlatform(PlatformManager.ANDROID_TYPE);
        instance.setSdkLocation("/Users/beci/android-sdk-macosx");
        System.out.println(instance.waitEmulatorReady(10000));
    }
    
    @Test
    public void testConnectIOS() throws Exception {
        WebSocketClientImpl client = new WebSocketClientImpl(new URI("ws://[::1]:9999/devtools/page/1"), ProtocolDraft.getProtocol(76));
        WebSocketReadHandler handler = new ReadH(client);
        client.setWebSocketReadHandler(handler);
        Thread thread = new Thread(client);
        thread.run();
        Thread.sleep(2000);
        thread.stop();
        //client.sendMessage("{\"id\":5,\"method\":\"Debugger.enable\"}");
        System.out.println("nevim");
    }
    
       @Test
    public void testConnectChrome() throws Exception {
        WebSocketClientImpl client = new WebSocketClientImpl(new URI("ws://localhost:9222/devtools/page/2_2"), ProtocolDraft.getRFC());
        WebSocketReadHandler handler = new ReadH(client);
        client.setWebSocketReadHandler(handler);
        Thread thread =  new Thread( client);
        thread.start();
        System.out.println("nevim");
    }


    
    
//    /**
//     * Test of createProject method, of class AndroidPlatform.
//     */
//    @Test
//    public void testCreateProject() throws Exception {
//        File dir = new File(getWorkDir() + File.separator + "androidtest");
//        String targetId = "12";
//        String projectName = "AndroidTest";
//        String activityName = "MainActivity";
//        String packageName = "com.test";
//        AndroidPlatform instance = new AndroidPlatform();
//        instance.createProject(dir, targetId, projectName, activityName, packageName);
//        
//        File build = new File(dir.getAbsolutePath() + File.separator + "build.xml");
//        assertTrue(build.exists());
//    }
//
//    /**
//     * Test of buildProject method, of class AndroidPlatform.
//     */
//    @Test
//    public void testBuildCleanProject() throws IOException {
//        File dir = new File(getWorkDir() + File.separator + "androidtest2");
//        String targetId = "12";
//        String projectName = "AndroidTest";
//        String activityName = "MainActivity";
//        String packageName = "com.test";
//        AndroidPlatform instance = new AndroidPlatform();
//        instance.createProject(dir, targetId, projectName, activityName, packageName);
//        
//        File build = new File(dir.getAbsolutePath() + File.separator + "build.xml");
//        assertTrue(build.exists());
//        
//        instance.buildProject(dir, "debug").waitFinished();
//
//        File bin = new File(dir.getAbsolutePath() + File.separator + "bin");
//        assertTrue(bin.exists());
//    }
//
//    /**
//     * Test of cleanProject method, of class AndroidPlatform.
//     */
//    @Test
//    public void testCleanProject() throws Exception {
//        File dir = new File(getWorkDir() + File.separator + "androidtest3");
//        String targetId = "12";
//        String projectName = "AndroidTest";
//        String activityName = "MainActivity";
//        String packageName = "com.test";
//        AndroidPlatform instance = new AndroidPlatform();
//        instance.createProject(dir, targetId, projectName, activityName, packageName);
//        
//        File build = new File(dir.getAbsolutePath() + File.separator + "build.xml");
//        assertTrue(build.exists());
//        
//        instance.buildProject(dir, "debug").waitFinished();
//
//        File bin = new File(dir.getAbsolutePath() + File.separator + "bin");
//        assertTrue(bin.exists());
//        
//        instance.cleanProject(dir);
//    }

    private static class ReadH implements WebSocketReadHandler {
        private final WebSocketClientImpl client;

        private ReadH(WebSocketClientImpl client) {
            this.client = client;
        }

        @Override
        public void accepted(SelectionKey key) {
            System.out.println("accepted");
            client.sendMessage("request: { \"id\": 123, \"method\": \"Page.disable\"}");
        }

        @Override
        public void read(SelectionKey key, byte[] message, Integer dataType) {
            System.out.println("read");
            System.out.println(key);
            System.out.println(message);
            System.out.println(dataType);
        }

        @Override
        public void closed(SelectionKey key) {
            System.out.println("closed");
            System.out.println(key);
        }
    }
}
