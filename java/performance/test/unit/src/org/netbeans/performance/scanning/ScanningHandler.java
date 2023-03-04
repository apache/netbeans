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
package org.netbeans.performance.scanning;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.netbeans.junit.NbPerformanceTest.PerformanceData;

/**
 *
 * @author petr
 */
class ScanningHandler extends Handler {

    private final Map<String, PerformanceData> data;

    Collection<PerformanceData> getData() {
        return data.values();
    }

    void clear() {
        data.clear();
    }
    
    static enum ScanType {

        INITIAL(" initial "),
        UP_TO_DATE(" up-to-date ");

        private final String name;

        private ScanType(String name) {
            this.name = name;
        }

        private String getName() {
            return name;
        }
    }

    private final String projectName;
    private ScanType type;
    private final long thBinaryScan;
    private final long thSourceScan;
    private final long thBinaryUpdate;
    private final long thSourceUpdate;

    public ScanningHandler(String projectName, long thBinaryScan, long thSourceScan, long thBinaryUpdate, long thSourceUpdate) {
        this.projectName = projectName;
        this.type = ScanType.INITIAL;
        this.thBinaryScan = thBinaryScan;
        this.thSourceScan = thSourceScan;
        this.thBinaryUpdate = thBinaryUpdate;
        this.thSourceUpdate = thSourceUpdate;
        data = new HashMap<>();
    }

    public void setType(ScanType type) {
        this.type = type;
    }

    @Override
    public void publish(LogRecord record) {
        String message = record.getMessage();
        if (message != null && message.startsWith("Complete indexing")) {
            String name = null;
            long value = 0;
            long threshold = 0;
            if (message.contains("source roots")) {
                if (record.getParameters()[0].hashCode() > 0) {
                    name = projectName + type.getName() + "source scan";
                    value = record.getParameters()[1].hashCode();
                    if (type.equals(ScanType.INITIAL)) {
                        threshold = thSourceScan;
                    } else {
                        threshold = thSourceUpdate;
                    }
                }
            } else if (message.contains("binary roots")) {
                if (record.getParameters()[0].hashCode() > 0) {
                    name = projectName + type.getName() + "binary scan";
                    value = record.getParameters()[1].hashCode();
                    if (type.equals(ScanType.INITIAL)) {
                        threshold = thBinaryScan;
                    } else {
                        threshold = thBinaryUpdate;
                    }
                }
            }
            if (name != null && value > 0) {
                PerformanceData newData = data.get(name);
                if (newData == null) {
                    newData = new PerformanceData();
                    newData.name = name;
                    newData.value = value;
                    newData.unit = "ms";
                    newData.threshold = threshold;
                    newData.runOrder = 0;
                    data.put(name, newData);
                } else {
                    newData.value = newData.value + value;
                }
            }
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
}
