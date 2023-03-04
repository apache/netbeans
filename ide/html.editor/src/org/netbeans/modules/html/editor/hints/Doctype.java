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
package org.netbeans.modules.html.editor.hints;

import java.util.regex.Pattern;
import org.netbeans.modules.csl.api.HintSeverity;

/**
 *
 * @author marekfukala
 */
public class Doctype extends PatternRule {

    private static final String[] PATTERNS_SOURCES = new String[]{
        //ErrorReportingTokenizer
        ".. in system identifier.",
        ".. in public identifier.",
        "Nameless doctype.",
        "Expected a public identifier but the doctype ended.",
        "Bogus doctype.",
        "End of file inside doctype.",
        "End of file inside system identifier.",
        "Expected a system identifier but the doctype ended.",
        "Missing space before doctype name.",
        "No space between the doctype",
        "Unquoted attribute value.",
        //TreeBuilder
        "Quirky doctype. Expected",
        "Almost standards mode doctype. Expected",
        "Obsolete doctype. Expected",
        "Legacy doctype. Expected",
        "The doctype did not contain the system identifier prescribed by",
        "The doctype was not the HTML 4.01 Strict doctype. Expected",
        "The doctype was not a non-quirky HTML 4.01 Transitional doctype. Expected",
        "The doctype was not the HTML 4.01 Transitional doctype. Expected",
        "Quirky doctype. Expected",
        
        "Start tag seen without seeing a doctype first. Expected", //????
        
        
        
            
        
        
        
    }; //NOI18N
    
    private static final Pattern[] PATTERNS = buildPatterns(PATTERNS_SOURCES);

    @Override
    public Pattern[] getPatterns() {
        return PATTERNS;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }
    
    
    

}
