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

package org.netbeans;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.openide.modules.Places;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Support for optimal checking of time stamps of certain files in
 * NetBeans directory structure. 
 *
 * @author Jaroslav Tulach &lt;jaroslav.tulach@netbeans.org&gt;
 * @since 2.9
 */
public final class Stamps {
    private static final Logger LOG = Logger.getLogger(Stamps.class.getName());
    private static AtomicLong moduleJARs;
    private static File moduleNewestFile;
    private static File[] fallbackCache;
    private static boolean populated;
    private static Boolean clustersChanged;

    private Worker worker = new Worker();

    private Stamps() {
    }
    
    /** This class can be executed from command line to perform various checks
     * on installed NetBeans, however outside of running NetBeans.
     * 
     */
    static void main(String... args) {
        if (args.length == 1 && "reset".equals(args[0])) { // NOI18N
            moduleJARs = null;
            Clusters.clear();
            clustersChanged = null;
            fallbackCache = null;
            stamp(false);
            return;
        }
        if (args.length == 1 && "init".equals(args[0])) { // NOI18N
            moduleJARs = null;
            Clusters.clear();
            clustersChanged = null;
            fallbackCache = null;
            stamp(true);
            return;
        }
        if (args.length == 1 && "clear".equals(args[0])) { // NOI18N
            moduleJARs = null;
            Clusters.clear();
            clustersChanged = null;
            fallbackCache = null;
            return;
        }
    }
    private static final Stamps MODULES_JARS = new Stamps();
    /** Creates instance of stamp that checks timestamp for all files that affect
     * module classloading and related caches.
     */
    public static Stamps getModulesJARs() {
        return MODULES_JARS;
    }
    
    /** Finds out the time of last modifications of files that influnce
     * this cache. Each cached file needs to be "younger".
     * @return time in ms since epoch
     */
    public long lastModified() {
        return moduleJARs();
    }
    
    /** Checks whether a cache exists
     * 
     * @param cache name of the cache
     * @return true if the cache exists and is not out of date
     */
    public boolean exists(String cache) {
        return file(cache, null) != null;
    }
    
    /** Opens the access to cache object as a stream.
     * @param cache name of the cache
     * @return stream to read from the cache or null if the cache is not valid
     */
    public InputStream asStream(String cache) {
        ByteBuffer bb = asByteBuffer(cache, false, false);
        if (bb == null) {
            return null;
        }
        return new ByteArrayInputStream(bb.array());
    }
    
    /** Getter for mmapped buffer access to the cache.
     * @param cache the file to access
     * @return mmapped read only buffer
     */
    public MappedByteBuffer asMappedByteBuffer(String cache) {
        return (MappedByteBuffer)asByteBuffer(cache, true, true);
    }
        
    /** Returns the stamp for this caches. 
     * @return a date, each cache needs to be newer than this date
     */
   
    /** Opens the access to cache object as a stream.
     * @param cache name of the cache
     * @return stream to read from the cache or null if the cache is not valid
     */
    public ByteBuffer asByteBuffer(String cache) {
        return asByteBuffer(cache, true, false);
    }
    final File file(String cache, int[] len) {
        if (clustersChanged()) {
            return null;
        }
        
        checkPopulateCache();
        
        synchronized (this) {
            if (worker.isProcessing(cache)) {
                LOG.log(Level.FINE, "Worker processing when asking for {0}", cache); // NOI18N
                return null;
            }
        }
        return fileImpl(cache, len, moduleJARs());
    }
    
