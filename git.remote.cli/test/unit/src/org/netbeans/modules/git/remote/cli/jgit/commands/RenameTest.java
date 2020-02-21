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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import static junit.framework.Assert.assertTrue;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitStatus;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class RenameTest extends AbstractGitTestCase {
    private JGitRepository repository;
    private VCSFileProxy workDir;

    public RenameTest (String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList("testMoveFileToFolderAfter","testMoveFileToExisting","testMoveFromSymlinkFolder",
                "testMoveTree","testMoveTreeAfter","testRenameFileAfter","testMoveFileToFolder").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }

    public void testRenameUnderItself () throws Exception {
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy target = VCSFileProxy.createFileProxy(folder, "subFolder");
        VCSFileProxySupport.mkdirs(target);

        Monitor m = new Monitor();
        GitClient client = getClient(workDir);
        try {
            client.rename(folder, target, false, m);
            fail();
        } catch (GitException ex) {
            assertEquals("Target folder [folder/subFolder] lies under the source [folder]", ex.getMessage());
        }
        try {
            client.rename(target, folder, false, m);
            fail();
        } catch (GitException ex) {
            assertEquals("Source folder [folder/subFolder] lies under the target [folder]", ex.getMessage());
        }
    }

    public void testRenameFailSourceDoesNotExist () throws Exception {
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy target = VCSFileProxy.createFileProxy(workDir, "target");

        Monitor m = new Monitor();
        assertTrue(VCSFileProxySupport.renameTo(folder, target));
        GitClient client = getClient(workDir);
        try {
            client.rename(folder, target, false, m);
            fail();
        } catch (GitException ex) {
            assertEquals("Source does not exist: " + folder.getPath(), ex.getMessage());
        }
    }

    public void testRenameFailTargetExists () throws Exception {
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy target = VCSFileProxy.createFileProxy(workDir, "target");
        VCSFileProxySupport.mkdirs(target);

        Monitor m = new Monitor();
        GitClient client = getClient(workDir);
        try {
            client.rename(folder, target, false, m);
            fail();
        } catch (GitException ex) {
            assertEquals("Target already exists: " + target.getPath(), ex.getMessage());
        }
    }

    public void testRenameFailTargetOutsideWorkingTree () throws Exception {
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy target = VCSFileProxy.createFileProxy(workDir.getParentFile(), "target");

        Monitor m = new Monitor();
        GitClient client = getClient(workDir);
        try {
            client.rename(folder, target, true, m);
            fail();
        } catch (IllegalArgumentException ex) {
            assertEquals(target.getPath() + " is not under " + workDir.getPath(), ex.getMessage());
        }
    }

    public void testRenameFailTargetDoesNotExist () throws Exception {
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy target = VCSFileProxy.createFileProxy(workDir, "target");

        Monitor m = new Monitor();
        GitClient client = getClient(workDir);
        try {
            client.rename(folder, target, true, m);
            fail();
        } catch (GitException ex) {
            assertEquals("Target does not exist: " + target.getPath(), ex.getMessage());
        }
    }

    public void testRenameFile () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        write(file, "hello");
        VCSFileProxy target = VCSFileProxy.createFileProxy(workDir, "fileRenamed");

        add(file);
        commit(file);
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.rename(file, target, false, m);
        assertTrue(target.exists());
        assertFalse(file.exists());
        assertEquals("hello", read(target));
        assertEquals(Collections.singleton(target), m.notifiedFiles);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { file, target }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, target, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertTrue(statuses.get(target).isRenamed());
        
        statuses = client.getStatus(new VCSFileProxy[0], NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, target, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertTrue(statuses.get(target).isRenamed());
    }

    public void testRenameFileAfter () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        write(file, "hello");
        VCSFileProxy target = VCSFileProxy.createFileProxy(workDir, "fileRenamed");

        add(file);
        commit(file);
        VCSFileProxySupport.renameTo(file, target);
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        assertTrue(target.exists());
        assertFalse(file.exists());
        assertEquals("hello", read(target));
        client.addNotificationListener(m);
        client.rename(file, target, true, m);
        assertEquals(Collections.singleton(target), m.notifiedFiles);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { file, target }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, target, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertTrue(statuses.get(target).isRenamed());
    }

    public void testMoveFileToFolder () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        write(file, "aaa");
        VCSFileProxy target = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(workDir, "folder"), "file");

        add(file);
        commit(file);
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.rename(file, target, false, m);
        assertTrue(target.exists());
        assertFalse(file.exists());
        assertEquals("aaa", read(target));
