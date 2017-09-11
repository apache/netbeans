/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.support;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;

/**
 *
 * P - type of task parameters
 * R - type of result
 *
 */
public final class TasksCachedProcessor<P, R>
        implements Computable<P, R> {

    private static final java.util.logging.Logger log = Logger.getInstance();
    private final ConcurrentMap<P, Future<R>> cache =
            new ConcurrentHashMap<>();
    private final Computable<P, R> computable;
    private final boolean removeOnCompletion;

    public TasksCachedProcessor(Computable<P, R> c, boolean removeOnCompletion) {
        this.computable = c;
        this.removeOnCompletion = removeOnCompletion;
    }

    public boolean isResultAvailable(final P arg) {
        Future<R> res = cache.get(arg);

        if (res == null) {
            return false;
        }

        return res.isDone() && !res.isCancelled();
    }

    /**
     * Here I implemented following logic:
     * if it is requested to fetch the data and the same request is in progress -
     * result that returned is taken from the original one.
     * once task is completed, it is removed from cache!
     *
     */
    @Override
    public R compute(final P arg) throws InterruptedException {
        Future<R> f = cache.get(arg);

        if (f == null) {
            Callable<R> evaluation = new Callable<R>() {

                @Override
                public R call() throws InterruptedException {
                    return computable.compute(arg);
                }
            };

            FutureTask<R> ft = new FutureTask<>(evaluation);
            f = cache.putIfAbsent(arg, ft);

            if (f == null) {
                f = ft;
                ft.run();
            }
        }

        try {
            return f.get();
        } catch (InterruptedException ex) {
            cache.remove(arg, f);
            throw new CancellationException(ex.getMessage());
        } catch (Throwable th) {
            cache.remove(arg, f);
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "TasksCachedProcessor: exception while task execution:", th); // NOI18N
            }
            throw new CancellationException(th.getMessage());
        } finally {
            if (removeOnCompletion) {
                cache.remove(arg, f);
            }
        }
    }

    public void remove(P param) {
        Future<R> f = cache.get(param);

        if (f != null && !f.isDone()) {
            f.cancel(true);
        }

        cache.remove(param);
    }

    public void resetCache() {
        // Even if some tasks are in progress it's OK just to clear the cache.
        // Tasks will not be terminated though...
        cache.clear();
    }
}
