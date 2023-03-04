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
package org.netbeans.modules.csl.api;

import java.util.Map;
import java.util.Set;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.spi.ParserResult;


/**
 * Provide code completion for this language. This implementation
 * is responsible for all the analysis around the given caret offset.
 * A code completion provider should be smart and for example limit
 * alternatives not just by the given identifier prefix at the caret offset,
 * but also by the surrounding context; for example, if we're doing
 * code completion inside an expression that is part of a {@code return}
 * statement, the types should be limited by the return type of the current
 * method, and so on.
 *
 * A default SPI implementation is available that will perform some of this
 * analysis assuming it's applied to a parse tree using the other SPI default
 * implementation classes.
 *
 * @todo Instead of passing in caseSensitive, should I pass in a Comparator which should be used
 *   for determining eligibility? That way it's completely insulated from the clients
 * @todo Pass in completion mode such that I can do different stuff for smart-completion
 * @todo The result should indicate whether it has been filtered!
 *
 * @author Tor Norbye
 */
public interface CodeCompletionHandler {
    /**
     * The type of code completion query to perform.
     */
    enum QueryType {
        COMPLETION,
        DOCUMENTATION,
        TOOLTIP,
        ALL_COMPLETION,
        NONE,
        STOP
    }
    
    /**
     * Compute a code completion result for the given code completion request.
     * If there are no results, you should NOT return null, you should return
     * {@link CodeCompletionResult#NONE}.
     * @param context Context regarding the completion
     * @return A result object holding the completion items
     * 
     */
    @NonNull
    CodeCompletionResult complete(@NonNull CodeCompletionContext context);

    /**
     *  Return the HTML documentation for the given program element (returned in CompletionProposals
     *  by the complete method)
     */
    @CheckForNull
    String document(@NonNull ParserResult info, @NonNull ElementHandle element);
    
    /**
     * Resolve a link that was written into the HTML returned by {@link #document}.
     *
     * @param link The link, which can be in any format chosen by the {@link #document} method.
     *   However, links starting with www or standard URL format (http://, etc.)
     *   will automatically be handled by the browser, so avoid this format.
     * @param originalHandle The handle to the documentation item where the link was generated.
     * @return An ElementHandle that will be passed in to {@link #document} to
     *   compute the new documentation to be warped to.
     */
    @CheckForNull
    ElementHandle resolveLink(@NonNull String link, ElementHandle originalHandle);

    /**
     * Compute the prefix to be used for completion at the given caretOffset
     * @param info The compilation info with parse tree info etc.
     * @param caretOffset The caret offset where completion was requested
     * @param upToOffset If true, provide a prefix only up to the caretOffset. Otherwise,
     *   compute the entire completion symbol under the caret. (The former is used
     *   to bring up a set of completion alternatives, whereas the latter is used
     *   to for example bring up the documentation under the symbol.)
     */
    @CheckForNull
    String getPrefix(@NonNull ParserResult info, int caretOffset, boolean upToOffset);

    /**
     * Consider a keystroke and decide whether it should automatically invoke some type
     * of completion. If so, return the desired type, otherwise return {@link QueryType#NONE}.
     * @return A QueryType if automatic completion should be initiated, or {@link QueryType#NONE}
     *   if it should be left alon, or {@link QueryType#STOP} if completion should be terminated
     */
    @NonNull
    QueryType getAutoQuery(@NonNull JTextComponent component, @NonNull String typedText);
    
    // TODO: 
    // processKey action stuff from GsfCompletionItem to handle "(", "." etc.
    
    
    
    /**
     * Perform code template parameter evaluation for use in code template completion
     * or editing. The actual set of parameters defined by the language plugins
     * is not defined and will be language specific. Return null if the variable
     * is not known or supported.
     * 
     * @todo This may need a better home than the Code Completion interface;
     *  while templates are used in template code completion it's unrelated to
     *  the regular Ruby code completion.
     */
    @CheckForNull
    String resolveTemplateVariable(String variable, @NonNull ParserResult info, int caretOffset, 
            @NonNull String name, @NullAllowed Map parameters);
    
    /**
     * Compute the set of applicable templates for a given text selection
     */
    @CheckForNull
    Set<String> getApplicableTemplates(@NonNull Document doc, int selectionBegin, int selectionEnd);
    
    /**
     * Compute parameter info for the given offset - parameters surrounding the given
     * offset, which particular parameter in that list we're currently on, and so on.
     * @param info The compilation info to pick an AST from
     * @param caretOffset The caret offset for the completion request
     * @param proposal May be null, but if not, provide the specific completion proposal
     *   that the parameter list is requested for
     * @return A ParameterInfo object, or ParameterInfo.NONE if parameter completion is not supported.
     */
    @NonNull
    ParameterInfo parameters(@NonNull ParserResult info, int caretOffset, @NullAllowed CompletionProposal proposal);
}
