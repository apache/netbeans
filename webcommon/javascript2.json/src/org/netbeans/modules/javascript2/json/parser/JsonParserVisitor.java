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
package org.netbeans.modules.javascript2.json.parser;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link JsonParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface JsonParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link JsonParser#json}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson(JsonParser.JsonContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(JsonParser.ValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonParser#object}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObject(JsonParser.ObjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonParser#pair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPair(JsonParser.PairContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonParser#key}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKey(JsonParser.KeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonParser#array}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray(JsonParser.ArrayContext ctx);
}