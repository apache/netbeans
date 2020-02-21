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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitStatus;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class RemoveTest extends AbstractGitTestCase {
    private static final boolean KIT = RemoveCommand.KIT;
    private JGitRepository repository;
    private VCSFileProxy workDir;
    
    public RemoveTest (String name) throws IOException {
        super(name);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList("testRemoveNested","testCancel","testRemoveUntrackedTree","testRemoveTreeCached","testRemoveFileCached","testRemoveUntrackedFile").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }

    public void testRemoveNoRoots () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "toRemove");
        VCSFileProxySupport.createNew(file);
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(workDir, "unversioned");
        VCSFileProxySupport.createNew(file2);

        GitClient client = getClient(workDir);
        add(file);
        commit(file);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { file }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.remove(new VCSFileProxy[0], false, m);
        assertEquals(1, m.notifiedWarnings.size());
        assertTrue(file.exists());
        assertTrue(file2.exists());
    }

    public void testRemoveFileHard () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "toRemove");
        VCSFileProxySupport.createNew(file);

        GitClient client = getClient(workDir);
        add(file);
        commit(file);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { file }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.remove(new VCSFileProxy[] { file }, false, m);
        assertEquals(Collections.singleton(file), m.notifiedFiles);
        assertFalse(file.exists());
        statuses = client.getStatus(new VCSFileProxy[] { file }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);

        commit(file);
        statuses = client.getStatus(new VCSFileProxy[] { file }, NULL_PROGRESS_MONITOR);
        assertEquals(0, statuses.size());
    }

    // test for: removing a file only from git control, but leave it on disk
    public void testRemoveFileCached () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "toRemove");
        VCSFileProxySupport.createNew(file);
        add(file);
        commit(file);

        GitClient client = getClient(workDir);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { file }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.remove(new VCSFileProxy[] { file }, true, m);
        assertTrue(file.exists());
        assertEquals(Collections.singleton(file), m.notifiedFiles);
        statuses = client.getStatus(new VCSFileProxy[] { file }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, false);

        commit(file);

        statuses = client.getStatus(new VCSFileProxy[] { file }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
    }

    public void testRemoveTreeHard () throws Exception {
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxy file = VCSFileProxy.createFileProxy(folder, "file");
        VCSFileProxy folder1 = VCSFileProxy.createFileProxy(folder, "folder1");
        VCSFileProxy folder2 = VCSFileProxy.createFileProxy(folder1, "folder2");
        VCSFileProxySupport.mkdirs(folder2);
        VCSFileProxySupport.createNew(file);
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(folder1, "file1");
        VCSFileProxySupport.createNew(file1);
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(folder1, "file2");
        VCSFileProxySupport.createNew(file2);
        VCSFileProxy file3 = VCSFileProxy.createFileProxy(folder2, "file3");
        VCSFileProxySupport.createNew(file3);

        VCSFileProxy[] folders = new VCSFileProxy[] { folder1, folder2 };
        add(folders);
        add(file);
        commit(workDir);
        GitClient client = getClient(workDir);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(folders, NULL_PROGRESS_MONITOR);
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
        assertEquals(new HashSet<VCSFileProxy>(Arrays.asList(file1, file2, file3, folder1, folder2)), m.notifiedFiles);
        statuses = client.getStatus(folders, NULL_PROGRESS_MONITOR);
        assertEquals(3, statuses.size());
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, file3, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_REMOVED, false);
        statuses = client.getStatus(new VCSFileProxy[] { file }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);

        commit(workDir);
        statuses = client.getStatus(folders, NULL_PROGRESS_MONITOR);
        assertEquals(0, statuses.size());
    }

    // test for: removing a file only from git control, but leave it on disk
    public void testRemoveTreeCached () throws Exception {
        VCSFileProxy folder1 = VCSFileProxy.createFileProxy(workDir, "folder1");
        VCSFileProxy folder2 = VCSFileProxy.createFileProxy(folder1, "folder2");
        VCSFileProxySupport.mkdirs(folder2);
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(folder1, "file1");
        VCSFileProxySupport.createNew(file1);
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(folder1, "file2");
        VCSFileProxySupport.createNew(file2);
        VCSFileProxy file3 = VCSFileProxy.createFileProxy(folder2, "file3");
        VCSFileProxySupport.createNew(file3);
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxySupport.createNew(file);


        VCSFileProxy[] folders = new VCSFileProxy[] { folder1, folder2 };
        add(folders);
        add(file);
        commit(workDir);
        GitClient client = getClient(workDir);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(folders, NULL_PROGRESS_MONITOR);
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
        assertEquals(new HashSet<VCSFileProxy>(Arrays.asList(file1, file2, file3)), m.notifiedFiles);
        statuses = client.getStatus(folders, NULL_PROGRESS_MONITOR);
        assertEquals(3, statuses.size());
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file3, true, GitStatus.Status.STATUS_REMOVED, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, false);
        statuses = client.getStatus(new VCSFileProxy[] { file }, NULL_PROGRESS_MONITOR);
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
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "toRemove");
        VCSFileProxySupport.createNew(file);
        assertTrue(file.exists());
        GitClient client = getClient(workDir);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { file }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);

        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.remove(new VCSFileProxy[] { file }, false, m);
        assertFalse(file.exists());
        assertEquals(Collections.singleton(file), m.notifiedFiles);
        statuses = client.getStatus(new VCSFileProxy[] { file }, NULL_PROGRESS_MONITOR);
        assertEquals(0, statuses.size());
    }

    public void testRemoveUntrackedTree () throws Exception {
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxy folder1 = VCSFileProxy.createFileProxy(folder, "folder1");
        VCSFileProxy folder2 = VCSFileProxy.createFileProxy(folder1, "folder2");
        VCSFileProxySupport.mkdirs(folder2);
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(folder1, "file1");
        VCSFileProxySupport.createNew(file1);
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(folder1, "file2");
        VCSFileProxySupport.createNew(file2);
        VCSFileProxy file3 = VCSFileProxy.createFileProxy(folder2, "file3");
        VCSFileProxySupport.createNew(file3);

        VCSFileProxy[] folders = new VCSFileProxy[] { folder1, folder2 };
        GitClient client = getClient(workDir);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(folders, NULL_PROGRESS_MONITOR);
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
        assertEquals(new HashSet<VCSFileProxy>(Arrays.asList(file1, file2, file3, folder1, folder2)), m.notifiedFiles);
        statuses = client.getStatus(folders, NULL_PROGRESS_MONITOR);
        assertEquals(0, statuses.size());
    }

    public void testCancel () throws Exception {
        final VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxySupport.createNew(file);
        final VCSFileProxy file2 = VCSFileProxy.createFileProxy(workDir, "file2");
        VCSFileProxySupport.createNew(file2);

        final Monitor m = new Monitor();
        final GitClient client = getClient(workDir);
        final Exception[] exs = new Exception[1];
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.addNotificationListener(m);
                    client.remove(new VCSFileProxy[] { file, file2 }, false, m);
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
        
        ProgressMonitor pm = new ProgressMonitor.DefaultProgressMonitor() {

            @Override
            public void notifyError (String message) {
                fail("No ERROR may occur: " + message);
            }
            
        };
        client.remove(new VCSFileProxy[] { nested }, false, pm);
        assertTrue(nested.exists());
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, f, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, nested, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
        
        statuses = clientNested.getStatus(new VCSFileProxy[] { nested }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, nested, f2, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
    }
}
