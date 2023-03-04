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

import org.openide.ErrorManager;

import org.openide.filesystems.*;
import java.io.IOException;
import java.util.*;

/** Simulates the deadlock from issue 35847.
 * @author Jaroslav Tulach
 */
public class Deadlock35847Test extends LoggingTestCaseHid {
    private ErrorManager err;

    public Deadlock35847Test(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        err = ErrorManager.getDefault().getInstance("TEST-" + getName());
        registerIntoLookup(new Pool());
    }
    
    public void testLoaderThatStopsToRecognizeWhatItHasRecognized () throws Exception {
        ForgetableLoader l = (ForgetableLoader)ForgetableLoader.getLoader(ForgetableLoader.class);
        
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), new String[] {
            "folder/f.forget",
            "folder/f.keep"
        });

        // do not recognize anything
        l.forget = true;

        FileObject fo = lfs.findResource("folder");
        DataFolder f = DataFolder.findFolder(fo);


        DataObject[] arr = f.getChildren ();
        assertEquals ("Two child there", 2, arr.length);

        DataObject keep;
        java.lang.ref.WeakReference forget;
        if (arr[0].getPrimaryFile().hasExt ("keep")) {
            keep = arr[0];
            forget = new java.lang.ref.WeakReference (arr[1]);
        } else {
            keep = arr[1];
            forget = new java.lang.ref.WeakReference (arr[0]);
        }

        org.openide.nodes.Node theDelegate = new org.openide.nodes.FilterNode (keep.getNodeDelegate());

        arr = null;
        assertGC ("Forgetable object can be forgeted", forget);

        class P extends org.openide.nodes.NodeAdapter
        implements java.beans.PropertyChangeListener {
            int cnt;
            String name;

            public void propertyChange (java.beans.PropertyChangeEvent ev) {
                name = ev.getPropertyName();
                cnt++;
                err.log("Event arrived: " + ev.getPropertyName());
            }
        }
        P listener = new P ();
        keep.addPropertyChangeListener (listener);
        // in order to trigger listening on the original node and cause deadlock
        theDelegate.addNodeListener(listener);

        // now recognize
        l.forget = false;

        // this will trigger invalidation of keep from Folder Recognizer Thread
        err.log("Beging to get children");
        DataObject[] newArr = f.getChildren ();
        err.log("End of get children");

        assertEquals ("Keep is Invalidated", 1, listener.cnt);
        assertEquals ("Property is PROP_VALID", DataObject.PROP_VALID, listener.name);
    }
    
    public void testLoaderThatStopsToRecognizeWhatItHasRecognizedAndDoesItWhileHoldingChildrenMutex () throws Exception {
        org.openide.nodes.Children.MUTEX.readAccess (new org.openide.util.Mutex.ExceptionAction () {
            public Object run () throws Exception {
                testLoaderThatStopsToRecognizeWhatItHasRecognized ();
                return null;
            }
        });
    }
    

    public static final class ForgetableLoader extends MultiFileLoader {
        public boolean forget;
        
        public ForgetableLoader () {
            super(MultiDataObject.class);
        }
        protected String displayName() {
            return "ForgetableLoader";
        }
        /** Recognizes just two files - .forget and .keep at once, only in non-forgetable mode 
         */
        protected FileObject findPrimaryFile(FileObject fo) {
            if (forget) {
                return null;
            }
            if (fo.hasExt ("forget")) {
                return FileUtil.findBrother (fo, "keep");
            }
            if (fo.hasExt ("keep")) {
                return fo;
            }
            return null;
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MultiDataObject (primaryFile, this);
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry (obj, primaryFile);
        }
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return new FileEntry(obj, secondaryFile);
        }
    }
    private static final class Pool extends DataLoaderPool {
        public Pool() {
        }
        
        public Enumeration loaders() {
            ForgetableLoader l = (ForgetableLoader)ForgetableLoader.getLoader(ForgetableLoader.class);
            return org.openide.util.Enumerations.singleton(l);
        }
    }
}
