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
package org.netbeans.modules.docker.ui.node;

import org.netbeans.modules.docker.api.DockerContainer;
import org.netbeans.modules.docker.api.DockerException;
import org.netbeans.modules.docker.api.DockerAction;
import org.netbeans.modules.docker.api.DockerContainerDetail;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class RemoveContainerAction extends AbstractContainerAction {

    @NbBundle.Messages("LBL_RemoveContainerAction=Remove")
    public RemoveContainerAction() {
        super(Bundle.LBL_RemoveContainerAction());
    }

    @NbBundle.Messages({
        "# {0} - container id",
        "MSG_RemovingContainer=Removing container {0}"
    })
    @Override
    protected String getProgressMessage(DockerContainer container) {
        return Bundle.MSG_RemovingContainer(container.getShortId());
    }

    @Override
    protected void performAction(DockerContainer container) throws DockerException {
        DockerAction facade = new DockerAction(container.getInstance());
        facade.remove(container);
    }

    @Override
    protected boolean isEnabled(DockerContainerDetail detail) {
        return detail.getStatus() == DockerContainer.Status.STOPPED;
    }

}
