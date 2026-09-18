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
lexer grammar BladePhpCommonLexer;

tokens {
    HTML, 
    BLADE_COMMENT
}

fragment Identifier 
    : [a-z_\u0080-\ufffe][a-z0-9_\u0080-\ufffe]*;    

fragment HtmlIdentifier 
    : [a-z_\u0080-\ufffe][a-z0-9_\u0080-\ufffe-]*;    

fragment ESC_DOUBLE_QUOTED_STRING 
    : [\\"];

fragment DOUBLE_QUOTED_STRING_FRAGMENT 
    : '"' (ESC_DOUBLE_QUOTED_STRING | . )*? '"';

fragment SINGLE_QUOTED_STRING_FRAGMENT 
    : '\'' (~('\'' | '\\') | '\\' . )* '\'';

fragment StringLiteral : DOUBLE_QUOTED_STRING_FRAGMENT | SINGLE_QUOTED_STRING_FRAGMENT;

fragment LineComment
    : '//' ~ [\r\n]*
    ;

fragment PhpVariable
    : '$' Identifier;


fragment Digit
    : ('0'..'9');

BLADE_COMMENT_START : '{{--' ->pushMode(INSIDE_BLADE_COMMENT), skip;

EMAIL_SUBSTRING : ('@' Identifier '.')->skip;

VERSION_WITH_AT: '@' (Digit '.'?)+->skip;

//escapes
D_ESCAPES 
    : (
      '{{{'
    |  '@@' '@'?
    | '@{' '{'?
    | '@media' [ ]+ ('screen' [ ]+ 'and'?)?
    | ( '@charset' | '@import' | '@namespace' | '@document' | '@font-face'
       | '@page' | '@layer' | '@supports' | '@tailwind' | '@apply' | '@-webkit-keyframes' 
       | '@keyframes' | '@counter-style' | '@font-feature-values' | '@property'
       | '@scope' | '@starting-style' | '@supports' | '@view-transition'
       | '@container' | '@color-profile' | '@styleset' | '@font-palette-values' | '@media'
      ) [ ]*
    )->type(HTML);

mode INSIDE_BLADE_COMMENT;

BLADE_COMMENT_END : '--}}'->popMode, skip;

//hack to merge all php inputs into one token
BLADE_COMMENT_PEEK : . {
        this._input.LA(1) == '-' &&
        this._input.LA(2) == '-' &&
        this._input.LA(3) == '}' &&
        this._input.LA(4) == '}'
      }? ->skip;
BLADE_COMMENT_MORE : . ->skip;

BLADE_COMMENT_EOF : EOF->popMode, skip;