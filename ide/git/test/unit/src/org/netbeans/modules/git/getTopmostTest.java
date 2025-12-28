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

package org.netbeans.modules.git;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Set;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.core.Utils;
import org.openide.util.NbPreferences;

/**
 *
 * @author tomas
 */
public class getTopmostTest extends NbTestCase {
    private File testBase;
    private File userdir;
    private GitVCS mvcs;

    public getTopmostTest(String arg0) {
        super(arg0);
        mvcs = new GitVCS();
    }

    @Override
    protected void setUp() throws Exception {
        testBase = Files.createTempDirectory("NBGitTest").toFile();
        userdir = new File(testBase, "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        super.setUp();
        clearCachedValues();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        NbPreferences.root().flush();
        NbPreferences.root().sync();
        Files.walkFileTree(testBase.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                if (e == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                } else {
                    throw e;
                }
            }
        });
    }

    public void testGetTopmostManagedParentR1R2() throws Exception {
        File r1 = createFolder("r1");
        File hgr1 = createFolder(r1, ".git");
        File r1f1 = createFile(r1, "f1");
        File r2 = createFolder(r1, "r2");
        File hgr2 = createFolder(r2, ".git");
        File r2f1 = createFile(r2, "f1");

        // test
        File tm1 = mvcs.getTopmostManagedAncestor(r1f1);
        assertEquals(r1, tm1);
        File tm2 = mvcs.getTopmostManagedAncestor(r2f1);
        assertEquals(r1, tm2);

        // test again - to get cached
        tm1 = mvcs.getTopmostManagedAncestor(r1f1);
        assertEquals(r1, tm1);
        tm1 = mvcs.getTopmostManagedAncestor(r1);
        assertEquals(r1, tm1);
        tm2 = mvcs.getTopmostManagedAncestor(r2f1);
        assertEquals(r1, tm2);
        tm2 = mvcs.getTopmostManagedAncestor(r2);
        assertEquals(r1, tm2);
    }

    public void testGetTopmostManagedParentR2R1() throws Exception {
        File r1 = createFolder("r1");
        File hgr1 = createFolder(r1, ".git");
        File r1f1 = createFile(r1, "f1");
        File r2 = createFolder(r1, "r2");
        File hgr2 = createFolder(r2, ".git");
        File r2f1 = createFile(r2, "f1");

        // test
        File tm2 = mvcs.getTopmostManagedAncestor(r2f1);
        assertEquals(r1, tm2);
        File tm1 = mvcs.getTopmostManagedAncestor(r1f1);
        assertEquals(r1, tm1);

        // test again - to get cached
        tm2 = mvcs.getTopmostManagedAncestor(r2f1);
        assertEquals(r1, tm2);
        tm2 = mvcs.getTopmostManagedAncestor(r2);
        assertEquals(r1, tm2);
        tm2 = mvcs.getTopmostManagedAncestor(r1);
        assertEquals(r1, tm2);
    }

    public void testGetRepositoryRoot() throws Exception {
        File r1   = createFolder(  "r1");
        File hgr1 = createFolder(r1,  ".git");
        File r1f1 = createFile(r1,    "f1");
        File r2   = createFolder(r1,  "r2");
        File hgr2 = createFolder(r2,     ".git");
        File r2f1 = createFile(r2,       "f1");
        File r2f2 = createFile(r2,       "f2");

        // test
        File tm1 = Git.getInstance().getRepositoryRoot(r1f1);
        assertEquals(r1, tm1);
        tm1 = Git.getInstance().getRepositoryRoot(r1);
        assertEquals(r1, tm1);
        File tm2 = Git.getInstance().getRepositoryRoot(r2f1);
        assertEquals(r2, tm2);
        tm2 = Git.getInstance().getRepositoryRoot(r2f2);
        assertEquals(r2, tm2);
        tm2 = Git.getInstance().getRepositoryRoot(r2);
        assertEquals(r2, tm2);

        // test again - to get cached
        tm1 = Git.getInstance().getRepositoryRoot(r1f1);
        assertEquals(r1, tm1);
        tm1 = Git.getInstance().getRepositoryRoot(r1);
        assertEquals(r1, tm1);
        tm2 = Git.getInstance().getRepositoryRoot(r2f1);
        assertEquals(r2, tm2);
        tm2 = Git.getInstance().getRepositoryRoot(r2f2);
        assertEquals(r2, tm2);
        tm2 = Git.getInstance().getRepositoryRoot(r2);
        assertEquals(r2, tm2);
    }

    public void testGetRepositoryRootDeeperFile() throws Exception {
        File r1   = createFolder(  "r1");
        File hgr1 = createFolder(r1,  ".git");
        File r1f1 = createFile(r1,    "f1");
        File r2   = createFolder(r1,  "r2");
        File hgr2 = createFolder(r2,     ".git");
        File r2fo1 = createFolder(r2,       "f01");
        File r2fo1fo2 = createFolder(r2,       "f02");
        File r2fo1fo2f1 = createFile(r2fo1,       "f1");
        File r2fo1fo3 = createFolder(r2fo1,       "f03");
        createFile(r2fo1fo3, ".git"); //submodules, .git can also be a file
        File r3   = createFolder("r3");
        createFile(r3, ".git"); //submodules, .git can also be a file

        // test
        File rr = Git.getInstance().getRepositoryRoot(r1);
        assertEquals(r1, rr);
        rr = Git.getInstance().getRepositoryRoot(r2);
        assertEquals(r2, rr);
        rr = Git.getInstance().getRepositoryRoot(r2fo1fo2);
        assertEquals(r2, rr);
        rr = Git.getInstance().getRepositoryRoot(r2fo1);
        assertEquals(r2, rr);
        rr = Git.getInstance().getRepositoryRoot(r2fo1fo2f1);
        assertEquals(r2, rr);

        rr = Git.getInstance().getRepositoryRoot(r2);
        assertEquals(r2, rr);
        rr = Git.getInstance().getRepositoryRoot(r2fo1fo2);
        assertEquals(r2, rr);
        rr = Git.getInstance().getRepositoryRoot(r2fo1);
        assertEquals(r2, rr);
        rr = Git.getInstance().getRepositoryRoot(r2fo1fo2f1);
        assertEquals(r2, rr);                      
        rr = Git.getInstance().getRepositoryRoot(r2fo1fo3);
        assertEquals(r2fo1fo3, rr);
        rr = Git.getInstance().getRepositoryRoot(r3);
        assertEquals(r3, rr);
    }

    public void testFoldersToRootOverflow() throws Exception {
        File r1   = createFolder(  "r1");
        File hgr1 = createFolder(r1,  ".git");
        
        Map<File, File> m = getFoldersToRoots();

        // test
        File r1f1 = getFile(r1, "file", "s1", 100);
        File tm1 = Git.getInstance().getRepositoryRoot(r1f1);
        assertEquals(r1, tm1);
        assertEquals(101, m.size());

        r1f1 = getFile(r1, "file", "s2", 1500);
        tm1 = Git.getInstance().getRepositoryRoot(r1f1);
        assertEquals(r1, tm1);
        assertEquals(1500, m.size());

        r1f1 = getFile(r1, "file", "s3", 10);
        tm1 = Git.getInstance().getRepositoryRoot(r1f1);
        assertEquals(r1, tm1);
        assertEquals(1500, m.size());
    }

    public void testGetRepositoryRootUnversioned() throws Exception {
        File r1 = createFolder("r1");
        File hgr1 = createFolder(r1, ".git");
        File r1f1 = createFile(r1, "f1");
        File r2 = createFolder(r1, "r2");
        File hgr2 = createFolder(r2, ".git");
        File r2f1 = createFile(r2, "f1");

        // test
        File tm1 = Git.getInstance().getRepositoryRoot(getTempDir());
        assertNull(tm1);
    }

    public void testGetTompomostUnversioned() throws Exception {
        File r1 = createFolder("r1");
        File hgr1 = createFolder(r1, ".git");
        File r1f1 = createFile(r1, "f1");
        File r2 = createFolder(r1, "r2");
        File hgr2 = createFolder(r2, ".git");
        File r2f1 = createFile(r2, "f1");

        // test
        File tm1 = mvcs.getTopmostManagedAncestor(getTempDir());
        assertNull(tm1);
    }

    public void testExcludeUserDir () throws Exception {
        MockServices.setServices(new Class[] {
            GitVCS.class});
        
        Field f = Utils.class.getDeclaredField("unversionedFolders");
        f.setAccessible(true);
        f.set(Utils.class, null);
        // completely ignore userdir being a repo root
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        File dotGitUserDir = createFolder(userdir.getParentFile(), ".git");
        File r1 = createFolder(userdir, "r1");
        File dotGitR1 = createFolder(r1, ".git");
        File r1f1 = createFile(r1, "f1");
        
        // test
        assertEquals(userdir.getParentFile(), Git.getInstance().getRepositoryRoot(userdir.getParentFile()));
        assertNull(mvcs.getTopmostManagedAncestor(userdir));
        assertNull(mvcs.getTopmostManagedAncestor(r1));
        assertNull(mvcs.getTopmostManagedAncestor(r1f1));

        Git.getInstance().versionedFilesChanged();
        f.set(Utils.class, null);
        // version also the userdir
        System.setProperty("versioning.netbeans.user.versioned", "true");
        assertEquals(r1, Git.getInstance().getRepositoryRoot(r1));
        assertEquals(r1, Git.getInstance().getRepositoryRoot(r1f1));
        
        f.set(Utils.class, null);
        System.setProperty("versioning.netbeans.user.versioned", "false");
        // ignore userdir being a subfolder under a repo root
        File r1fld1 = createFolder(r1, "folder1");
        File r1fld1f1 = createFile(r1fld1, "f1");
        System.setProperty("netbeans.user", r1fld1.getAbsolutePath());
        Git.getInstance().versionedFilesChanged();
        assertEquals(r1, Git.getInstance().getRepositoryRoot(r1));
        assertEquals(r1, Git.getInstance().getRepositoryRoot(r1f1));
        assertEquals(userdir.getParentFile(), mvcs.getTopmostManagedAncestor(r1));
        assertEquals(userdir.getParentFile(), mvcs.getTopmostManagedAncestor(r1f1));
        assertNull(mvcs.getTopmostManagedAncestor(r1fld1));
        assertNull(mvcs.getTopmostManagedAncestor(r1fld1f1));

        // logic in VersioningManager used to mark all parents of an excluded tree as unversioned too - that's obviously wrong
        // we want to exclude only userdir, not the whole disk
        assertNull(VersioningSupport.getOwner(r1fld1f1));
        assertNull(VersioningSupport.getOwner(r1fld1));
        
        // only subtree is unversioned, r1 itself is versioned
        assertEquals(GitVCS.class, VersioningSupport.getOwner(r1).getClass());
        assertEquals(GitVCS.class, VersioningSupport.getOwner(r1f1).getClass());
    }

    private void clearCachedValues() throws Exception {
        getFoldersToRoots().clear();
        getKnownRoots().clear();
    }

    private File createFile(File folder, String f) throws IOException {
        File file = new File(folder, f);
        file.createNewFile();
        file.deleteOnExit();
        return file;
    }

    private File createFolder(String f) throws IOException {
        return createFolder(testBase, f);
    }

    private File createFolder(File parentfolder, String f) {
        File folder = new File(parentfolder, f);
        folder.mkdirs();
        folder.deleteOnExit();
        return folder;
    }

    private File getFile(File parentfolder, String f, String segment, int segments) {
        File file = new File(parentfolder, segment);
        for (int i = 2; i < segments; i++) {
            file = new File(file, segment);
        }
        return new File(file, f);
    }

    private static File tmp = null;

    private Map<File, File> getFoldersToRoots() throws SecurityException, IllegalArgumentException, Exception, IllegalAccessException {
        Field f = null;
        try {
            f = Git.class.getDeclaredField("rootsToFile");
        } catch (Exception ex) {
            throw ex;
        }
        f.setAccessible(true);
        Object rootsToFile = f.get(Git.getInstance());
        f = rootsToFile.getClass().getDeclaredField("files");
        f.setAccessible(true);
        Map<File, File> m = (Map<File, File>) f.get(rootsToFile);
        return m;
    }

    private Set<File> getKnownRoots() throws SecurityException, IllegalArgumentException, Exception, IllegalAccessException {
        Field f = null;
        try {
            f = Git.class.getDeclaredField("knownRoots");
        } catch (Exception ex) {
            throw ex;
        }
        f.setAccessible(true);
        Set<File> m = (Set<File>) f.get(Git.getInstance());
        return m;
    }

    private File getTempDir() {
        if(tmp == null) {
            File tmpDir = new File(System.getProperty("java.io.tmpdir"));
            tmp = new File(tmpDir, "gtmt-" + Long.toString(System.currentTimeMillis()));
            tmp.deleteOnExit();
        }
        return tmp;
    }
}
