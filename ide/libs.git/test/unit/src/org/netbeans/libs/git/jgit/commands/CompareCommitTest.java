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
import java.util.Map;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitRevisionInfo.GitFileInfo.Status;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;

/**
 *
 * @author ondra
 */
public class CompareCommitTest extends AbstractGitTestCase {
    private Repository repository;
    private File workDir;

    public CompareCommitTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }
    
    public void testCompareSimple () throws Exception {
        File file = new File(workDir, "file");
        File[] files = new File[] { file };
        write(file, "init\n");
        add(files);
        commit(files);
        
        GitClient client = getClient(workDir);
        String revision1 = client.getBranches(false, NULL_PROGRESS_MONITOR).get(Constants.MASTER).getId();
        write(file, "modification\n");
        add(files);
        GitRevisionInfo revision2 = client.commit(files, "my commit message", null, null, NULL_PROGRESS_MONITOR);
        Map<File, GitRevisionInfo.GitFileInfo> statuses = client.getStatus(files, Constants.HEAD, revision1, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertEquals(Status.MODIFIED, statuses.get(file).getStatus());
        
        write(file, "modification 2\n");
        add(files);
        GitRevisionInfo revision3 = client.commit(files, "my commit 3 message", null, null, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, revision1, Constants.HEAD, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertEquals(Status.MODIFIED, statuses.get(file).getStatus());
        
        statuses = client.getStatus(files, revision1, revision3.getRevision(), NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertEquals(Status.MODIFIED, statuses.get(file).getStatus());
        
        statuses = client.getStatus(files, revision2.getRevision(), revision3.getRevision(), NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertEquals(Status.MODIFIED, statuses.get(file).getStatus());
    }
    
    public void testCompareRevertModification () throws Exception {
        File file = new File(workDir, "file");
        File[] files = new File[] { file };
        write(file, "init\n");
        add(files);
        commit(files);
        
        GitClient client = getClient(workDir);
        String revision1 = client.getBranches(false, NULL_PROGRESS_MONITOR).get(Constants.MASTER).getId();
        write(file, "modification\n");
        add(files);
        GitRevisionInfo revision2 = client.commit(files, "my commit message", null, null, NULL_PROGRESS_MONITOR);
        
        write(file, "init\n");
        add(files);
        GitRevisionInfo revision3 = client.commit(files, "my commit 3 message", null, null, NULL_PROGRESS_MONITOR);
        Map<File, GitRevisionInfo.GitFileInfo> statuses = client.getStatus(files, revision1, Constants.HEAD, NULL_PROGRESS_MONITOR);
        assertTrue(statuses.isEmpty());
    }
    
    public void testCompareSelection () throws Exception {
        File file = new File(workDir, "file");
        File file2 = new File(workDir, "file2");
        File[] files = new File[] { file, file2 };
        write(file, "init\n");
        add(files);
        commit(files);
        
        GitClient client = getClient(workDir);
        String revision1 = client.getBranches(false, NULL_PROGRESS_MONITOR).get(Constants.MASTER).getId();
        write(file, "modification\n");
        add(files);
        GitRevisionInfo revision2 = client.commit(files, "my commit message", null, null, NULL_PROGRESS_MONITOR);
        
        write(file2, "adding file 2\n");
        add(files);
        GitRevisionInfo revision3 = client.commit(files, "adding file 2", null, null, NULL_PROGRESS_MONITOR);
        Map<File, GitRevisionInfo.GitFileInfo> statuses = client.getStatus(new File[] { file }, revision1, revision3.getRevision(), NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertEquals(Status.MODIFIED, statuses.get(file).getStatus());
        
        statuses = client.getStatus(new File[] { file2 }, revision1, revision3.getRevision(), NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertEquals(Status.ADDED, statuses.get(file2).getStatus());
        
        statuses = client.getStatus(files, revision1, revision3.getRevision(), NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertEquals(Status.MODIFIED, statuses.get(file).getStatus());
        assertEquals(Status.ADDED, statuses.get(file2).getStatus());
    }
    
    public void testCompareRename () throws Exception {
        File file = new File(workDir, "file");
        File file2 = new File(workDir, "file2");
        File[] files = new File[] { file, file2 };
        write(file, "files content\n");
        add(files);
        commit(files);
        
        GitClient client = getClient(workDir);
        String revision1 = client.getBranches(false, NULL_PROGRESS_MONITOR).get(Constants.MASTER).getId();
        remove(false, file);
        
        client.commit(files, "removing file", null, null, NULL_PROGRESS_MONITOR);
        
        write(file2, "files content\n");
        add(files);
        GitRevisionInfo revision3 = client.commit(files, "adding file as file2", null, null, NULL_PROGRESS_MONITOR);
        Map<File, GitRevisionInfo.GitFileInfo> statuses = client.getStatus(files, revision1, revision3.getRevision(), NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertEquals(Status.REMOVED, statuses.get(file).getStatus());
        assertEquals(Status.RENAMED, statuses.get(file2).getStatus());
        assertEquals(file, statuses.get(file2).getOriginalFile());
    }
}
