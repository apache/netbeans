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
package org.netbeans.modules.javascript2.nodejs.editor;

import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.editor.spi.DeclarationFinder;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
@DeclarationFinder.Registration(priority = 16)
public class NodeJsDeclarationFinder implements DeclarationFinder {
    
    
    
    @Override
    public DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        Snapshot snapshot = info.getSnapshot();
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(snapshot, caretOffset);
        Token<? extends JsTokenId> pathToken = getModeluPath(ts, caretOffset);
        if (pathToken != null) {
            String module = pathToken.text().toString();
            FileObject moduleFO = NodeJsUtils.findModuleFile(snapshot.getSource().getFileObject(), module);
            if (moduleFO != null) {
                if (NodeJsDataProvider.getDefault(moduleFO).getRuntimeModules().contains(module)) {
                    ElementHandle eh = new NodeJsElement(moduleFO, module, NodeJsDataProvider.getDefault(moduleFO).getDocForModule(module), ElementKind.FILE);
                    return new DeclarationLocation(moduleFO, 0, eh);
                }
                
                return new DeclarationLocation(moduleFO, 0);
            }
        }
        return DeclarationLocation.NONE;
    }

    @Override
    public OffsetRange getReferenceSpan(final Document doc, final int caretOffset) {
        final OffsetRange[] value = new OffsetRange[1];
        value[0] = OffsetRange.NONE;

        doc.render(new Runnable() {

            @Override
            public void run() {
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, caretOffset);
                Token<? extends JsTokenId> path = getModeluPath(ts, caretOffset);
                if (path != null) {
                    value[0] = new OffsetRange(ts.offset(), ts.offset() + ts.token().length());
                }
            }
        });
        return value[0];
    }

    private Token<? extends JsTokenId> getModeluPath(TokenSequence<? extends JsTokenId> ts, final int offset) {
        Token<? extends JsTokenId> path = null;
        if (ts != null && NodeJsContext.findContext(ts, offset) == NodeJsContext.MODULE_PATH) {
            ts.move(offset);
            ts.moveNext();
            Token<? extends JsTokenId> token = ts.token();
            if (token.id() == JsTokenId.STRING_END) {
                ts.movePrevious();
                token = ts.token();
            }
            if (token.id() == JsTokenId.STRING) {
                String text = token.text().toString();
                if (text != null && !text.isEmpty()) {
                    path = token;
                }
            }
        }
        return path;
    }
}
