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
package org.netbeans.api.java.source;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 * Utility methods, which help JavaSource processing tasks to coordinate with the indexing/scanning process.
 * There are two variants of {@link JavaSource#runUserActionTask}, which support <b>abort and restart</b> of the user task,
 * if the task indicates incomplete data and the scanning is in progress. The restart is done in a hope that, after scanning
 * completes, the missing types or elements will appear.
 * <p>
 * The user task may use other provided wrapper methods to:
 * <ul>
 * <li>{@link #checkElement} to assert element's presence and validity
 * <li>{@link #signalIncompleteData} to request restart if running parallel to scan
 * </ul>
 * Note that even though the user task run when all the parsing/scanning is over, there's no guarantee that data obtained from
 * the parser structures will be complete and valid - the source code may be really broken, dependency could be missing etc.
 * <p>
 * An example pattern how to use this class is:
 * <pre>{@code
 * final TreePath tp = ... ; // obtain a tree path
 * ScanUtils.waitUserActionTask(new Task&lt;{@link CompilationController}>() {
 *      public void run(CompilationController ctrl) {
 * 
 *          // possibly abort and wait for all info to become available
 *          Element e = ScanUtils.checkElement(tp.resolve(ctrl));
 *          if (!ctrl.getElementUtilities().isErroneous(e)) {
 *              // report the error; element is not found or is otherwise unusable
 *          } else {
 *              // do the really useful work on Element e
 *          }
 *      }
 * };
 * }</pre>
 *
 * @author Svata Dedic
 * @since 0.95
 */
public final class ScanUtils {
    private ScanUtils() {}
    
    /**
     * Runs the user task through {@link JavaSource#runUserActionTask}, and returns Future completion handle for it. 
     * The executed Task may indicate that it does not have enough data when scan is in progress, through e.g. {@link #checkElement}.
     * If so, processing of the Task will abort, and will be restarted when the scan finishes.
     * <p>
     * For more details, see description of {@link #postUserTask(org.netbeans.modules.parsing.api.Source, org.netbeans.modules.parsing.api.UserTask) }.
     *
     * @param src JavaSource to process
     * @param uat action task that will be executed
     *
     * @return Future that allows to wait for task's completion.
     *
     * @see #postUserTask(org.netbeans.modules.parsing.api.Source, org.netbeans.modules.parsing.api.UserTask) 
     * @see JavaSource#runUserActionTask
     */
    public static Future<Void> postUserActionTask(@NonNull final JavaSource src, @NonNull final Task<CompilationController> uat) {
        Parameters.notNull("src", src);
        Parameters.notNull("uat", uat);

        AtomicReference<Throwable> status = new AtomicReference<>(null);
        Future<Void> f = postJavaTask(src, uat, status);
        
        Throwable t = status.get();
        if (t != null) {
            Exceptions.printStackTrace(t);
        }
        return f;
    }

    /**
     * Runs the user task, and returns Future completion handle for it. 
     * The executed Task may indicate that it does not have enough data when scan is in progress, through e.g. {@link #checkElement}.
     * If so, processing of the Task will abort, and will be restarted when the scan finishes.
     * <p>
     * The task <b>may run asynchronously</b>, pay attention to synchronization of task's output
     * data. The first attempt to run the task MAY execute synchronously. Do not call the method from
     * Swing EDT, if the task typically takes non-trivial time to complete (see {@link JavaSource#runUserActionTask}
     * for discussion).
     * <p>
     * As {@code postUserTask} may decide to defer the task execution, {@code postUserTask} cannot reliably report exceptions thrown
     * from the parsing infrastructure. The Task itself is responsible for handling exceptions and propagating them
     * to the caller, the {@code post} method will just log the exception.
     * 
     * @param src Source to process
     * @param task action task that will be executed
     * @return Future that allows to wait for task's completion.
     *
     * @see JavaSource#runUserActionTask
     */
    public static Future<Void> postUserTask(@NonNull final Source src, @NonNull final UserTask task) {
        Parameters.notNull("src", src);
        Parameters.notNull("task", task);

        AtomicReference<Throwable> status = new AtomicReference<>(null);
        Future<Void> f = postUserTask(src, task, status);
        
        Throwable t = status.get();
        if (t != null) {
            Exceptions.printStackTrace(t);
        }
        return f;
    }

   /**
     * Runs user action over source 'src' using {@link JavaSource#runUserActionTask} and waits for its completion.
     * The executed Task may indicate that it does not have enough data when scan is in progress, through e.g. {@link #checkElement}.
     * If so, processing of the Task will abort, and will be restarted when the scan finishes. The {@code waitUserActionTask} method
     * will wait until the rescheduled task completes.
     * <p>
     * Unlike {@link #postUserActionTask}, this method propagates exceptiosn from the Task to the caller, even if the Task
     * is postponed and waited for.
     * <p>
     * Calling this method from Swing ED thread is prohibited.
     *
     * @param src java source to process
     * @param uat task to execute
     * 
     * @throws IOException in the case of a failure in the user task, or scheduling failure (propagated from {@link JavaSource#runUserActionTask},
     * {@link JavaSource#runWhenScanFinished}
     *
     * @see JavaSource#runUserActionTask
     */
    public static void waitUserActionTask(@NonNull final JavaSource src, @NonNull final Task<CompilationController> uat) throws IOException {
        Parameters.notNull("src", src);
        Parameters.notNull("uat", uat);

        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Illegal to call within EDT");
        }
        AtomicReference<Throwable> status = new AtomicReference<>(null);
        Future<Void> f = postJavaTask(src, uat, status);
        if (f.isDone()) {
            return;
        }
        try {
            f.get();
        } catch (InterruptedException ex) {
            IOException ioex = new IOException("Interrupted", ex);
            throw ioex;
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof IOException) {
                throw (IOException)cause;
            }
            IOException ioex = new IOException("Failed", ex);
            throw ioex;
        }
        // propagate the 'retry' instruction as an exception - was not thrown in an appropriate context.
        Throwable t = status.get();
        if (t != null) {
            if (t instanceof IOException) {
                throw (IOException) t;
            }
            IOException ioex = new IOException("Exception during processing", t);
            throw ioex;
        }
    }

        /**
     * Runs user action over source 'src' using {@link ParserManager#parse} and waits for its completion.
     * The executed Task may indicate that it does not have enough data when scan is in progress, through e.g. {@link #checkElement}.
     * If so, processing of the Task will abort, and will be restarted when the scan finishes. The {@code waitUserTask} method
     * will wait until the rescheduled task completes.
     * <p>
     * Unlike {@link #postUserTask}, this method propagates exceptiosn from the Task to the caller, even if the Task
     * is postponed and waited for.
     * <p>
     * Calling this method from Swing ED thread is prohibited.
     *
     * @param src java source to process
     * @param task task to execute
     * 
     * @throws ParseException in the case of a failure in the user task, or scheduling failure (propagated from {@link JavaSource#runUserActionTask},
     * {@link ParserManager#parseWhenScanFinished(java.util.Collection, org.netbeans.modules.parsing.api.UserTask) }
     *
     * @see ParserManager#parse(java.util.Collection, org.netbeans.modules.parsing.api.UserTask) 
     * @see ParserManager#parseWhenScanFinished(java.util.Collection, org.netbeans.modules.parsing.api.UserTask) 
     */
    public static void waitUserTask(@NonNull final Source src, @NonNull final UserTask task) throws ParseException {
        Parameters.notNull("src", src);
        Parameters.notNull("task", task);
        
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Illegal to call within EDT");
        }
        AtomicReference<Throwable> status = new AtomicReference<>(null);
        Future<Void> f = postUserTask(src, task, status);
        if (f.isDone()) {
            Throwable t = status.get();
            if (t != null) {
                if (t instanceof ParseException) {
                    throw (ParseException)t;
                } else {
                    throw new ParseException("User task failure", t);
                }
            }
            return;
        }
        try {
            f.get();
        } catch (InterruptedException ex) {
            ParseException err  = new ParseException("Interrupted", ex);
            throw err;
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ParseException) {
                throw (ParseException)cause;
            }
            ParseException ioex = new ParseException("User task failure", ex);
            throw ioex;
        }
        // propagate the 'retry' instruction as an exception - was not thrown in an appropriate context.
        Throwable t = status.get();
        if (t != null) {
            if (t instanceof ParseException) {
                throw (ParseException) t;
            }
            ParseException err = new ParseException("User task failure", t);
            throw err;
        }
    }

    /**
     * Checks that the Element is valid, is not erroneous. If the Element is {@code null} or erroneous and
     * scanning is running, the method throws a {@link ScanUtils.RetryWhenScanFinished} exception to
     * indicate that the action should be retried. The method should be only used in code, which
     * is directly or indirectly executed by {@link #waitUserActionTask}, otherwise {@link IllegalStateException}
     * will be thrown.
     * <p>
     * If scan is not running, the method always returns the value of 'e' parameter.
     * <p>
     * An example usage is as follows:
     * <code>
     * TreePath tp = ...;
     * Element e = checkElement(cu.getTrees().getElement(tp));
     * </code>
     * <p>
     * <b>Note:</b> the method may be only called from within {@link #waitUserActionTask}, it aborts
     * the current processing under assumption the user task will be restarted. It is illegal to call the
     * method if not running as user task - IllegalStateException will be thrown.
     *
     * @param e the Element to check
     * @param info the source of the Element
     * @return the original Element, to support 'fluent' pattern
     *
     * @throws IllegalStateException if not called from within user task
     *
     */
    public static <T extends Element> T checkElement(@NonNull CompilationInfo info, @NullAllowed T e) {
        checkRetryContext();
        if (e == null) {
            signalIncompleteData(info, null);
            return e;
        }
        if (!info.getElementUtilities().isErroneous(e)) {
            return e;
        }
        if (shouldSignal()) {
            signalIncompleteData(info, ElementHandle.create(e));
        }
        return e;
    }

    private static boolean shouldSignal() {
        return SourceUtils.isScanInProgress() && Boolean.TRUE.equals(retryGuard.get());
    }
    
    /**
     * Aborts the user task and calls it again when parsing finishes, if a typename is not available.
     *
     * @param ci the Element which causes the trouble
     * @param handle of the Element
     * @throws IllegalStateException if not called from within {@link #waitUserActionTask} and the like
     *
     */
    public static void signalIncompleteData(@NonNull CompilationInfo ci, @NullAllowed ElementHandle handle) {
        checkRetryContext();
        if (shouldSignal()) {
            throw new RetryWhenScanFinished(ci, handle);
        }
    }

    private static void checkRetryContext() throws IllegalStateException {
        Boolean b = retryGuard.get();
        if (b == null) {
            throw new IllegalStateException("The method may be only called within SourceUtils.waitUserActionTask");
        }
    }
    
    private static Future<Void> postJavaTask(
            final JavaSource javaSrc, 
            final Task<CompilationController> javaTask, 
            final AtomicReference<Throwable> status) {
        boolean retry = false;
        Boolean b = retryGuard.get();
        boolean mode = b == null || b.booleanValue();
        try {
            retryGuard.set(mode);
            javaSrc.runUserActionTask(javaTask, true);
            // the action passed ;)
        } catch (RuntimeException e) {
            status.set(e);
        } catch (IOException e) {
            // log and swallow
            status.set(e);
        } catch (RetryWhenScanFinished e) {
            // expected, will retry in runWhenParseFinished
            retry = true;
        } finally {
            if (b == null) {
                retryGuard.remove();
            } else {
                retryGuard.set(b);
            }
        }
        if (!retry) {
            return new FinishedFuture();
        }
        
        final TaskWrapper wrapper;
        Future<Void> handle;
        
        wrapper = new TaskWrapper(javaTask, status, mode);
        try {
            handle = javaSrc.runWhenScanFinished(wrapper, true);
        } catch (IOException ex) {
            status.set(ex);
            handle = new FinishedFuture();
        }
        return handle;
    }

    private static Future<Void> postUserTask(
            final Source src,
            final UserTask  task,
            final AtomicReference<Throwable> status) {
        boolean retry = false;
        Boolean b = retryGuard.get();
        boolean mode = b == null || b.booleanValue();
        try {
            retryGuard.set(mode);
            ParserManager.parse(Collections.singleton(src), task);
        } catch (ParseException ex) {
            status.set(ex);
        } catch (RetryWhenScanFinished e) {
            // expected, will retry in runWhenParseFinished
            retry = true;
        } finally {
            if (b == null) {
                retryGuard.remove();
            } else {
                retryGuard.set(b);
            }
        }
        if (!retry) {
            return new FinishedFuture();
        }
        
        final TaskWrapper wrapper;
        Future<Void> handle;
        
        wrapper = new TaskWrapper(task, status, mode);
        try {
            handle = ParserManager.parseWhenScanFinished(Collections.singletonList(src), wrapper);
        } catch (ParseException ex) {
            status.set(ex);
            handle = new FinishedFuture();
        }
        return handle;
    }

    /**
     * Guards usage of the abortAndRetry methods; since they throw an exception, which is 
     * only caught on specific places
     */
    private static final ThreadLocal<Boolean> retryGuard = new ThreadLocal<Boolean>();
    
    private static final class FinishedFuture implements Future<Void> {
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public Void get() throws InterruptedException, ExecutionException {
            return null;
        }

        @Override
        public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public String toString() {
            return "FinishedFuture";
        }
        
        
    }
    
    private static class TaskWrapper extends UserTask implements Task<CompilationController> {
        private Task<CompilationController> javaTask;
        private UserTask  task;
        private AtomicReference<Throwable>  status;
        private boolean mode;
        
        public TaskWrapper(UserTask task, AtomicReference<Throwable> status, boolean mode) {
            this.task = task;
            this.status = status;
            this.mode = mode;
        }

        public TaskWrapper(Task<CompilationController> javaTask, AtomicReference<Throwable> status, boolean mode) {
            this.javaTask = javaTask;
            this.status = status;
            this.mode = mode;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            Boolean b = retryGuard.get();
            try {
                retryGuard.set(mode);
                task.run(resultIterator);
            } catch (RetryWhenScanFinished ex) {
                // swallow Error, but signal error
                status.set(ex);
            } catch (Exception ex) {
                status.set(ex);
                throw ex;
            } finally {
                if (b == null) {
                    retryGuard.remove();
                } else {
                    retryGuard.set(b);
                }
            }
        }

        @Override
        public void run(CompilationController parameter) throws Exception {
            Boolean b = retryGuard.get();
            try {
                retryGuard.set(mode);
                javaTask.run(parameter);
            } catch (RetryWhenScanFinished ex) {
                // swallow Error, but signal error
                status.set(ex);
            } catch (Exception ex) {
                status.set(ex);
                throw ex;
            } finally {
                if (b == null) {
                    retryGuard.remove();
                } else {
                    retryGuard.set(b);
                }
            }
        }
    }

    /**
     * An Exception which indicates that the operation should be aborted, and
     * retried when parsing is finished. The exception derives from the {@link Error}
     * class to bypass ill-written code, which caught {@link RuntimeException} or
     * even {@link Exception}.
     * <p>
     * The exception is interpreted by the Java Source infrastructure and is not
     * meant to be ever thrown or caught by regular application code. It's deliberately
     * package-private so users are not tempted to throw or catch it.
     */
    static class RetryWhenScanFinished extends Error {
        private ElementHandle   elHandle;
        private Source          source;

        public RetryWhenScanFinished(CompilationInfo ci, ElementHandle elHandle) {
            if (ci != null && ci.getSnapshot() != null) {
               source = ci.getSnapshot().getSource();
            }
            this.elHandle = elHandle;
        }

        @Override
        public String toString() {
            return "RetryWhenScanFinished{" + "elHandle=" + elHandle + ", source=" + source + '}';
        }

    }
}
