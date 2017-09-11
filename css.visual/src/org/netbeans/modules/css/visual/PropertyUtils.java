/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.visual;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.netbeans.modules.css.model.api.Declaration;
import org.netbeans.modules.css.model.api.PropertyDeclaration;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 *
 * @author marekfukala
 */
public class PropertyUtils {

    private static final Comparator<PropertyDeclaration> DECLARATIONS_COMPARATOR = new DeclarationsComparator();
    
    private static final Comparator<PropertyDefinition> PROPERTY_DEFINITIONS_COMPARATOR = new Comparator<PropertyDefinition>() {
        @Override
        public int compare(PropertyDefinition pd1, PropertyDefinition pd2) {
            String pd1name = pd1.getName();
            String pd2name = pd2.getName();

            //sort the vendor spec. props below the common ones
            boolean d1vendor = Properties.isVendorSpecificPropertyName(pd1name);
            boolean d2vendor = Properties.isVendorSpecificPropertyName(pd2name);

            if (d1vendor && !d2vendor) {
                return +1;
            } else if (!d1vendor && d2vendor) {
                return -1;
            }

            return pd1name.compareTo(pd2name);
        }
    };
    
    public static List<PropertyDeclaration> getPropertyDeclarations(Rule rule) {
        List<PropertyDeclaration> pds = new ArrayList<>();
        Declarations declarations = rule.getDeclarations();
        if(declarations == null) {
            return Collections.emptyList();
        }
        for(Declaration d : declarations.getDeclarations()) {
            PropertyDeclaration propertyDeclaration = d.getPropertyDeclaration();
            if(propertyDeclaration != null) {
                pds.add(propertyDeclaration);
            }
        }
        return pds;
    }
    
    /**
     * Returns an unique id of the property within current rule.
     *
     * Format of the ID:
     *
     * property name_S_D
     *
     * Where: "S" is the property index within the rule "D" is the number of the
     * property if there are more properties of same name
     *
     * Example:
     *
     * div { color: red; // color_0 font: courier; // font_1 color: green; //
     * color_2_1 }
     *
     * @param property
     */
    public static String getDeclarationId(Rule rule, PropertyDeclaration declaration) {
        assert rule.getModel() == declaration.getModel() 
                : String.format("The 'rule' and 'declaration' arguments are not elements "
                + "of the same model instance! %s.getModel()=%s, %s.getModel()=%s", 
                rule, rule.getModel(), declaration, declaration.getModel()); //NOI18N

        CharSequence searched = declaration.getProperty().getContent();
        Collection<PropertyDeclaration> declarations = getPropertyDeclarations(rule);

        int identityIndex = -1;
        int index = -1;
        for (PropertyDeclaration d : declarations) {
            index++;
            CharSequence propName = d.getProperty().getContent();
            if (LexerUtils.equals(searched, propName, false, false)) {
                identityIndex++;
            }
            if (d == declaration) {
                break;
            }
        }
        assert identityIndex >= 0;
        StringBuilder b = new StringBuilder();
        b.append(searched);
        b.append('_');
        b.append(index);
        if (identityIndex > 0) {
            b.append('_');
            b.append(identityIndex);
        }
        return b.toString();
    }
    
    static Comparator<PropertyDefinition> getPropertyDefinitionsComparator() {
        return PROPERTY_DEFINITIONS_COMPARATOR;
    }
    
    static Comparator<PropertyDeclaration> getDeclarationsComparator() {
        return DECLARATIONS_COMPARATOR;
    }
    
    /**
     * Creates a comparator for {@link Declaration} which allows to put some items
     * at the end of the list keeping their natural order.
     * 
     */
    static Comparator<PropertyDeclaration> createDeclarationsComparator(Rule rule, List<String> extraDeclarationsIds) {
        return new ExtDeclarationsComparator(rule, extraDeclarationsIds);
    }
    
    private static class ExtDeclarationsComparator extends DeclarationsComparator {
        
        private Rule rule;
        private List<String> extraDeclarationsIds;

        public ExtDeclarationsComparator(Rule rule, List<String> extraDeclarationsIds) {
            this.rule = rule;
            this.extraDeclarationsIds = extraDeclarationsIds;
        }

        @Override
        public int compare(PropertyDeclaration d1, PropertyDeclaration d2) {
            String d1Id = PropertyUtils.getDeclarationId(rule, d1);
            String d2Id = PropertyUtils.getDeclarationId(rule, d2);
            
            boolean d1Extra = extraDeclarationsIds.contains(d1Id);
            boolean d2Extra = extraDeclarationsIds.contains(d2Id);
            
            if (d1Extra && !d2Extra) {
                return +1;
            } else if (!d1Extra && d2Extra) {
                return -1;
            } else if(d1Extra && d2Extra) {
                //both extra items => compare according to the natural order in the list
                int d1Index = extraDeclarationsIds.indexOf(d1Id);
                int d2Index = extraDeclarationsIds.indexOf(d2Id);
                
                return d1Index - d2Index;
                
            } else {
                return super.compare(d1, d2);
            }
            
        }
        
    }

    private static class DeclarationsComparator implements Comparator<PropertyDeclaration> {
        
        @Override
        public int compare(PropertyDeclaration d1, PropertyDeclaration d2) {
            String d1Name = d1.getProperty().getContent().toString();
            String d2Name = d2.getProperty().getContent().toString();

            //sort the vendor spec. props below the common ones
            boolean d1vendor = Properties.isVendorSpecificPropertyName(d1Name);
            boolean d2vendor = Properties.isVendorSpecificPropertyName(d2Name);

            if (d1vendor && !d2vendor) {
                return +1;
            } else if (!d1vendor && d2vendor) {
                return -1;
            }

            return d1Name.compareTo(d2Name);
        }
    };
}
