/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.editor.completion;

import java.util.EnumSet;
import java.util.List;
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
    public boolean complete(List<CompletionProposal> proposals, CompletionContext request, int anchor) {
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
        filterLocation(request.location);
        filterClassInterfaceOrdering(request.context);
        filterMethodDefinitions(request.context);
        filterKeywordsNextToEachOther(request.context);

        // add the remaining keywords to the result

        for (GroovyKeyword groovyKeyword : keywords) {
            LOG.log(Level.FINEST, "Adding keyword proposal : {0}", groovyKeyword.getName()); // NOI18N
            proposals.add(new CompletionItem.KeywordItem(groovyKeyword.getName(), null, anchor, request.getParserResult(), groovyKeyword.isGroovyKeyword()));
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
