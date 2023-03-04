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

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndexListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.RootsEvent;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TypesEvent;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.openide.util.Exceptions;
import org.openide.util.WeakSet;

/**
 *
 * @author Andrei Badea
 */
public final class AnnotationModelHelper {

    // XXX exception wrapping in runJavaSourceTask()
    // XXX ExecutionException for the future returned by runJavaSourceTaskWhenScanFinished()

    private final ClasspathInfo cpi;
    // @GuardedBy("this")
    private final Set<JavaContextListener> javaContextListeners = new WeakSet<JavaContextListener>();
    // @GuardedBy("this")
    private final Set<PersistentObjectManager<? extends PersistentObject>> managers = new WeakSet<PersistentObjectManager<? extends PersistentObject>>();

    // @GuardedBy("this")
    private ClassIndex classIndex;
    // @GuardedBy("this")
    private ClassIndexListenerImpl listener;

    // not private because used in unit tests
    // @GuardedBy("this")
    JavaSource javaSource;
    // @GuardedBy("this")
    private Thread userActionTaskThread;

    private AnnotationScanner annotationScanner;
    private CompilationController controller;
    private AnnotationHelper helper;

    public static AnnotationModelHelper create(ClasspathInfo cpi) {
        return new AnnotationModelHelper(cpi);
    }

    private AnnotationModelHelper(ClasspathInfo cpi) {
        this.cpi = cpi;
        this.helper = new AnnotationHelper( this );
    }

    public ClasspathInfo getClasspathInfo() {
        return cpi;
    }

    public <T extends PersistentObject> PersistentObjectManager<T> createPersistentObjectManager(ObjectProvider<T> provider) {
        synchronized (this) {
            PersistentObjectManager<T> manager = PersistentObjectManager.create(this, provider);
            registerPersistentObjectManager(manager);
            return manager;
        }
    }

    private void registerPersistentObjectManager(PersistentObjectManager<? extends PersistentObject> manager) {
        assert Thread.holdsLock(this);
        if (classIndex == null) {
            classIndex = cpi.getClassIndex();
            // this doesn't get removed anywhere, which should not matter, since
            // the classpath info, its class index and all managers have the same lifecycle
            listener = new ClassIndexListenerImpl();
            classIndex.addClassIndexListener(listener);
        }
        managers.add(manager);
    }

    public void addJavaContextListener(JavaContextListener listener) {
        synchronized (this) {
            javaContextListeners.add(listener);
        }
    }

    /**
     * Runs the given callable as a JavaSource user action task.
     * The context of the JavaSource task can be accessed by {@link #getCompilationController}.
     */
    public <V> V runJavaSourceTask(Callable<V> callable) throws IOException {
        return runJavaSourceTask(callable, true);
    }

    /**
     * Runs the given runnable as a JavaSource user action task.
     *
     * @see #runJavaSourceTask(Callable)
     */
    public void runJavaSourceTask(final Runnable run) throws IOException {
        runJavaSourceTask(new Callable<Void>() {
            @Override
            public Void call() {
                run.run();
                return null;
            }
        });
    }

    /**
     * Runs the given callable as a JavaSource user action task. Not private because
     * used in unit tests.
     *
     * @param notify whether to notify <code>JavaContextListener</code>s.
     */
    <V> V runJavaSourceTask(final Callable<V> callable, final boolean notify) throws IOException {
        JavaSource existingJavaSource;
        synchronized (this) {
            existingJavaSource = javaSource;
        }
        JavaSource newJavaSource = existingJavaSource != null ? existingJavaSource : JavaSource.create(cpi);
        final List<V> result = new ArrayList<V>();
        try {
            newJavaSource.runUserActionTask(new CancellableTask<CompilationController>() {
                @Override
                public void run(CompilationController controller) throws Exception {
                    result.add(runCallable(callable, controller, notify));
                }
                @Override
                public void cancel() {
                    // we can't cancel
                }
            }, true);
        } catch (IOException e) {
            Throwable cause = e.getCause();
            if (cause instanceof MetadataModelException) {
                throw (MetadataModelException)cause;
            }
            throw e;
        }
        return result.get(0);
    }

