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

package org.netbeans.modules.java.navigation;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.modules.java.navigation.ElementNode.Description;
import org.netbeans.modules.java.navigation.base.Filters;
import org.netbeans.modules.java.navigation.base.FiltersDescription;
import org.netbeans.modules.java.navigation.base.FiltersManager;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/** Creates filtering for the ClassMemberPanel
 *
 * @author phrebejk
 */
public final class ClassMemberFilters extends Filters<Description> {
    
    private ClassMemberPanelUI ui;
    
    /** constants for defined filters */
    private static final String SHOW_NON_PUBLIC = "show_non_public";    //NOI18N
    private static final String SHOW_STATIC = "show_static";            //NOI18N
    private static final String SHOW_FIELDS = "show_fields";            //NOI18N
    private static final String SHOW_INNER_CLASSES = "show_inner_classes";  //NOI18N
    private static final String SHOW_INHERITED = "show_inherited";      //NOI18N
    
    
    /** Creates a new instance of ClassMemberFilters */
    ClassMemberFilters( ClassMemberPanelUI ui ) {
        this.ui = ui;        
    }    
    
    @Override
    public Collection<Description> filter( Collection<? extends Description> original ) {
        final FiltersManager fm = getFiltersManager();
        final boolean non_public = fm.isSelected(SHOW_NON_PUBLIC);
        final boolean inner = fm.isSelected(SHOW_INNER_CLASSES);
        final boolean statik = fm.isSelected(SHOW_STATIC);
        final boolean fields = fm.isSelected(SHOW_FIELDS);
        final boolean inherited = fm.isSelected(SHOW_INHERITED);
        
        ArrayList<Description> result = new ArrayList<Description>(original.size());
        for (Description description : original) {
            
            if ( !inherited && description.isInherited ) {
                continue;
            }
            if ( !non_public &&
                 !description.modifiers.contains(Modifier.PUBLIC) &&
                 !description.isTopLevel) {
                continue;
            }
            
            if ( !statik && description.modifiers.contains(Modifier.STATIC)) {
                continue;
            }
            
            if ( !fields && description.kind == ElementKind.FIELD ) {
                continue;
            }

            if (!inner && isInnerClass(description)) {
                continue;
            }
            
            // XXX Inherited members
            
            result.add(description);                        
        }
                
        result.sort(isNaturalSort() ?  Description.POSITION_COMPARATOR : Description.ALPHA_COMPARATOR );
        
        return result;
    }
            
    @Override
    public void sortUpdated() {        
        ui.sort();
    }

    @Override
    protected void fqnUpdated() {
        ui.refresh();
    }
    
    // Privare methods ---------------------------------------------------------
    
    /** Creates filter descriptions and filters itself */
    @Override
    protected final FiltersManager createFilters () {
        FiltersDescription desc = new FiltersDescription();
        
        desc.addFilter(SHOW_INHERITED,
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowInherited"),     //NOI18N
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowInheritedTip"),     //NOI18N
                false, ImageUtilities.loadImageIcon("org/netbeans/modules/java/navigation/resources/filterHideInherited.png", false), //NOI18N
                null
        );
        desc.addFilter(SHOW_FIELDS,
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowFields"),     //NOI18N
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowFieldsTip"),     //NOI18N
                true, ImageUtilities.loadImageIcon("org/netbeans/modules/java/navigation/resources/filterHideFields.png", false), //NOI18N
                null
        );        
        desc.addFilter(SHOW_STATIC,
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowStatic"),     //NOI18N
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowStaticTip"),     //NOI18N
                true, ImageUtilities.loadImageIcon("org/netbeans/modules/java/navigation/resources/filterHideStatic.png", false), //NOI18N
                null
        );
        desc.addFilter(SHOW_NON_PUBLIC,
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowNonPublic"),     //NOI18N
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowNonPublicTip"),     //NOI18N
                true, ImageUtilities.loadImageIcon("org/netbeans/modules/java/navigation/resources/filterHideNonPublic.png", false), //NOI18N
                null
        );
        desc.addFilter(SHOW_INNER_CLASSES,
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowInnerClasses"),     //NOI18N
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowInnerClassesTip"),     //NOI18N
                true,
                ElementIcons.getElementIcon(ElementKind.CLASS, Collections.<Modifier>emptySet()),
                null
        );
        return FiltersDescription.createManager(desc);
    }

    private static boolean isInnerClass(@NonNull Description desc) {
        return (desc.kind.isClass() || desc.kind.isInterface()) &&
            !desc.isTopLevel;
    }
}
