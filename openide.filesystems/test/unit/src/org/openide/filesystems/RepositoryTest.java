/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
