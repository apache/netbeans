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

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Implementation of a regular lock (read/write).
 * @author Jesse Glick
 */
final class ReadWriteLockWrapper implements DuplexLock {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    /** workaround needed in Tiger; see {@link #canRead} */
    private final ThreadLocal<Integer> reading = new ThreadLocal<Integer>() {
        protected Integer initialValue() {
            return 0;
        }
    };

    public ReadWriteLockWrapper() {}

    public void enterRead() {
        lock.readLock().lock();
        reading.set(reading.get() + 1);
    }

    public void exitRead() {
        lock.readLock().unlock();
        assert reading.get() > 0;
        reading.set(reading.get() - 1);
    }

    public void enterWrite() {
        lock.writeLock().lock();
    }

    public void exitWrite() {
        lock.writeLock().unlock();
    }

    public void read(Runnable action) {
        enterRead();
        try {
            action.run();
        } finally {
            exitRead();
        }
    }

    public void write(Runnable action) {
        enterWrite();
        try {
            action.run();
        } finally {
            exitWrite();
        }
    }

    public <T> T read(LockAction<T> action) {
        enterRead();
        try {
            return action.run();
        } finally {
            exitRead();
        }
    }

    public <T> T write(LockAction<T> action) {
        enterWrite();
        try {
            return action.run();
        } finally {
            exitWrite();
        }
    }

    public <T, E extends Exception> T read(LockExceptionAction<T, E> action) throws E {
        enterRead();
        try {
            return action.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            @SuppressWarnings("unchecked")
            E _e = (E) e;
            throw _e;
        } finally {
            exitRead();
        }
    }

    public <T, E extends Exception> T write(LockExceptionAction<T, E> action) throws E {
        enterWrite();
        try {
            return action.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            @SuppressWarnings("unchecked")
            E _e = (E) e;
            throw _e;
        } finally {
            exitWrite();
        }
    }

    public void readLater(final Runnable action) {
        Worker.start(new Runnable() {
            public void run() {
                read(action);
            }
        });
    }

    public void writeLater(final Runnable action) {
        Worker.start(new Runnable() {
            public void run() {
                write(action);
            }
        });
    }

    public boolean canRead() {
        // XXX in JDK 6 can just use: return lock.getReadHoldCount() > 0;
        return reading.get() > 0;
    }

    public boolean canWrite() {
        return lock.isWriteLockedByCurrentThread();
    }
    
}
