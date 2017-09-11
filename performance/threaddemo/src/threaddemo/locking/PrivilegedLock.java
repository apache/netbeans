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

package threaddemo.locking;

/**
 * Ability to acquire or release a lock by itself.
 * This class can be used when one wants to avoid creating a
 * bunch of Runnables. Instead use:
 * <pre>
 * p.enter*();
 * try {
 *     // your code here
 * } finally {
 *     p.exit*();
 * }
 * </pre>
 * You must be careful to match enter and exit calls reliably and exactly.
 * Please read the Javadoc for each method carefully.
 *
 * <p>You must control the related Lock, i.e. you must be the creator of
 * the Lock. Thus you may create a PrivilegedLock for efficient access within
 * a package and only expose the lock to outside code, to ensure that it is
 * not abused by being entered and not exited.
 * @see Lock
 */
public final class PrivilegedLock {
    
    private DuplexLock parent;
    
    /** Create a new privileged key to a lock.
     * (It may only be used in one lock.)
     */
    public PrivilegedLock() {}
    
    final synchronized void setParent(DuplexLock parent) {
        if (this.parent != null) throw new IllegalStateException();
        this.parent = parent;
    }
    
    /**
     * Get the associated lock.
     * You must have already created a lock with this privileged handle.
     * @return the lock associated with this object
     */
    public RWLock getLock() {
        if (parent == null) throw new IllegalStateException("Unbound PrivilegedLock"); // NOI18N
        return parent;
    }
    
    /** Enter read access for this lock.
     * <strong>You must ensure that {@link #exitRead} is reliably called
     * when you are done.</strong> The normal way to do this is as follows:
     * <pre>
     * p.enterRead();
     * // must be no additional code here!
     * try {
     *     // whatever code...
     * } finally {
     *     // must be no additional code here!
     *     p.exitRead();
     * }
     * </pre>
     *
     * <p>Detailed behavior:
     * <ol>
     * <li>You may already be holding the read or write lock. But you must
     *     still nest entries and exits, 1-to-1.
     * <li>If this lock has a level, you may not enter it if you are already
     *     holding another lock with a smaller or equal level in this thread.
     * <li>If another thread is holding the write lock, <strong>this method
     *     will block</strong> until it leaves.
     * </ol>
     *
     */
    public void enterRead() {
        parent.enterRead();
    }
    
    /** Enter write access for this lock.
     * <strong>You must ensure that {@link #exitWrite} is reliably called
     * when you are done.</strong> The normal way to do this is as follows:
     * <pre>
     * p.enterWrite();
     * // must be no additional code here!
     * try {
     *     // whatever code...
     * } finally {
     *     // must be no additional code here!
     *     p.exitWrite();
     * }
     * </pre>
     *
     * <p>Detailed behavior:
     * <ol>
     * <li>You may already be holding the write lock. But you must
     *     still nest entries and exits, 1-to-1.
     * <li><strong>You may not be holding the read lock</strong> - even if inside
     *     the write lock.
     * <li>If this lock has a level, you may not enter it if you are already
     *     holding another lock with a smaller or equal level in this thread.
     * <li>If other threads are holding the read or write lock, <strong>this method
     *     will block</strong> until they all leave.
     * </ol>
     *
     */
    public void enterWrite() {
        parent.enterWrite();
    }
    
    /** Exit the read lock.
     * For important usage instructions, see {@link #enterRead}.
     *
     * <p>Detailed behavior:
     * <ol>
     * <li>You must have already entered this lock in read mode (once for
     *     every time you exit it).
     * <li>You must exit a lock in the same thread you entered it.
     * <li>If this lock has a level, it must be the last lock with a level
     *     which you entered in this thread. You cannot interleave exits of
     *     locks with levels; they must nest.
     * <li>If this read access is inside another read access, this method
     *     will return immediately.
     * <li>If this read access is the outermost read access, and not inside any
     *     write access, it will return immediately.
     * <li>If this read access is the outermost read access within a write access,
     *     it will return immediately.
     * </ol>
     *
     */
    public void exitRead() {
        parent.exitRead();
    }
    
    /** Exit the write lock.
     * For important usage instructions, see {@link #enterWrite}.
     *
     * <p>Detailed behavior:
     * <ol>
     * <li>You must have already entered this lock in write mode (once for
     *     every time you exit it).
     * <li>You must exit a lock in the same thread you entered it.
     * <li>If this lock has a level, it must be the last lock with a level
     *     which you entered in this thread. You cannot interleave exits of
     *     locks with levels; they must nest.
     * <li>If this write access is inside another write access, this method
     *     will return immediately.
     * <li>If this write access is the outermost write access, it will return
     *     immediately.
     * </ol>
     *
     */
    public void exitWrite() {
        parent.exitWrite();
    }
    
}

