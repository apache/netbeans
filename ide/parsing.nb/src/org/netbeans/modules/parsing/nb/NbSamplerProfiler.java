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

package org.netbeans.modules.parsing.nb;

import java.io.DataOutputStream;
import java.io.IOException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.implspi.ProfilerSupport;
import org.netbeans.modules.sampler.Sampler;
import org.openide.util.Parameters;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
public final class NbSamplerProfiler extends ProfilerSupport {
    private @NonNull final Sampler profiler;

    private NbSamplerProfiler(@NonNull final Sampler profiler) {
        Parameters.notNull("profiler", profiler);   //NOI18N
        this.profiler = profiler;
    }

    @Override
    public void start() {
        profiler.start();
    }

    @Override
    public void cancel() {
        profiler.cancel();
    }

    @Override
    public void stopAndSnapshot(DataOutputStream dos) throws IOException {
        profiler.stopAndWriteTo(dos);
    }

    @ServiceProvider(service = ProfilerSupport.Factory.class)
    public static final class Factory implements ProfilerSupport.Factory {
        @Override
        @CheckForNull
        public ProfilerSupport create(@NonNull final String id) {
            Parameters.notNull("id", id);   //NOI18N
            Sampler s = Sampler.createSampler(id);
            return s != null ? new NbSamplerProfiler(s) : null;
        }
    }
}
