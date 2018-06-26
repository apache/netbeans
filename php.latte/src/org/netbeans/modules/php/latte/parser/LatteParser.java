/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.latte.parser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import javax.swing.event.ChangeListener;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.php.latte.lexer.LatteMarkupTokenId;
import org.netbeans.modules.php.latte.utils.LatteLexerUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class LatteParser extends Parser {
    private static final String CLOSING_SIGN = "/"; //NOI18N
    private static final List<String> PAIR_MACROS = new ArrayList<>();
    static {
        PAIR_MACROS.add("cache"); //NOI18N
        PAIR_MACROS.add("capture"); //NOI18N
        PAIR_MACROS.add("define"); //NOI18N
        PAIR_MACROS.add("first"); //NOI18N
        PAIR_MACROS.add("for"); //NOI18N
        PAIR_MACROS.add("foreach"); //NOI18N
        PAIR_MACROS.add("form"); //NOI18N
        PAIR_MACROS.add("if"); //NOI18N
        PAIR_MACROS.add("ifCurrent"); //NOI18N
        PAIR_MACROS.add("ifset"); //NOI18N
        PAIR_MACROS.add("last"); //NOI18N
        PAIR_MACROS.add("sep"); //NOI18N
        PAIR_MACROS.add("snippet"); //NOI18N
        PAIR_MACROS.add("while"); //NOI18N
    }
    private final Deque<Macro> macros = new ArrayDeque<>();
    private LatteParserResult parserResult;

    public LatteParser() {
    }

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        parserResult = new LatteParserResult(snapshot);
        macros.clear();
        TokenHierarchy<?> tokenHierarchy = snapshot.getTokenHierarchy();
        LanguagePath latteLanguagePath = LatteLexerUtils.fetchLanguagePath(tokenHierarchy, LatteMarkupTokenId.language());
        if (latteLanguagePath != null) {
            List<TokenSequence<?>> tokenSequenceList = tokenHierarchy.tokenSequenceList(latteLanguagePath, 0, Integer.MAX_VALUE);
            processTokenSequences(tokenSequenceList);
        }
    }

    private void processTokenSequences(List<TokenSequence<?>> tokenSequenceList) {
        for (TokenSequence<?> tokenSequence : tokenSequenceList) {
            processTokenSequence(tokenSequence);
        }
        for (Macro macro : macros) {
            parserResult.addError(Bundle.ERR_UnclosedMacro(macro.getName()), macro.getOffset(), macro.getLength());
        }
    }

    @NbBundle.Messages({
        "# {0} - macro name",
        "ERR_UnopenendMacro=Unopenend macro: {0}",
        "# {0} - macro name",
        "ERR_UnclosedMacro=Unclosed macro: {0}"
    })
    private void processTokenSequence(TokenSequence<?> tokenSequence) {
        while (tokenSequence.moveNext()) {
            Token<LatteMarkupTokenId> token = (Token<LatteMarkupTokenId>) tokenSequence.token();
            LatteMarkupTokenId tokenId = token.id();
            CharSequence tokenText = token.text();
            if (tokenId == LatteMarkupTokenId.T_MACRO_START && mustBeEnded(tokenText)) {
                macros.push(new Macro(token, tokenSequence.offset()));
            } else if (tokenId == LatteMarkupTokenId.T_MACRO_END && mustBeStarted(tokenText)) {
                Macro lastMacro = macros.peek();
                if (lastMacro == null) {
                    if (!CLOSING_SIGN.equals(tokenText)) {
                        parserResult.addError(Bundle.ERR_UnopenendMacro(tokenText), tokenSequence.offset(), token.length());
                    }
                } else {
                    macros.pop();
                    if (!lastMacro.endsWith(tokenText)) {
                        parserResult.addError(Bundle.ERR_UnclosedMacro(lastMacro.getName()), lastMacro.getOffset(), lastMacro.getLength());
                        lastMacro = macros.peek();
                        if (lastMacro != null && lastMacro.endsWith(tokenText)) {
                            macros.pop();
                        }
                    }
                }
            }
        }
    }

    private static boolean mustBeEnded(CharSequence macroName) {
        return PAIR_MACROS.contains(CharSequenceUtilities.toString(macroName));
    }

    private static boolean mustBeStarted(CharSequence macroName) {
        return PAIR_MACROS.contains(CharSequenceUtilities.toString(macroName.subSequence(1, macroName.length()))) || CLOSING_SIGN.equals(macroName);
    }

    @Override
    public Parser.Result getResult(Task task) throws ParseException {
        return parserResult;
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
    }

    private static final class Macro {
        private final Token<LatteMarkupTokenId> token;
        private final int offset;

        private Macro(Token<LatteMarkupTokenId> token, int offset) {
            this.token = token;
            this.offset = offset;
        }

        private boolean endsWith(CharSequence endingTokenText) {
            CharSequence tokenText = token.text();
            return CharSequenceUtilities.textEquals(tokenText, endingTokenText.subSequence(1, endingTokenText.length())) || CLOSING_SIGN.equals(endingTokenText);
        }

        public String getName() {
            return CharSequenceUtilities.toString(token.text());
        }

        public int getOffset() {
            return offset;
        }

        public int getLength() {
            return token.length();
        }

        @Override
        public String toString() {
            return getName();
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 59 * hash + Objects.hashCode(this.token);
            hash = 59 * hash + this.offset;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Macro other = (Macro) obj;
            if (!Objects.equals(this.token, other.token)) {
                return false;
            }
            return this.offset == other.offset;
        }

    }

}
