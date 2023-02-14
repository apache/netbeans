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

package org.netbeans.modules.tomcat5.util;

import java.io.File;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import org.netbeans.modules.tomcat5.deploy.TomcatManagerConfig;
import org.netbeans.modules.tomcat5.deploy.TomcatModule;
import org.netbeans.modules.tomcat5.deploy.TomcatModuleConfig;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * <code>LogManager</code> manages all context and shared context logs for one
 * Tomcat server instace (one <code>TomcatManager</code>).
 *
 * @author  Stepan Herold
 */
public class LogManager {
    private ServerLog serverLog;    
    private LogViewer sharedContextLogViewer;
    private LogViewer juliLogViewer;
    private Map<TomcatModule, TomcatModuleConfig> tomcatModuleConfigs = Collections.synchronizedMap(new WeakHashMap<>());
    private Map<String, LogViewer> contextLogViewers = Collections.synchronizedMap(new HashMap<>());
    private TomcatManager manager;
    
    private final Object serverLogLock = new Object();
    private final Object sharedContextLogLock = new Object();
    private final Object juliLogLock = new Object();
    private final Object contextLogLock = new Object();
    
    private Boolean juliJarExist;
    
    /** Creates a new instance of LogManager */
    public LogManager(TomcatManager tm) {
        manager = tm;
    }
    
    // ------- server log (output) ---------------------------------------------
    
    /**
     * Open the server log (output).
     */
    public void openServerLog() {
        final Process process = manager.getTomcatProcess();
        assert process != null;
        synchronized(serverLogLock) {
            if (serverLog != null) {
                serverLog.takeFocus();
                return;
            }
            serverLog = new ServerLog(
                manager,
                manager.getTomcatProperties().getDisplayName(),
                new InputStreamReader(process.getInputStream()),
                new InputStreamReader(process.getErrorStream()),
                true,
                false);
            serverLog.start();
        }
        //PENDING: currently we copy only Tomcat std & err output. We should
        //         also support copying to Tomcat std input.
        new Thread() {
            @Override
            public void run() {
                try {
                    process.waitFor();
                    Thread.sleep(2000);  // time for server log
                } catch (InterruptedException e) {
                } finally {
                    closeServerLog();
                }
            }
        }.start();
    }
    
    /**
     * Stop the server log thread, if started.
     */
    public void closeServerLog() {
        synchronized(serverLogLock) {
            if (serverLog != null) {
                serverLog.stop();
                serverLog = null;
            }
        }
    }

    /**
     * Can be the server log (output) displayed?
     *
     * @return <code>true</code> if the server log can be displayed, <code>false</code>
     *         otherwise.
     */
    public boolean hasServerLog() {
        return manager.getTomcatProcess() != null;
    }
    
    // ------- end of server log (output) --------------------------------------
    
    // ------- shared context log ----------------------------------------------
    
    /**
     * Opens shared context log. Shared context log can be defined in the host or
     * engine element. Definition in the host element overrides definition in the 
     * engine element.
     */
    public void openSharedContextLog() {
        TomcatManagerConfig tomcatManagerConfig = manager.getTomcatManagerConfig();
        tomcatManagerConfig.refresh();
        if (!tomcatManagerConfig.hasLogger()) {
            return;
        }
        LogViewer newSharedContextLog = null;
        try {
            TomcatProperties tp = manager.getTomcatProperties();
            newSharedContextLog = new LogViewer(
                manager,
                null,
                tomcatManagerConfig.loggerClassName(),
                tomcatManagerConfig.loggerDir(),
                tomcatManagerConfig.loggerPrefix(),
                tomcatManagerConfig.loggerSuffix(),
                tomcatManagerConfig.loggerTimestamp(),
                false);
        } catch (UnsupportedLoggerException e) {
            NotifyDescriptor notDesc = new NotifyDescriptor.Message(
                NbBundle.getMessage(LogManager.class, "MSG_UnsupportedLogger", 
                        e.getLoggerClassName()));
            DialogDisplayer.getDefault().notify(notDesc);
            return;
        } catch (NullPointerException npe) {
            Logger.getLogger(LogManager.class.getName()).log(Level.INFO, null, npe);
        }
        
        // ensure only one thread will be opened
        synchronized(sharedContextLogLock) {
            if (sharedContextLogViewer != null && sharedContextLogViewer.isOpen() 
                && !sharedContextLogViewer.equals(newSharedContextLog)) {
                sharedContextLogViewer.removeAllLogViewerStopListener();
                sharedContextLogViewer.close();
                sharedContextLogViewer = newSharedContextLog;
		sharedContextLogViewer.addLogViewerStopListener( () -> {
                    synchronized(sharedContextLogLock) {
                        sharedContextLogViewer = null;
                    }
                });
                sharedContextLogViewer.start();
            } else if (sharedContextLogViewer == null || !sharedContextLogViewer.isOpen()) {
                if (sharedContextLogViewer != null) {
                    sharedContextLogViewer.removeAllLogViewerStopListener();
                }
                sharedContextLogViewer = newSharedContextLog;
		sharedContextLogViewer.addLogViewerStopListener( () -> {
                    synchronized(sharedContextLogLock) {
                        sharedContextLogViewer = null;
                    }
                });
                sharedContextLogViewer.start();
            }
            sharedContextLogViewer.takeFocus();
        }
    }


