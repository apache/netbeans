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
package org.netbeans.modules.refactoring.spi.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.UIResource;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.modules.refactoring.spi.impl.DelegatingCustomScopeProvider;
import org.netbeans.modules.refactoring.spi.impl.DelegatingScopeInformation;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * ScopePanel provides a component to use for scope selection. A customize
 * button (a JButton with ellipses) will be displayed when any of the registered
 * scopes is customizable.
 *
 * @author Ralph Benjamin Ruijs &lt;ralphbenjamin@netbeans.org&gt;
 * @since 1.30
 */
public final class ScopePanel extends javax.swing.JPanel {

    private static final String ELLIPSIS = "\u2026"; //NOI18N
    private static final int SCOPE_COMBOBOX_COLUMNS = 14;
    private final String id;
    private final Preferences preferences;
    private final String preferencesKey;
    private ArrayList<DelegatingScopeInformation> scopes;
    private ChangeListener parent;

    /**
     * Creates new form ScopePanel.
     *
     * @deprecated do not use this constructor. Only available for the Matisse
     * GUI-builder.
     */
    @Deprecated
    public ScopePanel() {
        this(null, null, null);
    }

    /**
     * Creates a new ScopePanel. The supplied id will be used to only get the
     * Scopes registered for a specific set of Scopes. The preferences and
     * preferencesKey will be used to store the user's selection.
     *
     * @param id the id for which the scopes are registered
     * @param preferences a preferences object to store user's selection
     * @param preferencesKey a key to use to store user's selection
     */
    public ScopePanel(String id, Preferences preferences, String preferencesKey) {
        this(id, preferences, preferencesKey, null);
    }
    
    /**
     * Creates a new ScopePanel. The supplied id will be used to only get the
     * Scopes registered for a specific set of Scopes. The preferences and
     * preferencesKey will be used to store the user's selection.
     *
     * @param id the id for which the scopes are registered
     * @param preferences a preferences object to store user's selection
     * @param preferencesKey a key to use to store user's selection
     * @param parent listener which is called after the user's selection has changed
     * 
     * @since XXX
     */
    public ScopePanel(String id, Preferences preferences, String preferencesKey, ChangeListener parent) {
        this.id = id;
        this.preferences = preferences;
        this.preferencesKey = preferencesKey;
        this.scopes = new ArrayList<>();
        this.parent = parent;
        initComponents();
    }

