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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.docker.api.DockerContainer;
import org.netbeans.modules.docker.api.DockerContainerDetail;
import org.netbeans.modules.docker.api.DockerException;
import org.netbeans.modules.docker.ui.output.ExceptionHandler;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Petr Hejl
 */
public abstract class AbstractContainerAction extends NodeAction implements ExceptionHandler {

    private static final Logger LOGGER = Logger.getLogger(AbstractContainerAction.class.getName());

    private final RequestProcessor requestProcessor = new RequestProcessor(AbstractContainerAction.class);

    private final String name;

    public AbstractContainerAction(String name) {
        this.name = name;
    }

    protected abstract void performAction(DockerContainer container) throws DockerException;

    protected abstract String getProgressMessage(DockerContainer container);

    protected boolean isEnabled(DockerContainerDetail detail) {
        return true;
    }

    @Override
    protected final void performAction(Node[] activatedNodes) {
        for (final Node node : activatedNodes) {
            final StatefulDockerContainer container = node.getLookup().lookup(StatefulDockerContainer.class);
            if (container != null) {
                final ProgressHandle handle = ProgressHandle.createHandle(getProgressMessage(container.getContainer()));
                handle.start();
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            performAction(container.getContainer());
                        } catch (Exception ex) {
                            handleException(ex);
                        } finally {
                            handle.finish();
                        }
                    }
                };
                requestProcessor.post(task);
            }
        }
    }

    @Override
    protected final boolean enable(Node[] activatedNodes) {
        for (Node node : activatedNodes) {
            StatefulDockerContainer container = node.getLookup().lookup(StatefulDockerContainer.class);
            if (container == null || !isEnabled(container.getDetail())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected final boolean asynchronous() {
        return false;
    }

    @Override
    public final void handleException(Exception ex) {
        LOGGER.log(Level.INFO, null, ex);
        String msg = ex.getLocalizedMessage();
        NotifyDescriptor desc = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(desc);
    }
}
