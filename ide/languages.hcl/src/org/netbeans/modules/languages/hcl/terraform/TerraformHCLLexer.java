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
package org.netbeans.modules.languages.hcl.terraform;

import org.netbeans.api.lexer.Token;
import org.netbeans.modules.languages.hcl.AnstractHCLLexer;
import org.netbeans.modules.languages.hcl.HCLTokenId;
import static org.netbeans.modules.languages.hcl.HCLTokenId.*;
import org.netbeans.modules.languages.hcl.grammar.TerraformLexer;
import static org.netbeans.modules.languages.hcl.grammar.TerraformLexer.*;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class TerraformHCLLexer extends AnstractHCLLexer {

    public TerraformHCLLexer(LexerRestartInfo<HCLTokenId> info) {
        super(info, TerraformLexer::new);
    }

    @Override
    protected Token<HCLTokenId> mapToken(org.antlr.v4.runtime.Token antlrToken) {
        switch (antlrToken.getType()) {
            case LINE_COMMENT:
            case BLOCK_COMMENT:
                return token(COMMENT);

            case BOOL_LIT:
                return token(BOOLEAN);

            case NUMERIC_LIT:
                return token(HCLTokenId.NUMBER);

            case TerraformLexer.IDENTIFIER:
                return token(HCLTokenId.IDENTIFIER);

            case BOOL:
            case DATA:
            case LIST:
            case LOCALS:
            case LOCAL:
            case MAP:
            case MODULE:
            case MOVED:
            case TerraformLexer.NUMBER:
            case OBJECT:
            case OPTIONAL:
            case OUTPUT:
            case PROVIDER:
            case RESOURCE:
            case TERRAFORM:
            case VAR:
            case VARIABLE:
            case FOR:
            case IF:
            case IN:
            case NULL:
                return token(KEYWORD);

            case LBRACE:
            case RBRACE:
            case LBRACK:
            case RBRACK:
            case LPAREN:
            case RPAREN:
            case COMMA:
            case DOT:
            case INTERPOLATION_START:
            case INTERPOLATION_END:
            case RARROW:
            case TEMPLATE_START:
            case TEMPLATE_END:
                return token(SEPARATOR);

            case AND:
            case COLON:
            case ELLIPSIS:
            case EQUAL:
            case EQUALS:
            case GT:
            case GTE:
            case LT:
            case LTE:
            case NOT:
            case NOT_EQUALS:
            case OR:
            case PERCENT:
            case PLUS:
            case QUESTION:
                return token(OPERATOR);

            case QUOTE:
                return token(HCLTokenId.STRING);
            case HEREDOC_START:
            case HEREDOC_END:
                return token(HEREDOC_GUARD);
            case HEREDOC_CONTENT:
                return groupToken(HEREDOC, HEREDOC_CONTENT);

            case STRING_CONTENT:
                return groupToken(HCLTokenId.STRING, STRING_CONTENT);

            case INTERPOLATION_CONTENT:
                return groupToken(INTERPOLATION, INTERPOLATION_CONTENT);

            case TEMPLATE_CONTENT:
                return groupToken(ERROR, TEMPLATE_CONTENT);
            case WS:
            case NL:
                return token(WHITESPACE);

            default:
                return token(ERROR);
        }
    }
}
