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
lexer grammar BladePhpAntlrLexer;
import BladePhpCommonLexer;

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
  package org.netbeans.modules.php.blade.syntax.antlr4.php;
}

@lexer::members {
    int rparenBalance = 0;
    int sqparenBalance = 0;
    int curlyparenBalance = 0;
}

options { 
    superClass = LexerAdaptor;
    caseInsensitive = true;
}

LINE_COMMENT : LineComment->skip;

ARRAY : 'array';
AS : 'as';
ECHO : 'echo';
IF : 'if';
ELSEIF : 'elseif' | 'else if';
ELSE : 'else';
NEW : 'new';
CLASS : 'class';
FUNCTION : 'function';
LANG_CONSTRUCT : 'empty' | 'isset';
MATCH : 'match';
FOREACH : 'foreach';

COMMA : ',' ;

LPAREN : '(';
RPAREN : ')';

LSQUAREBRACKET: '[';
RSQUAREBRACKET: ']';

LCURLYBRACE: '{';
RCURLYBRACE: '}';

IDENTIFIER : Identifier;

PHP_VARIABLE : PhpVariable;

DOLLAR : '$';

NAMESPACE_SEPARATOR : '\\';
DOUBLE_COLON : '::';
ARROW : '=>';
OBJECT_OPERATOR : '->';

SEMI_COLON : ';';

COMPARISON_OPERATOR : ('==' | '!=' | '>' | '<') '='?;

LOGICAL_UNION_OPERATOR : '&&' | '||';

STRING_LITERAL : StringLiteral;


STYLE_COMMENT : '/*' .*? '*/' [\n\r]*->skip;

WS : ((' ')+ | [\r\n]+)->skip;

//testing purpose
PHP_DIRECTIVE : ('@php' | '@endphp')->skip;

OTHER : . ->skip;

