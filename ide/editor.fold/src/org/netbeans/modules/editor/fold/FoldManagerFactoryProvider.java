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

package org.netbeans.modules.editor.fold;

import java.util.List;
import org.netbeans.api.editor.fold.FoldHierarchy;

/**
 * Provides list of fold factories that produce fold managers
 * for the given fold hierarchy.
 *
 * <p>
 * The default implementation <code>NbFoldManagerFactoryProvider</code>
 * in fact first obtains a mime-type by using
 * <code>hierarchySpi.getComponent().getEditorKit().getContentType()</code>
 * and then inspects the contents of the following folder in the system FS:<pre>
 *     Editors/<mime-type>/FoldManager
 * </pre>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class FoldManagerFactoryProvider {
    
    private static FoldManagerFactoryProvider defaultProvider;
    
    private static FoldManagerFactoryProvider emptyProvider;
    
    private static boolean forceCustom;
    
    /**
     * Get the default provider used to produce the managers.
     * <br>
     * This method gets called by <code>FoldHierarchyExecution</code>
     * when rebuilding the managers.
     */
    public static synchronized FoldManagerFactoryProvider getDefault() {
        if (defaultProvider == null) {
            defaultProvider = findDefault();
        }
        
        return defaultProvider;
    }
    
    /**
     * Return provider that provides empty list of factories.
     * <br>
     * This method may be used e.g. by <code>FoldHierarchyExecution</code>
     * if the code folding is disabled in editor options.
     */
    public static FoldManagerFactoryProvider getEmpty() {
        if (emptyProvider == null) {
            // Multiple EmptyProvider can be created as method is not synced
            // but should be no harm
            emptyProvider = new EmptyProvider();
        }
        return emptyProvider;
    }
    
    /**
     * This method enforces the use of custom provider
     * instead of the default layer-based provider.
     * <br>
     * It can be used e.g. for testing purposes.
     *
     * @param forceCustomProvider whether the instance
     *  of the {@link CustomProvider} should be used forcibly.
     */
    public static synchronized void setForceCustomProvider(boolean forceCustomProvider) {
        if (!forceCustom) {
            defaultProvider = null;
        }
        forceCustom = forceCustomProvider;
    }
    
    private static FoldManagerFactoryProvider findDefault() {
        FoldManagerFactoryProvider provider = null;

        // By default use layer-based fold manager factory registrations.
        // In case of standalone editor the custom provider
        // will be used allowing custom fold manager factories registrations
        // (public packages restrictions should not apply).
        if (!forceCustom) {
            try {
                provider = new LayerProvider();
            } catch (Throwable t) {
                // FileObject class not found -> use layer
            }
        }

        if (provider == null) {
            provider = new CustomProvider();
        }
        
        return provider;
    }
    
    /**
     * Get fold managers appropriate for the given fold hierarchy.
     *
     * @param hierarchy fold hierarchy for which the fold managers
     *  are being created.
     * @return list of <code>FoldManagerFactory</code>s to be used
     *  for the given hierarchy.
     *  <br>
     *  The order of the factories in the returned list defines
     *  priority of the folds produced by the corresponding manager
     *  (manager produced by the factory being first in the list
     *  produces the most important folds).
     *  <br>
     *  The list must not be modified by the clients.
     */
    public abstract List getFactoryList(FoldHierarchy hierarchy);

    
    /**
     * Provider giving empty list of factories.
     */
    private static final class EmptyProvider extends FoldManagerFactoryProvider {
        
        public List getFactoryList(FoldHierarchy hierarchy) {
            return java.util.Collections.EMPTY_LIST;
        }
        
    }
    
}
