/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.loaders;

import java.util.Enumeration;
import junit.framework.TestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/** Mostly to test the correct behaviour of AWTTask.waitFinished.
 *
 * @author Jaroslav Tulach
 */
public class DataFolderCopyMoreWindowsLikeTest extends TestCase {
    DataFolder target;
    DataFolder source;
    DataFolder sub;

    public DataFolderCopyMoreWindowsLikeTest(String testName) {
        super (testName);
    }
    
    protected void setUp() throws Exception {
        FileObject root = FileUtil.getConfigRoot();
        FileObject[] arr = root.getChildren ();
        for (int i = 0; i < arr.length; i++) {
            arr[i].delete ();
        }
        
        target = DataFolder.findFolder (FileUtil.createFolder (root, "Target"));
        source = DataFolder.findFolder (FileUtil.createFolder (root, "Source"));
        sub = DataFolder.findFolder (FileUtil.createFolder (root, "Source/Sub/"));
        FileUtil.createData (root, "Source/Sub/A.txt");
    }

    public void testCopyIntoTheSameFolderCreatesFolderNamed2 () throws Exception {
        sub.copy (source);
       
        assertFO ("Sibling to Sub created", "/Source/Sub_1/A.txt");
    }
    
    public void testCopyIntoDifferentEmptyFolderIsWithotuRenames () throws Exception {
        sub.copy (target);
       
        assertFO ("A.txt name preserved", "/Target/Sub/A.txt");
    }

    public void testCopyIntoDifferentNonEmptyFolderCreatesSibling () throws Exception {
        FileUtil.createData (FileUtil.getConfigRoot(), "Target/Sub/A.txt");
        
        sub.copy (target);
       
        assertFO ("A_1.txt sibling created", "/Target/Sub/A_1.txt");
    }

    public void testMoveIntoTheSameFolderIsForbiden() throws Exception {
        FileObject old = source.getPrimaryFile ();
        
        sub.move (source);
        
        assertEquals ("No change", old, source.getPrimaryFile ());
    }
    
    private static void assertFO (String msg, String name) {
        FileObject fo = FileUtil.getConfigFile(name);
        if (fo == null) {
            StringBuffer sb = new StringBuffer (msg);
            sb.append (" - cannot find ");
            sb.append (name);
            Enumeration<? extends FileObject> en = FileUtil.getConfigRoot().getChildren(true);
            while (en.hasMoreElements ()) {
                sb.append ('\n');
                sb.append ("    ");
                sb.append (en.nextElement ());
            }
            fail (sb.toString ());
        }
    }
    
}
