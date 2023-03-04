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
package org.netbeans.modules.languages.neon.completion;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.languages.neon.spi.completion.MethodCompletionProvider;
import org.netbeans.modules.php.spi.templates.completion.CompletionProvider;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class CompletionProviders {
    public static final String TYPE_COMPLETION_PROVIDER_PATH = "Neon/completion/type"; //NOI18N
    public static final String METHOD_COMPLETION_PROVIDER_PATH = "Neon/completion/method"; //NOI18N
    private static final Lookup.Result<CompletionProvider> TYPE_PROVIDERS = Lookups.forPath(TYPE_COMPLETION_PROVIDER_PATH).lookupResult(CompletionProvider.class);
    private static final Lookup.Result<MethodCompletionProvider> METHOD_PROVIDERS = Lookups.forPath(METHOD_COMPLETION_PROVIDER_PATH).lookupResult(MethodCompletionProvider.class);

    private CompletionProviders() {
    }

    public static List<CompletionProvider> getTypeProviders() {
        return new ArrayList<CompletionProvider>(TYPE_PROVIDERS.allInstances());
    }

    public static void addTypeProviderListener(LookupListener listener) {
        TYPE_PROVIDERS.addLookupListener(listener);
    }

    public static void removeTypeProviderListener(LookupListener listener) {
        TYPE_PROVIDERS.removeLookupListener(listener);
    }

    public static List<MethodCompletionProvider> getMethodProviders() {
        return new ArrayList<MethodCompletionProvider>(METHOD_PROVIDERS.allInstances());
    }

    public static void addMethodProviderListener(LookupListener listener) {
        METHOD_PROVIDERS.addLookupListener(listener);
    }

    public static void removeMethodProviderListener(LookupListener listener) {
        METHOD_PROVIDERS.removeLookupListener(listener);
    }

}
