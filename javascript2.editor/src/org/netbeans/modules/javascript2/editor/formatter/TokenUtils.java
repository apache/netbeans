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
package org.netbeans.modules.javascript2.editor.formatter;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;

/**
 * Utility methods extracted from {@link FormatVisitor}.
 * 
 * @author Dusan Balek, Petr Hejl
 */
final class TokenUtils {
    
    private final TokenSequence<? extends JsTokenId> ts;
    private final FormatTokenStream tokenStream;
    private final int formatFinish;

    TokenUtils(TokenSequence<? extends JsTokenId> ts, FormatTokenStream tokenStream, int formatFinish) {
        this.ts = ts;
        this.tokenStream = tokenStream;
        this.formatFinish = formatFinish;
    }

    public FormatToken getNextToken(int offset, JsTokenId expected) {
        return getToken(offset, expected, false, false, null);
    }

    public FormatToken getNextToken(int offset, JsTokenId expected, int stop) {
        return getToken(offset, expected, false, false, stop);
    }

    public FormatToken getNextToken(int offset, JsTokenId expected, boolean startFallback) {
        return getToken(offset, expected, false, startFallback, null);
    }

    public FormatToken getPreviousToken(int offset, JsTokenId expected) {
        return getPreviousToken(offset, expected, false);
    }

    public FormatToken getPreviousToken(int offset, JsTokenId expected, int stop) {
        return getToken(offset, expected, true, false, stop);
    }

    public FormatToken getPreviousToken(int offset, JsTokenId expected, boolean startFallback) {
        return getToken(offset, expected, true, startFallback, null);
    }

    private FormatToken getToken(int offset, JsTokenId expected, boolean backward,
            boolean startFallback, Integer stopMark) {

        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }

        Token<? extends JsTokenId> token = ts.token();
        if (expected != null) {
            while (expected != token.id()
                    && (stopMark == null || ((stopMark >= ts.offset() && !backward) || (stopMark <=ts.offset() && backward)))
                    && ((backward && ts.movePrevious()) || (!backward && ts.moveNext()))) {
                token = ts.token();
            }
            if (expected != token.id()) {
                return null;
            }
        }
        if (stopMark != null && ((ts.offset() > stopMark && !backward) || (ts.offset() < stopMark && backward))) {
            return null;
        }
        if (token != null) {
            return getFallback(ts.offset(), startFallback);
        }
        return null;
    }

    public Token getPreviousNonEmptyToken(int offset) {
        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }

        Token ret = null;
        while (ts.movePrevious()) {
            Token token = ts.token();
            if ((token.id() != JsTokenId.BLOCK_COMMENT && token.id() != JsTokenId.DOC_COMMENT
                && token.id() != JsTokenId.LINE_COMMENT && token.id() != JsTokenId.EOL
                && token.id() != JsTokenId.WHITESPACE)) {
                ret = token;
                break;
            }
        }
        return ret;
    }

    public Token getNextNonEmptyToken(int offset) {
        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }

        Token ret = null;
        while (ts.moveNext()) {
            Token token = ts.token();
            if ((token.id() != JsTokenId.BLOCK_COMMENT && token.id() != JsTokenId.DOC_COMMENT
                && token.id() != JsTokenId.LINE_COMMENT && token.id() != JsTokenId.EOL
                && token.id() != JsTokenId.WHITESPACE)) {
                ret = token;
                break;
            }
        }
        return ret;
    }

    public FormatToken getPreviousNonWhiteToken(int offset, int stop, JsTokenId expected, boolean startFallback) {
        assert stop <= offset;
        FormatToken ret = getPreviousToken(offset, expected, startFallback);
        if (startFallback && ret != null && ret.getKind() == FormatToken.Kind.SOURCE_START) {
            return ret;
        }

        if (ret != null) {
            if (expected == null) {
                return ret;
            }

            Token token = null;
            while (ts.movePrevious() && ts.offset() >= stop) {
                Token current = ts.token();
                if (current.id() != JsTokenId.WHITESPACE) {
                    token = current;
                    break;
                }
            }

            if (token != null) {
                return getFallback(ts.offset(), startFallback);
            }
        }
        return null;
    }

    public FormatToken getFallback(int offset, boolean fallback) {
        FormatToken ret = tokenStream.getToken(offset);
        if (ret == null && fallback && offset < formatFinish) {
            ret = tokenStream.getTokens().get(0);
            assert ret != null && ret.getKind() == FormatToken.Kind.SOURCE_START;
        }
        return ret;
    }


    @CheckForNull
    public static FormatToken findVirtualToken(FormatToken token, FormatToken.Kind kind,
            boolean backwards) {
        FormatToken result = backwards ? token.previous() : token.next();
        while (result != null && result.isVirtual()
                && result.getKind() != kind) {
            result = backwards ? result.previous() : result.next();;
        }
        if (result != null && result.getKind() != kind) {
            return null;
        }
        return result;
    }

    public static void appendTokenAfterLastVirtual(FormatToken previous,
            FormatToken token) {
        appendTokenAfterLastVirtual(previous, token, false);
    }

    public static void appendTokenAfterLastVirtual(FormatToken previous,
            FormatToken token, boolean checkDuplicity) {

        assert previous != null;

        @NonNull
        FormatToken current = previous;
        FormatToken next = current.next();

        while (next != null && next.isVirtual()) {
            current = next;
            next = next.next();
        }
        if (!checkDuplicity || !current.isVirtual() || !token.isVirtual()
                || current.getKind() != token.getKind()) {
            appendToken(current, token);
        }
    }

    public static void appendToken(FormatToken previous, FormatToken token) {
        FormatToken original = previous.next();
        previous.setNext(token);
        token.setPrevious(previous);
        token.setNext(original);
        if (original != null) {
            original.setPrevious(token);
        }
    }    
}