    /**
     * Runs the given callable as a JavaSource user action task either immediately,
     * or, if the Java infrastructure is just performing a classpath scan,
     * when the scan has finished. This method is the equivalent of
     * {@link JavaSource#runWhenScanFinished}.
     */
    public <V> Future<V> runJavaSourceTaskWhenScanFinished(final Callable<V> callable) throws IOException {
        JavaSource existingJavaSource;
        synchronized (this) {
            existingJavaSource = javaSource;
        }
        JavaSource newJavaSource = existingJavaSource != null ? existingJavaSource : JavaSource.create(cpi);
        final DelegatingFuture<V> result = new DelegatingFuture<V>();
        try {
            result.setDelegate(newJavaSource.runWhenScanFinished(new CancellableTask<CompilationController>() {
                @Override
                public void run(CompilationController controller) throws Exception {
                    result.setResult(runCallable(callable, controller, true));
                }
                @Override
                public void cancel() {
                    // we can't cancel
                }
            }, true));
        } catch (IOException e) {
            Throwable cause = e.getCause();
            if (cause instanceof MetadataModelException) {
                throw (MetadataModelException)cause;
            }
            throw e;
        }
        assert result.delegate != null;
        return result;
    }

    /**
     * Runs the given callable in a javac context. Reentrant only in a single thread
     * (should be guaranteed by JavaSource.javacLock).
     */
    private <V> V runCallable(Callable<V> callable, CompilationController controller, boolean notify) throws IOException {
        JavaSource oldJavaSource;
        Thread oldUserActionTaskThread;
        synchronized (AnnotationModelHelper.this) {
            if (userActionTaskThread != null && userActionTaskThread != Thread.currentThread()) {
                throw new IllegalStateException("JavaSource.runUserActionTask() should not be executed by multiple threads concurrently"); // NOI18N
            }
            oldUserActionTaskThread = userActionTaskThread;
            userActionTaskThread = Thread.currentThread();
            oldJavaSource = javaSource;
            javaSource = controller.getJavaSource();
        }
        CompilationController oldController = AnnotationModelHelper.this.controller;
        AnnotationModelHelper.this.controller = controller;
        try {
            controller.toPhase(Phase.ELEMENTS_RESOLVED);
            return callable.call();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else {
                throw new MetadataModelException(e);
            }
        } finally {
            AnnotationModelHelper.this.controller = oldController;
            annotationScanner = null;
            synchronized (AnnotationModelHelper.this) {
                javaSource = oldJavaSource;
                userActionTaskThread = oldUserActionTaskThread;
            }
            if (notify) {
                // have to notify while still under the javac lock
                // to ensure the visibility of any changes made by the listeners
                for (JavaContextListener hook : javaContextListeners) {
                    hook.javaContextLeft();
                }
            }
        }
    }

    /**
     * Returns the {@link CompilationController} of a running JavaSource
     * user action task. This method can only be called when such an user action
     * task in running.
     *
     * @see #runJavaSourceTask(Callable)
     */
    public CompilationController getCompilationController() {
        assertUserActionTaskThread();
        assert controller != null;
        return controller;
    }

    public AnnotationScanner getAnnotationScanner() {
        assertUserActionTaskThread();
        if (annotationScanner == null) {
            annotationScanner = new AnnotationScanner( getHelper());
        }
        return annotationScanner;
    }
    
    public AnnotationHelper getHelper(){
        return helper;
    }

    /**
     * Returns true if the Java infrastructure is just performing a classpath
     * scan.
     */
    public boolean isJavaScanInProgress() {
        return SourceUtils.isScanInProgress();
    }

    private void assertUserActionTaskThread() {
        synchronized (this) {
            if (userActionTaskThread != Thread.currentThread()) {
                throw new IllegalStateException("The current thread is not running userActionTask()"); // NOI18N
            }
        }
    }
    
    /**
     * @param typeName must be the name of a type element
     * (resolvable by {@link javax.lang.model.util.Elements#getTypeElement}).
     */
    public TypeMirror resolveType(String typeName) {
        assertUserActionTaskThread();
        return getHelper().resolveType(typeName);
    }

