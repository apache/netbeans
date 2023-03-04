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
import org.netbeans.modules.csl.api.Error;
import org.openide.util.NbBundle;

/**
 * TODO: the patterns for html validator issues should rather reside in the html.validator module itself
 *
 * @author marekfukala
 */
public abstract class PatternRule extends HtmlValidatorRule {

    protected int matched_pattern_index;
    
    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>");
        sb.append(super.getDescription());
        sb.append("</b>");
        sb.append("<br><br>");
        sb.append(NbBundle.getMessage(PatternRule.class, "MSG_PatterRuleDesc"));
        sb.append("<br>");
        sb.append("<ul>");
        for(Pattern p : getPatterns()) {
            sb.append("<li>");
            sb.append(p.pattern());
        }
        sb.append("</ul>");
        return sb.toString();
    }

    public abstract Pattern[] getPatterns();

    @Override
    protected final boolean appliesTo(HtmlRuleContext content, Error e) {
        matched_pattern_index = -1;
        String msg = e.getDescription();
        for(int i = 0; i< getPatterns().length; i++) {
            Pattern p = getPatterns()[i];
            if(p.matcher(msg).matches()) {
                matched_pattern_index = i;
                return true;
            }
        }
        
        return false;
    }
    
    protected static Pattern[] buildPatterns(String[] sources) {
        Pattern[] patterns = new Pattern[sources.length];
        for (int i = 0; i < patterns.length; i++) {
            String src = new StringBuilder()
                    .append(ERROR_MGS_PATTERN_PREFIX)
                    .append(sources[i])
                    .append(ERROR_MGS_PATTERN_POSTFIX).toString();
            
            patterns[i] = Pattern.compile(src, Pattern.DOTALL); // (. matches newline)
        }
        return patterns;
        
    }
    
    //represents the Error: or Warning: error messages prefix
    private static final String ERROR_MGS_PATTERN_PREFIX = "[^:]*:\\s"; //NOI18N
    
    //represents the additional text above the core text of the message like
    //description of the error and its position in the source
    private static final String ERROR_MGS_PATTERN_POSTFIX = ".*"; //NOI18N
    
}
