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
package org.netbeans.modules.javascript2.editor.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.css.lib.api.FilterableError;
import org.netbeans.modules.javascript2.doc.spi.DocumentationContainer;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.model.spi.ModelContainer;
import org.netbeans.modules.javascript2.types.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Tomas Zezula
 */
public abstract class BaseParserResult extends ParserResult {
    private static final Set<String> MIME_TYPES;
    static {
        final Set<String> s = new HashSet<>();
        Collections.addAll(s,
            JsTokenId.JAVASCRIPT_MIME_TYPE,
            JsTokenId.GULP_MIME_TYPE,
            JsTokenId.GRUNT_MIME_TYPE,
            JsTokenId.KARMACONF_MIME_TYPE,
            JsTokenId.JSON_MIME_TYPE,
            JsTokenId.PACKAGE_JSON_MIME_TYPE,
            JsTokenId.BOWER_JSON_MIME_TYPE,
            JsTokenId.BOWERRC_JSON_MIME_TYPE,
            JsTokenId.JSHINTRC_JSON_MIME_TYPE
        );
        MIME_TYPES = Collections.unmodifiableSet(s);
    }

    private final ModelContainer modelContainer = new ModelContainer();
    private final DocumentationContainer documentationContainer = new DocumentationContainer();
    private final boolean embedded;
    private final boolean success;
    private final Lookup lookup;
    private List<? extends FilterableError> errors;

    BaseParserResult(
            Snapshot snapshot,
            boolean success,
            @NonNull final Lookup additionalLkp) {
        super(snapshot);
        errors = Collections.emptyList();
        this.success = success;
        embedded = isEmbedded(snapshot);
        final Lookup baseLkp = Lookups.fixed(this, modelContainer, documentationContainer);
        lookup = new ProxyLookup(baseLkp, additionalLkp);
    }

    @Override
    public final Lookup getLookup() {
        return lookup;
    }

    @Override
    @NonNull
    public final List<? extends FilterableError> getDiagnostics() {
        return getErrors(false);
    }

    @NonNull
    public final List<? extends FilterableError> getErrors(boolean includeFiltered) {
        if (includeFiltered) {
            return Collections.unmodifiableList(errors);
        } else {
            //remove filtered issues
            final List<FilterableError> result = new ArrayList<>(errors.size());
            for(FilterableError e : errors) {
                if(!e.isFiltered()) {
                    result.add(e);
                }
            }
            return result;
        }
    }

    public final void setErrors(@NonNull final List<? extends FilterableError> errors) {
        Parameters.notNull("errors", errors);   //NOI18N
        this.errors = errors;
    }

    public final boolean isEmbedded() {
        return embedded;
    }

    @Override
    protected void invalidate() {
    }

    public static boolean isEmbedded(@NonNull Snapshot snapshot) {
        return !MIME_TYPES.contains(snapshot.getMimePath().getPath());
    }

    boolean success() {
        return this.success;
    }

}
