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
package org.netbeans.modules.python.editor.refactoring;
/*
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.Severity;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.python.editor.AstPath;
import org.netbeans.modules.python.editor.PythonAstUtils;
import org.netbeans.modules.python.editor.PythonIndex;
import org.netbeans.modules.python.editor.elements.AstElement;
import org.netbeans.modules.python.editor.elements.Element;
import org.netbeans.modules.python.editor.elements.IndexedElement;
import org.netbeans.modules.python.editor.lexer.PythonLexerUtils;
import org.netbeans.modules.python.editor.lexer.PythonTokenId;
import org.netbeans.modules.python.editor.scopes.SymbolTable;
import org.netbeans.napi.gsfret.source.ClasspathInfo;
import org.netbeans.napi.gsfret.source.CompilationController;
import org.netbeans.napi.gsfret.source.CompilationInfo;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.napi.gsfret.source.UiUtils;
import org.netbeans.napi.gsfret.source.WorkingCopy;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Name;
import org.python.antlr.base.expr;
*/
/**
 * Actual implementation of Find Usages query search for Python
 * 
 * @todo Perform index lookups to determine the set of files to be checked!
 * @todo Do more prechecks of the elements we're trying to find usages for
 * 
 */
/* Uncomment when it works ;-)
public class PythonWhereUsedQueryPlugin extends PythonRefactoringPlugin {
    private WhereUsedQuery refactoring;
    private PythonElementCtx searchHandle;
    private Set<IndexedElement> subclasses;
    private String targetName;

    public PythonWhereUsedQueryPlugin(WhereUsedQuery refactoring) {
        this.refactoring = refactoring;
        this.searchHandle = refactoring.getRefactoringSource().lookup(PythonElementCtx.class);
        targetName = searchHandle.getSimpleName();
    }

    protected Source getPythonSource(Phase p) {
        switch (p) {
        default:
            return PythonRefUtils.getSource(searchHandle.getFileObject());
        }
    }

    @Override
    public Problem preCheck() {
        if (!searchHandle.getFileObject().isValid()) {
            return new Problem(true, NbBundle.getMessage(PythonWhereUsedQueryPlugin.class, "DSC_ElNotAvail")); // NOI18N
        }

        return null;
    }

    @Override
    protected Problem preCheck(CompilationController info) {
        return null;
    }

    private Set<FileObject> getRelevantFiles(final PythonElementCtx tph) {
        final ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        //final ClassIndex idx = cpInfo.getClassIndex();
        final Set<FileObject> set = new HashSet<FileObject>();

        final FileObject file = tph.getFileObject();
        Source source;
        if (file != null) {
            set.add(file);
            source = PythonRefUtils.createSource(cpInfo, tph.getFileObject());
        } else {
            source = Source.create(cpInfo);
        }
        //XXX: This is slow!
        CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
            public void cancel() {
            }

            public void run(CompilationController info) throws Exception {
                info.toPhase(org.netbeans.napi.gsfret.source.Phase.RESOLVED);
                //System.out.println("TODO - compute a full set of files to be checked... for now just lamely using the project files");
                //set.add(info.getFileObject());
                // (This currently doesn't need to run in a compilation controller since I'm not using parse results at all...)


                if (isFindSubclasses() || isFindDirectSubclassesOnly()) {
                    // No need to do any parsing, we'll be using the index to find these files!
                    set.add(info.getFileObject());

                    String name = tph.getName();

                    // Find overrides of the class
                    PythonIndex index = PythonIndex.get(info.getIndex(PythonTokenId.PYTHON_MIME_TYPE), info.getFileObject());
                    String fqn = PythonAstUtils.getFqnName(tph.getPath());
                    Set<IndexedElement> classes = index.getSubClasses(null, fqn, name, isFindDirectSubclassesOnly());

                    if (classes.size() > 0) {
                        subclasses = classes;
                        //for (IndexedClass clz : classes) {
                        //    FileObject fo = clz.getFileObject();
                        //    if (fo != null) {
                        //        set.add(fo);
                        //    }
                        //}
                        // For now just parse this particular file!
                        set.add(info.getFileObject());
                        return;
                    } else {
                        subclasses = Collections.emptySet();
                        return;
                    }
                }

                if (tph.getKind() == ElementKind.VARIABLE || tph.getKind() == ElementKind.PARAMETER) {
                    // For local variables, only look in the current file!
                    set.add(info.getFileObject());
                } else {
                    set.addAll(PythonRefUtils.getPythonFilesInProject(info.getFileObject()));
                }
            }
        };
        try {
            source.runUserActionTask(task, true);
        } catch (IOException ioe) {
            throw (RuntimeException)new RuntimeException().initCause(ioe);
        }
        return set;
    }

    //@Override
    public Problem prepare(final RefactoringElementsBag elements) {
        Set<FileObject> a = getRelevantFiles(searchHandle);
        fireProgressListenerStart(ProgressEvent.START, a.size());
        processFiles(a, new FindTask(elements));
        fireProgressListenerStop();
        return null;
    }

    //@Override
    protected Problem fastCheckParameters(CompilationController info) {
        if (targetName == null) {
            return new Problem(true, "Cannot determine target name. Please file a bug with detailed information on how to reproduce (preferably including the current source file and the cursor position)");
        }
        if (searchHandle.getKind() == ElementKind.METHOD) {
            return checkParametersForMethod(isFindOverridingMethods(), isFindUsages());
        }
        return null;
    }

    //@Override
    protected Problem checkParameters(CompilationController info) {
        return null;
    }

    private Problem checkParametersForMethod(boolean overriders, boolean usages) {
        if (!(usages || overriders)) {
            return new Problem(true, NbBundle.getMessage(PythonWhereUsedQueryPlugin.class, "MSG_NothingToFind"));
        } else {
            return null;
        }
    }

    private boolean isFindSubclasses() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.FIND_SUBCLASSES);
    }

    private boolean isFindUsages() {
        return refactoring.getBooleanValue(WhereUsedQuery.FIND_REFERENCES);
    }

    private boolean isFindDirectSubclassesOnly() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES);
    }

    private boolean isFindOverridingMethods() {
        return refactoring.getBooleanValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS);
    }

    private boolean isSearchFromBaseClass() {
        return false;
    }

    private boolean isSearchInComments() {
        return refactoring.getBooleanValue(WhereUsedQuery.SEARCH_IN_COMMENTS);
    }

    private class FindTask implements CancellableTask<WorkingCopy> {
        private RefactoringElementsBag elements;
        private volatile boolean cancelled;

        public FindTask(RefactoringElementsBag elements) {
            super();
            this.elements = elements;
        }

        public void cancel() {
            cancelled = true;
        }

        public void run(WorkingCopy compiler) throws IOException {
            if (cancelled) {
                return;
            }
            compiler.toPhase(org.netbeans.napi.gsfret.source.Phase.RESOLVED);

            Error error = null;

            PythonElementCtx searchCtx = searchHandle;

            PythonTree root = PythonAstUtils.getRoot(compiler);

            if (root == null) {
                // See if the document contains references to this symbol and if so, put a warning in
                String sourceText = compiler.getText();
                if (sourceText != null && sourceText.indexOf(targetName) != -1) {
                    int start = 0;
                    int end = 0;
                    String desc = "Parse error in file which contains " + targetName + " reference - skipping it";
                    List<Error> errors = compiler.getErrors();
                    if (errors.size() > 0) {
                        for (Error e : errors) {
                            if (e.getSeverity() == Severity.ERROR) {
                                error = e;
                                break;
                            }
                        }
                        if (error == null) {
                            error = errors.get(0);
                        }

                        String errorMsg = error.getDisplayName();

                        if (errorMsg.length() > 80) {
                            errorMsg = errorMsg.substring(0, 77) + "..."; // NOI18N
                        }

                        desc = desc + "; " + errorMsg;
                        start = error.getStartPosition();
                        start = PythonLexerUtils.getLexerOffset(compiler, start);
                        if (start == -1) {
                            start = 0;
                        }
                        end = start;
                    }

                    Set<Modifier> modifiers = Collections.emptySet();
                    Icon icon = UiUtils.getElementIcon(ElementKind.ERROR, modifiers);
                    OffsetRange range = new OffsetRange(start, end);
                    WhereUsedElement element = WhereUsedElement.create(compiler, targetName, desc, range, icon);
                    elements.add(refactoring, element);
                }
            }

            if (error == null && isSearchInComments()) {
                Document doc = PythonRefUtils.getDocument(compiler, compiler.getFileObject());
                if (doc != null) {
                    //force open
                    TokenHierarchy<Document> th = TokenHierarchy.get(doc);
                    TokenSequence<?> ts = th.tokenSequence();

                    ts.move(0);

                    searchTokenSequence(compiler, ts);
                }
            }

            if (root == null) {
                // TODO - warn that this file isn't compileable and is skipped?
                fireProgressListenerStep();
                return;
            }

            Element element = AstElement.create(compiler, root);
            PythonTree node = searchCtx.getNode();
            PythonElementCtx fileCtx = new PythonElementCtx(root, node, element, compiler.getFileObject(), compiler);

            // If it's a local search, use a simpler search routine
            // TODO: ArgumentNode - look to see if we're in a parameter list, and if so its a localvar
            // (if not, it's a method)

            if (isFindSubclasses() || isFindDirectSubclassesOnly()) {
                // I'm only looking for the specific classes
                assert subclasses != null;
                // Look in these files for the given classes
                //findSubClass(root);
                for (IndexedElement clz : subclasses) {
                    PythonElementCtx matchCtx = new PythonElementCtx(clz);
                    if (matchCtx.getNode() != null) {
                        elements.add(refactoring, WhereUsedElement.create(matchCtx));
                    }
                }
            } else if (isFindUsages()) {
                ElementKind kind = searchCtx.getKind();
                if (kind == ElementKind.METHOD || kind == ElementKind.CONSTRUCTOR) {
                    String name = searchCtx.getName();
                    FindUsagesMethodVisitor visitor = new FindUsagesMethodVisitor(name, searchCtx, fileCtx, elements);
                    try {
                        visitor.visit(root);
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else if (kind == ElementKind.CLASS) {
                    String name = searchCtx.getName();
                    FindUsagesClassVisitor visitor = new FindUsagesClassVisitor(name, searchCtx, fileCtx, elements);
                    try {
                        visitor.visit(root);
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else if (kind == ElementKind.VARIABLE || kind == ElementKind.PARAMETER) {
                    String name = searchCtx.getName();
                    // We can use the searchCtx because it should be the same file
                    // as the fileCtx - local search only!
                    assert searchCtx.getInfo().getFileObject() == fileCtx.getInfo().getFileObject() : fileCtx.getInfo().getFileObject();
                    AstPath path = searchCtx.getPath();
                    PythonTree scope = PythonAstUtils.getLocalScope(path);
                    SymbolTable symbolTable = PythonAstUtils.getParseResult(searchCtx.getInfo()).getSymbolTable();
                    List<PythonTree> nodes = symbolTable.getOccurrences(scope, name, false);
                    for (PythonTree n : nodes) {
                        // TODO - check arity - see OccurrencesFinder
                        PythonElementCtx matchCtx = new PythonElementCtx(searchCtx, n);
                        elements.add(refactoring, WhereUsedElement.create(matchCtx));
                    }
                    // TODO - @type declarations!
                }
            } else if (isFindOverridingMethods()) {
                // TODO
            } else if (isSearchFromBaseClass()) {
                // TODO
            }
            fireProgressListenerStep();
        }

        private void searchTokenSequence(CompilationInfo info, TokenSequence<?> ts) {
            if (ts.moveNext()) {
                do {
                    Token<?> token = ts.token();
                    TokenId id = token.id();

                    String primaryCategory = id.primaryCategory();
                    if ("comment".equals(primaryCategory) || "block-comment".equals(primaryCategory)) { // NOI18N
                        // search this comment
                        assert targetName != null;
                        CharSequence tokenText = token.text();
                        if (tokenText == null || targetName == null) {
                            continue;
                        }
                        int index = TokenUtilities.indexOf(tokenText, targetName);
                        if (index != -1) {
                            String text = tokenText.toString();
                            // TODO make sure it's its own word. Technically I could
                            // look at identifier chars like "_" here but since they are
                            // used for other purposes in comments, consider letters
                            // and numbers as enough
                            if ((index == 0 || !Character.isLetterOrDigit(text.charAt(index - 1))) &&
                                    (index + targetName.length() >= text.length() ||
                                    !Character.isLetterOrDigit(text.charAt(index + targetName.length())))) {
                                int start = ts.offset() + index;
                                int end = start + targetName.length();

                                // TODO - get a comment-reference icon? For now, just use the icon type
                                // of the search target
                                Set<Modifier> modifiers = Collections.emptySet();
                                if (searchHandle.getElement() != null) {
                                    modifiers = searchHandle.getElement().getModifiers();
                                }
                                Icon icon = UiUtils.getElementIcon(searchHandle.getKind(), modifiers);
                                OffsetRange range = new OffsetRange(start, end);
                                WhereUsedElement element = WhereUsedElement.create(info, targetName, range, icon);
                                elements.add(refactoring, element);
                            }
                        }
                    } else {
                        TokenSequence<?> embedded = ts.embedded();
                        if (embedded != null) {
                            searchTokenSequence(info, embedded);
                        }
                    }
                } while (ts.moveNext());
            }
        }
    }

    /**
     * @todo P1: This is matching method names on classes that have nothing to do with the class we're searching for
     *   - I've gotta filter fields, methods etc. that are not in the current class
     *  (but I also have to search for methods that are OVERRIDING the class... so I've gotta work a little harder!)
     * @todo Arity matching on the methods to preclude methods that aren't overriding or aliasing!
     *
    private class FindUsagesMethodVisitor extends Visitor {
        private String name;
        private PythonElementCtx searchCtx;
        private PythonElementCtx fileCtx;
        private RefactoringElementsBag elements;

        public FindUsagesMethodVisitor(String name, PythonElementCtx searchCtx, PythonElementCtx fileCtx, RefactoringElementsBag elements) {
            this.name = name;
            this.searchCtx = searchCtx;
            this.fileCtx = fileCtx;
            this.elements = elements;
        }

        @Override
        public Object visitCall(Call node) throws Exception {
            String callName = PythonAstUtils.getCallName(node);
            if (name.equals(callName)) {
                // TODO - check arity - see OccurrencesFinder
                PythonElementCtx matchCtx = new PythonElementCtx(fileCtx, node);
                elements.add(refactoring, WhereUsedElement.create(matchCtx));
            }
            return super.visitCall(node);
        }

        @Override
        public Object visitFunctionDef(FunctionDef node) throws Exception {
            if (name.equals(node.getInternalName())) {
                PythonElementCtx matchCtx = new PythonElementCtx(fileCtx, node);
                elements.add(refactoring, WhereUsedElement.create(matchCtx));
            }

            return super.visitFunctionDef(node);
        }
    }

    private class FindUsagesClassVisitor extends Visitor {
        private String name;
        private PythonElementCtx searchCtx;
        private PythonElementCtx fileCtx;
        private RefactoringElementsBag elements;

        public FindUsagesClassVisitor(String name, PythonElementCtx searchCtx, PythonElementCtx fileCtx, RefactoringElementsBag elements) {
            this.name = name;
            this.searchCtx = searchCtx;
            this.fileCtx = fileCtx;
            this.elements = elements;
        }

        @Override
        public Object visitClassDef(ClassDef node) throws Exception {
            if (name.equals(node.getInternalName())) {
                PythonElementCtx matchCtx = new PythonElementCtx(fileCtx, node);
                elements.add(refactoring, WhereUsedElement.create(matchCtx));
            }
            List<expr> bases = node.getInternalBases();
            if (bases != null) {
                for (expr base : bases) {
                    String extendsName = PythonAstUtils.getExprName(base);
                    if (extendsName != null && extendsName.equals(name)) {
                        //PythonElementCtx matchCtx = new PythonElementCtx(fileCtx, node);
                        PythonElementCtx matchCtx = new PythonElementCtx(fileCtx, base);
                        elements.add(refactoring, WhereUsedElement.create(matchCtx));
                    }
                }
            }

            return super.visitClassDef(node);
        }

        @Override
        public Object visitName(Name node) throws Exception {
            if (name.equals(node.getInternalId())) {
                PythonElementCtx matchCtx = new PythonElementCtx(fileCtx, node);
                elements.add(refactoring, WhereUsedElement.create(matchCtx));
            }
            return super.visitName(node);
        }
    }
}
*/
