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
    | <assoc=right> op=(NOT | MINUS) right=expression
    | left=expression op=(STAR | SLASH | PERCENT) right=expression
    | left=expression op=(PLUS | MINUS) right=expression
    | left=expression op=(AND | OR) right=expression
    | left=expression op=(LTE | GTE | LT | GT | EQUALS | NOT_EQUALS) right=expression
    | exprCond=expression QUESTION (exprTrue=expression COLON exprFalse=expression)
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
    // The original separator in HCL is Comma or NewLine
    : LBRACK expression (COMMA? expression)* COMMA? RBRACK
    | LBRACK RBRACK
    ;

object
    // The original separator in HCL is Comma or NewLine
    : LBRACE objectElem (COMMA? objectElem)* COMMA? RBRACE
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

interpolationContent
    : INTERPOLATION_CONTENT+
    ;

interpolation
    : INTERPOLATION_START ( interpolationContent | quotedTemplate)* INTERPOLATION_END
    ;

templateContent
    : TEMPLATE_CONTENT+
    ;

template
    : TEMPLATE_START ( templateContent | quotedTemplate)* TEMPLATE_END
    ;

heredocContent
    : HEREDOC_CONTENT+
    ;

heredocTemplate
    : HEREDOC_START (heredocContent | interpolation | template)* HEREDOC_END
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
