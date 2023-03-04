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
