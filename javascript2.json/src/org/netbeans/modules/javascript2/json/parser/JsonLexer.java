// Generated from Json.g4 by ANTLR 4.5.3

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
package org.netbeans.modules.javascript2.json.parser;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class JsonLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		COLON=1, COMMA=2, DOT=3, PLUS=4, MINUS=5, LBRACE=6, RBRACE=7, LBRACKET=8, 
		RBRACKET=9, TRUE=10, FALSE=11, NULL=12, NUMBER=13, STRING=14, LINE_COMMENT=15, 
		COMMENT=16, WS=17, ERROR_COMMENT=18, ERROR=19;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"COLON", "COMMA", "DOT", "PLUS", "MINUS", "LBRACE", "RBRACE", "LBRACKET", 
		"RBRACKET", "TRUE", "FALSE", "NULL", "NUMBER", "INTEGER", "DIGIT_0", "DIGIT_19", 
		"DIGIT", "FRACTION", "EXPONENT", "STRING", "QUOTE", "CHAR", "CONTROL", 
		"UNICODE", "HEXDIGIT", "LINE_COMMENT", "COMMENT", "WS", "ERROR_COMMENT", 
		"ERROR"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "':'", "','", "'.'", "'+'", "'-'", "'{'", "'}'", "'['", "']'", "'true'", 
		"'false'", "'null'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "COLON", "COMMA", "DOT", "PLUS", "MINUS", "LBRACE", "RBRACE", "LBRACKET", 
		"RBRACKET", "TRUE", "FALSE", "NULL", "NUMBER", "STRING", "LINE_COMMENT", 
		"COMMENT", "WS", "ERROR_COMMENT", "ERROR"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public static final int WHITESPACES = 1;
	public static final int COMMENTS = 2;
	public static final int ERRORS = 3;

	private static final Recovery[] RECOVERIES = {
	    Recovery.createLineCommentRecovery(),
	    Recovery.createCommentRecovery(),
	    Recovery.createStringRecovery()
	};

	private boolean isCommentSupported;
	private boolean hasErrorToken;

	public LexerState getLexerState() {
	    return new LexerState(getState());
	}

	public void setLexerState(LexerState state) {
	    this.setState(state.atnState);
	}

	public static final class LexerState {

	    final int atnState;

	    public LexerState(int atnState) {
	        this.atnState = atnState;
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
	        if (this.atnState != other.atnState) {
	            return false;
	        }
	        return true;
	    }

	    @Override
	    public int hashCode() {
	        int hash = 5;
	        hash = 29 * hash + this.atnState;
	        return hash;
	    }
	}

	public JsonLexer(
	        final CharStream input,
	        final boolean isCommentSupported) {
	    this(input, isCommentSupported, false);
	}

	public  JsonLexer(
	        final CharStream input,
	        final boolean isCommentSupported,
	        final boolean hasErrorToken) {
	    this(input);
	    this.isCommentSupported = isCommentSupported;
	    this.hasErrorToken = hasErrorToken;
	}

	@Override
	public void recover(LexerNoViableAltException e) {
	    final CharStream in = e.getInputStream();
	    final int current = in.index();
	    final int index = e.getStartIndex();
	    boolean resolved = false;
	    in.seek(index);
	    for (Recovery r : RECOVERIES) {
	        if (r.canRecover(in)) {
	            getInterpreter().setCharPositionInLine(_tokenStartCharPositionInLine);
	            getInterpreter().setLine(_tokenStartLine);
	            r.recover(in, getInterpreter());
	            resolved = true;
	            break;
	        }
	    }
	    if (!resolved) {
	        in.seek(current);
	        super.recover(e);
	    }
	}


	public JsonLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Json.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	@Override
	public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
		switch (ruleIndex) {
		case 25:
			LINE_COMMENT_action((RuleContext)_localctx, actionIndex);
			break;
		case 26:
			COMMENT_action((RuleContext)_localctx, actionIndex);
			break;
		case 27:
			WS_action((RuleContext)_localctx, actionIndex);
			break;
		case 28:
			ERROR_COMMENT_action((RuleContext)_localctx, actionIndex);
			break;
		case 29:
			ERROR_action((RuleContext)_localctx, actionIndex);
			break;
		}
	}
	private void LINE_COMMENT_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0:
			_channel = COMMENTS;
			break;
		}
	}
	private void COMMENT_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 1:
			_channel = COMMENTS;
			break;
		}
	}
	private void WS_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 2:
			_channel = WHITESPACES;
			break;
		}
	}
	private void ERROR_COMMENT_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 3:
			_channel = ERRORS;
			break;
		}
	}
	private void ERROR_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 4:
			_channel = ERRORS;
			break;
		}
	}
	@Override
	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 25:
			return LINE_COMMENT_sempred((RuleContext)_localctx, predIndex);
		case 26:
			return COMMENT_sempred((RuleContext)_localctx, predIndex);
		case 28:
			return ERROR_COMMENT_sempred((RuleContext)_localctx, predIndex);
		case 29:
			return ERROR_sempred((RuleContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean LINE_COMMENT_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return isCommentSupported;
		}
		return true;
	}
	private boolean COMMENT_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return isCommentSupported;
		}
		return true;
	}
	private boolean ERROR_COMMENT_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return hasErrorToken && isCommentSupported;
		}
		return true;
	}
	private boolean ERROR_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 3:
			return hasErrorToken;
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\25\u00e9\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\3\2"+
		"\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3"+
		"\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3"+
		"\16\3\16\5\16d\n\16\3\16\5\16g\n\16\3\17\5\17j\n\17\3\17\3\17\3\17\7\17"+
		"o\n\17\f\17\16\17r\13\17\5\17t\n\17\3\20\3\20\3\21\3\21\3\22\3\22\5\22"+
		"|\n\22\3\23\3\23\6\23\u0080\n\23\r\23\16\23\u0081\3\24\3\24\3\24\5\24"+
		"\u0087\n\24\3\24\6\24\u008a\n\24\r\24\16\24\u008b\3\25\3\25\7\25\u0090"+
		"\n\25\f\25\16\25\u0093\13\25\3\25\3\25\3\26\3\26\3\27\3\27\5\27\u009b"+
		"\n\27\3\30\3\30\3\30\5\30\u00a0\n\30\3\31\3\31\3\31\3\31\3\31\3\31\3\32"+
		"\3\32\3\33\3\33\3\33\3\33\7\33\u00ae\n\33\f\33\16\33\u00b1\13\33\3\33"+
		"\5\33\u00b4\n\33\3\33\3\33\3\33\3\33\3\33\3\34\3\34\3\34\3\34\7\34\u00bf"+
		"\n\34\f\34\16\34\u00c2\13\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\35\6"+
		"\35\u00cc\n\35\r\35\16\35\u00cd\3\35\3\35\3\36\3\36\3\36\3\36\3\36\6\36"+
		"\u00d7\n\36\r\36\16\36\u00d8\3\36\7\36\u00dc\n\36\f\36\16\36\u00df\13"+
		"\36\3\36\3\36\3\36\3\36\3\37\3\37\3\37\3\37\3\37\4\u00af\u00c0\2 \3\3"+
		"\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\2\37\2"+
		"!\2#\2%\2\'\2)\20+\2-\2/\2\61\2\63\2\65\21\67\229\23;\24=\25\3\2\n\3\2"+
		"\63;\4\2GGgg\5\2\2!$$^^\n\2$$\61\61^^ddhhppttvv\5\2\62;CHch\5\2\13\f\17"+
		"\17\"\"\3\2,,\3\2\61\61\u00f1\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t"+
		"\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2"+
		"\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2)\3\2\2\2\2\65"+
		"\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\3?\3\2\2\2\5A\3"+
		"\2\2\2\7C\3\2\2\2\tE\3\2\2\2\13G\3\2\2\2\rI\3\2\2\2\17K\3\2\2\2\21M\3"+
		"\2\2\2\23O\3\2\2\2\25Q\3\2\2\2\27V\3\2\2\2\31\\\3\2\2\2\33a\3\2\2\2\35"+
		"i\3\2\2\2\37u\3\2\2\2!w\3\2\2\2#{\3\2\2\2%}\3\2\2\2\'\u0083\3\2\2\2)\u008d"+
		"\3\2\2\2+\u0096\3\2\2\2-\u009a\3\2\2\2/\u009c\3\2\2\2\61\u00a1\3\2\2\2"+
		"\63\u00a7\3\2\2\2\65\u00a9\3\2\2\2\67\u00ba\3\2\2\29\u00cb\3\2\2\2;\u00d1"+
		"\3\2\2\2=\u00e4\3\2\2\2?@\7<\2\2@\4\3\2\2\2AB\7.\2\2B\6\3\2\2\2CD\7\60"+
		"\2\2D\b\3\2\2\2EF\7-\2\2F\n\3\2\2\2GH\7/\2\2H\f\3\2\2\2IJ\7}\2\2J\16\3"+
		"\2\2\2KL\7\177\2\2L\20\3\2\2\2MN\7]\2\2N\22\3\2\2\2OP\7_\2\2P\24\3\2\2"+
		"\2QR\7v\2\2RS\7t\2\2ST\7w\2\2TU\7g\2\2U\26\3\2\2\2VW\7h\2\2WX\7c\2\2X"+
		"Y\7n\2\2YZ\7u\2\2Z[\7g\2\2[\30\3\2\2\2\\]\7p\2\2]^\7w\2\2^_\7n\2\2_`\7"+
		"n\2\2`\32\3\2\2\2ac\5\35\17\2bd\5%\23\2cb\3\2\2\2cd\3\2\2\2df\3\2\2\2"+
		"eg\5\'\24\2fe\3\2\2\2fg\3\2\2\2g\34\3\2\2\2hj\5\13\6\2ih\3\2\2\2ij\3\2"+
		"\2\2js\3\2\2\2kt\5\37\20\2lp\5!\21\2mo\5#\22\2nm\3\2\2\2or\3\2\2\2pn\3"+
		"\2\2\2pq\3\2\2\2qt\3\2\2\2rp\3\2\2\2sk\3\2\2\2sl\3\2\2\2t\36\3\2\2\2u"+
		"v\7\62\2\2v \3\2\2\2wx\t\2\2\2x\"\3\2\2\2y|\5\37\20\2z|\5!\21\2{y\3\2"+
		"\2\2{z\3\2\2\2|$\3\2\2\2}\177\5\7\4\2~\u0080\5#\22\2\177~\3\2\2\2\u0080"+
		"\u0081\3\2\2\2\u0081\177\3\2\2\2\u0081\u0082\3\2\2\2\u0082&\3\2\2\2\u0083"+
		"\u0086\t\3\2\2\u0084\u0087\5\t\5\2\u0085\u0087\5\13\6\2\u0086\u0084\3"+
		"\2\2\2\u0086\u0085\3\2\2\2\u0086\u0087\3\2\2\2\u0087\u0089\3\2\2\2\u0088"+
		"\u008a\5#\22\2\u0089\u0088\3\2\2\2\u008a\u008b\3\2\2\2\u008b\u0089\3\2"+
		"\2\2\u008b\u008c\3\2\2\2\u008c(\3\2\2\2\u008d\u0091\5+\26\2\u008e\u0090"+
		"\5-\27\2\u008f\u008e\3\2\2\2\u0090\u0093\3\2\2\2\u0091\u008f\3\2\2\2\u0091"+
		"\u0092\3\2\2\2\u0092\u0094\3\2\2\2\u0093\u0091\3\2\2\2\u0094\u0095\5+"+
		"\26\2\u0095*\3\2\2\2\u0096\u0097\7$\2\2\u0097,\3\2\2\2\u0098\u009b\n\4"+
		"\2\2\u0099\u009b\5/\30\2\u009a\u0098\3\2\2\2\u009a\u0099\3\2\2\2\u009b"+
		".\3\2\2\2\u009c\u009f\7^\2\2\u009d\u00a0\t\5\2\2\u009e\u00a0\5\61\31\2"+
		"\u009f\u009d\3\2\2\2\u009f\u009e\3\2\2\2\u00a0\60\3\2\2\2\u00a1\u00a2"+
		"\7w\2\2\u00a2\u00a3\5\63\32\2\u00a3\u00a4\5\63\32\2\u00a4\u00a5\5\63\32"+
		"\2\u00a5\u00a6\5\63\32\2\u00a6\62\3\2\2\2\u00a7\u00a8\t\6\2\2\u00a8\64"+
		"\3\2\2\2\u00a9\u00aa\7\61\2\2\u00aa\u00ab\7\61\2\2\u00ab\u00af\3\2\2\2"+
		"\u00ac\u00ae\13\2\2\2\u00ad\u00ac\3\2\2\2\u00ae\u00b1\3\2\2\2\u00af\u00b0"+
		"\3\2\2\2\u00af\u00ad\3\2\2\2\u00b0\u00b3\3\2\2\2\u00b1\u00af\3\2\2\2\u00b2"+
		"\u00b4\7\17\2\2\u00b3\u00b2\3\2\2\2\u00b3\u00b4\3\2\2\2\u00b4\u00b5\3"+
		"\2\2\2\u00b5\u00b6\7\f\2\2\u00b6\u00b7\6\33\2\2\u00b7\u00b8\3\2\2\2\u00b8"+
		"\u00b9\b\33\2\2\u00b9\66\3\2\2\2\u00ba\u00bb\7\61\2\2\u00bb\u00bc\7,\2"+
		"\2\u00bc\u00c0\3\2\2\2\u00bd\u00bf\13\2\2\2\u00be\u00bd\3\2\2\2\u00bf"+
		"\u00c2\3\2\2\2\u00c0\u00c1\3\2\2\2\u00c0\u00be\3\2\2\2\u00c1\u00c3\3\2"+
		"\2\2\u00c2\u00c0\3\2\2\2\u00c3\u00c4\7,\2\2\u00c4\u00c5\7\61\2\2\u00c5"+
		"\u00c6\3\2\2\2\u00c6\u00c7\6\34\3\2\u00c7\u00c8\3\2\2\2\u00c8\u00c9\b"+
		"\34\3\2\u00c98\3\2\2\2\u00ca\u00cc\t\7\2\2\u00cb\u00ca\3\2\2\2\u00cc\u00cd"+
		"\3\2\2\2\u00cd\u00cb\3\2\2\2\u00cd\u00ce\3\2\2\2\u00ce\u00cf\3\2\2\2\u00cf"+
		"\u00d0\b\35\4\2\u00d0:\3\2\2\2\u00d1\u00d2\7\61\2\2\u00d2\u00d3\7,\2\2"+
		"\u00d3\u00dd\3\2\2\2\u00d4\u00dc\n\b\2\2\u00d5\u00d7\7,\2\2\u00d6\u00d5"+
		"\3\2\2\2\u00d7\u00d8\3\2\2\2\u00d8\u00d6\3\2\2\2\u00d8\u00d9\3\2\2\2\u00d9"+
		"\u00da\3\2\2\2\u00da\u00dc\n\t\2\2\u00db\u00d4\3\2\2\2\u00db\u00d6\3\2"+
		"\2\2\u00dc\u00df\3\2\2\2\u00dd\u00db\3\2\2\2\u00dd\u00de\3\2\2\2\u00de"+
		"\u00e0\3\2\2\2\u00df\u00dd\3\2\2\2\u00e0\u00e1\6\36\4\2\u00e1\u00e2\3"+
		"\2\2\2\u00e2\u00e3\b\36\5\2\u00e3<\3\2\2\2\u00e4\u00e5\13\2\2\2\u00e5"+
		"\u00e6\6\37\5\2\u00e6\u00e7\3\2\2\2\u00e7\u00e8\b\37\6\2\u00e8>\3\2\2"+
		"\2\26\2cfips{\u0081\u0086\u008b\u0091\u009a\u009f\u00af\u00b3\u00c0\u00cd"+
		"\u00d8\u00db\u00dd\7\3\33\2\3\34\3\3\35\4\3\36\5\3\37\6";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}