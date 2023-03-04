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

package org.openide.loaders;


import org.openide.filesystems.*;
import java.io.IOException;
import java.util.*;
import org.netbeans.junit.*;
import java.beans.PropertyChangeListener;

/** Test basic functionality of data loaders.
 * @author Jesse Glick
 */
public class DataLoaderOrigTest extends NbTestCase {

    public DataLoaderOrigTest(String name) {
        super(name);
    }
    
    protected void setUp() throws IOException {
        clearWorkDir();
    }

    public void testSimpleLoader() throws Exception {
        DataLoader l = DataLoader.getLoader(SimpleUniFileLoader.class);
        DataLoaderPool pool;
        AddLoaderManuallyHid.addRemoveLoader(l, true);
        try {
            pool = DataLoaderPool.getDefault ();
            assertTrue(Arrays.asList(pool.toArray()).contains(l));
            FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir (), new String[] {
                "folder/file.simple",
            });
            FileObject fo = lfs.findResource("folder/file.simple");
            assertNotNull(fo);
            DataObject dob = DataObject.find(fo);
            assertEquals(SimpleDataObject.class, dob.getClass());
        } finally {
            AddLoaderManuallyHid.addRemoveLoader(l, false);
        }
        assertFalse(Arrays.asList(pool.toArray()).contains(l));
        TestUtilHid.destroyLocalFileSystem(getName());
    }

    /** Test for bugfix #23065
     */
    public void testDataObjectFind() throws Exception {
        DataLoader l = DataLoader.getLoader(SimpleUniFileLoader.class);
        DataLoaderPool pool;
        AddLoaderManuallyHid.addRemoveLoader(l, true);
        try {
            pool = DataLoaderPool.getDefault ();
            assertTrue(Arrays.asList(pool.toArray()).contains(l));
            FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir (), new String[] {
                "folder/file.simple",
            });
            FileObject fo = lfs.findResource("folder/file.simple");
            assertNotNull(fo);
            
            DataObject jdo = DataObject.find(fo);
            for (int i = 0; i < 5000; i++) {
                FileObject primary = jdo.getPrimaryFile();
                jdo.setValid(false);
                jdo = DataObject.find(primary);
                assertNotNull(jdo);
                assertTrue(jdo.isValid());
            }
            
        } finally {
            AddLoaderManuallyHid.addRemoveLoader(l, false);
        }
        TestUtilHid.destroyLocalFileSystem(getName());
    }
    
    public static final class SimpleUniFileLoader extends UniFileLoader {
        public SimpleUniFileLoader() {
            super(SimpleDataObject.class.getName());
        }
        protected void initialize() {
            super.initialize();
            getExtensions().addExtension("simple");
        }
        protected String displayName() {
            return "Simple";
        }
        protected MultiDataObject createMultiObject(FileObject pf) throws IOException {
            return new SimpleDataObject(pf, this);
        }
    }
    public static final class SimpleDataObject extends MultiDataObject {
        private ArrayList supp = new ArrayList ();
        
        public SimpleDataObject(FileObject pf, MultiFileLoader loader) throws IOException {
            super(pf, loader);
        }
        
        /** Access method to modify cookies 
         * @return cookie set of this data object
         */
        public final org.openide.nodes.CookieSet cookieSet () {
            return getCookieSet ();
        }
        
        /** Getter for list of listeners attached to the data object.
         */
        public final Enumeration listeners () {
            return Collections.enumeration (supp);
        }
        
        public void addPropertyChangeListener (PropertyChangeListener l) {
            super.addPropertyChangeListener (l);
            supp.add (l);
        }

        public void removePropertyChangeListener (PropertyChangeListener l) {
            super.removePropertyChangeListener (l);
            supp.remove (l);
        }        
    }
    
    /** Test that finding a two-part object (here folder + file, with folder prim)
     * works reliably regardless of timing, if both parts in fact exist at the time
     * DataObject.find is called. Important for XML window system.
     * @see "#15928"
     * @author Jesse Glick
     */
    public void testFindTwoPart() throws Exception {
        DataLoader l = DataLoader.getLoader(TwoPartLoader.class);
        AddLoaderManuallyHid.addRemoveLoader(l, true);
        try {
            FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir (), new String[] {
                "folder/part/",
                "folder/part.ext",
                "folder/trash.txt",
            });
            // This method will be the easy version, harder versions below.
            FileObject fo = lfs.findResource("folder/part");
            assertEquals(TwoPartObject.class, DataObject.find(fo).getClass());
        } finally {
            AddLoaderManuallyHid.addRemoveLoader(l, false);
        }
        TestUtilHid.destroyLocalFileSystem(getName());
    }
    
//    /** Known to fail frequently. */
//    public void testFindTwoPart1() throws Exception {
//        abstractTestFindTwoPart(false, false, false);
//    }
//    /** Not known to fail, but not very surprising if it does. */
//    public void testFindTwoPart2() throws Exception {
//        abstractTestFindTwoPart(false, false, true);
//    }
//    /** Not known to fail, but not very surprising if it does. */
//    public void testFindTwoPart3() throws Exception {
//        abstractTestFindTwoPart(false, true, false);
//    }
//    /** Not known to fail, but not very surprising if it does. */
//    public void testFindTwoPart4() throws Exception {
//        abstractTestFindTwoPart(false, true, true);
//    }
//    /** Known to fail frequently. */
//    public void testFindTwoPart5() throws Exception {
//        abstractTestFindTwoPart(true, false, false);
//    }
    /** Hopefully will not fail: supposed to be safest option. */
    public void testFindTwoPart6() throws Exception {
        abstractTestFindTwoPart(true, false, true);
    }
