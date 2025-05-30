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
 HTML,
 CSS
}
    
fragment Identifier 
    : [a-zA-Z_\u0080-\ufffe][a-zA-Z0-9_\u0080-\ufffe]*;

fragment ArgumentExtra
    : (Identifier ('.' Identifier)*) 
      | ('[' Identifier ']');

TEMPLATE_TAG_OPEN : '<template' {this.setInsideTemplateTag(true);} ->pushMode(INSIDE_TEMPLATE);

STYLE_TAG_OPEN : '<style' (' ')* 'scoped'? '>' {this.setInsideStyleTag(true);} ->type(HTML),pushMode(INSIDE_STYLE);

OTHER : . ->type(HTML);   
    
mode INSIDE_TEMPLATE;

TEMPLATE_TAG_CLOSE : '</template>'->popMode;
VUE_DIRECTIVE_TEMPLATE : ('v-' Identifier (':' ArgumentExtra)? | '@' ArgumentExtra | ':' (Identifier | ('[' Identifier ']') )) {this._input.LA(1) == '='}? ->type(VUE_DIRECTIVE),pushMode(INSIDE_SCRIPT_ATTR);
VUE_DIRECTIVE_SIMPLE : 'v-' ( 'once' | 'else' | 'pre' | 'cloak' | 'slot:' Identifier  ) ->type(VUE_DIRECTIVE);

VAR_TAG : '{{' {this.setVarInterpolationOpened(true);} ->pushMode(INSIDE_VAR_INTERPOLATION);
TEMPLATE_OTHER : . ->type(HTML); 
EXIT_TEMPLATE_EOF : EOF->type(HTML),popMode;

mode INSIDE_SCRIPT_ATTR;

SCRIPT_ATTR_QUOTE_EXIT : {this.getAttrQuoteState() == true}? '"' {this.setAttrQuoteState(false);}->type(QUOTE_ATTR), popMode;
SCRIPT_ATTR_QUOTE : '"' {this.setAttrQuoteState(true);}->type(QUOTE_ATTR);

SCRIPT_ATTR_OTHER : . ->type(JAVASCRIPT_ATTR); 
EXIT_SCRIPT_ATTR_EOF : EOF->type(HTML),popMode;

mode INSIDE_STYLE;
    
STYLE_TAG_CLOSE : '</style>'->type(HTML),popMode;
STYLE_OTHER : . ->type(CSS); 
EXIT_STYLE_EOF : EOF->type(HTML),popMode;

mode INSIDE_VAR_INTERPOLATION;

VAR_INTERPOLATION_END : {this.isVarInterpolationOpened()}? '}}' {this.setVarInterpolationOpened(false);}->type(VAR_TAG), popMode; 
VAR_INTERPOLATION_OTHER : . ->type(JAVASCRIPT); 
EXIT_VAR_INTERPOLATION_EOF : EOF->type(HTML),popMode;

    
