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
package org.netbeans.modules.cnd.meson.lexer;

import java.util.HashSet;
import java.util.Set;

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 */
public class MesonBuildLexer implements Lexer<MesonBuildTokenId> {
    private static final Set<String> functions = new HashSet<>();
    private static final Set<String> objects = new HashSet<>();
    private static final Set<String> keywords = new HashSet<>();
    private static final Set<String> literals = new HashSet<>();

    static {
        functions.add("add_global_arguments"); // NOI18N
        functions.add("add_global_link_arguments"); // NOI18N
        functions.add("add_languages"); // NOI18N
        functions.add("add_project_arguments"); // NOI18N
        functions.add("add_project_dependencies"); // NOI18N
        functions.add("add_project_link_arguments"); // NOI18N
        functions.add("add_test_setup"); // NOI18N
        functions.add("alias_target"); // NOI18N
        functions.add("assert"); // NOI18N
        functions.add("benchmark"); // NOI18N
        functions.add("both_libraries"); // NOI18N
        functions.add("build_target"); // NOI18N
        functions.add("configuration_data"); // NOI18N
        functions.add("configure_file"); // NOI18N
        functions.add("custom_target"); // NOI18N
        functions.add("debug"); // NOI18N
        functions.add("declare_dependency"); // NOI18N
        functions.add("dependency"); // NOI18N
        functions.add("disabler"); // NOI18N
        functions.add("environment"); // NOI18N
        functions.add("error"); // NOI18N
        functions.add("executable"); // NOI18N
        functions.add("files"); // NOI18N
        functions.add("find_program"); // NOI18N
        functions.add("generator"); // NOI18N
        functions.add("get_option"); // NOI18N
        functions.add("get_variable"); // NOI18N
        functions.add("import"); // NOI18N
        functions.add("include_directories"); // NOI18N
        functions.add("install_data"); // NOI18N
        functions.add("install_emptydir"); // NOI18N
        functions.add("install_headers"); // NOI18N
        functions.add("install_man"); // NOI18N
        functions.add("install_subdir"); // NOI18N
        functions.add("install_symlink"); // NOI18N
        functions.add("is_disabler"); // NOI18N
        functions.add("is_variable"); // NOI18N
        functions.add("jar"); // NOI18N
        functions.add("join_paths"); // NOI18N
        functions.add("library"); // NOI18N
        functions.add("message"); // NOI18N
        functions.add("project"); // NOI18N
        functions.add("range"); // NOI18N
        functions.add("run_command"); // NOI18N
        functions.add("run_target"); // NOI18N
        functions.add("set_variable"); // NOI18N
        functions.add("shared_library"); // NOI18N
        functions.add("shared_module"); // NOI18N
        functions.add("static_library"); // NOI18N
        functions.add("structured_sources"); // NOI18N
        functions.add("subdir"); // NOI18N
        functions.add("subdir_done"); // NOI18N
        functions.add("subproject"); // NOI18N
        functions.add("summary"); // NOI18N
        functions.add("test"); // NOI18N
        functions.add("unset_variable"); // NOI18N
        functions.add("vcs_tag"); // NOI18N
        functions.add("warning"); // NOI18N

        objects.add("build_machine"); // NOI18N
        objects.add("host_machine"); // NOI18N
        objects.add("meson"); // NOI18N
        objects.add("target_machine"); // NOI18N

        keywords.add("and"); // NOI18N
        keywords.add("break"); // NOI18N
        keywords.add("continue"); // NOI18N
        keywords.add("elif"); // NOI18N
        keywords.add("else"); // NOI18N
        keywords.add("endforeach"); // NOI18N
        keywords.add("endif"); // NOI18N
        keywords.add("foreach"); // NOI18N
        keywords.add("if"); // NOI18N
        keywords.add("not"); // NOI18N
        keywords.add("or"); // NOI18N

        literals.add("false"); // NOI18N
        literals.add("true"); // NOI18N
    }

    private final LexerRestartInfo<MesonBuildTokenId> info;

    MesonBuildLexer(LexerRestartInfo<MesonBuildTokenId> info) {
        this.info = info;
    }

