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
package org.netbeans.modules.odcs.cnd.actions;

import org.netbeans.modules.odcs.cnd.json.misc.State;
import org.netbeans.modules.odcs.cnd.json.misc.Response;
import java.awt.event.ActionEvent;
import org.netbeans.modules.odcs.cnd.http.HttpClientAdapter;
import org.openide.util.NbBundle;

/**
 *
 */
public class ChangeVMStateAction extends RestAction {

    private static final String URL_TEMPLATE = "api/cc/vms/{0}/state"; // NOI18N
    private final String machineId;
    private final State state;
    private final String actionName;

    public ChangeVMStateAction(String serverUrl, String machineId, String state, String actionName) {
        super(serverUrl, actionName);
        this.machineId = machineId;
        this.state = new State(state);
        this.actionName = actionName;
    }

    @Override
    public void actionPerformedImpl(HttpClientAdapter client, ActionEvent e) {
        Response response = client.postForObject(getRestUrl(), Response.class, state, "REST - Change VM state");

        System.out.println(response.isSuccess());
    }

    @Override
    public String getRestUrl() {
        return String.join("/", getServerUrl(), formatUrl(URL_TEMPLATE, machineId));
    }

    @NbBundle.Messages({
        "remotevm.startvm.action.text=Start VM"
    })
    public static ChangeVMStateAction startedAction(String serverUrl, String machineId) {
        return new ChangeVMStateAction(serverUrl, machineId, "STARTED", Bundle.remotevm_startvm_action_text()); // NOI18N
    }

    @NbBundle.Messages({
        "remotevm.stopvm.action.text=Stop VM"
    })
    public static ChangeVMStateAction stoppedAction(String serverUrl, String machineId) {
        return new ChangeVMStateAction(serverUrl, machineId, "STOPPED", Bundle.remotevm_stopvm_action_text()); // NOI18N
    }
}
