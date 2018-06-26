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

import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.web.indent.api.LexUtilities;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

/**
 *
 * @author Petr Hejl
 */
public class JspTypedTextInterceptor implements TypedTextInterceptor {

    private boolean showCompletion;

    @Override
    public boolean beforeInsert(Context context) throws BadLocationException {
        return false;
    }

    @Override
    public void insert(MutableContext context) throws BadLocationException {
        showCompletion = handledELBracketsCompletion(context);
    }

    @Override
    public void afterInsert(final Context context) throws BadLocationException {
        char ch = context.getText().charAt(0);
        final BaseDocument doc = (BaseDocument) context.getDocument();
        if (ch == '}') { // NOI18N
            final AtomicReference<BadLocationException> ex = new AtomicReference<BadLocationException>();
            doc.runAtomicAsUser(new Runnable() {

                @Override
                public void run() {
                    int caretOffset = context.getOffset();
                    Caret caret = context.getComponent().getCaret();
                    try {
                        TokenSequence<JspTokenId> ts = LexUtilities.getTokenSequence(
                                doc, caretOffset, JspTokenId.language());
                        if (ts == null) {
                            return;
                        }
                        ts.move(caretOffset);
                        if (!ts.moveNext() && !ts.movePrevious()) {
                            return;
                        }
                        do {
                            Token<JspTokenId> token = ts.token();
                            if (token.id() == JspTokenId.EOL) {
                                break;
                            }

                            if (token.id() == JspTokenId.EL) {
                                String elText = CharSequenceUtilities.toString(token.text());
                                TokenHierarchy<Document> tokenHierarchy = TokenHierarchy.get((Document) doc);
                                int offset = token.offset(tokenHierarchy) + token.length();
                                if (elText.matches("(\\$\\{|\\#\\{).*") && offset > caretOffset) {  //NOI18N
                                    doc.remove(caretOffset, 1);
                                    caret.setDot(caretOffset + 1); // skip closing bracket
                                    return;
                                }
                            }
                        } while (ts.movePrevious());

                    } catch (BadLocationException blex) {
                        ex.set(blex);
                    }
                }
            });
            BadLocationException blex = ex.get();
            if (blex != null) {
                throw blex;
            }
            return;
        } else if (ch == '>') { // NOI18N
            final AtomicReference<BadLocationException> ex = new AtomicReference<BadLocationException>();
            doc.render(new Runnable() {

                @Override
                public void run() {
                    int caretOffset = context.getOffset();
                    try {
                        TokenSequence<JspTokenId> ts = LexUtilities.getTokenSequence(
                                doc, caretOffset, JspTokenId.language());
                        if (ts == null) {
                            return;
                        }
                        ts.move(caretOffset);
                        boolean found = false;
                        while (ts.movePrevious()) {
                            if (ts.token().id() == JspTokenId.SYMBOL && (ts.token().text().toString().equals("<")
                                    || ts.token().text().toString().equals("</"))) {
                                found = true;
                                break;
                            }
                            if (ts.token().id() == JspTokenId.SYMBOL && ts.token().text().toString().equals(">")) {
                                break;
                            }
                            if (ts.token().id() != JspTokenId.ATTRIBUTE
                                    && ts.token().id() != JspTokenId.ATTR_VALUE
                                    && ts.token().id() != JspTokenId.TAG
                                    && ts.token().id() != JspTokenId.ENDTAG
                                    && ts.token().id() != JspTokenId.SYMBOL
                                    && ts.token().id() != JspTokenId.EOL
                                    && ts.token().id() != JspTokenId.WHITESPACE) {
                                break;
                            }
                        }

                        if (found) {
                            //ok, the user just type tag closing symbol, lets reindent the line
                            //since the code runs under document read lock, we cannot lock the
                            //indentation infrastructure directly. Instead of that create a new
                            //AWT task and post it for later execution.
                            final Position from = doc.createPosition(Utilities.getRowStart(doc, ts.offset()));
                            final Position to = doc.createPosition(Utilities.getRowEnd(doc, ts.offset()));

                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    final Indent indent = Indent.get(doc);
                                    indent.lock();
                                    try {
                                        doc.runAtomic(new Runnable() {

                                            @Override
                                            public void run() {
                                                try {
                                                    indent.reindent(from.getOffset(), to.getOffset());
                                                } catch (BadLocationException ex) {
                                                    //ignore
                                                }
                                            }
                                        });
                                    } finally {
                                        indent.unlock();
                                    }
                                }
                            });
                        }
                    } catch (BadLocationException blex) {
                        ex.set(blex);
                    }
                }
            });
            BadLocationException blex = ex.get();
            if (blex != null) {
                throw blex;
            }
            return;
        }
        showCompletion();
    }

    @Override
    public void cancelled(Context context) {
        showCompletion = false;
    }

    private boolean handledELBracketsCompletion(MutableContext context) throws BadLocationException {
        // EL expression completion - #234702
        BaseDocument doc = (BaseDocument) context.getDocument();
        int dotPos = context.getOffset();
        if (dotPos > 0) {
            String charPrefix = doc.getText(dotPos - 1, 1);
            String str = context.getText();
            if ("{".equals(str) && ("#".equals(charPrefix) || "$".equals(charPrefix))) { //NOI18N
                context.setText("{}", 1); // NOI18N
                return true;
            }
        }
        return false;
    }

    private void showCompletion() {
        if (showCompletion) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    Completion.get().showCompletion();
                }
            });
        }
    }

    @MimeRegistrations({
        @MimeRegistration(mimeType = JspKit.JSP_MIME_TYPE, service = TypedTextInterceptor.Factory.class),
        @MimeRegistration(mimeType = JspKit.TAG_MIME_TYPE, service = TypedTextInterceptor.Factory.class)
    })
    public static class JspFactory implements TypedTextInterceptor.Factory {

        @Override
        public TypedTextInterceptor createTypedTextInterceptor(MimePath mimePath) {
            return new JspTypedTextInterceptor();
        }
    }
}
