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
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.Severity;
import org.netbeans.modules.python.editor.elements.Element;
import org.openide.text.CloneableEditorSupport;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position.Bias;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.python.editor.AstPath;
import org.netbeans.napi.gsfret.source.ClasspathInfo;
import org.netbeans.napi.gsfret.source.CompilationController;
import org.netbeans.napi.gsfret.source.ModificationResult;
import org.netbeans.napi.gsfret.source.ModificationResult.Difference;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.napi.gsfret.source.WorkingCopy;
import org.netbeans.modules.python.editor.PythonAstUtils;
import org.netbeans.modules.python.editor.PythonParserResult;
import org.netbeans.modules.python.editor.PythonStructureScanner.AnalysisResult;
import org.netbeans.modules.python.editor.PythonUtils;
import org.netbeans.modules.python.editor.elements.AstElement;
import org.netbeans.modules.python.editor.lexer.PythonLexerUtils;
import org.netbeans.modules.python.editor.scopes.SymbolTable;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileUtil;
import org.openide.text.PositionRef;
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
 * The actual Renaming refactoring work for Python.
 *
 * 
 * @todo Perform index lookups to determine the set of files to be checked!
 * @todo Check that the new name doesn't conflict with an existing name
 * @todo Check unknown files!
 * @todo More prechecks
 * @todo When invoking refactoring on a file object, I also rename the file. I should (a) list the
 *   name it's going to change the file to, and (b) definitely "filenamize" it - e.g. for class FooBar the
 *   filename should be foo_bar.
 * @todo If you rename a Model, I should add a corresponding rename_table entry in the migrations...
 *
 * @todo Complete this. Most of the prechecks are not implemented - and the refactorings themselves need a lot of work.
 */
