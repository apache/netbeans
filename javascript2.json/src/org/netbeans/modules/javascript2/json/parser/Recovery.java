/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
