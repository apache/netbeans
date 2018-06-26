/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.hints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author Petr Pisl
 */
public class JSHintSupport {

    private static String GLOBAL_DIRECTIVE = "global"; //NOI18N

    public static void addGlobalInline(Snapshot snapshot, int offset, String name) throws BadLocationException {
        Document document = snapshot.getSource().getDocument(false);
        if (document != null) {
            Collection<Identifier> definedGlobal = ModelUtils.getDefinedGlobal(snapshot, 0);
            Identifier lastOne = null;
            for (Identifier iden : definedGlobal) {
                if (lastOne == null || lastOne.getOffsetRange().getEnd() < iden.getOffsetRange().getEnd()) {
                    lastOne = iden;
                }
            }
            int insertWhere = -1;
            String insertText = null;
            if (lastOne != null) {
                insertWhere = lastOne.getOffsetRange().getEnd();
                insertText = ", " + name; //NOI18N
            } else {
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(snapshot, offset);
                if (ts != null) {
                    ts.move(0);
                    if (ts.moveNext()) {
                        Token<? extends JsTokenId> token = LexUtilities.findNext(ts, Arrays.asList(JsTokenId.BLOCK_COMMENT, JsTokenId.WHITESPACE, JsTokenId.EOL));
                        if (token != null) {
                            insertWhere = ts.offset();
                            StringBuilder sb = new StringBuilder();
                            sb.append("/* ").append(GLOBAL_DIRECTIVE).append(" ").append(name).append(" */\n\n");
                            insertText = sb.toString();
                        }
                    }
                }
            }
            if (insertWhere > -1) {
                document.insertString(insertWhere, insertText, null);
            }
        }
    }
}
