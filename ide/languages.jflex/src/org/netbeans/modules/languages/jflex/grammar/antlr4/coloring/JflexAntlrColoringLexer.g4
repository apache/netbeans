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
lexer grammar JflexAntlrColoringLexer;

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
  package org.netbeans.modules.languages.jflex.grammar.antlr4.coloring;
}

options { 
    superClass = ColoringLexerAdaptor;
}

import JflexBasicLexer;

tokens { 
    WS,
    KEYWORD,
    LEXICAL_REGEX,
    QUANTIFIER,
    OPTIONS_SEPARATOR,
    OPTION,
    MACRO,
    LEXICAL_STATE,
    STRING,
    COMMENT,
    DIRECTIVE_VALUE,
    CODE,
    OPERATOR,
    PUNCTUATION,
    ERROR
}
channels { OFF_CHANNEL , COMMENT_CHANNEL }

    
COMMENT
   : AllCommentTypes ->type(COMMENT), channel (COMMENT_CHANNEL)
   ;

WS_INIT
   : Whitespace+ -> type(CODE)
   ;

OPTIONS_SEPARATOR
    : '%%'->pushMode(OptionsAndDeclarations)
    ;

//raw java code
ANY
   : . -> type(CODE)
   ;

//Section where the options and declarations (macros) are prepared
mode OptionsAndDeclarations;

DEF_COMMENT
   : AllCommentTypes ->type(COMMENT),  channel (COMMENT_CHANNEL)
   ;

//
//directives with code injection

CLASS_CODE_START 
    : '%{' ->type(OPTION),pushMode(ClassCode)
    ; 

//constructor code
INIT_CODE_START
    : '%init{' ->type(OPTION),pushMode(InitCode)
    ; 

//end of line code
EOF_VAL_CODE_START 
    : '%eofval{' ->type(OPTION),pushMode(EofValCode)
    ;

//executed at the end of file (once)
EOF_CODE_START 
    : '%eof{' ->type(OPTION),pushMode(EofCode)
    ;

DEFINED_OPTIONS_WITH_VALUES :
    '%' (
        'classname'
        | 'class'
        | 'implements'
        | 'ctorarg'
        | 'scanerror'
        | 'buffer'
        | 'token_size_limit'
        | 'include'
        | 'function'
        | 'type'
        | 'initthrow'
        | 'yylexthrow'
        | 'eofthrow'
        | 'eofclose'
    ) {this._input.LA(1) == ' '}? ->type(OPTION),pushMode(OptionValue)
    ;

DEFINED_SIMPLE_OPTIONS :
     '%' (

        | 'public' 
        | 'final' 
        | 'abstract' 
        | 'no_supress_warnings' 
        | 'apiprivate'
        | 'unicode'
        | 'cup'
        | 'line'
        | 'column'
        | 'int'
        | 'integer'
        | 'intwrap'
        | 'eofclose'
        | 'debug'
        | 'standalone'
    ) {this._input.LA(1) == ' '}? ->type(OPTION)
    ;   

//state directive init
STATE_OPTIONS :
    '%' ('state' | 'xstate') {this._input.LA(1) == ' '}? ->type(OPTION),pushMode(StateDeclaration)
    ;

//undefined option
OPTION : '%' Identifier->type(OPTION);

MACRO_NAME : Identifier->type(MACRO);

//pipe operator for chained macro assigned values
PIPE 
    : '|' {this.setInMacroAssign(true);} ->type(OPERATOR), pushMode(DeclarationValue)
    ;

//macro value assign
EQ 
    : '=' {this.setInMacroAssign(true);} ->type(OPERATOR), pushMode(DeclarationValue)
;

//END of options and declaration
END_DEFINITION
    : '%%' {{this.setInMacroAssign(false);} } ->type(OPTIONS_SEPARATOR),pushMode(LexicalRules)
    ;

WS_DEFINITION
   : Whitespace+ ->type(WS), channel (OFF_CHANNEL)
   ;

ERRCHAR
   : . ->type(ERROR)
   ;

//declaration value rules for macros
mode DeclarationValue;

REGEX_START : '[' {this.incrementSQBracket();} ->type(LEXICAL_REGEX),pushMode(RegexSq);
REGEX_PAREN_START : '(' {this.openParenthesis();} ->type(LEXICAL_REGEX),pushMode(RegexParen);

ASCI_CHAR
    : AsciChar->type(NUMBER)
    ;

LEXICAL_REGEX 
    : '~'? RegexEsc
    | RegexOperator
    ;

QUANTIFIER : Quantifiers;

