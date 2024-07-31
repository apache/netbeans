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
parser grammar BladeAntlrParser;

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
package org.netbeans.modules.php.blade.syntax.antlr4.v10;

/**
 * Parser generated for netbeans blade editor
 * Some elements have been simplified to optimize parser speed
 * For example
 * - switch statement have a loos validation
 * - generic block statement "@isset" | "@unless" are grouped togehter
 * - the start match and end match will be checked in the parser
 */
}

@parser::members {public static int bladeVersion = 10;}

options { tokenVocab = BladeAntlrLexer; }

file : general_statement* EOF;

general_statement: inline_statement
    | block_statement
    | html
    ;

inline_statement: 
    inline_directive
    | possibleDirective
    | regular_echo
    | raw_echo
    | phpInline
    ;

inline_directive: 
     section_inline
    | identifiableType
    | stack
    | includeCond
    | each
    | (D_INCLUDE_FIRST | D_CLASS | D_STYLE | D_METHOD | D_PROPS 
           | D_DD | D_JS | D_JS | D_AWARE | D_HTML_ATTR_EXPR | D_CASE | D_JSON) composed_php_expression
    | (D_CSRF | D_LOOP_ACTION | D_BREAK | D_LIVEWIRE | D_DEFAULT)
    | asset_bundler
    | inject
    //using basic inline case statement to not add complexity to parser
    | D_PERMISSION_ELSE composed_php_expression
    | loop_action
    | D_ELSE //custom block directives?
    | D_MISC
    | custom_directive
    ;

block_statement: 
    section
    | condSection
    | push
    | pushIf
    | once_block
    | prepend
    | fragmentD
    | if
    | switch
    | env_block
    | empty_block
    | error_block
    //we can consider the statements not being empty
    | conditional_block
    | auth_block
    //lazy parser for can
    | permission
    | while
    | for
    | foreach
    | forelse
    | session
    | verbatim_block
    | php_blade
    ;

identifiableType : d_name=(D_INCLUDE | D_INCLUDE_IF | D_EXTENDS 
        |  D_YIELD | D_USE | D_LANG) BLADE_PARAM_LPAREN (idString = BL_PARAM_STRING | composedArgument) (BL_COMMA composedArgument)? (BL_COMMA)? BLADE_PARAM_RPAREN;
section_inline: D_SECTION doubleArgWrapperP;
section : D_SECTION singleArgWrapperP (general_statement | D_PARENT)* (D_SHOW | D_STOP | D_OVERWRITE | D_ENDSECTION | D_APPEND);
push : D_PUSH singleArgWrapperP general_statement* D_ENDPUSH;
pushOnce : D_PUSH_ONCE singleArgWrapperP general_statement* D_ENDPUSH_ONCE;
pushIf : D_PUSH_IF singleArgWrapperP general_statement* D_ENDPUSH_IF;
prepend : D_PREPEND singleArgWrapperP general_statement* D_ENDPREPEND;
fragmentD : D_FRAGMENT composed_php_expression general_statement* D_ENDFRAGMENT;

if : D_IF main_php_expression general_statement*  (D_ELSEIF main_php_expression general_statement*)* else?  endif;
else : D_ELSE general_statement*;
endif: D_ENDIF;
empty_block : D_EMPTY composed_php_expression simple_conditional_stm D_ENDEMPTY;

//the consistency for these blocks need to be checked inside the parser
conditional_block : D_COND_BLOCK_START main_php_expression simple_conditional_stm D_COND_BLOCK_END;
auth_block : D_AUTH_START (BLADE_PARAM_LPAREN (composedArgument)* BLADE_PARAM_RPAREN)* simple_conditional_stm D_AUTH_END;
env_block: (D_ENV  singleArgWrapperP simple_conditional_stm D_ENDENV) | D_PRODUCTION simple_conditional_stm D_ENDPRODUCTION;
permission : D_PERMISSION_START composed_php_expression simple_conditional_stm D_PERMISSION_END;

