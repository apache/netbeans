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
package org.netbeans.modules.php.composer.ui.actions;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.composer.commands.Composer;
import org.netbeans.modules.php.composer.output.model.ComposerPackage;
import org.netbeans.modules.php.composer.ui.DependenciesPanel;
import org.netbeans.modules.php.composer.ui.options.ComposerOptionsPanelController;
import org.netbeans.modules.php.composer.util.ComposerUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

final class AddDependencyPanel extends JPanel {

    private static final long serialVersionUID = -546689998137L;

    // @GuardedBy("EDT")
    private static boolean keepOpened = true;

    private final PhpModule phpModule;
    private final DependenciesPanel dependenciesPanel;


    private AddDependencyPanel(PhpModule phpModule) {
        assert EventQueue.isDispatchThread();
        assert phpModule != null;

        this.phpModule = phpModule;
        dependenciesPanel = DependenciesPanel.create(phpModule);

        initComponents();
        init();
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "AddDependencyPanel.panel.title=Composer Packages ({0})",
    })
    public static void open(PhpModule phpModule) {
        assert EventQueue.isDispatchThread();
        assert phpModule != null;

        final AddDependencyPanel addDependencyPanel = new AddDependencyPanel(phpModule);
        Object[] options = new Object[] {
            addDependencyPanel.requireButton,
            addDependencyPanel.requireDevButton,
            DialogDescriptor.CANCEL_OPTION,
        };

        final DialogDescriptor descriptor = new DialogDescriptor(
                addDependencyPanel,
                Bundle.AddDependencyPanel_panel_title(phpModule.getDisplayName()),
                false,
                options,
                addDependencyPanel.requireButton,
                DialogDescriptor.DEFAULT_ALIGN, null, null);
        descriptor.setClosingOptions(new Object[] {DialogDescriptor.CANCEL_OPTION});
        descriptor.setAdditionalOptions(new Object[] {addDependencyPanel.keepOpenCheckBox});
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        addDependencyPanel.dependenciesPanel.setDefaultAction(dialog, new Runnable() {
            @Override
            public void run() {
                if (addDependencyPanel.requireButton.isEnabled()) {
                    addDependencyPanel.requireButton.doClick();
                }
            }
        });
        handleKeepOpen(dialog, addDependencyPanel);
        dialog.setVisible(true);
    }

    private static void handleKeepOpen(final Dialog dialog, final AddDependencyPanel addDependencyPanel) {
        ActionListener keepOpenActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!addDependencyPanel.keepOpenCheckBox.isSelected()) {
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            }
        };
        addDependencyPanel.requireButton.addActionListener(keepOpenActionListener);
        addDependencyPanel.requireDevButton.addActionListener(keepOpenActionListener);
    }

    private void init() {
        innerPanel.add(dependenciesPanel, BorderLayout.CENTER);
        initActionButons();
        // listeners
        dependenciesPanel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                enableRequireButtons();
            }
        });
    }

    private void initActionButons() {
        assert EventQueue.isDispatchThread();
        // require buttons
        enableRequireButtons();
        // keep opened checkbox
        keepOpenCheckBox.setSelected(keepOpened);
        // listeners
        keepOpenCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                assert EventQueue.isDispatchThread();
                keepOpened = e.getStateChange() == ItemEvent.SELECTED;
            }
        });
    }


    void enableRequireButtons() {
        assert EventQueue.isDispatchThread();
        boolean validResultSelected = dependenciesPanel.getComposerPackage() != null;
        requireButton.setEnabled(validResultSelected);
        requireDevButton.setEnabled(validResultSelected);
    }


    @NbBundle.Messages("AddDependencyPanel.error.composer.notValid=Composer is not valid.")
    @CheckForNull
    Composer getComposer() {
        try {
            return Composer.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            UiUtils.invalidScriptProvided(Bundle.AddDependencyPanel_error_composer_notValid(), ComposerOptionsPanelController.OPTIONS_SUBPATH);
        }
        return null;
    }

    private void initComposer(Composer composer, Runnable postTask) {
        assert phpModule != null;
        Future<Integer> task = composer.initIfNotPresent(phpModule);
        if (task == null) {
            // file exists already
            postTask.run();
            return;
        }
        runWhenTaskFinish(task, postTask, null);
    }

    void runWhenTaskFinish(Future<Integer> task, @NullAllowed Runnable postTask, @NullAllowed Runnable finalTask) {
        try {
            task.get(3, TimeUnit.MINUTES);
            if (postTask != null) {
                postTask.run();
            }
        } catch (TimeoutException ex) {
            task.cancel(true);
        } catch (CancellationException ex) {
            // noop, dialog is being closed
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            UiUtils.processExecutionException(ex, ComposerOptionsPanelController.OPTIONS_SUBPATH);
        } finally {
            if (finalTask != null) {
                finalTask.run();
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        requireButton = new JButton();
        requireDevButton = new JButton();
        keepOpenCheckBox = new JCheckBox();
        innerPanel = new JPanel();

        Mnemonics.setLocalizedText(requireButton, NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.requireButton.text")); // NOI18N
        requireButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                requireButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(requireDevButton, NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.requireDevButton.text")); // NOI18N
        requireDevButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                requireDevButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(keepOpenCheckBox, NbBundle.getMessage(AddDependencyPanel.class, "AddDependencyPanel.keepOpenCheckBox.text")); // NOI18N

        innerPanel.setLayout(new BorderLayout());

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(innerPanel, GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(innerPanel, GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void requireButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_requireButtonActionPerformed
        assert EventQueue.isDispatchThread();
        final Composer composer = getComposer();
        if (composer == null) {
            return;
        }
        final ComposerPackage composerPackage = dependenciesPanel.getComposerPackage();
        assert composerPackage != null;
        initComposer(composer, new Runnable() {
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                assert phpModule != null;
                ComposerUtils.logUsageComposerRequire();
                composer.require(phpModule, composerPackage.asFullPackage());
            }
        });
    }//GEN-LAST:event_requireButtonActionPerformed

    private void requireDevButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_requireDevButtonActionPerformed
        assert EventQueue.isDispatchThread();
        final Composer composer = getComposer();
        if (composer == null) {
            return;
        }
        final ComposerPackage composerPackage = dependenciesPanel.getComposerPackage();
        initComposer(composer, new Runnable() {
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                assert phpModule != null;
                ComposerUtils.logUsageComposerRequire();
                composer.requireDev(phpModule, composerPackage.asFullPackage());
            }
        });
    }//GEN-LAST:event_requireDevButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel innerPanel;
    private JCheckBox keepOpenCheckBox;
    private JButton requireButton;
    private JButton requireDevButton;
    // End of variables declaration//GEN-END:variables
}
