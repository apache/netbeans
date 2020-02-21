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
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitObjectType;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.SearchCriteria;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class CatTest extends AbstractGitTestCase {
    private JGitRepository repository;
    private VCSFileProxy workDir;

    public CatTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected boolean isFailed() {
        return Arrays.asList("testCat").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }

    public void testCat () throws Exception {
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy f = VCSFileProxy.createFileProxy(folder, "testcat1");
        final String goldenString = "Manifest-Version: 1.0\n" +
                "X-COMMENT: Main-Class will be added automatically by build. Do not modify";
        write(f, goldenString);
        assertFile(f, goldenString);
        add(f);
        GitClient client = getClient(workDir);
        try {
            client.catFile(f, GitConstants.HEAD, VCSFileProxySupport.getOutputStream(f), NULL_PROGRESS_MONITOR);
            fail();
        } catch (GitException.MissingObjectException ex) {
            assertEquals(GitObjectType.COMMIT, ex.getObjectType());
            assertEquals(GitConstants.HEAD, ex.getObjectName());
        }
        commit(f);

        assertTrue(client.catFile(f, GitConstants.HEAD, VCSFileProxySupport.getOutputStream(f), NULL_PROGRESS_MONITOR));
        assertFile(f, goldenString);
        final SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setLimit(1);
        GitRevisionInfo[] log = client.log(searchCriteria, NULL_PROGRESS_MONITOR);
        String revision = log[0].getRevision();
        assertTrue(client.catFile(f, revision, VCSFileProxySupport.getOutputStream(f), NULL_PROGRESS_MONITOR));
        assertFile(f, goldenString);

        write(f, "blablabla");
        add(f);
        commit(f);
        assertTrue(client.catFile(f, revision, VCSFileProxySupport.getOutputStream(f), NULL_PROGRESS_MONITOR));
        assertFile(f, goldenString);
    }

    public void testCatIndex () throws Exception {
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy f = VCSFileProxy.createFileProxy(folder, "testcat1");
        final String goldenString = "Manifest-Version: 1.0\n" +
                "X-COMMENT: Main-Class will be added automatically by build. Do not modify";
        write(f, goldenString);
        assertFile(f, goldenString);
        GitClient client = getClient(workDir);
        VCSFileProxy temp = VCSFileProxySupport.createTempFile(workDir, "temp", null, true);
        //assertFalse(client.catIndexEntry(f, 0, VCSFileProxySupport.getOutputStream(temp), NULL_PROGRESS_MONITOR));
        
        add(f);

        assertTrue(client.catIndexEntry(f, 0, VCSFileProxySupport.getOutputStream(f), NULL_PROGRESS_MONITOR));
        assertFile(f, goldenString);
    }

    public void testCatRemoved () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "removed");
        final String goldenString = "Manifest-Version: 1.0\n" +
                "X-COMMENT: Main-Class will be added automatically by build";
        write(f, goldenString);
        assertFile(f, goldenString);
        add(f);
        commit(f);

        GitClient client = getClient(workDir);
        final SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setLimit(1);
        GitRevisionInfo[] log = client.log(searchCriteria, NULL_PROGRESS_MONITOR);
        String revision = log[0].getRevision();

        // remove and commit
        client.remove(new VCSFileProxy[] { f }, false, NULL_PROGRESS_MONITOR);
        commit(f);
        assertTrue(client.catFile(f, revision, VCSFileProxySupport.getOutputStream(f), NULL_PROGRESS_MONITOR));
        assertFile(f, goldenString);

        assertFalse(client.catFile(f, GitConstants.HEAD, VCSFileProxySupport.getOutputStream(f), NULL_PROGRESS_MONITOR));
    }
}
