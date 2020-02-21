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

package org.netbeans.modules.cnd.source;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class CndFileObjectTestCase extends NbTestCase {
    
    public CndFileObjectTestCase(String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testCFileObject() throws Exception {
        File newFile = new File(super.getWorkDir(), "file.c"); // NOI18N
        newFile.createNewFile();
        assertTrue("Not created file " + newFile, newFile.exists());
        FileObject fo = CndFileUtils.toFileObject(newFile);
        assertNotNull("Not found file object for file" + newFile, fo);
        assertTrue("File object not valid for file" + newFile, fo.isValid());
        assertEquals("Not text/x-c mime type", MIMENames.C_MIME_TYPE, fo.getMIMEType());
    }
    
    public void testCCFileObject() throws Exception {
        File newFile = new File(super.getWorkDir(), "file.cc"); // NOI18N
        newFile.createNewFile();
        assertTrue("Not created file " + newFile, newFile.exists());
        FileObject fo = CndFileUtils.toFileObject(newFile);
        assertNotNull("Not found file object for file" + newFile, fo);
        assertTrue("File object not valid for file" + newFile, fo.isValid());
        assertEquals("Not text/x-c++ mime type", MIMENames.CPLUSPLUS_MIME_TYPE, fo.getMIMEType());
    }

    public void testHFileObject() throws Exception {
        File newFile = new File(super.getWorkDir(), "file.h"); // NOI18N
        newFile.createNewFile();
        assertTrue("Not created file " + newFile, newFile.exists());
        FileObject fo = CndFileUtils.toFileObject(newFile);
        assertNotNull("Not found file object for file" + newFile, fo);
        assertTrue("File object not valid for file" + newFile, fo.isValid());
        assertEquals("Not text/x-c++ mime type", MIMENames.HEADER_MIME_TYPE, fo.getMIMEType());
    }
    
}
