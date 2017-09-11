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

package org.netbeans.modules.csl.navigation.base;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 * <p>
 * 
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
