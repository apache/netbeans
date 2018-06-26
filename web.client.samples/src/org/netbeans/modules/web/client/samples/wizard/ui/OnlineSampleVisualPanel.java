/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.client.samples.wizard.ui;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.web.client.samples.wizard.WizardConstants;
import org.netbeans.modules.web.client.samples.wizard.iterator.OnlineSiteTemplate;
import org.netbeans.modules.web.clientproject.api.network.NetworkException;
import org.netbeans.modules.web.clientproject.api.network.NetworkSupport;
import org.netbeans.modules.web.clientproject.api.util.ValidationUtilities;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Janicek
 */
public class OnlineSampleVisualPanel extends javax.swing.JPanel {

    private final List<ChangeListener> listeners;
    private final WizardDescriptor descriptor;


    public OnlineSampleVisualPanel(WizardDescriptor descriptor) {
        this.listeners = new CopyOnWriteArrayList<ChangeListener>();
        this.descriptor = descriptor;

        initComponents();
        initFields();
    }

    private void initFields() {
        final String projectName = (String) descriptor.getProperty(WizardConstants.SAMPLE_PROJECT_NAME);
        final String projectURL = (String) descriptor.getProperty(WizardConstants.SAMPLE_PROJECT_URL);

        templateUrlTextField.setText(projectURL);

        // default name & location
        File projectLocation = ProjectChooser.getProjectsFolder();
        projectLocationTextField.setText(projectLocation.getAbsolutePath());

        projectNameTextField.setText(findName(projectLocation, projectName));
        projectNameTextField.selectAll();
        createdFolderTextField.setText(getProjectLocation() + File.separatorChar + getProjectName());

        // listeners
        DocumentListener documentListener = new DefaultDocumentListener();
        projectNameTextField.getDocument().addDocumentListener(documentListener);
        projectLocationTextField.getDocument().addDocumentListener(documentListener);
        setName(NbBundle.getMessage(OnlineSampleVisualPanel.class, "LBL_NameAndLocation"));
    }

    private String findName(File projectLocation, String projectName) {
        final String baseName = projectName;
        int index = 0;
        while ((new File(projectLocation, projectName)).exists()) {
            index++;
            projectName = baseName + index;
        }
        return projectName;
    }

    public final void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public final void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    public String getErrorMessage() {
        String error = validateProjectName();
        if (error != null) {
            return error;
        }
        error = validateProjectLocation();
        if (error != null) {
            return error;
        }
        return null;
    }

    @NbBundle.Messages("OnlineSampleVisualPanel.error.name.missing=Project name must be provided.")
    private String validateProjectName() {
        String projectName = getProjectName();
        if (projectName.isEmpty()) {
            return Bundle.OnlineSampleVisualPanel_error_name_missing();
        }
        return null;
    }

    @NbBundle.Messages({
        "OnlineSampleVisualPanel.error.location.invalid=Project location is not a valid path.",
        "OnlineSampleVisualPanel.error.location.notWritable=Project folder cannot be created.",
        "OnlineSampleVisualPanel.error.location.notEmpty=Project folder already exists and is not empty."
    })
    private String validateProjectLocation() {
        File projectLocation = FileUtil.normalizeFile(new File(getProjectLocation()).getAbsoluteFile());
        if (!projectLocation.isDirectory()) {
            return Bundle.OnlineSampleVisualPanel_error_location_invalid();
        }
        final File destFolder = getProjectDirectory();
        if (!ValidationUtilities.isValidFilename(destFolder)) {
            return Bundle.OnlineSampleVisualPanel_error_location_invalid();
        }

        File projLoc = destFolder;
        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            return Bundle.OnlineSampleVisualPanel_error_location_notWritable();
        }

        if (FileUtil.toFileObject(projLoc) == null) {
            return Bundle.OnlineSampleVisualPanel_error_location_invalid();
        }

