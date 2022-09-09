/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.languages.antlr.v3;

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.LexerRestartInfo;

import static org.antlr.parser.antlr3.ANTLRv3Lexer.*;
import org.netbeans.modules.languages.antlr.AbstractAntlrLexer;
import org.netbeans.modules.languages.antlr.AntlrTokenId;
import static org.netbeans.modules.languages.antlr.AntlrTokenId.*;
import org.netbeans.modules.languages.antlr.LexerInputCharStream;

/**
 *
 * @author lkishalmi
 */
public final class Antlr3Lexer extends AbstractAntlrLexer {


    public Antlr3Lexer(LexerRestartInfo<AntlrTokenId> info) {
        super(info, new org.antlr.parser.antlr3.ANTLRv3Lexer(new LexerInputCharStream(info.input())));
    }

    private org.antlr.v4.runtime.Token preFetchedToken = null;

    @Override
    public Token<AntlrTokenId> nextToken() {
        org.antlr.v4.runtime.Token nextToken;
        if (preFetchedToken != null) {
            nextToken = preFetchedToken;
            lexer.getInputStream().seek(preFetchedToken.getStopIndex() + 1);
            preFetchedToken = null;
        } else {
            nextToken = lexer.nextToken();
        }
        if (nextToken.getType() == EOF) {
            return null;
        }
        switch (nextToken.getType()) {
//             PARSER=2, LEXER=3, RULE=4, BLOCK=5, OPTIONAL=6, CLOSURE=7,
//		POSITIVE_CLOSURE=8, SYNPRED=9, RANGE=10, CHAR_RANGE=11, EPSILON=12, ALT=13,
//		EOR=14, EOB=15, EOA=16, ID=17, ARG=18, ARGLIST=19, RET=20, LEXER_GRAMMAR=21,
//		PARSER_GRAMMAR=22, TREE_GRAMMAR=23, COMBINED_GRAMMAR=24, INITACTION=25,
//		LABEL=26, TEMPLATE=27, SCOPE=28, SEMPRED=29, GATED_SEMPRED=30, SYN_SEMPRED=31,
//		BACKTRACK_SEMPRED=32, FRAGMENT=33, TREE_BEGIN=34, ROOT=35, BANG=36, REWRITE=37,
//		ACTION_CONTENT=38, SL_COMMENT=39, ML_COMMENT=40, INT=41, CHAR_LITERAL=42,
//		STRING_LITERAL=43, DOUBLE_QUOTE_STRING_LITERAL=44, DOUBLE_ANGLE_STRING_LITERAL=45,
//		BEGIN_ARGUMENT=46, BEGIN_ACTION=47, OPTIONS=48, TOKENS=49, CATCH=50, FINALLY=51,
//		GRAMMAR=52, PRIVATE=53, PROTECTED=54, PUBLIC=55, RETURNS=56, THROWS=57,
//		TREE=58, AT=59, COLON=60, COLONCOLON=61, COMMA=62, DOT=63, EQUAL=64, LBRACE=65,
//		LBRACK=66, LPAREN=67, OR=68, PLUS=69, QM=70, RBRACE=71, RBRACK=72, RPAREN=73,
//		SEMI=74, SEMPREDOP=75, STAR=76, DOLLAR=77, PEQ=78, NOT=79, WS=80, TOKEN_REF=81,
//		RULE_REF=82, END_ARGUMENT=83, UNTERMINATED_ARGUMENT=84, ARGUMENT_CONTENT=85,
//		END_ACTION=86, UNTERMINATED_ACTION=87, OPT_LBRACE=88, LEXER_CHAR_SET=89,
//		UNTERMINATED_CHAR_SET=90;
            case TOKEN_REF:
                return token(AntlrTokenId.TOKEN);
            case RULE_REF:
                return token(AntlrTokenId.RULE);

            case DOC_COMMENT:
            case ML_COMMENT:
            case SL_COMMENT:
                return token(AntlrTokenId.COMMENT);

            case INT:
                return token(NUMBER);

            case OPTIONS:
            case TOKENS:
            case CATCH:
            case FINALLY:
            case GRAMMAR:
            case LEXER:
            case PARSER:
            case PRIVATE:
            case PROTECTED:
            case PUBLIC:
            case RETURNS:
            case SCOPE:
            case THROWS:
            case TREE:
                return token(KEYWORD);

            case WS:
                return token(WHITESPACE);

            case ACTION_CONTENT:
                preFetchedToken = lexer.nextToken();
                while (preFetchedToken.getType() == ACTION_CONTENT) {
                    preFetchedToken = lexer.nextToken();
                }
                lexer.getInputStream().seek(preFetchedToken.getStartIndex());
                return token(ACTION);

            default:
                return token(ERROR);
        }
    }

}
