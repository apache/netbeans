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

import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

@org.netbeans.api.annotations.common.SuppressWarnings({"URF_UNREAD_FIELD", "DLS_DEAD_LOCAL_STORE", "DM_DEFAULT_ENCODING"})
%%

%public
%final
%class JsonColoringLexer
%type JsTokenId
%unicode
%char

%{
    private LexerInput input;

    private boolean embedded;

    public JsonColoringLexer(LexerRestartInfo info) {
        this.input = info.input();

        this.embedded = !JsTokenId.JSON_MIME_TYPE.equals(info.languagePath().mimePath());
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

    public JsTokenId nextToken() throws java.io.IOException {
        return yylex();
    }

    private JsTokenId getErrorToken() {
        if (embedded) {
            return JsTokenId.UNKNOWN;
        }
        return JsTokenId.ERROR;
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

/* main character classes */
LineTerminator = \r|\n|\r\n

WhiteSpace = [ \t\f\u00A0\u000B]+

OctDigit          = [0-7]

/* number literals */
NumberLiteral = [0-9]+({Fraction}|{Exponent}|{Fraction}{Exponent})?

Fraction = \. [0-9]*
Exponent = [eE] [+-]? [0-9]+

/* string and character literals */
StringCharacter  = [^\r\n\"\\]

%state STRING
%state STRINGEND
%state ERROR

%%

<YYINITIAL> {

  /* boolean literals */
  "true"                         { return JsTokenId.KEYWORD_TRUE; }
  "false"                        { return JsTokenId.KEYWORD_FALSE; }

  /* null literal */
  "null"                         { return JsTokenId.KEYWORD_NULL; }

  /* operators */
  "{"                            { return JsTokenId.BRACKET_LEFT_CURLY; }
  "}"                            { return JsTokenId.BRACKET_RIGHT_CURLY; }
  "["                            { return JsTokenId.BRACKET_LEFT_BRACKET; }
  "]"                            { return JsTokenId.BRACKET_RIGHT_BRACKET; }
  ","                            { return JsTokenId.OPERATOR_COMMA; }
  ":"                            { return JsTokenId.OPERATOR_COLON; }
  "-"                            { return JsTokenId.OPERATOR_MINUS; }
  
  /* string literal */
  \"                             {
                                    yybegin(STRING);
                                    return JsTokenId.STRING_BEGIN;
                                 }

  /* numeric literals */
  {NumberLiteral}                { return JsTokenId.NUMBER; }

  /* whitespace */
  {WhiteSpace}                   { return JsTokenId.WHITESPACE; }

  /* whitespace */
  {LineTerminator}               { return JsTokenId.EOL; }

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
                                     yybegin(YYINITIAL);
                                     if (tokenLength - 1 > 0) {
                                         return getErrorToken();
                                     }
                                 }
}

<STRINGEND> {
  \"                             {
                                     yybegin(YYINITIAL);
                                     return JsTokenId.STRING_END;
                                 }
}


<ERROR> {
  .*{LineTerminator}             {
                                     yypushback(1);
                                     yybegin(YYINITIAL);
                                     if (tokenLength - 1 > 0) {
                                         return getErrorToken();
                                     }
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
