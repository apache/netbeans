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

package org.netbeans.modules.debugger.ui.views.debugging;

import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import javax.swing.Action;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.spi.debugger.ui.DebuggingView.DVFilter;
import org.netbeans.spi.debugger.ui.DebuggingView.DVFilter.Group;
import org.netbeans.spi.debugger.ui.DebuggingView.DVSupport;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

import org.openide.awt.Mnemonics;
import org.openide.util.ImageUtilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;


public final class FiltersDescriptor {

    public static final String SUSPEND_SORT = "suspend_sort";
    public static final String NATURAL_SORT = "natural_sort";
    public static final String ALPHABETIC_SORT = "alphabetic_sort";
    public static final String SHOW_QUALIFIED_NAMES = "show_fqn";
    public static final String SHOW_MONITORS = "show_monitors";
    public static final String SHOW_SYSTEM_THREADS = "show_system_threads";
    public static final String SHOW_SUSPEND_TABLE = "show_suspend_table";
    public static final String SHOW_THREAD_GROUPS = "thread_group";
    public static final String SHOW_SUSPENDED_THREADS_ONLY = "suspended_threads_only";
    
    public static final String PREF_SORT_ALPHABET = "sort.alphabet";
    public static final String PREF_SORT_NATURAL = "sort.natural";
    public static final String PREF_SORT_SUSPEND = "sort.suspend";
    public static final String PREF_SHOW_MONITORS = "show.monitors"; // NOI18N
    public static final String PREF_SHOW_SYSTEM_THREADS = "show.systemThreads";
    public static final String PREF_SHOW_THREAD_GROUPS = "show.threadGroups";
    public static final String PREF_SHOW_SUSPENDED_THREADS_ONLY = "show.suspendedThreadsOnly";
    public static final String PREF_SHOW_SUSPEND_TABLE = "show_suspend_table";
    public static final String PREF_SHOW_PACKAGE_NAMES = "show.packageNames";
    

    private static FiltersDescriptor instance;

    /** List of <Item> describing filters properties */
    private List<DVFilter> filters;

    private Action[] filterActions;
    
    private FiltersAccessor filtersAccessor;
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /** Creates a new instance of FiltersDescriptor */
    private FiltersDescriptor() {
        filters = Collections.emptyList();
    }

    public static synchronized FiltersDescriptor getInstance() {
        if (instance == null) {
            instance = new FiltersDescriptor();
            //instance = createDebuggingViewFilters();
        }
        return instance;
    }
    
    void setUpFilters(DVSupport dvs) {
        List<DVFilter> fs;
        if (dvs != null) {
            fs = filtersAccessor.getFilters(dvs);
        } else {
            fs = Collections.emptyList();
        }
        synchronized (this) {
            this.filters = fs;
            this.filterActions = null;
        }
        firePropertyChange("filters", null, null);
    }
    
    public synchronized Action[] getFilterActions() {
        if (filterActions == null) {
            if (filters.isEmpty()) {
                filterActions = new Action[] {};
                return filterActions;
            }
            List<Action> list = new ArrayList<Action>();
            for (DVFilter item : filters) {
                if (item.getGroup() != null) {
                    SortAction action = new SortAction(item);
                    list.add(action);
                }
            } // for
            int size = list.size();
            filterActions = new Action[size + 2];
            for (int x = 0; x < size; x++) {
                filterActions[x] = list.get(x);
            }
            filterActions[size] = null; // separator
            filterActions[size + 1] = new FilterSubmenuAction(this);
        } // if
        return filterActions;
    }

    public int getFilterCount() {
        return filters.size();
    }
    
    public String getName(int index) {
        return filters.get(index).getName();
    }
    
    public String getDisplayName(int index) {
        return filters.get(index).getDisplayName();
    }
    
    public String getTooltip(int index) {
        return filters.get(index).getTooltip();
    }
    
    public Icon getSelectedIcon(int index) {
        return filters.get(index).getIcon();
    }
    
    public boolean isSelected(int index) {
        return filters.get(index).isSelected();
    }

