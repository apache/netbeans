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
import org.openide.loaders.*;
import java.io.IOException;
import org.netbeans.junit.*;

/*
 * Checks whether a during a modify operation (copy, move) some
 * other thread can get a grip on unfinished and uncostructed
 * content on filesystem.
 *
 * @author Jaroslav Tulach
 */
@RandomlyFails // NB-Core-Build #1105, and looks inherently unreliable
public class SeparationOfThreadsTest extends NbTestCase {
    private DataFolder root;
    private DataFolder to;
    private DataObject a;
    private DataObject b;
    private DataObject res;

    /** Creates the test */
    public SeparationOfThreadsTest(String name) {
        super(name);
    }

    // For each test setup a FileSystem and DataObjects
    protected void setUp() throws Exception {
        clearWorkDir();
        String fsstruct [] = new String [] {
            "source/A.attr", 
            "B.attr",
            "dir/",
            "fake/A.instance"
        };
        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem fs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);
        root = DataFolder.findFolder (fs.getRoot ());
        
        AddLoaderManuallyHid.addRemoveLoader (ALoader.getLoader (ALoader.class), true);
        AddLoaderManuallyHid.addRemoveLoader (BLoader.getLoader (BLoader.class), true);
        
        to = DataFolder.findFolder (fs.findResource (fsstruct[2]));
        
        fs.findResource (fsstruct[0]).setAttribute ("A", Boolean.TRUE);
        
        a = DataObject.find (fs.findResource (fsstruct[0]));
        b = DataObject.find (fs.findResource (fsstruct[1]));
        
        ALoader loaderA = (ALoader)ALoader.getLoader (ALoader.class);
        
        assertEquals ("A is loaded by ALoader", loaderA, a.getLoader());
        assertEquals ("B is loaded by BLoader", ALoader.getLoader (BLoader.class), b.getLoader());

        // following code tests one bug that I have made during implementation
        // the runAtomicAction has to be run in finally block as some operation
        // can throw exceptions. This simulates operation that throws exception
        try {
            a.delete ();
            fail ("Should throw exception");
        } catch (IOException ex) {
            assertEquals ("Not implemented", ex.getMessage ());
        }
        
