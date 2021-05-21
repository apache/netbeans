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

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.IOException;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/** Separated into own test.
 *
 * @author rmatous
 */
public class FileObjectFactorySizeTest extends NbTestCase {
    private File testFile;


    public FileObjectFactorySizeTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();        
        testFile = new File(getWorkDir(),"testfile");//NOI18N
        if (!testFile.exists()) {
            assert testFile.createNewFile();
        }
        
    }
    

    public void testIssuingFileObject() throws IOException {      
        FileObjectFactory fbs = FileObjectFactory.getInstance(getWorkDir());
        assertEquals("One file object exists?", 1, fbs.getSize());
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        assertNotNull(workDir);
        //root + workdir
        assertEquals(2, fbs.getSize());
        assertEquals(2, fbs.getSize());
        Reference<FileObject> rf = new  WeakReference<>(workDir.getParent());
        assertGC("", rf);
        assertNull(((BaseFileObj)workDir).getExistingParent());
        assertEquals(2, fbs.getSize());
        fbs.getRoot().getFileObject(workDir.getPath());
        assertEquals(2, fbs.getSize());
        rf = new  WeakReference<>(workDir.getParent());
        assertGC("", rf);
        assertNull(((BaseFileObj)workDir).getExistingParent());
        assertEquals(2, fbs.getSize());
    }
}
