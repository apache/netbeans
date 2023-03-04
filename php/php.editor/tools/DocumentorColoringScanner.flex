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

package org.netbeans.modules.php.editor.lexer;

import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

@org.netbeans.api.annotations.common.SuppressWarnings({"SF_SWITCH_FALLTHROUGH", "URF_UNREAD_FIELD", "DLS_DEAD_LOCAL_STORE", "DM_DEFAULT_ENCODING"})
%%

%public
%class DocumentorColoringScanner
%type PHPDocCommentTokenId
%function nextToken
%unicode
%caseless
%char




%state ST_IN_TAG
%state ST_NO_TAG
%state ST_HTML_TAG

%eofval{
          if(input.readLength() > 0) {
              return PHPDocCommentTokenId.PHPDOC_COMMENT;
          }
          else {
              return null;
          }
%eofval}

%{
        private LexerInput input;

        DocumentorColoringScanner (LexerRestartInfo info) {
            this.input = info.input();

            if(info.state() != null) {
                //reset state
                setState((LexerState)info.state());
            } else {
                //initial state
                zzState = zzLexicalState = YYINITIAL;
            }
       }


        public int getTokenLength() {
            return yylength();
        }

        public class LexerState  {
            /** the current state of the DFA */
            final int zzState;
            /** the current lexical state */
            final int zzLexicalState;

            LexerState () {
                zzState =  DocumentorColoringScanner.this.zzState;
                zzLexicalState = DocumentorColoringScanner.this.zzLexicalState;
            }

        }

        public LexerState getState() {
            return new LexerState();
        }

        public void setState(LexerState state) {
            this.zzState = state.zzState;
            this.zzLexicalState = state.zzLexicalState;
        }

   // End user code

%}

ANY_CHAR=(.|[\n])
IDENTIFIER=[[:letter:][:digit:]_\\-]+
HTML_TAG="<"[^"\r""\n""\r\n"">"]+">"




%%

<YYINITIAL> {
    "@" {
        yybegin(ST_IN_TAG);
        yypushback(1);
    }
    "<" {
        yybegin(ST_HTML_TAG);
        yypushback(1);
    }
    [^@<]* {
        return PHPDocCommentTokenId.PHPDOC_COMMENT;
    }
}

<ST_HTML_TAG> {
    {HTML_TAG} {
        yybegin(YYINITIAL);
        return PHPDocCommentTokenId.PHPDOC_HTML_TAG;
    }
    {ANY_CHAR} {
        yybegin(YYINITIAL);
        return PHPDocCommentTokenId.PHPDOC_COMMENT;
    }
}

<ST_IN_TAG> {
    "@"{IDENTIFIER}  {yybegin(YYINITIAL); return PHPDocCommentTokenId.PHPDOC_ANNOTATION;}
    {ANY_CHAR}       {yybegin(ST_NO_TAG); yypushback(1);}
}

<ST_NO_TAG> "@"[^@]* {
    yybegin(YYINITIAL);
    return PHPDocCommentTokenId.PHPDOC_COMMENT;
}
