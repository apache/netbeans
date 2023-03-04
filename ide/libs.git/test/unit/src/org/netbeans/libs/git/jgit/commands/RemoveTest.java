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

package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class RemoveTest extends AbstractGitTestCase {

    private Repository repository;
    private File workDir;
    
    public RemoveTest (String name) throws IOException {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }

    public void testRemoveNoRoots () throws Exception {
        File file = new File(workDir, "toRemove");
        file.createNewFile();
        File file2 = new File(workDir, "unversioned");
        file2.createNewFile();

        GitClient client = getClient(workDir);
        add(file);
        commit(file);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { file }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.remove(new File[0], false, m);
        assertEquals(1, m.notifiedWarnings.size());
        assertTrue(file.exists());
        assertTrue(file2.exists());
    }

    public void testRemoveFileHard () throws Exception {
        File file = new File(workDir, "toRemove");
        file.createNewFile();

        GitClient client = getClient(workDir);
        add(file);
        commit(file);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { file }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.remove(new File[] { file }, false, m);
        assertEquals(Collections.singleton(file), m.notifiedFiles);
        assertFalse(file.exists());
        statuses = client.getStatus(new File[] { file }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);

        commit(file);
        statuses = client.getStatus(new File[] { file }, NULL_PROGRESS_MONITOR);
        assertEquals(0, statuses.size());
    }

    // test for: removing a file only from git control, but leave it on disk
    public void testRemoveFileCached () throws Exception {
        File file = new File(workDir, "toRemove");
        file.createNewFile();
        add(file);
        commit(file);

        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { file }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.remove(new File[] { file }, true, m);
        assertTrue(file.exists());
        assertEquals(Collections.singleton(file), m.notifiedFiles);
        statuses = client.getStatus(new File[] { file }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, false);

        commit(file);

        statuses = client.getStatus(new File[] { file }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
    }

    public void testRemoveTreeHard () throws Exception {
        File folder = new File(workDir, "folder");
        File file = new File(folder, "file");
        File folder1 = new File(folder, "folder1");
        File folder2 = new File(folder1, "folder2");
        folder2.mkdirs();
        file.createNewFile();
        File file1 = new File(folder1, "file1");
        file1.createNewFile();
        File file2 = new File(folder1, "file2");
        file2.createNewFile();
        File file3 = new File(folder2, "file3");
        file3.createNewFile();

        File[] folders = new File[] { folder1, folder2 };
        add(folders);
        add(file);
        commit(workDir);
        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(folders, NULL_PROGRESS_MONITOR);
        assertEquals(3, statuses.size());
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file3, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.remove(folders, false, m);
        assertFalse(file1.exists());
        assertFalse(file2.exists());
        assertFalse(file3.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());
        assertTrue(file.exists());
        assertTrue(folder.exists());
        assertEquals(new HashSet<File>(Arrays.asList(file1, file2, file3, folder1, folder2)), m.notifiedFiles);
        statuses = client.getStatus(folders, NULL_PROGRESS_MONITOR);
        assertEquals(3, statuses.size());
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, file3, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        statuses = client.getStatus(new File[] { file }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        commit(workDir);
        statuses = client.getStatus(folders, NULL_PROGRESS_MONITOR);
        assertEquals(0, statuses.size());
    }

    // test for: removing a file only from git control, but leave it on disk
    public void testRemoveTreeCached () throws Exception {
        File folder1 = new File(workDir, "folder1");
        File folder2 = new File(folder1, "folder2");
        folder2.mkdirs();
        File file1 = new File(folder1, "file1");
        file1.createNewFile();
        File file2 = new File(folder1, "file2");
        file2.createNewFile();
        File file3 = new File(folder2, "file3");
        file3.createNewFile();
        File file = new File(workDir, "file");
        file.createNewFile();


        File[] folders = new File[] { folder1, folder2 };
        add(folders);
        add(file);
        commit(workDir);
        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(folders, NULL_PROGRESS_MONITOR);
        assertEquals(3, statuses.size());
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file3, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.remove(folders, true, m);
        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertTrue(file3.exists());
        assertEquals(new HashSet<File>(Arrays.asList(file1, file2, file3)), m.notifiedFiles);
        statuses = client.getStatus(folders, NULL_PROGRESS_MONITOR);
        assertEquals(3, statuses.size());
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file3, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, false);
        statuses = client.getStatus(new File[] { file }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        commit(workDir);
        statuses = client.getStatus(folders,NULL_PROGRESS_MONITOR);
        assertEquals(3, statuses.size());
        assertStatus(statuses, workDir, file1, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, file2, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, file3, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
    }

    public void testRemoveUntrackedFile () throws Exception {
        File file = new File(workDir, "toRemove");
        file.createNewFile();
        assertTrue(file.exists());
        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { file }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);

        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.remove(new File[] { file }, false, m);
        assertFalse(file.exists());
        assertEquals(Collections.singleton(file), m.notifiedFiles);
        statuses = client.getStatus(new File[] { file }, NULL_PROGRESS_MONITOR);
        assertEquals(0, statuses.size());
    }

    public void testRemoveUntrackedTree () throws Exception {
        File folder = new File(workDir, "folder");
        File folder1 = new File(folder, "folder1");
        File folder2 = new File(folder1, "folder2");
        folder2.mkdirs();
        File file1 = new File(folder1, "file1");
        file1.createNewFile();
        File file2 = new File(folder1, "file2");
        file2.createNewFile();
        File file3 = new File(folder2, "file3");
        file3.createNewFile();

        File[] folders = new File[] { folder1, folder2 };
        GitClient client = getClient(workDir);
        Map<File, GitStatus> statuses = client.getStatus(folders, NULL_PROGRESS_MONITOR);
        assertEquals(3, statuses.size());
        assertStatus(statuses, workDir, file1, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, file2, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, file3, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);

        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.remove(folders, false, m);
        assertFalse(file1.exists());
        assertFalse(file2.exists());
        assertFalse(file3.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());
        assertTrue(folder.exists());
        assertEquals(new HashSet<File>(Arrays.asList(file1, file2, file3, folder1, folder2)), m.notifiedFiles);
        statuses = client.getStatus(folders, NULL_PROGRESS_MONITOR);
        assertEquals(0, statuses.size());
    }

    public void testCancel () throws Exception {
        final File file = new File(workDir, "file");
        file.createNewFile();
        final File file2 = new File(workDir, "file2");
        file2.createNewFile();

        final Monitor m = new Monitor();
        final GitClient client = getClient(workDir);
        final Exception[] exs = new Exception[1];
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.addNotificationListener(m);
                    client.remove(new File[] { file, file2 }, false, m);
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

    // must not return status for nested repositories
    public void testRemoveNested () throws Exception {
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
        
        ProgressMonitor pm = new ProgressMonitor.DefaultProgressMonitor() {

            @Override
            public void notifyError (String message) {
                fail("No ERROR may occur: " + message);
            }
            
        };
        client.remove(new File[] { nested }, false, pm);
        assertTrue(nested.exists());
        Map<File, GitStatus> statuses = client.getStatus(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, f, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, nested, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
        
        statuses = clientNested.getStatus(new File[] { nested }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, nested, f2, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
    }
}
