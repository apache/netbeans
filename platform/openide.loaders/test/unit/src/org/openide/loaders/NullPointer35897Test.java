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
import java.beans.*;
import java.io.IOException;
import junit.textui.TestRunner;
import org.netbeans.junit.*;

/*
 * Tries to reproduce NPE from #35897 issue.
 */
public class NullPointer35897Test extends NbTestCase {
    private FileSystem lfs;
    private FileObject file;
    private L loader;
    private D obj;

    public NullPointer35897Test (String name) {
        super (name);
    }

    protected void setUp () throws Exception {
        TestUtilHid.destroyLocalFileSystem (getName ());
        String fsstruct [] = new String [] {
            "dir/simple.simple"
        };
        lfs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);
        Repository.getDefault ().addFileSystem (lfs);

        file = lfs.findResource (fsstruct[0]);
        
        loader = (L)DataLoader.getLoader (L.class);
        AddLoaderManuallyHid.addRemoveLoader (loader, true);
    }
    
    //Clear all stuff when the test finish
    protected void tearDown () throws Exception {
        AddLoaderManuallyHid.addRemoveLoader (loader, false);
        TestUtilHid.destroyLocalFileSystem (getName ());
    }
    
    public void test35897 () throws Exception {
        class InitObj implements Runnable {
            public void run () {
                try {
                    obj = (D)DataObject.find (file);
                } catch (DataObjectNotFoundException ex) {
                    ex.printStackTrace();
                    fail ("Unexpected exception");
                }
            }
        }
        InitObj init = new InitObj ();
        
        org.openide.util.RequestProcessor.Task task;
        synchronized (loader) {
            task = org.openide.util.RequestProcessor.getDefault ().post (init);
            loader.wait ();
        }
        
        assertTrue ("The creation of DataObject is blocked in constructor", loader.waitingInConstructor);

        Repository.getDefault ().removeFileSystem (lfs);
        
        synchronized (loader) {
            loader.notifyAll ();
        }
        task.waitFinished ();
        
        assertNotNull ("The object has been finished", obj);
    }
    
    private static class D extends MultiDataObject {
        private boolean constructorFinished;
        
        public D (FileObject pf, L loader) throws IOException {
            super(pf, loader);

            synchronized (loader) {
                try {
                    loader.waitingInConstructor = true;
                    loader.notifyAll ();
                    loader.wait (2000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    fail ("No interruptions please");
                } finally {
                    loader.waitingInConstructor = false;
                    constructorFinished = true;
                }
            }            
        }
        
        

        protected org.openide.nodes.Node createNodeDelegate() {
            return org.openide.nodes.Node.EMPTY;
        }
        
        public java.util.Set files () {
            assertTrue ("This can be called only if the constructor is finished", constructorFinished);
            return super.files ();
        }
        
    }

    private static class L extends MultiFileLoader {
        public boolean waitingInConstructor;
        
        public L () {
            super(D.class.getName());
        }
        protected String displayName() {
            return "L";
        }

        protected FileObject findPrimaryFile (FileObject obj) {
            return obj.hasExt ("simple") ? obj : null;
        }

        protected MultiDataObject createMultiObject(FileObject pf) throws IOException {
            return new D(pf, this);
        }


        protected MultiDataObject.Entry createSecondaryEntry (MultiDataObject x, FileObject obj) {
            throw new IllegalStateException ();
        }

        protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject x, FileObject obj) {
            return new org.openide.loaders.FileEntry (x, obj);
        }
    }

    
}
