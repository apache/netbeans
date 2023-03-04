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

package org.netbeans.modules.groovy.gsp.lexer;

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Syntax class for GSP tags, recognizing GSP delimiters.
 *
 * @author Martin Adamek
 * @author Martin Janicek
 */

public final class GspLexer implements Lexer<GspTokenId> {

    private enum LexerState {
        // The comment 'after something' indicates which characters have been already
        // read (e.g. if we are in JEXPR state, we have already read characters '<%=')
        // The part 'expecting something' shows us what we are expecting as a next char
        // (e.g. in JEXPR state we are waiting for next % character and then we can move
        // to the next valid state)

        INIT,                            // nothing read yet, nothing expected
        LT,                              // after '<'             expecting %, g, / or !

        // Groovy expression
        DL,                              // after '$'             expecting {
        GEXPRESSION,                     // after '${ ...'        expecting }

        // Opening GTag states
        LT_G,                            // after '<g'            expecting :
        GSTART_TAG,                      // after '<g:   ...'     expecting > or tag name
        GSTART_TAG_WITH_NAME,            // after '<g:if ...'     expecting /, > or some attribute name with = at the end
        GSTART_TAG_WITH_NAME_ATTR,       // after '<g:if attr='   expecting /, >, ", space or some attribute value
        GSTART_TAG_WITH_NAME_ATTR_VALUE, // after '<g:if attr="'  expecting ", /, $ or \
        GSTART_TAG_EXPR,                 // after '<g:if ... $'   expecting {
        GSTART_TAG_EXPR_PC,              // after '<g:if ... ${'  expecting }
        GSTART_TAG_BACKSLASH,            // after '<g:if ... \'   expecting $
        GSTART_TAG_BACKSLASH_EXPR,       // after '<g:if ... \$'  expecting {
        GINDEPENDENT_TAG,                // after '<g:   ... /'   expecting >
        GINDEPENDENT_TAG_WITH_NAME,      // after '<g:if ... /'   expecting >

        // Closing GTag states
        LT_BS,                           // after '</'            expecting g
        LT_BS_G,                         // after '</g'           expecting :
        GEND_TAG,                        // after '</g:  ...'     expecting > or tag name
        GEND_TAG_WITH_NAME,              // after '</g:if...'     expecting >

        // GSP style comment states
        PC,                              // after '%'             expecting {
        PC_CB,                           // after '%{'            expecting -
        PC_CB_DASH,                      // after '%{-'           expecting -
        PC_CB_DOUBLE_DASH,               // after '%{--'          expecting -
        PC_CB_TRIPLE_DASH,               // after '%{-- ... -'    expecting -
        PC_CB_QUADRUPLE_DASH,            // after '%{-- ... --'   expecting }
        PC_CB_QUADRUPLE_DASH_CB,         // after '%{-- ... --}'  expecting %

        // HTML style comment states
        LT_EM,                           // after '<!'            expecting -
        LT_EM_DASH,                      // after '<!-'           expecting -
        LT_EM_DOUBLE_DASH,               // after '<!--'          expecting -
        LT_EM_TRIPLE_DASH,               // after '<!-- ... -'    expecting -
        LT_EM_QUADRUPLE_DASH,            // after '<!-- ... --'   expecting >

        // JSP style comment states
        LT_PC_DASH,                      // after '<%-'           expecting -
        LT_PC_DOUBLE_DASH,               // after '<%--'          expecting -
        LT_PC_TRIPLE_DASH,               // after '<%-- ... -'    expecting -
        LT_PC_QUADRUPLE_DASH,            // after '<%-- ... --'   expecting %
        LT_PC_QUADRUPLE_DASH_PC,         // after '<%-- ... --%'  expecting >

