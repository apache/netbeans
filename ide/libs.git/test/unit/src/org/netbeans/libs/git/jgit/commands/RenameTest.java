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

package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;

/**
 *
 * @author ondra
 */
public class RenameTest extends AbstractGitTestCase {
    private File workDir;

    public RenameTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
    }

    public void testRenameUnderItself () throws Exception {
        File folder = new File(workDir, "folder");
        folder.mkdirs();
        File target = new File(folder, "subFolder");
        target.mkdirs();

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
        File folder = new File(workDir, "folder");
        folder.mkdirs();
        File target = new File(workDir, "target");

        Monitor m = new Monitor();
        assertTrue(folder.renameTo(target));
        GitClient client = getClient(workDir);
        try {
            client.rename(folder, target, false, m);
            fail();
        } catch (GitException ex) {
            assertEquals("Source does not exist: " + folder.getAbsolutePath(), ex.getMessage());
        }
    }

    public void testRenameFailTargetExists () throws Exception {
        File folder = new File(workDir, "folder");
        folder.mkdirs();
        File target = new File(workDir, "target");
        target.mkdirs();

        Monitor m = new Monitor();
        GitClient client = getClient(workDir);
        try {
            client.rename(folder, target, false, m);
            fail();
        } catch (GitException ex) {
            assertEquals("Target already exists: " + target.getAbsolutePath(), ex.getMessage());
        }
    }

    public void testRenameFailTargetOutsideWorkingTree () throws Exception {
        File folder = new File(workDir, "folder");
        folder.mkdirs();
        File target = new File(workDir.getParentFile(), "target");

        Monitor m = new Monitor();
        GitClient client = getClient(workDir);
        try {
            client.rename(folder, target, true, m);
            fail();
        } catch (IllegalArgumentException ex) {
            assertEquals(target.getAbsolutePath() + " is not under " + workDir.getAbsolutePath(), ex.getMessage());
        }
    }

    public void testRenameFailTargetDoesNotExist () throws Exception {
        File folder = new File(workDir, "folder");
        folder.mkdirs();
        File target = new File(workDir, "target");

        Monitor m = new Monitor();
        GitClient client = getClient(workDir);
        try {
            client.rename(folder, target, true, m);
            fail();
        } catch (GitException ex) {
            assertEquals("Target does not exist: " + target.getAbsolutePath(), ex.getMessage());
        }
    }

    public void testRenameFile () throws Exception {
        File file = new File(workDir, "file");
        write(file, "hello");
        File target = new File(workDir, "fileRenamed");

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
        Map<File, GitStatus> statuses = client.getStatus(new File[] { file, target }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, target, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertTrue(statuses.get(target).isRenamed());
        
        statuses = client.getStatus(new File[0], NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, target, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertTrue(statuses.get(target).isRenamed());
    }

    public void testRenameFileAfter () throws Exception {
        File file = new File(workDir, "file");
        write(file, "hello");
        File target = new File(workDir, "fileRenamed");

        add(file);
        commit(file);
        file.renameTo(target);
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        assertTrue(target.exists());
        assertFalse(file.exists());
        assertEquals("hello", read(target));
        client.addNotificationListener(m);
        client.rename(file, target, true, m);
        assertEquals(Collections.singleton(target), m.notifiedFiles);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { file, target }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, target, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertTrue(statuses.get(target).isRenamed());
    }

    public void testMoveFileToFolder () throws Exception {
        File file = new File(workDir, "file");
        write(file, "aaa");
        File target = new File(new File(workDir, "folder"), "file");

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
        Map<File, GitStatus> statuses = client.getStatus(new File[] { file, target }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, target, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertTrue(statuses.get(target).isRenamed());

        write(file, "aaa");
        add(file);
        statuses = client.getStatus(new File[] { file }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        File target2 = new File(target.getParentFile(), "moved");
        m = new Monitor();
        client.addNotificationListener(m);
        client.rename(file, target2, false, m);
        assertTrue(target2.exists());
        assertFalse(file.exists());
        assertEquals("aaa", read(target2));
        assertEquals(Collections.singleton(target2), m.notifiedFiles);
        statuses = client.getStatus(new File[] { file, target, target2 }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, target, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, target2, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertTrue(statuses.get(target).isRenamed() && statuses.get(target2).isCopied()
                || statuses.get(target2).isRenamed() && statuses.get(target).isCopied());
    }

    public void testMoveFileToFolderAfter () throws Exception {
        File file = new File(workDir, "file");
        write(file, "aaa");
        File target = new File(new File(workDir, "folder"), "file");
        target.getParentFile().mkdirs();

        add(file);
        commit(file);
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        assertTrue(file.renameTo(target));
        assertTrue(target.exists());
        assertFalse(file.exists());
        assertEquals("aaa", read(target));
        client.addNotificationListener(m);
        client.rename(file, target, true, m);
        assertEquals(Collections.singleton(target), m.notifiedFiles);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { file, target }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, target, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertTrue(statuses.get(target).isRenamed());

        write(file, "aaa");
        add(file);
        statuses = client.getStatus(new File[] { file }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        File target2 = new File(target.getParentFile(), "moved");
        m = new Monitor();
        assertTrue(file.renameTo(target2));
        assertTrue(target2.exists());
        assertFalse(file.exists());
        assertEquals("aaa", read(target2));
        client.addNotificationListener(m);
        client.rename(file, target2, true, m);
        assertEquals(Collections.singleton(target2), m.notifiedFiles);
        statuses = client.getStatus(new File[] { file, target, target2 }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, target, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, target2, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertTrue(statuses.get(target).isRenamed() && statuses.get(target2).isCopied()
                || statuses.get(target2).isRenamed() && statuses.get(target).isCopied());
    }

    public void testMoveFileToExisting () throws Exception {
        File file = new File(workDir, "file");
        write(file, "aaa");
        File target = new File(new File(workDir, "folder"), "file");

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
        Map<File, GitStatus> statuses = client.getStatus(new File[] { file, target }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, target, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertTrue(statuses.get(target).isRenamed());

        write(file, "aaa");
        write(target, "bbb");
        add(target, file);
        commit(target);

        File target2 = target;
        assertTrue(target2.exists());
        m = new Monitor();
        if (!file.renameTo(target2)) {
            assertTrue(target2.delete() && file.renameTo(target2));
        }
        client.addNotificationListener(m);
        client.rename(file, target2, true, m);
        assertTrue(m.notifiedWarnings.contains("Index already contains an entry for folder/file"));
        assertEquals(Collections.singleton(target2), m.notifiedFiles);
        statuses = client.getStatus(new File[] { file, target, target2 }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, target, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, target2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        // aaa -> bbb is a 0% match
        assertFalse(statuses.get(target).isRenamed());
        assertFalse(statuses.get(target).isCopied());
    }

    public void testMoveTree () throws Exception {
        File folder = new File(workDir, "folder");
        folder.mkdirs();
        File file1 = new File(folder, "file1");
        write(file1, "file1 content");
        File file2 = new File(folder, "file2");
        write(file2, "file2 content");
        File subFolder1 = new File(folder, "folder1");
        subFolder1.mkdirs();
        File file11 = new File(subFolder1, "file");
        write(file11, "file11 content");
        File subFolder2 = new File(folder, "folder2");
        subFolder2.mkdirs();
        File file21 = new File(subFolder2, "file");
        write(file21, "file21 content");

        File target = new File(workDir, "target");
        File moved1 = new File(target, file1.getName());
        File moved2 = new File(target, file2.getName());
        File moved11 = new File(new File(target, file11.getParentFile().getName()), file11.getName());
        File moved21 = new File(new File(target, file21.getParentFile().getName()), file21.getName());

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
        assertEquals(new HashSet<File>(Arrays.asList(moved1, moved11, moved21)), m.notifiedFiles);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { file1, file2, file11, file21, moved1, moved11, moved2, moved21 }, NULL_PROGRESS_MONITOR);
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
        File folder = new File(workDir, "folder");
        folder.mkdirs();
        File file1 = new File(folder, "file1");
        write(file1, "file1 content");
        File file2 = new File(folder, "file2");
        write(file2, "file2 content");
        File subFolder1 = new File(folder, "folder1");
        subFolder1.mkdirs();
        File file11 = new File(subFolder1, "file");
        write(file11, "file11 content");
        File subFolder2 = new File(folder, "folder2");
        subFolder2.mkdirs();
        File file21 = new File(subFolder2, "file");
        write(file21, "file21 content");

        File target = new File(workDir, "target");
        File moved1 = new File(target, file1.getName());
        File moved2 = new File(target, file2.getName());
        File moved11 = new File(new File(target, file11.getParentFile().getName()), file11.getName());
        File moved21 = new File(new File(target, file21.getParentFile().getName()), file21.getName());

        add(file1, file11, file21);
        commit(file1, file11);

        folder.renameTo(target);
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.rename(folder, target, true, m);
        assertTrue(moved1.exists());
        assertTrue(moved2.exists());
        assertTrue(moved11.exists());
        assertTrue(moved21.exists());
        assertEquals(new HashSet<File>(Arrays.asList(moved1, moved11, moved21)), m.notifiedFiles);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { file1, file2, file11, file21, moved1, moved11, moved2, moved21 }, NULL_PROGRESS_MONITOR);
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
        final File folder = new File(workDir, "folder");
        folder.mkdirs();
        File file = new File(folder, "file");
        file.createNewFile();
        File file2 = new File(folder, "file2");
        file2.createNewFile();
        final Monitor m = new Monitor();
        final GitClient client = getClient(workDir);
        client.add(new File[] { }, NULL_PROGRESS_MONITOR);
        final Exception[] exs = new Exception[1];
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.addNotificationListener(m);
                    client.rename(folder, new File(folder.getParentFile(), "folder2"), false, m);
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
        File target = new File(workDir, "target");
        File folder = new File(workDir, "folder");
        File file = new File(folder, "file");
        File symlinkFolder = new File(folder, folder.getName());
        File symlinkFile = new File(symlinkFolder, "file");
        target.mkdirs();
        folder.mkdirs();
        write(file, "file");
        
        String relPath = "../folder";
        Files.createSymbolicLink(Paths.get(symlinkFolder.getAbsolutePath()), Paths.get(relPath));
        assertTrue(Files.isSymbolicLink(Paths.get(symlinkFolder.getAbsolutePath())));
        
        add();
        commit();
        assertTrue(symlinkFile.exists());
        
        getClient(workDir).rename(symlinkFile, new File(target, symlinkFile.getName()), false, NULL_PROGRESS_MONITOR);
        Map<File, GitStatus> status = getClient(workDir).getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(status, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(status, workDir, new File(target, file.getName()), true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
    }
    
    public void testRenameToIgnored () throws Exception {
        final File folder = new File(workDir, "folder");
        folder.mkdirs();
        File file = new File(folder, "file");
        file.createNewFile();
        File file2 = new File(folder, "file2");
        file2.createNewFile();
        
        File ignored = new File(workDir, "ignored");
        GitClient client = getClient(workDir);
        client.ignore(new File[] { ignored }, NULL_PROGRESS_MONITOR);
        client.add(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        
        File target = new File(ignored, "folder");
        file = new File(target, "file");
        file2 = new File(target, "file2");
        client.rename(folder, target, false, NULL_PROGRESS_MONITOR);
        
        Map<File, GitStatus> statuses = client.getStatus(new File[] { file, file2 }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_IGNORED, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, file2, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_IGNORED, GitStatus.Status.STATUS_ADDED, false);
    }
    
    public void testRenameToIgnoredFile () throws Exception {
        File file = new File(workDir, "test.global.php");
        file.createNewFile();
        File ignore = new File(workDir, ".gitignore");
        write(ignore, "*.local.php");
        
        GitClient client = getClient(workDir);
        client.add(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        client.commit(new File[] { workDir }, "message", null, null, NULL_PROGRESS_MONITOR);
        
        File rename = new File(workDir, "test.local.php");
        client.rename(file, rename, false, NULL_PROGRESS_MONITOR);
        
        Map<File, GitStatus> statuses = client.getStatus(new File[] { rename }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, rename, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_IGNORED, GitStatus.Status.STATUS_ADDED, false);
    }
}