/* Uncomment when it works ;-)
public class PythonRenameRefactoringPlugin extends PythonRefactoringPlugin {
    private PythonElementCtx treePathHandle = null;
    private Collection overriddenByMethods = null; // methods that override the method to be renamed
    private Collection overridesMethods = null; // methods that are overridden by the method to be renamed
    private boolean doCheckName = true;
    private RenameRefactoring refactoring;

    public PythonRenameRefactoringPlugin(RenameRefactoring rename) {
        this.refactoring = rename;
        PythonElementCtx tph = rename.getRefactoringSource().lookup(PythonElementCtx.class);
        if (tph != null) {
            treePathHandle = tph;
        } else {
            Source source = PythonRefUtils.getSource(rename.getRefactoringSource().lookup(FileObject.class));
            try {
                source.runUserActionTask(new CancellableTask<CompilationController>() {
                    public void cancel() {
                    }

                    public void run(CompilationController co) throws Exception {
                        co.toPhase(org.netbeans.napi.gsfret.source.Phase.RESOLVED);
                        org.python.antlr.PythonTree root = PythonAstUtils.getRoot(co);
                        if (root != null) {
                            PythonParserResult rpr = PythonAstUtils.getParseResult(co);
                            if (rpr != null) {
                                AnalysisResult ar = rpr.getStructure();
                                List<? extends AstElement> els = ar.getElements();
                                if (els.size() > 0) {
                                    // TODO - try to find the outermost or most "relevant" module/class in the file?
                                    // In Java, we look for a class with the name corresponding to the file.
                                    // It's not as simple in Python.
                                    AstElement element = els.get(0);
                                    org.python.antlr.PythonTree node = element.getNode();
                                    treePathHandle = new PythonElementCtx(root, node, element, co.getFileObject(), co);
                                    refactoring.getContext().add(co);
                                }
                            }
                        }
                    }
                }, false);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    protected Source getPythonSource(Phase p) {
        if (treePathHandle == null) {
            return null;
        }
        switch (p) {
        case PRECHECK:
        case CHECKPARAMETERS:
            if (treePathHandle == null) {
                return null;
            }
            ClasspathInfo cpInfo = getClasspathInfo(refactoring);
            return PythonRefUtils.createSource(cpInfo, treePathHandle.getFileObject());
        case FASTCHECKPARAMETERS:
            return PythonRefUtils.getSource(treePathHandle.getFileObject());

        }
        throw new IllegalStateException();
    }

    protected Problem preCheck(CompilationController info) throws IOException {
        Problem preCheckProblem = null;
        fireProgressListenerStart(RenameRefactoring.PRE_CHECK, 4);
        info.toPhase(org.netbeans.napi.gsfret.source.Phase.RESOLVED);

        // TODO - look for name clashes etc. here

        fireProgressListenerStop();
        return preCheckProblem;
    }

    protected Problem fastCheckParameters(CompilationController info) throws IOException {
        Problem fastCheckProblem = null;
        info.toPhase(org.netbeans.napi.gsfret.source.Phase.RESOLVED);
        ElementKind kind = treePathHandle.getKind();
        String newName = refactoring.getNewName();
        String oldName = treePathHandle.getSimpleName();
        if (oldName == null) {
            return new Problem(true, "Cannot determine target name. Please file a bug with detailed information on how to reproduce (preferably including the current source file and the cursor position)");
        }

        if (oldName.equals(newName)) {
            boolean nameNotChanged = true;
            //if (kind == ElementKind.CLASS || kind == ElementKind.MODULE) {
            //    if (!((TypeElement) element).getNestingKind().isNested()) {
            //        nameNotChanged = info.getFileObject().getName().equals(element);
            //    }
            //}
            if (nameNotChanged) {
                fastCheckProblem = createProblem(fastCheckProblem, true, getString("ERR_NameNotChanged"));
                return fastCheckProblem;
            }

        }

        // TODO - get a better python name picker - and check for invalid Python symbol names etc.
        // TODO - call PythonUtils.isValidLocalVariableName if we're renaming a local symbol!
        if (kind == ElementKind.CLASS && !PythonUtils.isValidPythonClassName(newName)) {
            String s = getString("ERR_InvalidClassName"); //NOI18N
            String msg = new MessageFormat(s).format(
                    new Object[]{newName});
            fastCheckProblem = createProblem(fastCheckProblem, true, msg);
            return fastCheckProblem;
        } else if (kind == ElementKind.METHOD && !PythonUtils.isValidPythonMethodName(newName)) {
            String s = getString("ERR_InvalidMethodName"); //NOI18N
            String msg = new MessageFormat(s).format(
                    new Object[]{newName});
            fastCheckProblem = createProblem(fastCheckProblem, true, msg);
            return fastCheckProblem;
        } else if (!PythonUtils.isValidPythonIdentifier(newName)) {
            String s = getString("ERR_InvalidIdentifier"); //NOI18N
            String msg = new MessageFormat(s).format(
                    new Object[]{newName});
            fastCheckProblem = createProblem(fastCheckProblem, true, msg);
            return fastCheckProblem;
        }


        String msg = PythonUtils.getIdentifierWarning(newName, 0);
        if (msg != null) {
            fastCheckProblem = createProblem(fastCheckProblem, false, msg);
        }

        // TODO - look for variable clashes etc
        return fastCheckProblem;
    }

    protected Problem checkParameters(CompilationController info) throws IOException {

        Problem checkProblem = null;
        int steps = 0;
        if (overriddenByMethods != null) {
            steps += overriddenByMethods.size();
        }
        if (overridesMethods != null) {
            steps += overridesMethods.size();
        }

        fireProgressListenerStart(RenameRefactoring.PARAMETERS_CHECK, 8 + 3 * steps);

        info.toPhase(org.netbeans.napi.gsfret.source.Phase.RESOLVED);

        fireProgressListenerStep();
        fireProgressListenerStep();
        String msg;

        // TODO - check more parameters

        fireProgressListenerStop();
        return checkProblem;
    }

    @Override
    public Problem preCheck() {
        if (treePathHandle == null || treePathHandle.getFileObject() == null || !treePathHandle.getFileObject().isValid()) {
            return new Problem(true, NbBundle.getMessage(PythonRenameRefactoringPlugin.class, "DSC_ElNotAvail")); // NOI18N
        }

        return null;
    }

    private Set<FileObject> getRelevantFiles() {
        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        final Set<FileObject> set = new HashSet<FileObject>();
        Source source = PythonRefUtils.createSource(cpInfo, treePathHandle.getFileObject());

        try {
            source.runUserActionTask(new CancellableTask<CompilationController>() {
                public void cancel() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                public void run(CompilationController info) throws Exception {
                    // TODO if getSearchInComments I -should- search all files
                    if (treePathHandle.getKind() == ElementKind.VARIABLE || treePathHandle.getKind() == ElementKind.PARAMETER) {
                        // For local variables, only look in the current file!
                        set.add(info.getFileObject());
                    } else {
                        set.addAll(PythonRefUtils.getPythonFilesInProject(info.getFileObject()));
                    }
                }
            }, true);
        } catch (IOException ioe) {
            throw (RuntimeException)new RuntimeException().initCause(ioe);
        }
        return set;
    }
    private Set<PythonElementCtx> allMethods;

    public Problem prepare(RefactoringElementsBag elements) {
        if (treePathHandle == null) {
            return null;
        }
        Set<FileObject> a = getRelevantFiles();
        fireProgressListenerStart(ProgressEvent.START, a.size());
        if (!a.isEmpty()) {
            TransformTask transform = new TransformTask(new RenameTransformer(refactoring.getNewName(), allMethods), treePathHandle);
            final Collection<ModificationResult> results = processFiles(a, transform);
            elements.registerTransaction(new PythonTransaction(results));
            for (ModificationResult result : results) {
                for (FileObject jfo : result.getModifiedFileObjects()) {
                    for (Difference diff : result.getDifferences(jfo)) {
                        String old = diff.getOldText();
                        if (old != null) {
                            //TODO: workaround
                            //generator issue?
                            elements.add(refactoring, DiffElement.create(diff, jfo, result));
                        }
                    }
                }
            }
        }
        //// see #126733. need to set a correct new name for the file rename plugin
        //// that gets invoked after this plugin when the refactoring is invoked on a file.
        //if (refactoring.getRefactoringSource().lookup(FileObject.class) != null) {
        //    String newName = PythonUtils.camelToUnderlinedName(refactoring.getNewName());
        //    refactoring.setNewName(newName);
        //}

        fireProgressListenerStop();

        return null;
    }

    private static final String getString(String key) {
        return NbBundle.getMessage(PythonRenameRefactoringPlugin.class, key);
    }

    public class RenameTransformer extends SearchVisitor {
        private Set<PythonElementCtx> allMethods;
        private String newName;
        private String oldName;
        private CloneableEditorSupport ces;
        private List<Difference> diffs;

        @Override
        public void setWorkingCopy(WorkingCopy workingCopy) {
            // Cached per working copy
            this.ces = null;
            assert diffs == null; // Should have been committed already
            super.setWorkingCopy(workingCopy);
        }

        public RenameTransformer(String newName, Set<PythonElementCtx> am) {
            this.newName = newName;
            this.oldName = treePathHandle.getSimpleName();
            this.allMethods = am;
        }

        @Override
        public void scan() {
            // TODO - do I need to force state to resolved?
            //compiler.toPhase(org.netbeans.napi.gsfret.source.Phase.RESOLVED);

            diffs = new ArrayList<Difference>();
            PythonElementCtx searchCtx = treePathHandle;
            Error error = null;
            PythonTree root = PythonAstUtils.getRoot(workingCopy);
            if (root != null) {

                Element element = AstElement.create(workingCopy, root);
                PythonTree node = searchCtx.getNode();
                PythonElementCtx fileCtx = new PythonElementCtx(root, node, element, workingCopy.getFileObject(), workingCopy);
                //PythonTree method = null;

                ElementKind kind = searchCtx.getKind();
                if (kind == ElementKind.METHOD || kind == ElementKind.CONSTRUCTOR) {
                    String name = searchCtx.getName();
                    RenameMethodVisitor visitor = new RenameMethodVisitor(name, searchCtx, fileCtx);
                    try {
                        visitor.visit(root);
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else if (kind == ElementKind.CLASS) {
                    String name = searchCtx.getName();
                    RenameClassVisitor visitor = new RenameClassVisitor(name, searchCtx, fileCtx);
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
                        rename(n, name, null, getString("UpdateLocalvar"));
                    // TODO - check arity - see OccurrencesFinder
                    // TODO - @type declarations!
                    }
                }
            } else {
                // See if the document contains references to this symbol and if so, put a warning in
                if (workingCopy.getText().indexOf(oldName) != -1) {
                    // TODO - icon??
                    if (ces == null) {
                        ces = PythonRefUtils.findCloneableEditorSupport(workingCopy);
                    }
                    int start = 0;
                    int end = 0;
                    String desc = NbBundle.getMessage(PythonRenameRefactoringPlugin.class, "ParseErrorFile", oldName);
                    List<Error> errors = workingCopy.getErrors();
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
                        start = PythonLexerUtils.getLexerOffset(workingCopy, start);
                        if (start == -1) {
                            start = 0;
                        }
                        end = start;
                    }
                    PositionRef startPos = ces.createPositionRef(start, Bias.Forward);
                    PositionRef endPos = ces.createPositionRef(end, Bias.Forward);
                    Difference diff = new Difference(Difference.Kind.CHANGE, startPos, endPos, "", "", desc); // NOI18N
                    diffs.add(diff);
                }
            }

            if (error == null && refactoring.isSearchInComments()) {
                Document doc = PythonRefUtils.getDocument(workingCopy, workingCopy.getFileObject());
                if (doc != null) {
                    //force open
                    TokenHierarchy<Document> th = TokenHierarchy.get(doc);
                    TokenSequence<?> ts = th.tokenSequence();

                    ts.move(0);

                    searchTokenSequence(ts);
                }
            }

            // Sort the diffs, if applicable
            if (diffs.size() > 0) {
                Collections.sort(diffs, new Comparator<Difference>() {
                    public int compare(Difference d1, Difference d2) {
                        return d1.getStartPosition().getOffset() - d2.getStartPosition().getOffset();
                    }
                });
                for (Difference diff : diffs) {
                    workingCopy.addDiff(diff);
                }
            }
            diffs = null;
            ces = null;

        }

        private void searchTokenSequence(TokenSequence<?> ts) {
            if (ts.moveNext()) {
                do {
                    Token<?> token = ts.token();
                    TokenId id = token.id();

                    String primaryCategory = id.primaryCategory();
                    if ("comment".equals(primaryCategory) || "block-comment".equals(primaryCategory)) { // NOI18N
                        // search this comment
                        CharSequence tokenText = token.text();
                        if (tokenText == null || oldName == null) {
                            continue;
                        }
                        int index = TokenUtilities.indexOf(tokenText, oldName);
                        if (index != -1) {
                            String text = tokenText.toString();
                            // TODO make sure it's its own word. Technically I could
                            // look at identifier chars like "_" here but since they are
                            // used for other purposes in comments, consider letters
                            // and numbers as enough
                            if ((index == 0 || !Character.isLetterOrDigit(text.charAt(index - 1))) &&
                                    (index + oldName.length() >= text.length() ||
                                    !Character.isLetterOrDigit(text.charAt(index + oldName.length())))) {
                                int start = ts.offset() + index;
                                int end = start + oldName.length();
                                if (ces == null) {
                                    ces = PythonRefUtils.findCloneableEditorSupport(workingCopy);
                                }
                                PositionRef startPos = ces.createPositionRef(start, Bias.Forward);
                                PositionRef endPos = ces.createPositionRef(end, Bias.Forward);
                                String desc = getString("ChangeComment");
                                Difference diff = new Difference(Difference.Kind.CHANGE, startPos, endPos, oldName, newName, desc);
                                diffs.add(diff);
                            }
                        }
                    } else {
                        TokenSequence<?> embedded = ts.embedded();
                        if (embedded != null) {
                            searchTokenSequence(embedded);
                        }
                    }
                } while (ts.moveNext());
            }
        }

        private void rename(PythonTree node, String oldCode, String newCode, String desc) {
            OffsetRange range = PythonAstUtils.getNameRange(null, node);
            assert range != OffsetRange.NONE;
            int pos = range.getStart();

            if (desc == null) {
                desc = NbBundle.getMessage(PythonRenameRefactoringPlugin.class, "UpdateRef", oldCode);
            }

            if (ces == null) {
                ces = PythonRefUtils.findCloneableEditorSupport(workingCopy);
            }

            // Convert from AST to lexer offsets if necessary
            pos = PythonLexerUtils.getLexerOffset(workingCopy, pos);
            if (pos == -1) {
                // Translation failed
                return;
            }

            int start = pos;
            int end = pos + oldCode.length();
            // TODO if a SymbolNode, +=1 since the symbolnode includes the ":"
            BaseDocument doc = null;
            try {
                doc = (BaseDocument)ces.openDocument();
                doc.readLock();

                if (start > doc.getLength()) {
                    start = end = doc.getLength();
                }

                if (end > doc.getLength()) {
                    end = doc.getLength();
                }

                // Look in the document and search around a bit to detect the exact method reference
                // (and adjust position accordingly). Thus, if I have off by one errors in the AST (which
                // occasionally happens) the user's source won't get munged
                if (!oldCode.equals(doc.getText(start, end - start))) {
                    // Look back and forwards by 1 at first
                    int lineStart = Utilities.getRowFirstNonWhite(doc, start);
                    int lineEnd = Utilities.getRowLastNonWhite(doc, start) + 1; // +1: after last char
                    if (lineStart == -1 || lineEnd == -1) { // We're really on the wrong line!
                        System.out.println("Empty line entry in " + FileUtil.getFileDisplayName(workingCopy.getFileObject()) +
                                "; no match for " + oldCode + " in line " + start + " referenced by node " +
                                node + " of type " + node.getClass().getName());
                        return;
                    }

                    if (lineStart < 0 || lineEnd - lineStart < 0) {
                        return; // Can't process this one
                    }

                    String line = doc.getText(lineStart, lineEnd - lineStart);
                    if (line.indexOf(oldCode) == -1) {
                        System.out.println("Skipping entry in " + FileUtil.getFileDisplayName(workingCopy.getFileObject()) +
                                "; no match for " + oldCode + " in line " + line + " referenced by node " +
                                node + " of type " + node.getClass().getName());
                    } else {
                        int lineOffset = start - lineStart;
                        int newOffset = -1;
                        // Search up and down by one
                        for (int distance = 1; distance < line.length(); distance++) {
                            // Ahead first
                            if (lineOffset + distance + oldCode.length() <= line.length() &&
                                    oldCode.equals(line.substring(lineOffset + distance, lineOffset + distance + oldCode.length()))) {
                                newOffset = lineOffset + distance;
                                break;
                            }
                            if (lineOffset - distance >= 0 && lineOffset - distance + oldCode.length() <= line.length() &&
                                    oldCode.equals(line.substring(lineOffset - distance, lineOffset - distance + oldCode.length()))) {
                                newOffset = lineOffset - distance;
                                break;
                            }
                        }

                        if (newOffset != -1) {
                            start = newOffset + lineStart;
                            end = start + oldCode.length();
                        }
                    }
                }
            } catch (IOException ie) {
                Exceptions.printStackTrace(ie);
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            } finally {
                if (doc != null) {
                    doc.readUnlock();
                }
            }

            if (newCode == null) {
                // Usually it's the new name so allow client code to refer to it as just null
                newCode = refactoring.getNewName(); // XXX isn't this == our field "newName"?
            }

            PositionRef startPos = ces.createPositionRef(start, Bias.Forward);
            PositionRef endPos = ces.createPositionRef(end, Bias.Forward);
            Difference diff = new Difference(Difference.Kind.CHANGE, startPos, endPos, oldCode, newCode, desc);
            diffs.add(diff);
        }
*/
        /**
         * @todo P1: This is matching method names on classes that have nothing to do with the class we're searching for
         *   - I've gotta filter fields, methods etc. that are not in the current class
         *  (but I also have to search for methods that are OVERRIDING the class... so I've gotta work a little harder!)
         * @todo Arity matching on the methods to preclude methods that aren't overriding or aliasing!
         */
