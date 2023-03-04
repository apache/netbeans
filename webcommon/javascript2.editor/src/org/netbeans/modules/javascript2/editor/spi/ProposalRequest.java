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
package org.netbeans.modules.javascript2.editor.spi;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.spi.ParserResult;

/**
 * Context information to provide code completion proposals.
 *
 * @author sdedic
 * @since 0.84
 */
public final class ProposalRequest {
    private final CodeCompletionContext context;
    private final CompletionContext type;
    private final Collection<String> selectors;
    private final int offset;

    public ProposalRequest(CodeCompletionContext context, CompletionContext type, Collection<String> selectors, int anchor) {
        this.context = context;
        this.type = type;
        this.selectors = selectors;
        this.offset = anchor;
    }

    /**
     * @return offset of the supposed symbol start in the AST.
     */
    public int getAnchor() {
        return offset;
    }

    /**
     * Provides code completion context from the parser.
     * @return
     */
    public CodeCompletionContext getContext() {
        return context;
    }

    /**
     * Determines the requested completion type.
     * @return
     */
    public CompletionContext getType() {
        return type;
    }

    /**
     * Convenience method to get the parser result.
     * @return
     */
    public ParserResult getInfo() {
        return context.getParserResult();
    }

    /**
     * Object or variable selectors determined from the prefix and parsed information.
     * @return selectors.
     */
    public Collection<String> getSelectors() {
        return selectors == null ? Collections.emptyList() : selectors;
    }

    /**
     * Convenience method that returns symbol prefix.
     * @return symbol prefix.
     */
    public String getPrefix() {
        return context.getPrefix();
    }
}
