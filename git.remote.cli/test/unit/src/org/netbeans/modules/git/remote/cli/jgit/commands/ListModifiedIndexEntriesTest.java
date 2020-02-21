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
import java.util.HashSet;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class ListModifiedIndexEntriesTest extends AbstractGitTestCase {
    private JGitRepository repository;
    private VCSFileProxy workDir;
    
    public ListModifiedIndexEntriesTest(String testName) throws IOException {
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

    public void testSingleModification () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxySupport.createNew(f);
        add();
        commit();

        write(f, "modification");

        VCSFileProxy[] modifications = getClient(workDir).listModifiedIndexEntries(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals(0, modifications.length);
        modifications = getClient(workDir).listModifiedIndexEntries(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(0, modifications.length);

        add(f);
        modifications = getClient(workDir).listModifiedIndexEntries(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals(1, modifications.length);
        assertTrue(Arrays.equals(new VCSFileProxy[] { f }, modifications));
        modifications = getClient(workDir).listModifiedIndexEntries(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(1, modifications.length);
        assertTrue(Arrays.equals(new VCSFileProxy[] { f }, modifications));
    }

    public void testMultipleModification () throws Exception {
        VCSFileProxy f1 = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxySupport.createNew(f1);
        VCSFileProxy f2 = VCSFileProxy.createFileProxy(workDir, "file2");
        VCSFileProxySupport.createNew(f2);
        add();
        commit();

        write(f1, "modification");
        write(f2, "modification 2");

        VCSFileProxy[] modifications = getClient(workDir).listModifiedIndexEntries(new VCSFileProxy[] { f1, f2 }, NULL_PROGRESS_MONITOR);
        assertEquals(0, modifications.length);
        modifications = getClient(workDir).listModifiedIndexEntries(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(0, modifications.length);

        add(f1, f2);
        modifications = getClient(workDir).listModifiedIndexEntries(new VCSFileProxy[] { f1 }, NULL_PROGRESS_MONITOR);
        assertEquals(1, modifications.length);
        assertTrue(Arrays.equals(new VCSFileProxy[] { f1 }, modifications));

        modifications = getClient(workDir).listModifiedIndexEntries(new VCSFileProxy[] { f2 }, NULL_PROGRESS_MONITOR);
        assertEquals(1, modifications.length);
        assertTrue(Arrays.equals(new VCSFileProxy[] { f2 }, modifications));

        modifications = getClient(workDir).listModifiedIndexEntries(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(2, modifications.length);
        assertEquals(new HashSet<VCSFileProxy>(Arrays.asList(new VCSFileProxy[] { f1, f2 })), new HashSet<VCSFileProxy>(Arrays.asList(modifications)));
    }
}
