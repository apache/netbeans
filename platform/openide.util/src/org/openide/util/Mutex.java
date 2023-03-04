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

package org.openide.util;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import org.openide.util.MutexException;
import org.netbeans.modules.openide.util.DefaultMutexImplementation;
import org.netbeans.modules.openide.util.LazyMutexImplementation;
import org.openide.util.lookup.Lookups;
import org.openide.util.spi.MutexEventProvider;
import org.openide.util.spi.MutexImplementation;

/** Read-many/write-one lock.
* Allows control over resources that
* can be read by several readers at once but only written by one writer.
* <P>
* It is guaranteed that if you are a writer you can also enter the
* mutex as a reader. Conversely, if you are the <em>only</em> reader you
* can enter the mutex as a writer, but you'll be warned because it is very
* deadlock prone (two readers trying to get write access concurently).
* <P>
* If the mutex is used only by one thread, the thread can repeatedly
* enter it as a writer or reader. So one thread can never deadlock itself,
* whichever order operations are performed in.
* <P>
* There is no strategy to prevent starvation.
* Even if there is a writer waiting to enter, another reader might enter
* the section instead.
* <P>
* Examples of use:
*
* <pre>
* Mutex m = new Mutex();
*
* // Grant write access, compute an integer and return it:
* return m.writeAccess(new Mutex.Action&lt;Integer&gt;(){
*     public Integer run() {
*         return 1;
*     }
* });
*
* // Obtain read access, do some computation,
* // possibly throw an IOException:
* try {
*     m.readAccess(new Mutex.ExceptionAction&lt;Void&gt;() {
*         public Void run() throws IOException {
*             if (...) throw new IOException();
*             return null;
*         }
*     });
* } catch (MutexException ex) {
*     throw (IOException) ex.getException();
* }
*
* // check whether you are already in read access
* if (m.isReadAccess()) {
*     // do your work
* }
* </pre>
*
* @author Ales Novak
*/
public final class Mutex {
    /**
     * The actual delegate, which performs the work
     */
    private final MutexImplementation impl;
    
    /** logger for things that happen in mutex */
    private static final Logger LOG = Logger.getLogger(Mutex.class.getName());

    /** Mutex that allows code to be synchronized with the AWT event dispatch thread.
     * <P>
     * When the Mutex methods are invoked on this mutex, the methods' semantics 
     * change as follows:
     * <UL>
     * <LI>The {@link #isReadAccess} and {@link #isWriteAccess} methods
     *  return <code>true</code> if the current thread is the event dispatch thread
     *  and false otherwise.
     * <LI>The {@link #postReadRequest} and {@link #postWriteRequest} methods
     *  asynchronously execute the {@link java.lang.Runnable} passed in their 
     *  <code>run</code> parameter on the event dispatch thead.
     * <LI>The {@link #readAccess(java.lang.Runnable)} and 
     *  {@link #writeAccess(java.lang.Runnable)} methods asynchronously execute the 
     *  {@link java.lang.Runnable} passed in their <code>run</code> parameter 
     *  on the event dispatch thread, unless the current thread is 
     *  the event dispatch thread, in which case 
     *  <code>run.run()</code> is immediately executed.
     * <LI>The {@link #readAccess(Mutex.Action)},
     *  {@link #readAccess(Mutex.ExceptionAction action)},
     *  {@link #writeAccess(Mutex.Action action)} and
     *  {@link #writeAccess(Mutex.ExceptionAction action)} 
     *  methods synchronously execute the {@link Mutex.ExceptionAction}
     *  passed in their <code>action</code> parameter on the event dispatch thread,
     *  unless the current thread is the event dispatch thread, in which case
     *  <code>action.run()</code> is immediately executed.
     * </UL>
     * <p>
     * Since version 9.18 the methods of the {@code EVENT} instance properly
     * understand semantics of {@link Lookups#executeWith(org.openide.util.Lookup, java.lang.Runnable)}
     * method and propagate the effective {@link Lookup} to the event dispatch
     * thread.
     */
    public static final Mutex EVENT;
    static {
        final Callable<MutexImplementation> c = new Callable<MutexImplementation>() {
            @Override
            public MutexImplementation call() throws Exception {
                final MutexEventProvider provider = Lookup.getDefault().lookup(MutexEventProvider.class);
                if (provider == null) {
                    throw new IllegalStateException("No MutexEventProvider found in default Lookup.");  //NOI18N
                }
                final MutexImplementation mutexImpl = provider.createMutex();
                if (mutexImpl == null) {
                    throw new IllegalStateException(String.format(
                        "Null value from %s.createMutex()", //NOI18N
                        provider.getClass()));
                }
                return mutexImpl;
            }
        };
        EVENT = new Mutex(new LazyMutexImplementation(c));
    }

