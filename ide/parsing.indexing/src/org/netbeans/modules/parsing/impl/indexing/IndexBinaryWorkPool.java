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
package org.netbeans.modules.parsing.impl.indexing;

import java.net.URL;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Zezula
 */
class IndexBinaryWorkPool {
    
    private static final int MIN_PROC = 4;
    private static final int DEFAULT_PROC_COUNT = 2; // 2 has min(n*tp/ts)
    private static final boolean PAR_DISABLED = Boolean.getBoolean("IndexBinaryWorkPool.sequential");   //NOI18N
    private static final int PROC_COUNT = Integer.getInteger("IndexBinaryWorkPool.proc.count",DEFAULT_PROC_COUNT);  //NOI18N
    private static final Logger LOG = Logger.getLogger(IndexBinaryWorkPool.class.getName());
    
    private final Function<URL,Boolean> fnc;
    private final Callable<Boolean> cancel;
    private final Collection<? extends URL> binaries;
    
    IndexBinaryWorkPool (
            @NonNull final Function<URL,Boolean> fnc,
            @NonNull final Callable<Boolean> cancel,
            @NonNull final Collection<? extends URL> binaries) {
        assert fnc != null;
        assert cancel != null;
        assert binaries != null;
        this.fnc = fnc;
        this.cancel = cancel;
        this.binaries = binaries;
    }
    
   
    Pair<Boolean,Collection<? extends URL>> execute() {
        final Strategy strategy = getStrategy(binaries.size());
        assert strategy != null;
        return strategy.execute(fnc, cancel, binaries);
    }
    
    static interface Function<P,R> {
        R apply (P param);
    }
    
    
    private static interface Strategy {
        @NonNull
        Pair<Boolean,Collection<? extends URL>> execute(
                @NonNull Function<URL,Boolean> fnc,
                @NonNull Callable<Boolean> cancel,
                @NonNull final Collection<? extends URL> binaries);
    }
    
    private static class SequentialStrategy implements Strategy {

        @Override
        @NonNull
        public Pair<Boolean,Collection<? extends URL>> execute(
                @NonNull Function<URL,Boolean> fnc,
                @NonNull Callable<Boolean> cancel,
                @NonNull Collection<? extends URL> binaries) {
            final Collection<URL> result = new ArrayDeque<URL>(binaries.size());
            boolean success = true;
            try {
                for (URL binary : binaries) {
                    if (cancel.call()) {
                        success = false;
                        break;
                    }
                    if (fnc.apply(binary)) {
                        result.add(binary);
                    } else {
                        success = false;
                        break;
                    }
                }
            } catch (Exception ce) {
                success = false;
            }
            LOG.log(Level.FINER, "Canceled: {0}", !success);  //NOI18N
            return Pair.<Boolean,Collection<? extends URL>>of(success,result);
        }
        
    }
    
    private static class ConcurrentStrategy implements Strategy {
        
        private static final RequestProcessor RP = new RequestProcessor(
            ConcurrentStrategy.class.getName(),
                PROC_COUNT,
                false,
                false);

        @Override
        @NonNull
        public Pair<Boolean,Collection<? extends URL>> execute(
                @NonNull final Function<URL,Boolean> fnc,
                @NonNull final Callable<Boolean> cancel,
                @NonNull final Collection<? extends URL> binaries) {
            final CompletionService<URL> cs = new ExecutorCompletionService<URL>(RP);
            int submitted = 0;
            for (URL binary : binaries) {
                cs.submit(new Task(binary,fnc, cancel));
                submitted++;
            }
            final Collection<URL> result = new ArrayDeque<URL>();
            //Don't break the cycle when is canceled,
            //rather wait for all submitted task, they should die fast.
            //The break will cause logging of wrong number of scanned roots.
            for (int i=0; i< submitted; i++) {
                try {                    
                    final Future<URL> becomeURL = cs.take();
                    final URL url = becomeURL.get();
                    if (url != null) {
                        result.add(url);
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            boolean success;
            try {
                success = !cancel.call();
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
                success = false;
            }
            LOG.log(Level.FINER, "Canceled: {0}", !success);  //NOI18N
            return Pair.<Boolean,Collection<? extends URL>>of(success,result);
        }
    }
    
    private static class Task implements Callable<URL> {
        
        private final URL binary;
        private final Function<URL,Boolean> performer;
        private final Callable<Boolean> cancel;
        
        private Task(
            @NonNull final URL binary,
            @NonNull final Function<URL,Boolean> performer,
            @NonNull final Callable<Boolean> cancel) {
            this.binary = binary;
            this.performer = performer;
            this.cancel = cancel;
        }

        @Override
        public URL call() throws Exception {
            return cancel.call()?
                    null:
                    performer.apply(binary)?
                        binary:
                        null;
        }
    }
    
    @NonNull
    private static Strategy getStrategy(final int binariesCount) {
        final int procCount = Runtime.getRuntime().availableProcessors();
        final boolean supportsPar = procCount >= MIN_PROC;
        LOG.log(
            Level.FINER,
            "Proc Count: {0} Binaries Count: {1} Concurrent worker disabled: {2}",  //NOI18N
            new Object[]{
                procCount,
                binariesCount,
                PAR_DISABLED}
        );
        if (!PAR_DISABLED && supportsPar && binariesCount >= 2) {
            LOG.log(
                Level.FINE,
                "Using concurrent strategy, {0} workers",    //NOI18N
                PROC_COUNT);
            return new ConcurrentStrategy();
        } else {
            LOG.fine("Using sequential strategy");    //NOI18N
            return new SequentialStrategy();
        }
    }
    
}
