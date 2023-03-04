/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javascript2.editor.hints;

import java.util.Arrays;
import java.util.Collection;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
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

    private static final String GLOBAL_DIRECTIVE = "global"; //NOI18N

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
