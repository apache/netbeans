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
