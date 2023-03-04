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

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.csl.spi.ParserResult;

/**
 * This class provides context regarding a code completion request. The infrastructure
 * will subclass this class and pass an instance to you.
 * 
 * @author Tor Norbye
 */
public abstract class CodeCompletionContext {

    /** 
     * The caret offset where we want completion 
     * @return The caret offset where we want cmpletion
     */
    public abstract int getCaretOffset();

//    /** 
//     * The compilation info for this file 
//     * @return The compilation info for this file
//     */
//    @NonNull
//    public abstract CompilationInfo getInfo();
    
    public abstract ParserResult getParserResult ();

    /** 
     * The prefix computed for this caret offset (as determined by your own {@link #getPrefix()} method 
     * @return The prefix computed for this caret offset
     */
    @NonNull
    public abstract String getPrefix();

    /**
     * The type of query to perform -- normal code completion for a popup list, or documentation
     * completion for a single item, or tooltip computation, etc.
     * @return The type of query to perform
     */
    @NonNull
    public abstract QueryType getQueryType();

    /**
     * Whether the search should match prefixes or whole identifiers.
     * @return If <code>true</code> the search should match <code>getPrefix</code> against
     *   the beginnig of identifiers. If <code>false</code> the search should match
     *   <code>getPrefix</code> against the whole identifier.
     */
    public abstract boolean isPrefixMatch();
    
    /** Whether the search should be case sensitive.
     * @return Whether the search should be case sensitive
     * @todo This should be merged with the NameKind which already passes this information
     */
    public abstract boolean isCaseSensitive();
}
