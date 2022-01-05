/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.progress.spi;

/**
 *
 * @author mkleint
 * @since org.netbeans.api.progress/1 1.18
 */
public interface ProgressUIWorkerProvider {

    public ProgressUIWorkerWithModel getDefaultWorker();
    
    public ExtractedProgressUIWorker getExtractedComponentWorker();

    /**
     * Provides an extracted worker instance for the given internal handle.
     * @param handle internal handle for the worker.
     * @return the worker instance, possibly {@code null}.
     * @since 1.59
     */
    public default ExtractedProgressUIWorker extractProgressWorker(InternalHandle handle) {
        return getExtractedComponentWorker();
    }
}
