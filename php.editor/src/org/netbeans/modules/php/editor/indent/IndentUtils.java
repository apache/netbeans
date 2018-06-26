/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.indent;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;

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
                    && !(token.id() == PHPTokenId.PHP_TOKEN
                    && (TokenUtilities.textEquals(token.text(), "(") // NOI18N
                    || TokenUtilities.textEquals(token.text(), "["))) // NOI18N
                    && ts.movePrevious()) {
                token = ts.token();
            }
            if (token.id() == PHPTokenId.PHP_CURLY_OPEN) {
                while (token.id() != PHPTokenId.PHP_CLASS
                        && token.id() != PHPTokenId.PHP_FUNCTION
                        && token.id() != PHPTokenId.PHP_IF
                        && token.id() != PHPTokenId.PHP_ELSE
                        && token.id() != PHPTokenId.PHP_ELSEIF
                        && token.id() != PHPTokenId.PHP_FOR
                        && token.id() != PHPTokenId.PHP_FOREACH
                        && token.id() != PHPTokenId.PHP_WHILE
                        && token.id() != PHPTokenId.PHP_DO
                        && token.id() != PHPTokenId.PHP_SWITCH
                        && ts.movePrevious()) {
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
                }
                value = bracePlacement == CodeStyle.BracePlacement.NEW_LINE_INDENTED ? previousIndent + codeStyle.getIndentSize() : previousIndent;
            }
        }
        return value;
    }
}
