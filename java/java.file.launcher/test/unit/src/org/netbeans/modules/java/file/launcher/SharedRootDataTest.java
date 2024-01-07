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
package org.netbeans.modules.java.file.launcher;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class SharedRootDataTest extends NbTestCase {

    public SharedRootDataTest(String name) {
        super(name);
    }

    public void testSourceLevels() throws IOException {
        clearWorkDir();

        File wd = getWorkDir();
        FileObject root = FileUtil.toFileObject(wd);
        FileObject file1 = FileUtil.createData(root, "File1.java");
        FileObject file2 = FileUtil.createData(root, "File2.java");

        SharedRootData.ensureRootRegistered(root);

        SharedRootData data = SharedRootData.getDataForRoot(root);

        assertNotNull(data);
        file1.setAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS, "--enable-preview --source 1.7");
        assertEquals("--enable-preview --source 1.7", root.getAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS));
        file2.setAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS, "--enable-preview --source 17");
        assertEquals("--enable-preview --source 17", root.getAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS));
        file2.setAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS, "--enable-preview --source 1.8");
        assertEquals("--enable-preview --source 1.8", root.getAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS));
        file1.setAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS, "--enable-preview --source 11");
        assertEquals("--enable-preview --source 11", root.getAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS));

        file1.delete();
        assertEquals("--enable-preview --source 1.8", root.getAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS));
    }

    public void testPathMerging() throws IOException {
        clearWorkDir();

        File wd = getWorkDir();
        FileObject wdFO = FileUtil.toFileObject(wd);
        FileObject root = FileUtil.createFolder(wdFO, "root");
        FileObject file1 = FileUtil.createData(root, "File1.java");
        FileObject file2 = FileUtil.createData(root, "File2.java");
        FileObject test1Jar = FileUtil.createData(wdFO, "test1.jar");
        FileObject test2Jar = FileUtil.createData(wdFO, "test2.jar");

        SharedRootData.ensureRootRegistered(root);

        SharedRootData data = SharedRootData.getDataForRoot(root);

        assertNotNull(data);
        file1.setAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS, "--class-path test1.jar");
        assertEquals("--class-path test1.jar", root.getAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS));
        file2.setAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS, "-cp test2.jar");
        assertEquals("--class-path test1.jar" + File.pathSeparator + "test2.jar", root.getAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS));

        file1.setAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS, "--module-path test1.jar");
        assertEquals("--class-path test2.jar --module-path test1.jar", root.getAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS));
        file2.setAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS, "--module-path test2.jar");
        assertEquals("--module-path test1.jar" + File.pathSeparator + "test2.jar", root.getAttribute(SingleSourceFileUtil.FILE_VM_OPTIONS));
    }

}
