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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.openide.loaders;


import java.awt.Button;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.beans.*;
import java.beans.beancontext.BeanContextChildSupport;
import java.io.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Level;
import javax.swing.JButton;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.RandomlyFails;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.modules.ModuleInfo;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.lookup.AbstractLookup;

/**
 * @author Vita Stejskal, Jesse Glick, Jan Pokorsky
 */
public class InstanceDataObjectTest extends NbTestCase {

    /** folder to create instances in */
    private DataFolder folder;
    /** filesystem containing created instances */
    private FileSystem lfs;
    
    /** Creates new DataFolderTest */
    public InstanceDataObjectTest(String name) {
        super (name);
    }
    
    public static Test suite() {
        NbTestSuite s = new NbTestSuite();
        if (!GraphicsEnvironment.isHeadless()) {
            s.addTestSuite(InstanceDataObjectTest.class);
            s.addTestSuite(LkpIDO.class);
        }
        return s;
    }
    
    public static class LkpIDO extends InstanceDataObjectTest {
        public LkpIDO(String s) {
            super(s);
        }
        
        @Override
        protected <T extends Node.Cookie> T getCookie(DataObject obj, Class<T> clazz) {
            return obj.getLookup().lookup(clazz);
        }
        
    }
    
    protected <T extends Node.Cookie> T getCookie(DataObject obj, Class<T> clazz) {
        return obj.getCookie(clazz);
    }
    
    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    
    
    protected void setUp () throws Exception {
        // initialize module layers
        Lookup.getDefault().lookup(ModuleInfo.class);
        
        String fsstruct [] = new String [] {
            "AA/AAA/A1/",
            "BB/AAA/",
            "system/Services/lookupTest/",
            "testCreateInstance/",
            "testFindInstance/",
            "testFindInstance/button[javax-swing-JButton].instance",
            "testFindInstance/button2[java-awt-Button].instance",
            "testFindInstance/javax-swing-JButton.instance",
        };
        
        TestUtilHid.destroyLocalFileSystem (getName());
        clearWorkDir();
        lfs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);

        FileObject bb = lfs.findResource("/BB");
        FileObject aa_aaa = lfs.findResource("/AA/AAA");
        FileObject bb_aaa = lfs.findResource("/BB/AAA");
        
        DataObject src = DataObject.find(aa_aaa);
        DataObject dest = DataObject.find(bb_aaa);
        
        assertTrue("Source folder doesn't exist.", src != null);
        assertTrue("Destination folder doesn't exist.", dest != null);
        assertTrue("Source folder is not valid.", src.isValid ());
        assertTrue("Destination folder is not valid.", dest.isValid ());
        
        folder = DataFolder.findFolder (bb);
        
