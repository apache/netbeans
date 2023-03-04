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


import java.io.OutputStream;
import java.net.URL;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import org.netbeans.junit.*;

/**
 *Test behavior of FileUtil.isArchiveFile
 *
 * @author Tomas Zezula
 */
public class IsArchiveFileTest extends NbTestCase {
    /**
     * filesystem containing created instances
     */
    private LocalFileSystem lfs;
    private FileObject directory;
    private FileObject brokenArchive;
    private FileObject archive;
    private FileObject file;
    private FileObject emptyFile;

    /**
     * Creates new test
     */
    public IsArchiveFileTest(String name) {
        super(name);
    }

    public static void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(new NbTestSuite(IsArchiveFileTest.class));
    }

    /**
     * Setups variables.
     */
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();                        
        lfs = new LocalFileSystem ();
        lfs.setRootDirectory(this.getWorkDir());
        Repository.getDefault().addFileSystem(lfs);
        FileObject root = lfs.getRoot();        
        directory = root.createFolder("dir");
        brokenArchive = root.createData ("brokenArchive.jar");
        archive = root.createData("archive.jar");
        FileLock lock = archive.lock();
        try {
            JarOutputStream out = new JarOutputStream (archive.getOutputStream(lock));
            try {
                out.putNextEntry(new ZipEntry("foo"));
                out.closeEntry();
            } finally {
                out.close();
            }
        } finally {
            lock.releaseLock();
        }
        file = root.createData ("file.txt");
        lock = file.lock ();
        try {
            OutputStream out = file.getOutputStream(lock);
            try {
                out.write ("Test file".getBytes());
            } finally {
                out.close();
            }
        } finally {
            lock.releaseLock();
        }
        emptyFile = root.createData("emptyFile.txt");
    }
    
    protected void tearDown() throws Exception {
        Repository.getDefault().removeFileSystem(lfs);
        super.tearDown();
    }
    

    public void testIsArchivFile () throws Exception {
        assertFalse (FileUtil.isArchiveFile(directory));
        assertFalse (FileUtil.isArchiveFile(brokenArchive));
        assertTrue (FileUtil.isArchiveFile(archive));
        assertFalse (FileUtil.isArchiveFile(file));
        assertFalse (FileUtil.isArchiveFile(emptyFile));
        
        assertFalse (FileUtil.isArchiveFile(new URL("jar:file:/foo.jar!/")));
        assertFalse (FileUtil.isArchiveFile(new URL("file:/foo/")));
        assertFalse (FileUtil.isArchiveFile(new URL("file:/javafx.base")));
        assertTrue (FileUtil.isArchiveFile(new URL("file:/foo.jar")));
    }

   
}
  
  
  
