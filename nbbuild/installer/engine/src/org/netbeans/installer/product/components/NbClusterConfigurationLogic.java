/**
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

package org.netbeans.installer.product.components;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.RemovalMode;
import org.netbeans.installer.utils.helper.Text;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardAction;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.wizard.components.actions.SetInstallationLocationAction;

/**
 *
 
 */
public abstract class NbClusterConfigurationLogic extends ProductConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String BASE_IDE_UID =
            "nb-all"; // NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private String[] clusterNames;
    private String productId;
    private String sourceUid;
    
    private List<WizardComponent> wizardComponents =
            new LinkedList<WizardComponent>();
    
    protected NbClusterConfigurationLogic(
            final String clusterName,
            final String productId,
            final String sourceUid) throws InitializationException {
        this(new String[]{clusterName}, productId, sourceUid);
    }
    
    protected NbClusterConfigurationLogic(
            final String[] clusterNames,
            final String productId,
            final String sourceUid) throws InitializationException {
        this.clusterNames = clusterNames;
        this.productId = productId;
        this.sourceUid = sourceUid;
        
        WizardAction action;
        
        action = new SetInstallationLocationAction();
        action.setProperty(
                SetInstallationLocationAction.SOURCE_UID_PROPERTY,
                sourceUid);
        wizardComponents.add(action);
    }
    
    protected NbClusterConfigurationLogic(
            final String clusterName,
            final String productId) throws InitializationException {
        this(new String[]{clusterName}, productId);
    }
    
    protected NbClusterConfigurationLogic(
            final String[] clusterNames,
            final String productId) throws InitializationException {
        this(clusterNames, productId, BASE_IDE_UID);
    }
    
    public void install(
            final Progress progress) throws InstallationException {
        final File installLocation = getProduct().getInstallationLocation();
        
        // get the list of suitable netbeans ide installations
        final List<Dependency> dependencies =
                getProduct().getDependencyByUid(sourceUid);
        final List<Product> sources =
                Registry.getInstance().getProducts(dependencies.get(0));
        
        // pick the first one and integrate with it
        final File nbLocation = sources.get(0).getInstallationLocation();
        
        // add the cluster to the active clusters list //////////////////////////////
        for (String clusterName: clusterNames) {
            try {
                progress.setDetail(ResourceUtils.getString(
                        NbClusterConfigurationLogic.class,
                        "NCCL.install.netbeans.clusters", // NOI18N
                        clusterName));
                File lastModified = new File(new File(installLocation, clusterName),
                                    NetBeansUtils.LAST_MODIFIED_MARKER);
                if(!FileUtils.exists(lastModified)) {
                    getProduct().getInstalledFiles().add(lastModified);
                }
                NetBeansUtils.addCluster(nbLocation, clusterName);
            } catch (IOException e) {
                throw new InstallationException(ResourceUtils.getString(
                        NbClusterConfigurationLogic.class,
                        "NCCL.install.error.netbeans.clusters", // NOI18N
                        clusterName),
                        e);
            }
        }

        // update the update_tracking files information //////////////////////////////
        for (String clusterName: clusterNames) {
            try {
                progress.setDetail(ResourceUtils.getString(
                        NbClusterConfigurationLogic.class,
                        "NCCL.install.netbeans.update.tracking", // NOI18N
                        clusterName));

                NetBeansUtils.updateTrackingFilesInfo(nbLocation, clusterName);
            } catch (IOException e) {
                throw new InstallationException(ResourceUtils.getString(
                        NbClusterConfigurationLogic.class,
                        "NCCL.install.error.netbeans.update.tracking", // NOI18N
                        clusterName),
                        e);
            }
        }
        
        // add the product id to the productid file /////////////////////////////////
        try {
	    if(productId!=null) {
            progress.setDetail(ResourceUtils.getString(
                    NbClusterConfigurationLogic.class,
                    "NCCL.install.productid", // NOI18N
                    productId));
            
            NetBeansUtils.addPackId(nbLocation, productId);
            }
        } catch (IOException e) {
            throw new InstallationException(ResourceUtils.getString(
                    NbClusterConfigurationLogic.class,
                    "NCCL.install.error.productid", // NOI18N
                    productId),
                    e);
        }
        
        // remove files that are not suited for the current platform ////////////////
        //for (String clusterName: clusterNames) {
        //    final File cluster = new File(installLocation, clusterName);
        //    
        //    try {
        //        progress.setDetail(ResourceUtils.getString(
        //                NbClusterConfigurationLogic.class,
        //                "NCCL.install.irrelevant.files")); // NOI18N
        //        
        //        SystemUtils.removeIrrelevantFiles(cluster);
        //    } catch (IOException e) {
        //        throw new InstallationException(ResourceUtils.getString(
        //                NbClusterConfigurationLogic.class,
        //                "NCCL.install.error.irrelevant.files"), // NOI18N
        //                e);
        //    }
        //}
        
        // corrent permisions on executable files ///////////////////////////////////
        //for (String clusterName: clusterNames) {
        //    final File cluster = new File(installLocation, clusterName);
        //
        //    try {
        //        progress.setDetail(ResourceUtils.getString(
        //                NbClusterConfigurationLogic.class,
        //                "NCCL.install.files.permissions")); // NOI18N
        //
        //        SystemUtils.correctFilesPermissions(cluster);
        //    } catch (IOException e) {
        //        throw new InstallationException(ResourceUtils.getString(
        //                NbClusterConfigurationLogic.class,
        //                "NCCL.install.error.files.permissions"), // NOI18N
        //                e);
        //    }
        //}
        
    }
    
    public void uninstall(final Progress progress) throws UninstallationException {
        checkNetbeansRunning();
        
        final File installLocation = getProduct().getInstallationLocation();
        
        // get the list of suitable netbeans ide installations
        final List<Dependency> dependencies =
                getProduct().getDependencyByUid(sourceUid);
        
        if(dependencies.isEmpty()) {
            LogManager.log(ErrorLevel.WARNING, 
                    "Can`t uninstall " + getProduct().getDisplayName());
            LogManager.log(ErrorLevel.WARNING, 
                    "Can`t find parent product with uid " + sourceUid);            
            return;
        }
        
        final List<Product> sources =
                Registry.getInstance().getProducts(dependencies.get(0));
        
        // pick the first one and assume that we're integrated with it
        final File nbLocation = sources.get(0).getInstallationLocation();
        
        // remove the product id from the productid file ////////////////////////////
        try {
            if(productId!=null) {
            progress.setDetail(ResourceUtils.getString(
                    NbClusterConfigurationLogic.class,
                    "NCCL.uninstall.productid", // NOI18N
                    productId));
            
            NetBeansUtils.removePackId(nbLocation, productId);
            }
        } catch (IOException e) {
            throw new UninstallationException(ResourceUtils.getString(
                    NbClusterConfigurationLogic.class,
                    "NCCL.uninstall.error.productid", // NOI18N
                    productId),
                    e);
        }
        
        // remove the cluster from the active clusters list ///////////////////////// 
        for (String clusterName: clusterNames) {
            try {
                progress.setDetail(ResourceUtils.getString(
                        NbClusterConfigurationLogic.class,
                        "NCCL.uninstall.netbeans.clusters", // NOI18N
                        clusterName));
                
                NetBeansUtils.removeCluster(nbLocation, clusterName);
            } catch (IOException e) {
                throw new UninstallationException(ResourceUtils.getString(
                        NbClusterConfigurationLogic.class,
                        "NCCL.uninstall.error.netbeans.clusters", // NOI18N
                        clusterName),
                        e);
            }
        }       
    }
    
    public List<WizardComponent> getWizardComponents() {
        return wizardComponents;
    }
    
    @Override
    public boolean registerInSystem() {
        return false;
    }
    
    @Override
    public RemovalMode getRemovalMode() {
        return RemovalMode.LIST;
    }

    @Override
    public Text getLicense() {
        return null;
    }

    protected void checkNetbeansRunning() {
        List<Dependency> dependencies =
                getProduct().getDependencyByUid(BASE_IDE_UID);
        
        if (dependencies.size() > 0 ) {
            List<Product> sources =
                    Registry.getInstance().getProducts(dependencies.get(0));
            
            // pick the first one and integrate with it
            final File nbLocation = sources.get(0).getInstallationLocation();
            NetBeansUtils.warnNetbeansRunning(nbLocation);
        } else {
            LogManager.log(ErrorLevel.DEBUG,
                    "... no dependencies on IDE was found for " + getProduct().getDisplayName());
            LogManager.log(ErrorLevel.DEBUG,
                    "... so skipping checking for running netbeans IDE");
        }
    }
}
