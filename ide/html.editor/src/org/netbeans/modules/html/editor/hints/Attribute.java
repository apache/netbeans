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
public class Attribute extends PatternRule {

    private static final String[] PATTERNS_SOURCES = new String[]{
        //ErrorReportingTokenizer
        ".<. in an unquoted attribute value. Probable cause: Missing .>. immediately before",
        "... in an unquoted attribute value. Probable cause: Using the wrong character as a quote",
        "... in an unquoted attribute value. Probable causes: Attributes running together or a URL query string in an unquoted attribute value",
        "Non-name character in an unquoted attribute value. (This is an HTML4-only error.)",
        "at the start of an unquoted attribute value. Probable cause:",
        "Attribute value missing.",
        "Saw ... when expecting an attribute name",
        ".<. in attribute name",
        "Quote ... in attribute name",
        "End of file reached when inside an attribute value. Ignoring tag.",
        "End of file occurred in an attribute name. Ignoring tag.",
        "Attribute without value",
        
        //HtmlAttributes
        "Attribute .*? is not serializable as XML 1.0",
        
        //source?
        "Bad value .*? for attribute .*? on element",
        "Attribute name .*? associated with an element type .*? must be followed by the ' = ' character."
        
    }; //NOI18N
    
    private static final Pattern[] PATTERNS = buildPatterns(PATTERNS_SOURCES);

    @Override
    public Pattern[] getPatterns() {
        return PATTERNS;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.ERROR;
    }
    
    
    

}