    public void setSelected(int index, boolean selected) {
        filters.get(index).setSelected(selected);
    }
    
    public void setSelected(String filterName, boolean selected) {
        for (DVFilter item : filters) {
            if (item.getName().equals(filterName)) {
                item.setSelected(selected);
                break;
            }
        }
    }
    
    public void connectToggleButton(int index, JToggleButton button) {
        FilterImpl impl = FiltersDescriptor.getInstance().filtersAccessor.getImpl(filters.get(index));
        impl.setToggleButton(button);
    }
    
    // **************************************************************************
    
    /*
    private static FiltersDescriptor createDebuggingViewFilters() {
        FiltersDescriptor desc = new FiltersDescriptor();
        desc.addItem(new Item(SHOW_SUSPENDED_THREADS_ONLY, getString("LBL_SUPSENDED_THREADS_ONLY"), getString("LBL_SUPSENDED_THREADS_ONLY_TIP"),
                false, loadIcon("show_suspended_threads_option_16.png")));
        desc.addItem(new Item(SHOW_THREAD_GROUPS, getString("LBL_THREAD_GROUPS"), getString("LBL_THREAD_GROUPS_TIP"),
                false, loadIcon("thread_group_mixed_16.png")));
        desc.addItem(new Item(SHOW_SUSPEND_TABLE, getString("LBL_SUSPEND_TABLE"), getString("LBL_SUSPEND_TABLE_TIP"),
                false, loadIcon("show_suspend_table_option_16.png")));
        desc.addItem(new Item(SHOW_SYSTEM_THREADS, getString("LBL_SYSTEM_THREADS"), getString("LBL_SYSTEM_THREADS_TIP"),
                false, loadIcon("show_system_threads_option_16.png")));
        desc.addItem(new Item(SHOW_MONITORS, getString("LBL_MONITORS"), getString("LBL_MONITORS_TIP"),
                false, loadIcon("monitor_acquired_16.png")));
        desc.addItem(new Item(SHOW_QUALIFIED_NAMES, getString("LBL_QUALIFIED_NAMES"), getString("LBL_QUALIFIED_NAMES_TIP"),
                false, loadIcon("show_fqn_option_16.png")));
        
        List<Item> groupMembers = new ArrayList<Item>();
        Group group = new Group();
        Item item;
        
        item = new Item(SUSPEND_SORT, getString("LBL_SUSPEND_SORT"), getString("LBL_SUSPEND_SORT_TIP"),
                false, loadIcon("suspend_property_sort_order_16.png"));
        groupMembers.add(item);
        desc.addItem(item);
        item.setGroup(group);
        
        item = new Item(ALPHABETIC_SORT, getString("LBL_ALPHABETIC_SORT"), getString("LBL_ALPHABETIC_SORT_TIP"),
                false, loadIcon("alphabetic_sort_order_16.png"));
        groupMembers.add(item);
        desc.addItem(item);
        item.setGroup(group);
        
        item = new Item(NATURAL_SORT, getString("LBL_NATURAL_SORT"), getString("LBL_NATURAL_SORT_TIP"),
                true, loadIcon("natural_sort_order_16.png"));
        groupMembers.add(item);
        desc.addItem(item);
        item.setGroup(group);
        
        group.setItems(groupMembers);
        return desc;
    }
    */

    // **************************************************************************
    
    /*private void addItem (Item newItem) {
        filters.add(newItem);
    }*/
    
    private static Icon loadIcon(String iconName) {
        return ImageUtilities.loadImageIcon("org/netbeans/modules/debugger/resources/debuggingView/" + iconName, false);
    }
    
    private static String getString(String label) {
        return NbBundle.getMessage(FiltersDescriptor.class, label);
    }

    public void setFiltersAccessor(FiltersAccessor filtersAccessor) {
        this.filtersAccessor = filtersAccessor;
    }
    