    /**
     * Is shared context log defined for this server?
     *
     * @return <code>true</code> shared context log is defined, <code>false</code>
     *         otherwise.
     */
    public boolean hasSharedLogger() {
        TomcatManagerConfig tomcatManagerConfig = manager.getTomcatManagerConfig();
        tomcatManagerConfig.refresh();
        return tomcatManagerConfig.hasLogger();
    }
    
    // ------- end of shared context log ---------------------------------------
    
    // ------- juli log --------------------------------------------------------
    
    public synchronized boolean hasJuliLog() {
        if (juliJarExist == null) {
            if (new File(manager.getTomcatProperties().getCatalinaHome(), "bin/tomcat-juli.jar").exists()) { // NOI18N
                juliJarExist = Boolean.TRUE;
            } else {
                juliJarExist = Boolean.FALSE;
            }
        }
        return juliJarExist;
    }
    
    public void openJuliLog() {        
        // ensure only one thread will be opened
        synchronized(juliLogLock) {
            if (juliLogViewer == null || !juliLogViewer.isOpen()) {
                if (juliLogViewer != null) {
                    juliLogViewer.removeAllLogViewerStopListener();
                }
                try {
                    TomcatProperties tp = manager.getTomcatProperties();
                    juliLogViewer = new LogViewer(manager, null, null, null, "localhost.", null, true, false); // NOI18N
                    juliLogViewer.setDisplayName(NbBundle.getMessage(LogManager.class, "TXT_JuliLogDisplayName", tp.getDisplayName()));
                } catch (UnsupportedLoggerException | NullPointerException e) { // should never occur
                    Logger.getLogger(LogManager.class.getName()).log(Level.INFO, null, e);
                    return;
                }
		juliLogViewer.addLogViewerStopListener( () -> {
                    synchronized(juliLogLock) {
                        juliLogViewer = null;
                    }
                });
                juliLogViewer.start();
            }
            juliLogViewer.takeFocus();
        }
    }
            
    // ------- end of juli log -------------------------------------------------
    
    // ------- context log -----------------------------------------------------
    
    /**
     * Open a context log for the specified module.
     *
     * @param module its context log should be opened.
     */
    public void openContextLog(TomcatModule module) {
        final String moduleID = module.getModuleID();
        Object o = tomcatModuleConfigs.get(module);
        TomcatModuleConfig  moduleConfig = null;
        LogViewer contextLog = null;
        if (o == null) {
            moduleConfig = new TomcatModuleConfig(
                    module.getDocRoot(),
                    module.getPath(),
                    manager.getTomcatManagerConfig().serverXmlPath());
            tomcatModuleConfigs.put(module, moduleConfig);
        } else {
            moduleConfig = (TomcatModuleConfig)o;
            moduleConfig.refresh();
        }
        if (!moduleConfig.hasLogger()) {
            return;
        }
        contextLog = (LogViewer)contextLogViewers.get(moduleID);
        LogViewer newContextLog = null;
        try {
            newContextLog = new LogViewer(
                manager,
                module.getPath(),
                moduleConfig.loggerClassName(),
                moduleConfig.loggerDir(),
                moduleConfig.loggerPrefix(),
                moduleConfig.loggerSuffix(),
                moduleConfig.loggerTimestamp(),
                false);
        } catch (UnsupportedLoggerException e) {
            NotifyDescriptor notDesc = new NotifyDescriptor.Message(
                NbBundle.getMessage(LogManager.class, "MSG_UnsupportedLogger", 
                        e.getLoggerClassName()));
            DialogDisplayer.getDefault().notify(notDesc);
            return;
        } catch (NullPointerException npe) {
            Logger.getLogger(LogManager.class.getName()).log(Level.INFO, null, npe);
        }
        
        // ensure only one thread will be opened
        synchronized(contextLogLock) {
            if (contextLog != null && contextLog.isOpen() 
                && !contextLog.equals(newContextLog)) {
                contextLog.removeAllLogViewerStopListener();
                contextLog.close();
                contextLog = newContextLog;
                contextLog.addLogViewerStopListener( () -> {
                    contextLogViewers.remove(moduleID);
                });
                contextLogViewers.put(moduleID, contextLog);
                contextLog.start();
            } else if (contextLog == null || !contextLog.isOpen()) {
                if (contextLog != null) {
                    contextLog.removeAllLogViewerStopListener();
                }
                contextLog = newContextLog;
                contextLog.addLogViewerStopListener( () -> {
                    contextLogViewers.remove(moduleID);
                });                
                contextLogViewers.put(moduleID, contextLog);
                contextLog.start();
            }
        }
        contextLog.takeFocus();
    }

    /**
     * Is context log defined for the specified module.
     *
     * @param module which should be examined.
     * @return <code>true</code> if specified module has a context log defined, 
     *         <code>false</code> otherwise.
     */
    public boolean hasContextLogger(TomcatModule module) {
        Object o = tomcatModuleConfigs.get(module);
        TomcatModuleConfig moduleConfig = null;
        if (o == null) {
            moduleConfig = new TomcatModuleConfig(
                    module.getDocRoot(),
                    module.getPath(),
                    manager.getTomcatManagerConfig().serverXmlPath());
            tomcatModuleConfigs.put(module, moduleConfig);
        } else {
            moduleConfig = (TomcatModuleConfig)o;
            moduleConfig.refresh();
        }
        return moduleConfig.hasLogger();
    }
    
    // ------- end of context log ----------------------------------------------
}
