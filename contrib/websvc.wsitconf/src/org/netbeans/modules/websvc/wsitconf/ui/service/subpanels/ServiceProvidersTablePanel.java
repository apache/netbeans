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
