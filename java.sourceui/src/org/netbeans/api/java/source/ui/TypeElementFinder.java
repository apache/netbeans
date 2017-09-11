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
