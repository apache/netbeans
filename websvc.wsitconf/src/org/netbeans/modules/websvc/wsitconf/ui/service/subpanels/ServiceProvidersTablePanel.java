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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.netbeans.modules.websvc.wsitconf.ui.service.subpanels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.websvc.wsitconf.ui.security.listmodels.ServiceProviderElement;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.STSConfiguration;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.ServiceProvider;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Martin Grebac
 */
public class ServiceProvidersTablePanel extends DefaultTablePanel {

    private static final String[] columnName = {NbBundle.getMessage(ServiceProvidersTablePanel.class, 
            "MSG_ServiceProviders")                                     // NOI18N
    };
    
    private ServiceProvidersTableModel tablemodel;
    private STSConfiguration stsConfig;
    
    private Map<String, ServiceProviderElement> addedProviders;
    private RemoveActionListener removeActionListener;
    private AddActionListener addActionListener;
    private EditActionListener editActionListener;

    private ConfigVersion cfgVersion;
    
    /**
     * Creates a new instance of ServiceProvidersTablePanel
     */
    public ServiceProvidersTablePanel(ServiceProvidersTableModel tablemodel, STSConfiguration stsConfig, ConfigVersion cfgVersion) {
        super(tablemodel);
        this.stsConfig = stsConfig;
        this.tablemodel = tablemodel;
        this.cfgVersion = cfgVersion;
        
        this.editButton.setVisible(true);

        addedProviders = new HashMap<String, ServiceProviderElement>();
        
        editActionListener = new EditActionListener();
        ActionListener editListener = WeakListeners.create(ActionListener.class,
                editActionListener, editButton);
        editButton.addActionListener(editListener);

        addActionListener = new AddActionListener();
        ActionListener addListener = WeakListeners.create(ActionListener.class,
                addActionListener, addButton);
        addButton.addActionListener(addListener);
        
        removeActionListener = new RemoveActionListener();
        ActionListener removeListener = WeakListeners.create(ActionListener.class,
                removeActionListener, removeButton);
        removeButton.addActionListener(removeListener);
    }

    public List getChildren(){
        return tablemodel.getChildren();
    }
    
