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
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.GitStatus;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class StashTest extends AbstractGitTestCase {
    private VCSFileProxy workDir;
    private JGitRepository repository;

    public StashTest (String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList().contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }

    public void testStashCreate () throws Exception {
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(folder, "file");
        
        VCSFileProxySupport.mkdirs(folder);
        write(file1, "file1");
        write(file2, "file2");
        
        add();
        commit();
        
        write(file1, "modification 1");
        add();
        write(file2, "modification 2");
        
        GitClient client = getClient(workDir);
        
        String msg = "Stash save";
        GitRevisionInfo stashedCommit = client.stashSave(msg, false, NULL_PROGRESS_MONITOR);
        
        assertEquals("file1", read(file1));
        assertEquals("file2", read(file2));
        assertEquals("On master: "+msg+"\n", stashedCommit.getFullMessage());
        
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[0], NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
    }

    public void testStashApply () throws Exception {
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(folder, "file");
        
        VCSFileProxySupport.mkdirs(folder);
        write(file1, "file1");
        write(file2, "file2");
        
        add();
        commit();
        
        write(file1, "modification 1");
        add();
        write(file2, "modification 2");
        
        GitClient client = getClient(workDir);
        
        client.stashSave("stash", false, NULL_PROGRESS_MONITOR);
        
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[0], NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        
        client.stashApply(0, false, NULL_PROGRESS_MONITOR);
        assertEquals("modification 1", read(file1));
        assertEquals("modification 2", read(file2));
        statuses = client.getStatus(new VCSFileProxy[0], NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        
        client.reset("master", GitClient.ResetType.HARD, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new VCSFileProxy[0], NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        
        client.stashApply(0, true, NULL_PROGRESS_MONITOR);
        assertEquals("modification 1", read(file1));
        assertEquals("modification 2", read(file2));
        statuses = client.getStatus(new VCSFileProxy[0], NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, false);
        
        assertEquals(0, client.stashList(NULL_PROGRESS_MONITOR).length);
    }

    public void testStashCreateUntracked () throws Exception {
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(folder, "untracked");
        
        VCSFileProxySupport.mkdirs(folder);
        write(file1, "file1");
        
        add();
        commit();
        
        write(file1, "modification 1");
        add();
        write(file2, "modification 2");
        
        GitClient client = getClient(workDir);
        
        String msg = "Stash save";
        GitRevisionInfo stashedCommit = client.stashSave(msg, false, NULL_PROGRESS_MONITOR);
        
        assertEquals("On master: "+msg+"\n", stashedCommit.getFullMessage());
        
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[0], NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
if(false)assertStatus(statuses, workDir, folder, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
else    ;
        
        stashedCommit = client.stashSave(msg, true, NULL_PROGRESS_MONITOR);
        assertEquals("On master: "+msg+"\n", stashedCommit.getFullMessage());
        
        statuses = client.getStatus(new VCSFileProxy[0], NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertFalse(statuses.containsKey(file2));
        
        client.stashApply(0, false, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(new VCSFileProxy[0], NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file1, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
if(false)assertStatus(statuses, workDir, folder, false, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_ADDED, GitStatus.Status.STATUS_ADDED, false);
else    ;
    }
    
    public void testStashDrop () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        
        write(file, "file");
        
        add();
        commit();
        
        write(file, "modification");
        add();
        
        GitClient client = getClient(workDir);
        
        client.stashSave("stash", false, NULL_PROGRESS_MONITOR);
        
        write(file, "modification 2");
        add();
        write(file, "modification 3");
        
        client.stashSave("stash", false, NULL_PROGRESS_MONITOR);
        
        GitRevisionInfo[] stashList = client.stashList(NULL_PROGRESS_MONITOR);
        assertEquals(2, stashList.length);
        
        client.stashDrop(1, NULL_PROGRESS_MONITOR);
        
        GitRevisionInfo[] stashList2 = client.stashList(NULL_PROGRESS_MONITOR);
        assertEquals(1, stashList2.length);
        assertEquals(stashList[0].getRevision(), stashList2[0].getRevision());
    }
}
