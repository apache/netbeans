/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.odcs.cnd.execution;

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 */
public abstract class DevelopVMExecutionEnvironment implements ExecutionEnvironment {

    public static final String CLOUD_PREFIX = "cloud.oracle";

    public abstract String getServerUrl();

    public abstract String getMachineId();

    public static String encode(String user, String machineId, int port, String serverUrl) {
        return String.format("%s://%s@%s:%d@%s", CLOUD_PREFIX, user, machineId, port, serverUrl);
    }

    public static DevelopVMExecutionEnvironment decode(String hostKey) {
        String userAtmachineAtHost = hostKey.substring((CLOUD_PREFIX + "://").length());

        String[] split = userAtmachineAtHost.split("@", 3);

        String user = split[0];
        String machineAndPort = split[1];
        String host = split[2];

        String machineId = machineAndPort.split(":")[0];
        int port = Integer.valueOf(machineAndPort.split(":")[1]);

        return new DevelopVMExecutionEnvironmentImpl(user, machineId, port, host);
    }

    public abstract void initializeOrWait();
}
