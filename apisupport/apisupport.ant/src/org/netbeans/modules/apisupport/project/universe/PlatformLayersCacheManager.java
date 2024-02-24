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

package org.netbeans.modules.apisupport.project.universe;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.core.startup.layers.LayerCacheManager;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.spi.LayerUtil;
import static org.netbeans.modules.apisupport.project.universe.Bundle.*;
import org.netbeans.modules.apisupport.project.universe.PlatformLayersCacheManager.PLFSCacheEntry;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.modules.Places;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Richard Michalsky
 */
public class PlatformLayersCacheManager {
    static final String CACHE_PATH = "nbplfsc";

    static class PLFSCacheEntry {
        private File jarFile;
        private long jarSize;
        private FileSystem fs;
        private boolean masked;
        private long jarTS;
        private boolean upToDate;
        private boolean ignored;
        private byte[] bytes;
        private boolean removed;

        private PLFSCacheEntry(File jarFile, long jarSize, long jarTS, boolean ignored, boolean masked, 
                FileSystem fs, byte[] bytes) {
            this.jarFile = jarFile;
            this.jarSize = jarSize;
            this.jarTS = jarTS;
            this.masked = masked;
            this.ignored = ignored;
            this.fs = fs;
            this.bytes = bytes;
        }

        boolean checkUpToDate() {
            upToDate = jarFile.exists() && jarFile.length() == jarSize && jarFile.lastModified() == jarTS;
            return upToDate;
        }

        FileSystem getFS() {
            return fs;
        }
        
        File getJar() {
            return jarFile;
        }

        private boolean isMasked() {
            return masked;
        }

        private void updateValues(PLFSCache oc) {
            jarSize = jarFile.length();
            jarTS = jarFile.lastModified();
            oc.modified = true;
            anyModified = true;
        }
    }

    static class PLFSCache {
        // jar --> cache entry map
        private Map<File, PLFSCacheEntry> allEntries = new HashMap<File, PLFSCacheEntry>();
        private boolean modified;

        private void add(PLFSCacheEntry entry) {
            allEntries.put(entry.getJar(), entry);
        }

        private Map<File, PLFSCacheEntry> getEntries() {
            return Collections.unmodifiableMap(allEntries);
        }

        private PLFSCacheEntry getEntry(File jar) {
            return allEntries.get(jar);
        }

        private boolean isModified() {
            return modified;
        }

        private void remove(PLFSCacheEntry ce) {
            allEntries.remove(ce.getJar());
        }
    }

    // <cluster root folder> --> <already loaded layer cache> mapping
    // also used for cache synchronization
    private static final Map<File, PLFSCache> loadedCaches = new HashMap<File, PLFSCache>();
    // XXX maybe some runtime cleanup of long unused caches from memory? WeakHashMap is too agile, nothing is usually left even for saving.
    // maybe just keep strong collection until caches are saved.

    // Location of cache dir in userdir; may be null when run as WebStart
    private static File cacheLocation;
    // cluster dir --> cache file index ("cache<index>.ser") mapping
    private static Map<File, Integer> cacheIndex;  
    private static boolean anyModified;
    private static final String CACHE_FILE_FMT = "cache%04d.ser";
    private static Logger LOGGER = Logger.getLogger(PlatformLayersCacheManager.class.getName());

    static {
        resetCacheLocation();
    }

    private static void resetCacheLocation() {
        cacheLocation = Places.getCacheSubdirectory(CACHE_PATH);
    }

