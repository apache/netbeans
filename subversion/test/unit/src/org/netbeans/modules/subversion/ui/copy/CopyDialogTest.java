/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
