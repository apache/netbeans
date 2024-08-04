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
package org.netbeans.modules.php.blade.syntax.antlr4.v10;

/**
 * Parser generated for netbeans blade editor
 * Some elements have been simplified to optimize parser speed
 * For example
 * - switch statement have a loos validation
 * - generic block statement "@isset" | "@unless" are grouped togehter
 * - the start match and end match will be checked in the parser
 */

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link BladeAntlrParser}.
 */
public interface BladeAntlrParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#file}.
	 * @param ctx the parse tree
	 */
	void enterFile(BladeAntlrParser.FileContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#file}.
	 * @param ctx the parse tree
	 */
	void exitFile(BladeAntlrParser.FileContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#general_statement}.
	 * @param ctx the parse tree
	 */
	void enterGeneral_statement(BladeAntlrParser.General_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#general_statement}.
	 * @param ctx the parse tree
	 */
	void exitGeneral_statement(BladeAntlrParser.General_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#inline_statement}.
	 * @param ctx the parse tree
	 */
	void enterInline_statement(BladeAntlrParser.Inline_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#inline_statement}.
	 * @param ctx the parse tree
	 */
	void exitInline_statement(BladeAntlrParser.Inline_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#inline_directive}.
	 * @param ctx the parse tree
	 */
	void enterInline_directive(BladeAntlrParser.Inline_directiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#inline_directive}.
	 * @param ctx the parse tree
	 */
	void exitInline_directive(BladeAntlrParser.Inline_directiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#block_statement}.
	 * @param ctx the parse tree
	 */
	void enterBlock_statement(BladeAntlrParser.Block_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#block_statement}.
	 * @param ctx the parse tree
	 */
	void exitBlock_statement(BladeAntlrParser.Block_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#identifiableType}.
	 * @param ctx the parse tree
	 */
	void enterIdentifiableType(BladeAntlrParser.IdentifiableTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#identifiableType}.
	 * @param ctx the parse tree
	 */
	void exitIdentifiableType(BladeAntlrParser.IdentifiableTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#section_inline}.
	 * @param ctx the parse tree
	 */
	void enterSection_inline(BladeAntlrParser.Section_inlineContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#section_inline}.
	 * @param ctx the parse tree
	 */
	void exitSection_inline(BladeAntlrParser.Section_inlineContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#section}.
	 * @param ctx the parse tree
	 */
	void enterSection(BladeAntlrParser.SectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#section}.
	 * @param ctx the parse tree
	 */
	void exitSection(BladeAntlrParser.SectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#push}.
	 * @param ctx the parse tree
	 */
	void enterPush(BladeAntlrParser.PushContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#push}.
	 * @param ctx the parse tree
	 */
	void exitPush(BladeAntlrParser.PushContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#pushOnce}.
	 * @param ctx the parse tree
	 */
	void enterPushOnce(BladeAntlrParser.PushOnceContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#pushOnce}.
	 * @param ctx the parse tree
	 */
	void exitPushOnce(BladeAntlrParser.PushOnceContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#pushIf}.
	 * @param ctx the parse tree
	 */
	void enterPushIf(BladeAntlrParser.PushIfContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#pushIf}.
	 * @param ctx the parse tree
	 */
	void exitPushIf(BladeAntlrParser.PushIfContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#prepend}.
	 * @param ctx the parse tree
	 */
	void enterPrepend(BladeAntlrParser.PrependContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#prepend}.
	 * @param ctx the parse tree
	 */
	void exitPrepend(BladeAntlrParser.PrependContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#fragmentD}.
	 * @param ctx the parse tree
	 */
	void enterFragmentD(BladeAntlrParser.FragmentDContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#fragmentD}.
	 * @param ctx the parse tree
	 */
	void exitFragmentD(BladeAntlrParser.FragmentDContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#if}.
	 * @param ctx the parse tree
	 */
	void enterIf(BladeAntlrParser.IfContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#if}.
	 * @param ctx the parse tree
	 */
	void exitIf(BladeAntlrParser.IfContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#else}.
	 * @param ctx the parse tree
	 */
	void enterElse(BladeAntlrParser.ElseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#else}.
	 * @param ctx the parse tree
	 */
	void exitElse(BladeAntlrParser.ElseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#endif}.
	 * @param ctx the parse tree
	 */
	void enterEndif(BladeAntlrParser.EndifContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#endif}.
	 * @param ctx the parse tree
	 */
	void exitEndif(BladeAntlrParser.EndifContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#empty_block}.
	 * @param ctx the parse tree
	 */
	void enterEmpty_block(BladeAntlrParser.Empty_blockContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#empty_block}.
	 * @param ctx the parse tree
	 */
	void exitEmpty_block(BladeAntlrParser.Empty_blockContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#conditional_block}.
	 * @param ctx the parse tree
	 */
	void enterConditional_block(BladeAntlrParser.Conditional_blockContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#conditional_block}.
	 * @param ctx the parse tree
	 */
	void exitConditional_block(BladeAntlrParser.Conditional_blockContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#auth_block}.
	 * @param ctx the parse tree
	 */
	void enterAuth_block(BladeAntlrParser.Auth_blockContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#auth_block}.
	 * @param ctx the parse tree
	 */
	void exitAuth_block(BladeAntlrParser.Auth_blockContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#env_block}.
	 * @param ctx the parse tree
	 */
	void enterEnv_block(BladeAntlrParser.Env_blockContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#env_block}.
	 * @param ctx the parse tree
	 */
	void exitEnv_block(BladeAntlrParser.Env_blockContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#permission}.
	 * @param ctx the parse tree
	 */
	void enterPermission(BladeAntlrParser.PermissionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#permission}.
	 * @param ctx the parse tree
	 */
	void exitPermission(BladeAntlrParser.PermissionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#simple_conditional_stm}.
	 * @param ctx the parse tree
	 */
	void enterSimple_conditional_stm(BladeAntlrParser.Simple_conditional_stmContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#simple_conditional_stm}.
	 * @param ctx the parse tree
	 */
	void exitSimple_conditional_stm(BladeAntlrParser.Simple_conditional_stmContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#error_block}.
	 * @param ctx the parse tree
	 */
	void enterError_block(BladeAntlrParser.Error_blockContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#error_block}.
	 * @param ctx the parse tree
	 */
	void exitError_block(BladeAntlrParser.Error_blockContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#switch}.
	 * @param ctx the parse tree
	 */
	void enterSwitch(BladeAntlrParser.SwitchContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#switch}.
	 * @param ctx the parse tree
	 */
	void exitSwitch(BladeAntlrParser.SwitchContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#while}.
	 * @param ctx the parse tree
	 */
	void enterWhile(BladeAntlrParser.WhileContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#while}.
	 * @param ctx the parse tree
	 */
	void exitWhile(BladeAntlrParser.WhileContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#for}.
	 * @param ctx the parse tree
	 */
	void enterFor(BladeAntlrParser.ForContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#for}.
	 * @param ctx the parse tree
	 */
	void exitFor(BladeAntlrParser.ForContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#foreach}.
	 * @param ctx the parse tree
	 */
	void enterForeach(BladeAntlrParser.ForeachContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#foreach}.
	 * @param ctx the parse tree
	 */
	void exitForeach(BladeAntlrParser.ForeachContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#forelse}.
	 * @param ctx the parse tree
	 */
	void enterForelse(BladeAntlrParser.ForelseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#forelse}.
	 * @param ctx the parse tree
	 */
	void exitForelse(BladeAntlrParser.ForelseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#session}.
	 * @param ctx the parse tree
	 */
	void enterSession(BladeAntlrParser.SessionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#session}.
	 * @param ctx the parse tree
	 */
	void exitSession(BladeAntlrParser.SessionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#stack}.
	 * @param ctx the parse tree
	 */
	void enterStack(BladeAntlrParser.StackContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#stack}.
	 * @param ctx the parse tree
	 */
	void exitStack(BladeAntlrParser.StackContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#asset_bundler}.
	 * @param ctx the parse tree
	 */
	void enterAsset_bundler(BladeAntlrParser.Asset_bundlerContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#asset_bundler}.
	 * @param ctx the parse tree
	 */
	void exitAsset_bundler(BladeAntlrParser.Asset_bundlerContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#inject}.
	 * @param ctx the parse tree
	 */
	void enterInject(BladeAntlrParser.InjectContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#inject}.
	 * @param ctx the parse tree
	 */
	void exitInject(BladeAntlrParser.InjectContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#includeCond}.
	 * @param ctx the parse tree
	 */
	void enterIncludeCond(BladeAntlrParser.IncludeCondContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#includeCond}.
	 * @param ctx the parse tree
	 */
	void exitIncludeCond(BladeAntlrParser.IncludeCondContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#each}.
	 * @param ctx the parse tree
	 */
	void enterEach(BladeAntlrParser.EachContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#each}.
	 * @param ctx the parse tree
	 */
	void exitEach(BladeAntlrParser.EachContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#once_block}.
	 * @param ctx the parse tree
	 */
	void enterOnce_block(BladeAntlrParser.Once_blockContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#once_block}.
	 * @param ctx the parse tree
	 */
	void exitOnce_block(BladeAntlrParser.Once_blockContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#condSection}.
	 * @param ctx the parse tree
	 */
	void enterCondSection(BladeAntlrParser.CondSectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#condSection}.
	 * @param ctx the parse tree
	 */
	void exitCondSection(BladeAntlrParser.CondSectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#custom_directive}.
	 * @param ctx the parse tree
	 */
	void enterCustom_directive(BladeAntlrParser.Custom_directiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#custom_directive}.
	 * @param ctx the parse tree
	 */
	void exitCustom_directive(BladeAntlrParser.Custom_directiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#possibleDirective}.
	 * @param ctx the parse tree
	 */
	void enterPossibleDirective(BladeAntlrParser.PossibleDirectiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#possibleDirective}.
	 * @param ctx the parse tree
	 */
	void exitPossibleDirective(BladeAntlrParser.PossibleDirectiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#php_blade}.
	 * @param ctx the parse tree
	 */
	void enterPhp_blade(BladeAntlrParser.Php_bladeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#php_blade}.
	 * @param ctx the parse tree
	 */
	void exitPhp_blade(BladeAntlrParser.Php_bladeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#phpInline}.
	 * @param ctx the parse tree
	 */
	void enterPhpInline(BladeAntlrParser.PhpInlineContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#phpInline}.
	 * @param ctx the parse tree
	 */
	void exitPhpInline(BladeAntlrParser.PhpInlineContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#regular_echo}.
	 * @param ctx the parse tree
	 */
	void enterRegular_echo(BladeAntlrParser.Regular_echoContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#regular_echo}.
	 * @param ctx the parse tree
	 */
	void exitRegular_echo(BladeAntlrParser.Regular_echoContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#raw_echo}.
	 * @param ctx the parse tree
	 */
	void enterRaw_echo(BladeAntlrParser.Raw_echoContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#raw_echo}.
	 * @param ctx the parse tree
	 */
	void exitRaw_echo(BladeAntlrParser.Raw_echoContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#echo_expr}.
	 * @param ctx the parse tree
	 */
	void enterEcho_expr(BladeAntlrParser.Echo_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#echo_expr}.
	 * @param ctx the parse tree
	 */
	void exitEcho_expr(BladeAntlrParser.Echo_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#class_expr_usage}.
	 * @param ctx the parse tree
	 */
	void enterClass_expr_usage(BladeAntlrParser.Class_expr_usageContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#class_expr_usage}.
	 * @param ctx the parse tree
	 */
	void exitClass_expr_usage(BladeAntlrParser.Class_expr_usageContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#object_alias_static_access}.
	 * @param ctx the parse tree
	 */
	void enterObject_alias_static_access(BladeAntlrParser.Object_alias_static_accessContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#object_alias_static_access}.
	 * @param ctx the parse tree
	 */
	void exitObject_alias_static_access(BladeAntlrParser.Object_alias_static_accessContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#object_alias_direct_access}.
	 * @param ctx the parse tree
	 */
	void enterObject_alias_direct_access(BladeAntlrParser.Object_alias_direct_accessContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#object_alias_direct_access}.
	 * @param ctx the parse tree
	 */
	void exitObject_alias_direct_access(BladeAntlrParser.Object_alias_direct_accessContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#static_direct_class_access}.
	 * @param ctx the parse tree
	 */
	void enterStatic_direct_class_access(BladeAntlrParser.Static_direct_class_accessContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#static_direct_class_access}.
	 * @param ctx the parse tree
	 */
	void exitStatic_direct_class_access(BladeAntlrParser.Static_direct_class_accessContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#static_direct_namespace_class_access}.
	 * @param ctx the parse tree
	 */
	void enterStatic_direct_namespace_class_access(BladeAntlrParser.Static_direct_namespace_class_accessContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#static_direct_namespace_class_access}.
	 * @param ctx the parse tree
	 */
	void exitStatic_direct_namespace_class_access(BladeAntlrParser.Static_direct_namespace_class_accessContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#class_instance}.
	 * @param ctx the parse tree
	 */
	void enterClass_instance(BladeAntlrParser.Class_instanceContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#class_instance}.
	 * @param ctx the parse tree
	 */
	void exitClass_instance(BladeAntlrParser.Class_instanceContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#class_name_reference}.
	 * @param ctx the parse tree
	 */
	void enterClass_name_reference(BladeAntlrParser.Class_name_referenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#class_name_reference}.
	 * @param ctx the parse tree
	 */
	void exitClass_name_reference(BladeAntlrParser.Class_name_referenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#namespacePath}.
	 * @param ctx the parse tree
	 */
	void enterNamespacePath(BladeAntlrParser.NamespacePathContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#namespacePath}.
	 * @param ctx the parse tree
	 */
	void exitNamespacePath(BladeAntlrParser.NamespacePathContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#function_call}.
	 * @param ctx the parse tree
	 */
	void enterFunction_call(BladeAntlrParser.Function_callContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#function_call}.
	 * @param ctx the parse tree
	 */
	void exitFunction_call(BladeAntlrParser.Function_callContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#php_expression}.
	 * @param ctx the parse tree
	 */
	void enterPhp_expression(BladeAntlrParser.Php_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#php_expression}.
	 * @param ctx the parse tree
	 */
	void exitPhp_expression(BladeAntlrParser.Php_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#loop_expression}.
	 * @param ctx the parse tree
	 */
	void enterLoop_expression(BladeAntlrParser.Loop_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#loop_expression}.
	 * @param ctx the parse tree
	 */
	void exitLoop_expression(BladeAntlrParser.Loop_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#main_php_expression}.
	 * @param ctx the parse tree
	 */
	void enterMain_php_expression(BladeAntlrParser.Main_php_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#main_php_expression}.
	 * @param ctx the parse tree
	 */
	void exitMain_php_expression(BladeAntlrParser.Main_php_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#composed_php_expression}.
	 * @param ctx the parse tree
	 */
	void enterComposed_php_expression(BladeAntlrParser.Composed_php_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#composed_php_expression}.
	 * @param ctx the parse tree
	 */
	void exitComposed_php_expression(BladeAntlrParser.Composed_php_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#simple_foreach_expr}.
	 * @param ctx the parse tree
	 */
	void enterSimple_foreach_expr(BladeAntlrParser.Simple_foreach_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#simple_foreach_expr}.
	 * @param ctx the parse tree
	 */
	void exitSimple_foreach_expr(BladeAntlrParser.Simple_foreach_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#singleArgWrapperP}.
	 * @param ctx the parse tree
	 */
	void enterSingleArgWrapperP(BladeAntlrParser.SingleArgWrapperPContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#singleArgWrapperP}.
	 * @param ctx the parse tree
	 */
	void exitSingleArgWrapperP(BladeAntlrParser.SingleArgWrapperPContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#doubleArgWrapperP}.
	 * @param ctx the parse tree
	 */
	void enterDoubleArgWrapperP(BladeAntlrParser.DoubleArgWrapperPContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#doubleArgWrapperP}.
	 * @param ctx the parse tree
	 */
	void exitDoubleArgWrapperP(BladeAntlrParser.DoubleArgWrapperPContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#multiArgWrapper}.
	 * @param ctx the parse tree
	 */
	void enterMultiArgWrapper(BladeAntlrParser.MultiArgWrapperContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#multiArgWrapper}.
	 * @param ctx the parse tree
	 */
	void exitMultiArgWrapper(BladeAntlrParser.MultiArgWrapperContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#identifiableArgument}.
	 * @param ctx the parse tree
	 */
	void enterIdentifiableArgument(BladeAntlrParser.IdentifiableArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#identifiableArgument}.
	 * @param ctx the parse tree
	 */
	void exitIdentifiableArgument(BladeAntlrParser.IdentifiableArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#composedArgument}.
	 * @param ctx the parse tree
	 */
	void enterComposedArgument(BladeAntlrParser.ComposedArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#composedArgument}.
	 * @param ctx the parse tree
	 */
	void exitComposedArgument(BladeAntlrParser.ComposedArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#phpExpr}.
	 * @param ctx the parse tree
	 */
	void enterPhpExpr(BladeAntlrParser.PhpExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#phpExpr}.
	 * @param ctx the parse tree
	 */
	void exitPhpExpr(BladeAntlrParser.PhpExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#identifiableArray}.
	 * @param ctx the parse tree
	 */
	void enterIdentifiableArray(BladeAntlrParser.IdentifiableArrayContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#identifiableArray}.
	 * @param ctx the parse tree
	 */
	void exitIdentifiableArray(BladeAntlrParser.IdentifiableArrayContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#arrayDefine}.
	 * @param ctx the parse tree
	 */
	void enterArrayDefine(BladeAntlrParser.ArrayDefineContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#arrayDefine}.
	 * @param ctx the parse tree
	 */
	void exitArrayDefine(BladeAntlrParser.ArrayDefineContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#paramAssign}.
	 * @param ctx the parse tree
	 */
	void enterParamAssign(BladeAntlrParser.ParamAssignContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#paramAssign}.
	 * @param ctx the parse tree
	 */
	void exitParamAssign(BladeAntlrParser.ParamAssignContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#verbatim_block}.
	 * @param ctx the parse tree
	 */
	void enterVerbatim_block(BladeAntlrParser.Verbatim_blockContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#verbatim_block}.
	 * @param ctx the parse tree
	 */
	void exitVerbatim_block(BladeAntlrParser.Verbatim_blockContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#loop_action}.
	 * @param ctx the parse tree
	 */
	void enterLoop_action(BladeAntlrParser.Loop_actionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#loop_action}.
	 * @param ctx the parse tree
	 */
	void exitLoop_action(BladeAntlrParser.Loop_actionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrParser#html}.
	 * @param ctx the parse tree
	 */
	void enterHtml(BladeAntlrParser.HtmlContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrParser#html}.
	 * @param ctx the parse tree
	 */
	void exitHtml(BladeAntlrParser.HtmlContext ctx);
}