//        assertEquals(Collections.singleton(target), m.notifiedFiles);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { file, target }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, target, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertTrue(statuses.get(target).isRenamed());

        write(file, "aaa");
        add(file);
        statuses = client.getStatus(new VCSFileProxy[] { file }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        VCSFileProxy target2 = VCSFileProxy.createFileProxy(target.getParentFile(), "moved");
        m = new Monitor();
        client.addNotificationListener(m);
        client.rename(file, target2, false, m);
        assertTrue(target2.exists());
        assertFalse(file.exists());
        assertEquals("aaa", read(target2));
//        assertEquals(Collections.singleton(target2), m.notifiedFiles);
        statuses = client.getStatus(new VCSFileProxy[] { file, target, target2 }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, target, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, target2, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertTrue(statuses.get(target).isRenamed() && statuses.get(target2).isCopied()
                || statuses.get(target2).isRenamed() && statuses.get(target).isCopied());
    }

    public void testMoveFileToFolderAfter () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        write(file, "aaa");
        VCSFileProxy target = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(workDir, "folder"), "file");
        VCSFileProxySupport.mkdirs(target.getParentFile());

        add(file);
        commit(file);
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        assertTrue(VCSFileProxySupport.renameTo(file, target));
        assertTrue(target.exists());
        assertFalse(file.exists());
        assertEquals("aaa", read(target));
        client.addNotificationListener(m);
        client.rename(file, target, true, m);
