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

package org.netbeans.modules.php.api.testing;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.netbeans.modules.php.spi.testing.PhpTestingProviders;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;

/**
 * This class provides access to the list of registered {@link PhpTestingProvider PHP testing providers}.
 * <p>
 * The path is "{@value #TESTING_PATH}" on SFS.
 */
public final class PhpTesting {

    private static final Logger LOGGER = Logger.getLogger(PhpTesting.class.getName());

    /**
     * Path on SFS where {@link PhpTestingProvider PHP testing providers} need to be registered.
     */
    public static final String TESTING_PATH = "PHP/Testing"; //NOI18N
    /**
     * Identifier of Project Customizer for testing.
     * @since 0.6
     */
    public static final String CUSTOMIZER_IDENT = "Testing"; // NOI18N

    private static final Lookup.Result<PhpTestingProvider> TESTING_PROVIDERS = Lookups.forPath(TESTING_PATH).lookupResult(PhpTestingProvider.class);


    private PhpTesting() {
    }

    /**
     * Get all registered {@link PhpTestingProvider}s.
     * @return a list of all registered {@link PhpTestingProvider}s; never {@code null}.
     */
    public static List<PhpTestingProvider> getTestingProviders() {
        return new ArrayList<>(TESTING_PROVIDERS.allInstances());
    }

    /**
     * Checks whether the given testing provider is enabled in the given PHP module.
     * @param providerIdentifier identifier of the testing provider
     * @param phpModule project to be checked
     * @return {@code true} if the given testing provider is enabled in the given PHP module, {@code false} otherwise
     * @since 0.17
     */
    public static boolean isTestingProviderEnabled(String providerIdentifier, PhpModule phpModule) {
        Parameters.notNull("providerIdentifier", providerIdentifier); // NOI18N
        Parameters.notNull("phpModule", phpModule); // NOI18N
        PhpTestingProviders testingProviders = phpModule.getLookup().lookup(PhpTestingProviders.class);
        if (testingProviders == null) {
            LOGGER.log(Level.INFO, "Cannot find PhpTestingProviders instance in lookup of project {0}", phpModule.getClass().getName());
            return false;
        }
        for (PhpTestingProvider testingProvider : testingProviders.getEnabledTestingProviders()) {
            if (providerIdentifier.equals(testingProvider.getIdentifier())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add {@link LookupListener listener} to be notified when testing providers change
     * (new provider added, existing one removed).
     * <p>
     * To avoid memory leaks, do not forget to {@link #removeTestingProvidersListener(LookupListener) remove} the listener.
     * @param listener {@link LookupListener listener} to be added
     * @see #removeTestingProvidersListener(LookupListener)
     */
    public static void addTestingProvidersListener(@NonNull LookupListener listener) {
        Parameters.notNull("listener", listener);
        TESTING_PROVIDERS.addLookupListener(listener);
    }

    /**
     * Remove {@link LookupListener listener}.
     * @param listener {@link LookupListener listener} to be removed
     * @see #addTestingProvidersListener(LookupListener)
     */
    public static void removeTestingProvidersListener(@NonNull LookupListener listener) {
        Parameters.notNull("listener", listener);
        TESTING_PROVIDERS.removeLookupListener(listener);
    }

}
