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