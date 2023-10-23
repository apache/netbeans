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

import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.java.hints.OrganizeImports;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = CodeActionsProvider.class, position = 90)
public final class OrganizeImportsCodeAction extends CodeActionsProvider {

    @Override
    @NbBundle.Messages({
        "DN_OrganizeImports=Organize Imports",
    })
    public List<CodeAction> getCodeActions(NbCodeLanguageClient client, ResultIterator resultIterator, CodeActionParams params) throws Exception {
        List<String> only = params.getContext().getOnly();
        if (only == null || !only.contains(CodeActionKind.Source) && !only.contains(CodeActionKind.SourceOrganizeImports)) {
            return Collections.emptyList();
        }
        CompilationController info = resultIterator.getParserResult() != null ? CompilationController.get(resultIterator.getParserResult()) : null;
        if (info == null) {
            return Collections.emptyList();
        }
        String uri = Utils.toUri(info.getFileObject());
        return Collections.singletonList(createCodeAction(client, Bundle.DN_OrganizeImports(), CodeActionKind.SourceOrganizeImports, uri, null));
    }

    @Override
    public CompletableFuture<CodeAction> resolve(NbCodeLanguageClient client, CodeAction codeAction, Object data) {
        CompletableFuture<CodeAction> future = new CompletableFuture<>();
        try {
            String uri = ((JsonPrimitive) data).getAsString();
            FileObject file = Utils.fromUri(uri);
            JavaSource js = JavaSource.forFileObject(file);
            if (js == null) {
                throw new IOException("Cannot get JavaSource for: " + uri);
            }
            List<TextEdit> edits = TextDocumentServiceImpl.modify2TextEdits(js, wc -> {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                OrganizeImports.doOrganizeImports(wc, null, false);
            });
            if (!edits.isEmpty()) {
                codeAction.setEdit(new WorkspaceEdit(Collections.singletonMap(uri, edits)));
            }
            future.complete(codeAction);
        } catch (IOException | IllegalArgumentException ex) {
            future.completeExceptionally(ex);
        }
        return future;
    }
}
