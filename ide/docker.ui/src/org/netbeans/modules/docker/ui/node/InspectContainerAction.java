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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.netbeans.modules.docker.api.DockerAction;
import org.netbeans.modules.docker.api.DockerException;
import org.netbeans.modules.docker.api.DockerInstanceEntity;
import org.netbeans.modules.docker.ui.JsonFormattingWriter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author ihsahn
 */
public class InspectContainerAction extends NodeAction {

    private static final Logger LOGGER = Logger.getLogger(InspectContainerAction.class.getName());
    private final RequestProcessor requestProcessor = new RequestProcessor(InspectContainerAction.class);

    @NbBundle.Messages("LBL_InspectAction=Inspect")
    @Override
    public String getName() {
        return Bundle.LBL_InspectAction();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        for (Node node : activatedNodes) {
            DockerInstanceEntity entity = node.getLookup().lookup(DockerInstanceEntity.class);
            if (entity == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        for (final Node node : activatedNodes) {
            DockerInstanceEntity container = node.getLookup().lookup(DockerInstanceEntity.class);
            if (container != null) {
                requestProcessor.post(new InspectRunnable(container));
            }
        }
    }

    private static class InspectRunnable implements Runnable {

        private final DockerInstanceEntity container;

        public InspectRunnable(DockerInstanceEntity container) {
            this.container = container;
        }

        @NbBundle.Messages({
            "# {0} - container id",
            "MSG_Inspecting=Inspecting container {0}"
        })
        @Override
        public void run() {
            JsonFormattingWriter formattedJsonWriter = new JsonFormattingWriter(2);

            try {
                DockerAction facade = new DockerAction(container.getInstance());
                JSONObject rawDetails = facade.getRawDetails(container.getType(), container.getId());

                rawDetails.writeJSONString(formattedJsonWriter);

                InputOutput io = IOProvider.getDefault().getIO(Bundle.MSG_Inspecting(container.getShortId()), false);
                io.getOut().reset();
                io.getOut().println(formattedJsonWriter.toString());
                io.getOut().close();
                io.getErr().close();
                io.select();
            } catch (IOException | DockerException ex) {
                LOGGER.log(Level.INFO, null, ex);
                String msg = ex.getLocalizedMessage();
                NotifyDescriptor desc = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            }
        }
    }

}
