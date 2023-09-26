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

// DO NOT MODIFY THIS FILE!
// This file is generated file from RustParser.g4 at compile time!


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
		RSQUAREBRACKET=126, LPAREN=127, RPAREN=128, SINGLEQUOTE=129, DOUBLEQUOTE=130, 
		BACKSLASH=131;
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
		RULE_shiftOperator = 78, RULE_comparisonOperator = 79, RULE_compoundAssignOperator = 80, 
		RULE_expressionWithBlock = 81, RULE_literalExpression = 82, RULE_pathExpression = 83, 
		RULE_blockExpression = 84, RULE_statements = 85, RULE_asyncBlockExpression = 86, 
		RULE_unsafeBlockExpression = 87, RULE_arrayElements = 88, RULE_tupleElements = 89, 
		RULE_tupleIndex = 90, RULE_structExpression = 91, RULE_structExprStruct = 92, 
		RULE_structExprFields = 93, RULE_structExprField = 94, RULE_structBase = 95, 
		RULE_structExprTuple = 96, RULE_structExprUnit = 97, RULE_enumerationVariantExpression = 98, 
		RULE_enumExprStruct = 99, RULE_enumExprFields = 100, RULE_enumExprField = 101, 
		RULE_enumExprTuple = 102, RULE_enumExprFieldless = 103, RULE_callParams = 104, 
		RULE_closureExpression = 105, RULE_closureParameters = 106, RULE_closureParam = 107, 
		RULE_loopExpression = 108, RULE_infiniteLoopExpression = 109, RULE_predicateLoopExpression = 110, 
		RULE_predicatePatternLoopExpression = 111, RULE_iteratorLoopExpression = 112, 
		RULE_loopLabel = 113, RULE_ifExpression = 114, RULE_ifLetExpression = 115, 
		RULE_matchExpression = 116, RULE_matchArms = 117, RULE_matchArmExpression = 118, 
		RULE_matchArm = 119, RULE_matchArmGuard = 120, RULE_pattern = 121, RULE_patternNoTopAlt = 122, 
		RULE_patternWithoutRange = 123, RULE_literalPattern = 124, RULE_identifierPattern = 125, 
		RULE_wildcardPattern = 126, RULE_restPattern = 127, RULE_rangePattern = 128, 
		RULE_rangePatternBound = 129, RULE_referencePattern = 130, RULE_structPattern = 131, 
		RULE_structPatternElements = 132, RULE_structPatternFields = 133, RULE_structPatternField = 134, 
		RULE_structPatternEtCetera = 135, RULE_tupleStructPattern = 136, RULE_tupleStructItems = 137, 
		RULE_tuplePattern = 138, RULE_tuplePatternItems = 139, RULE_groupedPattern = 140, 
		RULE_slicePattern = 141, RULE_slicePatternItems = 142, RULE_pathPattern = 143, 
		RULE_type_ = 144, RULE_typeNoBounds = 145, RULE_parenthesizedType = 146, 
		RULE_neverType = 147, RULE_tupleType = 148, RULE_arrayType = 149, RULE_sliceType = 150, 
		RULE_referenceType = 151, RULE_rawPointerType = 152, RULE_bareFunctionType = 153, 
		RULE_functionTypeQualifiers = 154, RULE_bareFunctionReturnType = 155, 
		RULE_functionParametersMaybeNamedVariadic = 156, RULE_maybeNamedFunctionParameters = 157, 
		RULE_maybeNamedParam = 158, RULE_maybeNamedFunctionParametersVariadic = 159, 
		RULE_traitObjectType = 160, RULE_traitObjectTypeOneBound = 161, RULE_implTraitType = 162, 
		RULE_implTraitTypeOneBound = 163, RULE_inferredType = 164, RULE_typeParamBounds = 165, 
		RULE_typeParamBound = 166, RULE_traitBound = 167, RULE_lifetimeBounds = 168, 
		RULE_lifetime = 169, RULE_simplePath = 170, RULE_simplePathSegment = 171, 
		RULE_pathInExpression = 172, RULE_pathExprSegment = 173, RULE_pathIdentSegment = 174, 
		RULE_genericArgs = 175, RULE_genericArg = 176, RULE_genericArgsConst = 177, 
		RULE_genericArgsLifetimes = 178, RULE_genericArgsTypes = 179, RULE_genericArgsBindings = 180, 
		RULE_genericArgsBinding = 181, RULE_qualifiedPathInExpression = 182, RULE_qualifiedPathType = 183, 
		RULE_qualifiedPathInType = 184, RULE_typePath = 185, RULE_typePathSegment = 186, 
		RULE_typePathFn = 187, RULE_typePathInputs = 188, RULE_visibility = 189, 
		RULE_identifier = 190, RULE_keyword = 191, RULE_macroIdentifierLikeToken = 192, 
		RULE_macroLiteralToken = 193, RULE_macroPunctuationToken = 194, RULE_shl = 195, 
		RULE_shr = 196;
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
			"statement", "letStatement", "expressionStatement", "expression", "shiftOperator", 
			"comparisonOperator", "compoundAssignOperator", "expressionWithBlock", 
			"literalExpression", "pathExpression", "blockExpression", "statements", 
			"asyncBlockExpression", "unsafeBlockExpression", "arrayElements", "tupleElements", 
			"tupleIndex", "structExpression", "structExprStruct", "structExprFields", 
			"structExprField", "structBase", "structExprTuple", "structExprUnit", 
			"enumerationVariantExpression", "enumExprStruct", "enumExprFields", "enumExprField", 
			"enumExprTuple", "enumExprFieldless", "callParams", "closureExpression", 
			"closureParameters", "closureParam", "loopExpression", "infiniteLoopExpression", 
			"predicateLoopExpression", "predicatePatternLoopExpression", "iteratorLoopExpression", 
			"loopLabel", "ifExpression", "ifLetExpression", "matchExpression", "matchArms", 
			"matchArmExpression", "matchArm", "matchArmGuard", "pattern", "patternNoTopAlt", 
			"patternWithoutRange", "literalPattern", "identifierPattern", "wildcardPattern", 
			"restPattern", "rangePattern", "rangePatternBound", "referencePattern", 
			"structPattern", "structPatternElements", "structPatternFields", "structPatternField", 
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
			"']'", "'('", "')'", "'''", "'\"'", "'\\'"
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
			"LSQUAREBRACKET", "RSQUAREBRACKET", "LPAREN", "RPAREN", "SINGLEQUOTE", 
			"DOUBLEQUOTE", "BACKSLASH"
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitCrate(this);
			else return visitor.visitChildren(this);
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
			setState(397);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(394);
					innerAttribute();
					}
					} 
				}
				setState(399);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			}
			setState(403);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((_la) & ~0x3f) == 0 && ((1L << _la) & 526921241179989416L) != 0 || _la==PATHSEP || _la==POUND) {
				{
				{
				setState(400);
				item();
				}
				}
				setState(405);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(406);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMacroInvocation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MacroInvocationContext macroInvocation() throws RecognitionException {
		MacroInvocationContext _localctx = new MacroInvocationContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_macroInvocation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(408);
			simplePath();
			setState(409);
			match(NOT);
			setState(410);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitDelimTokenTree(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DelimTokenTreeContext delimTokenTree() throws RecognitionException {
		DelimTokenTreeContext _localctx = new DelimTokenTreeContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_delimTokenTree);
		int _la;
		try {
			setState(436);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(412);
				match(LPAREN);
				setState(416);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((_la) & ~0x3f) == 0 && ((1L << _la) & 576460752303423486L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1585267068834412671L) != 0) {
					{
					{
					setState(413);
					tokenTree();
					}
					}
					setState(418);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(419);
				match(RPAREN);
				}
				break;
			case LSQUAREBRACKET:
				enterOuterAlt(_localctx, 2);
				{
				setState(420);
				match(LSQUAREBRACKET);
				setState(424);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((_la) & ~0x3f) == 0 && ((1L << _la) & 576460752303423486L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1585267068834412671L) != 0) {
					{
					{
					setState(421);
					tokenTree();
					}
					}
					setState(426);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(427);
				match(RSQUAREBRACKET);
				}
				break;
			case LCURLYBRACE:
				enterOuterAlt(_localctx, 3);
				{
				setState(428);
				match(LCURLYBRACE);
				setState(432);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((_la) & ~0x3f) == 0 && ((1L << _la) & 576460752303423486L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1585267068834412671L) != 0) {
					{
					{
					setState(429);
					tokenTree();
					}
					}
					setState(434);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(435);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTokenTree(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TokenTreeContext tokenTree() throws RecognitionException {
		TokenTreeContext _localctx = new TokenTreeContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_tokenTree);
		try {
			int _alt;
			setState(444);
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
				setState(439); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(438);
						tokenTreeToken();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(441); 
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
				setState(443);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTokenTreeToken(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TokenTreeTokenContext tokenTreeToken() throws RecognitionException {
		TokenTreeTokenContext _localctx = new TokenTreeTokenContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_tokenTreeToken);
		try {
			setState(451);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(446);
				macroIdentifierLikeToken();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(447);
				macroLiteralToken();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(448);
				macroPunctuationToken();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(449);
				macroRepOp();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(450);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMacroInvocationSemi(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MacroInvocationSemiContext macroInvocationSemi() throws RecognitionException {
		MacroInvocationSemiContext _localctx = new MacroInvocationSemiContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_macroInvocationSemi);
		int _la;
		try {
			setState(488);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(453);
				simplePath();
				setState(454);
				match(NOT);
				setState(455);
				match(LPAREN);
				setState(459);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((_la) & ~0x3f) == 0 && ((1L << _la) & 576460752303423486L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1585267068834412671L) != 0) {
					{
					{
					setState(456);
					tokenTree();
					}
					}
					setState(461);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(462);
				match(RPAREN);
				setState(463);
				match(SEMI);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(465);
				simplePath();
				setState(466);
				match(NOT);
				setState(467);
				match(LSQUAREBRACKET);
				setState(471);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((_la) & ~0x3f) == 0 && ((1L << _la) & 576460752303423486L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1585267068834412671L) != 0) {
					{
					{
					setState(468);
					tokenTree();
					}
					}
					setState(473);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(474);
				match(RSQUAREBRACKET);
				setState(475);
				match(SEMI);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(477);
				simplePath();
				setState(478);
				match(NOT);
				setState(479);
				match(LCURLYBRACE);
				setState(483);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((_la) & ~0x3f) == 0 && ((1L << _la) & 576460752303423486L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1585267068834412671L) != 0) {
					{
					{
					setState(480);
					tokenTree();
					}
					}
					setState(485);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(486);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMacroRulesDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MacroRulesDefinitionContext macroRulesDefinition() throws RecognitionException {
		MacroRulesDefinitionContext _localctx = new MacroRulesDefinitionContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_macroRulesDefinition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(490);
			match(KW_MACRORULES);
			setState(491);
			match(NOT);
			setState(492);
			identifier();
			setState(493);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMacroRulesDef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MacroRulesDefContext macroRulesDef() throws RecognitionException {
		MacroRulesDefContext _localctx = new MacroRulesDefContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_macroRulesDef);
		try {
			setState(509);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(495);
				match(LPAREN);
				setState(496);
				macroRules();
				setState(497);
				match(RPAREN);
				setState(498);
				match(SEMI);
				}
				break;
			case LSQUAREBRACKET:
				enterOuterAlt(_localctx, 2);
				{
				setState(500);
				match(LSQUAREBRACKET);
				setState(501);
				macroRules();
				setState(502);
				match(RSQUAREBRACKET);
				setState(503);
				match(SEMI);
				}
				break;
			case LCURLYBRACE:
				enterOuterAlt(_localctx, 3);
				{
				setState(505);
				match(LCURLYBRACE);
				setState(506);
				macroRules();
				setState(507);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMacroRules(this);
			else return visitor.visitChildren(this);
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
			setState(511);
			macroRule();
			setState(516);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(512);
					match(SEMI);
					setState(513);
					macroRule();
					}
					} 
				}
				setState(518);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			}
			setState(520);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(519);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMacroRule(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MacroRuleContext macroRule() throws RecognitionException {
		MacroRuleContext _localctx = new MacroRuleContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_macroRule);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(522);
			macroMatcher();
			setState(523);
			match(FATARROW);
			setState(524);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMacroMatcher(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MacroMatcherContext macroMatcher() throws RecognitionException {
		MacroMatcherContext _localctx = new MacroMatcherContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_macroMatcher);
		int _la;
		try {
			setState(550);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(526);
				match(LPAREN);
				setState(530);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((_la) & ~0x3f) == 0 && ((1L << _la) & 576460752303423486L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1585267068834412671L) != 0) {
					{
					{
					setState(527);
					macroMatch();
					}
					}
					setState(532);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(533);
				match(RPAREN);
				}
				break;
			case LSQUAREBRACKET:
				enterOuterAlt(_localctx, 2);
				{
				setState(534);
				match(LSQUAREBRACKET);
				setState(538);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((_la) & ~0x3f) == 0 && ((1L << _la) & 576460752303423486L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1585267068834412671L) != 0) {
					{
					{
					setState(535);
					macroMatch();
					}
					}
					setState(540);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(541);
				match(RSQUAREBRACKET);
				}
				break;
			case LCURLYBRACE:
				enterOuterAlt(_localctx, 3);
				{
				setState(542);
				match(LCURLYBRACE);
				setState(546);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((_la) & ~0x3f) == 0 && ((1L << _la) & 576460752303423486L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1585267068834412671L) != 0) {
					{
					{
					setState(543);
					macroMatch();
					}
					}
					setState(548);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(549);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMacroMatch(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MacroMatchContext macroMatch() throws RecognitionException {
		MacroMatchContext _localctx = new MacroMatchContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_macroMatch);
		int _la;
		try {
			int _alt;
			setState(578);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(553); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(552);
						macroMatchToken();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(555); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(557);
				macroMatcher();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(558);
				match(DOLLAR);
				setState(561);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case KW_MACRORULES:
				case NON_KEYWORD_IDENTIFIER:
				case RAW_IDENTIFIER:
					{
					setState(559);
					identifier();
					}
					break;
				case KW_SELFVALUE:
					{
					setState(560);
					match(KW_SELFVALUE);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(563);
				match(COLON);
				setState(564);
				macroFragSpec();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(565);
				match(DOLLAR);
				setState(566);
				match(LPAREN);
				setState(568); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(567);
					macroMatch();
					}
					}
					setState(570); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( ((_la) & ~0x3f) == 0 && ((1L << _la) & 576460752303423486L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1585267068834412671L) != 0 );
				setState(572);
				match(RPAREN);
				setState(574);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 576460752303423486L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 36028797018921087L) != 0) {
					{
					setState(573);
					macroRepSep();
					}
				}

				setState(576);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMacroMatchToken(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MacroMatchTokenContext macroMatchToken() throws RecognitionException {
		MacroMatchTokenContext _localctx = new MacroMatchTokenContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_macroMatchToken);
		try {
			setState(584);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(580);
				macroIdentifierLikeToken();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(581);
				macroLiteralToken();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(582);
				macroPunctuationToken();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(583);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMacroFragSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MacroFragSpecContext macroFragSpec() throws RecognitionException {
		MacroFragSpecContext _localctx = new MacroFragSpecContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_macroFragSpec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(586);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMacroRepSep(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MacroRepSepContext macroRepSep() throws RecognitionException {
		MacroRepSepContext _localctx = new MacroRepSepContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_macroRepSep);
		try {
			setState(592);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(588);
				macroIdentifierLikeToken();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(589);
				macroLiteralToken();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(590);
				macroPunctuationToken();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(591);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMacroRepOp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MacroRepOpContext macroRepOp() throws RecognitionException {
		MacroRepOpContext _localctx = new MacroRepOpContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_macroRepOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(594);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMacroTranscriber(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MacroTranscriberContext macroTranscriber() throws RecognitionException {
		MacroTranscriberContext _localctx = new MacroTranscriberContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_macroTranscriber);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(596);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ItemContext item() throws RecognitionException {
		ItemContext _localctx = new ItemContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_item);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(601);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(598);
				outerAttribute();
				}
				}
				setState(603);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(606);
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
				setState(604);
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
				setState(605);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitVisItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VisItemContext visItem() throws RecognitionException {
		VisItemContext _localctx = new VisItemContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_visItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(609);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_PUB) {
				{
				setState(608);
				visibility();
				}
			}

			setState(624);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
			case 1:
				{
				setState(611);
				module();
				}
				break;
			case 2:
				{
				setState(612);
				externCrate();
				}
				break;
			case 3:
				{
				setState(613);
				useDeclaration();
				}
				break;
			case 4:
				{
				setState(614);
				function_();
				}
				break;
			case 5:
				{
				setState(615);
				typeAlias();
				}
				break;
			case 6:
				{
				setState(616);
				struct_();
				}
				break;
			case 7:
				{
				setState(617);
				enumeration();
				}
				break;
			case 8:
				{
				setState(618);
				union_();
				}
				break;
			case 9:
				{
				setState(619);
				constantItem();
				}
				break;
			case 10:
				{
				setState(620);
				staticItem();
				}
				break;
			case 11:
				{
				setState(621);
				trait_();
				}
				break;
			case 12:
				{
				setState(622);
				implementation();
				}
				break;
			case 13:
				{
				setState(623);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMacroItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MacroItemContext macroItem() throws RecognitionException {
		MacroItemContext _localctx = new MacroItemContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_macroItem);
		try {
			setState(628);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(626);
				macroInvocationSemi();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(627);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitModule(this);
			else return visitor.visitChildren(this);
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
			setState(631);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_UNSAFE) {
				{
				setState(630);
				match(KW_UNSAFE);
				}
			}

			setState(633);
			match(KW_MOD);
			setState(634);
			identifier();
			setState(650);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SEMI:
				{
				setState(635);
				match(SEMI);
				}
				break;
			case LCURLYBRACE:
				{
				setState(636);
				match(LCURLYBRACE);
				setState(640);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,33,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(637);
						innerAttribute();
						}
						} 
					}
					setState(642);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,33,_ctx);
				}
				setState(646);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((_la) & ~0x3f) == 0 && ((1L << _la) & 526921241179989416L) != 0 || _la==PATHSEP || _la==POUND) {
					{
					{
					setState(643);
					item();
					}
					}
					setState(648);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(649);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitExternCrate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExternCrateContext externCrate() throws RecognitionException {
		ExternCrateContext _localctx = new ExternCrateContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_externCrate);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(652);
			match(KW_EXTERN);
			setState(653);
			match(KW_CRATE);
			setState(654);
			crateRef();
			setState(656);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_AS) {
				{
				setState(655);
				asClause();
				}
			}

			setState(658);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitCrateRef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CrateRefContext crateRef() throws RecognitionException {
		CrateRefContext _localctx = new CrateRefContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_crateRef);
		try {
			setState(662);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_MACRORULES:
			case NON_KEYWORD_IDENTIFIER:
			case RAW_IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(660);
				identifier();
				}
				break;
			case KW_SELFVALUE:
				enterOuterAlt(_localctx, 2);
				{
				setState(661);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitAsClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AsClauseContext asClause() throws RecognitionException {
		AsClauseContext _localctx = new AsClauseContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_asClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(664);
			match(KW_AS);
			setState(667);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_MACRORULES:
			case NON_KEYWORD_IDENTIFIER:
			case RAW_IDENTIFIER:
				{
				setState(665);
				identifier();
				}
				break;
			case UNDERSCORE:
				{
				setState(666);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitUseDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UseDeclarationContext useDeclaration() throws RecognitionException {
		UseDeclarationContext _localctx = new UseDeclarationContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_useDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(669);
			match(KW_USE);
			setState(670);
			useTree();
			setState(671);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitUseTree(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UseTreeContext useTree() throws RecognitionException {
		UseTreeContext _localctx = new UseTreeContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_useTree);
		int _la;
		try {
			int _alt;
			setState(705);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,47,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(677);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417557060190240L) != 0 || _la==PATHSEP) {
					{
					setState(674);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
					case 1:
						{
						setState(673);
						simplePath();
						}
						break;
					}
					setState(676);
					match(PATHSEP);
					}
				}

				setState(695);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case STAR:
					{
					setState(679);
					match(STAR);
					}
					break;
				case LCURLYBRACE:
					{
					setState(680);
					match(LCURLYBRACE);
					setState(692);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417557060190240L) != 0 || (((_la - 82)) & ~0x3f) == 0 && ((1L << (_la - 82)) & 2233382993921L) != 0) {
						{
						setState(681);
						useTree();
						setState(686);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,41,_ctx);
						while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
							if ( _alt==1 ) {
								{
								{
								setState(682);
								match(COMMA);
								setState(683);
								useTree();
								}
								} 
							}
							setState(688);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,41,_ctx);
						}
						setState(690);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==COMMA) {
							{
							setState(689);
							match(COMMA);
							}
						}

						}
					}

					setState(694);
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
				setState(697);
				simplePath();
				setState(703);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==KW_AS) {
					{
					setState(698);
					match(KW_AS);
					setState(701);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case KW_MACRORULES:
					case NON_KEYWORD_IDENTIFIER:
					case RAW_IDENTIFIER:
						{
						setState(699);
						identifier();
						}
						break;
					case UNDERSCORE:
						{
						setState(700);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitFunction_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Function_Context function_() throws RecognitionException {
		Function_Context _localctx = new Function_Context(_ctx, getState());
		enterRule(_localctx, 52, RULE_function_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(707);
			functionQualifiers();
			setState(708);
			match(KW_FN);
			setState(709);
			identifier();
			setState(711);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(710);
				genericParams();
				}
			}

			setState(713);
			match(LPAREN);
			setState(715);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 567453833619320608L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1487371226429577343L) != 0) {
				{
				setState(714);
				functionParameters();
				}
			}

			setState(717);
			match(RPAREN);
			setState(719);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RARROW) {
				{
				setState(718);
				functionReturnType();
				}
			}

			setState(722);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_WHERE) {
				{
				setState(721);
				whereClause();
				}
			}

			setState(726);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LCURLYBRACE:
				{
				setState(724);
				blockExpression();
				}
				break;
			case SEMI:
				{
				setState(725);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitFunctionQualifiers(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionQualifiersContext functionQualifiers() throws RecognitionException {
		FunctionQualifiersContext _localctx = new FunctionQualifiersContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_functionQualifiers);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(729);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_CONST) {
				{
				setState(728);
				match(KW_CONST);
				}
			}

			setState(732);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_ASYNC) {
				{
				setState(731);
				match(KW_ASYNC);
				}
			}

			setState(735);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_UNSAFE) {
				{
				setState(734);
				match(KW_UNSAFE);
				}
			}

			setState(741);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_EXTERN) {
				{
				setState(737);
				match(KW_EXTERN);
				setState(739);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STRING_LITERAL || _la==RAW_STRING_LITERAL) {
					{
					setState(738);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitAbi(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AbiContext abi() throws RecognitionException {
		AbiContext _localctx = new AbiContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_abi);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(743);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitFunctionParameters(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionParametersContext functionParameters() throws RecognitionException {
		FunctionParametersContext _localctx = new FunctionParametersContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_functionParameters);
		int _la;
		try {
			int _alt;
			setState(765);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,62,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(745);
				selfParam();
				setState(747);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(746);
					match(COMMA);
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(752);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,59,_ctx) ) {
				case 1:
					{
					setState(749);
					selfParam();
					setState(750);
					match(COMMA);
					}
					break;
				}
				setState(754);
				functionParam();
				setState(759);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,60,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(755);
						match(COMMA);
						setState(756);
						functionParam();
						}
						} 
					}
					setState(761);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,60,_ctx);
				}
				setState(763);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(762);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitSelfParam(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelfParamContext selfParam() throws RecognitionException {
		SelfParamContext _localctx = new SelfParamContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_selfParam);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(770);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(767);
				outerAttribute();
				}
				}
				setState(772);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(775);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,64,_ctx) ) {
			case 1:
				{
				setState(773);
				shorthandSelf();
				}
				break;
			case 2:
				{
				setState(774);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitShorthandSelf(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ShorthandSelfContext shorthandSelf() throws RecognitionException {
		ShorthandSelfContext _localctx = new ShorthandSelfContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_shorthandSelf);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(781);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AND) {
				{
				setState(777);
				match(AND);
				setState(779);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la - 53)) & ~0x3f) == 0 && ((1L << (_la - 53)) & 67108869L) != 0) {
					{
					setState(778);
					lifetime();
					}
				}

				}
			}

			setState(784);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_MUT) {
				{
				setState(783);
				match(KW_MUT);
				}
			}

			setState(786);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTypedSelf(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypedSelfContext typedSelf() throws RecognitionException {
		TypedSelfContext _localctx = new TypedSelfContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_typedSelf);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(789);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_MUT) {
				{
				setState(788);
				match(KW_MUT);
				}
			}

			setState(791);
			match(KW_SELFVALUE);
			setState(792);
			match(COLON);
			setState(793);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitFunctionParam(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionParamContext functionParam() throws RecognitionException {
		FunctionParamContext _localctx = new FunctionParamContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_functionParam);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(798);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(795);
				outerAttribute();
				}
				}
				setState(800);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(804);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,70,_ctx) ) {
			case 1:
				{
				setState(801);
				functionParamPattern();
				}
				break;
			case 2:
				{
				setState(802);
				match(DOTDOTDOT);
				}
				break;
			case 3:
				{
				setState(803);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitFunctionParamPattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionParamPatternContext functionParamPattern() throws RecognitionException {
		FunctionParamPatternContext _localctx = new FunctionParamPatternContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_functionParamPattern);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(806);
			pattern();
			setState(807);
			match(COLON);
			setState(810);
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
				setState(808);
				type_();
				}
				break;
			case DOTDOTDOT:
				{
				setState(809);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitFunctionReturnType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionReturnTypeContext functionReturnType() throws RecognitionException {
		FunctionReturnTypeContext _localctx = new FunctionReturnTypeContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_functionReturnType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(812);
			match(RARROW);
			setState(813);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTypeAlias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeAliasContext typeAlias() throws RecognitionException {
		TypeAliasContext _localctx = new TypeAliasContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_typeAlias);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(815);
			match(KW_TYPE);
			setState(816);
			identifier();
			setState(818);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(817);
				genericParams();
				}
			}

			setState(821);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_WHERE) {
				{
				setState(820);
				whereClause();
				}
			}

			setState(825);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ) {
				{
				setState(823);
				match(EQ);
				setState(824);
				type_();
				}
			}

			setState(827);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitStruct_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Struct_Context struct_() throws RecognitionException {
		Struct_Context _localctx = new Struct_Context(_ctx, getState());
		enterRule(_localctx, 74, RULE_struct_);
		try {
			setState(831);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,75,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(829);
				structStruct();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(830);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitStructStruct(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructStructContext structStruct() throws RecognitionException {
		StructStructContext _localctx = new StructStructContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_structStruct);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(833);
			match(KW_STRUCT);
			setState(834);
			identifier();
			setState(836);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(835);
				genericParams();
				}
			}

			setState(839);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_WHERE) {
				{
				setState(838);
				whereClause();
				}
			}

			setState(847);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LCURLYBRACE:
				{
				setState(841);
				match(LCURLYBRACE);
				setState(843);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 450359962739146752L) != 0 || _la==POUND) {
					{
					setState(842);
					structFields();
					}
				}

				setState(845);
				match(RCURLYBRACE);
				}
				break;
			case SEMI:
				{
				setState(846);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTupleStruct(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TupleStructContext tupleStruct() throws RecognitionException {
		TupleStructContext _localctx = new TupleStructContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_tupleStruct);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(849);
			match(KW_STRUCT);
			setState(850);
			identifier();
			setState(852);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(851);
				genericParams();
				}
			}

			setState(854);
			match(LPAREN);
			setState(856);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 567453832542432544L) != 0 || (((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 363114855924105L) != 0) {
				{
				setState(855);
				tupleFields();
				}
			}

			setState(858);
			match(RPAREN);
			setState(860);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_WHERE) {
				{
				setState(859);
				whereClause();
				}
			}

			setState(862);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitStructFields(this);
			else return visitor.visitChildren(this);
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
			setState(864);
			structField();
			setState(869);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,83,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(865);
					match(COMMA);
					setState(866);
					structField();
					}
					} 
				}
				setState(871);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,83,_ctx);
			}
			setState(873);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(872);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitStructField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructFieldContext structField() throws RecognitionException {
		StructFieldContext _localctx = new StructFieldContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_structField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(878);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(875);
				outerAttribute();
				}
				}
				setState(880);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(882);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_PUB) {
				{
				setState(881);
				visibility();
				}
			}

			setState(884);
			identifier();
			setState(885);
			match(COLON);
			setState(886);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTupleFields(this);
			else return visitor.visitChildren(this);
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
			setState(888);
			tupleField();
			setState(893);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,87,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(889);
					match(COMMA);
					setState(890);
					tupleField();
					}
					} 
				}
				setState(895);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,87,_ctx);
			}
			setState(897);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(896);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTupleField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TupleFieldContext tupleField() throws RecognitionException {
		TupleFieldContext _localctx = new TupleFieldContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_tupleField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(902);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(899);
				outerAttribute();
				}
				}
				setState(904);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(906);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_PUB) {
				{
				setState(905);
				visibility();
				}
			}

			setState(908);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitEnumeration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumerationContext enumeration() throws RecognitionException {
		EnumerationContext _localctx = new EnumerationContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_enumeration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(910);
			match(KW_ENUM);
			setState(911);
			identifier();
			setState(913);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(912);
				genericParams();
				}
			}

			setState(916);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_WHERE) {
				{
				setState(915);
				whereClause();
				}
			}

			setState(918);
			match(LCURLYBRACE);
			setState(920);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 450359962739146752L) != 0 || _la==POUND) {
				{
				setState(919);
				enumItems();
				}
			}

			setState(922);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitEnumItems(this);
			else return visitor.visitChildren(this);
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
			setState(924);
			enumItem();
			setState(929);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,94,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(925);
					match(COMMA);
					setState(926);
					enumItem();
					}
					} 
				}
				setState(931);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,94,_ctx);
			}
			setState(933);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(932);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitEnumItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumItemContext enumItem() throws RecognitionException {
		EnumItemContext _localctx = new EnumItemContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_enumItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(938);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(935);
				outerAttribute();
				}
				}
				setState(940);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(942);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_PUB) {
				{
				setState(941);
				visibility();
				}
			}

			setState(944);
			identifier();
			setState(948);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				{
				setState(945);
				enumItemTuple();
				}
				break;
			case LCURLYBRACE:
				{
				setState(946);
				enumItemStruct();
				}
				break;
			case EQ:
				{
				setState(947);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitEnumItemTuple(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumItemTupleContext enumItemTuple() throws RecognitionException {
		EnumItemTupleContext _localctx = new EnumItemTupleContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_enumItemTuple);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(950);
			match(LPAREN);
			setState(952);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 567453832542432544L) != 0 || (((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 363114855924105L) != 0) {
				{
				setState(951);
				tupleFields();
				}
			}

			setState(954);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitEnumItemStruct(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumItemStructContext enumItemStruct() throws RecognitionException {
		EnumItemStructContext _localctx = new EnumItemStructContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_enumItemStruct);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(956);
			match(LCURLYBRACE);
			setState(958);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 450359962739146752L) != 0 || _la==POUND) {
				{
				setState(957);
				structFields();
				}
			}

			setState(960);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitEnumItemDiscriminant(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumItemDiscriminantContext enumItemDiscriminant() throws RecognitionException {
		EnumItemDiscriminantContext _localctx = new EnumItemDiscriminantContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_enumItemDiscriminant);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(962);
			match(EQ);
			setState(963);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitUnion_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Union_Context union_() throws RecognitionException {
		Union_Context _localctx = new Union_Context(_ctx, getState());
		enterRule(_localctx, 100, RULE_union_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(965);
			match(KW_UNION);
			setState(966);
			identifier();
			setState(968);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(967);
				genericParams();
				}
			}

			setState(971);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_WHERE) {
				{
				setState(970);
				whereClause();
				}
			}

			setState(973);
			match(LCURLYBRACE);
			setState(974);
			structFields();
			setState(975);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitConstantItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstantItemContext constantItem() throws RecognitionException {
		ConstantItemContext _localctx = new ConstantItemContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_constantItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(977);
			match(KW_CONST);
			setState(980);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_MACRORULES:
			case NON_KEYWORD_IDENTIFIER:
			case RAW_IDENTIFIER:
				{
				setState(978);
				identifier();
				}
				break;
			case UNDERSCORE:
				{
				setState(979);
				match(UNDERSCORE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(982);
			match(COLON);
			setState(983);
			type_();
			setState(986);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ) {
				{
				setState(984);
				match(EQ);
				setState(985);
				expression(0);
				}
			}

			setState(988);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitStaticItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StaticItemContext staticItem() throws RecognitionException {
		StaticItemContext _localctx = new StaticItemContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_staticItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(990);
			match(KW_STATIC);
			setState(992);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_MUT) {
				{
				setState(991);
				match(KW_MUT);
				}
			}

			setState(994);
			identifier();
			setState(995);
			match(COLON);
			setState(996);
			type_();
			setState(999);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ) {
				{
				setState(997);
				match(EQ);
				setState(998);
				expression(0);
				}
			}

			setState(1001);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTrait_(this);
			else return visitor.visitChildren(this);
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
			setState(1004);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_UNSAFE) {
				{
				setState(1003);
				match(KW_UNSAFE);
				}
			}

			setState(1006);
			match(KW_TRAIT);
			setState(1007);
			identifier();
			setState(1009);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(1008);
				genericParams();
				}
			}

			setState(1015);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(1011);
				match(COLON);
				setState(1013);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 567453553367451680L) != 0 || (((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 290545947639809L) != 0) {
					{
					setState(1012);
					typeParamBounds();
					}
				}

				}
			}

			setState(1018);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_WHERE) {
				{
				setState(1017);
				whereClause();
				}
			}

			setState(1020);
			match(LCURLYBRACE);
			setState(1024);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,112,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1021);
					innerAttribute();
					}
					} 
				}
				setState(1026);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,112,_ctx);
			}
			setState(1030);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417632224216360L) != 0 || _la==PATHSEP || _la==POUND) {
				{
				{
				setState(1027);
				associatedItem();
				}
				}
				setState(1032);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1033);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitImplementation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ImplementationContext implementation() throws RecognitionException {
		ImplementationContext _localctx = new ImplementationContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_implementation);
		try {
			setState(1037);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,114,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1035);
				inherentImpl();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1036);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitInherentImpl(this);
			else return visitor.visitChildren(this);
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
			setState(1039);
			match(KW_IMPL);
			setState(1041);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,115,_ctx) ) {
			case 1:
				{
				setState(1040);
				genericParams();
				}
				break;
			}
			setState(1043);
			type_();
			setState(1045);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_WHERE) {
				{
				setState(1044);
				whereClause();
				}
			}

			setState(1047);
			match(LCURLYBRACE);
			setState(1051);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,117,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1048);
					innerAttribute();
					}
					} 
				}
				setState(1053);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,117,_ctx);
			}
			setState(1057);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417632224216360L) != 0 || _la==PATHSEP || _la==POUND) {
				{
				{
				setState(1054);
				associatedItem();
				}
				}
				setState(1059);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1060);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTraitImpl(this);
			else return visitor.visitChildren(this);
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
			setState(1063);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_UNSAFE) {
				{
				setState(1062);
				match(KW_UNSAFE);
				}
			}

			setState(1065);
			match(KW_IMPL);
			setState(1067);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(1066);
				genericParams();
				}
			}

			setState(1070);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(1069);
				match(NOT);
				}
			}

			setState(1072);
			typePath();
			setState(1073);
			match(KW_FOR);
			setState(1074);
			type_();
			setState(1076);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_WHERE) {
				{
				setState(1075);
				whereClause();
				}
			}

			setState(1078);
			match(LCURLYBRACE);
			setState(1082);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,123,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1079);
					innerAttribute();
					}
					} 
				}
				setState(1084);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,123,_ctx);
			}
			setState(1088);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417632224216360L) != 0 || _la==PATHSEP || _la==POUND) {
				{
				{
				setState(1085);
				associatedItem();
				}
				}
				setState(1090);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1091);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitExternBlock(this);
			else return visitor.visitChildren(this);
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
			setState(1094);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_UNSAFE) {
				{
				setState(1093);
				match(KW_UNSAFE);
				}
			}

			setState(1096);
			match(KW_EXTERN);
			setState(1098);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==STRING_LITERAL || _la==RAW_STRING_LITERAL) {
				{
				setState(1097);
				abi();
				}
			}

			setState(1100);
			match(LCURLYBRACE);
			setState(1104);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,127,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1101);
					innerAttribute();
					}
					} 
				}
				setState(1106);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,127,_ctx);
			}
			setState(1110);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417630143841576L) != 0 || _la==PATHSEP || _la==POUND) {
				{
				{
				setState(1107);
				externalItem();
				}
				}
				setState(1112);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1113);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitExternalItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExternalItemContext externalItem() throws RecognitionException {
		ExternalItemContext _localctx = new ExternalItemContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_externalItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1118);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(1115);
				outerAttribute();
				}
				}
				setState(1120);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1129);
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
				setState(1121);
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
				setState(1123);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==KW_PUB) {
					{
					setState(1122);
					visibility();
					}
				}

				setState(1127);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case KW_STATIC:
					{
					setState(1125);
					staticItem();
					}
					break;
				case KW_CONST:
				case KW_EXTERN:
				case KW_FN:
				case KW_UNSAFE:
				case KW_ASYNC:
					{
					setState(1126);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitGenericParams(this);
			else return visitor.visitChildren(this);
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
			setState(1131);
			match(LT);
			setState(1144);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 450359962737049608L) != 0 || _la==LIFETIME_OR_LABEL || _la==POUND) {
				{
				setState(1137);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,133,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1132);
						genericParam();
						setState(1133);
						match(COMMA);
						}
						} 
					}
					setState(1139);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,133,_ctx);
				}
				setState(1140);
				genericParam();
				setState(1142);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(1141);
					match(COMMA);
					}
				}

				}
			}

			setState(1146);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitGenericParam(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericParamContext genericParam() throws RecognitionException {
		GenericParamContext _localctx = new GenericParamContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_genericParam);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1151);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,136,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1148);
					outerAttribute();
					}
					} 
				}
				setState(1153);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,136,_ctx);
			}
			setState(1157);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,137,_ctx) ) {
			case 1:
				{
				setState(1154);
				lifetimeParam();
				}
				break;
			case 2:
				{
				setState(1155);
				typeParam();
				}
				break;
			case 3:
				{
				setState(1156);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitLifetimeParam(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LifetimeParamContext lifetimeParam() throws RecognitionException {
		LifetimeParamContext _localctx = new LifetimeParamContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_lifetimeParam);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1160);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==POUND) {
				{
				setState(1159);
				outerAttribute();
				}
			}

			setState(1162);
			match(LIFETIME_OR_LABEL);
			setState(1165);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(1163);
				match(COLON);
				setState(1164);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTypeParam(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeParamContext typeParam() throws RecognitionException {
		TypeParamContext _localctx = new TypeParamContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_typeParam);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1168);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==POUND) {
				{
				setState(1167);
				outerAttribute();
				}
			}

			setState(1170);
			identifier();
			setState(1175);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(1171);
				match(COLON);
				setState(1173);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 567453553367451680L) != 0 || (((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 290545947639809L) != 0) {
					{
					setState(1172);
					typeParamBounds();
					}
				}

				}
			}

			setState(1179);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ) {
				{
				setState(1177);
				match(EQ);
				setState(1178);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitConstParam(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstParamContext constParam() throws RecognitionException {
		ConstParamContext _localctx = new ConstParamContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_constParam);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1181);
			match(KW_CONST);
			setState(1182);
			identifier();
			setState(1183);
			match(COLON);
			setState(1184);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitWhereClause(this);
			else return visitor.visitChildren(this);
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
			setState(1186);
			match(KW_WHERE);
			setState(1192);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,144,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1187);
					whereClauseItem();
					setState(1188);
					match(COMMA);
					}
					} 
				}
				setState(1194);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,144,_ctx);
			}
			setState(1196);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 567453832540335392L) != 0 || (((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 360915832668553L) != 0) {
				{
				setState(1195);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitWhereClauseItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhereClauseItemContext whereClauseItem() throws RecognitionException {
		WhereClauseItemContext _localctx = new WhereClauseItemContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_whereClauseItem);
		try {
			setState(1200);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,146,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1198);
				lifetimeWhereClauseItem();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1199);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitLifetimeWhereClauseItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LifetimeWhereClauseItemContext lifetimeWhereClauseItem() throws RecognitionException {
		LifetimeWhereClauseItemContext _localctx = new LifetimeWhereClauseItemContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_lifetimeWhereClauseItem);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1202);
			lifetime();
			setState(1203);
			match(COLON);
			setState(1204);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTypeBoundWhereClauseItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeBoundWhereClauseItemContext typeBoundWhereClauseItem() throws RecognitionException {
		TypeBoundWhereClauseItemContext _localctx = new TypeBoundWhereClauseItemContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_typeBoundWhereClauseItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1207);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,147,_ctx) ) {
			case 1:
				{
				setState(1206);
				forLifetimes();
				}
				break;
			}
			setState(1209);
			type_();
			setState(1210);
			match(COLON);
			setState(1212);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 567453553367451680L) != 0 || (((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 290545947639809L) != 0) {
				{
				setState(1211);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitForLifetimes(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ForLifetimesContext forLifetimes() throws RecognitionException {
		ForLifetimesContext _localctx = new ForLifetimesContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_forLifetimes);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1214);
			match(KW_FOR);
			setState(1215);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitAssociatedItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssociatedItemContext associatedItem() throws RecognitionException {
		AssociatedItemContext _localctx = new AssociatedItemContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_associatedItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1220);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(1217);
				outerAttribute();
				}
				}
				setState(1222);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1232);
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
				setState(1223);
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
				setState(1225);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==KW_PUB) {
					{
					setState(1224);
					visibility();
					}
				}

				setState(1230);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,151,_ctx) ) {
				case 1:
					{
					setState(1227);
					typeAlias();
					}
					break;
				case 2:
					{
					setState(1228);
					constantItem();
					}
					break;
				case 3:
					{
					setState(1229);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitInnerAttribute(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InnerAttributeContext innerAttribute() throws RecognitionException {
		InnerAttributeContext _localctx = new InnerAttributeContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_innerAttribute);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1234);
			match(POUND);
			setState(1235);
			match(NOT);
			setState(1236);
			match(LSQUAREBRACKET);
			setState(1237);
			attr();
			setState(1238);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitOuterAttribute(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OuterAttributeContext outerAttribute() throws RecognitionException {
		OuterAttributeContext _localctx = new OuterAttributeContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_outerAttribute);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1240);
			match(POUND);
			setState(1241);
			match(LSQUAREBRACKET);
			setState(1242);
			attr();
			setState(1243);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitAttr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttrContext attr() throws RecognitionException {
		AttrContext _localctx = new AttrContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_attr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1245);
			simplePath();
			setState(1247);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la - 101)) & ~0x3f) == 0 && ((1L << (_la - 101)) & 88080385L) != 0) {
				{
				setState(1246);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitAttrInput(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttrInputContext attrInput() throws RecognitionException {
		AttrInputContext _localctx = new AttrInputContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_attrInput);
		try {
			setState(1252);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LCURLYBRACE:
			case LSQUAREBRACKET:
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(1249);
				delimTokenTree();
				}
				break;
			case EQ:
				enterOuterAlt(_localctx, 2);
				{
				setState(1250);
				match(EQ);
				setState(1251);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_statement);
		try {
			setState(1259);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,155,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1254);
				match(SEMI);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1255);
				item();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1256);
				letStatement();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1257);
				expressionStatement();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1258);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitLetStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LetStatementContext letStatement() throws RecognitionException {
		LetStatementContext _localctx = new LetStatementContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_letStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1264);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(1261);
				outerAttribute();
				}
				}
				setState(1266);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1267);
			match(KW_LET);
			setState(1268);
			patternNoTopAlt();
			setState(1271);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(1269);
				match(COLON);
				setState(1270);
				type_();
				}
			}

			setState(1275);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EQ) {
				{
				setState(1273);
				match(EQ);
				setState(1274);
				expression(0);
				}
			}

			setState(1277);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitExpressionStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionStatementContext expressionStatement() throws RecognitionException {
		ExpressionStatementContext _localctx = new ExpressionStatementContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_expressionStatement);
		try {
			setState(1286);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,160,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1279);
				expression(0);
				setState(1280);
				match(SEMI);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1282);
				expressionWithBlock();
				setState(1284);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,159,_ctx) ) {
				case 1:
					{
					setState(1283);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTypeCastExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PathExpression_Context extends ExpressionContext {
		public PathExpressionContext pathExpression() {
			return getRuleContext(PathExpressionContext.class,0);
		}
		public PathExpression_Context(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitPathExpression_(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTupleExpression(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitIndexExpression(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitRangeExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MacroInvocationAsExpressionContext extends ExpressionContext {
		public MacroInvocationContext macroInvocation() {
			return getRuleContext(MacroInvocationContext.class,0);
		}
		public MacroInvocationAsExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMacroInvocationAsExpression(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitReturnExpression(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitAwaitExpression(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitErrorPropagationExpression(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitContinueExpression(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitAssignmentExpression(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMethodCallExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LiteralExpression_Context extends ExpressionContext {
		public LiteralExpressionContext literalExpression() {
			return getRuleContext(LiteralExpressionContext.class,0);
		}
		public LiteralExpression_Context(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitLiteralExpression_(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StructExpression_Context extends ExpressionContext {
		public StructExpressionContext structExpression() {
			return getRuleContext(StructExpressionContext.class,0);
		}
		public StructExpression_Context(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitStructExpression_(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTupleIndexingExpression(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitNegationExpression(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitCallExpression(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitLazyBooleanExpression(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitDereferenceExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionWithBlock_Context extends ExpressionContext {
		public ExpressionWithBlockContext expressionWithBlock() {
			return getRuleContext(ExpressionWithBlockContext.class,0);
		}
		public ExpressionWithBlock_Context(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitExpressionWithBlock_(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitGroupedExpression(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitBreakExpression(this);
			else return visitor.visitChildren(this);
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
		public TerminalNode AND() { return getToken(RustParser.AND, 0); }
		public TerminalNode CARET() { return getToken(RustParser.CARET, 0); }
		public TerminalNode OR() { return getToken(RustParser.OR, 0); }
		public ShiftOperatorContext shiftOperator() {
			return getRuleContext(ShiftOperatorContext.class,0);
		}
		public ArithmeticOrLogicalExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitArithmeticOrLogicalExpression(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitFieldExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class EnumerationVariantExpression_Context extends ExpressionContext {
		public EnumerationVariantExpressionContext enumerationVariantExpression() {
			return getRuleContext(EnumerationVariantExpressionContext.class,0);
		}
		public EnumerationVariantExpression_Context(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitEnumerationVariantExpression_(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitComparisonExpression(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitAttributedExpression(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitBorrowExpression(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitCompoundAssignmentExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ClosureExpression_Context extends ExpressionContext {
		public ClosureExpressionContext closureExpression() {
			return getRuleContext(ClosureExpressionContext.class,0);
		}
		public ClosureExpression_Context(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitClosureExpression_(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitArrayExpression(this);
			else return visitor.visitChildren(this);
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
			setState(1368);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,174,_ctx) ) {
			case 1:
				{
				_localctx = new AttributedExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(1290); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(1289);
						outerAttribute();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1292); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,161,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(1294);
				expression(40);
				}
				break;
			case 2:
				{
				_localctx = new LiteralExpression_Context(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1296);
				literalExpression();
				}
				break;
			case 3:
				{
				_localctx = new PathExpression_Context(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1297);
				pathExpression();
				}
				break;
			case 4:
				{
				_localctx = new BorrowExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1298);
				_la = _input.LA(1);
				if ( !(_la==AND || _la==ANDAND) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1300);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==KW_MUT) {
					{
					setState(1299);
					match(KW_MUT);
					}
				}

				setState(1302);
				expression(30);
				}
				break;
			case 5:
				{
				_localctx = new DereferenceExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1303);
				match(STAR);
				setState(1304);
				expression(29);
				}
				break;
			case 6:
				{
				_localctx = new NegationExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1305);
				_la = _input.LA(1);
				if ( !(_la==MINUS || _la==NOT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1306);
				expression(28);
				}
				break;
			case 7:
				{
				_localctx = new RangeExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1307);
				match(DOTDOT);
				setState(1309);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,163,_ctx) ) {
				case 1:
					{
					setState(1308);
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
				setState(1311);
				match(DOTDOTEQ);
				setState(1312);
				expression(15);
				}
				break;
			case 9:
				{
				_localctx = new ContinueExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1313);
				match(KW_CONTINUE);
				setState(1315);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,164,_ctx) ) {
				case 1:
					{
					setState(1314);
					match(LIFETIME_OR_LABEL);
					}
					break;
				}
				setState(1318);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,165,_ctx) ) {
				case 1:
					{
					setState(1317);
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
				setState(1320);
				match(KW_BREAK);
				setState(1322);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,166,_ctx) ) {
				case 1:
					{
					setState(1321);
					match(LIFETIME_OR_LABEL);
					}
					break;
				}
				setState(1325);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,167,_ctx) ) {
				case 1:
					{
					setState(1324);
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
				setState(1327);
				match(KW_RETURN);
				setState(1329);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,168,_ctx) ) {
				case 1:
					{
					setState(1328);
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
				setState(1331);
				match(LPAREN);
				setState(1335);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,169,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1332);
						innerAttribute();
						}
						} 
					}
					setState(1337);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,169,_ctx);
				}
				setState(1338);
				expression(0);
				setState(1339);
				match(RPAREN);
				}
				break;
			case 13:
				{
				_localctx = new ArrayExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1341);
				match(LSQUAREBRACKET);
				setState(1345);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,170,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1342);
						innerAttribute();
						}
						} 
					}
					setState(1347);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,170,_ctx);
				}
				setState(1349);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417665550785076L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1523430809782507647L) != 0) {
					{
					setState(1348);
					arrayElements();
					}
				}

				setState(1351);
				match(RSQUAREBRACKET);
				}
				break;
			case 14:
				{
				_localctx = new TupleExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1352);
				match(LPAREN);
				setState(1356);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,172,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1353);
						innerAttribute();
						}
						} 
					}
					setState(1358);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,172,_ctx);
				}
				setState(1360);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417665550785076L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1523430809782507647L) != 0) {
					{
					setState(1359);
					tupleElements();
					}
				}

				setState(1362);
				match(RPAREN);
				}
				break;
			case 15:
				{
				_localctx = new StructExpression_Context(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1363);
				structExpression();
				}
				break;
			case 16:
				{
				_localctx = new EnumerationVariantExpression_Context(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1364);
				enumerationVariantExpression();
				}
				break;
			case 17:
				{
				_localctx = new ClosureExpression_Context(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1365);
				closureExpression();
				}
				break;
			case 18:
				{
				_localctx = new ExpressionWithBlock_Context(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1366);
				expressionWithBlock();
				}
				break;
			case 19:
				{
				_localctx = new MacroInvocationAsExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1367);
				macroInvocation();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(1450);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,179,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(1448);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,178,_ctx) ) {
					case 1:
						{
						_localctx = new ArithmeticOrLogicalExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1370);
						if (!(precpred(_ctx, 26))) throw new FailedPredicateException(this, "precpred(_ctx, 26)");
						setState(1371);
						_la = _input.LA(1);
						if ( !((((_la - 82)) & ~0x3f) == 0 && ((1L << (_la - 82)) & 7L) != 0) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(1372);
						expression(27);
						}
						break;
					case 2:
						{
						_localctx = new ArithmeticOrLogicalExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1373);
						if (!(precpred(_ctx, 25))) throw new FailedPredicateException(this, "precpred(_ctx, 25)");
						setState(1374);
						_la = _input.LA(1);
						if ( !(_la==PLUS || _la==MINUS) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(1375);
						expression(26);
						}
						break;
					case 3:
						{
						_localctx = new ArithmeticOrLogicalExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1376);
						if (!(precpred(_ctx, 24))) throw new FailedPredicateException(this, "precpred(_ctx, 24)");
						setState(1377);
						match(AND);
						setState(1378);
						expression(25);
						}
						break;
					case 4:
						{
						_localctx = new ArithmeticOrLogicalExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1379);
						if (!(precpred(_ctx, 23))) throw new FailedPredicateException(this, "precpred(_ctx, 23)");
						setState(1380);
						match(CARET);
						setState(1381);
						expression(24);
						}
						break;
					case 5:
						{
						_localctx = new ArithmeticOrLogicalExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1382);
						if (!(precpred(_ctx, 22))) throw new FailedPredicateException(this, "precpred(_ctx, 22)");
						setState(1383);
						match(OR);
						setState(1384);
						expression(23);
						}
						break;
					case 6:
						{
						_localctx = new ArithmeticOrLogicalExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1385);
						if (!(precpred(_ctx, 21))) throw new FailedPredicateException(this, "precpred(_ctx, 21)");
						setState(1386);
						shiftOperator();
						setState(1387);
						expression(22);
						}
						break;
					case 7:
						{
						_localctx = new ComparisonExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1389);
						if (!(precpred(_ctx, 20))) throw new FailedPredicateException(this, "precpred(_ctx, 20)");
						setState(1390);
						comparisonOperator();
						setState(1391);
						expression(21);
						}
						break;
					case 8:
						{
						_localctx = new LazyBooleanExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1393);
						if (!(precpred(_ctx, 19))) throw new FailedPredicateException(this, "precpred(_ctx, 19)");
						setState(1394);
						match(ANDAND);
						setState(1395);
						expression(20);
						}
						break;
					case 9:
						{
						_localctx = new LazyBooleanExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1396);
						if (!(precpred(_ctx, 18))) throw new FailedPredicateException(this, "precpred(_ctx, 18)");
						setState(1397);
						match(OROR);
						setState(1398);
						expression(19);
						}
						break;
					case 10:
						{
						_localctx = new RangeExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1399);
						if (!(precpred(_ctx, 14))) throw new FailedPredicateException(this, "precpred(_ctx, 14)");
						setState(1400);
						match(DOTDOTEQ);
						setState(1401);
						expression(15);
						}
						break;
					case 11:
						{
						_localctx = new AssignmentExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1402);
						if (!(precpred(_ctx, 13))) throw new FailedPredicateException(this, "precpred(_ctx, 13)");
						setState(1403);
						match(EQ);
						setState(1404);
						expression(14);
						}
						break;
					case 12:
						{
						_localctx = new CompoundAssignmentExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1405);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(1406);
						compoundAssignOperator();
						setState(1407);
						expression(13);
						}
						break;
					case 13:
						{
						_localctx = new MethodCallExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1409);
						if (!(precpred(_ctx, 37))) throw new FailedPredicateException(this, "precpred(_ctx, 37)");
						setState(1410);
						match(DOT);
						setState(1411);
						pathExprSegment();
						setState(1412);
						match(LPAREN);
						setState(1414);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417665550785076L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1523430809782507647L) != 0) {
							{
							setState(1413);
							callParams();
							}
						}

						setState(1416);
						match(RPAREN);
						}
						break;
					case 14:
						{
						_localctx = new FieldExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1418);
						if (!(precpred(_ctx, 36))) throw new FailedPredicateException(this, "precpred(_ctx, 36)");
						setState(1419);
						match(DOT);
						setState(1420);
						identifier();
						}
						break;
					case 15:
						{
						_localctx = new TupleIndexingExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1421);
						if (!(precpred(_ctx, 35))) throw new FailedPredicateException(this, "precpred(_ctx, 35)");
						setState(1422);
						match(DOT);
						setState(1423);
						tupleIndex();
						}
						break;
					case 16:
						{
						_localctx = new AwaitExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1424);
						if (!(precpred(_ctx, 34))) throw new FailedPredicateException(this, "precpred(_ctx, 34)");
						setState(1425);
						match(DOT);
						setState(1426);
						match(KW_AWAIT);
						}
						break;
					case 17:
						{
						_localctx = new CallExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1427);
						if (!(precpred(_ctx, 33))) throw new FailedPredicateException(this, "precpred(_ctx, 33)");
						setState(1428);
						match(LPAREN);
						setState(1430);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417665550785076L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1523430809782507647L) != 0) {
							{
							setState(1429);
							callParams();
							}
						}

						setState(1432);
						match(RPAREN);
						}
						break;
					case 18:
						{
						_localctx = new IndexExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1433);
						if (!(precpred(_ctx, 32))) throw new FailedPredicateException(this, "precpred(_ctx, 32)");
						setState(1434);
						match(LSQUAREBRACKET);
						setState(1435);
						expression(0);
						setState(1436);
						match(RSQUAREBRACKET);
						}
						break;
					case 19:
						{
						_localctx = new ErrorPropagationExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1438);
						if (!(precpred(_ctx, 31))) throw new FailedPredicateException(this, "precpred(_ctx, 31)");
						setState(1439);
						match(QUESTION);
						}
						break;
					case 20:
						{
						_localctx = new TypeCastExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1440);
						if (!(precpred(_ctx, 27))) throw new FailedPredicateException(this, "precpred(_ctx, 27)");
						setState(1441);
						match(KW_AS);
						setState(1442);
						typeNoBounds();
						}
						break;
					case 21:
						{
						_localctx = new RangeExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1443);
						if (!(precpred(_ctx, 17))) throw new FailedPredicateException(this, "precpred(_ctx, 17)");
						setState(1444);
						match(DOTDOT);
						setState(1446);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,177,_ctx) ) {
						case 1:
							{
							setState(1445);
							expression(0);
							}
							break;
						}
						}
						break;
					}
					} 
				}
				setState(1452);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,179,_ctx);
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
	public static class ShiftOperatorContext extends ParserRuleContext {
		public List<TerminalNode> GT() { return getTokens(RustParser.GT); }
		public TerminalNode GT(int i) {
			return getToken(RustParser.GT, i);
		}
		public List<TerminalNode> LT() { return getTokens(RustParser.LT); }
		public TerminalNode LT(int i) {
			return getToken(RustParser.LT, i);
		}
		public ShiftOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_shiftOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitShiftOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ShiftOperatorContext shiftOperator() throws RecognitionException {
		ShiftOperatorContext _localctx = new ShiftOperatorContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_shiftOperator);
		try {
			setState(1457);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case GT:
				enterOuterAlt(_localctx, 1);
				{
				setState(1453);
				match(GT);
				setState(1454);
				match(GT);
				}
				break;
			case LT:
				enterOuterAlt(_localctx, 2);
				{
				setState(1455);
				match(LT);
				setState(1456);
				match(LT);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitComparisonOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComparisonOperatorContext comparisonOperator() throws RecognitionException {
		ComparisonOperatorContext _localctx = new ComparisonOperatorContext(_ctx, getState());
		enterRule(_localctx, 158, RULE_comparisonOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1459);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitCompoundAssignOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CompoundAssignOperatorContext compoundAssignOperator() throws RecognitionException {
		CompoundAssignOperatorContext _localctx = new CompoundAssignOperatorContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_compoundAssignOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1461);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitExpressionWithBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionWithBlockContext expressionWithBlock() throws RecognitionException {
		ExpressionWithBlockContext _localctx = new ExpressionWithBlockContext(_ctx, getState());
		enterRule(_localctx, 162, RULE_expressionWithBlock);
		try {
			int _alt;
			setState(1477);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,182,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1464); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(1463);
						outerAttribute();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1466); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,181,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(1468);
				expressionWithBlock();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1470);
				blockExpression();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1471);
				asyncBlockExpression();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1472);
				unsafeBlockExpression();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1473);
				loopExpression();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1474);
				ifExpression();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1475);
				ifLetExpression();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(1476);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitLiteralExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralExpressionContext literalExpression() throws RecognitionException {
		LiteralExpressionContext _localctx = new LiteralExpressionContext(_ctx, getState());
		enterRule(_localctx, 164, RULE_literalExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1479);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitPathExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PathExpressionContext pathExpression() throws RecognitionException {
		PathExpressionContext _localctx = new PathExpressionContext(_ctx, getState());
		enterRule(_localctx, 166, RULE_pathExpression);
		try {
			setState(1483);
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
				setState(1481);
				pathInExpression();
				}
				break;
			case LT:
				enterOuterAlt(_localctx, 2);
				{
				setState(1482);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitBlockExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlockExpressionContext blockExpression() throws RecognitionException {
		BlockExpressionContext _localctx = new BlockExpressionContext(_ctx, getState());
		enterRule(_localctx, 168, RULE_blockExpression);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1485);
			match(LCURLYBRACE);
			setState(1489);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,184,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1486);
					innerAttribute();
					}
					} 
				}
				setState(1491);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,184,_ctx);
			}
			setState(1493);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 526921276656172988L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1523712284759218303L) != 0) {
				{
				setState(1492);
				statements();
				}
			}

			setState(1495);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitStatements(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementsContext statements() throws RecognitionException {
		StatementsContext _localctx = new StatementsContext(_ctx, getState());
		enterRule(_localctx, 170, RULE_statements);
		int _la;
		try {
			int _alt;
			setState(1506);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,188,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1498); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(1497);
						statement();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1500); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,186,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(1503);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417665550785076L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1523430809782507647L) != 0) {
					{
					setState(1502);
					expression(0);
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1505);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitAsyncBlockExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AsyncBlockExpressionContext asyncBlockExpression() throws RecognitionException {
		AsyncBlockExpressionContext _localctx = new AsyncBlockExpressionContext(_ctx, getState());
		enterRule(_localctx, 172, RULE_asyncBlockExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1508);
			match(KW_ASYNC);
			setState(1510);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_MOVE) {
				{
				setState(1509);
				match(KW_MOVE);
				}
			}

			setState(1512);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitUnsafeBlockExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnsafeBlockExpressionContext unsafeBlockExpression() throws RecognitionException {
		UnsafeBlockExpressionContext _localctx = new UnsafeBlockExpressionContext(_ctx, getState());
		enterRule(_localctx, 174, RULE_unsafeBlockExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1514);
			match(KW_UNSAFE);
			setState(1515);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitArrayElements(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayElementsContext arrayElements() throws RecognitionException {
		ArrayElementsContext _localctx = new ArrayElementsContext(_ctx, getState());
		enterRule(_localctx, 176, RULE_arrayElements);
		int _la;
		try {
			int _alt;
			setState(1532);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,192,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1517);
				expression(0);
				setState(1522);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,190,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1518);
						match(COMMA);
						setState(1519);
						expression(0);
						}
						} 
					}
					setState(1524);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,190,_ctx);
				}
				setState(1526);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(1525);
					match(COMMA);
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1528);
				expression(0);
				setState(1529);
				match(SEMI);
				setState(1530);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTupleElements(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TupleElementsContext tupleElements() throws RecognitionException {
		TupleElementsContext _localctx = new TupleElementsContext(_ctx, getState());
		enterRule(_localctx, 178, RULE_tupleElements);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1537); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(1534);
					expression(0);
					setState(1535);
					match(COMMA);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(1539); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,193,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(1542);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417665550785076L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1523430809782507647L) != 0) {
				{
				setState(1541);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTupleIndex(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TupleIndexContext tupleIndex() throws RecognitionException {
		TupleIndexContext _localctx = new TupleIndexContext(_ctx, getState());
		enterRule(_localctx, 180, RULE_tupleIndex);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1544);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitStructExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructExpressionContext structExpression() throws RecognitionException {
		StructExpressionContext _localctx = new StructExpressionContext(_ctx, getState());
		enterRule(_localctx, 182, RULE_structExpression);
		try {
			setState(1549);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,195,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1546);
				structExprStruct();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1547);
				structExprTuple();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1548);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitStructExprStruct(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructExprStructContext structExprStruct() throws RecognitionException {
		StructExprStructContext _localctx = new StructExprStructContext(_ctx, getState());
		enterRule(_localctx, 184, RULE_structExprStruct);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1551);
			pathInExpression();
			setState(1552);
			match(LCURLYBRACE);
			setState(1556);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,196,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1553);
					innerAttribute();
					}
					} 
				}
				setState(1558);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,196,_ctx);
			}
			setState(1561);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_MACRORULES:
			case NON_KEYWORD_IDENTIFIER:
			case RAW_IDENTIFIER:
			case INTEGER_LITERAL:
			case POUND:
				{
				setState(1559);
				structExprFields();
				}
				break;
			case DOTDOT:
				{
				setState(1560);
				structBase();
				}
				break;
			case RCURLYBRACE:
				break;
			default:
				break;
			}
			setState(1563);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitStructExprFields(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructExprFieldsContext structExprFields() throws RecognitionException {
		StructExprFieldsContext _localctx = new StructExprFieldsContext(_ctx, getState());
		enterRule(_localctx, 186, RULE_structExprFields);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1565);
			structExprField();
			setState(1570);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,198,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1566);
					match(COMMA);
					setState(1567);
					structExprField();
					}
					} 
				}
				setState(1572);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,198,_ctx);
			}
			setState(1578);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,200,_ctx) ) {
			case 1:
				{
				setState(1573);
				match(COMMA);
				setState(1574);
				structBase();
				}
				break;
			case 2:
				{
				setState(1576);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(1575);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitStructExprField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructExprFieldContext structExprField() throws RecognitionException {
		StructExprFieldContext _localctx = new StructExprFieldContext(_ctx, getState());
		enterRule(_localctx, 188, RULE_structExprField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1583);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(1580);
				outerAttribute();
				}
				}
				setState(1585);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1594);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,203,_ctx) ) {
			case 1:
				{
				setState(1586);
				identifier();
				}
				break;
			case 2:
				{
				setState(1589);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case KW_MACRORULES:
				case NON_KEYWORD_IDENTIFIER:
				case RAW_IDENTIFIER:
					{
					setState(1587);
					identifier();
					}
					break;
				case INTEGER_LITERAL:
					{
					setState(1588);
					tupleIndex();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(1591);
				match(COLON);
				setState(1592);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitStructBase(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructBaseContext structBase() throws RecognitionException {
		StructBaseContext _localctx = new StructBaseContext(_ctx, getState());
		enterRule(_localctx, 190, RULE_structBase);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1596);
			match(DOTDOT);
			setState(1597);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitStructExprTuple(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructExprTupleContext structExprTuple() throws RecognitionException {
		StructExprTupleContext _localctx = new StructExprTupleContext(_ctx, getState());
		enterRule(_localctx, 192, RULE_structExprTuple);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1599);
			pathInExpression();
			setState(1600);
			match(LPAREN);
			setState(1604);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,204,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1601);
					innerAttribute();
					}
					} 
				}
				setState(1606);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,204,_ctx);
			}
			setState(1618);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417665550785076L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1523430809782507647L) != 0) {
				{
				setState(1607);
				expression(0);
				setState(1612);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,205,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1608);
						match(COMMA);
						setState(1609);
						expression(0);
						}
						} 
					}
					setState(1614);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,205,_ctx);
				}
				setState(1616);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(1615);
					match(COMMA);
					}
				}

				}
			}

			setState(1620);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitStructExprUnit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructExprUnitContext structExprUnit() throws RecognitionException {
		StructExprUnitContext _localctx = new StructExprUnitContext(_ctx, getState());
		enterRule(_localctx, 194, RULE_structExprUnit);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1622);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitEnumerationVariantExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumerationVariantExpressionContext enumerationVariantExpression() throws RecognitionException {
		EnumerationVariantExpressionContext _localctx = new EnumerationVariantExpressionContext(_ctx, getState());
		enterRule(_localctx, 196, RULE_enumerationVariantExpression);
		try {
			setState(1627);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,208,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1624);
				enumExprStruct();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1625);
				enumExprTuple();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1626);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitEnumExprStruct(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumExprStructContext enumExprStruct() throws RecognitionException {
		EnumExprStructContext _localctx = new EnumExprStructContext(_ctx, getState());
		enterRule(_localctx, 198, RULE_enumExprStruct);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1629);
			pathInExpression();
			setState(1630);
			match(LCURLYBRACE);
			setState(1632);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la - 54)) & ~0x3f) == 0 && ((1L << (_la - 54)) & 524313L) != 0) {
				{
				setState(1631);
				enumExprFields();
				}
			}

			setState(1634);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitEnumExprFields(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumExprFieldsContext enumExprFields() throws RecognitionException {
		EnumExprFieldsContext _localctx = new EnumExprFieldsContext(_ctx, getState());
		enterRule(_localctx, 200, RULE_enumExprFields);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1636);
			enumExprField();
			setState(1641);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,210,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1637);
					match(COMMA);
					setState(1638);
					enumExprField();
					}
					} 
				}
				setState(1643);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,210,_ctx);
			}
			setState(1645);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(1644);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitEnumExprField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumExprFieldContext enumExprField() throws RecognitionException {
		EnumExprFieldContext _localctx = new EnumExprFieldContext(_ctx, getState());
		enterRule(_localctx, 202, RULE_enumExprField);
		try {
			setState(1655);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,213,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1647);
				identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1650);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case KW_MACRORULES:
				case NON_KEYWORD_IDENTIFIER:
				case RAW_IDENTIFIER:
					{
					setState(1648);
					identifier();
					}
					break;
				case INTEGER_LITERAL:
					{
					setState(1649);
					tupleIndex();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(1652);
				match(COLON);
				setState(1653);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitEnumExprTuple(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumExprTupleContext enumExprTuple() throws RecognitionException {
		EnumExprTupleContext _localctx = new EnumExprTupleContext(_ctx, getState());
		enterRule(_localctx, 204, RULE_enumExprTuple);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1657);
			pathInExpression();
			setState(1658);
			match(LPAREN);
			setState(1670);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417665550785076L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1523430809782507647L) != 0) {
				{
				setState(1659);
				expression(0);
				setState(1664);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,214,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1660);
						match(COMMA);
						setState(1661);
						expression(0);
						}
						} 
					}
					setState(1666);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,214,_ctx);
				}
				setState(1668);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(1667);
					match(COMMA);
					}
				}

				}
			}

			setState(1672);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitEnumExprFieldless(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumExprFieldlessContext enumExprFieldless() throws RecognitionException {
		EnumExprFieldlessContext _localctx = new EnumExprFieldlessContext(_ctx, getState());
		enterRule(_localctx, 206, RULE_enumExprFieldless);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1674);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitCallParams(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CallParamsContext callParams() throws RecognitionException {
		CallParamsContext _localctx = new CallParamsContext(_ctx, getState());
		enterRule(_localctx, 208, RULE_callParams);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1676);
			expression(0);
			setState(1681);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,217,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1677);
					match(COMMA);
					setState(1678);
					expression(0);
					}
					} 
				}
				setState(1683);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,217,_ctx);
			}
			setState(1685);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(1684);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitClosureExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClosureExpressionContext closureExpression() throws RecognitionException {
		ClosureExpressionContext _localctx = new ClosureExpressionContext(_ctx, getState());
		enterRule(_localctx, 210, RULE_closureExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1688);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_MOVE) {
				{
				setState(1687);
				match(KW_MOVE);
				}
			}

			setState(1696);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case OROR:
				{
				setState(1690);
				match(OROR);
				}
				break;
			case OR:
				{
				setState(1691);
				match(OR);
				setState(1693);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,220,_ctx) ) {
				case 1:
					{
					setState(1692);
					closureParameters();
					}
					break;
				}
				setState(1695);
				match(OR);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(1703);
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
				setState(1698);
				expression(0);
				}
				break;
			case RARROW:
				{
				setState(1699);
				match(RARROW);
				setState(1700);
				typeNoBounds();
				setState(1701);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitClosureParameters(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClosureParametersContext closureParameters() throws RecognitionException {
		ClosureParametersContext _localctx = new ClosureParametersContext(_ctx, getState());
		enterRule(_localctx, 212, RULE_closureParameters);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1705);
			closureParam();
			setState(1710);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,223,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1706);
					match(COMMA);
					setState(1707);
					closureParam();
					}
					} 
				}
				setState(1712);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,223,_ctx);
			}
			setState(1714);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(1713);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitClosureParam(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClosureParamContext closureParam() throws RecognitionException {
		ClosureParamContext _localctx = new ClosureParamContext(_ctx, getState());
		enterRule(_localctx, 214, RULE_closureParam);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1719);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(1716);
				outerAttribute();
				}
				}
				setState(1721);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1722);
			pattern();
			setState(1725);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(1723);
				match(COLON);
				setState(1724);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitLoopExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LoopExpressionContext loopExpression() throws RecognitionException {
		LoopExpressionContext _localctx = new LoopExpressionContext(_ctx, getState());
		enterRule(_localctx, 216, RULE_loopExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1728);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LIFETIME_OR_LABEL) {
				{
				setState(1727);
				loopLabel();
				}
			}

			setState(1734);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,228,_ctx) ) {
			case 1:
				{
				setState(1730);
				infiniteLoopExpression();
				}
				break;
			case 2:
				{
				setState(1731);
				predicateLoopExpression();
				}
				break;
			case 3:
				{
				setState(1732);
				predicatePatternLoopExpression();
				}
				break;
			case 4:
				{
				setState(1733);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitInfiniteLoopExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InfiniteLoopExpressionContext infiniteLoopExpression() throws RecognitionException {
		InfiniteLoopExpressionContext _localctx = new InfiniteLoopExpressionContext(_ctx, getState());
		enterRule(_localctx, 218, RULE_infiniteLoopExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1736);
			match(KW_LOOP);
			setState(1737);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitPredicateLoopExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicateLoopExpressionContext predicateLoopExpression() throws RecognitionException {
		PredicateLoopExpressionContext _localctx = new PredicateLoopExpressionContext(_ctx, getState());
		enterRule(_localctx, 220, RULE_predicateLoopExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1739);
			match(KW_WHILE);
			setState(1740);
			expression(0);
			setState(1741);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitPredicatePatternLoopExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicatePatternLoopExpressionContext predicatePatternLoopExpression() throws RecognitionException {
		PredicatePatternLoopExpressionContext _localctx = new PredicatePatternLoopExpressionContext(_ctx, getState());
		enterRule(_localctx, 222, RULE_predicatePatternLoopExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1743);
			match(KW_WHILE);
			setState(1744);
			match(KW_LET);
			setState(1745);
			pattern();
			setState(1746);
			match(EQ);
			setState(1747);
			expression(0);
			setState(1748);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitIteratorLoopExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IteratorLoopExpressionContext iteratorLoopExpression() throws RecognitionException {
		IteratorLoopExpressionContext _localctx = new IteratorLoopExpressionContext(_ctx, getState());
		enterRule(_localctx, 224, RULE_iteratorLoopExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1750);
			match(KW_FOR);
			setState(1751);
			pattern();
			setState(1752);
			match(KW_IN);
			setState(1753);
			expression(0);
			setState(1754);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitLoopLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LoopLabelContext loopLabel() throws RecognitionException {
		LoopLabelContext _localctx = new LoopLabelContext(_ctx, getState());
		enterRule(_localctx, 226, RULE_loopLabel);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1756);
			match(LIFETIME_OR_LABEL);
			setState(1757);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitIfExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IfExpressionContext ifExpression() throws RecognitionException {
		IfExpressionContext _localctx = new IfExpressionContext(_ctx, getState());
		enterRule(_localctx, 228, RULE_ifExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1759);
			match(KW_IF);
			setState(1760);
			expression(0);
			setState(1761);
			blockExpression();
			setState(1768);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,230,_ctx) ) {
			case 1:
				{
				setState(1762);
				match(KW_ELSE);
				setState(1766);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,229,_ctx) ) {
				case 1:
					{
					setState(1763);
					blockExpression();
					}
					break;
				case 2:
					{
					setState(1764);
					ifExpression();
					}
					break;
				case 3:
					{
					setState(1765);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitIfLetExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IfLetExpressionContext ifLetExpression() throws RecognitionException {
		IfLetExpressionContext _localctx = new IfLetExpressionContext(_ctx, getState());
		enterRule(_localctx, 230, RULE_ifLetExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1770);
			match(KW_IF);
			setState(1771);
			match(KW_LET);
			setState(1772);
			pattern();
			setState(1773);
			match(EQ);
			setState(1774);
			expression(0);
			setState(1775);
			blockExpression();
			setState(1782);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,232,_ctx) ) {
			case 1:
				{
				setState(1776);
				match(KW_ELSE);
				setState(1780);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,231,_ctx) ) {
				case 1:
					{
					setState(1777);
					blockExpression();
					}
					break;
				case 2:
					{
					setState(1778);
					ifExpression();
					}
					break;
				case 3:
					{
					setState(1779);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMatchExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MatchExpressionContext matchExpression() throws RecognitionException {
		MatchExpressionContext _localctx = new MatchExpressionContext(_ctx, getState());
		enterRule(_localctx, 232, RULE_matchExpression);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1784);
			match(KW_MATCH);
			setState(1785);
			expression(0);
			setState(1786);
			match(LCURLYBRACE);
			setState(1790);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,233,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1787);
					innerAttribute();
					}
					} 
				}
				setState(1792);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,233,_ctx);
			}
			setState(1794);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417558172729888L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1451307245037963391L) != 0) {
				{
				setState(1793);
				matchArms();
				}
			}

			setState(1796);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMatchArms(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MatchArmsContext matchArms() throws RecognitionException {
		MatchArmsContext _localctx = new MatchArmsContext(_ctx, getState());
		enterRule(_localctx, 234, RULE_matchArms);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1804);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,235,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1798);
					matchArm();
					setState(1799);
					match(FATARROW);
					setState(1800);
					matchArmExpression();
					}
					} 
				}
				setState(1806);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,235,_ctx);
			}
			setState(1807);
			matchArm();
			setState(1808);
			match(FATARROW);
			setState(1809);
			expression(0);
			setState(1811);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(1810);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMatchArmExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MatchArmExpressionContext matchArmExpression() throws RecognitionException {
		MatchArmExpressionContext _localctx = new MatchArmExpressionContext(_ctx, getState());
		enterRule(_localctx, 236, RULE_matchArmExpression);
		int _la;
		try {
			setState(1820);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,238,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1813);
				expression(0);
				setState(1814);
				match(COMMA);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1816);
				expressionWithBlock();
				setState(1818);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(1817);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMatchArm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MatchArmContext matchArm() throws RecognitionException {
		MatchArmContext _localctx = new MatchArmContext(_ctx, getState());
		enterRule(_localctx, 238, RULE_matchArm);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1825);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(1822);
				outerAttribute();
				}
				}
				setState(1827);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1828);
			pattern();
			setState(1830);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_IF) {
				{
				setState(1829);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMatchArmGuard(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MatchArmGuardContext matchArmGuard() throws RecognitionException {
		MatchArmGuardContext _localctx = new MatchArmGuardContext(_ctx, getState());
		enterRule(_localctx, 240, RULE_matchArmGuard);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1832);
			match(KW_IF);
			setState(1833);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitPattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PatternContext pattern() throws RecognitionException {
		PatternContext _localctx = new PatternContext(_ctx, getState());
		enterRule(_localctx, 242, RULE_pattern);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1836);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==OR) {
				{
				setState(1835);
				match(OR);
				}
			}

			setState(1838);
			patternNoTopAlt();
			setState(1843);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,242,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1839);
					match(OR);
					setState(1840);
					patternNoTopAlt();
					}
					} 
				}
				setState(1845);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitPatternNoTopAlt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PatternNoTopAltContext patternNoTopAlt() throws RecognitionException {
		PatternNoTopAltContext _localctx = new PatternNoTopAltContext(_ctx, getState());
		enterRule(_localctx, 244, RULE_patternNoTopAlt);
		try {
			setState(1848);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,243,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1846);
				patternWithoutRange();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1847);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitPatternWithoutRange(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PatternWithoutRangeContext patternWithoutRange() throws RecognitionException {
		PatternWithoutRangeContext _localctx = new PatternWithoutRangeContext(_ctx, getState());
		enterRule(_localctx, 246, RULE_patternWithoutRange);
		try {
			setState(1862);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,244,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1850);
				literalPattern();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1851);
				identifierPattern();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1852);
				wildcardPattern();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1853);
				restPattern();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1854);
				referencePattern();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1855);
				structPattern();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1856);
				tupleStructPattern();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(1857);
				tuplePattern();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(1858);
				groupedPattern();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(1859);
				slicePattern();
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(1860);
				pathPattern();
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(1861);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitLiteralPattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralPatternContext literalPattern() throws RecognitionException {
		LiteralPatternContext _localctx = new LiteralPatternContext(_ctx, getState());
		enterRule(_localctx, 248, RULE_literalPattern);
		int _la;
		try {
			setState(1880);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,247,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1864);
				match(KW_TRUE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1865);
				match(KW_FALSE);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1866);
				match(CHAR_LITERAL);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1867);
				match(BYTE_LITERAL);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1868);
				match(STRING_LITERAL);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1869);
				match(RAW_STRING_LITERAL);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1870);
				match(BYTE_STRING_LITERAL);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(1871);
				match(RAW_BYTE_STRING_LITERAL);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(1873);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(1872);
					match(MINUS);
					}
				}

				setState(1875);
				match(INTEGER_LITERAL);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(1877);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(1876);
					match(MINUS);
					}
				}

				setState(1879);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitIdentifierPattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierPatternContext identifierPattern() throws RecognitionException {
		IdentifierPatternContext _localctx = new IdentifierPatternContext(_ctx, getState());
		enterRule(_localctx, 250, RULE_identifierPattern);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1883);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_REF) {
				{
				setState(1882);
				match(KW_REF);
				}
			}

			setState(1886);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_MUT) {
				{
				setState(1885);
				match(KW_MUT);
				}
			}

			setState(1888);
			identifier();
			setState(1891);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AT) {
				{
				setState(1889);
				match(AT);
				setState(1890);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitWildcardPattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WildcardPatternContext wildcardPattern() throws RecognitionException {
		WildcardPatternContext _localctx = new WildcardPatternContext(_ctx, getState());
		enterRule(_localctx, 252, RULE_wildcardPattern);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1893);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitRestPattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RestPatternContext restPattern() throws RecognitionException {
		RestPatternContext _localctx = new RestPatternContext(_ctx, getState());
		enterRule(_localctx, 254, RULE_restPattern);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1895);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitInclusiveRangePattern(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitObsoleteRangePattern(this);
			else return visitor.visitChildren(this);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitHalfOpenRangePattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RangePatternContext rangePattern() throws RecognitionException {
		RangePatternContext _localctx = new RangePatternContext(_ctx, getState());
		enterRule(_localctx, 256, RULE_rangePattern);
		try {
			setState(1908);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,251,_ctx) ) {
			case 1:
				_localctx = new InclusiveRangePatternContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1897);
				rangePatternBound();
				setState(1898);
				match(DOTDOTEQ);
				setState(1899);
				rangePatternBound();
				}
				break;
			case 2:
				_localctx = new HalfOpenRangePatternContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1901);
				rangePatternBound();
				setState(1902);
				match(DOTDOT);
				}
				break;
			case 3:
				_localctx = new ObsoleteRangePatternContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1904);
				rangePatternBound();
				setState(1905);
				match(DOTDOTDOT);
				setState(1906);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitRangePatternBound(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RangePatternBoundContext rangePatternBound() throws RecognitionException {
		RangePatternBoundContext _localctx = new RangePatternBoundContext(_ctx, getState());
		enterRule(_localctx, 258, RULE_rangePatternBound);
		int _la;
		try {
			setState(1921);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,254,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1910);
				match(CHAR_LITERAL);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1911);
				match(BYTE_LITERAL);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1913);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(1912);
					match(MINUS);
					}
				}

				setState(1915);
				match(INTEGER_LITERAL);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1917);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(1916);
					match(MINUS);
					}
				}

				setState(1919);
				match(FLOAT_LITERAL);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1920);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitReferencePattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReferencePatternContext referencePattern() throws RecognitionException {
		ReferencePatternContext _localctx = new ReferencePatternContext(_ctx, getState());
		enterRule(_localctx, 260, RULE_referencePattern);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1923);
			_la = _input.LA(1);
			if ( !(_la==AND || _la==ANDAND) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(1925);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,255,_ctx) ) {
			case 1:
				{
				setState(1924);
				match(KW_MUT);
				}
				break;
			}
			setState(1927);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitStructPattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructPatternContext structPattern() throws RecognitionException {
		StructPatternContext _localctx = new StructPatternContext(_ctx, getState());
		enterRule(_localctx, 262, RULE_structPattern);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1929);
			pathInExpression();
			setState(1930);
			match(LCURLYBRACE);
			setState(1932);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 450359962742292480L) != 0 || (((_la - 73)) & ~0x3f) == 0 && ((1L << (_la - 73)) & 141012366262273L) != 0) {
				{
				setState(1931);
				structPatternElements();
				}
			}

			setState(1934);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitStructPatternElements(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructPatternElementsContext structPatternElements() throws RecognitionException {
		StructPatternElementsContext _localctx = new StructPatternElementsContext(_ctx, getState());
		enterRule(_localctx, 264, RULE_structPatternElements);
		int _la;
		try {
			setState(1944);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,259,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1936);
				structPatternFields();
				setState(1941);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(1937);
					match(COMMA);
					setState(1939);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==DOTDOT || _la==POUND) {
						{
						setState(1938);
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
				setState(1943);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitStructPatternFields(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructPatternFieldsContext structPatternFields() throws RecognitionException {
		StructPatternFieldsContext _localctx = new StructPatternFieldsContext(_ctx, getState());
		enterRule(_localctx, 266, RULE_structPatternFields);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1946);
			structPatternField();
			setState(1951);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,260,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1947);
					match(COMMA);
					setState(1948);
					structPatternField();
					}
					} 
				}
				setState(1953);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitStructPatternField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructPatternFieldContext structPatternField() throws RecognitionException {
		StructPatternFieldContext _localctx = new StructPatternFieldContext(_ctx, getState());
		enterRule(_localctx, 268, RULE_structPatternField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1957);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(1954);
				outerAttribute();
				}
				}
				setState(1959);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1975);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,264,_ctx) ) {
			case 1:
				{
				setState(1960);
				tupleIndex();
				setState(1961);
				match(COLON);
				setState(1962);
				pattern();
				}
				break;
			case 2:
				{
				setState(1964);
				identifier();
				setState(1965);
				match(COLON);
				setState(1966);
				pattern();
				}
				break;
			case 3:
				{
				setState(1969);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==KW_REF) {
					{
					setState(1968);
					match(KW_REF);
					}
				}

				setState(1972);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==KW_MUT) {
					{
					setState(1971);
					match(KW_MUT);
					}
				}

				setState(1974);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitStructPatternEtCetera(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructPatternEtCeteraContext structPatternEtCetera() throws RecognitionException {
		StructPatternEtCeteraContext _localctx = new StructPatternEtCeteraContext(_ctx, getState());
		enterRule(_localctx, 270, RULE_structPatternEtCetera);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1980);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(1977);
				outerAttribute();
				}
				}
				setState(1982);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1983);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTupleStructPattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TupleStructPatternContext tupleStructPattern() throws RecognitionException {
		TupleStructPatternContext _localctx = new TupleStructPatternContext(_ctx, getState());
		enterRule(_localctx, 272, RULE_tupleStructPattern);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1985);
			pathInExpression();
			setState(1986);
			match(LPAREN);
			setState(1988);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417558172729888L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1442300045783222399L) != 0) {
				{
				setState(1987);
				tupleStructItems();
				}
			}

			setState(1990);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTupleStructItems(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TupleStructItemsContext tupleStructItems() throws RecognitionException {
		TupleStructItemsContext _localctx = new TupleStructItemsContext(_ctx, getState());
		enterRule(_localctx, 274, RULE_tupleStructItems);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1992);
			pattern();
			setState(1997);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,267,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1993);
					match(COMMA);
					setState(1994);
					pattern();
					}
					} 
				}
				setState(1999);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,267,_ctx);
			}
			setState(2001);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(2000);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTuplePattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TuplePatternContext tuplePattern() throws RecognitionException {
		TuplePatternContext _localctx = new TuplePatternContext(_ctx, getState());
		enterRule(_localctx, 276, RULE_tuplePattern);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2003);
			match(LPAREN);
			setState(2005);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417558172729888L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1442300045783222399L) != 0) {
				{
				setState(2004);
				tuplePatternItems();
				}
			}

			setState(2007);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTuplePatternItems(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TuplePatternItemsContext tuplePatternItems() throws RecognitionException {
		TuplePatternItemsContext _localctx = new TuplePatternItemsContext(_ctx, getState());
		enterRule(_localctx, 278, RULE_tuplePatternItems);
		int _la;
		try {
			int _alt;
			setState(2023);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,272,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2009);
				pattern();
				setState(2010);
				match(COMMA);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2012);
				restPattern();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2013);
				pattern();
				setState(2016); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(2014);
						match(COMMA);
						setState(2015);
						pattern();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(2018); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,270,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(2021);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(2020);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitGroupedPattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupedPatternContext groupedPattern() throws RecognitionException {
		GroupedPatternContext _localctx = new GroupedPatternContext(_ctx, getState());
		enterRule(_localctx, 280, RULE_groupedPattern);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2025);
			match(LPAREN);
			setState(2026);
			pattern();
			setState(2027);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitSlicePattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SlicePatternContext slicePattern() throws RecognitionException {
		SlicePatternContext _localctx = new SlicePatternContext(_ctx, getState());
		enterRule(_localctx, 282, RULE_slicePattern);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2029);
			match(LSQUAREBRACKET);
			setState(2031);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 522417558172729888L) != 0 || (((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 1442300045783222399L) != 0) {
				{
				setState(2030);
				slicePatternItems();
				}
			}

			setState(2033);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitSlicePatternItems(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SlicePatternItemsContext slicePatternItems() throws RecognitionException {
		SlicePatternItemsContext _localctx = new SlicePatternItemsContext(_ctx, getState());
		enterRule(_localctx, 284, RULE_slicePatternItems);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2035);
			pattern();
			setState(2040);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,274,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2036);
					match(COMMA);
					setState(2037);
					pattern();
					}
					} 
				}
				setState(2042);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,274,_ctx);
			}
			setState(2044);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(2043);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitPathPattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PathPatternContext pathPattern() throws RecognitionException {
		PathPatternContext _localctx = new PathPatternContext(_ctx, getState());
		enterRule(_localctx, 286, RULE_pathPattern);
		try {
			setState(2048);
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
				setState(2046);
				pathInExpression();
				}
				break;
			case LT:
				enterOuterAlt(_localctx, 2);
				{
				setState(2047);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitType_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Type_Context type_() throws RecognitionException {
		Type_Context _localctx = new Type_Context(_ctx, getState());
		enterRule(_localctx, 288, RULE_type_);
		try {
			setState(2053);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,277,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2050);
				typeNoBounds();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2051);
				implTraitType();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2052);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTypeNoBounds(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeNoBoundsContext typeNoBounds() throws RecognitionException {
		TypeNoBoundsContext _localctx = new TypeNoBoundsContext(_ctx, getState());
		enterRule(_localctx, 290, RULE_typeNoBounds);
		try {
			setState(2069);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,278,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2055);
				parenthesizedType();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2056);
				implTraitTypeOneBound();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2057);
				traitObjectTypeOneBound();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2058);
				typePath();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2059);
				tupleType();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2060);
				neverType();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(2061);
				rawPointerType();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(2062);
				referenceType();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(2063);
				arrayType();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(2064);
				sliceType();
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(2065);
				inferredType();
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(2066);
				qualifiedPathInType();
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(2067);
				bareFunctionType();
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(2068);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitParenthesizedType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParenthesizedTypeContext parenthesizedType() throws RecognitionException {
		ParenthesizedTypeContext _localctx = new ParenthesizedTypeContext(_ctx, getState());
		enterRule(_localctx, 292, RULE_parenthesizedType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2071);
			match(LPAREN);
			setState(2072);
			type_();
			setState(2073);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitNeverType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NeverTypeContext neverType() throws RecognitionException {
		NeverTypeContext _localctx = new NeverTypeContext(_ctx, getState());
		enterRule(_localctx, 294, RULE_neverType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2075);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTupleType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TupleTypeContext tupleType() throws RecognitionException {
		TupleTypeContext _localctx = new TupleTypeContext(_ctx, getState());
		enterRule(_localctx, 296, RULE_tupleType);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2077);
			match(LPAREN);
			setState(2088);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 567453832540335392L) != 0 || (((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 360915832668553L) != 0) {
				{
				setState(2081); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(2078);
						type_();
						setState(2079);
						match(COMMA);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(2083); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,279,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(2086);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 567453832540335392L) != 0 || (((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 360915832668553L) != 0) {
					{
					setState(2085);
					type_();
					}
				}

				}
			}

			setState(2090);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitArrayType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayTypeContext arrayType() throws RecognitionException {
		ArrayTypeContext _localctx = new ArrayTypeContext(_ctx, getState());
		enterRule(_localctx, 298, RULE_arrayType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2092);
			match(LSQUAREBRACKET);
			setState(2093);
			type_();
			setState(2094);
			match(SEMI);
			setState(2095);
			expression(0);
			setState(2096);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitSliceType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SliceTypeContext sliceType() throws RecognitionException {
		SliceTypeContext _localctx = new SliceTypeContext(_ctx, getState());
		enterRule(_localctx, 300, RULE_sliceType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2098);
			match(LSQUAREBRACKET);
			setState(2099);
			type_();
			setState(2100);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitReferenceType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReferenceTypeContext referenceType() throws RecognitionException {
		ReferenceTypeContext _localctx = new ReferenceTypeContext(_ctx, getState());
		enterRule(_localctx, 302, RULE_referenceType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2102);
			match(AND);
			setState(2104);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la - 53)) & ~0x3f) == 0 && ((1L << (_la - 53)) & 67108869L) != 0) {
				{
				setState(2103);
				lifetime();
				}
			}

			setState(2107);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_MUT) {
				{
				setState(2106);
				match(KW_MUT);
				}
			}

			setState(2109);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitRawPointerType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RawPointerTypeContext rawPointerType() throws RecognitionException {
		RawPointerTypeContext _localctx = new RawPointerTypeContext(_ctx, getState());
		enterRule(_localctx, 304, RULE_rawPointerType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2111);
			match(STAR);
			setState(2112);
			_la = _input.LA(1);
			if ( !(_la==KW_CONST || _la==KW_MUT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(2113);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitBareFunctionType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BareFunctionTypeContext bareFunctionType() throws RecognitionException {
		BareFunctionTypeContext _localctx = new BareFunctionTypeContext(_ctx, getState());
		enterRule(_localctx, 306, RULE_bareFunctionType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2116);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_FOR) {
				{
				setState(2115);
				forLifetimes();
				}
			}

			setState(2118);
			functionTypeQualifiers();
			setState(2119);
			match(KW_FN);
			setState(2120);
			match(LPAREN);
			setState(2122);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 567453832540335392L) != 0 || (((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 363114855924105L) != 0) {
				{
				setState(2121);
				functionParametersMaybeNamedVariadic();
				}
			}

			setState(2124);
			match(RPAREN);
			setState(2126);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,286,_ctx) ) {
			case 1:
				{
				setState(2125);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitFunctionTypeQualifiers(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionTypeQualifiersContext functionTypeQualifiers() throws RecognitionException {
		FunctionTypeQualifiersContext _localctx = new FunctionTypeQualifiersContext(_ctx, getState());
		enterRule(_localctx, 308, RULE_functionTypeQualifiers);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2129);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_UNSAFE) {
				{
				setState(2128);
				match(KW_UNSAFE);
				}
			}

			setState(2135);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_EXTERN) {
				{
				setState(2131);
				match(KW_EXTERN);
				setState(2133);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STRING_LITERAL || _la==RAW_STRING_LITERAL) {
					{
					setState(2132);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitBareFunctionReturnType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BareFunctionReturnTypeContext bareFunctionReturnType() throws RecognitionException {
		BareFunctionReturnTypeContext _localctx = new BareFunctionReturnTypeContext(_ctx, getState());
		enterRule(_localctx, 310, RULE_bareFunctionReturnType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2137);
			match(RARROW);
			setState(2138);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitFunctionParametersMaybeNamedVariadic(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionParametersMaybeNamedVariadicContext functionParametersMaybeNamedVariadic() throws RecognitionException {
		FunctionParametersMaybeNamedVariadicContext _localctx = new FunctionParametersMaybeNamedVariadicContext(_ctx, getState());
		enterRule(_localctx, 312, RULE_functionParametersMaybeNamedVariadic);
		try {
			setState(2142);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,290,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2140);
				maybeNamedFunctionParameters();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2141);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMaybeNamedFunctionParameters(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MaybeNamedFunctionParametersContext maybeNamedFunctionParameters() throws RecognitionException {
		MaybeNamedFunctionParametersContext _localctx = new MaybeNamedFunctionParametersContext(_ctx, getState());
		enterRule(_localctx, 314, RULE_maybeNamedFunctionParameters);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2144);
			maybeNamedParam();
			setState(2149);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,291,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2145);
					match(COMMA);
					setState(2146);
					maybeNamedParam();
					}
					} 
				}
				setState(2151);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,291,_ctx);
			}
			setState(2153);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(2152);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMaybeNamedParam(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MaybeNamedParamContext maybeNamedParam() throws RecognitionException {
		MaybeNamedParamContext _localctx = new MaybeNamedParamContext(_ctx, getState());
		enterRule(_localctx, 316, RULE_maybeNamedParam);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2158);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(2155);
				outerAttribute();
				}
				}
				setState(2160);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2166);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,295,_ctx) ) {
			case 1:
				{
				setState(2163);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case KW_MACRORULES:
				case NON_KEYWORD_IDENTIFIER:
				case RAW_IDENTIFIER:
					{
					setState(2161);
					identifier();
					}
					break;
				case UNDERSCORE:
					{
					setState(2162);
					match(UNDERSCORE);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(2165);
				match(COLON);
				}
				break;
			}
			setState(2168);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMaybeNamedFunctionParametersVariadic(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MaybeNamedFunctionParametersVariadicContext maybeNamedFunctionParametersVariadic() throws RecognitionException {
		MaybeNamedFunctionParametersVariadicContext _localctx = new MaybeNamedFunctionParametersVariadicContext(_ctx, getState());
		enterRule(_localctx, 318, RULE_maybeNamedFunctionParametersVariadic);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2175);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,296,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2170);
					maybeNamedParam();
					setState(2171);
					match(COMMA);
					}
					} 
				}
				setState(2177);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,296,_ctx);
			}
			setState(2178);
			maybeNamedParam();
			setState(2179);
			match(COMMA);
			setState(2183);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==POUND) {
				{
				{
				setState(2180);
				outerAttribute();
				}
				}
				setState(2185);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(2186);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTraitObjectType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TraitObjectTypeContext traitObjectType() throws RecognitionException {
		TraitObjectTypeContext _localctx = new TraitObjectTypeContext(_ctx, getState());
		enterRule(_localctx, 320, RULE_traitObjectType);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTraitObjectTypeOneBound(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TraitObjectTypeOneBoundContext traitObjectTypeOneBound() throws RecognitionException {
		TraitObjectTypeOneBoundContext _localctx = new TraitObjectTypeOneBoundContext(_ctx, getState());
		enterRule(_localctx, 322, RULE_traitObjectTypeOneBound);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2194);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_DYN) {
				{
				setState(2193);
				match(KW_DYN);
				}
			}

			setState(2196);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitImplTraitType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ImplTraitTypeContext implTraitType() throws RecognitionException {
		ImplTraitTypeContext _localctx = new ImplTraitTypeContext(_ctx, getState());
		enterRule(_localctx, 324, RULE_implTraitType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2198);
			match(KW_IMPL);
			setState(2199);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitImplTraitTypeOneBound(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ImplTraitTypeOneBoundContext implTraitTypeOneBound() throws RecognitionException {
		ImplTraitTypeOneBoundContext _localctx = new ImplTraitTypeOneBoundContext(_ctx, getState());
		enterRule(_localctx, 326, RULE_implTraitTypeOneBound);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2201);
			match(KW_IMPL);
			setState(2202);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitInferredType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InferredTypeContext inferredType() throws RecognitionException {
		InferredTypeContext _localctx = new InferredTypeContext(_ctx, getState());
		enterRule(_localctx, 328, RULE_inferredType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2204);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTypeParamBounds(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeParamBoundsContext typeParamBounds() throws RecognitionException {
		TypeParamBoundsContext _localctx = new TypeParamBoundsContext(_ctx, getState());
		enterRule(_localctx, 330, RULE_typeParamBounds);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2206);
			typeParamBound();
			setState(2211);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,300,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2207);
					match(PLUS);
					setState(2208);
					typeParamBound();
					}
					} 
				}
				setState(2213);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,300,_ctx);
			}
			setState(2215);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,301,_ctx) ) {
			case 1:
				{
				setState(2214);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTypeParamBound(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeParamBoundContext typeParamBound() throws RecognitionException {
		TypeParamBoundContext _localctx = new TypeParamBoundContext(_ctx, getState());
		enterRule(_localctx, 332, RULE_typeParamBound);
		try {
			setState(2219);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_STATICLIFETIME:
			case KW_UNDERLINELIFETIME:
			case LIFETIME_OR_LABEL:
				enterOuterAlt(_localctx, 1);
				{
				setState(2217);
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
				setState(2218);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTraitBound(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TraitBoundContext traitBound() throws RecognitionException {
		TraitBoundContext _localctx = new TraitBoundContext(_ctx, getState());
		enterRule(_localctx, 334, RULE_traitBound);
		int _la;
		try {
			setState(2238);
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
				setState(2222);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==QUESTION) {
					{
					setState(2221);
					match(QUESTION);
					}
				}

				setState(2225);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==KW_FOR) {
					{
					setState(2224);
					forLifetimes();
					}
				}

				setState(2227);
				typePath();
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(2228);
				match(LPAREN);
				setState(2230);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==QUESTION) {
					{
					setState(2229);
					match(QUESTION);
					}
				}

				setState(2233);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==KW_FOR) {
					{
					setState(2232);
					forLifetimes();
					}
				}

				setState(2235);
				typePath();
				setState(2236);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitLifetimeBounds(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LifetimeBoundsContext lifetimeBounds() throws RecognitionException {
		LifetimeBoundsContext _localctx = new LifetimeBoundsContext(_ctx, getState());
		enterRule(_localctx, 336, RULE_lifetimeBounds);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2245);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,308,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2240);
					lifetime();
					setState(2241);
					match(PLUS);
					}
					} 
				}
				setState(2247);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,308,_ctx);
			}
			setState(2249);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la - 53)) & ~0x3f) == 0 && ((1L << (_la - 53)) & 67108869L) != 0) {
				{
				setState(2248);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitLifetime(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LifetimeContext lifetime() throws RecognitionException {
		LifetimeContext _localctx = new LifetimeContext(_ctx, getState());
		enterRule(_localctx, 338, RULE_lifetime);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2251);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitSimplePath(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SimplePathContext simplePath() throws RecognitionException {
		SimplePathContext _localctx = new SimplePathContext(_ctx, getState());
		enterRule(_localctx, 340, RULE_simplePath);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2254);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PATHSEP) {
				{
				setState(2253);
				match(PATHSEP);
				}
			}

			setState(2256);
			simplePathSegment();
			setState(2261);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,311,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2257);
					match(PATHSEP);
					setState(2258);
					simplePathSegment();
					}
					} 
				}
				setState(2263);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitSimplePathSegment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SimplePathSegmentContext simplePathSegment() throws RecognitionException {
		SimplePathSegmentContext _localctx = new SimplePathSegmentContext(_ctx, getState());
		enterRule(_localctx, 342, RULE_simplePathSegment);
		try {
			setState(2269);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_MACRORULES:
			case NON_KEYWORD_IDENTIFIER:
			case RAW_IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(2264);
				identifier();
				}
				break;
			case KW_SUPER:
				enterOuterAlt(_localctx, 2);
				{
				setState(2265);
				match(KW_SUPER);
				}
				break;
			case KW_SELFVALUE:
				enterOuterAlt(_localctx, 3);
				{
				setState(2266);
				match(KW_SELFVALUE);
				}
				break;
			case KW_CRATE:
				enterOuterAlt(_localctx, 4);
				{
				setState(2267);
				match(KW_CRATE);
				}
				break;
			case KW_DOLLARCRATE:
				enterOuterAlt(_localctx, 5);
				{
				setState(2268);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitPathInExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PathInExpressionContext pathInExpression() throws RecognitionException {
		PathInExpressionContext _localctx = new PathInExpressionContext(_ctx, getState());
		enterRule(_localctx, 344, RULE_pathInExpression);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2272);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PATHSEP) {
				{
				setState(2271);
				match(PATHSEP);
				}
			}

			setState(2274);
			pathExprSegment();
			setState(2279);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,314,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2275);
					match(PATHSEP);
					setState(2276);
					pathExprSegment();
					}
					} 
				}
				setState(2281);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitPathExprSegment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PathExprSegmentContext pathExprSegment() throws RecognitionException {
		PathExprSegmentContext _localctx = new PathExprSegmentContext(_ctx, getState());
		enterRule(_localctx, 346, RULE_pathExprSegment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2282);
			pathIdentSegment();
			setState(2285);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,315,_ctx) ) {
			case 1:
				{
				setState(2283);
				match(PATHSEP);
				setState(2284);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitPathIdentSegment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PathIdentSegmentContext pathIdentSegment() throws RecognitionException {
		PathIdentSegmentContext _localctx = new PathIdentSegmentContext(_ctx, getState());
		enterRule(_localctx, 348, RULE_pathIdentSegment);
		try {
			setState(2293);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case KW_MACRORULES:
			case NON_KEYWORD_IDENTIFIER:
			case RAW_IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(2287);
				identifier();
				}
				break;
			case KW_SUPER:
				enterOuterAlt(_localctx, 2);
				{
				setState(2288);
				match(KW_SUPER);
				}
				break;
			case KW_SELFVALUE:
				enterOuterAlt(_localctx, 3);
				{
				setState(2289);
				match(KW_SELFVALUE);
				}
				break;
			case KW_SELFTYPE:
				enterOuterAlt(_localctx, 4);
				{
				setState(2290);
				match(KW_SELFTYPE);
				}
				break;
			case KW_CRATE:
				enterOuterAlt(_localctx, 5);
				{
				setState(2291);
				match(KW_CRATE);
				}
				break;
			case KW_DOLLARCRATE:
				enterOuterAlt(_localctx, 6);
				{
				setState(2292);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitGenericArgs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericArgsContext genericArgs() throws RecognitionException {
		GenericArgsContext _localctx = new GenericArgsContext(_ctx, getState());
		enterRule(_localctx, 350, RULE_genericArgs);
		int _la;
		try {
			int _alt;
			setState(2338);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,324,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2295);
				match(LT);
				setState(2296);
				match(GT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2297);
				match(LT);
				setState(2298);
				genericArgsLifetimes();
				setState(2301);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,317,_ctx) ) {
				case 1:
					{
					setState(2299);
					match(COMMA);
					setState(2300);
					genericArgsTypes();
					}
					break;
				}
				setState(2305);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,318,_ctx) ) {
				case 1:
					{
					setState(2303);
					match(COMMA);
					setState(2304);
					genericArgsBindings();
					}
					break;
				}
				setState(2308);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(2307);
					match(COMMA);
					}
				}

				setState(2310);
				match(GT);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2312);
				match(LT);
				setState(2313);
				genericArgsTypes();
				setState(2316);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,320,_ctx) ) {
				case 1:
					{
					setState(2314);
					match(COMMA);
					setState(2315);
					genericArgsBindings();
					}
					break;
				}
				setState(2319);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(2318);
					match(COMMA);
					}
				}

				setState(2321);
				match(GT);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2323);
				match(LT);
				setState(2329);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,322,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(2324);
						genericArg();
						setState(2325);
						match(COMMA);
						}
						} 
					}
					setState(2331);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,322,_ctx);
				}
				setState(2332);
				genericArg();
				setState(2334);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(2333);
					match(COMMA);
					}
				}

				setState(2336);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitGenericArg(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericArgContext genericArg() throws RecognitionException {
		GenericArgContext _localctx = new GenericArgContext(_ctx, getState());
		enterRule(_localctx, 352, RULE_genericArg);
		try {
			setState(2344);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,325,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2340);
				lifetime();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2341);
				type_();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2342);
				genericArgsConst();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2343);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitGenericArgsConst(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericArgsConstContext genericArgsConst() throws RecognitionException {
		GenericArgsConstContext _localctx = new GenericArgsConstContext(_ctx, getState());
		enterRule(_localctx, 354, RULE_genericArgsConst);
		int _la;
		try {
			setState(2352);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LCURLYBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(2346);
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
				setState(2348);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(2347);
					match(MINUS);
					}
				}

				setState(2350);
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
				setState(2351);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitGenericArgsLifetimes(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericArgsLifetimesContext genericArgsLifetimes() throws RecognitionException {
		GenericArgsLifetimesContext _localctx = new GenericArgsLifetimesContext(_ctx, getState());
		enterRule(_localctx, 356, RULE_genericArgsLifetimes);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2354);
			lifetime();
			setState(2359);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,328,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2355);
					match(COMMA);
					setState(2356);
					lifetime();
					}
					} 
				}
				setState(2361);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitGenericArgsTypes(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericArgsTypesContext genericArgsTypes() throws RecognitionException {
		GenericArgsTypesContext _localctx = new GenericArgsTypesContext(_ctx, getState());
		enterRule(_localctx, 358, RULE_genericArgsTypes);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2362);
			type_();
			setState(2367);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,329,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2363);
					match(COMMA);
					setState(2364);
					type_();
					}
					} 
				}
				setState(2369);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitGenericArgsBindings(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericArgsBindingsContext genericArgsBindings() throws RecognitionException {
		GenericArgsBindingsContext _localctx = new GenericArgsBindingsContext(_ctx, getState());
		enterRule(_localctx, 360, RULE_genericArgsBindings);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2370);
			genericArgsBinding();
			setState(2375);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,330,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2371);
					match(COMMA);
					setState(2372);
					genericArgsBinding();
					}
					} 
				}
				setState(2377);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitGenericArgsBinding(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericArgsBindingContext genericArgsBinding() throws RecognitionException {
		GenericArgsBindingContext _localctx = new GenericArgsBindingContext(_ctx, getState());
		enterRule(_localctx, 362, RULE_genericArgsBinding);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2378);
			identifier();
			setState(2379);
			match(EQ);
			setState(2380);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitQualifiedPathInExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedPathInExpressionContext qualifiedPathInExpression() throws RecognitionException {
		QualifiedPathInExpressionContext _localctx = new QualifiedPathInExpressionContext(_ctx, getState());
		enterRule(_localctx, 364, RULE_qualifiedPathInExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2382);
			qualifiedPathType();
			setState(2385); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(2383);
					match(PATHSEP);
					setState(2384);
					pathExprSegment();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(2387); 
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitQualifiedPathType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedPathTypeContext qualifiedPathType() throws RecognitionException {
		QualifiedPathTypeContext _localctx = new QualifiedPathTypeContext(_ctx, getState());
		enterRule(_localctx, 366, RULE_qualifiedPathType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2389);
			match(LT);
			setState(2390);
			type_();
			setState(2393);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==KW_AS) {
				{
				setState(2391);
				match(KW_AS);
				setState(2392);
				typePath();
				}
			}

			setState(2395);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitQualifiedPathInType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedPathInTypeContext qualifiedPathInType() throws RecognitionException {
		QualifiedPathInTypeContext _localctx = new QualifiedPathInTypeContext(_ctx, getState());
		enterRule(_localctx, 368, RULE_qualifiedPathInType);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2397);
			qualifiedPathType();
			setState(2400); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(2398);
					match(PATHSEP);
					setState(2399);
					typePathSegment();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(2402); 
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTypePath(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypePathContext typePath() throws RecognitionException {
		TypePathContext _localctx = new TypePathContext(_ctx, getState());
		enterRule(_localctx, 370, RULE_typePath);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2405);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PATHSEP) {
				{
				setState(2404);
				match(PATHSEP);
				}
			}

			setState(2407);
			typePathSegment();
			setState(2412);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,335,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2408);
					match(PATHSEP);
					setState(2409);
					typePathSegment();
					}
					} 
				}
				setState(2414);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTypePathSegment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypePathSegmentContext typePathSegment() throws RecognitionException {
		TypePathSegmentContext _localctx = new TypePathSegmentContext(_ctx, getState());
		enterRule(_localctx, 372, RULE_typePathSegment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2415);
			pathIdentSegment();
			setState(2417);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,336,_ctx) ) {
			case 1:
				{
				setState(2416);
				match(PATHSEP);
				}
				break;
			}
			setState(2421);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,337,_ctx) ) {
			case 1:
				{
				setState(2419);
				genericArgs();
				}
				break;
			case 2:
				{
				setState(2420);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTypePathFn(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypePathFnContext typePathFn() throws RecognitionException {
		TypePathFnContext _localctx = new TypePathFnContext(_ctx, getState());
		enterRule(_localctx, 374, RULE_typePathFn);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2423);
			match(LPAREN);
			setState(2425);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 567453832540335392L) != 0 || (((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 360915832668553L) != 0) {
				{
				setState(2424);
				typePathInputs();
				}
			}

			setState(2427);
			match(RPAREN);
			setState(2430);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,339,_ctx) ) {
			case 1:
				{
				setState(2428);
				match(RARROW);
				setState(2429);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitTypePathInputs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypePathInputsContext typePathInputs() throws RecognitionException {
		TypePathInputsContext _localctx = new TypePathInputsContext(_ctx, getState());
		enterRule(_localctx, 376, RULE_typePathInputs);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2432);
			type_();
			setState(2437);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,340,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2433);
					match(COMMA);
					setState(2434);
					type_();
					}
					} 
				}
				setState(2439);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,340,_ctx);
			}
			setState(2441);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(2440);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitVisibility(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VisibilityContext visibility() throws RecognitionException {
		VisibilityContext _localctx = new VisibilityContext(_ctx, getState());
		enterRule(_localctx, 378, RULE_visibility);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2443);
			match(KW_PUB);
			setState(2453);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,343,_ctx) ) {
			case 1:
				{
				setState(2444);
				match(LPAREN);
				setState(2450);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case KW_CRATE:
					{
					setState(2445);
					match(KW_CRATE);
					}
					break;
				case KW_SELFVALUE:
					{
					setState(2446);
					match(KW_SELFVALUE);
					}
					break;
				case KW_SUPER:
					{
					setState(2447);
					match(KW_SUPER);
					}
					break;
				case KW_IN:
					{
					setState(2448);
					match(KW_IN);
					setState(2449);
					simplePath();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(2452);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 380, RULE_identifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2455);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitKeyword(this);
			else return visitor.visitChildren(this);
		}
	}

	public final KeywordContext keyword() throws RecognitionException {
		KeywordContext _localctx = new KeywordContext(_ctx, getState());
		enterRule(_localctx, 382, RULE_keyword);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2457);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMacroIdentifierLikeToken(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MacroIdentifierLikeTokenContext macroIdentifierLikeToken() throws RecognitionException {
		MacroIdentifierLikeTokenContext _localctx = new MacroIdentifierLikeTokenContext(_ctx, getState());
		enterRule(_localctx, 384, RULE_macroIdentifierLikeToken);
		try {
			setState(2465);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,344,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2459);
				keyword();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2460);
				identifier();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2461);
				match(KW_MACRORULES);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2462);
				match(KW_UNDERLINELIFETIME);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2463);
				match(KW_DOLLARCRATE);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2464);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMacroLiteralToken(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MacroLiteralTokenContext macroLiteralToken() throws RecognitionException {
		MacroLiteralTokenContext _localctx = new MacroLiteralTokenContext(_ctx, getState());
		enterRule(_localctx, 386, RULE_macroLiteralToken);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2467);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitMacroPunctuationToken(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MacroPunctuationTokenContext macroPunctuationToken() throws RecognitionException {
		MacroPunctuationTokenContext _localctx = new MacroPunctuationTokenContext(_ctx, getState());
		enterRule(_localctx, 388, RULE_macroPunctuationToken);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2469);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitShl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ShlContext shl() throws RecognitionException {
		ShlContext _localctx = new ShlContext(_ctx, getState());
		enterRule(_localctx, 390, RULE_shl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2471);
			match(LT);
			setState(2472);
			if (!(this.next('<'))) throw new FailedPredicateException(this, "this.next('<')");
			setState(2473);
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof RustParserVisitor ) return ((RustParserVisitor<? extends T>)visitor).visitShr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ShrContext shr() throws RecognitionException {
		ShrContext _localctx = new ShrContext(_ctx, getState());
		enterRule(_localctx, 392, RULE_shr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2475);
			match(GT);
			setState(2476);
			if (!(this.next('>'))) throw new FailedPredicateException(this, "this.next('>')");
			setState(2477);
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
		case 195:
			return shl_sempred((ShlContext)_localctx, predIndex);
		case 196:
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
		"\u0004\u0001\u0083\u09b0\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
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
		"\u00c2\u0002\u00c3\u0007\u00c3\u0002\u00c4\u0007\u00c4\u0001\u0000\u0005"+
		"\u0000\u018c\b\u0000\n\u0000\f\u0000\u018f\t\u0000\u0001\u0000\u0005\u0000"+
		"\u0192\b\u0000\n\u0000\f\u0000\u0195\t\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0005"+
		"\u0002\u019f\b\u0002\n\u0002\f\u0002\u01a2\t\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0005\u0002\u01a7\b\u0002\n\u0002\f\u0002\u01aa\t\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0005\u0002\u01af\b\u0002\n\u0002\f\u0002"+
		"\u01b2\t\u0002\u0001\u0002\u0003\u0002\u01b5\b\u0002\u0001\u0003\u0004"+
		"\u0003\u01b8\b\u0003\u000b\u0003\f\u0003\u01b9\u0001\u0003\u0003\u0003"+
		"\u01bd\b\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0003\u0004\u01c4\b\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0005\u0005\u01ca\b\u0005\n\u0005\f\u0005\u01cd\t\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0005"+
		"\u0005\u01d6\b\u0005\n\u0005\f\u0005\u01d9\t\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0005\u0005"+
		"\u01e2\b\u0005\n\u0005\f\u0005\u01e5\t\u0005\u0001\u0005\u0001\u0005\u0003"+
		"\u0005\u01e9\b\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u01fe\b\u0007\u0001\b\u0001"+
		"\b\u0001\b\u0005\b\u0203\b\b\n\b\f\b\u0206\t\b\u0001\b\u0003\b\u0209\b"+
		"\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0005\n\u0211\b\n\n"+
		"\n\f\n\u0214\t\n\u0001\n\u0001\n\u0001\n\u0005\n\u0219\b\n\n\n\f\n\u021c"+
		"\t\n\u0001\n\u0001\n\u0001\n\u0005\n\u0221\b\n\n\n\f\n\u0224\t\n\u0001"+
		"\n\u0003\n\u0227\b\n\u0001\u000b\u0004\u000b\u022a\b\u000b\u000b\u000b"+
		"\f\u000b\u022b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b"+
		"\u0232\b\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0004\u000b\u0239\b\u000b\u000b\u000b\f\u000b\u023a\u0001\u000b\u0001"+
		"\u000b\u0003\u000b\u023f\b\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u0243"+
		"\b\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u0249\b\f\u0001\r\u0001"+
		"\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u0251\b"+
		"\u000e\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0011\u0005"+
		"\u0011\u0258\b\u0011\n\u0011\f\u0011\u025b\t\u0011\u0001\u0011\u0001\u0011"+
		"\u0003\u0011\u025f\b\u0011\u0001\u0012\u0003\u0012\u0262\b\u0012\u0001"+
		"\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0003\u0012\u0271\b\u0012\u0001\u0013\u0001\u0013\u0003\u0013\u0275"+
		"\b\u0013\u0001\u0014\u0003\u0014\u0278\b\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0005\u0014\u027f\b\u0014\n\u0014"+
		"\f\u0014\u0282\t\u0014\u0001\u0014\u0005\u0014\u0285\b\u0014\n\u0014\f"+
		"\u0014\u0288\t\u0014\u0001\u0014\u0003\u0014\u028b\b\u0014\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0003\u0015\u0291\b\u0015\u0001\u0015"+
		"\u0001\u0015\u0001\u0016\u0001\u0016\u0003\u0016\u0297\b\u0016\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0003\u0017\u029c\b\u0017\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0001\u0019\u0003\u0019\u02a3\b\u0019\u0001\u0019"+
		"\u0003\u0019\u02a6\b\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0001\u0019\u0005\u0019\u02ad\b\u0019\n\u0019\f\u0019\u02b0\t\u0019\u0001"+
		"\u0019\u0003\u0019\u02b3\b\u0019\u0003\u0019\u02b5\b\u0019\u0001\u0019"+
		"\u0003\u0019\u02b8\b\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0003\u0019\u02be\b\u0019\u0003\u0019\u02c0\b\u0019\u0003\u0019\u02c2"+
		"\b\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0003\u001a\u02c8"+
		"\b\u001a\u0001\u001a\u0001\u001a\u0003\u001a\u02cc\b\u001a\u0001\u001a"+
		"\u0001\u001a\u0003\u001a\u02d0\b\u001a\u0001\u001a\u0003\u001a\u02d3\b"+
		"\u001a\u0001\u001a\u0001\u001a\u0003\u001a\u02d7\b\u001a\u0001\u001b\u0003"+
		"\u001b\u02da\b\u001b\u0001\u001b\u0003\u001b\u02dd\b\u001b\u0001\u001b"+
		"\u0003\u001b\u02e0\b\u001b\u0001\u001b\u0001\u001b\u0003\u001b\u02e4\b"+
		"\u001b\u0003\u001b\u02e6\b\u001b\u0001\u001c\u0001\u001c\u0001\u001d\u0001"+
		"\u001d\u0003\u001d\u02ec\b\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0003"+
		"\u001d\u02f1\b\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0005\u001d\u02f6"+
		"\b\u001d\n\u001d\f\u001d\u02f9\t\u001d\u0001\u001d\u0003\u001d\u02fc\b"+
		"\u001d\u0003\u001d\u02fe\b\u001d\u0001\u001e\u0005\u001e\u0301\b\u001e"+
		"\n\u001e\f\u001e\u0304\t\u001e\u0001\u001e\u0001\u001e\u0003\u001e\u0308"+
		"\b\u001e\u0001\u001f\u0001\u001f\u0003\u001f\u030c\b\u001f\u0003\u001f"+
		"\u030e\b\u001f\u0001\u001f\u0003\u001f\u0311\b\u001f\u0001\u001f\u0001"+
		"\u001f\u0001 \u0003 \u0316\b \u0001 \u0001 \u0001 \u0001 \u0001!\u0005"+
		"!\u031d\b!\n!\f!\u0320\t!\u0001!\u0001!\u0001!\u0003!\u0325\b!\u0001\""+
		"\u0001\"\u0001\"\u0001\"\u0003\"\u032b\b\"\u0001#\u0001#\u0001#\u0001"+
		"$\u0001$\u0001$\u0003$\u0333\b$\u0001$\u0003$\u0336\b$\u0001$\u0001$\u0003"+
		"$\u033a\b$\u0001$\u0001$\u0001%\u0001%\u0003%\u0340\b%\u0001&\u0001&\u0001"+
		"&\u0003&\u0345\b&\u0001&\u0003&\u0348\b&\u0001&\u0001&\u0003&\u034c\b"+
		"&\u0001&\u0001&\u0003&\u0350\b&\u0001\'\u0001\'\u0001\'\u0003\'\u0355"+
		"\b\'\u0001\'\u0001\'\u0003\'\u0359\b\'\u0001\'\u0001\'\u0003\'\u035d\b"+
		"\'\u0001\'\u0001\'\u0001(\u0001(\u0001(\u0005(\u0364\b(\n(\f(\u0367\t"+
		"(\u0001(\u0003(\u036a\b(\u0001)\u0005)\u036d\b)\n)\f)\u0370\t)\u0001)"+
		"\u0003)\u0373\b)\u0001)\u0001)\u0001)\u0001)\u0001*\u0001*\u0001*\u0005"+
		"*\u037c\b*\n*\f*\u037f\t*\u0001*\u0003*\u0382\b*\u0001+\u0005+\u0385\b"+
		"+\n+\f+\u0388\t+\u0001+\u0003+\u038b\b+\u0001+\u0001+\u0001,\u0001,\u0001"+
		",\u0003,\u0392\b,\u0001,\u0003,\u0395\b,\u0001,\u0001,\u0003,\u0399\b"+
		",\u0001,\u0001,\u0001-\u0001-\u0001-\u0005-\u03a0\b-\n-\f-\u03a3\t-\u0001"+
		"-\u0003-\u03a6\b-\u0001.\u0005.\u03a9\b.\n.\f.\u03ac\t.\u0001.\u0003."+
		"\u03af\b.\u0001.\u0001.\u0001.\u0001.\u0003.\u03b5\b.\u0001/\u0001/\u0003"+
		"/\u03b9\b/\u0001/\u0001/\u00010\u00010\u00030\u03bf\b0\u00010\u00010\u0001"+
		"1\u00011\u00011\u00012\u00012\u00012\u00032\u03c9\b2\u00012\u00032\u03cc"+
		"\b2\u00012\u00012\u00012\u00012\u00013\u00013\u00013\u00033\u03d5\b3\u0001"+
		"3\u00013\u00013\u00013\u00033\u03db\b3\u00013\u00013\u00014\u00014\u0003"+
		"4\u03e1\b4\u00014\u00014\u00014\u00014\u00014\u00034\u03e8\b4\u00014\u0001"+
		"4\u00015\u00035\u03ed\b5\u00015\u00015\u00015\u00035\u03f2\b5\u00015\u0001"+
		"5\u00035\u03f6\b5\u00035\u03f8\b5\u00015\u00035\u03fb\b5\u00015\u0001"+
		"5\u00055\u03ff\b5\n5\f5\u0402\t5\u00015\u00055\u0405\b5\n5\f5\u0408\t"+
		"5\u00015\u00015\u00016\u00016\u00036\u040e\b6\u00017\u00017\u00037\u0412"+
		"\b7\u00017\u00017\u00037\u0416\b7\u00017\u00017\u00057\u041a\b7\n7\f7"+
		"\u041d\t7\u00017\u00057\u0420\b7\n7\f7\u0423\t7\u00017\u00017\u00018\u0003"+
		"8\u0428\b8\u00018\u00018\u00038\u042c\b8\u00018\u00038\u042f\b8\u0001"+
		"8\u00018\u00018\u00018\u00038\u0435\b8\u00018\u00018\u00058\u0439\b8\n"+
		"8\f8\u043c\t8\u00018\u00058\u043f\b8\n8\f8\u0442\t8\u00018\u00018\u0001"+
		"9\u00039\u0447\b9\u00019\u00019\u00039\u044b\b9\u00019\u00019\u00059\u044f"+
		"\b9\n9\f9\u0452\t9\u00019\u00059\u0455\b9\n9\f9\u0458\t9\u00019\u0001"+
		"9\u0001:\u0005:\u045d\b:\n:\f:\u0460\t:\u0001:\u0001:\u0003:\u0464\b:"+
		"\u0001:\u0001:\u0003:\u0468\b:\u0003:\u046a\b:\u0001;\u0001;\u0001;\u0001"+
		";\u0005;\u0470\b;\n;\f;\u0473\t;\u0001;\u0001;\u0003;\u0477\b;\u0003;"+
		"\u0479\b;\u0001;\u0001;\u0001<\u0005<\u047e\b<\n<\f<\u0481\t<\u0001<\u0001"+
		"<\u0001<\u0003<\u0486\b<\u0001=\u0003=\u0489\b=\u0001=\u0001=\u0001=\u0003"+
		"=\u048e\b=\u0001>\u0003>\u0491\b>\u0001>\u0001>\u0001>\u0003>\u0496\b"+
		">\u0003>\u0498\b>\u0001>\u0001>\u0003>\u049c\b>\u0001?\u0001?\u0001?\u0001"+
		"?\u0001?\u0001@\u0001@\u0001@\u0001@\u0005@\u04a7\b@\n@\f@\u04aa\t@\u0001"+
		"@\u0003@\u04ad\b@\u0001A\u0001A\u0003A\u04b1\bA\u0001B\u0001B\u0001B\u0001"+
		"B\u0001C\u0003C\u04b8\bC\u0001C\u0001C\u0001C\u0003C\u04bd\bC\u0001D\u0001"+
		"D\u0001D\u0001E\u0005E\u04c3\bE\nE\fE\u04c6\tE\u0001E\u0001E\u0003E\u04ca"+
		"\bE\u0001E\u0001E\u0001E\u0003E\u04cf\bE\u0003E\u04d1\bE\u0001F\u0001"+
		"F\u0001F\u0001F\u0001F\u0001F\u0001G\u0001G\u0001G\u0001G\u0001G\u0001"+
		"H\u0001H\u0003H\u04e0\bH\u0001I\u0001I\u0001I\u0003I\u04e5\bI\u0001J\u0001"+
		"J\u0001J\u0001J\u0001J\u0003J\u04ec\bJ\u0001K\u0005K\u04ef\bK\nK\fK\u04f2"+
		"\tK\u0001K\u0001K\u0001K\u0001K\u0003K\u04f8\bK\u0001K\u0001K\u0003K\u04fc"+
		"\bK\u0001K\u0001K\u0001L\u0001L\u0001L\u0001L\u0001L\u0003L\u0505\bL\u0003"+
		"L\u0507\bL\u0001M\u0001M\u0004M\u050b\bM\u000bM\fM\u050c\u0001M\u0001"+
		"M\u0001M\u0001M\u0001M\u0001M\u0003M\u0515\bM\u0001M\u0001M\u0001M\u0001"+
		"M\u0001M\u0001M\u0001M\u0003M\u051e\bM\u0001M\u0001M\u0001M\u0001M\u0003"+
		"M\u0524\bM\u0001M\u0003M\u0527\bM\u0001M\u0001M\u0003M\u052b\bM\u0001"+
		"M\u0003M\u052e\bM\u0001M\u0001M\u0003M\u0532\bM\u0001M\u0001M\u0005M\u0536"+
		"\bM\nM\fM\u0539\tM\u0001M\u0001M\u0001M\u0001M\u0001M\u0005M\u0540\bM"+
		"\nM\fM\u0543\tM\u0001M\u0003M\u0546\bM\u0001M\u0001M\u0001M\u0005M\u054b"+
		"\bM\nM\fM\u054e\tM\u0001M\u0003M\u0551\bM\u0001M\u0001M\u0001M\u0001M"+
		"\u0001M\u0001M\u0003M\u0559\bM\u0001M\u0001M\u0001M\u0001M\u0001M\u0001"+
		"M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001"+
		"M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001"+
		"M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001"+
		"M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0003M\u0587"+
		"\bM\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001"+
		"M\u0001M\u0001M\u0001M\u0001M\u0003M\u0597\bM\u0001M\u0001M\u0001M\u0001"+
		"M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001"+
		"M\u0003M\u05a7\bM\u0005M\u05a9\bM\nM\fM\u05ac\tM\u0001N\u0001N\u0001N"+
		"\u0001N\u0003N\u05b2\bN\u0001O\u0001O\u0001P\u0001P\u0001Q\u0004Q\u05b9"+
		"\bQ\u000bQ\fQ\u05ba\u0001Q\u0001Q\u0001Q\u0001Q\u0001Q\u0001Q\u0001Q\u0001"+
		"Q\u0001Q\u0003Q\u05c6\bQ\u0001R\u0001R\u0001S\u0001S\u0003S\u05cc\bS\u0001"+
		"T\u0001T\u0005T\u05d0\bT\nT\fT\u05d3\tT\u0001T\u0003T\u05d6\bT\u0001T"+
		"\u0001T\u0001U\u0004U\u05db\bU\u000bU\fU\u05dc\u0001U\u0003U\u05e0\bU"+
		"\u0001U\u0003U\u05e3\bU\u0001V\u0001V\u0003V\u05e7\bV\u0001V\u0001V\u0001"+
		"W\u0001W\u0001W\u0001X\u0001X\u0001X\u0005X\u05f1\bX\nX\fX\u05f4\tX\u0001"+
		"X\u0003X\u05f7\bX\u0001X\u0001X\u0001X\u0001X\u0003X\u05fd\bX\u0001Y\u0001"+
		"Y\u0001Y\u0004Y\u0602\bY\u000bY\fY\u0603\u0001Y\u0003Y\u0607\bY\u0001"+
		"Z\u0001Z\u0001[\u0001[\u0001[\u0003[\u060e\b[\u0001\\\u0001\\\u0001\\"+
		"\u0005\\\u0613\b\\\n\\\f\\\u0616\t\\\u0001\\\u0001\\\u0003\\\u061a\b\\"+
		"\u0001\\\u0001\\\u0001]\u0001]\u0001]\u0005]\u0621\b]\n]\f]\u0624\t]\u0001"+
		"]\u0001]\u0001]\u0003]\u0629\b]\u0003]\u062b\b]\u0001^\u0005^\u062e\b"+
		"^\n^\f^\u0631\t^\u0001^\u0001^\u0001^\u0003^\u0636\b^\u0001^\u0001^\u0001"+
		"^\u0003^\u063b\b^\u0001_\u0001_\u0001_\u0001`\u0001`\u0001`\u0005`\u0643"+
		"\b`\n`\f`\u0646\t`\u0001`\u0001`\u0001`\u0005`\u064b\b`\n`\f`\u064e\t"+
		"`\u0001`\u0003`\u0651\b`\u0003`\u0653\b`\u0001`\u0001`\u0001a\u0001a\u0001"+
		"b\u0001b\u0001b\u0003b\u065c\bb\u0001c\u0001c\u0001c\u0003c\u0661\bc\u0001"+
		"c\u0001c\u0001d\u0001d\u0001d\u0005d\u0668\bd\nd\fd\u066b\td\u0001d\u0003"+
		"d\u066e\bd\u0001e\u0001e\u0001e\u0003e\u0673\be\u0001e\u0001e\u0001e\u0003"+
		"e\u0678\be\u0001f\u0001f\u0001f\u0001f\u0001f\u0005f\u067f\bf\nf\ff\u0682"+
		"\tf\u0001f\u0003f\u0685\bf\u0003f\u0687\bf\u0001f\u0001f\u0001g\u0001"+
		"g\u0001h\u0001h\u0001h\u0005h\u0690\bh\nh\fh\u0693\th\u0001h\u0003h\u0696"+
		"\bh\u0001i\u0003i\u0699\bi\u0001i\u0001i\u0001i\u0003i\u069e\bi\u0001"+
		"i\u0003i\u06a1\bi\u0001i\u0001i\u0001i\u0001i\u0001i\u0003i\u06a8\bi\u0001"+
		"j\u0001j\u0001j\u0005j\u06ad\bj\nj\fj\u06b0\tj\u0001j\u0003j\u06b3\bj"+
		"\u0001k\u0005k\u06b6\bk\nk\fk\u06b9\tk\u0001k\u0001k\u0001k\u0003k\u06be"+
		"\bk\u0001l\u0003l\u06c1\bl\u0001l\u0001l\u0001l\u0001l\u0003l\u06c7\b"+
		"l\u0001m\u0001m\u0001m\u0001n\u0001n\u0001n\u0001n\u0001o\u0001o\u0001"+
		"o\u0001o\u0001o\u0001o\u0001o\u0001p\u0001p\u0001p\u0001p\u0001p\u0001"+
		"p\u0001q\u0001q\u0001q\u0001r\u0001r\u0001r\u0001r\u0001r\u0001r\u0001"+
		"r\u0003r\u06e7\br\u0003r\u06e9\br\u0001s\u0001s\u0001s\u0001s\u0001s\u0001"+
		"s\u0001s\u0001s\u0001s\u0001s\u0003s\u06f5\bs\u0003s\u06f7\bs\u0001t\u0001"+
		"t\u0001t\u0001t\u0005t\u06fd\bt\nt\ft\u0700\tt\u0001t\u0003t\u0703\bt"+
		"\u0001t\u0001t\u0001u\u0001u\u0001u\u0001u\u0005u\u070b\bu\nu\fu\u070e"+
		"\tu\u0001u\u0001u\u0001u\u0001u\u0003u\u0714\bu\u0001v\u0001v\u0001v\u0001"+
		"v\u0001v\u0003v\u071b\bv\u0003v\u071d\bv\u0001w\u0005w\u0720\bw\nw\fw"+
		"\u0723\tw\u0001w\u0001w\u0003w\u0727\bw\u0001x\u0001x\u0001x\u0001y\u0003"+
		"y\u072d\by\u0001y\u0001y\u0001y\u0005y\u0732\by\ny\fy\u0735\ty\u0001z"+
		"\u0001z\u0003z\u0739\bz\u0001{\u0001{\u0001{\u0001{\u0001{\u0001{\u0001"+
		"{\u0001{\u0001{\u0001{\u0001{\u0001{\u0003{\u0747\b{\u0001|\u0001|\u0001"+
		"|\u0001|\u0001|\u0001|\u0001|\u0001|\u0001|\u0003|\u0752\b|\u0001|\u0001"+
		"|\u0003|\u0756\b|\u0001|\u0003|\u0759\b|\u0001}\u0003}\u075c\b}\u0001"+
		"}\u0003}\u075f\b}\u0001}\u0001}\u0001}\u0003}\u0764\b}\u0001~\u0001~\u0001"+
		"\u007f\u0001\u007f\u0001\u0080\u0001\u0080\u0001\u0080\u0001\u0080\u0001"+
		"\u0080\u0001\u0080\u0001\u0080\u0001\u0080\u0001\u0080\u0001\u0080\u0001"+
		"\u0080\u0003\u0080\u0775\b\u0080\u0001\u0081\u0001\u0081\u0001\u0081\u0003"+
		"\u0081\u077a\b\u0081\u0001\u0081\u0001\u0081\u0003\u0081\u077e\b\u0081"+
		"\u0001\u0081\u0001\u0081\u0003\u0081\u0782\b\u0081\u0001\u0082\u0001\u0082"+
		"\u0003\u0082\u0786\b\u0082\u0001\u0082\u0001\u0082\u0001\u0083\u0001\u0083"+
		"\u0001\u0083\u0003\u0083\u078d\b\u0083\u0001\u0083\u0001\u0083\u0001\u0084"+
		"\u0001\u0084\u0001\u0084\u0003\u0084\u0794\b\u0084\u0003\u0084\u0796\b"+
		"\u0084\u0001\u0084\u0003\u0084\u0799\b\u0084\u0001\u0085\u0001\u0085\u0001"+
		"\u0085\u0005\u0085\u079e\b\u0085\n\u0085\f\u0085\u07a1\t\u0085\u0001\u0086"+
		"\u0005\u0086\u07a4\b\u0086\n\u0086\f\u0086\u07a7\t\u0086\u0001\u0086\u0001"+
		"\u0086\u0001\u0086\u0001\u0086\u0001\u0086\u0001\u0086\u0001\u0086\u0001"+
		"\u0086\u0001\u0086\u0003\u0086\u07b2\b\u0086\u0001\u0086\u0003\u0086\u07b5"+
		"\b\u0086\u0001\u0086\u0003\u0086\u07b8\b\u0086\u0001\u0087\u0005\u0087"+
		"\u07bb\b\u0087\n\u0087\f\u0087\u07be\t\u0087\u0001\u0087\u0001\u0087\u0001"+
		"\u0088\u0001\u0088\u0001\u0088\u0003\u0088\u07c5\b\u0088\u0001\u0088\u0001"+
		"\u0088\u0001\u0089\u0001\u0089\u0001\u0089\u0005\u0089\u07cc\b\u0089\n"+
		"\u0089\f\u0089\u07cf\t\u0089\u0001\u0089\u0003\u0089\u07d2\b\u0089\u0001"+
		"\u008a\u0001\u008a\u0003\u008a\u07d6\b\u008a\u0001\u008a\u0001\u008a\u0001"+
		"\u008b\u0001\u008b\u0001\u008b\u0001\u008b\u0001\u008b\u0001\u008b\u0001"+
		"\u008b\u0004\u008b\u07e1\b\u008b\u000b\u008b\f\u008b\u07e2\u0001\u008b"+
		"\u0003\u008b\u07e6\b\u008b\u0003\u008b\u07e8\b\u008b\u0001\u008c\u0001"+
		"\u008c\u0001\u008c\u0001\u008c\u0001\u008d\u0001\u008d\u0003\u008d\u07f0"+
		"\b\u008d\u0001\u008d\u0001\u008d\u0001\u008e\u0001\u008e\u0001\u008e\u0005"+
		"\u008e\u07f7\b\u008e\n\u008e\f\u008e\u07fa\t\u008e\u0001\u008e\u0003\u008e"+
		"\u07fd\b\u008e\u0001\u008f\u0001\u008f\u0003\u008f\u0801\b\u008f\u0001"+
		"\u0090\u0001\u0090\u0001\u0090\u0003\u0090\u0806\b\u0090\u0001\u0091\u0001"+
		"\u0091\u0001\u0091\u0001\u0091\u0001\u0091\u0001\u0091\u0001\u0091\u0001"+
		"\u0091\u0001\u0091\u0001\u0091\u0001\u0091\u0001\u0091\u0001\u0091\u0001"+
		"\u0091\u0003\u0091\u0816\b\u0091\u0001\u0092\u0001\u0092\u0001\u0092\u0001"+
		"\u0092\u0001\u0093\u0001\u0093\u0001\u0094\u0001\u0094\u0001\u0094\u0001"+
		"\u0094\u0004\u0094\u0822\b\u0094\u000b\u0094\f\u0094\u0823\u0001\u0094"+
		"\u0003\u0094\u0827\b\u0094\u0003\u0094\u0829\b\u0094\u0001\u0094\u0001"+
		"\u0094\u0001\u0095\u0001\u0095\u0001\u0095\u0001\u0095\u0001\u0095\u0001"+
		"\u0095\u0001\u0096\u0001\u0096\u0001\u0096\u0001\u0096\u0001\u0097\u0001"+
		"\u0097\u0003\u0097\u0839\b\u0097\u0001\u0097\u0003\u0097\u083c\b\u0097"+
		"\u0001\u0097\u0001\u0097\u0001\u0098\u0001\u0098\u0001\u0098\u0001\u0098"+
		"\u0001\u0099\u0003\u0099\u0845\b\u0099\u0001\u0099\u0001\u0099\u0001\u0099"+
		"\u0001\u0099\u0003\u0099\u084b\b\u0099\u0001\u0099\u0001\u0099\u0003\u0099"+
		"\u084f\b\u0099\u0001\u009a\u0003\u009a\u0852\b\u009a\u0001\u009a\u0001"+
		"\u009a\u0003\u009a\u0856\b\u009a\u0003\u009a\u0858\b\u009a\u0001\u009b"+
		"\u0001\u009b\u0001\u009b\u0001\u009c\u0001\u009c\u0003\u009c\u085f\b\u009c"+
		"\u0001\u009d\u0001\u009d\u0001\u009d\u0005\u009d\u0864\b\u009d\n\u009d"+
		"\f\u009d\u0867\t\u009d\u0001\u009d\u0003\u009d\u086a\b\u009d\u0001\u009e"+
		"\u0005\u009e\u086d\b\u009e\n\u009e\f\u009e\u0870\t\u009e\u0001\u009e\u0001"+
		"\u009e\u0003\u009e\u0874\b\u009e\u0001\u009e\u0003\u009e\u0877\b\u009e"+
		"\u0001\u009e\u0001\u009e\u0001\u009f\u0001\u009f\u0001\u009f\u0005\u009f"+
		"\u087e\b\u009f\n\u009f\f\u009f\u0881\t\u009f\u0001\u009f\u0001\u009f\u0001"+
		"\u009f\u0005\u009f\u0886\b\u009f\n\u009f\f\u009f\u0889\t\u009f\u0001\u009f"+
		"\u0001\u009f\u0001\u00a0\u0003\u00a0\u088e\b\u00a0\u0001\u00a0\u0001\u00a0"+
		"\u0001\u00a1\u0003\u00a1\u0893\b\u00a1\u0001\u00a1\u0001\u00a1\u0001\u00a2"+
		"\u0001\u00a2\u0001\u00a2\u0001\u00a3\u0001\u00a3\u0001\u00a3\u0001\u00a4"+
		"\u0001\u00a4\u0001\u00a5\u0001\u00a5\u0001\u00a5\u0005\u00a5\u08a2\b\u00a5"+
		"\n\u00a5\f\u00a5\u08a5\t\u00a5\u0001\u00a5\u0003\u00a5\u08a8\b\u00a5\u0001"+
		"\u00a6\u0001\u00a6\u0003\u00a6\u08ac\b\u00a6\u0001\u00a7\u0003\u00a7\u08af"+
		"\b\u00a7\u0001\u00a7\u0003\u00a7\u08b2\b\u00a7\u0001\u00a7\u0001\u00a7"+
		"\u0001\u00a7\u0003\u00a7\u08b7\b\u00a7\u0001\u00a7\u0003\u00a7\u08ba\b"+
		"\u00a7\u0001\u00a7\u0001\u00a7\u0001\u00a7\u0003\u00a7\u08bf\b\u00a7\u0001"+
		"\u00a8\u0001\u00a8\u0001\u00a8\u0005\u00a8\u08c4\b\u00a8\n\u00a8\f\u00a8"+
		"\u08c7\t\u00a8\u0001\u00a8\u0003\u00a8\u08ca\b\u00a8\u0001\u00a9\u0001"+
		"\u00a9\u0001\u00aa\u0003\u00aa\u08cf\b\u00aa\u0001\u00aa\u0001\u00aa\u0001"+
		"\u00aa\u0005\u00aa\u08d4\b\u00aa\n\u00aa\f\u00aa\u08d7\t\u00aa\u0001\u00ab"+
		"\u0001\u00ab\u0001\u00ab\u0001\u00ab\u0001\u00ab\u0003\u00ab\u08de\b\u00ab"+
		"\u0001\u00ac\u0003\u00ac\u08e1\b\u00ac\u0001\u00ac\u0001\u00ac\u0001\u00ac"+
		"\u0005\u00ac\u08e6\b\u00ac\n\u00ac\f\u00ac\u08e9\t\u00ac\u0001\u00ad\u0001"+
		"\u00ad\u0001\u00ad\u0003\u00ad\u08ee\b\u00ad\u0001\u00ae\u0001\u00ae\u0001"+
		"\u00ae\u0001\u00ae\u0001\u00ae\u0001\u00ae\u0003\u00ae\u08f6\b\u00ae\u0001"+
		"\u00af\u0001\u00af\u0001\u00af\u0001\u00af\u0001\u00af\u0001\u00af\u0003"+
		"\u00af\u08fe\b\u00af\u0001\u00af\u0001\u00af\u0003\u00af\u0902\b\u00af"+
		"\u0001\u00af\u0003\u00af\u0905\b\u00af\u0001\u00af\u0001\u00af\u0001\u00af"+
		"\u0001\u00af\u0001\u00af\u0001\u00af\u0003\u00af\u090d\b\u00af\u0001\u00af"+
		"\u0003\u00af\u0910\b\u00af\u0001\u00af\u0001\u00af\u0001\u00af\u0001\u00af"+
		"\u0001\u00af\u0001\u00af\u0005\u00af\u0918\b\u00af\n\u00af\f\u00af\u091b"+
		"\t\u00af\u0001\u00af\u0001\u00af\u0003\u00af\u091f\b\u00af\u0001\u00af"+
		"\u0001\u00af\u0003\u00af\u0923\b\u00af\u0001\u00b0\u0001\u00b0\u0001\u00b0"+
		"\u0001\u00b0\u0003\u00b0\u0929\b\u00b0\u0001\u00b1\u0001\u00b1\u0003\u00b1"+
		"\u092d\b\u00b1\u0001\u00b1\u0001\u00b1\u0003\u00b1\u0931\b\u00b1\u0001"+
		"\u00b2\u0001\u00b2\u0001\u00b2\u0005\u00b2\u0936\b\u00b2\n\u00b2\f\u00b2"+
		"\u0939\t\u00b2\u0001\u00b3\u0001\u00b3\u0001\u00b3\u0005\u00b3\u093e\b"+
		"\u00b3\n\u00b3\f\u00b3\u0941\t\u00b3\u0001\u00b4\u0001\u00b4\u0001\u00b4"+
		"\u0005\u00b4\u0946\b\u00b4\n\u00b4\f\u00b4\u0949\t\u00b4\u0001\u00b5\u0001"+
		"\u00b5\u0001\u00b5\u0001\u00b5\u0001\u00b6\u0001\u00b6\u0001\u00b6\u0004"+
		"\u00b6\u0952\b\u00b6\u000b\u00b6\f\u00b6\u0953\u0001\u00b7\u0001\u00b7"+
		"\u0001\u00b7\u0001\u00b7\u0003\u00b7\u095a\b\u00b7\u0001\u00b7\u0001\u00b7"+
		"\u0001\u00b8\u0001\u00b8\u0001\u00b8\u0004\u00b8\u0961\b\u00b8\u000b\u00b8"+
		"\f\u00b8\u0962\u0001\u00b9\u0003\u00b9\u0966\b\u00b9\u0001\u00b9\u0001"+
		"\u00b9\u0001\u00b9\u0005\u00b9\u096b\b\u00b9\n\u00b9\f\u00b9\u096e\t\u00b9"+
		"\u0001\u00ba\u0001\u00ba\u0003\u00ba\u0972\b\u00ba\u0001\u00ba\u0001\u00ba"+
		"\u0003\u00ba\u0976\b\u00ba\u0001\u00bb\u0001\u00bb\u0003\u00bb\u097a\b"+
		"\u00bb\u0001\u00bb\u0001\u00bb\u0001\u00bb\u0003\u00bb\u097f\b\u00bb\u0001"+
		"\u00bc\u0001\u00bc\u0001\u00bc\u0005\u00bc\u0984\b\u00bc\n\u00bc\f\u00bc"+
		"\u0987\t\u00bc\u0001\u00bc\u0003\u00bc\u098a\b\u00bc\u0001\u00bd\u0001"+
		"\u00bd\u0001\u00bd\u0001\u00bd\u0001\u00bd\u0001\u00bd\u0001\u00bd\u0003"+
		"\u00bd\u0993\b\u00bd\u0001\u00bd\u0003\u00bd\u0996\b\u00bd\u0001\u00be"+
		"\u0001\u00be\u0001\u00bf\u0001\u00bf\u0001\u00c0\u0001\u00c0\u0001\u00c0"+
		"\u0001\u00c0\u0001\u00c0\u0001\u00c0\u0003\u00c0\u09a2\b\u00c0\u0001\u00c1"+
		"\u0001\u00c1\u0001\u00c2\u0001\u00c2\u0001\u00c3\u0001\u00c3\u0001\u00c3"+
		"\u0001\u00c3\u0001\u00c4\u0001\u00c4\u0001\u00c4\u0001\u00c4\u0001\u00c4"+
		"\u0000\u0001\u009a\u00c5\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012"+
		"\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPRTVXZ\\"+
		"^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e\u0090"+
		"\u0092\u0094\u0096\u0098\u009a\u009c\u009e\u00a0\u00a2\u00a4\u00a6\u00a8"+
		"\u00aa\u00ac\u00ae\u00b0\u00b2\u00b4\u00b6\u00b8\u00ba\u00bc\u00be\u00c0"+
		"\u00c2\u00c4\u00c6\u00c8\u00ca\u00cc\u00ce\u00d0\u00d2\u00d4\u00d6\u00d8"+
		"\u00da\u00dc\u00de\u00e0\u00e2\u00e4\u00e6\u00e8\u00ea\u00ec\u00ee\u00f0"+
		"\u00f2\u00f4\u00f6\u00f8\u00fa\u00fc\u00fe\u0100\u0102\u0104\u0106\u0108"+
		"\u010a\u010c\u010e\u0110\u0112\u0114\u0116\u0118\u011a\u011c\u011e\u0120"+
		"\u0122\u0124\u0126\u0128\u012a\u012c\u012e\u0130\u0132\u0134\u0136\u0138"+
		"\u013a\u013c\u013e\u0140\u0142\u0144\u0146\u0148\u014a\u014c\u014e\u0150"+
		"\u0152\u0154\u0156\u0158\u015a\u015c\u015e\u0160\u0162\u0164\u0166\u0168"+
		"\u016a\u016c\u016e\u0170\u0172\u0174\u0176\u0178\u017a\u017c\u017e\u0180"+
		"\u0182\u0184\u0186\u0188\u0000\u000e\u0003\u0000PPRRzz\u0001\u0000DE\u0002"+
		"\u0000WWYY\u0002\u0000QQVV\u0001\u0000RT\u0001\u0000PQ\u0001\u0000fk\u0001"+
		"\u0000[d\u0004\u0000\t\t\u001e\u001eCINN\u0002\u0000\u0003\u0003\u0014"+
		"\u0014\u0003\u00005577OO\u0002\u0000669:\u0001\u0000\u00015\u0002\u0000"+
		"QQSx\u0acc\u0000\u018d\u0001\u0000\u0000\u0000\u0002\u0198\u0001\u0000"+
		"\u0000\u0000\u0004\u01b4\u0001\u0000\u0000\u0000\u0006\u01bc\u0001\u0000"+
		"\u0000\u0000\b\u01c3\u0001\u0000\u0000\u0000\n\u01e8\u0001\u0000\u0000"+
		"\u0000\f\u01ea\u0001\u0000\u0000\u0000\u000e\u01fd\u0001\u0000\u0000\u0000"+
		"\u0010\u01ff\u0001\u0000\u0000\u0000\u0012\u020a\u0001\u0000\u0000\u0000"+
		"\u0014\u0226\u0001\u0000\u0000\u0000\u0016\u0242\u0001\u0000\u0000\u0000"+
		"\u0018\u0248\u0001\u0000\u0000\u0000\u001a\u024a\u0001\u0000\u0000\u0000"+
		"\u001c\u0250\u0001\u0000\u0000\u0000\u001e\u0252\u0001\u0000\u0000\u0000"+
		" \u0254\u0001\u0000\u0000\u0000\"\u0259\u0001\u0000\u0000\u0000$\u0261"+
		"\u0001\u0000\u0000\u0000&\u0274\u0001\u0000\u0000\u0000(\u0277\u0001\u0000"+
		"\u0000\u0000*\u028c\u0001\u0000\u0000\u0000,\u0296\u0001\u0000\u0000\u0000"+
		".\u0298\u0001\u0000\u0000\u00000\u029d\u0001\u0000\u0000\u00002\u02c1"+
		"\u0001\u0000\u0000\u00004\u02c3\u0001\u0000\u0000\u00006\u02d9\u0001\u0000"+
		"\u0000\u00008\u02e7\u0001\u0000\u0000\u0000:\u02fd\u0001\u0000\u0000\u0000"+
		"<\u0302\u0001\u0000\u0000\u0000>\u030d\u0001\u0000\u0000\u0000@\u0315"+
		"\u0001\u0000\u0000\u0000B\u031e\u0001\u0000\u0000\u0000D\u0326\u0001\u0000"+
		"\u0000\u0000F\u032c\u0001\u0000\u0000\u0000H\u032f\u0001\u0000\u0000\u0000"+
		"J\u033f\u0001\u0000\u0000\u0000L\u0341\u0001\u0000\u0000\u0000N\u0351"+
		"\u0001\u0000\u0000\u0000P\u0360\u0001\u0000\u0000\u0000R\u036e\u0001\u0000"+
		"\u0000\u0000T\u0378\u0001\u0000\u0000\u0000V\u0386\u0001\u0000\u0000\u0000"+
		"X\u038e\u0001\u0000\u0000\u0000Z\u039c\u0001\u0000\u0000\u0000\\\u03aa"+
		"\u0001\u0000\u0000\u0000^\u03b6\u0001\u0000\u0000\u0000`\u03bc\u0001\u0000"+
		"\u0000\u0000b\u03c2\u0001\u0000\u0000\u0000d\u03c5\u0001\u0000\u0000\u0000"+
		"f\u03d1\u0001\u0000\u0000\u0000h\u03de\u0001\u0000\u0000\u0000j\u03ec"+
		"\u0001\u0000\u0000\u0000l\u040d\u0001\u0000\u0000\u0000n\u040f\u0001\u0000"+
		"\u0000\u0000p\u0427\u0001\u0000\u0000\u0000r\u0446\u0001\u0000\u0000\u0000"+
		"t\u045e\u0001\u0000\u0000\u0000v\u046b\u0001\u0000\u0000\u0000x\u047f"+
		"\u0001\u0000\u0000\u0000z\u0488\u0001\u0000\u0000\u0000|\u0490\u0001\u0000"+
		"\u0000\u0000~\u049d\u0001\u0000\u0000\u0000\u0080\u04a2\u0001\u0000\u0000"+
		"\u0000\u0082\u04b0\u0001\u0000\u0000\u0000\u0084\u04b2\u0001\u0000\u0000"+
		"\u0000\u0086\u04b7\u0001\u0000\u0000\u0000\u0088\u04be\u0001\u0000\u0000"+
		"\u0000\u008a\u04c4\u0001\u0000\u0000\u0000\u008c\u04d2\u0001\u0000\u0000"+
		"\u0000\u008e\u04d8\u0001\u0000\u0000\u0000\u0090\u04dd\u0001\u0000\u0000"+
		"\u0000\u0092\u04e4\u0001\u0000\u0000\u0000\u0094\u04eb\u0001\u0000\u0000"+
		"\u0000\u0096\u04f0\u0001\u0000\u0000\u0000\u0098\u0506\u0001\u0000\u0000"+
		"\u0000\u009a\u0558\u0001\u0000\u0000\u0000\u009c\u05b1\u0001\u0000\u0000"+
		"\u0000\u009e\u05b3\u0001\u0000\u0000\u0000\u00a0\u05b5\u0001\u0000\u0000"+
		"\u0000\u00a2\u05c5\u0001\u0000\u0000\u0000\u00a4\u05c7\u0001\u0000\u0000"+
		"\u0000\u00a6\u05cb\u0001\u0000\u0000\u0000\u00a8\u05cd\u0001\u0000\u0000"+
		"\u0000\u00aa\u05e2\u0001\u0000\u0000\u0000\u00ac\u05e4\u0001\u0000\u0000"+
		"\u0000\u00ae\u05ea\u0001\u0000\u0000\u0000\u00b0\u05fc\u0001\u0000\u0000"+
		"\u0000\u00b2\u0601\u0001\u0000\u0000\u0000\u00b4\u0608\u0001\u0000\u0000"+
		"\u0000\u00b6\u060d\u0001\u0000\u0000\u0000\u00b8\u060f\u0001\u0000\u0000"+
		"\u0000\u00ba\u061d\u0001\u0000\u0000\u0000\u00bc\u062f\u0001\u0000\u0000"+
		"\u0000\u00be\u063c\u0001\u0000\u0000\u0000\u00c0\u063f\u0001\u0000\u0000"+
		"\u0000\u00c2\u0656\u0001\u0000\u0000\u0000\u00c4\u065b\u0001\u0000\u0000"+
		"\u0000\u00c6\u065d\u0001\u0000\u0000\u0000\u00c8\u0664\u0001\u0000\u0000"+
		"\u0000\u00ca\u0677\u0001\u0000\u0000\u0000\u00cc\u0679\u0001\u0000\u0000"+
		"\u0000\u00ce\u068a\u0001\u0000\u0000\u0000\u00d0\u068c\u0001\u0000\u0000"+
		"\u0000\u00d2\u0698\u0001\u0000\u0000\u0000\u00d4\u06a9\u0001\u0000\u0000"+
		"\u0000\u00d6\u06b7\u0001\u0000\u0000\u0000\u00d8\u06c0\u0001\u0000\u0000"+
		"\u0000\u00da\u06c8\u0001\u0000\u0000\u0000\u00dc\u06cb\u0001\u0000\u0000"+
		"\u0000\u00de\u06cf\u0001\u0000\u0000\u0000\u00e0\u06d6\u0001\u0000\u0000"+
		"\u0000\u00e2\u06dc\u0001\u0000\u0000\u0000\u00e4\u06df\u0001\u0000\u0000"+
		"\u0000\u00e6\u06ea\u0001\u0000\u0000\u0000\u00e8\u06f8\u0001\u0000\u0000"+
		"\u0000\u00ea\u070c\u0001\u0000\u0000\u0000\u00ec\u071c\u0001\u0000\u0000"+
		"\u0000\u00ee\u0721\u0001\u0000\u0000\u0000\u00f0\u0728\u0001\u0000\u0000"+
		"\u0000\u00f2\u072c\u0001\u0000\u0000\u0000\u00f4\u0738\u0001\u0000\u0000"+
		"\u0000\u00f6\u0746\u0001\u0000\u0000\u0000\u00f8\u0758\u0001\u0000\u0000"+
		"\u0000\u00fa\u075b\u0001\u0000\u0000\u0000\u00fc\u0765\u0001\u0000\u0000"+
		"\u0000\u00fe\u0767\u0001\u0000\u0000\u0000\u0100\u0774\u0001\u0000\u0000"+
		"\u0000\u0102\u0781\u0001\u0000\u0000\u0000\u0104\u0783\u0001\u0000\u0000"+
		"\u0000\u0106\u0789\u0001\u0000\u0000\u0000\u0108\u0798\u0001\u0000\u0000"+
		"\u0000\u010a\u079a\u0001\u0000\u0000\u0000\u010c\u07a5\u0001\u0000\u0000"+
		"\u0000\u010e\u07bc\u0001\u0000\u0000\u0000\u0110\u07c1\u0001\u0000\u0000"+
		"\u0000\u0112\u07c8\u0001\u0000\u0000\u0000\u0114\u07d3\u0001\u0000\u0000"+
		"\u0000\u0116\u07e7\u0001\u0000\u0000\u0000\u0118\u07e9\u0001\u0000\u0000"+
		"\u0000\u011a\u07ed\u0001\u0000\u0000\u0000\u011c\u07f3\u0001\u0000\u0000"+
		"\u0000\u011e\u0800\u0001\u0000\u0000\u0000\u0120\u0805\u0001\u0000\u0000"+
		"\u0000\u0122\u0815\u0001\u0000\u0000\u0000\u0124\u0817\u0001\u0000\u0000"+
		"\u0000\u0126\u081b\u0001\u0000\u0000\u0000\u0128\u081d\u0001\u0000\u0000"+
		"\u0000\u012a\u082c\u0001\u0000\u0000\u0000\u012c\u0832\u0001\u0000\u0000"+
		"\u0000\u012e\u0836\u0001\u0000\u0000\u0000\u0130\u083f\u0001\u0000\u0000"+
		"\u0000\u0132\u0844\u0001\u0000\u0000\u0000\u0134\u0851\u0001\u0000\u0000"+
		"\u0000\u0136\u0859\u0001\u0000\u0000\u0000\u0138\u085e\u0001\u0000\u0000"+
		"\u0000\u013a\u0860\u0001\u0000\u0000\u0000\u013c\u086e\u0001\u0000\u0000"+
		"\u0000\u013e\u087f\u0001\u0000\u0000\u0000\u0140\u088d\u0001\u0000\u0000"+
		"\u0000\u0142\u0892\u0001\u0000\u0000\u0000\u0144\u0896\u0001\u0000\u0000"+
		"\u0000\u0146\u0899\u0001\u0000\u0000\u0000\u0148\u089c\u0001\u0000\u0000"+
		"\u0000\u014a\u089e\u0001\u0000\u0000\u0000\u014c\u08ab\u0001\u0000\u0000"+
		"\u0000\u014e\u08be\u0001\u0000\u0000\u0000\u0150\u08c5\u0001\u0000\u0000"+
		"\u0000\u0152\u08cb\u0001\u0000\u0000\u0000\u0154\u08ce\u0001\u0000\u0000"+
		"\u0000\u0156\u08dd\u0001\u0000\u0000\u0000\u0158\u08e0\u0001\u0000\u0000"+
		"\u0000\u015a\u08ea\u0001\u0000\u0000\u0000\u015c\u08f5\u0001\u0000\u0000"+
		"\u0000\u015e\u0922\u0001\u0000\u0000\u0000\u0160\u0928\u0001\u0000\u0000"+
		"\u0000\u0162\u0930\u0001\u0000\u0000\u0000\u0164\u0932\u0001\u0000\u0000"+
		"\u0000\u0166\u093a\u0001\u0000\u0000\u0000\u0168\u0942\u0001\u0000\u0000"+
		"\u0000\u016a\u094a\u0001\u0000\u0000\u0000\u016c\u094e\u0001\u0000\u0000"+
		"\u0000\u016e\u0955\u0001\u0000\u0000\u0000\u0170\u095d\u0001\u0000\u0000"+
		"\u0000\u0172\u0965\u0001\u0000\u0000\u0000\u0174\u096f\u0001\u0000\u0000"+
		"\u0000\u0176\u0977\u0001\u0000\u0000\u0000\u0178\u0980\u0001\u0000\u0000"+
		"\u0000\u017a\u098b\u0001\u0000\u0000\u0000\u017c\u0997\u0001\u0000\u0000"+
		"\u0000\u017e\u0999\u0001\u0000\u0000\u0000\u0180\u09a1\u0001\u0000\u0000"+
		"\u0000\u0182\u09a3\u0001\u0000\u0000\u0000\u0184\u09a5\u0001\u0000\u0000"+
		"\u0000\u0186\u09a7\u0001\u0000\u0000\u0000\u0188\u09ab\u0001\u0000\u0000"+
		"\u0000\u018a\u018c\u0003\u008cF\u0000\u018b\u018a\u0001\u0000\u0000\u0000"+
		"\u018c\u018f\u0001\u0000\u0000\u0000\u018d\u018b\u0001\u0000\u0000\u0000"+
		"\u018d\u018e\u0001\u0000\u0000\u0000\u018e\u0193\u0001\u0000\u0000\u0000"+
		"\u018f\u018d\u0001\u0000\u0000\u0000\u0190\u0192\u0003\"\u0011\u0000\u0191"+
		"\u0190\u0001\u0000\u0000\u0000\u0192\u0195\u0001\u0000\u0000\u0000\u0193"+
		"\u0191\u0001\u0000\u0000\u0000\u0193\u0194\u0001\u0000\u0000\u0000\u0194"+
		"\u0196\u0001\u0000\u0000\u0000\u0195\u0193\u0001\u0000\u0000\u0000\u0196"+
		"\u0197\u0005\u0000\u0000\u0001\u0197\u0001\u0001\u0000\u0000\u0000\u0198"+
		"\u0199\u0003\u0154\u00aa\u0000\u0199\u019a\u0005V\u0000\u0000\u019a\u019b"+
		"\u0003\u0004\u0002\u0000\u019b\u0003\u0001\u0000\u0000\u0000\u019c\u01a0"+
		"\u0005\u007f\u0000\u0000\u019d\u019f\u0003\u0006\u0003\u0000\u019e\u019d"+
		"\u0001\u0000\u0000\u0000\u019f\u01a2\u0001\u0000\u0000\u0000\u01a0\u019e"+
		"\u0001\u0000\u0000\u0000\u01a0\u01a1\u0001\u0000\u0000\u0000\u01a1\u01a3"+
		"\u0001\u0000\u0000\u0000\u01a2\u01a0\u0001\u0000\u0000\u0000\u01a3\u01b5"+
		"\u0005\u0080\u0000\u0000\u01a4\u01a8\u0005}\u0000\u0000\u01a5\u01a7\u0003"+
		"\u0006\u0003\u0000\u01a6\u01a5\u0001\u0000\u0000\u0000\u01a7\u01aa\u0001"+
		"\u0000\u0000\u0000\u01a8\u01a6\u0001\u0000\u0000\u0000\u01a8\u01a9\u0001"+
		"\u0000\u0000\u0000\u01a9\u01ab\u0001\u0000\u0000\u0000\u01aa\u01a8\u0001"+
		"\u0000\u0000\u0000\u01ab\u01b5\u0005~\u0000\u0000\u01ac\u01b0\u0005{\u0000"+
		"\u0000\u01ad\u01af\u0003\u0006\u0003\u0000\u01ae\u01ad\u0001\u0000\u0000"+
		"\u0000\u01af\u01b2\u0001\u0000\u0000\u0000\u01b0\u01ae\u0001\u0000\u0000"+
		"\u0000\u01b0\u01b1\u0001\u0000\u0000\u0000\u01b1\u01b3\u0001\u0000\u0000"+
		"\u0000\u01b2\u01b0\u0001\u0000\u0000\u0000\u01b3\u01b5\u0005|\u0000\u0000"+
		"\u01b4\u019c\u0001\u0000\u0000\u0000\u01b4\u01a4\u0001\u0000\u0000\u0000"+
		"\u01b4\u01ac\u0001\u0000\u0000\u0000\u01b5\u0005\u0001\u0000\u0000\u0000"+
		"\u01b6\u01b8\u0003\b\u0004\u0000\u01b7\u01b6\u0001\u0000\u0000\u0000\u01b8"+
		"\u01b9\u0001\u0000\u0000\u0000\u01b9\u01b7\u0001\u0000\u0000\u0000\u01b9"+
		"\u01ba\u0001\u0000\u0000\u0000\u01ba\u01bd\u0001\u0000\u0000\u0000\u01bb"+
		"\u01bd\u0003\u0004\u0002\u0000\u01bc\u01b7\u0001\u0000\u0000\u0000\u01bc"+
		"\u01bb\u0001\u0000\u0000\u0000\u01bd\u0007\u0001\u0000\u0000\u0000\u01be"+
		"\u01c4\u0003\u0180\u00c0\u0000\u01bf\u01c4\u0003\u0182\u00c1\u0000\u01c0"+
		"\u01c4\u0003\u0184\u00c2\u0000\u01c1\u01c4\u0003\u001e\u000f\u0000\u01c2"+
		"\u01c4\u0005y\u0000\u0000\u01c3\u01be\u0001\u0000\u0000\u0000\u01c3\u01bf"+
		"\u0001\u0000\u0000\u0000\u01c3\u01c0\u0001\u0000\u0000\u0000\u01c3\u01c1"+
		"\u0001\u0000\u0000\u0000\u01c3\u01c2\u0001\u0000\u0000\u0000\u01c4\t\u0001"+
		"\u0000\u0000\u0000\u01c5\u01c6\u0003\u0154\u00aa\u0000\u01c6\u01c7\u0005"+
		"V\u0000\u0000\u01c7\u01cb\u0005\u007f\u0000\u0000\u01c8\u01ca\u0003\u0006"+
		"\u0003\u0000\u01c9\u01c8\u0001\u0000\u0000\u0000\u01ca\u01cd\u0001\u0000"+
		"\u0000\u0000\u01cb\u01c9\u0001\u0000\u0000\u0000\u01cb\u01cc\u0001\u0000"+
		"\u0000\u0000\u01cc\u01ce\u0001\u0000\u0000\u0000\u01cd\u01cb\u0001\u0000"+
		"\u0000\u0000\u01ce\u01cf\u0005\u0080\u0000\u0000\u01cf\u01d0\u0005s\u0000"+
		"\u0000\u01d0\u01e9\u0001\u0000\u0000\u0000\u01d1\u01d2\u0003\u0154\u00aa"+
		"\u0000\u01d2\u01d3\u0005V\u0000\u0000\u01d3\u01d7\u0005}\u0000\u0000\u01d4"+
		"\u01d6\u0003\u0006\u0003\u0000\u01d5\u01d4\u0001\u0000\u0000\u0000\u01d6"+
		"\u01d9\u0001\u0000\u0000\u0000\u01d7\u01d5\u0001\u0000\u0000\u0000\u01d7"+
		"\u01d8\u0001\u0000\u0000\u0000\u01d8\u01da\u0001\u0000\u0000\u0000\u01d9"+
		"\u01d7\u0001\u0000\u0000\u0000\u01da\u01db\u0005~\u0000\u0000\u01db\u01dc"+
		"\u0005s\u0000\u0000\u01dc\u01e9\u0001\u0000\u0000\u0000\u01dd\u01de\u0003"+
		"\u0154\u00aa\u0000\u01de\u01df\u0005V\u0000\u0000\u01df\u01e3\u0005{\u0000"+
		"\u0000\u01e0\u01e2\u0003\u0006\u0003\u0000\u01e1\u01e0\u0001\u0000\u0000"+
		"\u0000\u01e2\u01e5\u0001\u0000\u0000\u0000\u01e3\u01e1\u0001\u0000\u0000"+
		"\u0000\u01e3\u01e4\u0001\u0000\u0000\u0000\u01e4\u01e6\u0001\u0000\u0000"+
		"\u0000\u01e5\u01e3\u0001\u0000\u0000\u0000\u01e6\u01e7\u0005|\u0000\u0000"+
		"\u01e7\u01e9\u0001\u0000\u0000\u0000\u01e8\u01c5\u0001\u0000\u0000\u0000"+
		"\u01e8\u01d1\u0001\u0000\u0000\u0000\u01e8\u01dd\u0001\u0000\u0000\u0000"+
		"\u01e9\u000b\u0001\u0000\u0000\u0000\u01ea\u01eb\u00056\u0000\u0000\u01eb"+
		"\u01ec\u0005V\u0000\u0000\u01ec\u01ed\u0003\u017c\u00be\u0000\u01ed\u01ee"+
		"\u0003\u000e\u0007\u0000\u01ee\r\u0001\u0000\u0000\u0000\u01ef\u01f0\u0005"+
		"\u007f\u0000\u0000\u01f0\u01f1\u0003\u0010\b\u0000\u01f1\u01f2\u0005\u0080"+
		"\u0000\u0000\u01f2\u01f3\u0005s\u0000\u0000\u01f3\u01fe\u0001\u0000\u0000"+
		"\u0000\u01f4\u01f5\u0005}\u0000\u0000\u01f5\u01f6\u0003\u0010\b\u0000"+
		"\u01f6\u01f7\u0005~\u0000\u0000\u01f7\u01f8\u0005s\u0000\u0000\u01f8\u01fe"+
		"\u0001\u0000\u0000\u0000\u01f9\u01fa\u0005{\u0000\u0000\u01fa\u01fb\u0003"+
		"\u0010\b\u0000\u01fb\u01fc\u0005|\u0000\u0000\u01fc\u01fe\u0001\u0000"+
		"\u0000\u0000\u01fd\u01ef\u0001\u0000\u0000\u0000\u01fd\u01f4\u0001\u0000"+
		"\u0000\u0000\u01fd\u01f9\u0001\u0000\u0000\u0000\u01fe\u000f\u0001\u0000"+
		"\u0000\u0000\u01ff\u0204\u0003\u0012\t\u0000\u0200\u0201\u0005s\u0000"+
		"\u0000\u0201\u0203\u0003\u0012\t\u0000\u0202\u0200\u0001\u0000\u0000\u0000"+
		"\u0203\u0206\u0001\u0000\u0000\u0000\u0204\u0202\u0001\u0000\u0000\u0000"+
		"\u0204\u0205\u0001\u0000\u0000\u0000\u0205\u0208\u0001\u0000\u0000\u0000"+
		"\u0206\u0204\u0001\u0000\u0000\u0000\u0207\u0209\u0005s\u0000\u0000\u0208"+
		"\u0207\u0001\u0000\u0000\u0000\u0208\u0209\u0001\u0000\u0000\u0000\u0209"+
		"\u0011\u0001\u0000\u0000\u0000\u020a\u020b\u0003\u0014\n\u0000\u020b\u020c"+
		"\u0005w\u0000\u0000\u020c\u020d\u0003 \u0010\u0000\u020d\u0013\u0001\u0000"+
		"\u0000\u0000\u020e\u0212\u0005\u007f\u0000\u0000\u020f\u0211\u0003\u0016"+
		"\u000b\u0000\u0210\u020f\u0001\u0000\u0000\u0000\u0211\u0214\u0001\u0000"+
		"\u0000\u0000\u0212\u0210\u0001\u0000\u0000\u0000\u0212\u0213\u0001\u0000"+
		"\u0000\u0000\u0213\u0215\u0001\u0000\u0000\u0000\u0214\u0212\u0001\u0000"+
		"\u0000\u0000\u0215\u0227\u0005\u0080\u0000\u0000\u0216\u021a\u0005}\u0000"+
		"\u0000\u0217\u0219\u0003\u0016\u000b\u0000\u0218\u0217\u0001\u0000\u0000"+
		"\u0000\u0219\u021c\u0001\u0000\u0000\u0000\u021a\u0218\u0001\u0000\u0000"+
		"\u0000\u021a\u021b\u0001\u0000\u0000\u0000\u021b\u021d\u0001\u0000\u0000"+
		"\u0000\u021c\u021a\u0001\u0000\u0000\u0000\u021d\u0227\u0005~\u0000\u0000"+
		"\u021e\u0222\u0005{\u0000\u0000\u021f\u0221\u0003\u0016\u000b\u0000\u0220"+
		"\u021f\u0001\u0000\u0000\u0000\u0221\u0224\u0001\u0000\u0000\u0000\u0222"+
		"\u0220\u0001\u0000\u0000\u0000\u0222\u0223\u0001\u0000\u0000\u0000\u0223"+
		"\u0225\u0001\u0000\u0000\u0000\u0224\u0222\u0001\u0000\u0000\u0000\u0225"+
		"\u0227\u0005|\u0000\u0000\u0226\u020e\u0001\u0000\u0000\u0000\u0226\u0216"+
		"\u0001\u0000\u0000\u0000\u0226\u021e\u0001\u0000\u0000\u0000\u0227\u0015"+
		"\u0001\u0000\u0000\u0000\u0228\u022a\u0003\u0018\f\u0000\u0229\u0228\u0001"+
		"\u0000\u0000\u0000\u022a\u022b\u0001\u0000\u0000\u0000\u022b\u0229\u0001"+
		"\u0000\u0000\u0000\u022b\u022c\u0001\u0000\u0000\u0000\u022c\u0243\u0001"+
		"\u0000\u0000\u0000\u022d\u0243\u0003\u0014\n\u0000\u022e\u0231\u0005y"+
		"\u0000\u0000\u022f\u0232\u0003\u017c\u00be\u0000\u0230\u0232\u0005\u0018"+
		"\u0000\u0000\u0231\u022f\u0001\u0000\u0000\u0000\u0231\u0230\u0001\u0000"+
		"\u0000\u0000\u0232\u0233\u0001\u0000\u0000\u0000\u0233\u0234\u0005t\u0000"+
		"\u0000\u0234\u0243\u0003\u001a\r\u0000\u0235\u0236\u0005y\u0000\u0000"+
		"\u0236\u0238\u0005\u007f\u0000\u0000\u0237\u0239\u0003\u0016\u000b\u0000"+
		"\u0238\u0237\u0001\u0000\u0000\u0000\u0239\u023a\u0001\u0000\u0000\u0000"+
		"\u023a\u0238\u0001\u0000\u0000\u0000\u023a\u023b\u0001\u0000\u0000\u0000"+
		"\u023b\u023c\u0001\u0000\u0000\u0000\u023c\u023e\u0005\u0080\u0000\u0000"+
		"\u023d\u023f\u0003\u001c\u000e\u0000\u023e\u023d\u0001\u0000\u0000\u0000"+
		"\u023e\u023f\u0001\u0000\u0000\u0000\u023f\u0240\u0001\u0000\u0000\u0000"+
		"\u0240\u0241\u0003\u001e\u000f\u0000\u0241\u0243\u0001\u0000\u0000\u0000"+
		"\u0242\u0229\u0001\u0000\u0000\u0000\u0242\u022d\u0001\u0000\u0000\u0000"+
		"\u0242\u022e\u0001\u0000\u0000\u0000\u0242\u0235\u0001\u0000\u0000\u0000"+
		"\u0243\u0017\u0001\u0000\u0000\u0000\u0244\u0249\u0003\u0180\u00c0\u0000"+
		"\u0245\u0249\u0003\u0182\u00c1\u0000\u0246\u0249\u0003\u0184\u00c2\u0000"+
		"\u0247\u0249\u0003\u001e\u000f\u0000\u0248\u0244\u0001\u0000\u0000\u0000"+
		"\u0248\u0245\u0001\u0000\u0000\u0000\u0248\u0246\u0001\u0000\u0000\u0000"+
		"\u0248\u0247\u0001\u0000\u0000\u0000\u0249\u0019\u0001\u0000\u0000\u0000"+
		"\u024a\u024b\u0003\u017c\u00be\u0000\u024b\u001b\u0001\u0000\u0000\u0000"+
		"\u024c\u0251\u0003\u0180\u00c0\u0000\u024d\u0251\u0003\u0182\u00c1\u0000"+
		"\u024e\u0251\u0003\u0184\u00c2\u0000\u024f\u0251\u0005y\u0000\u0000\u0250"+
		"\u024c\u0001\u0000\u0000\u0000\u0250\u024d\u0001\u0000\u0000\u0000\u0250"+
		"\u024e\u0001\u0000\u0000\u0000\u0250\u024f\u0001\u0000\u0000\u0000\u0251"+
		"\u001d\u0001\u0000\u0000\u0000\u0252\u0253\u0007\u0000\u0000\u0000\u0253"+
		"\u001f\u0001\u0000\u0000\u0000\u0254\u0255\u0003\u0004\u0002\u0000\u0255"+
		"!\u0001\u0000\u0000\u0000\u0256\u0258\u0003\u008eG\u0000\u0257\u0256\u0001"+
		"\u0000\u0000\u0000\u0258\u025b\u0001\u0000\u0000\u0000\u0259\u0257\u0001"+
		"\u0000\u0000\u0000\u0259\u025a\u0001\u0000\u0000\u0000\u025a\u025e\u0001"+
		"\u0000\u0000\u0000\u025b\u0259\u0001\u0000\u0000\u0000\u025c\u025f\u0003"+
		"$\u0012\u0000\u025d\u025f\u0003&\u0013\u0000\u025e\u025c\u0001\u0000\u0000"+
		"\u0000\u025e\u025d\u0001\u0000\u0000\u0000\u025f#\u0001\u0000\u0000\u0000"+
		"\u0260\u0262\u0003\u017a\u00bd\u0000\u0261\u0260\u0001\u0000\u0000\u0000"+
		"\u0261\u0262\u0001\u0000\u0000\u0000\u0262\u0270\u0001\u0000\u0000\u0000"+
		"\u0263\u0271\u0003(\u0014\u0000\u0264\u0271\u0003*\u0015\u0000\u0265\u0271"+
		"\u00030\u0018\u0000\u0266\u0271\u00034\u001a\u0000\u0267\u0271\u0003H"+
		"$\u0000\u0268\u0271\u0003J%\u0000\u0269\u0271\u0003X,\u0000\u026a\u0271"+
		"\u0003d2\u0000\u026b\u0271\u0003f3\u0000\u026c\u0271\u0003h4\u0000\u026d"+
		"\u0271\u0003j5\u0000\u026e\u0271\u0003l6\u0000\u026f\u0271\u0003r9\u0000"+
		"\u0270\u0263\u0001\u0000\u0000\u0000\u0270\u0264\u0001\u0000\u0000\u0000"+
		"\u0270\u0265\u0001\u0000\u0000\u0000\u0270\u0266\u0001\u0000\u0000\u0000"+
		"\u0270\u0267\u0001\u0000\u0000\u0000\u0270\u0268\u0001\u0000\u0000\u0000"+
		"\u0270\u0269\u0001\u0000\u0000\u0000\u0270\u026a\u0001\u0000\u0000\u0000"+
		"\u0270\u026b\u0001\u0000\u0000\u0000\u0270\u026c\u0001\u0000\u0000\u0000"+
		"\u0270\u026d\u0001\u0000\u0000\u0000\u0270\u026e\u0001\u0000\u0000\u0000"+
		"\u0270\u026f\u0001\u0000\u0000\u0000\u0271%\u0001\u0000\u0000\u0000\u0272"+
		"\u0275\u0003\n\u0005\u0000\u0273\u0275\u0003\f\u0006\u0000\u0274\u0272"+
		"\u0001\u0000\u0000\u0000\u0274\u0273\u0001\u0000\u0000\u0000\u0275\'\u0001"+
		"\u0000\u0000\u0000\u0276\u0278\u0005 \u0000\u0000\u0277\u0276\u0001\u0000"+
		"\u0000\u0000\u0277\u0278\u0001\u0000\u0000\u0000\u0278\u0279\u0001\u0000"+
		"\u0000\u0000\u0279\u027a\u0005\u0012\u0000\u0000\u027a\u028a\u0003\u017c"+
		"\u00be\u0000\u027b\u028b\u0005s\u0000\u0000\u027c\u0280\u0005{\u0000\u0000"+
		"\u027d\u027f\u0003\u008cF\u0000\u027e\u027d\u0001\u0000\u0000\u0000\u027f"+
		"\u0282\u0001\u0000\u0000\u0000\u0280\u027e\u0001\u0000\u0000\u0000\u0280"+
		"\u0281\u0001\u0000\u0000\u0000\u0281\u0286\u0001\u0000\u0000\u0000\u0282"+
		"\u0280\u0001\u0000\u0000\u0000\u0283\u0285\u0003\"\u0011\u0000\u0284\u0283"+
		"\u0001\u0000\u0000\u0000\u0285\u0288\u0001\u0000\u0000\u0000\u0286\u0284"+
		"\u0001\u0000\u0000\u0000\u0286\u0287\u0001\u0000\u0000\u0000\u0287\u0289"+
		"\u0001\u0000\u0000\u0000\u0288\u0286\u0001\u0000\u0000\u0000\u0289\u028b"+
		"\u0005|\u0000\u0000\u028a\u027b\u0001\u0000\u0000\u0000\u028a\u027c\u0001"+
		"\u0000\u0000\u0000\u028b)\u0001\u0000\u0000\u0000\u028c\u028d\u0005\b"+
		"\u0000\u0000\u028d\u028e\u0005\u0005\u0000\u0000\u028e\u0290\u0003,\u0016"+
		"\u0000\u028f\u0291\u0003.\u0017\u0000\u0290\u028f\u0001\u0000\u0000\u0000"+
		"\u0290\u0291\u0001\u0000\u0000\u0000\u0291\u0292\u0001\u0000\u0000\u0000"+
		"\u0292\u0293\u0005s\u0000\u0000\u0293+\u0001\u0000\u0000\u0000\u0294\u0297"+
		"\u0003\u017c\u00be\u0000\u0295\u0297\u0005\u0018\u0000\u0000\u0296\u0294"+
		"\u0001\u0000\u0000\u0000\u0296\u0295\u0001\u0000\u0000\u0000\u0297-\u0001"+
		"\u0000\u0000\u0000\u0298\u029b\u0005\u0001\u0000\u0000\u0299\u029c\u0003"+
		"\u017c\u00be\u0000\u029a\u029c\u0005m\u0000\u0000\u029b\u0299\u0001\u0000"+
		"\u0000\u0000\u029b\u029a\u0001\u0000\u0000\u0000\u029c/\u0001\u0000\u0000"+
		"\u0000\u029d\u029e\u0005!\u0000\u0000\u029e\u029f\u00032\u0019\u0000\u029f"+
		"\u02a0\u0005s\u0000\u0000\u02a01\u0001\u0000\u0000\u0000\u02a1\u02a3\u0003"+
		"\u0154\u00aa\u0000\u02a2\u02a1\u0001\u0000\u0000\u0000\u02a2\u02a3\u0001"+
		"\u0000\u0000\u0000\u02a3\u02a4\u0001\u0000\u0000\u0000\u02a4\u02a6\u0005"+
		"u\u0000\u0000\u02a5\u02a2\u0001\u0000\u0000\u0000\u02a5\u02a6\u0001\u0000"+
		"\u0000\u0000\u02a6\u02b7\u0001\u0000\u0000\u0000\u02a7\u02b8\u0005R\u0000"+
		"\u0000\u02a8\u02b4\u0005{\u0000\u0000\u02a9\u02ae\u00032\u0019\u0000\u02aa"+
		"\u02ab\u0005r\u0000\u0000\u02ab\u02ad\u00032\u0019\u0000\u02ac\u02aa\u0001"+
		"\u0000\u0000\u0000\u02ad\u02b0\u0001\u0000\u0000\u0000\u02ae\u02ac\u0001"+
		"\u0000\u0000\u0000\u02ae\u02af\u0001\u0000\u0000\u0000\u02af\u02b2\u0001"+
		"\u0000\u0000\u0000\u02b0\u02ae\u0001\u0000\u0000\u0000\u02b1\u02b3\u0005"+
		"r\u0000\u0000\u02b2\u02b1\u0001\u0000\u0000\u0000\u02b2\u02b3\u0001\u0000"+
		"\u0000\u0000\u02b3\u02b5\u0001\u0000\u0000\u0000\u02b4\u02a9\u0001\u0000"+
		"\u0000\u0000\u02b4\u02b5\u0001\u0000\u0000\u0000\u02b5\u02b6\u0001\u0000"+
		"\u0000\u0000\u02b6\u02b8\u0005|\u0000\u0000\u02b7\u02a7\u0001\u0000\u0000"+
		"\u0000\u02b7\u02a8\u0001\u0000\u0000\u0000\u02b8\u02c2\u0001\u0000\u0000"+
		"\u0000\u02b9\u02bf\u0003\u0154\u00aa\u0000\u02ba\u02bd\u0005\u0001\u0000"+
		"\u0000\u02bb\u02be\u0003\u017c\u00be\u0000\u02bc\u02be\u0005m\u0000\u0000"+
		"\u02bd\u02bb\u0001\u0000\u0000\u0000\u02bd\u02bc\u0001\u0000\u0000\u0000"+
		"\u02be\u02c0\u0001\u0000\u0000\u0000\u02bf\u02ba\u0001\u0000\u0000\u0000"+
		"\u02bf\u02c0\u0001\u0000\u0000\u0000\u02c0\u02c2\u0001\u0000\u0000\u0000"+
		"\u02c1\u02a5\u0001\u0000\u0000\u0000\u02c1\u02b9\u0001\u0000\u0000\u0000"+
		"\u02c23\u0001\u0000\u0000\u0000\u02c3\u02c4\u00036\u001b\u0000\u02c4\u02c5"+
		"\u0005\n\u0000\u0000\u02c5\u02c7\u0003\u017c\u00be\u0000\u02c6\u02c8\u0003"+
		"v;\u0000\u02c7\u02c6\u0001\u0000\u0000\u0000\u02c7\u02c8\u0001\u0000\u0000"+
		"\u0000\u02c8\u02c9\u0001\u0000\u0000\u0000\u02c9\u02cb\u0005\u007f\u0000"+
		"\u0000\u02ca\u02cc\u0003:\u001d\u0000\u02cb\u02ca\u0001\u0000\u0000\u0000"+
		"\u02cb\u02cc\u0001\u0000\u0000\u0000\u02cc\u02cd\u0001\u0000\u0000\u0000"+
		"\u02cd\u02cf\u0005\u0080\u0000\u0000\u02ce\u02d0\u0003F#\u0000\u02cf\u02ce"+
		"\u0001\u0000\u0000\u0000\u02cf\u02d0\u0001\u0000\u0000\u0000\u02d0\u02d2"+
		"\u0001\u0000\u0000\u0000\u02d1\u02d3\u0003\u0080@\u0000\u02d2\u02d1\u0001"+
		"\u0000\u0000\u0000\u02d2\u02d3\u0001\u0000\u0000\u0000\u02d3\u02d6\u0001"+
		"\u0000\u0000\u0000\u02d4\u02d7\u0003\u00a8T\u0000\u02d5\u02d7\u0005s\u0000"+
		"\u0000\u02d6\u02d4\u0001\u0000\u0000\u0000\u02d6\u02d5\u0001\u0000\u0000"+
		"\u0000\u02d75\u0001\u0000\u0000\u0000\u02d8\u02da\u0005\u0003\u0000\u0000"+
		"\u02d9\u02d8\u0001\u0000\u0000\u0000\u02d9\u02da\u0001\u0000\u0000\u0000"+
		"\u02da\u02dc\u0001\u0000\u0000\u0000\u02db\u02dd\u0005$\u0000\u0000\u02dc"+
		"\u02db\u0001\u0000\u0000\u0000\u02dc\u02dd\u0001\u0000\u0000\u0000\u02dd"+
		"\u02df\u0001\u0000\u0000\u0000\u02de\u02e0\u0005 \u0000\u0000\u02df\u02de"+
		"\u0001\u0000\u0000\u0000\u02df\u02e0\u0001\u0000\u0000\u0000\u02e0\u02e5"+
		"\u0001\u0000\u0000\u0000\u02e1\u02e3\u0005\b\u0000\u0000\u02e2\u02e4\u0003"+
		"8\u001c\u0000\u02e3\u02e2\u0001\u0000\u0000\u0000\u02e3\u02e4\u0001\u0000"+
		"\u0000\u0000\u02e4\u02e6\u0001\u0000\u0000\u0000\u02e5\u02e1\u0001\u0000"+
		"\u0000\u0000\u02e5\u02e6\u0001\u0000\u0000\u0000\u02e67\u0001\u0000\u0000"+
		"\u0000\u02e7\u02e8\u0007\u0001\u0000\u0000\u02e89\u0001\u0000\u0000\u0000"+
		"\u02e9\u02eb\u0003<\u001e\u0000\u02ea\u02ec\u0005r\u0000\u0000\u02eb\u02ea"+
		"\u0001\u0000\u0000\u0000\u02eb\u02ec\u0001\u0000\u0000\u0000\u02ec\u02fe"+
		"\u0001\u0000\u0000\u0000\u02ed\u02ee\u0003<\u001e\u0000\u02ee\u02ef\u0005"+
		"r\u0000\u0000\u02ef\u02f1\u0001\u0000\u0000\u0000\u02f0\u02ed\u0001\u0000"+
		"\u0000\u0000\u02f0\u02f1\u0001\u0000\u0000\u0000\u02f1\u02f2\u0001\u0000"+
		"\u0000\u0000\u02f2\u02f7\u0003B!\u0000\u02f3\u02f4\u0005r\u0000\u0000"+
		"\u02f4\u02f6\u0003B!\u0000\u02f5\u02f3\u0001\u0000\u0000\u0000\u02f6\u02f9"+
		"\u0001\u0000\u0000\u0000\u02f7\u02f5\u0001\u0000\u0000\u0000\u02f7\u02f8"+
		"\u0001\u0000\u0000\u0000\u02f8\u02fb\u0001\u0000\u0000\u0000\u02f9\u02f7"+
		"\u0001\u0000\u0000\u0000\u02fa\u02fc\u0005r\u0000\u0000\u02fb\u02fa\u0001"+
		"\u0000\u0000\u0000\u02fb\u02fc\u0001\u0000\u0000\u0000\u02fc\u02fe\u0001"+
		"\u0000\u0000\u0000\u02fd\u02e9\u0001\u0000\u0000\u0000\u02fd\u02f0\u0001"+
		"\u0000\u0000\u0000\u02fe;\u0001\u0000\u0000\u0000\u02ff\u0301\u0003\u008e"+
		"G\u0000\u0300\u02ff\u0001\u0000\u0000\u0000\u0301\u0304\u0001\u0000\u0000"+
		"\u0000\u0302\u0300\u0001\u0000\u0000\u0000\u0302\u0303\u0001\u0000\u0000"+
		"\u0000\u0303\u0307\u0001\u0000\u0000\u0000\u0304\u0302\u0001\u0000\u0000"+
		"\u0000\u0305\u0308\u0003>\u001f\u0000\u0306\u0308\u0003@ \u0000\u0307"+
		"\u0305\u0001\u0000\u0000\u0000\u0307\u0306\u0001\u0000\u0000\u0000\u0308"+
		"=\u0001\u0000\u0000\u0000\u0309\u030b\u0005W\u0000\u0000\u030a\u030c\u0003"+
		"\u0152\u00a9\u0000\u030b\u030a\u0001\u0000\u0000\u0000\u030b\u030c\u0001"+
		"\u0000\u0000\u0000\u030c\u030e\u0001\u0000\u0000\u0000\u030d\u0309\u0001"+
		"\u0000\u0000\u0000\u030d\u030e\u0001\u0000\u0000\u0000\u030e\u0310\u0001"+
		"\u0000\u0000\u0000\u030f\u0311\u0005\u0014\u0000\u0000\u0310\u030f\u0001"+
		"\u0000\u0000\u0000\u0310\u0311\u0001\u0000\u0000\u0000\u0311\u0312\u0001"+
		"\u0000\u0000\u0000\u0312\u0313\u0005\u0018\u0000\u0000\u0313?\u0001\u0000"+
		"\u0000\u0000\u0314\u0316\u0005\u0014\u0000\u0000\u0315\u0314\u0001\u0000"+
		"\u0000\u0000\u0315\u0316\u0001\u0000\u0000\u0000\u0316\u0317\u0001\u0000"+
		"\u0000\u0000\u0317\u0318\u0005\u0018\u0000\u0000\u0318\u0319\u0005t\u0000"+
		"\u0000\u0319\u031a\u0003\u0120\u0090\u0000\u031aA\u0001\u0000\u0000\u0000"+
		"\u031b\u031d\u0003\u008eG\u0000\u031c\u031b\u0001\u0000\u0000\u0000\u031d"+
		"\u0320\u0001\u0000\u0000\u0000\u031e\u031c\u0001\u0000\u0000\u0000\u031e"+
		"\u031f\u0001\u0000\u0000\u0000\u031f\u0324\u0001\u0000\u0000\u0000\u0320"+
		"\u031e\u0001\u0000\u0000\u0000\u0321\u0325\u0003D\"\u0000\u0322\u0325"+
		"\u0005p\u0000\u0000\u0323\u0325\u0003\u0120\u0090\u0000\u0324\u0321\u0001"+
		"\u0000\u0000\u0000\u0324\u0322\u0001\u0000\u0000\u0000\u0324\u0323\u0001"+
		"\u0000\u0000\u0000\u0325C\u0001\u0000\u0000\u0000\u0326\u0327\u0003\u00f2"+
		"y\u0000\u0327\u032a\u0005t\u0000\u0000\u0328\u032b\u0003\u0120\u0090\u0000"+
		"\u0329\u032b\u0005p\u0000\u0000\u032a\u0328\u0001\u0000\u0000\u0000\u032a"+
		"\u0329\u0001\u0000\u0000\u0000\u032bE\u0001\u0000\u0000\u0000\u032c\u032d"+
		"\u0005v\u0000\u0000\u032d\u032e\u0003\u0120\u0090\u0000\u032eG\u0001\u0000"+
		"\u0000\u0000\u032f\u0330\u0005\u001f\u0000\u0000\u0330\u0332\u0003\u017c"+
		"\u00be\u0000\u0331\u0333\u0003v;\u0000\u0332\u0331\u0001\u0000\u0000\u0000"+
		"\u0332\u0333\u0001\u0000\u0000\u0000\u0333\u0335\u0001\u0000\u0000\u0000"+
		"\u0334\u0336\u0003\u0080@\u0000\u0335\u0334\u0001\u0000\u0000\u0000\u0335"+
		"\u0336\u0001\u0000\u0000\u0000\u0336\u0339\u0001\u0000\u0000\u0000\u0337"+
		"\u0338\u0005e\u0000\u0000\u0338\u033a\u0003\u0120\u0090\u0000\u0339\u0337"+
		"\u0001\u0000\u0000\u0000\u0339\u033a\u0001\u0000\u0000\u0000\u033a\u033b"+
		"\u0001\u0000\u0000\u0000\u033b\u033c\u0005s\u0000\u0000\u033cI\u0001\u0000"+
		"\u0000\u0000\u033d\u0340\u0003L&\u0000\u033e\u0340\u0003N\'\u0000\u033f"+
		"\u033d\u0001\u0000\u0000\u0000\u033f\u033e\u0001\u0000\u0000\u0000\u0340"+
		"K\u0001\u0000\u0000\u0000\u0341\u0342\u0005\u001b\u0000\u0000\u0342\u0344"+
		"\u0003\u017c\u00be\u0000\u0343\u0345\u0003v;\u0000\u0344\u0343\u0001\u0000"+
		"\u0000\u0000\u0344\u0345\u0001\u0000\u0000\u0000\u0345\u0347\u0001\u0000"+
		"\u0000\u0000\u0346\u0348\u0003\u0080@\u0000\u0347\u0346\u0001\u0000\u0000"+
		"\u0000\u0347\u0348\u0001\u0000\u0000\u0000\u0348\u034f\u0001\u0000\u0000"+
		"\u0000\u0349\u034b\u0005{\u0000\u0000\u034a\u034c\u0003P(\u0000\u034b"+
		"\u034a\u0001\u0000\u0000\u0000\u034b\u034c\u0001\u0000\u0000\u0000\u034c"+
		"\u034d\u0001\u0000\u0000\u0000\u034d\u0350\u0005|\u0000\u0000\u034e\u0350"+
		"\u0005s\u0000\u0000\u034f\u0349\u0001\u0000\u0000\u0000\u034f\u034e\u0001"+
		"\u0000\u0000\u0000\u0350M\u0001\u0000\u0000\u0000\u0351\u0352\u0005\u001b"+
		"\u0000\u0000\u0352\u0354\u0003\u017c\u00be\u0000\u0353\u0355\u0003v;\u0000"+
		"\u0354\u0353\u0001\u0000\u0000\u0000\u0354\u0355\u0001\u0000\u0000\u0000"+
		"\u0355\u0356\u0001\u0000\u0000\u0000\u0356\u0358\u0005\u007f\u0000\u0000"+
		"\u0357\u0359\u0003T*\u0000\u0358\u0357\u0001\u0000\u0000\u0000\u0358\u0359"+
		"\u0001\u0000\u0000\u0000\u0359\u035a\u0001\u0000\u0000\u0000\u035a\u035c"+
		"\u0005\u0080\u0000\u0000\u035b\u035d\u0003\u0080@\u0000\u035c\u035b\u0001"+
		"\u0000\u0000\u0000\u035c\u035d\u0001\u0000\u0000\u0000\u035d\u035e\u0001"+
		"\u0000\u0000\u0000\u035e\u035f\u0005s\u0000\u0000\u035fO\u0001\u0000\u0000"+
		"\u0000\u0360\u0365\u0003R)\u0000\u0361\u0362\u0005r\u0000\u0000\u0362"+
		"\u0364\u0003R)\u0000\u0363\u0361\u0001\u0000\u0000\u0000\u0364\u0367\u0001"+
		"\u0000\u0000\u0000\u0365\u0363\u0001\u0000\u0000\u0000\u0365\u0366\u0001"+
		"\u0000\u0000\u0000\u0366\u0369\u0001\u0000\u0000\u0000\u0367\u0365\u0001"+
		"\u0000\u0000\u0000\u0368\u036a\u0005r\u0000\u0000\u0369\u0368\u0001\u0000"+
		"\u0000\u0000\u0369\u036a\u0001\u0000\u0000\u0000\u036aQ\u0001\u0000\u0000"+
		"\u0000\u036b\u036d\u0003\u008eG\u0000\u036c\u036b\u0001\u0000\u0000\u0000"+
		"\u036d\u0370\u0001\u0000\u0000\u0000\u036e\u036c\u0001\u0000\u0000\u0000"+
		"\u036e\u036f\u0001\u0000\u0000\u0000\u036f\u0372\u0001\u0000\u0000\u0000"+
		"\u0370\u036e\u0001\u0000\u0000\u0000\u0371\u0373\u0003\u017a\u00bd\u0000"+
		"\u0372\u0371\u0001\u0000\u0000\u0000\u0372\u0373\u0001\u0000\u0000\u0000"+
		"\u0373\u0374\u0001\u0000\u0000\u0000\u0374\u0375\u0003\u017c\u00be\u0000"+
		"\u0375\u0376\u0005t\u0000\u0000\u0376\u0377\u0003\u0120\u0090\u0000\u0377"+
		"S\u0001\u0000\u0000\u0000\u0378\u037d\u0003V+\u0000\u0379\u037a\u0005"+
		"r\u0000\u0000\u037a\u037c\u0003V+\u0000\u037b\u0379\u0001\u0000\u0000"+
		"\u0000\u037c\u037f\u0001\u0000\u0000\u0000\u037d\u037b\u0001\u0000\u0000"+
		"\u0000\u037d\u037e\u0001\u0000\u0000\u0000\u037e\u0381\u0001\u0000\u0000"+
		"\u0000\u037f\u037d\u0001\u0000\u0000\u0000\u0380\u0382\u0005r\u0000\u0000"+
		"\u0381\u0380\u0001\u0000\u0000\u0000\u0381\u0382\u0001\u0000\u0000\u0000"+
		"\u0382U\u0001\u0000\u0000\u0000\u0383\u0385\u0003\u008eG\u0000\u0384\u0383"+
		"\u0001\u0000\u0000\u0000\u0385\u0388\u0001\u0000\u0000\u0000\u0386\u0384"+
		"\u0001\u0000\u0000\u0000\u0386\u0387\u0001\u0000\u0000\u0000\u0387\u038a"+
		"\u0001\u0000\u0000\u0000\u0388\u0386\u0001\u0000\u0000\u0000\u0389\u038b"+
		"\u0003\u017a\u00bd\u0000\u038a\u0389\u0001\u0000\u0000\u0000\u038a\u038b"+
		"\u0001\u0000\u0000\u0000\u038b\u038c\u0001\u0000\u0000\u0000\u038c\u038d"+
		"\u0003\u0120\u0090\u0000\u038dW\u0001\u0000\u0000\u0000\u038e\u038f\u0005"+
		"\u0007\u0000\u0000\u038f\u0391\u0003\u017c\u00be\u0000\u0390\u0392\u0003"+
		"v;\u0000\u0391\u0390\u0001\u0000\u0000\u0000\u0391\u0392\u0001\u0000\u0000"+
		"\u0000\u0392\u0394\u0001\u0000\u0000\u0000\u0393\u0395\u0003\u0080@\u0000"+
		"\u0394\u0393\u0001\u0000\u0000\u0000\u0394\u0395\u0001\u0000\u0000\u0000"+
		"\u0395\u0396\u0001\u0000\u0000\u0000\u0396\u0398\u0005{\u0000\u0000\u0397"+
		"\u0399\u0003Z-\u0000\u0398\u0397\u0001\u0000\u0000\u0000\u0398\u0399\u0001"+
		"\u0000\u0000\u0000\u0399\u039a\u0001\u0000\u0000\u0000\u039a\u039b\u0005"+
		"|\u0000\u0000\u039bY\u0001\u0000\u0000\u0000\u039c\u03a1\u0003\\.\u0000"+
		"\u039d\u039e\u0005r\u0000\u0000\u039e\u03a0\u0003\\.\u0000\u039f\u039d"+
		"\u0001\u0000\u0000\u0000\u03a0\u03a3\u0001\u0000\u0000\u0000\u03a1\u039f"+
		"\u0001\u0000\u0000\u0000\u03a1\u03a2\u0001\u0000\u0000\u0000\u03a2\u03a5"+
		"\u0001\u0000\u0000\u0000\u03a3\u03a1\u0001\u0000\u0000\u0000\u03a4\u03a6"+
		"\u0005r\u0000\u0000\u03a5\u03a4\u0001\u0000\u0000\u0000\u03a5\u03a6\u0001"+
		"\u0000\u0000\u0000\u03a6[\u0001\u0000\u0000\u0000\u03a7\u03a9\u0003\u008e"+
		"G\u0000\u03a8\u03a7\u0001\u0000\u0000\u0000\u03a9\u03ac\u0001\u0000\u0000"+
		"\u0000\u03aa\u03a8\u0001\u0000\u0000\u0000\u03aa\u03ab\u0001\u0000\u0000"+
		"\u0000\u03ab\u03ae\u0001\u0000\u0000\u0000\u03ac\u03aa\u0001\u0000\u0000"+
		"\u0000\u03ad\u03af\u0003\u017a\u00bd\u0000\u03ae\u03ad\u0001\u0000\u0000"+
		"\u0000\u03ae\u03af\u0001\u0000\u0000\u0000\u03af\u03b0\u0001\u0000\u0000"+
		"\u0000\u03b0\u03b4\u0003\u017c\u00be\u0000\u03b1\u03b5\u0003^/\u0000\u03b2"+
		"\u03b5\u0003`0\u0000\u03b3\u03b5\u0003b1\u0000\u03b4\u03b1\u0001\u0000"+
		"\u0000\u0000\u03b4\u03b2\u0001\u0000\u0000\u0000\u03b4\u03b3\u0001\u0000"+
		"\u0000\u0000\u03b4\u03b5\u0001\u0000\u0000\u0000\u03b5]\u0001\u0000\u0000"+
		"\u0000\u03b6\u03b8\u0005\u007f\u0000\u0000\u03b7\u03b9\u0003T*\u0000\u03b8"+
		"\u03b7\u0001\u0000\u0000\u0000\u03b8\u03b9\u0001\u0000\u0000\u0000\u03b9"+
		"\u03ba\u0001\u0000\u0000\u0000\u03ba\u03bb\u0005\u0080\u0000\u0000\u03bb"+
		"_\u0001\u0000\u0000\u0000\u03bc\u03be\u0005{\u0000\u0000\u03bd\u03bf\u0003"+
		"P(\u0000\u03be\u03bd\u0001\u0000\u0000\u0000\u03be\u03bf\u0001\u0000\u0000"+
		"\u0000\u03bf\u03c0\u0001\u0000\u0000\u0000\u03c0\u03c1\u0005|\u0000\u0000"+
		"\u03c1a\u0001\u0000\u0000\u0000\u03c2\u03c3\u0005e\u0000\u0000\u03c3\u03c4"+
		"\u0003\u009aM\u0000\u03c4c\u0001\u0000\u0000\u0000\u03c5\u03c6\u00054"+
		"\u0000\u0000\u03c6\u03c8\u0003\u017c\u00be\u0000\u03c7\u03c9\u0003v;\u0000"+
		"\u03c8\u03c7\u0001\u0000\u0000\u0000\u03c8\u03c9\u0001\u0000\u0000\u0000"+
		"\u03c9\u03cb\u0001\u0000\u0000\u0000\u03ca\u03cc\u0003\u0080@\u0000\u03cb"+
		"\u03ca\u0001\u0000\u0000\u0000\u03cb\u03cc\u0001\u0000\u0000\u0000\u03cc"+
		"\u03cd\u0001\u0000\u0000\u0000\u03cd\u03ce\u0005{\u0000\u0000\u03ce\u03cf"+
		"\u0003P(\u0000\u03cf\u03d0\u0005|\u0000\u0000\u03d0e\u0001\u0000\u0000"+
		"\u0000\u03d1\u03d4\u0005\u0003\u0000\u0000\u03d2\u03d5\u0003\u017c\u00be"+
		"\u0000\u03d3\u03d5\u0005m\u0000\u0000\u03d4\u03d2\u0001\u0000\u0000\u0000"+
		"\u03d4\u03d3\u0001\u0000\u0000\u0000\u03d5\u03d6\u0001\u0000\u0000\u0000"+
		"\u03d6\u03d7\u0005t\u0000\u0000\u03d7\u03da\u0003\u0120\u0090\u0000\u03d8"+
		"\u03d9\u0005e\u0000\u0000\u03d9\u03db\u0003\u009aM\u0000\u03da\u03d8\u0001"+
		"\u0000\u0000\u0000\u03da\u03db\u0001\u0000\u0000\u0000\u03db\u03dc\u0001"+
		"\u0000\u0000\u0000\u03dc\u03dd\u0005s\u0000\u0000\u03ddg\u0001\u0000\u0000"+
		"\u0000\u03de\u03e0\u0005\u001a\u0000\u0000\u03df\u03e1\u0005\u0014\u0000"+
		"\u0000\u03e0\u03df\u0001\u0000\u0000\u0000\u03e0\u03e1\u0001\u0000\u0000"+
		"\u0000\u03e1\u03e2\u0001\u0000\u0000\u0000\u03e2\u03e3\u0003\u017c\u00be"+
		"\u0000\u03e3\u03e4\u0005t\u0000\u0000\u03e4\u03e7\u0003\u0120\u0090\u0000"+
		"\u03e5\u03e6\u0005e\u0000\u0000\u03e6\u03e8\u0003\u009aM\u0000\u03e7\u03e5"+
		"\u0001\u0000\u0000\u0000\u03e7\u03e8\u0001\u0000\u0000\u0000\u03e8\u03e9"+
		"\u0001\u0000\u0000\u0000\u03e9\u03ea\u0005s\u0000\u0000\u03eai\u0001\u0000"+
		"\u0000\u0000\u03eb\u03ed\u0005 \u0000\u0000\u03ec\u03eb\u0001\u0000\u0000"+
		"\u0000\u03ec\u03ed\u0001\u0000\u0000\u0000\u03ed\u03ee\u0001\u0000\u0000"+
		"\u0000\u03ee\u03ef\u0005\u001d\u0000\u0000\u03ef\u03f1\u0003\u017c\u00be"+
		"\u0000\u03f0\u03f2\u0003v;\u0000\u03f1\u03f0\u0001\u0000\u0000\u0000\u03f1"+
		"\u03f2\u0001\u0000\u0000\u0000\u03f2\u03f7\u0001\u0000\u0000\u0000\u03f3"+
		"\u03f5\u0005t\u0000\u0000\u03f4\u03f6\u0003\u014a\u00a5\u0000\u03f5\u03f4"+
		"\u0001\u0000\u0000\u0000\u03f5\u03f6\u0001\u0000\u0000\u0000\u03f6\u03f8"+
		"\u0001\u0000\u0000\u0000\u03f7\u03f3\u0001\u0000\u0000\u0000\u03f7\u03f8"+
		"\u0001\u0000\u0000\u0000\u03f8\u03fa\u0001\u0000\u0000\u0000\u03f9\u03fb"+
		"\u0003\u0080@\u0000\u03fa\u03f9\u0001\u0000\u0000\u0000\u03fa\u03fb\u0001"+
		"\u0000\u0000\u0000\u03fb\u03fc\u0001\u0000\u0000\u0000\u03fc\u0400\u0005"+
		"{\u0000\u0000\u03fd\u03ff\u0003\u008cF\u0000\u03fe\u03fd\u0001\u0000\u0000"+
		"\u0000\u03ff\u0402\u0001\u0000\u0000\u0000\u0400\u03fe\u0001\u0000\u0000"+
		"\u0000\u0400\u0401\u0001\u0000\u0000\u0000\u0401\u0406\u0001\u0000\u0000"+
		"\u0000\u0402\u0400\u0001\u0000\u0000\u0000\u0403\u0405\u0003\u008aE\u0000"+
		"\u0404\u0403\u0001\u0000\u0000\u0000\u0405\u0408\u0001\u0000\u0000\u0000"+
		"\u0406\u0404\u0001\u0000\u0000\u0000\u0406\u0407\u0001\u0000\u0000\u0000"+
		"\u0407\u0409\u0001\u0000\u0000\u0000\u0408\u0406\u0001\u0000\u0000\u0000"+
		"\u0409\u040a\u0005|\u0000\u0000\u040ak\u0001\u0000\u0000\u0000\u040b\u040e"+
		"\u0003n7\u0000\u040c\u040e\u0003p8\u0000\u040d\u040b\u0001\u0000\u0000"+
		"\u0000\u040d\u040c\u0001\u0000\u0000\u0000\u040em\u0001\u0000\u0000\u0000"+
		"\u040f\u0411\u0005\r\u0000\u0000\u0410\u0412\u0003v;\u0000\u0411\u0410"+
		"\u0001\u0000\u0000\u0000\u0411\u0412\u0001\u0000\u0000\u0000\u0412\u0413"+
		"\u0001\u0000\u0000\u0000\u0413\u0415\u0003\u0120\u0090\u0000\u0414\u0416"+
		"\u0003\u0080@\u0000\u0415\u0414\u0001\u0000\u0000\u0000\u0415\u0416\u0001"+
		"\u0000\u0000\u0000\u0416\u0417\u0001\u0000\u0000\u0000\u0417\u041b\u0005"+
		"{\u0000\u0000\u0418\u041a\u0003\u008cF\u0000\u0419\u0418\u0001\u0000\u0000"+
		"\u0000\u041a\u041d\u0001\u0000\u0000\u0000\u041b\u0419\u0001\u0000\u0000"+
		"\u0000\u041b\u041c\u0001\u0000\u0000\u0000\u041c\u0421\u0001\u0000\u0000"+
		"\u0000\u041d\u041b\u0001\u0000\u0000\u0000\u041e\u0420\u0003\u008aE\u0000"+
		"\u041f\u041e\u0001\u0000\u0000\u0000\u0420\u0423\u0001\u0000\u0000\u0000"+
		"\u0421\u041f\u0001\u0000\u0000\u0000\u0421\u0422\u0001\u0000\u0000\u0000"+
		"\u0422\u0424\u0001\u0000\u0000\u0000\u0423\u0421\u0001\u0000\u0000\u0000"+
		"\u0424\u0425\u0005|\u0000\u0000\u0425o\u0001\u0000\u0000\u0000\u0426\u0428"+
		"\u0005 \u0000\u0000\u0427\u0426\u0001\u0000\u0000\u0000\u0427\u0428\u0001"+
		"\u0000\u0000\u0000\u0428\u0429\u0001\u0000\u0000\u0000\u0429\u042b\u0005"+
		"\r\u0000\u0000\u042a\u042c\u0003v;\u0000\u042b\u042a\u0001\u0000\u0000"+
		"\u0000\u042b\u042c\u0001\u0000\u0000\u0000\u042c\u042e\u0001\u0000\u0000"+
		"\u0000\u042d\u042f\u0005V\u0000\u0000\u042e\u042d\u0001\u0000\u0000\u0000"+
		"\u042e\u042f\u0001\u0000\u0000\u0000\u042f\u0430\u0001\u0000\u0000\u0000"+
		"\u0430\u0431\u0003\u0172\u00b9\u0000\u0431\u0432\u0005\u000b\u0000\u0000"+
		"\u0432\u0434\u0003\u0120\u0090\u0000\u0433\u0435\u0003\u0080@\u0000\u0434"+
		"\u0433\u0001\u0000\u0000\u0000\u0434\u0435\u0001\u0000\u0000\u0000\u0435"+
		"\u0436\u0001\u0000\u0000\u0000\u0436\u043a\u0005{\u0000\u0000\u0437\u0439"+
		"\u0003\u008cF\u0000\u0438\u0437\u0001\u0000\u0000\u0000\u0439\u043c\u0001"+
		"\u0000\u0000\u0000\u043a\u0438\u0001\u0000\u0000\u0000\u043a\u043b\u0001"+
		"\u0000\u0000\u0000\u043b\u0440\u0001\u0000\u0000\u0000\u043c\u043a\u0001"+
		"\u0000\u0000\u0000\u043d\u043f\u0003\u008aE\u0000\u043e\u043d\u0001\u0000"+
		"\u0000\u0000\u043f\u0442\u0001\u0000\u0000\u0000\u0440\u043e\u0001\u0000"+
		"\u0000\u0000\u0440\u0441\u0001\u0000\u0000\u0000\u0441\u0443\u0001\u0000"+
		"\u0000\u0000\u0442\u0440\u0001\u0000\u0000\u0000\u0443\u0444\u0005|\u0000"+
		"\u0000\u0444q\u0001\u0000\u0000\u0000\u0445\u0447\u0005 \u0000\u0000\u0446"+
		"\u0445\u0001\u0000\u0000\u0000\u0446\u0447\u0001\u0000\u0000\u0000\u0447"+
		"\u0448\u0001\u0000\u0000\u0000\u0448\u044a\u0005\b\u0000\u0000\u0449\u044b"+
		"\u00038\u001c\u0000\u044a\u0449\u0001\u0000\u0000\u0000\u044a\u044b\u0001"+
		"\u0000\u0000\u0000\u044b\u044c\u0001\u0000\u0000\u0000\u044c\u0450\u0005"+
		"{\u0000\u0000\u044d\u044f\u0003\u008cF\u0000\u044e\u044d\u0001\u0000\u0000"+
		"\u0000\u044f\u0452\u0001\u0000\u0000\u0000\u0450\u044e\u0001\u0000\u0000"+
		"\u0000\u0450\u0451\u0001\u0000\u0000\u0000\u0451\u0456\u0001\u0000\u0000"+
		"\u0000\u0452\u0450\u0001\u0000\u0000\u0000\u0453\u0455\u0003t:\u0000\u0454"+
		"\u0453\u0001\u0000\u0000\u0000\u0455\u0458\u0001\u0000\u0000\u0000\u0456"+
		"\u0454\u0001\u0000\u0000\u0000\u0456\u0457\u0001\u0000\u0000\u0000\u0457"+
		"\u0459\u0001\u0000\u0000\u0000\u0458\u0456\u0001\u0000\u0000\u0000\u0459"+
		"\u045a\u0005|\u0000\u0000\u045as\u0001\u0000\u0000\u0000\u045b\u045d\u0003"+
		"\u008eG\u0000\u045c\u045b\u0001\u0000\u0000\u0000\u045d\u0460\u0001\u0000"+
		"\u0000\u0000\u045e\u045c\u0001\u0000\u0000\u0000\u045e\u045f\u0001\u0000"+
		"\u0000\u0000\u045f\u0469\u0001\u0000\u0000\u0000\u0460\u045e\u0001\u0000"+
		"\u0000\u0000\u0461\u046a\u0003\n\u0005\u0000\u0462\u0464\u0003\u017a\u00bd"+
		"\u0000\u0463\u0462\u0001\u0000\u0000\u0000\u0463\u0464\u0001\u0000\u0000"+
		"\u0000\u0464\u0467\u0001\u0000\u0000\u0000\u0465\u0468\u0003h4\u0000\u0466"+
		"\u0468\u00034\u001a\u0000\u0467\u0465\u0001\u0000\u0000\u0000\u0467\u0466"+
		"\u0001\u0000\u0000\u0000\u0468\u046a\u0001\u0000\u0000\u0000\u0469\u0461"+
		"\u0001\u0000\u0000\u0000\u0469\u0463\u0001\u0000\u0000\u0000\u046au\u0001"+
		"\u0000\u0000\u0000\u046b\u0478\u0005i\u0000\u0000\u046c\u046d\u0003x<"+
		"\u0000\u046d\u046e\u0005r\u0000\u0000\u046e\u0470\u0001\u0000\u0000\u0000"+
		"\u046f\u046c\u0001\u0000\u0000\u0000\u0470\u0473\u0001\u0000\u0000\u0000"+
		"\u0471\u046f\u0001\u0000\u0000\u0000\u0471\u0472\u0001\u0000\u0000\u0000"+
		"\u0472\u0474\u0001\u0000\u0000\u0000\u0473\u0471\u0001\u0000\u0000\u0000"+
		"\u0474\u0476\u0003x<\u0000\u0475\u0477\u0005r\u0000\u0000\u0476\u0475"+
		"\u0001\u0000\u0000\u0000\u0476\u0477\u0001\u0000\u0000\u0000\u0477\u0479"+
		"\u0001\u0000\u0000\u0000\u0478\u0471\u0001\u0000\u0000\u0000\u0478\u0479"+
		"\u0001\u0000\u0000\u0000\u0479\u047a\u0001\u0000\u0000\u0000\u047a\u047b"+
		"\u0005h\u0000\u0000\u047bw\u0001\u0000\u0000\u0000\u047c\u047e\u0003\u008e"+
		"G\u0000\u047d\u047c\u0001\u0000\u0000\u0000\u047e\u0481\u0001\u0000\u0000"+
		"\u0000\u047f\u047d\u0001\u0000\u0000\u0000\u047f\u0480\u0001\u0000\u0000"+
		"\u0000\u0480\u0485\u0001\u0000\u0000\u0000\u0481\u047f\u0001\u0000\u0000"+
		"\u0000\u0482\u0486\u0003z=\u0000\u0483\u0486\u0003|>\u0000\u0484\u0486"+
		"\u0003~?\u0000\u0485\u0482\u0001\u0000\u0000\u0000\u0485\u0483\u0001\u0000"+
		"\u0000\u0000\u0485\u0484\u0001\u0000\u0000\u0000\u0486y\u0001\u0000\u0000"+
		"\u0000\u0487\u0489\u0003\u008eG\u0000\u0488\u0487\u0001\u0000\u0000\u0000"+
		"\u0488\u0489\u0001\u0000\u0000\u0000\u0489\u048a\u0001\u0000\u0000\u0000"+
		"\u048a\u048d\u0005O\u0000\u0000\u048b\u048c\u0005t\u0000\u0000\u048c\u048e"+
		"\u0003\u0150\u00a8\u0000\u048d\u048b\u0001\u0000\u0000\u0000\u048d\u048e"+
		"\u0001\u0000\u0000\u0000\u048e{\u0001\u0000\u0000\u0000\u048f\u0491\u0003"+
		"\u008eG\u0000\u0490\u048f\u0001\u0000\u0000\u0000\u0490\u0491\u0001\u0000"+
		"\u0000\u0000\u0491\u0492\u0001\u0000\u0000\u0000\u0492\u0497\u0003\u017c"+
		"\u00be\u0000\u0493\u0495\u0005t\u0000\u0000\u0494\u0496\u0003\u014a\u00a5"+
		"\u0000\u0495\u0494\u0001\u0000\u0000\u0000\u0495\u0496\u0001\u0000\u0000"+
		"\u0000\u0496\u0498\u0001\u0000\u0000\u0000\u0497\u0493\u0001\u0000\u0000"+
		"\u0000\u0497\u0498\u0001\u0000\u0000\u0000\u0498\u049b\u0001\u0000\u0000"+
		"\u0000\u0499\u049a\u0005e\u0000\u0000\u049a\u049c\u0003\u0120\u0090\u0000"+
		"\u049b\u0499\u0001\u0000\u0000\u0000\u049b\u049c\u0001\u0000\u0000\u0000"+
		"\u049c}\u0001\u0000\u0000\u0000\u049d\u049e\u0005\u0003\u0000\u0000\u049e"+
		"\u049f\u0003\u017c\u00be\u0000\u049f\u04a0\u0005t\u0000\u0000\u04a0\u04a1"+
		"\u0003\u0120\u0090\u0000\u04a1\u007f\u0001\u0000\u0000\u0000\u04a2\u04a8"+
		"\u0005\"\u0000\u0000\u04a3\u04a4\u0003\u0082A\u0000\u04a4\u04a5\u0005"+
		"r\u0000\u0000\u04a5\u04a7\u0001\u0000\u0000\u0000\u04a6\u04a3\u0001\u0000"+
		"\u0000\u0000\u04a7\u04aa\u0001\u0000\u0000\u0000\u04a8\u04a6\u0001\u0000"+
		"\u0000\u0000\u04a8\u04a9\u0001\u0000\u0000\u0000\u04a9\u04ac\u0001\u0000"+
		"\u0000\u0000\u04aa\u04a8\u0001\u0000\u0000\u0000\u04ab\u04ad\u0003\u0082"+
		"A\u0000\u04ac\u04ab\u0001\u0000\u0000\u0000\u04ac\u04ad\u0001\u0000\u0000"+
		"\u0000\u04ad\u0081\u0001\u0000\u0000\u0000\u04ae\u04b1\u0003\u0084B\u0000"+
		"\u04af\u04b1\u0003\u0086C\u0000\u04b0\u04ae\u0001\u0000\u0000\u0000\u04b0"+
		"\u04af\u0001\u0000\u0000\u0000\u04b1\u0083\u0001\u0000\u0000\u0000\u04b2"+
		"\u04b3\u0003\u0152\u00a9\u0000\u04b3\u04b4\u0005t\u0000\u0000\u04b4\u04b5"+
		"\u0003\u0150\u00a8\u0000\u04b5\u0085\u0001\u0000\u0000\u0000\u04b6\u04b8"+
		"\u0003\u0088D\u0000\u04b7\u04b6\u0001\u0000\u0000\u0000\u04b7\u04b8\u0001"+
		"\u0000\u0000\u0000\u04b8\u04b9\u0001\u0000\u0000\u0000\u04b9\u04ba\u0003"+
		"\u0120\u0090\u0000\u04ba\u04bc\u0005t\u0000\u0000\u04bb\u04bd\u0003\u014a"+
		"\u00a5\u0000\u04bc\u04bb\u0001\u0000\u0000\u0000\u04bc\u04bd\u0001\u0000"+
		"\u0000\u0000\u04bd\u0087\u0001\u0000\u0000\u0000\u04be\u04bf\u0005\u000b"+
		"\u0000\u0000\u04bf\u04c0\u0003v;\u0000\u04c0\u0089\u0001\u0000\u0000\u0000"+
		"\u04c1\u04c3\u0003\u008eG\u0000\u04c2\u04c1\u0001\u0000\u0000\u0000\u04c3"+
		"\u04c6\u0001\u0000\u0000\u0000\u04c4\u04c2\u0001\u0000\u0000\u0000\u04c4"+
		"\u04c5\u0001\u0000\u0000\u0000\u04c5\u04d0\u0001\u0000\u0000\u0000\u04c6"+
		"\u04c4\u0001\u0000\u0000\u0000\u04c7\u04d1\u0003\n\u0005\u0000\u04c8\u04ca"+
		"\u0003\u017a\u00bd\u0000\u04c9\u04c8\u0001\u0000\u0000\u0000\u04c9\u04ca"+
		"\u0001\u0000\u0000\u0000\u04ca\u04ce\u0001\u0000\u0000\u0000\u04cb\u04cf"+
		"\u0003H$\u0000\u04cc\u04cf\u0003f3\u0000\u04cd\u04cf\u00034\u001a\u0000"+
		"\u04ce\u04cb\u0001\u0000\u0000\u0000\u04ce\u04cc\u0001\u0000\u0000\u0000"+
		"\u04ce\u04cd\u0001\u0000\u0000\u0000\u04cf\u04d1\u0001\u0000\u0000\u0000"+
		"\u04d0\u04c7\u0001\u0000\u0000\u0000\u04d0\u04c9\u0001\u0000\u0000\u0000"+
		"\u04d1\u008b\u0001\u0000\u0000\u0000\u04d2\u04d3\u0005x\u0000\u0000\u04d3"+
		"\u04d4\u0005V\u0000\u0000\u04d4\u04d5\u0005}\u0000\u0000\u04d5\u04d6\u0003"+
		"\u0090H\u0000\u04d6\u04d7\u0005~\u0000\u0000\u04d7\u008d\u0001\u0000\u0000"+
		"\u0000\u04d8\u04d9\u0005x\u0000\u0000\u04d9\u04da\u0005}\u0000\u0000\u04da"+
		"\u04db\u0003\u0090H\u0000\u04db\u04dc\u0005~\u0000\u0000\u04dc\u008f\u0001"+
		"\u0000\u0000\u0000\u04dd\u04df\u0003\u0154\u00aa\u0000\u04de\u04e0\u0003"+
		"\u0092I\u0000\u04df\u04de\u0001\u0000\u0000\u0000\u04df\u04e0\u0001\u0000"+
		"\u0000\u0000\u04e0\u0091\u0001\u0000\u0000\u0000\u04e1\u04e5\u0003\u0004"+
		"\u0002\u0000\u04e2\u04e3\u0005e\u0000\u0000\u04e3\u04e5\u0003\u00a4R\u0000"+
		"\u04e4\u04e1\u0001\u0000\u0000\u0000\u04e4\u04e2\u0001\u0000\u0000\u0000"+
		"\u04e5\u0093\u0001\u0000\u0000\u0000\u04e6\u04ec\u0005s\u0000\u0000\u04e7"+
		"\u04ec\u0003\"\u0011\u0000\u04e8\u04ec\u0003\u0096K\u0000\u04e9\u04ec"+
		"\u0003\u0098L\u0000\u04ea\u04ec\u0003\n\u0005\u0000\u04eb\u04e6\u0001"+
		"\u0000\u0000\u0000\u04eb\u04e7\u0001\u0000\u0000\u0000\u04eb\u04e8\u0001"+
		"\u0000\u0000\u0000\u04eb\u04e9\u0001\u0000\u0000\u0000\u04eb\u04ea\u0001"+
		"\u0000\u0000\u0000\u04ec\u0095\u0001\u0000\u0000\u0000\u04ed\u04ef\u0003"+
		"\u008eG\u0000\u04ee\u04ed\u0001\u0000\u0000\u0000\u04ef\u04f2\u0001\u0000"+
		"\u0000\u0000\u04f0\u04ee\u0001\u0000\u0000\u0000\u04f0\u04f1\u0001\u0000"+
		"\u0000\u0000\u04f1\u04f3\u0001\u0000\u0000\u0000\u04f2\u04f0\u0001\u0000"+
		"\u0000\u0000\u04f3\u04f4\u0005\u000f\u0000\u0000\u04f4\u04f7\u0003\u00f4"+
		"z\u0000\u04f5\u04f6\u0005t\u0000\u0000\u04f6\u04f8\u0003\u0120\u0090\u0000"+
		"\u04f7\u04f5\u0001\u0000\u0000\u0000\u04f7\u04f8\u0001\u0000\u0000\u0000"+
		"\u04f8\u04fb\u0001\u0000\u0000\u0000\u04f9\u04fa\u0005e\u0000\u0000\u04fa"+
		"\u04fc\u0003\u009aM\u0000\u04fb\u04f9\u0001\u0000\u0000\u0000\u04fb\u04fc"+
		"\u0001\u0000\u0000\u0000\u04fc\u04fd\u0001\u0000\u0000\u0000\u04fd\u04fe"+
		"\u0005s\u0000\u0000\u04fe\u0097\u0001\u0000\u0000\u0000\u04ff\u0500\u0003"+
		"\u009aM\u0000\u0500\u0501\u0005s\u0000\u0000\u0501\u0507\u0001\u0000\u0000"+
		"\u0000\u0502\u0504\u0003\u00a2Q\u0000\u0503\u0505\u0005s\u0000\u0000\u0504"+
		"\u0503\u0001\u0000\u0000\u0000\u0504\u0505\u0001\u0000\u0000\u0000\u0505"+
		"\u0507\u0001\u0000\u0000\u0000\u0506\u04ff\u0001\u0000\u0000\u0000\u0506"+
		"\u0502\u0001\u0000\u0000\u0000\u0507\u0099\u0001\u0000\u0000\u0000\u0508"+
		"\u050a\u0006M\uffff\uffff\u0000\u0509\u050b\u0003\u008eG\u0000\u050a\u0509"+
		"\u0001\u0000\u0000\u0000\u050b\u050c\u0001\u0000\u0000\u0000\u050c\u050a"+
		"\u0001\u0000\u0000\u0000\u050c\u050d\u0001\u0000\u0000\u0000\u050d\u050e"+
		"\u0001\u0000\u0000\u0000\u050e\u050f\u0003\u009aM(\u050f\u0559\u0001\u0000"+
		"\u0000\u0000\u0510\u0559\u0003\u00a4R\u0000\u0511\u0559\u0003\u00a6S\u0000"+
		"\u0512\u0514\u0007\u0002\u0000\u0000\u0513\u0515\u0005\u0014\u0000\u0000"+
		"\u0514\u0513\u0001\u0000\u0000\u0000\u0514\u0515\u0001\u0000\u0000\u0000"+
		"\u0515\u0516\u0001\u0000\u0000\u0000\u0516\u0559\u0003\u009aM\u001e\u0517"+
		"\u0518\u0005R\u0000\u0000\u0518\u0559\u0003\u009aM\u001d\u0519\u051a\u0007"+
		"\u0003\u0000\u0000\u051a\u0559\u0003\u009aM\u001c\u051b\u051d\u0005o\u0000"+
		"\u0000\u051c\u051e\u0003\u009aM\u0000\u051d\u051c\u0001\u0000\u0000\u0000"+
		"\u051d\u051e\u0001\u0000\u0000\u0000\u051e\u0559\u0001\u0000\u0000\u0000"+
		"\u051f\u0520\u0005q\u0000\u0000\u0520\u0559\u0003\u009aM\u000f\u0521\u0523"+
		"\u0005\u0004\u0000\u0000\u0522\u0524\u0005O\u0000\u0000\u0523\u0522\u0001"+
		"\u0000\u0000\u0000\u0523\u0524\u0001\u0000\u0000\u0000\u0524\u0526\u0001"+
		"\u0000\u0000\u0000\u0525\u0527\u0003\u009aM\u0000\u0526\u0525\u0001\u0000"+
		"\u0000\u0000\u0526\u0527\u0001\u0000\u0000\u0000\u0527\u0559\u0001\u0000"+
		"\u0000\u0000\u0528\u052a\u0005\u0002\u0000\u0000\u0529\u052b\u0005O\u0000"+
		"\u0000\u052a\u0529\u0001\u0000\u0000\u0000\u052a\u052b\u0001\u0000\u0000"+
		"\u0000\u052b\u052d\u0001\u0000\u0000\u0000\u052c\u052e\u0003\u009aM\u0000"+
		"\u052d\u052c\u0001\u0000\u0000\u0000\u052d\u052e\u0001\u0000\u0000\u0000"+
		"\u052e\u0559\u0001\u0000\u0000\u0000\u052f\u0531\u0005\u0017\u0000\u0000"+
		"\u0530\u0532\u0003\u009aM\u0000\u0531\u0530\u0001\u0000\u0000\u0000\u0531"+
		"\u0532\u0001\u0000\u0000\u0000\u0532\u0559\u0001\u0000\u0000\u0000\u0533"+
		"\u0537\u0005\u007f\u0000\u0000\u0534\u0536\u0003\u008cF\u0000\u0535\u0534"+
		"\u0001\u0000\u0000\u0000\u0536\u0539\u0001\u0000\u0000\u0000\u0537\u0535"+
		"\u0001\u0000\u0000\u0000\u0537\u0538\u0001\u0000\u0000\u0000\u0538\u053a"+
		"\u0001\u0000\u0000\u0000\u0539\u0537\u0001\u0000\u0000\u0000\u053a\u053b"+
		"\u0003\u009aM\u0000\u053b\u053c\u0005\u0080\u0000\u0000\u053c\u0559\u0001"+
		"\u0000\u0000\u0000\u053d\u0541\u0005}\u0000\u0000\u053e\u0540\u0003\u008c"+
		"F\u0000\u053f\u053e\u0001\u0000\u0000\u0000\u0540\u0543\u0001\u0000\u0000"+
		"\u0000\u0541\u053f\u0001\u0000\u0000\u0000\u0541\u0542\u0001\u0000\u0000"+
		"\u0000\u0542\u0545\u0001\u0000\u0000\u0000\u0543\u0541\u0001\u0000\u0000"+
		"\u0000\u0544\u0546\u0003\u00b0X\u0000\u0545\u0544\u0001\u0000\u0000\u0000"+
		"\u0545\u0546\u0001\u0000\u0000\u0000\u0546\u0547\u0001\u0000\u0000\u0000"+
		"\u0547\u0559\u0005~\u0000\u0000\u0548\u054c\u0005\u007f\u0000\u0000\u0549"+
		"\u054b\u0003\u008cF\u0000\u054a\u0549\u0001\u0000\u0000\u0000\u054b\u054e"+
		"\u0001\u0000\u0000\u0000\u054c\u054a\u0001\u0000\u0000\u0000\u054c\u054d"+
		"\u0001\u0000\u0000\u0000\u054d\u0550\u0001\u0000\u0000\u0000\u054e\u054c"+
		"\u0001\u0000\u0000\u0000\u054f\u0551\u0003\u00b2Y\u0000\u0550\u054f\u0001"+
		"\u0000\u0000\u0000\u0550\u0551\u0001\u0000\u0000\u0000\u0551\u0552\u0001"+
		"\u0000\u0000\u0000\u0552\u0559\u0005\u0080\u0000\u0000\u0553\u0559\u0003"+
		"\u00b6[\u0000\u0554\u0559\u0003\u00c4b\u0000\u0555\u0559\u0003\u00d2i"+
		"\u0000\u0556\u0559\u0003\u00a2Q\u0000\u0557\u0559\u0003\u0002\u0001\u0000"+
		"\u0558\u0508\u0001\u0000\u0000\u0000\u0558\u0510\u0001\u0000\u0000\u0000"+
		"\u0558\u0511\u0001\u0000\u0000\u0000\u0558\u0512\u0001\u0000\u0000\u0000"+
		"\u0558\u0517\u0001\u0000\u0000\u0000\u0558\u0519\u0001\u0000\u0000\u0000"+
		"\u0558\u051b\u0001\u0000\u0000\u0000\u0558\u051f\u0001\u0000\u0000\u0000"+
		"\u0558\u0521\u0001\u0000\u0000\u0000\u0558\u0528\u0001\u0000\u0000\u0000"+
		"\u0558\u052f\u0001\u0000\u0000\u0000\u0558\u0533\u0001\u0000\u0000\u0000"+
		"\u0558\u053d\u0001\u0000\u0000\u0000\u0558\u0548\u0001\u0000\u0000\u0000"+
		"\u0558\u0553\u0001\u0000\u0000\u0000\u0558\u0554\u0001\u0000\u0000\u0000"+
		"\u0558\u0555\u0001\u0000\u0000\u0000\u0558\u0556\u0001\u0000\u0000\u0000"+
		"\u0558\u0557\u0001\u0000\u0000\u0000\u0559\u05aa\u0001\u0000\u0000\u0000"+
		"\u055a\u055b\n\u001a\u0000\u0000\u055b\u055c\u0007\u0004\u0000\u0000\u055c"+
		"\u05a9\u0003\u009aM\u001b\u055d\u055e\n\u0019\u0000\u0000\u055e\u055f"+
		"\u0007\u0005\u0000\u0000\u055f\u05a9\u0003\u009aM\u001a\u0560\u0561\n"+
		"\u0018\u0000\u0000\u0561\u0562\u0005W\u0000\u0000\u0562\u05a9\u0003\u009a"+
		"M\u0019\u0563\u0564\n\u0017\u0000\u0000\u0564\u0565\u0005U\u0000\u0000"+
		"\u0565\u05a9\u0003\u009aM\u0018\u0566\u0567\n\u0016\u0000\u0000\u0567"+
		"\u0568\u0005X\u0000\u0000\u0568\u05a9\u0003\u009aM\u0017\u0569\u056a\n"+
		"\u0015\u0000\u0000\u056a\u056b\u0003\u009cN\u0000\u056b\u056c\u0003\u009a"+
		"M\u0016\u056c\u05a9\u0001\u0000\u0000\u0000\u056d\u056e\n\u0014\u0000"+
		"\u0000\u056e\u056f\u0003\u009eO\u0000\u056f\u0570\u0003\u009aM\u0015\u0570"+
		"\u05a9\u0001\u0000\u0000\u0000\u0571\u0572\n\u0013\u0000\u0000\u0572\u0573"+
		"\u0005Y\u0000\u0000\u0573\u05a9\u0003\u009aM\u0014\u0574\u0575\n\u0012"+
		"\u0000\u0000\u0575\u0576\u0005Z\u0000\u0000\u0576\u05a9\u0003\u009aM\u0013"+
		"\u0577\u0578\n\u000e\u0000\u0000\u0578\u0579\u0005q\u0000\u0000\u0579"+
		"\u05a9\u0003\u009aM\u000f\u057a\u057b\n\r\u0000\u0000\u057b\u057c\u0005"+
		"e\u0000\u0000\u057c\u05a9\u0003\u009aM\u000e\u057d\u057e\n\f\u0000\u0000"+
		"\u057e\u057f\u0003\u00a0P\u0000\u057f\u0580\u0003\u009aM\r\u0580\u05a9"+
		"\u0001\u0000\u0000\u0000\u0581\u0582\n%\u0000\u0000\u0582\u0583\u0005"+
		"n\u0000\u0000\u0583\u0584\u0003\u015a\u00ad\u0000\u0584\u0586\u0005\u007f"+
		"\u0000\u0000\u0585\u0587\u0003\u00d0h\u0000\u0586\u0585\u0001\u0000\u0000"+
		"\u0000\u0586\u0587\u0001\u0000\u0000\u0000\u0587\u0588\u0001\u0000\u0000"+
		"\u0000\u0588\u0589\u0005\u0080\u0000\u0000\u0589\u05a9\u0001\u0000\u0000"+
		"\u0000\u058a\u058b\n$\u0000\u0000\u058b\u058c\u0005n\u0000\u0000\u058c"+
		"\u05a9\u0003\u017c\u00be\u0000\u058d\u058e\n#\u0000\u0000\u058e\u058f"+
		"\u0005n\u0000\u0000\u058f\u05a9\u0003\u00b4Z\u0000\u0590\u0591\n\"\u0000"+
		"\u0000\u0591\u0592\u0005n\u0000\u0000\u0592\u05a9\u0005%\u0000\u0000\u0593"+
		"\u0594\n!\u0000\u0000\u0594\u0596\u0005\u007f\u0000\u0000\u0595\u0597"+
		"\u0003\u00d0h\u0000\u0596\u0595\u0001\u0000\u0000\u0000\u0596\u0597\u0001"+
		"\u0000\u0000\u0000\u0597\u0598\u0001\u0000\u0000\u0000\u0598\u05a9\u0005"+
		"\u0080\u0000\u0000\u0599\u059a\n \u0000\u0000\u059a\u059b\u0005}\u0000"+
		"\u0000\u059b\u059c\u0003\u009aM\u0000\u059c\u059d\u0005~\u0000\u0000\u059d"+
		"\u05a9\u0001\u0000\u0000\u0000\u059e\u059f\n\u001f\u0000\u0000\u059f\u05a9"+
		"\u0005z\u0000\u0000\u05a0\u05a1\n\u001b\u0000\u0000\u05a1\u05a2\u0005"+
		"\u0001\u0000\u0000\u05a2\u05a9\u0003\u0122\u0091\u0000\u05a3\u05a4\n\u0011"+
		"\u0000\u0000\u05a4\u05a6\u0005o\u0000\u0000\u05a5\u05a7\u0003\u009aM\u0000"+
		"\u05a6\u05a5\u0001\u0000\u0000\u0000\u05a6\u05a7\u0001\u0000\u0000\u0000"+
		"\u05a7\u05a9\u0001\u0000\u0000\u0000\u05a8\u055a\u0001\u0000\u0000\u0000"+
		"\u05a8\u055d\u0001\u0000\u0000\u0000\u05a8\u0560\u0001\u0000\u0000\u0000"+
		"\u05a8\u0563\u0001\u0000\u0000\u0000\u05a8\u0566\u0001\u0000\u0000\u0000"+
		"\u05a8\u0569\u0001\u0000\u0000\u0000\u05a8\u056d\u0001\u0000\u0000\u0000"+
		"\u05a8\u0571\u0001\u0000\u0000\u0000\u05a8\u0574\u0001\u0000\u0000\u0000"+
		"\u05a8\u0577\u0001\u0000\u0000\u0000\u05a8\u057a\u0001\u0000\u0000\u0000"+
		"\u05a8\u057d\u0001\u0000\u0000\u0000\u05a8\u0581\u0001\u0000\u0000\u0000"+
		"\u05a8\u058a\u0001\u0000\u0000\u0000\u05a8\u058d\u0001\u0000\u0000\u0000"+
		"\u05a8\u0590\u0001\u0000\u0000\u0000\u05a8\u0593\u0001\u0000\u0000\u0000"+
		"\u05a8\u0599\u0001\u0000\u0000\u0000\u05a8\u059e\u0001\u0000\u0000\u0000"+
		"\u05a8\u05a0\u0001\u0000\u0000\u0000\u05a8\u05a3\u0001\u0000\u0000\u0000"+
		"\u05a9\u05ac\u0001\u0000\u0000\u0000\u05aa\u05a8\u0001\u0000\u0000\u0000"+
		"\u05aa\u05ab\u0001\u0000\u0000\u0000\u05ab\u009b\u0001\u0000\u0000\u0000"+
		"\u05ac\u05aa\u0001\u0000\u0000\u0000\u05ad\u05ae\u0005h\u0000\u0000\u05ae"+
		"\u05b2\u0005h\u0000\u0000\u05af\u05b0\u0005i\u0000\u0000\u05b0\u05b2\u0005"+
		"i\u0000\u0000\u05b1\u05ad\u0001\u0000\u0000\u0000\u05b1\u05af\u0001\u0000"+
		"\u0000\u0000\u05b2\u009d\u0001\u0000\u0000\u0000\u05b3\u05b4\u0007\u0006"+
		"\u0000\u0000\u05b4\u009f\u0001\u0000\u0000\u0000\u05b5\u05b6\u0007\u0007"+
		"\u0000\u0000\u05b6\u00a1\u0001\u0000\u0000\u0000\u05b7\u05b9\u0003\u008e"+
		"G\u0000\u05b8\u05b7\u0001\u0000\u0000\u0000\u05b9\u05ba\u0001\u0000\u0000"+
		"\u0000\u05ba\u05b8\u0001\u0000\u0000\u0000\u05ba\u05bb\u0001\u0000\u0000"+
		"\u0000\u05bb\u05bc\u0001\u0000\u0000\u0000\u05bc\u05bd\u0003\u00a2Q\u0000"+
		"\u05bd\u05c6\u0001\u0000\u0000\u0000\u05be\u05c6\u0003\u00a8T\u0000\u05bf"+
		"\u05c6\u0003\u00acV\u0000\u05c0\u05c6\u0003\u00aeW\u0000\u05c1\u05c6\u0003"+
		"\u00d8l\u0000\u05c2\u05c6\u0003\u00e4r\u0000\u05c3\u05c6\u0003\u00e6s"+
		"\u0000\u05c4\u05c6\u0003\u00e8t\u0000\u05c5\u05b8\u0001\u0000\u0000\u0000"+
		"\u05c5\u05be\u0001\u0000\u0000\u0000\u05c5\u05bf\u0001\u0000\u0000\u0000"+
		"\u05c5\u05c0\u0001\u0000\u0000\u0000\u05c5\u05c1\u0001\u0000\u0000\u0000"+
		"\u05c5\u05c2\u0001\u0000\u0000\u0000\u05c5\u05c3\u0001\u0000\u0000\u0000"+
		"\u05c5\u05c4\u0001\u0000\u0000\u0000\u05c6\u00a3\u0001\u0000\u0000\u0000"+
		"\u05c7\u05c8\u0007\b\u0000\u0000\u05c8\u00a5\u0001\u0000\u0000\u0000\u05c9"+
		"\u05cc\u0003\u0158\u00ac\u0000\u05ca\u05cc\u0003\u016c\u00b6\u0000\u05cb"+
		"\u05c9\u0001\u0000\u0000\u0000\u05cb\u05ca\u0001\u0000\u0000\u0000\u05cc"+
		"\u00a7\u0001\u0000\u0000\u0000\u05cd\u05d1\u0005{\u0000\u0000\u05ce\u05d0"+
		"\u0003\u008cF\u0000\u05cf\u05ce\u0001\u0000\u0000\u0000\u05d0\u05d3\u0001"+
		"\u0000\u0000\u0000\u05d1\u05cf\u0001\u0000\u0000\u0000\u05d1\u05d2\u0001"+
		"\u0000\u0000\u0000\u05d2\u05d5\u0001\u0000\u0000\u0000\u05d3\u05d1\u0001"+
		"\u0000\u0000\u0000\u05d4\u05d6\u0003\u00aaU\u0000\u05d5\u05d4\u0001\u0000"+
		"\u0000\u0000\u05d5\u05d6\u0001\u0000\u0000\u0000\u05d6\u05d7\u0001\u0000"+
		"\u0000\u0000\u05d7\u05d8\u0005|\u0000\u0000\u05d8\u00a9\u0001\u0000\u0000"+
		"\u0000\u05d9\u05db\u0003\u0094J\u0000\u05da\u05d9\u0001\u0000\u0000\u0000"+
		"\u05db\u05dc\u0001\u0000\u0000\u0000\u05dc\u05da\u0001\u0000\u0000\u0000"+
		"\u05dc\u05dd\u0001\u0000\u0000\u0000\u05dd\u05df\u0001\u0000\u0000\u0000"+
		"\u05de\u05e0\u0003\u009aM\u0000\u05df\u05de\u0001\u0000\u0000\u0000\u05df"+
		"\u05e0\u0001\u0000\u0000\u0000\u05e0\u05e3\u0001\u0000\u0000\u0000\u05e1"+
		"\u05e3\u0003\u009aM\u0000\u05e2\u05da\u0001\u0000\u0000\u0000\u05e2\u05e1"+
		"\u0001\u0000\u0000\u0000\u05e3\u00ab\u0001\u0000\u0000\u0000\u05e4\u05e6"+
		"\u0005$\u0000\u0000\u05e5\u05e7\u0005\u0013\u0000\u0000\u05e6\u05e5\u0001"+
		"\u0000\u0000\u0000\u05e6\u05e7\u0001\u0000\u0000\u0000\u05e7\u05e8\u0001"+
		"\u0000\u0000\u0000\u05e8\u05e9\u0003\u00a8T\u0000\u05e9\u00ad\u0001\u0000"+
		"\u0000\u0000\u05ea\u05eb\u0005 \u0000\u0000\u05eb\u05ec\u0003\u00a8T\u0000"+
		"\u05ec\u00af\u0001\u0000\u0000\u0000\u05ed\u05f2\u0003\u009aM\u0000\u05ee"+
		"\u05ef\u0005r\u0000\u0000\u05ef\u05f1\u0003\u009aM\u0000\u05f0\u05ee\u0001"+
		"\u0000\u0000\u0000\u05f1\u05f4\u0001\u0000\u0000\u0000\u05f2\u05f0\u0001"+
		"\u0000\u0000\u0000\u05f2\u05f3\u0001\u0000\u0000\u0000\u05f3\u05f6\u0001"+
		"\u0000\u0000\u0000\u05f4\u05f2\u0001\u0000\u0000\u0000\u05f5\u05f7\u0005"+
		"r\u0000\u0000\u05f6\u05f5\u0001\u0000\u0000\u0000\u05f6\u05f7\u0001\u0000"+
		"\u0000\u0000\u05f7\u05fd\u0001\u0000\u0000\u0000\u05f8\u05f9\u0003\u009a"+
		"M\u0000\u05f9\u05fa\u0005s\u0000\u0000\u05fa\u05fb\u0003\u009aM\u0000"+
		"\u05fb\u05fd\u0001\u0000\u0000\u0000\u05fc\u05ed\u0001\u0000\u0000\u0000"+
		"\u05fc\u05f8\u0001\u0000\u0000\u0000\u05fd\u00b1\u0001\u0000\u0000\u0000"+
		"\u05fe\u05ff\u0003\u009aM\u0000\u05ff\u0600\u0005r\u0000\u0000\u0600\u0602"+
		"\u0001\u0000\u0000\u0000\u0601\u05fe\u0001\u0000\u0000\u0000\u0602\u0603"+
		"\u0001\u0000\u0000\u0000\u0603\u0601\u0001\u0000\u0000\u0000\u0603\u0604"+
		"\u0001\u0000\u0000\u0000\u0604\u0606\u0001\u0000\u0000\u0000\u0605\u0607"+
		"\u0003\u009aM\u0000\u0606\u0605\u0001\u0000\u0000\u0000\u0606\u0607\u0001"+
		"\u0000\u0000\u0000\u0607\u00b3\u0001\u0000\u0000\u0000\u0608\u0609\u0005"+
		"I\u0000\u0000\u0609\u00b5\u0001\u0000\u0000\u0000\u060a\u060e\u0003\u00b8"+
		"\\\u0000\u060b\u060e\u0003\u00c0`\u0000\u060c\u060e\u0003\u00c2a\u0000"+
		"\u060d\u060a\u0001\u0000\u0000\u0000\u060d\u060b\u0001\u0000\u0000\u0000"+
		"\u060d\u060c\u0001\u0000\u0000\u0000\u060e\u00b7\u0001\u0000\u0000\u0000"+
		"\u060f\u0610\u0003\u0158\u00ac\u0000\u0610\u0614\u0005{\u0000\u0000\u0611"+
		"\u0613\u0003\u008cF\u0000\u0612\u0611\u0001\u0000\u0000\u0000\u0613\u0616"+
		"\u0001\u0000\u0000\u0000\u0614\u0612\u0001\u0000\u0000\u0000\u0614\u0615"+
		"\u0001\u0000\u0000\u0000\u0615\u0619\u0001\u0000\u0000\u0000\u0616\u0614"+
		"\u0001\u0000\u0000\u0000\u0617\u061a\u0003\u00ba]\u0000\u0618\u061a\u0003"+
		"\u00be_\u0000\u0619\u0617\u0001\u0000\u0000\u0000\u0619\u0618\u0001\u0000"+
		"\u0000\u0000\u0619\u061a\u0001\u0000\u0000\u0000\u061a\u061b\u0001\u0000"+
		"\u0000\u0000\u061b\u061c\u0005|\u0000\u0000\u061c\u00b9\u0001\u0000\u0000"+
		"\u0000\u061d\u0622\u0003\u00bc^\u0000\u061e\u061f\u0005r\u0000\u0000\u061f"+
		"\u0621\u0003\u00bc^\u0000\u0620\u061e\u0001\u0000\u0000\u0000\u0621\u0624"+
		"\u0001\u0000\u0000\u0000\u0622\u0620\u0001\u0000\u0000\u0000\u0622\u0623"+
		"\u0001\u0000\u0000\u0000\u0623\u062a\u0001\u0000\u0000\u0000\u0624\u0622"+
		"\u0001\u0000\u0000\u0000\u0625\u0626\u0005r\u0000\u0000\u0626\u062b\u0003"+
		"\u00be_\u0000\u0627\u0629\u0005r\u0000\u0000\u0628\u0627\u0001\u0000\u0000"+
		"\u0000\u0628\u0629\u0001\u0000\u0000\u0000\u0629\u062b\u0001\u0000\u0000"+
		"\u0000\u062a\u0625\u0001\u0000\u0000\u0000\u062a\u0628\u0001\u0000\u0000"+
		"\u0000\u062b\u00bb\u0001\u0000\u0000\u0000\u062c\u062e\u0003\u008eG\u0000"+
		"\u062d\u062c\u0001\u0000\u0000\u0000\u062e\u0631\u0001\u0000\u0000\u0000"+
		"\u062f\u062d\u0001\u0000\u0000\u0000\u062f\u0630\u0001\u0000\u0000\u0000"+
		"\u0630\u063a\u0001\u0000\u0000\u0000\u0631\u062f\u0001\u0000\u0000\u0000"+
		"\u0632\u063b\u0003\u017c\u00be\u0000\u0633\u0636\u0003\u017c\u00be\u0000"+
		"\u0634\u0636\u0003\u00b4Z\u0000\u0635\u0633\u0001\u0000\u0000\u0000\u0635"+
		"\u0634\u0001\u0000\u0000\u0000\u0636\u0637\u0001\u0000\u0000\u0000\u0637"+
		"\u0638\u0005t\u0000\u0000\u0638\u0639\u0003\u009aM\u0000\u0639\u063b\u0001"+
		"\u0000\u0000\u0000\u063a\u0632\u0001\u0000\u0000\u0000\u063a\u0635\u0001"+
		"\u0000\u0000\u0000\u063b\u00bd\u0001\u0000\u0000\u0000\u063c\u063d\u0005"+
		"o\u0000\u0000\u063d\u063e\u0003\u009aM\u0000\u063e\u00bf\u0001\u0000\u0000"+
		"\u0000\u063f\u0640\u0003\u0158\u00ac\u0000\u0640\u0644\u0005\u007f\u0000"+
		"\u0000\u0641\u0643\u0003\u008cF\u0000\u0642\u0641\u0001\u0000\u0000\u0000"+
		"\u0643\u0646\u0001\u0000\u0000\u0000\u0644\u0642\u0001\u0000\u0000\u0000"+
		"\u0644\u0645\u0001\u0000\u0000\u0000\u0645\u0652\u0001\u0000\u0000\u0000"+
		"\u0646\u0644\u0001\u0000\u0000\u0000\u0647\u064c\u0003\u009aM\u0000\u0648"+
		"\u0649\u0005r\u0000\u0000\u0649\u064b\u0003\u009aM\u0000\u064a\u0648\u0001"+
		"\u0000\u0000\u0000\u064b\u064e\u0001\u0000\u0000\u0000\u064c\u064a\u0001"+
		"\u0000\u0000\u0000\u064c\u064d\u0001\u0000\u0000\u0000\u064d\u0650\u0001"+
		"\u0000\u0000\u0000\u064e\u064c\u0001\u0000\u0000\u0000\u064f\u0651\u0005"+
		"r\u0000\u0000\u0650\u064f\u0001\u0000\u0000\u0000\u0650\u0651\u0001\u0000"+
		"\u0000\u0000\u0651\u0653\u0001\u0000\u0000\u0000\u0652\u0647\u0001\u0000"+
		"\u0000\u0000\u0652\u0653\u0001\u0000\u0000\u0000\u0653\u0654\u0001\u0000"+
		"\u0000\u0000\u0654\u0655\u0005\u0080\u0000\u0000\u0655\u00c1\u0001\u0000"+
		"\u0000\u0000\u0656\u0657\u0003\u0158\u00ac\u0000\u0657\u00c3\u0001\u0000"+
		"\u0000\u0000\u0658\u065c\u0003\u00c6c\u0000\u0659\u065c\u0003\u00ccf\u0000"+
		"\u065a\u065c\u0003\u00ceg\u0000\u065b\u0658\u0001\u0000\u0000\u0000\u065b"+
		"\u0659\u0001\u0000\u0000\u0000\u065b\u065a\u0001\u0000\u0000\u0000\u065c"+
		"\u00c5\u0001\u0000\u0000\u0000\u065d\u065e\u0003\u0158\u00ac\u0000\u065e"+
		"\u0660\u0005{\u0000\u0000\u065f\u0661\u0003\u00c8d\u0000\u0660\u065f\u0001"+
		"\u0000\u0000\u0000\u0660\u0661\u0001\u0000\u0000\u0000\u0661\u0662\u0001"+
		"\u0000\u0000\u0000\u0662\u0663\u0005|\u0000\u0000\u0663\u00c7\u0001\u0000"+
		"\u0000\u0000\u0664\u0669\u0003\u00cae\u0000\u0665\u0666\u0005r\u0000\u0000"+
		"\u0666\u0668\u0003\u00cae\u0000\u0667\u0665\u0001\u0000\u0000\u0000\u0668"+
		"\u066b\u0001\u0000\u0000\u0000\u0669\u0667\u0001\u0000\u0000\u0000\u0669"+
		"\u066a\u0001\u0000\u0000\u0000\u066a\u066d\u0001\u0000\u0000\u0000\u066b"+
		"\u0669\u0001\u0000\u0000\u0000\u066c\u066e\u0005r\u0000\u0000\u066d\u066c"+
		"\u0001\u0000\u0000\u0000\u066d\u066e\u0001\u0000\u0000\u0000\u066e\u00c9"+
		"\u0001\u0000\u0000\u0000\u066f\u0678\u0003\u017c\u00be\u0000\u0670\u0673"+
		"\u0003\u017c\u00be\u0000\u0671\u0673\u0003\u00b4Z\u0000\u0672\u0670\u0001"+
		"\u0000\u0000\u0000\u0672\u0671\u0001\u0000\u0000\u0000\u0673\u0674\u0001"+
		"\u0000\u0000\u0000\u0674\u0675\u0005t\u0000\u0000\u0675\u0676\u0003\u009a"+
		"M\u0000\u0676\u0678\u0001\u0000\u0000\u0000\u0677\u066f\u0001\u0000\u0000"+
		"\u0000\u0677\u0672\u0001\u0000\u0000\u0000\u0678\u00cb\u0001\u0000\u0000"+
		"\u0000\u0679\u067a\u0003\u0158\u00ac\u0000\u067a\u0686\u0005\u007f\u0000"+
		"\u0000\u067b\u0680\u0003\u009aM\u0000\u067c\u067d\u0005r\u0000\u0000\u067d"+
		"\u067f\u0003\u009aM\u0000\u067e\u067c\u0001\u0000\u0000\u0000\u067f\u0682"+
		"\u0001\u0000\u0000\u0000\u0680\u067e\u0001\u0000\u0000\u0000\u0680\u0681"+
		"\u0001\u0000\u0000\u0000\u0681\u0684\u0001\u0000\u0000\u0000\u0682\u0680"+
		"\u0001\u0000\u0000\u0000\u0683\u0685\u0005r\u0000\u0000\u0684\u0683\u0001"+
		"\u0000\u0000\u0000\u0684\u0685\u0001\u0000\u0000\u0000\u0685\u0687\u0001"+
		"\u0000\u0000\u0000\u0686\u067b\u0001\u0000\u0000\u0000\u0686\u0687\u0001"+
		"\u0000\u0000\u0000\u0687\u0688\u0001\u0000\u0000\u0000\u0688\u0689\u0005"+
		"\u0080\u0000\u0000\u0689\u00cd\u0001\u0000\u0000\u0000\u068a\u068b\u0003"+
		"\u0158\u00ac\u0000\u068b\u00cf\u0001\u0000\u0000\u0000\u068c\u0691\u0003"+
		"\u009aM\u0000\u068d\u068e\u0005r\u0000\u0000\u068e\u0690\u0003\u009aM"+
		"\u0000\u068f\u068d\u0001\u0000\u0000\u0000\u0690\u0693\u0001\u0000\u0000"+
		"\u0000\u0691\u068f\u0001\u0000\u0000\u0000\u0691\u0692\u0001\u0000\u0000"+
		"\u0000\u0692\u0695\u0001\u0000\u0000\u0000\u0693\u0691\u0001\u0000\u0000"+
		"\u0000\u0694\u0696\u0005r\u0000\u0000\u0695\u0694\u0001\u0000\u0000\u0000"+
		"\u0695\u0696\u0001\u0000\u0000\u0000\u0696\u00d1\u0001\u0000\u0000\u0000"+
		"\u0697\u0699\u0005\u0013\u0000\u0000\u0698\u0697\u0001\u0000\u0000\u0000"+
		"\u0698\u0699\u0001\u0000\u0000\u0000\u0699\u06a0\u0001\u0000\u0000\u0000"+
		"\u069a\u06a1\u0005Z\u0000\u0000\u069b\u069d\u0005X\u0000\u0000\u069c\u069e"+
		"\u0003\u00d4j\u0000\u069d\u069c\u0001\u0000\u0000\u0000\u069d\u069e\u0001"+
		"\u0000\u0000\u0000\u069e\u069f\u0001\u0000\u0000\u0000\u069f\u06a1\u0005"+
		"X\u0000\u0000\u06a0\u069a\u0001\u0000\u0000\u0000\u06a0\u069b\u0001\u0000"+
		"\u0000\u0000\u06a1\u06a7\u0001\u0000\u0000\u0000\u06a2\u06a8\u0003\u009a"+
		"M\u0000\u06a3\u06a4\u0005v\u0000\u0000\u06a4\u06a5\u0003\u0122\u0091\u0000"+
		"\u06a5\u06a6\u0003\u00a8T\u0000\u06a6\u06a8\u0001\u0000\u0000\u0000\u06a7"+
		"\u06a2\u0001\u0000\u0000\u0000\u06a7\u06a3\u0001\u0000\u0000\u0000\u06a8"+
		"\u00d3\u0001\u0000\u0000\u0000\u06a9\u06ae\u0003\u00d6k\u0000\u06aa\u06ab"+
		"\u0005r\u0000\u0000\u06ab\u06ad\u0003\u00d6k\u0000\u06ac\u06aa\u0001\u0000"+
		"\u0000\u0000\u06ad\u06b0\u0001\u0000\u0000\u0000\u06ae\u06ac\u0001\u0000"+
		"\u0000\u0000\u06ae\u06af\u0001\u0000\u0000\u0000\u06af\u06b2\u0001\u0000"+
		"\u0000\u0000\u06b0\u06ae\u0001\u0000\u0000\u0000\u06b1\u06b3\u0005r\u0000"+
		"\u0000\u06b2\u06b1\u0001\u0000\u0000\u0000\u06b2\u06b3\u0001\u0000\u0000"+
		"\u0000\u06b3\u00d5\u0001\u0000\u0000\u0000\u06b4\u06b6\u0003\u008eG\u0000"+
		"\u06b5\u06b4\u0001\u0000\u0000\u0000\u06b6\u06b9\u0001\u0000\u0000\u0000"+
		"\u06b7\u06b5\u0001\u0000\u0000\u0000\u06b7\u06b8\u0001\u0000\u0000\u0000"+
		"\u06b8\u06ba\u0001\u0000\u0000\u0000\u06b9\u06b7\u0001\u0000\u0000\u0000"+
		"\u06ba\u06bd\u0003\u00f2y\u0000\u06bb\u06bc\u0005t\u0000\u0000\u06bc\u06be"+
		"\u0003\u0120\u0090\u0000\u06bd\u06bb\u0001\u0000\u0000\u0000\u06bd\u06be"+
		"\u0001\u0000\u0000\u0000\u06be\u00d7\u0001\u0000\u0000\u0000\u06bf\u06c1"+
		"\u0003\u00e2q\u0000\u06c0\u06bf\u0001\u0000\u0000\u0000\u06c0\u06c1\u0001"+
		"\u0000\u0000\u0000\u06c1\u06c6\u0001\u0000\u0000\u0000\u06c2\u06c7\u0003"+
		"\u00dam\u0000\u06c3\u06c7\u0003\u00dcn\u0000\u06c4\u06c7\u0003\u00deo"+
		"\u0000\u06c5\u06c7\u0003\u00e0p\u0000\u06c6\u06c2\u0001\u0000\u0000\u0000"+
		"\u06c6\u06c3\u0001\u0000\u0000\u0000\u06c6\u06c4\u0001\u0000\u0000\u0000"+
		"\u06c6\u06c5\u0001\u0000\u0000\u0000\u06c7\u00d9\u0001\u0000\u0000\u0000"+
		"\u06c8\u06c9\u0005\u0010\u0000\u0000\u06c9\u06ca\u0003\u00a8T\u0000\u06ca"+
		"\u00db\u0001\u0000\u0000\u0000\u06cb\u06cc\u0005#\u0000\u0000\u06cc\u06cd"+
		"\u0003\u009aM\u0000\u06cd\u06ce\u0003\u00a8T\u0000\u06ce\u00dd\u0001\u0000"+
		"\u0000\u0000\u06cf\u06d0\u0005#\u0000\u0000\u06d0\u06d1\u0005\u000f\u0000"+
		"\u0000\u06d1\u06d2\u0003\u00f2y\u0000\u06d2\u06d3\u0005e\u0000\u0000\u06d3"+
		"\u06d4\u0003\u009aM\u0000\u06d4\u06d5\u0003\u00a8T\u0000\u06d5\u00df\u0001"+
		"\u0000\u0000\u0000\u06d6\u06d7\u0005\u000b\u0000\u0000\u06d7\u06d8\u0003"+
		"\u00f2y\u0000\u06d8\u06d9\u0005\u000e\u0000\u0000\u06d9\u06da\u0003\u009a"+
		"M\u0000\u06da\u06db\u0003\u00a8T\u0000\u06db\u00e1\u0001\u0000\u0000\u0000"+
		"\u06dc\u06dd\u0005O\u0000\u0000\u06dd\u06de\u0005t\u0000\u0000\u06de\u00e3"+
		"\u0001\u0000\u0000\u0000\u06df\u06e0\u0005\f\u0000\u0000\u06e0\u06e1\u0003"+
		"\u009aM\u0000\u06e1\u06e8\u0003\u00a8T\u0000\u06e2\u06e6\u0005\u0006\u0000"+
		"\u0000\u06e3\u06e7\u0003\u00a8T\u0000\u06e4\u06e7\u0003\u00e4r\u0000\u06e5"+
		"\u06e7\u0003\u00e6s\u0000\u06e6\u06e3\u0001\u0000\u0000\u0000\u06e6\u06e4"+
		"\u0001\u0000\u0000\u0000\u06e6\u06e5\u0001\u0000\u0000\u0000\u06e7\u06e9"+
		"\u0001\u0000\u0000\u0000\u06e8\u06e2\u0001\u0000\u0000\u0000\u06e8\u06e9"+
		"\u0001\u0000\u0000\u0000\u06e9\u00e5\u0001\u0000\u0000\u0000\u06ea\u06eb"+
		"\u0005\f\u0000\u0000\u06eb\u06ec\u0005\u000f\u0000\u0000\u06ec\u06ed\u0003"+
		"\u00f2y\u0000\u06ed\u06ee\u0005e\u0000\u0000\u06ee\u06ef\u0003\u009aM"+
		"\u0000\u06ef\u06f6\u0003\u00a8T\u0000\u06f0\u06f4\u0005\u0006\u0000\u0000"+
		"\u06f1\u06f5\u0003\u00a8T\u0000\u06f2\u06f5\u0003\u00e4r\u0000\u06f3\u06f5"+
		"\u0003\u00e6s\u0000\u06f4\u06f1\u0001\u0000\u0000\u0000\u06f4\u06f2\u0001"+
		"\u0000\u0000\u0000\u06f4\u06f3\u0001\u0000\u0000\u0000\u06f5\u06f7\u0001"+
		"\u0000\u0000\u0000\u06f6\u06f0\u0001\u0000\u0000\u0000\u06f6\u06f7\u0001"+
		"\u0000\u0000\u0000\u06f7\u00e7\u0001\u0000\u0000\u0000\u06f8\u06f9\u0005"+
		"\u0011\u0000\u0000\u06f9\u06fa\u0003\u009aM\u0000\u06fa\u06fe\u0005{\u0000"+
		"\u0000\u06fb\u06fd\u0003\u008cF\u0000\u06fc\u06fb\u0001\u0000\u0000\u0000"+
		"\u06fd\u0700\u0001\u0000\u0000\u0000\u06fe\u06fc\u0001\u0000\u0000\u0000"+
		"\u06fe\u06ff\u0001\u0000\u0000\u0000\u06ff\u0702\u0001\u0000\u0000\u0000"+
		"\u0700\u06fe\u0001\u0000\u0000\u0000\u0701\u0703\u0003\u00eau\u0000\u0702"+
		"\u0701\u0001\u0000\u0000\u0000\u0702\u0703\u0001\u0000\u0000\u0000\u0703"+
		"\u0704\u0001\u0000\u0000\u0000\u0704\u0705\u0005|\u0000\u0000\u0705\u00e9"+
		"\u0001\u0000\u0000\u0000\u0706\u0707\u0003\u00eew\u0000\u0707\u0708\u0005"+
		"w\u0000\u0000\u0708\u0709\u0003\u00ecv\u0000\u0709\u070b\u0001\u0000\u0000"+
		"\u0000\u070a\u0706\u0001\u0000\u0000\u0000\u070b\u070e\u0001\u0000\u0000"+
		"\u0000\u070c\u070a\u0001\u0000\u0000\u0000\u070c\u070d\u0001\u0000\u0000"+
		"\u0000\u070d\u070f\u0001\u0000\u0000\u0000\u070e\u070c\u0001\u0000\u0000"+
		"\u0000\u070f\u0710\u0003\u00eew\u0000\u0710\u0711\u0005w\u0000\u0000\u0711"+
		"\u0713\u0003\u009aM\u0000\u0712\u0714\u0005r\u0000\u0000\u0713\u0712\u0001"+
		"\u0000\u0000\u0000\u0713\u0714\u0001\u0000\u0000\u0000\u0714\u00eb\u0001"+
		"\u0000\u0000\u0000\u0715\u0716\u0003\u009aM\u0000\u0716\u0717\u0005r\u0000"+
		"\u0000\u0717\u071d\u0001\u0000\u0000\u0000\u0718\u071a\u0003\u00a2Q\u0000"+
		"\u0719\u071b\u0005r\u0000\u0000\u071a\u0719\u0001\u0000\u0000\u0000\u071a"+
		"\u071b\u0001\u0000\u0000\u0000\u071b\u071d\u0001\u0000\u0000\u0000\u071c"+
		"\u0715\u0001\u0000\u0000\u0000\u071c\u0718\u0001\u0000\u0000\u0000\u071d"+
		"\u00ed\u0001\u0000\u0000\u0000\u071e\u0720\u0003\u008eG\u0000\u071f\u071e"+
		"\u0001\u0000\u0000\u0000\u0720\u0723\u0001\u0000\u0000\u0000\u0721\u071f"+
		"\u0001\u0000\u0000\u0000\u0721\u0722\u0001\u0000\u0000\u0000\u0722\u0724"+
		"\u0001\u0000\u0000\u0000\u0723\u0721\u0001\u0000\u0000\u0000\u0724\u0726"+
		"\u0003\u00f2y\u0000\u0725\u0727\u0003\u00f0x\u0000\u0726\u0725\u0001\u0000"+
		"\u0000\u0000\u0726\u0727\u0001\u0000\u0000\u0000\u0727\u00ef\u0001\u0000"+
		"\u0000\u0000\u0728\u0729\u0005\f\u0000\u0000\u0729\u072a\u0003\u009aM"+
		"\u0000\u072a\u00f1\u0001\u0000\u0000\u0000\u072b\u072d\u0005X\u0000\u0000"+
		"\u072c\u072b\u0001\u0000\u0000\u0000\u072c\u072d\u0001\u0000\u0000\u0000"+
		"\u072d\u072e\u0001\u0000\u0000\u0000\u072e\u0733\u0003\u00f4z\u0000\u072f"+
		"\u0730\u0005X\u0000\u0000\u0730\u0732\u0003\u00f4z\u0000\u0731\u072f\u0001"+
		"\u0000\u0000\u0000\u0732\u0735\u0001\u0000\u0000\u0000\u0733\u0731\u0001"+
		"\u0000\u0000\u0000\u0733\u0734\u0001\u0000\u0000\u0000\u0734\u00f3\u0001"+
		"\u0000\u0000\u0000\u0735\u0733\u0001\u0000\u0000\u0000\u0736\u0739\u0003"+
		"\u00f6{\u0000\u0737\u0739\u0003\u0100\u0080\u0000\u0738\u0736\u0001\u0000"+
		"\u0000\u0000\u0738\u0737\u0001\u0000\u0000\u0000\u0739\u00f5\u0001\u0000"+
		"\u0000\u0000\u073a\u0747\u0003\u00f8|\u0000\u073b\u0747\u0003\u00fa}\u0000"+
		"\u073c\u0747\u0003\u00fc~\u0000\u073d\u0747\u0003\u00fe\u007f\u0000\u073e"+
		"\u0747\u0003\u0104\u0082\u0000\u073f\u0747\u0003\u0106\u0083\u0000\u0740"+
		"\u0747\u0003\u0110\u0088\u0000\u0741\u0747\u0003\u0114\u008a\u0000\u0742"+
		"\u0747\u0003\u0118\u008c\u0000\u0743\u0747\u0003\u011a\u008d\u0000\u0744"+
		"\u0747\u0003\u011e\u008f\u0000\u0745\u0747\u0003\u0002\u0001\u0000\u0746"+
		"\u073a\u0001\u0000\u0000\u0000\u0746\u073b\u0001\u0000\u0000\u0000\u0746"+
		"\u073c\u0001\u0000\u0000\u0000\u0746\u073d\u0001\u0000\u0000\u0000\u0746"+
		"\u073e\u0001\u0000\u0000\u0000\u0746\u073f\u0001\u0000\u0000\u0000\u0746"+
		"\u0740\u0001\u0000\u0000\u0000\u0746\u0741\u0001\u0000\u0000\u0000\u0746"+
		"\u0742\u0001\u0000\u0000\u0000\u0746\u0743\u0001\u0000\u0000\u0000\u0746"+
		"\u0744\u0001\u0000\u0000\u0000\u0746\u0745\u0001\u0000\u0000\u0000\u0747"+
		"\u00f7\u0001\u0000\u0000\u0000\u0748\u0759\u0005\u001e\u0000\u0000\u0749"+
		"\u0759\u0005\t\u0000\u0000\u074a\u0759\u0005C\u0000\u0000\u074b\u0759"+
		"\u0005F\u0000\u0000\u074c\u0759\u0005D\u0000\u0000\u074d\u0759\u0005E"+
		"\u0000\u0000\u074e\u0759\u0005G\u0000\u0000\u074f\u0759\u0005H\u0000\u0000"+
		"\u0750\u0752\u0005Q\u0000\u0000\u0751\u0750\u0001\u0000\u0000\u0000\u0751"+
		"\u0752\u0001\u0000\u0000\u0000\u0752\u0753\u0001\u0000\u0000\u0000\u0753"+
		"\u0759\u0005I\u0000\u0000\u0754\u0756\u0005Q\u0000\u0000\u0755\u0754\u0001"+
		"\u0000\u0000\u0000\u0755\u0756\u0001\u0000\u0000\u0000\u0756\u0757\u0001"+
		"\u0000\u0000\u0000\u0757\u0759\u0005N\u0000\u0000\u0758\u0748\u0001\u0000"+
		"\u0000\u0000\u0758\u0749\u0001\u0000\u0000\u0000\u0758\u074a\u0001\u0000"+
		"\u0000\u0000\u0758\u074b\u0001\u0000\u0000\u0000\u0758\u074c\u0001\u0000"+
		"\u0000\u0000\u0758\u074d\u0001\u0000\u0000\u0000\u0758\u074e\u0001\u0000"+
		"\u0000\u0000\u0758\u074f\u0001\u0000\u0000\u0000\u0758\u0751\u0001\u0000"+
		"\u0000\u0000\u0758\u0755\u0001\u0000\u0000\u0000\u0759\u00f9\u0001\u0000"+
		"\u0000\u0000\u075a\u075c\u0005\u0016\u0000\u0000\u075b\u075a\u0001\u0000"+
		"\u0000\u0000\u075b\u075c\u0001\u0000\u0000\u0000\u075c\u075e\u0001\u0000"+
		"\u0000\u0000\u075d\u075f\u0005\u0014\u0000\u0000\u075e\u075d\u0001\u0000"+
		"\u0000\u0000\u075e\u075f\u0001\u0000\u0000\u0000\u075f\u0760\u0001\u0000"+
		"\u0000\u0000\u0760\u0763\u0003\u017c\u00be\u0000\u0761\u0762\u0005l\u0000"+
		"\u0000\u0762\u0764\u0003\u00f2y\u0000\u0763\u0761\u0001\u0000\u0000\u0000"+
		"\u0763\u0764\u0001\u0000\u0000\u0000\u0764\u00fb\u0001\u0000\u0000\u0000"+
		"\u0765\u0766\u0005m\u0000\u0000\u0766\u00fd\u0001\u0000\u0000\u0000\u0767"+
		"\u0768\u0005o\u0000\u0000\u0768\u00ff\u0001\u0000\u0000\u0000\u0769\u076a"+
		"\u0003\u0102\u0081\u0000\u076a\u076b\u0005q\u0000\u0000\u076b\u076c\u0003"+
		"\u0102\u0081\u0000\u076c\u0775\u0001\u0000\u0000\u0000\u076d\u076e\u0003"+
		"\u0102\u0081\u0000\u076e\u076f\u0005o\u0000\u0000\u076f\u0775\u0001\u0000"+
		"\u0000\u0000\u0770\u0771\u0003\u0102\u0081\u0000\u0771\u0772\u0005p\u0000"+
		"\u0000\u0772\u0773\u0003\u0102\u0081\u0000\u0773\u0775\u0001\u0000\u0000"+
		"\u0000\u0774\u0769\u0001\u0000\u0000\u0000\u0774\u076d\u0001\u0000\u0000"+
		"\u0000\u0774\u0770\u0001\u0000\u0000\u0000\u0775\u0101\u0001\u0000\u0000"+
		"\u0000\u0776\u0782\u0005C\u0000\u0000\u0777\u0782\u0005F\u0000\u0000\u0778"+
		"\u077a\u0005Q\u0000\u0000\u0779\u0778\u0001\u0000\u0000\u0000\u0779\u077a"+
		"\u0001\u0000\u0000\u0000\u077a\u077b\u0001\u0000\u0000\u0000\u077b\u0782"+
		"\u0005I\u0000\u0000\u077c\u077e\u0005Q\u0000\u0000\u077d\u077c\u0001\u0000"+
		"\u0000\u0000\u077d\u077e\u0001\u0000\u0000\u0000\u077e\u077f\u0001\u0000"+
		"\u0000\u0000\u077f\u0782\u0005N\u0000\u0000\u0780\u0782\u0003\u011e\u008f"+
		"\u0000\u0781\u0776\u0001\u0000\u0000\u0000\u0781\u0777\u0001\u0000\u0000"+
		"\u0000\u0781\u0779\u0001\u0000\u0000\u0000\u0781\u077d\u0001\u0000\u0000"+
		"\u0000\u0781\u0780\u0001\u0000\u0000\u0000\u0782\u0103\u0001\u0000\u0000"+
		"\u0000\u0783\u0785\u0007\u0002\u0000\u0000\u0784\u0786\u0005\u0014\u0000"+
		"\u0000\u0785\u0784\u0001\u0000\u0000\u0000\u0785\u0786\u0001\u0000\u0000"+
		"\u0000\u0786\u0787\u0001\u0000\u0000\u0000\u0787\u0788\u0003\u00f6{\u0000"+
		"\u0788\u0105\u0001\u0000\u0000\u0000\u0789\u078a\u0003\u0158\u00ac\u0000"+
		"\u078a\u078c\u0005{\u0000\u0000\u078b\u078d\u0003\u0108\u0084\u0000\u078c"+
		"\u078b\u0001\u0000\u0000\u0000\u078c\u078d\u0001\u0000\u0000\u0000\u078d"+
		"\u078e\u0001\u0000\u0000\u0000\u078e\u078f\u0005|\u0000\u0000\u078f\u0107"+
		"\u0001\u0000\u0000\u0000\u0790\u0795\u0003\u010a\u0085\u0000\u0791\u0793"+
		"\u0005r\u0000\u0000\u0792\u0794\u0003\u010e\u0087\u0000\u0793\u0792\u0001"+
		"\u0000\u0000\u0000\u0793\u0794\u0001\u0000\u0000\u0000\u0794\u0796\u0001"+
		"\u0000\u0000\u0000\u0795\u0791\u0001\u0000\u0000\u0000\u0795\u0796\u0001"+
		"\u0000\u0000\u0000\u0796\u0799\u0001\u0000\u0000\u0000\u0797\u0799\u0003"+
		"\u010e\u0087\u0000\u0798\u0790\u0001\u0000\u0000\u0000\u0798\u0797\u0001"+
		"\u0000\u0000\u0000\u0799\u0109\u0001\u0000\u0000\u0000\u079a\u079f\u0003"+
		"\u010c\u0086\u0000\u079b\u079c\u0005r\u0000\u0000\u079c\u079e\u0003\u010c"+
		"\u0086\u0000\u079d\u079b\u0001\u0000\u0000\u0000\u079e\u07a1\u0001\u0000"+
		"\u0000\u0000\u079f\u079d\u0001\u0000\u0000\u0000\u079f\u07a0\u0001\u0000"+
		"\u0000\u0000\u07a0\u010b\u0001\u0000\u0000\u0000\u07a1\u079f\u0001\u0000"+
		"\u0000\u0000\u07a2\u07a4\u0003\u008eG\u0000\u07a3\u07a2\u0001\u0000\u0000"+
		"\u0000\u07a4\u07a7\u0001\u0000\u0000\u0000\u07a5\u07a3\u0001\u0000\u0000"+
		"\u0000\u07a5\u07a6\u0001\u0000\u0000\u0000\u07a6\u07b7\u0001\u0000\u0000"+
		"\u0000\u07a7\u07a5\u0001\u0000\u0000\u0000\u07a8\u07a9\u0003\u00b4Z\u0000"+
		"\u07a9\u07aa\u0005t\u0000\u0000\u07aa\u07ab\u0003\u00f2y\u0000\u07ab\u07b8"+
		"\u0001\u0000\u0000\u0000\u07ac\u07ad\u0003\u017c\u00be\u0000\u07ad\u07ae"+
		"\u0005t\u0000\u0000\u07ae\u07af\u0003\u00f2y\u0000\u07af\u07b8\u0001\u0000"+
		"\u0000\u0000\u07b0\u07b2\u0005\u0016\u0000\u0000\u07b1\u07b0\u0001\u0000"+
		"\u0000\u0000\u07b1\u07b2\u0001\u0000\u0000\u0000\u07b2\u07b4\u0001\u0000"+
		"\u0000\u0000\u07b3\u07b5\u0005\u0014\u0000\u0000\u07b4\u07b3\u0001\u0000"+
		"\u0000\u0000\u07b4\u07b5\u0001\u0000\u0000\u0000\u07b5\u07b6\u0001\u0000"+
		"\u0000\u0000\u07b6\u07b8\u0003\u017c\u00be\u0000\u07b7\u07a8\u0001\u0000"+
		"\u0000\u0000\u07b7\u07ac\u0001\u0000\u0000\u0000\u07b7\u07b1\u0001\u0000"+
		"\u0000\u0000\u07b8\u010d\u0001\u0000\u0000\u0000\u07b9\u07bb\u0003\u008e"+
		"G\u0000\u07ba\u07b9\u0001\u0000\u0000\u0000\u07bb\u07be\u0001\u0000\u0000"+
		"\u0000\u07bc\u07ba\u0001\u0000\u0000\u0000\u07bc\u07bd\u0001\u0000\u0000"+
		"\u0000\u07bd\u07bf\u0001\u0000\u0000\u0000\u07be\u07bc\u0001\u0000\u0000"+
		"\u0000\u07bf\u07c0\u0005o\u0000\u0000\u07c0\u010f\u0001\u0000\u0000\u0000"+
		"\u07c1\u07c2\u0003\u0158\u00ac\u0000\u07c2\u07c4\u0005\u007f\u0000\u0000"+
		"\u07c3\u07c5\u0003\u0112\u0089\u0000\u07c4\u07c3\u0001\u0000\u0000\u0000"+
		"\u07c4\u07c5\u0001\u0000\u0000\u0000\u07c5\u07c6\u0001\u0000\u0000\u0000"+
		"\u07c6\u07c7\u0005\u0080\u0000\u0000\u07c7\u0111\u0001\u0000\u0000\u0000"+
		"\u07c8\u07cd\u0003\u00f2y\u0000\u07c9\u07ca\u0005r\u0000\u0000\u07ca\u07cc"+
		"\u0003\u00f2y\u0000\u07cb\u07c9\u0001\u0000\u0000\u0000\u07cc\u07cf\u0001"+
		"\u0000\u0000\u0000\u07cd\u07cb\u0001\u0000\u0000\u0000\u07cd\u07ce\u0001"+
		"\u0000\u0000\u0000\u07ce\u07d1\u0001\u0000\u0000\u0000\u07cf\u07cd\u0001"+
		"\u0000\u0000\u0000\u07d0\u07d2\u0005r\u0000\u0000\u07d1\u07d0\u0001\u0000"+
		"\u0000\u0000\u07d1\u07d2\u0001\u0000\u0000\u0000\u07d2\u0113\u0001\u0000"+
		"\u0000\u0000\u07d3\u07d5\u0005\u007f\u0000\u0000\u07d4\u07d6\u0003\u0116"+
		"\u008b\u0000\u07d5\u07d4\u0001\u0000\u0000\u0000\u07d5\u07d6\u0001\u0000"+
		"\u0000\u0000\u07d6\u07d7\u0001\u0000\u0000\u0000\u07d7\u07d8\u0005\u0080"+
		"\u0000\u0000\u07d8\u0115\u0001\u0000\u0000\u0000\u07d9\u07da\u0003\u00f2"+
		"y\u0000\u07da\u07db\u0005r\u0000\u0000\u07db\u07e8\u0001\u0000\u0000\u0000"+
		"\u07dc\u07e8\u0003\u00fe\u007f\u0000\u07dd\u07e0\u0003\u00f2y\u0000\u07de"+
		"\u07df\u0005r\u0000\u0000\u07df\u07e1\u0003\u00f2y\u0000\u07e0\u07de\u0001"+
		"\u0000\u0000\u0000\u07e1\u07e2\u0001\u0000\u0000\u0000\u07e2\u07e0\u0001"+
		"\u0000\u0000\u0000\u07e2\u07e3\u0001\u0000\u0000\u0000\u07e3\u07e5\u0001"+
		"\u0000\u0000\u0000\u07e4\u07e6\u0005r\u0000\u0000\u07e5\u07e4\u0001\u0000"+
		"\u0000\u0000\u07e5\u07e6\u0001\u0000\u0000\u0000\u07e6\u07e8\u0001\u0000"+
		"\u0000\u0000\u07e7\u07d9\u0001\u0000\u0000\u0000\u07e7\u07dc\u0001\u0000"+
		"\u0000\u0000\u07e7\u07dd\u0001\u0000\u0000\u0000\u07e8\u0117\u0001\u0000"+
		"\u0000\u0000\u07e9\u07ea\u0005\u007f\u0000\u0000\u07ea\u07eb\u0003\u00f2"+
		"y\u0000\u07eb\u07ec\u0005\u0080\u0000\u0000\u07ec\u0119\u0001\u0000\u0000"+
		"\u0000\u07ed\u07ef\u0005}\u0000\u0000\u07ee\u07f0\u0003\u011c\u008e\u0000"+
		"\u07ef\u07ee\u0001\u0000\u0000\u0000\u07ef\u07f0\u0001\u0000\u0000\u0000"+
		"\u07f0\u07f1\u0001\u0000\u0000\u0000\u07f1\u07f2\u0005~\u0000\u0000\u07f2"+
		"\u011b\u0001\u0000\u0000\u0000\u07f3\u07f8\u0003\u00f2y\u0000\u07f4\u07f5"+
		"\u0005r\u0000\u0000\u07f5\u07f7\u0003\u00f2y\u0000\u07f6\u07f4\u0001\u0000"+
		"\u0000\u0000\u07f7\u07fa\u0001\u0000\u0000\u0000\u07f8\u07f6\u0001\u0000"+
		"\u0000\u0000\u07f8\u07f9\u0001\u0000\u0000\u0000\u07f9\u07fc\u0001\u0000"+
		"\u0000\u0000\u07fa\u07f8\u0001\u0000\u0000\u0000\u07fb\u07fd\u0005r\u0000"+
		"\u0000\u07fc\u07fb\u0001\u0000\u0000\u0000\u07fc\u07fd\u0001\u0000\u0000"+
		"\u0000\u07fd\u011d\u0001\u0000\u0000\u0000\u07fe\u0801\u0003\u0158\u00ac"+
		"\u0000\u07ff\u0801\u0003\u016c\u00b6\u0000\u0800\u07fe\u0001\u0000\u0000"+
		"\u0000\u0800\u07ff\u0001\u0000\u0000\u0000\u0801\u011f\u0001\u0000\u0000"+
		"\u0000\u0802\u0806\u0003\u0122\u0091\u0000\u0803\u0806\u0003\u0144\u00a2"+
		"\u0000\u0804\u0806\u0003\u0140\u00a0\u0000\u0805\u0802\u0001\u0000\u0000"+
		"\u0000\u0805\u0803\u0001\u0000\u0000\u0000\u0805\u0804\u0001\u0000\u0000"+
		"\u0000\u0806\u0121\u0001\u0000\u0000\u0000\u0807\u0816\u0003\u0124\u0092"+
		"\u0000\u0808\u0816\u0003\u0146\u00a3\u0000\u0809\u0816\u0003\u0142\u00a1"+
		"\u0000\u080a\u0816\u0003\u0172\u00b9\u0000\u080b\u0816\u0003\u0128\u0094"+
		"\u0000\u080c\u0816\u0003\u0126\u0093\u0000\u080d\u0816\u0003\u0130\u0098"+
		"\u0000\u080e\u0816\u0003\u012e\u0097\u0000\u080f\u0816\u0003\u012a\u0095"+
		"\u0000\u0810\u0816\u0003\u012c\u0096\u0000\u0811\u0816\u0003\u0148\u00a4"+
		"\u0000\u0812\u0816\u0003\u0170\u00b8\u0000\u0813\u0816\u0003\u0132\u0099"+
		"\u0000\u0814\u0816\u0003\u0002\u0001\u0000\u0815\u0807\u0001\u0000\u0000"+
		"\u0000\u0815\u0808\u0001\u0000\u0000\u0000\u0815\u0809\u0001\u0000\u0000"+
		"\u0000\u0815\u080a\u0001\u0000\u0000\u0000\u0815\u080b\u0001\u0000\u0000"+
		"\u0000\u0815\u080c\u0001\u0000\u0000\u0000\u0815\u080d\u0001\u0000\u0000"+
		"\u0000\u0815\u080e\u0001\u0000\u0000\u0000\u0815\u080f\u0001\u0000\u0000"+
		"\u0000\u0815\u0810\u0001\u0000\u0000\u0000\u0815\u0811\u0001\u0000\u0000"+
		"\u0000\u0815\u0812\u0001\u0000\u0000\u0000\u0815\u0813\u0001\u0000\u0000"+
		"\u0000\u0815\u0814\u0001\u0000\u0000\u0000\u0816\u0123\u0001\u0000\u0000"+
		"\u0000\u0817\u0818\u0005\u007f\u0000\u0000\u0818\u0819\u0003\u0120\u0090"+
		"\u0000\u0819\u081a\u0005\u0080\u0000\u0000\u081a\u0125\u0001\u0000\u0000"+
		"\u0000\u081b\u081c\u0005V\u0000\u0000\u081c\u0127\u0001\u0000\u0000\u0000"+
		"\u081d\u0828\u0005\u007f\u0000\u0000\u081e\u081f\u0003\u0120\u0090\u0000"+
		"\u081f\u0820\u0005r\u0000\u0000\u0820\u0822\u0001\u0000\u0000\u0000\u0821"+
		"\u081e\u0001\u0000\u0000\u0000\u0822\u0823\u0001\u0000\u0000\u0000\u0823"+
		"\u0821\u0001\u0000\u0000\u0000\u0823\u0824\u0001\u0000\u0000\u0000\u0824"+
		"\u0826\u0001\u0000\u0000\u0000\u0825\u0827\u0003\u0120\u0090\u0000\u0826"+
		"\u0825\u0001\u0000\u0000\u0000\u0826\u0827\u0001\u0000\u0000\u0000\u0827"+
		"\u0829\u0001\u0000\u0000\u0000\u0828\u0821\u0001\u0000\u0000\u0000\u0828"+
		"\u0829\u0001\u0000\u0000\u0000\u0829\u082a\u0001\u0000\u0000\u0000\u082a"+
		"\u082b\u0005\u0080\u0000\u0000\u082b\u0129\u0001\u0000\u0000\u0000\u082c"+
		"\u082d\u0005}\u0000\u0000\u082d\u082e\u0003\u0120\u0090\u0000\u082e\u082f"+
		"\u0005s\u0000\u0000\u082f\u0830\u0003\u009aM\u0000\u0830\u0831\u0005~"+
		"\u0000\u0000\u0831\u012b\u0001\u0000\u0000\u0000\u0832\u0833\u0005}\u0000"+
		"\u0000\u0833\u0834\u0003\u0120\u0090\u0000\u0834\u0835\u0005~\u0000\u0000"+
		"\u0835\u012d\u0001\u0000\u0000\u0000\u0836\u0838\u0005W\u0000\u0000\u0837"+
		"\u0839\u0003\u0152\u00a9\u0000\u0838\u0837\u0001\u0000\u0000\u0000\u0838"+
		"\u0839\u0001\u0000\u0000\u0000\u0839\u083b\u0001\u0000\u0000\u0000\u083a"+
		"\u083c\u0005\u0014\u0000\u0000\u083b\u083a\u0001\u0000\u0000\u0000\u083b"+
		"\u083c\u0001\u0000\u0000\u0000\u083c\u083d\u0001\u0000\u0000\u0000\u083d"+
		"\u083e\u0003\u0122\u0091\u0000\u083e\u012f\u0001\u0000\u0000\u0000\u083f"+
		"\u0840\u0005R\u0000\u0000\u0840\u0841\u0007\t\u0000\u0000\u0841\u0842"+
		"\u0003\u0122\u0091\u0000\u0842\u0131\u0001\u0000\u0000\u0000\u0843\u0845"+
		"\u0003\u0088D\u0000\u0844\u0843\u0001\u0000\u0000\u0000\u0844\u0845\u0001"+
		"\u0000\u0000\u0000\u0845\u0846\u0001\u0000\u0000\u0000\u0846\u0847\u0003"+
		"\u0134\u009a\u0000\u0847\u0848\u0005\n\u0000\u0000\u0848\u084a\u0005\u007f"+
		"\u0000\u0000\u0849\u084b\u0003\u0138\u009c\u0000\u084a\u0849\u0001\u0000"+
		"\u0000\u0000\u084a\u084b\u0001\u0000\u0000\u0000\u084b\u084c\u0001\u0000"+
		"\u0000\u0000\u084c\u084e\u0005\u0080\u0000\u0000\u084d\u084f\u0003\u0136"+
		"\u009b\u0000\u084e\u084d\u0001\u0000\u0000\u0000\u084e\u084f\u0001\u0000"+
		"\u0000\u0000\u084f\u0133\u0001\u0000\u0000\u0000\u0850\u0852\u0005 \u0000"+
		"\u0000\u0851\u0850\u0001\u0000\u0000\u0000\u0851\u0852\u0001\u0000\u0000"+
		"\u0000\u0852\u0857\u0001\u0000\u0000\u0000\u0853\u0855\u0005\b\u0000\u0000"+
		"\u0854\u0856\u00038\u001c\u0000\u0855\u0854\u0001\u0000\u0000\u0000\u0855"+
		"\u0856\u0001\u0000\u0000\u0000\u0856\u0858\u0001\u0000\u0000\u0000\u0857"+
		"\u0853\u0001\u0000\u0000\u0000\u0857\u0858\u0001\u0000\u0000\u0000\u0858"+
		"\u0135\u0001\u0000\u0000\u0000\u0859\u085a\u0005v\u0000\u0000\u085a\u085b"+
		"\u0003\u0122\u0091\u0000\u085b\u0137\u0001\u0000\u0000\u0000\u085c\u085f"+
		"\u0003\u013a\u009d\u0000\u085d\u085f\u0003\u013e\u009f\u0000\u085e\u085c"+
		"\u0001\u0000\u0000\u0000\u085e\u085d\u0001\u0000\u0000\u0000\u085f\u0139"+
		"\u0001\u0000\u0000\u0000\u0860\u0865\u0003\u013c\u009e\u0000\u0861\u0862"+
		"\u0005r\u0000\u0000\u0862\u0864\u0003\u013c\u009e\u0000\u0863\u0861\u0001"+
		"\u0000\u0000\u0000\u0864\u0867\u0001\u0000\u0000\u0000\u0865\u0863\u0001"+
		"\u0000\u0000\u0000\u0865\u0866\u0001\u0000\u0000\u0000\u0866\u0869\u0001"+
		"\u0000\u0000\u0000\u0867\u0865\u0001\u0000\u0000\u0000\u0868\u086a\u0005"+
		"r\u0000\u0000\u0869\u0868\u0001\u0000\u0000\u0000\u0869\u086a\u0001\u0000"+
		"\u0000\u0000\u086a\u013b\u0001\u0000\u0000\u0000\u086b\u086d\u0003\u008e"+
		"G\u0000\u086c\u086b\u0001\u0000\u0000\u0000\u086d\u0870\u0001\u0000\u0000"+
		"\u0000\u086e\u086c\u0001\u0000\u0000\u0000\u086e\u086f\u0001\u0000\u0000"+
		"\u0000\u086f\u0876\u0001\u0000\u0000\u0000\u0870\u086e\u0001\u0000\u0000"+
		"\u0000\u0871\u0874\u0003\u017c\u00be\u0000\u0872\u0874\u0005m\u0000\u0000"+
		"\u0873\u0871\u0001\u0000\u0000\u0000\u0873\u0872\u0001\u0000\u0000\u0000"+
		"\u0874\u0875\u0001\u0000\u0000\u0000\u0875\u0877\u0005t\u0000\u0000\u0876"+
		"\u0873\u0001\u0000\u0000\u0000\u0876\u0877\u0001\u0000\u0000\u0000\u0877"+
		"\u0878\u0001\u0000\u0000\u0000\u0878\u0879\u0003\u0120\u0090\u0000\u0879"+
		"\u013d\u0001\u0000\u0000\u0000\u087a\u087b\u0003\u013c\u009e\u0000\u087b"+
		"\u087c\u0005r\u0000\u0000\u087c\u087e\u0001\u0000\u0000\u0000\u087d\u087a"+
		"\u0001\u0000\u0000\u0000\u087e\u0881\u0001\u0000\u0000\u0000\u087f\u087d"+
		"\u0001\u0000\u0000\u0000\u087f\u0880\u0001\u0000\u0000\u0000\u0880\u0882"+
		"\u0001\u0000\u0000\u0000\u0881\u087f\u0001\u0000\u0000\u0000\u0882\u0883"+
		"\u0003\u013c\u009e\u0000\u0883\u0887\u0005r\u0000\u0000\u0884\u0886\u0003"+
		"\u008eG\u0000\u0885\u0884\u0001\u0000\u0000\u0000\u0886\u0889\u0001\u0000"+
		"\u0000\u0000\u0887\u0885\u0001\u0000\u0000\u0000\u0887\u0888\u0001\u0000"+
		"\u0000\u0000\u0888\u088a\u0001\u0000\u0000\u0000\u0889\u0887\u0001\u0000"+
		"\u0000\u0000\u088a\u088b\u0005p\u0000\u0000\u088b\u013f\u0001\u0000\u0000"+
		"\u0000\u088c\u088e\u0005&\u0000\u0000\u088d\u088c\u0001\u0000\u0000\u0000"+
		"\u088d\u088e\u0001\u0000\u0000\u0000\u088e\u088f\u0001\u0000\u0000\u0000"+
		"\u088f\u0890\u0003\u014a\u00a5\u0000\u0890\u0141\u0001\u0000\u0000\u0000"+
		"\u0891\u0893\u0005&\u0000\u0000\u0892\u0891\u0001\u0000\u0000\u0000\u0892"+
		"\u0893\u0001\u0000\u0000\u0000\u0893\u0894\u0001\u0000\u0000\u0000\u0894"+
		"\u0895\u0003\u014e\u00a7\u0000\u0895\u0143\u0001\u0000\u0000\u0000\u0896"+
		"\u0897\u0005\r\u0000\u0000\u0897\u0898\u0003\u014a\u00a5\u0000\u0898\u0145"+
		"\u0001\u0000\u0000\u0000\u0899\u089a\u0005\r\u0000\u0000\u089a\u089b\u0003"+
		"\u014e\u00a7\u0000\u089b\u0147\u0001\u0000\u0000\u0000\u089c\u089d\u0005"+
		"m\u0000\u0000\u089d\u0149\u0001\u0000\u0000\u0000\u089e\u08a3\u0003\u014c"+
		"\u00a6\u0000\u089f\u08a0\u0005P\u0000\u0000\u08a0\u08a2\u0003\u014c\u00a6"+
		"\u0000\u08a1\u089f\u0001\u0000\u0000\u0000\u08a2\u08a5\u0001\u0000\u0000"+
		"\u0000\u08a3\u08a1\u0001\u0000\u0000\u0000\u08a3\u08a4\u0001\u0000\u0000"+
		"\u0000\u08a4\u08a7\u0001\u0000\u0000\u0000\u08a5\u08a3\u0001\u0000\u0000"+
		"\u0000\u08a6\u08a8\u0005P\u0000\u0000\u08a7\u08a6\u0001\u0000\u0000\u0000"+
		"\u08a7\u08a8\u0001\u0000\u0000\u0000\u08a8\u014b\u0001\u0000\u0000\u0000"+
		"\u08a9\u08ac\u0003\u0152\u00a9\u0000\u08aa\u08ac\u0003\u014e\u00a7\u0000"+
		"\u08ab\u08a9\u0001\u0000\u0000\u0000\u08ab\u08aa\u0001\u0000\u0000\u0000"+
		"\u08ac\u014d\u0001\u0000\u0000\u0000\u08ad\u08af\u0005z\u0000\u0000\u08ae"+
		"\u08ad\u0001\u0000\u0000\u0000\u08ae\u08af\u0001\u0000\u0000\u0000\u08af"+
		"\u08b1\u0001\u0000\u0000\u0000\u08b0\u08b2\u0003\u0088D\u0000\u08b1\u08b0"+
		"\u0001\u0000\u0000\u0000\u08b1\u08b2\u0001\u0000\u0000\u0000\u08b2\u08b3"+
		"\u0001\u0000\u0000\u0000\u08b3\u08bf\u0003\u0172\u00b9\u0000\u08b4\u08b6"+
		"\u0005\u007f\u0000\u0000\u08b5\u08b7\u0005z\u0000\u0000\u08b6\u08b5\u0001"+
		"\u0000\u0000\u0000\u08b6\u08b7\u0001\u0000\u0000\u0000\u08b7\u08b9\u0001"+
		"\u0000\u0000\u0000\u08b8\u08ba\u0003\u0088D\u0000\u08b9\u08b8\u0001\u0000"+
		"\u0000\u0000\u08b9\u08ba\u0001\u0000\u0000\u0000\u08ba\u08bb\u0001\u0000"+
		"\u0000\u0000\u08bb\u08bc\u0003\u0172\u00b9\u0000\u08bc\u08bd\u0005\u0080"+
		"\u0000\u0000\u08bd\u08bf\u0001\u0000\u0000\u0000\u08be\u08ae\u0001\u0000"+
		"\u0000\u0000\u08be\u08b4\u0001\u0000\u0000\u0000\u08bf\u014f\u0001\u0000"+
		"\u0000\u0000\u08c0\u08c1\u0003\u0152\u00a9\u0000\u08c1\u08c2\u0005P\u0000"+
		"\u0000\u08c2\u08c4\u0001\u0000\u0000\u0000\u08c3\u08c0\u0001\u0000\u0000"+
		"\u0000\u08c4\u08c7\u0001\u0000\u0000\u0000\u08c5\u08c3\u0001\u0000\u0000"+
		"\u0000\u08c5\u08c6\u0001\u0000\u0000\u0000\u08c6\u08c9\u0001\u0000\u0000"+
		"\u0000\u08c7\u08c5\u0001\u0000\u0000\u0000\u08c8\u08ca\u0003\u0152\u00a9"+
		"\u0000\u08c9\u08c8\u0001\u0000\u0000\u0000\u08c9\u08ca\u0001\u0000\u0000"+
		"\u0000\u08ca\u0151\u0001\u0000\u0000\u0000\u08cb\u08cc\u0007\n\u0000\u0000"+
		"\u08cc\u0153\u0001\u0000\u0000\u0000\u08cd\u08cf\u0005u\u0000\u0000\u08ce"+
		"\u08cd\u0001\u0000\u0000\u0000\u08ce\u08cf\u0001\u0000\u0000\u0000\u08cf"+
		"\u08d0\u0001\u0000\u0000\u0000\u08d0\u08d5\u0003\u0156\u00ab\u0000\u08d1"+
		"\u08d2\u0005u\u0000\u0000\u08d2\u08d4\u0003\u0156\u00ab\u0000\u08d3\u08d1"+
		"\u0001\u0000\u0000\u0000\u08d4\u08d7\u0001\u0000\u0000\u0000\u08d5\u08d3"+
		"\u0001\u0000\u0000\u0000\u08d5\u08d6\u0001\u0000\u0000\u0000\u08d6\u0155"+
		"\u0001\u0000\u0000\u0000\u08d7\u08d5\u0001\u0000\u0000\u0000\u08d8\u08de"+
		"\u0003\u017c\u00be\u0000\u08d9\u08de\u0005\u001c\u0000\u0000\u08da\u08de"+
		"\u0005\u0018\u0000\u0000\u08db\u08de\u0005\u0005\u0000\u0000\u08dc\u08de"+
		"\u00058\u0000\u0000\u08dd\u08d8\u0001\u0000\u0000\u0000\u08dd\u08d9\u0001"+
		"\u0000\u0000\u0000\u08dd\u08da\u0001\u0000\u0000\u0000\u08dd\u08db\u0001"+
		"\u0000\u0000\u0000\u08dd\u08dc\u0001\u0000\u0000\u0000\u08de\u0157\u0001"+
		"\u0000\u0000\u0000\u08df\u08e1\u0005u\u0000\u0000\u08e0\u08df\u0001\u0000"+
		"\u0000\u0000\u08e0\u08e1\u0001\u0000\u0000\u0000\u08e1\u08e2\u0001\u0000"+
		"\u0000\u0000\u08e2\u08e7\u0003\u015a\u00ad\u0000\u08e3\u08e4\u0005u\u0000"+
		"\u0000\u08e4\u08e6\u0003\u015a\u00ad\u0000\u08e5\u08e3\u0001\u0000\u0000"+
		"\u0000\u08e6\u08e9\u0001\u0000\u0000\u0000\u08e7\u08e5\u0001\u0000\u0000"+
		"\u0000\u08e7\u08e8\u0001\u0000\u0000\u0000\u08e8\u0159\u0001\u0000\u0000"+
		"\u0000\u08e9\u08e7\u0001\u0000\u0000\u0000\u08ea\u08ed\u0003\u015c\u00ae"+
		"\u0000\u08eb\u08ec\u0005u\u0000\u0000\u08ec\u08ee\u0003\u015e\u00af\u0000"+
		"\u08ed\u08eb\u0001\u0000\u0000\u0000\u08ed\u08ee\u0001\u0000\u0000\u0000"+
		"\u08ee\u015b\u0001\u0000\u0000\u0000\u08ef\u08f6\u0003\u017c\u00be\u0000"+
		"\u08f0\u08f6\u0005\u001c\u0000\u0000\u08f1\u08f6\u0005\u0018\u0000\u0000"+
		"\u08f2\u08f6\u0005\u0019\u0000\u0000\u08f3\u08f6\u0005\u0005\u0000\u0000"+
		"\u08f4\u08f6\u00058\u0000\u0000\u08f5\u08ef\u0001\u0000\u0000\u0000\u08f5"+
		"\u08f0\u0001\u0000\u0000\u0000\u08f5\u08f1\u0001\u0000\u0000\u0000\u08f5"+
		"\u08f2\u0001\u0000\u0000\u0000\u08f5\u08f3\u0001\u0000\u0000\u0000\u08f5"+
		"\u08f4\u0001\u0000\u0000\u0000\u08f6\u015d\u0001\u0000\u0000\u0000\u08f7"+
		"\u08f8\u0005i\u0000\u0000\u08f8\u0923\u0005h\u0000\u0000\u08f9\u08fa\u0005"+
		"i\u0000\u0000\u08fa\u08fd\u0003\u0164\u00b2\u0000\u08fb\u08fc\u0005r\u0000"+
		"\u0000\u08fc\u08fe\u0003\u0166\u00b3\u0000\u08fd\u08fb\u0001\u0000\u0000"+
		"\u0000\u08fd\u08fe\u0001\u0000\u0000\u0000\u08fe\u0901\u0001\u0000\u0000"+
		"\u0000\u08ff\u0900\u0005r\u0000\u0000\u0900\u0902\u0003\u0168\u00b4\u0000"+
		"\u0901\u08ff\u0001\u0000\u0000\u0000\u0901\u0902\u0001\u0000\u0000\u0000"+
		"\u0902\u0904\u0001\u0000\u0000\u0000\u0903\u0905\u0005r\u0000\u0000\u0904"+
		"\u0903\u0001\u0000\u0000\u0000\u0904\u0905\u0001\u0000\u0000\u0000\u0905"+
		"\u0906\u0001\u0000\u0000\u0000\u0906\u0907\u0005h\u0000\u0000\u0907\u0923"+
		"\u0001\u0000\u0000\u0000\u0908\u0909\u0005i\u0000\u0000\u0909\u090c\u0003"+
		"\u0166\u00b3\u0000\u090a\u090b\u0005r\u0000\u0000\u090b\u090d\u0003\u0168"+
		"\u00b4\u0000\u090c\u090a\u0001\u0000\u0000\u0000\u090c\u090d\u0001\u0000"+
		"\u0000\u0000\u090d\u090f\u0001\u0000\u0000\u0000\u090e\u0910\u0005r\u0000"+
		"\u0000\u090f\u090e\u0001\u0000\u0000\u0000\u090f\u0910\u0001\u0000\u0000"+
		"\u0000\u0910\u0911\u0001\u0000\u0000\u0000\u0911\u0912\u0005h\u0000\u0000"+
		"\u0912\u0923\u0001\u0000\u0000\u0000\u0913\u0919\u0005i\u0000\u0000\u0914"+
		"\u0915\u0003\u0160\u00b0\u0000\u0915\u0916\u0005r\u0000\u0000\u0916\u0918"+
		"\u0001\u0000\u0000\u0000\u0917\u0914\u0001\u0000\u0000\u0000\u0918\u091b"+
		"\u0001\u0000\u0000\u0000\u0919\u0917\u0001\u0000\u0000\u0000\u0919\u091a"+
		"\u0001\u0000\u0000\u0000\u091a\u091c\u0001\u0000\u0000\u0000\u091b\u0919"+
		"\u0001\u0000\u0000\u0000\u091c\u091e\u0003\u0160\u00b0\u0000\u091d\u091f"+
		"\u0005r\u0000\u0000\u091e\u091d\u0001\u0000\u0000\u0000\u091e\u091f\u0001"+
		"\u0000\u0000\u0000\u091f\u0920\u0001\u0000\u0000\u0000\u0920\u0921\u0005"+
		"h\u0000\u0000\u0921\u0923\u0001\u0000\u0000\u0000\u0922\u08f7\u0001\u0000"+
		"\u0000\u0000\u0922\u08f9\u0001\u0000\u0000\u0000\u0922\u0908\u0001\u0000"+
		"\u0000\u0000\u0922\u0913\u0001\u0000\u0000\u0000\u0923\u015f\u0001\u0000"+
		"\u0000\u0000\u0924\u0929\u0003\u0152\u00a9\u0000\u0925\u0929\u0003\u0120"+
		"\u0090\u0000\u0926\u0929\u0003\u0162\u00b1\u0000\u0927\u0929\u0003\u016a"+
		"\u00b5\u0000\u0928\u0924\u0001\u0000\u0000\u0000\u0928\u0925\u0001\u0000"+
		"\u0000\u0000\u0928\u0926\u0001\u0000\u0000\u0000\u0928\u0927\u0001\u0000"+
		"\u0000\u0000\u0929\u0161\u0001\u0000\u0000\u0000\u092a\u0931\u0003\u00a8"+
		"T\u0000\u092b\u092d\u0005Q\u0000\u0000\u092c\u092b\u0001\u0000\u0000\u0000"+
		"\u092c\u092d\u0001\u0000\u0000\u0000\u092d\u092e\u0001\u0000\u0000\u0000"+
		"\u092e\u0931\u0003\u00a4R\u0000\u092f\u0931\u0003\u0156\u00ab\u0000\u0930"+
		"\u092a\u0001\u0000\u0000\u0000\u0930\u092c\u0001\u0000\u0000\u0000\u0930"+
		"\u092f\u0001\u0000\u0000\u0000\u0931\u0163\u0001\u0000\u0000\u0000\u0932"+
		"\u0937\u0003\u0152\u00a9\u0000\u0933\u0934\u0005r\u0000\u0000\u0934\u0936"+
		"\u0003\u0152\u00a9\u0000\u0935\u0933\u0001\u0000\u0000\u0000\u0936\u0939"+
		"\u0001\u0000\u0000\u0000\u0937\u0935\u0001\u0000\u0000\u0000\u0937\u0938"+
		"\u0001\u0000\u0000\u0000\u0938\u0165\u0001\u0000\u0000\u0000\u0939\u0937"+
		"\u0001\u0000\u0000\u0000\u093a\u093f\u0003\u0120\u0090\u0000\u093b\u093c"+
		"\u0005r\u0000\u0000\u093c\u093e\u0003\u0120\u0090\u0000\u093d\u093b\u0001"+
		"\u0000\u0000\u0000\u093e\u0941\u0001\u0000\u0000\u0000\u093f\u093d\u0001"+
		"\u0000\u0000\u0000\u093f\u0940\u0001\u0000\u0000\u0000\u0940\u0167\u0001"+
		"\u0000\u0000\u0000\u0941\u093f\u0001\u0000\u0000\u0000\u0942\u0947\u0003"+
		"\u016a\u00b5\u0000\u0943\u0944\u0005r\u0000\u0000\u0944\u0946\u0003\u016a"+
		"\u00b5\u0000\u0945\u0943\u0001\u0000\u0000\u0000\u0946\u0949\u0001\u0000"+
		"\u0000\u0000\u0947\u0945\u0001\u0000\u0000\u0000\u0947\u0948\u0001\u0000"+
		"\u0000\u0000\u0948\u0169\u0001\u0000\u0000\u0000\u0949\u0947\u0001\u0000"+
		"\u0000\u0000\u094a\u094b\u0003\u017c\u00be\u0000\u094b\u094c\u0005e\u0000"+
		"\u0000\u094c\u094d\u0003\u0120\u0090\u0000\u094d\u016b\u0001\u0000\u0000"+
		"\u0000\u094e\u0951\u0003\u016e\u00b7\u0000\u094f\u0950\u0005u\u0000\u0000"+
		"\u0950\u0952\u0003\u015a\u00ad\u0000\u0951\u094f\u0001\u0000\u0000\u0000"+
		"\u0952\u0953\u0001\u0000\u0000\u0000\u0953\u0951\u0001\u0000\u0000\u0000"+
		"\u0953\u0954\u0001\u0000\u0000\u0000\u0954\u016d\u0001\u0000\u0000\u0000"+
		"\u0955\u0956\u0005i\u0000\u0000\u0956\u0959\u0003\u0120\u0090\u0000\u0957"+
		"\u0958\u0005\u0001\u0000\u0000\u0958\u095a\u0003\u0172\u00b9\u0000\u0959"+
		"\u0957\u0001\u0000\u0000\u0000\u0959\u095a\u0001\u0000\u0000\u0000\u095a"+
		"\u095b\u0001\u0000\u0000\u0000\u095b\u095c\u0005h\u0000\u0000\u095c\u016f"+
		"\u0001\u0000\u0000\u0000\u095d\u0960\u0003\u016e\u00b7\u0000\u095e\u095f"+
		"\u0005u\u0000\u0000\u095f\u0961\u0003\u0174\u00ba\u0000\u0960\u095e\u0001"+
		"\u0000\u0000\u0000\u0961\u0962\u0001\u0000\u0000\u0000\u0962\u0960\u0001"+
		"\u0000\u0000\u0000\u0962\u0963\u0001\u0000\u0000\u0000\u0963\u0171\u0001"+
		"\u0000\u0000\u0000\u0964\u0966\u0005u\u0000\u0000\u0965\u0964\u0001\u0000"+
		"\u0000\u0000\u0965\u0966\u0001\u0000\u0000\u0000\u0966\u0967\u0001\u0000"+
		"\u0000\u0000\u0967\u096c\u0003\u0174\u00ba\u0000\u0968\u0969\u0005u\u0000"+
		"\u0000\u0969\u096b\u0003\u0174\u00ba\u0000\u096a\u0968\u0001\u0000\u0000"+
		"\u0000\u096b\u096e\u0001\u0000\u0000\u0000\u096c\u096a\u0001\u0000\u0000"+
		"\u0000\u096c\u096d\u0001\u0000\u0000\u0000\u096d\u0173\u0001\u0000\u0000"+
		"\u0000\u096e\u096c\u0001\u0000\u0000\u0000\u096f\u0971\u0003\u015c\u00ae"+
		"\u0000\u0970\u0972\u0005u\u0000\u0000\u0971\u0970\u0001\u0000\u0000\u0000"+
		"\u0971\u0972\u0001\u0000\u0000\u0000\u0972\u0975\u0001\u0000\u0000\u0000"+
		"\u0973\u0976\u0003\u015e\u00af\u0000\u0974\u0976\u0003\u0176\u00bb\u0000"+
		"\u0975\u0973\u0001\u0000\u0000\u0000\u0975\u0974\u0001\u0000\u0000\u0000"+
		"\u0975\u0976\u0001\u0000\u0000";
	private static final String _serializedATNSegment1 =
		"\u0000\u0976\u0175\u0001\u0000\u0000\u0000\u0977\u0979\u0005\u007f\u0000"+
		"\u0000\u0978\u097a\u0003\u0178\u00bc\u0000\u0979\u0978\u0001\u0000\u0000"+
		"\u0000\u0979\u097a\u0001\u0000\u0000\u0000\u097a\u097b\u0001\u0000\u0000"+
		"\u0000\u097b\u097e\u0005\u0080\u0000\u0000\u097c\u097d\u0005v\u0000\u0000"+
		"\u097d\u097f\u0003\u0120\u0090\u0000\u097e\u097c\u0001\u0000\u0000\u0000"+
		"\u097e\u097f\u0001\u0000\u0000\u0000\u097f\u0177\u0001\u0000\u0000\u0000"+
		"\u0980\u0985\u0003\u0120\u0090\u0000\u0981\u0982\u0005r\u0000\u0000\u0982"+
		"\u0984\u0003\u0120\u0090\u0000\u0983\u0981\u0001\u0000\u0000\u0000\u0984"+
		"\u0987\u0001\u0000\u0000\u0000\u0985\u0983\u0001\u0000\u0000\u0000\u0985"+
		"\u0986\u0001\u0000\u0000\u0000\u0986\u0989\u0001\u0000\u0000\u0000\u0987"+
		"\u0985\u0001\u0000\u0000\u0000\u0988\u098a\u0005r\u0000\u0000\u0989\u0988"+
		"\u0001\u0000\u0000\u0000\u0989\u098a\u0001\u0000\u0000\u0000\u098a\u0179"+
		"\u0001\u0000\u0000\u0000\u098b\u0995\u0005\u0015\u0000\u0000\u098c\u0992"+
		"\u0005\u007f\u0000\u0000\u098d\u0993\u0005\u0005\u0000\u0000\u098e\u0993"+
		"\u0005\u0018\u0000\u0000\u098f\u0993\u0005\u001c\u0000\u0000\u0990\u0991"+
		"\u0005\u000e\u0000\u0000\u0991\u0993\u0003\u0154\u00aa\u0000\u0992\u098d"+
		"\u0001\u0000\u0000\u0000\u0992\u098e\u0001\u0000\u0000\u0000\u0992\u098f"+
		"\u0001\u0000\u0000\u0000\u0992\u0990\u0001\u0000\u0000\u0000\u0993\u0994"+
		"\u0001\u0000\u0000\u0000\u0994\u0996\u0005\u0080\u0000\u0000\u0995\u098c"+
		"\u0001\u0000\u0000\u0000\u0995\u0996\u0001\u0000\u0000\u0000\u0996\u017b"+
		"\u0001\u0000\u0000\u0000\u0997\u0998\u0007\u000b\u0000\u0000\u0998\u017d"+
		"\u0001\u0000\u0000\u0000\u0999\u099a\u0007\f\u0000\u0000\u099a\u017f\u0001"+
		"\u0000\u0000\u0000\u099b\u09a2\u0003\u017e\u00bf\u0000\u099c\u09a2\u0003"+
		"\u017c\u00be\u0000\u099d\u09a2\u00056\u0000\u0000\u099e\u09a2\u00057\u0000"+
		"\u0000\u099f\u09a2\u00058\u0000\u0000\u09a0\u09a2\u0005O\u0000\u0000\u09a1"+
		"\u099b\u0001\u0000\u0000\u0000\u09a1\u099c\u0001\u0000\u0000\u0000\u09a1"+
		"\u099d\u0001\u0000\u0000\u0000\u09a1\u099e\u0001\u0000\u0000\u0000\u09a1"+
		"\u099f\u0001\u0000\u0000\u0000\u09a1\u09a0\u0001\u0000\u0000\u0000\u09a2"+
		"\u0181\u0001\u0000\u0000\u0000\u09a3\u09a4\u0003\u00a4R\u0000\u09a4\u0183"+
		"\u0001\u0000\u0000\u0000\u09a5\u09a6\u0007\r\u0000\u0000\u09a6\u0185\u0001"+
		"\u0000\u0000\u0000\u09a7\u09a8\u0005i\u0000\u0000\u09a8\u09a9\u0004\u00c3"+
		"\u0015\u0000\u09a9\u09aa\u0005i\u0000\u0000\u09aa\u0187\u0001\u0000\u0000"+
		"\u0000\u09ab\u09ac\u0005h\u0000\u0000\u09ac\u09ad\u0004\u00c4\u0016\u0000"+
		"\u09ad\u09ae\u0005h\u0000\u0000\u09ae\u0189\u0001\u0000\u0000\u0000\u0159"+
		"\u018d\u0193\u01a0\u01a8\u01b0\u01b4\u01b9\u01bc\u01c3\u01cb\u01d7\u01e3"+
		"\u01e8\u01fd\u0204\u0208\u0212\u021a\u0222\u0226\u022b\u0231\u023a\u023e"+
		"\u0242\u0248\u0250\u0259\u025e\u0261\u0270\u0274\u0277\u0280\u0286\u028a"+
		"\u0290\u0296\u029b\u02a2\u02a5\u02ae\u02b2\u02b4\u02b7\u02bd\u02bf\u02c1"+
		"\u02c7\u02cb\u02cf\u02d2\u02d6\u02d9\u02dc\u02df\u02e3\u02e5\u02eb\u02f0"+
		"\u02f7\u02fb\u02fd\u0302\u0307\u030b\u030d\u0310\u0315\u031e\u0324\u032a"+
		"\u0332\u0335\u0339\u033f\u0344\u0347\u034b\u034f\u0354\u0358\u035c\u0365"+
		"\u0369\u036e\u0372\u037d\u0381\u0386\u038a\u0391\u0394\u0398\u03a1\u03a5"+
		"\u03aa\u03ae\u03b4\u03b8\u03be\u03c8\u03cb\u03d4\u03da\u03e0\u03e7\u03ec"+
		"\u03f1\u03f5\u03f7\u03fa\u0400\u0406\u040d\u0411\u0415\u041b\u0421\u0427"+
		"\u042b\u042e\u0434\u043a\u0440\u0446\u044a\u0450\u0456\u045e\u0463\u0467"+
		"\u0469\u0471\u0476\u0478\u047f\u0485\u0488\u048d\u0490\u0495\u0497\u049b"+
		"\u04a8\u04ac\u04b0\u04b7\u04bc\u04c4\u04c9\u04ce\u04d0\u04df\u04e4\u04eb"+
		"\u04f0\u04f7\u04fb\u0504\u0506\u050c\u0514\u051d\u0523\u0526\u052a\u052d"+
		"\u0531\u0537\u0541\u0545\u054c\u0550\u0558\u0586\u0596\u05a6\u05a8\u05aa"+
		"\u05b1\u05ba\u05c5\u05cb\u05d1\u05d5\u05dc\u05df\u05e2\u05e6\u05f2\u05f6"+
		"\u05fc\u0603\u0606\u060d\u0614\u0619\u0622\u0628\u062a\u062f\u0635\u063a"+
		"\u0644\u064c\u0650\u0652\u065b\u0660\u0669\u066d\u0672\u0677\u0680\u0684"+
		"\u0686\u0691\u0695\u0698\u069d\u06a0\u06a7\u06ae\u06b2\u06b7\u06bd\u06c0"+
		"\u06c6\u06e6\u06e8\u06f4\u06f6\u06fe\u0702\u070c\u0713\u071a\u071c\u0721"+
		"\u0726\u072c\u0733\u0738\u0746\u0751\u0755\u0758\u075b\u075e\u0763\u0774"+
		"\u0779\u077d\u0781\u0785\u078c\u0793\u0795\u0798\u079f\u07a5\u07b1\u07b4"+
		"\u07b7\u07bc\u07c4\u07cd\u07d1\u07d5\u07e2\u07e5\u07e7\u07ef\u07f8\u07fc"+
		"\u0800\u0805\u0815\u0823\u0826\u0828\u0838\u083b\u0844\u084a\u084e\u0851"+
		"\u0855\u0857\u085e\u0865\u0869\u086e\u0873\u0876\u087f\u0887\u088d\u0892"+
		"\u08a3\u08a7\u08ab\u08ae\u08b1\u08b6\u08b9\u08be\u08c5\u08c9\u08ce\u08d5"+
		"\u08dd\u08e0\u08e7\u08ed\u08f5\u08fd\u0901\u0904\u090c\u090f\u0919\u091e"+
		"\u0922\u0928\u092c\u0930\u0937\u093f\u0947\u0953\u0959\u0962\u0965\u096c"+
		"\u0971\u0975\u0979\u097e\u0985\u0989\u0992\u0995\u09a1";
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