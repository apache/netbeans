/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.project.indexingbridge;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Allows parser indexing to be temporarily suppressed.
 * Unlike {@code org.netbeans.modules.parsing.api.indexing.IndexingManager}
 * this is not block-scoped. Every call to {@link #protectedMode} must
 * eventually be matched by exactly one call to {@link Lock#release}.
 * It is irrelevant which thread makes each call. It is permissible to make
 * multiple enter calls so long as each lock is released.
 */
public abstract class IndexingBridge {


    /**
     * IndexingBridge which allows a caller {@link IndexingBridge#protectedMode(boolean)}
     * to wait for not yet processed indexing tasks.
     * @since 1.5
     */
    public static abstract class Ordering extends IndexingBridge {
        /**
         * Waits until the non processes indexing tasks are done.
         * @throws InterruptedException when the waiting thread is interrupted.
         */
        protected abstract void await() throws InterruptedException;
    }

    private static final Logger LOG = Logger.getLogger(IndexingBridge.class.getName());

    protected IndexingBridge() {}

    /**
     * Begin suppression of indexing.
     * @return a lock indicating when to resume indexing
     */
    public final Lock protectedMode() {
        return protectedMode(false);
    }

    /**
     * Begin suppression of indexing.
     * @return a lock indicating when to resume indexing
     * @param waitForScan if ture and if the implementation of {@link IndexingBridge}
     * supports waits for not yet processed indexing tasks before entering to protected mode.
     * @return a lock indicating when to resume indexing
     * @since 1.5
     */
    public final Lock protectedMode(final boolean waitForScan) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, null, new Throwable("IndexingBridge.protectedMode"));
        }
        if (waitForScan && (this instanceof Ordering)) {
            try {
                ((Ordering)this).await();
            } catch (InterruptedException ex) {
                //pass: cancel of running task.
            }
        }
        enterProtectedMode();
        return new Lock();
    }

    /**
     * @see #protectedMode
     */
    public final class Lock {

        private final Stack creationStack = new Stack("locked here");
        private Stack releaseStack;

        /**
         * End suppression of indexing.
         * Indexing may resume if this is the last matching call.
         */
        public void release() {
            synchronized (IndexingBridge.this) {
                if (releaseStack != null) {
                    LOG.log(Level.WARNING, null, new IllegalStateException("Attempted to release lock twice", releaseStack));
                    return;
                }
                releaseStack = new Stack("released here", creationStack);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, null, new Throwable("IndexingBridge.Lock.release"));
                }
            }
            exitProtectedMode();
        }

        @SuppressWarnings("FinalizeDeclaration")
        @Override protected void finalize() throws Throwable {
            super.finalize();
            synchronized (IndexingBridge.this) {
                if (releaseStack != null) {
                    return;
                }
                LOG.log(Level.WARNING, "Unreleased lock", creationStack);
                releaseStack = new Stack("released here", creationStack);
            }
            exitProtectedMode();
        }
        
    }

    /**
     * SPI to enter protected mode semaphore. Will be matched eventually by one call to {@link #exitProtectedMode}.
     */
    protected abstract void enterProtectedMode();

    /**
     * SPI to exit protected mode semaphore. Will follow one call to {@link #enterProtectedMode}.
     */
    protected abstract void exitProtectedMode();

    /**
     * Gets the registered singleton of the bridge.
     * If none is registered, a dummy implementation is produced which tracks lock usage but does nothing else.
     */
    public static IndexingBridge getDefault() {
        IndexingBridge b = Lookup.getDefault().lookup(IndexingBridge.class);
        return b != null ? b : new IndexingBridge() {
            @Override protected void enterProtectedMode() {}
            @Override protected void exitProtectedMode() {}
        };
    }

    private static final class Stack extends Throwable {
        Stack(String msg) {
            super(msg);
        }
        Stack(String msg, Stack prior) {
            super(msg, prior);
        }
        @Override public synchronized Throwable fillInStackTrace() {
            boolean asserts = false;
            assert asserts = true;
            return asserts ? super.fillInStackTrace() : this;
        }
    }

}
