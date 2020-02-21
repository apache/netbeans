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
package org.netbeans.modules.remote.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsPanelSupport;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 */
public class ShowToolchainsAction extends AbstractAction {

    private final ExecutionEnvironment env;
    private final CompilerSet compilerSet;

    public ShowToolchainsAction(ExecutionEnvironment env, CompilerSet compilerSet) {
        super(NbBundle.getMessage(ToolchainListRootNode.class, "PropertiesMenuItem"));
        this.env = env;
        this.compilerSet = compilerSet;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //OptionsDisplayer.getDefault().open(CndUIConstants.TOOLS_OPTIONS_CND_TOOLS_PATH);
        JComponent tpc = ToolsPanelSupport.getToolsPanelComponent(env, compilerSet == null ? null : compilerSet.getName());
        String title = NbBundle.getMessage(ToolchainListRootNode.class, "CompilerSetPropertieesDlgTitile",
                ServerList.get(env).getDisplayName());
        tpc.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        DialogDescriptor dd = new DialogDescriptor(tpc, title);
        dd.setModal(true);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.pack();
        try {
            dlg.setVisible(true);
        } catch (Throwable th) {
            if (!(th.getCause() instanceof InterruptedException)) {
                throw new RuntimeException(th);
            }
            dd.setValue(DialogDescriptor.CANCEL_OPTION);
        } finally {
            dlg.dispose();
        }
        if (dd.getValue() == NotifyDescriptor.OK_OPTION) {
            VetoableChangeListener okL = (VetoableChangeListener) tpc.getClientProperty(ToolsPanelSupport.OK_LISTENER_KEY);
            CndUtils.assertNotNull(okL, "VetoableChangeListener shouldn't be null"); //NOI18N
            if (okL != null) {
                try {
                    okL.vetoableChange(null);
                } catch (PropertyVetoException ex) {
                }
            }
        }
    }
}
