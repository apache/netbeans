/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.debugger.jpda.projects;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.java.preprocessorbridge.api.JavaSourceUtil;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.debugger.jpda.EditorContext.MethodArgument;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;
import org.netbeans.spi.debugger.jpda.Evaluator.Expression;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.WeakListeners;

/**
 * Parsing with a preferred compilation controller.
 * 
 * @author Martin Entlicher
 */
class PreferredCCParser {
    
    private static final Logger LOG = Logger.getLogger(PreferredCCParser.class.getName());
    
    private final Map<JavaSource, JavaSourceUtil.Handle> sourceHandles = new WeakHashMapActive<JavaSource, JavaSourceUtil.Handle>();
    private final Map<JavaSource, Date> sourceModifStamps = new WeakHashMapActive<JavaSource, Date>();
    private DebuggerManagerListener sessionsListener; // cleans up sourceHandles
    
    PreferredCCParser() {
        sessionsListener = new SessionsListener();
        DebuggerManager.getDebuggerManager().addDebuggerListener(
                DebuggerManager.PROP_SESSIONS,
                WeakListeners.create(DebuggerManagerListener.class,
                                     sessionsListener,
                                     new SessionsListenerRemoval()));
    }
    
    private CompilationController getPreferredCompilationController(FileObject fo, JavaSource js) throws IOException {
        final CompilationController preferredCI;
        if (fo != null) {
            if (JavaSource.forFileObject(fo) == null) {
                // No JavaSource, we can not ask for a compilation controller
                return null;
            }
            Date lastModified = fo.lastModified();
            Date storedStamp;
            JavaSourceUtil.Handle handle;
            synchronized (sourceHandles) {
                handle = sourceHandles.get(js);
                storedStamp = sourceModifStamps.get(js);
            }
            if (handle == null || (storedStamp != null && lastModified.after(storedStamp))) {
                handle = JavaSourceUtil.createControllerHandle(fo, handle);
                synchronized (sourceHandles) {
                    sourceHandles.put(js, handle);
                    sourceModifStamps.put(js, lastModified);
                }
            }
            preferredCI = (CompilationController) handle.getCompilationController();
            runGuarded(preferredCI, new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    toPhase (preferredCI, JavaSource.Phase.PARSED, LOG);
                    return null;
                }
            });
        } else {
            preferredCI = null;
        }
        return preferredCI;
    }

    public Operation[] getOperations(String url, final int lineNumber,
                                     final EditorContext.BytecodeProvider bytecodeProvider,
                                     final ASTOperationCreationDelegate opCreationDelegate) {
        final FileObject file;
        try {
            file = URLMapper.findFileObject (new URL (url));
        } catch (MalformedURLException e) {
            return null;
        }
        JavaSource js = JavaSource.forFileObject(file);
        if (js == null) {
            return null;
        }
        
        final Object[] result = new Object[1];
        //long t1, t2, t3, t4;
        //t1 = System.nanoTime();
        if (SourceUtils.isScanInProgress()) {
            try {
                ParserManager.parse(Collections.singleton(Source.create(file)), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        CompilationController ci = EditorContextSupport.retrieveController(resultIterator, file);
                        if (ci == null) {
                            return;
                        }
                        if (!toPhase(ci, JavaSource.Phase.RESOLVED, LOG)) {
                            return ;
                        }
                        LineMap lineMap = ci.getCompilationUnit().getLineMap();
                        final int offset = findLineOffset(lineMap, ci.getSnapshot().getText(), (int) lineNumber);
                        result[0] = EditorContextSupport.computeOperations(
                                        ci, offset, lineNumber, bytecodeProvider,
                                        opCreationDelegate);
                    }
                });
            } catch (ParseException pex) {
                Exceptions.printStackTrace(pex);
                return null;
            }
        } else {
            try {
                final CompilationController ci = getPreferredCompilationController(file, js);
                result[0] = runGuarded(ci, new Callable<EditorContext.Operation[]>() {
                    @Override
                    public EditorContext.Operation[] call() throws Exception {
                        if (ci == null || !toPhase(ci, JavaSource.Phase.RESOLVED, LOG)) {
                            return new EditorContext.Operation[] {};
                        }
                        LineMap lineMap = ci.getCompilationUnit().getLineMap();
                        final int offset = findLineOffset(lineMap, ci.getSnapshot().getText(), (int) lineNumber);
                        return EditorContextSupport.computeOperations(
                                            ci, offset, lineNumber, bytecodeProvider,
                                            opCreationDelegate);
                    }
                });
                //t4 = System.nanoTime();
                //System.err.println("PARSE TIMES 2: "+(t2-t1)/1000000+", "+(t3-t2)/1000000+", "+(t4-t3)/1000000+" TOTAL: "+(t4-t1)/1000000+" ms.");
            } catch (IOException ioex) {
                Exceptions.printStackTrace(ioex);
                return null;
            }
        }
        //t2 = System.nanoTime();
        //System.err.println("PARSE TIME: "+(t2-t1)/1000000000+" s "+((t2-t1) % 1000000000)+" ns.");
        //System.err.printf("PARSE TIME: %d.%09d s.\n", (t2-t1)/1000000000, ((t2-t1) % 1000000000));
        return (EditorContext.Operation[])result[0];
    }

    public MethodArgument[] getArguments(String url,
                                         final EditorContext.Operation operation,
                                         final ASTOperationCreationDelegate opCreationDelegate) {
        final FileObject file;
        try {
            file = URLMapper.findFileObject (new URL (url));
        } catch (MalformedURLException e) {
            return null;
        }
        JavaSource js = JavaSource.forFileObject(file);
        if (js == null) {
            return null;
        }
        final EditorContext.MethodArgument args[][] = new EditorContext.MethodArgument[1][];
        if (SourceUtils.isScanInProgress()) {
            try {
                ParserManager.parse(Collections.singleton(Source.create(file)), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        CompilationController ci = EditorContextSupport.retrieveController(resultIterator, file);
                        if (ci == null) {
                            return;
                        }
                        args[0] = EditorContextSupport.computeMethodArguments(ci, operation, opCreationDelegate);
                    }
                });
            } catch (ParseException pex) {
                Exceptions.printStackTrace(pex);
                return null;
            }
        } else {
            try {
                final CompilationController ci = getPreferredCompilationController(file, js);
                if (ci == null) {
                    return null;
                }
                args[0] = runGuarded(ci, new Callable<EditorContext.MethodArgument[]>() {
                    @Override
                    public EditorContext.MethodArgument[] call() throws Exception {
                        return EditorContextSupport.computeMethodArguments(ci, operation, opCreationDelegate);
                    }
                });
            } catch (IOException ioex) {
                Exceptions.printStackTrace(ioex);
                return null;
            }
        }
        return args[0];
    }
    
    public MethodArgument[] getArguments(String url,
                                         final int methodLineNumber,
                                         final ASTOperationCreationDelegate opCreationDelegate) {
        final FileObject file;
        try {
            file = URLMapper.findFileObject (new URL (url));
        } catch (MalformedURLException e) {
            return null;
        }
        JavaSource js = JavaSource.forFileObject(file);
        if (js == null) {
            return null;
        }
        final EditorContext.MethodArgument args[][] = new EditorContext.MethodArgument[1][];
        if (SourceUtils.isScanInProgress()) {
            try {
                ParserManager.parse(Collections.singleton(Source.create(file)), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        CompilationController ci = EditorContextSupport.retrieveController(resultIterator, file);
                        if (ci == null || !toPhase(ci, JavaSource.Phase.RESOLVED, LOG)) {
                            return;
                        }
                        LineMap lineMap = ci.getCompilationUnit().getLineMap();
                        int offset = findLineOffset(lineMap, ci.getSnapshot().getText(), methodLineNumber);
                        args[0] = EditorContextSupport.computeMethodArguments(
                                    ci, methodLineNumber, offset,
                                    opCreationDelegate);
                    }
                });
            } catch (ParseException pex) {
                Exceptions.printStackTrace(pex);
                return null;
            }
        } else {
            try {
                final CompilationController ci = getPreferredCompilationController(file, js);
                args[0] = runGuarded(ci, new Callable<EditorContext.MethodArgument[]>() {
                    @Override
                    public EditorContext.MethodArgument[] call() throws Exception {
                        if (ci == null || !toPhase(ci, JavaSource.Phase.RESOLVED, LOG)) {
                            return null;
                        }
                        LineMap lineMap = ci.getCompilationUnit().getLineMap();
                        int offset = findLineOffset(lineMap, ci.getSnapshot().getText(), methodLineNumber);
                        return EditorContextSupport.computeMethodArguments(
                                ci, methodLineNumber, offset,
                                opCreationDelegate);
                    }
                });
            } catch (IOException ioex) {
                Exceptions.printStackTrace(ioex);
                return null;
            }
        }
        return args[0];
    }
    
    /**
     * Returns list of imports for given source url.
     *
     * @param url the url of source file
     *
     * @return list of imports for given source url
     */
    public String[] getImports(String url) {
        final FileObject file;
        try {
            file = URLMapper.findFileObject (new URL (url));
        } catch (MalformedURLException e) {
            return null;
        }
        JavaSource js;
        if (file == null || (js = JavaSource.forFileObject(file)) == null) {
            return new String [0];
        }
        final List<String> imports = new ArrayList<String>();
        if (SourceUtils.isScanInProgress()) {
            try {
                ParserManager.parse(Collections.singleton(Source.create(file)), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        CompilationController ci = EditorContextSupport.retrieveController(resultIterator, file);
                        if (ci == null) {
                            return;
                        }
                        computeImports(ci, imports);
                    }
                });
            } catch (ParseException pex) {
                Exceptions.printStackTrace(pex);
                return new String[0];
            }
        } else {
            try {
                final CompilationController ci = getPreferredCompilationController(file, js);
                if (ci == null) {
                    return null;
                }
                runGuarded(ci, new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        computeImports(ci, imports);
                        return null;
                    }
                });
            } catch (IOException ioex) {
                Exceptions.printStackTrace(ioex);
                return null;
            }
        }
        return imports.toArray(new String[0]);
    }
    
    private static void computeImports(CompilationController ci, List<String> imports) throws IOException {
        if (!toPhase(ci, JavaSource.Phase.RESOLVED, LOG)) {
            return;
        }
        List importDecl = ci.getCompilationUnit().getImports();
        int i = 0;
        for (Iterator it = importDecl.iterator(); it.hasNext(); i++) {
            ImportTree itree = (ImportTree) it.next();
            String importStr = itree.getQualifiedIdentifier().toString();
            imports.add(importStr);
        }
    }

    private static JavaSource getJavaSource(SourcePathProvider sp) {
        String[] roots = sp.getOriginalSourceRoots();
        List<FileObject> sourcePathFiles = new ArrayList<FileObject>();
        for (String root : roots) {
            FileObject fo = FileUtil.toFileObject (new java.io.File(root));
            if (fo != null && FileUtil.isArchiveFile (fo)) {
                fo = FileUtil.getArchiveRoot (fo);
            }
            sourcePathFiles.add(fo);
        }
        ClassPath bootPath = ClassPathSupport.createClassPath(new FileObject[] {});
        ClassPath classPath = ClassPathSupport.createClassPath(new FileObject[] {});
        ClassPath sourcePath = ClassPathSupport.createClassPath(sourcePathFiles.toArray(new FileObject[] {}));
        return JavaSource.create(ClasspathInfo.create(bootPath, classPath, sourcePath), new FileObject[] {});
    }

    /**
     * Parse the expression into AST tree and either traverse it via the provided interpreter,
     * or compile it into an extra method in a new class and interpret the method invocation.
     *
     * @return the visitor value or <code>null</code>.
     */
    @NbBundle.Messages("MSG_NoParseNoEval=Can not evaluate expression - parsing failed.")
    public <R,D> R interpretOrCompileCode(final Expression<Object> expression,
                                          final String url, final int line,
                                          final ErrorAwareTreePathScanner<Boolean,D> canInterpret,
                                          final ErrorAwareTreePathScanner<R,D> interpreter,
                                          final D context, boolean staticContext,
                                          final Function<Pair<String, byte[]>, Boolean> compiledClassHandler,
                                          final SourcePathProvider sp) throws InvalidExpressionException {
        final String code = expression.getExpression();
        TreePath treePath;
        Tree tree;
        Trees trees;
        ParsedData parsedData = (ParsedData) expression.getPreprocessedObject();
        if (parsedData == null) {
            JavaSource js = null;
            FileObject fo = null;
            if (url != null) {
                try {
                    fo = URLMapper.findFileObject(new URL(url));
                    if (fo != null) {
                        js = JavaSource.forFileObject(fo);
                    }
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(Exceptions.attachSeverity(ex, Level.WARNING));
                }
            }
            if (js == null) {
                js = getJavaSource(sp);
            }
            //long t1, t2, t3, t4;
            //t1 = System.nanoTime();
            try {
                final CompilationController ci = getPreferredCompilationController(fo, js);
                //t2 = System.nanoTime();
                ParseExpressionTask<D> task = new ParseExpressionTask<>(code, line, context);
                boolean parsed = task.parse(fo, js, ci);
                if (!parsed) {
                    return null;
                }
                treePath = task.getTreePath();
                tree = task.getTree();
                trees = task.getTrees();
                //t3 = System.nanoTime();
                Boolean canIntrpt;
                if (treePath != null) {
                    canIntrpt = canInterpret.scan(treePath, context);
                } else {
                    if (tree == null) {
                        throw new InvalidExpressionException(Bundle.MSG_NoParseNoEval()+" URL="+url+":"+line);
                    }
                    canIntrpt = true; // Can not compile without tree path
                }
                if (Boolean.FALSE.equals(canIntrpt)) {
                    // Can not interpret, compile:
                    ClassToInvoke compiledClass =
                            CodeSnippetCompiler.compileToClass(ci, code, task.getCodeOffset(),
                                                               js, fo, line, treePath, tree,
                                                               staticContext);
                    LOG.log(Level.FINE, "Compiled to: {0}", compiledClass);
                    if (compiledClass != null) {
                        boolean success = compiledClassHandler.apply(Pair.of(compiledClass.className, compiledClass.bytecode));
                        if (compiledClass.innerClasses != null && !compiledClass.innerClasses.isEmpty()) {
                            for (Map.Entry<String, byte[]> entry : compiledClass.innerClasses.entrySet()) {
                                success = compiledClassHandler.apply(Pair.of(entry.getKey(), entry.getValue()));
                                if (!success) {
                                    break;
                                }
                            }
                        }
                        if (success) {
                            // Class is uploaded, interpret the class' method invocation:
                            task = new ParseExpressionTask<>(compiledClass.methodInvoke, line, context);
                            parsed = task.parse(fo, js, ci);
                            if (!parsed) {
                                return null;
                            }
                            treePath = task.getTreePath();
                            tree = task.getTree();
                            trees = task.getTrees();
                        }
                    } // else when compiledClass == null, try to interpret the original anyway...
                }
            } catch (IOException ioex) {
                Exceptions.printStackTrace(ioex);
                return null;
            }
            expression.setPreprocessedObject(new ParsedData(treePath, tree, trees));
        } else {
            treePath = parsedData.getTreePath();
            tree = parsedData.getTree();
            trees = parsedData.getTrees();
            try {
                //context.setTrees(ci.getTrees());
                java.lang.reflect.Method setTreesMethod =
                        context.getClass().getMethod("setTrees", new Class[] { Trees.class });
                setTreesMethod.invoke(context, trees);
            } catch (Exception ex) {}
            if (treePath != null) {
                try {
                    //context.setTrees(ci.getTrees());
                    java.lang.reflect.Method setTreePathMethod =
                            context.getClass().getMethod("setTreePath", new Class[] { TreePath.class });
                    setTreePathMethod.invoke(context, treePath);
                } catch (Exception ex) {}
            }
        }
        R retValue;
        if (treePath != null) {
            retValue = interpreter.scan(treePath, context);
        } else {
            if (tree == null) {
                throw new InvalidExpressionException(Bundle.MSG_NoParseNoEval()+" URL="+url+":"+line);
            }
            retValue = tree.accept(interpreter, context);
        }
        //t4 = System.nanoTime();
        //System.err.println("PARSE TIMES 1: "+(t2-t1)/1000000+", "+(t3-t2)/1000000+", "+(t4-t3)/1000000+" TOTAL: "+(t4-t1)/1000000+" ms.");
        return retValue;
    }
    
    private static class ParseExpressionTask<D> implements Task<CompilationController> {

        private final int line;
        private final String expression;
        private final D context;
        private TreePath treePath;
        private Tree tree;
        private Trees trees;
        private int codeOffset;

        public ParseExpressionTask(String expression, int line, D context) {
            this.expression = expression;
            this.line = line;
            this.context = context;
        }
        
        boolean parse(FileObject fo, JavaSource js, CompilationController ci) throws IOException {
            if (fo != null && SourceUtils.isScanInProgress()) {
                try {
                    final FileObject file = fo;
                    ParserManager.parse(Collections.singleton(Source.create(fo)), new UserTask() {
                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            CompilationController ci = EditorContextSupport.retrieveController(resultIterator, file);
                            if (ci != null) {
                                ParseExpressionTask.this.run(ci);
                            }
                        }
                    });
                } catch (ParseException pex) {
                    Exceptions.printStackTrace(pex);
                    return false;
                }
            } else if (ci == null) {
                js.runUserActionTask(this, false);
            } else {
                try {
                    runGuarded(ci, new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            ParseExpressionTask.this.run(ci);
                            return null;
                        }
                    });
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    return false;
                }
            }
            return true;
        }

        @Override
        public void run(CompilationController ci) throws Exception {
            if (!toPhase(ci, JavaSource.Phase.PARSED, LOG)) {
                return ;
            }
            TreeUtilities treeUtilities = ci.getTreeUtilities();
            Scope scope = null;
            int offset = 0;
            LineMap lineMap;
            if (ci.getFileObject() != null) {
                lineMap = ci.getCompilationUnit().getLineMap();
            } else {
                lineMap = null;
            }
            if (lineMap != null) {
                offset = findLineOffset(lineMap, ci.getSnapshot().getText(), line);
                scope = treeUtilities.scopeFor(offset);
            }
            SourcePositions[] sourcePtr = new SourcePositions[] { null };
            // first, try to parse as a block of statements
            tree = treeUtilities.parseStatement(
                    "{\n" + expression + ";\n}", // NOI18N
                    sourcePtr
            );
            codeOffset = 2;
            if (isErroneous(tree)) {
                Tree asBlockTree = tree;
                // when block parsing fails, try to parse an expression
                tree = treeUtilities.parseExpression(
                        expression,
                        sourcePtr
                );
                if (isErroneous(tree)) {
                    tree = asBlockTree;
                } else {
                    codeOffset = 0;
                }
            }
            if (scope != null) {
                scope = treeUtilities.toScopeWithDisabledAccessibilityChecks(scope);
                treeUtilities.attributeTree(tree, scope);
            }
            trees = ci.getTrees();
            try {
                //context.setTrees(ci.getTrees());
                java.lang.reflect.Method setTreesMethod =
                        context.getClass().getMethod("setTrees", new Class[] { Trees.class });
                setTreesMethod.invoke(context, trees);
            } catch (Exception ex) {}
            treePath = null;
            try {
                //context.setTrees(ci.getTrees());
                java.lang.reflect.Method setTreePathMethod =
                        context.getClass().getMethod("setTreePath", new Class[] { TreePath.class });
                if (lineMap != null) {
                    treePath = treeUtilities.pathFor(offset);
                    treePath = new TreePath(treePath, tree);
                    setTreePathMethod.invoke(context, treePath);
                }
            } catch (Exception ex) {}
        }

        public TreePath getTreePath() {
            return treePath;
        }

        public Tree getTree() {
            return tree;
        }
        
        public Trees getTrees() {
            return trees;
        }
        
        public int getCodeOffset() {
            return codeOffset;
        }
    }

    private static boolean isErroneous(Tree tree) {

        class TreeChecker extends ErrorAwareTreePathScanner<Boolean,Void> {

            @Override
            public Boolean scan(Tree tree, Void p) {
                if (tree == null) {
                    return Boolean.FALSE;
                }
                if (tree.getKind() == Tree.Kind.ERRONEOUS) {
                    return Boolean.TRUE;
                }
                return tree.accept(this, p);
            }

            public Boolean visitErrorneous(ErroneousTree tree, Void p) {
                return Boolean.TRUE;
            }

        }

        Boolean result = new TreeChecker().scan(tree, null);
        return result != null && result.booleanValue();
    }

    /** return the offset of the first non-whitespace character on the line,
               or -1 when the line does not exist
     */
    private static int findLineOffset(LineMap lineMap, CharSequence text, int lineNumber) {
        int offset;
        try {
            offset = (int) lineMap.getStartPosition(lineNumber);
            int offset2 = (int) lineMap.getStartPosition(lineNumber + 1);
            CharSequence lineStr = text.subSequence(offset, offset2);
            for (int i = 0; i < lineStr.length(); i++) {
                if (!Character.isWhitespace(lineStr.charAt(i))) {
                    offset += i;
                    break;
                }
            }
        } catch (IndexOutOfBoundsException ioobex) {
            return -1;
        }
        return offset;
    }

    static boolean toPhase(CompilationController ci, JavaSource.Phase phase, Logger log) throws IOException {
        return toPhaseCheck(ci, phase, ci.toPhase(phase).compareTo(phase), log);
    }


    private static boolean toPhaseCheck(CompilationController ci, JavaSource.Phase phase, int compareToPhase, Logger log) {
        if (compareToPhase < 0) {
            log.log(Level.WARNING,
                    "Unable to resolve {0} to phase {1}, current phase = {2}\n"+
                    "Diagnostics = {3}\n"+
                    "Free memory = {4}",
                    new Object[]{ ci.getFileObject(),
                                  phase,
                                  ci.getPhase(),
                                  ci.getDiagnostics(),
                                  Runtime.getRuntime().freeMemory()});
            return false;
        } else {
            return true;
        }
    }

    private class SessionsListener extends DebuggerManagerAdapter {

        @Override
        public void sessionRemoved(Session session) {
            int numSession = DebuggerManager.getDebuggerManager().getSessions().length;
            if (numSession > 0) {
                // Trigger the check for live values
                sourceHandles.size();
                sourceModifStamps.size();
            } else {
                // No debugger sessions - clean the map
                sourceHandles.clear();
                sourceModifStamps.clear();
            }
        }

    }

    private static <T> T runGuarded(
        @NullAllowed final Object mutex,
        @NonNull final Callable<T> action) throws IOException {
        try {
            if (mutex != null) {
                synchronized (mutex) {
                    return action.call();
                }
            } else {
                return action.call();
            }
        } catch (IOException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private static class SessionsListenerRemoval {

        public void removeDebuggerListener (DebuggerManagerListener l) {
            DebuggerManager.getDebuggerManager().removeDebuggerListener(
                    DebuggerManager.PROP_SESSIONS, l);
        }
        
    }

}
