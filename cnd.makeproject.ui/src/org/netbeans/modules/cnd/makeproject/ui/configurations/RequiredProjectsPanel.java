/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.makeproject.ui.configurations;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.makeproject.api.ProjectSupport;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class RequiredProjectsPanel extends javax.swing.JPanel implements HelpCtx.Provider, PropertyChangeListener {

    private final Project project;
    private final MakeConfiguration conf;
    private final MyListEditorPanel myListEditorPanel;
    private final FSPath baseDir;
    private final PropertyEditorSupport editor;
    private final JButton addProjectButton;
    private JButton addStandardLibraryButton;
    private JButton addLibraryButton;
    private JButton addLibraryFileButton;
    private JButton addLibraryOption;

    public RequiredProjectsPanel(Project project, MakeConfiguration conf, FSPath baseDir, List<LibraryItem> data, PropertyEditorSupport editor, PropertyEnv env) {
        this.project = project;
        this.conf = conf;
        this.baseDir = baseDir;
        this.editor = editor;
        initComponents();
        //
        addProjectButton = new JButton(getString("ADD_PROJECTS_BUTTON_TXT")); // NOI18N
        addProjectButton.setToolTipText(getString("ADD_PROJECTS_BUTTON_TT")); // NOI18N
        addProjectButton.setMnemonic(getString("ADD_PROJECTS_BUTTON_MN").charAt(0)); // NOI18N

        JButton[] extraButtons = new JButton[]{addProjectButton};
        myListEditorPanel = new MyListEditorPanel(conf, data, extraButtons);
        addProjectButton.addActionListener(new AddProjectButtonAction());
        //
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        outerPanel.add(myListEditorPanel, gridBagConstraints);
        instructionsTextArea.setBackground(instructionPanel.getBackground());
        setPreferredSize(new java.awt.Dimension(700, 350));

        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addPropertyChangeListener(this);
    }

    public void setInstructionsText(String txt) {
        instructionsTextArea.setText(txt);
    }

    private List<LibraryItem> getListData() {
        return myListEditorPanel.getListData();
    }

    private Object getPropertyValue() throws IllegalStateException {
        return new ArrayList<>(getListData());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID) {
            editor.setValue(getPropertyValue());
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("RequiredProjects"); // NOI18N
    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        outerPanel = new javax.swing.JPanel();
        instructionPanel = new javax.swing.JPanel();
        instructionsTextArea = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(323, 223));
        outerPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(outerPanel, gridBagConstraints);

        instructionPanel.setLayout(new java.awt.GridBagLayout());

        instructionsTextArea.setEditable(false);
        instructionsTextArea.setLineWrap(true);
        instructionsTextArea.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        instructionPanel.add(instructionsTextArea, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 12, 0, 12);
        add(instructionPanel, gridBagConstraints);
    }

    private class MyListEditorPanel extends TableEditorPanel {
        private MakeConfiguration conf;
        public MyListEditorPanel(MakeConfiguration conf, List<LibraryItem> objects, JButton[] extraButtons) {
            super(conf, objects, extraButtons, baseDir);
            getAddButton().setVisible(false);
            getCopyButton().setVisible(false);
            getEditButton().setVisible(false);
            getDefaultButton().setVisible(false);
        }

        @Override
        public String getListLabelText() {
            return getString("PROJECTS_TXT");
        }

        @Override
        public char getListLabelMnemonic() {
            return getString("PROJECTS_MN").charAt(0);
        }
    }

    private class AddProjectButtonAction implements java.awt.event.ActionListener {

        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            MakeArtifact[] artifacts = MakeArtifactChooser.showDialog(MakeArtifactChooser.ArtifactType.PROJECT, project, baseDir, myListEditorPanel);
            if (artifacts != null) {
                for (int i = 0; i < artifacts.length; i++) {
                    String location = ProjectSupport.toProperPath(baseDir.getFileObject(), artifacts[i].getProjectLocation(), project);
                    String workingdir = ProjectSupport.toProperPath(baseDir.getFileObject(), artifacts[i].getWorkingDirectory(), project);
                    location = CndPathUtilities.normalizeSlashes(location);
                    workingdir = CndPathUtilities.normalizeSlashes(workingdir);
                    artifacts[i].setProjectLocation(location);
                    artifacts[i].setWorkingDirectory(workingdir);
                    artifacts[i].setBuild(false);
                    myListEditorPanel.addObjectAction(new LibraryItem.ProjectItem(artifacts[i]));
                }
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel instructionPanel;
    private javax.swing.JTextArea instructionsTextArea;
    private javax.swing.JPanel outerPanel;
    // End of variables declaration//GEN-END:variables

    private static String getString(String s) {
        return NbBundle.getBundle(RequiredProjectsPanel.class).getString(s);
    }
}
