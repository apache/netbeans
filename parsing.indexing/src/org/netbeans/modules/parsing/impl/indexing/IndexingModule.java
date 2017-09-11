/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import org.netbeans.modules.parsing.impl.indexing.lucene.DocumentBasedIndexManager;
import org.netbeans.modules.parsing.impl.indexing.lucene.LuceneIndexFactory;
import org.openide.modules.OnStart;
import org.openide.modules.OnStop;
import org.openide.util.Exceptions;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author sdedic
 */
public class IndexingModule {
    private static final String STOP_HOOKS_PATH = "Parsing/Indexing/Stop";   //NOI18N
    private static volatile boolean closed;

    /**
     * Initialization part of the former IndexerModule / ModuleInstall
     *
     * @author sdedic
     */
    @OnStart
    public static class Startup implements Runnable {
        @Override
        public void run() {
            RepositoryUpdater.getDefault().start(false);
        }
    }

    @OnStop
    public static class Shutdown implements Runnable, Callable<Boolean> {

        @Override
        public void run() {
            closed = true;
            final Runnable postTask = new Runnable() {
                @Override
                public void run() {
                    callStopHooks();
                    LuceneIndexFactory.getDefault().close();
                    DocumentBasedIndexManager.getDefault().close();
                }
            };
            try {
                RepositoryUpdater.getDefault().stop(postTask);
            } catch (TimeoutException | IllegalStateException e) {
                //Timeout or already closed
                postTask.run();
            }
        }

        @Override
        public Boolean call() throws Exception {
            LogContext.notifyClosing();
            return true;
        }
    }

    public static boolean isClosed() {
        return closed;
    }

    private static void callStopHooks() {
        for (Runnable r : Lookups.forPath(STOP_HOOKS_PATH).lookupAll(Runnable.class)) {
            try {
                r.run();
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    throw (ThreadDeath) t;
                } else {
                    Exceptions.printStackTrace(t);
                }
            }
        }

    }

}
