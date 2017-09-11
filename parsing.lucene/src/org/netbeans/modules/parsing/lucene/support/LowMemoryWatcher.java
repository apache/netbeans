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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
