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

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

import org.openide.filesystems.*;
import org.openide.cookies.*;
import org.openide.util.*;

import org.netbeans.junit.*;
import java.util.Enumeration;

public class FolderInstanceTest extends NbTestCase {
    private Logger err;

    public FolderInstanceTest(java.lang.String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected int timeOut() {
        return 20000;
    }

    protected void setUp () throws Exception {
        MockServices.setServices(Pool.class);
        
        DataLoaderPool pool = DataLoaderPool.getDefault ();
        assertNotNull (pool);
        assertEquals (Pool.class, pool.getClass ());
        
        Pool.setExtra(null);
        
        clearWorkDir ();
        
        err = Logger.getLogger("test." + getName());
        err.info("setUp over: " + getName());
    }

    /** Checks whether only necessary listeners are attached to the objects.
     * Initial object does not have a cookie.
     */
    public void testListenersCountNoCookie () throws Exception {
        doTestListenersCount (false);
    }
        
    /** Because listeners have different code for objects with cookie and 
     * without cookie, we add this utility test and run it twice.
     *
     * @param cookie add cookie or not
     */
    private void doTestListenersCount (boolean cookie) throws Exception {  
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), new String[0]);

        FileObject bb = lfs.findResource("/AA");
        err.info("Found resource: " + bb);
        if (bb != null) {
            bb.delete ();
        }
        err.info("Resource deleted");
        FileObject theFile = FileUtil.createData (lfs.getRoot (), "/AA/A.simple");
        err.info("Found the file: " + theFile);
        bb = FileUtil.createFolder(lfs.getRoot (), "/AA");
        err.info("Found the folder: " + bb);
        assertTrue("Is file", theFile.isData());
        err.info("Confirmed, its the data: " + theFile);
        
        
        DataFolder folder = DataFolder.findFolder (bb);
        

