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
package org.netbeans.lib.profiler.ui;

import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.ui.components.ProfilerToolbar;
import org.netbeans.lib.profiler.ui.cpu.LiveFlatProfilePanel;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Bachorik
 */
public interface LiveResultsWindowContributor {

    public abstract static class Adapter implements LiveResultsWindowContributor {

        @Override
        public void addToCpuResults(LiveFlatProfilePanel cpuPanel, ProfilerToolbar toolbar, ProfilerClient client, Lookup.Provider project) {
        }

        @Override
        public void addToMemoryResults(LiveFlatProfilePanel memoryPanel, ProfilerToolbar toolbar, ProfilerClient client, Lookup.Provider project) {
        }

        @Override
        public void hide() {
        }

        @Override
        public void show() {
        }

        @Override
        public void refresh() {
        }

        @Override
        public void reset() {
        }
    }

    void addToCpuResults(LiveFlatProfilePanel cpuPanel, ProfilerToolbar toolbar, ProfilerClient client, Lookup.Provider project);

    void addToMemoryResults(LiveFlatProfilePanel memoryPanel, ProfilerToolbar toolbar, ProfilerClient client, Lookup.Provider project);

    void show();

    void hide();

    void refresh();

    void reset();
}
