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

import javax.swing.ImageIcon;
import org.netbeans.modules.odcs.cnd.http.HttpClientAdapter;
import org.netbeans.modules.odcs.cnd.http.HttpClientAdapterFactory;
import org.netbeans.modules.odcs.cnd.json.VMDescriptor;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 */
public final class DevelopVMExecutionClient {

    private static final String VM_DESCRIPTOR_URL = "/api/cc/vms/";

    private final DevelopVMExecutionEnvironment env;

    public DevelopVMExecutionClient(DevelopVMExecutionEnvironment env) {
        this.env = env;
    }

    @NbBundle.Messages({
        "connection_title=Connection to DCS required",
        "connection_text=Connection to {0} can be restored after login to {1}"
    })
    public VMDescriptor getVMDescriptor() {
        HttpClientAdapter client = HttpClientAdapterFactory.get(env.getServerUrl());

        VMDescriptor descriptor = client.getForObject(env.getServerUrl() + VM_DESCRIPTOR_URL + env.getMachineId(), VMDescriptor.class, "REST - Get host info - " + env.getMachineId());

        return descriptor;
    }
}
