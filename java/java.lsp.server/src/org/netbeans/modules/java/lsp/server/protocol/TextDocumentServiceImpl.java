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
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
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
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lsp.Completion;
import org.netbeans.api.lsp.HyperlinkLocation;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.editor.java.GoToSupport;
import org.netbeans.modules.editor.java.GoToSupport.Context;
import org.netbeans.modules.editor.java.GoToSupport.GoToTarget;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController.TestMethod;
import org.netbeans.modules.java.completion.JavaDocumentationTask;
import org.netbeans.modules.java.editor.base.fold.JavaElementFoldVisitor;
import org.netbeans.modules.java.editor.base.fold.JavaElementFoldVisitor.FoldCreator;
import org.netbeans.modules.java.editor.base.semantic.MarkOccurrencesHighlighterBase;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.java.editor.options.MarkOccurencesSettings;
import org.netbeans.modules.java.editor.overridden.ComputeOverriders;
import org.netbeans.modules.java.editor.overridden.ComputeOverriding;
import org.netbeans.modules.java.editor.overridden.ElementDescription;
import org.netbeans.modules.java.hints.errors.CreateFixBase;
import org.netbeans.modules.java.hints.errors.ImportClass;
import org.netbeans.modules.java.hints.infrastructure.CreatorBasedLazyFixList;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.netbeans.modules.java.hints.introduce.IntroduceFixBase;
import org.netbeans.modules.java.hints.introduce.IntroduceHint;
import org.netbeans.modules.java.hints.introduce.IntroduceKind;
import org.netbeans.modules.java.hints.project.IncompleteClassPath;
import org.netbeans.modules.java.hints.spiimpl.JavaFixImpl;
import org.netbeans.modules.java.hints.spiimpl.hints.HintsInvoker;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.modules.java.lsp.server.LspServerState;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.debugging.utils.ErrorUtilities;
import org.netbeans.modules.java.testrunner.ui.spi.ComputeTestMethods;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.indexing.implspi.ActiveDocumentProvider.IndexingAware;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.api.impl.APIAccessor;
import org.netbeans.modules.refactoring.api.impl.SPIAccessor;
import org.netbeans.modules.refactoring.java.spi.hooks.JavaModificationResult;
import org.netbeans.modules.refactoring.plugins.FileRenamePlugin;
import org.netbeans.modules.refactoring.spi.RefactoringCommit;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class TextDocumentServiceImpl implements TextDocumentService, LanguageClientAware {
    private static final Logger LOG = Logger.getLogger(TextDocumentServiceImpl.class.getName());
    
    private static final RequestProcessor BACKGROUND_TASKS = new RequestProcessor(TextDocumentServiceImpl.class.getName(), 1, false, false);
    private static final RequestProcessor WORKER = new RequestProcessor(TextDocumentServiceImpl.class.getName(), 1, false, false);

    /**
     * File URIs touched / queried by the client.
     */
    private Map<String, Instant> knownFiles = new HashMap<>();
    
    /**
     * Documents actually opened by the client.
     */
    private final Map<String, Document> openedDocuments = new ConcurrentHashMap<>();
    private final Map<String, RequestProcessor.Task> diagnosticTasks = new HashMap<>();
    private final LspServerState server;
    private NbCodeLanguageClient client;

    TextDocumentServiceImpl(LspServerState server) {
        this.server = server;
        Lookup.getDefault().lookup(RefreshDocument.class).register(this);
    }

    private void reRunDiagnostics() {
        Set<String> documents = new HashSet<>(openedDocuments.keySet());

        for (String doc : documents) {
            runDiagnoticTasks(doc);
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
            final int caret = Utils.getOffset(doc, params.getPosition());
            List<CompletionItem> items = new ArrayList<>();
            Completion.Context context = params.getContext() != null
                    ? new Completion.Context(Completion.TriggerKind.valueOf(params.getContext().getTriggerKind().name()),
                            params.getContext().getTriggerCharacter() == null || params.getContext().getTriggerCharacter().isEmpty() ? null : params.getContext().getTriggerCharacter().charAt(0))
                    : null;
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
                    item.setTextEdit(new TextEdit(new Range(Utils.createPosition(file, edit.getStartOffset()), Utils.createPosition(file, edit.getEndOffset())), edit.getNewText()));
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
            completionList.setItems(items);
            return CompletableFuture.completedFuture(Either.forRight(completionList));
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

    private static final RequestProcessor JAVADOC_WORKER = new RequestProcessor(TextDocumentServiceImpl.class.getName() + ".javadoc", 1);

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
                    JAVADOC_WORKER.post(() -> {
                        if (completion.getDetail() != null) {
                            try {
                                String detail = completion.getDetail().get();
                                if (detail != null) {
                                    ci.setDetail(detail);
                                }
                            } catch (Exception ex) {
                            }
                        }
                        if (completion.getDocumentation() != null) {
                            try {
                                String documentation = completion.getDocumentation().get();
                                if (documentation != null) {
                                    MarkupContent markup = new MarkupContent();
                                    markup.setKind("markdown");
                                    markup.setValue(html2MD(documentation));
                                    ci.setDocumentation(markup);
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
        try {
            String uri = params.getTextDocument().getUri();
            FileObject file = fromURI(uri);
            if (file == null) {
                return CompletableFuture.completedFuture(null);
            }
            EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
            Document doc = ec.openDocument();
            final JavaDocumentationTask<Future<String>> task = JavaDocumentationTask.create(Utils.getOffset(doc, params.getPosition()), null, new JavaDocumentationTask.DocumentationFactory<Future<String>>() {
                @Override
                public Future<String> create(CompilationInfo compilationInfo, Element element, Callable<Boolean> cancel) {
                    return ElementJavadoc.create(compilationInfo, element, cancel).getTextAsync();
                }
            }, () -> false);
            ParserManager.parse(Collections.singletonList(Source.create(doc)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    task.run(resultIterator);
                }
            });
            Future<String> futureJavadoc = task.getDocumentation();
            CompletableFuture<Hover> result = new CompletableFuture<Hover>() {
                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    return futureJavadoc != null && futureJavadoc.cancel(mayInterruptIfRunning) && super.cancel(mayInterruptIfRunning);
                }
            };
            JAVADOC_WORKER.post(() -> {
                try {
                    String javadoc = futureJavadoc != null ? futureJavadoc.get() : null;
                    if (javadoc != null) {
                        MarkupContent markup = new MarkupContent();
                        markup.setKind("markdown");
                        markup.setValue(html2MD(javadoc));
                        result.complete(new Hover(markup));
                    } else {
                        result.complete(null);
                    }
                } catch (ExecutionException | InterruptedException ex) {
                    result.completeExceptionally(ex);
                }
            });
            return result;
        } catch (IOException | ParseException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public CompletableFuture<SignatureHelp> signatureHelp(SignatureHelpParams params) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(DefinitionParams params) {
        try {
            String uri = params.getTextDocument().getUri();
            Document doc = openedDocuments.get(uri);
            if (doc != null) {
                FileObject file = Utils.fromUri(uri);
                if (file != null) {
                    int offset = Utils.getOffset(doc, params.getPosition());
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
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> implementation(ImplementationParams params) {
        // shortcut: if the projects are not yet initialized, return empty:
        if (server.openedProjects().getNow(null) == null) {
            return CompletableFuture.completedFuture(Either.forLeft(Collections.emptyList()));
        }
        String uri = params.getTextDocument().getUri();
        JavaSource js = getJavaSource(uri);
        if (js == null) {
            return CompletableFuture.completedFuture(Either.forLeft(Collections.emptyList()));
        }
        List<GoToTarget> targets = new ArrayList<>();
        LineMap[] thisFileLineMap = new LineMap[1];
        try {
            js.runUserActionTask(cc -> {
                cc.toPhase(JavaSource.Phase.RESOLVED);
                Document doc = cc.getSnapshot().getSource().getDocument(true);
                int offset = Utils.getOffset(doc, params.getPosition());
                Element el = null;
                Context context = GoToSupport.resolveContext(cc, doc, offset, false, false);
                if (context == null) {
                    TreePath tp = cc.getTreeUtilities().pathFor(offset);
                    if (tp.getLeaf().getKind() == Kind.MODIFIERS) tp = tp.getParentPath();
                    int[] elementNameSpan = null;
                    switch (tp.getLeaf().getKind()) {
                        case ANNOTATION_TYPE:
                        case CLASS:
                        case ENUM:
                        case INTERFACE:
                            elementNameSpan = cc.getTreeUtilities().findNameSpan((ClassTree) tp.getLeaf());
                            break;
                        case METHOD:
                            elementNameSpan = cc.getTreeUtilities().findNameSpan((MethodTree) tp.getLeaf());
                            break;
                    }
                    if (elementNameSpan != null && offset <= elementNameSpan[1]) {
                        el = cc.getTrees().getElement(tp);
                    }
                } else if (EnumSet.of(ElementKind.METHOD, ElementKind.ANNOTATION_TYPE, ElementKind.CLASS, ElementKind.ENUM, ElementKind.INTERFACE).contains(context.resolved.getKind())) {
                    el = context.resolved;
                }
                if (el != null) {
                    TypeElement type = el.getKind() == ElementKind.METHOD ? (TypeElement) el.getEnclosingElement() : (TypeElement) el;
                    ExecutableElement method = el.getKind() == ElementKind.METHOD ? (ExecutableElement) el : null;
                    Map<ElementHandle<? extends Element>, List<ElementDescription>> overriding = new ComputeOverriders(new AtomicBoolean()).process(cc, type, method, true);
                    List<ElementDescription> overridingMethods = overriding != null ? overriding.get(ElementHandle.create(el)) : null;
                    if (overridingMethods != null) {
                        for (ElementDescription ed : overridingMethods) {
                            Element elm = ed.getHandle().resolve(cc);
                            TreePath tp = cc.getTrees().getPath(elm);
                            long startPos = tp != null && cc.getCompilationUnit() == tp.getCompilationUnit() ? cc.getTrees().getSourcePositions().getStartPosition(cc.getCompilationUnit(), tp.getLeaf()) : -1;
                            if (startPos >= 0) {
                                long endPos = cc.getTrees().getSourcePositions().getEndPosition(cc.getCompilationUnit(), tp.getLeaf());
                                targets.add(new GoToTarget(cc.getSnapshot().getOriginalOffset((int) startPos),
                                        cc.getSnapshot().getOriginalOffset((int) endPos), GoToSupport.getNameSpan(tp.getLeaf(), cc.getTreeUtilities()),
                                        null, null, null, ed.getDisplayName(), true));
                            } else {
                                TypeElement te = elm != null ? cc.getElementUtilities().outermostTypeElement(elm) : null;
                                targets.add(new GoToTarget(-1, -1, null, ed.getOriginalCPInfo(), ed.getHandle(), getResourceName(te, ed.getHandle()), ed.getDisplayName(), true));
                            }
                        }
                    }
                }
                thisFileLineMap[0] = cc.getCompilationUnit().getLineMap();
            }, true);
        } catch (IOException ex) {
            //TODO: include stack trace:
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
            return Either.forLeft(locations);
        });
    }

    @Override
    public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
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
            JavaSource js = getJavaSource(params.getTextDocument().getUri());
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
                    TreePath path = cc.getTreeUtilities().pathFor(Utils.getOffset(doc, params.getPosition()));
                    if (params.getContext().isIncludeDeclaration()) {
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
                    query[0] = new WhereUsedQuery(Lookups.singleton(TreePathHandle.create(path, cc)));
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
                    query[0].getContext().add(org.netbeans.modules.refactoring.api.Scope.create(sourceRoots, null, null));
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
                    locations.add(new Location(Utils.toUri(re.getParentFile()), toRange(re.getPosition())));
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
        return new Range(new Position(bounds.getBegin().getLine(),
                                      bounds.getBegin().getColumn()),
                         new Position(bounds.getEnd().getLine(),
                                      bounds.getEnd().getColumn()));
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
                int offset = Utils.getOffset(doc, params.getPosition());
                List<int[]> spans = new MOHighligther().processImpl(cc, node, doc, offset);
                if (spans != null) {
                    for (int[] span : spans) {
                        result.add(new DocumentHighlight(new Range(Utils.createPosition(cc.getCompilationUnit(), span[0]),
                                                                   Utils.createPosition(cc.getCompilationUnit(), span[1]))));
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
        // shortcut: if the projects are not yet initialized, return empty:
        if (server.openedProjects().getNow(null) == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        JavaSource js = getJavaSource(params.getTextDocument().getUri());
        if (js == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        List<Either<SymbolInformation, DocumentSymbol>> result = new ArrayList<>();
        try {
            js.runUserActionTask(cc -> {
                cc.toPhase(JavaSource.Phase.RESOLVED);
                for (Element tel : cc.getTopLevelElements()) {
                    DocumentSymbol ds = element2DocumentSymbol(cc, tel);
                    if (ds != null)
                        result.add(Either.forRight(ds));
                }
            }, true);
        } catch (IOException ex) {
            //TODO: include stack trace:
            client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
        }

        return CompletableFuture.completedFuture(result);
    }

    private DocumentSymbol element2DocumentSymbol(CompilationInfo info, Element el) throws BadLocationException {
        TreePath path = info.getTrees().getPath(el);
        if (path == null)
            return null;
        Range range = Utils.treeRange(info, path.getLeaf());
        if (range == null)
            return null;
        List<DocumentSymbol> children = new ArrayList<>();
        for (Element c : el.getEnclosedElements()) {
            DocumentSymbol ds = element2DocumentSymbol(info, c);
            if (ds != null) {
                children.add(ds);
            }
        }

        String simpleName;

        if (el.getKind() == ElementKind.CONSTRUCTOR) {
            simpleName = el.getEnclosingElement().getSimpleName().toString();
        } else {
            simpleName = el.getSimpleName().toString();
        }

        return new DocumentSymbol(simpleName, Utils.elementKind2SymbolKind(el.getKind()), range, range, null, children);
    }

    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
        // shortcut: if the projects are not yet initialized, return empty:
        if (server.openedProjects().getNow(null) == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        Document doc = openedDocuments.get(params.getTextDocument().getUri());
        if (doc == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        JavaSource js = JavaSource.forDocument(doc);
        if (js == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        Map<String, ErrorDescription> id2Errors = (Map<String, ErrorDescription>) doc.getProperty("lsp-errors");
        List<Either<Command, CodeAction>> result = new ArrayList<>();
        if (id2Errors != null) {
        for (Diagnostic diag : params.getContext().getDiagnostics()) {
            ErrorDescription err = id2Errors.get(diag.getCode().getLeft());

            if (err == null) {
                client.logMessage(new MessageParams(MessageType.Log, "Cannot resolve error, code: " + diag.getCode().getLeft()));
                continue;
            }

            TreePathHandle[] topLevelHandle = new TreePathHandle[1];
            LazyFixList lfl = err.getFixes();

            if (lfl instanceof CreatorBasedLazyFixList) {
                try {
                    js.runUserActionTask(cc -> {
                        cc.toPhase(JavaSource.Phase.RESOLVED);
                        ((CreatorBasedLazyFixList) lfl).compute(cc, new AtomicBoolean());
                        topLevelHandle[0] = TreePathHandle.create(new TreePath(cc.getCompilationUnit()), cc);
                    }, true);
                } catch (IOException ex) {
                    //TODO: include stack trace:
                    client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
                }
            }
            List<Fix> fixes = sortFixes(lfl.getFixes());

            //TODO: ordering

            for (Fix f : fixes) {
                if (f instanceof IncompleteClassPath.ResolveFix) {
                    CodeAction action = new CodeAction(f.getText());
                    action.setDiagnostics(Collections.singletonList(diag));
                    action.setKind(CodeActionKind.QuickFix);
                    action.setCommand(new Command(f.getText(), Server.JAVA_BUILD_WORKSPACE));
                    result.add(Either.forRight(action));
                }
                if (f instanceof ImportClass.FixImport) {
                    //TODO: FixImport is not a JavaFix, create one. Is there a better solution?
                    String text = f.getText();
                    CharSequence sortText = ((ImportClass.FixImport) f).getSortText();
                    ElementHandle<Element> toImport = ((ImportClass.FixImport) f).getToImport();
                    f = new JavaFix(topLevelHandle[0], sortText != null ? sortText.toString() : null) {
                        @Override
                        protected String getText() {
                            return text;
                        }
                        @Override
                        protected void performRewrite(JavaFix.TransformationContext ctx) throws Exception {
                            Element resolved = toImport.resolve(ctx.getWorkingCopy());
                            if (resolved == null) {
                                return ;
                            }
                            WorkingCopy copy = ctx.getWorkingCopy();
                            CompilationUnitTree cut = GeneratorUtilities.get(copy).addImports(
                                copy.getCompilationUnit(),
                                Collections.singleton(resolved)
                            );
                            copy.rewrite(copy.getCompilationUnit(), cut);
                        }
                    }.toEditorFix();
                }
                if (f instanceof JavaFixImpl) {
                    try {
                        JavaFix jf = ((JavaFixImpl) f).jf;
                        List<TextEdit> edits = modify2TextEdits(js, wc -> {
                            wc.toPhase(JavaSource.Phase.RESOLVED);
                            Map<FileObject, byte[]> resourceContentChanges = new HashMap<FileObject, byte[]>();
                            JavaFixImpl.Accessor.INSTANCE.process(jf, wc, true, resourceContentChanges, /*Ignored in editor:*/new ArrayList<>());
                        });
                        TextDocumentEdit te = new TextDocumentEdit(new VersionedTextDocumentIdentifier(params.getTextDocument().getUri(),
                                                                                                       -1),
                                                                   edits);
                        CodeAction action = new CodeAction(f.getText());
                        action.setDiagnostics(Collections.singletonList(diag));
                        action.setKind(CodeActionKind.QuickFix);
                        action.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(te))));
                        result.add(Either.forRight(action));
                    } catch (IOException ex) {
                        //TODO: include stack trace:
                        client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
                    }
                }
                if (f instanceof CreateFixBase) {
                    try {
                        CreateFixBase cf = (CreateFixBase) f;
                        ModificationResult changes = cf.getModificationResult();
                        List<Either<TextDocumentEdit, ResourceOperation>> documentChanges = new ArrayList<>();
                        Set<File> newFiles = changes.getNewFiles();
                        if (newFiles.size() > 1) {
                            throw new IllegalStateException();
                        }
                        String newFilePath = null;
                        for (File newFile : newFiles) {
                            newFilePath = newFile.getPath();
                            documentChanges.add(Either.forRight(new CreateFile(newFilePath)));
                        }
                        outer: for (FileObject fileObject : changes.getModifiedFileObjects()) {
                            List<? extends ModificationResult.Difference> diffs = changes.getDifferences(fileObject);
                            if (diffs != null) {
                                List<TextEdit> edits = new ArrayList<>();
                                for (ModificationResult.Difference diff : diffs) {
                                    String newText = diff.getNewText();
                                    if (diff.getKind() == ModificationResult.Difference.Kind.CREATE) {
                                        if (newFilePath != null) {
                                            documentChanges.add(Either.forLeft(new TextDocumentEdit(new VersionedTextDocumentIdentifier(newFilePath, -1),
                                                    Collections.singletonList(new TextEdit(new Range(Utils.createPosition(fileObject, 0), Utils.createPosition(fileObject, 0)),
                                                            newText != null ? newText : "")))));
                                        }
                                        continue outer;
                                    } else {
                                        edits.add(new TextEdit(new Range(Utils.createPosition(fileObject, diff.getStartPosition().getOffset()),
                                                                         Utils.createPosition(fileObject, diff.getEndPosition().getOffset())),
                                                               newText != null ? newText : ""));
                                    }
                                }
                                documentChanges.add(Either.forLeft(new TextDocumentEdit(new VersionedTextDocumentIdentifier(Utils.toUri(fileObject), -1), edits)));
                            }
                        }
                        if (!documentChanges.isEmpty()) {
                            CodeAction codeAction = new CodeAction(f.getText());
                            codeAction.setKind(CodeActionKind.QuickFix);
                            codeAction.setEdit(new WorkspaceEdit(documentChanges));
                            result.add(Either.forRight(codeAction));
                        }
                    } catch (IOException ex) {
                        client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
                    }
                }
            }
        }
        }

        try {
            js.runUserActionTask(cc -> {
                cc.toPhase(JavaSource.Phase.RESOLVED);
                //code generators:
                for (CodeGenerator codeGenerator : Lookup.getDefault().lookupAll(CodeGenerator.class)) {
                    for (CodeAction codeAction : codeGenerator.getCodeActions(cc, params)) {
                        result.add(Either.forRight(codeAction));
                    }
                }
                //introduce hints
                Range range = params.getRange();
                if (!range.getStart().equals(range.getEnd())) {
                    for (ErrorDescription err : IntroduceHint.computeError(cc, Utils.getOffset(doc, range.getStart()), Utils.getOffset(doc, range.getEnd()), new EnumMap<IntroduceKind, Fix>(IntroduceKind.class), new EnumMap<IntroduceKind, String>(IntroduceKind.class), new AtomicBoolean())) {
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
            }, true);
        } catch (IOException ex) {
            //TODO: include stack trace:
            client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
        }

        return CompletableFuture.completedFuture(result);
    }

    //TODO: copied from spi.editor.hints/.../FixData:
    private List<Fix> sortFixes(Collection<Fix> fixes) {
        List<Fix> result = new ArrayList<Fix>(fixes);

        Collections.sort(result, new FixComparator());

        return result;
    }

    private static final String DEFAULT_SORT_TEXT = "\uFFFF";

    private static CharSequence getSortText(Fix f) {
        if (f instanceof EnhancedFix) {
            return ((EnhancedFix) f).getSortText();
        } else {
            return DEFAULT_SORT_TEXT;
        }
    }
    private static final class FixComparator implements Comparator<Fix> {
        public int compare(Fix o1, Fix o2) {
            return compareText(getSortText(o1), getSortText(o2));
        }
    }

    private static int compareText(CharSequence text1, CharSequence text2) {
        int len = Math.min(text1.length(), text2.length());
        for (int i = 0; i < len; i++) {
            char ch1 = text1.charAt(i);
            char ch2 = text2.charAt(i);
            if (ch1 != ch2) {
                return ch1 - ch2;
            }
        }
        return text1.length() - text2.length();
    }
    //end copied

    private ConcurrentHashMap<String, Boolean> upToDateTests = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
        // shortcut: if the projects are not yet initialized, return empty:
        if (server.openedProjects().getNow(null) == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        String uri = params.getTextDocument().getUri();
        JavaSource source = getJavaSource(uri);
        if (source == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        CompletableFuture<List<? extends CodeLens>> result = new CompletableFuture<>();
        try {
            source.runUserActionTask(cc -> {
                cc.toPhase(Phase.ELEMENTS_RESOLVED);
                //look for test methods:
                if (!upToDateTests.getOrDefault(uri, Boolean.FALSE)) {
                    List<TestMethod> testMethods = new ArrayList<>();
                    for (ComputeTestMethods.Factory methodsFactory : Lookup.getDefault().lookupAll(ComputeTestMethods.Factory.class)) {
                        testMethods.addAll(methodsFactory.create().computeTestMethods(cc));
                    }
                    if (!testMethods.isEmpty()) {
                        String testClassName = null;
                        List<TestSuiteInfo.TestCaseInfo> tests = new ArrayList<>(testMethods.size());
                        for (TestMethod testMethod : testMethods) {
                            if (testClassName == null) {
                                testClassName = testMethod.getTestClassName();
                            }
                            String id = testMethod.getTestClassName() + ':' + testMethod.method().getMethodName();
                            String fullName = testMethod.getTestClassName() + '.' + testMethod.method().getMethodName();
                            int line = Utils.createPosition(cc.getCompilationUnit(), testMethod.start().getOffset()).getLine();
                            tests.add(new TestSuiteInfo.TestCaseInfo(id, testMethod.method().getMethodName(), fullName, uri, line, TestSuiteInfo.State.Loaded, null));
                        }
                        Integer line = null;
                        Trees trees = cc.getTrees();
                        for (Tree tree : cc.getCompilationUnit().getTypeDecls()) {
                            Element element = trees.getElement(trees.getPath(cc.getCompilationUnit(), tree));
                            if (element != null && element.getKind().isClass() && ((TypeElement)element).getQualifiedName().contentEquals(testClassName)) {
                                line = Utils.createPosition(cc.getCompilationUnit(), (int)trees.getSourcePositions().getStartPosition(cc.getCompilationUnit(), tree)).getLine();
                                break;
                            }
                        }
                        client.notifyTestProgress(new TestProgressParams(uri, new TestSuiteInfo(testClassName, uri, line, TestSuiteInfo.State.Loaded, tests)));
                        upToDateTests.put(uri, Boolean.TRUE);
                    }
                }
                //look for main methods:
                List<CodeLens> lens = new ArrayList<>();
                new TreePathScanner<Void, Void>() {
                    public Void visitMethod(MethodTree tree, Void p) {
                        Element el = cc.getTrees().getElement(getCurrentPath());
                        if (el != null && el.getKind() == ElementKind.METHOD && SourceUtils.isMainMethod((ExecutableElement) el)) {
                            Range range = Utils.treeRange(cc, tree);
                            List<Object> arguments = Collections.singletonList(params.getTextDocument().getUri());
                            lens.add(new CodeLens(range,
                                                  new Command("Run main", "java.run.single", arguments),
                                                  null));
                            lens.add(new CodeLens(range,
                                                  new Command("Debug main", "java.debug.single", arguments),
                                                  null));
                        }
                        return null;
                    }
                }.scan(cc.getCompilationUnit(), null);
                result.complete(lens);
            }, true);
        } catch (IOException ex) {
            result.completeExceptionally(ex);
        }
        return result;
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
            return CompletableFuture.completedFuture(Either.forLeft(null));
        }
        JavaSource source = getJavaSource(params.getTextDocument().getUri());
        if (source == null) {
            return CompletableFuture.completedFuture(Either.forLeft(null));
        }
        CompletableFuture<Either<Range, PrepareRenameResult>> result = new CompletableFuture<>();
        try {
            source.runUserActionTask(cc -> {
                cc.toPhase(JavaSource.Phase.RESOLVED);
                Document doc = cc.getSnapshot().getSource().getDocument(true);
                int pos = Utils.getOffset(doc, params.getPosition());
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
                    TreePath path = cc.getTreeUtilities().pathFor(Utils.getOffset(doc, params.getPosition()));
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
                        resultChanges.add(Either.forLeft(new TextDocumentEdit(new VersionedTextDocumentIdentifier(params.getTextDocument().getUri(), /*XXX*/-1), fileModifications(mr, modified, null))));
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
            openedDocuments.put(params.getTextDocument().getUri(), doc);
            
            // attempt to open the directly owning project, delay diagnostics after project open:
            server.asyncOpenFileOwner(file).thenRun(() ->
                runDiagnoticTasks(params.getTextDocument().getUri())
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
        upToDateTests.put(uri, Boolean.FALSE);
        Document doc = openedDocuments.get(uri);
        if (doc != null) {
            NbDocument.runAtomic((StyledDocument) doc, () -> {
                for (TextDocumentContentChangeEvent change : params.getContentChanges()) {
                    try {
                        int start = Utils.getOffset(doc, change.getRange().getStart());
                        int end   = Utils.getOffset(doc, change.getRange().getEnd());
                        doc.remove(start, end - start);
                        doc.insertString(start, change.getText(), null);
                    } catch (BadLocationException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            });
        }
        runDiagnoticTasks(params.getTextDocument().getUri());
        reportNotificationDone("didChange", params);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        try {
            String uri = params.getTextDocument().getUri();
            upToDateTests.remove(uri);
            // the order here is important ! As the file may cease to exist, it's
            // important that the doucment is already gone form the client.
            openedDocuments.remove(uri);
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
                    int offset = Utils.getOffset(doc, position);
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

    private void runDiagnoticTasks(String uri) {
        if (server.openedProjects().getNow(null) == null) {
            return;
        }
        //XXX: cancelling/deferring the tasks!
        diagnosticTasks.computeIfAbsent(uri, u -> {
            return BACKGROUND_TASKS.create(() -> {
                computeDiags(u, (info, doc) -> {
                    ErrorHintsProvider ehp = new ErrorHintsProvider();
                    return ehp.computeErrors(info, doc, "text/x-java"); //TODO: mimetype?
                }, "errors", false);
                BACKGROUND_TASKS.create(() -> {
                    computeDiags(u, (info, doc) -> {
                        Set<Severity> disabled = org.netbeans.modules.java.hints.spiimpl.Utilities.disableErrors(info.getFileObject());
                        if (disabled.size() == Severity.values().length) {
                            return Collections.emptyList();
                        }
                        return new HintsInvoker(HintsSettings.getGlobalSettings(), new AtomicBoolean()).computeHints(info)
                                                                                                       .stream()
                                                                                                       .filter(ed -> !disabled.contains(ed.getSeverity()))
                                                                                                       .collect(Collectors.toList());
                    }, "hints", true);
                }).schedule(DELAY);
            });
        }).schedule(DELAY);
    }

    private static final int DELAY = 500;

    private void computeDiags(String uri, ProduceErrors produceErrors, String keyPrefix, boolean update) {
        try {
            FileObject file = fromURI(uri);
            if (file == null) {
                // the file does not exist.
                return;
            }
            EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
            Document doc = ec.openDocument();
            ParserManager.parse(Collections.singletonList(Source.create(doc)), new UserTask() {
                @Override
                public void run(ResultIterator it) throws Exception {
                    CompilationController cc = CompilationController.get(it.getParserResult());
                    if (cc != null) {
                        cc.toPhase(JavaSource.Phase.RESOLVED);
                        Map<String, ErrorDescription> id2Errors = new HashMap<>();
                        List<Diagnostic> diags = new ArrayList<>();
                        int idx = 0;
                        List<ErrorDescription> errors = produceErrors.computeErrors(cc, doc);
                        if (errors == null) {
                            errors = Collections.emptyList();
                        }
                        for (ErrorDescription err : errors) {
                            Diagnostic diag = new Diagnostic(new Range(Utils.createPosition(cc.getCompilationUnit(), err.getRange().getBegin().getOffset()),
                                                                       Utils.createPosition(cc.getCompilationUnit(), err.getRange().getEnd().getOffset())),
                                                             err.getDescription());
                            switch (err.getSeverity()) {
                                case ERROR: diag.setSeverity(DiagnosticSeverity.Error); break;
                                case VERIFIER:
                                case WARNING: diag.setSeverity(DiagnosticSeverity.Warning); break;
                                case HINT: diag.setSeverity(DiagnosticSeverity.Hint); break;
                                default: diag.setSeverity(DiagnosticSeverity.Information); break;
                            }
                            String id = keyPrefix + ":" + idx++ + "-" + err.getId();
                            diag.setCode(id);
                            id2Errors.put(id, err);
                            diags.add(diag);
                        }
                        doc.putProperty("lsp-errors-" + keyPrefix, id2Errors);
                        doc.putProperty("lsp-errors-diags-" + keyPrefix, diags);
                        Map<String, ErrorDescription> mergedId2Errors = new HashMap<>();
                        List<Diagnostic> mergedDiags = new ArrayList<>();
                        for (String k : ERROR_KEYS) {
                            Map<String, ErrorDescription> prevErrors = (Map<String, ErrorDescription>) doc.getProperty("lsp-errors-" + k);
                            if (prevErrors != null) {
                                mergedId2Errors.putAll(prevErrors);
                            }
                            List<Diagnostic> prevDiags = (List<Diagnostic>) doc.getProperty("lsp-errors-diags-" + k);
                            if (prevDiags != null) {
                                mergedDiags.addAll(prevDiags);
                            }
                        }
                        doc.putProperty("lsp-errors", mergedId2Errors);
                        doc.putProperty("lsp-errors-diags", mergedDiags);
                        publishDiagnostics(uri, mergedDiags);
                    }
                }
            });
        } catch (IOException | ParseException ex) {
            throw new IllegalStateException(ex);
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
                    URI parentU = URI.create(uri).resolve("..").normalize();
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
            LOG.log(Level.WARNING, "Invalid file URL: " + uri, ex);
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
        if (openedDocuments.get(uri) != null) {
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
        Document doc = openedDocuments.get(fileUri);
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
        Document doc = openedDocuments.get(fileUri);
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

    public static List<TextEdit> modify2TextEdits(JavaSource js, Task<WorkingCopy> task) throws IOException {
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
