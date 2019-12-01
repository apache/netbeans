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
package org.netbeans.modules.java.lsp.server.text;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LineMap;
import com.sun.source.util.TreePath;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
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
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentPositionParams;
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
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.java.GoToSupport;
import org.netbeans.modules.editor.java.GoToSupport.Context;
import org.netbeans.modules.editor.java.GoToSupport.GoToTarget;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.completion.JavaCompletionTask;
import org.netbeans.modules.java.completion.JavaCompletionTask.Options;
import org.netbeans.modules.java.completion.JavaDocumentationTask;
import org.netbeans.modules.java.editor.base.semantic.MarkOccurrencesHighlighterBase;
import org.netbeans.modules.java.editor.options.MarkOccurencesSettings;
import org.netbeans.modules.java.hints.infrastructure.CreatorBasedLazyFixList;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.netbeans.modules.java.hints.spiimpl.JavaFixImpl;
import org.netbeans.modules.java.hints.spiimpl.hints.HintsInvoker;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.ui.ElementOpenAccessor;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
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
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author lahvac
 */
public class TextDocumentServiceImpl implements TextDocumentService, LanguageClientAware {

    private static final RequestProcessor BACKGROUND_TASKS = new RequestProcessor(TextDocumentServiceImpl.class.getName(), 1, false, false);

    private final Map<String, Document> openedDocuments = new HashMap<>();
    private final Map<String, RequestProcessor.Task> diagnosticTasks = new HashMap<>();
    private LanguageClient client;

