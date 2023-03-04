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
public class Encoding extends PatternRule {

    private static final String[] PATTERNS_SOURCES = new String[]{
        "Internal encoding declaration named an unsupported chararacter encoding .*?",
        "The internal character encoding declaration specified .*? which is not a rough superset of ASCII",
        "The encoding .*? is not",
        "Authors should not use the character encoding",
        "The character encoding .*? is not widely supported",
        "Using .*? instead of the declared encoding",
        "Unsupported character encoding name: .*?. Will continue sniffing",
        "Overriding document character encoding from ",
        //ErrorReportingTokenizer
        "The character encoding of the document was not explicit but the document contains non-ASCII",
        "No explicit character encoding declaration has been seen yet (assumed .*?) but the document contains non-ASCII",
        "This document is not mappable to XML 1.0 without data loss due to .*? which is not a legal XML 1.0 character",
        "Astral non-character",
        "Forbidden code point",
        "Document uses the Unicode Private Use Area(s), which should not be used in publicly exchanged documents. (Charmod C073)",
        //TreeBuilder
        "Attribute .content. would be sniffed as an internal character encoding declaration but there was no matching",        
        "Internal encoding declaration .*? disagrees with the actual encoding of the document (.*?)",
            
            
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
