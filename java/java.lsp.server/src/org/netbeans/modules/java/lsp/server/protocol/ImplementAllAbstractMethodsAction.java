/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.lsp.server.protocol;

import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = CodeActionsProvider.class, position = 200)
public final class ImplementAllAbstractMethodsAction extends CodeActionsProvider {

    private static final String IMPLEMENT_ALL_ABSTRACT_METHODS = "nbls.java.implement.all.abstract.methods"; //NOI18N
    private final Gson gson = new Gson();

    @Override
    public List<CodeAction> getCodeActions(NbCodeLanguageClient client, ResultIterator resultIterator, CodeActionParams params) throws Exception {
        return Collections.emptyList();
    }

    @Override
    public Set<String> getCommands() {
        return Collections.singleton(IMPLEMENT_ALL_ABSTRACT_METHODS);
    }

    @Override
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        try {
            if (arguments.size() >= 2) {
                String uri = ((JsonPrimitive) arguments.get(0)).getAsString();
                FileObject file = Utils.fromUri(uri);
                JavaSource js = JavaSource.forFileObject(file);
                if (js == null) {
                    throw new IOException("Cannot get JavaSource for: " + uri);
                }
                Position position = gson.fromJson(gson.toJson(arguments.get(1)), Position.class);
                List<TextEdit> edits = TextDocumentServiceImpl.modify2TextEdits(js, wc -> {
                    wc.toPhase(JavaSource.Phase.RESOLVED);
                  Document doc = wc.getSnapshot().getSource().getDocument(true);
                    if (doc instanceof StyledDocument) {
                        int offset = Utils.getOffset((StyledDocument) doc, position);
                        GeneratorUtils.generateAllAbstractMethodImplementations(wc, wc.getTreeUtilities().pathFor(offset));
                    }
                });
                client.applyEdit(new ApplyWorkspaceEditParams(new WorkspaceEdit(Collections.singletonMap(uri, edits))));
                future.complete(true);
            } else {
                throw new IllegalArgumentException(String.format("Illegal number of arguments received for command: %s", command));
            }
        } catch (IOException | IllegalArgumentException ex) {
            future.completeExceptionally(ex);
        }
        return future;
    }
}
