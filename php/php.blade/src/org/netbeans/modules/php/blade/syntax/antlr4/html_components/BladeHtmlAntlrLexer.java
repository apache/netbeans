// Generated from BladeHtmlAntlrLexer.g4 by ANTLR 4.13.0

  package org.netbeans.modules.php.blade.syntax.antlr4.html_components;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class BladeHtmlAntlrLexer extends LexerAdaptor {
	static { RuntimeMetaData.checkVersion("4.13.0", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		HTML_COMPONENT_OPEN_TAG=1, COMPONENT_ATTRIBUTE=2, GT=3, BLADE_COMMENT_START=4, 
		BLADE_TAG_ESCAPE=5, CONTENT_TAG_OPEN=6, CONTENT_TAG_CLOSE=7, RAW_TAG_OPEN=8, 
		RAW_TAG_CLOSE=9, WS=10, TAG_PART=11, OTHER=12, BLADE_COMMENT_END=13, BLADE_COMMENT_MORE=14, 
		BLADE_COMMENT_EOF=15;
	public static final int
		INSIDE_BLADE_COMMENT=1;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE", "INSIDE_BLADE_COMMENT"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"Identifier", "HTML_COMPONENT_OPEN_TAG", "COMPONENT_ATTRIBUTE", "GT", 
			"BLADE_COMMENT_START", "BLADE_TAG_ESCAPE", "CONTENT_TAG_OPEN", "CONTENT_TAG_CLOSE", 
			"RAW_TAG_OPEN", "RAW_TAG_CLOSE", "WS", "TAG_PART", "OTHER", "BLADE_COMMENT_END", 
			"BLADE_COMMENT_MORE", "BLADE_COMMENT_EOF"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, "'>'", "'{{--'", null, null, null, "'{!!'", "'!!}'", 
			null, null, null, "'--}}'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "HTML_COMPONENT_OPEN_TAG", "COMPONENT_ATTRIBUTE", "GT", "BLADE_COMMENT_START", 
			"BLADE_TAG_ESCAPE", "CONTENT_TAG_OPEN", "CONTENT_TAG_CLOSE", "RAW_TAG_OPEN", 
			"RAW_TAG_CLOSE", "WS", "TAG_PART", "OTHER", "BLADE_COMMENT_END", "BLADE_COMMENT_MORE", 
			"BLADE_COMMENT_EOF"
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


	    boolean tagOpened = false;
	    boolean insideTag = false;
	    int contentTagBalance = 0;
	    int rawTagBalance = 0;


	public BladeHtmlAntlrLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "BladeHtmlAntlrLexer.g4"; }

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
	public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
		switch (ruleIndex) {
		case 1:
			HTML_COMPONENT_OPEN_TAG_action((RuleContext)_localctx, actionIndex);
			break;
		case 3:
			GT_action((RuleContext)_localctx, actionIndex);
			break;
		case 6:
			CONTENT_TAG_OPEN_action((RuleContext)_localctx, actionIndex);
			break;
		case 7:
			CONTENT_TAG_CLOSE_action((RuleContext)_localctx, actionIndex);
			break;
		case 8:
			RAW_TAG_OPEN_action((RuleContext)_localctx, actionIndex);
			break;
		case 9:
			RAW_TAG_CLOSE_action((RuleContext)_localctx, actionIndex);
			break;
		}
	}
	private void HTML_COMPONENT_OPEN_TAG_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0:
			tagOpened = true;insideTag = true;
			break;
		}
	}
	private void GT_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 1:
			insideTag = false;
			break;
		}
	}
	private void CONTENT_TAG_OPEN_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 2:
			contentTagBalance++;
			break;
		}
	}
	private void CONTENT_TAG_CLOSE_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 3:
			contentTagBalance--;
			break;
		}
	}
	private void RAW_TAG_OPEN_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 4:
			rawTagBalance++;
			break;
		}
	}
	private void RAW_TAG_CLOSE_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 5:
			rawTagBalance--;
			break;
		}
	}
	@Override
	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 2:
			return COMPONENT_ATTRIBUTE_sempred((RuleContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean COMPONENT_ATTRIBUTE_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return insideTag == true && contentTagBalance == 0 && rawTagBalance == 0;
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0000\u000f\u0093\u0006\uffff\uffff\u0006\uffff\uffff\u0002\u0000"+
		"\u0007\u0000\u0002\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003"+
		"\u0007\u0003\u0002\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006"+
		"\u0007\u0006\u0002\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002"+
		"\n\u0007\n\u0002\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002"+
		"\u000e\u0007\u000e\u0002\u000f\u0007\u000f\u0001\u0000\u0001\u0000\u0005"+
		"\u0000%\b\u0000\n\u0000\f\u0000(\t\u0000\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003"+
		"\u00012\b\u0001\u0001\u0001\u0003\u00015\b\u0001\u0003\u00017\b\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005"+
		"\u0001\u0005\u0004\u0005L\b\u0005\u000b\u0005\f\u0005M\u0001\u0005\u0001"+
		"\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b"+
		"\u0001\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\n\u0004"+
		"\nm\b\n\u000b\n\f\nn\u0001\n\u0004\nr\b\n\u000b\n\f\ns\u0003\nv\b\n\u0001"+
		"\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b}\b\u000b\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0000\u0000\u0010"+
		"\u0002\u0000\u0004\u0001\u0006\u0002\b\u0003\n\u0004\f\u0005\u000e\u0006"+
		"\u0010\u0007\u0012\b\u0014\t\u0016\n\u0018\u000b\u001a\f\u001c\r\u001e"+
		"\u000e \u000f\u0002\u0000\u0001\u0004\u0003\u0000AZaz\u0080\u8000\ufffe"+
		"\u0006\u0000--09AZ__az\u0080\u8000\ufffe\u0002\u0000XXxx\u0002\u0000\n"+
		"\n\r\r\u0099\u0000\u0004\u0001\u0000\u0000\u0000\u0000\u0006\u0001\u0000"+
		"\u0000\u0000\u0000\b\u0001\u0000\u0000\u0000\u0000\n\u0001\u0000\u0000"+
		"\u0000\u0000\f\u0001\u0000\u0000\u0000\u0000\u000e\u0001\u0000\u0000\u0000"+
		"\u0000\u0010\u0001\u0000\u0000\u0000\u0000\u0012\u0001\u0000\u0000\u0000"+
		"\u0000\u0014\u0001\u0000\u0000\u0000\u0000\u0016\u0001\u0000\u0000\u0000"+
		"\u0000\u0018\u0001\u0000\u0000\u0000\u0000\u001a\u0001\u0000\u0000\u0000"+
		"\u0001\u001c\u0001\u0000\u0000\u0000\u0001\u001e\u0001\u0000\u0000\u0000"+
		"\u0001 \u0001\u0000\u0000\u0000\u0002\"\u0001\u0000\u0000\u0000\u0004"+
		")\u0001\u0000\u0000\u0000\u0006:\u0001\u0000\u0000\u0000\b>\u0001\u0000"+
		"\u0000\u0000\nA\u0001\u0000\u0000\u0000\fI\u0001\u0000\u0000\u0000\u000e"+
		"Q\u0001\u0000\u0000\u0000\u0010X\u0001\u0000\u0000\u0000\u0012_\u0001"+
		"\u0000\u0000\u0000\u0014e\u0001\u0000\u0000\u0000\u0016u\u0001\u0000\u0000"+
		"\u0000\u0018|\u0001\u0000\u0000\u0000\u001a~\u0001\u0000\u0000\u0000\u001c"+
		"\u0082\u0001\u0000\u0000\u0000\u001e\u008a\u0001\u0000\u0000\u0000 \u008e"+
		"\u0001\u0000\u0000\u0000\"&\u0007\u0000\u0000\u0000#%\u0007\u0001\u0000"+
		"\u0000$#\u0001\u0000\u0000\u0000%(\u0001\u0000\u0000\u0000&$\u0001\u0000"+
		"\u0000\u0000&\'\u0001\u0000\u0000\u0000\'\u0003\u0001\u0000\u0000\u0000"+
		"(&\u0001\u0000\u0000\u0000)*\u0005<\u0000\u0000*+\u0007\u0002\u0000\u0000"+
		"+,\u0005-\u0000\u0000,6\u0001\u0000\u0000\u0000-4\u0003\u0002\u0000\u0000"+
		"./\u0005:\u0000\u0000/2\u0005:\u0000\u000002\u0005.\u0000\u00001.\u0001"+
		"\u0000\u0000\u000010\u0001\u0000\u0000\u000023\u0001\u0000\u0000\u0000"+
		"35\u0003\u0002\u0000\u000041\u0001\u0000\u0000\u000045\u0001\u0000\u0000"+
		"\u000057\u0001\u0000\u0000\u00006-\u0001\u0000\u0000\u000067\u0001\u0000"+
		"\u0000\u000078\u0001\u0000\u0000\u000089\u0006\u0001\u0000\u00009\u0005"+
		"\u0001\u0000\u0000\u0000:;\u0004\u0002\u0000\u0000;<\u0005:\u0000\u0000"+
		"<=\u0003\u0002\u0000\u0000=\u0007\u0001\u0000\u0000\u0000>?\u0005>\u0000"+
		"\u0000?@\u0006\u0003\u0001\u0000@\t\u0001\u0000\u0000\u0000AB\u0005{\u0000"+
		"\u0000BC\u0005{\u0000\u0000CD\u0005-\u0000\u0000DE\u0005-\u0000\u0000"+
		"EF\u0001\u0000\u0000\u0000FG\u0006\u0004\u0002\u0000GH\u0006\u0004\u0003"+
		"\u0000H\u000b\u0001\u0000\u0000\u0000IK\u0005@\u0000\u0000JL\u0005{\u0000"+
		"\u0000KJ\u0001\u0000\u0000\u0000LM\u0001\u0000\u0000\u0000MK\u0001\u0000"+
		"\u0000\u0000MN\u0001\u0000\u0000\u0000NO\u0001\u0000\u0000\u0000OP\u0006"+
		"\u0005\u0003\u0000P\r\u0001\u0000\u0000\u0000QR\u0005{\u0000\u0000RS\u0005"+
		"{\u0000\u0000ST\u0001\u0000\u0000\u0000TU\u0006\u0006\u0004\u0000UV\u0001"+
		"\u0000\u0000\u0000VW\u0006\u0006\u0003\u0000W\u000f\u0001\u0000\u0000"+
		"\u0000XY\u0005}\u0000\u0000YZ\u0005}\u0000\u0000Z[\u0001\u0000\u0000\u0000"+
		"[\\\u0006\u0007\u0005\u0000\\]\u0001\u0000\u0000\u0000]^\u0006\u0007\u0003"+
		"\u0000^\u0011\u0001\u0000\u0000\u0000_`\u0005{\u0000\u0000`a\u0005!\u0000"+
		"\u0000ab\u0005!\u0000\u0000bc\u0001\u0000\u0000\u0000cd\u0006\b\u0006"+
		"\u0000d\u0013\u0001\u0000\u0000\u0000ef\u0005!\u0000\u0000fg\u0005!\u0000"+
		"\u0000gh\u0005}\u0000\u0000hi\u0001\u0000\u0000\u0000ij\u0006\t\u0007"+
		"\u0000j\u0015\u0001\u0000\u0000\u0000km\u0005 \u0000\u0000lk\u0001\u0000"+
		"\u0000\u0000mn\u0001\u0000\u0000\u0000nl\u0001\u0000\u0000\u0000no\u0001"+
		"\u0000\u0000\u0000ov\u0001\u0000\u0000\u0000pr\u0007\u0003\u0000\u0000"+
		"qp\u0001\u0000\u0000\u0000rs\u0001\u0000\u0000\u0000sq\u0001\u0000\u0000"+
		"\u0000st\u0001\u0000\u0000\u0000tv\u0001\u0000\u0000\u0000ul\u0001\u0000"+
		"\u0000\u0000uq\u0001\u0000\u0000\u0000vw\u0001\u0000\u0000\u0000wx\u0006"+
		"\n\u0003\u0000x\u0017\u0001\u0000\u0000\u0000y}\u0005!\u0000\u0000z{\u0005"+
		"!\u0000\u0000{}\u0005!\u0000\u0000|y\u0001\u0000\u0000\u0000|z\u0001\u0000"+
		"\u0000\u0000}\u0019\u0001\u0000\u0000\u0000~\u007f\t\u0000\u0000\u0000"+
		"\u007f\u0080\u0001\u0000\u0000\u0000\u0080\u0081\u0006\f\u0003\u0000\u0081"+
		"\u001b\u0001\u0000\u0000\u0000\u0082\u0083\u0005-\u0000\u0000\u0083\u0084"+
		"\u0005-\u0000\u0000\u0084\u0085\u0005}\u0000\u0000\u0085\u0086\u0005}"+
		"\u0000\u0000\u0086\u0087\u0001\u0000\u0000\u0000\u0087\u0088\u0006\r\b"+
		"\u0000\u0088\u0089\u0006\r\u0003\u0000\u0089\u001d\u0001\u0000\u0000\u0000"+
		"\u008a\u008b\t\u0000\u0000\u0000\u008b\u008c\u0001\u0000\u0000\u0000\u008c"+
		"\u008d\u0006\u000e\u0003\u0000\u008d\u001f\u0001\u0000\u0000\u0000\u008e"+
		"\u008f\u0005\u0000\u0000\u0001\u008f\u0090\u0001\u0000\u0000\u0000\u0090"+
		"\u0091\u0006\u000f\b\u0000\u0091\u0092\u0006\u000f\u0003\u0000\u0092!"+
		"\u0001\u0000\u0000\u0000\u000b\u0000\u0001&146Mnsu|\t\u0001\u0001\u0000"+
		"\u0001\u0003\u0001\u0005\u0001\u0000\u0006\u0000\u0000\u0001\u0006\u0002"+
		"\u0001\u0007\u0003\u0001\b\u0004\u0001\t\u0005\u0004\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}