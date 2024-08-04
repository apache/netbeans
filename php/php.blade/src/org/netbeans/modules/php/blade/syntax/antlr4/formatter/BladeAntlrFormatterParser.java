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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.blade.syntax.antlr4.formatter;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class BladeAntlrFormatterParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.11.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		HTML=1, PHP_CODE=2, PARAM_COMMA=3, PHP_INLINE=4, D_BLOCK_DIRECTIVE_START=5, 
		D_BLOCK_DIRECTIVE_END=6, D_SECTION=7, D_ENDSECTION=8, D_BLOCK_ALIGNED_DIRECTIVE=9, 
		NON_PARAM_DIRECTIVE=10, D_INLINE_DIRECTIVE=11, STRING=12, CONTENT_TAG_OPEN=13, 
		RAW_TAG_OPEN=14, SG_QUOTE=15, DB_QUOTE=16, HTML_CLOSE_TAG=17, HTML_COMMENT=18, 
		HTML_START_BLOCK_TAG=19, HTML_SELF_CLOSE_TAG=20, COMPONENT_TAG=21, EQ=22, 
		IDENTIFIER=23, INLINE_GT_SYMBOL=24, GT_SYMBOL=25, D_PHP=26, AT=27, WS=28, 
		NL=29, OTHER=30, D_ARG_LPAREN=31, D_ARG_RPAREN=32, PHP_EXPR=33, EXIT_EOF=34, 
		D_ARG_PARAM_LPAREN=35, D_ARG_PARAM_RPAREN=36, BL_SQ_LPAREN=37, BL_SQ_RPAREN=38, 
		BL_CURLY_LPAREN=39, BL_CURLY_RPAREN=40, D_ARG_COMMA_EL=41, BL_PHP_EXPR=42, 
		BL_EXIT_EOF=43, D_ENDPHP=44, CONTENT_TAG_CLOSE=45, CONTENT_OTHER=46, EXIT_REGULAR_ECHO_EOF=47, 
		RAW_TAG_CLOSE=48, RAW_CONTENT_OTHER=49, EXIT_RAW_ECHO_EOF=50;
	public static final int
		RULE_file = 0, RULE_statement = 1, RULE_inline_tag_statement = 2, RULE_html_close_tag = 3, 
		RULE_html_indent = 4, RULE_html_tag = 5, RULE_self_closed_tag = 6, RULE_block_start = 7, 
		RULE_block_directive_name = 8, RULE_block_end = 9, RULE_block_aligned_directive = 10, 
		RULE_inline_identable_element = 11, RULE_section_inline = 12, RULE_section_block = 13, 
		RULE_section_block_end = 14, RULE_section_block_start = 15, RULE_nl_with_space_before = 16, 
		RULE_nl_with_space = 17, RULE_static_element = 18, RULE_blade_echo = 19;
	private static String[] makeRuleNames() {
		return new String[] {
			"file", "statement", "inline_tag_statement", "html_close_tag", "html_indent", 
			"html_tag", "self_closed_tag", "block_start", "block_directive_name", 
			"block_end", "block_aligned_directive", "inline_identable_element", "section_inline", 
			"section_block", "section_block_end", "section_block_start", "nl_with_space_before", 
			"nl_with_space", "static_element", "blade_echo"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, "'{{'", "'{!!'", "'''", "'\"'", null, null, null, null, null, "'='", 
			null, "'/>'", "'>'", null, "'@'", null, null, null, null, null, null, 
			null, null, null, null, null, null, null, "','", null, null, "'@endphp'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "HTML", "PHP_CODE", "PARAM_COMMA", "PHP_INLINE", "D_BLOCK_DIRECTIVE_START", 
			"D_BLOCK_DIRECTIVE_END", "D_SECTION", "D_ENDSECTION", "D_BLOCK_ALIGNED_DIRECTIVE", 
			"NON_PARAM_DIRECTIVE", "D_INLINE_DIRECTIVE", "STRING", "CONTENT_TAG_OPEN", 
			"RAW_TAG_OPEN", "SG_QUOTE", "DB_QUOTE", "HTML_CLOSE_TAG", "HTML_COMMENT", 
			"HTML_START_BLOCK_TAG", "HTML_SELF_CLOSE_TAG", "COMPONENT_TAG", "EQ", 
			"IDENTIFIER", "INLINE_GT_SYMBOL", "GT_SYMBOL", "D_PHP", "AT", "WS", "NL", 
			"OTHER", "D_ARG_LPAREN", "D_ARG_RPAREN", "PHP_EXPR", "EXIT_EOF", "D_ARG_PARAM_LPAREN", 
			"D_ARG_PARAM_RPAREN", "BL_SQ_LPAREN", "BL_SQ_RPAREN", "BL_CURLY_LPAREN", 
			"BL_CURLY_RPAREN", "D_ARG_COMMA_EL", "BL_PHP_EXPR", "BL_EXIT_EOF", "D_ENDPHP", 
			"CONTENT_TAG_CLOSE", "CONTENT_OTHER", "EXIT_REGULAR_ECHO_EOF", "RAW_TAG_CLOSE", 
			"RAW_CONTENT_OTHER", "EXIT_RAW_ECHO_EOF"
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

	public BladeAntlrFormatterParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FileContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(BladeAntlrFormatterParser.EOF, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public FileContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_file; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).enterFile(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).exitFile(this);
		}
	}

	public final FileContext file() throws RecognitionException {
		FileContext _localctx = new FileContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_file);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(43);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((_la) & ~0x3f) == 0 && ((1L << _la) & 2000678626L) != 0) {
				{
				{
				setState(40);
				statement();
				}
				}
				setState(45);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(46);
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
	public static class StatementContext extends ParserRuleContext {
		public Html_indentContext html_indent() {
			return getRuleContext(Html_indentContext.class,0);
		}
		public Block_startContext block_start() {
			return getRuleContext(Block_startContext.class,0);
		}
		public Block_endContext block_end() {
			return getRuleContext(Block_endContext.class,0);
		}
		public Html_tagContext html_tag() {
			return getRuleContext(Html_tagContext.class,0);
		}
		public Self_closed_tagContext self_closed_tag() {
			return getRuleContext(Self_closed_tagContext.class,0);
		}
		public Section_blockContext section_block() {
			return getRuleContext(Section_blockContext.class,0);
		}
		public Inline_identable_elementContext inline_identable_element() {
			return getRuleContext(Inline_identable_elementContext.class,0);
		}
		public Block_aligned_directiveContext block_aligned_directive() {
			return getRuleContext(Block_aligned_directiveContext.class,0);
		}
		public Static_elementContext static_element() {
			return getRuleContext(Static_elementContext.class,0);
		}
		public Nl_with_space_beforeContext nl_with_space_before() {
			return getRuleContext(Nl_with_space_beforeContext.class,0);
		}
		public Blade_echoContext blade_echo() {
			return getRuleContext(Blade_echoContext.class,0);
		}
		public Html_close_tagContext html_close_tag() {
			return getRuleContext(Html_close_tagContext.class,0);
		}
		public TerminalNode SG_QUOTE() { return getToken(BladeAntlrFormatterParser.SG_QUOTE, 0); }
		public TerminalNode DB_QUOTE() { return getToken(BladeAntlrFormatterParser.DB_QUOTE, 0); }
		public TerminalNode INLINE_GT_SYMBOL() { return getToken(BladeAntlrFormatterParser.INLINE_GT_SYMBOL, 0); }
		public TerminalNode GT_SYMBOL() { return getToken(BladeAntlrFormatterParser.GT_SYMBOL, 0); }
		public TerminalNode NL() { return getToken(BladeAntlrFormatterParser.NL, 0); }
		public TerminalNode WS() { return getToken(BladeAntlrFormatterParser.WS, 0); }
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).exitStatement(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_statement);
		int _la;
		try {
			setState(65);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(48);
				html_indent();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(49);
				block_start();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(50);
				block_end();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(51);
				html_tag();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(52);
				self_closed_tag();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(53);
				section_block();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(54);
				inline_identable_element();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(55);
				block_aligned_directive();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(56);
				static_element();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(57);
				nl_with_space_before();
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(58);
				blade_echo();
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(59);
				block_end();
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(60);
				html_close_tag();
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(61);
				_la = _input.LA(1);
				if ( !(_la==SG_QUOTE || _la==DB_QUOTE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(62);
				match(INLINE_GT_SYMBOL);
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(63);
				match(GT_SYMBOL);
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 17);
				{
				setState(64);
				_la = _input.LA(1);
				if ( !(_la==WS || _la==NL) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
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
	public static class Inline_tag_statementContext extends ParserRuleContext {
		public List<TerminalNode> IDENTIFIER() { return getTokens(BladeAntlrFormatterParser.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(BladeAntlrFormatterParser.IDENTIFIER, i);
		}
		public TerminalNode EQ() { return getToken(BladeAntlrFormatterParser.EQ, 0); }
		public Block_startContext block_start() {
			return getRuleContext(Block_startContext.class,0);
		}
		public Block_endContext block_end() {
			return getRuleContext(Block_endContext.class,0);
		}
		public Blade_echoContext blade_echo() {
			return getRuleContext(Blade_echoContext.class,0);
		}
		public TerminalNode D_INLINE_DIRECTIVE() { return getToken(BladeAntlrFormatterParser.D_INLINE_DIRECTIVE, 0); }
		public TerminalNode NON_PARAM_DIRECTIVE() { return getToken(BladeAntlrFormatterParser.NON_PARAM_DIRECTIVE, 0); }
		public TerminalNode D_BLOCK_ALIGNED_DIRECTIVE() { return getToken(BladeAntlrFormatterParser.D_BLOCK_ALIGNED_DIRECTIVE, 0); }
		public TerminalNode STRING() { return getToken(BladeAntlrFormatterParser.STRING, 0); }
		public TerminalNode WS() { return getToken(BladeAntlrFormatterParser.WS, 0); }
		public TerminalNode NL() { return getToken(BladeAntlrFormatterParser.NL, 0); }
		public Inline_tag_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inline_tag_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).enterInline_tag_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).exitInline_tag_statement(this);
		}
	}

	public final Inline_tag_statementContext inline_tag_statement() throws RecognitionException {
		Inline_tag_statementContext _localctx = new Inline_tag_statementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_inline_tag_statement);
		try {
			setState(84);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(67);
				match(IDENTIFIER);
				setState(68);
				match(EQ);
				setState(69);
				match(IDENTIFIER);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(70);
				block_start();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(71);
				block_end();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(72);
				blade_echo();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(73);
				match(D_INLINE_DIRECTIVE);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(74);
				match(NON_PARAM_DIRECTIVE);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(75);
				match(D_BLOCK_ALIGNED_DIRECTIVE);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(76);
				match(IDENTIFIER);
				setState(77);
				match(EQ);
				setState(78);
				match(STRING);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(79);
				match(IDENTIFIER);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(80);
				match(STRING);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(81);
				match(EQ);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(82);
				match(WS);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(83);
				match(NL);
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
	public static class Html_close_tagContext extends ParserRuleContext {
		public TerminalNode HTML_CLOSE_TAG() { return getToken(BladeAntlrFormatterParser.HTML_CLOSE_TAG, 0); }
		public Html_close_tagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_html_close_tag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).enterHtml_close_tag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).exitHtml_close_tag(this);
		}
	}

	public final Html_close_tagContext html_close_tag() throws RecognitionException {
		Html_close_tagContext _localctx = new Html_close_tagContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_html_close_tag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(86);
			match(HTML_CLOSE_TAG);
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
	public static class Html_indentContext extends ParserRuleContext {
		public TerminalNode GT_SYMBOL() { return getToken(BladeAntlrFormatterParser.GT_SYMBOL, 0); }
		public TerminalNode NL() { return getToken(BladeAntlrFormatterParser.NL, 0); }
		public TerminalNode HTML_START_BLOCK_TAG() { return getToken(BladeAntlrFormatterParser.HTML_START_BLOCK_TAG, 0); }
		public TerminalNode COMPONENT_TAG() { return getToken(BladeAntlrFormatterParser.COMPONENT_TAG, 0); }
		public List<Inline_tag_statementContext> inline_tag_statement() {
			return getRuleContexts(Inline_tag_statementContext.class);
		}
		public Inline_tag_statementContext inline_tag_statement(int i) {
			return getRuleContext(Inline_tag_statementContext.class,i);
		}
		public List<TerminalNode> WS() { return getTokens(BladeAntlrFormatterParser.WS); }
		public TerminalNode WS(int i) {
			return getToken(BladeAntlrFormatterParser.WS, i);
		}
		public Html_indentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_html_indent; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).enterHtml_indent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).exitHtml_indent(this);
		}
	}

	public final Html_indentContext html_indent() throws RecognitionException {
		Html_indentContext _localctx = new Html_indentContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_html_indent);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(88);
			_la = _input.LA(1);
			if ( !(_la==HTML_START_BLOCK_TAG || _la==COMPONENT_TAG) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(92);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((_la) & ~0x3f) == 0 && ((1L << _la) & 817921632L) != 0) {
				{
				{
				setState(89);
				inline_tag_statement();
				}
				}
				setState(94);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(95);
			match(GT_SYMBOL);
			setState(96);
			match(NL);
			setState(100);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(97);
					match(WS);
					}
					} 
				}
				setState(102);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
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
	public static class Html_tagContext extends ParserRuleContext {
		public TerminalNode GT_SYMBOL() { return getToken(BladeAntlrFormatterParser.GT_SYMBOL, 0); }
		public TerminalNode HTML_START_BLOCK_TAG() { return getToken(BladeAntlrFormatterParser.HTML_START_BLOCK_TAG, 0); }
		public TerminalNode COMPONENT_TAG() { return getToken(BladeAntlrFormatterParser.COMPONENT_TAG, 0); }
		public List<Inline_tag_statementContext> inline_tag_statement() {
			return getRuleContexts(Inline_tag_statementContext.class);
		}
		public Inline_tag_statementContext inline_tag_statement(int i) {
			return getRuleContext(Inline_tag_statementContext.class,i);
		}
		public Html_tagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_html_tag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).enterHtml_tag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).exitHtml_tag(this);
		}
	}

	public final Html_tagContext html_tag() throws RecognitionException {
		Html_tagContext _localctx = new Html_tagContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_html_tag);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(103);
			_la = _input.LA(1);
			if ( !(_la==HTML_START_BLOCK_TAG || _la==COMPONENT_TAG) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(107);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((_la) & ~0x3f) == 0 && ((1L << _la) & 817921632L) != 0) {
				{
				{
				setState(104);
				inline_tag_statement();
				}
				}
				setState(109);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(110);
			match(GT_SYMBOL);
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
	public static class Self_closed_tagContext extends ParserRuleContext {
		public TerminalNode HTML_SELF_CLOSE_TAG() { return getToken(BladeAntlrFormatterParser.HTML_SELF_CLOSE_TAG, 0); }
		public TerminalNode INLINE_GT_SYMBOL() { return getToken(BladeAntlrFormatterParser.INLINE_GT_SYMBOL, 0); }
		public TerminalNode HTML_START_BLOCK_TAG() { return getToken(BladeAntlrFormatterParser.HTML_START_BLOCK_TAG, 0); }
		public TerminalNode COMPONENT_TAG() { return getToken(BladeAntlrFormatterParser.COMPONENT_TAG, 0); }
		public List<Inline_tag_statementContext> inline_tag_statement() {
			return getRuleContexts(Inline_tag_statementContext.class);
		}
		public Inline_tag_statementContext inline_tag_statement(int i) {
			return getRuleContext(Inline_tag_statementContext.class,i);
		}
		public Self_closed_tagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_self_closed_tag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).enterSelf_closed_tag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).exitSelf_closed_tag(this);
		}
	}

	public final Self_closed_tagContext self_closed_tag() throws RecognitionException {
		Self_closed_tagContext _localctx = new Self_closed_tagContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_self_closed_tag);
		int _la;
		try {
			setState(121);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case HTML_SELF_CLOSE_TAG:
				enterOuterAlt(_localctx, 1);
				{
				setState(112);
				match(HTML_SELF_CLOSE_TAG);
				}
				break;
			case HTML_START_BLOCK_TAG:
			case COMPONENT_TAG:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(113);
				_la = _input.LA(1);
				if ( !(_la==HTML_START_BLOCK_TAG || _la==COMPONENT_TAG) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(117);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((_la) & ~0x3f) == 0 && ((1L << _la) & 817921632L) != 0) {
					{
					{
					setState(114);
					inline_tag_statement();
					}
					}
					setState(119);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(120);
				match(INLINE_GT_SYMBOL);
				}
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
	public static class Block_startContext extends ParserRuleContext {
		public Nl_with_space_beforeContext ws_before;
		public Block_directive_nameContext block_directive_name() {
			return getRuleContext(Block_directive_nameContext.class,0);
		}
		public TerminalNode D_ARG_LPAREN() { return getToken(BladeAntlrFormatterParser.D_ARG_LPAREN, 0); }
		public TerminalNode D_ARG_RPAREN() { return getToken(BladeAntlrFormatterParser.D_ARG_RPAREN, 0); }
		public Nl_with_space_beforeContext nl_with_space_before() {
			return getRuleContext(Nl_with_space_beforeContext.class,0);
		}
		public Block_startContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block_start; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).enterBlock_start(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).exitBlock_start(this);
		}
	}

	public final Block_startContext block_start() throws RecognitionException {
		Block_startContext _localctx = new Block_startContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_block_start);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(124);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NL) {
				{
				setState(123);
				((Block_startContext)_localctx).ws_before = nl_with_space_before();
				}
			}

			setState(126);
			block_directive_name();
			setState(127);
			match(D_ARG_LPAREN);
			setState(128);
			match(D_ARG_RPAREN);
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
	public static class Block_directive_nameContext extends ParserRuleContext {
		public TerminalNode D_BLOCK_DIRECTIVE_START() { return getToken(BladeAntlrFormatterParser.D_BLOCK_DIRECTIVE_START, 0); }
		public Block_directive_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block_directive_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).enterBlock_directive_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).exitBlock_directive_name(this);
		}
	}

	public final Block_directive_nameContext block_directive_name() throws RecognitionException {
		Block_directive_nameContext _localctx = new Block_directive_nameContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_block_directive_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(130);
			match(D_BLOCK_DIRECTIVE_START);
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
	public static class Block_endContext extends ParserRuleContext {
		public TerminalNode D_BLOCK_DIRECTIVE_END() { return getToken(BladeAntlrFormatterParser.D_BLOCK_DIRECTIVE_END, 0); }
		public Block_endContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block_end; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).enterBlock_end(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).exitBlock_end(this);
		}
	}

	public final Block_endContext block_end() throws RecognitionException {
		Block_endContext _localctx = new Block_endContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_block_end);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(132);
			match(D_BLOCK_DIRECTIVE_END);
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
	public static class Block_aligned_directiveContext extends ParserRuleContext {
		public TerminalNode D_BLOCK_ALIGNED_DIRECTIVE() { return getToken(BladeAntlrFormatterParser.D_BLOCK_ALIGNED_DIRECTIVE, 0); }
		public Block_aligned_directiveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block_aligned_directive; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).enterBlock_aligned_directive(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).exitBlock_aligned_directive(this);
		}
	}

	public final Block_aligned_directiveContext block_aligned_directive() throws RecognitionException {
		Block_aligned_directiveContext _localctx = new Block_aligned_directiveContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_block_aligned_directive);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(134);
			match(D_BLOCK_ALIGNED_DIRECTIVE);
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
	public static class Inline_identable_elementContext extends ParserRuleContext {
		public TerminalNode D_INLINE_DIRECTIVE() { return getToken(BladeAntlrFormatterParser.D_INLINE_DIRECTIVE, 0); }
		public TerminalNode NON_PARAM_DIRECTIVE() { return getToken(BladeAntlrFormatterParser.NON_PARAM_DIRECTIVE, 0); }
		public Section_inlineContext section_inline() {
			return getRuleContext(Section_inlineContext.class,0);
		}
		public Blade_echoContext blade_echo() {
			return getRuleContext(Blade_echoContext.class,0);
		}
		public TerminalNode D_PHP() { return getToken(BladeAntlrFormatterParser.D_PHP, 0); }
		public TerminalNode D_ENDPHP() { return getToken(BladeAntlrFormatterParser.D_ENDPHP, 0); }
		public List<TerminalNode> PHP_CODE() { return getTokens(BladeAntlrFormatterParser.PHP_CODE); }
		public TerminalNode PHP_CODE(int i) {
			return getToken(BladeAntlrFormatterParser.PHP_CODE, i);
		}
		public Inline_identable_elementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inline_identable_element; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).enterInline_identable_element(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).exitInline_identable_element(this);
		}
	}

	public final Inline_identable_elementContext inline_identable_element() throws RecognitionException {
		Inline_identable_elementContext _localctx = new Inline_identable_elementContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_inline_identable_element);
		int _la;
		try {
			setState(147);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case D_INLINE_DIRECTIVE:
				enterOuterAlt(_localctx, 1);
				{
				setState(136);
				match(D_INLINE_DIRECTIVE);
				}
				break;
			case NON_PARAM_DIRECTIVE:
				enterOuterAlt(_localctx, 2);
				{
				setState(137);
				match(NON_PARAM_DIRECTIVE);
				}
				break;
			case D_SECTION:
				enterOuterAlt(_localctx, 3);
				{
				setState(138);
				section_inline();
				}
				break;
			case CONTENT_TAG_OPEN:
			case RAW_TAG_OPEN:
				enterOuterAlt(_localctx, 4);
				{
				setState(139);
				blade_echo();
				}
				break;
			case D_PHP:
				enterOuterAlt(_localctx, 5);
				{
				setState(140);
				match(D_PHP);
				setState(142); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(141);
					match(PHP_CODE);
					}
					}
					setState(144); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==PHP_CODE );
				setState(146);
				match(D_ENDPHP);
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
	public static class Section_inlineContext extends ParserRuleContext {
		public TerminalNode D_SECTION() { return getToken(BladeAntlrFormatterParser.D_SECTION, 0); }
		public TerminalNode D_ARG_LPAREN() { return getToken(BladeAntlrFormatterParser.D_ARG_LPAREN, 0); }
		public TerminalNode PARAM_COMMA() { return getToken(BladeAntlrFormatterParser.PARAM_COMMA, 0); }
		public TerminalNode D_ARG_RPAREN() { return getToken(BladeAntlrFormatterParser.D_ARG_RPAREN, 0); }
		public Section_inlineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_section_inline; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).enterSection_inline(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).exitSection_inline(this);
		}
	}

	public final Section_inlineContext section_inline() throws RecognitionException {
		Section_inlineContext _localctx = new Section_inlineContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_section_inline);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(149);
			match(D_SECTION);
			setState(150);
			match(D_ARG_LPAREN);
			setState(151);
			match(PARAM_COMMA);
			setState(152);
			match(D_ARG_RPAREN);
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
	public static class Section_blockContext extends ParserRuleContext {
		public Section_block_startContext section_block_start() {
			return getRuleContext(Section_block_startContext.class,0);
		}
		public Section_block_endContext section_block_end() {
			return getRuleContext(Section_block_endContext.class,0);
		}
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public Section_blockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_section_block; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).enterSection_block(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).exitSection_block(this);
		}
	}

	public final Section_blockContext section_block() throws RecognitionException {
		Section_blockContext _localctx = new Section_blockContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_section_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(154);
			section_block_start();
			setState(156); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(155);
				statement();
				}
				}
				setState(158); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( ((_la) & ~0x3f) == 0 && ((1L << _la) & 2000678626L) != 0 );
			setState(160);
			section_block_end();
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
	public static class Section_block_endContext extends ParserRuleContext {
		public TerminalNode D_ENDSECTION() { return getToken(BladeAntlrFormatterParser.D_ENDSECTION, 0); }
		public Section_block_endContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_section_block_end; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).enterSection_block_end(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).exitSection_block_end(this);
		}
	}

	public final Section_block_endContext section_block_end() throws RecognitionException {
		Section_block_endContext _localctx = new Section_block_endContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_section_block_end);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(162);
			match(D_ENDSECTION);
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
	public static class Section_block_startContext extends ParserRuleContext {
		public TerminalNode D_SECTION() { return getToken(BladeAntlrFormatterParser.D_SECTION, 0); }
		public TerminalNode D_ARG_LPAREN() { return getToken(BladeAntlrFormatterParser.D_ARG_LPAREN, 0); }
		public TerminalNode D_ARG_RPAREN() { return getToken(BladeAntlrFormatterParser.D_ARG_RPAREN, 0); }
		public Section_block_startContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_section_block_start; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).enterSection_block_start(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).exitSection_block_start(this);
		}
	}

	public final Section_block_startContext section_block_start() throws RecognitionException {
		Section_block_startContext _localctx = new Section_block_startContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_section_block_start);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(164);
			match(D_SECTION);
			setState(165);
			match(D_ARG_LPAREN);
			setState(166);
			match(D_ARG_RPAREN);
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
	public static class Nl_with_space_beforeContext extends ParserRuleContext {
		public TerminalNode NL() { return getToken(BladeAntlrFormatterParser.NL, 0); }
		public List<TerminalNode> WS() { return getTokens(BladeAntlrFormatterParser.WS); }
		public TerminalNode WS(int i) {
			return getToken(BladeAntlrFormatterParser.WS, i);
		}
		public Nl_with_space_beforeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nl_with_space_before; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).enterNl_with_space_before(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).exitNl_with_space_before(this);
		}
	}

	public final Nl_with_space_beforeContext nl_with_space_before() throws RecognitionException {
		Nl_with_space_beforeContext _localctx = new Nl_with_space_beforeContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_nl_with_space_before);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(168);
			match(NL);
			setState(172);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(169);
					match(WS);
					}
					} 
				}
				setState(174);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
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
	public static class Nl_with_spaceContext extends ParserRuleContext {
		public TerminalNode NL() { return getToken(BladeAntlrFormatterParser.NL, 0); }
		public List<TerminalNode> WS() { return getTokens(BladeAntlrFormatterParser.WS); }
		public TerminalNode WS(int i) {
			return getToken(BladeAntlrFormatterParser.WS, i);
		}
		public Nl_with_spaceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nl_with_space; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).enterNl_with_space(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).exitNl_with_space(this);
		}
	}

	public final Nl_with_spaceContext nl_with_space() throws RecognitionException {
		Nl_with_spaceContext _localctx = new Nl_with_spaceContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_nl_with_space);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(175);
			match(NL);
			setState(179);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WS) {
				{
				{
				setState(176);
				match(WS);
				}
				}
				setState(181);
				_errHandler.sync(this);
				_la = _input.LA(1);
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
	public static class Static_elementContext extends ParserRuleContext {
		public TerminalNode HTML_COMMENT() { return getToken(BladeAntlrFormatterParser.HTML_COMMENT, 0); }
		public TerminalNode HTML() { return getToken(BladeAntlrFormatterParser.HTML, 0); }
		public TerminalNode OTHER() { return getToken(BladeAntlrFormatterParser.OTHER, 0); }
		public Static_elementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_static_element; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).enterStatic_element(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).exitStatic_element(this);
		}
	}

	public final Static_elementContext static_element() throws RecognitionException {
		Static_elementContext _localctx = new Static_elementContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_static_element);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(182);
			_la = _input.LA(1);
			if ( !(((_la) & ~0x3f) == 0 && ((1L << _la) & 1074003970L) != 0) ) {
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
	public static class Blade_echoContext extends ParserRuleContext {
		public TerminalNode CONTENT_TAG_OPEN() { return getToken(BladeAntlrFormatterParser.CONTENT_TAG_OPEN, 0); }
		public TerminalNode CONTENT_TAG_CLOSE() { return getToken(BladeAntlrFormatterParser.CONTENT_TAG_CLOSE, 0); }
		public TerminalNode RAW_TAG_OPEN() { return getToken(BladeAntlrFormatterParser.RAW_TAG_OPEN, 0); }
		public TerminalNode RAW_TAG_CLOSE() { return getToken(BladeAntlrFormatterParser.RAW_TAG_CLOSE, 0); }
		public Blade_echoContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_blade_echo; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).enterBlade_echo(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof BladeAntlrFormatterParserListener ) ((BladeAntlrFormatterParserListener)listener).exitBlade_echo(this);
		}
	}

	public final Blade_echoContext blade_echo() throws RecognitionException {
		Blade_echoContext _localctx = new Blade_echoContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_blade_echo);
		try {
			setState(188);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CONTENT_TAG_OPEN:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(184);
				match(CONTENT_TAG_OPEN);
				setState(185);
				match(CONTENT_TAG_CLOSE);
				}
				}
				break;
			case RAW_TAG_OPEN:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(186);
				match(RAW_TAG_OPEN);
				setState(187);
				match(RAW_TAG_CLOSE);
				}
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

	public static final String _serializedATN =
		"\u0004\u00012\u00bf\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0001\u0000\u0005\u0000*\b\u0000\n\u0000\f\u0000"+
		"-\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0003\u0001B\b\u0001\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002U\b\u0002\u0001\u0003"+
		"\u0001\u0003\u0001\u0004\u0001\u0004\u0005\u0004[\b\u0004\n\u0004\f\u0004"+
		"^\t\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0005\u0004c\b\u0004\n\u0004"+
		"\f\u0004f\t\u0004\u0001\u0005\u0001\u0005\u0005\u0005j\b\u0005\n\u0005"+
		"\f\u0005m\t\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0005\u0006t\b\u0006\n\u0006\f\u0006w\t\u0006\u0001\u0006\u0003"+
		"\u0006z\b\u0006\u0001\u0007\u0003\u0007}\b\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\t\u0001\t\u0001\n\u0001"+
		"\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0004\u000b\u008f\b\u000b\u000b\u000b\f\u000b\u0090\u0001\u000b\u0003"+
		"\u000b\u0094\b\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\r\u0001"+
		"\r\u0004\r\u009d\b\r\u000b\r\f\r\u009e\u0001\r\u0001\r\u0001\u000e\u0001"+
		"\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u0010\u0001"+
		"\u0010\u0005\u0010\u00ab\b\u0010\n\u0010\f\u0010\u00ae\t\u0010\u0001\u0011"+
		"\u0001\u0011\u0005\u0011\u00b2\b\u0011\n\u0011\f\u0011\u00b5\t\u0011\u0001"+
		"\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0003"+
		"\u0013\u00bd\b\u0013\u0001\u0013\u0000\u0000\u0014\u0000\u0002\u0004\u0006"+
		"\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&\u0000"+
		"\u0004\u0001\u0000\u000f\u0010\u0001\u0000\u001c\u001d\u0002\u0000\u0013"+
		"\u0013\u0015\u0015\u0003\u0000\u0001\u0001\u0012\u0012\u001e\u001e\u00d6"+
		"\u0000+\u0001\u0000\u0000\u0000\u0002A\u0001\u0000\u0000\u0000\u0004T"+
		"\u0001\u0000\u0000\u0000\u0006V\u0001\u0000\u0000\u0000\bX\u0001\u0000"+
		"\u0000\u0000\ng\u0001\u0000\u0000\u0000\fy\u0001\u0000\u0000\u0000\u000e"+
		"|\u0001\u0000\u0000\u0000\u0010\u0082\u0001\u0000\u0000\u0000\u0012\u0084"+
		"\u0001\u0000\u0000\u0000\u0014\u0086\u0001\u0000\u0000\u0000\u0016\u0093"+
		"\u0001\u0000\u0000\u0000\u0018\u0095\u0001\u0000\u0000\u0000\u001a\u009a"+
		"\u0001\u0000\u0000\u0000\u001c\u00a2\u0001\u0000\u0000\u0000\u001e\u00a4"+
		"\u0001\u0000\u0000\u0000 \u00a8\u0001\u0000\u0000\u0000\"\u00af\u0001"+
		"\u0000\u0000\u0000$\u00b6\u0001\u0000\u0000\u0000&\u00bc\u0001\u0000\u0000"+
		"\u0000(*\u0003\u0002\u0001\u0000)(\u0001\u0000\u0000\u0000*-\u0001\u0000"+
		"\u0000\u0000+)\u0001\u0000\u0000\u0000+,\u0001\u0000\u0000\u0000,.\u0001"+
		"\u0000\u0000\u0000-+\u0001\u0000\u0000\u0000./\u0005\u0000\u0000\u0001"+
		"/\u0001\u0001\u0000\u0000\u00000B\u0003\b\u0004\u00001B\u0003\u000e\u0007"+
		"\u00002B\u0003\u0012\t\u00003B\u0003\n\u0005\u00004B\u0003\f\u0006\u0000"+
		"5B\u0003\u001a\r\u00006B\u0003\u0016\u000b\u00007B\u0003\u0014\n\u0000"+
		"8B\u0003$\u0012\u00009B\u0003 \u0010\u0000:B\u0003&\u0013\u0000;B\u0003"+
		"\u0012\t\u0000<B\u0003\u0006\u0003\u0000=B\u0007\u0000\u0000\u0000>B\u0005"+
		"\u0018\u0000\u0000?B\u0005\u0019\u0000\u0000@B\u0007\u0001\u0000\u0000"+
		"A0\u0001\u0000\u0000\u0000A1\u0001\u0000\u0000\u0000A2\u0001\u0000\u0000"+
		"\u0000A3\u0001\u0000\u0000\u0000A4\u0001\u0000\u0000\u0000A5\u0001\u0000"+
		"\u0000\u0000A6\u0001\u0000\u0000\u0000A7\u0001\u0000\u0000\u0000A8\u0001"+
		"\u0000\u0000\u0000A9\u0001\u0000\u0000\u0000A:\u0001\u0000\u0000\u0000"+
		"A;\u0001\u0000\u0000\u0000A<\u0001\u0000\u0000\u0000A=\u0001\u0000\u0000"+
		"\u0000A>\u0001\u0000\u0000\u0000A?\u0001\u0000\u0000\u0000A@\u0001\u0000"+
		"\u0000\u0000B\u0003\u0001\u0000\u0000\u0000CD\u0005\u0017\u0000\u0000"+
		"DE\u0005\u0016\u0000\u0000EU\u0005\u0017\u0000\u0000FU\u0003\u000e\u0007"+
		"\u0000GU\u0003\u0012\t\u0000HU\u0003&\u0013\u0000IU\u0005\u000b\u0000"+
		"\u0000JU\u0005\n\u0000\u0000KU\u0005\t\u0000\u0000LM\u0005\u0017\u0000"+
		"\u0000MN\u0005\u0016\u0000\u0000NU\u0005\f\u0000\u0000OU\u0005\u0017\u0000"+
		"\u0000PU\u0005\f\u0000\u0000QU\u0005\u0016\u0000\u0000RU\u0005\u001c\u0000"+
		"\u0000SU\u0005\u001d\u0000\u0000TC\u0001\u0000\u0000\u0000TF\u0001\u0000"+
		"\u0000\u0000TG\u0001\u0000\u0000\u0000TH\u0001\u0000\u0000\u0000TI\u0001"+
		"\u0000\u0000\u0000TJ\u0001\u0000\u0000\u0000TK\u0001\u0000\u0000\u0000"+
		"TL\u0001\u0000\u0000\u0000TO\u0001\u0000\u0000\u0000TP\u0001\u0000\u0000"+
		"\u0000TQ\u0001\u0000\u0000\u0000TR\u0001\u0000\u0000\u0000TS\u0001\u0000"+
		"\u0000\u0000U\u0005\u0001\u0000\u0000\u0000VW\u0005\u0011\u0000\u0000"+
		"W\u0007\u0001\u0000\u0000\u0000X\\\u0007\u0002\u0000\u0000Y[\u0003\u0004"+
		"\u0002\u0000ZY\u0001\u0000\u0000\u0000[^\u0001\u0000\u0000\u0000\\Z\u0001"+
		"\u0000\u0000\u0000\\]\u0001\u0000\u0000\u0000]_\u0001\u0000\u0000\u0000"+
		"^\\\u0001\u0000\u0000\u0000_`\u0005\u0019\u0000\u0000`d\u0005\u001d\u0000"+
		"\u0000ac\u0005\u001c\u0000\u0000ba\u0001\u0000\u0000\u0000cf\u0001\u0000"+
		"\u0000\u0000db\u0001\u0000\u0000\u0000de\u0001\u0000\u0000\u0000e\t\u0001"+
		"\u0000\u0000\u0000fd\u0001\u0000\u0000\u0000gk\u0007\u0002\u0000\u0000"+
		"hj\u0003\u0004\u0002\u0000ih\u0001\u0000\u0000\u0000jm\u0001\u0000\u0000"+
		"\u0000ki\u0001\u0000\u0000\u0000kl\u0001\u0000\u0000\u0000ln\u0001\u0000"+
		"\u0000\u0000mk\u0001\u0000\u0000\u0000no\u0005\u0019\u0000\u0000o\u000b"+
		"\u0001\u0000\u0000\u0000pz\u0005\u0014\u0000\u0000qu\u0007\u0002\u0000"+
		"\u0000rt\u0003\u0004\u0002\u0000sr\u0001\u0000\u0000\u0000tw\u0001\u0000"+
		"\u0000\u0000us\u0001\u0000\u0000\u0000uv\u0001\u0000\u0000\u0000vx\u0001"+
		"\u0000\u0000\u0000wu\u0001\u0000\u0000\u0000xz\u0005\u0018\u0000\u0000"+
		"yp\u0001\u0000\u0000\u0000yq\u0001\u0000\u0000\u0000z\r\u0001\u0000\u0000"+
		"\u0000{}\u0003 \u0010\u0000|{\u0001\u0000\u0000\u0000|}\u0001\u0000\u0000"+
		"\u0000}~\u0001\u0000\u0000\u0000~\u007f\u0003\u0010\b\u0000\u007f\u0080"+
		"\u0005\u001f\u0000\u0000\u0080\u0081\u0005 \u0000\u0000\u0081\u000f\u0001"+
		"\u0000\u0000\u0000\u0082\u0083\u0005\u0005\u0000\u0000\u0083\u0011\u0001"+
		"\u0000\u0000\u0000\u0084\u0085\u0005\u0006\u0000\u0000\u0085\u0013\u0001"+
		"\u0000\u0000\u0000\u0086\u0087\u0005\t\u0000\u0000\u0087\u0015\u0001\u0000"+
		"\u0000\u0000\u0088\u0094\u0005\u000b\u0000\u0000\u0089\u0094\u0005\n\u0000"+
		"\u0000\u008a\u0094\u0003\u0018\f\u0000\u008b\u0094\u0003&\u0013\u0000"+
		"\u008c\u008e\u0005\u001a\u0000\u0000\u008d\u008f\u0005\u0002\u0000\u0000"+
		"\u008e\u008d\u0001\u0000\u0000\u0000\u008f\u0090\u0001\u0000\u0000\u0000"+
		"\u0090\u008e\u0001\u0000\u0000\u0000\u0090\u0091\u0001\u0000\u0000\u0000"+
		"\u0091\u0092\u0001\u0000\u0000\u0000\u0092\u0094\u0005,\u0000\u0000\u0093"+
		"\u0088\u0001\u0000\u0000\u0000\u0093\u0089\u0001\u0000\u0000\u0000\u0093"+
		"\u008a\u0001\u0000\u0000\u0000\u0093\u008b\u0001\u0000\u0000\u0000\u0093"+
		"\u008c\u0001\u0000\u0000\u0000\u0094\u0017\u0001\u0000\u0000\u0000\u0095"+
		"\u0096\u0005\u0007\u0000\u0000\u0096\u0097\u0005\u001f\u0000\u0000\u0097"+
		"\u0098\u0005\u0003\u0000\u0000\u0098\u0099\u0005 \u0000\u0000\u0099\u0019"+
		"\u0001\u0000\u0000\u0000\u009a\u009c\u0003\u001e\u000f\u0000\u009b\u009d"+
		"\u0003\u0002\u0001\u0000\u009c\u009b\u0001\u0000\u0000\u0000\u009d\u009e"+
		"\u0001\u0000\u0000\u0000\u009e\u009c\u0001\u0000\u0000\u0000\u009e\u009f"+
		"\u0001\u0000\u0000\u0000\u009f\u00a0\u0001\u0000\u0000\u0000\u00a0\u00a1"+
		"\u0003\u001c\u000e\u0000\u00a1\u001b\u0001\u0000\u0000\u0000\u00a2\u00a3"+
		"\u0005\b\u0000\u0000\u00a3\u001d\u0001\u0000\u0000\u0000\u00a4\u00a5\u0005"+
		"\u0007\u0000\u0000\u00a5\u00a6\u0005\u001f\u0000\u0000\u00a6\u00a7\u0005"+
		" \u0000\u0000\u00a7\u001f\u0001\u0000\u0000\u0000\u00a8\u00ac\u0005\u001d"+
		"\u0000\u0000\u00a9\u00ab\u0005\u001c\u0000\u0000\u00aa\u00a9\u0001\u0000"+
		"\u0000\u0000\u00ab\u00ae\u0001\u0000\u0000\u0000\u00ac\u00aa\u0001\u0000"+
		"\u0000\u0000\u00ac\u00ad\u0001\u0000\u0000\u0000\u00ad!\u0001\u0000\u0000"+
		"\u0000\u00ae\u00ac\u0001\u0000\u0000\u0000\u00af\u00b3\u0005\u001d\u0000"+
		"\u0000\u00b0\u00b2\u0005\u001c\u0000\u0000\u00b1\u00b0\u0001\u0000\u0000"+
		"\u0000\u00b2\u00b5\u0001\u0000\u0000\u0000\u00b3\u00b1\u0001\u0000\u0000"+
		"\u0000\u00b3\u00b4\u0001\u0000\u0000\u0000\u00b4#\u0001\u0000\u0000\u0000"+
		"\u00b5\u00b3\u0001\u0000\u0000\u0000\u00b6\u00b7\u0007\u0003\u0000\u0000"+
		"\u00b7%\u0001\u0000\u0000\u0000\u00b8\u00b9\u0005\r\u0000\u0000\u00b9"+
		"\u00bd\u0005-\u0000\u0000\u00ba\u00bb\u0005\u000e\u0000\u0000\u00bb\u00bd"+
		"\u00050\u0000\u0000\u00bc\u00b8\u0001\u0000\u0000\u0000\u00bc\u00ba\u0001"+
		"\u0000\u0000\u0000\u00bd\'\u0001\u0000\u0000\u0000\u000f+AT\\dkuy|\u0090"+
		"\u0093\u009e\u00ac\u00b3\u00bc";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}