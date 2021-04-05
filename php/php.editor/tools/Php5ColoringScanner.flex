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

package org.netbeans.modules.php.editor.lexer;

import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.modules.web.common.api.ByteStack;

@org.netbeans.api.annotations.common.SuppressWarnings({"SF_SWITCH_FALLTHROUGH", "URF_UNREAD_FIELD", "DLS_DEAD_LOCAL_STORE", "DM_DEFAULT_ENCODING", "EI_EXPOSE_REP"})
%%

%public
%class PHP5ColoringLexer
%type PHPTokenId
%function nextToken
%unicode
%caseless
%char

%state ST_PHP_IN_SCRIPTING
%state ST_PHP_DOUBLE_QUOTES
%state ST_PHP_BACKQUOTE
%state ST_PHP_QUOTES_AFTER_VARIABLE
%state ST_PHP_LOOKING_FOR_STATIC_PROPERTY
%state ST_PHP_HEREDOC
%state ST_PHP_START_HEREDOC
%state ST_PHP_END_HEREDOC
%state ST_PHP_NOWDOC
%state ST_PHP_START_NOWDOC
%state ST_PHP_END_NOWDOC
%state ST_PHP_LOOKING_FOR_PROPERTY
%state ST_PHP_LOOKING_FOR_FUNCTION_NAME
%state ST_PHP_LOOKING_FOR_CONSTANT_NAME
%state ST_PHP_VAR_OFFSET
%state ST_PHP_COMMENT
%state ST_PHP_DOC_COMMENT
%state ST_PHP_LINE_COMMENT
%state ST_PHP_HIGHLIGHTING_ERROR
%state ST_HALTED_COMPILER

%eofval{
       if(input.readLength() > 0) {
            // backup eof
            input.backup(1);
            //and return the text as error token
            return PHPTokenId.UNKNOWN_TOKEN;
        } else {
            return null;
        }
%eofval}

%{

    private final ByteStack stack = new ByteStack();
    private String heredoc = null;
    private int hereocLength = 0;
    private int parenBalanceInConst = 0; // for context sensitive lexer
    private int bracketBalanceInConst = 0; // for context sensitive lexer
    private boolean aspTagsAllowed;
    private boolean shortTagsAllowed;
    private boolean isInConst;
    private LexerInput input;

    public PHP5ColoringLexer(LexerRestartInfo info, boolean shortTagsAllowed, boolean aspTagsAllowed, boolean inPHP) {
        this.input = info.input();
        this.aspTagsAllowed = aspTagsAllowed;
        this.shortTagsAllowed = shortTagsAllowed;

        if (info.state() != null) {
            //reset state
            setState((LexerState) info.state());
        } else {
            //initial state
            stack.push(YYINITIAL);
            if (inPHP) {
                stack.push(ST_PHP_IN_SCRIPTING);
                zzState = ST_PHP_IN_SCRIPTING;
                zzLexicalState = ST_PHP_IN_SCRIPTING;
            } else {
                zzState = YYINITIAL;
                zzLexicalState = YYINITIAL;
            }
        }

    }

    public static final class LexerState  {
        final ByteStack stack;
        /* the current state of the DFA */
        final int zzState;
        /* the current lexical state */
        final int zzLexicalState;
        /* remember the heredoc */
        final String heredoc;
        /* and the lenght of */
        final int hereocLength;

        LexerState(ByteStack stack, int zzState, int zzLexicalState, String heredoc, int hereocLength) {
            this.stack = stack;
            this.zzState = zzState;
            this.zzLexicalState = zzLexicalState;
            this.heredoc = heredoc;
            this.hereocLength = hereocLength;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }

            LexerState state = (LexerState) obj;
            return (this.stack.equals(state.stack)
                && (this.zzState == state.zzState)
                && (this.zzLexicalState == state.zzLexicalState)
                && (this.hereocLength == state.hereocLength)
                && ((this.heredoc == null && state.heredoc == null) || (this.heredoc != null && state.heredoc != null && this.heredoc.equals(state.heredoc))));
        }

        @Override
        public int hashCode() {
            int hash = 11;
            hash = 31 * hash + this.zzState;
            hash = 31 * hash + this.zzLexicalState;
            if (stack != null) {
                hash = 31 * hash + this.stack.hashCode();
            }
            hash = 31 * hash + this.hereocLength;
            if (heredoc != null) {
                hash = 31 * hash + this.heredoc.hashCode();
            }
            return hash;
        }
    }

    public LexerState getState() {
        return new LexerState(stack.copyOf(), zzState, zzLexicalState, heredoc, hereocLength);
    }

    public void setState(LexerState state) {
        this.stack.copyFrom(state.stack);
        this.zzState = state.zzState;
        this.zzLexicalState = state.zzLexicalState;
        this.heredoc = state.heredoc;
        this.hereocLength = state.hereocLength;
    }

    protected boolean isHeredocState(int state) {
        return state == ST_PHP_HEREDOC || state == ST_PHP_START_HEREDOC || state == ST_PHP_END_HEREDOC || state == ST_PHP_NOWDOC;
    }

    public int[] getParamenters() {
        return new int[]{zzMarkedPos, zzPushbackPos, zzCurrentPos, zzStartRead, zzEndRead, yyline, zzLexicalState};
    }

    protected int getZZLexicalState() {
        return zzLexicalState;
    }

    protected int getZZMarkedPos() {
        return zzMarkedPos;
    }

    protected int getZZEndRead() {
        return zzEndRead;
    }

    public char[] getZZBuffer() {
        return zzBuffer;
    }

    protected int getZZStartRead() {
        return this.zzStartRead;
    }

    protected int getZZPushBackPosition() {
        return this.zzPushbackPos;
    }

    protected void pushBack(int i) {
        yypushback(i);
    }

    protected void popState() {
        yybegin(stack.pop());
    }

    protected void pushState(final int state) {
        stack.push(getZZLexicalState());
        yybegin(state);
    }

    private boolean isLabelChar(char c) {
        return c == '_'
                || (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c >= 0x7f && c <= 0xff);
    }

    private boolean isEndNowdoc() {
        String yytext = yytext().trim();
        int lastIndexOfNewline = yytext.lastIndexOf('\n');
        if (lastIndexOfNewline == -1) {
            lastIndexOfNewline = yytext.lastIndexOf('\r');
        }
        if (lastIndexOfNewline != -1) {
            yytext = yytext.substring(lastIndexOfNewline);
        }
        return isEndHereOrNowdoc(yytext);
    }

    private boolean isEndHeredoc() {
        return isEndHereOrNowdoc(yytext());
    }

    private boolean isEndHereOrNowdoc(String text) {
        // check whether ID exists
        String trimedText = text.trim();
        boolean isEnd = false;
        if (trimedText.startsWith(heredoc)) {
            if (trimedText.length() == heredoc.length()) {
                isEnd = true;
            } else if (trimedText.length() > heredoc.length()
                    && !isLabelChar(trimedText.charAt(heredoc.length()))) {
                // e.g.
                // $test = <<< END
                // ENDING
                // END
                isEnd = true;
            }
        }
        return isEnd;
    }

 // End user code

