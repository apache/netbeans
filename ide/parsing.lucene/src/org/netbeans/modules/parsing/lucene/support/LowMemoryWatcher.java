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

package org.netbeans.modules.parsing.lucene.support;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.lucene.CacheCleaner;
import org.openide.util.Exceptions;

/**
 * A service providing information about
 * low memory condition.
 * @since 1.2
 * @author Tomas Zezula
 */
public final class LowMemoryWatcher {

    private static final Logger LOG = Logger.getLogger(LowMemoryWatcher.class.getName());
    private static final long LOGGER_RATE = Integer.getInteger(
            String.format("%s.logger_rate",LowMemoryWatcher.class.getName()),   //NOI18N
            1000);   //1s


    //@GuardedBy("LowMemoryWatcher.class")
    private static LowMemoryWatcher instance;

    private final Callable<Boolean> strategy;
    private final AtomicBoolean testEnforcesLowMemory = new AtomicBoolean();

    private LowMemoryWatcher () {
        this.strategy = new DefaultStrategy();
    }

    /**
     * Returns true if the application is in low memory condition.
     * This information can be used by batch file processing.
     * @return true if nearly whole memory is used
     */
    public boolean isLowMemory () {
        if (testEnforcesLowMemory.get()) {
            return true;
        }
        try {
            return strategy.call();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            return false;
        }
    }

    /**
     * Tries to free memory.
     * @since 2.12
     */
    public void free() {
        free(false);
    }

    /**
     * Tries to free memory including Lucene caches.
     * @param freeCaches should the Lucene caches be cleaned.
     * @since 2.32
     */
    public void free(final boolean freeCaches) {
        if (freeCaches) {
            CacheCleaner.clean();
        }
        final Runtime rt = Runtime.getRuntime();
        rt.gc();
        rt.runFinalization();
        rt.gc();
        rt.gc();
    }

    /*test*/ void setLowMemory(final boolean lowMemory) {
        this.testEnforcesLowMemory.set(lowMemory);
    }

    /**
     * Returns an instance of {@link LowMemoryWatcher}
     * @return the {@link LowMemoryWatcher}
     */
    public static synchronized LowMemoryWatcher getInstance() {
        if (instance == null) {
            instance = new LowMemoryWatcher();
        }
        return instance;
    }


    private static class DefaultStrategy implements Callable<Boolean> {

        private static final float heapLimit = 0.8f;
        private final MemoryMXBean memBean;
        private volatile long lastTime;

        DefaultStrategy() {
            this.memBean = ManagementFactory.getMemoryMXBean();
            assert this.memBean != null;
        }

        @Override
        public Boolean call() throws Exception {
            if (this.memBean != null) {
                final MemoryUsage usage = this.memBean.getHeapMemoryUsage();
                if (usage != null) {
                    long used = usage.getUsed();
                    long max = usage.getMax();
                    final boolean res = used > max * heapLimit;
                    if (LOG.isLoggable(Level.FINEST)) {
                        final long now = System.currentTimeMillis();
                        if (now - lastTime > LOGGER_RATE) {
                            LOG.log(
                                Level.FINEST,
                                "Max memory: {0}, Used memory: {1}, Low memory condition: {2}", //NOI18N
                                new Object[]{
                                    max,
                                    used,
                                    res
                                });
                            lastTime = now;
                        }
                    }
                    return res;
                }
            }
            return false;
        }
    }

}
