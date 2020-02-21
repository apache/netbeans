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

package org.netbeans.modules.cnd.api.toolchain.ui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.toolchain.ui.options.DownloadUtils;
import org.netbeans.modules.cnd.toolchain.ui.options.ToolsPanel;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 * Post the ToolsPanel as a standalone dialog.
 *
 */
public class BuildToolsAction extends CallableSystemAction implements PropertyChangeListener {
    
    private String title;
    private String name;
    private JButton jOK = null;
    private ToolsPanel tp;
    
    public BuildToolsAction() {
        name = NbBundle.getMessage(BuildToolsAction.class, "LBL_BuildToolsName"); // NOI18N
        title = NbBundle.getMessage(BuildToolsAction.class, "LBL_BuildToolsTitle"); // NOI18N
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    @Override
    public void performAction() {
        initBuildTools(new LocalToolsPanelModel(), new ArrayList<String>(), null);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(ToolsPanel.PROP_VALID) &&
                ev.getSource() instanceof ToolsPanel) {
            jOK.setEnabled(((Boolean) ev.getNewValue()));
        }
    }
    
    /**
     * Initialize the build tools
     *
     * @returns true if the user pressed OK, false if Cancel
     */
    public boolean initBuildTools(ToolsPanelModel model, ArrayList<String> errs, CompilerSet cs) {
        if (downloadIfNeed(model, cs)){
            return true;
        }
        tp = new ToolsPanel(model, "ResolveBuildTools"); // NOI18N
        tp.addPropertyChangeListener(this);
        jOK = new JButton(NbBundle.getMessage(BuildToolsAction.class, "BTN_OK")); // NOI18N
        tp.setPreferredSize(new Dimension(640, 450));
        tp.update(errs);
        DialogDescriptor dd = new DialogDescriptor((Object) constructOuterPanel(tp), getTitle(), true, 
                new Object[] { jOK, DialogDescriptor.CANCEL_OPTION},
                DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, HelpCtx.findHelp(tp), null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);

        try {
            dialog.setVisible(true);
        } catch (Throwable th) {
            if (!(th.getCause() instanceof InterruptedException)) {
                throw new RuntimeException(th);
            }
            dd.setValue(DialogDescriptor.CLOSED_OPTION);
        } finally {
            dialog.dispose();
        }
        
        if (dd.getValue() == jOK) {
            tp.applyChanges(true);
            return true;
        }
        return false;
    }

    private boolean downloadIfNeed(ToolsPanelModel model, CompilerSet cs){
        ExecutionEnvironment env = model.getSelectedDevelopmentHost();
        if (env.isLocal()){
            if (cs == null) {
                cs = CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal()).getDefaultCompilerSet();
            }
            if (cs != null) {
                if (cs.isUrlPointer()){
                    // Can be downloaded
                    return DownloadUtils.showDownloadConfirmation(cs);
                }
            }
        }
        return false;
    }

    private JPanel constructOuterPanel(JPanel innerPanel) {
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(16, 16, 16, 16);
        panel.add(innerPanel, gridBagConstraints);
        return panel;
    }
    
    @Override
    public void actionPerformed(ActionEvent ev) {
        performAction();
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
