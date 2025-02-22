// Generated from BladeHtmlAntlrParser.g4 by ANTLR 4.13.0

  /**
   * Parser generated for netbeans blade editor
   * Some elements have been simplified to optimize parser speed
   * For example
   * - switch statement have a loos validation
   * - generic block statement "@isset" | "@unless" are grouped togehter
   * - the start match and end match will be checked in the parser
   */
  package org.netbeans.modules.php.blade.syntax.antlr4.html_components;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link BladeHtmlAntlrParser}.
 */
public interface BladeHtmlAntlrParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link BladeHtmlAntlrParser#root}.
	 * @param ctx the parse tree
	 */
	void enterRoot(BladeHtmlAntlrParser.RootContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeHtmlAntlrParser#root}.
	 * @param ctx the parse tree
	 */
	void exitRoot(BladeHtmlAntlrParser.RootContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladeHtmlAntlrParser#element}.
	 * @param ctx the parse tree
	 */
	void enterElement(BladeHtmlAntlrParser.ElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladeHtmlAntlrParser#element}.
	 * @param ctx the parse tree
	 */
	void exitElement(BladeHtmlAntlrParser.ElementContext ctx);
}