//        assertEquals(Collections.singleton(target), m.notifiedFiles);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { file, target }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, target, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertTrue(statuses.get(target).isRenamed());

        write(file, "aaa");
        add(file);
        statuses = client.getStatus(new VCSFileProxy[] { file }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        VCSFileProxy target2 = VCSFileProxy.createFileProxy(target.getParentFile(), "moved");
        m = new Monitor();
        assertTrue(VCSFileProxySupport.renameTo(file, target2));
        assertTrue(target2.exists());
        assertFalse(file.exists());
        assertEquals("aaa", read(target2));
        client.addNotificationListener(m);
        client.rename(file, target2, true, m);
        assertEquals(Collections.singleton(target2), m.notifiedFiles);
        statuses = client.getStatus(new VCSFileProxy[] { file, target, target2 }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, target, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, target2, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertTrue(statuses.get(target).isRenamed() && statuses.get(target2).isCopied()
                || statuses.get(target2).isRenamed() && statuses.get(target).isCopied());
    }

    public void testMoveFileToExisting () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        write(file, "aaa");
        VCSFileProxy target = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(workDir, "folder"), "file");

        add(file);
        commit(file);
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.rename(file, target, false, m);
        assertTrue(target.exists());
        assertFalse(file.exists());
        assertEquals("aaa", read(target));
        assertEquals(Collections.singleton(target), m.notifiedFiles);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { file, target }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, target, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertTrue(statuses.get(target).isRenamed());

        write(file, "aaa");
        write(target, "bbb");
        add(target, file);
        commit(target);

        VCSFileProxy target2 = target;
        assertTrue(target2.exists());
        m = new Monitor();
        if (!VCSFileProxySupport.renameTo(file, target2)) {
            VCSFileProxySupport.delete(target2);
            assertTrue(VCSFileProxySupport.renameTo(file, target2));
        }
        client.addNotificationListener(m);
        client.rename(file, target2, true, m);
        assertTrue(m.notifiedWarnings.contains("Index already contains an entry for folder/file"));
        assertEquals(Collections.singleton(target2), m.notifiedFiles);
        statuses = client.getStatus(new VCSFileProxy[] { file, target, target2 }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, target, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, target2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        // aaa -> bbb is a 0% match
        assertFalse(statuses.get(target).isRenamed());
        assertFalse(statuses.get(target).isCopied());
    }

    public void testMoveTree () throws Exception {
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(folder, "file1");
        write(file1, "file1 content");
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(folder, "file2");
        write(file2, "file2 content");
        VCSFileProxy subFolder1 = VCSFileProxy.createFileProxy(folder, "folder1");
        VCSFileProxySupport.mkdirs(subFolder1);
        VCSFileProxy file11 = VCSFileProxy.createFileProxy(subFolder1, "file");
        write(file11, "file11 content");
        VCSFileProxy subFolder2 = VCSFileProxy.createFileProxy(folder, "folder2");
        VCSFileProxySupport.mkdirs(subFolder2);
        VCSFileProxy file21 = VCSFileProxy.createFileProxy(subFolder2, "file");
        write(file21, "file21 content");

        VCSFileProxy target = VCSFileProxy.createFileProxy(workDir, "target");
        VCSFileProxy moved1 = VCSFileProxy.createFileProxy(target, file1.getName());
        VCSFileProxy moved2 = VCSFileProxy.createFileProxy(target, file2.getName());
        VCSFileProxy moved11 = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(target, file11.getParentFile().getName()), file11.getName());
        VCSFileProxy moved21 = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(target, file21.getParentFile().getName()), file21.getName());

        add(file1, file11, file21);
        commit(file1, file11);

        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.rename(folder, target, false, m);
        assertTrue(moved1.exists());
        assertTrue(moved2.exists());
        assertTrue(moved11.exists());
        assertTrue(moved21.exists());
        assertEquals(new HashSet<VCSFileProxy>(Arrays.asList(moved1, moved11, moved21)), m.notifiedFiles);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { file1, file2, file11, file21, moved1, moved11, moved2, moved21 }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertNull(statuses.get(file2));
        assertStatus(statuses, workDir, file11, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertNull(statuses.get(file21));
        assertStatus(statuses, workDir, moved1, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, moved2, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, moved11, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, moved21, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertTrue(statuses.get(moved1).isRenamed());
        assertTrue(statuses.get(moved11).isRenamed());
        // file21 was not committed
        assertFalse(statuses.get(moved21).isRenamed());
    }

    public void testMoveTreeAfter () throws Exception {
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(folder, "file1");
        write(file1, "file1 content");
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(folder, "file2");
        write(file2, "file2 content");
        VCSFileProxy subFolder1 = VCSFileProxy.createFileProxy(folder, "folder1");
        VCSFileProxySupport.mkdirs(subFolder1);
        VCSFileProxy file11 = VCSFileProxy.createFileProxy(subFolder1, "file");
        write(file11, "file11 content");
        VCSFileProxy subFolder2 = VCSFileProxy.createFileProxy(folder, "folder2");
        VCSFileProxySupport.mkdirs(subFolder2);
        VCSFileProxy file21 = VCSFileProxy.createFileProxy(subFolder2, "file");
        write(file21, "file21 content");

        VCSFileProxy target = VCSFileProxy.createFileProxy(workDir, "target");
        VCSFileProxy moved1 = VCSFileProxy.createFileProxy(target, file1.getName());
        VCSFileProxy moved2 = VCSFileProxy.createFileProxy(target, file2.getName());
        VCSFileProxy moved11 = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(target, file11.getParentFile().getName()), file11.getName());
        VCSFileProxy moved21 = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(target, file21.getParentFile().getName()), file21.getName());

        add(file1, file11, file21);
        commit(file1, file11);

        VCSFileProxySupport.renameTo(folder, target);
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.rename(folder, target, true, m);
        assertTrue(moved1.exists());
        assertTrue(moved2.exists());
        assertTrue(moved11.exists());
        assertTrue(moved21.exists());
        assertEquals(new HashSet<VCSFileProxy>(Arrays.asList(moved1, moved11, moved21)), m.notifiedFiles);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { file1, file2, file11, file21, moved1, moved11, moved2, moved21 }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertNull(statuses.get(file2));
        assertStatus(statuses, workDir, file11, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertNull(statuses.get(file21));
        assertStatus(statuses, workDir, moved1, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, moved2, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, moved11, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, moved21, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertTrue(statuses.get(moved1).isRenamed());
        assertTrue(statuses.get(moved11).isRenamed());
        // file21 was not committed
        assertFalse(statuses.get(moved21).isRenamed());
    }

    public void testCancel () throws Exception {
        final VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy file = VCSFileProxy.createFileProxy(folder, "file");
        VCSFileProxySupport.createNew(file);
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(folder, "file2");
        VCSFileProxySupport.createNew(file2);
        final Monitor m = new Monitor();
        final GitClient client = getClient(workDir);
        client.add(new VCSFileProxy[] { }, NULL_PROGRESS_MONITOR);
        final Exception[] exs = new Exception[1];
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.addNotificationListener(m);
                    client.rename(folder, VCSFileProxy.createFileProxy(folder.getParentFile(), "folder2"), false, m);
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        });
        m.cont = false;
        t1.start();
        m.waitAtBarrier();
        m.cancel();
        m.cont = true;
        t1.join();
        assertTrue(m.isCanceled());
        assertEquals(1, m.count);
        assertEquals(null, exs[0]);
    }
    
    public void testMoveFromSymlinkFolder () throws Exception {
        VCSFileProxy target = VCSFileProxy.createFileProxy(workDir, "target");
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxy file = VCSFileProxy.createFileProxy(folder, "file");
        VCSFileProxy symlinkFolder = VCSFileProxy.createFileProxy(folder, folder.getName());
        VCSFileProxy symlinkFile = VCSFileProxy.createFileProxy(symlinkFolder, "file");
        VCSFileProxySupport.mkdirs(target);
        VCSFileProxySupport.mkdirs(folder);
        write(file, "file");
        
        String relPath = "../folder";
        Files.createSymbolicLink(Paths.get(symlinkFolder.getPath()), Paths.get(relPath));
        assertTrue(Files.isSymbolicLink(Paths.get(symlinkFolder.getPath())));
        
        add();
        commit();
        assertTrue(symlinkFile.exists());
        
        getClient(workDir).rename(symlinkFile, VCSFileProxy.createFileProxy(target, symlinkFile.getName()), false, NULL_PROGRESS_MONITOR);
        Map<VCSFileProxy, GitStatus> status = getClient(workDir).getStatus(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(status, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(status, workDir, VCSFileProxy.createFileProxy(target, file.getName()), true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
    }
}
