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

package org.netbeans.modules.refactoring.java.callhierarchy;

import com.sun.source.tree.*;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.awt.EventQueue;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.plugins.FindUsagesVisitor;
import org.netbeans.modules.refactoring.java.plugins.JavaPluginUtils;
import org.netbeans.modules.refactoring.java.plugins.JavaWhereUsedQueryPlugin;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Pokorsky
 */
final class CallHierarchyTasks {

    private static final RequestProcessor RP = new RequestProcessor("Call Hierarchy Processor", 1); // NOI18N
    
    private static final Object LOCK = new Object();
    private static final List<CancellableTask> CURR_TASK = new LinkedList<CancellableTask>();
    private static CancellableTask CLEAN_TASK;
    
    public static void stop() {
        synchronized (LOCK) {
            ListIterator<CancellableTask> listIterator = CURR_TASK.listIterator();
            while(listIterator.hasNext()) {
                CancellableTask task = listIterator.next();
                task.cancel();
                listIterator.remove();
            }
        }
    }
    
    public static void findCallers(Call c, boolean includeTest, boolean searchAll, Runnable resultHandler) {
        final CallersTask callersTask = new CallersTask(c, resultHandler, includeTest, searchAll);
        synchronized (LOCK) {
            RequestProcessor.Task task = RP.post(callersTask);
            updateCleaner(task);
            CURR_TASK.add(callersTask);
        }
    }
    
    public static void findCallees(Call c, Runnable resultHandler) {
        final CalleesTask calleesTask = new CalleesTask(c, resultHandler);
        synchronized (LOCK) {
            RequestProcessor.Task task = RP.post(calleesTask);
            updateCleaner(task);
            CURR_TASK.add(calleesTask);
        }
    }
    
    public static void resolveRoot(final Lookup lookup, final boolean searchFromBase, final boolean isCallerGraph, final Task<Call> rootCallback) {
        JavaSource js = null;
        RootResolver resolver = null;
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (ec != null/*RefactoringActionsProvider.isFromEditor(ec)*/) {
            JEditorPane openedPane = NbDocument.findRecentEditorPane(ec);
            Document doc = ec.getDocument();
            js = JavaSource.forDocument(doc);
            resolver = new RootResolver(openedPane.getCaretPosition(), isCallerGraph, searchFromBase);
        }
//        else {
            // XXX resolve Node.class
//        }
        postResolveRoot(js, resolver, rootCallback);
    }   
    
    static void resolveRoot(TreePathHandle selection, final boolean searchFromBase, boolean isCallerGraph, Task<Call> rootCallback) {
        JavaSource js = JavaSource.forFileObject(selection.getFileObject());
        if (js==null) {
            Logger.getLogger(CallHierarchyTasks.class.getName()).log(Level.INFO, "Cannot get JavaSource for {0}", selection.getFileObject().getPath());
            return;
        }
        postResolveRoot(js, new RootResolver(selection, isCallerGraph, searchFromBase), rootCallback);
    }
    
//    static void  resolveRoot(JavaSource src, int position, boolean isCallerGraph, Task<Call> rootCallback) {
//        RootResolver rr = new RootResolver(position, isCallerGraph);
//        postResolveRoot(src, rr, rootCallback);
//    }
    
