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
package org.netbeans.modules.javascript2.editor;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.javascript2.editor.spi.CompletionProvider;
import org.netbeans.modules.javascript2.editor.spi.DeclarationFinder;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Petr Hejl
 */
public final class EditorExtender {

    public static final String COMPLETION_PROVIDERS_PATH = "JavaScript/Editor/CompletionInterceptors";

    public static final String DECLARATION_FINDERS_PATH = "JavaScript/Editor/DeclarationFinderInterceptors";

    private static final Lookup.Result<CompletionProvider> COMPLETION_PROVIDERS =
            Lookups.forPath(COMPLETION_PROVIDERS_PATH).lookupResult(CompletionProvider.class);

    private static final Lookup.Result<DeclarationFinder> DECLARATION_FINDERS =
            Lookups.forPath(DECLARATION_FINDERS_PATH).lookupResult(DeclarationFinder.class);

    private static EditorExtender instance;

    private EditorExtender() {
        super();
    }

    public static synchronized EditorExtender getDefault() {
        if (instance == null) {
            instance = new EditorExtender();
        }
        return instance;
    }

    public List<CompletionProvider> getCompletionProviders() {
        return new ArrayList<>(COMPLETION_PROVIDERS.allInstances());
    }

    public List<DeclarationFinder> getDeclarationFinders() {
        return new ArrayList<>(DECLARATION_FINDERS.allInstances());
    }
}
