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
 PHP_EXPRESSION,
 BLADE_PHP_ECHO_EXPR,
 RAW_TAG,
 CONTENT_TAG,
 HTML,
 HTML_TAG,   
 ERROR
}

fragment NekudoWithelistMatch : '::' | '?:' | ' : ';

fragment DirectivesWithEndTag : 'for' ('each')? | 'if' | 'while' 
   | 'section' | 'session' | 'once' | 'push' | 'PushOnce'
   | 'switch' | 'unless' | 'can' ('any' | 'not')?
   | 'auth' | 'guest'
   | 'error' | 'production' | 'empty';

fragment Include : '@include' ('If' | 'When' | 'First' | 'Unless')?;

//to remove
fragment DOUBLE_QUOTED_STRING_FRAGMENT_WITH_PHP 
    : '"' (ESC_DOUBLE_QUOTED_STRING | '{' PhpVariable '}' | . | ~[:])*?  '"';

fragment ComponentTagIdentifier 
    : [a-z_\u0080-\ufffe][a-z0-9.:\u0080-\ufffe-]*;   

//??
fragment SpecialChars : '°';

PHP_INLINE : '<?=' .*? '?>' | '<?php' .*? ('?>' | EOF);

D_GENERIC_BLOCK_DIRECTIVES : ('@' DirectivesWithEndTag | '@sectionMissing' | '@hasSection')->pushMode(LOOK_FOR_PHP_EXPRESSION),type(DIRECTIVE);

D_GENERIC_INLINE_DIRECTIVES : ('@elseif' |  Include | '@extends' | '@each' | '@yield' | '@props' | '@method' 
   | '@class' | '@style' | '@aware' | '@break' | '@continue' | '@selected' | '@disabled' | '@readonly' | '@required') (' ')+ {this._input.LA(1) == '('}? ->pushMode(LOOK_FOR_PHP_EXPRESSION),type(DIRECTIVE);

D_GENERIC_INLINE_MIXED_DIRECTIVES : ('@break' | '@continue')->type(DIRECTIVE);

D_GENERIC_END_TAGS : ('@stop' | '@show' | '@overwrite' | '@viteReactRefresh' | '@end' DirectivesWithEndTag)->type(DIRECTIVE);

//verbatim has special blade escape logic
D_VERBATIM : '@verbatim' ->pushMode(VERBATIM_MODE), type(DIRECTIVE);
D_ENDVERBATIM : '@endverbatim'->type(DIRECTIVE);

D_MISC : ('@dd' | '@dump' | '@js' | '@json' | '@inject')->pushMode(LOOK_FOR_PHP_EXPRESSION),type(DIRECTIVE);

D_SIMPLE : ('@else' | '@csrf' | '@default' | '@append' | '@parent')->type(DIRECTIVE);

//php emebeddings
D_PHP_SHORT : '@php' (' ')? {this._input.LA(1) == '('}? ->type(D_PHP),pushMode(LOOK_FOR_PHP_EXPRESSION);
D_PHP : '@php'->pushMode(BLADE_INLINE_PHP);

//allow php expression highlight for custom directives which start with 'end' also
D_END : ('@end' NameString)->pushMode(LOOK_FOR_PHP_EXPRESSION),type(DIRECTIVE);

//known plugins
D_LIVEWIRE : ('@livewireStyles' | '@bukStyles' | '@livewireScripts' | '@bukScripts' | '@click' ('.away')? '=')->type(DIRECTIVE);
D_ASSET_BUNDLER : '@vite'->pushMode(LOOK_FOR_PHP_EXPRESSION),type(DIRECTIVE);

//we will decide that a custom directive has expression to avoid email matching
D_CUSTOM : ('@' NameString {this._input.LA(1) == '(' || 
        (this._input.LA(1) == ' ' && this._input.LA(2) == '(')}? ) ->pushMode(LOOK_FOR_PHP_EXPRESSION);

D_UNKNOWN : '@' NameString;

//TODO move all known directives to fragment?
//hack to allow completion for directives
//it doesn't trigger completion
D_AT : '@' (' ' | '>' | [\n\r])?;

//display
CONTENT_TAG_OPEN : '{{' ->pushMode(INSIDE_REGULAR_ECHO),type(CONTENT_TAG);
RAW_TAG_OPEN : '{!!' ->pushMode(INSIDE_RAW_ECHO),type(RAW_TAG);

CSS_COMMENT : ('/*' .*? '*/' | '//')->type(HTML);

HTML_X : ('<x-' ComponentTagIdentifier | '<' ComponentTagIdentifier ('::' ComponentTagIdentifier)+)->type(HTML),pushMode(INSIDE_HTML_COMPONENT_TAG);

CLOSE_TAG : ('</' FullIdentifier '>' [\n\r ]*)+->type(HTML);

