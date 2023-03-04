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

import org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

@org.netbeans.api.annotations.common.SuppressWarnings({"SF_SWITCH_FALLTHROUGH", "URF_UNREAD_FIELD", "DLS_DEAD_LOCAL_STORE", "DM_DEFAULT_ENCODING"})
%%

%public
%final
%class JsDocumentationColoringLexer
%type JsDocumentationTokenId
%unicode
%caseless
%char

%{
    private LexerInput input;

    public JsDocumentationColoringLexer(LexerRestartInfo info) {
        this.input = info.input();

        if(info.state() != null) {
            //reset state
            setState((LexerState)info.state());
        } else {
            //initial state
            zzState = zzLexicalState = YYINITIAL;
        }
    }

    public LexerState getState() {
        if (zzState == YYINITIAL && zzLexicalState == YYINITIAL) {
            return null;
        }
        return new LexerState(zzState, zzLexicalState);
    }

    public void setState(LexerState state) {
        this.zzState = state.zzState;
        this.zzLexicalState = state.zzLexicalState;
    }

    public JsDocumentationTokenId nextToken() throws java.io.IOException {
        JsDocumentationTokenId token = yylex();
        return token;
    }

    public static final class LexerState  {
        /** the current state of the DFA */
        final int zzState;
        /** the current lexical state */
        final int zzLexicalState;

        LexerState (int zzState, int zzLexicalState) {
            this.zzState = zzState;
            this.zzLexicalState = zzLexicalState;
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
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 29 * hash + this.zzState;
            hash = 29 * hash + this.zzLexicalState;
            return hash;
        }

        @Override
        public String toString() {
            return "LexerState{" + "zzState=" + zzState + ", zzLexicalState=" + zzLexicalState + '}';
        }
    }

 // End user code

%}

/* states */
%state DOCBLOCK
%state DOCBLOCK_START
%state AT
%state STRING
%state STRINGEND

/* base structural elements */
AnyChar = (.|[\n])
HtmlString = (["<"]("/"|[:letter:])+) [^"\r"|"\n"|"\r\n"|">"|"*"|"<"]* [>]
Identifier=[[:letter:][:digit:]]+
LineTerminator = \r|\n|\r\n
StringCharacter  = [^\r\n\"\\] | \\{LineTerminator}
WhiteSpace = [ \t\f\u00A0\u000B]+

/* comment types */
CommentStart = "/*"
CommentEnd = ["*"]+ + "/"


%%

<YYINITIAL> {

    {CommentStart}                  { yybegin(DOCBLOCK_START); }
    {CommentEnd}                    { return JsDocumentationTokenId.COMMENT_END; }
    {AnyChar}                       { }
}

<DOCBLOCK_START> {
    "*"                             { yybegin(DOCBLOCK); return JsDocumentationTokenId.COMMENT_DOC_START; }
    {AnyChar}                       { yypushback(1); yybegin(DOCBLOCK); return JsDocumentationTokenId.COMMENT_BLOCK_START; }
}

<DOCBLOCK> {
    {CommentEnd}                    { return JsDocumentationTokenId.COMMENT_END; }
    {WhiteSpace}                    { return JsDocumentationTokenId.WHITESPACE; }
    {LineTerminator}                { return JsDocumentationTokenId.EOL; }
    {HtmlString}                    { return JsDocumentationTokenId.HTML; }

    "@"                             { yybegin(AT); yypushback(1); }
    "<"                             { return JsDocumentationTokenId.OTHER; }
    "*"                             { return JsDocumentationTokenId.ASTERISK; }
    ","                             { return JsDocumentationTokenId.COMMA; }
    "{"                             { return JsDocumentationTokenId.BRACKET_LEFT_CURLY; }
    "}"                             { return JsDocumentationTokenId.BRACKET_RIGHT_CURLY; }
    "["                             { return JsDocumentationTokenId.BRACKET_LEFT_BRACKET; }
    "]"                             { return JsDocumentationTokenId.BRACKET_RIGHT_BRACKET; }

    "\""                            { yybegin(STRING); return JsDocumentationTokenId.STRING_BEGIN; }

    ~({WhiteSpace}
        | {LineTerminator}
        | "*" | "@" | "<" | "{"
        | "}" | "\"" | "," | "["
        | "]")                      { yypushback(1); return JsDocumentationTokenId.OTHER; }
}

<STRING> {
    \"                              { yypushback(1); yybegin(STRINGEND);
                                        if (tokenLength - 1 > 0) {
                                            return JsDocumentationTokenId.STRING;
                                        }
                                    }

    {StringCharacter}+              { }

    /* escape sequences */
    \\.                             { }
    {LineTerminator}                { yypushback(1); yybegin(DOCBLOCK);
                                        if (tokenLength - 1 > 0) {
                                            return JsDocumentationTokenId.UNKNOWN;
                                        }
                                    }
}

<STRINGEND> {
    \"                              { yybegin(DOCBLOCK); return JsDocumentationTokenId.STRING_END; }
}

<AT> {
    "@"{Identifier}                 { yybegin(DOCBLOCK); return JsDocumentationTokenId.KEYWORD; }
    {AnyChar}                       { yybegin(DOCBLOCK); return JsDocumentationTokenId.AT; }
}

<<EOF>> {
    if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return JsDocumentationTokenId.UNKNOWN;
    } else {
        return null;
    }
}