    /**
     * Initializes the Combobox and customize button of this ScopePanel. The
     * context will be passed to the different ScopeProviders initialize method.
     * This method will return false if there are no available scopes and this
     * panel should not be available to the user.
     *
     * @param context the Lookup to pass to the ScopeProviders
     * @return true if there is at least one Scope available, false otherwise
     */
    public boolean initialize(Lookup context, AtomicBoolean cancel) {
        scopes.clear();
        Collection<? extends ScopeProvider> scopeProviders = Lookups.forPath("Scopes" + "/" + id).lookupAll(ScopeProvider.class);
        final AtomicBoolean customizable = new AtomicBoolean();
        for (ScopeProvider provider : scopeProviders) {
            if (provider.initialize(context, new AtomicBoolean())) {
                scopes.add((DelegatingScopeInformation)provider);
                if (provider instanceof ScopeProvider.CustomScopeProvider) {
                    customizable.set(true);
                }
            }
        }

        scopes.sort(new Comparator<DelegatingScopeInformation>() {
            @Override
            public int compare(DelegatingScopeInformation o1, DelegatingScopeInformation o2) {
                return o1.getPosition() - o2.getPosition();
            }
        });
        if (!scopes.isEmpty()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    scopeCombobox.setModel(new DefaultComboBoxModel(scopes.toArray(new ScopeProvider[0])));
                    ScopePanel.this.btnCustomScope.setVisible(customizable.get());
                    String preselectId = preferences.get(preferencesKey, null);
                    if (preselectId == null || isNumeric(preselectId)) { // Needed for the old preferences of Java's Where Used Panel.
                        int defaultItem = (Integer) preferences.getInt(preferencesKey, -1); // NOI18N
                        if (defaultItem != (-1)) {
                            switch (defaultItem) {
                                case 0:
                                    preselectId = "all-projects";
                                    break;
                                case 1:
                                    preselectId = "current-project";
                                    break;
                                case 2:
                                    preselectId = "current-package";
                                    break;
                                case 3:
                                    preselectId = "current-file";
                                    break;
                                case 4:
                                    preselectId = "custom-scope";
                                    break;
                            }
                        }
                    }
                    if (preselectId != null) {
                        selectScopeById(preselectId);
                    } else {
                        selectPreferredScope();
                    }
                }
            });
        }
        return !scopes.isEmpty();
    }
    
    private static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * The currently selected scope in the Combobox. This can be a predefined
     * scope, or a customized one.
     *
     * @return the selected Scope
     */
    @CheckForNull
    public Scope getSelectedScope() {
        final ScopeProvider selectedScope = (ScopeProvider) scopeCombobox.getSelectedItem();
        return selectedScope != null ? selectedScope.getScope() : null;
    }
    
    /**
     * The currently selected ScopeProvider in the Combobox.
     *
     * @return the selected ScopeProvider
     * @since 1.44
     */
    @CheckForNull
    public ScopeProvider getSelectedScopeProvider() {
        return (ScopeProvider) scopeCombobox.getSelectedItem();
    }

    /**
     * Change the selected scope to one with the specified id. If the id does
     * not exist, nothing is changed. When the id is from a CustomScopeProvider
     * and it returns an empty scope, the preferred scope is selected.
     *
     * @see ScopeProvider.CustomScopeProvider
     *
     * @param id the id of the scope to select
     */
    public void selectScopeById(@NonNull String id) {
        ComboBoxModel m = scopeCombobox.getModel();

        for (int i = 0; i < m.getSize(); i++) {
            DelegatingScopeInformation sd = (DelegatingScopeInformation) m.getElementAt(i);

            if (sd.getId().equals(id)) {
                if (sd instanceof ScopeProvider.CustomScopeProvider) {
                    Scope s = sd.getScope();
                    if (s != null
                            && s.getFiles().isEmpty()
                            && s.getFolders().isEmpty()
                            && s.getSourceRoots().isEmpty()) {
                        selectPreferredScope();
                        return;
                    }
                }
                scopeCombobox.setSelectedItem(sd);
                return;
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnCustomScope = new javax.swing.JButton();
        scopeCombobox = new javax.swing.JComboBox();

        btnCustomScope.setAction(new ScopeAction(scopeCombobox));
        org.openide.awt.Mnemonics.setLocalizedText(btnCustomScope, "..."); // NOI18N

        scopeCombobox.setRenderer(new ScopeDescriptionRenderer());
        ((javax.swing.JTextField) scopeCombobox.getEditor().getEditorComponent()).setColumns(SCOPE_COMBOBOX_COLUMNS);
        scopeCombobox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scopeComboboxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(scopeCombobox, 0, 343, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCustomScope))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btnCustomScope)
                .addComponent(scopeCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void scopeComboboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scopeComboboxActionPerformed
        Object selectedItem = scopeCombobox.getSelectedItem();
        if(selectedItem instanceof DelegatingScopeInformation) {
            DelegatingScopeInformation scopeInfo = (DelegatingScopeInformation) selectedItem;
            preferences.put(preferencesKey, scopeInfo.getId());
        } else {
            preferences.remove(preferencesKey);
        }
        if(parent != null) {
            parent.stateChanged(new ChangeEvent(this));
        }
    }//GEN-LAST:event_scopeComboboxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCustomScope;
    private javax.swing.JComboBox scopeCombobox;
    // End of variables declaration//GEN-END:variables

    private void selectPreferredScope() {
        ComboBoxModel m = scopeCombobox.getModel();

        for (int i = 0; i < m.getSize(); i++) {
            DelegatingScopeInformation sd = (DelegatingScopeInformation) m.getElementAt(i);

            if (sd.getPosition() >= 0) {
                scopeCombobox.setSelectedItem(sd);
                return;
            }
        }
    }

    private class ScopeAction extends AbstractAction {

        private final JComboBox scopeCombobox;

        private ScopeAction(JComboBox scopeCombobox) {
            this.scopeCombobox = scopeCombobox;
            this.putValue(NAME, ELLIPSIS);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ComboBoxModel m = this.scopeCombobox.getModel();
            ScopeProvider selectedScope = (ScopeProvider) scopeCombobox.getSelectedItem();
            Scope scope = selectedScope.getScope();
            if (selectedScope instanceof DelegatingCustomScopeProvider) {
                showCustomizer((DelegatingCustomScopeProvider) selectedScope, scope);
            } else {
                for (int i = 0; i < m.getSize(); i++) {
                    ScopeProvider sd = (ScopeProvider) m.getElementAt(i);

                    if (sd instanceof DelegatingCustomScopeProvider) {
                        showCustomizer((DelegatingCustomScopeProvider) sd, scope);
                        break;
                    }
                }
            }
        }

        private void showCustomizer(DelegatingCustomScopeProvider csd, Scope scope) {
            csd.setScope(scope);
            if (csd.showCustomizer()) {
                selectScopeById(csd.getId());
            }
        }
    }

    private static class ScopeDescriptionRenderer extends JLabel implements ListCellRenderer, UIResource {

        public ScopeDescriptionRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            DelegatingScopeInformation scopeDescription = null;
            if (value instanceof DelegatingScopeInformation) {
                scopeDescription = (DelegatingScopeInformation) value;
            }
            if (scopeDescription != null) {
                String detail = scopeDescription.getDetail();
                String displayName = scopeDescription.getDisplayName();
                setText(detail == null ? displayName : displayName + " (" + detail + ")");
                setIcon(scopeDescription.getIcon());
            }

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            return this;
        }

        // #89393: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    }
}
