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

package org.netbeans.modules.groovy.editor.typinghooks;

import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor;

/**
 *
 * @author Martin Janicek
 */
public class GroovyDeletedTextInterceptor implements DeletedTextInterceptor {

    @MimeRegistration(mimeType = GroovyTokenId.GROOVY_MIME_TYPE, service = DeletedTextInterceptor.Factory.class)
    public static class GroovyDeletedTextInterceptorFactory implements DeletedTextInterceptor.Factory {

        @Override
        public DeletedTextInterceptor createDeletedTextInterceptor(MimePath mimePath) {
            return new GroovyDeletedTextInterceptor();
        }
    }

    @Override
    public boolean beforeRemove(Context context) throws BadLocationException {
        return false;
    }

    @Override
    public void remove(Context context) throws BadLocationException {
        final BaseDocument doc = (BaseDocument) context.getDocument();
        final char ch = context.getText().charAt(0);
        final int dotPos = context.getOffset() - 1;

        switch (ch) {

            case '{':
            case '(':
            case '[': { // and '{' via fallthrough
                char tokenAtDot = LexUtilities.getTokenChar(doc, dotPos);

                if (((tokenAtDot == ']')
                        && (LexUtilities.getTokenBalance(doc, GroovyTokenId.LBRACKET, GroovyTokenId.RBRACKET, dotPos) != 0))
                        || ((tokenAtDot == ')')
                        && (LexUtilities.getTokenBalance(doc, GroovyTokenId.LPAREN, GroovyTokenId.RPAREN, dotPos) != 0))
                        || ((tokenAtDot == '}')
                        && (LexUtilities.getTokenBalance(doc, GroovyTokenId.LBRACE, GroovyTokenId.RBRACE, dotPos) != 0))) {
                    doc.remove(dotPos, 1);
                }
                break;
            }

            case '|':
            case '\"':
            case '\'':
                char[] match = doc.getChars(dotPos, 1);

                if ((match != null) && (match[0] == ch)) {
                    doc.remove(dotPos, 1);
                }
        }
    }

    @Override
    public void afterRemove(Context context) throws BadLocationException {
    }

    @Override
    public void cancelled(Context context) {
    }
}
