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

import java.io.IOException;
import java.lang.reflect.Method;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.GitFileNode.GitLocalFileNode;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.GitTestKit;
import org.netbeans.modules.versioning.util.common.VCSCommitOptions;
import org.netbeans.modules.versioning.util.common.VCSFileNode;

/**
 *
 * @author Tomas Stupka
 */
public class CommitTableTest extends NbTestCase {

    public CommitTableTest (String arg0) {
        super(arg0);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();        
    }

    public void testNoFiles() {
       GitCommitTable t = new GitCommitTable();
        
       t.setNodes(new GitLocalFileNode[0]);
       assertFalse(t.containsCommitable());  
       assertNotNull(t.getErrorMessage());
    }
    
    public void testModifiedFile() throws IOException {
       GitCommitTable t = new GitCommitTable();
        
       t.setNodes(new GitLocalFileNode[] { GitTestKit.createFileNode(getWorkDir(), "file", FileInformation.Status.MODIFIED_HEAD_WORKING_TREE)});
       assertTrue(t.containsCommitable());         
       assertNull(t.getErrorMessage());
    }
    
    public void testRemovedFile() throws IOException {
       GitCommitTable t = new GitCommitTable();
        
       t.setNodes(new GitLocalFileNode[] { GitTestKit.createFileNode(getWorkDir(), "file", FileInformation.STATUS_REMOVED)});
       assertTrue(t.containsCommitable());         
       assertNull(t.getErrorMessage());
    }
    
    public void testExcludedFile() throws IOException {
       GitCommitTable t = new GitCommitTable();
        
       t.setNodes(new GitLocalFileNode[] { GitTestKit.createFileNode(getWorkDir(), "file", FileInformation.Status.MODIFIED_HEAD_WORKING_TREE, true)});
       assertFalse(t.containsCommitable());         
       assertNotNull(t.getErrorMessage());
    }
    
    public void testConflictFile() throws IOException {
       GitCommitTable t = new GitCommitTable();
        
       t.setNodes(new GitLocalFileNode[] { GitTestKit.createFileNode(getWorkDir(), "file", FileInformation.Status.IN_CONFLICT)});
       assertFalse(t.containsCommitable());         
       assertNotNull(t.getErrorMessage());
    }
    
    public void testExcludedConflictFile() throws IOException {
       GitCommitTable t = new GitCommitTable();
        
       t.setNodes(new GitLocalFileNode[] { GitTestKit.createFileNode(getWorkDir(), "file", FileInformation.Status.IN_CONFLICT, true)});
       assertFalse(t.containsCommitable());         
       assertNotNull(t.getErrorMessage());
    }
    
    public void testModifiedConflictFile() throws IOException {
       GitCommitTable t = new GitCommitTable();
        
       t.setNodes(new GitLocalFileNode[] { GitTestKit.createFileNode(getWorkDir(), "mfile", FileInformation.Status.MODIFIED_HEAD_WORKING_TREE), GitTestKit.createFileNode(getWorkDir(), "cfile", FileInformation.Status.IN_CONFLICT)});
       assertFalse(t.containsCommitable());         
       assertNotNull(t.getErrorMessage());
    }
    
    public void testModifiedExcludedConflictFile() throws IOException {
       GitCommitTable t = new GitCommitTable();
        
       t.setNodes(new GitLocalFileNode[] { GitTestKit.createFileNode(getWorkDir(), "mfile", FileInformation.Status.MODIFIED_HEAD_WORKING_TREE), GitTestKit.createFileNode(getWorkDir(), "cfile", FileInformation.Status.IN_CONFLICT, true)});
       assertTrue(t.containsCommitable());         
       assertNull(t.getErrorMessage());
    }
    
    public void testNewIncludeExcludeFile () throws Exception {
        GitCommitTable t = new GitCommitTable();

        GitLocalFileNode n = GitTestKit.createFileNode(getWorkDir(), "mfile", FileInformation.Status.NEW_INDEX_WORKING_TREE);
        t.setNodes(new GitLocalFileNode[] { n });
        assertTrue(t.containsCommitable());

        GitModuleConfig.getDefault().setExcludeNewFiles(true);
        n = GitTestKit.createFileNode(getWorkDir(), "mfile", FileInformation.Status.NEW_INDEX_WORKING_TREE);
        t.setNodes(new GitLocalFileNode[] { n });
        assertFalse(t.containsCommitable());

        // same as table does when including
        VCSCommitOptions options = n.getDefaultCommitOption(false);
        Method m = VCSFileNode.class.getDeclaredMethod("setCommitOptions", VCSCommitOptions.class);
        m.setAccessible(true);
        m.invoke(n, options);
        
        assertTrue(t.containsCommitable());
    }
    
}
