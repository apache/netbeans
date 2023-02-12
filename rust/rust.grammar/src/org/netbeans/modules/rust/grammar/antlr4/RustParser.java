// Generated from java-escape by ANTLR 4.11.1
package org.netbeans.modules.rust.grammar.antlr4;

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

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class RustParser extends RustParserBase {
	static { RuntimeMetaData.checkVersion("4.11.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		KW_AS=1, KW_BREAK=2, KW_CONST=3, KW_CONTINUE=4, KW_CRATE=5, KW_ELSE=6, 
		KW_ENUM=7, KW_EXTERN=8, KW_FALSE=9, KW_FN=10, KW_FOR=11, KW_IF=12, KW_IMPL=13, 
		KW_IN=14, KW_LET=15, KW_LOOP=16, KW_MATCH=17, KW_MOD=18, KW_MOVE=19, KW_MUT=20, 
		KW_PUB=21, KW_REF=22, KW_RETURN=23, KW_SELFVALUE=24, KW_SELFTYPE=25, KW_STATIC=26, 
		KW_STRUCT=27, KW_SUPER=28, KW_TRAIT=29, KW_TRUE=30, KW_TYPE=31, KW_UNSAFE=32, 
		KW_USE=33, KW_WHERE=34, KW_WHILE=35, KW_ASYNC=36, KW_AWAIT=37, KW_DYN=38, 
		KW_ABSTRACT=39, KW_BECOME=40, KW_BOX=41, KW_DO=42, KW_FINAL=43, KW_MACRO=44, 
		KW_OVERRIDE=45, KW_PRIV=46, KW_TYPEOF=47, KW_UNSIZED=48, KW_VIRTUAL=49, 
		KW_YIELD=50, KW_TRY=51, KW_UNION=52, KW_STATICLIFETIME=53, KW_MACRORULES=54, 
		KW_UNDERLINELIFETIME=55, KW_DOLLARCRATE=56, NON_KEYWORD_IDENTIFIER=57, 
		RAW_IDENTIFIER=58, LINE_COMMENT=59, INNER_BLOCK_DOC=60, BLOCK_COMMENT=61, 
		INNER_LINE_DOC=62, OUTER_LINE_DOC=63, SHEBANG=64, WHITESPACE=65, NEWLINE=66, 
		CHAR_LITERAL=67, STRING_LITERAL=68, RAW_STRING_LITERAL=69, BYTE_LITERAL=70, 
		BYTE_STRING_LITERAL=71, RAW_BYTE_STRING_LITERAL=72, INTEGER_LITERAL=73, 
		DEC_LITERAL=74, HEX_LITERAL=75, OCT_LITERAL=76, BIN_LITERAL=77, FLOAT_LITERAL=78, 
		LIFETIME_OR_LABEL=79, PLUS=80, MINUS=81, STAR=82, SLASH=83, PERCENT=84, 
		CARET=85, NOT=86, AND=87, OR=88, ANDAND=89, OROR=90, PLUSEQ=91, MINUSEQ=92, 
		STAREQ=93, SLASHEQ=94, PERCENTEQ=95, CARETEQ=96, ANDEQ=97, OREQ=98, SHLEQ=99, 
		SHREQ=100, EQ=101, EQEQ=102, NE=103, GT=104, LT=105, GE=106, LE=107, AT=108, 
		UNDERSCORE=109, DOT=110, DOTDOT=111, DOTDOTDOT=112, DOTDOTEQ=113, COMMA=114, 
		SEMI=115, COLON=116, PATHSEP=117, RARROW=118, FATARROW=119, POUND=120, 
		DOLLAR=121, QUESTION=122, LCURLYBRACE=123, RCURLYBRACE=124, LSQUAREBRACKET=125, 
		RSQUAREBRACKET=126, LPAREN=127, RPAREN=128;
	public static final int
		RULE_crate = 0, RULE_macroInvocation = 1, RULE_delimTokenTree = 2, RULE_tokenTree = 3, 
		RULE_tokenTreeToken = 4, RULE_macroInvocationSemi = 5, RULE_macroRulesDefinition = 6, 
		RULE_macroRulesDef = 7, RULE_macroRules = 8, RULE_macroRule = 9, RULE_macroMatcher = 10, 
		RULE_macroMatch = 11, RULE_macroMatchToken = 12, RULE_macroFragSpec = 13, 
		RULE_macroRepSep = 14, RULE_macroRepOp = 15, RULE_macroTranscriber = 16, 
		RULE_item = 17, RULE_visItem = 18, RULE_macroItem = 19, RULE_module = 20, 
		RULE_externCrate = 21, RULE_crateRef = 22, RULE_asClause = 23, RULE_useDeclaration = 24, 
		RULE_useTree = 25, RULE_function_ = 26, RULE_functionQualifiers = 27, 
		RULE_abi = 28, RULE_functionParameters = 29, RULE_selfParam = 30, RULE_shorthandSelf = 31, 
		RULE_typedSelf = 32, RULE_functionParam = 33, RULE_functionParamPattern = 34, 
		RULE_functionReturnType = 35, RULE_typeAlias = 36, RULE_struct_ = 37, 
		RULE_structStruct = 38, RULE_tupleStruct = 39, RULE_structFields = 40, 
		RULE_structField = 41, RULE_tupleFields = 42, RULE_tupleField = 43, RULE_enumeration = 44, 
		RULE_enumItems = 45, RULE_enumItem = 46, RULE_enumItemTuple = 47, RULE_enumItemStruct = 48, 
		RULE_enumItemDiscriminant = 49, RULE_union_ = 50, RULE_constantItem = 51, 
		RULE_staticItem = 52, RULE_trait_ = 53, RULE_implementation = 54, RULE_inherentImpl = 55, 
		RULE_traitImpl = 56, RULE_externBlock = 57, RULE_externalItem = 58, RULE_genericParams = 59, 
		RULE_genericParam = 60, RULE_lifetimeParam = 61, RULE_typeParam = 62, 
		RULE_constParam = 63, RULE_whereClause = 64, RULE_whereClauseItem = 65, 
		RULE_lifetimeWhereClauseItem = 66, RULE_typeBoundWhereClauseItem = 67, 
		RULE_forLifetimes = 68, RULE_associatedItem = 69, RULE_innerAttribute = 70, 
		RULE_outerAttribute = 71, RULE_attr = 72, RULE_attrInput = 73, RULE_statement = 74, 
		RULE_letStatement = 75, RULE_expressionStatement = 76, RULE_expression = 77, 
		RULE_comparisonOperator = 78, RULE_compoundAssignOperator = 79, RULE_expressionWithBlock = 80, 
		RULE_literalExpression = 81, RULE_pathExpression = 82, RULE_blockExpression = 83, 
		RULE_statements = 84, RULE_asyncBlockExpression = 85, RULE_unsafeBlockExpression = 86, 
		RULE_arrayElements = 87, RULE_tupleElements = 88, RULE_tupleIndex = 89, 
		RULE_structExpression = 90, RULE_structExprStruct = 91, RULE_structExprFields = 92, 
		RULE_structExprField = 93, RULE_structBase = 94, RULE_structExprTuple = 95, 
		RULE_structExprUnit = 96, RULE_enumerationVariantExpression = 97, RULE_enumExprStruct = 98, 
		RULE_enumExprFields = 99, RULE_enumExprField = 100, RULE_enumExprTuple = 101, 
		RULE_enumExprFieldless = 102, RULE_callParams = 103, RULE_closureExpression = 104, 
		RULE_closureParameters = 105, RULE_closureParam = 106, RULE_loopExpression = 107, 
		RULE_infiniteLoopExpression = 108, RULE_predicateLoopExpression = 109, 
		RULE_predicatePatternLoopExpression = 110, RULE_iteratorLoopExpression = 111, 
		RULE_loopLabel = 112, RULE_ifExpression = 113, RULE_ifLetExpression = 114, 
		RULE_matchExpression = 115, RULE_matchArms = 116, RULE_matchArmExpression = 117, 
		RULE_matchArm = 118, RULE_matchArmGuard = 119, RULE_pattern = 120, RULE_patternNoTopAlt = 121, 
		RULE_patternWithoutRange = 122, RULE_literalPattern = 123, RULE_identifierPattern = 124, 
		RULE_wildcardPattern = 125, RULE_restPattern = 126, RULE_rangePattern = 127, 
		RULE_rangePatternBound = 128, RULE_referencePattern = 129, RULE_structPattern = 130, 
		RULE_structPatternElements = 131, RULE_structPatternFields = 132, RULE_structPatternField = 133, 
		RULE_structPatternEtCetera = 134, RULE_tupleStructPattern = 135, RULE_tupleStructItems = 136, 
		RULE_tuplePattern = 137, RULE_tuplePatternItems = 138, RULE_groupedPattern = 139, 
		RULE_slicePattern = 140, RULE_slicePatternItems = 141, RULE_pathPattern = 142, 
		RULE_type_ = 143, RULE_typeNoBounds = 144, RULE_parenthesizedType = 145, 
		RULE_neverType = 146, RULE_tupleType = 147, RULE_arrayType = 148, RULE_sliceType = 149, 
		RULE_referenceType = 150, RULE_rawPointerType = 151, RULE_bareFunctionType = 152, 
		RULE_functionTypeQualifiers = 153, RULE_bareFunctionReturnType = 154, 
		RULE_functionParametersMaybeNamedVariadic = 155, RULE_maybeNamedFunctionParameters = 156, 
		RULE_maybeNamedParam = 157, RULE_maybeNamedFunctionParametersVariadic = 158, 
		RULE_traitObjectType = 159, RULE_traitObjectTypeOneBound = 160, RULE_implTraitType = 161, 
		RULE_implTraitTypeOneBound = 162, RULE_inferredType = 163, RULE_typeParamBounds = 164, 
		RULE_typeParamBound = 165, RULE_traitBound = 166, RULE_lifetimeBounds = 167, 
		RULE_lifetime = 168, RULE_simplePath = 169, RULE_simplePathSegment = 170, 
		RULE_pathInExpression = 171, RULE_pathExprSegment = 172, RULE_pathIdentSegment = 173, 
		RULE_genericArgs = 174, RULE_genericArg = 175, RULE_genericArgsConst = 176, 
		RULE_genericArgsLifetimes = 177, RULE_genericArgsTypes = 178, RULE_genericArgsBindings = 179, 
		RULE_genericArgsBinding = 180, RULE_qualifiedPathInExpression = 181, RULE_qualifiedPathType = 182, 
		RULE_qualifiedPathInType = 183, RULE_typePath = 184, RULE_typePathSegment = 185, 
		RULE_typePathFn = 186, RULE_typePathInputs = 187, RULE_visibility = 188, 
		RULE_identifier = 189, RULE_keyword = 190, RULE_macroIdentifierLikeToken = 191, 
		RULE_macroLiteralToken = 192, RULE_macroPunctuationToken = 193, RULE_shl = 194, 
		RULE_shr = 195;
	private static String[] makeRuleNames() {
		return new String[] {
			"crate", "macroInvocation", "delimTokenTree", "tokenTree", "tokenTreeToken", 
			"macroInvocationSemi", "macroRulesDefinition", "macroRulesDef", "macroRules", 
			"macroRule", "macroMatcher", "macroMatch", "macroMatchToken", "macroFragSpec", 
			"macroRepSep", "macroRepOp", "macroTranscriber", "item", "visItem", "macroItem", 
			"module", "externCrate", "crateRef", "asClause", "useDeclaration", "useTree", 
			"function_", "functionQualifiers", "abi", "functionParameters", "selfParam", 
			"shorthandSelf", "typedSelf", "functionParam", "functionParamPattern", 
			"functionReturnType", "typeAlias", "struct_", "structStruct", "tupleStruct", 
			"structFields", "structField", "tupleFields", "tupleField", "enumeration", 
			"enumItems", "enumItem", "enumItemTuple", "enumItemStruct", "enumItemDiscriminant", 
			"union_", "constantItem", "staticItem", "trait_", "implementation", "inherentImpl", 
			"traitImpl", "externBlock", "externalItem", "genericParams", "genericParam", 
			"lifetimeParam", "typeParam", "constParam", "whereClause", "whereClauseItem", 
			"lifetimeWhereClauseItem", "typeBoundWhereClauseItem", "forLifetimes", 
			"associatedItem", "innerAttribute", "outerAttribute", "attr", "attrInput", 
			"statement", "letStatement", "expressionStatement", "expression", "comparisonOperator", 
			"compoundAssignOperator", "expressionWithBlock", "literalExpression", 
			"pathExpression", "blockExpression", "statements", "asyncBlockExpression", 
			"unsafeBlockExpression", "arrayElements", "tupleElements", "tupleIndex", 
			"structExpression", "structExprStruct", "structExprFields", "structExprField", 
			"structBase", "structExprTuple", "structExprUnit", "enumerationVariantExpression", 
			"enumExprStruct", "enumExprFields", "enumExprField", "enumExprTuple", 
			"enumExprFieldless", "callParams", "closureExpression", "closureParameters", 
			"closureParam", "loopExpression", "infiniteLoopExpression", "predicateLoopExpression", 
			"predicatePatternLoopExpression", "iteratorLoopExpression", "loopLabel", 
			"ifExpression", "ifLetExpression", "matchExpression", "matchArms", "matchArmExpression", 
			"matchArm", "matchArmGuard", "pattern", "patternNoTopAlt", "patternWithoutRange", 
			"literalPattern", "identifierPattern", "wildcardPattern", "restPattern", 
			"rangePattern", "rangePatternBound", "referencePattern", "structPattern", 
			"structPatternElements", "structPatternFields", "structPatternField", 
			"structPatternEtCetera", "tupleStructPattern", "tupleStructItems", "tuplePattern", 
			"tuplePatternItems", "groupedPattern", "slicePattern", "slicePatternItems", 
			"pathPattern", "type_", "typeNoBounds", "parenthesizedType", "neverType", 
			"tupleType", "arrayType", "sliceType", "referenceType", "rawPointerType", 
			"bareFunctionType", "functionTypeQualifiers", "bareFunctionReturnType", 
			"functionParametersMaybeNamedVariadic", "maybeNamedFunctionParameters", 
			"maybeNamedParam", "maybeNamedFunctionParametersVariadic", "traitObjectType", 
			"traitObjectTypeOneBound", "implTraitType", "implTraitTypeOneBound", 
			"inferredType", "typeParamBounds", "typeParamBound", "traitBound", "lifetimeBounds", 
			"lifetime", "simplePath", "simplePathSegment", "pathInExpression", "pathExprSegment", 
			"pathIdentSegment", "genericArgs", "genericArg", "genericArgsConst", 
			"genericArgsLifetimes", "genericArgsTypes", "genericArgsBindings", "genericArgsBinding", 
			"qualifiedPathInExpression", "qualifiedPathType", "qualifiedPathInType", 
			"typePath", "typePathSegment", "typePathFn", "typePathInputs", "visibility", 
			"identifier", "keyword", "macroIdentifierLikeToken", "macroLiteralToken", 
			"macroPunctuationToken", "shl", "shr"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'as'", "'break'", "'const'", "'continue'", "'crate'", "'else'", 
			"'enum'", "'extern'", "'false'", "'fn'", "'for'", "'if'", "'impl'", "'in'", 
			"'let'", "'loop'", "'match'", "'mod'", "'move'", "'mut'", "'pub'", "'ref'", 
			"'return'", "'self'", "'Self'", "'static'", "'struct'", "'super'", "'trait'", 
			"'true'", "'type'", "'unsafe'", "'use'", "'where'", "'while'", "'async'", 
			"'await'", "'dyn'", "'abstract'", "'become'", "'box'", "'do'", "'final'", 
			"'macro'", "'override'", "'priv'", "'typeof'", "'unsized'", "'virtual'", 
			"'yield'", "'try'", "'union'", "''static'", "'macro_rules'", "''_'", 
			"'$crate'", null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, "'+'", "'-'", "'*'", "'/'", "'%'", "'^'", "'!'", "'&'", "'|'", 
			"'&&'", "'||'", "'+='", "'-='", "'*='", "'/='", "'%='", "'^='", "'&='", 
			"'|='", "'<<='", "'>>='", "'='", "'=='", "'!='", "'>'", "'<'", "'>='", 
			"'<='", "'@'", "'_'", "'.'", "'..'", "'...'", "'..='", "','", "';'", 
			"':'", "'::'", "'->'", "'=>'", "'#'", "'$'", "'?'", "'{'", "'}'", "'['", 
			"']'", "'('", "')'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "KW_AS", "KW_BREAK", "KW_CONST", "KW_CONTINUE", "KW_CRATE", "KW_ELSE", 
			"KW_ENUM", "KW_EXTERN", "KW_FALSE", "KW_FN", "KW_FOR", "KW_IF", "KW_IMPL", 
			"KW_IN", "KW_LET", "KW_LOOP", "KW_MATCH", "KW_MOD", "KW_MOVE", "KW_MUT", 
			"KW_PUB", "KW_REF", "KW_RETURN", "KW_SELFVALUE", "KW_SELFTYPE", "KW_STATIC", 
			"KW_STRUCT", "KW_SUPER", "KW_TRAIT", "KW_TRUE", "KW_TYPE", "KW_UNSAFE", 
			"KW_USE", "KW_WHERE", "KW_WHILE", "KW_ASYNC", "KW_AWAIT", "KW_DYN", "KW_ABSTRACT", 
			"KW_BECOME", "KW_BOX", "KW_DO", "KW_FINAL", "KW_MACRO", "KW_OVERRIDE", 
			"KW_PRIV", "KW_TYPEOF", "KW_UNSIZED", "KW_VIRTUAL", "KW_YIELD", "KW_TRY", 
			"KW_UNION", "KW_STATICLIFETIME", "KW_MACRORULES", "KW_UNDERLINELIFETIME", 
			"KW_DOLLARCRATE", "NON_KEYWORD_IDENTIFIER", "RAW_IDENTIFIER", "LINE_COMMENT", 
			"INNER_BLOCK_DOC", "BLOCK_COMMENT", "INNER_LINE_DOC", "OUTER_LINE_DOC", 
			"SHEBANG", "WHITESPACE", "NEWLINE", "CHAR_LITERAL", "STRING_LITERAL", 
			"RAW_STRING_LITERAL", "BYTE_LITERAL", "BYTE_STRING_LITERAL", "RAW_BYTE_STRING_LITERAL", 
			"INTEGER_LITERAL", "DEC_LITERAL", "HEX_LITERAL", "OCT_LITERAL", "BIN_LITERAL", 
			"FLOAT_LITERAL", "LIFETIME_OR_LABEL", "PLUS", "MINUS", "STAR", "SLASH", 
			"PERCENT", "CARET", "NOT", "AND", "OR", "ANDAND", "OROR", "PLUSEQ", "MINUSEQ", 
			"STAREQ", "SLASHEQ", "PERCENTEQ", "CARETEQ", "ANDEQ", "OREQ", "SHLEQ", 
			"SHREQ", "EQ", "EQEQ", "NE", "GT", "LT", "GE", "LE", "AT", "UNDERSCORE", 
			"DOT", "DOTDOT", "DOTDOTDOT", "DOTDOTEQ", "COMMA", "SEMI", "COLON", "PATHSEP", 
			"RARROW", "FATARROW", "POUND", "DOLLAR", "QUESTION", "LCURLYBRACE", "RCURLYBRACE", 
			"LSQUAREBRACKET", "RSQUAREBRACKET", "LPAREN", "RPAREN"
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

	public RustParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CrateContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(RustParser.EOF, 0); }
		public List<InnerAttributeContext> innerAttribute() {
			return getRuleContexts(InnerAttributeContext.class);
		}
		public InnerAttributeContext innerAttribute(int i) {
			return getRuleContext(InnerAttributeContext.class,i);
		}
		public List<ItemContext> item() {
			return getRuleContexts(ItemContext.class);
		}
		public ItemContext item(int i) {
			return getRuleContext(ItemContext.class,i);
		}
		public CrateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_crate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterCrate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitCrate(this);
		}
	}

	public final CrateContext crate() throws RecognitionException {
		CrateContext _localctx = new CrateContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_crate);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(395);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(392);
					innerAttribute();
					}
					} 
				}
				setState(397);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			}
			setState(401);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((_la) & ~0x3f) == 0 && ((1L << _la) & 526921241179989416L) != 0 || _la==PATHSEP || _la==POUND) {
				{
				{
				setState(398);
				item();
				}
				}
				setState(403);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(404);
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
	public static class MacroInvocationContext extends ParserRuleContext {
		public SimplePathContext simplePath() {
			return getRuleContext(SimplePathContext.class,0);
		}
		public TerminalNode NOT() { return getToken(RustParser.NOT, 0); }
		public DelimTokenTreeContext delimTokenTree() {
			return getRuleContext(DelimTokenTreeContext.class,0);
		}
		public MacroInvocationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macroInvocation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMacroInvocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMacroInvocation(this);
		}
	}

	public final MacroInvocationContext macroInvocation() throws RecognitionException {
		MacroInvocationContext _localctx = new MacroInvocationContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_macroInvocation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(406);
			simplePath();
			setState(407);
			match(NOT);
			setState(408);
			delimTokenTree();
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
	public static class DelimTokenTreeContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(RustParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(RustParser.RPAREN, 0); }
		public List<TokenTreeContext> tokenTree() {
			return getRuleContexts(TokenTreeContext.class);
		}
		public TokenTreeContext tokenTree(int i) {
			return getRuleContext(TokenTreeContext.class,i);
		}
		public TerminalNode LSQUAREBRACKET() { return getToken(RustParser.LSQUAREBRACKET, 0); }
		public TerminalNode RSQUAREBRACKET() { return getToken(RustParser.RSQUAREBRACKET, 0); }
		public TerminalNode LCURLYBRACE() { return getToken(RustParser.LCURLYBRACE, 0); }
		public TerminalNode RCURLYBRACE() { return getToken(RustParser.RCURLYBRACE, 0); }
		public DelimTokenTreeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_delimTokenTree; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterDelimTokenTree(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitDelimTokenTree(this);
		}
	}

	public final DelimTokenTreeContext delimTokenTree() throws RecognitionException {
		DelimTokenTreeContext _localctx = new DelimTokenTreeContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_delimTokenTree);
		int _la;
		try {
			setState(434);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(410);
				match(LPAREN);
				setState(414);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((_la) & ~0x3f) == 0 && ((1L << _la) & 576460752303423486L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1585267068834412671L) != 0) {
					{
					{
					setState(411);
					tokenTree();
					}
					}
					setState(416);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(417);
				match(RPAREN);
				}
				break;
			case LSQUAREBRACKET:
				enterOuterAlt(_localctx, 2);
				{
				setState(418);
				match(LSQUAREBRACKET);
				setState(422);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((_la) & ~0x3f) == 0 && ((1L << _la) & 576460752303423486L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1585267068834412671L) != 0) {
					{
					{
					setState(419);
					tokenTree();
					}
					}
					setState(424);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(425);
				match(RSQUAREBRACKET);
				}
				break;
			case LCURLYBRACE:
				enterOuterAlt(_localctx, 3);
				{
				setState(426);
				match(LCURLYBRACE);
				setState(430);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((_la) & ~0x3f) == 0 && ((1L << _la) & 576460752303423486L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1585267068834412671L) != 0) {
					{
					{
					setState(427);
					tokenTree();
					}
					}
					setState(432);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(433);
				match(RCURLYBRACE);
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
	public static class TokenTreeContext extends ParserRuleContext {
		public List<TokenTreeTokenContext> tokenTreeToken() {
			return getRuleContexts(TokenTreeTokenContext.class);
		}
		public TokenTreeTokenContext tokenTreeToken(int i) {
			return getRuleContext(TokenTreeTokenContext.class,i);
		}
		public DelimTokenTreeContext delimTokenTree() {
			return getRuleContext(DelimTokenTreeContext.class,0);
		}
		public TokenTreeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tokenTree; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTokenTree(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTokenTree(this);
		}
	}

	public final TokenTreeContext tokenTree() throws RecognitionException {
		TokenTreeContext _localctx = new TokenTreeContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_tokenTree);
		try {
			int _alt;
			setState(442);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_AS:
			case KW_BREAK:
			case KW_CONST:
			case KW_CONTINUE:
			case KW_CRATE:
			case KW_ELSE:
			case KW_ENUM:
			case KW_EXTERN:
			case KW_FALSE:
			case KW_FN:
			case KW_FOR:
			case KW_IF:
			case KW_IMPL:
			case KW_IN:
			case KW_LET:
			case KW_LOOP:
			case KW_MATCH:
			case KW_MOD:
			case KW_MOVE:
			case KW_MUT:
			case KW_PUB:
			case KW_REF:
			case KW_RETURN:
			case KW_SELFVALUE:
			case KW_SELFTYPE:
			case KW_STATIC:
			case KW_STRUCT:
			case KW_SUPER:
			case KW_TRAIT:
			case KW_TRUE:
			case KW_TYPE:
			case KW_UNSAFE:
			case KW_USE:
			case KW_WHERE:
			case KW_WHILE:
			case KW_ASYNC:
			case KW_AWAIT:
			case KW_DYN:
			case KW_ABSTRACT:
			case KW_BECOME:
			case KW_BOX:
			case KW_DO:
			case KW_FINAL:
			case KW_MACRO:
			case KW_OVERRIDE:
			case KW_PRIV:
			case KW_TYPEOF:
			case KW_UNSIZED:
			case KW_VIRTUAL:
			case KW_YIELD:
			case KW_TRY:
			case KW_UNION:
			case KW_STATICLIFETIME:
			case KW_MACRORULES:
			case KW_UNDERLINELIFETIME:
			case KW_DOLLARCRATE:
			case NON_KEYWORD_IDENTIFIER:
			case RAW_IDENTIFIER:
			case CHAR_LITERAL:
			case STRING_LITERAL:
			case RAW_STRING_LITERAL:
			case BYTE_LITERAL:
			case BYTE_STRING_LITERAL:
			case RAW_BYTE_STRING_LITERAL:
			case INTEGER_LITERAL:
			case FLOAT_LITERAL:
			case LIFETIME_OR_LABEL:
			case PLUS:
			case MINUS:
			case STAR:
			case SLASH:
			case PERCENT:
			case CARET:
			case NOT:
			case AND:
			case OR:
			case ANDAND:
			case OROR:
			case PLUSEQ:
			case MINUSEQ:
			case STAREQ:
			case SLASHEQ:
			case PERCENTEQ:
			case CARETEQ:
			case ANDEQ:
			case OREQ:
			case SHLEQ:
			case SHREQ:
			case EQ:
			case EQEQ:
			case NE:
			case GT:
			case LT:
			case GE:
			case LE:
			case AT:
			case UNDERSCORE:
			case DOT:
			case DOTDOT:
			case DOTDOTDOT:
			case DOTDOTEQ:
			case COMMA:
			case SEMI:
			case COLON:
			case PATHSEP:
			case RARROW:
			case FATARROW:
			case POUND:
			case DOLLAR:
			case QUESTION:
				enterOuterAlt(_localctx, 1);
				{
				setState(437); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(436);
						tokenTreeToken();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(439); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;
			case LCURLYBRACE:
			case LSQUAREBRACKET:
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(441);
				delimTokenTree();
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
	public static class TokenTreeTokenContext extends ParserRuleContext {
		public MacroIdentifierLikeTokenContext macroIdentifierLikeToken() {
			return getRuleContext(MacroIdentifierLikeTokenContext.class,0);
		}
		public MacroLiteralTokenContext macroLiteralToken() {
			return getRuleContext(MacroLiteralTokenContext.class,0);
		}
		public MacroPunctuationTokenContext macroPunctuationToken() {
			return getRuleContext(MacroPunctuationTokenContext.class,0);
		}
		public MacroRepOpContext macroRepOp() {
			return getRuleContext(MacroRepOpContext.class,0);
		}
		public TerminalNode DOLLAR() { return getToken(RustParser.DOLLAR, 0); }
		public TokenTreeTokenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tokenTreeToken; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTokenTreeToken(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTokenTreeToken(this);
		}
	}

	public final TokenTreeTokenContext tokenTreeToken() throws RecognitionException {
		TokenTreeTokenContext _localctx = new TokenTreeTokenContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_tokenTreeToken);
		try {
			setState(449);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(444);
				macroIdentifierLikeToken();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(445);
				macroLiteralToken();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(446);
				macroPunctuationToken();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(447);
				macroRepOp();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(448);
				match(DOLLAR);
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
	public static class MacroInvocationSemiContext extends ParserRuleContext {
		public SimplePathContext simplePath() {
			return getRuleContext(SimplePathContext.class,0);
		}
		public TerminalNode NOT() { return getToken(RustParser.NOT, 0); }
		public TerminalNode LPAREN() { return getToken(RustParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(RustParser.RPAREN, 0); }
		public TerminalNode SEMI() { return getToken(RustParser.SEMI, 0); }
		public List<TokenTreeContext> tokenTree() {
			return getRuleContexts(TokenTreeContext.class);
		}
		public TokenTreeContext tokenTree(int i) {
			return getRuleContext(TokenTreeContext.class,i);
		}
		public TerminalNode LSQUAREBRACKET() { return getToken(RustParser.LSQUAREBRACKET, 0); }
		public TerminalNode RSQUAREBRACKET() { return getToken(RustParser.RSQUAREBRACKET, 0); }
		public TerminalNode LCURLYBRACE() { return getToken(RustParser.LCURLYBRACE, 0); }
		public TerminalNode RCURLYBRACE() { return getToken(RustParser.RCURLYBRACE, 0); }
		public MacroInvocationSemiContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macroInvocationSemi; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMacroInvocationSemi(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMacroInvocationSemi(this);
		}
	}

	public final MacroInvocationSemiContext macroInvocationSemi() throws RecognitionException {
		MacroInvocationSemiContext _localctx = new MacroInvocationSemiContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_macroInvocationSemi);
		int _la;
		try {
			setState(486);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(451);
				simplePath();
				setState(452);
				match(NOT);
				setState(453);
				match(LPAREN);
				setState(457);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((_la) & ~0x3f) == 0 && ((1L << _la) & 576460752303423486L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1585267068834412671L) != 0) {
					{
					{
					setState(454);
					tokenTree();
					}
					}
					setState(459);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(460);
				match(RPAREN);
				setState(461);
				match(SEMI);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(463);
				simplePath();
				setState(464);
				match(NOT);
				setState(465);
				match(LSQUAREBRACKET);
				setState(469);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((_la) & ~0x3f) == 0 && ((1L << _la) & 576460752303423486L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1585267068834412671L) != 0) {
					{
					{
					setState(466);
					tokenTree();
					}
					}
					setState(471);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(472);
				match(RSQUAREBRACKET);
				setState(473);
				match(SEMI);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(475);
				simplePath();
				setState(476);
				match(NOT);
				setState(477);
				match(LCURLYBRACE);
				setState(481);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((_la) & ~0x3f) == 0 && ((1L << _la) & 576460752303423486L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1585267068834412671L) != 0) {
					{
					{
					setState(478);
					tokenTree();
					}
					}
					setState(483);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(484);
				match(RCURLYBRACE);
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
	public static class MacroRulesDefinitionContext extends ParserRuleContext {
		public TerminalNode KW_MACRORULES() { return getToken(RustParser.KW_MACRORULES, 0); }
		public TerminalNode NOT() { return getToken(RustParser.NOT, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public MacroRulesDefContext macroRulesDef() {
			return getRuleContext(MacroRulesDefContext.class,0);
		}
		public MacroRulesDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macroRulesDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMacroRulesDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMacroRulesDefinition(this);
		}
	}

	public final MacroRulesDefinitionContext macroRulesDefinition() throws RecognitionException {
		MacroRulesDefinitionContext _localctx = new MacroRulesDefinitionContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_macroRulesDefinition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(488);
			match(KW_MACRORULES);
			setState(489);
			match(NOT);
			setState(490);
			identifier();
			setState(491);
			macroRulesDef();
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
	public static class MacroRulesDefContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(RustParser.LPAREN, 0); }
		public MacroRulesContext macroRules() {
			return getRuleContext(MacroRulesContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(RustParser.RPAREN, 0); }
		public TerminalNode SEMI() { return getToken(RustParser.SEMI, 0); }
		public TerminalNode LSQUAREBRACKET() { return getToken(RustParser.LSQUAREBRACKET, 0); }
		public TerminalNode RSQUAREBRACKET() { return getToken(RustParser.RSQUAREBRACKET, 0); }
		public TerminalNode LCURLYBRACE() { return getToken(RustParser.LCURLYBRACE, 0); }
		public TerminalNode RCURLYBRACE() { return getToken(RustParser.RCURLYBRACE, 0); }
		public MacroRulesDefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macroRulesDef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMacroRulesDef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMacroRulesDef(this);
		}
	}

	public final MacroRulesDefContext macroRulesDef() throws RecognitionException {
		MacroRulesDefContext _localctx = new MacroRulesDefContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_macroRulesDef);
		try {
			setState(507);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(493);
				match(LPAREN);
				setState(494);
				macroRules();
				setState(495);
				match(RPAREN);
				setState(496);
				match(SEMI);
				}
				break;
			case LSQUAREBRACKET:
				enterOuterAlt(_localctx, 2);
				{
				setState(498);
				match(LSQUAREBRACKET);
				setState(499);
				macroRules();
				setState(500);
				match(RSQUAREBRACKET);
				setState(501);
				match(SEMI);
				}
				break;
			case LCURLYBRACE:
				enterOuterAlt(_localctx, 3);
				{
				setState(503);
				match(LCURLYBRACE);
				setState(504);
				macroRules();
				setState(505);
				match(RCURLYBRACE);
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
	public static class MacroRulesContext extends ParserRuleContext {
		public List<MacroRuleContext> macroRule() {
			return getRuleContexts(MacroRuleContext.class);
		}
		public MacroRuleContext macroRule(int i) {
			return getRuleContext(MacroRuleContext.class,i);
		}
		public List<TerminalNode> SEMI() { return getTokens(RustParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(RustParser.SEMI, i);
		}
		public MacroRulesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macroRules; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMacroRules(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMacroRules(this);
		}
	}

	public final MacroRulesContext macroRules() throws RecognitionException {
		MacroRulesContext _localctx = new MacroRulesContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_macroRules);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(509);
			macroRule();
			setState(514);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(510);
					match(SEMI);
					setState(511);
					macroRule();
					}
					} 
				}
				setState(516);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			}
			setState(518);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(517);
				match(SEMI);
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
	public static class MacroRuleContext extends ParserRuleContext {
		public MacroMatcherContext macroMatcher() {
			return getRuleContext(MacroMatcherContext.class,0);
		}
		public TerminalNode FATARROW() { return getToken(RustParser.FATARROW, 0); }
		public MacroTranscriberContext macroTranscriber() {
			return getRuleContext(MacroTranscriberContext.class,0);
		}
		public MacroRuleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macroRule; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMacroRule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMacroRule(this);
		}
	}

	public final MacroRuleContext macroRule() throws RecognitionException {
		MacroRuleContext _localctx = new MacroRuleContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_macroRule);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(520);
			macroMatcher();
			setState(521);
			match(FATARROW);
			setState(522);
			macroTranscriber();
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
	public static class MacroMatcherContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(RustParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(RustParser.RPAREN, 0); }
		public List<MacroMatchContext> macroMatch() {
			return getRuleContexts(MacroMatchContext.class);
		}
		public MacroMatchContext macroMatch(int i) {
			return getRuleContext(MacroMatchContext.class,i);
		}
		public TerminalNode LSQUAREBRACKET() { return getToken(RustParser.LSQUAREBRACKET, 0); }
		public TerminalNode RSQUAREBRACKET() { return getToken(RustParser.RSQUAREBRACKET, 0); }
		public TerminalNode LCURLYBRACE() { return getToken(RustParser.LCURLYBRACE, 0); }
		public TerminalNode RCURLYBRACE() { return getToken(RustParser.RCURLYBRACE, 0); }
		public MacroMatcherContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macroMatcher; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMacroMatcher(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMacroMatcher(this);
		}
	}

	public final MacroMatcherContext macroMatcher() throws RecognitionException {
		MacroMatcherContext _localctx = new MacroMatcherContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_macroMatcher);
		int _la;
		try {
			setState(548);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(524);
				match(LPAREN);
				setState(528);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((_la) & ~0x3f) == 0 && ((1L << _la) & 576460752303423486L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1585267068834412671L) != 0) {
					{
					{
					setState(525);
					macroMatch();
					}
					}
					setState(530);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(531);
				match(RPAREN);
				}
				break;
			case LSQUAREBRACKET:
				enterOuterAlt(_localctx, 2);
				{
				setState(532);
				match(LSQUAREBRACKET);
				setState(536);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((_la) & ~0x3f) == 0 && ((1L << _la) & 576460752303423486L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1585267068834412671L) != 0) {
					{
					{
					setState(533);
					macroMatch();
					}
					}
					setState(538);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(539);
				match(RSQUAREBRACKET);
				}
				break;
			case LCURLYBRACE:
				enterOuterAlt(_localctx, 3);
				{
				setState(540);
				match(LCURLYBRACE);
				setState(544);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((_la) & ~0x3f) == 0 && ((1L << _la) & 576460752303423486L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1585267068834412671L) != 0) {
					{
					{
					setState(541);
					macroMatch();
					}
					}
					setState(546);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(547);
				match(RCURLYBRACE);
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
	public static class MacroMatchContext extends ParserRuleContext {
		public List<MacroMatchTokenContext> macroMatchToken() {
			return getRuleContexts(MacroMatchTokenContext.class);
		}
		public MacroMatchTokenContext macroMatchToken(int i) {
			return getRuleContext(MacroMatchTokenContext.class,i);
		}
		public MacroMatcherContext macroMatcher() {
			return getRuleContext(MacroMatcherContext.class,0);
		}
		public TerminalNode DOLLAR() { return getToken(RustParser.DOLLAR, 0); }
		public TerminalNode COLON() { return getToken(RustParser.COLON, 0); }
		public MacroFragSpecContext macroFragSpec() {
			return getRuleContext(MacroFragSpecContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode KW_SELFVALUE() { return getToken(RustParser.KW_SELFVALUE, 0); }
		public TerminalNode LPAREN() { return getToken(RustParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(RustParser.RPAREN, 0); }
		public MacroRepOpContext macroRepOp() {
			return getRuleContext(MacroRepOpContext.class,0);
		}
		public List<MacroMatchContext> macroMatch() {
			return getRuleContexts(MacroMatchContext.class);
		}
		public MacroMatchContext macroMatch(int i) {
			return getRuleContext(MacroMatchContext.class,i);
		}
		public MacroRepSepContext macroRepSep() {
			return getRuleContext(MacroRepSepContext.class,0);
		}
		public MacroMatchContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macroMatch; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMacroMatch(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMacroMatch(this);
		}
	}

	public final MacroMatchContext macroMatch() throws RecognitionException {
		MacroMatchContext _localctx = new MacroMatchContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_macroMatch);
		int _la;
		try {
			int _alt;
			setState(576);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(551); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(550);
						macroMatchToken();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(553); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(555);
				macroMatcher();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(556);
				match(DOLLAR);
				setState(559);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case KW_MACRORULES:
				case NON_KEYWORD_IDENTIFIER:
				case RAW_IDENTIFIER:
					{
					setState(557);
					identifier();
					}
					break;
				case KW_SELFVALUE:
					{
					setState(558);
					match(KW_SELFVALUE);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(561);
				match(COLON);
				setState(562);
				macroFragSpec();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(563);
				match(DOLLAR);
				setState(564);
				match(LPAREN);
				setState(566); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(565);
					macroMatch();
					}
					}
					setState(568); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( ((_la) & ~0x3f) == 0 && ((1L << _la) & 576460752303423486L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1585267068834412671L) != 0 );
				setState(570);
				match(RPAREN);
				setState(572);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 576460752303423486L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 36028797018921087L) != 0) {
					{
					setState(571);
					macroRepSep();
					}
				}

				setState(574);
				macroRepOp();
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
	public static class MacroMatchTokenContext extends ParserRuleContext {
		public MacroIdentifierLikeTokenContext macroIdentifierLikeToken() {
			return getRuleContext(MacroIdentifierLikeTokenContext.class,0);
		}
		public MacroLiteralTokenContext macroLiteralToken() {
			return getRuleContext(MacroLiteralTokenContext.class,0);
		}
		public MacroPunctuationTokenContext macroPunctuationToken() {
			return getRuleContext(MacroPunctuationTokenContext.class,0);
		}
		public MacroRepOpContext macroRepOp() {
			return getRuleContext(MacroRepOpContext.class,0);
		}
		public MacroMatchTokenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macroMatchToken; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMacroMatchToken(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMacroMatchToken(this);
		}
	}

	public final MacroMatchTokenContext macroMatchToken() throws RecognitionException {
		MacroMatchTokenContext _localctx = new MacroMatchTokenContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_macroMatchToken);
		try {
			setState(582);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(578);
				macroIdentifierLikeToken();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(579);
				macroLiteralToken();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(580);
				macroPunctuationToken();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(581);
				macroRepOp();
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
	public static class MacroFragSpecContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public MacroFragSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macroFragSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMacroFragSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMacroFragSpec(this);
		}
	}

	public final MacroFragSpecContext macroFragSpec() throws RecognitionException {
		MacroFragSpecContext _localctx = new MacroFragSpecContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_macroFragSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(584);
			identifier();
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
	public static class MacroRepSepContext extends ParserRuleContext {
		public MacroIdentifierLikeTokenContext macroIdentifierLikeToken() {
			return getRuleContext(MacroIdentifierLikeTokenContext.class,0);
		}
		public MacroLiteralTokenContext macroLiteralToken() {
			return getRuleContext(MacroLiteralTokenContext.class,0);
		}
		public MacroPunctuationTokenContext macroPunctuationToken() {
			return getRuleContext(MacroPunctuationTokenContext.class,0);
		}
		public TerminalNode DOLLAR() { return getToken(RustParser.DOLLAR, 0); }
		public MacroRepSepContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macroRepSep; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMacroRepSep(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMacroRepSep(this);
		}
	}

	public final MacroRepSepContext macroRepSep() throws RecognitionException {
		MacroRepSepContext _localctx = new MacroRepSepContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_macroRepSep);
		try {
			setState(590);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(586);
				macroIdentifierLikeToken();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(587);
				macroLiteralToken();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(588);
				macroPunctuationToken();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(589);
				match(DOLLAR);
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
	public static class MacroRepOpContext extends ParserRuleContext {
		public TerminalNode STAR() { return getToken(RustParser.STAR, 0); }
		public TerminalNode PLUS() { return getToken(RustParser.PLUS, 0); }
		public TerminalNode QUESTION() { return getToken(RustParser.QUESTION, 0); }
		public MacroRepOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macroRepOp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMacroRepOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMacroRepOp(this);
		}
	}

	public final MacroRepOpContext macroRepOp() throws RecognitionException {
		MacroRepOpContext _localctx = new MacroRepOpContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_macroRepOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(592);
			_la = _input.LA(1);
			if ( !((((_la - 80)) & ~0x3f) == 0 && ((1L << (_la - 80)) & 4398046511109L) != 0) ) {
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
	public static class MacroTranscriberContext extends ParserRuleContext {
		public DelimTokenTreeContext delimTokenTree() {
			return getRuleContext(DelimTokenTreeContext.class,0);
		}
		public MacroTranscriberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macroTranscriber; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMacroTranscriber(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMacroTranscriber(this);
		}
	}

	public final MacroTranscriberContext macroTranscriber() throws RecognitionException {
		MacroTranscriberContext _localctx = new MacroTranscriberContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_macroTranscriber);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(594);
			delimTokenTree();
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
	public static class ItemContext extends ParserRuleContext {
		public VisItemContext visItem() {
			return getRuleContext(VisItemContext.class,0);
		}
		public MacroItemContext macroItem() {
			return getRuleContext(MacroItemContext.class,0);
		}
		public List<OuterAttributeContext> outerAttribute() {
			return getRuleContexts(OuterAttributeContext.class);
		}
		public OuterAttributeContext outerAttribute(int i) {
			return getRuleContext(OuterAttributeContext.class,i);
		}
		public ItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_item; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitItem(this);
		}
	}

	public final ItemContext item() throws RecognitionException {
		ItemContext _localctx = new ItemContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_item);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(599);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(596);
				outerAttribute();
				}
				}
				setState(601);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(604);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_CONST:
			case KW_ENUM:
			case KW_EXTERN:
			case KW_FN:
			case KW_IMPL:
			case KW_MOD:
			case KW_PUB:
			case KW_STATIC:
			case KW_STRUCT:
			case KW_TRAIT:
			case KW_TYPE:
			case KW_UNSAFE:
			case KW_USE:
			case KW_ASYNC:
			case KW_UNION:
				{
				setState(602);
				visItem();
				}
				break;
			case KW_CRATE:
			case KW_SELFVALUE:
			case KW_SUPER:
			case KW_MACRORULES:
			case KW_DOLLARCRATE:
			case NON_KEYWORD_IDENTIFIER:
			case RAW_IDENTIFIER:
			case PATHSEP:
				{
				setState(603);
				macroItem();
				}
				break;
			default:
				throw new NoViableAltException(this);
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
	public static class VisItemContext extends ParserRuleContext {
		public ModuleContext module() {
			return getRuleContext(ModuleContext.class,0);
		}
		public ExternCrateContext externCrate() {
			return getRuleContext(ExternCrateContext.class,0);
		}
		public UseDeclarationContext useDeclaration() {
			return getRuleContext(UseDeclarationContext.class,0);
		}
		public Function_Context function_() {
			return getRuleContext(Function_Context.class,0);
		}
		public TypeAliasContext typeAlias() {
			return getRuleContext(TypeAliasContext.class,0);
		}
		public Struct_Context struct_() {
			return getRuleContext(Struct_Context.class,0);
		}
		public EnumerationContext enumeration() {
			return getRuleContext(EnumerationContext.class,0);
		}
		public Union_Context union_() {
			return getRuleContext(Union_Context.class,0);
		}
		public ConstantItemContext constantItem() {
			return getRuleContext(ConstantItemContext.class,0);
		}
		public StaticItemContext staticItem() {
			return getRuleContext(StaticItemContext.class,0);
		}
		public Trait_Context trait_() {
			return getRuleContext(Trait_Context.class,0);
		}
		public ImplementationContext implementation() {
			return getRuleContext(ImplementationContext.class,0);
		}
		public ExternBlockContext externBlock() {
			return getRuleContext(ExternBlockContext.class,0);
		}
		public VisibilityContext visibility() {
			return getRuleContext(VisibilityContext.class,0);
		}
		public VisItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_visItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterVisItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitVisItem(this);
		}
	}

	public final VisItemContext visItem() throws RecognitionException {
		VisItemContext _localctx = new VisItemContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_visItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(607);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_PUB) {
				{
				setState(606);
				visibility();
				}
			}

			setState(622);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
			case 1:
				{
				setState(609);
				module();
				}
				break;
			case 2:
				{
				setState(610);
				externCrate();
				}
				break;
			case 3:
				{
				setState(611);
				useDeclaration();
				}
				break;
			case 4:
				{
				setState(612);
				function_();
				}
				break;
			case 5:
				{
				setState(613);
				typeAlias();
				}
				break;
			case 6:
				{
				setState(614);
				struct_();
				}
				break;
			case 7:
				{
				setState(615);
				enumeration();
				}
				break;
			case 8:
				{
				setState(616);
				union_();
				}
				break;
			case 9:
				{
				setState(617);
				constantItem();
				}
				break;
			case 10:
				{
				setState(618);
				staticItem();
				}
				break;
			case 11:
				{
				setState(619);
				trait_();
				}
				break;
			case 12:
				{
				setState(620);
				implementation();
				}
				break;
			case 13:
				{
				setState(621);
				externBlock();
				}
				break;
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
	public static class MacroItemContext extends ParserRuleContext {
		public MacroInvocationSemiContext macroInvocationSemi() {
			return getRuleContext(MacroInvocationSemiContext.class,0);
		}
		public MacroRulesDefinitionContext macroRulesDefinition() {
			return getRuleContext(MacroRulesDefinitionContext.class,0);
		}
		public MacroItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macroItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMacroItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMacroItem(this);
		}
	}

	public final MacroItemContext macroItem() throws RecognitionException {
		MacroItemContext _localctx = new MacroItemContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_macroItem);
		try {
			setState(626);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(624);
				macroInvocationSemi();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(625);
				macroRulesDefinition();
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
	public static class ModuleContext extends ParserRuleContext {
		public TerminalNode KW_MOD() { return getToken(RustParser.KW_MOD, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(RustParser.SEMI, 0); }
		public TerminalNode LCURLYBRACE() { return getToken(RustParser.LCURLYBRACE, 0); }
		public TerminalNode RCURLYBRACE() { return getToken(RustParser.RCURLYBRACE, 0); }
		public TerminalNode KW_UNSAFE() { return getToken(RustParser.KW_UNSAFE, 0); }
		public List<InnerAttributeContext> innerAttribute() {
			return getRuleContexts(InnerAttributeContext.class);
		}
		public InnerAttributeContext innerAttribute(int i) {
			return getRuleContext(InnerAttributeContext.class,i);
		}
		public List<ItemContext> item() {
			return getRuleContexts(ItemContext.class);
		}
		public ItemContext item(int i) {
			return getRuleContext(ItemContext.class,i);
		}
		public ModuleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_module; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterModule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitModule(this);
		}
	}

	public final ModuleContext module() throws RecognitionException {
		ModuleContext _localctx = new ModuleContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_module);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(629);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_UNSAFE) {
				{
				setState(628);
				match(KW_UNSAFE);
				}
			}

			setState(631);
			match(KW_MOD);
			setState(632);
			identifier();
			setState(648);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SEMI:
				{
				setState(633);
				match(SEMI);
				}
				break;
			case LCURLYBRACE:
				{
				setState(634);
				match(LCURLYBRACE);
				setState(638);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,33,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(635);
						innerAttribute();
						}
						} 
					}
					setState(640);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,33,_ctx);
				}
				setState(644);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((_la) & ~0x3f) == 0 && ((1L << _la) & 526921241179989416L) != 0 || _la==PATHSEP || _la==POUND) {
					{
					{
					setState(641);
					item();
					}
					}
					setState(646);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(647);
				match(RCURLYBRACE);
				}
				break;
			default:
				throw new NoViableAltException(this);
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
	public static class ExternCrateContext extends ParserRuleContext {
		public TerminalNode KW_EXTERN() { return getToken(RustParser.KW_EXTERN, 0); }
		public TerminalNode KW_CRATE() { return getToken(RustParser.KW_CRATE, 0); }
		public CrateRefContext crateRef() {
			return getRuleContext(CrateRefContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(RustParser.SEMI, 0); }
		public AsClauseContext asClause() {
			return getRuleContext(AsClauseContext.class,0);
		}
		public ExternCrateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_externCrate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterExternCrate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitExternCrate(this);
		}
	}

	public final ExternCrateContext externCrate() throws RecognitionException {
		ExternCrateContext _localctx = new ExternCrateContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_externCrate);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(650);
			match(KW_EXTERN);
			setState(651);
			match(KW_CRATE);
			setState(652);
			crateRef();
			setState(654);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_AS) {
				{
				setState(653);
				asClause();
				}
			}

			setState(656);
			match(SEMI);
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
	public static class CrateRefContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode KW_SELFVALUE() { return getToken(RustParser.KW_SELFVALUE, 0); }
		public CrateRefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_crateRef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterCrateRef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitCrateRef(this);
		}
	}

	public final CrateRefContext crateRef() throws RecognitionException {
		CrateRefContext _localctx = new CrateRefContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_crateRef);
		try {
			setState(660);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_MACRORULES:
			case NON_KEYWORD_IDENTIFIER:
			case RAW_IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(658);
				identifier();
				}
				break;
			case KW_SELFVALUE:
				enterOuterAlt(_localctx, 2);
				{
				setState(659);
				match(KW_SELFVALUE);
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
	public static class AsClauseContext extends ParserRuleContext {
		public TerminalNode KW_AS() { return getToken(RustParser.KW_AS, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode UNDERSCORE() { return getToken(RustParser.UNDERSCORE, 0); }
		public AsClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_asClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterAsClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitAsClause(this);
		}
	}

	public final AsClauseContext asClause() throws RecognitionException {
		AsClauseContext _localctx = new AsClauseContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_asClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(662);
			match(KW_AS);
			setState(665);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_MACRORULES:
			case NON_KEYWORD_IDENTIFIER:
			case RAW_IDENTIFIER:
				{
				setState(663);
				identifier();
				}
				break;
			case UNDERSCORE:
				{
				setState(664);
				match(UNDERSCORE);
				}
				break;
			default:
				throw new NoViableAltException(this);
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
	public static class UseDeclarationContext extends ParserRuleContext {
		public TerminalNode KW_USE() { return getToken(RustParser.KW_USE, 0); }
		public UseTreeContext useTree() {
			return getRuleContext(UseTreeContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(RustParser.SEMI, 0); }
		public UseDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_useDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterUseDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitUseDeclaration(this);
		}
	}

	public final UseDeclarationContext useDeclaration() throws RecognitionException {
		UseDeclarationContext _localctx = new UseDeclarationContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_useDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(667);
			match(KW_USE);
			setState(668);
			useTree();
			setState(669);
			match(SEMI);
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
	public static class UseTreeContext extends ParserRuleContext {
		public TerminalNode STAR() { return getToken(RustParser.STAR, 0); }
		public TerminalNode LCURLYBRACE() { return getToken(RustParser.LCURLYBRACE, 0); }
		public TerminalNode RCURLYBRACE() { return getToken(RustParser.RCURLYBRACE, 0); }
		public TerminalNode PATHSEP() { return getToken(RustParser.PATHSEP, 0); }
		public List<UseTreeContext> useTree() {
			return getRuleContexts(UseTreeContext.class);
		}
		public UseTreeContext useTree(int i) {
			return getRuleContext(UseTreeContext.class,i);
		}
		public SimplePathContext simplePath() {
			return getRuleContext(SimplePathContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public TerminalNode KW_AS() { return getToken(RustParser.KW_AS, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode UNDERSCORE() { return getToken(RustParser.UNDERSCORE, 0); }
		public UseTreeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_useTree; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterUseTree(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitUseTree(this);
		}
	}

	public final UseTreeContext useTree() throws RecognitionException {
		UseTreeContext _localctx = new UseTreeContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_useTree);
		int _la;
		try {
			int _alt;
			setState(703);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,47,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(675);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417557060190240L) != 0 || _la==PATHSEP) {
					{
					setState(672);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
					case 1:
						{
						setState(671);
						simplePath();
						}
						break;
					}
					setState(674);
					match(PATHSEP);
					}
				}

				setState(693);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case STAR:
					{
					setState(677);
					match(STAR);
					}
					break;
				case LCURLYBRACE:
					{
					setState(678);
					match(LCURLYBRACE);
					setState(690);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417557060190240L) != 0 || (((_la - 82)) & ~0x3f) == 0 && ((1L << (_la - 82)) & 2233382993921L) != 0) {
						{
						setState(679);
						useTree();
						setState(684);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,41,_ctx);
						while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
							if ( _alt==1 ) {
								{
								{
								setState(680);
								match(COMMA);
								setState(681);
								useTree();
								}
								} 
							}
							setState(686);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,41,_ctx);
						}
						setState(688);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==COMMA) {
							{
							setState(687);
							match(COMMA);
							}
						}

						}
					}

					setState(692);
					match(RCURLYBRACE);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(695);
				simplePath();
				setState(701);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==KW_AS) {
					{
					setState(696);
					match(KW_AS);
					setState(699);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case KW_MACRORULES:
					case NON_KEYWORD_IDENTIFIER:
					case RAW_IDENTIFIER:
						{
						setState(697);
						identifier();
						}
						break;
					case UNDERSCORE:
						{
						setState(698);
						match(UNDERSCORE);
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
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
	public static class Function_Context extends ParserRuleContext {
		public FunctionQualifiersContext functionQualifiers() {
			return getRuleContext(FunctionQualifiersContext.class,0);
		}
		public TerminalNode KW_FN() { return getToken(RustParser.KW_FN, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(RustParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(RustParser.RPAREN, 0); }
		public BlockExpressionContext blockExpression() {
			return getRuleContext(BlockExpressionContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(RustParser.SEMI, 0); }
		public GenericParamsContext genericParams() {
			return getRuleContext(GenericParamsContext.class,0);
		}
		public FunctionParametersContext functionParameters() {
			return getRuleContext(FunctionParametersContext.class,0);
		}
		public FunctionReturnTypeContext functionReturnType() {
			return getRuleContext(FunctionReturnTypeContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public Function_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_function_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterFunction_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitFunction_(this);
		}
	}

	public final Function_Context function_() throws RecognitionException {
		Function_Context _localctx = new Function_Context(_ctx, getState());
		enterRule(_localctx, 52, RULE_function_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(705);
			functionQualifiers();
			setState(706);
			match(KW_FN);
			setState(707);
			identifier();
			setState(709);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(708);
				genericParams();
				}
			}

			setState(711);
			match(LPAREN);
			setState(713);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 567453833619320608L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1487371226429577343L) != 0) {
				{
				setState(712);
				functionParameters();
				}
			}

			setState(715);
			match(RPAREN);
			setState(717);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RARROW) {
				{
				setState(716);
				functionReturnType();
				}
			}

			setState(720);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_WHERE) {
				{
				setState(719);
				whereClause();
				}
			}

			setState(724);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LCURLYBRACE:
				{
				setState(722);
				blockExpression();
				}
				break;
			case SEMI:
				{
				setState(723);
				match(SEMI);
				}
				break;
			default:
				throw new NoViableAltException(this);
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
	public static class FunctionQualifiersContext extends ParserRuleContext {
		public TerminalNode KW_CONST() { return getToken(RustParser.KW_CONST, 0); }
		public TerminalNode KW_ASYNC() { return getToken(RustParser.KW_ASYNC, 0); }
		public TerminalNode KW_UNSAFE() { return getToken(RustParser.KW_UNSAFE, 0); }
		public TerminalNode KW_EXTERN() { return getToken(RustParser.KW_EXTERN, 0); }
		public AbiContext abi() {
			return getRuleContext(AbiContext.class,0);
		}
		public FunctionQualifiersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionQualifiers; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterFunctionQualifiers(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitFunctionQualifiers(this);
		}
	}

	public final FunctionQualifiersContext functionQualifiers() throws RecognitionException {
		FunctionQualifiersContext _localctx = new FunctionQualifiersContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_functionQualifiers);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(727);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_CONST) {
				{
				setState(726);
				match(KW_CONST);
				}
			}

			setState(730);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_ASYNC) {
				{
				setState(729);
				match(KW_ASYNC);
				}
			}

			setState(733);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_UNSAFE) {
				{
				setState(732);
				match(KW_UNSAFE);
				}
			}

			setState(739);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_EXTERN) {
				{
				setState(735);
				match(KW_EXTERN);
				setState(737);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STRING_LITERAL || _la==RAW_STRING_LITERAL) {
					{
					setState(736);
					abi();
					}
				}

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
	public static class AbiContext extends ParserRuleContext {
		public TerminalNode STRING_LITERAL() { return getToken(RustParser.STRING_LITERAL, 0); }
		public TerminalNode RAW_STRING_LITERAL() { return getToken(RustParser.RAW_STRING_LITERAL, 0); }
		public AbiContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_abi; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterAbi(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitAbi(this);
		}
	}

	public final AbiContext abi() throws RecognitionException {
		AbiContext _localctx = new AbiContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_abi);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(741);
			_la = _input.LA(1);
			if ( !(_la==STRING_LITERAL || _la==RAW_STRING_LITERAL) ) {
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
	public static class FunctionParametersContext extends ParserRuleContext {
		public SelfParamContext selfParam() {
			return getRuleContext(SelfParamContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public List<FunctionParamContext> functionParam() {
			return getRuleContexts(FunctionParamContext.class);
		}
		public FunctionParamContext functionParam(int i) {
			return getRuleContext(FunctionParamContext.class,i);
		}
		public FunctionParametersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionParameters; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterFunctionParameters(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitFunctionParameters(this);
		}
	}

	public final FunctionParametersContext functionParameters() throws RecognitionException {
		FunctionParametersContext _localctx = new FunctionParametersContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_functionParameters);
		int _la;
		try {
			int _alt;
			setState(763);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,62,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(743);
				selfParam();
				setState(745);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(744);
					match(COMMA);
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(750);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,59,_ctx) ) {
				case 1:
					{
					setState(747);
					selfParam();
					setState(748);
					match(COMMA);
					}
					break;
				}
				setState(752);
				functionParam();
				setState(757);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,60,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(753);
						match(COMMA);
						setState(754);
						functionParam();
						}
						} 
					}
					setState(759);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,60,_ctx);
				}
				setState(761);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(760);
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
	public static class SelfParamContext extends ParserRuleContext {
		public ShorthandSelfContext shorthandSelf() {
			return getRuleContext(ShorthandSelfContext.class,0);
		}
		public TypedSelfContext typedSelf() {
			return getRuleContext(TypedSelfContext.class,0);
		}
		public List<OuterAttributeContext> outerAttribute() {
			return getRuleContexts(OuterAttributeContext.class);
		}
		public OuterAttributeContext outerAttribute(int i) {
			return getRuleContext(OuterAttributeContext.class,i);
		}
		public SelfParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selfParam; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterSelfParam(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitSelfParam(this);
		}
	}

	public final SelfParamContext selfParam() throws RecognitionException {
		SelfParamContext _localctx = new SelfParamContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_selfParam);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(768);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(765);
				outerAttribute();
				}
				}
				setState(770);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(773);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,64,_ctx) ) {
			case 1:
				{
				setState(771);
				shorthandSelf();
				}
				break;
			case 2:
				{
				setState(772);
				typedSelf();
				}
				break;
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
	public static class ShorthandSelfContext extends ParserRuleContext {
		public TerminalNode KW_SELFVALUE() { return getToken(RustParser.KW_SELFVALUE, 0); }
		public TerminalNode AND() { return getToken(RustParser.AND, 0); }
		public TerminalNode KW_MUT() { return getToken(RustParser.KW_MUT, 0); }
		public LifetimeContext lifetime() {
			return getRuleContext(LifetimeContext.class,0);
		}
		public ShorthandSelfContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_shorthandSelf; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterShorthandSelf(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitShorthandSelf(this);
		}
	}

	public final ShorthandSelfContext shorthandSelf() throws RecognitionException {
		ShorthandSelfContext _localctx = new ShorthandSelfContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_shorthandSelf);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(779);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AND) {
				{
				setState(775);
				match(AND);
				setState(777);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la - 53)) & ~0x3f) == 0 && ((1L << (_la - 53)) & 67108869L) != 0) {
					{
					setState(776);
					lifetime();
					}
				}

				}
			}

			setState(782);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_MUT) {
				{
				setState(781);
				match(KW_MUT);
				}
			}

			setState(784);
			match(KW_SELFVALUE);
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
	public static class TypedSelfContext extends ParserRuleContext {
		public TerminalNode KW_SELFVALUE() { return getToken(RustParser.KW_SELFVALUE, 0); }
		public TerminalNode COLON() { return getToken(RustParser.COLON, 0); }
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public TerminalNode KW_MUT() { return getToken(RustParser.KW_MUT, 0); }
		public TypedSelfContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typedSelf; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTypedSelf(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTypedSelf(this);
		}
	}

	public final TypedSelfContext typedSelf() throws RecognitionException {
		TypedSelfContext _localctx = new TypedSelfContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_typedSelf);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(787);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_MUT) {
				{
				setState(786);
				match(KW_MUT);
				}
			}

			setState(789);
			match(KW_SELFVALUE);
			setState(790);
			match(COLON);
			setState(791);
			type_();
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
	public static class FunctionParamContext extends ParserRuleContext {
		public FunctionParamPatternContext functionParamPattern() {
			return getRuleContext(FunctionParamPatternContext.class,0);
		}
		public TerminalNode DOTDOTDOT() { return getToken(RustParser.DOTDOTDOT, 0); }
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public List<OuterAttributeContext> outerAttribute() {
			return getRuleContexts(OuterAttributeContext.class);
		}
		public OuterAttributeContext outerAttribute(int i) {
			return getRuleContext(OuterAttributeContext.class,i);
		}
		public FunctionParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionParam; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterFunctionParam(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitFunctionParam(this);
		}
	}

	public final FunctionParamContext functionParam() throws RecognitionException {
		FunctionParamContext _localctx = new FunctionParamContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_functionParam);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(796);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(793);
				outerAttribute();
				}
				}
				setState(798);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(802);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,70,_ctx) ) {
			case 1:
				{
				setState(799);
				functionParamPattern();
				}
				break;
			case 2:
				{
				setState(800);
				match(DOTDOTDOT);
				}
				break;
			case 3:
				{
				setState(801);
				type_();
				}
				break;
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
	public static class FunctionParamPatternContext extends ParserRuleContext {
		public PatternContext pattern() {
			return getRuleContext(PatternContext.class,0);
		}
		public TerminalNode COLON() { return getToken(RustParser.COLON, 0); }
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public TerminalNode DOTDOTDOT() { return getToken(RustParser.DOTDOTDOT, 0); }
		public FunctionParamPatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionParamPattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterFunctionParamPattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitFunctionParamPattern(this);
		}
	}

	public final FunctionParamPatternContext functionParamPattern() throws RecognitionException {
		FunctionParamPatternContext _localctx = new FunctionParamPatternContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_functionParamPattern);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(804);
			pattern();
			setState(805);
			match(COLON);
			setState(808);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_CRATE:
			case KW_EXTERN:
			case KW_FN:
			case KW_FOR:
			case KW_IMPL:
			case KW_SELFVALUE:
			case KW_SELFTYPE:
			case KW_SUPER:
			case KW_UNSAFE:
			case KW_DYN:
			case KW_STATICLIFETIME:
			case KW_MACRORULES:
			case KW_UNDERLINELIFETIME:
			case KW_DOLLARCRATE:
			case NON_KEYWORD_IDENTIFIER:
			case RAW_IDENTIFIER:
			case LIFETIME_OR_LABEL:
			case STAR:
			case NOT:
			case AND:
			case LT:
			case UNDERSCORE:
			case PATHSEP:
			case QUESTION:
			case LSQUAREBRACKET:
			case LPAREN:
				{
				setState(806);
				type_();
				}
				break;
			case DOTDOTDOT:
				{
				setState(807);
				match(DOTDOTDOT);
				}
				break;
			default:
				throw new NoViableAltException(this);
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
	public static class FunctionReturnTypeContext extends ParserRuleContext {
		public TerminalNode RARROW() { return getToken(RustParser.RARROW, 0); }
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public FunctionReturnTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionReturnType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterFunctionReturnType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitFunctionReturnType(this);
		}
	}

	public final FunctionReturnTypeContext functionReturnType() throws RecognitionException {
		FunctionReturnTypeContext _localctx = new FunctionReturnTypeContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_functionReturnType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(810);
			match(RARROW);
			setState(811);
			type_();
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
	public static class TypeAliasContext extends ParserRuleContext {
		public TerminalNode KW_TYPE() { return getToken(RustParser.KW_TYPE, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(RustParser.SEMI, 0); }
		public GenericParamsContext genericParams() {
			return getRuleContext(GenericParamsContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public TerminalNode EQ() { return getToken(RustParser.EQ, 0); }
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public TypeAliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeAlias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTypeAlias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTypeAlias(this);
		}
	}

	public final TypeAliasContext typeAlias() throws RecognitionException {
		TypeAliasContext _localctx = new TypeAliasContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_typeAlias);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(813);
			match(KW_TYPE);
			setState(814);
			identifier();
			setState(816);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(815);
				genericParams();
				}
			}

			setState(819);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_WHERE) {
				{
				setState(818);
				whereClause();
				}
			}

			setState(823);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ) {
				{
				setState(821);
				match(EQ);
				setState(822);
				type_();
				}
			}

			setState(825);
			match(SEMI);
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
	public static class Struct_Context extends ParserRuleContext {
		public StructStructContext structStruct() {
			return getRuleContext(StructStructContext.class,0);
		}
		public TupleStructContext tupleStruct() {
			return getRuleContext(TupleStructContext.class,0);
		}
		public Struct_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_struct_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterStruct_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitStruct_(this);
		}
	}

	public final Struct_Context struct_() throws RecognitionException {
		Struct_Context _localctx = new Struct_Context(_ctx, getState());
		enterRule(_localctx, 74, RULE_struct_);
		try {
			setState(829);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,75,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(827);
				structStruct();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(828);
				tupleStruct();
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
	public static class StructStructContext extends ParserRuleContext {
		public TerminalNode KW_STRUCT() { return getToken(RustParser.KW_STRUCT, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode LCURLYBRACE() { return getToken(RustParser.LCURLYBRACE, 0); }
		public TerminalNode RCURLYBRACE() { return getToken(RustParser.RCURLYBRACE, 0); }
		public TerminalNode SEMI() { return getToken(RustParser.SEMI, 0); }
		public GenericParamsContext genericParams() {
			return getRuleContext(GenericParamsContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public StructFieldsContext structFields() {
			return getRuleContext(StructFieldsContext.class,0);
		}
		public StructStructContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structStruct; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterStructStruct(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitStructStruct(this);
		}
	}

	public final StructStructContext structStruct() throws RecognitionException {
		StructStructContext _localctx = new StructStructContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_structStruct);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(831);
			match(KW_STRUCT);
			setState(832);
			identifier();
			setState(834);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(833);
				genericParams();
				}
			}

			setState(837);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_WHERE) {
				{
				setState(836);
				whereClause();
				}
			}

			setState(845);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LCURLYBRACE:
				{
				setState(839);
				match(LCURLYBRACE);
				setState(841);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 450359962739146752L) != 0 || _la==POUND) {
					{
					setState(840);
					structFields();
					}
				}

				setState(843);
				match(RCURLYBRACE);
				}
				break;
			case SEMI:
				{
				setState(844);
				match(SEMI);
				}
				break;
			default:
				throw new NoViableAltException(this);
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
	public static class TupleStructContext extends ParserRuleContext {
		public TerminalNode KW_STRUCT() { return getToken(RustParser.KW_STRUCT, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(RustParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(RustParser.RPAREN, 0); }
		public TerminalNode SEMI() { return getToken(RustParser.SEMI, 0); }
		public GenericParamsContext genericParams() {
			return getRuleContext(GenericParamsContext.class,0);
		}
		public TupleFieldsContext tupleFields() {
			return getRuleContext(TupleFieldsContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public TupleStructContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tupleStruct; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTupleStruct(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTupleStruct(this);
		}
	}

	public final TupleStructContext tupleStruct() throws RecognitionException {
		TupleStructContext _localctx = new TupleStructContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_tupleStruct);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(847);
			match(KW_STRUCT);
			setState(848);
			identifier();
			setState(850);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(849);
				genericParams();
				}
			}

			setState(852);
			match(LPAREN);
			setState(854);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 567453832542432544L) != 0 || (((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 363114855924105L) != 0) {
				{
				setState(853);
				tupleFields();
				}
			}

			setState(856);
			match(RPAREN);
			setState(858);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_WHERE) {
				{
				setState(857);
				whereClause();
				}
			}

			setState(860);
			match(SEMI);
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
	public static class StructFieldsContext extends ParserRuleContext {
		public List<StructFieldContext> structField() {
			return getRuleContexts(StructFieldContext.class);
		}
		public StructFieldContext structField(int i) {
			return getRuleContext(StructFieldContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public StructFieldsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structFields; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterStructFields(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitStructFields(this);
		}
	}

	public final StructFieldsContext structFields() throws RecognitionException {
		StructFieldsContext _localctx = new StructFieldsContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_structFields);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(862);
			structField();
			setState(867);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,83,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(863);
					match(COMMA);
					setState(864);
					structField();
					}
					} 
				}
				setState(869);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,83,_ctx);
			}
			setState(871);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(870);
				match(COMMA);
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
	public static class StructFieldContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode COLON() { return getToken(RustParser.COLON, 0); }
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public List<OuterAttributeContext> outerAttribute() {
			return getRuleContexts(OuterAttributeContext.class);
		}
		public OuterAttributeContext outerAttribute(int i) {
			return getRuleContext(OuterAttributeContext.class,i);
		}
		public VisibilityContext visibility() {
			return getRuleContext(VisibilityContext.class,0);
		}
		public StructFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterStructField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitStructField(this);
		}
	}

	public final StructFieldContext structField() throws RecognitionException {
		StructFieldContext _localctx = new StructFieldContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_structField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(876);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(873);
				outerAttribute();
				}
				}
				setState(878);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(880);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_PUB) {
				{
				setState(879);
				visibility();
				}
			}

			setState(882);
			identifier();
			setState(883);
			match(COLON);
			setState(884);
			type_();
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
	public static class TupleFieldsContext extends ParserRuleContext {
		public List<TupleFieldContext> tupleField() {
			return getRuleContexts(TupleFieldContext.class);
		}
		public TupleFieldContext tupleField(int i) {
			return getRuleContext(TupleFieldContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public TupleFieldsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tupleFields; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTupleFields(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTupleFields(this);
		}
	}

	public final TupleFieldsContext tupleFields() throws RecognitionException {
		TupleFieldsContext _localctx = new TupleFieldsContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_tupleFields);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(886);
			tupleField();
			setState(891);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,87,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(887);
					match(COMMA);
					setState(888);
					tupleField();
					}
					} 
				}
				setState(893);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,87,_ctx);
			}
			setState(895);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(894);
				match(COMMA);
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
	public static class TupleFieldContext extends ParserRuleContext {
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public List<OuterAttributeContext> outerAttribute() {
			return getRuleContexts(OuterAttributeContext.class);
		}
		public OuterAttributeContext outerAttribute(int i) {
			return getRuleContext(OuterAttributeContext.class,i);
		}
		public VisibilityContext visibility() {
			return getRuleContext(VisibilityContext.class,0);
		}
		public TupleFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tupleField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTupleField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTupleField(this);
		}
	}

	public final TupleFieldContext tupleField() throws RecognitionException {
		TupleFieldContext _localctx = new TupleFieldContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_tupleField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(900);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(897);
				outerAttribute();
				}
				}
				setState(902);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(904);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_PUB) {
				{
				setState(903);
				visibility();
				}
			}

			setState(906);
			type_();
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
	public static class EnumerationContext extends ParserRuleContext {
		public TerminalNode KW_ENUM() { return getToken(RustParser.KW_ENUM, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode LCURLYBRACE() { return getToken(RustParser.LCURLYBRACE, 0); }
		public TerminalNode RCURLYBRACE() { return getToken(RustParser.RCURLYBRACE, 0); }
		public GenericParamsContext genericParams() {
			return getRuleContext(GenericParamsContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public EnumItemsContext enumItems() {
			return getRuleContext(EnumItemsContext.class,0);
		}
		public EnumerationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumeration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterEnumeration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitEnumeration(this);
		}
	}

	public final EnumerationContext enumeration() throws RecognitionException {
		EnumerationContext _localctx = new EnumerationContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_enumeration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(908);
			match(KW_ENUM);
			setState(909);
			identifier();
			setState(911);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(910);
				genericParams();
				}
			}

			setState(914);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_WHERE) {
				{
				setState(913);
				whereClause();
				}
			}

			setState(916);
			match(LCURLYBRACE);
			setState(918);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 450359962739146752L) != 0 || _la==POUND) {
				{
				setState(917);
				enumItems();
				}
			}

			setState(920);
			match(RCURLYBRACE);
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
	public static class EnumItemsContext extends ParserRuleContext {
		public List<EnumItemContext> enumItem() {
			return getRuleContexts(EnumItemContext.class);
		}
		public EnumItemContext enumItem(int i) {
			return getRuleContext(EnumItemContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public EnumItemsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumItems; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterEnumItems(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitEnumItems(this);
		}
	}

	public final EnumItemsContext enumItems() throws RecognitionException {
		EnumItemsContext _localctx = new EnumItemsContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_enumItems);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(922);
			enumItem();
			setState(927);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,94,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(923);
					match(COMMA);
					setState(924);
					enumItem();
					}
					} 
				}
				setState(929);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,94,_ctx);
			}
			setState(931);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(930);
				match(COMMA);
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
	public static class EnumItemContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<OuterAttributeContext> outerAttribute() {
			return getRuleContexts(OuterAttributeContext.class);
		}
		public OuterAttributeContext outerAttribute(int i) {
			return getRuleContext(OuterAttributeContext.class,i);
		}
		public VisibilityContext visibility() {
			return getRuleContext(VisibilityContext.class,0);
		}
		public EnumItemTupleContext enumItemTuple() {
			return getRuleContext(EnumItemTupleContext.class,0);
		}
		public EnumItemStructContext enumItemStruct() {
			return getRuleContext(EnumItemStructContext.class,0);
		}
		public EnumItemDiscriminantContext enumItemDiscriminant() {
			return getRuleContext(EnumItemDiscriminantContext.class,0);
		}
		public EnumItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterEnumItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitEnumItem(this);
		}
	}

	public final EnumItemContext enumItem() throws RecognitionException {
		EnumItemContext _localctx = new EnumItemContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_enumItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(936);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(933);
				outerAttribute();
				}
				}
				setState(938);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(940);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_PUB) {
				{
				setState(939);
				visibility();
				}
			}

			setState(942);
			identifier();
			setState(946);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				{
				setState(943);
				enumItemTuple();
				}
				break;
			case LCURLYBRACE:
				{
				setState(944);
				enumItemStruct();
				}
				break;
			case EQ:
				{
				setState(945);
				enumItemDiscriminant();
				}
				break;
			case COMMA:
			case RCURLYBRACE:
				break;
			default:
				break;
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
	public static class EnumItemTupleContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(RustParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(RustParser.RPAREN, 0); }
		public TupleFieldsContext tupleFields() {
			return getRuleContext(TupleFieldsContext.class,0);
		}
		public EnumItemTupleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumItemTuple; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterEnumItemTuple(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitEnumItemTuple(this);
		}
	}

	public final EnumItemTupleContext enumItemTuple() throws RecognitionException {
		EnumItemTupleContext _localctx = new EnumItemTupleContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_enumItemTuple);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(948);
			match(LPAREN);
			setState(950);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 567453832542432544L) != 0 || (((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 363114855924105L) != 0) {
				{
				setState(949);
				tupleFields();
				}
			}

			setState(952);
			match(RPAREN);
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
	public static class EnumItemStructContext extends ParserRuleContext {
		public TerminalNode LCURLYBRACE() { return getToken(RustParser.LCURLYBRACE, 0); }
		public TerminalNode RCURLYBRACE() { return getToken(RustParser.RCURLYBRACE, 0); }
		public StructFieldsContext structFields() {
			return getRuleContext(StructFieldsContext.class,0);
		}
		public EnumItemStructContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumItemStruct; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterEnumItemStruct(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitEnumItemStruct(this);
		}
	}

	public final EnumItemStructContext enumItemStruct() throws RecognitionException {
		EnumItemStructContext _localctx = new EnumItemStructContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_enumItemStruct);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(954);
			match(LCURLYBRACE);
			setState(956);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 450359962739146752L) != 0 || _la==POUND) {
				{
				setState(955);
				structFields();
				}
			}

			setState(958);
			match(RCURLYBRACE);
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
	public static class EnumItemDiscriminantContext extends ParserRuleContext {
		public TerminalNode EQ() { return getToken(RustParser.EQ, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public EnumItemDiscriminantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumItemDiscriminant; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterEnumItemDiscriminant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitEnumItemDiscriminant(this);
		}
	}

	public final EnumItemDiscriminantContext enumItemDiscriminant() throws RecognitionException {
		EnumItemDiscriminantContext _localctx = new EnumItemDiscriminantContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_enumItemDiscriminant);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(960);
			match(EQ);
			setState(961);
			expression(0);
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
	public static class Union_Context extends ParserRuleContext {
		public TerminalNode KW_UNION() { return getToken(RustParser.KW_UNION, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode LCURLYBRACE() { return getToken(RustParser.LCURLYBRACE, 0); }
		public StructFieldsContext structFields() {
			return getRuleContext(StructFieldsContext.class,0);
		}
		public TerminalNode RCURLYBRACE() { return getToken(RustParser.RCURLYBRACE, 0); }
		public GenericParamsContext genericParams() {
			return getRuleContext(GenericParamsContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public Union_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_union_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterUnion_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitUnion_(this);
		}
	}

	public final Union_Context union_() throws RecognitionException {
		Union_Context _localctx = new Union_Context(_ctx, getState());
		enterRule(_localctx, 100, RULE_union_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(963);
			match(KW_UNION);
			setState(964);
			identifier();
			setState(966);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(965);
				genericParams();
				}
			}

			setState(969);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_WHERE) {
				{
				setState(968);
				whereClause();
				}
			}

			setState(971);
			match(LCURLYBRACE);
			setState(972);
			structFields();
			setState(973);
			match(RCURLYBRACE);
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
	public static class ConstantItemContext extends ParserRuleContext {
		public TerminalNode KW_CONST() { return getToken(RustParser.KW_CONST, 0); }
		public TerminalNode COLON() { return getToken(RustParser.COLON, 0); }
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public TerminalNode SEMI() { return getToken(RustParser.SEMI, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode UNDERSCORE() { return getToken(RustParser.UNDERSCORE, 0); }
		public TerminalNode EQ() { return getToken(RustParser.EQ, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ConstantItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constantItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterConstantItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitConstantItem(this);
		}
	}

	public final ConstantItemContext constantItem() throws RecognitionException {
		ConstantItemContext _localctx = new ConstantItemContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_constantItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(975);
			match(KW_CONST);
			setState(978);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_MACRORULES:
			case NON_KEYWORD_IDENTIFIER:
			case RAW_IDENTIFIER:
				{
				setState(976);
				identifier();
				}
				break;
			case UNDERSCORE:
				{
				setState(977);
				match(UNDERSCORE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(980);
			match(COLON);
			setState(981);
			type_();
			setState(984);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ) {
				{
				setState(982);
				match(EQ);
				setState(983);
				expression(0);
				}
			}

			setState(986);
			match(SEMI);
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
	public static class StaticItemContext extends ParserRuleContext {
		public TerminalNode KW_STATIC() { return getToken(RustParser.KW_STATIC, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode COLON() { return getToken(RustParser.COLON, 0); }
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public TerminalNode SEMI() { return getToken(RustParser.SEMI, 0); }
		public TerminalNode KW_MUT() { return getToken(RustParser.KW_MUT, 0); }
		public TerminalNode EQ() { return getToken(RustParser.EQ, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public StaticItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_staticItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterStaticItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitStaticItem(this);
		}
	}

	public final StaticItemContext staticItem() throws RecognitionException {
		StaticItemContext _localctx = new StaticItemContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_staticItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(988);
			match(KW_STATIC);
			setState(990);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_MUT) {
				{
				setState(989);
				match(KW_MUT);
				}
			}

			setState(992);
			identifier();
			setState(993);
			match(COLON);
			setState(994);
			type_();
			setState(997);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ) {
				{
				setState(995);
				match(EQ);
				setState(996);
				expression(0);
				}
			}

			setState(999);
			match(SEMI);
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
	public static class Trait_Context extends ParserRuleContext {
		public TerminalNode KW_TRAIT() { return getToken(RustParser.KW_TRAIT, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode LCURLYBRACE() { return getToken(RustParser.LCURLYBRACE, 0); }
		public TerminalNode RCURLYBRACE() { return getToken(RustParser.RCURLYBRACE, 0); }
		public TerminalNode KW_UNSAFE() { return getToken(RustParser.KW_UNSAFE, 0); }
		public GenericParamsContext genericParams() {
			return getRuleContext(GenericParamsContext.class,0);
		}
		public TerminalNode COLON() { return getToken(RustParser.COLON, 0); }
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public List<InnerAttributeContext> innerAttribute() {
			return getRuleContexts(InnerAttributeContext.class);
		}
		public InnerAttributeContext innerAttribute(int i) {
			return getRuleContext(InnerAttributeContext.class,i);
		}
		public List<AssociatedItemContext> associatedItem() {
			return getRuleContexts(AssociatedItemContext.class);
		}
		public AssociatedItemContext associatedItem(int i) {
			return getRuleContext(AssociatedItemContext.class,i);
		}
		public TypeParamBoundsContext typeParamBounds() {
			return getRuleContext(TypeParamBoundsContext.class,0);
		}
		public Trait_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_trait_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTrait_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTrait_(this);
		}
	}

	public final Trait_Context trait_() throws RecognitionException {
		Trait_Context _localctx = new Trait_Context(_ctx, getState());
		enterRule(_localctx, 106, RULE_trait_);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1002);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_UNSAFE) {
				{
				setState(1001);
				match(KW_UNSAFE);
				}
			}

			setState(1004);
			match(KW_TRAIT);
			setState(1005);
			identifier();
			setState(1007);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(1006);
				genericParams();
				}
			}

			setState(1013);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(1009);
				match(COLON);
				setState(1011);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 567453553367451680L) != 0 || (((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 290545947639809L) != 0) {
					{
					setState(1010);
					typeParamBounds();
					}
				}

				}
			}

			setState(1016);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_WHERE) {
				{
				setState(1015);
				whereClause();
				}
			}

			setState(1018);
			match(LCURLYBRACE);
			setState(1022);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,112,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1019);
					innerAttribute();
					}
					} 
				}
				setState(1024);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,112,_ctx);
			}
			setState(1028);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417632224216360L) != 0 || _la==PATHSEP || _la==POUND) {
				{
				{
				setState(1025);
				associatedItem();
				}
				}
				setState(1030);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1031);
			match(RCURLYBRACE);
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
	public static class ImplementationContext extends ParserRuleContext {
		public InherentImplContext inherentImpl() {
			return getRuleContext(InherentImplContext.class,0);
		}
		public TraitImplContext traitImpl() {
			return getRuleContext(TraitImplContext.class,0);
		}
		public ImplementationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_implementation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterImplementation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitImplementation(this);
		}
	}

	public final ImplementationContext implementation() throws RecognitionException {
		ImplementationContext _localctx = new ImplementationContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_implementation);
		try {
			setState(1035);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,114,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1033);
				inherentImpl();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1034);
				traitImpl();
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
	public static class InherentImplContext extends ParserRuleContext {
		public TerminalNode KW_IMPL() { return getToken(RustParser.KW_IMPL, 0); }
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public TerminalNode LCURLYBRACE() { return getToken(RustParser.LCURLYBRACE, 0); }
		public TerminalNode RCURLYBRACE() { return getToken(RustParser.RCURLYBRACE, 0); }
		public GenericParamsContext genericParams() {
			return getRuleContext(GenericParamsContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public List<InnerAttributeContext> innerAttribute() {
			return getRuleContexts(InnerAttributeContext.class);
		}
		public InnerAttributeContext innerAttribute(int i) {
			return getRuleContext(InnerAttributeContext.class,i);
		}
		public List<AssociatedItemContext> associatedItem() {
			return getRuleContexts(AssociatedItemContext.class);
		}
		public AssociatedItemContext associatedItem(int i) {
			return getRuleContext(AssociatedItemContext.class,i);
		}
		public InherentImplContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inherentImpl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterInherentImpl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitInherentImpl(this);
		}
	}

	public final InherentImplContext inherentImpl() throws RecognitionException {
		InherentImplContext _localctx = new InherentImplContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_inherentImpl);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1037);
			match(KW_IMPL);
			setState(1039);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,115,_ctx) ) {
			case 1:
				{
				setState(1038);
				genericParams();
				}
				break;
			}
			setState(1041);
			type_();
			setState(1043);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_WHERE) {
				{
				setState(1042);
				whereClause();
				}
			}

			setState(1045);
			match(LCURLYBRACE);
			setState(1049);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,117,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1046);
					innerAttribute();
					}
					} 
				}
				setState(1051);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,117,_ctx);
			}
			setState(1055);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417632224216360L) != 0 || _la==PATHSEP || _la==POUND) {
				{
				{
				setState(1052);
				associatedItem();
				}
				}
				setState(1057);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1058);
			match(RCURLYBRACE);
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
	public static class TraitImplContext extends ParserRuleContext {
		public TerminalNode KW_IMPL() { return getToken(RustParser.KW_IMPL, 0); }
		public TypePathContext typePath() {
			return getRuleContext(TypePathContext.class,0);
		}
		public TerminalNode KW_FOR() { return getToken(RustParser.KW_FOR, 0); }
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public TerminalNode LCURLYBRACE() { return getToken(RustParser.LCURLYBRACE, 0); }
		public TerminalNode RCURLYBRACE() { return getToken(RustParser.RCURLYBRACE, 0); }
		public TerminalNode KW_UNSAFE() { return getToken(RustParser.KW_UNSAFE, 0); }
		public GenericParamsContext genericParams() {
			return getRuleContext(GenericParamsContext.class,0);
		}
		public TerminalNode NOT() { return getToken(RustParser.NOT, 0); }
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public List<InnerAttributeContext> innerAttribute() {
			return getRuleContexts(InnerAttributeContext.class);
		}
		public InnerAttributeContext innerAttribute(int i) {
			return getRuleContext(InnerAttributeContext.class,i);
		}
		public List<AssociatedItemContext> associatedItem() {
			return getRuleContexts(AssociatedItemContext.class);
		}
		public AssociatedItemContext associatedItem(int i) {
			return getRuleContext(AssociatedItemContext.class,i);
		}
		public TraitImplContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_traitImpl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTraitImpl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTraitImpl(this);
		}
	}

	public final TraitImplContext traitImpl() throws RecognitionException {
		TraitImplContext _localctx = new TraitImplContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_traitImpl);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1061);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_UNSAFE) {
				{
				setState(1060);
				match(KW_UNSAFE);
				}
			}

			setState(1063);
			match(KW_IMPL);
			setState(1065);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(1064);
				genericParams();
				}
			}

			setState(1068);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(1067);
				match(NOT);
				}
			}

			setState(1070);
			typePath();
			setState(1071);
			match(KW_FOR);
			setState(1072);
			type_();
			setState(1074);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_WHERE) {
				{
				setState(1073);
				whereClause();
				}
			}

			setState(1076);
			match(LCURLYBRACE);
			setState(1080);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,123,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1077);
					innerAttribute();
					}
					} 
				}
				setState(1082);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,123,_ctx);
			}
			setState(1086);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417632224216360L) != 0 || _la==PATHSEP || _la==POUND) {
				{
				{
				setState(1083);
				associatedItem();
				}
				}
				setState(1088);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1089);
			match(RCURLYBRACE);
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
	public static class ExternBlockContext extends ParserRuleContext {
		public TerminalNode KW_EXTERN() { return getToken(RustParser.KW_EXTERN, 0); }
		public TerminalNode LCURLYBRACE() { return getToken(RustParser.LCURLYBRACE, 0); }
		public TerminalNode RCURLYBRACE() { return getToken(RustParser.RCURLYBRACE, 0); }
		public TerminalNode KW_UNSAFE() { return getToken(RustParser.KW_UNSAFE, 0); }
		public AbiContext abi() {
			return getRuleContext(AbiContext.class,0);
		}
		public List<InnerAttributeContext> innerAttribute() {
			return getRuleContexts(InnerAttributeContext.class);
		}
		public InnerAttributeContext innerAttribute(int i) {
			return getRuleContext(InnerAttributeContext.class,i);
		}
		public List<ExternalItemContext> externalItem() {
			return getRuleContexts(ExternalItemContext.class);
		}
		public ExternalItemContext externalItem(int i) {
			return getRuleContext(ExternalItemContext.class,i);
		}
		public ExternBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_externBlock; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterExternBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitExternBlock(this);
		}
	}

	public final ExternBlockContext externBlock() throws RecognitionException {
		ExternBlockContext _localctx = new ExternBlockContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_externBlock);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1092);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_UNSAFE) {
				{
				setState(1091);
				match(KW_UNSAFE);
				}
			}

			setState(1094);
			match(KW_EXTERN);
			setState(1096);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==STRING_LITERAL || _la==RAW_STRING_LITERAL) {
				{
				setState(1095);
				abi();
				}
			}

			setState(1098);
			match(LCURLYBRACE);
			setState(1102);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,127,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1099);
					innerAttribute();
					}
					} 
				}
				setState(1104);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,127,_ctx);
			}
			setState(1108);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417630143841576L) != 0 || _la==PATHSEP || _la==POUND) {
				{
				{
				setState(1105);
				externalItem();
				}
				}
				setState(1110);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1111);
			match(RCURLYBRACE);
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
	public static class ExternalItemContext extends ParserRuleContext {
		public MacroInvocationSemiContext macroInvocationSemi() {
			return getRuleContext(MacroInvocationSemiContext.class,0);
		}
		public List<OuterAttributeContext> outerAttribute() {
			return getRuleContexts(OuterAttributeContext.class);
		}
		public OuterAttributeContext outerAttribute(int i) {
			return getRuleContext(OuterAttributeContext.class,i);
		}
		public StaticItemContext staticItem() {
			return getRuleContext(StaticItemContext.class,0);
		}
		public Function_Context function_() {
			return getRuleContext(Function_Context.class,0);
		}
		public VisibilityContext visibility() {
			return getRuleContext(VisibilityContext.class,0);
		}
		public ExternalItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_externalItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterExternalItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitExternalItem(this);
		}
	}

	public final ExternalItemContext externalItem() throws RecognitionException {
		ExternalItemContext _localctx = new ExternalItemContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_externalItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1116);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(1113);
				outerAttribute();
				}
				}
				setState(1118);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1127);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_CRATE:
			case KW_SELFVALUE:
			case KW_SUPER:
			case KW_MACRORULES:
			case KW_DOLLARCRATE:
			case NON_KEYWORD_IDENTIFIER:
			case RAW_IDENTIFIER:
			case PATHSEP:
				{
				setState(1119);
				macroInvocationSemi();
				}
				break;
			case KW_CONST:
			case KW_EXTERN:
			case KW_FN:
			case KW_PUB:
			case KW_STATIC:
			case KW_UNSAFE:
			case KW_ASYNC:
				{
				setState(1121);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==KW_PUB) {
					{
					setState(1120);
					visibility();
					}
				}

				setState(1125);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case KW_STATIC:
					{
					setState(1123);
					staticItem();
					}
					break;
				case KW_CONST:
				case KW_EXTERN:
				case KW_FN:
				case KW_UNSAFE:
				case KW_ASYNC:
					{
					setState(1124);
					function_();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
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
	public static class GenericParamsContext extends ParserRuleContext {
		public TerminalNode LT() { return getToken(RustParser.LT, 0); }
		public TerminalNode GT() { return getToken(RustParser.GT, 0); }
		public List<GenericParamContext> genericParam() {
			return getRuleContexts(GenericParamContext.class);
		}
		public GenericParamContext genericParam(int i) {
			return getRuleContext(GenericParamContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public GenericParamsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericParams; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterGenericParams(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitGenericParams(this);
		}
	}

	public final GenericParamsContext genericParams() throws RecognitionException {
		GenericParamsContext _localctx = new GenericParamsContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_genericParams);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1129);
			match(LT);
			setState(1142);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 450359962737049608L) != 0 || _la==LIFETIME_OR_LABEL || _la==POUND) {
				{
				setState(1135);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,133,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1130);
						genericParam();
						setState(1131);
						match(COMMA);
						}
						} 
					}
					setState(1137);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,133,_ctx);
				}
				setState(1138);
				genericParam();
				setState(1140);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(1139);
					match(COMMA);
					}
				}

				}
			}

			setState(1144);
			match(GT);
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
	public static class GenericParamContext extends ParserRuleContext {
		public LifetimeParamContext lifetimeParam() {
			return getRuleContext(LifetimeParamContext.class,0);
		}
		public TypeParamContext typeParam() {
			return getRuleContext(TypeParamContext.class,0);
		}
		public ConstParamContext constParam() {
			return getRuleContext(ConstParamContext.class,0);
		}
		public List<OuterAttributeContext> outerAttribute() {
			return getRuleContexts(OuterAttributeContext.class);
		}
		public OuterAttributeContext outerAttribute(int i) {
			return getRuleContext(OuterAttributeContext.class,i);
		}
		public GenericParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericParam; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterGenericParam(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitGenericParam(this);
		}
	}

	public final GenericParamContext genericParam() throws RecognitionException {
		GenericParamContext _localctx = new GenericParamContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_genericParam);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1149);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,136,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1146);
					outerAttribute();
					}
					} 
				}
				setState(1151);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,136,_ctx);
			}
			setState(1155);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,137,_ctx) ) {
			case 1:
				{
				setState(1152);
				lifetimeParam();
				}
				break;
			case 2:
				{
				setState(1153);
				typeParam();
				}
				break;
			case 3:
				{
				setState(1154);
				constParam();
				}
				break;
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
	public static class LifetimeParamContext extends ParserRuleContext {
		public TerminalNode LIFETIME_OR_LABEL() { return getToken(RustParser.LIFETIME_OR_LABEL, 0); }
		public OuterAttributeContext outerAttribute() {
			return getRuleContext(OuterAttributeContext.class,0);
		}
		public TerminalNode COLON() { return getToken(RustParser.COLON, 0); }
		public LifetimeBoundsContext lifetimeBounds() {
			return getRuleContext(LifetimeBoundsContext.class,0);
		}
		public LifetimeParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lifetimeParam; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterLifetimeParam(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitLifetimeParam(this);
		}
	}

	public final LifetimeParamContext lifetimeParam() throws RecognitionException {
		LifetimeParamContext _localctx = new LifetimeParamContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_lifetimeParam);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1158);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==POUND) {
				{
				setState(1157);
				outerAttribute();
				}
			}

			setState(1160);
			match(LIFETIME_OR_LABEL);
			setState(1163);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(1161);
				match(COLON);
				setState(1162);
				lifetimeBounds();
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
	public static class TypeParamContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public OuterAttributeContext outerAttribute() {
			return getRuleContext(OuterAttributeContext.class,0);
		}
		public TerminalNode COLON() { return getToken(RustParser.COLON, 0); }
		public TerminalNode EQ() { return getToken(RustParser.EQ, 0); }
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public TypeParamBoundsContext typeParamBounds() {
			return getRuleContext(TypeParamBoundsContext.class,0);
		}
		public TypeParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeParam; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTypeParam(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTypeParam(this);
		}
	}

	public final TypeParamContext typeParam() throws RecognitionException {
		TypeParamContext _localctx = new TypeParamContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_typeParam);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1166);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==POUND) {
				{
				setState(1165);
				outerAttribute();
				}
			}

			setState(1168);
			identifier();
			setState(1173);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(1169);
				match(COLON);
				setState(1171);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 567453553367451680L) != 0 || (((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 290545947639809L) != 0) {
					{
					setState(1170);
					typeParamBounds();
					}
				}

				}
			}

			setState(1177);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ) {
				{
				setState(1175);
				match(EQ);
				setState(1176);
				type_();
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
	public static class ConstParamContext extends ParserRuleContext {
		public TerminalNode KW_CONST() { return getToken(RustParser.KW_CONST, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode COLON() { return getToken(RustParser.COLON, 0); }
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public ConstParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constParam; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterConstParam(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitConstParam(this);
		}
	}

	public final ConstParamContext constParam() throws RecognitionException {
		ConstParamContext _localctx = new ConstParamContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_constParam);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1179);
			match(KW_CONST);
			setState(1180);
			identifier();
			setState(1181);
			match(COLON);
			setState(1182);
			type_();
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
	public static class WhereClauseContext extends ParserRuleContext {
		public TerminalNode KW_WHERE() { return getToken(RustParser.KW_WHERE, 0); }
		public List<WhereClauseItemContext> whereClauseItem() {
			return getRuleContexts(WhereClauseItemContext.class);
		}
		public WhereClauseItemContext whereClauseItem(int i) {
			return getRuleContext(WhereClauseItemContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public WhereClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whereClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterWhereClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitWhereClause(this);
		}
	}

	public final WhereClauseContext whereClause() throws RecognitionException {
		WhereClauseContext _localctx = new WhereClauseContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_whereClause);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1184);
			match(KW_WHERE);
			setState(1190);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,144,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1185);
					whereClauseItem();
					setState(1186);
					match(COMMA);
					}
					} 
				}
				setState(1192);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,144,_ctx);
			}
			setState(1194);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 567453832540335392L) != 0 || (((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 360915832668553L) != 0) {
				{
				setState(1193);
				whereClauseItem();
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
	public static class WhereClauseItemContext extends ParserRuleContext {
		public LifetimeWhereClauseItemContext lifetimeWhereClauseItem() {
			return getRuleContext(LifetimeWhereClauseItemContext.class,0);
		}
		public TypeBoundWhereClauseItemContext typeBoundWhereClauseItem() {
			return getRuleContext(TypeBoundWhereClauseItemContext.class,0);
		}
		public WhereClauseItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whereClauseItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterWhereClauseItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitWhereClauseItem(this);
		}
	}

	public final WhereClauseItemContext whereClauseItem() throws RecognitionException {
		WhereClauseItemContext _localctx = new WhereClauseItemContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_whereClauseItem);
		try {
			setState(1198);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,146,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1196);
				lifetimeWhereClauseItem();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1197);
				typeBoundWhereClauseItem();
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
	public static class LifetimeWhereClauseItemContext extends ParserRuleContext {
		public LifetimeContext lifetime() {
			return getRuleContext(LifetimeContext.class,0);
		}
		public TerminalNode COLON() { return getToken(RustParser.COLON, 0); }
		public LifetimeBoundsContext lifetimeBounds() {
			return getRuleContext(LifetimeBoundsContext.class,0);
		}
		public LifetimeWhereClauseItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lifetimeWhereClauseItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterLifetimeWhereClauseItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitLifetimeWhereClauseItem(this);
		}
	}

	public final LifetimeWhereClauseItemContext lifetimeWhereClauseItem() throws RecognitionException {
		LifetimeWhereClauseItemContext _localctx = new LifetimeWhereClauseItemContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_lifetimeWhereClauseItem);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1200);
			lifetime();
			setState(1201);
			match(COLON);
			setState(1202);
			lifetimeBounds();
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
	public static class TypeBoundWhereClauseItemContext extends ParserRuleContext {
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public TerminalNode COLON() { return getToken(RustParser.COLON, 0); }
		public ForLifetimesContext forLifetimes() {
			return getRuleContext(ForLifetimesContext.class,0);
		}
		public TypeParamBoundsContext typeParamBounds() {
			return getRuleContext(TypeParamBoundsContext.class,0);
		}
		public TypeBoundWhereClauseItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeBoundWhereClauseItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTypeBoundWhereClauseItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTypeBoundWhereClauseItem(this);
		}
	}

	public final TypeBoundWhereClauseItemContext typeBoundWhereClauseItem() throws RecognitionException {
		TypeBoundWhereClauseItemContext _localctx = new TypeBoundWhereClauseItemContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_typeBoundWhereClauseItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1205);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,147,_ctx) ) {
			case 1:
				{
				setState(1204);
				forLifetimes();
				}
				break;
			}
			setState(1207);
			type_();
			setState(1208);
			match(COLON);
			setState(1210);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 567453553367451680L) != 0 || (((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 290545947639809L) != 0) {
				{
				setState(1209);
				typeParamBounds();
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
	public static class ForLifetimesContext extends ParserRuleContext {
		public TerminalNode KW_FOR() { return getToken(RustParser.KW_FOR, 0); }
		public GenericParamsContext genericParams() {
			return getRuleContext(GenericParamsContext.class,0);
		}
		public ForLifetimesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_forLifetimes; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterForLifetimes(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitForLifetimes(this);
		}
	}

	public final ForLifetimesContext forLifetimes() throws RecognitionException {
		ForLifetimesContext _localctx = new ForLifetimesContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_forLifetimes);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1212);
			match(KW_FOR);
			setState(1213);
			genericParams();
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
	public static class AssociatedItemContext extends ParserRuleContext {
		public MacroInvocationSemiContext macroInvocationSemi() {
			return getRuleContext(MacroInvocationSemiContext.class,0);
		}
		public List<OuterAttributeContext> outerAttribute() {
			return getRuleContexts(OuterAttributeContext.class);
		}
		public OuterAttributeContext outerAttribute(int i) {
			return getRuleContext(OuterAttributeContext.class,i);
		}
		public TypeAliasContext typeAlias() {
			return getRuleContext(TypeAliasContext.class,0);
		}
		public ConstantItemContext constantItem() {
			return getRuleContext(ConstantItemContext.class,0);
		}
		public Function_Context function_() {
			return getRuleContext(Function_Context.class,0);
		}
		public VisibilityContext visibility() {
			return getRuleContext(VisibilityContext.class,0);
		}
		public AssociatedItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_associatedItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterAssociatedItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitAssociatedItem(this);
		}
	}

	public final AssociatedItemContext associatedItem() throws RecognitionException {
		AssociatedItemContext _localctx = new AssociatedItemContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_associatedItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1218);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(1215);
				outerAttribute();
				}
				}
				setState(1220);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1230);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_CRATE:
			case KW_SELFVALUE:
			case KW_SUPER:
			case KW_MACRORULES:
			case KW_DOLLARCRATE:
			case NON_KEYWORD_IDENTIFIER:
			case RAW_IDENTIFIER:
			case PATHSEP:
				{
				setState(1221);
				macroInvocationSemi();
				}
				break;
			case KW_CONST:
			case KW_EXTERN:
			case KW_FN:
			case KW_PUB:
			case KW_TYPE:
			case KW_UNSAFE:
			case KW_ASYNC:
				{
				setState(1223);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==KW_PUB) {
					{
					setState(1222);
					visibility();
					}
				}

				setState(1228);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,151,_ctx) ) {
				case 1:
					{
					setState(1225);
					typeAlias();
					}
					break;
				case 2:
					{
					setState(1226);
					constantItem();
					}
					break;
				case 3:
					{
					setState(1227);
					function_();
					}
					break;
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
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
	public static class InnerAttributeContext extends ParserRuleContext {
		public TerminalNode POUND() { return getToken(RustParser.POUND, 0); }
		public TerminalNode NOT() { return getToken(RustParser.NOT, 0); }
		public TerminalNode LSQUAREBRACKET() { return getToken(RustParser.LSQUAREBRACKET, 0); }
		public AttrContext attr() {
			return getRuleContext(AttrContext.class,0);
		}
		public TerminalNode RSQUAREBRACKET() { return getToken(RustParser.RSQUAREBRACKET, 0); }
		public InnerAttributeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_innerAttribute; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterInnerAttribute(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitInnerAttribute(this);
		}
	}

	public final InnerAttributeContext innerAttribute() throws RecognitionException {
		InnerAttributeContext _localctx = new InnerAttributeContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_innerAttribute);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1232);
			match(POUND);
			setState(1233);
			match(NOT);
			setState(1234);
			match(LSQUAREBRACKET);
			setState(1235);
			attr();
			setState(1236);
			match(RSQUAREBRACKET);
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
	public static class OuterAttributeContext extends ParserRuleContext {
		public TerminalNode POUND() { return getToken(RustParser.POUND, 0); }
		public TerminalNode LSQUAREBRACKET() { return getToken(RustParser.LSQUAREBRACKET, 0); }
		public AttrContext attr() {
			return getRuleContext(AttrContext.class,0);
		}
		public TerminalNode RSQUAREBRACKET() { return getToken(RustParser.RSQUAREBRACKET, 0); }
		public OuterAttributeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_outerAttribute; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterOuterAttribute(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitOuterAttribute(this);
		}
	}

	public final OuterAttributeContext outerAttribute() throws RecognitionException {
		OuterAttributeContext _localctx = new OuterAttributeContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_outerAttribute);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1238);
			match(POUND);
			setState(1239);
			match(LSQUAREBRACKET);
			setState(1240);
			attr();
			setState(1241);
			match(RSQUAREBRACKET);
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
	public static class AttrContext extends ParserRuleContext {
		public SimplePathContext simplePath() {
			return getRuleContext(SimplePathContext.class,0);
		}
		public AttrInputContext attrInput() {
			return getRuleContext(AttrInputContext.class,0);
		}
		public AttrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterAttr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitAttr(this);
		}
	}

	public final AttrContext attr() throws RecognitionException {
		AttrContext _localctx = new AttrContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_attr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1243);
			simplePath();
			setState(1245);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la - 101)) & ~0x3f) == 0 && ((1L << (_la - 101)) & 88080385L) != 0) {
				{
				setState(1244);
				attrInput();
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
	public static class AttrInputContext extends ParserRuleContext {
		public DelimTokenTreeContext delimTokenTree() {
			return getRuleContext(DelimTokenTreeContext.class,0);
		}
		public TerminalNode EQ() { return getToken(RustParser.EQ, 0); }
		public LiteralExpressionContext literalExpression() {
			return getRuleContext(LiteralExpressionContext.class,0);
		}
		public AttrInputContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attrInput; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterAttrInput(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitAttrInput(this);
		}
	}

	public final AttrInputContext attrInput() throws RecognitionException {
		AttrInputContext _localctx = new AttrInputContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_attrInput);
		try {
			setState(1250);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LCURLYBRACE:
			case LSQUAREBRACKET:
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(1247);
				delimTokenTree();
				}
				break;
			case EQ:
				enterOuterAlt(_localctx, 2);
				{
				setState(1248);
				match(EQ);
				setState(1249);
				literalExpression();
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
	public static class StatementContext extends ParserRuleContext {
		public TerminalNode SEMI() { return getToken(RustParser.SEMI, 0); }
		public ItemContext item() {
			return getRuleContext(ItemContext.class,0);
		}
		public LetStatementContext letStatement() {
			return getRuleContext(LetStatementContext.class,0);
		}
		public ExpressionStatementContext expressionStatement() {
			return getRuleContext(ExpressionStatementContext.class,0);
		}
		public MacroInvocationSemiContext macroInvocationSemi() {
			return getRuleContext(MacroInvocationSemiContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitStatement(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_statement);
		try {
			setState(1257);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,155,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1252);
				match(SEMI);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1253);
				item();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1254);
				letStatement();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1255);
				expressionStatement();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1256);
				macroInvocationSemi();
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
	public static class LetStatementContext extends ParserRuleContext {
		public TerminalNode KW_LET() { return getToken(RustParser.KW_LET, 0); }
		public PatternNoTopAltContext patternNoTopAlt() {
			return getRuleContext(PatternNoTopAltContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(RustParser.SEMI, 0); }
		public List<OuterAttributeContext> outerAttribute() {
			return getRuleContexts(OuterAttributeContext.class);
		}
		public OuterAttributeContext outerAttribute(int i) {
			return getRuleContext(OuterAttributeContext.class,i);
		}
		public TerminalNode COLON() { return getToken(RustParser.COLON, 0); }
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public TerminalNode EQ() { return getToken(RustParser.EQ, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public LetStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_letStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterLetStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitLetStatement(this);
		}
	}

	public final LetStatementContext letStatement() throws RecognitionException {
		LetStatementContext _localctx = new LetStatementContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_letStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1262);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(1259);
				outerAttribute();
				}
				}
				setState(1264);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1265);
			match(KW_LET);
			setState(1266);
			patternNoTopAlt();
			setState(1269);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(1267);
				match(COLON);
				setState(1268);
				type_();
				}
			}

			setState(1273);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ) {
				{
				setState(1271);
				match(EQ);
				setState(1272);
				expression(0);
				}
			}

			setState(1275);
			match(SEMI);
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
	public static class ExpressionStatementContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(RustParser.SEMI, 0); }
		public ExpressionWithBlockContext expressionWithBlock() {
			return getRuleContext(ExpressionWithBlockContext.class,0);
		}
		public ExpressionStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterExpressionStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitExpressionStatement(this);
		}
	}

	public final ExpressionStatementContext expressionStatement() throws RecognitionException {
		ExpressionStatementContext _localctx = new ExpressionStatementContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_expressionStatement);
		try {
			setState(1284);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,160,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1277);
				expression(0);
				setState(1278);
				match(SEMI);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1280);
				expressionWithBlock();
				setState(1282);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,159,_ctx) ) {
				case 1:
					{
					setState(1281);
					match(SEMI);
					}
					break;
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
	public static class ExpressionContext extends ParserRuleContext {
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
	 
		public ExpressionContext() { }
		public void copyFrom(ExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TypeCastExpressionContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode KW_AS() { return getToken(RustParser.KW_AS, 0); }
		public TypeNoBoundsContext typeNoBounds() {
			return getRuleContext(TypeNoBoundsContext.class,0);
		}
		public TypeCastExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTypeCastExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTypeCastExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PathExpression_Context extends ExpressionContext {
		public PathExpressionContext pathExpression() {
			return getRuleContext(PathExpressionContext.class,0);
		}
		public PathExpression_Context(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterPathExpression_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitPathExpression_(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TupleExpressionContext extends ExpressionContext {
		public TerminalNode LPAREN() { return getToken(RustParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(RustParser.RPAREN, 0); }
		public List<InnerAttributeContext> innerAttribute() {
			return getRuleContexts(InnerAttributeContext.class);
		}
		public InnerAttributeContext innerAttribute(int i) {
			return getRuleContext(InnerAttributeContext.class,i);
		}
		public TupleElementsContext tupleElements() {
			return getRuleContext(TupleElementsContext.class,0);
		}
		public TupleExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTupleExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTupleExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IndexExpressionContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode LSQUAREBRACKET() { return getToken(RustParser.LSQUAREBRACKET, 0); }
		public TerminalNode RSQUAREBRACKET() { return getToken(RustParser.RSQUAREBRACKET, 0); }
		public IndexExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterIndexExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitIndexExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RangeExpressionContext extends ExpressionContext {
		public TerminalNode DOTDOT() { return getToken(RustParser.DOTDOT, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode DOTDOTEQ() { return getToken(RustParser.DOTDOTEQ, 0); }
		public RangeExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterRangeExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitRangeExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MacroInvocationAsExpressionContext extends ExpressionContext {
		public MacroInvocationContext macroInvocation() {
			return getRuleContext(MacroInvocationContext.class,0);
		}
		public MacroInvocationAsExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMacroInvocationAsExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMacroInvocationAsExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ReturnExpressionContext extends ExpressionContext {
		public TerminalNode KW_RETURN() { return getToken(RustParser.KW_RETURN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ReturnExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterReturnExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitReturnExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AwaitExpressionContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode DOT() { return getToken(RustParser.DOT, 0); }
		public TerminalNode KW_AWAIT() { return getToken(RustParser.KW_AWAIT, 0); }
		public AwaitExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterAwaitExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitAwaitExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ErrorPropagationExpressionContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode QUESTION() { return getToken(RustParser.QUESTION, 0); }
		public ErrorPropagationExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterErrorPropagationExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitErrorPropagationExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ContinueExpressionContext extends ExpressionContext {
		public TerminalNode KW_CONTINUE() { return getToken(RustParser.KW_CONTINUE, 0); }
		public TerminalNode LIFETIME_OR_LABEL() { return getToken(RustParser.LIFETIME_OR_LABEL, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ContinueExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterContinueExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitContinueExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentExpressionContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode EQ() { return getToken(RustParser.EQ, 0); }
		public AssignmentExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterAssignmentExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitAssignmentExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MethodCallExpressionContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode DOT() { return getToken(RustParser.DOT, 0); }
		public PathExprSegmentContext pathExprSegment() {
			return getRuleContext(PathExprSegmentContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(RustParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(RustParser.RPAREN, 0); }
		public CallParamsContext callParams() {
			return getRuleContext(CallParamsContext.class,0);
		}
		public MethodCallExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMethodCallExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMethodCallExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LiteralExpression_Context extends ExpressionContext {
		public LiteralExpressionContext literalExpression() {
			return getRuleContext(LiteralExpressionContext.class,0);
		}
		public LiteralExpression_Context(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterLiteralExpression_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitLiteralExpression_(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StructExpression_Context extends ExpressionContext {
		public StructExpressionContext structExpression() {
			return getRuleContext(StructExpressionContext.class,0);
		}
		public StructExpression_Context(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterStructExpression_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitStructExpression_(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TupleIndexingExpressionContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode DOT() { return getToken(RustParser.DOT, 0); }
		public TupleIndexContext tupleIndex() {
			return getRuleContext(TupleIndexContext.class,0);
		}
		public TupleIndexingExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTupleIndexingExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTupleIndexingExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NegationExpressionContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode MINUS() { return getToken(RustParser.MINUS, 0); }
		public TerminalNode NOT() { return getToken(RustParser.NOT, 0); }
		public NegationExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterNegationExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitNegationExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CallExpressionContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(RustParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(RustParser.RPAREN, 0); }
		public CallParamsContext callParams() {
			return getRuleContext(CallParamsContext.class,0);
		}
		public CallExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterCallExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitCallExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LazyBooleanExpressionContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode ANDAND() { return getToken(RustParser.ANDAND, 0); }
		public TerminalNode OROR() { return getToken(RustParser.OROR, 0); }
		public LazyBooleanExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterLazyBooleanExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitLazyBooleanExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DereferenceExpressionContext extends ExpressionContext {
		public TerminalNode STAR() { return getToken(RustParser.STAR, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public DereferenceExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterDereferenceExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitDereferenceExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionWithBlock_Context extends ExpressionContext {
		public ExpressionWithBlockContext expressionWithBlock() {
			return getRuleContext(ExpressionWithBlockContext.class,0);
		}
		public ExpressionWithBlock_Context(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterExpressionWithBlock_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitExpressionWithBlock_(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GroupedExpressionContext extends ExpressionContext {
		public TerminalNode LPAREN() { return getToken(RustParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(RustParser.RPAREN, 0); }
		public List<InnerAttributeContext> innerAttribute() {
			return getRuleContexts(InnerAttributeContext.class);
		}
		public InnerAttributeContext innerAttribute(int i) {
			return getRuleContext(InnerAttributeContext.class,i);
		}
		public GroupedExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterGroupedExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitGroupedExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BreakExpressionContext extends ExpressionContext {
		public TerminalNode KW_BREAK() { return getToken(RustParser.KW_BREAK, 0); }
		public TerminalNode LIFETIME_OR_LABEL() { return getToken(RustParser.LIFETIME_OR_LABEL, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public BreakExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterBreakExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitBreakExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ArithmeticOrLogicalExpressionContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode STAR() { return getToken(RustParser.STAR, 0); }
		public TerminalNode SLASH() { return getToken(RustParser.SLASH, 0); }
		public TerminalNode PERCENT() { return getToken(RustParser.PERCENT, 0); }
		public TerminalNode PLUS() { return getToken(RustParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(RustParser.MINUS, 0); }
		public ShlContext shl() {
			return getRuleContext(ShlContext.class,0);
		}
		public ShrContext shr() {
			return getRuleContext(ShrContext.class,0);
		}
		public TerminalNode AND() { return getToken(RustParser.AND, 0); }
		public TerminalNode CARET() { return getToken(RustParser.CARET, 0); }
		public TerminalNode OR() { return getToken(RustParser.OR, 0); }
		public ArithmeticOrLogicalExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterArithmeticOrLogicalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitArithmeticOrLogicalExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FieldExpressionContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode DOT() { return getToken(RustParser.DOT, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public FieldExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterFieldExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitFieldExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class EnumerationVariantExpression_Context extends ExpressionContext {
		public EnumerationVariantExpressionContext enumerationVariantExpression() {
			return getRuleContext(EnumerationVariantExpressionContext.class,0);
		}
		public EnumerationVariantExpression_Context(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterEnumerationVariantExpression_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitEnumerationVariantExpression_(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ComparisonExpressionContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public ComparisonExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterComparisonExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitComparisonExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AttributedExpressionContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<OuterAttributeContext> outerAttribute() {
			return getRuleContexts(OuterAttributeContext.class);
		}
		public OuterAttributeContext outerAttribute(int i) {
			return getRuleContext(OuterAttributeContext.class,i);
		}
		public AttributedExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterAttributedExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitAttributedExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BorrowExpressionContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode AND() { return getToken(RustParser.AND, 0); }
		public TerminalNode ANDAND() { return getToken(RustParser.ANDAND, 0); }
		public TerminalNode KW_MUT() { return getToken(RustParser.KW_MUT, 0); }
		public BorrowExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterBorrowExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitBorrowExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CompoundAssignmentExpressionContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public CompoundAssignOperatorContext compoundAssignOperator() {
			return getRuleContext(CompoundAssignOperatorContext.class,0);
		}
		public CompoundAssignmentExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterCompoundAssignmentExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitCompoundAssignmentExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ClosureExpression_Context extends ExpressionContext {
		public ClosureExpressionContext closureExpression() {
			return getRuleContext(ClosureExpressionContext.class,0);
		}
		public ClosureExpression_Context(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterClosureExpression_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitClosureExpression_(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ArrayExpressionContext extends ExpressionContext {
		public TerminalNode LSQUAREBRACKET() { return getToken(RustParser.LSQUAREBRACKET, 0); }
		public TerminalNode RSQUAREBRACKET() { return getToken(RustParser.RSQUAREBRACKET, 0); }
		public List<InnerAttributeContext> innerAttribute() {
			return getRuleContexts(InnerAttributeContext.class);
		}
		public InnerAttributeContext innerAttribute(int i) {
			return getRuleContext(InnerAttributeContext.class,i);
		}
		public ArrayElementsContext arrayElements() {
			return getRuleContext(ArrayElementsContext.class,0);
		}
		public ArrayExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterArrayExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitArrayExpression(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 154;
		enterRecursionRule(_localctx, 154, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1366);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,174,_ctx) ) {
			case 1:
				{
				_localctx = new AttributedExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(1288); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(1287);
						outerAttribute();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1290); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,161,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(1292);
				expression(40);
				}
				break;
			case 2:
				{
				_localctx = new LiteralExpression_Context(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1294);
				literalExpression();
				}
				break;
			case 3:
				{
				_localctx = new PathExpression_Context(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1295);
				pathExpression();
				}
				break;
			case 4:
				{
				_localctx = new BorrowExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1296);
				_la = _input.LA(1);
				if ( !(_la==AND || _la==ANDAND) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1298);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==KW_MUT) {
					{
					setState(1297);
					match(KW_MUT);
					}
				}

				setState(1300);
				expression(30);
				}
				break;
			case 5:
				{
				_localctx = new DereferenceExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1301);
				match(STAR);
				setState(1302);
				expression(29);
				}
				break;
			case 6:
				{
				_localctx = new NegationExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1303);
				_la = _input.LA(1);
				if ( !(_la==MINUS || _la==NOT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1304);
				expression(28);
				}
				break;
			case 7:
				{
				_localctx = new RangeExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1305);
				match(DOTDOT);
				setState(1307);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,163,_ctx) ) {
				case 1:
					{
					setState(1306);
					expression(0);
					}
					break;
				}
				}
				break;
			case 8:
				{
				_localctx = new RangeExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1309);
				match(DOTDOTEQ);
				setState(1310);
				expression(15);
				}
				break;
			case 9:
				{
				_localctx = new ContinueExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1311);
				match(KW_CONTINUE);
				setState(1313);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,164,_ctx) ) {
				case 1:
					{
					setState(1312);
					match(LIFETIME_OR_LABEL);
					}
					break;
				}
				setState(1316);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,165,_ctx) ) {
				case 1:
					{
					setState(1315);
					expression(0);
					}
					break;
				}
				}
				break;
			case 10:
				{
				_localctx = new BreakExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1318);
				match(KW_BREAK);
				setState(1320);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,166,_ctx) ) {
				case 1:
					{
					setState(1319);
					match(LIFETIME_OR_LABEL);
					}
					break;
				}
				setState(1323);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,167,_ctx) ) {
				case 1:
					{
					setState(1322);
					expression(0);
					}
					break;
				}
				}
				break;
			case 11:
				{
				_localctx = new ReturnExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1325);
				match(KW_RETURN);
				setState(1327);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,168,_ctx) ) {
				case 1:
					{
					setState(1326);
					expression(0);
					}
					break;
				}
				}
				break;
			case 12:
				{
				_localctx = new GroupedExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1329);
				match(LPAREN);
				setState(1333);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,169,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1330);
						innerAttribute();
						}
						} 
					}
					setState(1335);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,169,_ctx);
				}
				setState(1336);
				expression(0);
				setState(1337);
				match(RPAREN);
				}
				break;
			case 13:
				{
				_localctx = new ArrayExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1339);
				match(LSQUAREBRACKET);
				setState(1343);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,170,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1340);
						innerAttribute();
						}
						} 
					}
					setState(1345);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,170,_ctx);
				}
				setState(1347);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417665550785076L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1523430809782507647L) != 0) {
					{
					setState(1346);
					arrayElements();
					}
				}

				setState(1349);
				match(RSQUAREBRACKET);
				}
				break;
			case 14:
				{
				_localctx = new TupleExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1350);
				match(LPAREN);
				setState(1354);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,172,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1351);
						innerAttribute();
						}
						} 
					}
					setState(1356);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,172,_ctx);
				}
				setState(1358);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417665550785076L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1523430809782507647L) != 0) {
					{
					setState(1357);
					tupleElements();
					}
				}

				setState(1360);
				match(RPAREN);
				}
				break;
			case 15:
				{
				_localctx = new StructExpression_Context(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1361);
				structExpression();
				}
				break;
			case 16:
				{
				_localctx = new EnumerationVariantExpression_Context(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1362);
				enumerationVariantExpression();
				}
				break;
			case 17:
				{
				_localctx = new ClosureExpression_Context(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1363);
				closureExpression();
				}
				break;
			case 18:
				{
				_localctx = new ExpressionWithBlock_Context(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1364);
				expressionWithBlock();
				}
				break;
			case 19:
				{
				_localctx = new MacroInvocationAsExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1365);
				macroInvocation();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(1451);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,180,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(1449);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,179,_ctx) ) {
					case 1:
						{
						_localctx = new ArithmeticOrLogicalExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1368);
						if (!(precpred(_ctx, 26))) throw new FailedPredicateException(this, "precpred(_ctx, 26)");
						setState(1369);
						_la = _input.LA(1);
						if ( !((((_la - 82)) & ~0x3f) == 0 && ((1L << (_la - 82)) & 7L) != 0) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(1370);
						expression(27);
						}
						break;
					case 2:
						{
						_localctx = new ArithmeticOrLogicalExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1371);
						if (!(precpred(_ctx, 25))) throw new FailedPredicateException(this, "precpred(_ctx, 25)");
						setState(1372);
						_la = _input.LA(1);
						if ( !(_la==PLUS || _la==MINUS) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(1373);
						expression(26);
						}
						break;
					case 3:
						{
						_localctx = new ArithmeticOrLogicalExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1374);
						if (!(precpred(_ctx, 24))) throw new FailedPredicateException(this, "precpred(_ctx, 24)");
						setState(1377);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case LT:
							{
							setState(1375);
							shl();
							}
							break;
						case GT:
							{
							setState(1376);
							shr();
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						setState(1379);
						expression(25);
						}
						break;
					case 4:
						{
						_localctx = new ArithmeticOrLogicalExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1381);
						if (!(precpred(_ctx, 23))) throw new FailedPredicateException(this, "precpred(_ctx, 23)");
						setState(1382);
						match(AND);
						setState(1383);
						expression(24);
						}
						break;
					case 5:
						{
						_localctx = new ArithmeticOrLogicalExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1384);
						if (!(precpred(_ctx, 22))) throw new FailedPredicateException(this, "precpred(_ctx, 22)");
						setState(1385);
						match(CARET);
						setState(1386);
						expression(23);
						}
						break;
					case 6:
						{
						_localctx = new ArithmeticOrLogicalExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1387);
						if (!(precpred(_ctx, 21))) throw new FailedPredicateException(this, "precpred(_ctx, 21)");
						setState(1388);
						match(OR);
						setState(1389);
						expression(22);
						}
						break;
					case 7:
						{
						_localctx = new ComparisonExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1390);
						if (!(precpred(_ctx, 20))) throw new FailedPredicateException(this, "precpred(_ctx, 20)");
						setState(1391);
						comparisonOperator();
						setState(1392);
						expression(21);
						}
						break;
					case 8:
						{
						_localctx = new LazyBooleanExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1394);
						if (!(precpred(_ctx, 19))) throw new FailedPredicateException(this, "precpred(_ctx, 19)");
						setState(1395);
						match(ANDAND);
						setState(1396);
						expression(20);
						}
						break;
					case 9:
						{
						_localctx = new LazyBooleanExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1397);
						if (!(precpred(_ctx, 18))) throw new FailedPredicateException(this, "precpred(_ctx, 18)");
						setState(1398);
						match(OROR);
						setState(1399);
						expression(19);
						}
						break;
					case 10:
						{
						_localctx = new RangeExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1400);
						if (!(precpred(_ctx, 14))) throw new FailedPredicateException(this, "precpred(_ctx, 14)");
						setState(1401);
						match(DOTDOTEQ);
						setState(1402);
						expression(15);
						}
						break;
					case 11:
						{
						_localctx = new AssignmentExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1403);
						if (!(precpred(_ctx, 13))) throw new FailedPredicateException(this, "precpred(_ctx, 13)");
						setState(1404);
						match(EQ);
						setState(1405);
						expression(14);
						}
						break;
					case 12:
						{
						_localctx = new CompoundAssignmentExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1406);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(1407);
						compoundAssignOperator();
						setState(1408);
						expression(13);
						}
						break;
					case 13:
						{
						_localctx = new MethodCallExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1410);
						if (!(precpred(_ctx, 37))) throw new FailedPredicateException(this, "precpred(_ctx, 37)");
						setState(1411);
						match(DOT);
						setState(1412);
						pathExprSegment();
						setState(1413);
						match(LPAREN);
						setState(1415);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417665550785076L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1523430809782507647L) != 0) {
							{
							setState(1414);
							callParams();
							}
						}

						setState(1417);
						match(RPAREN);
						}
						break;
					case 14:
						{
						_localctx = new FieldExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1419);
						if (!(precpred(_ctx, 36))) throw new FailedPredicateException(this, "precpred(_ctx, 36)");
						setState(1420);
						match(DOT);
						setState(1421);
						identifier();
						}
						break;
					case 15:
						{
						_localctx = new TupleIndexingExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1422);
						if (!(precpred(_ctx, 35))) throw new FailedPredicateException(this, "precpred(_ctx, 35)");
						setState(1423);
						match(DOT);
						setState(1424);
						tupleIndex();
						}
						break;
					case 16:
						{
						_localctx = new AwaitExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1425);
						if (!(precpred(_ctx, 34))) throw new FailedPredicateException(this, "precpred(_ctx, 34)");
						setState(1426);
						match(DOT);
						setState(1427);
						match(KW_AWAIT);
						}
						break;
					case 17:
						{
						_localctx = new CallExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1428);
						if (!(precpred(_ctx, 33))) throw new FailedPredicateException(this, "precpred(_ctx, 33)");
						setState(1429);
						match(LPAREN);
						setState(1431);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417665550785076L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1523430809782507647L) != 0) {
							{
							setState(1430);
							callParams();
							}
						}

						setState(1433);
						match(RPAREN);
						}
						break;
					case 18:
						{
						_localctx = new IndexExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1434);
						if (!(precpred(_ctx, 32))) throw new FailedPredicateException(this, "precpred(_ctx, 32)");
						setState(1435);
						match(LSQUAREBRACKET);
						setState(1436);
						expression(0);
						setState(1437);
						match(RSQUAREBRACKET);
						}
						break;
					case 19:
						{
						_localctx = new ErrorPropagationExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1439);
						if (!(precpred(_ctx, 31))) throw new FailedPredicateException(this, "precpred(_ctx, 31)");
						setState(1440);
						match(QUESTION);
						}
						break;
					case 20:
						{
						_localctx = new TypeCastExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1441);
						if (!(precpred(_ctx, 27))) throw new FailedPredicateException(this, "precpred(_ctx, 27)");
						setState(1442);
						match(KW_AS);
						setState(1443);
						typeNoBounds();
						}
						break;
					case 21:
						{
						_localctx = new RangeExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1444);
						if (!(precpred(_ctx, 17))) throw new FailedPredicateException(this, "precpred(_ctx, 17)");
						setState(1445);
						match(DOTDOT);
						setState(1447);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,178,_ctx) ) {
						case 1:
							{
							setState(1446);
							expression(0);
							}
							break;
						}
						}
						break;
					}
					} 
				}
				setState(1453);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,180,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ComparisonOperatorContext extends ParserRuleContext {
		public TerminalNode EQEQ() { return getToken(RustParser.EQEQ, 0); }
		public TerminalNode NE() { return getToken(RustParser.NE, 0); }
		public TerminalNode GT() { return getToken(RustParser.GT, 0); }
		public TerminalNode LT() { return getToken(RustParser.LT, 0); }
		public TerminalNode GE() { return getToken(RustParser.GE, 0); }
		public TerminalNode LE() { return getToken(RustParser.LE, 0); }
		public ComparisonOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparisonOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterComparisonOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitComparisonOperator(this);
		}
	}

	public final ComparisonOperatorContext comparisonOperator() throws RecognitionException {
		ComparisonOperatorContext _localctx = new ComparisonOperatorContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_comparisonOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1454);
			_la = _input.LA(1);
			if ( !((((_la - 102)) & ~0x3f) == 0 && ((1L << (_la - 102)) & 63L) != 0) ) {
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
	public static class CompoundAssignOperatorContext extends ParserRuleContext {
		public TerminalNode PLUSEQ() { return getToken(RustParser.PLUSEQ, 0); }
		public TerminalNode MINUSEQ() { return getToken(RustParser.MINUSEQ, 0); }
		public TerminalNode STAREQ() { return getToken(RustParser.STAREQ, 0); }
		public TerminalNode SLASHEQ() { return getToken(RustParser.SLASHEQ, 0); }
		public TerminalNode PERCENTEQ() { return getToken(RustParser.PERCENTEQ, 0); }
		public TerminalNode ANDEQ() { return getToken(RustParser.ANDEQ, 0); }
		public TerminalNode OREQ() { return getToken(RustParser.OREQ, 0); }
		public TerminalNode CARETEQ() { return getToken(RustParser.CARETEQ, 0); }
		public TerminalNode SHLEQ() { return getToken(RustParser.SHLEQ, 0); }
		public TerminalNode SHREQ() { return getToken(RustParser.SHREQ, 0); }
		public CompoundAssignOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_compoundAssignOperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterCompoundAssignOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitCompoundAssignOperator(this);
		}
	}

	public final CompoundAssignOperatorContext compoundAssignOperator() throws RecognitionException {
		CompoundAssignOperatorContext _localctx = new CompoundAssignOperatorContext(_ctx, getState());
		enterRule(_localctx, 158, RULE_compoundAssignOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1456);
			_la = _input.LA(1);
			if ( !((((_la - 91)) & ~0x3f) == 0 && ((1L << (_la - 91)) & 1023L) != 0) ) {
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
	public static class ExpressionWithBlockContext extends ParserRuleContext {
		public ExpressionWithBlockContext expressionWithBlock() {
			return getRuleContext(ExpressionWithBlockContext.class,0);
		}
		public List<OuterAttributeContext> outerAttribute() {
			return getRuleContexts(OuterAttributeContext.class);
		}
		public OuterAttributeContext outerAttribute(int i) {
			return getRuleContext(OuterAttributeContext.class,i);
		}
		public BlockExpressionContext blockExpression() {
			return getRuleContext(BlockExpressionContext.class,0);
		}
		public AsyncBlockExpressionContext asyncBlockExpression() {
			return getRuleContext(AsyncBlockExpressionContext.class,0);
		}
		public UnsafeBlockExpressionContext unsafeBlockExpression() {
			return getRuleContext(UnsafeBlockExpressionContext.class,0);
		}
		public LoopExpressionContext loopExpression() {
			return getRuleContext(LoopExpressionContext.class,0);
		}
		public IfExpressionContext ifExpression() {
			return getRuleContext(IfExpressionContext.class,0);
		}
		public IfLetExpressionContext ifLetExpression() {
			return getRuleContext(IfLetExpressionContext.class,0);
		}
		public MatchExpressionContext matchExpression() {
			return getRuleContext(MatchExpressionContext.class,0);
		}
		public ExpressionWithBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionWithBlock; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterExpressionWithBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitExpressionWithBlock(this);
		}
	}

	public final ExpressionWithBlockContext expressionWithBlock() throws RecognitionException {
		ExpressionWithBlockContext _localctx = new ExpressionWithBlockContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_expressionWithBlock);
		try {
			int _alt;
			setState(1472);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,182,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1459); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(1458);
						outerAttribute();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1461); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,181,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(1463);
				expressionWithBlock();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1465);
				blockExpression();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1466);
				asyncBlockExpression();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1467);
				unsafeBlockExpression();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1468);
				loopExpression();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1469);
				ifExpression();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1470);
				ifLetExpression();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(1471);
				matchExpression();
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
	public static class LiteralExpressionContext extends ParserRuleContext {
		public TerminalNode CHAR_LITERAL() { return getToken(RustParser.CHAR_LITERAL, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(RustParser.STRING_LITERAL, 0); }
		public TerminalNode RAW_STRING_LITERAL() { return getToken(RustParser.RAW_STRING_LITERAL, 0); }
		public TerminalNode BYTE_LITERAL() { return getToken(RustParser.BYTE_LITERAL, 0); }
		public TerminalNode BYTE_STRING_LITERAL() { return getToken(RustParser.BYTE_STRING_LITERAL, 0); }
		public TerminalNode RAW_BYTE_STRING_LITERAL() { return getToken(RustParser.RAW_BYTE_STRING_LITERAL, 0); }
		public TerminalNode INTEGER_LITERAL() { return getToken(RustParser.INTEGER_LITERAL, 0); }
		public TerminalNode FLOAT_LITERAL() { return getToken(RustParser.FLOAT_LITERAL, 0); }
		public TerminalNode KW_TRUE() { return getToken(RustParser.KW_TRUE, 0); }
		public TerminalNode KW_FALSE() { return getToken(RustParser.KW_FALSE, 0); }
		public LiteralExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literalExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterLiteralExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitLiteralExpression(this);
		}
	}

	public final LiteralExpressionContext literalExpression() throws RecognitionException {
		LiteralExpressionContext _localctx = new LiteralExpressionContext(_ctx, getState());
		enterRule(_localctx, 162, RULE_literalExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1474);
			_la = _input.LA(1);
			if ( !(_la==KW_FALSE || _la==KW_TRUE || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 2175L) != 0) ) {
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
	public static class PathExpressionContext extends ParserRuleContext {
		public PathInExpressionContext pathInExpression() {
			return getRuleContext(PathInExpressionContext.class,0);
		}
		public QualifiedPathInExpressionContext qualifiedPathInExpression() {
			return getRuleContext(QualifiedPathInExpressionContext.class,0);
		}
		public PathExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterPathExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitPathExpression(this);
		}
	}

	public final PathExpressionContext pathExpression() throws RecognitionException {
		PathExpressionContext _localctx = new PathExpressionContext(_ctx, getState());
		enterRule(_localctx, 164, RULE_pathExpression);
		try {
			setState(1478);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_CRATE:
			case KW_SELFVALUE:
			case KW_SELFTYPE:
			case KW_SUPER:
			case KW_MACRORULES:
			case KW_DOLLARCRATE:
			case NON_KEYWORD_IDENTIFIER:
			case RAW_IDENTIFIER:
			case PATHSEP:
				enterOuterAlt(_localctx, 1);
				{
				setState(1476);
				pathInExpression();
				}
				break;
			case LT:
				enterOuterAlt(_localctx, 2);
				{
				setState(1477);
				qualifiedPathInExpression();
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
	public static class BlockExpressionContext extends ParserRuleContext {
		public TerminalNode LCURLYBRACE() { return getToken(RustParser.LCURLYBRACE, 0); }
		public TerminalNode RCURLYBRACE() { return getToken(RustParser.RCURLYBRACE, 0); }
		public List<InnerAttributeContext> innerAttribute() {
			return getRuleContexts(InnerAttributeContext.class);
		}
		public InnerAttributeContext innerAttribute(int i) {
			return getRuleContext(InnerAttributeContext.class,i);
		}
		public StatementsContext statements() {
			return getRuleContext(StatementsContext.class,0);
		}
		public BlockExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_blockExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterBlockExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitBlockExpression(this);
		}
	}

	public final BlockExpressionContext blockExpression() throws RecognitionException {
		BlockExpressionContext _localctx = new BlockExpressionContext(_ctx, getState());
		enterRule(_localctx, 166, RULE_blockExpression);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1480);
			match(LCURLYBRACE);
			setState(1484);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,184,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1481);
					innerAttribute();
					}
					} 
				}
				setState(1486);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,184,_ctx);
			}
			setState(1488);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 526921276656172988L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1523712284759218303L) != 0) {
				{
				setState(1487);
				statements();
				}
			}

			setState(1490);
			match(RCURLYBRACE);
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
	public static class StatementsContext extends ParserRuleContext {
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public StatementsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statements; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterStatements(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitStatements(this);
		}
	}

	public final StatementsContext statements() throws RecognitionException {
		StatementsContext _localctx = new StatementsContext(_ctx, getState());
		enterRule(_localctx, 168, RULE_statements);
		int _la;
		try {
			int _alt;
			setState(1501);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,188,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1493); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(1492);
						statement();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1495); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,186,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(1498);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417665550785076L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1523430809782507647L) != 0) {
					{
					setState(1497);
					expression(0);
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1500);
				expression(0);
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
	public static class AsyncBlockExpressionContext extends ParserRuleContext {
		public TerminalNode KW_ASYNC() { return getToken(RustParser.KW_ASYNC, 0); }
		public BlockExpressionContext blockExpression() {
			return getRuleContext(BlockExpressionContext.class,0);
		}
		public TerminalNode KW_MOVE() { return getToken(RustParser.KW_MOVE, 0); }
		public AsyncBlockExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_asyncBlockExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterAsyncBlockExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitAsyncBlockExpression(this);
		}
	}

	public final AsyncBlockExpressionContext asyncBlockExpression() throws RecognitionException {
		AsyncBlockExpressionContext _localctx = new AsyncBlockExpressionContext(_ctx, getState());
		enterRule(_localctx, 170, RULE_asyncBlockExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1503);
			match(KW_ASYNC);
			setState(1505);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_MOVE) {
				{
				setState(1504);
				match(KW_MOVE);
				}
			}

			setState(1507);
			blockExpression();
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
	public static class UnsafeBlockExpressionContext extends ParserRuleContext {
		public TerminalNode KW_UNSAFE() { return getToken(RustParser.KW_UNSAFE, 0); }
		public BlockExpressionContext blockExpression() {
			return getRuleContext(BlockExpressionContext.class,0);
		}
		public UnsafeBlockExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unsafeBlockExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterUnsafeBlockExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitUnsafeBlockExpression(this);
		}
	}

	public final UnsafeBlockExpressionContext unsafeBlockExpression() throws RecognitionException {
		UnsafeBlockExpressionContext _localctx = new UnsafeBlockExpressionContext(_ctx, getState());
		enterRule(_localctx, 172, RULE_unsafeBlockExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1509);
			match(KW_UNSAFE);
			setState(1510);
			blockExpression();
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
	public static class ArrayElementsContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public TerminalNode SEMI() { return getToken(RustParser.SEMI, 0); }
		public ArrayElementsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayElements; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterArrayElements(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitArrayElements(this);
		}
	}

	public final ArrayElementsContext arrayElements() throws RecognitionException {
		ArrayElementsContext _localctx = new ArrayElementsContext(_ctx, getState());
		enterRule(_localctx, 174, RULE_arrayElements);
		int _la;
		try {
			int _alt;
			setState(1527);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,192,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1512);
				expression(0);
				setState(1517);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,190,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1513);
						match(COMMA);
						setState(1514);
						expression(0);
						}
						} 
					}
					setState(1519);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,190,_ctx);
				}
				setState(1521);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(1520);
					match(COMMA);
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1523);
				expression(0);
				setState(1524);
				match(SEMI);
				setState(1525);
				expression(0);
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
	public static class TupleElementsContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public TupleElementsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tupleElements; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTupleElements(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTupleElements(this);
		}
	}

	public final TupleElementsContext tupleElements() throws RecognitionException {
		TupleElementsContext _localctx = new TupleElementsContext(_ctx, getState());
		enterRule(_localctx, 176, RULE_tupleElements);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1532); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(1529);
					expression(0);
					setState(1530);
					match(COMMA);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(1534); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,193,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(1537);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417665550785076L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1523430809782507647L) != 0) {
				{
				setState(1536);
				expression(0);
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
	public static class TupleIndexContext extends ParserRuleContext {
		public TerminalNode INTEGER_LITERAL() { return getToken(RustParser.INTEGER_LITERAL, 0); }
		public TupleIndexContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tupleIndex; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTupleIndex(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTupleIndex(this);
		}
	}

	public final TupleIndexContext tupleIndex() throws RecognitionException {
		TupleIndexContext _localctx = new TupleIndexContext(_ctx, getState());
		enterRule(_localctx, 178, RULE_tupleIndex);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1539);
			match(INTEGER_LITERAL);
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
	public static class StructExpressionContext extends ParserRuleContext {
		public StructExprStructContext structExprStruct() {
			return getRuleContext(StructExprStructContext.class,0);
		}
		public StructExprTupleContext structExprTuple() {
			return getRuleContext(StructExprTupleContext.class,0);
		}
		public StructExprUnitContext structExprUnit() {
			return getRuleContext(StructExprUnitContext.class,0);
		}
		public StructExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterStructExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitStructExpression(this);
		}
	}

	public final StructExpressionContext structExpression() throws RecognitionException {
		StructExpressionContext _localctx = new StructExpressionContext(_ctx, getState());
		enterRule(_localctx, 180, RULE_structExpression);
		try {
			setState(1544);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,195,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1541);
				structExprStruct();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1542);
				structExprTuple();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1543);
				structExprUnit();
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
	public static class StructExprStructContext extends ParserRuleContext {
		public PathInExpressionContext pathInExpression() {
			return getRuleContext(PathInExpressionContext.class,0);
		}
		public TerminalNode LCURLYBRACE() { return getToken(RustParser.LCURLYBRACE, 0); }
		public TerminalNode RCURLYBRACE() { return getToken(RustParser.RCURLYBRACE, 0); }
		public List<InnerAttributeContext> innerAttribute() {
			return getRuleContexts(InnerAttributeContext.class);
		}
		public InnerAttributeContext innerAttribute(int i) {
			return getRuleContext(InnerAttributeContext.class,i);
		}
		public StructExprFieldsContext structExprFields() {
			return getRuleContext(StructExprFieldsContext.class,0);
		}
		public StructBaseContext structBase() {
			return getRuleContext(StructBaseContext.class,0);
		}
		public StructExprStructContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structExprStruct; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterStructExprStruct(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitStructExprStruct(this);
		}
	}

	public final StructExprStructContext structExprStruct() throws RecognitionException {
		StructExprStructContext _localctx = new StructExprStructContext(_ctx, getState());
		enterRule(_localctx, 182, RULE_structExprStruct);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1546);
			pathInExpression();
			setState(1547);
			match(LCURLYBRACE);
			setState(1551);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,196,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1548);
					innerAttribute();
					}
					} 
				}
				setState(1553);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,196,_ctx);
			}
			setState(1556);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_MACRORULES:
			case NON_KEYWORD_IDENTIFIER:
			case RAW_IDENTIFIER:
			case INTEGER_LITERAL:
			case POUND:
				{
				setState(1554);
				structExprFields();
				}
				break;
			case DOTDOT:
				{
				setState(1555);
				structBase();
				}
				break;
			case RCURLYBRACE:
				break;
			default:
				break;
			}
			setState(1558);
			match(RCURLYBRACE);
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
	public static class StructExprFieldsContext extends ParserRuleContext {
		public List<StructExprFieldContext> structExprField() {
			return getRuleContexts(StructExprFieldContext.class);
		}
		public StructExprFieldContext structExprField(int i) {
			return getRuleContext(StructExprFieldContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public StructBaseContext structBase() {
			return getRuleContext(StructBaseContext.class,0);
		}
		public StructExprFieldsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structExprFields; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterStructExprFields(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitStructExprFields(this);
		}
	}

	public final StructExprFieldsContext structExprFields() throws RecognitionException {
		StructExprFieldsContext _localctx = new StructExprFieldsContext(_ctx, getState());
		enterRule(_localctx, 184, RULE_structExprFields);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1560);
			structExprField();
			setState(1565);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,198,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1561);
					match(COMMA);
					setState(1562);
					structExprField();
					}
					} 
				}
				setState(1567);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,198,_ctx);
			}
			setState(1573);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,200,_ctx) ) {
			case 1:
				{
				setState(1568);
				match(COMMA);
				setState(1569);
				structBase();
				}
				break;
			case 2:
				{
				setState(1571);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(1570);
					match(COMMA);
					}
				}

				}
				break;
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
	public static class StructExprFieldContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode COLON() { return getToken(RustParser.COLON, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<OuterAttributeContext> outerAttribute() {
			return getRuleContexts(OuterAttributeContext.class);
		}
		public OuterAttributeContext outerAttribute(int i) {
			return getRuleContext(OuterAttributeContext.class,i);
		}
		public TupleIndexContext tupleIndex() {
			return getRuleContext(TupleIndexContext.class,0);
		}
		public StructExprFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structExprField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterStructExprField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitStructExprField(this);
		}
	}

	public final StructExprFieldContext structExprField() throws RecognitionException {
		StructExprFieldContext _localctx = new StructExprFieldContext(_ctx, getState());
		enterRule(_localctx, 186, RULE_structExprField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1578);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(1575);
				outerAttribute();
				}
				}
				setState(1580);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1589);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,203,_ctx) ) {
			case 1:
				{
				setState(1581);
				identifier();
				}
				break;
			case 2:
				{
				setState(1584);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case KW_MACRORULES:
				case NON_KEYWORD_IDENTIFIER:
				case RAW_IDENTIFIER:
					{
					setState(1582);
					identifier();
					}
					break;
				case INTEGER_LITERAL:
					{
					setState(1583);
					tupleIndex();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(1586);
				match(COLON);
				setState(1587);
				expression(0);
				}
				break;
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
	public static class StructBaseContext extends ParserRuleContext {
		public TerminalNode DOTDOT() { return getToken(RustParser.DOTDOT, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public StructBaseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structBase; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterStructBase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitStructBase(this);
		}
	}

	public final StructBaseContext structBase() throws RecognitionException {
		StructBaseContext _localctx = new StructBaseContext(_ctx, getState());
		enterRule(_localctx, 188, RULE_structBase);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1591);
			match(DOTDOT);
			setState(1592);
			expression(0);
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
	public static class StructExprTupleContext extends ParserRuleContext {
		public PathInExpressionContext pathInExpression() {
			return getRuleContext(PathInExpressionContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(RustParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(RustParser.RPAREN, 0); }
		public List<InnerAttributeContext> innerAttribute() {
			return getRuleContexts(InnerAttributeContext.class);
		}
		public InnerAttributeContext innerAttribute(int i) {
			return getRuleContext(InnerAttributeContext.class,i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public StructExprTupleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structExprTuple; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterStructExprTuple(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitStructExprTuple(this);
		}
	}

	public final StructExprTupleContext structExprTuple() throws RecognitionException {
		StructExprTupleContext _localctx = new StructExprTupleContext(_ctx, getState());
		enterRule(_localctx, 190, RULE_structExprTuple);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1594);
			pathInExpression();
			setState(1595);
			match(LPAREN);
			setState(1599);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,204,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1596);
					innerAttribute();
					}
					} 
				}
				setState(1601);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,204,_ctx);
			}
			setState(1613);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417665550785076L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1523430809782507647L) != 0) {
				{
				setState(1602);
				expression(0);
				setState(1607);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,205,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1603);
						match(COMMA);
						setState(1604);
						expression(0);
						}
						} 
					}
					setState(1609);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,205,_ctx);
				}
				setState(1611);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(1610);
					match(COMMA);
					}
				}

				}
			}

			setState(1615);
			match(RPAREN);
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
	public static class StructExprUnitContext extends ParserRuleContext {
		public PathInExpressionContext pathInExpression() {
			return getRuleContext(PathInExpressionContext.class,0);
		}
		public StructExprUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structExprUnit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterStructExprUnit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitStructExprUnit(this);
		}
	}

	public final StructExprUnitContext structExprUnit() throws RecognitionException {
		StructExprUnitContext _localctx = new StructExprUnitContext(_ctx, getState());
		enterRule(_localctx, 192, RULE_structExprUnit);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1617);
			pathInExpression();
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
	public static class EnumerationVariantExpressionContext extends ParserRuleContext {
		public EnumExprStructContext enumExprStruct() {
			return getRuleContext(EnumExprStructContext.class,0);
		}
		public EnumExprTupleContext enumExprTuple() {
			return getRuleContext(EnumExprTupleContext.class,0);
		}
		public EnumExprFieldlessContext enumExprFieldless() {
			return getRuleContext(EnumExprFieldlessContext.class,0);
		}
		public EnumerationVariantExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumerationVariantExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterEnumerationVariantExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitEnumerationVariantExpression(this);
		}
	}

	public final EnumerationVariantExpressionContext enumerationVariantExpression() throws RecognitionException {
		EnumerationVariantExpressionContext _localctx = new EnumerationVariantExpressionContext(_ctx, getState());
		enterRule(_localctx, 194, RULE_enumerationVariantExpression);
		try {
			setState(1622);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,208,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1619);
				enumExprStruct();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1620);
				enumExprTuple();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1621);
				enumExprFieldless();
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
	public static class EnumExprStructContext extends ParserRuleContext {
		public PathInExpressionContext pathInExpression() {
			return getRuleContext(PathInExpressionContext.class,0);
		}
		public TerminalNode LCURLYBRACE() { return getToken(RustParser.LCURLYBRACE, 0); }
		public TerminalNode RCURLYBRACE() { return getToken(RustParser.RCURLYBRACE, 0); }
		public EnumExprFieldsContext enumExprFields() {
			return getRuleContext(EnumExprFieldsContext.class,0);
		}
		public EnumExprStructContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumExprStruct; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterEnumExprStruct(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitEnumExprStruct(this);
		}
	}

	public final EnumExprStructContext enumExprStruct() throws RecognitionException {
		EnumExprStructContext _localctx = new EnumExprStructContext(_ctx, getState());
		enterRule(_localctx, 196, RULE_enumExprStruct);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1624);
			pathInExpression();
			setState(1625);
			match(LCURLYBRACE);
			setState(1627);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la - 54)) & ~0x3f) == 0 && ((1L << (_la - 54)) & 524313L) != 0) {
				{
				setState(1626);
				enumExprFields();
				}
			}

			setState(1629);
			match(RCURLYBRACE);
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
	public static class EnumExprFieldsContext extends ParserRuleContext {
		public List<EnumExprFieldContext> enumExprField() {
			return getRuleContexts(EnumExprFieldContext.class);
		}
		public EnumExprFieldContext enumExprField(int i) {
			return getRuleContext(EnumExprFieldContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public EnumExprFieldsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumExprFields; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterEnumExprFields(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitEnumExprFields(this);
		}
	}

	public final EnumExprFieldsContext enumExprFields() throws RecognitionException {
		EnumExprFieldsContext _localctx = new EnumExprFieldsContext(_ctx, getState());
		enterRule(_localctx, 198, RULE_enumExprFields);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1631);
			enumExprField();
			setState(1636);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,210,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1632);
					match(COMMA);
					setState(1633);
					enumExprField();
					}
					} 
				}
				setState(1638);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,210,_ctx);
			}
			setState(1640);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(1639);
				match(COMMA);
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
	public static class EnumExprFieldContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode COLON() { return getToken(RustParser.COLON, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TupleIndexContext tupleIndex() {
			return getRuleContext(TupleIndexContext.class,0);
		}
		public EnumExprFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumExprField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterEnumExprField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitEnumExprField(this);
		}
	}

	public final EnumExprFieldContext enumExprField() throws RecognitionException {
		EnumExprFieldContext _localctx = new EnumExprFieldContext(_ctx, getState());
		enterRule(_localctx, 200, RULE_enumExprField);
		try {
			setState(1650);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,213,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1642);
				identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1645);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case KW_MACRORULES:
				case NON_KEYWORD_IDENTIFIER:
				case RAW_IDENTIFIER:
					{
					setState(1643);
					identifier();
					}
					break;
				case INTEGER_LITERAL:
					{
					setState(1644);
					tupleIndex();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(1647);
				match(COLON);
				setState(1648);
				expression(0);
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
	public static class EnumExprTupleContext extends ParserRuleContext {
		public PathInExpressionContext pathInExpression() {
			return getRuleContext(PathInExpressionContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(RustParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(RustParser.RPAREN, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public EnumExprTupleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumExprTuple; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterEnumExprTuple(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitEnumExprTuple(this);
		}
	}

	public final EnumExprTupleContext enumExprTuple() throws RecognitionException {
		EnumExprTupleContext _localctx = new EnumExprTupleContext(_ctx, getState());
		enterRule(_localctx, 202, RULE_enumExprTuple);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1652);
			pathInExpression();
			setState(1653);
			match(LPAREN);
			setState(1665);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417665550785076L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1523430809782507647L) != 0) {
				{
				setState(1654);
				expression(0);
				setState(1659);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,214,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1655);
						match(COMMA);
						setState(1656);
						expression(0);
						}
						} 
					}
					setState(1661);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,214,_ctx);
				}
				setState(1663);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(1662);
					match(COMMA);
					}
				}

				}
			}

			setState(1667);
			match(RPAREN);
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
	public static class EnumExprFieldlessContext extends ParserRuleContext {
		public PathInExpressionContext pathInExpression() {
			return getRuleContext(PathInExpressionContext.class,0);
		}
		public EnumExprFieldlessContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumExprFieldless; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterEnumExprFieldless(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitEnumExprFieldless(this);
		}
	}

	public final EnumExprFieldlessContext enumExprFieldless() throws RecognitionException {
		EnumExprFieldlessContext _localctx = new EnumExprFieldlessContext(_ctx, getState());
		enterRule(_localctx, 204, RULE_enumExprFieldless);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1669);
			pathInExpression();
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
	public static class CallParamsContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public CallParamsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_callParams; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterCallParams(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitCallParams(this);
		}
	}

	public final CallParamsContext callParams() throws RecognitionException {
		CallParamsContext _localctx = new CallParamsContext(_ctx, getState());
		enterRule(_localctx, 206, RULE_callParams);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1671);
			expression(0);
			setState(1676);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,217,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1672);
					match(COMMA);
					setState(1673);
					expression(0);
					}
					} 
				}
				setState(1678);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,217,_ctx);
			}
			setState(1680);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(1679);
				match(COMMA);
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
	public static class ClosureExpressionContext extends ParserRuleContext {
		public TerminalNode OROR() { return getToken(RustParser.OROR, 0); }
		public List<TerminalNode> OR() { return getTokens(RustParser.OR); }
		public TerminalNode OR(int i) {
			return getToken(RustParser.OR, i);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RARROW() { return getToken(RustParser.RARROW, 0); }
		public TypeNoBoundsContext typeNoBounds() {
			return getRuleContext(TypeNoBoundsContext.class,0);
		}
		public BlockExpressionContext blockExpression() {
			return getRuleContext(BlockExpressionContext.class,0);
		}
		public TerminalNode KW_MOVE() { return getToken(RustParser.KW_MOVE, 0); }
		public ClosureParametersContext closureParameters() {
			return getRuleContext(ClosureParametersContext.class,0);
		}
		public ClosureExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_closureExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterClosureExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitClosureExpression(this);
		}
	}

	public final ClosureExpressionContext closureExpression() throws RecognitionException {
		ClosureExpressionContext _localctx = new ClosureExpressionContext(_ctx, getState());
		enterRule(_localctx, 208, RULE_closureExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1683);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_MOVE) {
				{
				setState(1682);
				match(KW_MOVE);
				}
			}

			setState(1691);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case OROR:
				{
				setState(1685);
				match(OROR);
				}
				break;
			case OR:
				{
				setState(1686);
				match(OR);
				setState(1688);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,220,_ctx) ) {
				case 1:
					{
					setState(1687);
					closureParameters();
					}
					break;
				}
				setState(1690);
				match(OR);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(1698);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_BREAK:
			case KW_CONTINUE:
			case KW_CRATE:
			case KW_FALSE:
			case KW_FOR:
			case KW_IF:
			case KW_LOOP:
			case KW_MATCH:
			case KW_MOVE:
			case KW_RETURN:
			case KW_SELFVALUE:
			case KW_SELFTYPE:
			case KW_SUPER:
			case KW_TRUE:
			case KW_UNSAFE:
			case KW_WHILE:
			case KW_ASYNC:
			case KW_MACRORULES:
			case KW_DOLLARCRATE:
			case NON_KEYWORD_IDENTIFIER:
			case RAW_IDENTIFIER:
			case CHAR_LITERAL:
			case STRING_LITERAL:
			case RAW_STRING_LITERAL:
			case BYTE_LITERAL:
			case BYTE_STRING_LITERAL:
			case RAW_BYTE_STRING_LITERAL:
			case INTEGER_LITERAL:
			case FLOAT_LITERAL:
			case LIFETIME_OR_LABEL:
			case MINUS:
			case STAR:
			case NOT:
			case AND:
			case OR:
			case ANDAND:
			case OROR:
			case LT:
			case DOTDOT:
			case DOTDOTEQ:
			case PATHSEP:
			case POUND:
			case LCURLYBRACE:
			case LSQUAREBRACKET:
			case LPAREN:
				{
				setState(1693);
				expression(0);
				}
				break;
			case RARROW:
				{
				setState(1694);
				match(RARROW);
				setState(1695);
				typeNoBounds();
				setState(1696);
				blockExpression();
				}
				break;
			default:
				throw new NoViableAltException(this);
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
	public static class ClosureParametersContext extends ParserRuleContext {
		public List<ClosureParamContext> closureParam() {
			return getRuleContexts(ClosureParamContext.class);
		}
		public ClosureParamContext closureParam(int i) {
			return getRuleContext(ClosureParamContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public ClosureParametersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_closureParameters; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterClosureParameters(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitClosureParameters(this);
		}
	}

	public final ClosureParametersContext closureParameters() throws RecognitionException {
		ClosureParametersContext _localctx = new ClosureParametersContext(_ctx, getState());
		enterRule(_localctx, 210, RULE_closureParameters);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1700);
			closureParam();
			setState(1705);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,223,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1701);
					match(COMMA);
					setState(1702);
					closureParam();
					}
					} 
				}
				setState(1707);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,223,_ctx);
			}
			setState(1709);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(1708);
				match(COMMA);
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
	public static class ClosureParamContext extends ParserRuleContext {
		public PatternContext pattern() {
			return getRuleContext(PatternContext.class,0);
		}
		public List<OuterAttributeContext> outerAttribute() {
			return getRuleContexts(OuterAttributeContext.class);
		}
		public OuterAttributeContext outerAttribute(int i) {
			return getRuleContext(OuterAttributeContext.class,i);
		}
		public TerminalNode COLON() { return getToken(RustParser.COLON, 0); }
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public ClosureParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_closureParam; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterClosureParam(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitClosureParam(this);
		}
	}

	public final ClosureParamContext closureParam() throws RecognitionException {
		ClosureParamContext _localctx = new ClosureParamContext(_ctx, getState());
		enterRule(_localctx, 212, RULE_closureParam);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1714);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(1711);
				outerAttribute();
				}
				}
				setState(1716);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1717);
			pattern();
			setState(1720);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(1718);
				match(COLON);
				setState(1719);
				type_();
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
	public static class LoopExpressionContext extends ParserRuleContext {
		public InfiniteLoopExpressionContext infiniteLoopExpression() {
			return getRuleContext(InfiniteLoopExpressionContext.class,0);
		}
		public PredicateLoopExpressionContext predicateLoopExpression() {
			return getRuleContext(PredicateLoopExpressionContext.class,0);
		}
		public PredicatePatternLoopExpressionContext predicatePatternLoopExpression() {
			return getRuleContext(PredicatePatternLoopExpressionContext.class,0);
		}
		public IteratorLoopExpressionContext iteratorLoopExpression() {
			return getRuleContext(IteratorLoopExpressionContext.class,0);
		}
		public LoopLabelContext loopLabel() {
			return getRuleContext(LoopLabelContext.class,0);
		}
		public LoopExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_loopExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterLoopExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitLoopExpression(this);
		}
	}

	public final LoopExpressionContext loopExpression() throws RecognitionException {
		LoopExpressionContext _localctx = new LoopExpressionContext(_ctx, getState());
		enterRule(_localctx, 214, RULE_loopExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1723);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LIFETIME_OR_LABEL) {
				{
				setState(1722);
				loopLabel();
				}
			}

			setState(1729);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,228,_ctx) ) {
			case 1:
				{
				setState(1725);
				infiniteLoopExpression();
				}
				break;
			case 2:
				{
				setState(1726);
				predicateLoopExpression();
				}
				break;
			case 3:
				{
				setState(1727);
				predicatePatternLoopExpression();
				}
				break;
			case 4:
				{
				setState(1728);
				iteratorLoopExpression();
				}
				break;
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
	public static class InfiniteLoopExpressionContext extends ParserRuleContext {
		public TerminalNode KW_LOOP() { return getToken(RustParser.KW_LOOP, 0); }
		public BlockExpressionContext blockExpression() {
			return getRuleContext(BlockExpressionContext.class,0);
		}
		public InfiniteLoopExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_infiniteLoopExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterInfiniteLoopExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitInfiniteLoopExpression(this);
		}
	}

	public final InfiniteLoopExpressionContext infiniteLoopExpression() throws RecognitionException {
		InfiniteLoopExpressionContext _localctx = new InfiniteLoopExpressionContext(_ctx, getState());
		enterRule(_localctx, 216, RULE_infiniteLoopExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1731);
			match(KW_LOOP);
			setState(1732);
			blockExpression();
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
	public static class PredicateLoopExpressionContext extends ParserRuleContext {
		public TerminalNode KW_WHILE() { return getToken(RustParser.KW_WHILE, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public BlockExpressionContext blockExpression() {
			return getRuleContext(BlockExpressionContext.class,0);
		}
		public PredicateLoopExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicateLoopExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterPredicateLoopExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitPredicateLoopExpression(this);
		}
	}

	public final PredicateLoopExpressionContext predicateLoopExpression() throws RecognitionException {
		PredicateLoopExpressionContext _localctx = new PredicateLoopExpressionContext(_ctx, getState());
		enterRule(_localctx, 218, RULE_predicateLoopExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1734);
			match(KW_WHILE);
			setState(1735);
			expression(0);
			setState(1736);
			blockExpression();
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
	public static class PredicatePatternLoopExpressionContext extends ParserRuleContext {
		public TerminalNode KW_WHILE() { return getToken(RustParser.KW_WHILE, 0); }
		public TerminalNode KW_LET() { return getToken(RustParser.KW_LET, 0); }
		public PatternContext pattern() {
			return getRuleContext(PatternContext.class,0);
		}
		public TerminalNode EQ() { return getToken(RustParser.EQ, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public BlockExpressionContext blockExpression() {
			return getRuleContext(BlockExpressionContext.class,0);
		}
		public PredicatePatternLoopExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicatePatternLoopExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterPredicatePatternLoopExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitPredicatePatternLoopExpression(this);
		}
	}

	public final PredicatePatternLoopExpressionContext predicatePatternLoopExpression() throws RecognitionException {
		PredicatePatternLoopExpressionContext _localctx = new PredicatePatternLoopExpressionContext(_ctx, getState());
		enterRule(_localctx, 220, RULE_predicatePatternLoopExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1738);
			match(KW_WHILE);
			setState(1739);
			match(KW_LET);
			setState(1740);
			pattern();
			setState(1741);
			match(EQ);
			setState(1742);
			expression(0);
			setState(1743);
			blockExpression();
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
	public static class IteratorLoopExpressionContext extends ParserRuleContext {
		public TerminalNode KW_FOR() { return getToken(RustParser.KW_FOR, 0); }
		public PatternContext pattern() {
			return getRuleContext(PatternContext.class,0);
		}
		public TerminalNode KW_IN() { return getToken(RustParser.KW_IN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public BlockExpressionContext blockExpression() {
			return getRuleContext(BlockExpressionContext.class,0);
		}
		public IteratorLoopExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_iteratorLoopExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterIteratorLoopExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitIteratorLoopExpression(this);
		}
	}

	public final IteratorLoopExpressionContext iteratorLoopExpression() throws RecognitionException {
		IteratorLoopExpressionContext _localctx = new IteratorLoopExpressionContext(_ctx, getState());
		enterRule(_localctx, 222, RULE_iteratorLoopExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1745);
			match(KW_FOR);
			setState(1746);
			pattern();
			setState(1747);
			match(KW_IN);
			setState(1748);
			expression(0);
			setState(1749);
			blockExpression();
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
	public static class LoopLabelContext extends ParserRuleContext {
		public TerminalNode LIFETIME_OR_LABEL() { return getToken(RustParser.LIFETIME_OR_LABEL, 0); }
		public TerminalNode COLON() { return getToken(RustParser.COLON, 0); }
		public LoopLabelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_loopLabel; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterLoopLabel(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitLoopLabel(this);
		}
	}

	public final LoopLabelContext loopLabel() throws RecognitionException {
		LoopLabelContext _localctx = new LoopLabelContext(_ctx, getState());
		enterRule(_localctx, 224, RULE_loopLabel);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1751);
			match(LIFETIME_OR_LABEL);
			setState(1752);
			match(COLON);
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
	public static class IfExpressionContext extends ParserRuleContext {
		public TerminalNode KW_IF() { return getToken(RustParser.KW_IF, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<BlockExpressionContext> blockExpression() {
			return getRuleContexts(BlockExpressionContext.class);
		}
		public BlockExpressionContext blockExpression(int i) {
			return getRuleContext(BlockExpressionContext.class,i);
		}
		public TerminalNode KW_ELSE() { return getToken(RustParser.KW_ELSE, 0); }
		public IfExpressionContext ifExpression() {
			return getRuleContext(IfExpressionContext.class,0);
		}
		public IfLetExpressionContext ifLetExpression() {
			return getRuleContext(IfLetExpressionContext.class,0);
		}
		public IfExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ifExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterIfExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitIfExpression(this);
		}
	}

	public final IfExpressionContext ifExpression() throws RecognitionException {
		IfExpressionContext _localctx = new IfExpressionContext(_ctx, getState());
		enterRule(_localctx, 226, RULE_ifExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1754);
			match(KW_IF);
			setState(1755);
			expression(0);
			setState(1756);
			blockExpression();
			setState(1763);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,230,_ctx) ) {
			case 1:
				{
				setState(1757);
				match(KW_ELSE);
				setState(1761);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,229,_ctx) ) {
				case 1:
					{
					setState(1758);
					blockExpression();
					}
					break;
				case 2:
					{
					setState(1759);
					ifExpression();
					}
					break;
				case 3:
					{
					setState(1760);
					ifLetExpression();
					}
					break;
				}
				}
				break;
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
	public static class IfLetExpressionContext extends ParserRuleContext {
		public TerminalNode KW_IF() { return getToken(RustParser.KW_IF, 0); }
		public TerminalNode KW_LET() { return getToken(RustParser.KW_LET, 0); }
		public PatternContext pattern() {
			return getRuleContext(PatternContext.class,0);
		}
		public TerminalNode EQ() { return getToken(RustParser.EQ, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<BlockExpressionContext> blockExpression() {
			return getRuleContexts(BlockExpressionContext.class);
		}
		public BlockExpressionContext blockExpression(int i) {
			return getRuleContext(BlockExpressionContext.class,i);
		}
		public TerminalNode KW_ELSE() { return getToken(RustParser.KW_ELSE, 0); }
		public IfExpressionContext ifExpression() {
			return getRuleContext(IfExpressionContext.class,0);
		}
		public IfLetExpressionContext ifLetExpression() {
			return getRuleContext(IfLetExpressionContext.class,0);
		}
		public IfLetExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ifLetExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterIfLetExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitIfLetExpression(this);
		}
	}

	public final IfLetExpressionContext ifLetExpression() throws RecognitionException {
		IfLetExpressionContext _localctx = new IfLetExpressionContext(_ctx, getState());
		enterRule(_localctx, 228, RULE_ifLetExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1765);
			match(KW_IF);
			setState(1766);
			match(KW_LET);
			setState(1767);
			pattern();
			setState(1768);
			match(EQ);
			setState(1769);
			expression(0);
			setState(1770);
			blockExpression();
			setState(1777);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,232,_ctx) ) {
			case 1:
				{
				setState(1771);
				match(KW_ELSE);
				setState(1775);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,231,_ctx) ) {
				case 1:
					{
					setState(1772);
					blockExpression();
					}
					break;
				case 2:
					{
					setState(1773);
					ifExpression();
					}
					break;
				case 3:
					{
					setState(1774);
					ifLetExpression();
					}
					break;
				}
				}
				break;
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
	public static class MatchExpressionContext extends ParserRuleContext {
		public TerminalNode KW_MATCH() { return getToken(RustParser.KW_MATCH, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode LCURLYBRACE() { return getToken(RustParser.LCURLYBRACE, 0); }
		public TerminalNode RCURLYBRACE() { return getToken(RustParser.RCURLYBRACE, 0); }
		public List<InnerAttributeContext> innerAttribute() {
			return getRuleContexts(InnerAttributeContext.class);
		}
		public InnerAttributeContext innerAttribute(int i) {
			return getRuleContext(InnerAttributeContext.class,i);
		}
		public MatchArmsContext matchArms() {
			return getRuleContext(MatchArmsContext.class,0);
		}
		public MatchExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_matchExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMatchExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMatchExpression(this);
		}
	}

	public final MatchExpressionContext matchExpression() throws RecognitionException {
		MatchExpressionContext _localctx = new MatchExpressionContext(_ctx, getState());
		enterRule(_localctx, 230, RULE_matchExpression);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1779);
			match(KW_MATCH);
			setState(1780);
			expression(0);
			setState(1781);
			match(LCURLYBRACE);
			setState(1785);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,233,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1782);
					innerAttribute();
					}
					} 
				}
				setState(1787);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,233,_ctx);
			}
			setState(1789);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417558172729888L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1451307245037963391L) != 0) {
				{
				setState(1788);
				matchArms();
				}
			}

			setState(1791);
			match(RCURLYBRACE);
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
	public static class MatchArmsContext extends ParserRuleContext {
		public List<MatchArmContext> matchArm() {
			return getRuleContexts(MatchArmContext.class);
		}
		public MatchArmContext matchArm(int i) {
			return getRuleContext(MatchArmContext.class,i);
		}
		public List<TerminalNode> FATARROW() { return getTokens(RustParser.FATARROW); }
		public TerminalNode FATARROW(int i) {
			return getToken(RustParser.FATARROW, i);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<MatchArmExpressionContext> matchArmExpression() {
			return getRuleContexts(MatchArmExpressionContext.class);
		}
		public MatchArmExpressionContext matchArmExpression(int i) {
			return getRuleContext(MatchArmExpressionContext.class,i);
		}
		public TerminalNode COMMA() { return getToken(RustParser.COMMA, 0); }
		public MatchArmsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_matchArms; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMatchArms(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMatchArms(this);
		}
	}

	public final MatchArmsContext matchArms() throws RecognitionException {
		MatchArmsContext _localctx = new MatchArmsContext(_ctx, getState());
		enterRule(_localctx, 232, RULE_matchArms);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1799);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,235,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1793);
					matchArm();
					setState(1794);
					match(FATARROW);
					setState(1795);
					matchArmExpression();
					}
					} 
				}
				setState(1801);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,235,_ctx);
			}
			setState(1802);
			matchArm();
			setState(1803);
			match(FATARROW);
			setState(1804);
			expression(0);
			setState(1806);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(1805);
				match(COMMA);
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
	public static class MatchArmExpressionContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(RustParser.COMMA, 0); }
		public ExpressionWithBlockContext expressionWithBlock() {
			return getRuleContext(ExpressionWithBlockContext.class,0);
		}
		public MatchArmExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_matchArmExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMatchArmExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMatchArmExpression(this);
		}
	}

	public final MatchArmExpressionContext matchArmExpression() throws RecognitionException {
		MatchArmExpressionContext _localctx = new MatchArmExpressionContext(_ctx, getState());
		enterRule(_localctx, 234, RULE_matchArmExpression);
		int _la;
		try {
			setState(1815);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,238,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1808);
				expression(0);
				setState(1809);
				match(COMMA);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1811);
				expressionWithBlock();
				setState(1813);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(1812);
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
	public static class MatchArmContext extends ParserRuleContext {
		public PatternContext pattern() {
			return getRuleContext(PatternContext.class,0);
		}
		public List<OuterAttributeContext> outerAttribute() {
			return getRuleContexts(OuterAttributeContext.class);
		}
		public OuterAttributeContext outerAttribute(int i) {
			return getRuleContext(OuterAttributeContext.class,i);
		}
		public MatchArmGuardContext matchArmGuard() {
			return getRuleContext(MatchArmGuardContext.class,0);
		}
		public MatchArmContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_matchArm; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMatchArm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMatchArm(this);
		}
	}

	public final MatchArmContext matchArm() throws RecognitionException {
		MatchArmContext _localctx = new MatchArmContext(_ctx, getState());
		enterRule(_localctx, 236, RULE_matchArm);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1820);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(1817);
				outerAttribute();
				}
				}
				setState(1822);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1823);
			pattern();
			setState(1825);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_IF) {
				{
				setState(1824);
				matchArmGuard();
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
	public static class MatchArmGuardContext extends ParserRuleContext {
		public TerminalNode KW_IF() { return getToken(RustParser.KW_IF, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public MatchArmGuardContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_matchArmGuard; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMatchArmGuard(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMatchArmGuard(this);
		}
	}

	public final MatchArmGuardContext matchArmGuard() throws RecognitionException {
		MatchArmGuardContext _localctx = new MatchArmGuardContext(_ctx, getState());
		enterRule(_localctx, 238, RULE_matchArmGuard);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1827);
			match(KW_IF);
			setState(1828);
			expression(0);
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
	public static class PatternContext extends ParserRuleContext {
		public List<PatternNoTopAltContext> patternNoTopAlt() {
			return getRuleContexts(PatternNoTopAltContext.class);
		}
		public PatternNoTopAltContext patternNoTopAlt(int i) {
			return getRuleContext(PatternNoTopAltContext.class,i);
		}
		public List<TerminalNode> OR() { return getTokens(RustParser.OR); }
		public TerminalNode OR(int i) {
			return getToken(RustParser.OR, i);
		}
		public PatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterPattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitPattern(this);
		}
	}

	public final PatternContext pattern() throws RecognitionException {
		PatternContext _localctx = new PatternContext(_ctx, getState());
		enterRule(_localctx, 240, RULE_pattern);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1831);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==OR) {
				{
				setState(1830);
				match(OR);
				}
			}

			setState(1833);
			patternNoTopAlt();
			setState(1838);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,242,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1834);
					match(OR);
					setState(1835);
					patternNoTopAlt();
					}
					} 
				}
				setState(1840);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,242,_ctx);
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
	public static class PatternNoTopAltContext extends ParserRuleContext {
		public PatternWithoutRangeContext patternWithoutRange() {
			return getRuleContext(PatternWithoutRangeContext.class,0);
		}
		public RangePatternContext rangePattern() {
			return getRuleContext(RangePatternContext.class,0);
		}
		public PatternNoTopAltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_patternNoTopAlt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterPatternNoTopAlt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitPatternNoTopAlt(this);
		}
	}

	public final PatternNoTopAltContext patternNoTopAlt() throws RecognitionException {
		PatternNoTopAltContext _localctx = new PatternNoTopAltContext(_ctx, getState());
		enterRule(_localctx, 242, RULE_patternNoTopAlt);
		try {
			setState(1843);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,243,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1841);
				patternWithoutRange();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1842);
				rangePattern();
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
	public static class PatternWithoutRangeContext extends ParserRuleContext {
		public LiteralPatternContext literalPattern() {
			return getRuleContext(LiteralPatternContext.class,0);
		}
		public IdentifierPatternContext identifierPattern() {
			return getRuleContext(IdentifierPatternContext.class,0);
		}
		public WildcardPatternContext wildcardPattern() {
			return getRuleContext(WildcardPatternContext.class,0);
		}
		public RestPatternContext restPattern() {
			return getRuleContext(RestPatternContext.class,0);
		}
		public ReferencePatternContext referencePattern() {
			return getRuleContext(ReferencePatternContext.class,0);
		}
		public StructPatternContext structPattern() {
			return getRuleContext(StructPatternContext.class,0);
		}
		public TupleStructPatternContext tupleStructPattern() {
			return getRuleContext(TupleStructPatternContext.class,0);
		}
		public TuplePatternContext tuplePattern() {
			return getRuleContext(TuplePatternContext.class,0);
		}
		public GroupedPatternContext groupedPattern() {
			return getRuleContext(GroupedPatternContext.class,0);
		}
		public SlicePatternContext slicePattern() {
			return getRuleContext(SlicePatternContext.class,0);
		}
		public PathPatternContext pathPattern() {
			return getRuleContext(PathPatternContext.class,0);
		}
		public MacroInvocationContext macroInvocation() {
			return getRuleContext(MacroInvocationContext.class,0);
		}
		public PatternWithoutRangeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_patternWithoutRange; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterPatternWithoutRange(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitPatternWithoutRange(this);
		}
	}

	public final PatternWithoutRangeContext patternWithoutRange() throws RecognitionException {
		PatternWithoutRangeContext _localctx = new PatternWithoutRangeContext(_ctx, getState());
		enterRule(_localctx, 244, RULE_patternWithoutRange);
		try {
			setState(1857);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,244,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1845);
				literalPattern();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1846);
				identifierPattern();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1847);
				wildcardPattern();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1848);
				restPattern();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1849);
				referencePattern();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1850);
				structPattern();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1851);
				tupleStructPattern();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(1852);
				tuplePattern();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(1853);
				groupedPattern();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(1854);
				slicePattern();
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(1855);
				pathPattern();
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(1856);
				macroInvocation();
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
	public static class LiteralPatternContext extends ParserRuleContext {
		public TerminalNode KW_TRUE() { return getToken(RustParser.KW_TRUE, 0); }
		public TerminalNode KW_FALSE() { return getToken(RustParser.KW_FALSE, 0); }
		public TerminalNode CHAR_LITERAL() { return getToken(RustParser.CHAR_LITERAL, 0); }
		public TerminalNode BYTE_LITERAL() { return getToken(RustParser.BYTE_LITERAL, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(RustParser.STRING_LITERAL, 0); }
		public TerminalNode RAW_STRING_LITERAL() { return getToken(RustParser.RAW_STRING_LITERAL, 0); }
		public TerminalNode BYTE_STRING_LITERAL() { return getToken(RustParser.BYTE_STRING_LITERAL, 0); }
		public TerminalNode RAW_BYTE_STRING_LITERAL() { return getToken(RustParser.RAW_BYTE_STRING_LITERAL, 0); }
		public TerminalNode INTEGER_LITERAL() { return getToken(RustParser.INTEGER_LITERAL, 0); }
		public TerminalNode MINUS() { return getToken(RustParser.MINUS, 0); }
		public TerminalNode FLOAT_LITERAL() { return getToken(RustParser.FLOAT_LITERAL, 0); }
		public LiteralPatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literalPattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterLiteralPattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitLiteralPattern(this);
		}
	}

	public final LiteralPatternContext literalPattern() throws RecognitionException {
		LiteralPatternContext _localctx = new LiteralPatternContext(_ctx, getState());
		enterRule(_localctx, 246, RULE_literalPattern);
		int _la;
		try {
			setState(1875);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,247,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1859);
				match(KW_TRUE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1860);
				match(KW_FALSE);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1861);
				match(CHAR_LITERAL);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1862);
				match(BYTE_LITERAL);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1863);
				match(STRING_LITERAL);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1864);
				match(RAW_STRING_LITERAL);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1865);
				match(BYTE_STRING_LITERAL);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(1866);
				match(RAW_BYTE_STRING_LITERAL);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(1868);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(1867);
					match(MINUS);
					}
				}

				setState(1870);
				match(INTEGER_LITERAL);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(1872);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(1871);
					match(MINUS);
					}
				}

				setState(1874);
				match(FLOAT_LITERAL);
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
	public static class IdentifierPatternContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode KW_REF() { return getToken(RustParser.KW_REF, 0); }
		public TerminalNode KW_MUT() { return getToken(RustParser.KW_MUT, 0); }
		public TerminalNode AT() { return getToken(RustParser.AT, 0); }
		public PatternContext pattern() {
			return getRuleContext(PatternContext.class,0);
		}
		public IdentifierPatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifierPattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterIdentifierPattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitIdentifierPattern(this);
		}
	}

	public final IdentifierPatternContext identifierPattern() throws RecognitionException {
		IdentifierPatternContext _localctx = new IdentifierPatternContext(_ctx, getState());
		enterRule(_localctx, 248, RULE_identifierPattern);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1878);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_REF) {
				{
				setState(1877);
				match(KW_REF);
				}
			}

			setState(1881);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_MUT) {
				{
				setState(1880);
				match(KW_MUT);
				}
			}

			setState(1883);
			identifier();
			setState(1886);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AT) {
				{
				setState(1884);
				match(AT);
				setState(1885);
				pattern();
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
	public static class WildcardPatternContext extends ParserRuleContext {
		public TerminalNode UNDERSCORE() { return getToken(RustParser.UNDERSCORE, 0); }
		public WildcardPatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_wildcardPattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterWildcardPattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitWildcardPattern(this);
		}
	}

	public final WildcardPatternContext wildcardPattern() throws RecognitionException {
		WildcardPatternContext _localctx = new WildcardPatternContext(_ctx, getState());
		enterRule(_localctx, 250, RULE_wildcardPattern);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1888);
			match(UNDERSCORE);
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
	public static class RestPatternContext extends ParserRuleContext {
		public TerminalNode DOTDOT() { return getToken(RustParser.DOTDOT, 0); }
		public RestPatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_restPattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterRestPattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitRestPattern(this);
		}
	}

	public final RestPatternContext restPattern() throws RecognitionException {
		RestPatternContext _localctx = new RestPatternContext(_ctx, getState());
		enterRule(_localctx, 252, RULE_restPattern);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1890);
			match(DOTDOT);
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
	public static class RangePatternContext extends ParserRuleContext {
		public RangePatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rangePattern; }
	 
		public RangePatternContext() { }
		public void copyFrom(RangePatternContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class InclusiveRangePatternContext extends RangePatternContext {
		public List<RangePatternBoundContext> rangePatternBound() {
			return getRuleContexts(RangePatternBoundContext.class);
		}
		public RangePatternBoundContext rangePatternBound(int i) {
			return getRuleContext(RangePatternBoundContext.class,i);
		}
		public TerminalNode DOTDOTEQ() { return getToken(RustParser.DOTDOTEQ, 0); }
		public InclusiveRangePatternContext(RangePatternContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterInclusiveRangePattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitInclusiveRangePattern(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ObsoleteRangePatternContext extends RangePatternContext {
		public List<RangePatternBoundContext> rangePatternBound() {
			return getRuleContexts(RangePatternBoundContext.class);
		}
		public RangePatternBoundContext rangePatternBound(int i) {
			return getRuleContext(RangePatternBoundContext.class,i);
		}
		public TerminalNode DOTDOTDOT() { return getToken(RustParser.DOTDOTDOT, 0); }
		public ObsoleteRangePatternContext(RangePatternContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterObsoleteRangePattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitObsoleteRangePattern(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class HalfOpenRangePatternContext extends RangePatternContext {
		public RangePatternBoundContext rangePatternBound() {
			return getRuleContext(RangePatternBoundContext.class,0);
		}
		public TerminalNode DOTDOT() { return getToken(RustParser.DOTDOT, 0); }
		public HalfOpenRangePatternContext(RangePatternContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterHalfOpenRangePattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitHalfOpenRangePattern(this);
		}
	}

	public final RangePatternContext rangePattern() throws RecognitionException {
		RangePatternContext _localctx = new RangePatternContext(_ctx, getState());
		enterRule(_localctx, 254, RULE_rangePattern);
		try {
			setState(1903);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,251,_ctx) ) {
			case 1:
				_localctx = new InclusiveRangePatternContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1892);
				rangePatternBound();
				setState(1893);
				match(DOTDOTEQ);
				setState(1894);
				rangePatternBound();
				}
				break;
			case 2:
				_localctx = new HalfOpenRangePatternContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1896);
				rangePatternBound();
				setState(1897);
				match(DOTDOT);
				}
				break;
			case 3:
				_localctx = new ObsoleteRangePatternContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1899);
				rangePatternBound();
				setState(1900);
				match(DOTDOTDOT);
				setState(1901);
				rangePatternBound();
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
	public static class RangePatternBoundContext extends ParserRuleContext {
		public TerminalNode CHAR_LITERAL() { return getToken(RustParser.CHAR_LITERAL, 0); }
		public TerminalNode BYTE_LITERAL() { return getToken(RustParser.BYTE_LITERAL, 0); }
		public TerminalNode INTEGER_LITERAL() { return getToken(RustParser.INTEGER_LITERAL, 0); }
		public TerminalNode MINUS() { return getToken(RustParser.MINUS, 0); }
		public TerminalNode FLOAT_LITERAL() { return getToken(RustParser.FLOAT_LITERAL, 0); }
		public PathPatternContext pathPattern() {
			return getRuleContext(PathPatternContext.class,0);
		}
		public RangePatternBoundContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rangePatternBound; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterRangePatternBound(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitRangePatternBound(this);
		}
	}

	public final RangePatternBoundContext rangePatternBound() throws RecognitionException {
		RangePatternBoundContext _localctx = new RangePatternBoundContext(_ctx, getState());
		enterRule(_localctx, 256, RULE_rangePatternBound);
		int _la;
		try {
			setState(1916);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,254,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1905);
				match(CHAR_LITERAL);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1906);
				match(BYTE_LITERAL);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1908);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(1907);
					match(MINUS);
					}
				}

				setState(1910);
				match(INTEGER_LITERAL);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1912);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(1911);
					match(MINUS);
					}
				}

				setState(1914);
				match(FLOAT_LITERAL);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1915);
				pathPattern();
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
	public static class ReferencePatternContext extends ParserRuleContext {
		public PatternWithoutRangeContext patternWithoutRange() {
			return getRuleContext(PatternWithoutRangeContext.class,0);
		}
		public TerminalNode AND() { return getToken(RustParser.AND, 0); }
		public TerminalNode ANDAND() { return getToken(RustParser.ANDAND, 0); }
		public TerminalNode KW_MUT() { return getToken(RustParser.KW_MUT, 0); }
		public ReferencePatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_referencePattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterReferencePattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitReferencePattern(this);
		}
	}

	public final ReferencePatternContext referencePattern() throws RecognitionException {
		ReferencePatternContext _localctx = new ReferencePatternContext(_ctx, getState());
		enterRule(_localctx, 258, RULE_referencePattern);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1918);
			_la = _input.LA(1);
			if ( !(_la==AND || _la==ANDAND) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(1920);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,255,_ctx) ) {
			case 1:
				{
				setState(1919);
				match(KW_MUT);
				}
				break;
			}
			setState(1922);
			patternWithoutRange();
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
	public static class StructPatternContext extends ParserRuleContext {
		public PathInExpressionContext pathInExpression() {
			return getRuleContext(PathInExpressionContext.class,0);
		}
		public TerminalNode LCURLYBRACE() { return getToken(RustParser.LCURLYBRACE, 0); }
		public TerminalNode RCURLYBRACE() { return getToken(RustParser.RCURLYBRACE, 0); }
		public StructPatternElementsContext structPatternElements() {
			return getRuleContext(StructPatternElementsContext.class,0);
		}
		public StructPatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structPattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterStructPattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitStructPattern(this);
		}
	}

	public final StructPatternContext structPattern() throws RecognitionException {
		StructPatternContext _localctx = new StructPatternContext(_ctx, getState());
		enterRule(_localctx, 260, RULE_structPattern);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1924);
			pathInExpression();
			setState(1925);
			match(LCURLYBRACE);
			setState(1927);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 450359962742292480L) != 0 || (((_la - 73)) & ~0x3f) == 0 && ((1L << (_la - 73)) & 141012366262273L) != 0) {
				{
				setState(1926);
				structPatternElements();
				}
			}

			setState(1929);
			match(RCURLYBRACE);
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
	public static class StructPatternElementsContext extends ParserRuleContext {
		public StructPatternFieldsContext structPatternFields() {
			return getRuleContext(StructPatternFieldsContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(RustParser.COMMA, 0); }
		public StructPatternEtCeteraContext structPatternEtCetera() {
			return getRuleContext(StructPatternEtCeteraContext.class,0);
		}
		public StructPatternElementsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structPatternElements; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterStructPatternElements(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitStructPatternElements(this);
		}
	}

	public final StructPatternElementsContext structPatternElements() throws RecognitionException {
		StructPatternElementsContext _localctx = new StructPatternElementsContext(_ctx, getState());
		enterRule(_localctx, 262, RULE_structPatternElements);
		int _la;
		try {
			setState(1939);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,259,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1931);
				structPatternFields();
				setState(1936);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(1932);
					match(COMMA);
					setState(1934);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==DOTDOT || _la==POUND) {
						{
						setState(1933);
						structPatternEtCetera();
						}
					}

					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1938);
				structPatternEtCetera();
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
	public static class StructPatternFieldsContext extends ParserRuleContext {
		public List<StructPatternFieldContext> structPatternField() {
			return getRuleContexts(StructPatternFieldContext.class);
		}
		public StructPatternFieldContext structPatternField(int i) {
			return getRuleContext(StructPatternFieldContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public StructPatternFieldsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structPatternFields; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterStructPatternFields(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitStructPatternFields(this);
		}
	}

	public final StructPatternFieldsContext structPatternFields() throws RecognitionException {
		StructPatternFieldsContext _localctx = new StructPatternFieldsContext(_ctx, getState());
		enterRule(_localctx, 264, RULE_structPatternFields);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1941);
			structPatternField();
			setState(1946);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,260,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1942);
					match(COMMA);
					setState(1943);
					structPatternField();
					}
					} 
				}
				setState(1948);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,260,_ctx);
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
	public static class StructPatternFieldContext extends ParserRuleContext {
		public TupleIndexContext tupleIndex() {
			return getRuleContext(TupleIndexContext.class,0);
		}
		public TerminalNode COLON() { return getToken(RustParser.COLON, 0); }
		public PatternContext pattern() {
			return getRuleContext(PatternContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<OuterAttributeContext> outerAttribute() {
			return getRuleContexts(OuterAttributeContext.class);
		}
		public OuterAttributeContext outerAttribute(int i) {
			return getRuleContext(OuterAttributeContext.class,i);
		}
		public TerminalNode KW_REF() { return getToken(RustParser.KW_REF, 0); }
		public TerminalNode KW_MUT() { return getToken(RustParser.KW_MUT, 0); }
		public StructPatternFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structPatternField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterStructPatternField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitStructPatternField(this);
		}
	}

	public final StructPatternFieldContext structPatternField() throws RecognitionException {
		StructPatternFieldContext _localctx = new StructPatternFieldContext(_ctx, getState());
		enterRule(_localctx, 266, RULE_structPatternField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1952);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(1949);
				outerAttribute();
				}
				}
				setState(1954);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1970);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,264,_ctx) ) {
			case 1:
				{
				setState(1955);
				tupleIndex();
				setState(1956);
				match(COLON);
				setState(1957);
				pattern();
				}
				break;
			case 2:
				{
				setState(1959);
				identifier();
				setState(1960);
				match(COLON);
				setState(1961);
				pattern();
				}
				break;
			case 3:
				{
				setState(1964);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==KW_REF) {
					{
					setState(1963);
					match(KW_REF);
					}
				}

				setState(1967);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==KW_MUT) {
					{
					setState(1966);
					match(KW_MUT);
					}
				}

				setState(1969);
				identifier();
				}
				break;
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
	public static class StructPatternEtCeteraContext extends ParserRuleContext {
		public TerminalNode DOTDOT() { return getToken(RustParser.DOTDOT, 0); }
		public List<OuterAttributeContext> outerAttribute() {
			return getRuleContexts(OuterAttributeContext.class);
		}
		public OuterAttributeContext outerAttribute(int i) {
			return getRuleContext(OuterAttributeContext.class,i);
		}
		public StructPatternEtCeteraContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structPatternEtCetera; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterStructPatternEtCetera(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitStructPatternEtCetera(this);
		}
	}

	public final StructPatternEtCeteraContext structPatternEtCetera() throws RecognitionException {
		StructPatternEtCeteraContext _localctx = new StructPatternEtCeteraContext(_ctx, getState());
		enterRule(_localctx, 268, RULE_structPatternEtCetera);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1975);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(1972);
				outerAttribute();
				}
				}
				setState(1977);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1978);
			match(DOTDOT);
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
	public static class TupleStructPatternContext extends ParserRuleContext {
		public PathInExpressionContext pathInExpression() {
			return getRuleContext(PathInExpressionContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(RustParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(RustParser.RPAREN, 0); }
		public TupleStructItemsContext tupleStructItems() {
			return getRuleContext(TupleStructItemsContext.class,0);
		}
		public TupleStructPatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tupleStructPattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTupleStructPattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTupleStructPattern(this);
		}
	}

	public final TupleStructPatternContext tupleStructPattern() throws RecognitionException {
		TupleStructPatternContext _localctx = new TupleStructPatternContext(_ctx, getState());
		enterRule(_localctx, 270, RULE_tupleStructPattern);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1980);
			pathInExpression();
			setState(1981);
			match(LPAREN);
			setState(1983);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417558172729888L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1442300045783222399L) != 0) {
				{
				setState(1982);
				tupleStructItems();
				}
			}

			setState(1985);
			match(RPAREN);
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
	public static class TupleStructItemsContext extends ParserRuleContext {
		public List<PatternContext> pattern() {
			return getRuleContexts(PatternContext.class);
		}
		public PatternContext pattern(int i) {
			return getRuleContext(PatternContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public TupleStructItemsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tupleStructItems; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTupleStructItems(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTupleStructItems(this);
		}
	}

	public final TupleStructItemsContext tupleStructItems() throws RecognitionException {
		TupleStructItemsContext _localctx = new TupleStructItemsContext(_ctx, getState());
		enterRule(_localctx, 272, RULE_tupleStructItems);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1987);
			pattern();
			setState(1992);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,267,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1988);
					match(COMMA);
					setState(1989);
					pattern();
					}
					} 
				}
				setState(1994);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,267,_ctx);
			}
			setState(1996);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(1995);
				match(COMMA);
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
	public static class TuplePatternContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(RustParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(RustParser.RPAREN, 0); }
		public TuplePatternItemsContext tuplePatternItems() {
			return getRuleContext(TuplePatternItemsContext.class,0);
		}
		public TuplePatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tuplePattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTuplePattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTuplePattern(this);
		}
	}

	public final TuplePatternContext tuplePattern() throws RecognitionException {
		TuplePatternContext _localctx = new TuplePatternContext(_ctx, getState());
		enterRule(_localctx, 274, RULE_tuplePattern);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1998);
			match(LPAREN);
			setState(2000);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417558172729888L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1442300045783222399L) != 0) {
				{
				setState(1999);
				tuplePatternItems();
				}
			}

			setState(2002);
			match(RPAREN);
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
	public static class TuplePatternItemsContext extends ParserRuleContext {
		public List<PatternContext> pattern() {
			return getRuleContexts(PatternContext.class);
		}
		public PatternContext pattern(int i) {
			return getRuleContext(PatternContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public RestPatternContext restPattern() {
			return getRuleContext(RestPatternContext.class,0);
		}
		public TuplePatternItemsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tuplePatternItems; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTuplePatternItems(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTuplePatternItems(this);
		}
	}

	public final TuplePatternItemsContext tuplePatternItems() throws RecognitionException {
		TuplePatternItemsContext _localctx = new TuplePatternItemsContext(_ctx, getState());
		enterRule(_localctx, 276, RULE_tuplePatternItems);
		int _la;
		try {
			int _alt;
			setState(2018);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,272,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2004);
				pattern();
				setState(2005);
				match(COMMA);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2007);
				restPattern();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2008);
				pattern();
				setState(2011); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(2009);
						match(COMMA);
						setState(2010);
						pattern();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(2013); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,270,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(2016);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(2015);
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
	public static class GroupedPatternContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(RustParser.LPAREN, 0); }
		public PatternContext pattern() {
			return getRuleContext(PatternContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(RustParser.RPAREN, 0); }
		public GroupedPatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupedPattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterGroupedPattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitGroupedPattern(this);
		}
	}

	public final GroupedPatternContext groupedPattern() throws RecognitionException {
		GroupedPatternContext _localctx = new GroupedPatternContext(_ctx, getState());
		enterRule(_localctx, 278, RULE_groupedPattern);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2020);
			match(LPAREN);
			setState(2021);
			pattern();
			setState(2022);
			match(RPAREN);
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
	public static class SlicePatternContext extends ParserRuleContext {
		public TerminalNode LSQUAREBRACKET() { return getToken(RustParser.LSQUAREBRACKET, 0); }
		public TerminalNode RSQUAREBRACKET() { return getToken(RustParser.RSQUAREBRACKET, 0); }
		public SlicePatternItemsContext slicePatternItems() {
			return getRuleContext(SlicePatternItemsContext.class,0);
		}
		public SlicePatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_slicePattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterSlicePattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitSlicePattern(this);
		}
	}

	public final SlicePatternContext slicePattern() throws RecognitionException {
		SlicePatternContext _localctx = new SlicePatternContext(_ctx, getState());
		enterRule(_localctx, 280, RULE_slicePattern);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2024);
			match(LSQUAREBRACKET);
			setState(2026);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417558172729888L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1442300045783222399L) != 0) {
				{
				setState(2025);
				slicePatternItems();
				}
			}

			setState(2028);
			match(RSQUAREBRACKET);
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
	public static class SlicePatternItemsContext extends ParserRuleContext {
		public List<PatternContext> pattern() {
			return getRuleContexts(PatternContext.class);
		}
		public PatternContext pattern(int i) {
			return getRuleContext(PatternContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public SlicePatternItemsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_slicePatternItems; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterSlicePatternItems(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitSlicePatternItems(this);
		}
	}

	public final SlicePatternItemsContext slicePatternItems() throws RecognitionException {
		SlicePatternItemsContext _localctx = new SlicePatternItemsContext(_ctx, getState());
		enterRule(_localctx, 282, RULE_slicePatternItems);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2030);
			pattern();
			setState(2035);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,274,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2031);
					match(COMMA);
					setState(2032);
					pattern();
					}
					} 
				}
				setState(2037);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,274,_ctx);
			}
			setState(2039);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(2038);
				match(COMMA);
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
	public static class PathPatternContext extends ParserRuleContext {
		public PathInExpressionContext pathInExpression() {
			return getRuleContext(PathInExpressionContext.class,0);
		}
		public QualifiedPathInExpressionContext qualifiedPathInExpression() {
			return getRuleContext(QualifiedPathInExpressionContext.class,0);
		}
		public PathPatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathPattern; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterPathPattern(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitPathPattern(this);
		}
	}

	public final PathPatternContext pathPattern() throws RecognitionException {
		PathPatternContext _localctx = new PathPatternContext(_ctx, getState());
		enterRule(_localctx, 284, RULE_pathPattern);
		try {
			setState(2043);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_CRATE:
			case KW_SELFVALUE:
			case KW_SELFTYPE:
			case KW_SUPER:
			case KW_MACRORULES:
			case KW_DOLLARCRATE:
			case NON_KEYWORD_IDENTIFIER:
			case RAW_IDENTIFIER:
			case PATHSEP:
				enterOuterAlt(_localctx, 1);
				{
				setState(2041);
				pathInExpression();
				}
				break;
			case LT:
				enterOuterAlt(_localctx, 2);
				{
				setState(2042);
				qualifiedPathInExpression();
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
	public static class Type_Context extends ParserRuleContext {
		public TypeNoBoundsContext typeNoBounds() {
			return getRuleContext(TypeNoBoundsContext.class,0);
		}
		public ImplTraitTypeContext implTraitType() {
			return getRuleContext(ImplTraitTypeContext.class,0);
		}
		public TraitObjectTypeContext traitObjectType() {
			return getRuleContext(TraitObjectTypeContext.class,0);
		}
		public Type_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterType_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitType_(this);
		}
	}

	public final Type_Context type_() throws RecognitionException {
		Type_Context _localctx = new Type_Context(_ctx, getState());
		enterRule(_localctx, 286, RULE_type_);
		try {
			setState(2048);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,277,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2045);
				typeNoBounds();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2046);
				implTraitType();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2047);
				traitObjectType();
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
	public static class TypeNoBoundsContext extends ParserRuleContext {
		public ParenthesizedTypeContext parenthesizedType() {
			return getRuleContext(ParenthesizedTypeContext.class,0);
		}
		public ImplTraitTypeOneBoundContext implTraitTypeOneBound() {
			return getRuleContext(ImplTraitTypeOneBoundContext.class,0);
		}
		public TraitObjectTypeOneBoundContext traitObjectTypeOneBound() {
			return getRuleContext(TraitObjectTypeOneBoundContext.class,0);
		}
		public TypePathContext typePath() {
			return getRuleContext(TypePathContext.class,0);
		}
		public TupleTypeContext tupleType() {
			return getRuleContext(TupleTypeContext.class,0);
		}
		public NeverTypeContext neverType() {
			return getRuleContext(NeverTypeContext.class,0);
		}
		public RawPointerTypeContext rawPointerType() {
			return getRuleContext(RawPointerTypeContext.class,0);
		}
		public ReferenceTypeContext referenceType() {
			return getRuleContext(ReferenceTypeContext.class,0);
		}
		public ArrayTypeContext arrayType() {
			return getRuleContext(ArrayTypeContext.class,0);
		}
		public SliceTypeContext sliceType() {
			return getRuleContext(SliceTypeContext.class,0);
		}
		public InferredTypeContext inferredType() {
			return getRuleContext(InferredTypeContext.class,0);
		}
		public QualifiedPathInTypeContext qualifiedPathInType() {
			return getRuleContext(QualifiedPathInTypeContext.class,0);
		}
		public BareFunctionTypeContext bareFunctionType() {
			return getRuleContext(BareFunctionTypeContext.class,0);
		}
		public MacroInvocationContext macroInvocation() {
			return getRuleContext(MacroInvocationContext.class,0);
		}
		public TypeNoBoundsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeNoBounds; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTypeNoBounds(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTypeNoBounds(this);
		}
	}

	public final TypeNoBoundsContext typeNoBounds() throws RecognitionException {
		TypeNoBoundsContext _localctx = new TypeNoBoundsContext(_ctx, getState());
		enterRule(_localctx, 288, RULE_typeNoBounds);
		try {
			setState(2064);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,278,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2050);
				parenthesizedType();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2051);
				implTraitTypeOneBound();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2052);
				traitObjectTypeOneBound();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2053);
				typePath();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2054);
				tupleType();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2055);
				neverType();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(2056);
				rawPointerType();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(2057);
				referenceType();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(2058);
				arrayType();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(2059);
				sliceType();
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(2060);
				inferredType();
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(2061);
				qualifiedPathInType();
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(2062);
				bareFunctionType();
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(2063);
				macroInvocation();
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
	public static class ParenthesizedTypeContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(RustParser.LPAREN, 0); }
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public TerminalNode RPAREN() { return getToken(RustParser.RPAREN, 0); }
		public ParenthesizedTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parenthesizedType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterParenthesizedType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitParenthesizedType(this);
		}
	}

	public final ParenthesizedTypeContext parenthesizedType() throws RecognitionException {
		ParenthesizedTypeContext _localctx = new ParenthesizedTypeContext(_ctx, getState());
		enterRule(_localctx, 290, RULE_parenthesizedType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2066);
			match(LPAREN);
			setState(2067);
			type_();
			setState(2068);
			match(RPAREN);
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
	public static class NeverTypeContext extends ParserRuleContext {
		public TerminalNode NOT() { return getToken(RustParser.NOT, 0); }
		public NeverTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_neverType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterNeverType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitNeverType(this);
		}
	}

	public final NeverTypeContext neverType() throws RecognitionException {
		NeverTypeContext _localctx = new NeverTypeContext(_ctx, getState());
		enterRule(_localctx, 292, RULE_neverType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2070);
			match(NOT);
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
	public static class TupleTypeContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(RustParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(RustParser.RPAREN, 0); }
		public List<Type_Context> type_() {
			return getRuleContexts(Type_Context.class);
		}
		public Type_Context type_(int i) {
			return getRuleContext(Type_Context.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public TupleTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tupleType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTupleType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTupleType(this);
		}
	}

	public final TupleTypeContext tupleType() throws RecognitionException {
		TupleTypeContext _localctx = new TupleTypeContext(_ctx, getState());
		enterRule(_localctx, 294, RULE_tupleType);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2072);
			match(LPAREN);
			setState(2083);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 567453832540335392L) != 0 || (((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 360915832668553L) != 0) {
				{
				setState(2076); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(2073);
						type_();
						setState(2074);
						match(COMMA);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(2078); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,279,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(2081);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 567453832540335392L) != 0 || (((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 360915832668553L) != 0) {
					{
					setState(2080);
					type_();
					}
				}

				}
			}

			setState(2085);
			match(RPAREN);
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
	public static class ArrayTypeContext extends ParserRuleContext {
		public TerminalNode LSQUAREBRACKET() { return getToken(RustParser.LSQUAREBRACKET, 0); }
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public TerminalNode SEMI() { return getToken(RustParser.SEMI, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RSQUAREBRACKET() { return getToken(RustParser.RSQUAREBRACKET, 0); }
		public ArrayTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterArrayType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitArrayType(this);
		}
	}

	public final ArrayTypeContext arrayType() throws RecognitionException {
		ArrayTypeContext _localctx = new ArrayTypeContext(_ctx, getState());
		enterRule(_localctx, 296, RULE_arrayType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2087);
			match(LSQUAREBRACKET);
			setState(2088);
			type_();
			setState(2089);
			match(SEMI);
			setState(2090);
			expression(0);
			setState(2091);
			match(RSQUAREBRACKET);
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
	public static class SliceTypeContext extends ParserRuleContext {
		public TerminalNode LSQUAREBRACKET() { return getToken(RustParser.LSQUAREBRACKET, 0); }
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public TerminalNode RSQUAREBRACKET() { return getToken(RustParser.RSQUAREBRACKET, 0); }
		public SliceTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sliceType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterSliceType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitSliceType(this);
		}
	}

	public final SliceTypeContext sliceType() throws RecognitionException {
		SliceTypeContext _localctx = new SliceTypeContext(_ctx, getState());
		enterRule(_localctx, 298, RULE_sliceType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2093);
			match(LSQUAREBRACKET);
			setState(2094);
			type_();
			setState(2095);
			match(RSQUAREBRACKET);
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
	public static class ReferenceTypeContext extends ParserRuleContext {
		public TerminalNode AND() { return getToken(RustParser.AND, 0); }
		public TypeNoBoundsContext typeNoBounds() {
			return getRuleContext(TypeNoBoundsContext.class,0);
		}
		public LifetimeContext lifetime() {
			return getRuleContext(LifetimeContext.class,0);
		}
		public TerminalNode KW_MUT() { return getToken(RustParser.KW_MUT, 0); }
		public ReferenceTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_referenceType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterReferenceType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitReferenceType(this);
		}
	}

	public final ReferenceTypeContext referenceType() throws RecognitionException {
		ReferenceTypeContext _localctx = new ReferenceTypeContext(_ctx, getState());
		enterRule(_localctx, 300, RULE_referenceType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2097);
			match(AND);
			setState(2099);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la - 53)) & ~0x3f) == 0 && ((1L << (_la - 53)) & 67108869L) != 0) {
				{
				setState(2098);
				lifetime();
				}
			}

			setState(2102);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_MUT) {
				{
				setState(2101);
				match(KW_MUT);
				}
			}

			setState(2104);
			typeNoBounds();
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
	public static class RawPointerTypeContext extends ParserRuleContext {
		public TerminalNode STAR() { return getToken(RustParser.STAR, 0); }
		public TypeNoBoundsContext typeNoBounds() {
			return getRuleContext(TypeNoBoundsContext.class,0);
		}
		public TerminalNode KW_MUT() { return getToken(RustParser.KW_MUT, 0); }
		public TerminalNode KW_CONST() { return getToken(RustParser.KW_CONST, 0); }
		public RawPointerTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rawPointerType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterRawPointerType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitRawPointerType(this);
		}
	}

	public final RawPointerTypeContext rawPointerType() throws RecognitionException {
		RawPointerTypeContext _localctx = new RawPointerTypeContext(_ctx, getState());
		enterRule(_localctx, 302, RULE_rawPointerType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2106);
			match(STAR);
			setState(2107);
			_la = _input.LA(1);
			if ( !(_la==KW_CONST || _la==KW_MUT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(2108);
			typeNoBounds();
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
	public static class BareFunctionTypeContext extends ParserRuleContext {
		public FunctionTypeQualifiersContext functionTypeQualifiers() {
			return getRuleContext(FunctionTypeQualifiersContext.class,0);
		}
		public TerminalNode KW_FN() { return getToken(RustParser.KW_FN, 0); }
		public TerminalNode LPAREN() { return getToken(RustParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(RustParser.RPAREN, 0); }
		public ForLifetimesContext forLifetimes() {
			return getRuleContext(ForLifetimesContext.class,0);
		}
		public FunctionParametersMaybeNamedVariadicContext functionParametersMaybeNamedVariadic() {
			return getRuleContext(FunctionParametersMaybeNamedVariadicContext.class,0);
		}
		public BareFunctionReturnTypeContext bareFunctionReturnType() {
			return getRuleContext(BareFunctionReturnTypeContext.class,0);
		}
		public BareFunctionTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bareFunctionType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterBareFunctionType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitBareFunctionType(this);
		}
	}

	public final BareFunctionTypeContext bareFunctionType() throws RecognitionException {
		BareFunctionTypeContext _localctx = new BareFunctionTypeContext(_ctx, getState());
		enterRule(_localctx, 304, RULE_bareFunctionType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2111);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_FOR) {
				{
				setState(2110);
				forLifetimes();
				}
			}

			setState(2113);
			functionTypeQualifiers();
			setState(2114);
			match(KW_FN);
			setState(2115);
			match(LPAREN);
			setState(2117);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 567453832540335392L) != 0 || (((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 363114855924105L) != 0) {
				{
				setState(2116);
				functionParametersMaybeNamedVariadic();
				}
			}

			setState(2119);
			match(RPAREN);
			setState(2121);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,286,_ctx) ) {
			case 1:
				{
				setState(2120);
				bareFunctionReturnType();
				}
				break;
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
	public static class FunctionTypeQualifiersContext extends ParserRuleContext {
		public TerminalNode KW_UNSAFE() { return getToken(RustParser.KW_UNSAFE, 0); }
		public TerminalNode KW_EXTERN() { return getToken(RustParser.KW_EXTERN, 0); }
		public AbiContext abi() {
			return getRuleContext(AbiContext.class,0);
		}
		public FunctionTypeQualifiersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionTypeQualifiers; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterFunctionTypeQualifiers(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitFunctionTypeQualifiers(this);
		}
	}

	public final FunctionTypeQualifiersContext functionTypeQualifiers() throws RecognitionException {
		FunctionTypeQualifiersContext _localctx = new FunctionTypeQualifiersContext(_ctx, getState());
		enterRule(_localctx, 306, RULE_functionTypeQualifiers);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2124);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_UNSAFE) {
				{
				setState(2123);
				match(KW_UNSAFE);
				}
			}

			setState(2130);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_EXTERN) {
				{
				setState(2126);
				match(KW_EXTERN);
				setState(2128);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STRING_LITERAL || _la==RAW_STRING_LITERAL) {
					{
					setState(2127);
					abi();
					}
				}

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
	public static class BareFunctionReturnTypeContext extends ParserRuleContext {
		public TerminalNode RARROW() { return getToken(RustParser.RARROW, 0); }
		public TypeNoBoundsContext typeNoBounds() {
			return getRuleContext(TypeNoBoundsContext.class,0);
		}
		public BareFunctionReturnTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bareFunctionReturnType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterBareFunctionReturnType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitBareFunctionReturnType(this);
		}
	}

	public final BareFunctionReturnTypeContext bareFunctionReturnType() throws RecognitionException {
		BareFunctionReturnTypeContext _localctx = new BareFunctionReturnTypeContext(_ctx, getState());
		enterRule(_localctx, 308, RULE_bareFunctionReturnType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2132);
			match(RARROW);
			setState(2133);
			typeNoBounds();
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
	public static class FunctionParametersMaybeNamedVariadicContext extends ParserRuleContext {
		public MaybeNamedFunctionParametersContext maybeNamedFunctionParameters() {
			return getRuleContext(MaybeNamedFunctionParametersContext.class,0);
		}
		public MaybeNamedFunctionParametersVariadicContext maybeNamedFunctionParametersVariadic() {
			return getRuleContext(MaybeNamedFunctionParametersVariadicContext.class,0);
		}
		public FunctionParametersMaybeNamedVariadicContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionParametersMaybeNamedVariadic; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterFunctionParametersMaybeNamedVariadic(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitFunctionParametersMaybeNamedVariadic(this);
		}
	}

	public final FunctionParametersMaybeNamedVariadicContext functionParametersMaybeNamedVariadic() throws RecognitionException {
		FunctionParametersMaybeNamedVariadicContext _localctx = new FunctionParametersMaybeNamedVariadicContext(_ctx, getState());
		enterRule(_localctx, 310, RULE_functionParametersMaybeNamedVariadic);
		try {
			setState(2137);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,290,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2135);
				maybeNamedFunctionParameters();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2136);
				maybeNamedFunctionParametersVariadic();
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
	public static class MaybeNamedFunctionParametersContext extends ParserRuleContext {
		public List<MaybeNamedParamContext> maybeNamedParam() {
			return getRuleContexts(MaybeNamedParamContext.class);
		}
		public MaybeNamedParamContext maybeNamedParam(int i) {
			return getRuleContext(MaybeNamedParamContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public MaybeNamedFunctionParametersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_maybeNamedFunctionParameters; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMaybeNamedFunctionParameters(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMaybeNamedFunctionParameters(this);
		}
	}

	public final MaybeNamedFunctionParametersContext maybeNamedFunctionParameters() throws RecognitionException {
		MaybeNamedFunctionParametersContext _localctx = new MaybeNamedFunctionParametersContext(_ctx, getState());
		enterRule(_localctx, 312, RULE_maybeNamedFunctionParameters);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2139);
			maybeNamedParam();
			setState(2144);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,291,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2140);
					match(COMMA);
					setState(2141);
					maybeNamedParam();
					}
					} 
				}
				setState(2146);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,291,_ctx);
			}
			setState(2148);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(2147);
				match(COMMA);
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
	public static class MaybeNamedParamContext extends ParserRuleContext {
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public List<OuterAttributeContext> outerAttribute() {
			return getRuleContexts(OuterAttributeContext.class);
		}
		public OuterAttributeContext outerAttribute(int i) {
			return getRuleContext(OuterAttributeContext.class,i);
		}
		public TerminalNode COLON() { return getToken(RustParser.COLON, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode UNDERSCORE() { return getToken(RustParser.UNDERSCORE, 0); }
		public MaybeNamedParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_maybeNamedParam; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMaybeNamedParam(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMaybeNamedParam(this);
		}
	}

	public final MaybeNamedParamContext maybeNamedParam() throws RecognitionException {
		MaybeNamedParamContext _localctx = new MaybeNamedParamContext(_ctx, getState());
		enterRule(_localctx, 314, RULE_maybeNamedParam);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2153);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(2150);
				outerAttribute();
				}
				}
				setState(2155);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2161);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,295,_ctx) ) {
			case 1:
				{
				setState(2158);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case KW_MACRORULES:
				case NON_KEYWORD_IDENTIFIER:
				case RAW_IDENTIFIER:
					{
					setState(2156);
					identifier();
					}
					break;
				case UNDERSCORE:
					{
					setState(2157);
					match(UNDERSCORE);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(2160);
				match(COLON);
				}
				break;
			}
			setState(2163);
			type_();
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
	public static class MaybeNamedFunctionParametersVariadicContext extends ParserRuleContext {
		public List<MaybeNamedParamContext> maybeNamedParam() {
			return getRuleContexts(MaybeNamedParamContext.class);
		}
		public MaybeNamedParamContext maybeNamedParam(int i) {
			return getRuleContext(MaybeNamedParamContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public TerminalNode DOTDOTDOT() { return getToken(RustParser.DOTDOTDOT, 0); }
		public List<OuterAttributeContext> outerAttribute() {
			return getRuleContexts(OuterAttributeContext.class);
		}
		public OuterAttributeContext outerAttribute(int i) {
			return getRuleContext(OuterAttributeContext.class,i);
		}
		public MaybeNamedFunctionParametersVariadicContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_maybeNamedFunctionParametersVariadic; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMaybeNamedFunctionParametersVariadic(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMaybeNamedFunctionParametersVariadic(this);
		}
	}

	public final MaybeNamedFunctionParametersVariadicContext maybeNamedFunctionParametersVariadic() throws RecognitionException {
		MaybeNamedFunctionParametersVariadicContext _localctx = new MaybeNamedFunctionParametersVariadicContext(_ctx, getState());
		enterRule(_localctx, 316, RULE_maybeNamedFunctionParametersVariadic);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2170);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,296,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2165);
					maybeNamedParam();
					setState(2166);
					match(COMMA);
					}
					} 
				}
				setState(2172);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,296,_ctx);
			}
			setState(2173);
			maybeNamedParam();
			setState(2174);
			match(COMMA);
			setState(2178);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(2175);
				outerAttribute();
				}
				}
				setState(2180);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2181);
			match(DOTDOTDOT);
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
	public static class TraitObjectTypeContext extends ParserRuleContext {
		public TypeParamBoundsContext typeParamBounds() {
			return getRuleContext(TypeParamBoundsContext.class,0);
		}
		public TerminalNode KW_DYN() { return getToken(RustParser.KW_DYN, 0); }
		public TraitObjectTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_traitObjectType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTraitObjectType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTraitObjectType(this);
		}
	}

	public final TraitObjectTypeContext traitObjectType() throws RecognitionException {
		TraitObjectTypeContext _localctx = new TraitObjectTypeContext(_ctx, getState());
		enterRule(_localctx, 318, RULE_traitObjectType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2184);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_DYN) {
				{
				setState(2183);
				match(KW_DYN);
				}
			}

			setState(2186);
			typeParamBounds();
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
	public static class TraitObjectTypeOneBoundContext extends ParserRuleContext {
		public TraitBoundContext traitBound() {
			return getRuleContext(TraitBoundContext.class,0);
		}
		public TerminalNode KW_DYN() { return getToken(RustParser.KW_DYN, 0); }
		public TraitObjectTypeOneBoundContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_traitObjectTypeOneBound; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTraitObjectTypeOneBound(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTraitObjectTypeOneBound(this);
		}
	}

	public final TraitObjectTypeOneBoundContext traitObjectTypeOneBound() throws RecognitionException {
		TraitObjectTypeOneBoundContext _localctx = new TraitObjectTypeOneBoundContext(_ctx, getState());
		enterRule(_localctx, 320, RULE_traitObjectTypeOneBound);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2189);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_DYN) {
				{
				setState(2188);
				match(KW_DYN);
				}
			}

			setState(2191);
			traitBound();
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
	public static class ImplTraitTypeContext extends ParserRuleContext {
		public TerminalNode KW_IMPL() { return getToken(RustParser.KW_IMPL, 0); }
		public TypeParamBoundsContext typeParamBounds() {
			return getRuleContext(TypeParamBoundsContext.class,0);
		}
		public ImplTraitTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_implTraitType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterImplTraitType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitImplTraitType(this);
		}
	}

	public final ImplTraitTypeContext implTraitType() throws RecognitionException {
		ImplTraitTypeContext _localctx = new ImplTraitTypeContext(_ctx, getState());
		enterRule(_localctx, 322, RULE_implTraitType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2193);
			match(KW_IMPL);
			setState(2194);
			typeParamBounds();
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
	public static class ImplTraitTypeOneBoundContext extends ParserRuleContext {
		public TerminalNode KW_IMPL() { return getToken(RustParser.KW_IMPL, 0); }
		public TraitBoundContext traitBound() {
			return getRuleContext(TraitBoundContext.class,0);
		}
		public ImplTraitTypeOneBoundContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_implTraitTypeOneBound; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterImplTraitTypeOneBound(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitImplTraitTypeOneBound(this);
		}
	}

	public final ImplTraitTypeOneBoundContext implTraitTypeOneBound() throws RecognitionException {
		ImplTraitTypeOneBoundContext _localctx = new ImplTraitTypeOneBoundContext(_ctx, getState());
		enterRule(_localctx, 324, RULE_implTraitTypeOneBound);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2196);
			match(KW_IMPL);
			setState(2197);
			traitBound();
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
	public static class InferredTypeContext extends ParserRuleContext {
		public TerminalNode UNDERSCORE() { return getToken(RustParser.UNDERSCORE, 0); }
		public InferredTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inferredType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterInferredType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitInferredType(this);
		}
	}

	public final InferredTypeContext inferredType() throws RecognitionException {
		InferredTypeContext _localctx = new InferredTypeContext(_ctx, getState());
		enterRule(_localctx, 326, RULE_inferredType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2199);
			match(UNDERSCORE);
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
	public static class TypeParamBoundsContext extends ParserRuleContext {
		public List<TypeParamBoundContext> typeParamBound() {
			return getRuleContexts(TypeParamBoundContext.class);
		}
		public TypeParamBoundContext typeParamBound(int i) {
			return getRuleContext(TypeParamBoundContext.class,i);
		}
		public List<TerminalNode> PLUS() { return getTokens(RustParser.PLUS); }
		public TerminalNode PLUS(int i) {
			return getToken(RustParser.PLUS, i);
		}
		public TypeParamBoundsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeParamBounds; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTypeParamBounds(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTypeParamBounds(this);
		}
	}

	public final TypeParamBoundsContext typeParamBounds() throws RecognitionException {
		TypeParamBoundsContext _localctx = new TypeParamBoundsContext(_ctx, getState());
		enterRule(_localctx, 328, RULE_typeParamBounds);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2201);
			typeParamBound();
			setState(2206);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,300,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2202);
					match(PLUS);
					setState(2203);
					typeParamBound();
					}
					} 
				}
				setState(2208);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,300,_ctx);
			}
			setState(2210);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,301,_ctx) ) {
			case 1:
				{
				setState(2209);
				match(PLUS);
				}
				break;
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
	public static class TypeParamBoundContext extends ParserRuleContext {
		public LifetimeContext lifetime() {
			return getRuleContext(LifetimeContext.class,0);
		}
		public TraitBoundContext traitBound() {
			return getRuleContext(TraitBoundContext.class,0);
		}
		public TypeParamBoundContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeParamBound; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTypeParamBound(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTypeParamBound(this);
		}
	}

	public final TypeParamBoundContext typeParamBound() throws RecognitionException {
		TypeParamBoundContext _localctx = new TypeParamBoundContext(_ctx, getState());
		enterRule(_localctx, 330, RULE_typeParamBound);
		try {
			setState(2214);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_STATICLIFETIME:
			case KW_UNDERLINELIFETIME:
			case LIFETIME_OR_LABEL:
				enterOuterAlt(_localctx, 1);
				{
				setState(2212);
				lifetime();
				}
				break;
			case KW_CRATE:
			case KW_FOR:
			case KW_SELFVALUE:
			case KW_SELFTYPE:
			case KW_SUPER:
			case KW_MACRORULES:
			case KW_DOLLARCRATE:
			case NON_KEYWORD_IDENTIFIER:
			case RAW_IDENTIFIER:
			case PATHSEP:
			case QUESTION:
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(2213);
				traitBound();
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
	public static class TraitBoundContext extends ParserRuleContext {
		public TypePathContext typePath() {
			return getRuleContext(TypePathContext.class,0);
		}
		public TerminalNode QUESTION() { return getToken(RustParser.QUESTION, 0); }
		public ForLifetimesContext forLifetimes() {
			return getRuleContext(ForLifetimesContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(RustParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(RustParser.RPAREN, 0); }
		public TraitBoundContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_traitBound; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTraitBound(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTraitBound(this);
		}
	}

	public final TraitBoundContext traitBound() throws RecognitionException {
		TraitBoundContext _localctx = new TraitBoundContext(_ctx, getState());
		enterRule(_localctx, 332, RULE_traitBound);
		int _la;
		try {
			setState(2233);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_CRATE:
			case KW_FOR:
			case KW_SELFVALUE:
			case KW_SELFTYPE:
			case KW_SUPER:
			case KW_MACRORULES:
			case KW_DOLLARCRATE:
			case NON_KEYWORD_IDENTIFIER:
			case RAW_IDENTIFIER:
			case PATHSEP:
			case QUESTION:
				enterOuterAlt(_localctx, 1);
				{
				setState(2217);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==QUESTION) {
					{
					setState(2216);
					match(QUESTION);
					}
				}

				setState(2220);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==KW_FOR) {
					{
					setState(2219);
					forLifetimes();
					}
				}

				setState(2222);
				typePath();
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(2223);
				match(LPAREN);
				setState(2225);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==QUESTION) {
					{
					setState(2224);
					match(QUESTION);
					}
				}

				setState(2228);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==KW_FOR) {
					{
					setState(2227);
					forLifetimes();
					}
				}

				setState(2230);
				typePath();
				setState(2231);
				match(RPAREN);
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
	public static class LifetimeBoundsContext extends ParserRuleContext {
		public List<LifetimeContext> lifetime() {
			return getRuleContexts(LifetimeContext.class);
		}
		public LifetimeContext lifetime(int i) {
			return getRuleContext(LifetimeContext.class,i);
		}
		public List<TerminalNode> PLUS() { return getTokens(RustParser.PLUS); }
		public TerminalNode PLUS(int i) {
			return getToken(RustParser.PLUS, i);
		}
		public LifetimeBoundsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lifetimeBounds; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterLifetimeBounds(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitLifetimeBounds(this);
		}
	}

	public final LifetimeBoundsContext lifetimeBounds() throws RecognitionException {
		LifetimeBoundsContext _localctx = new LifetimeBoundsContext(_ctx, getState());
		enterRule(_localctx, 334, RULE_lifetimeBounds);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2240);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,308,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2235);
					lifetime();
					setState(2236);
					match(PLUS);
					}
					} 
				}
				setState(2242);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,308,_ctx);
			}
			setState(2244);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la - 53)) & ~0x3f) == 0 && ((1L << (_la - 53)) & 67108869L) != 0) {
				{
				setState(2243);
				lifetime();
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
	public static class LifetimeContext extends ParserRuleContext {
		public TerminalNode LIFETIME_OR_LABEL() { return getToken(RustParser.LIFETIME_OR_LABEL, 0); }
		public TerminalNode KW_STATICLIFETIME() { return getToken(RustParser.KW_STATICLIFETIME, 0); }
		public TerminalNode KW_UNDERLINELIFETIME() { return getToken(RustParser.KW_UNDERLINELIFETIME, 0); }
		public LifetimeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lifetime; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterLifetime(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitLifetime(this);
		}
	}

	public final LifetimeContext lifetime() throws RecognitionException {
		LifetimeContext _localctx = new LifetimeContext(_ctx, getState());
		enterRule(_localctx, 336, RULE_lifetime);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2246);
			_la = _input.LA(1);
			if ( !((((_la - 53)) & ~0x3f) == 0 && ((1L << (_la - 53)) & 67108869L) != 0) ) {
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
	public static class SimplePathContext extends ParserRuleContext {
		public List<SimplePathSegmentContext> simplePathSegment() {
			return getRuleContexts(SimplePathSegmentContext.class);
		}
		public SimplePathSegmentContext simplePathSegment(int i) {
			return getRuleContext(SimplePathSegmentContext.class,i);
		}
		public List<TerminalNode> PATHSEP() { return getTokens(RustParser.PATHSEP); }
		public TerminalNode PATHSEP(int i) {
			return getToken(RustParser.PATHSEP, i);
		}
		public SimplePathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simplePath; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterSimplePath(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitSimplePath(this);
		}
	}

	public final SimplePathContext simplePath() throws RecognitionException {
		SimplePathContext _localctx = new SimplePathContext(_ctx, getState());
		enterRule(_localctx, 338, RULE_simplePath);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2249);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PATHSEP) {
				{
				setState(2248);
				match(PATHSEP);
				}
			}

			setState(2251);
			simplePathSegment();
			setState(2256);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,311,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2252);
					match(PATHSEP);
					setState(2253);
					simplePathSegment();
					}
					} 
				}
				setState(2258);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,311,_ctx);
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
	public static class SimplePathSegmentContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode KW_SUPER() { return getToken(RustParser.KW_SUPER, 0); }
		public TerminalNode KW_SELFVALUE() { return getToken(RustParser.KW_SELFVALUE, 0); }
		public TerminalNode KW_CRATE() { return getToken(RustParser.KW_CRATE, 0); }
		public TerminalNode KW_DOLLARCRATE() { return getToken(RustParser.KW_DOLLARCRATE, 0); }
		public SimplePathSegmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simplePathSegment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterSimplePathSegment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitSimplePathSegment(this);
		}
	}

	public final SimplePathSegmentContext simplePathSegment() throws RecognitionException {
		SimplePathSegmentContext _localctx = new SimplePathSegmentContext(_ctx, getState());
		enterRule(_localctx, 340, RULE_simplePathSegment);
		try {
			setState(2264);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_MACRORULES:
			case NON_KEYWORD_IDENTIFIER:
			case RAW_IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(2259);
				identifier();
				}
				break;
			case KW_SUPER:
				enterOuterAlt(_localctx, 2);
				{
				setState(2260);
				match(KW_SUPER);
				}
				break;
			case KW_SELFVALUE:
				enterOuterAlt(_localctx, 3);
				{
				setState(2261);
				match(KW_SELFVALUE);
				}
				break;
			case KW_CRATE:
				enterOuterAlt(_localctx, 4);
				{
				setState(2262);
				match(KW_CRATE);
				}
				break;
			case KW_DOLLARCRATE:
				enterOuterAlt(_localctx, 5);
				{
				setState(2263);
				match(KW_DOLLARCRATE);
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
	public static class PathInExpressionContext extends ParserRuleContext {
		public List<PathExprSegmentContext> pathExprSegment() {
			return getRuleContexts(PathExprSegmentContext.class);
		}
		public PathExprSegmentContext pathExprSegment(int i) {
			return getRuleContext(PathExprSegmentContext.class,i);
		}
		public List<TerminalNode> PATHSEP() { return getTokens(RustParser.PATHSEP); }
		public TerminalNode PATHSEP(int i) {
			return getToken(RustParser.PATHSEP, i);
		}
		public PathInExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathInExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterPathInExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitPathInExpression(this);
		}
	}

	public final PathInExpressionContext pathInExpression() throws RecognitionException {
		PathInExpressionContext _localctx = new PathInExpressionContext(_ctx, getState());
		enterRule(_localctx, 342, RULE_pathInExpression);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2267);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PATHSEP) {
				{
				setState(2266);
				match(PATHSEP);
				}
			}

			setState(2269);
			pathExprSegment();
			setState(2274);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,314,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2270);
					match(PATHSEP);
					setState(2271);
					pathExprSegment();
					}
					} 
				}
				setState(2276);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,314,_ctx);
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
	public static class PathExprSegmentContext extends ParserRuleContext {
		public PathIdentSegmentContext pathIdentSegment() {
			return getRuleContext(PathIdentSegmentContext.class,0);
		}
		public TerminalNode PATHSEP() { return getToken(RustParser.PATHSEP, 0); }
		public GenericArgsContext genericArgs() {
			return getRuleContext(GenericArgsContext.class,0);
		}
		public PathExprSegmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathExprSegment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterPathExprSegment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitPathExprSegment(this);
		}
	}

	public final PathExprSegmentContext pathExprSegment() throws RecognitionException {
		PathExprSegmentContext _localctx = new PathExprSegmentContext(_ctx, getState());
		enterRule(_localctx, 344, RULE_pathExprSegment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2277);
			pathIdentSegment();
			setState(2280);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,315,_ctx) ) {
			case 1:
				{
				setState(2278);
				match(PATHSEP);
				setState(2279);
				genericArgs();
				}
				break;
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
	public static class PathIdentSegmentContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode KW_SUPER() { return getToken(RustParser.KW_SUPER, 0); }
		public TerminalNode KW_SELFVALUE() { return getToken(RustParser.KW_SELFVALUE, 0); }
		public TerminalNode KW_SELFTYPE() { return getToken(RustParser.KW_SELFTYPE, 0); }
		public TerminalNode KW_CRATE() { return getToken(RustParser.KW_CRATE, 0); }
		public TerminalNode KW_DOLLARCRATE() { return getToken(RustParser.KW_DOLLARCRATE, 0); }
		public PathIdentSegmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathIdentSegment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterPathIdentSegment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitPathIdentSegment(this);
		}
	}

	public final PathIdentSegmentContext pathIdentSegment() throws RecognitionException {
		PathIdentSegmentContext _localctx = new PathIdentSegmentContext(_ctx, getState());
		enterRule(_localctx, 346, RULE_pathIdentSegment);
		try {
			setState(2288);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_MACRORULES:
			case NON_KEYWORD_IDENTIFIER:
			case RAW_IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(2282);
				identifier();
				}
				break;
			case KW_SUPER:
				enterOuterAlt(_localctx, 2);
				{
				setState(2283);
				match(KW_SUPER);
				}
				break;
			case KW_SELFVALUE:
				enterOuterAlt(_localctx, 3);
				{
				setState(2284);
				match(KW_SELFVALUE);
				}
				break;
			case KW_SELFTYPE:
				enterOuterAlt(_localctx, 4);
				{
				setState(2285);
				match(KW_SELFTYPE);
				}
				break;
			case KW_CRATE:
				enterOuterAlt(_localctx, 5);
				{
				setState(2286);
				match(KW_CRATE);
				}
				break;
			case KW_DOLLARCRATE:
				enterOuterAlt(_localctx, 6);
				{
				setState(2287);
				match(KW_DOLLARCRATE);
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
	public static class GenericArgsContext extends ParserRuleContext {
		public TerminalNode LT() { return getToken(RustParser.LT, 0); }
		public TerminalNode GT() { return getToken(RustParser.GT, 0); }
		public GenericArgsLifetimesContext genericArgsLifetimes() {
			return getRuleContext(GenericArgsLifetimesContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public GenericArgsTypesContext genericArgsTypes() {
			return getRuleContext(GenericArgsTypesContext.class,0);
		}
		public GenericArgsBindingsContext genericArgsBindings() {
			return getRuleContext(GenericArgsBindingsContext.class,0);
		}
		public List<GenericArgContext> genericArg() {
			return getRuleContexts(GenericArgContext.class);
		}
		public GenericArgContext genericArg(int i) {
			return getRuleContext(GenericArgContext.class,i);
		}
		public GenericArgsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericArgs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterGenericArgs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitGenericArgs(this);
		}
	}

	public final GenericArgsContext genericArgs() throws RecognitionException {
		GenericArgsContext _localctx = new GenericArgsContext(_ctx, getState());
		enterRule(_localctx, 348, RULE_genericArgs);
		int _la;
		try {
			int _alt;
			setState(2333);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,324,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2290);
				match(LT);
				setState(2291);
				match(GT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2292);
				match(LT);
				setState(2293);
				genericArgsLifetimes();
				setState(2296);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,317,_ctx) ) {
				case 1:
					{
					setState(2294);
					match(COMMA);
					setState(2295);
					genericArgsTypes();
					}
					break;
				}
				setState(2300);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,318,_ctx) ) {
				case 1:
					{
					setState(2298);
					match(COMMA);
					setState(2299);
					genericArgsBindings();
					}
					break;
				}
				setState(2303);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(2302);
					match(COMMA);
					}
				}

				setState(2305);
				match(GT);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2307);
				match(LT);
				setState(2308);
				genericArgsTypes();
				setState(2311);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,320,_ctx) ) {
				case 1:
					{
					setState(2309);
					match(COMMA);
					setState(2310);
					genericArgsBindings();
					}
					break;
				}
				setState(2314);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(2313);
					match(COMMA);
					}
				}

				setState(2316);
				match(GT);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2318);
				match(LT);
				setState(2324);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,322,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(2319);
						genericArg();
						setState(2320);
						match(COMMA);
						}
						} 
					}
					setState(2326);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,322,_ctx);
				}
				setState(2327);
				genericArg();
				setState(2329);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(2328);
					match(COMMA);
					}
				}

				setState(2331);
				match(GT);
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
	public static class GenericArgContext extends ParserRuleContext {
		public LifetimeContext lifetime() {
			return getRuleContext(LifetimeContext.class,0);
		}
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public GenericArgsConstContext genericArgsConst() {
			return getRuleContext(GenericArgsConstContext.class,0);
		}
		public GenericArgsBindingContext genericArgsBinding() {
			return getRuleContext(GenericArgsBindingContext.class,0);
		}
		public GenericArgContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericArg; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterGenericArg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitGenericArg(this);
		}
	}

	public final GenericArgContext genericArg() throws RecognitionException {
		GenericArgContext _localctx = new GenericArgContext(_ctx, getState());
		enterRule(_localctx, 350, RULE_genericArg);
		try {
			setState(2339);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,325,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2335);
				lifetime();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2336);
				type_();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2337);
				genericArgsConst();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2338);
				genericArgsBinding();
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
	public static class GenericArgsConstContext extends ParserRuleContext {
		public BlockExpressionContext blockExpression() {
			return getRuleContext(BlockExpressionContext.class,0);
		}
		public LiteralExpressionContext literalExpression() {
			return getRuleContext(LiteralExpressionContext.class,0);
		}
		public TerminalNode MINUS() { return getToken(RustParser.MINUS, 0); }
		public SimplePathSegmentContext simplePathSegment() {
			return getRuleContext(SimplePathSegmentContext.class,0);
		}
		public GenericArgsConstContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericArgsConst; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterGenericArgsConst(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitGenericArgsConst(this);
		}
	}

	public final GenericArgsConstContext genericArgsConst() throws RecognitionException {
		GenericArgsConstContext _localctx = new GenericArgsConstContext(_ctx, getState());
		enterRule(_localctx, 352, RULE_genericArgsConst);
		int _la;
		try {
			setState(2347);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LCURLYBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(2341);
				blockExpression();
				}
				break;
			case KW_FALSE:
			case KW_TRUE:
			case CHAR_LITERAL:
			case STRING_LITERAL:
			case RAW_STRING_LITERAL:
			case BYTE_LITERAL:
			case BYTE_STRING_LITERAL:
			case RAW_BYTE_STRING_LITERAL:
			case INTEGER_LITERAL:
			case FLOAT_LITERAL:
			case MINUS:
				enterOuterAlt(_localctx, 2);
				{
				setState(2343);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(2342);
					match(MINUS);
					}
				}

				setState(2345);
				literalExpression();
				}
				break;
			case KW_CRATE:
			case KW_SELFVALUE:
			case KW_SUPER:
			case KW_MACRORULES:
			case KW_DOLLARCRATE:
			case NON_KEYWORD_IDENTIFIER:
			case RAW_IDENTIFIER:
				enterOuterAlt(_localctx, 3);
				{
				setState(2346);
				simplePathSegment();
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
	public static class GenericArgsLifetimesContext extends ParserRuleContext {
		public List<LifetimeContext> lifetime() {
			return getRuleContexts(LifetimeContext.class);
		}
		public LifetimeContext lifetime(int i) {
			return getRuleContext(LifetimeContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public GenericArgsLifetimesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericArgsLifetimes; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterGenericArgsLifetimes(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitGenericArgsLifetimes(this);
		}
	}

	public final GenericArgsLifetimesContext genericArgsLifetimes() throws RecognitionException {
		GenericArgsLifetimesContext _localctx = new GenericArgsLifetimesContext(_ctx, getState());
		enterRule(_localctx, 354, RULE_genericArgsLifetimes);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2349);
			lifetime();
			setState(2354);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,328,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2350);
					match(COMMA);
					setState(2351);
					lifetime();
					}
					} 
				}
				setState(2356);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,328,_ctx);
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
	public static class GenericArgsTypesContext extends ParserRuleContext {
		public List<Type_Context> type_() {
			return getRuleContexts(Type_Context.class);
		}
		public Type_Context type_(int i) {
			return getRuleContext(Type_Context.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public GenericArgsTypesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericArgsTypes; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterGenericArgsTypes(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitGenericArgsTypes(this);
		}
	}

	public final GenericArgsTypesContext genericArgsTypes() throws RecognitionException {
		GenericArgsTypesContext _localctx = new GenericArgsTypesContext(_ctx, getState());
		enterRule(_localctx, 356, RULE_genericArgsTypes);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2357);
			type_();
			setState(2362);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,329,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2358);
					match(COMMA);
					setState(2359);
					type_();
					}
					} 
				}
				setState(2364);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,329,_ctx);
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
	public static class GenericArgsBindingsContext extends ParserRuleContext {
		public List<GenericArgsBindingContext> genericArgsBinding() {
			return getRuleContexts(GenericArgsBindingContext.class);
		}
		public GenericArgsBindingContext genericArgsBinding(int i) {
			return getRuleContext(GenericArgsBindingContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public GenericArgsBindingsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericArgsBindings; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterGenericArgsBindings(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitGenericArgsBindings(this);
		}
	}

	public final GenericArgsBindingsContext genericArgsBindings() throws RecognitionException {
		GenericArgsBindingsContext _localctx = new GenericArgsBindingsContext(_ctx, getState());
		enterRule(_localctx, 358, RULE_genericArgsBindings);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2365);
			genericArgsBinding();
			setState(2370);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,330,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2366);
					match(COMMA);
					setState(2367);
					genericArgsBinding();
					}
					} 
				}
				setState(2372);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,330,_ctx);
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
	public static class GenericArgsBindingContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode EQ() { return getToken(RustParser.EQ, 0); }
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public GenericArgsBindingContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericArgsBinding; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterGenericArgsBinding(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitGenericArgsBinding(this);
		}
	}

	public final GenericArgsBindingContext genericArgsBinding() throws RecognitionException {
		GenericArgsBindingContext _localctx = new GenericArgsBindingContext(_ctx, getState());
		enterRule(_localctx, 360, RULE_genericArgsBinding);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2373);
			identifier();
			setState(2374);
			match(EQ);
			setState(2375);
			type_();
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
	public static class QualifiedPathInExpressionContext extends ParserRuleContext {
		public QualifiedPathTypeContext qualifiedPathType() {
			return getRuleContext(QualifiedPathTypeContext.class,0);
		}
		public List<TerminalNode> PATHSEP() { return getTokens(RustParser.PATHSEP); }
		public TerminalNode PATHSEP(int i) {
			return getToken(RustParser.PATHSEP, i);
		}
		public List<PathExprSegmentContext> pathExprSegment() {
			return getRuleContexts(PathExprSegmentContext.class);
		}
		public PathExprSegmentContext pathExprSegment(int i) {
			return getRuleContext(PathExprSegmentContext.class,i);
		}
		public QualifiedPathInExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedPathInExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterQualifiedPathInExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitQualifiedPathInExpression(this);
		}
	}

	public final QualifiedPathInExpressionContext qualifiedPathInExpression() throws RecognitionException {
		QualifiedPathInExpressionContext _localctx = new QualifiedPathInExpressionContext(_ctx, getState());
		enterRule(_localctx, 362, RULE_qualifiedPathInExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2377);
			qualifiedPathType();
			setState(2380); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(2378);
					match(PATHSEP);
					setState(2379);
					pathExprSegment();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(2382); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,331,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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
	public static class QualifiedPathTypeContext extends ParserRuleContext {
		public TerminalNode LT() { return getToken(RustParser.LT, 0); }
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public TerminalNode GT() { return getToken(RustParser.GT, 0); }
		public TerminalNode KW_AS() { return getToken(RustParser.KW_AS, 0); }
		public TypePathContext typePath() {
			return getRuleContext(TypePathContext.class,0);
		}
		public QualifiedPathTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedPathType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterQualifiedPathType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitQualifiedPathType(this);
		}
	}

	public final QualifiedPathTypeContext qualifiedPathType() throws RecognitionException {
		QualifiedPathTypeContext _localctx = new QualifiedPathTypeContext(_ctx, getState());
		enterRule(_localctx, 364, RULE_qualifiedPathType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2384);
			match(LT);
			setState(2385);
			type_();
			setState(2388);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_AS) {
				{
				setState(2386);
				match(KW_AS);
				setState(2387);
				typePath();
				}
			}

			setState(2390);
			match(GT);
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
	public static class QualifiedPathInTypeContext extends ParserRuleContext {
		public QualifiedPathTypeContext qualifiedPathType() {
			return getRuleContext(QualifiedPathTypeContext.class,0);
		}
		public List<TerminalNode> PATHSEP() { return getTokens(RustParser.PATHSEP); }
		public TerminalNode PATHSEP(int i) {
			return getToken(RustParser.PATHSEP, i);
		}
		public List<TypePathSegmentContext> typePathSegment() {
			return getRuleContexts(TypePathSegmentContext.class);
		}
		public TypePathSegmentContext typePathSegment(int i) {
			return getRuleContext(TypePathSegmentContext.class,i);
		}
		public QualifiedPathInTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedPathInType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterQualifiedPathInType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitQualifiedPathInType(this);
		}
	}

	public final QualifiedPathInTypeContext qualifiedPathInType() throws RecognitionException {
		QualifiedPathInTypeContext _localctx = new QualifiedPathInTypeContext(_ctx, getState());
		enterRule(_localctx, 366, RULE_qualifiedPathInType);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2392);
			qualifiedPathType();
			setState(2395); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(2393);
					match(PATHSEP);
					setState(2394);
					typePathSegment();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(2397); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,333,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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
	public static class TypePathContext extends ParserRuleContext {
		public List<TypePathSegmentContext> typePathSegment() {
			return getRuleContexts(TypePathSegmentContext.class);
		}
		public TypePathSegmentContext typePathSegment(int i) {
			return getRuleContext(TypePathSegmentContext.class,i);
		}
		public List<TerminalNode> PATHSEP() { return getTokens(RustParser.PATHSEP); }
		public TerminalNode PATHSEP(int i) {
			return getToken(RustParser.PATHSEP, i);
		}
		public TypePathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typePath; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTypePath(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTypePath(this);
		}
	}

	public final TypePathContext typePath() throws RecognitionException {
		TypePathContext _localctx = new TypePathContext(_ctx, getState());
		enterRule(_localctx, 368, RULE_typePath);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2400);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PATHSEP) {
				{
				setState(2399);
				match(PATHSEP);
				}
			}

			setState(2402);
			typePathSegment();
			setState(2407);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,335,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2403);
					match(PATHSEP);
					setState(2404);
					typePathSegment();
					}
					} 
				}
				setState(2409);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,335,_ctx);
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
	public static class TypePathSegmentContext extends ParserRuleContext {
		public PathIdentSegmentContext pathIdentSegment() {
			return getRuleContext(PathIdentSegmentContext.class,0);
		}
		public TerminalNode PATHSEP() { return getToken(RustParser.PATHSEP, 0); }
		public GenericArgsContext genericArgs() {
			return getRuleContext(GenericArgsContext.class,0);
		}
		public TypePathFnContext typePathFn() {
			return getRuleContext(TypePathFnContext.class,0);
		}
		public TypePathSegmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typePathSegment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTypePathSegment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTypePathSegment(this);
		}
	}

	public final TypePathSegmentContext typePathSegment() throws RecognitionException {
		TypePathSegmentContext _localctx = new TypePathSegmentContext(_ctx, getState());
		enterRule(_localctx, 370, RULE_typePathSegment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2410);
			pathIdentSegment();
			setState(2412);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,336,_ctx) ) {
			case 1:
				{
				setState(2411);
				match(PATHSEP);
				}
				break;
			}
			setState(2416);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,337,_ctx) ) {
			case 1:
				{
				setState(2414);
				genericArgs();
				}
				break;
			case 2:
				{
				setState(2415);
				typePathFn();
				}
				break;
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
	public static class TypePathFnContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(RustParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(RustParser.RPAREN, 0); }
		public TypePathInputsContext typePathInputs() {
			return getRuleContext(TypePathInputsContext.class,0);
		}
		public TerminalNode RARROW() { return getToken(RustParser.RARROW, 0); }
		public Type_Context type_() {
			return getRuleContext(Type_Context.class,0);
		}
		public TypePathFnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typePathFn; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTypePathFn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTypePathFn(this);
		}
	}

	public final TypePathFnContext typePathFn() throws RecognitionException {
		TypePathFnContext _localctx = new TypePathFnContext(_ctx, getState());
		enterRule(_localctx, 372, RULE_typePathFn);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2418);
			match(LPAREN);
			setState(2420);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 567453832540335392L) != 0 || (((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 360915832668553L) != 0) {
				{
				setState(2419);
				typePathInputs();
				}
			}

			setState(2422);
			match(RPAREN);
			setState(2425);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,339,_ctx) ) {
			case 1:
				{
				setState(2423);
				match(RARROW);
				setState(2424);
				type_();
				}
				break;
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
	public static class TypePathInputsContext extends ParserRuleContext {
		public List<Type_Context> type_() {
			return getRuleContexts(Type_Context.class);
		}
		public Type_Context type_(int i) {
			return getRuleContext(Type_Context.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(RustParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RustParser.COMMA, i);
		}
		public TypePathInputsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typePathInputs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterTypePathInputs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitTypePathInputs(this);
		}
	}

	public final TypePathInputsContext typePathInputs() throws RecognitionException {
		TypePathInputsContext _localctx = new TypePathInputsContext(_ctx, getState());
		enterRule(_localctx, 374, RULE_typePathInputs);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2427);
			type_();
			setState(2432);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,340,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2428);
					match(COMMA);
					setState(2429);
					type_();
					}
					} 
				}
				setState(2434);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,340,_ctx);
			}
			setState(2436);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(2435);
				match(COMMA);
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
	public static class VisibilityContext extends ParserRuleContext {
		public TerminalNode KW_PUB() { return getToken(RustParser.KW_PUB, 0); }
		public TerminalNode LPAREN() { return getToken(RustParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(RustParser.RPAREN, 0); }
		public TerminalNode KW_CRATE() { return getToken(RustParser.KW_CRATE, 0); }
		public TerminalNode KW_SELFVALUE() { return getToken(RustParser.KW_SELFVALUE, 0); }
		public TerminalNode KW_SUPER() { return getToken(RustParser.KW_SUPER, 0); }
		public TerminalNode KW_IN() { return getToken(RustParser.KW_IN, 0); }
		public SimplePathContext simplePath() {
			return getRuleContext(SimplePathContext.class,0);
		}
		public VisibilityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_visibility; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterVisibility(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitVisibility(this);
		}
	}

	public final VisibilityContext visibility() throws RecognitionException {
		VisibilityContext _localctx = new VisibilityContext(_ctx, getState());
		enterRule(_localctx, 376, RULE_visibility);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2438);
			match(KW_PUB);
			setState(2448);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,343,_ctx) ) {
			case 1:
				{
				setState(2439);
				match(LPAREN);
				setState(2445);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case KW_CRATE:
					{
					setState(2440);
					match(KW_CRATE);
					}
					break;
				case KW_SELFVALUE:
					{
					setState(2441);
					match(KW_SELFVALUE);
					}
					break;
				case KW_SUPER:
					{
					setState(2442);
					match(KW_SUPER);
					}
					break;
				case KW_IN:
					{
					setState(2443);
					match(KW_IN);
					setState(2444);
					simplePath();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(2447);
				match(RPAREN);
				}
				break;
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
	public static class IdentifierContext extends ParserRuleContext {
		public TerminalNode NON_KEYWORD_IDENTIFIER() { return getToken(RustParser.NON_KEYWORD_IDENTIFIER, 0); }
		public TerminalNode RAW_IDENTIFIER() { return getToken(RustParser.RAW_IDENTIFIER, 0); }
		public TerminalNode KW_MACRORULES() { return getToken(RustParser.KW_MACRORULES, 0); }
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitIdentifier(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 378, RULE_identifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2450);
			_la = _input.LA(1);
			if ( !(((_la) & ~0x3f) == 0 && ((1L << _la) & 450359962737049600L) != 0) ) {
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
	public static class KeywordContext extends ParserRuleContext {
		public TerminalNode KW_AS() { return getToken(RustParser.KW_AS, 0); }
		public TerminalNode KW_BREAK() { return getToken(RustParser.KW_BREAK, 0); }
		public TerminalNode KW_CONST() { return getToken(RustParser.KW_CONST, 0); }
		public TerminalNode KW_CONTINUE() { return getToken(RustParser.KW_CONTINUE, 0); }
		public TerminalNode KW_CRATE() { return getToken(RustParser.KW_CRATE, 0); }
		public TerminalNode KW_ELSE() { return getToken(RustParser.KW_ELSE, 0); }
		public TerminalNode KW_ENUM() { return getToken(RustParser.KW_ENUM, 0); }
		public TerminalNode KW_EXTERN() { return getToken(RustParser.KW_EXTERN, 0); }
		public TerminalNode KW_FALSE() { return getToken(RustParser.KW_FALSE, 0); }
		public TerminalNode KW_FN() { return getToken(RustParser.KW_FN, 0); }
		public TerminalNode KW_FOR() { return getToken(RustParser.KW_FOR, 0); }
		public TerminalNode KW_IF() { return getToken(RustParser.KW_IF, 0); }
		public TerminalNode KW_IMPL() { return getToken(RustParser.KW_IMPL, 0); }
		public TerminalNode KW_IN() { return getToken(RustParser.KW_IN, 0); }
		public TerminalNode KW_LET() { return getToken(RustParser.KW_LET, 0); }
		public TerminalNode KW_LOOP() { return getToken(RustParser.KW_LOOP, 0); }
		public TerminalNode KW_MATCH() { return getToken(RustParser.KW_MATCH, 0); }
		public TerminalNode KW_MOD() { return getToken(RustParser.KW_MOD, 0); }
		public TerminalNode KW_MOVE() { return getToken(RustParser.KW_MOVE, 0); }
		public TerminalNode KW_MUT() { return getToken(RustParser.KW_MUT, 0); }
		public TerminalNode KW_PUB() { return getToken(RustParser.KW_PUB, 0); }
		public TerminalNode KW_REF() { return getToken(RustParser.KW_REF, 0); }
		public TerminalNode KW_RETURN() { return getToken(RustParser.KW_RETURN, 0); }
		public TerminalNode KW_SELFVALUE() { return getToken(RustParser.KW_SELFVALUE, 0); }
		public TerminalNode KW_SELFTYPE() { return getToken(RustParser.KW_SELFTYPE, 0); }
		public TerminalNode KW_STATIC() { return getToken(RustParser.KW_STATIC, 0); }
		public TerminalNode KW_STRUCT() { return getToken(RustParser.KW_STRUCT, 0); }
		public TerminalNode KW_SUPER() { return getToken(RustParser.KW_SUPER, 0); }
		public TerminalNode KW_TRAIT() { return getToken(RustParser.KW_TRAIT, 0); }
		public TerminalNode KW_TRUE() { return getToken(RustParser.KW_TRUE, 0); }
		public TerminalNode KW_TYPE() { return getToken(RustParser.KW_TYPE, 0); }
		public TerminalNode KW_UNSAFE() { return getToken(RustParser.KW_UNSAFE, 0); }
		public TerminalNode KW_USE() { return getToken(RustParser.KW_USE, 0); }
		public TerminalNode KW_WHERE() { return getToken(RustParser.KW_WHERE, 0); }
		public TerminalNode KW_WHILE() { return getToken(RustParser.KW_WHILE, 0); }
		public TerminalNode KW_ASYNC() { return getToken(RustParser.KW_ASYNC, 0); }
		public TerminalNode KW_AWAIT() { return getToken(RustParser.KW_AWAIT, 0); }
		public TerminalNode KW_DYN() { return getToken(RustParser.KW_DYN, 0); }
		public TerminalNode KW_ABSTRACT() { return getToken(RustParser.KW_ABSTRACT, 0); }
		public TerminalNode KW_BECOME() { return getToken(RustParser.KW_BECOME, 0); }
		public TerminalNode KW_BOX() { return getToken(RustParser.KW_BOX, 0); }
		public TerminalNode KW_DO() { return getToken(RustParser.KW_DO, 0); }
		public TerminalNode KW_FINAL() { return getToken(RustParser.KW_FINAL, 0); }
		public TerminalNode KW_MACRO() { return getToken(RustParser.KW_MACRO, 0); }
		public TerminalNode KW_OVERRIDE() { return getToken(RustParser.KW_OVERRIDE, 0); }
		public TerminalNode KW_PRIV() { return getToken(RustParser.KW_PRIV, 0); }
		public TerminalNode KW_TYPEOF() { return getToken(RustParser.KW_TYPEOF, 0); }
		public TerminalNode KW_UNSIZED() { return getToken(RustParser.KW_UNSIZED, 0); }
		public TerminalNode KW_VIRTUAL() { return getToken(RustParser.KW_VIRTUAL, 0); }
		public TerminalNode KW_YIELD() { return getToken(RustParser.KW_YIELD, 0); }
		public TerminalNode KW_TRY() { return getToken(RustParser.KW_TRY, 0); }
		public TerminalNode KW_UNION() { return getToken(RustParser.KW_UNION, 0); }
		public TerminalNode KW_STATICLIFETIME() { return getToken(RustParser.KW_STATICLIFETIME, 0); }
		public KeywordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyword; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterKeyword(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitKeyword(this);
		}
	}

	public final KeywordContext keyword() throws RecognitionException {
		KeywordContext _localctx = new KeywordContext(_ctx, getState());
		enterRule(_localctx, 380, RULE_keyword);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2452);
			_la = _input.LA(1);
			if ( !(((_la) & ~0x3f) == 0 && ((1L << _la) & 18014398509481982L) != 0) ) {
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
	public static class MacroIdentifierLikeTokenContext extends ParserRuleContext {
		public KeywordContext keyword() {
			return getRuleContext(KeywordContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode KW_MACRORULES() { return getToken(RustParser.KW_MACRORULES, 0); }
		public TerminalNode KW_UNDERLINELIFETIME() { return getToken(RustParser.KW_UNDERLINELIFETIME, 0); }
		public TerminalNode KW_DOLLARCRATE() { return getToken(RustParser.KW_DOLLARCRATE, 0); }
		public TerminalNode LIFETIME_OR_LABEL() { return getToken(RustParser.LIFETIME_OR_LABEL, 0); }
		public MacroIdentifierLikeTokenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macroIdentifierLikeToken; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMacroIdentifierLikeToken(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMacroIdentifierLikeToken(this);
		}
	}

	public final MacroIdentifierLikeTokenContext macroIdentifierLikeToken() throws RecognitionException {
		MacroIdentifierLikeTokenContext _localctx = new MacroIdentifierLikeTokenContext(_ctx, getState());
		enterRule(_localctx, 382, RULE_macroIdentifierLikeToken);
		try {
			setState(2460);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,344,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2454);
				keyword();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2455);
				identifier();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2456);
				match(KW_MACRORULES);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2457);
				match(KW_UNDERLINELIFETIME);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2458);
				match(KW_DOLLARCRATE);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2459);
				match(LIFETIME_OR_LABEL);
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
	public static class MacroLiteralTokenContext extends ParserRuleContext {
		public LiteralExpressionContext literalExpression() {
			return getRuleContext(LiteralExpressionContext.class,0);
		}
		public MacroLiteralTokenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macroLiteralToken; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMacroLiteralToken(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMacroLiteralToken(this);
		}
	}

	public final MacroLiteralTokenContext macroLiteralToken() throws RecognitionException {
		MacroLiteralTokenContext _localctx = new MacroLiteralTokenContext(_ctx, getState());
		enterRule(_localctx, 384, RULE_macroLiteralToken);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2462);
			literalExpression();
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
	public static class MacroPunctuationTokenContext extends ParserRuleContext {
		public TerminalNode MINUS() { return getToken(RustParser.MINUS, 0); }
		public TerminalNode SLASH() { return getToken(RustParser.SLASH, 0); }
		public TerminalNode PERCENT() { return getToken(RustParser.PERCENT, 0); }
		public TerminalNode CARET() { return getToken(RustParser.CARET, 0); }
		public TerminalNode NOT() { return getToken(RustParser.NOT, 0); }
		public TerminalNode AND() { return getToken(RustParser.AND, 0); }
		public TerminalNode OR() { return getToken(RustParser.OR, 0); }
		public TerminalNode ANDAND() { return getToken(RustParser.ANDAND, 0); }
		public TerminalNode OROR() { return getToken(RustParser.OROR, 0); }
		public TerminalNode PLUSEQ() { return getToken(RustParser.PLUSEQ, 0); }
		public TerminalNode MINUSEQ() { return getToken(RustParser.MINUSEQ, 0); }
		public TerminalNode STAREQ() { return getToken(RustParser.STAREQ, 0); }
		public TerminalNode SLASHEQ() { return getToken(RustParser.SLASHEQ, 0); }
		public TerminalNode PERCENTEQ() { return getToken(RustParser.PERCENTEQ, 0); }
		public TerminalNode CARETEQ() { return getToken(RustParser.CARETEQ, 0); }
		public TerminalNode ANDEQ() { return getToken(RustParser.ANDEQ, 0); }
		public TerminalNode OREQ() { return getToken(RustParser.OREQ, 0); }
		public TerminalNode SHLEQ() { return getToken(RustParser.SHLEQ, 0); }
		public TerminalNode SHREQ() { return getToken(RustParser.SHREQ, 0); }
		public TerminalNode EQ() { return getToken(RustParser.EQ, 0); }
		public TerminalNode EQEQ() { return getToken(RustParser.EQEQ, 0); }
		public TerminalNode NE() { return getToken(RustParser.NE, 0); }
		public TerminalNode GT() { return getToken(RustParser.GT, 0); }
		public TerminalNode LT() { return getToken(RustParser.LT, 0); }
		public TerminalNode GE() { return getToken(RustParser.GE, 0); }
		public TerminalNode LE() { return getToken(RustParser.LE, 0); }
		public TerminalNode AT() { return getToken(RustParser.AT, 0); }
		public TerminalNode UNDERSCORE() { return getToken(RustParser.UNDERSCORE, 0); }
		public TerminalNode DOT() { return getToken(RustParser.DOT, 0); }
		public TerminalNode DOTDOT() { return getToken(RustParser.DOTDOT, 0); }
		public TerminalNode DOTDOTDOT() { return getToken(RustParser.DOTDOTDOT, 0); }
		public TerminalNode DOTDOTEQ() { return getToken(RustParser.DOTDOTEQ, 0); }
		public TerminalNode COMMA() { return getToken(RustParser.COMMA, 0); }
		public TerminalNode SEMI() { return getToken(RustParser.SEMI, 0); }
		public TerminalNode COLON() { return getToken(RustParser.COLON, 0); }
		public TerminalNode PATHSEP() { return getToken(RustParser.PATHSEP, 0); }
		public TerminalNode RARROW() { return getToken(RustParser.RARROW, 0); }
		public TerminalNode FATARROW() { return getToken(RustParser.FATARROW, 0); }
		public TerminalNode POUND() { return getToken(RustParser.POUND, 0); }
		public MacroPunctuationTokenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macroPunctuationToken; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterMacroPunctuationToken(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitMacroPunctuationToken(this);
		}
	}

	public final MacroPunctuationTokenContext macroPunctuationToken() throws RecognitionException {
		MacroPunctuationTokenContext _localctx = new MacroPunctuationTokenContext(_ctx, getState());
		enterRule(_localctx, 386, RULE_macroPunctuationToken);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2464);
			_la = _input.LA(1);
			if ( !((((_la - 81)) & ~0x3f) == 0 && ((1L << (_la - 81)) & 1099511627773L) != 0) ) {
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
	public static class ShlContext extends ParserRuleContext {
		public List<TerminalNode> LT() { return getTokens(RustParser.LT); }
		public TerminalNode LT(int i) {
			return getToken(RustParser.LT, i);
		}
		public ShlContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_shl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterShl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitShl(this);
		}
	}

	public final ShlContext shl() throws RecognitionException {
		ShlContext _localctx = new ShlContext(_ctx, getState());
		enterRule(_localctx, 388, RULE_shl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2466);
			match(LT);
			setState(2467);
			if (!(this.next('<'))) throw new FailedPredicateException(this, "this.next('<')");
			setState(2468);
			match(LT);
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
	public static class ShrContext extends ParserRuleContext {
		public List<TerminalNode> GT() { return getTokens(RustParser.GT); }
		public TerminalNode GT(int i) {
			return getToken(RustParser.GT, i);
		}
		public ShrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_shr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).enterShr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RustParserListener ) ((RustParserListener)listener).exitShr(this);
		}
	}

	public final ShrContext shr() throws RecognitionException {
		ShrContext _localctx = new ShrContext(_ctx, getState());
		enterRule(_localctx, 390, RULE_shr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2470);
			match(GT);
			setState(2471);
			if (!(this.next('>'))) throw new FailedPredicateException(this, "this.next('>')");
			setState(2472);
			match(GT);
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 77:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		case 194:
			return shl_sempred((ShlContext)_localctx, predIndex);
		case 195:
			return shr_sempred((ShrContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 26);
		case 1:
			return precpred(_ctx, 25);
		case 2:
			return precpred(_ctx, 24);
		case 3:
			return precpred(_ctx, 23);
		case 4:
			return precpred(_ctx, 22);
		case 5:
			return precpred(_ctx, 21);
		case 6:
			return precpred(_ctx, 20);
		case 7:
			return precpred(_ctx, 19);
		case 8:
			return precpred(_ctx, 18);
		case 9:
			return precpred(_ctx, 14);
		case 10:
			return precpred(_ctx, 13);
		case 11:
			return precpred(_ctx, 12);
		case 12:
			return precpred(_ctx, 37);
		case 13:
			return precpred(_ctx, 36);
		case 14:
			return precpred(_ctx, 35);
		case 15:
			return precpred(_ctx, 34);
		case 16:
			return precpred(_ctx, 33);
		case 17:
			return precpred(_ctx, 32);
		case 18:
			return precpred(_ctx, 31);
		case 19:
			return precpred(_ctx, 27);
		case 20:
			return precpred(_ctx, 17);
		}
		return true;
	}
	private boolean shl_sempred(ShlContext _localctx, int predIndex) {
		switch (predIndex) {
		case 21:
			return this.next('<');
		}
		return true;
	}
	private boolean shr_sempred(ShrContext _localctx, int predIndex) {
		switch (predIndex) {
		case 22:
			return this.next('>');
		}
		return true;
	}

	private static final String _serializedATNSegment0 =
		"\u0004\u0001\u0080\u09ab\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b"+
		"\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007"+
		"\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007"+
		"\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007"+
		"\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007"+
		"\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007"+
		"\u001b\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007"+
		"\u001e\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007"+
		"\"\u0002#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007&\u0002\'\u0007"+
		"\'\u0002(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002+\u0007+\u0002,\u0007"+
		",\u0002-\u0007-\u0002.\u0007.\u0002/\u0007/\u00020\u00070\u00021\u0007"+
		"1\u00022\u00072\u00023\u00073\u00024\u00074\u00025\u00075\u00026\u0007"+
		"6\u00027\u00077\u00028\u00078\u00029\u00079\u0002:\u0007:\u0002;\u0007"+
		";\u0002<\u0007<\u0002=\u0007=\u0002>\u0007>\u0002?\u0007?\u0002@\u0007"+
		"@\u0002A\u0007A\u0002B\u0007B\u0002C\u0007C\u0002D\u0007D\u0002E\u0007"+
		"E\u0002F\u0007F\u0002G\u0007G\u0002H\u0007H\u0002I\u0007I\u0002J\u0007"+
		"J\u0002K\u0007K\u0002L\u0007L\u0002M\u0007M\u0002N\u0007N\u0002O\u0007"+
		"O\u0002P\u0007P\u0002Q\u0007Q\u0002R\u0007R\u0002S\u0007S\u0002T\u0007"+
		"T\u0002U\u0007U\u0002V\u0007V\u0002W\u0007W\u0002X\u0007X\u0002Y\u0007"+
		"Y\u0002Z\u0007Z\u0002[\u0007[\u0002\\\u0007\\\u0002]\u0007]\u0002^\u0007"+
		"^\u0002_\u0007_\u0002`\u0007`\u0002a\u0007a\u0002b\u0007b\u0002c\u0007"+
		"c\u0002d\u0007d\u0002e\u0007e\u0002f\u0007f\u0002g\u0007g\u0002h\u0007"+
		"h\u0002i\u0007i\u0002j\u0007j\u0002k\u0007k\u0002l\u0007l\u0002m\u0007"+
		"m\u0002n\u0007n\u0002o\u0007o\u0002p\u0007p\u0002q\u0007q\u0002r\u0007"+
		"r\u0002s\u0007s\u0002t\u0007t\u0002u\u0007u\u0002v\u0007v\u0002w\u0007"+
		"w\u0002x\u0007x\u0002y\u0007y\u0002z\u0007z\u0002{\u0007{\u0002|\u0007"+
		"|\u0002}\u0007}\u0002~\u0007~\u0002\u007f\u0007\u007f\u0002\u0080\u0007"+
		"\u0080\u0002\u0081\u0007\u0081\u0002\u0082\u0007\u0082\u0002\u0083\u0007"+
		"\u0083\u0002\u0084\u0007\u0084\u0002\u0085\u0007\u0085\u0002\u0086\u0007"+
		"\u0086\u0002\u0087\u0007\u0087\u0002\u0088\u0007\u0088\u0002\u0089\u0007"+
		"\u0089\u0002\u008a\u0007\u008a\u0002\u008b\u0007\u008b\u0002\u008c\u0007"+
		"\u008c\u0002\u008d\u0007\u008d\u0002\u008e\u0007\u008e\u0002\u008f\u0007"+
		"\u008f\u0002\u0090\u0007\u0090\u0002\u0091\u0007\u0091\u0002\u0092\u0007"+
		"\u0092\u0002\u0093\u0007\u0093\u0002\u0094\u0007\u0094\u0002\u0095\u0007"+
		"\u0095\u0002\u0096\u0007\u0096\u0002\u0097\u0007\u0097\u0002\u0098\u0007"+
		"\u0098\u0002\u0099\u0007\u0099\u0002\u009a\u0007\u009a\u0002\u009b\u0007"+
		"\u009b\u0002\u009c\u0007\u009c\u0002\u009d\u0007\u009d\u0002\u009e\u0007"+
		"\u009e\u0002\u009f\u0007\u009f\u0002\u00a0\u0007\u00a0\u0002\u00a1\u0007"+
		"\u00a1\u0002\u00a2\u0007\u00a2\u0002\u00a3\u0007\u00a3\u0002\u00a4\u0007"+
		"\u00a4\u0002\u00a5\u0007\u00a5\u0002\u00a6\u0007\u00a6\u0002\u00a7\u0007"+
		"\u00a7\u0002\u00a8\u0007\u00a8\u0002\u00a9\u0007\u00a9\u0002\u00aa\u0007"+
		"\u00aa\u0002\u00ab\u0007\u00ab\u0002\u00ac\u0007\u00ac\u0002\u00ad\u0007"+
		"\u00ad\u0002\u00ae\u0007\u00ae\u0002\u00af\u0007\u00af\u0002\u00b0\u0007"+
		"\u00b0\u0002\u00b1\u0007\u00b1\u0002\u00b2\u0007\u00b2\u0002\u00b3\u0007"+
		"\u00b3\u0002\u00b4\u0007\u00b4\u0002\u00b5\u0007\u00b5\u0002\u00b6\u0007"+
		"\u00b6\u0002\u00b7\u0007\u00b7\u0002\u00b8\u0007\u00b8\u0002\u00b9\u0007"+
		"\u00b9\u0002\u00ba\u0007\u00ba\u0002\u00bb\u0007\u00bb\u0002\u00bc\u0007"+
		"\u00bc\u0002\u00bd\u0007\u00bd\u0002\u00be\u0007\u00be\u0002\u00bf\u0007"+
		"\u00bf\u0002\u00c0\u0007\u00c0\u0002\u00c1\u0007\u00c1\u0002\u00c2\u0007"+
		"\u00c2\u0002\u00c3\u0007\u00c3\u0001\u0000\u0005\u0000\u018a\b\u0000\n"+
		"\u0000\f\u0000\u018d\t\u0000\u0001\u0000\u0005\u0000\u0190\b\u0000\n\u0000"+
		"\f\u0000\u0193\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0005\u0002\u019d\b\u0002"+
		"\n\u0002\f\u0002\u01a0\t\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0005"+
		"\u0002\u01a5\b\u0002\n\u0002\f\u0002\u01a8\t\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0005\u0002\u01ad\b\u0002\n\u0002\f\u0002\u01b0\t\u0002\u0001"+
		"\u0002\u0003\u0002\u01b3\b\u0002\u0001\u0003\u0004\u0003\u01b6\b\u0003"+
		"\u000b\u0003\f\u0003\u01b7\u0001\u0003\u0003\u0003\u01bb\b\u0003\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004\u01c2"+
		"\b\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0005\u0005\u01c8"+
		"\b\u0005\n\u0005\f\u0005\u01cb\t\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0005\u0005\u01d4\b\u0005"+
		"\n\u0005\f\u0005\u01d7\t\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0005\u0005\u01e0\b\u0005\n"+
		"\u0005\f\u0005\u01e3\t\u0005\u0001\u0005\u0001\u0005\u0003\u0005\u01e7"+
		"\b\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0003\u0007\u01fc\b\u0007\u0001\b\u0001\b\u0001\b\u0005"+
		"\b\u0201\b\b\n\b\f\b\u0204\t\b\u0001\b\u0003\b\u0207\b\b\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\n\u0001\n\u0005\n\u020f\b\n\n\n\f\n\u0212\t\n"+
		"\u0001\n\u0001\n\u0001\n\u0005\n\u0217\b\n\n\n\f\n\u021a\t\n\u0001\n\u0001"+
		"\n\u0001\n\u0005\n\u021f\b\n\n\n\f\n\u0222\t\n\u0001\n\u0003\n\u0225\b"+
		"\n\u0001\u000b\u0004\u000b\u0228\b\u000b\u000b\u000b\f\u000b\u0229\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u0230\b\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0004\u000b\u0237"+
		"\b\u000b\u000b\u000b\f\u000b\u0238\u0001\u000b\u0001\u000b\u0003\u000b"+
		"\u023d\b\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u0241\b\u000b\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0003\f\u0247\b\f\u0001\r\u0001\r\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u024f\b\u000e\u0001\u000f"+
		"\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0011\u0005\u0011\u0256\b\u0011"+
		"\n\u0011\f\u0011\u0259\t\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u025d"+
		"\b\u0011\u0001\u0012\u0003\u0012\u0260\b\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0003\u0012"+
		"\u026f\b\u0012\u0001\u0013\u0001\u0013\u0003\u0013\u0273\b\u0013\u0001"+
		"\u0014\u0003\u0014\u0276\b\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001"+
		"\u0014\u0001\u0014\u0005\u0014\u027d\b\u0014\n\u0014\f\u0014\u0280\t\u0014"+
		"\u0001\u0014\u0005\u0014\u0283\b\u0014\n\u0014\f\u0014\u0286\t\u0014\u0001"+
		"\u0014\u0003\u0014\u0289\b\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0003\u0015\u028f\b\u0015\u0001\u0015\u0001\u0015\u0001\u0016\u0001"+
		"\u0016\u0003\u0016\u0295\b\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0003"+
		"\u0017\u029a\b\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0019\u0003\u0019\u02a1\b\u0019\u0001\u0019\u0003\u0019\u02a4\b\u0019"+
		"\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0005\u0019"+
		"\u02ab\b\u0019\n\u0019\f\u0019\u02ae\t\u0019\u0001\u0019\u0003\u0019\u02b1"+
		"\b\u0019\u0003\u0019\u02b3\b\u0019\u0001\u0019\u0003\u0019\u02b6\b\u0019"+
		"\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0003\u0019\u02bc\b\u0019"+
		"\u0003\u0019\u02be\b\u0019\u0003\u0019\u02c0\b\u0019\u0001\u001a\u0001"+
		"\u001a\u0001\u001a\u0001\u001a\u0003\u001a\u02c6\b\u001a\u0001\u001a\u0001"+
		"\u001a\u0003\u001a\u02ca\b\u001a\u0001\u001a\u0001\u001a\u0003\u001a\u02ce"+
		"\b\u001a\u0001\u001a\u0003\u001a\u02d1\b\u001a\u0001\u001a\u0001\u001a"+
		"\u0003\u001a\u02d5\b\u001a\u0001\u001b\u0003\u001b\u02d8\b\u001b\u0001"+
		"\u001b\u0003\u001b\u02db\b\u001b\u0001\u001b\u0003\u001b\u02de\b\u001b"+
		"\u0001\u001b\u0001\u001b\u0003\u001b\u02e2\b\u001b\u0003\u001b\u02e4\b"+
		"\u001b\u0001\u001c\u0001\u001c\u0001\u001d\u0001\u001d\u0003\u001d\u02ea"+
		"\b\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0003\u001d\u02ef\b\u001d"+
		"\u0001\u001d\u0001\u001d\u0001\u001d\u0005\u001d\u02f4\b\u001d\n\u001d"+
		"\f\u001d\u02f7\t\u001d\u0001\u001d\u0003\u001d\u02fa\b\u001d\u0003\u001d"+
		"\u02fc\b\u001d\u0001\u001e\u0005\u001e\u02ff\b\u001e\n\u001e\f\u001e\u0302"+
		"\t\u001e\u0001\u001e\u0001\u001e\u0003\u001e\u0306\b\u001e\u0001\u001f"+
		"\u0001\u001f\u0003\u001f\u030a\b\u001f\u0003\u001f\u030c\b\u001f\u0001"+
		"\u001f\u0003\u001f\u030f\b\u001f\u0001\u001f\u0001\u001f\u0001 \u0003"+
		" \u0314\b \u0001 \u0001 \u0001 \u0001 \u0001!\u0005!\u031b\b!\n!\f!\u031e"+
		"\t!\u0001!\u0001!\u0001!\u0003!\u0323\b!\u0001\"\u0001\"\u0001\"\u0001"+
		"\"\u0003\"\u0329\b\"\u0001#\u0001#\u0001#\u0001$\u0001$\u0001$\u0003$"+
		"\u0331\b$\u0001$\u0003$\u0334\b$\u0001$\u0001$\u0003$\u0338\b$\u0001$"+
		"\u0001$\u0001%\u0001%\u0003%\u033e\b%\u0001&\u0001&\u0001&\u0003&\u0343"+
		"\b&\u0001&\u0003&\u0346\b&\u0001&\u0001&\u0003&\u034a\b&\u0001&\u0001"+
		"&\u0003&\u034e\b&\u0001\'\u0001\'\u0001\'\u0003\'\u0353\b\'\u0001\'\u0001"+
		"\'\u0003\'\u0357\b\'\u0001\'\u0001\'\u0003\'\u035b\b\'\u0001\'\u0001\'"+
		"\u0001(\u0001(\u0001(\u0005(\u0362\b(\n(\f(\u0365\t(\u0001(\u0003(\u0368"+
		"\b(\u0001)\u0005)\u036b\b)\n)\f)\u036e\t)\u0001)\u0003)\u0371\b)\u0001"+
		")\u0001)\u0001)\u0001)\u0001*\u0001*\u0001*\u0005*\u037a\b*\n*\f*\u037d"+
		"\t*\u0001*\u0003*\u0380\b*\u0001+\u0005+\u0383\b+\n+\f+\u0386\t+\u0001"+
		"+\u0003+\u0389\b+\u0001+\u0001+\u0001,\u0001,\u0001,\u0003,\u0390\b,\u0001"+
		",\u0003,\u0393\b,\u0001,\u0001,\u0003,\u0397\b,\u0001,\u0001,\u0001-\u0001"+
		"-\u0001-\u0005-\u039e\b-\n-\f-\u03a1\t-\u0001-\u0003-\u03a4\b-\u0001."+
		"\u0005.\u03a7\b.\n.\f.\u03aa\t.\u0001.\u0003.\u03ad\b.\u0001.\u0001.\u0001"+
		".\u0001.\u0003.\u03b3\b.\u0001/\u0001/\u0003/\u03b7\b/\u0001/\u0001/\u0001"+
		"0\u00010\u00030\u03bd\b0\u00010\u00010\u00011\u00011\u00011\u00012\u0001"+
		"2\u00012\u00032\u03c7\b2\u00012\u00032\u03ca\b2\u00012\u00012\u00012\u0001"+
		"2\u00013\u00013\u00013\u00033\u03d3\b3\u00013\u00013\u00013\u00013\u0003"+
		"3\u03d9\b3\u00013\u00013\u00014\u00014\u00034\u03df\b4\u00014\u00014\u0001"+
		"4\u00014\u00014\u00034\u03e6\b4\u00014\u00014\u00015\u00035\u03eb\b5\u0001"+
		"5\u00015\u00015\u00035\u03f0\b5\u00015\u00015\u00035\u03f4\b5\u00035\u03f6"+
		"\b5\u00015\u00035\u03f9\b5\u00015\u00015\u00055\u03fd\b5\n5\f5\u0400\t"+
		"5\u00015\u00055\u0403\b5\n5\f5\u0406\t5\u00015\u00015\u00016\u00016\u0003"+
		"6\u040c\b6\u00017\u00017\u00037\u0410\b7\u00017\u00017\u00037\u0414\b"+
		"7\u00017\u00017\u00057\u0418\b7\n7\f7\u041b\t7\u00017\u00057\u041e\b7"+
		"\n7\f7\u0421\t7\u00017\u00017\u00018\u00038\u0426\b8\u00018\u00018\u0003"+
		"8\u042a\b8\u00018\u00038\u042d\b8\u00018\u00018\u00018\u00018\u00038\u0433"+
		"\b8\u00018\u00018\u00058\u0437\b8\n8\f8\u043a\t8\u00018\u00058\u043d\b"+
		"8\n8\f8\u0440\t8\u00018\u00018\u00019\u00039\u0445\b9\u00019\u00019\u0003"+
		"9\u0449\b9\u00019\u00019\u00059\u044d\b9\n9\f9\u0450\t9\u00019\u00059"+
		"\u0453\b9\n9\f9\u0456\t9\u00019\u00019\u0001:\u0005:\u045b\b:\n:\f:\u045e"+
		"\t:\u0001:\u0001:\u0003:\u0462\b:\u0001:\u0001:\u0003:\u0466\b:\u0003"+
		":\u0468\b:\u0001;\u0001;\u0001;\u0001;\u0005;\u046e\b;\n;\f;\u0471\t;"+
		"\u0001;\u0001;\u0003;\u0475\b;\u0003;\u0477\b;\u0001;\u0001;\u0001<\u0005"+
		"<\u047c\b<\n<\f<\u047f\t<\u0001<\u0001<\u0001<\u0003<\u0484\b<\u0001="+
		"\u0003=\u0487\b=\u0001=\u0001=\u0001=\u0003=\u048c\b=\u0001>\u0003>\u048f"+
		"\b>\u0001>\u0001>\u0001>\u0003>\u0494\b>\u0003>\u0496\b>\u0001>\u0001"+
		">\u0003>\u049a\b>\u0001?\u0001?\u0001?\u0001?\u0001?\u0001@\u0001@\u0001"+
		"@\u0001@\u0005@\u04a5\b@\n@\f@\u04a8\t@\u0001@\u0003@\u04ab\b@\u0001A"+
		"\u0001A\u0003A\u04af\bA\u0001B\u0001B\u0001B\u0001B\u0001C\u0003C\u04b6"+
		"\bC\u0001C\u0001C\u0001C\u0003C\u04bb\bC\u0001D\u0001D\u0001D\u0001E\u0005"+
		"E\u04c1\bE\nE\fE\u04c4\tE\u0001E\u0001E\u0003E\u04c8\bE\u0001E\u0001E"+
		"\u0001E\u0003E\u04cd\bE\u0003E\u04cf\bE\u0001F\u0001F\u0001F\u0001F\u0001"+
		"F\u0001F\u0001G\u0001G\u0001G\u0001G\u0001G\u0001H\u0001H\u0003H\u04de"+
		"\bH\u0001I\u0001I\u0001I\u0003I\u04e3\bI\u0001J\u0001J\u0001J\u0001J\u0001"+
		"J\u0003J\u04ea\bJ\u0001K\u0005K\u04ed\bK\nK\fK\u04f0\tK\u0001K\u0001K"+
		"\u0001K\u0001K\u0003K\u04f6\bK\u0001K\u0001K\u0003K\u04fa\bK\u0001K\u0001"+
		"K\u0001L\u0001L\u0001L\u0001L\u0001L\u0003L\u0503\bL\u0003L\u0505\bL\u0001"+
		"M\u0001M\u0004M\u0509\bM\u000bM\fM\u050a\u0001M\u0001M\u0001M\u0001M\u0001"+
		"M\u0001M\u0003M\u0513\bM\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001"+
		"M\u0003M\u051c\bM\u0001M\u0001M\u0001M\u0001M\u0003M\u0522\bM\u0001M\u0003"+
		"M\u0525\bM\u0001M\u0001M\u0003M\u0529\bM\u0001M\u0003M\u052c\bM\u0001"+
		"M\u0001M\u0003M\u0530\bM\u0001M\u0001M\u0005M\u0534\bM\nM\fM\u0537\tM"+
		"\u0001M\u0001M\u0001M\u0001M\u0001M\u0005M\u053e\bM\nM\fM\u0541\tM\u0001"+
		"M\u0003M\u0544\bM\u0001M\u0001M\u0001M\u0005M\u0549\bM\nM\fM\u054c\tM"+
		"\u0001M\u0003M\u054f\bM\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0003"+
		"M\u0557\bM\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001"+
		"M\u0003M\u0562\bM\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001"+
		"M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001"+
		"M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001"+
		"M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0003M\u0588"+
		"\bM\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001"+
		"M\u0001M\u0001M\u0001M\u0001M\u0003M\u0598\bM\u0001M\u0001M\u0001M\u0001"+
		"M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001"+
		"M\u0003M\u05a8\bM\u0005M\u05aa\bM\nM\fM\u05ad\tM\u0001N\u0001N\u0001O"+
		"\u0001O\u0001P\u0004P\u05b4\bP\u000bP\fP\u05b5\u0001P\u0001P\u0001P\u0001"+
		"P\u0001P\u0001P\u0001P\u0001P\u0001P\u0003P\u05c1\bP\u0001Q\u0001Q\u0001"+
		"R\u0001R\u0003R\u05c7\bR\u0001S\u0001S\u0005S\u05cb\bS\nS\fS\u05ce\tS"+
		"\u0001S\u0003S\u05d1\bS\u0001S\u0001S\u0001T\u0004T\u05d6\bT\u000bT\f"+
		"T\u05d7\u0001T\u0003T\u05db\bT\u0001T\u0003T\u05de\bT\u0001U\u0001U\u0003"+
		"U\u05e2\bU\u0001U\u0001U\u0001V\u0001V\u0001V\u0001W\u0001W\u0001W\u0005"+
		"W\u05ec\bW\nW\fW\u05ef\tW\u0001W\u0003W\u05f2\bW\u0001W\u0001W\u0001W"+
		"\u0001W\u0003W\u05f8\bW\u0001X\u0001X\u0001X\u0004X\u05fd\bX\u000bX\f"+
		"X\u05fe\u0001X\u0003X\u0602\bX\u0001Y\u0001Y\u0001Z\u0001Z\u0001Z\u0003"+
		"Z\u0609\bZ\u0001[\u0001[\u0001[\u0005[\u060e\b[\n[\f[\u0611\t[\u0001["+
		"\u0001[\u0003[\u0615\b[\u0001[\u0001[\u0001\\\u0001\\\u0001\\\u0005\\"+
		"\u061c\b\\\n\\\f\\\u061f\t\\\u0001\\\u0001\\\u0001\\\u0003\\\u0624\b\\"+
		"\u0003\\\u0626\b\\\u0001]\u0005]\u0629\b]\n]\f]\u062c\t]\u0001]\u0001"+
		"]\u0001]\u0003]\u0631\b]\u0001]\u0001]\u0001]\u0003]\u0636\b]\u0001^\u0001"+
		"^\u0001^\u0001_\u0001_\u0001_\u0005_\u063e\b_\n_\f_\u0641\t_\u0001_\u0001"+
		"_\u0001_\u0005_\u0646\b_\n_\f_\u0649\t_\u0001_\u0003_\u064c\b_\u0003_"+
		"\u064e\b_\u0001_\u0001_\u0001`\u0001`\u0001a\u0001a\u0001a\u0003a\u0657"+
		"\ba\u0001b\u0001b\u0001b\u0003b\u065c\bb\u0001b\u0001b\u0001c\u0001c\u0001"+
		"c\u0005c\u0663\bc\nc\fc\u0666\tc\u0001c\u0003c\u0669\bc\u0001d\u0001d"+
		"\u0001d\u0003d\u066e\bd\u0001d\u0001d\u0001d\u0003d\u0673\bd\u0001e\u0001"+
		"e\u0001e\u0001e\u0001e\u0005e\u067a\be\ne\fe\u067d\te\u0001e\u0003e\u0680"+
		"\be\u0003e\u0682\be\u0001e\u0001e\u0001f\u0001f\u0001g\u0001g\u0001g\u0005"+
		"g\u068b\bg\ng\fg\u068e\tg\u0001g\u0003g\u0691\bg\u0001h\u0003h\u0694\b"+
		"h\u0001h\u0001h\u0001h\u0003h\u0699\bh\u0001h\u0003h\u069c\bh\u0001h\u0001"+
		"h\u0001h\u0001h\u0001h\u0003h\u06a3\bh\u0001i\u0001i\u0001i\u0005i\u06a8"+
		"\bi\ni\fi\u06ab\ti\u0001i\u0003i\u06ae\bi\u0001j\u0005j\u06b1\bj\nj\f"+
		"j\u06b4\tj\u0001j\u0001j\u0001j\u0003j\u06b9\bj\u0001k\u0003k\u06bc\b"+
		"k\u0001k\u0001k\u0001k\u0001k\u0003k\u06c2\bk\u0001l\u0001l\u0001l\u0001"+
		"m\u0001m\u0001m\u0001m\u0001n\u0001n\u0001n\u0001n\u0001n\u0001n\u0001"+
		"n\u0001o\u0001o\u0001o\u0001o\u0001o\u0001o\u0001p\u0001p\u0001p\u0001"+
		"q\u0001q\u0001q\u0001q\u0001q\u0001q\u0001q\u0003q\u06e2\bq\u0003q\u06e4"+
		"\bq\u0001r\u0001r\u0001r\u0001r\u0001r\u0001r\u0001r\u0001r\u0001r\u0001"+
		"r\u0003r\u06f0\br\u0003r\u06f2\br\u0001s\u0001s\u0001s\u0001s\u0005s\u06f8"+
		"\bs\ns\fs\u06fb\ts\u0001s\u0003s\u06fe\bs\u0001s\u0001s\u0001t\u0001t"+
		"\u0001t\u0001t\u0005t\u0706\bt\nt\ft\u0709\tt\u0001t\u0001t\u0001t\u0001"+
		"t\u0003t\u070f\bt\u0001u\u0001u\u0001u\u0001u\u0001u\u0003u\u0716\bu\u0003"+
		"u\u0718\bu\u0001v\u0005v\u071b\bv\nv\fv\u071e\tv\u0001v\u0001v\u0003v"+
		"\u0722\bv\u0001w\u0001w\u0001w\u0001x\u0003x\u0728\bx\u0001x\u0001x\u0001"+
		"x\u0005x\u072d\bx\nx\fx\u0730\tx\u0001y\u0001y\u0003y\u0734\by\u0001z"+
		"\u0001z\u0001z\u0001z\u0001z\u0001z\u0001z\u0001z\u0001z\u0001z\u0001"+
		"z\u0001z\u0003z\u0742\bz\u0001{\u0001{\u0001{\u0001{\u0001{\u0001{\u0001"+
		"{\u0001{\u0001{\u0003{\u074d\b{\u0001{\u0001{\u0003{\u0751\b{\u0001{\u0003"+
		"{\u0754\b{\u0001|\u0003|\u0757\b|\u0001|\u0003|\u075a\b|\u0001|\u0001"+
		"|\u0001|\u0003|\u075f\b|\u0001}\u0001}\u0001~\u0001~\u0001\u007f\u0001"+
		"\u007f\u0001\u007f\u0001\u007f\u0001\u007f\u0001\u007f\u0001\u007f\u0001"+
		"\u007f\u0001\u007f\u0001\u007f\u0001\u007f\u0003\u007f\u0770\b\u007f\u0001"+
		"\u0080\u0001\u0080\u0001\u0080\u0003\u0080\u0775\b\u0080\u0001\u0080\u0001"+
		"\u0080\u0003\u0080\u0779\b\u0080\u0001\u0080\u0001\u0080\u0003\u0080\u077d"+
		"\b\u0080\u0001\u0081\u0001\u0081\u0003\u0081\u0781\b\u0081\u0001\u0081"+
		"\u0001\u0081\u0001\u0082\u0001\u0082\u0001\u0082\u0003\u0082\u0788\b\u0082"+
		"\u0001\u0082\u0001\u0082\u0001\u0083\u0001\u0083\u0001\u0083\u0003\u0083"+
		"\u078f\b\u0083\u0003\u0083\u0791\b\u0083\u0001\u0083\u0003\u0083\u0794"+
		"\b\u0083\u0001\u0084\u0001\u0084\u0001\u0084\u0005\u0084\u0799\b\u0084"+
		"\n\u0084\f\u0084\u079c\t\u0084\u0001\u0085\u0005\u0085\u079f\b\u0085\n"+
		"\u0085\f\u0085\u07a2\t\u0085\u0001\u0085\u0001\u0085\u0001\u0085\u0001"+
		"\u0085\u0001\u0085\u0001\u0085\u0001\u0085\u0001\u0085\u0001\u0085\u0003"+
		"\u0085\u07ad\b\u0085\u0001\u0085\u0003\u0085\u07b0\b\u0085\u0001\u0085"+
		"\u0003\u0085\u07b3\b\u0085\u0001\u0086\u0005\u0086\u07b6\b\u0086\n\u0086"+
		"\f\u0086\u07b9\t\u0086\u0001\u0086\u0001\u0086\u0001\u0087\u0001\u0087"+
		"\u0001\u0087\u0003\u0087\u07c0\b\u0087\u0001\u0087\u0001\u0087\u0001\u0088"+
		"\u0001\u0088\u0001\u0088\u0005\u0088\u07c7\b\u0088\n\u0088\f\u0088\u07ca"+
		"\t\u0088\u0001\u0088\u0003\u0088\u07cd\b\u0088\u0001\u0089\u0001\u0089"+
		"\u0003\u0089\u07d1\b\u0089\u0001\u0089\u0001\u0089\u0001\u008a\u0001\u008a"+
		"\u0001\u008a\u0001\u008a\u0001\u008a\u0001\u008a\u0001\u008a\u0004\u008a"+
		"\u07dc\b\u008a\u000b\u008a\f\u008a\u07dd\u0001\u008a\u0003\u008a\u07e1"+
		"\b\u008a\u0003\u008a\u07e3\b\u008a\u0001\u008b\u0001\u008b\u0001\u008b"+
		"\u0001\u008b\u0001\u008c\u0001\u008c\u0003\u008c\u07eb\b\u008c\u0001\u008c"+
		"\u0001\u008c\u0001\u008d\u0001\u008d\u0001\u008d\u0005\u008d\u07f2\b\u008d"+
		"\n\u008d\f\u008d\u07f5\t\u008d\u0001\u008d\u0003\u008d\u07f8\b\u008d\u0001"+
		"\u008e\u0001\u008e\u0003\u008e\u07fc\b\u008e\u0001\u008f\u0001\u008f\u0001"+
		"\u008f\u0003\u008f\u0801\b\u008f\u0001\u0090\u0001\u0090\u0001\u0090\u0001"+
		"\u0090\u0001\u0090\u0001\u0090\u0001\u0090\u0001\u0090\u0001\u0090\u0001"+
		"\u0090\u0001\u0090\u0001\u0090\u0001\u0090\u0001\u0090\u0003\u0090\u0811"+
		"\b\u0090\u0001\u0091\u0001\u0091\u0001\u0091\u0001\u0091\u0001\u0092\u0001"+
		"\u0092\u0001\u0093\u0001\u0093\u0001\u0093\u0001\u0093\u0004\u0093\u081d"+
		"\b\u0093\u000b\u0093\f\u0093\u081e\u0001\u0093\u0003\u0093\u0822\b\u0093"+
		"\u0003\u0093\u0824\b\u0093\u0001\u0093\u0001\u0093\u0001\u0094\u0001\u0094"+
		"\u0001\u0094\u0001\u0094\u0001\u0094\u0001\u0094\u0001\u0095\u0001\u0095"+
		"\u0001\u0095\u0001\u0095\u0001\u0096\u0001\u0096\u0003\u0096\u0834\b\u0096"+
		"\u0001\u0096\u0003\u0096\u0837\b\u0096\u0001\u0096\u0001\u0096\u0001\u0097"+
		"\u0001\u0097\u0001\u0097\u0001\u0097\u0001\u0098\u0003\u0098\u0840\b\u0098"+
		"\u0001\u0098\u0001\u0098\u0001\u0098\u0001\u0098\u0003\u0098\u0846\b\u0098"+
		"\u0001\u0098\u0001\u0098\u0003\u0098\u084a\b\u0098\u0001\u0099\u0003\u0099"+
		"\u084d\b\u0099\u0001\u0099\u0001\u0099\u0003\u0099\u0851\b\u0099\u0003"+
		"\u0099\u0853\b\u0099\u0001\u009a\u0001\u009a\u0001\u009a\u0001\u009b\u0001"+
		"\u009b\u0003\u009b\u085a\b\u009b\u0001\u009c\u0001\u009c\u0001\u009c\u0005"+
		"\u009c\u085f\b\u009c\n\u009c\f\u009c\u0862\t\u009c\u0001\u009c\u0003\u009c"+
		"\u0865\b\u009c\u0001\u009d\u0005\u009d\u0868\b\u009d\n\u009d\f\u009d\u086b"+
		"\t\u009d\u0001\u009d\u0001\u009d\u0003\u009d\u086f\b\u009d\u0001\u009d"+
		"\u0003\u009d\u0872\b\u009d\u0001\u009d\u0001\u009d\u0001\u009e\u0001\u009e"+
		"\u0001\u009e\u0005\u009e\u0879\b\u009e\n\u009e\f\u009e\u087c\t\u009e\u0001"+
		"\u009e\u0001\u009e\u0001\u009e\u0005\u009e\u0881\b\u009e\n\u009e\f\u009e"+
		"\u0884\t\u009e\u0001\u009e\u0001\u009e\u0001\u009f\u0003\u009f\u0889\b"+
		"\u009f\u0001\u009f\u0001\u009f\u0001\u00a0\u0003\u00a0\u088e\b\u00a0\u0001"+
		"\u00a0\u0001\u00a0\u0001\u00a1\u0001\u00a1\u0001\u00a1\u0001\u00a2\u0001"+
		"\u00a2\u0001\u00a2\u0001\u00a3\u0001\u00a3\u0001\u00a4\u0001\u00a4\u0001"+
		"\u00a4\u0005\u00a4\u089d\b\u00a4\n\u00a4\f\u00a4\u08a0\t\u00a4\u0001\u00a4"+
		"\u0003\u00a4\u08a3\b\u00a4\u0001\u00a5\u0001\u00a5\u0003\u00a5\u08a7\b"+
		"\u00a5\u0001\u00a6\u0003\u00a6\u08aa\b\u00a6\u0001\u00a6\u0003\u00a6\u08ad"+
		"\b\u00a6\u0001\u00a6\u0001\u00a6\u0001\u00a6\u0003\u00a6\u08b2\b\u00a6"+
		"\u0001\u00a6\u0003\u00a6\u08b5\b\u00a6\u0001\u00a6\u0001\u00a6\u0001\u00a6"+
		"\u0003\u00a6\u08ba\b\u00a6\u0001\u00a7\u0001\u00a7\u0001\u00a7\u0005\u00a7"+
		"\u08bf\b\u00a7\n\u00a7\f\u00a7\u08c2\t\u00a7\u0001\u00a7\u0003\u00a7\u08c5"+
		"\b\u00a7\u0001\u00a8\u0001\u00a8\u0001\u00a9\u0003\u00a9\u08ca\b\u00a9"+
		"\u0001\u00a9\u0001\u00a9\u0001\u00a9\u0005\u00a9\u08cf\b\u00a9\n\u00a9"+
		"\f\u00a9\u08d2\t\u00a9\u0001\u00aa\u0001\u00aa\u0001\u00aa\u0001\u00aa"+
		"\u0001\u00aa\u0003\u00aa\u08d9\b\u00aa\u0001\u00ab\u0003\u00ab\u08dc\b"+
		"\u00ab\u0001\u00ab\u0001\u00ab\u0001\u00ab\u0005\u00ab\u08e1\b\u00ab\n"+
		"\u00ab\f\u00ab\u08e4\t\u00ab\u0001\u00ac\u0001\u00ac\u0001\u00ac\u0003"+
		"\u00ac\u08e9\b\u00ac\u0001\u00ad\u0001\u00ad\u0001\u00ad\u0001\u00ad\u0001"+
		"\u00ad\u0001\u00ad\u0003\u00ad\u08f1\b\u00ad\u0001\u00ae\u0001\u00ae\u0001"+
		"\u00ae\u0001\u00ae\u0001\u00ae\u0001\u00ae\u0003\u00ae\u08f9\b\u00ae\u0001"+
		"\u00ae\u0001\u00ae\u0003\u00ae\u08fd\b\u00ae\u0001\u00ae\u0003\u00ae\u0900"+
		"\b\u00ae\u0001\u00ae\u0001\u00ae\u0001\u00ae\u0001\u00ae\u0001\u00ae\u0001"+
		"\u00ae\u0003\u00ae\u0908\b\u00ae\u0001\u00ae\u0003\u00ae\u090b\b\u00ae"+
		"\u0001\u00ae\u0001\u00ae\u0001\u00ae\u0001\u00ae\u0001\u00ae\u0001\u00ae"+
		"\u0005\u00ae\u0913\b\u00ae\n\u00ae\f\u00ae\u0916\t\u00ae\u0001\u00ae\u0001"+
		"\u00ae\u0003\u00ae\u091a\b\u00ae\u0001\u00ae\u0001\u00ae\u0003\u00ae\u091e"+
		"\b\u00ae\u0001\u00af\u0001\u00af\u0001\u00af\u0001\u00af\u0003\u00af\u0924"+
		"\b\u00af\u0001\u00b0\u0001\u00b0\u0003\u00b0\u0928\b\u00b0\u0001\u00b0"+
		"\u0001\u00b0\u0003\u00b0\u092c\b\u00b0\u0001\u00b1\u0001\u00b1\u0001\u00b1"+
		"\u0005\u00b1\u0931\b\u00b1\n\u00b1\f\u00b1\u0934\t\u00b1\u0001\u00b2\u0001"+
		"\u00b2\u0001\u00b2\u0005\u00b2\u0939\b\u00b2\n\u00b2\f\u00b2\u093c\t\u00b2"+
		"\u0001\u00b3\u0001\u00b3\u0001\u00b3\u0005\u00b3\u0941\b\u00b3\n\u00b3"+
		"\f\u00b3\u0944\t\u00b3\u0001\u00b4\u0001\u00b4\u0001\u00b4\u0001\u00b4"+
		"\u0001\u00b5\u0001\u00b5\u0001\u00b5\u0004\u00b5\u094d\b\u00b5\u000b\u00b5"+
		"\f\u00b5\u094e\u0001\u00b6\u0001\u00b6\u0001\u00b6\u0001\u00b6\u0003\u00b6"+
		"\u0955\b\u00b6\u0001\u00b6\u0001\u00b6\u0001\u00b7\u0001\u00b7\u0001\u00b7"+
		"\u0004\u00b7\u095c\b\u00b7\u000b\u00b7\f\u00b7\u095d\u0001\u00b8\u0003"+
		"\u00b8\u0961\b\u00b8\u0001\u00b8\u0001\u00b8\u0001\u00b8\u0005\u00b8\u0966"+
		"\b\u00b8\n\u00b8\f\u00b8\u0969\t\u00b8\u0001\u00b9\u0001\u00b9\u0003\u00b9"+
		"\u096d\b\u00b9\u0001\u00b9\u0001\u00b9\u0003\u00b9\u0971\b\u00b9\u0001"+
		"\u00ba\u0001\u00ba\u0003\u00ba\u0975\b\u00ba\u0001\u00ba\u0001\u00ba\u0001"+
		"\u00ba\u0003\u00ba\u097a\b\u00ba\u0001\u00bb\u0001\u00bb\u0001\u00bb\u0005"+
		"\u00bb\u097f\b\u00bb\n\u00bb\f\u00bb\u0982\t\u00bb\u0001\u00bb\u0003\u00bb"+
		"\u0985\b\u00bb\u0001\u00bc\u0001\u00bc\u0001\u00bc\u0001\u00bc\u0001\u00bc"+
		"\u0001\u00bc\u0001\u00bc\u0003\u00bc\u098e\b\u00bc\u0001\u00bc\u0003\u00bc"+
		"\u0991\b\u00bc\u0001\u00bd\u0001\u00bd\u0001\u00be\u0001\u00be\u0001\u00bf"+
		"\u0001\u00bf\u0001\u00bf\u0001\u00bf\u0001\u00bf\u0001\u00bf\u0003\u00bf"+
		"\u099d\b\u00bf\u0001\u00c0\u0001\u00c0\u0001\u00c1\u0001\u00c1\u0001\u00c2"+
		"\u0001\u00c2\u0001\u00c2\u0001\u00c2\u0001\u00c3\u0001\u00c3\u0001\u00c3"+
		"\u0001\u00c3\u0001\u00c3\u0000\u0001\u009a\u00c4\u0000\u0002\u0004\u0006"+
		"\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,."+
		"02468:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088"+
		"\u008a\u008c\u008e\u0090\u0092\u0094\u0096\u0098\u009a\u009c\u009e\u00a0"+
		"\u00a2\u00a4\u00a6\u00a8\u00aa\u00ac\u00ae\u00b0\u00b2\u00b4\u00b6\u00b8"+
		"\u00ba\u00bc\u00be\u00c0\u00c2\u00c4\u00c6\u00c8\u00ca\u00cc\u00ce\u00d0"+
		"\u00d2\u00d4\u00d6\u00d8\u00da\u00dc\u00de\u00e0\u00e2\u00e4\u00e6\u00e8"+
		"\u00ea\u00ec\u00ee\u00f0\u00f2\u00f4\u00f6\u00f8\u00fa\u00fc\u00fe\u0100"+
		"\u0102\u0104\u0106\u0108\u010a\u010c\u010e\u0110\u0112\u0114\u0116\u0118"+
		"\u011a\u011c\u011e\u0120\u0122\u0124\u0126\u0128\u012a\u012c\u012e\u0130"+
		"\u0132\u0134\u0136\u0138\u013a\u013c\u013e\u0140\u0142\u0144\u0146\u0148"+
		"\u014a\u014c\u014e\u0150\u0152\u0154\u0156\u0158\u015a\u015c\u015e\u0160"+
		"\u0162\u0164\u0166\u0168\u016a\u016c\u016e\u0170\u0172\u0174\u0176\u0178"+
		"\u017a\u017c\u017e\u0180\u0182\u0184\u0186\u0000\u000e\u0003\u0000PPR"+
		"Rzz\u0001\u0000DE\u0002\u0000WWYY\u0002\u0000QQVV\u0001\u0000RT\u0001"+
		"\u0000PQ\u0001\u0000fk\u0001\u0000[d\u0004\u0000\t\t\u001e\u001eCINN\u0002"+
		"\u0000\u0003\u0003\u0014\u0014\u0003\u00005577OO\u0002\u0000669:\u0001"+
		"\u0000\u00015\u0002\u0000QQSx\u0ac8\u0000\u018b\u0001\u0000\u0000\u0000"+
		"\u0002\u0196\u0001\u0000\u0000\u0000\u0004\u01b2\u0001\u0000\u0000\u0000"+
		"\u0006\u01ba\u0001\u0000\u0000\u0000\b\u01c1\u0001\u0000\u0000\u0000\n"+
		"\u01e6\u0001\u0000\u0000\u0000\f\u01e8\u0001\u0000\u0000\u0000\u000e\u01fb"+
		"\u0001\u0000\u0000\u0000\u0010\u01fd\u0001\u0000\u0000\u0000\u0012\u0208"+
		"\u0001\u0000\u0000\u0000\u0014\u0224\u0001\u0000\u0000\u0000\u0016\u0240"+
		"\u0001\u0000\u0000\u0000\u0018\u0246\u0001\u0000\u0000\u0000\u001a\u0248"+
		"\u0001\u0000\u0000\u0000\u001c\u024e\u0001\u0000\u0000\u0000\u001e\u0250"+
		"\u0001\u0000\u0000\u0000 \u0252\u0001\u0000\u0000\u0000\"\u0257\u0001"+
		"\u0000\u0000\u0000$\u025f\u0001\u0000\u0000\u0000&\u0272\u0001\u0000\u0000"+
		"\u0000(\u0275\u0001\u0000\u0000\u0000*\u028a\u0001\u0000\u0000\u0000,"+
		"\u0294\u0001\u0000\u0000\u0000.\u0296\u0001\u0000\u0000\u00000\u029b\u0001"+
		"\u0000\u0000\u00002\u02bf\u0001\u0000\u0000\u00004\u02c1\u0001\u0000\u0000"+
		"\u00006\u02d7\u0001\u0000\u0000\u00008\u02e5\u0001\u0000\u0000\u0000:"+
		"\u02fb\u0001\u0000\u0000\u0000<\u0300\u0001\u0000\u0000\u0000>\u030b\u0001"+
		"\u0000\u0000\u0000@\u0313\u0001\u0000\u0000\u0000B\u031c\u0001\u0000\u0000"+
		"\u0000D\u0324\u0001\u0000\u0000\u0000F\u032a\u0001\u0000\u0000\u0000H"+
		"\u032d\u0001\u0000\u0000\u0000J\u033d\u0001\u0000\u0000\u0000L\u033f\u0001"+
		"\u0000\u0000\u0000N\u034f\u0001\u0000\u0000\u0000P\u035e\u0001\u0000\u0000"+
		"\u0000R\u036c\u0001\u0000\u0000\u0000T\u0376\u0001\u0000\u0000\u0000V"+
		"\u0384\u0001\u0000\u0000\u0000X\u038c\u0001\u0000\u0000\u0000Z\u039a\u0001"+
		"\u0000\u0000\u0000\\\u03a8\u0001\u0000\u0000\u0000^\u03b4\u0001\u0000"+
		"\u0000\u0000`\u03ba\u0001\u0000\u0000\u0000b\u03c0\u0001\u0000\u0000\u0000"+
		"d\u03c3\u0001\u0000\u0000\u0000f\u03cf\u0001\u0000\u0000\u0000h\u03dc"+
		"\u0001\u0000\u0000\u0000j\u03ea\u0001\u0000\u0000\u0000l\u040b\u0001\u0000"+
		"\u0000\u0000n\u040d\u0001\u0000\u0000\u0000p\u0425\u0001\u0000\u0000\u0000"+
		"r\u0444\u0001\u0000\u0000\u0000t\u045c\u0001\u0000\u0000\u0000v\u0469"+
		"\u0001\u0000\u0000\u0000x\u047d\u0001\u0000\u0000\u0000z\u0486\u0001\u0000"+
		"\u0000\u0000|\u048e\u0001\u0000\u0000\u0000~\u049b\u0001\u0000\u0000\u0000"+
		"\u0080\u04a0\u0001\u0000\u0000\u0000\u0082\u04ae\u0001\u0000\u0000\u0000"+
		"\u0084\u04b0\u0001\u0000\u0000\u0000\u0086\u04b5\u0001\u0000\u0000\u0000"+
		"\u0088\u04bc\u0001\u0000\u0000\u0000\u008a\u04c2\u0001\u0000\u0000\u0000"+
		"\u008c\u04d0\u0001\u0000\u0000\u0000\u008e\u04d6\u0001\u0000\u0000\u0000"+
		"\u0090\u04db\u0001\u0000\u0000\u0000\u0092\u04e2\u0001\u0000\u0000\u0000"+
		"\u0094\u04e9\u0001\u0000\u0000\u0000\u0096\u04ee\u0001\u0000\u0000\u0000"+
		"\u0098\u0504\u0001\u0000\u0000\u0000\u009a\u0556\u0001\u0000\u0000\u0000"+
		"\u009c\u05ae\u0001\u0000\u0000\u0000\u009e\u05b0\u0001\u0000\u0000\u0000"+
		"\u00a0\u05c0\u0001\u0000\u0000\u0000\u00a2\u05c2\u0001\u0000\u0000\u0000"+
		"\u00a4\u05c6\u0001\u0000\u0000\u0000\u00a6\u05c8\u0001\u0000\u0000\u0000"+
		"\u00a8\u05dd\u0001\u0000\u0000\u0000\u00aa\u05df\u0001\u0000\u0000\u0000"+
		"\u00ac\u05e5\u0001\u0000\u0000\u0000\u00ae\u05f7\u0001\u0000\u0000\u0000"+
		"\u00b0\u05fc\u0001\u0000\u0000\u0000\u00b2\u0603\u0001\u0000\u0000\u0000"+
		"\u00b4\u0608\u0001\u0000\u0000\u0000\u00b6\u060a\u0001\u0000\u0000\u0000"+
		"\u00b8\u0618\u0001\u0000\u0000\u0000\u00ba\u062a\u0001\u0000\u0000\u0000"+
		"\u00bc\u0637\u0001\u0000\u0000\u0000\u00be\u063a\u0001\u0000\u0000\u0000"+
		"\u00c0\u0651\u0001\u0000\u0000\u0000\u00c2\u0656\u0001\u0000\u0000\u0000"+
		"\u00c4\u0658\u0001\u0000\u0000\u0000\u00c6\u065f\u0001\u0000\u0000\u0000"+
		"\u00c8\u0672\u0001\u0000\u0000\u0000\u00ca\u0674\u0001\u0000\u0000\u0000"+
		"\u00cc\u0685\u0001\u0000\u0000\u0000\u00ce\u0687\u0001\u0000\u0000\u0000"+
		"\u00d0\u0693\u0001\u0000\u0000\u0000\u00d2\u06a4\u0001\u0000\u0000\u0000"+
		"\u00d4\u06b2\u0001\u0000\u0000\u0000\u00d6\u06bb\u0001\u0000\u0000\u0000"+
		"\u00d8\u06c3\u0001\u0000\u0000\u0000\u00da\u06c6\u0001\u0000\u0000\u0000"+
		"\u00dc\u06ca\u0001\u0000\u0000\u0000\u00de\u06d1\u0001\u0000\u0000\u0000"+
		"\u00e0\u06d7\u0001\u0000\u0000\u0000\u00e2\u06da\u0001\u0000\u0000\u0000"+
		"\u00e4\u06e5\u0001\u0000\u0000\u0000\u00e6\u06f3\u0001\u0000\u0000\u0000"+
		"\u00e8\u0707\u0001\u0000\u0000\u0000\u00ea\u0717\u0001\u0000\u0000\u0000"+
		"\u00ec\u071c\u0001\u0000\u0000\u0000\u00ee\u0723\u0001\u0000\u0000\u0000"+
		"\u00f0\u0727\u0001\u0000\u0000\u0000\u00f2\u0733\u0001\u0000\u0000\u0000"+
		"\u00f4\u0741\u0001\u0000\u0000\u0000\u00f6\u0753\u0001\u0000\u0000\u0000"+
		"\u00f8\u0756\u0001\u0000\u0000\u0000\u00fa\u0760\u0001\u0000\u0000\u0000"+
		"\u00fc\u0762\u0001\u0000\u0000\u0000\u00fe\u076f\u0001\u0000\u0000\u0000"+
		"\u0100\u077c\u0001\u0000\u0000\u0000\u0102\u077e\u0001\u0000\u0000\u0000"+
		"\u0104\u0784\u0001\u0000\u0000\u0000\u0106\u0793\u0001\u0000\u0000\u0000"+
		"\u0108\u0795\u0001\u0000\u0000\u0000\u010a\u07a0\u0001\u0000\u0000\u0000"+
		"\u010c\u07b7\u0001\u0000\u0000\u0000\u010e\u07bc\u0001\u0000\u0000\u0000"+
		"\u0110\u07c3\u0001\u0000\u0000\u0000\u0112\u07ce\u0001\u0000\u0000\u0000"+
		"\u0114\u07e2\u0001\u0000\u0000\u0000\u0116\u07e4\u0001\u0000\u0000\u0000"+
		"\u0118\u07e8\u0001\u0000\u0000\u0000\u011a\u07ee\u0001\u0000\u0000\u0000"+
		"\u011c\u07fb\u0001\u0000\u0000\u0000\u011e\u0800\u0001\u0000\u0000\u0000"+
		"\u0120\u0810\u0001\u0000\u0000\u0000\u0122\u0812\u0001\u0000\u0000\u0000"+
		"\u0124\u0816\u0001\u0000\u0000\u0000\u0126\u0818\u0001\u0000\u0000\u0000"+
		"\u0128\u0827\u0001\u0000\u0000\u0000\u012a\u082d\u0001\u0000\u0000\u0000"+
		"\u012c\u0831\u0001\u0000\u0000\u0000\u012e\u083a\u0001\u0000\u0000\u0000"+
		"\u0130\u083f\u0001\u0000\u0000\u0000\u0132\u084c\u0001\u0000\u0000\u0000"+
		"\u0134\u0854\u0001\u0000\u0000\u0000\u0136\u0859\u0001\u0000\u0000\u0000"+
		"\u0138\u085b\u0001\u0000\u0000\u0000\u013a\u0869\u0001\u0000\u0000\u0000"+
		"\u013c\u087a\u0001\u0000\u0000\u0000\u013e\u0888\u0001\u0000\u0000\u0000"+
		"\u0140\u088d\u0001\u0000\u0000\u0000\u0142\u0891\u0001\u0000\u0000\u0000"+
		"\u0144\u0894\u0001\u0000\u0000\u0000\u0146\u0897\u0001\u0000\u0000\u0000"+
		"\u0148\u0899\u0001\u0000\u0000\u0000\u014a\u08a6\u0001\u0000\u0000\u0000"+
		"\u014c\u08b9\u0001\u0000\u0000\u0000\u014e\u08c0\u0001\u0000\u0000\u0000"+
		"\u0150\u08c6\u0001\u0000\u0000\u0000\u0152\u08c9\u0001\u0000\u0000\u0000"+
		"\u0154\u08d8\u0001\u0000\u0000\u0000\u0156\u08db\u0001\u0000\u0000\u0000"+
		"\u0158\u08e5\u0001\u0000\u0000\u0000\u015a\u08f0\u0001\u0000\u0000\u0000"+
		"\u015c\u091d\u0001\u0000\u0000\u0000\u015e\u0923\u0001\u0000\u0000\u0000"+
		"\u0160\u092b\u0001\u0000\u0000\u0000\u0162\u092d\u0001\u0000\u0000\u0000"+
		"\u0164\u0935\u0001\u0000\u0000\u0000\u0166\u093d\u0001\u0000\u0000\u0000"+
		"\u0168\u0945\u0001\u0000\u0000\u0000\u016a\u0949\u0001\u0000\u0000\u0000"+
		"\u016c\u0950\u0001\u0000\u0000\u0000\u016e\u0958\u0001\u0000\u0000\u0000"+
		"\u0170\u0960\u0001\u0000\u0000\u0000\u0172\u096a\u0001\u0000\u0000\u0000"+
		"\u0174\u0972\u0001\u0000\u0000\u0000\u0176\u097b\u0001\u0000\u0000\u0000"+
		"\u0178\u0986\u0001\u0000\u0000\u0000\u017a\u0992\u0001\u0000\u0000\u0000"+
		"\u017c\u0994\u0001\u0000\u0000\u0000\u017e\u099c\u0001\u0000\u0000\u0000"+
		"\u0180\u099e\u0001\u0000\u0000\u0000\u0182\u09a0\u0001\u0000\u0000\u0000"+
		"\u0184\u09a2\u0001\u0000\u0000\u0000\u0186\u09a6\u0001\u0000\u0000\u0000"+
		"\u0188\u018a\u0003\u008cF\u0000\u0189\u0188\u0001\u0000\u0000\u0000\u018a"+
		"\u018d\u0001\u0000\u0000\u0000\u018b\u0189\u0001\u0000\u0000\u0000\u018b"+
		"\u018c\u0001\u0000\u0000\u0000\u018c\u0191\u0001\u0000\u0000\u0000\u018d"+
		"\u018b\u0001\u0000\u0000\u0000\u018e\u0190\u0003\"\u0011\u0000\u018f\u018e"+
		"\u0001\u0000\u0000\u0000\u0190\u0193\u0001\u0000\u0000\u0000\u0191\u018f"+
		"\u0001\u0000\u0000\u0000\u0191\u0192\u0001\u0000\u0000\u0000\u0192\u0194"+
		"\u0001\u0000\u0000\u0000\u0193\u0191\u0001\u0000\u0000\u0000\u0194\u0195"+
		"\u0005\u0000\u0000\u0001\u0195\u0001\u0001\u0000\u0000\u0000\u0196\u0197"+
		"\u0003\u0152\u00a9\u0000\u0197\u0198\u0005V\u0000\u0000\u0198\u0199\u0003"+
		"\u0004\u0002\u0000\u0199\u0003\u0001\u0000\u0000\u0000\u019a\u019e\u0005"+
		"\u007f\u0000\u0000\u019b\u019d\u0003\u0006\u0003\u0000\u019c\u019b\u0001"+
		"\u0000\u0000\u0000\u019d\u01a0\u0001\u0000\u0000\u0000\u019e\u019c\u0001"+
		"\u0000\u0000\u0000\u019e\u019f\u0001\u0000\u0000\u0000\u019f\u01a1\u0001"+
		"\u0000\u0000\u0000\u01a0\u019e\u0001\u0000\u0000\u0000\u01a1\u01b3\u0005"+
		"\u0080\u0000\u0000\u01a2\u01a6\u0005}\u0000\u0000\u01a3\u01a5\u0003\u0006"+
		"\u0003\u0000\u01a4\u01a3\u0001\u0000\u0000\u0000\u01a5\u01a8\u0001\u0000"+
		"\u0000\u0000\u01a6\u01a4\u0001\u0000\u0000\u0000\u01a6\u01a7\u0001\u0000"+
		"\u0000\u0000\u01a7\u01a9\u0001\u0000\u0000\u0000\u01a8\u01a6\u0001\u0000"+
		"\u0000\u0000\u01a9\u01b3\u0005~\u0000\u0000\u01aa\u01ae\u0005{\u0000\u0000"+
		"\u01ab\u01ad\u0003\u0006\u0003\u0000\u01ac\u01ab\u0001\u0000\u0000\u0000"+
		"\u01ad\u01b0\u0001\u0000\u0000\u0000\u01ae\u01ac\u0001\u0000\u0000\u0000"+
		"\u01ae\u01af\u0001\u0000\u0000\u0000\u01af\u01b1\u0001\u0000\u0000\u0000"+
		"\u01b0\u01ae\u0001\u0000\u0000\u0000\u01b1\u01b3\u0005|\u0000\u0000\u01b2"+
		"\u019a\u0001\u0000\u0000\u0000\u01b2\u01a2\u0001\u0000\u0000\u0000\u01b2"+
		"\u01aa\u0001\u0000\u0000\u0000\u01b3\u0005\u0001\u0000\u0000\u0000\u01b4"+
		"\u01b6\u0003\b\u0004\u0000\u01b5\u01b4\u0001\u0000\u0000\u0000\u01b6\u01b7"+
		"\u0001\u0000\u0000\u0000\u01b7\u01b5\u0001\u0000\u0000\u0000\u01b7\u01b8"+
		"\u0001\u0000\u0000\u0000\u01b8\u01bb\u0001\u0000\u0000\u0000\u01b9\u01bb"+
		"\u0003\u0004\u0002\u0000\u01ba\u01b5\u0001\u0000\u0000\u0000\u01ba\u01b9"+
		"\u0001\u0000\u0000\u0000\u01bb\u0007\u0001\u0000\u0000\u0000\u01bc\u01c2"+
		"\u0003\u017e\u00bf\u0000\u01bd\u01c2\u0003\u0180\u00c0\u0000\u01be\u01c2"+
		"\u0003\u0182\u00c1\u0000\u01bf\u01c2\u0003\u001e\u000f\u0000\u01c0\u01c2"+
		"\u0005y\u0000\u0000\u01c1\u01bc\u0001\u0000\u0000\u0000\u01c1\u01bd\u0001"+
		"\u0000\u0000\u0000\u01c1\u01be\u0001\u0000\u0000\u0000\u01c1\u01bf\u0001"+
		"\u0000\u0000\u0000\u01c1\u01c0\u0001\u0000\u0000\u0000\u01c2\t\u0001\u0000"+
		"\u0000\u0000\u01c3\u01c4\u0003\u0152\u00a9\u0000\u01c4\u01c5\u0005V\u0000"+
		"\u0000\u01c5\u01c9\u0005\u007f\u0000\u0000\u01c6\u01c8\u0003\u0006\u0003"+
		"\u0000\u01c7\u01c6\u0001\u0000\u0000\u0000\u01c8\u01cb\u0001\u0000\u0000"+
		"\u0000\u01c9\u01c7\u0001\u0000\u0000\u0000\u01c9\u01ca\u0001\u0000\u0000"+
		"\u0000\u01ca\u01cc\u0001\u0000\u0000\u0000\u01cb\u01c9\u0001\u0000\u0000"+
		"\u0000\u01cc\u01cd\u0005\u0080\u0000\u0000\u01cd\u01ce\u0005s\u0000\u0000"+
		"\u01ce\u01e7\u0001\u0000\u0000\u0000\u01cf\u01d0\u0003\u0152\u00a9\u0000"+
		"\u01d0\u01d1\u0005V\u0000\u0000\u01d1\u01d5\u0005}\u0000\u0000\u01d2\u01d4"+
		"\u0003\u0006\u0003\u0000\u01d3\u01d2\u0001\u0000\u0000\u0000\u01d4\u01d7"+
		"\u0001\u0000\u0000\u0000\u01d5\u01d3\u0001\u0000\u0000\u0000\u01d5\u01d6"+
		"\u0001\u0000\u0000\u0000\u01d6\u01d8\u0001\u0000\u0000\u0000\u01d7\u01d5"+
		"\u0001\u0000\u0000\u0000\u01d8\u01d9\u0005~\u0000\u0000\u01d9\u01da\u0005"+
		"s\u0000\u0000\u01da\u01e7\u0001\u0000\u0000\u0000\u01db\u01dc\u0003\u0152"+
		"\u00a9\u0000\u01dc\u01dd\u0005V\u0000\u0000\u01dd\u01e1\u0005{\u0000\u0000"+
		"\u01de\u01e0\u0003\u0006\u0003\u0000\u01df\u01de\u0001\u0000\u0000\u0000"+
		"\u01e0\u01e3\u0001\u0000\u0000\u0000\u01e1\u01df\u0001\u0000\u0000\u0000"+
		"\u01e1\u01e2\u0001\u0000\u0000\u0000\u01e2\u01e4\u0001\u0000\u0000\u0000"+
		"\u01e3\u01e1\u0001\u0000\u0000\u0000\u01e4\u01e5\u0005|\u0000\u0000\u01e5"+
		"\u01e7\u0001\u0000\u0000\u0000\u01e6\u01c3\u0001\u0000\u0000\u0000\u01e6"+
		"\u01cf\u0001\u0000\u0000\u0000\u01e6\u01db\u0001\u0000\u0000\u0000\u01e7"+
		"\u000b\u0001\u0000\u0000\u0000\u01e8\u01e9\u00056\u0000\u0000\u01e9\u01ea"+
		"\u0005V\u0000\u0000\u01ea\u01eb\u0003\u017a\u00bd\u0000\u01eb\u01ec\u0003"+
		"\u000e\u0007\u0000\u01ec\r\u0001\u0000\u0000\u0000\u01ed\u01ee\u0005\u007f"+
		"\u0000\u0000\u01ee\u01ef\u0003\u0010\b\u0000\u01ef\u01f0\u0005\u0080\u0000"+
		"\u0000\u01f0\u01f1\u0005s\u0000\u0000\u01f1\u01fc\u0001\u0000\u0000\u0000"+
		"\u01f2\u01f3\u0005}\u0000\u0000\u01f3\u01f4\u0003\u0010\b\u0000\u01f4"+
		"\u01f5\u0005~\u0000\u0000\u01f5\u01f6\u0005s\u0000\u0000\u01f6\u01fc\u0001"+
		"\u0000\u0000\u0000\u01f7\u01f8\u0005{\u0000\u0000\u01f8\u01f9\u0003\u0010"+
		"\b\u0000\u01f9\u01fa\u0005|\u0000\u0000\u01fa\u01fc\u0001\u0000\u0000"+
		"\u0000\u01fb\u01ed\u0001\u0000\u0000\u0000\u01fb\u01f2\u0001\u0000\u0000"+
		"\u0000\u01fb\u01f7\u0001\u0000\u0000\u0000\u01fc\u000f\u0001\u0000\u0000"+
		"\u0000\u01fd\u0202\u0003\u0012\t\u0000\u01fe\u01ff\u0005s\u0000\u0000"+
		"\u01ff\u0201\u0003\u0012\t\u0000\u0200\u01fe\u0001\u0000\u0000\u0000\u0201"+
		"\u0204\u0001\u0000\u0000\u0000\u0202\u0200\u0001\u0000\u0000\u0000\u0202"+
		"\u0203\u0001\u0000\u0000\u0000\u0203\u0206\u0001\u0000\u0000\u0000\u0204"+
		"\u0202\u0001\u0000\u0000\u0000\u0205\u0207\u0005s\u0000\u0000\u0206\u0205"+
		"\u0001\u0000\u0000\u0000\u0206\u0207\u0001\u0000\u0000\u0000\u0207\u0011"+
		"\u0001\u0000\u0000\u0000\u0208\u0209\u0003\u0014\n\u0000\u0209\u020a\u0005"+
		"w\u0000\u0000\u020a\u020b\u0003 \u0010\u0000\u020b\u0013\u0001\u0000\u0000"+
		"\u0000\u020c\u0210\u0005\u007f\u0000\u0000\u020d\u020f\u0003\u0016\u000b"+
		"\u0000\u020e\u020d\u0001\u0000\u0000\u0000\u020f\u0212\u0001\u0000\u0000"+
		"\u0000\u0210\u020e\u0001\u0000\u0000\u0000\u0210\u0211\u0001\u0000\u0000"+
		"\u0000\u0211\u0213\u0001\u0000\u0000\u0000\u0212\u0210\u0001\u0000\u0000"+
		"\u0000\u0213\u0225\u0005\u0080\u0000\u0000\u0214\u0218\u0005}\u0000\u0000"+
		"\u0215\u0217\u0003\u0016\u000b\u0000\u0216\u0215\u0001\u0000\u0000\u0000"+
		"\u0217\u021a\u0001\u0000\u0000\u0000\u0218\u0216\u0001\u0000\u0000\u0000"+
		"\u0218\u0219\u0001\u0000\u0000\u0000\u0219\u021b\u0001\u0000\u0000\u0000"+
		"\u021a\u0218\u0001\u0000\u0000\u0000\u021b\u0225\u0005~\u0000\u0000\u021c"+
		"\u0220\u0005{\u0000\u0000\u021d\u021f\u0003\u0016\u000b\u0000\u021e\u021d"+
		"\u0001\u0000\u0000\u0000\u021f\u0222\u0001\u0000\u0000\u0000\u0220\u021e"+
		"\u0001\u0000\u0000\u0000\u0220\u0221\u0001\u0000\u0000\u0000\u0221\u0223"+
		"\u0001\u0000\u0000\u0000\u0222\u0220\u0001\u0000\u0000\u0000\u0223\u0225"+
		"\u0005|\u0000\u0000\u0224\u020c\u0001\u0000\u0000\u0000\u0224\u0214\u0001"+
		"\u0000\u0000\u0000\u0224\u021c\u0001\u0000\u0000\u0000\u0225\u0015\u0001"+
		"\u0000\u0000\u0000\u0226\u0228\u0003\u0018\f\u0000\u0227\u0226\u0001\u0000"+
		"\u0000\u0000\u0228\u0229\u0001\u0000\u0000\u0000\u0229\u0227\u0001\u0000"+
		"\u0000\u0000\u0229\u022a\u0001\u0000\u0000\u0000\u022a\u0241\u0001\u0000"+
		"\u0000\u0000\u022b\u0241\u0003\u0014\n\u0000\u022c\u022f\u0005y\u0000"+
		"\u0000\u022d\u0230\u0003\u017a\u00bd\u0000\u022e\u0230\u0005\u0018\u0000"+
		"\u0000\u022f\u022d\u0001\u0000\u0000\u0000\u022f\u022e\u0001\u0000\u0000"+
		"\u0000\u0230\u0231\u0001\u0000\u0000\u0000\u0231\u0232\u0005t\u0000\u0000"+
		"\u0232\u0241\u0003\u001a\r\u0000\u0233\u0234\u0005y\u0000\u0000\u0234"+
		"\u0236\u0005\u007f\u0000\u0000\u0235\u0237\u0003\u0016\u000b\u0000\u0236"+
		"\u0235\u0001\u0000\u0000\u0000\u0237\u0238\u0001\u0000\u0000\u0000\u0238"+
		"\u0236\u0001\u0000\u0000\u0000\u0238\u0239\u0001\u0000\u0000\u0000\u0239"+
		"\u023a\u0001\u0000\u0000\u0000\u023a\u023c\u0005\u0080\u0000\u0000\u023b"+
		"\u023d\u0003\u001c\u000e\u0000\u023c\u023b\u0001\u0000\u0000\u0000\u023c"+
		"\u023d\u0001\u0000\u0000\u0000\u023d\u023e\u0001\u0000\u0000\u0000\u023e"+
		"\u023f\u0003\u001e\u000f\u0000\u023f\u0241\u0001\u0000\u0000\u0000\u0240"+
		"\u0227\u0001\u0000\u0000\u0000\u0240\u022b\u0001\u0000\u0000\u0000\u0240"+
		"\u022c\u0001\u0000\u0000\u0000\u0240\u0233\u0001\u0000\u0000\u0000\u0241"+
		"\u0017\u0001\u0000\u0000\u0000\u0242\u0247\u0003\u017e\u00bf\u0000\u0243"+
		"\u0247\u0003\u0180\u00c0\u0000\u0244\u0247\u0003\u0182\u00c1\u0000\u0245"+
		"\u0247\u0003\u001e\u000f\u0000\u0246\u0242\u0001\u0000\u0000\u0000\u0246"+
		"\u0243\u0001\u0000\u0000\u0000\u0246\u0244\u0001\u0000\u0000\u0000\u0246"+
		"\u0245\u0001\u0000\u0000\u0000\u0247\u0019\u0001\u0000\u0000\u0000\u0248"+
		"\u0249\u0003\u017a\u00bd\u0000\u0249\u001b\u0001\u0000\u0000\u0000\u024a"+
		"\u024f\u0003\u017e\u00bf\u0000\u024b\u024f\u0003\u0180\u00c0\u0000\u024c"+
		"\u024f\u0003\u0182\u00c1\u0000\u024d\u024f\u0005y\u0000\u0000\u024e\u024a"+
		"\u0001\u0000\u0000\u0000\u024e\u024b\u0001\u0000\u0000\u0000\u024e\u024c"+
		"\u0001\u0000\u0000\u0000\u024e\u024d\u0001\u0000\u0000\u0000\u024f\u001d"+
		"\u0001\u0000\u0000\u0000\u0250\u0251\u0007\u0000\u0000\u0000\u0251\u001f"+
		"\u0001\u0000\u0000\u0000\u0252\u0253\u0003\u0004\u0002\u0000\u0253!\u0001"+
		"\u0000\u0000\u0000\u0254\u0256\u0003\u008eG\u0000\u0255\u0254\u0001\u0000"+
		"\u0000\u0000\u0256\u0259\u0001\u0000\u0000\u0000\u0257\u0255\u0001\u0000"+
		"\u0000\u0000\u0257\u0258\u0001\u0000\u0000\u0000\u0258\u025c\u0001\u0000"+
		"\u0000\u0000\u0259\u0257\u0001\u0000\u0000\u0000\u025a\u025d\u0003$\u0012"+
		"\u0000\u025b\u025d\u0003&\u0013\u0000\u025c\u025a\u0001\u0000\u0000\u0000"+
		"\u025c\u025b\u0001\u0000\u0000\u0000\u025d#\u0001\u0000\u0000\u0000\u025e"+
		"\u0260\u0003\u0178\u00bc\u0000\u025f\u025e\u0001\u0000\u0000\u0000\u025f"+
		"\u0260\u0001\u0000\u0000\u0000\u0260\u026e\u0001\u0000\u0000\u0000\u0261"+
		"\u026f\u0003(\u0014\u0000\u0262\u026f\u0003*\u0015\u0000\u0263\u026f\u0003"+
		"0\u0018\u0000\u0264\u026f\u00034\u001a\u0000\u0265\u026f\u0003H$\u0000"+
		"\u0266\u026f\u0003J%\u0000\u0267\u026f\u0003X,\u0000\u0268\u026f\u0003"+
		"d2\u0000\u0269\u026f\u0003f3\u0000\u026a\u026f\u0003h4\u0000\u026b\u026f"+
		"\u0003j5\u0000\u026c\u026f\u0003l6\u0000\u026d\u026f\u0003r9\u0000\u026e"+
		"\u0261\u0001\u0000\u0000\u0000\u026e\u0262\u0001\u0000\u0000\u0000\u026e"+
		"\u0263\u0001\u0000\u0000\u0000\u026e\u0264\u0001\u0000\u0000\u0000\u026e"+
		"\u0265\u0001\u0000\u0000\u0000\u026e\u0266\u0001\u0000\u0000\u0000\u026e"+
		"\u0267\u0001\u0000\u0000\u0000\u026e\u0268\u0001\u0000\u0000\u0000\u026e"+
		"\u0269\u0001\u0000\u0000\u0000\u026e\u026a\u0001\u0000\u0000\u0000\u026e"+
		"\u026b\u0001\u0000\u0000\u0000\u026e\u026c\u0001\u0000\u0000\u0000\u026e"+
		"\u026d\u0001\u0000\u0000\u0000\u026f%\u0001\u0000\u0000\u0000\u0270\u0273"+
		"\u0003\n\u0005\u0000\u0271\u0273\u0003\f\u0006\u0000\u0272\u0270\u0001"+
		"\u0000\u0000\u0000\u0272\u0271\u0001\u0000\u0000\u0000\u0273\'\u0001\u0000"+
		"\u0000\u0000\u0274\u0276\u0005 \u0000\u0000\u0275\u0274\u0001\u0000\u0000"+
		"\u0000\u0275\u0276\u0001\u0000\u0000\u0000\u0276\u0277\u0001\u0000\u0000"+
		"\u0000\u0277\u0278\u0005\u0012\u0000\u0000\u0278\u0288\u0003\u017a\u00bd"+
		"\u0000\u0279\u0289\u0005s\u0000\u0000\u027a\u027e\u0005{\u0000\u0000\u027b"+
		"\u027d\u0003\u008cF\u0000\u027c\u027b\u0001\u0000\u0000\u0000\u027d\u0280"+
		"\u0001\u0000\u0000\u0000\u027e\u027c\u0001\u0000\u0000\u0000\u027e\u027f"+
		"\u0001\u0000\u0000\u0000\u027f\u0284\u0001\u0000\u0000\u0000\u0280\u027e"+
		"\u0001\u0000\u0000\u0000\u0281\u0283\u0003\"\u0011\u0000\u0282\u0281\u0001"+
		"\u0000\u0000\u0000\u0283\u0286\u0001\u0000\u0000\u0000\u0284\u0282\u0001"+
		"\u0000\u0000\u0000\u0284\u0285\u0001\u0000\u0000\u0000\u0285\u0287\u0001"+
		"\u0000\u0000\u0000\u0286\u0284\u0001\u0000\u0000\u0000\u0287\u0289\u0005"+
		"|\u0000\u0000\u0288\u0279\u0001\u0000\u0000\u0000\u0288\u027a\u0001\u0000"+
		"\u0000\u0000\u0289)\u0001\u0000\u0000\u0000\u028a\u028b\u0005\b\u0000"+
		"\u0000\u028b\u028c\u0005\u0005\u0000\u0000\u028c\u028e\u0003,\u0016\u0000"+
		"\u028d\u028f\u0003.\u0017\u0000\u028e\u028d\u0001\u0000\u0000\u0000\u028e"+
		"\u028f\u0001\u0000\u0000\u0000\u028f\u0290\u0001\u0000\u0000\u0000\u0290"+
		"\u0291\u0005s\u0000\u0000\u0291+\u0001\u0000\u0000\u0000\u0292\u0295\u0003"+
		"\u017a\u00bd\u0000\u0293\u0295\u0005\u0018\u0000\u0000\u0294\u0292\u0001"+
		"\u0000\u0000\u0000\u0294\u0293\u0001\u0000\u0000\u0000\u0295-\u0001\u0000"+
		"\u0000\u0000\u0296\u0299\u0005\u0001\u0000\u0000\u0297\u029a\u0003\u017a"+
		"\u00bd\u0000\u0298\u029a\u0005m\u0000\u0000\u0299\u0297\u0001\u0000\u0000"+
		"\u0000\u0299\u0298\u0001\u0000\u0000\u0000\u029a/\u0001\u0000\u0000\u0000"+
		"\u029b\u029c\u0005!\u0000\u0000\u029c\u029d\u00032\u0019\u0000\u029d\u029e"+
		"\u0005s\u0000\u0000\u029e1\u0001\u0000\u0000\u0000\u029f\u02a1\u0003\u0152"+
		"\u00a9\u0000\u02a0\u029f\u0001\u0000\u0000\u0000\u02a0\u02a1\u0001\u0000"+
		"\u0000\u0000\u02a1\u02a2\u0001\u0000\u0000\u0000\u02a2\u02a4\u0005u\u0000"+
		"\u0000\u02a3\u02a0\u0001\u0000\u0000\u0000\u02a3\u02a4\u0001\u0000\u0000"+
		"\u0000\u02a4\u02b5\u0001\u0000\u0000\u0000\u02a5\u02b6\u0005R\u0000\u0000"+
		"\u02a6\u02b2\u0005{\u0000\u0000\u02a7\u02ac\u00032\u0019\u0000\u02a8\u02a9"+
		"\u0005r\u0000\u0000\u02a9\u02ab\u00032\u0019\u0000\u02aa\u02a8\u0001\u0000"+
		"\u0000\u0000\u02ab\u02ae\u0001\u0000\u0000\u0000\u02ac\u02aa\u0001\u0000"+
		"\u0000\u0000\u02ac\u02ad\u0001\u0000\u0000\u0000\u02ad\u02b0\u0001\u0000"+
		"\u0000\u0000\u02ae\u02ac\u0001\u0000\u0000\u0000\u02af\u02b1\u0005r\u0000"+
		"\u0000\u02b0\u02af\u0001\u0000\u0000\u0000\u02b0\u02b1\u0001\u0000\u0000"+
		"\u0000\u02b1\u02b3\u0001\u0000\u0000\u0000\u02b2\u02a7\u0001\u0000\u0000"+
		"\u0000\u02b2\u02b3\u0001\u0000\u0000\u0000\u02b3\u02b4\u0001\u0000\u0000"+
		"\u0000\u02b4\u02b6\u0005|\u0000\u0000\u02b5\u02a5\u0001\u0000\u0000\u0000"+
		"\u02b5\u02a6\u0001\u0000\u0000\u0000\u02b6\u02c0\u0001\u0000\u0000\u0000"+
		"\u02b7\u02bd\u0003\u0152\u00a9\u0000\u02b8\u02bb\u0005\u0001\u0000\u0000"+
		"\u02b9\u02bc\u0003\u017a\u00bd\u0000\u02ba\u02bc\u0005m\u0000\u0000\u02bb"+
		"\u02b9\u0001\u0000\u0000\u0000\u02bb\u02ba\u0001\u0000\u0000\u0000\u02bc"+
		"\u02be\u0001\u0000\u0000\u0000\u02bd\u02b8\u0001\u0000\u0000\u0000\u02bd"+
		"\u02be\u0001\u0000\u0000\u0000\u02be\u02c0\u0001\u0000\u0000\u0000\u02bf"+
		"\u02a3\u0001\u0000\u0000\u0000\u02bf\u02b7\u0001\u0000\u0000\u0000\u02c0"+
		"3\u0001\u0000\u0000\u0000\u02c1\u02c2\u00036\u001b\u0000\u02c2\u02c3\u0005"+
		"\n\u0000\u0000\u02c3\u02c5\u0003\u017a\u00bd\u0000\u02c4\u02c6\u0003v"+
		";\u0000\u02c5\u02c4\u0001\u0000\u0000\u0000\u02c5\u02c6\u0001\u0000\u0000"+
		"\u0000\u02c6\u02c7\u0001\u0000\u0000\u0000\u02c7\u02c9\u0005\u007f\u0000"+
		"\u0000\u02c8\u02ca\u0003:\u001d\u0000\u02c9\u02c8\u0001\u0000\u0000\u0000"+
		"\u02c9\u02ca\u0001\u0000\u0000\u0000\u02ca\u02cb\u0001\u0000\u0000\u0000"+
		"\u02cb\u02cd\u0005\u0080\u0000\u0000\u02cc\u02ce\u0003F#\u0000\u02cd\u02cc"+
		"\u0001\u0000\u0000\u0000\u02cd\u02ce\u0001\u0000\u0000\u0000\u02ce\u02d0"+
		"\u0001\u0000\u0000\u0000\u02cf\u02d1\u0003\u0080@\u0000\u02d0\u02cf\u0001"+
		"\u0000\u0000\u0000\u02d0\u02d1\u0001\u0000\u0000\u0000\u02d1\u02d4\u0001"+
		"\u0000\u0000\u0000\u02d2\u02d5\u0003\u00a6S\u0000\u02d3\u02d5\u0005s\u0000"+
		"\u0000\u02d4\u02d2\u0001\u0000\u0000\u0000\u02d4\u02d3\u0001\u0000\u0000"+
		"\u0000\u02d55\u0001\u0000\u0000\u0000\u02d6\u02d8\u0005\u0003\u0000\u0000"+
		"\u02d7\u02d6\u0001\u0000\u0000\u0000\u02d7\u02d8\u0001\u0000\u0000\u0000"+
		"\u02d8\u02da\u0001\u0000\u0000\u0000\u02d9\u02db\u0005$\u0000\u0000\u02da"+
		"\u02d9\u0001\u0000\u0000\u0000\u02da\u02db\u0001\u0000\u0000\u0000\u02db"+
		"\u02dd\u0001\u0000\u0000\u0000\u02dc\u02de\u0005 \u0000\u0000\u02dd\u02dc"+
		"\u0001\u0000\u0000\u0000\u02dd\u02de\u0001\u0000\u0000\u0000\u02de\u02e3"+
		"\u0001\u0000\u0000\u0000\u02df\u02e1\u0005\b\u0000\u0000\u02e0\u02e2\u0003"+
		"8\u001c\u0000\u02e1\u02e0\u0001\u0000\u0000\u0000\u02e1\u02e2\u0001\u0000"+
		"\u0000\u0000\u02e2\u02e4\u0001\u0000\u0000\u0000\u02e3\u02df\u0001\u0000"+
		"\u0000\u0000\u02e3\u02e4\u0001\u0000\u0000\u0000\u02e47\u0001\u0000\u0000"+
		"\u0000\u02e5\u02e6\u0007\u0001\u0000\u0000\u02e69\u0001\u0000\u0000\u0000"+
		"\u02e7\u02e9\u0003<\u001e\u0000\u02e8\u02ea\u0005r\u0000\u0000\u02e9\u02e8"+
		"\u0001\u0000\u0000\u0000\u02e9\u02ea\u0001\u0000\u0000\u0000\u02ea\u02fc"+
		"\u0001\u0000\u0000\u0000\u02eb\u02ec\u0003<\u001e\u0000\u02ec\u02ed\u0005"+
		"r\u0000\u0000\u02ed\u02ef\u0001\u0000\u0000\u0000\u02ee\u02eb\u0001\u0000"+
		"\u0000\u0000\u02ee\u02ef\u0001\u0000\u0000\u0000\u02ef\u02f0\u0001\u0000"+
		"\u0000\u0000\u02f0\u02f5\u0003B!\u0000\u02f1\u02f2\u0005r\u0000\u0000"+
		"\u02f2\u02f4\u0003B!\u0000\u02f3\u02f1\u0001\u0000\u0000\u0000\u02f4\u02f7"+
		"\u0001\u0000\u0000\u0000\u02f5\u02f3\u0001\u0000\u0000\u0000\u02f5\u02f6"+
		"\u0001\u0000\u0000\u0000\u02f6\u02f9\u0001\u0000\u0000\u0000\u02f7\u02f5"+
		"\u0001\u0000\u0000\u0000\u02f8\u02fa\u0005r\u0000\u0000\u02f9\u02f8\u0001"+
		"\u0000\u0000\u0000\u02f9\u02fa\u0001\u0000\u0000\u0000\u02fa\u02fc\u0001"+
		"\u0000\u0000\u0000\u02fb\u02e7\u0001\u0000\u0000\u0000\u02fb\u02ee\u0001"+
		"\u0000\u0000\u0000\u02fc;\u0001\u0000\u0000\u0000\u02fd\u02ff\u0003\u008e"+
		"G\u0000\u02fe\u02fd\u0001\u0000\u0000\u0000\u02ff\u0302\u0001\u0000\u0000"+
		"\u0000\u0300\u02fe\u0001\u0000\u0000\u0000\u0300\u0301\u0001\u0000\u0000"+
		"\u0000\u0301\u0305\u0001\u0000\u0000\u0000\u0302\u0300\u0001\u0000\u0000"+
		"\u0000\u0303\u0306\u0003>\u001f\u0000\u0304\u0306\u0003@ \u0000\u0305"+
		"\u0303\u0001\u0000\u0000\u0000\u0305\u0304\u0001\u0000\u0000\u0000\u0306"+
		"=\u0001\u0000\u0000\u0000\u0307\u0309\u0005W\u0000\u0000\u0308\u030a\u0003"+
		"\u0150\u00a8\u0000\u0309\u0308\u0001\u0000\u0000\u0000\u0309\u030a\u0001"+
		"\u0000\u0000\u0000\u030a\u030c\u0001\u0000\u0000\u0000\u030b\u0307\u0001"+
		"\u0000\u0000\u0000\u030b\u030c\u0001\u0000\u0000\u0000\u030c\u030e\u0001"+
		"\u0000\u0000\u0000\u030d\u030f\u0005\u0014\u0000\u0000\u030e\u030d\u0001"+
		"\u0000\u0000\u0000\u030e\u030f\u0001\u0000\u0000\u0000\u030f\u0310\u0001"+
		"\u0000\u0000\u0000\u0310\u0311\u0005\u0018\u0000\u0000\u0311?\u0001\u0000"+
		"\u0000\u0000\u0312\u0314\u0005\u0014\u0000\u0000\u0313\u0312\u0001\u0000"+
		"\u0000\u0000\u0313\u0314\u0001\u0000\u0000\u0000\u0314\u0315\u0001\u0000"+
		"\u0000\u0000\u0315\u0316\u0005\u0018\u0000\u0000\u0316\u0317\u0005t\u0000"+
		"\u0000\u0317\u0318\u0003\u011e\u008f\u0000\u0318A\u0001\u0000\u0000\u0000"+
		"\u0319\u031b\u0003\u008eG\u0000\u031a\u0319\u0001\u0000\u0000\u0000\u031b"+
		"\u031e\u0001\u0000\u0000\u0000\u031c\u031a\u0001\u0000\u0000\u0000\u031c"+
		"\u031d\u0001\u0000\u0000\u0000\u031d\u0322\u0001\u0000\u0000\u0000\u031e"+
		"\u031c\u0001\u0000\u0000\u0000\u031f\u0323\u0003D\"\u0000\u0320\u0323"+
		"\u0005p\u0000\u0000\u0321\u0323\u0003\u011e\u008f\u0000\u0322\u031f\u0001"+
		"\u0000\u0000\u0000\u0322\u0320\u0001\u0000\u0000\u0000\u0322\u0321\u0001"+
		"\u0000\u0000\u0000\u0323C\u0001\u0000\u0000\u0000\u0324\u0325\u0003\u00f0"+
		"x\u0000\u0325\u0328\u0005t\u0000\u0000\u0326\u0329\u0003\u011e\u008f\u0000"+
		"\u0327\u0329\u0005p\u0000\u0000\u0328\u0326\u0001\u0000\u0000\u0000\u0328"+
		"\u0327\u0001\u0000\u0000\u0000\u0329E\u0001\u0000\u0000\u0000\u032a\u032b"+
		"\u0005v\u0000\u0000\u032b\u032c\u0003\u011e\u008f\u0000\u032cG\u0001\u0000"+
		"\u0000\u0000\u032d\u032e\u0005\u001f\u0000\u0000\u032e\u0330\u0003\u017a"+
		"\u00bd\u0000\u032f\u0331\u0003v;\u0000\u0330\u032f\u0001\u0000\u0000\u0000"+
		"\u0330\u0331\u0001\u0000\u0000\u0000\u0331\u0333\u0001\u0000\u0000\u0000"+
		"\u0332\u0334\u0003\u0080@\u0000\u0333\u0332\u0001\u0000\u0000\u0000\u0333"+
		"\u0334\u0001\u0000\u0000\u0000\u0334\u0337\u0001\u0000\u0000\u0000\u0335"+
		"\u0336\u0005e\u0000\u0000\u0336\u0338\u0003\u011e\u008f\u0000\u0337\u0335"+
		"\u0001\u0000\u0000\u0000\u0337\u0338\u0001\u0000\u0000\u0000\u0338\u0339"+
		"\u0001\u0000\u0000\u0000\u0339\u033a\u0005s\u0000\u0000\u033aI\u0001\u0000"+
		"\u0000\u0000\u033b\u033e\u0003L&\u0000\u033c\u033e\u0003N\'\u0000\u033d"+
		"\u033b\u0001\u0000\u0000\u0000\u033d\u033c\u0001\u0000\u0000\u0000\u033e"+
		"K\u0001\u0000\u0000\u0000\u033f\u0340\u0005\u001b\u0000\u0000\u0340\u0342"+
		"\u0003\u017a\u00bd\u0000\u0341\u0343\u0003v;\u0000\u0342\u0341\u0001\u0000"+
		"\u0000\u0000\u0342\u0343\u0001\u0000\u0000\u0000\u0343\u0345\u0001\u0000"+
		"\u0000\u0000\u0344\u0346\u0003\u0080@\u0000\u0345\u0344\u0001\u0000\u0000"+
		"\u0000\u0345\u0346\u0001\u0000\u0000\u0000\u0346\u034d\u0001\u0000\u0000"+
		"\u0000\u0347\u0349\u0005{\u0000\u0000\u0348\u034a\u0003P(\u0000\u0349"+
		"\u0348\u0001\u0000\u0000\u0000\u0349\u034a\u0001\u0000\u0000\u0000\u034a"+
		"\u034b\u0001\u0000\u0000\u0000\u034b\u034e\u0005|\u0000\u0000\u034c\u034e"+
		"\u0005s\u0000\u0000\u034d\u0347\u0001\u0000\u0000\u0000\u034d\u034c\u0001"+
		"\u0000\u0000\u0000\u034eM\u0001\u0000\u0000\u0000\u034f\u0350\u0005\u001b"+
		"\u0000\u0000\u0350\u0352\u0003\u017a\u00bd\u0000\u0351\u0353\u0003v;\u0000"+
		"\u0352\u0351\u0001\u0000\u0000\u0000\u0352\u0353\u0001\u0000\u0000\u0000"+
		"\u0353\u0354\u0001\u0000\u0000\u0000\u0354\u0356\u0005\u007f\u0000\u0000"+
		"\u0355\u0357\u0003T*\u0000\u0356\u0355\u0001\u0000\u0000\u0000\u0356\u0357"+
		"\u0001\u0000\u0000\u0000\u0357\u0358\u0001\u0000\u0000\u0000\u0358\u035a"+
		"\u0005\u0080\u0000\u0000\u0359\u035b\u0003\u0080@\u0000\u035a\u0359\u0001"+
		"\u0000\u0000\u0000\u035a\u035b\u0001\u0000\u0000\u0000\u035b\u035c\u0001"+
		"\u0000\u0000\u0000\u035c\u035d\u0005s\u0000\u0000\u035dO\u0001\u0000\u0000"+
		"\u0000\u035e\u0363\u0003R)\u0000\u035f\u0360\u0005r\u0000\u0000\u0360"+
		"\u0362\u0003R)\u0000\u0361\u035f\u0001\u0000\u0000\u0000\u0362\u0365\u0001"+
		"\u0000\u0000\u0000\u0363\u0361\u0001\u0000\u0000\u0000\u0363\u0364\u0001"+
		"\u0000\u0000\u0000\u0364\u0367\u0001\u0000\u0000\u0000\u0365\u0363\u0001"+
		"\u0000\u0000\u0000\u0366\u0368\u0005r\u0000\u0000\u0367\u0366\u0001\u0000"+
		"\u0000\u0000\u0367\u0368\u0001\u0000\u0000\u0000\u0368Q\u0001\u0000\u0000"+
		"\u0000\u0369\u036b\u0003\u008eG\u0000\u036a\u0369\u0001\u0000\u0000\u0000"+
		"\u036b\u036e\u0001\u0000\u0000\u0000\u036c\u036a\u0001\u0000\u0000\u0000"+
		"\u036c\u036d\u0001\u0000\u0000\u0000\u036d\u0370\u0001\u0000\u0000\u0000"+
		"\u036e\u036c\u0001\u0000\u0000\u0000\u036f\u0371\u0003\u0178\u00bc\u0000"+
		"\u0370\u036f\u0001\u0000\u0000\u0000\u0370\u0371\u0001\u0000\u0000\u0000"+
		"\u0371\u0372\u0001\u0000\u0000\u0000\u0372\u0373\u0003\u017a\u00bd\u0000"+
		"\u0373\u0374\u0005t\u0000\u0000\u0374\u0375\u0003\u011e\u008f\u0000\u0375"+
		"S\u0001\u0000\u0000\u0000\u0376\u037b\u0003V+\u0000\u0377\u0378\u0005"+
		"r\u0000\u0000\u0378\u037a\u0003V+\u0000\u0379\u0377\u0001\u0000\u0000"+
		"\u0000\u037a\u037d\u0001\u0000\u0000\u0000\u037b\u0379\u0001\u0000\u0000"+
		"\u0000\u037b\u037c\u0001\u0000\u0000\u0000\u037c\u037f\u0001\u0000\u0000"+
		"\u0000\u037d\u037b\u0001\u0000\u0000\u0000\u037e\u0380\u0005r\u0000\u0000"+
		"\u037f\u037e\u0001\u0000\u0000\u0000\u037f\u0380\u0001\u0000\u0000\u0000"+
		"\u0380U\u0001\u0000\u0000\u0000\u0381\u0383\u0003\u008eG\u0000\u0382\u0381"+
		"\u0001\u0000\u0000\u0000\u0383\u0386\u0001\u0000\u0000\u0000\u0384\u0382"+
		"\u0001\u0000\u0000\u0000\u0384\u0385\u0001\u0000\u0000\u0000\u0385\u0388"+
		"\u0001\u0000\u0000\u0000\u0386\u0384\u0001\u0000\u0000\u0000\u0387\u0389"+
		"\u0003\u0178\u00bc\u0000\u0388\u0387\u0001\u0000\u0000\u0000\u0388\u0389"+
		"\u0001\u0000\u0000\u0000\u0389\u038a\u0001\u0000\u0000\u0000\u038a\u038b"+
		"\u0003\u011e\u008f\u0000\u038bW\u0001\u0000\u0000\u0000\u038c\u038d\u0005"+
		"\u0007\u0000\u0000\u038d\u038f\u0003\u017a\u00bd\u0000\u038e\u0390\u0003"+
		"v;\u0000\u038f\u038e\u0001\u0000\u0000\u0000\u038f\u0390\u0001\u0000\u0000"+
		"\u0000\u0390\u0392\u0001\u0000\u0000\u0000\u0391\u0393\u0003\u0080@\u0000"+
		"\u0392\u0391\u0001\u0000\u0000\u0000\u0392\u0393\u0001\u0000\u0000\u0000"+
		"\u0393\u0394\u0001\u0000\u0000\u0000\u0394\u0396\u0005{\u0000\u0000\u0395"+
		"\u0397\u0003Z-\u0000\u0396\u0395\u0001\u0000\u0000\u0000\u0396\u0397\u0001"+
		"\u0000\u0000\u0000\u0397\u0398\u0001\u0000\u0000\u0000\u0398\u0399\u0005"+
		"|\u0000\u0000\u0399Y\u0001\u0000\u0000\u0000\u039a\u039f\u0003\\.\u0000"+
		"\u039b\u039c\u0005r\u0000\u0000\u039c\u039e\u0003\\.\u0000\u039d\u039b"+
		"\u0001\u0000\u0000\u0000\u039e\u03a1\u0001\u0000\u0000\u0000\u039f\u039d"+
		"\u0001\u0000\u0000\u0000\u039f\u03a0\u0001\u0000\u0000\u0000\u03a0\u03a3"+
		"\u0001\u0000\u0000\u0000\u03a1\u039f\u0001\u0000\u0000\u0000\u03a2\u03a4"+
		"\u0005r\u0000\u0000\u03a3\u03a2\u0001\u0000\u0000\u0000\u03a3\u03a4\u0001"+
		"\u0000\u0000\u0000\u03a4[\u0001\u0000\u0000\u0000\u03a5\u03a7\u0003\u008e"+
		"G\u0000\u03a6\u03a5\u0001\u0000\u0000\u0000\u03a7\u03aa\u0001\u0000\u0000"+
		"\u0000\u03a8\u03a6\u0001\u0000\u0000\u0000\u03a8\u03a9\u0001\u0000\u0000"+
		"\u0000\u03a9\u03ac\u0001\u0000\u0000\u0000\u03aa\u03a8\u0001\u0000\u0000"+
		"\u0000\u03ab\u03ad\u0003\u0178\u00bc\u0000\u03ac\u03ab\u0001\u0000\u0000"+
		"\u0000\u03ac\u03ad\u0001\u0000\u0000\u0000\u03ad\u03ae\u0001\u0000\u0000"+
		"\u0000\u03ae\u03b2\u0003\u017a\u00bd\u0000\u03af\u03b3\u0003^/\u0000\u03b0"+
		"\u03b3\u0003`0\u0000\u03b1\u03b3\u0003b1\u0000\u03b2\u03af\u0001\u0000"+
		"\u0000\u0000\u03b2\u03b0\u0001\u0000\u0000\u0000\u03b2\u03b1\u0001\u0000"+
		"\u0000\u0000\u03b2\u03b3\u0001\u0000\u0000\u0000\u03b3]\u0001\u0000\u0000"+
		"\u0000\u03b4\u03b6\u0005\u007f\u0000\u0000\u03b5\u03b7\u0003T*\u0000\u03b6"+
		"\u03b5\u0001\u0000\u0000\u0000\u03b6\u03b7\u0001\u0000\u0000\u0000\u03b7"+
		"\u03b8\u0001\u0000\u0000\u0000\u03b8\u03b9\u0005\u0080\u0000\u0000\u03b9"+
		"_\u0001\u0000\u0000\u0000\u03ba\u03bc\u0005{\u0000\u0000\u03bb\u03bd\u0003"+
		"P(\u0000\u03bc\u03bb\u0001\u0000\u0000\u0000\u03bc\u03bd\u0001\u0000\u0000"+
		"\u0000\u03bd\u03be\u0001\u0000\u0000\u0000\u03be\u03bf\u0005|\u0000\u0000"+
		"\u03bfa\u0001\u0000\u0000\u0000\u03c0\u03c1\u0005e\u0000\u0000\u03c1\u03c2"+
		"\u0003\u009aM\u0000\u03c2c\u0001\u0000\u0000\u0000\u03c3\u03c4\u00054"+
		"\u0000\u0000\u03c4\u03c6\u0003\u017a\u00bd\u0000\u03c5\u03c7\u0003v;\u0000"+
		"\u03c6\u03c5\u0001\u0000\u0000\u0000\u03c6\u03c7\u0001\u0000\u0000\u0000"+
		"\u03c7\u03c9\u0001\u0000\u0000\u0000\u03c8\u03ca\u0003\u0080@\u0000\u03c9"+
		"\u03c8\u0001\u0000\u0000\u0000\u03c9\u03ca\u0001\u0000\u0000\u0000\u03ca"+
		"\u03cb\u0001\u0000\u0000\u0000\u03cb\u03cc\u0005{\u0000\u0000\u03cc\u03cd"+
		"\u0003P(\u0000\u03cd\u03ce\u0005|\u0000\u0000\u03cee\u0001\u0000\u0000"+
		"\u0000\u03cf\u03d2\u0005\u0003\u0000\u0000\u03d0\u03d3\u0003\u017a\u00bd"+
		"\u0000\u03d1\u03d3\u0005m\u0000\u0000\u03d2\u03d0\u0001\u0000\u0000\u0000"+
		"\u03d2\u03d1\u0001\u0000\u0000\u0000\u03d3\u03d4\u0001\u0000\u0000\u0000"+
		"\u03d4\u03d5\u0005t\u0000\u0000\u03d5\u03d8\u0003\u011e\u008f\u0000\u03d6"+
		"\u03d7\u0005e\u0000\u0000\u03d7\u03d9\u0003\u009aM\u0000\u03d8\u03d6\u0001"+
		"\u0000\u0000\u0000\u03d8\u03d9\u0001\u0000\u0000\u0000\u03d9\u03da\u0001"+
		"\u0000\u0000\u0000\u03da\u03db\u0005s\u0000\u0000\u03dbg\u0001\u0000\u0000"+
		"\u0000\u03dc\u03de\u0005\u001a\u0000\u0000\u03dd\u03df\u0005\u0014\u0000"+
		"\u0000\u03de\u03dd\u0001\u0000\u0000\u0000\u03de\u03df\u0001\u0000\u0000"+
		"\u0000\u03df\u03e0\u0001\u0000\u0000\u0000\u03e0\u03e1\u0003\u017a\u00bd"+
		"\u0000\u03e1\u03e2\u0005t\u0000\u0000\u03e2\u03e5\u0003\u011e\u008f\u0000"+
		"\u03e3\u03e4\u0005e\u0000\u0000\u03e4\u03e6\u0003\u009aM\u0000\u03e5\u03e3"+
		"\u0001\u0000\u0000\u0000\u03e5\u03e6\u0001\u0000\u0000\u0000\u03e6\u03e7"+
		"\u0001\u0000\u0000\u0000\u03e7\u03e8\u0005s\u0000\u0000\u03e8i\u0001\u0000"+
		"\u0000\u0000\u03e9\u03eb\u0005 \u0000\u0000\u03ea\u03e9\u0001\u0000\u0000"+
		"\u0000\u03ea\u03eb\u0001\u0000\u0000\u0000\u03eb\u03ec\u0001\u0000\u0000"+
		"\u0000\u03ec\u03ed\u0005\u001d\u0000\u0000\u03ed\u03ef\u0003\u017a\u00bd"+
		"\u0000\u03ee\u03f0\u0003v;\u0000\u03ef\u03ee\u0001\u0000\u0000\u0000\u03ef"+
		"\u03f0\u0001\u0000\u0000\u0000\u03f0\u03f5\u0001\u0000\u0000\u0000\u03f1"+
		"\u03f3\u0005t\u0000\u0000\u03f2\u03f4\u0003\u0148\u00a4\u0000\u03f3\u03f2"+
		"\u0001\u0000\u0000\u0000\u03f3\u03f4\u0001\u0000\u0000\u0000\u03f4\u03f6"+
		"\u0001\u0000\u0000\u0000\u03f5\u03f1\u0001\u0000\u0000\u0000\u03f5\u03f6"+
		"\u0001\u0000\u0000\u0000\u03f6\u03f8\u0001\u0000\u0000\u0000\u03f7\u03f9"+
		"\u0003\u0080@\u0000\u03f8\u03f7\u0001\u0000\u0000\u0000\u03f8\u03f9\u0001"+
		"\u0000\u0000\u0000\u03f9\u03fa\u0001\u0000\u0000\u0000\u03fa\u03fe\u0005"+
		"{\u0000\u0000\u03fb\u03fd\u0003\u008cF\u0000\u03fc\u03fb\u0001\u0000\u0000"+
		"\u0000\u03fd\u0400\u0001\u0000\u0000\u0000\u03fe\u03fc\u0001\u0000\u0000"+
		"\u0000\u03fe\u03ff\u0001\u0000\u0000\u0000\u03ff\u0404\u0001\u0000\u0000"+
		"\u0000\u0400\u03fe\u0001\u0000\u0000\u0000\u0401\u0403\u0003\u008aE\u0000"+
		"\u0402\u0401\u0001\u0000\u0000\u0000\u0403\u0406\u0001\u0000\u0000\u0000"+
		"\u0404\u0402\u0001\u0000\u0000\u0000\u0404\u0405\u0001\u0000\u0000\u0000"+
		"\u0405\u0407\u0001\u0000\u0000\u0000\u0406\u0404\u0001\u0000\u0000\u0000"+
		"\u0407\u0408\u0005|\u0000\u0000\u0408k\u0001\u0000\u0000\u0000\u0409\u040c"+
		"\u0003n7\u0000\u040a\u040c\u0003p8\u0000\u040b\u0409\u0001\u0000\u0000"+
		"\u0000\u040b\u040a\u0001\u0000\u0000\u0000\u040cm\u0001\u0000\u0000\u0000"+
		"\u040d\u040f\u0005\r\u0000\u0000\u040e\u0410\u0003v;\u0000\u040f\u040e"+
		"\u0001\u0000\u0000\u0000\u040f\u0410\u0001\u0000\u0000\u0000\u0410\u0411"+
		"\u0001\u0000\u0000\u0000\u0411\u0413\u0003\u011e\u008f\u0000\u0412\u0414"+
		"\u0003\u0080@\u0000\u0413\u0412\u0001\u0000\u0000\u0000\u0413\u0414\u0001"+
		"\u0000\u0000\u0000\u0414\u0415\u0001\u0000\u0000\u0000\u0415\u0419\u0005"+
		"{\u0000\u0000\u0416\u0418\u0003\u008cF\u0000\u0417\u0416\u0001\u0000\u0000"+
		"\u0000\u0418\u041b\u0001\u0000\u0000\u0000\u0419\u0417\u0001\u0000\u0000"+
		"\u0000\u0419\u041a\u0001\u0000\u0000\u0000\u041a\u041f\u0001\u0000\u0000"+
		"\u0000\u041b\u0419\u0001\u0000\u0000\u0000\u041c\u041e\u0003\u008aE\u0000"+
		"\u041d\u041c\u0001\u0000\u0000\u0000\u041e\u0421\u0001\u0000\u0000\u0000"+
		"\u041f\u041d\u0001\u0000\u0000\u0000\u041f\u0420\u0001\u0000\u0000\u0000"+
		"\u0420\u0422\u0001\u0000\u0000\u0000\u0421\u041f\u0001\u0000\u0000\u0000"+
		"\u0422\u0423\u0005|\u0000\u0000\u0423o\u0001\u0000\u0000\u0000\u0424\u0426"+
		"\u0005 \u0000\u0000\u0425\u0424\u0001\u0000\u0000\u0000\u0425\u0426\u0001"+
		"\u0000\u0000\u0000\u0426\u0427\u0001\u0000\u0000\u0000\u0427\u0429\u0005"+
		"\r\u0000\u0000\u0428\u042a\u0003v;\u0000\u0429\u0428\u0001\u0000\u0000"+
		"\u0000\u0429\u042a\u0001\u0000\u0000\u0000\u042a\u042c\u0001\u0000\u0000"+
		"\u0000\u042b\u042d\u0005V\u0000\u0000\u042c\u042b\u0001\u0000\u0000\u0000"+
		"\u042c\u042d\u0001\u0000\u0000\u0000\u042d\u042e\u0001\u0000\u0000\u0000"+
		"\u042e\u042f\u0003\u0170\u00b8\u0000\u042f\u0430\u0005\u000b\u0000\u0000"+
		"\u0430\u0432\u0003\u011e\u008f\u0000\u0431\u0433\u0003\u0080@\u0000\u0432"+
		"\u0431\u0001\u0000\u0000\u0000\u0432\u0433\u0001\u0000\u0000\u0000\u0433"+
		"\u0434\u0001\u0000\u0000\u0000\u0434\u0438\u0005{\u0000\u0000\u0435\u0437"+
		"\u0003\u008cF\u0000\u0436\u0435\u0001\u0000\u0000\u0000\u0437\u043a\u0001"+
		"\u0000\u0000\u0000\u0438\u0436\u0001\u0000\u0000\u0000\u0438\u0439\u0001"+
		"\u0000\u0000\u0000\u0439\u043e\u0001\u0000\u0000\u0000\u043a\u0438\u0001"+
		"\u0000\u0000\u0000\u043b\u043d\u0003\u008aE\u0000\u043c\u043b\u0001\u0000"+
		"\u0000\u0000\u043d\u0440\u0001\u0000\u0000\u0000\u043e\u043c\u0001\u0000"+
		"\u0000\u0000\u043e\u043f\u0001\u0000\u0000\u0000\u043f\u0441\u0001\u0000"+
		"\u0000\u0000\u0440\u043e\u0001\u0000\u0000\u0000\u0441\u0442\u0005|\u0000"+
		"\u0000\u0442q\u0001\u0000\u0000\u0000\u0443\u0445\u0005 \u0000\u0000\u0444"+
		"\u0443\u0001\u0000\u0000\u0000\u0444\u0445\u0001\u0000\u0000\u0000\u0445"+
		"\u0446\u0001\u0000\u0000\u0000\u0446\u0448\u0005\b\u0000\u0000\u0447\u0449"+
		"\u00038\u001c\u0000\u0448\u0447\u0001\u0000\u0000\u0000\u0448\u0449\u0001"+
		"\u0000\u0000\u0000\u0449\u044a\u0001\u0000\u0000\u0000\u044a\u044e\u0005"+
		"{\u0000\u0000\u044b\u044d\u0003\u008cF\u0000\u044c\u044b\u0001\u0000\u0000"+
		"\u0000\u044d\u0450\u0001\u0000\u0000\u0000\u044e\u044c\u0001\u0000\u0000"+
		"\u0000\u044e\u044f\u0001\u0000\u0000\u0000\u044f\u0454\u0001\u0000\u0000"+
		"\u0000\u0450\u044e\u0001\u0000\u0000\u0000\u0451\u0453\u0003t:\u0000\u0452"+
		"\u0451\u0001\u0000\u0000\u0000\u0453\u0456\u0001\u0000\u0000\u0000\u0454"+
		"\u0452\u0001\u0000\u0000\u0000\u0454\u0455\u0001\u0000\u0000\u0000\u0455"+
		"\u0457\u0001\u0000\u0000\u0000\u0456\u0454\u0001\u0000\u0000\u0000\u0457"+
		"\u0458\u0005|\u0000\u0000\u0458s\u0001\u0000\u0000\u0000\u0459\u045b\u0003"+
		"\u008eG\u0000\u045a\u0459\u0001\u0000\u0000\u0000\u045b\u045e\u0001\u0000"+
		"\u0000\u0000\u045c\u045a\u0001\u0000\u0000\u0000\u045c\u045d\u0001\u0000"+
		"\u0000\u0000\u045d\u0467\u0001\u0000\u0000\u0000\u045e\u045c\u0001\u0000"+
		"\u0000\u0000\u045f\u0468\u0003\n\u0005\u0000\u0460\u0462\u0003\u0178\u00bc"+
		"\u0000\u0461\u0460\u0001\u0000\u0000\u0000\u0461\u0462\u0001\u0000\u0000"+
		"\u0000\u0462\u0465\u0001\u0000\u0000\u0000\u0463\u0466\u0003h4\u0000\u0464"+
		"\u0466\u00034\u001a\u0000\u0465\u0463\u0001\u0000\u0000\u0000\u0465\u0464"+
		"\u0001\u0000\u0000\u0000\u0466\u0468\u0001\u0000\u0000\u0000\u0467\u045f"+
		"\u0001\u0000\u0000\u0000\u0467\u0461\u0001\u0000\u0000\u0000\u0468u\u0001"+
		"\u0000\u0000\u0000\u0469\u0476\u0005i\u0000\u0000\u046a\u046b\u0003x<"+
		"\u0000\u046b\u046c\u0005r\u0000\u0000\u046c\u046e\u0001\u0000\u0000\u0000"+
		"\u046d\u046a\u0001\u0000\u0000\u0000\u046e\u0471\u0001\u0000\u0000\u0000"+
		"\u046f\u046d\u0001\u0000\u0000\u0000\u046f\u0470\u0001\u0000\u0000\u0000"+
		"\u0470\u0472\u0001\u0000\u0000\u0000\u0471\u046f\u0001\u0000\u0000\u0000"+
		"\u0472\u0474\u0003x<\u0000\u0473\u0475\u0005r\u0000\u0000\u0474\u0473"+
		"\u0001\u0000\u0000\u0000\u0474\u0475\u0001\u0000\u0000\u0000\u0475\u0477"+
		"\u0001\u0000\u0000\u0000\u0476\u046f\u0001\u0000\u0000\u0000\u0476\u0477"+
		"\u0001\u0000\u0000\u0000\u0477\u0478\u0001\u0000\u0000\u0000\u0478\u0479"+
		"\u0005h\u0000\u0000\u0479w\u0001\u0000\u0000\u0000\u047a\u047c\u0003\u008e"+
		"G\u0000\u047b\u047a\u0001\u0000\u0000\u0000\u047c\u047f\u0001\u0000\u0000"+
		"\u0000\u047d\u047b\u0001\u0000\u0000\u0000\u047d\u047e\u0001\u0000\u0000"+
		"\u0000\u047e\u0483\u0001\u0000\u0000\u0000\u047f\u047d\u0001\u0000\u0000"+
		"\u0000\u0480\u0484\u0003z=\u0000\u0481\u0484\u0003|>\u0000\u0482\u0484"+
		"\u0003~?\u0000\u0483\u0480\u0001\u0000\u0000\u0000\u0483\u0481\u0001\u0000"+
		"\u0000\u0000\u0483\u0482\u0001\u0000\u0000\u0000\u0484y\u0001\u0000\u0000"+
		"\u0000\u0485\u0487\u0003\u008eG\u0000\u0486\u0485\u0001\u0000\u0000\u0000"+
		"\u0486\u0487\u0001\u0000\u0000\u0000\u0487\u0488\u0001\u0000\u0000\u0000"+
		"\u0488\u048b\u0005O\u0000\u0000\u0489\u048a\u0005t\u0000\u0000\u048a\u048c"+
		"\u0003\u014e\u00a7\u0000\u048b\u0489\u0001\u0000\u0000\u0000\u048b\u048c"+
		"\u0001\u0000\u0000\u0000\u048c{\u0001\u0000\u0000\u0000\u048d\u048f\u0003"+
		"\u008eG\u0000\u048e\u048d\u0001\u0000\u0000\u0000\u048e\u048f\u0001\u0000"+
		"\u0000\u0000\u048f\u0490\u0001\u0000\u0000\u0000\u0490\u0495\u0003\u017a"+
		"\u00bd\u0000\u0491\u0493\u0005t\u0000\u0000\u0492\u0494\u0003\u0148\u00a4"+
		"\u0000\u0493\u0492\u0001\u0000\u0000\u0000\u0493\u0494\u0001\u0000\u0000"+
		"\u0000\u0494\u0496\u0001\u0000\u0000\u0000\u0495\u0491\u0001\u0000\u0000"+
		"\u0000\u0495\u0496\u0001\u0000\u0000\u0000\u0496\u0499\u0001\u0000\u0000"+
		"\u0000\u0497\u0498\u0005e\u0000\u0000\u0498\u049a\u0003\u011e\u008f\u0000"+
		"\u0499\u0497\u0001\u0000\u0000\u0000\u0499\u049a\u0001\u0000\u0000\u0000"+
		"\u049a}\u0001\u0000\u0000\u0000\u049b\u049c\u0005\u0003\u0000\u0000\u049c"+
		"\u049d\u0003\u017a\u00bd\u0000\u049d\u049e\u0005t\u0000\u0000\u049e\u049f"+
		"\u0003\u011e\u008f\u0000\u049f\u007f\u0001\u0000\u0000\u0000\u04a0\u04a6"+
		"\u0005\"\u0000\u0000\u04a1\u04a2\u0003\u0082A\u0000\u04a2\u04a3\u0005"+
		"r\u0000\u0000\u04a3\u04a5\u0001\u0000\u0000\u0000\u04a4\u04a1\u0001\u0000"+
		"\u0000\u0000\u04a5\u04a8\u0001\u0000\u0000\u0000\u04a6\u04a4\u0001\u0000"+
		"\u0000\u0000\u04a6\u04a7\u0001\u0000\u0000\u0000\u04a7\u04aa\u0001\u0000"+
		"\u0000\u0000\u04a8\u04a6\u0001\u0000\u0000\u0000\u04a9\u04ab\u0003\u0082"+
		"A\u0000\u04aa\u04a9\u0001\u0000\u0000\u0000\u04aa\u04ab\u0001\u0000\u0000"+
		"\u0000\u04ab\u0081\u0001\u0000\u0000\u0000\u04ac\u04af\u0003\u0084B\u0000"+
		"\u04ad\u04af\u0003\u0086C\u0000\u04ae\u04ac\u0001\u0000\u0000\u0000\u04ae"+
		"\u04ad\u0001\u0000\u0000\u0000\u04af\u0083\u0001\u0000\u0000\u0000\u04b0"+
		"\u04b1\u0003\u0150\u00a8\u0000\u04b1\u04b2\u0005t\u0000\u0000\u04b2\u04b3"+
		"\u0003\u014e\u00a7\u0000\u04b3\u0085\u0001\u0000\u0000\u0000\u04b4\u04b6"+
		"\u0003\u0088D\u0000\u04b5\u04b4\u0001\u0000\u0000\u0000\u04b5\u04b6\u0001"+
		"\u0000\u0000\u0000\u04b6\u04b7\u0001\u0000\u0000\u0000\u04b7\u04b8\u0003"+
		"\u011e\u008f\u0000\u04b8\u04ba\u0005t\u0000\u0000\u04b9\u04bb\u0003\u0148"+
		"\u00a4\u0000\u04ba\u04b9\u0001\u0000\u0000\u0000\u04ba\u04bb\u0001\u0000"+
		"\u0000\u0000\u04bb\u0087\u0001\u0000\u0000\u0000\u04bc\u04bd\u0005\u000b"+
		"\u0000\u0000\u04bd\u04be\u0003v;\u0000\u04be\u0089\u0001\u0000\u0000\u0000"+
		"\u04bf\u04c1\u0003\u008eG\u0000\u04c0\u04bf\u0001\u0000\u0000\u0000\u04c1"+
		"\u04c4\u0001\u0000\u0000\u0000\u04c2\u04c0\u0001\u0000\u0000\u0000\u04c2"+
		"\u04c3\u0001\u0000\u0000\u0000\u04c3\u04ce\u0001\u0000\u0000\u0000\u04c4"+
		"\u04c2\u0001\u0000\u0000\u0000\u04c5\u04cf\u0003\n\u0005\u0000\u04c6\u04c8"+
		"\u0003\u0178\u00bc\u0000\u04c7\u04c6\u0001\u0000\u0000\u0000\u04c7\u04c8"+
		"\u0001\u0000\u0000\u0000\u04c8\u04cc\u0001\u0000\u0000\u0000\u04c9\u04cd"+
		"\u0003H$\u0000\u04ca\u04cd\u0003f3\u0000\u04cb\u04cd\u00034\u001a\u0000"+
		"\u04cc\u04c9\u0001\u0000\u0000\u0000\u04cc\u04ca\u0001\u0000\u0000\u0000"+
		"\u04cc\u04cb\u0001\u0000\u0000\u0000\u04cd\u04cf\u0001\u0000\u0000\u0000"+
		"\u04ce\u04c5\u0001\u0000\u0000\u0000\u04ce\u04c7\u0001\u0000\u0000\u0000"+
		"\u04cf\u008b\u0001\u0000\u0000\u0000\u04d0\u04d1\u0005x\u0000\u0000\u04d1"+
		"\u04d2\u0005V\u0000\u0000\u04d2\u04d3\u0005}\u0000\u0000\u04d3\u04d4\u0003"+
		"\u0090H\u0000\u04d4\u04d5\u0005~\u0000\u0000\u04d5\u008d\u0001\u0000\u0000"+
		"\u0000\u04d6\u04d7\u0005x\u0000\u0000\u04d7\u04d8\u0005}\u0000\u0000\u04d8"+
		"\u04d9\u0003\u0090H\u0000\u04d9\u04da\u0005~\u0000\u0000\u04da\u008f\u0001"+
		"\u0000\u0000\u0000\u04db\u04dd\u0003\u0152\u00a9\u0000\u04dc\u04de\u0003"+
		"\u0092I\u0000\u04dd\u04dc\u0001\u0000\u0000\u0000\u04dd\u04de\u0001\u0000"+
		"\u0000\u0000\u04de\u0091\u0001\u0000\u0000\u0000\u04df\u04e3\u0003\u0004"+
		"\u0002\u0000\u04e0\u04e1\u0005e\u0000\u0000\u04e1\u04e3\u0003\u00a2Q\u0000"+
		"\u04e2\u04df\u0001\u0000\u0000\u0000\u04e2\u04e0\u0001\u0000\u0000\u0000"+
		"\u04e3\u0093\u0001\u0000\u0000\u0000\u04e4\u04ea\u0005s\u0000\u0000\u04e5"+
		"\u04ea\u0003\"\u0011\u0000\u04e6\u04ea\u0003\u0096K\u0000\u04e7\u04ea"+
		"\u0003\u0098L\u0000\u04e8\u04ea\u0003\n\u0005\u0000\u04e9\u04e4\u0001"+
		"\u0000\u0000\u0000\u04e9\u04e5\u0001\u0000\u0000\u0000\u04e9\u04e6\u0001"+
		"\u0000\u0000\u0000\u04e9\u04e7\u0001\u0000\u0000\u0000\u04e9\u04e8\u0001"+
		"\u0000\u0000\u0000\u04ea\u0095\u0001\u0000\u0000\u0000\u04eb\u04ed\u0003"+
		"\u008eG\u0000\u04ec\u04eb\u0001\u0000\u0000\u0000\u04ed\u04f0\u0001\u0000"+
		"\u0000\u0000\u04ee\u04ec\u0001\u0000\u0000\u0000\u04ee\u04ef\u0001\u0000"+
		"\u0000\u0000\u04ef\u04f1\u0001\u0000\u0000\u0000\u04f0\u04ee\u0001\u0000"+
		"\u0000\u0000\u04f1\u04f2\u0005\u000f\u0000\u0000\u04f2\u04f5\u0003\u00f2"+
		"y\u0000\u04f3\u04f4\u0005t\u0000\u0000\u04f4\u04f6\u0003\u011e\u008f\u0000"+
		"\u04f5\u04f3\u0001\u0000\u0000\u0000\u04f5\u04f6\u0001\u0000\u0000\u0000"+
		"\u04f6\u04f9\u0001\u0000\u0000\u0000\u04f7\u04f8\u0005e\u0000\u0000\u04f8"+
		"\u04fa\u0003\u009aM\u0000\u04f9\u04f7\u0001\u0000\u0000\u0000\u04f9\u04fa"+
		"\u0001\u0000\u0000\u0000\u04fa\u04fb\u0001\u0000\u0000\u0000\u04fb\u04fc"+
		"\u0005s\u0000\u0000\u04fc\u0097\u0001\u0000\u0000\u0000\u04fd\u04fe\u0003"+
		"\u009aM\u0000\u04fe\u04ff\u0005s\u0000\u0000\u04ff\u0505\u0001\u0000\u0000"+
		"\u0000\u0500\u0502\u0003\u00a0P\u0000\u0501\u0503\u0005s\u0000\u0000\u0502"+
		"\u0501\u0001\u0000\u0000\u0000\u0502\u0503\u0001\u0000\u0000\u0000\u0503"+
		"\u0505\u0001\u0000\u0000\u0000\u0504\u04fd\u0001\u0000\u0000\u0000\u0504"+
		"\u0500\u0001\u0000\u0000\u0000\u0505\u0099\u0001\u0000\u0000\u0000\u0506"+
		"\u0508\u0006M\uffff\uffff\u0000\u0507\u0509\u0003\u008eG\u0000\u0508\u0507"+
		"\u0001\u0000\u0000\u0000\u0509\u050a\u0001\u0000\u0000\u0000\u050a\u0508"+
		"\u0001\u0000\u0000\u0000\u050a\u050b\u0001\u0000\u0000\u0000\u050b\u050c"+
		"\u0001\u0000\u0000\u0000\u050c\u050d\u0003\u009aM(\u050d\u0557\u0001\u0000"+
		"\u0000\u0000\u050e\u0557\u0003\u00a2Q\u0000\u050f\u0557\u0003\u00a4R\u0000"+
		"\u0510\u0512\u0007\u0002\u0000\u0000\u0511\u0513\u0005\u0014\u0000\u0000"+
		"\u0512\u0511\u0001\u0000\u0000\u0000\u0512\u0513\u0001\u0000\u0000\u0000"+
		"\u0513\u0514\u0001\u0000\u0000\u0000\u0514\u0557\u0003\u009aM\u001e\u0515"+
		"\u0516\u0005R\u0000\u0000\u0516\u0557\u0003\u009aM\u001d\u0517\u0518\u0007"+
		"\u0003\u0000\u0000\u0518\u0557\u0003\u009aM\u001c\u0519\u051b\u0005o\u0000"+
		"\u0000\u051a\u051c\u0003\u009aM\u0000\u051b\u051a\u0001\u0000\u0000\u0000"+
		"\u051b\u051c\u0001\u0000\u0000\u0000\u051c\u0557\u0001\u0000\u0000\u0000"+
		"\u051d\u051e\u0005q\u0000\u0000\u051e\u0557\u0003\u009aM\u000f\u051f\u0521"+
		"\u0005\u0004\u0000\u0000\u0520\u0522\u0005O\u0000\u0000\u0521\u0520\u0001"+
		"\u0000\u0000\u0000\u0521\u0522\u0001\u0000\u0000\u0000\u0522\u0524\u0001"+
		"\u0000\u0000\u0000\u0523\u0525\u0003\u009aM\u0000\u0524\u0523\u0001\u0000"+
		"\u0000\u0000\u0524\u0525\u0001\u0000\u0000\u0000\u0525\u0557\u0001\u0000"+
		"\u0000\u0000\u0526\u0528\u0005\u0002\u0000\u0000\u0527\u0529\u0005O\u0000"+
		"\u0000\u0528\u0527\u0001\u0000\u0000\u0000\u0528\u0529\u0001\u0000\u0000"+
		"\u0000\u0529\u052b\u0001\u0000\u0000\u0000\u052a\u052c\u0003\u009aM\u0000"+
		"\u052b\u052a\u0001\u0000\u0000\u0000\u052b\u052c\u0001\u0000\u0000\u0000"+
		"\u052c\u0557\u0001\u0000\u0000\u0000\u052d\u052f\u0005\u0017\u0000\u0000"+
		"\u052e\u0530\u0003\u009aM\u0000\u052f\u052e\u0001\u0000\u0000\u0000\u052f"+
		"\u0530\u0001\u0000\u0000\u0000\u0530\u0557\u0001\u0000\u0000\u0000\u0531"+
		"\u0535\u0005\u007f\u0000\u0000\u0532\u0534\u0003\u008cF\u0000\u0533\u0532"+
		"\u0001\u0000\u0000\u0000\u0534\u0537\u0001\u0000\u0000\u0000\u0535\u0533"+
		"\u0001\u0000\u0000\u0000\u0535\u0536\u0001\u0000\u0000\u0000\u0536\u0538"+
		"\u0001\u0000\u0000\u0000\u0537\u0535\u0001\u0000\u0000\u0000\u0538\u0539"+
		"\u0003\u009aM\u0000\u0539\u053a\u0005\u0080\u0000\u0000\u053a\u0557\u0001"+
		"\u0000\u0000\u0000\u053b\u053f\u0005}\u0000\u0000\u053c\u053e\u0003\u008c"+
		"F\u0000\u053d\u053c\u0001\u0000\u0000\u0000\u053e\u0541\u0001\u0000\u0000"+
		"\u0000\u053f\u053d\u0001\u0000\u0000\u0000\u053f\u0540\u0001\u0000\u0000"+
		"\u0000\u0540\u0543\u0001\u0000\u0000\u0000\u0541\u053f\u0001\u0000\u0000"+
		"\u0000\u0542\u0544\u0003\u00aeW\u0000\u0543\u0542\u0001\u0000\u0000\u0000"+
		"\u0543\u0544\u0001\u0000\u0000\u0000\u0544\u0545\u0001\u0000\u0000\u0000"+
		"\u0545\u0557\u0005~\u0000\u0000\u0546\u054a\u0005\u007f\u0000\u0000\u0547"+
		"\u0549\u0003\u008cF\u0000\u0548\u0547\u0001\u0000\u0000\u0000\u0549\u054c"+
		"\u0001\u0000\u0000\u0000\u054a\u0548\u0001\u0000\u0000\u0000\u054a\u054b"+
		"\u0001\u0000\u0000\u0000\u054b\u054e\u0001\u0000\u0000\u0000\u054c\u054a"+
		"\u0001\u0000\u0000\u0000\u054d\u054f\u0003\u00b0X\u0000\u054e\u054d\u0001"+
		"\u0000\u0000\u0000\u054e\u054f\u0001\u0000\u0000\u0000\u054f\u0550\u0001"+
		"\u0000\u0000\u0000\u0550\u0557\u0005\u0080\u0000\u0000\u0551\u0557\u0003"+
		"\u00b4Z\u0000\u0552\u0557\u0003\u00c2a\u0000\u0553\u0557\u0003\u00d0h"+
		"\u0000\u0554\u0557\u0003\u00a0P\u0000\u0555\u0557\u0003\u0002\u0001\u0000"+
		"\u0556\u0506\u0001\u0000\u0000\u0000\u0556\u050e\u0001\u0000\u0000\u0000"+
		"\u0556\u050f\u0001\u0000\u0000\u0000\u0556\u0510\u0001\u0000\u0000\u0000"+
		"\u0556\u0515\u0001\u0000\u0000\u0000\u0556\u0517\u0001\u0000\u0000\u0000"+
		"\u0556\u0519\u0001\u0000\u0000\u0000\u0556\u051d\u0001\u0000\u0000\u0000"+
		"\u0556\u051f\u0001\u0000\u0000\u0000\u0556\u0526\u0001\u0000\u0000\u0000"+
		"\u0556\u052d\u0001\u0000\u0000\u0000\u0556\u0531\u0001\u0000\u0000\u0000"+
		"\u0556\u053b\u0001\u0000\u0000\u0000\u0556\u0546\u0001\u0000\u0000\u0000"+
		"\u0556\u0551\u0001\u0000\u0000\u0000\u0556\u0552\u0001\u0000\u0000\u0000"+
		"\u0556\u0553\u0001\u0000\u0000\u0000\u0556\u0554\u0001\u0000\u0000\u0000"+
		"\u0556\u0555\u0001\u0000\u0000\u0000\u0557\u05ab\u0001\u0000\u0000\u0000"+
		"\u0558\u0559\n\u001a\u0000\u0000\u0559\u055a\u0007\u0004\u0000\u0000\u055a"+
		"\u05aa\u0003\u009aM\u001b\u055b\u055c\n\u0019\u0000\u0000\u055c\u055d"+
		"\u0007\u0005\u0000\u0000\u055d\u05aa\u0003\u009aM\u001a\u055e\u0561\n"+
		"\u0018\u0000\u0000\u055f\u0562\u0003\u0184\u00c2\u0000\u0560\u0562\u0003"+
		"\u0186\u00c3\u0000\u0561\u055f\u0001\u0000\u0000\u0000\u0561\u0560\u0001"+
		"\u0000\u0000\u0000\u0562\u0563\u0001\u0000\u0000\u0000\u0563\u0564\u0003"+
		"\u009aM\u0019\u0564\u05aa\u0001\u0000\u0000\u0000\u0565\u0566\n\u0017"+
		"\u0000\u0000\u0566\u0567\u0005W\u0000\u0000\u0567\u05aa\u0003\u009aM\u0018"+
		"\u0568\u0569\n\u0016\u0000\u0000\u0569\u056a\u0005U\u0000\u0000\u056a"+
		"\u05aa\u0003\u009aM\u0017\u056b\u056c\n\u0015\u0000\u0000\u056c\u056d"+
		"\u0005X\u0000\u0000\u056d\u05aa\u0003\u009aM\u0016\u056e\u056f\n\u0014"+
		"\u0000\u0000\u056f\u0570\u0003\u009cN\u0000\u0570\u0571\u0003\u009aM\u0015"+
		"\u0571\u05aa\u0001\u0000\u0000\u0000\u0572\u0573\n\u0013\u0000\u0000\u0573"+
		"\u0574\u0005Y\u0000\u0000\u0574\u05aa\u0003\u009aM\u0014\u0575\u0576\n"+
		"\u0012\u0000\u0000\u0576\u0577\u0005Z\u0000\u0000\u0577\u05aa\u0003\u009a"+
		"M\u0013\u0578\u0579\n\u000e\u0000\u0000\u0579\u057a\u0005q\u0000\u0000"+
		"\u057a\u05aa\u0003\u009aM\u000f\u057b\u057c\n\r\u0000\u0000\u057c\u057d"+
		"\u0005e\u0000\u0000\u057d\u05aa\u0003\u009aM\u000e\u057e\u057f\n\f\u0000"+
		"\u0000\u057f\u0580\u0003\u009eO\u0000\u0580\u0581\u0003\u009aM\r\u0581"+
		"\u05aa\u0001\u0000\u0000\u0000\u0582\u0583\n%\u0000\u0000\u0583\u0584"+
		"\u0005n\u0000\u0000\u0584\u0585\u0003\u0158\u00ac\u0000\u0585\u0587\u0005"+
		"\u007f\u0000\u0000\u0586\u0588\u0003\u00ceg\u0000\u0587\u0586\u0001\u0000"+
		"\u0000\u0000\u0587\u0588\u0001\u0000\u0000\u0000\u0588\u0589\u0001\u0000"+
		"\u0000\u0000\u0589\u058a\u0005\u0080\u0000\u0000\u058a\u05aa\u0001\u0000"+
		"\u0000\u0000\u058b\u058c\n$\u0000\u0000\u058c\u058d\u0005n\u0000\u0000"+
		"\u058d\u05aa\u0003\u017a\u00bd\u0000\u058e\u058f\n#\u0000\u0000\u058f"+
		"\u0590\u0005n\u0000\u0000\u0590\u05aa\u0003\u00b2Y\u0000\u0591\u0592\n"+
		"\"\u0000\u0000\u0592\u0593\u0005n\u0000\u0000\u0593\u05aa\u0005%\u0000"+
		"\u0000\u0594\u0595\n!\u0000\u0000\u0595\u0597\u0005\u007f\u0000\u0000"+
		"\u0596\u0598\u0003\u00ceg\u0000\u0597\u0596\u0001\u0000\u0000\u0000\u0597"+
		"\u0598\u0001\u0000\u0000\u0000\u0598\u0599\u0001\u0000\u0000\u0000\u0599"+
		"\u05aa\u0005\u0080\u0000\u0000\u059a\u059b\n \u0000\u0000\u059b\u059c"+
		"\u0005}\u0000\u0000\u059c\u059d\u0003\u009aM\u0000\u059d\u059e\u0005~"+
		"\u0000\u0000\u059e\u05aa\u0001\u0000\u0000\u0000\u059f\u05a0\n\u001f\u0000"+
		"\u0000\u05a0\u05aa\u0005z\u0000\u0000\u05a1\u05a2\n\u001b\u0000\u0000"+
		"\u05a2\u05a3\u0005\u0001\u0000\u0000\u05a3\u05aa\u0003\u0120\u0090\u0000"+
		"\u05a4\u05a5\n\u0011\u0000\u0000\u05a5\u05a7\u0005o\u0000\u0000\u05a6"+
		"\u05a8\u0003\u009aM\u0000\u05a7\u05a6\u0001\u0000\u0000\u0000\u05a7\u05a8"+
		"\u0001\u0000\u0000\u0000\u05a8\u05aa\u0001\u0000\u0000\u0000\u05a9\u0558"+
		"\u0001\u0000\u0000\u0000\u05a9\u055b\u0001\u0000\u0000\u0000\u05a9\u055e"+
		"\u0001\u0000\u0000\u0000\u05a9\u0565\u0001\u0000\u0000\u0000\u05a9\u0568"+
		"\u0001\u0000\u0000\u0000\u05a9\u056b\u0001\u0000\u0000\u0000\u05a9\u056e"+
		"\u0001\u0000\u0000\u0000\u05a9\u0572\u0001\u0000\u0000\u0000\u05a9\u0575"+
		"\u0001\u0000\u0000\u0000\u05a9\u0578\u0001\u0000\u0000\u0000\u05a9\u057b"+
		"\u0001\u0000\u0000\u0000\u05a9\u057e\u0001\u0000\u0000\u0000\u05a9\u0582"+
		"\u0001\u0000\u0000\u0000\u05a9\u058b\u0001\u0000\u0000\u0000\u05a9\u058e"+
		"\u0001\u0000\u0000\u0000\u05a9\u0591\u0001\u0000\u0000\u0000\u05a9\u0594"+
		"\u0001\u0000\u0000\u0000\u05a9\u059a\u0001\u0000\u0000\u0000\u05a9\u059f"+
		"\u0001\u0000\u0000\u0000\u05a9\u05a1\u0001\u0000\u0000\u0000\u05a9\u05a4"+
		"\u0001\u0000\u0000\u0000\u05aa\u05ad\u0001\u0000\u0000\u0000\u05ab\u05a9"+
		"\u0001\u0000\u0000\u0000\u05ab\u05ac\u0001\u0000\u0000\u0000\u05ac\u009b"+
		"\u0001\u0000\u0000\u0000\u05ad\u05ab\u0001\u0000\u0000\u0000\u05ae\u05af"+
		"\u0007\u0006\u0000\u0000\u05af\u009d\u0001\u0000\u0000\u0000\u05b0\u05b1"+
		"\u0007\u0007\u0000\u0000\u05b1\u009f\u0001\u0000\u0000\u0000\u05b2\u05b4"+
		"\u0003\u008eG\u0000\u05b3\u05b2\u0001\u0000\u0000\u0000\u05b4\u05b5\u0001"+
		"\u0000\u0000\u0000\u05b5\u05b3\u0001\u0000\u0000\u0000\u05b5\u05b6\u0001"+
		"\u0000\u0000\u0000\u05b6\u05b7\u0001\u0000\u0000\u0000\u05b7\u05b8\u0003"+
		"\u00a0P\u0000\u05b8\u05c1\u0001\u0000\u0000\u0000\u05b9\u05c1\u0003\u00a6"+
		"S\u0000\u05ba\u05c1\u0003\u00aaU\u0000\u05bb\u05c1\u0003\u00acV\u0000"+
		"\u05bc\u05c1\u0003\u00d6k\u0000\u05bd\u05c1\u0003\u00e2q\u0000\u05be\u05c1"+
		"\u0003\u00e4r\u0000\u05bf\u05c1\u0003\u00e6s\u0000\u05c0\u05b3\u0001\u0000"+
		"\u0000\u0000\u05c0\u05b9\u0001\u0000\u0000\u0000\u05c0\u05ba\u0001\u0000"+
		"\u0000\u0000\u05c0\u05bb\u0001\u0000\u0000\u0000\u05c0\u05bc\u0001\u0000"+
		"\u0000\u0000\u05c0\u05bd\u0001\u0000\u0000\u0000\u05c0\u05be\u0001\u0000"+
		"\u0000\u0000\u05c0\u05bf\u0001\u0000\u0000\u0000\u05c1\u00a1\u0001\u0000"+
		"\u0000\u0000\u05c2\u05c3\u0007\b\u0000\u0000\u05c3\u00a3\u0001\u0000\u0000"+
		"\u0000\u05c4\u05c7\u0003\u0156\u00ab\u0000\u05c5\u05c7\u0003\u016a\u00b5"+
		"\u0000\u05c6\u05c4\u0001\u0000\u0000\u0000\u05c6\u05c5\u0001\u0000\u0000"+
		"\u0000\u05c7\u00a5\u0001\u0000\u0000\u0000\u05c8\u05cc\u0005{\u0000\u0000"+
		"\u05c9\u05cb\u0003\u008cF\u0000\u05ca\u05c9\u0001\u0000\u0000\u0000\u05cb"+
		"\u05ce\u0001\u0000\u0000\u0000\u05cc\u05ca\u0001\u0000\u0000\u0000\u05cc"+
		"\u05cd\u0001\u0000\u0000\u0000\u05cd\u05d0\u0001\u0000\u0000\u0000\u05ce"+
		"\u05cc\u0001\u0000\u0000\u0000\u05cf\u05d1\u0003\u00a8T\u0000\u05d0\u05cf"+
		"\u0001\u0000\u0000\u0000\u05d0\u05d1\u0001\u0000\u0000\u0000\u05d1\u05d2"+
		"\u0001\u0000\u0000\u0000\u05d2\u05d3\u0005|\u0000\u0000\u05d3\u00a7\u0001"+
		"\u0000\u0000\u0000\u05d4\u05d6\u0003\u0094J\u0000\u05d5\u05d4\u0001\u0000"+
		"\u0000\u0000\u05d6\u05d7\u0001\u0000\u0000\u0000\u05d7\u05d5\u0001\u0000"+
		"\u0000\u0000\u05d7\u05d8\u0001\u0000\u0000\u0000\u05d8\u05da\u0001\u0000"+
		"\u0000\u0000\u05d9\u05db\u0003\u009aM\u0000\u05da\u05d9\u0001\u0000\u0000"+
		"\u0000\u05da\u05db\u0001\u0000\u0000\u0000\u05db\u05de\u0001\u0000\u0000"+
		"\u0000\u05dc\u05de\u0003\u009aM\u0000\u05dd\u05d5\u0001\u0000\u0000\u0000"+
		"\u05dd\u05dc\u0001\u0000\u0000\u0000\u05de\u00a9\u0001\u0000\u0000\u0000"+
		"\u05df\u05e1\u0005$\u0000\u0000\u05e0\u05e2\u0005\u0013\u0000\u0000\u05e1"+
		"\u05e0\u0001\u0000\u0000\u0000\u05e1\u05e2\u0001\u0000\u0000\u0000\u05e2"+
		"\u05e3\u0001\u0000\u0000\u0000\u05e3\u05e4\u0003\u00a6S\u0000\u05e4\u00ab"+
		"\u0001\u0000\u0000\u0000\u05e5\u05e6\u0005 \u0000\u0000\u05e6\u05e7\u0003"+
		"\u00a6S\u0000\u05e7\u00ad\u0001\u0000\u0000\u0000\u05e8\u05ed\u0003\u009a"+
		"M\u0000\u05e9\u05ea\u0005r\u0000\u0000\u05ea\u05ec\u0003\u009aM\u0000"+
		"\u05eb\u05e9\u0001\u0000\u0000\u0000\u05ec\u05ef\u0001\u0000\u0000\u0000"+
		"\u05ed\u05eb\u0001\u0000\u0000\u0000\u05ed\u05ee\u0001\u0000\u0000\u0000"+
		"\u05ee\u05f1\u0001\u0000\u0000\u0000\u05ef\u05ed\u0001\u0000\u0000\u0000"+
		"\u05f0\u05f2\u0005r\u0000\u0000\u05f1\u05f0\u0001\u0000\u0000\u0000\u05f1"+
		"\u05f2\u0001\u0000\u0000\u0000\u05f2\u05f8\u0001\u0000\u0000\u0000\u05f3"+
		"\u05f4\u0003\u009aM\u0000\u05f4\u05f5\u0005s\u0000\u0000\u05f5\u05f6\u0003"+
		"\u009aM\u0000\u05f6\u05f8\u0001\u0000\u0000\u0000\u05f7\u05e8\u0001\u0000"+
		"\u0000\u0000\u05f7\u05f3\u0001\u0000\u0000\u0000\u05f8\u00af\u0001\u0000"+
		"\u0000\u0000\u05f9\u05fa\u0003\u009aM\u0000\u05fa\u05fb\u0005r\u0000\u0000"+
		"\u05fb\u05fd\u0001\u0000\u0000\u0000\u05fc\u05f9\u0001\u0000\u0000\u0000"+
		"\u05fd\u05fe\u0001\u0000\u0000\u0000\u05fe\u05fc\u0001\u0000\u0000\u0000"+
		"\u05fe\u05ff\u0001\u0000\u0000\u0000\u05ff\u0601\u0001\u0000\u0000\u0000"+
		"\u0600\u0602\u0003\u009aM\u0000\u0601\u0600\u0001\u0000\u0000\u0000\u0601"+
		"\u0602\u0001\u0000\u0000\u0000\u0602\u00b1\u0001\u0000\u0000\u0000\u0603"+
		"\u0604\u0005I\u0000\u0000\u0604\u00b3\u0001\u0000\u0000\u0000\u0605\u0609"+
		"\u0003\u00b6[\u0000\u0606\u0609\u0003\u00be_\u0000\u0607\u0609\u0003\u00c0"+
		"`\u0000\u0608\u0605\u0001\u0000\u0000\u0000\u0608\u0606\u0001\u0000\u0000"+
		"\u0000\u0608\u0607\u0001\u0000\u0000\u0000\u0609\u00b5\u0001\u0000\u0000"+
		"\u0000\u060a\u060b\u0003\u0156\u00ab\u0000\u060b\u060f\u0005{\u0000\u0000"+
		"\u060c\u060e\u0003\u008cF\u0000\u060d\u060c\u0001\u0000\u0000\u0000\u060e"+
		"\u0611\u0001\u0000\u0000\u0000\u060f\u060d\u0001\u0000\u0000\u0000\u060f"+
		"\u0610\u0001\u0000\u0000\u0000\u0610\u0614\u0001\u0000\u0000\u0000\u0611"+
		"\u060f\u0001\u0000\u0000\u0000\u0612\u0615\u0003\u00b8\\\u0000\u0613\u0615"+
		"\u0003\u00bc^\u0000\u0614\u0612\u0001\u0000\u0000\u0000\u0614\u0613\u0001"+
		"\u0000\u0000\u0000\u0614\u0615\u0001\u0000\u0000\u0000\u0615\u0616\u0001"+
		"\u0000\u0000\u0000\u0616\u0617\u0005|\u0000\u0000\u0617\u00b7\u0001\u0000"+
		"\u0000\u0000\u0618\u061d\u0003\u00ba]\u0000\u0619\u061a\u0005r\u0000\u0000"+
		"\u061a\u061c\u0003\u00ba]\u0000\u061b\u0619\u0001\u0000\u0000\u0000\u061c"+
		"\u061f\u0001\u0000\u0000\u0000\u061d\u061b\u0001\u0000\u0000\u0000\u061d"+
		"\u061e\u0001\u0000\u0000\u0000\u061e\u0625\u0001\u0000\u0000\u0000\u061f"+
		"\u061d\u0001\u0000\u0000\u0000\u0620\u0621\u0005r\u0000\u0000\u0621\u0626"+
		"\u0003\u00bc^\u0000\u0622\u0624\u0005r\u0000\u0000\u0623\u0622\u0001\u0000"+
		"\u0000\u0000\u0623\u0624\u0001\u0000\u0000\u0000\u0624\u0626\u0001\u0000"+
		"\u0000\u0000\u0625\u0620\u0001\u0000\u0000\u0000\u0625\u0623\u0001\u0000"+
		"\u0000\u0000\u0626\u00b9\u0001\u0000\u0000\u0000\u0627\u0629\u0003\u008e"+
		"G\u0000\u0628\u0627\u0001\u0000\u0000\u0000\u0629\u062c\u0001\u0000\u0000"+
		"\u0000\u062a\u0628\u0001\u0000\u0000\u0000\u062a\u062b\u0001\u0000\u0000"+
		"\u0000\u062b\u0635\u0001\u0000\u0000\u0000\u062c\u062a\u0001\u0000\u0000"+
		"\u0000\u062d\u0636\u0003\u017a\u00bd\u0000\u062e\u0631\u0003\u017a\u00bd"+
		"\u0000\u062f\u0631\u0003\u00b2Y\u0000\u0630\u062e\u0001\u0000\u0000\u0000"+
		"\u0630\u062f\u0001\u0000\u0000\u0000\u0631\u0632\u0001\u0000\u0000\u0000"+
		"\u0632\u0633\u0005t\u0000\u0000\u0633\u0634\u0003\u009aM\u0000\u0634\u0636"+
		"\u0001\u0000\u0000\u0000\u0635\u062d\u0001\u0000\u0000\u0000\u0635\u0630"+
		"\u0001\u0000\u0000\u0000\u0636\u00bb\u0001\u0000\u0000\u0000\u0637\u0638"+
		"\u0005o\u0000\u0000\u0638\u0639\u0003\u009aM\u0000\u0639\u00bd\u0001\u0000"+
		"\u0000\u0000\u063a\u063b\u0003\u0156\u00ab\u0000\u063b\u063f\u0005\u007f"+
		"\u0000\u0000\u063c\u063e\u0003\u008cF\u0000\u063d\u063c\u0001\u0000\u0000"+
		"\u0000\u063e\u0641\u0001\u0000\u0000\u0000\u063f\u063d\u0001\u0000\u0000"+
		"\u0000\u063f\u0640\u0001\u0000\u0000\u0000\u0640\u064d\u0001\u0000\u0000"+
		"\u0000\u0641\u063f\u0001\u0000\u0000\u0000\u0642\u0647\u0003\u009aM\u0000"+
		"\u0643\u0644\u0005r\u0000\u0000\u0644\u0646\u0003\u009aM\u0000\u0645\u0643"+
		"\u0001\u0000\u0000\u0000\u0646\u0649\u0001\u0000\u0000\u0000\u0647\u0645"+
		"\u0001\u0000\u0000\u0000\u0647\u0648\u0001\u0000\u0000\u0000\u0648\u064b"+
		"\u0001\u0000\u0000\u0000\u0649\u0647\u0001\u0000\u0000\u0000\u064a\u064c"+
		"\u0005r\u0000\u0000\u064b\u064a\u0001\u0000\u0000\u0000\u064b\u064c\u0001"+
		"\u0000\u0000\u0000\u064c\u064e\u0001\u0000\u0000\u0000\u064d\u0642\u0001"+
		"\u0000\u0000\u0000\u064d\u064e\u0001\u0000\u0000\u0000\u064e\u064f\u0001"+
		"\u0000\u0000\u0000\u064f\u0650\u0005\u0080\u0000\u0000\u0650\u00bf\u0001"+
		"\u0000\u0000\u0000\u0651\u0652\u0003\u0156\u00ab\u0000\u0652\u00c1\u0001"+
		"\u0000\u0000\u0000\u0653\u0657\u0003\u00c4b\u0000\u0654\u0657\u0003\u00ca"+
		"e\u0000\u0655\u0657\u0003\u00ccf\u0000\u0656\u0653\u0001\u0000\u0000\u0000"+
		"\u0656\u0654\u0001\u0000\u0000\u0000\u0656\u0655\u0001\u0000\u0000\u0000"+
		"\u0657\u00c3\u0001\u0000\u0000\u0000\u0658\u0659\u0003\u0156\u00ab\u0000"+
		"\u0659\u065b\u0005{\u0000\u0000\u065a\u065c\u0003\u00c6c\u0000\u065b\u065a"+
		"\u0001\u0000\u0000\u0000\u065b\u065c\u0001\u0000\u0000\u0000\u065c\u065d"+
		"\u0001\u0000\u0000\u0000\u065d\u065e\u0005|\u0000\u0000\u065e\u00c5\u0001"+
		"\u0000\u0000\u0000\u065f\u0664\u0003\u00c8d\u0000\u0660\u0661\u0005r\u0000"+
		"\u0000\u0661\u0663\u0003\u00c8d\u0000\u0662\u0660\u0001\u0000\u0000\u0000"+
		"\u0663\u0666\u0001\u0000\u0000\u0000\u0664\u0662\u0001\u0000\u0000\u0000"+
		"\u0664\u0665\u0001\u0000\u0000\u0000\u0665\u0668\u0001\u0000\u0000\u0000"+
		"\u0666\u0664\u0001\u0000\u0000\u0000\u0667\u0669\u0005r\u0000\u0000\u0668"+
		"\u0667\u0001\u0000\u0000\u0000\u0668\u0669\u0001\u0000\u0000\u0000\u0669"+
		"\u00c7\u0001\u0000\u0000\u0000\u066a\u0673\u0003\u017a\u00bd\u0000\u066b"+
		"\u066e\u0003\u017a\u00bd\u0000\u066c\u066e\u0003\u00b2Y\u0000\u066d\u066b"+
		"\u0001\u0000\u0000\u0000\u066d\u066c\u0001\u0000\u0000\u0000\u066e\u066f"+
		"\u0001\u0000\u0000\u0000\u066f\u0670\u0005t\u0000\u0000\u0670\u0671\u0003"+
		"\u009aM\u0000\u0671\u0673\u0001\u0000\u0000\u0000\u0672\u066a\u0001\u0000"+
		"\u0000\u0000\u0672\u066d\u0001\u0000\u0000\u0000\u0673\u00c9\u0001\u0000"+
		"\u0000\u0000\u0674\u0675\u0003\u0156\u00ab\u0000\u0675\u0681\u0005\u007f"+
		"\u0000\u0000\u0676\u067b\u0003\u009aM\u0000\u0677\u0678\u0005r\u0000\u0000"+
		"\u0678\u067a\u0003\u009aM\u0000\u0679\u0677\u0001\u0000\u0000\u0000\u067a"+
		"\u067d\u0001\u0000\u0000\u0000\u067b\u0679\u0001\u0000\u0000\u0000\u067b"+
		"\u067c\u0001\u0000\u0000\u0000\u067c\u067f\u0001\u0000\u0000\u0000\u067d"+
		"\u067b\u0001\u0000\u0000\u0000\u067e\u0680\u0005r\u0000\u0000\u067f\u067e"+
		"\u0001\u0000\u0000\u0000\u067f\u0680\u0001\u0000\u0000\u0000\u0680\u0682"+
		"\u0001\u0000\u0000\u0000\u0681\u0676\u0001\u0000\u0000\u0000\u0681\u0682"+
		"\u0001\u0000\u0000\u0000\u0682\u0683\u0001\u0000\u0000\u0000\u0683\u0684"+
		"\u0005\u0080\u0000\u0000\u0684\u00cb\u0001\u0000\u0000\u0000\u0685\u0686"+
		"\u0003\u0156\u00ab\u0000\u0686\u00cd\u0001\u0000\u0000\u0000\u0687\u068c"+
		"\u0003\u009aM\u0000\u0688\u0689\u0005r\u0000\u0000\u0689\u068b\u0003\u009a"+
		"M\u0000\u068a\u0688\u0001\u0000\u0000\u0000\u068b\u068e\u0001\u0000\u0000"+
		"\u0000\u068c\u068a\u0001\u0000\u0000\u0000\u068c\u068d\u0001\u0000\u0000"+
		"\u0000\u068d\u0690\u0001\u0000\u0000\u0000\u068e\u068c\u0001\u0000\u0000"+
		"\u0000\u068f\u0691\u0005r\u0000\u0000\u0690\u068f\u0001\u0000\u0000\u0000"+
		"\u0690\u0691\u0001\u0000\u0000\u0000\u0691\u00cf\u0001\u0000\u0000\u0000"+
		"\u0692\u0694\u0005\u0013\u0000\u0000\u0693\u0692\u0001\u0000\u0000\u0000"+
		"\u0693\u0694\u0001\u0000\u0000\u0000\u0694\u069b\u0001\u0000\u0000\u0000"+
		"\u0695\u069c\u0005Z\u0000\u0000\u0696\u0698\u0005X\u0000\u0000\u0697\u0699"+
		"\u0003\u00d2i\u0000\u0698\u0697\u0001\u0000\u0000\u0000\u0698\u0699\u0001"+
		"\u0000\u0000\u0000\u0699\u069a\u0001\u0000\u0000\u0000\u069a\u069c\u0005"+
		"X\u0000\u0000\u069b\u0695\u0001\u0000\u0000\u0000\u069b\u0696\u0001\u0000"+
		"\u0000\u0000\u069c\u06a2\u0001\u0000\u0000\u0000\u069d\u06a3\u0003\u009a"+
		"M\u0000\u069e\u069f\u0005v\u0000\u0000\u069f\u06a0\u0003\u0120\u0090\u0000"+
		"\u06a0\u06a1\u0003\u00a6S\u0000\u06a1\u06a3\u0001\u0000\u0000\u0000\u06a2"+
		"\u069d\u0001\u0000\u0000\u0000\u06a2\u069e\u0001\u0000\u0000\u0000\u06a3"+
		"\u00d1\u0001\u0000\u0000\u0000\u06a4\u06a9\u0003\u00d4j\u0000\u06a5\u06a6"+
		"\u0005r\u0000\u0000\u06a6\u06a8\u0003\u00d4j\u0000\u06a7\u06a5\u0001\u0000"+
		"\u0000\u0000\u06a8\u06ab\u0001\u0000\u0000\u0000\u06a9\u06a7\u0001\u0000"+
		"\u0000\u0000\u06a9\u06aa\u0001\u0000\u0000\u0000\u06aa\u06ad\u0001\u0000"+
		"\u0000\u0000\u06ab\u06a9\u0001\u0000\u0000\u0000\u06ac\u06ae\u0005r\u0000"+
		"\u0000\u06ad\u06ac\u0001\u0000\u0000\u0000\u06ad\u06ae\u0001\u0000\u0000"+
		"\u0000\u06ae\u00d3\u0001\u0000\u0000\u0000\u06af\u06b1\u0003\u008eG\u0000"+
		"\u06b0\u06af\u0001\u0000\u0000\u0000\u06b1\u06b4\u0001\u0000\u0000\u0000"+
		"\u06b2\u06b0\u0001\u0000\u0000\u0000\u06b2\u06b3\u0001\u0000\u0000\u0000"+
		"\u06b3\u06b5\u0001\u0000\u0000\u0000\u06b4\u06b2\u0001\u0000\u0000\u0000"+
		"\u06b5\u06b8\u0003\u00f0x\u0000\u06b6\u06b7\u0005t\u0000\u0000\u06b7\u06b9"+
		"\u0003\u011e\u008f\u0000\u06b8\u06b6\u0001\u0000\u0000\u0000\u06b8\u06b9"+
		"\u0001\u0000\u0000\u0000\u06b9\u00d5\u0001\u0000\u0000\u0000\u06ba\u06bc"+
		"\u0003\u00e0p\u0000\u06bb\u06ba\u0001\u0000\u0000\u0000\u06bb\u06bc\u0001"+
		"\u0000\u0000\u0000\u06bc\u06c1\u0001\u0000\u0000\u0000\u06bd\u06c2\u0003"+
		"\u00d8l\u0000\u06be\u06c2\u0003\u00dam\u0000\u06bf\u06c2\u0003\u00dcn"+
		"\u0000\u06c0\u06c2\u0003\u00deo\u0000\u06c1\u06bd\u0001\u0000\u0000\u0000"+
		"\u06c1\u06be\u0001\u0000\u0000\u0000\u06c1\u06bf\u0001\u0000\u0000\u0000"+
		"\u06c1\u06c0\u0001\u0000\u0000\u0000\u06c2\u00d7\u0001\u0000\u0000\u0000"+
		"\u06c3\u06c4\u0005\u0010\u0000\u0000\u06c4\u06c5\u0003\u00a6S\u0000\u06c5"+
		"\u00d9\u0001\u0000\u0000\u0000\u06c6\u06c7\u0005#\u0000\u0000\u06c7\u06c8"+
		"\u0003\u009aM\u0000\u06c8\u06c9\u0003\u00a6S\u0000\u06c9\u00db\u0001\u0000"+
		"\u0000\u0000\u06ca\u06cb\u0005#\u0000\u0000\u06cb\u06cc\u0005\u000f\u0000"+
		"\u0000\u06cc\u06cd\u0003\u00f0x\u0000\u06cd\u06ce\u0005e\u0000\u0000\u06ce"+
		"\u06cf\u0003\u009aM\u0000\u06cf\u06d0\u0003\u00a6S\u0000\u06d0\u00dd\u0001"+
		"\u0000\u0000\u0000\u06d1\u06d2\u0005\u000b\u0000\u0000\u06d2\u06d3\u0003"+
		"\u00f0x\u0000\u06d3\u06d4\u0005\u000e\u0000\u0000\u06d4\u06d5\u0003\u009a"+
		"M\u0000\u06d5\u06d6\u0003\u00a6S\u0000\u06d6\u00df\u0001\u0000\u0000\u0000"+
		"\u06d7\u06d8\u0005O\u0000\u0000\u06d8\u06d9\u0005t\u0000\u0000\u06d9\u00e1"+
		"\u0001\u0000\u0000\u0000\u06da\u06db\u0005\f\u0000\u0000\u06db\u06dc\u0003"+
		"\u009aM\u0000\u06dc\u06e3\u0003\u00a6S\u0000\u06dd\u06e1\u0005\u0006\u0000"+
		"\u0000\u06de\u06e2\u0003\u00a6S\u0000\u06df\u06e2\u0003\u00e2q\u0000\u06e0"+
		"\u06e2\u0003\u00e4r\u0000\u06e1\u06de\u0001\u0000\u0000\u0000\u06e1\u06df"+
		"\u0001\u0000\u0000\u0000\u06e1\u06e0\u0001\u0000\u0000\u0000\u06e2\u06e4"+
		"\u0001\u0000\u0000\u0000\u06e3\u06dd\u0001\u0000\u0000\u0000\u06e3\u06e4"+
		"\u0001\u0000\u0000\u0000\u06e4\u00e3\u0001\u0000\u0000\u0000\u06e5\u06e6"+
		"\u0005\f\u0000\u0000\u06e6\u06e7\u0005\u000f\u0000\u0000\u06e7\u06e8\u0003"+
		"\u00f0x\u0000\u06e8\u06e9\u0005e\u0000\u0000\u06e9\u06ea\u0003\u009aM"+
		"\u0000\u06ea\u06f1\u0003\u00a6S\u0000\u06eb\u06ef\u0005\u0006\u0000\u0000"+
		"\u06ec\u06f0\u0003\u00a6S\u0000\u06ed\u06f0\u0003\u00e2q\u0000\u06ee\u06f0"+
		"\u0003\u00e4r\u0000\u06ef\u06ec\u0001\u0000\u0000\u0000\u06ef\u06ed\u0001"+
		"\u0000\u0000\u0000\u06ef\u06ee\u0001\u0000\u0000\u0000\u06f0\u06f2\u0001"+
		"\u0000\u0000\u0000\u06f1\u06eb\u0001\u0000\u0000\u0000\u06f1\u06f2\u0001"+
		"\u0000\u0000\u0000\u06f2\u00e5\u0001\u0000\u0000\u0000\u06f3\u06f4\u0005"+
		"\u0011\u0000\u0000\u06f4\u06f5\u0003\u009aM\u0000\u06f5\u06f9\u0005{\u0000"+
		"\u0000\u06f6\u06f8\u0003\u008cF\u0000\u06f7\u06f6\u0001\u0000\u0000\u0000"+
		"\u06f8\u06fb\u0001\u0000\u0000\u0000\u06f9\u06f7\u0001\u0000\u0000\u0000"+
		"\u06f9\u06fa\u0001\u0000\u0000\u0000\u06fa\u06fd\u0001\u0000\u0000\u0000"+
		"\u06fb\u06f9\u0001\u0000\u0000\u0000\u06fc\u06fe\u0003\u00e8t\u0000\u06fd"+
		"\u06fc\u0001\u0000\u0000\u0000\u06fd\u06fe\u0001\u0000\u0000\u0000\u06fe"+
		"\u06ff\u0001\u0000\u0000\u0000\u06ff\u0700\u0005|\u0000\u0000\u0700\u00e7"+
		"\u0001\u0000\u0000\u0000\u0701\u0702\u0003\u00ecv\u0000\u0702\u0703\u0005"+
		"w\u0000\u0000\u0703\u0704\u0003\u00eau\u0000\u0704\u0706\u0001\u0000\u0000"+
		"\u0000\u0705\u0701\u0001\u0000\u0000\u0000\u0706\u0709\u0001\u0000\u0000"+
		"\u0000\u0707\u0705\u0001\u0000\u0000\u0000\u0707\u0708\u0001\u0000\u0000"+
		"\u0000\u0708\u070a\u0001\u0000\u0000\u0000\u0709\u0707\u0001\u0000\u0000"+
		"\u0000\u070a\u070b\u0003\u00ecv\u0000\u070b\u070c\u0005w\u0000\u0000\u070c"+
		"\u070e\u0003\u009aM\u0000\u070d\u070f\u0005r\u0000\u0000\u070e\u070d\u0001"+
		"\u0000\u0000\u0000\u070e\u070f\u0001\u0000\u0000\u0000\u070f\u00e9\u0001"+
		"\u0000\u0000\u0000\u0710\u0711\u0003\u009aM\u0000\u0711\u0712\u0005r\u0000"+
		"\u0000\u0712\u0718\u0001\u0000\u0000\u0000\u0713\u0715\u0003\u00a0P\u0000"+
		"\u0714\u0716\u0005r\u0000\u0000\u0715\u0714\u0001\u0000\u0000\u0000\u0715"+
		"\u0716\u0001\u0000\u0000\u0000\u0716\u0718\u0001\u0000\u0000\u0000\u0717"+
		"\u0710\u0001\u0000\u0000\u0000\u0717\u0713\u0001\u0000\u0000\u0000\u0718"+
		"\u00eb\u0001\u0000\u0000\u0000\u0719\u071b\u0003\u008eG\u0000\u071a\u0719"+
		"\u0001\u0000\u0000\u0000\u071b\u071e\u0001\u0000\u0000\u0000\u071c\u071a"+
		"\u0001\u0000\u0000\u0000\u071c\u071d\u0001\u0000\u0000\u0000\u071d\u071f"+
		"\u0001\u0000\u0000\u0000\u071e\u071c\u0001\u0000\u0000\u0000\u071f\u0721"+
		"\u0003\u00f0x\u0000\u0720\u0722\u0003\u00eew\u0000\u0721\u0720\u0001\u0000"+
		"\u0000\u0000\u0721\u0722\u0001\u0000\u0000\u0000\u0722\u00ed\u0001\u0000"+
		"\u0000\u0000\u0723\u0724\u0005\f\u0000\u0000\u0724\u0725\u0003\u009aM"+
		"\u0000\u0725\u00ef\u0001\u0000\u0000\u0000\u0726\u0728\u0005X\u0000\u0000"+
		"\u0727\u0726\u0001\u0000\u0000\u0000\u0727\u0728\u0001\u0000\u0000\u0000"+
		"\u0728\u0729\u0001\u0000\u0000\u0000\u0729\u072e\u0003\u00f2y\u0000\u072a"+
		"\u072b\u0005X\u0000\u0000\u072b\u072d\u0003\u00f2y\u0000\u072c\u072a\u0001"+
		"\u0000\u0000\u0000\u072d\u0730\u0001\u0000\u0000\u0000\u072e\u072c\u0001"+
		"\u0000\u0000\u0000\u072e\u072f\u0001\u0000\u0000\u0000\u072f\u00f1\u0001"+
		"\u0000\u0000\u0000\u0730\u072e\u0001\u0000\u0000\u0000\u0731\u0734\u0003"+
		"\u00f4z\u0000\u0732\u0734\u0003\u00fe\u007f\u0000\u0733\u0731\u0001\u0000"+
		"\u0000\u0000\u0733\u0732\u0001\u0000\u0000\u0000\u0734\u00f3\u0001\u0000"+
		"\u0000\u0000\u0735\u0742\u0003\u00f6{\u0000\u0736\u0742\u0003\u00f8|\u0000"+
		"\u0737\u0742\u0003\u00fa}\u0000\u0738\u0742\u0003\u00fc~\u0000\u0739\u0742"+
		"\u0003\u0102\u0081\u0000\u073a\u0742\u0003\u0104\u0082\u0000\u073b\u0742"+
		"\u0003\u010e\u0087\u0000\u073c\u0742\u0003\u0112\u0089\u0000\u073d\u0742"+
		"\u0003\u0116\u008b\u0000\u073e\u0742\u0003\u0118\u008c\u0000\u073f\u0742"+
		"\u0003\u011c\u008e\u0000\u0740\u0742\u0003\u0002\u0001\u0000\u0741\u0735"+
		"\u0001\u0000\u0000\u0000\u0741\u0736\u0001\u0000\u0000\u0000\u0741\u0737"+
		"\u0001\u0000\u0000\u0000\u0741\u0738\u0001\u0000\u0000\u0000\u0741\u0739"+
		"\u0001\u0000\u0000\u0000\u0741\u073a\u0001\u0000\u0000\u0000\u0741\u073b"+
		"\u0001\u0000\u0000\u0000\u0741\u073c\u0001\u0000\u0000\u0000\u0741\u073d"+
		"\u0001\u0000\u0000\u0000\u0741\u073e\u0001\u0000\u0000\u0000\u0741\u073f"+
		"\u0001\u0000\u0000\u0000\u0741\u0740\u0001\u0000\u0000\u0000\u0742\u00f5"+
		"\u0001\u0000\u0000\u0000\u0743\u0754\u0005\u001e\u0000\u0000\u0744\u0754"+
		"\u0005\t\u0000\u0000\u0745\u0754\u0005C\u0000\u0000\u0746\u0754\u0005"+
		"F\u0000\u0000\u0747\u0754\u0005D\u0000\u0000\u0748\u0754\u0005E\u0000"+
		"\u0000\u0749\u0754\u0005G\u0000\u0000\u074a\u0754\u0005H\u0000\u0000\u074b"+
		"\u074d\u0005Q\u0000\u0000\u074c\u074b\u0001\u0000\u0000\u0000\u074c\u074d"+
		"\u0001\u0000\u0000\u0000\u074d\u074e\u0001\u0000\u0000\u0000\u074e\u0754"+
		"\u0005I\u0000\u0000\u074f\u0751\u0005Q\u0000\u0000\u0750\u074f\u0001\u0000"+
		"\u0000\u0000\u0750\u0751\u0001\u0000\u0000\u0000\u0751\u0752\u0001\u0000"+
		"\u0000\u0000\u0752\u0754\u0005N\u0000\u0000\u0753\u0743\u0001\u0000\u0000"+
		"\u0000\u0753\u0744\u0001\u0000\u0000\u0000\u0753\u0745\u0001\u0000\u0000"+
		"\u0000\u0753\u0746\u0001\u0000\u0000\u0000\u0753\u0747\u0001\u0000\u0000"+
		"\u0000\u0753\u0748\u0001\u0000\u0000\u0000\u0753\u0749\u0001\u0000\u0000"+
		"\u0000\u0753\u074a\u0001\u0000\u0000\u0000\u0753\u074c\u0001\u0000\u0000"+
		"\u0000\u0753\u0750\u0001\u0000\u0000\u0000\u0754\u00f7\u0001\u0000\u0000"+
		"\u0000\u0755\u0757\u0005\u0016\u0000\u0000\u0756\u0755\u0001\u0000\u0000"+
		"\u0000\u0756\u0757\u0001\u0000\u0000\u0000\u0757\u0759\u0001\u0000\u0000"+
		"\u0000\u0758\u075a\u0005\u0014\u0000\u0000\u0759\u0758\u0001\u0000\u0000"+
		"\u0000\u0759\u075a\u0001\u0000\u0000\u0000\u075a\u075b\u0001\u0000\u0000"+
		"\u0000\u075b\u075e\u0003\u017a\u00bd\u0000\u075c\u075d\u0005l\u0000\u0000"+
		"\u075d\u075f\u0003\u00f0x\u0000\u075e\u075c\u0001\u0000\u0000\u0000\u075e"+
		"\u075f\u0001\u0000\u0000\u0000\u075f\u00f9\u0001\u0000\u0000\u0000\u0760"+
		"\u0761\u0005m\u0000\u0000\u0761\u00fb\u0001\u0000\u0000\u0000\u0762\u0763"+
		"\u0005o\u0000\u0000\u0763\u00fd\u0001\u0000\u0000\u0000\u0764\u0765\u0003"+
		"\u0100\u0080\u0000\u0765\u0766\u0005q\u0000\u0000\u0766\u0767\u0003\u0100"+
		"\u0080\u0000\u0767\u0770\u0001\u0000\u0000\u0000\u0768\u0769\u0003\u0100"+
		"\u0080\u0000\u0769\u076a\u0005o\u0000\u0000\u076a\u0770\u0001\u0000\u0000"+
		"\u0000\u076b\u076c\u0003\u0100\u0080\u0000\u076c\u076d\u0005p\u0000\u0000"+
		"\u076d\u076e\u0003\u0100\u0080\u0000\u076e\u0770\u0001\u0000\u0000\u0000"+
		"\u076f\u0764\u0001\u0000\u0000\u0000\u076f\u0768\u0001\u0000\u0000\u0000"+
		"\u076f\u076b\u0001\u0000\u0000\u0000\u0770\u00ff\u0001\u0000\u0000\u0000"+
		"\u0771\u077d\u0005C\u0000\u0000\u0772\u077d\u0005F\u0000\u0000\u0773\u0775"+
		"\u0005Q\u0000\u0000\u0774\u0773\u0001\u0000\u0000\u0000\u0774\u0775\u0001"+
		"\u0000\u0000\u0000\u0775\u0776\u0001\u0000\u0000\u0000\u0776\u077d\u0005"+
		"I\u0000\u0000\u0777\u0779\u0005Q\u0000\u0000\u0778\u0777\u0001\u0000\u0000"+
		"\u0000\u0778\u0779\u0001\u0000\u0000\u0000\u0779\u077a\u0001\u0000\u0000"+
		"\u0000\u077a\u077d\u0005N\u0000\u0000\u077b\u077d\u0003\u011c\u008e\u0000"+
		"\u077c\u0771\u0001\u0000\u0000\u0000\u077c\u0772\u0001\u0000\u0000\u0000"+
		"\u077c\u0774\u0001\u0000\u0000\u0000\u077c\u0778\u0001\u0000\u0000\u0000"+
		"\u077c\u077b\u0001\u0000\u0000\u0000\u077d\u0101\u0001\u0000\u0000\u0000"+
		"\u077e\u0780\u0007\u0002\u0000\u0000\u077f\u0781\u0005\u0014\u0000\u0000"+
		"\u0780\u077f\u0001\u0000\u0000\u0000\u0780\u0781\u0001\u0000\u0000\u0000"+
		"\u0781\u0782\u0001\u0000\u0000\u0000\u0782\u0783\u0003\u00f4z\u0000\u0783"+
		"\u0103\u0001\u0000\u0000\u0000\u0784\u0785\u0003\u0156\u00ab\u0000\u0785"+
		"\u0787\u0005{\u0000\u0000\u0786\u0788\u0003\u0106\u0083\u0000\u0787\u0786"+
		"\u0001\u0000\u0000\u0000\u0787\u0788\u0001\u0000\u0000\u0000\u0788\u0789"+
		"\u0001\u0000\u0000\u0000\u0789\u078a\u0005|\u0000\u0000\u078a\u0105\u0001"+
		"\u0000\u0000\u0000\u078b\u0790\u0003\u0108\u0084\u0000\u078c\u078e\u0005"+
		"r\u0000\u0000\u078d\u078f\u0003\u010c\u0086\u0000\u078e\u078d\u0001\u0000"+
		"\u0000\u0000\u078e\u078f\u0001\u0000\u0000\u0000\u078f\u0791\u0001\u0000"+
		"\u0000\u0000\u0790\u078c\u0001\u0000\u0000\u0000\u0790\u0791\u0001\u0000"+
		"\u0000\u0000\u0791\u0794\u0001\u0000\u0000\u0000\u0792\u0794\u0003\u010c"+
		"\u0086\u0000\u0793\u078b\u0001\u0000\u0000\u0000\u0793\u0792\u0001\u0000"+
		"\u0000\u0000\u0794\u0107\u0001\u0000\u0000\u0000\u0795\u079a\u0003\u010a"+
		"\u0085\u0000\u0796\u0797\u0005r\u0000\u0000\u0797\u0799\u0003\u010a\u0085"+
		"\u0000\u0798\u0796\u0001\u0000\u0000\u0000\u0799\u079c\u0001\u0000\u0000"+
		"\u0000\u079a\u0798\u0001\u0000\u0000\u0000\u079a\u079b\u0001\u0000\u0000"+
		"\u0000\u079b\u0109\u0001\u0000\u0000\u0000\u079c\u079a\u0001\u0000\u0000"+
		"\u0000\u079d\u079f\u0003\u008eG\u0000\u079e\u079d\u0001\u0000\u0000\u0000"+
		"\u079f\u07a2\u0001\u0000\u0000\u0000\u07a0\u079e\u0001\u0000\u0000\u0000"+
		"\u07a0\u07a1\u0001\u0000\u0000\u0000\u07a1\u07b2\u0001\u0000\u0000\u0000"+
		"\u07a2\u07a0\u0001\u0000\u0000\u0000\u07a3\u07a4\u0003\u00b2Y\u0000\u07a4"+
		"\u07a5\u0005t\u0000\u0000\u07a5\u07a6\u0003\u00f0x\u0000\u07a6\u07b3\u0001"+
		"\u0000\u0000\u0000\u07a7\u07a8\u0003\u017a\u00bd\u0000\u07a8\u07a9\u0005"+
		"t\u0000\u0000\u07a9\u07aa\u0003\u00f0x\u0000\u07aa\u07b3\u0001\u0000\u0000"+
		"\u0000\u07ab\u07ad\u0005\u0016\u0000\u0000\u07ac\u07ab\u0001\u0000\u0000"+
		"\u0000\u07ac\u07ad\u0001\u0000\u0000\u0000\u07ad\u07af\u0001\u0000\u0000"+
		"\u0000\u07ae\u07b0\u0005\u0014\u0000\u0000\u07af\u07ae\u0001\u0000\u0000"+
		"\u0000\u07af\u07b0\u0001\u0000\u0000\u0000\u07b0\u07b1\u0001\u0000\u0000"+
		"\u0000\u07b1\u07b3\u0003\u017a\u00bd\u0000\u07b2\u07a3\u0001\u0000\u0000"+
		"\u0000\u07b2\u07a7\u0001\u0000\u0000\u0000\u07b2\u07ac\u0001\u0000\u0000"+
		"\u0000\u07b3\u010b\u0001\u0000\u0000\u0000\u07b4\u07b6\u0003\u008eG\u0000"+
		"\u07b5\u07b4\u0001\u0000\u0000\u0000\u07b6\u07b9\u0001\u0000\u0000\u0000"+
		"\u07b7\u07b5\u0001\u0000\u0000\u0000\u07b7\u07b8\u0001\u0000\u0000\u0000"+
		"\u07b8\u07ba\u0001\u0000\u0000\u0000\u07b9\u07b7\u0001\u0000\u0000\u0000"+
		"\u07ba\u07bb\u0005o\u0000\u0000\u07bb\u010d\u0001\u0000\u0000\u0000\u07bc"+
		"\u07bd\u0003\u0156\u00ab\u0000\u07bd\u07bf\u0005\u007f\u0000\u0000\u07be"+
		"\u07c0\u0003\u0110\u0088\u0000\u07bf\u07be\u0001\u0000\u0000\u0000\u07bf"+
		"\u07c0\u0001\u0000\u0000\u0000\u07c0\u07c1\u0001\u0000\u0000\u0000\u07c1"+
		"\u07c2\u0005\u0080\u0000\u0000\u07c2\u010f\u0001\u0000\u0000\u0000\u07c3"+
		"\u07c8\u0003\u00f0x\u0000\u07c4\u07c5\u0005r\u0000\u0000\u07c5\u07c7\u0003"+
		"\u00f0x\u0000\u07c6\u07c4\u0001\u0000\u0000\u0000\u07c7\u07ca\u0001\u0000"+
		"\u0000\u0000\u07c8\u07c6\u0001\u0000\u0000\u0000\u07c8\u07c9\u0001\u0000"+
		"\u0000\u0000\u07c9\u07cc\u0001\u0000\u0000\u0000\u07ca\u07c8\u0001\u0000"+
		"\u0000\u0000\u07cb\u07cd\u0005r\u0000\u0000\u07cc\u07cb\u0001\u0000\u0000"+
		"\u0000\u07cc\u07cd\u0001\u0000\u0000\u0000\u07cd\u0111\u0001\u0000\u0000"+
		"\u0000\u07ce\u07d0\u0005\u007f\u0000\u0000\u07cf\u07d1\u0003\u0114\u008a"+
		"\u0000\u07d0\u07cf\u0001\u0000\u0000\u0000\u07d0\u07d1\u0001\u0000\u0000"+
		"\u0000\u07d1\u07d2\u0001\u0000\u0000\u0000\u07d2\u07d3\u0005\u0080\u0000"+
		"\u0000\u07d3\u0113\u0001\u0000\u0000\u0000\u07d4\u07d5\u0003\u00f0x\u0000"+
		"\u07d5\u07d6\u0005r\u0000\u0000\u07d6\u07e3\u0001\u0000\u0000\u0000\u07d7"+
		"\u07e3\u0003\u00fc~\u0000\u07d8\u07db\u0003\u00f0x\u0000\u07d9\u07da\u0005"+
		"r\u0000\u0000\u07da\u07dc\u0003\u00f0x\u0000\u07db\u07d9\u0001\u0000\u0000"+
		"\u0000\u07dc\u07dd\u0001\u0000\u0000\u0000\u07dd\u07db\u0001\u0000\u0000"+
		"\u0000\u07dd\u07de\u0001\u0000\u0000\u0000\u07de\u07e0\u0001\u0000\u0000"+
		"\u0000\u07df\u07e1\u0005r\u0000\u0000\u07e0\u07df\u0001\u0000\u0000\u0000"+
		"\u07e0\u07e1\u0001\u0000\u0000\u0000\u07e1\u07e3\u0001\u0000\u0000\u0000"+
		"\u07e2\u07d4\u0001\u0000\u0000\u0000\u07e2\u07d7\u0001\u0000\u0000\u0000"+
		"\u07e2\u07d8\u0001\u0000\u0000\u0000\u07e3\u0115\u0001\u0000\u0000\u0000"+
		"\u07e4\u07e5\u0005\u007f\u0000\u0000\u07e5\u07e6\u0003\u00f0x\u0000\u07e6"+
		"\u07e7\u0005\u0080\u0000\u0000\u07e7\u0117\u0001\u0000\u0000\u0000\u07e8"+
		"\u07ea\u0005}\u0000\u0000\u07e9\u07eb\u0003\u011a\u008d\u0000\u07ea\u07e9"+
		"\u0001\u0000\u0000\u0000\u07ea\u07eb\u0001\u0000\u0000\u0000\u07eb\u07ec"+
		"\u0001\u0000\u0000\u0000\u07ec\u07ed\u0005~\u0000\u0000\u07ed\u0119\u0001"+
		"\u0000\u0000\u0000\u07ee\u07f3\u0003\u00f0x\u0000\u07ef\u07f0\u0005r\u0000"+
		"\u0000\u07f0\u07f2\u0003\u00f0x\u0000\u07f1\u07ef\u0001\u0000\u0000\u0000"+
		"\u07f2\u07f5\u0001\u0000\u0000\u0000\u07f3\u07f1\u0001\u0000\u0000\u0000"+
		"\u07f3\u07f4\u0001\u0000\u0000\u0000\u07f4\u07f7\u0001\u0000\u0000\u0000"+
		"\u07f5\u07f3\u0001\u0000\u0000\u0000\u07f6\u07f8\u0005r\u0000\u0000\u07f7"+
		"\u07f6\u0001\u0000\u0000\u0000\u07f7\u07f8\u0001\u0000\u0000\u0000\u07f8"+
		"\u011b\u0001\u0000\u0000\u0000\u07f9\u07fc\u0003\u0156\u00ab\u0000\u07fa"+
		"\u07fc\u0003\u016a\u00b5\u0000\u07fb\u07f9\u0001\u0000\u0000\u0000\u07fb"+
		"\u07fa\u0001\u0000\u0000\u0000\u07fc\u011d\u0001\u0000\u0000\u0000\u07fd"+
		"\u0801\u0003\u0120\u0090\u0000\u07fe\u0801\u0003\u0142\u00a1\u0000\u07ff"+
		"\u0801\u0003\u013e\u009f\u0000\u0800\u07fd\u0001\u0000\u0000\u0000\u0800"+
		"\u07fe\u0001\u0000\u0000\u0000\u0800\u07ff\u0001\u0000\u0000\u0000\u0801"+
		"\u011f\u0001\u0000\u0000\u0000\u0802\u0811\u0003\u0122\u0091\u0000\u0803"+
		"\u0811\u0003\u0144\u00a2\u0000\u0804\u0811\u0003\u0140\u00a0\u0000\u0805"+
		"\u0811\u0003\u0170\u00b8\u0000\u0806\u0811\u0003\u0126\u0093\u0000\u0807"+
		"\u0811\u0003\u0124\u0092\u0000\u0808\u0811\u0003\u012e\u0097\u0000\u0809"+
		"\u0811\u0003\u012c\u0096\u0000\u080a\u0811\u0003\u0128\u0094\u0000\u080b"+
		"\u0811\u0003\u012a\u0095\u0000\u080c\u0811\u0003\u0146\u00a3\u0000\u080d"+
		"\u0811\u0003\u016e\u00b7\u0000\u080e\u0811\u0003\u0130\u0098\u0000\u080f"+
		"\u0811\u0003\u0002\u0001\u0000\u0810\u0802\u0001\u0000\u0000\u0000\u0810"+
		"\u0803\u0001\u0000\u0000\u0000\u0810\u0804\u0001\u0000\u0000\u0000\u0810"+
		"\u0805\u0001\u0000\u0000\u0000\u0810\u0806\u0001\u0000\u0000\u0000\u0810"+
		"\u0807\u0001\u0000\u0000\u0000\u0810\u0808\u0001\u0000\u0000\u0000\u0810"+
		"\u0809\u0001\u0000\u0000\u0000\u0810\u080a\u0001\u0000\u0000\u0000\u0810"+
		"\u080b\u0001\u0000\u0000\u0000\u0810\u080c\u0001\u0000\u0000\u0000\u0810"+
		"\u080d\u0001\u0000\u0000\u0000\u0810\u080e\u0001\u0000\u0000\u0000\u0810"+
		"\u080f\u0001\u0000\u0000\u0000\u0811\u0121\u0001\u0000\u0000\u0000\u0812"+
		"\u0813\u0005\u007f\u0000\u0000\u0813\u0814\u0003\u011e\u008f\u0000\u0814"+
		"\u0815\u0005\u0080\u0000\u0000\u0815\u0123\u0001\u0000\u0000\u0000\u0816"+
		"\u0817\u0005V\u0000\u0000\u0817\u0125\u0001\u0000\u0000\u0000\u0818\u0823"+
		"\u0005\u007f\u0000\u0000\u0819\u081a\u0003\u011e\u008f\u0000\u081a\u081b"+
		"\u0005r\u0000\u0000\u081b\u081d\u0001\u0000\u0000\u0000\u081c\u0819\u0001"+
		"\u0000\u0000\u0000\u081d\u081e\u0001\u0000\u0000\u0000\u081e\u081c\u0001"+
		"\u0000\u0000\u0000\u081e\u081f\u0001\u0000\u0000\u0000\u081f\u0821\u0001"+
		"\u0000\u0000\u0000\u0820\u0822\u0003\u011e\u008f\u0000\u0821\u0820\u0001"+
		"\u0000\u0000\u0000\u0821\u0822\u0001\u0000\u0000\u0000\u0822\u0824\u0001"+
		"\u0000\u0000\u0000\u0823\u081c\u0001\u0000\u0000\u0000\u0823\u0824\u0001"+
		"\u0000\u0000\u0000\u0824\u0825\u0001\u0000\u0000\u0000\u0825\u0826\u0005"+
		"\u0080\u0000\u0000\u0826\u0127\u0001\u0000\u0000\u0000\u0827\u0828\u0005"+
		"}\u0000\u0000\u0828\u0829\u0003\u011e\u008f\u0000\u0829\u082a\u0005s\u0000"+
		"\u0000\u082a\u082b\u0003\u009aM\u0000\u082b\u082c\u0005~\u0000\u0000\u082c"+
		"\u0129\u0001\u0000\u0000\u0000\u082d\u082e\u0005}\u0000\u0000\u082e\u082f"+
		"\u0003\u011e\u008f\u0000\u082f\u0830\u0005~\u0000\u0000\u0830\u012b\u0001"+
		"\u0000\u0000\u0000\u0831\u0833\u0005W\u0000\u0000\u0832\u0834\u0003\u0150"+
		"\u00a8\u0000\u0833\u0832\u0001\u0000\u0000\u0000\u0833\u0834\u0001\u0000"+
		"\u0000\u0000\u0834\u0836\u0001\u0000\u0000\u0000\u0835\u0837\u0005\u0014"+
		"\u0000\u0000\u0836\u0835\u0001\u0000\u0000\u0000\u0836\u0837\u0001\u0000"+
		"\u0000\u0000\u0837\u0838\u0001\u0000\u0000\u0000\u0838\u0839\u0003\u0120"+
		"\u0090\u0000\u0839\u012d\u0001\u0000\u0000\u0000\u083a\u083b\u0005R\u0000"+
		"\u0000\u083b\u083c\u0007\t\u0000\u0000\u083c\u083d\u0003\u0120\u0090\u0000"+
		"\u083d\u012f\u0001\u0000\u0000\u0000\u083e\u0840\u0003\u0088D\u0000\u083f"+
		"\u083e\u0001\u0000\u0000\u0000\u083f\u0840\u0001\u0000\u0000\u0000\u0840"+
		"\u0841\u0001\u0000\u0000\u0000\u0841\u0842\u0003\u0132\u0099\u0000\u0842"+
		"\u0843\u0005\n\u0000\u0000\u0843\u0845\u0005\u007f\u0000\u0000\u0844\u0846"+
		"\u0003\u0136\u009b\u0000\u0845\u0844\u0001\u0000\u0000\u0000\u0845\u0846"+
		"\u0001\u0000\u0000\u0000\u0846\u0847\u0001\u0000\u0000\u0000\u0847\u0849"+
		"\u0005\u0080\u0000\u0000\u0848\u084a\u0003\u0134\u009a\u0000\u0849\u0848"+
		"\u0001\u0000\u0000\u0000\u0849\u084a\u0001\u0000\u0000\u0000\u084a\u0131"+
		"\u0001\u0000\u0000\u0000\u084b\u084d\u0005 \u0000\u0000\u084c\u084b\u0001"+
		"\u0000\u0000\u0000\u084c\u084d\u0001\u0000\u0000\u0000\u084d\u0852\u0001"+
		"\u0000\u0000\u0000\u084e\u0850\u0005\b\u0000\u0000\u084f\u0851\u00038"+
		"\u001c\u0000\u0850\u084f\u0001\u0000\u0000\u0000\u0850\u0851\u0001\u0000"+
		"\u0000\u0000\u0851\u0853\u0001\u0000\u0000\u0000\u0852\u084e\u0001\u0000"+
		"\u0000\u0000\u0852\u0853\u0001\u0000\u0000\u0000\u0853\u0133\u0001\u0000"+
		"\u0000\u0000\u0854\u0855\u0005v\u0000\u0000\u0855\u0856\u0003\u0120\u0090"+
		"\u0000\u0856\u0135\u0001\u0000\u0000\u0000\u0857\u085a\u0003\u0138\u009c"+
		"\u0000\u0858\u085a\u0003\u013c\u009e\u0000\u0859\u0857\u0001\u0000\u0000"+
		"\u0000\u0859\u0858\u0001\u0000\u0000\u0000\u085a\u0137\u0001\u0000\u0000"+
		"\u0000\u085b\u0860\u0003\u013a\u009d\u0000\u085c\u085d\u0005r\u0000\u0000"+
		"\u085d\u085f\u0003\u013a\u009d\u0000\u085e\u085c\u0001\u0000\u0000\u0000"+
		"\u085f\u0862\u0001\u0000\u0000\u0000\u0860\u085e\u0001\u0000\u0000\u0000"+
		"\u0860\u0861\u0001\u0000\u0000\u0000\u0861\u0864\u0001\u0000\u0000\u0000"+
		"\u0862\u0860\u0001\u0000\u0000\u0000\u0863\u0865\u0005r\u0000\u0000\u0864"+
		"\u0863\u0001\u0000\u0000\u0000\u0864\u0865\u0001\u0000\u0000\u0000\u0865"+
		"\u0139\u0001\u0000\u0000\u0000\u0866\u0868\u0003\u008eG\u0000\u0867\u0866"+
		"\u0001\u0000\u0000\u0000\u0868\u086b\u0001\u0000\u0000\u0000\u0869\u0867"+
		"\u0001\u0000\u0000\u0000\u0869\u086a\u0001\u0000\u0000\u0000\u086a\u0871"+
		"\u0001\u0000\u0000\u0000\u086b\u0869\u0001\u0000\u0000\u0000\u086c\u086f"+
		"\u0003\u017a\u00bd\u0000\u086d\u086f\u0005m\u0000\u0000\u086e\u086c\u0001"+
		"\u0000\u0000\u0000\u086e\u086d\u0001\u0000\u0000\u0000\u086f\u0870\u0001"+
		"\u0000\u0000\u0000\u0870\u0872\u0005t\u0000\u0000\u0871\u086e\u0001\u0000"+
		"\u0000\u0000\u0871\u0872\u0001\u0000\u0000\u0000\u0872\u0873\u0001\u0000"+
		"\u0000\u0000\u0873\u0874\u0003\u011e\u008f\u0000\u0874\u013b\u0001\u0000"+
		"\u0000\u0000\u0875\u0876\u0003\u013a\u009d\u0000\u0876\u0877\u0005r\u0000"+
		"\u0000\u0877\u0879\u0001\u0000\u0000\u0000\u0878\u0875\u0001\u0000\u0000"+
		"\u0000\u0879\u087c\u0001\u0000\u0000\u0000\u087a\u0878\u0001\u0000\u0000"+
		"\u0000\u087a\u087b\u0001\u0000\u0000\u0000\u087b\u087d\u0001\u0000\u0000"+
		"\u0000\u087c\u087a\u0001\u0000\u0000\u0000\u087d\u087e\u0003\u013a\u009d"+
		"\u0000\u087e\u0882\u0005r\u0000\u0000\u087f\u0881\u0003\u008eG\u0000\u0880"+
		"\u087f\u0001\u0000\u0000\u0000\u0881\u0884\u0001\u0000\u0000\u0000\u0882"+
		"\u0880\u0001\u0000\u0000\u0000\u0882\u0883\u0001\u0000\u0000\u0000\u0883"+
		"\u0885\u0001\u0000\u0000\u0000\u0884\u0882\u0001\u0000\u0000\u0000\u0885"+
		"\u0886\u0005p\u0000\u0000\u0886\u013d\u0001\u0000\u0000\u0000\u0887\u0889"+
		"\u0005&\u0000\u0000\u0888\u0887\u0001\u0000\u0000\u0000\u0888\u0889\u0001"+
		"\u0000\u0000\u0000\u0889\u088a\u0001\u0000\u0000\u0000\u088a\u088b\u0003"+
		"\u0148\u00a4\u0000\u088b\u013f\u0001\u0000\u0000\u0000\u088c\u088e\u0005"+
		"&\u0000\u0000\u088d\u088c\u0001\u0000\u0000\u0000\u088d\u088e\u0001\u0000"+
		"\u0000\u0000\u088e\u088f\u0001\u0000\u0000\u0000\u088f\u0890\u0003\u014c"+
		"\u00a6\u0000\u0890\u0141\u0001\u0000\u0000\u0000\u0891\u0892\u0005\r\u0000"+
		"\u0000\u0892\u0893\u0003\u0148\u00a4\u0000\u0893\u0143\u0001\u0000\u0000"+
		"\u0000\u0894\u0895\u0005\r\u0000\u0000\u0895\u0896\u0003\u014c\u00a6\u0000"+
		"\u0896\u0145\u0001\u0000\u0000\u0000\u0897\u0898\u0005m\u0000\u0000\u0898"+
		"\u0147\u0001\u0000\u0000\u0000\u0899\u089e\u0003\u014a\u00a5\u0000\u089a"+
		"\u089b\u0005P\u0000\u0000\u089b\u089d\u0003\u014a\u00a5\u0000\u089c\u089a"+
		"\u0001\u0000\u0000\u0000\u089d\u08a0\u0001\u0000\u0000\u0000\u089e\u089c"+
		"\u0001\u0000\u0000\u0000\u089e\u089f\u0001\u0000\u0000\u0000\u089f\u08a2"+
		"\u0001\u0000\u0000\u0000\u08a0\u089e\u0001\u0000\u0000\u0000\u08a1\u08a3"+
		"\u0005P\u0000\u0000\u08a2\u08a1\u0001\u0000\u0000\u0000\u08a2\u08a3\u0001"+
		"\u0000\u0000\u0000\u08a3\u0149\u0001\u0000\u0000\u0000\u08a4\u08a7\u0003"+
		"\u0150\u00a8\u0000\u08a5\u08a7\u0003\u014c\u00a6\u0000\u08a6\u08a4\u0001"+
		"\u0000\u0000\u0000\u08a6\u08a5\u0001\u0000\u0000\u0000\u08a7\u014b\u0001"+
		"\u0000\u0000\u0000\u08a8\u08aa\u0005z\u0000\u0000\u08a9\u08a8\u0001\u0000"+
		"\u0000\u0000\u08a9\u08aa\u0001\u0000\u0000\u0000\u08aa\u08ac\u0001\u0000"+
		"\u0000\u0000\u08ab\u08ad\u0003\u0088D\u0000\u08ac\u08ab\u0001\u0000\u0000"+
		"\u0000\u08ac\u08ad\u0001\u0000\u0000\u0000\u08ad\u08ae\u0001\u0000\u0000"+
		"\u0000\u08ae\u08ba\u0003\u0170\u00b8\u0000\u08af\u08b1\u0005\u007f\u0000"+
		"\u0000\u08b0\u08b2\u0005z\u0000\u0000\u08b1\u08b0\u0001\u0000\u0000\u0000"+
		"\u08b1\u08b2\u0001\u0000\u0000\u0000\u08b2\u08b4\u0001\u0000\u0000\u0000"+
		"\u08b3\u08b5\u0003\u0088D\u0000\u08b4\u08b3\u0001\u0000\u0000\u0000\u08b4"+
		"\u08b5\u0001\u0000\u0000\u0000\u08b5\u08b6\u0001\u0000\u0000\u0000\u08b6"+
		"\u08b7\u0003\u0170\u00b8\u0000\u08b7\u08b8\u0005\u0080\u0000\u0000\u08b8"+
		"\u08ba\u0001\u0000\u0000\u0000\u08b9\u08a9\u0001\u0000\u0000\u0000\u08b9"+
		"\u08af\u0001\u0000\u0000\u0000\u08ba\u014d\u0001\u0000\u0000\u0000\u08bb"+
		"\u08bc\u0003\u0150\u00a8\u0000\u08bc\u08bd\u0005P\u0000\u0000\u08bd\u08bf"+
		"\u0001\u0000\u0000\u0000\u08be\u08bb\u0001\u0000\u0000\u0000\u08bf\u08c2"+
		"\u0001\u0000\u0000\u0000\u08c0\u08be\u0001\u0000\u0000\u0000\u08c0\u08c1"+
		"\u0001\u0000\u0000\u0000\u08c1\u08c4\u0001\u0000\u0000\u0000\u08c2\u08c0"+
		"\u0001\u0000\u0000\u0000\u08c3\u08c5\u0003\u0150\u00a8\u0000\u08c4\u08c3"+
		"\u0001\u0000\u0000\u0000\u08c4\u08c5\u0001\u0000\u0000\u0000\u08c5\u014f"+
		"\u0001\u0000\u0000\u0000\u08c6\u08c7\u0007\n\u0000\u0000\u08c7\u0151\u0001"+
		"\u0000\u0000\u0000\u08c8\u08ca\u0005u\u0000\u0000\u08c9\u08c8\u0001\u0000"+
		"\u0000\u0000\u08c9\u08ca\u0001\u0000\u0000\u0000\u08ca\u08cb\u0001\u0000"+
		"\u0000\u0000\u08cb\u08d0\u0003\u0154\u00aa\u0000\u08cc\u08cd\u0005u\u0000"+
		"\u0000\u08cd\u08cf\u0003\u0154\u00aa\u0000\u08ce\u08cc\u0001\u0000\u0000"+
		"\u0000\u08cf\u08d2\u0001\u0000\u0000\u0000\u08d0\u08ce\u0001\u0000\u0000"+
		"\u0000\u08d0\u08d1\u0001\u0000\u0000\u0000\u08d1\u0153\u0001\u0000\u0000"+
		"\u0000\u08d2\u08d0\u0001\u0000\u0000\u0000\u08d3\u08d9\u0003\u017a\u00bd"+
		"\u0000\u08d4\u08d9\u0005\u001c\u0000\u0000\u08d5\u08d9\u0005\u0018\u0000"+
		"\u0000\u08d6\u08d9\u0005\u0005\u0000\u0000\u08d7\u08d9\u00058\u0000\u0000"+
		"\u08d8\u08d3\u0001\u0000\u0000\u0000\u08d8\u08d4\u0001\u0000\u0000\u0000"+
		"\u08d8\u08d5\u0001\u0000\u0000\u0000\u08d8\u08d6\u0001\u0000\u0000\u0000"+
		"\u08d8\u08d7\u0001\u0000\u0000\u0000\u08d9\u0155\u0001\u0000\u0000\u0000"+
		"\u08da\u08dc\u0005u\u0000\u0000\u08db\u08da\u0001\u0000\u0000\u0000\u08db"+
		"\u08dc\u0001\u0000\u0000\u0000\u08dc\u08dd\u0001\u0000\u0000\u0000\u08dd"+
		"\u08e2\u0003\u0158\u00ac\u0000\u08de\u08df\u0005u\u0000\u0000\u08df\u08e1"+
		"\u0003\u0158\u00ac\u0000\u08e0\u08de\u0001\u0000\u0000\u0000\u08e1\u08e4"+
		"\u0001\u0000\u0000\u0000\u08e2\u08e0\u0001\u0000\u0000\u0000\u08e2\u08e3"+
		"\u0001\u0000\u0000\u0000\u08e3\u0157\u0001\u0000\u0000\u0000\u08e4\u08e2"+
		"\u0001\u0000\u0000\u0000\u08e5\u08e8\u0003\u015a\u00ad\u0000\u08e6\u08e7"+
		"\u0005u\u0000\u0000\u08e7\u08e9\u0003\u015c\u00ae\u0000\u08e8\u08e6\u0001"+
		"\u0000\u0000\u0000\u08e8\u08e9\u0001\u0000\u0000\u0000\u08e9\u0159\u0001"+
		"\u0000\u0000\u0000\u08ea\u08f1\u0003\u017a\u00bd\u0000\u08eb\u08f1\u0005"+
		"\u001c\u0000\u0000\u08ec\u08f1\u0005\u0018\u0000\u0000\u08ed\u08f1\u0005"+
		"\u0019\u0000\u0000\u08ee\u08f1\u0005\u0005\u0000\u0000\u08ef\u08f1\u0005"+
		"8\u0000\u0000\u08f0\u08ea\u0001\u0000\u0000\u0000\u08f0\u08eb\u0001\u0000"+
		"\u0000\u0000\u08f0\u08ec\u0001\u0000\u0000\u0000\u08f0\u08ed\u0001\u0000"+
		"\u0000\u0000\u08f0\u08ee\u0001\u0000\u0000\u0000\u08f0\u08ef\u0001\u0000"+
		"\u0000\u0000\u08f1\u015b\u0001\u0000\u0000\u0000\u08f2\u08f3\u0005i\u0000"+
		"\u0000\u08f3\u091e\u0005h\u0000\u0000\u08f4\u08f5\u0005i\u0000\u0000\u08f5"+
		"\u08f8\u0003\u0162\u00b1\u0000\u08f6\u08f7\u0005r\u0000\u0000\u08f7\u08f9"+
		"\u0003\u0164\u00b2\u0000\u08f8\u08f6\u0001\u0000\u0000\u0000\u08f8\u08f9"+
		"\u0001\u0000\u0000\u0000\u08f9\u08fc\u0001\u0000\u0000\u0000\u08fa\u08fb"+
		"\u0005r\u0000\u0000\u08fb\u08fd\u0003\u0166\u00b3\u0000\u08fc\u08fa\u0001"+
		"\u0000\u0000\u0000\u08fc\u08fd\u0001\u0000\u0000\u0000\u08fd\u08ff\u0001"+
		"\u0000\u0000\u0000\u08fe\u0900\u0005r\u0000\u0000\u08ff\u08fe\u0001\u0000"+
		"\u0000\u0000\u08ff\u0900\u0001\u0000\u0000\u0000\u0900\u0901\u0001\u0000"+
		"\u0000\u0000\u0901\u0902\u0005h\u0000\u0000\u0902\u091e\u0001\u0000\u0000"+
		"\u0000\u0903\u0904\u0005i\u0000\u0000\u0904\u0907\u0003\u0164\u00b2\u0000"+
		"\u0905\u0906\u0005r\u0000\u0000\u0906\u0908\u0003\u0166\u00b3\u0000\u0907"+
		"\u0905\u0001\u0000\u0000\u0000\u0907\u0908\u0001\u0000\u0000\u0000\u0908"+
		"\u090a\u0001\u0000\u0000\u0000\u0909\u090b\u0005r\u0000\u0000\u090a\u0909"+
		"\u0001\u0000\u0000\u0000\u090a\u090b\u0001\u0000\u0000\u0000\u090b\u090c"+
		"\u0001\u0000\u0000\u0000\u090c\u090d\u0005h\u0000\u0000\u090d\u091e\u0001"+
		"\u0000\u0000\u0000\u090e\u0914\u0005i\u0000\u0000\u090f\u0910\u0003\u015e"+
		"\u00af\u0000\u0910\u0911\u0005r\u0000\u0000\u0911\u0913\u0001\u0000\u0000"+
		"\u0000\u0912\u090f\u0001\u0000\u0000\u0000\u0913\u0916\u0001\u0000\u0000"+
		"\u0000\u0914\u0912\u0001\u0000\u0000\u0000\u0914\u0915\u0001\u0000\u0000"+
		"\u0000\u0915\u0917\u0001\u0000\u0000\u0000\u0916\u0914\u0001\u0000\u0000"+
		"\u0000\u0917\u0919\u0003\u015e\u00af\u0000\u0918\u091a\u0005r\u0000\u0000"+
		"\u0919\u0918\u0001\u0000\u0000\u0000\u0919\u091a\u0001\u0000\u0000\u0000"+
		"\u091a\u091b\u0001\u0000\u0000\u0000\u091b\u091c\u0005h\u0000\u0000\u091c"+
		"\u091e\u0001\u0000\u0000\u0000\u091d\u08f2\u0001\u0000\u0000\u0000\u091d"+
		"\u08f4\u0001\u0000\u0000\u0000\u091d\u0903\u0001\u0000\u0000\u0000\u091d"+
		"\u090e\u0001\u0000\u0000\u0000\u091e\u015d\u0001\u0000\u0000\u0000\u091f"+
		"\u0924\u0003\u0150\u00a8\u0000\u0920\u0924\u0003\u011e\u008f\u0000\u0921"+
		"\u0924\u0003\u0160\u00b0\u0000\u0922\u0924\u0003\u0168\u00b4\u0000\u0923"+
		"\u091f\u0001\u0000\u0000\u0000\u0923\u0920\u0001\u0000\u0000\u0000\u0923"+
		"\u0921\u0001\u0000\u0000\u0000\u0923\u0922\u0001\u0000\u0000\u0000\u0924"+
		"\u015f\u0001\u0000\u0000\u0000\u0925\u092c\u0003\u00a6S\u0000\u0926\u0928"+
		"\u0005Q\u0000\u0000\u0927\u0926\u0001\u0000\u0000\u0000\u0927\u0928\u0001"+
		"\u0000\u0000\u0000\u0928\u0929\u0001\u0000\u0000\u0000\u0929\u092c\u0003"+
		"\u00a2Q\u0000\u092a\u092c\u0003\u0154\u00aa\u0000\u092b\u0925\u0001\u0000"+
		"\u0000\u0000\u092b\u0927\u0001\u0000\u0000\u0000\u092b\u092a\u0001\u0000"+
		"\u0000\u0000\u092c\u0161\u0001\u0000\u0000\u0000\u092d\u0932\u0003\u0150"+
		"\u00a8\u0000\u092e\u092f\u0005r\u0000\u0000\u092f\u0931\u0003\u0150\u00a8"+
		"\u0000\u0930\u092e\u0001\u0000\u0000\u0000\u0931\u0934\u0001\u0000\u0000"+
		"\u0000\u0932\u0930\u0001\u0000\u0000\u0000\u0932\u0933\u0001\u0000\u0000"+
		"\u0000\u0933\u0163\u0001\u0000\u0000\u0000\u0934\u0932\u0001\u0000\u0000"+
		"\u0000\u0935\u093a\u0003\u011e\u008f\u0000\u0936\u0937\u0005r\u0000\u0000"+
		"\u0937\u0939\u0003\u011e\u008f\u0000\u0938\u0936\u0001\u0000\u0000\u0000"+
		"\u0939\u093c\u0001\u0000\u0000\u0000\u093a\u0938\u0001\u0000\u0000\u0000"+
		"\u093a\u093b\u0001\u0000\u0000\u0000\u093b\u0165\u0001\u0000\u0000\u0000"+
		"\u093c\u093a\u0001\u0000\u0000\u0000\u093d\u0942\u0003\u0168\u00b4\u0000"+
		"\u093e\u093f\u0005r\u0000\u0000\u093f\u0941\u0003\u0168\u00b4\u0000\u0940"+
		"\u093e\u0001\u0000\u0000\u0000\u0941\u0944\u0001\u0000\u0000\u0000\u0942"+
		"\u0940\u0001\u0000\u0000\u0000\u0942\u0943\u0001\u0000\u0000\u0000\u0943"+
		"\u0167\u0001\u0000\u0000\u0000\u0944\u0942\u0001\u0000\u0000\u0000\u0945"+
		"\u0946\u0003\u017a\u00bd\u0000\u0946\u0947\u0005e\u0000\u0000\u0947\u0948"+
		"\u0003\u011e\u008f\u0000\u0948\u0169\u0001\u0000\u0000\u0000\u0949\u094c"+
		"\u0003\u016c\u00b6\u0000\u094a\u094b\u0005u\u0000\u0000\u094b\u094d\u0003"+
		"\u0158\u00ac\u0000\u094c\u094a\u0001\u0000\u0000\u0000\u094d\u094e\u0001"+
		"\u0000\u0000\u0000\u094e\u094c\u0001\u0000\u0000\u0000\u094e\u094f\u0001"+
		"\u0000\u0000\u0000\u094f\u016b\u0001\u0000\u0000\u0000\u0950\u0951\u0005"+
		"i\u0000\u0000\u0951\u0954\u0003\u011e\u008f\u0000\u0952\u0953\u0005\u0001"+
		"\u0000\u0000\u0953\u0955\u0003\u0170\u00b8\u0000\u0954\u0952\u0001\u0000"+
		"\u0000\u0000\u0954\u0955\u0001\u0000\u0000\u0000\u0955\u0956\u0001\u0000"+
		"\u0000\u0000\u0956\u0957\u0005h\u0000\u0000\u0957\u016d\u0001\u0000\u0000"+
		"\u0000\u0958\u095b\u0003\u016c\u00b6\u0000\u0959\u095a\u0005u\u0000\u0000"+
		"\u095a\u095c\u0003\u0172\u00b9\u0000\u095b\u0959\u0001\u0000\u0000\u0000"+
		"\u095c\u095d\u0001\u0000\u0000\u0000\u095d\u095b\u0001\u0000\u0000\u0000"+
		"\u095d\u095e\u0001\u0000\u0000\u0000\u095e\u016f\u0001\u0000\u0000\u0000"+
		"\u095f\u0961\u0005u\u0000\u0000\u0960\u095f\u0001\u0000\u0000\u0000\u0960"+
		"\u0961\u0001\u0000\u0000\u0000\u0961\u0962\u0001\u0000\u0000\u0000\u0962"+
		"\u0967\u0003\u0172\u00b9\u0000\u0963\u0964\u0005u\u0000\u0000\u0964\u0966"+
		"\u0003\u0172\u00b9\u0000\u0965\u0963\u0001\u0000\u0000\u0000\u0966\u0969"+
		"\u0001\u0000\u0000\u0000\u0967\u0965\u0001\u0000\u0000\u0000\u0967\u0968"+
		"\u0001\u0000\u0000\u0000\u0968\u0171\u0001\u0000\u0000\u0000\u0969\u0967"+
		"\u0001\u0000\u0000\u0000\u096a\u096c\u0003\u015a\u00ad\u0000\u096b\u096d"+
		"\u0005u\u0000\u0000\u096c\u096b\u0001\u0000\u0000\u0000\u096c\u096d\u0001"+
		"\u0000\u0000\u0000\u096d\u0970\u0001\u0000\u0000\u0000\u096e\u0971\u0003"+
		"\u015c\u00ae\u0000\u096f\u0971\u0003\u0174\u00ba\u0000\u0970\u096e\u0001"+
		"\u0000\u0000\u0000\u0970\u096f\u0001\u0000\u0000\u0000\u0970\u0971\u0001"+
		"\u0000\u0000\u0000\u0971\u0173\u0001\u0000\u0000\u0000\u0972\u0974\u0005"+
		"\u007f\u0000\u0000\u0973\u0975\u0003\u0176\u00bb\u0000\u0974\u0973\u0001"+
		"\u0000\u0000\u0000\u0974\u0975\u0001\u0000\u0000\u0000\u0975\u0976\u0001"+
		"\u0000";
	private static final String _serializedATNSegment1 =
		"\u0000\u0000\u0976\u0979\u0005\u0080\u0000\u0000\u0977\u0978\u0005v\u0000"+
		"\u0000\u0978\u097a\u0003\u011e\u008f\u0000\u0979\u0977\u0001\u0000\u0000"+
		"\u0000\u0979\u097a\u0001\u0000\u0000\u0000\u097a\u0175\u0001\u0000\u0000"+
		"\u0000\u097b\u0980\u0003\u011e\u008f\u0000\u097c\u097d\u0005r\u0000\u0000"+
		"\u097d\u097f\u0003\u011e\u008f\u0000\u097e\u097c\u0001\u0000\u0000\u0000"+
		"\u097f\u0982\u0001\u0000\u0000\u0000\u0980\u097e\u0001\u0000\u0000\u0000"+
		"\u0980\u0981\u0001\u0000\u0000\u0000\u0981\u0984\u0001\u0000\u0000\u0000"+
		"\u0982\u0980\u0001\u0000\u0000\u0000\u0983\u0985\u0005r\u0000\u0000\u0984"+
		"\u0983\u0001\u0000\u0000\u0000\u0984\u0985\u0001\u0000\u0000\u0000\u0985"+
		"\u0177\u0001\u0000\u0000\u0000\u0986\u0990\u0005\u0015\u0000\u0000\u0987"+
		"\u098d\u0005\u007f\u0000\u0000\u0988\u098e\u0005\u0005\u0000\u0000\u0989"+
		"\u098e\u0005\u0018\u0000\u0000\u098a\u098e\u0005\u001c\u0000\u0000\u098b"+
		"\u098c\u0005\u000e\u0000\u0000\u098c\u098e\u0003\u0152\u00a9\u0000\u098d"+
		"\u0988\u0001\u0000\u0000\u0000\u098d\u0989\u0001\u0000\u0000\u0000\u098d"+
		"\u098a\u0001\u0000\u0000\u0000\u098d\u098b\u0001\u0000\u0000\u0000\u098e"+
		"\u098f\u0001\u0000\u0000\u0000\u098f\u0991\u0005\u0080\u0000\u0000\u0990"+
		"\u0987\u0001\u0000\u0000\u0000\u0990\u0991\u0001\u0000\u0000\u0000\u0991"+
		"\u0179\u0001\u0000\u0000\u0000\u0992\u0993\u0007\u000b\u0000\u0000\u0993"+
		"\u017b\u0001\u0000\u0000\u0000\u0994\u0995\u0007\f\u0000\u0000\u0995\u017d"+
		"\u0001\u0000\u0000\u0000\u0996\u099d\u0003\u017c\u00be\u0000\u0997\u099d"+
		"\u0003\u017a\u00bd\u0000\u0998\u099d\u00056\u0000\u0000\u0999\u099d\u0005"+
		"7\u0000\u0000\u099a\u099d\u00058\u0000\u0000\u099b\u099d\u0005O\u0000"+
		"\u0000\u099c\u0996\u0001\u0000\u0000\u0000\u099c\u0997\u0001\u0000\u0000"+
		"\u0000\u099c\u0998\u0001\u0000\u0000\u0000\u099c\u0999\u0001\u0000\u0000"+
		"\u0000\u099c\u099a\u0001\u0000\u0000\u0000\u099c\u099b\u0001\u0000\u0000"+
		"\u0000\u099d\u017f\u0001\u0000\u0000\u0000\u099e\u099f\u0003\u00a2Q\u0000"+
		"\u099f\u0181\u0001\u0000\u0000\u0000\u09a0\u09a1\u0007\r\u0000\u0000\u09a1"+
		"\u0183\u0001\u0000\u0000\u0000\u09a2\u09a3\u0005i\u0000\u0000\u09a3\u09a4"+
		"\u0004\u00c2\u0015\u0000\u09a4\u09a5\u0005i\u0000\u0000\u09a5\u0185\u0001"+
		"\u0000\u0000\u0000\u09a6\u09a7\u0005h\u0000\u0000\u09a7\u09a8\u0004\u00c3"+
		"\u0016\u0000\u09a8\u09a9\u0005h\u0000\u0000\u09a9\u0187\u0001\u0000\u0000"+
		"\u0000\u0159\u018b\u0191\u019e\u01a6\u01ae\u01b2\u01b7\u01ba\u01c1\u01c9"+
		"\u01d5\u01e1\u01e6\u01fb\u0202\u0206\u0210\u0218\u0220\u0224\u0229\u022f"+
		"\u0238\u023c\u0240\u0246\u024e\u0257\u025c\u025f\u026e\u0272\u0275\u027e"+
		"\u0284\u0288\u028e\u0294\u0299\u02a0\u02a3\u02ac\u02b0\u02b2\u02b5\u02bb"+
		"\u02bd\u02bf\u02c5\u02c9\u02cd\u02d0\u02d4\u02d7\u02da\u02dd\u02e1\u02e3"+
		"\u02e9\u02ee\u02f5\u02f9\u02fb\u0300\u0305\u0309\u030b\u030e\u0313\u031c"+
		"\u0322\u0328\u0330\u0333\u0337\u033d\u0342\u0345\u0349\u034d\u0352\u0356"+
		"\u035a\u0363\u0367\u036c\u0370\u037b\u037f\u0384\u0388\u038f\u0392\u0396"+
		"\u039f\u03a3\u03a8\u03ac\u03b2\u03b6\u03bc\u03c6\u03c9\u03d2\u03d8\u03de"+
		"\u03e5\u03ea\u03ef\u03f3\u03f5\u03f8\u03fe\u0404\u040b\u040f\u0413\u0419"+
		"\u041f\u0425\u0429\u042c\u0432\u0438\u043e\u0444\u0448\u044e\u0454\u045c"+
		"\u0461\u0465\u0467\u046f\u0474\u0476\u047d\u0483\u0486\u048b\u048e\u0493"+
		"\u0495\u0499\u04a6\u04aa\u04ae\u04b5\u04ba\u04c2\u04c7\u04cc\u04ce\u04dd"+
		"\u04e2\u04e9\u04ee\u04f5\u04f9\u0502\u0504\u050a\u0512\u051b\u0521\u0524"+
		"\u0528\u052b\u052f\u0535\u053f\u0543\u054a\u054e\u0556\u0561\u0587\u0597"+
		"\u05a7\u05a9\u05ab\u05b5\u05c0\u05c6\u05cc\u05d0\u05d7\u05da\u05dd\u05e1"+
		"\u05ed\u05f1\u05f7\u05fe\u0601\u0608\u060f\u0614\u061d\u0623\u0625\u062a"+
		"\u0630\u0635\u063f\u0647\u064b\u064d\u0656\u065b\u0664\u0668\u066d\u0672"+
		"\u067b\u067f\u0681\u068c\u0690\u0693\u0698\u069b\u06a2\u06a9\u06ad\u06b2"+
		"\u06b8\u06bb\u06c1\u06e1\u06e3\u06ef\u06f1\u06f9\u06fd\u0707\u070e\u0715"+
		"\u0717\u071c\u0721\u0727\u072e\u0733\u0741\u074c\u0750\u0753\u0756\u0759"+
		"\u075e\u076f\u0774\u0778\u077c\u0780\u0787\u078e\u0790\u0793\u079a\u07a0"+
		"\u07ac\u07af\u07b2\u07b7\u07bf\u07c8\u07cc\u07d0\u07dd\u07e0\u07e2\u07ea"+
		"\u07f3\u07f7\u07fb\u0800\u0810\u081e\u0821\u0823\u0833\u0836\u083f\u0845"+
		"\u0849\u084c\u0850\u0852\u0859\u0860\u0864\u0869\u086e\u0871\u087a\u0882"+
		"\u0888\u088d\u089e\u08a2\u08a6\u08a9\u08ac\u08b1\u08b4\u08b9\u08c0\u08c4"+
		"\u08c9\u08d0\u08d8\u08db\u08e2\u08e8\u08f0\u08f8\u08fc\u08ff\u0907\u090a"+
		"\u0914\u0919\u091d\u0923\u0927\u092b\u0932\u093a\u0942\u094e\u0954\u095d"+
		"\u0960\u0967\u096c\u0970\u0974\u0979\u0980\u0984\u098d\u0990\u099c";
	public static final String _serializedATN = Utils.join(
		new String[] {
			_serializedATNSegment0,
			_serializedATNSegment1
		},
		""
	);
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}