/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
