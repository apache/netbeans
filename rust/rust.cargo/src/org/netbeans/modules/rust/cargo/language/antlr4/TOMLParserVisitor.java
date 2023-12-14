// Generated from java-escape by ANTLR 4.11.1
package org.netbeans.modules.rust.cargo.language.antlr4;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link TOMLParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface TOMLParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link TOMLParser#document}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocument(TOMLParser.DocumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link TOMLParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(TOMLParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link TOMLParser#comment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment(TOMLParser.CommentContext ctx);
	/**
	 * Visit a parse tree produced by {@link TOMLParser#key_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKey_value(TOMLParser.Key_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link TOMLParser#key}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKey(TOMLParser.KeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link TOMLParser#simple_key}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimple_key(TOMLParser.Simple_keyContext ctx);
	/**
	 * Visit a parse tree produced by {@link TOMLParser#unquoted_key}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnquoted_key(TOMLParser.Unquoted_keyContext ctx);
	/**
	 * Visit a parse tree produced by {@link TOMLParser#quoted_key}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuoted_key(TOMLParser.Quoted_keyContext ctx);
	/**
	 * Visit a parse tree produced by {@link TOMLParser#dotted_key}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDotted_key(TOMLParser.Dotted_keyContext ctx);
	/**
	 * Visit a parse tree produced by {@link TOMLParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(TOMLParser.ValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link TOMLParser#string}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitString(TOMLParser.StringContext ctx);
	/**
	 * Visit a parse tree produced by {@link TOMLParser#integer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInteger(TOMLParser.IntegerContext ctx);
	/**
	 * Visit a parse tree produced by {@link TOMLParser#floating_point}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFloating_point(TOMLParser.Floating_pointContext ctx);
	/**
	 * Visit a parse tree produced by {@link TOMLParser#bool_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBool_(TOMLParser.Bool_Context ctx);
	/**
	 * Visit a parse tree produced by {@link TOMLParser#date_time}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDate_time(TOMLParser.Date_timeContext ctx);
	/**
	 * Visit a parse tree produced by {@link TOMLParser#inline_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInline_table(TOMLParser.Inline_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link TOMLParser#inner_array}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInner_array(TOMLParser.Inner_arrayContext ctx);
	/**
	 * Visit a parse tree produced by {@link TOMLParser#inline_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInline_value(TOMLParser.Inline_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link TOMLParser#array_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_(TOMLParser.Array_Context ctx);
	/**
	 * Visit a parse tree produced by {@link TOMLParser#array_values}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_values(TOMLParser.Array_valuesContext ctx);
	/**
	 * Visit a parse tree produced by {@link TOMLParser#comment_or_nl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment_or_nl(TOMLParser.Comment_or_nlContext ctx);
	/**
	 * Visit a parse tree produced by {@link TOMLParser#table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable(TOMLParser.TableContext ctx);
	/**
	 * Visit a parse tree produced by {@link TOMLParser#standard_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStandard_table(TOMLParser.Standard_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link TOMLParser#array_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_table(TOMLParser.Array_tableContext ctx);
}