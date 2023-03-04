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

package org.netbeans.modules.web.common.api;

import java.util.List;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author marekfukala
 */
public class FileReferenceTest extends CslTestBase {

    public FileReferenceTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        WebUtils.UNIT_TESTING = true;
        WebUtils.WEB_ROOT = FileUtil.toFileObject(getDataDir());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        WebUtils.UNIT_TESTING = false;
    }

    public void testFileReference() {
        FileObject one = getTestFile("one.txt");
        assertNotNull(one);
        FileObject two = getTestFile("folder/second.txt");
        assertNotNull(two);

        //test resolve path reference
        FileReference resolved = WebUtils.resolveToReference(one, "folder/second.txt");
        assertNotNull(resolved);

        assertEquals(one, resolved.source());
        assertEquals(two, resolved.target());
        assertEquals(FileReferenceType.RELATIVE, resolved.type());
        assertEquals("folder/second.txt", resolved.linkPath());
        assertEquals("folder/second.txt", resolved.optimizedLinkPath());

    }

    public void testOptimizedLink() {
        FileObject one = getTestFile("one.txt");
        assertNotNull(one);
        FileObject fourth = getTestFile("folder/innerfolder/fourth.txt");
        assertNotNull(fourth);

        FileReference resolved = WebUtils.resolveToReference(one, "folder/innerfolder/fourth.txt");
        assertNotNull(resolved);
        assertEquals(fourth, resolved.target());
        assertEquals("folder/innerfolder/fourth.txt", resolved.optimizedLinkPath());
        assertEquals(one.getParent(), resolved.baseFolder());

        //and back
        resolved = WebUtils.resolveToReference(fourth, "../../one.txt");
        assertNotNull(resolved);
        assertEquals(one, resolved.target());
        assertEquals("../../one.txt", resolved.optimizedLinkPath());
        assertEquals(one.getParent(), resolved.baseFolder());

    }

    public void testBaseFolder() {
        FileObject fourth = getTestFile("folder/innerfolder/fourth.txt");
        assertNotNull(fourth);
        FileObject fifth =  getTestFile("folder/innerfolder2/fifth.txt");
        assertNotNull(fifth);

        FileObject folder = getTestFile("folder");
        assertTrue(folder.isFolder());
        FileObject innerfolder = getTestFile("folder/innerfolder");
        assertTrue(innerfolder.isFolder());
        FileObject innerfolder2 = getTestFile("folder/innerfolder2");
        assertTrue(innerfolder2.isFolder());

        FileReference resolved = WebUtils.resolveToReference(fourth, "../innerfolder2/fifth.txt");
        assertNotNull(resolved);
        assertEquals(fifth, resolved.target());
        assertEquals("../innerfolder2/fifth.txt", resolved.optimizedLinkPath());
        assertEquals(folder, resolved.baseFolder());

        List<FileObject> smembers = resolved.sourcePathMembersToBase();
        assertEquals(1, smembers.size());
        assertEquals(smembers.get(0), innerfolder);

        List<FileObject> tmembers = resolved.targetPathMembersToBase();
        assertEquals(1, tmembers.size());
        assertEquals(tmembers.get(0), innerfolder2);

    }

    public void testFileReferenceModification() {
        FileObject fourth = getTestFile("folder/innerfolder/fourth.txt");
        assertNotNull(fourth);
        FileObject fifth =  getTestFile("folder/innerfolder2/fifth.txt");
        assertNotNull(fifth);

        FileObject folder = getTestFile("folder");
        assertTrue(folder.isFolder());
        FileObject innerfolder = getTestFile("folder/innerfolder");
        assertTrue(innerfolder.isFolder());
        FileObject innerfolder2 = getTestFile("folder/innerfolder2");
        assertTrue(innerfolder2.isFolder());

        FileReference resolved = WebUtils.resolveToReference(fourth, "../innerfolder2/fifth.txt");
        assertNotNull(resolved);
        assertEquals(fifth, resolved.target());
        assertEquals("../innerfolder2/fifth.txt", resolved.optimizedLinkPath());
        assertEquals(folder, resolved.baseFolder());

        FileReferenceModification modif = resolved.createModification();
        //the unmodified ref. path should be the same
        assertEquals(resolved.optimizedLinkPath(), modif.getModifiedReferencePath());

        //test modify
        modif.rename(innerfolder, "ble"); //should have no effect, the .. is not refactored
        assertEquals(resolved.optimizedLinkPath(), modif.getModifiedReferencePath());

        modif.rename(innerfolder2, "superinner");
        assertEquals("../superinner/fifth.txt", modif.getModifiedReferencePath());

    }

    public void testFileReferenceModificationFolderWithDot() {
        FileObject one = getTestFile("one.txt");
        FileObject fourth = getTestFile("folder/inner.folder/fourth.txt");
        assertNotNull(fourth);

        FileObject folder = getTestFile("folder");
        assertTrue(folder.isFolder());
        FileObject innerfolder = getTestFile("folder/inner.folder");
        assertTrue(innerfolder.isFolder());

        FileReference resolved = WebUtils.resolveToReference(one, "folder/inner.folder/fourth.txt");
        assertNotNull(resolved);
        assertEquals("folder/inner.folder/fourth.txt", resolved.optimizedLinkPath());

        FileReferenceModification modif = resolved.createModification();
        assertEquals("folder/inner.folder/fourth.txt", modif.getModifiedReferencePath());
        
        //the unmodified ref. path should be the same
        assertEquals(resolved.optimizedLinkPath(), modif.getModifiedReferencePath());

        //test modify
        modif.rename(folder, "renamed"); //should have no effect, the .. is not refactored

        assertEquals("renamed/inner.folder/fourth.txt", modif.getModifiedReferencePath());

    }

    public void testFileAbsoluteReferenceModification() {
        FileObject fourth = getTestFile("folder/innerfolder/fourth.txt");
        assertNotNull(fourth);
        FileObject fifth =  getTestFile("folder/innerfolder2/fifth.txt");
        assertNotNull(fifth);
        assertNotNull(fifth);

        FileObject folder = getTestFile("folder");
        assertTrue(folder.isFolder());
        FileObject innerfolder = getTestFile("folder/innerfolder");
        assertTrue(innerfolder.isFolder());
        FileObject innerfolder2 = getTestFile("folder/innerfolder2");
        assertTrue(innerfolder2.isFolder());
        FileObject baseFolder = FileUtil.toFileObject(getDataDir()); //folder's parent

        FileReference resolved = WebUtils.resolveToReference(fourth, "/folder/innerfolder2/fifth.txt");
        assertNotNull(resolved);
        assertEquals(fifth, resolved.target());
        assertEquals("/folder/innerfolder2/fifth.txt", resolved.optimizedLinkPath());
        assertEquals(baseFolder, resolved.baseFolder());

        FileReferenceModification modif = resolved.createModification();
        //the unmodified ref. path should be the same
        assertEquals(resolved.optimizedLinkPath(), modif.getModifiedReferencePath());

        //test modify
        modif.rename(innerfolder, "ble"); //should have no effect, the .. is not refactored
        assertEquals(resolved.optimizedLinkPath(), modif.getModifiedReferencePath());

        modif.rename(innerfolder2, "superinner");
        assertEquals("/folder/superinner/fifth.txt", modif.getModifiedReferencePath());

    }
    
    public void testResolveLinkWithQueryPart() {
        FileObject one = getTestFile("one.txt");
        assertNotNull(one);
        FileObject two = getTestFile("folder/second.txt");
        assertNotNull(two);

        FileReference resolved = WebUtils.resolveToReference(one, "folder/second.txt?param=val");
        assertNotNull(resolved);
        
        assertEquals(one, resolved.source());
        assertEquals(two, resolved.target());
        assertEquals(FileReferenceType.RELATIVE, resolved.type());
        assertNotNull(WebUtils.resolveToReference(one, "folder/second.txt#article"));
    }
    
    public void testResolveEmptyLink() {
        FileObject one = getTestFile("one.txt");
        assertNotNull(one);
        FileObject two = getTestFile("folder/second.txt");
        assertNotNull(two);

        FileReference resolved = WebUtils.resolveToReference(one, "");
        assertNull(resolved);
        
    }
    
    public void testResolveInvalidLink() {
        FileObject one = getTestFile("one.txt");
        assertNotNull(one);
        FileObject two = getTestFile("folder/second.txt");
        assertNotNull(two);

        FileReference resolved = WebUtils.resolveToReference(one, "is*this+an!invalid@ link?");
        assertNull(resolved);
        
    }

}