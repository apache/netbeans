// Generated from java-escape by ANTLR 4.11.1
package org.netbeans.modules.rust.cargo.language.antlr4;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class TOMLParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.11.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		TOML_ERROR=1, WS=2, NL=3, COMMENT=4, L_BRACKET=5, DOUBLE_L_BRACKET=6, 
		R_BRACKET=7, DOUBLE_R_BRACKET=8, EQUALS=9, DOT=10, COMMA=11, BASIC_STRING=12, 
		LITERAL_STRING=13, UNQUOTED_KEY=14, VALUE_WS=15, L_BRACE=16, BOOLEAN=17, 
		ML_BASIC_STRING=18, ML_LITERAL_STRING=19, FLOAT=20, INF=21, NAN=22, DEC_INT=23, 
		HEX_INT=24, OCT_INT=25, BIN_INT=26, OFFSET_DATE_TIME=27, LOCAL_DATE_TIME=28, 
		LOCAL_DATE=29, LOCAL_TIME=30, INLINE_TABLE_WS=31, R_BRACE=32, ARRAY_WS=33;
	public static final int
		RULE_document = 0, RULE_expression = 1, RULE_comment = 2, RULE_key_value = 3, 
		RULE_key = 4, RULE_simple_key = 5, RULE_unquoted_key = 6, RULE_quoted_key = 7, 
		RULE_dotted_key = 8, RULE_value = 9, RULE_string = 10, RULE_integer = 11, 
		RULE_floating_point = 12, RULE_bool_ = 13, RULE_date_time = 14, RULE_inline_table = 15, 
		RULE_inner_array = 16, RULE_inline_value = 17, RULE_array_ = 18, RULE_array_values = 19, 
		RULE_comment_or_nl = 20, RULE_table = 21, RULE_standard_table = 22, RULE_array_table = 23;
	private static String[] makeRuleNames() {
		return new String[] {
			"document", "expression", "comment", "key_value", "key", "simple_key", 
			"unquoted_key", "quoted_key", "dotted_key", "value", "string", "integer", 
			"floating_point", "bool_", "date_time", "inline_table", "inner_array", 
			"inline_value", "array_", "array_values", "comment_or_nl", "table", "standard_table", 
			"array_table"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, "'['", "'[['", "']'", "']]'", "'='", "'.'", 
			"','", null, null, null, null, "'{'", null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, "'}'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "TOML_ERROR", "WS", "NL", "COMMENT", "L_BRACKET", "DOUBLE_L_BRACKET", 
			"R_BRACKET", "DOUBLE_R_BRACKET", "EQUALS", "DOT", "COMMA", "BASIC_STRING", 
			"LITERAL_STRING", "UNQUOTED_KEY", "VALUE_WS", "L_BRACE", "BOOLEAN", "ML_BASIC_STRING", 
			"ML_LITERAL_STRING", "FLOAT", "INF", "NAN", "DEC_INT", "HEX_INT", "OCT_INT", 
			"BIN_INT", "OFFSET_DATE_TIME", "LOCAL_DATE_TIME", "LOCAL_DATE", "LOCAL_TIME", 
			"INLINE_TABLE_WS", "R_BRACE", "ARRAY_WS"
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

	@Override
	public String getGrammarFileName() { return "java-escape"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public TOMLParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DocumentContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode EOF() { return getToken(TOMLParser.EOF, 0); }
		public List<TerminalNode> NL() { return getTokens(TOMLParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(TOMLParser.NL, i);
		}
		public DocumentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_document; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitDocument(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DocumentContext document() throws RecognitionException {
		DocumentContext _localctx = new DocumentContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_document);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(48);
			expression();
			setState(53);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NL) {
				{
				{
				setState(49);
				match(NL);
				setState(50);
				expression();
				}
				}
				setState(55);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(56);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionContext extends ParserRuleContext {
		public Key_valueContext key_value() {
			return getRuleContext(Key_valueContext.class,0);
		}
		public CommentContext comment() {
			return getRuleContext(CommentContext.class,0);
		}
		public TableContext table() {
			return getRuleContext(TableContext.class,0);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_expression);
		try {
			setState(65);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BASIC_STRING:
			case LITERAL_STRING:
			case UNQUOTED_KEY:
				enterOuterAlt(_localctx, 1);
				{
				setState(58);
				key_value();
				setState(59);
				comment();
				}
				break;
			case L_BRACKET:
			case DOUBLE_L_BRACKET:
				enterOuterAlt(_localctx, 2);
				{
				setState(61);
				table();
				setState(62);
				comment();
				}
				break;
			case EOF:
			case NL:
			case COMMENT:
				enterOuterAlt(_localctx, 3);
				{
				setState(64);
				comment();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CommentContext extends ParserRuleContext {
		public TerminalNode COMMENT() { return getToken(TOMLParser.COMMENT, 0); }
		public CommentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comment; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitComment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CommentContext comment() throws RecognitionException {
		CommentContext _localctx = new CommentContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_comment);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(68);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(67);
				match(COMMENT);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Key_valueContext extends ParserRuleContext {
		public KeyContext key() {
			return getRuleContext(KeyContext.class,0);
		}
		public TerminalNode EQUALS() { return getToken(TOMLParser.EQUALS, 0); }
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public Key_valueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_key_value; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitKey_value(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Key_valueContext key_value() throws RecognitionException {
		Key_valueContext _localctx = new Key_valueContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_key_value);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(70);
			key();
			setState(71);
			match(EQUALS);
			setState(72);
			value();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class KeyContext extends ParserRuleContext {
		public Simple_keyContext simple_key() {
			return getRuleContext(Simple_keyContext.class,0);
		}
		public Dotted_keyContext dotted_key() {
			return getRuleContext(Dotted_keyContext.class,0);
		}
		public KeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_key; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitKey(this);
			else return visitor.visitChildren(this);
		}
	}

	public final KeyContext key() throws RecognitionException {
		KeyContext _localctx = new KeyContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_key);
		try {
			setState(76);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(74);
				simple_key();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(75);
				dotted_key();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Simple_keyContext extends ParserRuleContext {
		public Quoted_keyContext quoted_key() {
			return getRuleContext(Quoted_keyContext.class,0);
		}
		public Unquoted_keyContext unquoted_key() {
			return getRuleContext(Unquoted_keyContext.class,0);
		}
		public Simple_keyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simple_key; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitSimple_key(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Simple_keyContext simple_key() throws RecognitionException {
		Simple_keyContext _localctx = new Simple_keyContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_simple_key);
		try {
			setState(80);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BASIC_STRING:
			case LITERAL_STRING:
				enterOuterAlt(_localctx, 1);
				{
				setState(78);
				quoted_key();
				}
				break;
			case UNQUOTED_KEY:
				enterOuterAlt(_localctx, 2);
				{
				setState(79);
				unquoted_key();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Unquoted_keyContext extends ParserRuleContext {
		public TerminalNode UNQUOTED_KEY() { return getToken(TOMLParser.UNQUOTED_KEY, 0); }
		public Unquoted_keyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unquoted_key; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitUnquoted_key(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Unquoted_keyContext unquoted_key() throws RecognitionException {
		Unquoted_keyContext _localctx = new Unquoted_keyContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_unquoted_key);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(82);
			match(UNQUOTED_KEY);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Quoted_keyContext extends ParserRuleContext {
		public TerminalNode BASIC_STRING() { return getToken(TOMLParser.BASIC_STRING, 0); }
		public TerminalNode LITERAL_STRING() { return getToken(TOMLParser.LITERAL_STRING, 0); }
		public Quoted_keyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_quoted_key; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitQuoted_key(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Quoted_keyContext quoted_key() throws RecognitionException {
		Quoted_keyContext _localctx = new Quoted_keyContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_quoted_key);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(84);
			_la = _input.LA(1);
			if ( !(_la==BASIC_STRING || _la==LITERAL_STRING) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Dotted_keyContext extends ParserRuleContext {
		public List<Simple_keyContext> simple_key() {
			return getRuleContexts(Simple_keyContext.class);
		}
		public Simple_keyContext simple_key(int i) {
			return getRuleContext(Simple_keyContext.class,i);
		}
		public List<TerminalNode> DOT() { return getTokens(TOMLParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(TOMLParser.DOT, i);
		}
		public Dotted_keyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dotted_key; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitDotted_key(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dotted_keyContext dotted_key() throws RecognitionException {
		Dotted_keyContext _localctx = new Dotted_keyContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_dotted_key);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(86);
			simple_key();
			setState(89); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(87);
				match(DOT);
				setState(88);
				simple_key();
				}
				}
				setState(91); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==DOT );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ValueContext extends ParserRuleContext {
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public IntegerContext integer() {
			return getRuleContext(IntegerContext.class,0);
		}
		public Floating_pointContext floating_point() {
			return getRuleContext(Floating_pointContext.class,0);
		}
		public Bool_Context bool_() {
			return getRuleContext(Bool_Context.class,0);
		}
		public Date_timeContext date_time() {
			return getRuleContext(Date_timeContext.class,0);
		}
		public Array_Context array_() {
			return getRuleContext(Array_Context.class,0);
		}
		public Inline_tableContext inline_table() {
			return getRuleContext(Inline_tableContext.class,0);
		}
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_value);
		try {
			setState(100);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BASIC_STRING:
			case LITERAL_STRING:
			case ML_BASIC_STRING:
			case ML_LITERAL_STRING:
				enterOuterAlt(_localctx, 1);
				{
				setState(93);
				string();
				}
				break;
			case DEC_INT:
			case HEX_INT:
			case OCT_INT:
			case BIN_INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(94);
				integer();
				}
				break;
			case FLOAT:
			case INF:
			case NAN:
				enterOuterAlt(_localctx, 3);
				{
				setState(95);
				floating_point();
				}
				break;
			case BOOLEAN:
				enterOuterAlt(_localctx, 4);
				{
				setState(96);
				bool_();
				}
				break;
			case OFFSET_DATE_TIME:
			case LOCAL_DATE_TIME:
			case LOCAL_DATE:
			case LOCAL_TIME:
				enterOuterAlt(_localctx, 5);
				{
				setState(97);
				date_time();
				}
				break;
			case L_BRACKET:
				enterOuterAlt(_localctx, 6);
				{
				setState(98);
				array_();
				}
				break;
			case L_BRACE:
				enterOuterAlt(_localctx, 7);
				{
				setState(99);
				inline_table();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StringContext extends ParserRuleContext {
		public TerminalNode BASIC_STRING() { return getToken(TOMLParser.BASIC_STRING, 0); }
		public TerminalNode ML_BASIC_STRING() { return getToken(TOMLParser.ML_BASIC_STRING, 0); }
		public TerminalNode LITERAL_STRING() { return getToken(TOMLParser.LITERAL_STRING, 0); }
		public TerminalNode ML_LITERAL_STRING() { return getToken(TOMLParser.ML_LITERAL_STRING, 0); }
		public StringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_string; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitString(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringContext string() throws RecognitionException {
		StringContext _localctx = new StringContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_string);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(102);
			_la = _input.LA(1);
			if ( !(((_la) & ~0x3f) == 0 && ((1L << _la) & 798720L) != 0) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IntegerContext extends ParserRuleContext {
		public TerminalNode DEC_INT() { return getToken(TOMLParser.DEC_INT, 0); }
		public TerminalNode HEX_INT() { return getToken(TOMLParser.HEX_INT, 0); }
		public TerminalNode OCT_INT() { return getToken(TOMLParser.OCT_INT, 0); }
		public TerminalNode BIN_INT() { return getToken(TOMLParser.BIN_INT, 0); }
		public IntegerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integer; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitInteger(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntegerContext integer() throws RecognitionException {
		IntegerContext _localctx = new IntegerContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_integer);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(104);
			_la = _input.LA(1);
			if ( !(((_la) & ~0x3f) == 0 && ((1L << _la) & 125829120L) != 0) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Floating_pointContext extends ParserRuleContext {
		public TerminalNode FLOAT() { return getToken(TOMLParser.FLOAT, 0); }
		public TerminalNode INF() { return getToken(TOMLParser.INF, 0); }
		public TerminalNode NAN() { return getToken(TOMLParser.NAN, 0); }
		public Floating_pointContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_floating_point; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitFloating_point(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Floating_pointContext floating_point() throws RecognitionException {
		Floating_pointContext _localctx = new Floating_pointContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_floating_point);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(106);
			_la = _input.LA(1);
			if ( !(((_la) & ~0x3f) == 0 && ((1L << _la) & 7340032L) != 0) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Bool_Context extends ParserRuleContext {
		public TerminalNode BOOLEAN() { return getToken(TOMLParser.BOOLEAN, 0); }
		public Bool_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bool_; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitBool_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Bool_Context bool_() throws RecognitionException {
		Bool_Context _localctx = new Bool_Context(_ctx, getState());
		enterRule(_localctx, 26, RULE_bool_);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(108);
			match(BOOLEAN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Date_timeContext extends ParserRuleContext {
		public TerminalNode OFFSET_DATE_TIME() { return getToken(TOMLParser.OFFSET_DATE_TIME, 0); }
		public TerminalNode LOCAL_DATE_TIME() { return getToken(TOMLParser.LOCAL_DATE_TIME, 0); }
		public TerminalNode LOCAL_DATE() { return getToken(TOMLParser.LOCAL_DATE, 0); }
		public TerminalNode LOCAL_TIME() { return getToken(TOMLParser.LOCAL_TIME, 0); }
		public Date_timeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_date_time; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitDate_time(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Date_timeContext date_time() throws RecognitionException {
		Date_timeContext _localctx = new Date_timeContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_date_time);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(110);
			_la = _input.LA(1);
			if ( !(((_la) & ~0x3f) == 0 && ((1L << _la) & 2013265920L) != 0) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Inline_tableContext extends ParserRuleContext {
		public TerminalNode L_BRACE() { return getToken(TOMLParser.L_BRACE, 0); }
		public List<KeyContext> key() {
			return getRuleContexts(KeyContext.class);
		}
		public KeyContext key(int i) {
			return getRuleContext(KeyContext.class,i);
		}
		public List<TerminalNode> EQUALS() { return getTokens(TOMLParser.EQUALS); }
		public TerminalNode EQUALS(int i) {
			return getToken(TOMLParser.EQUALS, i);
		}
		public List<Inline_valueContext> inline_value() {
			return getRuleContexts(Inline_valueContext.class);
		}
		public Inline_valueContext inline_value(int i) {
			return getRuleContext(Inline_valueContext.class,i);
		}
		public TerminalNode R_BRACE() { return getToken(TOMLParser.R_BRACE, 0); }
		public List<TerminalNode> COMMA() { return getTokens(TOMLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(TOMLParser.COMMA, i);
		}
		public Inline_tableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inline_table; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitInline_table(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Inline_tableContext inline_table() throws RecognitionException {
		Inline_tableContext _localctx = new Inline_tableContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_inline_table);
		try {
			int _alt;
			setState(130);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(112);
				match(L_BRACE);
				setState(113);
				key();
				setState(114);
				match(EQUALS);
				setState(115);
				inline_value();
				setState(123);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(116);
						match(COMMA);
						setState(117);
						key();
						setState(118);
						match(EQUALS);
						setState(119);
						inline_value();
						}
						} 
					}
					setState(125);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
				}
				setState(126);
				match(R_BRACE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(128);
				match(L_BRACE);
				setState(129);
				match(R_BRACE);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Inner_arrayContext extends ParserRuleContext {
		public TerminalNode L_BRACKET() { return getToken(TOMLParser.L_BRACKET, 0); }
		public TerminalNode R_BRACKET() { return getToken(TOMLParser.R_BRACKET, 0); }
		public List<Inline_valueContext> inline_value() {
			return getRuleContexts(Inline_valueContext.class);
		}
		public Inline_valueContext inline_value(int i) {
			return getRuleContext(Inline_valueContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(TOMLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(TOMLParser.COMMA, i);
		}
		public Inner_arrayContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inner_array; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitInner_array(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Inner_arrayContext inner_array() throws RecognitionException {
		Inner_arrayContext _localctx = new Inner_arrayContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_inner_array);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(132);
			match(L_BRACKET);
			setState(134);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 2147430432L) != 0) {
				{
				setState(133);
				inline_value();
				}
			}

			setState(140);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1+1 ) {
					{
					{
					setState(136);
					match(COMMA);
					setState(137);
					inline_value();
					}
					} 
				}
				setState(142);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			}
			setState(146);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1+1 ) {
					{
					{
					setState(143);
					match(COMMA);
					}
					} 
				}
				setState(148);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			}
			setState(149);
			match(R_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Inline_valueContext extends ParserRuleContext {
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public IntegerContext integer() {
			return getRuleContext(IntegerContext.class,0);
		}
		public Floating_pointContext floating_point() {
			return getRuleContext(Floating_pointContext.class,0);
		}
		public Bool_Context bool_() {
			return getRuleContext(Bool_Context.class,0);
		}
		public Date_timeContext date_time() {
			return getRuleContext(Date_timeContext.class,0);
		}
		public Inner_arrayContext inner_array() {
			return getRuleContext(Inner_arrayContext.class,0);
		}
		public Inline_tableContext inline_table() {
			return getRuleContext(Inline_tableContext.class,0);
		}
		public Inline_valueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inline_value; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitInline_value(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Inline_valueContext inline_value() throws RecognitionException {
		Inline_valueContext _localctx = new Inline_valueContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_inline_value);
		try {
			setState(158);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BASIC_STRING:
			case LITERAL_STRING:
			case ML_BASIC_STRING:
			case ML_LITERAL_STRING:
				enterOuterAlt(_localctx, 1);
				{
				setState(151);
				string();
				}
				break;
			case DEC_INT:
			case HEX_INT:
			case OCT_INT:
			case BIN_INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(152);
				integer();
				}
				break;
			case FLOAT:
			case INF:
			case NAN:
				enterOuterAlt(_localctx, 3);
				{
				setState(153);
				floating_point();
				}
				break;
			case BOOLEAN:
				enterOuterAlt(_localctx, 4);
				{
				setState(154);
				bool_();
				}
				break;
			case OFFSET_DATE_TIME:
			case LOCAL_DATE_TIME:
			case LOCAL_DATE:
			case LOCAL_TIME:
				enterOuterAlt(_localctx, 5);
				{
				setState(155);
				date_time();
				}
				break;
			case L_BRACKET:
				enterOuterAlt(_localctx, 6);
				{
				setState(156);
				inner_array();
				}
				break;
			case L_BRACE:
				enterOuterAlt(_localctx, 7);
				{
				setState(157);
				inline_table();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Array_Context extends ParserRuleContext {
		public TerminalNode L_BRACKET() { return getToken(TOMLParser.L_BRACKET, 0); }
		public Comment_or_nlContext comment_or_nl() {
			return getRuleContext(Comment_or_nlContext.class,0);
		}
		public TerminalNode R_BRACKET() { return getToken(TOMLParser.R_BRACKET, 0); }
		public Array_valuesContext array_values() {
			return getRuleContext(Array_valuesContext.class,0);
		}
		public Array_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_array_; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitArray_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Array_Context array_() throws RecognitionException {
		Array_Context _localctx = new Array_Context(_ctx, getState());
		enterRule(_localctx, 36, RULE_array_);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(160);
			match(L_BRACKET);
			setState(162);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				{
				setState(161);
				array_values();
				}
				break;
			}
			setState(164);
			comment_or_nl();
			setState(165);
			match(R_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Array_valuesContext extends ParserRuleContext {
		public List<Comment_or_nlContext> comment_or_nl() {
			return getRuleContexts(Comment_or_nlContext.class);
		}
		public Comment_or_nlContext comment_or_nl(int i) {
			return getRuleContext(Comment_or_nlContext.class,i);
		}
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(TOMLParser.COMMA, 0); }
		public Array_valuesContext array_values() {
			return getRuleContext(Array_valuesContext.class,0);
		}
		public Array_valuesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_array_values; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitArray_values(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Array_valuesContext array_values() throws RecognitionException {
		Array_valuesContext _localctx = new Array_valuesContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_array_values);
		int _la;
		try {
			setState(180);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(167);
				comment_or_nl();
				setState(168);
				value();
				setState(169);
				comment_or_nl();
				setState(170);
				match(COMMA);
				setState(171);
				comment_or_nl();
				setState(172);
				array_values();
				setState(173);
				comment_or_nl();
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(175);
				comment_or_nl();
				setState(176);
				value();
				setState(178);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(177);
					match(COMMA);
					}
				}

				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Comment_or_nlContext extends ParserRuleContext {
		public List<TerminalNode> NL() { return getTokens(TOMLParser.NL); }
		public TerminalNode NL(int i) {
			return getToken(TOMLParser.NL, i);
		}
		public List<TerminalNode> COMMENT() { return getTokens(TOMLParser.COMMENT); }
		public TerminalNode COMMENT(int i) {
			return getToken(TOMLParser.COMMENT, i);
		}
		public Comment_or_nlContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comment_or_nl; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitComment_or_nl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Comment_or_nlContext comment_or_nl() throws RecognitionException {
		Comment_or_nlContext _localctx = new Comment_or_nlContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_comment_or_nl);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(188);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(183);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==COMMENT) {
						{
						setState(182);
						match(COMMENT);
						}
					}

					setState(185);
					match(NL);
					}
					} 
				}
				setState(190);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TableContext extends ParserRuleContext {
		public Standard_tableContext standard_table() {
			return getRuleContext(Standard_tableContext.class,0);
		}
		public Array_tableContext array_table() {
			return getRuleContext(Array_tableContext.class,0);
		}
		public TableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableContext table() throws RecognitionException {
		TableContext _localctx = new TableContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_table);
		try {
			setState(193);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case L_BRACKET:
				enterOuterAlt(_localctx, 1);
				{
				setState(191);
				standard_table();
				}
				break;
			case DOUBLE_L_BRACKET:
				enterOuterAlt(_localctx, 2);
				{
				setState(192);
				array_table();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Standard_tableContext extends ParserRuleContext {
		public TerminalNode L_BRACKET() { return getToken(TOMLParser.L_BRACKET, 0); }
		public KeyContext key() {
			return getRuleContext(KeyContext.class,0);
		}
		public TerminalNode R_BRACKET() { return getToken(TOMLParser.R_BRACKET, 0); }
		public Standard_tableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_standard_table; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitStandard_table(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Standard_tableContext standard_table() throws RecognitionException {
		Standard_tableContext _localctx = new Standard_tableContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_standard_table);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(195);
			match(L_BRACKET);
			setState(196);
			key();
			setState(197);
			match(R_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Array_tableContext extends ParserRuleContext {
		public TerminalNode DOUBLE_L_BRACKET() { return getToken(TOMLParser.DOUBLE_L_BRACKET, 0); }
		public KeyContext key() {
			return getRuleContext(KeyContext.class,0);
		}
		public TerminalNode DOUBLE_R_BRACKET() { return getToken(TOMLParser.DOUBLE_R_BRACKET, 0); }
		public Array_tableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_array_table; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof TOMLParserVisitor ) return ((TOMLParserVisitor<? extends T>)visitor).visitArray_table(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Array_tableContext array_table() throws RecognitionException {
		Array_tableContext _localctx = new Array_tableContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_array_table);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(199);
			match(DOUBLE_L_BRACKET);
			setState(200);
			key();
			setState(201);
			match(DOUBLE_R_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001!\u00cc\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0005\u00004\b\u0000\n\u0000\f\u00007\t\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0003\u0001B\b\u0001\u0001\u0002\u0003\u0002"+
		"E\b\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0004"+
		"\u0001\u0004\u0003\u0004M\b\u0004\u0001\u0005\u0001\u0005\u0003\u0005"+
		"Q\b\u0005\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001"+
		"\b\u0001\b\u0004\bZ\b\b\u000b\b\f\b[\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0003\te\b\t\u0001\n\u0001\n\u0001\u000b\u0001\u000b"+
		"\u0001\f\u0001\f\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0005\u000fz\b\u000f\n\u000f\f\u000f}\t\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u0083\b\u000f\u0001"+
		"\u0010\u0001\u0010\u0003\u0010\u0087\b\u0010\u0001\u0010\u0001\u0010\u0005"+
		"\u0010\u008b\b\u0010\n\u0010\f\u0010\u008e\t\u0010\u0001\u0010\u0005\u0010"+
		"\u0091\b\u0010\n\u0010\f\u0010\u0094\t\u0010\u0001\u0010\u0001\u0010\u0001"+
		"\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001"+
		"\u0011\u0003\u0011\u009f\b\u0011\u0001\u0012\u0001\u0012\u0003\u0012\u00a3"+
		"\b\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001"+
		"\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001"+
		"\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u00b3\b\u0013\u0003\u0013\u00b5"+
		"\b\u0013\u0001\u0014\u0003\u0014\u00b8\b\u0014\u0001\u0014\u0005\u0014"+
		"\u00bb\b\u0014\n\u0014\f\u0014\u00be\t\u0014\u0001\u0015\u0001\u0015\u0003"+
		"\u0015\u00c2\b\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0003{\u008c\u0092"+
		"\u0000\u0018\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016"+
		"\u0018\u001a\u001c\u001e \"$&(*,.\u0000\u0005\u0001\u0000\f\r\u0002\u0000"+
		"\f\r\u0012\u0013\u0001\u0000\u0017\u001a\u0001\u0000\u0014\u0016\u0001"+
		"\u0000\u001b\u001e\u00d1\u00000\u0001\u0000\u0000\u0000\u0002A\u0001\u0000"+
		"\u0000\u0000\u0004D\u0001\u0000\u0000\u0000\u0006F\u0001\u0000\u0000\u0000"+
		"\bL\u0001\u0000\u0000\u0000\nP\u0001\u0000\u0000\u0000\fR\u0001\u0000"+
		"\u0000\u0000\u000eT\u0001\u0000\u0000\u0000\u0010V\u0001\u0000\u0000\u0000"+
		"\u0012d\u0001\u0000\u0000\u0000\u0014f\u0001\u0000\u0000\u0000\u0016h"+
		"\u0001\u0000\u0000\u0000\u0018j\u0001\u0000\u0000\u0000\u001al\u0001\u0000"+
		"\u0000\u0000\u001cn\u0001\u0000\u0000\u0000\u001e\u0082\u0001\u0000\u0000"+
		"\u0000 \u0084\u0001\u0000\u0000\u0000\"\u009e\u0001\u0000\u0000\u0000"+
		"$\u00a0\u0001\u0000\u0000\u0000&\u00b4\u0001\u0000\u0000\u0000(\u00bc"+
		"\u0001\u0000\u0000\u0000*\u00c1\u0001\u0000\u0000\u0000,\u00c3\u0001\u0000"+
		"\u0000\u0000.\u00c7\u0001\u0000\u0000\u000005\u0003\u0002\u0001\u0000"+
		"12\u0005\u0003\u0000\u000024\u0003\u0002\u0001\u000031\u0001\u0000\u0000"+
		"\u000047\u0001\u0000\u0000\u000053\u0001\u0000\u0000\u000056\u0001\u0000"+
		"\u0000\u000068\u0001\u0000\u0000\u000075\u0001\u0000\u0000\u000089\u0005"+
		"\u0000\u0000\u00019\u0001\u0001\u0000\u0000\u0000:;\u0003\u0006\u0003"+
		"\u0000;<\u0003\u0004\u0002\u0000<B\u0001\u0000\u0000\u0000=>\u0003*\u0015"+
		"\u0000>?\u0003\u0004\u0002\u0000?B\u0001\u0000\u0000\u0000@B\u0003\u0004"+
		"\u0002\u0000A:\u0001\u0000\u0000\u0000A=\u0001\u0000\u0000\u0000A@\u0001"+
		"\u0000\u0000\u0000B\u0003\u0001\u0000\u0000\u0000CE\u0005\u0004\u0000"+
		"\u0000DC\u0001\u0000\u0000\u0000DE\u0001\u0000\u0000\u0000E\u0005\u0001"+
		"\u0000\u0000\u0000FG\u0003\b\u0004\u0000GH\u0005\t\u0000\u0000HI\u0003"+
		"\u0012\t\u0000I\u0007\u0001\u0000\u0000\u0000JM\u0003\n\u0005\u0000KM"+
		"\u0003\u0010\b\u0000LJ\u0001\u0000\u0000\u0000LK\u0001\u0000\u0000\u0000"+
		"M\t\u0001\u0000\u0000\u0000NQ\u0003\u000e\u0007\u0000OQ\u0003\f\u0006"+
		"\u0000PN\u0001\u0000\u0000\u0000PO\u0001\u0000\u0000\u0000Q\u000b\u0001"+
		"\u0000\u0000\u0000RS\u0005\u000e\u0000\u0000S\r\u0001\u0000\u0000\u0000"+
		"TU\u0007\u0000\u0000\u0000U\u000f\u0001\u0000\u0000\u0000VY\u0003\n\u0005"+
		"\u0000WX\u0005\n\u0000\u0000XZ\u0003\n\u0005\u0000YW\u0001\u0000\u0000"+
		"\u0000Z[\u0001\u0000\u0000\u0000[Y\u0001\u0000\u0000\u0000[\\\u0001\u0000"+
		"\u0000\u0000\\\u0011\u0001\u0000\u0000\u0000]e\u0003\u0014\n\u0000^e\u0003"+
		"\u0016\u000b\u0000_e\u0003\u0018\f\u0000`e\u0003\u001a\r\u0000ae\u0003"+
		"\u001c\u000e\u0000be\u0003$\u0012\u0000ce\u0003\u001e\u000f\u0000d]\u0001"+
		"\u0000\u0000\u0000d^\u0001\u0000\u0000\u0000d_\u0001\u0000\u0000\u0000"+
		"d`\u0001\u0000\u0000\u0000da\u0001\u0000\u0000\u0000db\u0001\u0000\u0000"+
		"\u0000dc\u0001\u0000\u0000\u0000e\u0013\u0001\u0000\u0000\u0000fg\u0007"+
		"\u0001\u0000\u0000g\u0015\u0001\u0000\u0000\u0000hi\u0007\u0002\u0000"+
		"\u0000i\u0017\u0001\u0000\u0000\u0000jk\u0007\u0003\u0000\u0000k\u0019"+
		"\u0001\u0000\u0000\u0000lm\u0005\u0011\u0000\u0000m\u001b\u0001\u0000"+
		"\u0000\u0000no\u0007\u0004\u0000\u0000o\u001d\u0001\u0000\u0000\u0000"+
		"pq\u0005\u0010\u0000\u0000qr\u0003\b\u0004\u0000rs\u0005\t\u0000\u0000"+
		"s{\u0003\"\u0011\u0000tu\u0005\u000b\u0000\u0000uv\u0003\b\u0004\u0000"+
		"vw\u0005\t\u0000\u0000wx\u0003\"\u0011\u0000xz\u0001\u0000\u0000\u0000"+
		"yt\u0001\u0000\u0000\u0000z}\u0001\u0000\u0000\u0000{|\u0001\u0000\u0000"+
		"\u0000{y\u0001\u0000\u0000\u0000|~\u0001\u0000\u0000\u0000}{\u0001\u0000"+
		"\u0000\u0000~\u007f\u0005 \u0000\u0000\u007f\u0083\u0001\u0000\u0000\u0000"+
		"\u0080\u0081\u0005\u0010\u0000\u0000\u0081\u0083\u0005 \u0000\u0000\u0082"+
		"p\u0001\u0000\u0000\u0000\u0082\u0080\u0001\u0000\u0000\u0000\u0083\u001f"+
		"\u0001\u0000\u0000\u0000\u0084\u0086\u0005\u0005\u0000\u0000\u0085\u0087"+
		"\u0003\"\u0011\u0000\u0086\u0085\u0001\u0000\u0000\u0000\u0086\u0087\u0001"+
		"\u0000\u0000\u0000\u0087\u008c\u0001\u0000\u0000\u0000\u0088\u0089\u0005"+
		"\u000b\u0000\u0000\u0089\u008b\u0003\"\u0011\u0000\u008a\u0088\u0001\u0000"+
		"\u0000\u0000\u008b\u008e\u0001\u0000\u0000\u0000\u008c\u008d\u0001\u0000"+
		"\u0000\u0000\u008c\u008a\u0001\u0000\u0000\u0000\u008d\u0092\u0001\u0000"+
		"\u0000\u0000\u008e\u008c\u0001\u0000\u0000\u0000\u008f\u0091\u0005\u000b"+
		"\u0000\u0000\u0090\u008f\u0001\u0000\u0000\u0000\u0091\u0094\u0001\u0000"+
		"\u0000\u0000\u0092\u0093\u0001\u0000\u0000\u0000\u0092\u0090\u0001\u0000"+
		"\u0000\u0000\u0093\u0095\u0001\u0000\u0000\u0000\u0094\u0092\u0001\u0000"+
		"\u0000\u0000\u0095\u0096\u0005\u0007\u0000\u0000\u0096!\u0001\u0000\u0000"+
		"\u0000\u0097\u009f\u0003\u0014\n\u0000\u0098\u009f\u0003\u0016\u000b\u0000"+
		"\u0099\u009f\u0003\u0018\f\u0000\u009a\u009f\u0003\u001a\r\u0000\u009b"+
		"\u009f\u0003\u001c\u000e\u0000\u009c\u009f\u0003 \u0010\u0000\u009d\u009f"+
		"\u0003\u001e\u000f\u0000\u009e\u0097\u0001\u0000\u0000\u0000\u009e\u0098"+
		"\u0001\u0000\u0000\u0000\u009e\u0099\u0001\u0000\u0000\u0000\u009e\u009a"+
		"\u0001\u0000\u0000\u0000\u009e\u009b\u0001\u0000\u0000\u0000\u009e\u009c"+
		"\u0001\u0000\u0000\u0000\u009e\u009d\u0001\u0000\u0000\u0000\u009f#\u0001"+
		"\u0000\u0000\u0000\u00a0\u00a2\u0005\u0005\u0000\u0000\u00a1\u00a3\u0003"+
		"&\u0013\u0000\u00a2\u00a1\u0001\u0000\u0000\u0000\u00a2\u00a3\u0001\u0000"+
		"\u0000\u0000\u00a3\u00a4\u0001\u0000\u0000\u0000\u00a4\u00a5\u0003(\u0014"+
		"\u0000\u00a5\u00a6\u0005\u0007\u0000\u0000\u00a6%\u0001\u0000\u0000\u0000"+
		"\u00a7\u00a8\u0003(\u0014\u0000\u00a8\u00a9\u0003\u0012\t\u0000\u00a9"+
		"\u00aa\u0003(\u0014\u0000\u00aa\u00ab\u0005\u000b\u0000\u0000\u00ab\u00ac"+
		"\u0003(\u0014\u0000\u00ac\u00ad\u0003&\u0013\u0000\u00ad\u00ae\u0003("+
		"\u0014\u0000\u00ae\u00b5\u0001\u0000\u0000\u0000\u00af\u00b0\u0003(\u0014"+
		"\u0000\u00b0\u00b2\u0003\u0012\t\u0000\u00b1\u00b3\u0005\u000b\u0000\u0000"+
		"\u00b2\u00b1\u0001\u0000\u0000\u0000\u00b2\u00b3\u0001\u0000\u0000\u0000"+
		"\u00b3\u00b5\u0001\u0000\u0000\u0000\u00b4\u00a7\u0001\u0000\u0000\u0000"+
		"\u00b4\u00af\u0001\u0000\u0000\u0000\u00b5\'\u0001\u0000\u0000\u0000\u00b6"+
		"\u00b8\u0005\u0004\u0000\u0000\u00b7\u00b6\u0001\u0000\u0000\u0000\u00b7"+
		"\u00b8\u0001\u0000\u0000\u0000\u00b8\u00b9\u0001\u0000\u0000\u0000\u00b9"+
		"\u00bb\u0005\u0003\u0000\u0000\u00ba\u00b7\u0001\u0000\u0000\u0000\u00bb"+
		"\u00be\u0001\u0000\u0000\u0000\u00bc\u00ba\u0001\u0000\u0000\u0000\u00bc"+
		"\u00bd\u0001\u0000\u0000\u0000\u00bd)\u0001\u0000\u0000\u0000\u00be\u00bc"+
		"\u0001\u0000\u0000\u0000\u00bf\u00c2\u0003,\u0016\u0000\u00c0\u00c2\u0003"+
		".\u0017\u0000\u00c1\u00bf\u0001\u0000\u0000\u0000\u00c1\u00c0\u0001\u0000"+
		"\u0000\u0000\u00c2+\u0001\u0000\u0000\u0000\u00c3\u00c4\u0005\u0005\u0000"+
		"\u0000\u00c4\u00c5\u0003\b\u0004\u0000\u00c5\u00c6\u0005\u0007\u0000\u0000"+
		"\u00c6-\u0001\u0000\u0000\u0000\u00c7\u00c8\u0005\u0006\u0000\u0000\u00c8"+
		"\u00c9\u0003\b\u0004\u0000\u00c9\u00ca\u0005\b\u0000\u0000\u00ca/\u0001"+
		"\u0000\u0000\u0000\u00135ADLP[d{\u0082\u0086\u008c\u0092\u009e\u00a2\u00b2"+
		"\u00b4\u00b7\u00bc\u00c1";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}