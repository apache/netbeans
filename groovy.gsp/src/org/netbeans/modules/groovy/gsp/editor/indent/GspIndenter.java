/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.gsp.editor.indent;

import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.groovy.gsp.lexer.GspLexerLanguage;
import org.netbeans.modules.groovy.gsp.lexer.GspTokenId;
import org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence;
import org.netbeans.modules.web.indent.api.support.IndenterContextData;
import org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter;

/**
 *
 * @author Martin Janicek
 */
public class GspIndenter extends MarkupAbstractIndenter<GspTokenId> {

    public GspIndenter(Context context) {
        super(GspLexerLanguage.getLanguage(), context);
    }


    @Override
    protected boolean isOpenTagNameToken(Token<GspTokenId> token) {
        return token.id() == GspTokenId.GTAG_OPENING_NAME;
    }

    @Override
    protected boolean isCloseTagNameToken(Token<GspTokenId> token) {
        return token.id() == GspTokenId.GTAG_CLOSING_NAME;
    }

    @Override
    protected boolean isStartTagSymbol(Token<GspTokenId> token) {
        return token.id() == GspTokenId.GTAG_OPENING_START;
    }

    @Override
    protected boolean isStartTagClosingSymbol(Token<GspTokenId> token) {
        return token.id() == GspTokenId.GTAG_CLOSING_START;
    }

    @Override
    protected boolean isEndTagSymbol(Token<GspTokenId> token) {
        return token.id() == GspTokenId.GTAG_OPENING_END
            || token.id() == GspTokenId.GTAG_CLOSING_END;
    }

    @Override
    protected boolean isEndTagClosingSymbol(Token<GspTokenId> token) {
        return token.id() == GspTokenId.GTAG_INDEPENDENT_END;
    }

    @Override
    protected boolean isTagArgumentToken(Token<GspTokenId> token) {
        return false;
    }

    @Override
    protected boolean isBlockCommentToken(Token<GspTokenId> token) {
        return token.id().isComment();
    }

    @Override
    protected boolean isTagContentToken(Token<GspTokenId> token) {
        return token.id() == GspTokenId.GTAG_ATTRIBUTE_NAME || token.id() == GspTokenId.GTAG_ATTRIBUTE_VALUE;
    }

    @Override
    protected boolean isClosingTagOptional(CharSequence tagName) {
        return false;
    }

    @Override
    protected boolean isOpeningTagOptional(CharSequence tagName) {
        return false;
    }

    @Override
    protected Boolean isEmptyTag(CharSequence tagName) {
        return Boolean.FALSE;
    }

    @Override
    protected boolean isTagContentUnformattable(CharSequence tagName) {
        return false;
    }

    @Override
    protected Set<String> getTagChildren(CharSequence tagName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean isPreservedLine(Token<GspTokenId> token, IndenterContextData<GspTokenId> context) {
        if (token.id().isComment()) {
            return true;
        }
        return false;
    }

    @Override
    protected int getPreservedLineInitialIndentation(JoinedTokenSequence<GspTokenId> ts) throws BadLocationException {
        int[] index = ts.index();
        boolean found = false;
        do {
            if (ts.token().id().isComment()) {
                found = true;
                break;
            } else {
                break;
            }
        } while (ts.movePrevious());
        int indent = 0;
        if (found) {
            int lineStart = Utilities.getRowStart(getDocument(), ts.offset());
            int column = ts.offset();
            indent = column - lineStart;
        }
        ts.moveIndex(index);
        ts.moveNext();
        return indent;
    }

    @Override
    protected boolean isForeignLanguageStartToken(Token<GspTokenId> token, JoinedTokenSequence<GspTokenId> ts) {
        return token.id().isStartDelimiter();
    }

    @Override
    protected boolean isForeignLanguageEndToken(Token<GspTokenId> token, JoinedTokenSequence<GspTokenId> ts) {
        return token.id().isEndDelimiter();
    }

    @Override
    protected boolean isWhiteSpaceToken(Token<GspTokenId> token) {
        return token.id() == GspTokenId.WHITESPACE
            || (token.id() == GspTokenId.HTML && token.text().toString().trim().length() == 0);
    }
}
