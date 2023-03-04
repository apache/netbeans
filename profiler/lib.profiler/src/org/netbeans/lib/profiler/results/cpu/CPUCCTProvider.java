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

package org.netbeans.lib.profiler.results.cpu;

import org.netbeans.lib.profiler.results.CCTProvider;


/**
 *
 * @author Jaroslav Bachorik
 */
public interface CPUCCTProvider extends CCTProvider {
    public static interface Listener extends CCTProvider.Listener {
    }

    /**
     * Generate the presentation CCTs for all threads that are currently profiled. Returns an array of all generated CPUCCTContainers
     * (which accidentally may contain just 0 elements, if none of the created threads actually have any data at this time).
     */
    CPUCCTContainer[] createPresentationCCTs(CPUResultsSnapshot cpuSnapshot);
}