simple_conditional_stm : general_statement* else?;
//
error_block :  D_ERROR php_expression general_statement* D_ENDERROR;

//no need to add complexity to parser
switch: D_SWITCH php_expression (general_statement | D_BREAK)+ D_ENDSWITCH;

//loops
while : D_WHILE php_expression (general_statement)* D_ENDWHILE;
for : D_FOR php_expression (general_statement)* D_ENDFOR;
foreach : D_FOREACH FOREACH_LOOP_LPAREN loop_expression FOREACH_LOOP_RPAREN (general_statement)* D_ENDFOREACH;
forelse : D_FORELSE FOREACH_LOOP_LPAREN loop_expression FOREACH_LOOP_RPAREN (general_statement | D_EMPTY)* D_ENDFORELSE;

//misc block
session : D_SESSION composed_php_expression general_statement* D_ENDSESSION;

//layout
stack : D_STACK singleArgWrapperP;
asset_bundler : d_name=D_ASSET_BUNDLER BLADE_EXPR_LPAREN (BL_SQ_LPAREN id_strings=EXPR_STRING+ BL_SQ_RPAREN dir=EXPR_STRING? | id_string=EXPR_STRING | composed_php_expression+) BLADE_EXPR_RPAREN;
inject : D_INJECT BLADE_PARAM_LPAREN composedArgument BL_COMMA (identifiableArgument | composedArgument) BLADE_PARAM_RPAREN;

includeCond : d_name=(D_INCLUDE_WHEN | D_INCLUDE_UNLESS) BLADE_PARAM_LPAREN
    composedArgument
    BL_COMMA
    (idString=BL_PARAM_STRING | composedArgument)
    (BL_COMMA composedArgument)?
    BLADE_PARAM_RPAREN;

each : D_EACH BLADE_PARAM_LPAREN 
    (idString=BL_PARAM_STRING | composedArgument) //default path
    BL_COMMA
    composedArgument
    BL_COMMA
    composedArgument
    (BL_COMMA
    (identifiableArgument | composedArgument))? //fallback
    BLADE_PARAM_RPAREN;

once_block : D_ONCE general_statement+ D_ENDONCE;
condSection : (D_SECTION_MISSING | D_HAS_SECTION) singleArgWrapperP simple_conditional_stm D_ENDIF;

custom_directive : D_CUSTOM ((BLADE_PARAM_LPAREN BLADE_PARAM_RPAREN) | multiArgWrapper )
;

possibleDirective : D_UNKNOWN | D_UNKNOWN_ATTR_ENC;
    
php_blade : D_PHP composed_php_expression* D_ENDPHP | D_PHP main_php_expression;

phpInline : PHP_INLINE_START composed_php_expression+ (PHP_EXIT | EOF);
//echo

regular_echo : CONTENT_TAG_OPEN echo_expr* CONTENT_TAG_CLOSE;
raw_echo : RAW_TAG_OPEN echo_expr* RAW_TAG_CLOSE;

echo_expr : composed_php_expression;

class_expr_usage: class_name_reference 
| object_alias_static_access 
| object_alias_direct_access
| static_direct_class_access
| static_direct_namespace_class_access
| class_instance;

object_alias_static_access : alias_name=PHP_VARIABLE PHP_STATIC_ACCESS static_property=PHP_IDENTIFIER;
object_alias_direct_access : alias_name=PHP_VARIABLE PHP_INSTANCE_ACCESS property=PHP_IDENTIFIER;
static_direct_class_access : class_name=PHP_IDENTIFIER PHP_STATIC_ACCESS 
    func_name=PHP_IDENTIFIER BLADE_EXPR_LPAREN composed_php_expression* BLADE_EXPR_RPAREN
    | class_name=PHP_IDENTIFIER PHP_STATIC_ACCESS static_property=PHP_IDENTIFIER
    ;
