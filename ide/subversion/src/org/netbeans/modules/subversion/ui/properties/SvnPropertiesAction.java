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

package org.netbeans.modules.subversion.ui.properties;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * 
 * @author Peter Pis
 */
public final class SvnPropertiesAction extends ContextAction {

    private static final String ICON_RESOURCE = "org/netbeans/modules/subversion/resources/icons/properties.png"; //NOI18N

    public SvnPropertiesAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    @Override
    protected boolean enable(Node[] nodes) {
        return super.enable(nodes);
    }
    
    @Override
    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_VERSIONED 
             | FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }

    @Override
    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(SvnPropertiesAction.class, "CTL_PropertiesAction");      // NOI18N
    }

    @Override
    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_MenuItem_Properties";   // NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {       
        final Context ctx = getContext(nodes);
        String ctxDisplayName = getContextDisplayName(nodes);       
        File[] roots = ctx.getRootFiles();
        if(roots == null || roots.length == 0) {
            return;
        }
        openProperties(roots, ctxDisplayName);
    }

    public static void openProperties(File[] roots, String ctxDisplayName) {
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }       

        final PropertiesPanel panel = new PropertiesPanel();
        final PropertiesTable propTable;
        propTable = new PropertiesTable(panel.labelForTable, PropertiesTable.PROPERTIES_COLUMNS, new String[] { PropertiesTableModel.COLUMN_NAME_VALUE});
        panel.setPropertiesTable(propTable);

        JComponent component = propTable.getComponent();
        panel.propsPanel.setLayout(new BorderLayout());
        panel.propsPanel.add(component, BorderLayout.CENTER);
        SvnProperties svnProperties = new SvnProperties(panel, propTable, roots);
        JButton btnClose = new JButton();
        Mnemonics.setLocalizedText(btnClose, getString("CTL_Properties_Action_Close"));   //NOI18N
        btnClose.getAccessibleContext().setAccessibleDescription(getString("CTL_Properties_Action_Close")); //NOI18N
        btnClose.getAccessibleContext().setAccessibleName(getString("CTL_Properties_Action_Close"));    //NOI18N

        DialogDescriptor dd = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(SvnPropertiesAction.class, "CTL_PropertiesDialog_Title", ctxDisplayName)); // NOI18N
        dd.setModal(true);
        dd.setOptions(new Object[] {btnClose});
        dd.setHelpCtx(new HelpCtx(SvnPropertiesAction.class));

        panel.putClientProperty("contentTitle", ctxDisplayName);  // NOI18N
        panel.putClientProperty("DialogDescriptor", dd); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SvnPropertiesAction.class, "CTL_PropertiesAction")); // NOI18N
        dialog.pack();
        dialog.setVisible(true);        
    }
    
    private static String getString(String msgKey) {
        return NbBundle.getMessage(SvnPropertiesAction.class, msgKey);
    }

}
