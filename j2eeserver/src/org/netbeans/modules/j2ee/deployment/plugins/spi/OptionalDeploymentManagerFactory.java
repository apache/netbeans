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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.j2ee.deployment.plugins.spi;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.annotations.common.CheckForNull;
import org.openide.WizardDescriptor;

/**
 * Factory for optional deployment functionality that a plugin can provide.
 * Plugins need to register an instance of this class in module layer in folder
 * <code>J2EE/DeploymentPlugins/{plugin_name}</code>.
 *
 * @author  Pavel Buzek
 */
public abstract class OptionalDeploymentManagerFactory {

    /**
     * Create StartServer for given DeploymentManager.
     * The instance returned by this method will be cached by the j2eeserver.
     */ 
    public abstract StartServer getStartServer (DeploymentManager dm);
    
    /** 
     * Create IncrementalDeployment for given DeploymentManager.
     * The instance returned by this method will be cached by the j2eeserver.
     */
    public abstract IncrementalDeployment getIncrementalDeployment (DeploymentManager dm);
    
    /** 
     * Create FindJSPServlet for given DeploymentManager.
     * The instance returned by this method will be cached by the j2eeserver.
     */
    public abstract FindJSPServlet getFindJSPServlet (DeploymentManager dm);
    
    /** 
     * Create TargetModuleIDResolver for the given DeploymentManager.
     * The instance returned by this method will be cached by the j2eeserver.
     */
    public TargetModuleIDResolver getTargetModuleIDResolver(DeploymentManager dm) {
        return null;
    }
    
    /** 
     * Create the wizard iterator to be used in the Add Server Instance wizard
     */
    public WizardDescriptor.InstantiatingIterator getAddInstanceIterator() {
        return null;
    }
    

    /**
     * Returns <code>true</code> if the common UI (like the wizard in common
     * add dialog) should be handled by insfrastructure of the j2eeserver.
     *
     * @return <code>true</code> if the common UI is required by the plugin
     * @since 1.38.0
     */
    public boolean isCommonUIRequired() {
        return true;
    }
    
    /**
     * Creates an Ant deployment provider for the specified deployment manager.
     *
     * @param dm deployment manager.
     * @return an instance of the AntDeploymentProvider if Ant deployment
     *         is supported for the specified deployment manager, null otherwise.
     * @since 1.18
     */
    public AntDeploymentProvider getAntDeploymentProvider(DeploymentManager dm) {
        return null;
    }
    
    /**
     * Creates a <code>DatasourceManager</code> for the given deployment manager
     * or <code>null</code> if data source management is not supported
     *
     * @param dm the deployment manager
     *
     * @return a data source manager or <code>null</code> if data source management
     *         is not supported
     *
     * @since 1.15
     */
    public DatasourceManager getDatasourceManager(DeploymentManager dm) {
        return null;
    }
    
    /**
     * Creates a JDBC driver deployer for the specified deployment manager.
     * 
     * @param dm deployment manager.
     * 
     * @return JDBC driver deployer for the specified deployment manager or null
     *         if JDBC driver deployment is not supported.
     * 
     * @since 1.24
     */
    public JDBCDriverDeployer getJDBCDriverDeployer(DeploymentManager dm) {
        return null;
    }

    /**
     * Creates a <code>MessageDestinationDeployment</code> for the given deployment manager
     * or <code>null</code> if message destination deployment is not supported
     *
     * @param dm the deployment manager
     *
     * @return a message destination deployment or <code>null</code> 
     *          if message destination deployment is not supported
     *
     * @since 1.25
     */
    public MessageDestinationDeployment getMessageDestinationDeployment(DeploymentManager dm) {
        return null;
    }

    /**
     * Creates a <code>ServerInstanceDescriptor</code> for the given deployment manager
     * or <code>null</code> if descriptor is not supported.
     *
     * @param dm the deployment manager
     * @return instance descriptor or <code>null</code> if descriptor is not supported
     * @since 1.46
     */
    public ServerInstanceDescriptor getServerInstanceDescriptor(DeploymentManager dm) {
        return null;
    }

    /**
     * Allows a plugin to perform post initialization action. When this method
     * invoked infrastructure is initialized so it can register/query instances.
     *
     * @since 1.51
     */
    public void finishServerInitialization() throws ServerInitializationException {
    }

    /**
     * Return the manager handling the server libraries. May return
     * <code>null</code> if the functionality is not supported by the plugin.
     *
     * @param dm the deployment manager
     * @return the manager handling the server libraries
     * @since 1.68
     * @see ServerLibraryManager
     */
    @CheckForNull
    public ServerLibraryManager getServerLibraryManager(DeploymentManager dm) {
        return null;
    }
}
