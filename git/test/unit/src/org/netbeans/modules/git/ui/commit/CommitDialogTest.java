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
