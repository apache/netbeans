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

import java.util.logging.Logger;
import org.openide.filesystems.*;

import java.beans.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.netbeans.junit.*;

/** Test recognition of objects in folders, and folder ordering.
 *
 * @author  Vita Stejskal, Jesse Glick
 */
public class DataReadOnlyTest extends LoggingTestCaseHid {
    private ArrayList hold = new ArrayList();

    /** Creates new DataFolderTest */
    public DataReadOnlyTest (String name) {
        super (name);
    }

    protected void setUp () throws Exception {
        clearWorkDir ();
    }
    
    public void testDeleteReadOnlyIssue81241() throws Exception {
        String fsstruct [] = new String [] {
            "temp/a/b/test.txt",
            "where/",
        };
        
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);

        FileObject fo = lfs.findResource ("temp/a/b/test.txt");
        DataObject obj = DataObject.find(fo);
        
        assertEquals ("Found the right one", fo, obj.getPrimaryFile());
        
        File f = FileUtil.toFile(fo);
        assertNotNull("File found", f);
        assertTrue("File exists", f.exists());
        
        if (!f.setReadOnly()) {
            // if the read only operation does not succeeds, then the test has to end
            Logger.getAnonymousLogger().warning("Cannot set read only: " + f);
            return;
        }
        
        assertFalse("Is read only", f.canWrite());
        
        DataFolder folder = obj.getFolder().getFolder();
        assertNotNull("Folder found", folder);
        
        
        DataFolder target = DataFolder.findFolder(lfs.findResource("where"));
        
        folder.move(target);
        
        assertTrue("File is not moved", f.exists());
        assertTrue("Remains valid", fo.isValid());
        
        DataObject[] arr = target.getChildren();
        assertEquals("One children", 1, arr.length);
        
        if (arr[0] instanceof DataFolder) {
            DataFolder subF = (DataFolder)arr[0];
            arr = subF.getChildren();
        } else {
            fail("Shall be a folder: " + arr[0]);
        }

        assertEquals("One children", 1, arr.length);
        
        if (arr[0] instanceof DataFolder) {
            DataFolder subF = (DataFolder)arr[0];
            arr = subF.getChildren();
        } else {
            fail("Shall be a folder: " + arr[0]);
        }
        
        assertEquals("No children in target subfolder: " + Arrays.asList(arr), 0, arr.length);

        arr = folder.getChildren();
        assertEquals("One children in orig folder", 1, arr.length);
        
        if (arr[0] instanceof DataFolder) {
            DataFolder subF = (DataFolder)arr[0];
            arr = subF.getChildren();
        } else {
            fail("Shall be a folder: " + arr[0]);
        }

        assertEquals("One children in orig subfolder", 1, arr.length);
        assertEquals("Named correctly", "test.txt", arr[0].getPrimaryFile().getNameExt());
    }
}
