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

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.profiler.spi.Profiler;
import org.openide.filesystems.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerSupport;
import org.openide.util.NbBundle;

/**
 * Ant task that starts the server in profile mode.
 *
 * @author sherold
 */
public class StartProfiledServer extends Task implements Deployment.Logger {
    
    private static final String PLAT_PROP_ANT_NAME = "platform.ant.name"; //NOI18N
    
    private boolean         forceRestart;
    /** timeout on waiting for server startup, default = 3 min */
    private int             startupTimeout = 180000;
    /** java platform "platform.ant.name" property */
    private String          javaPlatform;
    private CommandlineJava jvmarg       = new CommandlineJava();
    private Environment     env          = new Environment();
    
    public void execute() throws BuildException {
      
        Profiler profiler = ServerRegistry.getProfiler();
        if (profiler == null) {
            String msg = NbBundle.getMessage(StartProfiledServer.class, "MSG_ProfierNotFound");
            throw new BuildException(msg);
        }
        JavaPlatform[] installedPlatforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
        JavaPlatform platform = null;
        for (int i = 0; i < installedPlatforms.length; i++) {
            String platformName = (String)installedPlatforms[i].getProperties().get(PLAT_PROP_ANT_NAME);
            if (platformName != null && platformName.equals(javaPlatform)) {
                platform = installedPlatforms[i];
            }
        }
        if (platform == null) {
            String msg = NbBundle.getMessage(StartProfiledServer.class, "MSG_PlatformNotFound", javaPlatform);
            throw new BuildException(msg);
        }
        String[] envvar = env.getVariables();
        if (envvar == null) {
            envvar = new String[0];
        }
        FileObject fo = FileUtil.toFileObject(getProject().getBaseDir());
        fo.refresh(); // without this the "build" directory is not found in filesystems
        J2eeModuleProvider jmp = (J2eeModuleProvider)FileOwnerQuery.getOwner(fo).getLookup().lookup(J2eeModuleProvider.class);
        ServerInstance si = ServerRegistry.getInstance().getServerInstance(jmp.getServerInstanceID());
        if (!si.startProfile(forceRestart, this)) {
            String msg = NbBundle.getMessage(StartProfiledServer.class, "MSG_StartupFailed");
            throw new BuildException(msg);
        }
        // wait for the server to finish its startup
        long timeout = System.currentTimeMillis() + startupTimeout;
        while (profiler.getState() != ProfilerSupport.STATE_INACTIVE) {
            if (si.isRunning()) {
                log(NbBundle.getMessage(StartProfiledServer.class, "MSG_ServerUp"));
                si.refresh(); // update the server status
                return;
            }
            // if time-out ran out, suppose command failed
            if (System.currentTimeMillis() > timeout) {
                String msg = NbBundle.getMessage(StartProfiledServer.class, "MSG_StartTimedOut", String.valueOf(Math.round(startupTimeout / 1000)));
                throw new BuildException(msg);
            }
            try { 
                Thread.sleep(1000);  // take a nap before next retry
            } catch (Exception ex) {};
        }
        throw new BuildException(NbBundle.getMessage(StartProfiledServer.class, "MSG_StartupFailed")); // NOI18N
    }
    
    public void setForceRestart(boolean forceRestart) {
        this.forceRestart = forceRestart;
    }
    
    public void setStartupTimeout(int timeout) {
      startupTimeout = timeout;
    }
    
    public void setJavaPlatform(String javaPlatform) {
        this.javaPlatform = javaPlatform;
    }
    
    public Commandline.Argument createJvmarg() {
        return jvmarg.createVmArgument();
    }
    
    public void addEnv(Environment.Variable var) {
        env.addVariable(var);
    }
}
