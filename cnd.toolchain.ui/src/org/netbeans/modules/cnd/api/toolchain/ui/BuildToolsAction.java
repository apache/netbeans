/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
