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

/*
 * FollowUp.java
 *
 * Created on Dec 18, 2008, 3:12:25 PM
 */

package org.netbeans.modules.cnd.discovery.projectimport;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.discovery.projectimport.ImportProject.State;
import org.netbeans.modules.cnd.discovery.projectimport.ImportProject.Step;
import org.netbeans.modules.cnd.api.project.BrokenIncludes;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 */
public final class FollowUp extends JPanel {
    private static final RequestProcessor RP = new RequestProcessor(FollowUp.class.getName(), 2);
    /** Creates new form FollowUp */
    private FollowUp(ImportProject importer, NativeProject project) {
        initComponents();
        details.setVisible(false);
        detailsPane.setVisible(false);
        Map<Step,State> map = importer.getImportResult();
        showState(Step.Project, map.get(Step.Project), projectCreated);
        showState(Step.Configure, map.get(Step.Configure), configureDone);
        showState(Step.MakeClean, map.get(Step.MakeClean), makeClean);
        showState(Step.Make, map.get(Step.Make), make);
        showCodeAssistanceState(map, project);
        CsmModel model = CsmModelAccessor.getModel();
        if (model != null  && project != null) {
            initDetails(model.getProject(project));
        }
    }

    private void showState(Step step, State state, JLabel label){
        if (state == null) {
            label.setVisible(false);
            return;
        }
        switch (state){
            case Successful:
                break;
            case Fail:
                switch (step){
                    case Project:
                        label.setText(getString("ProjectCreatedFailedText")); // NOI18N
                        break;
                    case Configure:
                        label.setText(getString("ConfigureFailedText")); // NOI18N
                        break;
                    case MakeClean:
                        label.setText(getString("MakeCleanFailedText")); // NOI18N
                        break;
                    case Make:
                        label.setText(getString("MakeFailedText")); // NOI18N
                        break;
                }
                label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/discovery/wizard/resources/error.png"))); // NOI18N
                break;
            case Skiped:
                label.setVisible(false);
                break;
        }
    }

    private void showCodeAssistanceState(Map<Step,State> map, NativeProject project){
        if (hasBrokenIncludes(project)){
            //State dwarf = map.get(Step.DiscoveryDwarf);
            //State log = map.get(Step.DiscoveryLog);
            //State model = map.get(Step.DiscoveryModel);
            //State excluded = map.get(Step.FixExcluded);
            //State macros = map.get(Step.FixMacros);
            codeAssistance.setText(getString("CodeAssistanceFailedText")); // NOI18N
            codeAssistance.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/discovery/wizard/resources/error.png"))); // NOI18N
        } else {
            State stateMake = map.get(Step.Make);
            if (stateMake == State.Fail) {
                codeAssistance.setText(getString("CodeAssistanceInfoText")); // NOI18N
                codeAssistance.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/discovery/wizard/resources/info.png"))); // NOI18N
            }
        }
    }

    private static  boolean hasBrokenIncludes(NativeProject project) {
        BrokenIncludes provider = Lookup.getDefault().lookup(BrokenIncludes.class);
        if (provider != null) {
            return provider.isBroken(project);
        }
        return false;
    }

    private void initDetails(CsmProject csmProject) {
        if (csmProject == null) {
            return;
        }
        Object o = csmProject.getPlatformProject();
        if (o instanceof NativeProject) {
            NativeProject nativeProject = (NativeProject) o;
            int sourceFiles = 0;
            int sourceFilesExcluded = 0;
            int headerFiles = 0;
            int headerFilesExcluded = 0;
            for (NativeFileItem item : nativeProject.getAllFiles()) {
                switch (item.getLanguage()) {
                    case C:
                    case CPP:
                        sourceFiles++;
                        if (item.isExcluded()) {
                            sourceFilesExcluded++;
                        }
                        break;
                    case C_HEADER:
                        headerFiles++;
                        if (item.isExcluded()) {
                            headerFilesExcluded++;
                        }
                        break;
                }
            }
            String text = MessageFormat.format(getString("Details.String"), // NOI18N
                    sourceFilesExcluded, sourceFiles,
                    headerFilesExcluded, headerFiles);
            detailsTextPane.setContentType("text/html"); // NOI18N
            detailsTextPane.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
            detailsTextPane.setText(text);
            details.setVisible(true);
            detailsPane.setVisible(true);
        }
    }

