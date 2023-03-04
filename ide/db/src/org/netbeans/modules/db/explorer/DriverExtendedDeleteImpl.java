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
import java.text.MessageFormat;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.modules.db.explorer.node.DriverNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class DriverExtendedDeleteImpl {

    public static boolean delete(Node[] nodes) throws IOException {
        JDBCDriver[] jdbcDrivers = getJDBCDrivers(nodes);
        if (jdbcDrivers == null) {
            return false;
        }
        DatabaseConnection firstConnection = findFirstConnection(jdbcDrivers);
        if (firstConnection == null) {
            return false;
        }
        if (!canDeleteDrivers(jdbcDrivers, firstConnection)) {
            return true;
        }
        for (int i = 0; i < nodes.length; i++) {
            try {
                nodes[i].destroy();
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return true;
    }

    /**
     * Returns the drivers represented by the given nodes,
     * or null if not all nodes represent drivers.
     */
    private static JDBCDriver[] getJDBCDrivers(Node[] nodes) {
        JDBCDriver[] jdbcDrivers = new JDBCDriver[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            DriverNode driverInfo = nodes[i].getLookup().lookup(DriverNode.class);
            if (driverInfo == null) {
                return null;
            }
            jdbcDrivers[i] = driverInfo.getDatabaseDriver().getJDBCDriver();
            if (jdbcDrivers[i] == null) {
                return null;
            }
        }
        return jdbcDrivers;
    }

    /**
     * Returns true if at least one of the given drivers is used by
     * a registered connection.
     */
    private static DatabaseConnection findFirstConnection(JDBCDriver[] jdbcDrivers) {
        DatabaseConnection[] dbconns = ConnectionList.getDefault().getConnections();
        for (int i = 0; i < jdbcDrivers.length; i++) {
            // first try to find connections which refer to this driver by name
            for (int j = 0; j < dbconns.length; j++) {
                if (jdbcDrivers[i].getName().equals(dbconns[j].getDriverName())) {
                    return dbconns[j];
                }
            }
            // ... no such connection, but the driver might still be referred to by class
            // (e.g., after removing the driver for a certain class and registering it back
            // with the same class, but a different name)
            for (int j = 0; j < dbconns.length; j++) {
                if (jdbcDrivers[i].getClassName().equals(dbconns[j].getDriver())) {
                    return dbconns[j];
                }
            }
        }
        return null;
    }

    private static boolean canDeleteDrivers(JDBCDriver[] jdbcDrivers, DatabaseConnection firstConnection) {
        String message, title;
        if (jdbcDrivers.length == 1) {
            String format = NbBundle.getMessage (DriverExtendedDeleteImpl.class, "MSG_ConfirmDeleteDriver"); // NOI18N
            message = MessageFormat.format(format, new Object[] { jdbcDrivers[0].getDisplayName(), firstConnection.getDatabaseConnection().getDisplayName() });
            title = NbBundle.getMessage (DriverExtendedDeleteImpl.class, "MSG_ConfirmDeleteDriverTitle"); // NOI18N
        } else {
            String format = NbBundle.getMessage (DriverExtendedDeleteImpl.class, "MSG_ConfirmDeleteDrivers"); // NOI18N
            message = MessageFormat.format(format, new Object[] { Integer.valueOf(jdbcDrivers.length) });
            title = NbBundle.getMessage (DriverExtendedDeleteImpl.class, "MSG_ConfirmDeleteDriversTitle"); // NOI18N
        }
        return DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(message, title, NotifyDescriptor.YES_NO_OPTION)) == NotifyDescriptor.YES_OPTION;
    }
}
