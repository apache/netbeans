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
    | exprCond=expression op=QUESTION (exprTrue=expression COLON exprFalse=expression)
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
    ;

literalValue
    : stringLit     // See: https://github.com/hashicorp/hcl/issues/619
    | NUMERIC_LIT
    | TRUE
    | FALSE
    | NULL
    ;

collectionValue
    : tuple
    | object
    ;


tuple
    // The original separator in HCL is Comma or NewLine, TF says, it is Comma only
    // See: https://github.com/hashicorp/hcl/issues/618
    // Using TF which is stricter
    : LBRACK expression (COMMA expression)* COMMA? RBRACK
    | LBRACK RBRACK
    ;

object
    // The original separator in HCL is Comma or NewLine (NL)
    // HCL uses NewLine sometimes inconsistent. To have things simplified NL
    // is sent to the HIDDEN channel, so it is not available here.
    // The only thing we can do is make COMMA optional, that's result more
    // permissive grammar than HCL. { a = 1 b = 2 } is a valid object here.
    : LBRACE objectElem (COMMA? objectElem)* COMMA? RBRACE
    | LBRACE RBRACE
    ;

objectElem
    // HCL spec says (IDENTIFIER | expression) though a single IDENTIFIER
    // would map to variableExpression, so this is redundant here
    // AST implementation is easier simply using expression
    : key=expression (EQUAL | COLON) value=expression
    ;

templateExpr
    : quotedTemplate
    | heredoc
    ;

stringLit
    : QUOTE QUOTE
    | QUOTE stringContent QUOTE
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
    : (heredocContent | interpolation | template)*
    ;

heredoc
    : HEREDOC_START heredocTemplate HEREDOC_END
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
    : LBRACE forIntro key=expression RARROW value=expression ELLIPSIS? forCond? RBRACE
    ;

forIntro
    : FOR first=IDENTIFIER (COMMA second=IDENTIFIER)? IN expression COLON
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
