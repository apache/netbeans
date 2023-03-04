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

package org.netbeans.modules.nativeexecution.api.util;

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 * @author Egor Ushakov
 */
public class EnvUtils {
    private EnvUtils() {
        
    }

    public static String getKey(String envEntry) {
        int idx = envEntry.indexOf('=');
        if (idx != -1) {
            return envEntry.substring(0, idx);
        } else {
            return envEntry;
        }
    }

    public static String getValue(String envEntry) {
        int idx = envEntry.indexOf('=');
        if (idx != -1) {
            return envEntry.substring(idx + 1);
        } else {
            return "";
        }
    }

    public static String toHostID(ExecutionEnvironment env) {
        return env.getHost() + "_" + env.getSSHPort(); // NOI18N
    }
}
