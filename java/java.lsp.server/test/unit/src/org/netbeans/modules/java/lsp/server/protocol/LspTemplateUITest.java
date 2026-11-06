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
package org.netbeans.modules.java.lsp.server.protocol;

import java.util.Collections;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.FileBuilder;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class LspTemplateUITest {
    @Test
    public void testStripHtml() {
        String s = "<div>Pre <span>Text</span> Post</div>";
        String expResult = "Pre Text Post";
        String result = LspTemplateUI.stripHtml(s);
        assertEquals(expResult, result);
    }
    
    /**
     * All newlines should be removed
     */
    @Test
    public void testStripNewlines() {
        String s = "\n<div>Pre <span\n>\nText</span> Post\n</div>";
        String expResult = "Pre Text Post";
        String result = LspTemplateUI.stripHtml(s);
        assertEquals(expResult, result);
    }
    
    
    /**
     * Consecutive whitespaces should be collapsed to a single space. Leading/trailing whitespaces
     * removed.
     */
    @Test
    public void testStripConsecutiveWhitespces() {
        String s = "\t <div> Pre <span> Text\t </span>\t\t Post </div>\t";
        String expResult = "Pre Text Post";
        String result = LspTemplateUI.stripHtml(s);
        assertEquals(expResult, result);
    }
/*@aksinsin bug-1 start*/
    @Test
    public void testFindExistingTargetFileWithTemplateExtension() throws Exception {
        //given
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject template = FileUtil.createData(root, "Templates/Class.java");
        FileObject target = FileUtil.createFolder(root, "src");
        FileObject existing = FileUtil.createData(target, "TestDuplicateName.java");
        //when
        CreateDescriptor desc = new FileBuilder(template, target)
                .withParameters(Collections.emptyMap())
                .createDescriptor(false);
        //than
        assertEquals(existing, LspTemplateUI.findExistingTargetFile(desc, "TestDuplicateName"));
        assertEquals(existing, LspTemplateUI.findExistingTargetFile(desc, "TestDuplicateName.java"));
    }

    @Test
    public void testFindExistingTargetFileWithFreeExtension() throws Exception {
        //given
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject template = FileUtil.createData(root, "Templates/Any.txt");
        FileObject target = FileUtil.createFolder(root, "src");
        FileObject existing = FileUtil.createData(target, "TestDuplicateName.java");
        //when
        CreateDescriptor desc = new FileBuilder(template, target)
                .withParameters(Collections.emptyMap())
                .param(CreateDescriptor.FREE_FILE_EXTENSION, Boolean.TRUE)
                .createDescriptor(false);

        //than
        assertEquals(existing, LspTemplateUI.findExistingTargetFile(desc, "TestDuplicateName.java"));
        assertNull(LspTemplateUI.findExistingTargetFile(desc, "TestDuplicateName"));
    }/*@aksinsin bug-1 end*/

    @Test
    public void testFindExistingPkgTargetForExistingFolder() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject target = FileUtil.createFolder(root, "src");
        FileObject existing = FileUtil.createFolder(target, "org/example");

        assertEquals(existing, LspTemplateUI.checkExistingPkgTarget(target, "org.example"));
    }

    @Test
    public void testFindExistingPkgTargetForBlockingFile() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject target = FileUtil.createFolder(root, "src");
        FileObject blocking = FileUtil.createData(target, "org");

        assertEquals(blocking, LspTemplateUI.checkExistingPkgTarget(target, "org.example"));
    }

    @Test
    public void testCreatePkgFolderCreatesHierarchy() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject target = FileUtil.createFolder(root, "src");

        FileObject created = LspTemplateUI.createPkgFolderStructure(target, "org.example.demo");

        FileObject createdFolder = target.getFileObject("org/example/demo");
        assertNotNull(createdFolder);
        assertTrue(createdFolder.isFolder());
        assertEquals(createdFolder, created);
    }
}
