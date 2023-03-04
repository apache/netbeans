/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.db.explorer.action;

import java.util.logging.Logger;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseConnector;
import org.netbeans.modules.db.explorer.node.SchemaNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Rob Englander
 */
public class MakeDefaultSchemaAction extends BaseAction {
    private static final Logger LOGGER = Logger.getLogger(MakeDefaultSchemaAction.class.getName());

    @Override
    public String getName() {
        return NbBundle.getMessage (MakeDefaultSchemaAction.class, "MakeDefaultSchema"); // NOI18N
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean result = false;
        
        if (activatedNodes.length == 1) {
            SchemaNode node = activatedNodes[0].getLookup().lookup(SchemaNode.class);
            if (node != null) {
                DatabaseConnector connector = node.getLookup().lookup(DatabaseConnection.class).getConnector();
                result = connector.supportsCommand(Specification.DEFAULT_SCHEMA);
            }
        }

        return result;
    }

    @Override
    protected void performAction(final Node[] activatedNodes) {
        RequestProcessor.getDefault().post(
            new Runnable() {
                @Override
                public void run() {
                    DatabaseConnection connection = activatedNodes[0].getLookup().lookup(DatabaseConnection.class);
                    String name = activatedNodes[0].getLookup().lookup(SchemaNode.class).getName();

                    try {
                        connection.setDefaultSchema(name);
                        connection.setSchema(name);
                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            }
        );
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(MakeDefaultSchemaAction.class);
    }
}
