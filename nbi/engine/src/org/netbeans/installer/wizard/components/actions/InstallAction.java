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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.installer.Installer;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.filters.OrFilter;
import org.netbeans.installer.product.filters.ProductFilter;
import org.netbeans.installer.product.filters.RegistryFilter;
import org.netbeans.installer.utils.helper.DetailedStatus;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.progress.CompositeProgress;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardAction;

public class InstallAction extends WizardAction {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants

    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(InstallAction.class,
            "IA.title"); // NOI18N

    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(InstallAction.class,
            "IA.description"); // NOI18N

    public static final String DEFAULT_PROGRESS_INSTALL_TITLE =
            ResourceUtils.getString(InstallAction.class,
            "IA.progress.install.title"); // NOI18N

    public static final String PROGRESS_INSTALL_TITLE_PROPERTY =
            "progress.install.title"; //NOI18N

    public static final String DEFAULT_PROGRESS_ROLLBACK_TITLE =
            ResourceUtils.getString(InstallAction.class,
            "IA.progress.rollback.title"); // NOI18N

    public static final String PROGRESS_ROLLBACK_TITLE_PROPERTY =
            "progress.rollback.title"; //NOI18N

    public static final String DEFAULT_INSTALL_DEPENDENT_FAILED_EXCEPTION =
            ResourceUtils.getString(InstallAction.class,
            "IA.install.dependent.failed");//NOI18N

    public static final String INSTALL_DEPENDENT_FAILED_EXCEPTION_PROPERTY =
            "install.dependent.failed";
    public static final String DEFAULT_INSTALL_UNKNOWN_ERROR =
            ResourceUtils.getString(InstallAction.class,
            "IA.install.unknown.error");//NOI18N

    public static final String INSTALL_UNKNOWN_ERROR_PROPERTY =
            "install.unknown.error";
    
    public static final int INSTALLATION_ERROR_CODE = 
            127;//NOMAGI
    /////////////////////////////////////////////////////////////////////////////////
    // Instance

    private CompositeProgress overallProgress;
    private Progress currentProgress;

    public InstallAction() {
        setProperty(TITLE_PROPERTY, DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION);
        setProperty(PROGRESS_INSTALL_TITLE_PROPERTY,
                DEFAULT_PROGRESS_INSTALL_TITLE);
        setProperty(PROGRESS_ROLLBACK_TITLE_PROPERTY,
                DEFAULT_PROGRESS_ROLLBACK_TITLE);
        setProperty(INSTALL_DEPENDENT_FAILED_EXCEPTION_PROPERTY,
                DEFAULT_INSTALL_DEPENDENT_FAILED_EXCEPTION);
        setProperty(INSTALL_UNKNOWN_ERROR_PROPERTY,
                DEFAULT_INSTALL_UNKNOWN_ERROR);
    }

    public boolean canExecuteForward() {
        return Registry.getInstance().getProductsToInstall().size() > 0;
    }

    public boolean isPointOfNoReturn() {
        return true;
    }

