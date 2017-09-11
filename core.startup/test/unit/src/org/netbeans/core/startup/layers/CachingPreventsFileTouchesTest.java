/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.core.startup.layers;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import org.openide.filesystems.LocalFileSystem;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Places;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Read access test
 * see details on http://wiki.netbeans.org/FitnessViaWhiteAndBlackList
 */
public class CachingPreventsFileTouchesTest extends NbTestCase {
    private static final Logger LOG;
    static {
        LOG = Logger.getLogger(CachingPreventsFileTouchesTest.class.getName());
    }

    private static void initCheckReadAccess() throws IOException {
        Set<String> allowedFiles = new HashSet<String>();
        CountingSecurityManager.initialize(null, CountingSecurityManager.Mode.CHECK_READ, allowedFiles);
    }
    
    public CachingPreventsFileTouchesTest(String name) {
        super(name);
    }
    
    public static Test suite() throws IOException {
        CountingSecurityManager.initialize("none", CountingSecurityManager.Mode.CHECK_READ, null);

        NbTestSuite suite = new NbTestSuite();
        {
            NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(
                CachingPreventsFileTouchesTest.class
            ).reuseUserDir(false).enableModules("platform\\d*", ".*").enableClasspathModules(false)
            .honorAutoloadEager(true);
            conf = conf.addTest("testInitUserDir").gui(false);
            suite.addTest(conf.suite());
        }

        suite.addTest(new CachingPreventsFileTouchesTest("testInMiddle"));

        {
            NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(
                CachingPreventsFileTouchesTest.class
            ).reuseUserDir(true).enableModules("platform\\d*", ".*").enableClasspathModules(false)
            .honorAutoloadEager(true);
            conf = conf.addTest("testReadAccess", "testRememberCacheDir").gui(false);
            suite.addTest(conf.suite());
        }
        
        suite.addTest(new CachingPreventsFileTouchesTest("testCachesDontUseAbsolutePaths"));
        suite.addTest(new CachingPreventsFileTouchesTest("testDontLoadManifests"));
        
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
        initCheckReadAccess();
    }

    public void testInMiddle() throws IOException {
        String p = System.getProperty("manifestParsing");
        assertNotNull("Parsing of manifests during first run is natural", p);
        System.getProperties().remove("manifestParsing");
        System.setProperty("counting.off", "false");
    }

    public void testReadAccess() throws Exception {
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        try {
            Class<?> c = Class.forName("javax.help.HelpSet", true, l);
        } catch (ClassNotFoundException ex) {
            LOG.log(Level.FINE, "Can't pre-load JavaHelp", ex);
        }
        try {
            if (CountingSecurityManager.isEnabled()) {
                CountingSecurityManager.assertCounts("No reads during startup", 0);
            } else {
                System.out.println("Initialization mode, counting is disabled");
            }
        } catch (Error e) {
            e.printStackTrace(getLog("file-reads-report.txt"));
            throw e;
        }
        assertEnabled("org.netbeans.core.windows");
    }
    
    public void testRememberCacheDir() {
        File cacheDir = Places.getCacheDirectory();
        assertTrue("It is a directory", cacheDir.isDirectory());
        System.setProperty("mycache", cacheDir.getPath());
        
        File boot = InstalledFileLocator.getDefault().locate("lib/boot.jar", "org.netbeans.bootstrap", false);
        assertNotNull("Boot.jar found", boot);
        System.setProperty("myinstall", boot.getParentFile().getParentFile().getParentFile().getPath());
    }

    public void testCachesDontUseAbsolutePaths() throws Exception {
        String cache = System.getProperty("mycache");
        String install = System.getProperty("myinstall");
        
        assertNotNull("Cache found", cache);
        assertNotNull("Install found", install);
        
        File cacheDir = new File(cache);
        assertTrue("Cache dir is dir", cacheDir.isDirectory());
        int cnt = 0;
        final File[] arr = recursiveFiles(cacheDir, new ArrayList<File>());
        Collections.shuffle(Arrays.asList(arr));
        for (File f : arr) {
            if (!f.isDirectory()) {
                System.err.println("checking " + f);
                cnt++;
                assertFileDoesNotContain(f, install);
            }
        }
        assertTrue("Some cache files found", cnt > 4);
    }
    
    private static File[] recursiveFiles(File dir, List<? super File> collect) {
        File[] arr = dir.listFiles();
        if (arr != null) {
            for (File f : arr) {
                if (f.isDirectory()) {
                    recursiveFiles(f, collect);
                } else {
                    collect.add(f);
                }
            }
        }
        return collect.toArray(new File[0]);
    }
    
    public void testDontLoadManifests() {
        String p = System.getProperty("manifestParsing");
        if (p != null) {
            fail("No manifest parsing should happen:\n" + p);
        }
    }

    private static void assertFileDoesNotContain(File file, String text) throws IOException, PropertyVetoException {
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(file.getParentFile());
        FileObject fo = lfs.findResource(file.getName());
        assertNotNull("file object for " + file + " found", fo);
        String content = fo.asText();
        if (content.contains(text)) {
            fail("File " + file + " seems to contain '" + text + "'!");
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
    
    @ServiceProvider(service = Handler.class)
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
            final String m = record.getMessage();
            if (m != null && m.contains("loading manifest")) {
                String prev = System.getProperty("manifestParsing");
                if (prev == null) {
                    prev = m;
                } else {
                    prev = prev + "\n" + m;
                }
                System.setProperty("manifestParsing", prev);
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
            
            Logger.getLogger("org.netbeans.Stamps").setLevel(Level.ALL);
        }
    }
}
