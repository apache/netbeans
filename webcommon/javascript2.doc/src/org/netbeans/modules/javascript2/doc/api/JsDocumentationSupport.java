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
package org.netbeans.modules.javascript2.doc.api;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.javascript2.doc.JsDocumentationFallbackSyntaxProvider;
import org.netbeans.modules.javascript2.doc.JsDocumentationResolver;
import org.netbeans.modules.javascript2.doc.spi.DocumentationContainer;
import org.netbeans.modules.javascript2.doc.spi.JsComment;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationProvider;
import org.netbeans.modules.javascript2.doc.spi.SyntaxProvider;
import org.netbeans.modules.javascript2.types.spi.ParserResult;

/**
 * Contains support methods for obtaining {@link JsDocumentationProvider}.
 *
 * @author Martin Fousek, Petr Pisl
 */
public final class JsDocumentationSupport {

    /** Path of the documentation providers in the layer. */
    public static final String DOCUMENTATION_PROVIDER_PATH = "javascript/doc/providers"; //NOI18N

    private JsDocumentationSupport() {
    }

    /**
     * Gets {@code JsDocumentationProvider} for given {@code JsParserResult}.
     * @param result {@code ParserResult}
     * @return {@code JsDocumentationHolder} for given {@code ParserResult}, never {@code null}
     */
    @NonNull
    public static JsDocumentationHolder getDocumentationHolder(ParserResult result) {
        DocumentationContainer c = result.getLookup().lookup(DocumentationContainer.class);
        if (c != null) {
            return c.getHolder(result);
        } else {
            JsDocumentationProvider p = JsDocumentationResolver.getDefault().getDocumentationProvider(result.getSnapshot());
            return p.createDocumentationHolder(result.getSnapshot());
        }
    }

    /**
     * Gets the documentation provider for given parser result.
     * @param result JsParserResult
     * @return JsDocumentationProvider
     */
    @NonNull
    public static JsDocumentationProvider getDocumentationProvider(ParserResult result) {
        // XXX - complete caching of documentation tool provider
        return JsDocumentationResolver.getDefault().getDocumentationProvider(result.getSnapshot());
    }

    /**
     * Gets SyntaxProvider of appropriate documentation support.
     * @param parserResult JsParserResult
     * @return documentation support specific or default {@code SyntaxProvider}
     */
    @NonNull
    public static SyntaxProvider getSyntaxProvider(ParserResult parserResult) {
        SyntaxProvider syntaxProvider = getDocumentationHolder(parserResult).getProvider().getSyntaxProvider();
        return syntaxProvider != null ? syntaxProvider : new JsDocumentationFallbackSyntaxProvider();
    }

    /**
     * Gets JsComment for given offset in the snapshot.
     * @param result JsParserResult
     * @param offset snapshot offset
     * @return found {@code JsComment} or {@code null} otherwise
     */
    @CheckForNull
    public static JsComment getCommentForOffset(ParserResult result, int offset) {
        JsDocumentationHolder holder = getDocumentationHolder(result);
        return holder.getCommentForOffset(offset, holder.getCommentBlocks());
    }

}
