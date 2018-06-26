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

package org.netbeans.modules.php.twig.editor.lexer;

import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.modules.web.common.api.ByteStack;

@org.netbeans.api.annotations.common.SuppressWarnings({"SF_SWITCH_FALLTHROUGH", "URF_UNREAD_FIELD", "DLS_DEAD_LOCAL_STORE", "DM_DEFAULT_ENCODING"})
%%

%public
%class TwigBlockColoringLexer
%type TwigBlockTokenId
%function findNextToken
%unicode
%caseless
%char

%eofval{
        if(input.readLength() > 0) {
            // backup eof
            input.backup(1);
            //and return the text as error token
            return TwigBlockTokenId.T_TWIG_OTHER;
        } else {
            return null;
        }
%eofval}

%{

    private ByteStack stack = new ByteStack();
    private LexerInput input;

    public TwigBlockColoringLexer(LexerRestartInfo info) {
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

WHITESPACE=[ \t\r\n]+
OPERATOR=("as"|"="|"not"|"+"|"-"|"or"|"b-or"|"b-xor"|"and"|"b-and"|"=="|"!="|">"|"<"|">="|"<="|"in"|"~"|"*"|"/"|"//"|"%"|"is"|".."|"**")
OPEN_CURLY="{"
PUNCTUATION=("|"|"("|")"|"["|"]"|{OPEN_CURLY}|"}"|"?"|":"|"."|",")
NUMBER=[0-9]+(\.[0-9]+)?
NAME=[a-zA-Z_\x7f-\xff][a-zA-Z0-9_\x7f-\xff]*
D_STRING="\""([^"\"""\\"]|"\\"[^])*"\\"?"\""
S_STRING="'"([^"'"\\]|"\\"[^])*"\\"?"'"
TAG=("autoescape"|"endautoescape"|"block"|"endblock"|"do"|"embed"|"endembed"|"extends"|"filter"|"endfilter"|"flush"|"for"|"endfor"|"from"|"if"|"else"|"elseif"|"endif"|"import"|"include"|"macro"|"endmacro"|"raw"|"endraw"|"sandbox"|"endsandbox"|"set"|"endset"|"spaceless"|"endspaceless"|"use"|"verbatim"|"endverbatim")
INTERPOLATION_START="#{"
INTERPOLATION_END="}"
D_NO_INTERPOLATION=([^"#""\"""\\"] | #[^"{""\""]|"\\"[^])*"\\"?
D_INTERPOLATION={D_NO_INTERPOLATION} {INTERPOLATION_START}
D_PRE_INTERPOLATION="\"" {D_INTERPOLATION}
D_NO_INTERPOLATION_INSIDE="\"" {D_NO_INTERPOLATION} "#"* "\""
D_POST_INTERPOLATION={D_NO_INTERPOLATION} "\""

%state ST_BLOCK
%state ST_D_STRING
%state ST_S_STRING
%state ST_INTERPOLATION
%state ST_HIGHLIGHTING_ERROR

%%
<YYINITIAL, ST_BLOCK, ST_D_STRING, ST_S_STRING, ST_INTERPOLATION>{WHITESPACE}+ {
    return TwigBlockTokenId.T_TWIG_WHITESPACE;
}

<ST_INTERPOLATION> {
    {INTERPOLATION_START} {
        return TwigBlockTokenId.T_TWIG_INTERPOLATION_START;
    }
    {INTERPOLATION_END} {
        popState();
        return TwigBlockTokenId.T_TWIG_INTERPOLATION_END;
    }
}

<YYINITIAL> {
    {TAG} {
        pushState(ST_BLOCK);
        return TwigBlockTokenId.T_TWIG_TAG;
    }
    {OPERATOR} {
        return TwigBlockTokenId.T_TWIG_OPERATOR;
    }
    {PUNCTUATION} {
        pushState(ST_BLOCK);
        return TwigBlockTokenId.T_TWIG_PUNCTUATION;
    }
    {NUMBER} {
        pushState(ST_BLOCK);
        return TwigBlockTokenId.T_TWIG_NUMBER;
    }
    {D_STRING} {
        yypushback(yylength());
        pushState(ST_BLOCK);
    }
    {S_STRING} {
        pushState(ST_BLOCK);
        return TwigBlockTokenId.T_TWIG_STRING;
    }
    {NAME} {
        pushState(ST_BLOCK);
        return TwigBlockTokenId.T_TWIG_NAME;
    }
}

<ST_BLOCK> {
    {OPERATOR} {
        return TwigBlockTokenId.T_TWIG_OPERATOR;
    }
    {NAME} {
        return TwigBlockTokenId.T_TWIG_NAME;
    }
}

<ST_BLOCK, ST_INTERPOLATION> {
    {PUNCTUATION} {
        return TwigBlockTokenId.T_TWIG_PUNCTUATION;
    }
    {NUMBER} {
        return TwigBlockTokenId.T_TWIG_NUMBER;
    }
    {D_STRING} {
        yypushback(yylength());
        pushState(ST_D_STRING);
    }
    {S_STRING} {
        return TwigBlockTokenId.T_TWIG_STRING;
    }
    {NAME} {
        return TwigBlockTokenId.T_TWIG_NAME;
    }
}

<ST_D_STRING> {
    {D_PRE_INTERPOLATION} | {D_INTERPOLATION} {
        yypushback(2);
        pushState(ST_INTERPOLATION);
        return TwigBlockTokenId.T_TWIG_STRING;
    }
    {D_NO_INTERPOLATION_INSIDE} {
        popState();
        return TwigBlockTokenId.T_TWIG_STRING;
    }
    {D_POST_INTERPOLATION} {
        popState();
        return TwigBlockTokenId.T_TWIG_STRING;
    }
}


/* ============================================
   Stay in this state until we find a whitespace.
   After we find a whitespace we go the the prev state and try again from the next token.
   ============================================ */
<ST_HIGHLIGHTING_ERROR> {
    {WHITESPACE} {
        popState();
        return TwigBlockTokenId.T_TWIG_WHITESPACE;
    }
    . {
        return TwigBlockTokenId.T_TWIG_OTHER;
    }
}

/* ============================================
   This rule must be the last in the section!!
   it should contain all the states.
   ============================================ */
<YYINITIAL, ST_BLOCK, ST_D_STRING, ST_S_STRING, ST_INTERPOLATION> {
    . {
        yypushback(yylength());
        pushState(ST_HIGHLIGHTING_ERROR);
    }
}