    class RemoveActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            int row = getTable().getSelectedRow();
            if(row == -1) return;
            ServiceProviderElement spe = (ServiceProviderElement)getTable().getValueAt(row, 0);
            if (confirmDeletion(spe)) {
                addedProviders.remove(spe);
                ServiceProvidersTablePanel.this.tablemodel.removeRow(row);
                ProprietarySecurityPolicyModelHelper.removeSTSServiceProvider(stsConfig, spe);
            }
        }
        
        private boolean confirmDeletion(ServiceProviderElement spe) {
            NotifyDescriptor.Confirmation notifyDesc =
                    new NotifyDescriptor.Confirmation(NbBundle.getMessage
                    (ServiceProvidersTablePanel.class, "MSG_ServiceProviderConfirmDelete", spe.getEndpoint()),  //NOI18N
                    NotifyDescriptor.YES_NO_OPTION);
            DialogDisplayer.getDefault().notify(notifyDesc);
            return (notifyDesc.getValue() == NotifyDescriptor.YES_OPTION);
        }
    }
    
    class AddActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {

            ServiceProviderSelectorPanel spPanel = new ServiceProviderSelectorPanel(null, null, null, null);

            DialogDescriptor dd = new DialogDescriptor(
                    spPanel, 
                    NbBundle.getMessage(ServiceProvidersTablePanel.class, "LBL_SelectSProvider_Title"),  //NOI18N
                    true, 
                    DialogDescriptor.OK_CANCEL_OPTION, 
                    DialogDescriptor.CANCEL_OPTION, 
                    DialogDescriptor.DEFAULT_ALIGN,
                    new HelpCtx(ServiceProviderSelectorPanel.class),
                    null);

            if (DialogDisplayer.getDefault().notify(dd).equals(DialogDescriptor.OK_OPTION)) {
                if (spPanel != null) {
                    String url = spPanel.getSpUrl();
                    String alias = spPanel.getCertAlias();
                    String ttype = spPanel.getTokenType();
                    String ktype = spPanel.getKeyType();
                    ServiceProviderElement spe = new ServiceProviderElement(url, alias, ttype, ktype);
                    addedProviders.put(url, spe);
                    ServiceProvidersTablePanel.this.tablemodel.addRow(spe);
                    ProprietarySecurityPolicyModelHelper.addSTSServiceProvider(stsConfig, spe, cfgVersion);
                }
            }
        }
    }
    
    class EditActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            int row = getTable().getSelectedRow();
            if (row == -1) return;
            ServiceProviderElement speOld = (ServiceProviderElement)getTable().getValueAt(row, 0);
            ServiceProviderSelectorPanel spPanel = new ServiceProviderSelectorPanel(speOld.getEndpoint(), 
                    speOld.getCertAlias(), 
                    speOld.getTokenType(), 
                    speOld.getKeyType());
            
            DialogDescriptor dd = new DialogDescriptor(
                    spPanel, 
                    NbBundle.getMessage(ServiceProvidersTablePanel.class, "LBL_SelectSProvider_Title"),  //NOI18N
                    true, 
                    DialogDescriptor.OK_CANCEL_OPTION, 
                    DialogDescriptor.CANCEL_OPTION, 
                    DialogDescriptor.DEFAULT_ALIGN,
                    new HelpCtx(ServiceProviderSelectorPanel.class),
                    null);

            if (DialogDisplayer.getDefault().notify(dd).equals(DialogDescriptor.OK_OPTION)) {
                if (spPanel != null) {
                    String url = spPanel.getSpUrl();
                    String alias = spPanel.getCertAlias();
                    String ttype = spPanel.getTokenType();
                    String ktype = spPanel.getKeyType();
                    ServiceProviderElement speNew = new ServiceProviderElement(url, alias, ttype, ktype);
                    addedProviders.put(url, speNew);
                    ServiceProvidersTablePanel.this.tablemodel.removeRow(row);
                    ServiceProvidersTablePanel.this.tablemodel.addRow(speNew);
                    ProprietarySecurityPolicyModelHelper.removeSTSServiceProvider(stsConfig, speNew);
                    ProprietarySecurityPolicyModelHelper.addSTSServiceProvider(stsConfig, speNew, cfgVersion);
                }
            }
        }
    }
    
    public void populateModel(){
        tablemodel.setData(stsConfig);
    }
    
    public static class ServiceProvidersTableModel extends AbstractTableModel {
        
        List<ServiceProviderElement> children;
        
        public Object getValueAt(int row, int column) {
            return children.get(row);
        }
        
        public int getRowCount() {
            if(children != null){
                return children.size();
            }
            return 0;
        }
        
        public int getColumnCount() {
            return columnName.length;
        }
        
        public void removeRow(int row){
            children.remove(row);
            fireTableRowsDeleted(row, row);
        }
        
        public void addRow(ServiceProviderElement value){
            children.add(value);
            fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
        }
        
        public void setData(STSConfiguration stsConfig) {
            
            children = new ArrayList<ServiceProviderElement>();            
            List<ServiceProvider> spList = ProprietarySecurityPolicyModelHelper.getSTSServiceProviders(stsConfig);
            
            if ((spList != null) && !(spList.isEmpty())) {
                for (ServiceProvider sp : spList) {
                    String endpoint = sp.getEndpoint();
                    String certAlias = ProprietarySecurityPolicyModelHelper.getSPCertAlias(sp);
                    String tokenType = ProprietarySecurityPolicyModelHelper.getSPTokenType(sp);
                    String keyType = ProprietarySecurityPolicyModelHelper.getSPKeyType(sp);
                    ServiceProviderElement spe = new ServiceProviderElement(endpoint, certAlias, tokenType, keyType);
                    children.add(spe);
                }
                this.fireTableDataChanged(); //do we need to do this?
            }
        }
        
        @Override
        public String getColumnName(int column) {
            return columnName[column];
        }
        
        public List getChildren(){
            return children;
        }
    }
}
