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
lexer grammar HCLLexer;
options { superClass = org.netbeans.modules.languages.hcl.HCLHereDocAdaptor; }
import HCLLexerBasics;

channels { COMMENTS }

BLOCK_COMMENT
    : BlockComment -> channel(COMMENTS)
    ;

LINE_COMMENT
    : LineComment -> channel(COMMENTS)
    ;

FOR
    : 'for'
    ;

IF
    : 'if'
    ;

IN
    : 'in'
    ;

HEREDOC_START
 : HereDocIntro Letter LetterDigit* NewLine {pushHereDocVar(getText());} -> pushMode(HEREDOC_MODE)
 ;

EQUALS
   : Equal Equal
   ;

RARROW
   : RArrow
   ;

EQUAL
   : Equal
   ;

LBRACE
   : LBrace
   ;

RBRACE
   : RBrace
   ;

LBRACK
   : LBrack
   ;

RBRACK
   : RBrack
   ;

LPAREN
   : LParen
   ;

RPAREN
   : RParen
   ;

QUESTION
   : Question
   ;

COLON
   : Colon
   ;

GTE
   : Gt Equal
   ;

GT
   : Gt
   ;

LTE
   : Lt Equal
   ;

LT
   : Lt
   ;

NOT_EQUALS
   : Bang Equal
   ;

NOT
   : Bang
   ;

COMMA
   : Comma
   ;

ELLIPSIS
   : Ellipsis
   ;

LEGACY_INDEX
    : Dot DecDigit+
    ;

DOT
   : Dot
   ;

PLUS
   : Plus
   ;

MINUS
   : Minus
   ;

STAR
    : Star
    ;

SLASH
   : Slash
   ;

PERCENT
    : Percent
    ;

AND
    : And
    ;

OR
    : Or
    ;

BOOL_LIT
    : BoolLiteral
    ;

NULL
    : Null
    ;

NUMERIC_LIT
    : DecDigit+ (Dot DecDigit+)? (ExpMark DecDigit+)?
    ;

QUOTE
    : DQuote -> pushMode(STRING_MODE)
    ;

IDENTIFIER
    : IdStart (IdContinue | '-')*
    ;

WS
    : Hws + -> channel(HIDDEN)
    ;

// HCL Specification handles NewLine different every now and then
// Sending NewLine to the hidden channel makes the parser simpler,
// though some rules cannot be enforced by it.
NL
    : Vws + -> channel(HIDDEN)
    ;

ERRCHAR
    : .
    ;

mode STRING_MODE;

STRING_ESCAPE
   : EscAny -> type (STRING_CONTENT)
   ;

INTERPOLATION_ESCAPE
   : EscInterpolation -> type(STRING_CONTENT)
   ;

INTERPOLATION_START
    : InterpolationStart -> pushMode(INTERPOLATION_MODE)
    ;

TEMPLATE_START
    : TemplateStart -> pushMode(TEMPLATE_MODE)
    ;

STRING_END
    : DQuote      -> type(QUOTE), popMode
    ;

STRING_CONTENT
    : NonVws
    ;

STRING_ERR
   : .      -> popMode
   ;

mode INTERPOLATION_MODE;

INTERPOLATION_END
    : TemplateEnd  -> popMode
    ;

INTERPOLATION_QUOTE
    : DQuote       -> type(QUOTE), pushMode(STRING_MODE)
    ;

INTERPOLATION_CONTENT
    : .
    ;

mode TEMPLATE_MODE;

TEMPLATE_END
    : TemplateEnd  -> popMode
    ;

TEMPLATE_QUOTE
    : DQuote       -> type(QUOTE), pushMode(STRING_MODE)
    ;

TEMPLATE_CONTENT
    : .
    ;

mode HEREDOC_MODE;

HEREDOC_END
    : ({heredocEndAhead(getText())}? Hws* Letter LetterDigit*) {popHereDocVar();} -> popMode
    ;

H_INTERPOLATION_ESCAPE
   : EscInterpolation -> type(HEREDOC_CONTENT)
   ;

H_INTERPOLATION_START
    : InterpolationStart -> type(INTERPOLATION_START), pushMode(INTERPOLATION_MODE)
    ;

H_TEMPLATE_START
    : TemplateStart -> type(TEMPLATE_START), pushMode(TEMPLATE_MODE)
    ;

HEREDOC_CONTENT
    : ({!heredocEndAhead(getText())}? .)
    ;

HEREDOC_ERR
    : . -> type(ERRCHAR)
    ;