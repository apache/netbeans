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
package org.netbeans.modules.css.lib.api.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.openide.filesystems.FileObject;

/**
 * @author marekfukala
 */
public class Properties {

    private Properties() {
    }

    /**
     * Gets all property names available for the given context.
     *
     * @param context file context
     * @return
     */
    public static Collection<String> getPropertyNames(FileObject context) {
        return PropertyDefinitionProvider.Query.getPropertyNames(context);
    }

    /**
     * Get instance of {@link PropertyDefinition} for the given property name
     * and given context.
     *
     * @param propertyName property name
     * @return instance of {@link PropertyDefinition} or null if not found.
     */
    public static PropertyDefinition getPropertyDefinition(String propertyName) {
        return PropertyDefinitionProvider.Query.getPropertyDefinition(propertyName);
    }

//    /**
//     * Get instance of {@link PropertyDefinition} for the given property name with no context.
//     * 
//     * @param propertyName property name
//     * @return instance of {@link PropertyDefinition} or null if not found.
//     */
//    public static PropertyDefinition getPropertyDefinition(String propertyName) {
//        return PropertyDefinitionProvider.Query.getPropertyDefinition(null, propertyName);
//    }
    /**
     * Get instance of {@link PropertyDefinition} for the given property name
     * and given context
     *
     * Basically does the same as {@link #getPropertyDefinition(java.lang.String)
     * but it tries to resolve the refered element name with the at-sign prefix first so
     * the property appearance may contain link to appearance, which in fact
     * will be resolved as the
     *
     * @appearance property:
     *
     * appearance=<appearance> |normal
     * @appearance=...
     *
     * @param context file context
     * @param propertyName property name
     * @return instance of {@link PropertyDefinition} or null if not found.
     */
    public static PropertyDefinition getPropertyDefinition(String propertyName, boolean preferInvisibleProperties) {
        StringBuilder sb = new StringBuilder().append(GrammarElement.INVISIBLE_PROPERTY_PREFIX).append(propertyName);
        PropertyDefinition invisibleProperty = getPropertyDefinition(sb.toString());
        return preferInvisibleProperties && invisibleProperty != null ? invisibleProperty : getPropertyDefinition(propertyName);
    }

//    /**
//     * Get instance of {@link PropertyDefinition} for the given property name with no context
//     * 
//     * @param propertyName
//     * @param preferInvisibleProperties
//     * @return 
//     */
//    public static PropertyDefinition getPropertyDefinition(String propertyName, boolean preferInvisibleProperties) {
//        StringBuilder sb = new StringBuilder().append(GrammarElement.INVISIBLE_PROPERTY_PREFIX).append(propertyName);
//        PropertyDefinition invisibleProperty = getPropertyDefinition(null, sb.toString());
//        return preferInvisibleProperties && invisibleProperty != null ? invisibleProperty : getPropertyDefinition(null, propertyName);
//    }
    /**
     * Gets all available {@link PropertyDefinition}s for the given context
     *
     * @param context file context
     */
    public static Collection<PropertyDefinition> getPropertyDefinitions(FileObject context) {
        return getPropertyDefinitions(context, false);
    }

    public static Collection<PropertyDefinition> getPropertyDefinitions(FileObject context, boolean visibleOnly) {
        Collection<PropertyDefinition> all = new ArrayList<>();
        for (String propName : getPropertyNames(context)) {
            PropertyDefinition propertyDefinition = getPropertyDefinition(propName);
            if (!visibleOnly || isVisibleProperty(propertyDefinition)) {
                all.add(propertyDefinition);
            }
        }
        return all;
    }

    public static boolean isVisibleProperty(PropertyDefinition propertyDefinition) {
        char c = propertyDefinition.getName().charAt(0);
        return c != '@';
    }

    public static boolean isVendorSpecificProperty(PropertyDefinition propertyDefinition) {
        return isVendorSpecificPropertyName(propertyDefinition.getName());
    }

    public static boolean isVendorSpecificPropertyName(String propertyName) {
        char c = propertyName.charAt(0);
        return c == '_' || c == '-';
    }

    /**
     * Returns true if the given property have some visible sub-properties.
     * 
     * @since 1.19
     * 
     * @param context
     * @param propertyDefinition
     * @return 
     */
    public static boolean isAggregatedProperty(FileObject context, PropertyDefinition propertyDefinition) {
        final GroupGrammarElement grammarElement = propertyDefinition.getGrammarElement(context);
        final AtomicBoolean isAggregated = new AtomicBoolean(false);
        grammarElement.accept(new GrammarElementVisitor() {
            private final Set<GroupGrammarElement> seen = Collections.newSetFromMap(new IdentityHashMap<>());

            @Override
            public boolean visit(GroupGrammarElement element) {
                if(seen.contains(element)) {
                    return false;
                }
                seen.add(element);
                if(element == grammarElement) {
                    return true;
                }
                String elementName = element.getName();
                if (elementName != null) {
                    PropertyDefinition subDef = Properties.getPropertyDefinition(elementName);
                    if (subDef != null && isVisibleProperty(subDef)) {
                        isAggregated.set(true); //contains visible sub properties
                        return false;
                    }
                }
                return true;
            }
        });
        return isAggregated.get();
    }
    
}
