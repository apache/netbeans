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

package org.netbeans.modules.progress.ui;

import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.progress.spi.ExtractedProgressUIWorker;
import org.netbeans.modules.progress.spi.InternalHandle;
import org.netbeans.modules.progress.spi.ProgressUIWorkerProvider;
import org.netbeans.modules.progress.spi.ProgressUIWorkerWithModel;

/**
 *
 * @author mkleint
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.progress.spi.ProgressUIWorkerProvider.class)
public class ProviderImpl implements ProgressUIWorkerProvider {
    
    private final Map<InternalHandle, NbProgressBar>  progresses = new WeakHashMap<>();
    
    /** Creates a new instance of ProviderImpl */
    public ProviderImpl() {
    }

    @Override
    public ProgressUIWorkerWithModel getDefaultWorker() {
        return new StatusLineComponent();
    }

    @Override
    public ExtractedProgressUIWorker getExtractedComponentWorker() {
        return new NbProgressBar();
    }

    @Override
    public ExtractedProgressUIWorker extractProgressWorker(InternalHandle handle) {
        synchronized (this) {
            return progresses.computeIfAbsent(handle, (h) -> new NbProgressBar());
        }
    }
}
