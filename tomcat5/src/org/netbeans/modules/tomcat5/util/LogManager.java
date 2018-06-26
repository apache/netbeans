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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    private Map/*<TomcatModule, TomcatModuleConfig>*/ tomcatModuleConfigs = Collections.synchronizedMap(new WeakHashMap());
    private Map/*<String, LogViewer>*/ contextLogViewers = Collections.synchronizedMap(new HashMap());
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
		sharedContextLogViewer.addLogViewerStopListener(new LogViewer.LogViewerStopListener() {
                   public void callOnStop() {
                       synchronized(sharedContextLogLock) {
                           sharedContextLogViewer = null;
                       }
                   }
                });
                sharedContextLogViewer.start();
            } else if (sharedContextLogViewer == null || !sharedContextLogViewer.isOpen()) {
                if (sharedContextLogViewer != null) {
                    sharedContextLogViewer.removeAllLogViewerStopListener();
                }
                sharedContextLogViewer = newSharedContextLog;
		sharedContextLogViewer.addLogViewerStopListener(new LogViewer.LogViewerStopListener() {
                   public void callOnStop() {
                       synchronized(sharedContextLogLock) {
                           sharedContextLogViewer = null;
                       }
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
        return juliJarExist.booleanValue();
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
                } catch (UnsupportedLoggerException e) { // should never occur
                    Logger.getLogger(LogManager.class.getName()).log(Level.INFO, null, e);
                    return;
                } catch (NullPointerException npe) {
                    Logger.getLogger(LogManager.class.getName()).log(Level.INFO, null, npe);
                    return;
                }
		juliLogViewer.addLogViewerStopListener(new LogViewer.LogViewerStopListener() {
                   public void callOnStop() {
                       synchronized(juliLogLock) {
                           juliLogViewer = null;
                       }
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
        TomcatModuleConfig moduleConfig = null;
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
        if (!moduleConfig.hasLogger()) return;
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
                contextLog.addLogViewerStopListener(new LogViewer.LogViewerStopListener() {
                   public void callOnStop() {
                       contextLogViewers.remove(moduleID);
                   }
                });
                contextLogViewers.put(moduleID, contextLog);
                contextLog.start();
            } else if (contextLog == null || !contextLog.isOpen()) {
                if (contextLog != null) {
                    contextLog.removeAllLogViewerStopListener();
                }
                contextLog = newContextLog;
                contextLog.addLogViewerStopListener(new LogViewer.LogViewerStopListener() {
                   public void callOnStop() {
                       contextLogViewers.remove(moduleID);
                   }
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
