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

package org.netbeans.api.java.source.ui;

import java.util.Set;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.jumpto.type.TypeBrowser;
import org.netbeans.modules.java.source.ui.JavaTypeDescription;
import org.netbeans.modules.java.source.ui.JavaTypeProvider;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.openide.util.NbBundle;

/**
 * Support for browsing of the Java types. Opens search dialog for the type name 
 * with possibility to filter possible results.
 *
 * @author Martin Adamek
 * 
 * @since 1.3
 */
public final class TypeElementFinder {

    /**
     * Searches for classes on given classpath using defined restrictions.
     *
     * @param cpInfo classpath used for search; optional, can be null,
     * everything available will be searched
     * @param customizer possibility to add restrictions to search result;
     * optional, can be null, no restriction will be applied
     * @return found type or null if dialog was canceled
     */
    public static @CheckForNull ElementHandle<TypeElement> find(@NullAllowed ClasspathInfo cpInfo, @NullAllowed final Customizer customizer) {
        return find(cpInfo, null, customizer);
    }

    /**
     * Searches for classes on given classpath using defined restrictions.
     * 
     * @param cpInfo classpath used for search; optional, can be null,
     * everything available will be searched
     * @param initialText text that should be prefilled in the type name text field, or null to prefill text automatically from the context
     * @param customizer possibility to add restrictions to search result; 
     * optional, can be null, no restriction will be applied
     * @return found type or null if dialog was canceled
     * @since 1.24
     */
    public static @CheckForNull ElementHandle<TypeElement> find(@NullAllowed ClasspathInfo cpInfo, @NullAllowed String initialText, @NullAllowed final Customizer customizer) {

        // create filter only if client wants to customize the result
        TypeBrowser.Filter typeBrowserFilter = null;
        if (customizer != null) {
            typeBrowserFilter = new TypeBrowser.Filter() {
                public boolean accept(TypeDescriptor typeDescriptor) {
                    JavaTypeDescription javaTypeDesc = toJavaTypeDescription(typeDescriptor);
                    if (customizer != null && javaTypeDesc != null) {
                        return customizer.accept(javaTypeDesc.getHandle());
                    }
                    return true;
                }
            };
        }
        
        TypeDescriptor typeDescriptor = TypeBrowser.browse(
                NbBundle.getMessage(TypeElementFinder.class, "DLG_FindType"),
                initialText,
                typeBrowserFilter,
                new JavaTypeProvider(cpInfo, customizer == null ? null : customizer)
                );
        JavaTypeDescription javaTypeDesc = toJavaTypeDescription(typeDescriptor);
        
        return javaTypeDesc == null ? null : javaTypeDesc.getHandle();
    }

    /**
     * Customization of search scope and results
     */
    public static interface Customizer {
        
        /**
         * Set the scope of the search on particular classpath. Too expensive queries
         * will affect performance of dialog, consider also using in combination
         * with {@link #query}
         */
        Set<ElementHandle<TypeElement>> query(ClasspathInfo classpathInfo, String textForQuery, ClassIndex.NameKind nameKind, Set<ClassIndex.SearchScope> searchScopes);
        
        /**
         * Filter results directly in dialog. 
         * Useful when filtering operation itself is too expensive to perform globally
         * in <code>query</code> method and when ratio of filtered elements is reasonably high.
         * (e.g. visibility query)
         */
        boolean accept(ElementHandle<TypeElement> typeHandle);
        
    }
    
    // private
    
    private static JavaTypeDescription toJavaTypeDescription(TypeDescriptor typeDescriptor) {
        if (typeDescriptor instanceof JavaTypeDescription) {
            return (JavaTypeDescription) typeDescriptor;
        }
        return null;
    }
    
}
