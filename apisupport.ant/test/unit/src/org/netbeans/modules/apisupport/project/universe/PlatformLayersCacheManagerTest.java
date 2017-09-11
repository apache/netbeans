/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.Log;
import org.netbeans.modules.apisupport.project.TestBase;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;

/**
 *
 * @author uadmin
 */
public class PlatformLayersCacheManagerTest extends TestBase {
    private File cacheDir;
    private NbPlatform plaf;
    private HashSet<String> jarNames;
    private File[] clusters;

    public PlatformLayersCacheManagerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {

        super.setUp();
        cacheDir = new File(Places.getCacheDirectory(), PlatformLayersCacheManager.CACHE_PATH);
        assertFalse("Cache not yet saved", cacheDir.isDirectory());
        plaf = NbPlatform.getDefaultPlatform();
        jarNames = new HashSet<String>();
        Collections.addAll(jarNames, "org-netbeans-modules-apisupport-project.jar",
                "org-netbeans-core-windows.jar",
                "org-openide-filesystems.jar",  // not in "modules" dir, but has layer.xml
                "org-openide-util.jar");    // doesn't have layer.xml
        clusters = plaf.getDestDir().listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return (pathname.getName().startsWith("platform")
                        || pathname.getName().startsWith("apisupport"))
                        && ClusterUtils.isValidCluster(pathname);
            }
        });
        PlatformLayersCacheManager.reset();
    }

    public void testGetCache() throws Exception {
        Collection<FileSystem> cache = PlatformLayersCacheManager.getCache(clusters, new FileFilter() {

            public boolean accept(File pathname) {
                return jarNames.contains(pathname.getName());
            }
        });
        assertNotNull(cache);
        assertEquals("3 of 4 cached JAR-s have layer.xml", 3, cache.size());
        assertNotNull("Pending storing cache to userdir", PlatformLayersCacheManager.storeTask);
        assertTrue("Cache successfully stored to disk", PlatformLayersCacheManager.storeTask.waitFinished(10000));
        assertTrue("Cache exists on disk", (new File(cacheDir, "index.ser")).exists());
        assertEquals("JAR-s from two different clusters", 2,
                cacheDir.list(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.startsWith("cache");
            }
        }).length);
    }

    public void testFindOriginatingJar() throws Exception {
        Collection<FileSystem> cache = PlatformLayersCacheManager.getCache(clusters, new FileFilter() {

            public boolean accept(File pathname) {
                return jarNames.contains(pathname.getName());
            }
        });
        assertNotNull(cache);
        HashSet<String> foundJars = new HashSet<String>();
        for (FileSystem fs : cache) {
            File origJar = PlatformLayersCacheManager.findOriginatingJar(fs);
            assertTrue("Originating JAR exists", origJar.exists());
            foundJars.add(origJar.getName());
        }
        HashSet<String> expectedJars = new HashSet<String>(jarNames);
        expectedJars.remove("org-openide-util.jar");    // doesn't have layer file
        assertEquals(expectedJars, foundJars);
        assertNull("Null on not cached FS", PlatformLayersCacheManager.findOriginatingJar(FileUtil.createMemoryFileSystem()));
    }


    // XXX cannot be run in binary dist, requires sources; test against fake platform
    public void testNextQueryDoesntStoreAnything() throws Exception {
        final Logger LOG = Logger.getLogger(PlatformLayersCacheManager.class.getName());
        CharSequence logsCS = Log.enable(LOG.getName(), Level.FINE);

        String[] order = new String[] {
            "getCache for clusters:", "Will store caches", "Storing caches in background", "Stored 2 modified caches",
            "getCache for clusters:", "Nothing to store"
        };

        Collection<FileSystem> cache = PlatformLayersCacheManager.getCache(clusters, null);
        assertTrue(cache.size() > 0);
        assertTrue("Cache successfully stored to disk", PlatformLayersCacheManager.storeTask.waitFinished(3000));
        cache = PlatformLayersCacheManager.getCache(clusters, null);
        assertTrue(cache.size() > 0);
        String logs = logsCS.toString();
        int index = 0;
        for (int i = 0; i < order.length; i++) {
            String msg = order[i];
            index = logs.indexOf(msg, index);
            assertTrue("Message #" + i + " (" + msg + ") found in correct order.", index != -1);
            index++;
        }
    }

    // XXX cannot be run in binary dist, requires sources; test against fake platform
    public void testLoadCacheFromDisk() throws Exception {
        testGetCache(); // to create cache on disk in current work dir
        PlatformLayersCacheManager.reset();
        final Logger LOG = Logger.getLogger(PlatformLayersCacheManager.class.getName());
        CharSequence logsCS = Log.enable(LOG.getName(), Level.FINE);

        String[] order = new String[] {
            "getCache for clusters:",
            "Cache for cluster ", /*+ clusterDir +*/ " successfully loaded from cache file",
            "Cache for cluster ", /*+ clusterDir +*/ " successfully loaded from cache file",
            "Nothing to store"
        };

            Collection<FileSystem> cache = PlatformLayersCacheManager.getCache(clusters, new FileFilter() {

            public boolean accept(File pathname) {
                return jarNames.contains(pathname.getName());
            }
        });
        assertTrue(cache.size() > 0);
        assertTrue("Cache successfully stored to disk", PlatformLayersCacheManager.storeTask.waitFinished(3000));
        cache = PlatformLayersCacheManager.getCache(clusters, null);
        assertTrue(cache.size() > 0);
        String logs = logsCS.toString();
        int index = 0;
        for (int i = 0; i < order.length; i++) {
            String msg = order[i];
            index = logs.indexOf(msg, index);
            assertTrue("Message #" + i + " (" + msg + ") found in correct order.", index != -1);
            index++;
        }
        assertTrue("Not creating any cache", logs.indexOf(" successfully created.") == -1);
        index = logs.indexOf(" failed due to modifications in ");
        if (index != -1) {
            index += " failed due to modifications in ".length();
            String file = logs.substring(index, logs.indexOf("\n", index));
            fail("No files should be modified, but '" + file + "' was.");
        }
    }

