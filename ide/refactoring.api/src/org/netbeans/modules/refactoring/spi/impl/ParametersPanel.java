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
package org.netbeans.modules.refactoring.spi.impl;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.spi.ui.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.*;
import org.netbeans.modules.refactoring.api.impl.APIAccessor;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.refactoring.spi.impl.ProblemComponent.CallbackAction;
import org.openide.LifecycleManager;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;

/**
 * Main panel for refactoring parameters dialog. This panel is automatically
 * displayed by refactoring action. It handles all the generic logic for
 * displaying progress, checking for problems (during parameters validation and
 * refactoring preparation) and accepting/canceling the refactoring.
 * Refactoring-specific parameters panel is requested from {@link RefactoringUI}
 * implementation (passed to this panel by {@link
 * AbstractRefactoringAction}) and then displayed in the upper part of this
 * panel. Refactoring-specific panel can use setPreviewEnabled method to
 * enable/disable button that accepts the parameters. The button is disabled by
 * default.
 *
 * @author Martin Matula, Jan Becicka
 */
public class ParametersPanel extends JPanel implements ProgressListener, ChangeListener {

    public static final String JUMP_TO_FIRST_OCCURENCE = "JUMP_TO_FIRST_OCCURENCE"; //NOI18N
    private static final String PREF_OPEN_NEW_TAB = "PREF_OPEN_NEW_TAB"; //NI18N
    private static final Logger LOGGER = Logger.getLogger(ParametersPanel.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(ParametersPanel.class.getName(), 1, false, false);
    /**
     * @see #result
     */
    private final Object RESULT_LOCK = new Object();
    // refactoring elements that will be returned as a result of showDialog method
    private RefactoringSession result;
    // corresponding implementation of RefactoringUI
    private final RefactoringUI rui;
    // refactoring-specific panel returned from RefactoringUI.getPanel
    private final JPanel customPanel;
    private final CustomRefactoringPanel customComponent;
    // parent dialog
    private transient JDialog dialog = null;
    // disabled components that should be reenabled by a call to setPanelEnabled
    private ArrayList components = null;
    private Problem problem;
    private ErrorPanel errorPanel;
    private final int PRE_CHECK = 0;
    private final int INPUT_PARAMETERS = 1;
    private final int POST_CHECK = 2;
    private final int CHECK_PARAMETERS = 3;
    private transient int currentState = INPUT_PARAMETERS;
    private boolean cancelRequest = false;
    private boolean canceledDialog;
    private boolean inspect = false;
    
    private int suppressChecks;
    private boolean checkNeeded;

    /**
     * Enables/disables Preview button of dialog. Can be used by
     * refactoring-specific parameters panel to disable accepting the parameters
     * when needed (e.g. if not all parameters were entered). When the dialog is
     * displayed, the button is disabled by default.
     *
     * @param enabled
     * <code>true</code> to enable preview button,
     * <code>false</code> to disable it.
     */
    public void setPreviewEnabled(boolean enabled) {
        RefactoringPanel.checkEventThread();
        next.setEnabled(enabled && !isPreviewRequired());
    }

    /**
     * Creates ParametersPanel
     *
     * @param rui Implementation of RefactoringUI for desired refactoring.
     */
    public ParametersPanel(RefactoringUI rui) {
        RefactoringPanel.checkEventThread();
        this.rui = rui;
        initComponents();

        // #143551 
        HelpCtx helpCtx = getHelpCtx();
        help.setEnabled(helpCtx != null && helpCtx != HelpCtx.DEFAULT_HELP);

        innerPanel.setBorder(null);
        label.setText(" ");//NOI18N
        this.customComponent = rui.getPanel(this);
        if (this.customComponent != null) {
            this.customPanel = (JPanel) this.customComponent.getComponent();
        } else {
            customPanel = null;
        }
        errorPanel = new ErrorPanel(rui);
        back.setVisible(!rui.isQuery());
        previewButton.setVisible(!rui.isQuery());
        openInNewTab.setVisible(rui.isQuery());
        setButtonsEnabled(false);
        if (rui.isQuery()) {
            Preferences prefs = NbPreferences.forModule(RefactoringPanel.class);
            openInNewTab.setSelected(prefs.getBoolean(PREF_OPEN_NEW_TAB, true));
        }
        Mnemonics.setLocalizedText(next, NbBundle.getMessage(ParametersPanel.class, rui.isQuery() ? "CTL_Find" : "CTL_Finish"));

        //TODO: Ugly Hack
        inspect = "org.netbeans.modules.java.hints.spiimpl.refactoring.InspectAndRefactorUI".equals(rui.getClass().getName());
        //cancel.setEnabled(false);
        next.setVisible(!isPreviewRequired());
        label.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        label.addHyperlinkListener(new HyperlinkListener() {

            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    HtmlBrowser.URLDisplayer.getDefault().showURLExternal(e.getURL());
                }
            }
        });
    }
    
    public void withBatchedChanges(Runnable r) {
        suppressChecks++;
        try {
            r.run();
        } finally {
            if (--suppressChecks == 0) {
                recheck();
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension cpDim = new Dimension(0,0);
        Dimension ppDim = progressPanel.getPreferredSize();
        Dimension epDim = new Dimension(0,0);
        if (customPanel != null) {
            cpDim = customPanel.getPreferredSize();
        }
        if (errorPanel != null) {
            epDim = errorPanel.getPreferredSize();
        }
        Dimension dimension = new Dimension(Math.max(cpDim.width, epDim.width), Math.max(cpDim.height, epDim.height) + ppDim.height);
        
        return dimension;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        progressPanel = new javax.swing.JPanel();
        controlsPanel = new javax.swing.JPanel();
        buttonsPanel = new javax.swing.JPanel();
        back = new javax.swing.JButton();
        previewButton = new javax.swing.JButton();
        next = new javax.swing.JButton();
        cancel = new javax.swing.JButton();
        help = new javax.swing.JButton();
        openInNewTab = new javax.swing.JCheckBox();
        innerPanel = new javax.swing.JPanel();
        label = new javax.swing.JEditorPane();
        containerPanel = new javax.swing.JPanel();
        pleaseWait = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/spi/impl/Bundle"); // NOI18N
        setName(bundle.getString("LBL_FindUsagesDialog")); // NOI18N
        setLayout(new java.awt.BorderLayout());

        progressPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 11));
        progressPanel.setOpaque(false);
        progressPanel.setLayout(new java.awt.BorderLayout());

        controlsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 5, 0));
        controlsPanel.setLayout(new java.awt.BorderLayout());

        buttonsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(back, org.openide.util.NbBundle.getBundle("org/netbeans/modules/refactoring/spi/impl/Bundle").getString("CTL_Back")); // NOI18N
        back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(previewButton, org.openide.util.NbBundle.getMessage(ParametersPanel.class, "CTL_PreviewAll")); // NOI18N
        previewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                preview(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(next, org.openide.util.NbBundle.getBundle("org/netbeans/modules/refactoring/spi/impl/Bundle").getString("CTL_Finish")); // NOI18N
        next.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refactor(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cancel, org.openide.util.NbBundle.getBundle("org/netbeans/modules/refactoring/spi/impl/Bundle").getString("CTL_Cancel")); // NOI18N
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(help, org.openide.util.NbBundle.getBundle("org/netbeans/modules/refactoring/spi/impl/Bundle").getString("CTL_Help")); // NOI18N
        help.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonsPanelLayout = new javax.swing.GroupLayout(buttonsPanel);
        buttonsPanel.setLayout(buttonsPanelLayout);
        buttonsPanelLayout.setHorizontalGroup(
            buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonsPanelLayout.createSequentialGroup()
                .addComponent(back, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(previewButton, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(next)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancel, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(help, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        buttonsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {back, cancel, help, next, previewButton});

        buttonsPanelLayout.setVerticalGroup(
            buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(back)
            .addGroup(buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(previewButton)
                .addComponent(next)
                .addComponent(cancel)
                .addComponent(help))
        );

        back.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Back")); // NOI18N
        previewButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ParametersPanel.class, "ParametersPanel.previewButton.AccessibleContext.accessibleDescription")); // NOI18N
        next.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_finish")); // NOI18N
        cancel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_cancel")); // NOI18N
        help.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_help")); // NOI18N

        controlsPanel.add(buttonsPanel, java.awt.BorderLayout.EAST);

        org.openide.awt.Mnemonics.setLocalizedText(openInNewTab, org.openide.util.NbBundle.getMessage(ParametersPanel.class, "ParametersPanel.openInNewTab.text")); // NOI18N
        openInNewTab.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        openInNewTab.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                openInNewTabStateChanged(evt);
            }
        });
        controlsPanel.add(openInNewTab, java.awt.BorderLayout.WEST);

        progressPanel.add(controlsPanel, java.awt.BorderLayout.SOUTH);

        innerPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(128, 128, 128)));
        innerPanel.setLayout(new java.awt.BorderLayout());

        label.setEditable(false);
        label.setContentType("text/html"); // NOI18N
        label.setOpaque(false);
        innerPanel.add(label, java.awt.BorderLayout.NORTH);

        progressPanel.add(innerPanel, java.awt.BorderLayout.NORTH);

        add(progressPanel, java.awt.BorderLayout.SOUTH);

        containerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        containerPanel.setLayout(new java.awt.BorderLayout());

        pleaseWait.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(pleaseWait, org.openide.util.NbBundle.getBundle("org/netbeans/modules/refactoring/spi/impl/Bundle").getString("LBL_PleaseWait")); // NOI18N
        containerPanel.add(pleaseWait, java.awt.BorderLayout.CENTER);
        pleaseWait.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ParametersPanel.class, "LBL_PleaseWait")); // NOI18N

        add(containerPanel, java.awt.BorderLayout.CENTER);

        getAccessibleContext().setAccessibleName(bundle.getString("LBL_FindUsagesDialog")); // NOI18N
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_FindUsagesDialog")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void preview(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preview
        refactor(true);
}//GEN-LAST:event_preview

    private void helpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpActionPerformed
        HelpCtx ctx = getHelpCtx();
        if (ctx != null) {
            ctx.display();
        }
    }//GEN-LAST:event_helpActionPerformed

    private void backActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backActionPerformed
        placeCustomPanel();
    }//GEN-LAST:event_backActionPerformed

    private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
            canceledDialog = true;
            if (evt != null && evt.getSource() instanceof Cancellable) {
                putResult(null);
                if (dialog != null) {
                    setPanelEnabled(true);
                    dialog.setVisible(false);
                }
            } else {
                rui.getRefactoring().cancelRequest();
                putResult(null);
                if (dialog != null) {
                    dialog.setVisible(false);
                }
                cancelRequest = true;
            }
    }//GEN-LAST:event_cancelActionPerformed
    private void refactor(final boolean previewAll) {
        LOGGER.log(Level.FINEST, "refactor - currentState={0}", currentState);
        if (currentState == PRE_CHECK && rui.hasParameters()) {
            LOGGER.finest("refactor - PRE_CHECK");
            //next is "Next>"
            placeCustomPanel();
            return;
        }

        if (currentState == PRE_CHECK && !rui.hasParameters()) {
            RefactoringSession session = putResult(RefactoringSession.create(rui.getName()));
            try {
                rui.getRefactoring().prepare(session);
                return;
            } finally {
                setVisibleLater(false);
            }
        }

        if (currentState == POST_CHECK && previewAll && currentProblemAction != null) {
            LOGGER.finest("refactor - POST_CHECK - problems");
            Cancellable doCloseParent = new Cancellable() {
                @Override
                public boolean cancel() {
                    cancelActionPerformed(new ActionEvent(this, 0, null));
                    return true;
                }
            };
            currentProblemAction.showDetails(new CallbackAction(rui), doCloseParent);
            return;
        }

        //next is Finish
        setPanelEnabled(false);
        cancel.setEnabled(true);
        openInNewTab.setVisible(false);
        next.setVisible(false);

        RequestProcessor rp = new RequestProcessor();
        final int inputState = currentState;

        if (currentState != PRE_CHECK && currentState != POST_CHECK) {
            if (rui instanceof RefactoringUIBypass && ((RefactoringUIBypass) rui).isRefactoringBypassRequired()) {
                LOGGER.finest("refactor - bypass");
                try {
                    ((RefactoringUIBypass) rui).doRefactoringBypass();
                } catch (final IOException ioe) {
                    currentState = POST_CHECK;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            String message = ioe.getMessage();
                            message = message != null ? message : ""; // NOI18N
                            placeErrorPanel(new Problem(true, message));
                        }
                    });
                } finally {
                    if (inputState == currentState) {
                        result = null;
                        setVisibleLater(false);
                    }
                }
                return;
            } else if (currentState != POST_CHECK && currentState != CHECK_PARAMETERS) {
                putResult(RefactoringSession.create(rui.getName()));
                //setParameters and prepare is done asynchronously
                rp.post(new Prepare());
            } else if (currentState == CHECK_PARAMETERS) {
                rp.post(new Prepare());
            }
        }

        //refactoring is done asynchronously
        LOGGER.finest("refactor - asynchronously");
        rp.post(new Runnable() {
            @Override
            public void run() {
                //inputState != currentState means, that panels changed and dialog will not be closed
                LOGGER.log(Level.FINEST, "refactor - inputState={0}, currentState={1}", new Object[]{inputState, currentState});
                if (inputState == currentState) {
                    final RefactoringSession session = getResult();

                    if (session != null && !previewAll && currentState != POST_CHECK && (APIAccessor.DEFAULT.hasChangesInGuardedBlocks(session) || APIAccessor.DEFAULT.hasChangesInReadOnlyFiles(session))) {
                        currentState = POST_CHECK;
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                placeErrorPanel(new Problem(false, NbBundle.getMessage(ParametersPanel.class,
                                        APIAccessor.DEFAULT.hasChangesInReadOnlyFiles(session)
                                        ? "LBL_CannotRefactorReadOnlyFile"
                                        : "LBL_CannotRefactorGuardedBlock")));
                            }
                        });
                        return;
                    }

                    try {
                        if (!previewAll && session != null) {
                            if (!rui.isQuery() && session.getRefactoringElements().isEmpty()) {
                                DialogDescriptor nd = new DialogDescriptor(NbBundle.getMessage(ParametersPanel.class, "MSG_NoPatternsFound"),
                                        rui.getName(),
                                        true,
                                        new Object[]{DialogDescriptor.OK_OPTION},
                                        DialogDescriptor.OK_OPTION,
                                        DialogDescriptor.DEFAULT_ALIGN,
                                        rui.getHelpCtx(),
                                        null);
                                DialogDisplayer.getDefault().notifyLater(nd);
                            } else {
                                //UndoWatcher.watch(session, ParametersPanel.this);
                                session.addProgressListener(ParametersPanel.this);
                                try {
                                    session.doRefactoring(true);
                                } finally {
                                    session.removeProgressListener(ParametersPanel.this);
                                }
                                //UndoWatcher.stopWatching(ParametersPanel.this);
                            }
                        }
                    } finally {
                        if (!previewAll) {
                            putResult(null);
                        }
                        if (inputState == currentState) {
                            setVisibleLater(false);
                        }
                    }
                }
            }
        });
    }

    private void refactor(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refactor
        refactor(rui.isQuery());
}//GEN-LAST:event_refactor

    private void openInNewTabStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_openInNewTabStateChanged
        Preferences prefs = NbPreferences.forModule(RefactoringPanel.class);
        prefs.putBoolean(PREF_OPEN_NEW_TAB, openInNewTab.isSelected());
    }//GEN-LAST:event_openInNewTabStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton back;
    private javax.swing.JPanel buttonsPanel;
    public javax.swing.JButton cancel;
    private javax.swing.JPanel containerPanel;
    private javax.swing.JPanel controlsPanel;
    private javax.swing.JButton help;
    private javax.swing.JPanel innerPanel;
    private javax.swing.JEditorPane label;
    private javax.swing.JButton next;
    private javax.swing.JCheckBox openInNewTab;
    private javax.swing.JLabel pleaseWait;
    private javax.swing.JButton previewButton;
    private javax.swing.JPanel progressPanel;
    // End of variables declaration//GEN-END:variables

    /**
     * dialog is closed asynchronously on the AWT event thread
     */
    private void setVisibleLater(final boolean visible) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    dialog.setVisible(visible);
                }
            }
        });
    }

    // disables/re-enables components in the custom panel
    private void setPanelEnabled(boolean enabled) {
        RefactoringPanel.checkEventThread();
        setButtonsEnabled(enabled);
        if (enabled) {
            if (components == null) {
                return;
            }
            for (Iterator it = components.iterator(); it.hasNext();) {
                ((Component) it.next()).setEnabled(true);
            }
            components = null;
        } else {
            if (components != null) {
                return;
            }
            components = new ArrayList();
            disableComponents(customPanel);
        }
    }

    // disables all components in the custom panel
    private void disableComponents(Container c) {
        if (c == null) {
            assert customPanel == null;
            return;
        }
        RefactoringPanel.checkEventThread();
        Component children[] = c.getComponents();
        for (int i = 0; i < children.length; i++) {
            if (children[i].isEnabled()) {
                children[i].setEnabled(false);
                components.add(children[i]);
            }
            if (children[i] instanceof Container) {
                disableComponents((Container) children[i]);
            }
        }
    }

    /**
     * Method used by AbstractRefactoringAction to display refactoring
     * parameters dialog. Constructs a dialog consisting of this panel and
     * Preview and Cancel buttons. Let's user to enter refactoring parameters.
     *
     * @return Collection of refactoring elements returned from the refactoring
     * operation or
     * <code>null</code> if the operation was cancelled.
     */
    public RefactoringSession showDialog() {
        RefactoringPanel.checkEventThread();
        putClientProperty(JUMP_TO_FIRST_OCCURENCE, false);
        if (rui != null) {
            rui.getRefactoring().addProgressListener(this);

            openInNewTab.setVisible(rui.isQuery());
            next.setVisible(true);

        }
        String title = (customPanel != null && customPanel.getName() != null && !"".equals(customPanel.getName())) ? customPanel.getName() : rui.getName();
        DialogDescriptor descriptor = new DialogDescriptor(this, title, true, new Object[]{}, null, 0, null, null);

        dialog = (JDialog) DialogDisplayer.getDefault().createDialog(descriptor);
        if (customPanel != null) {
            dialog.getAccessibleContext().setAccessibleName(rui.getName());
            dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ParametersPanel.class, "ACSD_FindUsagesDialog"));
        }
        
        HelpCtx helpCtx = rui.getHelpCtx();
        helpCtx = (helpCtx == null) ? HelpCtx.DEFAULT_HELP : helpCtx;
        
        HelpCtx.setHelpIDString(dialog.getRootPane(), helpCtx.getHelpID());

        setOkCancelShortcuts();
        RequestProcessor.Task task = RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!rui.isQuery()) {
                        LifecycleManager.getDefault().saveAll();
                    }
                    problem = rui.getRefactoring().preCheck();
                } catch (RuntimeException e) {
                    setVisibleLater(false);
                    throw e;
                }
                if (problem != null) {
                    currentState = PRE_CHECK;
                    if (!problem.isFatal() && rui.hasParameters()) {
                        customComponent.initialize();
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog != null) {
                                placeErrorPanel(problem);
                                dialog.setVisible(true);
                            }
                        }
                    });
                } else {
                    if (customPanel != null) {
                        customComponent.initialize();
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            placeCustomPanel();
                        }
                    });
                    if (!rui.hasParameters()) {
                        currentState = POST_CHECK;
                        RefactoringSession session = putResult(RefactoringSession.create(rui.getName()));
                        Problem problem = null;
                        try {
                            problem = rui.getRefactoring().prepare(session);
                        } catch (Throwable t) {
                            setVisibleLater(false);
                        }
                        if (problem != null) {
                            back.setEnabled(false);
                            placeErrorPanel(problem);
                        } else {
                            setVisibleLater(false);
                        }
                    }

                }
            }
        });

        if (customComponent!=null || rui.hasParameters() || APIAccessor.DEFAULT.hasPluginsWithProgress(rui.getRefactoring())) {
            dialog.pack();
            dialog.setVisible(true);
        }
        dialog.dispose();
        dialog = null;
        descriptor.setMessage("");

        if (rui != null) {
            stop(null);
            rui.getRefactoring().removeProgressListener(this);
        }
        if (!cancelRequest) {
            task.waitFinished();
        }
        RefactoringSession temp = getResult();
        putResult(null);
        return temp;
    }

    private void setOkCancelShortcuts() {
        canceledDialog = false;
        KeyStroke cancelKS = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        String cancelActionKey = "cancel"; // NOI18N
        getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(cancelKS, cancelActionKey);
        Action cancelAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                if (cancel.isEnabled()) {
                    cancelActionPerformed(ev);
                }
            }
        };

        getRootPane().getActionMap().put(cancelActionKey, cancelAction);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                if (cancel.isEnabled()) {
                    cancelActionPerformed(null);
                }
            }
        });

        if (rui.isQuery()) {
            ContextAwareAction whereUsedAction = RefactoringActionsFactory.whereUsedAction();
            KeyStroke OKKS = (KeyStroke) whereUsedAction.getValue(Action.ACCELERATOR_KEY);
            String OKActionKey = "OK"; //NOI18N
            getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(OKKS, OKActionKey);
            Action OKAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    ParametersPanel.this.putClientProperty(JUMP_TO_FIRST_OCCURENCE, true);
                    refactor(null);
                }
            };
            getRootPane().getActionMap().put(OKActionKey, OKAction);
        }
    }

    boolean isCanceledDialog() {
        return canceledDialog;
    }

    private ProblemDetails getDetails(Problem problem) {
        if (problem.getNext() == null) {
            return problem.getDetails();
        }
        return null;
    }
    private ProblemDetails currentProblemAction;

    private void placeErrorPanel(Problem problem) {
        if (dialog == null) {
            //refactoring cancelled
            return;
        }
        showProblem(null);
        containerPanel.removeAll();
        errorPanel = new ErrorPanel(problem, rui);
        errorPanel.setBorder(new EmptyBorder(new Insets(12, 12, 11, 11)));
        containerPanel.add(errorPanel, BorderLayout.CENTER);

        next.setEnabled(!problem.isFatal() && !isPreviewRequired());
        dialog.getRootPane().setDefaultButton(isPreviewRequired() ? previewButton : next);
        next.setVisible(true);
        if (currentState == PRE_CHECK) {
            Mnemonics.setLocalizedText(next, NbBundle.getMessage(ParametersPanel.class, "CTL_Next"));
            back.setVisible(false);
            if (!rui.hasParameters()) {
                next.setVisible(false);
                previewButton.setVisible(true);
                previewButton.setEnabled(true);
            } else {
                next.setVisible(true);
                previewButton.setVisible(false);
                previewButton.setEnabled(false);
            }
        } else {
            ProblemDetails details = getDetails(problem);
            if (details != null) {
                Mnemonics.setLocalizedText(previewButton, details.getDetailsHint());
                previewButton.setVisible(true);
                previewButton.setEnabled(true);
                currentProblemAction = details;
            }
            back.setVisible(true);
            back.setEnabled(true);
            dialog.getRootPane().setDefaultButton(back);
        }
        cancel.setEnabled(true);
        previewButton.setEnabled(!problem.isFatal());
        if (progressHandle != null) {
            stop(new ProgressEvent(this, ProgressEvent.STOP));
        }
        ((BorderLayout)this.getLayout()).invalidateLayout(this);
        dialog.pack();
        if(next.isEnabled() && next.isVisible()) {
            next.requestFocusInWindow();
        } else if(previewButton.isEnabled() && previewButton.isVisible()) {
            previewButton.requestFocusInWindow();
        } else {
            cancel.requestFocusInWindow();
        }
        dialog.repaint();
    }

    private void placeCustomPanel() {
        if (dialog == null) {
            return;
        }
        if (customPanel == null) {
            return;
        }
        Mnemonics.setLocalizedText(next, NbBundle.getMessage(ParametersPanel.class, rui.isQuery() ? "CTL_Find" : "CTL_Finish"));
        Mnemonics.setLocalizedText(previewButton, NbBundle.getMessage(ParametersPanel.class, inspect ? "CTL_Inspect" : "CTL_PreviewAll"));
        customPanel.setBorder(new EmptyBorder(new Insets(12, 12, 11, 11)));
        containerPanel.removeAll();
        containerPanel.add(customPanel, BorderLayout.CENTER);
        back.setVisible(false);
        next.setVisible(!isPreviewRequired());
        previewButton.setVisible(!rui.isQuery());
        next.setEnabled(!isPreviewRequired());
        currentState = INPUT_PARAMETERS;
        setPanelEnabled(true);
        cancel.setEnabled(true);
        dialog.getRootPane().setDefaultButton(isPreviewRequired() ? previewButton : next);
        //Initial errors are ignored by on-line error checker
        //stateChanged(null);
        setOKorRefactor();
        ((BorderLayout)this.getLayout()).invalidateLayout(this);
        stop(new ProgressEvent(this, ProgressEvent.STOP));
        dialog.pack();
        if(!customPanel.requestFocusInWindow()) {
            if(previewButton.isEnabled() && previewButton.isVisible()) {
                previewButton.requestFocusInWindow();
            } else {
                next.requestFocusInWindow();
            }
        }
        dialog.repaint();
        stateChanged(null);
    }

    private boolean isPreviewRequired() {
        UI.Constants b = rui.getRefactoring().getContext().lookup(UI.Constants.class);
        return b != null && b == UI.Constants.REQUEST_PREVIEW;
    }
    private ProgressBar progressBar;
    private ProgressHandle progressHandle;
    private boolean isIndeterminate;

    /**
     * Implementation of ProgressListener.start method. Displays progress bar
     * and sets progress label and progress bar bounds.
     *
     * @param event Event object.
     */
    @Override
    public void start(final ProgressEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (dialog == null) return;
                if (progressBar != null && progressBar.isVisible()) {
                    LOGGER.log(Level.INFO, "{0} called start multiple times", event.getSource());
                    stop(event);
                }
                progressPanel.remove(innerPanel);
                progressBar = ProgressBar.create(progressHandle = ProgressHandleFactory.createHandle("")); //NOI18N
                if (event.getCount() == -1) {
                    isIndeterminate = true;
                    progressHandle.start();
                    progressHandle.switchToIndeterminate();
                } else {
                    isIndeterminate = false;
                    progressHandle.start(event.getCount());
                }

                String text;
                switch (event.getOperationType()) {
                    case AbstractRefactoring.PARAMETERS_CHECK:
                        text = NbBundle.getMessage(ParametersPanel.class, "LBL_ParametersCheck");
                        break;
                    case AbstractRefactoring.PREPARE:
                        text = NbBundle.getMessage(ParametersPanel.class, "LBL_Prepare");
                        break;
                    case AbstractRefactoring.PRE_CHECK:
                        text = NbBundle.getMessage(ParametersPanel.class, "LBL_PreCheck");
                        break;
                    default:
                        text = NbBundle.getMessage(ParametersPanel.class, "LBL_Usages");
                        break;
                }
                progressBar.setString(text);
                progressPanel.add(progressBar, BorderLayout.NORTH);
                progressPanel.setPreferredSize(null);
                if (dialog!=null)
                    dialog.validate();
                setButtonsEnabled(false);
            }
        });
    }

    /**
     * Implementation of ProgressListener.step method. Increments progress bar
     * value by 1.
     *
     * @param event Event object.
     */
    @Override
    public void step(final ProgressEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (progressHandle == null) {
                        return;
                    }

                    if (isIndeterminate && event.getCount() > 0) {
                        progressHandle.switchToDeterminate(event.getCount());
                        isIndeterminate = false;
                    } else {
                        progressHandle.progress(isIndeterminate ? -2 : event.getCount());
                    }
                } catch (Throwable e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        });
    }

    /**
     * Implementation of ProgressListener.stop method. Sets progress bar value
     * to its maximum.
     *
     * @param event Event object.
     */
    @Override
    public void stop(ProgressEvent event) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (progressHandle == null) {
                    return;
                }
                progressHandle.finish();
                progressPanel.remove(progressBar);
                progressPanel.add(innerPanel, BorderLayout.CENTER);
                //progressPanel.validate();
                //setButtonsEnabled(true); 
                //validate();
                progressHandle = null;
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            run.run();
        } else {
            SwingUtilities.invokeLater(run);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (!checkNeeded) {
            checkNeeded = true;
            SwingUtilities.invokeLater(this::recheck);
        }
    }
    
    private void recheck() {
        if (suppressChecks != 0) {
            checkNeeded = true;
            return;
        } else if (!checkNeeded) {
            return;
        }
        checkNeeded = false;
        if (rui instanceof RefactoringUIBypass && ((RefactoringUIBypass) rui).isRefactoringBypassRequired()) {
            showProblem(null);
        } else {
            showProblem(rui.checkParameters());
        }
        setOKorRefactor();
    }

    private void setOKorRefactor() {
        if (rui instanceof RefactoringUIBypass) {
            if (((RefactoringUIBypass) rui).isRefactoringBypassRequired()) {
                next.setText(NbBundle.getMessage(DialogDisplayer.class, "CTL_OK"));
                previewButton.setVisible(false);
            } else {
                Mnemonics.setLocalizedText(next, NbBundle.getMessage(ParametersPanel.class, rui.isQuery() ? "CTL_Find" : "CTL_Finish"));
                previewButton.setVisible(true);
            }
        }
    }

    private void showProblem(Problem problem) {
        if(dialog == null) {
            return;
        }
        if (problem == null) {
            label.setText(" "); // NOI18N
            innerPanel.setBorder(null);
            next.setEnabled(!isPreviewRequired());
            previewButton.setEnabled(true);
            return;
        }
        innerPanel.setBorder(new CompoundBorder(new javax.swing.border.LineBorder(java.awt.Color.gray),
                             new EmptyBorder(0, 2, 2, 2)));
        progressPanel.setVisible(true);
        if (problem.isFatal()) {
            displayError(problem.getMessage());
        } else {
            displayWarning(problem.getMessage());
        }
        dialog.pack();
    }

    private void displayError(String error) {
        next.setEnabled(false);
        previewButton.setEnabled(false);
        label.setText("<html><font color=\"red\">" + NbBundle.getMessage(ParametersPanel.class, "LBL_Error") + ": </font>" + error + "</html>"); //NOI18N
    }

    private void displayWarning(String warning) {
        next.setEnabled(!isPreviewRequired());
        previewButton.setEnabled(true);
        label.setText("<html><font color=\"red\">" + NbBundle.getMessage(ParametersPanel.class, "LBL_Warning") + ": </font>" + warning + "</html>"); //NOI18N
    }

    boolean isCreateNewTab() {
        return openInNewTab.isSelected();
    }

    private class Prepare implements Runnable {

        @Override
        public void run() {
            if (currentState != POST_CHECK && currentState != CHECK_PARAMETERS) {
                problem = rui.setParameters();
                if (problem != null && (problem.isFatal() || problem.getNext() != null) && currentState != POST_CHECK) {
                    currentState = CHECK_PARAMETERS;
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {
                            @Override
                            public void run() {
                                placeErrorPanel(problem);
                            }
                        });
                    } catch (Exception ie) {
                        throw new RuntimeException(ie);
                    }
                    return;
                }
            }

            try {
                final RefactoringSession refactoringSession = getResult();
                if (refactoringSession != null) {
                    problem=null;
                    if (rui.isQuery()) {
                        //run queries asynchronously
                        RP.post(new Runnable() {
                            @Override
                            public void run() {
                                    problem = rui.getRefactoring().prepare(refactoringSession);
                            }
                        });
                    } else {
                        problem = rui.getRefactoring().prepare(refactoringSession);
                    }
                }
            } catch (RuntimeException e) {
                setVisibleLater(false);
                throw e;
            }

            if (problem != null) {
                currentState = POST_CHECK;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        placeErrorPanel(problem);
                    }
                });
            }
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        next.setEnabled(enabled && !isPreviewRequired());
        //cancel.setEnabled(enabled);
        back.setEnabled(enabled);
        previewButton.setEnabled(enabled);
    }

    public HelpCtx getHelpCtx() {
        return rui.getHelpCtx();
    }

    private RefactoringSession getResult() {
        synchronized (RESULT_LOCK) {
            return result;
        }
    }

    private RefactoringSession putResult(RefactoringSession session) {
        synchronized (RESULT_LOCK) {
            this.result = session;
        }
        return session;
    }

    private static class ProgressBar extends JPanel {

        private JLabel label;

        private static ProgressBar create(ProgressHandle handle) {
            ProgressBar instance = new ProgressBar();
            instance.setLayout(new BorderLayout());
            instance.label = new JLabel(" "); //NOI18N
            instance.label.setBorder(new EmptyBorder(0, 0, 2, 0));
            instance.add(instance.label, BorderLayout.NORTH);
            JComponent progress = ProgressHandleFactory.createProgressComponent(handle);
            instance.add(progress, BorderLayout.CENTER);
            return instance;
        }

        public void setString(String value) {
            label.setText(value);
        }

        private ProgressBar() {
        }
    }
}
