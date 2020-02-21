/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.editor.cplusplus;

import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.editor.indent.CppIndentTask;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor;
import org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor.MutableContext;

/**
 *
 */
@MimeRegistrations({
    // cnd source files
    @MimeRegistration(mimeType = MIMENames.HEADER_MIME_TYPE, service = TypedBreakInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.CPLUSPLUS_MIME_TYPE, service = TypedBreakInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.C_MIME_TYPE, service = TypedBreakInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.DOXYGEN_MIME_TYPE, service = TypedBreakInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.STRING_DOUBLE_MIME_TYPE, service = TypedBreakInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.STRING_SINGLE_MIME_TYPE, service = TypedBreakInterceptor.Factory.class),
    @MimeRegistration(mimeType = MIMENames.PREPROC_MIME_TYPE, service = TypedBreakInterceptor.Factory.class)
})
public class CppTBIFactory implements TypedBreakInterceptor.Factory {

    @Override
    public TypedBreakInterceptor createTypedBreakInterceptor(MimePath mimePath) {
        return new TypedBreakInterceptorImpl();
    }

    private static class TypedBreakInterceptorImpl implements TypedBreakInterceptor {

        static final boolean DEBUG = false;
        private boolean postShift = false;

        public TypedBreakInterceptorImpl() {
        }

        @Override
        public boolean beforeInsert(Context context) throws BadLocationException {
            return false;
        }

        @Override
        public void insert(MutableContext context) throws BadLocationException {
            BaseDocument doc = (BaseDocument) context.getDocument();
            int dotPos = context.getCaretOffset();
            Caret caret = context.getComponent().getCaret();
            postShift = false;
            Object doWork = doWork(doc, dotPos, caret, context);
            if (doWork instanceof Integer) {
                postShift = true;
            }
        }

        @Override
        public void afterInsert(Context context) throws BadLocationException {
            if (postShift) {
                Caret caret = context.getComponent().getCaret();
                caret.setDot(context.getCaretOffset() + 1);
            }
        }

        @Override
        public void cancelled(Context context) {
        }
    }

    public static Object doWork(BaseDocument doc, int dotPos, Caret caret, MutableContext context) {
        if (BracketCompletion.posWithinNonRawString(doc, dotPos)) {
            try {
                if ((dotPos >= 1 && DocumentUtilities.getText(doc).charAt(dotPos - 1) != '\\')
                        || (dotPos >= 2 && DocumentUtilities.getText(doc).charAt(dotPos - 2) == '\\')) {
                    // not line continuation
                    if (context != null) {
                        context.setText("\"\n\"", 1, 3); //NOI18N
                        return true;
                    } else {
                        doc.insertString(dotPos, "\"\"", null); //NOI18N
                        dotPos += 1;
                        caret.setDot(dotPos);
                        return Integer.valueOf(1);
                    }
                }
            } catch (BadLocationException ex) {
            }
        } else {
            try {
                if (BracketCompletion.isAddRightBrace(doc, dotPos)) {
                    int end = BracketCompletion.getRowOrBlockEnd(doc, dotPos);
                    String insString = "}"; // NOI18N
                    // XXX: vv159170 simplest hack
                    // insert "};" for "{" when in "enum", "class", "struct" and union completion
                    // NOI18N
                    // XXX: vv159170 simplest hack
                    // insert "};" for "{" when in "enum", "class", "struct" and union completion
                    TokenItem<TokenId> firstNonWhiteBwd = CndTokenUtilities.getFirstNonWhiteBwd(doc, end);
                    if (firstNonWhiteBwd == null) {
                        return false;
                    }
                    if (!(firstNonWhiteBwd.id() == CppTokenId.LBRACE ||
                        firstNonWhiteBwd.id() == CppTokenId.LINE_COMMENT ||
                        firstNonWhiteBwd.id() == CppTokenId.BLOCK_COMMENT)) {
                        return false;
                    }
                    int lBracePos = firstNonWhiteBwd.offset();
                    int lastSepOffset = CndTokenUtilities.getLastCommandSeparator(doc, lBracePos - 1);
                    if (lastSepOffset == -1 && lBracePos > 0) {
                        lastSepOffset = 0;
                    }
                    if (lastSepOffset != -1 && lastSepOffset < dotPos) {
                        TokenSequence<TokenId> cppTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, lBracePos, false, false);
                        loop:
                        while (cppTokenSequence.movePrevious() && cppTokenSequence.offset() >= lastSepOffset) {
                            TokenId id = cppTokenSequence.token().id();
                            if (id instanceof CppTokenId) {
                                switch ((CppTokenId) id) {
                                    case RPAREN:
                                    case RBRACKET:
                                        break loop;
                                    case CLASS:
                                    case UNION:
                                    case STRUCT:
                                    case ENUM:
                                        insString = "};"; // NOI18N
                                        break loop;
                                }
                            }
                        }
                    }
                    if (context != null) {
                        context.setText("\n\n" + insString, 0, 1, 2, 2 + insString.length(), 1, 2); //NOI18N
                    } else {
                        doc.insertString(end, "\n" + insString, null); // NOI18N
                        // Lock does not need because method is invoked from BaseKit that already lock indent.
                        // NOI18N
                        // Lock does not need because method is invoked from BaseKit that already lock indent.
                        Indent indent = Indent.get(doc);
                        indent.lock();
                        try {
                            indent.reindent(end + 1);
                        } finally {
                            indent.unlock();
                        }
                        caret.setDot(dotPos);
                    }
                    return true;
                }
            } catch (BadLocationException ex) {
            }
        }
        if (context != null) {
            try {
                if (new CppIndentTask(context).doxyGen()) {
                    return true;
                }
            } catch (BadLocationException ex) {
            }
        }
        return false;
    }
}