    // lock mode constants

    /**
     * Creates {@link Mutex} with given SPI.
     * @param impl the {@link Mutex} SPI.
     * @since 9.1
     */
    public Mutex(MutexImplementation impl) {
        Parameters.notNull("impl", impl);  //NOI18N
        this.impl = impl;
    }

    public Mutex(Object lock) {
        this(DefaultMutexImplementation.usingLock(lock));
    }

    /** Default constructor.
    */
    public Mutex() {
        this(DefaultMutexImplementation.create());
    }

    /** @param privileged can enter privileged states of this Mutex
     * This helps avoid creating of custom Runnables.
     */
    public Mutex(Privileged privileged) {
        this.impl = DefaultMutexImplementation.controlledBy(privileged.delegate);
    }

    /** Constructor for those who wish to do some custom additional tasks
     * whenever an action or runnable is executed in the {@link Mutex}. This
     * may be useful for wrapping all the actions with custom {@link ThreadLocal}
     * value, etc. Just implement the {@link Executor}'s <code>execute(Runnable)</code>
     * method and do pre and post initialization tasks before running the runnable.
     * <p>
     * The {@link Executor#execute} method shall return only when the passed in
     * {@link Runnable} is finished, otherwise methods like {@link Mutex#readAccess(Action)} and co.
     * might not return proper result.
     * 
     * @param privileged can enter privileged states of this Mutex
     *  @param executor allows to wrap the work of the mutex with a custom code
     * @since 7.12
     */
    public Mutex(Privileged privileged, Executor executor) {
        this.impl = DefaultMutexImplementation.controlledBy(privileged.delegate, executor);
    }

    /** Run an action only with read access.
    * See class description re. entering for write access within the dynamic scope.
    * @param <T> type of action
    * @param action the action to perform
    * @return the object returned from {@link Mutex.Action#run}
    */
    public <T> T readAccess(final Action<T> action) {        
        try {
            return impl.readAccess(action);
        } catch (MutexException ex) {
            throw (InternalError) new InternalError("Exception from non-Exception Action").initCause(ex.getException()); // NOI18N
        }
    }
    

    /** Run an action with read access and possibly throw a checked exception.
    * The exception if thrown is then encapsulated
    * in a <code>MutexException</code> and thrown from this method. One is encouraged
    * to catch <code>MutexException</code>, obtain the inner exception, and rethrow it.
    * Here is an example:
    * <PRE><code>
    * try {
    *   mutex.readAccess (new ExceptionAction () {
    *     public void run () throws IOException {
    *       throw new IOException ();
    *     }
    *   });
    *  } catch (MutexException ex) {
    *    throw (IOException) ex.getException ();
    *  }
    * </code></PRE>
    * Note that <em>runtime exceptions</em> are always passed through, and neither
    * require this invocation style, nor are encapsulated.
    * @param <T> type of action
    * @param action the action to execute
    * @return the object returned from {@link Mutex.ExceptionAction#run}
    * @exception MutexException encapsulates a user exception
    * @exception RuntimeException if any runtime exception is thrown from the run method
    * @see #readAccess(Mutex.Action)
    */
    public <T> T readAccess(final ExceptionAction<T> action) throws MutexException {        
        return impl.readAccess(action);
    }

