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

package org.netbeans.modules.csl.navigation.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.modules.csl.navigation.ClassMemberFilters;
import org.netbeans.modules.csl.navigation.base.FiltersDescription;
import org.netbeans.modules.csl.navigation.base.FiltersManager;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 * <p>
 * 
 * Popup submenu consisting of boolean state filters
 *
 * @author Dafe Simonek
 */
public final class FilterSubmenuAction extends AbstractAction implements Presenter.Popup {
    
    private static final String PROP_FILTER_NAME = "nbFilterName";
    /** access to filter manager */        
    private ClassMemberFilters filters;
    
    /** Creates a new instance of FilterSubmenuAction */
    public FilterSubmenuAction(ClassMemberFilters filters) {
        this.filters = filters;
    }
    
    public void actionPerformed(ActionEvent ev) {
        Object source = ev.getSource();
        // react just on submenu items, not on submenu click itself
        if (source instanceof JCheckBoxMenuItem) {
            JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)source;
            String filterName = (String)(menuItem.getClientProperty(PROP_FILTER_NAME));
            filters.getInstance().setSelected(filterName, menuItem.isSelected());
        }
    }
    
    public final JMenuItem getPopupPresenter() {
        return createSubmenu();
    }
    
    private JMenuItem createSubmenu () {
        if (filters.disableFiltering) {
            return null;
        }
        FiltersDescription filtersDesc = filters.getInstance().getDescription();
        JMenuItem menu = new JMenu(NbBundle.getMessage(FilterSubmenuAction.class, "LBL_FilterSubmenu")); //NOI18N
        JMenuItem menuItem = null;
        String filterName = null;
        for (int i = 0; i < filtersDesc.getFilterCount(); i++) {
            filterName = filtersDesc.getName(i);
            menuItem = new JCheckBoxMenuItem(
                    filtersDesc.getDisplayName(i), filters.getInstance().isSelected(filterName));
            menuItem.addActionListener(this);
            menuItem.putClientProperty(PROP_FILTER_NAME, filterName);
            menu.add(menuItem);
        }
        return menu;
    }
    
    
}
