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
package org.netbeans.modules.lsp.client.bindings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.netbeans.modules.lsp.client.Utils;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author lahvac
 */
@MimeRegistration(mimeType="", service=CodeGenerator.Factory.class)
public class CodeActions implements CodeGenerator.Factory {

    @Override
    public List<? extends CodeGenerator> create(Lookup context) {
        JTextComponent component = context.lookup(JTextComponent.class);
        if (component == null) {
            return Collections.emptyList();
        }
        FileObject file = NbEditorUtilities.getFileObject(component.getDocument());
        if (file == null) {
            return Collections.emptyList();
        }

        List<LSPBindings> servers = LSPBindings.getBindings(file);
        List<CodeGenerator> output = new ArrayList<>();
        String uri = Utils.toURI(file);

        Utils.handleBindings(servers,
                             capa -> Utils.isEnabled(capa.getCodeActionProvider()),
                             () -> new CodeActionParams(new TextDocumentIdentifier(uri),
                                        new Range(Utils.createPosition(component.getDocument(), component.getSelectionStart()),
                                                Utils.createPosition(component.getDocument(), component.getSelectionEnd())),
                                        new CodeActionContext(Collections.emptyList())),
                             (server, params) -> server.getTextDocumentService().codeAction(params),
                             (server, result) -> result.forEach(cmd -> output.add(new CodeGenerator() {
                                @Override
                                public String getDisplayName() {
                                    return cmd.isLeft() ? cmd.getLeft().getTitle() : cmd.getRight().getTitle();
                                }

                                @Override
                                public void invoke() {
                                    Utils.applyCodeAction(server, cmd);
                                }
                            })));

        return output;
    }
    
}
