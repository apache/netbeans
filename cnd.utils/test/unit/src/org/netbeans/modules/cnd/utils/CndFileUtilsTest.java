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
package org.netbeans.modules.cnd.utils;

import java.io.File;
import java.io.IOException;
import junit.framework.Assert;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 */
public class CndFileUtilsTest extends NbTestCase {

    public CndFileUtilsTest(String name) {
        super(name);
        System.setProperty("cnd.modelimpl.assert.notfound", "true");
    }

    @Override
    protected int timeOut() {
        return 500000;
    }
    
    @Test
    public void testLocalUrlToFileObject() throws IOException {
        File temp = File.createTempFile("urlToFileObject", ".txt");
        Assert.assertNotNull(temp);
        // on windows it has ~ to have short names
        temp = FileUtil.normalizeFile(temp);
        temp.deleteOnExit();
        String fileExternalForm = temp.toURI().toURL().toExternalForm();
        String absPath = temp.getAbsolutePath();
        FileObject fo = FileUtil.toFileObject(temp);
        Assert.assertNotNull(fo);
        String foExternalForm = fo.getURL().toExternalForm();
        CharSequence url1 = CndFileUtils.fileObjectToUrl(fo);
        Assert.assertEquals(fo, CndFileUtils.urlToFileObject(url1));
        Assert.assertEquals(fo, CndFileUtils.urlToFileObject(fileExternalForm));
        Assert.assertEquals(fo, CndFileUtils.urlToFileObject(foExternalForm));
        Assert.assertEquals(fo, CndFileUtils.urlToFileObject(absPath));
        // we can not concatenate abs path and protocol as is, because
        // if abs path contains space => it's invalid URI symbol
        String escapedPath = absPath.replaceAll(" ", "%20");
        if (!escapedPath.startsWith("/")) {
            escapedPath = "/" + escapedPath;
        }
        Assert.assertEquals(fo, CndFileUtils.urlToFileObject("file:" + escapedPath));
        Assert.assertNull(CndFileUtils.urlToFileObject("file:/" + escapedPath));
        Assert.assertEquals(fo, CndFileUtils.urlToFileObject("file://" + escapedPath));
        temp.delete();
    }
    
    @Test
    public void testFlagsUpdates() throws IOException {
        clearWorkDir();
        FileObject workDirFO = FileUtil.toFileObject(getWorkDir());
        int i = 0, MAX_ITER = 100;
        while (i++ < MAX_ITER) {
            // create folders and files
            FileObject parent = workDirFO.createFolder("parent");
            assertTrue(CndFileUtils.isExistingDirectory(CndFileUtils.getLocalFileSystem(), parent.getPath()));
            
            FileObject child = parent.createFolder("child");
            assertTrue(CndFileUtils.isExistingDirectory(CndFileUtils.getLocalFileSystem(), child.getPath()));
            assertTrue(CndFileUtils.isExistingDirectory(CndFileUtils.getLocalFileSystem(), parent.getPath()));
            
            FileObject file = child.createData("test", "c");
            assertTrue(CndFileUtils.isExistingFile(file.getPath()));
            assertTrue(CndFileUtils.isExistingDirectory(CndFileUtils.getLocalFileSystem(), child.getPath()));
            assertTrue(CndFileUtils.isExistingDirectory(CndFileUtils.getLocalFileSystem(), parent.getPath()));
            
            // delete folders and files
            file.delete();
            assertFalse(CndFileUtils.isExistingFile(file.getPath()));
            assertTrue(CndFileUtils.isExistingDirectory(CndFileUtils.getLocalFileSystem(), child.getPath()));
            assertTrue(CndFileUtils.isExistingDirectory(CndFileUtils.getLocalFileSystem(), parent.getPath()));
            
            child.delete();
            assertFalse(CndFileUtils.isExistingDirectory(CndFileUtils.getLocalFileSystem(), child.getPath()));
            assertTrue(CndFileUtils.isExistingDirectory(CndFileUtils.getLocalFileSystem(), parent.getPath()));
            
            parent.delete();
            assertFalse(CndFileUtils.isExistingDirectory(CndFileUtils.getLocalFileSystem(), parent.getPath()));
            assertFalse(CndFileUtils.isExistingDirectory(CndFileUtils.getLocalFileSystem(), child.getPath()));
        }
    }
    
    @Test
    public void testFlagsUpdates2() throws IOException {
        // see IZ 216271
        clearWorkDir();
        FileObject workDirFO = FileUtil.toFileObject(getWorkDir());
        // create folders and files
        FileObject parent = workDirFO.createFolder("parent");
        FileObject file1 = parent.createData("test", "c");
        FileObject child = parent.createFolder("child");
        FileObject file2 = child.createData("test", "c");
        assertTrue(CndFileUtils.isExistingFile(CndFileUtils.getLocalFileSystem(), file1.getPath()));
        assertTrue(CndFileUtils.isExistingFile(CndFileUtils.getLocalFileSystem(), file2.getPath()));
        file2.delete();
        assertTrue(CndFileUtils.isExistingDirectory(CndFileUtils.getLocalFileSystem(), child.getPath()));
    }
}