    public TextDocumentServiceImpl() {
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        try {
            String uri = params.getTextDocument().getUri();
            FileObject file = fromUri(uri);
            EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
            Document doc = ec.openDocument();
            int caret = getOffset(doc, params.getPosition());
            JavaCompletionTask<CompletionItem> task = JavaCompletionTask.create(caret, new ItemFactoryImpl(uri), EnumSet.noneOf(Options.class), () -> false);
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
        this.client = client;
    }

    private static class ItemFactoryImpl implements JavaCompletionTask.ItemFactory<CompletionItem> {

        private final String uri;

        public ItemFactoryImpl(String uri) {
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
            return null; //TODO: fill
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
            item.setInsertText(elem.getSimpleName().toString());
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

        @Override
        public CompletionItem createStaticMemberItem(CompilationInfo info, DeclaredType type, Element memberElem, TypeMirror memberType, boolean multipleVersions, int substitutionOffset, boolean isDeprecated, boolean addSemicolon) {
            return null; //TODO: fill
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
            FileObject file = fromUri(data.uri);
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
    public CompletableFuture<Hover> hover(TextDocumentPositionParams arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CompletableFuture<List<? extends Location>> definition(TextDocumentPositionParams params) {
        JavaSource js = getSource(params.getTextDocument().getUri());
        GoToTarget[] target = new GoToTarget[1];
        LineMap[] lm = new LineMap[1];
        try {
            js.runUserActionTask(cc -> {
                cc.toPhase(JavaSource.Phase.RESOLVED);
                Document doc = cc.getSnapshot().getSource().getDocument(true);
                int offset = getOffset(doc, params.getPosition());
                Context context = GoToSupport.resolveContext(cc, doc, offset, false, false);
                if (context == null) {
                    return ;
                }
                target[0] = GoToSupport.computeGoToTarget(cc, context, offset);
                lm[0] = cc.getCompilationUnit().getLineMap();
            }, true);
        } catch (IOException ex) {
            //TODO: include stack trace:
            client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
        }

        List<Location> result = new ArrayList<>();

        if (target[0] != null && target[0].success) {
            if (target[0].offsetToOpen < 0) {
                Object[] openInfo = ElementOpenAccessor.getInstance().getOpenInfo(target[0].cpInfo, target[0].elementToOpen, new AtomicBoolean());
                if (openInfo != null) {
                    FileObject file = (FileObject) openInfo[0];
                    int start = (int) openInfo[1];
                    int end = (int) openInfo[2];
                    result.add(new Location(toUri(file),
                                            new Range(createPosition(lm[0], start),
                                                      createPosition(lm[0], end))));
                }
            } else {
                Position pos = createPosition(js.getFileObjects().iterator().next(), target[0].offsetToOpen);
                result.add(new Location(params.getTextDocument().getUri(),
                                        new Range(pos, pos)));
            }
        }
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<List<? extends Location>> references(ReferenceParams arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams params) {
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
                int offset = getOffset(doc, params.getPosition());
                List<int[]> spans = new MOHighligther().processImpl(cc, node, doc, offset);
                if (spans != null) {
                    for (int[] span : spans) {
                        result.add(new DocumentHighlight(new Range(createPosition(cc.getCompilationUnit(), span[0]),
                                                                   createPosition(cc.getCompilationUnit(), span[1]))));
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
        long start = info.getTrees().getSourcePositions().getStartPosition(path.getCompilationUnit(), path.getLeaf());
        long end   = info.getTrees().getSourcePositions().getEndPosition(path.getCompilationUnit(), path.getLeaf());
        if (end == (-1))
            return null;
        Range range = new Range(createPosition(info.getCompilationUnit(), (int) start),
                                createPosition(info.getCompilationUnit(), (int) end));
        List<DocumentSymbol> children = new ArrayList<>();
        for (Element c : el.getEnclosedElements()) {
            DocumentSymbol ds = element2DocumentSymbol(info, c);
            if (ds != null) {
                children.add(ds);
            }
        }
        return new DocumentSymbol(el.getSimpleName().toString(), elementKind2SymbolKind(el.getKind()), range, range, null, children);
    }

    private static SymbolKind elementKind2SymbolKind(ElementKind kind) {
        switch (kind) {
            case PACKAGE:
                return SymbolKind.Package;
            case ENUM:
                return SymbolKind.Enum;
            case CLASS:
                return SymbolKind.Class;
            case ANNOTATION_TYPE:
                return SymbolKind.Interface;
            case INTERFACE:
                return SymbolKind.Interface;
            case ENUM_CONSTANT:
                return SymbolKind.EnumMember;
            case FIELD:
                return SymbolKind.Field; //TODO: constant
            case PARAMETER:
                return SymbolKind.Variable;
            case LOCAL_VARIABLE:
                return SymbolKind.Variable;
            case EXCEPTION_PARAMETER:
                return SymbolKind.Variable;
            case METHOD:
                return SymbolKind.Method;
            case CONSTRUCTOR:
                return SymbolKind.Constructor;
            case TYPE_PARAMETER:
                return SymbolKind.TypeParameter;
            case RESOURCE_VARIABLE:
                return SymbolKind.Variable;
            case MODULE:
                return SymbolKind.Module;
            case STATIC_INIT:
            case INSTANCE_INIT:
            case OTHER:
            default:
                return SymbolKind.File; //XXX: what here?
        }
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
            ErrorDescription err = id2Errors.get(diag.getCode());

            if (err == null) {
                client.logMessage(new MessageParams(MessageType.Log, "Cannot resolve error, code: " + diag.getCode()));
                continue;
            }

            LazyFixList lfl = err.getFixes();

            if (lfl instanceof CreatorBasedLazyFixList) {
                try {
                    js.runUserActionTask(cc -> {
                        cc.toPhase(JavaSource.Phase.RESOLVED);
                        ((CreatorBasedLazyFixList) lfl).compute(cc, new AtomicBoolean());
                    }, true);
                } catch (IOException ex) {
                    //TODO: include stack trace:
                    client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
                }
            }
            List<Fix> fixes = lfl.getFixes();

            //TODO: ordering

            for (Fix f : fixes) {
                if (f instanceof JavaFixImpl) {
                    try {
                        LineMap[] lm = new LineMap[1];
                        ModificationResult changes = js.runModificationTask(wc -> {
                            wc.toPhase(JavaSource.Phase.RESOLVED);
                            Map<FileObject, byte[]> resourceContentChanges = new HashMap<FileObject, byte[]>();
                            JavaFix jf = ((JavaFixImpl) f).jf;
                            JavaFixImpl.Accessor.INSTANCE.process(jf, wc, true, resourceContentChanges, /*Ignored in editor:*/new ArrayList<>());
                            lm[0] = wc.getCompilationUnit().getLineMap();
                        });
                        //TODO: full, correct and safe edit production:
                        List<? extends ModificationResult.Difference> diffs = changes.getDifferences(changes.getModifiedFileObjects().iterator().next());
                        List<TextEdit> edits = new ArrayList<>();
                        for (ModificationResult.Difference diff : diffs) {
                            edits.add(new TextEdit(new Range(createPosition(lm[0], diff.getStartPosition().getOffset()),
                                                             createPosition(lm[0], diff.getEndPosition().getOffset())),
                                                   diff.getNewText()));
                        }
                        TextDocumentEdit te = new TextDocumentEdit(new VersionedTextDocumentIdentifier(params.getTextDocument().getUri(),
                                                                                                       -1),
                                                                   edits);
                        CodeAction action = new CodeAction(f.getText());
                        action.setDiagnostics(Collections.singletonList(diag));
                        action.setKind(CodeActionKind.QuickFix);
                        action.setEdit(new WorkspaceEdit(Collections.singletonList(te)));
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
            FileObject file = fromUri(params.getTextDocument().getUri());
            EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
            Document doc = ec.openDocument();
            openedDocuments.put(params.getTextDocument().getUri(), doc);
            String text = params.getTextDocument().getText();
            try {
                doc.remove(0, doc.getLength());
                doc.insertString(0, text, null);
            } catch (BadLocationException ex) {
                //TODO: include stack trace:
                client.logMessage(new MessageParams(MessageType.Error, ex.getMessage()));
            }
            runDiagnoticTasks(params.getTextDocument().getUri());
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        Document doc = openedDocuments.get(params.getTextDocument().getUri());
        NbDocument.runAtomic((StyledDocument) doc, () -> {
            for (TextDocumentContentChangeEvent change : params.getContentChanges()) {
                try {
                    int start = getOffset(doc, change.getRange().getStart());
                    int end   = getOffset(doc, change.getRange().getEnd());
                    doc.remove(start, end - start);
                    doc.insertString(start, change.getText(), null);
                } catch (BadLocationException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        });
        runDiagnoticTasks(params.getTextDocument().getUri());
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        openedDocuments.remove(params.getTextDocument().getUri());
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
            FileObject file = fromUri(uri);
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
                        Diagnostic diag = new Diagnostic(new Range(createPosition(cc.getCompilationUnit(), err.getRange().getBegin().getOffset()),
                                                                   createPosition(cc.getCompilationUnit(), err.getRange().getEnd().getOffset())),
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
                FileObject file = fromUri(fileUri);
                return JavaSource.forFileObject(file);
            } catch (MalformedURLException ex) {
                return null;
            }
        } else {
            return JavaSource.forDocument(doc);
        }
    }

    public static Position createPosition(CompilationUnitTree cut, int offset) {
        return createPosition(cut.getLineMap(), offset);
    }

    public static Position createPosition(LineMap lm, int offset) {
        return new Position((int) lm.getLineNumber(offset) - 1,
                            (int) lm.getColumnNumber(offset) - 1);
    }

    public static Position createPosition(FileObject file, int offset) {
        try {
            EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
            StyledDocument doc = ec.openDocument();
            int line = NbDocument.findLineNumber(doc, offset);
            int column = NbDocument.findLineColumn(doc, offset);

            return new Position(line, column);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static int getOffset(Document doc, Position pos) {
        return LineDocumentUtils.getLineStartFromIndex((LineDocument) doc, pos.getLine()) + pos.getCharacter();
    }

    private static String toUri(FileObject file) {
        if (FileUtil.isArchiveArtifact(file)) {
            //VS code cannot open jar:file: URLs, workaround:
            File cacheDir = Places.getCacheSubfile("java-server");
            File segments = new File(cacheDir, "segments");
            Properties props = new Properties();

            try (InputStream in = new FileInputStream(segments)) {
                props.load(in);
            } catch (IOException ex) {
                //OK, may not exist yet
            }
            FileObject archive = FileUtil.getArchiveFile(file);
            String archiveString = archive.toURL().toString();
            File foundSegment = null;
            for (String segment : props.stringPropertyNames()) {
                if (archiveString.equals(props.getProperty(segment))) {
                    foundSegment = new File(cacheDir, segment);
                    break;
                }
            }
            if (foundSegment == null) {
                int i = 0;
                while (props.getProperty("s" + i) != null)
                    i++;
                foundSegment = new File(cacheDir, "s" + i);
                props.put("s" + i, archiveString);
                try (OutputStream in = new FileOutputStream(segments)) {
                    props.store(in, "");
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            File cache = new File(foundSegment, FileUtil.getRelativePath(FileUtil.getArchiveRoot(archive), file));
            cache.getParentFile().mkdirs();
            try (OutputStream out = new FileOutputStream(cache)) {
                out.write(file.asBytes());
                return cache.toURI().toString();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return file.toURI().toString();
    }

    //TODO: move to a separate Utils class:
    public static FileObject fromUri(String uri) throws MalformedURLException {
        File cacheDir = Places.getCacheSubfile("java-server");
        URI uriUri = URI.create(uri);
        URI relative = cacheDir.toURI().relativize(uriUri);
        if (relative != null && new File(cacheDir, relative.toString()).canRead()) {
            String segmentAndPath = relative.toString();
            int slash = segmentAndPath.indexOf('/');
            String segment = segmentAndPath.substring(0, slash);
            String path = segmentAndPath.substring(slash + 1);
            File segments = new File(cacheDir, "segments");
            Properties props = new Properties();

            try (InputStream in = new FileInputStream(segments)) {
                props.load(in);
                String archiveUri = props.getProperty(segment);
                FileObject archive = URLMapper.findFileObject(URI.create(archiveUri).toURL());
                archive = archive != null ? FileUtil.getArchiveRoot(archive) : null;
                FileObject file = archive != null ? archive.getFileObject(path) : null;
                if (file != null) {
                    return file;
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return URLMapper.findFileObject(URI.create(uri).toURL());
    }

}
