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

package org.netbeans.modules.php.twig.editor.lexer;

import java.util.Objects;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.modules.web.common.api.ByteStack;

@org.netbeans.api.annotations.common.SuppressWarnings({"SF_SWITCH_FALLTHROUGH", "URF_UNREAD_FIELD", "DLS_DEAD_LOCAL_STORE", "DM_DEFAULT_ENCODING"})
%%

%public
%class TwigTopColoringLexer
%type TwigTopTokenId
%function findNextToken
%unicode
%caseless
%char

%eofval{
        if(input.readLength() > 0) {
            // backup eof
            input.backup(1);
            //and return the text as error token
            if (zzLexicalState == ST_BLOCK) {
                return TwigTopTokenId.T_TWIG_BLOCK;
            } else if (zzLexicalState == ST_VAR) {
                return TwigTopTokenId.T_TWIG_VAR;
            } else {
                return TwigTopTokenId.T_HTML;
            }
        } else {
            return null;
        }
%eofval}

%{

    private ByteStack stack = new ByteStack();
    private LexerInput input;
    private Lexing lexing;
    private boolean probablyInDString;
    private boolean probablyInSString;
    private int curlyBalance;

    public TwigTopColoringLexer(LexerRestartInfo info) {
        this.input = info.input();
        if(info.state() != null) {
            //reset state
            setState((LexerState) info.state());
            this.lexing = ((LexerState) info.state()).lexing;
            probablyInDString = ((LexerState) info.state()).probablyInDString;
            probablyInSString = ((LexerState) info.state()).probablyInSString;
            curlyBalance = ((LexerState) info.state()).curlyBalance;
        } else {
            zzState = zzLexicalState = YYINITIAL;
            this.lexing = Lexing.NORMAL;
            probablyInDString = false;
            probablyInSString = false;
            curlyBalance = 0;
            stack.clear();
        }

    }

    private enum Lexing {
        NORMAL,
        RAW,
        VERBATIM;
    }

    public static final class LexerState  {
        final ByteStack stack;
        /** the current state of the DFA */
        final int zzState;
        /** the current lexical state */
        final int zzLexicalState;
        private final Lexing lexing;
        private final boolean probablyInDString;
        private final boolean probablyInSString;
        private final int curlyBalance;

        LexerState(ByteStack stack, int zzState, int zzLexicalState, Lexing lexing, boolean probablyInDString, boolean probablyInSString, int curlyBalance) {
            this.stack = stack;
            this.zzState = zzState;
            this.zzLexicalState = zzLexicalState;
            this.lexing = lexing;
            this.probablyInDString = probablyInDString;
            this.probablyInSString = probablyInSString;
            this.curlyBalance = curlyBalance;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 17 * hash + Objects.hashCode(this.stack);
            hash = 17 * hash + this.zzState;
            hash = 17 * hash + this.zzLexicalState;
            hash = 17 * hash + Objects.hashCode(this.lexing);
            hash = 17 * hash + (this.probablyInDString ? 1 : 0);
            hash = 17 * hash + (this.probablyInSString ? 1 : 0);
            hash = 17 * hash + this.curlyBalance;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
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
            if (this.probablyInDString != other.probablyInDString) {
                return false;
            }
            if (this.probablyInSString != other.probablyInSString) {
                return false;
            }
            if (this.curlyBalance != other.curlyBalance) {
                return false;
            }
            if (!Objects.equals(this.stack, other.stack)) {
                return false;
            }
            if (this.lexing != other.lexing) {
                return false;
            }
            return true;
        }
    }

    public LexerState getState() {
        return new LexerState(stack.copyOf(), zzState, zzLexicalState, lexing, probablyInDString, probablyInSString, curlyBalance);
    }

    public void setState(LexerState state) {
        this.stack.copyFrom(state.stack);
        this.zzState = state.zzState;
        this.zzLexicalState = state.zzLexicalState;
        this.lexing = state.lexing;
        this.probablyInDString = state.probablyInDString;
        this.probablyInSString = state.probablyInSString;
        this.curlyBalance = state.curlyBalance;
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
BLOCK_START="{%"
BLOCK_END="%}"
BLOCK_RAW_START="{%"[ \t]*"raw"[ \t]*"%}"
BLOCK_RAW_END="{%"[ \t]*"endraw"[ \t]*"%}"
BLOCK_VERBATIM_START="{%"[ \t]*"verbatim"[ \t]*"%}"
BLOCK_VERBATIM_END="{%"[ \t]*"endverbatim"[ \t]*"%}"
VAR_START="{{"
VAR_END="}}"
COMMENT_START="{#"
COMMENT_END=([^#] | #[^}])*"#}"
D_STRING_DELIM=\"
S_STRING_DELIM='
PRECEDED_STRING="\\\'"|"\\\""|"\\\\"
OPEN_CURLY="{"
CLOSE_CURLY="}"

%state ST_RAW_START
%state ST_RAW_END
%state ST_VERBATIM_START
%state ST_VERBATIM_END
%state ST_BLOCK
%state ST_VAR
%state ST_COMMENT
%state ST_HIGHLIGHTING_ERROR

%%

<YYINITIAL, ST_RAW_START, ST_RAW_END, ST_VERBATIM_START, ST_VERBATIM_END, ST_BLOCK, ST_VAR, ST_COMMENT>{WHITESPACE}+ {
}

<YYINITIAL> {
    {BLOCK_RAW_START} {
        if (lexing == Lexing.NORMAL) {
            yypushback(yylength());
            pushState(ST_RAW_START);
        }
    }
    {BLOCK_RAW_END} {
        if (lexing != Lexing.VERBATIM) {
            int indexOfRawBlockStart = yytext().lastIndexOf("{%"); //NOI18N
            yypushback(yylength() - indexOfRawBlockStart);
            pushState(ST_RAW_END);
        }
    }
    {BLOCK_VERBATIM_START} {
        if (lexing == Lexing.NORMAL) {
            yypushback(yylength());
            pushState(ST_VERBATIM_START);
        }
    }
    {BLOCK_VERBATIM_END} {
        if (lexing != Lexing.RAW) {
            int indexOfVerbatimBlockStart = yytext().lastIndexOf("{%"); //NOI18N
            yypushback(yylength() - indexOfVerbatimBlockStart);
            pushState(ST_VERBATIM_END);
        }
    }
    {BLOCK_START} {
        if (lexing == Lexing.NORMAL) {
            if (yylength() > 2) {
                yypushback(2);
                return TwigTopTokenId.T_HTML;
            }
            pushState(ST_BLOCK);
            return TwigTopTokenId.T_TWIG_BLOCK_START;
        }
    }
    {COMMENT_START} {
        if (lexing == Lexing.NORMAL) {
            int textLength = yylength();
            yypushback(2);
            pushState(ST_COMMENT);
            if (textLength > 2) {
                return TwigTopTokenId.T_HTML;
            }
        }
    }
    {VAR_START} {
        if (lexing == Lexing.NORMAL) {
            if (yylength() > 2) {
                yypushback(2);
                return TwigTopTokenId.T_HTML;
            }
            pushState(ST_VAR);
            curlyBalance = 0;
            return TwigTopTokenId.T_TWIG_VAR_START;
        }
    }
    . {}
}

<ST_RAW_START> {
    {BLOCK_START} {
        if (yylength() > 2) {
            yypushback(2);
            return TwigTopTokenId.T_HTML;
        }
        lexing = Lexing.RAW;
        return TwigTopTokenId.T_TWIG_BLOCK_START;
    }
}

<ST_VERBATIM_START> {
    {BLOCK_START} {
        if (yylength() > 2) {
            yypushback(2);
            return TwigTopTokenId.T_HTML;
        }
        lexing = Lexing.VERBATIM;
        return TwigTopTokenId.T_TWIG_BLOCK_START;
    }
}

<ST_RAW_START, ST_VERBATIM_START> {
    {BLOCK_END} {
        if (yylength() > 2) {
            yypushback(2);
            return TwigTopTokenId.T_TWIG_BLOCK;
        }
        popState();
        return TwigTopTokenId.T_TWIG_BLOCK_END;
    }
    . {}
}

<ST_RAW_END, ST_VERBATIM_END> {
    {BLOCK_START} {
        if (yylength() > 2) {
            yypushback(2);
            return TwigTopTokenId.T_HTML;
        }
        lexing = Lexing.NORMAL;
        return TwigTopTokenId.T_TWIG_BLOCK_START;
    }
    {BLOCK_END} {
        if (yylength() > 2) {
            yypushback(2);
            return TwigTopTokenId.T_TWIG_BLOCK;
        }
        popState();
        return TwigTopTokenId.T_TWIG_BLOCK_END;
    }
    . {}
}

<ST_COMMENT> {
    {COMMENT_END} {
        popState();
        return TwigTopTokenId.T_TWIG_COMMENT;
    }
    . {}
}

<ST_BLOCK> {
    {BLOCK_END} {
        if (yylength() > 2) {
            yypushback(2);
            return TwigTopTokenId.T_TWIG_BLOCK;
        }
        popState();
        return TwigTopTokenId.T_TWIG_BLOCK_END;
    }
    . {}
}

<ST_VAR> {
    {PRECEDED_STRING} {
    }
    {D_STRING_DELIM} {
        if (!probablyInSString) {
            probablyInDString = !probablyInDString;
        }
    }
    {S_STRING_DELIM} {
        if (!probablyInDString) {
            probablyInSString = !probablyInSString;
        }
    }
    {OPEN_CURLY} {
        if (!probablyInDString && !probablyInSString) {
            curlyBalance++;
        }
    }
    {CLOSE_CURLY} {
        if (!probablyInDString && !probablyInSString) {
            curlyBalance--;
        }
    }
    "}}}" { // {{{}}}
        if (!probablyInDString && !probablyInSString) {
            if (curlyBalance >= 0 && curlyBalance <= 2) {
                curlyBalance--;
                yypushback(2);
            } else {
                curlyBalance -= 3;
            }
        }
    }
    {VAR_END} {
        if (!probablyInDString && !probablyInSString) {
            if (yylength() > 2) {
                if (curlyBalance == 0 || curlyBalance == 1) {
                    if (zzInput == YYEOF) {
                        yypushback(3);
                    } else {
                        yypushback(2);
                    }
                    return TwigTopTokenId.T_TWIG_VAR;
                }
            }
            if (curlyBalance == 0) {
                popState();
                return TwigTopTokenId.T_TWIG_VAR_END;
            } else if (curlyBalance == 1) {
                // missing closing curly "}"
                popState();
                curlyBalance = 0;
                return TwigTopTokenId.T_TWIG_VAR_END;
            } else {
                curlyBalance -= 2;
            }
        }
    }
    . {}
}

/* ============================================
   Stay in this state until we find a whitespace.
   After we find a whitespace we go the the prev state and try again from the next token.
   ============================================ */
<ST_HIGHLIGHTING_ERROR> {
    . {
        return TwigTopTokenId.T_TWIG_OTHER;
    }
}

/* ============================================
   This rule must be the last in the section!!
   it should contain all the states.
   ============================================ */
<YYINITIAL, ST_RAW_START, ST_RAW_END, ST_VERBATIM_START, ST_VERBATIM_END, ST_BLOCK, ST_VAR, ST_COMMENT> {
    . {
        yypushback(yylength());
        pushState(ST_HIGHLIGHTING_ERROR);
    }
}
