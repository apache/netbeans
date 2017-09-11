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
package org.netbeans.modules.css.lib.api.properties;

import java.util.ArrayList;
import java.util.Collection;
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
            private boolean cancelled = false;
            @Override
            public void visit(GroupGrammarElement element) {
                if(element == grammarElement) {
                    //skip itself
                    return ;
                }
                if(cancelled) {
                    return ;
                }
                String elementName = element.getName();
                if (elementName != null) {
                    PropertyDefinition subDef = Properties.getPropertyDefinition(elementName);
                    if (subDef != null && isVisibleProperty(subDef)) {
                        isAggregated.set(true); //contains visible sub properties
                        cancelled = true;
                    }
                }
            }
        });
        return isAggregated.get();
    }
    
}