    public void execute() {
        LogManager.logIndent("Start products installation");
        final Registry registry = Registry.getInstance();
        final List<Product> products = registry.getProductsToInstall();
        int percentageChunk = Progress.COMPLETE / products.size();
        int percentageLeak = Progress.COMPLETE % products.size();          
        final Map<Product, Progress> progresses = new HashMap<Product, Progress>();

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
            overallProgress.setTitle(StringUtils.format(getProperty(PROGRESS_INSTALL_TITLE_PROPERTY),
                    product.getDisplayName()));
            boolean isProductRolledback = false;
            try {
                product.install(currentProgress);

                if (isCanceled()) {
                    LogManager.log("... installation is cancelled : " + 
                            product.getDisplayName() + 
                            "(" + product.getUid() + "/" + product.getVersion() + ")");
                    overallProgress.setTitle(StringUtils.format(getProperty(PROGRESS_ROLLBACK_TITLE_PROPERTY),
                            product.getDisplayName()));
                    product.rollback(currentProgress);                    
                    isProductRolledback = true;


                    for (Product toInstall : registry.getProductsToInstall()) {
                        LogManager.log("... marking to-be-installed product as not-installed : " + toInstall);
                        toInstall.setStatus(Status.NOT_INSTALLED);
                    }
                    
                    final RegistryFilter filter = new OrFilter(new ProductFilter(DetailedStatus.INSTALLED_SUCCESSFULLY),
                            new ProductFilter(DetailedStatus.INSTALLED_WITH_WARNINGS));
                    for (Product installed : registry.queryProducts(filter)) {
                        LogManager.log("... marking installed product as to-be-uninstalled : " + installed);
                        installed.setStatus(Status.TO_BE_UNINSTALLED);
                    }


                    for (Product toRollback : registry.getProductsToUninstall()) {
                        LogManager.log("... also rollbacking " + toRollback.getDisplayName() + 
                                "(" + toRollback.getUid() + "/" + toRollback.getVersion() + ")");
                        overallProgress.setTitle(StringUtils.format(getProperty(PROGRESS_ROLLBACK_TITLE_PROPERTY),
                                toRollback.getDisplayName()));
                        toRollback.rollback(progresses.get(toRollback));                        
                    }
                    break;
                }

                progresses.put(product, currentProgress);

                // sleep a little so that the user can perceive that something
                // is happening
                SystemUtils.sleep(200);
            } catch (Throwable e) {
                LogManager.log(e);
                if (!(e instanceof InstallationException)) {
                    e = new InstallationException(getProperty(INSTALL_UNKNOWN_ERROR_PROPERTY), e);
                }
                // do not override already set exit code
                if (System.getProperties().get(Installer.EXIT_CODE_PROPERTY) == null) {
                     System.getProperties().put(Installer.EXIT_CODE_PROPERTY, 
                             Integer.valueOf(INSTALLATION_ERROR_CODE));
                }
                // adjust the product's status and save this error - it will
                // be reused later at the PostInstallSummary
                product.setStatus(Status.NOT_INSTALLED);
                product.setInstallationError(e);                               

                // since the current product failed to install, we should cancel the
                // installation of the products that may require this one
                for (Product dependent : registry.getProducts()) {
                    if ((dependent.getStatus() == Status.TO_BE_INSTALLED) &&
                            registry.satisfiesRequirement(product, dependent)) {
                        final String exceptionName = StringUtils.format(getProperty(INSTALL_DEPENDENT_FAILED_EXCEPTION_PROPERTY),
                                dependent.getDisplayName(),
                                product.getDisplayName());

                        final InstallationException dependentError =
                                new InstallationException(exceptionName, e);

                        dependent.setStatus(Status.NOT_INSTALLED);
                        dependent.setInstallationError(dependentError);

                        products.remove(dependent);
                    }
                }

                if(!isProductRolledback) {
                    try{                         
                        overallProgress.setTitle(StringUtils.format(
                                getProperty(PROGRESS_ROLLBACK_TITLE_PROPERTY),
                                product.getDisplayName()));
                        product.rollback(currentProgress);                                           
                    }catch(Exception uie) {
                        LogManager.log(uie);                        
                    }
                }
                overallProgress.removeChild(currentProgress);                
                final int productsToInstallSize = registry.getProductsToInstall().size();                
                if(productsToInstallSize > 0) {
                    final int theRestOfPercentage = Progress.COMPLETE - 
                            overallProgress.getPercentage();
                    percentageChunk = theRestOfPercentage / productsToInstallSize;
                    percentageLeak =  theRestOfPercentage % productsToInstallSize;                    
                    overallProgress.addPercentage(percentageLeak);                                   
                }

                 // finally notify the user of what has happened
                 LogManager.log(ErrorLevel.ERROR, e);
            }            
        }
        LogManager.logUnindent("... finished products installation");
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
}
