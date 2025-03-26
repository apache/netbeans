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

package org.netbeans.modules.derby;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.derby.api.DerbyDatabases;
import org.netbeans.modules.derby.ui.SecurityManagerBugPanel;
import org.netbeans.spi.db.explorer.DatabaseRuntime;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.NotificationDisplayer;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;


/**
 *
 * @author  Ludo, Petr Jiricka
 */
public class RegisterDerby implements DatabaseRuntime {
    
    // XXX this class does too much. Should maybe be split into 
    // DatabaseRuntimeImpl, DerbyStartStop and the rest.
    
    // XXX refactor this soon, it is full of race conditions!
    
    private static final Logger LOGGER = Logger.getLogger(RegisterDerby.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    private static final String DISABLE_SECURITY_MANAGER
            = "disableSecurityManager";                                 //NOI18N
    private static final String DO_NOT_CHECK_SECURITY_MANAGER_BUG
            = "doNotCheckSecurityManagerBug";                           //NOI18N
    private static final String SECURITY_MANAGER_BUG_OUTPUT
            = "java.security.AccessControlException: "                  //NOI18N
            + "access denied (\"java.net.SocketPermission\"";           //NOI18N
    
    private static final int START_TIMEOUT = 0; // seconds
    
    private static RegisterDerby reg = null;
    private static DatabaseRuntime regModular = null;
    
    /** Derby server process */
    private static Process process = null;
    
    /** Creates a new instance of RegisterDerby */
    private RegisterDerby() {}
    
    public static synchronized RegisterDerby getDefault() {
        if (reg == null) {
            reg = new RegisterDerby();
            if (EventQueue.isDispatchThread()) { // #229741
                RequestProcessor.getDefault().post(new Runnable() {

                    @Override
                    public void run() {
                        DerbyActivator.activate();
                    }
                });
            } else {
                DerbyActivator.activate();
            }
        }
        return reg;
    }

    public static synchronized DatabaseRuntime getModular() {
        if (regModular == null) {
            RegisterDerby mainRuntime = getDefault();
            regModular = new DatabaseRuntime() {
                @Override
                public String getJDBCDriverClass() {
                    return DerbyOptions.DRIVER_CLASS_NET_MODULAR;
                }

                @Override
                public boolean acceptsDatabaseURL(String url) {
                    return mainRuntime.acceptsDatabaseURL(url);
                }

                @Override
                public boolean isRunning() {
                    return mainRuntime.isRunning();
                }

                @Override
                public boolean canStart() {
                    return mainRuntime.canStart();
                }

                @Override
                public void start() {
                    mainRuntime.start();
                }

                @Override
                public void stop() {
                    mainRuntime.stop();
                }
            };
        }
        return regModular;
    }

    /**
     * Whether this runtime accepts this connection string.
     */
    @Override
    public boolean acceptsDatabaseURL(String url){
        return url.trim().startsWith("jdbc:derby://localhost"); // NOI18N
    }
    
    /**
     * Is database server up and running.
     */
    @Override
    public boolean isRunning(){
        if (process!=null){
            try{
                int e = process.exitValue();
                process=null;
            } catch (IllegalThreadStateException e){
                //not exited yet...it's ok
                
            }
        }
        return (process!=null);
        
    }
    
    @Override
    public String getJDBCDriverClass() {
        return DerbyOptions.DRIVER_CLASS_NET;
    }
    
    /**
     * Can the database be started from inside the IDE?
     */
    @Override
    public boolean canStart(){
        // issue 81619: should only try to start if the location is set
        return DerbyOptions.getDefault().getLocation().length() > 0;
    }
    
    /**
     * Start the database server.
     */
    @Override
    public void start(){
        start(START_TIMEOUT);
    }
    
    private String getNetworkServerClasspath() {
        return 
            Util.getDerbyFile("lib/derby.jar").getAbsolutePath() + File.pathSeparator +
            Util.getDerbyFile("lib/derbytools.jar").getAbsolutePath() + File.pathSeparator +
            Util.getDerbyFile("lib/derbynet.jar").getAbsolutePath(); // NOI18N
    }
    
    public int getPort() {
        return 1527;
    }
    
    /** Posts the creation of the new database to request processor.
     */
    void postCreateNewDatabase(final String databaseName, final String user, final String password) throws Exception {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run () {
                try {
                    // DerbyDatabases.createDatabase would start the database too, but
                    // doing it beforehand to avoid having two progress bars running
                    if (!ensureStarted(true)) {
                        return;
                    }
                    
                    ProgressHandle ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(
                        RegisterDerby.class, "MSG_CreatingDBProgressLabel", databaseName));
                    ph.start();
                    try {
                        DerbyDatabases.createDatabase(databaseName, user, password);
                    } finally {
                        ph.finish();
                    }
               } catch (RuntimeException | DatabaseException | IOException e) {
                    LOGGER.log(Level.WARNING, null, e);
                    String message = NbBundle.getMessage(RegisterDerby.class, "ERR_CreateDatabase", e.getMessage());
                    Util.showInformation(message);
               }
           }
        });
    }
    
    private String getDerbySystemHome() {
        // return System.getProperty("netbeans.user") + File.separator + "derby";
        return DerbyOptions.getDefault().getSystemHome();
    }
    
    private void createDerbyPropertiesFile() {
        File derbyProperties = new File(getDerbySystemHome(), "derby.properties");
        if (derbyProperties.exists()) {
            return;
        }
        Properties derbyProps = new Properties();
        // fill it
        if (Utilities.isMac()) {
            derbyProps.setProperty("derby.storage.fileSyncTransactionLog", "true");
        }

        // write it out
        OutputStream fileos = null; 
        try {
            File derbyPropertiesParent = derbyProperties.getParentFile();
            derbyPropertiesParent.mkdirs();
            fileos = new FileOutputStream(derbyProperties);
            derbyProps.store(fileos, NbBundle.getMessage(RegisterDerby.class, "MSG_DerbyPropsFile"));
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, ex.getLocalizedMessage() + " while createDerbyPropertiesFile into " + derbyProps, ex);
        } finally {
            if (fileos != null) {
                try {
                    fileos.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }
        }
        
    }
    
    private File getInstallLocation() {
        String location = DerbyOptions.getDefault().getLocation();
        if (location.equals("")) { // NOI18N
            return null;
        }
        return new File(location);
    }
            
    private String[] getEnvironment() {
        String location = DerbyOptions.getDefault().getLocation();
        if (location.equals("")) { // NOI18N
            return null;
        }
        return new String[] { "DERBY_INSTALL=" + location }; // NOI18N
    }
    
    /**
     * Start the database server, and wait some time (in milliseconds) to make sure the server is active.
     *
     * @param  waitTime the time to wait. If less than or equal to zero, do not
     *         wait at all.
     *
     * @return true if the server is definitely started, false otherwise (the server is
     *         not started or the status is unknown). If <code>waitTime</code> was
     *         less than zero, then always false.
     */
    private boolean start(int waitTime){
        if (process!=null){// seems to be already running?
            stop();
        }
        if (!Util.checkInstallLocation()) {
            return false;
        }
        try {
            String java = getJavaExecutable();
            
            // create the derby.properties file
            createDerbyPropertiesFile();
            
            // java -Dderby.system.home="<userdir/derby>" -classpath  
            //     "<DERBY_INSTALL>/lib/derby.jar:<DERBY_INSTALL>/lib/derbytools.jar:<DERBY_INSTALL>/lib/derbynet.jar"
            //     org.apache.derby.drda.NetworkServerControl start
            NbProcessDescriptor desc = new NbProcessDescriptor(
              java,
              "-Dderby.system.home=\"" + getDerbySystemHome() + "\" " +
              "-classpath \"" + getNetworkServerClasspath() + "\"" + 
              " org.apache.derby.drda.NetworkServerControl start" + startArgs()
            );
            if (LOG) {
                LOGGER.log(Level.FINE, "Running {0} {1}", new Object[]{desc.getProcessName(), desc.getArguments()});
            }
            process = desc.exec (
                null,
                getEnvironment(),
                true,
                getInstallLocation()
            );

            ExecSupport ee = new ExecSupport(process,NbBundle.getMessage(StartAction.class, "LBL_outputtab"));
            ee.setStringToLookFor("" + getPort()); // NOI18N
            addSecurityBugHandler(ee);
            ee.start();
            if (waitTime >= 0) {
                // to make sure the server is up and running
                boolean canStart = waitStart(ee, waitTime);
                if (!canStart) {
                    stop();
                }
                return canStart;
            } else {
                return false;
            }
        } catch (IOException | RuntimeException e) {
            Util.showInformation(e.getLocalizedMessage());
            return false;
        } finally {
            InputOutput io = org.openide.windows.IOProvider.getDefault().getIO(
                    NbBundle.getMessage(StartAction.class, "LBL_outputtab"), false);
            io.getOut().close();
        }
    }
    
    /**
     * Add security manager bug handler, if needed. See bug #239962.
     */
    private void addSecurityBugHandler(ExecSupport ee) {
        Preferences prefs = NbPreferences.forModule(RegisterDerby.class);
        if (!prefs.getBoolean(DISABLE_SECURITY_MANAGER, false)
                && !prefs.getBoolean(DO_NOT_CHECK_SECURITY_MANAGER_BUG, false)) {
            ee.addOutputStringHandler(SECURITY_MANAGER_BUG_OUTPUT,
                    createSecurityManagerBugHandler());
        }
    }

    @NbBundle.Messages({
        "TTL_SecurityBug=JavaDB - Security Manager Problem"
    })
    public Runnable createSecurityManagerBugHandler() {
        return new Runnable() {

            @Override
            public void run() {
                if (!EventQueue.isDispatchThread()) {
                    EventQueue.invokeLater(this);
                    return;
                }
                class NotifyPanel extends SecurityManagerBugPanel {

                    @Override
                    public void disableSecurityManagerClicked() {
                        NbPreferences.forModule(RegisterDerby.class).putBoolean(
                                DISABLE_SECURITY_MANAGER, true);
                    }

                    @Override
                    public void doNotShowAgainClicked() {
                        NbPreferences.forModule(RegisterDerby.class).putBoolean(
                                DO_NOT_CHECK_SECURITY_MANAGER_BUG, true);
                    }
                }
                NotificationDisplayer.getDefault().notify(
                        Bundle.TTL_SecurityBug(), getDbIcon(),
                        new NotifyPanel(), new NotifyPanel(),
                        NotificationDisplayer.Priority.HIGH);
            }
        };
    }

    /**
     * Check whether some non-standard arguments are needed to start JavaDB.
     *
     * @return String containing non-standard arguments, prefixed by a space
     * character (if not empty).
     */
    private String startArgs() {

        Preferences prefs = NbPreferences.forModule(RegisterDerby.class);

        boolean disableSecurityManager = Integer.getInteger("java.specification.version", -1) > 17;

        if (prefs.getBoolean(DISABLE_SECURITY_MANAGER, false) || disableSecurityManager) {
            return " -noSecurityManager";                               //NOI18N
        } else {
            return "";                                                  //NOI18N
        }
    }

    private Icon getDbIcon() {
        return ImageUtilities.loadImageIcon(
                "org/netbeans/modules/derby/resources/database.gif", //NOI18N
                false);
    }

    private boolean waitStart(final ExecSupport execSupport, int waitTime) {
        boolean started = false;
        final boolean[] forceExit = new boolean[1];
        String waitMessage = NbBundle.getMessage(RegisterDerby.class, "MSG_StartingDerby");
        ProgressHandle progress = ProgressHandleFactory.createHandle(waitMessage, new Cancellable() {
            @Override
            public boolean cancel() {
                forceExit[0] = true;
                return execSupport.interruptWaiting();
            }
        });
        progress.start();
        try {
            while (!started) {
                started = execSupport.waitForMessage(waitTime * 1000);
                if (!started) {
                    if (waitTime > 0 && (!forceExit[0])) {
                        String title = NbBundle.getMessage(RegisterDerby.class, "LBL_DerbyDatabase");
                        String message = NbBundle.getMessage(RegisterDerby.class, "MSG_WaitStart", waitTime);
                        NotifyDescriptor waitConfirmation = new NotifyDescriptor.Confirmation(message, title, NotifyDescriptor.YES_NO_OPTION);
                        if (DialogDisplayer.getDefault().notify(waitConfirmation)
                                != NotifyDescriptor.YES_OPTION) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
            if (!started) {
                execSupport.terminate();
                LOGGER.log(Level.WARNING, "Derby server failed to start"); // NOI18N
            }
        } finally {
            progress.finish();
        }
        return started;
    }
    
    /**
     * Stop the database server.
     */
    @Override
    public void stop(){
        try {
            if (process==null){//nothing to stop...
                return;
            }
            String java = getJavaExecutable();
            // java -Dderby.system.home="<userdir/derby>" -classpath  
            //     "<DERBY_INSTALL>/lib/derby.jar:<DERBY_INSTALL>/lib/derbytools.jar:<DERBY_INSTALL>/lib/derbynet.jar"
            //     org.apache.derby.drda.NetworkServerControl shutdown
            NbProcessDescriptor desc = new NbProcessDescriptor(
              java,
              "-Dderby.system.home=\"" + getDerbySystemHome() + "\" " +
              "-classpath \"" + getNetworkServerClasspath() + "\"" + 
              " org.apache.derby.drda.NetworkServerControl shutdown"
            );
            if (LOG) {
                LOGGER.log(Level.FINE, "Running {0} {1}", new Object[]{desc.getProcessName(), desc.getArguments()});
            }
            Process shutwownProcess = desc.exec (
                null,
                getEnvironment(),
                true,
                getInstallLocation()
            );
            shutwownProcess.waitFor();

            process.destroy();
            disconnectAllDerbyConnections();
        } catch (IOException | InterruptedException | RuntimeException e) {
            Util.showInformation(e.getMessage());
        }
        finally {
            process=null;
        }
    }

    private static String getJavaExecutable() {
        File javaExe = new File(System.getProperty("java.home"), "/bin/java" + (Utilities.isWindows() ? ".exe" : "")); // NOI18N
        assert javaExe.exists() && javaExe.canExecute() : javaExe + " exists and it's executable.";
        File javaExeNormalized = FileUtil.normalizeFile(javaExe);
        FileObject javaFO = FileUtil.toFileObject(javaExeNormalized);
        if (javaFO == null) {
            throw new RuntimeException (NbBundle.getMessage(RegisterDerby.class, "EXC_JavaExecutableNotFound"));
        }
        String java = FileUtil.toFile(javaFO).getAbsolutePath();
        if (java == null) {
            throw new RuntimeException (NbBundle.getMessage(RegisterDerby.class, "EXC_JavaExecutableNotFound"));
        }
        return java;
    }
    
    private void disconnectAllDerbyConnections() {
        DatabaseConnection[] dbconn = ConnectionManager.getDefault().getConnections();
        for (int i = 0; i < dbconn.length; i++) {
            if (RegisterDerby.getDefault().acceptsDatabaseURL(dbconn[i].getDatabaseURL())) {
                ConnectionManager.getDefault().disconnect(dbconn[i]);
            }
        }
    }
    
    /**
     * Starts the server if necessary, and can wait for it to start if it was
     * not already started.
     *
     * @param  waitIfNotStarted true if to wait for a certain period of time for the server to start 
     *         if it is not already started; false otherwise.
     *
     * @return true if the server is definitely known to be started, false otherwise.
     */
    public boolean ensureStarted(boolean waitIfNotStarted) {
        if (isRunning()) {
            return true;
        }
        if (!canStart()) {
            return false;
        }
        if (waitIfNotStarted) {
            return start(START_TIMEOUT);
        } else {
            start(-1);
            return false;
        }
    }
}