        synchronized (loaderA) {
            new Thread ((Runnable)loaderA, "Asynchronous access test").start ();
            loaderA.wait ();
        }
    }
    
    //Clear all stuff when the test finish
    protected void tearDown() throws Exception {
        ALoader loader = (ALoader)ALoader.getLoader(ALoader.class);
        synchronized (loader) {
            try {
                int cnt = 0;
                while (!loader.finished) {
                    loader.wait(1000);
                    assertTrue("apparent hang in tearDown", cnt++ < 100);
                }

                if (res == null) {
                    // an exception in testXXXX method
                    return;
                }
                
                assertEquals ("The right loader synchronously", loader, res.getLoader ());
                
                if (loader.asyncError != null) {
                    throw loader.asyncError;
                }
                
                assertNotNull (loader.asyncRes);
                assertEquals ("It is the right loader asynchronously", loader, loader.asyncRes.getLoader());
                
            } finally {
                loader.asyncError = null;
                loader.currentThread = null;
                loader.current = null;
                loader.asyncRes = null;
                loader.finished = false;
                // clears any such flag
                Thread.interrupted();


                TestUtilHid.destroyLocalFileSystem (getName());
                AddLoaderManuallyHid.addRemoveLoader (ALoader.getLoader (ALoader.class), false);
                AddLoaderManuallyHid.addRemoveLoader (BLoader.getLoader (BLoader.class), false);
                
                // end of test
                loader.notify ();
            }            
        }
    }

    public void testCopy () throws Exception {
        res = a.copy (to);
    }

    public void testCreateFromTemplate () throws Exception {
        res = a.createFromTemplate (to);
    }
    public void testMove () throws Exception {
        a.move (to);
        res = a;
    }
    
    public void testRename () throws Exception {
        a.rename ("AnyThing");
        res = a;
    }
    
    //
    // Inner classes
    //
    
    public static final class ALoader extends UniFileLoader implements Runnable {
        DataObject asyncRes;
        Exception asyncError;
        FileObject current;
        Thread currentThread;
        boolean finished;
        
        public ALoader() {
            super(DataObject.class.getName());
        }
        protected void initialize() {
            super.initialize();
            getExtensions().addExtension("attr");
        }
        protected String displayName() {
            return getClass().getName ();
        }
        protected MultiDataObject createMultiObject(FileObject pf) throws IOException {
            return new PostDataObject (pf, this);
        }
        
        protected org.openide.loaders.MultiDataObject.Entry createPrimaryEntry(org.openide.loaders.MultiDataObject multiDataObject, org.openide.filesystems.FileObject fileObject) {
            return new SlowEntry (multiDataObject, fileObject);
        }
        
        // 
        // Notification that the copy is in middle
        // 
        
        public void notifyCopied (final FileObject current) {
            try {
                if (current.getPath ().indexOf ("rename") < 0) {
                    // do not run this part for rename test
                    
                    // first of all do some really ugly and complex operation =>
                    // get children of our target folder while we are modifying it
                    // ugly, but 
                    DataObject[] arr = DataFolder.findFolder (current.getParent ()).getChildren ();
                    assertEquals ("In folder fake there is one object", 1, arr.length);
                    assertEquals ("The loader has to be the BLoader, as we are asking in the middle of copying", 
                        DataLoader.findObject (BLoader.class),
                        arr[0].getLoader ()
                    );

                    java.lang.ref.WeakReference ref = new java.lang.ref.WeakReference (arr[0]);
                    arr = null;
                    assertGC ("The created object has to be garbage collected, otherwise the result of the test" + 
                        " will not be the right DataObject, because the previous one already exists", ref
                    );
                }
                    
                //
                // Now something even uglier => any thread can call DataFolder.getChildren,
                // but only on different folder than the one we are copying to
                //
                org.openide.util.RequestProcessor.getDefault().post (new Runnable () {
                    public void run () {
                        try {
                            final DataFolder queryOn = DataFolder.findFolder (current.getFileSystem().findResource("fake"));
                            DataObject[] one = queryOn.getChildren ();
                            assertEquals ("One object is in that folder", 1, one.length);

                            //
                            // Another uglyness => get access to already existing object outside of target folder
                            //
                            FileObject fo = current.getFileSystem().findResource("B.attr");
                            DataObject obj = DataObject.find (fo);
                            assertNotNull (obj);
                            
                            java.lang.ref.WeakReference ref = new java.lang.ref.WeakReference (one[0]);
                            one = null;
                            assertGC ("The object has to disappear", ref);

                        } catch (java.io.IOException ex) {
                            fail ("should not happen");
                        }
                    }
                }).waitFinished();


            } finally {
                synchronized (this) {
                    this.current = current;
                    this.currentThread = Thread.currentThread ();
                    this.notify ();
                }
            }
            
            int cnt = 1;
            while (cnt-- > 0) {
                try {
                    Thread.sleep (500);
                } catch (InterruptedException ex) {
                    // is interrupted to be wake up sooner
                }
            }
        }
        
        //
        // The second thread that waits for the copy operation
        //
        public void run () {
            DataLoader loader = this;
            synchronized (loader) {

                try {
                    // continue execution in setUp
                    loader.notify ();
                    
                    // wait for being notify about copying
                    loader.wait ();
                    
                    asyncRes = DataObject.find (current);
                    currentThread.interrupt();
                } catch (InterruptedException ex) {
                    asyncError = ex;
                } catch (IOException ex) {
                    asyncError = ex;
                } finally {
                    // notify that we have computed everything
                    finished = true;
                    loader.notify ();

                    while (asyncRes != null && asyncError != null) {
                        try {
                            loader.wait ();
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            }
        }
        
    } // end of ALoader
    
    /** Calls super to do some copying and then does some post processing
     */
    public static final class PostDataObject extends MultiDataObject {
       public PostDataObject (FileObject fo, ALoader loader) throws DataObjectExistsException {
           super (fo, loader);
       }

       /** Tryies to find a file in secondary thread.
        */
       private void assertObject (final DataObject obj) {
           final Object[] res = new Object[1];
           org.openide.util.RequestProcessor.getDefault ().post (new Runnable () {
                public void run () {
                    try {
                        res[0] = DataObject.find (obj.getPrimaryFile());
                    } catch (DataObjectNotFoundException ex) {
                        res[0] = ex;
                    }
                }
           }).waitFinished ();
           
           assertEquals ("Objects are the same", obj, res[0]);
       }
           
       
       protected FileObject handleRename(String name) throws IOException {
           FileObject retValue;
           retValue = super.handleRename(name);
           return retValue;
       }       
       
       protected void handleDelete() throws IOException {
           super.handleDelete();
       }       
        
       protected DataObject handleCopy(DataFolder df) throws IOException {
           DataObject retValue;
           
           retValue = super.handleCopy(df);
           
           assertObject (retValue);
           return retValue;
       }
       
       protected DataObject handleCreateFromTemplate(DataFolder df, String name) throws IOException {
           DataObject retValue;

           FileObject artificial = FileUtil.createData(
               FileUtil.createMemoryFileSystem().getRoot(),
               "x.art"
           );
           FileUtil.setMIMEType("art", "text/x-art");
           DataObject obj = DataObject.find(artificial);
           assertEquals("Object really created", obj.getPrimaryFile(), artificial);
           
           retValue = super.handleCreateFromTemplate(df, name);
           
           assertObject (retValue);
           return retValue;
       }
       
       protected FileObject handleMove(DataFolder df) throws IOException {
           FileObject retValue;
           
           retValue = super.handleMove(df);
           return retValue;
       }
       
    } // end of PostDataObject
    
    public static final class BLoader extends UniFileLoader {
        public BLoader() {
            super(DataObject.class.getName());
        }
        protected void initialize() {
            super.initialize();
            getExtensions().addExtension("attr");
        }
        protected String displayName() {
            return getClass ().getName ();
        }
        protected org.openide.filesystems.FileObject findPrimaryFile(org.openide.filesystems.FileObject fileObject) {
            if (Boolean.TRUE.equals (fileObject.getAttribute ("A"))) {
                return null;
            }
            
            org.openide.filesystems.FileObject retValue;
            
            retValue = super.findPrimaryFile(fileObject);
            return retValue;
        }        
        
        protected MultiDataObject createMultiObject(FileObject pf) throws IOException {
            return new MultiDataObject(pf, this);
        }
    }
    
    private static final class SlowEntry extends MultiDataObject.Entry {
        public SlowEntry (MultiDataObject obj, FileObject fo) {
            obj.super (fo);
        }
        
        private void notifyCopied (FileObject fo) {
            ALoader l = (ALoader)ALoader.getLoader(ALoader.class);
            l.notifyCopied (fo);
        }
        
        public org.openide.filesystems.FileObject copy(org.openide.filesystems.FileObject fileObject, String str) throws java.io.IOException {
            FileObject ret = fileObject.createData ("copy", "attr");
            notifyCopied (ret);
            ret.setAttribute ("A", Boolean.TRUE);
            return ret;
        }
        
        public org.openide.filesystems.FileObject createFromTemplate(org.openide.filesystems.FileObject fileObject, String str) throws java.io.IOException {
            FileObject ret = fileObject.createData ("createFromTemplate", "attr");
            notifyCopied (ret);
            ret.setAttribute ("A", Boolean.TRUE);
            return ret;
        }
        
        public void delete() throws java.io.IOException {
            throw new IOException ("Not implemented");
        }
        
        public org.openide.filesystems.FileObject move(org.openide.filesystems.FileObject fileObject, String str) throws java.io.IOException {
            FileObject ret = fileObject.createData ("move", "attr");
            notifyCopied (ret);
            ret.setAttribute ("A", Boolean.TRUE);
            super.getFile ().delete ();
            return ret;
        }
        
        public org.openide.filesystems.FileObject rename(String str) throws java.io.IOException {
            FileObject ret = getFile ().getParent ().createData ("rename", "attr");
            super.getFile ().delete ();
            notifyCopied (ret);
            ret.setAttribute ("A", Boolean.TRUE);
            return ret;
        }
        
    }
}
