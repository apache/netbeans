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

package org.netbeans.modules.javascript2.lexer;

import java.util.LinkedList;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

@org.netbeans.api.annotations.common.SuppressWarnings({"SF_SWITCH_FALLTHROUGH", "URF_UNREAD_FIELD", "DLS_DEAD_LOCAL_STORE", "DM_DEFAULT_ENCODING"})
%%

%public
%final
%class JsColoringLexer
%type JsTokenId
%unicode
%char

%{
    private LexerInput input;

    private boolean embedded;

    private boolean canFollowLiteral = true;

    private boolean canFollowKeyword = true;

    private JsTokenId lastNonWhiteToken = null;

    private LinkedList<Brace> braceBalances = new LinkedList<>();

    private enum Brace {
            EXP,
            JSX,
            TEMPLATE
    }

    private LinkedList<Integer> jsxBalances = new LinkedList<Integer>();

    public JsColoringLexer(LexerRestartInfo info) {
        this.input = info.input();

        this.embedded = !JsTokenId.JAVASCRIPT_MIME_TYPE.equals(info.languagePath().mimePath());
        if(info.state() != null) {
            //reset state
            setState((LexerState)info.state());
        } else {
            //initial state
            zzState = zzLexicalState = YYINITIAL;
        }
    }

    public LexerState getState() {
        if (zzState == YYINITIAL && zzLexicalState == YYINITIAL
                && canFollowLiteral && canFollowKeyword) {
            return null;
        }
        return new LexerState(zzState, zzLexicalState, canFollowLiteral, canFollowKeyword, braceBalances, jsxBalances, lastNonWhiteToken);
    }

    public void setState(LexerState state) {
        this.zzState = state.zzState;
        this.zzLexicalState = state.zzLexicalState;
        this.canFollowLiteral = state.canFollowLiteral;
        this.canFollowKeyword = state.canFollowKeyword;
        this.braceBalances = new LinkedList<>(state.braceBalances);
        this.jsxBalances = new LinkedList<Integer>(state.jsxBalances);
        this.lastNonWhiteToken = state.lastNonWhiteToken;
    }

    public JsTokenId nextToken() throws java.io.IOException {
        JsTokenId token = yylex();
        if (token != null && !JsTokenId.ERROR.equals(token)
                && !JsTokenId.UNKNOWN.equals(token)
                && !JsTokenId.WHITESPACE.equals(token)
                && !JsTokenId.LINE_COMMENT.equals(token)
                && !JsTokenId.BLOCK_COMMENT.equals(token)
                && !JsTokenId.DOC_COMMENT.equals(token)) {
            canFollowLiteral = canFollowLiteral(token);
            if (!JsTokenId.EOL.equals(token)) {
                canFollowKeyword = canFollowKeyword(token);
                lastNonWhiteToken = token;
            }
        }
        return token;
    }

    private JsTokenId getErrorToken() {
        if (embedded) {
            return JsTokenId.UNKNOWN;
        }
        if (yystate() == JSX) {
            return JsTokenId.JSX_TEXT;
        }
        return JsTokenId.ERROR;
    }

    private static boolean canFollowLiteral(JsTokenId token) {
        switch (token) {
            case OPERATOR_INCREMENT:
            case OPERATOR_DECREMENT:
                return false;
            case BRACKET_LEFT_CURLY:
            case BRACKET_LEFT_PAREN:
            case BRACKET_LEFT_BRACKET:
            case KEYWORD_RETURN:
            case KEYWORD_THROW:
            case KEYWORD_YIELD:
            // unary operators (keyword)
            case KEYWORD_TYPEOF:
            // end of line
            case EOL:
            case OPERATOR_SEMICOLON:
            case OPERATOR_COMMA:
            case OPERATOR_DOT:
            case OPERATOR_COLON:
                return true;
        }

        if ("operator".equals(token.primaryCategory())) {
            return true;
        }
        return false;
    }

    private static boolean canFollowKeyword(JsTokenId token) {
        if (JsTokenId.OPERATOR_DOT.equals(token)) {
            return false;
        }
        return true;
    }

    public static final class LexerState  {
        /** the current state of the DFA */
        final int zzState;
        /** the current lexical state */
        final int zzLexicalState;
        /** can be the literal used here */
        final boolean canFollowLiteral;
        /** can be the literal used here */
        final boolean canFollowKeyword;
        /** where we are in Brace Type */
        final LinkedList<Brace> braceBalances;
        /** are we in jsx primary expression */
        final LinkedList<Integer> jsxBalances;
        /** remember last non white token */
        final JsTokenId lastNonWhiteToken;

        LexerState (int zzState, int zzLexicalState, boolean canFollowLiteral, boolean canFollowKeyword, LinkedList<Brace> braceBalances, LinkedList<Integer> jsxBalances, JsTokenId lastNonWhiteToken) {
            this.zzState = zzState;
            this.zzLexicalState = zzLexicalState;
            this.canFollowLiteral = canFollowLiteral;
            this.canFollowKeyword = canFollowKeyword;
            this.braceBalances = new LinkedList<>(braceBalances);
            this.jsxBalances = new LinkedList<Integer>(jsxBalances);
            this.lastNonWhiteToken = lastNonWhiteToken;
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
            if (this.zzState != other.zzState) {
                return false;
            }
            if (this.zzLexicalState != other.zzLexicalState) {
                return false;
            }
            if (this.canFollowLiteral != other.canFollowLiteral) {
                return false;
            }
            if (this.canFollowKeyword != other.canFollowKeyword) {
                return false;
            }
            if (this.braceBalances.size() != other.braceBalances.size()) {
                return false;
            }
            for (int i = 0; i < this.braceBalances.size(); i++) {
                if (this.braceBalances.get(i).equals(other.braceBalances.get(i))) {
                    return false;
                }
            }
            if (this.jsxBalances.size() != other.jsxBalances.size()) {
                return false;
            }
            for (int i = 0; i < this.jsxBalances.size(); i++) {
                if (this.jsxBalances.get(i).equals(other.jsxBalances.get(i))) {
                    return false;
                }
            }
            if (this.lastNonWhiteToken != other.lastNonWhiteToken) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 29 * hash + this.zzState;
            hash = 29 * hash + this.zzLexicalState;
            hash = 29 * hash + (this.canFollowLiteral ? 1 : 0);
            hash = 29 * hash + (this.canFollowKeyword ? 1 : 0);
            for (int i = 0; i < this.braceBalances.size(); i++) {
                hash = 29 * hash + this.braceBalances.get(i).ordinal();
            }
            for (int i = 0; i < this.jsxBalances.size(); i++) {
                hash = 29 * hash + this.jsxBalances.get(i);
            }
            hash = 29 * hash + this.lastNonWhiteToken.ordinal();
            return hash;
        }

        @Override
        public String toString() {
            return "LexerState{canFollowLiteral=" + canFollowLiteral + ", canFollowKeyword=" + canFollowKeyword
                + ", braceBalances=" + braceBalances + ", jsxBalances=" + jsxBalances + '}';
        }
    }

 // End user code

%}

