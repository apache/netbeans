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

package org.netbeans.modules.apisupport.project.ui.customizer;

import org.netbeans.modules.apisupport.project.ModuleDependency;
import java.awt.Component;
import java.io.CharConversionException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.table.AbstractTableModel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.openide.awt.HtmlRenderer;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

/**
 * @author mkrauskopf
 */
public final class CustomizerComponentFactory {
    
    static DependencyListModel INVALID_DEP_LIST_MODEL;
    
    private static final String INVALID_PLATFORM =
            "<html><font color=\"!nb.errorForeground\">&lt;" // NOI18N
            + NbBundle.getMessage(CustomizerComponentFactory.class, "MSG_InvalidPlatform")
            + "&gt;</font></html>"; // NOI18N
    
    private CustomizerComponentFactory() {
        // don't allow instances
    }
    
    /**
     * Creates a list model for a set of module dependencies.
     * The dependencies will be sorted by module display name.
     */
    static CustomizerComponentFactory.DependencyListModel createSortedDependencyListModel(
            final Set<ModuleDependency> deps) {
        assert deps != null;
        return new CustomizerComponentFactory.DependencyListModel(deps, true);
    }
    
    /**
     * Creates a list model for a set of module dependencies.
     * The dependencies will be left in the order given.
     */
    static CustomizerComponentFactory.DependencyListModel createDependencyListModel(
            final Set<ModuleDependency> deps) {
        assert deps != null;
        return new CustomizerComponentFactory.DependencyListModel(deps, false);
    }
    
    static synchronized CustomizerComponentFactory.DependencyListModel getInvalidDependencyListModel() {
        if (INVALID_DEP_LIST_MODEL == null) {
            INVALID_DEP_LIST_MODEL = new DependencyListModel(DependencyListModel.State.INVALID, true);
        }
        return INVALID_DEP_LIST_MODEL;
    }
    
    static ListCellRenderer/*<ModuleDependency|WAIT_VALUE>*/ getDependencyCellRenderer(boolean boldfaceApiModules) {
        return new DependencyListCellRenderer(boldfaceApiModules);
    }
    
    static ListCellRenderer/*<Project>*/ getModuleCellRenderer() {
        return new ProjectListCellRenderer();
    }
    static ListCellRenderer/*<ModuleEntry>*/ getModuleEntryCellRenderer() {
        return new ModuleEntryListCellRenderer();
    }
    
    static final class DependencyListModel extends AbstractListModel implements UIUtil.WaitingModel {
        
        private final Set<ModuleDependency> currentDeps;
        
        private boolean changed;
        private enum State { INVALID, WAITING, OK };
        private State state;
        
        DependencyListModel(Set<ModuleDependency> deps, boolean sorted) {
            if (sorted) {
                currentDeps = new TreeSet<ModuleDependency>(ModuleDependency.LOCALIZED_NAME_COMPARATOR);
                currentDeps.addAll(deps);
            } else {
                currentDeps = deps;
            }
            state = State.OK;
        }
        
        private DependencyListModel(State st, boolean sorted) {
            if (st == State.INVALID) {
                currentDeps = Collections.emptySet();
            } else if (sorted) {
                currentDeps = new TreeSet<ModuleDependency>(ModuleDependency.LOCALIZED_NAME_COMPARATOR);
            } else {
                currentDeps = new HashSet<ModuleDependency>();
            }
            state = st;
        }
        
        public int getSize() {
            return state != State.OK ? 1 : currentDeps.size();
        }
        
        public Object getElementAt(int i) {
            switch (state) {
                case OK:
                    Object[] currentDepsA = currentDeps.toArray();
                    return i >= 0 && i < currentDepsA.length ? currentDepsA[i] : /* #202954 */INVALID_PLATFORM;
                case INVALID:
                    return INVALID_PLATFORM;
                case WAITING:
                default:
                    return UIUtil.WAIT_VALUE;
            }
        }

        public static DependencyListModel createBgWaitModel(boolean sorted) {
            return new DependencyListModel(State.WAITING, sorted);
        }

