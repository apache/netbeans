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
/**
 *
 * @author Tomas Zezula
 */
lexer grammar JsonLexer;

@lexer::header {
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
package org.netbeans.modules.javascript2.json.parser;
}

@lexer::members {
private static final Recovery[] RECOVERIES = {
    Recovery.createLineCommentRecovery(),
    Recovery.createCommentRecovery(),
    Recovery.createStringRecovery()
};

private boolean isCommentSupported;
private boolean hasErrorToken;

public LexerState getLexerState() {
    return new LexerState(getState());
}

public void setLexerState(LexerState state) {
    this.setState(state.atnState);
}

public static final class LexerState {

    final int atnState;

    public LexerState(int atnState) {
        this.atnState = atnState;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LexerState other = (LexerState) obj;
        if (this.atnState != other.atnState) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + this.atnState;
        return hash;
    }
}

public JsonLexer(
        final CharStream input,
        final boolean isCommentSupported) {
    this(input, isCommentSupported, false);
}

public  JsonLexer(
        final CharStream input,
        final boolean isCommentSupported,
        final boolean hasErrorToken) {
    this(input);
    this.isCommentSupported = isCommentSupported;
    this.hasErrorToken = hasErrorToken;
}

@Override
public void recover(LexerNoViableAltException e) {
    final CharStream in = e.getInputStream();
    final int current = in.index();
    final int index = e.getStartIndex();
    boolean resolved = false;
    in.seek(index);
    for (Recovery r : RECOVERIES) {
        if (r.canRecover(in)) {
            getInterpreter().setCharPositionInLine(_tokenStartCharPositionInLine);
            getInterpreter().setLine(_tokenStartLine);
            r.recover(in, getInterpreter());
            resolved = true;
            break;
        }
    }
    if (!resolved) {
        in.seek(current);
        super.recover(e);
    }
}
}

channels { WHITESPACES, COMMENTS, ERRORS }

COLON               : ':';
COMMA               : ',';
DOT                 : '.';
PLUS                : '+';
MINUS               : '-';
LBRACE              : '{';
RBRACE              : '}';
LBRACKET            : '[';
RBRACKET            : ']';
TRUE                : 'true';
FALSE               : 'false';
NULL                : 'null';
NUMBER              : INTEGER FRACTION? EXPONENT?;
fragment INTEGER    : (MINUS)? (DIGIT_0 | DIGIT_19 DIGIT*);
fragment DIGIT_0    : '0';
fragment DIGIT_19   : [1-9];
fragment DIGIT      : DIGIT_0 | DIGIT_19;
fragment FRACTION   : DOT DIGIT+;
fragment EXPONENT   : ('e'|'E')(PLUS | MINUS)? DIGIT+;

STRING              : QUOTE (CHAR)* QUOTE;
fragment QUOTE      : '"';
fragment CHAR       : ~[\u0000-\u001F"\\] | CONTROL;
fragment CONTROL    : '\\' (["\\/bfnrt] | UNICODE);
fragment UNICODE    : 'u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT;
fragment HEXDIGIT   : [0-9a-fA-F];

LINE_COMMENT        : '//' .*? '\r'? '\n' {isCommentSupported}? -> channel(COMMENTS);
COMMENT             : '/*' .*? '*/' {isCommentSupported}? -> channel(COMMENTS);
WS                  : [ \t\r\n]+ -> channel(WHITESPACES);
ERROR_COMMENT       : '/*' (~'*' | ('*'+ ~'/'))* {hasErrorToken && isCommentSupported}? -> channel(ERRORS);
ERROR               : . {hasErrorToken}? -> channel(ERRORS);

