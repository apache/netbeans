/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.generic.project.ui.wizard;

import java.awt.Component;
import java.io.File;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle.Messages;

/**
 * The single wizard panel asking for the project name and location.
 */
@Messages({
    "LBL_CreateProjectStep=Name and Location",
    "LBL_ProjectName=Project &Name:",
    "LBL_ProjectLocation=Project &Location:",
    "LBL_CreatedFolder=Project &Folder:",
    "LBL_Browse=Br&owse...",
    "LBL_BrowseDialogTitle=Select Project Location",
    "LBL_LocalMarker=Keep repository &clean (store marker locally)",
    "TT_LocalMarker=<html>"
            + "<p>The project marker is stored in the <b>IDE's user directory</b> instead of the .nbgp file in the project folder.</p><br>"
            + "<p>The project is then only recognized by this IDE installation at this location.</p></html>",
    "MSG_DefaultProjectName=MyGenericProject",
    "ERR_InvalidName=Project Name is not a valid folder name.",
    "ERR_InvalidPath=Project Folder is not a valid path.",
    "ERR_NotWritable=Project Folder cannot be created.",
    "ERR_FolderExists=Project Folder already exists and is not empty.",
})
final class GpWizardPanel implements WizardDescriptor.Panel<WizardDescriptor>,
        WizardDescriptor.ValidatingPanel<WizardDescriptor>,
        WizardDescriptor.FinishablePanel<WizardDescriptor>,
        DocumentListener {

    private WizardDescriptor wiz;
    private JComponent component;

    private final JTextField projectName = new JTextField();
    private final JTextField projectLocation = new JTextField();
    private final JTextField createdFolder = new JTextField();
    private final JButton browseButton = new JButton();
    private final JCheckBox localMarker = new JCheckBox();
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    @Override
    public Component getComponent() {
        if (component == null) {
            component = createComponent();
        }
        return component;
    }

    @Override
    public org.openide.util.HelpCtx getHelp() {
        return null;
    }

    @Override
    public boolean isValid() {
        getComponent();
        return valid(wiz);
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    private void fireChangeEvent() {
        changeSupport.fireChange();
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        wiz = settings;
        File location = (File) wiz.getProperty("projdir"); // NOI18N
        if (location == null || location.getParentFile() == null || !location.getParentFile().isDirectory()) {
            location = ProjectChooser.getProjectsFolder();
        } else {
            location = location.getParentFile();
        }
        projectLocation.setText(location.getAbsolutePath());

        String name = (String) wiz.getProperty("name"); // NOI18N
        projectName.setText(name == null ? Bundle.MSG_DefaultProjectName() : name);
        projectName.selectAll();
        localMarker.setSelected(false);
        updateCreatedFolder();
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        settings.putProperty("name", projectName.getText().strip()); // NOI18N
        settings.putProperty("projdir", new File(createdFolder.getText().strip())); // NOI18N
        settings.putProperty("localMarker", localMarker.isSelected()); // NOI18N
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

    @Override
    public void validate() throws WizardValidationException {
        // nothing to validate
    }

    private boolean valid(WizardDescriptor d) {
        if (projectName.getText().isEmpty()) {
            d.putProperty("WizardPanel_errorMessage", Bundle.ERR_InvalidName()); // NOI18N
            return false;
        }
        File location = FileUtil.normalizeFile(new File(projectLocation.getText()).getAbsoluteFile());
        if (!location.isDirectory()) {
            d.putProperty("WizardPanel_errorMessage", Bundle.ERR_InvalidPath()); // NOI18N
            return false;
        }
        File destFolder = FileUtil.normalizeFile(new File(createdFolder.getText()).getAbsoluteFile());
        File parent = destFolder;
        while (parent != null && !parent.exists()) {
            parent = parent.getParentFile();
        }
        if (parent == null || !parent.canWrite()) {
            d.putProperty("WizardPanel_errorMessage", Bundle.ERR_NotWritable()); // NOI18N
            return false;
        }
        if (FileUtil.toFileObject(parent) == null) {
            d.putProperty("WizardPanel_errorMessage", Bundle.ERR_InvalidPath()); // NOI18N
            return false;
        }
        File[] kids = destFolder.listFiles();
        if (destFolder.exists() && kids != null && kids.length > 0) {
            // Folder exists and is not empty
            d.putProperty("WizardPanel_errorMessage", Bundle.ERR_FolderExists()); // NOI18N
            return false;
        }
        d.putProperty("WizardPanel_errorMessage", ""); // NOI18N
        return true;
    }

    private JComponent createComponent() {
        JLabel nameLabel = new JLabel();
        Mnemonics.setLocalizedText(nameLabel, Bundle.LBL_ProjectName());
        JLabel locationLabel = new JLabel();
        Mnemonics.setLocalizedText(locationLabel, Bundle.LBL_ProjectLocation());
        JLabel folderLabel = new JLabel();
        Mnemonics.setLocalizedText(folderLabel, Bundle.LBL_CreatedFolder());
        createdFolder.setEditable(false);
        Mnemonics.setLocalizedText(browseButton, Bundle.LBL_Browse());
        browseButton.addActionListener(e -> browse());
        Mnemonics.setLocalizedText(localMarker, Bundle.LBL_LocalMarker());
        localMarker.setToolTipText(Bundle.TT_LocalMarker());

        // Making all labels stretchable snaps the shorter ones to
        // the widest label, so the fields start at the same x and
        // stretch to the edge.
        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING, false)
                    .addComponent(nameLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(locationLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(folderLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(projectName, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(projectLocation, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseButton))
                    .addComponent(createdFolder, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(localMarker))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(projectName))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(locationLabel)
                    .addComponent(projectLocation)
                    .addComponent(browseButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(folderLabel)
                    .addComponent(createdFolder))
                .addGap(18, 18, 18)
                .addComponent(localMarker)
                .addGap(0, 0, Short.MAX_VALUE)
            )
        );
        projectName.getDocument().addDocumentListener(this);
        projectLocation.getDocument().addDocumentListener(this);

        panel.setName(Bundle.LBL_CreateProjectStep());
        return panel;
    }

    private void browse() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(Bundle.LBL_BrowseDialogTitle());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String path = projectLocation.getText();
        if (!path.isEmpty()) {
            File f = new File(path);
            if (f.exists()) {
                chooser.setSelectedFile(f);
            }
        }
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(component)) {
            projectLocation.setText(FileUtil.normalizeFile(chooser.getSelectedFile()).getAbsolutePath());
        }
        fireChangeEvent();
    }

    // Implementation of DocumentListener --------------------------------------
    
    @Override
    public void changedUpdate(DocumentEvent e) {
        updateTexts(e);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateTexts(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateTexts(e);
    }

    private void updateTexts(DocumentEvent e) {
        Document doc = e.getDocument();
        if (doc == projectName.getDocument() || doc == projectLocation.getDocument()) {
            updateCreatedFolder();
        }
        fireChangeEvent();
    }

    private void updateCreatedFolder() {
        createdFolder.setText(projectLocation.getText() + File.separator + projectName.getText());
    }

}
