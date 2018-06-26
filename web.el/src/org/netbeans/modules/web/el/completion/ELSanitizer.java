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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.el.completion;

import com.sun.el.parser.Node;
import java.util.HashSet;
import java.util.Set;
import javax.el.ELException;
import org.netbeans.modules.el.lexer.api.ELTokenId;
import org.netbeans.modules.web.el.ELElement;
import org.netbeans.modules.web.el.ELParser;
import org.netbeans.modules.web.el.ELPreprocessor;
import org.openide.util.Pair;

/**
 * Attempts to sanitize EL statements. Check the unit test
 * for finding out what cases are currently handled.
 *
 * @author Erno Mononen
 */
public final class ELSanitizer {

    static final String ADDED_SUFFIX = "x"; // NOI18N
    static final String ADDED_QUOTED_SUFFIX = "'x'"; // NOI18N
    private final ELPreprocessor expression;
    private final ELElement element;
    private static final Set<Pair<ELTokenId, ELTokenId>> BRACKETS;
    private final int relativeOffset;

    static {
        BRACKETS = new HashSet<>();
        BRACKETS.add(Pair.of(ELTokenId.LBRACKET, ELTokenId.RBRACKET));
        BRACKETS.add(Pair.of(ELTokenId.LPAREN, ELTokenId.RPAREN));
    }

    public ELSanitizer(ELElement element, int relativeOffset) {
        this.element = element;
        this.expression = element.getExpression();
        this.relativeOffset = relativeOffset;
    }

    /**
     * Attempts to sanitize the contained element.
     * @return Returns a
     * sanitized copy of the element if sanitization was successful, otherwise
     * the element itself. In other words, the returned element is <strong>not</strong>
     * guaranteed to be valid.
     */
    public ELElement sanitized() {
        try {
            String sanitizedExpression = sanitize(expression.getOriginalExpression(), relativeOffset); //use original expression!
            ELPreprocessor elp = new ELPreprocessor(sanitizedExpression, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);
            Node sanitizedNode = ELParser.parse(elp);
            return element.makeValidCopy(sanitizedNode, elp);
        } catch (ELException ex) {
            return element;
        }
    }

    // package private for unit tests
    static String sanitize(final String expression) {
        return sanitize(expression, -1);
    }
    
    static String sanitize(final String expression, int relativeOffset) {
        boolean closingCurlyBracketAdded = false;
        String copy = expression;
        if (!expression.endsWith("}")) {
            copy += "}";
            closingCurlyBracketAdded = true;
        }
        CleanExpression cleanExpression = CleanExpression.getCleanExression(copy);
        if (cleanExpression == null) {
            return expression;
        }
        //the CleanExpression removed the #{ or ${ prefix
        relativeOffset -= 2;
        
        String result = cleanExpression.clean;
        if (closingCurlyBracketAdded && relativeOffset >= 0) {
            result = secondPass(result, relativeOffset);
        }
        if (result.trim().isEmpty()) {
            result += ADDED_SUFFIX;
        }

        // resolve completion invoked within the EL
        if (relativeOffset > 0 && relativeOffset < result.length()) {
            String exprEnd = result.substring(relativeOffset);
            String exprStart = result.substring(0, relativeOffset);
            result = thirdPass(exprStart, exprEnd) + exprEnd;
        } else {
            result = thirdPass(result, ""); //NOI18N
        }
        return cleanExpression.prefix + result + cleanExpression.suffix;
    }

    //unclosed expressions handling
    private static String secondPass(String expression, int relativeOffset) {
        //Cut everything after up to the relative offset (caret)
        return expression.substring(0, relativeOffset);
    }

