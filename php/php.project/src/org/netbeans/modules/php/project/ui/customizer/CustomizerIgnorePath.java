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

package org.netbeans.modules.php.project.ui.customizer;

import java.awt.Component;
import java.io.File;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.classpath.BasePathSupport;
import org.netbeans.modules.php.project.ui.PathUiSupport;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class CustomizerIgnorePath extends JPanel implements HelpCtx.Provider {
    private static final long serialVersionUID = 48657896579437L;

    private final Category category;
    private final PhpProject project;

    public CustomizerIgnorePath(Category category, PhpProjectProperties uiProps) {

        this.category = category;
        project = uiProps.getProject();

        initComponents();

        PathUiSupport.EditMediator.FileChooserDirectoryHandler directoryHandler = new PathUiSupport.EditMediator.FileChooserDirectoryHandler() {
            @Override
            public String getDirKey() {
                return CustomizerIgnorePath.class.getName();
            }
            @Override
            public File getCurrentDirectory() {
                return FileUtil.toFile(ProjectPropertiesSupport.getSourcesDirectory(project));
            }
        };

        ignorePathList.setModel(uiProps.getIgnorePathListModel());
        ignorePathList.setCellRenderer(uiProps.getIgnorePathListRenderer());
        PathUiSupport.EditMediator.register(uiProps.getProject(),
                                               ignorePathList,
                                               addIgnorePathButton.getModel(),
                                               removeIgnorePathButton.getModel(),
                                               directoryHandler);
        codeAnalysisExcludesList.setModel(uiProps.getCodeAnalysisExcludesListModel());
        codeAnalysisExcludesList.setCellRenderer(uiProps.getCodeAnalysisExcludesListModelListRenderer());
        PathUiSupport.EditMediator.register(uiProps.getProject(),
                                               codeAnalysisExcludesList,
                                               addCodeAnalysisExcludesButton.getModel(),
                                               removeCodeAnalysisExcludesButton.getModel(),
                                               directoryHandler);

        ListDataListener listDataListener = new DefaultListDataListener();
        ignorePathList.getModel().addListDataListener(listDataListener);
        codeAnalysisExcludesList.getModel().addListDataListener(listDataListener);
    }

    void validateData() {
        if (!validateFolders(ignorePathList.getModel())) {
            return;
        }
        if (!validateFolders(codeAnalysisExcludesList.getModel())) {
            return;
        }
        category.setErrorMessage(null);
        category.setValid(true);
    }

    private boolean validateFolders(ListModel<BasePathSupport.Item> model) {
        int size = model.getSize();
        for (int i = 0; i < size; ++i) {
            BasePathSupport.Item item = model.getElementAt(i);
            if (item.isBroken()) {
                continue;
            }
            FileObject fo = item.getFileObject(project.getProjectDirectory());
            if (fo == null) {
                // not broken but not found?!
                category.setErrorMessage(NbBundle.getMessage(CustomizerIgnorePath.class, "MSG_NotFound", item.getFilePath()));
                category.setValid(false);
                return false;
            }
            if (!CommandUtils.isUnderAnySourceGroup(project, fo, false)) {
                category.setErrorMessage(NbBundle.getMessage(CustomizerIgnorePath.class, "MSG_NotSourceGroupSubdirectory", fo.getNameExt()));
                category.setValid(false);
                return false;
            }
        }
        return true;
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ignorePathLabel = new JLabel();
        ignorePathScrollPane = new JScrollPane();
        ignorePathList = new JList<BasePathSupport.Item>();
        addIgnorePathButton = new JButton();
        removeIgnorePathButton = new JButton();
        codeAnalysisExcludesLabel = new JLabel();
        codeAnalysisExcludesScrollPane = new JScrollPane();
        codeAnalysisExcludesList = new JList<BasePathSupport.Item>();
        addCodeAnalysisExcludesButton = new JButton();
        removeCodeAnalysisExcludesButton = new JButton();
        codeAnalysisExcludesInfoLabel = new JLabel();

        ignorePathLabel.setLabelFor(ignorePathList);
        Mnemonics.setLocalizedText(ignorePathLabel, NbBundle.getMessage(CustomizerIgnorePath.class, "CustomizerIgnorePath.ignorePathLabel.text")); // NOI18N

        ignorePathScrollPane.setViewportView(ignorePathList);
        ignorePathList.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerIgnorePath.class, "CustomizerIgnorePath.ignorePathList.AccessibleContext.accessibleName")); // NOI18N
        ignorePathList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerIgnorePath.class, "CustomizerIgnorePath.ignorePathList.AccessibleContext.accessibleDescription")); // NOI18N

        Mnemonics.setLocalizedText(addIgnorePathButton, NbBundle.getMessage(CustomizerIgnorePath.class, "CustomizerIgnorePath.addIgnorePathButton.text")); // NOI18N

        Mnemonics.setLocalizedText(removeIgnorePathButton, NbBundle.getMessage(CustomizerIgnorePath.class, "CustomizerIgnorePath.removeIgnorePathButton.text")); // NOI18N

        Mnemonics.setLocalizedText(codeAnalysisExcludesLabel, NbBundle.getMessage(CustomizerIgnorePath.class, "CustomizerIgnorePath.codeAnalysisExcludesLabel.text")); // NOI18N

        codeAnalysisExcludesScrollPane.setViewportView(codeAnalysisExcludesList);

        Mnemonics.setLocalizedText(addCodeAnalysisExcludesButton, NbBundle.getMessage(CustomizerIgnorePath.class, "CustomizerIgnorePath.addCodeAnalysisExcludesButton.text")); // NOI18N

        Mnemonics.setLocalizedText(removeCodeAnalysisExcludesButton, NbBundle.getMessage(CustomizerIgnorePath.class, "CustomizerIgnorePath.removeCodeAnalysisExcludesButton.text")); // NOI18N

        Mnemonics.setLocalizedText(codeAnalysisExcludesInfoLabel, NbBundle.getMessage(CustomizerIgnorePath.class, "CustomizerIgnorePath.codeAnalysisExcludesInfoLabel.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(ignorePathScrollPane)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(removeIgnorePathButton)
                            .addComponent(addIgnorePathButton)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(codeAnalysisExcludesScrollPane)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(addCodeAnalysisExcludesButton)
                            .addComponent(removeCodeAnalysisExcludesButton)))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(ignorePathLabel)
                    .addComponent(codeAnalysisExcludesLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(codeAnalysisExcludesInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {addCodeAnalysisExcludesButton, addIgnorePathButton, removeCodeAnalysisExcludesButton, removeIgnorePathButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(ignorePathLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addIgnorePathButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(removeIgnorePathButton))
                    .addComponent(ignorePathScrollPane))
                .addGap(18, 18, 18)
                .addComponent(codeAnalysisExcludesLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addCodeAnalysisExcludesButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(removeCodeAnalysisExcludesButton))
                    .addComponent(codeAnalysisExcludesScrollPane, GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(codeAnalysisExcludesInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        ignorePathLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerIgnorePath.class, "CustomizerIgnorePath.ignorePathLabel.AccessibleContext.accessibleName")); // NOI18N
        ignorePathLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerIgnorePath.class, "CustomizerIgnorePath.ignorePathLabel.AccessibleContext.accessibleDescription")); // NOI18N
        ignorePathScrollPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerIgnorePath.class, "CustomizerIgnorePath.ignorePathScrollPane.AccessibleContext.accessibleName")); // NOI18N
        ignorePathScrollPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerIgnorePath.class, "CustomizerIgnorePath.ignorePathScrollPane.AccessibleContext.accessibleDescription")); // NOI18N
        addIgnorePathButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerIgnorePath.class, "CustomizerIgnorePath.addIgnorePathButton.AccessibleContext.accessibleName")); // NOI18N
        addIgnorePathButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerIgnorePath.class, "CustomizerIgnorePath.addIgnorePathButton.AccessibleContext.accessibleDescription")); // NOI18N
        removeIgnorePathButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerIgnorePath.class, "CustomizerIgnorePath.removeIgnorePathButton.AccessibleContext.accessibleName")); // NOI18N
        removeIgnorePathButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerIgnorePath.class, "CustomizerIgnorePath.removeIgnorePathButton.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerIgnorePath.class, "CustomizerIgnorePath.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerIgnorePath.class, "CustomizerIgnorePath.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton addCodeAnalysisExcludesButton;
    private JButton addIgnorePathButton;
    private JLabel codeAnalysisExcludesInfoLabel;
    private JLabel codeAnalysisExcludesLabel;
    private JList<BasePathSupport.Item> codeAnalysisExcludesList;
    private JScrollPane codeAnalysisExcludesScrollPane;
    private JLabel ignorePathLabel;
    private JList<BasePathSupport.Item> ignorePathList;
    private JScrollPane ignorePathScrollPane;
    private JButton removeCodeAnalysisExcludesButton;
    private JButton removeIgnorePathButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.project.ui.customizer.CustomizerIgnorePath"); // NOI18N
    }

    //~ Inner classes

    private final class DefaultListDataListener implements ListDataListener {

        @Override
        public void intervalAdded(ListDataEvent e) {
            validateData();
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            validateData();
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            validateData();
        }

    }

}