/* main character classes */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = [ \t\f\u00A0\u000B]+

/* comments */
TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
ShebangComment = "#!" {InputCharacter}*
EndOfLineComment = "//" {InputCharacter}*
DocumentationComment = "/*" "*"+ [^/*] ~"*/"

/* identifiers */
IdentifierPart = [:jletterdigit:]
Identifier = [:jletter:]{IdentifierPart}*
PrivateIdentifier = "#" {Identifier}?

/* integer literals */
DecIntegerLiteral = 0 | [1-9] ("_"* [0-9])*
DecLongLiteral    = {DecIntegerLiteral} [lL]
BigInt = {DecIntegerLiteral} [n]

HexIntegerLiteral = 0 [xX] {HexDigit} ("_"* {HexDigit})*
HexLongLiteral    = {HexIntegerLiteral} [lL]
HexDigit          = [0-9a-fA-F]

OctIntegerLiteral = 0 [oO] {OctDigit} ("_"* {OctDigit})*

OctLegacyIntegerLiteral = 0+ [1-3]? {OctDigit} {1,15}
OctLegacyLongLiteral    = 0+ 1? {OctDigit} {1,21} [lL]
OctDigit          = [0-7]

BinaryIntegerLiteral = 0 [bB] {BinaryDigit} ("_"* {BinaryDigit})*
BinaryDigit          = [0-1]

/* floating point literals */
FloatLiteral  = ({FLit1}|{FLit2}|{FLit3}) {Exponent}? [fF]
DoubleLiteral = ({FLit1}|{FLit2}|{FLit3}) {Exponent}?

FLit1    = [0-9]+ \. [0-9]*
FLit2    = \. [0-9]+
FLit3    = [0-9]+
Exponent = [eE] [+-]? [0-9]+

/* string and character literals */
TemplateCharacter = [^`$\\]
StringCharacter  = [^\r\n\"\\] | \\{LineTerminator}
SStringCharacter = [^\r\n\'\\] | \\{LineTerminator}
JSXCharacter = [^<>/{]

RegexpBackslashSequence = \\{InputCharacter}
RegexpClass = "["([^\x5d\r\n\\] | {RegexpBackslashSequence})*"]"
RegexpCharacter = [^\x5b/\r\n\\] | {RegexpBackslashSequence} | {RegexpClass}
RegexpFirstCharacter = [^*\x5b/\r\n\\] | {RegexpBackslashSequence} | {RegexpClass}

%state INITIAL
%state STRING
%state STRINGEND
%state SSTRING
%state SSTRINGEND
%state TEMPLATE
%state TEMPLATEEND
%state TEMPLATEEXP
%state TEMPLATEEXPEND
%state REGEXP
%state REGEXPEND
%state LCOMMENTEND
%state JSX
%state JSXEXP
%state JSXEXPEND
%state ERROR

%%

<YYINITIAL> {
  {ShebangComment}               {
                                   yybegin(LCOMMENTEND);
                                   return JsTokenId.LINE_COMMENT;
                                 }
  .|\n                           {
                                   yypushback(1);
                                   yybegin(INITIAL);
                                 }
}

<INITIAL> {

  /* keywords 7.6.1.1 */
  "break"                        { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_BREAK; }
  "case"                         { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_CASE; }
  "catch"                        { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_CATCH; }
  "continue"                     { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_CONTINUE; }
  "debugger"                     { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_DEBUGGER; }
  "default"                      { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_DEFAULT; }
  "delete"                       { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_DELETE; }
  "do"                           { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_DO; }
  "else"                         { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_ELSE; }
  "finally"                      { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_FINALLY; }
  "for"                          { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_FOR; }
  "function"                     { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_FUNCTION; }
  "if"                           { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_IF; }
  "in"                           { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_IN; }
  "instanceof"                   { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_INSTANCEOF; }
  "new"                          { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_NEW; }
  "return"                       { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_RETURN; }
  "switch"                       { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_SWITCH; }
  "this"                         { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_THIS; }
  "throw"                        { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_THROW; }
  "try"                          { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_TRY; }
  "typeof"                       { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_TYPEOF; }
  "var"                          { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_VAR; }
  "void"                         { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_VOID; }
  "while"                        { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_WHILE; }
  "with"                         { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_WITH; }

  "class"                        { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_CLASS; }
  "const"                        { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_CONST; }
  "export"                       { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_EXPORT; }
  "extends"                      { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_EXTENDS; }
  "import"                       { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_IMPORT; }
  "super"                        { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_SUPER; }
  "yield"                        { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_YIELD; }

  /* reserved keywords 7.6.1.2 */
  "enum"                         { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_ENUM; }

  "implements"                   { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_IMPLEMENTS; }
  "interface"                    { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_INTERFACE; }
  "let"                          { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_LET; }
  "package"                      { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_PACKAGE; }
  "private"                      { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_PRIVATE; }
  "protected"                    { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_PROTECTED; }
  "public"                       { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_PUBLIC; }
  "static"                       { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_STATIC; }

  "await"                        { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_AWAIT; }

  /* boolean literals */
  "true"                         { return JsTokenId.KEYWORD_TRUE; }
  "false"                        { return JsTokenId.KEYWORD_FALSE; }

  /* null literal */
  "null"                         { return JsTokenId.KEYWORD_NULL; }

  "/"[*]                         { return getErrorToken(); }
  "/"
                                 {
                                     if (canFollowLiteral) {
                                       yybegin(REGEXP);
                                       return JsTokenId.REGEXP_BEGIN;
                                     } else {
                                       return JsTokenId.OPERATOR_DIVISION;
                                     }
                                 }
  "/="
                                 {
                                     if (canFollowLiteral) {
                                       yypushback(1);
                                       yybegin(REGEXP);
                                       return JsTokenId.REGEXP_BEGIN;
                                     } else {
                                       return JsTokenId.OPERATOR_DIVISION_ASSIGNMENT;
                                     }
                                 }
  /* operators */

  "("                            { return JsTokenId.BRACKET_LEFT_PAREN; }
  ")"                            { return JsTokenId.BRACKET_RIGHT_PAREN; }
  "{"                            { 
                                     // we are checking if we are in template expression
                                     if (!braceBalances.isEmpty()) {
                                        braceBalances.push(Brace.EXP);
                                     }

                                     return JsTokenId.BRACKET_LEFT_CURLY;
                                 }
  "}"                            { 
                                     // we are checking if we are in template expression
                                     if (!braceBalances.isEmpty()) {
                                        Brace braceType = braceBalances.pop();
                                        if (braceType == Brace.TEMPLATE) {
                                            yypushback(1);
                                            yybegin(TEMPLATEEXPEND);
                                        } else if (braceType == Brace.JSX) {
                                            yypushback(1);
                                            yybegin(JSXEXPEND);
                                        } else {
                                            return JsTokenId.BRACKET_RIGHT_CURLY;
                                        }
                                     } else {
                                        return JsTokenId.BRACKET_RIGHT_CURLY;
                                     }
                                 }
  "["                            { return JsTokenId.BRACKET_LEFT_BRACKET; }
  "]"                            { return JsTokenId.BRACKET_RIGHT_BRACKET; }
  ";"                            { return JsTokenId.OPERATOR_SEMICOLON; }
  ","                            { return JsTokenId.OPERATOR_COMMA; }
  "."                            { return JsTokenId.OPERATOR_DOT; }
  "?."                           { return JsTokenId.OPERATOR_OPTIONAL_ACCESS; }
  "..."                          { return JsTokenId.OPERATOR_REST; }
  "="                            { return JsTokenId.OPERATOR_ASSIGNMENT; }
  ">"                            { return JsTokenId.OPERATOR_GREATER; }
  "<"                            { if (!canFollowLiteral || (lastNonWhiteToken != null 
                  && (lastNonWhiteToken == JsTokenId.IDENTIFIER
                  || lastNonWhiteToken == JsTokenId.NUMBER))) {
                                        return JsTokenId.OPERATOR_LOWER; 
                                   } else {
                                        jsxBalances.push(0);
                                        yypushback(1);
                                        yybegin(JSX);
                                   }
                                 }
  "!"                            { return JsTokenId.OPERATOR_NOT; }
  "~"                            { return JsTokenId.OPERATOR_BITWISE_NOT; }
  "?"                            { return JsTokenId.OPERATOR_TERNARY; }
  ":"                            { return JsTokenId.OPERATOR_COLON; }
  "=="                           { return JsTokenId.OPERATOR_EQUALS; }
  "==="                          { return JsTokenId.OPERATOR_EQUALS_EXACTLY; }
  "<="                           { return JsTokenId.OPERATOR_LOWER_EQUALS; }
  ">="                           { return JsTokenId.OPERATOR_GREATER_EQUALS; }
  "!="                           { return JsTokenId.OPERATOR_NOT_EQUALS; }
  "!=="                          { return JsTokenId.OPERATOR_NOT_EQUALS_EXACTLY; }
  "&&"                           { return JsTokenId.OPERATOR_AND; }
  "||"                           { return JsTokenId.OPERATOR_OR; }
  "++"                           { return JsTokenId.OPERATOR_INCREMENT; }
  "--"                           { return JsTokenId.OPERATOR_DECREMENT; }
  "+"                            { return JsTokenId.OPERATOR_PLUS; }
  "-"                            { return JsTokenId.OPERATOR_MINUS; }
  "**"                           { return JsTokenId.OPERATOR_EXPONENTIATION; }
  "*"                            { return JsTokenId.OPERATOR_MULTIPLICATION; }
  "&"                            { return JsTokenId.OPERATOR_BITWISE_AND; }
  "|"                            { return JsTokenId.OPERATOR_BITWISE_OR; }
  "^"                            { return JsTokenId.OPERATOR_BITWISE_XOR; }
  "%"                            { return JsTokenId.OPERATOR_MODULUS; }
  "<<"                           { return JsTokenId.OPERATOR_LEFT_SHIFT_ARITHMETIC; }
  ">>"                           { return JsTokenId.OPERATOR_RIGHT_SHIFT_ARITHMETIC; }
  ">>>"                          { return JsTokenId.OPERATOR_RIGHT_SHIFT; }
  "+="                           { return JsTokenId.OPERATOR_PLUS_ASSIGNMENT; }
  "-="                           { return JsTokenId.OPERATOR_MINUS_ASSIGNMENT; }
  "**="                          { return JsTokenId.OPERATOR_EXPONENTIATION_ASSIGNMENT; }
  "*="                           { return JsTokenId.OPERATOR_MULTIPLICATION_ASSIGNMENT; }
  "&="                           { return JsTokenId.OPERATOR_BITWISE_AND_ASSIGNMENT; }
  "|="                           { return JsTokenId.OPERATOR_BITWISE_OR_ASSIGNMENT; }
  "^="                           { return JsTokenId.OPERATOR_BITWISE_XOR_ASSIGNMENT; }
  "%="                           { return JsTokenId.OPERATOR_MODULUS_ASSIGNMENT; }
  "<<="                          { return JsTokenId.OPERATOR_LEFT_SHIFT_ARITHMETIC_ASSIGNMENT; }
  ">>="                          { return JsTokenId.OPERATOR_RIGHT_SHIFT_ARITHMETIC_ASSIGNMENT; }
  ">>>="                         { return JsTokenId.OPERATOR_RIGHT_SHIFT_ASSIGNMENT; }
  "=>"                           { return JsTokenId.OPERATOR_ARROW; }
  "@"                            { return JsTokenId.OPERATOR_AT; }
  "??"                           { return JsTokenId.OPERATOR_NULLISH; }
  "&&="                          { return JsTokenId.OPERATOR_ASSIGN_LOG_AND; }
  "||="                          { return JsTokenId.OPERATOR_ASSIGN_LOG_OR; }
  "??="                          { return JsTokenId.OPERATOR_ASSIGN_NULLISH; }

  /* string literal */
  \"                             {
                                    yybegin(STRING);
                                    return JsTokenId.STRING_BEGIN;
                                 }

  \'                             {
                                    yybegin(SSTRING);
                                    return JsTokenId.STRING_BEGIN;
                                 }

   `                             {
                                    yybegin(TEMPLATE);
                                    return JsTokenId.TEMPLATE_BEGIN;
                                 }

  /* numeric literals */

  {DecIntegerLiteral}            |
  {DecLongLiteral}               |
  {BigInt}                       |

  {BinaryIntegerLiteral}         |

  {HexIntegerLiteral}            |
  {HexLongLiteral}               |

  {OctIntegerLiteral}            |
  {OctLegacyIntegerLiteral}      |
  {OctLegacyLongLiteral}         |

  {FloatLiteral}                 |
  {DoubleLiteral}                |
  {DoubleLiteral}[dD]            { return JsTokenId.NUMBER; }

  /* comments */
  {DocumentationComment}         { return JsTokenId.DOC_COMMENT; }

  /* comments */
  {TraditionalComment}           { return JsTokenId.BLOCK_COMMENT; }

  /* comments */
  {EndOfLineComment}             {
                                   yybegin(LCOMMENTEND);
                                   return JsTokenId.LINE_COMMENT;
                                 }

  /* whitespace */
  {WhiteSpace}                   { return JsTokenId.WHITESPACE; }

  /* whitespace */
  {LineTerminator}               { return JsTokenId.EOL; }

  /* identifiers */
  {Identifier}                   { return JsTokenId.IDENTIFIER; }
  {PrivateIdentifier}            { return JsTokenId.PRIVATE_IDENTIFIER; }
}

<STRING> {
  \"                             {  
                                     yypushback(1);
                                     yybegin(STRINGEND);
                                     if (tokenLength - 1 > 0) {
                                         return JsTokenId.STRING;
                                     }
                                 }

  {StringCharacter}+             { }

  \\[0-3]?{OctDigit}?{OctDigit}  { }

  /* escape sequences */

  \\.                            { }
  {LineTerminator}               {
                                     yypushback(1);
                                     yybegin(INITIAL);
                                     if (tokenLength - 1 > 0) {
                                         return getErrorToken();
                                     }
                                 }
}

