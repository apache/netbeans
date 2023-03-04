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

package org.netbeans.modules.groovy.editor.completion;

import java.util.EnumSet;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.groovy.editor.api.completion.CaretLocation;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.GroovyKeyword;
import org.netbeans.modules.groovy.editor.api.completion.KeywordCategory;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionSurrounding;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;

/**
 * Complete Groovy or Java Keywords.
 *
 * @see GroovyKeyword for matrix of capabilities, scope and allowed usage.
 * @author Martin Janicek
 */
class KeywordCompletion extends BaseCompletion {

    private EnumSet<GroovyKeyword> keywords;
    private CompletionContext request;


    @Override
    public boolean complete(Map<Object, CompletionProposal> proposals, CompletionContext request, int anchor) {
        this.request = request;

        LOG.log(Level.FINEST, "-> completeKeywords"); // NOI18N
        String prefix = request.getPrefix();

        if (request.location == CaretLocation.INSIDE_PARAMETERS) {
            return false;
        }
        
        if (request.dotContext != null) {
            if (request.dotContext.isFieldsOnly() || request.dotContext.isMethodsOnly()) {
                return false;
            }
        }

        // We are after either implements or extends keyword
        if ((request.context.beforeLiteral != null && request.context.beforeLiteral.id() == GroovyTokenId.LITERAL_implements) ||
            (request.context.beforeLiteral != null && request.context.beforeLiteral.id() == GroovyTokenId.LITERAL_extends)) {
            return false;
        }

        if (request.isBehindDot()) {
            LOG.log(Level.FINEST, "We are invoked right behind a dot."); // NOI18N
            return false;
        }

        // Is there already a "package"-statement in the sourcecode?
        boolean havePackage = checkForPackageStatement(request);

        keywords = EnumSet.allOf(GroovyKeyword.class);

        // filter-out keywords in a step-by-step approach
        filterPackageStatement(havePackage);
        filterPrefix(prefix);
        if (keywords.contains(GroovyKeyword.KEYWORD_package) && SpockUtils.isFirstStatement(request)) {
            // This is a hack for offering package keyword in the script as the first statement.
            // The current implementation use INSIDE_LOCATION for the top context in script, which is OK, 
            // but package is only above class keyword and will not be displayed here. 
            // This covers case, when you have empty file and you want to write package as the first. 
            filterLocation(request.location);
            if (!keywords.contains(GroovyKeyword.KEYWORD_package)) {
                // the package is only above class keyword, but on the first position we should offer it
                keywords.add(GroovyKeyword.KEYWORD_package);
            }
        } else {
            filterLocation(request.location);
        }
        filterClassInterfaceOrdering(request.context);
        filterMethodDefinitions(request.context);
        filterKeywordsNextToEachOther(request.context);

        // add the remaining keywords to the result

        for (GroovyKeyword groovyKeyword : keywords) {
            LOG.log(Level.FINEST, "Adding keyword proposal : {0}", groovyKeyword.getName()); // NOI18N
            proposals.put("keyword:" + groovyKeyword.getName(), 
                    new CompletionItem.KeywordItem(groovyKeyword.getName(), null, anchor, request.getParserResult(), groovyKeyword.isGroovyKeyword()));
        }

        return true;
    }

    boolean checkForPackageStatement(final CompletionContext request) {
        TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(request.doc, 1);

        if (ts != null) {
            ts.move(1);

            while (ts.isValid() && ts.moveNext() && ts.offset() < request.doc.getLength()) {
                Token<GroovyTokenId> t = ts.token();

                if (t.id() == GroovyTokenId.LITERAL_package) {
                    return true;
                }
            }
        }

        return false;
    }

    // filter-out package-statemen, if there's already one
    void filterPackageStatement(boolean havePackage) {
        for (GroovyKeyword groovyKeyword : keywords) {
            if (groovyKeyword.getName().equals("package") && havePackage) {
                keywords.remove(groovyKeyword);
            }
        }
    }

