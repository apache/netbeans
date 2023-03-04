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