        DataLoader l = DataLoader.getLoader(DataLoaderOrigTest.SimpleUniFileLoader.class);
        err.info("Add loader: " + l);
        Pool.setExtra(l);
        err.info("Loader added");
        try {
            FileObject aa = lfs.findResource("/AA/A.simple");
            DataObject tmp = DataObject.find (aa);
            assertEquals ("Is of the right type", DataLoaderOrigTest.SimpleDataObject.class, tmp.getClass ());
            DataLoaderOrigTest.SimpleDataObject obj = (DataLoaderOrigTest.SimpleDataObject)tmp;
            
            err.info("simple object found: " + obj);
            
            if (cookie) {
                err.info("Adding cookie");
                obj.cookieSet().add (new InstanceSupport.Instance (new Integer (100)));
                err.info("Cookie added");
            }
            
            
            F instance = new F (folder);
            err.info("Instance for " + folder + " created");
            Object result = instance.instanceCreate ();
            err.info("instanceCreate called. Result: " + result);
            
            Enumeration en = obj.listeners ();
            
            err.info("Asking for listeners of " + obj);
            
            assertTrue ("Folder instance should have add one listener", en.hasMoreElements ());
            en.nextElement ();
            assertTrue ("But there should be just one", !en.hasMoreElements ());
            
            err.info("Successfully tested for one listener, creating B.simple");
            
            folder.getPrimaryFile().createData("B.simple");
            err.info("B.simple created");
            assertEquals ("DO created", folder.getChildren ().length, 2);
            err.info("Children obtained correctly");
            
            // wait to finish processing
            result = instance.instanceCreate ();
            err.info("instanceCreate finished, with result: " + result);

            en = obj.listeners ();
            err.info("Asking for listeners once again");
            assertTrue ("Folder instance should not change the amount of listeners", en.hasMoreElements ());
            en.nextElement ();
            assertTrue ("And there still should be just one", !en.hasMoreElements ());
            err.info("Successfully tested for listeners");
        } finally {
            err.info("Clearing data loader");
            Pool.setExtra(null);
            err.info("Loader cleared");
        }
    }

    /** Checks whether folder instance correctly reacts to changes of cookies in data objects.
     */
    @RandomlyFails
    public void testChangeCookie () throws Exception {
        String fsstruct [] = new String [] {
            "AA/A.simple"
        };
        
        
        TestUtilHid.destroyLocalFileSystem (getName());
        err.info("destroyLocalFileSystem");
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);
        err.info("lfs: " + lfs);

        FileObject bb = lfs.findResource("/AA");
        
        DataFolder folder = DataFolder.findFolder (bb);
        

        DataLoader l = DataLoader.getLoader(DataLoaderOrigTest.SimpleUniFileLoader.class);
        err.info("Changing the loader: " + l);
        Pool.setExtra(l);
        err.info("Changing the loader done: " + l);
        
        
        try {
            FileObject aa = lfs.findResource("/AA/A.simple");
            DataObject obj = DataObject.find (aa);
            
            if (! (obj instanceof DataLoaderOrigTest.SimpleDataObject)) {
                fail ("Not instance of desired object");
            }

            err.info("Create instance F");
            F instance = new F (folder);
            err.info("Instance is created");
            
            org.openide.nodes.CookieSet set = ((DataLoaderOrigTest.SimpleDataObject)obj).cookieSet ();
            
            List list;
            list = (List)instance.instanceCreate ();
            if (!list.isEmpty ()) {
                fail ("Should be empty with object with no cookies");
            }
            
            err.info("changing instance in cookie set");
            InstanceSupport.Instance is = new InstanceSupport.Instance (new Integer (100));
            set.add (is);
            err.info("changing instance in cookie set, done");
            
            list = (List)instance.instanceCreate ();
            if (list.isEmpty ()) {
                fail ("Cookie added, should return instance");
            }
            
            err.info("removing instance from the cookie set");        
            set.remove (is);
            err.info("removing instance from the cookie set, done");        
            
            list = (List)instance.instanceCreate ();
            if (!list.isEmpty ()) {
                fail ("Cookie removed should be empty");
            }
            
            err.info("adding again");
            set.add (is);
            list = (List)instance.instanceCreate ();
            if (list.isEmpty ()) {
                fail ("Cookie added again, should return instance");
            }
            err.info("adding again, done");
        } finally {
            err.info("finally: cleaning the Pool");
            Pool.setExtra(null);
            err.info("finally: cleaning the Pool, done");
        }
    }
    
    /** Does FolderInstance react to change of order?
     */
    @RandomlyFails
    public void testChangeOfOrder () throws Exception {
        String fsstruct [] = new String [] {
            "AA/A.simple",
            "AA/B.simple"
        };
        
        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);

        FileObject f = lfs.findResource("/AA");
        
        DataFolder folder = DataFolder.findFolder (f);
        

        DataLoader l = DataLoader.getLoader(DataLoaderOrigTest.SimpleUniFileLoader.class);
        Pool.setExtra(l);
        try {
            FileObject aa = lfs.findResource("/AA/A.simple");
            DataObject objA = DataObject.find (aa);
            FileObject bb = lfs.findResource("/AA/B.simple");
            DataObject objB = DataObject.find (bb);
            
            if (! (objA instanceof DataLoaderOrigTest.SimpleDataObject)) {
                fail ("Not instance of desired object: " + objA);
            }
            if (! (objB instanceof DataLoaderOrigTest.SimpleDataObject)) {
                fail ("Not instance of desired object: " + objB);
            }
            
            folder.setOrder (new DataObject[] { objA, objB });

            F instance = new F (folder);

            {
                org.openide.nodes.CookieSet set = ((DataLoaderOrigTest.SimpleDataObject)objA).cookieSet ();
                InstanceSupport.Instance is = new InstanceSupport.Instance (new Integer (1));
                set.add (is);
            }
            {
                org.openide.nodes.CookieSet set = ((DataLoaderOrigTest.SimpleDataObject)objB).cookieSet ();
                InstanceSupport.Instance is = new InstanceSupport.Instance (new Integer (2));
                set.add (is);
            }
            
            List list;
            list = (List)instance.instanceCreate ();
            assertEquals ("Two integer", 2, list.size ());
            assertEquals ("1 is first", new Integer (1), list.get (0));
            assertEquals ("2 is next", new Integer (2), list.get (1));
            
            folder.setOrder (new DataObject[] { objB, objA });
            
            list = (List)instance.instanceCreate ();
            assertEquals ("Two integer", 2, list.size ());
            assertEquals ("2 is first", new Integer (2), list.get (0));
            assertEquals ("1 is next", new Integer (1), list.get (1));
            
        } finally {
            Pool.setExtra(null);
        }
    }
    
    /** Tests whether correct result is returned when an object is added and removed
     * from the folder.
     */
    @RandomlyFails
    public void testModification () throws Exception {
        String fsstruct [] = new String [] {
            "AA/"
        };
        
        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);

        FileObject bb = lfs.findResource("/AA");
        
        DataFolder folder = DataFolder.findFolder (bb);
        DataFolder subfolder = DataFolder.create (folder, "BB");
        
        modification (new F (folder), folder);
    }
    
    
    /** Tests whether correct result is returned when an object is added and removed
     * from the folder.
     */
    @RandomlyFails
    public void testModificationOnSubfolder () throws Exception {
        String fsstruct [] = new String [] {
            "AA/BB/"
        };
        
        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);

        FileObject bb = lfs.findResource("/AA");
        
        DataFolder folder = DataFolder.findFolder (bb);
        DataFolder subfolder = DataFolder.create (folder, "BB");
        
        modification (new F (folder), subfolder);
    }

    /** Tests whether correct result is returned when an object is added and removed
     * from the folder.
     */
    @RandomlyFails
    public void testModificationOnSubSubfolder () throws Exception {
        String fsstruct [] = new String [] {
            "/AA/BB/CC/DD/EE/FF/GG/HH/II/JJ/KK"
        };
        
        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);

        FileObject bb = lfs.findResource("/AA");
        
        DataFolder folder = DataFolder.findFolder (bb);
        
        Enumeration en = lfs.getRoot ().getChildren (true);
        FileObject fo = null;
        while (en.hasMoreElements ()) {
            FileObject f = (FileObject)en.nextElement ();
            if (f.isFolder ()) {
                fo = f;
            }
        }
        
        DataFolder subfolder = DataFolder.findFolder (fo);
        
        modification (new F (folder), subfolder);
    }
    
    @RandomlyFails
    public void testWhetherRenameTriggersRevalidationOfTheFolderInstance() throws Exception {
        String fsstruct [] = new String [] {
            "/AAXX/OldName.shadow"
        };
        
        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);

        FileObject bb = lfs.findResource("/AAXX");
        assertEquals ("One child", 1, bb.getChildren ().length);

        class NamedF extends F {
            public NamedF (DataFolder f) {
                super (f);
            }
            
            protected InstanceCookie acceptDataObject (DataObject obj) {
                return new InstanceSupport.Instance (obj.getName ());
            }
        }
        
        DataFolder f = DataFolder.findFolder (bb);
        NamedF namedf = new NamedF (f);
        
        List result;
        result = (List)namedf.instanceCreate ();
        if (1 != result.size ()) {
            fail ("One item expected, but: " + result);
        }
        assertEquals ("It is the name of data object", "OldName", result.get (0));
        
        FileObject aa = lfs.findResource (fsstruct[0]);
        DataObject.find (aa).rename ("NewName");
        
        result = (List)namedf.instanceCreate ();
        assertEquals ("One item", 1, result.size ());
        assertEquals ("It is the name of data object", "NewName", result.get (0));
    }
    
    /** Runs modification test on a given folder with provided folder instance.
     */
    private static void modification (F instance, DataFolder folder)
    throws Exception {
        List list;
        int cnt;
        list = (List)instance.instanceCreate ();
        
        if (list.size () != 0) {
            fail ("List should be empty: " + list);
        }
        
        cnt = instance.getCount ();
        if (cnt != 1) {
            fail ("Too many calls to createInstance during initialization: " + cnt);
        }
            
        
        InstanceDataObject obj = InstanceDataObject.create (folder, null, Numb.class);
        
        list = (List)instance.instanceCreate ();
        
        assertEquals ("One item", 1, list.size ());
        assertEquals ("The item is of the right class", Numb.class, list.get (0).getClass ());
        
        cnt = instance.getCount ();
        if (cnt != 1) {
            fail ("Too many calls to createInstance after create: " + cnt);
        }

        obj.delete ();
        
        list = (List)instance.instanceCreate ();
        
        if (list.size () != 0) {
            fail ("List should be empty again: " + list);
        }
        
        cnt = instance.getCount ();
        if (cnt != 1) {
            fail ("Too many calls to createInstance after delete: " + cnt);
        }
        
    }
    

   
    private static class F extends FolderInstance {
        /** count number of changes. */
        private int count;
        
        public F (DataFolder f) {
            super (f);
        }
        
        /** Getter to number of changes of this folder instance.
         */
        public synchronized int getCount () {
            int c = count;
            count = 0;
            return c;
        }
            
        
        /** Accepts folder.
         */
        protected InstanceCookie acceptFolder (DataFolder f) {
            return new F (f);
        }
        
        protected Object createInstance (InstanceCookie[] arr) 
        throws java.io.IOException, ClassNotFoundException {
            synchronized (this) {
                count++;
            }
            LinkedList ll = new LinkedList ();
            for (int i = 0; i < arr.length; i++) {
                Object obj = arr[i].instanceCreate ();
                if (obj instanceof Collection) {
                    ll.addAll ((Collection)obj);
                } else {
                    ll.add (obj);
                }
            }
            return ll;
        }
        protected Task postCreationTask (Runnable run) {
            //super.postCreationTask (run);
            
            run.run ();
            return null;
        }
    }   
    
    /** See #12960.
     * Appears that MenuBar.Folder was being passed already-invalidated objects
     * on occasion, which of course it was not prepared to deal with.
     * @author Jesse Glick
     */
    @RandomlyFails
    public void testFolderInstanceNeverPassesInvObjects() throws Exception {
        doFolderInstanceNeverPassesInvObjects (100, 1000);
    }
    private void doFolderInstanceNeverPassesInvObjects (int cnt, int sleep) throws Exception {
        String[] names = new String[cnt];
        for (int i = 0; i < names.length; i++) {
            names[i] = "folder/file" + i + ".simple";
        }
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), names);
        Repository.getDefault().addFileSystem(lfs);
        try {
            FileObject folder = lfs.findResource("folder");
            DataLoader l = DataLoader.getLoader(DataLoaderOrigTest.SimpleUniFileLoader.class);
            DataFolder f = DataFolder.findFolder(folder);
            InvCheckFolderInstance icfi = new InvCheckFolderInstance(f, false);
            assertTrue(icfi.ok);
            assertEquals(new Integer(0), icfi.instanceCreate());
            err.info("sample1: " + DataObject.find(lfs.findResource(names[0])));
            Pool.setExtra(l);
            try {
                err.info("sample2: " + DataObject.find(lfs.findResource(names[0])));
                assertTrue(icfi.ok);
                /*
                Thread.sleep(100);
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        // just get here
                    }
                });
                Thread.sleep(100);
                System.err.println("sample: " + DataObject.find(lfs.findResource(names[0])));
                 */
                Thread.sleep(sleep);
                assertEquals(new Integer(cnt), icfi.instanceCreate());
                //Thread.sleep(sleep);
                assertTrue(icfi.ok);
                //Thread.sleep(sleep);
                //assertTrue(icfi.ok);
            } finally {
                err.info("begining to clear the pool");
                Pool.setExtra(null);
                err.info("clearing pool is finished");
            }
            err.info("sample3: " + DataObject.find(lfs.findResource(names[0])));
            err.info("sample4: " + DataFolder.findFolder(lfs.findResource(names[0]).getParent ()).getChildren()[0]);
            assertTrue(icfi.ok);
            Object instance = null;
            for (int i = 0; i < 1; i++) {
                Thread.sleep(sleep);
                err.info("getting the instance: " + i);
                instance = icfi.instanceCreate();
                err.info("instance is here (" + i + "): " + instance);
                
                if (new Integer (0).equals (instance)) {
                    break;
                }
            }
            assertEquals(new Integer(0), instance);
            err.info("passed the usual failing point");
            //Thread.sleep(sleep);
            assertTrue(icfi.ok);
            Pool.setExtra(l);
            try {
                assertTrue(icfi.ok);
                Thread.sleep(sleep);
                assertTrue(icfi.ok);
            } finally {
                Pool.setExtra(null);
            }
            assertTrue(icfi.ok);
            Pool.setExtra(l);
            try {
                assertTrue(icfi.ok);
            } finally {
                Pool.setExtra(null);
            }
            assertTrue(icfi.ok);
            Thread.sleep(sleep);
            assertTrue(icfi.ok);
        } finally {
            Repository.getDefault().removeFileSystem(lfs);
        }
    }

    @RandomlyFails
    public void testFolderInstanceNeverPassesInvFolders() throws Exception {
        String[] names = {
            "folder/sub/"
        };
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), names);
        Repository.getDefault().addFileSystem(lfs);
        try {
            FileObject folder = lfs.findResource("folder");
            DataFolder f = DataFolder.findFolder(folder);
        
            DataObject[] arr = f.getChildren();
            assertEquals("One child", 1, arr.length);
            assertEquals("It is folder", DataFolder.class, arr[0].getClass());
            
            err.info("Creating InvCheckFolderInstance");
            InvCheckFolderInstance icfi = new InvCheckFolderInstance(f, true);
            err.info("Computing result");
            List computed = (List)icfi.instanceCreate();
            err.info("Result is here: " + computed);
            assertEquals("One from folder instance", 1, computed.size());
            assertEquals("The same data object", arr[0], computed.get(0));
            
            arr[0].setValid(false);
            
            List newComputed = (List)icfi.instanceCreate();
            assertEquals("Still one", 1, newComputed.size());

            DataObject[] arr2 = f.getChildren();
            assertEquals("Still one", 1, arr2.length);
            if (arr[0] == arr2[0]) {
                fail("They should not be the same: " + arr2[0]);
            }
            
            assertEquals("The same new object", arr2[0], newComputed.get(0));
            
            
        } finally {
            Repository.getDefault().removeFileSystem(lfs);
        }
    }
    
    private final class InvCheckFolderInstance extends FolderInstance {
        public boolean ok = true;
        private boolean acceptF;
        public InvCheckFolderInstance(DataFolder f, boolean folders) {
            super(f);
            this.acceptF = folders;
        }
        
        protected Object createInstance(InstanceCookie[] cookies) throws IOException, ClassNotFoundException {
            // Whatever, irrelevant.
            err.info("new createInstance: " + cookies.length);
            
            if (acceptF) {
                ArrayList list = new ArrayList();
                for (int i = 0; i < cookies.length; i++) {
                    list.add(cookies[i].instanceCreate());
                }
                return list;
            }
            
            return new Integer(cookies.length);
        }
        
        protected InstanceCookie acceptDataObject(DataObject o) {
            if (! o.isValid()) {
                ok = false;
                Thread.dumpStack();
                return null;
            }
            if (o instanceof DataLoaderOrigTest.SimpleDataObject) {
                err.info("got a simpledataobject");
                // Simulate some computation here:
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ie) {}
                return new InstanceSupport.Instance("ignore");
            } else {
                if (acceptF && o instanceof DataFolder) {
                    err.info("Recognized folder: " + o);
                    return new InstanceSupport.Instance(o);
                }
                err.info("got a " + o);
                return null;
            }
        }
        // For faithfulness to the original:
        protected Task postCreationTask (Runnable run) {
            err.info("postCreationTask");
            return new AWTTask (run);
        }
    }
    private final class AWTTask extends Task {
        private boolean executed;
        public AWTTask (Runnable r) {
            super (r);
            Mutex.EVENT.readAccess (this);
        }
        public void run () {
            if (!executed) {
                super.run ();
                executed = true;
                err.info("AWTTask executed");
            }
        }
        public void waitFinished () {
            err.info("AWTTask waitFinished");
            if (SwingUtilities.isEventDispatchThread ()) {
                err.info("AWTTask waitFinished on AWT thread");
                run ();
                err.info("AWTTask waitFinished on AWT thread done");
            } else {
                super.waitFinished ();
                err.info("AWTTask waitFinished done");
            }
        }
    }

    public static final class Numb extends Object implements Serializable {
        public Numb () {
        }
    }
    
    
    
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp() {
            this(new org.openide.util.lookup.InstanceContent());
        }
        
        private Lkp(org.openide.util.lookup.InstanceContent ic) {
            super(ic);
            ic.add(new Pool ());
        }
    }
    
    public static final class Pool extends org.openide.loaders.DataLoaderPool {
        private static DataLoader extra;
        
        
        protected Enumeration loaders () {
            if (getExtra() == null) {
                return Enumerations.empty ();
            } else {
                return Enumerations.singleton (getExtra());
            }
        }

        public static DataLoader getExtra () {
            return extra;
        }

        public static void setExtra (DataLoader aExtra) {
            if (extra != null && aExtra != null) {
                fail ("Both are not null: " + extra + " aExtra: " + aExtra);
            }
            extra = aExtra;
            Pool p = (Pool)DataLoaderPool.getDefault ();
            p.fireChangeEvent (new javax.swing.event.ChangeEvent (p));
        }
    }
}
