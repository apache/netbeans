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
package org.netbeans.modules.javascript2.json.parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.netbeans.api.annotations.common.NonNull;

/**
 *
 * @author Tomas Zezula
 */
interface Recovery {
    boolean canRecover(@NonNull CharStream in);
    void recover(
            @NonNull CharStream in,
            @NonNull LexerATNSimulator interpreter);

    static boolean isEOF(int input) {
        return  input == Recognizer.EOF;
    }

    @NonNull
    static Recovery createLineCommentRecovery() {
        return new Recovery () {
            @Override
            public boolean canRecover(CharStream in) {
                return in.LA(1) == '/' && in.LA(2) == '/';  //NOI18N
            }

            @Override
            public void recover(
                    CharStream in,
                    LexerATNSimulator interpreter) {
                int input;
                do {
                    input = in.LA(1);
                    if (!isEOF(input)) {
                        interpreter.consume(in);
                    }
                } while (input != '\n' && !isEOF(input));                //NOI18N
            }
        };
    }


    @NonNull
    static Recovery createCommentRecovery() {
        return new Recovery () {

            @Override
            public boolean canRecover(CharStream in) {
                return in.LA(1) == '/' && in.LA(2) == '*';  //NOI18N
            }

            @Override
            public void recover(CharStream in, LexerATNSimulator interpreter) {
                interpreter.consume(in);
                interpreter.consume(in);
                int input, state = 0;
                do {
                    input = in.LA(1);
                    switch (state) {
                        case 0:
                            switch (input) {
                                case '*':   //NOI18N
                                    state = 1;
                                    break;
                            }
                            break;
                        case 1:
                            switch (input) {
                                case '/':   //NOI18N
                                    state = 2;
                                    break;
                                case '*':   //NOI18N
                                    state = 1;
                                    break;
                                default:
                                    state = 0;
                            }
                    }
                    if (!Recovery.isEOF(input)) {
                        interpreter.consume(in);
                    }
                } while (state != 2 && !Recovery.isEOF(input));
            }
        };
    }

    @NonNull
    public static Recovery createStringRecovery() {
        return new Recovery() {
            @Override
            public boolean canRecover(CharStream in) {
                return (in.LA(1) == '"');
            }

            @Override
            public void recover(CharStream in, LexerATNSimulator interpreter) {
                interpreter.consume(in);
                int input;
                do {
                    input = in.LA(1);
                    if (!isEOF(input)) {
                        interpreter.consume(in);
                    }
                } while (input != '"' && input != '\n' && !isEOF(input));                //NOI18N
            }
        };
    }
}
