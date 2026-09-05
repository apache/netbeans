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
parser grammar BladePhpAntlrParser;

@header{
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
  package org.netbeans.modules.php.blade.syntax.antlr4.php;
}

options { 
    superClass = ParserAdaptor;
    tokenVocab = BladePhpAntlrLexer;
 }

expression : exprStatement* EOF;

exprStatement :
    //empty statement
    ';'
    | '{' exprStatement* '}'
    | '(' exprStatement* ')'
    | COMPARISON_OPERATOR exprStatement
    | LOGICAL_UNION_OPERATOR functionalExpr+
    | 'foreach' '(' foreachArguments ')'
    | functionalExpr
    | ifStatement
    | foreachDirectiveStatement
    | output
    | misc
;

logicalStatement:
    '(' functionalExpr* ')'
    | functionalExpr (LOGICAL_UNION_OPERATOR functionalExpr)+
    | functionalExpr (COMPARISON_OPERATOR functionalExpr)+
    | functionalExpr
;

ifStatement:
    //long expr
    'if' '(' logicalStatement+ ')' '{' exprStatement* '}' 
    (ELSEIF '(' logicalStatement+ ')' '{' exprStatement* '}' )*
    ('else' '{' exprStatement* '}')?
    ;

inputExpr:
   varExpr
    ;

functionalExpr:
    matchStatement
    | classExpression (classMember)*
    | functionExpr
    | LANG_CONSTRUCT '(' functionalExpr ')'
    | inputExpr
    ;

classExpression:
    classInstanceStatement
    | staticMethodAccess
    | staticFieldAccess
    | staticClassReference
    | staticAccess
    | aliasDirectAccess
    | directMethodAccess
    ;

foreachDirectiveStatement:
    {this.bladeParserContext.equals(ParserContext.FOREACH)}? foreachArguments
    ;

foreachArguments:
    (array | main_array = PHP_VARIABLE) 'as' array_item=PHP_VARIABLE
    | (array | main_array = PHP_VARIABLE) 'as' array_key=PHP_VARIABLE '=>'  array_item=PHP_VARIABLE
    | functionExpr 'as' functionExpr ('=>' functionExpr)?
;

classInstanceStatement:
    'new' namespace? className=IDENTIFIER arguments?
    ;

matchStatement:
    'match' '(' functionalExpr ')' '{'
        (functionalExpr+ (',' functionalExpr+)* '=>' functionalExpr+)* 
        (',' functionalExpr+ (',' functionalExpr+)* '=>' functionalExpr+)*
        ','?
    '}'
;
staticClassReference :
    namespace? IDENTIFIER '::' CLASS
;

staticMethodAccess : 
    namespace? className=IDENTIFIER '::' method=IDENTIFIER arguments
;

staticFieldAccess : 
    namespace? className=IDENTIFIER '::' (const=IDENTIFIER | propertyAlias=PHP_VARIABLE | 'class')
    | classAlias=PHP_VARIABLE '::' const=IDENTIFIER
    | classAlias=PHP_VARIABLE '::' propertyAlias=PHP_VARIABLE
;

staticAccess : 
    //should throw an error?
    namespace? className=IDENTIFIER '::'
;

aliasDirectAccess:
    PHP_VARIABLE classMember
    ;

classMember:
    directMethodAccess
    | '->' IDENTIFIER
;

directMethodAccess : 
    '->' IDENTIFIER arguments
;

directAccess : 
    IDENTIFIER arguments '->' IDENTIFIER arguments
;

functionExpr : 
    IDENTIFIER arguments
;

arguments :
    '(' argument (',' argument )* ')'
    | '(' ')'
    ;

namespace :
    '\\'? (IDENTIFIER '\\')+
    | '\\'
;

argument:
    functionalExpr
    | IDENTIFIER
    ;

array:
   PHP_VARIABLE array_key_item+
   | array_key_item
;

array_key_item:
     '[' array_key_item* ']'
    | 'array' '(' array_key_item* ')'
    | '[' (array_child '=>' array_key_item)+ ']'
    | 'array' '(' (array_child '=>' array_key_item)+ ')'
    | '[' array_child (',' array_child)* ','? ']'
    | 'array' '(' array_child (',' array_child)* ','? ')'
;

array_child:
    functionalExpr '=>' array_key_item
    | functionalExpr '=>' functionalExpr
    | functionalExpr
    ;

varExpr:
  array
  | '$'? PHP_VARIABLE
  | STRING_LITERAL  
;

misc:
  'new' PHP_VARIABLE arguments?
  | 'new' namespace //incomplete namespcace
  | namespace className=IDENTIFIER
;

output: 
  'echo' functionalExpr
;  