<STRINGEND> {
  \"                             {
                                     yybegin(INITIAL);
                                     return JsTokenId.STRING_END;
                                 }
}

<SSTRING> {
  \'                             {
                                     yypushback(1);
                                     yybegin(SSTRINGEND);
                                     if (tokenLength - 1 > 0) {
                                         return JsTokenId.STRING;
                                     }
                                 }

  {SStringCharacter}+            { }

  \\[0-3]?{OctDigit}?{OctDigit}  { }

  /* escape sequences */

  \\.                            { }
  {LineTerminator}               {
                                     yypushback(1);
                                     yybegin(INITIAL);
                                     if (tokenLength -1 > 0) {
                                         return getErrorToken();
                                     }
                                 }
}

<SSTRINGEND> {
  \'                             {
                                     yybegin(INITIAL);
                                     return JsTokenId.STRING_END;
                                 }
}

<TEMPLATE> {
  `                              {
                                     yypushback(1);
                                     yybegin(TEMPLATEEND);
                                     if (tokenLength - 1 > 0) {
                                         return JsTokenId.TEMPLATE;
                                     }
                                 }

  "$"\{                          { 
                                     yypushback(2);
                                     yybegin(TEMPLATEEXP);
                                     if (tokenLength - 2 > 0) {
                                         return JsTokenId.TEMPLATE;
                                     }
                                 }

  "$"                            |
  {TemplateCharacter}+           { }

  \\.                            { }
}

<TEMPLATEEND> {
  `                              {
                                     yybegin(INITIAL);
                                     return JsTokenId.TEMPLATE_END;
                                 }
}

