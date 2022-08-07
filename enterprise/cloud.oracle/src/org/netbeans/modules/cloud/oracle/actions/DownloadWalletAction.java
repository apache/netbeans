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
package org.netbeans.modules.cloud.oracle.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.database.DatabaseItem;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@ActionID(
        category = "Tools",
        id = "org.netbeans.modules.cloud.oracle.actions.DownloadWalletAction"
)
@ActionRegistration( 
        displayName = "#CTL_DownloadWalletAction", 
        asynchronous = true
)

@ActionReferences(value = {
    @ActionReference(path = "Cloud/Oracle/Databases/Actions", position = 250)
})
@NbBundle.Messages({
    "LBL_SaveWallet=Save DB Wallet",
    "CTL_DownloadWalletAction=Download Wallet",
    "MSG_WalletDownloaded=Database Wallet was downloaded to {0}",
    "MSG_WalletDownloadedPassword=Database Wallet was downloaded. \nGenerated wallet password is: {0}",
    "MSG_WalletNoConnection=Wallet doesn't contain any connection"
})
public class DownloadWalletAction implements ActionListener {
    
    private static final String URL_TEMPLATE = "jdbc:oracle:thin:@{0}?TNS_ADMIN=\"{1}\""; //NOI18N
    private final DatabaseItem context;

    public DownloadWalletAction(DatabaseItem context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        Optional<DownloadWalletDialog.WalletInfo> result = DownloadWalletDialog.showDialog(context);
        result.ifPresent((p) -> {
            try {
                Path walletPath = OCIManager.getDefault().downloadWallet(context, new String(p.getWalletPassword()), p.getPath());
                if (p.getDbUser() != null && p.getDbPassword() != null) {
                    
                    JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers("oracle.jdbc.OracleDriver"); //NOI18N
                    if (drivers.length > 0) {
                        String connectionName = context.getConnectionName();
                        if (connectionName == null) {
                            Optional<String> n = parseConnectionNames(walletPath).stream().findFirst();
                            if (n.isPresent()) {
                                connectionName = n.get();
                            } else {
                                StatusDisplayer.getDefault().setStatusText(Bundle.MSG_WalletNoConnection());
                                return;
                            }
                        }
                        String dbUrl = MessageFormat.format(URL_TEMPLATE, connectionName, walletPath);
                        DatabaseConnection dbConn = DatabaseConnection.create(
                                drivers[0], 
                                dbUrl, 
                                p.getDbUser(), 
                                p.getDbUser(), 
                                new String(p.getDbPassword()), 
                                true, 
                                context.getName());
                        ConnectionManager.getDefault().addConnection(dbConn);
                    }
                    DialogDisplayer.getDefault().notifyLater(
                            new NotifyDescriptor.Message(
                                    Bundle.MSG_WalletDownloadedPassword(
                                            new String(p.getWalletPassword()))));
                } else {
                    StatusDisplayer.getDefault().setStatusText(Bundle.MSG_WalletDownloaded(walletPath.toString()));
                }
            } catch (DatabaseException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }
    
    protected List<String> parseConnectionNames(Path wallet) {
        Path tns = wallet.resolve("tnsnames.ora"); //NOI18N
        try {
            return Files.newBufferedReader(tns).lines()
                    .filter(l -> l.contains("=")) //NOI18N
                    .map(l -> l.substring(0, l.indexOf("=")).trim()) //NOI18N
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.emptyList();
    }
}
