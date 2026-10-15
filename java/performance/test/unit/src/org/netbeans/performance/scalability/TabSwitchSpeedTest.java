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

package org.netbeans.performance.scalability;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Pavel Flaška
 */
public class TabSwitchSpeedTest extends NbTestCase {

    private static final long SWITCH_LIMIT = 100; // ms
    private TopComponent[] openTC;
    
    
    public TabSwitchSpeedTest(String name) {
        super(name);
    }

    public static Test suite() {
        NbTestSuite s = new NbTestSuite();
        s.addTest(NbModuleSuite.create(TabSwitchSpeedTest.class, ".*", ".*"));
        s.addTest(NbModuleSuite.create(TabSwitchSpeedTest.class, null, ".*"));
//        s.addTest(NbModuleSuite.create(TabSwitchSpeedTest.class, "ide[0-9]*|java[0-9]*", ".*"));
        return s;
    }

    @Override
    public void setUp() throws Exception {
        FileObject root = FileUtil.toFileObject(getWorkDir());
        assertNotNull("Cannot find dir for " + getWorkDir() + " exists: " + getWorkDir().exists(), root);

        FileObject[] openFiles = new FileObject[30];
        for (int i = 0; i < openFiles.length; i++) {
            openFiles[i] = FileUtil.createData(root, "empty" + i + ".java");
        }

        openTC = new TopComponent[openFiles.length];
        for (int i = 0; i < openFiles.length; i++) {
            DataObject dobj = DataObject.find(openFiles[i]);
            final EditorCookie cookie = dobj.getLookup().lookup(EditorCookie.class);
            cookie.open();
            class Q implements Runnable {
                JEditorPane[] arr;
                
                public Q() {
                    SwingUtilities.invokeLater(this);
                }
                
                public synchronized void run() {
                    arr = cookie.getOpenedPanes();
                    if (arr == null) {
                        SwingUtilities.invokeLater(this);
                    } else {
                        notifyAll();
                    }
                }
                
                public synchronized void await() throws InterruptedException {
                    while (arr == null) {
                        wait();
                    }
                    assertNotNull(arr);
                    assertEquals("One " + arr.length, 1, arr.length);
                }
            }
            Q q = new Q();
            q.await();
            openTC[i] = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, q.arr[0]);
            assertNotNull("Component found for " + q.arr[0], openTC);
        }
    }
    
    private void waitAWT() throws Exception {
    }

    @Override
    public void tearDown() throws Exception {
        class Q implements Runnable {
            public void run() {
                for (int i = 0; i < openTC.length; i++) {
                    openTC[i].close();
                }
            }
        }
        Q q = new Q();
        SwingUtilities.invokeAndWait(q);
    }

    public void testSimpleSwitch() throws Exception {
        doSwitchTest();
    }
    /*
    public void testAllPlatform() throws Exception {
        enableModulesFromCluster(".*");
        doSwitchTest();
    }
    */
    static Map<String,Object> map;
    final void activateComponent(TopComponent tc) {
        if (map == null) {
            try {
                Object o = Class.forName("org.netbeans.performance.scalability.Calls").getDeclaredConstructor().newInstance();
                @SuppressWarnings("unchecked")
                Map<String,Object> m = (Map<String,Object>)o;
                map = m;
            } catch (Exception ex) {
                throw (AssertionFailedError)new AssertionFailedError().initCause(ex);
            }
        }
        
        map.put("requestActive", tc);
    }
    
    private void doSwitchTest() throws Exception {
        Thread.sleep(5000);
        
        class Activate implements Runnable {
            int index;
            long time;

            public void run() {
                time = System.currentTimeMillis();
                activateComponent(openTC[index]);
            }

        }
        Activate activate = new Activate();
        for (int i = openTC.length - 1; i > 0; i--) {
            activate.index = i;
            SwingUtilities.invokeLater(activate);
            Thread.sleep(SWITCH_LIMIT);
        }
        long a = System.currentTimeMillis();
        class Wait implements Runnable {
            public void run() {
            }
        }
        Wait w = new Wait();
        SwingUtilities.invokeAndWait(w);
        long time = System.currentTimeMillis() - a;
        System.err.println("Result time: " + time);
        if (time > 300) {
            fail("Failed, too long: " + time);
        }
    }
    
   private static void enableModulesFromCluster(String cluster) throws Exception {
        Pattern p = Pattern.compile(cluster);
        String dirs = System.getProperty("netbeans.dirs");
        int cnt = 0;
        for (String c : dirs.split(File.pathSeparator)) {
            if (!p.matcher(c).find()) {
                continue;
            }
            
            File cf = new File(c);
            File ud = new File(System.getProperty("netbeans.user"));
            turnModules(ud, cf);
            cnt++;
        }
        if (cnt == 0) {
            fail("Cannot find cluster " + cluster + " in " + dirs);
        }
        
        FileUtil.getConfigRoot().getFileSystem().refresh(false);
        LOOP: for (int i = 0; i < 20; i++) {
            Thread.sleep(1000);
            for (ModuleInfo info : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
                if (!info.isEnabled()) {
                    System.err.println("not enabled yet " + info);
                    continue LOOP;
                }
            }
        }
    }
    private static void turnModules(File ud, File... clusterDirs) throws IOException {
        File config = new File(new File(ud, "config"), "Modules");
        config.mkdirs();

        for (File c : clusterDirs) {
            File modulesDir = new File(new File(c, "config"), "Modules");
            for (File m : modulesDir.listFiles()) {
                String n = m.getName();
                if (n.endsWith(".xml")) {
                    n = n.substring(0, n.length() - 4);
                }
                n = n.replace('-', '.');

                String xml = asString(new FileInputStream(m), true);
                Matcher matcherEnabled = ENABLED.matcher(xml);
             //   Matcher matcherEager = EAGER.matcher(xml);

                boolean found = matcherEnabled.find();

                if (found) {
                    assert matcherEnabled.groupCount() == 1 : "Groups: " + matcherEnabled.groupCount() + " for:\n" + xml;

                    try {
                        String out = 
                            xml.substring(0, matcherEnabled.start(1)) +
                            "true" +
                            xml.substring(matcherEnabled.end(1));
                        writeModule(new File(config, m.getName()), out);
                    } catch (IllegalStateException ex) {
                        throw (IOException)new IOException("Unparsable:\n" + xml).initCause(ex);
                    }
                }
            }
        }
    }
    private static Pattern ENABLED = Pattern.compile("<param name=[\"']enabled[\"']>([^<]*)</param>", Pattern.MULTILINE);

    private static void writeModule(File file, String xml) throws IOException {
        FileOutputStream os = new FileOutputStream(file);
        os.write(xml.getBytes(StandardCharsets.UTF_8));
        os.close();
    }
    private static String asString(InputStream is, boolean close) throws IOException {
        byte[] arr = new byte[is.available()];
        int len = is.read(arr);
        if (len != arr.length) {
            throw new IOException("Not fully read: " + arr.length + " was " + len);
        }
        if (close) {
            is.close();
        }
        return new String(arr, StandardCharsets.UTF_8);
    }
}


