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

package org.netbeans.modules.j2ee.deployment.impl.ui.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ui.RenamePanel;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.actions.CookieAction;

public class RenameAction extends CookieAction {
    
    protected void performAction(org.openide.nodes.Node[] nodes) {
        for (int i=0; i<nodes.length; i++) {
            ServerInstance instance = (ServerInstance) nodes[i].getCookie(ServerInstance.class);
            if (instance == null || instance.isRemoveForbidden()) {
                continue;
            }
            showRenameDialog(instance);
        }
    }
    
    protected boolean enable (Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            ServerInstance instance = (ServerInstance) nodes[i].getCookie(ServerInstance.class);
            if (instance == null || instance.isRemoveForbidden()) {
                return false;
            }
        }
        return true;
    }
    
    protected Class[] cookieClasses() {
        return new Class[] {
            ServerInstance.class 
        };
    }
    
    protected int mode() {
        return MODE_ALL;
    }

    @NbBundle.Messages("LBL_Rename=Re&name...")
    public String getName() {
        return Bundle.LBL_Rename();
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false; 
    }

    //XXX inspired in org.netbeans.modules.project.uiapi.DefaultProjectOperationsImplementation
    @NbBundle.Messages({
        "LBL_ConfirmButton=Rename",
        "MSG_RenameInstanceTitle=Rename Server Instance",
        "LBL_CancelButton=Cancel",
        "ACSD_CancelButton=Cancel Button",
        "ACSD_ConfirmButton=Rename Button"
    })
    private static void showRenameDialog(final ServerInstance instance) {
        final RenamePanel panel = new RenamePanel(instance.getDisplayName());
        final JButton confirm = new JButton(Bundle.LBL_ConfirmButton());
        final JButton cancel = new JButton(Bundle.LBL_CancelButton());

        confirm.getAccessibleContext().setAccessibleDescription(Bundle.ACSD_ConfirmButton());
        cancel.getAccessibleContext().setAccessibleDescription(Bundle.ACSD_CancelButton());

        panel.addChangeListener(new ChangeListener() {
            public @Override void stateChanged(ChangeEvent e) {
                confirm.setEnabled(panel.isPanelValid());
            }
        });

        confirm.setEnabled(panel.isPanelValid());

        final Dialog[] dialog = new Dialog[1];

        DialogDescriptor dd = new DialogDescriptor(wrapPanel(panel), Bundle.MSG_RenameInstanceTitle(),
                true, new Object[] {confirm, cancel}, confirm,
                DialogDescriptor.DEFAULT_ALIGN, null, new ActionListener() {
            private boolean operationRunning;
            public @Override void actionPerformed(ActionEvent e) {
                //#65634: making sure that the user cannot close the dialog before the operation is finished:
                if (operationRunning) {
                    return ;
                }

                if (dialog[0] instanceof JDialog) {
                    ((JDialog) dialog[0]).getRootPane().getInputMap(
                            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
                    ((JDialog) dialog[0]).setDefaultCloseOperation(
                            JDialog.DO_NOTHING_ON_CLOSE);
                }

                operationRunning = true;

                if (e.getSource() == confirm) {
                    instance.getInstanceProperties().setProperty(
                            InstanceProperties.DISPLAY_NAME_ATTR, panel.getServerName());
                }
                dialog[0].setVisible(false);
            }
        });

        dd.setClosingOptions(new Object[0]);

        dialog[0] = DialogDisplayer.getDefault().createDialog(dd);

        dialog[0].setVisible(true);

        dialog[0].dispose();
        dialog[0] = null;
    }

    private static JComponent wrapPanel(JComponent component) {
        component.setBorder(new EmptyBorder(12, 12, 12, 12));

        return component;
    }

}
