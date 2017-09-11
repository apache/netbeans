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