MACRO 
    : Macro
    ;

STRING_VALUE 
    : (DQuoteLiteral | SQuoteLiteral)->type(STRING)
    ;

NUMBER 
    : [0-9]
    ;

WS_DECL
   : InlineWS+ ->type(WS), channel (OFF_CHANNEL)
   ;

//new line or comment ended in new line deisgnates the end of macro assignment
DECL_LINE_COMMENT
    : LineComment {this.setInMacroAssign(false);} ->type(COMMENT), channel(COMMENT_CHANNEL),popMode
    ;

EXIT_DECL 
    : NewLineWS {this.setInMacroAssign(false);} ->type(WS),channel(OFF_CHANNEL),popMode
    ;

//missed matches
//permissive mode
DECL_VALUE_ANY
   : . ->type(LEXICAL_REGEX)
   ;

//inside regex syntax
mode RegexSq;

SQ_BRACKET_OPEN 
    : '[' {this.incrementSQBracket();}-> type(LEXICAL_REGEX)
    ;

EXIT_REGEX 
    : {this.getSQBracketBalance() <= 1}? ']' {this.setSQBracketBalance(0);} -> type(LEXICAL_REGEX), popMode
    ;

SQ_BRACKET_CLOSE 
    : ']' {this.decrementSQBracket();}-> type(LEXICAL_REGEX)
    ;

SQ_ASCI_CHARS 
    : AsciChar->type(NUMBER)
    ;

SQ_PREDEFINED_CHARS 
    : PredefinedCharacters->type(KEYWORD)
    ;

ANY_REGEX 
    : . -> type(LEXICAL_REGEX)
    ;

//regex pattern written betwen parenthesis '(' ')'
mode RegexParen;

PAREN_OPEN 
    : '(' {this.incrementParenthesis();}-> type(LEXICAL_REGEX)
    ;

EXIT_PAREN_REGEX 
    : {this.getParenthesisBalance() <= 1}? ')' {this.setParenthesisBalance(0);} -> type(LEXICAL_REGEX), popMode
    ;

PAREN_CLOSE : ')' {this.decrementParenthesis();}-> type(LEXICAL_REGEX);

//enter in squar bracket regex syntax
RGXPAREN_SQ_REGEX_START 
    : '[' {
        this.incrementSQBracket();
        if (!this.isInMacroAssign()) {
            this.setRuleDefined(true);
        }
    } ->type(LEXICAL_REGEX),pushMode(RegexSq)
    ;

RGXPAREN_MACRO 
    : Macro->type(MACRO);

MISC_REGEX_CHARS
    : MiscRegexChars ->type(LEXICAL_REGEX)
    ;

ESCAPED_QUOTE
    : EscSeq ->type(LEXICAL_REGEX)
    ;

RGXPAREN_STRING 
    :(DQuoteLiteral | SQuoteLiteral)
    ->type(STRING)
    ;

RGXPAREN_QUANTIFIER 
    : Quantifiers ->type(QUANTIFIER)
    ;

ANY_PAREN_REGEX 
    : . -> type(LEXICAL_REGEX)
    ;

mode OptionValue;

WS_OPTION_VALUE
   : InlineWS+ ->type(WS),channel (OFF_CHANNEL)
   ;

OPTION_VALUE 
    : Identifier->type(DIRECTIVE_VALUE),popMode
    ;

OPTION_STRING_VALUE
    :(DQuoteLiteral | SQuoteLiteral)
    ->type(STRING)
    ;

EXIT_OPTION_VALUE
    : NewLineWS->type(WS),channel(OFF_CHANNEL),popMode
    ;

ANY_OPTION_VALUE 
    : . -> type(ERROR),popMode
    ;

//%state NAME
mode StateDeclaration;

STATE_WS_DECLARATION
    : InlineWS+ ->type(WS),channel (OFF_CHANNEL)
    ;

STATE_SEPARATOR 
    : ',' ->type(PUNCTUATION)
    ;

STATE_DECLARATION_LIST 
    : Identifier {
        this._input.LA(1) == ',' 
        || (this._input.LA(1) == ' ' && this._input.LA(2) == ',')
    }? ->type(LEXICAL_STATE)
    ;

STATE_DECLARATION 
    : Identifier->type(LEXICAL_STATE),popMode;

STATE_EXIT_OPTION_DECLARATION : NewLineWS->type(WS),channel (OFF_CHANNEL),popMode;

ANY_STATE_TOKEN 
    : . -> type(ERROR),popMode
    ;

//section of lexical rules list
mode LexicalRules;

