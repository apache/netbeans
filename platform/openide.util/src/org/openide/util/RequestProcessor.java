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

package org.openide.util;

import java.lang.reflect.Method;
import java.security.PrivilegedAction;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.Lookups;

/** Request processor is {@link Executor} (since version 7.16) capable to
 * perform asynchronous requests in a dedicated thread pool.
 * <A name="use_cases">There are several use cases for RequestProcessor</A>,
 * most of them start with creating own <code>RequestProcessor</code>
 * instance (which by itself is quite lightweight).
 * <p>
 * <strong>Do something later</strong>
 * <p>
 * In case you want something to be done later in some background thread,
 * create an instance of <code>RequestProcessor</code> and post tasks to it.
 * <pre>
 * private static final RequestProcessor RP = new {@link RequestProcessor#RequestProcessor(java.lang.Class) RequestProcessor(MyClass.class)};
 * // later
 * RP.{@link #post(java.lang.Runnable,int) post(runnable,&nbsp;delay)}
 * </pre>
 *
 * The above example guarantees that there is at most one runnable being
 * processed in parallel. All your requests are serialized and processed
 * one by one. <code>RP</code> works here like a simple mutex.
 * <p>
 * In case you want more tasks to run in parallel (not very often use case)
 * you can specify higher
 * throughput via {@link #RequestProcessor(java.lang.String, int)}. Then
 * the <code>RP</code> works like a queue of requests passing through a
 * semaphore with predefined number of <CODE>DOWN()</CODE>s.
 * <p>
 * You can wait for your tasks to be processed by keeping a reference to the
 * last one and using {@link RequestProcessor.Task#waitFinished waitFinished()}:
 * <pre>
 * private static final RequestProcessor RP = new RequestProcessor("My tasks");
 * private volatile {@link RequestProcessor.Task} last;
 *
 * // when posting update the task
 * last = RP.{@link #post(java.lang.Runnable,int) post(runnable,&nbsp;delay)}
 *
 * // later wait
 * last.{@link RequestProcessor.Task#waitFinished waitFinished()}
 * </pre>
 * <p>
 * <strong>Periodic task</strong>
 * <p>
 * It is also possible to do something periodically. Use the {@link RequestProcessor.Task#schedule schedule} method:
 * <pre>
 * class Periodic implements Runnable {
 *   private static final RequestProcessor RP = new {@link RequestProcessor#RequestProcessor(java.lang.Class) RequestProcessor(Periodic.class)};
 *   private final RequestProcessor.Task CLEANER = RP.{@link #create(java.lang.Runnable) create(this)};
 *   public void run() {
 *     doTheWork();
 *     CLEANER.schedule(DELAY);
 *   }
 * }
 * </pre>
 *  Please think twice before using such periodic
 *  background activity. It is generally considered evil if some code runs
 *  without any user action. Your code shall respect  the application's state,
 *  and for example when the application is minimized, do nothing.
 * <p>
 * <strong>Sliding task</strong>
 * <p>
 * Often you want to perform an update of your object internals
 * based on changes in some model. However your update may be costly
 * and you want to do it just once, regardless of how many changes are
 * reported by the model. This can be achieved with a sliding task:
 * <pre>
 * class Updater implements PropertyChangeListener, Runnable {
 *   private static final RequestProcessor RP = new {@link RequestProcessor#RequestProcessor(java.lang.Class) RequestProcessor(Updater.class)};
 *   private final RequestProcessor.Task UPDATE = RP.{@link #create(java.lang.Runnable) create(this)};
 *
 *   public void propertyChange(PropertyChangeEvent ev) {
 *     UPDATE.{@link RequestProcessor.Task#schedule schedule(1000)};
 *   }
 *
 *   public void run() {
 *     doTheWork();
 *   }
 * }
 * </pre>
 * The above code coalesces all events that arrive in 1s and for all of them
 * does <code>doTheWork</code> just once.
 *
 * <p>
 * <strong>Interruption of tasks</strong>
 * <p>
 * Since version 6.3 there is a conditional support for interruption of long running tasks.
 * There always was a way to cancel not yet running task using {@link RequestProcessor.Task#cancel }
 * but if the task's run() method was already running, one was out of luck.
 * Since version 6.3
 * the thread running the task is interrupted and the Runnable can check for that
 * and terminate its execution sooner. In the runnable one shall check for 
 * thread interruption (done from {@link RequestProcessor.Task#cancel }) and 
 * if true, return immediately as in this example:
 * <pre>
 * private static final RequestProcessor RP = new {@link #RequestProcessor(String,int,boolean) RequestProcessor("Interruptible", 1, true)};
 * public void run () {
 *     while (veryLongTimeLoop) {
 *       doAPieceOfIt ();
 *
 *       if (Thread.interrupted ()) return;
 *     }
 * }
 * </pre>
 * <p>
 * Since <code>org.openide.util</code>, implements
 * {@link java.util.concurrent.ScheduledExecutorService}
 * @author Petr Nejedly, Jaroslav Tulach, Tim Boudreau
 */
public final class RequestProcessor implements ScheduledExecutorService {

    static {
        Processor.class.hashCode(); // ensure loaded; cf. FELIX-2128
    }

    /** the static instance for users that do not want to have own processor */
    private static final RequestProcessor DEFAULT = new RequestProcessor();

    /** logger */
    private static final Logger logger = Logger.getLogger("org.openide.util.RequestProcessor"); // NOI18N

    /** the static instance for users that do not want to have own processor */
    private static final RequestProcessor UNLIMITED;
    /** The counter for automatic naming of unnamed RequestProcessors */
    private static int counter = 0;
    private static final boolean SLOW;
    static {
        boolean slow = false;
        assert slow = true;
        SLOW = slow;
        // 55: a conservative value, just for case of misuse
        UNLIMITED = new RequestProcessor("Default RequestProcessor", 55, false, SLOW, SLOW ? 3 : 0); // NOI18N
    }

    /** The name of the RequestProcessor instance */
    String name;

    /** If the RP was stopped, this variable will be set, every new post()
     * will throw an exception and no task will be processed any further */
    volatile boolean stopped = false;
    
    /** Flag indicating that awaiting tasks should be executed although
     * RP is in stopped state (rejecting new tasks) */
    volatile boolean finishAwaitingTasks = false;

    /** The lock covering following five fields. They should be accessed
     * only while having this lock held. */
    private final Object processorLock = new Object();

    /** The set holding all the Processors assigned to this RequestProcessor */
    private final HashSet<Processor> processors = new HashSet<Processor>();

    /** Actualy the first item is pending to be processed.
     * Can be accessed/trusted only under the above processorLock lock.
     * If null, nothing is scheduled and the processor is not running. 
     * @GuardedBy("processorLock")
     */
    private final SortedSet<Item> queue = new TreeSet<Item>();

    /** The maximal number of processors that can perform the requests sent
     * to this RequestProcessors. If 1, all the requests are serialized. */
    private int throughput;
    /** mapping of classes executed in parallel */
    private Map<Class<? extends Runnable>,AtomicInteger> inParallel;
    /** Warn if there is parallel execution */
    private final int warnParallel;
    
    /** support for interrupts or not? */
    private boolean interruptThread;
    /** fill stacktraces when task is posted? */
    private boolean enableStackTraces;

    /** Creates new RequestProcessor with automatically assigned unique name. */
    public RequestProcessor() {
        this(null, 1);
    }

    /** Creates a new named RequestProcessor with throughput 1.
     * @param name the name to use for the request processor thread */
    public RequestProcessor(String name) {
        this(name, 1);
    }

    /** Convenience constructor for a new RequestProcessor with throughput 1.
     * Typical usage is:
     * <pre>
     * class MyClass {
     *   private static final RequestProcessor RP = new RequestProcessor(MyClass.class);
     * 
     * }
     * </pre>
     * Behaves as <code>new RequestProcessor(MyClass.class.getName())</code>.
     *
     * @param forClass name of this class gives name for the processor threads
     * @since 8.6
     */
    public RequestProcessor(Class<?> forClass) {
        this(forClass.getName());
    }

    /** Creates a new named RequestProcessor with defined throughput.
     * @param name the name to use for the request processor thread
     * @param throughput the maximal count of requests allowed to run in parallel
     *
     * @since OpenAPI version 2.12
     */
    public RequestProcessor(String name, int throughput) {
        this(name, throughput, false);
    }

