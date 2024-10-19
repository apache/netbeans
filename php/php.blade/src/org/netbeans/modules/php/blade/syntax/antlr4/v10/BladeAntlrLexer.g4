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
lexer grammar BladeAntlrLexer;
import BladeCommonLexer;

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
}

options { 
    superClass = LexerAdaptor;
    caseInsensitive = true;
}
 
tokens {
   PHP_EXPRESSION,
   PHP_VARIABLE,
   PHP_KEYWORD,
   PHP_NEW,
   PHP_IDENTIFIER,
   PHP_NAMESPACE_PATH, 
   PHP_STATIC_ACCESS,
   PHP_CLASS_KEYWORD,
   PHP_INSTANCE_ACCESS,
   BLADE_PARAM_EXTRA,
   BLADE_PARAM_LPAREN,
   BLADE_PARAM_RPAREN,
   BLADE_EXPR_LPAREN,
   BLADE_EXPR_RPAREN,
   BL_SQ_LPAREN,
   BL_SQ_LRAREN,
   BL_PARAM_STRING,
   BL_PARAM_ASSIGN,
   BL_COMMA,
   BL_PARAM_COMMA,
   PHP_EXPR_STRING,
   ERROR
}
 
channels { COMMENT, PHP_CODE }

fragment CompomentIdentifier
    : [a-z\u0080-\ufffe][a-z0-9-_.:\u0080-\ufffe]*;

fragment CssSelector
    : ('#' | '.')? [a-z\u0080-\ufffe][a-z0-9-_:\u0080-\ufffe]* | CssAttrSelector;

fragment JsFunctionStart
    : NameString '(' NameString* (',' (' ')* NameString)* (' ')* ')' (' ')* ('{' { this._input.LA(1) != '{'}?)?;

