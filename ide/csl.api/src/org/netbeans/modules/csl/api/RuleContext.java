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
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.HintsProvider.HintsManager;
import org.netbeans.modules.csl.spi.ParserResult;

/**
 * Information about the current context a rule is being asked to evaluate.
 * 
 * @author Tor Norbye
 */
public class RuleContext {

    public RuleContext() {
    }

    /** The HintsManager associated with this rule context */
    @NonNull
    public HintsManager manager;
    
    /** The current caret offset (for caret-based rules), or -1 */
    public int caretOffset = -1;
    
    /** The start of the current selection (if any) or -1 */
    public int selectionStart = -1;
    
    /** The end of the current selection (if any) or -1 */
    public int selectionEnd;
    
    /** The CompilationInfo corresponding to this rule context */
    @NonNull
    public ParserResult parserResult;
    
    /** The document */
    @NonNull
    public BaseDocument doc;
    
// XXX: parsingapi
//    /** All the embedded parser results for this compilation info */
//    @NonNull
//    public Collection<? extends ParserResult> parserResults;
//
//    /** The FIRST parser result (if parserResults.size() > 0) or null */
//    @CheckForNull
//    public ParserResult parserResult;

    // Fields useful for subclasses
    // TODO - push into subclasses?
    public int lexOffset;
    public int astOffset;
}
