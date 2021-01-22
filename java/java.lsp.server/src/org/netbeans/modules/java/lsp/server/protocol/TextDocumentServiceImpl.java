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
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
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
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.CompletionTriggerKind;
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
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
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
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.queries.SourceJavadocAttacher;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.CompilationInfo.CacheClearPolicy;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.java.GoToSupport;
import org.netbeans.modules.editor.java.GoToSupport.Context;
import org.netbeans.modules.editor.java.GoToSupport.GoToTarget;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.completion.JavaCompletionTask;
import org.netbeans.modules.java.completion.JavaCompletionTask.Options;
import org.netbeans.modules.java.completion.JavaDocumentationTask;
import org.netbeans.modules.java.editor.base.semantic.MarkOccurrencesHighlighterBase;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.java.editor.options.MarkOccurencesSettings;
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
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.debugging.utils.ErrorUtilities;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.ui.ElementOpenAccessor;
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
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
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

    private static final RequestProcessor BACKGROUND_TASKS = new RequestProcessor(TextDocumentServiceImpl.class.getName(), 1, false, false);
    private static final RequestProcessor WORKER = new RequestProcessor(TextDocumentServiceImpl.class.getName(), 1, false, false);

    private final Map<String, Document> openedDocuments = new HashMap<>();
    private final Map<String, RequestProcessor.Task> diagnosticTasks = new HashMap<>();
    private NbCodeLanguageClient client;

    public TextDocumentServiceImpl() {
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

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        try {
            String uri = params.getTextDocument().getUri();
            FileObject file = Utils.fromUri(uri);
            EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
            Document doc = ec.openDocument();
            final int caret = Utils.getOffset(doc, params.getPosition());
            final CompletionList completionList = new CompletionList();
            ParserManager.parse(Collections.singletonList(Source.create(doc)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    TokenSequence<JavaTokenId> ts = resultIterator.getSnapshot().getTokenHierarchy().tokenSequence(JavaTokenId.language());
                    if (ts.move(caret) == 0 || !ts.moveNext()) {
                        if (!ts.movePrevious()) {
                            ts.moveNext();
                        }
                    }
                    int len = caret - ts.offset();
                    boolean allCompletion = params.getContext() != null && params.getContext().getTriggerKind() == CompletionTriggerKind.TriggerForIncompleteCompletions
                            || len > 0 && ts.token().length() >= len && ts.token().id() == JavaTokenId.IDENTIFIER;
                    CompilationController controller = CompilationController.get(resultIterator.getParserResult(ts.offset()));
                    controller.toPhase(JavaSource.Phase.RESOLVED);
                    JavaCompletionTask<CompletionItem> task = JavaCompletionTask.create(caret, new ItemFactoryImpl(client, controller, uri, ts.offset()), allCompletion ? EnumSet.of(Options.ALL_COMPLETION) : EnumSet.noneOf(Options.class), () -> false);
                    task.run(resultIterator);
                    List<CompletionItem> results = task.getResults();
                    if (results != null) {
                        for (Iterator<CompletionItem> it = results.iterator(); it.hasNext();) {
                            CompletionItem item = it.next();
                            if (item == null) {
                                it.remove();
                            }
                        }
                        completionList.setItems(results);
                    }
                    completionList.setIsIncomplete(task.hasAdditionalClasses());
                }
            });
            return CompletableFuture.completedFuture(Either.<List<CompletionItem>, CompletionList>forRight(completionList));
        } catch (IOException | ParseException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static final class CompletionData {
        public String uri;
        public int offset;
        public String kind;
        public String[] elementHandle;

        public CompletionData() {
        }

        public CompletionData(String uri, int offset, String kind, String[] elementHandle) {
            this.uri = uri;
            this.offset = offset;
            this.kind = kind;
            this.elementHandle = elementHandle;
        }

        @Override
        public String toString() {
            return "CompletionData{" + "uri=" + uri + ", kind=" + kind + ", elementHandle=" + elementHandle + '}';
        }
        
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = (NbCodeLanguageClient)client;
    }

    private static class ItemFactoryImpl implements JavaCompletionTask.ItemFactory<CompletionItem> {

        private final LanguageClient client;
        private final String uri;
        private final int offset;
        private final CompilationInfo info;
        private final Scope scope;

        public ItemFactoryImpl(LanguageClient client, CompilationInfo info, String uri, int offset) {
            this.client = client;
            this.uri = uri;
            this.offset = offset;
            this.info = info;
            this.scope = info.getTrees().getScope(info.getTreeUtilities().pathFor(offset));
        }

        private static final Set<String> SUPPORTED_ELEMENT_KINDS = new HashSet<>(Arrays.asList("PACKAGE", "CLASS", "INTERFACE", "ENUM", "ANNOTATION_TYPE", "METHOD", "CONSTRUCTOR", "INSTANCE_INIT", "STATIC_INIT", "FIELD", "ENUM_CONSTANT", "TYPE_PARAMETER", "MODULE"));

        private void setCompletionData(CompletionItem ci, Element el) {
            if (SUPPORTED_ELEMENT_KINDS.contains(el.getKind().name())) {
                setCompletionData(ci, ElementHandle.create(el));
            }
        }

        private void setCompletionData(CompletionItem ci, ElementHandle handle) {
            ci.setData(new CompletionData(uri, offset, handle.getKind().name(), SourceUtils.getJVMSignature(handle)));
        }

        @Override
        public CompletionItem createKeywordItem(String kwd, String postfix, int substitutionOffset, boolean smartType) {
            CompletionItem item = new CompletionItem(kwd);
            item.setKind(CompletionItemKind.Keyword);
            item.setSortText(String.format("%4d%s", smartType ? 670 : 1670, kwd)); //NOI18N
            return item;
        }

        @Override
        public CompletionItem createPackageItem(String pkgFQN, int substitutionOffset, boolean inPackageStatement) {
            final String simpleName = pkgFQN.substring(pkgFQN.lastIndexOf('.') + 1);
            CompletionItem item = new CompletionItem(simpleName);
            item.setKind(CompletionItemKind.Folder);
            item.setSortText(String.format("%4d%s#%s", 1900, simpleName, pkgFQN)); //NOI18N
            return item;
        }

        @Override
        public CompletionItem createTypeItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addTypeVars, boolean addSimpleName, boolean smartType, boolean autoImportEnclosingType) {
            CompletionItem item = new CompletionItem(elem.getSimpleName().toString());
            item.setKind(elementKind2CompletionItemKind(elem.getKind()));
            String name = elem.getQualifiedName().toString();
            int idx = name.lastIndexOf('.');
            String pkgName = idx < 0 ? "" : name.substring(0, idx);
            if (!pkgName.isEmpty()) {
                item.setDetail(name.substring(0, idx));
            }
            item.setSortText(String.format("%4d%s#%2d#%s", smartType ? 800 : 1800, elem.getSimpleName().toString(), Utilities.getImportanceLevel(name), pkgName)); //NOI18N
            setCompletionData(item, elem);
            return item;
        }

        @Override
        public CompletionItem createTypeItem(ElementHandle<TypeElement> handle, EnumSet<ElementKind> kinds, int substitutionOffset, ReferencesCount referencesCount, Source source, boolean insideNew, boolean addTypeVars, boolean afterExtends) {
            TypeElement te = handle.resolve(info);
            if (te != null && info.getTrees().isAccessible(scope, te)) {
                CompletionItem item = new CompletionItem(te.getSimpleName().toString());
                String name = handle.getQualifiedName();
                int idx = name.lastIndexOf('.');
                String pkgName = idx < 0 ? "" : name.substring(0, idx);
                if (!pkgName.isEmpty()) {
                    item.setDetail(pkgName);
                }
                item.setKind(elementKind2CompletionItemKind(handle.getKind()));
                item.setSortText(String.format("%4d%s#%2d#%s", 1800, te.getSimpleName().toString(), Utilities.getImportanceLevel(name), pkgName)); //NOI18N
                setCompletionData(item, handle);
                return item;
            }
            return null;
        }

        @Override
        public CompletionItem createArrayItem(CompilationInfo info, ArrayType type, int substitutionOffset, ReferencesCount referencesCount, Elements elements) {
            return null; //TODO: fill
        }

        @Override
        public CompletionItem createTypeParameterItem(TypeParameterElement elem, int substitutionOffset) {
            CompletionItem item = new CompletionItem(elem.getSimpleName().toString());
            item.setKind(elementKind2CompletionItemKind(elem.getKind()));
            item.setSortText(String.format("%4d%s", 1700, elem.getSimpleName().toString())); //NOI18N
            return item;
        }

        @Override
        public CompletionItem createVariableItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean smartType, int assignToVarOffset) {
            CompletionItem item = new CompletionItem(elem.getSimpleName().toString());
            item.setKind(elementKind2CompletionItemKind(elem.getKind()));
            int priority = elem.getKind() == ElementKind.ENUM_CONSTANT || elem.getKind() == ElementKind.FIELD ? smartType ? 300 : 1300 : smartType ? 200 : 1200;
            item.setSortText(String.format("%4d%s", priority, elem.getSimpleName().toString())); //NOI18N
            setCompletionData(item, elem);
            return item;
        }

        @Override
        public CompletionItem createVariableItem(CompilationInfo info, String varName, int substitutionOffset, boolean newVarName, boolean smartType) {
            CompletionItem item = new CompletionItem(varName);
            item.setKind(CompletionItemKind.Variable);
            item.setSortText(String.format("%4d%s", smartType ? 200 : 1200, varName)); //NOI18N
            return item;
        }

        @Override
        public CompletionItem createExecutableItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean inImport, boolean addSemicolon, boolean smartType, int assignToVarOffset, boolean memberRef) {
            Iterator<? extends VariableElement> it = elem.getParameters().iterator();
            Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
            StringBuilder label = new StringBuilder();
            String sep = "";
            label.append(elem.getSimpleName().toString());
            label.append("(");
            StringBuilder sortParams = new StringBuilder();
            sortParams.append('(');
            int cnt = 0;
            while(it.hasNext() && tIt.hasNext()) {
                TypeMirror tm = tIt.next();
                if (tm == null) {
                    break;
                }
                label.append(sep);
                String paramTypeName = Utilities.getTypeName(info, tm, false, elem.isVarArgs() && !tIt.hasNext()).toString();
                label.append(paramTypeName);
                label.append(' ');
                label.append(it.next().getSimpleName().toString());
                sep = ", ";
                sortParams.append(paramTypeName);
                if (tIt.hasNext()) {
                    sortParams.append(',');
                }
                cnt++;
            }
            label.append(") : ");
            sortParams.append(')');
            TypeMirror retType = type.getReturnType();
            label.append(Utilities.getTypeName(info, retType, false).toString());
            CompletionItem item = new CompletionItem(label.toString());
            item.setKind(elementKind2CompletionItemKind(elem.getKind()));
            StringBuilder insertText = new StringBuilder();
            insertText.append(elem.getSimpleName());
            insertText.append("(");
            if (elem.getParameters().isEmpty()) {
                insertText.append(")");
            }
            item.setInsertText(insertText.toString());
            item.setInsertTextFormat(InsertTextFormat.PlainText);
            int priority = elem.getKind() == ElementKind.METHOD ? smartType ? 500 : 1500 : smartType ? 650 : 1650;
            item.setSortText(String.format("%4d%s#%2d%s", priority, elem.getSimpleName().toString(), cnt, sortParams)); //NOI18N
            setCompletionData(item, elem);
            return item;
        }

        @Override
        public CompletionItem createThisOrSuperConstructorItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, String name) {
            CompletionItem item = createExecutableItem(info, elem, type, substitutionOffset, null, false, isDeprecated, false, false, false, -1, false);
            item.setLabel(name != null ? name : elem.getEnclosingElement().getSimpleName().toString());
            StringBuilder insertText = new StringBuilder();
            insertText.append(item.getLabel());
            insertText.append("(");
            if (elem.getParameters().isEmpty()) {
                insertText.append(")");
            }
            item.setInsertText(insertText.toString());
            item.setKind(CompletionItemKind.Constructor);
            item.setSortText(String.format("%4d%s", name != null ? 1550 : 1650, item.getSortText().substring(4))); //NOI18N
            setCompletionData(item, elem);
            return item;
        }

        @Override
        public CompletionItem createOverrideMethodItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean implement) {
            CompletionItem item = createExecutableItem(info, elem, type, substitutionOffset, null, false, false, false, false, false, -1, false);
            item.setLabel(String.format("%s - %s", item.getLabel(), implement ? "implement" : "override"));
            item.setInsertText(null);
            item.setKind(elementKind2CompletionItemKind(elem.getKind()));
            try {
                List<TextEdit> textEdits = modify2TextEdits(JavaSource.forFileObject(info.getFileObject()), wc -> {
                    wc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TreePath tp = wc.getTreeUtilities().pathFor(offset);
                    if (implement) {
                        GeneratorUtils.generateAbstractMethodImplementation(wc, tp, elem, offset);
                    } else {
                        GeneratorUtils.generateMethodOverride(wc, tp, elem, offset);
                    }
                });
                if (!textEdits.isEmpty()) {
                    item.setTextEdit(textEdits.get(0));
                }
            } catch (IOException ex) {
                //TODO: include stack trace:
                client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
            }
            setCompletionData(item, elem);
            return item;
        }

        @Override
        public CompletionItem createGetterSetterMethodItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, String name, boolean setter) {
            return null; //TODO: fill
        }

        @Override
        public CompletionItem createDefaultConstructorItem(TypeElement elem, int substitutionOffset, boolean smartType) {
            return null; //TODO: fill
        }

        @Override
        public CompletionItem createParametersItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, int activeParamIndex, String name) {
            return null; //TODO: fill
        }

        @Override
        public CompletionItem createAnnotationItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated) {
            return null; //TODO: fill
        }

        @Override
        public CompletionItem createAttributeItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated) {
            return null; //TODO: fill
        }

        @Override
        public CompletionItem createAttributeValueItem(CompilationInfo info, String value, String documentation, TypeElement element, int substitutionOffset, ReferencesCount referencesCount) {
            return null; //TODO: fill
        }

        private static final Object KEY_IMPORT_TEXT_EDITS = new Object();

        @Override
        public CompletionItem createStaticMemberItem(CompilationInfo info, DeclaredType type, Element memberElem, TypeMirror memberType, boolean multipleVersions, int substitutionOffset, boolean isDeprecated, boolean addSemicolon) {
            //TODO: prefer static imports (but would be much slower?)
            //TODO: should be resolveImport instead of addImports:
            Map<Element, List<TextEdit>> imports = (Map<Element, List<TextEdit>>) info.getCachedValue(KEY_IMPORT_TEXT_EDITS);
            if (imports == null) {
                info.putCachedValue(KEY_IMPORT_TEXT_EDITS, imports = new HashMap<>(), CacheClearPolicy.ON_TASK_END);
            }
            List<TextEdit> currentClassImport = imports.computeIfAbsent(type.asElement(), toImport -> {
                try {
                    return modify2TextEdits(JavaSource.forFileObject(info.getFileObject()), wc -> {
                        wc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        wc.rewrite(info.getCompilationUnit(), GeneratorUtilities.get(wc).addImports(wc.getCompilationUnit(), new HashSet<>(Arrays.asList(toImport))));
                    });
                } catch (IOException ex) {
                    //TODO: include stack trace:
                    client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
                    return Collections.emptyList();
                }
            });
            String label = type.asElement().getSimpleName() + "." + memberElem.getSimpleName();
            CompletionItem item = new CompletionItem(label);
            item.setKind(elementKind2CompletionItemKind(memberElem.getKind()));
            item.setInsertText(label);
            item.setInsertTextFormat(InsertTextFormat.PlainText);
            item.setAdditionalTextEdits(currentClassImport);
            String sortText = memberElem.getSimpleName().toString();
            if (memberElem.getKind().isField()) {
                sortText += String.format("#%s", Utilities.getTypeName(info, type, false)); //NOI18N
            } else {
                StringBuilder sortParams = new StringBuilder();
                sortParams.append('(');
                int cnt = 0;
                Iterator<? extends TypeMirror> tIt = ((ExecutableType)memberType).getParameterTypes().iterator();
                while(tIt.hasNext()) {
                    TypeMirror tm = tIt.next();
                    if (tm == null) {
                        break;
                    }
                    sortParams.append(Utilities.getTypeName(info, tm, false, ((ExecutableElement)memberElem).isVarArgs() && !tIt.hasNext()).toString());
                    if (tIt.hasNext()) {
                        sortParams.append(',');
                    }
                    cnt++;
                }
                sortParams.append(')');
                sortText += String.format("#%2d#%s#s", cnt, sortParams.toString(), Utilities.getTypeName(info, type, false)); //NOI18N
            }
            item.setSortText(String.format("%4d%s", memberElem.getKind().isField() ? 720 : 750, sortText)); //NOI18N
            setCompletionData(item, memberElem);
            return item;
        }

        @Override
        public CompletionItem createStaticMemberItem(ElementHandle<TypeElement> handle, String name, int substitutionOffset, boolean addSemicolon, ReferencesCount referencesCount, Source source) {
            return null; //TODO: fill
        }

        @Override
        public CompletionItem createChainedMembersItem(CompilationInfo info, List<? extends Element> chainedElems, List<? extends TypeMirror> chainedTypes, int substitutionOffset, boolean isDeprecated, boolean addSemicolon) {
            return null; //TODO: fill
        }

        @Override
        public CompletionItem createInitializeAllConstructorItem(CompilationInfo info, boolean isDefault, Iterable<? extends VariableElement> fields, ExecutableElement superConstructor, TypeElement parent, int substitutionOffset) {
            return null; //TODO: fill
        }

        private static CompletionItemKind elementKind2CompletionItemKind(ElementKind kind) {
            switch (kind) {
                case PACKAGE:
                    return CompletionItemKind.Folder;
                case ENUM:
                    return CompletionItemKind.Enum;
                case CLASS:
                    return CompletionItemKind.Class;
                case ANNOTATION_TYPE:
                    return CompletionItemKind.Interface;
                case INTERFACE:
                    return CompletionItemKind.Interface;
                case ENUM_CONSTANT:
                    return CompletionItemKind.EnumMember;
                case FIELD:
                    return CompletionItemKind.Field;
                case PARAMETER:
                    return CompletionItemKind.Variable;
                case LOCAL_VARIABLE:
                    return CompletionItemKind.Variable;
                case EXCEPTION_PARAMETER:
                    return CompletionItemKind.Variable;
                case METHOD:
                    return CompletionItemKind.Method;
                case CONSTRUCTOR:
                    return CompletionItemKind.Constructor;
                case TYPE_PARAMETER:
                    return CompletionItemKind.TypeParameter;
                case RESOURCE_VARIABLE:
                    return CompletionItemKind.Variable;
                case MODULE:
                    return CompletionItemKind.Module;
                case STATIC_INIT:
                case INSTANCE_INIT:
                case OTHER:
                default:
                    return CompletionItemKind.Text;
            }
        }
    }

    private static final RequestProcessor JAVADOC_WORKER = new RequestProcessor(TextDocumentServiceImpl.class.getName() + ".javadoc", 1);

    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem ci) {
        JsonObject rawData = (JsonObject) ci.getData();
        if (rawData == null) {
            return CompletableFuture.completedFuture(ci);
        }
        CompletionData data = new Gson().fromJson(rawData, CompletionData.class);
        try {
            FileObject file = Utils.fromUri(data.uri);
            EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
            Document doc = ec.openDocument();
            final ElementHandle<Element> handle = ElementHandleAccessor.getInstance().create(ElementKind.valueOf(data.kind), data.elementHandle);
            final JavaDocumentationTask<Future<String>> task = JavaDocumentationTask.create(-1, handle, new JavaDocumentationTask.DocumentationFactory<Future<String>>() {
                @Override
                public Future<String> create(CompilationInfo compilationInfo, Element element, Callable<Boolean> cancel) {
                    return ElementJavadoc.create(compilationInfo, element, cancel).getTextAsync();
                }
            }, () -> false);
            LineMap[] lm = new LineMap[1];
            ModificationResult mr = ModificationResult.runModificationTask(Collections.singletonList(Source.create(doc)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    task.run(resultIterator);
                    if (ci.getDetail() != null) {
                        final WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult(data.offset));
                        copy.toPhase(JavaSource.Phase.RESOLVED);
                        Element e = handle.resolve(copy);
                        if (e != null) {
                            copy.rewrite(copy.getCompilationUnit(), GeneratorUtilities.get(copy).addImports(copy.getCompilationUnit(), Collections.singleton(e)));
                        }
                        lm[0] = copy.getCompilationUnit().getLineMap();
                    }
                }
            });
            List<? extends ModificationResult.Difference> diffs = mr.getDifferences(file);
            if (diffs != null && !diffs.isEmpty()) {
                List<TextEdit> edits = new ArrayList<>();
                for (ModificationResult.Difference diff : diffs) {
                    edits.add(new TextEdit(new Range(Utils.createPosition(lm[0], diff.getStartPosition().getOffset()),
                            Utils.createPosition(lm[0], diff.getEndPosition().getOffset())), diff.getNewText()));
                }
                ci.setAdditionalTextEdits(edits);
            }
            Future<String> futureJavadoc = task.getDocumentation();
            CompletableFuture<CompletionItem> result = new CompletableFuture<CompletionItem>() {
                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    return futureJavadoc.cancel(mayInterruptIfRunning) && super.cancel(mayInterruptIfRunning);
                }
            };

            JAVADOC_WORKER.post(() -> {
                try {
                    String javadoc = futureJavadoc.get();
                    MarkupContent markup = new MarkupContent();
                    markup.setKind("markdown");
                    markup.setValue(html2MD(javadoc));
                    ci.setDocumentation(markup);
                    result.complete(ci);
                } catch (ExecutionException | InterruptedException ex) {
                    result.completeExceptionally(ex);
                }
            });
            return result;
        } catch (IOException | ParseException ex) {
            CompletableFuture<CompletionItem> result = new CompletableFuture<CompletionItem>();
            result.completeExceptionally(ex);
            return result;
        }
    }

    public static String html2MD(String html) {
        int idx = html.indexOf("<p id=\"not-found\">"); // strip "No Javadoc found" message
        return FlexmarkHtmlConverter.builder().build().convert(idx >= 0 ? html.substring(0, idx) : html).replaceAll("<br />[ \n]*$", "");
    }

    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        try {
            String uri = params.getTextDocument().getUri();
            FileObject file = Utils.fromUri(uri);
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
        JavaSource js = getSource(params.getTextDocument().getUri());
        GoToTarget[] target = new GoToTarget[1];
        LineMap[] thisFileLineMap = new LineMap[1];
        try {
            js.runUserActionTask(cc -> {
                cc.toPhase(JavaSource.Phase.RESOLVED);
                Document doc = cc.getSnapshot().getSource().getDocument(true);
                int offset = Utils.getOffset(doc, params.getPosition());
                Context context = GoToSupport.resolveContext(cc, doc, offset, false, false);
                if (context == null) {
                    return ;
                }
                target[0] = GoToSupport.computeGoToTarget(cc, context, offset);
                thisFileLineMap[0] = cc.getCompilationUnit().getLineMap();
            }, true);
        } catch (IOException ex) {
            //TODO: include stack trace:
            client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
        }

        List<Location> result = new ArrayList<>();

        if (target[0] != null && target[0].success) {
            if (target[0].offsetToOpen < 0) {
                Object[] openInfo = ElementOpenAccessor.getInstance().getOpenInfo(target[0].cpInfo, target[0].elementToOpen, new AtomicBoolean());
                if (openInfo == null && target[0].resourceName != null) {
                    // try to attach sources
                    final ClassPath cp = ClassPathSupport.createProxyClassPath(
                            target[0].cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT),
                            target[0].cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE),
                            target[0].cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE));
                    final FileObject resource = cp.findResource(target[0].resourceName);
                    if (resource != null) {
                        final FileObject root = cp.findOwnerRoot(resource);
                        if (root != null) {
                            final CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> future = new CompletableFuture<>();
                            SourceJavadocAttacher.attachSources(root.toURL(), new SourceJavadocAttacher.AttachmentListener() {
                                @Override
                                public void attachmentSucceeded() {
                                    Object[] openInfo = ElementOpenAccessor.getInstance().getOpenInfo(target[0].cpInfo, target[0].elementToOpen, new AtomicBoolean());
                                    if (openInfo != null && (int) openInfo[1] != (-1) && (int) openInfo[2] != (-1) && openInfo[3] != null) {
                                        FileObject file = (FileObject) openInfo[0];
                                        int start = (int) openInfo[1];
                                        int end = (int) openInfo[2];
                                        LineMap lm = (LineMap) openInfo[3];
                                        future.complete(Either.forLeft(Collections.singletonList(new Location(Utils.toUri(file),
                                                new Range(Utils.createPosition(lm, start), Utils.createPosition(lm, end))))));
                                    }
                                }
                                @Override
                                public void attachmentFailed() {
                                    try {
                                        FileObject generated = org.netbeans.modules.java.classfile.CodeGenerator.generateCode(target[0].cpInfo, target[0].elementToOpen);
                                        if (generated != null) {
                                            final int[] pos = new int[] {-1};
                                            try {
                                                JavaSource.create(target[0].cpInfo, generated).runUserActionTask(new Task<CompilationController>() {
                                                    @Override public void run(CompilationController parameter) throws Exception {
                                                        parameter.toPhase(JavaSource.Phase.RESOLVED);
                                                        Element el = target[0].elementToOpen.resolve(parameter);
                                                        if (el != null) {
                                                            TreePath p = parameter.getTrees().getPath(el);
                                                            if (p != null) {
                                                                pos[0] = (int) parameter.getTrees().getSourcePositions().getStartPosition(p.getCompilationUnit(), p.getLeaf());
                                                            }
                                                        }
                                                    }
                                                }, true);
                                            } catch (IOException ex) {
                                            }
                                            int offset = pos[0] != -1 ? pos[0] : 0;
                                            future.complete(Either.forLeft(Collections.singletonList(new Location(Utils.toUri(generated), new Range(Utils.createPosition(generated, offset), Utils.createPosition(generated, offset))))));
                                            return;
                                        }
                                    } catch (Exception e) {
                                    }
                                    future.complete(Either.forLeft((Collections.emptyList())));
                                }
                            });
                            return future;
                        }
                    }
                }
                if (openInfo != null && (int) openInfo[1] != (-1) && (int) openInfo[2] != (-1) && openInfo[3] != null) {
                    FileObject file = (FileObject) openInfo[0];
                    int start = (int) openInfo[1];
                    int end = (int) openInfo[2];
                    LineMap lm = (LineMap) openInfo[3];
                    result.add(new Location(Utils.toUri(file),
                                            new Range(Utils.createPosition(lm, start),
                                                      Utils.createPosition(lm, end))));
                }
            } else {
                int start = target[0].offsetToOpen;
                int end = target[0].endPos;
                result.add(new Location(params.getTextDocument().getUri(),
                                        new Range(Utils.createPosition(thisFileLineMap[0], start),
                                                  Utils.createPosition(thisFileLineMap[0], end))));
            }
        }
        return CompletableFuture.completedFuture(Either.forLeft(result));
    }

    @Override
    public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
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
            JavaSource js = getSource(params.getTextDocument().getUri());
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

        JavaSource js = getSource(params.getTextDocument().getUri());
        List<DocumentHighlight> result = new ArrayList<>();
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
        JavaSource js = getSource(params.getTextDocument().getUri());
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
        Document doc = openedDocuments.get(params.getTextDocument().getUri());
        JavaSource js = JavaSource.forDocument(doc);
        if (doc == null || js == null) {
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
                        CodeAction codeAction = new CodeAction(f.getText());
                        codeAction.setKind(CodeActionKind.QuickFix);
                        codeAction.setEdit(new WorkspaceEdit(documentChanges));
                        result.add(Either.forRight(codeAction));
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

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
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
        JavaSource source = getSource(params.getTextDocument().getUri());
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
            JavaSource js = getSource(params.getTextDocument().getUri());
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
    public void didOpen(DidOpenTextDocumentParams params) {
        try {
            FileObject file = Utils.fromUri(params.getTextDocument().getUri());
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
            runDiagnoticTasks(params.getTextDocument().getUri());
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        } finally {
            reportNotificationDone("didOpen", params);
        }
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        Document doc = openedDocuments.get(params.getTextDocument().getUri());
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
        runDiagnoticTasks(params.getTextDocument().getUri());
        reportNotificationDone("didChange", params);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        try {
            FileObject file = Utils.fromUri(params.getTextDocument().getUri());
            EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
            ec.close();
            openedDocuments.remove(params.getTextDocument().getUri());
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        } finally {
            reportNotificationDone("didClose", params);
        }
    }

    @Override
    public void didSave(DidSaveTextDocumentParams arg0) {
        //TODO: nothing for now?
    }

    private void runDiagnoticTasks(String uri) {
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
            FileObject file = Utils.fromUri(uri);
            EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
            Document doc = ec.openDocument();
            ParserManager.parse(Collections.singletonList(Source.create(doc)), new UserTask() {
                @Override
                public void run(ResultIterator it) throws Exception {
                    CompilationController cc = CompilationController.get(it.getParserResult());
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
                    client.publishDiagnostics(new PublishDiagnosticsParams(uri, mergedDiags));
                }
            });
        } catch (IOException | ParseException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static final String[] ERROR_KEYS = {"errors", "hints"};

    private interface ProduceErrors {
        public List<ErrorDescription> computeErrors(CompilationInfo info, Document doc) throws IOException;
    }

    private JavaSource getSource(String fileUri) {
        Document doc = openedDocuments.get(fileUri);
        if (doc == null) {
            try {
                FileObject file = Utils.fromUri(fileUri);
                return JavaSource.forFileObject(file);
            } catch (MalformedURLException ex) {
                return null;
            }
        } else {
            return JavaSource.forDocument(doc);
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
