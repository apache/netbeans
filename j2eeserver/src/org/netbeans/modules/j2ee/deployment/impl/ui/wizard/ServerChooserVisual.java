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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.deployment.impl.ui.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.openide.util.NbBundle;

/**
 *
 * @author  Andrei Badea
 */
public class ServerChooserVisual extends javax.swing.JPanel {
    private final List listeners = new ArrayList();
    private AddServerInstanceWizard wizard;
    private HashMap displayNames;
    private boolean updatingDisplayName = false;
    
    public ServerChooserVisual() {
        displayNames = new HashMap();
        initComponents();
        
        ServerAdapter selected = (ServerAdapter) ((ComboBoxModel)serverListBox.getModel()).getSelectedItem();
        serverListBox.setSelectedValue(selected, true);
        if (selected != null)
            fillDisplayName(selected.getServer());
        
        displayNameEditField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                displayNameEditFieldUpdate();
            }
            
            public void removeUpdate(DocumentEvent e) {
                displayNameEditFieldUpdate();
            }
            
            public void changedUpdate(DocumentEvent e) {
                displayNameEditFieldUpdate();
            }
        });
    }
    
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    public void read(AddServerInstanceWizard wizard) {
        if (this.wizard == null)
            this.wizard = wizard;
        
        Object prop = wizard.getProperty(AddServerInstanceWizard.PROP_DISPLAY_NAME);
        if (prop != null)
            displayNameEditField.setText((String)prop);
    }
    
    public void store(AddServerInstanceWizard wizard) {
        wizard.putProperty(AddServerInstanceWizard.PROP_DISPLAY_NAME, displayNameEditField.getText());
        Object selectedItem = serverListBox.getSelectedValue();
        if (selectedItem != null) {
            wizard.putProperty(AddServerInstanceWizard.PROP_SERVER, ((ServerAdapter)selectedItem).getServer());
        }
    }

    public boolean hasValidData() {
        boolean result = isServerValid() && isDisplayNameValid();
        if (result) {
            wizard.setErrorMessage(null);
        }
        return result;
    }

    private boolean isServerValid() {
        boolean result = serverListBox.getSelectedValue() != null;
        if (!result)
            wizard.setErrorMessage(NbBundle.getMessage(ServerChooserVisual.class, "MSG_SCV_ChooseServer"));
        return result;
    }
    
    private boolean isDisplayNameValid() {
        String trimmed = displayNameEditField.getText().trim();
        boolean result;
        
        if (trimmed.length() <= 0) {
            wizard.setErrorMessage(NbBundle.getMessage(ServerChooserVisual.class, "MSG_SCV_DisplayName"));
            return false;
        }
        
        if (getServerInstance(trimmed) != null) {
            wizard.setErrorMessage(NbBundle.getMessage(ServerChooserVisual.class, "MSG_SCV_DisplayNameExists"));
            return false;
        }
        
        return true;
    }
    
    private ServerInstance getServerInstance(String displayName) {
        Iterator iter = ServerRegistry.getInstance().getInstances().iterator();
        while (iter.hasNext()) {
            ServerInstance instance = (ServerInstance)iter.next();
            if (instance.getDisplayName() != null
                    && instance.getDisplayName().equalsIgnoreCase(displayName)) {
                return instance;
            }
        }
        return null;
    }
    
    private void displayNameEditFieldUpdate() {
        if (!updatingDisplayName) {
            fireChange();
        }
    }
    
    private void fireChange() {
        ChangeEvent event = new ChangeEvent(this);
        ArrayList tempList;

        synchronized (listeners) {
            tempList = new ArrayList(listeners);
        }

        Iterator iter = tempList.iterator();
        while (iter.hasNext())
            ((ChangeListener)iter.next()).stateChanged(event);
    }   
    
    private String generateDisplayName(Server server) {
        String name;
        int count = 0;
        
        do {
            name = server.getDisplayName();
            if (count != 0)
                name += " (" + String.valueOf(count) + ")";
            
            count++;
        } while (getServerInstance(name) != null);
        
        return name;
    }
    
    private void fillDisplayName(Server server) {
        String name = (String)displayNames.get(server);
        if (name == null)
            name = generateDisplayName(server);
        updatingDisplayName = true; //disable firing from setText
        displayNameEditField.setText(name);
        updatingDisplayName = false;
        fireChange();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        displayNameEditField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        serverListBox = new javax.swing.JList();

        setName(org.openide.util.NbBundle.getBundle(ServerChooserVisual.class).getString("LBL_SCV_Name")); // NOI18N

        jLabel1.setLabelFor(serverListBox);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getBundle(ServerChooserVisual.class).getString("LBL_SCV_Server")); // NOI18N

        jLabel2.setLabelFor(displayNameEditField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getBundle(ServerChooserVisual.class).getString("LBL_SCV_DisplayName")); // NOI18N

        displayNameEditField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                displayNameEditFieldKeyReleased(evt);
            }
        });

        serverListBox.setModel(new ServerModel());
        serverListBox.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                serverListBoxValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(serverListBox);
        serverListBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServerChooserVisual.class, "A11Y_SCV_NAME_Server")); // NOI18N
        serverListBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerChooserVisual.class, "A11Y_SCV_DESC_Server")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(displayNameEditField, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(displayNameEditField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        displayNameEditField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServerChooserVisual.class, "A11Y_SCV_NAME_DisplayName")); // NOI18N
        displayNameEditField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerChooserVisual.class, "A11Y_SCV_DESC_DisplayName")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServerChooserVisual.class, "A11Y_SCV_NAME")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerChooserVisual.class, "A11Y_SCV_DESC")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void serverListBoxValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_serverListBoxValueChanged
       if (!evt.getValueIsAdjusting()) {
           ServerAdapter adapter = (ServerAdapter) serverListBox.getSelectedValue();
           if (adapter != null) {
               Server server = adapter.getServer();
               if (server != null) {
                   fillDisplayName(server);
               }
           } else {
               fireChange();
           }
       }
}//GEN-LAST:event_serverListBoxValueChanged

    private void displayNameEditFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_displayNameEditFieldKeyReleased
        ServerAdapter serverAdapter = (ServerAdapter) serverListBox.getSelectedValue();
        if (serverAdapter != null) {
            displayNames.put(serverAdapter.getServer(), displayNameEditField.getText());
        }
    }//GEN-LAST:event_displayNameEditFieldKeyReleased
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField displayNameEditField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList serverListBox;
    // End of variables declaration//GEN-END:variables
    
    private static class ServerModel implements ComboBoxModel {
        private List servers;
        private ServerAdapter selected;
                
        public ServerModel() {
            servers = new ArrayList();
            Collection allServers = ServerRegistry.getInstance().getServers();
            Iterator iter = allServers.iterator();
            while (iter.hasNext()) {
                Server server = (Server)iter.next();
                OptionalDeploymentManagerFactory factory = server.getOptionalFactory();
                if (factory != null && factory.getAddInstanceIterator() != null) {
                    ServerAdapter serverAdapter = new ServerAdapter(server);
                    servers.add(serverAdapter);
                    String n = server.getShortName();
                    if (null != n && n.startsWith("gfv3")) {
                        selected = serverAdapter;
                    }
                    if (null == selected && "J2EE".equals(n)) { // NOI18N
                        selected = serverAdapter;
                    }
                }
            }
            Collections.sort(servers);
            if (selected == null) {
                selected = (servers.size() > 0) ? (ServerAdapter)servers.get(0) : null;
            }
        }
        
        public Object getElementAt(int index) {
            return servers.get(index);
        }

        public void removeListDataListener(javax.swing.event.ListDataListener l) {
        }

        public void addListDataListener(javax.swing.event.ListDataListener l) {
        }

        public int getSize() {
            return servers.size();
        }
        
        public Object getSelectedItem() {
            return selected;
        }
        
        public void setSelectedItem(Object anItem) {
            selected = (ServerAdapter)anItem;
        }
    }
    
    private static class ServerAdapter implements Comparable {
        private Server server;
        
        public ServerAdapter(Server server) {
            this.server = server;
        }
        
        public Server getServer() {
            return server;
        }
        
        public String toString() {
            return server.getDisplayName();
        }
        
        public int compareTo(Object o) {
            return toString().compareTo(o.toString());
        }
    }
}
