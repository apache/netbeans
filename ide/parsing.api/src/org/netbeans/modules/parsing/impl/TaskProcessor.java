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

package org.netbeans.modules.parsing.impl;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.lucene.spi.ScanSuspendImplementation;
import org.netbeans.modules.parsing.spi.*;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Zezula
 */
public class TaskProcessor {

    private static final String COMPAT_MODE = "org.netbeans.modules.parsing.impl.TaskProcessor.compatMode"; //NOI18N
    private static final Logger LOGGER = Logger.getLogger(TaskProcessor.class.getName());

    /**Limit for task to be marked as a slow one, in ms*/
    private static final int SLOW_CANCEL_LIMIT = 50;

    //Scheduled requests waiting for execution
    private static final PriorityBlockingQueue<Request> requests = new PriorityBlockingQueue<> (10, new RequestPriorityComparator());
    //Finished requests waiting on reschedule by some scheduler or parser
    private static final Map<Source,Collection<Request>> finishedRequests = new WeakHashMap<>();
    //Tasks which are scheduled (not yet executed) but blocked by expected event (waiting for event)
    private static final Map<Source,Collection<Request>> waitingRequests = new WeakHashMap<>();
    //Tasked which should be cleared from requests or finieshedRequests
    private static final Collection<RemovedTask> toRemove = new LinkedList<> ();

    //Worker thread factory - single worker thread
    static final  RequestProcessor WORKER = new RequestProcessor(
            String.format("Editor Parsing Loop (%s)",   //NOI18N
                System.getProperty("netbeans.buildnumber")),    //NOI18N
            1,
            false,
            false);
    //Currently running SchedulerTask
    private static final CurrentRequestReference currentRequest = new CurrentRequestReference ();

    //Internal lock used to synchronize parsing api iternal state (TaskProcessor, Source, SourceCache)
    private static class InternalLock {};
    public static final Object INTERNAL_LOCK = new InternalLock ();


    //Parser lock used to prevent other tasks to run in case when there is an active task
    private static final ReentrantLock parserLock = new ReentrantLock (true);
    private static int lockCount = 0;

    //Regexp of class names of tasks which shouldn't be scheduled - used for debugging & performance testing
    private static final Pattern excludedTasks;
    //Regexp of class names of tasks which should be scheduled - used for debugging & performance testing
    private static final Pattern includedTasks;
    //Already logged warninig about running in AWT
    private static final Set<StackTraceElement> warnedAboutRunInEQ = new HashSet<>();

    /*test*/ static volatile boolean SAMPLING_ENABLED = false;     //Diabled until statistics server will be extended by slowcancel
    private static final int DEFAULT_START_SAMPLING_AFTER = 500;
    private static final Sampler sampler = new Sampler();
    private static final RequestProcessor SAMPLING_RP = new RequestProcessor(TaskProcessor.class.getName()+"-sampling");    //NOI18N
    private static final RequestProcessor.Task SAMPLING_TASK = SAMPLING_RP.create(sampler);

    static {
        WORKER.submit (new CompilationJob());
        //Initialize the excludedTasks
        Pattern _excludedTasks = null;
        try {
            String excludedValue= System.getProperty("org.netbeans.modules.parsing.impl.Source.excludedTasks");      //NOI18N
            if (excludedValue != null) {
                _excludedTasks = Pattern.compile(excludedValue);
            }
        } catch (PatternSyntaxException e) {
            Exceptions.printStackTrace(e);
        }
        excludedTasks = _excludedTasks;
        Pattern _includedTasks = null;
        try {
            String includedValue= System.getProperty("org.netbeans.modules.parsing.impl.Source.includedTasks");      //NOI18N
            if (includedValue != null) {
                _includedTasks = Pattern.compile(includedValue);
            }
        } catch (PatternSyntaxException e) {
            Exceptions.printStackTrace(e);
        }
        includedTasks = _includedTasks;
    }

