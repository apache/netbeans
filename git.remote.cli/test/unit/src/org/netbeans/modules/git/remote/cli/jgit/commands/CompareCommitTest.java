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

package org.netbeans.modules.git.remote.cli.jgit.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import static junit.framework.Assert.assertEquals;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo.GitFileInfo.Status;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class CompareCommitTest extends AbstractGitTestCase {
    private JGitRepository repository;
    private VCSFileProxy workDir;

    public CompareCommitTest (String testName) throws IOException {
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
    
    public void testCompareSimple () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxy[] files = new VCSFileProxy[] { file };
        write(file, "init\n");
        add(files);
        commit(files);
        
        GitClient client = getClient(workDir);
        String revision1 = client.getBranches(false, NULL_PROGRESS_MONITOR).get(GitConstants.MASTER).getId();
        write(file, "modification\n");
        add(files);
        GitRevisionInfo revision2 = client.commit(files, "my commit message", null, null, NULL_PROGRESS_MONITOR);
        Map<VCSFileProxy, GitRevisionInfo.GitFileInfo> statuses = client.getStatus(files, GitConstants.HEAD, revision1, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertEquals(Status.MODIFIED, statuses.get(file).getStatus());
        
        write(file, "modification 2\n");
        add(files);
        GitRevisionInfo revision3 = client.commit(files, "my commit 3 message", null, null, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, revision1, GitConstants.HEAD, NULL_PROGRESS_MONITOR);
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
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxy[] files = new VCSFileProxy[] { file };
        write(file, "init\n");
        add(files);
        commit(files);
        
        GitClient client = getClient(workDir);
        String revision1 = client.getBranches(false, NULL_PROGRESS_MONITOR).get(GitConstants.MASTER).getId();
        write(file, "modification\n");
        add(files);
        GitRevisionInfo revision2 = client.commit(files, "my commit message", null, null, NULL_PROGRESS_MONITOR);
        
        write(file, "init\n");
        add(files);
        GitRevisionInfo revision3 = client.commit(files, "my commit 3 message", null, null, NULL_PROGRESS_MONITOR);
        Map<VCSFileProxy, GitRevisionInfo.GitFileInfo> statuses = client.getStatus(files, revision1, GitConstants.HEAD, NULL_PROGRESS_MONITOR);
        assertTrue(statuses.isEmpty());
    }
    
    public void testCompareSelection () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(workDir, "file2");
        VCSFileProxy[] files = new VCSFileProxy[] { file, file2 };
        write(file, "init\n");
        add(files);
        commit(files);
        
        GitClient client = getClient(workDir);
        String revision1 = client.getBranches(false, NULL_PROGRESS_MONITOR).get(GitConstants.MASTER).getId();
        write(file, "modification\n");
        add(files);
        GitRevisionInfo revision2 = client.commit(files, "my commit message", null, null, NULL_PROGRESS_MONITOR);
        
        write(file2, "adding file 2\n");
        add(files);
        GitRevisionInfo revision3 = client.commit(files, "adding file 2", null, null, NULL_PROGRESS_MONITOR);
        Map<VCSFileProxy, GitRevisionInfo.GitFileInfo> statuses = client.getStatus(new VCSFileProxy[] { file }, revision1, revision3.getRevision(), NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertEquals(Status.MODIFIED, statuses.get(file).getStatus());
        
        statuses = client.getStatus(new VCSFileProxy[] { file2 }, revision1, revision3.getRevision(), NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertEquals(Status.ADDED, statuses.get(file2).getStatus());
        
        statuses = client.getStatus(files, revision1, revision3.getRevision(), NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertEquals(Status.MODIFIED, statuses.get(file).getStatus());
        assertEquals(Status.ADDED, statuses.get(file2).getStatus());
    }
    
    public void testCompareRename () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(workDir, "file2");
        VCSFileProxy[] files = new VCSFileProxy[] { file, file2 };
        write(file, "files content\n");
        add(files);
        commit(files);
        
        GitClient client = getClient(workDir);
        String revision1 = client.getBranches(false, NULL_PROGRESS_MONITOR).get(GitConstants.MASTER).getId();
        remove(false, file);
        
        client.commit(files, "removing file", null, null, NULL_PROGRESS_MONITOR);
        
        write(file2, "files content\n");
        add(files);
        GitRevisionInfo revision3 = client.commit(files, "adding file as file2", null, null, NULL_PROGRESS_MONITOR);
        Map<VCSFileProxy, GitRevisionInfo.GitFileInfo> statuses = client.getStatus(files, revision1, revision3.getRevision(), NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertEquals(Status.REMOVED, statuses.get(file).getStatus());
        assertEquals(Status.RENAMED, statuses.get(file2).getStatus());
        assertEquals(file, statuses.get(file2).getOriginalFile());
    }
}
