/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.git.ui.merge;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitMergeResult;
import org.netbeans.libs.git.GitMergeResult.MergeStatus;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.modules.git.AbstractGitTestCase;
import org.netbeans.modules.git.utils.GitUtils;

/**
 *
 * @author ondra
 */
public class MergeTest extends AbstractGitTestCase {

    public MergeTest (String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Add notification listener in Merge action when git lib starts sending notifications.
     */
    public void testNotifiedFiles () throws Exception {
        File f = new File(repositoryLocation, "file");
        f.createNewFile();
        GitClient client = getClient(repositoryLocation);
        File[] roots = { f };
        client.add(roots, GitUtils.NULL_PROGRESS_MONITOR);
        client.commit(roots, "initial", null, null, GitUtils.NULL_PROGRESS_MONITOR);
        String branchName = "nova";
        assertEquals(branchName, client.createBranch(branchName, "master", GitUtils.NULL_PROGRESS_MONITOR).getName());
        client.checkoutRevision(branchName, true, GitUtils.NULL_PROGRESS_MONITOR);
        write(f, "blablabla");
        client.add(roots, GitUtils.NULL_PROGRESS_MONITOR);
        client.commit(roots, branchName, null, null, GitUtils.NULL_PROGRESS_MONITOR);
        client.checkoutRevision("master", true, GitUtils.NULL_PROGRESS_MONITOR);
        assertEquals("", read(f));
        
        final Set<File> notifiedFiles = new HashSet<File>();
        client.addNotificationListener(new FileListener() {
            @Override
            public void notifyFile (File file, String relativePathToRoot) {
                notifiedFiles.add(file);
            }
        });
        GitMergeResult result = client.merge(branchName, GitUtils.NULL_PROGRESS_MONITOR);
        assertEquals(MergeStatus.FAST_FORWARD, result.getMergeStatus());
        assertEquals("blablabla", read(f));
        
        assertEquals(0, notifiedFiles.size());
    }
}
