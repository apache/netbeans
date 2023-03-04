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

package org.netbeans;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.Stamps.Updater;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class StampsTest extends NbTestCase {

    private File userdir;
    private File ide;
    private File platform;
    private File install;
    private String branding;
    private Locale locale;
    
    
    public StampsTest(String testName) {
        super(testName);
    }            
    
    
    protected File createIdeCluster(File dir) {
        return new File(dir, "ide");
    }

    @Override
    protected void setUp() throws Exception {
        branding = NbBundle.getBranding();
        locale = Locale.getDefault();
        
        clearWorkDir();
        
        install = new File(getWorkDir(), "install");
        platform = new File(install, "platform");
        ide = createIdeCluster(install);
        userdir = new File(getWorkDir(), "tmp");
        
        System.setProperty("netbeans.home", platform.getPath());
        System.setProperty("netbeans.dirs", ide.getPath());
        System.setProperty("netbeans.user", userdir.getPath());
        
        createModule("org.openide.awt", platform, 50000L);
        createModule("org.openide.nodes", platform, 60000L);
        createModule("org.netbeans.api.languages", ide, 90000L);
        createModule("org.netbeans.modules.logmanagement", userdir, 10000L);
        
        reset();
        
        Thread.sleep(100);

        Logger l = Logger.getLogger("org");
        l.setLevel(Level.OFF);
        l.setUseParentHandlers(false);
    }

    @Override
    protected void tearDown() throws Exception {
        NbBundle.setBranding(branding);
        Locale.setDefault(locale);
    }
    
    public void testEmpty() {
        Stamps.getModulesJARs().waitFor(false);
    }

    public void testStampsInvalidatedWhenConfigFileDeleted() throws Exception {
        File f = new File(new File(new File(userdir, "config"), "Modules"), "org-some.xml");
        f.getParentFile().mkdirs();
        f.createNewFile();
        assertTrue("File created", f.canRead());
        File f1 = new File(new File(new File(userdir, "config"), "Modules"), "org-some-else.xml");
        f1.getParentFile().mkdirs();
        f1.createNewFile();
        assertTrue("File created", f1.canRead());
        Thread.sleep(100);
        
        reset();

        Thread.sleep(100);
        final Stamps s = Stamps.getModulesJARs();


        assertNull(s.asByteBuffer("mycache.dat"));
        assertNull(s.asStream("mycache.dat"));

        class Up implements Stamps.Updater {

            public void flushCaches(DataOutputStream os) throws IOException {
                os.writeInt(1);
                os.writeInt(2);
                os.writeShort(2);
            }

            public void cacheReady() {
                assertNotNull("stream can be obtained", s.asStream("mycache.dat"));
            }

        }
        Up updater = new Up();

        s.scheduleSave(updater, "mycache.dat", false);

        assertNull(s.asByteBuffer("mycache.dat"));
        assertNull(s.asStream("mycache.dat"));

        s.waitFor(false);

        ByteBuffer bb;
        InputStream is;
        assertNotNull(bb = s.asByteBuffer("mycache.dat"));
        assertNotNull(is = s.asStream("mycache.dat"));

        assertEquals("10 bytes", 10, bb.remaining());
        assertEquals("10 bytes stream", 10, is.available());
        is.close();
        bb.clear();

        f.delete();
        assertFalse("File disappered", f.exists());
        Stamps.main("clear");
        Stamps p = Stamps.getModulesJARs();

        assertNull(p.asByteBuffer("mycache.dat"));
        assertNull(p.asStream("mycache.dat"));
    }

    public void testGenerateTimeStamps() {
        long stamp = Stamps.moduleJARs();
        assertEquals("Timestamp is taken from api.languages module", 90000L, stamp);
        
        assertStamp(60000L, platform, false, true);
        assertStamp(90000L, ide, false, true);
        assertStamp(-1L, userdir, false, false);
        
        reset();
        
        CountingSecurityManager.initialize(install.getPath());

        long newStamp = Stamps.moduleJARs();
        
        CountingSecurityManager.assertCounts("Just two accesses installation", 2);
        assertEquals("Stamps are the same", stamp, newStamp);
        
        
        assertStamp(60000L, platform, false, true);
        assertStamp(90000L, ide, false, true);
        assertStamp(-1L, userdir, false, false);

        reset();
        CountingSecurityManager.initialize(new File(userdir, "var").getPath());
        long newStamp2 = Stamps.moduleJARs();
        
        CountingSecurityManager.assertCounts("Just four accesses to cache", 4);
        assertEquals("Stamps are the same", stamp, newStamp2);
    }        
    
    
    public void testStampsInvalidatedWhenClustersChange() throws IOException {
        final Stamps s = Stamps.getModulesJARs();
        
        assertNull(s.asByteBuffer("mycache.dat"));
        assertNull(s.asStream("mycache.dat"));

        class Up implements Stamps.Updater {

            public void flushCaches(DataOutputStream os) throws IOException {
                os.writeInt(1);
                os.writeInt(2);
                os.writeShort(2);
            }

            public void cacheReady() {
                assertNotNull("stream can be obtained", s.asStream("mycache.dat"));
            }
            
        }
        Up updater = new Up();
        
        s.scheduleSave(updater, "mycache.dat", false);
        
        assertNull(s.asByteBuffer("mycache.dat"));
        assertNull(s.asStream("mycache.dat"));
        
        s.waitFor(false);
        
        ByteBuffer bb;
        InputStream is;
        assertNotNull(bb = s.asByteBuffer("mycache.dat"));
        assertNotNull(is = s.asStream("mycache.dat"));
        
        assertEquals("10 bytes", 10, bb.remaining());
        assertEquals("10 bytes stream", 10, is.available());
        is.close();
        bb.clear();

        System.getProperties().remove("netbeans.dirs");
        reset();
        Stamps p = Stamps.getModulesJARs();

        assertNull(p.asByteBuffer("mycache.dat"));
        assertNull(p.asStream("mycache.dat"));
    }        

    public void testWriteToCache() throws Exception {
        final Stamps s = Stamps.getModulesJARs();
        
        assertNull(s.asByteBuffer("mycache.dat"));
        assertNull(s.asStream("mycache.dat"));
        class Up implements Stamps.Updater {

            public void flushCaches(DataOutputStream os) throws IOException {
                os.writeInt(1);
                os.writeInt(2);
                os.writeShort(2);
            }

            public void cacheReady() {
                assertNotNull("stream can be obtained", s.asStream("mycache.dat"));
            }
            
        }
        Up updater = new Up();
        
        s.scheduleSave(updater, "mycache.dat", false);
        
        assertNull(s.asByteBuffer("mycache.dat"));
        assertNull(s.asStream("mycache.dat"));
        
        s.waitFor(false);
        
        ByteBuffer bb;
        InputStream is;
        assertNotNull(bb = s.asByteBuffer("mycache.dat"));
        assertNotNull(is = s.asStream("mycache.dat"));
        
        assertEquals("10 bytes", 10, bb.remaining());
        assertEquals("10 bytes stream", 10, is.available());
        is.close();
        bb.clear();

        s.discardCaches();
        
        assertNull(s.asByteBuffer("mycache.dat"));
        assertNull(s.asStream("mycache.dat"));
    }
    
    public void testAppendToCache() throws Exception {
        final Stamps s = Stamps.getModulesJARs();
        
        assertNull(s.asByteBuffer("mycache.dat"));
        assertNull(s.asStream("mycache.dat"));

        class Up implements Stamps.Updater {

            public void flushCaches(DataOutputStream os) throws IOException {
                os.writeInt(1);
                os.writeInt(2);
                os.writeShort(2);
            }

            public void cacheReady() {
                assertNotNull("stream can be obtained", s.asStream("mycache.dat"));
            }
            
        }
        Up updater = new Up();
        
        s.scheduleSave(updater, "mycache.dat", true);
        
        assertNull(s.asByteBuffer("mycache.dat"));
        assertNull(s.asStream("mycache.dat"));
        
        s.waitFor(false);

        {
            ByteBuffer bb;
            InputStream is;
            assertNotNull(bb = s.asByteBuffer("mycache.dat"));
            assertNotNull(is = s.asStream("mycache.dat"));

            assertEquals("10 bytes", 10, bb.remaining());
            assertEquals("10 bytes stream", 10, is.available());

            is.close();
            bb.clear();
        }
        
        s.scheduleSave(updater, "mycache.dat", true);
        
        s.waitFor(false);

        {
            ByteBuffer bb;
            assertNotNull(bb = s.asByteBuffer("mycache.dat"));

            assertEquals("appened bytes", 20, bb.remaining());

            bb.clear();
        }
        
    }
    
    
    public void testFastWhenShutdown() throws Exception {
        Stamps s = Stamps.getModulesJARs();
        
        assertNull(s.asByteBuffer("mycache.dat"));
        assertNull(s.asStream("mycache.dat"));

        class Up implements Stamps.Updater {
            volatile boolean finished;

            public void flushCaches(DataOutputStream os) throws IOException {
                byte[] arr = new byte[4096];
                for (int i = 0; i < 10000; i++) {
                    long previous = System.currentTimeMillis();
                    os.write(arr);
                    synchronized (this) {
                        notifyAll();
                    }
                }
                finished = true;
            }

            public void cacheReady() {
            }
            
        }
        Up updater = new Up();
        
        s.scheduleSave(updater, "mycache.dat", false);

        long now = System.currentTimeMillis();
        s.flush(1000);
        synchronized (updater) {
            updater.wait();
        }
        long diff = System.currentTimeMillis() - now;
        if (diff < 800) {
            fail("Updating shall start after 1s, not sooner: " + diff);
        }
        s.waitFor(false);
        
        assertTrue("Save is done", updater.finished);
    }
    
    public void testWriteToCacheWithError() {
        Stamps s = Stamps.getModulesJARs();
        
        assertNull(s.asByteBuffer("mycache.dat"));
        assertNull(s.asStream("mycache.dat"));

        class Up implements Stamps.Updater {
            boolean called;

            public void flushCaches(DataOutputStream os) throws IOException {
                for (int i = 0; i < 1024 * 1024; i++) {
                    os.write(10);
                }
                os.flush();
                os.close();
                throw new IOException("Not supported yet.");
            }

            public void cacheReady() {
                called = true;
            }
            
        }
        Up updater = new Up();
        
        s.scheduleSave(updater, "mycache.dat", false);
        
        assertNull(s.asByteBuffer("mycache.dat"));
        assertNull(s.asStream("mycache.dat"));
        
        CharSequence log = Log.enable("org.netbeans", Level.WARNING);
        s.waitFor(false);
        
        assertNull(s.asByteBuffer("mycache.dat"));
        assertNull(s.asStream("mycache.dat"));
        
        if (log.length() < 10) {
            fail("There should be a warning written to log:\n" + log);
        }
        
        assertFalse("cache ready not called", updater.called);
    }
    
    public void testCanHaveSubdirs() {
        final Stamps s = Stamps.getModulesJARs();
        
        assertNull(s.asByteBuffer("mydir/mycache.dat"));

        class Up implements Stamps.Updater {
            boolean called;

            public void flushCaches(DataOutputStream os) throws IOException {
                os.write(1);
            }

            public void cacheReady() {
                assertTrue("Now the cache can be accessed", s.exists("mydir/mycache.dat"));
                called = true;
            }
            
        }
        Up updater = new Up();
        
        s.scheduleSave(updater, "mydir/mycache.dat", false);
        s.waitFor(false);
        
        
        File userDir = new File(System.getProperty("netbeans.user"));
        File my = new File(new File(new File(new File(userDir, "var"), "cache"), "mydir"), "mycache.dat");
        
        assertTrue("file created", my.canRead());
        assertEquals("size 1", 1, my.length());
        
        assertTrue("cache was ready", updater.called);
    }
    
    public void testShutdownAndThenNoNotify() {
        final Stamps s = Stamps.getModulesJARs();
        
        assertNull(s.asByteBuffer("mydir/mycache.dat"));

        class Up implements Stamps.Updater {
            boolean called;

            public void flushCaches(DataOutputStream os) throws IOException {
                os.write(1);
            }

            public void cacheReady() {
                called = true;
            }
            
        }
        Up updater = new Up();
        
        s.scheduleSave(updater, "mydir/mycache.dat", false);
        s.flush(10000);
        s.shutdown();
        
        File userDir = new File(System.getProperty("netbeans.user"));
        File my = new File(new File(new File(new File(userDir, "var"), "cache"), "mydir"), "mycache.dat");
        
        assertTrue("file created", my.canRead());
        assertEquals("size 1", 1, my.length());
        
        assertFalse("cache was not called, due to shutdown", updater.called);
    }
    
    
    
    
    public void testJustOnce() {
        final Stamps s = Stamps.getModulesJARs();
        
        assertNull(s.asByteBuffer("mydir/mycache.dat"));

        class Up implements Stamps.Updater {
            int cnt;
            
            public void flushCaches(DataOutputStream os) throws IOException {
                
                assertNull("Now it is null", s.asStream("mydir/mycache.dat"));
                
                os.write(1);
                cnt++;
                if (cnt == 2) {
                    fail("Can save just once");
                }
            }

            public void cacheReady() {
            }
            
        }
        Up updater = new Up();
        
        s.scheduleSave(updater, "mydir/mycache.dat", false);
        s.scheduleSave(updater, "mydir/mycache.dat", false);
        
        s.waitFor(false);
        
        assertEquals("only once", 1, updater.cnt);
        
        
        s.scheduleSave(updater, "mydir/mycache.dat", false);
        assertNull("Now it is null as well", s.asStream("mydir/mycache.dat"));
        updater.cnt = 0;
        s.waitFor(false);
        
        assertNotNull("Returns value again", s.asStream("mydir/mycache.dat"));
        assertEquals("only once", 1, updater.cnt);
        
    }
    
    public void testParael() throws InterruptedException {
        final Stamps s = Stamps.getModulesJARs();
        
        assertNull(s.asMappedByteBuffer("mydir/mycache.dat"));

        class Up implements Stamps.Updater, Runnable {
            int ready;
            int cnt;
            Semaphore flushing = new Semaphore(0);
            Semaphore scheduled = new Semaphore(0);
            
            public void flushCaches(DataOutputStream os) throws IOException {
                int what = cnt++;
                if (what == 0) {
                    flushing.release();
                    scheduled.acquireUninterruptibly();
                }
                for (int i = 0; i < 1024 * 1024; i++) {
                    os.write(what);
                }
            }
            
            
            public void run() {
                flushing.acquireUninterruptibly();
                s.scheduleSave(this, "mydir/mycache.dat", false);
                scheduled.release();
                assertFalse(s.exists("mydir/mycache.dat"));
                s.waitFor(false);
            }

            public void cacheReady() {
                ready++;
            }
        }
        Up updater = new Up();
        
        s.scheduleSave(updater, "mydir/mycache.dat", false);
        
        Thread t = new Thread(updater, "fast flush");
        t.start();
        // slow flush
        s.flush(50);
        t.join();
        
        assertEquals("run twice", 2, updater.cnt);
        assertEquals("but just once ready", 1, updater.ready);

        MappedByteBuffer mmap = s.asMappedByteBuffer("mydir/mycache.dat");
        {
            assertEquals("1mb", 1024 * 1024, mmap.remaining());
            int r = 0;
            while (mmap.remaining() > 0) {
                assertEquals("Value " + r + " OK: ", 1, mmap.get());
                r++;
            }
        }
        
        s.scheduleSave(updater, "mydir/mycache.dat", false);
        assertNull("Now it is null as well", s.asStream("mydir/mycache.dat"));
        s.waitFor(false);

        MappedByteBuffer mmap2 = s.asMappedByteBuffer("mydir/mycache.dat");
        assertNotNull(mmap2);
        
        {
            assertEquals("1mb", 1024 * 1024, mmap2.remaining());
            int r = 0;
            while (mmap2.remaining() > 0) {
                assertEquals("Value2 " + r + " OK: ", 2, mmap2.get());
                r++;
            }
        }
    }
    
    public void testBrandingChecked() throws Exception {
        Stamps s = Stamps.getModulesJARs();
        ByteBuffer first = s.asByteBuffer("branding.cache");
        assertNull("No cache yet", first);
        
        s.scheduleSave(new SaveByte(), "branding.cache", false);
        s.waitFor(false);

        reset();
        s = Stamps.getModulesJARs();
        ByteBuffer snd = s.asByteBuffer("branding.cache");
        assertNotNull("Cache found", snd);

        reset();
        NbBundle.setBranding("my_perfect_branding");
        
        s = Stamps.getModulesJARs();
        ByteBuffer third = s.asByteBuffer("branding.cache");
        assertNull("Branding changed no cache found", third);
    }

    public void testLocaleChecked() throws Exception {
        Locale.setDefault(Locale.US);
        
        Stamps s = Stamps.getModulesJARs();
        ByteBuffer first = s.asByteBuffer("locale.cache");
        assertNull("No cache yet", first);
        
        s.scheduleSave(new SaveByte(), "locale.cache", false);
        s.waitFor(false);

        reset();
        s = Stamps.getModulesJARs();
        ByteBuffer snd = s.asByteBuffer("locale.cache");
        assertNotNull("Cache found", snd);

        
        reset();
        Locale.setDefault(Locale.FRANCE);
        
        s = Stamps.getModulesJARs();
        ByteBuffer third = s.asByteBuffer("locale.cache");
        assertNull("Locale changed no cache found", third);
    }
    
    public void testCanFallbackCacheToFirstDirsCluster() throws Exception {
        File cache = new File(new File(new File(ide, "var"), "cache"), "mycache");
        cache.getParentFile().mkdirs();
        FileOutputStream os = new FileOutputStream(cache);
        os.write("Ahoj".getBytes());
        os.close();
        
        InputStream is = Stamps.getModulesJARs().asStream("mycache");
        assertNotNull("Cache found", is);
        
        byte[] arr = new byte[10];
        int len = is.read(arr);
        assertEquals("Len is 4", 4, len);
        assertEquals("Ahoj", new String(arr, 0, 4));
    }

    /** Helper method to reset state of Stamps. */
    public static void reset() {
        Stamps.main("reset");
    }

    static void assertStamp(long expectedValue, File cluster, boolean global, boolean local) {
        File globalStamp = new File(cluster, ".lastModified");

        File userDir = new File(System.getProperty("netbeans.user"));
        File localStamp = new File(new File(new File(new File(userDir, "var"), "cache"), "lastModified"), Stamps.clusterLocalStamp(cluster));
        
        if (global) {
            assertTrue("File shall exist: " + globalStamp, globalStamp.exists());
            assertEquals("Modification time is good " + globalStamp, expectedValue, globalStamp.lastModified());
        } else {
            assertFalse("File shall not exist: " + globalStamp, globalStamp.exists());
        }

        if (local) {
            assertTrue("File shall exist: " + localStamp, localStamp.exists());
            assertEquals("Modification time is good " + localStamp, expectedValue, localStamp.lastModified());
        } else {
            assertFalse("File shall not exist: " + localStamp, localStamp.exists());
        }
        
    }

    static void createModule(String cnb, File cluster, long accesTime) throws IOException {
        String dashes = cnb.replace('.', '-');
        
        File config = new File(new File(new File(cluster, "config"), "Modules"), dashes + ".xml");
        File jar = new File(new File(cluster, "modules"), dashes + ".jar");
        
        config.getParentFile().mkdirs();
        jar.getParentFile().mkdirs();
        
        config.createNewFile();
        jar.createNewFile();
        config.setLastModified(accesTime);
        jar.setLastModified(accesTime);
    }

    private static final class SaveByte implements Updater {
        @Override
        public void flushCaches(DataOutputStream os) throws IOException {
            os.write(1);
        }

        @Override
        public void cacheReady() {
        }
    }
}
