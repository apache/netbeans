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
package org.netbeans.modules.db.explorer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.modules.db.explorer.node.ColumnNode;
import org.netbeans.modules.db.explorer.node.DriverNode;
import org.netbeans.modules.db.explorer.node.ProcedureNode;
import org.netbeans.modules.db.explorer.node.TableNode;
import org.netbeans.modules.db.explorer.node.ViewNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExtendedDelete;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
@org.openide.util.lookup.ServiceProvider(service = org.openide.explorer.ExtendedDelete.class)
public class DbExtendedDelete implements ExtendedDelete {
    private static final Class[] GUARDED_OBJECTS = new Class[] {DriverNode.class, TableNode.class, ViewNode.class, ColumnNode.class, ProcedureNode.class};

    public DbExtendedDelete() {}

    @Override
    public final boolean delete(Node[] nodes) throws IOException {
        if (nodes == null || nodes.length == 0) {
            return false;
        }
        List<String> tables = new ArrayList<> ();
        List<String> columns = new ArrayList<> ();
        List<String> others = new ArrayList<> ();
        List<DriverNode> drivers = new ArrayList<> ();
        for (Node n : nodes) {
            for (Class<? extends BaseNode> c : GUARDED_OBJECTS) {
                BaseNode bn = n.getLookup().lookup(c);
                if (bn instanceof TableNode) {
                    // table node
                    tables.add(bn.getDisplayName());
                } else if (bn instanceof ColumnNode) {
                    columns.add(bn.getDisplayName());
                } else if (bn instanceof DriverNode) {
                    // driver node
                    drivers.add((DriverNode) bn);
                } else if (bn != null) {
                    // others
                    others.add(bn.getDisplayName());
                }
            }
        }
        if (!tables.isEmpty()) {
            boolean preventDelete = preventDelete(tables,
                    NbBundle.getMessage(DbExtendedDelete.class, "DbExtendedDelete_ConfirmationMessage_Tables", tables.size()), // NOI18N
                    NbBundle.getMessage(DbExtendedDelete.class, "DbExtendedDelete_ConfirmationTitle_Tables", tables.size())); // NOI18N
            if (!preventDelete) {
                TableExtendedDelete.delete(nodes);
            }
            // return true to indicate, that deletion is always handled here
            return true;
        } else if (!columns.isEmpty()) {
            return preventDelete(columns,
                    NbBundle.getMessage(DbExtendedDelete.class, "DbExtendedDelete_ConfirmationMessage_Columns", columns.size()), // NOI18N
                    NbBundle.getMessage(DbExtendedDelete.class, "DbExtendedDelete_ConfirmationTitle_Columns", columns.size())); // NOI18N
        } else if (!others.isEmpty()) {
            return preventDelete(others,
                    NbBundle.getMessage(DbExtendedDelete.class, "DbExtendedDelete_ConfirmationMessage_Others", others.size()), // NOI18N
                    NbBundle.getMessage(DbExtendedDelete.class, "DbExtendedDelete_ConfirmationTitle_Others", others.size())); // NOI18N
        } else if (!drivers.isEmpty()) {
            return DriverExtendedDeleteImpl.delete(nodes);
        }
        return false;
    }

    private boolean preventDelete(List<String> objects, String type, String title) {
        // confirmation
        return DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(DbExtendedDelete.class, "DbExtendedDelete_ConfirmationMessage_DeleteObjects", type, formatNames(objects)), // msg
                    NbBundle.getMessage(DbExtendedDelete.class, "DbExtendedDelete_ConfirmationTitle_DeleteObjects", title), // title
                    NotifyDescriptor.YES_NO_OPTION)) != NotifyDescriptor.YES_OPTION;
    }

    private static String formatNames(List<String> names) {
        StringBuilder sb = new StringBuilder();
        for (String s : names) {
            if (sb.length() > 0) {
                sb.append(", "); // NOI18N
            }
            sb.append(s);
        }
        return sb.toString();
    }
}
