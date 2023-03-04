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

package org.netbeans.core.startup.preferences;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Radek Matous
 */
public class TestPreferences extends NbPreferencesTest.TestBasicSetup {
    public TestPreferences(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        //if diabled - all tests should pass with default impl. of Preferences API
        assertSame(new NbPreferencesFactory().userRoot(), Preferences.userRoot());
        
        Preferences.userRoot().flush();
    }

   
    private Preferences getPreferencesNode() {
        return getUserPreferencesNode();
    }
    
    private Preferences getUserPreferencesNode() {
        return Preferences.userNodeForPackage(NbPreferencesTest.class).node(getName());
    }
    
    private Preferences getSystemPreferencesNode() {
        return Preferences.systemNodeForPackage(NbPreferencesTest.class).node(getName());
    }
    
    
    public void testUserRoot() throws Exception {
        try {
            Preferences.userRoot().removeNode();
            fail();
        } catch (UnsupportedOperationException ex) {
        }
    }
    

    /* We do not really care about enforcing these limits:
    public void testKeyExceededSize() throws Exception {
        Preferences pref = getPreferencesNode();
        StringBuffer sb = new StringBuffer();
        for (; sb.toString().length() < Preferences.MAX_KEY_LENGTH + 1; ) {
            sb.append("1234567890");
        }
        assertTrue(sb.toString().length() > Preferences.MAX_KEY_LENGTH);
        try {
            pref.put(sb.toString(),"sss");
            fail();
        } catch (IllegalArgumentException iax) {
        }
    }
    
    public void testValueExceededSize() throws Exception {
        Preferences pref = getPreferencesNode();
        StringBuffer sb = new StringBuffer();
        for (; sb.toString().length() < Preferences.MAX_VALUE_LENGTH + 1; ) {
            sb.append("1234567890");
        }
        assertTrue(sb.toString().length() > Preferences.MAX_VALUE_LENGTH);
        try {
            pref.put("sss", sb.toString());
            fail();
        } catch (IllegalArgumentException iax) {
        }
    }
     */
    
    public void testNameExceededSize() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (; sb.toString().length() < Preferences.MAX_NAME_LENGTH + 1; ) {
            sb.append("1234567890");
        }
        