        // Page directives
        PAGE_DIRECTIVE,                  // after '<%@ ...'       expecting 'p'
        PAGE_DIRECTIVE_P,                // after '<%@ p'         expecting 'a'
        PAGE_DIRECTIVE_PA,               // after '<%@ pa'        expecting 'g'
        PAGE_DIRECTIVE_PAG,              // after '<%@ pag'       expecting 'e'
        PAGE_DIRECTIVE_WITH_NAME,        // after '<%@page ...'   expecting some attribute name with = at the end
        PAGE_DIRECTIVE_WITH_NAME_ATTR,   // after '<%@page attr=' expecting some attribute value
        PAGE_DIRECTIVE_PC,               // after '<%@ ... %'     expecting >

        // Scriptlets, GStrings
        LT_PC,                           // after '<%'            expecting =, @, %, -
        JSCRIPT,                         // after '<% ...'        expecting %
        JSCRIPT_PC,                      // after '<% ... %'      expecting >
        JEXPR,                           // after '<%= ...'       expecting %
        JEXPR_PC,                        // after '<%= ... %'     expecting >
    }

    private final TokenFactory<GspTokenId> tokenFactory;
    private final LexerInput input;

    private LexerState state;


    public GspLexer(LexerRestartInfo<GspTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        if (info.state() == null) {
            state = LexerState.INIT;
        } else {
            state = (LexerState) info.state();
        }
    }

    @Override
    public Token<GspTokenId> nextToken() {
        final GspTokenId tokenId = nextTokenId();
        if (tokenId == null) {
            return null; // EOF
        }
        return tokenFactory.createToken(tokenId);
    }

