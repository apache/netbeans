/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.java.api.common.project.ui.customizer.vmo.gen;
import java.util.Queue;
import java.util.LinkedList;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class CommandLineLexer extends Lexer {
    public static final int EOF=-1;
    public static final int T__7=7;
    public static final int T__8=8;
    public static final int T__9=9;
    public static final int T__10=10;
    public static final int WS=4;
    public static final int TEXT=5;
    public static final int LETTER=6;


        private final Queue<Token> tokens = new LinkedList<Token>();

        @Override
        public void recover(final RecognitionException re) {	
            input.seek(state.tokenStartCharIndex);
            input.setLine(state.tokenStartLine);
            input.setCharPositionInLine(state.tokenStartCharPositionInLine);
            state.type = TEXT;
            state.token = null;
            state.channel = Token.DEFAULT_CHANNEL;
            state.tokenStartCharIndex = input.index();
            state.tokenStartCharPositionInLine = input.getCharPositionInLine();
            state.tokenStartLine = input.getLine();
            state.text = null;
            //read upto white space and emmit as TEXT, todo: specail ERROR token should be better
            while (!((input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' '||input.LA(1) == EOF)) {                
                input.consume();
            }
            tokens.add(emit());
        }
        
        @Override
        public Token nextToken() {
        	tokens.add(super.nextToken());
        	return tokens.poll();
        }


    // delegates
    // delegators

    public CommandLineLexer() {;} 
    public CommandLineLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public CommandLineLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g"; }

    // $ANTLR start "T__7"
    public final void mT__7() throws RecognitionException {
        try {
            int _type = T__7;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:42:6: ( '-' )
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:42:8: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__7"

    // $ANTLR start "T__8"
    public final void mT__8() throws RecognitionException {
        try {
            int _type = T__8;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:43:6: ( '=' )
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:43:8: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__8"

    // $ANTLR start "T__9"
    public final void mT__9() throws RecognitionException {
        try {
            int _type = T__9;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:44:6: ( '\\'' )
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:44:8: '\\''
            {
            match('\''); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__9"

    // $ANTLR start "T__10"
    public final void mT__10() throws RecognitionException {
        try {
            int _type = T__10;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:45:7: ( '\"' )
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:45:9: '\"'
            {
            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__10"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:282:4: ( ( ' ' | '\\r' | '\\t' | '\\u000C' | '\\n' )+ )
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:282:6: ( ' ' | '\\r' | '\\t' | '\\u000C' | '\\n' )+
            {
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:282:6: ( ' ' | '\\r' | '\\t' | '\\u000C' | '\\n' )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='\t' && LA1_0<='\n')||(LA1_0>='\f' && LA1_0<='\r')||LA1_0==' ') ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt1 >= 1 ) break loop1;
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "TEXT"
    public final void mTEXT() throws RecognitionException {
        try {
            int _type = TEXT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:284:6: ( LETTER ( LETTER | '-' | ';' | ':' | '{' | '}' )* )
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:284:8: LETTER ( LETTER | '-' | ';' | ':' | '{' | '}' )*
            {
            mLETTER(); 
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:284:15: ( LETTER | '-' | ';' | ':' | '{' | '}' )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0=='!'||(LA2_0>='#' && LA2_0<='&')||LA2_0=='+'||(LA2_0>='-' && LA2_0<=';')||(LA2_0>='A' && LA2_0<='Z')||LA2_0=='\\'||LA2_0=='_'||(LA2_0>='a' && LA2_0<='{')||(LA2_0>='}' && LA2_0<='~')||(LA2_0>='\u00C0' && LA2_0<='\u00D6')||(LA2_0>='\u00D8' && LA2_0<='\u00F6')||(LA2_0>='\u00F8' && LA2_0<='\u1FFF')||(LA2_0>='\u3040' && LA2_0<='\u318F')||(LA2_0>='\u3300' && LA2_0<='\u337F')||(LA2_0>='\u3400' && LA2_0<='\u3D2D')||(LA2_0>='\u4E00' && LA2_0<='\u9FFF')||(LA2_0>='\uF900' && LA2_0<='\uFAFF')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:
            	    {
            	    if ( input.LA(1)=='!'||(input.LA(1)>='#' && input.LA(1)<='&')||input.LA(1)=='+'||(input.LA(1)>='-' && input.LA(1)<=';')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='\\'||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='{')||(input.LA(1)>='}' && input.LA(1)<='~')||(input.LA(1)>='\u00C0' && input.LA(1)<='\u00D6')||(input.LA(1)>='\u00D8' && input.LA(1)<='\u00F6')||(input.LA(1)>='\u00F8' && input.LA(1)<='\u1FFF')||(input.LA(1)>='\u3040' && input.LA(1)<='\u318F')||(input.LA(1)>='\u3300' && input.LA(1)<='\u337F')||(input.LA(1)>='\u3400' && input.LA(1)<='\u3D2D')||(input.LA(1)>='\u4E00' && input.LA(1)<='\u9FFF')||(input.LA(1)>='\uF900' && input.LA(1)<='\uFAFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TEXT"

    // $ANTLR start "LETTER"
    public final void mLETTER() throws RecognitionException {
        try {
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:288:5: ( '\\u0021' | '\\u0023' .. '\\u0026' | '\\u002b' | '\\u002e' .. '\\u0039' | '\\u0041' .. '\\u005a' | '\\u005c' | '\\u005f' | '\\u0061' .. '\\u007a' | '\\u007e' | '\\u00c0' .. '\\u00d6' | '\\u00d8' .. '\\u00f6' | '\\u00f8' .. '\\u00ff' | '\\u0100' .. '\\u1fff' | '\\u3040' .. '\\u318f' | '\\u3300' .. '\\u337f' | '\\u3400' .. '\\u3d2d' | '\\u4e00' .. '\\u9fff' | '\\uf900' .. '\\ufaff' )
            // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:
            {
            if ( input.LA(1)=='!'||(input.LA(1)>='#' && input.LA(1)<='&')||input.LA(1)=='+'||(input.LA(1)>='.' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='\\'||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z')||input.LA(1)=='~'||(input.LA(1)>='\u00C0' && input.LA(1)<='\u00D6')||(input.LA(1)>='\u00D8' && input.LA(1)<='\u00F6')||(input.LA(1)>='\u00F8' && input.LA(1)<='\u1FFF')||(input.LA(1)>='\u3040' && input.LA(1)<='\u318F')||(input.LA(1)>='\u3300' && input.LA(1)<='\u337F')||(input.LA(1)>='\u3400' && input.LA(1)<='\u3D2D')||(input.LA(1)>='\u4E00' && input.LA(1)<='\u9FFF')||(input.LA(1)>='\uF900' && input.LA(1)<='\uFAFF') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "LETTER"

    public void mTokens() throws RecognitionException {
        // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:8: ( T__7 | T__8 | T__9 | T__10 | WS | TEXT )
        int alt3=6;
        int LA3_0 = input.LA(1);

        if ( (LA3_0=='-') ) {
            alt3=1;
        }
        else if ( (LA3_0=='=') ) {
            alt3=2;
        }
        else if ( (LA3_0=='\'') ) {
            alt3=3;
        }
        else if ( (LA3_0=='\"') ) {
            alt3=4;
        }
        else if ( ((LA3_0>='\t' && LA3_0<='\n')||(LA3_0>='\f' && LA3_0<='\r')||LA3_0==' ') ) {
            alt3=5;
        }
        else if ( (LA3_0=='!'||(LA3_0>='#' && LA3_0<='&')||LA3_0=='+'||(LA3_0>='.' && LA3_0<='9')||(LA3_0>='A' && LA3_0<='Z')||LA3_0=='\\'||LA3_0=='_'||(LA3_0>='a' && LA3_0<='z')||LA3_0=='~'||(LA3_0>='\u00C0' && LA3_0<='\u00D6')||(LA3_0>='\u00D8' && LA3_0<='\u00F6')||(LA3_0>='\u00F8' && LA3_0<='\u1FFF')||(LA3_0>='\u3040' && LA3_0<='\u318F')||(LA3_0>='\u3300' && LA3_0<='\u337F')||(LA3_0>='\u3400' && LA3_0<='\u3D2D')||(LA3_0>='\u4E00' && LA3_0<='\u9FFF')||(LA3_0>='\uF900' && LA3_0<='\uFAFF')) ) {
            alt3=6;
        }
        else {
            NoViableAltException nvae =
                new NoViableAltException("", 3, 0, input);

            throw nvae;
        }
        switch (alt3) {
            case 1 :
                // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:10: T__7
                {
                mT__7(); 

                }
                break;
            case 2 :
                // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:15: T__8
                {
                mT__8(); 

                }
                break;
            case 3 :
                // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:20: T__9
                {
                mT__9(); 

                }
                break;
            case 4 :
                // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:25: T__10
                {
                mT__10(); 

                }
                break;
            case 5 :
                // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:31: WS
                {
                mWS(); 

                }
                break;
            case 6 :
                // /Users/tom/Projects/netbeans/jet-main/java.j2seproject/src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:34: TEXT
                {
                mTEXT(); 

                }
                break;

        }

    }


 

}