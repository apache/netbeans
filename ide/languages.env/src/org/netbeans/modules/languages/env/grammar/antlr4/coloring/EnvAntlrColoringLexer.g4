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
lexer grammar EnvAntlrColoringLexer;

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

package org.netbeans.modules.languages.env.grammar.antlr4.coloring;}

tokens { 
    NL,
    WS,
    COMMENT,
    STRING,
    VALUE,
    KEY,
    KEYWORD,
    OPERATOR,
    ASSIGN_OPERATOR,
    DOLLAR,
    DELIMITATOR
}

options { 
    superClass = LexerAdaptor;
    caseInsensitive = true; 
}    

fragment Esc
   : '\\'
   ;

fragment SQuote
   : '\''
   ;

fragment DQuote
   : '"'
   ;

fragment BackTickQuote
   : '`'
   ;

fragment SQuoteLiteral
   : SQuote (Esc [btnfr"'\\] | ~ ['\\])* SQuote
   ;

fragment NewLine
    : [\r\n]
    ;

fragment NewLineComment
    : '#' ~ [\r\n]* (NL | EOF)
    ;

fragment Identifier 
    : [a-z_\u0080-\ufffe][a-z0-9_.\u0080-\ufffe-]*;

fragment KeyIdentiifier
    : [a-z_]+[a-z0-9_]*
    ;

KEY
    : KeyIdentiifier
    ;
COMMENT
    : NewLineComment
    ;
ASSIGN_OPERATOR
    : ('=' | ':')->pushMode(VarAssign)
    ;
NL
    : NewLine+
    ;
WS
    : [ \t]+ ->skip
    ;
ERROR
    : .
    ;
mode VarAssign;
DB_STRING_OPEN
    : DQuote ->type(STRING),pushMode(DbQuoteString)
    ;

B_STRING_OPEN
    : BackTickQuote ->type(STRING),pushMode(BackQuotedString)
    ;

SG_STRING_OPEN
    : SQuoteLiteral ->type(STRING)
    ;
EXIT_COMMENT 
    : (' ')+ NewLineComment->type(COMMENT), popMode
    ;
DELIMITATOR_VAR
    : (',' | '|' | ':') ->type(DELIMITATOR)
    ;
KEYWORD_VAR
    : (
    'true' | 'false' | 'null' | 'on' | '?'+
    | 'prod' | 'production' | 'live'
    | 'development' | 'local' | 'test'
    ) {this._input.LA(1) == '\n'}?->type(KEYWORD)
    ;
INTERPOLATED_VAR 
    : '$' {this._input.LA(1) == '{'}? 
    ->type(DOLLAR),pushMode(StringInterpolation)
;

//greedy identifier matching
IDENTIFIER_VAR
    : Identifier {this._input.LA(1) == '\n'}? 
    ->type(VALUE)
    ;
EXIT_VAR_ASSING : NewLine->type(NL), popMode;
INLINE_WS : [ \t]+->skip;
ANY_VALUE : . ->type(VALUE);

mode DbQuoteString;

DBQ_TEXT : (Esc [btnfr"'\\] | ~ [$"\r\n\\])+->type(STRING);
DBQ_INTERPOLATED_VAR 
    : '$' {this._input.LA(1) == '{'}? 
    ->type(DOLLAR),pushMode(StringInterpolation)
;
DBQ_STRING_CLOSE : DQuote ->type(STRING),popMode;
ANY_DBQ_TEXT : . ->type(STRING);

mode BackQuotedString;

BQ_TEXT : (Esc [btnfr"'`\\] | ~ [$`\r\n\\])+->type(STRING);
BQ_INTERPOLATED_VAR 
    : '$' {this._input.LA(1) == '{'}? 
    ->type(DOLLAR),pushMode(StringInterpolation)
;
BQ_STRING_CLOSE : BackTickQuote ->type(STRING),popMode;
ANY_BQ_TEXT : . ->type(STRING);

mode StringInterpolation;

CURLY_OPEN 
    : '{' {this.resetInterpolationKeyAdded();}
    ;
CURLY_CLOSE
    : '}' ->popMode
    ;
INTERPOLATION_VAR
    : {!this.keyTokenAdded()}? KeyIdentiifier {this.consumeKeyToken();}->type(KEY)
    ;

/*
from https://dotenvx.com/docs/env-file
${VAR:-default} -> value of VAR if set and non-empty, otherwise default
${VAR-default} -> value of VAR if set, otherwise default

${VAR:+alternate} -> value of alternate if VAR is set and non-empty, otherwise empty ''
${VAR+alternate} -> value of alternate if VAR is set and non-empty, otherwise empty ''
*/
INTERPOLATION_OPERATOR
    : (':' ('+' | '-')? | '?' | '+' | '-')
    ;

VALUE_INTERPOLATION
    : . ->type(VALUE)
    ;