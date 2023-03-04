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

import java.io.IOException;
import org.netbeans.modules.profiler.snaptracer.ItemValueFormatter;
import org.netbeans.modules.profiler.snaptracer.ProbeItemDescriptor;
import org.netbeans.modules.profiler.snaptracer.TracerProbe;
import org.netbeans.modules.profiler.snaptracer.impl.IdeSnapshot;
import org.openide.util.Exceptions;

/**
 *
 * @author Jiri Sedlacek
 */
class TestProbe extends TracerProbe {
    
//    private int items;
    private IdeSnapshot snapshot;
    
    
    public TestProbe(IdeSnapshot snapshot) {
        super(descriptors(1));
        this.snapshot = snapshot;
//        this.items = items;
    }

    public long[] getItemValues(int sampleIndex) {
        return values(sampleIndex);
    }
    
    
    private static ProbeItemDescriptor[] descriptors(int items) {
        ProbeItemDescriptor[] descriptors = new ProbeItemDescriptor[items];
        descriptors[0] = ProbeItemDescriptor.continuousLineItem("Cumulative stack depth",
                             "Reports the cumulative depth of all running threads", ItemValueFormatter.DEFAULT_DECIMAL);
//        for (int i = 0; i < descriptors.length; i++)
//            descriptors[i] = ProbeItemDescriptor.continuousLineItem("Item " + i,
//                             "Description " + i, ItemValueFormatter.SIMPLE);
        return descriptors;
    }
    
    private long[] values(int sampleIndex) {
        long[] values = new long[1];
        try {
            values[0] = snapshot.getValue(sampleIndex, 0);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
//        for (int i = 0; i < values.length; i++)
//            values[i] = (long)(Math.random() * 10000);
        return values;
    }

}
