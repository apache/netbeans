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
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.OCIProfile;
import org.netbeans.modules.cloud.oracle.database.DatabaseItem;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.URLMapper;
import org.openide.util.BaseUtilities;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
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
        asynchronous = true,
        lazy = true
)

@ActionReferences(value = {
    @ActionReference(path = "Cloud/Oracle/Databases/Actions", position = 250)
})
@NbBundle.Messages({
    "LBL_SaveWallet=Save DB Wallet",
    "CTL_DownloadWalletAction=Download Wallet",
    "MSG_WalletDownloaded=Database Wallet was downloaded to {0}",
    "MSG_WalletDownloadedPassword=Database Wallet was downloaded. \nGenerated wallet password is: {0}",
    "MSG_WalletNoConnection=Wallet doesn't contain any connection",
    "WARN_DriverWithoutJars=No matching JDBC drivers are configured with code location(s). Driver {0} will be associated with the connection, but the " +
            "connection may fail because driver's code is not loadable. Continue ?"
    
})
public class DownloadWalletAction extends AbstractAction implements ContextAwareAction {
    private static final Logger LOG = Logger.getLogger(DownloadWalletAction.class.getName());
    
    private static final String URL_TEMPLATE = "jdbc:oracle:thin:@{0}?TNS_ADMIN={1}"; //NOI18N
    private final DatabaseItem context;
    private OCIProfile session;

    public DownloadWalletAction(DatabaseItem context) {
        this.context = context;
        this.session = OCIManager.getDefault().getActiveProfile();
    }

    DownloadWalletAction(OCIProfile session, DatabaseItem context) {
        this.context = context;
        this.session = session;
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        OCIProfile session = actionContext.lookup(OCIProfile.class);
        return new DownloadWalletAction(session, context);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        Optional<DownloadWalletDialog.WalletInfo> result = DownloadWalletDialog.showDialog(context);
        result.ifPresent(p ->  addConnection(p));
    }

    void addConnection(DownloadWalletDialog.WalletInfo p) {
        try {
            Path walletPath = session.downloadWallet(context, new String(p.getWalletPassword()), p.getPath());
            if (p.getDbUser() != null && p.getDbPassword() != null) {

                JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers("oracle.jdbc.OracleDriver"); //NOI18N
                JDBCDriver jarsPresent = null;

                if (drivers.length > 0) {

                    // prefer a driver that actually defines some JARs.
                    for (JDBCDriver d : drivers) {
                        if (isAvailable(d)) {
                            jarsPresent = d;
                            break;
                        }
                    }
                    if (jarsPresent == null) {
                        jarsPresent = drivers[0];
                        LOG.log(Level.WARNING, "Unable to find driver JARs for wallet {0}, using fallback driver: {1}", new Object[] { walletPath, jarsPresent.getName() });
                        NotifyDescriptor.Confirmation msg = new NotifyDescriptor.Confirmation(Bundle.WARN_DriverWithoutJars(jarsPresent.getName()), 
                            NotifyDescriptor.WARNING_MESSAGE, NotifyDescriptor.YES_NO_OPTION);
                        Object choice = DialogDisplayer.getDefault().notify(msg);
                        if (choice != NotifyDescriptor.YES_OPTION && choice != NotifyDescriptor.OK_OPTION) {
                            return;
                        }
                    }
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
                    Properties props = new Properties();
                    props.put("OCID", p.getOcid()); //NOI18N
                    props.put("CompartmentOCID", p.getComaprtmentId()); //NOI18N
                    String dbUrl = MessageFormat.format(URL_TEMPLATE, connectionName, BaseUtilities.escapeParameters(new String[] { walletPath.toString() }));
                    DatabaseConnection dbConn = DatabaseConnection.create(
                            drivers[0], 
                            dbUrl, 
                            p.getDbUser(), 
                            p.getDbUser().toUpperCase(), 
                            new String(p.getDbPassword()), 
                            true, 
                            context.getName(),
                            props);
                    ConnectionManager.getDefault().addConnection(dbConn);
                }

                // PENDING: what should happen, if the driver is not found at all - display an info message ?

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
    }
    
    static boolean isAvailable(JDBCDriver driver) {
        URL[] urls = driver.getURLs();
        for (URL u : urls) {
            if (URLMapper.findFileObject(u) == null) {
                return false;
            }
        }
        if (urls.length > 0) {
            // true, some jar is defined && exists.
            return true;
        } else {
            // if the JDBC drive does not list jars, its class must be reachable. DbDriverManager uses no-arg classloader constructor, so it is
            // using systemClassLoader as a parent for no-URL URLClassloader.
            try {
                Class.forName(driver.getClassName(), true, ClassLoader.getSystemClassLoader());
                return true;
            } catch (ClassNotFoundException | SecurityException | LinkageError ex) {
                // expected, class is not avaialble
                return false;
            }
        }
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
