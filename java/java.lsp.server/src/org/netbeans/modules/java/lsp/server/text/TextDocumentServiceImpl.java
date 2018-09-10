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

import com.sun.source.util.TreePath;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
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
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.completion.JavaCompletionTask;
import org.netbeans.modules.java.completion.JavaCompletionTask.Options;
import org.netbeans.modules.java.hints.infrastructure.CreatorBasedLazyFixList;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.netbeans.modules.java.hints.spiimpl.JavaFixImpl;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
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
            FileObject file = URLMapper.findFileObject(URI.create(params.getTextDocument().getUri()).toURL());
            EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
            Document doc = ec.openDocument();
            int caret = getOffset(doc, params.getPosition());
            JavaCompletionTask<CompletionItem> task = JavaCompletionTask.create(caret, new ItemFactoryImpl(), EnumSet.noneOf(Options.class), () -> false);
            ParserManager.parse(Collections.singletonList(Source.create(doc)), task);
            return CompletableFuture.completedFuture(Either.<List<CompletionItem>, CompletionList>forRight(new CompletionList(task.getResults())));
        } catch (IOException | ParseException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
    }

    private static class ItemFactoryImpl implements JavaCompletionTask.ItemFactory<CompletionItem> {

        public ItemFactoryImpl() {
        }

        @Override
        public CompletionItem createKeywordItem(String kwd, String postfix, int substitutionOffset, boolean smartType) {
            CompletionItem item = new CompletionItem(kwd);
            item.setKind(CompletionItemKind.Keyword);
            return item;
        }

        @Override
        public CompletionItem createPackageItem(String pkgFQN, int substitutionOffset, boolean inPackageStatement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CompletionItem createTypeItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addTypeVars, boolean addSimpleName, boolean smartType, boolean autoImportEnclosingType) {
            CompletionItem item = new CompletionItem(elem.getSimpleName().toString());
            item.setKind(elementKind2CompletionItemKind(elem.getKind()));
            return item;
        }

        @Override
        public CompletionItem createTypeItem(ElementHandle<TypeElement> handle, EnumSet<ElementKind> kinds, int substitutionOffset, ReferencesCount referencesCount, Source source, boolean insideNew, boolean addTypeVars, boolean afterExtends) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CompletionItem createArrayItem(CompilationInfo info, ArrayType type, int substitutionOffset, ReferencesCount referencesCount, Elements elements) {
            throw new UnsupportedOperationException("Not supported yet.");
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
            CompletionItem item = new CompletionItem(elem.getSimpleName().toString());
            item.setKind(elementKind2CompletionItemKind(elem.getKind()));
            Iterator<? extends VariableElement> it = elem.getParameters().iterator();
            Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
            StringBuilder details = new StringBuilder();
            String sep = "";
            details.append("(");
            while(it.hasNext() && tIt.hasNext()) {
                TypeMirror tm = tIt.next();
                if (tm == null) {
                    break;
                }
                details.append(sep);
                details.append(Utilities.getTypeName(info, tm, false, elem.isVarArgs() && !tIt.hasNext()).toString());
                details.append(' ');
                details.append(it.next().getSimpleName().toString());
                sep = ", ";
            }
            details.append(")");
            TypeMirror retType = type.getReturnType();
            details.append(Utilities.getTypeName(info, retType, false).toString());
            item.setDetail(details.toString());
            return item;
        }

        @Override
        public CompletionItem createThisOrSuperConstructorItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, String name) {
            CompletionItem item = new CompletionItem(name);
            item.setKind(CompletionItemKind.Field);
            return item;
        }

        @Override
        public CompletionItem createOverrideMethodItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean implement) {
            CompletionItem item = new CompletionItem(elem.getSimpleName().toString() + " - override");
            item.setKind(elementKind2CompletionItemKind(elem.getKind()));
            return item;
        }

        @Override
        public CompletionItem createGetterSetterMethodItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, String name, boolean setter) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CompletionItem createDefaultConstructorItem(TypeElement elem, int substitutionOffset, boolean smartType) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CompletionItem createParametersItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, int activeParamIndex, String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CompletionItem createAnnotationItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CompletionItem createAttributeItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CompletionItem createAttributeValueItem(CompilationInfo info, String value, String documentation, TypeElement element, int substitutionOffset, ReferencesCount referencesCount) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CompletionItem createStaticMemberItem(CompilationInfo info, DeclaredType type, Element memberElem, TypeMirror memberType, boolean multipleVersions, int substitutionOffset, boolean isDeprecated, boolean addSemicolon) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CompletionItem createStaticMemberItem(ElementHandle<TypeElement> handle, String name, int substitutionOffset, boolean addSemicolon, ReferencesCount referencesCount, Source source) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CompletionItem createChainedMembersItem(CompilationInfo info, List<? extends Element> chainedElems, List<? extends TypeMirror> chainedTypes, int substitutionOffset, boolean isDeprecated, boolean addSemicolon) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CompletionItem createInitializeAllConstructorItem(CompilationInfo info, boolean isDefault, Iterable<? extends VariableElement> fields, ExecutableElement superConstructor, TypeElement parent, int substitutionOffset) {
            throw new UnsupportedOperationException("Not supported yet.");
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

    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public CompletableFuture<List<? extends Location>> definition(TextDocumentPositionParams arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CompletableFuture<List<? extends Location>> references(ReferenceParams arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(DocumentSymbolParams params) {
        Document doc = openedDocuments.get(params.getTextDocument().getUri());
        if (doc == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        JavaSource js = JavaSource.forDocument(doc);
        List<Either<SymbolInformation, DocumentSymbol>> result = new ArrayList<>();
        try {
            js.runUserActionTask(cc -> {
                cc.toPhase(JavaSource.Phase.RESOLVED);
                for (Element tel : cc.getTopLevelElements()) {
                    DocumentSymbol ds = element2DocumentSymbol(doc, cc, tel);
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

    private DocumentSymbol element2DocumentSymbol(Document doc, CompilationInfo info, Element el) throws BadLocationException {
        TreePath path = info.getTrees().getPath(el);
        if (path == null)
            return null;
        long start = info.getTrees().getSourcePositions().getStartPosition(path.getCompilationUnit(), path.getLeaf());
        long end   = info.getTrees().getSourcePositions().getEndPosition(path.getCompilationUnit(), path.getLeaf());
        if (end == (-1))
            return null;
        Range range = new Range(createPosition(doc, (int) start), createPosition(doc, (int) end));
        List<DocumentSymbol> children = new ArrayList<>();
        for (Element c : el.getEnclosedElements()) {
            DocumentSymbol ds = element2DocumentSymbol(doc, info, c);
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
                        ModificationResult changes = js.runModificationTask(wc -> {
                            wc.toPhase(JavaSource.Phase.RESOLVED);
                            Map<FileObject, byte[]> resourceContentChanges = new HashMap<FileObject, byte[]>();
                            JavaFix jf = ((JavaFixImpl) f).jf;
                            JavaFixImpl.Accessor.INSTANCE.process(jf, wc, true, resourceContentChanges, /*Ignored in editor:*/new ArrayList<>());
                        });
                        //TODO: full, correct and safe edit production:
                        List<? extends ModificationResult.Difference> diffs = changes.getDifferences(changes.getModifiedFileObjects().iterator().next());
                        List<TextEdit> edits = new ArrayList<>();
                        for (ModificationResult.Difference diff : diffs) {
                            edits.add(new TextEdit(new Range(createPosition(doc, diff.getStartPosition().getOffset()),
                                                             createPosition(doc, diff.getEndPosition().getOffset())),
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
                    } catch (IOException | BadLocationException ex) {
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
            FileObject file = URLMapper.findFileObject(URI.create(params.getTextDocument().getUri()).toURL());
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
    public void didClose(DidCloseTextDocumentParams arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void didSave(DidSaveTextDocumentParams arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void runDiagnoticTasks(String uri) {
        diagnosticTasks.computeIfAbsent(uri, u -> {
            return BACKGROUND_TASKS.create(() -> {
                try {
                    FileObject file = URLMapper.findFileObject(URI.create(u).toURL());
                    EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
                    Document doc = ec.openDocument();
                    ParserManager.parse(Collections.singletonList(Source.create(doc)), new UserTask() {
                        @Override
                        public void run(ResultIterator it) throws Exception {
                            CompilationController cc = CompilationController.get(it.getParserResult());
                            cc.toPhase(JavaSource.Phase.RESOLVED);
                            ErrorHintsProvider ehp = new ErrorHintsProvider();
                            Map<String, ErrorDescription> id2Errors = new HashMap<>();
                            List<Diagnostic> diags = new ArrayList<>();
                            int idx = 0;
                            for (ErrorDescription err : ehp.computeErrors(cc, doc, "text/x-java")) { //TODO: mimetype?
                                Diagnostic diag = new Diagnostic(new Range(createPosition(doc, err.getRange().getBegin().getOffset()),
                                                                           createPosition(doc, err.getRange().getEnd().getOffset())),
                                                                 err.getDescription());
                                switch (err.getSeverity()) {
                                    case ERROR: diag.setSeverity(DiagnosticSeverity.Error); break;
                                    case WARNING: diag.setSeverity(DiagnosticSeverity.Warning); break;
                                    case HINT: diag.setSeverity(DiagnosticSeverity.Hint); break;
                                    default: diag.setSeverity(DiagnosticSeverity.Information); break;
                                }
                                String id = idx + "-" + err.getId();
                                diag.setCode(id);
                                id2Errors.put(id, err);
                                diags.add(diag);
                            }
                            doc.putProperty("lsp-errors", id2Errors);
                            client.publishDiagnostics(new PublishDiagnosticsParams(u, diags));
                        }
                    });
                } catch (IOException | ParseException ex) {
                    throw new IllegalStateException(ex);
                }
            });
        }).schedule(DELAY);
    }

    private static final int DELAY = 500;

    //copied from lsp.client//Utils:
    public static Position createPosition(Document doc, int offset) throws BadLocationException {
         return new Position(LineDocumentUtils.getLineIndex((LineDocument) doc, offset),
                             offset - LineDocumentUtils.getLineStart((LineDocument) doc, offset));
    }

    public static int getOffset(Document doc, Position pos) {
        return LineDocumentUtils.getLineStartFromIndex((LineDocument) doc, pos.getLine()) + pos.getCharacter();
    }

}
