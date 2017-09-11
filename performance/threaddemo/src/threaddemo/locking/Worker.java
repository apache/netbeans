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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Worker thread (off-AWT) that can run tasks asynch.
 * Convenience wrapper for {@link ExecutorService}.
 * @author Jesse Glick
 */
public final class Worker {

    private static final ExecutorService POOL = Executors.newCachedThreadPool();

    private Worker() {}

    /**
     * Start a task.
     * It will be run soon.
     * At most one task will be run at a time.
     */
    public static void start(Runnable run) {
        POOL.submit(run);
    }
    
    /**
     * Do something and wait for it to finish.
     */
    public static <T> T block(final LockAction<T> act) {
        Future<T> f = POOL.submit(new Callable<T>() {
            public T call() {
                return act.run();
            }
        });
        try {
            return f.get();
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw (RuntimeException) t;
            }
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }
    
    /**
     * Do something and wait for it to finish.
     * May throw exceptions.
     */
    public static <T, E extends Exception> T block(final LockExceptionAction<T,E> act) throws E {
        Future<T> f = POOL.submit(new Callable<T>() {
            public T call() throws Exception {
                return act.run();
            }
        });
        try {
            return f.get();
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof Error) {
                throw (Error) t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                @SuppressWarnings("unchecked")
                E _e = (E) e;
                throw _e;
            }
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }
    
}
