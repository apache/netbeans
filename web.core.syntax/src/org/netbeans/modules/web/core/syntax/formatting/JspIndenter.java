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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.core.syntax.formatting;

import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence;
import org.netbeans.modules.web.indent.api.support.IndenterContextData;
import org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter;

/**
 *
 * @author david
 */
public class JspIndenter extends MarkupAbstractIndenter<JspTokenId> {

//    private boolean inScriptlet;

    public JspIndenter(Context context) {
        super(JspTokenId.language(), context);
    }

    @Override
    protected boolean isWhiteSpaceToken(Token<JspTokenId> token) {
        return token.id() == JspTokenId.WHITESPACE ||
                (token.id() == JspTokenId.TEXT && token.text().toString().trim().length() == 0);
    }


    @Override
    protected boolean isOpenTagNameToken(Token<JspTokenId> token) {
        return token.id() == JspTokenId.TAG;
    }

    @Override
    protected boolean isCloseTagNameToken(Token<JspTokenId> token) {
        return token.id() == JspTokenId.ENDTAG;
    }

    @Override
    protected boolean isStartTagSymbol(Token<JspTokenId> token) {
        return token.id() == JspTokenId.SYMBOL &&
            (token.text().toString().equals("<") || 
            token.text().toString().equals("<%@"));
    }

    @Override
    protected boolean isStartTagClosingSymbol(Token<JspTokenId> token) {
        return token.id() == JspTokenId.SYMBOL &&
                token.text().toString().equals("</");
    }

    @Override
    protected boolean isEndTagSymbol(Token<JspTokenId> token) {
        return token.id() == JspTokenId.SYMBOL &&
                token.text().toString().equals(">");
    }

    @Override
    protected boolean isEndTagClosingSymbol(Token<JspTokenId> token) {
        return token.id() == JspTokenId.SYMBOL &&
                (token.text().toString().equals("/>") || token.text().toString().equals("%>"));
    }

    @Override
    protected boolean isTagArgumentToken(Token<JspTokenId> token) {
        return token.id() == JspTokenId.ATTRIBUTE;
    }

    @Override
    protected boolean isBlockCommentToken(Token<JspTokenId> token) {
        return token.id() == JspTokenId.COMMENT;
    }

    @Override
    protected boolean isTagContentToken(Token<JspTokenId> token) {
        return token.id() == JspTokenId.TEXT;
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
        return null;
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
    protected boolean isPreservedLine(Token<JspTokenId> token, IndenterContextData<JspTokenId> context) {
        String text = token.text().toString().trim();
        if (token.id() == JspTokenId.COMMENT) {
            if (!text.startsWith("<%--") && !text.startsWith("--%>")) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected int getPreservedLineInitialIndentation(JoinedTokenSequence<JspTokenId> ts)
            throws BadLocationException {
        int[] index = ts.index();
        boolean found = false;
        do {
            if (ts.token().id() == JspTokenId.COMMENT) {
                String comment = ts.token().text().toString().trim();
                if (comment.startsWith("<%--")) {
                    found = true;
                    break;
                }
            } else {
                break;
            }
        } while (ts.movePrevious());
        int indent = 0;
        if (found) {
            int lineStart = Utilities.getRowStart(getDocument(), ts.offset());
            // TODO: can comment token start with spaces?? if yes then adjust
            // column to point to first non-whitespace
            int column = ts.offset();
            indent = column - lineStart;
        }
        ts.moveIndex(index);
        ts.moveNext();
        return indent;
    }

    @Override
    protected boolean isForeignLanguageStartToken(Token<JspTokenId> token, JoinedTokenSequence<JspTokenId> ts) {
        return token.id() == JspTokenId.SYMBOL2 && (
                token.text().toString().equals("<%") || 
                token.text().toString().equals("<%=") ||
                token.text().toString().equals("<%!"));
    }

    @Override
    protected boolean isForeignLanguageEndToken(Token<JspTokenId> token, JoinedTokenSequence<JspTokenId> ts) {
        return token.id() == JspTokenId.SYMBOL2 && token.text().toString().equals("%>");
    }

    @Override
    protected boolean isStableFormattingStartToken(Token<JspTokenId> token, JoinedTokenSequence<JspTokenId> ts) {
        return token.id() == JspTokenId.SYMBOL2 && (
                token.text().toString().equals("<%") ||
                token.text().toString().equals("<%=") ||
                token.text().toString().equals("<%!"));
    }

}
