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

import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.node.ToggleImportantInfo;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Havlin
 */
public class ToggleImportantAction extends BaseAction {

    private String actionDisplayName;

    @Override
    public String getName() {
        return actionDisplayName;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean toggleImportant = false;
        boolean toggleUnImportant = false;

        for (Node node : activatedNodes) {
            ToggleImportantInfo tii = node.getLookup().lookup(ToggleImportantInfo.class);
            if (tii != null) {
                if (!tii.isDefault()) {
                    if (tii.isImportant()) {
                        toggleUnImportant = true;
                    } else {
                        toggleImportant = true;
                    }
                }
            }
        }

        if (toggleUnImportant && toggleImportant) {
            actionDisplayName = NbBundle.getMessage(ToggleImportantAction.class,
                    "ToggleImportant");                                 //NOI18N
            return true;
        } else if (toggleUnImportant) {
            actionDisplayName = NbBundle.getMessage(ToggleImportantAction.class,
                    "ToggleImportantRemove");                           //NOI18N
            return true;
        } else if (toggleImportant) {
            actionDisplayName = NbBundle.getMessage(ToggleImportantAction.class,
                    "ToggleImportantAdd");                              //NOI18N
            return true;
        }
        actionDisplayName = NbBundle.getMessage(ToggleImportantAction.class,
                "ToggleImportant");                                     //NOI18N

        return false;
    }

    @Override
    protected void performAction(final Node[] activatedNodes) {

        for (Node node : activatedNodes) {
            DatabaseConnection conn = node.getLookup().lookup(DatabaseConnection.class);
            ToggleImportantInfo tii = node.getLookup().lookup(ToggleImportantInfo.class);
            if (conn != null && tii != null
                    && Catalog.class.isAssignableFrom(tii.getType())) {
                String name = node.getName();
                if (name.equals(conn.getDefaultCatalog())) {
                    tii.setDefault(true);
                    tii.setImportant(false);
                    conn.removeImportantCatalog(name);
                } else if (!conn.isImportantCatalog(name)) {
                    conn.addImportantCatalog(name);
                    tii.setDefault(false);
                    tii.setImportant(true);
                } else {
                    conn.removeImportantCatalog(name);
                    tii.setDefault(false);
                    tii.setImportant(false);
                }
            } else if (conn != null && tii != null
                    && Schema.class.isAssignableFrom(tii.getType())) {
                String name = node.getName();
                if (name.equals(conn.getDefaultSchema())) {
                    tii.setDefault(true);
                    tii.setImportant(false);
                    conn.removeImportantSchema(name);
                } else if (!conn.isImportantSchema(name)) {
                    conn.addImportantSchema(name);
                    tii.setDefault(false);
                    tii.setImportant(true);
                } else {
                    conn.removeImportantSchema(name);
                    tii.setDefault(false);
                    tii.setImportant(false);
                }
            }
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
