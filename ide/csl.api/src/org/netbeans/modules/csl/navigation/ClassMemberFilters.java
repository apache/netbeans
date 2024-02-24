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

package org.netbeans.modules.csl.navigation;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.navigation.ElementNode.Description;
import org.netbeans.modules.csl.navigation.base.FiltersDescription;
import org.netbeans.modules.csl.navigation.base.FiltersManager;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 * <p>
 * Creates filtering for the ClassMemberPanel
 *
 * @author phrebejk
 */
public final class ClassMemberFilters {
    
    private ClassMemberPanelUI ui;
    
    /** constants for defined filters */
    private static final String SHOW_NON_PUBLIC = "show_non_public";
    private static final String SHOW_STATIC = "show_static";
    private static final String SHOW_FIELDS = "show_fields";
    private static final String SHOW_INHERITED = "show_inherited";
    
    private FiltersManager filters;
    
    private boolean naturalSort = false;
    
    //issue #132883 workaround
    public boolean disableFiltering = false;
    
    /** Creates a new instance of ClassMemberFilters */
    ClassMemberFilters( ClassMemberPanelUI ui ) {
        this.ui = ui;
    }
    
    public FiltersManager getInstance() {
        if (filters == null) {
            filters = createFilters();
        }
        return filters;
    }
    
    public JComponent getComponent() {        
        FiltersManager f = getInstance();                        
        return f.getComponent();
        
    }
    
    public Collection<StructureItem> filter(List<StructureItem> original) {
        boolean non_public = filters.isSelected(SHOW_NON_PUBLIC);
        boolean statik = filters.isSelected(SHOW_STATIC);
        boolean fields = filters.isSelected(SHOW_FIELDS);
        boolean inherited = filters.isSelected(SHOW_INHERITED);
        
        if (original == null || original.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        List<StructureItem> result = new ArrayList<StructureItem>(original.size());
      
        if (!disableFiltering) {
            for (StructureItem description : original) {

                // I default no access modifier to mean public, unlike the Java version where it means package protected
                if (!non_public &&
                        (description.getModifiers().contains(Modifier.PROTECTED) || description.getModifiers().contains(Modifier.PRIVATE))) {
//            if ( !non_public && 
//                 !description.getModifiers().contains(Modifier.PUBLIC)                 
//                 /* Fix for #89777 && !description.modifiers.contains(Modifier.PROTECTED) */ ) {
                    continue;
                }

                if (!statik && description.getModifiers().contains(Modifier.STATIC)) {
                    continue;
                }

                if (!fields && (description.getKind() == ElementKind.FIELD || (description.getKind() == ElementKind.ATTRIBUTE))) {
                    continue;
                }

                // Inherited members
                if (!inherited && StructureItem.isInherited(description)) {
                    continue;
                }

                result.add(description);
            }
        } else {
            result.addAll(original);
        }

        result.sort(isNaturalSort() ? Description.POSITION_COMPARATOR : Description.ALPHA_COMPARATOR);

        return result;
    }
    
    public boolean isNaturalSort() {
        return naturalSort;        
    }
    
    public void setNaturalSort( boolean naturalSort ) {
        this.naturalSort = naturalSort;
        ui.sort();
    }
    
    // Privare methods ---------------------------------------------------------
    
    /** Creates filter descriptions and filters itself */
    private static FiltersManager createFilters () {
        FiltersDescription desc = new FiltersDescription();
        
        desc.addFilter(SHOW_INHERITED,
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowInherited"),     //NOI18N
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowInheritedTip"),     //NOI18N
                false,
                new ImageIcon (ImageUtilities.loadImage("org/netbeans/modules/csl/navigation/resources/filterHideInherited.png")), //NOI18N
                null
        );
        desc.addFilter(SHOW_FIELDS,
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowFields"),     //NOI18N
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowFieldsTip"),     //NOI18N
                true,
                new ImageIcon (ImageUtilities.loadImage("org/netbeans/modules/csl/navigation/resources/filterHideFields.gif")), //NOI18N
                null
        );
        desc.addFilter(SHOW_STATIC,
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowStatic"),     //NOI18N
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowStaticTip"),     //NOI18N
                true,
                new ImageIcon (ImageUtilities.loadImage("org/netbeans/modules/csl/navigation/resources/filterHideStatic.png")), //NOI18N
                null
        );
        desc.addFilter(SHOW_NON_PUBLIC,
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowNonPublic"),     //NOI18N
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowNonPublicTip"),     //NOI18N
                true,
                new ImageIcon (ImageUtilities.loadImage("org/netbeans/modules/csl/navigation/resources/filterHideNonPublic.png")), //NOI18N
                null
        );
        
        return FiltersDescription.createManager(desc);
    }
    
    
        
}
