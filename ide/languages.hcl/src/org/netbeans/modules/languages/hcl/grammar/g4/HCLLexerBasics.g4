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
lexer grammar HCLLexerBasics;

fragment Ws
    : Hws
    | Vws
    ;

fragment Hws
    : [ \t]
    ;

fragment Vws
    : '\r'? '\n'
    ;

fragment NonVws
    : ~[\r\n]
    ;

fragment BlockComment
    : '/*' .*? ('*/' | EOF)
    ;

fragment LineComment
    : '#' ~ [\r\n]*
    | '//' ~ [\r\n]*
    ;

fragment Letter
    : [a-zA-Z\-_]
    ;

fragment LetterDigit
    : Letter
    | [0-9]
    ;

fragment HereDocIntro
    : '<<' '-'?
    ;

fragment DecDigit
    : [0-9]
    ;

fragment ExpMark
    : ('e' | 'E') (Plus | Minus)?
    ;

fragment NewLine
    : '\r'? '\n'
    ;

fragment Esc
    : '\\'
    ;

fragment HexDigit
    : [0-9a-fA-F]
    ;

fragment EscSeq
    : Esc ([btnfr"\\] | UnicodeEsc | . | EOF)
    ;

fragment EscAny
    : Esc .
    ;

fragment InterpolationStart
    : '${' '~'?
    ;

fragment EscInterpolation
    : '$${'
    ;

fragment TemplateStart
    : '%{' '~'?
    ;

fragment TemplateEnd
    : '~'? '}'
    ;

fragment EscTemplate
    : '%%{'
    ;

// The Lexer is a bit loose here on purpose, keep the token type during editing
fragment UnicodeEsc
    : 'u' HexDigit*
    ;

fragment True
    : 'true'
    ;

fragment False
    : 'false'
    ;

fragment Null
    : 'null'
    ;

fragment Colon
    : ':'
    ;

fragment DQuote
    : '"'
    ;

fragment LParen
    : '('
    ;

fragment RParen
    : ')'
    ;

fragment LBrace
    : '{'
    ;

fragment RBrace
    : '}'
    ;

fragment LBrack
    : '['
    ;

fragment RBrack
    : ']'
    ;

fragment RArrow
    : '=>'
    ;

fragment Lt
    : '<'
    ;

fragment Gt
    : '>'
    ;

fragment Equal
    : '='
    ;

fragment Question
    : '?'
    ;

fragment Star
    : '*'
    ;

fragment Slash
    : '/'
    ;

fragment Percent
    : '%'
    ;

fragment Minus
    : '-'
    ;

fragment Plus
    : '+'
    ;

fragment Underscore
    : '_'
    ;

fragment Dollar
    : '$'
    ;

fragment Comma
    : ','
    ;

fragment Dot
    : '.'
    ;

fragment Bang
    : '!'
    ;

fragment Ellipsis
    : '...'
    ;
    
fragment Or
    : '||'
    ;

fragment And
    : '&&'
    ;

fragment IdContinue
   : IdStart
   | '0' .. '9'
   | Underscore
   | Minus
   | '\u00B7'
   | '\u0300' .. '\u036F'
   | '\u203F' .. '\u2040'
   ;

fragment IdStart
   : 'A' .. 'Z'
   | 'a' .. 'z'
   | '\u00C0' .. '\u00D6'
   | '\u00D8' .. '\u00F6'
   | '\u00F8' .. '\u02FF'
   | '\u0370' .. '\u037D'
   | '\u037F' .. '\u1FFF'
   | '\u200C' .. '\u200D'
   | '\u2070' .. '\u218F'
   | '\u2C00' .. '\u2FEF'
   | '\u3001' .. '\uD7FF'
   | '\uF900' .. '\uFDCF'
   | '\uFDF0' .. '\uFFFD'
   ;
