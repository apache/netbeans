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
package org.netbeans.api.search.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.modules.search.SearchPanel;
import org.netbeans.modules.search.SearchScopeList;
import org.netbeans.spi.search.SearchScopeDefinition;
import org.openide.util.WeakListeners;

/**
 * Component controller for selecting search scope.
 *
 * Use {@link ComponentUtils} to create instances of this class.
 *
 * @author jhavlin
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class ScopeController extends ComponentController<JComboBox> {

    SearchScopeChangeListener searchScopeChangeListener;
    ChangeListener searchScopeChangeListenerWeak;
    private SearchScopeDefinition selectedSearchScope;
    private ScopeController.ManualSelectionListener manualSelectionListener;
    private String preferredId = null;
    private String manuallySelectedId = null;
    private boolean active = false;
    SearchScopeList scopeList;
    SearchScopeDefinition[] extraSearchScopes;

    ScopeController(JComboBox jComboBox, String preferredId,
            SearchScopeDefinition... extraSearchScopes) {
        super(jComboBox);
        this.preferredId = preferredId;
        this.manuallySelectedId = null;
        this.extraSearchScopes = extraSearchScopes;
        component.addHierarchyListener(new ScopeComboBoxHierarchyListener());
        component.setEditable(false);
        component.setRenderer(new ScopeCellRenderer());
    }

    private String chooseId() {
        if (SearchPanel.isOpenedForSelection()) {
            return "node selection";                                    //NOI18N
        } else {
            return preferredId;
        }
    }

    private void updateScopeItems(String preferredId) {

        component.removeAllItems();
        selectedSearchScope = null;

        for (SearchScopeDefinition ss : scopeList.getSeachScopeDefinitions()) {
            if (ss.isApplicable()) { // add only enabled search scopes
                ScopeItem si = new ScopeItem(ss);
                component.addItem(si);
                if (selectedSearchScope == null) {
                    if (ss.getTypeId().equals(preferredId)) {
                        selectedSearchScope = ss;
                        component.setSelectedItem(si);
                    }
                }
            }
        }
        if (selectedSearchScope == null) {
            ScopeItem si = (ScopeItem) component.getItemAt(0);
            selectedSearchScope = si.getSearchScope();
            component.setSelectedIndex(0);
        }
    }

    /**
     *
     * @return Currently selected search scope, or null if no search scope is
     * available.
     */
    private SearchScopeDefinition getSelectedSearchScope() {
        return selectedSearchScope;
    }

    /**
     * @return ID of selected search scope, or null if no scope is selected.
     */
    public @CheckForNull String getSelectedScopeId() {
        SearchScopeDefinition ss = getSelectedSearchScope();
        return ss == null ? null : ss.getTypeId();
    }

    public @CheckForNull String getSelectedScopeTitle() {
        ScopeItem si = (ScopeItem) component.getSelectedItem();
        return si == null ? null : si.toString();
    }

    /**
     * Get search info for selected search scope.
     *
     * @return Appropriate search info, or null if not available.
     */
    public @CheckForNull
    SearchInfo getSearchInfo() {
        SearchScopeDefinition ss = getSelectedSearchScope();
        if (ss == null) {
            return null;
        } else {
            SearchInfo ssi = ss.getSearchInfo();
            return ssi;
        }
    }

    /**
     * Wrapper of scope to be used as JComboBox item.
     */
    private final class ScopeItem {

        private static final String START = "(";                       // NOI18N
        private static final String END = ")";                         // NOI18N
        private static final String SP = " ";                          // NOI18N
        private static final String ELLIPSIS = "...";                  // NOI18N
        private static final int MAX_EXTRA_INFO_LEN = 20;
        private SearchScopeDefinition searchScope;

        public ScopeItem(SearchScopeDefinition searchScope) {
            this.searchScope = searchScope;
        }

        public SearchScopeDefinition getSearchScope() {
            return this.searchScope;
        }

        private boolean isAdditionaInfoAvailable() {
            return searchScope.getAdditionalInfo() != null
                    && searchScope.getAdditionalInfo().length() > 0;
        }

        private String getTextForLabel(String text) {
            String extraInfo = searchScope.getAdditionalInfo();
            String extraText = extraInfo;
            if (extraInfo.length() > MAX_EXTRA_INFO_LEN) {
                extraText = extraInfo.substring(0, MAX_EXTRA_INFO_LEN)
                        + ELLIPSIS;
                if (extraText.length() >= extraInfo.length()) {
                    extraText = extraInfo;
                }
            }
            return getFullText(text, extraText);
        }

        private String getFullText(String text, String extraText) {
            return text + SP + START + extraText + END;
        }

        @Override
        public String toString() {
            if (isAdditionaInfoAvailable()) {
                return getTextForLabel(clr(searchScope.getDisplayName()));
            } else {
                return clr(searchScope.getDisplayName());
            }
        }

        /**
         * Clear some legacy special characters from scope names.
         *
         * Some providers can still include ampresands that were used for
         * mnemonics in previous versions, but now are ignored.
         */
        private String clr(String s) {
            return s == null ? "" : s.replaceAll("\\&", "");            //NOI18N
        }
    }

    private class SearchScopeChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            if (manuallySelectedId == null && selectedSearchScope != null) {
                manuallySelectedId = selectedSearchScope.getTypeId();
            }
            component.removeActionListener(manualSelectionListener);
            updateScopeItems(manuallySelectedId);
            component.addActionListener(manualSelectionListener);
            Dialog d = (Dialog) SwingUtilities.getAncestorOfClass(
                    Dialog.class, component);
            if (d != null) {
                d.repaint();
            }
        }
    }

    private class ManualSelectionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            ScopeItem item = (ScopeItem) component.getSelectedItem();
            if (item != null) {
                selectedSearchScope = item.getSearchScope();
                manuallySelectedId = selectedSearchScope.getTypeId();
                selectedSearchScope.selected();
            } else {
                selectedSearchScope = null;
            }
        }
    }

    private class ScopeComboBoxHierarchyListener implements HierarchyListener {

        @Override
        public void hierarchyChanged(HierarchyEvent e) {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                toggleListeners();
            }
        }

        private synchronized void toggleListeners() {
            if (component.isShowing() && !active) {
                initListeners();
            } else if (!component.isShowing() && active) {
                cleanListeners();
            }
        }

        private void initListeners() {
            if (manuallySelectedId == null) {
                manuallySelectedId = chooseId();
            }
            scopeList = new SearchScopeList(extraSearchScopes);
            manualSelectionListener = new ManualSelectionListener();
            searchScopeChangeListener = new SearchScopeChangeListener();
            searchScopeChangeListenerWeak = WeakListeners.change(
                    searchScopeChangeListener, scopeList);
            scopeList.addChangeListener(searchScopeChangeListenerWeak);
            updateScopeItems(manuallySelectedId);
            component.addActionListener(manualSelectionListener);
            active = true;
        }

        private void cleanListeners() {
            scopeList.removeChangeListener(searchScopeChangeListenerWeak);
            searchScopeChangeListenerWeak = null;
            searchScopeChangeListener = null;
            scopeList.clean();
            scopeList = null;
            component.removeActionListener(manualSelectionListener);
            manualSelectionListener = null;
            active = false;
        }
    }

    private static class ScopeCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            Component component = super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            if (component instanceof JLabel) {
                JLabel label = (JLabel) component;
                if (value instanceof ScopeItem) {
                    ScopeItem item = (ScopeItem) value;
                    label.setIcon(item.getSearchScope().getIcon());
                }
            }
            return component;
        }
    }
};
