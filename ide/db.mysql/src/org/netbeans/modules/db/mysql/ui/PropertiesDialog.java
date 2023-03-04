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

package org.netbeans.modules.db.mysql.ui;

import java.awt.Dialog;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.impl.ServerNodeProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author David
 */
public class PropertiesDialog  {
    private static final Logger LOGGER = Logger.getLogger(PropertiesDialog.class.getName());
    private static final String HELP_CTX = PropertiesDialog.class.getName();

    public enum Tab { BASIC, ADMIN };
    
    private final JTabbedPane tabbedPane;
    private final BasePropertiesPanel basePanel;
    private final AdminPropertiesPanel adminPanel;
    private final DatabaseServer server;

    public PropertiesDialog(DatabaseServer server) {
        this.server = server;
        
        basePanel = new BasePropertiesPanel(server);
        adminPanel = new AdminPropertiesPanel(server);
        
        tabbedPane = createTabbedPane(basePanel, adminPanel);
    }
    
    /**
     * Display the properties dialog
     * 
     * @return true if the user confirmed changes, false if they canceled
     */
    public boolean displayDialog() {
        return displayDialog(Tab.BASIC);
    }

    /**
     * Display the properties dialog, choosing which tab you want to have
     * initial focus
     * 
     * @param focusTab
     * @return true if the user confirmed changes, false if they canceled
     */
    public boolean displayDialog(Tab focusTab) {
        DialogDescriptor descriptor = createDialogDescriptor();
        if ( focusTab == Tab.ADMIN ) {
            tabbedPane.setSelectedIndex(1);
        }
        
        boolean ok = displayDialog(descriptor);
        
        if ( ok ) {
            updateServer();
        }     
        
        return ok;
    }
    
    private static JTabbedPane createTabbedPane(JPanel basePanel, JPanel adminPanel) {
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        
        tabbedPane.addTab(
            getMessage("PropertiesDialog.BasePanelTitle"),
            /* icon */ null, basePanel,
            getMessage("PropertiesDialog.BasePanelHint"));
                
        tabbedPane.addTab(
            getMessage("PropertiesDialog.AdminPanelTitle"),
            /* icon */ null, adminPanel,
            getMessage("PropertiesDialog.AdminPanelHint"));
        
        tabbedPane.getAccessibleContext().setAccessibleName(
                getMessage("PropertiesDialog.ACS_Name"));
        tabbedPane.getAccessibleContext().setAccessibleDescription(
                getMessage("PropertiesDialog.ACS_Desc"));

        
        return tabbedPane;
    }
    
    private DialogDescriptor createDialogDescriptor() {
        DialogDescriptor descriptor = new DialogDescriptor(
                tabbedPane, 
                getMessage("PropertiesDialog.Title"));
        descriptor.setHelpCtx(new HelpCtx(HELP_CTX));
        
        basePanel.setDialogDescriptor(descriptor);
        adminPanel.setDialogDescriptor(descriptor);

        return descriptor;
    }

    private boolean displayDialog(DialogDescriptor descriptor) {
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        dialog.dispose();
        return DialogDescriptor.OK_OPTION.equals(descriptor.getValue());
    }

    private void updateServer() {
        if (! basePanel.getHost().equals(server.getHost())) {
            server.setHost(basePanel.getHost());
        }
        if (! basePanel.getPort().equals(server.getPort())) {
            server.setPort(basePanel.getPort());
        }
        if (! basePanel.getUser().equals(server.getUser())) {
            server.setUser(basePanel.getUser());
        }
        if (! basePanel.getPassword().equals(server.getPassword())) {
            server.setPassword(basePanel.getPassword());
        }
        if (basePanel.getSavePassword() != server.isSavePassword()) {
            server.setSavePassword(basePanel.getSavePassword());
        }

        if (! adminPanel.getAdminPath().equals(server.getAdminPath())) {
            server.setAdminPath(adminPanel.getAdminPath());
        }
        if (! adminPanel.getAdminArgs().equals(server.getAdminArgs())) {
            server.setAdminArgs(adminPanel.getAdminArgs());
        }
        if (! adminPanel.getStartPath().equals(server.getStartPath())) {
            server.setStartPath(adminPanel.getStartPath());
        }
        if (! adminPanel.getStartArgs().equals(server.getStartArgs())) {
            server.setStartArgs(adminPanel.getStartArgs());
        }
        if (! adminPanel.getStopPath().equals(server.getStopPath())) {
            server.setStopPath(adminPanel.getStopPath());
        }
        if (! adminPanel.getStopArgs().equals(server.getStopArgs())) {
            server.setStopArgs(adminPanel.getStopArgs());
        }

        ServerNodeProvider provider = ServerNodeProvider.getDefault();
        if ( ! provider.isRegistered() ) {
            // setRegistered will connect the server
            provider.setRegistered(true);
            
        } 
    }
        
    private static String getMessage(String id) {
        return NbBundle.getMessage(PropertiesDialog.class, id);
    }

    public void setErrorMessage(String msg) {
        basePanel.setErrorMessage(msg);
    }

}
