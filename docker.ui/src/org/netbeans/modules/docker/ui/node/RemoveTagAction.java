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
import org.netbeans.modules.docker.api.DockerInstance;
import org.netbeans.modules.docker.api.DockerTag;
import org.netbeans.modules.docker.api.DockerAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Petr Hejl
 */
public class RemoveTagAction extends NodeAction {

    private static final Logger LOGGER = Logger.getLogger(RemoveTagAction.class.getName());

    private final RequestProcessor requestProcessor = new RequestProcessor(RemoveTagAction.class);

    @NbBundle.Messages({
        "# {0} - image id",
        "MSG_RemovingTag=Removing image {0}"
    })
    @Override
    protected void performAction(Node[] activatedNodes) {
        for (final Node node : activatedNodes) {
            final DockerTag tag = node.getLookup().lookup(DockerTag.class);
            if (tag != null) {
                final ProgressHandle handle = ProgressHandle.createHandle(Bundle.MSG_RemovingTag(tag.getShortId()));
                handle.start();
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DockerInstance instance = tag.getImage().getInstance();
                            DockerAction facade = new DockerAction(instance);
                            facade.remove(tag);
                        } catch (Exception ex) {
                            // FIXME offer force remove ?
                            LOGGER.log(Level.INFO, null, ex);
                            String msg = ex.getLocalizedMessage();
                            NotifyDescriptor desc = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify(desc);
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
    protected boolean enable(Node[] activatedNodes) {
        for (Node node : activatedNodes) {
            if (node.getLookup().lookup(DockerTag.class) == null) {
                return false;
            }
        }
        return true;
    }

    @NbBundle.Messages("LBL_RemoveTagAction=Remove")
    @Override
    public String getName() {
        return Bundle.LBL_RemoveTagAction();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

}
