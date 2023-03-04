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
