/*
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
