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

import javax.swing.JComboBox;
import org.netbeans.junit.NbTestCase;
import org.netbeans.libs.git.GitUser;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.GitTestKit;

/**
 *
 * @author Tomas Stupka
 */
public class CommitParametersTest extends NbTestCase {

    public CommitParametersTest (String arg0) {
        super(arg0);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();        
    }

    public void testUser() {
        GitCommitParameters parameters = new GitCommitParameters(GitModuleConfig.getDefault().getPreferences(), "", null);
        CommitPanel panel = parameters.getPanel();
        assertUser(parameters, panel.authorComboBox, "A U Thor <author@example.com>", "A U Thor", "author@example.com");
        assertUser(parameters, panel.authorComboBox, "Perun <author@example.com>", "Perun", "author@example.com");
        assertUser(parameters, panel.authorComboBox, "A U Thor <>", "A U Thor", "");
        assertUser(parameters, panel.authorComboBox, "Thor", "Thor", "");
        assertUser(parameters, panel.authorComboBox, "A U Thor", "A U Thor", "");
        assertUser(parameters, panel.authorComboBox, "<a", "<a", ""); // cli seems to live with this
    }
    
    public void testWrongUser() {
        GitCommitParameters parameters = new GitCommitParameters(GitModuleConfig.getDefault().getPreferences(), "", null);
        CommitPanel panel = parameters.getPanel();
                
        assertNull(getUser(parameters, panel.authorComboBox, "<>"));        
        assertNull(getUser(parameters, panel.authorComboBox, " <author@example.com>"));
        assertNull(getUser(parameters, panel.authorComboBox, "<author@example.com>"));
        assertNull(getUser(parameters, panel.authorComboBox, "<odin>"));
        assertNull(getUser(parameters, panel.authorComboBox, "<odin.org>"));
        assertNull(getUser(parameters, panel.authorComboBox, "org>"));
        assertNull(getUser(parameters, panel.authorComboBox, ">"));
    }
    
    public void testIsCommitable() {
        GitCommitParameters parameters = new GitCommitParameters(GitModuleConfig.getDefault().getPreferences(), "", null);
        CommitPanel panel = parameters.getPanel();
        
        panel.authorComboBox.getEditor().setItem("");
        panel.commiterComboBox.getEditor().setItem("");
        assertFalse(parameters.isCommitable());
        
        panel.authorComboBox.getEditor().setItem("A U Thor <author@example.com>");
        panel.commiterComboBox.getEditor().setItem("");
        assertFalse(parameters.isCommitable());
        
        panel.authorComboBox.getEditor().setItem("");
        panel.commiterComboBox.getEditor().setItem("A U Thor <author@example.com>");
        assertFalse(parameters.isCommitable());
        
        panel.authorComboBox.getEditor().setItem("A U Thor <author@example.com>");
        panel.commiterComboBox.getEditor().setItem("A U Thor <author@example.com>");
        assertTrue(parameters.isCommitable());
        
        panel.authorComboBox.getEditor().setItem("");
        panel.commiterComboBox.getEditor().setItem("");
        assertFalse(parameters.isCommitable());
        
    }

    public void testPrefilledParameters() throws Exception {
        GitUser user = GitTestKit.createGitUser();
        String  message = "msg";
        GitCommitParameters parameters = new GitCommitParameters(GitModuleConfig.getDefault().getPreferences(), message, user);
        CommitPanel panel = parameters.getPanel();
        
        assertEquals(message, parameters.getCommitMessage());
        assertEquals(user, parameters.getAuthor());
        assertEquals(user, parameters.getCommiter());
    }
    
    private void assertUser(GitCommitParameters parameters, JComboBox combo, String userString, String name, String mail) {
        GitUser user = getUser(parameters, combo, userString);
        assertNotNull(user);
        assertEquals(name, user.getName()); 
        assertEquals(mail, user.getEmailAddress());
    }
   
    private GitUser getUser(GitCommitParameters parameters, JComboBox combo, String userString) {
        combo.setSelectedItem(userString);
        GitUser user = parameters.getUser(combo);
        return user;
    }
}
