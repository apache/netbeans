/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.jgit.Utils;

/**
 *
 * @author Tomas Stupka
 */
public class CleanTest extends AbstractGitTestCase {
    private Repository repository;
    private File workDir;

    public CleanTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }
    
    public void testCleanFileAdded() throws Exception {
        File file = new File(workDir, "file");
        file.createNewFile();

        assertNullDirCacheEntry(Collections.singleton(file));
        GitClient client = getClient(workDir);
        assertTrue(file.exists());
        client.add(new File[] { file }, NULL_PROGRESS_MONITOR);
        assertDirCacheEntry(Collections.singleton(file));

        Monitor m = new Monitor();
        client.addNotificationListener(m);
                
        client.clean(new File[] { file }, m);
        assertTrue(file.exists());  
        assertTrue(m.notifiedFiles.isEmpty());
    }
    
    public void testCleanAddedTree() throws Exception {
        File folder = new File(workDir, "folder");
        folder.mkdirs();
        File folder1 = new File(folder, "folder1");
        folder1.mkdirs();
        File file11 = new File(folder1, "file11");
        file11.createNewFile();
        File folder2 = new File(folder, "folder2");
        folder2.mkdirs();
        File file21 = new File(folder2, "file21");
        file21.createNewFile();

        assertNullDirCacheEntry(Collections.singleton(folder));
        GitClient client = getClient(workDir);
        assertTrue(folder.exists());
        client.add(new File[] { file11, file21 }, NULL_PROGRESS_MONITOR);
        assertDirCacheEntry(Arrays.asList(new File[] {file11, file21}));

        Monitor m = new Monitor();
        client.addNotificationListener(m);
        
        File[] files = new File[] {folder, folder1, folder2, file11, file21};
        for (File file : files) {            
            if(!file.exists()) {
                fail("file does not exist " + file);
            }
        }
                
        client.clean(new File[] { folder }, m);
                
        for (File file : files) {            
            if(!file.exists()) {
                fail("file does not exist " + file);
            }
        }                        
        assertTrue(m.notifiedFiles.isEmpty());
    }
    
    public void testCleanFileIgnored() throws Exception {
        File file = new File(workDir, "file");
        file.createNewFile();
        File gitignore = new File(workDir, ".gitignore");
        gitignore.createNewFile();
        FileOutputStream fos = new FileOutputStream(gitignore);
        fos.write(file.getName().getBytes());
        fos.flush();

        assertNullDirCacheEntry(Collections.singleton(file));
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        
        assertTrue(file.exists());        
        client.clean(new File[] { file }, m);
        assertTrue(file.exists());        
        assertTrue(m.notifiedFiles.isEmpty());
    }
    
    public void testCleanFolderIgnored() throws Exception {
        File folder = new File(workDir, "folder");
        folder.createNewFile();
        File gitignore = new File(workDir, ".gitignore");
        gitignore.createNewFile();
        FileOutputStream fos = new FileOutputStream(gitignore);
        fos.write(folder.getName().getBytes());
        fos.flush();

        assertNullDirCacheEntry(Collections.singleton(folder));
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        
        assertTrue(folder.exists());        
        client.clean(new File[] { folder }, m);
        assertTrue(folder.exists());        
        assertTrue(m.notifiedFiles.isEmpty());
    }
    
    public void testCleanIgnoredFolderTree() throws Exception {
        File root = new File(workDir, "root");
        File folder1 = new File(root, "folder1");
        File file11 = new File(folder1, "file11");
        File folder2 = new File(root, "folder2");
        File folder11 = new File(folder1, "folder11");
        File file111 = new File(folder11, "file111");
        File folder21 = new File(folder2, "folder21");
        folder11.mkdirs();
        folder21.mkdirs();
        file111.createNewFile();
        file11.createNewFile();
        
        File gitignore = new File(workDir, ".gitignore");        
        FileOutputStream fos = new FileOutputStream(gitignore);
        fos.write(root.getName().getBytes());
        fos.flush();
        
        assertNullDirCacheEntry(Collections.singleton(root));
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        
        File[] files = new File[] { root, folder1, folder2, folder11, folder21, file111, file11 };
        
        for (File file : files) {            
            if(!file.exists()) {
                fail("file does not exist " + file);
            }
        }
        
        client.clean(new File[] { root }, m);

        for (File file : files) {            
            if(!file.exists()) {
                fail("file does not exist " + file);
            }
        }
        
        assertTrue(m.notifiedFiles.isEmpty());
    }
    
    public void testCleanFileVersionedUptodate() throws Exception {
        File file = new File(workDir, "file");
        file.createNewFile();

        assertNullDirCacheEntry(Collections.singleton(file));
        GitClient client = getClient(workDir);
        
        assertTrue(file.exists());
        client.add(new File[] { file }, NULL_PROGRESS_MONITOR);
        client.commit(new File[] { file }, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        assertDirCacheEntry(Collections.singleton(file));
        
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        
        client.clean(new File[] { file }, m);
        assertTrue(file.exists());        
        assertTrue(m.notifiedFiles.isEmpty());
    }
    
    public void testCleanUversionedFile() throws Exception {
        File file = new File(workDir, "file");
        file.createNewFile();

        assertNullDirCacheEntry(Collections.singleton(file));
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        
        assertTrue(file.exists());
        client.clean(new File[] { file }, m);
        assertFalse(file.exists());
        
        assertEquals(Collections.singleton(file), m.notifiedFiles);        
    }
    
    public void testCleanUnversionedFolder() throws Exception {
        File folder = new File(workDir, "folder");
        folder.mkdirs();

        assertNullDirCacheEntry(Collections.singleton(folder));
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        
        assertTrue(folder.exists());
        client.clean(new File[] { folder }, m);     
        assertFalse(folder.exists());
        
        assertEquals(Collections.singleton(folder), m.notifiedFiles);
    }
    
    public void testCleanUnversionedFolderTree() throws Exception {
        File root = new File(workDir, "root");
        File folder1 = new File(root, "folder1");
        File file11 = new File(folder1, "file11");
        File folder2 = new File(root, "folder2");
        File folder11 = new File(folder1, "folder11");
        File file111 = new File(folder11, "file111");
        File folder21 = new File(folder2, "folder21");
        folder11.mkdirs();
        folder21.mkdirs();
        file111.createNewFile();
        file11.createNewFile();
        
        assertNullDirCacheEntry(Collections.singleton(root));
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        
        File[] files = new File[] { root, folder1, folder2, folder11, folder21, file111, file11 };
        
        for (File file : files) {            
            if(!file.exists()) {
                fail("file does not exist " + file);
            }
        }
        
        client.clean(new File[] { root }, m);
        assertFalse(root.exists());
                
        for (File file : files) {            
            if(file.exists()) {
                fail("file exists " + file);
            }
        }
        
        assertEquals(files.length, m.notifiedFiles.size());
        for (File file : files) {
            if(!m.notifiedFiles.contains(file)) {
                fail("file " + file + " wans't notified");
            }
        }
        assertNotifiedCleanedFiles(m.notifiedFiles, files);
    }

    public void testCleanWorkingTree() throws Exception {
        File root = new File(workDir, "root");                
        root.mkdirs();
                
        File addedFile = new File(root, "addedFile");         
        addedFile.createNewFile();
        
        File ignoredFile = new File(root, "ignoredFile");
        ignoredFile.createNewFile();
        
        File ignoredFolder = new File(root, "ignoredFolder");
        ignoredFolder.mkdirs();
        
        File nestedIgnoredFile = new File(ignoredFolder, "nestedIgnoredFile");
        nestedIgnoredFile.createNewFile();
        
        File unversionedFile = new File(root, "unversionedFile");         
        unversionedFile.createNewFile();
        
        File folder = new File(root, "folder");
        folder.mkdirs();
        
        File nestedUnversionedFile = new File(folder, "nestedUnversionedFile");
        nestedUnversionedFile.createNewFile();
        
        File nestedAddedFile = new File(folder, "nestedAddedFile");
        nestedAddedFile.createNewFile();
        
        File nestedUnversionedFolder = new File(folder, "nestedUnversionedFolder");
        nestedUnversionedFolder.mkdirs();
                
        File gitignore = new File(workDir, ".gitignore");        
        FileOutputStream fos = new FileOutputStream(gitignore);
        fos.write(ignoredFile.getName().getBytes());
        fos.write("\n".getBytes());
        fos.write(ignoredFolder.getName().getBytes());
        fos.flush();
        
        assertNullDirCacheEntry(Collections.singleton(root));
        GitClient client = getClient(workDir);
        
        File[] addedFiles = new File[] {addedFile, nestedAddedFile};
        client.add(addedFiles, NULL_PROGRESS_MONITOR);
        assertDirCacheEntry(Arrays.asList(addedFiles));
        
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        
        File[] files = new File[] { root, addedFile, ignoredFile, ignoredFolder, nestedIgnoredFile, unversionedFile, folder, nestedUnversionedFile, nestedAddedFile, nestedUnversionedFolder };
        for (File file : files) {            
            if(!file.exists()) {
                fail("file exists " + file);
            }
        }
        
        client.clean(new File[] { root }, m);
        
        assertTrue(root.exists());
        assertTrue(addedFile.exists());
        assertTrue(ignoredFile.exists());
        assertTrue(ignoredFolder.exists());
        assertTrue(nestedIgnoredFile.exists());
        assertFalse(unversionedFile.exists());
        assertTrue(folder.exists());
        assertFalse(nestedUnversionedFile.exists());
        assertTrue(nestedAddedFile.exists());
        assertFalse(nestedUnversionedFolder.exists());
         
        assertNotifiedCleanedFiles(m.notifiedFiles, new File[] {unversionedFile, nestedUnversionedFile, nestedUnversionedFolder});
    }
    
    // must not return status for nested repositories
    public void testCleanNested () throws Exception {
        File f = new File(workDir, "f");
        write(f, "file");
        
        GitClient client = getClient(workDir);
        client.add(new File[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new File[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        File nested = new File(workDir, "nested");
        nested.mkdirs();
        File f2 = new File(nested, "f");
        write(f2, "file");
        GitClient clientNested = getClient(nested);
        clientNested.init(NULL_PROGRESS_MONITOR);
        
        client.clean(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, f, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, nested, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
        
        statuses = clientNested.getStatus(new File[] { nested }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, nested, f2, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
    }
    
    private void assertDirCacheEntry (Collection<File> files) throws IOException {
        assertDirCacheEntry(repository, workDir, files);
    }
   
    private void assertNullDirCacheEntry (Collection<File> files) throws Exception {
        DirCache cache = repository.lockDirCache();
        for (File f : files) {
            DirCacheEntry e = cache.getEntry(Utils.getRelativePath(workDir, f));
            assertNull(e);
        }
        cache.unlock();
    }

    private void assertNotifiedCleanedFiles(Collection<File> c , File... files) {
        assertEquals(files.length, c.size());
        for (File file : files) {
            if(!c.contains(file)) {
                fail("file " + file + " was not notified");
            }
        }
    }
}