    /** Run an action with read access, returning no result.
    * It may be run asynchronously.
    *
    * @param action the action to perform
    * @see #readAccess(Mutex.Action)
    */
    public void readAccess(final Runnable action) {        
        impl.readAccess(action);
    }

    /** Run an action with write access.
    * The same thread may meanwhile reenter the mutex; see the class description for details.
    * @param <T> type of action
    * @param action the action to perform
    * @return the result of {@link Mutex.Action#run}
    */
    public <T> T writeAccess(Action<T> action) {        
        try {
            return impl.writeAccess(action);
        } catch (MutexException ex) {
            throw (InternalError) new InternalError("Exception from non-Exception Action").initCause(ex.getException()); // NOI18N
        }
    }

    /** Run an action with write access and possibly throw an exception.
    * Here is an example:
    * <PRE><code>
    * try {
    *   mutex.writeAccess (new ExceptionAction () {
    *     public void run () throws IOException {
    *       throw new IOException ();
    *     }
    *   });
    *  } catch (MutexException ex) {
    *    throw (IOException) ex.getException ();
    *  }
    * </code></PRE>
    * @param <T> type of action
    * @param action the action to execute
    * @return the result of {@link Mutex.ExceptionAction#run}
    * @exception MutexException an encapsulated checked exception, if any
    * @exception RuntimeException if a runtime exception is thrown in the action
    * @see #writeAccess(Mutex.Action)
    * @see #readAccess(Mutex.ExceptionAction)
    */
    public <T> T writeAccess(ExceptionAction<T> action) throws MutexException {
        return impl.writeAccess(action);
    }

    /** Run an action with write access and return no result.
    * It may be run asynchronously.
    *
    * @param action the action to perform
    * @see #writeAccess(Mutex.Action)
    * @see #readAccess(Runnable)
    */
    public void writeAccess(final Runnable action) {        
        impl.writeAccess(action);
    }

    /** Tests whether this thread has already entered the mutex in read access.
     * If it returns true, calling <code>readAccess</code>
     * will be executed immediatelly
     * without any blocking.
     * Calling <code>postWriteAccess</code> will delay the execution
     * of its <code>Runnable</code> until a readAccess section is over
     * and calling <code>writeAccess</code> is strongly prohibited and will
     * result in a warning as a deadlock prone behaviour.
     * <p><strong>Warning:</strong> since a thread with write access automatically
     * has effective read access as well (whether or not explicitly requested), if
     * you want to check whether a thread can read some data, you should check for
     * either kind of access, e.g.:
     * <pre>assert myMutex.isReadAccess() || myMutex.isWriteAccess();</pre>
     *
     * @return true if the thread is in read access section
     * @since 4.48
     */
    public boolean isReadAccess() {        
        return impl.isReadAccess();
    }

    /** Tests whether this thread has already entered the mutex in write access.
     * If it returns true, calling <code>writeAccess</code> will be executed
     * immediatelly without any other blocking. <code>postReadAccess</code>
     * will be delayed until a write access runnable is over.
     *
     * @return true if the thread is in write access section
     * @since 4.48
     */
    public boolean isWriteAccess() {        
        return impl.isWriteAccess();
    }

    /** toString */
    @Override
    public String toString() {        
        return String.format(
            "Mutex[%s]",    //NOI18N
            impl.toString());
    }

    // priv methods  -----------------------------------------

    /** Posts a read request. This request runs immediately iff
     * this SimpleMutex is in the shared mode or this SimpleMutex is not contended
     * at all.
     *
     * This request is delayed if this SimpleMutex is in the exclusive
     * mode and is held by this thread, until the exclusive is left.
     *
     * Finally, this request blocks, if this SimpleMutex is in the exclusive
     * mode and is held by another thread.
     *
     * <p><strong>Warning:</strong> this method blocks.</p>
     *
     * @param run runnable to run
     */
    public void postReadRequest(final Runnable run) {        
        impl.postReadRequest(run);
    }

