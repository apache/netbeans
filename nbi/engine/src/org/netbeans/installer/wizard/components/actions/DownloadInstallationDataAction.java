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

package org.netbeans.installer.wizard.components.actions;

import java.util.List;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.RegistryType;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.progress.CompositeProgress;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardAction;

public class DownloadInstallationDataAction extends WizardAction {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(DownloadInstallationDataAction.class,
            "DIDA.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(DownloadInstallationDataAction.class,
            "DIDA.description"); // NOI18N
    
    public static final String DEFAULT_PROGRESS_TITLE_LOCAL =
            ResourceUtils.getString(DownloadInstallationDataAction.class,
            "DIDA.progress.local.title"); //NOI18N
    public static final String PROGRESS_TITLE_LOCAL_PROPERTY =
            "progress.title.local";//NOI18N
    
    public static final String DEFAULT_PROGRESS_TITLE_REMOTE =
            ResourceUtils.getString(DownloadInstallationDataAction.class,
            "DIDA.progress.remote.title"); //NOI18N
    public static final String PROGRESS_TITLE_REMOTE_PROPERTY =
            "progress.title.remote";//NOI18N
    
    public static final String DEFAULT_DOWNLOAD_FAILED_EXCEPTION =
            ResourceUtils.getString(DownloadInstallationDataAction.class,
            "DIDA.failed"); //NOI18N
    public static final String DOWNLOAD_FAILED_EXCEPTION_PROPERTY =
            "download.failed";//NOI18N
    
    public static final String DEFAULT_DEPENDENT_FAILED_EXCEPTION =
            ResourceUtils.getString(DownloadInstallationDataAction.class,
            "DIDA.dependent.failed"); //NOI18N    
    public static final String DEPENDENT_FAILED_EXCEPTION_PROPERTY =
            "download.dependent.failed"; //NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private CompositeProgress overallProgress;
    private Progress          currentProgress;
    
    public DownloadInstallationDataAction() {
        setProperty(TITLE_PROPERTY, DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION);
        setProperty(PROGRESS_TITLE_LOCAL_PROPERTY, DEFAULT_PROGRESS_TITLE_LOCAL);
        setProperty(PROGRESS_TITLE_REMOTE_PROPERTY, DEFAULT_PROGRESS_TITLE_REMOTE);
        setProperty(DOWNLOAD_FAILED_EXCEPTION_PROPERTY, DEFAULT_DOWNLOAD_FAILED_EXCEPTION);
        setProperty(DEPENDENT_FAILED_EXCEPTION_PROPERTY, DEFAULT_DEPENDENT_FAILED_EXCEPTION);
    }
    
    public void execute() {
        LogManager.logEntry("getting all installation data");
        final Registry registry = Registry.getInstance();
        final List<Product> products = registry.getProductsToInstall();
        final int percentageChunk = Progress.COMPLETE / products.size();
        final int percentageLeak = Progress.COMPLETE % products.size();
        
        overallProgress = new CompositeProgress();
        overallProgress.setPercentage(percentageLeak);
        overallProgress.synchronizeDetails(true);
        
        getWizardUi().setProgress(overallProgress);
        for (int i = 0; i < products.size(); i++) {
            // get the handle of the current item
            final Product product = products.get(i);
            
            // initiate the progress for the current element
            currentProgress = new Progress();
            
            overallProgress.addChild(currentProgress, percentageChunk);
            try {
                String prop = product.getRegistryType() == RegistryType.REMOTE ?
                    PROGRESS_TITLE_REMOTE_PROPERTY :
                    PROGRESS_TITLE_LOCAL_PROPERTY;
                String overallProgressTitle = StringUtils.format(
                        getProperty(prop), product.getDisplayName());
                overallProgress.setTitle(overallProgressTitle);
                
                product.downloadData(currentProgress);
                
                // check for cancel status
                if (isCanceled()) return;
                
                // sleep a little so that the user can perceive that something
                // is happening
                SystemUtils.sleep(200);
            }  catch (DownloadException e) {
                // wrap the download exception with a more user-friendly one
                InstallationException error = new InstallationException(
                         StringUtils.format(
                        getProperty(DOWNLOAD_FAILED_EXCEPTION_PROPERTY),
                        product.getDisplayName()), e);
                
                // adjust the product's status and save this error - it will
                // be reused later at the PostInstallSummary
                product.setStatus(Status.NOT_INSTALLED);
                product.setInstallationError(error);
                
                // since the installation data for the current product failed to
                // be downloaded, we should cancel the installation of the products
                // that may require this one
                for(Product dependent: registry.getProducts()) {
                    if ((dependent.getStatus()  == Status.TO_BE_INSTALLED) &&
                            registry.satisfiesRequirement(product, dependent)) {
                       String exString = StringUtils.format(
                                getProperty(DEPENDENT_FAILED_EXCEPTION_PROPERTY),
                                dependent.getDisplayName(),
                                product.getDisplayName());
                        
                        final InstallationException dependentError =
                                new InstallationException(exString, error);
                        
                        dependent.setStatus(Status.NOT_INSTALLED);
                        dependent.setInstallationError(dependentError);
                        
                        products.remove(dependent);
                    }
                }
                
                // finally notify the user of what has happened
                LogManager.log(ErrorLevel.ERROR, error);
            }
        }
        LogManager.logExit("... finished getting of the installation data");
    }
    
    public void cancel() {
        if (currentProgress != null) {
            currentProgress.setCanceled(true);
        }
        
        if (overallProgress != null) {
            overallProgress.setCanceled(true);
        }
        
        super.cancel();
    }
    
    public boolean canExecuteForward() {
        return Registry.getInstance().getProductsToInstall().size() > 0;
    }
    
    public boolean isPointOfNoReturn() {
        return true;
    }
}
