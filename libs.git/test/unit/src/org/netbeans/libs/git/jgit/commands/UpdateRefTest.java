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

package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.lib.ReflogReader;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitRefUpdateResult;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;

/**
 *
 * @author ondra
 */
public class UpdateRefTest extends AbstractGitTestCase {
    private Repository repository;
    private File workDir;

    public UpdateRefTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }

    public void testNotAttempted () throws Exception {
        File f = new File(workDir, "f");
        write(f, "init");
        add(f);
        commit(f);
        
        write(f, "modi");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        GitRevisionInfo info = client.log("HEAD", NULL_PROGRESS_MONITOR);
        
        client.reset("HEAD~1", GitClient.ResetType.HARD, NULL_PROGRESS_MONITOR);
        
        GitRefUpdateResult res = client.updateReference("HEAD", info.getRevision(), NULL_PROGRESS_MONITOR);
        assertEquals(GitRefUpdateResult.NOT_ATTEMPTED, res);
    }

    public void testMoveMergeCommit () throws Exception {
        File f = new File(workDir, "f");
        write(f, "init");
        add(f);
        commit(f);
        
        write(f, "modif");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        GitRevisionInfo info = client.log("HEAD", NULL_PROGRESS_MONITOR);
        
        client.reset("HEAD~1", GitClient.ResetType.HARD, NULL_PROGRESS_MONITOR);
        
        GitRefUpdateResult res = client.updateReference("master", info.getRevision(), NULL_PROGRESS_MONITOR);
        assertEquals(GitRefUpdateResult.FAST_FORWARD, res);
        ReflogReader reflogReader = repository.getReflogReader("master");
        assertEquals("merge " + info.getRevision() + ": Fast-forward", reflogReader.getLastEntry().getComment());
    }

    public void testMoveMergeRef () throws Exception {
        File f = new File(workDir, "f");
        write(f, "init");
        add(f);
        commit(f);
        
        write(f, "modif");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        client.createBranch("BRANCH", "HEAD", NULL_PROGRESS_MONITOR);
        
        client.reset("HEAD~1", GitClient.ResetType.HARD, NULL_PROGRESS_MONITOR);
        
        GitRefUpdateResult res = client.updateReference("master", "BRANCH", NULL_PROGRESS_MONITOR);
        assertEquals(GitRefUpdateResult.FAST_FORWARD, res);
        ReflogReader reflogReader = repository.getReflogReader("master");
        assertEquals("merge BRANCH: Fast-forward", reflogReader.getLastEntry().getComment());
    }

    // must fail if would end in a non FF update
    public void testMoveMergeRejected () throws Exception {
        File f = new File(workDir, "f");
        write(f, "init");
        add(f);
        commit(f);
        
        write(f, "modif");
        add(f);
        commit(f);
        
        GitClient client = getClient(workDir);
        client.createBranch("BRANCH", "HEAD", NULL_PROGRESS_MONITOR);
        
        client.reset("HEAD~1", GitClient.ResetType.HARD, NULL_PROGRESS_MONITOR);
        
        write(f, "modif2");
        add(f);
        commit(f);
        GitRefUpdateResult res = client.updateReference("master", "BRANCH", NULL_PROGRESS_MONITOR);
        assertEquals(GitRefUpdateResult.REJECTED, res);
    }
}
