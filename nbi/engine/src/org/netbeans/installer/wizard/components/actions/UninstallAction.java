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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.installer.Installer;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.UninstallUtils;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.progress.CompositeProgress;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardAction;

public class UninstallAction extends WizardAction {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(UninstallAction.class,
            "UA.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(UninstallAction.class,
            "UA.description"); // NOI18N
    public static final String DEFAULT_PROGRESS_UNINSTALL_TITLE_MESSAGE =
            ResourceUtils.getString(UninstallAction.class,
            "UA.progress.uninstall.title");//NOI18N
    public static final String DEFAULT_UNINSTALL_DEPENDENT_FAILED_MESSAGE =
            ResourceUtils.getString(UninstallAction.class,
            "UA.uninstall.dependent.failed");//NOI18N
    
    public static final String PROGRESS_UNINSTALL_TITLE_PROPERTY =
            "progress.uninstall.title";    
    
    public static final String UNINSTALL_DEPENDENT_FAILED_PROPERTY =
            "uninstall.dependent.failed";
    public static final int UNINSTALLATION_ERROR_CODE = 
            126;//NOMAGI     
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private CompositeProgress overallProgress;
    private Progress          currentProgress;        
    
    public UninstallAction() {
        setProperty(TITLE_PROPERTY, DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION);
        setProperty(PROGRESS_UNINSTALL_TITLE_PROPERTY,
                DEFAULT_PROGRESS_UNINSTALL_TITLE_MESSAGE);
        setProperty(UNINSTALL_DEPENDENT_FAILED_PROPERTY,
                DEFAULT_UNINSTALL_DEPENDENT_FAILED_MESSAGE);
    }
    
    @Override
    public void execute() {
        LogManager.logIndent("Start products uninstallation");
        final Registry registry = Registry.getInstance();
        final List<Product> products = registry.getProductsToUninstall();
        final int percentageChunk = Progress.COMPLETE / products.size();
        final int percentageLeak = Progress.COMPLETE % products.size();
        
        overallProgress = new CompositeProgress();
        overallProgress.setPercentage(percentageLeak);
        overallProgress.synchronizeDetails(true);
        
        getWizardUi().setProgress(overallProgress);
        for (Product product : products) {         
            // initiate the progress for the current element
            currentProgress = new Progress();
            
            overallProgress.addChild(currentProgress, percentageChunk);
            overallProgress.setTitle(StringUtils.format(
                    getProperty(PROGRESS_UNINSTALL_TITLE_PROPERTY),
                    product.getDisplayName()));
            try {
                product.uninstall(currentProgress);
                
                // sleep a little so that the user can perceive that something
                // is happening
                SystemUtils.sleep(200);
            }  catch (UninstallationException e) {
                // do not override already set exit code
                if (System.getProperties().get(Installer.EXIT_CODE_PROPERTY) == null) {
                     System.getProperties().put(Installer.EXIT_CODE_PROPERTY, 
                             Integer.valueOf(UNINSTALLATION_ERROR_CODE));
                }
                // adjust the component's status and save this error - it will
                // be reused later at the PostInstallSummary
                product.setStatus(Status.INSTALLED);
                product.setUninstallationError(e);
                
                // since the product failed to uninstall  - we should remove
                // the components it depends on from our plans to uninstall
                for(Product requirement : registry.getProducts()) {
                    if ((requirement.getStatus() == Status.TO_BE_UNINSTALLED) &&
                            registry.satisfiesRequirement(requirement, product)) {
                        UninstallationException requirementError =
                                new UninstallationException(
                                StringUtils.format(
                                getProperty(PROGRESS_UNINSTALL_TITLE_PROPERTY),
                                requirement.getDisplayName(),
                                product.getDisplayName()), e);
                        
                        requirement.setStatus(Status.INSTALLED);
                        requirement.setUninstallationError(requirementError);
                        
                        products.remove(requirement);
                    }
                }
                
                // finally notify the user of what has happened
                ErrorManager.notify(ErrorLevel.ERROR, e);
            }
        }
        LogManager.logUnindent("... finished products uninstallation");
                
        LogManager.logUnindent("... starting updates and plugins uninstallation");        
        try {
            // delete updated files and downloaded plugins in installation folder
            FileUtils.deleteFiles(new ArrayList<File>(UninstallUtils.getFilesToDeteleAfterUninstallation()));            
            
            // delete all empty folders in installation directory                          
            FileUtils.deleteFiles(UninstallUtils.getEmptyFolders());
        } catch (IOException ex) {
            LogManager.log(ex);
        }
        LogManager.logUnindent("... finished updates and plugins uninstallation");        
    }        
    
    @Override
    public boolean canExecuteForward() {
        return Registry.getInstance().getProductsToUninstall().size() > 0;
    }
    
    @Override
    public boolean isPointOfNoReturn() {
        return true;
    }
    
    @Override
    public boolean isCancelable() {
        return false;
    }
}
