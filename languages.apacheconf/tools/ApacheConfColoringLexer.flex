/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */

package org.netbeans.modules.languages.apacheconf.lexer;

import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.modules.web.common.api.ByteStack;

@org.netbeans.api.annotations.common.SuppressWarnings({"SF_SWITCH_FALLTHROUGH", "URF_UNREAD_FIELD", "DLS_DEAD_LOCAL_STORE", "DM_DEFAULT_ENCODING"})
%%

%public
%class ApacheConfColoringLexer
%type ApacheConfTokenId
%function nextToken
%unicode
%caseless
%char

%eofval{
        if(input.readLength() > 0) {
            // backup eof
            input.backup(1);
            //and return the text as error token
            return ApacheConfTokenId.AC_UNKNOWN;
        } else {
            return null;
        }
%eofval}

%{

    private ByteStack stack = new ByteStack();

    private LexerInput input;

    public ApacheConfColoringLexer(LexerRestartInfo info) {
        this.input = info.input();
        if(info.state() != null) {
            //reset state
            setState((LexerState) info.state());
        } else {
            zzState = zzLexicalState = YYINITIAL;
            stack.clear();
        }

    }

    public static final class LexerState  {
        final ByteStack stack;
        /** the current state of the DFA */
        final int zzState;
        /** the current lexical state */
        final int zzLexicalState;

        LexerState(ByteStack stack, int zzState, int zzLexicalState) {
            this.stack = stack;
            this.zzState = zzState;
            this.zzLexicalState = zzLexicalState;
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
                && (this.zzLexicalState == state.zzLexicalState));
        }

        @Override
        public int hashCode() {
            int hash = 11;
            hash = 31 * hash + this.zzState;
            hash = 31 * hash + this.zzLexicalState;
            if (stack != null) {
                hash = 31 * hash + this.stack.hashCode();
            }
            return hash;
        }
    }

    public LexerState getState() {
        return new LexerState(stack.copyOf(), zzState, zzLexicalState);
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

IDENTIFIER=[[:letter:]_\x7f-\xff][[:letter:][:digit:]_\x7f-\xff]*

WHITESPACE=[ \t]+

NEWLINE=("\r"|"\n"|"\r\n")

ZERO=0
DECIMAL=[\+\-]?[1-9][0-9]*
OCTAL=0[0-7]+
HEXADECIMAL=0[xX][0-9A-Fa-f]+
EXPONENT=[eE][\+\-]?[0-9]+
FLOAT_1=[0-9]+\.[0-9]+{EXPONENT}?
FLOAT_2=\.[0-9]+{EXPONENT}?
FLOAT_3=[0-9]+\.{EXPONENT}?
FLOAT_4=[0-9]+{EXPONENT}
FLOAT=[\+\-]?({FLOAT_1} | {FLOAT_2} | {FLOAT_3} | {FLOAT_4})
NUMBER={ZERO} | {DECIMAL} | {OCTAL} | {HEXADECIMAL} | {FLOAT}

LITERAL=([^+#%\"',=\[\]\{\}\(\)\<\>\t\n\r@ ])+

D_STRING="\""([^"\r""\n""\r\n""\""]|"\\\"")*"\""
S_STRING="'"([^"\r""\n""\r\n""'"]|"\\'")*"'"
STRING = {D_STRING} | {S_STRING}

COMMENT="#"[^"\r""\n""\r\n"]*

VARIABLE="%{"{LITERAL}"}"

FLAG="["([:letter:] | [:digit:] | "=" | ",")+"]"

CLOSE_TAG="</"{IDENTIFIER}">"

TAG_PARAM=[^"\r""\n""\r\n""\t"" ""<"">"]*

DIRECTIVE_PARAM_TOKEN=[^"\r""\n""\r\n""\t"" "]

%state ST_HIGHLIGHTING_ERROR
%state ST_IN_OPEN_TAG
%state ST_IN_DIRECTIVE


%%
<YYINITIAL, ST_IN_OPEN_TAG, ST_IN_DIRECTIVE>{WHITESPACE}+ {
    return ApacheConfTokenId.AC_WHITESPACE;
}

<YYINITIAL> {
    {CLOSE_TAG} {
        return ApacheConfTokenId.AC_TAG;
    }
    "<"{IDENTIFIER} {
        pushState(ST_IN_OPEN_TAG);
        return ApacheConfTokenId.AC_TAG;
    }
    {IDENTIFIER} {
        pushState(ST_IN_DIRECTIVE);
        return ApacheConfTokenId.AC_DIRECTIVE;
    }
    {NEWLINE} {
        return ApacheConfTokenId.AC_WHITESPACE;
    }
    {COMMENT} {
        return ApacheConfTokenId.AC_COMMENT;
    }
}

<ST_IN_OPEN_TAG> {
    ">" {
        popState();
        return ApacheConfTokenId.AC_TAG;
    }
    {TAG_PARAM} {
        return ApacheConfTokenId.AC_TAG_PARAM;
    }
}

<ST_IN_DIRECTIVE> {
    {NUMBER} {
        return ApacheConfTokenId.AC_NUMBER;
    }
    {VARIABLE} {
        return ApacheConfTokenId.AC_VARIABLE;
    }
    {FLAG} {
        return ApacheConfTokenId.AC_FLAG;
    }
    {STRING} {
        return ApacheConfTokenId.AC_STRING;
    }
    {DIRECTIVE_PARAM_TOKEN} {
        return ApacheConfTokenId.AC_DIRECTIVE_PARAM_TOKEN;
    }
    {NEWLINE} {
        popState();
        return ApacheConfTokenId.AC_WHITESPACE;
    }
}

/* ============================================
   Stay in this state until we find a whitespace.
   After we find a whitespace we go the the prev state and try again from the next token.
   ============================================ */
<ST_HIGHLIGHTING_ERROR> {
    {WHITESPACE} {
        popState();
        return ApacheConfTokenId.AC_WHITESPACE;
    }
    . | {NEWLINE} {
        return ApacheConfTokenId.AC_UNKNOWN;
    }
}

/* ============================================
   This rule must be the last in the section!!
   it should contain all the states.
   ============================================ */
<YYINITIAL, ST_IN_OPEN_TAG, ST_IN_DIRECTIVE> {
    . {
        yypushback(yylength());
        pushState(ST_HIGHLIGHTING_ERROR);
    }
    {NEWLINE} {
        yypushback(yylength());
        pushState(ST_HIGHLIGHTING_ERROR);
    }
}
