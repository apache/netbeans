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
lexer grammar BladeHtmlAntlrLexer;

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
  package org.netbeans.modules.php.blade.syntax.antlr4.html_components;
}

@lexer::members {
    boolean tagOpened = false;
    boolean insideTag = false;
    int contentTagBalance = 0;
    int rawTagBalance = 0;
}

options { 
    caseInsensitive = true;
}

fragment Identifier
    : [a-z\u0080-\ufffe][a-z0-9-_\u0080-\ufffe]*;


HTML_COMPONENT_OPEN_TAG : '<x-' (Identifier (('::' | '.') Identifier)?)? {tagOpened = true;insideTag = true;};

COMPONENT_ATTRIBUTE : {insideTag == true && contentTagBalance == 0 && rawTagBalance == 0}? ':' Identifier;

GT : '>' {insideTag = false;};

BLADE_COMMENT_START : '{{--' ->pushMode(INSIDE_BLADE_COMMENT), skip;

BLADE_TAG_ESCAPE : '@' ('{')+->skip;
CONTENT_TAG_OPEN : '{{' {contentTagBalance++;}->skip;
CONTENT_TAG_CLOSE : '}}' {contentTagBalance--;}->skip;

RAW_TAG_OPEN : '{!!' {rawTagBalance++;};
RAW_TAG_CLOSE : '!!}' {rawTagBalance--;};

WS : ((' ')+ | [\r\n]+)->skip;

TAG_PART : '!' | '!!';

OTHER : . ->skip;

//==============================================

mode INSIDE_BLADE_COMMENT;

BLADE_COMMENT_END : '--}}'->popMode, skip;

BLADE_COMMENT_MORE : . ->skip;

BLADE_COMMENT_EOF : EOF->popMode, skip;