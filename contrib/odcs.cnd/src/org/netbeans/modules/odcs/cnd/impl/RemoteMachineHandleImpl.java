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
package org.netbeans.modules.odcs.cnd.impl;

import java.net.URL;
import javax.swing.Action;
import org.netbeans.modules.odcs.cnd.actions.AddRemoteHostAction;
import org.netbeans.modules.odcs.cnd.actions.ChangeVMStateAction;
import org.netbeans.modules.odcs.cnd.actions.PropertiesAction;
import org.netbeans.modules.odcs.cnd.json.VMDescriptor;
import org.netbeans.modules.team.server.ui.spi.RemoteMachineHandle;

/**
 *
 */
public class RemoteMachineHandleImpl extends RemoteMachineHandle {

    private final String name;
    private final String url;
    private final URL serverUrl;
    private final VMDescriptor desc;

    public RemoteMachineHandleImpl(URL serverUrl, VMDescriptor desc) {
        this.serverUrl = serverUrl;
        this.desc = desc;
        this.name = desc.getDisplayName();
        this.url = desc.getHost();
    }

    @Override
    public String getDisplayName() {
        return name; // NOI18N
    }

    @Override
    public Action getDefaultAction() {
        return new AddRemoteHostAction(serverUrl.toExternalForm(), desc.getMachineId());
    }

    @Override
    public Action getPropertiesAction() {
        return new PropertiesAction(desc);
    }

    @Override
    public Action[] getAdditionalActions() {
        return new Action[]{
            getDefaultAction(),
            ChangeVMStateAction.startedAction(serverUrl.toExternalForm(), desc.getMachineId()),
            ChangeVMStateAction.stoppedAction(serverUrl.toExternalForm(), desc.getMachineId())};
    }

    @Override
    public String getState() {
        return desc.getState();
    }

    @Override
    public String getStateDetail() {
        return desc.getStateDetail();
    }
}
