// Generated from java-escape by ANTLR 4.11.1

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
package org.netbeans.modules.javascript2.json.parser;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class JsonLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.11.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		COLON=1, COMMA=2, DOT=3, PLUS=4, MINUS=5, LBRACE=6, RBRACE=7, LBRACKET=8, 
		RBRACKET=9, TRUE=10, FALSE=11, NULL=12, NUMBER=13, STRING=14, LINE_COMMENT=15, 
		COMMENT=16, WS=17, ERROR_COMMENT=18, ERROR=19;
	public static final int
		WHITESPACES=2, COMMENTS=3, ERRORS=4;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN", "WHITESPACES", "COMMENTS", "ERRORS"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"COLON", "COMMA", "DOT", "PLUS", "MINUS", "LBRACE", "RBRACE", "LBRACKET", 
			"RBRACKET", "TRUE", "FALSE", "NULL", "NUMBER", "INTEGER", "DIGIT_0", 
			"DIGIT_19", "DIGIT", "FRACTION", "EXPONENT", "STRING", "QUOTE", "CHAR", 
			"CONTROL", "UNICODE", "HEXDIGIT", "LINE_COMMENT", "COMMENT", "WS", "ERROR_COMMENT", 
			"ERROR"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "':'", "','", "'.'", "'+'", "'-'", "'{'", "'}'", "'['", "']'", 
			"'true'", "'false'", "'null'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "COLON", "COMMA", "DOT", "PLUS", "MINUS", "LBRACE", "RBRACE", "LBRACKET", 
			"RBRACKET", "TRUE", "FALSE", "NULL", "NUMBER", "STRING", "LINE_COMMENT", 
			"COMMENT", "WS", "ERROR_COMMENT", "ERROR"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
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
	public String getGrammarFileName() { return "JsonLexer.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

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
		"\u0004\u0000\u0013\u00e7\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"+
		"\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"+
		"\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"+
		"\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"+
		"\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e"+
		"\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011"+
		"\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014"+
		"\u0002\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017"+
		"\u0002\u0018\u0007\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a"+
		"\u0002\u001b\u0007\u001b\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d"+
		"\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002"+
		"\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005"+
		"\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\f\u0001\f\u0003\fb\b\f\u0001\f\u0003\fe\b\f\u0001\r\u0003\rh\b"+
		"\r\u0001\r\u0001\r\u0001\r\u0005\rm\b\r\n\r\f\rp\t\r\u0003\rr\b\r\u0001"+
		"\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0003"+
		"\u0010z\b\u0010\u0001\u0011\u0001\u0011\u0004\u0011~\b\u0011\u000b\u0011"+
		"\f\u0011\u007f\u0001\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u0085\b"+
		"\u0012\u0001\u0012\u0004\u0012\u0088\b\u0012\u000b\u0012\f\u0012\u0089"+
		"\u0001\u0013\u0001\u0013\u0005\u0013\u008e\b\u0013\n\u0013\f\u0013\u0091"+
		"\t\u0013\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0015\u0001"+
		"\u0015\u0003\u0015\u0099\b\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0003"+
		"\u0016\u009e\b\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0018\u0001\u0018\u0001\u0019\u0001\u0019\u0001"+
		"\u0019\u0001\u0019\u0005\u0019\u00ac\b\u0019\n\u0019\f\u0019\u00af\t\u0019"+
		"\u0001\u0019\u0003\u0019\u00b2\b\u0019\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0001\u0019\u0001\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a"+
		"\u0005\u001a\u00bd\b\u001a\n\u001a\f\u001a\u00c0\t\u001a\u0001\u001a\u0001"+
		"\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001"+
		"\u001b\u0004\u001b\u00ca\b\u001b\u000b\u001b\f\u001b\u00cb\u0001\u001b"+
		"\u0001\u001b\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0004\u001c\u00d5\b\u001c\u000b\u001c\f\u001c\u00d6\u0001\u001c\u0005"+
		"\u001c\u00da\b\u001c\n\u001c\f\u001c\u00dd\t\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d"+
		"\u0001\u001d\u0002\u00ad\u00be\u0000\u001e\u0001\u0001\u0003\u0002\u0005"+
		"\u0003\u0007\u0004\t\u0005\u000b\u0006\r\u0007\u000f\b\u0011\t\u0013\n"+
		"\u0015\u000b\u0017\f\u0019\r\u001b\u0000\u001d\u0000\u001f\u0000!\u0000"+
		"#\u0000%\u0000\'\u000e)\u0000+\u0000-\u0000/\u00001\u00003\u000f5\u0010"+
		"7\u00119\u0012;\u0013\u0001\u0000\b\u0001\u000019\u0002\u0000EEee\u0003"+
		"\u0000\u0000\u001f\"\"\\\\\b\u0000\"\"//\\\\bbffnnrrtt\u0003\u000009A"+
		"Faf\u0003\u0000\t\n\r\r  \u0001\u0000**\u0001\u0000//\u00ef\u0000\u0001"+
		"\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005"+
		"\u0001\u0000\u0000\u0000\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001"+
		"\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000"+
		"\u0000\u0000\u0000\u000f\u0001\u0000\u0000\u0000\u0000\u0011\u0001\u0000"+
		"\u0000\u0000\u0000\u0013\u0001\u0000\u0000\u0000\u0000\u0015\u0001\u0000"+
		"\u0000\u0000\u0000\u0017\u0001\u0000\u0000\u0000\u0000\u0019\u0001\u0000"+
		"\u0000\u0000\u0000\'\u0001\u0000\u0000\u0000\u00003\u0001\u0000\u0000"+
		"\u0000\u00005\u0001\u0000\u0000\u0000\u00007\u0001\u0000\u0000\u0000\u0000"+
		"9\u0001\u0000\u0000\u0000\u0000;\u0001\u0000\u0000\u0000\u0001=\u0001"+
		"\u0000\u0000\u0000\u0003?\u0001\u0000\u0000\u0000\u0005A\u0001\u0000\u0000"+
		"\u0000\u0007C\u0001\u0000\u0000\u0000\tE\u0001\u0000\u0000\u0000\u000b"+
		"G\u0001\u0000\u0000\u0000\rI\u0001\u0000\u0000\u0000\u000fK\u0001\u0000"+
		"\u0000\u0000\u0011M\u0001\u0000\u0000\u0000\u0013O\u0001\u0000\u0000\u0000"+
		"\u0015T\u0001\u0000\u0000\u0000\u0017Z\u0001\u0000\u0000\u0000\u0019_"+
		"\u0001\u0000\u0000\u0000\u001bg\u0001\u0000\u0000\u0000\u001ds\u0001\u0000"+
		"\u0000\u0000\u001fu\u0001\u0000\u0000\u0000!y\u0001\u0000\u0000\u0000"+
		"#{\u0001\u0000\u0000\u0000%\u0081\u0001\u0000\u0000\u0000\'\u008b\u0001"+
		"\u0000\u0000\u0000)\u0094\u0001\u0000\u0000\u0000+\u0098\u0001\u0000\u0000"+
		"\u0000-\u009a\u0001\u0000\u0000\u0000/\u009f\u0001\u0000\u0000\u00001"+
		"\u00a5\u0001\u0000\u0000\u00003\u00a7\u0001\u0000\u0000\u00005\u00b8\u0001"+
		"\u0000\u0000\u00007\u00c9\u0001\u0000\u0000\u00009\u00cf\u0001\u0000\u0000"+
		"\u0000;\u00e2\u0001\u0000\u0000\u0000=>\u0005:\u0000\u0000>\u0002\u0001"+
		"\u0000\u0000\u0000?@\u0005,\u0000\u0000@\u0004\u0001\u0000\u0000\u0000"+
		"AB\u0005.\u0000\u0000B\u0006\u0001\u0000\u0000\u0000CD\u0005+\u0000\u0000"+
		"D\b\u0001\u0000\u0000\u0000EF\u0005-\u0000\u0000F\n\u0001\u0000\u0000"+
		"\u0000GH\u0005{\u0000\u0000H\f\u0001\u0000\u0000\u0000IJ\u0005}\u0000"+
		"\u0000J\u000e\u0001\u0000\u0000\u0000KL\u0005[\u0000\u0000L\u0010\u0001"+
		"\u0000\u0000\u0000MN\u0005]\u0000\u0000N\u0012\u0001\u0000\u0000\u0000"+
		"OP\u0005t\u0000\u0000PQ\u0005r\u0000\u0000QR\u0005u\u0000\u0000RS\u0005"+
		"e\u0000\u0000S\u0014\u0001\u0000\u0000\u0000TU\u0005f\u0000\u0000UV\u0005"+
		"a\u0000\u0000VW\u0005l\u0000\u0000WX\u0005s\u0000\u0000XY\u0005e\u0000"+
		"\u0000Y\u0016\u0001\u0000\u0000\u0000Z[\u0005n\u0000\u0000[\\\u0005u\u0000"+
		"\u0000\\]\u0005l\u0000\u0000]^\u0005l\u0000\u0000^\u0018\u0001\u0000\u0000"+
		"\u0000_a\u0003\u001b\r\u0000`b\u0003#\u0011\u0000a`\u0001\u0000\u0000"+
		"\u0000ab\u0001\u0000\u0000\u0000bd\u0001\u0000\u0000\u0000ce\u0003%\u0012"+
		"\u0000dc\u0001\u0000\u0000\u0000de\u0001\u0000\u0000\u0000e\u001a\u0001"+
		"\u0000\u0000\u0000fh\u0003\t\u0004\u0000gf\u0001\u0000\u0000\u0000gh\u0001"+
		"\u0000\u0000\u0000hq\u0001\u0000\u0000\u0000ir\u0003\u001d\u000e\u0000"+
		"jn\u0003\u001f\u000f\u0000km\u0003!\u0010\u0000lk\u0001\u0000\u0000\u0000"+
		"mp\u0001\u0000\u0000\u0000nl\u0001\u0000\u0000\u0000no\u0001\u0000\u0000"+
		"\u0000or\u0001\u0000\u0000\u0000pn\u0001\u0000\u0000\u0000qi\u0001\u0000"+
		"\u0000\u0000qj\u0001\u0000\u0000\u0000r\u001c\u0001\u0000\u0000\u0000"+
		"st\u00050\u0000\u0000t\u001e\u0001\u0000\u0000\u0000uv\u0007\u0000\u0000"+
		"\u0000v \u0001\u0000\u0000\u0000wz\u0003\u001d\u000e\u0000xz\u0003\u001f"+
		"\u000f\u0000yw\u0001\u0000\u0000\u0000yx\u0001\u0000\u0000\u0000z\"\u0001"+
		"\u0000\u0000\u0000{}\u0003\u0005\u0002\u0000|~\u0003!\u0010\u0000}|\u0001"+
		"\u0000\u0000\u0000~\u007f\u0001\u0000\u0000\u0000\u007f}\u0001\u0000\u0000"+
		"\u0000\u007f\u0080\u0001\u0000\u0000\u0000\u0080$\u0001\u0000\u0000\u0000"+
		"\u0081\u0084\u0007\u0001\u0000\u0000\u0082\u0085\u0003\u0007\u0003\u0000"+
		"\u0083\u0085\u0003\t\u0004\u0000\u0084\u0082\u0001\u0000\u0000\u0000\u0084"+
		"\u0083\u0001\u0000\u0000\u0000\u0084\u0085\u0001\u0000\u0000\u0000\u0085"+
		"\u0087\u0001\u0000\u0000\u0000\u0086\u0088\u0003!\u0010\u0000\u0087\u0086"+
		"\u0001\u0000\u0000\u0000\u0088\u0089\u0001\u0000\u0000\u0000\u0089\u0087"+
		"\u0001\u0000\u0000\u0000\u0089\u008a\u0001\u0000\u0000\u0000\u008a&\u0001"+
		"\u0000\u0000\u0000\u008b\u008f\u0003)\u0014\u0000\u008c\u008e\u0003+\u0015"+
		"\u0000\u008d\u008c\u0001\u0000\u0000\u0000\u008e\u0091\u0001\u0000\u0000"+
		"\u0000\u008f\u008d\u0001\u0000\u0000\u0000\u008f\u0090\u0001\u0000\u0000"+
		"\u0000\u0090\u0092\u0001\u0000\u0000\u0000\u0091\u008f\u0001\u0000\u0000"+
		"\u0000\u0092\u0093\u0003)\u0014\u0000\u0093(\u0001\u0000\u0000\u0000\u0094"+
		"\u0095\u0005\"\u0000\u0000\u0095*\u0001\u0000\u0000\u0000\u0096\u0099"+
		"\b\u0002\u0000\u0000\u0097\u0099\u0003-\u0016\u0000\u0098\u0096\u0001"+
		"\u0000\u0000\u0000\u0098\u0097\u0001\u0000\u0000\u0000\u0099,\u0001\u0000"+
		"\u0000\u0000\u009a\u009d\u0005\\\u0000\u0000\u009b\u009e\u0007\u0003\u0000"+
		"\u0000\u009c\u009e\u0003/\u0017\u0000\u009d\u009b\u0001\u0000\u0000\u0000"+
		"\u009d\u009c\u0001\u0000\u0000\u0000\u009e.\u0001\u0000\u0000\u0000\u009f"+
		"\u00a0\u0005u\u0000\u0000\u00a0\u00a1\u00031\u0018\u0000\u00a1\u00a2\u0003"+
		"1\u0018\u0000\u00a2\u00a3\u00031\u0018\u0000\u00a3\u00a4\u00031\u0018"+
		"\u0000\u00a40\u0001\u0000\u0000\u0000\u00a5\u00a6\u0007\u0004\u0000\u0000"+
		"\u00a62\u0001\u0000\u0000\u0000\u00a7\u00a8\u0005/\u0000\u0000\u00a8\u00a9"+
		"\u0005/\u0000\u0000\u00a9\u00ad\u0001\u0000\u0000\u0000\u00aa\u00ac\t"+
		"\u0000\u0000\u0000\u00ab\u00aa\u0001\u0000\u0000\u0000\u00ac\u00af\u0001"+
		"\u0000\u0000\u0000\u00ad\u00ae\u0001\u0000\u0000\u0000\u00ad\u00ab\u0001"+
		"\u0000\u0000\u0000\u00ae\u00b1\u0001\u0000\u0000\u0000\u00af\u00ad\u0001"+
		"\u0000\u0000\u0000\u00b0\u00b2\u0005\r\u0000\u0000\u00b1\u00b0\u0001\u0000"+
		"\u0000\u0000\u00b1\u00b2\u0001\u0000\u0000\u0000\u00b2\u00b3\u0001\u0000"+
		"\u0000\u0000\u00b3\u00b4\u0005\n\u0000\u0000\u00b4\u00b5\u0004\u0019\u0000"+
		"\u0000\u00b5\u00b6\u0001\u0000\u0000\u0000\u00b6\u00b7\u0006\u0019\u0000"+
		"\u0000\u00b74\u0001\u0000\u0000\u0000\u00b8\u00b9\u0005/\u0000\u0000\u00b9"+
		"\u00ba\u0005*\u0000\u0000\u00ba\u00be\u0001\u0000\u0000\u0000\u00bb\u00bd"+
		"\t\u0000\u0000\u0000\u00bc\u00bb\u0001\u0000\u0000\u0000\u00bd\u00c0\u0001"+
		"\u0000\u0000\u0000\u00be\u00bf\u0001\u0000\u0000\u0000\u00be\u00bc\u0001"+
		"\u0000\u0000\u0000\u00bf\u00c1\u0001\u0000\u0000\u0000\u00c0\u00be\u0001"+
		"\u0000\u0000\u0000\u00c1\u00c2\u0005*\u0000\u0000\u00c2\u00c3\u0005/\u0000"+
		"\u0000\u00c3\u00c4\u0001\u0000\u0000\u0000\u00c4\u00c5\u0004\u001a\u0001"+
		"\u0000\u00c5\u00c6\u0001\u0000\u0000\u0000\u00c6\u00c7\u0006\u001a\u0000"+
		"\u0000\u00c76\u0001\u0000\u0000\u0000\u00c8\u00ca\u0007\u0005\u0000\u0000"+
		"\u00c9\u00c8\u0001\u0000\u0000\u0000\u00ca\u00cb\u0001\u0000\u0000\u0000"+
		"\u00cb\u00c9\u0001\u0000\u0000\u0000\u00cb\u00cc\u0001\u0000\u0000\u0000"+
		"\u00cc\u00cd\u0001\u0000\u0000\u0000\u00cd\u00ce\u0006\u001b\u0001\u0000"+
		"\u00ce8\u0001\u0000\u0000\u0000\u00cf\u00d0\u0005/\u0000\u0000\u00d0\u00d1"+
		"\u0005*\u0000\u0000\u00d1\u00db\u0001\u0000\u0000\u0000\u00d2\u00da\b"+
		"\u0006\u0000\u0000\u00d3\u00d5\u0005*\u0000\u0000\u00d4\u00d3\u0001\u0000"+
		"\u0000\u0000\u00d5\u00d6\u0001\u0000\u0000\u0000\u00d6\u00d4\u0001\u0000"+
		"\u0000\u0000\u00d6\u00d7\u0001\u0000\u0000\u0000\u00d7\u00d8\u0001\u0000"+
		"\u0000\u0000\u00d8\u00da\b\u0007\u0000\u0000\u00d9\u00d2\u0001\u0000\u0000"+
		"\u0000\u00d9\u00d4\u0001\u0000\u0000\u0000\u00da\u00dd\u0001\u0000\u0000"+
		"\u0000\u00db\u00d9\u0001\u0000\u0000\u0000\u00db\u00dc\u0001\u0000\u0000"+
		"\u0000\u00dc\u00de\u0001\u0000\u0000\u0000\u00dd\u00db\u0001\u0000\u0000"+
		"\u0000\u00de\u00df\u0004\u001c\u0002\u0000\u00df\u00e0\u0001\u0000\u0000"+
		"\u0000\u00e0\u00e1\u0006\u001c\u0002\u0000\u00e1:\u0001\u0000\u0000\u0000"+
		"\u00e2\u00e3\t\u0000\u0000\u0000\u00e3\u00e4\u0004\u001d\u0003\u0000\u00e4"+
		"\u00e5\u0001\u0000\u0000\u0000\u00e5\u00e6\u0006\u001d\u0002\u0000\u00e6"+
		"<\u0001\u0000\u0000\u0000\u0014\u0000adgnqy\u007f\u0084\u0089\u008f\u0098"+
		"\u009d\u00ad\u00b1\u00be\u00cb\u00d6\u00d9\u00db\u0003\u0000\u0003\u0000"+
		"\u0000\u0002\u0000\u0000\u0004\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}