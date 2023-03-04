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

package org.netbeans.modules.php.latte.lexer;

import java.util.ArrayDeque;
import java.util.Objects;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.modules.web.common.api.ByteStack;

@org.netbeans.api.annotations.common.SuppressWarnings({"SF_SWITCH_FALLTHROUGH", "URF_UNREAD_FIELD", "DLS_DEAD_LOCAL_STORE", "DM_DEFAULT_ENCODING"})
%%

%public
%class LatteTopColoringLexer
%type LatteTopTokenId
%function findNextToken
%unicode
%caseless
%char

%eofval{
        if (input.readLength() > 0) {
            // backup eof
            input.backup(1);
            //and return the text as HTML token
            return LatteTopTokenId.T_HTML;
        } else {
            return null;
        }
%eofval}

%{

    private ByteStack stack = new ByteStack();
    private LexerInput input;
    private Syntax syntax;
    private ArrayDeque<HtmlTag> tags;

    public LatteTopColoringLexer(LexerRestartInfo info) {
        this.input = info.input();
        if (info.state() != null) {
            //reset state
            setState((LexerState) info.state());
            this.syntax = ((LexerState) info.state()).syntax;
            this.tags = ((LexerState) info.state()).tags.clone();
        } else {
            zzState = zzLexicalState = YYINITIAL;
            this.syntax = Syntax.LATTE;
            this.tags = new ArrayDeque<>() ;
            stack.clear();
        }

    }

    private boolean curlyInBalance(String text) {
        int textLength = text.length();
        int openCurly = textLength - text.replace("{", "").length();
        int closeCurly = textLength - text.replace("}", "").length();
        return openCurly == closeCurly;
    }

    private enum Syntax {
        LATTE,
        DOUBLE,
        ASP,
        PYTHON,
        OFF;
    }

    private static final class HtmlTag {
        private boolean isSyntax;

        public void setIsSyntax(boolean isSyntax) {
            this.isSyntax = isSyntax;
        }

        public boolean isSyntax() {
            return isSyntax;
        }

    }

    public static final class LexerState  {
        final ByteStack stack;
        /** the current state of the DFA */
        final int zzState;
        /** the current lexical state */
        final int zzLexicalState;
        private final Syntax syntax;
        private final ArrayDeque<HtmlTag> tags;

        LexerState(ByteStack stack, int zzState, int zzLexicalState, Syntax syntax, ArrayDeque<HtmlTag> tags) {
            this.stack = stack;
            this.zzState = zzState;
            this.zzLexicalState = zzLexicalState;
            this.syntax = syntax;
            this.tags = tags;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + Objects.hashCode(this.stack);
            hash = 89 * hash + this.zzState;
            hash = 89 * hash + this.zzLexicalState;
            hash = 89 * hash + (this.syntax != null ? this.syntax.hashCode() : 0);
            hash = 89 * hash + Objects.hashCode(this.tags);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final LexerState other = (LexerState) obj;
            if (!Objects.equals(this.stack, other.stack)) {
                return false;
            }
            if (this.zzState != other.zzState) {
                return false;
            }
            if (this.zzLexicalState != other.zzLexicalState) {
                return false;
            }
            if (this.syntax != other.syntax) {
                return false;
            }
            if (!Objects.equals(this.tags, other.tags)) {
                return false;
            }
            return true;
        }
    }

    public LexerState getState() {
        return new LexerState(stack.copyOf(), zzState, zzLexicalState, syntax, tags.clone());
    }

    public void setState(LexerState state) {
        this.stack.copyFrom(state.stack);
        this.zzState = state.zzState;
        this.zzLexicalState = state.zzLexicalState;
    }

    protected int getZZLexicalState() {
        return zzLexicalState;
    }

    protected void popState() {
        yybegin(stack.pop());
    }

    protected void pushState(final int state) {
        stack.push(getZZLexicalState());
        yybegin(state);
    }


 // End user code

%}