        assertTrue(sb.toString().length() > Preferences.MAX_NAME_LENGTH);
        try {
            Preferences pref =
                    getPreferencesNode().node(sb.toString());
            fail();
        } catch (IllegalArgumentException iax) {
        }
    }
    
    public void testNullParameter() throws Exception {
        Preferences pref = getPreferencesNode();
        try {
            pref.get(null, "value");
            fail();
        } catch(NullPointerException npe) {
        }
        
        try {
            //null permited here
            pref.get("key", null);
        } catch(NullPointerException npe) {
            fail();
        }
        
        try {
            pref.node(null);
            fail();
        } catch(NullPointerException npe) {
        }
        
        try {
            pref.node("node2/");
            fail();
        } catch(IllegalArgumentException iax) {
        }
        
    }
    
    public void testIsUserNode() {
        Preferences upref = getUserPreferencesNode();
        Preferences spref = getSystemPreferencesNode();
        assertTrue(upref.isUserNode());
        assertFalse(spref.isUserNode());
    }
    
    public void testNode() throws BackingStoreException {
        Preferences pref = getPreferencesNode();
        assertNotNull(pref);
        assertTrue(pref.nodeExists(""));
        assertFalse(pref.nodeExists("sub1"));
        assertFalse(Arrays.asList(pref.childrenNames()).contains("sub1"));
        
        Preferences sub1 =pref.node("sub1");
        assertTrue(pref.nodeExists("sub1"));
        assertTrue(Arrays.asList(pref.childrenNames()).contains("sub1"));
    }
    
    public void testChildrenNames() throws Exception {
        Preferences pref = getPreferencesNode();
        assertNotNull(pref);
        assertEquals(0, pref.childrenNames().length);
        
        assertFalse(pref.nodeExists("sub1"));
        Preferences sub1 =pref.node("sub1");
        assertNotNull(sub1);
        assertTrue(pref.nodeExists("sub1"));
        assertEquals(1, pref.childrenNames().length);
        
        
        assertFalse(pref.nodeExists("sub2"));
        Preferences sub2 =pref.node("sub2");
        assertNotNull(sub2);
        assertTrue(pref.nodeExists("sub2"));
        assertEquals(2, pref.childrenNames().length);
        
        sub1.removeNode();
        assertEquals(1, pref.childrenNames().length);
        sub2.removeNode();
        assertEquals(0, pref.childrenNames().length);
    }
    
    public void testPut()  {
        Preferences pref = getPreferencesNode();
        assertNotNull(pref);
        
        assertNull(pref.get("key1", null));
        pref.put("key1", "value1");
        assertEquals("value1",pref.get("key1", null));
    }

    public void testPut_245383()  {
        NbPreferences pref = (NbPreferences)getPreferencesNode();
        assertNotNull(pref);
        
        assertNull(pref.get("key1", null));
        pref.put("key1", "true");
        assertEquals("true", pref.get("key1", null));
        
        pref.put("key1", "false");
        assertEquals("false", pref.get("key1", null));
        
        // no need to call flush() or sync()
        pref.put("key1", "true");
        assertEquals("true", pref.get("key1", null));
        
        // need to call flush() or sync() in order to clear NbPreferences.cachedKeyValues list
        try {
            pref.flush();
            pref.sync();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
        assertEquals("true", pref.get("key1", null));
        
        // mimic state change event call, e.g. when importing options
        pref.put("key1", "false", true);
        assertEquals("false", pref.get("key1", null));
    }

    /*
    @RandomlyFails // timeout in NB-Core-Build #1651; three of the waits actually time out; test is probably broken
    public void testPut2()  throws Exception {
        final Object sync = getEventQueueSync();
        Preferences pref = getPreferencesNode();
        assertNotNull(pref);
        final List<Object> l = new ArrayList<Object>();
        assertNull(pref.get("key1", null));
        PreferenceChangeListener pl = new PreferenceChangeListener(){
            public void preferenceChange(PreferenceChangeEvent evt) {
                synchronized(sync) {
                    l.add(evt.getNewValue());
                    sync.notifyAll();
                }
            }
        };
        pref.addPreferenceChangeListener(pl);
        try {
            pref.put("key1", "value1");
            assertEquals("value1",pref.get("key1", null));            
            synchronized(sync) {
                sync.wait(5000);
                assertEquals(1, l.size());
            }
            assertEquals("value1",l.get(0));
            l.clear();
            
            pref.put("key1", "value2");
            assertEquals("value2",pref.get("key1", null));
            synchronized(sync) {
                sync.wait(5000);
                assertEquals(1, l.size());
            }
            assertEquals("value2",l.get(0));
            l.clear();
            
            pref.put("key1", "value2");
            assertEquals("value2",pref.get("key1", null));
            synchronized(sync) {
                sync.wait(5000);
                assertEquals(0, l.size());
            }
            l.clear();
            
            pref.put("key1", "value2");
            assertEquals("value2",pref.get("key1", null));
            synchronized(sync) {
                sync.wait(5000);
                assertEquals(0, l.size());
            }
            l.clear();
            
        } finally {
            pref.removePreferenceChangeListener(pl);
        }
    }
    
    private Object getEventQueueSync() {
        try {
            Field f = AbstractPreferences.class.getDeclaredField("eventQueue");
            f.setAccessible(true);
            return f.get(null);
            
        } catch (Exception ex) {
            Logger.getLogger("global").log(java.util.logging.Level.SEVERE,ex.getMessage(), ex);
        }
        return null;
    }
     */
     
    public void testRemove() {
        testPut();
        Preferences pref = getPreferencesNode();
        assertEquals("value1",pref.get("key1", null));
        pref.remove("key1");
        assertNull(pref.get("key1", null));
    }
    
    public void testClear() throws Exception {
        testKeys();
        Preferences pref = getPreferencesNode();
        pref.clear();
        assertEquals(0,pref.keys().length);
        assertNull(pref.get("key1", null));
        assertNull(pref.get("key2", null));
    }
    
    public void testKeys() throws Exception {
        Preferences pref = getPreferencesNode();
        assertNotNull(pref);
        assertEquals(0,pref.keys().length);
        pref.put("key1", "value1");
        pref.put("key2", "value2");
        assertEquals(2,pref.keys().length);
    }
    
    
    public void testParent() {
        Preferences pref = getPreferencesNode();
        Preferences pref2 = pref.node("1/2/3");
        assertNotSame(pref, pref2);
        
        for (int i = 0; i < 3; i++) {
            pref2 = pref2.parent();
        }
        
        assertSame(pref2.absolutePath(), pref, pref2);
    }
    
    public void testNodeExists() throws Exception {
        Preferences pref = getPreferencesNode();
        Preferences pref2 = pref.node("a/b/c");
        while(pref2 != Preferences.userRoot()) {
            assertTrue(pref2.nodeExists(""));
            Preferences parent = pref2.parent();
            pref2.removeNode();
            assertFalse(pref2.nodeExists(""));
            pref2 = parent;
        }
        
        assertNotNull(getPreferencesNode().node("a/b/c/d"));
        assertTrue(getPreferencesNode().node("a/b/c/d").nodeExists(""));
    }
    
    
    public void testName() {
        Preferences pref = getPreferencesNode();
        assertEquals("myname",pref.node("myname").name());
    }
    
    public void testAbsolutePath() {
        String validPath = "/a/b/c/d";
        Preferences pref = Preferences.userRoot().node(validPath);
        assertEquals(validPath, pref.absolutePath());
        
        //relative path
        assertSame(pref, pref.parent().node("d"));
        
        String invalidPath = "/a/b/c/d/";
        try {
            pref = Preferences.userRoot().node(invalidPath);
            fail();
        } catch(IllegalArgumentException iax) {}
        
    }
    
    public void testAddPreferenceChangeListener() throws BackingStoreException, InterruptedException {
        final Preferences pref = getPreferencesNode();
        PreferenceChangeListener l = new PreferenceChangeListener() {
            public void preferenceChange(PreferenceChangeEvent evt) {
                synchronized (TestPreferences.class) {
                    //assertionerrors cause deadlock here
                    assertSame(pref, evt.getNode());
                    assertEquals("key", evt.getKey());
                    assertEquals(evt.getNewValue(),pref.get(evt.getKey(),null), evt.getNewValue());
                    TestPreferences.class.notifyAll();
                }
            }
        };
        pref.addPreferenceChangeListener(l);
        try {
            synchronized (TestPreferences.class) {
                pref.put("key","AddPreferenceChangeListener");
                pref.flush();
                TestPreferences.class.wait();
            }
            
            synchronized (TestPreferences.class) {
                pref.remove("key");
                pref.flush();
                TestPreferences.class.wait();
            }
            
            synchronized (TestPreferences.class) {
                pref.put("key","AddPreferenceChangeListener2");
                pref.flush();
                TestPreferences.class.wait();
            }
        } finally {
            pref.removePreferenceChangeListener(l);
        }
    }
    
    public void testAddNodeChangeListener() throws BackingStoreException, InterruptedException {
        final Preferences pref = getPreferencesNode();
        NodeChangeListener l = new NodeChangeListener() {
            public void childAdded(NodeChangeEvent evt) {
                synchronized (TestPreferences.class){
                    //assertionerrors cause deadlock here
                    assertSame(pref, evt.getParent());
                    assertEquals("added",evt.getChild().name());
                    TestPreferences.class.notifyAll();
                }
            }
            
            public void childRemoved(NodeChangeEvent evt) {
                synchronized (TestPreferences.class) {
                    //assertionerrors cause deadlock here
                    assertSame(pref, evt.getParent());
                    assertEquals("added",evt.getChild().name());
                    TestPreferences.class.notifyAll();
                }
            }
        };
        pref.addNodeChangeListener(l);
        try {
            Preferences added;
            synchronized (TestPreferences.class) {
                added = pref.node("added");
                TestPreferences.class.wait();
            }
            
            synchronized (TestPreferences.class) {
                added.removeNode();
                TestPreferences.class.wait();
            }
            
        } finally {
            pref.removeNodeChangeListener(l);
        }
    }
    
    public void testIsPersistent()  throws BackingStoreException,InterruptedException {
        NbPreferences pref = (NbPreferences)getPreferencesNode();
        assertNotNull(pref);
        assertEquals(NbPreferences.UserPreferences.class, pref.getClass());
        pref.put("key", "value");
        assertEquals("value", pref.get("key", null));
        pref.sync();
        assertEquals("value", pref.get("key", null));
    }  
    
    public void testClearCache_248538() throws BackingStoreException {
        NbPreferences pref = (NbPreferences) getPreferencesNode();
        assertNotNull(pref);
        for (int i = 0; i < 10000; i++) {
            pref.put("key", "value-".concat(Integer.toString(i)));
        }
        // cache should only contain values [9000 - 9999]
        assertEquals(1000, getCachedKeyValuesSize(pref, "key"));
        
        pref.put("key", "value-10000");
        // cache should only contain values [9900 - 10000]
        assertEquals(101, getCachedKeyValuesSize(pref, "key"));
        
        pref.flush();
        // cache should be cleared
        assertEquals(-1, getCachedKeyValuesSize(pref, "key"));
    }
    
    private int getCachedKeyValuesSize(NbPreferences pref, String key) {
        ArrayList<String> values = pref.cachedKeyValues.get(key);
        return values == null ? -1 : values.size();
    }
    
    public void testUnsavedChangesDropped () throws Exception {
        NbPreferences pref = (NbPreferences) getPreferencesNode();
        FileObject fo = ((PropertiesStorage) pref.fileStorage).toPropertiesFile(true);
        InputStream is = fo.getInputStream();
        // first change made to prefs
        pref.put("KEY1", "VALUE1");
        // wait until the async task persisting the props starts
        Thread.sleep(300);
        // now let the async task finish it's job
        is.close();
        // make the second change before the map in memory is nulled
        pref.put("KEY2", "VALUE2");
        // wait for the finilizing stage of the async task - property map should be nulled
        Thread.sleep(300);
        // now time for tests, first value should be OK
        assertEquals("VALUE1", pref.get("KEY1", null));
        // but where's the second value?? it nulled
        assertEquals("VALUE2", pref.get("KEY2", null));
    }
    
    public void testRemoveNode () throws Exception {
        NbPreferences pref = (NbPreferences) getPreferencesNode();
        assertNotNull(pref);
        pref.put("key", "value");
        assertEquals("value", pref.get("key", null));
        pref.removeNode();
        assertNotNull(pref);
        pref.put("key1", "value1");
    }
    
    public void testRemoveNode2 () throws Exception {
        NbPreferences pref = (NbPreferences) getPreferencesNode();
        assertNotNull(pref);
        pref.put("key0", "value0");
        pref.flush();        
        assertEquals("value0", pref.get("key0", null));
        
        pref.removeNode();
        pref.flush();
        assertNotNull(pref);        
        PropertiesStorage storage = (PropertiesStorage) pref.fileStorage;        
        assertNull(storage.toPropertiesFile());
        assertNull(storage.toFolder());
        assertFalse(storage.existsNode());
        
        storage.toPropertiesFile(true);
        assertNotNull(storage.toPropertiesFile());
        
        OutputStream storageOutputStream = storage.toPropertiesFile().getOutputStream();
        storageOutputStream.write("key1=value1".getBytes(StandardCharsets.ISO_8859_1));
        storageOutputStream.close();
    }

    public void testRemoveNode3 () throws Exception {
        NbPreferences pref = (NbPreferences) getPreferencesNode();
        assertNotNull(pref);
        pref.put("key", "value");
        assertEquals("value", pref.get("key", null));
        pref.removeNode();
        assertNotNull(pref);
        pref.removeNode();
    }

    public void testRemoveNode4 () throws Exception {
        NbPreferences pref = (NbPreferences) getPreferencesNode();
        assertNotNull(pref);
        pref.put("key", "value");
        assertEquals("value", pref.get("key", null));
        pref.removeNode();
        assertNotNull(pref);
        pref.sync();
    }
    
    @Override
    protected int timeOut() {
        return 20000;
    }


}