    @Override
    public Token<MesonBuildTokenId> nextToken () {
        LexerInput input = info.input ();
        int i = input.read ();
        switch (i) {
            case LexerInput.EOF:
                return null;
            case '+':
            case '<':
            case '>':
            case '!':
            case '=':
            case ',':
            case '(':
            case ')':
            case '{':
            case '}':
            case '[':
            case ']':
            case '-':
            case '*':
            case '/':
            case '\\':
            case ':':
            case '?':
            case '.':
            case '%':
                return info.tokenFactory().createToken(MesonBuildTokenId.OPERATOR);
            case ' ':
            case '\n':
            case '\r':
            case '\t':
                do {
                    i = input.read();
                } while ((i == ' ') || (i == '\n') || (i == '\r') || (i == '\t'));
                if (i != LexerInput.EOF) {
                    input.backup(1);
                }
                return info.tokenFactory().createToken(MesonBuildTokenId.WHITESPACE);
            case '#':
                do {
                    i = input.read();
                } while ((i != '\n') && (i != '\r') && (i != LexerInput.EOF));
                return info.tokenFactory().createToken(MesonBuildTokenId.COMMENT);
            case '\'':
                // meson supports simple string literals like 'string'
                // and multiline string literals like '''some really
                // long string'''
                if (input.read() == '\'') {
                    if (input.read() == '\'') {
                        do {
                            i = input.read();
                            if (i == '\'') {
                                i = input.read();
                                if (i == '\'') {
                                    i = input.read();
                                    if (i == '\'') {
                                        break;
                                    } else {
                                        input.backup(2);
                                    }
                                } else {
                                    input.backup(1);
                                }
                            }
                        } while (i != LexerInput.EOF);
                        return info.tokenFactory().createToken(MesonBuildTokenId.STRING);
                    } else {
                        input.backup(2);
                    }
                } else {
                    input.backup(1);
                }
                do {
                    i = input.read();
                    if (i == '\\') { // string literals can contain escape sequences
                        input.read();
                        i = input.read();
                    }
                } while ((i != '\'') && (i != '\n') && (i != '\r') && (i != LexerInput.EOF));
                return info.tokenFactory().createToken(MesonBuildTokenId.STRING);
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
                // meson only supports integer numeric literals
                // hexadecimal literals are supported starting with 0x
                // octal literals are supported starting with 0o
                // binary literals are supported starting with 0b
                if (i == '0') {
                    i = input.read();
                    switch (i) {
                        case 'x':
                            do {
                                i = java.lang.Character.toLowerCase(input.read());
                            } while (   ((i >= '0') && (i <= '9'))
                                     || ((i >= 'a') && (i <= 'f')));
                            input.backup(1);
                            return info.tokenFactory().createToken(MesonBuildTokenId.NUMBER);
                        case 'o':
                            do {
                                i = input.read();
                            } while ((i >= '0') && (i <= '7'));
                            input.backup(1);
                            return info.tokenFactory().createToken(MesonBuildTokenId.NUMBER);
                        case 'b':
                            do {
                                i = input.read();
                            } while ((i == '0') || (i == '1'));
                            input.backup(1);
                            return info.tokenFactory().createToken(MesonBuildTokenId.NUMBER);
                    }
                }
                do {
                    i = input.read();
                } while ((i >= '0') && (i <= '9'));
                input.backup(1);
                return info.tokenFactory().createToken(MesonBuildTokenId.NUMBER);
            default:
                if (   (i >= 'a' && i <= 'z')
                    || (i >= 'A' && i <= 'Z')
                    || (i == '_')) {
                    do {
                        i = input.read();
                    } while (   ((i >= 'a') && (i <= 'z'))
                             || ((i >= 'A') && (i <= 'Z'))
                             || ((i >= '0') && (i <= '9'))
                             || (i == '_'));
                    input.backup(1);
                    final String token = input.readText().toString();
                    if (keywords.contains(token)) {
                        return info.tokenFactory().createToken(MesonBuildTokenId.KEYWORD);
                    }
                    else if (functions.contains(token)) {
                        return info.tokenFactory().createToken(MesonBuildTokenId.FUNCTION);
                    }
                    else if (objects.contains(token)) {
                        return info.tokenFactory().createToken(MesonBuildTokenId.OBJECT);
                    }
                    else if (literals.contains(token)) {
                        return info.tokenFactory().createToken(MesonBuildTokenId.LITERAL);
                    }
                    return info.tokenFactory().createToken(MesonBuildTokenId.IDENTIFIER);
                }
                return info.tokenFactory().createToken(MesonBuildTokenId.ERROR);
        }
    }

    @Override
    public Object state() {
        return null;
    }

    @Override
    public void release() {
    }
}