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

package org.netbeans.modules.ide.ergonomics;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 * Verifies that modules outside of platform,ide,nb and ergonomics clusters
 * are not initialized - e.g. their manifests are not parsed. This is done
 * by a "logging" contract from ModuleManager caches. As soon as module
 * manifest is loaded, the test verifies that the file is in one of 
 * allowed clusters.
 */
public class CachingPreventsLoadingOfModuleManifestsTest extends NbTestCase {
    static {
        System.setProperty("java.util.logging.config.class", CaptureLog.class.getName());
    }
    private static final Logger LOG;
    static {
        LOG = Logger.getLogger(CachingPreventsLoadingOfModuleManifestsTest.class.getName());
        CaptureLog.assertCalled();
    }

    public CachingPreventsLoadingOfModuleManifestsTest(String name) {
        super(name);
    }
    
    public static Test suite() throws IOException {
        NbModuleSuite.Configuration base = NbModuleSuite.createConfiguration(
                CachingPreventsLoadingOfModuleManifestsTest.class
            ).
            gui(false).
            clusters("ergonomics.*").
            clusters("(?!extra).*").
            enableModules("(ide|agentic)[0-9]*", ".*").
            enableClasspathModules(false).
            honorAutoloadEager(true);
        
        System.setProperty("counting.off", "false");
        System.setProperty("no.stacks", "true");
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(base.reuseUserDir(false).addTest("testInitUserDir").suite());

        suite.addTest(new CachingPreventsLoadingOfModuleManifestsTest("testInMiddle"));

        suite.addTest(
            base.reuseUserDir(true).addTest("testEnabledWindows").suite()
        );
        suite.addTest(new CachingPreventsLoadingOfModuleManifestsTest("testDontLoadManifests"));
        suite.addTest(new CachingPreventsLoadingOfModuleManifestsTest("testCaptureAgenticManifestLoad"));
        
        return suite;
    }

    public void testInitUserDir() throws Exception {
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        try {
            Class<?> c = Class.forName("javax.help.HelpSet", true, l);
        } catch (ClassNotFoundException ex) {
            LOG.log(Level.FINE, "Can't pre-load JavaHelp", ex);
        }
        FileObject fo = FileUtil.getConfigFile("Services/Browsers");
        if (fo != null) {
            fo.delete();
        }
        assertEnabled("org.netbeans.core.windows");
        System.setProperty("counting.off", "true");
    }

    public void testInMiddle() throws IOException {
        String p = System.getProperty("manifestParsing");
        assertNotNull("Parsing of manifests during first run is natural", p);
        System.getProperties().remove("manifestParsing");
        System.getProperties().remove("agenticDataInitialized");
        System.setProperty("no.stacks", "false");
        System.setProperty("counting.off", "false");
    }

    public void testEnabledWindows() throws Exception {
        assertEnabled("org.netbeans.core.windows");
    }
    
    public void testDontLoadManifests() {
        String p = System.getProperty("manifestParsing");
        if (p != null) {
            fail("No manifest parsing should happen:\n" + p);
        }
        String dirs = System.getProperty("netbeans.dirs", "");
        boolean agenticPresent = Arrays.stream(dirs.split(File.pathSeparator))
                .anyMatch(dir -> new File(dir).getName().matches("agentic[0-9]*"));
        if (agenticPresent) {
            assertNotNull("Agentic module data was initialized during the second startup",
                    System.getProperty("agenticDataInitialized"));
        }
    }

    public void testCaptureAgenticManifestLoad() {
        LogRecord record = new LogRecord(Level.FINE, "loading manifest of {0}");
        record.setParameters(new Object[]{new File("agentic/modules/test.jar")});
        System.getProperties().remove("manifestParsing");
        CaptureLog capture = new CaptureLog();
        try {
            capture.publish(record);
            assertNotNull("Agentic manifest loads must be detected", System.getProperty("manifestParsing"));
        } finally {
            Logger.getLogger("org.netbeans.core.modules").removeHandler(capture);
            System.getProperties().remove("manifestParsing");
        }
    }

    private static void assertEnabled(String cnb) {
        for (ModuleInfo mi : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (mi.getCodeNameBase().equals(cnb)) {
                assertTrue("Is enabled", mi.isEnabled());
                return;
            }
        }
        fail("Not found " + cnb);
    }
    
    public static final class CaptureLog extends Handler {
        private static Logger watchOver = Logger.getLogger("org.netbeans.core.modules");
        private static void assertCalled() {
            assertEquals("OK", System.getProperty("CaptureLog"));
        }

        public CaptureLog() {
            System.setProperty("CaptureLog", "OK");
            close();
        }
        
        @Override
        public void publish(LogRecord record) {
            if (Boolean.getBoolean("counting.off")) {
                return;
            }
            final String m = record.getMessage();
            if (m == null || !(m.startsWith("Initialize data") || m.startsWith("loading manifest of"))) {
                return;
            }
            {
                Object[] params = record.getParameters();
                assertNotNull("There are parameters", params);
                assertEquals("There is just one parameter: " + Arrays.toString(params), 1, params.length);
                if (params[0] == null) {
                    // fixed modules are OK
                    return;
                }
                File module = (File) params[0];
                boolean agentic = module.getParentFile() != null
                        && module.getParentFile().getParentFile() != null
                        && module.getParentFile().getParentFile().getName().matches("agentic[0-9]*");
                if (agentic && m.startsWith("Initialize data")) {
                    System.setProperty("agenticDataInitialized", "true");
                    // Enabled agentic modules initialize data at startup; this may use the cache.
                    return;
                }
                if ((!agentic && m.startsWith("loading manifest of")) || isPlatformOrIde(module)) {
                    return;
                }

                String prev = System.getProperty("manifestParsing");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                if (prev != null) {
                    pw.append(prev).append("\n");
                }
                final String msg = m + ": " + params[0];
                if (Boolean.getBoolean("no.stacks")) {
                    pw.print(msg);
                } else { 
                    new Exception(msg).printStackTrace(pw);
                }
                pw.flush();
                System.setProperty("manifestParsing", sw.toString());
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
            watchOver.addHandler(this);
            setLevel(Level.FINE);
            watchOver.setLevel(Level.FINE);
            
            Logger.getLogger("org.netbeans.core.modules").setLevel(Level.ALL);
        }

        private boolean isPlatformOrIde(File file) {
            String path = file.getPath();
            final String platform = System.getProperty("netbeans.home");
            if (platform != null && path.startsWith(platform)) {
                return true;
            }
            final String dirs = System.getProperty("netbeans.dirs");
            if (dirs != null) {
                for (String s : dirs.split(File.pathSeparator)) {
                    if (s.endsWith(File.separator + "ide")) {
                        if (path.startsWith(s)) {
                            return true;
                        }
                    }
                    if (s.endsWith(File.separator + "ergonomics")) {
                        if (path.startsWith(s)) {
                            return true;
                        }
                    }
                    if (s.endsWith(File.separator + "nb")) {
                        if (path.startsWith(s)) {
                            return true;
                        }
                    }
                    if (s.endsWith(File.separator + "webcommon")) {
                        if (path.startsWith(s)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }
}