    /**
     * Returns path to cache file for given cluster.
     * May be called with <tt>null</tt> to initialize cache index.
     * @param clusterDir Cluster dir or <tt>null</tt>
     * @return cache file  or <tt>null</tt> if cache file doesn't exist (or another problem occurs).
     * @throws java.io.IOException
     */
    private static File findCacheFile(File clusterDir) throws IOException {
        // XXX last access timestamp and cleaning of long unused cache files? If cache gets too big...
        if (cacheIndex == null) {
            cacheIndex = new LinkedHashMap<File, Integer>();
            ObjectInputStream ois = null;
            try {
                File indexF = new File(cacheLocation, "index.ser");
                if (indexF.exists()) {
                    ois = new ObjectInputStream(new FileInputStream(indexF));
                    int version = ois.readInt();
                    assert version == 1;
                    int count = ois.readInt();
                    for (int c = 0; c < count; c++) {
                        String clusterPath = (String) ois.readObject();
                        Date lastAccess = (Date) ois.readObject(); // last access timestamp, unused so far
                        File cd = new File(clusterPath);
                        if (cd.isDirectory()) {
                            cacheIndex.put(cd, c);

                        }
                    }
                }
            } catch (FileNotFoundException ex) {
                // no cache yet, continue
            } catch (ClassNotFoundException ex2) {
                // corrupted index file, keep what we've read and skip the rest
                LOGGER.log(Level.WARNING, "Exception during loading project layers cache index file (for cluster "
                        + clusterDir + "): " + ex2.toString());
            } finally {
                if (ois != null)
                    ois.close();
            }
        }
        Integer index = cacheIndex.get(clusterDir);
        if (index == null)
            return null;
        File cf = new File(cacheLocation, String.format(CACHE_FILE_FMT, Integer.valueOf(index)));
        return cf.exists() ? cf : null;
    }

    /**
     * Clears caches from memory (not from disk).
     * Only for use from unit tests!
     */
    static void reset() {
        synchronized (loadedCaches) {
            loadedCaches.clear();
            cacheIndex = null;
            resetCacheLocation();
        }
    }
    /**
     * Returns cache for given cluster dirs.
     * Note that this call may block for some time if the cache is invalid,
     * in such case it gets rebuilt here.
     * Returned collection is ordered to handle masked ("_hidden") files correctly.
     * @param clusters List of absolute paths to cluster root folders
     * @param filter Filter for jars in clusters
     * @return Collection of cache entries, may be empty
     * (should only when something goes terribly wrong), but not null.
     */
    @Messages("MSG_scanning_layers=Scanning NetBeans Platform layers...")
    public static Collection<FileSystem> getCache(File[] clusters, FileFilter filter) throws IOException {
        List<FileSystem> entries = new ArrayList<FileSystem>();
        LOGGER.fine("getCache for clusters: " + Arrays.toString(clusters) + (filter != null ? ", FILTERED" : ""));
        synchronized (loadedCaches) {
            try (ProgressHandle handle = ProgressHandle.createHandle(MSG_scanning_layers())) {
                handle.start(clusters.length + 1);
                int c = 0;
                for (File cl : clusters) {
                    assert cl.isDirectory();
                    boolean mustUpdate = true;
                    PLFSCache oc = loadedCaches.get(cl);
                    if (oc == null) {
                        oc = loadCache(cl);
                        if (oc == null) {
                            oc = fillCache(cl);
                            cacheIndex.put(cl, cacheIndex.size());
                            mustUpdate = false;
                        }
                        loadedCaches.put(new File(cl.getAbsolutePath()), oc);   // so that weak map keys are not referenced from within oc
                    }

                    handle.progress(c++);
                    File[] jars = getClusterJars(cl);
                    for (File jar : jars) {
                        if (filter != null && !filter.accept(jar)) {
                            continue;

                        }
                        PLFSCacheEntry entry = oc.getEntry(jar);
                        if (entry == null) {
                            entry = new PLFSCacheEntry(jar, 0, 0, false, false, null, null);    // bogus entry, will get refreshed
                            oc.add(entry);
                        }
                        if (!entry.ignored) {
                            if (mustUpdate && !entry.checkUpToDate()) {
                                refreshEntry(oc, entry);
                                LOGGER.log(Level.FINE, "Loading of layer cache for cluster " + cl + " failed due to modifications in " + jar);
                            }
                            assert entry.checkUpToDate() : "entry not up-to-date even immediately after refresh()";
                            if (entry.isMasked()) // masked entries (with "_hidden" files) come first, "Not as good as following module deps but probably close enough."
                            // according to original code in LayerUtils
                            {
                                entries.add(0, entry.getFS());
                            } else {
                                entries.add(entry.getFS());
                            }
                        }
                    }
                }
                // XXX "scan all caches" bg task on apisupport project load hook?
                // getCache would wait for finishing its cache, optionally scheduling it as priority
                storeCaches();
                handle.progress(c++);
            }
        }
        return entries;
    }