    /** Posts a write request. This request runs immediately iff
     * this SimpleMutex is in the "pure" exclusive mode, i.e. this SimpleMutex
     * is not reentered in shared mode after the exclusive mode
     * was acquired. Otherwise it is delayed until all read requests
     * are executed.
     *
     * This request runs immediately if this SimpleMutex is not contended at all.
     *
     * This request blocks if this SimpleMutex is in the shared mode.
     *
     * <p><strong>Warning:</strong> this method blocks.</p>
     * @param run runnable to run
     */
    public void postWriteRequest(Runnable run) {        
        impl.postWriteRequest(run);
    }
    /** Action to be executed in a mutex without throwing any checked exceptions.
    * Unchecked exceptions will be propagated to calling code.
    * @param <T> the type of object to return
    */
    @SuppressWarnings("PublicInnerClass")
    public interface Action<T> extends ExceptionAction<T> {
        /** Execute the action.
        * @return any object, then returned from {@link Mutex#readAccess(Mutex.Action)} or {@link Mutex#writeAccess(Mutex.Action)}
        */
        @Override
        T run();
    }

    /** Action to be executed in a mutex, possibly throwing checked exceptions.
    * May throw a checked exception, in which case calling
    * code should catch the encapsulating exception and rethrow the
    * real one.
    * Unchecked exceptions will be propagated to calling code without encapsulation.
    * @param <T> the type of object to return
    */
    @SuppressWarnings("PublicInnerClass")
    public interface ExceptionAction<T> {
        /** Execute the action.
        * Can throw an exception.
        * @return any object, then returned from {@link Mutex#readAccess(Mutex.ExceptionAction)} or {@link Mutex#writeAccess(Mutex.ExceptionAction)}
        * @exception Exception any exception the body needs to throw
        */
        T run() throws Exception;
    }

    /** Provides access to Mutex's internal methods.
     *
     * This class can be used when one wants to avoid creating a
     * bunch of Runnables. Instead,
     * <pre>
     * try {
     *     enterXAccess ();
     *     yourCustomMethod ();
     * } finally {
     *     exitXAccess ();
     * }
     * </pre>
     * can be used.
     *
     * You must, however, control the related Mutex, i.e. you must be creator of
     * the Mutex.
     *
     * @since 1.17
     */
    public static final class Privileged {
        private final DefaultMutexImplementation.Privileged delegate;
        
        public Privileged() {
            this.delegate = new DefaultMutexImplementation.Privileged();
        }

        public void enterReadAccess() {
            delegate.enterReadAccess();
        }
        
        /** Tries to obtain read access. If the access cannot by
         * gained by given milliseconds, the method returns without gaining
         * it.
         * 
         * @param timeout amount of milliseconds to wait before giving up.
         *   <code>0</code> means to wait indefinitely.
         *   <code>-1</code> means to not wait at all and immediately exit
         * @return <code>true</code> if the access has been granted, 
         *   <code>false</code> otherwise
         * @since 8.37
         */
        public boolean tryReadAccess(long timeout) {
            return delegate.tryReadAccess(timeout);
        }

        public void enterWriteAccess() {
            delegate.enterWriteAccess();
        }
        
        /**
         * Tries to obtain write access. If the access cannot by gained by given
         * milliseconds, the method returns without gaining it.
         *
         * @param timeout amount of milliseconds to wait before giving up.
         *   <code>0</code> means to wait indefinitely.
         *   <code>-1</code> means to not wait at all and immediately exit
         * @return <code>true</code> if the access has been granted,
         * <code>false</code> otherwise
         * @since 8.37
         */
        public boolean tryWriteAccess(long timeout) {
            return delegate.tryWriteAccess(timeout);
        }

        public void exitReadAccess() {
            delegate.exitReadAccess();
        }

        public void exitWriteAccess() {
            delegate.exitWriteAccess();
        }
    }    
}
