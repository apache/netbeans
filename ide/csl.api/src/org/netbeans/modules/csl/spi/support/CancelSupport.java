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
package org.netbeans.modules.csl.spi.support;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.core.CancelSupportImplementation;
import org.netbeans.modules.csl.core.SpiSupportAccessor;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.openide.util.Parameters;

/**
 * Provides a thread safe testing of the {@link SchedulerTask} canceling.
 * Provides a thread safe testing of the {@link SchedulerTask} canceling for CSL
 * services which are not implemented using {@link SchedulerTask}. For {@link SchedulerTask}
 * subclasses use parsing api {@link org.netbeans.modules.parsing.spi.support.CancelSupport}
 * @author Tomas Zezula
 * @since 2.50
 */
public final class CancelSupport {

    private static final CancelSupport INSTANCE = new CancelSupport();

    static {
        SpiSupportAccessor.setInstance(new SpiSupportAccessorImpl());
    }

    private final ThreadLocal<CancelSupportImplementation> selfSpi;

    private CancelSupport() {
        this.selfSpi = new ThreadLocal<>();
    }

    /**
     * Returns true if the task is canceled.
     * @return true when task is canceled
     */
    public boolean isCancelled() {
        final CancelSupportImplementation spi = selfSpi.get();
        return spi == null ?
                false :
                spi.isCancelled();
    }

    /**
     * Returns the {@link CancelSupport} instance.
     * @return the {@link CancelSupport} instance.
     */
    @NonNull
    public static CancelSupport getDefault() {
        return INSTANCE;
    }

    private static final class SpiSupportAccessorImpl extends SpiSupportAccessor {

        @Override
        public void setCancelSupport(@NonNull final CancelSupportImplementation cancelSupport) {
            Parameters.notNull("cancelSupport", cancelSupport); //NOI18N
            final CancelSupport cs = getDefault();
            if (cs.selfSpi.get() == null) {
                cs.selfSpi.set(cancelSupport);
            }
        }

        @Override
        public void removeCancelSupport(@NonNull final CancelSupportImplementation cancelSupport) {
            Parameters.notNull("cancelSupport", cancelSupport); //NOI18N
            final CancelSupport cs = getDefault();
            if (cs.selfSpi.get() == cancelSupport) {
                cs.selfSpi.remove();
            }
        }
    }
}
