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

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.netbeans.api.lsp.LazyCodeAction;
import org.netbeans.api.lsp.WorkspaceEdit;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.spi.lsp.CodeActionProvider;
import org.netbeans.spi.lsp.CommandProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=CodeActionsProvider.class, position = 5)
public class CodeActionsProvider2LspApiBridge extends CodeActionsProvider {

    private static final String URL = "url";// NOI18N
    private static final String INDEX = "index";// NOI18N
    private List<LazyCodeAction> lastCodeActions = null;

    @Override
    public Set<String> getCommands() {
        Set<String> allCommands = new HashSet<>();
        for (CommandProvider cmdProvider : Lookup.getDefault().lookupAll(CommandProvider.class)) {
            allCommands.addAll(cmdProvider.getCommands());
        }
        return allCommands;
    }

    @Override
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        for (CommandProvider cmdProvider : Lookup.getDefault().lookupAll(CommandProvider.class)) {
            if (cmdProvider.getCommands().contains(command)) {
                return cmdProvider.runCommand(command, arguments).thenApply(ret -> {
                    if (ret instanceof WorkspaceEdit) {
                        return Utils.workspaceEditFromApi((WorkspaceEdit) ret, null, client);
                    }
                    return ret;
                });
            }
        }
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public List<CodeAction> getCodeActions(NbCodeLanguageClient client, ResultIterator resultIterator, CodeActionParams params) throws Exception {
        lastCodeActions = new ArrayList<>();
        List<CodeAction> allActions = new ArrayList<>();
        Document doc = resultIterator.getSnapshot().getSource().getDocument(false);
        if (doc instanceof StyledDocument) {
            String uri = params.getTextDocument().getUri();
            int startOffset = Utils.getOffset((StyledDocument) doc, params.getRange().getStart());
            int endOffset = Utils.getOffset((StyledDocument) doc, params.getRange().getEnd());
            org.netbeans.api.lsp.Range r = new org.netbeans.api.lsp.Range(startOffset, endOffset);
            List<String> only = params.getContext().getOnly();
            Lookup l = only != null ? Lookups.fixed(client, resultIterator, only) : Lookups.fixed(client, resultIterator);
            for (CodeActionProvider caProvider : Lookup.getDefault().lookupAll(CodeActionProvider.class)) {
                try {
                    for (org.netbeans.api.lsp.CodeAction ca : caProvider.getCodeActions(doc, r, l)) {
                        Object data = null;
                        String command = ca.getCommand() != null ? ca.getCommand().getCommand() : null;
                        WorkspaceEdit edit = null;
                        if (ca instanceof LazyCodeAction && ((LazyCodeAction) ca).getLazyEdit() != null) {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put(URL, uri);
                            map.put(INDEX, lastCodeActions.size());
                            lastCodeActions.add((LazyCodeAction) ca);
                            data = map;
                        } else if (ca.getEdit() != null) {
                            edit = ca.getEdit();
                        }
                        CodeAction codeAction = createCodeAction(client, ca.getTitle(), ca.getKind(), data, command, command != null ? ca.getCommand().getArguments() : null);
                        if (edit != null) {
                            codeAction.setEdit(Utils.workspaceEditFromApi(edit, uri, client));
                        }
                        allActions.add(codeAction);
                    }
                } catch (Exception ex) {
                    client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
                }
            }
        }
        return allActions;
    }

    @Override
    public CompletableFuture<CodeAction> resolve(NbCodeLanguageClient client, CodeAction codeAction, Object data) {
        if (data instanceof JsonObject) {
            JsonObject obj = (JsonObject) data;
            if (obj.has(URL) && obj.has(INDEX)) {
                LazyCodeAction inputAction = lastCodeActions.get(obj.getAsJsonPrimitive(INDEX).getAsInt());
                if (inputAction != null) {
                    codeAction.setEdit(Utils.workspaceEditFromApi(inputAction.getLazyEdit().get(), obj.getAsJsonPrimitive(URL).getAsString(), client));
                }
            }
        }
       return CompletableFuture.completedFuture(codeAction);
    }
}
