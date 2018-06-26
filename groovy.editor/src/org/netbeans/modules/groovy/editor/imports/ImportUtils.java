/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.editor.imports;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.groovy.editor.spi.completion.DefaultImportsProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Janicek
 */
public final class ImportUtils {

    private ImportUtils() {
    }
    
    /**
     * Finds out if the given fully qualified name is imported by default or not.
     * 
     * @param fqn fully qualified name for the type we need to check
     * @return true if the given fqn is defaultly imported, false otherwise
     */
    public static boolean isDefaultlyImported(String fqn) {
        for (String defaultImport : getDefaultImportClasses()) {
            if (defaultImport.equals(fqn)) {
                return true; // We don't want to add import statement for default imports
            }
        }
        
        final String packageName = getPackageName(fqn);
        for (String defaultImport : getDefaultImportPackages()) {
            if (defaultImport.equals(packageName)) {
                return true; // We don't want to add import statement for types from defaultly imported packages
            }
        }
        return false;
    }
    
    private static String getPackageName(String fqn) {
        if (fqn.contains(".")) {
            fqn = fqn.substring(0, fqn.lastIndexOf("."));
        }
        return fqn;
    }
    
    public static Set<String> getDefaultImportPackages() {
        Set<String> defaultPackages = new HashSet<>();
        
        defaultPackages.add("java.io");     // NOI18N
        defaultPackages.add("java.lang");   // NOI18N
        defaultPackages.add("java.net");    // NOI18N
        defaultPackages.add("java.util");   // NOI18N
        defaultPackages.add("groovy.util"); // NOI18N
        defaultPackages.add("groovy.lang"); // NOI18N
        
        for (DefaultImportsProvider importsProvider : Lookup.getDefault().lookupAll(DefaultImportsProvider.class)) {
            defaultPackages.addAll(importsProvider.getDefaultImportPackages());
        }

        return defaultPackages;
    }
    
    public static Set<String> getDefaultImportClasses() {
        Set<String> defaultClasses = new HashSet<>();
        
        defaultClasses.add("java.math.BigDecimal"); // NOI18N
        defaultClasses.add("java.math.BigInteger"); // NOI18N
        
        for (DefaultImportsProvider importsProvider : Lookup.getDefault().lookupAll(DefaultImportsProvider.class)) {
            defaultClasses.addAll(importsProvider.getDefaultImportClasses());
        }

        return defaultClasses;
    }
}
