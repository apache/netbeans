/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
