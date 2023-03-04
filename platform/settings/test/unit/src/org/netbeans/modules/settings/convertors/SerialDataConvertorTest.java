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

package org.netbeans.modules.settings.convertors;

import java.awt.GraphicsEnvironment;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem; // override java.io.FileSystem
import org.openide.loaders.*;
import org.openide.cookies.*;
import java.util.logging.Level;
import org.openide.modules.ModuleInfo;
import org.openide.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.settings.FactoryMethod;

import org.netbeans.junit.*;

/**
 * @author Jan Pokorsky
 */
public class SerialDataConvertorTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(SerialDataConvertorTest.class);
    }

    /** folder to create instances in */
    private DataFolder folder;
    /** filesystem containing created instances */
    //private FileSystem lfs;
    
    /** Creates new DataFolderTest */
    public SerialDataConvertorTest(String name) {
        super (name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    /** Setups variables.
     */
    protected void setUp () throws Exception {
        Lookup.getDefault().lookup(ModuleInfo.class);
        org.openide.filesystems.FileUtil.createFolder(FileUtil.getConfigRoot(), "BB/AAA");
        org.openide.filesystems.FileUtil.createFolder(FileUtil.getConfigRoot(), "system/Services/lookupTest");
        org.openide.filesystems.FileUtil.createFolder(FileUtil.getConfigRoot(), "testCreateInstance");
        
        
        String fsstruct [] = new String [] {
            "BB/AAA/",
            "system/Services/lookupTest/",
            "testCreateInstance/",
        };
        
        FileObject bb = FileUtil.getConfigFile("/BB");
        FileObject bb_aaa = FileUtil.getConfigFile("/BB/AAA");
        
        DataObject dest = DataObject.find(bb_aaa);
        
        assertTrue("Destination folder doesn't exist.", dest != null);
        assertTrue("Destination folder is not valid.", dest.isValid ());
        
        folder = DataFolder.findFolder (bb);
    }
     public void testSaveCookieChanges() throws Exception {
         class MyListener implements LookupListener {
             int i = 0;
             public void resultChanged(LookupEvent ev) {
                 i++;
             }
         }
         MyListener ml = new MyListener();
         LocalFileSystem lfs = new LocalFileSystem();
         InstanceDataObject i = InstanceDataObject.create (folder, null, lfs, null);
         assertNull(i.getCookie(SaveCookie.class));
         Lookup.Result<SaveCookie> scr =  i.getLookup().lookup(new Lookup.Template(SaveCookie.class));        
         scr.addLookupListener(ml);
         Collection<? extends SaveCookie>  saveCookies = scr.allInstances();
         assertFalse(saveCookies.contains(lfs));
         
         lfs.setRootDirectory(getWorkDir());        
         SaveCookie sv = i.getCookie(SaveCookie.class);
         assertNotNull(sv);
         saveCookies = scr.allInstances();
         assertTrue("Cookie " + sv + "is there: " + saveCookies, saveCookies.contains(sv));        
         assertEquals(1, ml.i);
         sv.save();
         sv = i.getCookie(SaveCookie.class);
         assertNull(sv);        
         saveCookies = scr.allInstances();        
         assertTrue(saveCookies.isEmpty());                
         assertEquals(2, ml.i);
         i.getPrimaryFile().delete();
     }
    
    public void test50177ProblemSimulation () throws Exception {
        FileObject testFolder = FileUtil.createFolder(FileUtil.getConfigRoot (), "Services");
        assertNotNull(testFolder);
        
        InstanceDataObject ido = InstanceDataObject.create(DataFolder.findFolder(testFolder), "test50177ProblemSimulation", new Ex50177(),null);
        
        Lookup.Item item = Lookup.getDefault().lookupItem(new Lookup.Template (Ex50177.class, null, null));    
        assertNotNull(item);        
        String id = item.getId();
        
        Ex50177 exObj = (Ex50177)item.getInstance();
        assertNotNull(exObj);
        
        exObj.setSomething("set any value shouldn't cause #50177");
        SerialDataConvertorTest.waitUntilIsSaved (ido);

        //!!! this is the failing line causing #50177
        assertNotNull(Lookup.getDefault().lookupItem(new Lookup.Template (null, id, null)));
    }

    public void test50177Cause () throws Exception {
        FileObject testFolder = FileUtil.createFolder(FileUtil.getConfigRoot (), "Services");
        assertNotNull(testFolder);
        
        InstanceDataObject ido = InstanceDataObject.create(DataFolder.findFolder(testFolder), "test50177Cause", new Ex50177(),null);
        String idoName = ido.getName();
        
        Ex50177 exObj = (Ex50177)ido.instanceCreate();
        assertNotNull(exObj);
        assertEquals(idoName, ido.getName());
        
        exObj.setSomething("any value");//set any value shouldn't cause #50177
        SerialDataConvertorTest.waitUntilIsSaved (ido);
        //!!! this is the failing line causing #50177
        assertEquals(idoName, ido.getName());
    }

    private static void waitUntilIsSaved (InstanceDataObject ido) throws InterruptedException {
            SaveCookie sc = (SaveCookie)ido.getCookie(SaveCookie.class);         
            for (int i = 0; i < 5 && sc != null; i++) {
                Thread.sleep (3000);            
                sc = (SaveCookie)ido.getCookie(SaveCookie.class);
            }
            assertNull(sc);        
    }
    
    public void test50177SideEffectsAfterRename () throws Exception {
        FileObject testFolder = FileUtil.createFolder(FileUtil.getConfigRoot (), "Services");
        assertNotNull(testFolder);
        
        InstanceDataObject ido = InstanceDataObject.create(DataFolder.findFolder(testFolder), "test50177SideEffectsAfterRename", new Ex50177(),null);
        FileObject fo = ido.getPrimaryFile();
        fo.setAttribute("SystemFileSystem.localizingBundle", "org.netbeans.modules.settings.convertors.data.Bundle");
        
        String newName = "newName";
        ido.getNodeDelegate().setName(newName);
        SerialDataConvertorTest.waitUntilIsSaved (ido);
        assertEquals(newName, ido.getNodeDelegate().getDisplayName());
        
        /// simulates recretaion of instance e.g. after IDE restart
        fo = FileUtil.copyFile(fo, fo.getParent(),"copiedPeer", fo.getExt());
        fo.setAttribute("SystemFileSystem.localizingBundle", "org.netbeans.modules.settings.convertors.data.Bundle");        
        ido = (InstanceDataObject)DataObject.find(fo);
        
        assertEquals(newName, ido.getNodeDelegate().getDisplayName());        
    }
    
    public static final class Ex50177 extends org.openide.ServiceType {
    /** generated Serialized Version UID */
        private static final long serialVersionUID = -7572487174423654252L;
        private String name = "My Own Ex";
        
        protected String displayName() {
            return name;
        }
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }
        
        
        private String something;
        public String getSomething () {
            return something;
        }
        public void setSomething (String s) {
            String old = something;
            something = s;
            firePropertyChange("Something", old, s);
        }

        public void setName(String name) {
            this.name = name;
            firePropertyChange(name, null, name);
        }
    }
    
    /** Checks whether the instance is the same.
     */
    public void testSame() throws Exception {

        Ser ser = new Ser ("1");
        
        InstanceDataObject i = InstanceDataObject.create (folder, null, ser, null);
        
        Object n = i.instanceCreate ();
        if (n != ser) {
            fail ("instanceCreate is not the same: " + ser + " != " + n);
        }
        
        i.delete ();
    }
    
    /** Test whether instances survive garbage collection.
     */
    public void testSameWithGC () throws Exception {
        Object ser = new java.awt.Button();
        
        FileObject prim = InstanceDataObject.create (folder, "MyName", ser, null).getPrimaryFile ();
        String name = prim.getName ();
        String ext = prim.getExt ();
        prim = null;

        System.gc ();
        System.gc ();
        System.gc ();
        System.gc ();
        System.gc ();
        System.gc ();
        System.gc ();
        System.gc ();
        System.gc ();
        
        FileObject fo = folder.getPrimaryFile ().getFileObject (name, ext);
        assertTrue ("MyName.settings not found", fo != null);
        
        DataObject obj = DataObject.find (fo);
        
        InstanceCookie ic = (InstanceCookie)obj.getCookie (InstanceCookie.class);
        assertTrue ("Object: " + obj + " does not have instance cookie", ic != null);
        
        Object value = ic.instanceCreate ();
        if (value != ser) {
            fail ("Value is different than serialized: " + System.identityHashCode (ser) + " value: " + System.identityHashCode (value));
        }
        
        obj.delete ();
    }
    
    /** Tests the creation in atomic section.
     */
    public void testSameInAtomicSection () throws Exception {
        class AtomAct extends FileChangeAdapter 
        implements FileSystem.AtomicAction {
            
            private java.awt.Button testSer = new java.awt.Button ();
            
            private FileObject data;
            private InstanceDataObject obj;
            
            public void run () throws IOException {
                folder.getPrimaryFile ().addFileChangeListener (this);
                data = folder.getPrimaryFile ().createData ("SomeData");
                
                
                obj = InstanceDataObject.create (folder, null, testSer, null);
            }
            
            public void doTest () throws Exception {
                Object now = obj.instanceCreate ();
                if (now != testSer) {
                    fail ("Different values. Original: " + testSer + " now: " + now);
                }
            }
            
            public void cleanUp () throws Exception {
                data.delete ();
                obj.delete ();
            }
            
            public void fileDataCreated (FileEvent ev) {
                try {
                    Thread.sleep (500);
                } catch (Exception ex) {
                }
            }
        }

        
        AtomAct t = new AtomAct ();
        try {
            folder.getPrimaryFile().getFileSystem ().runAtomicAction (t);

            t.doTest ();
        } finally {
            t.cleanUp ();
        }
    }

    /** Tests whether createFromTemplate works correctly.
    */
    public void testCreateFromTemplateForSettingsFile () throws Exception {
        Object ser = new java.awt.Button ();

        InstanceDataObject obj = InstanceDataObject.create (folder, "SomeName", ser, null);
        obj.setTemplate (true);

        DataObject newObj = obj.createFromTemplate(folder, "NewName");
        
        if (!newObj.getName().equals ("NewName")) {
            fail ("Wrong name of new data object: " + newObj.getName ());
        }

        InstanceCookie ic = (InstanceCookie)newObj.getCookie (InstanceCookie.class);
        
        if (ic == null) {
            fail ("No instance cookie for " + newObj);
        }

        if (ic.instanceCreate () != ser) {
            fail ("created instance is different than the original in template");
        }
        
        if (ic.instanceCreate () == obj.instanceCreate ()) {
            fail ("Instance of the new object is same as the current of the template");
        }
    }
    
    /** Test if the Lookup reflects IDO' cokie changes. */
    public void testLookupRefreshOfInstanceCookieChanges() throws Exception {
//        Object ser = new java.awt.Button ();
        Object ser = new java.beans.beancontext.BeanContextChildSupport();

        FileObject lookupFO = FileUtil.getConfigFile("/system/Services/lookupTest");
        FileObject systemFO = FileUtil.getConfigFile("/system");
        
        FolderLookup lookup = new FolderLookup(DataFolder.findFolder(systemFO));
        Lookup l = lookup.getLookup();
        DataFolder folderTest = DataFolder.findFolder(lookupFO);
        
        InstanceDataObject ido = InstanceDataObject.create (folderTest, "testLookupRefresh", ser, null);
        Lookup.Result res = l.lookup(new Lookup.Template(ser.getClass()));
        Collection col = res.allInstances ();
        InstanceCookie ic = (InstanceCookie) ido.getCookie(InstanceCookie.class);
        assertEquals("IDO did not create new InstanceCookie", ser, ic.instanceCreate());
        
        Set origSet = new HashSet(Arrays.asList(new Object[] {ser}));
        assertEquals("wrong lookup result", origSet, new HashSet(col));
        
        assertTrue("Lookup is not finished and surprisingly returned a result", lookup.isFinished ());
        
        Object found = col.iterator().next();
        assertEquals("found wrong object instance", ser, found);
        
        // due to #14795 workaround
        Thread.sleep(1000);
        
        // external file change forcing IDO to create new InstanceCookie
        final FileObject fo = ido.getPrimaryFile();
        FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileLock lock = null;
                try {
                    InputStream in = fo.getInputStream();
                    byte[] buf = new byte[(int)fo.getSize()];
                    in.read(buf);
                    in.close();

                    lock = fo.lock();
                    OutputStream out = fo.getOutputStream(lock);
                    out.write(buf);
                    out.write(32);
                    out.flush();
                    out.close();
                    
                } finally {
                    if (lock != null) lock.releaseLock();
                }
            }
        });
        
        col = res.allInstances ();
        ic = (InstanceCookie) ido.getCookie(InstanceCookie.class);
        origSet = new HashSet(Arrays.asList(new Object[] {ic.instanceCreate()}));
        
        assertEquals("wrong lookup result", origSet, new HashSet(col));
        
        found = col.iterator().next();
        assertTrue("IDO did not create new InstanceCookie", ser != ic.instanceCreate());
        assertTrue("Lookup did not refresh changed InstanceCookie", ser != found);
    }
    /*
    private void assertEquals(boolean b1, boolean b2) {
        assertEquals(b1  ? Boolean.TRUE : Boolean.FALSE, b2  ? Boolean.TRUE : Boolean.FALSE);
    }
    */
    /** Checks whether the instance is not saved multiple times.
     *
    public void testMultiSave () throws Exception {
        Ser ser1 = new Ser ("1");
        Ser ser2 = new Ser ("2");
        
        InstanceDataObject i = InstanceDataObject.create (folder, null, ser1, null);
        
        Thread.sleep (3000);
        
        InstanceDataObject j = InstanceDataObject.create (folder, null, ser2, null);
        Thread.sleep (3000);
        
        Object n = i.instanceCreate ();
        if (n != ser1) {
            fail ("instanceCreate is not the same: ");
        }
        i.instanceCreate ();
        j.instanceCreate ();
        j.instanceCreate ();
        
    } */
    
    public static final class Ser extends Object implements Externalizable {
        static final long serialVersionUID = -123456;
        public int deserialized;
        public int serialized;
        private String name;
        
        private int property;
        
        private java.beans.PropertyChangeSupport propertyChangeSupport =  new java.beans.PropertyChangeSupport(this);
        
        public Ser (String name) {
            this.name = name;
        }
        
        public synchronized void readExternal(java.io.ObjectInput objectInput) 
        throws java.io.IOException, java.lang.ClassNotFoundException {
//            System.err.println(name + " deserialized");
            deserialized++;
        }
        
        public synchronized void writeExternal(java.io.ObjectOutput objectOutput) 
        throws java.io.IOException {
//            System.err.println(name + " serialized");
            serialized++;
        }
        
        public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
            propertyChangeSupport.addPropertyChangeListener(l);
        }
        
        public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
            propertyChangeSupport.removePropertyChangeListener(l);
        }
        
        public int getProperty() {
            return this.property;
        }
        
        public void setProperty(int property) {
            int oldProperty = this.property;
            this.property = property;
            propertyChangeSupport.firePropertyChange("property", new Integer(oldProperty), new Integer(property));
        }
        
    }
    
    /** Tests creating .settings file (<code>IDO.create</code>) using parameter
     * <code>create</code>
     */
    public void testCreateSettings() throws Exception {
        FileObject fo = FileUtil.getConfigFile("/testCreateInstance");
        assertNotNull("missing folder /testCreateInstance", fo);
        DataFolder folder = DataFolder.findFolder(fo);
        assertNotNull("cannot find DataFolder /testCreateInstance", folder);
        
        // test non null filename
        String filename = "testCreateSettings";
        Object obj = new javax.swing.JButton();
        InstanceDataObject ido = InstanceDataObject.create(folder, filename, obj, null, false);
        assertNotNull("InstanceDataObject.create cannot return null!", ido);
        
        InstanceDataObject ido2 = InstanceDataObject.create(folder, filename, obj, null, false);
        assertNotNull("InstanceDataObject.create cannot return null!", ido2);
        assertEquals("InstanceDataObject.create(..., false) must reuse existing file: ",
            ido.getPrimaryFile(), ido2.getPrimaryFile());
        
        for (int i = 0; i < 3; i++) {
            ido2 = InstanceDataObject.create(folder, filename, obj, null, true);
            assertNotNull("InstanceDataObject.create cannot return null!", ido2);
            assertTrue("InstanceDataObject.create(..., true) must create new file: "
                + "step: " + i + ", "
                + ido2.getPrimaryFile(), ido.getPrimaryFile() != ido2.getPrimaryFile());
        }
        
        // test null filename
        ido = InstanceDataObject.create(folder, null, obj, null, false);
        assertNotNull("InstanceDataObject.create cannot return null!", ido);
        
        ido2 = InstanceDataObject.create(folder, null, obj, null, false);
        assertNotNull("InstanceDataObject.create cannot return null!", ido2);
        // filename == null => always create new file (ignore create parameter) => backward compatibility
        assertTrue("InstanceDataObject.create(..., false) must create new file: "
            + ido2.getPrimaryFile(), ido.getPrimaryFile() != ido2.getPrimaryFile());
        
        for (int i = 0; i < 3; i++) {
            ido2 = InstanceDataObject.create(folder, null, obj, null, true);
            assertNotNull("InstanceDataObject.create cannot return null!", ido2);
            assertTrue("InstanceDataObject.create(..., true) must create new file: "
                + ido2.getPrimaryFile(), ido.getPrimaryFile() != ido2.getPrimaryFile());
        }
    }
    
    public void testDeleteSettings() throws Exception {
        FileObject root = FileUtil.getConfigRoot();
        DataFolder folder = DataFolder.findFolder(root);
        
        String filename = "testDeleteSettings";
        javax.swing.JButton obj = new javax.swing.JButton();
        InstanceDataObject ido = InstanceDataObject.create(folder, filename, obj, null, false);
        assertNotNull("InstanceDataObject.create cannot return null!", ido);
        
        // test if file object does not remain locked when ido is deleted and
        // the storing is not rescheduled in consequence of the serialization 
        obj.setForeground(java.awt.Color.black);
        Thread.sleep(500);
        ido.delete();
        assertNull(filename + ".settings was not deleted!", root.getFileObject(filename));
        Thread.sleep(3000);
        assertNull(filename + ".settings was not deleted!", root.getFileObject(filename));
        
        filename = "testDeleteSettings2";
        Ser ser = new Ser("bla");
        ido = InstanceDataObject.create(folder, filename, ser, null, false);
        assertNotNull("InstanceDataObject.create cannot return null!", ido);
        
        ser.setProperty(10);
        ido.delete();
        assertNull(filename + ".settings was not deleted!", root.getFileObject(filename));
        Thread.sleep(3000);
        assertNull(filename + ".settings was not deleted!", root.getFileObject(filename));
    }

    public void testDisabledOrUnknownModule() throws Exception {    
        final FileObject valid = FileUtil.getConfigFile("/Settings/org-netbeans-modules-settings-convertors-FooSettingDisabledOrUnknownModule.settings");
        assertNotNull(valid);
        DataObject ido = DataObject.find(valid);
        InstanceCookie ic = (InstanceCookie) ido.getCookie(InstanceCookie.class);
        assertNull("There shouldn't be provided InstanceCookie for disabled module", ic);        
    }

    /** If class name is mapped to an empty string in META-INF.netbeans/translate.names,
     * InstanceCookie should not be created and instanceOf should return false. */
    public void testRemovedClass137240() throws DataObjectNotFoundException {
        FileObject RemovedClassFO = FileUtil.getConfigFile("/Settings/org-netbeans-modules-settings-convertors-FooSettingRemovedClass.settings");
        assertNotNull(RemovedClassFO);
        DataObject ido = DataObject.find(RemovedClassFO);
        InstanceCookie ic = ido.getCookie(InstanceCookie.class);
        assertNull("InstanceCookie issued for removed class.", ic);
        FileObject unknownSerialFO = FileUtil.getConfigFile("/Settings/org-netbeans-modules-settings-convertors-FooSettingRemovedClassSerial.settings");
        assertNotNull(unknownSerialFO);
        ido = DataObject.find(unknownSerialFO);
        InstanceCookie icSerial = ido.getCookie(InstanceCookie.class);
        assertNull("InstanceCookie issued for removed class.", icSerial);
        FileObject unknownInstanceOfFO = FileUtil.getConfigFile("/Settings/org-netbeans-modules-settings-convertors-FooSettingRemovedClassInstanceOf.settings");
        assertNotNull(unknownInstanceOfFO);
        ido = DataObject.find(unknownInstanceOfFO);
        InstanceCookie.Of icOf = ido.getCookie(InstanceCookie.Of.class);
        assertFalse("instanceOf should not return true for removed class.", icOf.instanceOf(RemovedClass.class));
    }

    public void testDeleteOfUnrecognizedSettingsFile () throws Exception {
        final FileObject corrupted = FileUtil.getConfigFile("/Settings/org-netbeans-modules-settings-convertors-FooSettingUnrecognizedSettingsFile.settings");
        assertNotNull(corrupted);
        
        DataObject ido = DataObject.find(corrupted);
        org.openide.nodes.Node node = ido.getNodeDelegate();
        node.destroy();        
        FileObject corrupted2 = FileUtil.getConfigFile("/Settings/org-netbeans-modules-settings-convertors-FooSettingUnrecognizedSettingsFile.settings");
        assertNull(corrupted2);
    }
    
    public void testCorruptedSettingsFile() throws Exception {
        final FileObject corrupted = FileUtil.getConfigFile("/Settings/org-netbeans-modules-settings-convertors-FooSettingSerialDataCorruptedTest.settings");
        assertNotNull(corrupted);
        
        DataObject ido = DataObject.find(corrupted);
        InstanceCookie ic = (InstanceCookie) ido.getCookie(InstanceCookie.class);
        assertNotNull("Missing InstanceCookie", ic);
        
        Object obj = null;
        try {
            obj = ic.instanceCreate();
        } catch (IOException ex) {
        }
        assertNull("corrupted .settings file cannot provide an object", obj);
        
        final FileObject valid = FileUtil.getConfigFile("/Settings/org-netbeans-modules-settings-convertors-FooSettingSerialDataCorruptedTest2.settings");
        assertNotNull(valid);
        
        // simulate revert to default of a corrupted setting object
        corrupted.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileLock l = null;
                OutputStream os = null;
                try {
                    l = corrupted.lock();
                    os = corrupted.getOutputStream(l);
                    FileUtil.copy(valid.getInputStream(), os);
                    os.flush();
                } finally {
                    if (os != null) try { os.close(); } catch (IOException ex) {}
                    if (l != null) l.releaseLock();
                }
            }
        });
        
        ic = (InstanceCookie) ido.getCookie(InstanceCookie.class);
        assertNotNull("Missing InstanceCookie", ic);
        assertNotNull("the persisted object cannot be read", ic.instanceCreate());
    }
    
    public void testFactoryMethod() throws IOException, ClassNotFoundException {
        DataFolder df = DataFolder.findFolder(FileUtil.getConfigRoot().createFolder("testFactoryMethod"));
        FileObject fo = df.getPrimaryFile().createData("test.settings");
        OutputStream os = fo.getOutputStream();
        os.write((
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<!DOCTYPE settings PUBLIC \"-//NetBeans//DTD Session settings 1.0//EN\" \"http://www.netbeans.org/dtds/sessionsettings-1_0.dtd\">\n" +
"<settings version=\"1.0\">\n" +
"  <instance class=\"" + FactoryBase.class.getName() + "\"/>\n" +
"</settings>\n"
        ).getBytes(StandardCharsets.UTF_8));
        os.close();
        
        InstanceCookie ido = DataObject.find(fo).getCookie(InstanceCookie.class);
        FactoryBase fb = (FactoryBase) ido.instanceCreate();
        assertNotNull("Re-created OK!", fb);
    }

    @FactoryMethod("create")
    public static class FactoryBase implements Serializable {
        private FactoryBase() {
            throw new IllegalStateException("Don't call my default constructor");
        }
        
        FactoryBase(boolean ok) {
        }
        
        static FactoryBase create() {
            return new FactoryBase(true);
        }
    }
}