        void setDependencies(SortedSet<ModuleDependency> deps) {
            if (state != State.WAITING) {
                return; // #179979: unknown how this happens
            }
            currentDeps.addAll(deps);
            state = State.OK;
            int origSize = currentDeps.size();
            fireContentsChanged(this, 0, origSize);
        }

        void setInvalid() {
            if (state == State.INVALID)
                throw new IllegalStateException("DependencyListModel already marked 'invalid'.");    // NOI18N
            state = State.INVALID;
            int origSize = currentDeps.size();
            fireContentsChanged(this, 0, origSize);
        }

        @Override public boolean isWaiting() {
            return state == State.WAITING;
        }
        
        ModuleDependency getDependency(int i) {
            return (ModuleDependency) getElementAt(i);
        }
        
        void addDependency(ModuleDependency dep) {
            if (!currentDeps.contains(dep)) {
                int origSize = currentDeps.size();
                currentDeps.add(dep);
                changed = true;
                this.fireContentsChanged(this, 0, origSize);
            }
        }
        
        void removeDependencies(Collection<ModuleDependency> deps) {
            int origSize = currentDeps.size();
            currentDeps.removeAll(deps);
            changed = true;
            this.fireContentsChanged(this, 0, origSize);
        }
        
        void editDependency(ModuleDependency origDep, ModuleDependency newDep) {
            currentDeps.remove(origDep);
            currentDeps.add(newDep);
            changed = true;
            this.fireContentsChanged(this, 0, currentDeps.size());
        }
        
        Set<ModuleDependency> getDependencies() {
            return Collections.unmodifiableSet(currentDeps);
        }
        
        boolean isChanged() {
            return changed;
        }
    }
    
    private static final class DependencyListCellRenderer implements ListCellRenderer {
        
        private final HtmlRenderer.Renderer renderer = HtmlRenderer.createRenderer();
        private final boolean boldfaceApiModules;
        
