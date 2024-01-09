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

package org.netbeans.modules.hudson.ui.wizard;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.netbeans.modules.hudson.api.HudsonVersion;
import org.netbeans.modules.hudson.api.Utilities;
import org.netbeans.modules.hudson.ui.api.UI;
import org.netbeans.modules.hudson.ui.util.UsageLogging;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class InstanceDialog extends DialogDescriptor {

    private static final Logger LOG = Logger.getLogger(InstanceDialog.class.getName());

    private final InstancePropertiesVisual panel;
    private final JButton addButton;
    private Dialog dialog;
    private HudsonInstance created;
    
    public InstanceDialog() {
        this(new InstancePropertiesVisual());
    }
    private InstanceDialog(InstancePropertiesVisual panel) {
        super(panel, NbBundle.getMessage(InstanceDialog.class, "LBL_InstanceWiz_Title"));
        this.panel = panel;
        addButton = new JButton(NbBundle.getMessage(InstanceDialog.class, "InstanceDialog.add"));
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tryToAdd();
            }
        });
        panel.init(createNotificationLineSupport(), addButton);
        setOptions(new Object[] {addButton, NotifyDescriptor.CANCEL_OPTION});
        setClosingOptions(new Object[] {NotifyDescriptor.CANCEL_OPTION});
    }

    public HudsonInstance show() {
        dialog = DialogDisplayer.getDefault().createDialog(this);
        dialog.setVisible(true);
        LOG.log(Level.FINE, "Added Jenkins instance: {0}", created);
        return created;
    }

    @NbBundle.Messages({
        "# UI logging of adding new server",
        "UI_HUDSON_SERVER_REGISTERED=Jenkins server registered",
        "# Usage Logging",
        "USG_HUDSON_SERVER_REGISTERED=Jenkins server registered"
    })
    private void tryToAdd() {
        addButton.setEnabled(false);
        panel.showChecking();
        dialog.pack();
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                Utilities.HudsonURLCheckResult checkResult
                        = Utilities.checkHudsonURL(panel.getUrl());

                switch (checkResult) {
                    case OK:
                        break;
                    case WRONG_VERSION:
                        problem(NbBundle.getMessage(InstanceDialog.class,
                                "MSG_WrongVersion", //NOI18N
                                HudsonVersion.SUPPORTED_VERSION));
                        return;
                    case INCORRECT_REDIRECTS:
                        problem(NbBundle.getMessage(InstanceDialog.class,
                                "MSG_incorrect_redirects")); //NOI18N
                        return;
                    default:
                        problem(NbBundle.getMessage(InstanceDialog.class,
                                "MSG_FailedToConnect")); //NOI18N
                        return;
                }
                created = HudsonManager.addInstance(panel.getDisplayName(),
                        panel.getUrl(), panel.getSyncTime(), true);

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dispose();
                        UI.selectNode(panel.getUrl());
                    }
                });
                // stats
                UsageLogging.logUI(NbBundle.getBundle(InstanceDialog.class),
                        "UI_HUDSON_SERVER_REGISTERED");                 //NOI18N
                UsageLogging.logUsage(InstanceDialog.class,
                        "USG_HUDSON_SERVER_REGISTERED");                //NOI18N
            }
            private void problem(final String explanation) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        addButton.setEnabled(true);
                        panel.checkFailed(explanation);
                        dialog.pack();
                    }
                });
            }
        });
    }

}