    /** Creates a new named RequestProcessor with defined throughput which 
     * can support interruption of the thread the processor runs in.
     * There always was a way how to cancel not yet running task using {@link RequestProcessor.Task#cancel }
     * but if the task was already running, one was out of luck. With this
     * constructor one can create a {@link RequestProcessor} which threads
     * thread running tasks are interrupted and the Runnable can check for that
     * and terminate its execution sooner. In the runnable one shall check for 
     * thread interruption (done from {@link RequestProcessor.Task#cancel }) and 
     * if true, return immediatelly as in this example:
     * <PRE>
     * public void run () {
     *     while (veryLongTimeLook) {
     *       doAPieceOfIt ();
     *
     *       if (Thread.interrupted ()) return;
     *     }
     * }
     * </PRE>
     *
     * @param name the name to use for the request processor thread
     * @param throughput the maximal count of requests allowed to run in parallel
     * @param interruptThread true if {@link RequestProcessor.Task#cancel} shall interrupt the thread
     *
     * @since 6.3
     */
    public RequestProcessor(String name, int throughput, boolean interruptThread) {
        this(name, throughput, interruptThread, SLOW);
    }

    /** Creates a new named <code>RequestProcessor</code> that allows to disable stack trace filling.
     * By default, when assertions are on, each task posted on <code>RequestProcessor</code> stores
     * the stack trace at the time of posting. When an exception is later thrown from the task,
     * it allows to print not only stack trace of the task but also stack trace of the code that posted it.
     * However this may be a performance bottleneck in cases when hundreds of short task are scheduled.
     * This constructor then allows to create <code>RequestProcessor</code> which never stores stack traces
     * at the time of posting.
     * <p>
     * See constructor {@link #RequestProcessor(String, int, boolean)} for details of <code>interruptThread</code>
     * parameter.
     * </p>
     * @param name the name to use for the request processor thread
     * @param throughput the maximal count of requests allowed to run in parallel
     * @param interruptThread true if {@link RequestProcessor.Task#cancel} shall interrupt the thread
     * @param enableStackTraces <code>false</code> when request processor should not fill stack traces when task is posted.
     *              Default is <code>true</code> when assertions are enabled, <code>false</code> otherwise.
     * @since 7.24
     */
    public RequestProcessor(String name, int throughput, boolean interruptThread, boolean enableStackTraces) {
        this(name, throughput, interruptThread, enableStackTraces, 0);
    }

    private RequestProcessor(String name, int throughput, boolean interruptThread, boolean enableStackTraces, int warnParallel) {
        this.throughput = throughput;
        this.name = (name != null) ? name : ("OpenIDE-request-processor-" + (counter++));
        this.interruptThread = interruptThread;
        this.enableStackTraces = enableStackTraces;
        this.warnParallel = warnParallel;
    }

    
    /** <b>Warning:</b> The instance of <code>RequestProcessor</code> returned
     * by this method has very bad performance side effects, don't use unless
     * you understand all implications!
     * <p>
     * This is the getter for the shared instance of the <CODE>RequestProcessor</CODE>.
     * This instance is shared by anybody who
     * needs a way of performing <em>sporadic</em> asynchronous work.
     * <p>
     * The problem of this method lays exactly in the definition of <em>sporadic</em>.
     * Often one needs to process something at some <em>sporadic</em> moment,
     * but, for example
     * due to <em>storm of events</em>, one needs to execute more than one tasks
     * at the same <em>sporadic</em> moment. In this situation
     * using {@link #getDefault()} is horribly inefficient. All such tasks
     * would be processed in parallel, allocating their own execution threads
     * (up to 50). As the price per one thread is estimated to 1MB on common
     * systems, you shall think twice whether you want to increase the memory
     * consumption of your application so much at these <em>sporadic</em> moments.
     * <p>
     * There is a runtime detection of the <em>parallel misuse</em> of this
     * method since version 8.3. It is activated only in development mode
     * (when executed with assertions on) and prints warning into log
     * whenever there are more than three same tasks running in parallel.
     * In case you see such warning, or in case you are in doubts consider
     * creation of your own, private, single throughput processor:
     * <pre>
     * class YourClass {
     *   private static final RequestProcessor RP = new {@link RequestProcessor#RequestProcessor(java.lang.Class) RequestProcessor(YourClass.class)};
     * }
     * </pre>
     * Such private field is lightweight and guarantees that all your tasks
     * will be processed sequentially, one by one. Just don't forget to make
     * the field static!
     * <p>
     * Tasks posted to this instance may be canceled until they start their
     * execution. If a there is a need to cancel a task while it is running
     * a seperate request processor needs to be created via 
     * {@link #RequestProcessor(String, int, boolean)} constructor.
     *
     * @return an instance of RequestProcessor that is capable of performing
     * "unlimited" (currently limited to 50, just for case of misuse) number
     * of requests in parallel. 
     *
     * @see #RequestProcessor(String, int, boolean)
     * @see RequestProcessor.Task#cancel
     *
     * @since version 2.12
     */
    public static RequestProcessor getDefault() {
        return UNLIMITED;
    }

    /** Implements contract of {@link Executor}. 
     * Simply delegates to {@link #post(java.lang.Runnable)}.
     * @param command the runnable to execute
     * @since 7.16
     */
    @Override
    public void execute(Runnable command) {
        post(command);
    }
    
    /** This methods asks the request processor to start given
     * runnable immediately. The default priority is {@link Thread#MIN_PRIORITY}.
     *
     * @param run class to run
     * @return the task to control the request
     */
    public Task post(Runnable run) {
        return post(run, 0, Thread.MIN_PRIORITY);
    }

    /** This methods asks the request processor to start given
    * runnable after <code>timeToWait</code> milliseconds. The default priority is {@link Thread#MIN_PRIORITY}.
    *
    * @param run class to run
    * @param timeToWait to wait before execution
    * @return the task to control the request
    */
    public Task post(final Runnable run, int timeToWait) {
        return post(run, timeToWait, Thread.MIN_PRIORITY);
    }

    /** This methods asks the request processor to start given
    * runnable after <code>timeToWait</code> milliseconds. Given priority is assigned to the
    * request. <p>
    * For request relaying please consider:
    * <pre>
    *    post(run, timeToWait, Thread.currentThread().getPriority());
    * </pre>
    *
    * @param run class to run
    * @param timeToWait to wait before execution
    * @param priority the priority from {@link Thread#MIN_PRIORITY} to {@link Thread#MAX_PRIORITY}
    * @return the task to control the request
    */
    public Task post(final Runnable run, int timeToWait, int priority) {
        RequestProcessor.Task task = new Task(run, priority);
        task.schedule(timeToWait);

        return task;
    }

    /** Creates request that can be later started by setting its delay.
    * The request is not immediatelly put into the queue. It is planned after
    * setting its delay by schedule method. By default the initial state of 
    * the task is <code>!isFinished()</code> so doing waitFinished() will
    * block on and wait until the task is scheduled.
    *
    * @param run action to run in the process
    * @return the task to control execution of given action
    */
    public Task create(Runnable run) {
        return create(run, false);
    }
    
    /** Creates request that can be later started by setting its delay.
    * The request is not immediatelly put into the queue. It is planned after
    * setting its delay by schedule method.
    *
    * @param run action to run in the process
    * @param initiallyFinished should the task be marked initially finished? If 
    *   so the {@link Task#waitFinished} on the task will succeeded immediatelly even
    *   the task has not yet been {@link Task#schedule}d.
    * @return the task to control execution of given action
    * @since 6.8
    */
    public Task create(Runnable run, boolean initiallyFinished) {
        Task t = new Task(run);
        t.markCreated();
        if (initiallyFinished) {
            t.notifyFinished();
        }
        return t;
    }
    

    /** Tests if the current thread is request processor thread.
    * This method could be used to prevent the deadlocks using
    * <CODE>waitFinished</CODE> method. Any two tasks created
    * by request processor must not wait for themself.
    *
    * @return <CODE>true</CODE> if the current thread is request processor
    *          thread, otherwise <CODE>false</CODE>
    */
    public boolean isRequestProcessorThread() {
        Thread c = Thread.currentThread();
        if (c instanceof Processor) {
            Processor p = (Processor)c;
            return p.procesing == this;
        }
        return false;
    }

