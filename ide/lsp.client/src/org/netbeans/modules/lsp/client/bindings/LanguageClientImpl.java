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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionOptions;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ProgressParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkDoneProgressBegin;
import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.WorkDoneProgressEnd;
import org.eclipse.lsp4j.WorkDoneProgressNotification;
import org.eclipse.lsp4j.WorkDoneProgressReport;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.netbeans.api.progress.*;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.netbeans.modules.lsp.client.Utils;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.QuickPick.Item;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author lahvac
 */
public class LanguageClientImpl implements LanguageClient, Endpoint {

    private static final Logger LOG = Logger.getLogger(LanguageClientImpl.class.getName());
    private static final RequestProcessor WORKER = new RequestProcessor(LanguageClientImpl.class.getName(), 1, false, false);

    private LSPBindings bindings;
    private boolean allowCodeActions;

    public void setBindings(LSPBindings bindings) {
        this.bindings = bindings;
        ServerCapabilities serverCapabilities = bindings.getInitResult().getCapabilities();
        Either<Boolean, CodeActionOptions> codeActions = serverCapabilities.getCodeActionProvider();
        allowCodeActions = codeActions != null && (!codeActions.isLeft() || codeActions.getLeft());
    }

    @Override
    public void telemetryEvent(Object arg0) {
        System.err.println("telemetry: " + arg0);
    }

    @Override
    public CompletableFuture<Void> createProgress(WorkDoneProgressCreateParams params) {
        return CompletableFuture.completedFuture(null);
    }

    private final Map<Object, ProgressHandle> key2Progress = new HashMap<>();

    @Override
    public void notifyProgress(ProgressParams params) {
        Either<WorkDoneProgressNotification, Object> value = params.getValue();
        if (value.isRight()) {
            return ;
        }
        WorkDoneProgressNotification n = value.getLeft();
        SwingUtilities.invokeLater(() -> {
            switch (n.getKind()) {
                case begin: {
                    WorkDoneProgressBegin progress = (WorkDoneProgressBegin) n;
                    ProgressHandle handle = ProgressHandle.createHandle(progress.getTitle());
                    key2Progress.put(params.getToken().get(), handle);
                    handle.start();
                    handle.progress(progress.getMessage());
                    break;
                }
                case report: {
                    WorkDoneProgressReport progress = (WorkDoneProgressReport) n;
                    ProgressHandle handle = key2Progress.get(params.getToken().get());
                    if (progress.getPercentage() != null) {
                        handle.switchToDeterminate(100);
                        handle.progress(progress.getPercentage());
                    } else {
                        handle.switchToIndeterminate();
                    }
                    if (progress.getMessage() != null) {
                        handle.progress(progress.getMessage());
                    }
                    break;
                }
                case end: {
                    WorkDoneProgressEnd progress = (WorkDoneProgressEnd) n;
                    ProgressHandle handle = key2Progress.get(params.getToken().get());
                    handle.finish();
                    break;
                }
            }
        });
    }

    @Override
    public void publishDiagnostics(PublishDiagnosticsParams pdp) {
        try {
            FileObject file = URLMapper.findFileObject(new URI(pdp.getUri()).toURL());
            EditorCookie ec = file != null ? file.getLookup().lookup(EditorCookie.class) : null;
            Document doc = ec != null ? ec.getDocument() : null;
            if (doc == null) {
                return ; //ignore...
            }
            assert file != null;
            List<ErrorDescription> diags = pdp.getDiagnostics().stream().map(d -> {
                LazyFixList fixList = allowCodeActions ? new DiagnosticFixList(pdp.getUri(), d) : ErrorDescriptionFactory.lazyListForFixes(Collections.emptyList());
                return ErrorDescriptionFactory.createErrorDescription(severityMap.get(d.getSeverity()), d.getMessage(), fixList, file, Utils.getOffset(doc, d.getRange().getStart()), Utils.getOffset(doc, d.getRange().getEnd()));
            }).collect(Collectors.toList());
            HintsController.setErrors(doc, LanguageClientImpl.class.getName(), diags);
        } catch (URISyntaxException | MalformedURLException ex) {
            LOG.log(Level.FINE, null, ex);
        }
    }

    private static final Map<DiagnosticSeverity, Severity> severityMap = new EnumMap<>(DiagnosticSeverity.class);
    
    static {
        severityMap.put(DiagnosticSeverity.Error, Severity.ERROR);
        severityMap.put(DiagnosticSeverity.Hint, Severity.HINT);
        severityMap.put(DiagnosticSeverity.Information, Severity.HINT);
        severityMap.put(DiagnosticSeverity.Warning, Severity.WARNING);
    }

