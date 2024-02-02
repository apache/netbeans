// $ANTLR 3.5.2 /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2023-09-14 19:44:33

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.css.lib;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@SuppressWarnings("all")
public class Css3Lexer extends Lexer {
	public static final int EOF=-1;
	public static final int A=4;
	public static final int ANGLE=5;
	public static final int AT_IDENT=6;
	public static final int AT_SIGN=7;
	public static final int B=8;
	public static final int BEGINS=9;
	public static final int BOTTOMCENTER_SYM=10;
	public static final int BOTTOMLEFTCORNER_SYM=11;
	public static final int BOTTOMLEFT_SYM=12;
	public static final int BOTTOMRIGHTCORNER_SYM=13;
	public static final int BOTTOMRIGHT_SYM=14;
	public static final int C=15;
	public static final int CDC=16;
	public static final int CDO=17;
	public static final int CHARSET_SYM=18;
	public static final int COLON=19;
	public static final int COMMA=20;
	public static final int COMMENT=21;
	public static final int CONTAINER_SYM=22;
	public static final int CONTAINS=23;
	public static final int COUNTER_STYLE_SYM=24;
	public static final int CP_DOTS=25;
	public static final int CP_EQ=26;
	public static final int CP_NOT_EQ=27;
	public static final int D=28;
	public static final int DASHMATCH=29;
	public static final int DCOLON=30;
	public static final int DIMENSION=31;
	public static final int DOT=32;
	public static final int E=33;
	public static final int EMS=34;
	public static final int ENDS=35;
	public static final int ESCAPE=36;
	public static final int EXCLAMATION_MARK=37;
	public static final int EXS=38;
	public static final int F=39;
	public static final int FONT_FACE_SYM=40;
	public static final int FREQ=41;
	public static final int G=42;
	public static final int GEN=43;
	public static final int GREATER=44;
	public static final int GREATER_OR_EQ=45;
	public static final int H=46;
	public static final int HASH=47;
	public static final int HASH_SYMBOL=48;
	public static final int HEXCHAR=49;
	public static final int HEXCHAR_WILDCARD=50;
	public static final int I=51;
	public static final int IDENT=52;
	public static final int IMPORTANT_SYM=53;
	public static final int IMPORT_SYM=54;
	public static final int INCLUDES=55;
	public static final int INVALID=56;
	public static final int J=57;
	public static final int K=58;
	public static final int L=59;
	public static final int LAYER_SYM=60;
	public static final int LBRACE=61;
	public static final int LBRACKET=62;
	public static final int LEFTBOTTOM_SYM=63;
	public static final int LEFTMIDDLE_SYM=64;
	public static final int LEFTTOP_SYM=65;
	public static final int LENGTH=66;
	public static final int LESS=67;
	public static final int LESS_AND=68;
	public static final int LESS_JS_STRING=69;
	public static final int LESS_OR_EQ=70;
	public static final int LESS_REST=71;
	public static final int LINE_COMMENT=72;
	public static final int LPAREN=73;
	public static final int M=74;
	public static final int MEDIA_SYM=75;
	public static final int MINUS=76;
	public static final int MOZ_DOCUMENT_SYM=77;
	public static final int MOZ_DOMAIN=78;
	public static final int MOZ_REGEXP=79;
	public static final int MOZ_URL_PREFIX=80;
	public static final int N=81;
	public static final int NAME=82;
	public static final int NAMESPACE_SYM=83;
	public static final int NL=84;
	public static final int NMCHAR=85;
	public static final int NMSTART=86;
	public static final int NONASCII=87;
	public static final int NOT=88;
	public static final int NUMBER=89;
	public static final int O=90;
	public static final int OPEQ=91;
	public static final int P=92;
	public static final int PAGE_SYM=93;
	public static final int PERCENTAGE=94;
	public static final int PERCENTAGE_SYMBOL=95;
	public static final int PIPE=96;
	public static final int PLUS=97;
	public static final int Q=98;
	public static final int R=99;
	public static final int RBRACE=100;
	public static final int RBRACKET=101;
	public static final int REM=102;
	public static final int RESOLUTION=103;
	public static final int RIGHTBOTTOM_SYM=104;
	public static final int RIGHTMIDDLE_SYM=105;
	public static final int RIGHTTOP_SYM=106;
	public static final int RPAREN=107;
	public static final int S=108;
	public static final int SASS_AT_ROOT=109;
	public static final int SASS_CONTENT=110;
	public static final int SASS_DEBUG=111;
	public static final int SASS_DEFAULT=112;
	public static final int SASS_EACH=113;
	public static final int SASS_ELSE=114;
	public static final int SASS_ELSEIF=115;
	public static final int SASS_ERROR=116;
	public static final int SASS_EXTEND=117;
	public static final int SASS_EXTEND_ONLY_SELECTOR=118;
	public static final int SASS_FOR=119;
	public static final int SASS_FORWARD=120;
	public static final int SASS_FUNCTION=121;
	public static final int SASS_GLOBAL=122;
	public static final int SASS_IF=123;
	public static final int SASS_INCLUDE=124;
	public static final int SASS_MIXIN=125;
	public static final int SASS_OPTIONAL=126;
	public static final int SASS_RETURN=127;
	public static final int SASS_USE=128;
	public static final int SASS_VAR=129;
	public static final int SASS_WARN=130;
	public static final int SASS_WHILE=131;
	public static final int SEMI=132;
	public static final int SOLIDUS=133;
	public static final int STAR=134;
	public static final int STRING=135;
	public static final int SUPPORTS_SYM=136;
	public static final int T=137;
	public static final int TILDE=138;
	public static final int TIME=139;
	public static final int TOPCENTER_SYM=140;
	public static final int TOPLEFTCORNER_SYM=141;
	public static final int TOPLEFT_SYM=142;
	public static final int TOPRIGHTCORNER_SYM=143;
	public static final int TOPRIGHT_SYM=144;
	public static final int U=145;
	public static final int UNICODE=146;
	public static final int URANGE=147;
	public static final int URI=148;
	public static final int URL=149;
	public static final int V=150;
	public static final int VARIABLE=151;
	public static final int W=152;
	public static final int WEBKIT_KEYFRAMES_SYM=153;
	public static final int WS=154;
	public static final int X=155;
	public static final int Y=156;
	public static final int Z=157;

	    protected boolean isLessSource() {
	        return false;
	    }

	    protected boolean isScssSource() {
	        return false;
	    }

	    private boolean isCssPreprocessorSource() {
	        return isLessSource() || isScssSource();
	    }


	// delegates
	// delegators
	public Lexer[] getDelegates() {
		return new Lexer[] {};
	}

	public Css3Lexer() {} 
	public Css3Lexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}
	public Css3Lexer(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	@Override public String getGrammarFileName() { return "/home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g"; }

	// $ANTLR start "GEN"
	public final void mGEN() throws RecognitionException {
		try {
			int _type = GEN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1655:25: ( '@@@' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1655:27: '@@@'
			{
			match("@@@"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "GEN"

	// $ANTLR start "HEXCHAR"
	public final void mHEXCHAR() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1657:25: ( ( 'a' .. 'f' | 'A' .. 'F' | '0' .. '9' ) )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
			{
			if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'F')||(input.LA(1) >= 'a' && input.LA(1) <= 'f') ) {
				input.consume();
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "HEXCHAR"

	// $ANTLR start "NONASCII"
	public final void mNONASCII() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1659:25: ( '\\u0080' .. '\\uFFFF' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
			{
			if ( (input.LA(1) >= '\u0080' && input.LA(1) <= '\uFFFF') ) {
				input.consume();
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NONASCII"

	// $ANTLR start "UNICODE"
	public final void mUNICODE() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1661:25: ( '\\\\' HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )? )? ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1661:27: '\\\\' HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )? )? ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
			{
			match('\\'); if (state.failed) return;
			mHEXCHAR(); if (state.failed) return;

			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1662:33: ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )? )?
			int alt5=2;
			int LA5_0 = input.LA(1);
			if ( ((LA5_0 >= '0' && LA5_0 <= '9')||(LA5_0 >= 'A' && LA5_0 <= 'F')||(LA5_0 >= 'a' && LA5_0 <= 'f')) ) {
				alt5=1;
			}
			switch (alt5) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1662:34: HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )?
					{
					mHEXCHAR(); if (state.failed) return;

					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1663:37: ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )?
					int alt4=2;
					int LA4_0 = input.LA(1);
					if ( ((LA4_0 >= '0' && LA4_0 <= '9')||(LA4_0 >= 'A' && LA4_0 <= 'F')||(LA4_0 >= 'a' && LA4_0 <= 'f')) ) {
						alt4=1;
					}
					switch (alt4) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1663:38: HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )?
							{
							mHEXCHAR(); if (state.failed) return;

							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1664:41: ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )?
							int alt3=2;
							int LA3_0 = input.LA(1);
							if ( ((LA3_0 >= '0' && LA3_0 <= '9')||(LA3_0 >= 'A' && LA3_0 <= 'F')||(LA3_0 >= 'a' && LA3_0 <= 'f')) ) {
								alt3=1;
							}
							switch (alt3) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1664:42: HEXCHAR ( HEXCHAR ( HEXCHAR )? )?
									{
									mHEXCHAR(); if (state.failed) return;

									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1665:45: ( HEXCHAR ( HEXCHAR )? )?
									int alt2=2;
									int LA2_0 = input.LA(1);
									if ( ((LA2_0 >= '0' && LA2_0 <= '9')||(LA2_0 >= 'A' && LA2_0 <= 'F')||(LA2_0 >= 'a' && LA2_0 <= 'f')) ) {
										alt2=1;
									}
									switch (alt2) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1665:46: HEXCHAR ( HEXCHAR )?
											{
											mHEXCHAR(); if (state.failed) return;

											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1665:54: ( HEXCHAR )?
											int alt1=2;
											int LA1_0 = input.LA(1);
											if ( ((LA1_0 >= '0' && LA1_0 <= '9')||(LA1_0 >= 'A' && LA1_0 <= 'F')||(LA1_0 >= 'a' && LA1_0 <= 'f')) ) {
												alt1=1;
											}
											switch (alt1) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
													{
													if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'F')||(input.LA(1) >= 'a' && input.LA(1) <= 'f') ) {
														input.consume();
														state.failed=false;
													}
													else {
														if (state.backtracking>0) {state.failed=true; return;}
														MismatchedSetException mse = new MismatchedSetException(null,input);
														recover(mse);
														throw mse;
													}
													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							}
							break;

					}

					}
					break;

			}

			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1669:33: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
			loop6:
			while (true) {
				int alt6=2;
				int LA6_0 = input.LA(1);
				if ( ((LA6_0 >= '\t' && LA6_0 <= '\n')||(LA6_0 >= '\f' && LA6_0 <= '\r')||LA6_0==' ') ) {
					alt6=1;
				}

				switch (alt6) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
					{
					if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||(input.LA(1) >= '\f' && input.LA(1) <= '\r')||input.LA(1)==' ' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop6;
				}
			}

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "UNICODE"

	// $ANTLR start "ESCAPE"
	public final void mESCAPE() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1671:25: ( UNICODE | '\\\\' ~ ( '\\r' | '\\n' | '\\f' | HEXCHAR ) )
			int alt7=2;
			int LA7_0 = input.LA(1);
			if ( (LA7_0=='\\') ) {
				int LA7_1 = input.LA(2);
				if ( ((LA7_1 >= '\u0000' && LA7_1 <= '\t')||LA7_1=='\u000B'||(LA7_1 >= '\u000E' && LA7_1 <= '/')||(LA7_1 >= ':' && LA7_1 <= '@')||(LA7_1 >= 'G' && LA7_1 <= '`')||(LA7_1 >= 'g' && LA7_1 <= '\uFFFF')) ) {
					alt7=2;
				}
				else if ( ((LA7_1 >= '0' && LA7_1 <= '9')||(LA7_1 >= 'A' && LA7_1 <= 'F')||(LA7_1 >= 'a' && LA7_1 <= 'f')) ) {
					alt7=1;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 7, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 7, 0, input);
				throw nvae;
			}

			switch (alt7) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1671:27: UNICODE
					{
					mUNICODE(); if (state.failed) return;

					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1671:37: '\\\\' ~ ( '\\r' | '\\n' | '\\f' | HEXCHAR )
					{
					match('\\'); if (state.failed) return;
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||input.LA(1)=='\u000B'||(input.LA(1) >= '\u000E' && input.LA(1) <= '/')||(input.LA(1) >= ':' && input.LA(1) <= '@')||(input.LA(1) >= 'G' && input.LA(1) <= '`')||(input.LA(1) >= 'g' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ESCAPE"

	// $ANTLR start "NMSTART"
	public final void mNMSTART() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1673:25: ( '_' | 'a' .. 'z' | 'A' .. 'Z' | NONASCII | ESCAPE )
			int alt8=5;
			int LA8_0 = input.LA(1);
			if ( (LA8_0=='_') ) {
				alt8=1;
			}
			else if ( ((LA8_0 >= 'a' && LA8_0 <= 'z')) ) {
				alt8=2;
			}
			else if ( ((LA8_0 >= 'A' && LA8_0 <= 'Z')) ) {
				alt8=3;
			}
			else if ( ((LA8_0 >= '\u0080' && LA8_0 <= '\uFFFF')) ) {
				alt8=4;
			}
			else if ( (LA8_0=='\\') ) {
				alt8=5;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 8, 0, input);
				throw nvae;
			}

			switch (alt8) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1673:27: '_'
					{
					match('_'); if (state.failed) return;
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1674:27: 'a' .. 'z'
					{
					matchRange('a','z'); if (state.failed) return;
					}
					break;
				case 3 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1675:27: 'A' .. 'Z'
					{
					matchRange('A','Z'); if (state.failed) return;
					}
					break;
				case 4 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1676:27: NONASCII
					{
					mNONASCII(); if (state.failed) return;

					}
					break;
				case 5 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1677:27: ESCAPE
					{
					mESCAPE(); if (state.failed) return;

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NMSTART"

	// $ANTLR start "NMCHAR"
	public final void mNMCHAR() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1680:25: ( '_' | 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '-' | NONASCII | ESCAPE )
			int alt9=7;
			int LA9_0 = input.LA(1);
			if ( (LA9_0=='_') ) {
				alt9=1;
			}
			else if ( ((LA9_0 >= 'a' && LA9_0 <= 'z')) ) {
				alt9=2;
			}
			else if ( ((LA9_0 >= 'A' && LA9_0 <= 'Z')) ) {
				alt9=3;
			}
			else if ( ((LA9_0 >= '0' && LA9_0 <= '9')) ) {
				alt9=4;
			}
			else if ( (LA9_0=='-') ) {
				alt9=5;
			}
			else if ( ((LA9_0 >= '\u0080' && LA9_0 <= '\uFFFF')) ) {
				alt9=6;
			}
			else if ( (LA9_0=='\\') ) {
				alt9=7;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 9, 0, input);
				throw nvae;
			}

			switch (alt9) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1680:27: '_'
					{
					match('_'); if (state.failed) return;
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1681:27: 'a' .. 'z'
					{
					matchRange('a','z'); if (state.failed) return;
					}
					break;
				case 3 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1682:27: 'A' .. 'Z'
					{
					matchRange('A','Z'); if (state.failed) return;
					}
					break;
				case 4 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1683:27: '0' .. '9'
					{
					matchRange('0','9'); if (state.failed) return;
					}
					break;
				case 5 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1684:27: '-'
					{
					match('-'); if (state.failed) return;
					}
					break;
				case 6 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1685:27: NONASCII
					{
					mNONASCII(); if (state.failed) return;

					}
					break;
				case 7 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1686:27: ESCAPE
					{
					mESCAPE(); if (state.failed) return;

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NMCHAR"

	// $ANTLR start "NAME"
	public final void mNAME() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1689:25: ( ( NMCHAR )+ )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1689:27: ( NMCHAR )+
			{
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1689:27: ( NMCHAR )+
			int cnt10=0;
			loop10:
			while (true) {
				int alt10=2;
				int LA10_0 = input.LA(1);
				if ( (LA10_0=='-'||(LA10_0 >= '0' && LA10_0 <= '9')||(LA10_0 >= 'A' && LA10_0 <= 'Z')||LA10_0=='\\'||LA10_0=='_'||(LA10_0 >= 'a' && LA10_0 <= 'z')||(LA10_0 >= '\u0080' && LA10_0 <= '\uFFFF')) ) {
					alt10=1;
				}

				switch (alt10) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1689:27: NMCHAR
					{
					mNMCHAR(); if (state.failed) return;

					}
					break;

				default :
					if ( cnt10 >= 1 ) break loop10;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(10, input);
					throw eee;
				}
				cnt10++;
			}

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NAME"

	// $ANTLR start "URL"
	public final void mURL() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1691:25: ( ( ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | '?' | '=' | ';' | ',' | '+' | '@' | '|' | '{' | '}' | NMCHAR ) ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | '?' | '=' | ';' | ',' | '+' | '@' | '|' | WS | '\\\"' | '{' | '}' | NMCHAR )* )? )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1691:27: ( ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | '?' | '=' | ';' | ',' | '+' | '@' | '|' | '{' | '}' | NMCHAR ) ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | '?' | '=' | ';' | ',' | '+' | '@' | '|' | WS | '\\\"' | '{' | '}' | NMCHAR )* )?
			{
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1691:27: ( ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | '?' | '=' | ';' | ',' | '+' | '@' | '|' | '{' | '}' | NMCHAR ) ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | '?' | '=' | ';' | ',' | '+' | '@' | '|' | WS | '\\\"' | '{' | '}' | NMCHAR )* )?
			int alt13=2;
			int LA13_0 = input.LA(1);
			if ( (LA13_0=='!'||(LA13_0 >= '#' && LA13_0 <= '&')||(LA13_0 >= '*' && LA13_0 <= ';')||LA13_0=='='||(LA13_0 >= '?' && LA13_0 <= '\\')||LA13_0=='_'||(LA13_0 >= 'a' && LA13_0 <= '~')||(LA13_0 >= '\u0080' && LA13_0 <= '\uFFFF')) ) {
				alt13=1;
			}
			switch (alt13) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1691:28: ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | '?' | '=' | ';' | ',' | '+' | '@' | '|' | '{' | '}' | NMCHAR ) ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | '?' | '=' | ';' | ',' | '+' | '@' | '|' | WS | '\\\"' | '{' | '}' | NMCHAR )*
					{
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1691:28: ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | '?' | '=' | ';' | ',' | '+' | '@' | '|' | '{' | '}' | NMCHAR )
					int alt11=21;
					int LA11_0 = input.LA(1);
					if ( (LA11_0=='[') ) {
						alt11=1;
					}
					else if ( (LA11_0=='!') ) {
						alt11=2;
					}
					else if ( (LA11_0=='#') ) {
						alt11=3;
					}
					else if ( (LA11_0=='$') ) {
						alt11=4;
					}
					else if ( (LA11_0=='%') ) {
						alt11=5;
					}
					else if ( (LA11_0=='&') ) {
						alt11=6;
					}
					else if ( (LA11_0=='*') ) {
						alt11=7;
					}
					else if ( (LA11_0=='~') ) {
						alt11=8;
					}
					else if ( (LA11_0=='.') ) {
						alt11=9;
					}
					else if ( (LA11_0==':') ) {
						alt11=10;
					}
					else if ( (LA11_0=='/') ) {
						alt11=11;
					}
					else if ( (LA11_0=='?') ) {
						alt11=12;
					}
					else if ( (LA11_0=='=') ) {
						alt11=13;
					}
					else if ( (LA11_0==';') ) {
						alt11=14;
					}
					else if ( (LA11_0==',') ) {
						alt11=15;
					}
					else if ( (LA11_0=='+') ) {
						alt11=16;
					}
					else if ( (LA11_0=='@') ) {
						alt11=17;
					}
					else if ( (LA11_0=='|') ) {
						alt11=18;
					}
					else if ( (LA11_0=='{') ) {
						alt11=19;
					}
					else if ( (LA11_0=='}') ) {
						alt11=20;
					}
					else if ( (LA11_0=='-'||(LA11_0 >= '0' && LA11_0 <= '9')||(LA11_0 >= 'A' && LA11_0 <= 'Z')||LA11_0=='\\'||LA11_0=='_'||(LA11_0 >= 'a' && LA11_0 <= 'z')||(LA11_0 >= '\u0080' && LA11_0 <= '\uFFFF')) ) {
						alt11=21;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 11, 0, input);
						throw nvae;
					}

					switch (alt11) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:31: '['
							{
							match('['); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:35: '!'
							{
							match('!'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:39: '#'
							{
							match('#'); if (state.failed) return;
							}
							break;
						case 4 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:43: '$'
							{
							match('$'); if (state.failed) return;
							}
							break;
						case 5 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:47: '%'
							{
							match('%'); if (state.failed) return;
							}
							break;
						case 6 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:51: '&'
							{
							match('&'); if (state.failed) return;
							}
							break;
						case 7 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:55: '*'
							{
							match('*'); if (state.failed) return;
							}
							break;
						case 8 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:59: '~'
							{
							match('~'); if (state.failed) return;
							}
							break;
						case 9 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:63: '.'
							{
							match('.'); if (state.failed) return;
							}
							break;
						case 10 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:67: ':'
							{
							match(':'); if (state.failed) return;
							}
							break;
						case 11 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:71: '/'
							{
							match('/'); if (state.failed) return;
							}
							break;
						case 12 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:75: '?'
							{
							match('?'); if (state.failed) return;
							}
							break;
						case 13 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:79: '='
							{
							match('='); if (state.failed) return;
							}
							break;
						case 14 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:83: ';'
							{
							match(';'); if (state.failed) return;
							}
							break;
						case 15 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:87: ','
							{
							match(','); if (state.failed) return;
							}
							break;
						case 16 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:91: '+'
							{
							match('+'); if (state.failed) return;
							}
							break;
						case 17 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:95: '@'
							{
							match('@'); if (state.failed) return;
							}
							break;
						case 18 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:99: '|'
							{
							match('|'); if (state.failed) return;
							}
							break;
						case 19 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:105: '{'
							{
							match('{'); if (state.failed) return;
							}
							break;
						case 20 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:111: '}'
							{
							match('}'); if (state.failed) return;
							}
							break;
						case 21 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1693:31: NMCHAR
							{
							mNMCHAR(); if (state.failed) return;

							}
							break;

					}

					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1695:27: ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | '?' | '=' | ';' | ',' | '+' | '@' | '|' | WS | '\\\"' | '{' | '}' | NMCHAR )*
					loop12:
					while (true) {
						int alt12=24;
						int LA12_0 = input.LA(1);
						if ( (LA12_0=='[') ) {
							alt12=1;
						}
						else if ( (LA12_0=='!') ) {
							alt12=2;
						}
						else if ( (LA12_0=='#') ) {
							alt12=3;
						}
						else if ( (LA12_0=='$') ) {
							alt12=4;
						}
						else if ( (LA12_0=='%') ) {
							alt12=5;
						}
						else if ( (LA12_0=='&') ) {
							alt12=6;
						}
						else if ( (LA12_0=='*') ) {
							alt12=7;
						}
						else if ( (LA12_0=='~') ) {
							alt12=8;
						}
						else if ( (LA12_0=='.') ) {
							alt12=9;
						}
						else if ( (LA12_0==':') ) {
							alt12=10;
						}
						else if ( (LA12_0=='/') ) {
							alt12=11;
						}
						else if ( (LA12_0=='?') ) {
							alt12=12;
						}
						else if ( (LA12_0=='=') ) {
							alt12=13;
						}
						else if ( (LA12_0==';') ) {
							alt12=14;
						}
						else if ( (LA12_0==',') ) {
							alt12=15;
						}
						else if ( (LA12_0=='+') ) {
							alt12=16;
						}
						else if ( (LA12_0=='@') ) {
							alt12=17;
						}
						else if ( (LA12_0=='|') ) {
							alt12=18;
						}
						else if ( (LA12_0=='\t'||LA12_0==' ') ) {
							alt12=19;
						}
						else if ( (LA12_0=='\"') ) {
							alt12=20;
						}
						else if ( (LA12_0=='{') ) {
							alt12=21;
						}
						else if ( (LA12_0=='}') ) {
							alt12=22;
						}
						else if ( (LA12_0=='-'||(LA12_0 >= '0' && LA12_0 <= '9')||(LA12_0 >= 'A' && LA12_0 <= 'Z')||LA12_0=='\\'||LA12_0=='_'||(LA12_0 >= 'a' && LA12_0 <= 'z')||(LA12_0 >= '\u0080' && LA12_0 <= '\uFFFF')) ) {
							alt12=23;
						}

						switch (alt12) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:31: '['
							{
							match('['); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:35: '!'
							{
							match('!'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:39: '#'
							{
							match('#'); if (state.failed) return;
							}
							break;
						case 4 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:43: '$'
							{
							match('$'); if (state.failed) return;
							}
							break;
						case 5 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:47: '%'
							{
							match('%'); if (state.failed) return;
							}
							break;
						case 6 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:51: '&'
							{
							match('&'); if (state.failed) return;
							}
							break;
						case 7 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:55: '*'
							{
							match('*'); if (state.failed) return;
							}
							break;
						case 8 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:59: '~'
							{
							match('~'); if (state.failed) return;
							}
							break;
						case 9 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:63: '.'
							{
							match('.'); if (state.failed) return;
							}
							break;
						case 10 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:67: ':'
							{
							match(':'); if (state.failed) return;
							}
							break;
						case 11 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:71: '/'
							{
							match('/'); if (state.failed) return;
							}
							break;
						case 12 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:75: '?'
							{
							match('?'); if (state.failed) return;
							}
							break;
						case 13 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:79: '='
							{
							match('='); if (state.failed) return;
							}
							break;
						case 14 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:83: ';'
							{
							match(';'); if (state.failed) return;
							}
							break;
						case 15 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:87: ','
							{
							match(','); if (state.failed) return;
							}
							break;
						case 16 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:91: '+'
							{
							match('+'); if (state.failed) return;
							}
							break;
						case 17 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:95: '@'
							{
							match('@'); if (state.failed) return;
							}
							break;
						case 18 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:99: '|'
							{
							match('|'); if (state.failed) return;
							}
							break;
						case 19 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:105: WS
							{
							mWS(); if (state.failed) return;

							}
							break;
						case 20 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:111: '\\\"'
							{
							match('\"'); if (state.failed) return;
							}
							break;
						case 21 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:118: '{'
							{
							match('{'); if (state.failed) return;
							}
							break;
						case 22 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:124: '}'
							{
							match('}'); if (state.failed) return;
							}
							break;
						case 23 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1697:31: NMCHAR
							{
							mNMCHAR(); if (state.failed) return;

							}
							break;

						default :
							break loop12;
						}
					}

					}
					break;

			}

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "URL"

	// $ANTLR start "A"
	public final void mA() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1704:17: ( ( 'a' | 'A' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '1' )
			int alt18=2;
			int LA18_0 = input.LA(1);
			if ( (LA18_0=='A'||LA18_0=='a') ) {
				alt18=1;
			}
			else if ( (LA18_0=='\\') ) {
				alt18=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 18, 0, input);
				throw nvae;
			}

			switch (alt18) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1704:21: ( 'a' | 'A' )
					{
					if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1705:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '1'
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1705:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
					int alt17=2;
					int LA17_0 = input.LA(1);
					if ( (LA17_0=='0') ) {
						alt17=1;
					}
					switch (alt17) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1705:27: '0' ( '0' ( '0' ( '0' )? )? )?
							{
							match('0'); if (state.failed) return;
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1705:31: ( '0' ( '0' ( '0' )? )? )?
							int alt16=2;
							int LA16_0 = input.LA(1);
							if ( (LA16_0=='0') ) {
								alt16=1;
							}
							switch (alt16) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1705:32: '0' ( '0' ( '0' )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1705:36: ( '0' ( '0' )? )?
									int alt15=2;
									int LA15_0 = input.LA(1);
									if ( (LA15_0=='0') ) {
										alt15=1;
									}
									switch (alt15) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1705:37: '0' ( '0' )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1705:41: ( '0' )?
											int alt14=2;
											int LA14_0 = input.LA(1);
											if ( (LA14_0=='0') ) {
												alt14=1;
											}
											switch (alt14) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1705:41: '0'
													{
													match('0'); if (state.failed) return;
													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							}
							break;

					}

					if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					match('1'); if (state.failed) return;
					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "A"

	// $ANTLR start "B"
	public final void mB() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1707:17: ( ( 'b' | 'B' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '2' )
			int alt23=2;
			int LA23_0 = input.LA(1);
			if ( (LA23_0=='B'||LA23_0=='b') ) {
				alt23=1;
			}
			else if ( (LA23_0=='\\') ) {
				alt23=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 23, 0, input);
				throw nvae;
			}

			switch (alt23) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1707:21: ( 'b' | 'B' )
					{
					if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1708:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '2'
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1708:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
					int alt22=2;
					int LA22_0 = input.LA(1);
					if ( (LA22_0=='0') ) {
						alt22=1;
					}
					switch (alt22) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1708:27: '0' ( '0' ( '0' ( '0' )? )? )?
							{
							match('0'); if (state.failed) return;
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1708:31: ( '0' ( '0' ( '0' )? )? )?
							int alt21=2;
							int LA21_0 = input.LA(1);
							if ( (LA21_0=='0') ) {
								alt21=1;
							}
							switch (alt21) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1708:32: '0' ( '0' ( '0' )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1708:36: ( '0' ( '0' )? )?
									int alt20=2;
									int LA20_0 = input.LA(1);
									if ( (LA20_0=='0') ) {
										alt20=1;
									}
									switch (alt20) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1708:37: '0' ( '0' )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1708:41: ( '0' )?
											int alt19=2;
											int LA19_0 = input.LA(1);
											if ( (LA19_0=='0') ) {
												alt19=1;
											}
											switch (alt19) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1708:41: '0'
													{
													match('0'); if (state.failed) return;
													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							}
							break;

					}

					if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					match('2'); if (state.failed) return;
					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "B"

	// $ANTLR start "C"
	public final void mC() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1710:17: ( ( 'c' | 'C' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '3' )
			int alt28=2;
			int LA28_0 = input.LA(1);
			if ( (LA28_0=='C'||LA28_0=='c') ) {
				alt28=1;
			}
			else if ( (LA28_0=='\\') ) {
				alt28=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 28, 0, input);
				throw nvae;
			}

			switch (alt28) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1710:21: ( 'c' | 'C' )
					{
					if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1711:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '3'
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1711:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
					int alt27=2;
					int LA27_0 = input.LA(1);
					if ( (LA27_0=='0') ) {
						alt27=1;
					}
					switch (alt27) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1711:27: '0' ( '0' ( '0' ( '0' )? )? )?
							{
							match('0'); if (state.failed) return;
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1711:31: ( '0' ( '0' ( '0' )? )? )?
							int alt26=2;
							int LA26_0 = input.LA(1);
							if ( (LA26_0=='0') ) {
								alt26=1;
							}
							switch (alt26) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1711:32: '0' ( '0' ( '0' )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1711:36: ( '0' ( '0' )? )?
									int alt25=2;
									int LA25_0 = input.LA(1);
									if ( (LA25_0=='0') ) {
										alt25=1;
									}
									switch (alt25) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1711:37: '0' ( '0' )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1711:41: ( '0' )?
											int alt24=2;
											int LA24_0 = input.LA(1);
											if ( (LA24_0=='0') ) {
												alt24=1;
											}
											switch (alt24) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1711:41: '0'
													{
													match('0'); if (state.failed) return;
													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							}
							break;

					}

					if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					match('3'); if (state.failed) return;
					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "C"

	// $ANTLR start "D"
	public final void mD() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1713:17: ( ( 'd' | 'D' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '4' )
			int alt33=2;
			int LA33_0 = input.LA(1);
			if ( (LA33_0=='D'||LA33_0=='d') ) {
				alt33=1;
			}
			else if ( (LA33_0=='\\') ) {
				alt33=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 33, 0, input);
				throw nvae;
			}

			switch (alt33) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1713:21: ( 'd' | 'D' )
					{
					if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1714:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '4'
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1714:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
					int alt32=2;
					int LA32_0 = input.LA(1);
					if ( (LA32_0=='0') ) {
						alt32=1;
					}
					switch (alt32) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1714:27: '0' ( '0' ( '0' ( '0' )? )? )?
							{
							match('0'); if (state.failed) return;
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1714:31: ( '0' ( '0' ( '0' )? )? )?
							int alt31=2;
							int LA31_0 = input.LA(1);
							if ( (LA31_0=='0') ) {
								alt31=1;
							}
							switch (alt31) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1714:32: '0' ( '0' ( '0' )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1714:36: ( '0' ( '0' )? )?
									int alt30=2;
									int LA30_0 = input.LA(1);
									if ( (LA30_0=='0') ) {
										alt30=1;
									}
									switch (alt30) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1714:37: '0' ( '0' )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1714:41: ( '0' )?
											int alt29=2;
											int LA29_0 = input.LA(1);
											if ( (LA29_0=='0') ) {
												alt29=1;
											}
											switch (alt29) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1714:41: '0'
													{
													match('0'); if (state.failed) return;
													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							}
							break;

					}

					if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					match('4'); if (state.failed) return;
					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "D"

	// $ANTLR start "E"
	public final void mE() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1716:17: ( ( 'e' | 'E' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '5' )
			int alt38=2;
			int LA38_0 = input.LA(1);
			if ( (LA38_0=='E'||LA38_0=='e') ) {
				alt38=1;
			}
			else if ( (LA38_0=='\\') ) {
				alt38=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 38, 0, input);
				throw nvae;
			}

			switch (alt38) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1716:21: ( 'e' | 'E' )
					{
					if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1717:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '5'
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1717:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
					int alt37=2;
					int LA37_0 = input.LA(1);
					if ( (LA37_0=='0') ) {
						alt37=1;
					}
					switch (alt37) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1717:27: '0' ( '0' ( '0' ( '0' )? )? )?
							{
							match('0'); if (state.failed) return;
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1717:31: ( '0' ( '0' ( '0' )? )? )?
							int alt36=2;
							int LA36_0 = input.LA(1);
							if ( (LA36_0=='0') ) {
								alt36=1;
							}
							switch (alt36) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1717:32: '0' ( '0' ( '0' )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1717:36: ( '0' ( '0' )? )?
									int alt35=2;
									int LA35_0 = input.LA(1);
									if ( (LA35_0=='0') ) {
										alt35=1;
									}
									switch (alt35) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1717:37: '0' ( '0' )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1717:41: ( '0' )?
											int alt34=2;
											int LA34_0 = input.LA(1);
											if ( (LA34_0=='0') ) {
												alt34=1;
											}
											switch (alt34) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1717:41: '0'
													{
													match('0'); if (state.failed) return;
													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							}
							break;

					}

					if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					match('5'); if (state.failed) return;
					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "E"

	// $ANTLR start "F"
	public final void mF() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1719:17: ( ( 'f' | 'F' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '6' )
			int alt43=2;
			int LA43_0 = input.LA(1);
			if ( (LA43_0=='F'||LA43_0=='f') ) {
				alt43=1;
			}
			else if ( (LA43_0=='\\') ) {
				alt43=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 43, 0, input);
				throw nvae;
			}

			switch (alt43) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1719:21: ( 'f' | 'F' )
					{
					if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1720:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '6'
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1720:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
					int alt42=2;
					int LA42_0 = input.LA(1);
					if ( (LA42_0=='0') ) {
						alt42=1;
					}
					switch (alt42) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1720:27: '0' ( '0' ( '0' ( '0' )? )? )?
							{
							match('0'); if (state.failed) return;
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1720:31: ( '0' ( '0' ( '0' )? )? )?
							int alt41=2;
							int LA41_0 = input.LA(1);
							if ( (LA41_0=='0') ) {
								alt41=1;
							}
							switch (alt41) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1720:32: '0' ( '0' ( '0' )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1720:36: ( '0' ( '0' )? )?
									int alt40=2;
									int LA40_0 = input.LA(1);
									if ( (LA40_0=='0') ) {
										alt40=1;
									}
									switch (alt40) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1720:37: '0' ( '0' )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1720:41: ( '0' )?
											int alt39=2;
											int LA39_0 = input.LA(1);
											if ( (LA39_0=='0') ) {
												alt39=1;
											}
											switch (alt39) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1720:41: '0'
													{
													match('0'); if (state.failed) return;
													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							}
							break;

					}

					if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					match('6'); if (state.failed) return;
					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "F"

	// $ANTLR start "G"
	public final void mG() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1722:17: ( ( 'g' | 'G' ) | '\\\\' ( 'g' | 'G' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7' ) )
			int alt49=2;
			int LA49_0 = input.LA(1);
			if ( (LA49_0=='G'||LA49_0=='g') ) {
				alt49=1;
			}
			else if ( (LA49_0=='\\') ) {
				alt49=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 49, 0, input);
				throw nvae;
			}

			switch (alt49) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1722:21: ( 'g' | 'G' )
					{
					if ( input.LA(1)=='G'||input.LA(1)=='g' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1723:21: '\\\\' ( 'g' | 'G' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7' )
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1724:25: ( 'g' | 'G' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7' )
					int alt48=3;
					switch ( input.LA(1) ) {
					case 'g':
						{
						alt48=1;
						}
						break;
					case 'G':
						{
						alt48=2;
						}
						break;
					case '0':
					case '4':
					case '6':
						{
						alt48=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 48, 0, input);
						throw nvae;
					}
					switch (alt48) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1725:31: 'g'
							{
							match('g'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1726:31: 'G'
							{
							match('G'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1727:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7'
							{
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1727:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt47=2;
							int LA47_0 = input.LA(1);
							if ( (LA47_0=='0') ) {
								alt47=1;
							}
							switch (alt47) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1727:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1727:36: ( '0' ( '0' ( '0' )? )? )?
									int alt46=2;
									int LA46_0 = input.LA(1);
									if ( (LA46_0=='0') ) {
										alt46=1;
									}
									switch (alt46) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1727:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1727:41: ( '0' ( '0' )? )?
											int alt45=2;
											int LA45_0 = input.LA(1);
											if ( (LA45_0=='0') ) {
												alt45=1;
											}
											switch (alt45) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1727:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1727:46: ( '0' )?
													int alt44=2;
													int LA44_0 = input.LA(1);
													if ( (LA44_0=='0') ) {
														alt44=1;
													}
													switch (alt44) {
														case 1 :
															// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1727:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							match('7'); if (state.failed) return;
							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "G"

	// $ANTLR start "H"
	public final void mH() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1730:17: ( ( 'h' | 'H' ) | '\\\\' ( 'h' | 'H' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8' ) )
			int alt55=2;
			int LA55_0 = input.LA(1);
			if ( (LA55_0=='H'||LA55_0=='h') ) {
				alt55=1;
			}
			else if ( (LA55_0=='\\') ) {
				alt55=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 55, 0, input);
				throw nvae;
			}

			switch (alt55) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1730:21: ( 'h' | 'H' )
					{
					if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1731:19: '\\\\' ( 'h' | 'H' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8' )
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1732:25: ( 'h' | 'H' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8' )
					int alt54=3;
					switch ( input.LA(1) ) {
					case 'h':
						{
						alt54=1;
						}
						break;
					case 'H':
						{
						alt54=2;
						}
						break;
					case '0':
					case '4':
					case '6':
						{
						alt54=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 54, 0, input);
						throw nvae;
					}
					switch (alt54) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1733:31: 'h'
							{
							match('h'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1734:31: 'H'
							{
							match('H'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1735:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8'
							{
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1735:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt53=2;
							int LA53_0 = input.LA(1);
							if ( (LA53_0=='0') ) {
								alt53=1;
							}
							switch (alt53) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1735:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1735:36: ( '0' ( '0' ( '0' )? )? )?
									int alt52=2;
									int LA52_0 = input.LA(1);
									if ( (LA52_0=='0') ) {
										alt52=1;
									}
									switch (alt52) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1735:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1735:41: ( '0' ( '0' )? )?
											int alt51=2;
											int LA51_0 = input.LA(1);
											if ( (LA51_0=='0') ) {
												alt51=1;
											}
											switch (alt51) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1735:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1735:46: ( '0' )?
													int alt50=2;
													int LA50_0 = input.LA(1);
													if ( (LA50_0=='0') ) {
														alt50=1;
													}
													switch (alt50) {
														case 1 :
															// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1735:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							match('8'); if (state.failed) return;
							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "H"

	// $ANTLR start "I"
	public final void mI() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1738:17: ( ( 'i' | 'I' ) | '\\\\' ( 'i' | 'I' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9' ) )
			int alt61=2;
			int LA61_0 = input.LA(1);
			if ( (LA61_0=='I'||LA61_0=='i') ) {
				alt61=1;
			}
			else if ( (LA61_0=='\\') ) {
				alt61=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 61, 0, input);
				throw nvae;
			}

			switch (alt61) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1738:21: ( 'i' | 'I' )
					{
					if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1739:19: '\\\\' ( 'i' | 'I' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9' )
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1740:25: ( 'i' | 'I' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9' )
					int alt60=3;
					switch ( input.LA(1) ) {
					case 'i':
						{
						alt60=1;
						}
						break;
					case 'I':
						{
						alt60=2;
						}
						break;
					case '0':
					case '4':
					case '6':
						{
						alt60=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 60, 0, input);
						throw nvae;
					}
					switch (alt60) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1741:31: 'i'
							{
							match('i'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1742:31: 'I'
							{
							match('I'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1743:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9'
							{
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1743:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt59=2;
							int LA59_0 = input.LA(1);
							if ( (LA59_0=='0') ) {
								alt59=1;
							}
							switch (alt59) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1743:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1743:36: ( '0' ( '0' ( '0' )? )? )?
									int alt58=2;
									int LA58_0 = input.LA(1);
									if ( (LA58_0=='0') ) {
										alt58=1;
									}
									switch (alt58) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1743:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1743:41: ( '0' ( '0' )? )?
											int alt57=2;
											int LA57_0 = input.LA(1);
											if ( (LA57_0=='0') ) {
												alt57=1;
											}
											switch (alt57) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1743:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1743:46: ( '0' )?
													int alt56=2;
													int LA56_0 = input.LA(1);
													if ( (LA56_0=='0') ) {
														alt56=1;
													}
													switch (alt56) {
														case 1 :
															// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1743:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							match('9'); if (state.failed) return;
							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "I"

	// $ANTLR start "J"
	public final void mJ() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1746:17: ( ( 'j' | 'J' ) | '\\\\' ( 'j' | 'J' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' ) ) )
			int alt67=2;
			int LA67_0 = input.LA(1);
			if ( (LA67_0=='J'||LA67_0=='j') ) {
				alt67=1;
			}
			else if ( (LA67_0=='\\') ) {
				alt67=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 67, 0, input);
				throw nvae;
			}

			switch (alt67) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1746:21: ( 'j' | 'J' )
					{
					if ( input.LA(1)=='J'||input.LA(1)=='j' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1747:19: '\\\\' ( 'j' | 'J' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1748:25: ( 'j' | 'J' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' ) )
					int alt66=3;
					switch ( input.LA(1) ) {
					case 'j':
						{
						alt66=1;
						}
						break;
					case 'J':
						{
						alt66=2;
						}
						break;
					case '0':
					case '4':
					case '6':
						{
						alt66=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 66, 0, input);
						throw nvae;
					}
					switch (alt66) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1749:31: 'j'
							{
							match('j'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1750:31: 'J'
							{
							match('J'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1751:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' )
							{
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1751:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt65=2;
							int LA65_0 = input.LA(1);
							if ( (LA65_0=='0') ) {
								alt65=1;
							}
							switch (alt65) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1751:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1751:36: ( '0' ( '0' ( '0' )? )? )?
									int alt64=2;
									int LA64_0 = input.LA(1);
									if ( (LA64_0=='0') ) {
										alt64=1;
									}
									switch (alt64) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1751:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1751:41: ( '0' ( '0' )? )?
											int alt63=2;
											int LA63_0 = input.LA(1);
											if ( (LA63_0=='0') ) {
												alt63=1;
											}
											switch (alt63) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1751:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1751:46: ( '0' )?
													int alt62=2;
													int LA62_0 = input.LA(1);
													if ( (LA62_0=='0') ) {
														alt62=1;
													}
													switch (alt62) {
														case 1 :
															// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1751:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "J"

	// $ANTLR start "K"
	public final void mK() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1754:17: ( ( 'k' | 'K' ) | '\\\\' ( 'k' | 'K' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' ) ) )
			int alt73=2;
			int LA73_0 = input.LA(1);
			if ( (LA73_0=='K'||LA73_0=='k') ) {
				alt73=1;
			}
			else if ( (LA73_0=='\\') ) {
				alt73=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 73, 0, input);
				throw nvae;
			}

			switch (alt73) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1754:21: ( 'k' | 'K' )
					{
					if ( input.LA(1)=='K'||input.LA(1)=='k' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1755:19: '\\\\' ( 'k' | 'K' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1756:25: ( 'k' | 'K' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' ) )
					int alt72=3;
					switch ( input.LA(1) ) {
					case 'k':
						{
						alt72=1;
						}
						break;
					case 'K':
						{
						alt72=2;
						}
						break;
					case '0':
					case '4':
					case '6':
						{
						alt72=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 72, 0, input);
						throw nvae;
					}
					switch (alt72) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1757:31: 'k'
							{
							match('k'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1758:31: 'K'
							{
							match('K'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1759:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' )
							{
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1759:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt71=2;
							int LA71_0 = input.LA(1);
							if ( (LA71_0=='0') ) {
								alt71=1;
							}
							switch (alt71) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1759:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1759:36: ( '0' ( '0' ( '0' )? )? )?
									int alt70=2;
									int LA70_0 = input.LA(1);
									if ( (LA70_0=='0') ) {
										alt70=1;
									}
									switch (alt70) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1759:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1759:41: ( '0' ( '0' )? )?
											int alt69=2;
											int LA69_0 = input.LA(1);
											if ( (LA69_0=='0') ) {
												alt69=1;
											}
											switch (alt69) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1759:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1759:46: ( '0' )?
													int alt68=2;
													int LA68_0 = input.LA(1);
													if ( (LA68_0=='0') ) {
														alt68=1;
													}
													switch (alt68) {
														case 1 :
															// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1759:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "K"

	// $ANTLR start "L"
	public final void mL() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1762:17: ( ( 'l' | 'L' ) | '\\\\' ( 'l' | 'L' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' ) ) )
			int alt79=2;
			int LA79_0 = input.LA(1);
			if ( (LA79_0=='L'||LA79_0=='l') ) {
				alt79=1;
			}
			else if ( (LA79_0=='\\') ) {
				alt79=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 79, 0, input);
				throw nvae;
			}

			switch (alt79) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1762:21: ( 'l' | 'L' )
					{
					if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1763:19: '\\\\' ( 'l' | 'L' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1764:25: ( 'l' | 'L' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' ) )
					int alt78=3;
					switch ( input.LA(1) ) {
					case 'l':
						{
						alt78=1;
						}
						break;
					case 'L':
						{
						alt78=2;
						}
						break;
					case '0':
					case '4':
					case '6':
						{
						alt78=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 78, 0, input);
						throw nvae;
					}
					switch (alt78) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1765:31: 'l'
							{
							match('l'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1766:31: 'L'
							{
							match('L'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1767:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' )
							{
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1767:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt77=2;
							int LA77_0 = input.LA(1);
							if ( (LA77_0=='0') ) {
								alt77=1;
							}
							switch (alt77) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1767:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1767:36: ( '0' ( '0' ( '0' )? )? )?
									int alt76=2;
									int LA76_0 = input.LA(1);
									if ( (LA76_0=='0') ) {
										alt76=1;
									}
									switch (alt76) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1767:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1767:41: ( '0' ( '0' )? )?
											int alt75=2;
											int LA75_0 = input.LA(1);
											if ( (LA75_0=='0') ) {
												alt75=1;
											}
											switch (alt75) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1767:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1767:46: ( '0' )?
													int alt74=2;
													int LA74_0 = input.LA(1);
													if ( (LA74_0=='0') ) {
														alt74=1;
													}
													switch (alt74) {
														case 1 :
															// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1767:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "L"

	// $ANTLR start "M"
	public final void mM() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1770:17: ( ( 'm' | 'M' ) | '\\\\' ( 'm' | 'M' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' ) ) )
			int alt85=2;
			int LA85_0 = input.LA(1);
			if ( (LA85_0=='M'||LA85_0=='m') ) {
				alt85=1;
			}
			else if ( (LA85_0=='\\') ) {
				alt85=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 85, 0, input);
				throw nvae;
			}

			switch (alt85) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1770:21: ( 'm' | 'M' )
					{
					if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1771:19: '\\\\' ( 'm' | 'M' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1772:25: ( 'm' | 'M' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' ) )
					int alt84=3;
					switch ( input.LA(1) ) {
					case 'm':
						{
						alt84=1;
						}
						break;
					case 'M':
						{
						alt84=2;
						}
						break;
					case '0':
					case '4':
					case '6':
						{
						alt84=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 84, 0, input);
						throw nvae;
					}
					switch (alt84) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1773:31: 'm'
							{
							match('m'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1774:31: 'M'
							{
							match('M'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1775:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' )
							{
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1775:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt83=2;
							int LA83_0 = input.LA(1);
							if ( (LA83_0=='0') ) {
								alt83=1;
							}
							switch (alt83) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1775:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1775:36: ( '0' ( '0' ( '0' )? )? )?
									int alt82=2;
									int LA82_0 = input.LA(1);
									if ( (LA82_0=='0') ) {
										alt82=1;
									}
									switch (alt82) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1775:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1775:41: ( '0' ( '0' )? )?
											int alt81=2;
											int LA81_0 = input.LA(1);
											if ( (LA81_0=='0') ) {
												alt81=1;
											}
											switch (alt81) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1775:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1775:46: ( '0' )?
													int alt80=2;
													int LA80_0 = input.LA(1);
													if ( (LA80_0=='0') ) {
														alt80=1;
													}
													switch (alt80) {
														case 1 :
															// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1775:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "M"

	// $ANTLR start "N"
	public final void mN() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1778:17: ( ( 'n' | 'N' ) | '\\\\' ( 'n' | 'N' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' ) ) )
			int alt91=2;
			int LA91_0 = input.LA(1);
			if ( (LA91_0=='N'||LA91_0=='n') ) {
				alt91=1;
			}
			else if ( (LA91_0=='\\') ) {
				alt91=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 91, 0, input);
				throw nvae;
			}

			switch (alt91) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1778:21: ( 'n' | 'N' )
					{
					if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1779:19: '\\\\' ( 'n' | 'N' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1780:25: ( 'n' | 'N' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' ) )
					int alt90=3;
					switch ( input.LA(1) ) {
					case 'n':
						{
						alt90=1;
						}
						break;
					case 'N':
						{
						alt90=2;
						}
						break;
					case '0':
					case '4':
					case '6':
						{
						alt90=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 90, 0, input);
						throw nvae;
					}
					switch (alt90) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1781:31: 'n'
							{
							match('n'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1782:31: 'N'
							{
							match('N'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1783:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' )
							{
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1783:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt89=2;
							int LA89_0 = input.LA(1);
							if ( (LA89_0=='0') ) {
								alt89=1;
							}
							switch (alt89) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1783:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1783:36: ( '0' ( '0' ( '0' )? )? )?
									int alt88=2;
									int LA88_0 = input.LA(1);
									if ( (LA88_0=='0') ) {
										alt88=1;
									}
									switch (alt88) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1783:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1783:41: ( '0' ( '0' )? )?
											int alt87=2;
											int LA87_0 = input.LA(1);
											if ( (LA87_0=='0') ) {
												alt87=1;
											}
											switch (alt87) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1783:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1783:46: ( '0' )?
													int alt86=2;
													int LA86_0 = input.LA(1);
													if ( (LA86_0=='0') ) {
														alt86=1;
													}
													switch (alt86) {
														case 1 :
															// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1783:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "N"

	// $ANTLR start "O"
	public final void mO() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1786:17: ( ( 'o' | 'O' ) | '\\\\' ( 'o' | 'O' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' ) ) )
			int alt97=2;
			int LA97_0 = input.LA(1);
			if ( (LA97_0=='O'||LA97_0=='o') ) {
				alt97=1;
			}
			else if ( (LA97_0=='\\') ) {
				alt97=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 97, 0, input);
				throw nvae;
			}

			switch (alt97) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1786:21: ( 'o' | 'O' )
					{
					if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1787:19: '\\\\' ( 'o' | 'O' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1788:25: ( 'o' | 'O' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' ) )
					int alt96=3;
					switch ( input.LA(1) ) {
					case 'o':
						{
						alt96=1;
						}
						break;
					case 'O':
						{
						alt96=2;
						}
						break;
					case '0':
					case '4':
					case '6':
						{
						alt96=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 96, 0, input);
						throw nvae;
					}
					switch (alt96) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1789:31: 'o'
							{
							match('o'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1790:31: 'O'
							{
							match('O'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1791:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' )
							{
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1791:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt95=2;
							int LA95_0 = input.LA(1);
							if ( (LA95_0=='0') ) {
								alt95=1;
							}
							switch (alt95) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1791:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1791:36: ( '0' ( '0' ( '0' )? )? )?
									int alt94=2;
									int LA94_0 = input.LA(1);
									if ( (LA94_0=='0') ) {
										alt94=1;
									}
									switch (alt94) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1791:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1791:41: ( '0' ( '0' )? )?
											int alt93=2;
											int LA93_0 = input.LA(1);
											if ( (LA93_0=='0') ) {
												alt93=1;
											}
											switch (alt93) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1791:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1791:46: ( '0' )?
													int alt92=2;
													int LA92_0 = input.LA(1);
													if ( (LA92_0=='0') ) {
														alt92=1;
													}
													switch (alt92) {
														case 1 :
															// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1791:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "O"

	// $ANTLR start "P"
	public final void mP() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1794:17: ( ( 'p' | 'P' ) | '\\\\' ( 'p' | 'P' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' ) ) )
			int alt103=2;
			int LA103_0 = input.LA(1);
			if ( (LA103_0=='P'||LA103_0=='p') ) {
				alt103=1;
			}
			else if ( (LA103_0=='\\') ) {
				alt103=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 103, 0, input);
				throw nvae;
			}

			switch (alt103) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1794:21: ( 'p' | 'P' )
					{
					if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1795:19: '\\\\' ( 'p' | 'P' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1796:25: ( 'p' | 'P' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' ) )
					int alt102=3;
					switch ( input.LA(1) ) {
					case 'p':
						{
						alt102=1;
						}
						break;
					case 'P':
						{
						alt102=2;
						}
						break;
					case '0':
					case '5':
					case '7':
						{
						alt102=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 102, 0, input);
						throw nvae;
					}
					switch (alt102) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1797:31: 'p'
							{
							match('p'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1798:31: 'P'
							{
							match('P'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1799:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' )
							{
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1799:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt101=2;
							int LA101_0 = input.LA(1);
							if ( (LA101_0=='0') ) {
								alt101=1;
							}
							switch (alt101) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1799:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1799:36: ( '0' ( '0' ( '0' )? )? )?
									int alt100=2;
									int LA100_0 = input.LA(1);
									if ( (LA100_0=='0') ) {
										alt100=1;
									}
									switch (alt100) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1799:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1799:41: ( '0' ( '0' )? )?
											int alt99=2;
											int LA99_0 = input.LA(1);
											if ( (LA99_0=='0') ) {
												alt99=1;
											}
											switch (alt99) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1799:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1799:46: ( '0' )?
													int alt98=2;
													int LA98_0 = input.LA(1);
													if ( (LA98_0=='0') ) {
														alt98=1;
													}
													switch (alt98) {
														case 1 :
															// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1799:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1799:66: ( '0' )
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1799:67: '0'
							{
							match('0'); if (state.failed) return;
							}

							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "P"

	// $ANTLR start "Q"
	public final void mQ() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1802:17: ( ( 'q' | 'Q' ) | '\\\\' ( 'q' | 'Q' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' ) ) )
			int alt109=2;
			int LA109_0 = input.LA(1);
			if ( (LA109_0=='Q'||LA109_0=='q') ) {
				alt109=1;
			}
			else if ( (LA109_0=='\\') ) {
				alt109=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 109, 0, input);
				throw nvae;
			}

			switch (alt109) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1802:21: ( 'q' | 'Q' )
					{
					if ( input.LA(1)=='Q'||input.LA(1)=='q' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1803:19: '\\\\' ( 'q' | 'Q' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1804:25: ( 'q' | 'Q' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' ) )
					int alt108=3;
					switch ( input.LA(1) ) {
					case 'q':
						{
						alt108=1;
						}
						break;
					case 'Q':
						{
						alt108=2;
						}
						break;
					case '0':
					case '5':
					case '7':
						{
						alt108=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 108, 0, input);
						throw nvae;
					}
					switch (alt108) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1805:31: 'q'
							{
							match('q'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1806:31: 'Q'
							{
							match('Q'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1807:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' )
							{
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1807:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt107=2;
							int LA107_0 = input.LA(1);
							if ( (LA107_0=='0') ) {
								alt107=1;
							}
							switch (alt107) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1807:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1807:36: ( '0' ( '0' ( '0' )? )? )?
									int alt106=2;
									int LA106_0 = input.LA(1);
									if ( (LA106_0=='0') ) {
										alt106=1;
									}
									switch (alt106) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1807:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1807:41: ( '0' ( '0' )? )?
											int alt105=2;
											int LA105_0 = input.LA(1);
											if ( (LA105_0=='0') ) {
												alt105=1;
											}
											switch (alt105) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1807:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1807:46: ( '0' )?
													int alt104=2;
													int LA104_0 = input.LA(1);
													if ( (LA104_0=='0') ) {
														alt104=1;
													}
													switch (alt104) {
														case 1 :
															// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1807:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1807:66: ( '1' )
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1807:67: '1'
							{
							match('1'); if (state.failed) return;
							}

							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Q"

	// $ANTLR start "R"
	public final void mR() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1810:17: ( ( 'r' | 'R' ) | '\\\\' ( 'r' | 'R' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' ) ) )
			int alt115=2;
			int LA115_0 = input.LA(1);
			if ( (LA115_0=='R'||LA115_0=='r') ) {
				alt115=1;
			}
			else if ( (LA115_0=='\\') ) {
				alt115=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 115, 0, input);
				throw nvae;
			}

			switch (alt115) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1810:21: ( 'r' | 'R' )
					{
					if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1811:19: '\\\\' ( 'r' | 'R' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1812:25: ( 'r' | 'R' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' ) )
					int alt114=3;
					switch ( input.LA(1) ) {
					case 'r':
						{
						alt114=1;
						}
						break;
					case 'R':
						{
						alt114=2;
						}
						break;
					case '0':
					case '5':
					case '7':
						{
						alt114=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 114, 0, input);
						throw nvae;
					}
					switch (alt114) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1813:31: 'r'
							{
							match('r'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1814:31: 'R'
							{
							match('R'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1815:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' )
							{
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1815:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt113=2;
							int LA113_0 = input.LA(1);
							if ( (LA113_0=='0') ) {
								alt113=1;
							}
							switch (alt113) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1815:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1815:36: ( '0' ( '0' ( '0' )? )? )?
									int alt112=2;
									int LA112_0 = input.LA(1);
									if ( (LA112_0=='0') ) {
										alt112=1;
									}
									switch (alt112) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1815:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1815:41: ( '0' ( '0' )? )?
											int alt111=2;
											int LA111_0 = input.LA(1);
											if ( (LA111_0=='0') ) {
												alt111=1;
											}
											switch (alt111) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1815:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1815:46: ( '0' )?
													int alt110=2;
													int LA110_0 = input.LA(1);
													if ( (LA110_0=='0') ) {
														alt110=1;
													}
													switch (alt110) {
														case 1 :
															// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1815:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1815:66: ( '2' )
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1815:67: '2'
							{
							match('2'); if (state.failed) return;
							}

							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "R"

	// $ANTLR start "S"
	public final void mS() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1818:17: ( ( 's' | 'S' ) | '\\\\' ( 's' | 'S' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' ) ) )
			int alt121=2;
			int LA121_0 = input.LA(1);
			if ( (LA121_0=='S'||LA121_0=='s') ) {
				alt121=1;
			}
			else if ( (LA121_0=='\\') ) {
				alt121=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 121, 0, input);
				throw nvae;
			}

			switch (alt121) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1818:21: ( 's' | 'S' )
					{
					if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1819:19: '\\\\' ( 's' | 'S' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1820:25: ( 's' | 'S' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' ) )
					int alt120=3;
					switch ( input.LA(1) ) {
					case 's':
						{
						alt120=1;
						}
						break;
					case 'S':
						{
						alt120=2;
						}
						break;
					case '0':
					case '5':
					case '7':
						{
						alt120=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 120, 0, input);
						throw nvae;
					}
					switch (alt120) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1821:31: 's'
							{
							match('s'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1822:31: 'S'
							{
							match('S'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1823:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' )
							{
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1823:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt119=2;
							int LA119_0 = input.LA(1);
							if ( (LA119_0=='0') ) {
								alt119=1;
							}
							switch (alt119) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1823:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1823:36: ( '0' ( '0' ( '0' )? )? )?
									int alt118=2;
									int LA118_0 = input.LA(1);
									if ( (LA118_0=='0') ) {
										alt118=1;
									}
									switch (alt118) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1823:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1823:41: ( '0' ( '0' )? )?
											int alt117=2;
											int LA117_0 = input.LA(1);
											if ( (LA117_0=='0') ) {
												alt117=1;
											}
											switch (alt117) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1823:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1823:46: ( '0' )?
													int alt116=2;
													int LA116_0 = input.LA(1);
													if ( (LA116_0=='0') ) {
														alt116=1;
													}
													switch (alt116) {
														case 1 :
															// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1823:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1823:66: ( '3' )
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1823:67: '3'
							{
							match('3'); if (state.failed) return;
							}

							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "S"

	// $ANTLR start "T"
	public final void mT() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1826:17: ( ( 't' | 'T' ) | '\\\\' ( 't' | 'T' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' ) ) )
			int alt127=2;
			int LA127_0 = input.LA(1);
			if ( (LA127_0=='T'||LA127_0=='t') ) {
				alt127=1;
			}
			else if ( (LA127_0=='\\') ) {
				alt127=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 127, 0, input);
				throw nvae;
			}

			switch (alt127) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1826:21: ( 't' | 'T' )
					{
					if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1827:19: '\\\\' ( 't' | 'T' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1828:25: ( 't' | 'T' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' ) )
					int alt126=3;
					switch ( input.LA(1) ) {
					case 't':
						{
						alt126=1;
						}
						break;
					case 'T':
						{
						alt126=2;
						}
						break;
					case '0':
					case '5':
					case '7':
						{
						alt126=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 126, 0, input);
						throw nvae;
					}
					switch (alt126) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1829:31: 't'
							{
							match('t'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1830:31: 'T'
							{
							match('T'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1831:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' )
							{
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1831:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt125=2;
							int LA125_0 = input.LA(1);
							if ( (LA125_0=='0') ) {
								alt125=1;
							}
							switch (alt125) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1831:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1831:36: ( '0' ( '0' ( '0' )? )? )?
									int alt124=2;
									int LA124_0 = input.LA(1);
									if ( (LA124_0=='0') ) {
										alt124=1;
									}
									switch (alt124) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1831:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1831:41: ( '0' ( '0' )? )?
											int alt123=2;
											int LA123_0 = input.LA(1);
											if ( (LA123_0=='0') ) {
												alt123=1;
											}
											switch (alt123) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1831:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1831:46: ( '0' )?
													int alt122=2;
													int LA122_0 = input.LA(1);
													if ( (LA122_0=='0') ) {
														alt122=1;
													}
													switch (alt122) {
														case 1 :
															// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1831:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1831:66: ( '4' )
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1831:67: '4'
							{
							match('4'); if (state.failed) return;
							}

							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T"

	// $ANTLR start "U"
	public final void mU() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1834:17: ( ( 'u' | 'U' ) | '\\\\' ( 'u' | 'U' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' ) ) )
			int alt133=2;
			int LA133_0 = input.LA(1);
			if ( (LA133_0=='U'||LA133_0=='u') ) {
				alt133=1;
			}
			else if ( (LA133_0=='\\') ) {
				alt133=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 133, 0, input);
				throw nvae;
			}

			switch (alt133) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1834:21: ( 'u' | 'U' )
					{
					if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1835:19: '\\\\' ( 'u' | 'U' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1836:25: ( 'u' | 'U' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' ) )
					int alt132=3;
					switch ( input.LA(1) ) {
					case 'u':
						{
						alt132=1;
						}
						break;
					case 'U':
						{
						alt132=2;
						}
						break;
					case '0':
					case '5':
					case '7':
						{
						alt132=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 132, 0, input);
						throw nvae;
					}
					switch (alt132) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1837:31: 'u'
							{
							match('u'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1838:31: 'U'
							{
							match('U'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1839:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' )
							{
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1839:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt131=2;
							int LA131_0 = input.LA(1);
							if ( (LA131_0=='0') ) {
								alt131=1;
							}
							switch (alt131) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1839:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1839:36: ( '0' ( '0' ( '0' )? )? )?
									int alt130=2;
									int LA130_0 = input.LA(1);
									if ( (LA130_0=='0') ) {
										alt130=1;
									}
									switch (alt130) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1839:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1839:41: ( '0' ( '0' )? )?
											int alt129=2;
											int LA129_0 = input.LA(1);
											if ( (LA129_0=='0') ) {
												alt129=1;
											}
											switch (alt129) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1839:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1839:46: ( '0' )?
													int alt128=2;
													int LA128_0 = input.LA(1);
													if ( (LA128_0=='0') ) {
														alt128=1;
													}
													switch (alt128) {
														case 1 :
															// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1839:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1839:66: ( '5' )
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1839:67: '5'
							{
							match('5'); if (state.failed) return;
							}

							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "U"

	// $ANTLR start "V"
	public final void mV() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1842:17: ( ( 'v' | 'V' ) | '\\\\' ( 'v' | 'V' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' ) ) )
			int alt139=2;
			int LA139_0 = input.LA(1);
			if ( (LA139_0=='V'||LA139_0=='v') ) {
				alt139=1;
			}
			else if ( (LA139_0=='\\') ) {
				alt139=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 139, 0, input);
				throw nvae;
			}

			switch (alt139) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1842:21: ( 'v' | 'V' )
					{
					if ( input.LA(1)=='V'||input.LA(1)=='v' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1843:19: '\\\\' ( 'v' | 'V' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1844:25: ( 'v' | 'V' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' ) )
					int alt138=3;
					switch ( input.LA(1) ) {
					case 'v':
						{
						alt138=1;
						}
						break;
					case 'V':
						{
						alt138=2;
						}
						break;
					case '0':
					case '5':
					case '7':
						{
						alt138=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 138, 0, input);
						throw nvae;
					}
					switch (alt138) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1844:31: 'v'
							{
							match('v'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1845:31: 'V'
							{
							match('V'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1846:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' )
							{
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1846:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt137=2;
							int LA137_0 = input.LA(1);
							if ( (LA137_0=='0') ) {
								alt137=1;
							}
							switch (alt137) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1846:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1846:36: ( '0' ( '0' ( '0' )? )? )?
									int alt136=2;
									int LA136_0 = input.LA(1);
									if ( (LA136_0=='0') ) {
										alt136=1;
									}
									switch (alt136) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1846:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1846:41: ( '0' ( '0' )? )?
											int alt135=2;
											int LA135_0 = input.LA(1);
											if ( (LA135_0=='0') ) {
												alt135=1;
											}
											switch (alt135) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1846:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1846:46: ( '0' )?
													int alt134=2;
													int LA134_0 = input.LA(1);
													if ( (LA134_0=='0') ) {
														alt134=1;
													}
													switch (alt134) {
														case 1 :
															// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1846:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1846:66: ( '6' )
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1846:67: '6'
							{
							match('6'); if (state.failed) return;
							}

							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "V"

	// $ANTLR start "W"
	public final void mW() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1849:17: ( ( 'w' | 'W' ) | '\\\\' ( 'w' | 'W' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' ) ) )
			int alt145=2;
			int LA145_0 = input.LA(1);
			if ( (LA145_0=='W'||LA145_0=='w') ) {
				alt145=1;
			}
			else if ( (LA145_0=='\\') ) {
				alt145=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 145, 0, input);
				throw nvae;
			}

			switch (alt145) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1849:21: ( 'w' | 'W' )
					{
					if ( input.LA(1)=='W'||input.LA(1)=='w' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1850:19: '\\\\' ( 'w' | 'W' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1851:25: ( 'w' | 'W' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' ) )
					int alt144=3;
					switch ( input.LA(1) ) {
					case 'w':
						{
						alt144=1;
						}
						break;
					case 'W':
						{
						alt144=2;
						}
						break;
					case '0':
					case '5':
					case '7':
						{
						alt144=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 144, 0, input);
						throw nvae;
					}
					switch (alt144) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1852:31: 'w'
							{
							match('w'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1853:31: 'W'
							{
							match('W'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1854:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' )
							{
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1854:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt143=2;
							int LA143_0 = input.LA(1);
							if ( (LA143_0=='0') ) {
								alt143=1;
							}
							switch (alt143) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1854:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1854:36: ( '0' ( '0' ( '0' )? )? )?
									int alt142=2;
									int LA142_0 = input.LA(1);
									if ( (LA142_0=='0') ) {
										alt142=1;
									}
									switch (alt142) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1854:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1854:41: ( '0' ( '0' )? )?
											int alt141=2;
											int LA141_0 = input.LA(1);
											if ( (LA141_0=='0') ) {
												alt141=1;
											}
											switch (alt141) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1854:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1854:46: ( '0' )?
													int alt140=2;
													int LA140_0 = input.LA(1);
													if ( (LA140_0=='0') ) {
														alt140=1;
													}
													switch (alt140) {
														case 1 :
															// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1854:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1854:66: ( '7' )
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1854:67: '7'
							{
							match('7'); if (state.failed) return;
							}

							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "W"

	// $ANTLR start "X"
	public final void mX() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1857:17: ( ( 'x' | 'X' ) | '\\\\' ( 'x' | 'X' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' ) ) )
			int alt151=2;
			int LA151_0 = input.LA(1);
			if ( (LA151_0=='X'||LA151_0=='x') ) {
				alt151=1;
			}
			else if ( (LA151_0=='\\') ) {
				alt151=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 151, 0, input);
				throw nvae;
			}

			switch (alt151) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1857:21: ( 'x' | 'X' )
					{
					if ( input.LA(1)=='X'||input.LA(1)=='x' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1858:19: '\\\\' ( 'x' | 'X' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1859:25: ( 'x' | 'X' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' ) )
					int alt150=3;
					switch ( input.LA(1) ) {
					case 'x':
						{
						alt150=1;
						}
						break;
					case 'X':
						{
						alt150=2;
						}
						break;
					case '0':
					case '5':
					case '7':
						{
						alt150=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 150, 0, input);
						throw nvae;
					}
					switch (alt150) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1860:31: 'x'
							{
							match('x'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1861:31: 'X'
							{
							match('X'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1862:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' )
							{
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1862:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt149=2;
							int LA149_0 = input.LA(1);
							if ( (LA149_0=='0') ) {
								alt149=1;
							}
							switch (alt149) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1862:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1862:36: ( '0' ( '0' ( '0' )? )? )?
									int alt148=2;
									int LA148_0 = input.LA(1);
									if ( (LA148_0=='0') ) {
										alt148=1;
									}
									switch (alt148) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1862:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1862:41: ( '0' ( '0' )? )?
											int alt147=2;
											int LA147_0 = input.LA(1);
											if ( (LA147_0=='0') ) {
												alt147=1;
											}
											switch (alt147) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1862:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1862:46: ( '0' )?
													int alt146=2;
													int LA146_0 = input.LA(1);
													if ( (LA146_0=='0') ) {
														alt146=1;
													}
													switch (alt146) {
														case 1 :
															// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1862:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1862:66: ( '8' )
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1862:67: '8'
							{
							match('8'); if (state.failed) return;
							}

							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "X"

	// $ANTLR start "Y"
	public final void mY() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1865:17: ( ( 'y' | 'Y' ) | '\\\\' ( 'y' | 'Y' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' ) ) )
			int alt157=2;
			int LA157_0 = input.LA(1);
			if ( (LA157_0=='Y'||LA157_0=='y') ) {
				alt157=1;
			}
			else if ( (LA157_0=='\\') ) {
				alt157=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 157, 0, input);
				throw nvae;
			}

			switch (alt157) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1865:21: ( 'y' | 'Y' )
					{
					if ( input.LA(1)=='Y'||input.LA(1)=='y' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1866:19: '\\\\' ( 'y' | 'Y' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1867:25: ( 'y' | 'Y' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' ) )
					int alt156=3;
					switch ( input.LA(1) ) {
					case 'y':
						{
						alt156=1;
						}
						break;
					case 'Y':
						{
						alt156=2;
						}
						break;
					case '0':
					case '5':
					case '7':
						{
						alt156=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 156, 0, input);
						throw nvae;
					}
					switch (alt156) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1868:31: 'y'
							{
							match('y'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1869:31: 'Y'
							{
							match('Y'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1870:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' )
							{
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1870:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt155=2;
							int LA155_0 = input.LA(1);
							if ( (LA155_0=='0') ) {
								alt155=1;
							}
							switch (alt155) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1870:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1870:36: ( '0' ( '0' ( '0' )? )? )?
									int alt154=2;
									int LA154_0 = input.LA(1);
									if ( (LA154_0=='0') ) {
										alt154=1;
									}
									switch (alt154) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1870:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1870:41: ( '0' ( '0' )? )?
											int alt153=2;
											int LA153_0 = input.LA(1);
											if ( (LA153_0=='0') ) {
												alt153=1;
											}
											switch (alt153) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1870:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1870:46: ( '0' )?
													int alt152=2;
													int LA152_0 = input.LA(1);
													if ( (LA152_0=='0') ) {
														alt152=1;
													}
													switch (alt152) {
														case 1 :
															// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1870:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1870:66: ( '9' )
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1870:67: '9'
							{
							match('9'); if (state.failed) return;
							}

							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Y"

	// $ANTLR start "Z"
	public final void mZ() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1873:17: ( ( 'z' | 'Z' ) | '\\\\' ( 'z' | 'Z' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' ) ) )
			int alt163=2;
			int LA163_0 = input.LA(1);
			if ( (LA163_0=='Z'||LA163_0=='z') ) {
				alt163=1;
			}
			else if ( (LA163_0=='\\') ) {
				alt163=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 163, 0, input);
				throw nvae;
			}

			switch (alt163) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1873:21: ( 'z' | 'Z' )
					{
					if ( input.LA(1)=='Z'||input.LA(1)=='z' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1874:19: '\\\\' ( 'z' | 'Z' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1875:25: ( 'z' | 'Z' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' ) )
					int alt162=3;
					switch ( input.LA(1) ) {
					case 'z':
						{
						alt162=1;
						}
						break;
					case 'Z':
						{
						alt162=2;
						}
						break;
					case '0':
					case '5':
					case '7':
						{
						alt162=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 162, 0, input);
						throw nvae;
					}
					switch (alt162) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1876:31: 'z'
							{
							match('z'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1877:31: 'Z'
							{
							match('Z'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1878:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' )
							{
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1878:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt161=2;
							int LA161_0 = input.LA(1);
							if ( (LA161_0=='0') ) {
								alt161=1;
							}
							switch (alt161) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1878:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1878:36: ( '0' ( '0' ( '0' )? )? )?
									int alt160=2;
									int LA160_0 = input.LA(1);
									if ( (LA160_0=='0') ) {
										alt160=1;
									}
									switch (alt160) {
										case 1 :
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1878:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1878:41: ( '0' ( '0' )? )?
											int alt159=2;
											int LA159_0 = input.LA(1);
											if ( (LA159_0=='0') ) {
												alt159=1;
											}
											switch (alt159) {
												case 1 :
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1878:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1878:46: ( '0' )?
													int alt158=2;
													int LA158_0 = input.LA(1);
													if ( (LA158_0=='0') ) {
														alt158=1;
													}
													switch (alt158) {
														case 1 :
															// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1878:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Z"

	// $ANTLR start "CDO"
	public final void mCDO() throws RecognitionException {
		try {
			int _type = CDO;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1890:17: ( '<!--' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1890:19: '<!--'
			{
			match("<!--"); if (state.failed) return;

			if ( state.backtracking==0 ) {
			                        _channel = 3;   // CDO on channel 3 in case we want it later
			                    }
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CDO"

	// $ANTLR start "CDC"
	public final void mCDC() throws RecognitionException {
		try {
			int _type = CDC;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1903:17: ( '-->' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1903:19: '-->'
			{
			match("-->"); if (state.failed) return;

			if ( state.backtracking==0 ) {
			                        _channel = 4;   // CDC on channel 4 in case we want it later
			                    }
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CDC"

	// $ANTLR start "INCLUDES"
	public final void mINCLUDES() throws RecognitionException {
		try {
			int _type = INCLUDES;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1910:17: ( '~=' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1910:19: '~='
			{
			match("~="); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "INCLUDES"

	// $ANTLR start "DASHMATCH"
	public final void mDASHMATCH() throws RecognitionException {
		try {
			int _type = DASHMATCH;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1911:17: ( '|=' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1911:19: '|='
			{
			match("|="); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DASHMATCH"

	// $ANTLR start "BEGINS"
	public final void mBEGINS() throws RecognitionException {
		try {
			int _type = BEGINS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1912:17: ( '^=' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1912:19: '^='
			{
			match("^="); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BEGINS"

	// $ANTLR start "ENDS"
	public final void mENDS() throws RecognitionException {
		try {
			int _type = ENDS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1913:17: ( '$=' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1913:19: '$='
			{
			match("$="); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ENDS"

	// $ANTLR start "CONTAINS"
	public final void mCONTAINS() throws RecognitionException {
		try {
			int _type = CONTAINS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1914:17: ( '*=' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1914:19: '*='
			{
			match("*="); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CONTAINS"

	// $ANTLR start "GREATER"
	public final void mGREATER() throws RecognitionException {
		try {
			int _type = GREATER;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1916:17: ( '>' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1916:19: '>'
			{
			match('>'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "GREATER"

	// $ANTLR start "LBRACE"
	public final void mLBRACE() throws RecognitionException {
		try {
			int _type = LBRACE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1917:17: ( '{' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1917:19: '{'
			{
			match('{'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LBRACE"

	// $ANTLR start "RBRACE"
	public final void mRBRACE() throws RecognitionException {
		try {
			int _type = RBRACE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1918:17: ( '}' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1918:19: '}'
			{
			match('}'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RBRACE"

	// $ANTLR start "LBRACKET"
	public final void mLBRACKET() throws RecognitionException {
		try {
			int _type = LBRACKET;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1919:17: ( '[' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1919:19: '['
			{
			match('['); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LBRACKET"

	// $ANTLR start "RBRACKET"
	public final void mRBRACKET() throws RecognitionException {
		try {
			int _type = RBRACKET;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1920:17: ( ']' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1920:19: ']'
			{
			match(']'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RBRACKET"

	// $ANTLR start "OPEQ"
	public final void mOPEQ() throws RecognitionException {
		try {
			int _type = OPEQ;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1921:17: ( '=' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1921:19: '='
			{
			match('='); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "OPEQ"

	// $ANTLR start "SEMI"
	public final void mSEMI() throws RecognitionException {
		try {
			int _type = SEMI;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1922:17: ( ';' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1922:19: ';'
			{
			match(';'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SEMI"

	// $ANTLR start "COLON"
	public final void mCOLON() throws RecognitionException {
		try {
			int _type = COLON;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1923:17: ( ':' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1923:19: ':'
			{
			match(':'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "COLON"

	// $ANTLR start "DCOLON"
	public final void mDCOLON() throws RecognitionException {
		try {
			int _type = DCOLON;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1924:17: ( '::' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1924:19: '::'
			{
			match("::"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DCOLON"

	// $ANTLR start "SOLIDUS"
	public final void mSOLIDUS() throws RecognitionException {
		try {
			int _type = SOLIDUS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1925:17: ( '/' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1925:19: '/'
			{
			match('/'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SOLIDUS"

	// $ANTLR start "MINUS"
	public final void mMINUS() throws RecognitionException {
		try {
			int _type = MINUS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1926:17: ( '-' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1926:19: '-'
			{
			match('-'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "MINUS"

	// $ANTLR start "PLUS"
	public final void mPLUS() throws RecognitionException {
		try {
			int _type = PLUS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1927:17: ( '+' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1927:19: '+'
			{
			match('+'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "PLUS"

	// $ANTLR start "STAR"
	public final void mSTAR() throws RecognitionException {
		try {
			int _type = STAR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1928:17: ( '*' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1928:19: '*'
			{
			match('*'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STAR"

	// $ANTLR start "LPAREN"
	public final void mLPAREN() throws RecognitionException {
		try {
			int _type = LPAREN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1929:17: ( '(' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1929:19: '('
			{
			match('('); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LPAREN"

	// $ANTLR start "RPAREN"
	public final void mRPAREN() throws RecognitionException {
		try {
			int _type = RPAREN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1930:17: ( ')' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1930:19: ')'
			{
			match(')'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RPAREN"

	// $ANTLR start "COMMA"
	public final void mCOMMA() throws RecognitionException {
		try {
			int _type = COMMA;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1931:17: ( ',' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1931:19: ','
			{
			match(','); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "COMMA"

	// $ANTLR start "DOT"
	public final void mDOT() throws RecognitionException {
		try {
			int _type = DOT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1932:17: ( '.' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1932:19: '.'
			{
			match('.'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DOT"

	// $ANTLR start "TILDE"
	public final void mTILDE() throws RecognitionException {
		try {
			int _type = TILDE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1933:8: ( '~' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1933:10: '~'
			{
			match('~'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TILDE"

	// $ANTLR start "PIPE"
	public final void mPIPE() throws RecognitionException {
		try {
			int _type = PIPE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1934:17: ( '|' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1934:19: '|'
			{
			match('|'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "PIPE"

	// $ANTLR start "PERCENTAGE_SYMBOL"
	public final void mPERCENTAGE_SYMBOL() throws RecognitionException {
		try {
			int _type = PERCENTAGE_SYMBOL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1936:17: ( '%' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1936:19: '%'
			{
			match('%'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "PERCENTAGE_SYMBOL"

	// $ANTLR start "EXCLAMATION_MARK"
	public final void mEXCLAMATION_MARK() throws RecognitionException {
		try {
			int _type = EXCLAMATION_MARK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1937:17: ( '!' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1937:19: '!'
			{
			match('!'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "EXCLAMATION_MARK"

	// $ANTLR start "CP_EQ"
	public final void mCP_EQ() throws RecognitionException {
		try {
			int _type = CP_EQ;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1939:17: ( '==' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1939:19: '=='
			{
			match("=="); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CP_EQ"

	// $ANTLR start "CP_NOT_EQ"
	public final void mCP_NOT_EQ() throws RecognitionException {
		try {
			int _type = CP_NOT_EQ;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1940:17: ( '!=' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1940:19: '!='
			{
			match("!="); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CP_NOT_EQ"

	// $ANTLR start "LESS"
	public final void mLESS() throws RecognitionException {
		try {
			int _type = LESS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1941:17: ( '<' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1941:19: '<'
			{
			match('<'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LESS"

	// $ANTLR start "GREATER_OR_EQ"
	public final void mGREATER_OR_EQ() throws RecognitionException {
		try {
			int _type = GREATER_OR_EQ;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1942:17: ( '>=' | '=>' )
			int alt164=2;
			int LA164_0 = input.LA(1);
			if ( (LA164_0=='>') ) {
				alt164=1;
			}
			else if ( (LA164_0=='=') ) {
				alt164=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 164, 0, input);
				throw nvae;
			}

			switch (alt164) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1942:19: '>='
					{
					match(">="); if (state.failed) return;

					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1942:26: '=>'
					{
					match("=>"); if (state.failed) return;

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "GREATER_OR_EQ"

	// $ANTLR start "LESS_OR_EQ"
	public final void mLESS_OR_EQ() throws RecognitionException {
		try {
			int _type = LESS_OR_EQ;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1943:17: ( '=<' | '<=' )
			int alt165=2;
			int LA165_0 = input.LA(1);
			if ( (LA165_0=='=') ) {
				alt165=1;
			}
			else if ( (LA165_0=='<') ) {
				alt165=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 165, 0, input);
				throw nvae;
			}

			switch (alt165) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1943:19: '=<'
					{
					match("=<"); if (state.failed) return;

					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1943:26: '<='
					{
					match("<="); if (state.failed) return;

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LESS_OR_EQ"

	// $ANTLR start "LESS_AND"
	public final void mLESS_AND() throws RecognitionException {
		try {
			int _type = LESS_AND;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1944:17: ( '&' ( '-' )* )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1944:19: '&' ( '-' )*
			{
			match('&'); if (state.failed) return;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1944:23: ( '-' )*
			loop166:
			while (true) {
				int alt166=2;
				int LA166_0 = input.LA(1);
				if ( (LA166_0=='-') ) {
					alt166=1;
				}

				switch (alt166) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1944:23: '-'
					{
					match('-'); if (state.failed) return;
					}
					break;

				default :
					break loop166;
				}
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LESS_AND"

	// $ANTLR start "CP_DOTS"
	public final void mCP_DOTS() throws RecognitionException {
		try {
			int _type = CP_DOTS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1945:17: ( '...' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1945:19: '...'
			{
			match("..."); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CP_DOTS"

	// $ANTLR start "LESS_REST"
	public final void mLESS_REST() throws RecognitionException {
		try {
			int _type = LESS_REST;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1946:17: ( '@rest...' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1946:19: '@rest...'
			{
			match("@rest..."); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LESS_REST"

	// $ANTLR start "INVALID"
	public final void mINVALID() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1951:21: ()
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1951:22: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "INVALID"

	// $ANTLR start "STRING"
	public final void mSTRING() throws RecognitionException {
		try {
			int _type = STRING;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1952:17: ( '\\'' (~ ( '\\r' | '\\f' | '\\'' ) )* ( '\\'' |) | '\"' ( ( '\\\\\\\"' )=> '\\\\\\\"' | ( '\\\\\\\\' )=> '\\\\\\\\' |~ ( '\\r' | '\\f' | '\"' ) )* ( '\"' |) )
			int alt171=2;
			int LA171_0 = input.LA(1);
			if ( (LA171_0=='\'') ) {
				alt171=1;
			}
			else if ( (LA171_0=='\"') ) {
				alt171=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 171, 0, input);
				throw nvae;
			}

			switch (alt171) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1952:19: '\\'' (~ ( '\\r' | '\\f' | '\\'' ) )* ( '\\'' |)
					{
					match('\''); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1952:24: (~ ( '\\r' | '\\f' | '\\'' ) )*
					loop167:
					while (true) {
						int alt167=2;
						int LA167_0 = input.LA(1);
						if ( ((LA167_0 >= '\u0000' && LA167_0 <= '\u000B')||(LA167_0 >= '\u000E' && LA167_0 <= '&')||(LA167_0 >= '(' && LA167_0 <= '\uFFFF')) ) {
							alt167=1;
						}

						switch (alt167) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
							{
							if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\u000B')||(input.LA(1) >= '\u000E' && input.LA(1) <= '&')||(input.LA(1) >= '(' && input.LA(1) <= '\uFFFF') ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

						default :
							break loop167;
						}
					}

					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1953:21: ( '\\'' |)
					int alt168=2;
					int LA168_0 = input.LA(1);
					if ( (LA168_0=='\'') ) {
						alt168=1;
					}

					else {
						alt168=2;
					}

					switch (alt168) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1954:27: '\\''
							{
							match('\''); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1955:27: 
							{
							if ( state.backtracking==0 ) { _type = INVALID; }
							}
							break;

					}

					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1958:19: '\"' ( ( '\\\\\\\"' )=> '\\\\\\\"' | ( '\\\\\\\\' )=> '\\\\\\\\' |~ ( '\\r' | '\\f' | '\"' ) )* ( '\"' |)
					{
					match('\"'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1958:24: ( ( '\\\\\\\"' )=> '\\\\\\\"' | ( '\\\\\\\\' )=> '\\\\\\\\' |~ ( '\\r' | '\\f' | '\"' ) )*
					loop169:
					while (true) {
						int alt169=4;
						int LA169_0 = input.LA(1);
						if ( (LA169_0=='\\') ) {
							switch ( input.LA(2) ) {
							case '\"':
								{
								int LA169_4 = input.LA(3);
								if ( (synpred1_Css3()) ) {
									alt169=1;
								}
								else if ( (true) ) {
									alt169=3;
								}

								}
								break;
							case '\\':
								{
								int LA169_5 = input.LA(3);
								if ( (synpred2_Css3()) ) {
									alt169=2;
								}
								else if ( (true) ) {
									alt169=3;
								}

								}
								break;
							default:
								alt169=3;
								break;
							}
						}
						else if ( ((LA169_0 >= '\u0000' && LA169_0 <= '\u000B')||(LA169_0 >= '\u000E' && LA169_0 <= '!')||(LA169_0 >= '#' && LA169_0 <= '[')||(LA169_0 >= ']' && LA169_0 <= '\uFFFF')) ) {
							alt169=3;
						}

						switch (alt169) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1958:26: ( '\\\\\\\"' )=> '\\\\\\\"'
							{
							match("\\\""); if (state.failed) return;

							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1958:47: ( '\\\\\\\\' )=> '\\\\\\\\'
							{
							match("\\\\"); if (state.failed) return;

							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1958:68: ~ ( '\\r' | '\\f' | '\"' )
							{
							if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\u000B')||(input.LA(1) >= '\u000E' && input.LA(1) <= '!')||(input.LA(1) >= '#' && input.LA(1) <= '\uFFFF') ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

						default :
							break loop169;
						}
					}

					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1959:21: ( '\"' |)
					int alt170=2;
					int LA170_0 = input.LA(1);
					if ( (LA170_0=='\"') ) {
						alt170=1;
					}

					else {
						alt170=2;
					}

					switch (alt170) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1960:27: '\"'
							{
							match('\"'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1961:27: 
							{
							if ( state.backtracking==0 ) { _type = INVALID; }
							}
							break;

					}

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STRING"

	// $ANTLR start "LESS_JS_STRING"
	public final void mLESS_JS_STRING() throws RecognitionException {
		try {
			int _type = LESS_JS_STRING;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1965:17: ( '`' (~ ( '\\r' | '\\f' | '`' ) )* ( '`' |) )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1965:19: '`' (~ ( '\\r' | '\\f' | '`' ) )* ( '`' |)
			{
			match('`'); if (state.failed) return;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1965:23: (~ ( '\\r' | '\\f' | '`' ) )*
			loop172:
			while (true) {
				int alt172=2;
				int LA172_0 = input.LA(1);
				if ( ((LA172_0 >= '\u0000' && LA172_0 <= '\u000B')||(LA172_0 >= '\u000E' && LA172_0 <= '_')||(LA172_0 >= 'a' && LA172_0 <= '\uFFFF')) ) {
					alt172=1;
				}

				switch (alt172) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\u000B')||(input.LA(1) >= '\u000E' && input.LA(1) <= '_')||(input.LA(1) >= 'a' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop172;
				}
			}

			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1966:21: ( '`' |)
			int alt173=2;
			int LA173_0 = input.LA(1);
			if ( (LA173_0=='`') ) {
				alt173=1;
			}

			else {
				alt173=2;
			}

			switch (alt173) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1967:27: '`'
					{
					match('`'); if (state.failed) return;
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1968:27: 
					{
					if ( state.backtracking==0 ) { _type = INVALID; }
					}
					break;

			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LESS_JS_STRING"

	// $ANTLR start "NOT"
	public final void mNOT() throws RecognitionException {
		try {
			int _type = NOT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1972:6: ( 'NOT' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1972:8: 'NOT'
			{
			match("NOT"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NOT"

	// $ANTLR start "VARIABLE"
	public final void mVARIABLE() throws RecognitionException {
		try {
			int _type = VARIABLE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1976:17: ( '--' NMSTART ( NMCHAR )* )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1976:19: '--' NMSTART ( NMCHAR )*
			{
			match("--"); if (state.failed) return;

			mNMSTART(); if (state.failed) return;

			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1976:32: ( NMCHAR )*
			loop174:
			while (true) {
				int alt174=2;
				int LA174_0 = input.LA(1);
				if ( (LA174_0=='-'||(LA174_0 >= '0' && LA174_0 <= '9')||(LA174_0 >= 'A' && LA174_0 <= 'Z')||LA174_0=='\\'||LA174_0=='_'||(LA174_0 >= 'a' && LA174_0 <= 'z')||(LA174_0 >= '\u0080' && LA174_0 <= '\uFFFF')) ) {
					alt174=1;
				}

				switch (alt174) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1976:32: NMCHAR
					{
					mNMCHAR(); if (state.failed) return;

					}
					break;

				default :
					break loop174;
				}
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "VARIABLE"

	// $ANTLR start "IDENT"
	public final void mIDENT() throws RecognitionException {
		try {
			int _type = IDENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1981:17: ( ( '-' )? NMSTART ( NMCHAR )* )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1981:19: ( '-' )? NMSTART ( NMCHAR )*
			{
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1981:19: ( '-' )?
			int alt175=2;
			int LA175_0 = input.LA(1);
			if ( (LA175_0=='-') ) {
				alt175=1;
			}
			switch (alt175) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1981:19: '-'
					{
					match('-'); if (state.failed) return;
					}
					break;

			}

			mNMSTART(); if (state.failed) return;

			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1981:32: ( NMCHAR )*
			loop176:
			while (true) {
				int alt176=2;
				int LA176_0 = input.LA(1);
				if ( (LA176_0=='-'||(LA176_0 >= '0' && LA176_0 <= '9')||(LA176_0 >= 'A' && LA176_0 <= 'Z')||LA176_0=='\\'||LA176_0=='_'||(LA176_0 >= 'a' && LA176_0 <= 'z')||(LA176_0 >= '\u0080' && LA176_0 <= '\uFFFF')) ) {
					alt176=1;
				}

				switch (alt176) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1981:32: NMCHAR
					{
					mNMCHAR(); if (state.failed) return;

					}
					break;

				default :
					break loop176;
				}
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "IDENT"

	// $ANTLR start "HASH_SYMBOL"
	public final void mHASH_SYMBOL() throws RecognitionException {
		try {
			int _type = HASH_SYMBOL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1986:17: ( '#' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1986:19: '#'
			{
			match('#'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "HASH_SYMBOL"

	// $ANTLR start "HASH"
	public final void mHASH() throws RecognitionException {
		try {
			int _type = HASH;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1987:17: ( HASH_SYMBOL NAME )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1987:19: HASH_SYMBOL NAME
			{
			mHASH_SYMBOL(); if (state.failed) return;

			mNAME(); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "HASH"

	// $ANTLR start "IMPORTANT_SYM"
	public final void mIMPORTANT_SYM() throws RecognitionException {
		try {
			int _type = IMPORTANT_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1989:17: ( EXCLAMATION_MARK ( WS | COMMENT )* 'IMPORTANT' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1989:19: EXCLAMATION_MARK ( WS | COMMENT )* 'IMPORTANT'
			{
			mEXCLAMATION_MARK(); if (state.failed) return;

			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1989:36: ( WS | COMMENT )*
			loop177:
			while (true) {
				int alt177=3;
				int LA177_0 = input.LA(1);
				if ( (LA177_0=='\t'||LA177_0==' ') ) {
					alt177=1;
				}
				else if ( (LA177_0=='/') ) {
					alt177=2;
				}

				switch (alt177) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1989:37: WS
					{
					mWS(); if (state.failed) return;

					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1989:40: COMMENT
					{
					mCOMMENT(); if (state.failed) return;

					}
					break;

				default :
					break loop177;
				}
			}

			match("IMPORTANT"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "IMPORTANT_SYM"

	// $ANTLR start "IMPORT_SYM"
	public final void mIMPORT_SYM() throws RecognitionException {
		try {
			int _type = IMPORT_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1991:21: ( '@IMPORT' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1991:23: '@IMPORT'
			{
			match("@IMPORT"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "IMPORT_SYM"

	// $ANTLR start "PAGE_SYM"
	public final void mPAGE_SYM() throws RecognitionException {
		try {
			int _type = PAGE_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1992:21: ( '@PAGE' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1992:23: '@PAGE'
			{
			match("@PAGE"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "PAGE_SYM"

	// $ANTLR start "MEDIA_SYM"
	public final void mMEDIA_SYM() throws RecognitionException {
		try {
			int _type = MEDIA_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1993:21: ( '@MEDIA' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1993:23: '@MEDIA'
			{
			match("@MEDIA"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "MEDIA_SYM"

	// $ANTLR start "NAMESPACE_SYM"
	public final void mNAMESPACE_SYM() throws RecognitionException {
		try {
			int _type = NAMESPACE_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1994:21: ( '@NAMESPACE' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1994:23: '@NAMESPACE'
			{
			match("@NAMESPACE"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NAMESPACE_SYM"

	// $ANTLR start "CHARSET_SYM"
	public final void mCHARSET_SYM() throws RecognitionException {
		try {
			int _type = CHARSET_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1995:21: ( '@CHARSET' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1995:23: '@CHARSET'
			{
			match("@CHARSET"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CHARSET_SYM"

	// $ANTLR start "COUNTER_STYLE_SYM"
	public final void mCOUNTER_STYLE_SYM() throws RecognitionException {
		try {
			int _type = COUNTER_STYLE_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1996:21: ( '@COUNTER-STYLE' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1996:23: '@COUNTER-STYLE'
			{
			match("@COUNTER-STYLE"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "COUNTER_STYLE_SYM"

	// $ANTLR start "FONT_FACE_SYM"
	public final void mFONT_FACE_SYM() throws RecognitionException {
		try {
			int _type = FONT_FACE_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1997:21: ( '@FONT-FACE' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1997:23: '@FONT-FACE'
			{
			match("@FONT-FACE"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FONT_FACE_SYM"

	// $ANTLR start "SUPPORTS_SYM"
	public final void mSUPPORTS_SYM() throws RecognitionException {
		try {
			int _type = SUPPORTS_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1998:21: ( '@SUPPORTS' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1998:23: '@SUPPORTS'
			{
			match("@SUPPORTS"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SUPPORTS_SYM"

	// $ANTLR start "LAYER_SYM"
	public final void mLAYER_SYM() throws RecognitionException {
		try {
			int _type = LAYER_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1999:21: ( '@LAYER' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1999:23: '@LAYER'
			{
			match("@LAYER"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LAYER_SYM"

	// $ANTLR start "CONTAINER_SYM"
	public final void mCONTAINER_SYM() throws RecognitionException {
		try {
			int _type = CONTAINER_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2000:21: ( '@CONTAINER' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2000:23: '@CONTAINER'
			{
			match("@CONTAINER"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CONTAINER_SYM"

	// $ANTLR start "TOPLEFTCORNER_SYM"
	public final void mTOPLEFTCORNER_SYM() throws RecognitionException {
		try {
			int _type = TOPLEFTCORNER_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2002:23: ( '@TOP-LEFT-CORNER' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2002:24: '@TOP-LEFT-CORNER'
			{
			match("@TOP-LEFT-CORNER"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TOPLEFTCORNER_SYM"

	// $ANTLR start "TOPLEFT_SYM"
	public final void mTOPLEFT_SYM() throws RecognitionException {
		try {
			int _type = TOPLEFT_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2003:23: ( '@TOP-LEFT' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2003:24: '@TOP-LEFT'
			{
			match("@TOP-LEFT"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TOPLEFT_SYM"

	// $ANTLR start "TOPCENTER_SYM"
	public final void mTOPCENTER_SYM() throws RecognitionException {
		try {
			int _type = TOPCENTER_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2004:23: ( '@TOP-CENTER' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2004:24: '@TOP-CENTER'
			{
			match("@TOP-CENTER"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TOPCENTER_SYM"

	// $ANTLR start "TOPRIGHT_SYM"
	public final void mTOPRIGHT_SYM() throws RecognitionException {
		try {
			int _type = TOPRIGHT_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2005:23: ( '@TOP-RIGHT' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2005:24: '@TOP-RIGHT'
			{
			match("@TOP-RIGHT"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TOPRIGHT_SYM"

	// $ANTLR start "TOPRIGHTCORNER_SYM"
	public final void mTOPRIGHTCORNER_SYM() throws RecognitionException {
		try {
			int _type = TOPRIGHTCORNER_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2006:23: ( '@TOP-RIGHT-CORNER' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2006:24: '@TOP-RIGHT-CORNER'
			{
			match("@TOP-RIGHT-CORNER"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TOPRIGHTCORNER_SYM"

	// $ANTLR start "BOTTOMLEFTCORNER_SYM"
	public final void mBOTTOMLEFTCORNER_SYM() throws RecognitionException {
		try {
			int _type = BOTTOMLEFTCORNER_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2007:23: ( '@BOTTOM-LEFT-CORNER' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2007:24: '@BOTTOM-LEFT-CORNER'
			{
			match("@BOTTOM-LEFT-CORNER"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BOTTOMLEFTCORNER_SYM"

	// $ANTLR start "BOTTOMLEFT_SYM"
	public final void mBOTTOMLEFT_SYM() throws RecognitionException {
		try {
			int _type = BOTTOMLEFT_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2008:23: ( '@BOTTOM-LEFT' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2008:24: '@BOTTOM-LEFT'
			{
			match("@BOTTOM-LEFT"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BOTTOMLEFT_SYM"

	// $ANTLR start "BOTTOMCENTER_SYM"
	public final void mBOTTOMCENTER_SYM() throws RecognitionException {
		try {
			int _type = BOTTOMCENTER_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2009:23: ( '@BOTTOM-CENTER' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2009:24: '@BOTTOM-CENTER'
			{
			match("@BOTTOM-CENTER"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BOTTOMCENTER_SYM"

	// $ANTLR start "BOTTOMRIGHT_SYM"
	public final void mBOTTOMRIGHT_SYM() throws RecognitionException {
		try {
			int _type = BOTTOMRIGHT_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2010:23: ( '@BOTTOM-RIGHT' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2010:24: '@BOTTOM-RIGHT'
			{
			match("@BOTTOM-RIGHT"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BOTTOMRIGHT_SYM"

	// $ANTLR start "BOTTOMRIGHTCORNER_SYM"
	public final void mBOTTOMRIGHTCORNER_SYM() throws RecognitionException {
		try {
			int _type = BOTTOMRIGHTCORNER_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2011:23: ( '@BOTTOM-RIGHT-CORNER' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2011:24: '@BOTTOM-RIGHT-CORNER'
			{
			match("@BOTTOM-RIGHT-CORNER"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BOTTOMRIGHTCORNER_SYM"

	// $ANTLR start "LEFTTOP_SYM"
	public final void mLEFTTOP_SYM() throws RecognitionException {
		try {
			int _type = LEFTTOP_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2012:23: ( '@LEFT-TOP' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2012:24: '@LEFT-TOP'
			{
			match("@LEFT-TOP"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LEFTTOP_SYM"

	// $ANTLR start "LEFTMIDDLE_SYM"
	public final void mLEFTMIDDLE_SYM() throws RecognitionException {
		try {
			int _type = LEFTMIDDLE_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2013:23: ( '@LEFT-MIDDLE' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2013:24: '@LEFT-MIDDLE'
			{
			match("@LEFT-MIDDLE"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LEFTMIDDLE_SYM"

	// $ANTLR start "LEFTBOTTOM_SYM"
	public final void mLEFTBOTTOM_SYM() throws RecognitionException {
		try {
			int _type = LEFTBOTTOM_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2014:23: ( '@LEFT-BOTTOM' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2014:24: '@LEFT-BOTTOM'
			{
			match("@LEFT-BOTTOM"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LEFTBOTTOM_SYM"

	// $ANTLR start "RIGHTTOP_SYM"
	public final void mRIGHTTOP_SYM() throws RecognitionException {
		try {
			int _type = RIGHTTOP_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2015:23: ( '@RIGHT-TOP' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2015:24: '@RIGHT-TOP'
			{
			match("@RIGHT-TOP"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RIGHTTOP_SYM"

	// $ANTLR start "RIGHTMIDDLE_SYM"
	public final void mRIGHTMIDDLE_SYM() throws RecognitionException {
		try {
			int _type = RIGHTMIDDLE_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2016:23: ( '@RIGHT-MIDDLE' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2016:24: '@RIGHT-MIDDLE'
			{
			match("@RIGHT-MIDDLE"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RIGHTMIDDLE_SYM"

	// $ANTLR start "RIGHTBOTTOM_SYM"
	public final void mRIGHTBOTTOM_SYM() throws RecognitionException {
		try {
			int _type = RIGHTBOTTOM_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2017:23: ( '@RIGHT-BOTTOM' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2017:24: '@RIGHT-BOTTOM'
			{
			match("@RIGHT-BOTTOM"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RIGHTBOTTOM_SYM"

	// $ANTLR start "MOZ_DOCUMENT_SYM"
	public final void mMOZ_DOCUMENT_SYM() throws RecognitionException {
		try {
			int _type = MOZ_DOCUMENT_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2019:23: ( '@-MOZ-DOCUMENT' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2019:25: '@-MOZ-DOCUMENT'
			{
			match("@-MOZ-DOCUMENT"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "MOZ_DOCUMENT_SYM"

	// $ANTLR start "WEBKIT_KEYFRAMES_SYM"
	public final void mWEBKIT_KEYFRAMES_SYM() throws RecognitionException {
		try {
			int _type = WEBKIT_KEYFRAMES_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2020:23: ( '@-WEBKIT-KEYFRAMES' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2020:25: '@-WEBKIT-KEYFRAMES'
			{
			match("@-WEBKIT-KEYFRAMES"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WEBKIT_KEYFRAMES_SYM"

	// $ANTLR start "SASS_CONTENT"
	public final void mSASS_CONTENT() throws RecognitionException {
		try {
			int _type = SASS_CONTENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2023:21: ( '@CONTENT' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2023:23: '@CONTENT'
			{
			match("@CONTENT"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_CONTENT"

	// $ANTLR start "SASS_MIXIN"
	public final void mSASS_MIXIN() throws RecognitionException {
		try {
			int _type = SASS_MIXIN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2024:21: ( '@MIXIN' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2024:23: '@MIXIN'
			{
			match("@MIXIN"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_MIXIN"

	// $ANTLR start "SASS_INCLUDE"
	public final void mSASS_INCLUDE() throws RecognitionException {
		try {
			int _type = SASS_INCLUDE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2025:21: ( '@INCLUDE' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2025:23: '@INCLUDE'
			{
			match("@INCLUDE"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_INCLUDE"

	// $ANTLR start "SASS_EXTEND"
	public final void mSASS_EXTEND() throws RecognitionException {
		try {
			int _type = SASS_EXTEND;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2026:21: ( '@EXTEND' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2026:23: '@EXTEND'
			{
			match("@EXTEND"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_EXTEND"

	// $ANTLR start "SASS_DEBUG"
	public final void mSASS_DEBUG() throws RecognitionException {
		try {
			int _type = SASS_DEBUG;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2027:21: ( '@DEBUG' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2027:23: '@DEBUG'
			{
			match("@DEBUG"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_DEBUG"

	// $ANTLR start "SASS_ERROR"
	public final void mSASS_ERROR() throws RecognitionException {
		try {
			int _type = SASS_ERROR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2028:21: ( '@ERROR' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2028:23: '@ERROR'
			{
			match("@ERROR"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_ERROR"

	// $ANTLR start "SASS_WARN"
	public final void mSASS_WARN() throws RecognitionException {
		try {
			int _type = SASS_WARN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2029:21: ( '@WARN' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2029:23: '@WARN'
			{
			match("@WARN"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_WARN"

	// $ANTLR start "SASS_IF"
	public final void mSASS_IF() throws RecognitionException {
		try {
			int _type = SASS_IF;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2030:21: ( '@IF' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2030:23: '@IF'
			{
			match("@IF"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_IF"

	// $ANTLR start "SASS_ELSE"
	public final void mSASS_ELSE() throws RecognitionException {
		try {
			int _type = SASS_ELSE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2031:21: ( '@ELSE' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2031:23: '@ELSE'
			{
			match("@ELSE"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_ELSE"

	// $ANTLR start "SASS_ELSEIF"
	public final void mSASS_ELSEIF() throws RecognitionException {
		try {
			int _type = SASS_ELSEIF;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2032:21: ( '@ELSEIF' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2032:23: '@ELSEIF'
			{
			match("@ELSEIF"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_ELSEIF"

	// $ANTLR start "SASS_FOR"
	public final void mSASS_FOR() throws RecognitionException {
		try {
			int _type = SASS_FOR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2033:21: ( '@FOR' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2033:23: '@FOR'
			{
			match("@FOR"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_FOR"

	// $ANTLR start "SASS_FUNCTION"
	public final void mSASS_FUNCTION() throws RecognitionException {
		try {
			int _type = SASS_FUNCTION;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2034:21: ( '@FUNCTION' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2034:23: '@FUNCTION'
			{
			match("@FUNCTION"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_FUNCTION"

	// $ANTLR start "SASS_RETURN"
	public final void mSASS_RETURN() throws RecognitionException {
		try {
			int _type = SASS_RETURN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2035:21: ( '@RETURN' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2035:23: '@RETURN'
			{
			match("@RETURN"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_RETURN"

	// $ANTLR start "SASS_USE"
	public final void mSASS_USE() throws RecognitionException {
		try {
			int _type = SASS_USE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2036:21: ( '@USE' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2036:23: '@USE'
			{
			match("@USE"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_USE"

	// $ANTLR start "SASS_FORWARD"
	public final void mSASS_FORWARD() throws RecognitionException {
		try {
			int _type = SASS_FORWARD;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2037:21: ( '@FORWARD' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2037:23: '@FORWARD'
			{
			match("@FORWARD"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_FORWARD"

	// $ANTLR start "SASS_EACH"
	public final void mSASS_EACH() throws RecognitionException {
		try {
			int _type = SASS_EACH;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2039:21: ( '@EACH' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2039:23: '@EACH'
			{
			match("@EACH"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_EACH"

	// $ANTLR start "SASS_WHILE"
	public final void mSASS_WHILE() throws RecognitionException {
		try {
			int _type = SASS_WHILE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2040:21: ( '@WHILE' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2040:23: '@WHILE'
			{
			match("@WHILE"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_WHILE"

	// $ANTLR start "SASS_AT_ROOT"
	public final void mSASS_AT_ROOT() throws RecognitionException {
		try {
			int _type = SASS_AT_ROOT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2041:21: ( '@AT-ROOT' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2041:23: '@AT-ROOT'
			{
			match("@AT-ROOT"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_AT_ROOT"

	// $ANTLR start "AT_SIGN"
	public final void mAT_SIGN() throws RecognitionException {
		try {
			int _type = AT_SIGN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2043:21: ( '@' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2043:23: '@'
			{
			match('@'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "AT_SIGN"

	// $ANTLR start "AT_IDENT"
	public final void mAT_IDENT() throws RecognitionException {
		try {
			int _type = AT_IDENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2044:14: ( ( AT_SIGN | ( AT_SIGN AT_SIGN ) ) ( NMCHAR )+ )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2044:16: ( AT_SIGN | ( AT_SIGN AT_SIGN ) ) ( NMCHAR )+
			{
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2044:16: ( AT_SIGN | ( AT_SIGN AT_SIGN ) )
			int alt178=2;
			int LA178_0 = input.LA(1);
			if ( (LA178_0=='@') ) {
				int LA178_1 = input.LA(2);
				if ( (LA178_1=='-'||(LA178_1 >= '0' && LA178_1 <= '9')||(LA178_1 >= 'A' && LA178_1 <= 'Z')||LA178_1=='\\'||LA178_1=='_'||(LA178_1 >= 'a' && LA178_1 <= 'z')||(LA178_1 >= '\u0080' && LA178_1 <= '\uFFFF')) ) {
					alt178=1;
				}
				else if ( (LA178_1=='@') ) {
					alt178=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 178, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 178, 0, input);
				throw nvae;
			}

			switch (alt178) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2044:17: AT_SIGN
					{
					mAT_SIGN(); if (state.failed) return;

					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2044:27: ( AT_SIGN AT_SIGN )
					{
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2044:27: ( AT_SIGN AT_SIGN )
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2044:28: AT_SIGN AT_SIGN
					{
					mAT_SIGN(); if (state.failed) return;

					mAT_SIGN(); if (state.failed) return;

					}

					}
					break;

			}

			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2044:46: ( NMCHAR )+
			int cnt179=0;
			loop179:
			while (true) {
				int alt179=2;
				int LA179_0 = input.LA(1);
				if ( (LA179_0=='-'||(LA179_0 >= '0' && LA179_0 <= '9')||(LA179_0 >= 'A' && LA179_0 <= 'Z')||LA179_0=='\\'||LA179_0=='_'||(LA179_0 >= 'a' && LA179_0 <= 'z')||(LA179_0 >= '\u0080' && LA179_0 <= '\uFFFF')) ) {
					alt179=1;
				}

				switch (alt179) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2044:46: NMCHAR
					{
					mNMCHAR(); if (state.failed) return;

					}
					break;

				default :
					if ( cnt179 >= 1 ) break loop179;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(179, input);
					throw eee;
				}
				cnt179++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "AT_IDENT"

	// $ANTLR start "SASS_VAR"
	public final void mSASS_VAR() throws RecognitionException {
		try {
			int _type = SASS_VAR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2046:21: ( '$' ( NMCHAR )+ )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2046:23: '$' ( NMCHAR )+
			{
			match('$'); if (state.failed) return;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2046:27: ( NMCHAR )+
			int cnt180=0;
			loop180:
			while (true) {
				int alt180=2;
				int LA180_0 = input.LA(1);
				if ( (LA180_0=='-'||(LA180_0 >= '0' && LA180_0 <= '9')||(LA180_0 >= 'A' && LA180_0 <= 'Z')||LA180_0=='\\'||LA180_0=='_'||(LA180_0 >= 'a' && LA180_0 <= 'z')||(LA180_0 >= '\u0080' && LA180_0 <= '\uFFFF')) ) {
					alt180=1;
				}

				switch (alt180) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2046:27: NMCHAR
					{
					mNMCHAR(); if (state.failed) return;

					}
					break;

				default :
					if ( cnt180 >= 1 ) break loop180;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(180, input);
					throw eee;
				}
				cnt180++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_VAR"

	// $ANTLR start "SASS_DEFAULT"
	public final void mSASS_DEFAULT() throws RecognitionException {
		try {
			int _type = SASS_DEFAULT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2047:21: ( '!DEFAULT' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2047:23: '!DEFAULT'
			{
			match("!DEFAULT"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_DEFAULT"

	// $ANTLR start "SASS_OPTIONAL"
	public final void mSASS_OPTIONAL() throws RecognitionException {
		try {
			int _type = SASS_OPTIONAL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2048:21: ( '!OPTIONAL' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2048:23: '!OPTIONAL'
			{
			match("!OPTIONAL"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_OPTIONAL"

	// $ANTLR start "SASS_GLOBAL"
	public final void mSASS_GLOBAL() throws RecognitionException {
		try {
			int _type = SASS_GLOBAL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2049:21: ( '!GLOBAL' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2049:23: '!GLOBAL'
			{
			match("!GLOBAL"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_GLOBAL"

	// $ANTLR start "SASS_EXTEND_ONLY_SELECTOR"
	public final void mSASS_EXTEND_ONLY_SELECTOR() throws RecognitionException {
		try {
			int _type = SASS_EXTEND_ONLY_SELECTOR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2052:21: ( PERCENTAGE_SYMBOL ( NMCHAR )+ )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2052:23: PERCENTAGE_SYMBOL ( NMCHAR )+
			{
			mPERCENTAGE_SYMBOL(); if (state.failed) return;

			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2052:41: ( NMCHAR )+
			int cnt181=0;
			loop181:
			while (true) {
				int alt181=2;
				int LA181_0 = input.LA(1);
				if ( (LA181_0=='-'||(LA181_0 >= '0' && LA181_0 <= '9')||(LA181_0 >= 'A' && LA181_0 <= 'Z')||LA181_0=='\\'||LA181_0=='_'||(LA181_0 >= 'a' && LA181_0 <= 'z')||(LA181_0 >= '\u0080' && LA181_0 <= '\uFFFF')) ) {
					alt181=1;
				}

				switch (alt181) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2052:41: NMCHAR
					{
					mNMCHAR(); if (state.failed) return;

					}
					break;

				default :
					if ( cnt181 >= 1 ) break loop181;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(181, input);
					throw eee;
				}
				cnt181++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_EXTEND_ONLY_SELECTOR"

	// $ANTLR start "EMS"
	public final void mEMS() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2064:25: ()
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2064:26: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "EMS"

	// $ANTLR start "EXS"
	public final void mEXS() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2065:25: ()
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2065:26: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "EXS"

	// $ANTLR start "LENGTH"
	public final void mLENGTH() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2066:25: ()
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2066:26: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LENGTH"

	// $ANTLR start "REM"
	public final void mREM() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2067:18: ()
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2067:19: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "REM"

	// $ANTLR start "ANGLE"
	public final void mANGLE() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2068:25: ()
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2068:26: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ANGLE"

	// $ANTLR start "TIME"
	public final void mTIME() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2069:25: ()
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2069:26: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TIME"

	// $ANTLR start "FREQ"
	public final void mFREQ() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2070:25: ()
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2070:26: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FREQ"

	// $ANTLR start "DIMENSION"
	public final void mDIMENSION() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2071:25: ()
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2071:26: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DIMENSION"

	// $ANTLR start "PERCENTAGE"
	public final void mPERCENTAGE() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2072:25: ()
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2072:26: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "PERCENTAGE"

	// $ANTLR start "RESOLUTION"
	public final void mRESOLUTION() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2073:25: ()
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2073:26: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RESOLUTION"

	// $ANTLR start "NUMBER"
	public final void mNUMBER() throws RecognitionException {
		try {
			int _type = NUMBER;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2076:5: ( ( ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? | '.' ( '0' .. '9' )+ ) ( ( D P ( I | C ) )=> D P ( I | C M ) | ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R ( A | E ) )=> R ( A D | E M ) | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | PERCENTAGE_SYMBOL |) )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2076:9: ( ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? | '.' ( '0' .. '9' )+ ) ( ( D P ( I | C ) )=> D P ( I | C M ) | ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R ( A | E ) )=> R ( A D | E M ) | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | PERCENTAGE_SYMBOL |)
			{
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2076:9: ( ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? | '.' ( '0' .. '9' )+ )
			int alt186=2;
			int LA186_0 = input.LA(1);
			if ( ((LA186_0 >= '0' && LA186_0 <= '9')) ) {
				alt186=1;
			}
			else if ( (LA186_0=='.') ) {
				alt186=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 186, 0, input);
				throw nvae;
			}

			switch (alt186) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2077:15: ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )?
					{
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2077:15: ( '0' .. '9' )+
					int cnt182=0;
					loop182:
					while (true) {
						int alt182=2;
						int LA182_0 = input.LA(1);
						if ( ((LA182_0 >= '0' && LA182_0 <= '9')) ) {
							alt182=1;
						}

						switch (alt182) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
							{
							if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

						default :
							if ( cnt182 >= 1 ) break loop182;
							if (state.backtracking>0) {state.failed=true; return;}
							EarlyExitException eee = new EarlyExitException(182, input);
							throw eee;
						}
						cnt182++;
					}

					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2077:25: ( '.' ( '0' .. '9' )+ )?
					int alt184=2;
					int LA184_0 = input.LA(1);
					if ( (LA184_0=='.') ) {
						alt184=1;
					}
					switch (alt184) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2077:26: '.' ( '0' .. '9' )+
							{
							match('.'); if (state.failed) return;
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2077:30: ( '0' .. '9' )+
							int cnt183=0;
							loop183:
							while (true) {
								int alt183=2;
								int LA183_0 = input.LA(1);
								if ( ((LA183_0 >= '0' && LA183_0 <= '9')) ) {
									alt183=1;
								}

								switch (alt183) {
								case 1 :
									// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
									{
									if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
										input.consume();
										state.failed=false;
									}
									else {
										if (state.backtracking>0) {state.failed=true; return;}
										MismatchedSetException mse = new MismatchedSetException(null,input);
										recover(mse);
										throw mse;
									}
									}
									break;

								default :
									if ( cnt183 >= 1 ) break loop183;
									if (state.backtracking>0) {state.failed=true; return;}
									EarlyExitException eee = new EarlyExitException(183, input);
									throw eee;
								}
								cnt183++;
							}

							}
							break;

					}

					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2078:15: '.' ( '0' .. '9' )+
					{
					match('.'); if (state.failed) return;
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2078:19: ( '0' .. '9' )+
					int cnt185=0;
					loop185:
					while (true) {
						int alt185=2;
						int LA185_0 = input.LA(1);
						if ( ((LA185_0 >= '0' && LA185_0 <= '9')) ) {
							alt185=1;
						}

						switch (alt185) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
							{
							if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

						default :
							if ( cnt185 >= 1 ) break loop185;
							if (state.backtracking>0) {state.failed=true; return;}
							EarlyExitException eee = new EarlyExitException(185, input);
							throw eee;
						}
						cnt185++;
					}

					}
					break;

			}

			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2080:9: ( ( D P ( I | C ) )=> D P ( I | C M ) | ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R ( A | E ) )=> R ( A D | E M ) | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | PERCENTAGE_SYMBOL |)
			int alt193=13;
			alt193 = dfa193.predict(input);
			switch (alt193) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2081:15: ( D P ( I | C ) )=> D P ( I | C M )
					{
					mD(); if (state.failed) return;

					mP(); if (state.failed) return;

					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2083:17: ( I | C M )
					int alt187=2;
					switch ( input.LA(1) ) {
					case 'I':
					case 'i':
						{
						alt187=1;
						}
						break;
					case '\\':
						{
						switch ( input.LA(2) ) {
						case 'I':
						case 'i':
							{
							alt187=1;
							}
							break;
						case '0':
							{
							int LA187_4 = input.LA(3);
							if ( (LA187_4=='0') ) {
								int LA187_6 = input.LA(4);
								if ( (LA187_6=='0') ) {
									int LA187_7 = input.LA(5);
									if ( (LA187_7=='0') ) {
										int LA187_8 = input.LA(6);
										if ( (LA187_8=='4'||LA187_8=='6') ) {
											int LA187_5 = input.LA(7);
											if ( (LA187_5=='9') ) {
												alt187=1;
											}
											else if ( (LA187_5=='3') ) {
												alt187=2;
											}

											else {
												if (state.backtracking>0) {state.failed=true; return;}
												int nvaeMark = input.mark();
												try {
													for (int nvaeConsume = 0; nvaeConsume < 7 - 1; nvaeConsume++) {
														input.consume();
													}
													NoViableAltException nvae =
														new NoViableAltException("", 187, 5, input);
													throw nvae;
												} finally {
													input.rewind(nvaeMark);
												}
											}

										}

										else {
											if (state.backtracking>0) {state.failed=true; return;}
											int nvaeMark = input.mark();
											try {
												for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
													input.consume();
												}
												NoViableAltException nvae =
													new NoViableAltException("", 187, 8, input);
												throw nvae;
											} finally {
												input.rewind(nvaeMark);
											}
										}

									}
									else if ( (LA187_7=='4'||LA187_7=='6') ) {
										int LA187_5 = input.LA(6);
										if ( (LA187_5=='9') ) {
											alt187=1;
										}
										else if ( (LA187_5=='3') ) {
											alt187=2;
										}

										else {
											if (state.backtracking>0) {state.failed=true; return;}
											int nvaeMark = input.mark();
											try {
												for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
													input.consume();
												}
												NoViableAltException nvae =
													new NoViableAltException("", 187, 5, input);
												throw nvae;
											} finally {
												input.rewind(nvaeMark);
											}
										}

									}

									else {
										if (state.backtracking>0) {state.failed=true; return;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 187, 7, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}

								}
								else if ( (LA187_6=='4'||LA187_6=='6') ) {
									int LA187_5 = input.LA(5);
									if ( (LA187_5=='9') ) {
										alt187=1;
									}
									else if ( (LA187_5=='3') ) {
										alt187=2;
									}

									else {
										if (state.backtracking>0) {state.failed=true; return;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 187, 5, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}

								}

								else {
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 187, 6, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}
							else if ( (LA187_4=='4'||LA187_4=='6') ) {
								int LA187_5 = input.LA(4);
								if ( (LA187_5=='9') ) {
									alt187=1;
								}
								else if ( (LA187_5=='3') ) {
									alt187=2;
								}

								else {
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 187, 5, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 187, 4, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

							}
							break;
						case '4':
						case '6':
							{
							int LA187_5 = input.LA(3);
							if ( (LA187_5=='9') ) {
								alt187=1;
							}
							else if ( (LA187_5=='3') ) {
								alt187=2;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 187, 5, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

							}
							break;
						default:
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								input.consume();
								NoViableAltException nvae =
									new NoViableAltException("", 187, 2, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}
						}
						break;
					case 'C':
					case 'c':
						{
						alt187=2;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 187, 0, input);
						throw nvae;
					}
					switch (alt187) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2084:22: I
							{
							mI(); if (state.failed) return;

							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2084:26: C M
							{
							mC(); if (state.failed) return;

							mM(); if (state.failed) return;

							}
							break;

					}

					if ( state.backtracking==0 ) { _type = RESOLUTION; }
					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2088:15: ( E ( M | X ) )=> E ( M | X )
					{
					mE(); if (state.failed) return;

					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2090:17: ( M | X )
					int alt188=2;
					switch ( input.LA(1) ) {
					case 'M':
					case 'm':
						{
						alt188=1;
						}
						break;
					case '\\':
						{
						switch ( input.LA(2) ) {
						case '4':
						case '6':
						case 'M':
						case 'm':
							{
							alt188=1;
							}
							break;
						case '0':
							{
							switch ( input.LA(3) ) {
							case '0':
								{
								switch ( input.LA(4) ) {
								case '0':
									{
									switch ( input.LA(5) ) {
									case '0':
										{
										int LA188_7 = input.LA(6);
										if ( (LA188_7=='4'||LA188_7=='6') ) {
											alt188=1;
										}
										else if ( (LA188_7=='5'||LA188_7=='7') ) {
											alt188=2;
										}

										else {
											if (state.backtracking>0) {state.failed=true; return;}
											int nvaeMark = input.mark();
											try {
												for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
													input.consume();
												}
												NoViableAltException nvae =
													new NoViableAltException("", 188, 7, input);
												throw nvae;
											} finally {
												input.rewind(nvaeMark);
											}
										}

										}
										break;
									case '4':
									case '6':
										{
										alt188=1;
										}
										break;
									case '5':
									case '7':
										{
										alt188=2;
										}
										break;
									default:
										if (state.backtracking>0) {state.failed=true; return;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 188, 6, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}
									}
									break;
								case '4':
								case '6':
									{
									alt188=1;
									}
									break;
								case '5':
								case '7':
									{
									alt188=2;
									}
									break;
								default:
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 188, 5, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}
								}
								break;
							case '4':
							case '6':
								{
								alt188=1;
								}
								break;
							case '5':
							case '7':
								{
								alt188=2;
								}
								break;
							default:
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 188, 4, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}
							}
							break;
						case '5':
						case '7':
						case 'X':
						case 'x':
							{
							alt188=2;
							}
							break;
						default:
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								input.consume();
								NoViableAltException nvae =
									new NoViableAltException("", 188, 2, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}
						}
						break;
					case 'X':
					case 'x':
						{
						alt188=2;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 188, 0, input);
						throw nvae;
					}
					switch (alt188) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2091:23: M
							{
							mM(); if (state.failed) return;

							if ( state.backtracking==0 ) { _type = EMS;          }
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2092:23: X
							{
							mX(); if (state.failed) return;

							if ( state.backtracking==0 ) { _type = EXS;          }
							}
							break;

					}

					}
					break;
				case 3 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2094:15: ( P ( X | T | C ) )=> P ( X | T | C )
					{
					mP(); if (state.failed) return;

					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2096:17: ( X | T | C )
					int alt189=3;
					switch ( input.LA(1) ) {
					case 'X':
					case 'x':
						{
						alt189=1;
						}
						break;
					case '\\':
						{
						switch ( input.LA(2) ) {
						case 'X':
						case 'x':
							{
							alt189=1;
							}
							break;
						case '0':
							{
							switch ( input.LA(3) ) {
							case '0':
								{
								switch ( input.LA(4) ) {
								case '0':
									{
									switch ( input.LA(5) ) {
									case '0':
										{
										int LA189_9 = input.LA(6);
										if ( (LA189_9=='5'||LA189_9=='7') ) {
											int LA189_6 = input.LA(7);
											if ( (LA189_6=='8') ) {
												alt189=1;
											}
											else if ( (LA189_6=='4') ) {
												alt189=2;
											}

											else {
												if (state.backtracking>0) {state.failed=true; return;}
												int nvaeMark = input.mark();
												try {
													for (int nvaeConsume = 0; nvaeConsume < 7 - 1; nvaeConsume++) {
														input.consume();
													}
													NoViableAltException nvae =
														new NoViableAltException("", 189, 6, input);
													throw nvae;
												} finally {
													input.rewind(nvaeMark);
												}
											}

										}
										else if ( (LA189_9=='4'||LA189_9=='6') ) {
											alt189=3;
										}

										else {
											if (state.backtracking>0) {state.failed=true; return;}
											int nvaeMark = input.mark();
											try {
												for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
													input.consume();
												}
												NoViableAltException nvae =
													new NoViableAltException("", 189, 9, input);
												throw nvae;
											} finally {
												input.rewind(nvaeMark);
											}
										}

										}
										break;
									case '5':
									case '7':
										{
										int LA189_6 = input.LA(6);
										if ( (LA189_6=='8') ) {
											alt189=1;
										}
										else if ( (LA189_6=='4') ) {
											alt189=2;
										}

										else {
											if (state.backtracking>0) {state.failed=true; return;}
											int nvaeMark = input.mark();
											try {
												for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
													input.consume();
												}
												NoViableAltException nvae =
													new NoViableAltException("", 189, 6, input);
												throw nvae;
											} finally {
												input.rewind(nvaeMark);
											}
										}

										}
										break;
									case '4':
									case '6':
										{
										alt189=3;
										}
										break;
									default:
										if (state.backtracking>0) {state.failed=true; return;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 189, 8, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}
									}
									break;
								case '5':
								case '7':
									{
									int LA189_6 = input.LA(5);
									if ( (LA189_6=='8') ) {
										alt189=1;
									}
									else if ( (LA189_6=='4') ) {
										alt189=2;
									}

									else {
										if (state.backtracking>0) {state.failed=true; return;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 189, 6, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}

									}
									break;
								case '4':
								case '6':
									{
									alt189=3;
									}
									break;
								default:
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 189, 7, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}
								}
								break;
							case '5':
							case '7':
								{
								int LA189_6 = input.LA(4);
								if ( (LA189_6=='8') ) {
									alt189=1;
								}
								else if ( (LA189_6=='4') ) {
									alt189=2;
								}

								else {
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 189, 6, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

								}
								break;
							case '4':
							case '6':
								{
								alt189=3;
								}
								break;
							default:
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 189, 5, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}
							}
							break;
						case '5':
						case '7':
							{
							int LA189_6 = input.LA(3);
							if ( (LA189_6=='8') ) {
								alt189=1;
							}
							else if ( (LA189_6=='4') ) {
								alt189=2;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 189, 6, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

							}
							break;
						case 'T':
						case 't':
							{
							alt189=2;
							}
							break;
						case '4':
						case '6':
							{
							alt189=3;
							}
							break;
						default:
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								input.consume();
								NoViableAltException nvae =
									new NoViableAltException("", 189, 2, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}
						}
						break;
					case 'T':
					case 't':
						{
						alt189=2;
						}
						break;
					case 'C':
					case 'c':
						{
						alt189=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 189, 0, input);
						throw nvae;
					}
					switch (alt189) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2097:23: X
							{
							mX(); if (state.failed) return;

							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2098:23: T
							{
							mT(); if (state.failed) return;

							}
							break;
						case 3 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2099:23: C
							{
							mC(); if (state.failed) return;

							}
							break;

					}

					if ( state.backtracking==0 ) { _type = LENGTH;       }
					}
					break;
				case 4 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2102:15: ( C M )=> C M
					{
					mC(); if (state.failed) return;

					mM(); if (state.failed) return;

					if ( state.backtracking==0 ) { _type = LENGTH;       }
					}
					break;
				case 5 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2104:15: ( M ( M | S ) )=> M ( M | S )
					{
					mM(); if (state.failed) return;

					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2106:17: ( M | S )
					int alt190=2;
					switch ( input.LA(1) ) {
					case 'M':
					case 'm':
						{
						alt190=1;
						}
						break;
					case '\\':
						{
						switch ( input.LA(2) ) {
						case '4':
						case '6':
						case 'M':
						case 'm':
							{
							alt190=1;
							}
							break;
						case '0':
							{
							switch ( input.LA(3) ) {
							case '0':
								{
								switch ( input.LA(4) ) {
								case '0':
									{
									switch ( input.LA(5) ) {
									case '0':
										{
										int LA190_7 = input.LA(6);
										if ( (LA190_7=='4'||LA190_7=='6') ) {
											alt190=1;
										}
										else if ( (LA190_7=='5'||LA190_7=='7') ) {
											alt190=2;
										}

										else {
											if (state.backtracking>0) {state.failed=true; return;}
											int nvaeMark = input.mark();
											try {
												for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
													input.consume();
												}
												NoViableAltException nvae =
													new NoViableAltException("", 190, 7, input);
												throw nvae;
											} finally {
												input.rewind(nvaeMark);
											}
										}

										}
										break;
									case '4':
									case '6':
										{
										alt190=1;
										}
										break;
									case '5':
									case '7':
										{
										alt190=2;
										}
										break;
									default:
										if (state.backtracking>0) {state.failed=true; return;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 190, 6, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}
									}
									break;
								case '4':
								case '6':
									{
									alt190=1;
									}
									break;
								case '5':
								case '7':
									{
									alt190=2;
									}
									break;
								default:
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 190, 5, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}
								}
								break;
							case '4':
							case '6':
								{
								alt190=1;
								}
								break;
							case '5':
							case '7':
								{
								alt190=2;
								}
								break;
							default:
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 190, 4, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}
							}
							break;
						case '5':
						case '7':
						case 'S':
						case 's':
							{
							alt190=2;
							}
							break;
						default:
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								input.consume();
								NoViableAltException nvae =
									new NoViableAltException("", 190, 2, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}
						}
						break;
					case 'S':
					case 's':
						{
						alt190=2;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 190, 0, input);
						throw nvae;
					}
					switch (alt190) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2107:23: M
							{
							mM(); if (state.failed) return;

							if ( state.backtracking==0 ) { _type = LENGTH;       }
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2109:23: S
							{
							mS(); if (state.failed) return;

							if ( state.backtracking==0 ) { _type = TIME;         }
							}
							break;

					}

					}
					break;
				case 6 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2111:15: ( I N )=> I N
					{
					mI(); if (state.failed) return;

					mN(); if (state.failed) return;

					if ( state.backtracking==0 ) { _type = LENGTH;       }
					}
					break;
				case 7 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2114:15: ( D E G )=> D E G
					{
					mD(); if (state.failed) return;

					mE(); if (state.failed) return;

					mG(); if (state.failed) return;

					if ( state.backtracking==0 ) { _type = ANGLE;        }
					}
					break;
				case 8 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2119:15: ( R ( A | E ) )=> R ( A D | E M )
					{
					mR(); if (state.failed) return;

					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2121:17: ( A D | E M )
					int alt191=2;
					switch ( input.LA(1) ) {
					case 'A':
					case 'a':
						{
						alt191=1;
						}
						break;
					case '\\':
						{
						int LA191_2 = input.LA(2);
						if ( (LA191_2=='0') ) {
							int LA191_4 = input.LA(3);
							if ( (LA191_4=='0') ) {
								int LA191_6 = input.LA(4);
								if ( (LA191_6=='0') ) {
									int LA191_7 = input.LA(5);
									if ( (LA191_7=='0') ) {
										int LA191_8 = input.LA(6);
										if ( (LA191_8=='4'||LA191_8=='6') ) {
											int LA191_5 = input.LA(7);
											if ( (LA191_5=='1') ) {
												alt191=1;
											}
											else if ( (LA191_5=='5') ) {
												alt191=2;
											}

											else {
												if (state.backtracking>0) {state.failed=true; return;}
												int nvaeMark = input.mark();
												try {
													for (int nvaeConsume = 0; nvaeConsume < 7 - 1; nvaeConsume++) {
														input.consume();
													}
													NoViableAltException nvae =
														new NoViableAltException("", 191, 5, input);
													throw nvae;
												} finally {
													input.rewind(nvaeMark);
												}
											}

										}

										else {
											if (state.backtracking>0) {state.failed=true; return;}
											int nvaeMark = input.mark();
											try {
												for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
													input.consume();
												}
												NoViableAltException nvae =
													new NoViableAltException("", 191, 8, input);
												throw nvae;
											} finally {
												input.rewind(nvaeMark);
											}
										}

									}
									else if ( (LA191_7=='4'||LA191_7=='6') ) {
										int LA191_5 = input.LA(6);
										if ( (LA191_5=='1') ) {
											alt191=1;
										}
										else if ( (LA191_5=='5') ) {
											alt191=2;
										}

										else {
											if (state.backtracking>0) {state.failed=true; return;}
											int nvaeMark = input.mark();
											try {
												for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
													input.consume();
												}
												NoViableAltException nvae =
													new NoViableAltException("", 191, 5, input);
												throw nvae;
											} finally {
												input.rewind(nvaeMark);
											}
										}

									}

									else {
										if (state.backtracking>0) {state.failed=true; return;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 191, 7, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}

								}
								else if ( (LA191_6=='4'||LA191_6=='6') ) {
									int LA191_5 = input.LA(5);
									if ( (LA191_5=='1') ) {
										alt191=1;
									}
									else if ( (LA191_5=='5') ) {
										alt191=2;
									}

									else {
										if (state.backtracking>0) {state.failed=true; return;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 191, 5, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}

								}

								else {
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 191, 6, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}
							else if ( (LA191_4=='4'||LA191_4=='6') ) {
								int LA191_5 = input.LA(4);
								if ( (LA191_5=='1') ) {
									alt191=1;
								}
								else if ( (LA191_5=='5') ) {
									alt191=2;
								}

								else {
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 191, 5, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 191, 4, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}
						else if ( (LA191_2=='4'||LA191_2=='6') ) {
							int LA191_5 = input.LA(3);
							if ( (LA191_5=='1') ) {
								alt191=1;
							}
							else if ( (LA191_5=='5') ) {
								alt191=2;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 191, 5, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}

						else {
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								input.consume();
								NoViableAltException nvae =
									new NoViableAltException("", 191, 2, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

						}
						break;
					case 'E':
					case 'e':
						{
						alt191=2;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 191, 0, input);
						throw nvae;
					}
					switch (alt191) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2122:20: A D
							{
							mA(); if (state.failed) return;

							mD(); if (state.failed) return;

							if ( state.backtracking==0 ) {_type = ANGLE;         }
							}
							break;
						case 2 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2123:20: E M
							{
							mE(); if (state.failed) return;

							mM(); if (state.failed) return;

							if ( state.backtracking==0 ) {_type = REM;           }
							}
							break;

					}

					}
					break;
				case 9 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2126:15: ( S )=> S
					{
					mS(); if (state.failed) return;

					if ( state.backtracking==0 ) { _type = TIME;         }
					}
					break;
				case 10 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2128:15: ( ( K )? H Z )=> ( K )? H Z
					{
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2129:17: ( K )?
					int alt192=2;
					int LA192_0 = input.LA(1);
					if ( (LA192_0=='K'||LA192_0=='k') ) {
						alt192=1;
					}
					else if ( (LA192_0=='\\') ) {
						switch ( input.LA(2) ) {
							case 'K':
							case 'k':
								{
								alt192=1;
								}
								break;
							case '0':
								{
								int LA192_4 = input.LA(3);
								if ( (LA192_4=='0') ) {
									int LA192_6 = input.LA(4);
									if ( (LA192_6=='0') ) {
										int LA192_7 = input.LA(5);
										if ( (LA192_7=='0') ) {
											int LA192_8 = input.LA(6);
											if ( (LA192_8=='4'||LA192_8=='6') ) {
												int LA192_5 = input.LA(7);
												if ( (LA192_5=='B'||LA192_5=='b') ) {
													alt192=1;
												}
											}
										}
										else if ( (LA192_7=='4'||LA192_7=='6') ) {
											int LA192_5 = input.LA(6);
											if ( (LA192_5=='B'||LA192_5=='b') ) {
												alt192=1;
											}
										}
									}
									else if ( (LA192_6=='4'||LA192_6=='6') ) {
										int LA192_5 = input.LA(5);
										if ( (LA192_5=='B'||LA192_5=='b') ) {
											alt192=1;
										}
									}
								}
								else if ( (LA192_4=='4'||LA192_4=='6') ) {
									int LA192_5 = input.LA(4);
									if ( (LA192_5=='B'||LA192_5=='b') ) {
										alt192=1;
									}
								}
								}
								break;
							case '4':
							case '6':
								{
								int LA192_5 = input.LA(3);
								if ( (LA192_5=='B'||LA192_5=='b') ) {
									alt192=1;
								}
								}
								break;
						}
					}
					switch (alt192) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2129:17: K
							{
							mK(); if (state.failed) return;

							}
							break;

					}

					mH(); if (state.failed) return;

					mZ(); if (state.failed) return;

					if ( state.backtracking==0 ) { _type = FREQ;         }
					}
					break;
				case 11 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2131:15: IDENT
					{
					mIDENT(); if (state.failed) return;

					if ( state.backtracking==0 ) { _type = DIMENSION;    }
					}
					break;
				case 12 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2133:15: PERCENTAGE_SYMBOL
					{
					mPERCENTAGE_SYMBOL(); if (state.failed) return;

					if ( state.backtracking==0 ) { _type = PERCENTAGE;   }
					}
					break;
				case 13 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2136:9: 
					{
					}
					break;

			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NUMBER"

	// $ANTLR start "URI"
	public final void mURI() throws RecognitionException {
		try {
			int _type = URI;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2142:5: ( U R L '(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2142:9: U R L '(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')'
			{
			mU(); if (state.failed) return;

			mR(); if (state.failed) return;

			mL(); if (state.failed) return;

			match('('); if (state.failed) return;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2144:13: ( ( WS )=> WS )?
			int alt194=2;
			int LA194_0 = input.LA(1);
			if ( (LA194_0=='\t'||LA194_0==' ') ) {
				int LA194_1 = input.LA(2);
				if ( (synpred13_Css3()) ) {
					alt194=1;
				}
			}
			switch (alt194) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2144:14: ( WS )=> WS
					{
					mWS(); if (state.failed) return;

					}
					break;

			}

			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2144:25: ( URL | STRING )
			int alt195=2;
			int LA195_0 = input.LA(1);
			if ( (LA195_0=='\t'||(LA195_0 >= ' ' && LA195_0 <= '!')||(LA195_0 >= '#' && LA195_0 <= '&')||(LA195_0 >= ')' && LA195_0 <= ';')||LA195_0=='='||(LA195_0 >= '?' && LA195_0 <= '\\')||LA195_0=='_'||(LA195_0 >= 'a' && LA195_0 <= '~')||(LA195_0 >= '\u0080' && LA195_0 <= '\uFFFF')) ) {
				alt195=1;
			}
			else if ( (LA195_0=='\"'||LA195_0=='\'') ) {
				alt195=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 195, 0, input);
				throw nvae;
			}

			switch (alt195) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2144:26: URL
					{
					mURL(); if (state.failed) return;

					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2144:30: STRING
					{
					mSTRING(); if (state.failed) return;

					}
					break;

			}

			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2144:38: ( WS )?
			int alt196=2;
			int LA196_0 = input.LA(1);
			if ( (LA196_0=='\t'||LA196_0==' ') ) {
				alt196=1;
			}
			switch (alt196) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2144:38: WS
					{
					mWS(); if (state.failed) return;

					}
					break;

			}

			match(')'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "URI"

	// $ANTLR start "HEXCHAR_WILDCARD"
	public final void mHEXCHAR_WILDCARD() throws RecognitionException {
		try {
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2148:26: ( '?' | HEXCHAR )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
			{
			if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||input.LA(1)=='?'||(input.LA(1) >= 'A' && input.LA(1) <= 'F')||(input.LA(1) >= 'a' && input.LA(1) <= 'f') ) {
				input.consume();
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "HEXCHAR_WILDCARD"

	// $ANTLR start "URANGE"
	public final void mURANGE() throws RecognitionException {
		try {
			int _type = URANGE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2150:7: ( ( 'u' | 'U' ) PLUS ( HEXCHAR_WILDCARD )+ ( MINUS ( HEXCHAR_WILDCARD )+ )? )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2150:9: ( 'u' | 'U' ) PLUS ( HEXCHAR_WILDCARD )+ ( MINUS ( HEXCHAR_WILDCARD )+ )?
			{
			if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
				input.consume();
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			mPLUS(); if (state.failed) return;

			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2150:24: ( HEXCHAR_WILDCARD )+
			int cnt197=0;
			loop197:
			while (true) {
				int alt197=2;
				int LA197_0 = input.LA(1);
				if ( ((LA197_0 >= '0' && LA197_0 <= '9')||LA197_0=='?'||(LA197_0 >= 'A' && LA197_0 <= 'F')||(LA197_0 >= 'a' && LA197_0 <= 'f')) ) {
					alt197=1;
				}

				switch (alt197) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
					{
					if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||input.LA(1)=='?'||(input.LA(1) >= 'A' && input.LA(1) <= 'F')||(input.LA(1) >= 'a' && input.LA(1) <= 'f') ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt197 >= 1 ) break loop197;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(197, input);
					throw eee;
				}
				cnt197++;
			}

			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2150:42: ( MINUS ( HEXCHAR_WILDCARD )+ )?
			int alt199=2;
			int LA199_0 = input.LA(1);
			if ( (LA199_0=='-') ) {
				alt199=1;
			}
			switch (alt199) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2150:43: MINUS ( HEXCHAR_WILDCARD )+
					{
					mMINUS(); if (state.failed) return;

					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2150:49: ( HEXCHAR_WILDCARD )+
					int cnt198=0;
					loop198:
					while (true) {
						int alt198=2;
						int LA198_0 = input.LA(1);
						if ( ((LA198_0 >= '0' && LA198_0 <= '9')||LA198_0=='?'||(LA198_0 >= 'A' && LA198_0 <= 'F')||(LA198_0 >= 'a' && LA198_0 <= 'f')) ) {
							alt198=1;
						}

						switch (alt198) {
						case 1 :
							// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
							{
							if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||input.LA(1)=='?'||(input.LA(1) >= 'A' && input.LA(1) <= 'F')||(input.LA(1) >= 'a' && input.LA(1) <= 'f') ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

						default :
							if ( cnt198 >= 1 ) break loop198;
							if (state.backtracking>0) {state.failed=true; return;}
							EarlyExitException eee = new EarlyExitException(198, input);
							throw eee;
						}
						cnt198++;
					}

					}
					break;

			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "URANGE"

	// $ANTLR start "MOZ_URL_PREFIX"
	public final void mMOZ_URL_PREFIX() throws RecognitionException {
		try {
			int _type = MOZ_URL_PREFIX;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2153:2: ( 'URL-PREFIX(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2154:2: 'URL-PREFIX(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')'
			{
			match("URL-PREFIX("); if (state.failed) return;

			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2155:13: ( ( WS )=> WS )?
			int alt200=2;
			int LA200_0 = input.LA(1);
			if ( (LA200_0=='\t'||LA200_0==' ') ) {
				int LA200_1 = input.LA(2);
				if ( (synpred14_Css3()) ) {
					alt200=1;
				}
			}
			switch (alt200) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2155:14: ( WS )=> WS
					{
					mWS(); if (state.failed) return;

					}
					break;

			}

			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2155:25: ( URL | STRING )
			int alt201=2;
			int LA201_0 = input.LA(1);
			if ( (LA201_0=='\t'||(LA201_0 >= ' ' && LA201_0 <= '!')||(LA201_0 >= '#' && LA201_0 <= '&')||(LA201_0 >= ')' && LA201_0 <= ';')||LA201_0=='='||(LA201_0 >= '?' && LA201_0 <= '\\')||LA201_0=='_'||(LA201_0 >= 'a' && LA201_0 <= '~')||(LA201_0 >= '\u0080' && LA201_0 <= '\uFFFF')) ) {
				alt201=1;
			}
			else if ( (LA201_0=='\"'||LA201_0=='\'') ) {
				alt201=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 201, 0, input);
				throw nvae;
			}

			switch (alt201) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2155:26: URL
					{
					mURL(); if (state.failed) return;

					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2155:30: STRING
					{
					mSTRING(); if (state.failed) return;

					}
					break;

			}

			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2155:38: ( WS )?
			int alt202=2;
			int LA202_0 = input.LA(1);
			if ( (LA202_0=='\t'||LA202_0==' ') ) {
				alt202=1;
			}
			switch (alt202) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2155:38: WS
					{
					mWS(); if (state.failed) return;

					}
					break;

			}

			match(')'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "MOZ_URL_PREFIX"

	// $ANTLR start "MOZ_DOMAIN"
	public final void mMOZ_DOMAIN() throws RecognitionException {
		try {
			int _type = MOZ_DOMAIN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2161:2: ( 'DOMAIN(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2162:2: 'DOMAIN(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')'
			{
			match("DOMAIN("); if (state.failed) return;

			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2163:13: ( ( WS )=> WS )?
			int alt203=2;
			int LA203_0 = input.LA(1);
			if ( (LA203_0=='\t'||LA203_0==' ') ) {
				int LA203_1 = input.LA(2);
				if ( (synpred15_Css3()) ) {
					alt203=1;
				}
			}
			switch (alt203) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2163:14: ( WS )=> WS
					{
					mWS(); if (state.failed) return;

					}
					break;

			}

			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2163:25: ( URL | STRING )
			int alt204=2;
			int LA204_0 = input.LA(1);
			if ( (LA204_0=='\t'||(LA204_0 >= ' ' && LA204_0 <= '!')||(LA204_0 >= '#' && LA204_0 <= '&')||(LA204_0 >= ')' && LA204_0 <= ';')||LA204_0=='='||(LA204_0 >= '?' && LA204_0 <= '\\')||LA204_0=='_'||(LA204_0 >= 'a' && LA204_0 <= '~')||(LA204_0 >= '\u0080' && LA204_0 <= '\uFFFF')) ) {
				alt204=1;
			}
			else if ( (LA204_0=='\"'||LA204_0=='\'') ) {
				alt204=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 204, 0, input);
				throw nvae;
			}

			switch (alt204) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2163:26: URL
					{
					mURL(); if (state.failed) return;

					}
					break;
				case 2 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2163:30: STRING
					{
					mSTRING(); if (state.failed) return;

					}
					break;

			}

			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2163:38: ( WS )?
			int alt205=2;
			int LA205_0 = input.LA(1);
			if ( (LA205_0=='\t'||LA205_0==' ') ) {
				alt205=1;
			}
			switch (alt205) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2163:38: WS
					{
					mWS(); if (state.failed) return;

					}
					break;

			}

			match(')'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "MOZ_DOMAIN"

	// $ANTLR start "MOZ_REGEXP"
	public final void mMOZ_REGEXP() throws RecognitionException {
		try {
			int _type = MOZ_REGEXP;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2169:2: ( 'REGEXP(' ( ( WS )=> WS )? STRING ( WS )? ')' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2170:2: 'REGEXP(' ( ( WS )=> WS )? STRING ( WS )? ')'
			{
			match("REGEXP("); if (state.failed) return;

			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2171:13: ( ( WS )=> WS )?
			int alt206=2;
			int LA206_0 = input.LA(1);
			if ( (LA206_0=='\t'||LA206_0==' ') && (synpred16_Css3())) {
				alt206=1;
			}
			switch (alt206) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2171:14: ( WS )=> WS
					{
					mWS(); if (state.failed) return;

					}
					break;

			}

			mSTRING(); if (state.failed) return;

			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2171:32: ( WS )?
			int alt207=2;
			int LA207_0 = input.LA(1);
			if ( (LA207_0=='\t'||LA207_0==' ') ) {
				alt207=1;
			}
			switch (alt207) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2171:32: WS
					{
					mWS(); if (state.failed) return;

					}
					break;

			}

			match(')'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "MOZ_REGEXP"

	// $ANTLR start "WS"
	public final void mWS() throws RecognitionException {
		try {
			int _type = WS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2182:5: ( ( ' ' | '\\t' )+ )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2183:5: ( ' ' | '\\t' )+
			{
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2183:5: ( ' ' | '\\t' )+
			int cnt208=0;
			loop208:
			while (true) {
				int alt208=2;
				int LA208_0 = input.LA(1);
				if ( (LA208_0=='\t'||LA208_0==' ') ) {
					alt208=1;
				}

				switch (alt208) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
					{
					if ( input.LA(1)=='\t'||input.LA(1)==' ' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt208 >= 1 ) break loop208;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(208, input);
					throw eee;
				}
				cnt208++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WS"

	// $ANTLR start "NL"
	public final void mNL() throws RecognitionException {
		try {
			int _type = NL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2187:5: ( ( '\\r' | '\\n' )+ )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2189:5: ( '\\r' | '\\n' )+
			{
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2189:5: ( '\\r' | '\\n' )+
			int cnt209=0;
			loop209:
			while (true) {
				int alt209=2;
				int LA209_0 = input.LA(1);
				if ( (LA209_0=='\n'||LA209_0=='\r') ) {
					alt209=1;
				}

				switch (alt209) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
					{
					if ( input.LA(1)=='\n'||input.LA(1)=='\r' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt209 >= 1 ) break loop209;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(209, input);
					throw eee;
				}
				cnt209++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NL"

	// $ANTLR start "COMMENT"
	public final void mCOMMENT() throws RecognitionException {
		try {
			int _type = COMMENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2195:5: ( '/*' ( options {greedy=false; } : ( . )* ) '*/' )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2196:5: '/*' ( options {greedy=false; } : ( . )* ) '*/'
			{
			match("/*"); if (state.failed) return;

			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2196:10: ( options {greedy=false; } : ( . )* )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2196:40: ( . )*
			{
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2196:40: ( . )*
			loop210:
			while (true) {
				int alt210=2;
				int LA210_0 = input.LA(1);
				if ( (LA210_0=='*') ) {
					int LA210_1 = input.LA(2);
					if ( (LA210_1=='/') ) {
						alt210=2;
					}
					else if ( ((LA210_1 >= '\u0000' && LA210_1 <= '.')||(LA210_1 >= '0' && LA210_1 <= '\uFFFF')) ) {
						alt210=1;
					}

				}
				else if ( ((LA210_0 >= '\u0000' && LA210_0 <= ')')||(LA210_0 >= '+' && LA210_0 <= '\uFFFF')) ) {
					alt210=1;
				}

				switch (alt210) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2196:40: .
					{
					matchAny(); if (state.failed) return;
					}
					break;

				default :
					break loop210;
				}
			}

			}

			match("*/"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "COMMENT"

	// $ANTLR start "LINE_COMMENT"
	public final void mLINE_COMMENT() throws RecognitionException {
		try {
			int _type = LINE_COMMENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2200:5: ( '//' ( options {greedy=false; } : (~ ( '\\r' | '\\n' ) )* ) )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2201:5: '//' ( options {greedy=false; } : (~ ( '\\r' | '\\n' ) )* )
			{
			match("//"); if (state.failed) return;

			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2201:9: ( options {greedy=false; } : (~ ( '\\r' | '\\n' ) )* )
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2201:39: (~ ( '\\r' | '\\n' ) )*
			{
			// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2201:39: (~ ( '\\r' | '\\n' ) )*
			loop211:
			while (true) {
				int alt211=2;
				int LA211_0 = input.LA(1);
				if ( ((LA211_0 >= '\u0000' && LA211_0 <= '\t')||(LA211_0 >= '\u000B' && LA211_0 <= '\f')||(LA211_0 >= '\u000E' && LA211_0 <= '\uFFFF')) ) {
					alt211=1;
				}

				switch (alt211) {
				case 1 :
					// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop211;
				}
			}

			}

			if ( state.backtracking==0 ) {
				if (isCssPreprocessorSource()) {_channel = HIDDEN;}
			    }
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LINE_COMMENT"

	@Override
	public void mTokens() throws RecognitionException {
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:8: ( GEN | CDO | CDC | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS | GREATER | LBRACE | RBRACE | LBRACKET | RBRACKET | OPEQ | SEMI | COLON | DCOLON | SOLIDUS | MINUS | PLUS | STAR | LPAREN | RPAREN | COMMA | DOT | TILDE | PIPE | PERCENTAGE_SYMBOL | EXCLAMATION_MARK | CP_EQ | CP_NOT_EQ | LESS | GREATER_OR_EQ | LESS_OR_EQ | LESS_AND | CP_DOTS | LESS_REST | STRING | LESS_JS_STRING | NOT | VARIABLE | IDENT | HASH_SYMBOL | HASH | IMPORTANT_SYM | IMPORT_SYM | PAGE_SYM | MEDIA_SYM | NAMESPACE_SYM | CHARSET_SYM | COUNTER_STYLE_SYM | FONT_FACE_SYM | SUPPORTS_SYM | LAYER_SYM | CONTAINER_SYM | TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM | MOZ_DOCUMENT_SYM | WEBKIT_KEYFRAMES_SYM | SASS_CONTENT | SASS_MIXIN | SASS_INCLUDE | SASS_EXTEND | SASS_DEBUG | SASS_ERROR | SASS_WARN | SASS_IF | SASS_ELSE | SASS_ELSEIF | SASS_FOR | SASS_FUNCTION | SASS_RETURN | SASS_USE | SASS_FORWARD | SASS_EACH | SASS_WHILE | SASS_AT_ROOT | AT_SIGN | AT_IDENT | SASS_VAR | SASS_DEFAULT | SASS_OPTIONAL | SASS_GLOBAL | SASS_EXTEND_ONLY_SELECTOR | NUMBER | URI | URANGE | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP | WS | NL | COMMENT | LINE_COMMENT )
		int alt212=108;
		alt212 = dfa212.predict(input);
		switch (alt212) {
			case 1 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:10: GEN
				{
				mGEN(); if (state.failed) return;

				}
				break;
			case 2 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:14: CDO
				{
				mCDO(); if (state.failed) return;

				}
				break;
			case 3 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:18: CDC
				{
				mCDC(); if (state.failed) return;

				}
				break;
			case 4 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:22: INCLUDES
				{
				mINCLUDES(); if (state.failed) return;

				}
				break;
			case 5 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:31: DASHMATCH
				{
				mDASHMATCH(); if (state.failed) return;

				}
				break;
			case 6 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:41: BEGINS
				{
				mBEGINS(); if (state.failed) return;

				}
				break;
			case 7 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:48: ENDS
				{
				mENDS(); if (state.failed) return;

				}
				break;
			case 8 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:53: CONTAINS
				{
				mCONTAINS(); if (state.failed) return;

				}
				break;
			case 9 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:62: GREATER
				{
				mGREATER(); if (state.failed) return;

				}
				break;
			case 10 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:70: LBRACE
				{
				mLBRACE(); if (state.failed) return;

				}
				break;
			case 11 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:77: RBRACE
				{
				mRBRACE(); if (state.failed) return;

				}
				break;
			case 12 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:84: LBRACKET
				{
				mLBRACKET(); if (state.failed) return;

				}
				break;
			case 13 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:93: RBRACKET
				{
				mRBRACKET(); if (state.failed) return;

				}
				break;
			case 14 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:102: OPEQ
				{
				mOPEQ(); if (state.failed) return;

				}
				break;
			case 15 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:107: SEMI
				{
				mSEMI(); if (state.failed) return;

				}
				break;
			case 16 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:112: COLON
				{
				mCOLON(); if (state.failed) return;

				}
				break;
			case 17 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:118: DCOLON
				{
				mDCOLON(); if (state.failed) return;

				}
				break;
			case 18 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:125: SOLIDUS
				{
				mSOLIDUS(); if (state.failed) return;

				}
				break;
			case 19 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:133: MINUS
				{
				mMINUS(); if (state.failed) return;

				}
				break;
			case 20 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:139: PLUS
				{
				mPLUS(); if (state.failed) return;

				}
				break;
			case 21 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:144: STAR
				{
				mSTAR(); if (state.failed) return;

				}
				break;
			case 22 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:149: LPAREN
				{
				mLPAREN(); if (state.failed) return;

				}
				break;
			case 23 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:156: RPAREN
				{
				mRPAREN(); if (state.failed) return;

				}
				break;
			case 24 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:163: COMMA
				{
				mCOMMA(); if (state.failed) return;

				}
				break;
			case 25 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:169: DOT
				{
				mDOT(); if (state.failed) return;

				}
				break;
			case 26 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:173: TILDE
				{
				mTILDE(); if (state.failed) return;

				}
				break;
			case 27 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:179: PIPE
				{
				mPIPE(); if (state.failed) return;

				}
				break;
			case 28 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:184: PERCENTAGE_SYMBOL
				{
				mPERCENTAGE_SYMBOL(); if (state.failed) return;

				}
				break;
			case 29 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:202: EXCLAMATION_MARK
				{
				mEXCLAMATION_MARK(); if (state.failed) return;

				}
				break;
			case 30 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:219: CP_EQ
				{
				mCP_EQ(); if (state.failed) return;

				}
				break;
			case 31 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:225: CP_NOT_EQ
				{
				mCP_NOT_EQ(); if (state.failed) return;

				}
				break;
			case 32 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:235: LESS
				{
				mLESS(); if (state.failed) return;

				}
				break;
			case 33 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:240: GREATER_OR_EQ
				{
				mGREATER_OR_EQ(); if (state.failed) return;

				}
				break;
			case 34 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:254: LESS_OR_EQ
				{
				mLESS_OR_EQ(); if (state.failed) return;

				}
				break;
			case 35 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:265: LESS_AND
				{
				mLESS_AND(); if (state.failed) return;

				}
				break;
			case 36 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:274: CP_DOTS
				{
				mCP_DOTS(); if (state.failed) return;

				}
				break;
			case 37 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:282: LESS_REST
				{
				mLESS_REST(); if (state.failed) return;

				}
				break;
			case 38 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:292: STRING
				{
				mSTRING(); if (state.failed) return;

				}
				break;
			case 39 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:299: LESS_JS_STRING
				{
				mLESS_JS_STRING(); if (state.failed) return;

				}
				break;
			case 40 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:314: NOT
				{
				mNOT(); if (state.failed) return;

				}
				break;
			case 41 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:318: VARIABLE
				{
				mVARIABLE(); if (state.failed) return;

				}
				break;
			case 42 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:327: IDENT
				{
				mIDENT(); if (state.failed) return;

				}
				break;
			case 43 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:333: HASH_SYMBOL
				{
				mHASH_SYMBOL(); if (state.failed) return;

				}
				break;
			case 44 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:345: HASH
				{
				mHASH(); if (state.failed) return;

				}
				break;
			case 45 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:350: IMPORTANT_SYM
				{
				mIMPORTANT_SYM(); if (state.failed) return;

				}
				break;
			case 46 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:364: IMPORT_SYM
				{
				mIMPORT_SYM(); if (state.failed) return;

				}
				break;
			case 47 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:375: PAGE_SYM
				{
				mPAGE_SYM(); if (state.failed) return;

				}
				break;
			case 48 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:384: MEDIA_SYM
				{
				mMEDIA_SYM(); if (state.failed) return;

				}
				break;
			case 49 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:394: NAMESPACE_SYM
				{
				mNAMESPACE_SYM(); if (state.failed) return;

				}
				break;
			case 50 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:408: CHARSET_SYM
				{
				mCHARSET_SYM(); if (state.failed) return;

				}
				break;
			case 51 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:420: COUNTER_STYLE_SYM
				{
				mCOUNTER_STYLE_SYM(); if (state.failed) return;

				}
				break;
			case 52 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:438: FONT_FACE_SYM
				{
				mFONT_FACE_SYM(); if (state.failed) return;

				}
				break;
			case 53 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:452: SUPPORTS_SYM
				{
				mSUPPORTS_SYM(); if (state.failed) return;

				}
				break;
			case 54 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:465: LAYER_SYM
				{
				mLAYER_SYM(); if (state.failed) return;

				}
				break;
			case 55 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:475: CONTAINER_SYM
				{
				mCONTAINER_SYM(); if (state.failed) return;

				}
				break;
			case 56 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:489: TOPLEFTCORNER_SYM
				{
				mTOPLEFTCORNER_SYM(); if (state.failed) return;

				}
				break;
			case 57 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:507: TOPLEFT_SYM
				{
				mTOPLEFT_SYM(); if (state.failed) return;

				}
				break;
			case 58 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:519: TOPCENTER_SYM
				{
				mTOPCENTER_SYM(); if (state.failed) return;

				}
				break;
			case 59 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:533: TOPRIGHT_SYM
				{
				mTOPRIGHT_SYM(); if (state.failed) return;

				}
				break;
			case 60 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:546: TOPRIGHTCORNER_SYM
				{
				mTOPRIGHTCORNER_SYM(); if (state.failed) return;

				}
				break;
			case 61 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:565: BOTTOMLEFTCORNER_SYM
				{
				mBOTTOMLEFTCORNER_SYM(); if (state.failed) return;

				}
				break;
			case 62 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:586: BOTTOMLEFT_SYM
				{
				mBOTTOMLEFT_SYM(); if (state.failed) return;

				}
				break;
			case 63 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:601: BOTTOMCENTER_SYM
				{
				mBOTTOMCENTER_SYM(); if (state.failed) return;

				}
				break;
			case 64 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:618: BOTTOMRIGHT_SYM
				{
				mBOTTOMRIGHT_SYM(); if (state.failed) return;

				}
				break;
			case 65 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:634: BOTTOMRIGHTCORNER_SYM
				{
				mBOTTOMRIGHTCORNER_SYM(); if (state.failed) return;

				}
				break;
			case 66 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:656: LEFTTOP_SYM
				{
				mLEFTTOP_SYM(); if (state.failed) return;

				}
				break;
			case 67 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:668: LEFTMIDDLE_SYM
				{
				mLEFTMIDDLE_SYM(); if (state.failed) return;

				}
				break;
			case 68 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:683: LEFTBOTTOM_SYM
				{
				mLEFTBOTTOM_SYM(); if (state.failed) return;

				}
				break;
			case 69 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:698: RIGHTTOP_SYM
				{
				mRIGHTTOP_SYM(); if (state.failed) return;

				}
				break;
			case 70 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:711: RIGHTMIDDLE_SYM
				{
				mRIGHTMIDDLE_SYM(); if (state.failed) return;

				}
				break;
			case 71 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:727: RIGHTBOTTOM_SYM
				{
				mRIGHTBOTTOM_SYM(); if (state.failed) return;

				}
				break;
			case 72 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:743: MOZ_DOCUMENT_SYM
				{
				mMOZ_DOCUMENT_SYM(); if (state.failed) return;

				}
				break;
			case 73 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:760: WEBKIT_KEYFRAMES_SYM
				{
				mWEBKIT_KEYFRAMES_SYM(); if (state.failed) return;

				}
				break;
			case 74 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:781: SASS_CONTENT
				{
				mSASS_CONTENT(); if (state.failed) return;

				}
				break;
			case 75 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:794: SASS_MIXIN
				{
				mSASS_MIXIN(); if (state.failed) return;

				}
				break;
			case 76 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:805: SASS_INCLUDE
				{
				mSASS_INCLUDE(); if (state.failed) return;

				}
				break;
			case 77 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:818: SASS_EXTEND
				{
				mSASS_EXTEND(); if (state.failed) return;

				}
				break;
			case 78 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:830: SASS_DEBUG
				{
				mSASS_DEBUG(); if (state.failed) return;

				}
				break;
			case 79 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:841: SASS_ERROR
				{
				mSASS_ERROR(); if (state.failed) return;

				}
				break;
			case 80 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:852: SASS_WARN
				{
				mSASS_WARN(); if (state.failed) return;

				}
				break;
			case 81 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:862: SASS_IF
				{
				mSASS_IF(); if (state.failed) return;

				}
				break;
			case 82 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:870: SASS_ELSE
				{
				mSASS_ELSE(); if (state.failed) return;

				}
				break;
			case 83 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:880: SASS_ELSEIF
				{
				mSASS_ELSEIF(); if (state.failed) return;

				}
				break;
			case 84 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:892: SASS_FOR
				{
				mSASS_FOR(); if (state.failed) return;

				}
				break;
			case 85 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:901: SASS_FUNCTION
				{
				mSASS_FUNCTION(); if (state.failed) return;

				}
				break;
			case 86 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:915: SASS_RETURN
				{
				mSASS_RETURN(); if (state.failed) return;

				}
				break;
			case 87 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:927: SASS_USE
				{
				mSASS_USE(); if (state.failed) return;

				}
				break;
			case 88 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:936: SASS_FORWARD
				{
				mSASS_FORWARD(); if (state.failed) return;

				}
				break;
			case 89 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:949: SASS_EACH
				{
				mSASS_EACH(); if (state.failed) return;

				}
				break;
			case 90 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:959: SASS_WHILE
				{
				mSASS_WHILE(); if (state.failed) return;

				}
				break;
			case 91 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:970: SASS_AT_ROOT
				{
				mSASS_AT_ROOT(); if (state.failed) return;

				}
				break;
			case 92 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:983: AT_SIGN
				{
				mAT_SIGN(); if (state.failed) return;

				}
				break;
			case 93 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:991: AT_IDENT
				{
				mAT_IDENT(); if (state.failed) return;

				}
				break;
			case 94 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1000: SASS_VAR
				{
				mSASS_VAR(); if (state.failed) return;

				}
				break;
			case 95 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1009: SASS_DEFAULT
				{
				mSASS_DEFAULT(); if (state.failed) return;

				}
				break;
			case 96 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1022: SASS_OPTIONAL
				{
				mSASS_OPTIONAL(); if (state.failed) return;

				}
				break;
			case 97 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1036: SASS_GLOBAL
				{
				mSASS_GLOBAL(); if (state.failed) return;

				}
				break;
			case 98 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1048: SASS_EXTEND_ONLY_SELECTOR
				{
				mSASS_EXTEND_ONLY_SELECTOR(); if (state.failed) return;

				}
				break;
			case 99 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1074: NUMBER
				{
				mNUMBER(); if (state.failed) return;

				}
				break;
			case 100 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1081: URI
				{
				mURI(); if (state.failed) return;

				}
				break;
			case 101 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1085: URANGE
				{
				mURANGE(); if (state.failed) return;

				}
				break;
			case 102 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1092: MOZ_URL_PREFIX
				{
				mMOZ_URL_PREFIX(); if (state.failed) return;

				}
				break;
			case 103 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1107: MOZ_DOMAIN
				{
				mMOZ_DOMAIN(); if (state.failed) return;

				}
				break;
			case 104 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1118: MOZ_REGEXP
				{
				mMOZ_REGEXP(); if (state.failed) return;

				}
				break;
			case 105 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1129: WS
				{
				mWS(); if (state.failed) return;

				}
				break;
			case 106 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1132: NL
				{
				mNL(); if (state.failed) return;

				}
				break;
			case 107 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1135: COMMENT
				{
				mCOMMENT(); if (state.failed) return;

				}
				break;
			case 108 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1143: LINE_COMMENT
				{
				mLINE_COMMENT(); if (state.failed) return;

				}
				break;

		}
	}

	// $ANTLR start synpred1_Css3
	public final void synpred1_Css3_fragment() throws RecognitionException {
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1958:26: ( '\\\\\\\"' )
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1958:27: '\\\\\\\"'
		{
		match("\\\""); if (state.failed) return;

		}

	}
	// $ANTLR end synpred1_Css3

	// $ANTLR start synpred2_Css3
	public final void synpred2_Css3_fragment() throws RecognitionException {
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1958:47: ( '\\\\\\\\' )
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1958:48: '\\\\\\\\'
		{
		match("\\\\"); if (state.failed) return;

		}

	}
	// $ANTLR end synpred2_Css3

	// $ANTLR start synpred3_Css3
	public final void synpred3_Css3_fragment() throws RecognitionException {
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2081:15: ( D P ( I | C ) )
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2081:16: D P ( I | C )
		{
		mD(); if (state.failed) return;

		mP(); if (state.failed) return;

		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2081:20: ( I | C )
		int alt213=2;
		switch ( input.LA(1) ) {
		case 'I':
		case 'i':
			{
			alt213=1;
			}
			break;
		case '\\':
			{
			switch ( input.LA(2) ) {
			case 'I':
			case 'i':
				{
				alt213=1;
				}
				break;
			case '0':
				{
				int LA213_4 = input.LA(3);
				if ( (LA213_4=='0') ) {
					int LA213_6 = input.LA(4);
					if ( (LA213_6=='0') ) {
						int LA213_7 = input.LA(5);
						if ( (LA213_7=='0') ) {
							int LA213_8 = input.LA(6);
							if ( (LA213_8=='4'||LA213_8=='6') ) {
								int LA213_5 = input.LA(7);
								if ( (LA213_5=='9') ) {
									alt213=1;
								}
								else if ( (LA213_5=='3') ) {
									alt213=2;
								}

								else {
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 7 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 213, 5, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 213, 8, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}
						else if ( (LA213_7=='4'||LA213_7=='6') ) {
							int LA213_5 = input.LA(6);
							if ( (LA213_5=='9') ) {
								alt213=1;
							}
							else if ( (LA213_5=='3') ) {
								alt213=2;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 213, 5, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}

						else {
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 213, 7, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}
					else if ( (LA213_6=='4'||LA213_6=='6') ) {
						int LA213_5 = input.LA(5);
						if ( (LA213_5=='9') ) {
							alt213=1;
						}
						else if ( (LA213_5=='3') ) {
							alt213=2;
						}

						else {
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 213, 5, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						if (state.backtracking>0) {state.failed=true; return;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 213, 6, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}
				else if ( (LA213_4=='4'||LA213_4=='6') ) {
					int LA213_5 = input.LA(4);
					if ( (LA213_5=='9') ) {
						alt213=1;
					}
					else if ( (LA213_5=='3') ) {
						alt213=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 213, 5, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
							input.consume();
						}
						NoViableAltException nvae =
							new NoViableAltException("", 213, 4, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case '4':
			case '6':
				{
				int LA213_5 = input.LA(3);
				if ( (LA213_5=='9') ) {
					alt213=1;
				}
				else if ( (LA213_5=='3') ) {
					alt213=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
							input.consume();
						}
						NoViableAltException nvae =
							new NoViableAltException("", 213, 5, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				int nvaeMark = input.mark();
				try {
					input.consume();
					NoViableAltException nvae =
						new NoViableAltException("", 213, 2, input);
					throw nvae;
				} finally {
					input.rewind(nvaeMark);
				}
			}
			}
			break;
		case 'C':
		case 'c':
			{
			alt213=2;
			}
			break;
		default:
			if (state.backtracking>0) {state.failed=true; return;}
			NoViableAltException nvae =
				new NoViableAltException("", 213, 0, input);
			throw nvae;
		}
		switch (alt213) {
			case 1 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2081:21: I
				{
				mI(); if (state.failed) return;

				}
				break;
			case 2 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2081:23: C
				{
				mC(); if (state.failed) return;

				}
				break;

		}

		}

	}
	// $ANTLR end synpred3_Css3

	// $ANTLR start synpred4_Css3
	public final void synpred4_Css3_fragment() throws RecognitionException {
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2088:15: ( E ( M | X ) )
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2088:16: E ( M | X )
		{
		mE(); if (state.failed) return;

		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2088:18: ( M | X )
		int alt214=2;
		switch ( input.LA(1) ) {
		case 'M':
		case 'm':
			{
			alt214=1;
			}
			break;
		case '\\':
			{
			switch ( input.LA(2) ) {
			case '4':
			case '6':
			case 'M':
			case 'm':
				{
				alt214=1;
				}
				break;
			case '0':
				{
				switch ( input.LA(3) ) {
				case '0':
					{
					switch ( input.LA(4) ) {
					case '0':
						{
						switch ( input.LA(5) ) {
						case '0':
							{
							int LA214_7 = input.LA(6);
							if ( (LA214_7=='4'||LA214_7=='6') ) {
								alt214=1;
							}
							else if ( (LA214_7=='5'||LA214_7=='7') ) {
								alt214=2;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 214, 7, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

							}
							break;
						case '4':
						case '6':
							{
							alt214=1;
							}
							break;
						case '5':
						case '7':
							{
							alt214=2;
							}
							break;
						default:
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 214, 6, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}
						}
						break;
					case '4':
					case '6':
						{
						alt214=1;
						}
						break;
					case '5':
					case '7':
						{
						alt214=2;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 214, 5, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}
					}
					break;
				case '4':
				case '6':
					{
					alt214=1;
					}
					break;
				case '5':
				case '7':
					{
					alt214=2;
					}
					break;
				default:
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
							input.consume();
						}
						NoViableAltException nvae =
							new NoViableAltException("", 214, 4, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
				}
				break;
			case '5':
			case '7':
			case 'X':
			case 'x':
				{
				alt214=2;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				int nvaeMark = input.mark();
				try {
					input.consume();
					NoViableAltException nvae =
						new NoViableAltException("", 214, 2, input);
					throw nvae;
				} finally {
					input.rewind(nvaeMark);
				}
			}
			}
			break;
		case 'X':
		case 'x':
			{
			alt214=2;
			}
			break;
		default:
			if (state.backtracking>0) {state.failed=true; return;}
			NoViableAltException nvae =
				new NoViableAltException("", 214, 0, input);
			throw nvae;
		}
		switch (alt214) {
			case 1 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2088:19: M
				{
				mM(); if (state.failed) return;

				}
				break;
			case 2 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2088:21: X
				{
				mX(); if (state.failed) return;

				}
				break;

		}

		}

	}
	// $ANTLR end synpred4_Css3

	// $ANTLR start synpred5_Css3
	public final void synpred5_Css3_fragment() throws RecognitionException {
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2094:15: ( P ( X | T | C ) )
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2094:16: P ( X | T | C )
		{
		mP(); if (state.failed) return;

		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2094:17: ( X | T | C )
		int alt215=3;
		switch ( input.LA(1) ) {
		case 'X':
		case 'x':
			{
			alt215=1;
			}
			break;
		case '\\':
			{
			switch ( input.LA(2) ) {
			case 'X':
			case 'x':
				{
				alt215=1;
				}
				break;
			case '0':
				{
				switch ( input.LA(3) ) {
				case '0':
					{
					switch ( input.LA(4) ) {
					case '0':
						{
						switch ( input.LA(5) ) {
						case '0':
							{
							int LA215_9 = input.LA(6);
							if ( (LA215_9=='5'||LA215_9=='7') ) {
								int LA215_6 = input.LA(7);
								if ( (LA215_6=='8') ) {
									alt215=1;
								}
								else if ( (LA215_6=='4') ) {
									alt215=2;
								}

								else {
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 7 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 215, 6, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}
							else if ( (LA215_9=='4'||LA215_9=='6') ) {
								alt215=3;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 215, 9, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

							}
							break;
						case '5':
						case '7':
							{
							int LA215_6 = input.LA(6);
							if ( (LA215_6=='8') ) {
								alt215=1;
							}
							else if ( (LA215_6=='4') ) {
								alt215=2;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 215, 6, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

							}
							break;
						case '4':
						case '6':
							{
							alt215=3;
							}
							break;
						default:
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 215, 8, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}
						}
						break;
					case '5':
					case '7':
						{
						int LA215_6 = input.LA(5);
						if ( (LA215_6=='8') ) {
							alt215=1;
						}
						else if ( (LA215_6=='4') ) {
							alt215=2;
						}

						else {
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 215, 6, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

						}
						break;
					case '4':
					case '6':
						{
						alt215=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 215, 7, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}
					}
					break;
				case '5':
				case '7':
					{
					int LA215_6 = input.LA(4);
					if ( (LA215_6=='8') ) {
						alt215=1;
					}
					else if ( (LA215_6=='4') ) {
						alt215=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 215, 6, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

					}
					break;
				case '4':
				case '6':
					{
					alt215=3;
					}
					break;
				default:
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
							input.consume();
						}
						NoViableAltException nvae =
							new NoViableAltException("", 215, 5, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
				}
				break;
			case '5':
			case '7':
				{
				int LA215_6 = input.LA(3);
				if ( (LA215_6=='8') ) {
					alt215=1;
				}
				else if ( (LA215_6=='4') ) {
					alt215=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
							input.consume();
						}
						NoViableAltException nvae =
							new NoViableAltException("", 215, 6, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 'T':
			case 't':
				{
				alt215=2;
				}
				break;
			case '4':
			case '6':
				{
				alt215=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				int nvaeMark = input.mark();
				try {
					input.consume();
					NoViableAltException nvae =
						new NoViableAltException("", 215, 2, input);
					throw nvae;
				} finally {
					input.rewind(nvaeMark);
				}
			}
			}
			break;
		case 'T':
		case 't':
			{
			alt215=2;
			}
			break;
		case 'C':
		case 'c':
			{
			alt215=3;
			}
			break;
		default:
			if (state.backtracking>0) {state.failed=true; return;}
			NoViableAltException nvae =
				new NoViableAltException("", 215, 0, input);
			throw nvae;
		}
		switch (alt215) {
			case 1 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2094:18: X
				{
				mX(); if (state.failed) return;

				}
				break;
			case 2 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2094:20: T
				{
				mT(); if (state.failed) return;

				}
				break;
			case 3 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2094:22: C
				{
				mC(); if (state.failed) return;

				}
				break;

		}

		}

	}
	// $ANTLR end synpred5_Css3

	// $ANTLR start synpred6_Css3
	public final void synpred6_Css3_fragment() throws RecognitionException {
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2102:15: ( C M )
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2102:16: C M
		{
		mC(); if (state.failed) return;

		mM(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred6_Css3

	// $ANTLR start synpred7_Css3
	public final void synpred7_Css3_fragment() throws RecognitionException {
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2104:15: ( M ( M | S ) )
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2104:16: M ( M | S )
		{
		mM(); if (state.failed) return;

		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2104:18: ( M | S )
		int alt216=2;
		switch ( input.LA(1) ) {
		case 'M':
		case 'm':
			{
			alt216=1;
			}
			break;
		case '\\':
			{
			switch ( input.LA(2) ) {
			case '4':
			case '6':
			case 'M':
			case 'm':
				{
				alt216=1;
				}
				break;
			case '0':
				{
				switch ( input.LA(3) ) {
				case '0':
					{
					switch ( input.LA(4) ) {
					case '0':
						{
						switch ( input.LA(5) ) {
						case '0':
							{
							int LA216_7 = input.LA(6);
							if ( (LA216_7=='4'||LA216_7=='6') ) {
								alt216=1;
							}
							else if ( (LA216_7=='5'||LA216_7=='7') ) {
								alt216=2;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 216, 7, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

							}
							break;
						case '4':
						case '6':
							{
							alt216=1;
							}
							break;
						case '5':
						case '7':
							{
							alt216=2;
							}
							break;
						default:
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 216, 6, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}
						}
						break;
					case '4':
					case '6':
						{
						alt216=1;
						}
						break;
					case '5':
					case '7':
						{
						alt216=2;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 216, 5, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}
					}
					break;
				case '4':
				case '6':
					{
					alt216=1;
					}
					break;
				case '5':
				case '7':
					{
					alt216=2;
					}
					break;
				default:
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
							input.consume();
						}
						NoViableAltException nvae =
							new NoViableAltException("", 216, 4, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
				}
				break;
			case '5':
			case '7':
			case 'S':
			case 's':
				{
				alt216=2;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				int nvaeMark = input.mark();
				try {
					input.consume();
					NoViableAltException nvae =
						new NoViableAltException("", 216, 2, input);
					throw nvae;
				} finally {
					input.rewind(nvaeMark);
				}
			}
			}
			break;
		case 'S':
		case 's':
			{
			alt216=2;
			}
			break;
		default:
			if (state.backtracking>0) {state.failed=true; return;}
			NoViableAltException nvae =
				new NoViableAltException("", 216, 0, input);
			throw nvae;
		}
		switch (alt216) {
			case 1 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2104:19: M
				{
				mM(); if (state.failed) return;

				}
				break;
			case 2 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2104:21: S
				{
				mS(); if (state.failed) return;

				}
				break;

		}

		}

	}
	// $ANTLR end synpred7_Css3

	// $ANTLR start synpred8_Css3
	public final void synpred8_Css3_fragment() throws RecognitionException {
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2111:15: ( I N )
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2111:16: I N
		{
		mI(); if (state.failed) return;

		mN(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred8_Css3

	// $ANTLR start synpred9_Css3
	public final void synpred9_Css3_fragment() throws RecognitionException {
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2114:15: ( D E G )
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2114:16: D E G
		{
		mD(); if (state.failed) return;

		mE(); if (state.failed) return;

		mG(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred9_Css3

	// $ANTLR start synpred10_Css3
	public final void synpred10_Css3_fragment() throws RecognitionException {
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2119:15: ( R ( A | E ) )
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2119:16: R ( A | E )
		{
		mR(); if (state.failed) return;

		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2119:18: ( A | E )
		int alt217=2;
		switch ( input.LA(1) ) {
		case 'A':
		case 'a':
			{
			alt217=1;
			}
			break;
		case '\\':
			{
			int LA217_2 = input.LA(2);
			if ( (LA217_2=='0') ) {
				int LA217_4 = input.LA(3);
				if ( (LA217_4=='0') ) {
					int LA217_6 = input.LA(4);
					if ( (LA217_6=='0') ) {
						int LA217_7 = input.LA(5);
						if ( (LA217_7=='0') ) {
							int LA217_8 = input.LA(6);
							if ( (LA217_8=='4'||LA217_8=='6') ) {
								int LA217_5 = input.LA(7);
								if ( (LA217_5=='1') ) {
									alt217=1;
								}
								else if ( (LA217_5=='5') ) {
									alt217=2;
								}

								else {
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 7 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 217, 5, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 217, 8, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}
						else if ( (LA217_7=='4'||LA217_7=='6') ) {
							int LA217_5 = input.LA(6);
							if ( (LA217_5=='1') ) {
								alt217=1;
							}
							else if ( (LA217_5=='5') ) {
								alt217=2;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 217, 5, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}

						else {
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 217, 7, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}
					else if ( (LA217_6=='4'||LA217_6=='6') ) {
						int LA217_5 = input.LA(5);
						if ( (LA217_5=='1') ) {
							alt217=1;
						}
						else if ( (LA217_5=='5') ) {
							alt217=2;
						}

						else {
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 217, 5, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						if (state.backtracking>0) {state.failed=true; return;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 217, 6, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}
				else if ( (LA217_4=='4'||LA217_4=='6') ) {
					int LA217_5 = input.LA(4);
					if ( (LA217_5=='1') ) {
						alt217=1;
					}
					else if ( (LA217_5=='5') ) {
						alt217=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 217, 5, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
							input.consume();
						}
						NoViableAltException nvae =
							new NoViableAltException("", 217, 4, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( (LA217_2=='4'||LA217_2=='6') ) {
				int LA217_5 = input.LA(3);
				if ( (LA217_5=='1') ) {
					alt217=1;
				}
				else if ( (LA217_5=='5') ) {
					alt217=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
							input.consume();
						}
						NoViableAltException nvae =
							new NoViableAltException("", 217, 5, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				int nvaeMark = input.mark();
				try {
					input.consume();
					NoViableAltException nvae =
						new NoViableAltException("", 217, 2, input);
					throw nvae;
				} finally {
					input.rewind(nvaeMark);
				}
			}

			}
			break;
		case 'E':
		case 'e':
			{
			alt217=2;
			}
			break;
		default:
			if (state.backtracking>0) {state.failed=true; return;}
			NoViableAltException nvae =
				new NoViableAltException("", 217, 0, input);
			throw nvae;
		}
		switch (alt217) {
			case 1 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2119:19: A
				{
				mA(); if (state.failed) return;

				}
				break;
			case 2 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2119:21: E
				{
				mE(); if (state.failed) return;

				}
				break;

		}

		}

	}
	// $ANTLR end synpred10_Css3

	// $ANTLR start synpred11_Css3
	public final void synpred11_Css3_fragment() throws RecognitionException {
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2126:15: ( S )
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2126:16: S
		{
		mS(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred11_Css3

	// $ANTLR start synpred12_Css3
	public final void synpred12_Css3_fragment() throws RecognitionException {
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2128:15: ( ( K )? H Z )
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2128:16: ( K )? H Z
		{
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2128:16: ( K )?
		int alt218=2;
		int LA218_0 = input.LA(1);
		if ( (LA218_0=='K'||LA218_0=='k') ) {
			alt218=1;
		}
		else if ( (LA218_0=='\\') ) {
			switch ( input.LA(2) ) {
				case 'K':
				case 'k':
					{
					alt218=1;
					}
					break;
				case '0':
					{
					int LA218_4 = input.LA(3);
					if ( (LA218_4=='0') ) {
						int LA218_6 = input.LA(4);
						if ( (LA218_6=='0') ) {
							int LA218_7 = input.LA(5);
							if ( (LA218_7=='0') ) {
								int LA218_8 = input.LA(6);
								if ( (LA218_8=='4'||LA218_8=='6') ) {
									int LA218_5 = input.LA(7);
									if ( (LA218_5=='B'||LA218_5=='b') ) {
										alt218=1;
									}
								}
							}
							else if ( (LA218_7=='4'||LA218_7=='6') ) {
								int LA218_5 = input.LA(6);
								if ( (LA218_5=='B'||LA218_5=='b') ) {
									alt218=1;
								}
							}
						}
						else if ( (LA218_6=='4'||LA218_6=='6') ) {
							int LA218_5 = input.LA(5);
							if ( (LA218_5=='B'||LA218_5=='b') ) {
								alt218=1;
							}
						}
					}
					else if ( (LA218_4=='4'||LA218_4=='6') ) {
						int LA218_5 = input.LA(4);
						if ( (LA218_5=='B'||LA218_5=='b') ) {
							alt218=1;
						}
					}
					}
					break;
				case '4':
				case '6':
					{
					int LA218_5 = input.LA(3);
					if ( (LA218_5=='B'||LA218_5=='b') ) {
						alt218=1;
					}
					}
					break;
			}
		}
		switch (alt218) {
			case 1 :
				// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2128:16: K
				{
				mK(); if (state.failed) return;

				}
				break;

		}

		mH(); if (state.failed) return;

		mZ(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred12_Css3

	// $ANTLR start synpred13_Css3
	public final void synpred13_Css3_fragment() throws RecognitionException {
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2144:14: ( WS )
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2144:15: WS
		{
		mWS(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred13_Css3

	// $ANTLR start synpred14_Css3
	public final void synpred14_Css3_fragment() throws RecognitionException {
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2155:14: ( WS )
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2155:15: WS
		{
		mWS(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred14_Css3

	// $ANTLR start synpred15_Css3
	public final void synpred15_Css3_fragment() throws RecognitionException {
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2163:14: ( WS )
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2163:15: WS
		{
		mWS(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred15_Css3

	// $ANTLR start synpred16_Css3
	public final void synpred16_Css3_fragment() throws RecognitionException {
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2171:14: ( WS )
		// /home/matthias/src/netbeans/ide/css.lib/src/org/netbeans/modules/css/lib/Css3.g:2171:15: WS
		{
		mWS(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred16_Css3

	public final boolean synpred11_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred11_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred8_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred8_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred2_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred2_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred5_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred5_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred14_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred14_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred9_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred9_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred4_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred4_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred7_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred7_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred16_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred16_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred13_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred13_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred1_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred1_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred10_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred10_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred6_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred6_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred15_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred15_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred12_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred12_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred3_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred3_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}


	protected DFA193 dfa193 = new DFA193(this);
	protected DFA212 dfa212 = new DFA212(this);
	static final String DFA193_eotS =
		"\1\30\1\14\1\uffff\6\14\1\uffff\2\14\1\uffff\7\14\1\uffff\2\14\2\uffff"+
		"\1\14\1\uffff\16\14\2\uffff\4\14\27\uffff\1\14\1\uffff\3\14\1\uffff\1"+
		"\14\1\uffff\1\14\31\uffff\1\14\1\uffff\6\14\15\uffff\14\14\12\uffff\2"+
		"\14\21\uffff\2\14\1\uffff\1\14\4\uffff\2\14\1\uffff\1\14\3\uffff\2\14"+
		"\4\uffff\2\14\1\uffff\1\14\3\uffff\2\14\3\uffff\2\14\11\uffff\4\14\5\uffff"+
		"\2\14\3\uffff\2\14\11\uffff\5\14\3\uffff\20\14\1\uffff\2\14\30\uffff\7"+
		"\14\3\uffff\3\14\3\uffff\2\14\2\uffff\3\14\3\uffff\2\14\3\uffff\6\14\2"+
		"\uffff\2\14\4\uffff\5\14\2\uffff\2\14\1\uffff\1\14\2\uffff\7\14\1\uffff"+
		"\1\14\1\uffff\1\14\2\uffff\2\14\1\uffff\16\14\1\uffff\2\14\30\uffff\4"+
		"\14\14\uffff\3\14\3\uffff\3\14\3\uffff\2\14\2\uffff\3\14\3\uffff\2\14"+
		"\2\uffff\2\14\1\uffff\4\14\4\uffff\2\14\2\uffff\3\14\2\uffff\2\14\2\uffff"+
		"\2\14\1\uffff\1\14\2\uffff\2\14\2\uffff\5\14\1\uffff\1\14\1\uffff\1\14"+
		"\6\uffff\2\14\1\uffff\15\14\1\uffff\2\14\30\uffff\4\14\14\uffff\3\14\3"+
		"\uffff\3\14\3\uffff\2\14\2\uffff\3\14\3\uffff\2\14\2\uffff\2\14\1\uffff"+
		"\4\14\4\uffff\2\14\2\uffff\3\14\2\uffff\2\14\2\uffff\2\14\1\uffff\1\14"+
		"\2\uffff\2\14\2\uffff\4\14\1\uffff\1\14\1\uffff\1\14\6\uffff\2\14\1\uffff"+
		"\13\14\1\uffff\2\14\30\uffff\4\14\14\uffff\2\14\3\uffff\2\14\3\uffff\1"+
		"\14\2\uffff\2\14\3\uffff\1\14\2\uffff\2\14\1\uffff\3\14\4\uffff\2\14\2"+
		"\uffff\2\14\2\uffff\1\14\2\uffff\1\14\1\uffff\1\14\2\uffff\2\14\2\uffff"+
		"\2\14\1\uffff\1\14\1\uffff\1\14\6\uffff\1\14\62\uffff\1\14\1\uffff\2\14"+
		"\4\uffff\1\14\2\uffff\1\14\5\uffff\1\14\2\uffff\1\14\30\uffff";
	static final String DFA193_eofS =
		"\u0349\uffff";
	static final String DFA193_minS =
		"\1\45\1\105\1\0\1\115\1\103\2\115\1\116\1\101\1\0\1\110\1\132\1\uffff"+
		"\1\105\1\115\1\103\2\115\1\116\1\101\1\0\1\110\1\132\2\uffff\1\103\1\0"+
		"\1\107\1\103\1\107\1\103\1\60\1\63\1\103\1\115\1\60\1\115\2\116\2\101"+
		"\2\0\2\110\2\132\27\0\1\104\1\0\1\115\1\104\1\115\1\uffff\1\132\1\0\1"+
		"\132\31\0\1\115\1\0\1\115\2\103\2\60\1\65\15\0\1\60\1\63\1\60\1\105\3"+
		"\115\1\116\1\110\1\132\1\115\1\110\12\0\1\103\1\101\15\0\1\uffff\3\0\1"+
		"\60\1\104\1\0\1\70\1\uffff\3\0\1\60\1\64\1\0\1\63\1\uffff\2\0\1\60\1\104"+
		"\1\uffff\3\0\1\60\1\104\1\0\1\63\1\uffff\2\0\1\60\1\105\3\0\1\60\1\61"+
		"\11\0\2\132\1\60\1\70\2\0\1\uffff\2\0\1\60\1\101\1\uffff\2\0\1\60\1\63"+
		"\11\0\2\60\1\65\1\103\1\107\1\uffff\2\0\1\60\1\67\1\60\1\63\1\60\1\105"+
		"\3\115\1\116\1\110\1\132\1\115\1\110\1\103\1\101\1\0\2\107\30\0\1\104"+
		"\1\115\1\104\1\115\1\60\1\104\1\70\3\0\1\60\1\64\1\63\3\0\1\60\1\104\2"+
		"\0\1\60\1\104\1\63\3\0\1\60\1\105\2\0\1\uffff\1\60\1\64\1\60\1\61\1\104"+
		"\1\115\2\0\1\60\1\104\4\0\1\60\1\70\1\132\1\60\1\101\2\0\1\60\1\63\1\0"+
		"\1\115\2\0\1\60\1\104\2\60\1\65\1\103\1\107\1\0\1\115\1\0\1\115\2\0\1"+
		"\60\1\67\1\0\1\64\1\63\1\60\1\105\3\115\1\116\1\110\1\132\1\115\1\110"+
		"\1\103\1\101\1\0\2\107\30\0\1\104\1\115\1\104\1\115\14\0\1\60\1\104\1"+
		"\70\3\0\1\60\1\64\1\63\3\0\1\60\1\104\2\0\1\60\1\104\1\63\3\0\1\60\1\105"+
		"\2\0\1\60\1\64\1\0\1\60\1\61\1\104\1\115\4\0\1\60\1\104\2\0\1\60\1\70"+
		"\1\132\2\0\1\60\1\101\2\0\1\60\1\63\1\0\1\115\2\0\1\60\1\104\2\0\1\64"+
		"\1\60\1\65\1\103\1\107\1\0\1\115\1\0\1\115\6\0\1\60\1\67\1\0\1\63\1\60"+
		"\1\105\3\115\1\116\1\110\1\132\1\115\1\110\1\103\1\101\1\0\2\107\30\0"+
		"\1\104\1\115\1\104\1\115\14\0\1\64\1\104\1\70\3\0\2\64\1\63\3\0\1\64\1"+
		"\104\2\0\1\64\1\104\1\63\3\0\1\64\1\105\2\0\1\60\1\64\1\0\1\64\1\61\1"+
		"\104\1\115\4\0\1\60\1\104\2\0\1\64\1\70\1\132\2\0\1\65\1\101\2\0\1\64"+
		"\1\63\1\0\1\115\2\0\1\60\1\104\2\0\1\60\1\65\1\103\1\107\1\0\1\115\1\0"+
		"\1\115\6\0\1\64\1\67\1\0\1\105\3\115\1\116\1\110\1\132\1\115\1\110\1\103"+
		"\1\101\1\0\2\107\30\0\1\104\1\115\1\104\1\115\14\0\1\104\1\70\3\0\1\64"+
		"\1\63\3\0\1\104\2\0\1\104\1\63\3\0\1\105\2\0\2\64\1\0\1\61\1\104\1\115"+
		"\4\0\1\64\1\104\2\0\1\70\1\132\2\0\1\101\2\0\1\63\1\0\1\115\2\0\1\64\1"+
		"\104\2\0\1\103\1\107\1\0\1\115\1\0\1\115\6\0\1\67\62\0\1\64\1\0\1\104"+
		"\1\115\4\0\1\104\2\0\1\132\5\0\1\115\2\0\1\104\30\0";
	static final String DFA193_maxS =
		"\1\uffff\1\160\1\uffff\2\170\1\155\1\163\1\156\1\145\1\0\1\150\1\172\1"+
		"\uffff\1\160\2\170\1\155\1\163\1\156\1\145\1\0\1\150\1\172\2\uffff\1\151"+
		"\1\uffff\1\147\1\151\1\147\1\170\1\67\1\144\1\170\1\163\1\63\1\163\2\156"+
		"\2\145\2\0\2\150\2\172\1\0\1\uffff\4\0\1\uffff\6\0\1\uffff\2\0\1\uffff"+
		"\4\0\1\uffff\1\0\1\144\1\uffff\1\155\1\144\1\155\1\uffff\1\172\1\uffff"+
		"\1\172\1\0\1\uffff\26\0\1\uffff\1\155\1\0\1\155\2\151\1\67\1\60\1\65\1"+
		"\0\1\uffff\13\0\1\67\1\144\1\63\1\160\1\170\1\155\1\163\1\156\1\150\1"+
		"\172\1\163\1\150\12\0\1\170\1\145\15\0\1\uffff\3\0\1\67\1\144\1\0\1\70"+
		"\1\uffff\3\0\1\67\1\70\1\0\1\63\1\uffff\2\0\1\66\1\144\1\uffff\3\0\1\67"+
		"\1\144\1\0\1\63\1\uffff\2\0\1\66\1\145\1\0\1\uffff\1\0\1\66\1\65\1\0\1"+
		"\uffff\7\0\2\172\1\66\1\70\2\0\1\uffff\2\0\1\67\1\141\1\uffff\2\0\1\66"+
		"\1\71\1\0\1\uffff\7\0\1\67\1\60\1\65\1\151\1\147\1\uffff\2\0\1\66\2\67"+
		"\1\144\1\63\1\160\1\170\1\155\1\163\1\156\1\150\1\172\1\163\1\150\1\170"+
		"\1\145\1\0\2\147\30\0\1\144\1\155\1\144\1\155\1\67\1\144\1\70\3\0\1\67"+
		"\1\70\1\63\3\0\1\66\1\144\2\0\1\67\1\144\1\63\3\0\1\66\1\145\2\0\1\uffff"+
		"\1\66\1\64\1\66\1\65\1\144\1\155\2\0\1\66\1\144\4\0\1\66\1\70\1\172\1"+
		"\67\1\141\2\0\1\66\1\71\1\0\1\155\2\0\1\66\1\144\1\67\1\60\1\65\1\151"+
		"\1\147\1\0\1\155\1\0\1\155\2\0\1\66\1\67\1\0\1\67\1\144\1\63\1\160\1\170"+
		"\1\155\1\163\1\156\1\150\1\172\1\163\1\150\1\170\1\145\1\0\2\147\30\0"+
		"\1\144\1\155\1\144\1\155\14\0\1\67\1\144\1\70\3\0\1\67\1\70\1\63\3\0\1"+
		"\66\1\144\2\0\1\67\1\144\1\63\3\0\1\66\1\145\2\0\1\66\1\64\1\0\1\66\1"+
		"\65\1\144\1\155\4\0\1\66\1\144\2\0\1\66\1\70\1\172\2\0\1\67\1\141\2\0"+
		"\1\66\1\71\1\0\1\155\2\0\1\66\1\144\2\0\1\67\1\60\1\65\1\151\1\147\1\0"+
		"\1\155\1\0\1\155\6\0\1\66\1\67\1\0\1\144\1\63\1\160\1\170\1\155\1\163"+
		"\1\156\1\150\1\172\1\163\1\150\1\170\1\145\1\0\2\147\30\0\1\144\1\155"+
		"\1\144\1\155\14\0\1\67\1\144\1\70\3\0\1\67\1\70\1\63\3\0\1\66\1\144\2"+
		"\0\1\67\1\144\1\63\3\0\1\66\1\145\2\0\1\66\1\64\1\0\1\66\1\65\1\144\1"+
		"\155\4\0\1\66\1\144\2\0\1\66\1\70\1\172\2\0\1\67\1\141\2\0\1\66\1\71\1"+
		"\0\1\155\2\0\1\66\1\144\2\0\1\60\1\65\1\151\1\147\1\0\1\155\1\0\1\155"+
		"\6\0\1\66\1\67\1\0\1\160\1\170\1\155\1\163\1\156\1\150\1\172\1\163\1\150"+
		"\1\170\1\145\1\0\2\147\30\0\1\144\1\155\1\144\1\155\14\0\1\144\1\70\3"+
		"\0\1\70\1\63\3\0\1\144\2\0\1\144\1\63\3\0\1\145\2\0\1\66\1\64\1\0\1\65"+
		"\1\144\1\155\4\0\1\66\1\144\2\0\1\70\1\172\2\0\1\141\2\0\1\71\1\0\1\155"+
		"\2\0\1\66\1\144\2\0\1\151\1\147\1\0\1\155\1\0\1\155\6\0\1\67\62\0\1\64"+
		"\1\0\1\144\1\155\4\0\1\144\2\0\1\172\5\0\1\155\2\0\1\144\30\0";
	static final String DFA193_acceptS =
		"\14\uffff\1\13\12\uffff\1\14\1\15\62\uffff\1\11\126\uffff\1\2\7\uffff"+
		"\1\3\7\uffff\1\4\4\uffff\1\5\7\uffff\1\6\30\uffff\1\12\4\uffff\1\1\22"+
		"\uffff\1\7\113\uffff\1\10\u020c\uffff";
	static final String DFA193_specialS =
		"\2\uffff\1\u012d\6\uffff\1\u0176\12\uffff\1\u0177\5\uffff\1\71\16\uffff"+
		"\1\u0183\1\u0184\4\uffff\1\u01b6\1\u01ec\1\u00b1\1\u01be\1\u00b8\1\u00c6"+
		"\1\u01ee\1\110\1\u010e\1\u00d0\1\120\1\u0113\1\u012e\1\u017d\1\u0136\1"+
		"\u01cf\1\1\1\133\1\u01de\1\150\1\u0164\1\u01f7\1\u016d\1\uffff\1\43\5"+
		"\uffff\1\u01a1\1\uffff\1\u0085\1\u00c1\1\u0095\1\u01b7\1\u00b2\1\u01bf"+
		"\1\u00b9\1\u00c7\1\111\1\u010f\1\u00d1\1\121\1\u0114\1\u012f\1\u0137\1"+
		"\u01d0\1\136\1\u01df\1\147\1\u0165\1\u016e\1\u0084\1\u0096\1\u014e\1\u0127"+
		"\1\uffff\1\u0158\6\uffff\1\u00e8\1\u012a\1\u00f6\1\u014f\1\u0159\1\u00e7"+
		"\1\u00f7\1\u00c9\1\u00d2\1\107\1\122\1\u0110\1\u0115\14\uffff\1\u00ca"+
		"\1\u00d3\1\106\1\123\1\u0111\1\u0116\1\u01d2\1\u01dd\1\132\1\151\2\uffff"+
		"\1\u0185\1\u01d4\1\u01e0\1\131\1\153\1\u0166\1\u016c\1\u0167\1\u016f\1"+
		"\u0086\1\u0097\1\u0089\1\u009a\1\uffff\1\u0186\1\u0187\1\u00e3\2\uffff"+
		"\1\u00e4\2\uffff\1\u0107\1\u0108\1\u0080\2\uffff\1\u0081\2\uffff\1\u0124"+
		"\1\u0125\3\uffff\1\u01c0\1\u01c6\1\176\2\uffff\1\177\2\uffff\1\u014c\1"+
		"\u0156\2\uffff\1\u0139\1\u00e2\1\u0143\2\uffff\1\4\1\u0182\1\23\1\u013a"+
		"\1\u0144\1\5\1\24\1\u008a\1\u009b\4\uffff\1\u0083\1\u009e\1\uffff\1\u00e5"+
		"\1\u00e6\3\uffff\1\u0119\1\u011a\2\uffff\1\u0193\1\u017a\1\u01a3\1\u0194"+
		"\1\u01a4\1\u0150\1\u015a\1\u014d\1\u015b\6\uffff\1\u00a9\1\u00aa\20\uffff"+
		"\1\0\2\uffff\1\u01b8\1\u00b3\1\u01c1\1\u00ba\1\u0130\1\u013b\1\u01ce\1"+
		"\141\1\u01e1\1\154\1\u0168\1\u0170\1\u008e\1\u009f\1\u01d5\1\142\1\u01e2"+
		"\1\160\1\u00c5\1\112\1\u01ca\1\u00d4\1\124\1\u01cb\7\uffff\1\u0148\1\u0149"+
		"\1\u00ce\3\uffff\1\u00db\1\67\1\u018f\2\uffff\1\u0105\1\u0106\3\uffff"+
		"\1\u0178\1\u0179\1\64\2\uffff\1\u011f\1\u0121\7\uffff\1\u01ed\1\u01ef"+
		"\2\uffff\1\u0090\1\u00a2\1\u0092\1\u00a5\5\uffff\1\u00d8\1\u00d9\2\uffff"+
		"\1\u00e0\1\uffff\1\u0180\1\u0181\7\uffff\1\u0151\1\uffff\1\u0157\1\uffff"+
		"\1\u00ea\1\u00f9\2\uffff\1\114\16\uffff\1\104\2\uffff\1\u01b9\1\u00b4"+
		"\1\u01c2\1\u00bb\1\u0131\1\u013f\1\u01d6\1\144\1\u01e4\1\161\1\u0169\1"+
		"\u0171\1\u008f\1\u00a6\1\u01d8\1\145\1\u01e6\1\162\1\u00cc\1\115\1\62"+
		"\1\u00cf\1\125\1\63\4\uffff\1\u00eb\1\u00fc\1\u00ef\1\u00fd\1\172\1\174"+
		"\1\6\1\25\1\173\1\175\1\7\1\27\3\uffff\1\u01b5\1\u01ba\1\u010b\3\uffff"+
		"\1\u011d\1\u00bf\1\44\2\uffff\1\u0132\1\u0140\3\uffff\1\u01dc\1\u01eb"+
		"\1\u00ab\2\uffff\1\u017b\1\u017c\2\uffff\1\u01f6\4\uffff\1\73\1\75\1\3"+
		"\1\22\2\uffff\1\u018c\1\u018d\3\uffff\1\u0087\1\u009d\2\uffff\1\u010c"+
		"\1\u010d\2\uffff\1\u0126\1\uffff\1\u0197\1\u01a8\2\uffff\1\u012b\1\u012c"+
		"\5\uffff\1\u0152\1\uffff\1\u015c\1\uffff\1\u00f1\1\u00fe\1\u0192\1\u01a2"+
		"\1\u0198\1\u01aa\2\uffff\1\u00dc\15\uffff\1\2\2\uffff\1\u01bb\1\u00b5"+
		"\1\u01c3\1\u00bc\1\u0133\1\u0141\1\u01da\1\134\1\u01e9\1\164\1\u0163\1"+
		"\u0172\1\u0093\1\u0099\1\u01d7\1\137\1\u01e7\1\163\1\u00cd\1\116\1\u01cc"+
		"\1\u00d5\1\126\1\u01cd\4\uffff\1\u00f2\1\u0102\1\u00f3\1\u0103\1\54\1"+
		"\56\1\15\1\30\1\55\1\57\1\16\1\34\3\uffff\1\60\1\61\1\u0160\3\uffff\1"+
		"\u0175\1\u010a\1\167\2\uffff\1\u01b1\1\u01b2\3\uffff\1\70\1\72\1\u0109"+
		"\2\uffff\1\u01f0\1\u01f2\2\uffff\1\77\4\uffff\1\u00c3\1\u00c4\1\17\1\36"+
		"\2\uffff\1\41\1\42\3\uffff\1\u0088\1\u00a4\2\uffff\1\u0161\1\u0162\2\uffff"+
		"\1\u0188\1\uffff\1\u0199\1\u01ab\2\uffff\1\u0190\1\u0191\4\uffff\1\u0153"+
		"\1\uffff\1\u015d\1\uffff\1\u00f4\1\u0104\1\u019a\1\u01ad\1\u019e\1\u01ae"+
		"\2\uffff\1\u011e\13\uffff\1\u0082\2\uffff\1\u01bc\1\u00b6\1\u01c4\1\u00bd"+
		"\1\u0134\1\u0142\1\u01d3\1\143\1\u01e8\1\157\1\u016a\1\u0173\1\u0094\1"+
		"\u009c\1\u01db\1\140\1\u01e5\1\152\1\u00cb\1\117\1\102\1\u00d6\1\127\1"+
		"\103\4\uffff\1\u00ed\1\u00fb\1\u00e9\1\u0101\1\u00ad\1\u00af\1\20\1\26"+
		"\1\u00ae\1\u00b0\1\12\1\37\2\uffff\1\u01c7\1\u01c8\1\u0118\2\uffff\1\u0120"+
		"\1\u00c2\1\53\1\uffff\1\u014a\1\u014b\2\uffff\1\u01f4\1\u01f5\1\u00c0"+
		"\1\uffff\1\u017e\1\u017f\2\uffff\1\u00da\3\uffff\1\100\1\101\1\13\1\40"+
		"\2\uffff\1\165\1\166\2\uffff\1\u008c\1\u00a0\1\uffff\1\u011b\1\u011c\1"+
		"\uffff\1\u0128\1\uffff\1\u01a0\1\u01af\2\uffff\1\50\1\52\2\uffff\1\u0154"+
		"\1\uffff\1\u015e\1\uffff\1\u00ee\1\u00fa\1\u019b\1\u01ac\1\u019d\1\u01b0"+
		"\1\uffff\1\u00dd\1\u01bd\1\u00b7\1\u01c5\1\u00be\1\u0135\1\u0138\1\u01d9"+
		"\1\135\1\u01ea\1\155\1\u016b\1\u0174\1\u008d\1\u00a1\1\u01d1\1\146\1\u01e3"+
		"\1\156\1\u00c8\1\113\1\u0112\1\u00d7\1\130\1\u0117\1\u00f0\1\u00ff\1\u00f5"+
		"\1\u0100\1\u013c\1\u0145\1\14\1\33\1\u013e\1\u0146\1\21\1\35\1\74\1\76"+
		"\1\u0189\1\u018e\1\u0123\1\u00ac\1\u01f1\1\u01f3\1\170\1\171\1\u0122\1"+
		"\47\1\51\1\uffff\1\105\2\uffff\1\u00de\1\u00df\1\11\1\31\1\uffff\1\45"+
		"\1\46\1\uffff\1\u008b\1\u0098\1\u018a\1\u018b\1\u01c9\1\uffff\1\u0195"+
		"\1\u01a9\1\uffff\1\u01b3\1\u01b4\1\u0155\1\u015f\1\u00ec\1\u00f8\1\u0196"+
		"\1\u01a7\1\u019c\1\u01a5\1\u0129\1\u00e1\1\u013d\1\u0147\1\10\1\32\1\u00a7"+
		"\1\u00a8\1\u0091\1\u00a3\1\u019f\1\u01a6\1\65\1\66}>";
	static final String[] DFA193_transitionS = {
			"\1\27\7\uffff\1\14\23\uffff\2\14\1\20\1\15\1\16\2\14\1\26\1\22\1\14\1"+
			"\25\1\14\1\21\2\14\1\17\1\14\1\23\1\24\7\14\1\uffff\1\2\2\uffff\1\14"+
			"\1\uffff\2\14\1\5\1\1\1\3\2\14\1\13\1\7\1\14\1\12\1\14\1\6\2\14\1\4\1"+
			"\14\1\10\1\11\7\14\5\uffff\uff80\14",
			"\1\35\12\uffff\1\34\13\uffff\1\32\10\uffff\1\33\12\uffff\1\31",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\37\3\14\1\40\1\43\1\40\1\43\20\14"+
			"\1\56\1\46\1\14\1\54\1\14\1\44\2\14\1\41\1\14\1\50\1\52\24\14\1\55\1"+
			"\45\1\14\1\53\1\14\1\42\2\14\1\36\1\14\1\47\1\51\uff8c\14",
			"\1\62\12\uffff\1\63\3\uffff\1\60\20\uffff\1\57\12\uffff\1\61",
			"\1\72\20\uffff\1\71\3\uffff\1\70\3\uffff\1\65\6\uffff\1\67\20\uffff"+
			"\1\66\3\uffff\1\64",
			"\1\75\16\uffff\1\74\20\uffff\1\73",
			"\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1\100",
			"\1\105\15\uffff\1\104\21\uffff\1\103",
			"\1\111\3\uffff\1\112\26\uffff\1\107\4\uffff\1\106\3\uffff\1\110",
			"\1\uffff",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\121\1\uffff\1\120\35\uffff\1\117",
			"",
			"\1\35\12\uffff\1\34\13\uffff\1\32\10\uffff\1\33\12\uffff\1\31",
			"\1\124\12\uffff\1\125\3\uffff\1\60\20\uffff\1\122\12\uffff\1\123",
			"\1\133\20\uffff\1\132\3\uffff\1\131\3\uffff\1\65\6\uffff\1\130\20\uffff"+
			"\1\127\3\uffff\1\126",
			"\1\135\16\uffff\1\74\20\uffff\1\134",
			"\1\140\5\uffff\1\141\10\uffff\1\77\20\uffff\1\136\5\uffff\1\137",
			"\1\143\15\uffff\1\104\21\uffff\1\142",
			"\1\111\3\uffff\1\112\26\uffff\1\107\4\uffff\1\106\3\uffff\1\110",
			"\1\uffff",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\145\1\uffff\1\120\35\uffff\1\144",
			"",
			"",
			"\1\152\5\uffff\1\151\22\uffff\1\147\6\uffff\1\150\5\uffff\1\146",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\155\3\14\1\157\1\156\1\157\1\156"+
			"\30\14\1\154\37\14\1\153\uff8f\14",
			"\1\162\24\uffff\1\161\12\uffff\1\160",
			"\1\152\5\uffff\1\164\22\uffff\1\147\6\uffff\1\150\5\uffff\1\163",
			"\1\166\24\uffff\1\161\12\uffff\1\165",
			"\1\174\20\uffff\1\172\3\uffff\1\170\3\uffff\1\65\6\uffff\1\173\20\uffff"+
			"\1\171\3\uffff\1\167",
			"\1\175\3\uffff\1\176\1\177\1\176\1\177",
			"\1\u0082\1\u0080\1\u0081\2\uffff\1\u0086\1\u0084\10\uffff\1\u0088\1"+
			"\uffff\1\u0087\35\uffff\1\u0085\1\uffff\1\u0083",
			"\1\u008e\20\uffff\1\u008c\3\uffff\1\u008a\3\uffff\1\65\6\uffff\1\u008d"+
			"\20\uffff\1\u008b\3\uffff\1\u0089",
			"\1\u0090\5\uffff\1\u0092\10\uffff\1\77\20\uffff\1\u008f\5\uffff\1\u0091",
			"\1\u0093\1\uffff\1\u0094\1\u0095",
			"\1\u0097\5\uffff\1\u0099\10\uffff\1\77\20\uffff\1\u0096\5\uffff\1\u0098",
			"\1\u009b\15\uffff\1\104\21\uffff\1\u009a",
			"\1\u009d\15\uffff\1\104\21\uffff\1\u009c",
			"\1\111\3\uffff\1\112\26\uffff\1\107\4\uffff\1\106\3\uffff\1\110",
			"\1\111\3\uffff\1\112\26\uffff\1\107\4\uffff\1\106\3\uffff\1\110",
			"\1\uffff",
			"\1\uffff",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\u009f\1\uffff\1\120\35\uffff\1\u009e",
			"\1\u00a1\1\uffff\1\120\35\uffff\1\u00a0",
			"\1\uffff",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u00a6\3\14\1\u00a7\1\u00a9\1\u00a7"+
			"\1\u00a9\25\14\1\u00a4\12\14\1\u00a8\24\14\1\u00a3\12\14\1\u00a5\uff87"+
			"\14",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u00ae\3\14\1\u00b1\1\u00af\1\u00b1"+
			"\1\u00af\34\14\1\u00b0\3\14\1\u00ac\33\14\1\u00ad\3\14\1\u00ab\uff87"+
			"\14",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u00b5\3\14\1\u00b6\1\14\1\u00b6"+
			"\26\14\1\u00b4\37\14\1\u00b3\uff92\14",
			"\1\uffff",
			"\1\uffff",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u00bb\3\14\1\u00bc\1\u00be\1\u00bc"+
			"\1\u00be\25\14\1\u00b9\5\14\1\u00bd\31\14\1\u00b8\5\14\1\u00ba\uff8c"+
			"\14",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u00c2\3\14\1\u00c3\1\14\1\u00c3"+
			"\27\14\1\u00c1\37\14\1\u00c0\uff91\14",
			"\1\uffff",
			"\1\u00c6\27\uffff\1\u00c5\7\uffff\1\u00c4",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u00c7\3\14\1\u00c8\1\14\1\u00c8"+
			"\uffc9\14",
			"\1\u00cb\16\uffff\1\u00ca\20\uffff\1\u00c9",
			"\1\u00cd\27\uffff\1\u00c5\7\uffff\1\u00cc",
			"\1\u00cf\16\uffff\1\u00ca\20\uffff\1\u00ce",
			"",
			"\1\u00d1\1\uffff\1\120\35\uffff\1\u00d0",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u00d4\3\14\1\u00d5\1\14\1\u00d5"+
			"\21\14\1\u00d3\37\14\1\u00d2\uff97\14",
			"\1\u00d7\1\uffff\1\120\35\uffff\1\u00d6",
			"\1\uffff",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u00db\4\14\1\u00dc\1\14\1\u00dc"+
			"\42\14\1\u00da\37\14\1\u00d9\uff85\14",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u00e0\3\14\1\u00e1\1\14\1\u00e1"+
			"\22\14\1\u00df\37\14\1\u00de\uff96\14",
			"\1\u00e4\16\uffff\1\u00e3\20\uffff\1\u00e2",
			"\1\uffff",
			"\1\u00e6\16\uffff\1\u00e3\20\uffff\1\u00e5",
			"\1\152\5\uffff\1\u00e8\22\uffff\1\147\6\uffff\1\150\5\uffff\1\u00e7",
			"\1\152\5\uffff\1\u00ea\22\uffff\1\147\6\uffff\1\150\5\uffff\1\u00e9",
			"\1\u00eb\3\uffff\1\u00ed\1\u00ec\1\u00ed\1\u00ec",
			"\1\u00ee",
			"\1\u00ef",
			"\1\uffff",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u00f3\3\14\1\u00f4\1\14\1\u00f4"+
			"\20\14\1\u00f2\37\14\1\u00f1\uff98\14",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u00f5\3\uffff\1\u00f6\1\u00f7\1\u00f6\1\u00f7",
			"\1\u00fa\1\u00f8\1\u00f9\2\uffff\1\u00fe\1\u00fc\10\uffff\1\u0100\1"+
			"\uffff\1\u00ff\35\uffff\1\u00fd\1\uffff\1\u00fb",
			"\1\u0101\1\uffff\1\u0102\1\u0103",
			"\1\u0105\12\uffff\1\34\13\uffff\1\32\10\uffff\1\u0104\12\uffff\1\31",
			"\1\u0108\12\uffff\1\u0109\3\uffff\1\60\20\uffff\1\u0106\12\uffff\1\u0107",
			"\1\u010b\16\uffff\1\74\20\uffff\1\u010a",
			"\1\u010e\5\uffff\1\u010f\10\uffff\1\77\20\uffff\1\u010c\5\uffff\1\u010d",
			"\1\u0111\15\uffff\1\104\21\uffff\1\u0110",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\u0113\1\uffff\1\120\35\uffff\1\u0112",
			"\1\u0116\5\uffff\1\u0117\10\uffff\1\77\20\uffff\1\u0114\5\uffff\1\u0115",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u011d\20\uffff\1\u011c\3\uffff\1\u011b\3\uffff\1\65\6\uffff\1\u011a"+
			"\20\uffff\1\u0119\3\uffff\1\u0118",
			"\1\u0120\3\uffff\1\u0121\26\uffff\1\107\4\uffff\1\u011e\3\uffff\1\u011f",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0122\3\uffff\1\u0123\1\u0124\1\u0123\1\u0124",
			"\1\u0126\37\uffff\1\u0125",
			"\1\uffff",
			"\1\u0127",
			"",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0128\3\uffff\1\u012a\1\u0129\1\u012a\1\u0129",
			"\1\u012c\3\uffff\1\u012b",
			"\1\uffff",
			"\1\u012d",
			"",
			"\1\uffff",
			"\1\uffff",
			"\1\u012e\3\uffff\1\u012f\1\uffff\1\u012f",
			"\1\u0131\37\uffff\1\u0130",
			"",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0132\3\uffff\1\u0133\1\u0134\1\u0133\1\u0134",
			"\1\u0136\37\uffff\1\u0135",
			"\1\uffff",
			"\1\u0137",
			"",
			"\1\uffff",
			"\1\uffff",
			"\1\u0138\3\uffff\1\u0139\1\uffff\1\u0139",
			"\1\u013b\37\uffff\1\u013a",
			"\1\uffff",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u013d\3\14\1\u013e\1\14\1\u013e"+
			"\uffc9\14",
			"\1\uffff",
			"\1\u013f\3\uffff\1\u0140\1\uffff\1\u0140",
			"\1\u0141\3\uffff\1\u0142",
			"\1\uffff",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u0145\3\14\1\u0146\1\14\1\u0146"+
			"\26\14\1\u0144\37\14\1\u0143\uff92\14",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0148\1\uffff\1\120\35\uffff\1\u0147",
			"\1\u014a\1\uffff\1\120\35\uffff\1\u0149",
			"\1\u014b\3\uffff\1\u014c\1\uffff\1\u014c",
			"\1\u014d",
			"\1\uffff",
			"\1\uffff",
			"",
			"\1\uffff",
			"\1\uffff",
			"\1\u014e\4\uffff\1\u014f\1\uffff\1\u014f",
			"\1\u0151\37\uffff\1\u0150",
			"",
			"\1\uffff",
			"\1\uffff",
			"\1\u0152\3\uffff\1\u0153\1\uffff\1\u0153",
			"\1\u0155\5\uffff\1\u0154",
			"\1\uffff",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u0158\3\14\1\u0159\1\14\1\u0159"+
			"\26\14\1\u0157\37\14\1\u0156\uff92\14",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u015a\3\uffff\1\u015c\1\u015b\1\u015c\1\u015b",
			"\1\u015d",
			"\1\u015e",
			"\1\u0162\5\uffff\1\u0161\22\uffff\1\147\6\uffff\1\u0160\5\uffff\1\u015f",
			"\1\u0164\24\uffff\1\161\12\uffff\1\u0163",
			"",
			"\1\uffff",
			"\1\uffff",
			"\1\u0165\3\uffff\1\u0166\1\uffff\1\u0166",
			"\1\u0167",
			"\1\u0168\3\uffff\1\u0169\1\u016a\1\u0169\1\u016a",
			"\1\u016d\1\u016b\1\u016c\2\uffff\1\u0171\1\u016f\10\uffff\1\u0173\1"+
			"\uffff\1\u0172\35\uffff\1\u0170\1\uffff\1\u016e",
			"\1\u0174\1\uffff\1\u0175\1\u0176",
			"\1\u0178\12\uffff\1\34\13\uffff\1\32\10\uffff\1\u0177\12\uffff\1\31",
			"\1\u017b\12\uffff\1\u017c\3\uffff\1\60\20\uffff\1\u0179\12\uffff\1\u017a",
			"\1\u017e\16\uffff\1\74\20\uffff\1\u017d",
			"\1\u0181\5\uffff\1\u0182\10\uffff\1\77\20\uffff\1\u017f\5\uffff\1\u0180",
			"\1\u0184\15\uffff\1\104\21\uffff\1\u0183",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\u0186\1\uffff\1\120\35\uffff\1\u0185",
			"\1\u0189\5\uffff\1\u018a\10\uffff\1\77\20\uffff\1\u0187\5\uffff\1\u0188",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\u0190\20\uffff\1\u018f\3\uffff\1\u018e\3\uffff\1\65\6\uffff\1\u018d"+
			"\20\uffff\1\u018c\3\uffff\1\u018b",
			"\1\u0193\3\uffff\1\u0194\26\uffff\1\107\4\uffff\1\u0191\3\uffff\1\u0192",
			"\1\uffff",
			"\1\u0196\24\uffff\1\161\12\uffff\1\u0195",
			"\1\u0198\24\uffff\1\161\12\uffff\1\u0197",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u019a\27\uffff\1\u00c5\7\uffff\1\u0199",
			"\1\u019c\16\uffff\1\u00ca\20\uffff\1\u019b",
			"\1\u019e\27\uffff\1\u00c5\7\uffff\1\u019d",
			"\1\u01a0\16\uffff\1\u00ca\20\uffff\1\u019f",
			"\1\u01a1\3\uffff\1\u01a2\1\u01a3\1\u01a2\1\u01a3",
			"\1\u01a5\37\uffff\1\u01a4",
			"\1\u01a6",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u01a7\3\uffff\1\u01a9\1\u01a8\1\u01a9\1\u01a8",
			"\1\u01ab\3\uffff\1\u01aa",
			"\1\u01ac",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u01ad\3\uffff\1\u01ae\1\uffff\1\u01ae",
			"\1\u01b0\37\uffff\1\u01af",
			"\1\uffff",
			"\1\uffff",
			"\1\u01b1\3\uffff\1\u01b2\1\u01b3\1\u01b2\1\u01b3",
			"\1\u01b5\37\uffff\1\u01b4",
			"\1\u01b6",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u01b7\3\uffff\1\u01b8\1\uffff\1\u01b8",
			"\1\u01ba\37\uffff\1\u01b9",
			"\1\uffff",
			"\1\uffff",
			"",
			"\1\u01bb\3\uffff\1\u01bc\1\uffff\1\u01bc",
			"\1\u01bd",
			"\1\u01be\3\uffff\1\u01bf\1\uffff\1\u01bf",
			"\1\u01c0\3\uffff\1\u01c1",
			"\1\u01c3\27\uffff\1\u00c5\7\uffff\1\u01c2",
			"\1\u01c5\16\uffff\1\u00ca\20\uffff\1\u01c4",
			"\1\uffff",
			"\1\uffff",
			"\1\u01c6\3\uffff\1\u01c7\1\uffff\1\u01c7",
			"\1\u01c9\37\uffff\1\u01c8",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u01ca\3\uffff\1\u01cb\1\uffff\1\u01cb",
			"\1\u01cc",
			"\1\u01ce\1\uffff\1\120\35\uffff\1\u01cd",
			"\1\u01cf\4\uffff\1\u01d0\1\uffff\1\u01d0",
			"\1\u01d2\37\uffff\1\u01d1",
			"\1\uffff",
			"\1\uffff",
			"\1\u01d3\3\uffff\1\u01d4\1\uffff\1\u01d4",
			"\1\u01d6\5\uffff\1\u01d5",
			"\1\uffff",
			"\1\u01d8\16\uffff\1\u00e3\20\uffff\1\u01d7",
			"\1\uffff",
			"\1\uffff",
			"\1\u01d9\3\uffff\1\u01da\1\uffff\1\u01da",
			"\1\u01dc\37\uffff\1\u01db",
			"\1\u01dd\3\uffff\1\u01df\1\u01de\1\u01df\1\u01de",
			"\1\u01e0",
			"\1\u01e1",
			"\1\u01e5\5\uffff\1\u01e4\22\uffff\1\147\6\uffff\1\u01e3\5\uffff\1\u01e2",
			"\1\u01e7\24\uffff\1\161\12\uffff\1\u01e6",
			"\1\uffff",
			"\1\u01e9\16\uffff\1\u00e3\20\uffff\1\u01e8",
			"\1\uffff",
			"\1\u01eb\16\uffff\1\u00e3\20\uffff\1\u01ea",
			"\1\uffff",
			"\1\uffff",
			"\1\u01ec\3\uffff\1\u01ed\1\uffff\1\u01ed",
			"\1\u01ee",
			"\1\uffff",
			"\1\u01ef\1\u01f0\1\u01ef\1\u01f0",
			"\1\u01f3\1\u01f1\1\u01f2\2\uffff\1\u01f7\1\u01f5\10\uffff\1\u01f9\1"+
			"\uffff\1\u01f8\35\uffff\1\u01f6\1\uffff\1\u01f4",
			"\1\u01fa\1\uffff\1\u01fb\1\u01fc",
			"\1\u01fe\12\uffff\1\34\13\uffff\1\32\10\uffff\1\u01fd\12\uffff\1\31",
			"\1\u0201\12\uffff\1\u0202\3\uffff\1\60\20\uffff\1\u01ff\12\uffff\1\u0200",
			"\1\u0204\16\uffff\1\74\20\uffff\1\u0203",
			"\1\u0207\5\uffff\1\u0208\10\uffff\1\77\20\uffff\1\u0205\5\uffff\1\u0206",
			"\1\u020a\15\uffff\1\104\21\uffff\1\u0209",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\u020c\1\uffff\1\120\35\uffff\1\u020b",
			"\1\u020f\5\uffff\1\u0210\10\uffff\1\77\20\uffff\1\u020d\5\uffff\1\u020e",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\u0216\20\uffff\1\u0215\3\uffff\1\u0214\3\uffff\1\65\6\uffff\1\u0213"+
			"\20\uffff\1\u0212\3\uffff\1\u0211",
			"\1\u0219\3\uffff\1\u021a\26\uffff\1\107\4\uffff\1\u0217\3\uffff\1\u0218",
			"\1\uffff",
			"\1\u021c\24\uffff\1\161\12\uffff\1\u021b",
			"\1\u021e\24\uffff\1\161\12\uffff\1\u021d",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0220\27\uffff\1\u00c5\7\uffff\1\u021f",
			"\1\u0222\16\uffff\1\u00ca\20\uffff\1\u0221",
			"\1\u0224\27\uffff\1\u00c5\7\uffff\1\u0223",
			"\1\u0226\16\uffff\1\u00ca\20\uffff\1\u0225",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0227\3\uffff\1\u0228\1\u0229\1\u0228\1\u0229",
			"\1\u022b\37\uffff\1\u022a",
			"\1\u022c",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u022d\3\uffff\1\u022f\1\u022e\1\u022f\1\u022e",
			"\1\u0231\3\uffff\1\u0230",
			"\1\u0232",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0233\3\uffff\1\u0234\1\uffff\1\u0234",
			"\1\u0236\37\uffff\1\u0235",
			"\1\uffff",
			"\1\uffff",
			"\1\u0237\3\uffff\1\u0238\1\u0239\1\u0238\1\u0239",
			"\1\u023b\37\uffff\1\u023a",
			"\1\u023c",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u023d\3\uffff\1\u023e\1\uffff\1\u023e",
			"\1\u0240\37\uffff\1\u023f",
			"\1\uffff",
			"\1\uffff",
			"\1\u0241\3\uffff\1\u0242\1\uffff\1\u0242",
			"\1\u0243",
			"\1\uffff",
			"\1\u0244\3\uffff\1\u0245\1\uffff\1\u0245",
			"\1\u0246\3\uffff\1\u0247",
			"\1\u0249\27\uffff\1\u00c5\7\uffff\1\u0248",
			"\1\u024b\16\uffff\1\u00ca\20\uffff\1\u024a",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u024c\3\uffff\1\u024d\1\uffff\1\u024d",
			"\1\u024f\37\uffff\1\u024e",
			"\1\uffff",
			"\1\uffff",
			"\1\u0250\3\uffff\1\u0251\1\uffff\1\u0251",
			"\1\u0252",
			"\1\u0254\1\uffff\1\120\35\uffff\1\u0253",
			"\1\uffff",
			"\1\uffff",
			"\1\u0255\4\uffff\1\u0256\1\uffff\1\u0256",
			"\1\u0258\37\uffff\1\u0257",
			"\1\uffff",
			"\1\uffff",
			"\1\u0259\3\uffff\1\u025a\1\uffff\1\u025a",
			"\1\u025c\5\uffff\1\u025b",
			"\1\uffff",
			"\1\u025e\16\uffff\1\u00e3\20\uffff\1\u025d",
			"\1\uffff",
			"\1\uffff",
			"\1\u025f\3\uffff\1\u0260\1\uffff\1\u0260",
			"\1\u0262\37\uffff\1\u0261",
			"\1\uffff",
			"\1\uffff",
			"\1\u0264\1\u0263\1\u0264\1\u0263",
			"\1\u0265",
			"\1\u0266",
			"\1\u026a\5\uffff\1\u0269\22\uffff\1\147\6\uffff\1\u0268\5\uffff\1\u0267",
			"\1\u026c\24\uffff\1\161\12\uffff\1\u026b",
			"\1\uffff",
			"\1\u026e\16\uffff\1\u00e3\20\uffff\1\u026d",
			"\1\uffff",
			"\1\u0270\16\uffff\1\u00e3\20\uffff\1\u026f",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0271\3\uffff\1\u0272\1\uffff\1\u0272",
			"\1\u0273",
			"\1\uffff",
			"\1\u0276\1\u0274\1\u0275\2\uffff\1\u027a\1\u0278\10\uffff\1\u027c\1"+
			"\uffff\1\u027b\35\uffff\1\u0279\1\uffff\1\u0277",
			"\1\u027d\1\uffff\1\u027e\1\u027f",
			"\1\u0281\12\uffff\1\34\13\uffff\1\32\10\uffff\1\u0280\12\uffff\1\31",
			"\1\u0284\12\uffff\1\u0285\3\uffff\1\60\20\uffff\1\u0282\12\uffff\1\u0283",
			"\1\u0287\16\uffff\1\74\20\uffff\1\u0286",
			"\1\u028a\5\uffff\1\u028b\10\uffff\1\77\20\uffff\1\u0288\5\uffff\1\u0289",
			"\1\u028d\15\uffff\1\104\21\uffff\1\u028c",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\u028f\1\uffff\1\120\35\uffff\1\u028e",
			"\1\u0292\5\uffff\1\u0293\10\uffff\1\77\20\uffff\1\u0290\5\uffff\1\u0291",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\u0299\20\uffff\1\u0298\3\uffff\1\u0297\3\uffff\1\65\6\uffff\1\u0296"+
			"\20\uffff\1\u0295\3\uffff\1\u0294",
			"\1\u029c\3\uffff\1\u029d\26\uffff\1\107\4\uffff\1\u029a\3\uffff\1\u029b",
			"\1\uffff",
			"\1\u029f\24\uffff\1\161\12\uffff\1\u029e",
			"\1\u02a1\24\uffff\1\161\12\uffff\1\u02a0",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u02a3\27\uffff\1\u00c5\7\uffff\1\u02a2",
			"\1\u02a5\16\uffff\1\u00ca\20\uffff\1\u02a4",
			"\1\u02a7\27\uffff\1\u00c5\7\uffff\1\u02a6",
			"\1\u02a9\16\uffff\1\u00ca\20\uffff\1\u02a8",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u02aa\1\u02ab\1\u02aa\1\u02ab",
			"\1\u02ad\37\uffff\1\u02ac",
			"\1\u02ae",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u02b0\1\u02af\1\u02b0\1\u02af",
			"\1\u02b2\3\uffff\1\u02b1",
			"\1\u02b3",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u02b4\1\uffff\1\u02b4",
			"\1\u02b6\37\uffff\1\u02b5",
			"\1\uffff",
			"\1\uffff",
			"\1\u02b7\1\u02b8\1\u02b7\1\u02b8",
			"\1\u02ba\37\uffff\1\u02b9",
			"\1\u02bb",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u02bc\1\uffff\1\u02bc",
			"\1\u02be\37\uffff\1\u02bd",
			"\1\uffff",
			"\1\uffff",
			"\1\u02bf\3\uffff\1\u02c0\1\uffff\1\u02c0",
			"\1\u02c1",
			"\1\uffff",
			"\1\u02c2\1\uffff\1\u02c2",
			"\1\u02c3\3\uffff\1\u02c4",
			"\1\u02c6\27\uffff\1\u00c5\7\uffff\1\u02c5",
			"\1\u02c8\16\uffff\1\u00ca\20\uffff\1\u02c7",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u02c9\3\uffff\1\u02ca\1\uffff\1\u02ca",
			"\1\u02cc\37\uffff\1\u02cb",
			"\1\uffff",
			"\1\uffff",
			"\1\u02cd\1\uffff\1\u02cd",
			"\1\u02ce",
			"\1\u02d0\1\uffff\1\120\35\uffff\1\u02cf",
			"\1\uffff",
			"\1\uffff",
			"\1\u02d1\1\uffff\1\u02d1",
			"\1\u02d3\37\uffff\1\u02d2",
			"\1\uffff",
			"\1\uffff",
			"\1\u02d4\1\uffff\1\u02d4",
			"\1\u02d6\5\uffff\1\u02d5",
			"\1\uffff",
			"\1\u02d8\16\uffff\1\u00e3\20\uffff\1\u02d7",
			"\1\uffff",
			"\1\uffff",
			"\1\u02d9\3\uffff\1\u02da\1\uffff\1\u02da",
			"\1\u02dc\37\uffff\1\u02db",
			"\1\uffff",
			"\1\uffff",
			"\1\u02dd",
			"\1\u02de",
			"\1\u02e2\5\uffff\1\u02e1\22\uffff\1\147\6\uffff\1\u02e0\5\uffff\1\u02df",
			"\1\u02e4\24\uffff\1\161\12\uffff\1\u02e3",
			"\1\uffff",
			"\1\u02e6\16\uffff\1\u00e3\20\uffff\1\u02e5",
			"\1\uffff",
			"\1\u02e8\16\uffff\1\u00e3\20\uffff\1\u02e7",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u02e9\1\uffff\1\u02e9",
			"\1\u02ea",
			"\1\uffff",
			"\1\35\12\uffff\1\34\13\uffff\1\32\10\uffff\1\33\12\uffff\1\31",
			"\1\u02ed\12\uffff\1\u02ee\3\uffff\1\60\20\uffff\1\u02eb\12\uffff\1\u02ec",
			"\1\u02f0\16\uffff\1\74\20\uffff\1\u02ef",
			"\1\u02f3\5\uffff\1\u02f4\10\uffff\1\77\20\uffff\1\u02f1\5\uffff\1\u02f2",
			"\1\u02f6\15\uffff\1\104\21\uffff\1\u02f5",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\u02f8\1\uffff\1\120\35\uffff\1\u02f7",
			"\1\u02fb\5\uffff\1\u02fc\10\uffff\1\77\20\uffff\1\u02f9\5\uffff\1\u02fa",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\u0302\20\uffff\1\u0301\3\uffff\1\u0300\3\uffff\1\65\6\uffff\1\u02ff"+
			"\20\uffff\1\u02fe\3\uffff\1\u02fd",
			"\1\111\3\uffff\1\112\26\uffff\1\107\4\uffff\1\106\3\uffff\1\110",
			"\1\uffff",
			"\1\u0304\24\uffff\1\161\12\uffff\1\u0303",
			"\1\u0306\24\uffff\1\161\12\uffff\1\u0305",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0308\27\uffff\1\u00c5\7\uffff\1\u0307",
			"\1\u030a\16\uffff\1\u00ca\20\uffff\1\u0309",
			"\1\u030c\27\uffff\1\u00c5\7\uffff\1\u030b",
			"\1\u030e\16\uffff\1\u00ca\20\uffff\1\u030d",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0310\37\uffff\1\u030f",
			"\1\u0311",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0313\3\uffff\1\u0312",
			"\1\u0314",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0316\37\uffff\1\u0315",
			"\1\uffff",
			"\1\uffff",
			"\1\u0318\37\uffff\1\u0317",
			"\1\u0319",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u031b\37\uffff\1\u031a",
			"\1\uffff",
			"\1\uffff",
			"\1\u031c\1\uffff\1\u031c",
			"\1\u031d",
			"\1\uffff",
			"\1\u031e\3\uffff\1\u031f",
			"\1\u0321\27\uffff\1\u00c5\7\uffff\1\u0320",
			"\1\u0323\16\uffff\1\u00ca\20\uffff\1\u0322",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0324\1\uffff\1\u0324",
			"\1\u0326\37\uffff\1\u0325",
			"\1\uffff",
			"\1\uffff",
			"\1\u0327",
			"\1\u0329\1\uffff\1\120\35\uffff\1\u0328",
			"\1\uffff",
			"\1\uffff",
			"\1\u032b\37\uffff\1\u032a",
			"\1\uffff",
			"\1\uffff",
			"\1\u032d\5\uffff\1\u032c",
			"\1\uffff",
			"\1\u032f\16\uffff\1\u00e3\20\uffff\1\u032e",
			"\1\uffff",
			"\1\uffff",
			"\1\u0330\1\uffff\1\u0330",
			"\1\u0332\37\uffff\1\u0331",
			"\1\uffff",
			"\1\uffff",
			"\1\152\5\uffff\1\u0334\22\uffff\1\147\6\uffff\1\150\5\uffff\1\u0333",
			"\1\u0336\24\uffff\1\161\12\uffff\1\u0335",
			"\1\uffff",
			"\1\u0338\16\uffff\1\u00e3\20\uffff\1\u0337",
			"\1\uffff",
			"\1\u033a\16\uffff\1\u00e3\20\uffff\1\u0339",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u033b",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u033c",
			"\1\uffff",
			"\1\u033e\27\uffff\1\u00c5\7\uffff\1\u033d",
			"\1\u0340\16\uffff\1\u00ca\20\uffff\1\u033f",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0342\37\uffff\1\u0341",
			"\1\uffff",
			"\1\uffff",
			"\1\u0344\1\uffff\1\120\35\uffff\1\u0343",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0346\16\uffff\1\u00e3\20\uffff\1\u0345",
			"\1\uffff",
			"\1\uffff",
			"\1\u0348\37\uffff\1\u0347",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff"
	};

	static final short[] DFA193_eot = DFA.unpackEncodedString(DFA193_eotS);
	static final short[] DFA193_eof = DFA.unpackEncodedString(DFA193_eofS);
	static final char[] DFA193_min = DFA.unpackEncodedStringToUnsignedChars(DFA193_minS);
	static final char[] DFA193_max = DFA.unpackEncodedStringToUnsignedChars(DFA193_maxS);
	static final short[] DFA193_accept = DFA.unpackEncodedString(DFA193_acceptS);
	static final short[] DFA193_special = DFA.unpackEncodedString(DFA193_specialS);
	static final short[][] DFA193_transition;

	static {
		int numStates = DFA193_transitionS.length;
		DFA193_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA193_transition[i] = DFA.unpackEncodedString(DFA193_transitionS[i]);
		}
	}

	protected class DFA193 extends DFA {

		public DFA193(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 193;
			this.eot = DFA193_eot;
			this.eof = DFA193_eof;
			this.min = DFA193_min;
			this.max = DFA193_max;
			this.accept = DFA193_accept;
			this.special = DFA193_special;
			this.transition = DFA193_transition;
		}
		@Override
		public String getDescription() {
			return "2080:9: ( ( D P ( I | C ) )=> D P ( I | C M ) | ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R ( A | E ) )=> R ( A D | E M ) | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | PERCENTAGE_SYMBOL |)";
		}
		@Override
		public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
			IntStream input = _input;
			int _s = s;
			switch ( s ) {
					case 0 : 
						int LA193_259 = input.LA(1);
						 
						int index193_259 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_259);
						if ( s>=0 ) return s;
						break;

					case 1 : 
						int LA193_63 = input.LA(1);
						s = -1;
						if ( (LA193_63=='m') ) {s = 184;}
						else if ( (LA193_63=='M') ) {s = 185;}
						else if ( (LA193_63=='s') ) {s = 186;}
						else if ( (LA193_63=='0') ) {s = 187;}
						else if ( (LA193_63=='4'||LA193_63=='6') ) {s = 188;}
						else if ( (LA193_63=='S') ) {s = 189;}
						else if ( ((LA193_63 >= '\u0000' && LA193_63 <= '\t')||LA193_63=='\u000B'||(LA193_63 >= '\u000E' && LA193_63 <= '/')||(LA193_63 >= '1' && LA193_63 <= '3')||(LA193_63 >= '8' && LA193_63 <= 'L')||(LA193_63 >= 'N' && LA193_63 <= 'R')||(LA193_63 >= 'T' && LA193_63 <= 'l')||(LA193_63 >= 'n' && LA193_63 <= 'r')||(LA193_63 >= 't' && LA193_63 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA193_63=='5'||LA193_63=='7') ) {s = 190;}
						if ( s>=0 ) return s;
						break;

					case 2 : 
						int LA193_508 = input.LA(1);
						 
						int index193_508 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_508);
						if ( s>=0 ) return s;
						break;

					case 3 : 
						int LA193_452 = input.LA(1);
						 
						int index193_452 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_452);
						if ( s>=0 ) return s;
						break;

					case 4 : 
						int LA193_201 = input.LA(1);
						 
						int index193_201 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_201);
						if ( s>=0 ) return s;
						break;

					case 5 : 
						int LA193_206 = input.LA(1);
						 
						int index193_206 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_206);
						if ( s>=0 ) return s;
						break;

					case 6 : 
						int LA193_411 = input.LA(1);
						 
						int index193_411 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_411);
						if ( s>=0 ) return s;
						break;

					case 7 : 
						int LA193_415 = input.LA(1);
						 
						int index193_415 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_415);
						if ( s>=0 ) return s;
						break;

					case 8 : 
						int LA193_831 = input.LA(1);
						 
						int index193_831 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_831);
						if ( s>=0 ) return s;
						break;

					case 9 : 
						int LA193_802 = input.LA(1);
						 
						int index193_802 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_802);
						if ( s>=0 ) return s;
						break;

					case 10 : 
						int LA193_680 = input.LA(1);
						 
						int index193_680 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_680);
						if ( s>=0 ) return s;
						break;

					case 11 : 
						int LA193_711 = input.LA(1);
						 
						int index193_711 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_711);
						if ( s>=0 ) return s;
						break;

					case 12 : 
						int LA193_777 = input.LA(1);
						 
						int index193_777 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_777);
						if ( s>=0 ) return s;
						break;

					case 13 : 
						int LA193_545 = input.LA(1);
						 
						int index193_545 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_545);
						if ( s>=0 ) return s;
						break;

					case 14 : 
						int LA193_549 = input.LA(1);
						 
						int index193_549 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_549);
						if ( s>=0 ) return s;
						break;

					case 15 : 
						int LA193_586 = input.LA(1);
						 
						int index193_586 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_586);
						if ( s>=0 ) return s;
						break;

					case 16 : 
						int LA193_676 = input.LA(1);
						 
						int index193_676 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_676);
						if ( s>=0 ) return s;
						break;

					case 17 : 
						int LA193_781 = input.LA(1);
						 
						int index193_781 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_781);
						if ( s>=0 ) return s;
						break;

					case 18 : 
						int LA193_453 = input.LA(1);
						 
						int index193_453 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_453);
						if ( s>=0 ) return s;
						break;

					case 19 : 
						int LA193_203 = input.LA(1);
						 
						int index193_203 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_203);
						if ( s>=0 ) return s;
						break;

					case 20 : 
						int LA193_207 = input.LA(1);
						 
						int index193_207 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_207);
						if ( s>=0 ) return s;
						break;

					case 21 : 
						int LA193_412 = input.LA(1);
						 
						int index193_412 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_412);
						if ( s>=0 ) return s;
						break;

					case 22 : 
						int LA193_677 = input.LA(1);
						 
						int index193_677 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_677);
						if ( s>=0 ) return s;
						break;

					case 23 : 
						int LA193_416 = input.LA(1);
						 
						int index193_416 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_416);
						if ( s>=0 ) return s;
						break;

					case 24 : 
						int LA193_546 = input.LA(1);
						 
						int index193_546 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_546);
						if ( s>=0 ) return s;
						break;

					case 25 : 
						int LA193_803 = input.LA(1);
						 
						int index193_803 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_803);
						if ( s>=0 ) return s;
						break;

					case 26 : 
						int LA193_832 = input.LA(1);
						 
						int index193_832 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_832);
						if ( s>=0 ) return s;
						break;

					case 27 : 
						int LA193_778 = input.LA(1);
						 
						int index193_778 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_778);
						if ( s>=0 ) return s;
						break;

					case 28 : 
						int LA193_550 = input.LA(1);
						 
						int index193_550 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_550);
						if ( s>=0 ) return s;
						break;

					case 29 : 
						int LA193_782 = input.LA(1);
						 
						int index193_782 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_782);
						if ( s>=0 ) return s;
						break;

					case 30 : 
						int LA193_587 = input.LA(1);
						 
						int index193_587 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_587);
						if ( s>=0 ) return s;
						break;

					case 31 : 
						int LA193_681 = input.LA(1);
						 
						int index193_681 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_681);
						if ( s>=0 ) return s;
						break;

					case 32 : 
						int LA193_712 = input.LA(1);
						 
						int index193_712 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_712);
						if ( s>=0 ) return s;
						break;

					case 33 : 
						int LA193_590 = input.LA(1);
						 
						int index193_590 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_590);
						if ( s>=0 ) return s;
						break;

					case 34 : 
						int LA193_591 = input.LA(1);
						 
						int index193_591 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_591);
						if ( s>=0 ) return s;
						break;

					case 35 : 
						int LA193_71 = input.LA(1);
						s = -1;
						if ( ((LA193_71 >= '\u0000' && LA193_71 <= '\t')||LA193_71=='\u000B'||(LA193_71 >= '\u000E' && LA193_71 <= '/')||(LA193_71 >= '1' && LA193_71 <= '3')||LA193_71=='5'||(LA193_71 >= '7' && LA193_71 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA193_71=='0') ) {s = 199;}
						else if ( (LA193_71=='4'||LA193_71=='6') ) {s = 200;}
						if ( s>=0 ) return s;
						break;

					case 36 : 
						int LA193_428 = input.LA(1);
						 
						int index193_428 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_428);
						if ( s>=0 ) return s;
						break;

					case 37 : 
						int LA193_805 = input.LA(1);
						 
						int index193_805 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_805);
						if ( s>=0 ) return s;
						break;

					case 38 : 
						int LA193_806 = input.LA(1);
						 
						int index193_806 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_806);
						if ( s>=0 ) return s;
						break;

					case 39 : 
						int LA193_794 = input.LA(1);
						 
						int index193_794 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_794);
						if ( s>=0 ) return s;
						break;

					case 40 : 
						int LA193_731 = input.LA(1);
						 
						int index193_731 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_731);
						if ( s>=0 ) return s;
						break;

					case 41 : 
						int LA193_795 = input.LA(1);
						 
						int index193_795 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_795);
						if ( s>=0 ) return s;
						break;

					case 42 : 
						int LA193_732 = input.LA(1);
						 
						int index193_732 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_732);
						if ( s>=0 ) return s;
						break;

					case 43 : 
						int LA193_691 = input.LA(1);
						 
						int index193_691 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_691);
						if ( s>=0 ) return s;
						break;

					case 44 : 
						int LA193_543 = input.LA(1);
						 
						int index193_543 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_543);
						if ( s>=0 ) return s;
						break;

					case 45 : 
						int LA193_547 = input.LA(1);
						 
						int index193_547 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_547);
						if ( s>=0 ) return s;
						break;

					case 46 : 
						int LA193_544 = input.LA(1);
						 
						int index193_544 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_544);
						if ( s>=0 ) return s;
						break;

					case 47 : 
						int LA193_548 = input.LA(1);
						 
						int index193_548 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_548);
						if ( s>=0 ) return s;
						break;

					case 48 : 
						int LA193_554 = input.LA(1);
						 
						int index193_554 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_554);
						if ( s>=0 ) return s;
						break;

					case 49 : 
						int LA193_555 = input.LA(1);
						 
						int index193_555 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_555);
						if ( s>=0 ) return s;
						break;

					case 50 : 
						int LA193_397 = input.LA(1);
						 
						int index193_397 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_397);
						if ( s>=0 ) return s;
						break;

					case 51 : 
						int LA193_400 = input.LA(1);
						 
						int index193_400 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_400);
						if ( s>=0 ) return s;
						break;

					case 52 : 
						int LA193_311 = input.LA(1);
						 
						int index193_311 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_311);
						if ( s>=0 ) return s;
						break;

					case 53 : 
						int LA193_839 = input.LA(1);
						 
						int index193_839 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_839);
						if ( s>=0 ) return s;
						break;

					case 54 : 
						int LA193_840 = input.LA(1);
						 
						int index193_840 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_840);
						if ( s>=0 ) return s;
						break;

					case 55 : 
						int LA193_300 = input.LA(1);
						 
						int index193_300 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_300);
						if ( s>=0 ) return s;
						break;

					case 56 : 
						int LA193_570 = input.LA(1);
						 
						int index193_570 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_570);
						if ( s>=0 ) return s;
						break;

					case 57 : 
						int LA193_26 = input.LA(1);
						s = -1;
						if ( (LA193_26=='p') ) {s = 107;}
						else if ( (LA193_26=='P') ) {s = 108;}
						else if ( ((LA193_26 >= '\u0000' && LA193_26 <= '\t')||LA193_26=='\u000B'||(LA193_26 >= '\u000E' && LA193_26 <= '/')||(LA193_26 >= '1' && LA193_26 <= '3')||(LA193_26 >= '8' && LA193_26 <= 'O')||(LA193_26 >= 'Q' && LA193_26 <= 'o')||(LA193_26 >= 'q' && LA193_26 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA193_26=='0') ) {s = 109;}
						else if ( (LA193_26=='5'||LA193_26=='7') ) {s = 110;}
						else if ( (LA193_26=='4'||LA193_26=='6') ) {s = 111;}
						if ( s>=0 ) return s;
						break;

					case 58 : 
						int LA193_571 = input.LA(1);
						 
						int index193_571 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_571);
						if ( s>=0 ) return s;
						break;

					case 59 : 
						int LA193_450 = input.LA(1);
						 
						int index193_450 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_450);
						if ( s>=0 ) return s;
						break;

					case 60 : 
						int LA193_783 = input.LA(1);
						 
						int index193_783 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_783);
						if ( s>=0 ) return s;
						break;

					case 61 : 
						int LA193_451 = input.LA(1);
						 
						int index193_451 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_451);
						if ( s>=0 ) return s;
						break;

					case 62 : 
						int LA193_784 = input.LA(1);
						 
						int index193_784 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_784);
						if ( s>=0 ) return s;
						break;

					case 63 : 
						int LA193_579 = input.LA(1);
						 
						int index193_579 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_579);
						if ( s>=0 ) return s;
						break;

					case 64 : 
						int LA193_709 = input.LA(1);
						 
						int index193_709 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_709);
						if ( s>=0 ) return s;
						break;

					case 65 : 
						int LA193_710 = input.LA(1);
						 
						int index193_710 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_710);
						if ( s>=0 ) return s;
						break;

					case 66 : 
						int LA193_662 = input.LA(1);
						 
						int index193_662 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_662);
						if ( s>=0 ) return s;
						break;

					case 67 : 
						int LA193_665 = input.LA(1);
						 
						int index193_665 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_665);
						if ( s>=0 ) return s;
						break;

					case 68 : 
						int LA193_374 = input.LA(1);
						 
						int index193_374 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_374);
						if ( s>=0 ) return s;
						break;

					case 69 : 
						int LA193_797 = input.LA(1);
						 
						int index193_797 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_797);
						if ( s>=0 ) return s;
						break;

					case 70 : 
						int LA193_139 = input.LA(1);
						 
						int index193_139 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_139);
						if ( s>=0 ) return s;
						break;

					case 71 : 
						int LA193_121 = input.LA(1);
						 
						int index193_121 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_121);
						if ( s>=0 ) return s;
						break;

					case 72 : 
						int LA193_54 = input.LA(1);
						 
						int index193_54 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_54);
						if ( s>=0 ) return s;
						break;

					case 73 : 
						int LA193_87 = input.LA(1);
						 
						int index193_87 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_87);
						if ( s>=0 ) return s;
						break;

					case 74 : 
						int LA193_281 = input.LA(1);
						 
						int index193_281 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_281);
						if ( s>=0 ) return s;
						break;

					case 75 : 
						int LA193_766 = input.LA(1);
						 
						int index193_766 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_766);
						if ( s>=0 ) return s;
						break;

					case 76 : 
						int LA193_359 = input.LA(1);
						 
						int index193_359 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_359);
						if ( s>=0 ) return s;
						break;

					case 77 : 
						int LA193_396 = input.LA(1);
						 
						int index193_396 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_396);
						if ( s>=0 ) return s;
						break;

					case 78 : 
						int LA193_530 = input.LA(1);
						 
						int index193_530 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_530);
						if ( s>=0 ) return s;
						break;

					case 79 : 
						int LA193_661 = input.LA(1);
						 
						int index193_661 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_661);
						if ( s>=0 ) return s;
						break;

					case 80 : 
						int LA193_57 = input.LA(1);
						 
						int index193_57 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_57);
						if ( s>=0 ) return s;
						break;

					case 81 : 
						int LA193_90 = input.LA(1);
						 
						int index193_90 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_90);
						if ( s>=0 ) return s;
						break;

					case 82 : 
						int LA193_122 = input.LA(1);
						 
						int index193_122 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_122);
						if ( s>=0 ) return s;
						break;

					case 83 : 
						int LA193_140 = input.LA(1);
						 
						int index193_140 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_140);
						if ( s>=0 ) return s;
						break;

					case 84 : 
						int LA193_284 = input.LA(1);
						 
						int index193_284 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_284);
						if ( s>=0 ) return s;
						break;

					case 85 : 
						int LA193_399 = input.LA(1);
						 
						int index193_399 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_399);
						if ( s>=0 ) return s;
						break;

					case 86 : 
						int LA193_533 = input.LA(1);
						 
						int index193_533 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_533);
						if ( s>=0 ) return s;
						break;

					case 87 : 
						int LA193_664 = input.LA(1);
						 
						int index193_664 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_664);
						if ( s>=0 ) return s;
						break;

					case 88 : 
						int LA193_769 = input.LA(1);
						 
						int index193_769 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_769);
						if ( s>=0 ) return s;
						break;

					case 89 : 
						int LA193_152 = input.LA(1);
						 
						int index193_152 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_152);
						if ( s>=0 ) return s;
						break;

					case 90 : 
						int LA193_145 = input.LA(1);
						 
						int index193_145 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_145);
						if ( s>=0 ) return s;
						break;

					case 91 : 
						int LA193_64 = input.LA(1);
						 
						int index193_64 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_64);
						if ( s>=0 ) return s;
						break;

					case 92 : 
						int LA193_518 = input.LA(1);
						 
						int index193_518 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_518);
						if ( s>=0 ) return s;
						break;

					case 93 : 
						int LA193_754 = input.LA(1);
						 
						int index193_754 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_754);
						if ( s>=0 ) return s;
						break;

					case 94 : 
						int LA193_95 = input.LA(1);
						 
						int index193_95 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_95);
						if ( s>=0 ) return s;
						break;

					case 95 : 
						int LA193_526 = input.LA(1);
						 
						int index193_526 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_526);
						if ( s>=0 ) return s;
						break;

					case 96 : 
						int LA193_657 = input.LA(1);
						 
						int index193_657 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_657);
						if ( s>=0 ) return s;
						break;

					case 97 : 
						int LA193_269 = input.LA(1);
						 
						int index193_269 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_269);
						if ( s>=0 ) return s;
						break;

					case 98 : 
						int LA193_277 = input.LA(1);
						 
						int index193_277 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_277);
						if ( s>=0 ) return s;
						break;

					case 99 : 
						int LA193_649 = input.LA(1);
						 
						int index193_649 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_649);
						if ( s>=0 ) return s;
						break;

					case 100 : 
						int LA193_384 = input.LA(1);
						 
						int index193_384 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_384);
						if ( s>=0 ) return s;
						break;

					case 101 : 
						int LA193_392 = input.LA(1);
						 
						int index193_392 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_392);
						if ( s>=0 ) return s;
						break;

					case 102 : 
						int LA193_762 = input.LA(1);
						 
						int index193_762 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_762);
						if ( s>=0 ) return s;
						break;

					case 103 : 
						int LA193_97 = input.LA(1);
						 
						int index193_97 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_97);
						if ( s>=0 ) return s;
						break;

					case 104 : 
						int LA193_66 = input.LA(1);
						 
						int index193_66 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_66);
						if ( s>=0 ) return s;
						break;

					case 105 : 
						int LA193_146 = input.LA(1);
						 
						int index193_146 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_146);
						if ( s>=0 ) return s;
						break;

					case 106 : 
						int LA193_659 = input.LA(1);
						 
						int index193_659 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_659);
						if ( s>=0 ) return s;
						break;

					case 107 : 
						int LA193_153 = input.LA(1);
						 
						int index193_153 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_153);
						if ( s>=0 ) return s;
						break;

					case 108 : 
						int LA193_271 = input.LA(1);
						 
						int index193_271 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_271);
						if ( s>=0 ) return s;
						break;

					case 109 : 
						int LA193_756 = input.LA(1);
						 
						int index193_756 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_756);
						if ( s>=0 ) return s;
						break;

					case 110 : 
						int LA193_764 = input.LA(1);
						 
						int index193_764 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_764);
						if ( s>=0 ) return s;
						break;

					case 111 : 
						int LA193_651 = input.LA(1);
						 
						int index193_651 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_651);
						if ( s>=0 ) return s;
						break;

					case 112 : 
						int LA193_279 = input.LA(1);
						 
						int index193_279 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_279);
						if ( s>=0 ) return s;
						break;

					case 113 : 
						int LA193_386 = input.LA(1);
						 
						int index193_386 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_386);
						if ( s>=0 ) return s;
						break;

					case 114 : 
						int LA193_394 = input.LA(1);
						 
						int index193_394 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_394);
						if ( s>=0 ) return s;
						break;

					case 115 : 
						int LA193_528 = input.LA(1);
						 
						int index193_528 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_528);
						if ( s>=0 ) return s;
						break;

					case 116 : 
						int LA193_520 = input.LA(1);
						 
						int index193_520 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_520);
						if ( s>=0 ) return s;
						break;

					case 117 : 
						int LA193_715 = input.LA(1);
						 
						int index193_715 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_715);
						if ( s>=0 ) return s;
						break;

					case 118 : 
						int LA193_716 = input.LA(1);
						 
						int index193_716 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_716);
						if ( s>=0 ) return s;
						break;

					case 119 : 
						int LA193_562 = input.LA(1);
						 
						int index193_562 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_562);
						if ( s>=0 ) return s;
						break;

					case 120 : 
						int LA193_791 = input.LA(1);
						 
						int index193_791 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_791);
						if ( s>=0 ) return s;
						break;

					case 121 : 
						int LA193_792 = input.LA(1);
						 
						int index193_792 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_792);
						if ( s>=0 ) return s;
						break;

					case 122 : 
						int LA193_409 = input.LA(1);
						 
						int index193_409 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_409);
						if ( s>=0 ) return s;
						break;

					case 123 : 
						int LA193_413 = input.LA(1);
						 
						int index193_413 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_413);
						if ( s>=0 ) return s;
						break;

					case 124 : 
						int LA193_410 = input.LA(1);
						 
						int index193_410 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_410);
						if ( s>=0 ) return s;
						break;

					case 125 : 
						int LA193_414 = input.LA(1);
						 
						int index193_414 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_414);
						if ( s>=0 ) return s;
						break;

					case 126 : 
						int LA193_186 = input.LA(1);
						 
						int index193_186 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_186);
						if ( s>=0 ) return s;
						break;

					case 127 : 
						int LA193_189 = input.LA(1);
						 
						int index193_189 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_189);
						if ( s>=0 ) return s;
						break;

					case 128 : 
						int LA193_173 = input.LA(1);
						 
						int index193_173 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_173);
						if ( s>=0 ) return s;
						break;

					case 129 : 
						int LA193_176 = input.LA(1);
						 
						int index193_176 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_176);
						if ( s>=0 ) return s;
						break;

					case 130 : 
						int LA193_639 = input.LA(1);
						 
						int index193_639 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_639);
						if ( s>=0 ) return s;
						break;

					case 131 : 
						int LA193_214 = input.LA(1);
						 
						int index193_214 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_214);
						if ( s>=0 ) return s;
						break;

					case 132 : 
						int LA193_100 = input.LA(1);
						 
						int index193_100 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_100);
						if ( s>=0 ) return s;
						break;

					case 133 : 
						int LA193_79 = input.LA(1);
						 
						int index193_79 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_79);
						if ( s>=0 ) return s;
						break;

					case 134 : 
						int LA193_158 = input.LA(1);
						 
						int index193_158 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_158);
						if ( s>=0 ) return s;
						break;

					case 135 : 
						int LA193_461 = input.LA(1);
						 
						int index193_461 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_461);
						if ( s>=0 ) return s;
						break;

					case 136 : 
						int LA193_595 = input.LA(1);
						 
						int index193_595 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_595);
						if ( s>=0 ) return s;
						break;

					case 137 : 
						int LA193_160 = input.LA(1);
						 
						int index193_160 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_160);
						if ( s>=0 ) return s;
						break;

					case 138 : 
						int LA193_208 = input.LA(1);
						 
						int index193_208 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_208);
						if ( s>=0 ) return s;
						break;

					case 139 : 
						int LA193_808 = input.LA(1);
						 
						int index193_808 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_808);
						if ( s>=0 ) return s;
						break;

					case 140 : 
						int LA193_719 = input.LA(1);
						 
						int index193_719 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_719);
						if ( s>=0 ) return s;
						break;

					case 141 : 
						int LA193_759 = input.LA(1);
						 
						int index193_759 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_759);
						if ( s>=0 ) return s;
						break;

					case 142 : 
						int LA193_274 = input.LA(1);
						 
						int index193_274 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_274);
						if ( s>=0 ) return s;
						break;

					case 143 : 
						int LA193_389 = input.LA(1);
						 
						int index193_389 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_389);
						if ( s>=0 ) return s;
						break;

					case 144 : 
						int LA193_327 = input.LA(1);
						 
						int index193_327 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_327);
						if ( s>=0 ) return s;
						break;

					case 145 : 
						int LA193_835 = input.LA(1);
						 
						int index193_835 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_835);
						if ( s>=0 ) return s;
						break;

					case 146 : 
						int LA193_329 = input.LA(1);
						 
						int index193_329 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_329);
						if ( s>=0 ) return s;
						break;

					case 147 : 
						int LA193_523 = input.LA(1);
						 
						int index193_523 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_523);
						if ( s>=0 ) return s;
						break;

					case 148 : 
						int LA193_654 = input.LA(1);
						 
						int index193_654 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_654);
						if ( s>=0 ) return s;
						break;

					case 149 : 
						int LA193_81 = input.LA(1);
						 
						int index193_81 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_81);
						if ( s>=0 ) return s;
						break;

					case 150 : 
						int LA193_101 = input.LA(1);
						 
						int index193_101 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_101);
						if ( s>=0 ) return s;
						break;

					case 151 : 
						int LA193_159 = input.LA(1);
						 
						int index193_159 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_159);
						if ( s>=0 ) return s;
						break;

					case 152 : 
						int LA193_809 = input.LA(1);
						 
						int index193_809 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_809);
						if ( s>=0 ) return s;
						break;

					case 153 : 
						int LA193_524 = input.LA(1);
						 
						int index193_524 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_524);
						if ( s>=0 ) return s;
						break;

					case 154 : 
						int LA193_161 = input.LA(1);
						 
						int index193_161 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_161);
						if ( s>=0 ) return s;
						break;

					case 155 : 
						int LA193_209 = input.LA(1);
						 
						int index193_209 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_209);
						if ( s>=0 ) return s;
						break;

					case 156 : 
						int LA193_655 = input.LA(1);
						 
						int index193_655 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_655);
						if ( s>=0 ) return s;
						break;

					case 157 : 
						int LA193_462 = input.LA(1);
						 
						int index193_462 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_462);
						if ( s>=0 ) return s;
						break;

					case 158 : 
						int LA193_215 = input.LA(1);
						 
						int index193_215 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_215);
						if ( s>=0 ) return s;
						break;

					case 159 : 
						int LA193_275 = input.LA(1);
						 
						int index193_275 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_275);
						if ( s>=0 ) return s;
						break;

					case 160 : 
						int LA193_720 = input.LA(1);
						 
						int index193_720 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_720);
						if ( s>=0 ) return s;
						break;

					case 161 : 
						int LA193_760 = input.LA(1);
						 
						int index193_760 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_760);
						if ( s>=0 ) return s;
						break;

					case 162 : 
						int LA193_328 = input.LA(1);
						 
						int index193_328 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_328);
						if ( s>=0 ) return s;
						break;

					case 163 : 
						int LA193_836 = input.LA(1);
						 
						int index193_836 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_836);
						if ( s>=0 ) return s;
						break;

					case 164 : 
						int LA193_596 = input.LA(1);
						 
						int index193_596 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_596);
						if ( s>=0 ) return s;
						break;

					case 165 : 
						int LA193_330 = input.LA(1);
						 
						int index193_330 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_330);
						if ( s>=0 ) return s;
						break;

					case 166 : 
						int LA193_390 = input.LA(1);
						 
						int index193_390 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_390);
						if ( s>=0 ) return s;
						break;

					case 167 : 
						int LA193_833 = input.LA(1);
						 
						int index193_833 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_833);
						if ( s>=0 ) return s;
						break;

					case 168 : 
						int LA193_834 = input.LA(1);
						 
						int index193_834 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_834);
						if ( s>=0 ) return s;
						break;

					case 169 : 
						int LA193_241 = input.LA(1);
						 
						int index193_241 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_241);
						if ( s>=0 ) return s;
						break;

					case 170 : 
						int LA193_242 = input.LA(1);
						 
						int index193_242 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_242);
						if ( s>=0 ) return s;
						break;

					case 171 : 
						int LA193_438 = input.LA(1);
						 
						int index193_438 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_438);
						if ( s>=0 ) return s;
						break;

					case 172 : 
						int LA193_788 = input.LA(1);
						 
						int index193_788 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_788);
						if ( s>=0 ) return s;
						break;

					case 173 : 
						int LA193_674 = input.LA(1);
						 
						int index193_674 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_674);
						if ( s>=0 ) return s;
						break;

					case 174 : 
						int LA193_678 = input.LA(1);
						 
						int index193_678 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_678);
						if ( s>=0 ) return s;
						break;

					case 175 : 
						int LA193_675 = input.LA(1);
						 
						int index193_675 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_675);
						if ( s>=0 ) return s;
						break;

					case 176 : 
						int LA193_679 = input.LA(1);
						 
						int index193_679 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_679);
						if ( s>=0 ) return s;
						break;

					case 177 : 
						int LA193_49 = input.LA(1);
						 
						int index193_49 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_49);
						if ( s>=0 ) return s;
						break;

					case 178 : 
						int LA193_83 = input.LA(1);
						 
						int index193_83 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_83);
						if ( s>=0 ) return s;
						break;

					case 179 : 
						int LA193_263 = input.LA(1);
						 
						int index193_263 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_263);
						if ( s>=0 ) return s;
						break;

					case 180 : 
						int LA193_378 = input.LA(1);
						 
						int index193_378 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_378);
						if ( s>=0 ) return s;
						break;

					case 181 : 
						int LA193_512 = input.LA(1);
						 
						int index193_512 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_512);
						if ( s>=0 ) return s;
						break;

					case 182 : 
						int LA193_643 = input.LA(1);
						 
						int index193_643 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_643);
						if ( s>=0 ) return s;
						break;

					case 183 : 
						int LA193_748 = input.LA(1);
						 
						int index193_748 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_748);
						if ( s>=0 ) return s;
						break;

					case 184 : 
						int LA193_51 = input.LA(1);
						 
						int index193_51 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_51);
						if ( s>=0 ) return s;
						break;

					case 185 : 
						int LA193_85 = input.LA(1);
						 
						int index193_85 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_85);
						if ( s>=0 ) return s;
						break;

					case 186 : 
						int LA193_265 = input.LA(1);
						 
						int index193_265 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_265);
						if ( s>=0 ) return s;
						break;

					case 187 : 
						int LA193_380 = input.LA(1);
						 
						int index193_380 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_380);
						if ( s>=0 ) return s;
						break;

					case 188 : 
						int LA193_514 = input.LA(1);
						 
						int index193_514 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_514);
						if ( s>=0 ) return s;
						break;

					case 189 : 
						int LA193_645 = input.LA(1);
						 
						int index193_645 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_645);
						if ( s>=0 ) return s;
						break;

					case 190 : 
						int LA193_750 = input.LA(1);
						 
						int index193_750 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_750);
						if ( s>=0 ) return s;
						break;

					case 191 : 
						int LA193_427 = input.LA(1);
						 
						int index193_427 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_427);
						if ( s>=0 ) return s;
						break;

					case 192 : 
						int LA193_699 = input.LA(1);
						 
						int index193_699 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_699);
						if ( s>=0 ) return s;
						break;

					case 193 : 
						int LA193_80 = input.LA(1);
						s = -1;
						if ( (LA193_80=='z') ) {s = 217;}
						else if ( (LA193_80=='Z') ) {s = 218;}
						else if ( ((LA193_80 >= '\u0000' && LA193_80 <= '\t')||LA193_80=='\u000B'||(LA193_80 >= '\u000E' && LA193_80 <= '/')||(LA193_80 >= '1' && LA193_80 <= '4')||LA193_80=='6'||(LA193_80 >= '8' && LA193_80 <= 'Y')||(LA193_80 >= '[' && LA193_80 <= 'y')||(LA193_80 >= '{' && LA193_80 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA193_80=='0') ) {s = 219;}
						else if ( (LA193_80=='5'||LA193_80=='7') ) {s = 220;}
						if ( s>=0 ) return s;
						break;

					case 194 : 
						int LA193_690 = input.LA(1);
						 
						int index193_690 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_690);
						if ( s>=0 ) return s;
						break;

					case 195 : 
						int LA193_584 = input.LA(1);
						 
						int index193_584 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_584);
						if ( s>=0 ) return s;
						break;

					case 196 : 
						int LA193_585 = input.LA(1);
						 
						int index193_585 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_585);
						if ( s>=0 ) return s;
						break;

					case 197 : 
						int LA193_280 = input.LA(1);
						 
						int index193_280 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_280);
						if ( s>=0 ) return s;
						break;

					case 198 : 
						int LA193_52 = input.LA(1);
						 
						int index193_52 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_52);
						if ( s>=0 ) return s;
						break;

					case 199 : 
						int LA193_86 = input.LA(1);
						 
						int index193_86 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_86);
						if ( s>=0 ) return s;
						break;

					case 200 : 
						int LA193_765 = input.LA(1);
						 
						int index193_765 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_765);
						if ( s>=0 ) return s;
						break;

					case 201 : 
						int LA193_119 = input.LA(1);
						 
						int index193_119 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_119);
						if ( s>=0 ) return s;
						break;

					case 202 : 
						int LA193_137 = input.LA(1);
						 
						int index193_137 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_137);
						if ( s>=0 ) return s;
						break;

					case 203 : 
						int LA193_660 = input.LA(1);
						 
						int index193_660 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_660);
						if ( s>=0 ) return s;
						break;

					case 204 : 
						int LA193_395 = input.LA(1);
						 
						int index193_395 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_395);
						if ( s>=0 ) return s;
						break;

					case 205 : 
						int LA193_529 = input.LA(1);
						 
						int index193_529 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_529);
						if ( s>=0 ) return s;
						break;

					case 206 : 
						int LA193_295 = input.LA(1);
						 
						int index193_295 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_295);
						if ( s>=0 ) return s;
						break;

					case 207 : 
						int LA193_398 = input.LA(1);
						 
						int index193_398 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_398);
						if ( s>=0 ) return s;
						break;

					case 208 : 
						int LA193_56 = input.LA(1);
						 
						int index193_56 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_56);
						if ( s>=0 ) return s;
						break;

					case 209 : 
						int LA193_89 = input.LA(1);
						 
						int index193_89 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_89);
						if ( s>=0 ) return s;
						break;

					case 210 : 
						int LA193_120 = input.LA(1);
						 
						int index193_120 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_120);
						if ( s>=0 ) return s;
						break;

					case 211 : 
						int LA193_138 = input.LA(1);
						 
						int index193_138 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_138);
						if ( s>=0 ) return s;
						break;

					case 212 : 
						int LA193_283 = input.LA(1);
						 
						int index193_283 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_283);
						if ( s>=0 ) return s;
						break;

					case 213 : 
						int LA193_532 = input.LA(1);
						 
						int index193_532 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_532);
						if ( s>=0 ) return s;
						break;

					case 214 : 
						int LA193_663 = input.LA(1);
						 
						int index193_663 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_663);
						if ( s>=0 ) return s;
						break;

					case 215 : 
						int LA193_768 = input.LA(1);
						 
						int index193_768 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_768);
						if ( s>=0 ) return s;
						break;

					case 216 : 
						int LA193_336 = input.LA(1);
						 
						int index193_336 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_336);
						if ( s>=0 ) return s;
						break;

					case 217 : 
						int LA193_337 = input.LA(1);
						 
						int index193_337 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_337);
						if ( s>=0 ) return s;
						break;

					case 218 : 
						int LA193_705 = input.LA(1);
						 
						int index193_705 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_705);
						if ( s>=0 ) return s;
						break;

					case 219 : 
						int LA193_299 = input.LA(1);
						 
						int index193_299 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_299);
						if ( s>=0 ) return s;
						break;

					case 220 : 
						int LA193_494 = input.LA(1);
						 
						int index193_494 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_494);
						if ( s>=0 ) return s;
						break;

					case 221 : 
						int LA193_746 = input.LA(1);
						 
						int index193_746 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_746);
						if ( s>=0 ) return s;
						break;

					case 222 : 
						int LA193_800 = input.LA(1);
						 
						int index193_800 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_800);
						if ( s>=0 ) return s;
						break;

					case 223 : 
						int LA193_801 = input.LA(1);
						 
						int index193_801 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_801);
						if ( s>=0 ) return s;
						break;

					case 224 : 
						int LA193_340 = input.LA(1);
						 
						int index193_340 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_340);
						if ( s>=0 ) return s;
						break;

					case 225 : 
						int LA193_828 = input.LA(1);
						 
						int index193_828 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_828);
						if ( s>=0 ) return s;
						break;

					case 226 : 
						int LA193_197 = input.LA(1);
						s = -1;
						if ( ((LA193_197 >= '\u0000' && LA193_197 <= '\t')||LA193_197=='\u000B'||(LA193_197 >= '\u000E' && LA193_197 <= '/')||(LA193_197 >= '1' && LA193_197 <= '3')||LA193_197=='5'||(LA193_197 >= '7' && LA193_197 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA193_197=='0') ) {s = 317;}
						else if ( (LA193_197=='4'||LA193_197=='6') ) {s = 318;}
						if ( s>=0 ) return s;
						break;

					case 227 : 
						int LA193_165 = input.LA(1);
						 
						int index193_165 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_165);
						if ( s>=0 ) return s;
						break;

					case 228 : 
						int LA193_168 = input.LA(1);
						 
						int index193_168 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_168);
						if ( s>=0 ) return s;
						break;

					case 229 : 
						int LA193_217 = input.LA(1);
						 
						int index193_217 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_217);
						if ( s>=0 ) return s;
						break;

					case 230 : 
						int LA193_218 = input.LA(1);
						 
						int index193_218 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_218);
						if ( s>=0 ) return s;
						break;

					case 231 : 
						int LA193_117 = input.LA(1);
						 
						int index193_117 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_117);
						if ( s>=0 ) return s;
						break;

					case 232 : 
						int LA193_112 = input.LA(1);
						 
						int index193_112 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_112);
						if ( s>=0 ) return s;
						break;

					case 233 : 
						int LA193_672 = input.LA(1);
						 
						int index193_672 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_672);
						if ( s>=0 ) return s;
						break;

					case 234 : 
						int LA193_355 = input.LA(1);
						 
						int index193_355 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_355);
						if ( s>=0 ) return s;
						break;

					case 235 : 
						int LA193_405 = input.LA(1);
						 
						int index193_405 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_405);
						if ( s>=0 ) return s;
						break;

					case 236 : 
						int LA193_821 = input.LA(1);
						 
						int index193_821 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_821);
						if ( s>=0 ) return s;
						break;

					case 237 : 
						int LA193_670 = input.LA(1);
						 
						int index193_670 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_670);
						if ( s>=0 ) return s;
						break;

					case 238 : 
						int LA193_739 = input.LA(1);
						 
						int index193_739 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_739);
						if ( s>=0 ) return s;
						break;

					case 239 : 
						int LA193_407 = input.LA(1);
						 
						int index193_407 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_407);
						if ( s>=0 ) return s;
						break;

					case 240 : 
						int LA193_771 = input.LA(1);
						 
						int index193_771 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_771);
						if ( s>=0 ) return s;
						break;

					case 241 : 
						int LA193_486 = input.LA(1);
						 
						int index193_486 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_486);
						if ( s>=0 ) return s;
						break;

					case 242 : 
						int LA193_539 = input.LA(1);
						 
						int index193_539 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_539);
						if ( s>=0 ) return s;
						break;

					case 243 : 
						int LA193_541 = input.LA(1);
						 
						int index193_541 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_541);
						if ( s>=0 ) return s;
						break;

					case 244 : 
						int LA193_619 = input.LA(1);
						 
						int index193_619 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_619);
						if ( s>=0 ) return s;
						break;

					case 245 : 
						int LA193_773 = input.LA(1);
						 
						int index193_773 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_773);
						if ( s>=0 ) return s;
						break;

					case 246 : 
						int LA193_114 = input.LA(1);
						 
						int index193_114 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_114);
						if ( s>=0 ) return s;
						break;

					case 247 : 
						int LA193_118 = input.LA(1);
						 
						int index193_118 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_118);
						if ( s>=0 ) return s;
						break;

					case 248 : 
						int LA193_822 = input.LA(1);
						 
						int index193_822 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_822);
						if ( s>=0 ) return s;
						break;

					case 249 : 
						int LA193_356 = input.LA(1);
						 
						int index193_356 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_356);
						if ( s>=0 ) return s;
						break;

					case 250 : 
						int LA193_740 = input.LA(1);
						 
						int index193_740 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_740);
						if ( s>=0 ) return s;
						break;

					case 251 : 
						int LA193_671 = input.LA(1);
						 
						int index193_671 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_671);
						if ( s>=0 ) return s;
						break;

					case 252 : 
						int LA193_406 = input.LA(1);
						 
						int index193_406 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_406);
						if ( s>=0 ) return s;
						break;

					case 253 : 
						int LA193_408 = input.LA(1);
						 
						int index193_408 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_408);
						if ( s>=0 ) return s;
						break;

					case 254 : 
						int LA193_487 = input.LA(1);
						 
						int index193_487 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_487);
						if ( s>=0 ) return s;
						break;

					case 255 : 
						int LA193_772 = input.LA(1);
						 
						int index193_772 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_772);
						if ( s>=0 ) return s;
						break;

					case 256 : 
						int LA193_774 = input.LA(1);
						 
						int index193_774 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_774);
						if ( s>=0 ) return s;
						break;

					case 257 : 
						int LA193_673 = input.LA(1);
						 
						int index193_673 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_673);
						if ( s>=0 ) return s;
						break;

					case 258 : 
						int LA193_540 = input.LA(1);
						 
						int index193_540 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_540);
						if ( s>=0 ) return s;
						break;

					case 259 : 
						int LA193_542 = input.LA(1);
						 
						int index193_542 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_542);
						if ( s>=0 ) return s;
						break;

					case 260 : 
						int LA193_620 = input.LA(1);
						 
						int index193_620 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_620);
						if ( s>=0 ) return s;
						break;

					case 261 : 
						int LA193_304 = input.LA(1);
						 
						int index193_304 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_304);
						if ( s>=0 ) return s;
						break;

					case 262 : 
						int LA193_305 = input.LA(1);
						 
						int index193_305 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_305);
						if ( s>=0 ) return s;
						break;

					case 263 : 
						int LA193_171 = input.LA(1);
						 
						int index193_171 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_171);
						if ( s>=0 ) return s;
						break;

					case 264 : 
						int LA193_172 = input.LA(1);
						 
						int index193_172 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_172);
						if ( s>=0 ) return s;
						break;

					case 265 : 
						int LA193_572 = input.LA(1);
						 
						int index193_572 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_572);
						if ( s>=0 ) return s;
						break;

					case 266 : 
						int LA193_561 = input.LA(1);
						 
						int index193_561 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_561);
						if ( s>=0 ) return s;
						break;

					case 267 : 
						int LA193_422 = input.LA(1);
						 
						int index193_422 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_422);
						if ( s>=0 ) return s;
						break;

					case 268 : 
						int LA193_465 = input.LA(1);
						 
						int index193_465 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_465);
						if ( s>=0 ) return s;
						break;

					case 269 : 
						int LA193_466 = input.LA(1);
						 
						int index193_466 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_466);
						if ( s>=0 ) return s;
						break;

					case 270 : 
						int LA193_55 = input.LA(1);
						 
						int index193_55 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_55);
						if ( s>=0 ) return s;
						break;

					case 271 : 
						int LA193_88 = input.LA(1);
						 
						int index193_88 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_88);
						if ( s>=0 ) return s;
						break;

					case 272 : 
						int LA193_123 = input.LA(1);
						 
						int index193_123 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_123);
						if ( s>=0 ) return s;
						break;

					case 273 : 
						int LA193_141 = input.LA(1);
						 
						int index193_141 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_141);
						if ( s>=0 ) return s;
						break;

					case 274 : 
						int LA193_767 = input.LA(1);
						 
						int index193_767 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_767);
						if ( s>=0 ) return s;
						break;

					case 275 : 
						int LA193_58 = input.LA(1);
						 
						int index193_58 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_58);
						if ( s>=0 ) return s;
						break;

					case 276 : 
						int LA193_91 = input.LA(1);
						 
						int index193_91 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_91);
						if ( s>=0 ) return s;
						break;

					case 277 : 
						int LA193_124 = input.LA(1);
						 
						int index193_124 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_124);
						if ( s>=0 ) return s;
						break;

					case 278 : 
						int LA193_142 = input.LA(1);
						 
						int index193_142 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_142);
						if ( s>=0 ) return s;
						break;

					case 279 : 
						int LA193_770 = input.LA(1);
						 
						int index193_770 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_770);
						if ( s>=0 ) return s;
						break;

					case 280 : 
						int LA193_686 = input.LA(1);
						 
						int index193_686 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_686);
						if ( s>=0 ) return s;
						break;

					case 281 : 
						int LA193_222 = input.LA(1);
						 
						int index193_222 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_222);
						if ( s>=0 ) return s;
						break;

					case 282 : 
						int LA193_223 = input.LA(1);
						 
						int index193_223 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_223);
						if ( s>=0 ) return s;
						break;

					case 283 : 
						int LA193_722 = input.LA(1);
						 
						int index193_722 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_722);
						if ( s>=0 ) return s;
						break;

					case 284 : 
						int LA193_723 = input.LA(1);
						 
						int index193_723 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_723);
						if ( s>=0 ) return s;
						break;

					case 285 : 
						int LA193_426 = input.LA(1);
						 
						int index193_426 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_426);
						if ( s>=0 ) return s;
						break;

					case 286 : 
						int LA193_627 = input.LA(1);
						 
						int index193_627 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_627);
						if ( s>=0 ) return s;
						break;

					case 287 : 
						int LA193_314 = input.LA(1);
						 
						int index193_314 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_314);
						if ( s>=0 ) return s;
						break;

					case 288 : 
						int LA193_689 = input.LA(1);
						 
						int index193_689 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_689);
						if ( s>=0 ) return s;
						break;

					case 289 : 
						int LA193_315 = input.LA(1);
						 
						int index193_315 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_315);
						if ( s>=0 ) return s;
						break;

					case 290 : 
						int LA193_793 = input.LA(1);
						 
						int index193_793 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_793);
						if ( s>=0 ) return s;
						break;

					case 291 : 
						int LA193_787 = input.LA(1);
						 
						int index193_787 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_787);
						if ( s>=0 ) return s;
						break;

					case 292 : 
						int LA193_179 = input.LA(1);
						 
						int index193_179 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_179);
						if ( s>=0 ) return s;
						break;

					case 293 : 
						int LA193_180 = input.LA(1);
						 
						int index193_180 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_180);
						if ( s>=0 ) return s;
						break;

					case 294 : 
						int LA193_469 = input.LA(1);
						 
						int index193_469 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_469);
						if ( s>=0 ) return s;
						break;

					case 295 : 
						int LA193_103 = input.LA(1);
						s = -1;
						if ( (LA193_103=='i') ) {s = 222;}
						else if ( (LA193_103=='I') ) {s = 223;}
						else if ( ((LA193_103 >= '\u0000' && LA193_103 <= '\t')||LA193_103=='\u000B'||(LA193_103 >= '\u000E' && LA193_103 <= '/')||(LA193_103 >= '1' && LA193_103 <= '3')||LA193_103=='5'||(LA193_103 >= '7' && LA193_103 <= 'H')||(LA193_103 >= 'J' && LA193_103 <= 'h')||(LA193_103 >= 'j' && LA193_103 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA193_103=='0') ) {s = 224;}
						else if ( (LA193_103=='4'||LA193_103=='6') ) {s = 225;}
						if ( s>=0 ) return s;
						break;

					case 296 : 
						int LA193_725 = input.LA(1);
						 
						int index193_725 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_725);
						if ( s>=0 ) return s;
						break;

					case 297 : 
						int LA193_827 = input.LA(1);
						 
						int index193_827 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_827);
						if ( s>=0 ) return s;
						break;

					case 298 : 
						int LA193_113 = input.LA(1);
						s = -1;
						if ( (LA193_113=='g') ) {s = 241;}
						else if ( (LA193_113=='G') ) {s = 242;}
						else if ( ((LA193_113 >= '\u0000' && LA193_113 <= '\t')||LA193_113=='\u000B'||(LA193_113 >= '\u000E' && LA193_113 <= '/')||(LA193_113 >= '1' && LA193_113 <= '3')||LA193_113=='5'||(LA193_113 >= '7' && LA193_113 <= 'F')||(LA193_113 >= 'H' && LA193_113 <= 'f')||(LA193_113 >= 'h' && LA193_113 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA193_113=='0') ) {s = 243;}
						else if ( (LA193_113=='4'||LA193_113=='6') ) {s = 244;}
						if ( s>=0 ) return s;
						break;

					case 299 : 
						int LA193_475 = input.LA(1);
						 
						int index193_475 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_475);
						if ( s>=0 ) return s;
						break;

					case 300 : 
						int LA193_476 = input.LA(1);
						 
						int index193_476 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_476);
						if ( s>=0 ) return s;
						break;

					case 301 : 
						int LA193_2 = input.LA(1);
						s = -1;
						if ( (LA193_2=='p') ) {s = 30;}
						else if ( (LA193_2=='0') ) {s = 31;}
						else if ( (LA193_2=='4'||LA193_2=='6') ) {s = 32;}
						else if ( (LA193_2=='P') ) {s = 33;}
						else if ( (LA193_2=='m') ) {s = 34;}
						else if ( (LA193_2=='5'||LA193_2=='7') ) {s = 35;}
						else if ( (LA193_2=='M') ) {s = 36;}
						else if ( (LA193_2=='i') ) {s = 37;}
						else if ( (LA193_2=='I') ) {s = 38;}
						else if ( (LA193_2=='r') ) {s = 39;}
						else if ( (LA193_2=='R') ) {s = 40;}
						else if ( (LA193_2=='s') ) {s = 41;}
						else if ( (LA193_2=='S') ) {s = 42;}
						else if ( (LA193_2=='k') ) {s = 43;}
						else if ( (LA193_2=='K') ) {s = 44;}
						else if ( (LA193_2=='h') ) {s = 45;}
						else if ( (LA193_2=='H') ) {s = 46;}
						else if ( ((LA193_2 >= '\u0000' && LA193_2 <= '\t')||LA193_2=='\u000B'||(LA193_2 >= '\u000E' && LA193_2 <= '/')||(LA193_2 >= '1' && LA193_2 <= '3')||(LA193_2 >= '8' && LA193_2 <= 'G')||LA193_2=='J'||LA193_2=='L'||(LA193_2 >= 'N' && LA193_2 <= 'O')||LA193_2=='Q'||(LA193_2 >= 'T' && LA193_2 <= 'g')||LA193_2=='j'||LA193_2=='l'||(LA193_2 >= 'n' && LA193_2 <= 'o')||LA193_2=='q'||(LA193_2 >= 't' && LA193_2 <= '\uFFFF')) ) {s = 12;}
						if ( s>=0 ) return s;
						break;

					case 302 : 
						int LA193_59 = input.LA(1);
						 
						int index193_59 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_59);
						if ( s>=0 ) return s;
						break;

					case 303 : 
						int LA193_92 = input.LA(1);
						 
						int index193_92 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_92);
						if ( s>=0 ) return s;
						break;

					case 304 : 
						int LA193_266 = input.LA(1);
						 
						int index193_266 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_266);
						if ( s>=0 ) return s;
						break;

					case 305 : 
						int LA193_381 = input.LA(1);
						 
						int index193_381 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_381);
						if ( s>=0 ) return s;
						break;

					case 306 : 
						int LA193_431 = input.LA(1);
						 
						int index193_431 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_431);
						if ( s>=0 ) return s;
						break;

					case 307 : 
						int LA193_515 = input.LA(1);
						 
						int index193_515 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_515);
						if ( s>=0 ) return s;
						break;

					case 308 : 
						int LA193_646 = input.LA(1);
						 
						int index193_646 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_646);
						if ( s>=0 ) return s;
						break;

					case 309 : 
						int LA193_751 = input.LA(1);
						 
						int index193_751 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_751);
						if ( s>=0 ) return s;
						break;

					case 310 : 
						int LA193_61 = input.LA(1);
						 
						int index193_61 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_61);
						if ( s>=0 ) return s;
						break;

					case 311 : 
						int LA193_93 = input.LA(1);
						 
						int index193_93 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_93);
						if ( s>=0 ) return s;
						break;

					case 312 : 
						int LA193_752 = input.LA(1);
						 
						int index193_752 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_752);
						if ( s>=0 ) return s;
						break;

					case 313 : 
						int LA193_196 = input.LA(1);
						 
						int index193_196 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_196);
						if ( s>=0 ) return s;
						break;

					case 314 : 
						int LA193_204 = input.LA(1);
						 
						int index193_204 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_204);
						if ( s>=0 ) return s;
						break;

					case 315 : 
						int LA193_267 = input.LA(1);
						 
						int index193_267 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_267);
						if ( s>=0 ) return s;
						break;

					case 316 : 
						int LA193_775 = input.LA(1);
						 
						int index193_775 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_775);
						if ( s>=0 ) return s;
						break;

					case 317 : 
						int LA193_829 = input.LA(1);
						 
						int index193_829 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_829);
						if ( s>=0 ) return s;
						break;

					case 318 : 
						int LA193_779 = input.LA(1);
						 
						int index193_779 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_779);
						if ( s>=0 ) return s;
						break;

					case 319 : 
						int LA193_382 = input.LA(1);
						 
						int index193_382 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_382);
						if ( s>=0 ) return s;
						break;

					case 320 : 
						int LA193_432 = input.LA(1);
						 
						int index193_432 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_432);
						if ( s>=0 ) return s;
						break;

					case 321 : 
						int LA193_516 = input.LA(1);
						 
						int index193_516 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_516);
						if ( s>=0 ) return s;
						break;

					case 322 : 
						int LA193_647 = input.LA(1);
						 
						int index193_647 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_647);
						if ( s>=0 ) return s;
						break;

					case 323 : 
						int LA193_198 = input.LA(1);
						 
						int index193_198 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_198);
						if ( s>=0 ) return s;
						break;

					case 324 : 
						int LA193_205 = input.LA(1);
						 
						int index193_205 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_205);
						if ( s>=0 ) return s;
						break;

					case 325 : 
						int LA193_776 = input.LA(1);
						 
						int index193_776 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_776);
						if ( s>=0 ) return s;
						break;

					case 326 : 
						int LA193_780 = input.LA(1);
						 
						int index193_780 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_780);
						if ( s>=0 ) return s;
						break;

					case 327 : 
						int LA193_830 = input.LA(1);
						 
						int index193_830 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_830);
						if ( s>=0 ) return s;
						break;

					case 328 : 
						int LA193_293 = input.LA(1);
						 
						int index193_293 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_293);
						if ( s>=0 ) return s;
						break;

					case 329 : 
						int LA193_294 = input.LA(1);
						 
						int index193_294 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_294);
						if ( s>=0 ) return s;
						break;

					case 330 : 
						int LA193_693 = input.LA(1);
						 
						int index193_693 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_693);
						if ( s>=0 ) return s;
						break;

					case 331 : 
						int LA193_694 = input.LA(1);
						 
						int index193_694 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_694);
						if ( s>=0 ) return s;
						break;

					case 332 : 
						int LA193_192 = input.LA(1);
						 
						int index193_192 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_192);
						if ( s>=0 ) return s;
						break;

					case 333 : 
						int LA193_233 = input.LA(1);
						 
						int index193_233 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_233);
						if ( s>=0 ) return s;
						break;

					case 334 : 
						int LA193_102 = input.LA(1);
						 
						int index193_102 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_102);
						if ( s>=0 ) return s;
						break;

					case 335 : 
						int LA193_115 = input.LA(1);
						 
						int index193_115 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_115);
						if ( s>=0 ) return s;
						break;

					case 336 : 
						int LA193_231 = input.LA(1);
						 
						int index193_231 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_231);
						if ( s>=0 ) return s;
						break;

					case 337 : 
						int LA193_351 = input.LA(1);
						 
						int index193_351 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_351);
						if ( s>=0 ) return s;
						break;

					case 338 : 
						int LA193_482 = input.LA(1);
						 
						int index193_482 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_482);
						if ( s>=0 ) return s;
						break;

					case 339 : 
						int LA193_615 = input.LA(1);
						 
						int index193_615 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_615);
						if ( s>=0 ) return s;
						break;

					case 340 : 
						int LA193_735 = input.LA(1);
						 
						int index193_735 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_735);
						if ( s>=0 ) return s;
						break;

					case 341 : 
						int LA193_819 = input.LA(1);
						 
						int index193_819 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_819);
						if ( s>=0 ) return s;
						break;

					case 342 : 
						int LA193_193 = input.LA(1);
						 
						int index193_193 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_193);
						if ( s>=0 ) return s;
						break;

					case 343 : 
						int LA193_353 = input.LA(1);
						 
						int index193_353 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_353);
						if ( s>=0 ) return s;
						break;

					case 344 : 
						int LA193_105 = input.LA(1);
						 
						int index193_105 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_105);
						if ( s>=0 ) return s;
						break;

					case 345 : 
						int LA193_116 = input.LA(1);
						 
						int index193_116 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_116);
						if ( s>=0 ) return s;
						break;

					case 346 : 
						int LA193_232 = input.LA(1);
						 
						int index193_232 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_232);
						if ( s>=0 ) return s;
						break;

					case 347 : 
						int LA193_234 = input.LA(1);
						 
						int index193_234 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_234);
						if ( s>=0 ) return s;
						break;

					case 348 : 
						int LA193_484 = input.LA(1);
						 
						int index193_484 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_484);
						if ( s>=0 ) return s;
						break;

					case 349 : 
						int LA193_617 = input.LA(1);
						 
						int index193_617 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_617);
						if ( s>=0 ) return s;
						break;

					case 350 : 
						int LA193_737 = input.LA(1);
						 
						int index193_737 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_737);
						if ( s>=0 ) return s;
						break;

					case 351 : 
						int LA193_820 = input.LA(1);
						 
						int index193_820 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_820);
						if ( s>=0 ) return s;
						break;

					case 352 : 
						int LA193_556 = input.LA(1);
						 
						int index193_556 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_556);
						if ( s>=0 ) return s;
						break;

					case 353 : 
						int LA193_599 = input.LA(1);
						 
						int index193_599 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_599);
						if ( s>=0 ) return s;
						break;

					case 354 : 
						int LA193_600 = input.LA(1);
						 
						int index193_600 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_600);
						if ( s>=0 ) return s;
						break;

					case 355 : 
						int LA193_521 = input.LA(1);
						 
						int index193_521 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_521);
						if ( s>=0 ) return s;
						break;

					case 356 : 
						int LA193_67 = input.LA(1);
						 
						int index193_67 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_67);
						if ( s>=0 ) return s;
						break;

					case 357 : 
						int LA193_98 = input.LA(1);
						 
						int index193_98 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_98);
						if ( s>=0 ) return s;
						break;

					case 358 : 
						int LA193_154 = input.LA(1);
						 
						int index193_154 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_154);
						if ( s>=0 ) return s;
						break;

					case 359 : 
						int LA193_156 = input.LA(1);
						 
						int index193_156 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_156);
						if ( s>=0 ) return s;
						break;

					case 360 : 
						int LA193_272 = input.LA(1);
						 
						int index193_272 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_272);
						if ( s>=0 ) return s;
						break;

					case 361 : 
						int LA193_387 = input.LA(1);
						 
						int index193_387 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_387);
						if ( s>=0 ) return s;
						break;

					case 362 : 
						int LA193_652 = input.LA(1);
						 
						int index193_652 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_652);
						if ( s>=0 ) return s;
						break;

					case 363 : 
						int LA193_757 = input.LA(1);
						 
						int index193_757 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_757);
						if ( s>=0 ) return s;
						break;

					case 364 : 
						int LA193_155 = input.LA(1);
						 
						int index193_155 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_155);
						if ( s>=0 ) return s;
						break;

					case 365 : 
						int LA193_69 = input.LA(1);
						 
						int index193_69 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_69);
						if ( s>=0 ) return s;
						break;

					case 366 : 
						int LA193_99 = input.LA(1);
						 
						int index193_99 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_99);
						if ( s>=0 ) return s;
						break;

					case 367 : 
						int LA193_157 = input.LA(1);
						 
						int index193_157 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_157);
						if ( s>=0 ) return s;
						break;

					case 368 : 
						int LA193_273 = input.LA(1);
						 
						int index193_273 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_273);
						if ( s>=0 ) return s;
						break;

					case 369 : 
						int LA193_388 = input.LA(1);
						 
						int index193_388 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_388);
						if ( s>=0 ) return s;
						break;

					case 370 : 
						int LA193_522 = input.LA(1);
						 
						int index193_522 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_522);
						if ( s>=0 ) return s;
						break;

					case 371 : 
						int LA193_653 = input.LA(1);
						 
						int index193_653 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_653);
						if ( s>=0 ) return s;
						break;

					case 372 : 
						int LA193_758 = input.LA(1);
						 
						int index193_758 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_758);
						if ( s>=0 ) return s;
						break;

					case 373 : 
						int LA193_560 = input.LA(1);
						 
						int index193_560 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_560);
						if ( s>=0 ) return s;
						break;

					case 374 : 
						int LA193_9 = input.LA(1);
						 
						int index193_9 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_9);
						if ( s>=0 ) return s;
						break;

					case 375 : 
						int LA193_20 = input.LA(1);
						 
						int index193_20 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_20);
						if ( s>=0 ) return s;
						break;

					case 376 : 
						int LA193_309 = input.LA(1);
						 
						int index193_309 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_309);
						if ( s>=0 ) return s;
						break;

					case 377 : 
						int LA193_310 = input.LA(1);
						 
						int index193_310 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_310);
						if ( s>=0 ) return s;
						break;

					case 378 : 
						int LA193_227 = input.LA(1);
						s = -1;
						if ( (LA193_227=='m') ) {s = 342;}
						else if ( (LA193_227=='M') ) {s = 343;}
						else if ( ((LA193_227 >= '\u0000' && LA193_227 <= '\t')||LA193_227=='\u000B'||(LA193_227 >= '\u000E' && LA193_227 <= '/')||(LA193_227 >= '1' && LA193_227 <= '3')||LA193_227=='5'||(LA193_227 >= '7' && LA193_227 <= 'L')||(LA193_227 >= 'N' && LA193_227 <= 'l')||(LA193_227 >= 'n' && LA193_227 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA193_227=='0') ) {s = 344;}
						else if ( (LA193_227=='4'||LA193_227=='6') ) {s = 345;}
						if ( s>=0 ) return s;
						break;

					case 379 : 
						int LA193_441 = input.LA(1);
						 
						int index193_441 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_441);
						if ( s>=0 ) return s;
						break;

					case 380 : 
						int LA193_442 = input.LA(1);
						 
						int index193_442 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_442);
						if ( s>=0 ) return s;
						break;

					case 381 : 
						int LA193_60 = input.LA(1);
						s = -1;
						if ( (LA193_60=='m') ) {s = 179;}
						else if ( (LA193_60=='M') ) {s = 180;}
						else if ( ((LA193_60 >= '\u0000' && LA193_60 <= '\t')||LA193_60=='\u000B'||(LA193_60 >= '\u000E' && LA193_60 <= '/')||(LA193_60 >= '1' && LA193_60 <= '3')||LA193_60=='5'||(LA193_60 >= '7' && LA193_60 <= 'L')||(LA193_60 >= 'N' && LA193_60 <= 'l')||(LA193_60 >= 'n' && LA193_60 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA193_60=='0') ) {s = 181;}
						else if ( (LA193_60=='4'||LA193_60=='6') ) {s = 182;}
						if ( s>=0 ) return s;
						break;

					case 382 : 
						int LA193_701 = input.LA(1);
						 
						int index193_701 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_701);
						if ( s>=0 ) return s;
						break;

					case 383 : 
						int LA193_702 = input.LA(1);
						 
						int index193_702 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_702);
						if ( s>=0 ) return s;
						break;

					case 384 : 
						int LA193_342 = input.LA(1);
						 
						int index193_342 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_342);
						if ( s>=0 ) return s;
						break;

					case 385 : 
						int LA193_343 = input.LA(1);
						 
						int index193_343 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_343);
						if ( s>=0 ) return s;
						break;

					case 386 : 
						int LA193_202 = input.LA(1);
						s = -1;
						if ( (LA193_202=='m') ) {s = 323;}
						else if ( (LA193_202=='M') ) {s = 324;}
						else if ( ((LA193_202 >= '\u0000' && LA193_202 <= '\t')||LA193_202=='\u000B'||(LA193_202 >= '\u000E' && LA193_202 <= '/')||(LA193_202 >= '1' && LA193_202 <= '3')||LA193_202=='5'||(LA193_202 >= '7' && LA193_202 <= 'L')||(LA193_202 >= 'N' && LA193_202 <= 'l')||(LA193_202 >= 'n' && LA193_202 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA193_202=='0') ) {s = 325;}
						else if ( (LA193_202=='4'||LA193_202=='6') ) {s = 326;}
						if ( s>=0 ) return s;
						break;

					case 387 : 
						int LA193_41 = input.LA(1);
						 
						int index193_41 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_41);
						if ( s>=0 ) return s;
						break;

					case 388 : 
						int LA193_42 = input.LA(1);
						 
						int index193_42 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_42);
						if ( s>=0 ) return s;
						break;

					case 389 : 
						int LA193_149 = input.LA(1);
						 
						int index193_149 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_149);
						if ( s>=0 ) return s;
						break;

					case 390 : 
						int LA193_163 = input.LA(1);
						 
						int index193_163 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_163);
						if ( s>=0 ) return s;
						break;

					case 391 : 
						int LA193_164 = input.LA(1);
						 
						int index193_164 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_164);
						if ( s>=0 ) return s;
						break;

					case 392 : 
						int LA193_603 = input.LA(1);
						 
						int index193_603 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_603);
						if ( s>=0 ) return s;
						break;

					case 393 : 
						int LA193_785 = input.LA(1);
						 
						int index193_785 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_785);
						if ( s>=0 ) return s;
						break;

					case 394 : 
						int LA193_810 = input.LA(1);
						 
						int index193_810 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_810);
						if ( s>=0 ) return s;
						break;

					case 395 : 
						int LA193_811 = input.LA(1);
						 
						int index193_811 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred12_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_811);
						if ( s>=0 ) return s;
						break;

					case 396 : 
						int LA193_456 = input.LA(1);
						 
						int index193_456 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_456);
						if ( s>=0 ) return s;
						break;

					case 397 : 
						int LA193_457 = input.LA(1);
						 
						int index193_457 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_457);
						if ( s>=0 ) return s;
						break;

					case 398 : 
						int LA193_786 = input.LA(1);
						 
						int index193_786 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_786);
						if ( s>=0 ) return s;
						break;

					case 399 : 
						int LA193_301 = input.LA(1);
						 
						int index193_301 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_301);
						if ( s>=0 ) return s;
						break;

					case 400 : 
						int LA193_609 = input.LA(1);
						 
						int index193_609 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_609);
						if ( s>=0 ) return s;
						break;

					case 401 : 
						int LA193_610 = input.LA(1);
						 
						int index193_610 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_610);
						if ( s>=0 ) return s;
						break;

					case 402 : 
						int LA193_488 = input.LA(1);
						 
						int index193_488 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_488);
						if ( s>=0 ) return s;
						break;

					case 403 : 
						int LA193_226 = input.LA(1);
						 
						int index193_226 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_226);
						if ( s>=0 ) return s;
						break;

					case 404 : 
						int LA193_229 = input.LA(1);
						 
						int index193_229 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_229);
						if ( s>=0 ) return s;
						break;

					case 405 : 
						int LA193_814 = input.LA(1);
						 
						int index193_814 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_814);
						if ( s>=0 ) return s;
						break;

					case 406 : 
						int LA193_823 = input.LA(1);
						 
						int index193_823 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_823);
						if ( s>=0 ) return s;
						break;

					case 407 : 
						int LA193_471 = input.LA(1);
						 
						int index193_471 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_471);
						if ( s>=0 ) return s;
						break;

					case 408 : 
						int LA193_490 = input.LA(1);
						 
						int index193_490 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_490);
						if ( s>=0 ) return s;
						break;

					case 409 : 
						int LA193_605 = input.LA(1);
						 
						int index193_605 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_605);
						if ( s>=0 ) return s;
						break;

					case 410 : 
						int LA193_621 = input.LA(1);
						 
						int index193_621 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_621);
						if ( s>=0 ) return s;
						break;

					case 411 : 
						int LA193_741 = input.LA(1);
						 
						int index193_741 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_741);
						if ( s>=0 ) return s;
						break;

					case 412 : 
						int LA193_825 = input.LA(1);
						 
						int index193_825 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_825);
						if ( s>=0 ) return s;
						break;

					case 413 : 
						int LA193_743 = input.LA(1);
						 
						int index193_743 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_743);
						if ( s>=0 ) return s;
						break;

					case 414 : 
						int LA193_623 = input.LA(1);
						 
						int index193_623 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_623);
						if ( s>=0 ) return s;
						break;

					case 415 : 
						int LA193_837 = input.LA(1);
						 
						int index193_837 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_837);
						if ( s>=0 ) return s;
						break;

					case 416 : 
						int LA193_727 = input.LA(1);
						 
						int index193_727 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_727);
						if ( s>=0 ) return s;
						break;

					case 417 : 
						int LA193_77 = input.LA(1);
						s = -1;
						if ( (LA193_77=='h') ) {s = 210;}
						else if ( (LA193_77=='H') ) {s = 211;}
						else if ( ((LA193_77 >= '\u0000' && LA193_77 <= '\t')||LA193_77=='\u000B'||(LA193_77 >= '\u000E' && LA193_77 <= '/')||(LA193_77 >= '1' && LA193_77 <= '3')||LA193_77=='5'||(LA193_77 >= '7' && LA193_77 <= 'G')||(LA193_77 >= 'I' && LA193_77 <= 'g')||(LA193_77 >= 'i' && LA193_77 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA193_77=='0') ) {s = 212;}
						else if ( (LA193_77=='4'||LA193_77=='6') ) {s = 213;}
						if ( s>=0 ) return s;
						break;

					case 418 : 
						int LA193_489 = input.LA(1);
						 
						int index193_489 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_489);
						if ( s>=0 ) return s;
						break;

					case 419 : 
						int LA193_228 = input.LA(1);
						 
						int index193_228 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_228);
						if ( s>=0 ) return s;
						break;

					case 420 : 
						int LA193_230 = input.LA(1);
						 
						int index193_230 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_230);
						if ( s>=0 ) return s;
						break;

					case 421 : 
						int LA193_826 = input.LA(1);
						 
						int index193_826 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_826);
						if ( s>=0 ) return s;
						break;

					case 422 : 
						int LA193_838 = input.LA(1);
						 
						int index193_838 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_838);
						if ( s>=0 ) return s;
						break;

					case 423 : 
						int LA193_824 = input.LA(1);
						 
						int index193_824 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_824);
						if ( s>=0 ) return s;
						break;

					case 424 : 
						int LA193_472 = input.LA(1);
						 
						int index193_472 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_472);
						if ( s>=0 ) return s;
						break;

					case 425 : 
						int LA193_815 = input.LA(1);
						 
						int index193_815 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_815);
						if ( s>=0 ) return s;
						break;

					case 426 : 
						int LA193_491 = input.LA(1);
						 
						int index193_491 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_491);
						if ( s>=0 ) return s;
						break;

					case 427 : 
						int LA193_606 = input.LA(1);
						 
						int index193_606 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_606);
						if ( s>=0 ) return s;
						break;

					case 428 : 
						int LA193_742 = input.LA(1);
						 
						int index193_742 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_742);
						if ( s>=0 ) return s;
						break;

					case 429 : 
						int LA193_622 = input.LA(1);
						 
						int index193_622 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_622);
						if ( s>=0 ) return s;
						break;

					case 430 : 
						int LA193_624 = input.LA(1);
						 
						int index193_624 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_624);
						if ( s>=0 ) return s;
						break;

					case 431 : 
						int LA193_728 = input.LA(1);
						 
						int index193_728 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_728);
						if ( s>=0 ) return s;
						break;

					case 432 : 
						int LA193_744 = input.LA(1);
						 
						int index193_744 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_744);
						if ( s>=0 ) return s;
						break;

					case 433 : 
						int LA193_565 = input.LA(1);
						 
						int index193_565 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_565);
						if ( s>=0 ) return s;
						break;

					case 434 : 
						int LA193_566 = input.LA(1);
						 
						int index193_566 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_566);
						if ( s>=0 ) return s;
						break;

					case 435 : 
						int LA193_817 = input.LA(1);
						 
						int index193_817 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_817);
						if ( s>=0 ) return s;
						break;

					case 436 : 
						int LA193_818 = input.LA(1);
						 
						int index193_818 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_818);
						if ( s>=0 ) return s;
						break;

					case 437 : 
						int LA193_420 = input.LA(1);
						 
						int index193_420 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_420);
						if ( s>=0 ) return s;
						break;

					case 438 : 
						int LA193_47 = input.LA(1);
						 
						int index193_47 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_47);
						if ( s>=0 ) return s;
						break;

					case 439 : 
						int LA193_82 = input.LA(1);
						 
						int index193_82 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_82);
						if ( s>=0 ) return s;
						break;

					case 440 : 
						int LA193_262 = input.LA(1);
						 
						int index193_262 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_262);
						if ( s>=0 ) return s;
						break;

					case 441 : 
						int LA193_377 = input.LA(1);
						 
						int index193_377 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_377);
						if ( s>=0 ) return s;
						break;

					case 442 : 
						int LA193_421 = input.LA(1);
						 
						int index193_421 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_421);
						if ( s>=0 ) return s;
						break;

					case 443 : 
						int LA193_511 = input.LA(1);
						 
						int index193_511 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_511);
						if ( s>=0 ) return s;
						break;

					case 444 : 
						int LA193_642 = input.LA(1);
						 
						int index193_642 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_642);
						if ( s>=0 ) return s;
						break;

					case 445 : 
						int LA193_747 = input.LA(1);
						 
						int index193_747 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_747);
						if ( s>=0 ) return s;
						break;

					case 446 : 
						int LA193_50 = input.LA(1);
						 
						int index193_50 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_50);
						if ( s>=0 ) return s;
						break;

					case 447 : 
						int LA193_84 = input.LA(1);
						 
						int index193_84 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_84);
						if ( s>=0 ) return s;
						break;

					case 448 : 
						int LA193_184 = input.LA(1);
						 
						int index193_184 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_184);
						if ( s>=0 ) return s;
						break;

					case 449 : 
						int LA193_264 = input.LA(1);
						 
						int index193_264 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_264);
						if ( s>=0 ) return s;
						break;

					case 450 : 
						int LA193_379 = input.LA(1);
						 
						int index193_379 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_379);
						if ( s>=0 ) return s;
						break;

					case 451 : 
						int LA193_513 = input.LA(1);
						 
						int index193_513 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_513);
						if ( s>=0 ) return s;
						break;

					case 452 : 
						int LA193_644 = input.LA(1);
						 
						int index193_644 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_644);
						if ( s>=0 ) return s;
						break;

					case 453 : 
						int LA193_749 = input.LA(1);
						 
						int index193_749 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_749);
						if ( s>=0 ) return s;
						break;

					case 454 : 
						int LA193_185 = input.LA(1);
						 
						int index193_185 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_185);
						if ( s>=0 ) return s;
						break;

					case 455 : 
						int LA193_684 = input.LA(1);
						 
						int index193_684 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_684);
						if ( s>=0 ) return s;
						break;

					case 456 : 
						int LA193_685 = input.LA(1);
						 
						int index193_685 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_685);
						if ( s>=0 ) return s;
						break;

					case 457 : 
						int LA193_812 = input.LA(1);
						 
						int index193_812 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_812);
						if ( s>=0 ) return s;
						break;

					case 458 : 
						int LA193_282 = input.LA(1);
						 
						int index193_282 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_282);
						if ( s>=0 ) return s;
						break;

					case 459 : 
						int LA193_285 = input.LA(1);
						 
						int index193_285 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_285);
						if ( s>=0 ) return s;
						break;

					case 460 : 
						int LA193_531 = input.LA(1);
						 
						int index193_531 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_531);
						if ( s>=0 ) return s;
						break;

					case 461 : 
						int LA193_534 = input.LA(1);
						 
						int index193_534 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_534);
						if ( s>=0 ) return s;
						break;

					case 462 : 
						int LA193_268 = input.LA(1);
						 
						int index193_268 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_268);
						if ( s>=0 ) return s;
						break;

					case 463 : 
						int LA193_62 = input.LA(1);
						 
						int index193_62 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_62);
						if ( s>=0 ) return s;
						break;

					case 464 : 
						int LA193_94 = input.LA(1);
						 
						int index193_94 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_94);
						if ( s>=0 ) return s;
						break;

					case 465 : 
						int LA193_761 = input.LA(1);
						 
						int index193_761 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_761);
						if ( s>=0 ) return s;
						break;

					case 466 : 
						int LA193_143 = input.LA(1);
						 
						int index193_143 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_143);
						if ( s>=0 ) return s;
						break;

					case 467 : 
						int LA193_648 = input.LA(1);
						 
						int index193_648 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_648);
						if ( s>=0 ) return s;
						break;

					case 468 : 
						int LA193_150 = input.LA(1);
						 
						int index193_150 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_150);
						if ( s>=0 ) return s;
						break;

					case 469 : 
						int LA193_276 = input.LA(1);
						 
						int index193_276 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_276);
						if ( s>=0 ) return s;
						break;

					case 470 : 
						int LA193_383 = input.LA(1);
						 
						int index193_383 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_383);
						if ( s>=0 ) return s;
						break;

					case 471 : 
						int LA193_525 = input.LA(1);
						 
						int index193_525 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_525);
						if ( s>=0 ) return s;
						break;

					case 472 : 
						int LA193_391 = input.LA(1);
						 
						int index193_391 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_391);
						if ( s>=0 ) return s;
						break;

					case 473 : 
						int LA193_753 = input.LA(1);
						 
						int index193_753 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_753);
						if ( s>=0 ) return s;
						break;

					case 474 : 
						int LA193_517 = input.LA(1);
						 
						int index193_517 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_517);
						if ( s>=0 ) return s;
						break;

					case 475 : 
						int LA193_656 = input.LA(1);
						 
						int index193_656 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_656);
						if ( s>=0 ) return s;
						break;

					case 476 : 
						int LA193_436 = input.LA(1);
						 
						int index193_436 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_436);
						if ( s>=0 ) return s;
						break;

					case 477 : 
						int LA193_144 = input.LA(1);
						 
						int index193_144 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_144);
						if ( s>=0 ) return s;
						break;

					case 478 : 
						int LA193_65 = input.LA(1);
						 
						int index193_65 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_65);
						if ( s>=0 ) return s;
						break;

					case 479 : 
						int LA193_96 = input.LA(1);
						 
						int index193_96 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_96);
						if ( s>=0 ) return s;
						break;

					case 480 : 
						int LA193_151 = input.LA(1);
						 
						int index193_151 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_151);
						if ( s>=0 ) return s;
						break;

					case 481 : 
						int LA193_270 = input.LA(1);
						 
						int index193_270 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_270);
						if ( s>=0 ) return s;
						break;

					case 482 : 
						int LA193_278 = input.LA(1);
						 
						int index193_278 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_278);
						if ( s>=0 ) return s;
						break;

					case 483 : 
						int LA193_763 = input.LA(1);
						 
						int index193_763 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_763);
						if ( s>=0 ) return s;
						break;

					case 484 : 
						int LA193_385 = input.LA(1);
						 
						int index193_385 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_385);
						if ( s>=0 ) return s;
						break;

					case 485 : 
						int LA193_658 = input.LA(1);
						 
						int index193_658 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_658);
						if ( s>=0 ) return s;
						break;

					case 486 : 
						int LA193_393 = input.LA(1);
						 
						int index193_393 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_393);
						if ( s>=0 ) return s;
						break;

					case 487 : 
						int LA193_527 = input.LA(1);
						 
						int index193_527 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_527);
						if ( s>=0 ) return s;
						break;

					case 488 : 
						int LA193_650 = input.LA(1);
						 
						int index193_650 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_650);
						if ( s>=0 ) return s;
						break;

					case 489 : 
						int LA193_519 = input.LA(1);
						 
						int index193_519 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_519);
						if ( s>=0 ) return s;
						break;

					case 490 : 
						int LA193_755 = input.LA(1);
						 
						int index193_755 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_755);
						if ( s>=0 ) return s;
						break;

					case 491 : 
						int LA193_437 = input.LA(1);
						 
						int index193_437 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_437);
						if ( s>=0 ) return s;
						break;

					case 492 : 
						int LA193_48 = input.LA(1);
						s = -1;
						if ( (LA193_48=='m') ) {s = 163;}
						else if ( (LA193_48=='M') ) {s = 164;}
						else if ( (LA193_48=='x') ) {s = 165;}
						else if ( (LA193_48=='0') ) {s = 166;}
						else if ( (LA193_48=='4'||LA193_48=='6') ) {s = 167;}
						else if ( (LA193_48=='X') ) {s = 168;}
						else if ( ((LA193_48 >= '\u0000' && LA193_48 <= '\t')||LA193_48=='\u000B'||(LA193_48 >= '\u000E' && LA193_48 <= '/')||(LA193_48 >= '1' && LA193_48 <= '3')||(LA193_48 >= '8' && LA193_48 <= 'L')||(LA193_48 >= 'N' && LA193_48 <= 'W')||(LA193_48 >= 'Y' && LA193_48 <= 'l')||(LA193_48 >= 'n' && LA193_48 <= 'w')||(LA193_48 >= 'y' && LA193_48 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA193_48=='5'||LA193_48=='7') ) {s = 169;}
						if ( s>=0 ) return s;
						break;

					case 493 : 
						int LA193_323 = input.LA(1);
						 
						int index193_323 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_323);
						if ( s>=0 ) return s;
						break;

					case 494 : 
						int LA193_53 = input.LA(1);
						s = -1;
						if ( (LA193_53=='x') ) {s = 171;}
						else if ( (LA193_53=='X') ) {s = 172;}
						else if ( (LA193_53=='t') ) {s = 173;}
						else if ( (LA193_53=='0') ) {s = 174;}
						else if ( (LA193_53=='5'||LA193_53=='7') ) {s = 175;}
						else if ( (LA193_53=='T') ) {s = 176;}
						else if ( ((LA193_53 >= '\u0000' && LA193_53 <= '\t')||LA193_53=='\u000B'||(LA193_53 >= '\u000E' && LA193_53 <= '/')||(LA193_53 >= '1' && LA193_53 <= '3')||(LA193_53 >= '8' && LA193_53 <= 'S')||(LA193_53 >= 'U' && LA193_53 <= 'W')||(LA193_53 >= 'Y' && LA193_53 <= 's')||(LA193_53 >= 'u' && LA193_53 <= 'w')||(LA193_53 >= 'y' && LA193_53 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA193_53=='4'||LA193_53=='6') ) {s = 177;}
						if ( s>=0 ) return s;
						break;

					case 495 : 
						int LA193_324 = input.LA(1);
						 
						int index193_324 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_324);
						if ( s>=0 ) return s;
						break;

					case 496 : 
						int LA193_575 = input.LA(1);
						 
						int index193_575 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_575);
						if ( s>=0 ) return s;
						break;

					case 497 : 
						int LA193_789 = input.LA(1);
						 
						int index193_789 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_789);
						if ( s>=0 ) return s;
						break;

					case 498 : 
						int LA193_576 = input.LA(1);
						 
						int index193_576 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_576);
						if ( s>=0 ) return s;
						break;

					case 499 : 
						int LA193_790 = input.LA(1);
						 
						int index193_790 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_790);
						if ( s>=0 ) return s;
						break;

					case 500 : 
						int LA193_697 = input.LA(1);
						 
						int index193_697 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_697);
						if ( s>=0 ) return s;
						break;

					case 501 : 
						int LA193_698 = input.LA(1);
						 
						int index193_698 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_698);
						if ( s>=0 ) return s;
						break;

					case 502 : 
						int LA193_445 = input.LA(1);
						 
						int index193_445 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index193_445);
						if ( s>=0 ) return s;
						break;

					case 503 : 
						int LA193_68 = input.LA(1);
						s = -1;
						if ( (LA193_68=='n') ) {s = 192;}
						else if ( (LA193_68=='N') ) {s = 193;}
						else if ( ((LA193_68 >= '\u0000' && LA193_68 <= '\t')||LA193_68=='\u000B'||(LA193_68 >= '\u000E' && LA193_68 <= '/')||(LA193_68 >= '1' && LA193_68 <= '3')||LA193_68=='5'||(LA193_68 >= '7' && LA193_68 <= 'M')||(LA193_68 >= 'O' && LA193_68 <= 'm')||(LA193_68 >= 'o' && LA193_68 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA193_68=='0') ) {s = 194;}
						else if ( (LA193_68=='4'||LA193_68=='6') ) {s = 195;}
						if ( s>=0 ) return s;
						break;
			}
			if (state.backtracking>0) {state.failed=true; return -1;}
			NoViableAltException nvae =
				new NoViableAltException(getDescription(), 193, _s, input);
			error(nvae);
			throw nvae;
		}
	}

	static final String DFA212_eotS =
		"\1\uffff\1\72\1\76\1\100\1\102\1\104\2\uffff\1\110\1\112\4\uffff\1\114"+
		"\1\uffff\1\116\1\121\4\uffff\1\123\1\124\1\132\3\uffff\1\35\1\uffff\2"+
		"\35\1\uffff\1\146\1\uffff\2\35\3\uffff\22\73\42\uffff\3\35\2\uffff\5\35"+
		"\2\uffff\2\35\1\uffff\3\73\1\u009c\32\73\2\uffff\1\u00b9\2\35\1\uffff"+
		"\12\35\3\73\1\uffff\10\73\1\u00d4\21\73\1\u00e6\1\73\2\uffff\15\35\3\73"+
		"\1\u00f8\10\73\1\uffff\14\73\1\u0111\1\u0112\1\73\1\u0114\1\73\1\uffff"+
		"\1\73\15\35\1\uffff\2\73\1\uffff\1\u0125\1\u0126\11\73\1\u0130\12\73\1"+
		"\u013d\1\73\2\uffff\1\u013f\1\uffff\1\u0140\1\73\14\35\1\u014c\1\73\2"+
		"\uffff\11\73\1\uffff\10\73\1\u0161\2\73\1\u0164\1\uffff\1\u0165\2\uffff"+
		"\1\73\10\35\3\uffff\1\u016c\1\73\1\u016e\2\73\1\u0171\1\73\1\u0173\14"+
		"\73\1\uffff\2\73\2\uffff\1\u0184\5\35\1\uffff\1\73\1\uffff\2\73\1\uffff"+
		"\1\73\1\uffff\1\u018c\1\u018d\1\u018e\2\73\1\u0192\12\73\1\uffff\3\35"+
		"\1\u019e\1\73\1\u01a0\1\u01a1\3\uffff\3\73\1\uffff\1\73\1\u01a7\3\73\1"+
		"\u01ab\4\73\1\35\1\uffff\1\73\2\uffff\3\73\1\u01b5\1\73\1\uffff\3\73\1"+
		"\uffff\4\73\1\uffff\1\73\1\u01bf\1\u01c0\1\73\1\uffff\1\73\1\u01c4\7\73"+
		"\2\uffff\3\73\1\uffff\1\73\1\u01d1\1\u01d2\1\u01d3\2\73\1\u01d6\3\73\1"+
		"\u01da\1\73\3\uffff\1\u01dc\1\73\1\uffff\3\73\1\uffff\1\73\1\uffff\1\73"+
		"\1\u01e3\4\73\1\uffff\1\u01e8\3\73\1\uffff\2\73\1\u01ee\1\u01ef\1\73\2"+
		"\uffff\1\u01f1\1\uffff";
	static final String DFA212_eofS =
		"\u01f2\uffff";
	static final String DFA212_minS =
		"\1\11\1\55\1\41\1\55\2\75\1\uffff\1\55\2\75\4\uffff\1\74\1\uffff\1\72"+
		"\1\52\4\uffff\1\56\1\55\1\11\3\uffff\1\117\1\uffff\2\53\1\0\1\55\1\uffff"+
		"\1\117\1\105\2\uffff\1\55\1\145\1\106\1\101\1\105\1\101\1\110\1\117\1"+
		"\125\1\101\2\117\1\105\1\115\1\101\1\105\1\101\1\123\1\124\5\uffff\1\76"+
		"\34\uffff\1\124\2\114\1\0\1\uffff\1\114\1\122\1\60\1\122\1\65\2\uffff"+
		"\1\115\1\107\1\uffff\1\163\1\120\1\103\1\55\1\107\1\104\1\130\1\115\1"+
		"\101\3\116\1\120\1\131\1\106\1\120\1\124\1\107\1\124\1\117\1\105\1\124"+
		"\1\122\1\123\1\103\1\102\1\122\1\111\1\105\1\55\2\uffff\1\55\2\50\1\0"+
		"\1\114\1\60\1\114\1\62\1\50\1\60\1\65\1\122\1\101\1\105\1\164\1\117\1"+
		"\114\1\uffff\1\105\2\111\1\105\1\122\1\116\2\124\1\55\1\103\1\120\1\105"+
		"\1\124\1\55\1\124\1\110\1\125\1\132\1\102\1\105\1\117\1\105\1\110\1\125"+
		"\1\116\1\114\1\55\1\122\2\uffff\1\50\1\60\1\50\1\103\1\60\1\62\1\114\1"+
		"\120\1\60\1\65\1\122\1\111\1\130\1\56\1\122\1\125\1\55\1\101\1\116\2\123"+
		"\1\124\1\101\1\55\1\101\1\uffff\1\124\1\117\1\122\1\55\1\103\1\117\1\124"+
		"\1\122\1\55\1\113\1\116\1\122\2\55\1\107\1\55\1\105\1\uffff\1\117\1\60"+
		"\1\103\2\50\1\60\1\62\1\114\1\122\2\65\1\122\1\116\1\120\1\uffff\1\124"+
		"\1\104\1\uffff\2\55\1\120\2\105\1\111\1\116\1\106\1\122\1\111\1\122\1"+
		"\55\1\102\2\105\1\111\1\115\1\55\1\116\1\104\1\111\1\104\1\55\1\106\2"+
		"\uffff\1\55\1\uffff\1\55\1\117\1\60\1\103\2\50\1\65\1\62\1\114\1\105\1"+
		"\65\1\122\2\50\1\55\1\105\2\uffff\1\101\1\124\1\122\1\116\1\124\1\101"+
		"\1\104\1\117\1\124\1\uffff\1\117\1\111\1\117\1\106\1\116\1\107\1\55\1"+
		"\102\1\55\1\117\1\124\1\55\1\uffff\1\55\2\uffff\1\124\1\64\1\103\2\50"+
		"\1\62\1\114\1\106\1\122\3\uffff\1\55\1\103\2\55\1\105\1\55\1\103\1\55"+
		"\1\116\1\123\1\120\1\104\3\124\1\110\1\103\1\117\1\111\1\117\1\uffff\1"+
		"\103\1\55\2\uffff\1\55\1\103\2\50\1\114\1\111\1\uffff\1\105\1\uffff\1"+
		"\123\1\122\1\uffff\1\105\1\uffff\3\55\1\104\1\124\1\55\1\105\1\124\2\105"+
		"\1\111\1\120\1\104\1\124\1\125\1\113\1\uffff\2\50\1\130\1\55\1\124\2\55"+
		"\3\uffff\1\114\1\117\1\103\1\uffff\1\122\1\55\1\106\1\116\1\107\1\55\1"+
		"\104\1\124\1\115\1\105\1\50\1\uffff\1\131\2\uffff\1\105\1\115\1\117\1"+
		"\55\1\103\1\uffff\2\124\1\110\1\uffff\1\114\1\117\1\105\1\131\1\uffff"+
		"\1\114\2\55\1\122\1\uffff\1\117\1\55\1\105\1\124\1\105\1\115\1\116\1\106"+
		"\1\105\2\uffff\1\116\1\122\1\103\1\uffff\1\122\3\55\1\124\1\122\1\55\1"+
		"\105\1\116\1\117\1\55\1\103\3\uffff\1\55\1\101\1\uffff\1\122\1\105\1\122"+
		"\1\uffff\1\117\1\uffff\1\115\1\55\1\122\1\116\1\122\1\105\1\uffff\1\55"+
		"\1\105\1\116\1\123\1\uffff\1\122\1\105\2\55\1\122\2\uffff\1\55\1\uffff";
	static final String DFA212_maxS =
		"\2\uffff\1\75\1\uffff\2\75\1\uffff\1\uffff\2\75\4\uffff\1\76\1\uffff\1"+
		"\72\1\57\4\uffff\1\71\1\uffff\1\117\3\uffff\1\117\1\uffff\2\162\2\uffff"+
		"\1\uffff\1\117\1\105\2\uffff\1\uffff\1\145\1\116\1\101\1\111\1\101\1\117"+
		"\2\125\1\105\2\117\1\111\1\127\1\130\1\105\1\110\1\123\1\124\5\uffff\1"+
		"\uffff\34\uffff\1\124\2\154\1\uffff\1\uffff\1\154\1\162\1\67\1\162\1\65"+
		"\2\uffff\1\115\1\107\1\uffff\1\163\1\120\1\103\1\uffff\1\107\1\104\1\130"+
		"\1\115\1\101\1\125\1\122\1\116\1\120\1\131\1\106\1\120\1\124\1\107\1\124"+
		"\1\117\1\105\1\124\1\122\1\123\1\103\1\102\1\122\1\111\1\105\1\55\2\uffff"+
		"\1\uffff\2\50\1\uffff\1\154\1\67\1\154\1\62\1\55\1\67\1\65\1\162\1\101"+
		"\1\105\1\164\1\117\1\114\1\uffff\1\105\2\111\1\105\1\122\1\116\2\124\1"+
		"\uffff\1\103\1\120\1\105\1\124\1\55\1\124\1\110\1\125\1\132\1\102\1\105"+
		"\1\117\1\105\1\110\1\125\1\116\1\114\1\uffff\1\122\2\uffff\1\50\1\66\1"+
		"\50\1\143\1\67\1\62\1\154\1\120\1\67\1\65\1\162\1\111\1\130\1\56\1\122"+
		"\1\125\1\uffff\1\101\1\116\2\123\1\124\1\105\1\55\1\101\1\uffff\1\124"+
		"\1\117\1\122\1\55\1\122\1\117\1\124\1\122\1\55\1\113\1\116\1\122\2\uffff"+
		"\1\107\1\uffff\1\105\1\uffff\1\117\1\66\1\143\2\50\1\67\1\62\1\154\1\122"+
		"\1\67\1\65\1\162\1\116\1\120\1\uffff\1\124\1\104\1\uffff\2\uffff\1\120"+
		"\2\105\1\111\1\116\1\106\1\122\1\111\1\122\1\uffff\1\124\2\105\1\111\1"+
		"\115\1\55\1\116\1\104\1\111\1\104\1\uffff\1\106\2\uffff\1\uffff\1\uffff"+
		"\1\uffff\1\117\1\66\1\143\2\50\1\67\1\62\1\154\1\105\1\65\1\162\2\50\1"+
		"\uffff\1\105\2\uffff\1\101\1\124\1\122\1\116\1\124\1\101\1\104\1\117\1"+
		"\124\1\uffff\1\117\1\111\1\117\1\106\1\116\1\107\1\55\1\124\1\uffff\1"+
		"\117\1\124\1\uffff\1\uffff\1\uffff\2\uffff\1\124\1\66\1\143\2\50\1\62"+
		"\1\154\1\106\1\162\3\uffff\1\uffff\1\103\1\uffff\1\55\1\105\1\uffff\1"+
		"\103\1\uffff\1\116\1\123\1\120\1\104\3\124\1\110\1\122\1\117\1\111\1\117"+
		"\1\uffff\1\103\1\55\2\uffff\1\uffff\1\143\2\50\1\154\1\111\1\uffff\1\105"+
		"\1\uffff\1\123\1\122\1\uffff\1\105\1\uffff\3\uffff\1\104\1\124\1\uffff"+
		"\1\105\1\124\2\105\1\111\1\120\1\104\1\124\1\125\1\113\1\uffff\2\50\1"+
		"\130\1\uffff\1\124\2\uffff\3\uffff\1\114\1\117\1\103\1\uffff\1\122\1\uffff"+
		"\1\106\1\116\1\107\1\uffff\1\104\1\124\1\115\1\105\1\50\1\uffff\1\131"+
		"\2\uffff\1\105\1\115\1\117\1\uffff\1\103\1\uffff\2\124\1\110\1\uffff\1"+
		"\114\1\117\1\105\1\131\1\uffff\1\114\2\uffff\1\122\1\uffff\1\117\1\uffff"+
		"\1\105\1\124\1\105\1\115\1\116\1\106\1\105\2\uffff\1\116\1\122\1\103\1"+
		"\uffff\1\122\3\uffff\1\124\1\122\1\uffff\1\105\1\116\1\117\1\uffff\1\103"+
		"\3\uffff\1\uffff\1\101\1\uffff\1\122\1\105\1\122\1\uffff\1\117\1\uffff"+
		"\1\115\1\uffff\1\122\1\116\1\122\1\105\1\uffff\1\uffff\1\105\1\116\1\123"+
		"\1\uffff\1\122\1\105\2\uffff\1\122\2\uffff\1\uffff\1\uffff";
	static final String DFA212_acceptS =
		"\6\uffff\1\6\3\uffff\1\12\1\13\1\14\1\15\1\uffff\1\17\2\uffff\1\24\1\26"+
		"\1\27\1\30\3\uffff\1\43\1\46\1\47\1\uffff\1\52\4\uffff\1\143\2\uffff\1"+
		"\151\1\152\23\uffff\1\134\1\135\1\2\1\42\1\40\1\uffff\1\23\1\4\1\32\1"+
		"\5\1\33\1\7\1\136\1\10\1\25\1\41\1\11\1\36\1\16\1\21\1\20\1\153\1\154"+
		"\1\22\1\44\1\31\1\34\1\142\1\37\1\137\1\140\1\141\1\35\1\55\4\uffff\1"+
		"\145\5\uffff\1\53\1\54\2\uffff\1\1\36\uffff\1\3\1\51\21\uffff\1\121\34"+
		"\uffff\1\50\1\144\31\uffff\1\124\21\uffff\1\127\16\uffff\1\45\2\uffff"+
		"\1\57\30\uffff\1\122\1\131\1\uffff\1\120\20\uffff\1\60\1\113\11\uffff"+
		"\1\66\14\uffff\1\117\1\uffff\1\116\1\132\11\uffff\1\147\1\150\1\56\24"+
		"\uffff\1\126\2\uffff\1\115\1\123\6\uffff\1\114\1\uffff\1\62\2\uffff\1"+
		"\112\1\uffff\1\130\20\uffff\1\133\7\uffff\1\125\1\65\1\102\3\uffff\1\71"+
		"\13\uffff\1\61\1\uffff\1\67\1\64\5\uffff\1\73\3\uffff\1\105\4\uffff\1"+
		"\146\4\uffff\1\72\11\uffff\1\103\1\104\3\uffff\1\76\14\uffff\1\100\1\106"+
		"\1\107\2\uffff\1\63\3\uffff\1\77\1\uffff\1\110\6\uffff\1\70\4\uffff\1"+
		"\74\5\uffff\1\111\1\75\1\uffff\1\101";
	static final String DFA212_specialS =
		"\40\uffff\1\2\76\uffff\1\1\56\uffff\1\0\u0163\uffff}>";
	static final String[] DFA212_transitionS = {
			"\1\45\1\46\2\uffff\1\46\22\uffff\1\45\1\30\1\32\1\41\1\7\1\27\1\31\1"+
			"\32\1\23\1\24\1\10\1\22\1\25\1\3\1\26\1\21\12\42\1\20\1\17\1\2\1\16\1"+
			"\11\1\uffff\1\1\3\35\1\43\11\35\1\34\3\35\1\44\2\35\1\37\5\35\1\14\1"+
			"\40\1\15\1\6\1\35\1\33\24\35\1\36\5\35\1\12\1\5\1\13\1\4\1\uffff\uff80"+
			"\35",
			"\1\64\2\uffff\12\73\6\uffff\1\47\1\71\1\62\1\55\1\66\1\65\1\56\2\73"+
			"\1\51\2\73\1\60\1\53\1\54\1\73\1\52\1\73\1\63\1\57\1\61\1\70\1\73\1\67"+
			"\3\73\1\uffff\1\73\2\uffff\1\73\1\uffff\21\73\1\50\10\73\5\uffff\uff80"+
			"\73",
			"\1\74\33\uffff\1\75",
			"\1\77\23\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
			"\uff80\35",
			"\1\101",
			"\1\103",
			"",
			"\1\106\2\uffff\12\106\3\uffff\1\105\3\uffff\32\106\1\uffff\1\106\2\uffff"+
			"\1\106\1\uffff\32\106\5\uffff\uff80\106",
			"\1\107",
			"\1\111",
			"",
			"",
			"",
			"",
			"\1\75\1\113\1\111",
			"",
			"\1\115",
			"\1\117\4\uffff\1\120",
			"",
			"",
			"",
			"",
			"\1\122\1\uffff\12\42",
			"\1\125\2\uffff\12\125\7\uffff\32\125\1\uffff\1\125\2\uffff\1\125\1\uffff"+
			"\32\125\5\uffff\uff80\125",
			"\1\133\26\uffff\1\133\16\uffff\1\133\15\uffff\1\126\6\uffff\1\127\2"+
			"\uffff\1\131\1\uffff\1\133\5\uffff\1\130",
			"",
			"",
			"",
			"\1\134",
			"",
			"\1\140\46\uffff\1\136\11\uffff\1\137\25\uffff\1\135",
			"\1\140\46\uffff\1\141\11\uffff\1\137\25\uffff\1\135",
			"\12\35\1\uffff\1\35\2\uffff\42\35\1\143\4\35\1\145\1\35\1\145\35\35"+
			"\1\144\37\35\1\142\uff8a\35",
			"\1\147\2\uffff\12\147\7\uffff\32\147\1\uffff\1\147\2\uffff\1\147\1\uffff"+
			"\32\147\5\uffff\uff80\147",
			"",
			"\1\150",
			"\1\151",
			"",
			"",
			"\1\73\2\uffff\12\73\6\uffff\1\152\32\73\1\uffff\1\73\2\uffff\1\73\1"+
			"\uffff\32\73\5\uffff\uff80\73",
			"\1\153",
			"\1\156\6\uffff\1\154\1\155",
			"\1\157",
			"\1\160\3\uffff\1\161",
			"\1\162",
			"\1\163\6\uffff\1\164",
			"\1\165\5\uffff\1\166",
			"\1\167",
			"\1\170\3\uffff\1\171",
			"\1\172",
			"\1\173",
			"\1\175\3\uffff\1\174",
			"\1\176\11\uffff\1\177",
			"\1\u0083\12\uffff\1\u0082\5\uffff\1\u0081\5\uffff\1\u0080",
			"\1\u0084",
			"\1\u0085\6\uffff\1\u0086",
			"\1\u0087",
			"\1\u0088",
			"",
			"",
			"",
			"",
			"",
			"\1\u0089\2\uffff\32\u008a\1\uffff\1\u008a\2\uffff\1\u008a\1\uffff\32"+
			"\u008a\5\uffff\uff80\u008a",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\u008b",
			"\1\u008d\17\uffff\1\u008e\17\uffff\1\u008c",
			"\1\u008d\17\uffff\1\u008e\17\uffff\1\u008c",
			"\12\35\1\uffff\1\35\2\uffff\42\35\1\u0090\4\35\1\u0092\1\35\1\u0092"+
			"\32\35\1\u0091\37\35\1\u008f\uff8d\35",
			"",
			"\1\u0093\17\uffff\1\u008e\17\uffff\1\u008c",
			"\1\136\11\uffff\1\137\25\uffff\1\135",
			"\1\u0094\4\uffff\1\u0095\1\uffff\1\u0095",
			"\1\136\11\uffff\1\137\25\uffff\1\135",
			"\1\u0096",
			"",
			"",
			"\1\u0097",
			"\1\u0098",
			"",
			"\1\u0099",
			"\1\u009a",
			"\1\u009b",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u009d",
			"\1\u009e",
			"\1\u009f",
			"\1\u00a0",
			"\1\u00a1",
			"\1\u00a3\6\uffff\1\u00a2",
			"\1\u00a4\3\uffff\1\u00a5",
			"\1\u00a6",
			"\1\u00a7",
			"\1\u00a8",
			"\1\u00a9",
			"\1\u00aa",
			"\1\u00ab",
			"\1\u00ac",
			"\1\u00ad",
			"\1\u00ae",
			"\1\u00af",
			"\1\u00b0",
			"\1\u00b1",
			"\1\u00b2",
			"\1\u00b3",
			"\1\u00b4",
			"\1\u00b5",
			"\1\u00b6",
			"\1\u00b7",
			"\1\u00b8",
			"",
			"",
			"\1\35\2\uffff\12\35\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff"+
			"\32\35\5\uffff\uff80\35",
			"\1\u00ba",
			"\1\u00ba",
			"\12\35\1\uffff\1\35\2\uffff\42\35\1\u00bc\3\35\1\u00be\1\35\1\u00be"+
			"\25\35\1\u00bd\37\35\1\u00bb\uff93\35",
			"\1\u008d\17\uffff\1\u008e\17\uffff\1\u008c",
			"\1\u00bf\4\uffff\1\u00c0\1\uffff\1\u00c0",
			"\1\u008d\17\uffff\1\u008e\17\uffff\1\u008c",
			"\1\u00c1",
			"\1\u00ba\4\uffff\1\u00c2",
			"\1\u00c3\4\uffff\1\u00c4\1\uffff\1\u00c4",
			"\1\u00c5",
			"\1\136\11\uffff\1\137\25\uffff\1\135",
			"\1\u00c6",
			"\1\u00c7",
			"\1\u00c8",
			"\1\u00c9",
			"\1\u00ca",
			"",
			"\1\u00cb",
			"\1\u00cc",
			"\1\u00cd",
			"\1\u00ce",
			"\1\u00cf",
			"\1\u00d0",
			"\1\u00d1",
			"\1\u00d2",
			"\1\73\2\uffff\12\73\7\uffff\26\73\1\u00d3\3\73\1\uffff\1\73\2\uffff"+
			"\1\73\1\uffff\32\73\5\uffff\uff80\73",
			"\1\u00d5",
			"\1\u00d6",
			"\1\u00d7",
			"\1\u00d8",
			"\1\u00d9",
			"\1\u00da",
			"\1\u00db",
			"\1\u00dc",
			"\1\u00dd",
			"\1\u00de",
			"\1\u00df",
			"\1\u00e0",
			"\1\u00e1",
			"\1\u00e2",
			"\1\u00e3",
			"\1\u00e4",
			"\1\u00e5",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u00e7",
			"",
			"",
			"\1\u00ba",
			"\1\u00e8\3\uffff\1\u00e9\1\uffff\1\u00e9",
			"\1\u00ba",
			"\1\u00eb\37\uffff\1\u00ea",
			"\1\u00ec\4\uffff\1\u00ed\1\uffff\1\u00ed",
			"\1\u00ee",
			"\1\u008d\17\uffff\1\u008e\17\uffff\1\u008c",
			"\1\u00ef",
			"\1\u00f0\4\uffff\1\u00f1\1\uffff\1\u00f1",
			"\1\u00f2",
			"\1\136\11\uffff\1\137\25\uffff\1\135",
			"\1\u00f3",
			"\1\u00f4",
			"\1\u00f5",
			"\1\u00f6",
			"\1\u00f7",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u00f9",
			"\1\u00fa",
			"\1\u00fb",
			"\1\u00fc",
			"\1\u00fd",
			"\1\u00fe\3\uffff\1\u00ff",
			"\1\u0100",
			"\1\u0101",
			"",
			"\1\u0102",
			"\1\u0103",
			"\1\u0104",
			"\1\u0105",
			"\1\u0107\10\uffff\1\u0106\5\uffff\1\u0108",
			"\1\u0109",
			"\1\u010a",
			"\1\u010b",
			"\1\u010c",
			"\1\u010d",
			"\1\u010e",
			"\1\u010f",
			"\1\73\2\uffff\12\73\7\uffff\10\73\1\u0110\21\73\1\uffff\1\73\2\uffff"+
			"\1\73\1\uffff\32\73\5\uffff\uff80\73",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u0113",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u0115",
			"",
			"\1\u0116",
			"\1\u0117\3\uffff\1\u0118\1\uffff\1\u0118",
			"\1\u011a\37\uffff\1\u0119",
			"\1\u00ba",
			"\1\u00ba",
			"\1\u011b\4\uffff\1\u011c\1\uffff\1\u011c",
			"\1\u011d",
			"\1\u008d\17\uffff\1\u008e\17\uffff\1\u008c",
			"\1\u011e",
			"\1\u011f\1\uffff\1\u011f",
			"\1\u0120",
			"\1\136\11\uffff\1\137\25\uffff\1\135",
			"\1\u0121",
			"\1\u0122",
			"",
			"\1\u0123",
			"\1\u0124",
			"",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u0127",
			"\1\u0128",
			"\1\u0129",
			"\1\u012a",
			"\1\u012b",
			"\1\u012c",
			"\1\u012d",
			"\1\u012e",
			"\1\u012f",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u0133\12\uffff\1\u0132\6\uffff\1\u0131",
			"\1\u0134",
			"\1\u0135",
			"\1\u0136",
			"\1\u0137",
			"\1\u0138",
			"\1\u0139",
			"\1\u013a",
			"\1\u013b",
			"\1\u013c",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u013e",
			"",
			"",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u0141",
			"\1\u0142\3\uffff\1\u0143\1\uffff\1\u0143",
			"\1\u0145\37\uffff\1\u0144",
			"\1\u00ba",
			"\1\u00ba",
			"\1\u0146\1\uffff\1\u0146",
			"\1\u0147",
			"\1\u008d\17\uffff\1\u008e\17\uffff\1\u008c",
			"\1\u0148",
			"\1\u0149",
			"\1\136\11\uffff\1\137\25\uffff\1\135",
			"\1\u014a",
			"\1\u014b",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u014d",
			"",
			"",
			"\1\u014e",
			"\1\u014f",
			"\1\u0150",
			"\1\u0151",
			"\1\u0152",
			"\1\u0153",
			"\1\u0154",
			"\1\u0155",
			"\1\u0156",
			"",
			"\1\u0157",
			"\1\u0158",
			"\1\u0159",
			"\1\u015a",
			"\1\u015b",
			"\1\u015c",
			"\1\u015d",
			"\1\u0160\12\uffff\1\u015f\6\uffff\1\u015e",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u0162",
			"\1\u0163",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"",
			"",
			"\1\u0166",
			"\1\u0167\1\uffff\1\u0167",
			"\1\u0169\37\uffff\1\u0168",
			"\1\u00ba",
			"\1\u00ba",
			"\1\u016a",
			"\1\u008d\17\uffff\1\u008e\17\uffff\1\u008c",
			"\1\u016b",
			"\1\136\11\uffff\1\137\25\uffff\1\135",
			"",
			"",
			"",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u016d",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u016f",
			"\1\u0170",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u0172",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u0174",
			"\1\u0175",
			"\1\u0176",
			"\1\u0177",
			"\1\u0178",
			"\1\u0179",
			"\1\u017a",
			"\1\u017b",
			"\1\u017d\10\uffff\1\u017c\5\uffff\1\u017e",
			"\1\u017f",
			"\1\u0180",
			"\1\u0181",
			"",
			"\1\u0182",
			"\1\u0183",
			"",
			"",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u0186\37\uffff\1\u0185",
			"\1\u00ba",
			"\1\u00ba",
			"\1\u008d\17\uffff\1\u008e\17\uffff\1\u008c",
			"\1\u0187",
			"",
			"\1\u0188",
			"",
			"\1\u0189",
			"\1\u018a",
			"",
			"\1\u018b",
			"",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u018f",
			"\1\u0190",
			"\1\u0191\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u0193",
			"\1\u0194",
			"\1\u0195",
			"\1\u0196",
			"\1\u0197",
			"\1\u0198",
			"\1\u0199",
			"\1\u019a",
			"\1\u019b",
			"\1\u019c",
			"",
			"\1\u00ba",
			"\1\u00ba",
			"\1\u019d",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u019f",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"",
			"",
			"",
			"\1\u01a2",
			"\1\u01a3",
			"\1\u01a4",
			"",
			"\1\u01a5",
			"\1\u01a6\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u01a8",
			"\1\u01a9",
			"\1\u01aa",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u01ac",
			"\1\u01ad",
			"\1\u01ae",
			"\1\u01af",
			"\1\u01b0",
			"",
			"\1\u01b1",
			"",
			"",
			"\1\u01b2",
			"\1\u01b3",
			"\1\u01b4",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u01b6",
			"",
			"\1\u01b7",
			"\1\u01b8",
			"\1\u01b9",
			"",
			"\1\u01ba",
			"\1\u01bb",
			"\1\u01bc",
			"\1\u01bd",
			"",
			"\1\u01be",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u01c1",
			"",
			"\1\u01c2",
			"\1\u01c3\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u01c5",
			"\1\u01c6",
			"\1\u01c7",
			"\1\u01c8",
			"\1\u01c9",
			"\1\u01ca",
			"\1\u01cb",
			"",
			"",
			"\1\u01cc",
			"\1\u01cd",
			"\1\u01ce",
			"",
			"\1\u01cf",
			"\1\u01d0\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u01d4",
			"\1\u01d5",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u01d7",
			"\1\u01d8",
			"\1\u01d9",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u01db",
			"",
			"",
			"",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u01dd",
			"",
			"\1\u01de",
			"\1\u01df",
			"\1\u01e0",
			"",
			"\1\u01e1",
			"",
			"\1\u01e2",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u01e4",
			"\1\u01e5",
			"\1\u01e6",
			"\1\u01e7",
			"",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u01e9",
			"\1\u01ea",
			"\1\u01eb",
			"",
			"\1\u01ec",
			"\1\u01ed",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u01f0",
			"",
			"",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			""
	};

	static final short[] DFA212_eot = DFA.unpackEncodedString(DFA212_eotS);
	static final short[] DFA212_eof = DFA.unpackEncodedString(DFA212_eofS);
	static final char[] DFA212_min = DFA.unpackEncodedStringToUnsignedChars(DFA212_minS);
	static final char[] DFA212_max = DFA.unpackEncodedStringToUnsignedChars(DFA212_maxS);
	static final short[] DFA212_accept = DFA.unpackEncodedString(DFA212_acceptS);
	static final short[] DFA212_special = DFA.unpackEncodedString(DFA212_specialS);
	static final short[][] DFA212_transition;

	static {
		int numStates = DFA212_transitionS.length;
		DFA212_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA212_transition[i] = DFA.unpackEncodedString(DFA212_transitionS[i]);
		}
	}

	protected class DFA212 extends DFA {

		public DFA212(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 212;
			this.eot = DFA212_eot;
			this.eof = DFA212_eof;
			this.min = DFA212_min;
			this.max = DFA212_max;
			this.accept = DFA212_accept;
			this.special = DFA212_special;
			this.transition = DFA212_transition;
		}
		@Override
		public String getDescription() {
			return "1:1: Tokens : ( GEN | CDO | CDC | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS | GREATER | LBRACE | RBRACE | LBRACKET | RBRACKET | OPEQ | SEMI | COLON | DCOLON | SOLIDUS | MINUS | PLUS | STAR | LPAREN | RPAREN | COMMA | DOT | TILDE | PIPE | PERCENTAGE_SYMBOL | EXCLAMATION_MARK | CP_EQ | CP_NOT_EQ | LESS | GREATER_OR_EQ | LESS_OR_EQ | LESS_AND | CP_DOTS | LESS_REST | STRING | LESS_JS_STRING | NOT | VARIABLE | IDENT | HASH_SYMBOL | HASH | IMPORTANT_SYM | IMPORT_SYM | PAGE_SYM | MEDIA_SYM | NAMESPACE_SYM | CHARSET_SYM | COUNTER_STYLE_SYM | FONT_FACE_SYM | SUPPORTS_SYM | LAYER_SYM | CONTAINER_SYM | TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM | MOZ_DOCUMENT_SYM | WEBKIT_KEYFRAMES_SYM | SASS_CONTENT | SASS_MIXIN | SASS_INCLUDE | SASS_EXTEND | SASS_DEBUG | SASS_ERROR | SASS_WARN | SASS_IF | SASS_ELSE | SASS_ELSEIF | SASS_FOR | SASS_FUNCTION | SASS_RETURN | SASS_USE | SASS_FORWARD | SASS_EACH | SASS_WHILE | SASS_AT_ROOT | AT_SIGN | AT_IDENT | SASS_VAR | SASS_DEFAULT | SASS_OPTIONAL | SASS_GLOBAL | SASS_EXTEND_ONLY_SELECTOR | NUMBER | URI | URANGE | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP | WS | NL | COMMENT | LINE_COMMENT );";
		}
		@Override
		public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
			IntStream input = _input;
			int _s = s;
			switch ( s ) {
					case 0 : 
						int LA212_142 = input.LA(1);
						s = -1;
						if ( (LA212_142=='l') ) {s = 187;}
						else if ( (LA212_142=='0') ) {s = 188;}
						else if ( (LA212_142=='L') ) {s = 189;}
						else if ( ((LA212_142 >= '\u0000' && LA212_142 <= '\t')||LA212_142=='\u000B'||(LA212_142 >= '\u000E' && LA212_142 <= '/')||(LA212_142 >= '1' && LA212_142 <= '3')||LA212_142=='5'||(LA212_142 >= '7' && LA212_142 <= 'K')||(LA212_142 >= 'M' && LA212_142 <= 'k')||(LA212_142 >= 'm' && LA212_142 <= '\uFFFF')) ) {s = 29;}
						else if ( (LA212_142=='4'||LA212_142=='6') ) {s = 190;}
						if ( s>=0 ) return s;
						break;

					case 1 : 
						int LA212_95 = input.LA(1);
						s = -1;
						if ( (LA212_95=='r') ) {s = 143;}
						else if ( (LA212_95=='0') ) {s = 144;}
						else if ( (LA212_95=='R') ) {s = 145;}
						else if ( ((LA212_95 >= '\u0000' && LA212_95 <= '\t')||LA212_95=='\u000B'||(LA212_95 >= '\u000E' && LA212_95 <= '/')||(LA212_95 >= '1' && LA212_95 <= '4')||LA212_95=='6'||(LA212_95 >= '8' && LA212_95 <= 'Q')||(LA212_95 >= 'S' && LA212_95 <= 'q')||(LA212_95 >= 's' && LA212_95 <= '\uFFFF')) ) {s = 29;}
						else if ( (LA212_95=='5'||LA212_95=='7') ) {s = 146;}
						if ( s>=0 ) return s;
						break;

					case 2 : 
						int LA212_32 = input.LA(1);
						s = -1;
						if ( (LA212_32=='u') ) {s = 98;}
						else if ( (LA212_32=='0') ) {s = 99;}
						else if ( (LA212_32=='U') ) {s = 100;}
						else if ( ((LA212_32 >= '\u0000' && LA212_32 <= '\t')||LA212_32=='\u000B'||(LA212_32 >= '\u000E' && LA212_32 <= '/')||(LA212_32 >= '1' && LA212_32 <= '4')||LA212_32=='6'||(LA212_32 >= '8' && LA212_32 <= 'T')||(LA212_32 >= 'V' && LA212_32 <= 't')||(LA212_32 >= 'v' && LA212_32 <= '\uFFFF')) ) {s = 29;}
						else if ( (LA212_32=='5'||LA212_32=='7') ) {s = 101;}
						if ( s>=0 ) return s;
						break;
			}
			if (state.backtracking>0) {state.failed=true; return -1;}
			NoViableAltException nvae =
				new NoViableAltException(getDescription(), 212, _s, input);
			error(nvae);
			throw nvae;
		}
	}

}