    private static String thirdPass(String expression, String ending) {
        String spaces = "";
        if (expression.endsWith(" ")) {
            int lastNonWhiteSpace = findLastNonWhiteSpace(expression);
            if (lastNonWhiteSpace > 0) {
                spaces = expression.substring(lastNonWhiteSpace + 1);
                expression = expression.substring(0, lastNonWhiteSpace + 1);
            }
        }

        if (!expression.isEmpty()) {
            char lastChar = expression.charAt(expression.length() - 1);
            if (lastChar == '\'' || lastChar == '"') { //NOI18N
                expression += lastChar;
            }
        }

        for (ELTokenId elToken : ELTokenId.values()) {
            if (elToken.fixedText() == null || !expression.endsWith(elToken.fixedText())) {
                continue;
            }
            // special handling for brackets
            for (Pair<ELTokenId, ELTokenId> bracket : BRACKETS) {
                if (expression.endsWith(bracket.first().fixedText())) {
                    if (expression.endsWith(ELTokenId.LBRACKET.fixedText())) {
                        return expression + ADDED_QUOTED_SUFFIX + bracket.second().fixedText();
                    }
                    return expression + bracket.second().fixedText();
                } else if (expression.endsWith(bracket.second().fixedText())) {
                    if (expression.endsWith(ELTokenId.RBRACKET.fixedText())) {
                        // e.g. #{bean.items[|]}
                        return expression.substring(0, expression.length() - 1) + ADDED_QUOTED_SUFFIX + ELTokenId.RBRACKET.fixedText();
                    } else if (expression.endsWith(ELTokenId.DOT.fixedText() + ELTokenId.RPAREN.fixedText())
                        // for opened classname call - e.g. #{(java.|)}
                            || expression.endsWith(ELTokenId.LAMBDA.fixedText() + ELTokenId.RPAREN.fixedText())) {
                            // for started lambda expression - e.g. #{[1,4].stream().peek(i->|)}
                        return expression.substring(0, expression.length() - 1) + ADDED_SUFFIX + ELTokenId.RPAREN.fixedText();
                    }
                }
            }
            // sanitizes cases where the expressions ends with dot and spaces,
            // e.g. #{foo.  }
            if (ELTokenId.DOT == elToken) {
                if (unbalancedLeftParen(expression + ending)) {
                    return expression + ADDED_SUFFIX + ELTokenId.RPAREN.fixedText() + spaces ;
                } else {
                    return expression + ADDED_SUFFIX + spaces ;
                }
            }

            // for COLON - e.g. #{foo:
            if (ELTokenId.COLON == elToken) {
                return expression + ADDED_SUFFIX + ELTokenId.LPAREN.fixedText()
                        + ELTokenId.RPAREN.fixedText() + spaces;
            }

            // for operators
            if (ELTokenId.ELTokenCategories.OPERATORS.hasCategory(elToken)) {
                return expression + spaces + ADDED_SUFFIX;
            }
            if (ELTokenId.ELTokenCategories.KEYWORDS.hasCategory(elToken)) {
                return expression + " " + spaces + ADDED_SUFFIX;
            }
        }

        // for COLON - e.g. #{foo:foo
        if (expression.contains(ELTokenId.COLON.fixedText())) {
            return expression + ELTokenId.LPAREN.fixedText() + ELTokenId.RPAREN.fixedText() + spaces;
        }

        if (unbalancedLeftBracket(expression)) {
            return expression + ELTokenId.RBRACKET.fixedText();
        }

        return expression + spaces;
    }

    private static boolean unbalancedLeftBracket(String expression) {
        return (expression.indexOf(ELTokenId.LBRACKET.fixedText()) > expression.indexOf(ELTokenId.RBRACKET.fixedText()));
    }

    private static boolean unbalancedLeftParen(String expression) {
        return (expression.indexOf(ELTokenId.LPAREN.fixedText()) > expression.indexOf(ELTokenId.RPAREN.fixedText()));
    }

    // package private for tests
    static int findLastNonWhiteSpace(String str) {
        int lastNonWhiteSpace = -1;
        for (int i = str.length() - 1; i >= 0; i--) {
            if (!Character.isWhitespace(str.charAt(i))) {
                lastNonWhiteSpace = i;
                break;
            }
        }
        return lastNonWhiteSpace;
    }

    private static class CleanExpression {

        private final String clean, prefix, suffix;

        public CleanExpression(String clean, String prefix, String suffix) {
            this.clean = clean;
            this.prefix = prefix;
            this.suffix = suffix;
        }

        private static CleanExpression getCleanExression(String expression) {
            if ((expression.startsWith("#{") || expression.startsWith("${"))
                    && expression.endsWith("}")) {

                String prefix = expression.substring(0, 2);
                String clean = expression.substring(2, expression.length() - 1);
                String suffix = expression.substring(expression.length() - 1);
                return new CleanExpression(clean, prefix, suffix);
            }
            return null;
        }
    }
}
