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

package org.netbeans.modules.refactoring.java.ui.elements;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;

/**
 * @author Dafe Simonek
 */
public final class FiltersDescription {

    public static FiltersManager createManager (FiltersDescription descr) {
        return FiltersManager.create(descr);
    }

    /** List of <FilterItem> describing filters properties */
    private List<FilterItem> filters;

    /** Creates a new instance of FiltersDescription */
    public FiltersDescription() {
        filters = new ArrayList<FilterItem>();
    }
    
    public void addFilter (String name, String displayName, String tooltip,
            boolean isSelected, Icon selectedIcon, Icon unselectedIcon) {
        FilterItem newItem = new FilterItem(name, displayName, tooltip, 
                isSelected, selectedIcon, unselectedIcon);
        filters.add(newItem);
    }
    
    public int getFilterCount () {
        return filters.size();
    }
    
    public String getName (int index) {
        return filters.get(index).name;
    }
    
    public String getDisplayName (int index) {
        return filters.get(index).displayName;
    }
    
    public String getTooltip (int index) {
        return filters.get(index).tooltip;
    }
    
    public Icon getSelectedIcon (int index) {
        return filters.get(index).selectedIcon;
    }
    
    public Icon getUnselectedIcon (int index) {
        return filters.get(index).unselectedIcon;
    }
    
    public boolean isSelected (int index) {
        return filters.get(index).isSelected;
    }
    
    static class FilterItem {
        String name;
        String displayName;
        String tooltip;
        Icon selectedIcon;
        Icon unselectedIcon;
        boolean isSelected;
        
        FilterItem (String name, String displayName, String tooltip,
                boolean isSelected, Icon selectedIcon, Icon unselectedIcon) {
            this.name = name;
            this.displayName = displayName;
            this.tooltip = tooltip;
            this.selectedIcon = selectedIcon;
            this.unselectedIcon = unselectedIcon;
            this.isSelected = isSelected;
        }
        
    }
    
}
