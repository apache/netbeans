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
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.prefs.Preferences;
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
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
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
import org.netbeans.modules.editor.java.GoToSupport;
import org.netbeans.modules.editor.java.GoToSupport.Context;
import org.netbeans.modules.editor.java.GoToSupport.GoToTarget;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.completion.JavaCompletionTask;
import org.netbeans.modules.java.completion.JavaCompletionTask.Options;
import org.netbeans.modules.java.completion.JavaDocumentationTask;
import org.netbeans.modules.java.editor.base.semantic.MarkOccurrencesHighlighterBase;
import org.netbeans.modules.java.editor.options.MarkOccurencesSettings;
import org.netbeans.modules.java.hints.errors.ImportClass;
import org.netbeans.modules.java.hints.infrastructure.CreatorBasedLazyFixList;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.netbeans.modules.java.hints.project.IncompleteClassPath;
import org.netbeans.modules.java.hints.spiimpl.JavaFixImpl;
import org.netbeans.modules.java.hints.spiimpl.hints.HintsInvoker;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.ui.ElementOpenAccessor;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.Places;
import org.openide.text.NbDocument;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

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
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        try {
            String uri = params.getTextDocument().getUri();
            FileObject file = Utils.fromUri(uri);
            EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
            Document doc = ec.openDocument();
            int caret = Utils.getOffset(doc, params.getPosition());
            JavaCompletionTask<CompletionItem> task = JavaCompletionTask.create(caret, new ItemFactoryImpl(client, uri), EnumSet.noneOf(Options.class), () -> false);
            ParserManager.parse(Collections.singletonList(Source.create(doc)), task);
            List<CompletionItem> result = task.getResults();
            for (Iterator<CompletionItem> it = result.iterator(); it.hasNext();) {
                CompletionItem item = it.next();
                if (item == null) {
                    it.remove();
                }
            }
            return CompletableFuture.completedFuture(Either.<List<CompletionItem>, CompletionList>forRight(new CompletionList(result)));
        } catch (IOException | ParseException ex) {
            throw new IllegalStateException(ex);
            }
        }

    public static final class CompletionData {
        public String uri;
        public String kind;
        public String[] elementHandle;

        public CompletionData() {
        }

        public CompletionData(String uri, String kind, String[] elementHandle) {
            this.uri = uri;
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

        public ItemFactoryImpl(LanguageClient client, String uri) {
            this.client = client;
            this.uri = uri;
        }

        private static final Set<String> SUPPORTED_ELEMENT_KINDS = new HashSet<>(Arrays.asList("PACKAGE", "CLASS", "INTERFACE", "ENUM", "ANNOTATION_TYPE", "METHOD", "CONSTRUCTOR", "INSTANCE_INIT", "STATIC_INIT", "FIELD", "ENUM_CONSTANT", "TYPE_PARAMETER", "MODULE"));
        private void setCompletionData(CompletionItem ci, Element el) {
            if (SUPPORTED_ELEMENT_KINDS.contains(el.getKind().name())) {
                ci.setData(new CompletionData(uri, el.getKind().name(), SourceUtils.getJVMSignature(ElementHandle.create(el))));
            }
        }

        @Override
        public CompletionItem createKeywordItem(String kwd, String postfix, int substitutionOffset, boolean smartType) {
            CompletionItem item = new CompletionItem(kwd);
            item.setKind(CompletionItemKind.Keyword);
            return item;
        }

        @Override
        public CompletionItem createPackageItem(String pkgFQN, int substitutionOffset, boolean inPackageStatement) {
            CompletionItem item = new CompletionItem(pkgFQN.substring(pkgFQN.lastIndexOf('.') + 1));
            item.setKind(CompletionItemKind.Folder);
            return item;
        }

        @Override
        public CompletionItem createTypeItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addTypeVars, boolean addSimpleName, boolean smartType, boolean autoImportEnclosingType) {
            CompletionItem item = new CompletionItem(elem.getSimpleName().toString());
            item.setKind(elementKind2CompletionItemKind(elem.getKind()));
            setCompletionData(item, elem);
            return item;
        }

        @Override
        public CompletionItem createTypeItem(ElementHandle<TypeElement> handle, EnumSet<ElementKind> kinds, int substitutionOffset, ReferencesCount referencesCount, Source source, boolean insideNew, boolean addTypeVars, boolean afterExtends) {
            return null; //TODO: fill
        }

        @Override
        public CompletionItem createArrayItem(CompilationInfo info, ArrayType type, int substitutionOffset, ReferencesCount referencesCount, Elements elements) {
            return null; //TODO: fill
        }

        @Override
        public CompletionItem createTypeParameterItem(TypeParameterElement elem, int substitutionOffset) {
            CompletionItem item = new CompletionItem(elem.getSimpleName().toString());
            item.setKind(elementKind2CompletionItemKind(elem.getKind()));
            return item;
        }

        @Override
        public CompletionItem createVariableItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean smartType, int assignToVarOffset) {
            CompletionItem item = new CompletionItem(elem.getSimpleName().toString());
            item.setKind(elementKind2CompletionItemKind(elem.getKind()));
            setCompletionData(item, elem);
            return item;
        }

        @Override
        public CompletionItem createVariableItem(CompilationInfo info, String varName, int substitutionOffset, boolean newVarName, boolean smartType) {
            CompletionItem item = new CompletionItem(varName);
            item.setKind(CompletionItemKind.Variable);
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
            while(it.hasNext() && tIt.hasNext()) {
                TypeMirror tm = tIt.next();
                if (tm == null) {
                    break;
                }
                label.append(sep);
                label.append(Utilities.getTypeName(info, tm, false, elem.isVarArgs() && !tIt.hasNext()).toString());
                label.append(' ');
                label.append(it.next().getSimpleName().toString());
                sep = ", ";
            }
            label.append(") : ");
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
            setCompletionData(item, elem);
            return item;
        }

        @Override
        public CompletionItem createThisOrSuperConstructorItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, String name) {
            CompletionItem item = new CompletionItem(name);
            item.setKind(CompletionItemKind.Field);
            setCompletionData(item, elem);
            return item;
        }

        @Override
        public CompletionItem createOverrideMethodItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean implement) {
            CompletionItem item = new CompletionItem(elem.getSimpleName().toString() + " - override");
            item.setKind(elementKind2CompletionItemKind(elem.getKind()));
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
            ElementHandle<Element> handle = ElementHandleAccessor.getInstance().create(ElementKind.valueOf(data.kind), data.elementHandle);
            JavaDocumentationTask<Future<String>> task = JavaDocumentationTask.create(-1, handle, new JavaDocumentationTask.DocumentationFactory<Future<String>>() {
                @Override
                public Future<String> create(CompilationInfo compilationInfo, Element element, Callable<Boolean> cancel) {
                    return ElementJavadoc.create(compilationInfo, element, cancel).getTextAsync();
                }
            }, () -> false);
            ParserManager.parse(Collections.singletonList(Source.create(doc)), task);
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
        return FlexmarkHtmlConverter.builder().build().convert(html).replaceAll("<br />[ \n]*$", "");
    }

    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        throw new UnsupportedOperationException("Not supported yet.");
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
                    result.completeExceptionally(new IllegalStateException(p.getMessage()));
                    return ;
                }
                p = query[0].preCheck();
                if (p != null && p.isFatal()) {
                    result.completeExceptionally(new IllegalStateException(p.getMessage()));
                    return ;
                }
                if (cancel.get()) return ;
                p = query[0].prepare(refactoring);
                if (p != null && p.isFatal()) {
                    result.completeExceptionally(new IllegalStateException(p.getMessage()));
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
        if (doc == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        Map<String, ErrorDescription> id2Errors = (Map<String, ErrorDescription>) doc.getProperty("lsp-errors");
        if (id2Errors == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        JavaSource js = JavaSource.forDocument(doc);
        List<Either<Command, CodeAction>> result = new ArrayList<>();
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
            }
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
    public CompletableFuture<WorkspaceEdit> rename(RenameParams arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
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
        openedDocuments.remove(params.getTextDocument().getUri());
        reportNotificationDone("didClose", params);
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
                        return new HintsInvoker(HintsSettings.getGlobalSettings(), new AtomicBoolean()).computeHints(info);
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
                        String id = keyPrefix + ":" + idx + "-" + err.getId();
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

    private static List<TextEdit> modify2TextEdits(JavaSource js, Task<WorkingCopy> task) throws IOException {
        FileObject[] file = new FileObject[1];
        LineMap[] lm = new LineMap[1];
        ModificationResult changes = js.runModificationTask(wc -> {
            task.run(wc);
            file[0] = wc.getFileObject();
            lm[0] = wc.getCompilationUnit().getLineMap();
        });
        //TODO: full, correct and safe edit production:
        List<? extends ModificationResult.Difference> diffs = changes.getDifferences(file[0]);
        if (diffs == null) {
            return Collections.emptyList();
        }
        List<TextEdit> edits = new ArrayList<>();
        for (ModificationResult.Difference diff : diffs) {
            String newText = diff.getNewText();
            edits.add(new TextEdit(new Range(Utils.createPosition(lm[0], diff.getStartPosition().getOffset()),
                                             Utils.createPosition(lm[0], diff.getEndPosition().getOffset())),
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
