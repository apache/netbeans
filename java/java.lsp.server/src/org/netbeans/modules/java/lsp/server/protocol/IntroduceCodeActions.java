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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.StyledDocument;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.java.hints.introduce.IntroduceFixBase;
import org.netbeans.modules.java.hints.introduce.IntroduceHint;
import org.netbeans.modules.java.hints.introduce.IntroduceKind;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = CodeActionsProvider.class)
public final class IntroduceCodeActions extends CodeActionsProvider {

    private static final Set<String> SUPPORTED_CODE_ACTION_KINDS =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(CodeActionKind.RefactorExtract)));

    public IntroduceCodeActions() {
    }

    @Override
    public Set<String> getSupportedCodeActionKinds() {
        return SUPPORTED_CODE_ACTION_KINDS;
    }

    @Override
    public List<CodeAction> getCodeActions(NbCodeLanguageClient client, ResultIterator resultIterator, CodeActionParams params) throws Exception {
        Range range = params.getRange();
        List<CodeAction> result = new ArrayList<>();

        if (client.getNbCodeCapabilities().wantsJavaSupport() && !range.getStart().equals(range.getEnd())) {
            CompilationController cc = resultIterator.getParserResult() != null ? CompilationController.get(resultIterator.getParserResult()) : null;

            if (cc != null) {
                cc.toPhase(JavaSource.Phase.RESOLVED);

                StyledDocument doc = (StyledDocument) cc.getDocument();
                int startOffset = Utils.getOffset(doc, range.getStart());
                int endOffset = Utils.getOffset(doc, range.getEnd());

                for (ErrorDescription err : IntroduceHint.computeError(cc, startOffset, endOffset, new EnumMap<IntroduceKind, Fix>(IntroduceKind.class), new EnumMap<IntroduceKind, String>(IntroduceKind.class), new AtomicBoolean())) {
                    for (Fix fix : err.getFixes().getFixes()) {
                        if (fix instanceof IntroduceFixBase) {
                            try {
                                ModificationResult changes = ((IntroduceFixBase) fix).getModificationResult();
                                if (changes != null) {
                                    List<Either<TextDocumentEdit, ResourceOperation>> documentChanges = new ArrayList<>();
                                    Set<? extends FileObject> fos = changes.getModifiedFileObjects();
                                    if (fos.size() == 1) {
                                        FileObject fileObject = fos.iterator().next();
                                        List<? extends ModificationResult.Difference> diffs = changes.getDifferences(fileObject);
                                        if (diffs != null) {
                                            List<TextEdit> edits = new ArrayList<>();
                                            for (ModificationResult.Difference diff : diffs) {
                                                String newText = diff.getNewText();
                                                edits.add(new TextEdit(new Range(Utils.createPosition(fileObject, diff.getStartPosition().getOffset()),
                                                        Utils.createPosition(fileObject, diff.getEndPosition().getOffset())),
                                                        newText != null ? newText : ""));
                                            }
                                            documentChanges.add(Either.forLeft(new TextDocumentEdit(new VersionedTextDocumentIdentifier(Utils.toUri(fileObject), -1), edits)));
                                        }
                                        CodeAction codeAction = new CodeAction(fix.getText());
                                        codeAction.setKind(CodeActionKind.RefactorExtract);
                                        codeAction.setEdit(new WorkspaceEdit(documentChanges));
                                        int renameOffset = ((IntroduceFixBase) fix).getNameOffset(changes);
                                        if (renameOffset >= 0) {
                                            codeAction.setCommand(new Command("Rename", client.getNbCodeCapabilities().getCommandPrefix() + ".rename.element.at", Collections.singletonList(renameOffset)));
                                        }
                                        result.add(codeAction);
                                    }
                                }
                            } catch (GeneratorUtils.DuplicateMemberException dme) {
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

}
