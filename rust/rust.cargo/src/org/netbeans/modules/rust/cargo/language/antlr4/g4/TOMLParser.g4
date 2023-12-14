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

// From https://github.com/antlr/grammars-v4/blob/master/toml/TomlParser.g4 (APLv2)

parser grammar TOMLParser;
options { tokenVocab = TOMLLexer; }

document : expression (NL expression)* EOF ;

expression : key_value comment | table comment | comment ;

comment: COMMENT? ;

key_value : key EQUALS value ;

key : simple_key | dotted_key ;

simple_key : quoted_key | unquoted_key ;

unquoted_key : UNQUOTED_KEY ;

quoted_key :  BASIC_STRING | LITERAL_STRING ;

dotted_key : simple_key (DOT simple_key)+ ;

value : string | integer | floating_point | bool_ | date_time | array_ | inline_table ;

string : BASIC_STRING | ML_BASIC_STRING | LITERAL_STRING | ML_LITERAL_STRING ;

integer : DEC_INT | HEX_INT | OCT_INT | BIN_INT ;

floating_point : FLOAT | INF | NAN ;

bool_ : BOOLEAN ;

date_time : OFFSET_DATE_TIME | LOCAL_DATE_TIME | LOCAL_DATE | LOCAL_TIME ;

inline_table : 
    L_BRACE key EQUALS inline_value (COMMA key EQUALS inline_value)*? R_BRACE 
    | L_BRACE R_BRACE;

inner_array: L_BRACKET inline_value? (COMMA inline_value)*? COMMA*? R_BRACKET;

inline_value: string | integer | floating_point | bool_ | date_time | inner_array | inline_table;

array_ : L_BRACKET array_values? comment_or_nl R_BRACKET ;

array_values : (comment_or_nl value comment_or_nl COMMA comment_or_nl array_values comment_or_nl) | comment_or_nl value COMMA? ;

comment_or_nl : (COMMENT? NL)* ;

table : standard_table | array_table ;

standard_table : L_BRACKET key R_BRACKET ;

array_table : DOUBLE_L_BRACKET key DOUBLE_R_BRACKET ;