    private GspTokenId nextTokenId() {
        while (true) {
            int actChar = input.read();
            if (actChar == LexerInput.EOF) {
                if (input.readLengthEOF() == 1) {
                    return null; // Just EOL is read
                } else {
                    // There is something else in the buffer except EOL. We will return last
                    // token now. Backup the EOL, we will return null in next nextToken() call
                    input.backup(1);
                    break;
                }
            }

            switch (state) {
                case INIT: // Nothing read yet
                    switch (actChar) {
                        case '<': state = LexerState.LT; break;
                        case '$': state = LexerState.DL; break;
                        case '%': state = LexerState.PC; break;
                        case '>': return GspTokenId.HTML;
                        case '\n': return GspTokenId.WHITESPACE;
                        default: break;
                    }
                    break;
                case LT: // after <
                    switch (actChar) {
                        case '%': state = LexerState.LT_PC; break;
                        case 'g': state = LexerState.LT_G; break;
                        case '!': state = LexerState.LT_EM; break;
                        case '/': state = LexerState.LT_BS; break;
                        default:  state = LexerState.INIT; break;
                    }
                    break;
                case DL: // after $
                    switch (actChar) {
                        case '{':
                            if (isContentRead(2)) {
                                state = LexerState.INIT;
                                return GspTokenId.HTML;
                            } else {
                                state = LexerState.GEXPRESSION;
                                return GspTokenId.GSTRING_START;
                            }
                        default:
                            state = LexerState.INIT;
                            break;
                    }
                    break;
                case PC: // after %
                    switch (actChar) {
                        case '{':
                            state = LexerState.PC_CB;
                            break;
                        default:
                            state = LexerState.INIT; //just content
                            break;
                    }
                    break;
                case PC_CB: // after %{
                    switch (actChar) {
                        case '-':
                            state = LexerState.PC_CB_DASH;
                            break;
                        default:
                            state = LexerState.INIT;
                            break;
                    }
                    break;
                case PC_CB_DASH: // after %{-
                    switch (actChar) {
                        case '-':
                            state = LexerState.PC_CB_DOUBLE_DASH;
                            return GspTokenId.COMMENT_GSP_STYLE_START;
                        default:
                            state = LexerState.INIT;
                            break;
                    }
                    break;
                case PC_CB_DOUBLE_DASH: // after %{--
                    switch (actChar) {
                        case '-':
                            state = LexerState.PC_CB_TRIPLE_DASH;
                            break;
                        default:
                            state = LexerState.PC_CB_DOUBLE_DASH;
                            break;
                    }
                    break;
                case PC_CB_TRIPLE_DASH: // after %{-- ... -
                    switch (actChar) {
                        case '-':
                            state = LexerState.PC_CB_QUADRUPLE_DASH;
                            break;
                        default:
                            state = LexerState.PC_CB_DOUBLE_DASH;
                            break;
                    }
                    break;
                case PC_CB_QUADRUPLE_DASH: // after %{-- ... --
                    switch (actChar) {
                        case '}':
                            state = LexerState.PC_CB_QUADRUPLE_DASH_CB;
                            break;
                        default:
                            state = LexerState.PC_CB_DOUBLE_DASH;
                            break;
                    }
                    break;
                case PC_CB_QUADRUPLE_DASH_CB: // after %{-- ... --}
                    switch (actChar) {
                        case '%':
                            if (isContentRead(4)) {
                                state = LexerState.PC_CB_DOUBLE_DASH;
                                return GspTokenId.COMMENT_GSP_STYLE_CONTENT;
                            } else {
                                state = LexerState.INIT;
                                return GspTokenId.COMMENT_GSP_STYLE_END;
                            }
                        default:
                            state = LexerState.PC_CB_DOUBLE_DASH;
                            break;
                    }
                    break;
                case LT_PC: // after <%
                    switch (actChar) {
                        case '=':
                            state = LexerState.JEXPR;
                            return GspTokenId.SCRIPTLET_OUTPUT_VALUE_START;
                        case '@':
                            state = LexerState.PAGE_DIRECTIVE;
                            return GspTokenId.PAGE_DIRECTIVE_START;
                        case '-':
                            state = LexerState.LT_PC_DASH;
                            break;
                        default:
                            input.backup(1); // backup the third character, it is a part of the Groovy scriptlet
                            state = LexerState.JSCRIPT;
                            return GspTokenId.SCRIPTLET_START;
                    }
                    break;
                case LT_PC_DASH: // after <%-
                    switch (actChar) {
                        case '-':
                            state = LexerState.LT_PC_DOUBLE_DASH;
                            return GspTokenId.COMMENT_JSP_STYLE_START;
                        default:
                            state = LexerState.INIT;
                            break;
                    }
                    break;
                case LT_PC_DOUBLE_DASH: // after <%--
                    switch (actChar) {
                        case '-':
                            state = LexerState.LT_PC_TRIPLE_DASH;
                            break;
                        default:
                            state = LexerState.LT_PC_DOUBLE_DASH;
                            break;
                    }
                    break;
                case LT_PC_TRIPLE_DASH: // after <%-- ... -
                    switch (actChar) {
                        case '-':
                            state = LexerState.LT_PC_QUADRUPLE_DASH;
                            break;
                        default:
                            state = LexerState.LT_PC_DOUBLE_DASH;
                            break;
                    }
                    break;
                case LT_PC_QUADRUPLE_DASH: // after <%-- ... --
                    switch (actChar) {
                        case '%':
                            state = LexerState.LT_PC_QUADRUPLE_DASH_PC;
                            break;
                        default:
                            state = LexerState.LT_PC_DOUBLE_DASH;
                            break;
                    }
                    break;
                case LT_PC_QUADRUPLE_DASH_PC: // after <%-- ... --%
                    switch (actChar) {
                        case '>':
                            if (isContentRead(4)) {
                                state = LexerState.LT_PC_DOUBLE_DASH;
                                return GspTokenId.COMMENT_JSP_STYLE_CONTENT;
                            } else {
                                state = LexerState.INIT;
                                return GspTokenId.COMMENT_JSP_STYLE_END;
                            }
                        default:
                            state = LexerState.LT_PC_DOUBLE_DASH;
                            break;
                    }
                    break;
                case LT_G: // after <g
                    switch (actChar) {
                        case ':':
                            if (isContentRead(3)) {
                                state = LexerState.INIT;
                                return GspTokenId.HTML;
                            } else {
                                state = LexerState.GSTART_TAG;
                                return GspTokenId.GTAG_OPENING_START;
                            }
                        default:
                            state = LexerState.INIT;
                    }
                    break;
                case LT_EM: // after <!
                    switch (actChar) {
                        case '-':
                            state = LexerState.LT_EM_DASH;
                            break;
                        default:
                            state = LexerState.INIT;
                            break;
                    }
                    break;
                case LT_EM_DASH: // after <!-
                    switch (actChar) {
                        case '-':
                            state = LexerState.LT_EM_DOUBLE_DASH;
                            return GspTokenId.COMMENT_HTML_STYLE_START;
                        default:
                            state = LexerState.INIT;
                            break;
                    }
                    break;
                case LT_EM_DOUBLE_DASH: // after <!--
                    switch (actChar) {
                        case '-':
                            state = LexerState.LT_EM_TRIPLE_DASH;
                            break;
                        default:
                            // We are already inside of the comment, wait there for the end tokens
                            state = LexerState.LT_EM_DOUBLE_DASH;
                            break;
                    }
                    break;
                case LT_EM_TRIPLE_DASH: // after <!-- ... -
                    switch (actChar) {
                        case '-':
                            state = LexerState.LT_EM_QUADRUPLE_DASH;
                            break;
                        default:
                            // Anything else than second 'end' dash means we are still in comment
                            state = LexerState.LT_EM_DOUBLE_DASH;
                            break;
                    }
                    break;
                case LT_EM_QUADRUPLE_DASH: // after <!-- ... --
                    switch (actChar) {
                        case '>':
                            if (isContentRead(3)) {
                                state = LexerState.LT_EM_DOUBLE_DASH;
                                return GspTokenId.COMMENT_HTML_STYLE_CONTENT;
                            } else {
                                state = LexerState.INIT;
                                return GspTokenId.COMMENT_HTML_STYLE_END;
                            }
                        default:
                            // Anything else means we are still in comment
                            state = LexerState.LT_EM_DOUBLE_DASH;
                            break;
                    }
                    break;
                case LT_BS : // after </
                    switch (actChar) {
                        case 'g':
                            state = LexerState.LT_BS_G;
                            break; // after </g
                        default:
                            state = LexerState.INIT;
                    }
                    break;
                case LT_BS_G: // after </g
                    switch (actChar) {
                        case ':':
                            if (isContentRead(4)) {
                                state = LexerState.INIT;
                                return GspTokenId.HTML;
                            } else {
                                state = LexerState.GEND_TAG;
                                return GspTokenId.GTAG_CLOSING_START;
                            }
                        default:
                            state = LexerState.INIT;
                    }
                    break;
                case GEXPRESSION: // after ${
                    switch (actChar) {
                        case '}':
                            if (isContentRead(1)) {
                                state = LexerState.GEXPRESSION;
                                return GspTokenId.GSTRING_CONTENT;
                            } else {
                                state = LexerState.INIT;
                                return GspTokenId.GSTRING_END;
                            }
                        default:
                            // If anything else then '}' wait in the GEXPR
                            state = LexerState.GEXPRESSION;
                            break;
                    }
                    break;
                case JEXPR: // <%= .... %>
                    switch (actChar) {
                        case '%':
                            state = LexerState.JEXPR_PC;
                            break;
                        default:
                            break;
                    }
                    break;
                case JEXPR_PC:
                    switch (actChar) {
                        case '>':
                            if (isContentRead(2)) {
                                state = LexerState.JEXPR;
                                return GspTokenId.SCRIPTLET_OUTPUT_VALUE_CONTENT;
                            } else {
                                state = LexerState.INIT;
                                return GspTokenId.SCRIPTLET_OUTPUT_VALUE_END;
                            }
                        default:
                            state = LexerState.JEXPR;
                            break;
                    }
                    break;
                case JSCRIPT: // <% ... %>
                    switch (actChar) {
                        case '%':
                            state = LexerState.JSCRIPT_PC;
                            break;
                        default:
                            break;
                    }
                    break;
                case JSCRIPT_PC:
                    switch (actChar) {
                        case '>':
                            if (isContentRead(2)) {
                                state = LexerState.JSCRIPT;
                                return GspTokenId.SCRIPTLET_CONTENT;
                            } else {
                                state = LexerState.INIT;
                                return GspTokenId.SCRIPTLET_END;
                            }
                        default:
                            state = LexerState.JSCRIPT;
                            break;
                    }
                    break;
                case PAGE_DIRECTIVE: // after <%@ ...
                    switch (actChar) {
                        case 'p':
                            state = LexerState.PAGE_DIRECTIVE_P;
                            break;
                        case '%':
                            state = LexerState.PAGE_DIRECTIVE_PC;
                            break;
                        default:
                            state = LexerState.PAGE_DIRECTIVE;
                            break;
                    }
                    break;
                case PAGE_DIRECTIVE_P: // after <%@ p ...
                    switch (actChar) {
                        case 'a':
                            state = LexerState.PAGE_DIRECTIVE_PA;
                            break;
                        default:
                            state = LexerState.PAGE_DIRECTIVE;
                            break;
                    }
                    break;
                case PAGE_DIRECTIVE_PA: // after <%@ pa ...
                    switch (actChar) {
                        case 'g':
                            state = LexerState.PAGE_DIRECTIVE_PAG;
                            break;
                        default:
                            state = LexerState.PAGE_DIRECTIVE;
                            break;
                    }
                    break;
                case PAGE_DIRECTIVE_PAG: // after <%@ pag ...
                    switch (actChar) {
                        case 'e':
                            state = LexerState.PAGE_DIRECTIVE_WITH_NAME;
                            return GspTokenId.PAGE_DIRECTIVE_NAME;
                        default:
                            state = LexerState.PAGE_DIRECTIVE;
                            break;
                    }
                    break;
                case PAGE_DIRECTIVE_WITH_NAME: // <%@ page ...
                    switch (actChar) {
                        case '=':
                            state = LexerState.PAGE_DIRECTIVE_WITH_NAME_ATTR;
                            return GspTokenId.PAGE_ATTRIBUTE_NAME;
                        case '%':
                            state = LexerState.PAGE_DIRECTIVE_PC;
                            break;
                        case '>':
                            state = LexerState.INIT;
                            return GspTokenId.PAGE_DIRECTIVE_END;
                        default:
                            state = LexerState.PAGE_DIRECTIVE_WITH_NAME;
                            break;
                    }
                    break;
                case PAGE_DIRECTIVE_WITH_NAME_ATTR: // <%@page attr=...
                    switch (actChar) {
                        case ' ':
                            state = LexerState.PAGE_DIRECTIVE_WITH_NAME;
                            return GspTokenId.PAGE_ATTRIBUTE_VALUE;
                        case '%':
                            state = LexerState.PAGE_DIRECTIVE_PC;
                            break;
                    }
                    break;
                case PAGE_DIRECTIVE_PC:
                    switch (actChar) {
                        case '>':
                            if (isContentRead(2)) {
                                state = LexerState.PAGE_DIRECTIVE_WITH_NAME_ATTR;
                                return GspTokenId.PAGE_ATTRIBUTE_VALUE;
                            } else {
                                state = LexerState.INIT;
                                return GspTokenId.PAGE_DIRECTIVE_END;
                            }
                        default:
                            state = LexerState.PAGE_DIRECTIVE;
                            break;
                    }
                    break;
                case GSTART_TAG: // after <g:
                    switch (actChar) {
                        case ' ':
                            state = LexerState.GSTART_TAG_WITH_NAME;
                            input.backup(1);
                            return GspTokenId.GTAG_OPENING_NAME;
                        case '>':
                            if (isContentRead(1)) {
                                state = LexerState.GSTART_TAG;
                                return GspTokenId.GTAG_OPENING_NAME;
                            } else {
                                state = LexerState.INIT;
                                return GspTokenId.GTAG_OPENING_END;
                            }
                        case '/':
                            state = LexerState.GINDEPENDENT_TAG;
                            break;
                        default:
                            state = LexerState.GSTART_TAG;
                            break;
                    }
                    break;
                case GSTART_TAG_WITH_NAME:  // after for example <g:if
                    switch (actChar) {
                        case '>':
                            state = LexerState.INIT;
                            return GspTokenId.GTAG_OPENING_END;
                        case '/':
                            state = LexerState.GINDEPENDENT_TAG_WITH_NAME;
                            break;
                        case '=':
                            state = LexerState.GSTART_TAG_WITH_NAME_ATTR;
                            return GspTokenId.GTAG_ATTRIBUTE_NAME;
                        default:
                            state = LexerState.GSTART_TAG_WITH_NAME;
                            break;
                    }
                    break;
                case GSTART_TAG_WITH_NAME_ATTR:
                    switch (actChar) {
                        case '\"':
                            state = LexerState.GSTART_TAG_WITH_NAME_ATTR_VALUE;
                            break;
                        case '>':
                            if (isContentRead(1)) {
                                state = LexerState.GSTART_TAG_WITH_NAME;
                                return GspTokenId.GTAG_ATTRIBUTE_VALUE;
                            } else {
                                state = LexerState.INIT;
                                return GspTokenId.GTAG_OPENING_END;
                            }
                        case ' ':
                            state = LexerState.GSTART_TAG_WITH_NAME;
                            return GspTokenId.GTAG_ATTRIBUTE_VALUE;
                        case '/':
                            state = LexerState.GINDEPENDENT_TAG_WITH_NAME;
                            return GspTokenId.GTAG_ATTRIBUTE_VALUE;
                        default:
                            state = LexerState.GSTART_TAG_WITH_NAME_ATTR;
                            break;
                    }
                    break;
                case GSTART_TAG_WITH_NAME_ATTR_VALUE:
                    switch (actChar) {
                        case '\"':
                            state = LexerState.GSTART_TAG_WITH_NAME;
                            return GspTokenId.GTAG_ATTRIBUTE_VALUE;
                        case '\\':
                            state = LexerState.GSTART_TAG_BACKSLASH;
                            break;
                        case '$':
                            state = LexerState.GSTART_TAG_EXPR;
                            break;
                        case '/':
                            state = LexerState.GINDEPENDENT_TAG_WITH_NAME;
                            return GspTokenId.GTAG_ATTRIBUTE_VALUE;
                    }
                    break;
                case GEND_TAG: // after </g:
                    switch (actChar) {
                        case ' ':
                            state = LexerState.GEND_TAG_WITH_NAME;
                            return GspTokenId.GTAG_CLOSING_NAME;
                        case '>':
                            if (isContentRead(1)) {
                                state = LexerState.GEND_TAG;
                                return GspTokenId.GTAG_CLOSING_NAME;
                            } else {
                                state = LexerState.INIT;
                                return GspTokenId.GTAG_CLOSING_END;
                            }
                        default:
                            state = LexerState.GEND_TAG;
                            break;
                    }
                    break;
                case GEND_TAG_WITH_NAME:
                    switch (actChar) {
                        case '>':
                            state = LexerState.INIT;
                            return GspTokenId.GTAG_CLOSING_END;
                        default:
                            state = LexerState.GEND_TAG_WITH_NAME;
                            break;
                    }
                    break;
                case GINDEPENDENT_TAG: // after <g: ... /
                    switch (actChar) {
                        case '>':
                            if (isContentRead(2)) {
                                state = LexerState.GSTART_TAG;
                                return GspTokenId.GTAG_OPENING_NAME;
                            } else {
                                state = LexerState.INIT;
                                return GspTokenId.GTAG_INDEPENDENT_END;
                            }
                        default:
                            state = LexerState.GSTART_TAG;
                            break;
                    }
                    break;
                case GINDEPENDENT_TAG_WITH_NAME: // after <g:if ... /
                    switch (actChar) {
                        case '>':
                            state = LexerState.INIT;
                            return GspTokenId.GTAG_INDEPENDENT_END;
                        default:
                            state = LexerState.GSTART_TAG_WITH_NAME;
                            break;
                    }
                    break;
                case GSTART_TAG_BACKSLASH: // after <g: ... \
                    switch (actChar) {
                        case '$':
                            state = LexerState.GSTART_TAG_BACKSLASH_EXPR;
                            break;
                        default:
                            state = LexerState.GSTART_TAG_WITH_NAME_ATTR_VALUE;
                            break;
                    }
                    break;
                case GSTART_TAG_BACKSLASH_EXPR: // after <g: ...\$
                    switch (actChar) {
                        case '{':
                            if (isContentRead(3)) {
                                state = LexerState.GSTART_TAG_WITH_NAME_ATTR_VALUE;
                                return GspTokenId.GTAG_ATTRIBUTE_VALUE;
                            } else {
                                state = LexerState.GSTART_TAG_EXPR_PC;
                                return GspTokenId.GSTRING_START;
                            }
                        default:
                            break;
                    }
                    break;
                case GSTART_TAG_EXPR: // after <g: ... $
                    switch (actChar) {
                        case '{':
                            if (isContentRead(2)) {
                                state = LexerState.GSTART_TAG_WITH_NAME_ATTR_VALUE;
                                return GspTokenId.GTAG_ATTRIBUTE_VALUE;
                            } else {
                                state = LexerState.GSTART_TAG_EXPR_PC;
                                return GspTokenId.GSTRING_START;
                            }
                        default:
                            state = LexerState.GSTART_TAG_WITH_NAME_ATTR_VALUE;
                            break;
                    }
                    break;
                case GSTART_TAG_EXPR_PC: // after <g: ... ${ or <g: .../${
                    switch (actChar) {
                        case '}':
                            if (isContentRead(1)) {
                                state = LexerState.GSTART_TAG_EXPR_PC;
                                return GspTokenId.GSTRING_CONTENT;
                            } else {
                                state = LexerState.GSTART_TAG_WITH_NAME_ATTR_VALUE;
                                return GspTokenId.GSTRING_END;
                            }
                        // --> issue 220938
                        case '>':
                            state = LexerState.INIT;
                            return GspTokenId.GTAG_OPENING_START;
                        default:
                            state = LexerState.GSTART_TAG_EXPR_PC;
                            break;
                    }
            }
        }

        // It is possible that we end the lexing with no other chars in the text
        // but not in the final state. Typically this can happen when the source
        // code is in comment that has no end token (-->, --}% or --%>). In that
        // case we are still waiting for 4 more chars but they will never come.
        // And without this we would get IAE thrown by the Lexer infrastructure.
        switch (state) {
            case JSCRIPT: return GspTokenId.GSTRING_CONTENT;
            case GEXPRESSION: return GspTokenId.GSTRING_CONTENT;
            case PAGE_DIRECTIVE: return GspTokenId.PAGE_DIRECTIVE_START;
            case LT_EM_DOUBLE_DASH: return GspTokenId.COMMENT_HTML_STYLE_START;
            case LT_PC_DOUBLE_DASH: return GspTokenId.COMMENT_JSP_STYLE_START;
            case PC_CB_DOUBLE_DASH: return GspTokenId.COMMENT_GSP_STYLE_START;
            default: return GspTokenId.ERROR;
        }
    }

    /**
     * In a lot of cases we want to do something like this. Read the input chars
     * and if there is a certain number of chars, than change the state and return
     * some TokenId. BUT if there is more than the expected number of chars, it
     * means that something else is still in the buffer and in that case we want
     * to return those chars as some other TokenId, backup few last chars we were
     * waiting for and read them again.
     *
     * One example might be "...sometext />". This is most probably independent
     * GTag and when we read "/>" we want to return GspTokenId.GTAG_INDEPENDENT_END.
     * But we might have "sometext" in the buffer as well and in that case we need
     * to backup last two chars ("/>"), return "sometext" as an GspTokenId.GTAG_CONTENT
     * and read the last two chars again so we will get GspTokenId.GTAG_INDEPENDENT_END
     * as well.
     *
     * @param endTokenLength length of the end token (for example 2 for "/>")
     * @return true if this is a content read, else otherwise (typically end token read)
     */
    private boolean isContentRead(int endTokenLength) {
        if (input.readLength() > endTokenLength) {
            input.backup(endTokenLength);
            return true;
        }
        return false;
    }

    @Override
    public Object state() {
        return state;
    }

    @Override
    public void release() {
    }
}
