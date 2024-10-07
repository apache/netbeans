parser grammar BladeAntlrFormatterParser;

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

options { tokenVocab = BladeAntlrFormatterLexer; }

file : statement* EOF;

statement:
    html_indent
    | block_start
    | block_end
    | html_tag
    | self_closed_tag
    | section_block
    | inline_identable_element
    | block_aligned_directive
    | static_element
    | nl_with_space_after
    | blade_echo
    | block_end
    | html_close_tag
    | (SG_QUOTE | DB_QUOTE)
    | INLINE_GT_SYMBOL
    | GT_SYMBOL
    | (NL | WS)
    ;

inline_tag_statement : 
    IDENTIFIER EQ IDENTIFIER
    | block_start
    | block_end
    | blade_echo
    | D_INLINE_DIRECTIVE
    | NON_PARAM_DIRECTIVE
    | D_BLOCK_ALIGNED_DIRECTIVE
    | IDENTIFIER EQ STRING
    | IDENTIFIER
    | STRING
    | EQ
    | WS
    | NL
    ;

html_close_tag : HTML_CLOSE_TAG;

html_indent : (HTML_START_BLOCK_TAG | COMPONENT_TAG) inline_tag_statement* GT_SYMBOL NL WS*;
html_tag : (HTML_START_BLOCK_TAG | COMPONENT_TAG) inline_tag_statement* GT_SYMBOL;
self_closed_tag : HTML_SELF_CLOSE_TAG | ((HTML_START_BLOCK_TAG | COMPONENT_TAG) inline_tag_statement* INLINE_GT_SYMBOL);
//block_start : ws_before=nl_with_space_before? block_directive_name  D_ARG_LPAREN D_ARG_RPAREN  ;
block_start :  D_BLOCK_DIRECTIVE_START D_ARG_LPAREN D_ARG_RPAREN ws_after=nl_with_space_after*;
block_end : D_BLOCK_DIRECTIVE_END;
block_aligned_directive : D_BLOCK_ALIGNED_DIRECTIVE;
inline_identable_element : D_INLINE_DIRECTIVE | NON_PARAM_DIRECTIVE | section_inline
    | blade_echo |  D_PHP PHP_CODE+ D_ENDPHP
    ;

section_inline : D_SECTION D_ARG_LPAREN PARAM_COMMA  D_ARG_RPAREN;
section_block : D_SECTION D_ARG_LPAREN D_ARG_RPAREN ws_after=nl_with_space_after* (statement)+ D_ENDSECTION;
nl_with_space_after : NL+ WS*;
nl_with_space : NL WS*;

static_element :  HTML_COMMENT
    | HTML
    | OTHER;

blade_echo : (CONTENT_TAG_OPEN CONTENT_TAG_CLOSE)
    | (RAW_TAG_OPEN RAW_TAG_CLOSE);