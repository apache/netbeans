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
import java.beans.*;
import java.lang.ref.WeakReference;
import org.netbeans.junit.*;

/** Test the complexity of deleting N files in a folder without recognized
 * DataOjects (which may as well be tested on deleting secondary files
 * of a recognized DataObject).
 *
 * The test may need slight tweaking of the timing constants for reliability.
 * 
 * @author  Petr Nejedly
 */
public class DataFolderSlowDeletionTest extends LoggingTestCaseHid {
    private FileObject folder;
    // just holders
    private FileObject[] children;
    private DataFolder df;
    private DataObject do0;
    

    /** Creates new DataFolderSlowDeletionTest */
    public DataFolderSlowDeletionTest (String name) {
        super (name);
    }
    
    /**
     * @return a  speedSuite configured as to allow 2x linear slowdown between
     * 10-fold increase of the paratemer
     */
    public static NbTestSuite suite () {
        return NbTestSuite.linearSpeedSuite(DataFolderSlowDeletionTest.class, 2, 5);
    }
    
    /**
     * Prepares a filesystem with a prepopulated folder of N files, where N
     * is extracted from the test name.
     * @throws java.lang.Exception 
     */
    protected void setUp() throws Exception {
        clearWorkDir();
        TestUtilHid.destroyLocalFileSystem(getName());
        
        int count = getTestNumber ();
        String[] resources = new String[count];       
        for (int i=0; i<resources.length; i++) resources[i] = "folder/file" + i + ".txt";
        FileSystem fs = TestUtilHid.createLocalFileSystem(getWorkDir(), resources);
        folder = fs.findResource("folder");
        
        // convert to masterfs
        folder = FileUtil.toFileObject(FileUtil.toFile(folder));
        
        
        children = folder.getChildren();
        df = DataFolder.findFolder (folder);
        do0 = DataObject.find(children[0]);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        WeakReference<?> ref = new WeakReference<Object>(folder);
        folder = null;
        children = null;
        df = null;
        do0 = null;
        assertGC("Cleanup before next test", ref);
    }
    
    
    /**
     * 
     * @throws java.lang.Exception 
     */
    private void performSlowDeletionTest () throws Exception {
        folder.delete();
    }
    
    /**
     * Preheat the infrastructure so the lower end is measured already JITed
     * @throws java.lang.Exception 
     */
    public void testSlowDeletionPrime1000() throws Exception {
        performSlowDeletionTest();
    }

    /**
     * 
     * @throws java.lang.Exception 
     */
    public void testSlowDeletion1000() throws Exception {
        performSlowDeletionTest();
    }

    public void testSlowDeletion3000() throws Exception {
        performSlowDeletionTest();
    }
    
}
