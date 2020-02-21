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

package org.netbeans.modules.cnd.makeproject.ui.actions;

import java.awt.Dimension;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.wizards.SourceFilesPanel;
import org.netbeans.modules.cnd.utils.FileObjectFilter;
import org.netbeans.modules.cnd.utils.ui.ModalMessageDlg;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.windows.WindowManager;

public final class AddExistingFolderItemsAction extends NodeAction {
    
    public AddExistingFolderItemsAction () {
        //TODO: uncomment when problem iwth MakeProjectLogicalViewRootNode folder will be fixed, now "Folder" can be null when it should not be null
        //putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
    }

    @Override
    protected boolean enable(Node[] activatedNodes)  {
        if (activatedNodes.length != 1) {
            return false;
        }
        
        Object project = activatedNodes[0].getValue("Project"); // NOI18N
        if (project == null || (!(project instanceof Project))) {
            return false;
        }
        
        Folder folder = (Folder)activatedNodes[0].getValue("Folder"); // NOI18N
        if (folder == null) {
            return false;
        }
        if (!folder.isProjectFiles()) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return getString("CTL_AddExistingFolderItemsAction"); // NOI18N
    }

    @Override
    public void performAction(Node[] activatedNodes) {
        //boolean notifySources = false;
        Node n = activatedNodes[0];
        Project project = (Project)n.getValue("Project"); // NOI18N
        assert project != null;
        Folder folder = (Folder)n.getValue("Folder"); // NOI18N
        assert folder != null;
        
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class );
        MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
        
        if (!makeConfigurationDescriptor.okToChange()) {
            return;
        }
        //String seed = null;
        //if (FileChooser.getCurrectChooserFile() != null) {
        //    seed = FileChooser.getCurrectChooserFile().getPath();
        //}
        //if (seed == null) {
        //    seed = makeConfigurationDescriptor.getBaseDir();
        //}
        
        JButton addButton = new JButton(getString("AddButtonText"));
        addButton.getAccessibleContext().setAccessibleDescription(getString("AddButtonAD"));
        Object[] options = new Object[] {
            addButton,
            DialogDescriptor.CANCEL_OPTION,
        };
        final SourceFilesPanel sourceFilesPanel = new SourceFilesPanel(project);
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(700, 380));
        panel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        panel.add(sourceFilesPanel, gridBagConstraints);
        
        JTextArea instructionsTextArea = new JTextArea();
        instructionsTextArea.setEditable(false);
        instructionsTextArea.setLineWrap(true);
        instructionsTextArea.setText(getString("AddExistingFolderItemsTxt")); // NOI18N
        instructionsTextArea.setWrapStyleWord(true);
        instructionsTextArea.setBackground(panel.getBackground());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        panel.add(instructionsTextArea, gridBagConstraints);
        
        sourceFilesPanel.setSeed(makeConfigurationDescriptor.getBaseDir(), null);
//        sourceFilesPanel.requestFocus();
//        sourceFilesPanel.initFocus();
        panel.getAccessibleContext().setAccessibleDescription(getString("AddFilesDialogAD"));
        DialogDescriptor dialogDescriptor = new DialogDescriptor(
                panel,
                getString("AddFilesDialogText"), 
                true,
                options,
                addButton,
                DialogDescriptor.BOTTOM_ALIGN,
                null,
                null);
        Object ret = DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (ret == addButton && !sourceFilesPanel.getSourceListData().isEmpty()) {
            Runnable task = new AddFilesRunnable(
                    makeConfigurationDescriptor, folder,
                    sourceFilesPanel.getSourceListData(),
                    sourceFilesPanel.getFileFilter());
            ModalMessageDlg.runLongTask(
                    WindowManager.getDefault().getMainWindow(),
                    task, null, null,
                    getString("AddingFilesDialogTitle"), // NOI18N
                    getString("AddingFilesDialogText")); // NOI18N
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(BatchBuildAction.class);
        }
        return bundle.getString(s);
    }

    private static final class AddFilesRunnable implements Runnable {
        private final MakeConfigurationDescriptor confDescriptor;
        private final Folder targetFolder;
        private final List<? extends SourceFolderInfo> foldersToAdd;
        private final FileObjectFilter fileFilter;

        public AddFilesRunnable(
                MakeConfigurationDescriptor confDescriptor,
                Folder targetFolder,
                List<? extends SourceFolderInfo> foldersToAdd,
                FileObjectFilter fileFilter) {
            this.confDescriptor = confDescriptor;
            this.targetFolder = targetFolder;
            this.foldersToAdd = foldersToAdd;
            this.fileFilter = fileFilter;
        }

        @Override
        public void run() {
            foldersToAdd.forEach((folderInfo) -> {
                confDescriptor.addFilesFromRoot(targetFolder, folderInfo.getFileObject(), null, null, false, Folder.Kind.SOURCE_LOGICAL_FOLDER, fileFilter);
            });
            confDescriptor.save();
        }
    }
}