    @Override
    public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
        Utils.applyWorkspaceEdit(params.getEdit());
        return CompletableFuture.completedFuture(new ApplyWorkspaceEditResponse(true));
    }

    @Override
    public void showMessage(MessageParams arg0) {
        int messageType;

        switch (Optional.ofNullable(arg0.getType()).orElse(MessageType.Log)) {
            default:
            case Log:
            case Info:
                messageType = NotifyDescriptor.INFORMATION_MESSAGE;
                break;
            case Warning:
                messageType = NotifyDescriptor.WARNING_MESSAGE;
                break;
            case Error:
                messageType = NotifyDescriptor.ERROR_MESSAGE;
                break;
        }

        NotifyDescriptor nd = new NotifyDescriptor.Message(
                arg0.getMessage(),
                messageType
        );

        DialogDisplayer.getDefault().notifyLater(nd);
    }

    @Override
    public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
        int messageType;

        switch (Optional.ofNullable(arg0.getType()).orElse(MessageType.Log)) {
            default:
            case Log:
            case Info:
                messageType = NotifyDescriptor.INFORMATION_MESSAGE;
                break;
            case Warning:
                messageType = NotifyDescriptor.WARNING_MESSAGE;
                break;
            case Error:
                messageType = NotifyDescriptor.ERROR_MESSAGE;
                break;
        }

        NotifyDescriptor.QuickPick nd = new NotifyDescriptor.QuickPick(
                arg0.getMessage(),
                "Please select",
                arg0.getActions().stream()
                        .map(mai -> new Item(mai.getTitle(), mai.getTitle()))
                        .collect(Collectors.toList())
                ,
                false
        );

        nd.setMessageType(messageType);

        return DialogDisplayer.getDefault()
                .notifyFuture(nd)
                .thenApply(nd2 -> {
                    return new MessageActionItem(
                            nd2.getItems().stream()
                                    .filter(i -> i.isSelected())
                                    .findFirst()
                                    .map(i -> i.getLabel())
                                    .orElse(""));
                });
    }

    @Override
    public void logMessage(MessageParams arg0) {
        switch (Optional.ofNullable(arg0.getType()).orElse(MessageType.Log)) {
            case Log:
            case Info:
                LOG.info(arg0.getMessage());
                break;
            case Warning:
                LOG.warning(arg0.getMessage());
                break;
            case Error:
                LOG.severe(arg0.getMessage());
                break;
        }
    }

    @Override
    public CompletableFuture<List<Object>> configuration(ConfigurationParams configurationParams) {
        CompletableFuture<List<Object>> result = new CompletableFuture<>();
        WORKER.post(() -> {
            List<Object> outcome = new ArrayList<>();
            for (ConfigurationItem ci : configurationParams.getItems()) {
                outcome.add(null);
            }
            result.complete(outcome);
        });
        return result;
    }

    @Override
    public CompletableFuture<?> request(String method, Object parameter) {
        LOG.log(Level.WARNING, "Received unhandled request: {0}: {1}", new Object[] {method, parameter});
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void notify(String method, Object parameter) {
        LOG.log(Level.WARNING, "Received unhandled notification: {0}: {1}", new Object[] {method, parameter});
    }

    private final class DiagnosticFixList implements LazyFixList {

        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private final String fileUri;
        private final Diagnostic diagnostic;
        private List<Fix> fixes;
        private boolean computing;
        private boolean computed;

        public DiagnosticFixList(String fileUri, Diagnostic diagnostic) {
            this.fileUri = fileUri;
            this.diagnostic = diagnostic;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }

        @Override
        public boolean probablyContainsFixes() {
            return true;
        }

        @Override
        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public synchronized List<Fix> getFixes() {
            if (!computing && !computed) {
                computing = true;
                bindings.runOnBackground(() -> {
                    try {
                        List<Either<Command, CodeAction>> commands =
                                bindings.getTextDocumentService().codeAction(new CodeActionParams(new TextDocumentIdentifier(fileUri),
                                        diagnostic.getRange(),
                                        new CodeActionContext(Collections.singletonList(diagnostic)))).get();

                        List<Fix> newFixes = Collections.emptyList();

                        if (commands != null) {
                            newFixes = commands.stream()
                                    .map(cmd -> new CommandBasedFix(cmd))
                                    .collect(Collectors.toList());
                        }

                        synchronized (this) {
                            this.fixes = Collections.unmodifiableList(newFixes);
                            this.computed = true;
                            this.computing = false;
                        }
                        pcs.firePropertyChange(PROP_COMPUTED, null, null);
                        pcs.firePropertyChange(PROP_FIXES, null, null);
                    } catch (InterruptedException | ExecutionException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                });
            }
            return fixes;
        }

        @Override
        public synchronized boolean isComputed() {
            return computed;
        }

        private class CommandBasedFix implements Fix {

            private final Either<Command, CodeAction> cmd;

            public CommandBasedFix(Either<Command, CodeAction> cmd) {
                this.cmd = cmd;
            }

            @Override
            public String getText() {
                return cmd.isLeft() ? cmd.getLeft().getTitle() : cmd.getRight().getTitle();
            }

            @Override
            public ChangeInfo implement() throws Exception {
                Utils.applyCodeAction(bindings, cmd);
                return null;
            }
        }
        
    }
}