HTML : ((' ')+ | [\r\n]+ | ComponentTagIdentifier | SpecialChars | '"' {this._input.LA(1) != '@'}? | '\\\'' {this._input.LA(1) != '@'}? | '_' | '.' 
| ',' | '=' | [()-;]+ | '[' | ']' )* '<' {this._input.LA(1) != 'x' && this._input.LA(1) != '?' && this._input.LA(2) != 'p'}? ->pushMode(INSIDE_HTML_TAG),more;

HTML_MISC : ((' ')+ | [\r\n]+ | ('#' | '.')? ComponentTagIdentifier | SpecialChars | '"' {this._input.LA(1) != '@'}?
| ',' | '\\\'' | '_' | '.' | '=' | [()-;]+ | '[' | ']'  )+->type(HTML);

HTML_WS : ((' ')+ | [\r\n]+)->type(HTML);

INCOMPLETE_BLADE_TAG : ('{!' | '{{-') ->type(HTML);

OTHER : . ->type(HTML);

mode INSIDE_HTML_TAG;

OTHER_HTML_POP : . {this._input.LA(1) == '@' || this._input.LA(1) == '{' || (this._input.LA(1) == '<' && (this._input.LA(2) == 'x' || this._input.LA(2) == '?'))}? ->type(HTML_TAG), popMode;

OTHER_HTML : . ->more;

HTML_EOF : EOF->type(HTML_TAG),popMode;

// {{  }}
mode INSIDE_REGULAR_ECHO;

CONTENT_TAG_CLOSE : ('}}')->popMode,type(CONTENT_TAG);
//hack due to a netbeans php embedding issue when adding or deleting ':' chars
ECHO_DOUBLE_NEKODU : NekudoWithelistMatch {this.consumeEscapedEchoToken();};
ECHO_STRING_LITERAL : (SINGLE_QUOTED_STRING_FRAGMENT | DOUBLE_QUOTED_STRING_FRAGMENT_WITH_PHP) {this.consumeEscapedEchoToken();};
ECHO_PHP_FREEZE_SYNTAX : (':)' | ':') ->skip;

