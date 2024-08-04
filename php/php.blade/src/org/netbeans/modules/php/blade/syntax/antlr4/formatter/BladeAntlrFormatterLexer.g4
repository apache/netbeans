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
lexer grammar BladeAntlrFormatterLexer;

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
package org.netbeans.modules.php.blade.syntax.antlr4.formatter;
}

options { 
    superClass = LexerAdaptor;
    caseInsensitive = true;
}
 
//we will hide html in the end
tokens {HTML, PHP_CODE, PARAM_COMMA}
    
    channels { COMMENT }
    
    fragment DirectiveLabel 
    : [a-z\u0080-\ufffe][a-z0-9_\u0080-\ufffe]*;

fragment Identifier
    : [a-z\u0080-\ufffe][a-z0-9_\u0080-\ufffe]*;

fragment CompomentIdentifier
    : [a-z\u0080-\ufffe][a-z0-9_.:\u0080-\ufffe]*;

fragment DirectiveArgLookup
    : (' ')* {this._input.LA(1) == '('}?;

fragment DOUBLE_QUOTED_STRING_FRAGMENT 
    : '"' ([\\"] | . )*? '"';

fragment SINGLE_QUOTED_STRING_FRAGMENT 
    : '\'' (~('\'' | '\\') | '\\' . )* '\'';

fragment BlockDirectiveName 
    : 'auth' | 'guest'
    | 'if' | 'can' ('any' | 'not')? | 'for' ('each' | 'else')? 
    | 'while' | 'hasSection' | 'sectionMissing' | 'fragment' | 'verbatim'
    | 'isset' | 'unless' | 'empty' 
    | 'session'
    | 'env' | 'once' | 'error'
    | 'push' ('if' | 'once')? | 'prepend' | 'switch';

PHP_INLINE : '<?=' .*? '?>' | '<?php' .*? '?>';
//
D_ESCAPES 
    : (
    '{{{'
    |  '@@' '@'?
    | '@{' '{'?
    | '@media' [ ]*
    | '@charset'
    | '@import'
    | '@namespace'
    | '@document'
    | '@font-face'
    | '@page'
    | '@supports'
    | '@layer'
    | '@tailwind'
    | '@apply' 
    | '@-webkit-keyframes' 
    | '@keyframes'
    )->type(HTML);

D_BLOCK_DIRECTIVE_START : ('@' BlockDirectiveName DirectiveArgLookup)->pushMode(DIRECTIVE_ARG);
D_BLOCK_DIRECTIVE_START_NO_ARG : '@' ('auth' | 'production')->type(D_BLOCK_DIRECTIVE_START);
D_BLOCK_DIRECTIVE_END : '@end' BlockDirectiveName | '@endphp';

D_SECTION : ('@section' DirectiveArgLookup)->pushMode(DIRECTIVE_ARG_WITH_PARAM);
D_ENDSECTION : '@endsection' | '@show' | '@append' | '@stop';
D_BLOCK_ALIGNED_DIRECTIVE : '@else' | '@elseif' | '@empty';
NON_PARAM_DIRECTIVE : '@continue' | '@break';

D_INLINE_DIRECTIVE : '@' DirectiveLabel DirectiveArgLookup | '@csrf';

STRING : DOUBLE_QUOTED_STRING_FRAGMENT | SINGLE_QUOTED_STRING_FRAGMENT;

CONTENT_TAG_OPEN : '{{' ->pushMode(INSIDE_REGULAR_ECHO);
RAW_TAG_OPEN : '{!!' ->pushMode(INSIDE_RAW_ECHO);

SG_QUOTE : '\'';
DB_QUOTE : '"';

HTML_CLOSE_TAG : ('<' (' ')* '/' (' ')*  [a-z\u0080-\ufffe][a-z0-9_.\u0080-\ufffe]* (' ')* '>') 
| ('</' (' ')* ('x-'  CompomentIdentifier |  CompomentIdentifier ('::' CompomentIdentifier)+ | 
  'livewire:' CompomentIdentifier ('-' CompomentIdentifier)*) (' ')* '>') 
;
HTML_COMMENT: '<!--' .*? '-->';
HTML_START_BLOCK_TAG : '<' ('div'
    | 'section' | 'main' | 'article'
    | 'html' | 'title' | 'head' | 'style' | 'script' | 'footer'
    | 'pre' | 'code' | 'blockquote'
    | 'dt' | 'dl' | 'video'
    | 'template'
    | 'span' | 'strong' | 'em' | 'small' | 'sub' | 'sup'
    | 'figure' | 'canvas' | 'svg' | 'use' | 'path' | 'polygon' | 'picture'
    | 'header' | 'h' [1-9] | 'nav'
    | 'dialog'
    | 'summary' | 'details' | 'slot'
    | 'label' | 'select' | 'optgroup' | 'option' | 'fieldset' | 'textarea' | 'button' | 'form' | 'search'
    | 'ul' | 'ol' | 'li'
    | 'table' | 'tr' | 'td' | 'th' | 'tbody' | 'thead' | 'tfoot' | 'caption' |
    | 'time' |
    | 'var' | 'q' | 'p' | 'a' | 'b' | 'i') {this._input.LA(1) == '>' || this._input.LA(1) == '@' || this._input.LA(1) == ' ' || this._input.LA(1) == '\n'}?;


HTML_SELF_CLOSE_TAG : '<' ('img' | 'input' | 'br' | 'hr' | 'link' | 'meta');

COMPONENT_TAG : '<x-' CompomentIdentifier | '<' CompomentIdentifier ('::' CompomentIdentifier)+ | '<livewire:' CompomentIdentifier;

EQ : '=';
IDENTIFIER : Identifier;
INLINE_GT_SYMBOL : '/>';
GT_SYMBOL : '>';

D_PHP : '@php' {this._input.LA(1) == ' ' || this._input.LA(1) == '\n'}?->pushMode(BLADE_INLINE_PHP);

AT : '@' ->skip;

WS : ((' ') | [\t])+;
NL : [\r\n];



OTHER : . ->skip;

mode DIRECTIVE_ARG;

D_ARG_LPAREN : '(' {this.consumeDirectiveArgLParen();};
D_ARG_RPAREN : ')' {this.consumeDirectiveArgRParen();};

D_ARG_NL : [\r\n]->type(NL);

PHP_EXPR : . ->skip;

EXIT_EOF : EOF->popMode;

mode DIRECTIVE_ARG_WITH_PARAM;

D_ARG_PARAM_LPAREN : '(' {this.consumeDirectiveArgLParen();};
D_ARG_PARAM_RPAREN : ')' {this.consumeDirectiveArgRParen();};

BL_SQ_LPAREN : '[' {this.squareParenBalance++;}->skip;
BL_SQ_RPAREN : ']' {this.squareParenBalance--;}->skip;

BL_CURLY_LPAREN : '{' {this.curlyParenBalance++;}->skip;
BL_CURLY_RPAREN : '}' {this.curlyParenBalance--;}->skip;

D_ARG_COMMA_EL : ','  {this.consumeBladeParamComma();};
D_ARG_PARAM_NL : [\r\n]->type(NL);

BL_PHP_EXPR : . ->skip;

BL_EXIT_EOF : EOF->popMode;

mode BLADE_INLINE_PHP;

D_ENDPHP : '@endphp'->popMode;

PHP_CODE_GREEDY : ~[@]+->type(PHP_CODE);

PHP_CODE_COMPLETION : . ->type(PHP_CODE);

// {{  }}
mode INSIDE_REGULAR_ECHO;

CONTENT_TAG_CLOSE : ('}}')->popMode;
CONTENT_OTHER : . ->skip;
EXIT_REGULAR_ECHO_EOF : EOF->popMode;

// {!!  !!}
mode INSIDE_RAW_ECHO;

RAW_TAG_CLOSE : ('!!}')->popMode;
RAW_CONTENT_OTHER : . ->skip;
EXIT_RAW_ECHO_EOF : EOF->popMode;