    private ByteBuffer asByteBuffer(String cache, boolean direct, boolean mmap) {
        int[] len = new int[1];
        File cacheFile = file(cache, len);
        if (cacheFile == null) {
            return null;
        }
        
        try {
            FileChannel fc = new FileInputStream(cacheFile).getChannel();
            ByteBuffer master;
            if (mmap) {
                master = fc.map(FileChannel.MapMode.READ_ONLY, 0, len[0]);
                master.order(ByteOrder.LITTLE_ENDIAN);
            } else {
                master = direct ? ByteBuffer.allocateDirect(len[0]) : ByteBuffer.allocate(len[0]);
                int red = fc.read(master);
                if (red != len[0]) {
                    LOG.warning("Read less than expected: " + red + " expected: " + len + " for " + cacheFile); // NOI18N
                    return null;
                }
                master.flip();
            }

            fc.close();
            
            return master;
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Cannot read cache " + cacheFile, ex); // NOI18N
            return null;
        }
    }
    
    /** Method for registering updates to caches.
     * @param updater the callback to start when flushing caches
     * @param cache name of the file to store the cache into
     * @param append write from scratch or append?
     */
    public void scheduleSave(Updater updater, String cache, boolean append) {
        boolean firstAdd;
        firstAdd = scheduleSaveImpl(updater, cache, append);
        LOG.log(firstAdd ? Level.FINE : Level.FINER, 
            "Scheduling save for {0} cache", cache
        );
        Clusters.scheduleSave(this);
    }
    
    final boolean scheduleSaveImpl(Updater updater, String cache, boolean append) {
        synchronized (worker) {
            return worker.addStorage(new Store(updater, cache, append));
        }
    }
    
    /** Flushes all caches.
     * @param delay the delay to wait with starting the parsing, if zero, that also means
     *   we want to wait for the end of parsing
     */
    public void flush(int delay) {
        synchronized (worker) {
            worker.start(delay);
        }
    }

    /** Waits for the worker to finish */
    public void shutdown() {
        waitFor(true);
    }
    
    public void discardCaches() {
        discardCachesImpl(moduleJARs);
    }
    