WHITESPACE=[ \t\r\n]+
COMMENT_START="*"
COMMENT_CONTENT=([^\*] | \*[^\}\%])*
COMMENT_END="*"
LATTE_COMMENT_START={SYNTAX_LATTE_START}{COMMENT_START}
LATTE_COMMENT_END={COMMENT_END}{SYNTAX_LATTE_END}
DOUBLE_COMMENT_START={SYNTAX_DOUBLE_START}{COMMENT_START}
DOUBLE_COMMENT_END={COMMENT_END}{SYNTAX_DOUBLE_END}
ASP_COMMENT_START={SYNTAX_ASP_START}{COMMENT_START}
ASP_COMMENT_END={COMMENT_END}{SYNTAX_ASP_END}
PYTHON_COMMENT_START={SYNTAX_PYTHON_START}{COMMENT_START}
PYTHON_COMMENT_END={COMMENT_END}{SYNTAX_PYTHON_END}
MACRO_SYNTAX_START="syntax"[ \t]+
MACRO_SYNTAX_END="/syntax"
SYNTAX_LATTE_START="{"
SYNTAX_LATTE_END="}"
SYNTAX_DOUBLE_START="{{"
SYNTAX_DOUBLE_END="}}"
SYNTAX_ASP_START="<%"
SYNTAX_ASP_END="%>"
SYNTAX_PYTHON_START="{%"
SYNTAX_PYTHON_END="%}"

%state ST_COMMENT
%state ST_POSSIBLE_LATTE
%state ST_LATTE
%state ST_DOUBLE
%state ST_ASP
%state ST_PYTHON
%state ST_PYTHON_DOUBLE
%state ST_SYNTAX_CHANGE
%state ST_IN_HTML_TAG
%state ST_IN_SYNTAX_ATTR
%state ST_N_ATTR_DOUBLE
%state ST_N_ATTR_SINGLE
%state ST_HIGHLIGHTING_ERROR

%%
<YYINITIAL, ST_SYNTAX_CHANGE, ST_IN_HTML_TAG, ST_COMMENT, ST_LATTE, ST_DOUBLE, ST_ASP, ST_PYTHON, ST_PYTHON_DOUBLE, ST_N_ATTR_DOUBLE, ST_N_ATTR_SINGLE, ST_IN_SYNTAX_ATTR>{WHITESPACE}+ {
}

<ST_IN_HTML_TAG> {
    "<""/"[a-zA-Z0-9:]+">" {
        HtmlTag tag = tags.pop();
        if (tag.isSyntax()) {
            syntax = Syntax.LATTE;
        }
        popState();
    }
}

