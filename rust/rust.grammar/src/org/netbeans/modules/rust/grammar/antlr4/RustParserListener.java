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

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link RustParser}.
 */
public interface RustParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link RustParser#crate}.
	 * @param ctx the parse tree
	 */
	void enterCrate(RustParser.CrateContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#crate}.
	 * @param ctx the parse tree
	 */
	void exitCrate(RustParser.CrateContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#macroInvocation}.
	 * @param ctx the parse tree
	 */
	void enterMacroInvocation(RustParser.MacroInvocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#macroInvocation}.
	 * @param ctx the parse tree
	 */
	void exitMacroInvocation(RustParser.MacroInvocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#delimTokenTree}.
	 * @param ctx the parse tree
	 */
	void enterDelimTokenTree(RustParser.DelimTokenTreeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#delimTokenTree}.
	 * @param ctx the parse tree
	 */
	void exitDelimTokenTree(RustParser.DelimTokenTreeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#tokenTree}.
	 * @param ctx the parse tree
	 */
	void enterTokenTree(RustParser.TokenTreeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#tokenTree}.
	 * @param ctx the parse tree
	 */
	void exitTokenTree(RustParser.TokenTreeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#tokenTreeToken}.
	 * @param ctx the parse tree
	 */
	void enterTokenTreeToken(RustParser.TokenTreeTokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#tokenTreeToken}.
	 * @param ctx the parse tree
	 */
	void exitTokenTreeToken(RustParser.TokenTreeTokenContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#macroInvocationSemi}.
	 * @param ctx the parse tree
	 */
	void enterMacroInvocationSemi(RustParser.MacroInvocationSemiContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#macroInvocationSemi}.
	 * @param ctx the parse tree
	 */
	void exitMacroInvocationSemi(RustParser.MacroInvocationSemiContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#macroRulesDefinition}.
	 * @param ctx the parse tree
	 */
	void enterMacroRulesDefinition(RustParser.MacroRulesDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#macroRulesDefinition}.
	 * @param ctx the parse tree
	 */
	void exitMacroRulesDefinition(RustParser.MacroRulesDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#macroRulesDef}.
	 * @param ctx the parse tree
	 */
	void enterMacroRulesDef(RustParser.MacroRulesDefContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#macroRulesDef}.
	 * @param ctx the parse tree
	 */
	void exitMacroRulesDef(RustParser.MacroRulesDefContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#macroRules}.
	 * @param ctx the parse tree
	 */
	void enterMacroRules(RustParser.MacroRulesContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#macroRules}.
	 * @param ctx the parse tree
	 */
	void exitMacroRules(RustParser.MacroRulesContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#macroRule}.
	 * @param ctx the parse tree
	 */
	void enterMacroRule(RustParser.MacroRuleContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#macroRule}.
	 * @param ctx the parse tree
	 */
	void exitMacroRule(RustParser.MacroRuleContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#macroMatcher}.
	 * @param ctx the parse tree
	 */
	void enterMacroMatcher(RustParser.MacroMatcherContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#macroMatcher}.
	 * @param ctx the parse tree
	 */
	void exitMacroMatcher(RustParser.MacroMatcherContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#macroMatch}.
	 * @param ctx the parse tree
	 */
	void enterMacroMatch(RustParser.MacroMatchContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#macroMatch}.
	 * @param ctx the parse tree
	 */
	void exitMacroMatch(RustParser.MacroMatchContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#macroMatchToken}.
	 * @param ctx the parse tree
	 */
	void enterMacroMatchToken(RustParser.MacroMatchTokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#macroMatchToken}.
	 * @param ctx the parse tree
	 */
	void exitMacroMatchToken(RustParser.MacroMatchTokenContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#macroFragSpec}.
	 * @param ctx the parse tree
	 */
	void enterMacroFragSpec(RustParser.MacroFragSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#macroFragSpec}.
	 * @param ctx the parse tree
	 */
	void exitMacroFragSpec(RustParser.MacroFragSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#macroRepSep}.
	 * @param ctx the parse tree
	 */
	void enterMacroRepSep(RustParser.MacroRepSepContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#macroRepSep}.
	 * @param ctx the parse tree
	 */
	void exitMacroRepSep(RustParser.MacroRepSepContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#macroRepOp}.
	 * @param ctx the parse tree
	 */
	void enterMacroRepOp(RustParser.MacroRepOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#macroRepOp}.
	 * @param ctx the parse tree
	 */
	void exitMacroRepOp(RustParser.MacroRepOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#macroTranscriber}.
	 * @param ctx the parse tree
	 */
	void enterMacroTranscriber(RustParser.MacroTranscriberContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#macroTranscriber}.
	 * @param ctx the parse tree
	 */
	void exitMacroTranscriber(RustParser.MacroTranscriberContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#item}.
	 * @param ctx the parse tree
	 */
	void enterItem(RustParser.ItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#item}.
	 * @param ctx the parse tree
	 */
	void exitItem(RustParser.ItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#visItem}.
	 * @param ctx the parse tree
	 */
	void enterVisItem(RustParser.VisItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#visItem}.
	 * @param ctx the parse tree
	 */
	void exitVisItem(RustParser.VisItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#macroItem}.
	 * @param ctx the parse tree
	 */
	void enterMacroItem(RustParser.MacroItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#macroItem}.
	 * @param ctx the parse tree
	 */
	void exitMacroItem(RustParser.MacroItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#module}.
	 * @param ctx the parse tree
	 */
	void enterModule(RustParser.ModuleContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#module}.
	 * @param ctx the parse tree
	 */
	void exitModule(RustParser.ModuleContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#externCrate}.
	 * @param ctx the parse tree
	 */
	void enterExternCrate(RustParser.ExternCrateContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#externCrate}.
	 * @param ctx the parse tree
	 */
	void exitExternCrate(RustParser.ExternCrateContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#crateRef}.
	 * @param ctx the parse tree
	 */
	void enterCrateRef(RustParser.CrateRefContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#crateRef}.
	 * @param ctx the parse tree
	 */
	void exitCrateRef(RustParser.CrateRefContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#asClause}.
	 * @param ctx the parse tree
	 */
	void enterAsClause(RustParser.AsClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#asClause}.
	 * @param ctx the parse tree
	 */
	void exitAsClause(RustParser.AsClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#useDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterUseDeclaration(RustParser.UseDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#useDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitUseDeclaration(RustParser.UseDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#useTree}.
	 * @param ctx the parse tree
	 */
	void enterUseTree(RustParser.UseTreeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#useTree}.
	 * @param ctx the parse tree
	 */
	void exitUseTree(RustParser.UseTreeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#function_}.
	 * @param ctx the parse tree
	 */
	void enterFunction_(RustParser.Function_Context ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#function_}.
	 * @param ctx the parse tree
	 */
	void exitFunction_(RustParser.Function_Context ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#functionQualifiers}.
	 * @param ctx the parse tree
	 */
	void enterFunctionQualifiers(RustParser.FunctionQualifiersContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#functionQualifiers}.
	 * @param ctx the parse tree
	 */
	void exitFunctionQualifiers(RustParser.FunctionQualifiersContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#abi}.
	 * @param ctx the parse tree
	 */
	void enterAbi(RustParser.AbiContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#abi}.
	 * @param ctx the parse tree
	 */
	void exitAbi(RustParser.AbiContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#functionParameters}.
	 * @param ctx the parse tree
	 */
	void enterFunctionParameters(RustParser.FunctionParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#functionParameters}.
	 * @param ctx the parse tree
	 */
	void exitFunctionParameters(RustParser.FunctionParametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#selfParam}.
	 * @param ctx the parse tree
	 */
	void enterSelfParam(RustParser.SelfParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#selfParam}.
	 * @param ctx the parse tree
	 */
	void exitSelfParam(RustParser.SelfParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#shorthandSelf}.
	 * @param ctx the parse tree
	 */
	void enterShorthandSelf(RustParser.ShorthandSelfContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#shorthandSelf}.
	 * @param ctx the parse tree
	 */
	void exitShorthandSelf(RustParser.ShorthandSelfContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#typedSelf}.
	 * @param ctx the parse tree
	 */
	void enterTypedSelf(RustParser.TypedSelfContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#typedSelf}.
	 * @param ctx the parse tree
	 */
	void exitTypedSelf(RustParser.TypedSelfContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#functionParam}.
	 * @param ctx the parse tree
	 */
	void enterFunctionParam(RustParser.FunctionParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#functionParam}.
	 * @param ctx the parse tree
	 */
	void exitFunctionParam(RustParser.FunctionParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#functionParamPattern}.
	 * @param ctx the parse tree
	 */
	void enterFunctionParamPattern(RustParser.FunctionParamPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#functionParamPattern}.
	 * @param ctx the parse tree
	 */
	void exitFunctionParamPattern(RustParser.FunctionParamPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#functionReturnType}.
	 * @param ctx the parse tree
	 */
	void enterFunctionReturnType(RustParser.FunctionReturnTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#functionReturnType}.
	 * @param ctx the parse tree
	 */
	void exitFunctionReturnType(RustParser.FunctionReturnTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#typeAlias}.
	 * @param ctx the parse tree
	 */
	void enterTypeAlias(RustParser.TypeAliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#typeAlias}.
	 * @param ctx the parse tree
	 */
	void exitTypeAlias(RustParser.TypeAliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#struct_}.
	 * @param ctx the parse tree
	 */
	void enterStruct_(RustParser.Struct_Context ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#struct_}.
	 * @param ctx the parse tree
	 */
	void exitStruct_(RustParser.Struct_Context ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#structStruct}.
	 * @param ctx the parse tree
	 */
	void enterStructStruct(RustParser.StructStructContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#structStruct}.
	 * @param ctx the parse tree
	 */
	void exitStructStruct(RustParser.StructStructContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#tupleStruct}.
	 * @param ctx the parse tree
	 */
	void enterTupleStruct(RustParser.TupleStructContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#tupleStruct}.
	 * @param ctx the parse tree
	 */
	void exitTupleStruct(RustParser.TupleStructContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#structFields}.
	 * @param ctx the parse tree
	 */
	void enterStructFields(RustParser.StructFieldsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#structFields}.
	 * @param ctx the parse tree
	 */
	void exitStructFields(RustParser.StructFieldsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#structField}.
	 * @param ctx the parse tree
	 */
	void enterStructField(RustParser.StructFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#structField}.
	 * @param ctx the parse tree
	 */
	void exitStructField(RustParser.StructFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#tupleFields}.
	 * @param ctx the parse tree
	 */
	void enterTupleFields(RustParser.TupleFieldsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#tupleFields}.
	 * @param ctx the parse tree
	 */
	void exitTupleFields(RustParser.TupleFieldsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#tupleField}.
	 * @param ctx the parse tree
	 */
	void enterTupleField(RustParser.TupleFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#tupleField}.
	 * @param ctx the parse tree
	 */
	void exitTupleField(RustParser.TupleFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#enumeration}.
	 * @param ctx the parse tree
	 */
	void enterEnumeration(RustParser.EnumerationContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#enumeration}.
	 * @param ctx the parse tree
	 */
	void exitEnumeration(RustParser.EnumerationContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#enumItems}.
	 * @param ctx the parse tree
	 */
	void enterEnumItems(RustParser.EnumItemsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#enumItems}.
	 * @param ctx the parse tree
	 */
	void exitEnumItems(RustParser.EnumItemsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#enumItem}.
	 * @param ctx the parse tree
	 */
	void enterEnumItem(RustParser.EnumItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#enumItem}.
	 * @param ctx the parse tree
	 */
	void exitEnumItem(RustParser.EnumItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#enumItemTuple}.
	 * @param ctx the parse tree
	 */
	void enterEnumItemTuple(RustParser.EnumItemTupleContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#enumItemTuple}.
	 * @param ctx the parse tree
	 */
	void exitEnumItemTuple(RustParser.EnumItemTupleContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#enumItemStruct}.
	 * @param ctx the parse tree
	 */
	void enterEnumItemStruct(RustParser.EnumItemStructContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#enumItemStruct}.
	 * @param ctx the parse tree
	 */
	void exitEnumItemStruct(RustParser.EnumItemStructContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#enumItemDiscriminant}.
	 * @param ctx the parse tree
	 */
	void enterEnumItemDiscriminant(RustParser.EnumItemDiscriminantContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#enumItemDiscriminant}.
	 * @param ctx the parse tree
	 */
	void exitEnumItemDiscriminant(RustParser.EnumItemDiscriminantContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#union_}.
	 * @param ctx the parse tree
	 */
	void enterUnion_(RustParser.Union_Context ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#union_}.
	 * @param ctx the parse tree
	 */
	void exitUnion_(RustParser.Union_Context ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#constantItem}.
	 * @param ctx the parse tree
	 */
	void enterConstantItem(RustParser.ConstantItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#constantItem}.
	 * @param ctx the parse tree
	 */
	void exitConstantItem(RustParser.ConstantItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#staticItem}.
	 * @param ctx the parse tree
	 */
	void enterStaticItem(RustParser.StaticItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#staticItem}.
	 * @param ctx the parse tree
	 */
	void exitStaticItem(RustParser.StaticItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#trait_}.
	 * @param ctx the parse tree
	 */
	void enterTrait_(RustParser.Trait_Context ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#trait_}.
	 * @param ctx the parse tree
	 */
	void exitTrait_(RustParser.Trait_Context ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#implementation}.
	 * @param ctx the parse tree
	 */
	void enterImplementation(RustParser.ImplementationContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#implementation}.
	 * @param ctx the parse tree
	 */
	void exitImplementation(RustParser.ImplementationContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#inherentImpl}.
	 * @param ctx the parse tree
	 */
	void enterInherentImpl(RustParser.InherentImplContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#inherentImpl}.
	 * @param ctx the parse tree
	 */
	void exitInherentImpl(RustParser.InherentImplContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#traitImpl}.
	 * @param ctx the parse tree
	 */
	void enterTraitImpl(RustParser.TraitImplContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#traitImpl}.
	 * @param ctx the parse tree
	 */
	void exitTraitImpl(RustParser.TraitImplContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#externBlock}.
	 * @param ctx the parse tree
	 */
	void enterExternBlock(RustParser.ExternBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#externBlock}.
	 * @param ctx the parse tree
	 */
	void exitExternBlock(RustParser.ExternBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#externalItem}.
	 * @param ctx the parse tree
	 */
	void enterExternalItem(RustParser.ExternalItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#externalItem}.
	 * @param ctx the parse tree
	 */
	void exitExternalItem(RustParser.ExternalItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#genericParams}.
	 * @param ctx the parse tree
	 */
	void enterGenericParams(RustParser.GenericParamsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#genericParams}.
	 * @param ctx the parse tree
	 */
	void exitGenericParams(RustParser.GenericParamsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#genericParam}.
	 * @param ctx the parse tree
	 */
	void enterGenericParam(RustParser.GenericParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#genericParam}.
	 * @param ctx the parse tree
	 */
	void exitGenericParam(RustParser.GenericParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#lifetimeParam}.
	 * @param ctx the parse tree
	 */
	void enterLifetimeParam(RustParser.LifetimeParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#lifetimeParam}.
	 * @param ctx the parse tree
	 */
	void exitLifetimeParam(RustParser.LifetimeParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#typeParam}.
	 * @param ctx the parse tree
	 */
	void enterTypeParam(RustParser.TypeParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#typeParam}.
	 * @param ctx the parse tree
	 */
	void exitTypeParam(RustParser.TypeParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#constParam}.
	 * @param ctx the parse tree
	 */
	void enterConstParam(RustParser.ConstParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#constParam}.
	 * @param ctx the parse tree
	 */
	void exitConstParam(RustParser.ConstParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void enterWhereClause(RustParser.WhereClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void exitWhereClause(RustParser.WhereClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#whereClauseItem}.
	 * @param ctx the parse tree
	 */
	void enterWhereClauseItem(RustParser.WhereClauseItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#whereClauseItem}.
	 * @param ctx the parse tree
	 */
	void exitWhereClauseItem(RustParser.WhereClauseItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#lifetimeWhereClauseItem}.
	 * @param ctx the parse tree
	 */
	void enterLifetimeWhereClauseItem(RustParser.LifetimeWhereClauseItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#lifetimeWhereClauseItem}.
	 * @param ctx the parse tree
	 */
	void exitLifetimeWhereClauseItem(RustParser.LifetimeWhereClauseItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#typeBoundWhereClauseItem}.
	 * @param ctx the parse tree
	 */
	void enterTypeBoundWhereClauseItem(RustParser.TypeBoundWhereClauseItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#typeBoundWhereClauseItem}.
	 * @param ctx the parse tree
	 */
	void exitTypeBoundWhereClauseItem(RustParser.TypeBoundWhereClauseItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#forLifetimes}.
	 * @param ctx the parse tree
	 */
	void enterForLifetimes(RustParser.ForLifetimesContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#forLifetimes}.
	 * @param ctx the parse tree
	 */
	void exitForLifetimes(RustParser.ForLifetimesContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#associatedItem}.
	 * @param ctx the parse tree
	 */
	void enterAssociatedItem(RustParser.AssociatedItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#associatedItem}.
	 * @param ctx the parse tree
	 */
	void exitAssociatedItem(RustParser.AssociatedItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#innerAttribute}.
	 * @param ctx the parse tree
	 */
	void enterInnerAttribute(RustParser.InnerAttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#innerAttribute}.
	 * @param ctx the parse tree
	 */
	void exitInnerAttribute(RustParser.InnerAttributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#outerAttribute}.
	 * @param ctx the parse tree
	 */
	void enterOuterAttribute(RustParser.OuterAttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#outerAttribute}.
	 * @param ctx the parse tree
	 */
	void exitOuterAttribute(RustParser.OuterAttributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#attr}.
	 * @param ctx the parse tree
	 */
	void enterAttr(RustParser.AttrContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#attr}.
	 * @param ctx the parse tree
	 */
	void exitAttr(RustParser.AttrContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#attrInput}.
	 * @param ctx the parse tree
	 */
	void enterAttrInput(RustParser.AttrInputContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#attrInput}.
	 * @param ctx the parse tree
	 */
	void exitAttrInput(RustParser.AttrInputContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(RustParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(RustParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#letStatement}.
	 * @param ctx the parse tree
	 */
	void enterLetStatement(RustParser.LetStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#letStatement}.
	 * @param ctx the parse tree
	 */
	void exitLetStatement(RustParser.LetStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#expressionStatement}.
	 * @param ctx the parse tree
	 */
	void enterExpressionStatement(RustParser.ExpressionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#expressionStatement}.
	 * @param ctx the parse tree
	 */
	void exitExpressionStatement(RustParser.ExpressionStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code TypeCastExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterTypeCastExpression(RustParser.TypeCastExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code TypeCastExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitTypeCastExpression(RustParser.TypeCastExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code PathExpression_}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPathExpression_(RustParser.PathExpression_Context ctx);
	/**
	 * Exit a parse tree produced by the {@code PathExpression_}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPathExpression_(RustParser.PathExpression_Context ctx);
	/**
	 * Enter a parse tree produced by the {@code TupleExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterTupleExpression(RustParser.TupleExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code TupleExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitTupleExpression(RustParser.TupleExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IndexExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterIndexExpression(RustParser.IndexExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IndexExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitIndexExpression(RustParser.IndexExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code RangeExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterRangeExpression(RustParser.RangeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code RangeExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitRangeExpression(RustParser.RangeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MacroInvocationAsExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterMacroInvocationAsExpression(RustParser.MacroInvocationAsExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MacroInvocationAsExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitMacroInvocationAsExpression(RustParser.MacroInvocationAsExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ReturnExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterReturnExpression(RustParser.ReturnExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ReturnExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitReturnExpression(RustParser.ReturnExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AwaitExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAwaitExpression(RustParser.AwaitExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AwaitExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAwaitExpression(RustParser.AwaitExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ErrorPropagationExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterErrorPropagationExpression(RustParser.ErrorPropagationExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ErrorPropagationExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitErrorPropagationExpression(RustParser.ErrorPropagationExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ContinueExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterContinueExpression(RustParser.ContinueExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ContinueExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitContinueExpression(RustParser.ContinueExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AssignmentExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentExpression(RustParser.AssignmentExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AssignmentExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentExpression(RustParser.AssignmentExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MethodCallExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterMethodCallExpression(RustParser.MethodCallExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MethodCallExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitMethodCallExpression(RustParser.MethodCallExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LiteralExpression_}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterLiteralExpression_(RustParser.LiteralExpression_Context ctx);
	/**
	 * Exit a parse tree produced by the {@code LiteralExpression_}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitLiteralExpression_(RustParser.LiteralExpression_Context ctx);
	/**
	 * Enter a parse tree produced by the {@code StructExpression_}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterStructExpression_(RustParser.StructExpression_Context ctx);
	/**
	 * Exit a parse tree produced by the {@code StructExpression_}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitStructExpression_(RustParser.StructExpression_Context ctx);
	/**
	 * Enter a parse tree produced by the {@code TupleIndexingExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterTupleIndexingExpression(RustParser.TupleIndexingExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code TupleIndexingExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitTupleIndexingExpression(RustParser.TupleIndexingExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NegationExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterNegationExpression(RustParser.NegationExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NegationExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitNegationExpression(RustParser.NegationExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code CallExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterCallExpression(RustParser.CallExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code CallExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitCallExpression(RustParser.CallExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LazyBooleanExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterLazyBooleanExpression(RustParser.LazyBooleanExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LazyBooleanExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitLazyBooleanExpression(RustParser.LazyBooleanExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code DereferenceExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterDereferenceExpression(RustParser.DereferenceExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code DereferenceExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitDereferenceExpression(RustParser.DereferenceExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExpressionWithBlock_}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpressionWithBlock_(RustParser.ExpressionWithBlock_Context ctx);
	/**
	 * Exit a parse tree produced by the {@code ExpressionWithBlock_}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpressionWithBlock_(RustParser.ExpressionWithBlock_Context ctx);
	/**
	 * Enter a parse tree produced by the {@code GroupedExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterGroupedExpression(RustParser.GroupedExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code GroupedExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitGroupedExpression(RustParser.GroupedExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code BreakExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBreakExpression(RustParser.BreakExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code BreakExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBreakExpression(RustParser.BreakExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ArithmeticOrLogicalExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterArithmeticOrLogicalExpression(RustParser.ArithmeticOrLogicalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ArithmeticOrLogicalExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitArithmeticOrLogicalExpression(RustParser.ArithmeticOrLogicalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FieldExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterFieldExpression(RustParser.FieldExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FieldExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitFieldExpression(RustParser.FieldExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EnumerationVariantExpression_}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterEnumerationVariantExpression_(RustParser.EnumerationVariantExpression_Context ctx);
	/**
	 * Exit a parse tree produced by the {@code EnumerationVariantExpression_}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitEnumerationVariantExpression_(RustParser.EnumerationVariantExpression_Context ctx);
	/**
	 * Enter a parse tree produced by the {@code ComparisonExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterComparisonExpression(RustParser.ComparisonExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ComparisonExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitComparisonExpression(RustParser.ComparisonExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AttributedExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAttributedExpression(RustParser.AttributedExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AttributedExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAttributedExpression(RustParser.AttributedExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code BorrowExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBorrowExpression(RustParser.BorrowExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code BorrowExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBorrowExpression(RustParser.BorrowExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code CompoundAssignmentExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterCompoundAssignmentExpression(RustParser.CompoundAssignmentExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code CompoundAssignmentExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitCompoundAssignmentExpression(RustParser.CompoundAssignmentExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ClosureExpression_}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterClosureExpression_(RustParser.ClosureExpression_Context ctx);
	/**
	 * Exit a parse tree produced by the {@code ClosureExpression_}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitClosureExpression_(RustParser.ClosureExpression_Context ctx);
	/**
	 * Enter a parse tree produced by the {@code ArrayExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterArrayExpression(RustParser.ArrayExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ArrayExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitArrayExpression(RustParser.ArrayExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#shiftOperator}.
	 * @param ctx the parse tree
	 */
	void enterShiftOperator(RustParser.ShiftOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#shiftOperator}.
	 * @param ctx the parse tree
	 */
	void exitShiftOperator(RustParser.ShiftOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOperator(RustParser.ComparisonOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOperator(RustParser.ComparisonOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#compoundAssignOperator}.
	 * @param ctx the parse tree
	 */
	void enterCompoundAssignOperator(RustParser.CompoundAssignOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#compoundAssignOperator}.
	 * @param ctx the parse tree
	 */
	void exitCompoundAssignOperator(RustParser.CompoundAssignOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#expressionWithBlock}.
	 * @param ctx the parse tree
	 */
	void enterExpressionWithBlock(RustParser.ExpressionWithBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#expressionWithBlock}.
	 * @param ctx the parse tree
	 */
	void exitExpressionWithBlock(RustParser.ExpressionWithBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#literalExpression}.
	 * @param ctx the parse tree
	 */
	void enterLiteralExpression(RustParser.LiteralExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#literalExpression}.
	 * @param ctx the parse tree
	 */
	void exitLiteralExpression(RustParser.LiteralExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#pathExpression}.
	 * @param ctx the parse tree
	 */
	void enterPathExpression(RustParser.PathExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#pathExpression}.
	 * @param ctx the parse tree
	 */
	void exitPathExpression(RustParser.PathExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#blockExpression}.
	 * @param ctx the parse tree
	 */
	void enterBlockExpression(RustParser.BlockExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#blockExpression}.
	 * @param ctx the parse tree
	 */
	void exitBlockExpression(RustParser.BlockExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#statements}.
	 * @param ctx the parse tree
	 */
	void enterStatements(RustParser.StatementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#statements}.
	 * @param ctx the parse tree
	 */
	void exitStatements(RustParser.StatementsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#asyncBlockExpression}.
	 * @param ctx the parse tree
	 */
	void enterAsyncBlockExpression(RustParser.AsyncBlockExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#asyncBlockExpression}.
	 * @param ctx the parse tree
	 */
	void exitAsyncBlockExpression(RustParser.AsyncBlockExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#unsafeBlockExpression}.
	 * @param ctx the parse tree
	 */
	void enterUnsafeBlockExpression(RustParser.UnsafeBlockExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#unsafeBlockExpression}.
	 * @param ctx the parse tree
	 */
	void exitUnsafeBlockExpression(RustParser.UnsafeBlockExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#arrayElements}.
	 * @param ctx the parse tree
	 */
	void enterArrayElements(RustParser.ArrayElementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#arrayElements}.
	 * @param ctx the parse tree
	 */
	void exitArrayElements(RustParser.ArrayElementsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#tupleElements}.
	 * @param ctx the parse tree
	 */
	void enterTupleElements(RustParser.TupleElementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#tupleElements}.
	 * @param ctx the parse tree
	 */
	void exitTupleElements(RustParser.TupleElementsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#tupleIndex}.
	 * @param ctx the parse tree
	 */
	void enterTupleIndex(RustParser.TupleIndexContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#tupleIndex}.
	 * @param ctx the parse tree
	 */
	void exitTupleIndex(RustParser.TupleIndexContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#structExpression}.
	 * @param ctx the parse tree
	 */
	void enterStructExpression(RustParser.StructExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#structExpression}.
	 * @param ctx the parse tree
	 */
	void exitStructExpression(RustParser.StructExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#structExprStruct}.
	 * @param ctx the parse tree
	 */
	void enterStructExprStruct(RustParser.StructExprStructContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#structExprStruct}.
	 * @param ctx the parse tree
	 */
	void exitStructExprStruct(RustParser.StructExprStructContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#structExprFields}.
	 * @param ctx the parse tree
	 */
	void enterStructExprFields(RustParser.StructExprFieldsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#structExprFields}.
	 * @param ctx the parse tree
	 */
	void exitStructExprFields(RustParser.StructExprFieldsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#structExprField}.
	 * @param ctx the parse tree
	 */
	void enterStructExprField(RustParser.StructExprFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#structExprField}.
	 * @param ctx the parse tree
	 */
	void exitStructExprField(RustParser.StructExprFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#structBase}.
	 * @param ctx the parse tree
	 */
	void enterStructBase(RustParser.StructBaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#structBase}.
	 * @param ctx the parse tree
	 */
	void exitStructBase(RustParser.StructBaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#structExprTuple}.
	 * @param ctx the parse tree
	 */
	void enterStructExprTuple(RustParser.StructExprTupleContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#structExprTuple}.
	 * @param ctx the parse tree
	 */
	void exitStructExprTuple(RustParser.StructExprTupleContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#structExprUnit}.
	 * @param ctx the parse tree
	 */
	void enterStructExprUnit(RustParser.StructExprUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#structExprUnit}.
	 * @param ctx the parse tree
	 */
	void exitStructExprUnit(RustParser.StructExprUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#enumerationVariantExpression}.
	 * @param ctx the parse tree
	 */
	void enterEnumerationVariantExpression(RustParser.EnumerationVariantExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#enumerationVariantExpression}.
	 * @param ctx the parse tree
	 */
	void exitEnumerationVariantExpression(RustParser.EnumerationVariantExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#enumExprStruct}.
	 * @param ctx the parse tree
	 */
	void enterEnumExprStruct(RustParser.EnumExprStructContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#enumExprStruct}.
	 * @param ctx the parse tree
	 */
	void exitEnumExprStruct(RustParser.EnumExprStructContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#enumExprFields}.
	 * @param ctx the parse tree
	 */
	void enterEnumExprFields(RustParser.EnumExprFieldsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#enumExprFields}.
	 * @param ctx the parse tree
	 */
	void exitEnumExprFields(RustParser.EnumExprFieldsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#enumExprField}.
	 * @param ctx the parse tree
	 */
	void enterEnumExprField(RustParser.EnumExprFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#enumExprField}.
	 * @param ctx the parse tree
	 */
	void exitEnumExprField(RustParser.EnumExprFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#enumExprTuple}.
	 * @param ctx the parse tree
	 */
	void enterEnumExprTuple(RustParser.EnumExprTupleContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#enumExprTuple}.
	 * @param ctx the parse tree
	 */
	void exitEnumExprTuple(RustParser.EnumExprTupleContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#enumExprFieldless}.
	 * @param ctx the parse tree
	 */
	void enterEnumExprFieldless(RustParser.EnumExprFieldlessContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#enumExprFieldless}.
	 * @param ctx the parse tree
	 */
	void exitEnumExprFieldless(RustParser.EnumExprFieldlessContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#callParams}.
	 * @param ctx the parse tree
	 */
	void enterCallParams(RustParser.CallParamsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#callParams}.
	 * @param ctx the parse tree
	 */
	void exitCallParams(RustParser.CallParamsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#closureExpression}.
	 * @param ctx the parse tree
	 */
	void enterClosureExpression(RustParser.ClosureExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#closureExpression}.
	 * @param ctx the parse tree
	 */
	void exitClosureExpression(RustParser.ClosureExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#closureParameters}.
	 * @param ctx the parse tree
	 */
	void enterClosureParameters(RustParser.ClosureParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#closureParameters}.
	 * @param ctx the parse tree
	 */
	void exitClosureParameters(RustParser.ClosureParametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#closureParam}.
	 * @param ctx the parse tree
	 */
	void enterClosureParam(RustParser.ClosureParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#closureParam}.
	 * @param ctx the parse tree
	 */
	void exitClosureParam(RustParser.ClosureParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#loopExpression}.
	 * @param ctx the parse tree
	 */
	void enterLoopExpression(RustParser.LoopExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#loopExpression}.
	 * @param ctx the parse tree
	 */
	void exitLoopExpression(RustParser.LoopExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#infiniteLoopExpression}.
	 * @param ctx the parse tree
	 */
	void enterInfiniteLoopExpression(RustParser.InfiniteLoopExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#infiniteLoopExpression}.
	 * @param ctx the parse tree
	 */
	void exitInfiniteLoopExpression(RustParser.InfiniteLoopExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#predicateLoopExpression}.
	 * @param ctx the parse tree
	 */
	void enterPredicateLoopExpression(RustParser.PredicateLoopExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#predicateLoopExpression}.
	 * @param ctx the parse tree
	 */
	void exitPredicateLoopExpression(RustParser.PredicateLoopExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#predicatePatternLoopExpression}.
	 * @param ctx the parse tree
	 */
	void enterPredicatePatternLoopExpression(RustParser.PredicatePatternLoopExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#predicatePatternLoopExpression}.
	 * @param ctx the parse tree
	 */
	void exitPredicatePatternLoopExpression(RustParser.PredicatePatternLoopExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#iteratorLoopExpression}.
	 * @param ctx the parse tree
	 */
	void enterIteratorLoopExpression(RustParser.IteratorLoopExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#iteratorLoopExpression}.
	 * @param ctx the parse tree
	 */
	void exitIteratorLoopExpression(RustParser.IteratorLoopExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#loopLabel}.
	 * @param ctx the parse tree
	 */
	void enterLoopLabel(RustParser.LoopLabelContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#loopLabel}.
	 * @param ctx the parse tree
	 */
	void exitLoopLabel(RustParser.LoopLabelContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#ifExpression}.
	 * @param ctx the parse tree
	 */
	void enterIfExpression(RustParser.IfExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#ifExpression}.
	 * @param ctx the parse tree
	 */
	void exitIfExpression(RustParser.IfExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#ifLetExpression}.
	 * @param ctx the parse tree
	 */
	void enterIfLetExpression(RustParser.IfLetExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#ifLetExpression}.
	 * @param ctx the parse tree
	 */
	void exitIfLetExpression(RustParser.IfLetExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#matchExpression}.
	 * @param ctx the parse tree
	 */
	void enterMatchExpression(RustParser.MatchExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#matchExpression}.
	 * @param ctx the parse tree
	 */
	void exitMatchExpression(RustParser.MatchExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#matchArms}.
	 * @param ctx the parse tree
	 */
	void enterMatchArms(RustParser.MatchArmsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#matchArms}.
	 * @param ctx the parse tree
	 */
	void exitMatchArms(RustParser.MatchArmsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#matchArmExpression}.
	 * @param ctx the parse tree
	 */
	void enterMatchArmExpression(RustParser.MatchArmExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#matchArmExpression}.
	 * @param ctx the parse tree
	 */
	void exitMatchArmExpression(RustParser.MatchArmExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#matchArm}.
	 * @param ctx the parse tree
	 */
	void enterMatchArm(RustParser.MatchArmContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#matchArm}.
	 * @param ctx the parse tree
	 */
	void exitMatchArm(RustParser.MatchArmContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#matchArmGuard}.
	 * @param ctx the parse tree
	 */
	void enterMatchArmGuard(RustParser.MatchArmGuardContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#matchArmGuard}.
	 * @param ctx the parse tree
	 */
	void exitMatchArmGuard(RustParser.MatchArmGuardContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#pattern}.
	 * @param ctx the parse tree
	 */
	void enterPattern(RustParser.PatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#pattern}.
	 * @param ctx the parse tree
	 */
	void exitPattern(RustParser.PatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#patternNoTopAlt}.
	 * @param ctx the parse tree
	 */
	void enterPatternNoTopAlt(RustParser.PatternNoTopAltContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#patternNoTopAlt}.
	 * @param ctx the parse tree
	 */
	void exitPatternNoTopAlt(RustParser.PatternNoTopAltContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#patternWithoutRange}.
	 * @param ctx the parse tree
	 */
	void enterPatternWithoutRange(RustParser.PatternWithoutRangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#patternWithoutRange}.
	 * @param ctx the parse tree
	 */
	void exitPatternWithoutRange(RustParser.PatternWithoutRangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#literalPattern}.
	 * @param ctx the parse tree
	 */
	void enterLiteralPattern(RustParser.LiteralPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#literalPattern}.
	 * @param ctx the parse tree
	 */
	void exitLiteralPattern(RustParser.LiteralPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#identifierPattern}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierPattern(RustParser.IdentifierPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#identifierPattern}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierPattern(RustParser.IdentifierPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#wildcardPattern}.
	 * @param ctx the parse tree
	 */
	void enterWildcardPattern(RustParser.WildcardPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#wildcardPattern}.
	 * @param ctx the parse tree
	 */
	void exitWildcardPattern(RustParser.WildcardPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#restPattern}.
	 * @param ctx the parse tree
	 */
	void enterRestPattern(RustParser.RestPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#restPattern}.
	 * @param ctx the parse tree
	 */
	void exitRestPattern(RustParser.RestPatternContext ctx);
	/**
	 * Enter a parse tree produced by the {@code InclusiveRangePattern}
	 * labeled alternative in {@link RustParser#rangePattern}.
	 * @param ctx the parse tree
	 */
	void enterInclusiveRangePattern(RustParser.InclusiveRangePatternContext ctx);
	/**
	 * Exit a parse tree produced by the {@code InclusiveRangePattern}
	 * labeled alternative in {@link RustParser#rangePattern}.
	 * @param ctx the parse tree
	 */
	void exitInclusiveRangePattern(RustParser.InclusiveRangePatternContext ctx);
	/**
	 * Enter a parse tree produced by the {@code HalfOpenRangePattern}
	 * labeled alternative in {@link RustParser#rangePattern}.
	 * @param ctx the parse tree
	 */
	void enterHalfOpenRangePattern(RustParser.HalfOpenRangePatternContext ctx);
	/**
	 * Exit a parse tree produced by the {@code HalfOpenRangePattern}
	 * labeled alternative in {@link RustParser#rangePattern}.
	 * @param ctx the parse tree
	 */
	void exitHalfOpenRangePattern(RustParser.HalfOpenRangePatternContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ObsoleteRangePattern}
	 * labeled alternative in {@link RustParser#rangePattern}.
	 * @param ctx the parse tree
	 */
	void enterObsoleteRangePattern(RustParser.ObsoleteRangePatternContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ObsoleteRangePattern}
	 * labeled alternative in {@link RustParser#rangePattern}.
	 * @param ctx the parse tree
	 */
	void exitObsoleteRangePattern(RustParser.ObsoleteRangePatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#rangePatternBound}.
	 * @param ctx the parse tree
	 */
	void enterRangePatternBound(RustParser.RangePatternBoundContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#rangePatternBound}.
	 * @param ctx the parse tree
	 */
	void exitRangePatternBound(RustParser.RangePatternBoundContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#referencePattern}.
	 * @param ctx the parse tree
	 */
	void enterReferencePattern(RustParser.ReferencePatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#referencePattern}.
	 * @param ctx the parse tree
	 */
	void exitReferencePattern(RustParser.ReferencePatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#structPattern}.
	 * @param ctx the parse tree
	 */
	void enterStructPattern(RustParser.StructPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#structPattern}.
	 * @param ctx the parse tree
	 */
	void exitStructPattern(RustParser.StructPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#structPatternElements}.
	 * @param ctx the parse tree
	 */
	void enterStructPatternElements(RustParser.StructPatternElementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#structPatternElements}.
	 * @param ctx the parse tree
	 */
	void exitStructPatternElements(RustParser.StructPatternElementsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#structPatternFields}.
	 * @param ctx the parse tree
	 */
	void enterStructPatternFields(RustParser.StructPatternFieldsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#structPatternFields}.
	 * @param ctx the parse tree
	 */
	void exitStructPatternFields(RustParser.StructPatternFieldsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#structPatternField}.
	 * @param ctx the parse tree
	 */
	void enterStructPatternField(RustParser.StructPatternFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#structPatternField}.
	 * @param ctx the parse tree
	 */
	void exitStructPatternField(RustParser.StructPatternFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#structPatternEtCetera}.
	 * @param ctx the parse tree
	 */
	void enterStructPatternEtCetera(RustParser.StructPatternEtCeteraContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#structPatternEtCetera}.
	 * @param ctx the parse tree
	 */
	void exitStructPatternEtCetera(RustParser.StructPatternEtCeteraContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#tupleStructPattern}.
	 * @param ctx the parse tree
	 */
	void enterTupleStructPattern(RustParser.TupleStructPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#tupleStructPattern}.
	 * @param ctx the parse tree
	 */
	void exitTupleStructPattern(RustParser.TupleStructPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#tupleStructItems}.
	 * @param ctx the parse tree
	 */
	void enterTupleStructItems(RustParser.TupleStructItemsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#tupleStructItems}.
	 * @param ctx the parse tree
	 */
	void exitTupleStructItems(RustParser.TupleStructItemsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#tuplePattern}.
	 * @param ctx the parse tree
	 */
	void enterTuplePattern(RustParser.TuplePatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#tuplePattern}.
	 * @param ctx the parse tree
	 */
	void exitTuplePattern(RustParser.TuplePatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#tuplePatternItems}.
	 * @param ctx the parse tree
	 */
	void enterTuplePatternItems(RustParser.TuplePatternItemsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#tuplePatternItems}.
	 * @param ctx the parse tree
	 */
	void exitTuplePatternItems(RustParser.TuplePatternItemsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#groupedPattern}.
	 * @param ctx the parse tree
	 */
	void enterGroupedPattern(RustParser.GroupedPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#groupedPattern}.
	 * @param ctx the parse tree
	 */
	void exitGroupedPattern(RustParser.GroupedPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#slicePattern}.
	 * @param ctx the parse tree
	 */
	void enterSlicePattern(RustParser.SlicePatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#slicePattern}.
	 * @param ctx the parse tree
	 */
	void exitSlicePattern(RustParser.SlicePatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#slicePatternItems}.
	 * @param ctx the parse tree
	 */
	void enterSlicePatternItems(RustParser.SlicePatternItemsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#slicePatternItems}.
	 * @param ctx the parse tree
	 */
	void exitSlicePatternItems(RustParser.SlicePatternItemsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#pathPattern}.
	 * @param ctx the parse tree
	 */
	void enterPathPattern(RustParser.PathPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#pathPattern}.
	 * @param ctx the parse tree
	 */
	void exitPathPattern(RustParser.PathPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#type_}.
	 * @param ctx the parse tree
	 */
	void enterType_(RustParser.Type_Context ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#type_}.
	 * @param ctx the parse tree
	 */
	void exitType_(RustParser.Type_Context ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#typeNoBounds}.
	 * @param ctx the parse tree
	 */
	void enterTypeNoBounds(RustParser.TypeNoBoundsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#typeNoBounds}.
	 * @param ctx the parse tree
	 */
	void exitTypeNoBounds(RustParser.TypeNoBoundsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#parenthesizedType}.
	 * @param ctx the parse tree
	 */
	void enterParenthesizedType(RustParser.ParenthesizedTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#parenthesizedType}.
	 * @param ctx the parse tree
	 */
	void exitParenthesizedType(RustParser.ParenthesizedTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#neverType}.
	 * @param ctx the parse tree
	 */
	void enterNeverType(RustParser.NeverTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#neverType}.
	 * @param ctx the parse tree
	 */
	void exitNeverType(RustParser.NeverTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#tupleType}.
	 * @param ctx the parse tree
	 */
	void enterTupleType(RustParser.TupleTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#tupleType}.
	 * @param ctx the parse tree
	 */
	void exitTupleType(RustParser.TupleTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#arrayType}.
	 * @param ctx the parse tree
	 */
	void enterArrayType(RustParser.ArrayTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#arrayType}.
	 * @param ctx the parse tree
	 */
	void exitArrayType(RustParser.ArrayTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#sliceType}.
	 * @param ctx the parse tree
	 */
	void enterSliceType(RustParser.SliceTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#sliceType}.
	 * @param ctx the parse tree
	 */
	void exitSliceType(RustParser.SliceTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#referenceType}.
	 * @param ctx the parse tree
	 */
	void enterReferenceType(RustParser.ReferenceTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#referenceType}.
	 * @param ctx the parse tree
	 */
	void exitReferenceType(RustParser.ReferenceTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#rawPointerType}.
	 * @param ctx the parse tree
	 */
	void enterRawPointerType(RustParser.RawPointerTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#rawPointerType}.
	 * @param ctx the parse tree
	 */
	void exitRawPointerType(RustParser.RawPointerTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#bareFunctionType}.
	 * @param ctx the parse tree
	 */
	void enterBareFunctionType(RustParser.BareFunctionTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#bareFunctionType}.
	 * @param ctx the parse tree
	 */
	void exitBareFunctionType(RustParser.BareFunctionTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#functionTypeQualifiers}.
	 * @param ctx the parse tree
	 */
	void enterFunctionTypeQualifiers(RustParser.FunctionTypeQualifiersContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#functionTypeQualifiers}.
	 * @param ctx the parse tree
	 */
	void exitFunctionTypeQualifiers(RustParser.FunctionTypeQualifiersContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#bareFunctionReturnType}.
	 * @param ctx the parse tree
	 */
	void enterBareFunctionReturnType(RustParser.BareFunctionReturnTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#bareFunctionReturnType}.
	 * @param ctx the parse tree
	 */
	void exitBareFunctionReturnType(RustParser.BareFunctionReturnTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#functionParametersMaybeNamedVariadic}.
	 * @param ctx the parse tree
	 */
	void enterFunctionParametersMaybeNamedVariadic(RustParser.FunctionParametersMaybeNamedVariadicContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#functionParametersMaybeNamedVariadic}.
	 * @param ctx the parse tree
	 */
	void exitFunctionParametersMaybeNamedVariadic(RustParser.FunctionParametersMaybeNamedVariadicContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#maybeNamedFunctionParameters}.
	 * @param ctx the parse tree
	 */
	void enterMaybeNamedFunctionParameters(RustParser.MaybeNamedFunctionParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#maybeNamedFunctionParameters}.
	 * @param ctx the parse tree
	 */
	void exitMaybeNamedFunctionParameters(RustParser.MaybeNamedFunctionParametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#maybeNamedParam}.
	 * @param ctx the parse tree
	 */
	void enterMaybeNamedParam(RustParser.MaybeNamedParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#maybeNamedParam}.
	 * @param ctx the parse tree
	 */
	void exitMaybeNamedParam(RustParser.MaybeNamedParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#maybeNamedFunctionParametersVariadic}.
	 * @param ctx the parse tree
	 */
	void enterMaybeNamedFunctionParametersVariadic(RustParser.MaybeNamedFunctionParametersVariadicContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#maybeNamedFunctionParametersVariadic}.
	 * @param ctx the parse tree
	 */
	void exitMaybeNamedFunctionParametersVariadic(RustParser.MaybeNamedFunctionParametersVariadicContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#traitObjectType}.
	 * @param ctx the parse tree
	 */
	void enterTraitObjectType(RustParser.TraitObjectTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#traitObjectType}.
	 * @param ctx the parse tree
	 */
	void exitTraitObjectType(RustParser.TraitObjectTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#traitObjectTypeOneBound}.
	 * @param ctx the parse tree
	 */
	void enterTraitObjectTypeOneBound(RustParser.TraitObjectTypeOneBoundContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#traitObjectTypeOneBound}.
	 * @param ctx the parse tree
	 */
	void exitTraitObjectTypeOneBound(RustParser.TraitObjectTypeOneBoundContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#implTraitType}.
	 * @param ctx the parse tree
	 */
	void enterImplTraitType(RustParser.ImplTraitTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#implTraitType}.
	 * @param ctx the parse tree
	 */
	void exitImplTraitType(RustParser.ImplTraitTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#implTraitTypeOneBound}.
	 * @param ctx the parse tree
	 */
	void enterImplTraitTypeOneBound(RustParser.ImplTraitTypeOneBoundContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#implTraitTypeOneBound}.
	 * @param ctx the parse tree
	 */
	void exitImplTraitTypeOneBound(RustParser.ImplTraitTypeOneBoundContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#inferredType}.
	 * @param ctx the parse tree
	 */
	void enterInferredType(RustParser.InferredTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#inferredType}.
	 * @param ctx the parse tree
	 */
	void exitInferredType(RustParser.InferredTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#typeParamBounds}.
	 * @param ctx the parse tree
	 */
	void enterTypeParamBounds(RustParser.TypeParamBoundsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#typeParamBounds}.
	 * @param ctx the parse tree
	 */
	void exitTypeParamBounds(RustParser.TypeParamBoundsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#typeParamBound}.
	 * @param ctx the parse tree
	 */
	void enterTypeParamBound(RustParser.TypeParamBoundContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#typeParamBound}.
	 * @param ctx the parse tree
	 */
	void exitTypeParamBound(RustParser.TypeParamBoundContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#traitBound}.
	 * @param ctx the parse tree
	 */
	void enterTraitBound(RustParser.TraitBoundContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#traitBound}.
	 * @param ctx the parse tree
	 */
	void exitTraitBound(RustParser.TraitBoundContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#lifetimeBounds}.
	 * @param ctx the parse tree
	 */
	void enterLifetimeBounds(RustParser.LifetimeBoundsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#lifetimeBounds}.
	 * @param ctx the parse tree
	 */
	void exitLifetimeBounds(RustParser.LifetimeBoundsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#lifetime}.
	 * @param ctx the parse tree
	 */
	void enterLifetime(RustParser.LifetimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#lifetime}.
	 * @param ctx the parse tree
	 */
	void exitLifetime(RustParser.LifetimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#simplePath}.
	 * @param ctx the parse tree
	 */
	void enterSimplePath(RustParser.SimplePathContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#simplePath}.
	 * @param ctx the parse tree
	 */
	void exitSimplePath(RustParser.SimplePathContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#simplePathSegment}.
	 * @param ctx the parse tree
	 */
	void enterSimplePathSegment(RustParser.SimplePathSegmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#simplePathSegment}.
	 * @param ctx the parse tree
	 */
	void exitSimplePathSegment(RustParser.SimplePathSegmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#pathInExpression}.
	 * @param ctx the parse tree
	 */
	void enterPathInExpression(RustParser.PathInExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#pathInExpression}.
	 * @param ctx the parse tree
	 */
	void exitPathInExpression(RustParser.PathInExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#pathExprSegment}.
	 * @param ctx the parse tree
	 */
	void enterPathExprSegment(RustParser.PathExprSegmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#pathExprSegment}.
	 * @param ctx the parse tree
	 */
	void exitPathExprSegment(RustParser.PathExprSegmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#pathIdentSegment}.
	 * @param ctx the parse tree
	 */
	void enterPathIdentSegment(RustParser.PathIdentSegmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#pathIdentSegment}.
	 * @param ctx the parse tree
	 */
	void exitPathIdentSegment(RustParser.PathIdentSegmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#genericArgs}.
	 * @param ctx the parse tree
	 */
	void enterGenericArgs(RustParser.GenericArgsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#genericArgs}.
	 * @param ctx the parse tree
	 */
	void exitGenericArgs(RustParser.GenericArgsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#genericArg}.
	 * @param ctx the parse tree
	 */
	void enterGenericArg(RustParser.GenericArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#genericArg}.
	 * @param ctx the parse tree
	 */
	void exitGenericArg(RustParser.GenericArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#genericArgsConst}.
	 * @param ctx the parse tree
	 */
	void enterGenericArgsConst(RustParser.GenericArgsConstContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#genericArgsConst}.
	 * @param ctx the parse tree
	 */
	void exitGenericArgsConst(RustParser.GenericArgsConstContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#genericArgsLifetimes}.
	 * @param ctx the parse tree
	 */
	void enterGenericArgsLifetimes(RustParser.GenericArgsLifetimesContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#genericArgsLifetimes}.
	 * @param ctx the parse tree
	 */
	void exitGenericArgsLifetimes(RustParser.GenericArgsLifetimesContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#genericArgsTypes}.
	 * @param ctx the parse tree
	 */
	void enterGenericArgsTypes(RustParser.GenericArgsTypesContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#genericArgsTypes}.
	 * @param ctx the parse tree
	 */
	void exitGenericArgsTypes(RustParser.GenericArgsTypesContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#genericArgsBindings}.
	 * @param ctx the parse tree
	 */
	void enterGenericArgsBindings(RustParser.GenericArgsBindingsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#genericArgsBindings}.
	 * @param ctx the parse tree
	 */
	void exitGenericArgsBindings(RustParser.GenericArgsBindingsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#genericArgsBinding}.
	 * @param ctx the parse tree
	 */
	void enterGenericArgsBinding(RustParser.GenericArgsBindingContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#genericArgsBinding}.
	 * @param ctx the parse tree
	 */
	void exitGenericArgsBinding(RustParser.GenericArgsBindingContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#qualifiedPathInExpression}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedPathInExpression(RustParser.QualifiedPathInExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#qualifiedPathInExpression}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedPathInExpression(RustParser.QualifiedPathInExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#qualifiedPathType}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedPathType(RustParser.QualifiedPathTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#qualifiedPathType}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedPathType(RustParser.QualifiedPathTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#qualifiedPathInType}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedPathInType(RustParser.QualifiedPathInTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#qualifiedPathInType}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedPathInType(RustParser.QualifiedPathInTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#typePath}.
	 * @param ctx the parse tree
	 */
	void enterTypePath(RustParser.TypePathContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#typePath}.
	 * @param ctx the parse tree
	 */
	void exitTypePath(RustParser.TypePathContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#typePathSegment}.
	 * @param ctx the parse tree
	 */
	void enterTypePathSegment(RustParser.TypePathSegmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#typePathSegment}.
	 * @param ctx the parse tree
	 */
	void exitTypePathSegment(RustParser.TypePathSegmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#typePathFn}.
	 * @param ctx the parse tree
	 */
	void enterTypePathFn(RustParser.TypePathFnContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#typePathFn}.
	 * @param ctx the parse tree
	 */
	void exitTypePathFn(RustParser.TypePathFnContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#typePathInputs}.
	 * @param ctx the parse tree
	 */
	void enterTypePathInputs(RustParser.TypePathInputsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#typePathInputs}.
	 * @param ctx the parse tree
	 */
	void exitTypePathInputs(RustParser.TypePathInputsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#visibility}.
	 * @param ctx the parse tree
	 */
	void enterVisibility(RustParser.VisibilityContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#visibility}.
	 * @param ctx the parse tree
	 */
	void exitVisibility(RustParser.VisibilityContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(RustParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(RustParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#keyword}.
	 * @param ctx the parse tree
	 */
	void enterKeyword(RustParser.KeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#keyword}.
	 * @param ctx the parse tree
	 */
	void exitKeyword(RustParser.KeywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#macroIdentifierLikeToken}.
	 * @param ctx the parse tree
	 */
	void enterMacroIdentifierLikeToken(RustParser.MacroIdentifierLikeTokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#macroIdentifierLikeToken}.
	 * @param ctx the parse tree
	 */
	void exitMacroIdentifierLikeToken(RustParser.MacroIdentifierLikeTokenContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#macroLiteralToken}.
	 * @param ctx the parse tree
	 */
	void enterMacroLiteralToken(RustParser.MacroLiteralTokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#macroLiteralToken}.
	 * @param ctx the parse tree
	 */
	void exitMacroLiteralToken(RustParser.MacroLiteralTokenContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#macroPunctuationToken}.
	 * @param ctx the parse tree
	 */
	void enterMacroPunctuationToken(RustParser.MacroPunctuationTokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#macroPunctuationToken}.
	 * @param ctx the parse tree
	 */
	void exitMacroPunctuationToken(RustParser.MacroPunctuationTokenContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#shl}.
	 * @param ctx the parse tree
	 */
	void enterShl(RustParser.ShlContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#shl}.
	 * @param ctx the parse tree
	 */
	void exitShl(RustParser.ShlContext ctx);
	/**
	 * Enter a parse tree produced by {@link RustParser#shr}.
	 * @param ctx the parse tree
	 */
	void enterShr(RustParser.ShrContext ctx);
	/**
	 * Exit a parse tree produced by {@link RustParser#shr}.
	 * @param ctx the parse tree
	 */
	void exitShr(RustParser.ShrContext ctx);
}