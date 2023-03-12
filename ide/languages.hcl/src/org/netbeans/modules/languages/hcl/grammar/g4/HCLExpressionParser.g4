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
parser grammar HCLExpressionParser;
options { tokenVocab = HCLLexer; }

expression
    : exprTerm
    | operation
    | <assoc=right> expression QUESTION expression COLON expression
    ;

exprTerm
    : LPAREN expression RPAREN
    | literalValue
    | collectionValue
    | templateExpr
    | variableExpr
    | functionCall
    | forExpr
    | exprTerm index
    | exprTerm getAttr
    | exprTerm splat
    | literalValue
    ;

literalValue
    : NUMERIC_LIT
    | BOOL_LIT
    | NULL
    ;

collectionValue
    : tuple
    | object
    ;

tuple
    : LBRACK expression (COMMA expression)* COMMA? RBRACK
    | LBRACK RBRACK
    ;

object
    : LBRACE objectElem (COMMA objectElem)* COMMA? RBRACE
    | LBRACE RBRACE
    ;

objectElem
    : (IDENTIFIER | expression) (EQUAL | COLON) expression
    ;

templateExpr
    : quotedTemplate
    | heredocTemplate
    ;

quotedTemplate
    : QUOTE QUOTE
    | QUOTE (stringContent | interpolation | template)+ QUOTE
    ;

stringContent
    : STRING_CONTENT+
    ;

interpolation
    : INTERPOLATION_START INTERPOLATION_CONTENT* INTERPOLATION_END
    ;

template
    : TEMPLATE_START TEMPLATE_CONTENT* TEMPLATE_END
    ;

heredocTemplate
    : HEREDOC_START HEREDOC_CONTENT* HEREDOC_END
    ;

variableExpr
    : IDENTIFIER
    ;

functionCall
    : IDENTIFIER LPAREN arguments RPAREN
    | IDENTIFIER LPAREN RPAREN
    ;

arguments
    :  expression (COMMA expression)* (COMMA | ELLIPSIS)?
    ;

forExpr
    : forTupleExpr
    | forObjectExpr
    ;

forTupleExpr
    : LBRACK forIntro expression forCond? RBRACK
    ;

forObjectExpr
    : LBRACE forIntro expression RARROW expression ELLIPSIS? forCond? RBRACE
    ;

forIntro
    : FOR IDENTIFIER (COMMA IDENTIFIER)? IN expression COLON
    ;

forCond
    : IF expression
    ;

index
    : LBRACK expression RBRACK
    | LEGACY_INDEX
    ;

getAttr
    : DOT IDENTIFIER
    ;

splat
    : attrSplat
    | fullSplat
    ;

attrSplat
    : DOT STAR getAttr*
    ;

fullSplat
    : LBRACK STAR RBRACK (getAttr | index)*
    ;

operation
    : unaryOp
    | binaryOp
    ;

unaryOp
    : (MINUS | NOT) exprTerm
    ;

binaryOp
    : exprTerm binaryOperator exprTerm
    ;

binaryOperator
    : compareOperator
    | arithmeticOperator
    | logicOperator
    ;

compareOperator
    : EQUALS
    | NOT_EQUALS
    | LT
    | GT
    | LTE
    | GTE
    ;

arithmeticOperator
    : PLUS
    | MINUS
    | STAR
    | SLASH
    | PERCENT
    ;

logicOperator
    : AND
    | OR
    ;