<YYINITIAL, ST_IN_HTML_TAG> {
    {PYTHON_COMMENT_START} {
        if (yylength() > 3) {
            yypushback(3);
            return LatteTopTokenId.T_HTML;
        }
        if (syntax == Syntax.PYTHON) {
            pushState(ST_COMMENT);
            return LatteTopTokenId.T_LATTE_COMMENT_DELIMITER;
        }
    }
    {SYNTAX_PYTHON_START}[^ \t\r\n] {
        if (yylength() > 3) {
            yypushback(3);
            return LatteTopTokenId.T_HTML;
        }
        yypushback(1);
        if (syntax == Syntax.PYTHON) {
            pushState(ST_PYTHON);
            return LatteTopTokenId.T_LATTE_OPEN_DELIMITER;
        }
    }
    {DOUBLE_COMMENT_START} {
        if (yylength() > 3) {
            yypushback(3);
            return LatteTopTokenId.T_HTML;
        }
        if (syntax == Syntax.DOUBLE || syntax == Syntax.PYTHON) {
            pushState(ST_COMMENT);
            return LatteTopTokenId.T_LATTE_COMMENT_DELIMITER;
        }
    }
    {SYNTAX_DOUBLE_START}[^ \t\r\n{] {
        if (yylength() > 3) {
            yypushback(3);
            return LatteTopTokenId.T_HTML;
        }
        yypushback(1);
        if (syntax == Syntax.DOUBLE) {
            pushState(ST_DOUBLE);
            return LatteTopTokenId.T_LATTE_OPEN_DELIMITER;
        }
        if (syntax == Syntax.PYTHON) {
            pushState(ST_PYTHON_DOUBLE);
            return LatteTopTokenId.T_LATTE_OPEN_DELIMITER;
        }
        if (syntax == Syntax.LATTE) {
            yypushback(1);
        }
    }
    {ASP_COMMENT_START} {
        if (yylength() > 3) {
            yypushback(3);
            return LatteTopTokenId.T_HTML;
        }
        if (syntax == Syntax.ASP) {
            pushState(ST_COMMENT);
            return LatteTopTokenId.T_LATTE_COMMENT_DELIMITER;
        }
    }
    {SYNTAX_ASP_START}[^ \t\r\n] {
        if (yylength() > 3) {
            yypushback(3);
            return LatteTopTokenId.T_HTML;
        }
        yypushback(1);
        if (syntax == Syntax.ASP) {
            pushState(ST_ASP);
            return LatteTopTokenId.T_LATTE_OPEN_DELIMITER;
        }
    }
    {LATTE_COMMENT_START} {
        if (yylength() > 2) {
            yypushback(2);
            return LatteTopTokenId.T_HTML;
        }
        if (syntax == Syntax.LATTE) {
            pushState(ST_COMMENT);
            return LatteTopTokenId.T_LATTE_COMMENT_DELIMITER;
        }
    }
    {SYNTAX_LATTE_START} {
        yypushback(1);
        pushState(ST_POSSIBLE_LATTE);
    }
    "<""!"?[a-zA-Z0-9:]+ {
        tags.push(new HtmlTag());
        pushState(ST_IN_HTML_TAG);
    }
    . {}
}

<ST_POSSIBLE_LATTE> {
    {SYNTAX_LATTE_START}[a-zA-Z0-9_/\!=\?\$] {
        if (yylength() > 2) {
            yypushback(2);
            return LatteTopTokenId.T_HTML;
        }
        yypushback(1);
        if (syntax == Syntax.LATTE) {
            pushState(ST_LATTE);
            return LatteTopTokenId.T_LATTE_OPEN_DELIMITER;
        } else {
            popState();
            return LatteTopTokenId.T_HTML;
        }
    }
    {SYNTAX_LATTE_START}{SYNTAX_LATTE_END} {
        yypushback(1);
        popState();
        return LatteTopTokenId.T_HTML;
    }
    {SYNTAX_LATTE_START}{WHITESPACE}* {
        popState();
    }
    {WHITESPACE}+ | . {
        yypushback(yylength());
        popState();
    }
}

<ST_IN_HTML_TAG> {
    "n:"[a-zA-Z0-9\-]+=\" {
        String text = yytext().toLowerCase().trim();
        String attributeName = text.substring(0, text.length() - 2);
        if (attributeName.endsWith("n:syntax")) { //NOI18N
            tags.peek().setIsSyntax(true);
            pushState(ST_IN_SYNTAX_ATTR);
        } else {
            pushState(ST_N_ATTR_DOUBLE);
        }
        return LatteTopTokenId.T_HTML;
    }
    "n:"[a-zA-Z0-9\-]+=' {
        String text = yytext().toLowerCase().trim();
        String attributeName = text.substring(0, text.length() - 2);
        if (attributeName.endsWith("n:syntax")) { //NOI18N
            tags.peek().setIsSyntax(true);
            pushState(ST_IN_SYNTAX_ATTR);
        } else {
            pushState(ST_N_ATTR_SINGLE);
        }
        return LatteTopTokenId.T_HTML;
    }
    "/>" {
        if (!tags.isEmpty()) {
            HtmlTag tag = tags.pop();
            if (tag.isSyntax()) {
                syntax = Syntax.LATTE;
            }
        }
        popState();
    }
    . {}
}

<ST_IN_SYNTAX_ATTR> {
    "latte" {
        popState();
        syntax = Syntax.LATTE;
        return LatteTopTokenId.T_LATTE;
    }
    "double" {
        popState();
        syntax = Syntax.DOUBLE;
        return LatteTopTokenId.T_LATTE;
    }
    "asp" {
        popState();
        syntax = Syntax.ASP;
        return LatteTopTokenId.T_LATTE;
    }
    "python" {
        popState();
        syntax = Syntax.PYTHON;
        return LatteTopTokenId.T_LATTE;
    }
    "off" {
        popState();
        syntax = Syntax.OFF;
        return LatteTopTokenId.T_LATTE;
    }
    . {
        popState();
    }
}

