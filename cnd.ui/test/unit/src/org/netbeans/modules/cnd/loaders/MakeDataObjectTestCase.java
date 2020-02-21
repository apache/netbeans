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
package org.netbeans.modules.cnd.loaders;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 */
public class MakeDataObjectTestCase extends NbTestCase {
    public MakeDataObjectTestCase(String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testCMakeDataObject1() throws Exception {
        File newFile = new File(super.getWorkDir(), "CMakeLists.txt"); // NOI18N
        checkCMake(newFile, MIMENames.CMAKE_MIME_TYPE, CMakeDataObject.class);
    }

    public void testCMakeDataObject2() throws Exception {
        File newFile = new File(super.getWorkDir(), "qq.cmake"); // NOI18N
        checkCMake(newFile, MIMENames.CMAKE_INCLUDE_MIME_TYPE, CMakeIncludeDataObject.class);
    }

    private void checkCMake(File newFile, String mime, Class<?> cls) throws IOException, DataObjectNotFoundException {
        newFile.createNewFile();
        assertTrue("Not created file " + newFile, newFile.exists());
        FileObject fo = CndFileUtils.toFileObject(newFile);
        assertNotNull("Not found file object for file" + newFile, fo);
        assertTrue("File object not valid for file" + newFile, fo.isValid());
        assertEquals("Not text/x-cmake mime type", mime, fo.getMIMEType());
        DataObject dob = DataObject.find(fo);
        assertTrue("data object is not recognized by default infrastructure:" + dob.getClass(), cls.isInstance(dob));
    }
}
