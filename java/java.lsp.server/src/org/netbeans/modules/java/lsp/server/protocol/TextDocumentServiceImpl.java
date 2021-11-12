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
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionItemTag;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.CreateFile;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightParams;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.FoldingRange;
import org.eclipse.lsp4j.FoldingRangeKind;
import org.eclipse.lsp4j.FoldingRangeRequestParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.ImplementationParams;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PrepareRenameParams;
import org.eclipse.lsp4j.PrepareRenameResult;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameFile;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.TypeDefinitionParams;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WillSaveTextDocumentParams;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lsp.Completion;
import org.netbeans.api.lsp.HyperlinkLocation;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.editor.java.GoToSupport;
import org.netbeans.modules.editor.java.GoToSupport.GoToTarget;
import org.netbeans.modules.java.editor.base.fold.JavaElementFoldVisitor;
import org.netbeans.modules.java.editor.base.fold.JavaElementFoldVisitor.FoldCreator;
import org.netbeans.modules.java.editor.base.semantic.MarkOccurrencesHighlighterBase;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.java.editor.options.MarkOccurencesSettings;
import org.netbeans.modules.java.editor.overridden.ComputeOverriding;
import org.netbeans.modules.java.editor.overridden.ElementDescription;
import org.netbeans.modules.java.hints.OrganizeImports;
import org.netbeans.modules.java.hints.introduce.IntroduceFixBase;
import org.netbeans.modules.java.hints.introduce.IntroduceHint;
import org.netbeans.modules.java.hints.introduce.IntroduceKind;
import org.netbeans.modules.java.lsp.server.LspServerState;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.debugging.utils.ErrorUtilities;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.indexing.implspi.ActiveDocumentProvider.IndexingAware;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.api.impl.APIAccessor;
import org.netbeans.modules.refactoring.api.impl.SPIAccessor;
import org.netbeans.modules.refactoring.java.api.WhereUsedQueryConstants;
import org.netbeans.modules.refactoring.java.spi.hooks.JavaModificationResult;
import org.netbeans.modules.refactoring.plugins.FileRenamePlugin;
import org.netbeans.modules.refactoring.spi.RefactoringCommit;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.lsp.ErrorProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.text.PositionBounds;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.Union2;
import org.openide.util.WeakSet;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class TextDocumentServiceImpl implements TextDocumentService, LanguageClientAware {
    private static final Logger LOG = Logger.getLogger(TextDocumentServiceImpl.class.getName());
    
    private static final String COMMAND_RUN_SINGLE = "java.run.single";         // NOI18N
    private static final String COMMAND_DEBUG_SINGLE = "java.debug.single";     // NOI18N
    private static final String NETBEANS_JAVADOC_LOAD_TIMEOUT = "netbeans.javadoc.load.timeout";// NOI18N
    private static final String NETBEANS_JAVA_ON_SAVE_ORGANIZE_IMPORTS = "netbeans.java.onSave.organizeImports";// NOI18N
    
    private static final RequestProcessor BACKGROUND_TASKS = new RequestProcessor(TextDocumentServiceImpl.class.getName(), 1, false, false);
    private static final RequestProcessor WORKER = new RequestProcessor(TextDocumentServiceImpl.class.getName(), 1, false, false);

    /**
     * File URIs touched / queried by the client.
     */
    private Map<String, Instant> knownFiles = new HashMap<>();
    
    /**
     * Documents actually opened by the client.
     */
    private final Map<String, RequestProcessor.Task> diagnosticTasks = new HashMap<>();
    private final LspServerState server;
    private NbCodeLanguageClient client;

    TextDocumentServiceImpl(LspServerState server) {
        this.server = server;
        Lookup.getDefault().lookup(RefreshDocument.class).register(this);
    }

    private void reRunDiagnostics() {
        for (String doc : server.getOpenedDocuments().getUris()) {
            runDiagnosticTasks(doc);
        }
    }

    @ServiceProvider(service=IndexingAware.class, position=0)
    public static final class RefreshDocument implements IndexingAware {

        private final Set<TextDocumentServiceImpl> delegates = new WeakSet<>();

        public synchronized void register(TextDocumentServiceImpl delegate) {
            delegates.add(delegate);
        }

        @Override
        public void indexingComplete(Set<URL> indexedRoots) {
            TextDocumentServiceImpl[] delegates;
            synchronized (this) {
                delegates = this.delegates.toArray(new TextDocumentServiceImpl[this.delegates.size()]);
            }
            for (TextDocumentServiceImpl delegate : delegates) {
                delegate.reRunDiagnostics();
            }
        }
    }

    private final AtomicInteger javadocTimeout = new AtomicInteger(-1);
    private List<Completion> lastCompletions = null;

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        lastCompletions = new ArrayList<>();
        AtomicInteger index = new AtomicInteger(0);
        final CompletionList completionList = new CompletionList();
        // shortcut: if the projects are not yet initialized, return empty:
        if (server.openedProjects().getNow(null) == null) {
            return CompletableFuture.completedFuture(Either.forRight(completionList));
        }
        try {
            String uri = params.getTextDocument().getUri();
            FileObject file = fromURI(uri);
            if (file == null) {
                return CompletableFuture.completedFuture(Either.forRight(completionList));
            }
            EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
            Document doc = ec.openDocument();
            if (!(doc instanceof LineDocument)) {
                return CompletableFuture.completedFuture(Either.forRight(completionList));
            }
            ConfigurationItem conf = new ConfigurationItem();
            conf.setScopeUri(uri);
            conf.setSection(NETBEANS_JAVADOC_LOAD_TIMEOUT);
            return client.configuration(new ConfigurationParams(Collections.singletonList(conf))).thenApply(c -> {
                if (c != null && !c.isEmpty()) {
                    javadocTimeout.set(((JsonPrimitive)c.get(0)).getAsInt());
                }
                final int caret = Utils.getOffset((LineDocument) doc, params.getPosition());
                List<CompletionItem> items = new ArrayList<>();
                Completion.Context context = params.getContext() != null
                        ? new Completion.Context(Completion.TriggerKind.valueOf(params.getContext().getTriggerKind().name()),
                                params.getContext().getTriggerCharacter() == null || params.getContext().getTriggerCharacter().isEmpty() ? null : params.getContext().getTriggerCharacter().charAt(0))
                        : null;
                Preferences prefs = CodeStylePreferences.get(doc, "text/x-java").getPreferences();
                String point = prefs.get("classMemberInsertionPoint", null);
                try {
                    prefs.put("classMemberInsertionPoint", CodeStyle.InsertionPoint.CARET_LOCATION.name());
                    boolean isComplete = Completion.collect(doc, caret, context, completion -> {
                        CompletionItem item = new CompletionItem(completion.getLabel());
                        if (completion.getKind() != null) {
                            item.setKind(CompletionItemKind.valueOf(completion.getKind().name()));
                        }
                        if (completion.getTags() != null) {
                            item.setTags(completion.getTags().stream().map(tag -> CompletionItemTag.valueOf(tag.name())).collect(Collectors.toList()));
                        }
                        if (completion.getDetail() != null && completion.getDetail().isDone()) {
                            item.setDetail(completion.getDetail().getNow(null));
                        }
                        if (completion.getDocumentation() != null && completion.getDocumentation().isDone()) {
                            String documentation = completion.getDocumentation().getNow(null);
                            if (documentation != null) {
                                MarkupContent markup = new MarkupContent();
                                markup.setKind("markdown");
                                markup.setValue(html2MD(documentation));
                                item.setDocumentation(markup);
                            }
                        }
                        if (completion.isPreselect()) {
                            item.setPreselect(true);
                        }
                        item.setSortText(completion.getSortText());
                        item.setFilterText(completion.getFilterText());
                        item.setInsertText(completion.getInsertText());
                        if (completion.getInsertTextFormat() != null) {
                            item.setInsertTextFormat(InsertTextFormat.valueOf(completion.getInsertTextFormat().name()));
                        }
                        org.netbeans.api.lsp.TextEdit edit = completion.getTextEdit();
                        if (edit != null) {
                            item.setTextEdit(Either.forLeft(new TextEdit(new Range(Utils.createPosition(file, edit.getStartOffset()), Utils.createPosition(file, edit.getEndOffset())), edit.getNewText())));
                        }
                        if (completion.getAdditionalTextEdits() != null && completion.getAdditionalTextEdits().isDone()) {
                            List<org.netbeans.api.lsp.TextEdit> additionalTextEdits = completion.getAdditionalTextEdits().getNow(null);
                            if (additionalTextEdits != null && !additionalTextEdits.isEmpty()) {
                                item.setAdditionalTextEdits(additionalTextEdits.stream().map(ed -> {
                                    return new TextEdit(new Range(Utils.createPosition(file, ed.getStartOffset()), Utils.createPosition(file, ed.getEndOffset())), ed.getNewText());
                                }).collect(Collectors.toList()));
                            }
                        }
                        if (completion.getCommitCharacters() != null) {
                            item.setCommitCharacters(completion.getCommitCharacters().stream().map(ch -> ch.toString()).collect(Collectors.toList()));
                        }
                        lastCompletions.add(completion);
                        item.setData(new CompletionData(uri, index.getAndIncrement()));
                        items.add(item);
                    });
                    if (!isComplete) {
                        completionList.setIsIncomplete(true);
                    }
                } finally {
                    if (point != null) {
                        prefs.put("classMemberInsertionPoint", point);
                    } else {
                        prefs.remove("classMemberInsertionPoint");
                    }
                }
                completionList.setItems(items);
                return Either.forRight(completionList);
            });
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static final class CompletionData {
        public String uri;
        public int index;

        public CompletionData() {
        }

        public CompletionData(String uri, int index) {
            this.uri = uri;
            this.index = index;
        }

        @Override
        public String toString() {
            return "CompletionData{" + "uri=" + uri + ", index=" + index + '}';
        }
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = (NbCodeLanguageClient)client;
    }

    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem ci) {
        JsonObject rawData = (JsonObject) ci.getData();
        if (rawData != null) {
            CompletionData data = new Gson().fromJson(rawData, CompletionData.class);
            Completion completion = lastCompletions.get(data.index);
            if (completion != null) {
                FileObject file = fromURI(data.uri);
                if (file != null) {
                    CompletableFuture<CompletionItem> result = new CompletableFuture<>();
                    WORKER.post(() -> {
                        if (completion.getDetail() != null) {
                            try {
                                String detail = completion.getDetail().get();
                                if (detail != null) {
                                    ci.setDetail(detail);
                                }
                            } catch (Exception ex) {
                            }
                        }
                        if (completion.getAdditionalTextEdits() != null) {
                            try {
                                List<org.netbeans.api.lsp.TextEdit> additionalTextEdits = completion.getAdditionalTextEdits().get();
                                if (additionalTextEdits != null && !additionalTextEdits.isEmpty()) {
                                    ci.setAdditionalTextEdits(additionalTextEdits.stream().map(ed -> {
                                        return new TextEdit(new Range(Utils.createPosition(file, ed.getStartOffset()), Utils.createPosition(file, ed.getEndOffset())), ed.getNewText());
                                    }).collect(Collectors.toList()));
                                }
                            } catch (Exception ex) {
                            }
                        }
                        if (completion.getDocumentation() != null) {
                            try {
                                int timeout = javadocTimeout.get();
                                String documentation = timeout < 0
                                        ? completion.getDocumentation().get()
                                        : timeout == 0 ? completion.getDocumentation().getNow(null)
                                        : completion.getDocumentation().get(timeout, TimeUnit.MILLISECONDS);
                                if (documentation != null) {
                                    MarkupContent markup = new MarkupContent();
                                    markup.setKind("markdown");
                                    markup.setValue(html2MD(documentation));
                                    ci.setDocumentation(markup);
                                }
                            } catch (Exception ex) {
                            }
                        }
                        result.complete(ci);
                    });
                    return result;
                }
            }
        }
        return CompletableFuture.completedFuture(ci);
    }

    public static String html2MD(String html) {
        int idx = html.indexOf("<p id=\"not-found\">"); // strip "No Javadoc found" message
        return FlexmarkHtmlConverter.builder().build().convert(idx >= 0 ? html.substring(0, idx) : html).replaceAll("<br />[ \n]*$", "");
    }

    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        // shortcut: if the projects are not yet initialized, return empty:
        if (server.openedProjects().getNow(null) == null) {
            return CompletableFuture.completedFuture(null);
        }
        String uri = params.getTextDocument().getUri();
        FileObject file = fromURI(uri);
        Document doc = server.getOpenedDocuments().getDocument(uri);
        if (file == null || !(doc instanceof LineDocument)) {
            return CompletableFuture.completedFuture(null);
        }
        return org.netbeans.api.lsp.Hover.getContent(doc, Utils.getOffset((LineDocument) doc, params.getPosition())).thenApply(content -> {
            if (content != null) {
                MarkupContent markup = new MarkupContent();
                markup.setKind("markdown");
                markup.setValue(html2MD(content));
                return new Hover(markup);
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<SignatureHelp> signatureHelp(SignatureHelpParams params) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(DefinitionParams params) {
        try {
            String uri = params.getTextDocument().getUri();
            Document doc = server.getOpenedDocuments().getDocument(uri);
            if (doc instanceof LineDocument) {
                FileObject file = Utils.fromUri(uri);
                if (file != null) {
                    int offset = Utils.getOffset((LineDocument) doc, params.getPosition());
                    return HyperlinkLocation.resolve(doc, offset).thenApply(locs -> {
                        return Either.forLeft(locs.stream().map(location -> {
                            FileObject fo = location.getFileObject();
                            return new Location(Utils.toUri(fo), new Range(Utils.createPosition(fo, location.getStartOffset()), Utils.createPosition(fo, location.getEndOffset())));
                        }).collect(Collectors.toList()));
                    });
                }
            }
        } catch (MalformedURLException ex) {
            client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
        }
        return CompletableFuture.completedFuture(Either.forLeft(Collections.emptyList()));
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> typeDefinition(TypeDefinitionParams params) {
        try {
            String uri = params.getTextDocument().getUri();
            Document doc = server.getOpenedDocuments().getDocument(uri);
            if (doc instanceof LineDocument) {
                FileObject file = Utils.fromUri(uri);
                if (file != null) {
                    int offset = Utils.getOffset((LineDocument) doc, params.getPosition());
                    return HyperlinkLocation.resolveTypeDefinition(doc, offset).thenApply(locs -> {
                        return Either.forLeft(locs.stream().map(location -> {
                            FileObject fo = location.getFileObject();
                            return new Location(Utils.toUri(fo), new Range(Utils.createPosition(fo, location.getStartOffset()), Utils.createPosition(fo, location.getEndOffset())));
                        }).collect(Collectors.toList()));
                    });
                }
            }
        } catch (MalformedURLException ex) {
            client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
        }
        return CompletableFuture.completedFuture(Either.forLeft(Collections.emptyList()));
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> implementation(ImplementationParams params) {
        return usages(params.getTextDocument().getUri(), params.getPosition(), true, false).thenApply(locations -> Either.forLeft(locations));
    }

    @Override
    public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
        return usages(params.getTextDocument().getUri(), params.getPosition(), false, params.getContext().isIncludeDeclaration());
    }

    private CompletableFuture<List<? extends Location>> usages(String uri, Position position, boolean implementations, boolean includeDeclaration) {
        final Project[] projects = server.openedProjects().getNow(null);
        // shortcut: if the projects are not yet initialized, return empty:
        if (projects == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        AtomicBoolean cancel = new AtomicBoolean();
        Runnable[] cancelCallback = new Runnable[1];
        CompletableFuture<List<? extends Location>> result = new CompletableFuture<List<? extends Location>>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                cancel.set(mayInterruptIfRunning);
                if (cancelCallback[0] != null) {
                    cancelCallback[0].run();
                }
                return super.cancel(mayInterruptIfRunning);
            }
        };
        WORKER.post(() -> {
            JavaSource js = getJavaSource(uri);
            if (js == null) {
                result.complete(new ArrayList<>());
                return;
            }
            try {
                WhereUsedQuery[] query = new WhereUsedQuery[1];
                List<Location> locations = new ArrayList<>();
                js.runUserActionTask(cc -> {
                    cc.toPhase(JavaSource.Phase.RESOLVED);
                    if (cancel.get()) return ;
                    Document doc = cc.getSnapshot().getSource().getDocument(true);
                    if (doc instanceof LineDocument) {
                        TreePath path = cc.getTreeUtilities().pathFor(Utils.getOffset((LineDocument) doc, position));
                        query[0] = new WhereUsedQuery(Lookups.singleton(TreePathHandle.create(path, cc)));
                        if (implementations) {
                            query[0].putValue(WhereUsedQueryConstants.FIND_SUBCLASSES, true);
                            query[0].putValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS, true);
                            query[0].putValue(WhereUsedQuery.FIND_REFERENCES, false);
                        } else if (includeDeclaration) {
                            Element decl = cc.getTrees().getElement(path);
                            if (decl != null) {
                                TreePath declPath = cc.getTrees().getPath(decl);
                                if (declPath != null && cc.getCompilationUnit() == declPath.getCompilationUnit()) {
                                    Range range = declarationRange(cc, declPath);
                                    if (range != null) {
                                        locations.add(new Location(Utils.toUri(cc.getFileObject()),
                                                                   range));
                                    }
                                } else {
                                    ElementHandle<Element> declHandle = ElementHandle.create(decl);
                                    FileObject sourceFile = SourceUtils.getFile(declHandle, cc.getClasspathInfo());
                                    JavaSource source = sourceFile != null ? JavaSource.forFileObject(sourceFile) : null;
                                    if (source != null) {
                                        source.runUserActionTask(nestedCC -> {
                                            nestedCC.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                                            Element declHandle2 = declHandle.resolve(nestedCC);
                                            TreePath declPath2 = declHandle2 != null ? nestedCC.getTrees().getPath(declHandle2) : null;
                                            if (declPath2 != null) {
                                                Range range = declarationRange(nestedCC, declPath2);
                                                if (range != null) {
                                                    locations.add(new Location(Utils.toUri(nestedCC.getFileObject()),
                                                                               range));
                                                }
                                            }
                                        }, true);
                                    }
                                }
                            }
                        }
                    }
                }, true);
                if (cancel.get()) return ;
                List<FileObject> sourceRoots = new ArrayList<>();
                for (Project project : projects) {
                    Sources sources = ProjectUtils.getSources(project);
                    for (SourceGroup sourceGroup : sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                        sourceRoots.add(sourceGroup.getRootFolder());
                    }
                }
                if (!sourceRoots.isEmpty()) {
                    query[0].getContext().add(org.netbeans.modules.refactoring.api.Scope.create(sourceRoots, null, null, true));
                }
                cancelCallback[0] = () -> query[0].cancelRequest();
                RefactoringSession refactoring = RefactoringSession.create("FindUsages");
                Problem p;
                p = query[0].checkParameters();
                if (cancel.get()) return ;
                if (p != null && p.isFatal()) {
                    ErrorUtilities.completeExceptionally(result, p.getMessage(), ResponseErrorCode.UnknownErrorCode);
                    return ;
                }
                p = query[0].preCheck();
                if (p != null && p.isFatal()) {
                    ErrorUtilities.completeExceptionally(result, p.getMessage(), ResponseErrorCode.UnknownErrorCode);
                    return ;
                }
                if (cancel.get()) return ;
                p = query[0].prepare(refactoring);
                if (p != null && p.isFatal()) {
                    ErrorUtilities.completeExceptionally(result, p.getMessage(), ResponseErrorCode.UnknownErrorCode);
                    return ;
                }
                for (RefactoringElement re : refactoring.getRefactoringElements()) {
                    if (cancel.get()) return ;
                    FileObject parentFile = re.getParentFile();
                    if (parentFile.isData()) {
                        locations.add(new Location(Utils.toUri(parentFile), toRange(re.getPosition())));
                    }
                }

                refactoring.finished();

                result.complete(locations);
            } catch (Throwable ex) {
                result.completeExceptionally(ex);
            }
        });
        return result;
    }

    private static Range declarationRange(CompilationInfo info, TreePath tp) {
        Tree t = tp.getLeaf();
        int[] span;
        if (TreeUtilities.CLASS_TREE_KINDS.contains(t.getKind())) {
            span = info.getTreeUtilities().findNameSpan((ClassTree) t);
        } else if (t.getKind() == Kind.VARIABLE) {
            span = info.getTreeUtilities().findNameSpan((VariableTree) t);
        } else if (t.getKind() == Kind.METHOD) {
            span = info.getTreeUtilities().findNameSpan((MethodTree) t);
            if (span == null) {
                span = info.getTreeUtilities().findNameSpan((ClassTree) tp.getParentPath().getLeaf());
            }
        } else {
            return null;
        }
        if (span == null) {
            return null;
        }
        return new Range(Utils.createPosition(info.getCompilationUnit().getLineMap(), span[0]),
                         Utils.createPosition(info.getCompilationUnit().getLineMap(), span[1]));
    }

    private static Range toRange(PositionBounds bounds) throws IOException {
        return bounds != null
                ? new Range(new Position(bounds.getBegin().getLine(), bounds.getBegin().getColumn()),
                        new Position(bounds.getEnd().getLine(), bounds.getEnd().getColumn()))
                : new Range(new Position(0, 0), new Position(0, 0));
    }

    @Override
    public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(DocumentHighlightParams params) {
        class MOHighligther extends MarkOccurrencesHighlighterBase {
            @Override
            protected void process(CompilationInfo arg0, Document arg1, SchedulerEvent arg2) {
                throw new UnsupportedOperationException("Should not be called.");
            }
            @Override
            public List<int[]> processImpl(CompilationInfo info, Preferences node, Document doc, int caretPosition) {
                return super.processImpl(info, node, doc, caretPosition);
            }
        }

        Preferences node = MarkOccurencesSettings.getCurrentNode();

        JavaSource js = getJavaSource(params.getTextDocument().getUri());
        List<DocumentHighlight> result = new ArrayList<>();
        if (js == null) {
            return CompletableFuture.completedFuture(result);
        }
        try {
            js.runUserActionTask(cc -> {
                cc.toPhase(JavaSource.Phase.RESOLVED);
                Document doc = cc.getSnapshot().getSource().getDocument(true);
                if (doc instanceof LineDocument) {
                    int offset = Utils.getOffset((LineDocument) doc, params.getPosition());
                    List<int[]> spans = new MOHighligther().processImpl(cc, node, doc, offset);
                    if (spans != null) {
                        for (int[] span : spans) {
                            result.add(new DocumentHighlight(new Range(Utils.createPosition(cc.getCompilationUnit(), span[0]),
                                                                       Utils.createPosition(cc.getCompilationUnit(), span[1]))));
                        }
                    }
                }
            }, true);
        } catch (IOException ex) {
            //TODO: include stack trace:
            client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
        }
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(DocumentSymbolParams params) {
        return server.openedProjects().thenCompose(projects -> {
            JavaSource js = getJavaSource(params.getTextDocument().getUri());
            if (js == null) {
                return CompletableFuture.completedFuture(Collections.emptyList());
            }
            List<Either<SymbolInformation, DocumentSymbol>> result = new ArrayList<>();
            try {
                js.runUserActionTask(cc -> {
                    cc.toPhase(JavaSource.Phase.RESOLVED);
                    Trees trees = cc.getTrees();
                    CompilationUnitTree cu = cc.getCompilationUnit();
                    if (cu.getPackage() != null) {
                        TreePath tp = trees.getPath(cu, cu.getPackage());
                        Element el = trees.getElement(tp);
                        if (el != null && el.getKind() == ElementKind.PACKAGE) {
                            String name = Utils.label(cc, el, true);
                            SymbolKind kind = Utils.elementKind2SymbolKind(el.getKind());
                            Range range = Utils.treeRange(cc, tp.getLeaf());
                            result.add(Either.forRight(new DocumentSymbol(name, kind, range, range)));
                        }
                    }
                    for (Element tel : cc.getTopLevelElements()) {
                        DocumentSymbol ds = element2DocumentSymbol(cc, tel);
                        if (ds != null)
                            result.add(Either.forRight(ds));
                    }
                }, true);
            } catch (IOException ex) {
                client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
            }
            return CompletableFuture.completedFuture(result);
        });
    }

    private static DocumentSymbol element2DocumentSymbol(CompilationInfo info, Element el) {
        TreePath path = info.getTrees().getPath(el);
        if (path == null)
            return null;
        TreeUtilities tu = info.getTreeUtilities();
        if (tu.isSynthetic(path))
            return null;
        Range range = Utils.treeRange(info, path.getLeaf());
        if (range == null)
            return null;
        Range selection = Utils.selectionRange(info, path.getLeaf());
        List<DocumentSymbol> children = new ArrayList<>();
        for (Element c : el.getEnclosedElements()) {
            DocumentSymbol ds = element2DocumentSymbol(info, c);
            if (ds != null) {
                children.add(ds);
            }
        }
        if (path.getLeaf().getKind() == Kind.METHOD || path.getLeaf().getKind() == Kind.VARIABLE) {
            children.addAll(getAnonymousInnerClasses(info, path));
        }
        String name = Utils.label(info, el, false);
        String detail = Utils.detail(info, el, false);
        return new DocumentSymbol(name, Utils.elementKind2SymbolKind(el.getKind()), range, selection != null ? selection : range, detail, children);
    }

    private static List<DocumentSymbol> getAnonymousInnerClasses(CompilationInfo info, TreePath path) {
        List<DocumentSymbol> inner = new ArrayList<>();
        new TreePathScanner<Void, Void>() {
            @Override
            public Void visitNewClass(NewClassTree node, Void p) {
                if (node.getClassBody() != null) {
                    Range range = Utils.treeRange(info, node);
                    if (range != null) {
                        Range selectionRange = Utils.treeRange(info, node.getIdentifier());
                        Element e = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getClassBody()));
                        if (e != null) {
                            Element te = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getIdentifier()));
                            if (te != null) {
                                String name = "new " + te.getSimpleName() + "() {...}";
                                List<DocumentSymbol> children = new ArrayList<>();
                                for (Element c : e.getEnclosedElements()) {
                                    DocumentSymbol ds = element2DocumentSymbol(info, c);
                                    if (ds != null) {
                                        children.add(ds);
                                    }
                                }
                                inner.add(new DocumentSymbol(name, Utils.elementKind2SymbolKind(e.getKind()), range, selectionRange, null, children));
                            }
                        }
                    }
                }
                return null;
            }
        }.scan(path, null);
        return inner;
    }

    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
        // shortcut: if the projects are not yet initialized, return empty:
        if (server.openedProjects().getNow(null) == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        Document doc = server.getOpenedDocuments().getDocument(params.getTextDocument().getUri());
        if (!(doc instanceof LineDocument)) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        List<Either<Command, CodeAction>> result = new ArrayList<>();
        Range range = params.getRange();
        int startOffset = Utils.getOffset((LineDocument) doc, range.getStart());
        int endOffset = Utils.getOffset((LineDocument) doc, range.getEnd());
        if (startOffset == endOffset) {
            int lineStartOffset = LineDocumentUtils.getLineStart((LineDocument) doc, startOffset);
            int lineEndOffset;
            try {
                lineEndOffset = LineDocumentUtils.getLineEnd((LineDocument) doc, endOffset);
            } catch (BadLocationException ex) {
                lineEndOffset = endOffset;
            }

            ArrayList<Diagnostic> diagnostics = new ArrayList<>(params.getContext().getDiagnostics());
            diagnostics.addAll(computeDiags(params.getTextDocument().getUri(), startOffset, ErrorProvider.Kind.HINTS, documentVersion(doc)));

            Map<String, org.netbeans.api.lsp.Diagnostic> id2Errors = (Map<String, org.netbeans.api.lsp.Diagnostic>) doc.getProperty("lsp-errors");
            if (id2Errors != null) {
                for (Entry<String, org.netbeans.api.lsp.Diagnostic> entry : id2Errors.entrySet()) {
                    org.netbeans.api.lsp.Diagnostic err = entry.getValue();
                    if (err.getSeverity() == org.netbeans.api.lsp.Diagnostic.Severity.Error) {
                        if (err.getEndPosition().getOffset() < startOffset || err.getStartPosition().getOffset() > endOffset) {
                            continue;
                        }
                    } else {
                        if (err.getEndPosition().getOffset() < lineStartOffset || err.getStartPosition().getOffset() > lineEndOffset) {
                            continue;
                        }
                    }
                    Optional<Diagnostic> diag = diagnostics.stream().filter(d -> entry.getKey().equals(d.getCode().getLeft())).findFirst();
                    org.netbeans.api.lsp.Diagnostic.LazyCodeActions actions = err.getActions();
                    if (actions != null) {
                        for (org.netbeans.api.lsp.CodeAction inputAction : actions.computeCodeActions(ex -> client.logMessage(new MessageParams(MessageType.Error, ex.getMessage())))) {
                            CodeAction action = new CodeAction(inputAction.getTitle());
                            if (diag.isPresent()) {
                                action.setDiagnostics(Collections.singletonList(diag.get()));
                            }
                            action.setKind(kind(err.getSeverity()));
                            if (inputAction.getCommand() != null) {
                                action.setCommand(new Command(inputAction.getCommand().getTitle(), inputAction.getCommand().getCommand()));
                            }
                            if (inputAction.getEdit() != null) {
                                org.netbeans.api.lsp.WorkspaceEdit edit = inputAction.getEdit();
                                List<Either<TextDocumentEdit, ResourceOperation>> documentChanges = new ArrayList<>();
                                for (Union2<org.netbeans.api.lsp.TextDocumentEdit, org.netbeans.api.lsp.ResourceOperation> parts : edit.getDocumentChanges()) {
                                    if (parts.hasFirst()) {
                                        String docUri = parts.first().getDocument();
                                        try {
                                            FileObject file = Utils.fromUri(docUri);
                                            if (file == null) {
                                                file = Utils.fromUri(params.getTextDocument().getUri());
                                            }
                                            FileObject fo = file;
                                            if (fo != null) {
                                                List<TextEdit> edits = parts.first().getEdits().stream().map(te -> new TextEdit(new Range(Utils.createPosition(fo, te.getStartOffset()), Utils.createPosition(fo, te.getEndOffset())), te.getNewText())).collect(Collectors.toList());
                                                TextDocumentEdit tde = new TextDocumentEdit(new VersionedTextDocumentIdentifier(docUri, -1), edits);
                                                documentChanges.add(Either.forLeft(tde));
                                            }
                                        } catch (Exception ex) {
                                            client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
                                        }
                                    } else {
                                        if (parts.second() instanceof org.netbeans.api.lsp.ResourceOperation.CreateFile) {
                                            documentChanges.add(Either.forRight(new CreateFile(((org.netbeans.api.lsp.ResourceOperation.CreateFile) parts.second()).getNewFile())));
                                        } else {
                                            throw new IllegalStateException(String.valueOf(parts.second()));
                                        }
                                    }
                                }

                                action.setEdit(new WorkspaceEdit(documentChanges));
                            }
                            result.add(Either.forRight(action));
                        }
                    }
                }
            }
        }

        final CompletableFuture<List<Either<Command, CodeAction>>> resultFuture = new CompletableFuture<>();
        Source source = Source.create(doc);
        BACKGROUND_TASKS.post(() -> {
            try {
                ParserManager.parse(Collections.singleton(source), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        //code generators:
                        for (CodeActionsProvider codeGenerator : Lookup.getDefault().lookupAll(CodeActionsProvider.class)) {
                            try {
                                for (CodeAction codeAction : codeGenerator.getCodeActions(resultIterator, params)) {
                                    result.add(Either.forRight(codeAction));
                                }
                            } catch (Exception ex) {
                                client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
                            }
                        }
                        //introduce hints:
                        CompilationController cc = CompilationController.get(resultIterator.getParserResult());
                        if (cc != null) {
                            cc.toPhase(JavaSource.Phase.RESOLVED);
                            if (!range.getStart().equals(range.getEnd())) {
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
                                                            codeAction.setCommand(new Command("Rename", "java.rename.element.at", Collections.singletonList(renameOffset)));
                                                        }
                                                        result.add(Either.forRight(codeAction));
                                                    }
                                                }
                                            } catch (GeneratorUtils.DuplicateMemberException dme) {
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            } catch (ParseException ex) {
                //TODO: include stack trace:
                client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
            } finally {
                resultFuture.complete(result);
            }
        });
        return resultFuture;
    }

    @Override
    public CompletableFuture<CodeAction> resolveCodeAction(CodeAction unresolved) {
        JsonObject data = (JsonObject) unresolved.getData();
        if (data != null) {
            String providerClass = data.getAsJsonPrimitive(CodeActionsProvider.CODE_ACTIONS_PROVIDER_CLASS).getAsString();
            for (CodeActionsProvider codeGenerator : Lookup.getDefault().lookupAll(CodeActionsProvider.class)) {
                try {
                    if (codeGenerator.getClass().getName().equals(providerClass)) {
                        return codeGenerator.resolve(client, unresolved, data.get(CodeActionsProvider.DATA));
                    }
                } catch (Exception ex) {
                }
            }
        }
        return CompletableFuture.completedFuture(unresolved);
    }

    @NbBundle.Messages({"# {0} - method name", "LBL_Run=Run {0}",
                        "# {0} - method name", "LBL_Debug=Debug {0}",
                        "# {0} - method name", "# {1} - configuration name", "LBL_RunWith=Run {0} with {1}",
                        "# {0} - method name", "# {1} - configuration name", "LBL_DebugWith=Debug {0} with {1}"})
    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
        // shortcut: if the projects are not yet initialized, return empty:
        if (server.openedProjects().getNow(null) == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        String uri = params.getTextDocument().getUri();
        Source source = getSource(uri);
        if (source == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        CompletableFuture<List<? extends CodeLens>> result = new CompletableFuture<>();
        try {
            ParserManager.parseWhenScanFinished(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Parser.Result parserResult = resultIterator.getParserResult();
                    //look for main methods:
                    List<CodeLens> lens = new ArrayList<>();
                    CompilationController cc = CompilationController.get(parserResult);
                    if (cc != null) {
                        cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        AtomicReference<List<Pair<String, String>>> projectConfigurations = new AtomicReference<>();
                        new TreePathScanner<Void, Void>() {
                            public Void visitMethod(MethodTree tree, Void p) {
                                Element el = cc.getTrees().getElement(getCurrentPath());
                                if (el != null && el.getKind() == ElementKind.METHOD && SourceUtils.isMainMethod((ExecutableElement) el)) {
                                    Range range = Utils.treeRange(cc, tree);
                                    List<Object> arguments = Collections.singletonList(params.getTextDocument().getUri());
                                    String method = el.getSimpleName().toString();
                                    lens.add(new CodeLens(range,
                                                          new Command(Bundle.LBL_Run(method), COMMAND_RUN_SINGLE, arguments),
                                                          null));
                                    lens.add(new CodeLens(range,
                                                          new Command(Bundle.LBL_Debug(method), COMMAND_DEBUG_SINGLE, arguments),
                                                          null));
                                    // Run and Debug configurations:
                                    List<Pair<String, String>> configs = projectConfigurations.accumulateAndGet(null, (l, nul) -> l == null ? getProjectConfigurations(source) : l);
                                    for (Pair<String, String> config : configs) {
                                        String runConfig = config.first();
                                        if (runConfig != null) {
                                            lens.add(new CodeLens(range,
                                                                  new Command(Bundle.LBL_RunWith(method, runConfig), COMMAND_RUN_SINGLE, Arrays.asList(params.getTextDocument().getUri(), null, runConfig)),
                                                                  null));
                                        }
                                        String debugConfig = config.second();
                                        if (debugConfig != null) {
                                            lens.add(new CodeLens(range,
                                                                  new Command(Bundle.LBL_DebugWith(method, debugConfig), COMMAND_DEBUG_SINGLE, Arrays.asList(params.getTextDocument().getUri(), null, debugConfig)),
                                                                  null));
                                        }
                                    }
                                }
                                return null;
                            }
                        }.scan(cc.getCompilationUnit(), null);
                    }
                    result.complete(lens);
                }
            });
        } catch (ParseException ex) {
            result.completeExceptionally(ex);
        }
        return result;
    }

    private List<Pair<String, String>> getProjectConfigurations(Source source) {
        FileObject fo = source.getFileObject();
        Project p = FileOwnerQuery.getOwner(fo);
        if (p != null) {
            ProjectConfigurationProvider<ProjectConfiguration> configProvider = p.getLookup().lookup(ProjectConfigurationProvider.class);
            ActionProvider actionProvider = p.getLookup().lookup(ActionProvider.class);
            List<Pair<String, String>> configDispNames = new ArrayList<>();
            if (configProvider != null && actionProvider != null) {
                boolean skippedFirst = false;
                for (ProjectConfiguration configuration : configProvider.getConfigurations()) {
                    if (skippedFirst) {
                        String runConfig = null;
                        String debugConfig = null;
                        Lookup configLookup = Lookups.fixed(fo, configuration);
                        if (isConfigurationAction(configProvider, actionProvider, configLookup, ActionProvider.COMMAND_RUN_SINGLE)) {
                            runConfig = configuration.getDisplayName();
                        }
                        if (isConfigurationAction(configProvider, actionProvider, configLookup, ActionProvider.COMMAND_DEBUG_SINGLE)) {
                            debugConfig = configuration.getDisplayName();
                        }
                        if (runConfig != null || debugConfig != null) {
                            configDispNames.add(Pair.of(runConfig, debugConfig));
                        }
                    } else {
                        // Ignore the default config
                        skippedFirst = true;
                    }
                }
            }
            return configDispNames;
        }
        return Collections.emptyList();
    }

    private static boolean isConfigurationAction(ProjectConfigurationProvider<ProjectConfiguration> configProvider, ActionProvider actionProvider, Lookup configLookup, String action) {
        return configProvider.configurationsAffectAction(action) && actionProvider.isActionEnabled(action, configLookup);
    }

    @Override
    public CompletableFuture<CodeLens> resolveCodeLens(CodeLens arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CompletableFuture<Either<Range, PrepareRenameResult>> prepareRename(PrepareRenameParams params) {
        // shortcut: if the projects are not yet initialized, return empty:
        if (server.openedProjects().getNow(null) == null) {
            return CompletableFuture.completedFuture(null);
        }
        JavaSource source = getJavaSource(params.getTextDocument().getUri());
        if (source == null) {
            return CompletableFuture.completedFuture(null);
        }
        CompletableFuture<Either<Range, PrepareRenameResult>> result = new CompletableFuture<>();
        try {
            source.runUserActionTask(cc -> {
                cc.toPhase(JavaSource.Phase.RESOLVED);
                Document doc = cc.getSnapshot().getSource().getDocument(true);
                if (!(doc instanceof LineDocument)) {
                    result.complete(null);
                }
                int pos = Utils.getOffset((LineDocument) doc, params.getPosition());
                TreePath path = cc.getTreeUtilities().pathFor(pos);
                RenameRefactoring ref = new RenameRefactoring(Lookups.singleton(TreePathHandle.create(path, cc)));
                ref.setNewName("any");
                Problem p = ref.fastCheckParameters();
                boolean hasFatalProblem = false;
                while (p != null) {
                    hasFatalProblem |= p.isFatal();
                    p = p.getNext();
                }
                if (hasFatalProblem) {
                    result.complete(null);
                } else {
                    //XXX: better range computation
                    TokenSequence<JavaTokenId> ts = cc.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                    int d = ts.move(pos);
                    if (ts.moveNext()) {
                        if (d == 0 && ts.token().id() != JavaTokenId.IDENTIFIER) {
                            ts.movePrevious();
                        }
                        Range r = new Range(Utils.createPosition(cc.getCompilationUnit(), ts.offset()),
                                            Utils.createPosition(cc.getCompilationUnit(), ts.offset() + ts.token().length()));
                        result.complete(Either.forRight(new PrepareRenameResult(r, ts.token().text().toString())));
                    } else {
                        result.complete(null);
                    }
                }
            }, true);
        } catch (IOException ex) {
            result.completeExceptionally(ex);
        }
        return result;
    }

    @Override
    public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
        // shortcut: if the projects are not yet initialized, return empty:
        if (server.openedProjects().getNow(null) == null) {
            return CompletableFuture.completedFuture(new WorkspaceEdit());
        }
        AtomicBoolean cancel = new AtomicBoolean();
        Runnable[] cancelCallback = new Runnable[1];
        CompletableFuture<WorkspaceEdit> result = new CompletableFuture<WorkspaceEdit>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                cancel.set(mayInterruptIfRunning);
                if (cancelCallback[0] != null) {
                    cancelCallback[0].run();
                }
                return super.cancel(mayInterruptIfRunning);
            }
        };
        WORKER.post(() -> {
            JavaSource js = getJavaSource(params.getTextDocument().getUri());
            if (js == null) {
                result.completeExceptionally(new FileNotFoundException(params.getTextDocument().getUri()));
                return;
            }
            try {
                RenameRefactoring[] refactoring = new RenameRefactoring[1];
                js.runUserActionTask(cc -> {
                    cc.toPhase(JavaSource.Phase.RESOLVED);
                    if (cancel.get()) return ;
                    Document doc = cc.getSnapshot().getSource().getDocument(true);
                    if (doc instanceof LineDocument) {
                        TreePath path = cc.getTreeUtilities().pathFor(Utils.getOffset((LineDocument) doc, params.getPosition()));
                        List<Object> lookupContent = new ArrayList<>();

                        lookupContent.add(TreePathHandle.create(path, cc));

                        //from RenameRefactoringUI:
                        Element selected = cc.getTrees().getElement(path);
                        if (selected instanceof TypeElement && !((TypeElement) selected).getNestingKind().isNested()) {
                            ElementHandle<TypeElement> handle = ElementHandle.create((TypeElement) selected);
                            FileObject f = SourceUtils.getFile(handle, cc.getClasspathInfo());
                            if (f != null && selected.getSimpleName().toString().equals(f.getName())) {
                                lookupContent.add(f);
                            }
                        }

                        refactoring[0] = new RenameRefactoring(Lookups.fixed(lookupContent.toArray(new Object[0])));
                        refactoring[0].setNewName(params.getNewName());
                        refactoring[0].setSearchInComments(true); //TODO?
                    }
                }, true);
                if (cancel.get()) return ;
                cancelCallback[0] = () -> refactoring[0].cancelRequest();
                RefactoringSession session = RefactoringSession.create("Rename");
                Problem p;
                p = refactoring[0].checkParameters();
                if (cancel.get()) return ;
                if (p != null && p.isFatal()) {
                    ErrorUtilities.completeExceptionally(result, p.getMessage(), ResponseErrorCode.UnknownErrorCode);
                    return ;
                }
                p = refactoring[0].preCheck();
                if (p != null && p.isFatal()) {
                    ErrorUtilities.completeExceptionally(result, p.getMessage(), ResponseErrorCode.UnknownErrorCode);
                    return ;
                }
                if (cancel.get()) return ;
                p = refactoring[0].prepare(session);
                if (p != null && p.isFatal()) {
                    ErrorUtilities.completeExceptionally(result, p.getMessage(), ResponseErrorCode.UnknownErrorCode);
                    return ;
                }
                //TODO: check client capabilities!
                List<Either<TextDocumentEdit, ResourceOperation>> resultChanges = new ArrayList<>();
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
                    for (FileObject modified : mr.getModifiedFileObjects()) {
                        resultChanges.add(Either.forLeft(new TextDocumentEdit(new VersionedTextDocumentIdentifier(Utils.toUri(modified), /*XXX*/-1), fileModifications(mr, modified, null))));
                    }
                }
                List<RefactoringElementImplementation> fileChanges = APIAccessor.DEFAULT.getFileChanges(session);
                for (RefactoringElementImplementation rei : fileChanges) {
                    if (rei instanceof FileRenamePlugin.RenameFile) {
                        String oldURI = params.getTextDocument().getUri();
                        int dot = oldURI.lastIndexOf('.');
                        int slash = oldURI.lastIndexOf('/');
                        String newURI = oldURI.substring(0, slash + 1) + params.getNewName() + oldURI.substring(dot);
                        ResourceOperation op = new RenameFile(oldURI, newURI);
                        resultChanges.add(Either.forRight(op));
                    } else {
                        throw new IllegalStateException(rei.getClass().toString());
                    }
                }
                for (RefactoringElement re : session.getRefactoringElements()) {
                    //TODO: verify no unknown elements!
                }

                session.finished();

                result.complete(new WorkspaceEdit(resultChanges));
            } catch (Throwable ex) {
                result.completeExceptionally(ex);
            }
        });
        return result;
    }

    @Override
    public CompletableFuture<List<FoldingRange>> foldingRange(FoldingRangeRequestParams params) {
        JavaSource source = getJavaSource(params.getTextDocument().getUri());
        if (source == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        CompletableFuture<List<FoldingRange>> result = new CompletableFuture<>();
        try {
            source.runUserActionTask(cc -> {
                cc.toPhase(JavaSource.Phase.RESOLVED);
                Document doc = cc.getSnapshot().getSource().getDocument(true);
                JavaElementFoldVisitor v = new JavaElementFoldVisitor(cc, cc.getCompilationUnit(), cc.getTrees().getSourcePositions(), doc, new FoldCreator<FoldingRange>() {
                    @Override
                    public FoldingRange createImportsFold(int start, int end) {
                        return createFold(start, end, FoldingRangeKind.Imports);
                    }

                    @Override
                    public FoldingRange createInnerClassFold(int start, int end) {
                        return createFold(start, end, FoldingRangeKind.Region);
                    }

                    @Override
                    public FoldingRange createCodeBlockFold(int start, int end) {
                        return createFold(start, end, FoldingRangeKind.Region);
                    }

                    @Override
                    public FoldingRange createJavadocFold(int start, int end) {
                        return createFold(start, end, FoldingRangeKind.Comment);
                    }

                    @Override
                    public FoldingRange createInitialCommentFold(int start, int end) {
                        return createFold(start, end, FoldingRangeKind.Comment);
                    }

                    private FoldingRange createFold(int start, int end, String kind) {
                        Position startPos = Utils.createPosition(cc.getCompilationUnit(), start);
                        Position endPos = Utils.createPosition(cc.getCompilationUnit(), end);
                        FoldingRange range = new FoldingRange(startPos.getLine(), endPos.getLine());

                        range.setStartCharacter(startPos.getCharacter());
                        range.setEndCharacter(endPos.getCharacter());
                        range.setKind(kind);

                        return range;
                    }
                });
                v.checkInitialFold();
                v.scan(cc.getCompilationUnit(), null);
                result.complete(v.getFolds());
            }, true);
        } catch (IOException ex) {
            result.completeExceptionally(ex);
        }
        return result;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        try {
            FileObject file = fromURI(params.getTextDocument().getUri(), true);
            if (file == null) {
                return;
            }
            EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
            Document doc = ec.getDocument();
            // the document may be not opened yet. Clash with in-memory content can happen only if
            // the doc was opened prior to request reception.
            String text = params.getTextDocument().getText();
            try {
                if (doc == null) {
                    doc = ec.openDocument();
                }
                if (!text.contentEquals(doc.getText(0, doc.getLength()))) {
                    doc.remove(0, doc.getLength());
                    doc.insertString(0, text, null);
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
                //TODO: include stack trace:
                client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
            }
            server.getOpenedDocuments().notifyOpened(params.getTextDocument().getUri(), doc);
            
            // attempt to open the directly owning project, delay diagnostics after project open:
            server.asyncOpenFileOwner(file).thenRun(() ->
                runDiagnosticTasks(params.getTextDocument().getUri())
            );
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        } finally {
            reportNotificationDone("didOpen", params);
        }
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        Document doc = server.getOpenedDocuments().getDocument(uri);
        if (doc != null) {
            NbDocument.runAtomic((StyledDocument) doc, () -> {
                for (TextDocumentContentChangeEvent change : params.getContentChanges()) {
                    try {
                        int start = Utils.getOffset((LineDocument) doc, change.getRange().getStart());
                        int end   = Utils.getOffset((LineDocument) doc, change.getRange().getEnd());
                        doc.remove(start, end - start);
                        doc.insertString(start, change.getText(), null);
                    } catch (BadLocationException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            });
        }
        runDiagnosticTasks(params.getTextDocument().getUri());
        reportNotificationDone("didChange", params);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        try {
            String uri = params.getTextDocument().getUri();
            // the order here is important ! As the file may cease to exist, it's
            // important that the doucment is already gone form the client.
            server.getOpenedDocuments().notifyClosed(uri);
            FileObject file = fromURI(uri, true);
            if (file == null) {
                return;
            }
            EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
            ec.close();
        } finally {
            reportNotificationDone("didClose", params);
        }
    }

    @Override
    public CompletableFuture<List<TextEdit>> willSaveWaitUntil(WillSaveTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        JavaSource js = getJavaSource(uri);
        if (js == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        ConfigurationItem conf = new ConfigurationItem();
        conf.setScopeUri(uri);
        conf.setSection(NETBEANS_JAVA_ON_SAVE_ORGANIZE_IMPORTS);
        return client.configuration(new ConfigurationParams(Collections.singletonList(conf))).thenApply(c -> {
            if (c != null && !c.isEmpty() && ((JsonPrimitive) c.get(0)).getAsBoolean()) {
                try {
                    List<TextEdit> edits = TextDocumentServiceImpl.modify2TextEdits(js, wc -> {
                        wc.toPhase(JavaSource.Phase.RESOLVED);
                        OrganizeImports.doOrganizeImports(wc, null, false);
                    });
                    return edits;
                } catch (IOException ex) {}
            }
            return Collections.emptyList();
        });
    }

    @Override
    public void didSave(DidSaveTextDocumentParams arg0) {
        //TODO: nothing for now?
    }

    CompletableFuture<List<? extends Location>> superImplementations(String uri, Position position) {
        JavaSource js = getJavaSource(uri);
        List<GoToTarget> targets = new ArrayList<>();
        LineMap[] thisFileLineMap = new LineMap[1];
        try {
            if (js != null) {
                js.runUserActionTask(cc -> {
                    cc.toPhase(JavaSource.Phase.RESOLVED);
                    Document doc = cc.getSnapshot().getSource().getDocument(true);
                    if (doc instanceof LineDocument) {
                        int offset = Utils.getOffset((LineDocument) doc, position);
                        TreeUtilities treeUtilities = cc.getTreeUtilities();
                        TreePath path = treeUtilities.getPathElementOfKind(EnumSet.of(Kind.CLASS, Kind.INTERFACE, Kind.ENUM, Kind.ANNOTATION_TYPE, Kind.METHOD), treeUtilities.pathFor(offset));
                        if (path != null) {
                            Trees trees = cc.getTrees();
                            Element resolved = trees.getElement(path);
                            if (resolved != null) {
                                if (resolved.getKind() == ElementKind.METHOD) {
                                    Map<ElementHandle<? extends Element>, List<ElementDescription>> overriding = new ComputeOverriding(new AtomicBoolean()).process(cc);
                                    List<ElementDescription> eds = overriding.get(ElementHandle.create(resolved));
                                    if (eds != null) {
                                        for (ElementDescription ed : eds) {
                                            Element el = ed.getHandle().resolve(cc);
                                            TreePath tp = trees.getPath(el);
                                            long startPos = tp != null && cc.getCompilationUnit() == tp.getCompilationUnit() ? trees.getSourcePositions().getStartPosition(cc.getCompilationUnit(), tp.getLeaf()) : -1;
                                            if (startPos >= 0) {
                                                long endPos = trees.getSourcePositions().getEndPosition(cc.getCompilationUnit(), tp.getLeaf());
                                                targets.add(new GoToTarget(cc.getSnapshot().getOriginalOffset((int) startPos),
                                                        cc.getSnapshot().getOriginalOffset((int) endPos), GoToSupport.getNameSpan(tp.getLeaf(), treeUtilities),
                                                        null, null, null, ed.getDisplayName(), true));
                                            } else {
                                                TypeElement te = el != null ? cc.getElementUtilities().outermostTypeElement(el) : null;
                                                targets.add(new GoToTarget(-1, -1, null, ed.getOriginalCPInfo(), ed.getHandle(), getResourceName(te, ed.getHandle()), ed.getDisplayName(), true));
                                            }
                                        }
                                    }
                                } else if (resolved.getKind().isClass() || resolved.getKind().isInterface()) {
                                    List<TypeMirror> superTypes = new ArrayList<>();
                                    superTypes.add(((TypeElement)resolved).getSuperclass());
                                    superTypes.addAll(((TypeElement)resolved).getInterfaces());
                                    for (TypeMirror superType : superTypes) {
                                        if (superType.getKind() == TypeKind.DECLARED) {
                                            Element el = ((DeclaredType) superType).asElement();
                                            TreePath tp = trees.getPath(el);
                                            long startPos = tp != null && cc.getCompilationUnit() == tp.getCompilationUnit() ? trees.getSourcePositions().getStartPosition(cc.getCompilationUnit(), tp.getLeaf()) : -1;
                                            if (startPos >= 0) {
                                                long endPos = trees.getSourcePositions().getEndPosition(cc.getCompilationUnit(), tp.getLeaf());
                                                targets.add(new GoToTarget(cc.getSnapshot().getOriginalOffset((int) startPos),
                                                        cc.getSnapshot().getOriginalOffset((int) endPos), GoToSupport.getNameSpan(tp.getLeaf(), treeUtilities),
                                                        null, null, null, cc.getElementUtilities().getElementName(el, false).toString(), true));
                                            } else {
                                                TypeElement te = el != null ? cc.getElementUtilities().outermostTypeElement(el) : null;
                                                targets.add(new GoToTarget(-1, -1, null, cc.getClasspathInfo(), ElementHandle.create(el), getResourceName(te, null),
                                                        cc.getElementUtilities().getElementName(el, false).toString(), true));
                                            }
                                        }
                                    }
                                }
                                thisFileLineMap[0] = cc.getCompilationUnit().getLineMap();
                            }
                        }
                    }
                }, true);
            }
        } catch (IOException ex) {
            client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
        }
        CompletableFuture<Location>[] futures = targets.stream().map(target -> gotoTarget2Location(uri, target, thisFileLineMap[0])).toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures).thenApply(value -> {
            ArrayList<Location> locations = new ArrayList<>(futures.length);
            for (CompletableFuture<Location> future : futures) {
                Location location = future.getNow(null);
                if (location != null) {
                    locations.add(location);
                }
            }
            return locations;
        });
    }

    private CompletableFuture<Location> gotoTarget2Location(String uri, GoToTarget target, LineMap lineMap) {
        Location location = null;
        if (target != null && target.success) {
            if (target.offsetToOpen < 0) {
                final CompletableFuture<ElementOpen.Location> future = ElementOpen.getLocation(target.cpInfo, target.elementToOpen, target.resourceName);
                return future.thenApply(loc -> {
                    if (loc != null) {
                        FileObject fo = loc.getFileObject();
                        return new Location(Utils.toUri(fo), new Range(Utils.createPosition(fo, loc.getStartOffset()), Utils.createPosition(fo, loc.getEndOffset())));
                    }
                    return null;
                });
            } else {
                int start = target.nameSpan != null ? target.nameSpan[0] : target.offsetToOpen;
                int end = target.nameSpan != null ? target.nameSpan[1] : target.endPos;
                location = new Location(uri, new Range(Utils.createPosition(lineMap, start), Utils.createPosition(lineMap, end)));
            }
        }
        return CompletableFuture.completedFuture(location);
    }

    private void runDiagnosticTasks(String uri) {
        if (server.openedProjects().getNow(null) == null) {
            return;
        }
        diagnosticTasks.computeIfAbsent(uri, u -> {
            return BACKGROUND_TASKS.create(() -> {
                Document originalDoc = server.getOpenedDocuments().getDocument(uri);
                long originalVersion = documentVersion(originalDoc);
                List<Diagnostic> errorDiags = computeDiags(u, -1, ErrorProvider.Kind.ERRORS, originalVersion);
                if (documentVersion(originalDoc) == originalVersion) {
                    publishDiagnostics(uri, errorDiags);
                    BACKGROUND_TASKS.create(() -> {
                        List<Diagnostic> hintDiags = computeDiags(u, -1, ErrorProvider.Kind.HINTS, originalVersion);
                        Document doc = server.getOpenedDocuments().getDocument(uri);
                        if (documentVersion(doc) == originalVersion) {
                            publishDiagnostics(uri, hintDiags);
                }
                    }).schedule(DELAY);
                }
            });
        }).schedule(DELAY);
    }

    private static final int DELAY = 500;

    private List<Diagnostic> computeDiags(String uri, int offset, ErrorProvider.Kind errorKind, long originalVersion) {
        List<Diagnostic> result = new ArrayList<>();
        FileObject file = fromURI(uri);
        if (file == null) {
            // the file does not exist.
            return result;
        }
        try {
            String keyPrefix = key(errorKind);
            EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
            Document doc = ec.openDocument();
            Map<String, org.netbeans.api.lsp.Diagnostic> id2Errors = new HashMap<>();
            ErrorProvider errorProvider = MimeLookup.getLookup(DocumentUtilities.getMimeType(doc))
                                                    .lookup(ErrorProvider.class);
            List<? extends org.netbeans.api.lsp.Diagnostic> errors;
            if (errorProvider != null) {
                ErrorProvider.Context context = new ErrorProvider.Context(file, offset, errorKind);
                class CancelListener implements DocumentListener {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        checkCancel();
                    }
                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        checkCancel();
                    }
                    private void checkCancel() {
                        if (documentVersion(doc) != originalVersion) {
                            context.cancel();
                        }
                    }
                    @Override
                    public void changedUpdate(DocumentEvent e) {}
                }
                CancelListener l = new CancelListener();
                try {
                    doc.addDocumentListener(l);
                    l.checkCancel();
                    errors = errorProvider.computeErrors(context);
                } finally {
                    doc.removeDocumentListener(l);
                }
            } else {
                errors = null;
            }
            if (errors == null) {
                errors = Collections.emptyList();
            }
            if (documentVersion(doc) != originalVersion) {
                return result;
            }
            for (org.netbeans.api.lsp.Diagnostic err : errors) {
                String id = err.getCode();
                id2Errors.put(id, err);
            }
            if (offset < 0) {
                doc.putProperty("lsp-errors-" + keyPrefix, id2Errors);
            }
            Map<String, org.netbeans.api.lsp.Diagnostic> mergedId2Errors = new HashMap<>();
            for (String k : ERROR_KEYS) {
                Map<String, org.netbeans.api.lsp.Diagnostic> prevErrors = (Map<String, org.netbeans.api.lsp.Diagnostic>) doc.getProperty("lsp-errors-" + k);
                if (prevErrors != null) {
                    mergedId2Errors.putAll(prevErrors);
                }
            }
            for (Entry<String, org.netbeans.api.lsp.Diagnostic> id2Error : (offset < 0 ? mergedId2Errors : id2Errors).entrySet()) {
                org.netbeans.api.lsp.Diagnostic err = id2Error.getValue();
                Diagnostic diag = new Diagnostic(new Range(Utils.createPosition(file, err.getStartPosition().getOffset()),
                                                           Utils.createPosition(file, err.getEndPosition().getOffset())),
                                                 err.getDescription());
                switch (err.getSeverity()) {
                    case Error: diag.setSeverity(DiagnosticSeverity.Error); break;
                    case Warning: diag.setSeverity(DiagnosticSeverity.Warning); break;
                    case Hint: diag.setSeverity(DiagnosticSeverity.Hint); break;
                    case Information: diag.setSeverity(DiagnosticSeverity.Information); break;
                    default: throw new IllegalStateException("Unknown severity: " + err.getSeverity());
                }
                diag.setCode(id2Error.getKey());
                result.add(diag);
            }
            if (offset >= 0) {
                mergedId2Errors.putAll(id2Errors);
            }
            doc.putProperty("lsp-errors", mergedId2Errors);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        return result;
    }
    
    private String key(ErrorProvider.Kind errorKind) {
        return errorKind.name().toLowerCase(Locale.ROOT);
    }

    private String kind(org.netbeans.api.lsp.Diagnostic.Severity severity) {
        switch (severity) {
            case Hint:
                return CodeActionKind.RefactorRewrite;
            default:
                return CodeActionKind.QuickFix;
        }
    }

    private FileObject fromURI(String uri) {
        return fromURI(uri, false);
    }
    
    /**
     * Converts URI to a FileObject. Can try harder using refresh on the filesystem, useful
     * for didOpen or didClose notification which may have been fired because the client changed files on the dist.
     * @param uri the uri
     * @param tryHard if true, will refresh the filesystems first.
     * @return FileObject or null if missing.
     */
    private FileObject fromURI(String uri, boolean tryHard) {
        try {
            FileObject file = Utils.fromUri(uri);
            if (tryHard) {
                if (file != null ) {
                    file.refresh(true);
                } else {
                    URI parentU = BaseUtilities.normalizeURI(URI.create(uri).resolve(".."));
                    FileObject parentF = Utils.fromUri(parentU.toString());
                    if (parentF != null) {
                        parentF.refresh(true);
                        file = Utils.fromUri(uri);
                    }
                }
            }
            if (file != null && file.isValid()) {
                return file;
            }
            missingFileDiscovered(uri);
        } catch (MalformedURLException ex) {
            if (!uri.startsWith("untitled:")) {
                LOG.log(Level.WARNING, "Invalid file URL: " + uri, ex);
            }
        }
        return null;
    }
    
    /**
     * Handles file disappearance. The method should be called whenever a file that ought to exist 
     * is not found on the disk. The method should fire any necessary updates to the client to synchronize the
     * state, i.e. remove obsolete diagnostics for the file.
     * @param uri file URI
     */
    private void missingFileDiscovered(String uri) {
        if (server.getOpenedDocuments().getDocument(uri) != null) {
            // do not report anything, the document is still opened in the editor
            return;
        }
        Instant last = knownFiles.remove(uri);
        if (last == null) {
            return;
        }
        client.publishDiagnostics(new PublishDiagnosticsParams(uri, new ArrayList<>()));
    }
    
    /**
     * Publishes diagnostics to the client. Remembers the file, so that if the file vanishes (i.e. event is received, or
     * it's reported as missing during some processing), the client will get an empty diagnostics message for this file to
     * clear out its problem view.
     * @param uri file URI
     * @param mergedDiags the diagnostics
     */
    private void publishDiagnostics(String uri, List<Diagnostic> mergedDiags) {
        knownFiles.put(uri, Instant.now());
        client.publishDiagnostics(new PublishDiagnosticsParams(uri, mergedDiags));
    }
    
    private static final String[] ERROR_KEYS = {"errors", "hints"};

    private interface ProduceErrors {
        public List<ErrorDescription> computeErrors(CompilationInfo info, Document doc) throws IOException;
    }

    @CheckForNull
    public JavaSource getJavaSource(String fileUri) {
        Document doc = server.getOpenedDocuments().getDocument(fileUri);
        if (doc == null) {
            FileObject file = fromURI(fileUri);
            if (file == null) {
                return null;
            }
            return JavaSource.forFileObject(file);
        } else {
            return JavaSource.forDocument(doc);
        }
    }

    @CheckForNull
    public Source getSource(String fileUri) {
        Document doc = server.getOpenedDocuments().getDocument(fileUri);
        if (doc == null) {
            FileObject file = fromURI(fileUri);
            if (file == null) {
                return null;
            }
            return Source.create(file);
        } else {
            return Source.create(doc);
        }
    }

    public static List<TextEdit> modify2TextEdits(JavaSource js, Task<WorkingCopy> task) throws IOException {//TODO: is this still used?
        FileObject[] file = new FileObject[1];
        LineMap[] lm = new LineMap[1];
        ModificationResult changes = js.runModificationTask(wc -> {
            task.run(wc);
            file[0] = wc.getFileObject();
            lm[0] = wc.getCompilationUnit().getLineMap();
        });
        return fileModifications(changes, file[0], lm[0]);
    }
    
    private static List<TextEdit> fileModifications(ModificationResult changes, FileObject file, LineMap lm) {
        //TODO: full, correct and safe edit production:
        List<? extends ModificationResult.Difference> diffs = changes.getDifferences(file);
        if (diffs == null) {
            return Collections.emptyList();
        }
        List<TextEdit> edits = new ArrayList<>();
        IntFunction<Position> offset2Position = lm != null ? pos -> Utils.createPosition(lm, pos)
                                                           : pos -> Utils.createPosition(file, pos);
                                            
        for (ModificationResult.Difference diff : diffs) {
            String newText = diff.getNewText();
            edits.add(new TextEdit(new Range(offset2Position.apply(diff.getStartPosition().getOffset()),
                                             offset2Position.apply(diff.getEndPosition().getOffset())),
                                   newText != null ? newText : ""));
        }
        return edits;
    }

    private static String getResourceName(TypeElement te, ElementHandle<?> handle) {
        String qualifiedName = null;
        if (te != null) {
            qualifiedName = te.getQualifiedName().toString();
        } else if (handle != null && (handle.getKind().isClass() || handle.getKind().isInterface())) {
            qualifiedName = handle.getQualifiedName();
        }
        return qualifiedName != null ? qualifiedName.replace('.', '/') + ".class" : null;
    }

    private static long documentVersion(Document doc) {
        Object ver = doc != null ? doc.getProperty("version") : null;
        return ver instanceof Number ? ((Number) ver).longValue() : -1;
    }

    private static void reportNotificationDone(String s, Object parameter) {
        if (HOOK_NOTIFICATION != null) {
            HOOK_NOTIFICATION.accept(s, parameter);
        }
    }

    /**
     * For testing only; calls that do not return a result should call
     * this hook, if defined, with the method name and parameter.
     */
    static BiConsumer<String, Object> HOOK_NOTIFICATION = null;
}
