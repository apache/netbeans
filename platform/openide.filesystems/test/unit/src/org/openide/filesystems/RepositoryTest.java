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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.junit.NbTestCase;
import org.openide.util.RequestProcessor;
import org.openide.util.test.MockLookup;

public class RepositoryTest extends NbTestCase {
    
    public RepositoryTest(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        Repository.reset();
        MockLookup.setInstances();
    }

    public void testInitializationFromTwoThreads() throws Exception {
        final AtomicInteger addCnt = new AtomicInteger();
        final AtomicInteger removeCnt = new AtomicInteger();
        Repository r = new Repository(new MultiFileSystem() {
            public @Override void addNotify() {
                addCnt.incrementAndGet();
                super.addNotify();
            }
            public @Override void removeNotify() {
                removeCnt.incrementAndGet();
                super.removeNotify();
            }
        });
        MockLookup.setInstances(r);
        assertSame("Repo created and it is ours", r, Repository.getDefault());
        final AtomicReference<Repository> fromRunnable = new AtomicReference<Repository>();
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                fromRunnable.set(Repository.getDefault());
            }
        }).waitFinished();
        assertSame("Both repositories are same", r, fromRunnable.get());
        assertEquals("One add", 1, addCnt.get());
        assertEquals("No remove", 0, removeCnt.get());
    }

    public void testDynamicSystemsCanAlsoBeBehindLayers() throws Exception {
        final File dir1 = new File(getWorkDir(), "dir1");
        final File dir2 = new File(getWorkDir(), "dir2");
        class MyFS1 extends LocalFileSystem {
            public MyFS1() throws Exception {
                dir1.mkdirs();
                setRootDirectory(dir1);
                getRoot().setAttribute("fallback", true);
                FileObject fo1 = FileUtil.createData(getRoot(), "test/data.txt");
                fo1.setAttribute("one", 1);
                write(fo1, "fileone");
                FileObject fo11 = FileUtil.createData(getRoot(), "test-fs-is-there.txt");
                write(fo11, "hereIam");
            }
        }
        class MyFS2 extends LocalFileSystem {
            public MyFS2() throws Exception {
                dir2.mkdirs();
                setRootDirectory(dir2);
                FileObject fo1 = FileUtil.createData(getRoot(), "test/data.txt");
                fo1.setAttribute("two", 1);
                write(fo1, "two");
            }
        }
        MockLookup.setInstances(new MyFS1(), new MyFS2());
        FileObject global = FileUtil.getConfigFile("test/data.txt");
        assertEquals("Second file system takes preceedence", "two", global.asText());
        assertTrue("Still valid", global.isValid());
        FileObject fo = FileUtil.getConfigFile("test-fs-is-there.txt");
        assertNotNull("File found: " + Arrays.toString(FileUtil.getConfigRoot().getChildren()), fo);
        assertEquals("Text is correct", "hereIam", fo.asText());
    }
    private static void write(FileObject fo, String txt) throws IOException {
        OutputStream os = fo.getOutputStream();
        os.write(txt.getBytes());
        os.close();
    }

    public void testStatus() throws Exception {
        FileObject r = FileUtil.getConfigRoot();
        StatusDecorator s = r.getFileSystem().getDecorator();
        FileObject f = r.createData("f");
        f.setAttribute("displayName", "F!");
        assertEquals("F!", s.annotateName("f", Collections.singleton(f)));
        // XXX test SystemFileSystem.localizingBundle, iconBase, SystemFileSystem.icon
        // (move tests from org.netbeans.core.projects.SystemFileSystemTest)
    }

    public void testContentOfFileSystemIsInfluencedByLookup () throws Exception {
        FileSystem mem = FileUtil.createMemoryFileSystem();
        String dir = "/yarda/own/file";
        org.openide.filesystems.FileUtil.createFolder (mem.getRoot (), dir);

        // XXX fails to test that Repo contents are right from *initial* lookup
        // (try commenting out 'resultChanged(null);' in ExternalUtil.MainFS - still passes)
        assertNull ("File is not there yet", FileUtil.getConfigFile(dir));
        MockLookup.setInstances(mem);
        try {
            assertNotNull ("The file is there now", FileUtil.getConfigFile(dir));
        } finally {
            MockLookup.setInstances();
        }
        assertNull ("File is no longer there", FileUtil.getConfigFile(dir));
    }

}
