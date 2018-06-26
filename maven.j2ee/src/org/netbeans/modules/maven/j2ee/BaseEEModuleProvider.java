/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.j2ee;

import org.netbeans.modules.maven.j2ee.execution.ExecutionChecker;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.j2ee.utils.Server;
import org.netbeans.modules.maven.j2ee.utils.ServerUtils;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;

/**
 * Base class for ModuleProvider implementation of different project types
 * At the moment it's not a base class only for EAR projects, because there is different API provider which need to be
 * implemented in EAR projects.
 *
 * @author mjanicek
 */
public abstract class BaseEEModuleProvider extends J2eeModuleProvider {

    protected Project project;
    protected String serverInstanceID;
    protected J2eeModule j2eemodule;
    protected CopyOnSave copyOnSave;
    protected ModuleChangeReporter changeReporter;


    public BaseEEModuleProvider(Project project) {
        this.project = project;
        this.changeReporter = new ModuleChangeReporterImpl();
    }

    public abstract J2eeModuleImplementation2 getModuleImpl();


    @Override
    public boolean isOnlyCompileOnSaveEnabled() {
        return RunUtils.isCompileOnSaveEnabled(project) && !MavenProjectSupport.isDeployOnSave(project);
    }

    @Override
    public ModuleChangeReporter getModuleChangeReporter() {
        return changeReporter;
    }

    @Override
    @CheckForNull
    public DeployOnSaveSupport getDeployOnSaveSupport() {
        return getCopyOnSave();
    }

    /**
     * Returns actual CopyOnSave instance registered for the project. This method also call initialize method,
     * but it's up to client to call copyOnSave.cleanup().
     *
     * @return actual or newly created instance
     */
    @CheckForNull
    public CopyOnSave getCopyOnSaveSupport() {
        return getCopyOnSave();
    }

    @CheckForNull
    private CopyOnSave getCopyOnSave() {
        if (copyOnSave == null) {
            copyOnSave = project.getLookup().lookup(CopyOnSave.class);
            if (copyOnSave != null) {
                copyOnSave.initialize();
            }
        }
        return copyOnSave;
    }

    @Override
    public synchronized J2eeModule getJ2eeModule() {
        if (j2eemodule == null) {
            j2eemodule = J2eeModuleFactory.createJ2eeModule(getModuleImpl());
        }
        return j2eemodule;
    }

    @Override
    public void setServerInstanceID(String newId) {
        String oldId = null;
        if (serverInstanceID != null) {
            oldId = MavenProjectSupport.obtainServerID(serverInstanceID);
        }
        serverInstanceID = newId;
        fireServerChange(oldId, getServerID());
    }

    /**
     * Id of server instance for deployment. The default implementation returns
     * the default server instance selected in Server Registry.
     * The return value may not be null.
     * If modules override this method they also need to override {@link useDefaultServer}.
     */
    @Override
    public String getServerInstanceID() {
        if (serverInstanceID != null) {
            return serverInstanceID;
        }

        Server server = ServerUtils.findServer(project);
        if (server != null) {
            return server.getServerInstanceID();
        }
        return ExecutionChecker.DEV_NULL;
    }

    /**
     * This method is used to determine type of target server.
     * The return value must correspond to value returned from {@link getServerInstanceID}.
     */
    @Override
    public String getServerID() {
        if (serverInstanceID != null) {
            String serverID = MavenProjectSupport.obtainServerID(serverInstanceID);
            if (serverID != null) {
                return serverID;
            }
        }

        Server server = ServerUtils.findServer(project);
        if (server != null) {
            return server.getServerID();
        }
        return ExecutionChecker.DEV_NULL;
    }
}