    private static void  postResolveRoot(final JavaSource src, final RootResolver rr, final Task<Call> callback) {
        stop();
        RP.post(new Runnable() {
            @Override
            public void run() {
                synchronized (callback) {
                    Task<CompilationController> ct = new Task<CompilationController>() {
                        @Override
                        public void run(CompilationController parameter) throws Exception {
                            synchronized (callback) {
                                rr.run(parameter);
                                callback.run(rr.getRoot());
                            }
                        }
                    };
                    final Future<Void> rootResolve = ScanUtils.postUserActionTask(src, ct);
                    // still synchronized
                    if (!rootResolve.isDone()) {
                        try {
                            Call tempNode = Call.createEmpty();
                            callback.run(tempNode);
                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    CallHierarchyTopComponent.findInstance().setRunningState(true);
                                }
                            });
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        });
    }

    private static void updateCleaner(final RequestProcessor.Task task) {
        if(CLEAN_TASK != null) {
            CLEAN_TASK.cancel();
        }
        CLEAN_TASK = new CleanTask(task);
        final CancellableTask cleanTask = CLEAN_TASK;
        RP.post(new Runnable() {

            @Override
            public void run() {
                try {
                    cleanTask.run(null);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }
    
    static final class RootResolver implements Task<CompilationController> {
        
        private int offset = -1;
        private TreePathHandle tHandle;
        private final boolean isCallerGraph;
        private final boolean searchFromBase;
        private Call root;

        public RootResolver(TreePathHandle tHandle, boolean isCallerGraph, boolean searchFromBase) {
            this.tHandle = tHandle;
            this.isCallerGraph = isCallerGraph;
            this.searchFromBase = searchFromBase;
        }

        public RootResolver(int offset, boolean isCallerGraph, boolean searchFromBase) {
            this.offset = offset;
            this.isCallerGraph = isCallerGraph;
            this.searchFromBase = searchFromBase;
        }

        @Override
        public void run(CompilationController javac) throws Exception {
            TreePath tpath = null;
            Element method = null;
            
            javac.toPhase(JavaSource.Phase.RESOLVED);
            if (tHandle == null) {
                tpath = javac.getTreeUtilities().pathFor(offset);
            } else {
                tpath = tHandle.resolve(javac);
            }
            
            while (tpath != null) {
                Kind kind = tpath.getLeaf().getKind();
                if (kind == Kind.METHOD || kind == Kind.METHOD_INVOCATION || kind == Kind.MEMBER_SELECT || kind == Kind.NEW_CLASS) {
                    method = ScanUtils.checkElement(javac, javac.getTrees().getElement(tpath));
                    if (RefactoringUtils.isExecutableElement(method)) {
                        break;
                    }
                    method = null;
                }
                tpath = tpath.getParentPath();
            }
            
            if (method != null) {
                if(isCallerGraph && this.searchFromBase) {
                    Collection<ExecutableElement> overriddenMethods = JavaRefactoringUtils.getOverriddenMethods((ExecutableElement)method, javac);
                    if(!overriddenMethods.isEmpty()) {
                        method = overriddenMethods.iterator().next();
                    }
                }
                root = Call.createRoot(javac, tpath, method, isCallerGraph);
            }
        }
        
        public Call getRoot() {
            return root;
        }
        
    }
    
    private abstract static class CallTaskBase implements Runnable, CancellableTask<CompilationController> {
        
        protected final Call elmDesc;
        private final Runnable resultHandler;
        AtomicBoolean isCanceled = new AtomicBoolean(false);
        protected final List<Call> result = new ArrayList<Call>();

        protected abstract void runTask() throws Exception;

        public CallTaskBase(Call elmDesc, Runnable resultHandler) {
            this.elmDesc = elmDesc;
            this.resultHandler = resultHandler;
        }
        
        private void notifyRunning(final boolean isRunning) {
            try {
                EventQueue.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        CallHierarchyTopComponent.findInstance().setRunningState(isRunning);
                    }
                });
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        @Override
        public void cancel() {
            isCanceled.set(true);
        }
        
        protected boolean isCanceled() {
            if (Thread.interrupted()) {
                isCanceled.set(true);
            }
            return isCanceled.get();
        }
        
        @Override
        public void run() {
            try {
                notifyRunning(true);

                runTask();
                elmDesc.setCanceled(isCanceled());
                elmDesc.setReferences(result);
                resultHandler.run();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                elmDesc.setCanceled(isCanceled());
                notifyRunning(false);
            }
        }
    }
    
    private static final class CallersTask extends CallTaskBase {
        private final boolean includeTest;
        private final boolean searchAll;
        
        public CallersTask(Call elmDesc, Runnable resultHandler, boolean includeTest, boolean searchAll) {
            super(elmDesc, resultHandler);
            this.includeTest = includeTest;
            this.searchAll = searchAll;
        }
        
        @Override
        public void runTask() throws Exception {
            TreePathHandle sourceToQuery = elmDesc.getSourceToQuery();
            if (isCanceled()) {
                return;
            }
            // validate source
            // TODO: what is this?
            //if (RefactoringUtils.getElementHandle(sourceToQuery) == null) {
            //    elmDesc.setBroken();
            //} else {
                ClasspathInfo cpInfo;
                if (searchAll) {
                    cpInfo = RefactoringUtils.getClasspathInfoFor(true, sourceToQuery.getFileObject());
                } else {
                    cpInfo = RefactoringUtils.getClasspathInfoFor(false, elmDesc.selection.getFileObject());
                }

                Set<FileObject> relevantFiles = null;
                if (!isCanceled()) {
                    relevantFiles = JavaWhereUsedQueryPlugin.getRelevantFiles(
                        sourceToQuery, cpInfo, false, false, false, false, true, false, false, null, isCanceled);
                    if (SourceUtils.isScanInProgress()) {
                        elmDesc.setIncomplete(true);
                    }
                }
                if (!isCanceled()) {
                    processFiles(relevantFiles, this, null);
                }
            //}
        }
        
        @Override
        public void run(CompilationController javac) throws Exception {
            if (isCanceled()) {
                return;
            }
            if (javac.toPhase(JavaSource.Phase.RESOLVED) != JavaSource.Phase.RESOLVED) {
                return;
            }
            Element wanted = elmDesc.getSourceToQuery().resolveElement(javac);
            if (wanted == null) {
                // XXX log it
                return;
            }
            FindUsagesVisitor findVisitor = new FindUsagesVisitor(javac, isCanceled, false, false);
            findVisitor.scan(javac.getCompilationUnit(), wanted);
            Collection<TreePath> usages = findVisitor.getUsages();
            Map<Element, OccurrencesDesc> refs = new HashMap<Element, OccurrencesDesc>();
            int order = 0;
            
            for (TreePath treePath : usages) {
                if (isCanceled()) {
                    return;
                }
                TreePath declarationPath = resolveDeclarationContext(treePath);
                if (declarationPath == null) {
                    // XXX log unknown path
                    continue;
                }
                
                Element elm = null;
                if (declarationPath.getLeaf().getKind() != Kind.BLOCK) {
                    elm = javac.getTrees().getElement(declarationPath);
                } else {
                    // initializer
                    Element enclosing = javac.getTrees().getElement(declarationPath.getParentPath());
                    BlockTree block = (BlockTree) declarationPath.getLeaf();
                    elm = enclosing == null ? null : new InitializerElement(enclosing, block.isStatic());
                }
                
                if (elm == null) {
                    // XXX log unknown path
                    continue;
                }

                if (elmDesc.declaration != null && elm == elmDesc.declaration.resolveElement(javac)) {
                    if (treePath.getLeaf().getKind() == Kind.MEMBER_SELECT) {
                        ExpressionTree exp = ((MemberSelectTree) treePath.getLeaf()).getExpression();
                        if (exp.getKind() == Kind.IDENTIFIER && "super".contentEquals(((IdentifierTree)exp).getName())) { //NOI18N
                            continue;
                        }
                    }
                }

                OccurrencesDesc occurDesc = refs.get(elm);
                if (occurDesc == null) {
                    occurDesc = new OccurrencesDesc(declarationPath, elm, order++);
                    refs.put(elm, occurDesc);
                }
                occurDesc.occurrences.add(treePath);
            }
            
            List<Call> usageDescs = new ArrayList<Call>(refs.size());
            for (OccurrencesDesc occurDesc : OccurrencesDesc.extract(refs)) {
                if (isCanceled()) {
                    return;
                }
                Call newDesc = Call.createUsage(
                        javac, occurDesc.selection, occurDesc.elm, elmDesc,
                        occurDesc.occurrences);
                usageDescs.add(newDesc);
            }
            result.addAll(usageDescs);
        }
        
        private static TreePath resolveDeclarationContext(TreePath usage) {
            TreePath declaration = usage;
            
            while (declaration != null) {
                switch (declaration.getLeaf().getKind()) {
                    case BLOCK:
                        if (TreeUtilities.CLASS_TREE_KINDS.contains(declaration.getParentPath().getLeaf().getKind())) {
                            // it is static or instance initializer
                            return declaration;
                        }
                        break;
                    case METHOD:
                        return declaration;
                    case VARIABLE:
                        if (TreeUtilities.CLASS_TREE_KINDS.contains(declaration.getParentPath().getLeaf().getKind())) {
                            // it is field declaration
                            // private int field = init();
                            return declaration;
                        }
                        break;
                }
                declaration = declaration.getParentPath();
            }
            return null;
        }
    
        private Iterable<? extends List<FileObject>> groupByRoot (Iterable<? extends FileObject> data) {
            Map<FileObject,List<FileObject>> result = new HashMap<FileObject,List<FileObject>> ();
            for (FileObject file : data) {
                if (isCanceled()) {
                    return Collections.emptyList();
                }
                
                ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
                if (cp != null) {
                    FileObject root = cp.findOwnerRoot(file);
                    if (root != null) {
                        if (!includeTest && UnitTestForSourceQuery.findSources(root).length > 0) {
                            continue;
                        }
                        List<FileObject> subr = result.get (root);
                        if (subr == null) {
                            subr = new LinkedList<FileObject>();
                            result.put (root,subr);
                        }
                        subr.add(file);
                    }
                }
            }
            return result.values();
        }
        
        protected final void processFiles(Set<FileObject> files, Task<CompilationController> task, ClasspathInfo info) throws IOException {
            Iterable<? extends List<FileObject>> work = groupByRoot(files);
            for (List<FileObject> fos : work) {
                if (isCanceled()) {
                    return;
                }
                final JavaSource javaSource = JavaSource.create(info == null ? ClasspathInfo.create(fos.get(0)) : info, fos);
                javaSource.runUserActionTask(task, true);
            }
        }

    }
    
    private static final class CalleesTask extends CallTaskBase {
        public CalleesTask(Call element, Runnable resultHandler) {
            super(element, resultHandler);
        }
        
        @Override
        protected void runTask() throws Exception {
            TreePathHandle tph = elmDesc.getSourceToQuery();
            if (tph == null || tph.getFileObject() == null) {
                return;
            }
            JavaSource js = JavaSource.forFileObject(tph.getFileObject());
            if (js != null) {
                Future<Void> control = ScanUtils.postUserActionTask(js, this);
                control.get();
            }
        }

        @Override
        public void run(CompilationController javac) throws Exception {
            if (isCanceled()) {
                elmDesc.setCanceled(true);
                return;
            }
            javac.toPhase(JavaSource.Phase.RESOLVED);
            TreePath resolved = elmDesc.getSourceToQuery().resolve(javac);
            if (resolved == null) {
                // nothing to find
                // XXX descriptor should be invalidated and it should be presented to user
                return;
            }

            Element resolvedElm = javac.getTrees().getElement(resolved);
            resolved = javac.getTrees().getPath(resolvedElm);
            if (resolved == null) {
                // nothing to find, missing source file
                // XXX descriptor should be invalidated and it should be presented to user
                return;
            }

            CalleeScanner scanner = new CalleeScanner(javac);
            scanner.scan(resolved, null);
            elmDesc.setIncomplete(scanner.incomplete);
            for (OccurrencesDesc occurDesc : scanner.getOccurrences()) {
                if (isCanceled()) {
                    elmDesc.setCanceled(true);
                    return;
                }
                result.add(Call.createUsage(
                        javac, occurDesc.selection, occurDesc.elm, elmDesc, occurDesc.occurrences));
            }
        }
        
    }
    
    private static final class CalleeScanner extends ErrorAwareTreePathScanner<Void, Void> {
        private final CompilationInfo   javac;
        /** map of all executables and their occurrences in the method body */
        private Map<Element, OccurrencesDesc> refs = new HashMap<Element, OccurrencesDesc>();
        private int elmCounter = 0;
        private boolean incomplete;

        public CalleeScanner(CompilationInfo javac) {
            this.javac = javac;
        }
        
        public List<OccurrencesDesc> getOccurrences() {
            return OccurrencesDesc.extract(refs);
        }
        
        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
            resolvePath(getCurrentPath());
            return super.visitMethodInvocation(node, p);
        }

        @Override
        public Void visitNewClass(NewClassTree node, Void p) {
            resolvePath(getCurrentPath());
            return super.visitNewClass(node, p);
        }
        
        private void resolvePath(TreePath tpath) {
            if(JavaPluginUtils.isSyntheticPath(javac, tpath)) {
                return;
            }
            Element resolved = javac.getTrees().getElement(tpath);
            if (javac.getElementUtilities().isErroneous(resolved) &&
                SourceUtils.isScanInProgress()) {
                incomplete = true;
                return;
            } 
            if (RefactoringUtils.isExecutableElement(resolved)
                    && !javac.getElementUtilities().isSynthetic(resolved)) {
                addRef(resolved, tpath);
            }
        }
        
        private void addRef(Element ref, TreePath occurrence) {
            OccurrencesDesc desc = refs.get(ref);
            if (desc == null) {
                desc = new OccurrencesDesc(occurrence, ref, elmCounter++);
                refs.put(ref, desc);
            }
            desc.occurrences.add(occurrence);
        }
    }
    
    private static final class OccurrencesDesc implements Comparable<OccurrencesDesc> {
        final List<TreePath> occurrences;
        final Element elm;
        final TreePath selection;
        final int order;

        public OccurrencesDesc(TreePath selection, Element elm, int order) {
            this.occurrences = new ArrayList<TreePath>();
            this.order = order;
            this.elm = elm;
            this.selection = selection;
        }

        @Override
        public int compareTo(OccurrencesDesc o) {
            return order - o.order;
        }
        
        public static List<OccurrencesDesc> extract(Map<Element, OccurrencesDesc> refs) {
            int size = refs.size();
            List<OccurrencesDesc> l;
            if (size > 0) {
                l = new ArrayList<OccurrencesDesc>(size);
                l.addAll(refs.values());
                Collections.sort(l);
            } else {
                l = Collections.emptyList();
            }
            return l;
        }
    }
    
    private static final class InitializerElement implements Element {
        
        private static final Set<Modifier> STATICM = EnumSet.of(Modifier.STATIC);
        private final boolean isStatic;
        private final Element enclosing;

        public InitializerElement(Element enclosing, boolean isStatic) {
            this.isStatic = isStatic;
            this.enclosing = enclosing;
        }
        
        @Override
        public TypeMirror asType() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public ElementKind getKind() {
            return isStatic ? ElementKind.STATIC_INIT : ElementKind.INSTANCE_INIT;
        }

        @Override
        public List<? extends AnnotationMirror> getAnnotationMirrors() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Set<Modifier> getModifiers() {
            return isStatic ? STATICM : Collections.<Modifier>emptySet();
        }

        @Override
        public Name getSimpleName() {
            return null;
        }

        @Override
        public Element getEnclosingElement() {
            return enclosing;
        }

        @Override
        public List<? extends Element> getEnclosedElements() {
            return Collections.emptyList();
        }

        @Override
        public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }
        
    }

    private static class CleanTask implements CancellableTask {

        private final RequestProcessor.Task task;
        private AtomicBoolean isCancelled;

        public CleanTask(RequestProcessor.Task task) {
            this.task = task;
            isCancelled = new AtomicBoolean(false);
        }

        @Override
        public void cancel() {
            isCancelled.set(true);
        }

        @Override
        public void run(Object parameter) throws Exception {
            task.waitFinished();
            if(!isCancelled.get()) {
                synchronized(LOCK) {
                    CURR_TASK.clear();
                }
            }
        }
    }
}
