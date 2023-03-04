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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.prefs.*;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.*;
import org.openide.util.Exceptions;

/**
 *
 * @author Radek Matous
 */
public class TestPropertiesStorage extends TestFileStorage {
    private PropertiesStorage storage;
    private NbPreferences pref;
    private CountDownLatch prefChangedEvent = new CountDownLatch(1);
    private CountDownLatch nodeAddedEvent = new CountDownLatch(1);
    private CountDownLatch nodeRemovedEvent = new CountDownLatch(1);
    private CountDownLatch prefRemovedEvent = new CountDownLatch(1);
    
    public TestPropertiesStorage(String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return 20000;
    }
    
    @Override
    protected void setUp() throws Exception {        
        super.setUp();
        assertSame(new NbPreferencesFactory().userRoot(), Preferences.userRoot());
        pref = getPreferencesNode();
        assertNotNull(pref);
        storage = (PropertiesStorage)pref.fileStorage;
        assertNotNull(storage);        
    }
    
    @Override
    protected NbPreferences.FileStorage getInstance() {
        return PropertiesStorage.instanceReadOnly(FileUtil.getSystemConfigRoot(), "/PropertiesStorageTest/" + getName());//NOI18N);
    }
    
    @Override
    void noFileRepresentationAssertion() throws IOException {
        super.noFileRepresentationAssertion();
        assertNull(((PropertiesStorage)instance).toFolder());
        assertNull(((PropertiesStorage)instance).toPropertiesFile());
    }
    
    @Override
    void fileRepresentationAssertion() throws IOException {
        super.fileRepresentationAssertion();
        assertNotNull(((PropertiesStorage)instance).toFolder());
        assertNotNull(((PropertiesStorage)instance).toPropertiesFile());
    }
    
    private NbPreferences getPreferencesNode() {
        return (NbPreferences)Preferences.userNodeForPackage(TestPropertiesStorage.class).node(getName());
    }
    