    private void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        pcs.firePropertyChange(propertyName, oldValue, newValue);
    }

    void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        pcs.addPropertyChangeListener(propertyChangeListener);
    }
    
    void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        pcs.removePropertyChangeListener(propertyChangeListener);
    }
    
    public static interface FiltersAccessor {
        public List<DVFilter> getFilters(DVSupport dvs);
        public FilterImpl getImpl(DVFilter filter);
    }
    
    // **************************************************************************
    //     filter Item
    // **************************************************************************
    public static class FilterImpl {
        private String name;
        private String displayName;
        private String tooltip;
        private Icon selectedIcon;
        private Preferences prefs;
        private String prefKey;
        private PreferenceChangeListener pchl;
        
        private boolean isSelected;
        private Group group;
        private Reference<JToggleButton> toggleButtonRef = new WeakReference<>(null);
        
        public static FilterImpl createDefault(DVFilter.DefaultFilter df) {
            FilterImpl fimpl;
            switch (df) {
                case showMonitors:
                    fimpl = new FilterImpl(SHOW_MONITORS, getString("LBL_MONITORS"),
                                           getString("LBL_MONITORS_TIP"),
                                           loadIcon("monitor_acquired_16.png"),
                                           getDefaultPreferences(),
                                           PREF_SHOW_MONITORS, false);
                    break;
                case showQualifiedNames:
                    fimpl = new FilterImpl(SHOW_QUALIFIED_NAMES,
                                           getString("LBL_QUALIFIED_NAMES"),
                                           getString("LBL_QUALIFIED_NAMES_TIP"),
                                           loadIcon("show_fqn_option_16.png"),
                                           getDefaultPreferences(),
                                           PREF_SHOW_PACKAGE_NAMES, false);
                    break;
                case showSuspendTable:
                    fimpl = new FilterImpl(SHOW_SUSPEND_TABLE,
                                           getString("LBL_SUSPEND_TABLE"),
                                           getString("LBL_SUSPEND_TABLE_TIP"),
                                           loadIcon("show_suspend_table_option_16.png"),
                                           getDefaultPreferences(),
                                           PREF_SHOW_SUSPEND_TABLE, true);
                    break;
                case showSuspendedThreadsOnly:
                    fimpl = new FilterImpl(SHOW_SUSPENDED_THREADS_ONLY,
                                           getString("LBL_SUPSENDED_THREADS_ONLY"),
                                           getString("LBL_SUPSENDED_THREADS_ONLY_TIP"),
                                           loadIcon("show_suspended_threads_option_16.png"),
                                           getDefaultPreferences(),
                                           PREF_SHOW_SUSPENDED_THREADS_ONLY, false);
                    break;
                case showSystemThreads:
                    fimpl = new FilterImpl(SHOW_SYSTEM_THREADS,
                                           getString("LBL_SYSTEM_THREADS"),
                                           getString("LBL_SYSTEM_THREADS_TIP"),
                                           loadIcon("show_system_threads_option_16.png"),
                                           getDefaultPreferences(),
                                           PREF_SHOW_SYSTEM_THREADS, false);
                    break;
                case showThreadGroups:
                    fimpl = new FilterImpl(SHOW_THREAD_GROUPS,
                                           getString("LBL_THREAD_GROUPS"),
                                           getString("LBL_THREAD_GROUPS_TIP"),
                                           loadIcon("thread_group_mixed_16.png"),
                                           getDefaultPreferences(),
                                           PREF_SHOW_THREAD_GROUPS, false);
                    break;
                case sortAlphabetic:
                    fimpl = new FilterImpl(ALPHABETIC_SORT,
                                           getString("LBL_ALPHABETIC_SORT"),
                                           getString("LBL_ALPHABETIC_SORT_TIP"),
                                           loadIcon("alphabetic_sort_order_16.png"),
                                           getDefaultPreferences(),
                                           PREF_SORT_ALPHABET, false);
                    break;
                case sortNatural:
                    fimpl = new FilterImpl(NATURAL_SORT,
                                           getString("LBL_NATURAL_SORT"),
                                           getString("LBL_NATURAL_SORT_TIP"),
                                           loadIcon("natural_sort_order_16.png"),
                                           getDefaultPreferences(),
                                           PREF_SORT_NATURAL, true);
                    break;
                case sortSuspend:
                    fimpl = new FilterImpl(SUSPEND_SORT,
                                           getString("LBL_SUSPEND_SORT"),
                                           getString("LBL_SUSPEND_SORT_TIP"),
                                           loadIcon("suspend_property_sort_order_16.png"),
                                           getDefaultPreferences(),
                                           PREF_SORT_SUSPEND, false);
                    break;
                default:
                    throw new IllegalArgumentException(df.name());
            }
            return fimpl;
        }
        
        public FilterImpl (String name, String displayName, String tooltip,
                           Icon selectedIcon, Preferences prefs, String prefKey,
                           boolean isSelectedDefault) {
            this.name = name;
            this.displayName = displayName;
            this.tooltip = tooltip;
            this.selectedIcon = selectedIcon;
            this.isSelected = isSelectedDefault;
            this.prefs= prefs;
            this.prefKey = prefKey;
            
            readValue();
        }

        private Group getGroup() {
            return group;
        }
        
        public void setGroup(Group group) {
            this.group = group;
        }

        public String getName() {
            return name;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getTooltip() {
            return tooltip;
        }

        public Icon getIcon() {
            return selectedIcon;
        }
        
        public Preferences getPreferences() {
            return prefs;
        }
        
        public String getPrefKey() {
            return prefKey;
        }

        public boolean isSelected() {
            return isSelected;
        }
        
        public void setSelected(boolean state) {
            if (isSelected == state) {
                return;
            }
            isSelected = state;
            setState(state);
            writeValue();
        }
        
        public void assureButtonSelected(boolean selected) {
            JToggleButton toggleButton = toggleButtonRef.get();
            if (toggleButton != null) {
                toggleButton.setSelected(selected);
            }
        }
        
        private void setState(boolean state) {
            JToggleButton toggleButton = toggleButtonRef.get();
            if (toggleButton != null) {
                toggleButton.setSelected(state);
            }
            if (state && group != null) {
                for (DVFilter item : group.getItems()) {
                    FilterImpl impl = FiltersDescriptor.getInstance().filtersAccessor.getImpl(item);
                    if (impl != this && item.isSelected()) {
                        item.setSelected(false);
                    } // if
                } // for
            } // if
        }

        public void setToggleButton(JToggleButton button) {
            toggleButtonRef = new WeakReference<JToggleButton>(button);
//            if (group != null && isSelected) {
//                toggleButton.setEnabled(false);
//            }
        }

        private void readValue() {
            isSelected = prefs.getBoolean(prefKey, isSelected);
            /*
            Preferences origPrefs = NbPreferences.root().node("org/netbeans/modules/debugger/jpda/ui").node("debugging");
            Preferences preferences = NbPreferences.forModule(getClass()).node("debugging"); // NOI18N
            if (name.equals(SHOW_SYSTEM_THREADS)) {
                isSelected = origPrefs.getBoolean(DebuggingTreeModel.SHOW_SYSTEM_THREADS, false);
                isSelected = preferences.getBoolean(DebuggingTreeModel.SHOW_SYSTEM_THREADS, isSelected);
            } else if (name.equals(SHOW_THREAD_GROUPS)) {
                isSelected = preferences.getBoolean(DebuggingTreeModel.SHOW_THREAD_GROUPS, false);
            } else if (name.equals(ALPHABETIC_SORT)) {
                isSelected = preferences.getBoolean(DebuggingTreeModel.SORT_ALPHABET, true);
            } else if (name.equals(SUSPEND_SORT)) {
                isSelected = preferences.getBoolean(DebuggingTreeModel.SORT_SUSPEND, false);
            } else if (name.equals(SHOW_SUSPENDED_THREADS_ONLY)) {
                isSelected = preferences.getBoolean(DebuggingTreeModel.SHOW_SUSPENDED_THREADS_ONLY, false);
            } else if (name.equals(NATURAL_SORT)) {
                isSelected = !preferences.getBoolean(DebuggingTreeModel.SORT_ALPHABET, true) &&
                        !preferences.getBoolean(DebuggingTreeModel.SORT_SUSPEND, false); // [TODO]
            } else if (name.equals(SHOW_MONITORS)) {
                isSelected = preferences.getBoolean(DebuggingMonitorModel.SHOW_MONITORS, false);
            } else if (name.equals(SHOW_QUALIFIED_NAMES)) {
                isSelected = preferences.getBoolean(DebuggingNodeModel.SHOW_PACKAGE_NAMES, false);
            } else if (name.equals(SHOW_SUSPEND_TABLE)){
                isSelected = preferences.getBoolean(SHOW_SUSPEND_TABLE, true);
            } else {
                isSelected = false;
            }*/
            if (pchl == null) {
                pchl = new PreferenceChangeListener() {
                    @Override
                    public void preferenceChange(PreferenceChangeEvent evt) {
                        boolean wasSelected = isSelected;
                        readValue();
                        final boolean newSelected = isSelected;
                        if (wasSelected != newSelected) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    setState(newSelected);
                                }
                            });
                            
                        }
                    }
                };
                prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, pchl, prefs));
            }
        }

        private void writeValue() {
            prefs.putBoolean(prefKey, isSelected);
            /*
            String keyName = null;
            Preferences preferences = NbPreferences.forModule(getClass()).node("debugging"); // NOI18N
            if (name.equals(SHOW_SYSTEM_THREADS)) {
                keyName = DebuggingTreeModel.SHOW_SYSTEM_THREADS;
            } else if (name.equals(SHOW_THREAD_GROUPS)) {
                keyName = DebuggingTreeModel.SHOW_THREAD_GROUPS;
            } else if (name.equals(SHOW_SUSPENDED_THREADS_ONLY)) {
                keyName = DebuggingTreeModel.SHOW_SUSPENDED_THREADS_ONLY;
            } else if (name.equals(ALPHABETIC_SORT)) {
                keyName = DebuggingTreeModel.SORT_ALPHABET;
            } else if (name.equals(SUSPEND_SORT)) {
                keyName = DebuggingTreeModel.SORT_SUSPEND;
            } else if (name.equals(SHOW_MONITORS)) {
                keyName = DebuggingMonitorModel.SHOW_MONITORS;
            } else if (name.equals(SHOW_QUALIFIED_NAMES)) {
                keyName = DebuggingNodeModel.SHOW_PACKAGE_NAMES;
            } else if (name.equals(SHOW_SUSPEND_TABLE)) {
                keyName = SHOW_SUSPEND_TABLE;
            }
            if (keyName != null) {
                preferences.putBoolean(keyName, isSelected);
            }*/
        }
        
        private static Preferences getDefaultPreferences() {
            final Preferences origPrefs = NbPreferences.root().node("org/netbeans/modules/debugger/jpda/ui").node("debugging");
            final Preferences preferences = NbPreferences.forModule(FilterImpl.class).node("debugging"); // NOI18N
            return new AbstractPreferences(null, "") {
                
                @Override
                protected void putSpi(String key, String value) {
                    preferences.put(key, value);
                }
                
                @Override
                protected String getSpi(String key) {
                    String value = preferences.get(key, null);
                    if (value == null) {
                        value = origPrefs.get(key, null);
                    }
                    return value;
                }
                
                @Override
                protected void removeSpi(String key) {}
                
                @Override
                protected void removeNodeSpi() throws BackingStoreException {}
                
                @Override
                protected String[] keysSpi() throws BackingStoreException {
                    return preferences.keys();
                }
                
                @Override
                protected String[] childrenNamesSpi() throws BackingStoreException {
                    return preferences.childrenNames();
                }
                
                @Override
                protected AbstractPreferences childSpi(String name) {
                    return null;
                }
                
                @Override
                protected void syncSpi() throws BackingStoreException {
                    origPrefs.sync();
                    preferences.sync();
                }
                
                @Override
                protected void flushSpi() throws BackingStoreException {
                    preferences.flush();
                }

                @Override
                public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
                    preferences.addPreferenceChangeListener(pcl);
                }

                @Override
                public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
                    preferences.removePreferenceChangeListener(pcl);
                }
                
            };
        }
        
    }
    
    // **************************************************************************
    //     Group of Items
    // **************************************************************************
    /*static class Group {
        List<Item> items = Collections.EMPTY_LIST;
        
        public void setItems(List<Item> items) {
            this.items = items;
            for (Item item : items) {
                item.setGroup(this);
            }
        }
        
        public List<Item> getItems() {
            return items;
        }
        
    }*/
    
    // **************************************************************************
    //     Filter Actions Support
    // **************************************************************************
    
    private static final class SortAction extends AbstractAction implements Presenter.Popup {
    
        private DVFilter filterItem;

        /** Creates a new instance of SortByNameAction */
        SortAction (DVFilter item) {
            this.filterItem = item;
            String displayName = item.getDisplayName();
            int i = Mnemonics.findMnemonicAmpersand(displayName);
            if (i >= 0) {
                displayName = displayName.substring(0, i) + displayName.substring(i+1);
            }
            putValue(Action.NAME, displayName);
            putValue(Action.SMALL_ICON, item.getIcon());
        }

        public final JMenuItem getPopupPresenter() {
            JMenuItem result = obtainMenuItem();
            return result;
        }

        protected final JRadioButtonMenuItem obtainMenuItem () {
            JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem();
            Mnemonics.setLocalizedText(menuItem, filterItem.getDisplayName());
            menuItem.setAction(this);
            menuItem.addHierarchyListener(new ParentChangeListener(menuItem));
            menuItem.setSelected(filterItem.isSelected());
            return menuItem;
        }

        public void actionPerformed(ActionEvent e) {
            filterItem.setSelected(!filterItem.isSelected());
        }

        private class ParentChangeListener implements HierarchyListener {

            private JRadioButtonMenuItem menuItem;

            public ParentChangeListener(JRadioButtonMenuItem menuItem) {
                this.menuItem = menuItem;
            }

            public void hierarchyChanged(HierarchyEvent e) {
                JComponent parent = (JComponent) e.getChangedParent();
                if (parent == null) {
                    return ;
                }
                ButtonGroup group = (ButtonGroup) parent.getClientProperty(getClass().getName()+" buttonGroup");
                if (group == null) {
                    group = new ButtonGroup();
                }
                group.add(menuItem);
                menuItem.removeHierarchyListener(this);
            }

        }

    }

    static final class FilterSubmenuAction extends AbstractAction implements Presenter.Popup {
    
        private static final String PROP_FILTER_NAME = "nbFilterName";

        private FiltersDescriptor filtersDesc;

        public FilterSubmenuAction(FiltersDescriptor filters) {
            this.filtersDesc = filters;
        }

        public void actionPerformed(ActionEvent ev) {
            Object source = ev.getSource();
            // react just on submenu items, not on submenu click itself
            if (source instanceof JCheckBoxMenuItem) {
                JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)source;
                String filterName = (String)(menuItem.getClientProperty(PROP_FILTER_NAME));
                filtersDesc.setSelected(filterName, menuItem.isSelected());
            }
        }

        public final JMenuItem getPopupPresenter() {
            return createSubmenu();
        }

        private JMenuItem createSubmenu () {
            JMenuItem menu = new JMenu();
            Mnemonics.setLocalizedText(menu, NbBundle.getMessage(FiltersDescriptor.class, "LBL_FilterSubmenu"));
            JMenuItem menuItem;
            String filterName;
            for (DVFilter item : filtersDesc.filters) {
                if (item.getGroup() != null) {
                    continue;
                }
                filterName = item.getName();
                menuItem = new JCheckBoxMenuItem(item.getDisplayName(), item.isSelected());
                Mnemonics.setLocalizedText(menuItem, item.getDisplayName());
                menuItem.addActionListener(this);
                menuItem.putClientProperty(PROP_FILTER_NAME, filterName);
                menu.add(menuItem);
            }
            return menu;
        }

    }
    
}
