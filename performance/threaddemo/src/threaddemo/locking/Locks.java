/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