/*        private class RenameMethodVisitor extends Visitor {
            private String name;
            private PythonElementCtx searchCtx;
            private PythonElementCtx fileCtx;

            public RenameMethodVisitor(String name, PythonElementCtx searchCtx, PythonElementCtx fileCtx) {
                this.name = name;
                this.searchCtx = searchCtx;
                this.fileCtx = fileCtx;
            }

            @Override
            public Object visitCall(Call node) throws Exception {
                String callName = PythonAstUtils.getCallName(node);
                if (name.equals(callName)) {
                    // TODO - check arity - see OccurrencesFinder
                    rename(node, name, null, getString("UpdateCall"));
                }
                return super.visitCall(node);
            }

            @Override
            public Object visitFunctionDef(FunctionDef node) throws Exception {
                if (name.equals(node.getInternalName())) {
                    rename(node, name, null, getString("UpdateMethodDef"));
                }

                return super.visitFunctionDef(node);
            }
        }

        ** @todo Rename!*
        private class RenameClassVisitor extends Visitor {
            private String name;
            private PythonElementCtx searchCtx;
            private PythonElementCtx fileCtx;

            public RenameClassVisitor(String name, PythonElementCtx searchCtx, PythonElementCtx fileCtx) {
                this.name = name;
                this.searchCtx = searchCtx;
                this.fileCtx = fileCtx;
            }

            @Override
            public Object visitClassDef(ClassDef node) throws Exception {
                if (name.equals(node.getInternalName())) {
                    rename(node, name, null, getString("UpdateClassDef"));
                }
                List<expr> bases = node.getInternalBases();
                if (bases != null) {
                    for (expr base : bases) {
                        String extendsName = PythonAstUtils.getExprName(base);
                        if (extendsName != null && extendsName.equals(name)) {
                            //rename(node, name, null, null);
                            rename(base, name, null, null);
                        }
                    }
                }

                return super.visitClassDef(node);
            }

            @Override
            public Object visitName(Name node) throws Exception {
                if (name.equals(node.getInternalId())) {
                    rename(node, name, null, getString("UpdateRef"));
                }
                return super.visitName(node);
            }
        }
    }
}
*/
