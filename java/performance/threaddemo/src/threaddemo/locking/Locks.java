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

// XXX uses: FolderChildren, Children.MutexChildren, module system
// XXX lock wrapper for AbstractDocument (or Document + NbDocument.WriteLockable)

package threaddemo.locking;

/**
 * Factory for locks.
 * @author Ales Novak (old code), Jesse Glick (rewrite - #32439)
 */
public class Locks {

    private Locks() {}

    /**
     * Pseudo-lock that allows code to be synchronized with the AWT event dispatch thread.
     * This is handy in that you can define a constant of type Lock in some API, initially
     * set to a normal lock, and then switch everything to the event thread (or vice-versa).
     * <p>It behaves somewhat differently from a read-write lock.
     * <ol>
     * <li>There is no distinction between read and write access. There is only one
     *     access mode, which is exclusive, and runs on the AWT thread, not in the
     *     caller thread.
     * <li>There is no {@link PrivilegedLock}, so you cannot make entry or exit calls
     *     by themselves (which would make no sense).
     * <li>You cannot specify a level. The event lock is considered to be at a higher
     *     level than any ordinary lock with a defined level. This means that from the
     *     event thread, you can enter any lock (subject to other restrictions), but
     *     while holding any <em>ordered</em> lock you may not block on the event thread
     *     (using <code>Locks.eventLock</code> methods).
     * <li>{@link Lock#read(LockAction)}, {@link Lock#read(LockExceptionAction)},
     *     {@link Lock#write(LockAction)}, {@link Lock#write(LockExceptionAction)},
     *     {@link Lock#read(Runnable)}, and {@link Lock#write(Runnable)} when called from the
     *     event thread run synchronously. Else they all block, like
     *     {@link java.awt.EventQueue#invokeAndWait}.
     * <li>{@link Lock#readLater(Runnable)} and {@link Lock#writeLater(Runnable)} run asynchronously, like
     *     {@link java.awt.EventQueue#invokeLater}.
     * <li>{@link Lock#canRead} and {@link Lock#canWrite} just test whether you are in the event
     *     thread, like {@link java.awt.EventQueue#isDispatchThread}.
     * </ol>
     */
    public static RWLock event() {
        return EventLock.DEFAULT;
    }
    
    /**
     * XXX
     */
    public static synchronized RWLock eventHybrid() {
        return EventHybridLock.DEFAULT;
    }
    
    /**
     * XXX
     */
    public static RWLock monitor(Object monitor) {
        return new MonitorLock(monitor);
    }
    
    /**
     * Create a read/write lock.
     * Allows control over resources that
     * can be read by several readers at once but only written by one writer.
     * Wrapper for {@link java.util.concurrent.locks.ReentrantReadWriteLock}.
     */
    public static RWLock readWrite() {
        return new ReadWriteLockWrapper();
    }
    
    /**
     * Create a lock with a privileged key.
     * @param privileged a key which may be used to call unbalanced entry/exit methods directly
     * @see #readWrite()
     */
    public static RWLock readWrite(PrivilegedLock privileged) {
        DuplexLock l = (DuplexLock) readWrite();
        privileged.setParent(l);
        return l;
    }
    
}
