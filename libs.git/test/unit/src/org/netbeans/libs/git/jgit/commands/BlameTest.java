/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.netbeans.libs.git.ApiUtils;
import org.netbeans.libs.git.GitBlameResult;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitLineDetails;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitUser;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;

/**
 *
 * @author ondra
 */
public class BlameTest extends AbstractGitTestCase {
    private File workDir;
    private static final GitUser USER1 = ApiUtils.getClassFactory().createUser(new PersonIdent("user1", "user1@company.com")); //NOI18N
    private static final GitUser USER2 = ApiUtils.getClassFactory().createUser(new PersonIdent("user2", "user2@company.com")); //NOI18N
    private Repository repository;

    public BlameTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }
    
    public void testBlameSimple () throws Exception {
        File f = new File(workDir, "f");
        write(f, "aaa\nbbb\nccc\n");
        File[] files = { f };
        add(files);
        GitClient client = getClient(workDir);
        String revision1 = client.commit(files, "initial commit", USER1, USER1, NULL_PROGRESS_MONITOR).getRevision();
        GitBlameResult result = client.blame(f, revision1, NULL_PROGRESS_MONITOR);
        assertEquals(f, result.getBlamedFile());
        assertLineDetails(f, 0, revision1, USER1, USER1, result.getLineDetails(0));
        // the same authors should be the same one instance
        assertSame(result.getLineDetails(0).getAuthor(), result.getLineDetails(1).getAuthor());
        assertSame(result.getLineDetails(1).getAuthor(), result.getLineDetails(2).getAuthor());
        assertSame(result.getLineDetails(0).getAuthor(), result.getLineDetails(0).getCommitter());
        assertSame(result.getLineDetails(0).getCommitter(), result.getLineDetails(1).getCommitter());
        assertSame(result.getLineDetails(1).getCommitter(), result.getLineDetails(2).getCommitter());
        // the same commits should be the same one instance
        assertSame(result.getLineDetails(0).getRevisionInfo(), result.getLineDetails(1).getRevisionInfo());
        assertSame(result.getLineDetails(1).getRevisionInfo(), result.getLineDetails(2).getRevisionInfo());
        
        write(f, "aaa\nzzz\nccc\n");
        add(files);
        String revision2 = client.commit(files, "change 1", USER2, USER1, NULL_PROGRESS_MONITOR).getRevision();
        result = client.blame(f, revision2, NULL_PROGRESS_MONITOR);
        assertEquals(f, result.getBlamedFile());
        assertLineDetails(f, 0, revision1, USER1, USER1, result.getLineDetails(0));
        assertLineDetails(f, 1, revision2, USER2, USER1, result.getLineDetails(1));
        assertLineDetails(f, 2, revision1, USER1, USER1, result.getLineDetails(2));
        
        write(f, "aaa\nzzz\nyyy\n");
        add(files);
        String revision3 = client.commit(files, "change 2", USER1, USER2, NULL_PROGRESS_MONITOR).getRevision();
        result = client.blame(f, revision3, NULL_PROGRESS_MONITOR);
        assertEquals(f, result.getBlamedFile());
        assertLineDetails(f, 0, revision1, USER1, USER1, result.getLineDetails(0));
        assertLineDetails(f, 1, revision2, USER2, USER1, result.getLineDetails(1));
        assertLineDetails(f, 2, revision3, USER1, USER2, result.getLineDetails(2));
    }
    
    public void testBlameNullForModifiedLines () throws Exception {
        File f = new File(workDir, "f");
        write(f, "aaa\nbbb\n");
        File[] files = { f };
        add(files);
        GitClient client = getClient(workDir);
        String revision1 = client.commit(files, "initial commit", USER1, USER1, NULL_PROGRESS_MONITOR).getRevision();
        
        write(f, "aaa\nccc\n");
        GitBlameResult result = client.blame(f, null, NULL_PROGRESS_MONITOR);
        assertEquals(f, result.getBlamedFile());
        assertLineDetails(f, 0, revision1, USER1, USER1, result.getLineDetails(0));
        assertNull(result.getLineDetails(1));
        add(files);
        result = client.blame(f, null, NULL_PROGRESS_MONITOR);
        assertEquals(f, result.getBlamedFile());
        assertLineDetails(f, 0, revision1, USER1, USER1, result.getLineDetails(0));
        assertNull(result.getLineDetails(1));
        write(f, "aaa\nbbb\n");
        result = client.blame(f, null, NULL_PROGRESS_MONITOR);
        assertEquals(f, result.getBlamedFile());
        assertLineDetails(f, 0, revision1, USER1, USER1, result.getLineDetails(0));
        assertNull(result.getLineDetails(1));
    }
    
    public void testBlameRename () throws Exception {
        File f = new File(workDir, "f");
        File f2 = new File(workDir, "f2");
        write(f, "aaa\nbbb\nccc\n");
        File[] files = { f, f2 };
        add(files);
        GitClient client = getClient(workDir);
        String revision1 = client.commit(files, "initial commit", USER1, USER1, NULL_PROGRESS_MONITOR).getRevision();
        
        client.rename(f, f2, false, NULL_PROGRESS_MONITOR);
        write(f2, "aaa\nbbb\nddd\n");
        add(f2);
        String revision2 = client.commit(files, "rename", USER1, USER1, NULL_PROGRESS_MONITOR).getRevision();
        GitBlameResult result = client.blame(f, revision2, NULL_PROGRESS_MONITOR);
        assertNull(result);
        result = client.blame(f, revision1, NULL_PROGRESS_MONITOR);
        assertEquals(f, result.getBlamedFile());
        assertLineDetails(f, 0, revision1, USER1, USER1, result.getLineDetails(0));
        assertLineDetails(f, 1, revision1, USER1, USER1, result.getLineDetails(1));
        assertLineDetails(f, 2, revision1, USER1, USER1, result.getLineDetails(2));
        result = client.blame(f2, null, NULL_PROGRESS_MONITOR);
        assertEquals(f2, result.getBlamedFile());
        assertLineDetails(f, 0, revision1, USER1, USER1, result.getLineDetails(0));
        assertLineDetails(f, 1, revision1, USER1, USER1, result.getLineDetails(1));
        assertLineDetails(f2, 2, revision2, USER1, USER1, result.getLineDetails(2));
    }
    
    public void testBlameAddLine () throws Exception {
        File f = new File(workDir, "f");
        write(f, "aaa\nccc\n");
        File[] files = { f };
        add(files);
        GitClient client = getClient(workDir);
        String revision1 = client.commit(files, "initial commit", USER1, USER1, NULL_PROGRESS_MONITOR).getRevision();
        
        write(f, "aaa\nbbb\nccc\n");
        add(f);
        String revision2 = client.commit(files, "add line", USER1, USER1, NULL_PROGRESS_MONITOR).getRevision();
        GitBlameResult result = client.blame(f, revision2, NULL_PROGRESS_MONITOR);
        assertEquals(f, result.getBlamedFile());
        assertLineDetails(f, 0, revision1, USER1, USER1, result.getLineDetails(0));
        assertLineDetails(f, 1, revision2, USER1, USER1, result.getLineDetails(1));
        assertLineDetails(f, 1, revision1, USER1, USER1, result.getLineDetails(2));
    }
    
    public void testBlameRemoveLine () throws Exception {
        File f = new File(workDir, "f");
        write(f, "aaa\nbbb\nccc");
        File[] files = { f };
        add(files);
        GitClient client = getClient(workDir);
        String revision1 = client.commit(files, "initial commit", USER1, USER1, NULL_PROGRESS_MONITOR).getRevision();
        
        write(f, "aaa\nccc");
        add(f);
        String revision2 = client.commit(files, "remove line", USER1, USER1, NULL_PROGRESS_MONITOR).getRevision();
        GitBlameResult result = client.blame(f, revision2, NULL_PROGRESS_MONITOR);
        assertEquals(f, result.getBlamedFile());
        assertEquals(2, result.getLineCount());
        assertLineDetails(f, 0, revision1, USER1, USER1, result.getLineDetails(0));
        assertLineDetails(f, 2, revision1, USER1, USER1, result.getLineDetails(1));
    }
    
    public void testBlameMixedLineEndings () throws Exception {
        File f = new File(workDir, "f");
        String content = "";
        for (int i = 0; i < 10000; ++i) {
            content += i + "\r\n";
        }
        write(f, content);

        // lets turn autocrlf on
        StoredConfig cfg = repository.getConfig();
        cfg.setString(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_AUTOCRLF, "true");
        cfg.save();

        File[] files = new File[] { f };
        GitClient client = getClient(workDir);
        client.add(files, NULL_PROGRESS_MONITOR);
        GitRevisionInfo info = client.commit(files, "commit", null, null, NULL_PROGRESS_MONITOR);

        content = content.replaceFirst("0", "01");
        write(f, content);

        // it should be up to date again
        org.eclipse.jgit.api.BlameCommand cmd = new Git(repository).blame();
        cmd.setFilePath("f");
        BlameResult blameResult = cmd.call();
        assertEquals(info.getRevision(), blameResult.getSourceCommit(1).getName());
        
        GitBlameResult res = client.blame(f, null, NULL_PROGRESS_MONITOR);
        assertNull(res.getLineDetails(0));
        assertLineDetails(f, 1, info.getRevision(), info.getAuthor(), info.getCommitter(), res.getLineDetails(1));
        
        // without autocrlf it should all be modified
        cfg.setString(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_AUTOCRLF, "false");
        cfg.save();
        res = client.blame(f, null, NULL_PROGRESS_MONITOR);
        assertNull(res.getLineDetails(1));
    }

    private void assertLineDetails (File file, int line, String revision, GitUser author, GitUser committer, GitLineDetails lineDetails) {
        assertEquals(file, lineDetails.getSourceFile());
        assertEquals(line, lineDetails.getSourceLine());
        assertEquals(revision, lineDetails.getRevisionInfo().getRevision());
        assertEquals(author, lineDetails.getAuthor());
        assertEquals(committer, lineDetails.getCommitter());
    }
}