    /** Stops processing of runnables processor.
    * The currently running runnable is finished and no new is started.
    */
    public void stop() {
        if ((this == UNLIMITED) || (this == DEFAULT)) {
            throw new IllegalArgumentException("Can't stop shared RP's"); // NOI18N
        }

        synchronized (processorLock) {
            stopped = true;

            for (Processor p : processors) {
                p.interrupt();
            }
        }
    }

    //
    // Static methods communicating with default request processor
    //

    /** This methods asks the request processor to start given
     * runnable after <code>timeToWait</code> milliseconds. The default priority is {@link Thread#MIN_PRIORITY}.
     *
     * @param run class to run
     * @return the task to control the request
     *
     * @deprecated Sharing of one singlethreaded <CODE>RequestProcessor</CODE>
     * among different users and posting even blocking requests is inherently
     * deadlock-prone. See {@link RequestProcessor use cases}. */
    @Deprecated
    public static Task postRequest(Runnable run) {
        return DEFAULT.post(run);
    }

    /** This methods asks the request processor to start given
     * runnable after <code>timeToWait</code> milliseconds.
     * The default priority is {@link Thread#MIN_PRIORITY}.
     *
     * @param run class to run
     * @param timeToWait to wait before execution
     * @return the task to control the request
     *
     * @deprecated Sharing of one singlethreaded <CODE>RequestProcessor</CODE>
     * among different users and posting even blocking requests is inherently
     * deadlock-prone. See {@link RequestProcessor use cases}. */
    @Deprecated
    public static Task postRequest(final Runnable run, int timeToWait) {
        return DEFAULT.post(run, timeToWait);
    }

    /** This methods asks the request processor to start given
     * runnable after <code>timeToWait</code> milliseconds. Given priority is assigned to the
     * request.
     * @param run class to run
     * @param timeToWait to wait before execution
     * @param priority the priority from {@link Thread#MIN_PRIORITY} to {@link Thread#MAX_PRIORITY}
     * @return the task to control the request
     *
     * @deprecated Sharing of one singlethreaded <CODE>RequestProcessor</CODE>
     * among different users and posting even blocking requests is inherently
     * deadlock-prone. See {@link RequestProcessor use cases}. */
    @Deprecated
    public static Task postRequest(final Runnable run, int timeToWait, int priority) {
        return DEFAULT.post(run, timeToWait, priority);
    }

    /** Creates request that can be later started by setting its delay.
     * The request is not immediately put into the queue. It is planned after
     * setting its delay by setDelay method.
     * @param run action to run in the process
     * @return the task to control execution of given action
     *
     * @deprecated Sharing of one singlethreaded <CODE>RequestProcessor</CODE>
     * among different users and posting even blocking requests is inherently
     * deadlock-prone. See {@link RequestProcessor use cases}. */
    @Deprecated
    public static Task createRequest(Runnable run) {
        return DEFAULT.create(run);
    }

    /** Logger for the error manager.
     */
    static Logger logger() {
        return logger;
    }

    //------------------------------------------------------------------------------
    // The pending queue management implementation
    //------------------------------------------------------------------------------

    /** Place the Task to the queue of pending tasks for immediate processing.
     * If there is no other Task planned, this task is immediately processed
     * in the Processor.
     */
    void enqueue(Item item) {
        Logger em = logger();
        boolean loggable = em.isLoggable(Level.FINE);
        boolean wasNull;
        
        synchronized (processorLock) {
            wasNull = item.getTask() == null;
            if (!wasNull) {
                prioritizedEnqueue(item);

                if (processors.size() < throughput) {
                    Processor proc = Processor.get();
                    processors.add(proc);
                    if (proc.getContextClassLoader() != item.ctxLoader) {
                        if (loggable) {
                            // item classloader may be null, if the item was posted from the Finalizer thread
                            ClassLoader itemLoader = item.ctxLoader;
                            ClassLoader procLoader = proc.getContextClassLoader();
                            em.log(Level.FINE, "Setting ctxLoader for old:{0} loader:{1}#{2} new:{3} loader:{4}#{5}",
                                new Object[] {
                                 proc.getName(),
                                 procLoader == null ? "<none>" : procLoader.getClass().getName(),
                                 procLoader == null ? "-" : Integer.toHexString(System.identityHashCode(proc.getContextClassLoader())),
                                 name,
                                 itemLoader == null ? "<none>" : item.ctxLoader.getClass().getName(),
                                 itemLoader == null ? "-" : Integer.toHexString(System.identityHashCode(item.ctxLoader))
                                }
                            );
                        }
                        proc.setContextClassLoader(item.ctxLoader);
                    }
                    proc.setName(name);
                    proc.attachTo(this);
                }
            }
        }
        if (loggable) {
            if (wasNull) {
                em.log(Level.FINE, "Null task for item {0}", item); // NOI18N
            } else {
                em.log(Level.FINE, "Item enqueued: {0} status: {1}", new Object[]{item.action, item.enqueued}); // NOI18N
            }
        }
    }

    // call it under queue lock i.e. processorLock
    private void prioritizedEnqueue(Item item) {
        getQueue().add(item);
        item.enqueued = true;
    }

    Task askForWork(Processor worker, String debug, Lookup[] lkp) {
        if (getQueue().isEmpty() || (stopped && !finishAwaitingTasks)) { // no more work in this burst, return him
            processors.remove(worker);
            Processor.put(worker, debug);
            return null;
        } else { // we have some work for the worker, pass it

            Item i = getQueue().first();
            getQueue().remove(i);
            Task t = i.getTask();
            lkp[0] = i.current;
            i.clear(worker);

            return t;
        }
    }

    /**
     * {@inheritDoc}
     * @throws IllegalStateException if called on the
     * {@linkplain #getDefault default request processor}
     * @since org.openide.util 8.2
     */
    @Override
    public void shutdown() {
        if (this == UNLIMITED) {
            throw new IllegalStateException ("Cannot shut down the default " + //NOI18N
                    "request processor"); //NOI18N
        }
        stopped = true;
        finishAwaitingTasks = true;
    }

    /**
     * {@inheritDoc}
     * @throws IllegalStateException if called on the
     * {@linkplain #getDefault default request processor}
     * @since org.openide.util 8.2
     */
    @Override
    public List<Runnable> shutdownNow() {
        if (this == UNLIMITED) {
            throw new IllegalStateException ("Cannot shut down the default " + //NOI18N
                    "request processor"); //NOI18N
        }
        //XXX more aggressive shutdown?
        stop();
        synchronized (processorLock) {
            List<Runnable> result = new ArrayList<Runnable>(getQueue().size());
            for (Item item : getQueue()) {
                Task task = item.getTask();
                if (task != null && task.run != null) {
                    Runnable r = task.run;
                    if (r instanceof RunnableWrapper) {
                        Runnable other = ((RunnableWrapper) r).getRunnable();
                        r = other == null ? r : other;
                    }
                    result.add(r);
                }
            }
            return result;
        }
    }

    /**
     * {@inheritDoc}
     * @since org.openide.util 8.2
     */
    @Override
    public boolean isShutdown() {
        return stopped;
    }

