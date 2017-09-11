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
