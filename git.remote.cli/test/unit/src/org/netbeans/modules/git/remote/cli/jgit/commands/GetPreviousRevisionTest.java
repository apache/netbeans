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
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class GetPreviousRevisionTest extends AbstractGitTestCase {
    private JGitRepository repository;
    private VCSFileProxy workDir;

    public GetPreviousRevisionTest (String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList("testGetPreviousRevisionSkipUntouched").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }
    
    public void testGetPreviousRevision () throws Exception {
        VCSFileProxy f =VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "init");
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        write(f, "modification");
        add(files);
        GitRevisionInfo commit1 = client.commit(files, "modification", null, null, NULL_PROGRESS_MONITOR);
        
        write(f, "modification2");
        add(files);
        GitRevisionInfo commit2 = client.commit(files, "modification2", null, null, NULL_PROGRESS_MONITOR);
        GitRevisionInfo revision = client.getPreviousRevision(f, commit2.getRevision(), NULL_PROGRESS_MONITOR);
        assertRevisions(commit1, revision);
    }
    
    public void testGetPreviousRevisionSkipUntouched () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        VCSFileProxy f2 = VCSFileProxy.createFileProxy(workDir, "f2");
        write(f, "init");
        write(f2, "init");
        VCSFileProxy[] files = new VCSFileProxy[] { f, f2 };
        add(files);
        GitClient client = getClient(workDir);
        GitRevisionInfo init = client.commit(files, "init", null, null, NULL_PROGRESS_MONITOR);

        write(f, "modification 1");
        add(files);
        GitRevisionInfo commit1 = client.commit(files, "modification 1", null, null, NULL_PROGRESS_MONITOR);
        
        // commit 2 does not touch "f"
        write(f2, "modification 2");
        add(files);
        GitRevisionInfo commit2 = client.commit(files, "modification 2", null, null, NULL_PROGRESS_MONITOR);
        
        write(f, "modification 3");
        add(files);
        GitRevisionInfo commit3 = client.commit(files, "modification 3", null, null, NULL_PROGRESS_MONITOR);
        GitRevisionInfo revision = client.getPreviousRevision(f, commit3.getRevision(), NULL_PROGRESS_MONITOR);
        assertRevisions(commit1, revision);
        
        revision = client.getPreviousRevision(f2, commit2.getRevision(), NULL_PROGRESS_MONITOR);
        assertRevisions(init, revision);
        
        revision = client.getCommonAncestor(new String[] { commit2.getParents()[0] }, NULL_PROGRESS_MONITOR);
        assertRevisions(commit1, revision);
        revision = client.getPreviousRevision(f2, commit3.getRevision(), NULL_PROGRESS_MONITOR);
        assertRevisions(commit2, revision);
    }

    private void assertRevisions (GitRevisionInfo expected, GitRevisionInfo info) throws GitException {
        assertEquals(expected.getRevision(), info.getRevision());
        assertEquals(expected.getAuthor().toString(), info.getAuthor().toString());
        assertEquals(expected.getCommitTime(), info.getCommitTime());
        assertEquals(expected.getCommitter().toString(), info.getCommitter().toString());
        assertEquals(expected.getFullMessage(), info.getFullMessage());
        assertEquals(expected.getShortMessage(), info.getShortMessage());
        assertEquals(expected.getModifiedFiles().size(), info.getModifiedFiles().size());
    }

}
