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
