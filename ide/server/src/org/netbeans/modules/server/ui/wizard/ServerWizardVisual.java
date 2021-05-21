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

package org.netbeans.modules.server.ui.wizard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.ListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.server.ServerRegistry;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.netbeans.spi.server.ServerWizardProvider;
import org.openide.awt.Mnemonics;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Andrei Badea
 * @author Petr Hejl
 */
public class ServerWizardVisual extends javax.swing.JPanel {

    private final CopyOnWriteArrayList<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();

    private final Map<ServerWizardProvider, String> displayNames = new HashMap<ServerWizardProvider, String>();

    private AddServerInstanceWizard wizard;

    private boolean updatingDisplayName = false;
    
    private ServerRegistry registry;

    public ServerWizardVisual(ServerRegistry registry) {
        this.registry = registry;
        initComponents();
        if (registry.isCloud()) {
            Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getBundle(ServerWizardVisual.class).getString("LBL_SCV_Cloud")); // NOI18N
            setName(NbBundle.getBundle(ServerWizardVisual.class).getString("LBL_CCV_Name")); // NOI18N
        }
        Queue<WizardAdapter> selected = new PriorityQueue<WizardAdapter>(5, 
                registry.isCloud() ? new WizardPriority(CLOUD_PRIORITY_LIST) : new WizardPriority(PRIORITY_LIST));
        for (int i = 0; i < serverListBox.getModel().getSize(); i++) {
            selected.add((WizardAdapter) serverListBox.getModel().getElementAt(i));
        }

        if (!selected.isEmpty()) {
            WizardAdapter selectedItem = selected.peek();
            serverListBox.setSelectedValue(selectedItem, true);
            fillDisplayName(selectedItem.getServerInstanceWizard());
        }

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