    void filterPrefix(String prefix) {
        for (GroovyKeyword groovyKeyword : keywords) {
            if (!groovyKeyword.getName().startsWith(prefix)) {
                keywords.remove(groovyKeyword);
            }
        }
    }

    void filterLocation(CaretLocation location) {
        for (GroovyKeyword groovyKeyword : keywords) {
            if (!checkKeywordAllowance(groovyKeyword, location)) {
                keywords.remove(groovyKeyword);
            }
        }
    }

    // Filter right Keyword ordering
    void filterClassInterfaceOrdering(CompletionSurrounding ctx) {
        if (ctx == null || ctx.beforeLiteral == null) {
            return;
        }

        if (ctx.beforeLiteral.id() == GroovyTokenId.LITERAL_interface) {
            keywords.clear();
            addIfPrefixed(GroovyKeyword.KEYWORD_extends);
        } else if (ctx.beforeLiteral.id() == GroovyTokenId.LITERAL_class) {
            keywords.clear();
            addIfPrefixed(GroovyKeyword.KEYWORD_extends);
            addIfPrefixed(GroovyKeyword.KEYWORD_implements);
        } else if (ctx.beforeLiteral.id() == GroovyTokenId.LITERAL_trait) {
            keywords.clear();
            addIfPrefixed(GroovyKeyword.KEYWORD_implements);
        }
    }

    private void addIfPrefixed(GroovyKeyword keyword) {
        if (isPrefixed(request, keyword.getName())) {
            keywords.add(keyword);
        }
    }

    // Filter-out modifier/datatype ordering in method definitions
    void filterMethodDefinitions(CompletionSurrounding ctx) {
        if (ctx == null || ctx.afterLiteral == null) {
            return;
        }

        if (ctx.afterLiteral.id() == GroovyTokenId.LITERAL_void ||
            ctx.afterLiteral.id() == GroovyTokenId.IDENTIFIER ||
            ctx.afterLiteral.id().primaryCategory().equals("number")) {

            // we have to filter-out the primitive types

            for (GroovyKeyword groovyKeyword : keywords) {
                if (groovyKeyword.getCategory() == KeywordCategory.PRIMITIVE) {
                    LOG.log(Level.FINEST, "filterMethodDefinitions - removing : {0}", groovyKeyword.getName());
                    keywords.remove(groovyKeyword);
                }
            }
        }
    }

    // Filter-out keywords, if we are surrounded by others.
    // This can only be an approximation.
    void filterKeywordsNextToEachOther(CompletionSurrounding ctx) {
        if (ctx == null) {
            return;
        }

        boolean filter = false;
        if (ctx.after1 != null && ctx.after1.id().primaryCategory().equals("keyword")) {
            filter = true;
        }
        if (ctx.before1 != null && ctx.before1.id().primaryCategory().equals("keyword")) {
            filter = true;
        }
        if (filter) {
            for (GroovyKeyword groovyKeyword : keywords) {
                if (groovyKeyword.getCategory() == KeywordCategory.KEYWORD) {
                    LOG.log(Level.FINEST, "filterMethodDefinitions - removing : {0}", groovyKeyword.getName());
                    keywords.remove(groovyKeyword);
                }
            }
        }
    }

    boolean checkKeywordAllowance(GroovyKeyword groovyKeyword, CaretLocation location) {
        if (location == null) {
            return false;
        }

        switch (location) {
            case ABOVE_FIRST_CLASS:
                if (groovyKeyword.isAboveFistClass()) {
                    return true;
                }
                break;
            case OUTSIDE_CLASSES:
                if (groovyKeyword.isOutsideClasses()) {
                    return true;
                }
                break;
            case INSIDE_CLASS:
                if (groovyKeyword.isInsideClass()) {
                    return true;
                }
                break;
            case INSIDE_METHOD: // intentionally fall-through
            case INSIDE_CLOSURE:
                if (groovyKeyword.isInsideCode()) {
                    return true;
                }
                break;
        }
        return false;
    }
}
