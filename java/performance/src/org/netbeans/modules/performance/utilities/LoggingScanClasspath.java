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
package org.netbeans.modules.performance.utilities;

import java.io.PrintStream;
import java.util.ArrayList;

/**
 *
 * @author mmirilovic@netbeans.org
 */
public class LoggingScanClasspath {

    private static ArrayList<PerformanceScanData> data = new ArrayList<PerformanceScanData>();

    /**
     * Creates a new instance of LoggingScanClasspath
     */
    public LoggingScanClasspath() {
    }

    public void reportScanOfFile(String file, Long measuredTime) {
        data.add(new PerformanceScanData(file, measuredTime));
    }

    public static ArrayList<PerformanceScanData> getData() {
        return data;
    }

    public static void printMeasuredValues(PrintStream ps) {
        for (PerformanceScanData performanceScanData : data) {
            ps.println(performanceScanData);
        }
    }

    public class PerformanceScanData {

        private String name;
        private long value;
        private String fullyQualifiedName;

        public PerformanceScanData() {
        }

        public PerformanceScanData(String name, Long value) {
            this(name, value, name);
        }

        public PerformanceScanData(String name, Long value, String fullyQualifiedName) {
            // jar:file:/path_to_jdk/src.zip!/
            // jar:file:/C:/path_jdk/src.zip!/
            // file:/path_to_project/jEdit41/src/

            int beginIndex;
            int endIndex;

            try {
                beginIndex = name.substring(0, name.lastIndexOf('/') - 1).lastIndexOf('/') + 1;
                endIndex = name.indexOf('!', beginIndex); // it's jar and it ends with '!/'
                if (endIndex == -1) { // it's directory and it ends with '/'
                    endIndex = name.length() - 1;
                    beginIndex = name.lastIndexOf('/', beginIndex - 2) + 1; // log "jedit41/src" not only "src" 
                }

                this.setName(name.substring(beginIndex, endIndex));
            } catch (Exception exc) {
                exc.printStackTrace(System.err);
                this.setName(name);
            }

            this.setValue(value.longValue());
            this.setFullyQualifiedName(fullyQualifiedName);
        }

        @Override
        public String toString() {
            return "name =[" + getName() + "] value=" + getValue() + " FQN=[" + getFullyQualifiedName() + "]";
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }

        public String getFullyQualifiedName() {
            return fullyQualifiedName;
        }

        public void setFullyQualifiedName(String fullyQualifiedName) {
            this.fullyQualifiedName = fullyQualifiedName;
        }

    }
}
