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
package org.netbeans.modules.subversion.ui.copy;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.subversion.RepositoryFile;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author ondra
 */
public class CopyDialogTest extends NbTestCase {
    
    private static final List<String> recentUrlsWithBranches = Arrays.asList(
        "Project/src/folder/file",
        "Project/src/folder",
        "Project2/src/folder",
        "Project",
        "trunk/Project/src/folder/file",
        "trunk/Project/src/folder",
        "trunk/Project",
        "trunk/subfolder/folder",
        "branches/branch1/Project/src/folder/file",
        "branches/branch1/Project/src/folder",
        "branches/branch1/Project",
        "tags/tag1/Project/src/folder/file",
        "tags/tag1/Project/src/folder",
        "tags/tag1/Project"
    );
    
    private static final List<String> recentUrlsWithoutBranches = Arrays.asList(
        "Project/src/folder/file",
        "Project/src/folder",
        "Project2/src/folder",
        "Project",
        "trunk/Project/src/folder/file",
        "trunk/Project/src/folder",
        "trunk/Project"
    );
    private static final String MORE_BRANCHES = NbBundle.getMessage(CopyDialog.class, "LBL_CopyDialog.moreBranchesAndTags"); //NOI18N

    public CopyDialogTest (String name) {
        super(name);
    }
    
    public void testProcessRecentFolders_NoBranchStructure () throws Exception {
        JComboBox combo = new JComboBox();
        JTextComponent comp = (JTextComponent) combo.getEditor().getEditorComponent();
        Map<String, String> items;
        // combo items should be sorted by name
        
        // no relevant recent URL
        items = CopyDialog.setupModel(combo, new RepositoryFile(new SVNUrl("file:///home"), "pathToSomeProject", SVNRevision.HEAD), recentUrlsWithBranches, false);
        assertEquals(Arrays.asList(new String[] { }), new LinkedList<String>(items.keySet()));
        
        // relevant file, but no branches
        // no highlighting
        items = CopyDialog.setupModel(combo, new RepositoryFile(new SVNUrl("file:///home"), "pathToSomeFolder/subfolder/folder", SVNRevision.HEAD), recentUrlsWithBranches, false);
        assertModel(items, combo, Arrays.asList(new String[] {
            "branches/branch1/Project/src/folder", null,
            "Project/src/folder", null,
            "Project2/src/folder", null,
            "tags/tag1/Project/src/folder", null,
            "trunk/Project/src/folder", null,
            "trunk/subfolder/folder", null
        }));
        // least recently used is preselected
        assertEquals("Project/src/folder", comp.getText());
        // no branch - no selection
        assertEquals(null, comp.getSelectedText());
    }

    public void testProcessRecentFolders_NoBranchInHistory_OnTrunk () throws Exception {
        JComboBox combo = new JComboBox();
        JTextComponent comp = (JTextComponent) combo.getEditor().getEditorComponent();
        Map<String, String> items;
        // combo items should be sorted by name
        
        // file on trunk, no branch in history
        items = CopyDialog.setupModel(combo, new RepositoryFile(new SVNUrl("file:///home"), "trunk/subfolder/folder", SVNRevision.HEAD), recentUrlsWithoutBranches, false);
        assertModel(items, combo, Arrays.asList(new String[] {
            "trunk/subfolder/folder", null,
            "----------", null,
            MORE_BRANCHES, null,
            "----------", null,
            "Project/src/folder", null,
            "Project2/src/folder", null,
            "trunk/Project/src/folder", null
        }));
        // least recently used is preselected
        assertEquals("branches/[BRANCH_NAME]/subfolder/folder", comp.getText());
        // no branch - no selection
        assertEquals("[BRANCH_NAME]", comp.getSelectedText());
    }
    
    public void testProcessRecentFolders_NoBranchInHistory_OnBranch () throws Exception {
        JComboBox combo = new JComboBox();
        JTextComponent comp = (JTextComponent) combo.getEditor().getEditorComponent();
        Map<String, String> items;
        // combo items should be sorted by name
        
        // file on branch, no branch in history
        items = CopyDialog.setupModel(combo, new RepositoryFile(new SVNUrl("file:///home"), "branches/SOMEBRANCH/subfolder/folder", SVNRevision.HEAD), recentUrlsWithoutBranches, false);
        assertModel(items, combo, Arrays.asList(new String[] {
            "trunk/subfolder/folder", null,
            "----------", null,
            MORE_BRANCHES, null,
            "----------", null,
            "Project/src/folder", null,
            "Project2/src/folder", null,
            "trunk/Project/src/folder", null
        }));
        // template is offered
        assertEquals("branches/[BRANCH_NAME]/subfolder/folder", comp.getText());
        // no branch - no selection
        assertEquals("[BRANCH_NAME]", comp.getSelectedText());
        
    }

    public void testProcessRecentFolders_BranchInHistory () throws Exception {
        JComboBox combo = new JComboBox();
        JTextComponent comp = (JTextComponent) combo.getEditor().getEditorComponent();
        Map<String, String> items;
        // combo items should be sorted by name
        
        // file on branch, branches and tags in history
        items = CopyDialog.setupModel(combo, new RepositoryFile(new SVNUrl("file:///home"), "branches/SOMEBRANCH/subfolder/folder", SVNRevision.HEAD), recentUrlsWithBranches, false);
        assertModel(items, combo, Arrays.asList(new String[] {
            "trunk/subfolder/folder", null,
            "----------", null,
            "branches/branch1/subfolder/folder", "<html>branches/<strong>branch1</strong>/subfolder/folder</html>",
            "tags/tag1/subfolder/folder", "<html>tags/<strong>tag1</strong>/subfolder/folder</html>",
            MORE_BRANCHES, null,
            "----------", null,
            "branches/branch1/Project/src/folder", null, 
            "Project/src/folder", null,
            "Project2/src/folder", null,
            "tags/tag1/Project/src/folder", null,
            "trunk/Project/src/folder", null
        }));
        // least recently used branch is preselected
        assertEquals("branches/branch1/subfolder/folder", comp.getText());
        // no branch - no selection
        assertEquals("branch1", comp.getSelectedText());
    }

    private void assertModel (Map<String, String> items, JComboBox combo, List<String> expected) {
        ComboBoxModel model = combo.getModel();
        assertEquals(expected.size() / 2, model.getSize());
        for (int i = 0; i < model.getSize(); ++i) {
            assertEquals(expected.get(i * 2), model.getElementAt(i));
            assertEquals(expected.get(i * 2 + 1), items.get((String) model.getElementAt(i)));
        }
    }
}
