/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib.api.properties;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.css.lib.properties.GrammarParser;
import org.openide.filesystems.FileObject;

/**
 * Represents a resolved css declaration
 *
 * @author mfukala@netbeans.org
 */
public class ResolvedProperty {

    private final GrammarResolverResult grammarResolverResult;
    private static final Pattern FILTER_COMMENTS_PATTERN = Pattern.compile("/\\*.*?\\*/");//NOI18N
    private PropertyDefinition propertyModel;

    public static ResolvedProperty resolve(FileObject context, PropertyDefinition propertyModel, CharSequence propertyValue) {
        return new ResolvedProperty(context, propertyModel, propertyValue);
    }
    
    public ResolvedProperty(FileObject context, PropertyDefinition propertyModel, CharSequence value) {
        this(propertyModel.getGrammarElement(context), filterComments(value));
        this.propertyModel = propertyModel;
    }
   
    //No need to be public - used just by tests!
    //tests only
    //
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    public ResolvedProperty(PropertyDefinition propertyModel, CharSequence value) {
        this(propertyModel.getGrammarElement(null), filterComments(value));
        this.propertyModel = propertyModel;
    }
    
    public ResolvedProperty(GroupGrammarElement groupGrammarElement, String value) {
        this.grammarResolverResult = GrammarResolver.resolve(groupGrammarElement, value);
    }
    
    public ResolvedProperty(GrammarResolver grammarResolver, String value) {
        this.grammarResolverResult = grammarResolver.resolve(value);
    }
    
    public ResolvedProperty(String grammar, String value) {
        this(GrammarParser.parse(grammar), value);
    }

    public List<Token> getTokens() {
        return grammarResolverResult.tokens();
    }
    
    public List<ResolvedToken> getResolvedTokens() {
        return grammarResolverResult.resolved();
    }
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    /**
     * Return an instance of {@link PropertyModel}.
     */
    public PropertyDefinition getPropertyDefinition() {
        return propertyModel;
    }
    
    /**
     * @return true if the property value fully corresponds to the property grammar.
     */
    public boolean isResolved() {
        return grammarResolverResult.success();
    }
    
    /**
     * @return list of unresolved property value tokens. It means these tokens cannot
     * be consumed by the property grammar.
     * 
     * Currently used just by the CssAnalyzer - error checking of the property values
     */
    public List<Token> getUnresolvedTokens() {
        return grammarResolverResult.left();
    }
    
    /**
     * @return set of alternatives which may follow the end of the property value.
     * The values are computed according to the grammar and the existing property value
     */
    public Set<ValueGrammarElement> getAlternatives() {
        return grammarResolverResult.getAlternatives();
    }

    /**
     * @return a parse tree for the property value. 
     * The parse tree contains only named nodes (references).
     * 
     * In most cases clients will use this method.
     */    
    public synchronized Node getParseTree() {
        return grammarResolverResult.getParseTree();
    }
    

    //--------- private -----------
    
    /**
     * @return a text with all css comments replaced by spaces
     */
    private static String filterComments(CharSequence text) {
        Matcher m = FILTER_COMMENTS_PATTERN.matcher(text);
        StringBuilder b = new StringBuilder(text);
        while (m.find()) {
            int from = m.start();
            int to = m.end();
            if (from != to) {
                char[] spaces = new char[to - from];
                Arrays.fill(spaces, ' ');
                String replacement = new String(spaces);
                b.replace(from, to, replacement);
            }
        }
        return b.toString();
    }

   
}
