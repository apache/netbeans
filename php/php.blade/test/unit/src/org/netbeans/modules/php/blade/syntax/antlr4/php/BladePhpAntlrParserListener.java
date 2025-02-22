// Generated from BladePhpAntlrParser.g4 by ANTLR 4.13.0

  package org.netbeans.modules.php.blade.syntax.antlr4.php;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link BladePhpAntlrParser}.
 */
public interface BladePhpAntlrParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(BladePhpAntlrParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(BladePhpAntlrParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#exprStatement}.
	 * @param ctx the parse tree
	 */
	void enterExprStatement(BladePhpAntlrParser.ExprStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#exprStatement}.
	 * @param ctx the parse tree
	 */
	void exitExprStatement(BladePhpAntlrParser.ExprStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#logicalStatement}.
	 * @param ctx the parse tree
	 */
	void enterLogicalStatement(BladePhpAntlrParser.LogicalStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#logicalStatement}.
	 * @param ctx the parse tree
	 */
	void exitLogicalStatement(BladePhpAntlrParser.LogicalStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void enterIfStatement(BladePhpAntlrParser.IfStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void exitIfStatement(BladePhpAntlrParser.IfStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#inputExpr}.
	 * @param ctx the parse tree
	 */
	void enterInputExpr(BladePhpAntlrParser.InputExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#inputExpr}.
	 * @param ctx the parse tree
	 */
	void exitInputExpr(BladePhpAntlrParser.InputExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#functionalExpr}.
	 * @param ctx the parse tree
	 */
	void enterFunctionalExpr(BladePhpAntlrParser.FunctionalExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#functionalExpr}.
	 * @param ctx the parse tree
	 */
	void exitFunctionalExpr(BladePhpAntlrParser.FunctionalExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#classExpression}.
	 * @param ctx the parse tree
	 */
	void enterClassExpression(BladePhpAntlrParser.ClassExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#classExpression}.
	 * @param ctx the parse tree
	 */
	void exitClassExpression(BladePhpAntlrParser.ClassExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#foreachDirectiveStatement}.
	 * @param ctx the parse tree
	 */
	void enterForeachDirectiveStatement(BladePhpAntlrParser.ForeachDirectiveStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#foreachDirectiveStatement}.
	 * @param ctx the parse tree
	 */
	void exitForeachDirectiveStatement(BladePhpAntlrParser.ForeachDirectiveStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#foreachArguments}.
	 * @param ctx the parse tree
	 */
	void enterForeachArguments(BladePhpAntlrParser.ForeachArgumentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#foreachArguments}.
	 * @param ctx the parse tree
	 */
	void exitForeachArguments(BladePhpAntlrParser.ForeachArgumentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#classInstanceStatement}.
	 * @param ctx the parse tree
	 */
	void enterClassInstanceStatement(BladePhpAntlrParser.ClassInstanceStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#classInstanceStatement}.
	 * @param ctx the parse tree
	 */
	void exitClassInstanceStatement(BladePhpAntlrParser.ClassInstanceStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#matchStatement}.
	 * @param ctx the parse tree
	 */
	void enterMatchStatement(BladePhpAntlrParser.MatchStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#matchStatement}.
	 * @param ctx the parse tree
	 */
	void exitMatchStatement(BladePhpAntlrParser.MatchStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#staticClassReference}.
	 * @param ctx the parse tree
	 */
	void enterStaticClassReference(BladePhpAntlrParser.StaticClassReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#staticClassReference}.
	 * @param ctx the parse tree
	 */
	void exitStaticClassReference(BladePhpAntlrParser.StaticClassReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#staticMethodAccess}.
	 * @param ctx the parse tree
	 */
	void enterStaticMethodAccess(BladePhpAntlrParser.StaticMethodAccessContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#staticMethodAccess}.
	 * @param ctx the parse tree
	 */
	void exitStaticMethodAccess(BladePhpAntlrParser.StaticMethodAccessContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#staticFieldAccess}.
	 * @param ctx the parse tree
	 */
	void enterStaticFieldAccess(BladePhpAntlrParser.StaticFieldAccessContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#staticFieldAccess}.
	 * @param ctx the parse tree
	 */
	void exitStaticFieldAccess(BladePhpAntlrParser.StaticFieldAccessContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#staticAccess}.
	 * @param ctx the parse tree
	 */
	void enterStaticAccess(BladePhpAntlrParser.StaticAccessContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#staticAccess}.
	 * @param ctx the parse tree
	 */
	void exitStaticAccess(BladePhpAntlrParser.StaticAccessContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#aliasDirectAccess}.
	 * @param ctx the parse tree
	 */
	void enterAliasDirectAccess(BladePhpAntlrParser.AliasDirectAccessContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#aliasDirectAccess}.
	 * @param ctx the parse tree
	 */
	void exitAliasDirectAccess(BladePhpAntlrParser.AliasDirectAccessContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#classMember}.
	 * @param ctx the parse tree
	 */
	void enterClassMember(BladePhpAntlrParser.ClassMemberContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#classMember}.
	 * @param ctx the parse tree
	 */
	void exitClassMember(BladePhpAntlrParser.ClassMemberContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#directMethodAccess}.
	 * @param ctx the parse tree
	 */
	void enterDirectMethodAccess(BladePhpAntlrParser.DirectMethodAccessContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#directMethodAccess}.
	 * @param ctx the parse tree
	 */
	void exitDirectMethodAccess(BladePhpAntlrParser.DirectMethodAccessContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#directAccess}.
	 * @param ctx the parse tree
	 */
	void enterDirectAccess(BladePhpAntlrParser.DirectAccessContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#directAccess}.
	 * @param ctx the parse tree
	 */
	void exitDirectAccess(BladePhpAntlrParser.DirectAccessContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#functionExpr}.
	 * @param ctx the parse tree
	 */
	void enterFunctionExpr(BladePhpAntlrParser.FunctionExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#functionExpr}.
	 * @param ctx the parse tree
	 */
	void exitFunctionExpr(BladePhpAntlrParser.FunctionExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#arguments}.
	 * @param ctx the parse tree
	 */
	void enterArguments(BladePhpAntlrParser.ArgumentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#arguments}.
	 * @param ctx the parse tree
	 */
	void exitArguments(BladePhpAntlrParser.ArgumentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#namespace}.
	 * @param ctx the parse tree
	 */
	void enterNamespace(BladePhpAntlrParser.NamespaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#namespace}.
	 * @param ctx the parse tree
	 */
	void exitNamespace(BladePhpAntlrParser.NamespaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#argument}.
	 * @param ctx the parse tree
	 */
	void enterArgument(BladePhpAntlrParser.ArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#argument}.
	 * @param ctx the parse tree
	 */
	void exitArgument(BladePhpAntlrParser.ArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#array}.
	 * @param ctx the parse tree
	 */
	void enterArray(BladePhpAntlrParser.ArrayContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#array}.
	 * @param ctx the parse tree
	 */
	void exitArray(BladePhpAntlrParser.ArrayContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#array_key_item}.
	 * @param ctx the parse tree
	 */
	void enterArray_key_item(BladePhpAntlrParser.Array_key_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#array_key_item}.
	 * @param ctx the parse tree
	 */
	void exitArray_key_item(BladePhpAntlrParser.Array_key_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#array_child}.
	 * @param ctx the parse tree
	 */
	void enterArray_child(BladePhpAntlrParser.Array_childContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#array_child}.
	 * @param ctx the parse tree
	 */
	void exitArray_child(BladePhpAntlrParser.Array_childContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#varExpr}.
	 * @param ctx the parse tree
	 */
	void enterVarExpr(BladePhpAntlrParser.VarExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#varExpr}.
	 * @param ctx the parse tree
	 */
	void exitVarExpr(BladePhpAntlrParser.VarExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#misc}.
	 * @param ctx the parse tree
	 */
	void enterMisc(BladePhpAntlrParser.MiscContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#misc}.
	 * @param ctx the parse tree
	 */
	void exitMisc(BladePhpAntlrParser.MiscContext ctx);
	/**
	 * Enter a parse tree produced by {@link BladePhpAntlrParser#output}.
	 * @param ctx the parse tree
	 */
	void enterOutput(BladePhpAntlrParser.OutputContext ctx);
	/**
	 * Exit a parse tree produced by {@link BladePhpAntlrParser#output}.
	 * @param ctx the parse tree
	 */
	void exitOutput(BladePhpAntlrParser.OutputContext ctx);
}