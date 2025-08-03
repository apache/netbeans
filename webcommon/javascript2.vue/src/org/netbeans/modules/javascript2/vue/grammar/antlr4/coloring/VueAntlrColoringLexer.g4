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
lexer grammar VueAntlrColoringLexer;

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
  package org.netbeans.modules.javascript2.vue.grammar.antlr4.coloring;
}

options { 
    superClass = ColoringLexerAdaptor;
 }

tokens {
 TEMPLATE_TAG_OPEN,
 VUE_DIRECTIVE,
 QUOTE_ATTR,
 JAVASCRIPT,
 JAVASCRIPT_ATTR,
 JAVASCRIPT_INTERP,
 HTML,
 CSS
}

//fragments

fragment Identifier 
    : [a-zA-Z_\u0080-\ufffe][a-zA-Z0-9_\u0080-\ufffe]*;

fragment PropertyChain
    : (Identifier ('.' Identifier)*) 
      | ('[' Identifier ']');

fragment DoubleQuoteStringFragment 
    : '"' ([\\"] | . )*? '"';

fragment SingleQuoteStringFragment 
    : '\'' (~('\'' | '\\') | '\\' . )* '\'';

fragment StringLiteral : DoubleQuoteStringFragment | SingleQuoteStringFragment;

//TOKENS

SCRIPT_TAG_START : '<script' (' ')* ->type(HTML),pushMode(INSIDE_SCRIPT_TAG_START);

TEMPLATE_TAG_OPEN : '<template' ->pushMode(INSIDE_TEMPLATE);

STYLE_TAG_OPEN : '<style' (' ')* ->type(HTML),pushMode(INSIDE_STYLE_TAG_START);

OTHER : . ->type(HTML);   
    
mode INSIDE_TEMPLATE;

TEMPLATE_TAG_CLOSE : '</template>'->popMode;
VUE_DIRECTIVE_WITH_VALUE :
    (
       'v-' Identifier ('-' Identifier)* (':' PropertyChain)? //attribute bind
     | '@' PropertyChain //on event listener
     | ':' (Identifier | ('[' Identifier ']') ) //short attribute bind
     ) '=' ->type(VUE_DIRECTIVE),pushMode(INSIDE_SCRIPT_ATTR);

//directives which don't expect value assignment
VUE_DIRECTIVE_SIMPLE : 
    ('v-' (
        'once' //Render the element and component once only, and skip future updates
       | 'else' //if block
       | 'pre' //Skip compilation for this element and all its children
       | 'cloak' //hide un-compiled template
       | 'slot:' Identifier  //slot reference
    )
    | '@' PropertyChain )->type(VUE_DIRECTIVE);

VAR_TAG : '{{' {this.setVarInterpolationOpened(true);} ->pushMode(INSIDE_VAR_INTERPOLATION);
TEMPLATE_OTHER : . ->type(HTML); 
EXIT_TEMPLATE_EOF : EOF->type(HTML),popMode;

mode INSIDE_SCRIPT_ATTR;

SCRIPT_ATTR_QUOTE_EXIT : {this.getAttrQuoteState() == true}? '"' {this.setAttrQuoteState(false);}->type(QUOTE_ATTR), popMode;
SCRIPT_ATTR_QUOTE : '"' {this.setAttrQuoteState(true);}->type(QUOTE_ATTR);

SCRIPT_ATTR_OTHER : . ->type(JAVASCRIPT_ATTR); 
EXIT_SCRIPT_ATTR_EOF : EOF->type(HTML),popMode;

mode INSIDE_STYLE_TAG_START;

STYLE_LANG_ATTR : 'lang=' StringLiteral {this.setStyleLanguage();}->type(HTML);
STYLE_TAG_START_END : '>' ->type(HTML),pushMode(INSIDE_STYLE);
STYLE_TAG_START_OTHER : . ->type(HTML); 
EXIT_STYLE_TAG_START_EOF : EOF->type(HTML),popMode;

mode INSIDE_STYLE;
    
STYLE_TAG_CLOSE : '</style>'->type(HTML),mode(DEFAULT_MODE);
STYLE_OTHER : . ->type(CSS); 
EXIT_STYLE_EOF : EOF->type(HTML),popMode;

mode INSIDE_VAR_INTERPOLATION;

VAR_INTERPOLATION_END : {this.isVarInterpolationOpened()}? '}}' {this.setVarInterpolationOpened(false);}->type(VAR_TAG), popMode; 
VAR_INTERPOLATION_OTHER : . ->type(JAVASCRIPT_INTERP); 
EXIT_VAR_INTERPOLATION_EOF : EOF->type(HTML),popMode;

mode INSIDE_SCRIPT_TAG_START;

SCRIPT_LANG_ATTR : 'lang=' StringLiteral {this.setScriptLanguage();}->type(HTML);
SCRIPT_TAG_START_END : '>' ->type(HTML),pushMode(INSIDE_SCRIPT);
SCRIPT_TAG_START_OTHER : . ->type(HTML); 
EXIT_SCRIPT_TAG_START_EOF : EOF->type(HTML),popMode;

mode INSIDE_SCRIPT;

SCRIPT_TAG_CLOSE : '</script>'->type(HTML),mode(DEFAULT_MODE);
SCRIPT_TAG_OTHER : . ->type(JAVASCRIPT); 
EXIT_SCRIPT_TAG_EOF : EOF->type(HTML),popMode;    
