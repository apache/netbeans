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

import java.io.InputStream;

/**
 *
 * @author Tomas Hurka
 */
public class Histogram {
    private static Boolean initialized;
    private static boolean runningOnJdk9;
    
    public static boolean isAvailable() {
        if (initialized != null) {
            return initialized.booleanValue();
        }
        return false;
    }
    
    public static boolean initialize(boolean jdk9) {
        runningOnJdk9 = jdk9;
        if (runningOnJdk9) {
            initialized = Boolean.valueOf(Histogram19.initialize());            
        } else {
            initialized = Boolean.valueOf(Histogram18.initialize());
        }
        return initialized.booleanValue();
    }
    
    public static InputStream getRawHistogram() {
        if (runningOnJdk9) {
            return Histogram19.getRawHistogram();
        }
        return Histogram18.getRawHistogram();
    }
}