<TEMPLATEEXP> {
  "$"\{                          {
                                     braceBalances.push(Brace.TEMPLATE);
                                     yybegin(INITIAL);
                                     return JsTokenId.TEMPLATE_EXP_BEGIN;
                                 }
}

<TEMPLATEEXPEND> {
  "}"                            {
                                     yybegin(TEMPLATE);
                                     return JsTokenId.TEMPLATE_EXP_END;
                                 }
}

<REGEXP> {
  {RegexpFirstCharacter}{RegexpCharacter}*"/"
                                 {
                                     yypushback(1);
                                     yybegin(REGEXPEND);
                                     if (tokenLength - 1 > 0) {
                                         return JsTokenId.REGEXP;
                                     }
                                 }
  .                              {
                                     yypushback(1);
                                     yybegin(ERROR);
                                 }
}

<REGEXPEND> {
  "/"{IdentifierPart}*           {
                                     yybegin(INITIAL);
                                     return JsTokenId.REGEXP_END;
                                 }
  .                              {
                                     yypushback(1);
                                     yybegin(ERROR);
                                 }
}
<ERROR> {
  .*{LineTerminator}             {
                                     yypushback(1);
                                     yybegin(INITIAL);
                                     if (tokenLength - 1 > 0) {
                                         return getErrorToken();
                                     }
                                 }
}

<LCOMMENTEND> {
  {LineTerminator}?              {
                                     yybegin(INITIAL);
                                     if (tokenLength > 0) {
                                         return JsTokenId.EOL;
                                     }
                                 }
}

<JSX> {
   "/>" | "</"{JSXCharacter}+">"       
                                {
                                     Integer balance = jsxBalances.isEmpty() ? 0 : jsxBalances.pop() - 1;
                                     if (balance <= 0) {
                                        yybegin(INITIAL);
                                        return JsTokenId.JSX_TEXT;
                                     } else {
                                        jsxBalances.push(balance);
                                     }
                                }
  "{"                           
                                { 
                                     yypushback(1);
                                     yybegin(JSXEXP);
                                     if (tokenLength - 1 > 0) {
                                         return JsTokenId.JSX_TEXT;
                                     }
                                }

    "<"                         
                                { 
                                    Integer balance = jsxBalances.isEmpty() ? 0 : jsxBalances.pop();
                                    jsxBalances.push(balance+1);
                                }

  {JSXCharacter} | ">" | "/"    
                                { }

  \\.                           { }
}


<JSXEXP> {
  "{"                          {
                                     braceBalances.push(Brace.JSX);
                                     yybegin(INITIAL);
                                     return JsTokenId.JSX_EXP_BEGIN;
                                 }
}

<JSXEXPEND> {
  "}"                           {
                                    yybegin(JSX);
                                    return JsTokenId.JSX_EXP_END;
                                }
}
/* error fallback */
.|\n                             { return getErrorToken(); }
<<EOF>>                          {
    if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return getErrorToken();
    } else {
        return null;
    }
}
