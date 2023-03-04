/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/* Antlr grammar file for the Calc example. */

header {
package org.netbeans.modules.lexer.demo.antlr;
}

class CalcScanner extends Lexer;
options {
    k = 3;
    charVocabulary = '\0'..'\ufffe';
}

{

    /**
     * State variable used to hold current lexer state.
     * In this case it's used for incomplete tokens only.
     */
    private int state;

    int getState() {
        return state;
    }

    void resetState() {
        state = 0;
    }

}

WHITESPACE  : (' '
            | '\t'
            | '\n'
            | '\r')+
            ;

PLUS        : '+'
            ;

MINUS       : '-'
            ;

MUL         : '*'
            ;

DIV         : '/'
            ;

LPAREN      : '('
            ;

RPAREN      : ')'
            ;

ABC         : "abc"
            ;

CONSTANT    : FLOAT (('e' | 'E') ('+' | '-')? INTEGER )?
            ;

ML_COMMENT  : INCOMPLETE_ML_COMMENT { state = CalcScannerTokenTypes.INCOMPLETE_ML_COMMENT; }
            (  { LA(2) != '/' }? '*'
               | ~('*')
            )*
            "*/" { state = 0; }
            ;

/* Protected tokens are used internally by the scanner only */
protected
FLOAT       : (INTEGER ('.' INTEGER)?
            | '.' INTEGER)
            ;

protected
INTEGER     : (DIGIT)+
            ;

protected
DIGIT       : '0'..'9'
            ;

protected
INCOMPLETE_ML_COMMENT   : "/*"
                        ;
