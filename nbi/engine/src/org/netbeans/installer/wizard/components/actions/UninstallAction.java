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
                             new Integer(UNINSTALLATION_ERROR_CODE));
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
