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
lexer grammar BladeAntlrColoringLexer;
import BladeColoringCommonLexer;

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
    superClass = ColoringLexerAdaptor;
    caseInsensitive = true;
 }

channels {PHP_CODE}

tokens {
 DIRECTIVE,
 BLADE_PAREN,
 PHP_EXPRESSION,
 BLADE_PHP_ECHO_EXPR,
 BLADE_PHP_INLINE,
 RAW_TAG,
 CONTENT_TAG,
 HTML,
 HTML_TAG,
 ERROR
}

fragment DirectivesWithEndTag : 'for' ('each' | 'else')? | 'if' | 'while' 
   | 'section' | 'session' | 'once' | 'push' | 'PushOnce'
   | 'switch' | 'unless' | 'can' ('any' | 'not')? | 'env'
   | 'auth' | 'guest'  | 'error' | 'empty' | 'isset'
   //11.x
   | 'fragment';

fragment Include : '@include' ('If' | 'When' | 'First' | 'Unless')?;

fragment ComponentTagIdentifier 
    : [a-z_\u0080-\ufffe][a-z0-9.:\u0080-\ufffe-]*;   

//====================================================
//TOKENS:

PHP_INLINE : '<?=' .*? '?>' | '<?php' .*? ('?>' | EOF);

D_GENERIC_BLOCK_DIRECTIVES : ('@' DirectivesWithEndTag | '@sectionMissing' | '@hasSection') (' ')* {this._input.LA(1) == '('}? ->pushMode(INSIDE_PHP_EXPRESSION),type(DIRECTIVE);

D_SIMPLE_BLOCK_DIRECTIVES : '@' 'end'? ('empty' | 'production' | 'once') ->type(DIRECTIVE);

D_GENERIC_INLINE_DIRECTIVES : ('@elseif' |  Include | '@extends' | '@each' | '@yield' | '@props' | '@method' | '@elsecan' ('any' | 'not')?
   | '@class' | '@style' | '@aware' | '@break' | '@continue' | '@selected' | '@disabled' 
   | '@readonly' | '@required' | '@when' | '@bool') (' ')* {this._input.LA(1) == '('}? ->pushMode(INSIDE_PHP_EXPRESSION),type(DIRECTIVE);

D_GENERIC_INLINE_MIXED_DIRECTIVES : ('@break' | '@continue' | '@auth' | '@guest')->type(DIRECTIVE);

D_GENERIC_END_TAGS : ('@stop' | '@show' | '@overwrite' | '@viteReactRefresh' | '@end' DirectivesWithEndTag)->type(DIRECTIVE);

//verbatim has special blade escape logic
D_VERBATIM : '@verbatim' ->pushMode(VERBATIM_MODE), type(DIRECTIVE);
D_ENDVERBATIM : '@endverbatim'->type(DIRECTIVE);

D_MISC : ('@dd' | '@dump' | '@js' | '@json' | '@inject' | '@when' | '@bool') (' ')* {this._input.LA(1) == '('}? ->pushMode(INSIDE_PHP_EXPRESSION),type(DIRECTIVE);

D_SIMPLE : ('@else' 'guest'? | '@csrf' | '@default' | '@append' | '@parent')->type(DIRECTIVE);

//php emebeddings
D_PHP_SHORT : '@php' (' ')? {this._input.LA(1) == '('}? ->type(D_PHP),pushMode(INSIDE_PHP_EXPRESSION);
D_PHP : '@php'->pushMode(BLADE_INLINE_PHP);

//allow php expression highlight for custom directives which start with 'end' also
D_END_ARG : ('@end' NameString) (' ')* {this._input.LA(1) == '('}?->pushMode(INSIDE_PHP_EXPRESSION),type(DIRECTIVE);
D_END : ('@end' NameString)->type(DIRECTIVE);


D_ASSET_BUNDLER : '@vite' (' ')* {this._input.LA(1) == '('}? ->pushMode(INSIDE_PHP_EXPRESSION),type(DIRECTIVE);

//known plugins
D_LIVEWIRE : ('@livewireStyles' | '@bukStyles' | '@livewireScripts' | '@bukScripts' | '@click' ('.away')? '=')->type(DIRECTIVE);

D_SPATIE_ARG : ('@' ( ('unless' | 'has' ('any')? )?  'role') | 'haspermission') {this._input.LA(1) == '('}? ->pushMode(INSIDE_PHP_EXPRESSION),type(DIRECTIVE);

D_SPATIE : ('@end' ('unless' | 'has' ('any')?)? 'role' | 'haspermission')->type(DIRECTIVE);

