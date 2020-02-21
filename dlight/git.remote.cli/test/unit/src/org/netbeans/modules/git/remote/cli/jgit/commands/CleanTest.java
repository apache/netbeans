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

package org.netbeans.modules.git.remote.cli.jgit.commands;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitStatus;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class CleanTest extends AbstractGitTestCase {
    private JGitRepository repository;
    private VCSFileProxy workDir;
    private static final boolean SKIP_FAILED_TESTS = true;

    public CleanTest (String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList("testCleanNested").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }
    
    public void testCleanFileAdded() throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxySupport.createNew(file);

        assertNullDirCacheEntry(Collections.singleton(file));
        GitClient client = getClient(workDir);
        assertTrue(file.exists());
        client.add(new VCSFileProxy[] { file }, NULL_PROGRESS_MONITOR);
        assertDirCacheEntry(Collections.singleton(file));

        Monitor m = new Monitor();
        client.addNotificationListener(m);
                
        client.clean(new VCSFileProxy[] { file }, m);
        assertTrue(file.exists());  
        assertTrue(m.notifiedFiles.isEmpty());
    }
    
    public void testCleanAddedTree() throws Exception {
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy folder1 = VCSFileProxy.createFileProxy(folder, "folder1");
        VCSFileProxySupport.mkdirs(folder1);
        VCSFileProxy file11 = VCSFileProxy.createFileProxy(folder1, "file11");
        VCSFileProxySupport.createNew(file11);
        VCSFileProxy folder2 = VCSFileProxy.createFileProxy(folder, "folder2");
        VCSFileProxySupport.mkdirs(folder2);
        VCSFileProxy file21 = VCSFileProxy.createFileProxy(folder2, "file21");
        VCSFileProxySupport.createNew(file21);

        assertNullDirCacheEntry(Collections.singleton(folder));
        GitClient client = getClient(workDir);
        assertTrue(folder.exists());
        client.add(new VCSFileProxy[] { file11, file21 }, NULL_PROGRESS_MONITOR);
        assertDirCacheEntry(Arrays.asList(new VCSFileProxy[] {file11, file21}));

        Monitor m = new Monitor();
        client.addNotificationListener(m);
        
        VCSFileProxy[] files = new VCSFileProxy[] {folder, folder1, folder2, file11, file21};
        for (VCSFileProxy file : files) {            
            if(!file.exists()) {
                fail("file does not exist " + file);
            }
        }
                
        client.clean(new VCSFileProxy[] { folder }, m);
                
        for (VCSFileProxy file : files) {            
            if(!file.exists()) {
                fail("file does not exist " + file);
            }
        }                        
        assertTrue(m.notifiedFiles.isEmpty());
    }
    
    public void testCleanFileIgnored() throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxySupport.createNew(file);
        VCSFileProxy gitignore = VCSFileProxy.createFileProxy(workDir, GitConstants.GITIGNORE_FILENAME);
        VCSFileProxySupport.createNew(gitignore);
        OutputStream fos = VCSFileProxySupport.getOutputStream(gitignore);
        fos.write(file.getName().getBytes());
        fos.flush();

        assertNullDirCacheEntry(Collections.singleton(file));
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        
        assertTrue(file.exists());        
        client.clean(new VCSFileProxy[] { file }, m);
        assertTrue(file.exists());        
        assertTrue(m.notifiedFiles.isEmpty());
    }
    
    public void testCleanFolderIgnored() throws Exception {
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxySupport.createNew(folder);
        VCSFileProxy gitignore = VCSFileProxy.createFileProxy(workDir, GitConstants.GITIGNORE_FILENAME);
        VCSFileProxySupport.createNew(gitignore);
        OutputStream fos = VCSFileProxySupport.getOutputStream(gitignore);
        fos.write(folder.getName().getBytes());
        fos.flush();

        assertNullDirCacheEntry(Collections.singleton(folder));
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        
        assertTrue(folder.exists());        
        client.clean(new VCSFileProxy[] { folder }, m);
        assertTrue(folder.exists());        
        assertTrue(m.notifiedFiles.isEmpty());
    }
    
    public void testCleanIgnoredFolderTree() throws Exception {
        VCSFileProxy root = VCSFileProxy.createFileProxy(workDir, "root");
        VCSFileProxy folder1 = VCSFileProxy.createFileProxy(root, "folder1");
        VCSFileProxy file11 = VCSFileProxy.createFileProxy(folder1, "file11");
        VCSFileProxy folder2 = VCSFileProxy.createFileProxy(root, "folder2");
        VCSFileProxy folder11 = VCSFileProxy.createFileProxy(folder1, "folder11");
        VCSFileProxy file111 = VCSFileProxy.createFileProxy(folder11, "file111");
        VCSFileProxy folder21 = VCSFileProxy.createFileProxy(folder2, "folder21");
        VCSFileProxySupport.mkdirs(folder11);
        VCSFileProxySupport.mkdirs(folder21);
        VCSFileProxySupport.createNew(file111);
        VCSFileProxySupport.createNew(file11);
        
        VCSFileProxy gitignore = VCSFileProxy.createFileProxy(workDir, GitConstants.GITIGNORE_FILENAME);        
        OutputStream fos = VCSFileProxySupport.getOutputStream(gitignore);
        fos.write(root.getName().getBytes());
        fos.flush();
        
        assertNullDirCacheEntry(Collections.singleton(root));
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        
        VCSFileProxy[] files = new VCSFileProxy[] { root, folder1, folder2, folder11, folder21, file111, file11 };
        
        for (VCSFileProxy file : files) {            
            if(!file.exists()) {
                fail("file does not exist " + file);
            }
        }
        
        client.clean(new VCSFileProxy[] { root }, m);

        for (VCSFileProxy file : files) {            
            if(!file.exists()) {
                fail("file does not exist " + file);
            }
        }
        
        assertTrue(m.notifiedFiles.isEmpty());
    }
    
    public void testCleanFileVersionedUptodate() throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxySupport.createNew(file);

        assertNullDirCacheEntry(Collections.singleton(file));
        GitClient client = getClient(workDir);
        
        assertTrue(file.exists());
        client.add(new VCSFileProxy[] { file }, NULL_PROGRESS_MONITOR);
        client.commit(new VCSFileProxy[] { file }, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        assertDirCacheEntry(Collections.singleton(file));
        
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        
        client.clean(new VCSFileProxy[] { file }, m);
        assertTrue(file.exists());        
        assertTrue(m.notifiedFiles.isEmpty());
    }
    
    public void testCleanUversionedFile() throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxySupport.createNew(file);

        assertNullDirCacheEntry(Collections.singleton(file));
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        
        assertTrue(file.exists());
        client.clean(new VCSFileProxy[] { file }, m);
        assertFalse(file.exists());
        
        assertEquals(Collections.singleton(file), m.notifiedFiles);        
    }
    
    public void testCleanUnversionedFolder() throws Exception {
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxySupport.mkdirs(folder);

        assertNullDirCacheEntry(Collections.singleton(folder));
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        
        assertTrue(folder.exists());
        client.clean(new VCSFileProxy[] { folder }, m);     
        assertFalse(folder.exists());
        
        assertEquals(Collections.singleton(folder), m.notifiedFiles);
    }
    
    public void testCleanUnversionedFolderTree() throws Exception {
        VCSFileProxy root = VCSFileProxy.createFileProxy(workDir, "root");
        VCSFileProxy folder1 = VCSFileProxy.createFileProxy(root, "folder1");
        VCSFileProxy file11 = VCSFileProxy.createFileProxy(folder1, "file11");
        VCSFileProxy folder2 = VCSFileProxy.createFileProxy(root, "folder2");
        VCSFileProxy folder11 = VCSFileProxy.createFileProxy(folder1, "folder11");
        VCSFileProxy file111 = VCSFileProxy.createFileProxy(folder11, "file111");
        VCSFileProxy folder21 = VCSFileProxy.createFileProxy(folder2, "folder21");
        VCSFileProxySupport.mkdirs(folder11);
        VCSFileProxySupport.mkdirs(folder21);
        VCSFileProxySupport.createNew(file111);
        VCSFileProxySupport.createNew(file11);
        
        assertNullDirCacheEntry(Collections.singleton(root));
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        
        VCSFileProxy[] files = new VCSFileProxy[] { root, folder1, folder2, folder11, folder21, file111, file11 };
        
        for (VCSFileProxy file : files) {            
            if(!file.exists()) {
                fail("file does not exist " + file);
            }
        }
        
        client.clean(new VCSFileProxy[] { root }, m);
        assertFalse(root.exists());
                
        for (VCSFileProxy file : files) {            
            if(file.exists()) {
                fail("file exists " + file);
            }
        }
        
if (CleanCommand.KIT) ;
else files = new VCSFileProxy[]{root};

        assertEquals(files.length, m.notifiedFiles.size());
        for (VCSFileProxy file : files) {
            if(!m.notifiedFiles.contains(file)) {
                fail("file " + file + " wans't notified");
            }
        }
        assertNotifiedCleanedFiles(m.notifiedFiles, files);
    }

    public void testCleanWorkingTree() throws Exception {
        VCSFileProxy root = VCSFileProxy.createFileProxy(workDir, "root");                
        VCSFileProxySupport.mkdirs(root);
                
        VCSFileProxy addedFile = VCSFileProxy.createFileProxy(root, "addedFile");         
        VCSFileProxySupport.createNew(addedFile);
        
        VCSFileProxy ignoredFile = VCSFileProxy.createFileProxy(root, "ignoredFile");
        VCSFileProxySupport.createNew(ignoredFile);
        
        VCSFileProxy ignoredFolder = VCSFileProxy.createFileProxy(root, "ignoredFolder");
        VCSFileProxySupport.mkdirs(ignoredFolder);
        
        VCSFileProxy nestedIgnoredFile = VCSFileProxy.createFileProxy(ignoredFolder, "nestedIgnoredFile");
        VCSFileProxySupport.createNew(nestedIgnoredFile);
        
        VCSFileProxy unversionedFile = VCSFileProxy.createFileProxy(root, "unversionedFile");         
        VCSFileProxySupport.createNew(unversionedFile);
        
        VCSFileProxy folder = VCSFileProxy.createFileProxy(root, "folder");
        VCSFileProxySupport.mkdirs(folder);
        
        VCSFileProxy nestedUnversionedFile = VCSFileProxy.createFileProxy(folder, "nestedUnversionedFile");
        VCSFileProxySupport.createNew(nestedUnversionedFile);
        
        VCSFileProxy nestedAddedFile = VCSFileProxy.createFileProxy(folder, "nestedAddedFile");
        VCSFileProxySupport.createNew(nestedAddedFile);
        
        VCSFileProxy nestedUnversionedFolder = VCSFileProxy.createFileProxy(folder, "nestedUnversionedFolder");
        VCSFileProxySupport.mkdirs(nestedUnversionedFolder);
                
        VCSFileProxy gitignore = VCSFileProxy.createFileProxy(workDir, GitConstants.GITIGNORE_FILENAME);        
        OutputStream fos = VCSFileProxySupport.getOutputStream(gitignore);
        fos.write(ignoredFile.getName().getBytes());
        fos.write("\n".getBytes());
        fos.write(ignoredFolder.getName().getBytes());
        fos.flush();
        
        assertNullDirCacheEntry(Collections.singleton(root));
        GitClient client = getClient(workDir);
        
        VCSFileProxy[] addedFiles = new VCSFileProxy[] {addedFile, nestedAddedFile};
        client.add(addedFiles, NULL_PROGRESS_MONITOR);
        assertDirCacheEntry(Arrays.asList(addedFiles));
        
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        
        VCSFileProxy[] files = new VCSFileProxy[] { root, addedFile, ignoredFile, ignoredFolder, nestedIgnoredFile, unversionedFile, folder, nestedUnversionedFile, nestedAddedFile, nestedUnversionedFolder };
        for (VCSFileProxy file : files) {            
            if(!file.exists()) {
                fail("file exists " + file);
            }
        }
        
        client.clean(new VCSFileProxy[] { root }, m);
        
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
         
        assertNotifiedCleanedFiles(m.notifiedFiles, new VCSFileProxy[] {unversionedFile, nestedUnversionedFile, nestedUnversionedFolder});
    }
    
    // must not return status for nested repositories
    public void testCleanNested () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "file");
        
        GitClient client = getClient(workDir);
        client.add(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new VCSFileProxy[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        VCSFileProxy nested = VCSFileProxy.createFileProxy(workDir, "nested");
        VCSFileProxySupport.mkdirs(nested);
        VCSFileProxy f2 = VCSFileProxy.createFileProxy(nested, "f");
        write(f2, "file");
        GitClient clientNested = getClient(nested);
        clientNested.init(NULL_PROGRESS_MONITOR);
        
        client.clean(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, f, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, nested, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
        
        statuses = clientNested.getStatus(new VCSFileProxy[] { nested }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, nested, f2, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
    }
    
    private void assertDirCacheEntry (Collection<VCSFileProxy> files) throws IOException {
        assertDirCacheEntry(repository, workDir, files);
    }
   
    private void assertNullDirCacheEntry (Collection<VCSFileProxy> files) throws Exception {
        GitClient client = getClient(workDir);
        for (VCSFileProxy f : files) {
            Map<VCSFileProxy, GitStatus> status = client.getStatus(new VCSFileProxy[]{f}, NULL_PROGRESS_MONITOR);
            GitStatus st = status.get(f);
            if (st != null) {
                if (st.getStatusHeadWC() == GitStatus.Status.STATUS_ADDED || st.getStatusHeadWC() == GitStatus.Status.STATUS_IGNORED) {
                    //Ok
                } else {
                    assertFalse(false);
                }
            }
        }
    }

    private void assertNotifiedCleanedFiles(Collection<VCSFileProxy> c , VCSFileProxy... files) {
        assertEquals(files.length, c.size());
        for (VCSFileProxy file : files) {
            if(!c.contains(file)) {
                fail("file " + file + " was not notified");
            }
        }
    }
}
