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
package org.netbeans.modules.profiler.snaptracer.impl.icons;

import java.util.Map;
import org.netbeans.modules.profiler.spi.IconsProvider;

/**
 *
 * @author Jiri Sedlacek
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.spi.IconsProvider.class)
public final class TracerIconsProviderImpl extends IconsProvider.Basic {
    
    @Override
    protected final void initStaticImages(Map<String, String> cache) {
        cache.put(TracerIcons.INCREMENT, "increment.png"); // NOI18N
        cache.put(TracerIcons.DECREMENT, "decrement.png"); // NOI18N
        cache.put(TracerIcons.RESET, "reset.png"); // NOI18N
        cache.put(TracerIcons.GENERIC_ACTION, "genericAction.png"); // NOI18N
        cache.put(TracerIcons.MOUSE_WHEEL_HORIZONTAL, "hmwheel.png"); // NOI18N
        cache.put(TracerIcons.MOUSE_WHEEL_VERTICAL, "vmwheel.png"); // NOI18N
        cache.put(TracerIcons.MOUSE_WHEEL_ZOOM, "zmwheel.png"); // NOI18N
        cache.put(TracerIcons.MARK, "mark.png"); // NOI18N
        cache.put(TracerIcons.MARK_CLEAR, "markClear.png"); // NOI18N
        cache.put(TracerIcons.MARK_HIGHLIGHT, "markHighl.png"); // NOI18N
        cache.put(TracerIcons.SELECT_ALL, "selectAll.png"); // NOI18N
        cache.put(TracerIcons.PROBE, "probe.png"); // NOI18N
        cache.put(TracerIcons.TRACER, "tracer.png"); // NOI18N
        cache.put(TracerIcons.TRACER_32, "tracer32.png"); // NOI18N
    }
    
}