    /**
     * {@inheritDoc}
     * @since org.openide.util 8.2
     */
    @Override
    public boolean isTerminated() {
        boolean result = true;
        Set<Processor> set = collectProcessors(new HashSet<Processor>());
        for (Processor p : set) {
            if (p.isAlive() && p.belongsTo(this)) {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * {@inheritDoc} 
     * @since org.openide.util 8.2
     */
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        Parameters.notNull("unit", unit); //NOI18N
        long timeoutMillis = TimeUnit.MILLISECONDS.convert(timeout, unit);
        boolean result = stopped;
        long doneTime = System.currentTimeMillis() + timeoutMillis;
        Set<Processor> procs = new HashSet<Processor>();
outer:  do {
            procs = collectProcessors(procs);
            if (procs.isEmpty()) {
                return true;
            }
            for (Processor p : procs) {
                long remaining = doneTime - System.currentTimeMillis();
                if (remaining <= 0) {
                    result = collectProcessors(procs).isEmpty();
                    break outer;
                }
                if (p.belongsTo(this)) {
                    p.join(remaining);
                }
                result = !p.isAlive() || !p.belongsTo(this);
            }
            procs.clear();
        } while (!procs.isEmpty());
        return result;
    }

    private Set<Processor> collectProcessors (Set<Processor> procs) {
        procs.clear();
        synchronized (processorLock) {
            for (Processor p : processors) {
                if (p.belongsTo(this)) {
                    procs.add(p);
                }
            }
        }
        return procs;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Note:</b> If the passed {@link java.util.concurrent.Callable} implements
     * {@link org.openide.util.Cancellable}, then that object's {@link org.openide.util.Cancellable#cancel()}
     * method will be called if {@link java.util.concurrent.Future#cancel(boolean)} is invoked.
     * If <code>Cancellable.cancel()</code> returns false, then <i>the job will <u>not</u> be
     * cancelled</i>.
     * @since org.openide.util 8.2
     */
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        Parameters.notNull("task", task); //NOI18N
        if (stopped) {
            throw new RejectedExecutionException("Request Processor already " + //NOI18N
                    "stopped"); //NOI18N
        }
        RPFutureTask<T> result = new RPFutureTask<T>(task);
        Task t = create(result);
        result.setTask(t);
        t.schedule(0);
        return result;
    }
    /**
     * {@inheritDoc}
     * <b>Note:</b> If the passed {@link java.lang.Runnable} implements
     * {@link org.openide.util.Cancellable}, then that object's {@link org.openide.util.Cancellable#cancel()}
     * method will be called if {@link java.util.concurrent.Future#cancel(boolean)} is invoked.
     * If <code>Cancellable.cancel()</code> returns false, then <i>the job will <u>not</u> be
     * cancelled</i>.
     * @since org.openide.util 8.2
     */
    @Override
    public <T> Future<T> submit(Runnable task, T predefinedResult) {
        Parameters.notNull("task", task); //NOI18N
        if (stopped) {
            throw new RejectedExecutionException("Request Processor already " + //NOI18N
                    "stopped"); //NOI18N
        }
        RPFutureTask<T> result = new RPFutureTask<T>(task, predefinedResult);
        Task t = create(result);
        result.setTask(t);
        t.schedule(0);
        return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Note:</b> If the passed {@link java.lang.Runnable} implements
     * {@link org.openide.util.Cancellable}, then that object's {@link org.openide.util.Cancellable#cancel()}
     * method will be called if {@link java.util.concurrent.Future#cancel(boolean)} is invoked.
     * If <code>Cancellable.cancel()</code> returns false, then <i>the job will <u>not</u> be
     * cancelled</i>.
     * @since org.openide.util 8.2
     */
    @Override
    public Future<?> submit(Runnable task) {
        return this.<Void>submit (task, null);
    }

    /**
     * {@inheritDoc}
     * @since org.openide.util 8.2
     */
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        Parameters.notNull("tasks", tasks); //NOI18N
        List<Future<T>> result = new ArrayList<Future<T>>(tasks.size());
        CountDownLatch wait = new CountDownLatch(tasks.size());
        for (Callable<T> c : tasks) {
            if (c == null) {
                    throw new NullPointerException ("Contains null tasks: " +  //NOI18N
                            tasks);
            }
            Callable<T> delegate = new WaitableCallable<T>(c, wait);
            result.add (submit(delegate));
        }
        wait.await();
        return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Executes the given tasks, returning a list of Futures holding their
     * status and results when all complete or the timeout expires, whichever
     * happens first.
     * @since org.openide.util 8.2
     */
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        Parameters.notNull("unit", unit); //NOI18N
        Parameters.notNull("tasks", tasks); //NOI18N
        CountDownLatch wait = new CountDownLatch(tasks.size());
        List<Future<T>> result = new ArrayList<Future<T>>(tasks.size());
        for (Callable<T> c : tasks) {
            if (c == null) {
                throw new NullPointerException ("Contains null tasks: " + tasks); //NOI18N
            }
            Callable<T> delegate = new WaitableCallable<T>(c, wait);
            result.add (submit(delegate));
        }
        if (!wait.await(timeout, unit)) {
            for (Future<T> f : result) {
                RPFutureTask<?> ft = (RPFutureTask<?>) f;
                ft.cancel(true);
            }
        }
        return result;
    }
    /**
     * {@inheritDoc}
     * <p>
     * Executes the given tasks, returning the result of one which has
     * completed and cancelling any incomplete tasks.
     * @since org.openide.util 8.2
     */
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        Parameters.notNull("tasks", tasks); //NOI18N
        CountDownLatch wait = new CountDownLatch(1);
        List<Future<T>> result = new ArrayList<Future<T>>(tasks.size());
        AtomicReference<T> ref = new AtomicReference<T>();
        try {
            for (Callable<T> c : tasks) {
                if (c == null) {
                    throw new NullPointerException ("Contains null tasks: " +  //NOI18N
                            tasks);
                }
                Callable<T> delegate = new WaitableCallable<T>(c, ref, wait);
                result.add (submit(delegate));
            }
            wait.await();
        } finally {
            for (Future<T> f : result) {
                RPFutureTask<?> ft = (RPFutureTask<?>) f;
                ft.cancel(true);
            }
        }
        return ref.get();
    }
    /**
     * {@inheritDoc}
     * <p>
     * Executes the given tasks, returning a list of Futures holding their
     * status and results when all complete or the timeout expires, whichever
     * happens first.
     * @param <T> The result type
     * @param tasks A collection of callables
     * @param timeout The maximum time to wait for completion, in the specified time units
     * @param unit The time unit
     * @return A list of futures
     * @throws InterruptedException if the timeout expires or execution is interrupted
     * @since org.openide.util 8.2
     */
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        Parameters.notNull("unit", unit); //NOI18N
        Parameters.notNull("tasks", tasks); //NOI18N
        CountDownLatch wait = new CountDownLatch(1);
        List<Future<T>> result = new ArrayList<Future<T>>(tasks.size());
        AtomicReference<T> ref = new AtomicReference<T>();
        try {
            for (Callable<T> c : tasks) {
                if (c == null) {
                    throw new NullPointerException ("Contains null tasks: " +  //NOI18N
                            tasks);
                }
                Callable<T> delegate = new WaitableCallable<T>(c, ref, wait);
                result.add (submit(delegate));
            }
            wait.await(timeout, unit);
        } finally {
            for (Future<T> f : result) {
                RPFutureTask<?> ft = (RPFutureTask) f;
                ft.cancel(true);
            }
        }
        return ref.get();
    }

    /**
     * {@inheritDoc}
     * @since org.openide.util 8.2
     */
    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        Parameters.notNull("command", command); //NOI18N
        Parameters.notNull("unit", unit); //NOI18N
        if (delay < 0) {
            throw new IllegalArgumentException ("Negative delay: " + delay);
        }
        if (stopped) {
            throw new RejectedExecutionException("Request Processor already stopped"); //NOI18N
        }
        long delayMillis = TimeUnit.MILLISECONDS.convert(delay, unit);
        ScheduledRPFutureTask<Void> result = new ScheduledRPFutureTask<Void>(command, null, delayMillis);
        Task t = create(result);
        result.setTask(t);
        t.schedule(delayMillis);
        return result;
    }
    /**
     * {@inheritDoc}
     * @since org.openide.util 8.2
     */
    @Override
    public <T> ScheduledFuture<T> schedule(Callable<T> callable, long delay, TimeUnit unit) {
        Parameters.notNull("unit", unit); //NOI18N
        Parameters.notNull("callable", callable); //NOI18N
        if (delay < 0) {
            throw new IllegalArgumentException ("Negative delay: " + delay);
        }
        if (stopped) {
            throw new RejectedExecutionException("Request Processor already " + //NOI18N
                    "stopped"); //NOI18N
        }
        long delayMillis = TimeUnit.MILLISECONDS.convert(delay, unit);
        ScheduledRPFutureTask<T> result = new ScheduledRPFutureTask<T>(callable, delayMillis);
        Task t = create(result);
        result.setTask(t);
        t.schedule(delayMillis);
        return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Schedules a runnable which will run with a given frequency, regardless
     * of how long execution takes, with the exception that if execution takes
     * longer than the specified delay, execution will be delayed but will
     * never be run on two threads concurrently.
     * @since org.openide.util 8.2
     */
    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduleFixed(command, initialDelay, period, unit, false);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Schedules a runnable which will run repeatedly after the specified initial
     * delay, with the specified delay between the completion of one run and
     * the start of the next.
     * @since org.openide.util 8.2
     */
    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return scheduleFixed(command, initialDelay, delay, unit, true);
    }

    private ScheduledFuture<?> scheduleFixed (Runnable command, long initialDelay, long period, TimeUnit unit, boolean fixedDelay) {
        Parameters.notNull("unit", unit); //NOI18N
        Parameters.notNull("command", command); //NOI18N
        if (period < 0) {
            throw new IllegalArgumentException ("Negative delay: " + period); //NOI18N
        }
        if (initialDelay < 0) {
            throw new IllegalArgumentException ("Negative initialDelay: "  //NOI18N
                    + initialDelay);
        }
        if (stopped) {
            throw new RejectedExecutionException("Request Processor already " + //NOI18N
                    "stopped"); //NOI18N
        }
        long initialDelayMillis = TimeUnit.MILLISECONDS.convert(initialDelay, unit);
        long periodMillis = TimeUnit.MILLISECONDS.convert(period, unit);

        TaskFutureWrapper wrap = fixedDelay ? 
            new FixedDelayTask(command, initialDelayMillis, periodMillis) :
            new FixedRateTask(command, initialDelay, periodMillis);
        Task t = create(wrap);
        wrap.t = t;
        t.cancelled = wrap.cancelled;
        t.schedule (initialDelayMillis);

        return wrap;
    }

    private SortedSet<Item> getQueue() {
        assert Thread.holdsLock(processorLock);
        return queue;
    }
    
    /**
     * @return a top level ThreadGroup. The method ensures that even Processors
     * created by internal execution will survive the end of the task.
     */
    private static final TopLevelThreadGroup TOP_GROUP = new TopLevelThreadGroup();
    private static final class TopLevelThreadGroup implements PrivilegedAction<ThreadGroup> {
        public ThreadGroup getTopLevelThreadGroup() {
            ThreadGroup orig = java.security.AccessController.doPrivileged(this);
            ThreadGroup nuova = null;

            try {
                Class<?> appContext = Class.forName("sun.awt.AppContext");
                Method instance = appContext.getMethod("getAppContext");
                Method getTG = appContext.getMethod("getThreadGroup");
                nuova = (ThreadGroup) getTG.invoke(instance.invoke(null));
            } catch (Exception exception) {
                logger().log(Level.FINE, "Cannot access sun.awt.AppContext", exception);
                return orig;
            }

            assert nuova != null;

            if (nuova != orig) {
                logger().log(Level.WARNING, "AppContext group {0} differs from originally used {1}", new Object[]{nuova, orig});
            }
            return nuova;
            
        }
        @Override
        public ThreadGroup run() {
            ThreadGroup current = Thread.currentThread().getThreadGroup();

            while (current.getParent() != null) {
                current = current.getParent();
            }

            return current;
        }
    }

    private abstract static class TaskFutureWrapper implements ScheduledFuture<Void>, Runnable, RunnableWrapper {
        volatile Task t;
        protected final Runnable toRun;
        protected final long initialDelay;
        protected final long period;
        final AtomicBoolean cancelled = new AtomicBoolean();
        TaskFutureWrapper(Runnable run, long initialDelay, long period) {
            this.toRun = run;
            this.initialDelay = initialDelay;
            this.period = period;
        }

        @Override
        public final Runnable getRunnable() {
            return toRun;
        }

        @Override
        public int compareTo(Delayed o) {
            long other = o.getDelay(TimeUnit.MILLISECONDS);
            long ours = getDelay(TimeUnit.MILLISECONDS);
            //Might overflow on, say, ms compared to Long.MAX_VALUE, TimeUnit.DAYS
            return (int) (ours - other);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            boolean result = true;
            if (toRun instanceof Cancellable) {
                result = ((Cancellable) toRun).cancel();
            }
            if (result) {
                //will invoke cancelled.set(true)
                result = t.cancel(mayInterruptIfRunning);
            }
            return result;
        }

        @Override
        public boolean isCancelled() {
            return cancelled.get();
        }

        @Override
        public boolean isDone() {
            return cancelled.get() || t.isFinished();
        }

        @Override
        public Void get() throws InterruptedException, ExecutionException {
            if (cancelled.get()) {
                throw new CancellationException();
            }
            t.waitFinished();
            if (cancelled.get()) {
                throw new CancellationException();
            }
            return null;
        }

        @Override
        public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            if (cancelled.get()) {
                throw new CancellationException();
            }
            long millis = TimeUnit.MILLISECONDS.convert(timeout, unit);
            t.waitFinished(millis);
            if (cancelled.get()) {
                throw new CancellationException();
            }
            return null;
        }
    }