        public DependencyListCellRenderer(boolean boldfaceApiModules) {
            this.boldfaceApiModules = boldfaceApiModules;
        }
        
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            String text;
            if (value == UIUtil.WAIT_VALUE) {
                text = UIUtil.WAIT_VALUE;
            } else if (value == INVALID_PLATFORM) {
                text = INVALID_PLATFORM;
                renderer.setHtml(true);
            } else {
                ModuleDependency md = (ModuleDependency) value;
                // XXX the following is wrong; spec requires additional logic:
                boolean bold = boldfaceApiModules && md.getModuleEntry().getPublicPackages().length > 0;
                boolean deprecated = md.getModuleEntry().isDeprecated();
                renderer.setHtml(bold || deprecated);
                String locName = md.getModuleEntry().getLocalizedName();
                text = locName;
                if (bold || deprecated) {
                    try {
                        text = "<html>" + (bold ? "<b>" : "") + (deprecated ? "<s>" : "") + XMLUtil.toElementContent(locName); // NOI18N
                    } catch (CharConversionException e) {
                        // forget it
                    }
                }
            }
            return renderer.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
        }
        
    }
    
    private static class ProjectListCellRenderer extends DefaultListCellRenderer {
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(
                    list, ProjectUtils.getInformation((Project) value).getDisplayName(),
                    index, isSelected, cellHasFocus);
            return c;
        }
        
    }
    
    private static class ModuleEntryListCellRenderer extends DefaultListCellRenderer {
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            ModuleEntry me = (ModuleEntry)value;
            Component c = super.getListCellRendererComponent(
                    list, me.getLocalizedName(), index, isSelected, cellHasFocus);
            return c;
        }
        
    }
    
    static final class PublicPackagesTableModel extends AbstractTableModel {
        
        private Boolean[] selected;
        private Boolean[] originalSelected;
        private String[] pkgNames;
        
        PublicPackagesTableModel(Map<String, Boolean> publicPackages) {
            reloadData(publicPackages);
        }
        
        void reloadData(Map<String, Boolean> publicPackages) {
            selected = new Boolean[publicPackages.size()];
            publicPackages.values().toArray(selected);
            if (originalSelected == null) {
                originalSelected = new Boolean[publicPackages.size()];
                System.arraycopy(selected, 0, originalSelected, 0, selected.length);
            }
            pkgNames = new String[publicPackages.size()];
            publicPackages.keySet().toArray(pkgNames);
            fireTableDataChanged();
        }
        
        public int getRowCount() {
            return pkgNames.length;
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return selected[rowIndex];
            } else {
                return pkgNames[rowIndex];
            }
        }
        
        @Override
        public Class getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            assert columnIndex == 0 : "Who is trying to modify second column?"; // NOI18N
            selected[rowIndex] = (Boolean) aValue;
            fireTableCellUpdated(rowIndex, 0);
        }

        /**
         * Returns a (sorted) set of selected packages.
         * Set is newly created each time the method gets called
         * @return set of selected packages
         */
        Set<String> getSelectedPackages() {
            Set<String> s = new TreeSet<String>();
            for (int i = 0; i < pkgNames.length; i++) {
                if (selected[i]) {
                    s.add(pkgNames[i]);
                }
            }
            return s;
        }
        
        public boolean isChanged() {
            return !Arrays.asList(selected).equals(Arrays.asList(originalSelected));
        }
        
    }
    
    static final class FriendListModel extends AbstractListModel {
        
        private final Set<String> friends = new TreeSet<String>();
        
        private boolean changed;
        
        FriendListModel(String[] friends) {
            if (friends != null) {
                this.friends.addAll(Arrays.asList(friends));
            }
        }
        
        public Object getElementAt(int index) {
            if (index >= friends.size()) {
                return null;
            } else {
                return friends.toArray()[index];
            }
        }
        
        public int getSize() {
            return friends.size();
        }
        
        void addFriend(String friend) {
            friends.add(friend);
            changed = true;
            super.fireIntervalAdded(this, 0, friends.size());
        }
        
        void removeFriend(String friend) {
            friends.remove(friend);
            changed = true;
            super.fireIntervalRemoved(this, 0, friends.size());
        }
        
        Set<String> getFriends() {
            return Collections.unmodifiableSet(friends);
        }
        
        boolean isChanged() {
            return changed;
        }
    }
    
    static final class RequiredTokenListModel extends AbstractListModel {
        
        private final SortedSet<String> tokens;
        private boolean changed;
        
        RequiredTokenListModel(final SortedSet<String> tokens) {
            this.tokens = new TreeSet<String>(tokens);
        }
        
        public Object getElementAt(int index) {
            return index >= tokens.size() ? null : tokens.toArray()[index];
        }
        
        public int getSize() {
            return tokens.size();
        }
        
        void addToken(String token) {
            tokens.add(token);
            changed = true;
            super.fireIntervalAdded(this, 0, tokens.size());
        }
        
        void removeToken(String token) {
            tokens.remove(token);
            changed = true;
            super.fireIntervalRemoved(this, 0, tokens.size());
        }
        
        String[] getTokens() {
            String[] result = new String[tokens.size()];
            return tokens.toArray(result);
        }
        
        boolean isChanged() {
            return changed;
        }
        
    }
    
    static final class SuiteSubModulesListModel extends AbstractListModel {
        
        private final SortedSet<NbModuleProject> subModules;
        
        private boolean changed;
        
        SuiteSubModulesListModel(Set<NbModuleProject> subModules) {
            this.subModules = new TreeSet<NbModuleProject>(Util.projectDisplayNameComparator());
            this.subModules.addAll(subModules);
        }
        
        public int getSize() {
            return subModules.size();
        }
        
        public Object getElementAt(int i) {
            return subModules.toArray()[i];
        }
        
        boolean contains(Project module) {
            return subModules.contains(module);
        }
        
        void removeModules(Collection modules) {
            int origSize = subModules.size();
            subModules.removeAll(modules);
            changed = true;
            this.fireContentsChanged(this, 0, origSize);
        }
        
        boolean addModule(NbModuleProject module) {
            int origSize = subModules.size();
            boolean added = subModules.add(module);
            changed = true;
            this.fireContentsChanged(this, 0, origSize + 1);
            return added;
        }
        
        public Set<NbModuleProject> getSubModules() {
            return Collections.unmodifiableSortedSet(subModules);
        }
        
        public boolean isChanged() {
            return changed;
        }
    }
    
}
