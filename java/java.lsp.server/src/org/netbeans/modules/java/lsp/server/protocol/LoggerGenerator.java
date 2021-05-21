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
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.java.lsp.server.Utils;
import org.openide.filesystems.FileObject;
import org.openide.util.BaseUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service = CodeGenerator.class, position = 20)
public final class LoggerGenerator extends CodeGenerator {

    public static final String GENERATE_LOGGER =  "java.generate.logger";

    private final Set<String> commands = Collections.singleton(GENERATE_LOGGER);
    private final Gson gson = new Gson();

    public LoggerGenerator() {
    }

    @Override
    @NbBundle.Messages({
        "DN_GenerateLogger=Generate Logger...",
    })
    public List<CodeAction> getCodeActions(CompilationInfo info, CodeActionParams params) {
        List<String> only = params.getContext().getOnly();
        if (only == null || !only.contains(CodeActionKind.Source)) {
            return Collections.emptyList();
        }
        int offset = getOffset(info, params.getRange().getStart());
        TreePath tp = info.getTreeUtilities().pathFor(offset);
        tp = info.getTreeUtilities().getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, tp);
        if (tp == null) {
            return Collections.emptyList();
        }
        TypeElement typeElement = (TypeElement) info.getTrees().getElement(tp);
        if (typeElement == null || !typeElement.getKind().isClass()) {
            return Collections.emptyList();
        }
        for (VariableElement ve : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            TypeMirror type = ve.asType();
            if (type.getKind() == TypeKind.DECLARED && ((TypeElement)((DeclaredType)type).asElement()).getQualifiedName().contentEquals(Logger.class.getName())
                    || type.getKind() == TypeKind.ERROR && ((TypeElement)((DeclaredType)type).asElement()).getSimpleName().contentEquals(Logger.class.getSimpleName())) {
                return Collections.emptyList();
            }
        }
        String uri = Utils.toUri(info.getFileObject());
        return Collections.singletonList(createCodeAction(Bundle.DN_GenerateLogger(), CODE_GENERATOR_KIND, GENERATE_LOGGER, uri, offset));
    }

    @Override
    public Set<String> getCommands() {
        return commands;
    }

    @Override
    @NbBundle.Messages({
        "DN_SelectLoggerName=Logger field name",
    })
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        if (arguments.size() > 1) {
            String uri = gson.fromJson(gson.toJson(arguments.get(0)), String.class);
            int offset = gson.fromJson(gson.toJson(arguments.get(1)), Integer.class);
            client.showInputBox(new ShowInputBoxParams(Bundle.DN_SelectLoggerName(), "LOG")).thenAccept(value -> {
                if (value != null && BaseUtilities.isJavaIdentifier(value)) {
                    try {
                        FileObject file = Utils.fromUri(uri);
                        JavaSource js = JavaSource.forFileObject(file);
                        if (js == null) {
                            throw new IOException("Cannot get JavaSource for: " + uri);
                        }
                        List<TextEdit> edits = TextDocumentServiceImpl.modify2TextEdits(js, wc -> {
                            wc.toPhase(JavaSource.Phase.RESOLVED);
                            TreePath tp = wc.getTreeUtilities().pathFor(offset);
                            tp = wc.getTreeUtilities().getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, tp);
                            if (tp != null) {
                                ClassTree cls = (ClassTree) tp.getLeaf();
                                VariableTree field = org.netbeans.modules.java.editor.codegen.LoggerGenerator.createLoggerField(wc.getTreeMaker(), cls, value, EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL));
                                wc.rewrite(cls, GeneratorUtilities.get(wc).insertClassMember(cls, field));
                            }
                        });
                        client.applyEdit(new ApplyWorkspaceEditParams(new WorkspaceEdit(Collections.singletonMap(uri, edits))));
                    } catch (IOException | IllegalArgumentException ex) {
                        client.logMessage(new MessageParams(MessageType.Error, ex.getLocalizedMessage()));
                    }
                }
            });
        } else {
            client.logMessage(new MessageParams(MessageType.Error, String.format("Illegal number of arguments received for command: %s", command)));
        }
        return CompletableFuture.completedFuture(true);
    }
}