GREEDY_REGULAR_ECHO_EXPR : ~[ ':{}]+ {this.consumeEscapedEchoToken();};

ESCAPED_ECHO_EXPR : . [ ]* {this.consumeEscapedEchoToken();};
EXIT_ECHO_EOF : EOF->type(ERROR),popMode;

// {!!  !!}
mode INSIDE_RAW_ECHO;

RAW_TAG_CLOSE : ('!!}')->popMode, type(RAW_TAG);
//hack due to a netbeans php embedding issue when adding or deleting ':' chars
RAW_ECHO_DOUBLE_NEKODU : NekudoWithelistMatch {this.consumeNotEscapedEchoToken();};
RAW_ECHO_STRING_LITERAL : (SINGLE_QUOTED_STRING_FRAGMENT | DOUBLE_QUOTED_STRING_FRAGMENT_WITH_PHP) {this.consumeNotEscapedEchoToken();};
RAW_ECHO_PHP_FREEZE_SYNTAX : (':)' | ':') ->skip;
RAW_ECHO_EXPR : ~[ ':!{}]+ {this.consumeNotEscapedEchoToken();};
RAW_ECHO_EXPR_MORE : . [ ]* {this.consumeNotEscapedEchoToken();};
EXIT_RAW_ECHO_EOF : EOF->type(ERROR),popMode;

// @directive ()?
mode LOOK_FOR_PHP_EXPRESSION;

WS_EXPR : [ ]+ {this._input.LA(1) == '('}? ->pushMode(INSIDE_PHP_EXPRESSION);
OPEN_EXPR_PAREN_MORE : '(' {this.increaseRoundParenBalance();} ->more,pushMode(INSIDE_PHP_EXPRESSION);

AFTER_DIRECTIVE : NameString->type(ERROR), popMode;
L_OTHER : . ->type(ERROR), popMode;

// @directive (?)
mode INSIDE_PHP_EXPRESSION;

OPEN_EXPR_PAREN : {this.roundParenBalance == 0}? '(' {this.increaseRoundParenBalance();} {this.consumeExprToken();};
CLOSE_EXPR_PAREN : {this.roundParenBalance == 1}? ')' 
    {this.decreaseRoundParenBalance();}->type(PHP_EXPRESSION),mode(DEFAULT_MODE);

LPAREN : {this.roundParenBalance > 0}? '(' {this.increaseRoundParenBalance();} {this.consumeExprToken();};
RPAREN : {this.roundParenBalance > 0}? ')' {this.decreaseRoundParenBalance();} {this.consumeExprToken();};

//in case of lexer restart context
EXIT_RPAREN : ')' {this.roundParenBalance == 0}?->type(PHP_EXPRESSION),mode(DEFAULT_MODE);

DB_STRING_OPEN : '"' ->more,pushMode(DB_STRING_MODE);
//hack due to a netbeans php embedding issue when adding or deleting ':' chars

SHORT_IF_EXPR_ERR : ('?:') {this.testForFreezeCombination();};

DOUBLE_NEKODU : ('::') {this.consumeExprToken();};


EXPR_STRING_LITERAL : (SINGLE_QUOTED_STRING_FRAGMENT (' ')*) {this.consumeExprToken();};

//STATIC_STRING : //check if start of token ... check if bracket and 

FREEZE_NEKUDO_GREEDY : ':' {this._input.LA(1) != ':'}?->skip;

FREEZE_NEKUDO : ':'->skip;

PHP_EXPRESSION_COMMENT : ('/*' .*? '*/')->skip;

PHP_EXPRESSION_MORE : . {this.consumeExprToken();};

EXIT_EOF : EOF->type(ERROR),mode(DEFAULT_MODE);

// @php
mode BLADE_INLINE_PHP;

D_ENDPHP : '@endphp'->popMode;

//hack to merge all php inputs into one token
BLADE_PHP_INLINE : . {
        this._input.LA(1) == '@' &&
        this._input.LA(2) == 'e' &&
        this._input.LA(3) == 'n' &&
        this._input.LA(4) == 'd' &&
        this._input.LA(5) == 'p' &&
        this._input.LA(6) == 'h' &&
        this._input.LA(7) == 'p'
      }?->channel(PHP_CODE) ;
BLADE_PHP_INLINE_MORE : . ->more;

EXIT_INLINE_PHP_EOF : EOF->type(ERROR),popMode;

// @verbatim
mode VERBATIM_MODE;

D_ENDVERBATIM_IN_MODE : '@endverbatim'->type(DIRECTIVE), popMode;

//hack to merge all php inputs into one token
VERBATIM_HTML : . {
        this._input.LA(1) == '@' &&
        this._input.LA(2) == 'e' &&
        this._input.LA(3) == 'n' &&
        this._input.LA(4) == 'd' &&
        this._input.LA(5) == 'v' &&
        this._input.LA(6) == 'e' &&
        this._input.LA(7) == 'r'
      }? ->type(HTML);
VERBATIM_HTML_MORE : . ->more;

EXIT_VERBATIM_MOD_EOF : EOF->type(ERROR),popMode;

mode INSIDE_HTML_COMPONENT_TAG;

COMPONENT_ATTRIBUTE : (':' FullIdentifier '="') ->type(HTML),pushMode(COMPONENT_PHP_EXPRESSION); 

COMPONENT_CONTENT_TAG_OPEN : '{{' ->pushMode(INSIDE_REGULAR_ECHO),type(CONTENT_TAG);
COMPONENT_RAW_TAG_OPEN : '{!!' ->pushMode(INSIDE_RAW_ECHO),type(RAW_TAG);

EXIT_HTML_COMPONENT : '>'->type(HTML), popMode;

HTML_COMPONENT_ANY : . ->type(HTML);

EXIT_HTML_COMPONENT_EOF : EOF->type(ERROR),popMode;

mode COMPONENT_PHP_EXPRESSION;

EXIT_COMPONENT_PHP_EXPRESSION : '"'->type(HTML), popMode;
COMPONENT_PHP_EXPRESSION_LAST : . {this._input.LA(1) == '"'}? ->type(PHP_EXPRESSION);
COMPONENT_PHP_EXPRESSION : . ->more;

EXIT_COMPONENT_PHP_EXPRESSION_EOF : EOF->type(ERROR),popMode;

mode DB_STRING_MODE;

DB_STRING_NEKUDO_GREEDY : NekudoWithelistMatch '$'? FullIdentifier '}' ->more;

DB_STRING_NEKUDO : NekudoWithelistMatch ->more;
//TODO numeric
DB_JSON_PAIR : '{' [\\']?  FullIdentifier [\\']? ':'+ [\\']?  FullIdentifier?  [\\']?  (',' ( [\\']?  FullIdentifier [\\']?  ':'+ [\\']?   FullIdentifier [\\']? ))* ','? '}' ->more;

PHP_INTERCALATED : '{' '$' FullIdentifier ('[' [\\'] FullIdentifier  [\\'] ']')* '::' FullIdentifier '}'->more;

DB_POINT : ('{' '$' FullIdentifier ('[' [\\'] FullIdentifier?  [\\'] ']')* (('::'+ FullIdentifier)? (':' FullIdentifier?)+ ) '}' | ':$')->type(ERROR);

DB_QUOTE_MORE : '\\"'->more;

DB_QUOTE_EXIT : '"'->more, popMode;

DB_QUOTE_ANY : . ->more;

DB_QUOTE_EOF : EOF->type(ERROR),popMode;