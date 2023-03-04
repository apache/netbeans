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

package org.netbeans.modules.php.api.documentation;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.php.api.documentation.ui.customizer.CompositeCategoryProviderImpl;
import org.netbeans.modules.php.spi.documentation.PhpDocumentationProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;

/**
 * This class provides access to the list of registered {@link PhpDocumentationProvider PHP documentation providers}.
 * <p>
 * The path is "{@value #DOCUMENTATION_PATH}" on SFS.
 * @author Tomas Mysik
 */
public final class PhpDocumentations {

    /**
     * Path on SFS where {@link PhpDocumentationProvider PHP documentation providers} need to be registered.
     */
    public static final String DOCUMENTATION_PATH = "PHP/Documentation"; //NOI18N
    /**
     * Identifier of Project Customizer for documentation.
     * @since 0.11
     */
    public static final String CUSTOMIZER_IDENT = "Documentation"; // NOI18N

    private static final Lookup.Result<PhpDocumentationProvider> DOCUMENTATIONS = Lookups.forPath(DOCUMENTATION_PATH).lookupResult(PhpDocumentationProvider.class);

    private PhpDocumentations() {
    }

    /**
     * Get all registered {@link PhpDocumentationProvider}s.
     * @return a list of all registered {@link PhpDocumentationProvider}s; never {@code null}.
     */
    public static List<PhpDocumentationProvider> getDocumentations() {
        return new ArrayList<>(DOCUMENTATIONS.allInstances());
    }

    /**
     * Add {@link LookupListener listener} to be notified when documentation providers change
     * (new provider added, existing removed).
     * <p>
     * To avoid memory leaks, do not forget to {@link #removeDocumentationsListener(LookupListener) remove} the listener.
     * @param listener {@link LookupListener listener} to be added
     * @see #removeDocumentationsListener(LookupListener)
     */
    public static void addDocumentationsListener(@NonNull LookupListener listener) {
        Parameters.notNull("listener", listener);
        DOCUMENTATIONS.addLookupListener(listener);
    }

    /**
     * Remove {@link LookupListener listener}.
     * @param listener {@link LookupListener listener} to be removed
     * @see #addDocumentationsListener(LookupListener)
     */
    public static void removeDocumentationsListener(@NonNull LookupListener listener) {
        Parameters.notNull("listener", listener);
        DOCUMENTATIONS.removeLookupListener(listener);
    }

    /**
     * Create project customizer for documentation providers.
     * @return project customizer for documentation providers
     * @since 0.11
     */
    public static ProjectCustomizer.CompositeCategoryProvider createCustomizer() {
        return new CompositeCategoryProviderImpl();
    }

}
