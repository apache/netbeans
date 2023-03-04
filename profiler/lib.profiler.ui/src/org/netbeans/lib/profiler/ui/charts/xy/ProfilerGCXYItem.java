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

package org.netbeans.lib.profiler.ui.charts.xy;

import org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItem;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class ProfilerGCXYItem extends SynchronousXYItem {

    // --- Constructor ---------------------------------------------------------

    public ProfilerGCXYItem(String name) {
        super(name, Long.MAX_VALUE, Long.MIN_VALUE);
    }


    // --- Item telemetry ------------------------------------------------------

    public abstract long[] getGCStarts(int index);

    public abstract long[] getGCEnds(int index);

    
    public long getYValue(int index) { return 0; }

}
