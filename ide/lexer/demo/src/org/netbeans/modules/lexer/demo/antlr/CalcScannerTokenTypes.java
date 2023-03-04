/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.lexer.demo.antlr;

public interface CalcScannerTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int WHITESPACE = 4;
	int PLUS = 5;
	int MINUS = 6;
	int MUL = 7;
	int DIV = 8;
	int LPAREN = 9;
	int RPAREN = 10;
	int ABC = 11;
	int CONSTANT = 12;
	int ML_COMMENT = 13;
	int FLOAT = 14;
	int INTEGER = 15;
	int DIGIT = 16;
	int INCOMPLETE_ML_COMMENT = 17;
}
