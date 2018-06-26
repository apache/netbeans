/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.core.syntax;

import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.web.indent.api.LexUtilities;
import org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor;

/**
 *
 * @author Petr Hejl
 */
public class JspTypedBreakInterceptor implements TypedBreakInterceptor {

    @Override
    public boolean beforeInsert(Context context) throws BadLocationException {
        return false;
    }

    @Override
    public void insert(MutableContext context) throws BadLocationException {
        // TODO: below whitespace skipping does not work because whitespace
        // tokens between JSP tokens are actually HTML tokens and not JSP tokens.
        // Proper way is to iterate over document characters and skip all whitespaces
        // till you get to a text and then get token for the text.
        BaseDocument doc = (BaseDocument) context.getDocument();
        int caretOffset = context.getCaretOffset();
        TokenSequence<JspTokenId> ts = LexUtilities.getTokenSequence(doc, caretOffset, JspTokenId.language());
        if (ts == null) {
            return;
        }
        ts.move(caretOffset);
        String closingTagName = null;
        int end = -1;
        if (ts.moveNext() && ts.token().id() == JspTokenId.SYMBOL
                && ts.token().text().toString().equals("</")) {
            if (ts.moveNext() && ts.token().id() == JspTokenId.ENDTAG) {
                closingTagName = ts.token().text().toString();
                end = ts.offset() + ts.token().text().length();
                ts.movePrevious();
                ts.movePrevious();
            }
        }
        if (closingTagName == null) {
            return;
        }
        boolean foundOpening = false;
        if (ts.token().id() == JspTokenId.SYMBOL
                && ts.token().text().toString().equals(">")) {
            while (ts.movePrevious()) {
                if (ts.token().id() == JspTokenId.TAG) {
                    if (ts.token().text().toString().equals(closingTagName)) {
                        foundOpening = true;
                    }
                    break;
                }
            }
        }
        if (foundOpening) {
            int indent = GsfUtilities.getLineIndent(doc, caretOffset);
            StringBuilder text = new StringBuilder();
            text.append("\n");
            text.append(IndentUtils.createIndentString(doc, indent + IndentUtils.indentLevelSize(doc)));
            int caret = text.length();
            text.append("\n");
            text.append(IndentUtils.createIndentString(doc, indent));
            context.setText(text.toString(), caret, caret);
        }
    }

    @Override
    public void afterInsert(Context context) throws BadLocationException {
    }

    @Override
    public void cancelled(Context context) {
    }

    @MimeRegistrations({
        @MimeRegistration(mimeType = JspKit.JSP_MIME_TYPE, service = TypedBreakInterceptor.Factory.class),
        @MimeRegistration(mimeType = JspKit.TAG_MIME_TYPE, service = TypedBreakInterceptor.Factory.class)
    })
    public static class JspFactory implements TypedBreakInterceptor.Factory {

        @Override
        public TypedBreakInterceptor createTypedBreakInterceptor(MimePath mimePath) {
            return new JspTypedBreakInterceptor();
        }

    }
}
