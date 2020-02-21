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
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 */
public class CndDataObjectTestCase extends NbTestCase {
    
    public CndDataObjectTestCase(String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testCDataObject() throws Exception {
        File newFile = new File(super.getWorkDir(), "file.c"); // NOI18N
        newFile.createNewFile();
        assertTrue("Not created file " + newFile, newFile.exists());
        FileObject fo = CndFileUtils.toFileObject(newFile);
        assertNotNull("Not found file object for file" + newFile, fo);
        assertTrue("File object not valid for file" + newFile, fo.isValid());
        DataObject dob = DataObject.find(fo);
        assertTrue("data object is not recognized by default infrastructure", dob instanceof CDataObject);
    }
    
    public void testCCDataObject() throws Exception {
        File newFile = new File(super.getWorkDir(), "file.cc"); // NOI18N
        newFile.createNewFile();
        assertTrue("Not created file " + newFile, newFile.exists());
        FileObject fo = CndFileUtils.toFileObject(newFile);
        assertTrue("File object not valid for file" + newFile, fo.isValid());
        assertNotNull("Not found file object for file" + newFile, fo);
        DataObject dob = DataObject.find(fo);
        assertTrue("data object is not recognized by default infrastructure", dob instanceof CCDataObject);
    }

    public void testHDataObject() throws Exception {
        File newFile = new File(super.getWorkDir(), "file.h"); // NOI18N
        newFile.createNewFile();
        assertTrue("Not created file " + newFile, newFile.exists());
        FileObject fo = CndFileUtils.toFileObject(newFile);
        assertNotNull("Not found file object for file" + newFile, fo);
        assertTrue("File object not valid for file" + newFile, fo.isValid());
        DataObject dob = DataObject.find(fo);
        assertTrue("data object is not recognized by default infrastructure", dob instanceof HDataObject);
    }

    public void testHDataObjectWithoutExtension() throws Exception {
        checkHeaderWithoutExtension("headerWithComments", "//    -*- C++ -*-    \n"); // NOI18N
        checkHeaderWithoutExtension("headerWithStandardComments", "//    standard header\n"); // NOI18N
        checkHeaderWithoutExtension("headerWithInclude", "\n\n#include <stdio>\n"); // NOI18N
        checkHeaderWithoutExtension("headerWithPragma", "\n#pragma once\n"); // NOI18N
    }

    private void checkHeaderWithoutExtension(String fileName, CharSequence content) throws Exception {
        File newFile = new File(super.getWorkDir(), fileName); // NOI18N
        newFile.createNewFile();
        assertTrue("Not created file " + newFile, newFile.exists());
        FileObject fo = CndFileUtils.toFileObject(newFile);
        Writer writer = new OutputStreamWriter(fo.getOutputStream());
        try {
            writer.append(content);
            writer.flush();
        } finally {
            writer.close();
        }
        assertNotNull("Not found file object for file" + newFile, fo);
        assertTrue("File object not valid for file" + newFile, fo.isValid());
        String mime = FileUtil.getMIMEType(fo, MIMENames.HEADER_MIME_TYPE);
        assertEquals("header with content " + content + " is not recognized ", MIMENames.HEADER_MIME_TYPE, mime);
        DataObject dob = DataObject.find(fo);
        assertTrue("data object is not recognized by default infrastructure", dob instanceof HDataObject);
    }
}
