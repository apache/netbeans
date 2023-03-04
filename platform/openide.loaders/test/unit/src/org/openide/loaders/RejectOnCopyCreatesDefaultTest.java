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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import junit.extensions.*;
import junit.textui.TestRunner;

import org.openide.filesystems.*;
import junit.framework.*;
import org.netbeans.junit.*;
import java.io.IOException;
import org.openide.loaders.DataLoader.RecognizedFiles;
import org.openide.nodes.Node;
import java.lang.ref.WeakReference;
import java.io.*;
import java.util.*;
import java.beans.PropertyVetoException;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import org.openide.*;
import org.openide.util.Enumerations;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.nodes.CookieSet;

/**
 * @author Jaroslav Tulach
 */
public class RejectOnCopyCreatesDefaultTest extends LoggingTestCaseHid {
    FileSystem fs;
    DataObject one;
    DataFolder from;
    DataFolder to;
    ErrorManager err;
    
    
    /** Creates new DataObjectTest */
    public RejectOnCopyCreatesDefaultTest (String name) {
        super (name);
    }
    
    public void setUp() throws Exception {
        clearWorkDir();
        
        SimpleLoader.active = true;
        
        super.setUp();
        
        registerIntoLookup(new Pool());
        
        err = ErrorManager.getDefault().getInstance("TEST-" + getName());
        
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        fs = lfs;
        FileUtil.createData(fs.getRoot(), "from/x.prima");
        FileUtil.createFolder(fs.getRoot(), "to/");
        
        one = DataObject.find(fs.findResource("from/x.prima"));
        assertEquals(MultiDataObject.class, one.getClass());
        assertEquals(SimpleLoader.getLoader(SimpleLoader.class), one.getLoader());
        
        from = one.getFolder();
        to = DataFolder.findFolder(fs.findResource("to/"));
        
        assertEquals("Nothing there", 0, to.getPrimaryFile().getChildren().length);
    }
    
    public void testCopyAndSee() throws Exception {
        
        SimpleLoader.active = false;
        
        DataObject copy = one.copy(to);
        
        if (copy.getLoader() == one.getLoader()) {
            fail("Loaders shall be different:\n" + one + "\nand:\n" + copy);
        }
    }

    private static class Pool extends DataLoaderPool {
        protected Enumeration loaders() {
            return Enumerations.singleton(SimpleLoader.getLoader(SimpleLoader.class));
        }
    }
    
    public static final class SimpleLoader extends MultiFileLoader {
        public SimpleLoader() {
            super(MultiDataObject.class.getName());
        }
        protected String displayName() {
            return "SimpleLoader";
        }
        
        public static boolean active = true;
        
        protected FileObject findPrimaryFile(FileObject fo) {
            if (!fo.isFolder() && active) {
                return fo.hasExt("prima") ? fo : null;
            }
            return null;
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MultiDataObject(primaryFile, this);
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry(obj, primaryFile);
        }
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return new FileEntry(obj, secondaryFile);
        }
    }
}
