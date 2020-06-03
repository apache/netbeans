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