static_direct_namespace_class_access : namespace=PHP_NAMESPACE_PATH? class_name=PHP_IDENTIFIER PHP_STATIC_ACCESS
    func_name=PHP_IDENTIFIER BLADE_EXPR_LPAREN composed_php_expression* BLADE_EXPR_RPAREN
    | namespace=PHP_NAMESPACE_PATH? class_name=PHP_IDENTIFIER PHP_STATIC_ACCESS static_property=PHP_IDENTIFIER
    ;

class_instance : PHP_NEW (namespace=PHP_NAMESPACE_PATH? class_name=PHP_IDENTIFIER) BLADE_EXPR_LPAREN composed_php_expression* BLADE_EXPR_RPAREN;
class_name_reference : namespace=PHP_NAMESPACE_PATH? class_name=PHP_IDENTIFIER PHP_STATIC_ACCESS PHP_CLASS_KEYWORD;

namespacePath : namespace=PHP_NAMESPACE_PATH? class_name=PHP_IDENTIFIER;

function_call : func_name=PHP_IDENTIFIER BLADE_EXPR_LPAREN composed_php_expression* BLADE_EXPR_RPAREN;

php_expression: PHP_EXPRESSION;
loop_expression : simple_foreach_expr
| (PHP_VARIABLE | PHP_EXPRESSION | FOREACH_PARAM_ASSIGN | FOREACH_LOOP_LPAREN | FOREACH_LOOP_RPAREN | FOREACH_AS)+ //complex expression (lazy handling)
;

main_php_expression : BLADE_EXPR_LPAREN composed_php_expression+ BLADE_EXPR_RPAREN;

composed_php_expression : class_expr_usage | function_call | PHP_IDENTIFIER | namespacePath | PHP_VARIABLE 
| PHP_NAMESPACE_PATH | EXPR_STRING |
 PHP_KEYWORD | PHP_EXPRESSION+ | PHP_STATIC_ACCESS | PHP_CLASS_KEYWORD
| PHP_INSTANCE_ACCESS | BL_SQ_LPAREN composed_php_expression* BL_SQ_RPAREN |  BLADE_EXPR_LPAREN composed_php_expression* BLADE_EXPR_RPAREN | PHP_EXPR_STRING;

simple_foreach_expr: loop_array=PHP_VARIABLE FOREACH_AS key=PHP_VARIABLE (FOREACH_PARAM_ASSIGN item=PHP_VARIABLE)?;

singleArgWrapperP:  BLADE_PARAM_LPAREN (idString=BL_PARAM_STRING | composedArgument) BLADE_PARAM_RPAREN;
doubleArgWrapperP:  BLADE_PARAM_LPAREN (idString=BL_PARAM_STRING | composedArgument) BL_COMMA composedArgument BLADE_PARAM_RPAREN;
multiArgWrapper :  BLADE_PARAM_LPAREN (composedArgument) (BL_COMMA composedArgument)* BLADE_PARAM_RPAREN;

identifiableArgument : BL_PARAM_STRING;
composedArgument : (phpExpr)+ ;

phpExpr : identifiableArray | arrayDefine | BLADE_PARAM_EXTRA | PHP_VARIABLE | PHP_KEYWORD |  BL_PARAM_CONCAT_OPERATOR | BL_PARAM_STRING | BL_PARAM_ASSIGN | BL_NAME_STRING | BL_PARAM_COMMA;

//['key' => $value]
identifiableArray : BL_SQ_LPAREN paramAssign (BL_PARAM_COMMA paramAssign)* BL_PARAM_COMMA? BL_SQ_RPAREN;
arrayDefine : BL_SQ_LPAREN phpExpr+ BL_SQ_RPAREN
| BL_SQ_LPAREN BL_SQ_RPAREN;

paramAssign : BL_PARAM_STRING BL_PARAM_ASSIGN (PHP_VARIABLE | PHP_KEYWORD | BL_PARAM_STRING);
verbatim_block : D_VERBATIM D_ENDVERBATIM;

loop_action : (D_LOOP_ACTION | D_BREAK) php_expression?;


//html
html : HTML+ | HTML_COMPONENT_PREFIX | HTML_TAG_START | EQ | HTML_IDENTIFIER;