        FileObject fo = lfs.findResource("/testFindInstance");
        fo.createData("fileWithInstanceClass", "instance").setAttribute("instanceClass", "javax.swing.JButton");
    }

    /** #28118, win sys relies that instance data object fires cookie 
     * changes when its settings file removed, it gets into corruped state otherwise. */
    public void testFiringEventWhenDeleted() throws Exception {
        // Init.
        Lookup.getDefault().lookup(ModuleInfo.class);
        
        FileObject root = FileUtil.getConfigRoot();
        FileObject myFolder = FileUtil.createFolder(root, "My"); // NOI18N
        for (FileObject fo : myFolder.getChildren()) {
            fo.delete();
        }

        
        final InstanceDataObject ido = InstanceDataObject.create(
            DataFolder.findFolder(myFolder),
            "object",
            new Integer(2),
            null);
        System.err.println("Created instance " + ido);
        
        class Listener implements PropertyChangeListener {
            private PropertyChangeEvent evt;
            
            private Exception exception;
            
            public void propertyChange(PropertyChangeEvent evt) {
                System.err.println("Event received->" + evt
                        + " name=" + evt.getPropertyName());
                if(DataObject.PROP_VALID.equals(evt.getPropertyName())) {
                    this.evt = evt;
                    
                    synchronized(ido) {
                        System.err.println("Event came -> notifying");
                        ido.notify();
                    }
                }
            }
        }
        
        final Listener l = new Listener();
        ido.addPropertyChangeListener(l);

        synchronized(ido) {

            // XXX I don't know whether the firing of event should be 
            // synch or not. Anyway giving a chance to instance data object
            // that it needs not to be sych.
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        FileObject primary = ido.getPrimaryFile();
                        System.err.println("Deleting file=" + primary);
                        primary.delete();
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                        l.exception = ioe;
                    }
                }
            });
        
            System.err.println("Waiting for the event with 30 sec timeout");
            ido.wait(30000);
        }
        
        if(l.exception != null) {
            throw l.exception;
        }
        
        ido.removePropertyChangeListener(l);
        
        assertNotNull("PROP_COOKIE change event has to come", l.evt);
    }
    
    /**test creating of instances from .instance files; alternatives with
     * instanceClass/instanceOf/instanceCreate
     */
    public void testInstanceDefinitions() throws Exception {
        FileSystem fs = new XMLFileSystem(this.getClass().getClassLoader().
            getResource("org/openide/loaders/data/InstanceDataObjectTest.xml"));
        
        testParticularInstanceDefinition(fs.findResource("testInstanceDefinitions/a.instance"));
        testParticularInstanceDefinition(fs.findResource("testInstanceDefinitions/b.instance"));
        testParticularInstanceDefinition(fs.findResource("testInstanceDefinitions/c.instance"));
        testParticularInstanceDefinition(fs.findResource("testInstanceDefinitions/d.instance"));
        testParticularInstanceDefinition(fs.findResource("testInstanceDefinitions/e.instance"));
        testParticularInstanceDefinition(fs.findResource(
            "testInstanceDefinitions/org-openide-loaders-InstanceDataObjectTest$TestDefinitions.instance"));
        testParticularInstanceDefinition(fs.findResource(
            "testInstanceDefinitions/hu[org-openide-loaders-InstanceDataObjectTest$TestDefinitions].instance"));
    }
    
    private void testParticularInstanceDefinition(FileObject fo) throws Exception {
        assertNotNull(fo);
        String filename = fo.getNameExt();
        DataObject dobj = DataObject.find(fo);
        InstanceCookie.Of ic = (InstanceCookie.Of) getCookie(dobj, InstanceCookie.Of.class);
        assertNotNull(filename, ic);
        
        assertTrue(filename, ic.instanceOf(Runnable.class));
        assertTrue(filename, ic.instanceOf(TestDefinitions.class));
        
        assertEquals(filename, ic.instanceClass(), TestDefinitions.class);
        assertNotNull(filename, ic.instanceCreate());
    }
    
    public static class TestDefinitions implements Runnable {
        TestDefinitions() {}
        
        static TestDefinitions create() {
            return new TestDefinitions();
        }
        public void run() {
        }
        
    }
    
    /** Checks whether the instance is the same.
     */
    @RandomlyFails // NB-Core-Build #2131, #2136 (both in IDOT and IDOT.LkpIDO)
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
        Object ser = new Button();
        
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
        
        InstanceCookie ic = (InstanceCookie)getCookie (obj, InstanceCookie.class);
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
        class Work extends FileChangeAdapter 
        implements FileSystem.AtomicAction {
            
            private Button testSer = new Button ();
            
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

        
        Work t = new Work ();
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
        Object ser = new Button();

        InstanceDataObject obj = InstanceDataObject.create (folder, "SomeName", ser, null);
        obj.setTemplate (true);

        DataObject newObj = obj.createFromTemplate(folder, "NewName");
        
        if (!newObj.getName().equals ("NewName")) {
            fail ("Wrong name of new data object: " + newObj.getName ());
        }

        InstanceCookie ic = (InstanceCookie)getCookie (newObj, InstanceCookie.class);
        
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
    
    /** Tests whether handleCopy works correctly.
    */
    public void testHandleCopyForSettingsFile () throws Exception {
        Object ser = new Button();

        InstanceDataObject obj = InstanceDataObject.create (folder, null, ser, null);
        
        InstanceCookie icOrig = (InstanceCookie) getCookie (obj, InstanceCookie.class);
        assertNotNull("No instance cookie for " + obj, icOrig);
        assertEquals("created instance is different from the original", ser, icOrig.instanceCreate());
        
        DataObject newObj = obj.copy(folder);
        
        InstanceCookie ic = (InstanceCookie) getCookie (newObj, InstanceCookie.class);
        
        assertNotNull("No instance cookie for " + newObj, ic);
        assertTrue("created instance is same as the original", ic.instanceCreate() != icOrig.instanceCreate());
        
    }
    
    /** Test if the Lookup reflects IDO' cokie changes. */
    public void testLookupRefreshOfInstanceCookieChanges() throws Exception {
//        Object ser = new Button();
        Object ser = new BeanContextChildSupport();

        FileObject lookupFO = lfs.findResource("/system/Services/lookupTest");
        FileObject systemFO = lfs.findResource("/system");
        
        FolderLookup lookup = new FolderLookup(DataFolder.findFolder(systemFO));
        Lookup l = lookup.getLookup();
        DataFolder folderTest = DataFolder.findFolder(lookupFO);
        
        InstanceDataObject ido = InstanceDataObject.create (folderTest, "testLookupRefresh", ser, null);
        Collection col = l.lookupAll(ser.getClass());
        InstanceCookie ic = (InstanceCookie) getCookie(ido, InstanceCookie.class);
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
        lfs.runAtomicAction(new FileSystem.AtomicAction() {
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
        
        col = l.lookupAll(ser.getClass());
        ic = (InstanceCookie) getCookie(ido, InstanceCookie.class);
        origSet = new HashSet(Arrays.asList(new Object[] {ic.instanceCreate()}));
        
        assertEquals("wrong lookup result", origSet, new HashSet(col));
        
        found = col.iterator().next();
        assertTrue("IDO did not create new InstanceCookie", ser != ic.instanceCreate());
        assertTrue("Lookup did not refresh changed InstanceCookie", ser != found);
    }
    
    /** Test method InstanceDataObject.create producing .instance files. */
    public void testCreateInstance () throws Exception {
        FileObject fo = lfs.findResource("/testCreateInstance");
        assertNotNull("missing folder /testCreateInstance", fo);
        DataFolder folder = DataFolder.findFolder(fo);
        assertNotNull("cannot find DataFolder /testCreateInstance", folder);
        String filename = null;
        InstanceDataObject ido = InstanceDataObject.create(folder, filename, "javax.swing.JButton");
        assertEquals("created wrong filename: ", "javax-swing-JButton", ido.getName());
        InstanceDataObject ido2 = InstanceDataObject.create(folder, filename, "javax.swing.JButton");
        assertEquals("creating the same instance failed", ido, ido2);
        InstanceCookie ic = (InstanceCookie) getCookie(ido, InstanceCookie.class);
        assertEquals("wrong classname: ", "javax.swing.JButton", ic.instanceClass().getName());
        
        filename = "new file";
        ido = InstanceDataObject.create(folder, filename, "javax.swing.JButton");
        assertEquals("created wrong filename: ", filename, ido.getName());
        ido2 = InstanceDataObject.create(folder, filename, "javax.swing.JButton");
        assertEquals("creating the same instance failed", ido, ido2);
        ic = (InstanceCookie) getCookie(ido, InstanceCookie.class);
        assertEquals("wrong classname: ", "javax.swing.JButton", ic.instanceClass().getName());
    }
    
    /** Test method InstanceDataObject.find searching .instance files. */
    public void testFindInstance () throws Exception {
        FileObject fo = lfs.findResource("/testFindInstance");
        assertNotNull("missing folder /testFindInstance", fo);
        DataFolder folder = DataFolder.findFolder(fo);
        assertNotNull("cannot find DataFolder /testFindInstance", folder);
        
        InstanceDataObject ido = InstanceDataObject.find(folder, "button2", "java.awt.Button");
        assertNotNull("ido not found: 'button2'", ido);
        assertEquals("found wrong ido", "button2", ido.getName());
        InstanceCookie ic = (InstanceCookie) getCookie(ido, InstanceCookie.class);
        assertEquals("found ido with wrong classname", "java.awt.Button", ic.instanceClass().getName());
        
        ido = InstanceDataObject.find(folder, "button", "javax.swing.JButton");
        assertNotNull("ido not found: 'button'", ido);
        assertEquals("found wrong ido", "button", ido.getName());
        ic = (InstanceCookie) getCookie(ido, InstanceCookie.class);
        assertEquals("found ido with wrong classname", "javax.swing.JButton", ic.instanceClass().getName());
        
        String renamed = "renamed_button";
        ido.rename(renamed);
        assertNotNull("missing IDO.EA_NAME in " + ido.getPrimaryFile(), ido.getPrimaryFile().getAttribute("name"));
        ido = InstanceDataObject.find(folder, renamed, "javax.swing.JButton");
        assertNotNull("ido not found: " + ido.getName(), ido);
        assertEquals("found wrong ido", renamed, ido.getName());
        ic = (InstanceCookie) getCookie(ido, InstanceCookie.class);
        assertEquals("found ido with wrong classname", "javax.swing.JButton", ic.instanceClass().getName());
        
        ido = InstanceDataObject.find(folder, "button", "javax.swing.JButton");
        assertEquals("ido should not be found: ", null, ido);
        
        ido = InstanceDataObject.find(folder, "fileWithInstanceClass", "javax.swing.JButton");
        assertNotNull("ido not found: 'fileWithInstanceClass'", ido);
        assertEquals("found wrong ido", "fileWithInstanceClass", ido.getName());
        ic = (InstanceCookie) getCookie(ido, InstanceCookie.class);
        assertEquals("found ido with wrong classname", "javax.swing.JButton", ic.instanceClass().getName());
        
        ido = InstanceDataObject.find(folder, null, "javax.swing.JButton");
        assertNotNull("ido not found 'javax-swing-JButton'", ido);
        assertEquals("found wrong ido", "javax-swing-JButton", ido.getName());
        ic = (InstanceCookie) getCookie(ido, InstanceCookie.class);
        assertEquals("found ido with wrong classname", "javax.swing.JButton", ic.instanceClass().getName());
        
        ido = InstanceDataObject.find(folder, null, "java.awt.Button");
        assertEquals("ido should not be found: ", null, ido);
    }

    /** Test method InstanceDataObject.remove removing .instance files. */
    public void testRemoveInstance () throws Exception {
        FileObject fo = lfs.findResource("/testFindInstance");
        assertNotNull("missing folder /testFindInstance", fo);
        DataFolder folder = DataFolder.findFolder(fo);
        assertNotNull("cannot find DataFolder /testFindInstance", folder);
        
        assertEquals(InstanceDataObject.remove(folder, "button2", "java.awt.Button"), true);
        assertEquals(InstanceDataObject.remove(folder, "button2", "java.awt.Button"), false);
        
        InstanceDataObject ido = InstanceDataObject.find(folder, "button", "javax.swing.JButton");
        String renamed = "renamed_button";
        ido.rename(renamed);
        assertEquals(InstanceDataObject.remove(folder, renamed, "javax.swing.JButton"), true);
        assertEquals(InstanceDataObject.remove(folder, renamed, "javax.swing.JButton"), false);
        
        assertEquals(InstanceDataObject.remove(folder, "fileWithInstanceClass", "javax.swing.JButton"), true);
        assertEquals(InstanceDataObject.remove(folder, "fileWithInstanceClass", "javax.swing.JButton"), false);
        
        assertEquals(InstanceDataObject.remove(folder, null, "javax.swing.JButton"), true);
        assertEquals(InstanceDataObject.remove(folder, null, "javax.swing.JButton"), false);
    }

    @SuppressWarnings("unchecked")
    public void testClassCastIssue91694() throws Exception {
        FileObject fo = lfs.findResource("/testFindInstance");
        assertNotNull("missing folder /testFindInstance", fo);
        DataFolder folder = DataFolder.findFolder(fo);
        assertNotNull("cannot find DataFolder /testFindInstance", folder);

        JButton b = new JButton();
        
        
        InstanceDataObject ido = InstanceDataObject.create(folder, "MyButtonek", b, null);
        
        Class c = Object.class;
        Object res = getCookie(ido, c);
        assertNotNull("Some object found", res);
        assertEquals("This finds InstanceDataObject", InstanceDataObject.class, res.getClass());
    }
    
    public void testWhenAFileInToolsOptionsIsRevertedTheSettingIsUpdatedIssue20962 () throws Exception {
        TestUtilHid.destroyLocalFileSystem (getName() + "2");
        String[] fsstruct = {
            "system/Services/lookupTest/",
        };
        FileSystem sndlfs = TestUtilHid.createLocalFileSystem (new File (getWorkDir(), "2"), fsstruct);
        

        Object x = new Integer (10);
        String path;
        {
            FileObject lookupFO = lfs.findResource("/system/Services/lookupTest");
            DataFolder folderTest = DataFolder.findFolder(lookupFO);
            InstanceDataObject ido = InstanceDataObject.create (folderTest, "testLookupRefresh", x, null);
            path = ido.getPrimaryFile ().getPath ();
            WeakReference ref = new WeakReference (ido);
            ido = null;
            assertGC ("disappear", ref);
        }

        Object y = new Integer (11);
        FileObject lookupFO;
        {
            lookupFO = sndlfs.findResource("/system/Services/lookupTest");
            DataFolder folderTest = DataFolder.findFolder(lookupFO);
            InstanceDataObject ido = InstanceDataObject.create (folderTest, "testLookupRefresh", y, null);
            
            assertEquals (
                "The same path", path, 
                ido.getPrimaryFile ().getPath ()
            );
            WeakReference ref = new WeakReference (ido);
            ido = null;
            assertGC ("disappear", ref);
        }
        
        MultiFileSystem mfs = new MultiFileSystem (new FileSystem[] {
            sndlfs,
            lfs,
        });
        
        FileObject file = mfs.findResource (path);
        assertNotNull ("File is really found", file);
        
        InstanceDataObject ido = (InstanceDataObject)DataObject.find (file);
        
        InstanceCookie ic = (InstanceCookie)getCookie (ido, InstanceCookie.class);
        assertNotNull ("Has cookie", ic);
        assertEquals ("And its value is y", y, ic.instanceCreate ());
        

        lookupFO.delete ();
        
        ic = (InstanceCookie)getCookie (ido, InstanceCookie.class);
        assertNotNull ("Has cookie", ic);
        assertEquals ("And its value is x", x, ic.instanceCreate ());
    }

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
        public int listenerCount;
        private String name;
        
        private int property;
        
        private PropertyChangeSupport propertyChangeSupport =  new PropertyChangeSupport(this);
        
        public Ser (String name) {
            this.name = name;
        }
        
        public synchronized void readExternal(ObjectInput objectInput) 
        throws IOException, ClassNotFoundException {
//            System.err.println(name + " deserialized");
            deserialized++;
        }
        
        public synchronized void writeExternal(ObjectOutput objectOutput) 
        throws IOException {
//            System.err.println(name + " serialized");
            serialized++;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
            listenerCount++;
            propertyChangeSupport.addPropertyChangeListener(l);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener l) {
            listenerCount--;
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
    
    /** Test filename escaping and unescaping functionality.
     * @see "#16479"
     */
    public void testEscapeAndUnescape() throws Exception {
        // First make sure plain things are not escaped.
        assertEquals("hello-x_99", InstanceDataObject.escape("hello-x_99"));
        assertEquals("hello-x_99", InstanceDataObject.unescape("hello-x_99"));
        // That high bits are.
        assertEquals("x#ABCDy", InstanceDataObject.escape("x\uABCDy"));
        // Now that the operation is symmetric.
        String[] names = new String[] {
            "hello",
            "back \\ slash",
            "control\ncharacters",
            "test #1",
            "\u0158ekni `\u0159' dob\u0159e!",
            "maybe this is Chinese: \u1234\u5678\uABCD?",
            "junk1: !@#$%^&*",
	    "junk2: ()-_=+[]{}",
	    "junk3: #~;:'\"/?",
	    "junk4: .>,<\\|`~",
            " ...ahem.",
            "trailing: ",
            "too many  internal",
        };
        for (int i = 0; i < names.length; i++) {
            assertEquals(names[i], InstanceDataObject.unescape(InstanceDataObject.escape(names[i])));
        }
        // Now the fun part: check that the filesystem can actually store the filenames
        // we are making. For best results, try this on different OS's.
        clearWorkDir();
        File dir = new File(getWorkDir(), "namesdir");
        dir.mkdirs();
        for (int i = 0; i < names.length; i++) {
            String name = InstanceDataObject.escape(names[i]);
            //System.err.println("testing: " + name);
            File x = new File(dir, name);
            try {
                assertTrue(x.createNewFile());
            } catch (IOException ioe) {
                ioe.printStackTrace();
                fail("could not make file named '" + name + "'");
            }
            x = new File(dir, name);
            assertTrue("acknowledges existence of a file named '" + name + "'", x.isFile());
            String[] children = dir.list();
            assertEquals("correctly read filename '" + name + "'",
                Arrays.asList(new String[] {name}),
                Arrays.asList(children));
            assertTrue("can delete a file named '" + name + "'", x.delete());
        }
        dir.delete();
    }

    /** Test very long filename escaping functionality.
     * @see "#17186"
     */
    public void testEscapeAndCut() throws Exception {
        // First make sure plain things are not escaped and not cut.
        assertEquals("hello-x_99", InstanceDataObject.escapeAndCut("hello-x_99"));
        // That high bits are.
        assertEquals("x#ABCDy", InstanceDataObject.escapeAndCut("x\uABCDy"));
        // Now that the operation is symmetric.
        char[] charBuf = new char[100];
        Arrays.fill(charBuf, 'a');
        String[] names = new String[2];
        names[0] = new String(charBuf);
        charBuf[50] = 'b';
        names[1] = new String(charBuf);
        String ename0 = InstanceDataObject.escapeAndCut(names[0]);
        String ename1 = InstanceDataObject.escapeAndCut(names[1]);
        assertNotNull("Cutting failed: src: " + names[0] + " dst: " + ename0, ename0);
        assertNotNull("Cutting failed: src: " + names[1] + " dst: " + ename1, ename1);
        assertTrue("Unchanged: " + names[0], !names[0].equals(ename0));
        assertTrue("Unchanged: " + names[1], !names[1].equals(ename1));
        assertTrue("Cutting failed: src: " + names[0] + " dst: " + ename0 +
            ", src: " + names[1] + " dst: " + ename1, !ename0.equals(ename1));
        
        //#203980 - if the name is too long it may be truncated in the middle of an escaped char
        assertEquals("MultiView-makefile3DFFC0ADtext#002Ehistory#007C", InstanceDataObject.escapeAndCut("MultiView-makefile.source|text.history|"));
        assertEquals("MultiView-makefile3DFFC0ADtext.history|", InstanceDataObject.unescape( "MultiView-makefile3DFFC0ADtext#002Ehistory#007C"));
        // Now the fun part: check that the filesystem can actually store the filenames
        // we are making. For best results, try this on different OS's.
        clearWorkDir();
        File dir = new File(getWorkDir(), "namesdir");
        dir.mkdirs();
        for (int i = 0; i < names.length; i++) {
            String name = InstanceDataObject.escapeAndCut(names[i]);
            //System.err.println("testing: " + name);
            File x = new File(dir, name);
            try {
                assertTrue(x.createNewFile());
            } catch (IOException ioe) {
                ioe.printStackTrace();
                fail("could not make file named '" + name + "'");
            }
            x = new File(dir, name);
            assertTrue("acknowledges existence of a file named '" + name + "'", x.isFile());
            String[] children = dir.list();
            assertEquals("correctly read filename '" + name + "'",
                Arrays.asList(new String[] {name}),
                Arrays.asList(children));
            assertTrue("can delete a file named '" + name + "'", x.delete());
        }
        dir.delete();
    }
    
    /** Tests creating .settings file (<code>IDO.create</code>) using parameter
     * <code>create</code>
     */
    public void testCreateSettings() throws Exception {
        FileObject fo = lfs.findResource("/testCreateInstance");
        assertNotNull("missing folder /testCreateInstance", fo);
        DataFolder folder = DataFolder.findFolder(fo);
        assertNotNull("cannot find DataFolder /testCreateInstance", folder);
        
        // test non null filename
        String filename = "testCreateSettings";
        Object obj = new JButton();
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
    
    /** Tests instanceOf attribute.
     */
    public void testInstanceOfAttribute () throws Exception {
        FileObject fo = FileUtil.createData (lfs.getRoot (), "BB/AAA/X.instance");
        fo.setAttribute ("instanceClass", "java.lang.Number");
        fo.setAttribute ("instanceCreate", new Long (1L));
        fo.setAttribute ("instanceOf", "java.lang.Object,java.lang.Long");
        
        DataObject obj = DataObject.find (fo);
        assertNotNull("Object found", obj);
        
        InstanceCookie.Of c = (InstanceCookie.Of)getCookie(obj, InstanceCookie.Of.class);
        assertNotNull ("Cookie found", c);
        
        assertTrue("Instance of object", c.instanceOf(Object.class));
        assertTrue("Not declared to be Serializable", !c.instanceOf(Serializable.class));
        assertTrue("Declared to be also Long", c.instanceOf(Long.class));
        assertTrue("Nobody knows about it being number", !c.instanceOf(Number.class));
        
        assertEquals ("Class is defined to be Number", Number.class, c.instanceClass());
        Object o = c.instanceCreate ();
        assertTrue ("It is a long", o instanceof Long);
        assertEquals ("It is 1", 1, ((Long)o).intValue());
    }
    
    @RandomlyFails // NB-Core-Build #2131, #2136 (both in IDOT and IDOT.LkpIDO)
    public void testDeleteSettings() throws Exception {
        FileObject root = lfs.getRoot();
        DataFolder folder = DataFolder.findFolder(root);
        
        String filename = "testDeleteSettings";
        JButton obj = new JButton();
        InstanceDataObject ido = InstanceDataObject.create(folder, filename, obj, null, false);
        assertNotNull("InstanceDataObject.create cannot return null!", ido);
        
        // test if file object does not remain locked when ido is deleted and
        // the storing is not rescheduled in consequence of the serialization 
        obj.setForeground(Color.black);
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
    
    @RandomlyFails // NB-Core-Build #2131, #2136 (both in IDOT and IDOT.LkpIDO)
    public void testDeleteSettingsRemovesListener() throws Exception {
        FileObject root = lfs.getRoot();
        DataFolder folder = DataFolder.findFolder(root);
        
        String filename = "testDeleteSettings3";
        Ser ser = new Ser("bla");
        InstanceDataObject ido = InstanceDataObject.create(folder, filename, ser, null, false);
        assertNotNull("InstanceDataObject.create cannot return null!", ido);
        assertTrue("Listener not attached!", ser.listenerCount == 1);
        ido.delete();
        assertTrue("Listener remained attached!", ser.listenerCount == 0);
    }
    
    public void testCookieChangeEvent () throws Exception {        
        FileObject fo = FileUtil.createData (lfs.getRoot (), "GG/AAA/X.settings");
        //assertTrue (fo.getFileSystem().toString(), fo.getFileSystem().isDefault());
        DataObject newSetting = InstanceDataObject.create((DataFolder)DataFolder.find(fo.getParent()),"myString","myString", null);
        
        final List events = new ArrayList();
        
        DataObject obj = DataObject.find (fo);
        assertNotNull(obj);
        Object res = getCookie(obj, InstanceCookie.class);
        if (this instanceof LkpIDO) {
            assertEquals("Cannot hide content of lookup", obj, res);
        } else {
            assertEquals ("will be null", null, res);
        }
        

        obj.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(DataObject.PROP_COOKIE)) {
                    events.add(evt);    
                }                
            }
        });

        copySetting(fo, newSetting.getPrimaryFile());


        InputStream is = fo.getInputStream();
        byte[] all = new byte[is.available()];
        is.read(all);
        is.close();

        assertNotNull (getCookie(obj, InstanceCookie.class));        
        assertEquals (new String (all),1, events.size()); 
    }

    @RandomlyFails // NB-Core-Build #2131, #2136 (both in IDOT and IDOT.LkpIDO)
    public void testSerFileChange () throws Exception {
        FileObject fo = FileUtil.createData (lfs.getRoot (), "HH/BBB/Y.ser");
        FileObject services = FileUtil.createData (FileUtil.getConfigRoot (), "Services");
        assertNotNull (services);
        assertTrue (services.isFolder());
        SerEnvironmentProvider environmentProvider = new SerEnvironmentProvider();
        assertNotNull (InstanceDataObject.create((DataFolder)DataFolder.find(services),"serprovider", environmentProvider,null ));
        Object obj = new ArrayList ();
        
        writeObject(fo, obj);
        DataObject ser = DataObject.find(fo);
        InstanceCookie ic = (InstanceCookie)getCookie(ser, InstanceCookie.class);            

        
        assertNotNull (ic);
        Object instance = ic.instanceCreate();
        assertNotNull (instance);
        assertEquals (instance.getClass(), ArrayList.class);
        assertEquals (((ArrayList)instance).size(), 0);
        

        ((ArrayList)obj).add("element");
        Thread.sleep(2000);
        writeObject(fo, obj);
        ic = (InstanceCookie)getCookie(ser, InstanceCookie.class);            
        assertNotNull (ic);        
        Object instance2 = ic.instanceCreate();
        assertNotNull (instance2);
        assertEquals (instance2.getClass(), ArrayList.class);
        assertEquals (((ArrayList)instance2).size(), 1);        

        environmentProvider.assertUsage();        
        
    }
    
    private static class SerEnvironmentProvider implements Environment.Provider, Serializable {
        private boolean failure = false;
        public void assertUsage () {
            if (failure) {
                fail ("InstanceDataObject has hardcoded impl. for *.ser files and no EnvironmentProvider should be called.");
            }
        }
        public Lookup getEnvironment(DataObject obj) {
            failure = true;
            return new AbstractLookup (new AbstractLookup.Content ());
        }
    }

    private void writeObject(FileObject fo, Object obj) throws IOException {
        FileLock fLock = fo.lock();
        OutputStream os = null;
        
        try {
            os = fo.getOutputStream(fLock);
            ObjectOutputStream  obs = new ObjectOutputStream (os);
            obs.writeObject(obj);                        
        } finally {
            if (os != null) os.close();
            if (fLock != null) fLock.releaseLock();            
        }
    }
    
    public void testHelp() throws Exception {
        FileObject fo = FileUtil.createData(lfs.getRoot(), "bla/bla/hle/X.settings");
        InstanceDataObject ico = (InstanceDataObject)DataObject.find(fo);
        HelpCtx x = ico.getHelpCtx();
        assertNotNull("This used to throw stack overflow, issue 68715", x);
        
    }

    public void testCookieChangeEvent2 () throws Exception {        
        FileObject fo = FileUtil.createData (lfs.getRoot (), "GG/AAA/X.settings");
        //assertTrue (fo.getFileSystem().toString(), fo.getFileSystem().isDefault());
        DataObject newSetting = InstanceDataObject.create((DataFolder)DataFolder.find(fo.getParent()),"myString","myString", null);
        copySetting(fo, newSetting.getPrimaryFile());        
        final List events = new ArrayList();
        
        DataObject obj = DataObject.find (fo);
        assertNotNull(obj);
        assertNotNull (getCookie(obj, InstanceCookie.class));
        

        obj.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(DataObject.PROP_COOKIE)) {
                    events.add(evt);    
                }                
            }
        });
        newSetting = InstanceDataObject.create((DataFolder)DataFolder.find(fo.getParent()),"myString2","myString2", null);
        copySetting(fo, newSetting.getPrimaryFile());


        InputStream is = fo.getInputStream();
        byte[] all = new byte[is.available()];
        is.read(all);
        is.close();

        assertNotNull (getCookie(obj, InstanceCookie.class));        
        assertEquals (new String (all),2, events.size()); 
    }

    private void copySetting(FileObject fo, FileObject primaryFile) throws IOException {
        FileLock fLock = fo.lock();

        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            outputStream = fo.getOutputStream(fLock);
            inputStream = primaryFile.getInputStream();
            FileUtil.copy(inputStream, outputStream);            
        } finally {
            if (fLock != null) fLock.releaseLock();  
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();            
        }
    }
}
