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


import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link RustParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface RustParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link RustParser#crate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCrate(RustParser.CrateContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#macroInvocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacroInvocation(RustParser.MacroInvocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#delimTokenTree}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDelimTokenTree(RustParser.DelimTokenTreeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#tokenTree}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTokenTree(RustParser.TokenTreeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#tokenTreeToken}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTokenTreeToken(RustParser.TokenTreeTokenContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#macroInvocationSemi}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacroInvocationSemi(RustParser.MacroInvocationSemiContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#macroRulesDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacroRulesDefinition(RustParser.MacroRulesDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#macroRulesDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacroRulesDef(RustParser.MacroRulesDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#macroRules}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacroRules(RustParser.MacroRulesContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#macroRule}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacroRule(RustParser.MacroRuleContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#macroMatcher}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacroMatcher(RustParser.MacroMatcherContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#macroMatch}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacroMatch(RustParser.MacroMatchContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#macroMatchToken}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacroMatchToken(RustParser.MacroMatchTokenContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#macroFragSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacroFragSpec(RustParser.MacroFragSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#macroRepSep}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacroRepSep(RustParser.MacroRepSepContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#macroRepOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacroRepOp(RustParser.MacroRepOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#macroTranscriber}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacroTranscriber(RustParser.MacroTranscriberContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitItem(RustParser.ItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#visItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVisItem(RustParser.VisItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#macroItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacroItem(RustParser.MacroItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#module}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModule(RustParser.ModuleContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#externCrate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExternCrate(RustParser.ExternCrateContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#crateRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCrateRef(RustParser.CrateRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#asClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsClause(RustParser.AsClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#useDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUseDeclaration(RustParser.UseDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#useTree}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUseTree(RustParser.UseTreeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#function_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_(RustParser.Function_Context ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#functionQualifiers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionQualifiers(RustParser.FunctionQualifiersContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#abi}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAbi(RustParser.AbiContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#functionParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionParameters(RustParser.FunctionParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#selfParam}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelfParam(RustParser.SelfParamContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#shorthandSelf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShorthandSelf(RustParser.ShorthandSelfContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#typedSelf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypedSelf(RustParser.TypedSelfContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#functionParam}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionParam(RustParser.FunctionParamContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#functionParamPattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionParamPattern(RustParser.FunctionParamPatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#functionReturnType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionReturnType(RustParser.FunctionReturnTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#typeAlias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeAlias(RustParser.TypeAliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#struct_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStruct_(RustParser.Struct_Context ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#structStruct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructStruct(RustParser.StructStructContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#tupleStruct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTupleStruct(RustParser.TupleStructContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#structFields}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructFields(RustParser.StructFieldsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#structField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructField(RustParser.StructFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#tupleFields}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTupleFields(RustParser.TupleFieldsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#tupleField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTupleField(RustParser.TupleFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#enumeration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumeration(RustParser.EnumerationContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#enumItems}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumItems(RustParser.EnumItemsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#enumItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumItem(RustParser.EnumItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#enumItemTuple}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumItemTuple(RustParser.EnumItemTupleContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#enumItemStruct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumItemStruct(RustParser.EnumItemStructContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#enumItemDiscriminant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumItemDiscriminant(RustParser.EnumItemDiscriminantContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#union_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnion_(RustParser.Union_Context ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#constantItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantItem(RustParser.ConstantItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#staticItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStaticItem(RustParser.StaticItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#trait_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrait_(RustParser.Trait_Context ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#implementation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImplementation(RustParser.ImplementationContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#inherentImpl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInherentImpl(RustParser.InherentImplContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#traitImpl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTraitImpl(RustParser.TraitImplContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#externBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExternBlock(RustParser.ExternBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#externalItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExternalItem(RustParser.ExternalItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#genericParams}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericParams(RustParser.GenericParamsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#genericParam}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericParam(RustParser.GenericParamContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#lifetimeParam}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLifetimeParam(RustParser.LifetimeParamContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#typeParam}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeParam(RustParser.TypeParamContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#constParam}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstParam(RustParser.ConstParamContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#whereClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhereClause(RustParser.WhereClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#whereClauseItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhereClauseItem(RustParser.WhereClauseItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#lifetimeWhereClauseItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLifetimeWhereClauseItem(RustParser.LifetimeWhereClauseItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#typeBoundWhereClauseItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeBoundWhereClauseItem(RustParser.TypeBoundWhereClauseItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#forLifetimes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForLifetimes(RustParser.ForLifetimesContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#associatedItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssociatedItem(RustParser.AssociatedItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#innerAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInnerAttribute(RustParser.InnerAttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#outerAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOuterAttribute(RustParser.OuterAttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#attr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttr(RustParser.AttrContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#attrInput}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttrInput(RustParser.AttrInputContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(RustParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#letStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLetStatement(RustParser.LetStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#expressionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionStatement(RustParser.ExpressionStatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code TypeCastExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeCastExpression(RustParser.TypeCastExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code PathExpression_}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPathExpression_(RustParser.PathExpression_Context ctx);
	/**
	 * Visit a parse tree produced by the {@code TupleExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTupleExpression(RustParser.TupleExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code IndexExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexExpression(RustParser.IndexExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code RangeExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRangeExpression(RustParser.RangeExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MacroInvocationAsExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacroInvocationAsExpression(RustParser.MacroInvocationAsExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ReturnExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnExpression(RustParser.ReturnExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AwaitExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAwaitExpression(RustParser.AwaitExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ErrorPropagationExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitErrorPropagationExpression(RustParser.ErrorPropagationExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ContinueExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContinueExpression(RustParser.ContinueExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AssignmentExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentExpression(RustParser.AssignmentExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MethodCallExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodCallExpression(RustParser.MethodCallExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code LiteralExpression_}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteralExpression_(RustParser.LiteralExpression_Context ctx);
	/**
	 * Visit a parse tree produced by the {@code StructExpression_}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructExpression_(RustParser.StructExpression_Context ctx);
	/**
	 * Visit a parse tree produced by the {@code TupleIndexingExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTupleIndexingExpression(RustParser.TupleIndexingExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NegationExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNegationExpression(RustParser.NegationExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code CallExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCallExpression(RustParser.CallExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code LazyBooleanExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLazyBooleanExpression(RustParser.LazyBooleanExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code DereferenceExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDereferenceExpression(RustParser.DereferenceExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExpressionWithBlock_}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionWithBlock_(RustParser.ExpressionWithBlock_Context ctx);
	/**
	 * Visit a parse tree produced by the {@code GroupedExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupedExpression(RustParser.GroupedExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code BreakExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBreakExpression(RustParser.BreakExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ArithmeticOrLogicalExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArithmeticOrLogicalExpression(RustParser.ArithmeticOrLogicalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FieldExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldExpression(RustParser.FieldExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code EnumerationVariantExpression_}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumerationVariantExpression_(RustParser.EnumerationVariantExpression_Context ctx);
	/**
	 * Visit a parse tree produced by the {@code ComparisonExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonExpression(RustParser.ComparisonExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AttributedExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributedExpression(RustParser.AttributedExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code BorrowExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBorrowExpression(RustParser.BorrowExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code CompoundAssignmentExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompoundAssignmentExpression(RustParser.CompoundAssignmentExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ClosureExpression_}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClosureExpression_(RustParser.ClosureExpression_Context ctx);
	/**
	 * Visit a parse tree produced by the {@code ArrayExpression}
	 * labeled alternative in {@link RustParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayExpression(RustParser.ArrayExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#shiftOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShiftOperator(RustParser.ShiftOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperator(RustParser.ComparisonOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#compoundAssignOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompoundAssignOperator(RustParser.CompoundAssignOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#expressionWithBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionWithBlock(RustParser.ExpressionWithBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#literalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteralExpression(RustParser.LiteralExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#pathExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPathExpression(RustParser.PathExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#blockExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockExpression(RustParser.BlockExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#statements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatements(RustParser.StatementsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#asyncBlockExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsyncBlockExpression(RustParser.AsyncBlockExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#unsafeBlockExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnsafeBlockExpression(RustParser.UnsafeBlockExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#arrayElements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayElements(RustParser.ArrayElementsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#tupleElements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTupleElements(RustParser.TupleElementsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#tupleIndex}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTupleIndex(RustParser.TupleIndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#structExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructExpression(RustParser.StructExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#structExprStruct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructExprStruct(RustParser.StructExprStructContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#structExprFields}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructExprFields(RustParser.StructExprFieldsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#structExprField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructExprField(RustParser.StructExprFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#structBase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructBase(RustParser.StructBaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#structExprTuple}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructExprTuple(RustParser.StructExprTupleContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#structExprUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructExprUnit(RustParser.StructExprUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#enumerationVariantExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumerationVariantExpression(RustParser.EnumerationVariantExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#enumExprStruct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumExprStruct(RustParser.EnumExprStructContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#enumExprFields}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumExprFields(RustParser.EnumExprFieldsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#enumExprField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumExprField(RustParser.EnumExprFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#enumExprTuple}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumExprTuple(RustParser.EnumExprTupleContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#enumExprFieldless}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumExprFieldless(RustParser.EnumExprFieldlessContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#callParams}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCallParams(RustParser.CallParamsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#closureExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClosureExpression(RustParser.ClosureExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#closureParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClosureParameters(RustParser.ClosureParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#closureParam}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClosureParam(RustParser.ClosureParamContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#loopExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopExpression(RustParser.LoopExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#infiniteLoopExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInfiniteLoopExpression(RustParser.InfiniteLoopExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#predicateLoopExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicateLoopExpression(RustParser.PredicateLoopExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#predicatePatternLoopExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicatePatternLoopExpression(RustParser.PredicatePatternLoopExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#iteratorLoopExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIteratorLoopExpression(RustParser.IteratorLoopExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#loopLabel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopLabel(RustParser.LoopLabelContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#ifExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfExpression(RustParser.IfExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#ifLetExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfLetExpression(RustParser.IfLetExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#matchExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchExpression(RustParser.MatchExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#matchArms}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchArms(RustParser.MatchArmsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#matchArmExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchArmExpression(RustParser.MatchArmExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#matchArm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchArm(RustParser.MatchArmContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#matchArmGuard}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchArmGuard(RustParser.MatchArmGuardContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#pattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPattern(RustParser.PatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#patternNoTopAlt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPatternNoTopAlt(RustParser.PatternNoTopAltContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#patternWithoutRange}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPatternWithoutRange(RustParser.PatternWithoutRangeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#literalPattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteralPattern(RustParser.LiteralPatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#identifierPattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierPattern(RustParser.IdentifierPatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#wildcardPattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWildcardPattern(RustParser.WildcardPatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#restPattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRestPattern(RustParser.RestPatternContext ctx);
	/**
	 * Visit a parse tree produced by the {@code InclusiveRangePattern}
	 * labeled alternative in {@link RustParser#rangePattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInclusiveRangePattern(RustParser.InclusiveRangePatternContext ctx);
	/**
	 * Visit a parse tree produced by the {@code HalfOpenRangePattern}
	 * labeled alternative in {@link RustParser#rangePattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHalfOpenRangePattern(RustParser.HalfOpenRangePatternContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ObsoleteRangePattern}
	 * labeled alternative in {@link RustParser#rangePattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObsoleteRangePattern(RustParser.ObsoleteRangePatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#rangePatternBound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRangePatternBound(RustParser.RangePatternBoundContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#referencePattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReferencePattern(RustParser.ReferencePatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#structPattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructPattern(RustParser.StructPatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#structPatternElements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructPatternElements(RustParser.StructPatternElementsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#structPatternFields}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructPatternFields(RustParser.StructPatternFieldsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#structPatternField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructPatternField(RustParser.StructPatternFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#structPatternEtCetera}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructPatternEtCetera(RustParser.StructPatternEtCeteraContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#tupleStructPattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTupleStructPattern(RustParser.TupleStructPatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#tupleStructItems}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTupleStructItems(RustParser.TupleStructItemsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#tuplePattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTuplePattern(RustParser.TuplePatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#tuplePatternItems}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTuplePatternItems(RustParser.TuplePatternItemsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#groupedPattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupedPattern(RustParser.GroupedPatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#slicePattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSlicePattern(RustParser.SlicePatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#slicePatternItems}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSlicePatternItems(RustParser.SlicePatternItemsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#pathPattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPathPattern(RustParser.PathPatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#type_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_(RustParser.Type_Context ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#typeNoBounds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeNoBounds(RustParser.TypeNoBoundsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#parenthesizedType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenthesizedType(RustParser.ParenthesizedTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#neverType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNeverType(RustParser.NeverTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#tupleType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTupleType(RustParser.TupleTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#arrayType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayType(RustParser.ArrayTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#sliceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSliceType(RustParser.SliceTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#referenceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReferenceType(RustParser.ReferenceTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#rawPointerType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRawPointerType(RustParser.RawPointerTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#bareFunctionType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBareFunctionType(RustParser.BareFunctionTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#functionTypeQualifiers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionTypeQualifiers(RustParser.FunctionTypeQualifiersContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#bareFunctionReturnType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBareFunctionReturnType(RustParser.BareFunctionReturnTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#functionParametersMaybeNamedVariadic}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionParametersMaybeNamedVariadic(RustParser.FunctionParametersMaybeNamedVariadicContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#maybeNamedFunctionParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMaybeNamedFunctionParameters(RustParser.MaybeNamedFunctionParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#maybeNamedParam}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMaybeNamedParam(RustParser.MaybeNamedParamContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#maybeNamedFunctionParametersVariadic}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMaybeNamedFunctionParametersVariadic(RustParser.MaybeNamedFunctionParametersVariadicContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#traitObjectType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTraitObjectType(RustParser.TraitObjectTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#traitObjectTypeOneBound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTraitObjectTypeOneBound(RustParser.TraitObjectTypeOneBoundContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#implTraitType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImplTraitType(RustParser.ImplTraitTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#implTraitTypeOneBound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImplTraitTypeOneBound(RustParser.ImplTraitTypeOneBoundContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#inferredType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInferredType(RustParser.InferredTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#typeParamBounds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeParamBounds(RustParser.TypeParamBoundsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#typeParamBound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeParamBound(RustParser.TypeParamBoundContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#traitBound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTraitBound(RustParser.TraitBoundContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#lifetimeBounds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLifetimeBounds(RustParser.LifetimeBoundsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#lifetime}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLifetime(RustParser.LifetimeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#simplePath}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimplePath(RustParser.SimplePathContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#simplePathSegment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimplePathSegment(RustParser.SimplePathSegmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#pathInExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPathInExpression(RustParser.PathInExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#pathExprSegment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPathExprSegment(RustParser.PathExprSegmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#pathIdentSegment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPathIdentSegment(RustParser.PathIdentSegmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#genericArgs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericArgs(RustParser.GenericArgsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#genericArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericArg(RustParser.GenericArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#genericArgsConst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericArgsConst(RustParser.GenericArgsConstContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#genericArgsLifetimes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericArgsLifetimes(RustParser.GenericArgsLifetimesContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#genericArgsTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericArgsTypes(RustParser.GenericArgsTypesContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#genericArgsBindings}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericArgsBindings(RustParser.GenericArgsBindingsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#genericArgsBinding}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenericArgsBinding(RustParser.GenericArgsBindingContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#qualifiedPathInExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedPathInExpression(RustParser.QualifiedPathInExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#qualifiedPathType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedPathType(RustParser.QualifiedPathTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#qualifiedPathInType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedPathInType(RustParser.QualifiedPathInTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#typePath}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypePath(RustParser.TypePathContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#typePathSegment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypePathSegment(RustParser.TypePathSegmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#typePathFn}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypePathFn(RustParser.TypePathFnContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#typePathInputs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypePathInputs(RustParser.TypePathInputsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#visibility}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVisibility(RustParser.VisibilityContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(RustParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#keyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyword(RustParser.KeywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#macroIdentifierLikeToken}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacroIdentifierLikeToken(RustParser.MacroIdentifierLikeTokenContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#macroLiteralToken}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacroLiteralToken(RustParser.MacroLiteralTokenContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#macroPunctuationToken}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMacroPunctuationToken(RustParser.MacroPunctuationTokenContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#shl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShl(RustParser.ShlContext ctx);
	/**
	 * Visit a parse tree produced by {@link RustParser#shr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShr(RustParser.ShrContext ctx);
}