RULE_COMMENTS
   : AllCommentTypes ->type(COMMENT),channel (COMMENT_CHANNEL)
   ;

RULES_WS
   : Whitespace+ ->type(WS), channel (OFF_CHANNEL)
   ;

RULE_IDENTIFIER 
    : '<' Identifier (',' [ ]* Identifier)* '>' ->type(LEXICAL_STATE),pushMode(RuleExpression)
    ;

MACRO_RULE 
    : Macro {this.setRuleDefined(true);}->type(MACRO), pushMode(RuleExpression)
    ;

ANY_REGEX_RULE
    : '[^]' {this.setRuleDefined(true);}->type(LEXICAL_REGEX), pushMode(RuleExpression)
    ;

RULE_REGEX_CONCAT
    : MiscRegexChars ->type(LEXICAL_REGEX)
    ;

RULE_LIST_EXPR_STRING : (DQuoteLiteral | SQuoteLiteral) ->type(STRING);

RULE_LIST_REGEX_START : '[' {this.incrementSQBracket();} ->type(LEXICAL_REGEX),pushMode(RegexSq);

RULE_LIST_REGEX_PAREN_START : '(' {this.openParenthesis();} ->type(LEXICAL_REGEX),pushMode(RegexParen);

RULE_LIST_ANY_CODE
    : . ->type(CODE)
    ;

mode ClassCode;

CLASS_CODE_END : '%}' ->type(OPTION),popMode; 

CLASS_CODE: . ->type(CODE);

mode InitCode;

INIT_CODE_END : '%init}' ->type(OPTION),popMode; 

INIT_ANY_CODE: . ->type(CODE);

mode EofValCode;

EOF_VAL_CODE_END : '%eofval}' ->type(OPTION),popMode; 

EOF_VAL_CODE: . ->type(CODE);

mode EofCode;

EOF_CODE_END : '%eof}' ->type(OPTION),popMode; 

EOF_ANY_CODE: . ->type(CODE);

mode RuleExpression;

RULE_EXPR_COMMENTS
   : AllCommentTypes ->type(COMMENT),channel (COMMENT_CHANNEL)
   ;

RULE_EXPR_WS
   : Whitespace+ ->type(WS), channel (OFF_CHANNEL)
   ;

RULE_EOF_STATE 
    : '<<EOF>>' {this.setRuleDefined(true);}->type(LEXICAL_STATE)
    ;

RULE_EXPR_ID : Macro {this.setRuleDefined(true);} ->type(MACRO);

RULE_EXPR_REGEX_CONCAT
    : MiscRegexChars {this.setRuleDefined(true);} ->type(LEXICAL_REGEX)
    ;

RULE_REGEX_START : '[' {this.incrementSQBracket();this.setRuleDefined(true);} ->type(LEXICAL_REGEX),pushMode(RegexSq);
RULE_REGEX_PAREN_START : '(' {this.openParenthesis();this.setRuleDefined(true);} ->type(LEXICAL_REGEX),pushMode(RegexParen);

RULE_EXPR_STRING : (DQuoteLiteral | SQuoteLiteral) {this.setRuleDefined(true);}->type(STRING);

CODE_BLOCK_START : {this.isRuleDefined()}? '{' {this.incrementCurlyBracket();this.setRuleDefined(false);} ->type(CODE),pushMode(CodeBlock);

RULE_QUANTIFIER : Quantifiers->type(QUANTIFIER);

RULE_ANY_MATCH : MiscRegexChars {this.setRuleDefined(true);}->type(LEXICAL_REGEX);

RULE_LIST_START : '{' {this.setInRuleList(true);} -> type(CODE);
RULE_LIST_END : '}' {this.setInRuleList(false);this.mode(LexicalRules);} -> type(CODE);

RULES_ANY_CODE: . ->type(CODE);

//Contains code token collection which is used for embeddeding java coloring
mode CodeBlock;

CODE_COMMENTS
   : AllCommentTypes ->type(CODE)
   ;

CODE_STRING : (DQuoteLiteral | SQuoteLiteral)->type(CODE);

CODE_CURLY_OPEN : '{' {this.incrementCurlyBracket();}-> type(CODE);

CODE_CURL_EXIT : {this.getCurlyBracketBalance() == 1}? '}' 
     {
         this.decrementCurlyBracket();
         
         if (this.isInRuleList()) {
            this.mode(RuleExpression);
         } else {
            this.mode(LexicalRules);
         }
    } -> type(CODE);

CODE_CURLY_CLOSE : '}' {this.decrementCurlyBracket();}-> type(CODE);

ANY_CODE : . -> type(CODE)
    ;