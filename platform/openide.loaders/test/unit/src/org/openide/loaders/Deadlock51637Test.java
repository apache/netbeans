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

import junit.framework.AssertionFailedError;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/** Simulates the deadlock between copy/move operation and the creation
 * of node.
 *
 * @author Jaroslav Tulach
 */
public class Deadlock51637Test extends NbTestCase implements FileChangeListener {
    FileObject toolbars;
    FileSystem fs;
    DataFolder toolbarsFolder;
    DataFolder anotherFolder;
    DataObject obj;
    
    Node node;
    Exception assigned;
    boolean called;
    boolean enough;
    
    public Deadlock51637Test(String testName) {
        super (testName);
    }
    
    protected void setUp() throws Exception {
        fs = FileUtil.getConfigRoot().getFileSystem();
        FileObject root = fs.getRoot ();
        toolbars = FileUtil.createFolder (root, "Toolbars");
        toolbarsFolder = DataFolder.findFolder (toolbars);
        FileObject[] arr = toolbars.getChildren ();
        for (int i = 0; i < arr.length; i++) {
            arr[i].delete ();
        }
        FileObject fo = FileUtil.createData (root, "Ahoj.txt");
        obj = DataObject.find (fo);
        fo = FileUtil.createFolder (root, "Another");
        anotherFolder = DataFolder.findFolder (fo);
        
        fs.addFileChangeListener (this);
    }

    protected void tearDown() throws Exception {
        fs.removeFileChangeListener (this);
        
        assertTrue ("The doCreateNode must be called", called);
        
        FileObject[] arr = toolbars.getChildren ();
        for (int i = 0; i < arr.length; i++) {
            arr[i].delete ();
        }
        
    }
    
    private void doCreateNode () {
        if (enough) {
            return;
        }
        if (node != null) {
            assertNotNull ("Node is not null, but it was not assigned", assigned);
            
            AssertionFailedError a = new AssertionFailedError ("Node cannot be null");
            a.initCause (assigned);
            throw a;
        }
        // just one event is enough
        enough = true;
        fs.removeFileChangeListener (this);
        
        called = true;
        
        boolean ok;
        try {
            final Exception now = new Exception ("Calling to rp");
            ok = RequestProcessor.getDefault ().post (new Runnable () {
                public void run () {
                    node = obj.getNodeDelegate ();
                    
                    assigned = new Exception ("Created in RP");
                    assigned.initCause (now);
                }
            }).waitFinished (100000);
        } catch (InterruptedException ex) {
            AssertionFailedError a = new AssertionFailedError (ex.getMessage ());
            a.initCause (ex);
            throw a;
        }
        
        if (node == null) {
            fail ("Node is still null and the waitFinished was " + ok);
        }
    }
    

    public void testMove () throws Exception {
        obj.move (anotherFolder);
    }

    public void testCopy () throws Exception {
        obj.copy (anotherFolder);
    }
    
    public void testRename () throws Exception {
        obj.rename ("NewName.txt");
    }
    
    public void testCreateShadow () throws Exception {
        obj.createShadow (anotherFolder);
    }
    
    public void testTemplate () throws Exception {
        obj.createFromTemplate (anotherFolder);
    }

    public void testTemplate2 () throws Exception {
        obj.createFromTemplate (anotherFolder, "AhojVole.txt");
    }

    //
    // Listener triggers creation of the node
    //

    public void fileRenamed (FileRenameEvent fe) {
        doCreateNode ();
    }

    public void fileAttributeChanged (FileAttributeEvent fe) {
    }

    public void fileFolderCreated (FileEvent fe) {
        doCreateNode ();
    }

    public void fileDeleted (FileEvent fe) {
        doCreateNode ();
    }

    public void fileDataCreated (FileEvent fe) {
        doCreateNode ();
    }

    public void fileChanged (FileEvent fe) {
        doCreateNode ();
    }
    
}