    private static final class FixedRateTask extends TaskFutureWrapper {
        private final Object runLock = new Object();
        private final Object timeLock = new Object();
        //must be accessed holding timeLock
        private int runCount;
        private long nextRunTime;
        private long start = Long.MIN_VALUE;
        volatile boolean firstRun = true;
        FixedRateTask (Runnable run, long initialDelay, long period) {
            super (run, initialDelay, period);
        }

        @Override
        public void run() {
            if (firstRun) {
                synchronized (timeLock) {
                    start = System.currentTimeMillis();
                    firstRun = false;
                }
            }
            try {
                synchronized(runLock) {
                    toRun.run();
                }
            } catch (RuntimeException e) {
                cancel(true);
                throw e;
            }
            reschedule();
        }

        private void reschedule() {
            //All access to nextRunTime & runCount under lock.
            long interval;
            synchronized (timeLock) {
                nextRunTime = start + (initialDelay + period * runCount);
                runCount++;
                interval = Math.max(0,  nextRunTime - System.currentTimeMillis());
            }
            boolean canContinue = !cancelled.get() && !Thread.currentThread().isInterrupted();
            if (canContinue) {
                t.schedule(interval);
            }
        }

        @Override
        public long getDelay(TimeUnit unit) {
            if (isCancelled()) {
                return Long.MAX_VALUE;
            }
            long delay;
            synchronized (timeLock) {
                delay = Math.min(0, nextRunTime - System.currentTimeMillis());
            }
            return unit.convert(delay, TimeUnit.MILLISECONDS);
        }
    }

