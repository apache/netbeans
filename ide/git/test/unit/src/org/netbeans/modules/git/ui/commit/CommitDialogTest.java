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

package org.netbeans.modules.git.ui.commit;

import org.netbeans.modules.git.GitFileNode;
import java.awt.EventQueue;
import java.io.File;
import java.util.List;
import org.netbeans.junit.MockServices;
import org.netbeans.libs.git.GitUser;
import org.netbeans.modules.git.AbstractGitTestCase;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.GitTestKit;
import org.netbeans.modules.git.GitVCS;

/**
 *
 * @author Tomas Stupka
 */
public class CommitDialogTest extends AbstractGitTestCase {

    public CommitDialogTest (String arg0) {
        super(arg0);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(new Class[] {
            GitVCS.class});
    }

    public void testPrefilledPanel() throws Exception {
        final File repository = getRepositoryLocation();
        final File[] roots = new File[] {repository};
        File file = createFile(repository, "file");                
        add(file);
        
        final GitUser user = GitTestKit.createGitUser();
        String  message = "msg";        
        GitModuleConfig.getDefault().setLastCanceledCommitMessage(message);
                
        final GitCommitPanel[] panels = new GitCommitPanel[1];
        EventQueue.invokeAndWait(new Runnable() {
            @Override public void run() {
                panels[0] = GitCommitPanel.create(roots, repository, user, false);
            }
        });
        GitCommitPanel p = panels[0];
        p.computeNodesIntern().waitFinished();
        EventQueue.invokeAndWait(new Runnable() {@Override public void run() {}});
        
        assertEquals(user, p.getParameters().getAuthor());
        assertEquals(user, p.getParameters().getCommiter());
        assertEquals(message, p.getParameters().getCommitMessage());           
        
        List<GitFileNode.GitLocalFileNode> files = p.getCommitTable().getCommitFiles();
        assertEquals(1, files.size());
        assertEquals(file, files.get(0).getFile());
    }     
    
}
