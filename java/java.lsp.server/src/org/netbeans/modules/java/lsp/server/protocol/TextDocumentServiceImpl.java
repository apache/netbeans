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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
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
import org.eclipse.lsp4j.CallHierarchyIncomingCall;
import org.eclipse.lsp4j.CallHierarchyIncomingCallsParams;
import org.eclipse.lsp4j.CallHierarchyItem;
import org.eclipse.lsp4j.CallHierarchyOutgoingCall;
import org.eclipse.lsp4j.CallHierarchyOutgoingCallsParams;
import org.eclipse.lsp4j.CallHierarchyPrepareParams;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionItemLabelDetails;
import org.eclipse.lsp4j.CompletionItemTag;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
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
import org.eclipse.lsp4j.FoldingRangeCapabilities;
import org.eclipse.lsp4j.FoldingRangeKind;
import org.eclipse.lsp4j.FoldingRangeRequestParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.ImplementationParams;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintLabelPart;
import org.eclipse.lsp4j.InlayHintParams;
import org.eclipse.lsp4j.InlineValue;
import org.eclipse.lsp4j.InlineValueEvaluatableExpression;
import org.eclipse.lsp4j.InlineValueParams;
import org.eclipse.lsp4j.InlineValueVariableLookup;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PrepareRenameParams;
import org.eclipse.lsp4j.PrepareRenameResult;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameFile;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensCapabilities;
import org.eclipse.lsp4j.SemanticTokensLegend;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.SemanticTokensServerFull;
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
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
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
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
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lsp.CallHierarchyEntry;
import org.netbeans.api.lsp.Completion;
import org.netbeans.api.lsp.HyperlinkLocation;
import org.netbeans.api.lsp.LazyCodeAction;
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
import org.netbeans.modules.java.editor.base.semantic.ColoringAttributes;
import org.netbeans.modules.java.editor.base.semantic.MarkOccurrencesHighlighterBase;
import org.netbeans.modules.java.editor.base.semantic.SemanticHighlighterBase;
import org.netbeans.modules.java.editor.base.semantic.SemanticHighlighterBase.ErrorDescriptionSetter;
import org.netbeans.modules.java.editor.options.MarkOccurencesSettings;
import org.netbeans.modules.java.editor.overridden.ComputeOverriding;
import org.netbeans.modules.java.editor.overridden.ElementDescription;
import org.netbeans.modules.java.hints.OrganizeImports;
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
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.api.WhereUsedQueryConstants;
import org.netbeans.modules.refactoring.java.spi.hooks.JavaModificationResult;
import org.netbeans.modules.refactoring.plugins.FileRenamePlugin;
import org.netbeans.modules.refactoring.spi.RefactoringCommit;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.netbeans.api.lsp.StructureElement;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.java.lsp.server.URITranslator;
import org.netbeans.modules.java.lsp.server.ui.AbstractJavaPlatformProviderOverride;
import org.netbeans.modules.parsing.impl.SourceAccessor;
import org.netbeans.modules.sampler.Sampler;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.lsp.CallHierarchyProvider;
import org.netbeans.spi.lsp.CodeLensProvider;
import org.netbeans.spi.lsp.ErrorProvider;
import org.netbeans.spi.lsp.InlayHintsProvider;
import org.netbeans.spi.lsp.InlineValuesProvider;
import org.netbeans.spi.lsp.StructureProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.modules.Places;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.text.PositionBounds;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class TextDocumentServiceImpl implements TextDocumentService, LanguageClientAware {
    private static final Logger LOG = Logger.getLogger(TextDocumentServiceImpl.class.getName());
    
    private static final String COMMAND_RUN_SINGLE = "nbls.run.single";         // NOI18N
    private static final String COMMAND_DEBUG_SINGLE = "nbls.debug.single";     // NOI18N
    private static final String NETBEANS_INLAY_HINT = "inlay.enabled";   // NOI18N
    private static final String NETBEANS_JAVADOC_LOAD_TIMEOUT = "javadoc.load.timeout";// NOI18N
    private static final String NETBEANS_COMPLETION_WARNING_TIME = "completion.warning.time";// NOI18N
    private static final String NETBEANS_JAVA_ON_SAVE_ORGANIZE_IMPORTS = "java.onSave.organizeImports";// NOI18N
    private static final String NETBEANS_CODE_COMPLETION_COMMIT_CHARS = "java.completion.commit.chars";// NOI18N
    private static final String URL = "url";// NOI18N
    private static final String INDEX = "index";// NOI18N
    
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

    void reRunDiagnostics() {
        for (String doc : server.getOpenedDocuments().getUris()) {
            runDiagnosticTasks(doc, true);
        }
    }

    @ServiceProvider(service=IndexingAware.class, position=0)
    public static final class RefreshDocument implements IndexingAware {

        private final Set<TextDocumentServiceImpl> delegates = Collections.newSetFromMap(new WeakHashMap<>());

        public synchronized void register(TextDocumentServiceImpl delegate) {
            delegates.add(delegate);
        }

        @Override
        public void indexingComplete(Set<URL> indexedRoots) {
            TextDocumentServiceImpl[] delegates;
            synchronized (this) {
                delegates = this.delegates.toArray(new TextDocumentServiceImpl[0]);
            }
            for (TextDocumentServiceImpl delegate : delegates) {
                //augmenting the lookup with NbCodeLanguageClient, so that the
                //correct javac configuration is used for project-less files
                //note this lookup does not contain other services usually present
                //while processing a request, like OperationContext:
                ProxyLookup augmentedLookup = new ProxyLookup(Lookups.fixed(delegate.client), Lookup.getDefault());
                Lookups.executeWith(augmentedLookup, () -> {
                    delegate.reRunDiagnostics();
                });
            }
        }
    }

    private final AtomicInteger javadocTimeout = new AtomicInteger(-1);
    private List<Completion> lastCompletions = null;

    private static final int INITIAL_COMPLETION_SAMPLING_DELAY = 1000;
    private static final int DEFAULT_COMPLETION_WARNING_LENGTH = 10_000;
    private static final RequestProcessor COMPLETION_SAMPLER_WORKER = new RequestProcessor("java-lsp-completion-sampler", 1, false, false);
    private static final AtomicReference<Sampler> RUNNING_SAMPLER = new AtomicReference<>();

    @Override
    @Messages({
        "# {0} - the timeout elapsed",
        "# {1} - path to the saved sampler file",
        "INFO_LongCodeCompletion=Analyze completions taking longer than {0}. A sampler snapshot has been saved to: {1}"
    })
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        AtomicBoolean done = new AtomicBoolean();
        AtomicReference<Sampler> samplerRef = new AtomicReference<>();
        AtomicLong samplingStart = new AtomicLong();
        AtomicLong samplingWarningLength = new AtomicLong(DEFAULT_COMPLETION_WARNING_LENGTH);
        AtomicReference<List<String>> codeCompletionCommitChars = new AtomicReference<>(List.of());
        long completionStart = System.currentTimeMillis();
        COMPLETION_SAMPLER_WORKER.post(() -> {
            if (!done.get()) {
                Sampler sampler = Sampler.createSampler("completion");
                if (sampler != null) {
                    Sampler witnessSampler = RUNNING_SAMPLER.compareAndExchange(null, sampler);

                    if (witnessSampler == null) {
                        sampler.start();
                        samplerRef.set(sampler);
                        samplingStart.set(System.currentTimeMillis());
                        if (done.get()) {
                            sampler.stop();
                        }
                    }
                }
            }
        }, INITIAL_COMPLETION_SAMPLING_DELAY);

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
            Document rawDoc = ec.openDocument();
            if (!(rawDoc instanceof StyledDocument)) {
                return CompletableFuture.completedFuture(Either.forRight(completionList));
            }
            StyledDocument doc = (StyledDocument)rawDoc;
            ConfigurationItem conf = new ConfigurationItem();
            conf.setScopeUri(uri);
            conf.setSection(client.getNbCodeCapabilities().getConfigurationPrefix() + NETBEANS_JAVADOC_LOAD_TIMEOUT);
            ConfigurationItem completionWarningLength = new ConfigurationItem();
            completionWarningLength.setScopeUri(uri);
            completionWarningLength.setSection(client.getNbCodeCapabilities().getConfigurationPrefix() + NETBEANS_COMPLETION_WARNING_TIME);
            ConfigurationItem commitCharacterConfig = new ConfigurationItem();
            commitCharacterConfig.setScopeUri(uri);
            commitCharacterConfig.setSection(client.getNbCodeCapabilities().getConfigurationPrefix() + NETBEANS_CODE_COMPLETION_COMMIT_CHARS);
            return client.configuration(new ConfigurationParams(Arrays.asList(conf, completionWarningLength, commitCharacterConfig))).thenApply(c -> {
                if (c != null && !c.isEmpty()) {
                    if (c.get(0) instanceof JsonPrimitive) {
                        JsonPrimitive javadocTimeSetting = (JsonPrimitive) c.get(0);

                        javadocTimeout.set(javadocTimeSetting.getAsInt());
                    }
                    if (c.get(1) instanceof JsonPrimitive) {
                        JsonPrimitive samplingWarningsLengthSetting = (JsonPrimitive) c.get(1);

                        samplingWarningLength.set(samplingWarningsLengthSetting.getAsLong());
                    }
                    if(c.get(2) instanceof JsonArray){
                        JsonArray commitCharsJsonArray = (JsonArray) c.get(2);
                        codeCompletionCommitChars.set(commitCharsJsonArray.asList().stream().map(ch -> ch.toString()).collect(Collectors.toList()));
                    }
                }
                final int caret = Utils.getOffset(doc, params.getPosition());
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
                        if (completion.getLabelDetail() != null || completion.getLabelDescription() != null) {
                            CompletionItemLabelDetails labelDetails = new CompletionItemLabelDetails();
                            labelDetails.setDetail(completion.getLabelDetail());
                            labelDetails.setDescription(completion.getLabelDescription());
                            item.setLabelDetails(labelDetails);
                        }
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
                        org.netbeans.api.lsp.Command command = completion.getCommand();
                        if (command != null) {
                            item.setCommand(new Command(command.getTitle(), Utils.encodeCommand(command.getCommand(), client.getNbCodeCapabilities()), command.getArguments()));
                        }
                        if (completion.getAdditionalTextEdits() != null && completion.getAdditionalTextEdits().isDone()) {
                            List<org.netbeans.api.lsp.TextEdit> additionalTextEdits = completion.getAdditionalTextEdits().getNow(null);
                            if (additionalTextEdits != null && !additionalTextEdits.isEmpty()) {
                                item.setAdditionalTextEdits(additionalTextEdits.stream().map(ed -> {
                                    return new TextEdit(new Range(Utils.createPosition(file, ed.getStartOffset()), Utils.createPosition(file, ed.getEndOffset())), ed.getNewText());
                                }).collect(Collectors.toList()));
                            }
                        }
                        if (codeCompletionCommitChars.get() != null) {
                            item.setCommitCharacters(codeCompletionCommitChars.get());
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

                    done.set(true);
                    Sampler sampler = samplerRef.get();
                    RUNNING_SAMPLER.compareAndExchange(sampler, null);
                    if (sampler != null) {
                        long samplingTime = (System.currentTimeMillis() - completionStart);
                        long minSamplingTime = Math.min(1_000, samplingWarningLength.get());
                        if (samplingTime >= minSamplingTime &&
                            samplingTime >= samplingWarningLength.get() &&
                            samplingWarningLength.get() >= 0) {
                            Lookup lookup = Lookup.getDefault();
                            new Thread(() -> {
                                Lookups.executeWith(lookup, () -> {
                                    Path logDir = Places.getUserDirectory().toPath().resolve("var/log");
                                    try {
                                        Path target = Files.createTempFile(logDir, "completion-sampler", ".npss");
                                        try (OutputStream out = Files.newOutputStream(target);
                                             DataOutputStream dos = new DataOutputStream(out)) {
                                            sampler.stopAndWriteTo(dos);

                                            NotifyDescriptor notifyUser = new Message(Bundle.INFO_LongCodeCompletion(samplingWarningLength.get(), target.toAbsolutePath().toString()));

                                            DialogDisplayer.getDefault().notifyLater(notifyUser);
                                        }
                                    } catch (IOException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                });
                            }).start();
                        }
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

    private int[] coloring2TokenType;
    private int[] coloring2TokenModifier;

    {
        coloring2TokenType = new int[ColoringAttributes.values().length];
        Arrays.fill(coloring2TokenType, -1);
        coloring2TokenModifier = new int[ColoringAttributes.values().length];
        Arrays.fill(coloring2TokenModifier, -1);
    }

    public void init(ClientCapabilities clientCapabilities, ServerCapabilities severCapabilities) {
        SemanticTokensCapabilities semanticTokens = clientCapabilities != null && clientCapabilities.getTextDocument() != null ? clientCapabilities.getTextDocument().getSemanticTokens() : null;
        if (semanticTokens != null) {
            SemanticTokensWithRegistrationOptions cap = new SemanticTokensWithRegistrationOptions();
            cap.setFull(new SemanticTokensServerFull(false));
            Set<String> knownTokenTypes = semanticTokens.getTokenTypes() != null ? new HashSet<>(semanticTokens.getTokenTypes()) : Collections.emptySet();
            Map<String, Integer> tokenLegend = new LinkedHashMap<>();
            for (Entry<ColoringAttributes, List<String>> e : COLORING_TO_TOKEN_TYPE_CANDIDATES.entrySet()) {
                for (String candidate : e.getValue()) {
                    if (knownTokenTypes.contains(candidate)) {
                        coloring2TokenType[e.getKey().ordinal()] = tokenLegend.computeIfAbsent(candidate, c -> tokenLegend.size());
                        break;
                    }
                }
            }
            Set<String> knownTokenModifiers = semanticTokens.getTokenModifiers() != null ? new HashSet<>(semanticTokens.getTokenModifiers()) : Collections.emptySet();
            Map<String, Integer> modifiersLegend = new LinkedHashMap<>();
            for (Entry<ColoringAttributes, List<String>> e : COLORING_TO_TOKEN_MODIFIERS_CANDIDATES.entrySet()) {
                for (String candidate : e.getValue()) {
                    if (knownTokenModifiers.contains(candidate)) {
                        coloring2TokenModifier[e.getKey().ordinal()] = modifiersLegend.computeIfAbsent(candidate, c -> 1 << modifiersLegend.size());
                        break;
                    }
                }
            }
            SemanticTokensLegend legend = new SemanticTokensLegend(new ArrayList<>(tokenLegend.keySet()), new ArrayList<>(modifiersLegend.keySet()));
            cap.setLegend(legend);
            severCapabilities.setSemanticTokensProvider(cap);
        }
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
                        Preferences prefs = CodeStylePreferences.get(file, "text/x-java").getPreferences();
                        String point = prefs.get("classMemberInsertionPoint", null);
                        try {
                            prefs.put("classMemberInsertionPoint", CodeStyle.InsertionPoint.CARET_LOCATION.name());
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
                        } finally {
                            if (point != null) {
                                prefs.put("classMemberInsertionPoint", point);
                            } else {
                                prefs.remove("classMemberInsertionPoint");
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
        Document rawDoc = server.getOpenedDocuments().getDocument(uri);
        if (file == null || !(rawDoc instanceof StyledDocument)) {
            return CompletableFuture.completedFuture(null);
        }
        StyledDocument doc = (StyledDocument) rawDoc;
        return org.netbeans.api.lsp.Hover.getContent(doc, Utils.getOffset(doc, params.getPosition())).thenApply(content -> {
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
        // shortcut: if the projects are not yet initialized, return empty:
        if (server.openedProjects().getNow(null) == null) {
            return CompletableFuture.completedFuture(null);
        }
        String uri = params.getTextDocument().getUri();
        FileObject file = fromURI(uri);
        Document rawDoc = server.getOpenedDocuments().getDocument(uri);
        if (file == null || !(rawDoc instanceof StyledDocument)) {
            return CompletableFuture.completedFuture(null);
        }
        StyledDocument doc = (StyledDocument) rawDoc;
        List<SignatureInformation> signatures = new ArrayList<>();
        AtomicInteger activeSignature = new AtomicInteger(-1);
        AtomicInteger activeParameter = new AtomicInteger(-1);
        org.netbeans.api.lsp.SignatureInformation.collect(doc, Utils.getOffset(doc, params.getPosition()), null, signature -> {
            SignatureInformation signatureInformation = new SignatureInformation(signature.getLabel());
            List<ParameterInformation> parameters = new ArrayList<>(signature.getParameters().size());
            for (int i = 0; i < signature.getParameters().size(); i++) {
                org.netbeans.api.lsp.SignatureInformation.ParameterInformation parameter = signature.getParameters().get(i);
                ParameterInformation parameterInformation = new ParameterInformation(parameter.getLabel());
                if (parameter.getDocumentation() != null) {
                    MarkupContent markup = new MarkupContent();
                    markup.setKind("markdown");
                    markup.setValue(html2MD(parameter.getDocumentation()));
                    parameterInformation.setDocumentation(markup);
                }
                parameters.add(parameterInformation);
                if (signatureInformation.getActiveParameter() == null && parameter.isActive()) {
                    signatureInformation.setActiveParameter(i);
                }
            }
            if (signature.getDocumentation() != null) {
                MarkupContent markup = new MarkupContent();
                markup.setKind("markdown");
                markup.setValue(html2MD(signature.getDocumentation()));
                signatureInformation.setDocumentation(markup);
            }
            signatureInformation.setParameters(parameters);
            if (activeSignature.get() < 0 && signature.isActive()) {
                activeSignature.set(signatures.size());
                if (signatureInformation.getActiveParameter() != null) {
                    activeParameter.set(signatureInformation.getActiveParameter());
                }
            }
            signatures.add(signatureInformation);
        });
        return CompletableFuture.completedFuture(signatures.isEmpty() ? null : new SignatureHelp(signatures, activeSignature.get(), activeParameter.get()));
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(DefinitionParams params) {
        try {
            String uri = params.getTextDocument().getUri();
            Document rawDoc = server.getOpenedDocuments().getDocument(uri);
            if (rawDoc instanceof StyledDocument) {
                StyledDocument doc = (StyledDocument) rawDoc;
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
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> typeDefinition(TypeDefinitionParams params) {
        try {
            String uri = params.getTextDocument().getUri();
            Document rawDoc = server.getOpenedDocuments().getDocument(uri);
            if (rawDoc instanceof StyledDocument) {
                StyledDocument doc = (StyledDocument)rawDoc;
                FileObject file = Utils.fromUri(uri);
                if (file != null) {
                    int offset = Utils.getOffset(doc, params.getPosition());
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
                    Document rawDoc = cc.getSnapshot().getSource().getDocument(true);
                    if (rawDoc instanceof StyledDocument) {
                        StyledDocument doc = (StyledDocument)rawDoc;
                        TreePath path = cc.getTreeUtilities().pathFor(Utils.getOffset(doc, position));
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
                Document rawDoc = cc.getSnapshot().getSource().getDocument(true);
                if (rawDoc instanceof StyledDocument) {
                    StyledDocument doc = (StyledDocument)rawDoc;
                    int offset = Utils.getOffset(doc, params.getPosition());
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
        final CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> resultFuture = new CompletableFuture<>();
        
        BACKGROUND_TASKS.post(() -> {
            List<Either<SymbolInformation, DocumentSymbol>> result = new ArrayList<>();
            String uri = params.getTextDocument().getUri();
            FileObject file = fromURI(uri);
            Document rawDoc = server.getOpenedDocuments().getDocument(uri);
            if (file != null && rawDoc instanceof StyledDocument) {
                StyledDocument doc = (StyledDocument)rawDoc;
                Collection<? extends StructureProvider> structureProviders = MimeLookup.getLookup(DocumentUtilities.getMimeType(doc)).lookupAll(StructureProvider.class);
                for (StructureProvider structureProvider : structureProviders) {
                    List<StructureElement> structureElements = structureProvider.getStructure(doc);
                    if (!structureElements.isEmpty()) {
                        for (StructureElement structureElement : structureElements) {
                            DocumentSymbol ds = structureElement2DocumentSymbol(doc, structureElement);
                            if (ds != null) {
                                result.add(Either.forRight(ds));
                            }
                        }
                    }
                }
            }
            resultFuture.complete(result);
        });
        return resultFuture;
    }

    static DocumentSymbol structureElement2DocumentSymbol (StyledDocument doc, StructureElement el) {
        Position selectionStartPos = Utils.createPosition(doc, el.getSelectionStartOffset());
        Position selectionEndPos = Utils.createPosition(doc, el.getSelectionEndOffset());
        Range selectionRange = new Range(selectionStartPos, selectionEndPos);
        Position enclosedStartPos = Utils.createPosition(doc, el.getExpandedStartOffset());
        Position enclosedEndPos = Utils.createPosition(doc, el.getExpandedEndOffset());
        Range expandedRange = new Range(enclosedStartPos, enclosedEndPos);
        DocumentSymbol ds;
        if (el.getChildren() != null && !el.getChildren().isEmpty()) {
            List<DocumentSymbol> children = new ArrayList<>();
            for (StructureElement child: el.getChildren()) {
                ds = structureElement2DocumentSymbol(doc, child);
                if (ds != null) {
                    children.add(ds);
                }
            }
            ds = new DocumentSymbol(el.getName(), Utils.structureElementKind2SymbolKind(el.getKind()), expandedRange, selectionRange, el.getDetail(), children);
            ds.setTags(Utils.elementTags2SymbolTags(el.getTags()));
            return ds;
        }
        ds = new DocumentSymbol(el.getName(), Utils.structureElementKind2SymbolKind(el.getKind()), expandedRange, selectionRange, el.getDetail());
        ds.setTags(Utils.elementTags2SymbolTags(el.getTags()));
        return ds;
    }

    private List<LazyCodeAction> lastCodeActions = null;
    
    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
        lastCodeActions = new ArrayList<>();
        AtomicInteger index = new AtomicInteger(0);

        // shortcut: if the projects are not yet initialized, return empty:
        if (server.openedProjects().getNow(null) == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        String uri = params.getTextDocument().getUri();
        Document rawDoc = server.getOpenedDocuments().getDocument(uri);
        if (!(rawDoc instanceof StyledDocument)) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        StyledDocument doc = (StyledDocument)rawDoc;

        List<Either<Command, CodeAction>> result = new ArrayList<>();
        Range range = params.getRange();
        int startOffset = Utils.getOffset(doc, range.getStart());
        int endOffset = Utils.getOffset(doc, range.getEnd());
        Predicate<String> codeActionKindPermitted = Utils.codeActionKindFilter(params.getContext().getOnly());
        if ((startOffset == endOffset || !params.getContext().getDiagnostics().isEmpty()) &&
            (codeActionKindPermitted.test(CodeActionKind.QuickFix) || codeActionKindPermitted.test(CodeActionKind.RefactorRewrite))) {
            final javax.swing.text.Element elem = NbDocument.findLineRootElement(doc);
            int lineStartOffset = elem.getStartOffset();
            int lineEndOffset = elem.getEndOffset();

            ArrayList<Diagnostic> diagnostics = new ArrayList<>(params.getContext().getDiagnostics());
            if (diagnostics.isEmpty()) {
                diagnostics.addAll(computeDiags(params.getTextDocument().getUri(), startOffset, ErrorProvider.Kind.HINTS, documentVersion(doc), null));
            }

            Map<String, org.netbeans.api.lsp.Diagnostic> id2Errors = new HashMap<>();
            for (String key : VALID_ERROR_KEYS) {
                Map<String, org.netbeans.api.lsp.Diagnostic> diags = (Map<String, org.netbeans.api.lsp.Diagnostic>) doc.getProperty("lsp-errors-valid-" + key);
                if (diags != null) {
                    id2Errors.putAll(diags);
                }
            }
            if (!id2Errors.isEmpty()) {
                for (Entry<String, org.netbeans.api.lsp.Diagnostic> entry : id2Errors.entrySet()) {
                    org.netbeans.api.lsp.Diagnostic err = entry.getValue();
                    if (err.getDescription() == null || err.getDescription().isEmpty()) {
                        continue;
                    }
                    if (err.getSeverity() == org.netbeans.api.lsp.Diagnostic.Severity.Error) {
                        if (err.getEndPosition().getOffset() < startOffset || err.getStartPosition().getOffset() > endOffset) {
                            continue;
                        }
                    } else {
                        if (err.getEndPosition().getOffset() < lineStartOffset || err.getStartPosition().getOffset() > lineEndOffset) {
                            continue;
                        }
			int lineStart = NbDocument.findLineNumber(doc, startOffset);
                        int errStartLine = NbDocument.findLineNumber(doc, err.getStartPosition().getOffset());
                        if(errStartLine != lineStart){
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
                            String codeActionKind = kind(err.getSeverity());
                            if (!codeActionKindPermitted.test(codeActionKind)) {
                                continue;
                            }
                            action.setKind(codeActionKind);
                            if (inputAction.getCommand() != null) {
                                List<Object> commandParams = new ArrayList<>();

                                commandParams.add(params.getTextDocument().getUri());

                                if (inputAction.getCommand().getArguments() != null) {
                                    commandParams.addAll(inputAction.getCommand().getArguments());
                                }

                                action.setCommand(new Command(inputAction.getCommand().getTitle(), Utils.encodeCommand(inputAction.getCommand().getCommand(), client.getNbCodeCapabilities()), commandParams));
                            }
                            if (inputAction instanceof LazyCodeAction && ((LazyCodeAction) inputAction).getLazyEdit() != null) {
                                lastCodeActions.add((LazyCodeAction) inputAction);
                                Map<String, Object> data = new HashMap<>();
                                data.put(URL, uri);
                                data.put(INDEX, index.getAndIncrement());
                                action.setData(data);
                            } else if (inputAction.getEdit() != null) {
                                action.setEdit(Utils.workspaceEditFromApi(inputAction.getEdit(), uri, client));
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
                            Set<String> supportedCodeActionKinds = codeGenerator.getSupportedCodeActionKinds();
                            if (supportedCodeActionKinds != null &&
                                supportedCodeActionKinds.stream()
                                                        .noneMatch(kind -> codeActionKindPermitted.test(kind))) {
                                continue;
                            }
                            try {
                                for (CodeAction codeAction : codeGenerator.getCodeActions(client, resultIterator, params)) {
                                    if (!codeActionKindPermitted.test(codeAction.getKind())) {
                                        continue;
                                    }
                                    result.add(Either.forRight(codeAction));
                                }
                            } catch (Exception ex) {
                                client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
                            }
                        }
                    }
                });
            } catch (ParseException ex) {
                StringWriter w = new StringWriter();
                try (PrintWriter pw = new PrintWriter(w)) {
                  ex.printStackTrace(pw);
                }
                client.logMessage(new MessageParams(MessageType.Error, w.toString()));
            } finally {
                resultFuture.complete(result);
            }
        });
        return resultFuture;
    }

    @Override
    public CompletableFuture<CodeAction> resolveCodeAction(CodeAction unresolved) {
        CompletableFuture<CodeAction> future = new CompletableFuture<>();
        BACKGROUND_TASKS.post(() -> {
            JsonObject data = (JsonObject) unresolved.getData();
            if (data != null) {
                if (data.has(CodeActionsProvider.CODE_ACTIONS_PROVIDER_CLASS)) {
                    String providerClass = data.getAsJsonPrimitive(CodeActionsProvider.CODE_ACTIONS_PROVIDER_CLASS).getAsString();
                    for (CodeActionsProvider codeGenerator : Lookup.getDefault().lookupAll(CodeActionsProvider.class)) {
                        if (codeGenerator.getClass().getName().equals(providerClass)) {
                            try {
                                codeGenerator.resolve(client, unresolved, data.get(CodeActionsProvider.DATA)).thenAccept(action -> {
                                    future.complete(action);
                                });
                            } catch (Exception e) {
                                future.completeExceptionally(e);
                            }
                            return;
                        }
                    }
                } else if (data.has(URL) && data.has(INDEX)) {
                    LazyCodeAction inputAction = lastCodeActions.get(data.getAsJsonPrimitive(INDEX).getAsInt());
                    if (inputAction != null) {
                        try {
                            unresolved.setEdit(Utils.workspaceEditFromApi(inputAction.getLazyEdit().get(), data.getAsJsonPrimitive(URL).getAsString(), client));
                        } catch (Exception e) {
                            future.completeExceptionally(e);
                            return;
                        }
                    }
                }
            }
            future.complete(unresolved);
        });
        return future;
    }

    @NbBundle.Messages({"# {0} - method name", "LBL_Run=Run {0}",
                        "# {0} - method name", "LBL_Debug=Debug {0}",
                        "# {0} - method name", "# {1} - configuration name", "LBL_RunWith=Run {0} with {1}",
                        "# {0} - method name", "# {1} - configuration name", "LBL_DebugWith=Debug {0} with {1}"})
    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
        if (!client.getNbCodeCapabilities().wantsJavaSupport()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        // shortcut: if the projects are not yet initialized, return empty:
        if (server.openedProjects().getNow(null) == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        CompletableFuture<List<? extends CodeLens>> result = CompletableFuture.completedFuture(Collections.emptyList());
        try {
            String uri = params.getTextDocument().getUri();
            FileObject file = Utils.fromUri(uri);
            Document doc = server.getOpenedDocuments().getDocument(uri);
            if (file == null || doc == null) {
                return CompletableFuture.completedFuture(Collections.emptyList());
            }
            for (CodeLensProvider provider : MimeLookup.getLookup(DocumentUtilities.getMimeType(doc)).lookupAll(CodeLensProvider.class)) {
                result = result.thenCombine(provider.codeLens(doc), (leftList, rightList) -> mergeLists(leftList, convertCodeLens(doc, rightList)));
            }
        } catch (MalformedURLException ex) {
            client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
        }
        return result;
    }

    private List<CodeLens> convertCodeLens(Document doc, List<? extends org.netbeans.api.lsp.CodeLens> origin) {
        List<CodeLens> result = new ArrayList<>();
        for (org.netbeans.api.lsp.CodeLens len : origin) {
            Command cmd = null;
            if (len.getCommand() != null) {
                cmd = new Command(len.getCommand().getTitle(), Utils.encodeCommand(len.getCommand().getCommand(), client.getNbCodeCapabilities()), len.getCommand().getArguments());
            }
            result.add(new CodeLens(callRange2Range(len.getRange(), doc), cmd, len.getData()));
        }
        return result;
    }
    private List<? extends CodeLens> mergeLists(List<? extends CodeLens> left, List<? extends CodeLens> right) {
        List<CodeLens> result = new ArrayList<>();
        result.addAll(left);
        result.addAll(right);
        return result;
    }

    @MimeRegistration(mimeType="text/x-java", service=CodeLensProvider.class) //TODO: other mime types?
    public static final class MainLens implements CodeLensProvider {

        @Override
        public CompletableFuture<List<? extends org.netbeans.api.lsp.CodeLens>> codeLens(Document doc) {
            Source source = Source.create(doc);
            if (source == null) {
                return CompletableFuture.completedFuture(Collections.emptyList());
            }
            String uri = Utils.toUri(source.getFileObject());
            CompletableFuture<List<? extends org.netbeans.api.lsp.CodeLens>> result = new CompletableFuture<>();
            try {
                ParserManager.parseWhenScanFinished(Collections.singleton(source), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        Parser.Result parserResult = resultIterator.getParserResult();
                        //look for main methods:
                        List<org.netbeans.api.lsp.CodeLens> lens = new ArrayList<>();
                        if (parserResult == null) {
                            // no parser for the sourec type
                            result.complete(lens);
                            return;
                        }
                        CompilationController cc = CompilationController.get(parserResult);
                        if (cc != null) {
                            cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                            AtomicReference<List<Pair<String, String>>> projectConfigurations = new AtomicReference<>();
                            new TreePathScanner<Void, Void>() {
                                public Void visitMethod(MethodTree tree, Void p) {
                                    Element el = cc.getTrees().getElement(getCurrentPath());
                                    if (el != null && el.getKind() == ElementKind.METHOD && SourceUtils.isMainMethod((ExecutableElement) el)) {
                                        int start = (int) cc.getTrees().getSourcePositions().getStartPosition(cc.getCompilationUnit(), tree);
                                        int end = (int) cc.getTrees().getSourcePositions().getEndPosition(cc.getCompilationUnit(), tree);
                                        org.netbeans.api.lsp.Range range = new org.netbeans.api.lsp.Range(start, end);
                                        List<Object> arguments = new ArrayList<>();
                                        arguments.add(uri);
                                        arguments.add(null);
                                        arguments.add("");
                                        String method = el.getSimpleName().toString();
                                        lens.add(new org.netbeans.api.lsp.CodeLens(range,
                                                              new org.netbeans.api.lsp.Command(Bundle.LBL_Run(method), COMMAND_RUN_SINGLE, arguments),
                                                              null));
                                        lens.add(new org.netbeans.api.lsp.CodeLens(range,
                                                              new org.netbeans.api.lsp.Command(Bundle.LBL_Debug(method), COMMAND_DEBUG_SINGLE, arguments),
                                                              null));
                                        // Run and Debug configurations:
                                        List<Pair<String, String>> configs = projectConfigurations.accumulateAndGet(null, (l, nul) -> l == null ? getProjectConfigurations(source) : l);
                                        for (Pair<String, String> config : configs) {
                                            String runConfig = config.first();
                                            if (runConfig != null) {
                                                lens.add(new org.netbeans.api.lsp.CodeLens(range,
                                                                      new org.netbeans.api.lsp.Command(Bundle.LBL_RunWith(method, runConfig), COMMAND_RUN_SINGLE, Arrays.asList(uri, null, runConfig)),
                                                                      null));
                                            }
                                            String debugConfig = config.second();
                                            if (debugConfig != null) {
                                                lens.add(new org.netbeans.api.lsp.CodeLens(range,
                                                                      new org.netbeans.api.lsp.Command(Bundle.LBL_DebugWith(method, debugConfig), COMMAND_DEBUG_SINGLE, Arrays.asList(uri, null, debugConfig)),
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
    }

    private static List<Pair<String, String>> getProjectConfigurations(Source source) {
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
    public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
        String uri = params.getTextDocument().getUri();
        Document doc = server.getOpenedDocuments().getDocument(uri);
        return format(doc, 0, doc.getLength());
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
        String uri = params.getTextDocument().getUri();
        Document rawDoc = server.getOpenedDocuments().getDocument(uri);
        if (rawDoc instanceof StyledDocument) {
            StyledDocument lDoc = (StyledDocument) rawDoc;
            Range range = params.getRange();
            return format(lDoc, Utils.getOffset(lDoc, range.getStart()), Utils.getOffset(lDoc, range.getEnd()));
        }
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    private CompletableFuture<List<? extends TextEdit>> format(Document doc, int startOffset, int endOffset) {
        CompletableFuture<List<? extends TextEdit>> result = new CompletableFuture<>();
        if (doc instanceof StyledDocument) {
            FormatterDocument formDoc = new FormatterDocument((StyledDocument) doc);
            Reformat reformat = Reformat.get(formDoc);
            if (reformat != null) {
                reformat.lock();
                try {
                    reformat.reformat(startOffset, endOffset);
                    result.complete(formDoc.getEdits());
                } catch (BadLocationException ex) {
                    result.completeExceptionally(ex);
                } finally {
                    reformat.unlock();
                }
            } else {
                result.complete(Collections.emptyList());
            }
        } else {
            result.complete(Collections.emptyList());
        }
        return result;
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
                Document rawDoc = cc.getSnapshot().getSource().getDocument(true);
                if (!(rawDoc instanceof StyledDocument)) {
                    result.complete(null);
                    return;
                }
                StyledDocument lDoc = (StyledDocument) rawDoc;
                int pos = Utils.getOffset(lDoc, params.getPosition());
                TokenSequence<JavaTokenId> ts = cc.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                ts.move(pos);
                if (ts.moveNext() && ts.token().id() != JavaTokenId.WHITESPACE && ts.offset() == pos) {
                    pos += 1;
                }
                TreePath path = cc.getTreeUtilities().pathFor(pos);
                RenameRefactoring ref = new RenameRefactoring(Lookups.singleton(TreePathHandle.create(path, cc)));
                ref.getContext().add(JavaRefactoringUtils.getClasspathInfoFor(cc.getFileObject()));
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
                    Document rawDoc = cc.getSnapshot().getSource().getDocument(true);
                    if (rawDoc instanceof StyledDocument) {
                        StyledDocument doc = (StyledDocument) rawDoc;
                        int pos = Utils.getOffset(doc, params.getPosition());
                        TokenSequence<JavaTokenId> ts = cc.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                        ts.move(pos);
                        if (ts.moveNext() && ts.token().id() != JavaTokenId.WHITESPACE && ts.offset() == pos) {
                            pos += 1;
                        }
                        TreePath path = cc.getTreeUtilities().pathFor(pos);
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
                        refactoring[0].getContext().add(JavaRefactoringUtils.getClasspathInfoFor(cc.getFileObject()));
                        refactoring[0].setNewName(params.getNewName());
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
                        String oldURI = Utils.toUri(rei.getParentFile());
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
        ClientCapabilities clientCapabilities = client.getNbCodeCapabilities()
                                                      .getClientCapabilities();
        TextDocumentClientCapabilities textDocumentCapabilities = clientCapabilities != null ? clientCapabilities.getTextDocument() : null;
        FoldingRangeCapabilities foldingRangeCapabilities = textDocumentCapabilities != null ? textDocumentCapabilities.getFoldingRange() : null;
        Boolean lineFoldingOnlyCapability = foldingRangeCapabilities != null ? foldingRangeCapabilities.getLineFoldingOnly() : null;
        final boolean lineFoldingOnly = lineFoldingOnlyCapability == Boolean.TRUE;
        CompletableFuture<List<FoldingRange>> result = new CompletableFuture<>();
        try {
            source.runUserActionTask(cc -> {
                cc.toPhase(JavaSource.Phase.RESOLVED);
                Document doc = cc.getSnapshot().getSource().getDocument(true);
                JavaElementFoldVisitor<FoldingRange> v = new JavaElementFoldVisitor<>(cc, cc.getCompilationUnit(), cc.getTrees().getSourcePositions(), doc, new FoldCreator<FoldingRange>() {
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
                    public FoldingRange createMethodFold(int start, int end) {
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
                List<FoldingRange> folds = v.getFolds();
                if (lineFoldingOnly)
                    folds = convertToLineOnlyFolds(folds);
                result.complete(folds);
            }, true);
        } catch (IOException ex) {
            result.completeExceptionally(ex);
        }
        return result;
    }

    /**
     * Converts a list of code-folds to a line-only Range form, in place of the
     * finer-grained form of {@linkplain Position Position-based} (line, column) Ranges.
     * <p>
     * This is needed for LSP clients that do not support the finer grained Range
     * specification. This is expected to be advertised by the client in
     * {@code FoldingRangeClientCapabilities.lineFoldingOnly}.
     *
     * @implSpec The line-only ranges computed uphold the code-folding invariant that:
     * <em>a fold <b>does not end</b> at the same point <b>where</b> another fold <b>starts</b></em>.
     *
     * @implNote This is performed in {@code O(n log n) + O(n)} time and {@code O(n)} space for the returned list.
     *
     * @param folds List of code-folding ranges computed for a textDocument,
     *              containing fine-grained {@linkplain Position Position-based}
     *              (line, column) ranges.
     * @return List of code-folding ranges computed for a textDocument,
     * containing coarse-grained line-only ranges.
     *
     * @see <a href="https://microsoft.github.io/language-server-protocol/specifications/specification-current/#foldingRangeClientCapabilities">
     *     LSP FoldingRangeClientCapabilities</a>
     */
    static List<FoldingRange> convertToLineOnlyFolds(List<FoldingRange> folds) {
        if (folds != null && folds.size() > 1) {
            // Ensure that the folds are sorted in increasing order of their start position
            folds = new ArrayList<>(folds);
            folds.sort(Comparator.comparingInt(FoldingRange::getStartLine)
                    .thenComparing(FoldingRange::getStartCharacter));
            // Maintain a stack of enclosing folds
            Deque<FoldingRange> enclosingFolds = new ArrayDeque<>();
            for (FoldingRange fold : folds) {
                FoldingRange last;
                while ((last = enclosingFolds.peek()) != null &&
                        (last.getEndLine() < fold.getEndLine() || 
                        (last.getEndLine() == fold.getEndLine() && last.getEndCharacter() < fold.getEndCharacter()))) {
                    // The last enclosingFold does not enclose this fold.
                    // Due to sortedness of the folds, last also ends before this fold starts.
                    enclosingFolds.pop();
                    // If needed, adjust last to end on a line prior to this fold start
                    if (last.getEndLine() == fold.getStartLine()) {
                        last.setEndLine(last.getEndLine() - 1);
                    }
                    last.setEndCharacter(null);       // null denotes the end of the line.
                    last.setStartCharacter(null);     // null denotes the end of the line.
                }
                enclosingFolds.push(fold);
            }
            // empty the stack; since each fold completely encloses the next higher one.
            FoldingRange fold;
            while ((fold = enclosingFolds.poll()) != null) {
                fold.setEndCharacter(null);       // null denotes the end of the line.
                fold.setStartCharacter(null);     // null denotes the end of the line.
            }
            // Remove invalid or duplicate folds
            Iterator<FoldingRange> it = folds.iterator();
            FoldingRange prev = null;
            while(it.hasNext()) {
                FoldingRange next = it.next();
                if (next.getEndLine() <= next.getStartLine() || 
                        (prev != null && prev.equals(next))) {
                    it.remove();
                } else {
                    prev = next;
                }
            }
        }
        return folds;
    }


    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        LOG.log(Level.FINER, "didOpen: {0}", params);
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
                updateDocumentIfNeeded(text, doc);
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

    static void updateDocumentIfNeeded(String text, Document doc) throws BadLocationException {
        String docText = doc.getText(0, doc.getLength());

        if (text.contentEquals(docText)) {
            //the texts are the same, no need to change the Document content:
            return ;
        }

        //normalize line endings:
        StringBuilder newText = new StringBuilder(text.length());
        int len = text.length();
        boolean modified = false;

        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (c == '\r') {
                if (i + 1 < len && text.charAt(i + 1) == '\n') {
                    i++;
                }
                c = '\n';
                modified = true;
            }
            newText.append(c);
        }

        String newTextString = newText.toString();

        if (modified && docText.equals(newTextString)) {
            //only change in line endings, no need to change the Document content:
            return ;
        }

        doc.remove(0, doc.getLength());
        doc.insertString(0, newTextString, null);
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        LOG.log(Level.FINER, "didChange: {0}", params);
        String uri = params.getTextDocument().getUri();
        Document rawDoc = server.getOpenedDocuments().getDocument(uri);
        if (rawDoc != null) {
            StyledDocument doc = (StyledDocument) rawDoc;
            NbDocument.runAtomic(doc, () -> {
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
            for (String key : VALID_ERROR_KEYS) {
                doc.putProperty("lsp-errors-valid-" + key, null);
            }
        }
        runDiagnosticTasks(params.getTextDocument().getUri());
        reportNotificationDone("didChange", params);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        LOG.log(Level.FINER, "didClose: {0}", params);
        try {
            String uri = params.getTextDocument().getUri();
            // the order here is important ! As the file may cease to exist, it's
            // important that the doucment is already gone form the client.
            Document doc = server.getOpenedDocuments().notifyClosed(uri);
            FileObject file = fromURI(uri, true);
            EditorCookie ec = file != null ? file.getLookup().lookup(EditorCookie.class) : null;
            if (ec == null && doc != null) {
                DataObject dObj = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
                if (dObj != null) {
                    ec = dObj.getLookup().lookup(EditorCookie.class);
                }
            }
            if (ec != null) {
                ec.close();
            }
        } finally {
            reportNotificationDone("didClose", params);
        }
    }

    @Override
    public CompletableFuture<List<TextEdit>> willSaveWaitUntil(WillSaveTextDocumentParams params) {
        LOG.log(Level.FINER, "willSaveWaitUntil: {0}", params);
        String uri = params.getTextDocument().getUri();
        JavaSource js = getJavaSource(uri);
        if (js == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        ConfigurationItem conf = new ConfigurationItem();
        conf.setScopeUri(uri);
        conf.setSection(client.getNbCodeCapabilities().getConfigurationPrefix() + NETBEANS_JAVA_ON_SAVE_ORGANIZE_IMPORTS);
        return client.configuration(new ConfigurationParams(Collections.singletonList(conf))).thenApply(c -> {
            if (c != null && !c.isEmpty() && ((JsonPrimitive) c.get(0)).getAsBoolean()) {
                try {
                    List<TextEdit> edits = TextDocumentServiceImpl.modify2TextEdits(js, wc -> {
                        wc.toPhase(JavaSource.Phase.RESOLVED);
                        if (wc.getDiagnostics().isEmpty()) {
                            OrganizeImports.doOrganizeImports(wc, null, false);
                        }
                    });
                    return edits;
                } catch (IOException ex) {}
            }
            return Collections.emptyList();
        });
    }

    @Override
    public void didSave(DidSaveTextDocumentParams savedParams) {
        LOG.log(Level.FINE, "didSave: {0}", savedParams.getTextDocument().getUri());
        FileObject file = fromURI(savedParams.getTextDocument().getUri());
        if (file == null) {
            return;
        }
        // refresh the file systems, potentially fire events
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Refreshing file {0}, timestamp {1}", new Object[] { file, file.lastModified().getTime() });
        }
        file.refresh();
        EditorCookie cake = file.getLookup().lookup(EditorCookie.class);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Refresh done on {0}, timestamp {1}, existing editor: {2}", new Object[] { file, file.lastModified().getTime(), cake });
        }
        if (cake == null) {
            return;
        }
        StyledDocument alreadyLoaded = cake.getDocument();
        if (alreadyLoaded == null) {
            return;
        }
        try {
            // if the FileObject.refresh() only now discovered a change, it have fired an event and initiated a reload, which might
            // be still pending. Grab the reload task and wait for it:
            Method reload = CloneableEditorSupport.class.getDeclaredMethod("reloadDocument");
            reload.setAccessible(true);
            org.openide.util.Task t = (org.openide.util.Task)reload.invoke(cake);
            // wait for a limited time, this could be enough for the reload to complete, blocking LSP queue. We do not want to block LSP queue indefinitely:
            // in case of an error, the server could become unresponsive.
            if (!t.waitFinished(300)) {
                LOG.log(Level.WARNING, "{0}: document reload did not finish in 300ms", file);
            }
        } catch (ReflectiveOperationException | InterruptedException | SecurityException ex) {
            // nop 
        }
    }

    CompletableFuture<List<? extends Location>> superImplementations(String uri, Position position) {
        JavaSource js = getJavaSource(uri);
        List<GoToTarget> targets = new ArrayList<>();
        LineMap[] thisFileLineMap = new LineMap[1];
        try {
            if (js != null) {
                js.runUserActionTask(cc -> {
                    cc.toPhase(JavaSource.Phase.RESOLVED);
                    Document rawDoc = cc.getSnapshot().getSource().getDocument(true);
                    if (rawDoc instanceof StyledDocument) {
                        StyledDocument doc = (StyledDocument) rawDoc;
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
        runDiagnosticTasks(uri, false);
    }

    private void runDiagnosticTasks(String uri, boolean force) {
        if (server.openedProjects().getNow(null) == null) {
            return;
        }

        if (force) {
            Document originalDoc = server.getOpenedDocuments().getDocument(uri);

            if (originalDoc != null) {
                //invalidate the source, so that's it is parsed again:
                SourceAccessor.getINSTANCE().invalidate(Source.create(originalDoc), true);
            }
        }
        if (Lookup.getDefault().lookup(NbCodeLanguageClient.class) == null) {
            new Exception("no NbCodeLanguageClient!").printStackTrace();
        }

        // sync needed - this can be called also from reporterControl, from other that LSP request thread. The factory function just cretaes a stopped
        // Task that is executed later.
        synchronized (diagnosticTasks) {
            diagnosticTasks.computeIfAbsent(uri, u -> {
                return BACKGROUND_TASKS.create(() -> {
                    Document originalDoc = server.getOpenedDocuments().getDocument(uri);
                    long originalVersion = documentVersion(originalDoc);
                    AtomicReference<Document> docHolder = new AtomicReference<>(originalDoc);
                    List<Diagnostic> errorDiags = computeDiags(u, -1, ErrorProvider.Kind.ERRORS, originalVersion, docHolder);
                    if (documentVersion(originalDoc) == originalVersion) {
                        publishDiagnostics(uri, errorDiags);
                        BACKGROUND_TASKS.create(() -> {
                            List<Diagnostic> hintDiags = computeDiags(u, -1, ErrorProvider.Kind.HINTS, originalVersion, docHolder);
                            Document doc = server.getOpenedDocuments().getDocument(uri);
                            if (documentVersion(doc) == originalVersion) {
                                publishDiagnostics(uri, hintDiags);
                            }
                        }).schedule(DELAY);
                    }
                });
            }).schedule(DELAY);
        }
    }
    
    CompletableFuture<List<Diagnostic>> computeDiagnostics(String uri, EnumSet<ErrorProvider.Kind> types) {
        CompletableFuture<List<Diagnostic>> r = new CompletableFuture<>();
        BACKGROUND_TASKS.post(() -> {
            try {
                Document originalDoc = server.getOpenedDocuments().getDocument(uri);
                long originalVersion = documentVersion(originalDoc);
                AtomicReference<Document> docHolder = new AtomicReference<>(originalDoc);
                List<Diagnostic> result = Collections.emptyList();
                if (types.contains(ErrorProvider.Kind.ERRORS)) {
                    result = computeDiags(uri, -1, ErrorProvider.Kind.ERRORS, originalVersion, docHolder);
                }
                if (types.contains(ErrorProvider.Kind.HINTS)) {
                    result = computeDiags(uri, -1, ErrorProvider.Kind.HINTS, originalVersion, docHolder);
                }
                r.complete(result);
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                r.completeExceptionally(t);
            }
        });
        return r;
    }

    private static final int DELAY = 500;
    public boolean hintsSettingsRead = false;
    private FileObject hintsPrefsFile = null;
    
    /**
     * Recomputes a specific kinds of diagnostics for the file, and returns a complete set diagnostics for that
     * file. If the document changes during the computation, the computation aborts and returns an empty list. 
     * It is possible to provide the reference version of the document, any change beyond that is detected.
     * 
     * @param uri the file that should be processed.
     * @param offset offset to compute diagnostics for.
     * @param errorKind the kind of diagnostics to recompute/update
     * @param orgV version of the document. or -1 to obtain the current version.
     * @return complete list of diagnostics for the file.
     */
    private List<Diagnostic> computeDiags(String uri, int offset, ErrorProvider.Kind errorKind, long orgV, AtomicReference<Document> docHolder) {
        List<Diagnostic> result = new ArrayList<>();
        FileObject file = fromURI(uri);
        if (file == null) {
            // the file does not exist.
            return result;
        }
        if(!this.hintsSettingsRead){
            // hints preferences file is not read yet
            return result;
        }
        try {
            String keyPrefix = key(errorKind);
            EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
            Document doc = ec.openDocument();
            if (docHolder != null) {
                docHolder.set(doc);
            }
            long originalVersion = orgV != -1 ? orgV : documentVersion(doc);
            Map<String, org.netbeans.api.lsp.Diagnostic> id2Errors = new HashMap<>();
            Collection<? extends ErrorProvider> errorProviders = MimeLookup.getLookup(DocumentUtilities.getMimeType(doc))
                                                    .lookupAll(ErrorProvider.class);
            List<? extends org.netbeans.api.lsp.Diagnostic> errors;
            if (!errorProviders.isEmpty()) {
                ErrorProvider.Context context = new ErrorProvider.Context(file, offset, errorKind, hintsPrefsFile);
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
                    errors = errorProviders.stream().flatMap(provider -> {
                        List<? extends org.netbeans.api.lsp.Diagnostic> errorsOrNull = provider.computeErrors(context);
                        if (errorsOrNull == null) {
                            errorsOrNull = Collections.emptyList();
                        }
                        return errorsOrNull.stream();
                    }).collect(Collectors.toList());
                } finally {
                    doc.removeDocumentListener(l);
                }
            } else {
                errors = null;
            }
            if (errors == null) {
                errors = Collections.emptyList();
            }
            if (originalVersion != -1 && documentVersion(doc) != originalVersion) {
                return result;
            }
            for (org.netbeans.api.lsp.Diagnostic err : errors) {
                String id = err.getCode();
                id2Errors.put(id, err);
            }
            if (offset < 0) {
                doc.putProperty("lsp-errors-" + keyPrefix, id2Errors);
                doc.putProperty("lsp-errors-valid-" + keyPrefix, id2Errors);
            } else {
                doc.putProperty("lsp-errors-valid-offsetHints", id2Errors);
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
                // TODO: currently Diagnostic.getCode() is misused to provide an unique ID for the diagnostic. This should be changed somehow
                // at SPI level between ErrorProvider and LSP core. For now, report just part of the (mangled) diagnostics code.
                String realCode = id2Error.getKey();
                int idPart = realCode == null ?  -1 : realCode.indexOf("~~"); // NOI18N
                if (idPart != -1) {
                    realCode = realCode.substring(0, idPart);
                }
                diag.setCode(realCode);
                result.add(diag);
            }
            if (offset >= 0) {
                mergedId2Errors.putAll(id2Errors);
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        return result;
    }
    
    void updateJavaHintPreferences(JsonObject configuration) {
        this.hintsSettingsRead = true;
        
        if (configuration != null && configuration.has("preferences") && configuration.get("preferences").isJsonPrimitive()) {
            JsonElement pathPrimitive = configuration.get("preferences");
            String path = pathPrimitive.getAsString();
            Path p = Paths.get(path);
            FileObject preferencesFile = FileUtil.toFileObject(p);
            if (preferencesFile != null && preferencesFile.isValid() && preferencesFile.canRead() && preferencesFile.getExt().equals("xml")) {
                this.hintsPrefsFile = preferencesFile;
            }
            else {
                this.hintsPrefsFile = null;
            }
        }
        reRunDiagnostics();
    }
    
    void updateProjectJDKHome(JsonPrimitive configuration) {
        if (configuration == null) {
            client.logMessage(new MessageParams(MessageType.Log,"Project runtime JDK unset, defaults to NBLS JDK"));
        } else {
            client.logMessage(new MessageParams(MessageType.Log, "Project runtime JDK set to " + configuration.getAsString()));
        }
        AbstractJavaPlatformProviderOverride.setDefaultPlatformOverride(configuration != null ? configuration.getAsString() : null);
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
        } catch (MalformedURLException | IllegalArgumentException ex) {
            if (!uri.startsWith("untitled:") && !uri.startsWith("jdt:")) {
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
    
    private org.netbeans.api.lsp.Diagnostic.ReporterControl reporterControl = new org.netbeans.api.lsp.Diagnostic.ReporterControl() {
        @Override
        public void diagnosticChanged(Collection<FileObject> files, String mimeType) {
            // possibly duplicities are handled by runDiagnosticTasks
            for (FileObject f : files) {
                // do not process directories at the moment
                if (mimeType != null && !f.getMIMEType().equals(mimeType)) {
                    continue;
                }
                URL url = URLMapper.findURL(f, URLMapper.EXTERNAL);
                try {
                    String uriString = url.toURI().toString();
                    String lspUri = URITranslator.getDefault().uriToLSP(uriString);
                    server.asyncOpenFileOwner(f).thenRun(() -> {
                        Lookups.executeWith(new ProxyLookup(Lookups.singleton(client), Lookup.getDefault()), () -> {
                            runDiagnosticTasks(lspUri);
                        });
                    });
                } catch (URISyntaxException ex) {
                    // should not happen
                }
            }
        }
    };
    
    public org.netbeans.api.lsp.Diagnostic.ReporterControl createReporterControl() {
        return reporterControl;
    }
    
    /**
     * Publishes diagnostics to the client. Remembers the file, so that if the file vanishes (i.e. event is received, or
     * it's reported as missing during some processing), the client will get an empty diagnostics message for this file to
     * clear out its problem view.
     * @param uri file URI
     * @param mergedDiags the diagnostics
     */
    public void publishDiagnostics(String uri, List<Diagnostic> mergedDiags) {
        knownFiles.put(uri, Instant.now());
        client.publishDiagnostics(new PublishDiagnosticsParams(uri, mergedDiags));
    }
    
    private static final String[] ERROR_KEYS = {"errors", "hints"};
    private static final String[] VALID_ERROR_KEYS = {"errors", "hints", "offsetHints"};

    private interface ProduceErrors {
        public List<ErrorDescription> computeErrors(CompilationInfo info, Document doc) throws IOException;
    }

    @CheckForNull
    public JavaSource getJavaSource(String fileUri) {
        if (!client.getNbCodeCapabilities().wantsJavaSupport()) {
            return null;
        }
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

    static List<TextEdit> modify2TextEdits(JavaSource js, Task<WorkingCopy> task) throws IOException {//TODO: is this still used?
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

    private static final Map<ColoringAttributes, List<String>> COLORING_TO_TOKEN_TYPE_CANDIDATES = new HashMap<ColoringAttributes, List<String>>() {{
        put(ColoringAttributes.FIELD, Arrays.asList("field", "member"));
        put(ColoringAttributes.RECORD_COMPONENT, Arrays.asList("field", "member"));
        put(ColoringAttributes.LOCAL_VARIABLE, Arrays.asList("variable"));
        put(ColoringAttributes.PARAMETER, Arrays.asList("parameter"));
        put(ColoringAttributes.METHOD, Arrays.asList("method", "function"));
        put(ColoringAttributes.CONSTRUCTOR, Arrays.asList("method", "function"));
        put(ColoringAttributes.CLASS, Arrays.asList("class"));
        put(ColoringAttributes.RECORD, Arrays.asList("class"));
        put(ColoringAttributes.INTERFACE, Arrays.asList("interface"));
        put(ColoringAttributes.ANNOTATION_TYPE, Arrays.asList("interface"));
        put(ColoringAttributes.ENUM, Arrays.asList("enum"));
        put(ColoringAttributes.TYPE_PARAMETER_DECLARATION, Arrays.asList("typeParameter"));
        put(ColoringAttributes.KEYWORD, Arrays.asList("keyword"));
    }};

    private static final Map<ColoringAttributes, List<String>> COLORING_TO_TOKEN_MODIFIERS_CANDIDATES = new HashMap<ColoringAttributes, List<String>>() {{
        put(ColoringAttributes.ABSTRACT, Arrays.asList("abstract"));
        put(ColoringAttributes.DECLARATION, Arrays.asList("declaration"));
        put(ColoringAttributes.DEPRECATED, Arrays.asList("deprecated"));
        put(ColoringAttributes.STATIC, Arrays.asList("static"));
    }};

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
        JavaSource js = getJavaSource(params.getTextDocument().getUri());
        List<Integer> result = new ArrayList<>();
        if (js != null) {
            try {
                js.runUserActionTask(cc -> {
                    cc.toPhase(JavaSource.Phase.RESOLVED);
                    Document doc = cc.getSnapshot().getSource().getDocument(true);
                    new SemanticHighlighterBase() {
                        @Override
                        protected boolean process(CompilationInfo info, Document doc) {
                            process(info, doc, new ErrorDescriptionSetter() {
                                @Override
                                public void setHighlights(Document doc, Collection<Pair<int[], ColoringAttributes.Coloring>> highlights, Map<int[], String> preText) {
                                    //...nothing
                                }

                                @Override
                                public void setColorings(Document doc, Map<Token, ColoringAttributes.Coloring> colorings) {
                                    int line = 0;
                                    long column = 0;
                                    int lastLine = 0;
                                    long currentLineStart = 0;
                                    long nextLineStart = getStartPosition(line + 2);
                                    Map<Token, ColoringAttributes.Coloring> ordered = new TreeMap<>((t1, t2) -> t1.offset(null) - t2.offset(null));
                                    ordered.putAll(colorings);
                                    for (Entry<Token, ColoringAttributes.Coloring> e : ordered.entrySet()) {
                                        int currentOffset = e.getKey().offset(null);
                                        while (nextLineStart < currentOffset) {
                                            line++;
                                            currentLineStart = nextLineStart;
                                            nextLineStart = getStartPosition(line + 2);
                                            column = 0;
                                        }
                                        Optional<Integer> tokenType = e.getValue().stream().map(c -> coloring2TokenType[c.ordinal()]).collect(Collectors.maxBy((v1, v2) -> v1.intValue() - v2.intValue()));
                                        int modifiers = 0;
                                        for (ColoringAttributes c : e.getValue()) {
                                            int mod = coloring2TokenModifier[c.ordinal()];
                                            if (mod != (-1)) {
                                                modifiers |= mod;
                                            }
                                        }
                                        if (tokenType.isPresent() && tokenType.get() >= 0) {
                                            result.add(line - lastLine);
                                            result.add((int) (currentOffset - currentLineStart - column));
                                            result.add(e.getKey().length());
                                            result.add(tokenType.get());
                                            result.add(modifiers);
                                            lastLine = line;
                                            column = currentOffset - currentLineStart;
                                        }
                                    }
                                }

                                private long getStartPosition(int line) {
                                    try {
                                        return line < 0 ? -1 : info.getCompilationUnit().getLineMap().getStartPosition(line);
                                    } catch (Exception e) {
                                        return info.getText().length();
                                    }
                                }
                            });
                            return true;
                        }
                    }.process(cc, doc);
                }, true);
            } catch (IOException ex) {
                //TODO: include stack trace:
                client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
            }
        }
        SemanticTokens tokens = new SemanticTokens(result);
        return CompletableFuture.completedFuture(tokens);
    }

    @Override
    public CompletableFuture<List<CallHierarchyItem>> prepareCallHierarchy(CallHierarchyPrepareParams params) {
        FileObject file = fromURI(params.getTextDocument().getUri());
        if (file == null) {
            return CompletableFuture.completedFuture(null);
        }
        // FIXME: probably the MIME type ought be derived from the exact source position, not from the whole file.
        CallHierarchyProvider chp = MimeLookup.getLookup(file.getMIMEType()).lookup(CallHierarchyProvider.class);
        if (chp == null) {
            return CompletableFuture.completedFuture(null);
        }
        EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
        // PENDING: possibly handle cancellation with this Future ?
        StyledDocument lDoc;
        try {
            lDoc = ec.openDocument();
        } catch (IOException ex) {
            CompletableFuture f = new CompletableFuture();
            f.completeExceptionally(ex);
            return f;
        }
        return chp.findCallOrigin(lDoc, Utils.getOffset(lDoc, params.getPosition())).thenApply(l -> {
            if (l == null) {
                return null;
            }
            List<CallHierarchyItem> res = new ArrayList<>();
            for (CallHierarchyEntry c : l) {
                CallHierarchyItem n = callEntryToItem(file, c, callEntryDocument(c, lDoc));
                if (n != null) {
                    res.add(n);
                }
            }
            return res;
        });
    }
    
    private static CallHierarchyItem callEntryToItem(FileObject documentFile, CallHierarchyEntry c, StyledDocument lDoc) {
        FileObject owner = c.getElement().getFile();
        if (owner == null) {
            owner = documentFile;
        }
        if (owner == null || lDoc == null) {
            return null;
        }
        CallHierarchyItem chi = new CallHierarchyItem();
        DocumentSymbol ds = structureElement2DocumentSymbol(lDoc, c.getElement());
        if (ds == null) {
            return null;
        }
        chi.setKind(ds.getKind());
        chi.setName(ds.getName());
        chi.setTags(ds.getTags());
        chi.setRange(ds.getRange());
        chi.setSelectionRange(ds.getSelectionRange());
        chi.setUri(Utils.toUri(owner));
        chi.setData(c.getCustomData());
        return chi;
    }
    
    StyledDocument callEntryDocument(CallHierarchyEntry e, StyledDocument documentFile) {
        FileObject owner = e.getElement().getFile();
        if (owner != null && owner != documentFile) {
            // must open the document
            EditorCookie ck = owner.getLookup().lookup(EditorCookie.class);
            if (ck == null) {
                return null;
            }
            try {
                return ck.openDocument();
            } catch (IOException ex) {
                // TODO: report to the client ?
                return null;
            }
        } else {
            return documentFile;
        }
    }

    abstract class HierarchyTask<T> {
        final CallHierarchyItem request;
        final FileObject file;
        final CallHierarchyProvider provider;
        final AtomicBoolean cancelled = new AtomicBoolean();
        protected StyledDocument doc;

        public HierarchyTask(CallHierarchyItem request) {
            this.request = request;
            this.file = fromURI(request.getUri());
            this.provider = file == null ? null :
                    MimeLookup.getLookup(file.getMIMEType()).lookup(CallHierarchyProvider.class);
        }
        
        public CompletableFuture<List<T>> processRequest() {
            if (file == null || provider == null) {
                return null;
            }
            try {
                EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
                Document doc = ec.openDocument();
                if (!(doc instanceof StyledDocument)) {
                    return null;
                }
                this.doc = (StyledDocument)doc;
            } catch (IOException | RuntimeException ex) {
                CompletableFuture<List<T>> res = new CompletableFuture<>();
                res.completeExceptionally(ex);
                return res;
            }

            StructureProvider.Builder b = StructureProvider.newBuilder(
                    request.getName(), StructureElement.Kind.valueOf(request.getKind().toString()));

            b.file(file);
            b.expandedStartOffset(Utils.getOffset(doc, request.getRange().getStart()));
            b.expandedEndOffset(Utils.getOffset(doc, request.getRange().getEnd()));
            b.selectionStartOffset(Utils.getOffset(doc, request.getSelectionRange().getStart()));
            b.selectionEndOffset(Utils.getOffset(doc, request.getSelectionRange().getEnd()));

            String d;
            if (request.getData() instanceof JsonPrimitive) {
                d = ((JsonPrimitive)request.getData()).getAsString();
            } else if (request.getData() != null) {
                d = request.getData().toString();
            } else {
                d = null;
            }
            
            return callProvider(provider, new CallHierarchyEntry(b.build(), d)).thenApply(this::convert);
        }
        
        List<T> convert(List<CallHierarchyEntry.Call> l) {
            if (l == null) {
                return null;
            }
            List<T> res = (List<T>)new ArrayList();
            for (CallHierarchyEntry.Call call : l) {
                CallHierarchyEntry che = call.getItem();
                StyledDocument lDoc = callEntryDocument(che, doc);
                CallHierarchyItem callItem = callEntryToItem(file, che, lDoc);
                if (callItem == null) {
                    continue;
                }
                List<Range> ranges = new ArrayList<>();
                for (org.netbeans.api.lsp.Range r : call.getRanges()) {
                    // lDoc cannot be null if callItem != null.
                    ranges.add(callRange2Range(r, lDoc));
                }
                T lspCall = createResultItem(callItem, ranges);
                res.add(lspCall);
            }
            return res;
        }
                
        protected abstract CompletableFuture<List<CallHierarchyEntry.Call>> callProvider(CallHierarchyProvider p, CallHierarchyEntry e);
        
        protected abstract T createResultItem(CallHierarchyItem item, List<Range> ranges);
    }
    
    @Override
    public CompletableFuture<List<CallHierarchyIncomingCall>> callHierarchyIncomingCalls(CallHierarchyIncomingCallsParams params) {
        HierarchyTask<CallHierarchyIncomingCall> t =  new HierarchyTask<CallHierarchyIncomingCall>(params.getItem()) {
            @Override
            protected CompletableFuture<List<CallHierarchyEntry.Call>> callProvider(CallHierarchyProvider p, CallHierarchyEntry e) {
                return p.findIncomingCalls(e);
            }

            @Override
            protected CallHierarchyIncomingCall createResultItem(CallHierarchyItem item, List<Range> ranges) {
                return new CallHierarchyIncomingCall(item, ranges);
            }
        };
        return t.processRequest();
    }
    
    private static Position offset2Position(Document doc, int offset) {
        int line = NbDocument.findLineNumber((StyledDocument)doc, offset);
        int column = NbDocument.findLineColumn((StyledDocument)doc, offset);
        return new Position(line, column);
    }
    
    private static Range callRange2Range(org.netbeans.api.lsp.Range r, Document doc) {
        return new Range(offset2Position(doc, r.getStartOffset()), offset2Position(doc, r.getEndOffset()));
    }

    @Override
    public CompletableFuture<List<CallHierarchyOutgoingCall>> callHierarchyOutgoingCalls(CallHierarchyOutgoingCallsParams params) {
        HierarchyTask<CallHierarchyOutgoingCall> t =  new HierarchyTask<CallHierarchyOutgoingCall>(params.getItem()) {
            @Override
            protected CompletableFuture<List<CallHierarchyEntry.Call>> callProvider(CallHierarchyProvider p, CallHierarchyEntry e) {
                return p.findOutgoingCalls(e);
            }

            @Override
            protected CallHierarchyOutgoingCall createResultItem(CallHierarchyItem item, List<Range> ranges) {
                return new CallHierarchyOutgoingCall(item, ranges);
            }
        };
        return t.processRequest();
    }

    @Override
    public CompletableFuture<List<InlayHint>> inlayHint(InlayHintParams params) {
        String uri = params.getTextDocument().getUri();
        ConfigurationItem conf = new ConfigurationItem();
        conf.setScopeUri(uri);
        conf.setSection(client.getNbCodeCapabilities().getConfigurationPrefix() + NETBEANS_INLAY_HINT);
        return client.configuration(new ConfigurationParams(Collections.singletonList(conf))).thenCompose(c -> {
            FileObject file;
            try {
                file = Utils.fromUri(uri);
            } catch (MalformedURLException ex) {
                return CompletableFuture.failedFuture(ex);
            }
            Set<String> enabled = null;
            if (c != null && !c.isEmpty()) {
                enabled = new HashSet<>();

                JsonArray actualSettings = ((JsonArray) c.get(0));

                for (JsonElement el : actualSettings) {
                    enabled.add(((JsonPrimitive) el).getAsString());
                }
            }
            org.netbeans.api.lsp.Range range = new org.netbeans.api.lsp.Range(Utils.getOffset(file, params.getRange().getStart()),
                                                                              Utils.getOffset(file, params.getRange().getEnd()));
            CompletableFuture<List<InlayHint>> result = CompletableFuture.completedFuture(List.of());
            for (InlayHintsProvider p : MimeLookup.getLookup(FileUtil.getMIMEType(file)).lookupAll(InlayHintsProvider.class)) {
                Set<String> currentTypes = new HashSet<>(p.supportedHintTypes());

                if (enabled != null) {
                    currentTypes.retainAll(enabled);
                }

                if (!currentTypes.isEmpty()) {
                    InlayHintsProvider.Context ctx = new InlayHintsProvider.Context(file, range, currentTypes);
                    result = result.thenCombine(p.inlayHints(ctx).thenApply(lspHints -> {
                        List<InlayHint> hints = new ArrayList<>();

                        for (org.netbeans.api.lsp.InlayHint h : lspHints) {
                            hints.add(new InlayHint(Utils.createPosition(file, h.getPosition().getOffset()), Either.forRight(List.of(new InlayHintLabelPart(h.getText())))));
                        }

                        return hints;
                    }), (l1, l2) -> {
                        List<InlayHint> combined = new ArrayList<>();

                        combined.addAll(l1);
                        combined.addAll(l2);

                        return combined;
                    });
                }
            }
            return result;
        });
    }

    @Override
    public CompletableFuture<List<InlineValue>> inlineValue(InlineValueParams params) {
        String uri = params.getTextDocument().getUri();
        FileObject file = fromURI(uri);
        if (file == null) {
            return CompletableFuture.completedFuture(null);
        }
        CompletableFuture<List<InlineValue>> result = new CompletableFuture<>();
        result.complete(List.of());
        Document rawDoc = server.getOpenedDocuments().getDocument(uri);
        if (!(rawDoc instanceof StyledDocument)) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        StyledDocument doc = (StyledDocument)rawDoc;
        int currentExecutionPosition = Utils.getOffset(doc, params.getContext().getStoppedLocation().getEnd());
        for (InlineValuesProvider provider : MimeLookup.getLookup(file.getMIMEType()).lookupAll(InlineValuesProvider.class)) {
            result = result.thenCombine(provider.inlineValues(file, currentExecutionPosition), (l1, l2) -> {
                List<InlineValue> res = new ArrayList<>(l1.size() + l2.size());
                res.addAll(l1);
                for (org.netbeans.api.lsp.InlineValue val : l2) {
                    res.add(new InlineValue(new InlineValueEvaluatableExpression(new Range(Utils.createPosition(file, val.getRange().getStartOffset()), Utils.createPosition(file, val.getRange().getEndOffset())), val.getExpression())));
                }
                return res;
            });
        }
        return result;
    }

}
