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
package org.netbeans.modules.php.latte.completion;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.php.spi.templates.completion.CompletionProvider;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class CompletionProviders {
    public static final String VARIABLE_COMPLETION_PROVIDER_PATH = "Latte/Completion/Variables"; //NOI18N
    private static final Lookup.Result<CompletionProvider> VARIABLE_PROVIDERS
            = Lookups.forPath(VARIABLE_COMPLETION_PROVIDER_PATH).lookupResult(CompletionProvider.class);
    public static final String CONTROL_COMPLETION_PROVIDER_PATH = "Latte/Completion/Controls"; //NOI18N
    private static final Lookup.Result<CompletionProvider> CONTROL_PROVIDERS
            = Lookups.forPath(CONTROL_COMPLETION_PROVIDER_PATH).lookupResult(CompletionProvider.class);

    private CompletionProviders() {
    }

    public static List<CompletionProvider> getVariableProviders() {
        return new ArrayList<>(VARIABLE_PROVIDERS.allInstances());
    }

    public static void addVariableProviderListener(LookupListener listener) {
        VARIABLE_PROVIDERS.addLookupListener(listener);
    }

    public static void removeVariableProviderListener(LookupListener listener) {
        VARIABLE_PROVIDERS.removeLookupListener(listener);
    }

    public static List<CompletionProvider> getControlProviders() {
        return new ArrayList<>(CONTROL_PROVIDERS.allInstances());
    }

    public static void addControlProviderListener(LookupListener listener) {
        CONTROL_PROVIDERS.addLookupListener(listener);
    }

    public static void removeControlProviderListener(LookupListener listener) {
        CONTROL_PROVIDERS.removeLookupListener(listener);
    }

}
