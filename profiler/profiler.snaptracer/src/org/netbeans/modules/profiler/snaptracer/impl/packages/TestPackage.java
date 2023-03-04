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

package org.netbeans.modules.profiler.snaptracer.impl.packages;

import org.netbeans.modules.profiler.snaptracer.TracerPackage;
import org.netbeans.modules.profiler.snaptracer.TracerProbe;
import org.netbeans.modules.profiler.snaptracer.TracerProbeDescriptor;
import org.netbeans.modules.profiler.snaptracer.impl.IdeSnapshot;

/**
 *
 * @author Jiri Sedlacek
 */
class TestPackage extends TracerPackage {
    
    private TracerProbeDescriptor descriptor1;
    private TracerProbeDescriptor descriptor2;
    private TracerProbe probe1;
    private TracerProbe probe2;

    private IdeSnapshot snapshot;
    
    
    TestPackage(IdeSnapshot snapshot) {
        super("Test Package", "Package for testing purposes", null, 1);
        this.snapshot = snapshot;
    }

    
    public TracerProbeDescriptor[] getProbeDescriptors() {
        if (snapshot.hasUiGestures()) {
            descriptor1 = new TracerProbeDescriptor("UI Actions", "Shows UI actions performed by the user in the IDE", null, 1, true);
            descriptor2 = new TracerProbeDescriptor("Stack depth", "Reports the cummulative depth of all running threads", null, 2, true);
            return new TracerProbeDescriptor[] { descriptor1, descriptor2, };
        } else {
            descriptor2 = new TracerProbeDescriptor("Stack depth", "Reports the cummulative depth of all running threads", null, 2, true);
            return new TracerProbeDescriptor[] { descriptor2, };
        }
    }

    public TracerProbe getProbe(TracerProbeDescriptor descriptor) {
        if (descriptor == descriptor1) {
            if (probe1 == null) probe1 = new UiGesturesProbe(snapshot);
            return probe1;
        } else if (descriptor == descriptor2) {
            if (probe2 == null) probe2 = new TestProbe(snapshot);
            return probe2;
        } else {
            return null;
        }
    }

}
