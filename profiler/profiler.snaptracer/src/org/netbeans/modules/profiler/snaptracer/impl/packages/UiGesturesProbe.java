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
class UiGesturesProbe extends TracerProbe {

    private IdeSnapshot snapshot;


    public UiGesturesProbe(IdeSnapshot snapshot) {
        super(descriptors(1, snapshot));
        this.snapshot = snapshot;
    }

    public long[] getItemValues(int sampleIndex) {
        return values(sampleIndex);
    }


    private static ProbeItemDescriptor[] descriptors(int items, IdeSnapshot snapshot) {
        ProbeItemDescriptor[] descriptors = new ProbeItemDescriptor[items];
        descriptors[0] = ProbeItemDescriptor.iconItem("UI Gesture",
                             "Shows UI actions performed by the user in the IDE",
                             new UiGesturesFormatter(snapshot));
        return descriptors;
    }

    private long[] values(int sampleIndex) {
        long[] values = new long[1];
        try {
            values[0] = snapshot.getValue(sampleIndex, 1);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return values;
    }


    private static class UiGesturesFormatter extends ItemValueFormatter {

        private IdeSnapshot snapshot;

        UiGesturesFormatter(IdeSnapshot snapshot) {
            this.snapshot = snapshot;
        }

        public String formatValue(long value, int format) {
            switch (format) {
                case FORMAT_TOOLTIP:
                case FORMAT_DETAILS:
                case FORMAT_EXPORT:
                    IdeSnapshot.LogRecordInfo info = snapshot.getLogInfoForValue(value);
                    String message = null;
                    if (info != null) {
                        message = info.getDisplayName();
                        if (message == null) message = info.getName();
                        if (message == null) message = "<unknown>";
                    }
                    return message != null ? message : "<none>";
                case FORMAT_UNITS:
                    return "";
                default:
                    return null;
            }
        }

        public String getUnits(int format) {
            return "";
        }

    }

}
