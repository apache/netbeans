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
package org.netbeans.modules.java.lsp.server.refactoring;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.CreateFile;
import org.eclipse.lsp4j.DeleteFile;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.RenameFile;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.protocol.CodeActionsProvider;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.impl.APIAccessor;
import org.netbeans.modules.refactoring.api.impl.SPIAccessor;
import org.netbeans.modules.refactoring.java.plugins.JavaPluginUtils;
import org.netbeans.modules.refactoring.java.spi.hooks.JavaModificationResult;
import org.netbeans.modules.refactoring.plugins.FileMovePlugin;
import org.netbeans.modules.refactoring.spi.RefactoringCommit;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public abstract class CodeRefactoring extends CodeActionsProvider {

    private static WorkspaceEdit perform(AbstractRefactoring refactoring, RefactoringSession session) throws Exception {
        List<Either<TextDocumentEdit, ResourceOperation>> resultChanges = new ArrayList<>();
        Map<String, String> renames = new HashMap<>();
        List<RefactoringElementImplementation> fileChanges = APIAccessor.DEFAULT.getFileChanges(session);
        for (RefactoringElementImplementation rei : fileChanges) {
            if (rei instanceof FileMovePlugin.MoveFile) {
                String oldURI = Utils.toUri(rei.getParentFile());
                int slash = oldURI.lastIndexOf('/');
                URL url = ((org.netbeans.modules.refactoring.api.MoveRefactoring)refactoring).getTarget().lookup(URL.class);
                String newURI = url.toString() + oldURI.substring(slash + 1);
                renames.put(oldURI, newURI);
                ResourceOperation op = new RenameFile(oldURI, newURI);
                resultChanges.add(Either.forRight(op));
            } else if (rei instanceof org.netbeans.modules.refactoring.java.plugins.DeleteFile) {
                String oldURI = Utils.toUri(rei.getParentFile());
                ResourceOperation op = new DeleteFile(oldURI);
                resultChanges.add(Either.forRight(op));
            } else {
                throw new IllegalStateException(rei.getClass().toString());
            }
        }
        List<Transaction> transactions = APIAccessor.DEFAULT.getCommits(session);
        List<ModificationResult> results = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t instanceof RefactoringCommit) {
                RefactoringCommit c = (RefactoringCommit) t;
                for (org.netbeans.modules.refactoring.spi.ModificationResult refResult : SPIAccessor.DEFAULT.getTransactions(c)) {
                    if (refResult instanceof JavaModificationResult) {
                        results.add(((JavaModificationResult) refResult).delegate);
                    } else {
                        throw new IllegalStateException(refResult.getClass().toString());
                    }
                }
            } else {
                throw new IllegalStateException(t.getClass().toString());
            }
        }
        for (ModificationResult mr : results) {
            Set<File> newFiles = mr.getNewFiles();
            if (newFiles.size() > 1) {
                throw new IllegalStateException();
            }
            String newFilePath = null;
            for (File newFile : newFiles) {
                newFilePath = newFile.toURI().toString();
                resultChanges.add(Either.forRight(new CreateFile(newFilePath)));
            }
            for (FileObject modified : mr.getModifiedFileObjects()) {
                String modifiedUri = Utils.toUri(modified);
                List<TextEdit> edits = new ArrayList<>();
                for (ModificationResult.Difference diff : mr.getDifferences(modified)) {
                    String newText = diff.getNewText();
                    if (diff.getKind() == ModificationResult.Difference.Kind.CREATE) {
                        Position pos = new Position(0, 0);
                        resultChanges.add(Either.forLeft(new TextDocumentEdit(new VersionedTextDocumentIdentifier(newFilePath, -1), Collections.singletonList(new TextEdit(new Range(pos, pos), newText != null ? newText : "")))));
                    } else {
                        edits.add(new TextEdit(new Range(Utils.createPosition(modified, diff.getStartPosition().getOffset()), Utils.createPosition(modified, diff.getEndPosition().getOffset())), newText != null ? newText : ""));
                    }
                }
                resultChanges.add(Either.forLeft(new TextDocumentEdit(new VersionedTextDocumentIdentifier(renames.getOrDefault(modifiedUri, modifiedUri), -1), edits)));
            }
        }
        session.finished();
        return new WorkspaceEdit(resultChanges);
    }

    private static Problem prepare(AbstractRefactoring refactoring, RefactoringSession session) {
        Problem p = refactoring.checkParameters();
        p = JavaPluginUtils.chainProblems(p, refactoring.preCheck());
        p = JavaPluginUtils.chainProblems(p, refactoring.prepare(session));
        return p;
    }

    @NbBundle.Messages({
        "# {0} - problems message",
        "PROMPT_AskProceedWithRefactoringProblems=Refactoring may lead to following problems: {0}.\nDo you still want to proceed with the refactoring?",
        "PROMPT_AskProceedWithRefactoringProblems_Yes=Yes",
        "PROMPT_AskProceedWithRefactoringProblems_No=No",
    })
    private static ShowMessageRequestParams warningsMessageParams(
            Problem p) {
        final MessageActionItem yes = new MessageActionItem(Bundle.PROMPT_AskProceedWithRefactoringProblems_Yes());
        final MessageActionItem no = new MessageActionItem(Bundle.PROMPT_AskProceedWithRefactoringProblems_No());
        ShowMessageRequestParams smrp = new ShowMessageRequestParams(Arrays.asList(yes, no));
        StringBuilder msgs = new StringBuilder();
        while (p != null) {
            msgs.append(p.getMessage());
            msgs.append('\n');
            p = p.getNext();
        }
        smrp.setMessage(Bundle.PROMPT_AskProceedWithRefactoringProblems(msgs.toString()));
        smrp.setType(MessageType.Warning);
        return smrp;
    }

    private static void showRefactoringWarnings(NbCodeLanguageClient client,AbstractRefactoring refactoring,RefactoringSession session,Problem p) {
        ShowMessageRequestParams smrp = warningsMessageParams(p);
        client.showMessageRequest(smrp).thenAccept(ai -> {
            if (ai.getTitle().equalsIgnoreCase(Bundle.PROMPT_AskProceedWithRefactoringProblems_Yes())) {
                try {
                    client.applyEdit(new ApplyWorkspaceEditParams(perform(refactoring, session)));
                } catch (Exception ex) {
                    if (client == null) {
                        Exceptions.printStackTrace(Exceptions.attachSeverity(ex, Level.SEVERE));
                    } else {
                        client.showMessage(new MessageParams(MessageType.Error, ex.getLocalizedMessage()));
                    }
                }
            }
        });
    }

    protected static void sendRefactoringChanges(NbCodeLanguageClient client, AbstractRefactoring refactoring, String name) throws Exception {
        RefactoringSession session = RefactoringSession.create(name);
        Problem p = prepare(refactoring, session);
        if (p == null) {
            client.applyEdit(new ApplyWorkspaceEditParams(perform(refactoring, session)));
        } else if (p.isFatal()) {
            throw new IllegalStateException(p.getMessage());
        } else {
            showRefactoringWarnings(client, refactoring, session, p);
        }
    }
}
