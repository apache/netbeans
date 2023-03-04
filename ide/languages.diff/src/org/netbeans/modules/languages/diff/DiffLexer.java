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

package org.netbeans.modules.languages.diff;

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;


/**
 *
 * @author Jan Jancura
 */
class DiffLexer implements Lexer<DiffTokenId> {

    private LexerRestartInfo<DiffTokenId> info;

    DiffLexer (LexerRestartInfo<DiffTokenId> info) {
        this.info = info;
    }

    public Token<DiffTokenId> nextToken () {
        LexerInput input = info.input ();
        int i = input.read ();
        switch (i) {
            case LexerInput.EOF:
                return null;
            case 'I':
                if (read ("ndex:", input)) 
                    return info.tokenFactory ().createToken (DiffTokenId.INDEX);
                return info.tokenFactory ().createToken (DiffTokenId.PLAIN);
            case '=':
                if (read ("==", input))
                    return info.tokenFactory ().createToken (DiffTokenId.HEADER);
                return info.tokenFactory ().createToken (DiffTokenId.PLAIN);
            case '-':
                if (read ("--", input))
                    return info.tokenFactory ().createToken (DiffTokenId.HEADER);
                return info.tokenFactory ().createToken (DiffTokenId.REMOVED);
            case '+':
                if (read ("++", input))
                    return info.tokenFactory ().createToken (DiffTokenId.HEADER);
                return info.tokenFactory ().createToken (DiffTokenId.ADDED);
            case 'd':
                if (read ("iff", input))
                    return info.tokenFactory ().createToken (DiffTokenId.HEADER);
                return info.tokenFactory ().createToken (DiffTokenId.PLAIN);
            case 'r':
                if (read ("etrieving", input))
                    return info.tokenFactory ().createToken (DiffTokenId.HEADER);
                return info.tokenFactory ().createToken (DiffTokenId.PLAIN);
            case '@':
                if (read ("@", input))
                    return info.tokenFactory ().createToken (DiffTokenId.HEADER);
                return info.tokenFactory ().createToken (DiffTokenId.PLAIN);
            case 'R':
                if (read ("CS file:", input))
                    return info.tokenFactory ().createToken (DiffTokenId.HEADER);
                return info.tokenFactory ().createToken (DiffTokenId.PLAIN);
            case '>':
                read ("", input);
                return info.tokenFactory ().createToken (DiffTokenId.ADDED);
            case '<':
                read ("", input);
                return info.tokenFactory ().createToken (DiffTokenId.REMOVED);
            default:
                read ("", input);
                return info.tokenFactory ().createToken (DiffTokenId.PLAIN);
        }
    }

    private static boolean read (String text, LexerInput input) {
        boolean result = true;
        for (int i = 0; i < text.length (); i++) {
            int c;
            if (text.charAt (i) != (c = input.read ())) {
                result = false;
                if (c == '\n' || c == '\r') {
                    input.backup(1);
                }
                break;
            }
        }
        int i = input.read ();
        while (
            i != '\n' &&
            i != '\r' &&
            i != LexerInput.EOF
        ) {
            i = input.read ();
        }
        while (
            i != LexerInput.EOF &&
            (i == '\n' ||
             i == '\r')
        ) {
            i = input.read ();
        }
        if (i != LexerInput.EOF)
            input.backup (1);
        return result;
    }

    public Object state () {
        return null;
    }

    public void release () {
    }
}


