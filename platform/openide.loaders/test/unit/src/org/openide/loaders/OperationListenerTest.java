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

import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import org.openide.filesystems.*;
import java.io.IOException;
import java.util.*;
import org.openide.util.Enumerations;
import org.openide.util.RequestProcessor;

/*
 * Checks whether a during a modify operation (copy, move) some
 * other thread can get a grip on unfinished and uncostructed 
 * content on filesystem.
 *
 * @author Jaroslav Tulach
 */
public class OperationListenerTest extends LoggingTestCaseHid
implements OperationListener {
    private ArrayList events = new ArrayList ();
    private FileSystem fs;
    private DataLoaderPool pool;
    private Logger err;
    
    /** Creates the test */
    public OperationListenerTest(String name) {
        super(name);
    }

    @Override protected Level logLevel() {
        return Level.INFO;
    }
    
    // For each test setup a FileSystem and DataObjects
    protected void setUp() throws Exception {
        registerIntoLookup(new Pool());
        
        String fsstruct [] = new String [] {
            "source/A.attr", 
            "B.attr",
            "dir/",
            "fake/A.instance"
        };
        TestUtilHid.destroyLocalFileSystem (getName());
        fs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);
        
        err = Logger.getLogger("TEST-" + getName());
        
        pool = DataLoaderPool.getDefault ();
        assertNotNull (pool);
        assertEquals (Pool.class, pool.getClass ());
        
        Pool.setExtra(null);
        
        err.info("setUp is over");
    }
    
    //Clear all stuff when the test finish
    protected void tearDown() throws Exception {
        err.info("entering tearDown");
        
        pool.removeOperationListener(this);
        
        err.info("Making sure everything is cleaned");
        WeakReference ref = new WeakReference(fs);
        fs = null;
        events = null;
        assertGC("GC the filesystem", ref);
        err.info("Ok, tearDown finished");
        
//        AddLoaderManuallyHid.addRemoveLoader (ALoader.getLoader (ALoader.class), false);
//        AddLoaderManuallyHid.addRemoveLoader (BLoader.getLoader (BLoader.class), false);
    }
    
    //
    // Tests
    //
    
    public void testRecognizeFolder () {
        pool.addOperationListener(this);
        DataFolder df = DataFolder.findFolder (fs.findResource ("fake"));
        DataObject[] arr = df.getChildren ();
        
        assertEquals ("One child", 1, arr.length);
        
        assertEvents ("Recognized well", new OperationEvent[] {
            new OperationEvent (df),
            new OperationEvent (arr[0])
        });
    }

    public void testCopyFile() throws Exception {
        err.info("Before add listener");
        pool.addOperationListener(this);
        err.info("after add listener");
        DataObject obj = DataObject.find (fs.findResource ("fake/A.instance"));
        err.info("object found: " + obj);
        DataFolder df = DataFolder.findFolder (fs.findResource ("dir"));
        err.info("folder found: " + df);
        DataObject n = obj.copy (df);
        err.info("copy done: " + n);
        assertEquals ("Copy successfull", n.getFolder(), df);
        
        err.info("Comparing events");
        assertEvents ("All well", new OperationEvent[] {
            new OperationEvent (obj),
            new OperationEvent (df),
            new OperationEvent (n),
            new OperationEvent.Copy (n, obj)
        });
    }
    
    public void testBrokenLoader () throws Exception {
        BrokenLoader loader = (BrokenLoader)DataLoader.getLoader(BrokenLoader.class);
        
        try {
            err.info("before setExtra: " + loader);
            Pool.setExtra(loader);
            
            err.info("before addOperationListener");
            pool.addOperationListener(this);
            
            loader.acceptableFO = fs.findResource ("source/A.attr");
            err.info("File object found: " + loader.acceptableFO);
            try {
                DataObject obj = DataObject.find (fs.findResource ("source/A.attr"));
                fail ("The broken loader throws exception and cannot be created");
            } catch (IOException ex) {
                // ok
                err.info("Exception thrown correctly:");
                err.log(Level.INFO, null, ex);
            }
            assertEquals ("Loader created an object", loader, loader.obj.getLoader());
            
            err.info("brefore waitFinished");
            // and the task can be finished
            loader.recognize.waitFinished ();
            
            err.info("waitFinished done");
            
            assertEvents ("One creation notified even if the object is broken", new OperationEvent[] {
                new OperationEvent (loader.obj),
            });
        } finally {
            Pool.setExtra(null);
        }
    }
    
    //
    // helper methods
    //
    
    private void assertEvents (String txt, OperationEvent[] expected) {
        boolean failure = false;
        if (expected.length != events.size ()) {
            failure = true;
        } else {
            for (int i = 0; i < expected.length; i++) {
                OperationEvent e = expected[i];
                OperationEvent r = (OperationEvent)events.get (i);
                if (e.getClass  () != r.getClass ()) {
                    failure = true;
                    break;
                }
                if (e.getObject () != r.getObject()) {
                    failure = true;
                    break;
                }
            }
        }
        
        
        if (failure) {
            StringBuffer sb = new StringBuffer ();
            
            int till = Math.max (expected.length, events.size ());
            sb.append ("Expected events: " + expected.length + " was: " + events.size () + "\n");
            for (int i = 0; i < till; i++) {
                sb.append ("  Expected: ");
                if (i < expected.length) {
                    sb.append (expected[i].getClass () + " source: " + expected[i].getObject ());
                }
                sb.append ('\n');
                sb.append ("  Was     : ");
                if (i < events.size ()) {
                    OperationEvent ev = (OperationEvent)events.get (i);
                    sb.append (ev.getClass () + " source: " + ev.getObject ());
                }
                sb.append ('\n');
            }
            
            fail (sb.toString ());
        }
        
        events.clear();
    }
    
    //
    // Listener implementation
    //
    
    public void operationCopy(org.openide.loaders.OperationEvent.Copy ev) {
        record("  operationCopy: ", ev);
    }
    
    public void operationCreateFromTemplate(org.openide.loaders.OperationEvent.Copy ev) {
        record("  operationCreateFromTemplate: ", ev);
    }
    
    public void operationCreateShadow(org.openide.loaders.OperationEvent.Copy ev) {
        record("  operationCreateShadow: ", ev);
    }
    
    public void operationDelete(OperationEvent ev) {
        record("  operationDelete: ", ev);
    }
    
    public void operationMove(org.openide.loaders.OperationEvent.Move ev) {
        record("  operationMove: ", ev);
    }
    
    public void operationPostCreate(OperationEvent ev) {
        record("  operationPostCreate: ", ev);
    }
    
    public void operationRename(org.openide.loaders.OperationEvent.Rename ev) {
        record("  operationRename: ", ev);
    }

    private void record(String msg, OperationEvent ev) {
        try {
            if (!ev.getObject().getPrimaryFile().getFileSystem().isDefault()) {
                events.add(ev);
                err.info(msg + ev);
            }
        } catch (FileStateInvalidException ex) {
            fail(ex.getMessage());
        }
    }
            
    
    //
    // Own loader
    //
    public static final class BrokenLoader extends UniFileLoader {
        public FileObject acceptableFO;
        public RequestProcessor.Task recognize;
        public MultiDataObject obj;
        
        public BrokenLoader() {
            super(MultiDataObject.class.getName ());
        }
        protected String displayName() {
            return "BrokenLoader";
        }
        protected FileObject findPrimaryFile(FileObject fo) {
            if (acceptableFO != null && acceptableFO.equals(fo)) {
                return fo;
            }
            return null;
        }
        protected MultiDataObject createMultiObject(final FileObject primaryFile) throws DataObjectExistsException, IOException {
            obj = new MultiDataObject (primaryFile, this);
            
            assertNull ("Only one invocation of this code allowed", recognize);
            
            class R implements Runnable {
                public DataObject found;
                public void run () {
                    synchronized (this) {
                        notify ();
                    }
                    // this basicly means another call to createMultiObject method
                    // of this loader again, but the new MultiDataObject will throw
                    // DataObjectExistsException and will block in its
                    // getDataObject method
                    try {
                        found = DataObject.find (primaryFile);
                    } catch (IOException ex) {
                        fail ("Unexepcted exception: " + ex);
                    }
                    
                    assertEquals ("DataObjects are the same", found, obj);
                }
            }
            R run = new R ();
            synchronized (run) {
                recognize = RequestProcessor.getDefault ().post (run);
                try {
                    run.wait ();
                } catch (InterruptedException ex) {
                    fail ("Unexepcted ex: " + ex);
                }
            }
                
            
            throw new IOException ("I am broken!");
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry(obj, primaryFile);
        }
        
        public void run() {
        }
        
    }
    
    private static final class Pool extends DataLoaderPool {
        private static DataLoader extra;
        
        
        protected Enumeration loaders () {
            if (extra == null) {
                return Enumerations.empty ();
            } else {
                return Enumerations.singleton (extra);
            }
        }

        public static void setExtra(DataLoader aExtra) {
            extra = aExtra;
            Pool p = (Pool)DataLoaderPool.getDefault();
            p.fireChangeEvent(new ChangeEvent(p));
        }
    }
}
