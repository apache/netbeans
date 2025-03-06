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

package org.netbeans.modules.php.editor.indent;

import java.util.Collection;
import java.util.Set;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.lexer.utils.LexerUtils;

/**
 * This class will be unnecessary when issue #192289 is fixed.
 * @author Petr Pisl
 */
public final class IndentUtils {
    private static final int MAX_CACHED_INDENT = 80;

    private static final String[] CACHED_SPACES_STRINGS = new String[MAX_CACHED_INDENT + 1];
    static {
        CACHED_SPACES_STRINGS[0] = ""; //NOI18N
    }

    private static final int MAX_CACHED_TAB_SIZE = 8; // Should mostly be <= 8
    private static final Collection<PHPTokenId> BRACE_PLACEMENT_START_TOKENS = Set.of(
            PHPTokenId.PHP_CLASS,
            PHPTokenId.PHP_FUNCTION,
            PHPTokenId.PHP_IF,
            PHPTokenId.PHP_ELSE,
            PHPTokenId.PHP_ELSEIF,
            PHPTokenId.PHP_FOR,
            PHPTokenId.PHP_FOREACH,
            PHPTokenId.PHP_WHILE,
            PHPTokenId.PHP_DO,
            PHPTokenId.PHP_SWITCH,
            PHPTokenId.PHP_PUBLIC,
            PHPTokenId.PHP_PROTECTED,
            PHPTokenId.PHP_PRIVATE,
            PHPTokenId.PHP_PUBLIC_SET,
            PHPTokenId.PHP_PROTECTED_SET,
            PHPTokenId.PHP_PRIVATE_SET
    );
    /**
     * Cached indentation string containing tabs.
     * <br/>
     * The cache does not contain indents smaller than the particular tabSize
     * since they are only spaces contained in cachedSpacesStrings.
     */
    private static final String[][] CACHED_TAB_INDENTS = new String[MAX_CACHED_TAB_SIZE + 1][];

    private IndentUtils() {
    }

    static String cachedOrCreatedIndentString(int indent, boolean expandTabs, int tabSize) {
        String indentString;
        if (expandTabs || (indent < tabSize)) {
            if (indent <= MAX_CACHED_INDENT) {
                synchronized (CACHED_SPACES_STRINGS) {
                    indentString = CACHED_SPACES_STRINGS[indent];
                    if (indentString == null) {
                        // Create string with MAX_CACHED_SPACES spaces first if not cached yet
                        indentString = CACHED_SPACES_STRINGS[MAX_CACHED_INDENT];
                        if (indentString == null) {
                            indentString = createSpacesString(MAX_CACHED_INDENT);
                            CACHED_SPACES_STRINGS[MAX_CACHED_INDENT] = indentString;
                        }
                        indentString = indentString.substring(0, indent);
                        CACHED_SPACES_STRINGS[indent] = indentString;
                    }
                }
            } else {
                indentString = createSpacesString(indent);
            }

        } else { // Do not expand tabs
            if (indent <= MAX_CACHED_INDENT && tabSize <= MAX_CACHED_TAB_SIZE) {
                synchronized (CACHED_TAB_INDENTS) {
                    String[] tabIndents = CACHED_TAB_INDENTS[tabSize];
                    if (tabIndents == null) {
                        // Do not cache spaces-only strings
                        tabIndents = new String[MAX_CACHED_INDENT - tabSize + 1];
                        CACHED_TAB_INDENTS[tabSize] = tabIndents;
                    }
                    indentString = tabIndents[indent - tabSize];
                    if (indentString == null) {
                        indentString = createTabIndentString(indent, tabSize);
                        tabIndents[indent - tabSize] = indentString;
                    }
                }
            } else {
                indentString = createTabIndentString(indent, tabSize);
            }
        }
        return indentString;
    }

    private static String createSpacesString(int spaceCount) {
        StringBuilder sb = new StringBuilder(spaceCount);
        ArrayUtilities.appendSpaces(sb, spaceCount);
        return sb.toString();
    }

    private static String createTabIndentString(int indent, int tabSize) {
        StringBuilder sb = new StringBuilder();
        while (indent >= tabSize) {
            sb.append('\t'); //NOI18N
            indent -= tabSize;
        }
        ArrayUtilities.appendSpaces(sb, indent);
        return sb.toString();
    }


    /**
     * This method count new indent ofr braces and parent
     *
     * @param doc
     * @param offset - the original offset, where is cursor
     * @param currentIndent - the indnet that should be modified
     * @param previousIndent - indent of the line abot
     * @return
     */
    public static int countIndent(BaseDocument doc, int offset, int previousIndent) {
        int value = previousIndent;
        TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);
        if (ts != null) {
            ts.move(offset);
            if (!ts.movePrevious() || !ts.moveNext()) {
                return previousIndent;
            }
            Token<? extends PHPTokenId> token = ts.token();
            while (token.id() != PHPTokenId.PHP_CURLY_OPEN
                    && token.id() != PHPTokenId.PHP_SEMICOLON
                    && !LexerUtils.isOpenParen(token)
                    && !LexerUtils.isOpenBracket(token)
                    && ts.movePrevious()) {
                token = ts.token();
            }
            if (token.id() == PHPTokenId.PHP_CURLY_OPEN) {
                while (!BRACE_PLACEMENT_START_TOKENS.contains(token.id()) && ts.movePrevious()) {
                    token = ts.token();
                }
                CodeStyle codeStyle = CodeStyle.get(doc);
                CodeStyle.BracePlacement bracePlacement = codeStyle.getOtherBracePlacement();
                if (token.id() == PHPTokenId.PHP_CLASS) {
                    bracePlacement = codeStyle.getClassDeclBracePlacement();
                } else if (token.id() == PHPTokenId.PHP_FUNCTION) {
                    bracePlacement = codeStyle.getMethodDeclBracePlacement();
                } else if (token.id() == PHPTokenId.PHP_IF || token.id() == PHPTokenId.PHP_ELSE || token.id() == PHPTokenId.PHP_ELSEIF) {
                    bracePlacement = codeStyle.getIfBracePlacement();
                } else if (token.id() == PHPTokenId.PHP_FOR || token.id() == PHPTokenId.PHP_FOREACH) {
                    bracePlacement = codeStyle.getForBracePlacement();
                } else if (token.id() == PHPTokenId.PHP_WHILE || token.id() == PHPTokenId.PHP_DO) {
                    bracePlacement = codeStyle.getWhileBracePlacement();
                } else if (token.id() == PHPTokenId.PHP_SWITCH) {
                    bracePlacement = codeStyle.getSwitchBracePlacement();
                } else if (LexerUtils.isGetOrSetVisibilityToken(token)) {
                    bracePlacement = codeStyle.getFieldDeclBracePlacement();
                }
                value = bracePlacement == CodeStyle.BracePlacement.NEW_LINE_INDENTED ? previousIndent + codeStyle.getIndentSize() : previousIndent;
            }
        }
        return value;
    }
}