fragment StringParam
    : [\\'] CssSelector ((' ')* CssSelector)* [\\'] | '"' CssSelector ((' ')* CssSelector)* [\\'] '"';

fragment CssAttrSelector 
    : '[' FullIdentifier (EQ StringAttrValue)?  ']';

fragment StringAttrValue
    : '"' FullIdentifier '"' | [\\'] FullIdentifier [\\'];
//RULES


//conditionals
D_IF : '@if'->pushMode(LOOK_FOR_PHP_COMPOSED_EXPRESSION);
D_ELSEIF : '@elseif'->pushMode(LOOK_FOR_PHP_COMPOSED_EXPRESSION);
D_ELSE : '@else';
D_ENDIF : '@endif';
D_SWITCH : '@switch'->pushMode(LOOK_FOR_PHP_EXPRESSION);
D_CASE : '@case'->pushMode(LOOK_FOR_PHP_EXPRESSION);
D_DEFAULT : '@default';
D_ENDSWITCH : '@endswitch';

D_EMPTY : '@empty'->pushMode(LOOK_FOR_PHP_COMPOSED_EXPRESSION);
D_ENDEMPTY : '@endempty';

D_COND_BLOCK_START : ('@unless' | '@isset')->pushMode(LOOK_FOR_PHP_COMPOSED_EXPRESSION);
D_COND_BLOCK_END : ('@endunless' | '@endisset');

//loops
D_FOREACH : '@foreach'->pushMode(FOREACH_LOOP_EXPRESSION);
D_ENDFOREACH : '@endforeach';
D_FOR : '@for'->pushMode(LOOK_FOR_PHP_EXPRESSION);
D_ENDFOR : '@endfor';
D_FORELSE : '@forelse'->pushMode(FOREACH_LOOP_EXPRESSION);
D_ENDFORELSE : '@endforelse';
D_WHILE : '@while'->pushMode(LOOK_FOR_PHP_EXPRESSION);
D_ENDWHILE : '@endwhile';
D_BREAK : '@break'->pushMode(LOOK_FOR_PHP_EXPRESSION);
D_LOOP_ACTION : ('@continue')->pushMode(LOOK_FOR_PHP_EXPRESSION);

//includes
D_INCLUDE : '@include'->pushMode(LOOK_FOR_BLADE_PARAMETERS);
D_INCLUDE_IF : '@includeIf'->pushMode(LOOK_FOR_BLADE_PARAMETERS);
D_INCLUDE_WHEN : '@includeWhen'->pushMode(LOOK_FOR_BLADE_PARAMETERS);
D_INCLUDE_FIRST : '@includeFirst'->pushMode(LOOK_FOR_BLADE_PARAMETERS);
D_INCLUDE_UNLESS : '@includeUnless'->pushMode(LOOK_FOR_BLADE_PARAMETERS);
D_EACH : '@each'->pushMode(LOOK_FOR_BLADE_PARAMETERS);

//layout
D_EXTENDS : '@extends'->pushMode(LOOK_FOR_BLADE_PARAMETERS);

//from livewire (converts variable to javascript syntax)
D_JS : '@js'->pushMode(LOOK_FOR_PHP_COMPOSED_EXPRESSION);

//safe json_encode
D_JSON  : '@json'->pushMode(LOOK_FOR_PHP_COMPOSED_EXPRESSION);

D_SECTION : '@section'->pushMode(LOOK_FOR_BLADE_PARAMETERS);
D_HAS_SECTION : '@hasSection'->pushMode(LOOK_FOR_BLADE_PARAMETERS);
D_SECTION_MISSING : '@sectionMissing'->pushMode(LOOK_FOR_BLADE_PARAMETERS);
D_ENDSECTION : '@endsection';
D_YIELD : '@yield'->pushMode(LOOK_FOR_BLADE_PARAMETERS);
D_PARENT : '@parent';
D_SHOW : '@show';
D_OVERWRITE : '@overwrite';
D_STOP : '@stop';
D_APPEND : '@append';
D_ONCE : '@once';
D_ENDONCE : '@endonce';
D_STACK : '@stack'->pushMode(LOOK_FOR_BLADE_PARAMETERS);
D_PUSH : '@push'->pushMode(LOOK_FOR_BLADE_PARAMETERS);
D_ENDPUSH : '@endpush';
D_PUSH_IF : '@pushIf'->pushMode(LOOK_FOR_BLADE_PARAMETERS);
D_ENDPUSH_IF : '@endPushIf';
D_PUSH_ONCE : '@pushOnce'->pushMode(LOOK_FOR_BLADE_PARAMETERS);
D_ENDPUSH_ONCE : '@endPushOnce';
D_PREPEND : '@prepend'->pushMode(LOOK_FOR_BLADE_PARAMETERS);
D_ENDPREPEND : '@endprepend';
D_PROPS : '@props'->pushMode(LOOK_FOR_PHP_EXPRESSION);

D_FRAGMENT : '@fragment'->pushMode(LOOK_FOR_PHP_COMPOSED_EXPRESSION);
D_ENDFRAGMENT : '@endfragment';

//forms
D_CSRF  : '@csrf';
D_METHOD : '@method'->pushMode(LOOK_FOR_PHP_EXPRESSION);
D_ERROR : '@error'->pushMode(LOOK_FOR_PHP_EXPRESSION);
D_ENDERROR : '@enderror';

//env
D_PRODUCTION : '@production';
D_ENDPRODUCTION : '@endproduction';
D_ENV : '@env'->pushMode(LOOK_FOR_BLADE_PARAMETERS);
D_ENDENV : '@endenv';

//auth and roles
D_AUTH_START : ('@auth' | '@guest')->pushMode(LOOK_FOR_BLADE_PARAMETERS);
D_AUTH_END : ('@endauth' | '@endguest');

//lazy parser
D_PERMISSION_START : '@can' ('not' | 'any')?->pushMode(LOOK_FOR_PHP_COMPOSED_EXPRESSION);
D_PERMISSION_ELSE : '@elsecan' ('not' | 'any')?->pushMode(LOOK_FOR_PHP_COMPOSED_EXPRESSION);
D_PERMISSION_END : '@endcan' ('not' |'any')?;

//styles, attributes
D_CLASS : '@class'->pushMode(LOOK_FOR_PHP_EXPRESSION);
D_STYLE : '@style'->pushMode(LOOK_FOR_PHP_EXPRESSION);
D_HTML_ATTR_EXPR : ('@checked' | '@disabled' | '@readonly' | '@required' | '@selected')->pushMode(LOOK_FOR_PHP_COMPOSED_EXPRESSION);
D_AWARE : '@aware'->pushMode(LOOK_FOR_PHP_COMPOSED_EXPRESSION);

//misc
D_BOOL : '@bool'->pushMode(LOOK_FOR_BLADE_PARAMETERS);
D_WHEN : '@when'->pushMode(LOOK_FOR_BLADE_PARAMETERS);
D_SESSION : '@session'->pushMode(LOOK_FOR_PHP_COMPOSED_EXPRESSION);
D_ENDSESSION : '@endsession';

D_DD : ('@dd' | '@dump')->pushMode(LOOK_FOR_PHP_COMPOSED_EXPRESSION);
D_LANG : '@lang'->pushMode(LOOK_FOR_BLADE_PARAMETERS);

//php injection
D_USE : '@use'->pushMode(LOOK_FOR_BLADE_PARAMETERS);
D_INJECT : '@inject'->pushMode(LOOK_FOR_BLADE_PARAMETERS);
D_PHP_SHORT : '@php' (' ')? {this._input.LA(1) == '('}? ->type(D_PHP),pushMode(LOOK_FOR_PHP_COMPOSED_EXPRESSION);
D_PHP : '@php' {this._input.LA(1) == ' ' || this._input.LA(1) == '\r' || this._input.LA(1) == '\n'}?->pushMode(BLADE_INLINE_PHP);

D_VERBATIM : '@verbatim' ->pushMode(VERBATIM_MODE);
D_ENDVERBATIM : '@endverbatim';

//known plugins
D_LIVEWIRE : '@livewireStyles' | '@bukStyles' | '@livewireScripts' | '@bukScripts' | '@livewire';
D_ASSET_BUNDLER : '@vite'->pushMode(LOOK_FOR_PHP_COMPOSED_EXPRESSION);

D_MISC : '@viteReactRefresh';

//we will decide that a custom directive has expression to avoid email matching
D_CUSTOM : ('@' NameString {this._input.LA(1) == '(' || 
        (this._input.LA(1) == ' ' && this._input.LA(2) == '(')}? ) ->pushMode(LOOK_FOR_BLADE_PARAMETERS);

D_UNKNOWN_ATTR_ENC : '@' NameString {this._input.LA(1) == '"'}?;
D_UNKNOWN : '@' NameString {this._input.LA(1) != '"'}?;

//display
CONTENT_TAG_OPEN : '{{' ->pushMode(INSIDE_REGULAR_ECHO);
RAW_TAG_OPEN : '{!!' ->pushMode(INSIDE_RAW_ECHO);

AT : '@'->type(HTML);
//for completion
RAW_TAG_START : '{!'->type(HTML);

PHP_INLINE_START : ('<?php' | '<?=')->pushMode(INSIDE_PHP_INLINE);



HTML_COMPONENT_PREFIX : '<x-' (CompomentIdentifier |  CompomentIdentifier ('::' CompomentIdentifier)+)? {this.setComponentTagOpenStatus(true);};
HTML_L_COMPONENT : '<x-' CompomentIdentifier {this._input.LA(1) == '>'}? ->type(HTML_COMPONENT_PREFIX);
JS_SCRIPT : ('$'? '(' StringParam | FullIdentifier ')' ('.' NameString)? |  JsFunctionStart ('.' JsFunctionStart)*) ->skip;
HTML_TAG_START : '<' FullIdentifier;
HTML_CLOSE_TAG : ('</' FullIdentifier [\n\r ]* '>')+ ->skip;
HTML_TAG_SELF_CLOSE : '/>' {this.setComponentTagOpenStatus(false);}->type(HTML);
HTML_CLOSE_SYMBOL : '>' {this.setComponentTagOpenStatus(false);} ->type(HTML);
STRING_PATH : ('"' HTML_PATH* '"' | [\\'] HTML_PATH [\\'])->skip;
HTML_PATH : (' ')* FullIdentifier ('/' FullIdentifier)+ ('.' NameString)? ('?' NameString (EQ NameString)*)? ->skip;
HTML_TEXT : (' ')* FullIdentifier ((' ')+ FullIdentifier)+ ->skip;


HTML_IDENTIFIER : FullIdentifier {this.consumeHtmlIdentifier();};

EQ : '=';
WS : ((' ')+ | [\r\n]+)->skip;
OTHER : . ->skip;

/**
* MODES
*
*/

// {{  }}
mode INSIDE_REGULAR_ECHO;

REGULAR_ECHO_PHP_VAR : PhpVariable->type(PHP_VARIABLE);
REGULAR_ECHO_KEYWORD : PhpKeyword->type(PHP_KEYWORD);

REGULAR_PHP_NAMESPACE_PATH : ('\\'? (NameString '\\')+)->type(PHP_NAMESPACE_PATH);
REGULAR_ECHO_PHP_IDENTIFIER : NameString->type(PHP_IDENTIFIER);
REGULAR_ECHO_STATIC_ACCESS : '::'->type(PHP_STATIC_ACCESS);
CONTENT_TAG_CLOSE : ('}}')->popMode;
REGULAR_ECHO_LPAREN : '(' ->type(BLADE_EXPR_LPAREN);
REGULAR_ECHO_RPAREN : ')' ->type(BLADE_EXPR_RPAREN);
REGULAR_ECHO_INSTANCE_ACCESS : '->'->type(PHP_INSTANCE_ACCESS);

//not treated
REGULAR_ECHO_EXPR_MORE : . ->skip;
EXIT_REGULAR_ECHO_EOF : EOF->type(ERROR),popMode;

// {!!  !!}
mode INSIDE_RAW_ECHO;

RAW_ECHO_PHP_VAR : PhpVariable->type(PHP_VARIABLE);
RAW_ECHO_KEYWORD : PhpKeyword->type(PHP_KEYWORD);
RAW_ECHO_PHP_NAMESPACE_PATH : ('\\'? (NameString '\\')+)->type(PHP_NAMESPACE_PATH);
RAW_ECHO_PHP_IDENTIFIER : NameString->type(PHP_IDENTIFIER);
RAW_ECHO_STATIC_ACCESS : '::'->type(PHP_STATIC_ACCESS);
RAW_TAG_CLOSE : ('!!}')->popMode;
RAW_ECHO_LPAREN : '(' ->type(BLADE_EXPR_LPAREN);
RAW_ECHO_RPAREN : ')' ->type(BLADE_EXPR_RPAREN);
RAW_ECHO_INSTANCE_ACCESS : '->'->type(PHP_INSTANCE_ACCESS);

//NOT TREATED
RAW_ECHO_EXPR_MORE : . ->skip;
EXIT_RAW_ECHO_EOF : EOF->type(ERROR),popMode;

mode LOOK_FOR_PHP_EXPRESSION;

WS_EXPR_ESCAPE : [ ]+ {this._input.LA(1) == '@'}?->skip, popMode;
WS_EXPR : [ ]+->skip;
OPEN_EXPR_PAREN_MORE : '(' ->more,pushMode(INSIDE_PHP_EXPRESSION);

L_OHTER_ESCAPE : . {this._input.LA(1) == '@'}?->type(HTML), popMode;
L_OTHER : . ->type(HTML), popMode;

//{{}}, @if, @foreach
mode INSIDE_PHP_EXPRESSION;

OPEN_EXPR_PAREN : {this.getRoundParenBalance() == 0}? '(' {this.increaseRoundParenBalance();} ->more;
CLOSE_EXPR_PAREN : {this.getRoundParenBalance() == 1}? ')' 
    {this.decreaseRoundParenBalance();}->type(PHP_EXPRESSION),mode(DEFAULT_MODE);

LPAREN : {this.getRoundParenBalance() > 0}? '(' {this.increaseRoundParenBalance();}->more;
RPAREN : {this.getRoundParenBalance() > 0}? ')' {this.decreaseRoundParenBalance();}->more;

//in case of lexer restart context
EXIT_RPAREN : ')' {this.getRoundParenBalance() == 0}?->type(PHP_EXPRESSION),mode(DEFAULT_MODE);

PHP_EXPRESSION_MORE : . ->more;

EXIT_EOF : EOF->type(ERROR),popMode;

//@if
mode LOOK_FOR_PHP_COMPOSED_EXPRESSION;

WS_COMPOSED_EXPR : [ ]+->skip;
BLADE_EXPR_LPAREN : '(' {this.resetRoundParenBalance();} ->pushMode(INSIDE_PHP_COMPOSED_EXPRESSION);

L_COMPOSED_EXPR_OTHER : . ->type(HTML), popMode;

//{{}}, @if, @foreach
mode INSIDE_PHP_COMPOSED_EXPRESSION;

EXPR_SQ_LPAREN : '[' {this.increaseSquareParenBalance();}->type(BL_SQ_LPAREN);
EXPR_SQ_RPAREN : ']' {this.decreaseSquareParenBalance();}->type(BL_SQ_RPAREN);

EXPR_CURLY_LPAREN : '{' {this.increaseCurlyParenBalance();}->type(PHP_EXPRESSION);
EXPR_CURLY_RPAREN : '}' {this.decreaseCurlyParenBalance();}->type(PHP_EXPRESSION);

EXPR_STRING : DOUBLE_QUOTED_STRING_FRAGMENT | SINGLE_QUOTED_STRING_FRAGMENT;

//EXPR_ASSIGN : '=>'->type(BL_PARAM_ASSIGN);

COMPOSED_EXPR_PHP_VAR : PhpVariable->type(PHP_VARIABLE);
COMPOSED_PHP_KEYWORD : PhpKeyword->type(PHP_KEYWORD);
COMPOSED_PHP_NAMESPACE_PATH : ('\\'? (NameString '\\')+)->type(PHP_NAMESPACE_PATH);
COMPOSED_EXPR_PHP_IDENTIFIER : NameString->type(PHP_IDENTIFIER);
COMPOSED_EXPR_STATIC_ACCESS : '::'->type(PHP_STATIC_ACCESS);

COMPOSED_EXPR_LPAREN : '(' {this.increaseRoundParenBalance();}->type(BLADE_EXPR_LPAREN);
COMPOSED_EXPR_RPAREN : ')' {consumeExprRParen();};

//not treated
PHP_COMPOSED_EXPRESSION : . ->skip;

EXIT_COMPOSED_EXPRESSION_EOF : EOF->type(ERROR),popMode;

//@section, @include etc
mode LOOK_FOR_BLADE_PARAMETERS;

WS_BL_PARAM : [ ]+->skip;
OPEN_BL_PARAM_PAREN_MORE : '(' {this.resetRoundParenBalance();} ->type(BLADE_PARAM_LPAREN),pushMode(INSIDE_BLADE_PARAMETERS);

L_BL_PARAM_OTHER : . ->type(HTML), popMode;

mode FOREACH_LOOP_EXPRESSION;

FOREACH_WS_EXPR : [ ]+->skip;
FOREACH_LOOP_LPAREN : '(' {this.increaseRoundParenBalance();};
FOREACH_LOOP_RPAREN : ')' {this.decreaseRoundParenBalance(); if (this.getRoundParenBalance() == 0){this.popMode();}};

FOREACH_AS : 'as';

FOREACH_PHP_VARIABLE : PhpVariable->type(PHP_VARIABLE);

FOREACH_PARAM_ASSIGN : '=>';

LOOP_COMPOSED_PHP_KEYWORD : PhpKeyword->type(PHP_EXPRESSION);

LOOP_NAME_STRING : (DOUBLE_QUOTED_STRING_FRAGMENT | SINGLE_QUOTED_STRING_FRAGMENT | NameString)->type(PHP_EXPRESSION);

LOOP_STATIC_ACCESS : '::'->type(PHP_EXPRESSION);

LOOP_PHP_EXPRESSION : . ->type(PHP_EXPRESSION);
FOREACH_EOF : EOF->type(ERROR),popMode;
//( )
mode INSIDE_BLADE_PARAMETERS;

BL_PARAM_LINE_COMMENT : LineComment->channel(COMMENT);

BL_SQ_LPAREN : '[' {this.increaseSquareParenBalance();};
BL_SQ_RPAREN : ']' {this.decreaseSquareParenBalance();};

BL_CURLY_LPAREN : '{' {this.increaseCurlyParenBalance();}->type(BLADE_PARAM_EXTRA);
BL_CURLY_RPAREN : '}' {this.decreaseCurlyParenBalance();}->type(BLADE_PARAM_EXTRA);

BL_PARAM_LPAREN : '(' {this.increaseRoundParenBalance();}->type(BLADE_PARAM_EXTRA);
BL_PARAM_RPAREN : ')' {consumeParamRParen();};

BL_PARAM_STRING : DOUBLE_QUOTED_STRING_FRAGMENT | SINGLE_QUOTED_STRING_FRAGMENT;

BL_PARAM_PHP_VARIABLE : PhpVariable->type(PHP_VARIABLE);

BL_PARAM_ASSIGN : '=>';

BL_PARAM_PHP_KEYWORD : PhpKeyword->type(PHP_KEYWORD);

BL_PARAM_CONCAT_OPERATOR : '.';

BL_COMMA_EL : ','  {this.consumeBladeParamComma();};

BL_PARAM_WS : [ \t\r\n]+->skip;

BL_NAME_STRING : NameString;

BL_PARAM_MORE : . ->type(BLADE_PARAM_EXTRA);

BL_PARAM_EXIT_EOF : EOF->type(ERROR),popMode;

//@php @endphp
mode BLADE_INLINE_PHP;

PHP_D_BLADE_COMMENT : ('//' ~[\n\r]+)->skip;
PHP_D_BLADE_ML_COMMENT : '/*' .*? '*/' [\n\r]*->skip;

D_ENDPHP : '@endphp'->popMode;
PHP_D_UNKNOWN : '@'->type(HTML),popMode;

//hack to merge all php inputs into one token
PHP_D_EXPR_SQ_LPAREN : '[' ->type(PHP_EXPRESSION);
PHP_D_EXPR_SQ_RPAREN : ']' ->type(PHP_EXPRESSION);

PHP_D_EXPR_CURLY_LPAREN : '{' ->type(PHP_EXPRESSION);
PHP_D_EXPR_CURLY_RPAREN : '}' ->type(PHP_EXPRESSION);

PHP_D_EXPR_STRING : (DOUBLE_QUOTED_STRING_FRAGMENT | SINGLE_QUOTED_STRING_FRAGMENT)->type(PHP_EXPR_STRING);

//EXPR_ASSIGN : '=>'->type(BL_PARAM_ASSIGN);

PHP_D_COMPOSED_EXPR_PHP_VAR : PhpVariable->type(PHP_VARIABLE);
PHP_D_NEW : 'new' {this._input.LA(1) == ' '}? ->type(PHP_NEW);
PHP_D_CLASS : 'class' ->type(PHP_CLASS_KEYWORD);

PHP_D_COMPOSED_PHP_KEYWORD : PhpKeyword->type(PHP_KEYWORD);

PHP_D_NAMESPACE_PATH : ('\\'? (NameString '\\')+)->type(PHP_NAMESPACE_PATH);

PHP_D_COMPOSED_EXPR_PHP_CLASS_IDENTIFIER : '\\' NameString->type(PHP_IDENTIFIER);
PHP_D_COMPOSED_EXPR_PHP_IDENTIFIER : NameString->type(PHP_IDENTIFIER);
PHP_D_COMPOSED_EXPR_STATIC_ACCESS : '::'->type(PHP_STATIC_ACCESS);

PHP_D_COMPOSED_EXPR_LPAREN : '('->type(BLADE_EXPR_LPAREN);
PHP_D_COMPOSED_EXPR_RPAREN : ')' ->type(BLADE_EXPR_RPAREN);
PHP_D_WS : ' ' ->skip;

PHP_D_EXIT_COMPOSED_EXPRESSION_EOF : EOF->type(ERROR),popMode;

//untreated
PHP_D_PHP_COMPOSED_EXPRESSION : . ->skip;

//php inline <?php ?>
//might think to skip tokens which are not used ??
mode INSIDE_PHP_INLINE;

PHP_EXIT : '?>'->popMode;

PHP_INLINE_COMMENT : ('//' ~[\n\r]+)->skip;
PHP_INLINE_ML_COMMENT : ('/*' .*? '*/')->skip;
//hack to merge all php inputs into one token
PHP_EXPR_SQ_LPAREN : '[' ->type(PHP_EXPRESSION);
PHP_EXPR_SQ_RPAREN : ']' ->type(PHP_EXPRESSION);

PHP_EXPR_CURLY_LPAREN : '{' ->type(PHP_EXPRESSION);
PHP_EXPR_CURLY_RPAREN : '}' ->type(PHP_EXPRESSION);

PHP_EXPR_STRING : (DOUBLE_QUOTED_STRING_FRAGMENT | SINGLE_QUOTED_STRING_FRAGMENT)->type(PHP_EXPR_STRING);

//EXPR_ASSIGN : '=>'->type(BL_PARAM_ASSIGN);

PHP_COMPOSED_EXPR_PHP_VAR : PhpVariable->type(PHP_VARIABLE);
PHP_COMPOSED_PHP_KEYWORD : PhpKeyword->type(PHP_KEYWORD);
PHP_COMPOSED_EXPR_PHP_IDENTIFIER : NameString->type(PHP_IDENTIFIER);
PHP_COMPOSED_EXPR_STATIC_ACCESS : '::'->type(PHP_STATIC_ACCESS);
//need to implement alias access
PHP_COMPOSED_EXPR_INSTANCE_ACCESS : '->'->type(PHP_INSTANCE_ACCESS);

PHP_COMPOSED_EXPR_LPAREN : '('->type(BLADE_EXPR_LPAREN);
PHP_COMPOSED_EXPR_RPAREN : ')' ->type(BLADE_EXPR_RPAREN);


PHP_EXIT_COMPOSED_EXPRESSION_EOF : EOF->type(PHP_EXPRESSION),popMode;

PHP_PHP_COMPOSED_EXPRESSION : . ->type(PHP_EXPRESSION);

mode VERBATIM_MODE;

D_ENDVERBATIM_IN_MODE : '@endverbatim'->type(D_ENDVERBATIM), mode(DEFAULT_MODE);

//hack to merge all php inputs into one token
VERBATIM_HTML : . {
        this._input.LA(1) == '@' &&
        this._input.LA(2) == 'e' &&
        this._input.LA(3) == 'n' &&
        this._input.LA(4) == 'd' &&
        this._input.LA(5) == 'v' &&
        this._input.LA(6) == 'e' &&
        this._input.LA(7) == 'r'
      }? ->skip;


EXIT_VERBATIM_MOD_EOF : EOF->type(ERROR),mode(DEFAULT_MODE);

VERBATIM_HTML_MORE : . ->more;