    /**
     * Searches for given file system in <b>already loaded</b> cached layer FS-s and
     * if it is found, returns its JAR file.
     * @param fs One layer filesystem
     * @return Originating JAR file for give file system or <tt>null</tt> if given FS is not cached layer FS or the cache is not yet loaded
     */
    static File findOriginatingJar(FileSystem fs) {
        Parameters.notNull("fs", fs);
        synchronized (loadedCaches) {
            for (PLFSCache cache : loadedCaches.values()) {
                for (PLFSCacheEntry entry : cache.allEntries.values()) {
                    if (fs.equals(entry.getFS()))
                        return entry.getJar();
                }
            }
        }
        return null;
    }

    private static void refreshEntry(PLFSCache oc, PLFSCacheEntry ce) throws IOException {
        assert ! ce.checkUpToDate() : "refresh() called on up-to-date entry";
        if (ce.upToDate) {
            return;
        }
        File jarFile = ce.getJar();
        if (! jarFile.exists()) {
            oc.remove(ce);
            ce.removed = true;
            ce.updateValues(oc);
        }
        List<URL> urll = LayerUtil.layersOf(jarFile);
        if (urll.isEmpty()) {
            ce.ignored = true;
            ce.updateValues(oc);
            return;
        }
        LayerCacheManager man = LayerCacheManager.manager(true);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        man.store(null, urll, os);
        byte[] buf = os.toByteArray();
        ce.fs = man.load(null, ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN));
        ce.bytes = buf;
        ce.ignored = false;
        ce.updateValues(oc);
        ce.masked = false;
        Enumeration e = ce.fs.getRoot().getChildren(true);
        while (e.hasMoreElements()) {
            FileObject f = (FileObject) e.nextElement();
            if (f.getNameExt().endsWith("_hidden")) { // NOI18N
                // #63295: put it at the beginning. Not as good as following module deps but probably close enough.
                ce.masked = true;
                break;
            }
        }
    }

    private static RequestProcessor RP = new RequestProcessor(PlatformLayersCacheManager.class.getName(), 1);
    static RequestProcessor.Task storeTask; // package-private for tests

    private static void storeCaches() {
        if (! anyModified) {
            LOGGER.fine("Nothing to store");
            return;
        }
        if (storeTask == null) {
            storeTask = RP.create(new Runnable() {
                public void run() {
                    doStoreCaches();
                }
            });
        }
        LOGGER.fine("Will store caches");
        storeTask.schedule(0);
    }

    private static void doStoreCaches() {
        // store cache index
        synchronized (loadedCaches) {
            LOGGER.fine("Storing caches in background");
            ObjectOutputStream oos = null;
            File indexF = null;
            try {
                try {
                    indexF = new File(cacheLocation, "index.ser");
                    oos = new ObjectOutputStream(new FileOutputStream(indexF));
                    oos.writeInt(1);    // index version
                    oos.writeInt(cacheIndex.size());
                    Date now = Calendar.getInstance().getTime();
                    int c = 0;
                    for (File clusterDir : cacheIndex.keySet()) {
                        oos.writeObject(clusterDir.getAbsolutePath());
                        oos.writeObject(now);   // XXX ignored so far, in future maybe write last access time of loaded caches
                        PLFSCache cache = loadedCaches.get(clusterDir);
                        if (cache != null && cache.isModified()) {
                            File cf = new File(cacheLocation, String.format(CACHE_FILE_FMT, c));
                            storeCache(cache, cf);
                        }
                        c++;
                    }
                    LOGGER.fine("Stored " + c + " modified caches");
                } finally {
                    if (oos != null) {
                        oos.close();
                    }
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Saving platform layers cache index into " +  indexF
                        + " failed with exception: " + ex.getLocalizedMessage(), ex);
            }
            anyModified = false;
        }
    }

    private static void storeCache(PLFSCache cache, File cf) {
        ObjectOutputStream oos = null;
        try {
            try {
                oos = new ObjectOutputStream(new FileOutputStream(cf));

                // cache file starts with version number (int), number of entries (int) and continues with a sequence of entries in format:
                // <JAR name (no path)><JAR size><JAR timestamp><ignore JAR>[<has masked entries><binary layerFS size><serialized binary layerFS>];
                // when <ignore JAR> is true, the rest of the entry is missing
                oos.writeInt(1);    // version
                oos.writeInt(cache.getEntries().size());
                for (PLFSCacheEntry ce : cache.getEntries().values()) {
                    File jar = ce.getJar();
                    oos.writeObject(jar.getName());
                    oos.writeLong(jar.length());
                    oos.writeLong(jar.lastModified());
                    oos.writeBoolean(ce.ignored);
                    if (! ce.ignored) {
                        oos.writeBoolean(ce.isMasked());
                        oos.writeInt(ce.bytes.length);
                        oos.write(ce.bytes);
                    }
                }
            } finally {
                if (oos != null) {
                    oos.close();
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Saving platform layers cache file " +  cf
                    + " failed with exception: " + ex.getLocalizedMessage(), ex);
        }
        cache.modified = false;
    }

    private static final String[] MODULE_DIRS = { "modules", "lib", "core" };

    private static File[] getClusterJars(File clusterDir) {
        Collection<File> allJars = new ArrayList<File>();

        for (String mds : MODULE_DIRS) {
            File[] jars;
            File modulesDir = new File(clusterDir, mds);
            if (modulesDir.isDirectory()) {
                allJars.addAll(Arrays.asList(modulesDir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".jar");
                    }
                })));
            }
        }
        return allJars.toArray(new File[0]);
    }

    private static PLFSCache fillCache(File clusterDir) throws IOException {
        File[] jars = getClusterJars(clusterDir);
        PLFSCache cache = new PLFSCache();
        cache.modified = true;
        anyModified = true;
        LayerCacheManager man = LayerCacheManager.manager(true);

        JAR: for (File jar : jars) {
            ManifestManager mm = ManifestManager.getInstanceFromJAR(jar, true);
            for (String tok : mm.getRequiredTokens()) {
                if (tok.startsWith("org.openide.modules.os.")) { // NOI18N
                    // Best to exclude platform-specific modules, e.g. ide/applemenu, as they can cause confusion.
                    cache.add(new PLFSCacheEntry(jar, jar.length(), jar.lastModified(), true, false, null, null));
                    continue JAR;
                }
            }
            String layer = mm.getLayer();
            String generatedLayer = mm.getGeneratedLayer();
            if (layer == null && generatedLayer == null) {
                cache.add(new PLFSCacheEntry(jar, jar.length(), jar.lastModified(), true, false, null, null));
                continue JAR;
            }
            List<URL> urll = new ArrayList<URL>(2);
            try {
                URI juri = Utilities.toURI(jar);
                if (layer != null) {
                    urll.add(new URL("jar:" + juri + "!/" + layer));

                }
                if (generatedLayer != null) {
                    urll.add(new URL("jar:" + juri + "!/" + generatedLayer));

                }
            } catch (MalformedURLException e) {
                throw (IOException) new IOException(e.toString()).initCause(e);
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            man.store(null, urll, os);
            byte[] bytes = os.toByteArray();
            FileSystem fs = man.load(null, ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN));
            Enumeration e = fs.getRoot().getChildren(true);
            boolean isMasked = false;
            while (e.hasMoreElements()) {
                FileObject f = (FileObject) e.nextElement();
                if (f.getNameExt().endsWith("_hidden")) { // NOI18N
                    // #63295: put it at the beginning. Not as good as following module deps but probably close enough.
                    isMasked = true;
                    break;
                }
            }
            PLFSCacheEntry ce = new PLFSCacheEntry(jar, jar.length(), jar.lastModified(), false, isMasked, fs, bytes);
            cache.add(ce);
        }
        LOGGER.fine("Cache for cluster " + clusterDir + " successfully created.");
        return cache;
        // XXX if storing bytes in memory takes too much memory, store cache directly on disk (possible slowdown due to disk IO)
    }

    private static PLFSCache loadCache(File clusterDir) {
//        assert ClusterUtils.isValidCluster(clusterDir);
        PLFSCache cache = new PLFSCache();
        LayerCacheManager man = LayerCacheManager.manager(true);

        // try loading the cache
        try {
            File cacheFile = findCacheFile(clusterDir);
            if (cacheFile == null) {
                return null;
            }
            FileInputStream fis = null;
            try {
                File[] moduleDirs = new File[MODULE_DIRS.length];
                for (int i = 0; i < moduleDirs.length; i++) {
                    moduleDirs[i] = new File(clusterDir, MODULE_DIRS[i]);
                }
                fis = new FileInputStream(cacheFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                // cache file starts with version number (int), number of entries (int) and continues with a sequence of entries in format:
                // <JAR name (no path)><JAR size><JAR timestamp><ignore JAR>[<has masked entries><binary layerFS size><serialized binary layerFS>];
                // when <ignore JAR> is true, the rest of the entry is missing
                int version = ois.readInt();
                assert version == 1;
                int count = ois.readInt();
                for (int c = 0; c < count; c++) {
                    String jarName = (String) ois.readObject();
                    File jarFile = null;
                    for (File dir : moduleDirs) {
                        jarFile = new File(dir, jarName);
                        if (jarFile.exists())
                            break;
                    }
                    long jarSize = ois.readLong();
                    long jarTS = ois.readLong();
                    boolean isIgnored = ois.readBoolean();
                    boolean isMasked = false;
                    FileSystem fs = null;
                    byte[] bytes = null;
                    if (! isIgnored) {
                        isMasked = ois.readBoolean();
                        int binFSSize = ois.readInt();
                        bytes = new byte[binFSSize];
                        ois.readFully(bytes, 0, binFSSize);
                        fs = man.load(null, ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN));
                    }
                    cache.add(new PLFSCacheEntry(jarFile, jarSize, jarTS, isIgnored, isMasked, fs, bytes));
                }
            } catch (FileNotFoundException ex) {
                // cache not found
                LOGGER.log(Level.WARNING, "Exception while loading project layers cache (from file " + cacheFile.getAbsolutePath()
                        + " for cluster " + clusterDir + "): " + ex.toString());
                return null;
            } catch (ClassNotFoundException ex2) {
                // corrupted cache file, throw the cache away
                LOGGER.log(Level.WARNING, "Exception while loading project layers cache (from file " + cacheFile.getAbsolutePath()
                        + " for cluster " + clusterDir + "): " + ex2.toString());
                return null;
            } finally {
                fis.close();
            }
            LOGGER.fine("Cache for cluster " + clusterDir + " successfully loaded from cache file " + cacheFile);
        } catch (IOException ex) {
            // corrupted cache or index file, throw the cache away
            LOGGER.log(Level.WARNING, "IOException during loading project layers cache (for cluster " + clusterDir + "): " + ex.toString());
            return null;
        }
        return cache;
    }
}