//    /** Known to fail occasionally. */
//    public void testFindTwoPart7() throws Exception {
//        abstractTestFindTwoPart(true, true, false);
//    }
//    /** Known to fail occasionally. */
//    public void testFindTwoPart8() throws Exception {
//        abstractTestFindTwoPart(true, true, true);
//    }
    
    private static final boolean DEBUG = false;
    private void abstractTestFindTwoPart(final boolean atomic, final boolean makePrimFirst, final boolean findPrim) throws Exception {
        if (DEBUG) System.err.println("atomic=" + atomic + " makePrimFirst=" + makePrimFirst + " findPrim=" + findPrim);
        DataLoader l = DataLoader.getLoader(TwoPartLoader.class);
        AddLoaderManuallyHid.addRemoveLoader(l, true);
        try {
            TestUtilHid.destroyLocalFileSystem(getName());
            FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir (), new String[] {
                "folder/trash.txt",
            });
            final FileObject filefolder = lfs.findResource("folder");
            final boolean[] stop = new boolean[] {false};
            Thread t = new Thread("recognizing objects sometimes") {
                @Override
                public void run() {
                    try {
                        DataFolder folder = DataFolder.findFolder(filefolder);
                        for (int delay = 0; !stop[0]; delay++) {
                            Thread.sleep(delay * 10);
                            switch (delay % 3) {
                            case 0:
                                DataObject[] kids = folder.getChildren();
                                if (DEBUG) System.err.println("got children; " + kids.length);
                                break;
                            case 1:
                                FileObject fo = filefolder.getFileObject("part");
                                if (fo != null) {
                                    // Don't particularly care what the result is
                                    // here; mainly just want to make the folder
                                    // recognizer do something in hopes of clashing
                                    // with the main thread.
                                    DataObject o = DataObject.find(fo);
                                    if (DEBUG) System.err.println("from folder found: " + o);
                                } else {
                                    if (DEBUG) System.err.println("no folder folder/part");
                                }
                                break;
                            case 2:
                                fo = filefolder.getFileObject("part", "ext");
                                if (fo != null) {
                                    DataObject o = DataObject.find(fo);
                                    if (DEBUG) System.err.println("from file found: " + o);
                                } else {
                                    if (DEBUG) System.err.println("no file folder/part.ext");
                                }
                                break;
                            default:
                                throw new IllegalStateException();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            try {
                t.start();
                for (int i = 0; i < 35; i++) {
                    // Sleep a different amount each time.
                    // Total sleep time  ~ 5sec.
                    Thread.sleep((i * 17) % 100);
                    // Create the files.
                    final FileObject[] primSec = new FileObject[2];
                    //final String name = "part" + i;
                    final String name = "part";
                    final int pause = (i * 23) % 100;
                    final int _i = i;
                    FileSystem.AtomicAction action = new FileSystem.AtomicAction() {
                        public void run() throws IOException {
                            try {
                                if (makePrimFirst) {
                                    loadPrim();
                                    Thread.sleep(pause);
                                }
                                loadSec();
                                if (! makePrimFirst) {
                                    Thread.sleep(pause);
                                    loadPrim();
                                }
                            } catch (InterruptedException ie) {
                                throw new IOException(ie.toString());
                            }
                        }
                        private void loadPrim() throws IOException {
                            primSec[0] = filefolder.createFolder(name);
                        }
                        private void loadSec() throws IOException {
                            primSec[1] = filefolder.createData(name, "ext");
                        }
                    };
                    if (atomic) {
                        lfs.runAtomicAction(action);
                    } else {
                        action.run();
                    }
                    Thread.sleep((i * 19) % 100);
                    FileObject tofind = findPrim ? primSec[0] : primSec[1];
                    DataObject dob = DataObject.find(tofind);
                    assertEquals("On iteration #" + i + ", found right object", TwoPartObject.class, dob.getClass());
                    if (DEBUG) System.err.println("it worked, #" + i);
                    // Clean up for the next round.
                    action = new FileSystem.AtomicAction() {
                        public void run() throws IOException {
                            // Delete in reverse order.
                            if (! makePrimFirst) {
                                primSec[0].delete();
                            }
                            primSec[1].delete();
                            if (makePrimFirst) {
                                primSec[0].delete();
                            }
                        }
                    };
                    if (atomic) {
                        lfs.runAtomicAction(action);
                    } else {
                        action.run();
                    }
                }
            } finally {
                // Done, stop recognizer.
                stop[0] = true;
                t.join(5000);
            }
        } finally {
            AddLoaderManuallyHid.addRemoveLoader(l, false);
        }
        TestUtilHid.destroyLocalFileSystem(getName());
    }
    
    public static final class TwoPartLoader extends MultiFileLoader {
        public TwoPartLoader() {
            super(TwoPartObject.class);
        }
        protected String displayName() {
            return "TwoPart";
        }
        protected FileObject findPrimaryFile(FileObject fo) {
            if (fo.isFolder() && ! fo.isRoot() && FileUtil.findBrother(fo, "ext") != null) {
                return fo;
            } else if (fo.isData() && fo.hasExt("ext")) {
                FileObject fo2 = fo.getParent().getFileObject(fo.getName());
                if (fo2 != null && fo2.isFolder()) {
                    return fo2;
                }
            }
            return null;
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new TwoPartObject(this, primaryFile);
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry.Folder(obj, primaryFile);
        }
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return new FileEntry(obj, secondaryFile);
        }
    }
    public static final class TwoPartObject extends DataFolder {
        public TwoPartObject(TwoPartLoader l, FileObject folder) throws DataObjectExistsException {
            super(folder, l);
        }
    }
    
    
}
