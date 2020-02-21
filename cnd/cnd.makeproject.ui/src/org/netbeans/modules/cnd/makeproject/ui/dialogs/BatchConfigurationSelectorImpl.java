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
package org.netbeans.modules.cnd.makeproject.ui.dialogs;

import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import javax.swing.JButton;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.ui.utils.ConfSelectorPanel;
import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport.BatchConfigurationSelector;
import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport.BatchConfigurationSelectorFactory;
import static org.netbeans.spi.project.ActionProvider.COMMAND_BUILD;
import static org.netbeans.spi.project.ActionProvider.COMMAND_CLEAN;
import static org.netbeans.spi.project.ActionProvider.COMMAND_REBUILD;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 */
public class BatchConfigurationSelectorImpl implements BatchConfigurationSelector, ActionListener {
    @org.openide.util.lookup.ServiceProvider(service = BatchConfigurationSelectorFactory.class)
    public static final class BatchConfigurationSelectorFactoryImpl implements BatchConfigurationSelectorFactory {

        @Override
        public BatchConfigurationSelector create(MakeProject project, Configuration[] confs) {
            BatchConfigurationSelector batchConfigurationSelector = new BatchConfigurationSelectorImpl(project, confs);
            String batchCommand = batchConfigurationSelector.getCommand();
            Configuration[] confsArray = batchConfigurationSelector.getSelectedConfs();
            if (batchCommand == null || confsArray == null || confsArray.length == 0) {
                return null;
            }
            return batchConfigurationSelector;
        }
    }

    private JButton buildButton = new JButton(getString("BuildButton"));
    private JButton rebuildButton = new JButton(getString("CleanBuildButton"));
    private JButton cleanButton = new JButton(getString("CleanButton"));
    private JButton closeButton = new JButton(getString("CloseButton"));
    private ConfSelectorPanel confSelectorPanel;
    private String command = null;
    private Dialog dialog = null;
    private final String recentSelectionKey;

    private BatchConfigurationSelectorImpl(MakeProject project, Configuration[] confs) {
        confSelectorPanel = new ConfSelectorPanel(getString("CheckLabel"), getString("CheckLabelMn").charAt(0), confs, new JButton[]{buildButton, rebuildButton, cleanButton});
        recentSelectionKey = project.getProjectDirectory().getPath();
        confSelectorPanel.restoreSelection(recentSelectionKey);

        String dialogTitle = MessageFormat.format(getString("BatchBuildTitle"), // NOI18N
                new Object[]{ProjectUtils.getInformation(project).getDisplayName()});

        buildButton.setMnemonic(getString("BuildButtonMn").charAt(0));
        buildButton.getAccessibleContext().setAccessibleDescription(getString("BuildButtonAD"));
        buildButton.addActionListener(this);
        rebuildButton.setMnemonic(getString("CleanBuildButtonMn").charAt(0));
        rebuildButton.addActionListener(this);
        rebuildButton.getAccessibleContext().setAccessibleDescription(getString("CleanBuildButtonAD"));
        cleanButton.setMnemonic(getString("CleanButtonMn").charAt(0));
        cleanButton.addActionListener(this);
        cleanButton.getAccessibleContext().setAccessibleDescription(getString("CleanButtonAD"));
        closeButton.getAccessibleContext().setAccessibleDescription(getString("CloseButtonAD"));
        // Show the dialog
        DialogDescriptor dd = new DialogDescriptor(confSelectorPanel, dialogTitle, true, new Object[]{closeButton}, closeButton, 0, null, null);
        //DialogDisplayer.getDefault().notify(dd);
        dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.getAccessibleContext().setAccessibleDescription(getString("BatchBuildDialogAD"));

        try {
            dialog.setVisible(true);
        } catch (Throwable th) {
            if (!(th.getCause() instanceof InterruptedException)) {
                throw new RuntimeException(th);
            }
            dd.setValue(closeButton);
        } finally {
            dialog.setVisible(false);
        }
    }

    @Override
    public Configuration[] getSelectedConfs() {
        return confSelectorPanel.getSelectedConfs();
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == buildButton) {
            command = COMMAND_BUILD;
        } else if (evt.getSource() == rebuildButton) {
            command = COMMAND_REBUILD;
        } else if (evt.getSource() == cleanButton) {
            command = COMMAND_CLEAN;
        } else {
            assert false;
        }
        dialog.dispose();
        confSelectorPanel.storeSelection(recentSelectionKey);
    }
    
    private static String getString(String s) {
        return NbBundle.getMessage(BatchConfigurationSelectorImpl.class, s);
    }
}