D_CSS_AT_RULE : ('@supports' | '@container' | '@scope' | '@media') (' ')* {this._input.LA(1) == '('}? ->type(HTML);
//we will decide that a custom directive has expression to avoid email matching
D_CUSTOM : ('@' NameString (' ')* {this._input.LA(1) == '('}? ) ->pushMode(INSIDE_PHP_EXPRESSION);

D_UNKNOWN : '@' NameString->pushMode(ADIACENT_DIRECTIVE_TOKENS);

//hack to trigger completion handler which is stopped due to embedding context like HTML, PHP
D_AT : '@' (' ' | '>' | [\n\r])?;

//display
CONTENT_TAG_OPEN : '{{' ->pushMode(INSIDE_REGULAR_ECHO),type(CONTENT_TAG);
RAW_TAG_OPEN : '{!!' ->pushMode(INSIDE_RAW_ECHO),type(RAW_TAG);

JS_COMMENT : LineComment->type(HTML);
CSS_COMMENT : ('/*'  | '*/')->type(HTML);

HTML_WS : ((' ')+ | [\r\n]+)->type(HTML);

INCOMPLETE_BLADE_TAG : ('{!' | '{{-') ->type(HTML);

GENERAL_IDENTIFIER : '$'? NameString->type(HTML);

OTHER : . ->type(HTML);

//=========================================================
//=========================================================
//MODES
// {{  }}
mode INSIDE_REGULAR_ECHO;

CONTENT_TAG_CLOSE : ('}}')->popMode,type(CONTENT_TAG);

GREEDY_REGULAR_ECHO_EXPR : ~[ {}]+ {this.consumeEscapedEchoToken();};

ESCAPED_ECHO_EXPR : . [ ]* {this.consumeEscapedEchoToken();};
EXIT_ECHO_EOF : EOF->type(HTML),popMode;

//=========================================================
// {!!  !!}
mode INSIDE_RAW_ECHO;

RAW_TAG_CLOSE : ('!!}')->popMode, type(RAW_TAG);

RAW_ECHO_EXPR : ~[ !{}]+ {this.consumeNotEscapedEchoToken();};
RAW_ECHO_EXPR_MORE : . [ ]* {this.consumeNotEscapedEchoToken();};
EXIT_RAW_ECHO_EOF : EOF->type(HTML),popMode;

//=========================================================
// @directive (?)
mode INSIDE_PHP_EXPRESSION;

OPEN_PAREN : '(' {this.rParenBalance == 0}? {this.rParenBalance++;}->type(BLADE_PAREN);
OPEN_E_PAREN : '(' {this.rParenBalance++;}->type(PHP_EXPRESSION);
CLOSE_PAREN : ')' {this.rParenBalance <= 1}? {this.rParenBalance = 0;}->type(BLADE_PAREN),mode(DEFAULT_MODE);
CLOSE_E_PAREN : ')' {this.rParenBalance--;}->type(PHP_EXPRESSION);

PHP_EXPRESSION_COMMENT : ('/*' .*? '*/')->type(PHP_EXPRESSION);

PHP_EXPRESSION_GREEDY : ~[()]+ ->type(PHP_EXPRESSION);

PHP_EXPRESSION_MORE : . ->type(PHP_EXPRESSION);

EXIT_EOF : EOF->type(ERROR),mode(DEFAULT_MODE);

//=========================================================
// @php
mode BLADE_INLINE_PHP;

D_ENDPHP : '@endphp'->popMode;

BLADE_PHP_INLINE : ~[@]+;
BLADE_PHP_INLINE_MORE : . ->type(BLADE_PHP_INLINE);

EXIT_INLINE_PHP_EOF : EOF->type(ERROR),popMode;

//=========================================================
// @verbatim
mode VERBATIM_MODE;

D_ENDVERBATIM_IN_MODE : '@endverbatim'->type(DIRECTIVE), popMode;

//hack to merge all php inputs into one token
VERBATIM_HTML : ~[@]+ ->type(HTML);
VERBATIM_HTML_MORE : . ->type(HTML);

EXIT_VERBATIM_MOD_EOF : EOF->type(ERROR),popMode;

//=========================================================
mode ADIACENT_DIRECTIVE_TOKENS;

TOKEN_ADIACENT_DIRECTIVE : (' ' | '>' | [\n\r] | '"')->type(D_UNKNOWN),popMode;

TOKEN_ADIACENT_DIRECTIVE_OTHER : . ->type(HTML),popMode;
TOKEN_ADIACENT_DIRECTIVE_EOF : EOF->type(ERROR),popMode;