%}

LNUM=[0-9]+(_[0-9]+)*
DNUM=({LNUM}?[\.]{LNUM})|({LNUM}[\.]{LNUM}?)
EXPONENT_DNUM=(({LNUM}|{DNUM})[eE][+-]?{LNUM})
HNUM="0x"[0-9a-fA-F]+(_[0-9a-fA-F]+)*
BNUM="0b"[01]+(_[01]+)*
//LABEL=[a-zA-Z_\x7f-\xff][a-zA-Z0-9_\x7f-\xff]*
LABEL=([[:letter:]_]|[\u007f-\u00ff])([[:letter:][:digit:]_]|[\u007f-\u00ff])*
WHITESPACE=[ \n\r\t]+
TABS_AND_SPACES=[ \t]*
TOKENS=[:,.\[\]()$?//]
CLOSE_EXPRESSION=[;]
ANY_CHAR=[^]
NEWLINE=("\r"|"\n"|"\r\n")
DOUBLE_QUOTES_LITERAL_DOLLAR=("$"+([^a-zA-Z_\x7f-\xff$\"\\{]|("\\"{ANY_CHAR})))
BACKQUOTE_LITERAL_DOLLAR=("$"+([^a-zA-Z_\x7f-\xff$`\\{]|("\\"{ANY_CHAR})))

HEREDOC_LITERAL_DOLLAR=("$"+([^a-zA-Z_\x7f-\xff$\n\r\\{]|("\\"[^\n\r])))
HEREDOC_CURLY_OR_ESCAPE_OR_DOLLAR=(("{"+[^$\n\r\\{])|("{"*"\\"[^\n\r])|{HEREDOC_LITERAL_DOLLAR})
HEREDOC_NON_LABEL=([^a-zA-Z_\x7f-\xff$\n\r\\{]|{HEREDOC_CURLY_OR_ESCAPE_OR_DOLLAR})
HEREDOC_LABEL_NO_NEWLINE=({LABEL}([^a-zA-Z0-9_\x7f-\xff;$\n\r\\{]|(";"[^$\n\r\\{])|(";"?{HEREDOC_CURLY_OR_ESCAPE_OR_DOLLAR})))

DOUBLE_QUOTES_CHARS=("{"*([^$\"\\{]|("\\"{ANY_CHAR}))|{DOUBLE_QUOTES_LITERAL_DOLLAR})
BACKQUOTE_CHARS=("{"*([^$`\\{]|("\\"{ANY_CHAR}))|{BACKQUOTE_LITERAL_DOLLAR})

HEREDOC_CHARS=([^$\\{]|("\\"{ANY_CHAR}))({HEREDOC_LABEL_NO_NEWLINE} | {HEREDOC_NON_LABEL} | {LABEL})*
NOWDOC_CHARS=({NEWLINE}*(([^a-zA-Z_\x7f-\xff\n\r][^\n\r]*)|({LABEL}[^a-zA-Z0-9_\x7f-\xff;\n\r][^\n\r]*)|({LABEL}[;][^\n\r]+)))
PHP_OPERATOR="=>"|"++"|"--"|"==="|"!=="|"=="|"!="|"<>"|"<="|">="|"+="|"-="|"*="|"/="|".="|"%="|"<<="|">>="|"&="|"|="|"^="|"||"|"&&"|"<<"|">>"|"**"|"**="|"..."|"="|"+"|"-"|"/"|"*"|"%"|"<"|">"|"!"|"@"|"^"|"&"|"|"|"~"|"<=>"|"??"|"??="
PHP_TEXTUAL_OPERATOR="OR"|"AND"|"XOR"
// XXX how to define case sensitive patterns?
PHP_TYPE_INT=[i][n][t]
PHP_TYPE_FLOAT=[f][l][o][a][t]
PHP_TYPE_STRING=[s][t][r][i][n][g]
PHP_TYPE_BOOL=[b][o][o][l]
// PHP7.1: These may be used as type names in PHP7.0 or older
PHP_TYPE_VOID=[v][o][i][d]
PHP_ITERABLE=[i][t][e][r][a][b][l][e]
// PHP7.2
PHP_TYPE_OBJECT=[o][b][j][e][c][t]





%%

<YYINITIAL>(([^<]|"<"[^?%(script)<])+)|"<script"|"<" {
    return PHPTokenId.T_INLINE_HTML;
}

<YYINITIAL>"<script"{WHITESPACE}+"language"{WHITESPACE}*"="{WHITESPACE}*("php"|"\"php\""|"\'php\'"){WHITESPACE}*">" {
    pushState(ST_PHP_IN_SCRIPTING);
    return PHPTokenId.T_INLINE_HTML;
}

<YYINITIAL>"<?" {
    if (shortTagsAllowed ) {
        //yybegin(ST_PHP_IN_SCRIPTING);
        pushState(ST_PHP_IN_SCRIPTING);
        return PHPTokenId.PHP_OPENTAG;
        //return createSymbol(ASTSymbol.T_OPEN_TAG);
    } else {
        //return createSymbol(ASTSymbol.T_INLINE_HTML);
        return PHPTokenId.T_INLINE_HTML;
    }
}

<YYINITIAL>"<%="|"<?=" {
    String text = yytext();
    if ((text.charAt(1)=='%' && aspTagsAllowed)
        || (text.charAt(1)=='?')) {
        //yybegin(ST_PHP_IN_SCRIPTING);
        pushState(ST_PHP_IN_SCRIPTING);
        return PHPTokenId.T_OPEN_TAG_WITH_ECHO;
        //return createSymbol(ASTSymbol.T_OPEN_TAG);
    } else {
        //return createSymbol(ASTSymbol.T_INLINE_HTML);
        return PHPTokenId.T_INLINE_HTML;
    }
}

<YYINITIAL>"<%" {
    if (aspTagsAllowed) {
        //yybegin(ST_PHP_IN_SCRIPTING);
        pushState(ST_PHP_IN_SCRIPTING);
        return PHPTokenId.PHP_OPENTAG;
        //return createSymbol(ASTSymbol.T_OPEN_TAG);
    } else {
        //return createSymbol(ASTSymbol.T_INLINE_HTML);
        return PHPTokenId.T_INLINE_HTML;
    }
}

<YYINITIAL>"<?php" {
    pushState(ST_PHP_IN_SCRIPTING);
    //yybegin(ST_PHP_IN_SCRIPTING);
    return PHPTokenId.PHP_OPENTAG;
    //return createSymbol(ASTSymbol.T_OPEN_TAG);
}


/***********************************************************************************************
**************************************** P  H  P ***********************************************
***********************************************************************************************/

<ST_PHP_IN_SCRIPTING> "exit" {
    return PHPTokenId.PHP_EXIT;
}

<ST_PHP_IN_SCRIPTING>"die" {
    return PHPTokenId.PHP_DIE;
}

<ST_PHP_IN_SCRIPTING>"fn" {
    // PHP 7.4 Arrow Functions 2.0
    // https://wiki.php.net/rfc/arrow_functions_v2
    return PHPTokenId.PHP_FN;
}

<ST_PHP_IN_SCRIPTING>"function" {
    pushState(ST_PHP_LOOKING_FOR_FUNCTION_NAME);
    return PHPTokenId.PHP_FUNCTION;
}

<ST_PHP_LOOKING_FOR_FUNCTION_NAME>{WHITESPACE}+ {
    return PHPTokenId.WHITESPACE;
}

<ST_PHP_LOOKING_FOR_FUNCTION_NAME>"(" {
    popState();
    return PHPTokenId.PHP_TOKEN;
}

<ST_PHP_LOOKING_FOR_FUNCTION_NAME>{LABEL} {
    popState();
    return PHPTokenId.PHP_STRING;
}

<ST_PHP_LOOKING_FOR_FUNCTION_NAME>{ANY_CHAR} {
    yypushback(1);
    popState();
}

<ST_PHP_IN_SCRIPTING>"const" {
    isInConst = true;
    parenBalanceInConst = 0;
    bracketBalanceInConst = 0;
    pushState(ST_PHP_LOOKING_FOR_CONSTANT_NAME);
    return PHPTokenId.PHP_CONST;
}

<ST_PHP_LOOKING_FOR_CONSTANT_NAME>{WHITESPACE}+ {
    return PHPTokenId.WHITESPACE;
}

<ST_PHP_LOOKING_FOR_CONSTANT_NAME>{LABEL}{WHITESPACE}*"=" {
    // const keyword is also used within group uses. so check "=", otherwise it matches the following:
    // use A\{const CONSTANTA, function myFunction,...}
    popState();
    String match = yytext();
    String[] segments = match.split("[ \n\r\t]+");
    int back = 1;
    if(segments.length > 1) {
        int wsLength = yylength() - 1 - segments[0].length(); // - "=" - {LABEL}
        back +=  wsLength;
    }
    yypushback(back);
    return PHPTokenId.PHP_STRING;
}

<ST_PHP_LOOKING_FOR_CONSTANT_NAME>{ANY_CHAR} {
    if(parenBalanceInConst == 0 && bracketBalanceInConst == 0) {
        isInConst = false;
    }
    yypushback(1);
    popState();
}

<ST_PHP_IN_SCRIPTING>"," {
    if (isInConst) {
        pushState(ST_PHP_LOOKING_FOR_CONSTANT_NAME);
    }
    return PHPTokenId.PHP_TOKEN;
}

<ST_PHP_IN_SCRIPTING>"return" {
    return PHPTokenId.PHP_RETURN;
}

<ST_PHP_IN_SCRIPTING>"yield"{WHITESPACE}+"from" {
    return PHPTokenId.PHP_YIELD_FROM;
}

<ST_PHP_IN_SCRIPTING>"yield" {
    return PHPTokenId.PHP_YIELD;
}

<ST_PHP_IN_SCRIPTING>"try" {
    return PHPTokenId.PHP_TRY;
}

<ST_PHP_IN_SCRIPTING>"catch" {
    return PHPTokenId.PHP_CATCH;
}

<ST_PHP_IN_SCRIPTING>"finally" {
    return PHPTokenId.PHP_FINALLY;
}

<ST_PHP_IN_SCRIPTING>"throw" {
    return PHPTokenId.PHP_THROW;
}

<ST_PHP_IN_SCRIPTING>"if" {
    return PHPTokenId.PHP_IF;
}

<ST_PHP_IN_SCRIPTING>"elseif" {
    return PHPTokenId.PHP_ELSEIF;
}

<ST_PHP_IN_SCRIPTING>"endif" {
    return PHPTokenId.PHP_ENDIF;
}

<ST_PHP_IN_SCRIPTING>"else" {
    return PHPTokenId.PHP_ELSE;
}

<ST_PHP_IN_SCRIPTING>"while" {
    return PHPTokenId.PHP_WHILE;
}

<ST_PHP_IN_SCRIPTING>"endwhile" {
    return PHPTokenId.PHP_ENDWHILE;
}

<ST_PHP_IN_SCRIPTING>"do" {
    return PHPTokenId.PHP_DO;
}

<ST_PHP_IN_SCRIPTING>"for" {
    return PHPTokenId.PHP_FOR;
}

<ST_PHP_IN_SCRIPTING>"endfor" {
    return PHPTokenId.PHP_ENDFOR;
}

<ST_PHP_IN_SCRIPTING>"foreach" {
    return PHPTokenId.PHP_FOREACH;
}

<ST_PHP_IN_SCRIPTING>"endforeach" {
    return PHPTokenId.PHP_ENDFOREACH;
}

<ST_PHP_IN_SCRIPTING>"declare" {
    return PHPTokenId.PHP_DECLARE;
}

<ST_PHP_IN_SCRIPTING>"enddeclare" {
    return PHPTokenId.PHP_ENDDECLARE;
}

<ST_PHP_IN_SCRIPTING>"instanceof" {
    return PHPTokenId.PHP_INSTANCEOF;
}

<ST_PHP_IN_SCRIPTING>"insteadof" {
    return PHPTokenId.PHP_INSTEADOF;
}

<ST_PHP_IN_SCRIPTING>"as" {
    return PHPTokenId.PHP_AS;
}

<ST_PHP_IN_SCRIPTING>"switch" {
    return PHPTokenId.PHP_SWITCH;
}

<ST_PHP_IN_SCRIPTING>"endswitch" {
    return PHPTokenId.PHP_ENDSWITCH;
}

<ST_PHP_IN_SCRIPTING>"case" {
    return PHPTokenId.PHP_CASE;
}

<ST_PHP_IN_SCRIPTING>"default" {
    return PHPTokenId.PHP_DEFAULT;
}

<ST_PHP_IN_SCRIPTING>"break" {
    return PHPTokenId.PHP_BREAK;
}

<ST_PHP_IN_SCRIPTING>"continue" {
    return PHPTokenId.PHP_CONTINUE;
}

<ST_PHP_IN_SCRIPTING>"echo" {
    return PHPTokenId.PHP_ECHO;
}

<ST_PHP_IN_SCRIPTING>"goto" {
    return PHPTokenId.PHP_GOTO;
}

<ST_PHP_IN_SCRIPTING>"print" {
    return PHPTokenId.PHP_PRINT;
}

<ST_PHP_IN_SCRIPTING>"class" {
    return PHPTokenId.PHP_CLASS;
}

<ST_PHP_IN_SCRIPTING>"trait" {
    return PHPTokenId.PHP_TRAIT;
}

<ST_PHP_IN_SCRIPTING>"interface" {
    return PHPTokenId.PHP_INTERFACE;
}

<ST_PHP_IN_SCRIPTING>"extends" {
    return PHPTokenId.PHP_EXTENDS;
}

<ST_PHP_IN_SCRIPTING>"implements" {
    return PHPTokenId.PHP_IMPLEMENTS;
}

<ST_PHP_IN_SCRIPTING>"self" {
    return PHPTokenId.PHP_SELF;
}

<ST_PHP_IN_SCRIPTING>{PHP_TYPE_INT} {
    return PHPTokenId.PHP_TYPE_INT;
}

<ST_PHP_IN_SCRIPTING>{PHP_TYPE_FLOAT} {
    return PHPTokenId.PHP_TYPE_FLOAT;
}

<ST_PHP_IN_SCRIPTING>{PHP_TYPE_STRING} {
    return PHPTokenId.PHP_TYPE_STRING;
}

<ST_PHP_IN_SCRIPTING>{PHP_TYPE_BOOL} {
    return PHPTokenId.PHP_TYPE_BOOL;
}

<ST_PHP_IN_SCRIPTING>{PHP_TYPE_VOID} {
    return PHPTokenId.PHP_TYPE_VOID;
}

<ST_PHP_IN_SCRIPTING>{PHP_TYPE_OBJECT} {
    return PHPTokenId.PHP_TYPE_OBJECT;
}

<ST_PHP_IN_SCRIPTING>"->" {
    pushState(ST_PHP_LOOKING_FOR_PROPERTY);
    return PHPTokenId.PHP_OBJECT_OPERATOR;
}

<ST_PHP_QUOTES_AFTER_VARIABLE> {
    "->" {
    popState();
    pushState(ST_PHP_LOOKING_FOR_PROPERTY);
    return PHPTokenId.PHP_OBJECT_OPERATOR;
    }
    {ANY_CHAR} {
        yypushback(1);
        popState();
    }
}

<ST_PHP_IN_SCRIPTING,ST_PHP_LOOKING_FOR_PROPERTY>{WHITESPACE}+ {
    return PHPTokenId.WHITESPACE;
}

<ST_PHP_LOOKING_FOR_PROPERTY>"->" {
    return PHPTokenId.PHP_OBJECT_OPERATOR;
}

<ST_PHP_LOOKING_FOR_PROPERTY>{LABEL} {
    popState();
    return PHPTokenId.PHP_STRING;
}

<ST_PHP_LOOKING_FOR_PROPERTY>{ANY_CHAR} {
    yypushback(1);
    popState();
}

<ST_PHP_IN_SCRIPTING>"::" {
    pushState(ST_PHP_LOOKING_FOR_STATIC_PROPERTY);
    return PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM;
}

<ST_PHP_LOOKING_FOR_STATIC_PROPERTY>{WHITESPACE}+ {
    return PHPTokenId.WHITESPACE;
}

<ST_PHP_LOOKING_FOR_STATIC_PROPERTY>"::" {
    return PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM;
}

<ST_PHP_LOOKING_FOR_STATIC_PROPERTY> {
    {LABEL} {
        popState();
        return PHPTokenId.PHP_STRING;
    }
    {ANY_CHAR} {
        yypushback(1);
        popState();
    }
}

<ST_PHP_IN_SCRIPTING>"\\" {
    return PHPTokenId.PHP_NS_SEPARATOR;
}
<ST_PHP_IN_SCRIPTING>"new" {
    return PHPTokenId.PHP_NEW;
}

<ST_PHP_IN_SCRIPTING>"clone" {
    return PHPTokenId.PHP_CLONE;
}

<ST_PHP_IN_SCRIPTING>"var" {
    return PHPTokenId.PHP_VAR;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}({PHP_TYPE_INT}|{PHP_TYPE_FLOAT}|{PHP_TYPE_STRING}|{PHP_TYPE_BOOL}){TABS_AND_SPACES}")" {
    return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}("int"|"integer"){TABS_AND_SPACES}")" {
    return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}("real"|"double"|"float"){TABS_AND_SPACES}")" {
    return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}"string"{TABS_AND_SPACES}")" {
    return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}"binary"{TABS_AND_SPACES}")" {
    return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}"array"{TABS_AND_SPACES}")" {
    return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}"object"{TABS_AND_SPACES}")" {
    return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}("bool"|"boolean"){TABS_AND_SPACES}")" {
    return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"("{TABS_AND_SPACES}("unset"){TABS_AND_SPACES}")" {
    return PHPTokenId.PHP_CASTING;
}

<ST_PHP_IN_SCRIPTING>"eval" {
    return PHPTokenId.PHP_EVAL;
}

<ST_PHP_IN_SCRIPTING>"include" {
    return PHPTokenId.PHP_INCLUDE;
}

<ST_PHP_IN_SCRIPTING>"include_once" {
    return PHPTokenId.PHP_INCLUDE_ONCE;
}

<ST_PHP_IN_SCRIPTING>"require" {
    return PHPTokenId.PHP_REQUIRE;
}

<ST_PHP_IN_SCRIPTING>"require_once" {
    return PHPTokenId.PHP_REQUIRE_ONCE;
}

<ST_PHP_IN_SCRIPTING>"namespace" {
    return PHPTokenId.PHP_NAMESPACE;
}

<ST_PHP_IN_SCRIPTING>"use" {
    return PHPTokenId.PHP_USE;
}

<ST_PHP_IN_SCRIPTING>"global" {
    return PHPTokenId.PHP_GLOBAL;
}

<ST_PHP_IN_SCRIPTING>"isset" {
    return PHPTokenId.PHP_ISSET;
}

<ST_PHP_IN_SCRIPTING>"empty" {
    return PHPTokenId.PHP_EMPTY;
}

<ST_PHP_IN_SCRIPTING>"__halt_compiler" {
    pushState(ST_HALTED_COMPILER);
    return PHPTokenId.PHP_HALT_COMPILER;
}

<ST_HALTED_COMPILER> {ANY_CHAR}+ {
    popState();
    return PHPTokenId.T_INLINE_HTML;
}

<ST_PHP_IN_SCRIPTING>"static" {
    return PHPTokenId.PHP_STATIC;
}

<ST_PHP_IN_SCRIPTING>"abstract" {
    return PHPTokenId.PHP_ABSTRACT;
}

<ST_PHP_IN_SCRIPTING>"final" {
    return PHPTokenId.PHP_FINAL;
}

<ST_PHP_IN_SCRIPTING>"private" {
    return PHPTokenId.PHP_PRIVATE;
}

<ST_PHP_IN_SCRIPTING>"protected" {
    return PHPTokenId.PHP_PROTECTED;
}

<ST_PHP_IN_SCRIPTING>"public" {
    return PHPTokenId.PHP_PUBLIC;
}

<ST_PHP_IN_SCRIPTING>"unset" {
    return PHPTokenId.PHP_UNSET;
}

<ST_PHP_IN_SCRIPTING>"list" {
    return PHPTokenId.PHP_LIST;
}

<ST_PHP_IN_SCRIPTING>"array" {
    return PHPTokenId.PHP_ARRAY;
}

<ST_PHP_IN_SCRIPTING>"callable" {
    return PHPTokenId.PHP_CALLABLE;
}

<ST_PHP_IN_SCRIPTING>{PHP_ITERABLE} {
    return PHPTokenId.PHP_ITERABLE;
}

<ST_PHP_IN_SCRIPTING>"parent" {
    return PHPTokenId.PHP_PARENT;
}

<ST_PHP_IN_SCRIPTING>"true" {
    return PHPTokenId.PHP_TRUE;
}

<ST_PHP_IN_SCRIPTING>"null" {
    return PHPTokenId.PHP_NULL;
}

<ST_PHP_IN_SCRIPTING>"false" {
    return PHPTokenId.PHP_FALSE;
}

<ST_PHP_IN_SCRIPTING>{PHP_OPERATOR} {
    return PHPTokenId.PHP_OPERATOR;
}

<ST_PHP_IN_SCRIPTING>{PHP_TEXTUAL_OPERATOR} {
    return PHPTokenId.PHP_TEXTUAL_OPERATOR;
}

<ST_PHP_IN_SCRIPTING>{TOKENS} {
    if(isInConst) {
        // for checking arrays
        // e.g. const CONST = [1, 2], const GOTO = 1;
        String text = yytext();
        switch (text) {
            case "[":
                bracketBalanceInConst++;
                break;
            case "]":
                bracketBalanceInConst--;
                break;
            case "(":
                parenBalanceInConst++;
                break;
            case ")":
                parenBalanceInConst--;
                break;
            default:
                break;
        }
    }
    return PHPTokenId.PHP_TOKEN;
}

<ST_PHP_IN_SCRIPTING>{CLOSE_EXPRESSION} {
    if(isInConst) {
        isInConst = false;
        parenBalanceInConst = 0;
        bracketBalanceInConst = 0;
    }
    return PHPTokenId.PHP_SEMICOLON;
}

<ST_PHP_IN_SCRIPTING>"{" {
    return PHPTokenId.PHP_CURLY_OPEN;
}

<ST_PHP_DOUBLE_QUOTES,ST_PHP_BACKQUOTE,ST_PHP_HEREDOC>"${" {
    pushState(ST_PHP_IN_SCRIPTING);
    return PHPTokenId.PHP_TOKEN;
}

<ST_PHP_IN_SCRIPTING>"}" {
    int lastState = stack.peek();
    if (lastState != ST_PHP_IN_SCRIPTING && lastState != YYINITIAL) {
        // probably in some sub state -> "{$" or "${"
        popState();
    }
    return PHPTokenId.PHP_CURLY_CLOSE;
}

<ST_PHP_IN_SCRIPTING>{BNUM} {
    return PHPTokenId.PHP_NUMBER;
}

<ST_PHP_IN_SCRIPTING>{LNUM} {
    return PHPTokenId.PHP_NUMBER;
}

<ST_PHP_IN_SCRIPTING>{HNUM} {
    return PHPTokenId.PHP_NUMBER;
}

<ST_PHP_VAR_OFFSET>0|([1-9][0-9]*) {
    return PHPTokenId.PHP_NUMBER;
}

<ST_PHP_VAR_OFFSET>{LNUM}|{HNUM} {
    return PHPTokenId.PHP_NUMBER;
}

<ST_PHP_IN_SCRIPTING>{DNUM}|{EXPONENT_DNUM} {
    return PHPTokenId.PHP_NUMBER;
}

<ST_PHP_IN_SCRIPTING>"__CLASS__" {
    return PHPTokenId.PHP__CLASS__;
}

<ST_PHP_IN_SCRIPTING>"__TRAIT__" {
    return PHPTokenId.PHP__TRAIT__;
}

<ST_PHP_IN_SCRIPTING>"__FUNCTION__" {
    return PHPTokenId.PHP__FUNCTION__;
}

<ST_PHP_IN_SCRIPTING>"__METHOD__" {
    return PHPTokenId.PHP__METHOD__;
}

<ST_PHP_IN_SCRIPTING>"__LINE__" {
    return PHPTokenId.PHP__LINE__;
}

<ST_PHP_IN_SCRIPTING>"__FILE__" {
    return PHPTokenId.PHP__FILE__;
}

<ST_PHP_IN_SCRIPTING>"__DIR__" {
    return PHPTokenId.PHP__DIR__;
}

<ST_PHP_IN_SCRIPTING>"__NAMESPACE__" {
    return PHPTokenId.PHP__NAMESPACE__;
}

<ST_PHP_IN_SCRIPTING>"$"{LABEL} {
    return PHPTokenId.PHP_VARIABLE;
}

<ST_PHP_DOUBLE_QUOTES,ST_PHP_BACKQUOTE,ST_PHP_HEREDOC,ST_PHP_VAR_OFFSET>"$"{LABEL} {
    pushState(ST_PHP_QUOTES_AFTER_VARIABLE);
    return PHPTokenId.PHP_VARIABLE;
}

<ST_PHP_DOUBLE_QUOTES,ST_PHP_HEREDOC,ST_PHP_BACKQUOTE>"$"{LABEL}"[" {
    yypushback(1);
    pushState(ST_PHP_VAR_OFFSET);
    return PHPTokenId.PHP_VARIABLE;
}

<ST_PHP_VAR_OFFSET>"]" {
    popState();
    return PHPTokenId.PHP_TOKEN;
}

<ST_PHP_VAR_OFFSET>"[" {
    return PHPTokenId.PHP_TOKEN;
}

<ST_PHP_VAR_OFFSET>{TOKENS}|[;{}\"`] {//the difference from the original rules comes from the fact that we took ';' out out of tokens
    return  PHPTokenId.UNKNOWN_TOKEN;
}

<ST_PHP_VAR_OFFSET>[ \n\r\t\\'#] {
    yypushback(1);
    popState();
        if (yylength() > 0)
            return PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE;
}

<ST_PHP_IN_SCRIPTING,ST_PHP_VAR_OFFSET>{LABEL} {
    return  PHPTokenId.PHP_STRING;
}

<ST_PHP_IN_SCRIPTING>([#]|"//") {
    pushState(ST_PHP_LINE_COMMENT);
    return PHPTokenId.PHP_LINE_COMMENT;
}

<ST_PHP_LINE_COMMENT>"?"|"%" {
    return PHPTokenId.PHP_LINE_COMMENT;
}

<ST_PHP_LINE_COMMENT>[^\n\r?%]*{ANY_CHAR} {
    String yytext = yytext();
    switch (yytext.charAt(yytext.length() - 1)) {
        case '?':
        case '%':
            yypushback(1);
            break;
        default:
            popState();
    }
     return PHPTokenId.PHP_LINE_COMMENT;
}

<ST_PHP_LINE_COMMENT>{NEWLINE} {
    popState();
    return PHPTokenId.PHP_LINE_COMMENT;
}


<ST_PHP_IN_SCRIPTING>"/**"{WHITESPACE} {
    pushState(ST_PHP_DOC_COMMENT);
    yypushback(yylength()-3);
    return PHPTokenId.PHPDOC_COMMENT_START;
}

<ST_PHP_DOC_COMMENT>"*/" {
    popState();
    return PHPTokenId.PHPDOC_COMMENT_END;
}

<ST_PHP_DOC_COMMENT>~"*/" {
        yypushback(2); // go back to mark end of comment in the next token
        return PHPTokenId.PHPDOC_COMMENT;
}

<ST_PHP_DOC_COMMENT> <<EOF>> {
              if (input.readLength() > 0) {
                    input.backup(1);  // backup eof
                    return PHPTokenId.PHPDOC_COMMENT;
                }
                else {
                    return null;
                }
}

<ST_PHP_IN_SCRIPTING>"/*" {
    pushState(ST_PHP_COMMENT);
    return PHPTokenId.PHP_COMMENT_START;
}

<ST_PHP_COMMENT>"*/" {
    popState();
    return PHPTokenId.PHP_COMMENT_END;
}

<ST_PHP_COMMENT>~"*/" {
    yypushback(2);
    return PHPTokenId.PHP_COMMENT;
}

<ST_PHP_COMMENT> <<EOF>> {
              if (input.readLength() > 0) {
                input.backup(1);  // backup eof
                return PHPTokenId.PHP_COMMENT;
              }
              else {
                  return null;
              }
}

<ST_PHP_IN_SCRIPTING,ST_PHP_LINE_COMMENT>"?>"{WHITESPACE}? {
        //popState();
        yybegin(YYINITIAL);
        if (yylength() > 2) {
            yypushback(yylength()-2);
        }
        stack.clear();
    return PHPTokenId.PHP_CLOSETAG;
}

<ST_PHP_IN_SCRIPTING,ST_PHP_LINE_COMMENT>"</script>"{WHITESPACE}? {
        popState();
    return PHPTokenId.T_INLINE_HTML;
}

<ST_PHP_IN_SCRIPTING>"%>"{WHITESPACE}? {
    if (aspTagsAllowed) {
            yybegin(YYINITIAL);
            stack.clear();
        return PHPTokenId.PHP_CLOSETAG;
    }
    return  PHPTokenId.UNKNOWN_TOKEN;
}

<ST_PHP_LINE_COMMENT>"%>"{WHITESPACE}? {
    if (aspTagsAllowed) {
            yybegin(YYINITIAL);
            stack.clear();
        return PHPTokenId.PHP_CLOSETAG;
    }
    String text = yytext();
    if(text.indexOf('\r') != -1 || text.indexOf('\n') != -1 ){
        popState();
    }
    return PHPTokenId.PHP_LINE_COMMENT;
}

<ST_PHP_IN_SCRIPTING>(b?[\"]{DOUBLE_QUOTES_CHARS}*("{"*|"$"*)[\"]) {
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_IN_SCRIPTING>(b?[']([^'\\]|("\\"{ANY_CHAR}))*[']) {
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_IN_SCRIPTING>b?[\"] {
    pushState(ST_PHP_DOUBLE_QUOTES);
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_IN_SCRIPTING>b?"<<<"{TABS_AND_SPACES}[']{LABEL}[']{NEWLINE} {
    int bprefix = (yytext().charAt(0) != '<') ? 1 : 0;
        int startString=3+bprefix;
        /* 3 is <<<, 2 is quotes, 1 is newline */
        hereocLength = yylength()-bprefix-3-2-1-(yytext().charAt(yylength()-2)=='\r'?1:0);
        while ((yytext().charAt(startString) == ' ') || (yytext().charAt(startString) == '\t')) {
            startString++;
            hereocLength--;
        }
        // first quate
        startString++;
        heredoc = yytext().substring(startString, hereocLength+startString);
        yybegin(ST_PHP_START_NOWDOC);
        return PHPTokenId.PHP_NOWDOC_TAG_START;
}

<ST_PHP_START_NOWDOC>{ANY_CHAR} {
    yypushback(1);
    yybegin(ST_PHP_NOWDOC);
}

<ST_PHP_START_NOWDOC>{TABS_AND_SPACES}{LABEL}";"?[^\n\r]*[\r\n]? {
    /* <ST_START_NOWDOC>{TABS_AND_SPACES}{LABEL}";"?[^\n\r]*[\r\n]? */
    // there is no [\r\n] if it is the last line
    // i.e. not [\r\n] but EOF, so check not [\r\n] but [\r\n]?
    if (isEndNowdoc()) {
        int indexOfNowdocId = yytext().indexOf(heredoc);
        int back = yylength() - indexOfNowdocId - heredoc.length();
        yypushback(back);
        heredoc=null;
        hereocLength=0;
        yybegin(ST_PHP_IN_SCRIPTING);
        return PHPTokenId.PHP_NOWDOC_TAG_END;
    } else {
        yypushback(1);
        yybegin(ST_PHP_NOWDOC);
    }
}

<ST_PHP_NOWDOC> {
    {NEWLINE}{TABS_AND_SPACES}{LABEL}";"?[^\n\r]*[\r\n]? {
        /* <ST_PHP_NOWDOC>{NEWLINE}{TABS_AND_SPACES}{LABEL}";"?[^\n\r]*[\r\n]? */
        if (isEndNowdoc()) {
            String yytext = yytext();
            int trailingNewlineOffset = (yytext.endsWith("\n") || yytext.endsWith("\r")) ? 2 : 0;
            int lastIndexOfNewline = yytext.lastIndexOf('\n', yylength() - trailingNewlineOffset);
            if (lastIndexOfNewline == -1) {
                lastIndexOfNewline = yytext.lastIndexOf('\r', yylength() - trailingNewlineOffset);
            }
            int back = yylength() - lastIndexOfNewline - 1; // -1 [\r\n] length
            yypushback(back);
            yybegin(ST_PHP_END_NOWDOC);
            return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
        } else {
            yypushback(1); // [\r\n] length
        }
    }

    {NOWDOC_CHARS}|{NEWLINE} {
        /* <ST_PHP_NOWDOC>{NOWDOC_CHARS}|{NEWLINE} */
        // retrun PHPTokenId when the closing marker is found
    }
}

<ST_PHP_END_NOWDOC>{NEWLINE}*{TABS_AND_SPACES}{LABEL}";"? {
    heredoc=null; hereocLength=0;
    yybegin(ST_PHP_IN_SCRIPTING);
    int back = 0;
    // mark just the label
    if (yytext().charAt(yylength() - 1)==';') {
        back++;
    }
    yypushback(back);
    return PHPTokenId.PHP_NOWDOC_TAG_END;
}

<ST_PHP_IN_SCRIPTING>b?"<<<"{TABS_AND_SPACES}({LABEL}|"\""{LABEL}"\""){NEWLINE} {
    int bprefix = (yytext().charAt(0) != '<') ? 1 : 0;
    int startString=3+bprefix;
    hereocLength = yylength()-bprefix-3-1-(yytext().charAt(yylength()-2)=='\r'?1:0);
    while ((yytext().charAt(startString) == ' ') || (yytext().charAt(startString) == '\t')) {
        startString++;
        hereocLength--;
    }
    // HEREDOC PHP 5.3
    if (yytext().charAt(startString) == '"') {
        hereocLength -= 2;
        startString ++;
    }
    heredoc = yytext().substring(startString,hereocLength+startString);
    yybegin(ST_PHP_START_HEREDOC);
    return PHPTokenId.PHP_HEREDOC_TAG_START;
}

<ST_PHP_START_HEREDOC> {
    {TABS_AND_SPACES}{LABEL}";"?[^\n\r]*[\n\r]? {
        int trailingNewLineLength = 1;
        int label_len = yylength() - trailingNewLineLength;
        int back = trailingNewLineLength;

        if (yytext().charAt(label_len - 1)==';') {
           label_len--;
           back++;
        }

        if (isEndHeredoc()) {
            int indexOfHeredocId = yytext().indexOf(heredoc);
            back += label_len - indexOfHeredocId;
            yypushback(back);
            yybegin(ST_PHP_END_HEREDOC);
        } else {
            yypushback(yylength() - trailingNewLineLength);
            yybegin(ST_PHP_HEREDOC);
        }
    }
    {ANY_CHAR} {
        yypushback(1);
        yybegin(ST_PHP_HEREDOC);
    }
}

<ST_PHP_HEREDOC> {
    {NEWLINE}{TABS_AND_SPACES}{LABEL}";"?[^\n\r]*[\n\r]? {
        /* {NEWLINE}{TABS_AND_SPACES}{LABEL}";"?[^\n\r]*[\n\r]? */
        int trailingNewLineLength = 1;
        if (isEndHeredoc()) {
            String yytext = yytext();
            int newlineLength = yytext.startsWith("\r\n") ? 2 : 1;
            int back = yylength() - newlineLength;
            yypushback(back);
            yybegin(ST_PHP_END_HEREDOC);
        } else {
            // handle variable
            char previousChar = ' ';
            int indexOfVariable = -1;
            for (int i = 0; i < yylength(); i++) {
                char currentChar = yytext().charAt(i);
                if (currentChar == '$' && previousChar == '{') {
                    indexOfVariable = i - 1;
                    break;
                }
                if (currentChar == '$' && previousChar != '\\') {
                    indexOfVariable = i;
                    break;
                }
                previousChar = currentChar;
            }

            if (indexOfVariable == -1) {
                yypushback(trailingNewLineLength);
            } else {
                yypushback(yylength() - indexOfVariable);
                return PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE;
            }
        }
        return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
    }

    {HEREDOC_CHARS} {
        int indexOfNewline = yytext().indexOf("\r");
        if (indexOfNewline == -1) {
            indexOfNewline = yytext().indexOf("\n");
        }
        if (indexOfNewline > 0) {
            // if index equals 0, don't pushback (infinite loop)
            yypushback(yylength() - indexOfNewline);
        }
        return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
    }

    {HEREDOC_CHARS}("{$" | "${") {
        yypushback(2);
        return PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE;
    }

    {HEREDOC_CHARS}"$"{LABEL}"["? {
        String text = yytext();
        int lastIndexOfDollar = text.lastIndexOf('$');
        yypushback(text.length() - lastIndexOfDollar);
        return PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE;
    }

    "$" | "{" {
        return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
    }
}

<ST_PHP_IN_SCRIPTING>[`] {
    pushState(ST_PHP_BACKQUOTE);
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_END_HEREDOC>{TABS_AND_SPACES}{LABEL}";"? {
    heredoc=null;
    hereocLength=0;
    yybegin(ST_PHP_IN_SCRIPTING);
    int back = 0;
    // mark just the label
    if (yytext().charAt(yylength() - 1)==';') {
        back++;
    }
    yypushback(back);
    return PHPTokenId.PHP_HEREDOC_TAG_END;
}

<ST_PHP_DOUBLE_QUOTES,ST_PHP_BACKQUOTE,ST_PHP_HEREDOC,ST_PHP_QUOTES_AFTER_VARIABLE>"{$" {
    yypushback(1);
    pushState(ST_PHP_IN_SCRIPTING);
    return PHPTokenId.PHP_CURLY_OPEN;
}

<ST_PHP_DOUBLE_QUOTES>{DOUBLE_QUOTES_CHARS}+ {
    return PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE;
}

/*
The original parsing rule was {DOUBLE_QUOTES_CHARS}*("{"{2,}|"$"{2,}|(("{"+|"$"+)[\"]))
but jflex doesn't support a{n,} so we changed a{2,} to aa+
*/
<ST_PHP_DOUBLE_QUOTES>{DOUBLE_QUOTES_CHARS}*("{""{"+|"$""$"+|(("{"+|"$"+)[\"])) {
    yypushback(1);
    return PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE;
}

<ST_PHP_BACKQUOTE>{BACKQUOTE_CHARS}+ {
    return PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE;
}

/*
The original parsing rule was {BACKQUOTE_CHARS}*("{"{2,}|"$"{2,}|(("{"+|"$"+)[`]))
but jflex doesn't support a{n,} so we changed a{2,} to aa+
*/
<ST_PHP_BACKQUOTE>{BACKQUOTE_CHARS}*("{""{"+|"$""$"+|(("{"+|"$"+)[`])) {
    yypushback(1);
    return PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE;
}

<ST_PHP_DOUBLE_QUOTES>[\"] {
    popState();
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_BACKQUOTE>[`] {
    popState();
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_DOUBLE_QUOTES>. {
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

<ST_PHP_BACKQUOTE>. {
    return PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
}

/* ============================================
   Stay in this state until we find a whitespace.
   After we find a whitespace we go the the prev state and try again from the next token.
   ============================================ */
<ST_PHP_HIGHLIGHTING_ERROR> {
    {WHITESPACE}	{popState();return PHPTokenId.WHITESPACE;}
    .   	        {return  PHPTokenId.UNKNOWN_TOKEN;}
}

/* ============================================
   This rule must be the last in the section!!
   it should contain all the states.
   ============================================ */
<ST_PHP_IN_SCRIPTING,ST_PHP_DOUBLE_QUOTES,ST_PHP_VAR_OFFSET,ST_PHP_BACKQUOTE,ST_PHP_HEREDOC,ST_PHP_START_HEREDOC,ST_PHP_END_HEREDOC,ST_PHP_NOWDOC,ST_PHP_START_NOWDOC,ST_PHP_END_NOWDOC>. {
    yypushback(1);
    pushState(ST_PHP_HIGHLIGHTING_ERROR);
}
