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
