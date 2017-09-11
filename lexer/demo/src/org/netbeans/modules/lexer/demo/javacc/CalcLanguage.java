/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.modules.lexer.demo.javacc;

import org.netbeans.api.lexer.Lexer;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.AbstractLanguage;
import org.netbeans.spi.lexer.MatcherFactory;

public class CalcLanguage extends AbstractLanguage {

    /** Maximum lexer state determined from xxxConstants class. */
    static final int MAX_STATE = 1;

    /** Lazily initialized singleton instance of this language. */
    private static CalcLanguage INSTANCE;

    /** @return singleton instance of this language. */
    public static synchronized CalcLanguage get() {
        if (INSTANCE == null)
            INSTANCE = new CalcLanguage();

        return INSTANCE;
    }

    public static final int WHITESPACE_INT = 1;
    public static final int INCOMPLETE_ML_COMMENT_INT = 2;
    public static final int ML_COMMENT_INT = 3;
    public static final int PLUS_INT = 4;
    public static final int MINUS_INT = 5;
    public static final int MUL_INT = 6;
    public static final int DIV_INT = 7;
    public static final int MUL3_INT = 8;
    public static final int PLUS5_INT = 9;
    public static final int LPAREN_INT = 10;
    public static final int RPAREN_INT = 11;
    public static final int CONSTANT_INT = 12;
    public static final int ML_COMMENT_END_INT = 16;
    public static final int ERROR_INT = 17;


    public static final TokenId CONSTANT = new TokenId("constant", CONSTANT_INT, new String[]{"literal"});
    public static final TokenId DIV = new TokenId("div", DIV_INT, new String[]{"operator"}, MatcherFactory.createTextCheckMatcher("/"));
    public static final TokenId ERROR = new TokenId("error", ERROR_INT, new String[]{"error"});
    public static final TokenId INCOMPLETE_ML_COMMENT = new TokenId("incomplete-ml-comment", INCOMPLETE_ML_COMMENT_INT, new String[]{"comment", "incomplete", "error"});
    public static final TokenId LPAREN = new TokenId("lparen", LPAREN_INT, new String[]{"separator"}, MatcherFactory.createTextCheckMatcher("("));
    public static final TokenId MINUS = new TokenId("minus", MINUS_INT, new String[]{"operator"}, MatcherFactory.createTextCheckMatcher("-"));
    public static final TokenId ML_COMMENT = new TokenId("ml-comment", ML_COMMENT_INT, new String[]{"comment"});
    public static final TokenId ML_COMMENT_END = new TokenId("ml-comment-end", ML_COMMENT_END_INT, new String[]{"error"}, MatcherFactory.createTextCheckMatcher("*/"));
    public static final TokenId MUL = new TokenId("mul", MUL_INT, new String[]{"operator"}, MatcherFactory.createTextCheckMatcher("*"));
    public static final TokenId MUL3 = new TokenId("mul3", MUL3_INT, new String[]{"operator"}, MatcherFactory.createTextCheckMatcher("***")); // Special token for testing extra lookahead and lookback
    public static final TokenId PLUS = new TokenId("plus", PLUS_INT, new String[]{"operator"}, MatcherFactory.createTextCheckMatcher("+"));
    public static final TokenId PLUS5 = new TokenId("plus5", PLUS5_INT, new String[]{"operator"}, MatcherFactory.createTextCheckMatcher("+++++")); // Special token for testing extra lookahead and lookback
    public static final TokenId RPAREN = new TokenId("rparen", RPAREN_INT, new String[]{"separator"}, MatcherFactory.createTextCheckMatcher(")"));
    public static final TokenId WHITESPACE = new TokenId("whitespace", WHITESPACE_INT, new String[]{"whitespace"}, MatcherFactory.createTextCheckMatcher(" "));

    CalcLanguage() {
    }

    public Lexer createLexer() {
        return new CalcLexer();
    }

}
