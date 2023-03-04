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
package org.netbeans.modules.profiler.ppoints.ui.icons;

import java.util.Map;
import org.netbeans.modules.profiler.spi.IconsProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
@ServiceProvider(service=IconsProvider.class)
public final class ProfilingPointsIconsProviderImpl extends IconsProvider.Basic {
    
    @Override
    protected final void initStaticImages(Map<String, String> cache) {
        cache.put(ProfilingPointsIcons.CODE, "codeProfilingPoint.png"); // NOI18N
        cache.put(ProfilingPointsIcons.GLOBAL, "globalProfilingPoint.png"); // NOI18N
        cache.put(ProfilingPointsIcons.LOAD_GENERATOR, "loadgenProfilingPoint.png"); // NOI18N
        cache.put(ProfilingPointsIcons.LOAD_GENERATOR_DISABLED, "loadgenProfilingPointD.png"); // NOI18N
        cache.put(ProfilingPointsIcons.PPOINT, "ppoint.png"); // NOI18N
        cache.put(ProfilingPointsIcons.ADD, "ppointAdd.png"); // NOI18N
        cache.put(ProfilingPointsIcons.EDIT, "ppointEdit.png"); // NOI18N
        cache.put(ProfilingPointsIcons.ENABLE_DISABLE, "ppointEnableDisable.png"); // NOI18N
        cache.put(ProfilingPointsIcons.REMOVE, "ppointRemove.png"); // NOI18N
        cache.put(ProfilingPointsIcons.RESET_RESULTS, "resetResultsProfilingPoint.png"); // NOI18N
        cache.put(ProfilingPointsIcons.RESET_RESULTS_DISABLED, "resetResultsProfilingPointD.png"); // NOI18N
        cache.put(ProfilingPointsIcons.STOPWATCH, "stopwatchProfilingPoint.png"); // NOI18N
        cache.put(ProfilingPointsIcons.STOPWATCH_DISABLED, "stopwatchProfilingPointD.png"); // NOI18N
        cache.put(ProfilingPointsIcons.TAKE_SNAPSHOT, "takeSnapshotProfilingPoint.png"); // NOI18N
        cache.put(ProfilingPointsIcons.TAKE_SNAPSHOT_DISABLED, "takeSnapshotProfilingPointD.png"); // NOI18N
        cache.put(ProfilingPointsIcons.TAKE_SNAPSHOT_TIMED, "timedTakeSnapshotProfilingPoint.png"); // NOI18N
        cache.put(ProfilingPointsIcons.TAKE_SNAPSHOT_TRIGGERED, "triggeredTakeSnapshotProfilingPoint.png"); // NOI18N
    }
    
}
