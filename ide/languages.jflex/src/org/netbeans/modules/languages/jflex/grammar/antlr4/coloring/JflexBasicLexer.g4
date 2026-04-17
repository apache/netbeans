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
lexer grammar JflexBasicLexer;

fragment BlockComment
   : '/*' .*? ('*/' | EOF)
   ;

fragment DocComment
   : '/**' .*? ('*/' | EOF)
   ;

fragment LineComment
   : '//' ~ [\r\n]*
   ;

fragment AllCommentTypes
    : BlockComment | DocComment | LineComment
    ;

fragment Whitespace
    : InlineWS
    | NewLineWS
;

fragment InlineWS
    : [ \t]
    ;

fragment NewLineWS
    : [\r\n\f]
;

fragment EscSeq
   : Esc ([btnfr"'\\] | UnicodeEsc | . | EOF)
   ;

fragment Esc
   : '\\'
   ;

fragment UnicodeEsc
   : 'u' (HexDigit (HexDigit (HexDigit HexDigit?)?)?)?
   ;

fragment DecimalNumeral
   : '0'
   | [1-9] DecDigit*
   ;
   // -----------------------------------
   // Digits

fragment HexDigit
   : [0-9a-fA-F]
   ;

fragment DecDigit
   : [0-9]
   ;

fragment CharLiteral
   : SQuote (EscSeq | ~ ['\r\n\\]) SQuote
   ;

fragment SQuoteLiteral
   : SQuote (EscSeq | ~ ['\r\n\\])* SQuote
   ;

fragment DQuoteLiteral
   : DQuote (EscSeq | ~ ["\r\n\\])* DQuote
   ;

fragment USQuoteLiteral
   : SQuote (EscSeq | ~ ['\r\n\\])*
;
    
fragment SQuote
   : '\''
   ;

fragment DQuote
   : '"'
   ;

fragment AsciChar
    : '\\' ('u' | 'x') [a-zA-Z0-9]+
    ;

fragment MiscRegexChars
    : ('=' ('\\"' | [']) | '.') 
    | '~'? RegexEsc 
    | '~\''
    ;

fragment PredefinedCharacters
    : ':letter:'
    | ':jletter:'
    | ':jletterdigit:'
    | ':digit:'
    | ':uppercase:'
    | ':lowercase:'
;

fragment Identifier 
    : [a-zA-Z_\u0080-\ufffe][a-zA-Z0-9_\u0080-\ufffe]*;    

fragment Macro
    : '{' Identifier '}'
    ;

fragment RegexOperator
    : ('|' | '~' | '&' | '--')
    ;

fragment Quantifiers
    : ('^' | '+' | '*' | '?' | '!')
    | '{' [1-9][0-9]* (',' [1-9][0-9]*)? '}'
    ;

fragment RegexEsc
    : '\\' ('\\')? ('\\' | '{' | '}' | '"' | 'r' | 'n' | 't' | 'd' | 'b' | 'f' | '*' | '`' | '\'')?
    | '\\' '\\' '\\' '"'
    ;