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
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.html.editor.spi.HintFixProvider;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.util.Lookup;

/**
 *
 * @author marekfukala
 */
public class Nesting extends PatternRule {

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.ERROR;
    }

    private static final String[] PATTERNS_SOURCES = new String[]{
        "Element (.*?) not allowed as child of element (.*?) in this context.",
        "Heading cannot be a child of another heading.",
        ".*? start tag found but the .*? element is already open.",
        "Unclosed elements inside a list.",
        "An .*? start tag seen with already an active .*? element.",
        ".*? start tag seen when there was an open .*? element in scope.",
        "Start tag for .*? seen when .*? was already open.",
        "Bad start tag in .*? in",
        ".*? start tag with .*? open",
        ".*? start tag where end tag expected.",
        ".*? start tag seen in",
        ".*? element outside",
        ".*? element between",
        "Unclosed elements on stack.",
        "No .*? element in scope but a .*? end tag seen.",
        "No .*? element in list scope",
        "Saw an end tag after .*? had been closed.",
        "No element .*? to close.",
        "End tag .*? violates nesting rules.",
        "XHTML element .*? is missing a required instance of child element .*?"


    }; //NOI18N

    private static final Pattern[] PATTERNS = buildPatterns(PATTERNS_SOURCES);

    private static final int UNKNOWN_ELEMENT_PATTERN_INDEX = 0;

    @Override
    public Pattern[] getPatterns() {
        return PATTERNS;
    }

    @Override
    protected List<HintFix> getExtraHintFixes(Error e, HtmlRuleContext context) {
        if (matched_pattern_index == UNKNOWN_ELEMENT_PATTERN_INDEX) {
            //the "Element .*? not allowed as child of element .*? in this context." pattern
            List<HintFix> fixes = new ArrayList<>();
            fixes.addAll(super.getExtraHintFixes(e, context));
            fixes.addAll(getSPIHintFixes(e, context));
            return fixes;
        } else {
            return super.getExtraHintFixes(e, context);
        }
    }

    private List<HintFix> getSPIHintFixes(Error e, HtmlRuleContext context) {
        List<HintFix> fixes = new ArrayList<>();
        //extract the element name and name of its parent first from the error message
        Pattern p = PATTERNS[UNKNOWN_ELEMENT_PATTERN_INDEX];
        Matcher matcher = p.matcher(e.getDescription());
        if (matcher.matches()) {
            String unknownElement = WebUtils.unquotedValue(matcher.group(1).trim());
            String contextElement = WebUtils.unquotedValue(matcher.group(2).trim());
            
            //the nu.validator converts the names of the unknown elements to lowercase
            //so we need to try to find out the original case by a little heuristic
            int embeddedStart = context.getSnapshot().getEmbeddedOffset(e.getStartPosition());
            int embeddedEnd = context.getSnapshot().getEmbeddedOffset(e.getEndPosition());
            if(embeddedStart != -1 && embeddedEnd != -1) {
                CharSequence errorCode = context.getSnapshot().getText().subSequence(embeddedStart, embeddedEnd);
                //the error contains the whole element including the delimiters and attributes
                //example: <myDirective myAttr='value'>
                int expectedElementNameEnd = 1 + unknownElement.length();
                if(errorCode.length() > expectedElementNameEnd) {
                    CharSequence elementName = errorCode.subSequence(1, expectedElementNameEnd);
                    if(LexerUtils.equals(unknownElement, elementName, true, false)) {
                        unknownElement = elementName.toString(); //get the correct case from the document
                    }
                }

                Map<String, Object> meta = new HashMap<>();
                meta.put(HintFixProvider.UNKNOWN_ELEMENT_FOUND, unknownElement);
                meta.put(HintFixProvider.UNKNOWN_ELEMENT_CONTEXT, contextElement);

                HintFixProvider.Context ctx = new HintFixProvider.Context(context.getSnapshot(), context.getHtmlParserResult(), meta);
                for (HintFixProvider provider : Lookup.getDefault().lookupAll(HintFixProvider.class)) {
                    fixes.addAll(provider.getHintFixes(ctx));
                }
            }
        }
        return fixes;
    }

}
