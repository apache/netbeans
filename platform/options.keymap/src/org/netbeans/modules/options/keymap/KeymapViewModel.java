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

package org.netbeans.modules.options.keymap;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.Task;

/**
 *
 * @author Jan Jancura
 * @author Max Sauer
 */
class KeymapViewModel extends DefaultTableModel implements Runnable {
    private KeymapModel         model = new KeymapModel ();
    
    private MutableShortcutsModel   mutableModel;

    private String currentProfile;
    static final ActionsComparator actionsComparator = new ActionsComparator ();
    private String searchText = "";
    
    
    /** 
     * Creates a new instance of KeymapModel 
     */
    public KeymapViewModel () {
        super(new String[]{
                    NbBundle.getMessage(KeymapViewModel.class, "ActionsColumnName"), //NOI18N
                    NbBundle.getMessage(KeymapViewModel.class, "ShortcutColumnName"), //NOI18N
                    NbBundle.getMessage(KeymapViewModel.class, "CategoryColumnName"), //NOI18N
//                    NbBundle.getMessage(KeymapViewModel.class, "ScopeColumnName") //NOI18N
                }, 0);
        mutableModel = new MutableShortcutsModel(model, null);
        //currentProfile = model.getCurrentProfile ();
    }
    
    MutableShortcutsModel getMutableModel() {
        return mutableModel;
    }
    
    private volatile Task   initTask;
    
    void update() {
        postUpdate();
    }

    /**
     * Refreshes the table model, after task that computed the data
     * finishes.
     */
    @Override
    public void run() {
        update0();
    }
    
    private void scheduleUpdate() {
        if (SwingUtilities.isEventDispatchThread()) {
            run();
        } else {
            SwingUtilities.invokeLater(this);
        }
    }
    
    public Task postUpdate() {
        Task t = initTask;
        if (t != null && t.isFinished()) {
            scheduleUpdate();
            return t;
        }
        if (t == null) {
            return initTask = KeymapModel.RP.post(new Runnable() {
                public void run() {
                    // just initialize
                    mutableModel.getCategories();
                    mutableModel.getItems("");

                    scheduleUpdate();
                }
            });
        } else if (t.isFinished()) {
            scheduleUpdate();
        }
        return t;
    }
    
    // DefaultTableModel
    @Override
    public Class getColumnClass(int columnIndex) {
        switch(columnIndex) {
            case 0:
                return ActionHolder.class;
            default:
                return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1) //shotcuts cells editable
            return true;
        else
            return false;
    }

    
    void setSearchText(String searchText) {
        this.searchText = searchText;
    }
    
    // ListModel ...............................................................

    // Map (String ("xx/yy") > Map ...)
    private Map<String, List<String>> categories;
    
    
    /**
     * Returns map of categories and subcategories.
     * Root: getCategories ().get ("")
     * Subcategories: getCategories ().get (category)
     *
     * Map (String (category name) > List (String (category name))).
     */
    public Map<String, List<String>> getCategories () {
        if (categories == null) {
            categories = new TreeMap<String, List<String>> ();
            List<String> c = new ArrayList<String> (model.getActionCategories ());
            Collections.sort (c);
            for (String cn: c) {
                String folderName = "";
                StringTokenizer st = new StringTokenizer (cn, "/");
                while (st.hasMoreTokens ()) {
                    String name = st.nextToken ();
                    List<String> asd = categories.get (folderName);
                    if (asd == null) {
                        asd = new ArrayList<String> ();
                        categories.put (folderName, asd);
                    }
                    folderName = folderName.length () == 0 ?
                        name : folderName + '/' + name;
                    if (asd.isEmpty () || 
                        !asd.get (asd.size () - 1).equals (folderName)
                    )
                        asd.add (folderName);
                }
            }
        }
        return categories;
    }
    
    private boolean supressDataEvents;

    @Override
    public void fireTableDataChanged() {
        if (!supressDataEvents) {
            super.fireTableDataChanged();
        }
    }

    @Override
    public void fireTableRowsInserted(int firstRow, int lastRow) {
        if (!supressDataEvents) {
            super.fireTableRowsInserted(firstRow, lastRow);
        }
    }

    @Override
    public void fireTableRowsDeleted(int firstRow, int lastRow) {
        if (!supressDataEvents) {
            super.fireTableRowsDeleted(firstRow, lastRow);
        }
    }

    @Override
    public void fireTableChanged(TableModelEvent e) {
        if (!supressDataEvents) {
            super.fireTableChanged(e);
        }
    }
    
    

    
    // other methods ...........................................................

    private void update0() {
        boolean caseSensitiveSearch = false;
        String searchTxt;

        if (searchText.matches(".*[A-Z].*")) { //NOI18N
            caseSensitiveSearch = true;
            searchTxt = searchText;
        } else {
            searchTxt = searchText.toLowerCase();
        }

        supressDataEvents = true;
        getDataVector().removeAllElements();

        for (List<String> categories : getCategories().values()) {
            for (String category : categories) {
                for (Object o : mutableModel.getItems(category, false)) {
                    ShortcutAction sca = (ShortcutAction) o;
                    String[] shortcuts = mutableModel.getShortcuts(sca);
                    String displayName = sca.getDisplayName();
                    if (displayName.isEmpty()) {
                        continue;
                    }
//                        System.out.println("### " + sca.getDisplayName() + " " + searched(displayName.toLowerCase()));
                    if (searched(caseSensitiveSearch ? displayName : displayName.toLowerCase(), searchTxt)) {
                        if (shortcuts.length == 0)
                            addRow(new Object[]{new ActionHolder(sca, false), "", category}); // NOI18N
                        else
                            for (int i = 0; i < shortcuts.length; i++) {
                                String shortcut = shortcuts[i];
//                                    String shownDisplayName = i == 0 ? displayName : displayName + " (alternative shortcut)";
                                addRow(new Object[]{
                                            i == 0 ? new ActionHolder(sca, false) : new ActionHolder(sca, true),
                                            shortcut, category,
                                        });
                            }
                    }
                }
            }
        }
        supressDataEvents = false;
        fireTableDataChanged();
    }

    private boolean searched(String displayName, String searchText) {
        if (displayName.length() == 0 || displayName.startsWith(searchText) || displayName.contains(searchText))
            return true;
        else
            return false;
    }

    // innerclasses ............................................................

    static class ActionsComparator implements Comparator {
        
        public int compare (Object o1, Object o2) {
            if (o1 instanceof String)
                if (o2 instanceof String)
                    return ((String) o1).compareTo ((String) o2);
                else
                    return 1;
            else
                if (o2 instanceof String)
                    return -1;
                else
                    return ((ShortcutAction) o1).getDisplayName ().compareTo (
                        ((ShortcutAction) o2).getDisplayName ()
                    );
        }
    }
    
    void runWithoutEvents(Runnable r) {
        try {
            supressDataEvents = true;
            r.run();
        } finally {
            supressDataEvents = false;
        }
    }
}