    public static void showFollowUp(final ImportProject importer, final NativeProject project) {
        ActionListener onClickAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (importer.isProjectOpened()){
                    FollowUp panel = new FollowUp(importer, project);
                    DialogDescriptor descriptor = new DialogDescriptor(panel, getString("Dialog_Title"), // NOI18N
                            true, new Object[]{DialogDescriptor.CLOSED_OPTION}, DialogDescriptor.CLOSED_OPTION,
                            DialogDescriptor.DEFAULT_ALIGN, null, null);
                    Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
                    try {
                        dlg.setVisible(true);
                    } catch (Throwable th) {
                        if (!(th.getCause() instanceof InterruptedException)) {
                            throw new RuntimeException(th);
                        }
                        descriptor.setValue(DialogDescriptor.CANCEL_OPTION);
                    } finally {
                        dlg.dispose();
                    }
                }
            }
        };
        String title;
        ImageIcon icon;
        NotificationDisplayer.Category category;
        if (hasBrokenIncludes(project)) {
            title = getString("Configure_Fail"); // NOI18N
            icon  = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/discovery/wizard/resources/info.png", false); // NOI18N
            category = NotificationDisplayer.Category.ERROR;
        } else {
            State stateMake = importer.getImportResult().get(Step.Make);
            if (stateMake == State.Fail) {
                title = getString("Configure_Info"); // NOI18N
                icon  = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/discovery/wizard/resources/info.png", false); // NOI18N
                category = NotificationDisplayer.Category.WARNING;
            } else {
                title = getString("Configure_Success"); // NOI18N
                icon  = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/discovery/wizard/resources/check.png", false); // NOI18N
                category = NotificationDisplayer.Category.INFO;
            }
        }
        final Notification notification = NotificationDisplayer.getDefault().notify(title, icon,
                getString("Dialog_Action"), onClickAction, NotificationDisplayer.Priority.HIGH, category); // NOI18N
        RP.post(new Runnable() {
            @Override
            public void run() {
                notification.clear();
            }
        }, 15*1000);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        projectCreated = new javax.swing.JLabel();
        configureDone = new javax.swing.JLabel();
        makeClean = new javax.swing.JLabel();
        make = new javax.swing.JLabel();
        codeAssistance = new javax.swing.JLabel();
        detailsPane = new javax.swing.JScrollPane();
        detailsTextPane = new javax.swing.JTextPane();
        details = new javax.swing.JLabel();

        setEnabled(false);
        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(320, 280));
        setLayout(new java.awt.GridBagLayout());

        projectCreated.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/discovery/wizard/resources/check.png"))); // NOI18N
        projectCreated.setText(org.openide.util.NbBundle.getMessage(FollowUp.class, "ProjectCreatedText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(projectCreated, gridBagConstraints);

        configureDone.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/discovery/wizard/resources/check.png"))); // NOI18N
        configureDone.setText(org.openide.util.NbBundle.getMessage(FollowUp.class, "ConfigureDoneText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 10);
        add(configureDone, gridBagConstraints);

        makeClean.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/discovery/wizard/resources/check.png"))); // NOI18N
        makeClean.setText(org.openide.util.NbBundle.getMessage(FollowUp.class, "MakeCleanText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 10);
        add(makeClean, gridBagConstraints);

        make.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/discovery/wizard/resources/check.png"))); // NOI18N
        make.setText(org.openide.util.NbBundle.getMessage(FollowUp.class, "MakeText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 10);
        add(make, gridBagConstraints);

        codeAssistance.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/discovery/wizard/resources/check.png"))); // NOI18N
        codeAssistance.setText(org.openide.util.NbBundle.getMessage(FollowUp.class, "CodeAssistanceText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 10, 10);
        add(codeAssistance, gridBagConstraints);

        detailsPane.setBorder(null);
        detailsPane.setOpaque(false);
        detailsPane.setPreferredSize(new java.awt.Dimension(100, 100));

        detailsTextPane.setEditable(false);
        detailsTextPane.setOpaque(false);
        detailsPane.setViewportView(detailsTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        add(detailsPane, gridBagConstraints);

        details.setLabelFor(detailsTextPane);
        details.setText(org.openide.util.NbBundle.getMessage(FollowUp.class, "FollowUp.details.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        add(details, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel codeAssistance;
    private javax.swing.JLabel configureDone;
    private javax.swing.JLabel details;
    private javax.swing.JScrollPane detailsPane;
    private javax.swing.JTextPane detailsTextPane;
    private javax.swing.JLabel make;
    private javax.swing.JLabel makeClean;
    private javax.swing.JLabel projectCreated;
    // End of variables declaration//GEN-END:variables

    private static String getString(String key, String ... params){
        return NbBundle.getMessage(FollowUp.class, key, params);
    }
}
