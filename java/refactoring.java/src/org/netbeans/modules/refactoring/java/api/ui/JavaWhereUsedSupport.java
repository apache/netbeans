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
package org.netbeans.modules.refactoring.java.api.ui;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.java.api.WhereUsedQueryConstants;
import org.netbeans.modules.refactoring.java.ui.PreparedWhereUsedQueryUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 * Support for prepared Java where-used queries that should open results directly.
 *
 * @since 1.95
 */
public final class JavaWhereUsedSupport {

    private static final RequestProcessor WORKER = new RequestProcessor(JavaWhereUsedSupport.class);

    private static final Set<ElementKind> SUPPORTED_TYPE_KINDS = EnumSet.of(
            ElementKind.CLASS,
            ElementKind.INTERFACE,
            ElementKind.ENUM,
            ElementKind.RECORD,
            ElementKind.ANNOTATION_TYPE);

    private JavaWhereUsedSupport() {
    }

    /**
     * Computes the direct reference count for a Java method or named type.
     *
     * @param handle the Java declaration handle
     * @param scope the search scope, or {@code null} to use the query default
     * @param cancel cancellation flag checked between query steps
     * @return the number of matching references
     * @throws IOException if the declaration cannot be resolved or the query cannot be prepared
     */
    public static int getDirectReferenceCount(TreePathHandle handle, Scope scope, AtomicBoolean cancel) throws IOException {
        PreparedQuery prepared = prepare(handle, scope, cancel);
        if (prepared != null) {
            return prepared.session.getRefactoringElements().size();
        }
        if (cancel != null && cancel.get()) {
            throw new InterruptedIOException("Cancelled");
        }
        throw new IOException("Could not prepare direct-reference query for " + handle); // NOI18N
    }

    /**
     * Computes the direct reference count for a Java method or named type using
     * the editor's current classpath info.
     *
     * @param handle the Java declaration handle
     * @param cpInfo the classpath info used to resolve the handle
     * @param scope the search scope, or {@code null} to use the query default
     * @param cancel cancellation flag checked between query steps
     * @return the number of matching references
     * @throws IOException if the declaration cannot be resolved or the query cannot be prepared
     */
    public static int getDirectReferenceCount(ElementHandle<? extends Element> handle, ClasspathInfo cpInfo, Scope scope, AtomicBoolean cancel) throws IOException {
        PreparedQuery prepared = prepare(handle, cpInfo, scope, cancel);
        if (prepared != null) {
            return prepared.session.getRefactoringElements().size();
        }
        if (cancel != null && cancel.get()) {
            throw new InterruptedIOException("Cancelled");
        }
        throw new IOException("Could not prepare direct-reference query for " + handle); // NOI18N
    }

    /**
     * Opens the direct reference results for a Java method or named type without
     * showing the where-used parameter dialog.
     *
     * @param handle the Java declaration handle
     * @param scope the search scope, or {@code null} to use the query default
     */
    public static void openDirectReferenceResults(TreePathHandle handle, Scope scope) {
        Request.open(handle, scope);
    }

    /**
     * Opens the direct reference results for a Java method or named type without
     * showing the where-used parameter dialog, using the editor's current
     * classpath info.
     *
     * @param handle the Java declaration handle
     * @param cpInfo the classpath info used to resolve the handle
     * @param scope the search scope, or {@code null} to use the query default
     */
    public static void openDirectReferenceResults(ElementHandle<? extends Element> handle, ClasspathInfo cpInfo, Scope scope) {
        Request.open(handle, cpInfo, scope);
    }

    private static PreparedQuery prepare(TreePathHandle handle, Scope scope, AtomicBoolean cancel) throws IOException {
        ResolvedElement resolved = resolve(handle);
        if (resolved == null) {
            return null;
        }
        WhereUsedQuery query = createQuery(handle, resolved.kind, scope);
        RefactoringSession session = RefactoringSession.create(getName(resolved.displayName));
        Problem problem = query.preCheck();
        if (problem != null && problem.isFatal()) {
            return null;
        }
        problem = query.fastCheckParameters();
        if (problem != null && problem.isFatal()) {
            return null;
        }
        problem = query.checkParameters();
        if (problem != null && problem.isFatal()) {
            return null;
        }
        if (cancel != null && cancel.get()) {
            return null;
        }
        problem = query.prepare(session);
        if (problem != null && problem.isFatal()) {
            return null;
        }
        if (cancel != null && cancel.get()) {
            return null;
        }
        return new PreparedQuery(query, session, resolved.displayName);
    }

    private static PreparedQuery prepare(ElementHandle<? extends Element> handle, ClasspathInfo cpInfo, Scope scope, AtomicBoolean cancel) throws IOException {
        FileObject file = SourceUtils.getFile(handle, cpInfo);
        if (file == null) {
            return null;
        }
        ResolvedElement resolved = resolve(handle, cpInfo, file);
        if (resolved == null) {
            return null;
        }
        TreePathHandle treePathHandle = TreePathHandle.from(handle, cpInfo);
        WhereUsedQuery query = createQuery(treePathHandle, resolved.kind, scope);
        RefactoringSession session = RefactoringSession.create(getName(resolved.displayName));
        Problem problem = query.preCheck();
        if (problem != null && problem.isFatal()) {
            return null;
        }
        problem = query.fastCheckParameters();
        if (problem != null && problem.isFatal()) {
            return null;
        }
        problem = query.checkParameters();
        if (problem != null && problem.isFatal()) {
            return null;
        }
        if (cancel != null && cancel.get()) {
            return null;
        }
        problem = query.prepare(session);
        if (problem != null && problem.isFatal()) {
            return null;
        }
        if (cancel != null && cancel.get()) {
            return null;
        }
        return new PreparedQuery(query, session, resolved.displayName);
    }

