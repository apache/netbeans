// Generated from BladeAntlrFormatterParser.g4 by ANTLR 4.13.0

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

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link BladeAntlrFormatterParser}.
 */
public interface BladeAntlrFormatterParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link BladeAntlrFormatterParser#file}.
	 * @param ctx the parse tree
	 */
	void enterFile(BladeAntlrFormatterParser.FileContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrFormatterParser#file}.
	 * @param ctx the parse tree
	 */
	void exitFile(BladeAntlrFormatterParser.FileContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrFormatterParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(BladeAntlrFormatterParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrFormatterParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(BladeAntlrFormatterParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrFormatterParser#inline_tag_statement}.
	 * @param ctx the parse tree
	 */
	void enterInline_tag_statement(BladeAntlrFormatterParser.Inline_tag_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrFormatterParser#inline_tag_statement}.
	 * @param ctx the parse tree
	 */
	void exitInline_tag_statement(BladeAntlrFormatterParser.Inline_tag_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrFormatterParser#html_close_tag}.
	 * @param ctx the parse tree
	 */
	void enterHtml_close_tag(BladeAntlrFormatterParser.Html_close_tagContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrFormatterParser#html_close_tag}.
	 * @param ctx the parse tree
	 */
	void exitHtml_close_tag(BladeAntlrFormatterParser.Html_close_tagContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrFormatterParser#html_indent}.
	 * @param ctx the parse tree
	 */
	void enterHtml_indent(BladeAntlrFormatterParser.Html_indentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrFormatterParser#html_indent}.
	 * @param ctx the parse tree
	 */
	void exitHtml_indent(BladeAntlrFormatterParser.Html_indentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrFormatterParser#html_tag}.
	 * @param ctx the parse tree
	 */
	void enterHtml_tag(BladeAntlrFormatterParser.Html_tagContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrFormatterParser#html_tag}.
	 * @param ctx the parse tree
	 */
	void exitHtml_tag(BladeAntlrFormatterParser.Html_tagContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrFormatterParser#self_closed_tag}.
	 * @param ctx the parse tree
	 */
	void enterSelf_closed_tag(BladeAntlrFormatterParser.Self_closed_tagContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrFormatterParser#self_closed_tag}.
	 * @param ctx the parse tree
	 */
	void exitSelf_closed_tag(BladeAntlrFormatterParser.Self_closed_tagContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrFormatterParser#block_start}.
	 * @param ctx the parse tree
	 */
	void enterBlock_start(BladeAntlrFormatterParser.Block_startContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrFormatterParser#block_start}.
	 * @param ctx the parse tree
	 */
	void exitBlock_start(BladeAntlrFormatterParser.Block_startContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrFormatterParser#block_directive_name}.
	 * @param ctx the parse tree
	 */
	void enterBlock_directive_name(BladeAntlrFormatterParser.Block_directive_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrFormatterParser#block_directive_name}.
	 * @param ctx the parse tree
	 */
	void exitBlock_directive_name(BladeAntlrFormatterParser.Block_directive_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrFormatterParser#block_end}.
	 * @param ctx the parse tree
	 */
	void enterBlock_end(BladeAntlrFormatterParser.Block_endContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrFormatterParser#block_end}.
	 * @param ctx the parse tree
	 */
	void exitBlock_end(BladeAntlrFormatterParser.Block_endContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrFormatterParser#block_aligned_directive}.
	 * @param ctx the parse tree
	 */
	void enterBlock_aligned_directive(BladeAntlrFormatterParser.Block_aligned_directiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrFormatterParser#block_aligned_directive}.
	 * @param ctx the parse tree
	 */
	void exitBlock_aligned_directive(BladeAntlrFormatterParser.Block_aligned_directiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrFormatterParser#inline_identable_element}.
	 * @param ctx the parse tree
	 */
	void enterInline_identable_element(BladeAntlrFormatterParser.Inline_identable_elementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrFormatterParser#inline_identable_element}.
	 * @param ctx the parse tree
	 */
	void exitInline_identable_element(BladeAntlrFormatterParser.Inline_identable_elementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrFormatterParser#section_inline}.
	 * @param ctx the parse tree
	 */
	void enterSection_inline(BladeAntlrFormatterParser.Section_inlineContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrFormatterParser#section_inline}.
	 * @param ctx the parse tree
	 */
	void exitSection_inline(BladeAntlrFormatterParser.Section_inlineContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrFormatterParser#section_block}.
	 * @param ctx the parse tree
	 */
	void enterSection_block(BladeAntlrFormatterParser.Section_blockContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrFormatterParser#section_block}.
	 * @param ctx the parse tree
	 */
	void exitSection_block(BladeAntlrFormatterParser.Section_blockContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrFormatterParser#section_block_end}.
	 * @param ctx the parse tree
	 */
	void enterSection_block_end(BladeAntlrFormatterParser.Section_block_endContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrFormatterParser#section_block_end}.
	 * @param ctx the parse tree
	 */
	void exitSection_block_end(BladeAntlrFormatterParser.Section_block_endContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrFormatterParser#section_block_start}.
	 * @param ctx the parse tree
	 */
	void enterSection_block_start(BladeAntlrFormatterParser.Section_block_startContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrFormatterParser#section_block_start}.
	 * @param ctx the parse tree
	 */
	void exitSection_block_start(BladeAntlrFormatterParser.Section_block_startContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrFormatterParser#nl_with_space_before}.
	 * @param ctx the parse tree
	 */
	void enterNl_with_space_before(BladeAntlrFormatterParser.Nl_with_space_beforeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrFormatterParser#nl_with_space_before}.
	 * @param ctx the parse tree
	 */
	void exitNl_with_space_before(BladeAntlrFormatterParser.Nl_with_space_beforeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrFormatterParser#nl_with_space}.
	 * @param ctx the parse tree
	 */
	void enterNl_with_space(BladeAntlrFormatterParser.Nl_with_spaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrFormatterParser#nl_with_space}.
	 * @param ctx the parse tree
	 */
	void exitNl_with_space(BladeAntlrFormatterParser.Nl_with_spaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrFormatterParser#static_element}.
	 * @param ctx the parse tree
	 */
	void enterStatic_element(BladeAntlrFormatterParser.Static_elementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrFormatterParser#static_element}.
	 * @param ctx the parse tree
	 */
	void exitStatic_element(BladeAntlrFormatterParser.Static_elementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeAntlrFormatterParser#blade_echo}.
	 * @param ctx the parse tree
	 */
	void enterBlade_echo(BladeAntlrFormatterParser.Blade_echoContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeAntlrFormatterParser#blade_echo}.
	 * @param ctx the parse tree
	 */
	void exitBlade_echo(BladeAntlrFormatterParser.Blade_echoContext ctx);
}