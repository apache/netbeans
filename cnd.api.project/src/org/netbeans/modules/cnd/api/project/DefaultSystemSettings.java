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

package org.netbeans.modules.cnd.api.project;

import java.util.Collections;
import java.util.List;
import org.openide.util.Lookup;

/**
 * This service provides default include path and macros.
 */
public abstract class DefaultSystemSettings {
    /** A dummy provider that never returns any results.
     */
    private static final DefaultSystemSettings EMPTY = new Empty();
    
    /** default instance */
    private static DefaultSystemSettings defaultProvider;
    
    
    /**
     * Static method to obtain the provider.
     * @return the provider
     */
    public static synchronized DefaultSystemSettings getDefault() {
        if (defaultProvider != null) {
            return defaultProvider;
        }
        defaultProvider = Lookup.getDefault().lookup(DefaultSystemSettings.class);
        return defaultProvider == null ? EMPTY : defaultProvider;
    }
    
    /**
     * Obtain a list of default system includes for given language.
     * Return empty list if language is neither C nor C++ or no default compilers were found.
     * @return Unmodifiable list of strings or empty list.
     */
    public abstract List<String> getSystemIncludes(NativeFileItem.Language language, NativeFileItem.LanguageFlavor flavor, NativeProject project);

    /**
     * Obtain a list of default system pre-included headers for given language.
     * Return empty list if language is neither C nor C++ or no default compilers were found.
     * @return Unmodifiable list of strings or empty list.
     */
    public abstract List<String> getSystemIncludeHeaders(NativeFileItem.Language language, NativeFileItem.LanguageFlavor flavor, NativeProject project);
    
    /**
     * Obtain a list of default system macros for given language. 
     * Return empty list if language is neither C nor C++ or no default compilers were found.
     * @return Unmodifiable list of strings or empty list.
     */
    public abstract List<String> getSystemMacros(NativeFileItem.Language language, NativeFileItem.LanguageFlavor flavor, NativeProject project);

    private static final class Empty extends DefaultSystemSettings {

        @Override
        public List<String> getSystemIncludes(NativeFileItem.Language language, NativeFileItem.LanguageFlavor flavor, NativeProject project) {
            return Collections.emptyList();
        }
      
        @Override
        public List<String> getSystemIncludeHeaders(NativeFileItem.Language language, NativeFileItem.LanguageFlavor flavor, NativeProject project) {
            return Collections.emptyList();
        }

        @Override
        public List<String> getSystemMacros(NativeFileItem.Language language, NativeFileItem.LanguageFlavor flavor, NativeProject project) {
            return Collections.emptyList();
        }

    }
}