    private static WhereUsedQuery createQuery(TreePathHandle handle, ElementKind kind, Scope scope) {
        WhereUsedQuery query = new WhereUsedQuery(Lookups.singleton(handle));
        query.putValue(WhereUsedQuery.SEARCH_IN_COMMENTS, false);
        if (scope != null) {
            query.getContext().add(scope);
        }
        if (kind == ElementKind.METHOD) {
            query.getContext().add(handle);
            query.putValue(WhereUsedQueryConstants.SEARCH_FROM_BASECLASS, false);
            query.putValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS, false);
            query.putValue(WhereUsedQueryConstants.SEARCH_OVERLOADED, false);
            query.putValue(WhereUsedQueryConstants.FIND_DIRECT_REFERENCES, true);
            query.putValue(WhereUsedQuery.FIND_REFERENCES, true);
        } else if (SUPPORTED_TYPE_KINDS.contains(kind)) {
            query.putValue(WhereUsedQueryConstants.FIND_SUBCLASSES, false);
            query.putValue(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES, false);
            query.putValue(WhereUsedQuery.FIND_REFERENCES, true);
        } else {
            throw new IllegalArgumentException("Unsupported where-used element kind: " + kind); // NOI18N
        }
        return query;
    }

    private static ResolvedElement resolve(TreePathHandle handle) throws IOException {
        JavaSource source = JavaSource.forFileObject(handle.getFileObject());
        if (source == null) {
            return null;
        }
        final ResolvedElement[] resolved = new ResolvedElement[1];
        source.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                Element element = handle.resolveElement(controller);
                if (element == null) {
                    return;
                }
                ElementKind kind = element.getKind();
                if (kind == ElementKind.CONSTRUCTOR || !(kind == ElementKind.METHOD || SUPPORTED_TYPE_KINDS.contains(kind))) {
                    return;
                }
                String displayName;
                if (kind == ElementKind.METHOD) {
                    displayName = element.getEnclosingElement().getSimpleName() + "." + element.getSimpleName(); // NOI18N
                } else {
                    displayName = element.getSimpleName().toString();
                }
                resolved[0] = new ResolvedElement(kind, displayName);
            }
        }, true);
        return resolved[0];
    }

    private static ResolvedElement resolve(ElementHandle<? extends Element> handle, ClasspathInfo cpInfo, FileObject file) throws IOException {
        JavaSource source = JavaSource.create(cpInfo, file);
        if (source == null) {
            source = JavaSource.forFileObject(file);
        }
        if (source == null) {
            return null;
        }
        final ResolvedElement[] resolved = new ResolvedElement[1];
        source.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                Element element = handle.resolve(controller);
                if (element == null) {
                    return;
                }
                ElementKind kind = element.getKind();
                if (kind == ElementKind.CONSTRUCTOR || !(kind == ElementKind.METHOD || SUPPORTED_TYPE_KINDS.contains(kind))) {
                    return;
                }
                String displayName;
                if (kind == ElementKind.METHOD) {
                    displayName = element.getEnclosingElement().getSimpleName() + "." + element.getSimpleName(); // NOI18N
                } else {
                    displayName = element.getSimpleName().toString();
                }
                resolved[0] = new ResolvedElement(kind, displayName);
            }
        }, true);
        return resolved[0];
    }

    private static String getName(String displayName) {
        return new MessageFormat(NbBundle.getMessage(JavaWhereUsedSupport.class, "LBL_UsagesOf")).format(new Object[]{displayName});
    }

    private record PreparedQuery(WhereUsedQuery query, RefactoringSession session, String displayName) {
    }

    private record ResolvedElement(ElementKind kind, String displayName) {
    }

    private static final class Request implements Runnable {

        private final TreePathHandle handle;
        private final ElementHandle<? extends Element> elementHandle;
        private final ClasspathInfo cpInfo;
        private final Scope scope;

        private Request(TreePathHandle handle, Scope scope) {
            this.handle = handle;
            this.elementHandle = null;
            this.cpInfo = null;
            this.scope = scope;
        }

        private Request(ElementHandle<? extends Element> handle, ClasspathInfo cpInfo, Scope scope) {
            this.handle = null;
            this.elementHandle = handle;
            this.cpInfo = cpInfo;
            this.scope = scope;
        }

        static void open(TreePathHandle handle, Scope scope) {
            WORKER.post(new Request(handle, scope));
        }

        static void open(ElementHandle<? extends Element> handle, ClasspathInfo cpInfo, Scope scope) {
            WORKER.post(new Request(handle, cpInfo, scope));
        }

        @Override
        public void run() {
            try {
                PreparedQuery prepared = handle != null
                        ? prepare(handle, scope, new AtomicBoolean())
                        : prepare(elementHandle, cpInfo, scope, new AtomicBoolean());
                if (prepared == null) {
                    return;
                }
                SwingUtilities.invokeLater(() -> UI.openRefactoringUI(
                        new PreparedWhereUsedQueryUI(prepared.query, prepared.displayName),
                        prepared.session,
                        null));
            } catch (IOException ex) {
                // keep the editor interaction lightweight; unresolved handles are silently ignored
            }
        }
    }
}
