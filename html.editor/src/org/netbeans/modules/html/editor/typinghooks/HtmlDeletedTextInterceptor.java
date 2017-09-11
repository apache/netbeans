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
package org.netbeans.modules.html.editor.typinghooks;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.web.indent.api.LexUtilities;
import org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor;

/**
 *
 * @author marek
 */
public class HtmlDeletedTextInterceptor implements DeletedTextInterceptor {

    @Override
    public boolean beforeRemove(Context context) throws BadLocationException {
        //no-op
        return false;
    }

    //called *after* the change
    @Override
    public void remove(Context context) throws BadLocationException {
        Document doc = context.getDocument();
        int dotPos = context.getOffset();
        char ch = context.getText().charAt(0);
        if (ch == '\'' || ch == '"') { //NOI18N
            TokenSequence<HTMLTokenId> ts = LexUtilities.getTokenSequence((BaseDocument) doc, dotPos, HTMLTokenId.language());
            if (ts != null) {
                int diff = ts.move(dotPos);
                if (diff != 1) {
                    //we only handle situation where leading quote is removed from an attribute value -- the diff must be 1
                    return;
                }
                if (!ts.moveNext()) {
                    return;
                }
                Token<HTMLTokenId> token = ts.token();
                if (token.id() == HTMLTokenId.VALUE || token.id() == HTMLTokenId.VALUE_CSS || token.id() == HTMLTokenId.VALUE_JAVASCRIPT) {
                    char first = token.text().charAt(0);
                    if (first == ch) {
                        //user pressed backspace in empty value: <div class="|" + BACKSPACE
                        //now the text is: <div class=|"
                        //expected result: remove the second quote
                        doc.remove(dotPos - 1, 1);
                    }
                }
            }

        }
    }

    @Override
    public void afterRemove(Context context) throws BadLocationException {
        //no-op
    }

    @Override
    public void cancelled(Context context) {
        //no-op
    }

    @MimeRegistration(mimeType = "text/html", service = DeletedTextInterceptor.Factory.class)  //NOI18N
    public static final class Factory implements DeletedTextInterceptor.Factory {

        @Override
        public DeletedTextInterceptor createDeletedTextInterceptor(MimePath mimePath) {
            return new HtmlDeletedTextInterceptor();
        }

    }

}