    private static void discardCachesImpl(AtomicLong al) {
        File user = Places.getUserDirectory();
        long now = System.currentTimeMillis();
        if (user != null) {
            File f = new File(user, ".lastModified");
            if (f.exists()) {
                f.setLastModified(now);
            } else {
                f.getParentFile().mkdirs();
                try {
                    f.createNewFile();
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, "Cannot create " + f, ex);
                }
            }
        }
        if (al != null) {
            al.set(now);
        }
    }
    
    final void waitFor(boolean noNotify) {
        Worker wait;
        synchronized (worker) {
            flush(0);
            wait = worker;
        }
        wait.waitFor(noNotify);
    }
    
    
    /** Computes and returns timestamp for all files that affect
     * module classloading and related caches.
     * @return
     */
    static long moduleJARs() {
        AtomicLong local = moduleJARs;
        if (local == null) {
            local = new AtomicLong();
            AtomicReference<File> newestFile = new AtomicReference<File>();
            stamp(true, local, newestFile);
            moduleJARs = local;
            moduleNewestFile = newestFile.get();
        }
        return local.longValue();
    }
    
    //
    // Implementation. As less dependecies on other NetBeans clases, as possible, please.
    // This will be called externally from a launcher.
    //

    private static AtomicLong stamp(boolean checkStampFile) {
        AtomicLong result = new AtomicLong();
        AtomicReference<File> newestFile = new AtomicReference<File>();
        stamp(checkStampFile, result, newestFile);
        return result;
    }

    private static void stamp(boolean checkStampFile, AtomicLong result, AtomicReference<File> newestFile) {
        StringBuilder sb = new StringBuilder();
        
        Set<File> processedDirs = new HashSet<File>();
        String[] relativeDirs = Clusters.relativeDirsWithHome();
        String home = System.getProperty ("netbeans.home"); // NOI18N
        if (home != null) {
            long stamp = stampForCluster (new File (home), result, newestFile, processedDirs, checkStampFile, true, null);
            sb.append(relativeDirs[0]).append('=').append(stamp).append('\n');
        }
        String[] drs = Clusters.dirs();
        for (int i = 0; i < drs.length; i++) {
            final File clusterDir = new File(drs[i]);
            long stamp = stampForCluster(clusterDir, result, newestFile, processedDirs, checkStampFile, true, null);
            if (stamp != -1) {
                sb.append("cluster.").append(relativeDirs[i + 1]).append('=').append(stamp).append('\n');
            }
        }
        File user = Places.getUserDirectory();
        if (user != null) {
            AtomicInteger crc = new AtomicInteger();
            stampForCluster(user, result, newestFile, new HashSet<File>(), false, false, crc);
            sb.append("user=").append(result.longValue()).append('\n');
            sb.append("crc=").append(crc.intValue()).append('\n');
            sb.append("locale=").append(Locale.getDefault()).append('\n');
            sb.append("branding=").append(NbBundle.getBranding()).append('\n');
            sb.append("java.version=").append(System.getProperty("java.version")).append('\n');
            sb.append("java.vm.version=").append(System.getProperty("java.vm.version")).append('\n');
            if (BaseUtilities.isWindows()) {
              /* NETBEANS-1914: On Windows (but not on Linux or MacOS), the cache directory has been
              observed to contain absolute paths to the NetBeans install directory (netbeans.home).
              This can cause errors on startup if said directory is later moved. As a workaround,
              include the netbeans.home path among the values that will cause the cache to be
              invalidated if changed. (A better solution would be to get rid of the absolute paths;
              but after some investigation, I could not figure out how to do this.) */
              sb.append("netbeans.home=").append(home == null ? "" : home).append('\n');
            }
                    
            File checkSum = new File(Places.getCacheDirectory(), "lastModified/all-checksum.txt");
            if (!compareAndUpdateFile(checkSum, sb.toString(), result)) {
                discardCachesImpl(result);
            }
        }
    }
    
    private static long stampForCluster(
        File cluster, AtomicLong result, AtomicReference<File> newestFile, Set<File> hashSet,
        boolean checkStampFile, boolean createStampFile, AtomicInteger crc
    ) {
        File stamp = new File(cluster, ".lastModified"); // NOI18N
        long time;
        if (checkStampFile && (time = stamp.lastModified()) > 0) {
            if (time > result.longValue()) {
                newestFile.set(stamp);
                result.set(time);
            }
            return time;
        }
        if (Places.getUserDirectory() != null) {
            stamp = new File(new File(Places.getCacheDirectory(), "lastModified"), clusterLocalStamp(cluster));
            if (checkStampFile && (time = stamp.lastModified()) > 0) {
                if (time > result.longValue()) {
                    newestFile.set(stamp);
                    result.set(time);
                }
                return time;
            }
        } else {
            createStampFile = false;
        }

        File configDir = new File(new File(cluster, "config"), "Modules"); // NOI18N
        File modulesDir = new File(cluster, "modules"); // NOI18N

        AtomicLong clusterResult = new AtomicLong();
        AtomicReference<File> newestInCluster = new AtomicReference<File>();
        if (highestStampForDir(configDir, newestInCluster, clusterResult, crc) && highestStampForDir(modulesDir, newestInCluster, clusterResult, crc)) {
            // ok
        } else {
            if (!cluster.isDirectory()) {
                // skip non-existing clusters`
                return -1;
            }
        }

        if (clusterResult.longValue() > result.longValue()) {
            newestFile.set(newestInCluster.get());
            result.set(clusterResult.longValue());
        }
        
        if (createStampFile) {
            try {
                stamp.getParentFile().mkdirs();
                stamp.createNewFile();
                stamp.setLastModified(clusterResult.longValue());
            } catch (IOException ex) {
                System.err.println("Cannot write timestamp to " + stamp); // NOI18N
            }
        }
        return clusterResult.longValue();
    }

    private static boolean highestStampForDir(File file, AtomicReference<File> newestFile, AtomicLong result, AtomicInteger crc) {
        if (file.getName().equals(".nbattrs")) { // NOI18N
            return true;
        }

        File[] children = file.listFiles();
        if (children == null) {
            if (crc != null) {
                crc.addAndGet(file.getName().length());
            }
            long time = file.lastModified();
            if (time > result.longValue()) {
                newestFile.set(file);
                result.set(time);
            }
            return false;
        }
        
        for (File f : children) {
            highestStampForDir(f, newestFile, result, crc);
        }
        return true;
    }
    
    private static boolean compareAndUpdateFile(File file, String content, AtomicLong result) {
        try {
            byte[] expected = content.getBytes(StandardCharsets.UTF_8);
            byte[] read = new byte[expected.length];
            FileInputStream is = null;
            boolean areCachesOK;
            boolean writeFile;
            long lastMod;
            try {
                is = new FileInputStream(file);
                int len = is.read(read);
                areCachesOK = len == read.length && is.available() == 0 && Arrays.equals(expected, read);
                writeFile = !areCachesOK;
                lastMod = file.lastModified();
            } catch (FileNotFoundException notFoundEx) {
                // ok, running for the first time, no need to invalidate the cache
                areCachesOK = true;
                writeFile = true;
                lastMod = result.get();
            } finally {
                if (is != null) {
                    is.close();
                }
            }
            if (writeFile) {
                file.getParentFile().mkdirs();
                FileOutputStream os = new FileOutputStream(file);
                os.write(expected);
                os.close();
                if (areCachesOK) {
                    file.setLastModified(lastMod);
                }
            } else {
                if (lastMod > result.get()) {
                    result.set(lastMod);
                }
            }
            return areCachesOK;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    private static void deleteCache(File cacheFile) throws IOException {
        int fileCounter = 0;
        if (cacheFile.exists()) {
            // all of this mess is here because Windows can't delete mmaped file.
            File tmpFile = new File(cacheFile.getParentFile(), cacheFile.getName() + "." + fileCounter++);
            tmpFile.delete(); // delete any leftover file from previous session
            boolean renamed = false;
            Random r = null;
            for (int i = 0; i < 10; i++) {
                renamed = cacheFile.renameTo(tmpFile); // try to rename it
                if (renamed) {
                    break;
                }
                LOG.log(Level.INFO, "cannot rename (#{0}): {1}", new Object[]{i, cacheFile}); // NOI18N
                // try harder
                System.gc();
                System.runFinalization();
                LOG.info("after GC"); // NOI18N
                if (r == null) {
                    r = new Random();
                }
                try {
                    final int ms = r.nextInt(1000) + 1;
                    Thread.sleep(ms);
                    LOG.log(Level.INFO, "Slept {0} ms", ms);
                } catch (InterruptedException ex) {
                    LOG.log(Level.INFO, "Interrupted", ex); // NOI18N
                }
            }
            if (!renamed) {
                // still delete on exit, so next start is ok
                cacheFile.deleteOnExit();
                throw new IOException("Could not delete: " + cacheFile); // NOI18N
            }
            if (!tmpFile.delete()) {
                tmpFile.deleteOnExit();
            } // delete now or later
        }
    }

    private static File findFallbackCache(String cache) {
        String fallbackCacheLocation = System.getProperty("netbeans.fallback.cache"); // NOI18N
        if ("none".equals(fallbackCacheLocation)) { // NOI18N
            return null;
        }

        if (fallbackCache == null) {
            fallbackCache = new File[0];
            if (fallbackCacheLocation != null) {
                File fallbackFile = new File(fallbackCacheLocation);
                if (fallbackFile.isDirectory()) {
                    fallbackCache = new File[]{fallbackFile};
                }
            }
            if (fallbackCache.length == 0 && Clusters.dirs().length >= 1) {
                File fallback = new File(new File(new File(Clusters.dirs()[0]), "var"), "cache"); // NOI18N
                if (fallback.isDirectory()) {
                    fallbackCache = new File[]{ fallback };
                }
            }
        }
        if (fallbackCache.length == 0) {
            return null;
        }
        return new File(fallbackCache[0], cache);
    }

    static void checkPopulateCache() {
        if (populated) {
            return;
        }
        populated = true;
        
        File cache = Places.getCacheDirectory();
        String[] children = cache.list();
        if (children != null && children.length > 0) {
            return;
        }
        InputStream is = Stamps.getModulesJARs().asStream("populate.zip"); // NOI18N
        if (is == null) {
            return;
        }
        ZipInputStream zip = null;
        FileOutputStream os = null;
        try {
            byte[] arr = new byte[4096];
            LOG.log(Level.FINE, "Found populate.zip about to extract it into {0}", cache);
            zip = new ZipInputStream(is);
            for (;;) {
                ZipEntry en = zip.getNextEntry();
                if (en == null) {
                    break;
                }
                if (en.isDirectory()) {
                    continue;
                }
                File f = new File(cache, en.getName().replace('/', File.separatorChar));
                f.getParentFile().mkdirs();
                os = new FileOutputStream(f);
                for (;;) {
                    int len = zip.read(arr);
                    if (len == -1) {
                        break;
                    }
                    os.write(arr, 0, len);
                }
                os.close();
            }
            zip.close();
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Failed to populate {0}", cache);
        }
    }
    
    private static boolean clustersChanged() {
        if (clustersChanged != null) {
            return clustersChanged;
        }
        
        final String clustersCache = "all-clusters.dat"; // NOI18N
        File f = fileImpl(clustersCache, null, -1); // no timestamp check
        if (f != null) {
            DataInputStream dis = null;
            try {
                dis = new DataInputStream(new FileInputStream(f));
                if (Clusters.compareDirs(dis)) {
                    return false;
                }
            } catch (IOException ex) {
                return clustersChanged = true;
            } finally {
                if (dis != null) {
                    try {
                        dis.close();
                    } catch (IOException ex) {
                        LOG.log(Level.INFO, null, ex);
                    }
                }
            }
        } else {
            // missing cluster file signals caches are OK, for 
            // backward compatibility
            return clustersChanged = false;
        }
        return clustersChanged = true;
    }

    private static File fileImpl(String cache, int[] len, long moduleJARs) {
        File cacheFile = new File(Places.getCacheDirectory(), cache);
        long last = cacheFile.lastModified();
        if (last <= 0) {
            LOG.log(Level.FINE, "Cache does not exist when asking for {0}", cache); // NOI18N
            cacheFile = findFallbackCache(cache);
            if (cacheFile == null || (last = cacheFile.lastModified()) <= 0) {
                return null;
            }
            LOG.log(Level.FINE, "Found fallback cache at {0}", cacheFile);
        }

        if (moduleJARs > last) {
            LOG.log(Level.FINE, "Timestamp does not pass when asking for {0}. Newest file {1}", new Object[] { cache, moduleNewestFile }); // NOI18N
            return null;
        }

        long longLen = cacheFile.length();
        if (longLen > Integer.MAX_VALUE) {
            LOG.log(Level.WARNING, "Cache file is too big: {0} bytes for {1}", new Object[]{longLen, cacheFile}); // NOI18N
            return null;
        }
        if (len != null) {
            len[0] = (int)longLen;
        }
        
        LOG.log(Level.FINE, "Cache found: {0}", cache); // NOI18N
        return cacheFile;
    }

    /** A callback interface to flush content of some cache at a suitable
     * point in time.
     */
    public static interface Updater {
        /** Callback method to allow storage of the cache to a stream.
         * If an excetion is thrown, cache is invalidated.
         * 
         * @param os the stream to write to
         * @throws IOException exception in case something goes wrong
         */
        public void flushCaches(DataOutputStream os) throws IOException;
        
        /** Callback method to notify the caller, that
         * caches are successfully written.
         */
        public void cacheReady();
    }
    
    /** Internal structure keeping info about storages.
     */
    private static final class Store extends OutputStream {
        final Updater updater;
        final String cache;
        final boolean append;
        
        OutputStream os;
        AtomicInteger delay;
        int count;
        
        public Store(Updater updater, String cache, boolean append) {
            this.updater = updater;
            this.cache = cache;
            this.append = append;
        }
        
        public boolean store(AtomicInteger delay) {
            assert os == null;
            
            File cacheDir = Places.getCacheDirectory();
            if (!cacheDir.isDirectory()) {
                LOG.log(Level.WARNING, "Nonexistent cache directory: {0}", cacheDir); // NOI18N
                return false;
            }
            File cacheFile = new File(cacheDir, cache); // NOI18N
            boolean delete = false;
            try {
                LOG.log(Level.FINE, "Cleaning cache {0}", cacheFile);
                
                if (!append) {
                    deleteCache(cacheFile);
                }
                cacheFile.getParentFile().mkdirs();

                LOG.log(Level.FINE, "Storing cache {0}", cacheFile);
                os = new FileOutputStream(cacheFile, append); //append new entries only
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(this, 1024 * 1024));
                
                this.delay = delay;
        
                updater.flushCaches(dos);
                dos.close();
                LOG.log(Level.FINE, "Done Storing cache {0}", cacheFile);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Error saving cache {0}", cacheFile);
                LOG.log(Level.INFO, ex.getMessage(), ex); // NOI18N
                delete = true;
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException ex) {
                        LOG.log(Level.WARNING, "Error closing stream for " + cacheFile, ex); // NOI18N
                    }
                    os = null;
                }
                if (delete) {
                    cacheFile.delete();
                    cacheFile.deleteOnExit();
                } else {
                    cacheFile.setLastModified(moduleJARs());
                }
            }
            return !delete;
        }

        @Override
        public void close() throws IOException {
            os.close();
        }

        @Override
        public void flush() throws IOException {
            os.flush();
        }

        @Override
        public void write(int b) throws IOException {
            os.write(b);
            count(1);
        }

        @Override
        public void write(byte[] b) throws IOException {
            os.write(b);
            count(b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            os.write(b, off, len);
            count(len);
        }
        
        private void count(int add) {
            count += add;
            if (count > 64 * 1024) {
                int wait = delay.get();
                if (wait > 0) {
                    try {
                        Thread.sleep(wait);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                count = 0;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Store other = (Store) obj;
            if (!this.updater.equals(other.updater)) {
                return false;
            }
            if (!this.cache.equals(other.cache)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 19 * hash + (this.updater != null ? this.updater.hashCode() : 0);
            hash = 19 * hash + (this.cache != null ? this.cache.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return cache;
        }
    } // end of Store
    
    private final class Worker extends Thread {
        private final LinkedList<Store> storages;
        private final HashSet<String> processing;
        private AtomicInteger delay;
        private boolean noNotify;
        
        public Worker() {
            super("Flushing caches");
            storages = new LinkedList<Stamps.Store>();
            processing = new HashSet<String>();
            setPriority(MIN_PRIORITY);
        }
        
        public synchronized void start(int time) {
            if (delay == null) {
                delay = new AtomicInteger(time);
                super.start();
            }
        }
        
        public synchronized boolean addStorage(Store s) {
            boolean addNew = true;
            processing.add(s.cache);
            for (Iterator<Stamps.Store> it = storages.iterator(); it.hasNext();) {
                Stamps.Store store = it.next();
                if (store.equals(s)) {
                    it.remove();
                    addNew = false;
                }
            }
            storages.add(s);
            return addNew;
        }
        
        @Override
        public void run() {
            int before = delay.get();
            for (int till = before; till >= 0; till -= 500) {
                try {
                    synchronized (this) {
                        wait(500);
                    }
                } catch (InterruptedException ex) {
                    LOG.log(Level.INFO, null, ex);
                }
                if (before != delay.get()) {
                    break;
                }
            }
            if (before > 512) {
                delay.compareAndSet(before, 512);
            }
            
            long time = System.currentTimeMillis();
            LOG.log(Level.FINE, "Storing caches {0}", storages);

            HashSet<Store> notify = new HashSet<Stamps.Store>();
            for (;;) {
                Store store;
                synchronized (this) {
                    store = this.storages.poll();
                    if (store == null) {
                        // ready for new round of work
                        worker = new Worker();
                        break;
                    }
                }
                if (store.store(delay)) {
                    notify.add(store);
                }
            }
            
            long much = System.currentTimeMillis() - time;
            LOG.log(Level.FINE, "Done storing caches {0}", notify);
            LOG.log(Level.FINE, "Took {0} ms", much);
            
            processing.clear();
            
            for (Stamps.Store store : notify) {
                if (!noNotify) {
                    store.updater.cacheReady();
                }
            }
            LOG.log(Level.FINE, "Notified ready {0}", notify);

        }


        final void waitFor(boolean noNotify) {
            try {
                this.noNotify = noNotify;
                delay.set(0);
                synchronized (this) {
                    notifyAll();
                }
                join();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        private boolean isProcessing(String cache) {
            return processing.contains(cache);
        }
        
    }

    static String clusterLocalStamp(File cluster) {
        return cluster.getName().replace("..", "__");
    }
    
    static String readRelativePath(DataInput dis) throws IOException {
        String index = dis.readUTF();
        if (index.isEmpty()) {
            return index;
        }
        String relative = dis.readUTF();
        if ("user".equals(index)) { // NOI18N
            return System.getProperty("netbeans.user").concat(relative); // NOI18N
        }
        if ("home".equals(index)) { // NOI18N
            return System.getProperty("netbeans.home").concat(relative); // NOI18N
        }
        if ("abs".equals(index)) { // NOI18N
            return relative;
        }
        int indx = Integer.parseInt(index);
        String[] _dirs = Clusters.dirs();
        if (indx < 0 || indx >= _dirs.length) {
            throw new IOException("Bad index " + indx + " for " + Arrays.toString(_dirs));
        }
        return _dirs[indx].concat(relative); // NOI18N
    }

    static void writeRelativePath(String path, DataOutput dos) throws IOException {
        produceRelativePath(path, dos);
    }

    private static void produceRelativePath(String path, Object out) throws IOException {
        if (path.isEmpty()) {
            if (out instanceof DataOutput) {
                DataOutput dos = (DataOutput)out;
                dos.writeUTF(path);
            }
            return;
        }
        if (testWritePath(path, System.getProperty("netbeans.user"), "user", out)) { // NOI18N
            return;
        }
        int cnt = 0;
        for (String p : Clusters.dirs()) {
            if (testWritePath(path, p, "" + cnt, out)) {
                return;
            }
            cnt++;
        }
        if (testWritePath(path, System.getProperty("netbeans.home"), "home", out)) { // NOI18N
            return;
        }
        LOG.log(Level.FINE, "Cannot find relative path for {0}", path); // NOI18N
        doWritePath("abs", path, out); // NOI18N
    }

    private static boolean testWritePath(String path, String prefix, String codeName, Object out) throws IOException {
        if (prefix == null || prefix.isEmpty()) {
            return false;
        }
        if (path.startsWith(prefix)) {
            final String relPath = path.substring(prefix.length());
            doWritePath(codeName, relPath, out);
            return true;
        }
        return false;
    }
    private static void doWritePath(String codeName, String relPath, Object out) throws IOException {
        if (out instanceof DataOutput) {
            DataOutput dos = (DataOutput) out;
            dos.writeUTF(codeName);
            dos.writeUTF(relPath);
        } else {
            @SuppressWarnings("unchecked")
            Collection<String> coll = (Collection<String>) out;
            coll.add(codeName);
            coll.add(relPath);
        }
    }

    static String findRelativePath(String file) {
        List<String> arrayList = new ArrayList<String>();
        try {
            produceRelativePath(file, arrayList);
        } catch (IOException ex) {
            return file;
        }
        return arrayList.get(1);
    }


}
