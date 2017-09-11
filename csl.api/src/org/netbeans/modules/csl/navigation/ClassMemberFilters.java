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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
        // XXX Enable later boolean inherited = filters.isSelected(SHOW_INHERITED);
        
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

                // XXX Inherited members

                result.add(description);
            }
        } else {
            result.addAll(original);
        }

        Collections.sort(result, isNaturalSort() ? Description.POSITION_COMPARATOR : Description.ALPHA_COMPARATOR);

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
        
        // XXX to be enabled later
//        desc.addFilter(SHOW_INHERITED,
//                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowInherited"),     //NOI18N
//                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowInheritedTip"),     //NOI18N
//                false,
//                new ImageIcon (Utilities.loadImage("org/netbeans/modules/csl/navigation/resources/filterHideInherited.png")), //NOI18N
//                null
//        );
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
