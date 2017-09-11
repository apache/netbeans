/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
