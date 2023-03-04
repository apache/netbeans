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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.html.editor.spi.HintFixProvider;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.util.Lookup;

/**
 *
 * @author marekfukala
 */
public class Element extends PatternRule {

    private static final String[] PATTERNS_SOURCES = new String[]{
        //attributes
        "Attribute (.*?) not allowed on element (.*?) at this point",
        "Required attributes missing on element",
        
        //ErrorReportingTokenizer
        "The ./>. syntax on void elements is not allowed.  (This is an HTML4-only error.)",
        "No space between attributes",
        "Saw .<\\. when expecting an attribute name. Probable cause: Missing .>. immediately before.",
        "Bad character ... after .<.. Probable cause: Unescaped .<\\.. Try escaping it as .&lt;..",
        "Saw .<>.. Probable causes: Unescaped .<\\. (escape as .&lt;.) or mistyped start tag.",
        "End tag had attributes.",
        "Stray ./. at the end of an end tag.",
        "Saw end of file without the previous tag ending",
        "End of file seen when looking for tag name. Ignoring tag.",
        "End of file inside end tag. Ignoring tag.",
        "End of file after .<\\.",
        "Duplicate attribute",
        "Self-closing syntax (./>.) used on a non-void HTML element",
        
        "Text not allowed in element .*? in this context",
        
        //xhtml
        "XHTML element .*? not allowed as child of XHTML element .*? in this context.",
        "Attribute .*? not allowed on XHTML element .*? at this point.",
        
        //???
        "The end-tag for element type .*? must end with a '>' delimiter.",
        
            
    }; //NOI18N
    
    private static final Pattern[] PATTERNS = buildPatterns(PATTERNS_SOURCES);

    private static final int UNKNOWN_ATTRIBUTE_PATTERN_INDEX = 0;
    
    @Override
    public Pattern[] getPatterns() {
        return PATTERNS;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.ERROR;
    }
    
     @Override
    protected List<HintFix> getExtraHintFixes(org.netbeans.modules.csl.api.Error e, HtmlRuleContext context) {
        if (matched_pattern_index == UNKNOWN_ATTRIBUTE_PATTERN_INDEX) {
            //the "Element .*? not allowed as child of element .*? in this context." pattern
            List<HintFix> fixes = new ArrayList<>();
            fixes.addAll(super.getExtraHintFixes(e, context));
            fixes.addAll(getSPIHintFixes(e, context));
            return fixes;
        } else {
            return super.getExtraHintFixes(e, context);
        }
    }

    private List<HintFix> getSPIHintFixes(org.netbeans.modules.csl.api.Error e, HtmlRuleContext context) {
        List<HintFix> fixes = new ArrayList<>();
        //extract the element name and name of its parent first from the error message
        Pattern p = PATTERNS[UNKNOWN_ATTRIBUTE_PATTERN_INDEX];
        Matcher matcher = p.matcher(e.getDescription());
        if (matcher.matches()) {
            String unknownElement = WebUtils.unquotedValue(matcher.group(1).trim());
            String contextElement = WebUtils.unquotedValue(matcher.group(2).trim());

            Map<String, Object> meta = new HashMap<>();
            meta.put(HintFixProvider.UNKNOWN_ATTRIBUTE_FOUND, unknownElement);
            meta.put(HintFixProvider.UNKNOWN_ELEMENT_CONTEXT, contextElement);
            
            HintFixProvider.Context ctx = new HintFixProvider.Context(context.getSnapshot(), context.getHtmlParserResult(), meta);
            for (HintFixProvider provider : Lookup.getDefault().lookupAll(HintFixProvider.class)) {
                fixes.addAll(provider.getHintFixes(ctx));
            }
        }
        return fixes;
    }
    

}
