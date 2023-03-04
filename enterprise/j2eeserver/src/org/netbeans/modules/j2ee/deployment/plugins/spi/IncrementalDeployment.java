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

/*
 * IncrementalDeployment.java
 *
 * Created on November 14, 2003, 9:13 AM
 */

package org.netbeans.modules.j2ee.deployment.plugins.spi;

import org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.api.DeploymentChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;

/**
 * This interface replaces DeploymentManager calls <code>deploy</code> and <code>redeploy</code> during
 * directory-based deployment.  The calling sequence is as following:
 * <P>
 * Initially, j2eeserver will call <code>getDirectoryForNewApplication()</code>
 * to request destination directory to deliver the being deployed application or 
 * stand-alone module. In case of J2EE applications, <code>getDirectoryForNewModule()</code>
 * will be called for each child module.
 * <P>
 * After done with copying of files over to the destination, <code>initialDeploy()</code> will
 * be called to signal the copying is done.  Processing of the returned <code>ProgressObject</code>
 * is the same as in <code>DeploymentManager.distribute()</code> call. 
 * <P> 
 * Subsequent deployments are incremental. For each root and child module the IDE will ask plugin
 * for destination directory by calling <code>getDirectoryForModule()</code>.  After delivering
 * the changed files for all modules, the IDE then call <code>incrementalDeploy</code> with
 * the description of what have changed since previous deployment.
 *<P>
 * For in-place file deployment, where the file copying step is skipped, method 
 * <code>getDirectoryForNewApplication</code> or <code>getDirectoryForNewModule</code> calls
 * return null.
 * <P>
 * J2eeserver optain an instance of IncrementalDeployment from server integration plugin by
 * calling {@link OptionalDeploymentManagerFactory} to optain an instance of IncrementalDeployment
 * for each {@link javax.enterprise.deploy.spi.DeploymentManager} instance.
 * <P>
 * @author  George Finklang
 */
public abstract class IncrementalDeployment {

    /**
     * First time deployment file distribution.  Before this method is called 
     * the module content files should be ready in the destination location.
     *
     * @param target target of deployment
     * @param app the app to deploy
     * @param configuration server specific data for deployment
     * @param dir the destination directory for the given deploy app
     * @return the object for feedback on progress of deployment
     */ 
    public abstract ProgressObject initialDeploy(Target target, J2eeModule app, ModuleConfiguration configuration, File dir);

    /**
     * Before this method is called, the on-disk representation of TargetModuleID
     * is updated.
     * @param module the TargetModuleID of the deployed application or stand-alone module.
     * @param changes AppChangeDescriptor describing what in the application changed. 
     * @return the ProgressObject providing feedback on deployment progress.
     **/
    public abstract ProgressObject incrementalDeploy(TargetModuleID module, AppChangeDescriptor changes);
    
    /**
     * Whether the deployable object could be file deployed to the specified target
     * @param target target in question
     * @param deployable the deployable object in question
     * @return true if it is possible to do file deployment
     */
    public abstract boolean canFileDeploy(Target target, J2eeModule deployable);
    
    /**
     * Return absolute path which the IDE will write the specified app or
     * stand-alone module content to.
     * @param target target server of the deployment
     * @param app the app or stand-alone module to deploy
     * @param configuration server specific data for deployment
     * @return absolute path root directory for the specified app or
     * null if server can accept the deployment from an arbitrary directory.
     */
    @CheckForNull
    public abstract File getDirectoryForNewApplication(Target target, J2eeModule app, ModuleConfiguration configuration);
    
    /**
     * Return absolute path the IDE will write the app or stand-alone module content to.
     * Note: to use deployment name, implementation nees to override this.
     *
     * @param deploymentName name to use in deployment
     * @param target target server of the deployment
     * @param configuration server specific data for deployment
     * @return absolute path root directory for the specified app or null if server can accept the deployment from an arbitrary directory.
     */
    @CheckForNull
    public File getDirectoryForNewApplication(String deploymentName, Target target, ModuleConfiguration configuration) {
        return getDirectoryForNewApplication(target, configuration.getJ2eeModule(), configuration);
  } 

  /**
     * Return absolute path to which the IDE will write the specified module content.
     * @param appDir the root directory of containing application
     * @param uri the URI of child module within the app
     * @param module the child module object to deploy
     * @param configuration server specific data for deployment
     * @return absolute path root directory for the specified module.
     */
    public abstract File getDirectoryForNewModule(File appDir, String uri, J2eeModule module, ModuleConfiguration configuration);
    
    /**
     * Return absolute path to which the IDE will write the content changes of specified module.
     * @param module id for the target module.
     * @return absolute path root directory for the specified module.
     */
    public abstract File getDirectoryForModule(TargetModuleID module);
    
    /**
     * Get the URI pointing to location of child module inside a application archive.
     *
     * @param module TargetModuleID of the child module
     * @return its relative path within application archive, returns null by 
     * default (for standalone module). The value should match {@link J2eeModule#getUrl()}
     */
    public String getModuleUrl(TargetModuleID module) {
        return null;
    }
    
    /**
     * Inform the plugin that the specified module is being deployed. Notification
     * is sent even if there is really nothing needed to be deployed.
     *
     * @param module module which is being deployed.
     */
    public void notifyDeployment(TargetModuleID module) {
        //do nothing, override if needed
    }

    /**
     * Returns <code>true</code> if deploy on save is supported, <code>false</code>
     * otherwise. If this method returns <code>true</code>
     * {@link #deployOnSave(javax.enterprise.deploy.spi.TargetModuleID, org.netbeans.modules.j2ee.deployment.plugins.api.DeploymentChangeDescriptor)}
     * must provide (reasonably fast) redeploy functionality.
     *
     * @return <code>true</code> if deploy on save is supported
     * @since 1.47
     */
    public boolean isDeployOnSaveSupported() {
        return false;
    }

    /**
     * Performs reload of the artifacts when the deploy on save is requested.
     * All chenged files are alredy prepared at required location before this
     * call. Returns object tracking the reload.
     *
     * @param module module owning the artifacts
     * @param desc description of changes
     * @return object tracking the reload
     * @since 1.47
     */
    public ProgressObject deployOnSave(TargetModuleID module, DeploymentChangeDescriptor desc) {
        throw new UnsupportedOperationException("Deploy on save not supported");
    }

    public static IncrementalDeployment getIncrementalDeploymentForModule(
            IncrementalDeployment incremental, J2eeModule deployable) throws IOException {

        // defend against incomplete J2eeModule objects.
        IncrementalDeployment retVal = incremental;
        if (null != retVal && null == deployable.getContentDirectory()) {
            retVal = null;
        }
        if (null != retVal && deployable instanceof J2eeApplication) {
            // make sure all the sub modules will support directory deployment, too
            J2eeModule[] childModules = ((J2eeApplication) deployable).getModules();
            for (int i = 0; i < childModules.length; i++) {
                if (null == childModules[i].getContentDirectory()) {
                    retVal = null;
                }
            }
        }
        return retVal;
    }    
}