    public boolean isSameRawType(TypeMirror type1, String type2ElementName) {
        assertUserActionTaskThread();
        return getHelper().isSameRawType(type1, type2ElementName);
    }

    public List<? extends TypeElement> getSuperclasses(TypeElement type) {
        assertUserActionTaskThread();
        return getHelper().getSuperclasses(type);
    }

    public TypeElement getSuperclass(TypeElement type) {
        assertUserActionTaskThread();
        return getHelper().getSuperclass(type);
    }

    public boolean hasAnnotation(List<? extends AnnotationMirror> annotations, 
            String annotationTypeName) 
    {
        assertUserActionTaskThread();
        return getHelper().hasAnnotation(annotations, annotationTypeName);
    }

    public boolean hasAnyAnnotation(List<? extends AnnotationMirror> annotations, Set<String> annotationTypeNames) {
        assertUserActionTaskThread();
        return getHelper().hasAnyAnnotation(annotations, annotationTypeNames);
    }

    public Map<String, ? extends AnnotationMirror> getAnnotationsByType(
            List<? extends AnnotationMirror> annotations) 
    {
        assertUserActionTaskThread();
        return getHelper().getAnnotationsByType(annotations);
    }

    /**
     * @return the annotation type name or null if <code>typeMirror</code>
     *         was not an annotation type.
     */
    public String getAnnotationTypeName(DeclaredType typeMirror) {
        assertUserActionTaskThread();
        return getHelper().getAnnotationTypeName(typeMirror);
    }

    private final class ClassIndexListenerImpl implements ClassIndexListener {

        @Override
        public void typesAdded(final TypesEvent event) {
            try {
                runInJavacContext(new Callable<Void>() {
                    @Override
                    public Void call() {
                        for (PersistentObjectManager<? extends PersistentObject> manager : managers) {
                            manager.typesAdded(event.getTypes());
                        }
                        return null;
                    }
                });
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }

        @Override
        public void typesRemoved(final TypesEvent event) {
            try {
                runInJavacContext(new Callable<Void>() {
                    @Override
                    public Void call() {
                        for (PersistentObjectManager<? extends PersistentObject> manager : managers) {
                            manager.typesRemoved(event.getTypes());
                        }
                        return null;
                    }
                });
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }

        @Override
        public void typesChanged(final TypesEvent event) {
            try {
                runInJavacContext(new Callable<Void>() {
                    @Override
                    public Void call() {
                        for (PersistentObjectManager<? extends PersistentObject> manager : managers) {
                            manager.typesChanged(event.getTypes());
                        }
                        return null;
                    }
                });
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }

        @Override
        public void rootsAdded(RootsEvent event) {
            rootsChanged();
        }

        @Override
        public void rootsRemoved(RootsEvent event) {
            rootsChanged();
        }

        private void rootsChanged() {
            try {
                runInJavacContext(new Callable<Void>() {
                    @Override
                    public Void call() {
                        for (PersistentObjectManager<? extends PersistentObject> manager : managers) {
                            manager.rootsChanged();
                        }
                        return null;
                    }
                });
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }

        private <V> void runInJavacContext(final Callable<V> call) throws IOException {
            synchronized (AnnotationModelHelper.this) {
                if (userActionTaskThread == Thread.currentThread()) {
                    throw new IllegalStateException("Retouche is sending ClassIndex events from within JavaSource.runUserActionTask()"); // NOI18N
                }
            }
            runJavaSourceTask(call, false);
        }
    }

    private static final class DelegatingFuture<V> implements Future<V> {

        private volatile Future<Void> delegate;
        private volatile V result;

        public void setDelegate(Future<Void> delegate) {
            assert this.delegate == null;
            this.delegate = delegate;
        }

        public void setResult(V result) {
            this.result = result;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return delegate.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return delegate.isCancelled();
        }

        @Override
        public boolean isDone() {
            return delegate.isDone();
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            delegate.get();
            return result;
        }

        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            delegate.get(timeout, unit);
            return result;
        }
    }
}