//    @Override
//    protected Level logLevel() {
//        return Level.FINE;
//    }
//
//    @Override
//    protected int timeOut() {
//        return 2000000;
//    }
//
//  // XXX cannot be run in binary dist, requires sources; test against fake platform
//  XXX cancellation of storing of caches not implemented (yet): public void testStoringCacheDoesntBlockQueries() throws Exception {
//        final Logger LOG = Logger.getLogger(PlatformLayersCacheManager.class.getName());
//        Logger observer = Logger.getLogger("observer");
//        Log.enable(LOG.getName(), Level.ALL);
//
//        String mt = "THREAD: Test Watch Dog: testStoringCacheDoesntBlockQueries MSG:";
//        String wt = "THREAD: worker MSG:";
//        String rpt = "THREAD: " + PlatformLayersCacheManager.class.getName() + " MSG:";
//        String order =
//            wt + "getCache for clusters:.*" +
//            wt + "Will store caches" +
//            rpt + "Storing caches in background" +
//            rpt + "Stored 2 modified caches" +
//            mt + "getCache for clusters:.*" +
//            mt + "Nothing to store";
//        Log.controlFlow(LOG, observer, order, 0);
//        Thread t = new Thread("worker") {
//
//            @Override
//            public void run() {
//                try {
//                    Collection<FileSystem> cache = PlatformLayersCacheManager.getCache(clusters, null);
//                    assertTrue(cache.size() > 0);
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                    fail(ex.getLocalizedMessage());
//                }
//            }
//        };
//        t.start();
//        Collection<FileSystem> cache = PlatformLayersCacheManager.getCache(clusters, null);
//        assertTrue(cache.size() > 0);
//        t.join();
//    }
}