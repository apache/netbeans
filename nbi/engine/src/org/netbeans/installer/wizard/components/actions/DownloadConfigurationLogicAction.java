/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.wizard.components.actions;

import java.util.List;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.RegistryType;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.progress.CompositeProgress;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardAction;

public class DownloadConfigurationLogicAction extends WizardAction {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(DownloadConfigurationLogicAction.class,
            "DCLA.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(DownloadConfigurationLogicAction.class,
            "DCLA.description"); // NOI18N
    
    
    public static final String DEFAULT_PROGRESS_TITLE_LOCAL =
            ResourceUtils.getString(DownloadConfigurationLogicAction.class,
            "DCLA.progress.local.title"); //NOI18N
    public static final String PROGRESS_TITLE_LOCAL_PROPERTY =
            "progress.title.local";//NOI18N
    
    public static final String DEFAULT_PROGRESS_TITLE_REMOTE =
            ResourceUtils.getString(DownloadConfigurationLogicAction.class,
            "DCLA.progress.remote.title"); //NOI18N
    public static final String PROGRESS_TITLE_REMOTE_PROPERTY =
            "progress.title.remote";//NOI18N
    
    public static final String DEFAULT_DOWNLOAD_FAILED_EXCEPTION =
            ResourceUtils.getString(DownloadConfigurationLogicAction.class,
            "DCLA.failed"); //NOI18N
    public static final String DOWNLOAD_FAILED_EXCEPTION_PROPERTY =
            "download.failed";//NOI18N
    
    public static final String DEFAULT_DEPENDENT_FAILED_EXCEPTION =
            ResourceUtils.getString(DownloadConfigurationLogicAction.class,
            "DCLA.dependent.failed"); //NOI18N
    
    public static final String DEPENDENT_FAILED_EXCEPTION_PROPERTY =
            "download.dependent.failed"; //NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private CompositeProgress overallProgress;
    private Progress          currentProgress;
    
    public DownloadConfigurationLogicAction() {
        setProperty(TITLE_PROPERTY, DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION);
        setProperty(PROGRESS_TITLE_LOCAL_PROPERTY, DEFAULT_PROGRESS_TITLE_LOCAL);
        setProperty(PROGRESS_TITLE_REMOTE_PROPERTY, DEFAULT_PROGRESS_TITLE_REMOTE);
        setProperty(DOWNLOAD_FAILED_EXCEPTION_PROPERTY, DEFAULT_DOWNLOAD_FAILED_EXCEPTION);
        setProperty(DEPENDENT_FAILED_EXCEPTION_PROPERTY, DEFAULT_DEPENDENT_FAILED_EXCEPTION);
    }
    
    public boolean canExecuteForward() {
        for (Product product: Registry.getInstance().getProductsToInstall()) {
            if (!product.isLogicDownloaded()) {
                return true;
            }
        }
        
        return false;
    }
    
    public void execute() {
        final Registry registry = Registry.getInstance();
        final List<Product> products = registry.getProductsToInstall();
        final int percentageChunk = Progress.COMPLETE / products.size();
        final int percentageLeak  = Progress.COMPLETE % products.size();
        
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
                
                product.downloadLogic(currentProgress);
                
                // ensure that the current progress has reached the complete state
                // (sometimes it just does not happen and we're left over with 99%)
                currentProgress.setPercentage(Progress.COMPLETE);
                
                // check for cancel status
                if (isCanceled()) return;
                
                // sleep a little so that the user can perceive that something
                // is happening
                SystemUtils.sleep(200);
            } catch (DownloadException e) {
                // wrap the download exception with a more user-friendly one
                final InstallationException error = new InstallationException(
                        StringUtils.format(
                        getProperty(DOWNLOAD_FAILED_EXCEPTION_PROPERTY),
                        product.getDisplayName()), e);
                
                // adjust the product's status and save this error - it will
                // be reused later at the PostInstallSummary
                product.setStatus(Status.NOT_INSTALLED);
                product.setInstallationError(error);
                
                // since the configuration logic for the current product failed to
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
                ErrorManager.notify(ErrorLevel.ERROR, error);
            }
        }
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
