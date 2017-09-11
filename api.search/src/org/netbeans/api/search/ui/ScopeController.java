/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