    public static void runUserTask (final Mutex.ExceptionAction<Void> task, final Collection<Source> sources) throws ParseException {
        Parameters.notNull("task", task);
        //tzezula: ugly, Hanzy isn't here a nicer solution to distinguish single source from multi source?
        if (sources.size() == 1) {
            SourceAccessor.getINSTANCE().assignListeners(sources.iterator().next());
        }
        boolean a = false;
        assert a = true;
        if (a && javax.swing.SwingUtilities.isEventDispatchThread()) {
            StackTraceElement stackTraceElement = Utilities.findCaller(Thread.currentThread().getStackTrace(), TaskProcessor.class, ParserManager.class, 
                    "org.netbeans.api.java.source.JavaSource", //NOI18N
                    "org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper"); //NOI18N
            if (stackTraceElement != null && warnedAboutRunInEQ.add(stackTraceElement)) {
                LOGGER.log(Level.WARNING, "ParserManager.parse called in AWT event thread by: {0}", stackTraceElement); // NOI18N
            }
        }
        final Request request = currentRequest.cancel(new CancelStrategy(Parser.CancelReason.USER_TASK) {
            @Override
            public boolean apply(final @NonNull Request request) {
                return true;
            }
        });
        try {
            suspendOrResumeBackgroundTasks(true);
            try {
                parserLock.lock();
                try {
                    if (lockCount < 1) {
                        for (Source source : sources) {
                            SourceAccessor.getINSTANCE ().invalidate(source,false);
                        }
                    }
                    lockCount++;
                    Utilities.runPriorityIO(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            task.run ();
                            return null;
                        }
                    });
                } catch (final Exception e) {
                    final ParseException ioe = new ParseException ();
                    ioe.initCause(e);
                    throw ioe;
                } finally {
                    lockCount--;
                    parserLock.unlock();
                }
            } finally {
                suspendOrResumeBackgroundTasks(false);
            }
        } finally {
            currentRequest.cancelCompleted (request);
        }        
    }


    /** Adds a task to scheduled requests. The tasks will run sequentially.
     * @see SchedulerTask for information about implementation requirements 
     * @task The task to run.
     * @source The source on which the task operates
     */ 
    public static void addPhaseCompletionTasks(
            @NonNull final Collection<Pair<SchedulerTask,Class<? extends Scheduler>>> tasks,
            @NonNull final SourceCache cache,
            final boolean bridge) {
        final Collection<? extends Request> rqs = toRequests(tasks, cache, bridge);
        if (handleAddRequests(cache.getSource(), rqs)) {
            cancelLowPriorityTask(rqs);
        }
    }
        
    /**
     * Removes a task from scheduled requests.
     * @param task The task to be removed.
     */
    public static void removePhaseCompletionTasks(final Collection<? extends SchedulerTask> tasks, final Source source) {
        Parameters.notNull("task", tasks);
        Parameters.notNull("source", source);
        synchronized (INTERNAL_LOCK) {
            boolean wakeUp = false;
            Collection<Request> frqs = finishedRequests.get(source);
            Collection<Request> wrqs = waitingRequests.get(source);
            for (SchedulerTask task : tasks) {
                boolean found = false;
                //Ignore excluded tasks
                final String taskClassName = task.getClass().getName();
                if (excludedTasks != null && excludedTasks.matcher(taskClassName).matches()) {
                    if (includedTasks == null || !includedTasks.matcher(taskClassName).matches()) {
                        continue;
                    }
                }
                //First) Try to find it in finished tasks
                if (frqs != null) {
                    for (Iterator<Request> it = frqs.iterator(); it.hasNext(); ) {
                        final Request rq = it.next();
                        if (rq.task == task && rq.cache != null && rq.cache.getSource() == source) {
                            it.remove();
                            found = true;
//                      break; todo: Some tasks are duplicated (racecondition?), remove even them, Prevent duplication of tasks
                        }
                    }
                    if (frqs.isEmpty()) {
                        finishedRequests.remove(source);
                        frqs = null;
                    }
                }
                //Sencond) Try to find it among started task
                for (Iterator<Request> it = requests.iterator(); it.hasNext();) {
                    final Request rq = it.next();
                    if (rq.task == task && rq.cache != null && rq.cache.getSource() == source) {
                        it.remove();
                        found = true;
                    }
                }
                //Third) Try to find in among waiting tasks
                if (wrqs != null) {
                    for (Iterator<Request> it = wrqs.iterator(); it.hasNext(); ) {
                        final Request rq = it.next();
                        if (rq.task == task && rq.cache != null && rq.cache.getSource() == source) {
                            it.remove();
                            found = true;
                        }
                    }
                    if (wrqs.isEmpty()) {
                        waitingRequests.remove(source);
                        wrqs = null;
                    }
                }
                
                //Fourh) Not found either due to weak consistency (1 task) or removing task which is not added
                if (!found) {
                    toRemove.add (new RemovedTask(source,task));
                    wakeUp = true;
                }
                SourceAccessor.getINSTANCE().taskRemoved(source);
            }
            if (wakeUp) {
                // there was a modification in toRemove, wake up the thread
                requests.add(Request.NONE);
            }
        }
    }
    
    /**
     * Reschedules the task in case it was already executed.
     * Does nothing if the task was not yet executed.
     * @param task to reschedule
     * @param source to which the task it bound
     */
    public static void rescheduleTasks(final Collection<SchedulerTask> tasks, final Source source, final Class<? extends Scheduler> schedulerType) {
        Parameters.notNull("task", tasks);
        Parameters.notNull("source", source);        
        final Request request = currentRequest.cancel (new CancelStrategy(Parser.CancelReason.PARSER_RESULT_TASK) {
            @Override
            public boolean apply(final @NonNull Request request) {
                return tasks.contains(request.task);
            }
        });
        try {
            synchronized (INTERNAL_LOCK) {
                final Collection<Request> cr = finishedRequests.get(source);
                if (cr != null) {
                    for (SchedulerTask task : tasks) {
                        if (request == null || request.task != task) {
                            List<Request> aRequests = new ArrayList<Request> ();
                            for (Iterator<Request> it = cr.iterator(); it.hasNext();) {
                                Request fr = it.next();                                
                                if (task == fr.task) {
                                    it.remove();
                                    assert fr.reschedule == ReschedulePolicy.ON_CHANGE;
                                    fr.schedulerType = schedulerType;
                                    aRequests.add(fr);
                                    if (cr.isEmpty()) {
                                        finishedRequests.remove(source);
                                    }
                                    break;
                                }
                            }
                            requests.addAll (aRequests);
                        }
                    }
                }
            }
        } finally {
            if (request != null) {
                currentRequest.cancelCompleted(request);
            }
        }
    }

    public static void updatePhaseCompletionTask (
            final @NonNull Collection<Pair<SchedulerTask,Class<? extends Scheduler>>> add,
            final @NonNull Collection<SchedulerTask> remove,
            final @NonNull Source source,
            final @NonNull SourceCache cache) {
        Parameters.notNull("add", add);
        Parameters.notNull("remove", remove);
        Parameters.notNull("source", source);
        Parameters.notNull("cache", cache);
        if (add.isEmpty() && remove.isEmpty()) {
            return;
        }
        final Collection<? extends Request> rqs = toRequests(add, cache, false);
        synchronized (INTERNAL_LOCK) {
            removePhaseCompletionTasks(remove, source);
            handleAddRequests (source, rqs);
        }
        cancelLowPriorityTask(rqs);
    }
    
    //Changes handling
    
    private static final AtomicReference<Request> rst = new AtomicReference<Request>();
    
    //DO NOT CALL DIRECTLY - called by Source
    public static Request resetState (final Source source,
            final boolean mayInterruptParser,
            final boolean sync) {
        assert source != null;
        final TaskProcessor.Request r = currentRequest.cancel (new CancelStrategy(
            Parser.CancelReason.SOURCE_MODIFICATION_EVENT,
            Request.DUMMY,
            mayInterruptParser) {
            @Override
            public boolean apply(final @NonNull Request request) {
                return true;
            }
        });
        if (sync && r != null) {
            Request oldR = rst.getAndSet(r);
            assert oldR == null;            
        }
        return r;
    }
    
    //DO NOT CALL DIRECTLY - called by EventSupport
    public static void resetStateImpl (final Source source) {
        final Request r = rst.getAndSet(null);
        currentRequest.cancelCompleted(r);
        if (source != null) {
            final boolean reschedule2 = SourceAccessor.getINSTANCE().testFlag(source, SourceFlags.RESCHEDULE_FINISHED_TASKS);

            if (reschedule2) {
                // moved from EventSupport.rescheduleImpl(), was done when RESCHEDULE_FINISHED_TASKS was set; call outside lock.
                SourceAccessor.getINSTANCE ().getCache (source).sourceModified ();
            }
            
            synchronized (INTERNAL_LOCK) {
                final boolean reschedule = SourceAccessor.getINSTANCE().testAndCleanFlags(source,SourceFlags.RESCHEDULE_FINISHED_TASKS,
                            EnumSet.of(SourceFlags.RESCHEDULE_FINISHED_TASKS, SourceFlags.CHANGE_EXPECTED));

                Collection<Request> cr;
                if (reschedule) {
                    if ((cr=finishedRequests.remove(source)) != null && cr.size()>0)  {
                        for (Request toAdd : cr) {
                            assert toAdd.reschedule == ReschedulePolicy.ON_CHANGE;
                            requests.add(toAdd);
                        }
                    }
                }
                if ((cr=waitingRequests.remove(source)) != null && cr.size()>0)  {
                    for (Request toAdd : cr) {
                        requests.add(toAdd);
                    }
                }
            }
        }
    }

    public static boolean isCancelled(@NonNull final SchedulerTask task) {
        assert task != null;
        return currentRequest.isCancelled(task);
    }
            
    //Package private methods needed by the Utilities accessor
    static void acquireParserLock () {
        suspendOrResumeBackgroundTasks(true);
        parserLock.lock();
    }

    static void releaseParserLock () {
        parserLock.unlock();
        suspendOrResumeBackgroundTasks(false);
    }

    static boolean holdsParserLock () {
        return parserLock.isHeldByCurrentThread();
    }

    static void scheduleSpecialTask (
            @NonNull final Runnable runnable,
            @NonNull final Lookup context,
            final int priority) {
        Parameters.notNull("runnable", runnable);   //NOI18N
        Parameters.notNull("context", context);     //NOI18N
        final ParserResultTask<Result> task = new ParserResultTask<Result>() {
            @Override
            public int getPriority() {
                return priority;
            }

            @Override
            public Class<? extends Scheduler> getSchedulerClass() {
                return null;
            }

            @Override
            public void cancel() {
            }

            @Override
            public void run(Result result, SchedulerEvent event) {
                runnable.run();
            }
        };
        final Collection<Request> rqs = Collections.singleton(
            new Request(task, context));
        if (handleAddRequests (null, rqs)) {
            cancelLowPriorityTask(rqs);
        }
    }


    //Private methods
    private static @NonNull Collection<? extends Request> toRequests (
            final @NonNull Collection<Pair<SchedulerTask,Class<? extends Scheduler>>> tasks,
            final @NonNull SourceCache cache,
            final boolean bridge) {
        Parameters.notNull("task", tasks);   //NOI18N
        Parameters.notNull("cache", cache);   //NOI18N
        List<Request> _requests = new ArrayList<Request> ();
        for (Pair<SchedulerTask,Class<? extends Scheduler>> task : tasks) {
            final String taskClassName = task.first().getClass().getName();
            if (excludedTasks != null && excludedTasks.matcher(taskClassName).matches()) {
                if (includedTasks == null || !includedTasks.matcher(taskClassName).matches())
                    continue;
            }
            _requests.add (new Request (task.first(), cache, bridge ? ReschedulePolicy.ON_CHANGE : ReschedulePolicy.CANCELED, task.second()));
        }
        return _requests;
    }

    private static boolean handleAddRequests (
            final @NullAllowed Source source,
            final @NonNull Collection<? extends Request> requests) {
        Parameters.notNull("requests", requests);
        if (requests.isEmpty()) {
            return false;
        }
        if (source != null) {
            SourceAccessor.getINSTANCE().assignListeners(source);
        }
        //Issue #102073 - removed running task which is readded is not performed
        synchronized (INTERNAL_LOCK) {
            TaskProcessor.requests.addAll (requests);
        }
        return true;
    }


    private static void cancelLowPriorityTask(final @NonNull Iterable<? extends Request> requests) {
        int priority = Integer.MAX_VALUE;
        for (Request r : requests) {
            priority = Math.min(priority, r.task.getPriority());
        }
        final int pf = priority;
        final Request request = currentRequest.cancel(new CancelStrategy(Parser.CancelReason.PARSER_RESULT_TASK) {
            @Override
            public boolean apply(Request request) {
                return pf < request.task.getPriority();
            }
        });        
        currentRequest.cancelCompleted(request);
     }

    /*test*/ static void cancelTask (
            final @NonNull SchedulerTask task,
            final @NonNull Parser.CancelReason reason) {
        assert task != null;
        assert reason != null;
        assert !Thread.holdsLock(INTERNAL_LOCK);
        Utilities.setTaskCancelReason(reason);
        try {
            LOGGER.fine("Cancelling task: " + task);
            task.cancel();
        } finally {
            Utilities.setTaskCancelReason(null);
        }
    }

    /*test*/ static void cancelParser(
            final @NonNull Parser parser,
            final boolean callDeprecatedCancel,
            final @NonNull Parser.CancelReason cancelReason,
            final @NullAllowed SourceModificationEvent event) {
        assert parser != null;
        assert cancelReason != null;
        assert !Thread.holdsLock(INTERNAL_LOCK);
        if (callDeprecatedCancel) {
                parser.cancel();
        }
        parser.cancel(cancelReason,event);
    }

    /*test*/ static <T extends Parser.Result> long callParserResultTask (
            final @NonNull ParserResultTask<T> task,
            final @NullAllowed T result,
            final @NullAllowed SchedulerEvent event) {
            assert task != null;
            assert !Thread.holdsLock(INTERNAL_LOCK);
            assert parserLock.isHeldByCurrentThread();
            sampler.enableSampling();
            final long now;
            final long cancelTime;
            try {
                task.run(result, event);
            } finally {
                now = System.currentTimeMillis();
                cancelTime = sampler.disableSampling();
            }
            return cancelTime == 0 ? 0 : now - cancelTime;
    }

    /*test*/ static <T extends Parser.Result> List<Embedding> callParserBaseEmbeddingProvider (
            final @NonNull ParserBasedEmbeddingProvider<T> task,
            final @NullAllowed T result,
            final @NullAllowed SchedulerEvent event) {
            assert task != null;
            assert !Thread.holdsLock(INTERNAL_LOCK);
            assert parserLock.isHeldByCurrentThread();
            sampler.enableSampling();
            final long now;
            final long cancelTime;
            try {
                return task.getEmbeddings(result);
            } finally {
                now = System.currentTimeMillis();
                cancelTime = sampler.disableSampling();
            }
//            return cancelTime == 0 ? 0 : now - cancelTime;
    }

    static List<Embedding> callEmbeddingProvider(
            final @NonNull EmbeddingProvider embeddingProvider,
            final @NonNull Snapshot snapshot) {
        assert embeddingProvider != null;
        assert snapshot != null;
        assert !Thread.holdsLock(INTERNAL_LOCK);
        //EmbeddingProvider does not do parsing no need of parserLock
        return embeddingProvider.getEmbeddings(snapshot);
    }

    public static void callUserTask(
            final @NonNull UserTask task,
            final @NonNull ResultIterator resultIterator) throws Exception {
        assert task != null;
        assert resultIterator != null;
        assert !Thread.holdsLock(INTERNAL_LOCK);
        assert parserLock.isHeldByCurrentThread();
        task.run(resultIterator);
    }

    public static void callParse(
        final @NonNull Parser parser,
        final @NullAllowed Snapshot snapshot,
        final @NonNull Task task,
        final @NullAllowed SourceModificationEvent event) throws ParseException {
        assert parser != null;
        assert task != null;
        assert !Thread.holdsLock(INTERNAL_LOCK);
        assert parserLock.isHeldByCurrentThread();
        parser.parse(snapshot, task, event);
    }

    public static Parser.Result callGetResult(
            final @NonNull Parser parser,
            final @NonNull Task task) throws ParseException {
        assert parser !=  null;
        assert task != null;
        assert !Thread.holdsLock(INTERNAL_LOCK);
        assert parserLock.isHeldByCurrentThread();
        return parser.getResult(task);

    }
    
    /**
     * Checks if the current thread holds a document write lock on some of given files
     * Slow should be used only in assertions
     * @param files to be checked
     * @return true when the current thread holds a edeitor write lock on some of given files
     */
    private static boolean holdsDocumentWriteLock (final Iterable<Source> sources) {
        assert sources != null;
        final Class<AbstractDocument> docClass = AbstractDocument.class;
        try {
            final Method method = docClass.getDeclaredMethod("getCurrentWriter"); //NOI18N
            method.setAccessible(true);
            final Thread currentThread = Thread.currentThread();
            for (Source source : sources) {
                try {
                    Document doc = source.getDocument (true);
                    if (doc instanceof AbstractDocument) {
                        Object result = method.invoke(doc);
                        if (result == currentThread) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }            
            }
        } catch (NoSuchMethodException e) {
            Exceptions.printStackTrace(e);
        }
        return false;
    }

    //Private classes
    /**
     * SchedulerTask scheduler loop
     * Dispatches scheduled tasks from {@link TaskProcessor#requests} and performs
     * them.
     */
     private static class CompilationJob implements Runnable {

        @SuppressWarnings ("unchecked") //NOI18N
        @Override
        public void run() {
            try {
                while (true) {
                    try {
                        final Request r = requests.take();
                        if (r != null && r != Request.NONE) {
                            currentRequest.setCurrentTask(r);
                            LOGGER.log(Level.FINE, "Set current request to: {0}", r);   //NOI18N
                            try {
                                new RequestPerformer(r).execute();
                            } finally {
                                currentRequest.setCurrentTask(null);
                            }
                        } else if (r != null) {
                            synchronized (INTERNAL_LOCK) {
                                toRemove.clear();
                            }
                        }
                    } catch (Throwable e) {
                        if (e instanceof InterruptedException) {
                            throw (InterruptedException) e;
                        } else if (e instanceof ThreadDeath) {
                            throw (ThreadDeath) e;
                        } else {
                            Exceptions.printStackTrace(e);
                        }
                    }
                }
            } catch (InterruptedException ie) {
                Exceptions.printStackTrace(ie);
                // stop the service.
            }
        }
    }

     private static final class RequestPerformer implements Runnable {
         private final Request r;
         private final AtomicReference<Thread> owner;
         RequestPerformer(@NonNull final Request request) {
             this.r = request;
             this.owner = new AtomicReference<>();
         }

         public void execute() throws Exception {
             if (!owner.compareAndSet(null, Thread.currentThread())) {
                 throw new IllegalStateException("Already running");    //NOI18N
             }
             try {
                Lookups.executeWith(r.context, this);
             } catch (Transfer t) {
                 throw (Exception) t.getCause();
             } finally {
                 owner.set(null);
             }
         }

         @Override
         public void run () {
             if (owner.get() != Thread.currentThread()) {
                 throw new IllegalStateException("Not an owner");    //NOI18N
             }
            try {
                final SourceCache sourceCache = r.cache;
                if (sourceCache == null) {
                    assert r.task instanceof ParserResultTask : "Illegal request: EmbeddingProvider has to be bound to Source";     //NOI18N
                    suspendOrResumeBackgroundTasks(true);
                    try {
                        parserLock.lock();
                        try {
                            try {
                                LOGGER.log(Level.FINE, "Running Special Task: {0}", r);
                                callParserResultTask((ParserResultTask) r.task, null, null);
                            } finally {
                                currentRequest.clearCurrentTask();
                                boolean cancelled = requests.contains(r);
                                if (!cancelled) {
                                }
                            }
                        } catch (RuntimeException re) {
                            Exceptions.printStackTrace(re);
                        } finally {
                            parserLock.unlock();
                        }
                    } finally {
                        suspendOrResumeBackgroundTasks(false);
                    }
                } else {
                    final Source source = sourceCache.getSnapshot().getSource();
                    assert source != null;

                    boolean reschedule = false;
                    byte validFlags = 0;
                    synchronized (INTERNAL_LOCK) {
                        if (toRemove.contains(new RemovedTask(source, r.task))) {
                            //No need to perform the task as it's removed
                            validFlags = 1;
                        } else if (SourceAccessor.getINSTANCE().testFlag(source, SourceFlags.CHANGE_EXPECTED)) {
                            validFlags = 2;
                        }
                    }

                    suspendOrResumeBackgroundTasks(true);
                    try {
                        if (validFlags == 0) {
                            Snapshot snapshot = null;
                            long[] id = new long[]{-1};
                            if (SourceAccessor.getINSTANCE().testFlag(source, SourceFlags.INVALID)) {
                                snapshot = sourceCache.createSnapshot(id);
                            }
                            parserLock.lock();
                            try {
                                if (SourceAccessor.getINSTANCE().invalidate(source, id[0], snapshot)) {
                                    lockCount++;
                                    try {
                                        if (r.task instanceof EmbeddingProvider) {
                                            if (shouldCall(source, r.task, false)) {
                                                sourceCache.refresh((EmbeddingProvider) r.task, r.schedulerType);
                                            }
                                        } else {
                                            if (shouldCall(source, r.task, true)) {
                                                currentRequest.setCurrentParser(sourceCache.getParser());
                                                final Parser.Result currentResult = sourceCache.getResult(r.task);
                                                if (currentResult != null) {
                                                    try {
                                                        if (shouldCall(source, r.task, false)) {
                                                            try {
                                                                final long startTime = System.currentTimeMillis();
                                                                long cancelTime = 0;
                                                                if (r.task instanceof ParserResultTask) {
                                                                    LOGGER.log(Level.FINE, "Running Task: {0}", r);
                                                                    ParserResultTask parserResultTask = (ParserResultTask) r.task;
                                                                    SchedulerEvent schedulerEvent = SourceAccessor.getINSTANCE().getSchedulerEvent(source, parserResultTask.getSchedulerClass());
                                                                    cancelTime = callParserResultTask(parserResultTask, currentResult, schedulerEvent);
                                                                } else if (r.task instanceof ParserBasedEmbeddingProvider) {
                                                                    LOGGER.log(Level.FINE, "Running Embedding Task: {0}", r);
                                                                    ParserBasedEmbeddingProvider parserBasedEmbeddingProvider = (ParserBasedEmbeddingProvider) r.task;
                                                                    SchedulerEvent schedulerEvent = SourceAccessor.getINSTANCE().getSchedulerEvent(source, parserBasedEmbeddingProvider.getSchedulerClass());
                                                                    sourceCache.refresh(callParserBaseEmbeddingProvider(parserBasedEmbeddingProvider, currentResult, schedulerEvent), parserBasedEmbeddingProvider, r.schedulerType);
                                                                } else {
                                                                    assert false :
                                                                        String.format (
                                                                            "Unknown task type: %s : %s",   //NOI18N
                                                                            r.task,
                                                                            r.task.getClass());
                                                                }
                                                                if (LOGGER.isLoggable(Level.FINEST)) {
                                                                    LOGGER.log(
                                                                            Level.FINEST,
                                                                            "Executed task: {0} : {1} in {2} ms.", //NOI18N
                                                                            new Object[]{
                                                                                r.task,
                                                                                r.task.getClass(),
                                                                                System.currentTimeMillis() - startTime
                                                                            });
                                                                }
                                                                if (cancelTime > SLOW_CANCEL_LIMIT) {
                                                                    LOGGER.log(
                                                                            Level.INFO,
                                                                            "Task: {0} : {1} ignored cancel for {2} ms.", //NOI18N
                                                                            new Object[]{
                                                                                r.task,
                                                                                r.task.getClass(),
                                                                                cancelTime
                                                                            });
                                                                }
                                                            } catch (Exception re) {
                                                                Exceptions.printStackTrace(re);
                                                            }
                                                        }
                                                    } finally {
                                                        ParserAccessor.getINSTANCE().invalidate(currentResult);
                                                    }
                                                }
                                            }
                                        }
                                    } finally {
                                        lockCount--;
                                    }
                                } else {
                                    reschedule = true;
                                }
                            } finally {
                                parserLock.unlock();
                            }
                        }
                    } finally {
                        suspendOrResumeBackgroundTasks(false);
                    }
                    //Maybe should be in finally to prevent task lost when parser crashes
                    //threading: Cannot be called with suspended RU. When RU is suspended and
                    //and CC is active and waits for parseWhenScanFinished the setCurrentTask
                    //deadlocks as it waits until CC becomes inactive
                    if (r.reschedule != ReschedulePolicy.NEVER) {
                        reschedule |= currentRequest.setCurrentTask(null);
                        synchronized (INTERNAL_LOCK) {
                            if (!toRemove.contains(new RemovedTask(source, r.task))) {
                                if (validFlags == 2) {
                                    if (SourceAccessor.getINSTANCE().testFlag(source, SourceFlags.CHANGE_EXPECTED)) {
                                        Collection<Request> rc = waitingRequests.get(source);
                                        if (rc == null) {
                                            rc = new LinkedList<Request>();
                                            waitingRequests.put(source, rc);
                                        }
                                        rc.add(r);
                                        LOGGER.log(Level.FINE, "Waiting Task: {0}", r);      //NOI18N
                                    } else {
                                        requests.add(r);
                                        LOGGER.log(Level.FINE, "Rescheduling Waiting Task: {0}", r); //NOI18N
                                    }
                                } else if (reschedule || SourceAccessor.getINSTANCE().testFlag(source, SourceFlags.INVALID)) {
                                    //The JavaSource was changed or canceled rechedule it now
                                    requests.add(r);
                                    LOGGER.log(Level.FINE, "Rescheduling Canceled Task: {0}", r); //NOI18N
                                } else if (r.reschedule == ReschedulePolicy.ON_CHANGE) {
                                    //Up to date JavaSource add it to the finishedRequests
                                    Collection<Request> rc = finishedRequests.get(r.cache.getSnapshot().getSource());
                                    if (rc == null) {
                                        rc = new LinkedList<Request>();
                                        finishedRequests.put(r.cache.getSnapshot().getSource(), rc);
                                    }
                                    rc.add(r);
                                    LOGGER.log(Level.FINE, "Finished ON_CHANGE Task: {0}", r); //NOI18N
                                } else {
                                    LOGGER.log(Level.FINE, "Finished  CANCELED Task: {0}", r); //NOI18N
                                }
                            } else {
                                LOGGER.log(Level.FINE, "Removing Task: {0}", r); //NOI18N
                            }
                            toRemove.clear();
                        }
                    } else {
                        synchronized (INTERNAL_LOCK) {
                            if (validFlags == 2 && !toRemove.contains(new RemovedTask(source, r.task))) {
                                if (SourceAccessor.getINSTANCE().testFlag(source, SourceFlags.CHANGE_EXPECTED)) {
                                    Collection<Request> rc = waitingRequests.get(source);
                                    if (rc == null) {
                                        rc = new LinkedList<Request>();
                                        waitingRequests.put(source, rc);
                                    }
                                    rc.add(r);
                                    LOGGER.log(Level.FINE, "Waiting NEVER Task: {0}", r.toString()); //NOI18N
                                } else {
                                    requests.add(r);
                                    LOGGER.log(Level.FINE, "Rescheduling Waiting NEVER Task: {0}", r.toString()); //NOI18N
                                }
                            } else {
                                LOGGER.log(Level.FINE, "Finished NEVER task: {0}", r.toString()); //NOI18N
                            }
                            toRemove.clear();
                        }
                        SourceAccessor.getINSTANCE().taskRemoved(source);
                    }
                }
            } catch (InterruptedException | ParseException e) {
                throw new Transfer(e);
            }
        }

         private static final class Transfer extends RuntimeException {
             Transfer(Exception e) {
                 super(e);
             }
         }
    }

    private static boolean shouldCall (
        @NonNull final Source source,
        @NullAllowed final SchedulerTask task,
        final boolean checkScan) {
        final boolean sourceInvalid = SourceAccessor.getINSTANCE().testFlag(
            source,
            SourceFlags.INVALID);
        boolean scanAffinity = true;
        if (checkScan) {
            final boolean scanInProgress = getIndexerBridge().isIndexing();
            final boolean canRunDuringScan = (task instanceof IndexingAwareParserResultTask)
                && ((IndexingAwareParserResultTask)task).getIndexingMode() == TaskIndexingMode.ALLOWED_DURING_SCAN;
            final boolean compatMode = "true".equals(System.getProperty(COMPAT_MODE));  //NOI18N
            scanAffinity = !scanInProgress || canRunDuringScan || compatMode;
        }
        final boolean taskCancelled = currentRequest.isCancelled(task);
        return !sourceInvalid && scanAffinity && !taskCancelled;
    }

    private enum ReschedulePolicy {
        NEVER,
        CANCELED,
        ON_CHANGE
    }

    /**
     * Request for performing a task on given Source
     */
    //@ThreadSafe
    static class Request {

        static final Request DUMMY = new Request () {
            @Override
            public String toString() {
                return "DUMMY"; //NOI18N
            }
        };
        static final Request NONE = new Request() {
            @Override
            public String toString() {
                return "NONE";  //NOI18N
            }
        };

        private final SchedulerTask task;
        private final SourceCache cache;
        private final ReschedulePolicy reschedule;
        private final Lookup context;
        private Class<? extends Scheduler> schedulerType;

        /**
         * Creates new Request
         * @param task to be performed
         * @param source on which the task should be performed
         * @param reschedule when true the task is periodic request otherwise one time request
         */
        private Request (
                @NonNull final SchedulerTask task,
                @NonNull final SourceCache cache,
                @NonNull final ReschedulePolicy reschedule,
                @NullAllowed final Class<? extends Scheduler> schedulerType) {
            Parameters.notNull("task", task);   //NOI18N
            Parameters.notNull("cache", cache); //NOI18N
            Parameters.notNull("reschedule", reschedule);   //NOI18N
            this.task = task;
            this.cache = cache;
            this.reschedule = reschedule;
            this.context = this.cache.getSource().getLookup();
            this.schedulerType = schedulerType;
        }

        private Request (
            @NonNull final SchedulerTask task,
            @NonNull final Lookup context) {
            Parameters.notNull("tesk", task);   //NOI18N
            Parameters.notNull("context", context); //NOI18N
            this.task = task;
            this.cache = null;
            this.reschedule = ReschedulePolicy.NEVER;
            this.context = context;
            this.schedulerType = null;
        }

        private Request () {
            this (
                new ParserResultTask(){
                    @Override
                    public int getPriority() {
                        return 0;
                    }
                    @Override
                    public Class<? extends Scheduler> getSchedulerClass() {
                        return null;
                    }
                    @Override
                    public void cancel() {
                    }
                    @Override
                    public void run(Result result, SchedulerEvent event) {
                    }
                }, Lookup.EMPTY);
        }

        @Override
        public String toString () {
            if (reschedule != ReschedulePolicy.NEVER) {
                return String.format("Periodic request %d to perform: %s on: %s",  //NOI18N
                        System.identityHashCode(this),
                        task == null ? null : task.toString(),
                        cache == null ? null : cache.toString());
            } else {
                return String.format("One time request %d to perform: %s on: %s",  //NOI18N
                        System.identityHashCode(this),
                        task == null ? null : task.toString(),
                        cache == null ? null : cache.toString());
            }
        }

        @Override
        public int hashCode () {
            return this.task == null ? 0 : this.task.getPriority();
        }

        @Override
        public boolean equals (Object other) {
            if (other instanceof Request) {
                Request otherRequest = (Request) other;
                return reschedule == otherRequest.reschedule
                    && (cache == null ? otherRequest.cache == null : cache.equals (otherRequest.cache))
                    && (task == null ? otherRequest.task == null : task.equals(otherRequest.task));
            }
            else {
                return false;
            }
        }
    }

    /**
     * Comparator of {@link Request}s which oreders them using {@link SchedulerTask#getPriority()}
     */
    //@ThreadSafe
    private static class RequestPriorityComparator implements Comparator<Request> {
        @Override
        public int compare (Request r1, Request r2) {
            assert r1 != null && r2 != null;
            return r1.task.getPriority() - r2.task.getPriority();
        }
    }
       
    private abstract static class CancelStrategy {
        
        private final Parser.CancelReason cancelReason;
        private final Request cancelReplace;
        private final boolean callDeprecatedParserCancel;
        
        CancelStrategy(final @NonNull Parser.CancelReason cancelReason) {
            this(cancelReason,null,false);
        }

        CancelStrategy(
            final @NonNull Parser.CancelReason cancelReason,
            final @NullAllowed Request cancelReplace,
            final boolean callDeprecatedParserCancel) {
            Parameters.notNull("cancelReason", cancelReason);   //NOI18N
            this.cancelReason = cancelReason;
            this.cancelReplace = cancelReplace;
            this.callDeprecatedParserCancel = callDeprecatedParserCancel;
        }
        
        public final @NonNull Parser.CancelReason getCancelReason() {
            return cancelReason;
        }

        public final @CheckForNull Request getRequestToCancel() {
            return this.cancelReplace;
        }

        public final boolean callDeprecatedParserCancel() {
            return callDeprecatedParserCancel;
        }

        public abstract boolean apply(@NonNull Request request);
    }
    
    /**
     *  Encapsulates current request. May be transformed into
     *  JavaSource private static methods, but it may be less readable.
     */
    //@ThreadSafe
    private static final class CurrentRequestReference {                        

        //@GuardedBy("CRR_LOCK")
        private Request reference;
        //@GuardedBy("CRR_LOCK")
        private Request canceledReference;
        //@GuardedBy("CRR_LOCK")
        private Parser activeParser;       
        //@GuardedBy("CRR_LOCK")
        private SchedulerTask canceled;
        /**
         * Threading: The CurrentRequestReference has it's own private lock
         * rather than the INTERNAL_LOCK to prevent deadlocks caused by events
         * fired under Document locks. So, it's NOT allowed to call outside this
         * class (to the rest of the parsing api) under the private lock!
         */
        private static class CRRLock {};
        private static final Object CRR_LOCK = new CRRLock();
        
        boolean setCurrentTask (Request reference) throws InterruptedException {
            boolean result = false;
            assert !parserLock.isHeldByCurrentThread();
            assert reference == null || reference.cache == null || !Thread.holdsLock(INTERNAL_LOCK);
            synchronized (CRR_LOCK) {
                while (this.canceledReference!=null) {
                    CRR_LOCK.wait();
                }
                result = this.canceled != null;
                canceled = null;
                this.activeParser = null;
                this.reference = reference;
            }
            return result;
        }

        void clearCurrentTask () {
            synchronized (CRR_LOCK) {
                this.reference = null;
            }
        }

        void setCurrentParser (final Parser parser) {
            synchronized (CRR_LOCK) {
                activeParser = parser;
            }
        }
        
        Request cancel (final @NonNull CancelStrategy cancelStrategy) {
            Request request = null;
            Parser parser = null;
            if (!WORKER.isRequestProcessorThread()) {
                synchronized (CRR_LOCK) {
                    if (this.reference != null && cancelStrategy.apply(this.reference)) {
                        assert this.canceledReference == null;
                        request = this.reference;
                        this.canceledReference = request;
                        this.reference = null;
                        this.canceled = request.task;
                        parser = activeParser;
                        sampler.schedule();
                    } else if (canceledReference == null && cancelStrategy.getRequestToCancel()!=null) {
                        request = cancelStrategy.getRequestToCancel();
                        this.canceledReference = request;
                        parser = activeParser;
                    }
                }
                final Parser.CancelReason cancelReason = cancelStrategy.getCancelReason();
                try {
                    try {
                        if (parser != null) {
                            if (cancelReason == Parser.CancelReason.SOURCE_MODIFICATION_EVENT) {
                                Source src;
                                SourceCache sc;
                                if (request != null && (sc = request.cache)!= null && (src=sc.getSource())!=null) {
                                    cancelParser(
                                        parser,
                                        cancelStrategy.callDeprecatedParserCancel(),
                                        cancelReason,
                                        SourceAccessor.getINSTANCE().getSourceModificationEvent(src));
                                }
                            } else {
                                cancelParser (parser, false, cancelReason, null);
                            }
                        }
                    } finally {
                        if (request != null) {
                            cancelTask(request.task, cancelReason);
                        }
                    }
                } catch (Throwable t) {
                    //If the client code in cancel throws a Throwable
                    //log it and continue.
                    if (t instanceof ThreadDeath) {
                        throw (ThreadDeath) t;
                    } else {
                        Exceptions.printStackTrace(t);
                    }
                }
            }
            return request;
        }                                                                                        
        
        void cancelCompleted (final Request request) {
            if (request != null) {
                synchronized (CRR_LOCK) {
                    assert request == this.canceledReference;
                    this.canceledReference = null;
                    CRR_LOCK.notify();
                }
            }
        }

       boolean isCancelled(@NullAllowed final SchedulerTask task) {
           synchronized (CRR_LOCK) {
               return this.canceled == task;
           }
       }
    }

    

    static final class RemovedTask extends WeakReference<Source> implements Runnable {
        
        private final SchedulerTask task;
        
        public RemovedTask(final @NonNull Source src, final @NonNull SchedulerTask task) {
            super (src, org.openide.util.BaseUtilities.activeReferenceQueue());
            Parameters.notNull("src", src);     //NOI18N
            Parameters.notNull("task", task);   //NOI18N
            this.task = task;            
        }
        
        @Override
        public boolean equals(final Object other) {
            if (!(other instanceof RemovedTask)) {
                return false;
            }
            final RemovedTask otherRt = (RemovedTask) other;
            final Source thisSrc = get();
            final Source otherSrc = otherRt.get();
            return (thisSrc == null ? otherSrc == null : thisSrc.equals(otherSrc)) &&
                (this.task.equals(otherRt.task));
        }
        
        @Override
        public int hashCode() {
            return task.hashCode();
        }
        
        @Override
        public String toString() {
            return String.format("RemovedTask[%s, %s]", get(), task);   //NOI18N
        }

        @Override
        public void run() {
            synchronized (INTERNAL_LOCK) {
                for (Iterator<RemovedTask> it = toRemove.iterator(); it.hasNext(); ) {
                    final RemovedTask rt = it.next();
                    if (rt == this) {
                        it.remove();
                        break;
                    }
                }
            }
        }                
    }

    private static class Sampler implements Runnable {

        //GuardedBy("this")
        private boolean samplingEnabled;
        //@GuardedBy("this")
        private SelfProfile profiler;
        //@GuardedBy("this")
        private long cancelTime;



        synchronized void schedule() {
            this.cancelTime = System.currentTimeMillis();
            if (samplingEnabled) {
                SAMPLING_TASK.schedule(
                    Integer.getInteger("org.netbeans.modules.parsing.api.taskcancel.slowness.start",    // NOI18N
                    DEFAULT_START_SAMPLING_AFTER));
            }
        }


        synchronized void enableSampling() {
            this.cancelTime = 0L;
            boolean ae = false;
            assert ae = true;
            if (ae && SAMPLING_ENABLED) {
                samplingEnabled = true;
            }
        }

        synchronized long disableSampling() {
            try {
                samplingEnabled = false;
                SAMPLING_TASK.cancel();
            } finally {
                if (profiler != null) {
                    profiler.stop();
                    profiler = null;
                }
            }
            return cancelTime;
        }

        @Override
        public synchronized void run() {            
            if (!samplingEnabled) {
                return;
            }
            assert profiler == null;
            profiler = new SelfProfile(System.currentTimeMillis());
        }
    }

    private static void suspendOrResumeBackgroundTasks(boolean stop) {
        final ScanSuspendImplementation suspend = getScanSuspend();
        if (stop) {
            suspend.suspend();
        } else {
            suspend.resume();
        }
    }

    private static volatile ScanSuspendImplementation scanSuspend;

    @NonNull
    private static ScanSuspendImplementation getScanSuspend() {
        ScanSuspendImplementation suspend = scanSuspend;
        if (suspend == null) {
            suspend  = Lookup.getDefault().lookup(ScanSuspendImplementation.class);
            if (suspend == null) {
                suspend = new ScanSuspendImplementation() {
                    @Override
                    public void suspend() {
                    }
                    @Override
                    public void resume() {
                    }
                };
            }
            scanSuspend = suspend;
        }
        return suspend;
    }

    private static volatile IndexerBridge indexingBridge;

    public static IndexerBridge getIndexerBridge() {
        IndexerBridge bridge = indexingBridge;
        if (bridge != null) {
            return bridge;
        }
        bridge = Lookup.getDefault().lookup(IndexerBridge.class);
        if (bridge == null) {
            bridge = new IndexerBridge() { 
                @Override
                public boolean isIndexing() {
                    return false;
                }

                @Override
                public boolean ownsProtectedMode() {
                    return false;
                }
            };
        }
        return indexingBridge = bridge;
    }
}
