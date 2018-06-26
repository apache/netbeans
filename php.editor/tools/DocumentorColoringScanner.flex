/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