<ST_N_ATTR_DOUBLE> {
    ([^\"] | \\\")+ ~\" {
        yypushback(1);
        popState();
        return LatteTopTokenId.T_LATTE;
    }
    . {
        popState();
    }
}

<ST_N_ATTR_SINGLE> {
    ([^'] | \\')+ ~' {
        yypushback(1);
        popState();
        return LatteTopTokenId.T_LATTE;
    }
    . {
        popState();
    }
}

<ST_LATTE, ST_DOUBLE, ST_ASP, ST_PYTHON, ST_PYTHON_DOUBLE> {
    {MACRO_SYNTAX_START} "latte" "}"? {
        if (yytext().endsWith("}")) {
            yypushback(1);
        }
        syntax = Syntax.LATTE;
        return LatteTopTokenId.T_LATTE;
    }
    {MACRO_SYNTAX_START} "double" "}"? {
        if (yytext().endsWith("}")) {
            yypushback(1);
        }
        syntax = Syntax.DOUBLE;
        return LatteTopTokenId.T_LATTE;
    }
    {MACRO_SYNTAX_START} "asp" "}"? {
        if (yytext().endsWith("}")) {
            yypushback(1);
        }
        syntax = Syntax.ASP;
        return LatteTopTokenId.T_LATTE;
    }
    {MACRO_SYNTAX_START} "python" "}"? {
        if (yytext().endsWith("}")) {
            yypushback(1);
        }
        syntax = Syntax.PYTHON;
        return LatteTopTokenId.T_LATTE;
    }
    {MACRO_SYNTAX_START} "off" "}"? {
        if (yytext().endsWith("}")) {
            yypushback(1);
        }
        syntax = Syntax.OFF;
        return LatteTopTokenId.T_LATTE;
    }
    {MACRO_SYNTAX_END} {
        syntax = Syntax.LATTE;
        return LatteTopTokenId.T_LATTE;
    }
}

<ST_LATTE> {
    {SYNTAX_LATTE_END} {
        popState();
        return LatteTopTokenId.T_LATTE_CLOSE_DELIMITER;
    }
    [^"}""\r""\n""\r\n"]+ "}" {
        if (curlyInBalance(yytext().substring(0, yylength() - 1))) {
            yypushback(1);
            return LatteTopTokenId.T_LATTE;
        }
    }
    [^"}""\r""\n""\r\n"]+ {
        if (curlyInBalance(yytext())) {
            return LatteTopTokenId.T_LATTE;
        }
    }
}

<ST_DOUBLE> {
    {SYNTAX_DOUBLE_END} {
        popState();
        return LatteTopTokenId.T_LATTE_CLOSE_DELIMITER;
    }
    ([^"}"] | }[^"}"])+ {
        return LatteTopTokenId.T_LATTE;
    }
}

<ST_ASP> {
    {SYNTAX_ASP_END} {
        popState();
        return LatteTopTokenId.T_LATTE_CLOSE_DELIMITER;
    }
    ([^"%"] | %[^">"])+ {
        return LatteTopTokenId.T_LATTE;
    }
}

<ST_PYTHON> {
    {SYNTAX_PYTHON_END} {
        popState();
        return LatteTopTokenId.T_LATTE_CLOSE_DELIMITER;
    }
    ([^"%"] | %[^"}"])+ {
        return LatteTopTokenId.T_LATTE;
    }
}

<ST_PYTHON_DOUBLE> {
    {SYNTAX_DOUBLE_END} {
        popState();
        return LatteTopTokenId.T_LATTE_CLOSE_DELIMITER;
    }
    ([^"}"] | }[^"}"])+ {
        return LatteTopTokenId.T_LATTE;
    }
}

<ST_COMMENT> {
    {LATTE_COMMENT_END} {
        if (syntax == Syntax.LATTE) {
            popState();
            return LatteTopTokenId.T_LATTE_COMMENT_DELIMITER;
        }
    }
    {DOUBLE_COMMENT_END} {
        if (syntax == Syntax.DOUBLE || syntax == Syntax.PYTHON) {
            popState();
            return LatteTopTokenId.T_LATTE_COMMENT_DELIMITER;
        }
    }
    {ASP_COMMENT_END} {
        if (syntax == Syntax.ASP) {
            popState();
            return LatteTopTokenId.T_LATTE_COMMENT_DELIMITER;
        }
    }
    {PYTHON_COMMENT_END} {
        if (syntax == Syntax.PYTHON) {
            popState();
            return LatteTopTokenId.T_LATTE_COMMENT_DELIMITER;
        }
    }
    {COMMENT_CONTENT} {
        return LatteTopTokenId.T_LATTE_COMMENT;
    }
    . {}
}

<ST_HIGHLIGHTING_ERROR> {
    . {
        return LatteTopTokenId.T_LATTE_ERROR;
    }
}

/* ============================================
   This rule must be the last in the section!!
   it should contain all the states.
   ============================================ */
<YYINITIAL, ST_COMMENT, ST_POSSIBLE_LATTE, ST_LATTE, ST_DOUBLE, ST_ASP, ST_PYTHON, ST_N_ATTR_DOUBLE, ST_N_ATTR_SINGLE, ST_IN_HTML_TAG, ST_IN_SYNTAX_ATTR> {
    . {
        yypushback(yylength());
        pushState(ST_HIGHLIGHTING_ERROR);
    }
}
