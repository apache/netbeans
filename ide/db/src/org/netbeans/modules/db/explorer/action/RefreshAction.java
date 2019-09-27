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

package org.netbeans.modules.db.explorer.action;

import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Rob
 */
public class RefreshAction extends BaseAction {
    private static final RequestProcessor RP = new RequestProcessor(RefreshAction.class);
    @Override
    public String getName() {
        return NbBundle.getMessage (RefreshAction.class, "Refresh"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(RefreshAction.class);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean enabled = false;

        if (activatedNodes.length == 1) {
            enabled = null != activatedNodes[0].getLookup().lookup(BaseNode.class);
        }

        return enabled;
    }

    @Override
    public void performAction(Node[] activatedNodes) {
        if (activatedNodes[0] == null) {
            return;
        }
        final BaseNode baseNode = activatedNodes[0].getLookup().lookup(BaseNode.class);
        RP.post(
            new Runnable() {
                @Override
                public void run() {
                    MetadataModel model = baseNode.getLookup().lookup(DatabaseConnection.class).getMetadataModel();
                    if (model != null) {
                        try {
                            model.runReadAction(
                                new Action<Metadata>() {
                                    @Override
                                    public void run(Metadata metaData) {
                                        metaData.refresh();
                                    }
                                }
                            );
                        } catch (MetadataModelException e) {
                            Exceptions.printStackTrace(e);
                        }
                    }

                    baseNode.refresh();
                }
            }
        );
    }

}