    public void testNode() throws BackingStoreException {        
        assertNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());
        pref.flush();
        assertNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());        
        pref.put("key","value");
    }
    
    public void testNode2() throws BackingStoreException {
        testNode();
        pref.flush();
        assertNotNull(storage.toPropertiesFile());
    }
    
    public void testNode3() throws BackingStoreException {
        testNode();
        pref.flushTask.waitFinished();
        assertNotNull(storage.toPropertiesFile());                
    }

    public void testNode4() throws BackingStoreException {
        pref.node("a");
        testNode();
    }

    public void testNode5() throws BackingStoreException {
        Preferences child = pref.node("a");
        child.put("key","value");
        child.flush();
        assertNotNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());                
    }

      public void testNodeIsReloadedAfterChange() throws Exception {
         String key= "key";
         String value = "oldValue";
         String newValue = "newValue";
         storeEntry(key, value);
         overrideStorageEntryWithNewValue(value, newValue);

         String reloadedValue = pref.get(key, null);

         assertNotNull("Reloaded value must not be null", reloadedValue);
         assertEquals("Reloaded value must equals to manually stored value", newValue, reloadedValue);
         /*
          Still need to cope with a memory leak

         WeakReference weakPref = new WeakReference(pref);
         storage = null;
         pref = null;
         assertGC("NbPreferences is not GC", weakPref);
         */

     }

     private void storeEntry(String keyName, String oldValue) throws BackingStoreException {
         pref.put(keyName, oldValue);
         pref.flush();
     }

     private void overrideStorageEntryWithNewValue(String oldValue, String newValue) throws IOException {
         String newText = constructNewEntryText(oldValue, newValue);         
         OutputStream storageOutputStream = storage.toPropertiesFile().getOutputStream();
         storageOutputStream.write(newText.getBytes(StandardCharsets.ISO_8859_1));
         storageOutputStream.close();                     
     }
     
     private void overrideStorageEntryWithNewData(String newData) throws IOException {
         OutputStream storageOutputStream = storage.toPropertiesFile(true).getOutputStream();
         storageOutputStream.write(newData.getBytes(StandardCharsets.ISO_8859_1));
         storageOutputStream.close();                     
     }
     
     private void deleteStorageEntry(String key) throws IOException {
         String currentText = storage.toPropertiesFile().asText();
         int index = currentText.indexOf(key);
         
         String before = currentText.substring(0, index);
         String after = currentText.substring(index + key.length() + 1);
         String newText = before.concat(after);
         
         OutputStream storageOutputStream = storage.toPropertiesFile().getOutputStream();
         storageOutputStream.write(newText.getBytes(StandardCharsets.ISO_8859_1));
         storageOutputStream.close();                     
     }

     private String constructNewEntryText(String oldValue, String newValue) throws IOException {
         String currentText = storage.toPropertiesFile().asText();
         String newText = currentText.replace(oldValue, newValue);
         return newText;
     }
     
     
    private class NodeListener implements NodeChangeListener {

        @Override
        public void childAdded(NodeChangeEvent evt) {
            nodeAddedEvent.countDown();
        }

        @Override
        public void childRemoved(NodeChangeEvent evt) {
            nodeRemovedEvent.countDown();
        }
    }

    private class PrefListener implements PreferenceChangeListener {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            if (evt.getNewValue() != null) {
                prefChangedEvent.countDown();
            } else {
                prefRemovedEvent.countDown();
            }
        }
    }
    
    private class FileListener implements FileChangeListener {

        @Override
        public void fileFolderCreated(FileEvent fe) {
            String path = fe.getFile().getPath();
            pref.node(path);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            String path = fe.getFile().getPath();
            pref.node(path);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            String path = fe.getFile().getPath();
            pref.node(path);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            String path = fe.getFile().getPath();
            try {
                pref.node(path).removeNode();
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    }
     
     public void testValueChangeFireEvent() throws Exception {
        pref.put("key0", "value10");
        pref.addPreferenceChangeListener(new PrefListener());
        pref.sync();
        assertEquals("value10", pref.get("key0", null));
        overrideStorageEntryWithNewData("key0=value0");
        
        assertEquals("value0", pref.get("key0", null));
        prefChangedEvent.await();
        assertEquals("No preference changed event was fired for value change", 0, prefChangedEvent.getCount());
    }

    public void testKeyRemovalFireEvent() throws Exception {
        pref.put("key", "value");
        pref.put("key0", "value10");
        pref.put("key1", "value11");

        pref.addPreferenceChangeListener(new PrefListener());
        pref.sync();
        assertEquals("value10", pref.get("key0", null));
        deleteStorageEntry("key0=value10");
        
        assertEquals(null, pref.get("key0", null));
        prefRemovedEvent.await();
        assertEquals("No preference changed event was fired for key removal", 0, prefRemovedEvent.getCount());
    }
    
    public void testPreferencesEvents() throws Exception {
        storage.toFolder(true);
        FileUtil.getConfigRoot().addRecursiveListener(new FileListener());
        pref.addNodeChangeListener(new NodeListener());
        
        String newPath = "a/b/c";
        String[] paths = newPath.split("/");
        FileObject fo = null;
        FileObject fo0 = null;
        for (int i = 0; i < paths.length; i++) {
            String path = paths[i];
            fo = FileUtil.createFolder((fo == null) ? FileUtil.getConfigRoot() : fo, path);
            if(i == 0) {
                fo0 = fo;
            }
            nodeAddedEvent.await();
            assertEquals("Missing node added event", 0, nodeAddedEvent.getCount());
            nodeAddedEvent = new CountDownLatch(1);

            Preferences pref2 = pref.node(fo.getPath());
            pref2.addNodeChangeListener(new NodeListener());
        }
        
        FileObject fo1 = FileUtil.createData(fo, "a.properties");
        nodeAddedEvent.await();
        assertEquals("Missing node added event", 0, nodeAddedEvent.getCount());
        
        nodeRemovedEvent = new CountDownLatch(paths.length + 1);
        fo0.delete();
        nodeRemovedEvent.await();
        assertEquals("Missing node removed event", 0, nodeRemovedEvent.getCount());
    }
    
    public void testPreferencesEvents2() throws Exception {
        pref.addNodeChangeListener(new NodeListener());
        pref.addPreferenceChangeListener(new PrefListener());
        
        Preferences child = pref.node("a");
        nodeAddedEvent.await();
        assertEquals("Missing node added event", 0, nodeAddedEvent.getCount());
        pref.put("key","value");
        prefChangedEvent.await();
        assertEquals("Missing preference change event", 0, prefChangedEvent.getCount());
        pref.remove("key");
        prefRemovedEvent.await();
        assertEquals("Missing preference removed event", 0, prefRemovedEvent.getCount());
        child.removeNode();
        nodeRemovedEvent.await();
        assertEquals("Missing node removed event", 0, nodeRemovedEvent.getCount());
    }

    public void testRemove() throws BackingStoreException {
        assertNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());                
        
        pref.put("key","value");
        pref.flush();
        assertNotNull(storage.toPropertiesFile());
        pref.remove("key");
        assertTrue(pref.properties.isEmpty());
        pref.flush();
        assertNull(storage.toPropertiesFile());                
    }

    public void testRemove2() throws BackingStoreException {
        assertNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());                

        pref.put("key","value");
        pref.put("key1","value1");

        pref.flush();
        assertNotNull(storage.toPropertiesFile());
        pref.remove("key");
        assertFalse(pref.properties.isEmpty());
        pref.flush();
        assertNotNull(storage.toPropertiesFile());                
    }

    public void testClear() throws BackingStoreException {
        assertNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());
        pref.put("key","value");
        pref.put("key1","value");
        pref.put("key2","value");
        pref.put("key3","value");
        pref.put("key5","value");
        pref.flush();
        assertNotNull(storage.toPropertiesFile());                

        pref.clear();
        pref.flush();
        assertNull(storage.toPropertiesFile());                
    }

    @RandomlyFails // FSException: Invalid lock (from flush)
    public void testRemoveNode() throws BackingStoreException {
        assertNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());                

        pref.put("key","value");
        pref.node("subnode").put("key","value");
        pref.flush();
        assertNotNull(storage.toPropertiesFile());
        assertNotNull(storage.toFolder());
        pref.removeNode();
        pref.flush();
        assertNull(storage.toPropertiesFile());
        assertNull(storage.toFolder());
        assertFalse(storage.existsNode());
        pref.sync();
    }

    public void testRemoveParentNode() throws BackingStoreException {
        assertNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());                

        Preferences subnode = pref.node("subnode");
        assertNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());                
        subnode.put("key","value");
        subnode.flush();
        assertNotNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());                
        subnode.removeNode();        
        pref.flush();
        assertNull(storage.toPropertiesFile());
        assertNull(storage.toFolder());
        assertFalse(storage.existsNode());
    }    

    public void testChildrenNames() throws Exception {
        Preferences subnode = pref.node("c1");
        subnode.put("k","v");
        subnode.flush();
        subnode = pref.node("c2");
        subnode.put("k","v");
        subnode.flush();
        subnode = pref.node("c3/c4");
        subnode.put("k","v");
        subnode.flush();
        assertEquals(new TreeSet<String>(Arrays.asList("c1", "c2", "c3")), new TreeSet<String>(Arrays.asList(storage.childrenNames())));
        pref.node("c2").removeNode();
        assertEquals(new TreeSet<String>(Arrays.asList("c1", "c3")), new TreeSet<String>(Arrays.asList(storage.childrenNames())));
        pref.node("c3").removeNode();
        assertEquals(Collections.singleton("c1"), new TreeSet<String>(Arrays.asList(storage.childrenNames())));
        pref.node("c1").removeNode();
        assertEquals(Collections.emptySet(), new TreeSet<String>(Arrays.asList(storage.childrenNames())));
    }
    
    public void testInvalidChildrenNames() throws Exception {
        NbPreferences subnode = pref;
        assertNotNull(subnode);
        PropertiesStorage ps = (PropertiesStorage)pref.fileStorage;        
        FileObject fold = ps.toFolder(true);
        assertNotNull(FileUtil.createData(fold, "a/b/c/invalid1"));
        subnode.sync();
        assertEquals(0, subnode.childrenNames().length);

        assertNotNull(FileUtil.createData(fold, "a/b/c/invalid2.huh"));
        subnode.sync();
        assertEquals(0, subnode.childrenNames().length);

        assertNotNull(FileUtil.createData(fold, "a/b/c/invalid3.properties.huh"));
        subnode.sync();
        assertEquals(0, subnode.childrenNames().length);
        
        assertNotNull(FileUtil.createData(fold, "a/b/c/valid.properties"));
        subnode.sync();
        assertEquals(1, subnode.childrenNames().length);        
        assertEquals("a", subnode.childrenNames()[0]);        
    }    
}
