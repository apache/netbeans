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

import java.io.IOException;
import org.json.simple.JSONObject;
import org.netbeans.modules.docker.api.DockerAction;
import org.netbeans.modules.docker.api.DockerContainer;
import org.netbeans.modules.docker.api.DockerContainerDetail;
import org.netbeans.modules.docker.api.DockerException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author ihsahn
 */
public class ProcessListAction extends AbstractContainerAction {

    private static final String PROCESSES_JSON_KEY = "Processes";
    private static final String TITLES_JSON_KEY = "Titles";

    @NbBundle.Messages("LBL_ProcessListAction=List running processes")
    public ProcessListAction() {
        super(Bundle.LBL_ProcessListAction());
    }

    @NbBundle.Messages({
        "# {0} - container id",
        "MSG_ProcessList=Processes in {0}"
    })
    @Override
    protected void performAction(DockerContainer container) throws DockerException {
        try {
            DockerAction facade = new DockerAction(container.getInstance());
            JSONObject processList = facade.getRunningProcessesList(container);

            InputOutput io = IOProvider.getDefault().getIO(Bundle.MSG_ProcessList(container.getShortId()), false);
            io.getOut().reset();
            io.getOut().println(processList.get(TITLES_JSON_KEY).toString());
            io.getOut().println(processList.get(PROCESSES_JSON_KEY).toString());
            io.getOut().close();
            io.getErr().close();
            io.select();
        } catch (IOException ex) {
            throw new DockerException(ex);
        }
    }

    @NbBundle.Messages({
        "# {0} - container id",
        "MSG_ListingProcessesInContainer=Listing running processes in container {0}"
    })
    @Override
    protected String getProgressMessage(DockerContainer container) {
        return Bundle.MSG_ListingProcessesInContainer(container.getShortId());
    }

    @Override
    protected boolean isEnabled(DockerContainerDetail detail) {
        return detail.getStatus() == DockerContainer.Status.RUNNING;
    }
}