        File[] kids = destFolder.listFiles();
        if (destFolder.exists() && kids != null && kids.length > 0) {
            return Bundle.OnlineSampleVisualPanel_error_location_notEmpty();
        }
        return null;
    }

    @NbBundle.Messages({
        "# {0} - template name",
        "SiteTemplateWizard.template.preparing=Preparing template \"{0}\" for first usage...",
        "# {0} - template name",
        "SiteTemplateWizard.error.preparing=Cannot prepare template \"{0}\" (see IDE log for more details)."
    })
    public String prepareTemplate() {
        assert !EventQueue.isDispatchThread();

        final OnlineSiteTemplate siteTemplate = (OnlineSiteTemplate) descriptor.getProperty(WizardConstants.SAMPLE_TEMPLATE);
        final String templateName = siteTemplate.getName();

        if (siteTemplate.isPrepared()) {
            return null;
        }

        ProgressHandle progressHandle = ProgressHandleFactory.createHandle(Bundle.SiteTemplateWizard_template_preparing(templateName));
        progressHandle.start();
        try {
            while (true) {
                try {
                    siteTemplate.prepare();
                    break;
                } catch (NetworkException ex) {
                    if (!NetworkSupport.showNetworkErrorDialog(ex.getFailedRequests())) {
                        return Bundle.SiteTemplateWizard_error_preparing(templateName);
                    }
                } catch (InterruptedException ex) {
                    return Bundle.SiteTemplateWizard_error_preparing(templateName);
                }
            }
        } catch (IOException ex) {
            return Bundle.SiteTemplateWizard_error_preparing(templateName);
        } finally {
            progressHandle.finish();
        }
        return null;
    }

    public String getProjectName() {
        return projectNameTextField.getText().trim();
    }

    public String getProjectLocation() {
        return projectLocationTextField.getText().trim();
    }

    public File getProjectDirectory() {
        return FileUtil.normalizeFile(new File(createdFolderTextField.getText()));
    }

    public String getProjectURL() {
        return templateUrlTextField.getText();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        createdFolderLabel = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();
        projectLocationTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        templateUrlLabel = new javax.swing.JLabel();
        templateUrlTextField = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(OnlineSampleVisualPanel.class, "LBL_ProjectName")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(OnlineSampleVisualPanel.class, "LBL_ProjectLocation")); // NOI18N
        projectLocationLabel.setMaximumSize(new java.awt.Dimension(141, 15));
        projectLocationLabel.setMinimumSize(new java.awt.Dimension(141, 15));
        projectLocationLabel.setPreferredSize(new java.awt.Dimension(141, 15));

        org.openide.awt.Mnemonics.setLocalizedText(createdFolderLabel, org.openide.util.NbBundle.getMessage(OnlineSampleVisualPanel.class, "LBL_ProjectFolder")); // NOI18N
        createdFolderLabel.setMaximumSize(new java.awt.Dimension(141, 15));
        createdFolderLabel.setMinimumSize(new java.awt.Dimension(141, 15));
        createdFolderLabel.setPreferredSize(new java.awt.Dimension(141, 15));

        createdFolderTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(OnlineSampleVisualPanel.class, "LBL_Browse")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(templateUrlLabel, org.openide.util.NbBundle.getMessage(OnlineSampleVisualPanel.class, "OnlineSampleVisualPanel.templateUrlLabel.text")); // NOI18N
        templateUrlLabel.setMaximumSize(new java.awt.Dimension(141, 15));
        templateUrlLabel.setMinimumSize(new java.awt.Dimension(141, 15));
        templateUrlLabel.setPreferredSize(new java.awt.Dimension(141, 15));

        templateUrlTextField.setEditable(false);
        templateUrlTextField.setText(org.openide.util.NbBundle.getMessage(OnlineSampleVisualPanel.class, "OnlineSampleVisualPanel.templateUrlTextField.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(projectNameLabel)
                    .addComponent(projectLocationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(createdFolderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(templateUrlLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(projectLocationTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(createdFolderTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(projectNameTextField)
                    .addComponent(templateUrlTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browseButton))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectNameLabel)
                    .addComponent(projectNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectLocationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(projectLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createdFolderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(createdFolderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(templateUrlLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(templateUrlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        projectNameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OnlineSampleVisualPanel.class, "OnlineSampleVisualPanel.projectNameLabel.AccessibleContext.accessibleName")); // NOI18N
        projectLocationLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OnlineSampleVisualPanel.class, "OnlineSampleVisualPanel.projectLocationLabel.AccessibleContext.accessibleName")); // NOI18N
        createdFolderLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OnlineSampleVisualPanel.class, "OnlineSampleVisualPanel.createdFolderLabel.AccessibleContext.accessibleName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        File workDir = null;
        String projectLocation = getProjectLocation();
        if (projectLocation != null && !projectLocation.isEmpty()) {
            File projDir = new File(projectLocation);
            if (projDir.isDirectory()) {
                workDir = projDir;
            }
        }
        if (workDir == null) {
            workDir = ProjectChooser.getProjectsFolder();
        }
        File projectDir = new FileChooserBuilder(SampleVisualPanel.class)
        .setTitle(NbBundle.getMessage(SampleVisualPanel.class, "TTL_DialogLocation"))   // NOI18N
        .setDirectoriesOnly(true)
        .setDefaultWorkingDirectory(workDir)
        .forceUseOfDefaultWorkingDirectory(true)
        .showOpenDialog();
        if (projectDir != null) {
            projectLocationTextField.setText(FileUtil.normalizeFile(projectDir).getAbsolutePath());
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    private void updateProjectFolder() {
        createdFolderTextField.setText(getProjectLocation() + File.separatorChar + getProjectName());
    }

    private void fireChange() {
        for (ChangeListener listener : listeners) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }

    private final class DefaultDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processChange();
        }

        private void processChange() {
            updateProjectFolder();
            fireChange();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
    private javax.swing.JLabel templateUrlLabel;
    private javax.swing.JTextField templateUrlTextField;
    // End of variables declaration//GEN-END:variables
}
