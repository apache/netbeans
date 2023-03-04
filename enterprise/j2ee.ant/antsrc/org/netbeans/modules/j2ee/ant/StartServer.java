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
package org.netbeans.modules.j2ee.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.netbeans.modules.j2ee.deployment.impl.ServerException;
import org.openide.util.Lookup;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.filesystems.*;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;
import org.openide.util.NbBundle;

/**
 * Ant task that starts the server if needed
 * @author 
 */
public class StartServer extends Task implements Deployment.Logger {

    // default profiling timeout
    private static final long DEFAULT_TIMEOUT = 300000; // in millis

    /**
     * Holds value of property debugmode.
     */
    private boolean debugmode = false;

    /**
     * Holds value of property profilemode.
     */
    private boolean profilemode = false;
    
    public void execute() throws BuildException {

        ClassLoader originalLoader = null;

        try {
            // see issue #62448
            ClassLoader current = (ClassLoader) Lookup.getDefault().lookup(
                    ClassLoader.class);
            if (current == null) {
                current = ClassLoader.getSystemClassLoader();
            }
            if (current != null) {
                originalLoader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(current);
            }

            J2eeModuleProvider jmp = null;
            try {
                FileObject fob = FileUtil.toFileObject(getProject().getBaseDir());
                fob.refresh(); // without this the "build" directory is not found in filesystems
                jmp = (J2eeModuleProvider) FileOwnerQuery.getOwner(fob).getLookup().lookup(
                        J2eeModuleProvider.class);
            } catch (Exception e) {
                throw new BuildException(e);
            }

            // get server instance
            ServerInstance si = ServerRegistry.getInstance().getServerInstance(
                    jmp.getServerInstanceID());
            String title = NbBundle.getMessage(ServerInstance.class,
                    "LBL_StartServerProgressMonitor", si.getDisplayName());
            ProgressUI ui = new ProgressUI(title, false, this);
            ui.start();
            ServerDebugInfo sdi = null;
            // Start server instance if it has not yet been started.
            try {
                if (debugmode) {
                    si.startDebug(ui);
                } else if (profilemode) {
                    si.startProfile(false, ui);
                    // TODO whole this thing would deserve a better solution
                    long start = System.nanoTime();
                    while (!si.isReallyRunning()
                            && DEFAULT_TIMEOUT > ((System.nanoTime() - start) / 1000000)) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            // proceed to exit
                        }
                    }
                } else {
                    si.start(ui);
                }

                //  set the debug info 
                sdi = jmp.getServerDebugInfo();
                if (sdi != null) {
                    String h = sdi.getHost();
                    String transport = sdi.getTransport();
                    String address = "";   //NOI18N

                    if (transport.equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                        address = sdi.getShmemName();
                    } else {
                        address = Integer.toString(sdi.getPort());
                    }
                    getProject().setProperty("name", jmp.getDeploymentName());
                    getProject().setProperty("jpda.transport", transport);
                    getProject().setProperty("jpda.host", h);
                    getProject().setProperty("jpda.address", address);
                }
            } catch (ServerException ex) {
                if (null != ex.getCause()) {
                    // send the message and the exception to the ant output
                    throw new BuildException(ex.getMessage(), ex);
                } else {
                    // just send the message to the ant output
                    throw new BuildException(ex.getMessage());
                }
            } catch (Exception ex) {
                throw new BuildException(ex);
            } finally {
                ui.finish();
            }

        } finally {
            if (originalLoader != null) {
                Thread.currentThread().setContextClassLoader(originalLoader);
            }
        }
    }

    /**
     * Getter for property debugmode.
     * @return Value of property debugmode.
     */
    public boolean getDebugmode() {
        return this.debugmode;
    }
    
    /**
     * Getter for property profilemode.
     * @return Value of property profilemode.
     */
    public boolean getProfilemode() {
        return this.profilemode;
    }

    /**
     * Setter for property debugmode.
     * @param debugmode New value of property debugmode.
     */
    public void setDebugmode(boolean debugmode) {
        this.debugmode = debugmode;
    }
    
    /**
     * Setter for property profilemode.
     * @param profilemode New value of property profilemode.
     */
    public void setProfilemode(boolean profilemode) {
        this.profilemode = profilemode;
    }
}
