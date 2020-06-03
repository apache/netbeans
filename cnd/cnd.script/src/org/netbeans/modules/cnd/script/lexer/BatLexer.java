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

package org.netbeans.modules.cnd.script.lexer;

import org.netbeans.modules.cnd.api.script.BatTokenId;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;


/**
 *
 */
class BatLexer implements Lexer<BatTokenId> {

    private static Set<String> keywords = new HashSet<String> ();
    private static Set<String> commands = new HashSet<String> ();

    static {
        keywords.add ("aux"); // NOI18N
        keywords.add ("call"); // NOI18N
        keywords.add ("choice"); // NOI18N
        keywords.add ("defined"); // NOI18N
        keywords.add ("do"); // NOI18N
        keywords.add ("else"); // NOI18N
        keywords.add ("errorlevel"); // NOI18N
        keywords.add ("exist"); // NOI18N
        keywords.add ("endlocal"); // NOI18N
        keywords.add ("for"); // NOI18N
        keywords.add ("goto"); // NOI18N
        keywords.add ("if"); // NOI18N
        keywords.add ("in"); // NOI18N
        keywords.add ("not"); // NOI18N
        keywords.add ("nul"); // NOI18N
        keywords.add ("set"); // NOI18N
        keywords.add ("setlocal"); // NOI18N
        keywords.add ("shift"); // NOI18N
        keywords.add ("prn"); // NOI18N
        commands.add ("assign"); // NOI18N
        commands.add ("attrib"); // NOI18N
        commands.add ("cd"); // NOI18N
        commands.add ("chdir"); // NOI18N
        commands.add ("chkdsk"); // NOI18N
        commands.add ("cls"); // NOI18N
        commands.add ("comp"); // NOI18N
        commands.add ("copy"); // NOI18N
        commands.add ("date"); // NOI18N
        commands.add ("defrag"); // NOI18N
        commands.add ("del"); // NOI18N
        commands.add ("deltree"); // NOI18N
        commands.add ("dir"); // NOI18N
        commands.add ("echo"); // NOI18N
        commands.add ("echo."); // NOI18N
        commands.add ("erase"); // NOI18N
        commands.add ("exit"); // NOI18N
        commands.add ("fc"); // NOI18N
        commands.add ("fdisk"); // NOI18N
        commands.add ("find"); // NOI18N
        commands.add ("format"); // NOI18N
        commands.add ("help"); // NOI18N
        commands.add ("label"); // NOI18N
        commands.add ("md"); // NOI18N
        commands.add ("mem"); // NOI18N
        commands.add ("memmaker"); // NOI18N
        commands.add ("mkdir"); // NOI18N
        commands.add ("more"); // NOI18N
        commands.add ("move"); // NOI18N
        commands.add ("path"); // NOI18N
        commands.add ("pause"); // NOI18N
        commands.add ("ren"); // NOI18N
        commands.add ("rename"); // NOI18N
        commands.add ("rd"); // NOI18N
        commands.add ("rmdir"); // NOI18N
        commands.add ("sort"); // NOI18N
        commands.add ("time"); // NOI18N
        commands.add ("tree"); // NOI18N
        commands.add ("type"); // NOI18N
        commands.add ("undelete"); // NOI18N
        commands.add ("ver"); // NOI18N
        commands.add ("xcopy"); // NOI18N
    }


    private LexerRestartInfo<BatTokenId> info;

    BatLexer (LexerRestartInfo<BatTokenId> info) {
        this.info = info;
    }

    public Token<BatTokenId> nextToken () {
        LexerInput input = info.input ();
        int i = input.read ();
        switch (i) {
            case LexerInput.EOF:
                return null;
            case '+':
            case '|':
            case '&':
            case '<':
            case '>':
            case '!':
            case ':':
            case '@':
            case '=':
            case '/':
            case '\\':
            case '(':
            case ')':
            case ',':
            case '%':
            case '^':
            case '#':
            case '{':
            case '}':
            case '?':
            case '.':
            case '$':
            case '*':
            case '_':
            case '-':
            case '`':
            case ';':
                return info.tokenFactory ().createToken (BatTokenId.OPERATOR);
            case ' ':
            case '\n':
            case '\r':
            case '\t':
                do {
                    i = input.read ();
                } while (
                    i == ' ' ||
                    i == '\n' ||
                    i == '\r' ||
                    i == '\t'
                );
                if (i != LexerInput.EOF)
                    input.backup (1);
                return info.tokenFactory ().createToken (BatTokenId.WHITESPACE);
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                do {
                    i = input.read ();
                } while (
                    i >= '0' &&
                    i <= '9'
                );
                if (i == '.') {
                    do {
                        i = input.read ();
                    } while (
                        i >= '0' &&
                        i <= '9'
                    );
                }
                input.backup (1);
                return info.tokenFactory ().createToken (BatTokenId.NUMBER);
            case '"':
                do {
                    i = input.read ();
                    if (i == '^') {
                        i = input.read ();
                        i = input.read ();
                    }
                    if (i == '"') {
                        i = input.read ();
                        if (i == '"') {
                            i = input.read ();
                        } else {
                            input.backup (1);
                            break;
                        }
                    }
                } while (
                    i != '"' &&
                    i != '\n' &&
                    i != '\r' &&
                    i != LexerInput.EOF
                );
                return info.tokenFactory ().createToken (BatTokenId.STRING);
            case '\'':
                do {
                    i = input.read ();
                    if (i == '\\') {
                        i = input.read ();
                        i = input.read ();
                    }
                } while (
                    i != '\'' &&
                    i != '\n' &&
                    i != '\r' &&
                    i != LexerInput.EOF
                );
                return info.tokenFactory ().createToken (BatTokenId.STRING);
            default:
                if (
                    (i >= 'a' && i <= 'z') ||
                    (i >= 'A' && i <= 'Z')
                ) {
                    do {
                        i = input.read ();
                    } while (
                        (i >= 'a' && i <= 'z') ||
                        (i >= 'A' && i <= 'Z') ||
                        (i >= '0' && i <= '9') ||
                        i == '_' ||
                        i == '-' ||
                        i == '~'
                    );
                    input.backup (1);
                    String id = input.readText ().toString ();
                    String lcid = id.toLowerCase ();
                    if (keywords.contains (lcid))
                        return info.tokenFactory ().createToken (BatTokenId.KEYWORD);
                    if (commands.contains (lcid))
                        return info.tokenFactory ().createToken (BatTokenId.COMMAND);
                    if ("rem".equals (lcid)) { // NOI18N
                        do {
                            i = input.read ();
                        } while (
                            i != '\n' &&
                            i != '\r' &&
                            i != LexerInput.EOF
                        );
                        return info.tokenFactory ().createToken (BatTokenId.COMMENT);
                    }
                    return info.tokenFactory ().createToken (BatTokenId.IDENTIFIER);
                }
                return info.tokenFactory ().createToken (BatTokenId.ERROR);
        }
    }

    public Object state () {
        return null;
    }

    public void release () {
    }
}