    private static final class FixedDelayTask extends TaskFutureWrapper {
        private final AtomicLong nextRunTime = new AtomicLong();
        FixedDelayTask(Runnable run, long initialDelay, long period)  {
            super (run, initialDelay, period);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long next = nextRunTime.get();
            return unit.convert (next - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public void run() {
            if (!fini()) {
                toRun.run();
            }
            if (!fini()) {
                reschedule();
            }
        }

        private boolean fini() {
            boolean result = cancelled.get() || Thread.currentThread().isInterrupted();
            return result;
        }

        private void reschedule() {
            nextRunTime.set(System.currentTimeMillis() + period);
            if (!fini()) {
                t.schedule((int) period);
            }
        }
    }

    private interface RunnableWrapper {
        Runnable getRunnable();
    }

    private static final class WaitableCallable<T> implements Callable<T>, Cancellable {
        private final CountDownLatch countdown;
        private final Callable<T> delegate;
        private final AtomicReference<T> ref;
        private volatile boolean failed;
        WaitableCallable(Callable<T> delegate, CountDownLatch countdown) {
            this (delegate, null, countdown);
        }

        WaitableCallable(Callable<T> delegate, AtomicReference<T> ref, CountDownLatch countdown) {
            this.delegate = delegate;
            this.countdown = countdown;
            this.ref = ref;
        }

        boolean failed() {
            return failed;
        }

        @Override
        public T call() throws Exception {
            try {
                T result = delegate.call();
                if (ref != null) {
                    ref.set(result);
                }
                return result;
            } catch (RuntimeException e) {
                failed = true;
                throw e;
            } catch (Error e) {
                failed = true;
                throw e;
            } finally {
                if (!failed || ref == null) {
                    countdown.countDown();
                }
            }
        }

        @Override
        public boolean cancel() {
            return delegate instanceof Cancellable ? ((Cancellable) delegate).cancel() : true;
        }
    }

    private static class RPFutureTask<T> extends FutureTask<T> implements RunnableWrapper {
        protected volatile Task task;
        private final Runnable runnable;
        private final Cancellable cancellable;
        RPFutureTask(Callable<T> c) {
            super (c);
            this.runnable = null;
            this.cancellable = c instanceof Cancellable ? (Cancellable) c : null;
        }

        RPFutureTask(Runnable r, T result) {
            super (r, result);
            this.runnable = r;
            this.cancellable = r instanceof Cancellable ? (Cancellable) r : null;
        }

        void setTask(Task task) {
            this.task = task;
        }

        RPFutureTask(Callable<T> c, T predefinedResult) {
            this (c);
            set(predefinedResult);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            boolean result = cancellable == null ? true : cancellable.cancel();
            if (result) {
                boolean taskCancelled = task.cancel();
                boolean superCancel = super.cancel(mayInterruptIfRunning); //must call both!
                result = taskCancelled && superCancel;
            }
            return result;
        }

        @Override
        public Runnable getRunnable() {
            return this.runnable;
        }
    }

    private static final class ScheduledRPFutureTask<T> extends RPFutureTask<T> implements ScheduledFuture<T> {
        protected final long delayMillis;
        ScheduledRPFutureTask(Callable<T> c, long delayMillis) {
            super (c);
            this.delayMillis = delayMillis;
        }

        ScheduledRPFutureTask(Runnable r, T result, long delayMillis) {
            super (r, result);
            this.delayMillis = delayMillis;
        }

        
        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(delayMillis, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            //Can overflow, if one delay is, say, days, and the other, microseconds
            long otherDelayMillis = o.getDelay(TimeUnit.MILLISECONDS);
            return (int) (delayMillis - otherDelayMillis);
        }
    }

    /**
     * The task describing the request sent to the processor.
     * Cancellable since 4.1.
     */
    public final class Task extends org.openide.util.Task implements Cancellable {
        private Item item;
        private int priority = Thread.MIN_PRIORITY;
        private long time = 0;
        private Thread lastThread = null;
        private AtomicBoolean cancelled;

        /** @param run runnable to start
        */
        Task(Runnable run) {
            super(run);
        }

        /** @param run runnable to start
         * @param priority the priorty of the task
         */
        Task(Runnable run, int priority) {
            super(run);

            if (priority < Thread.MIN_PRIORITY) {
                priority = Thread.MIN_PRIORITY;
            }

            if (priority > Thread.MAX_PRIORITY) {
                priority = Thread.MAX_PRIORITY;
            }

            this.priority = priority;
        }

        @Override
        public void run() {
            try {
                synchronized (Task.class) {
                    while (lastThread != null) {
                        try {
                            Task.class.wait();
                        } catch (InterruptedException ex) {
                            // OK wait again
                        }
                    }
                    lastThread = Thread.currentThread();
                }
                notifyRunning();
                run.run();
            } finally {
                Item scheduled = this.item;
                if (scheduled != null && !scheduled.isNew() && scheduled.getTask() == this) {
                    // do not mark as finished, we are scheduled for future
                } else {
                    notifyFinished();
                }
                synchronized (Task.class) {
                    lastThread = null;
                    Task.class.notifyAll();
                }
            }
        }

        /** Getter for amount of millis till this task
        * is started.
        * @return amount of millis
        */
        public int getDelay() {
            long delay = time - System.currentTimeMillis();

            if (delay < 0L) {
                return 0;
            }

            if (delay > (long) Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }

            return (int) delay;
        }

        /** (Re-)schedules a task to run in the future.
        * If the task has not been run yet, it is postponed to
        * the new time. If it has already run and finished, it is scheduled
        * to be started again. If it is currently running, it is nevertheless
        * left to finish, and also scheduled to run again.
        * @param delay time in milliseconds to wait (starting from now)
        */
        public void schedule(int delay) {
            schedule((long) delay);
        }

        void schedule(long delay) {
            if (stopped) {
                return;
            }

            time = System.currentTimeMillis() + delay;

            final Item localItem;

            synchronized (processorLock) {
                if (cancelled != null) {
                    cancelled.set(false);
                }
                notifyRunning();

                if (item != null) {
                    item.clear(null);
                }

                item = enableStackTraces ?
                    new SlowItem(this, RequestProcessor.this) :
                    new FastItem(this, RequestProcessor.this);
                localItem = item;
            }

            if (delay == 0) { // Place it to pending queue immediatelly
                enqueue(localItem);
            } else { // Post the starter
                TickTac.schedule(localItem, delay);
            }
        }

        private void markCreated() {
            assert item == null;
            item = new CreatedItem(this, null);
        }

        /** Removes the task from the queue.
        *
        * @return true if the task has been removed from the queue,
        *   false it the task has already been processed
        */
        @Override
        public boolean cancel() {
            return cancelOrNew(false);
        }
        
        private boolean cancelOrNew(boolean canBeNew) {
            synchronized (processorLock) {
                boolean success;

                if (item == null) {
                    success = false;
                } else {
                    Processor p = item.getProcessor();
                    success = item.clearOrNew(canBeNew);

                    if (p != null) {
                        p.interruptTask(this, RequestProcessor.this);
                        item = null;
                    }
                    
                    if (success) {
                        item = null;
                    }
                }

                if (success) {
                    notifyFinished(); // mark it as finished
                }

                return success;
            }
        }

        /**
         * Implementation of cancel for use with Future objects, to guarantee
         * that the thread will be interrupted, no matter what the setting
         * on the owning RP.
         * @param interrupt If true, the thread should be interrupted
         * @return true if cancellation occurred
         */
        boolean cancel (boolean interrupt) {
            synchronized (processorLock) {
                if (cancelled != null) {
                    boolean wasCancelled = !cancelled.getAndSet(true);
                    if (wasCancelled) {
                        return false;
                    }
                }
                boolean success;

                if (item == null) {
                    success = false;
                } else {
                    Processor p = item.getProcessor();
                    success = item.clear(null);

                    if (p != null) {
                        if (interrupt) {
                            success = p.interrupt(this, RequestProcessor.this);
                        } else {
                            //despite its name, will not actually interrupt
                            //unless the RP specifies that it should
                            p.interruptTask(this, RequestProcessor.this);
                        }
                        if (success) {
                            item = null;
                        }
                    }
                }
                if (success) {
                    notifyFinished(); // mark it as finished
                }
                return success;
            }
        }

        /** Current priority of the task.
        * @return the priority level (see e.g. {@link Thread#NORM_PRIORITY}
         */
        public int getPriority() {
            return priority;
        }

        /** Changes the priority the task will be performed with. 
         * @param priority the priority level (see e.g. {@link Thread#NORM_PRIORITY}
         */
        public void setPriority(int priority) {
            if (this.priority == priority) {
                return;
            }

            if (priority < Thread.MIN_PRIORITY) {
                priority = Thread.MIN_PRIORITY;
            }

            if (priority > Thread.MAX_PRIORITY) {
                priority = Thread.MAX_PRIORITY;
            }


            // update queue position accordingly
            synchronized (processorLock) {
                if (item != null && getQueue().remove(item)) {
                    this.priority = priority;
                    prioritizedEnqueue(item);
                } else {
                    this.priority = priority;
                }
            }
        }

        /** This method is an implementation of the waitFinished method
        * in the RequestProcessor.Task. It check the current thread if it is
        * request processor thread and in such case runs the task immediatelly
        * to prevent deadlocks.
        */
        @Override
        public void waitFinished() {
            if (isRequestProcessorThread()) { //System.err.println(
                boolean runAtAll;
                boolean toRun;
                
                Logger em = logger();
                boolean loggable = em.isLoggable(Level.FINE);
                
                if (loggable) {
                    em.log(Level.FINE, "Task.waitFinished on {0} from other task in RP: {1}", new Object[]{this, Thread.currentThread().getName()}); // NOI18N
                }
                

                synchronized (processorLock) {
                    // correct line:    toRun = (item == null) ? !isFinished (): (item.clear() && !isFinished ());
                    // the same:        toRun = !isFinished () && (item == null ? true : item.clear ());
                    runAtAll = cancelOrNew(true);
                    toRun = runAtAll && ((item == null) || item.clear(null));
                    if (loggable) {
                        em.log(Level.FINE, "    ## finished: {0}", isFinished()); // NOI18N
                        em.log(Level.FINE, "    ## item: {0}", item); // NOI18N
                    }
                }

                if (toRun) { 
                    if (loggable) {
                        em.fine("    ## running it synchronously"); // NOI18N
                    }
                    Processor processor = (Processor)Thread.currentThread();
                    processor.doEvaluate (this, processorLock, RequestProcessor.this);
                } else { // it is already running in other thread of this RP
                    if (loggable) {
                        em.fine("    ## not running it synchronously"); // NOI18N
                    }

                    if ((runAtAll || lastThread != null) && lastThread != Thread.currentThread()) {
                        if (loggable) {
                            em.log(Level.FINE, "    ## waiting for it to be finished: {0} now: {1}", new Object[]{lastThread, Thread.currentThread()}); // NOI18N
                        }
                        super.waitFinished();
                    }

                    //                    else {
                    //System.err.println("Thread waiting for itself!!!!! - semantics broken!!!");
                    //Thread.dumpStack();
                    //                    }
                }
                if (loggable) {
                    em.fine("    ## exiting waitFinished"); // NOI18N
                }
            } else {
                super.waitFinished();
            }
        }

        /** Enhanced reimplementation of the {@link Task#waitFinished(long)}
        * method. The added semantic is that if one calls this method from
        * another task of the same processor, and the task has not yet been
        * executed, the method will immediatelly detect that and throw
        * <code>InterruptedException</code> to signal that state.
        *
        * @param timeout the amount of time to wait
        * @exception InterruptedException if waiting has been interrupted or if
        *    the wait cannot succeed due to possible deadlock collision
        * @return true if the task was finished successfully during the
        *    timeout period, false otherwise
        *  @since 5.0
        */
        @Override
        public boolean waitFinished(long timeout) throws InterruptedException {
            if (isRequestProcessorThread()) {
                boolean toRun;

                synchronized (processorLock) {
                    toRun = cancelOrNew(true);
                }

                if (toRun) {
                    throw new InterruptedException(
                        "Cannot wait with timeout " + timeout + " from the RequestProcessor thread for task: " + this
                    ); // NOI18N
                } else { // it is already running in other thread of this RP

                    if (lastThread != Thread.currentThread()) {
                        return super.waitFinished(timeout);
                    } else {
                        return true;
                    }
                }
            } else {
                return super.waitFinished(timeout);
            }
        }

        @Override
        public String toString() {
            return "RequestProcessor.Task [" + name + ", " + priority + "] for " + super.toString(); // NOI18N
        }
    }

    /* One item representing the task pending in the pending queue */
    private static class Item extends Exception implements Comparable<Item> {
        private static int counter;
        private final RequestProcessor owner;
        private final int cnt;
        final Lookup current;
        final ClassLoader ctxLoader;
        Object action;
        boolean enqueued;
        String message;
        /** @GuardedBy(TICK) */
        long when;

        Item(Task task, RequestProcessor rp) {
            action = task;
            owner = rp;
            cnt = counter++;
            current = Lookup.getDefault();
            ctxLoader = Thread.currentThread().getContextClassLoader();
        }

        final Task getTask() {
            Object a = action;

            return (a instanceof Task) ? (Task) a : null;
        }
        
        boolean clearOrNew(boolean canBeNew) {
            return clear(null);
        }

        /** Annulate this request iff still possible.
         * @returns true if it was possible to skip this item, false
         * if the item was/is already processed */
        boolean clear(Processor processor) {
            boolean ret;
            synchronized (owner.processorLock) {
                ret = enqueued ? owner.getQueue().remove(this) : true;
                action = processor;
            }
            TickTac.cancel(this);
            return ret;
        }

        boolean isNew() {
            return false;
        }

        final Processor getProcessor() {
            Object a = action;

            return (a instanceof Processor) ? (Processor) a : null;
        }

        final int getPriority() {
            final Task t = getTask();
            return t == null ? 0 : t.getPriority();
        }

        public final @Override String getMessage() {
            return message;
        }

        @Override
        public int compareTo(Item o) {
            if (this == o) {
                return 0;
            }
            int myp = getPriority();
            int yrp = o.getPriority();
            if (myp == yrp) {
                return cnt - o.cnt;
            } else {
                return yrp - myp;
            }
        }
    }
    
    private static class CreatedItem extends Item {
        public CreatedItem(Task task, RequestProcessor rp) {
            super(task, rp);
        }

        @Override
        boolean clearOrNew(boolean canBeNew) {
            return canBeNew;
        }
        
        @Override
        boolean clear(Processor processor) {
            return false;
        }

        @Override
        boolean isNew() {
            return true;
        }
    } // end of CreatedItem

    private static class FastItem extends Item {
        FastItem(Task task, RequestProcessor rp) {
            super(task, rp);
        }

        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }
    private static class SlowItem extends Item {

        SlowItem(Task task, RequestProcessor rp) {
            super(task, rp);
        }

        @Override
        public Throwable fillInStackTrace() {
            Throwable ret = super.fillInStackTrace();
            StackTraceElement[] arr = ret.getStackTrace();
            for (int i = 1; i < arr.length; i++) {
                if (arr[i].getClassName().startsWith("java.lang")) {
                    continue;
                }
                if (arr[i].getClassName().startsWith(RequestProcessor.class.getName())) {
                    continue;
                }
                ret.setStackTrace(Arrays.asList(arr).subList(i - 1, arr.length).toArray(new StackTraceElement[0]));
                break;
            }
            return ret;
        }
    }

    //------------------------------------------------------------------------------
    // The Processor management implementation
    //------------------------------------------------------------------------------

    /**
    /** A special thread that processes timouted Tasks from a RequestProcessor.
     * It uses the RequestProcessor as a synchronized queue (a Channel),
     * so it is possible to run more Processors in paralel for one RequestProcessor
     */
    private static class Processor extends Thread {
        /** A stack containing all the inactive Processors */
        private static final Deque<Processor> POOL = new ArrayDeque<>();

        /* One minute of inactivity and the Thread will die if not assigned */
        private static final int INACTIVE_TIMEOUT = Integer.getInteger("org.openide.util.RequestProcessor.inactiveTime", 60000); // NOI18N

        /** Internal variable holding the Runnable to be run.
         * Used for passing Runnable through Thread boundaries.
         */

        //private Item task;
        private RequestProcessor source;

        /** task we are working on */
        private RequestProcessor.Task todo;
        private boolean idle = true;

        /** Waiting lock */
        private final Object lock = new Object();
        private RequestProcessor procesing;

        public Processor() {
            super(TOP_GROUP.getTopLevelThreadGroup(), "Inactive RequestProcessor thread"); // NOI18N
            setDaemon(true);
            assert !Thread.holdsLock(POOL); // new Thread may lead to huge classloading
        }

        /** Provide an inactive Processor instance. It will return either
         * existing inactive processor from the pool or will create a new instance
         * if no instance is in the pool.
         *
         * @return inactive Processor
         */
        static Processor get() {
            Processor newP = null;
            for (;;) {
                synchronized (POOL) {
                    if (POOL.isEmpty()) {
                        if (newP != null) {
                            Processor proc = newP;
                            proc.idle = false;
                            proc.start();

                            return proc;
                        }
                    } else {
                        assert checkAccess(TOP_GROUP.getTopLevelThreadGroup());
                        Processor proc = POOL.pop();
                        proc.idle = false;

                        return proc;
                    }
                }
                newP = new Processor();
            }
        }
        private static boolean checkAccess(ThreadGroup g) throws SecurityException {
            g.checkAccess();
            return true;
        }

        /** A way of returning a Processor to the inactive pool.
         *
         * @param proc the Processor to return to the pool. It shall be inactive.
         * @param last the debugging string identifying the last client.
         */
        static void put(Processor proc, String last) {
            synchronized (POOL) {
                proc.setName("Inactive RequestProcessor thread [Was:" + proc.getName() + "/" + last + "]"); // NOI18N
                proc.idle = true;
                POOL.push(proc);
            }
        }

        /** setPriority wrapper that skips setting the same priority
         * we'return already running at */
        void setPrio(int priority) {
            if (priority != getPriority()) {
                setPriority(priority);
            }
        }

        /**
         * Sets an Item to be performed and notifies the performing Thread
         * to start the processing.
         *
         * @param r the Item to run.
         */
        public void attachTo(RequestProcessor src) {
            synchronized (lock) {
                //assert(source == null);
                source = src;
                lock.notify();
            }
        }

        boolean belongsTo(RequestProcessor r) {
            synchronized (lock) {
                return source == r;
            }
        }

        /**
         * The method that will repeatedly wait for a request and perform it.
         */
        @Override
        public void run() {
            for (;;) {
                RequestProcessor current = null;

                synchronized (lock) {
                    try {
                        if (source == null) {
                            lock.wait(INACTIVE_TIMEOUT); // wait for the job
                        }
                    } catch (InterruptedException e) {
                    }
                     // not interesting

                    current = source;
                    source = null;

                    if (current == null) { // We've timeouted

                        synchronized (POOL) {
                            if (idle) { // and we're idle
                                POOL.remove(this);

                                break; // exit the thread
                            } else { // this will happen if we've been just

                                continue; // before timeout when we were assigned
                            }
                        }
                    }
                }

                String debug = null;

                Logger em = logger();
                boolean loggable = em.isLoggable(Level.FINE);

                if (loggable) {
                    try {
                        procesing = current;
                        em.log(Level.FINE, "Begining work {0}", getName()); // NOI18N
                    } finally {
                        procesing = null;
                    }
                }

                // while we have something to do
                for (;;) {
                    Lookup[] lkp = new Lookup[1];
                    // need the same sync as interruptTask
                    synchronized (current.processorLock) {
                        todo = current.askForWork(this, debug, lkp);
                        if (todo == null) {
                            break;
                        }
                    }
                    setPrio(todo.getPriority());

                    try {
                        procesing = current;
                        if (loggable) {
                            em.log(Level.FINE, "  Executing {0}", todo); // NOI18N
                        }
                        registerParallel(todo, current);
                        Lookups.executeWith(lkp[0], todo);
                        lkp[0] = null;

                        if (loggable) {
                            em.log(Level.FINE, "  Execution finished in {0}", getName()); // NOI18N
                        }

                        debug = todo.debug();
                    } catch (OutOfMemoryError oome) {
                        // direct notification, there may be no room for
                        // annotations and we need OOME to be processed
                        // for debugging hooks
                        em.log(Level.SEVERE, null, oome);
                    } catch (StackOverflowError e) {
                        // recoverable too
                        doNotify(todo, e);
                    } catch (ThreadDeath t) {
                        // #201098: ignore
                    } catch (Throwable t) {
                        doNotify(todo, t);
                    } finally {
                        procesing = null;
                        unregisterParallel(todo, current);
                    }

                    // need the same sync as interruptTask
                    synchronized (current.processorLock) {
                        // to improve GC
                        todo = null;
                        // and to clear any possible interrupted state
                        // set by calling Task.cancel ()
                        Thread.interrupted();
                    }
                }

                if (loggable) {
                    try {
                        procesing = current;
                        em.log(Level.FINE, "Work finished {0}", getName()); // NOI18N
                    } finally {
                        procesing = null;
                    }
                }
            }
        }
        
        /** Evaluates given task directly.
         */
        final void doEvaluate (Task t, Object processorLock, RequestProcessor src) {
            Task previous = todo;
            boolean interrupted = Thread.interrupted();
            try {
                todo = t;
                t.run ();
            } finally {
                synchronized (processorLock) {
                    todo = previous;
                    if (interrupted || todo.item == null) {
                        if (src.interruptThread) {
                            // reinterrupt the thread if it was interrupted and
                            // we support interrupts
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        }

        /** Called under the processorLock */
        public void interruptTask(Task t, RequestProcessor src) {
            if (t != todo) {
                // not running this task so
                return;
            }
            
            if (src.interruptThread) {
                // otherwise interrupt this thread
                interrupt();
            }
        }

        boolean interrupt (Task t, RequestProcessor src) {
            if (t != todo) {
                return false;
            }
            interrupt();
            return true;
        }

        /** See #20467. */
        private static void doNotify(RequestProcessor.Task todo, Throwable ex) {
            if (SLOW) {
                Item item = todo.item;
                if (item != null && item.message == null) {
                    item.message = ex.toString();
                    item.initCause(ex);
                    ex = item;
                }
            }
            logger().log(Level.SEVERE, "Error in RequestProcessor " + todo.debug(), ex);
        }

        private static final Map<Class<? extends Runnable>,Object> warnedClasses = Collections.synchronizedMap(
            new WeakHashMap<Class<? extends Runnable>,Object>()
        );
        private void registerParallel(Task todo, RequestProcessor rp) {
            if (rp.warnParallel == 0 || todo.run == null) {
                return;
            }
            final Class<? extends Runnable> c = todo.run.getClass();
            AtomicInteger number;
            synchronized (rp.processorLock) {
                if (rp.inParallel == null) {
                    rp.inParallel = new WeakHashMap<Class<? extends Runnable>,AtomicInteger>();
                }
                number = rp.inParallel.get(c);
                if (number == null) {
                    rp.inParallel.put(c, number = new AtomicInteger(1));
                } else {
                    number.incrementAndGet();
                }
            }
            if (number.get() >= rp.warnParallel && warnedClasses.put(c, "") == null) {
                final String msg = "Too many " + c.getName() + " (" + number + ") in shared RequestProcessor; create your own"; // NOI18N
                Exception ex = null;
                Item itm = todo.item;
                if (itm != null) {
                    ex = new IllegalStateException(msg);
                    ex.setStackTrace(itm.getStackTrace());
                }
                logger().log(Level.WARNING, msg, ex);
            }
        }

        private void unregisterParallel(Task todo, RequestProcessor rp) {
            if (rp.warnParallel == 0 || todo.run == null) {
                return;
            }
            synchronized (rp.processorLock) {
                Class<? extends Runnable> c = todo.run.getClass();
                rp.inParallel.get(c).decrementAndGet();
            }
        }
    }
    
    private static final class TickTac extends Thread implements Comparator<Item> {
        private static TickTac TICK;
        private final PriorityQueue<Item> queue;
        
        public TickTac() {
            super("RequestProcessor queue manager"); // NOI18N
            setDaemon(true);
            queue = new PriorityQueue<Item>(128, this);
        }

        @Override
        public int compare(Item o1, Item o2) {
            if (o1.when < o2.when) {
                return -1;
            }
            if (o1.when > o2.when) {
                return 1;
            }
            return 0;
        }

        static final synchronized void schedule(Item localItem, long delay) {
            if (TICK == null) {
                TICK = new TickTac();
                TICK.scheduleImpl(localItem, delay);
                TICK.start();
            } else {
                TICK.scheduleImpl(localItem, delay);
            }
            TickTac.class.notifyAll();
        }
        
        private void scheduleImpl(Item localItem, long delay) {
            assert Thread.holdsLock(TickTac.class);
            
            localItem.when = System.currentTimeMillis() + delay;
            queue.add(localItem);
        }
        
        static final synchronized void cancel(Item localItem) {
            if (TICK != null) {
                TICK.cancelImpl(localItem);
                TickTac.class.notifyAll();
            }
        }
        
        private void cancelImpl(Item localItem) {
            assert Thread.holdsLock(TickTac.class);
            queue.remove(localItem);
        }

        @Override
        public void run() {
            while (TICK == this) {
                try {
                    Item first = obtainFirst();
                    if (first != null) {
                        first.owner.enqueue(first);
                    }
                } catch (InterruptedException ex) {
                    continue;
                }
            }
            
        }
        
        private static synchronized Item obtainFirst() throws InterruptedException {
            if (TICK == null) {
                return null;
            }
            PriorityQueue<Item> q = TICK.queue;
            Item first = q.poll();
            if (first == null) {
                TICK = null;
                return null;
            }
            long delay = first.when - System.currentTimeMillis();
            if (delay > 0) {
                q.add(first);
                TickTac.class.wait(delay);
                return null;
            }
            return first;
        }
    }
}
