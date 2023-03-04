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
package org.openide.filesystems;

import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.concurrent.Callable;
import org.netbeans.junit.NbTestCase;

/**
 * Assures behaviour the 'revealEntries' pseudo-attribute
 * @author sdedic
 */
public class MultiFileSystemRevealTest  extends NbTestCase {

    public MultiFileSystemRevealTest(String name) {
        super(name);
    }

    @Override
    protected void setUp () throws Exception {
        super.setUp();
        clearWorkDir();
        System.setProperty("workdir", getWorkDirPath());
    }
    
    /**
     * In the case of no user modifications, empty Collection is provided
     * @throws Exception 
     */
    public void testNoUserModifications() throws Exception {
        MultiFileSystem fs = new MultiFileSystem(
            TestUtilHid.createLocalFileSystem(getName() + "1", new String[0]),
            TestUtilHid.createXMLFileSystem(getName() + "2", new String[] {
                "folder/file1",
                "folder/file2",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "3", new String[] {
                "folder/file1",
                "folder/file3",
            })
        );
        Collection<FileObject> fos = (Collection<FileObject>)fs.findResource("folder").getAttribute("revealEntries");
        assertTrue(fos.isEmpty());
    }
    
    /**
     * Checks that for a deleted file, a FileObject is produced.
     * file1 and file3 are deleted from different layers, then undeleted.
     * 
     * @throws Exception 
     */
    public void testFileDeleted() throws Exception {
        TestUtilHid.Resource root2 = TestUtilHid.createRoot();
        root2.add("folder/file2");
        root2.add("folder/file1").addAttribute("name", "stringvalue", "jouda");
        
        TestUtilHid.Resource root3 = TestUtilHid.createRoot();
        root3.add("folder/file1");
        root3.add("folder/file3").addAttribute("name", "stringvalue", "bubak");
        
        MultiFileSystem fs = new MultiFileSystem(
            TestUtilHid.createLocalFileSystem(getName() + "1", new String[] {
                "folder/file1_hidden",
                "folder/file3_hidden",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "2", root2),
            TestUtilHid.createXMLFileSystem(getName() + "3", root3)
        );
        Collection<FileObject> fos = (Collection<FileObject>)fs.findResource("folder").getAttribute("revealEntries");
        FileObject f = findFile(fos, "file1");
        assertNotNull("Delete file1 should be revealed", f);
        assertTrue(f instanceof Callable);
        assertEquals("folder/file1", f.getPath());
        assertSame(fs, f.getFileSystem());
        assertNotNull(f.getParent().getFileObject("file2"));
        assertNull(f.getParent().getFileObject("file3"));
        
        assertNotNull(f.getAttribute("name"));
        assertEquals("jouda", f.getAttribute("name"));

        FileObject f2 = findFile(fos, "file3");
        assertNotNull("Delete file1 should be revealed", f);
        assertTrue(f2 instanceof Callable);
        assertEquals("folder/file3", f2.getPath());
        assertSame(fs, f2.getFileSystem());
        assertNotNull(f2.getParent().getFileObject("file2"));
        assertNull(f2.getParent().getFileObject("file1"));
        
        assertNotNull(f2.getAttribute("name"));
        assertEquals("bubak", f2.getAttribute("name"));
        
        // try to un-delete the files:
        ((Callable)f2).call();
        ((Callable)f).call();
        
        assertEquals(3, fs.findResource("folder").getChildren().length);
    }
    
    private FileObject findFile(Collection<FileObject> files, String name) {
        for (FileObject f : files) {
            if (f.getNameExt().equals(name)) {
                return f;
            }
        }
        return null;
    }
    
    /**
     * For a changed file, FileObject with the original content should
     * be produced.
     * @throws Exception 
     */
    public void testFileChangedAttributesAndContent() throws Exception {
        TestUtilHid.Resource root2 = TestUtilHid.createRoot();
        root2.add("folder/file2");
        root2.add("folder/file1").withContent("Povidam povidam pohadku");
        
        TestUtilHid.Resource root3 = TestUtilHid.createRoot();
        root3.add("folder/file1");
        root3.add("folder/file3").addAttribute("name", "stringvalue", "jouda");
        
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getName() + "1", new String[] {
            "folder/file1",
            "folder/file3",
        });
        FileObject local1 = lfs.findResource("folder/file1");
        OutputStreamWriter wr = new OutputStreamWriter(local1.getOutputStream());
        wr.append("o cervenem kotatku");
        wr.close();
        
        FileObject local3 = lfs.findResource("folder/file3");
        local3.setAttribute("test", "failed");
        local3.setAttribute("name", "truhlik");
        
        MultiFileSystem fs = new MultiFileSystem(
            lfs,
            TestUtilHid.createXMLFileSystem(getName() + "2", root2),
            TestUtilHid.createXMLFileSystem(getName() + "3", root3)
        );
        
        Collection<FileObject> fos = (Collection<FileObject>)fs.findResource("folder").getAttribute("revealEntries");
        FileObject f = findFile(fos, "file1");
        assertNotNull("Delete file1 should be revealed", f);
        assertTrue(f instanceof Callable);
        assertEquals("Povidam povidam pohadku", f.asText());
        assertEquals("folder/file1", f.getPath());
        assertSame(fs, f.getFileSystem());
        assertNotNull(f.getParent().getFileObject("file2"));
        
        FileObject f2 = findFile(fos, "file3");
        assertNotNull("Delete file1 should be revealed", f);
        assertTrue(f2 instanceof Callable);
        assertEquals("folder/file3", f2.getPath());
        assertSame(fs, f2.getFileSystem());
        assertNotNull(f2.getParent().getFileObject("file2"));
        
        assertNull(f2.getAttribute("test"));
        assertEquals("jouda", f2.getAttribute("name"));
        
        FileObject multiFile1 = fs.findResource("folder/file1");
        FileObject multiFile3 = fs.findResource("folder/file3");
        
        assertEquals("o cervenem kotatku", multiFile1.asText());
        assertEquals("truhlik", multiFile3.getAttribute("name"));
        
        // try to un-delete the files:
        assertSame(multiFile3, ((Callable)f2).call());
        assertSame(multiFile1, ((Callable)f).call());
        
        assertEquals("Povidam povidam pohadku", multiFile1.asText());
        assertEquals("jouda", multiFile3.getAttribute("name"));
        
        assertEquals(3, fs.findResource("folder").getChildren().length);
    }
    
    /**
     * No reveal entry should be present for user-only modification
     * @throws Exception 
     */
    public void testUserOnlyContent() throws Exception {
        MultiFileSystem fs = new MultiFileSystem(
            TestUtilHid.createLocalFileSystem(getName() + "1", new String[] {
                "folder/file4",
                "folder/file5",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "2", new String[] {
                "folder/file1",
                "folder/file2",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "3", new String[] {
                "folder/file1",
                "folder/file3",
            })
        );
        Collection<FileObject> fos = (Collection<FileObject>)fs.findResource("folder").getAttribute("revealEntries");
        assertTrue(fos.isEmpty());
    }

    /**
     * No reveal entry should be present for user-only modification
     * @throws Exception 
     */
    public void testNotWritableLayer() throws Exception {
        MultiFileSystem fs = new MultiFileSystem(
            TestUtilHid.createXMLFileSystem(getName() + "1", new String[] {
                "folder/file1",
                "folder/file3",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "2", new String[] {
                "folder/file1",
                "folder/file2",
            }),
            TestUtilHid.createXMLFileSystem(getName() + "3", new String[] {
                "folder/file1",
                "folder/file3",
            })
        );
        Collection<FileObject> fos = (Collection<FileObject>)fs.findResource("folder").getAttribute("revealEntries");
        assertNull("Readonly MFS does not support revealEntries", fos);
    }
}
