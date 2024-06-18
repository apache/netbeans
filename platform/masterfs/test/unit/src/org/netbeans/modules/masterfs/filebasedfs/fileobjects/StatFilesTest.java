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

import java.io.File;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.StatFiles;
import org.openide.util.Utilities;

/**
 * @author Radek Matous
 */
public class StatFilesTest extends NbTestCase {

    private StatFiles monitor;
    private File testFile = null;

    public StatFilesTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        FileObjectFactory.WARNINGS = false;
        clearWorkDir();
        testFile = new File(getWorkDir(), "testLockFile.txt");
        if (!testFile.exists()) {
            testFile.createNewFile();
        }

        assertTrue(testFile.exists());
        //init
        FileUtil.toFileObject(testFile);
        monitor = new StatFiles();
        System.setSecurityManager(monitor);
        monitor.reset();
    }

    private File getFile(FileObject fo) {
        return ((BaseFileObj) fo).getFileName().getFile();
    }

    private FileObject getFileObject(File f) {
        return FileBasedFileSystem.getFileObject(f);
    }

    public void testToFileObject() throws IOException {
        File workDir = getWorkDir();
        assertGC("NamingFactory not cleared.", new WeakReference<FileNaming>(NamingFactory.fromFile(workDir)));
        monitor.reset();
        monitor();
        assertNotNull(FileUtil.toFileObject(workDir));
        int expectedCount = 5;
        if(Utilities.isWindows()) {
            expectedCount = 3;
        }
        monitor.getResults().assertResult(expectedCount, StatFiles.ALL);
        monitor.getResults().assertResult(expectedCount, StatFiles.READ);
    }

    /** Tests it is not neccessary to create FileObjects for the whole path. */
    public void testGetFileObject23() throws IOException {
        File workDir = getWorkDir();
        assertGC("NamingFactory not cleared.", new WeakReference<FileNaming>(NamingFactory.fromFile(workDir)));
        File rootFile = null;
        Stack<String> stack = new Stack<String>();
        while (workDir != null) {
            stack.push(workDir.getName());
            rootFile = workDir;
            workDir = workDir.getParentFile();
        }
        String relativePath = "";
        while (!stack.empty()) {
            relativePath += stack.pop() + "/";
        }
        FileObject root = FileUtil.toFileObject(rootFile);
        monitor.reset();
        assertNotNull(root.getFileObject(relativePath));
        monitor.getResults().assertResult(2, StatFiles.ALL);
    }

    public void testGetCachedChildren() throws IOException {
        FileObject fobj = getFileObject(testFile);
        FileObject parent = fobj.getParent();
        List<FileObject> l = new ArrayList<FileObject>();
        parent = parent.createFolder("parent");
        for (int i = 0; i < 10; i++) {
            l.add(parent.createData("file" + i));
            l.add(parent.createFolder("fold" + i));
        }

        monitor.reset();
        //20 x FileObject + 1 File.listFiles
        FileObject[] children = parent.getChildren();
        monitor.getResults().assertResult(1, StatFiles.ALL);
        monitor.getResults().assertResult(1, StatFiles.READ);
        //second time
        monitor.reset();
        children = parent.getChildren();
        monitor.getResults().assertResult(0, StatFiles.ALL);
    }

    /** Testing that queries for siblings in fully expanded folder do not generate
     * new disk touches. */
    public void testGetChildrenCaches() throws IOException {
        FileObject fobj = getFileObject(testFile);
        FileObject parent = fobj.getParent();
        List<FileObject> l = new ArrayList<FileObject>();
        parent = parent.createFolder("parent");
        for (int i = 0; i < 20; i++) {
            l.add(parent.createData("file" + i + ".txt"));
        }

        monitor.reset();
        //20 x FileObject + 1 File.listFiles
        FileObject[] children = parent.getChildren();
        monitor.getResults().assertResult(1, StatFiles.ALL);
        monitor.getResults().assertResult(1, StatFiles.READ);
        for (FileObject ch : children) {
            assertNull("No sibling", FileUtil.findBrother(ch, "exe"));
        }
        monitor.getResults().assertResult(1, StatFiles.ALL);
        monitor.getResults().assertResult(1, StatFiles.READ);
    }

    public void testLockFile() throws IOException {
        FileObject fobj = getFileObject(testFile);
        monitor.reset();
        final FileLock lock = fobj.lock();
        try {
            // TODO fragile
            int expectedCount = 0;
            if (Utilities.isUnix()) {
                // called File.toURI() from FileUtil.normalizeFile()
                expectedCount++;
                // sun.awt.PlatformGraphicsInfo.getDefaultHeadlessProperty probes a .so or .dylib
                // Runtime.version().feature() > 18
                if (Integer.parseInt(System.getProperty("java.version").split("\\.")[0]) > 18) {
                    expectedCount++;
                }
            }
            // we check canWrite once
            monitor.getResults().assertResult(1, StatFiles.WRITE);
            // adding one for the canWrite access above
            monitor.getResults().assertResult(1 + expectedCount, StatFiles.ALL);
            //second time
            monitor.reset();
            FileLock lock2 = null;
            try {
                lock2 = fobj.lock();
                fail();
            } catch (IOException ex) {
            }
            // again one for canWrite
            monitor.getResults().assertResult(1 + expectedCount, StatFiles.ALL);
        } finally {
            lock.releaseLock();
        }
    }

    public void testGetFileObject2() throws IOException {
        FileObject fobj = getFileObject(testFile);
        FileObject parent = fobj.getParent();
        parent = parent.createFolder("parent");
        File nbbuild = new File(getFile(parent), "nbbuild");
        File pXml = new File(nbbuild, "project.xml");
        assertTrue(nbbuild.mkdir());
        assertTrue(pXml.createNewFile());
        monitor.reset();
        FileObject ch = parent.getFileObject("nbbuild/project.xml");
        monitor.getResults().assertResult(2, StatFiles.ALL);
        monitor.getResults().assertResult(2, StatFiles.READ);
        //second time
        monitor.reset();
        ch = parent.getFileObject("nbbuild/project.xml");
        monitor.getResults().assertResult(0, StatFiles.ALL);
    }

    public void testIssueFileObject() throws IOException {
        FileObject parent = FileBasedFileSystem.getFileObject(testFile).getParent();
        assertGC("NamingFactory not cleared.", new WeakReference<FileNaming>(NamingFactory.fromFile(testFile)));

        //parent exists with cached info + testFile not exists
        monitor.reset();
        assertGC("", new WeakReference<FileObject>(FileBasedFileSystem.getFileObject(testFile)));
        assertNotNull(FileBasedFileSystem.getFileObject(testFile));
        monitor.getResults().assertResult(2, StatFiles.ALL);
        monitor.getResults().assertResult(2, StatFiles.READ);

        //parent not exists + testFile not exists
        monitor.reset();
        parent = null;
        assertGC("", new WeakReference<FileObject>(parent));
        assertGC("", new WeakReference<FileObject>(FileBasedFileSystem.getFileObject((testFile))));
        assertGC("NamingFactory not cleared.", new WeakReference<FileNaming>(NamingFactory.fromFile(testFile)));
        assertNotNull(FileBasedFileSystem.getFileObject((testFile)));
        monitor.getResults().assertResult(3, StatFiles.ALL);
        monitor.getResults().assertResult(3, StatFiles.READ);


        parent = FileBasedFileSystem.getFileObject((testFile)).getParent();
        assertGC("NamingFactory not cleared.", new WeakReference<FileNaming>(NamingFactory.fromFile(testFile)));
        monitor.reset();
        FileObject fobj = FileBasedFileSystem.getFileObject((testFile));
        assertNotNull(fobj);
        monitor.getResults().assertResult(2, StatFiles.ALL);
        monitor.getResults().assertResult(2, StatFiles.READ);

        monitor.reset();
        File tFile = testFile.getParentFile();
        monitor.getResults().assertResult(0, StatFiles.ALL);
        while (tFile != null && tFile.getParentFile() != null) {
            monitor.getResults().assertResult(0, StatFiles.ALL);
            tFile = tFile.getParentFile();
        }
        //second time        
        monitor.reset();
        fobj = getFileObject(testFile);
        monitor.getResults().assertResult(0, StatFiles.ALL);
    }

    public void testGetParent() {
        FileObject fobj = getFileObject(testFile);
        monitor.reset();
        FileObject parent = fobj.getParent();
        monitor.getResults().assertResult(0, StatFiles.ALL);
        monitor.reset();
        parent = fobj.getParent();
        monitor.getResults().assertResult(0, StatFiles.ALL);

        //second time
        monitor.reset();
        parent = fobj.getParent();
        monitor.getResults().assertResult(0, StatFiles.ALL);
    }

    //on trunk fails: expected:<0> but was:<1>
    public void testGetCachedFileObject() throws IOException {
        FileObject fobj = getFileObject(testFile);
        FileObject parent = fobj.getParent();
        parent = parent.createFolder("parent");
        FileObject child = parent.createData("child");
        monitor.reset();
        FileObject ch = parent.getFileObject("child");
        monitor.getResults().assertResult(0, StatFiles.ALL);
        //second time
        monitor.reset();
        ch = parent.getFileObject("child");
        monitor.getResults().assertResult(0, StatFiles.ALL);
    }

    //on trunk fails: expected:<2> but was:<5>
    public void testGetFileObject() throws IOException {
        FileObject fobj = getFileObject(testFile);
        FileObject parent = fobj.getParent();
        parent = parent.createFolder("parent");
        assertTrue(new File(getFile(parent), "child").createNewFile());
        monitor.reset();
        FileObject ch = parent.getFileObject("child");
        monitor.getResults().assertResult(2, StatFiles.ALL);
        monitor.getResults().assertResult(2, StatFiles.READ);
        //second time
        monitor.reset();
        ch = parent.getFileObject("child");
        monitor.getResults().assertResult(0, StatFiles.ALL);
    }


    //on trunk fails: expected:<1> but was:<3>    
    public void testGetCachedChild() {
        FileObject fobj = getFileObject(testFile);
        FileObject parent = fobj.getParent();
        monitor.reset();
        FileObject[] childs = parent.getChildren();
        monitor.getResults().assertResult(1, StatFiles.ALL);
        monitor.getResults().assertResult(1, StatFiles.READ);
        //second time
        monitor.reset();
        childs = parent.getChildren();
        monitor.getResults().assertResult(0, StatFiles.ALL);
    }


    //on trunk fails: expected:<21> but was:<91>    
    public void testGetChildren() throws IOException {
        FileObject fobj = getFileObject(testFile);
        FileObject parent = fobj.getParent();
        parent = parent.createFolder("parent");
        File pFile = getFile(parent);
        for (int i = 0; i < 10; i++) {
            assertTrue(new File(pFile, "file" + i).createNewFile());
            assertTrue(new File(pFile, "fold" + i).mkdir());
        }
        monitor.reset();
        FileObject[] children = parent.getChildren();
        //20 x children, 1 x File.listFiles 
        monitor.getResults().assertResult(21, StatFiles.ALL);
        monitor.getResults().assertResult(21, StatFiles.READ);
        //second time
        monitor.reset();
        children = parent.getChildren();
        monitor.getResults().assertResult(0, StatFiles.ALL);
    }

    public void testRefreshFile() {
        FileObject fobj = getFileObject(testFile);
        monitor.reset();
        fobj.refresh();
        monitor.getResults().assertResult(4, StatFiles.ALL);
        monitor.getResults().assertResult(4, StatFiles.READ);
        //second time
        monitor.reset();
        fobj.refresh();
        monitor.getResults().assertResult(4, StatFiles.ALL);
        monitor.getResults().assertResult(4, StatFiles.READ);
    }

    private void monitor() {
        monitor.setMonitor(new StatFiles.Monitor() {

            public void checkRead(File file) {
            }

            public void checkAll(File file) {
            }
        });
    }
}
