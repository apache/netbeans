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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.ModelOperation;
import net.java.html.json.Property;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.netbeans.api.htmlui.HTMLDialog;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.java.editor.imports.JavaFixAllImports;
import org.netbeans.modules.java.editor.imports.JavaFixAllImports.CandidateDescription;
import org.netbeans.modules.java.editor.imports.JavaFixAllImports.ImportData;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author shimadan
 */
@ServiceProvider(service = CodeActionsProvider.class, position = 91)
public class FixImportsCodeAction extends CodeActionsProvider {
    
    private static final String FIX_IMPORTS_KIND = "source.fixImports";
    @Override
    @NbBundle.Messages({
        "DN_FixImports=Fix Imports...",})
    public List<CodeAction> getCodeActions(NbCodeLanguageClient client, ResultIterator resultIterator, CodeActionParams params) throws Exception {
        List<String> only = params.getContext().getOnly();
        if (only == null || !only.contains(CodeActionKind.Source)) {
            return Collections.emptyList();
        }
        CompilationController info = resultIterator.getParserResult() != null ? CompilationController.get(resultIterator.getParserResult()) : null;
        if (info == null) {
            return Collections.emptyList();
        }
        String uri = Utils.toUri(info.getFileObject());
        return Collections.singletonList(createCodeAction(client, Bundle.DN_FixImports(), FIX_IMPORTS_KIND, uri, null));
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
            final AtomicReference<ImportData> missingImports = new AtomicReference<ImportData>();
            js.runUserActionTask(cc -> {
                cc.toPhase(JavaSource.Phase.RESOLVED);
                missingImports.set(JavaFixAllImports.computeImports(cc));
            }, true);
            future = showFixImportsDialog(missingImports.get()).thenApply(selections -> {
                List<TextEdit> edits;
                try {
                    edits = TextDocumentServiceImpl.modify2TextEdits(js, wc -> {
                        wc.toPhase(JavaSource.Phase.RESOLVED);
                        JavaFixAllImports.performFixImports(wc, missingImports.get(), selections, false);

                    });
                    if (!edits.isEmpty()) {
                        codeAction.setEdit(new WorkspaceEdit(Collections.singletonMap(uri, edits)));
                    }
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
                return codeAction;
            });
        } catch (IOException | IllegalArgumentException ex) {
            future.completeExceptionally(ex);
        }
        return future;
    }

    private CompletableFuture<CandidateDescription[]> showFixImportsDialog(ImportData Impdata) {
        CompletableFuture<CandidateDescription[]> selections = new CompletableFuture<>();
        Pages.showFixImportsDialog(Impdata, selections);
        return selections;
    }

    @HTMLDialog(url = "ui/FixImports.html", resources = "FixImports.css")
    static final HTMLDialog.OnSubmit showFixImportsDialog(ImportData missingImports, CompletableFuture<CandidateDescription[]> selectedCandidates) {
        FixImportsUI model = new FixImportsUI();
        ImportDataUI[] imports = IntStream.range(0, missingImports.simpleNames.length)
            .mapToObj(i -> new ImportDataUI(
                missingImports.simpleNames[i],
                missingImports.defaults[i].displayName,
                Stream.of(missingImports.variants[i]).map((candidate)->candidate.displayName).toArray(String[]::new)
            ))
            .toArray(ImportDataUI[]::new);
        model.withImports(imports).assignData(missingImports, selectedCandidates);
        model.applyBindings();
        return (id) -> {
            if ("accept".equals(id)) {
                model.completeSelectedCandidates();
            }else{
                model.cancel();
            }
            return true;
        };
    }

    @Model(className = "FixImportsUI", targetId = "", instance = true, builder = "with",
            properties = {
                @Property(name = "imports", type = ImportDataUI.class, array = true)
            })
    static final class FixImportsControl {

        private CompletableFuture<CandidateDescription[]> selectedCandidates;
        private ImportData missingImports;

        @ModelOperation
        void assignData(FixImportsUI ui, ImportData missingImports, CompletableFuture<CandidateDescription[]> selectedCandidates) {
            this.selectedCandidates = selectedCandidates;
            this.missingImports = missingImports;
        }

        @ModelOperation
        @Function
        void completeSelectedCandidates(FixImportsUI ui) {
            List<ImportDataUI> imports = ui.getImports();
            CandidateDescription[] chosen = IntStream.range(0, imports.size())
                .mapToObj(i -> Stream.of(missingImports.variants[i])
                    .filter((variant) -> variant.displayName.equals(imports.get(i).getSelectedCandidateFQN()))
                    .findFirst()
                    .get()
                )
                .toArray(CandidateDescription[]::new);
            selectedCandidates.complete(chosen);
        }
        @ModelOperation
        @Function
        void cancel(){
            selectedCandidates.cancel(true);
        }
    }

    @Model(className = "ImportDataUI", instance = true, properties = {
        @Property(name = "simpleName", type = String.class),
        @Property(name = "selectedCandidateFQN", type = String.class),
        @Property(name = "candidatesFQN", type = String.class, array = true)
    })
    static final class ImportDataControl {
        
    }
    
}
