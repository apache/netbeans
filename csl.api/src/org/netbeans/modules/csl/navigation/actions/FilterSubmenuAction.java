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
