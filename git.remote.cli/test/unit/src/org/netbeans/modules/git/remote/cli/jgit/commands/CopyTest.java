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
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class CopyTest extends AbstractGitTestCase {
    private JGitRepository repository;
    private VCSFileProxy workDir;

    public CopyTest (String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList("testCopyFileToExisting").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }

    public void testCopyUnderItself () throws Exception {
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy target = VCSFileProxy.createFileProxy(folder, "subFolder");
        VCSFileProxySupport.mkdirs(target);

        Monitor m = new Monitor();
        GitClient client = getClient(workDir);
        try {
            client.copyAfter(folder, target, m);
            fail();
        } catch (GitException ex) {
            assertEquals("Target folder [folder/subFolder] lies under the source [folder]", ex.getMessage());
        }
        try {
            client.copyAfter(target, folder, m);
            fail();
        } catch (GitException ex) {
            assertEquals("Source folder [folder/subFolder] lies under the target [folder]", ex.getMessage());
        }
    }

    public void testCopyFile () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxySupport.createNew(file);
        VCSFileProxy target = VCSFileProxy.createFileProxy(workDir, "fileCopy");

        add(file);
        copyFile(file, target);
        assertTrue(target.exists());
        assertFile(target, "");
        Monitor m = new Monitor();
        GitClient client = getClient(workDir);
        client.addNotificationListener(m);
        client.copyAfter(file, target, m);
        assertEquals(Collections.singleton(target), m.notifiedFiles);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, target, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        // sadly, rename detection works only when the source is deleted
        assertFalse(statuses.get(target).isCopied());
    }

    public void testCopyFileToFolder () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        write(file, "aaa");
        VCSFileProxy target = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(workDir, "folder"), "file");

        add(file);
        commit(file);
        copyFile(file, target);
        assertTrue(target.exists());
        assertFile(target, "aaa");
        Monitor m = new Monitor();
        GitClient client = getClient(workDir);
        client.addNotificationListener(m);
        client.copyAfter(file, target, m);
        assertEquals(Collections.singleton(target), m.notifiedFiles);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, target, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);

        VCSFileProxy target2 = VCSFileProxy.createFileProxy(target.getParentFile(), "copy");
        copyFile(file, target2);
        assertTrue(target2.exists());
        assertFile(target2, "aaa");
        m = new Monitor();
        client = getClient(workDir);
        client.addNotificationListener(m);
        client.copyAfter(file, target2, m);
        assertEquals(Collections.singleton(target2), m.notifiedFiles);
        statuses = client.getStatus(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, target, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, target2, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
    }

    public void testCopyFileToExisting () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        write(file, "aaa");
        VCSFileProxy target = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(workDir, "folder"), "file");

        add(file);
        commit(file);
        copyFile(file, target);
        assertTrue(target.exists());
       assertFile(target, "aaa");
        Monitor m = new Monitor();
        GitClient client = getClient(workDir);
        client.addNotificationListener(m);
        client.copyAfter(file, target, m);
        assertEquals(Collections.singleton(target), m.notifiedFiles);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, target, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);

        write(target, "bbb");
        add(target);
        commit(target);

        VCSFileProxy target2 = target;
        assertTrue(target2.exists());
        m = new Monitor();
        copyFile(file, target2);
        client = getClient(workDir);
        client.addNotificationListener(m);
        client.copyAfter(file, target2, m);
        assertTrue(m.notifiedWarnings.contains("Index already contains an entry for folder/file"));
        assertEquals(Collections.singleton(target2), m.notifiedFiles);
        statuses = client.getStatus(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, target, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, target2, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
    }

    public void testCopyTree () throws Exception {
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
        VCSFileProxy copy1 = VCSFileProxy.createFileProxy(target, file1.getName());
        VCSFileProxy copy2 = VCSFileProxy.createFileProxy(target, file2.getName());
        VCSFileProxy copy11 = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(target, file11.getParentFile().getName()), file11.getName());
        VCSFileProxy copy21 = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(target, file21.getParentFile().getName()), file21.getName());

        add(file1, file11, file21);
        commit(file1, file11);

        copyFile(folder, target);
        assertTrue(copy1.exists());
        assertTrue(copy2.exists());
        assertTrue(copy11.exists());
        assertTrue(copy21.exists());
        Monitor m = new Monitor();
        GitClient client = getClient(workDir);
        client.addNotificationListener(m);
        client.copyAfter(folder, target, m);
        assertEquals(new HashSet<VCSFileProxy>(Arrays.asList(copy1, copy11, copy21)), m.notifiedFiles);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, file11, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file21, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, copy1, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, copy2, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, copy11, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
        assertStatus(statuses, workDir, copy21, true, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, false);
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

        // simulate copy
        final VCSFileProxy target = VCSFileProxy.createFileProxy(folder.getParentFile(), "folder2");
        VCSFileProxySupport.mkdirs(target);
        VCSFileProxySupport.createNew(VCSFileProxy.createFileProxy(target, file.getName()));
        VCSFileProxySupport.createNew(VCSFileProxy.createFileProxy(target, file2.getName()));

        final Exception[] exs = new Exception[1];
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.addNotificationListener(m);
                    client.copyAfter(folder, target, m);
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
}
