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

import java.util.List;
import java.util.stream.Collectors;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.api.ODCSServer;
import org.netbeans.modules.odcs.cnd.http.HttpClientAdapter;
import org.netbeans.modules.odcs.cnd.http.HttpClientAdapterFactory;
import org.netbeans.modules.odcs.cnd.json.VMList;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.netbeans.modules.team.server.ui.spi.RemoteMachineAccessor;
import org.netbeans.modules.team.server.ui.spi.RemoteMachineHandle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = RemoteMachineAccessor.class)
public class RemoteMachineAccessorImpl extends RemoteMachineAccessor<ODCSProject> {

    private static final String REST_URL_GET_VMS = "/api/cc/vms"; // NOI18N

    @Override
    public Class<ODCSProject> type() {
        return ODCSProject.class;
    }

    @Override
    public boolean hasRemoteMachines(ProjectHandle<ODCSProject> project) {
        // XXX should check if there are any for the given project
        return true;
    }

    @Override
    public List<RemoteMachineHandle> getRemoteMachines(ProjectHandle<ODCSProject> project) {
        if (!hasRemoteMachines(project)) {
            return null;
        }

        ODCSServer server = project.getTeamProject().getServer();

        // TODO clear
//        server.addPropertyChangeListener(ODCSServer.PROP_LOGIN, (PropertyChangeEvent evt) -> {
//            if (evt.getNewValue() == null) {
//                clients.remove(evt.)
//            }
//        });
        HttpClientAdapter client = HttpClientAdapterFactory.create(server.getUrl().toExternalForm(), server.getPasswordAuthentication());

        VMList vms = client.getForObject(server.getUrl() + REST_URL_GET_VMS, VMList.class);

        List<RemoteMachineHandle> result = vms.getMapList()
                .stream()
                .map(desc -> new RemoteMachineHandleImpl(server.getUrl(), desc))
                .collect(Collectors.toList());

        return result;
    }
}