    public void addChangeListener(ChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeChangeListener(ChangeListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public void read(AddServerInstanceWizard wizard) {
        if (this.wizard == null) {
            this.wizard = wizard;
        }

        Object prop = wizard.getProperty(AddServerInstanceWizard.PROP_DISPLAY_NAME);
        if (prop != null) {
            displayNameEditField.setText((String) prop);
        }
    }
    
    public void store(AddServerInstanceWizard wizard) {
        wizard.putProperty(AddServerInstanceWizard.PROP_DISPLAY_NAME, displayNameEditField.getText());
        Object selectedItem = serverListBox.getSelectedValue();
        if (selectedItem != null) {
            wizard.putProperty(AddServerInstanceWizard.PROP_SERVER_INSTANCE_WIZARD,
                    ((WizardAdapter) selectedItem).getServerInstanceWizard());
        }
    }

    boolean hasValidData() {
        boolean result = isServerValid() && isDisplayNameValid();
        if (result) {
            wizard.setErrorMessage(null);
        }

        return result;
    }

    private boolean isServerValid() {
        boolean result = serverListBox.getSelectedValue() != null;
        if (!result) {
            if (registry.isCloud()) {
                wizard.setErrorMessage(NbBundle.getMessage(ServerWizardVisual.class, "MSG_SCV_ChooseServer"));
            } else {
                wizard.setErrorMessage(NbBundle.getMessage(ServerWizardVisual.class, "MSG_CCV_ChooseServer"));
            }
        }
        return result;
    }

    private boolean isDisplayNameValid() {
        String trimmed = displayNameEditField.getText().trim();

        if (trimmed.length() <= 0) {
            wizard.setErrorMessage(NbBundle.getMessage(ServerWizardVisual.class, "MSG_SCV_DisplayName"));
            return false;
        }

        if (existsDisplayName(trimmed)) {
            wizard.setErrorMessage(NbBundle.getMessage(ServerWizardVisual.class, "MSG_SCV_DisplayNameExists"));
            return false;
        }

        return true;
    }

    private boolean existsDisplayName(String displayName) {
        for (ServerInstanceProvider type : registry.getProviders()) {
            for (ServerInstance instance : type.getInstances()) {
                assert instance != null : "ServerInstance returned by provider " + type + " is null";
                if (instance == null) {
                    continue;
                }
                String instanceName = instance.getDisplayName();
                if (null != instanceName && instanceName.equalsIgnoreCase(displayName)) {
                    return true;
                } else if (null == instanceName) {
                    Logger.getLogger(this.getClass().getName()).log(Level.FINE,
                            "corrupted ServerInstance: " + instance.toString());
                }
            }
        }
        return false;
    }

    private void displayNameEditFieldUpdate() {
        if (!updatingDisplayName) {
            fireChange();
        }
    }

    private void fireChange() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);
        }
    }

    private String generateDisplayName(ServerWizardProvider server) {
        String name;
        int count = 0;

        do {
            name = server.getDisplayName();
            if (count != 0) {
                name += " (" + String.valueOf(count) + ")"; // NOI18N
            }

            count++;
        } while (existsDisplayName(name));

        return name;
    }

    private void fillDisplayName(ServerWizardProvider server) {
        String name = displayNames.get(server);
        if (name == null || name.length() == 0) {
            name = generateDisplayName(server);
        }
        updatingDisplayName = true; // disable firing from setText
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

        setName(org.openide.util.NbBundle.getBundle(ServerWizardVisual.class).getString("LBL_SCV_Name")); // NOI18N

        jLabel1.setLabelFor(serverListBox);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getBundle(ServerWizardVisual.class).getString("LBL_SCV_Server")); // NOI18N

        jLabel2.setLabelFor(displayNameEditField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getBundle(ServerWizardVisual.class).getString("LBL_SCV_DisplayName")); // NOI18N

        displayNameEditField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                displayNameEditFieldKeyReleased(evt);
            }
        });

        serverListBox.setModel(new WizardListModel(registry));
        serverListBox.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        serverListBox.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                serverListBoxValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(serverListBox);
        serverListBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServerWizardVisual.class, "A11Y_SCV_NAME_Server")); // NOI18N
        serverListBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerWizardVisual.class, "A11Y_SCV_DESC_Server")); // NOI18N

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

        displayNameEditField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServerWizardVisual.class, "A11Y_SCV_NAME_DisplayName")); // NOI18N
        displayNameEditField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerWizardVisual.class, "A11Y_SCV_DESC_DisplayName")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServerWizardVisual.class, "A11Y_SCV_NAME")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerWizardVisual.class, "A11Y_SCV_DESC")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void serverListBoxValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_serverListBoxValueChanged
       if (!evt.getValueIsAdjusting()) {
           WizardAdapter adapter = (WizardAdapter) serverListBox.getSelectedValue();
           if (adapter != null) {
               ServerWizardProvider server = adapter.getServerInstanceWizard();
               if (server != null) {
                   fillDisplayName(server);
               }
           } else {
               fireChange();
           }
       }
}//GEN-LAST:event_serverListBoxValueChanged

    private void displayNameEditFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_displayNameEditFieldKeyReleased
        WizardAdapter wizardAdapter = (WizardAdapter) serverListBox.getSelectedValue();
        if (wizardAdapter != null) {
            displayNames.put(wizardAdapter.getServerInstanceWizard(), displayNameEditField.getText());
        }
    }//GEN-LAST:event_displayNameEditFieldKeyReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField displayNameEditField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList serverListBox;
    // End of variables declaration//GEN-END:variables

    private static class WizardListModel implements ListModel, LookupListener {

        private final List<WizardAdapter> serverWizards = Collections.synchronizedList(new ArrayList<WizardAdapter>());

        private final List<ListDataListener> listeners = new CopyOnWriteArrayList<ListDataListener>();

        private final Lookup.Result<ServerWizardProvider> result;

        public WizardListModel(ServerRegistry registry) {
            this.result = Lookups.forPath(registry.getPath()).lookupResult(ServerWizardProvider.class);
            this.result.addLookupListener(WeakListeners.create(LookupListener.class, this, result));
            resultChanged(null);
        }

        @Override
        public Object getElementAt(int index) {
            return serverWizards.get(index);
        }

        @Override
        public int getSize() {
            return serverWizards.size();
        }

        @Override
        public void addListDataListener(ListDataListener l) {
            listeners.add(l);
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
            listeners.remove(l);
        }

        @Override
        public final void resultChanged(LookupEvent ev) {
            List<WizardAdapter> fresh = new ArrayList<WizardAdapter>();
            for (ServerWizardProvider wizard : result.allInstances()) {

                // safety precaution shouldn't ever happen - used because of bridging
                if (wizard.getInstantiatingIterator() != null) {
                    fresh.add(new WizardAdapter(wizard));
                }
            }
            Collections.sort(fresh);

            synchronized (serverWizards) {
                serverWizards.clear();
                serverWizards.addAll(fresh);
            }

            ListDataEvent event = new ListDataEvent(this,
                        ListDataEvent.CONTENTS_CHANGED, 0, fresh.size() - 1);
            for (ListDataListener l : listeners) {
                l.contentsChanged(event);
            }
        }
    }

    private static class WizardAdapter implements Comparable<WizardAdapter> {

        private final ServerWizardProvider serverInstanceWizard;

        public WizardAdapter(ServerWizardProvider serverInstanceWizard) {
            this.serverInstanceWizard = serverInstanceWizard;
        }

        public ServerWizardProvider getServerInstanceWizard() {
            return serverInstanceWizard;
        }

        public int compareTo(WizardAdapter o) {
            return serverInstanceWizard.getDisplayName().compareTo(
                    o.getServerInstanceWizard().getDisplayName());
        }

        @Override
        public String toString() {
            return serverInstanceWizard.getDisplayName();
        }
    }

    private static final List<Pattern> PRIORITY_LIST = new ArrayList<Pattern>(7);
    private static final List<Pattern> CLOUD_PRIORITY_LIST = new ArrayList<Pattern>(1);

    static {
        PRIORITY_LIST.add(Pattern.compile(".*Sailfin.*")); // NOI18N
        PRIORITY_LIST.add(Pattern.compile(".*Sun\\s*Java\\s*System.*")); // NOI18N
        PRIORITY_LIST.add(Pattern.compile(".*GlassFish\\s*v1.*")); // NOI18N
        PRIORITY_LIST.add(Pattern.compile(".*GlassFish\\s*v2.*")); // NOI18N
        PRIORITY_LIST.add(Pattern.compile(".*GlassFish\\s*v3.*")); // NOI18N
        PRIORITY_LIST.add(Pattern.compile(".*GlassFish\\s*v3")); // NOI18N
        PRIORITY_LIST.add(Pattern.compile(".*GlassFish\\s*Server\\s*3.*")); // NOI18N
        PRIORITY_LIST.add(Pattern.compile(".*GlassFish\\s*Server.*")); // NOI18N
        CLOUD_PRIORITY_LIST.add(Pattern.compile(".*Oracle\\sCloud.*")); // NOI18N
    }
    
    private static class WizardPriority implements Comparator<WizardAdapter>, Serializable {

        private List<Pattern> priorityList;

        private WizardPriority(List<Pattern> priorityList) {
            this.priorityList = priorityList;
        }
        
        public int compare(WizardAdapter o1, WizardAdapter o2) {
            Integer priority1 = computePriority(o1.getServerInstanceWizard().getDisplayName());
            Integer priority2 = computePriority(o2.getServerInstanceWizard().getDisplayName());

            return -priority1.compareTo(priority2);
        }

        private int computePriority(String name) {
            int priority = 0;
            for (int i = 0; i < priorityList.size(); i++) {
                if (priorityList.get(i).matcher(name).matches()) {
                    priority = i+1;
                }
            }
            return priority;
        }

    }
}
