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

package org.netbeans.lib.profiler.server.system;

/**
 *
 * @author Tomas Hurka
 */
public class HeapDump {

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    private HeapDump() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static void initialize(boolean jdk15) {
    }

    public static String takeHeapDump(String outputFile) {
        return takeHeapDumpCVM(outputFile);
    }


    private static String takeHeapDumpCVM(String outputFile) {
        return "Take heap dump is not available."; // NOI18N
    }
}
